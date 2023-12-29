<template>
  <div style="width: 96vw; margin-left: 2vw; height: 70vh">
    <canvas id="reject-rate-chart-view"></canvas>
    <div
      id="reject-rate-chart-view-legend"
      v-if="requestParam.chartType === chartTypeOption.YIELD_RATE"
    >
      <div class="line-legend-wrapper">
        <div class="legend-icon">
          <img
            style="width: 54px; height: 8px"
            src="/images/graph/shot-icon.svg"
          />
        </div>
        <div class="legend-title">
          <span v-text="resources['yield_rate']"></span>
        </div>
      </div>
    </div>
    <div
      id="reject-rate-chart-view-legend"
      v-if="requestParam.chartType === chartTypeOption.BREAKDOWN"
    >
      <div class="line-legend-wrapper">
        <div class="legend-icon">
          <div class="icon-line"></div>
          <div class="icon-line"></div>
          <div class="icon-line"></div>
          <div class="icon-line"></div>
        </div>
        <div class="legend-title">
          <span v-text="resources['cum_percentage_curve']"></span>
        </div>
      </div>
      <div class="bar-legend-wrapper">
        <div
          class="legend-item"
          v-for="(legend, index) in breakdownBarLegends"
          :key="index"
        >
          <div
            class="legend-icon"
            :style="{ backgroundColor: legend.color }"
          ></div>
          <div class="legend-title">{{ legend.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
const breakdownColors = [
  "#f1cc68",
  "#8fe6d5",
  "#a2daf1",
  "#e67c7a",
  "#b3225d",
  "#8bb487",
  "#5a4645",
  "#808080",
  "#f85a51",
  "#7383c1",
  "#795cc8",
  "#efeeee",
];
module.exports = {
  props: {
    resources: Object,
    chartParam: String,
  },
  data() {
    return {
      chartTypeOption: {
        YIELD_RATE: "YIELD_RATE",
        BREAKDOWN: "BREAKDOWN",
      },
      breakdownBarLegends: [],
      chartConfig: {},
      id: "",
      requestParam: {
        moldId: null,
        partId: null,
        startDate: null,
        frequent: "",
        chartType: "",
      },
    };
  },
  mounted() {
    this.requestParam.chartType = this.chartTypeOption.YIELD_RATE;
    this.seprateParam();
    this.setupRejectChart();
    this.showRejectDetail();
  },
  methods: {
    seprateParam() {
      const chartParam = this.chartParam.split("?");
      if (chartParam[0]) {
        this.id = chartParam[0];
      }
      if (chartParam[1]) {
        const paramArray = chartParam[1].split("&");
        paramArray.forEach((item) => {
          let result = item.split("=");
          if (result[1] != "" && result[1] != null)
            this.requestParam[result[0]] = result[1];
        });
        console.log("this.requestParam", this.requestParam);
        this.tempDateData = {
          from: new Date(this.requestParam.start),
          end: new Date(this.requestParam.end),
          fromTitle: moment(this.requestParam.start, "YYYYMMDD").format(
            "YYYY-MM-DD"
          ),
          toTitle: moment(this.requestParam.start, "YYYYMMDD").format(
            "YYYY-MM-DD"
          ),
        };
      }
    },
    showRejectDetail() {
      switch (this.requestParam.frequent) {
        case "WEEKLY":
          delete this.requestParam.month;
          delete this.requestParam.day;
          this.requestParam.startDate = moment(this.tempDateData.from).format(
            "YYYYMMDD"
          );
          break;
        case "MONTHLY":
          delete this.requestParam.day;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          break;
        case "DAILY":
          delete this.requestParam.month;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          break;
        case "HOURLY":
          delete this.requestParam.month;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          delete this.requestParam.day;
          break;
      }
      this.bindDetailData();
    },
    bindDetailData() {
      if (!this.requestParam.moldId || !this.requestParam.partId) {
        return;
      }
      let param = Common.param(this.requestParam);
      axios
        .get("/api/rejected-part?" + param)
        .then((response) => {
          if (this.requestParam.chartType === this.chartTypeOption.YIELD_RATE) {
            console.log("response", response.data.content);
            this.bindYieldRateChart(response.data.content);
          }
        })
        .catch((error) => {
          console.log(error);
        });
      if (this.requestParam.chartType === this.chartTypeOption.BREAKDOWN) {
        axios
          .get("/api/rejected-part/breakdown-chart?" + param)
          .then((response) => {
            console.log("bindBreakdownChart", response.data);
            this.bindBreakdownChart(response.data);
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    setDefaultStartDateAndEndDate(startDate) {
      let previousWeek = moment(startDate, this.dateFormat).week() - 1;
      if (previousWeek === moment().week()) {
        previousWeek = previousWeek - 1;
      }
      let year = moment(startDate, this.dateFormat).year();
      if (previousWeek < 1) {
        year = year - 1;
        previousWeek = moment(year + "-31-12", "YYYY-MM-DD").week() - 1;
      }
      this.requestParam.startDate = moment()
        .week(previousWeek)
        .year(year)
        .weekday(1)
        .format(this.dateFormat);
      this.requestParam.endDate = moment()
        .week(previousWeek)
        .year(year)
        .weekday(7)
        .format(this.dateFormat);
      let child = Common.vue.getChild(this.$children, "date-detail-filter");
      if (child != null) {
        child.initOptionValue(previousWeek, year);
      }
    },
    enumerateDaysBetweenDates(startDate, endDate, frequent) {
      console.log("enumerateDaysBetweenDates", startDate, endDate, frequent);

      if (this.requestParam.frequent != "WEEKLY") {
        let currDate = null;
        let lastDate = null;
        let format = "";
        let step = "";
        let numberOfDate = 0;
        switch (this.requestParam.frequent) {
          case "DAILY":
            format = "MM/DD";
            step = "days";
            currDate = moment(startDate, "YYYY-MM-DD").startOf("day");
            lastDate = moment(endDate, "YYYY-MM-DD").startOf("day");
            numberOfDate = lastDate.diff(currDate, "days") + 1;
            break;
          case "WEEKLY":
            format = "YYYY/WW";
            step = "weeks";
            currDate = moment(startDate, "YYYY-ww").startOf("weeks");
            lastDate = moment(endDate, "YYYY-ww").startOf("weeks");
            console.log("check date weekly", currDate, lastDate);
            numberOfDate = lastDate.diff(currDate, "weeks") + 1;
            break;
          case "MONTHLY":
            format = "YYYY/MM";
            step = "months";
            currDate = moment(startDate, "YYYY-MM").startOf("month");
            lastDate = moment(endDate, "YYYY-MM").startOf("month");
            numberOfDate = lastDate.diff(currDate, "months") + 1;
            break;
        }

        console.log(
          "enumerateDaysBetweenDates 2",
          currDate,
          lastDate,
          numberOfDate
        );

        let response = [];
        for (let index = 0; index < numberOfDate; index++) {
          response.push(currDate.format(format));
          currDate.add(1, step);
        }
        // while (currDate.diff(lastDate) < 0) {
        //   response.push(currDate.format(this.dateFormat));
        //   currDate.add(1, "days");
        // }
        console.log(response, "respone");
        return response;
      } else {
        let firstYear = startDate.split("-")[0];
        let firstWeek = startDate.split("-")[1];
        let secondYear = endDate.split("-")[0];
        let secondWeek = endDate.split("-")[1];
        let response = [];

        if (firstYear == secondYear) {
          while (firstWeek <= secondWeek) {
            response.push(
              `${firstYear}/${+firstWeek > 9 ? +firstWeek : "0" + +firstWeek}`
            );
            firstWeek = +firstWeek + 1;
          }
        } else {
          let yearGarp = secondYear - firstYear;
          for (
            let index = firstWeek;
            index <= this.getNumberOfWeek(firstYear);
            index++
          ) {
            response.push(`${firstYear}/${index > 9 ? index : "0" + index}`);
          }
          if (yearGarp > 1) {
            for (let i = 1; i < yearGarp; i++) {
              for (let j = 1; j < this.getNumberOfWeek(firstYear + i); j++) {
                response.push(`${firstYear + i}/${j > 9 ? j : "0" + j}`);
              }
            }
          }
          for (let index = 0; index <= secondWeek; index++) {
            response.push(`${secondYear}/${index > 9 ? index : "0" + index}`);
          }
        }
        console.log(response, "respone");
        return response;
      }
    },
    getDateBetween() {
      let startDate = null;
      let endDate = null;
      if (["MONTHLY", "WEEKLY", "DAILY"].includes(this.requestParam.frequent)) {
        startDate = this.tempDateData.fromTitle;
        endDate = this.tempDateData.toTitle;
      }
      let days = this.enumerateDaysBetweenDates(
        startDate,
        endDate,
        this.requestParam.frequent
      );
      return days;
    },
    bindYieldRateChart(chartData) {
      this.resetConfigForYieldRateChart();
      this.chartConfig.data.labels = [];
      this.chartConfig.data.datasets = [];
      this.chart.update();
      let dataset = {
        type: "line",
        label: this.resources["yield_rate"],
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      // sort
      chartData.sort((first, second) => {
        return first.day > second.day ? 1 : -1;
      });
      let days = this.getDateBetween();
      console.log("Date Between", days);
      days.forEach((day) => {
        // let date = moment(day, this.dateFormat).format("MM/DD");
        // if (this.requestParam.frequent.includes("DAILY")) {
        //   this.chartConfig.data.labels.push(day);
        // } else {
        //   this.chartConfig.data.labels.push(this.getTitle);
        // }
        this.chartConfig.data.labels.push(day);
        dataset.data.push(null);
      });
      console.log("chartData", chartData);
      chartData.forEach((item) => {
        let date = this.getTitle;
        if (this.requestParam.frequent.includes("DAILY")) {
          date = moment(item.day, this.dateFormat).format("MM/DD");
        } else if (this.requestParam.frequent.includes("WEEKLY")) {
          date = item.week;
        } else if (this.requestParam.frequent.includes("MONTHLY")) {
          date = item.month;
        }
        let dataIndex = null;
        this.chartConfig.data.labels.forEach((label, index) => {
          // if (label === date) {
          //   dataIndex = index;
          // }
          console.log("index", index);
          dataIndex = index;
        });
        if (dataIndex !== null) {
          dataset.data[dataIndex] = item.yieldRate < 100 ? item.yieldRate : 100;
        }
      });

      // dataset.data.push(dataset);
      this.chartConfig.data.datasets.push(dataset);
      this.chart.update();
    },
    bindBreakdownChart(chartData) {
      chartData = chartData.filter((item) => item.rejectedAmount);
      chartData.sort((first, second) => {
        return first.rejectedRate < second.rejectedRate ? 1 : -1;
      });
      this.resetConfigForBreakdownChart();
      this.chartConfig.data.labels = [];
      this.chartConfig.data.datasets = [];
      this.chart.update();

      this.breakdownBarLegends = [];
      let tmpRejectedRateRate = 0;
      // line chart
      let lineDataset = {
        type: "line",
        label: this.resources["cum_percentage_curve"],
        yAxisID: "right",
        backgroundColor: "transparent",
        borderColor: "black",
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      let dataset = {
        type: "bar",
        label: "",
        yAxisID: "left",
        backgroundColor: [],
        borderColor: [],
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };
      chartData.forEach((item, index) => {
        let color = breakdownColors[index];
        if (!color) {
          color = Common.getRandomElement(breakdownColors);
        }

        // bar chart

        tmpRejectedRateRate += item.rejectedRate;
        if (tmpRejectedRateRate > 100) {
          tmpRejectedRateRate = 100;
        }

        lineDataset.data.push(tmpRejectedRateRate);

        dataset.backgroundColor.push(color);
        dataset.borderColor.push(color);
        dataset.data.push(item.rejectedAmount);
        this.chartConfig.data.labels.push(item.reason);
        this.breakdownBarLegends.push({
          name: item.reason,
          color: color,
        });
      });

      this.chartConfig.data.datasets.push(lineDataset);
      this.chartConfig.data.datasets.push(dataset);
      this.chart.update();
    },
    setupRejectChart() {
      this.chartConfig = this.getChartConfig();
      this.chart = new Chart($("#reject-rate-chart-view"), this.chartConfig);
    },
    getChartConfig() {
      return {
        type: "bar",
        data: {
          labels: [],
          datasets: [],
        },
        options: {
          maintainAspectRatio: false,
          legend: {
            display: true,
            position: "bottom",
          },
          tooltips: {
            mode: "index",
            intersect: false,
          },
          scales: {
            xAxes: [
              {
                scaleLabel: {
                  display: true,
                },
              },
            ],
            yAxes: [
              {
                stacked: false,
                position: "left",
                id: "left",
                ticks: {
                  beginAtZero: true,
                  min: 0,
                },
                scaleLabel: {
                  display: true,
                },
              },
            ],
          },
          elements: {
            point: {
              radius: 0,
              hitRadius: 10,
              hoverRadius: 4,
              hoverBorderWidth: 3,
            },
          },
        },
      };
    },
    resetConfigForYieldRateChart() {
      if (this.chartConfig && this.chartConfig.options) {
        delete this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString;
      }
      this.chartConfig.options.scales.xAxes = [
        {
          scaleLabel: {
            display: true,
          },
        },
      ];

      this.chartConfig.options.scales.yAxes = [
        {
          stacked: false,
          position: "left",
          id: "left",
          ticks: {
            beginAtZero: true,
            callback: function (value) {
              if (value > 100) return null;
              return value + "%";
            },
            min: 0,
            max: 105,
          },
          scaleLabel: {
            display: true,
          },
        },
      ];

      this.chartConfig.options.legend.display = false;
      this.chartConfig.options.legend.legendCallback = function (chart) {
        let text = `<div>
              <div class="legend-item" id="legend-item-0">
                  <div class="chart-legend">
                      <div class="legend-icon" style="height: 1px; border-top: 3px solid #63C2DE"></div>
                      <div class="label">Yield Rate</div>
                  </div>
              </div>
              <div class="clear"></div>
          </div>`;
        return text;
      };

      this.chartConfig.options.tooltips = {
        mode: "index",
        intersect: false,
        callbacks: {
          afterLabel: function () {
            return "";
          },
          label: function (tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel)
              .toFixed(2)
              .replace(".00", "")
              .toString();
            return `${label}: ${value}%`;
          },
        },
      };
      console.log("config", this.chartConfig);
    },
    resetConfigForBreakdownChart() {
      this.chartConfig.options.scales.xAxes = [
        {
          scaleLabel: {
            display: false,
          },
          ticks: {
            display: false,
          },
        },
      ];
      this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString =
        "Frequency";
      this.chartConfig.options.scales.yAxes = [
        {
          stacked: false,
          position: "left",
          id: "left",
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          scaleLabel: {
            display: true,
            labelString: this.resources["frequency"],
          },
        },
        {
          stacked: false,
          id: "right",
          position: "right",
          ticks: {
            beginAtZero: true,
            callback: function (value) {
              if (value > 100) return null;
              return value + "%";
            },
            min: 0,
            max: 105,
          },
          gridLines: {
            display: false,
          },
          scaleLabel: {
            display: true,
            labelString: this.resources["cum_percentage"],
          },
        },
      ];
      this.chartConfig.options.legend.display = false;
      this.chartConfig.options.tooltips = {
        enabled: true,
        intersect: true,
        callbacks: {
          afterLabel: function () {
            return "";
          },
          title: function () {},
          label: function (tooltipItem, data) {
            let label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel)
              .toFixed(2)
              .replace(".00", "")
              .toString();
            if (tooltipItem.datasetIndex === 1) {
              label = tooltipItem.xLabel;
              return `${label}: ${value}`;
            }
            return `${label}: ${value}%`;
          },
        },
      };
    },
  },
};
</script>
<style scoped>
.line-legend-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.line-legend-wrapper .legend-icon {
  display: flex;
  align-items: center;
}
</style>
