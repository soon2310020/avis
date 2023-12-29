<template>
  <div :style="chartWrapStyle">
    <canvas class="chart" id="chart-detail"></canvas>

    <!-- chart legend -->
    <div id="legend-chart-detail">
      <div class="chart-legend">
        <div v-for="(item, index) of chartSet?.data?.datasets" :key="index">
          <div class="legend-item" :id="`legend-item-${index}`">
            <div
              v-if="item.type === 'line'"
              class="legend-icon"
              :style="`height: 1px; 
                                    border-top: ${item.borderWidth}px ${
                item?.borderDash ? 'dashed' : 'solid'
              } ${item.borderColor};
                                    `"
            ></div>
            <div
              v-if="item.type === 'bar'"
              class="legend-icon bar"
              :style="`
                                background-color: ${item.backgroundColor};
                                border-color: ${item.borderColor};
                                ${legendIconStyle}
                                `"
            ></div>
            <div
              :style="legendLabelStyle ? legendLabelStyle : ''"
              class="label"
            >
              <span v-if="item.label === 'Within (sec)'">
                Cycle Time (Within, L1, L2)
              </span>
              <span v-else-if="item.label === 'Within'"> Uptime </span>
              <span v-else> {{ item.label }} </span>
            </div>
          </div>
          <div class="clear"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// test url
// part url: http://localhost:8080/admin/parts/chart/partId=2533447&year=2022&month=1&chartDataType=QUANTITY&dateViewType=DAY
// mold url: http://localhost:8080/admin/molds/chart/year=2022&chartDataType=QUANTITY&dateViewType=DAY&moldId=120046&detailDate=&sort=id%2Casc&size=50&month=01
module.exports = {
  props: {
    chartWrapStyle: "string", // ex:) "width: 80vw; height: 40vw"
    chartParam: String,
    // part chart ex:) "partId=2533447&year=2022&month=1&chartDataType=QUANTITY&dateViewType=DAY"
    // mold chart ex:) "dateViewType=DAY&chartDataType=QUANTITY%2CCYCLE_TIME&year=2022&month=01&moldId=120046"
    type: String, // ex:) "mold" || "part"
    resources: Object,
    // ex:) "font-size: 1vw;"
    legendLabelStyle: {
      default: "",
      type: String,
    },
    // ex:) "width: 3vw; height: 1vw"
    legendIconStyle: {
      default: "",
      type: String,
    },
  },
  data() {
    return {
      responseChartData: [],
      chartSet: {},
      indivChartDataSet: {
        approvedCycleTime: {},
        cycleTimeL1: {},
        cycleTimeL2: {},
        cycleTimeWithin: {},
        maxCycleTime: {},
        minCycleTime: {},
        totalQuantity: {},
        maximumCapacity: {},
      },
      requestParam: {
        year: "",
        month: "",
        chartDataType: "",
        dateViewType: "",
        moldId: "",
      },
      chartTypeConfig: [],
    };
  },
  mounted() {
    const ctx = document.getElementById("chart-detail");
    axios.get("/json/chart-js/default-config.json").then((res) => {
      this.chartSet = res.data;
      this.$window.detailChart = new Chart(ctx, res.data);
      this.setRequestParam().then(this.getChartTypeConfig());
    });
  },
  methods: {
    setRequestParam() {
      console.log("setRequestParam number 1");
      return new Promise(() => {
        let paramArray = this.chartParam.split("&");
        paramArray.forEach((item) => {
          let result = item.split("=");
          this.requestParam[result[0]] = result[1];
        });
        this.requestParam.chartDataType =
          this.requestParam.chartDataType.split(",");
      });
    },
    getChartTypeConfig() {
      console.log("getChartTypeConfig number 2");
      return new Promise(() => {
        axios.get("/api/common/dsp-stp").then((res) => {
          this.chartTypeConfig = res.data.chartTypeConfig;
          this.getIndivChartDataSet().then(this.getChartData());
        });
      });
    },
    getIndivChartDataSet() {
      console.log("getIndivChartDataSet");
      return new Promise(() => {
        axios
          .get("/json/chart-js/individual-config/approved-cycle-time.json")
          .then((res) => {
            this.indivChartDataSet.approvedCycleTime = res.data;
            this.indivChartDataSet.approvedCycleTime.borderColor =
              getStyle("--success");
          });
        axios
          .get("/json/chart-js/individual-config/cycle-time-l1.json")
          .then((res) => {
            this.indivChartDataSet.cycleTimeL1 = res.data;
            console.log(
              "this.indivChartDataSet.cycleTimeL1.type: ",
              this.indivChartDataSet.cycleTimeL1.type
            );
          });
        axios
          .get("/json/chart-js/individual-config/cycle-time-l2.json")
          .then((res) => {
            this.indivChartDataSet.cycleTimeL2 = res.data;
            console.log(
              "this.indivChartDataSet.cycleTimeL2.type: ",
              this.indivChartDataSet.cycleTimeL1.type
            );
          });
        axios
          .get("/json/chart-js/individual-config/cycle-time-within.json")
          .then((res) => {
            this.indivChartDataSet.cycleTimeWithin = res.data;
            console.log(
              "this.indivChartDataSet.cycleTimeWithin.type: ",
              this.indivChartDataSet.cycleTimeL1.type
            );
          });
        axios
          .get("/json/chart-js/individual-config/max-cycle-time.json")
          .then((res) => {
            this.indivChartDataSet.maxCycleTime = res.data;
            this.indivChartDataSet.maxCycleTime.borderColor =
              getStyle("--warning");
          });
        axios
          .get("/json/chart-js/individual-config/min-cycle-time.json")
          .then((res) => {
            this.indivChartDataSet.minCycleTime = res.data;
            this.indivChartDataSet.minCycleTime.borderColor =
              getStyle("--danger");
          });
        axios
          .get("/json/chart-js/individual-config/maximum-capacity.json")
          .then((res) => {
            this.indivChartDataSet.maximumCapacity = res.data;
          });
        axios
          .get("/json/chart-js/individual-config/total-quantity.json")
          .then((res) => {
            this.indivChartDataSet.totalQuantity = res.data;
            this.indivChartDataSet.totalQuantity.type =
              this.chartTypeConfig.filter(
                (item) => item.chartDataType === "PART_QUANTITY"
              )[0].chartType === "LINE"
                ? "line"
                : "bar";
          });
      });
    },
    getChartData() {
      console.log("getChartData number 3");
      return new Promise(() => {
        let apiUrl = "";
        if (this.type === "part") {
          apiUrl = "/api/chart/part?";
        }
        if (this.type === "mold") {
          apiUrl = "/api/chart/molds?";
        }
        axios.get(apiUrl + this.chartParam).then((res) => {
          this.responseChartData = res.data;
          this.bindAllChartData();
        });
      });
    },
    setChartPrepare1() {
      return new Promise(() => {
        this.chartSet.data.labels = [];
        this.chartSet.data.datasets = [];

        for (let i = 0; i < this.responseChartData.length; i++) {
          this.setChartLabel(i);
          this.setIndivChartDataSet(i);
        }
      });
    },
    setChartPrepare2() {
      this.setChartDatasets();
      this.setChartOptions();
      this.setChartJsTooltip();
    },
    setChartUpdatePrepare() {
      console.log("setChartUpdatePrepare");

      return new Promise(() => {
        this.$window.detailChart.options = this.chartSet.options;
        this.$window.detailChart.data = this.chartSet.data;
      });
    },
    setChartUpdate() {
      console.log("setChartUpdate");

      return new Promise(() => {
        console.log(
          "this.$window.detailChart.options:",
          this.$window.detailChart.options
        );
        console.log(
          "this.$window.detailChart.data.datasets:",
          this.$window.detailChart.data.datasets
        );
        this.$window.detailChart.update();
        this.$forceUpdate();
        console.log("this.$window.detailChart.update!!");
      });
    },
    bindAllChartData() {
      console.log("bindAllChartData number 4");
      this.setChartPrepare1()
        .then(this.setChartPrepare2())
        .then(this.setChartUpdatePrepare())
        .then(this.setChartUpdate());
    },
    // chart tooltip setting
    setChartJsTooltip() {
      console.log("setChartJsTooltip");

      const self = this;
      this.chartSet.options.tooltips = {
        enabled: false,
        custom: function (tooltipModel) {
          if (
            tooltipModel.body &&
            !tooltipModel.body[0].lines[0].includes("Total Part Quantity")
          ) {
            let tooltipEl = document.getElementById("chartjs-tooltip");
            let xLabel = "";
            let yLabel = 0;
            let itemIndex = 0;
            let name = "";
            if (tooltipModel.body[0].lines[0].includes("Min CT")) {
              name = "Min CT";
            } else if (
              tooltipModel.body[0].lines[0].includes("Maximum Capacity")
            ) {
              name = "Maximum Capacity";
            } else if (tooltipModel.body[0].lines[0].includes("L2")) {
              name = "L2";
            } else if (tooltipModel.body[0].lines[0].includes("L1")) {
              name = "L1";
            } else if (tooltipModel.body[0].lines[0].includes("Approved CT")) {
              name = "Approved CT";
            } else if (
              tooltipModel.body[0].lines[0].includes("Weighted Average CT")
            ) {
              name = "Weighted Average CT";
            } else if (tooltipModel.body[0].lines[0].includes("Max CT")) {
              name = "Max CT";
            }
            const { dataPoints } = tooltipModel;
            if (dataPoints && dataPoints.length > 0) {
              xLabel = Math.round(dataPoints[0].xLabel * 100) / 100;
              yLabel = Math.round(dataPoints[0].yLabel * 100) / 100;
              itemIndex = dataPoints[0].index;
            }
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = `
                            <div class="tooltips-container" >
                                <div class="tooltips-title">
                                    <div
                                        style="
                                        background-color: ${tooltipModel.labelColors[0].backgroundColor};
                                        border-color: ${tooltipModel.labelColors[0].borderColor}"
                                        class="box-title">
                                    </div>
                                    ${name}: ${yLabel}
                                </div>
                            </div>`;
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }

            tooltipEl.innerHTML = `
                        <div class="tooltips-container" >
                            <div class="tooltips-title">
                                <div
                                    style="
                                    background-color: ${
                                      tooltipModel.labelColors[0]
                                        .backgroundColor
                                    };
                                    border-color: ${
                                      tooltipModel.labelColors[0].borderColor
                                    }"
                                    class="box-title">
                                </div>
                                ${name}: ${yLabel
              .toString()
              .replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
                            </div>
                        </div>`;

            let position =
              self.$window.detailChart.canvas.getBoundingClientRect();
            tooltipEl.style.opacity = 0.9;
            tooltipEl.style.position = "absolute";
            tooltipEl.style[`z-index`] = 9999;
            tooltipEl.style.left =
              position.left +
              self.$window.pageXOffset +
              tooltipModel.caretX +
              "px";
            tooltipEl.style.top =
              position.top +
              self.$window.pageYOffset +
              (tooltipModel.caretY + 20) +
              "px";
            tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
            tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
            tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
            tooltipEl.style.padding =
              tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
            tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
            tooltipEl.style.borderRadius = "6px";
            tooltipEl.style.pointerEvents = "none";
            tooltipEl.style.transform = "translateX(-25%)";
          } else {
            let tooltipEl = document.getElementById("chartjs-tooltip");
            let xLabel = "";
            let yLabel = 0;
            let itemIndex = 0;
            const { dataPoints } = tooltipModel;

            if (dataPoints && dataPoints.length > 0) {
              xLabel = dataPoints[0].xLabel;
              yLabel = dataPoints[0].yLabel;
              itemIndex = dataPoints[0].index;
            }
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = `
                            <div class="tooltips-container" >
                                <div style="margin: 5px 0px;">${xLabel}</div>
                                <div class="tooltips-title">
                                    <div class="box-title"></div>
                                    Total Part Quantity: ${yLabel}
                                </div>
                                <table class="table"></table>
                            </div>`;
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }

            tooltipEl.innerHTML = `
                        <div class="tooltips-container" >
                            <div style="margin: 5px 0px;">${xLabel}</div>
                            <div class="tooltips-title">
                                <div class="box-title"></div>
                                Total Part Quantity: ${yLabel
                                  .toString()
                                  .replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
                            </div>
                            <table class="table"></table>
                        </div>`;

            if (tooltipModel.body) {
              let innerHtml = "<thead>";
              innerHtml += "<tr>";
              ["Tool ID", "Shots", "Avg.Cavities", "Quantity"].forEach(
                function (value) {
                  innerHtml += `<th>${value}</th>`;
                }
              );
              innerHtml += "</tr></thead>";
              if (
                self.responseChartData?.length > itemIndex &&
                itemIndex >= 0
              ) {
                self.responseChartData[itemIndex].moldShots &&
                  self.responseChartData[itemIndex].moldShots.map((value) => {
                    innerHtml += `
                                        <tr>
                                            <td style="text-align: center;">${
                                              value.moldCode
                                            }</td>
                                            <td style="text-align: center;">
                                                ${value.shotCount
                                                  .toString()
                                                  .replace(
                                                    /\B(?=(\d{3})+(?!\d))/g,
                                                    ","
                                                  )}
                                            </td>
                                            <td style="text-align: center;">
                                                ${Number(value.avgCavity)
                                                  .toFixed(2)
                                                  .toString()
                                                  .replace(
                                                    /\B(?=(\d{3})+(?!\d))/g,
                                                    ","
                                                  )}
                                            </td>
                                            <td style="text-align: center;">
                                                ${value.quantity
                                                  .toString()
                                                  .replace(
                                                    /\B(?=(\d{3})+(?!\d))/g,
                                                    ","
                                                  )}
                                            </td>
                                        </tr>`;
                  });
              }
              let tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
            }

            let position =
              self.$window.detailChart.canvas.getBoundingClientRect();
            tooltipEl.style.opacity = 1;
            tooltipEl.style.position = "absolute";
            tooltipEl.style[`z-index`] = 9999;
            tooltipEl.style.left =
              position.left +
              self.$window.pageXOffset +
              tooltipModel.caretX +
              "px";
            tooltipEl.style.top =
              position.top +
              self.$window.pageYOffset +
              (tooltipModel.caretY + 20) +
              "px";
            tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
            tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
            tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
            tooltipEl.style.padding =
              tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
            tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
            tooltipEl.style.pointerEvents = "none";
            tooltipEl.style.transform = "translateX(-25%)";
          }
        },
      };
    },
    // chart datasets setting
    setChartDatasets() {
      console.log("setChartDatasets");

      if (typeof this.requestParam.chartDataType !== "object") {
        this.requestParam.chartDataType = [this.requestParam.chartDataType];
      }
      if (this.requestParam.chartDataType.includes("QUANTITY")) {
        this.chartSet.data.datasets.push(this.indivChartDataSet.totalQuantity);
      }
      if (this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")) {
        this.chartSet.data.datasets.push(
          this.indivChartDataSet.approvedCycleTime
        );
      }
      if (this.requestParam.chartDataType.includes("CYCLE_TIME")) {
        this.chartSet.data.datasets.push(this.indivChartDataSet.minCycleTime);
        this.chartSet.data.datasets.push(this.indivChartDataSet.maxCycleTime);
        this.chartSet.data.datasets.push(this.indivChartDataSet.cycleTimeL1);
        this.chartSet.data.datasets.push(this.indivChartDataSet.cycleTimeL2);
        this.chartSet.data.datasets.push(
          this.indivChartDataSet.cycleTimeWithin
        );
      }
      if (this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY")) {
        this.chartSet.data.datasets.push(
          this.indivChartDataSet.maximumCapacity
        );
      }
    },
    // chart options setting
    setChartOptions() {
      console.log("setChartOptions");

      if (typeof this.requestParam.chartDataType !== "object") {
        this.requestParam.chartDataType = [this.requestParam.chartDataType];
      }
      if (
        !this.requestParam.chartDataType.includes("CYCLE_TIME") &&
        !this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")
      ) {
        this.chartSet.options.scales.yAxes[1].scaleLabel.labelString = "";
        this.chartSet.options.scales.yAxes[1].display = false;
      } else {
        this.chartSet.options.scales.yAxes[1].scaleLabel.labelString =
          "Cycle Time";
        this.chartSet.options.scales.yAxes[1].display = true;
      }
      this.chartSet.options.scales.xAxes[0].scaleLabel.labelString =
        this.toPascalCase(this.requestParam.dateViewType);
    },
    // chart label setting
    setChartLabel(i) {
      let label = this.responseChartData[i].title.replace(
        this.requestParam.year,
        "W-"
      );

      switch (this.requestParam.dateViewType) {
        case "DAY":
          label = label.replace("W-", "").substring(2, 4);
          break;
        case "MONTH":
          label = label.replace("W-", "");
          break;
        case "HOUR":
          label = moment(this.responseChartData[i].title, "YYYYMMDDHH").format(
            "HH"
          );
          break;
        case "WEEK":
          let year = this.responseChartData[i].title.substring(0, 4);
          let week = this.responseChartData[i].title.replace(year, "");
          label = `W${week}, ${year}`;
          break;
        default:
      }

      this.chartSet.data.labels.push(label);
    },
    setIndivChartDataSet(i) {
      if (typeof this.requestParam.chartDataType !== "object") {
        this.requestParam.chartDataType = [this.requestParam.chartDataType];
      }

      if (this.requestParam.chartDataType.includes("QUANTITY")) {
        this.indivChartDataSet.totalQuantity.data.push(
          this.responseChartData[i].data
        );
      }

      let contranctedCycleTime = 0;

      if (this.responseChartData[i].contractedCycleTime > 0) {
        contranctedCycleTime = this.responseChartData[i].contractedCycleTime;
        if (!isNaN(contranctedCycleTime) && contranctedCycleTime % 1 !== 0)
          contranctedCycleTime = Math.round(contranctedCycleTime * 100) / 100;
      }

      if (this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")) {
        this.indivChartDataSet.approvedCycleTime.data.push(
          contranctedCycleTime
        );
      }
      if (this.requestParam.chartDataType.includes("CYCLE_TIME")) {
        this.indivChartDataSet.maxCycleTime.data.push(
          this.responseChartData[i].maxCycleTime
        );
        this.indivChartDataSet.minCycleTime.data.push(
          this.responseChartData[i].minCycleTime
        );
        this.indivChartDataSet.cycleTimeWithin.data.push(
          this.responseChartData[i].cycleTimeWithin
        );
        this.indivChartDataSet.cycleTimeL1.data.push(
          this.responseChartData[i].cycleTimeL1
        );
        this.indivChartDataSet.cycleTimeL2.data.push(
          this.responseChartData[i].cycleTimeL2
        );
      }
      if (this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY")) {
        this.indivChartDataSet.maximumCapacity.data.push(
          this.responseChartData[i].maxCapacity
        );
      }
    },
    toPascalCase(param) {
      let lowerParam = param.toLowerCase();
      let result = lowerParam.replace(/^./, lowerParam[0].toUpperCase());
      return result;
    },
  },
};
</script>

<style scoped>
.chart-wrapper {
  width: 100vw;
  height: 60vw;
}
#chartjs-tooltip {
  border-radius: 6px;
  color: #ffffff;
}
#chartjs-tooltip::before {
  content: "";
  position: absolute;
  left: 25%;
  top: -10px;
  display: block;
  width: 0;
  height: 0;
  border-left: 5px solid rgba(0, 0, 0, 0);
  border-right: 5px solid rgba(0, 0, 0, 0);
  border-bottom: 10px solid rgba(51, 51, 51, 1);
  transform: translateX(-50%);
}
.chart-legend {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  padding-top: 5px;
}

.chart-legend .legend-item,
.chart-legend .legend-frequently-item {
  margin-right: 5px;
  display: flex;
  align-items: center;
}

.legend-item .legend-icon,
.legend-frequently-item .legend-icon {
  width: 35px;
}

.legend-icon.bar {
  height: 12px;
  border-width: 2px;
  border-style: solid;
}

.legend-item .label,
.legend-frequently-item .label {
  color: #666666;
  font-size: 13px;
  cursor: default;
}

.clear {
  clear: both;
}

.analysis-hint {
  position: absolute;
  right: -5px;
  bottom: -5px;
  font-size: 13px;
}

.legend-item.removed,
.legend-frequently-item.removed {
  text-decoration: line-through;
}
</style>
