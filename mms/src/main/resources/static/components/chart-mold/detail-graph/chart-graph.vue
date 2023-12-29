<template>
  <div :style="chartWrapStyle">
    <div v-show="switchTab === 'switch-graph'" style="position: relative">
      <div
        class="frequently-chart-container-wrapper"
        style="
          position: absolute;
          left: 0px;
          width: 100%;
          background: rgb(255, 255, 255);
          z-index: 1;
          background: #fff;
          padding-bottom: 15px;
        "
      >
        <div class="frequently-chart-container">
          <div
            style="width: 100%"
            class="op-chart-wrapper"
            :style="{ height: height }"
          >
            <div class="op-chart-content" :style="{ height: height }">
              <canvas class="chart" id="chart-frequently"></canvas>
              <div id="legend-chart-frequently"></div>
            </div>
          </div>
        </div>

        <div class="col-auto ml-auto text-right" style="position: relative">
          <button
            type="button"
            class="btn btn-default op-close-frequently"
            style="position: absolute; right: 15px; bottom: -2px"
            @click="hideFrequentlyChart"
          >
            Back
          </button>
        </div>
      </div>

      <!-- temp chart.js & amchart5 wrapper -->
      <div>
        <!-- 기존 차트 컨테이너 -->
        <div
          class="next-chart-container"
          :class="{ 'hourly-custom-legend': isTemBar }"
          style="position: relative"
          v-show="chartType !== 'ACCELERATION'"
        >
          <div
            v-show="!isHourlyChart"
            style="width: 100%"
            class="op-chart-wrapper"
            :style="{ height: height }"
          >
            <div class="op-chart-content" :style="{ height: height }">
              <canvas class="chart" id="chart-mold"></canvas>
              <div id="chart-tooltips"></div>
            </div>
            <div v-if="!hideLegend" id="legend-chart-mold"></div>
          </div>

          <div
            v-show="isHourlyChart"
            id="hourly-chart-wrapper"
            style="width: 100%; margin-top: 30px"
            :style="{ height: height }"
          >
            <div class="chart-wrapper-inner" :style="{ height: height }">
              <canvas
                class="other-chart"
                :id="'hourly-chart' + mold.id"
              ></canvas>
              <div id="chart-hint"></div>
              <div id="circle-point">
                <div id="shot-count-wrapper">
                  <div id="shot-count-content">
                    <div id="count-header"></div>
                    <div id="count-content">
                      <div id="count-content-left">
                        <div id="square-hint"></div>
                      </div>
                      <div id="count-content-right">
                        <div id="shot-count"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div
            class="analysis-hint"
            v-if="
              requestParam.dateViewType === 'HOUR' &&
              (chartType === 'CYCLE_TIME_ANALYSIS' ||
                chartType === 'TEMPERATURE_ANALYSIS')
            "
          >
            *Click on the data point to view details
          </div>

          <span
            v-if="noDataAvailable"
            style="
              position: absolute;
              left: 50%;
              top: calc(50% - 40px);
              transform: translate(-50%, -50%);
            "
            >No Data Available</span
          >
        </div>

        <!-- new chart container -->
        <bubble-chart
          v-if="chartType === 'ACCELERATION'"
          :chart-data="amchartData"
          x-axis-name="Elapsed time"
          y-axis-name="Acceleration"
          more-series-data-name="Occurred time"
          :key="bubbleChartComponentKey"
        ></bubble-chart>
      </div>

      <div
        v-if="
          isShowNoteWeek &&
          requestParam.dateViewType == 'WEEK' &&
          chartType == 'UPTIME'
        "
        style="text-align: right; font-size: 13px"
        v-text="resources['weekly_uptime_target_msg']"
      ></div>
      <div
        v-if="
          isShowNoteMonth &&
          requestParam.dateViewType == 'MONTH' &&
          chartType == 'UPTIME'
        "
        style="text-align: right; font-size: 13px"
        v-text="resources['monthly_uptime_target_msg']"
      ></div>
      <div
        id="op-daily-details"
        style="
          display: none;
          position: absolute;
          left: 0;
          top: 0;
          width: 100%;
          height: 100%;
          padding: 20px;
          background: #fff;
          z-index: 1;
        "
      >
        <h5>
          {{ mold.equipmentCode }} ({{ requestParam.detailDate }},
          {{ requestParam.year }})
        </h5>
        <table
          class="table table-responsive-sm table-striped"
          style="border-top: 1px solid #c8ced3"
        >
          <thead>
            <tr>
              <th v-text="resources['start_time']"></th>
              <th v-text="resources['end_time']"></th>
              <th v-text="resources['shots']"></th>
              <th v-text="resources['no_of_cavity']"></th>
              <th v-text="resources['cycle_time'] + '(sec)'"></th>
              <th v-text="resources['uptime'] + '(min)'"></th>
              <th
                v-if="isShowTemperatureColumn"
                v-text="resources['uptime'] + '(Â°C)'"
              ></th>
            </tr>
          </thead>
          <tbody class="op-list" style="display: none">
            <tr
              v-for="(result, index, id) in results"
              :id="result.id"
              :key="id"
            >
              <td>{{ result.firstShotDateTime }}</td>
              <td>{{ result.lastShotDateTime }}</td>
              <td>{{ result.shotCount }}</td>
              <td>{{ result.cavity }}</td>
              <td>{{ result.cycleTimeSeconds }}</td>
              <td>{{ result.uptimeMinutes }}</td>
              <td v-if="isShowTemperatureColumn">
                {{ result.temperature / 10 }}
              </td>
            </tr>
          </tbody>
        </table>
        <div
          class="no-results"
          :class="{ 'd-none': total > 0 }"
          v-text="resources['no_results']"
        ></div>

        <div style="flex-wrap: nowrap" class="row">
          <div class="col-md-8">
            <ul class="pagination">
              <li
                v-for="(data, index) in pagination"
                :key="index"
                class="page-item"
                :class="{ active: data.isActive }"
              >
                <a class="page-link" @click="paging(data.pageNumber)">{{
                  data.text
                }}</a>
              </li>
            </ul>
          </div>
          <div class="col-auto ml-auto">
            <button
              type="button"
              class="btn btn-success op-close-details"
              v-text="resources['ok']"
            ></button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="isTemBar"
      id="legend-chart-temp"
      style="
        position: absolute;
        width: 100%;
        left: 0;
        padding: 10px 0 25px;
        background-color: transparent;
        height: 50px;
      "
    ></div>
  </div>
</template>

<script>
/**
 * @param {Number|String} val
 * @param {Number} digitNumber
 * @returns {String}
 */
const formatToDecimal = (val, digitNumber = 0) => {
  return Number(val).toFixed(digitNumber);
};

/**
 * @param {Number|String} val
 * @returns {String}
 */
const appendThousandSeperator = (val, separator = ",") => {
  return val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, separator);
};

const generateTooltipHTML = ({
  xLabel,
  iconCode,
  title,
  dataValue,
  avgCavityTitle,
  totalPartProducedTitle = undefined,
  resetLabel,
}) => `
 <div class="tooltips-container">
        <div style="font-weight: bold;">${xLabel}</div>
        <div class="tooltips-title">
            ${iconCode} ${title} ${dataValue}
        </div>
        <div style="margin-left: 16px;">${avgCavityTitle}</div>
        ${
          totalPartProducedTitle
            ? `<div style="margin-left: 16px;">${totalPartProducedTitle}</div>`
            : ""
        }
        ${resetLabel ? `<div>${resetLabel}</div>` : ""}
    </div>
    <table class="table"> </table>
  </div>
`;

const generateResetTooltipHTML = (resinData) => {
  let tableContent = "";
  resinData.forEach((element) => {
    const time = Common.convertTimestampToDate(element.time);
    const displayTime = moment(time).format("YYYY-MM-DD hh:mm:ss");
    tableContent += `
        <div class="reset-tooltip__content" style="width: 400px;">
          <div>
            ${displayTime}
          </div>
          <div class="d-flex justify-space-between">
            <div class="reset-tooltip__sub-content">
              <div style="font-weight: bold;">Material ${
                element.oldResinCode
              } (old) WACT</div>
              <div class="text-left">${
                element.oldWACT > 1
                  ? element.oldWACT.toFixed(1) + " seconds"
                  : element.oldWACT.toFixed(1) + " second"
              } </div>
            </div>
            <div class="reset-tooltip__sub-content">
              <div style="font-weight: bold;">Material ${
                element.newResinCode
              } (new) WACT</div>
              <div class="text-right">${
                element.newWACT > 1
                  ? element.newWACT.toFixed(1) + " seconds"
                  : element.newWACT.toFixed(1) + " second"
              } </div>
            </div>
          </div>
        </div>
    `;
  });

  const result = `
      <div class="resin-tooltips-container">
        ${tableContent}
      </div>
      `;
  return result;
};

module.exports = {
  props: {
    resources: Object,
    showFilePreviewer: Function,
    chartWrapStyle: "string",
    isMobile: {
      type: Boolean,
      default: false,
    },
    userType: String,
    currentPage: Number,
    detailCurrentPage: Number,
    height: {
      type: [String, Number],
      default: undefined,
    },
    hideLegend: {
      type: Boolean,
      default: false,
    },
    hideBackdrop: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    "bubble-chart": httpVueLoader("/components/amchart5/BubbleChart.vue"),
  },
  data() {
    return {
      accelerationList: [],
      startDate: moment(),
      bubbleChartComponentKey: 0,
      amchartData: {},
      graphSelectedOptions: ["QUANTITY"],
      shotSelected: [],
      shotDefaulted: [],
      cycleTimeSelected: [],
      cycleTimeDefaulted: [],
      UptimeSelected: [],
      UptimeDefaulted: [],
      datePickerType: "DAILY",
      currentDateData: null,
      chartType: "QUANTITY",
      mold: {},
      optimalCycleTime: "Approved CT",
      parts: [],
      isShowDateWeekMonth: true,
      isShowNoteWeek: false,
      isShowNoteMonth: false,
      part: {},
      requestParam: {
        year: moment().format("YYYY"),
        month: moment().format("MM"),
        chartDataType: "QUANTITY",
        dateViewType: "DAY",
        moldId: "",

        // details
        detailDate: "",
        sort: "id,asc",
        size: "50",
      },
      yearSelected: moment().format("YYYY"),
      // details
      results: [],
      // isAbbGermany: false,
      total: 0,
      pagination: [],
      month: 1,
      day: 1,
      hour: "00",
      responseDataChart: null,
      rangeYear: [],
      rangeMonth: [],
      rangeDay: [],
      hourlyChartManager: null,

      //for hour
      monthHour: "01",
      monthSelected: "01",
      isShowFrequentlyChart: false,
      frequentlyChartData: [],
      frequentlyChartDataIndex: 0,
      analysisChartData: [],
      xAxisTitleMapping: {
        HOUR: "Hour",
        DAY: "Date",
        WEEK: "Week",
        MONTH: "Month",
      },
      yAxisTitleMapping: {
        QUANTITY: "Shot",
        CYCLE_TIME: "Cycle Time",
        UPTIME: "Uptime",
        ACCELERATION: "Acceleration",
      },
      // for detail
      xAxisDetailTitleMapping: {
        CYCLE_TIME_ANALYSIS: "Cycle Time",
        TEMPERATURE_ANALYSIS: "Time",
      },
      yAxisDetailTitleMapping: {
        CYCLE_TIME_ANALYSIS: "Quantity",
        TEMPERATURE_ANALYSIS: "Temperature",
      },
      isShowTemperatureColumn: false,
      switchTab: "switch-graph",
      partChangeData: "",
      loadingPartChange: true,
      numberOfPart: 0,
      singlePartName: "",
      hourlyDate: 0,
      isShowGraphDropDown: true,
      isShowChart: false,
      minDate: null,
      maxDate: null,
      isSelectedCustomRange: false,
      chartTypeConfig: [],
      chartTemperature: {},
      shotImg: "",
      cycleTimeImg: "",
      uptimeImg: "",
      customRangeData: {
        allData: [],
        page: 0,
      },
      noDataAvailable: false,
      chart: null,
      frequentlyChart: null,
    };
  },
  methods: {
    getAcceleration() {
      console.log("startDate: ", this.startDate);

      let endDateTimeStamp = this.startDate.unix() + 3600;

      let startDate = Number(this.startDate.format("YYYYMMDDHHmmss"));
      let endDate = Number(
        moment.unix(endDateTimeStamp).format("YYYYMMDDHHmmss")
      );

      console.log("startDate: ", startDate);
      console.log("endDate: ", endDate);
      console.log("startDate: ", Math.floor(startDate / 10000) * 10000);
      console.log("endDate: ", Math.floor(endDate / 10000) * 10000);
      console.log("mold id: ", this.mold.id);

      // todo: get date
      let moldId = `moldId=${this.mold.id}`;
      let fromDateStr = `&fromDateStr=${Math.floor(startDate / 10000) * 10000}`;
      let toDateStr = `&toDateStr=${Math.floor(endDate / 10000) * 10000}`;
      let param = moldId + fromDateStr + toDateStr;

      axios
        .get("/api/analysis/trd-sen/acceleration-chart?" + param)
        .then((res) => {
          this.amchartData = res.data;
          console.log("axios complete amchartData: ", this.amchartData);
          this.bubbleChartComponentKey += 1;
        });
    },
    convertChartType() {
      let type = null;
      switch (this.chartType) {
        case "QUANTITY":
          type = "SHOT";
          break;
        case "PART_QUANTITY":
          type = "PART_QUANTITY";
          break;
        case "CYCLE_TIME":
          type = "CYCLE_TIME";
          break;
        case "CYCLE_TIME_ANALYSIS":
          type = "CYCLE_TIME";
          break;
        case "UPTIME":
          type = "UPTIME";
          break;
        case "TEMPERATURE_ANALYSIS":
          type = "TEMPERATURE";
          break;
      }
      return type;
    },
    async loadChartTypeConfig(chartTypeConfig) {
      this.chartTypeConfig = chartTypeConfig;
      this.accelerationList = this.chartTypeConfig.filter(
        (item) => item.chartDataType === "ACCELERATION"
      );
    },
    // toPascalCase 함수 추가 (DAY => Day 차트 표시용)
    toPascalCase(param) {
      // lowercase 처리
      let lowerParam = param.toLowerCase();
      // 첫번째 글자만 대문자
      let result = lowerParam.replace(/^./, lowerParam[0].toUpperCase());
      return result;
    },
    displayMonths() {
      let months = [];
      const { yearSelected, rangeYear, rangeMonth } = this;

      /*
            for (let i = 1; i <= 12; i++) {
              months.push(i < 10 ? `0${i}` : i);
            }
      */
      if (rangeYear[0] !== rangeYear[1]) {
        if (Number(yearSelected) === Number(rangeYear[1])) {
          for (let i = 1; i <= rangeMonth[1]; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        } else if (Number(yearSelected) === Number(rangeYear[0])) {
          for (let i = rangeMonth[0]; i <= 12; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        } else {
          for (let i = 1; i <= 12; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        }
      } else if (rangeYear.length > 0) {
        for (let i = rangeMonth[0]; i <= rangeMonth[1]; i++) {
          months.push(i < 10 ? `0${i}` : i);
        }
      }
      return months;
    },
    updateChartMoldLegend() {
      $(this.getRootToCurrentId() + "#legend-chart-mold").html(
        this.chart.generateLegend()
      );
    },
    updateChartTempLegend() {
      $(this.getRootToCurrentId() + "#legend-chart-temp").html(
        this.chartTemperature.generateLegend()
      );
    },
    updateChartFrequentlyLegend() {
      let self = this;
      let frequentlyHtmlItem = self.frequentlyChart
        .generateLegend()
        .replace(/legend-item/g, "legend-frequently-item");
      $(this.getRootToCurrentId() + "#legend-chart-frequently").html(
        frequentlyHtmlItem
      );

      // set click frequently legend event
      for (let i = 0; i < self.frequentlyChart.data.datasets.length; i++) {
        $(this.getRootToCurrentId() + "#legend-frequently-item-" + i).on(
          "click",
          () => {
            let meta = self.frequentlyChart.getDatasetMeta(i);
            if (meta.hidden) {
              // show if is hiding
              meta.hidden = null;
              $(this).removeClass("removed");
            } else {
              // hide if is not hiding
              meta.hidden = true;
              $(this).addClass("removed");
            }
            self.frequentlyChart.update();
          }
        );
      }
    },
    hideHourlyChart() {
      $(this.getRootToCurrentId() + "#hourly-chart-wrapper").hide();
    },
    resetHourlyChart() {
      // reset analysis chart data
      if (this.chartType === "CYCLE_TIME_ANALYSIS") {
        this.bindCycleTimeAnalysisChartData([]);
      } else {
        this.bindTemperatureBarChart([]);
        this.bindTemperatureAnalysisChartData([]);
      }
    },
    async initGraphData(optimalCycleTime, chartTypeConfig) {
      CommonChart.initGraphOptionsDefault(optimalCycleTime, chartTypeConfig);
      this.loadChartTypeConfig(chartTypeConfig);
    },
    loadGraphSetting(chartType, graphSettingDataList) {
      let graphOptionsDefaultSelected = JSON.parse(
        JSON.stringify(GraphOptionsDefault)
      );

      for (let i = 0; i < graphSettingDataList.length; i++) {
        let g = graphSettingDataList[i];
        let findIdx = (element) => element.code === g.graphItemType;
        if (graphOptionsDefaultSelected[g.chartDataType]) {
          graphOptionsDefaultSelected[g.chartDataType][
            graphOptionsDefaultSelected[g.chartDataType].findIndex(findIdx)
          ].selected = g.selected;
        }
      }
      let settings = {
        type: chartType,
        data: graphOptionsDefaultSelected[
          CommonChart.convertChartType(chartType)
        ]?.filter((item) => item.selected),
      };
      if (["QUANTITY", "CYCLE_TIME", "UPTIME"].includes(chartType)) {
        // this.filterQueryFunc(settings);
        this.graphSelectedOptions = settings.data.map((g) => g.code);

        const chartDataOptions = [];
        if (this.graphSelectedOptions.includes("QUANTITY")) {
          chartDataOptions.push("QUANTITY");
        }
        if (
          this.graphSelectedOptions.includes("CYCLE_TIME") ||
          this.graphSelectedOptions.includes("MAX_CYCLE_TIME") ||
          this.graphSelectedOptions.includes("MIN_CYCLE_TIME")
        ) {
          chartDataOptions.push("CYCLE_TIME");
        }
        if (
          this.graphSelectedOptions.includes("UPTIME") ||
          this.graphSelectedOptions.includes("UPTIME_TARGET")
        ) {
          chartDataOptions.push("UPTIME");
        }
        this.requestParam.moldId = this.mold.id;
        this.requestParam.chartDataType = chartDataOptions;
      }
    },
    loadChartGraph(
      mold,
      requestParam,
      chartType,
      oct,
      graphSettingDataList,
      startDateAcceleraion
    ) {
      let self = this;
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      if (this.chartType != chartType) {
        $(this.getRootToCurrentId() + "#hourly-chart-wrapper").hide();
      }
      this.isShowFrequentlyChart = false;
      config.data.labels = [];
      config.data.datasets = [];
      this.chart.update();

      this.mold = mold;
      console.log("loadChartGraph", requestParam, chartType);
      this.chartType = chartType ? chartType : "QUANTITY";
      this.isSelectedCustomRange = requestParam.isSelectedCustomRange;
      this.requestParam = requestParam;
      if (oct) this.optimalCycleTime = oct;
      console.log("requestParam 0", this.requestParam);
      this.loadGraphSetting(this.chartType, graphSettingDataList);
      console.log("requestParam 1", this.requestParam);
      const param = this.getParam(this.requestParam);
      console.log("requestParam 2", param);
      if (
        "CYCLE_TIME_ANALYSIS" === this.chartType ||
        "TEMPERATURE_ANALYSIS" === this.chartType
      ) {
        this.resetHourlyChart();
      } else {
        this.hideHourlyChart();
      }

      if (this.chartType === "ACCELERATION") {
        this.startDate = startDateAcceleraion;
        this.getAcceleration();
      } else {
        config.options.scales.yAxes[0].ticks.beginAtZero = true;
        self.chart.update();
        axios
          .get(`/api/chart/molds?` + param)
          .then((response) => {
            this.responseDataChart = response.data;
            if (this.chartType === "CYCLE_TIME_ANALYSIS") {
              $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
              if (
                this.chartTemperature != null &&
                this.chartTemperature.options
              ) {
                try {
                  this.chartTemperature.destroy();
                } catch (e) {
                  console.log(e);
                }
              }
              this.bindCycleTimeAnalysisChartData(response.data);
              this.analysisChartData = response.data;
            } else if (this.chartType === "TEMPERATURE_ANALYSIS") {
              $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
              if (this.isTemBar) {
                this.bindTemperatureBarChart(response.data);
                this.analysisChartData = response.data;
                this.updateChartTempLegend();
              } else {
                this.bindTemperatureAnalysisChartData(response.data);
                this.analysisChartData = response.data;
              }
            } else {
              let dataShow = this.getDataShow(response.data, 0);
              this.bindAllChartData(dataShow);
              this.updateChartMoldLegend();
            }
            self.chart.config.data.datasets = config.data.datasets;
            self.chart.config.data.labels = config.data.labels;
            self.chart.resize();
            self.chart.update();
          })
          .catch((e) => console.log(e));
      }
    },
    setDayWhenChangeMonth(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
        }
      }
    },
    isValidMonth() {
      return (
        this.monthHour != null &&
        this.displayMonths()
          .map((m) => Number(m))
          .includes(Number(this.monthHour))
      );
    },
    resetMonth() {
      let months = this.displayMonths();
      if (months.length > 0) {
        this.monthHour = months[0];
      }
    },
    getDataShow(allData, pageIndex) {
      if (this.isSelectedCustomRange) {
        this.customRangeData.allData = allData;
        this.customRangeData.page = pageIndex;
        let indexStart = Common.MAX_VIEW_POINT * pageIndex;
        let indexEnd = Math.min(
          Common.MAX_VIEW_POINT * pageIndex + Common.MAX_VIEW_POINT,
          allData.length
        );
        let totalPage = Math.ceil(
          this.customRangeData.allData.length / Common.MAX_VIEW_POINT
        );
        this.$emit("graph-page", this.customRangeData.page, totalPage);
        if (allData.length) {
          return allData.slice(indexStart, indexEnd);
        }
      } else {
        this.customRangeData = {
          allData: [],
          page: 0,
        };
      }
      this.$emit("graph-page", this.customRangeData.page, 0);

      return allData;
    },
    bindAllChartData(dataShow) {
      console.log("Run there!", dataShow);
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      config.data.labels = [];
      config.data.datasets = [];
      // this.chart.update();
      if (this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }

      this.isShowFrequentlyChart = false;
      this.frequentlyChartData = [];
      this.setDayWhenChangeMonth(this.monthHour);

      if (
        "CYCLE_TIME_ANALYSIS" === this.chartType ||
        "TEMPERATURE_ANALYSIS" === this.chartType
      ) {
        $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
        this.resetHourlyChart();
      } else {
        this.hideHourlyChart();
      }

      let shotQuantity = {
        type: "line",
        label: "Shot",
        backgroundColor: "#B1E0EE",
        fill: false,
        borderColor: "#63C2DE",
        pointRadius: 2,
        borderWidth: 2,
        data: [],
        yAxisID: "first-y-axis",
        stack: "shotQuantity",
      };

      let approvedCycleTime = {
        type: "line",
        label: this.optimalCycleTime + " (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--success"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "approvedCycleTime",
      };

      const maxCycleTime = {
        type: "line",
        label: "Max CT (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--warning"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "maxCycleTime",
      };

      const minCycleTime = {
        type: "line",
        label: "Min CT (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--danger"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "minCycleTime",
      };

      let cycleTimeWithin = {
        type: "line",
        label: "Within (sec)",
        fill: false,
        backgroundColor: "#B1E0EE",
        borderColor: "#63C2DE",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      let cycleTimeL1 = {
        type: "line",
        label: "L1 (sec)",
        fill: false,
        backgroundColor: "#FFE083",
        borderColor: "#FFC107",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      let cycleTimeL2 = {
        type: "line",
        label: "L2 (sec)",
        fill: false,
        backgroundColor: "#FBB5B5",
        borderColor: "#F86C6B",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      const uptimeTarget = {
        type: "line",
        fill: false,
        label:
          "MONTH" === this.requestParam.dateViewType ||
          "WEEK" === this.requestParam.dateViewType
            ? "Uptime Limit (hour)"
            : "Uptime Target (" + this.mold.uptimeTarget + "%)",
        backgroundColor: hexToRgba(getStyle("--success"), 0),
        borderColor: getStyle("--success"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptimeTarget",
      };

      let uptimeWithin = {
        type: "line",
        label: "Within",
        fill: false,
        backgroundColor: "#B1E0EE",
        borderColor: "#63C2DE",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };
      let uptimeL1 = {
        type: "line",
        label: "L1",
        fill: false,
        backgroundColor: "#FFE083",
        borderColor: "#FFC107",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };
      let uptimeL2 = {
        type: "line",
        label: "L2",
        fill: false,
        backgroundColor: "#FBB5B5",
        borderColor: "#F86C6B",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };
      if (
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
      ) {
        shotQuantity.type = "line";
        shotQuantity.order = 1;
      } else {
        shotQuantity.type = "bar";
        shotQuantity.order = 0;
      }
      if (
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType === "LINE"
      ) {
        cycleTimeWithin.type = "line";
        cycleTimeL1.type = "line";
        cycleTimeL2.type = "line";
      } else {
        cycleTimeWithin.type = "bar";
        cycleTimeL1.type = "bar";
        cycleTimeL2.type = "bar";
      }
      if (
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "UPTIME"
        )[0].chartType === "LINE"
      ) {
        uptimeWithin.type = "line";
        uptimeL1.type = "line";
        uptimeL2.type = "line";
      } else {
        uptimeWithin.type = "bar";
        uptimeL1.type = "bar";
        uptimeL2.type = "bar";
      }

      let contranctedCycleTime = 0;

      const data = dataShow;

      for (let i = 0; i < data.length; i++) {
        let title = data[i].title.replace(this.requestParam.year, "W-");

        if (this.isSelectedCustomRange) {
          if ("DAY" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(6);
          } else if ("MONTH" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(4);
          } else if ("HOUR" == this.requestParam.dateViewType) {
            title = moment(data[i].title, "YYYYMMDDHH").format("HH");
          } else if ("WEEK" == this.requestParam.dateViewType) {
            let year = data[i].title.substring(0, 4);
            let week = data[i].title.replace(year, "");
            title = `W${week}, ${year}`;
            console.log(`check week data - ${i}`, data[i].title);
          }
        } else {
          if ("DAY" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(2, 4);
          } else if ("MONTH" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(0, 2);
          } else if ("HOUR" == this.requestParam.dateViewType) {
            title = moment(data[i].title, "YYYYMMDDHH").format("HH");
          } else if ("WEEK" == this.requestParam.dateViewType) {
            let year = data[i].title.substring(0, 4);
            let week = data[i].title.replace(year, "");
            title = `W${week}, ${year}`;
            console.log(`check week data - ${i}`, data[i].title);
          }
        }

        if (data[i].contractedCycleTime > 0 && contranctedCycleTime === 0) {
          contranctedCycleTime = data[i].contractedCycleTime;
          if (
            contranctedCycleTime &&
            !isNaN(contranctedCycleTime) &&
            contranctedCycleTime % 1 !== 0
          )
            contranctedCycleTime = Math.round(contranctedCycleTime * 100) / 100;
        }

        config.data.labels.push(title);
        if (this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")) {
          approvedCycleTime.data.push(contranctedCycleTime);
        }
        if (this.graphSelectedOptions.includes("MAX_CYCLE_TIME")) {
          maxCycleTime.data.push(data[i].maxCycleTime);
        }
        if (this.graphSelectedOptions.includes("MIN_CYCLE_TIME")) {
          minCycleTime.data.push(data[i].minCycleTime);
        }
        if (this.graphSelectedOptions.includes("CYCLE_TIME")) {
          cycleTimeL1.data.push(data[i].cycleTimeL1);
          cycleTimeL2.data.push(data[i].cycleTimeL2);
          cycleTimeWithin.data.push(data[i].cycleTimeWithin);
        }
        if (this.graphSelectedOptions.includes("QUANTITY")) {
          shotQuantity.data.push(data[i].data);
        }

        if (
          this.graphSelectedOptions.includes("UPTIME") ||
          this.graphSelectedOptions.includes("UPTIME_TARGET")
        ) {
          const ratioL1 = this.mold.uptimeLimitL1;
          const ratioL2 = this.mold.uptimeLimitL2;

          let displayTime = "HOUR";
          let uptime = data[i].uptime;
          let basicTime = 86400;

          if ("HOUR" === displayTime) {
            uptime = data[i].uptimeHour;
            basicTime = 24;
          } else if ("MINUTE" === displayTime) {
            uptime = data[i].uptimeMinute;
            basicTime = 1440;
          }

          if ("WEEK" === this.requestParam.dateViewType) {
            basicTime = basicTime * 7;
          } else if ("MONTH" === this.requestParam.dateViewType) {
            basicTime = basicTime * 30;
          }

          let utarget = basicTime * this.mold.uptimeTarget * 0.01;
          let hourPerShift = 24;
          let shiftsPerDay = 1;
          let productionDays = 7;
          let productionWeeks = 1;

          if (this.mold.hourPerShift && !isNaN(this.mold.hourPerShift)) {
            hourPerShift = this.mold.hourPerShift;
          } else {
            this.isShowNoteWeek = true;
            this.isShowNoteMonth = true;
          }
          if (this.mold.shiftsPerDay && !isNaN(this.mold.shiftsPerDay)) {
            shiftsPerDay = this.mold.shiftsPerDay;
          } else {
            this.isShowNoteWeek = true;
            this.isShowNoteMonth = true;
          }
          if (this.mold.productionDays && !isNaN(this.mold.productionDays)) {
            if (this.mold.productionDays > 7) {
              productionDays = 7;
              this.isShowNoteWeek = true;
            } else {
              productionDays = this.mold.productionDays;
            }
          } else {
            this.isShowNoteWeek = true;
          }

          if ("DAY" === this.requestParam.dateViewType) {
            if (shiftsPerDay) {
              utarget = shiftsPerDay * this.mold.uptimeTarget * 0.01;
            }
            // !!!: alertuptimeGoal = hourPerShift * shiftsPerDay;
          } else if ("WEEK" === this.requestParam.dateViewType) {
            if (hourPerShift * shiftsPerDay > 24) {
              this.isShowNoteWeek = true;
              utarget = 24 * productionDays * this.mold.uptimeTarget * 0.01;
            } else {
              utarget =
                hourPerShift *
                shiftsPerDay *
                productionDays *
                this.mold.uptimeTarget *
                0.01;
            }
          } else if ("MONTH" === this.requestParam.dateViewType) {
            if (hourPerShift * shiftsPerDay > 24) {
              this.isShowNoteMonth = true;
              utarget = 24 * 30 * this.mold.uptimeTarget * 0.01;
            } else {
              utarget =
                hourPerShift *
                shiftsPerDay *
                30 *
                this.mold.uptimeTarget *
                0.01;
            }
          } else if ("HOUR" == this.requestParam.dateViewType) {
            utarget = 1 * this.mold.uptimeTarget * 0.01;
          }
          let uptime1 = utarget * (ratioL1 / 100);
          let uptime2 = utarget * (ratioL2 / 100);

          let minusL2 = utarget - uptime2;
          let minusL1 = utarget - uptime1;
          let plusL1 = utarget + uptime1;
          let plusL2 = utarget + uptime2;
          let w = 0;
          let l1 = 0;
          let l2 = 0;
          if (uptime < minusL2 || uptime > plusL2) {
            l2 = uptime;
          } else if (
            (minusL2 <= uptime && uptime < minusL1) ||
            (uptime > plusL1 && uptime <= plusL2)
          ) {
            l1 = uptime;
          } else {
            w = uptime;
          }
          if (this.graphSelectedOptions.includes("UPTIME")) {
            uptimeWithin.data.push(w);
            uptimeL1.data.push(l1);
            uptimeL2.data.push(l2);
          }
          if (this.graphSelectedOptions.includes("UPTIME_TARGET")) {
            uptimeTarget.data.push(utarget.toFixed(1));
          }
        }
      }

      if (shotQuantity) {
        let pointConfig = this.getQuantityPointConfig(data);
        shotQuantity.pointStyle = pointConfig.pointStyles;
        shotQuantity.pointRadius = pointConfig.pointRadius;
        shotQuantity.pointHitRadius = pointConfig.pointHoverRadius;
        shotQuantity.pointBackgroundColor = pointConfig.pointBackgroundColor;
        shotQuantity.pointHoverBackgroundColor =
          pointConfig.pointHoverBackgroundColor;
      }
      config.options.tooltips = this.setTooltipForMainChart(
        this.requestParam.dateViewType,
        data
      );

      console.log("this.chartTypeConfig", this.chartTypeConfig);

      if (
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType === "LINE" &&
        this.graphSelectedOptions.includes("CYCLE_TIME")
      ) {
        config.data.datasets.push(cycleTimeWithin);
        config.data.datasets.push(cycleTimeL1);
        config.data.datasets.push(cycleTimeL2);
        if (cycleTimeL2) {
          console.log("lorem");
          let pointConfig = this.getCycleTimePointConfig(data);
          cycleTimeL2.pointStyle = pointConfig.pointStyles;
          cycleTimeL2.pointRadius = pointConfig.pointRadius;
          cycleTimeL2.pointHitRadius = pointConfig.pointHoverRadius;
          cycleTimeL2.pointBackgroundColor = pointConfig.pointBackgroundColor;
          cycleTimeL2.pointHoverBackgroundColor =
            pointConfig.pointHoverBackgroundColor;
        }
      }

      if (this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")) {
        config.data.datasets.push(approvedCycleTime);
      }
      if (this.graphSelectedOptions.includes("MAX_CYCLE_TIME")) {
        config.data.datasets.push(maxCycleTime);
      }
      if (this.graphSelectedOptions.includes("MIN_CYCLE_TIME")) {
        config.data.datasets.push(minCycleTime);
      }
      if (this.graphSelectedOptions.includes("QUANTITY")) {
        config.data.datasets.push(shotQuantity);
      }

      if (
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType !== "LINE" &&
        this.graphSelectedOptions.includes("CYCLE_TIME")
      ) {
        config.data.datasets.push(cycleTimeWithin);
        config.data.datasets.push(cycleTimeL1);
        config.data.datasets.push(cycleTimeL2);
      }

      if (this.graphSelectedOptions.includes("UPTIME")) {
        config.data.datasets.push(uptimeWithin);
        config.data.datasets.push(uptimeL1);
        config.data.datasets.push(uptimeL2);
      }
      if (this.graphSelectedOptions.includes("UPTIME_TARGET")) {
        config.data.datasets.push(uptimeTarget);
      }

      if (!dataShow.length) {
        let defaultData = this.getChartDefaultData();
        config.data.labels = defaultData.labels;
      }
      //this.setGraphAxisTitle();
      this.updateChartMoldLegend();
      switch (this.chartType) {
        case "QUANTITY":
          // for cycle time
          config.options.scales.yAxes[1].position = "right";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = false;
          // for shot
          config.options.scales.yAxes[0].position = "left";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
          config.options.scales.yAxes[0].display = true;
          if (
            !this.graphSelectedOptions.includes("CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("MIN_CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("MAX_CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")
          ) {
            config.options.scales.yAxes[1].scaleLabel.labelString = "";
            config.options.scales.yAxes[1].display = false;
          } else {
            config.options.scales.yAxes[1].scaleLabel.labelString =
              "Cycle Time";
            config.options.scales.yAxes[1].display = true;
          }
          break;
        case "CYCLE_TIME":
          // for shot
          config.options.scales.yAxes[0].position = "right";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = false;
          // for cycle time
          config.options.scales.yAxes[1].scaleLabel.labelString = "Cycle Time";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[1].position = "left";
          config.options.scales.yAxes[1].display = true;
          if (!this.graphSelectedOptions.includes("QUANTITY")) {
            config.options.scales.yAxes[0].scaleLabel.labelString = "";
            config.options.scales.yAxes[0].display = false;
          } else {
            config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
            config.options.scales.yAxes[0].display = true;
          }
          break;
        case "UPTIME":
          // for shot
          config.options.scales.yAxes[0].position = "right";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = false;
          // for uptime
          config.options.scales.yAxes[1].position = "left";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[1].scaleLabel.labelString = "Uptime";
          config.options.scales.yAxes[1].display = true;
          if (!this.graphSelectedOptions.includes("QUANTITY")) {
            config.options.scales.yAxes[0].scaleLabel.labelString = "";
            config.options.scales.yAxes[0].display = false;
          } else {
            config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
            config.options.scales.yAxes[0].display = true;
          }
          break;
      }
      config.options.scales.xAxes[0].scaleLabel.labelString = this.toPascalCase(
        this.requestParam.dateViewType
      );
      console.log("config.data.datasets:::::::::::::::", config.data.datasets);
      // const isDataAvailable = config.data.datasets[0].data.some(item => !!item)
      const isDataAvailable = config.data.datasets.some((set) =>
        set.data.some((item) => !!item)
      );
      config.options.scales.yAxes[0].gridLines.display = isDataAvailable;
      config.options.scales.yAxes[1].gridLines.display = isDataAvailable;
      this.noDataAvailable = !isDataAvailable;
      this.chart.config.data.datasets = config.data.datasets;
      this.chart.config.data.labels = config.data.labels;
      console.log(
        "this.chart.config.data.datasets",
        this.chart.config.data.datasets
      );
      console.log(
        "this.chart.config.data.labels",
        this.chart.config.data.labels
      );

      // TODO: need check timing for proper solution
      setTimeout(() => {
        this.chart.resize();
        this.chart.update();
        this.$forceUpdate();
      }, 2000);
    },
    getParam(requestParam) {
      let paramNew = {};
      switch (requestParam.dateViewType) {
        case "HOUR":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: this.requestParam.chartDataType,

            date: requestParam.date,
            moldId: this.requestParam.moldId,
          };
          break;
        case "DAY":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            month: requestParam.month,
            moldId: requestParam.moldId,
          };
          break;
        case "MONTH":
        case "WEEK":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            moldId: requestParam.moldId,
          };
          break;
      }
      if (
        this.isSelectedCustomRange &&
        requestParam.startDate &&
        requestParam.endDate
      ) {
        paramNew = {
          ...paramNew,
          startDate: requestParam.startDate,
          endDate: requestParam.endDate,
        };
      }
      return Common.param(paramNew);
    },
    bindFrequentlyDataChart(detailDate) {
      this.frequentlyChartData = [];
      let param = {
        moldId: this.requestParam.moldId,
        chartDataType: this.requestParam.chartDataType,
      };
      /*
      let date = `${this.yearSelected}${
        Number(this.monthHour) < 10
          ? `0${Number(this.monthHour)}`
          : this.monthHour
      }${Number(this.day) < 10 ? `0${this.day}` : this.day}`;
*/
      let date = `${this.requestParam.year}${
        Number(this.requestParam.month) < 10
          ? `0${Number(this.requestParam.month)}`
          : this.requestParam.month
      }${
        Number(this.requestParam.day) < 10
          ? `0${Number(this.requestParam.day)}`
          : this.requestParam.day
      }`;

      if (this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")) {
        date = `${date}${
          Number(detailDate) < 10 ? `0${Number(detailDate)}` : detailDate
        }`;
      }
      param.date = date;

      param = Common.param(param);
      axios.get("/api/chart/hour-details?" + param).then((response) => {
        if (!response.data || response.data.length === 0) {
          return;
        }
        this.isShowFrequentlyChart = true;
        if (this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")) {
          this.bindFrequentlyChart(response.data);
        } else if (
          this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
        ) {
          let chartData = [];
          let hashDataByMinute = {};
          response.data.forEach((item) => {
            let splitParams = item.title.split(" ")[1].split(":");
            let minute = splitParams[0] + ":" + splitParams[1];
            hashDataByMinute[minute] = item.data / 10;
          });
          let blockCount = 4; // 1, 2, 3, 4, 6 hour per block, block 1 is 00 - 05h59, 06h00 - 11h59, 12h00 - 17h59, 18h00 - 23h59, ()
          // group data by block, index will be 0, 1, 2, 3
          let hourPerBlock = 24 / blockCount;
          let groupData = [];
          for (let i = 0; i < blockCount; i++) {
            groupData.push([]);
          }

          Object.keys(hashDataByMinute).forEach((minute) => {
            let hour = parseInt(minute.split(":")[0]);
            let indexOfBlock = Math.floor(hour / hourPerBlock);
            groupData[indexOfBlock].push({
              data: hashDataByMinute[minute],
              title: minute,
            });
          });

          let realGroupData = [];
          groupData.forEach((item) => {
            if (item.length > 0) {
              realGroupData.push(item);
            }
          });

          this.frequentlyChartData = realGroupData;
          this.frequentlyChartDataIndex = 0;
          this.bindFrequentlyChart(realGroupData[0] || []);
          this.$emit(
            "load-detail-graph-page",
            this.frequentlyChartDataIndex,
            realGroupData.length
          );
        }
      });
    },
    bindFrequentlyChart(dataShow) {
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).show();
      frequentlyConfig.data.labels = [];
      frequentlyConfig.data.datasets = [];
      this.frequentlyChart.update();

      let newDataset = null;
      if (this.chartType === "CYCLE_TIME_ANALYSIS") {
        newDataset = {
          type: "bar",
          label: "Shot",
          backgroundColor: hexToRgba(getStyle("--info"), 50),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      } else {
        newDataset = {
          type: "line",
          label: "Temperature",
          backgroundColor: hexToRgba(getStyle("--info"), 10),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      }

      const data = dataShow;

      for (let i = 0; i < data.length; i++) {
        let title = data[i].title;
        frequentlyConfig.data.labels.push(title);
        newDataset.data.push(data[i].data);
      }
      frequentlyConfig.options.tooltips = {
        enabled: true,
        intersect: true,
        callbacks: {
          afterLabel(tooltipItem) {
            const { index } = tooltipItem;
            let avgCavities = 0;
            if (data[index]) {
            }

            return ``;
          },
          label(tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel).toString();
            return `${label}: ${value}`;
          },
        },
      };
      frequentlyConfig.data.datasets.push(newDataset);
      //set caption
      frequentlyConfig.options.scales.xAxes[0].scaleLabel.labelString =
        this.xAxisDetailTitleMapping[this.chartType];
      frequentlyConfig.options.scales.yAxes[0].scaleLabel.labelString =
        this.yAxisDetailTitleMapping[this.chartType];
      frequentlyConfig.options.scales.yAxes[0].fontSize = 13;
      frequentlyConfig.options.scales.xAxes[0].fontSize = 13;

      if (frequentlyConfig.options.scales.yAxes[1]) {
        frequentlyConfig.options.scales.yAxes[1].display = false;
      }

      this.frequentlyChart.update();
      this.updateChartFrequentlyLegend();
    },
    hideFrequentlyChart() {
      this.isShowFrequentlyChart = false;
      this.$nextTick(() => {
        if ("CYCLE_TIME_ANALYSIS" === this.chartType) {
          if (this.chartTemperature != null && this.chartTemperature.options) {
            try {
              this.chartTemperature.destroy();
            } catch (e) {
              console.log(e);
            }
          }
          this.bindCycleTimeAnalysisChartData(this.analysisChartData);
        } else if ("TEMPERATURE_ANALYSIS" === this.chartType) {
          if (this.isTemBar) {
            this.bindTemperatureBarChart(this.analysisChartData);
          } else {
            this.bindTemperatureAnalysisChartData(this.analysisChartData);
          }
          // this.bindTemperatureAnalysisChartData(this.analysisChartData);
        }
      });
    },
    getChartDefaultData() {
      let data = [];
      let labels = [];
      if ("HOUR" === this.requestParam.dateViewType) {
        for (let i = 0; i <= 23; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      } else if ("DAY" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 28; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      } else if ("WEEK" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 52; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push("W-" + title);
          data.push(0);
        }
      } else if ("MONTH" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 12; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      }
      return {
        labels: labels,
        data: data,
      };
    },
    setTooltipForMainChart(dateViewType, inputData) {
      const self = this;
      const chartType = this.chartType;
      return {
        intersect: true,
        enabled: false,
        footerFontStyle: "normal",
        position: "nearest",
        custom: function (tooltipModel) {
          // Tooltip Element

          var tooltipEl = document.getElementById("chartjs-tooltip");
          // Create element on first render
          let xLabel = "";
          let yLabel = 0;
          let itemIndex = 0;
          const { dataPoints } = tooltipModel;
          if (dataPoints && dataPoints.length > 0) {
            xLabel = dataPoints[0].xLabel;
            yLabel = dataPoints[0].yLabel;
            itemIndex = dataPoints[0].index;
          }
          let avgCavities = 0;
          let totalPartProduced = 0;
          let dataValue = yLabel;
          let parts = [];

          let type = "";
          let title = "";
          let iconCode = "";

          if (tooltipModel.body) {
            type = tooltipModel.body[0].lines[0];
            const regexp = /([A-Za-z0-9 ()%]+):(.*)/;
            const match = type.match(regexp);
            title = `${match[1]}: `;
            if (type.includes("Shot")) {
              iconCode = `<div style="border: 1px solid #63C2DE; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (
              type.includes("Weighted Average CT") ||
              type.includes("Uptime Target") ||
              type.includes("Approved CT")
            ) {
              iconCode = `<div style="border: 1px solid #4dbd74; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Max CT")) {
              iconCode = `<div style="border: 1px solid #ffc107; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Min CT")) {
              iconCode = `<div style="border: 1px solid #f86c6b; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Within")) {
              iconCode = `<div style="border: 1px solid #63C2DE; background-color: #B1E0EE; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("L1")) {
              iconCode = `<div style="border: 1px solid #FFC107; background-color: #FFE083; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("L2")) {
              iconCode = `<div style="border: 1px solid #F86C6B; background-color: #FBB5B5; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            }
          }

          let resetLabel = null;
          if (inputData[itemIndex]) {
            avgCavities = inputData[itemIndex].avgCavities;
            totalPartProduced = inputData[itemIndex].totalPartProduced;
            let reset = inputData[itemIndex].resetValue;
            let lastShot = inputData[itemIndex].lastShot;
            if (reset != null && reset !== 0) {
              dataValue = inputData[itemIndex].data;
              resetLabel = `On this ${dateViewType.toLowerCase()}, the tooling was reset to ${lastShot} shots`;
            }
            parts = inputData[itemIndex].partData;
          }

          const avgCavityTitle =
            "Avg.Cavities: " +
            appendThousandSeperator(formatToDecimal(avgCavities, 2));
          const totalPartProducedTitle =
            "Total Part Produced: " +
            appendThousandSeperator(totalPartProduced);
          if (title.includes("Shot")) {
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = generateTooltipHTML({
                xLabel,
                iconCode,
                title,
                dataValue,
                avgCavityTitle,
                totalPartProducedTitle,
                resetLabel,
              });
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }
            console.log("case 2");
            tooltipEl.innerHTML = generateTooltipHTML({
              xLabel,
              iconCode,
              title,
              dataValue,
              avgCavityTitle,
              totalPartProducedTitle,
              resetLabel,
            });
            var innerHtml = "<thead>";
            innerHtml += "<tr>";
            ["Part ID", "Shots", "Part Produced"].forEach(function (value) {
              innerHtml += "<th>" + value + "</th>";
            });
            innerHtml += "</tr></thead>";
            parts.forEach((element) => {
              let content = `
                        <tr>
                            <td style="text-align: center;">${
                              element.partCode
                            }</td>
                            <td style="text-align: center;">${appendThousandSeperator(
                              element.shot
                            )}</td>
                            <td style="text-align: center;">${appendThousandSeperator(
                              element.partProduced
                            )}</td>
                        </tr>
                      `;
              innerHtml += content;
            });
            var tableRoot = tooltipEl.querySelector("table");
            tableRoot.innerHTML = innerHtml;
            tooltipEl.style.transform = "translateX(-25%)";
            document.body.appendChild(tooltipEl);
          } else {
            if (
              self.chartTypeConfig.filter(
                (item) => item.chartDataType === "CYCLE_TIME"
              )[0].chartType === "LINE" &&
              chartType === "CYCLE_TIME" &&
              (type.includes("Within") ||
                type.includes("L2") ||
                type.includes("L1")) &&
              inputData[itemIndex] &&
              inputData[itemIndex].resinCodeChangeData
            ) {
              if (tooltipModel.opacity === 0) {
                tooltipEl.style.opacity = 0;
                return;
              }
              const lastPoint = tooltipModel.dataPoints.length - 1;
              if (!tooltipEl) {
                tooltipEl = document.createElement("div");
                tooltipEl.id = "chartjs-tooltip";
                tooltipEl.innerHTML = generateResetTooltipHTML(
                  inputData[itemIndex].resinCodeChangeData
                );
                document.body.appendChild(tooltipEl);
                if (tooltipModel.dataPoints[lastPoint].y >= 300) {
                  tooltipEl.style.transform =
                    "translateX(-25%) translateY(calc(-100% - 50px))";
                } else {
                  tooltipEl.style.transform = "translateX(-25%)";
                }
              } else {
                tooltipEl.remove();
                tooltipEl = document.createElement("div");
                tooltipEl.id = "chartjs-tooltip";
                tooltipEl.innerHTML = generateResetTooltipHTML(
                  inputData[itemIndex].resinCodeChangeData
                );
                document.body.appendChild(tooltipEl);
                if (tooltipModel.dataPoints[lastPoint].y >= 300) {
                  tooltipEl.style.transform =
                    "translateX(-25%) translateY(calc(-100% - 50px))";
                } else {
                  tooltipEl.style.transform = "translateX(-25%)";
                }
              }
            } else {
              if (!tooltipEl) {
                tooltipEl = document.createElement("div");
                tooltipEl.id = "chartjs-tooltip";
                tooltipEl.innerHTML = generateTooltipHTML({
                  xLabel,
                  iconCode,
                  title,
                  dataValue,
                  avgCavityTitle,
                });
                document.body.appendChild(tooltipEl);
              }

              if (tooltipModel.opacity === 0) {
                tooltipEl.style.opacity = 0;
                return;
              }
              tooltipEl.innerHTML = generateTooltipHTML({
                xLabel,
                iconCode,
                title,
                dataValue,
                avgCavityTitle,
              });
              document.body.appendChild(tooltipEl);
              tooltipEl.style.transform = "translateX(-25%)";
            }
          }

          if (
            self.chartTypeConfig.filter(
              (item) => item.chartDataType === "CYCLE_TIME"
            )[0].chartType === "LINE" &&
            chartType === "CYCLE_TIME" &&
            (type.includes("Within") ||
              type.includes("L2") ||
              type.includes("L1")) &&
            inputData[itemIndex] &&
            inputData[itemIndex].resinCodeChangeData
          ) {
            tooltipEl.style["background-color"] = "rgba(255, 255, 255, 1)";
            !tooltipEl.classList.contains("resin") &&
              tooltipEl.classList.add("resin");
          } else {
            tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
            tooltipEl.classList.contains("resin") &&
              tooltipEl.classList.remove("resin");
          }

          var position = self.chart.canvas.getBoundingClientRect();
          tooltipEl.style.opacity = 1;
          tooltipEl.style.position = "absolute";
          tooltipEl.style[`z-index`] = 9999;
          tooltipEl.style.left =
            position.left + window.pageXOffset + +tooltipModel.caretX + "px";
          tooltipEl.style.top =
            position.top +
            window.pageYOffset +
            (tooltipModel.caretY + 20) +
            "px";
          tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
          tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
          tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
          tooltipEl.style.padding =
            tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
          tooltipEl.style.pointerEvents = "none";
        },
      };
    },
    getResetIcon() {
      let iconUrl = "/images/icon/reset-point-icon2.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 21;
      icon.height = 21;
      return icon;
    },
    getIndicatorIcon() {
      let iconUrl = "/images/icon/icon-indicator.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 21;
      icon.height = 21;
      return icon;
    },
    isResetItem(item, i) {
      if (item.resetValue && item.resetValue > 0) {
        return true;
      }
      return false;
    },
    getQuantityPointConfig(data) {
      let pointStyles = [];
      let pointRadius = [];
      let pointHoverRadius = [];
      let pointHoverBackgroundColor = "rgb(255,255,255)";
      let pointBackgroundColor = [];
      for (let i = 0; i < data.length; i++) {
        if (this.isResetItem(data[i], i)) {
          pointStyles.push(this.getResetIcon());
          pointBackgroundColor.push("rgb(65,153,255)");
          let radius = 2;
          pointRadius.push(radius);
          pointHoverRadius.push(radius * 5);
        } else {
          pointStyles.push("circle");
          pointRadius.push(2);
          pointHoverRadius.push(2);
          pointBackgroundColor.push(getStyle("--info"));
        }
      }
      return {
        pointStyles,
        pointRadius,
        pointHoverRadius,
        pointBackgroundColor,
        pointHoverBackgroundColor,
      };
    },
    getCycleTimePointConfig(data) {
      let pointStyles = [];
      let pointRadius = [];
      let pointHoverRadius = [];
      let pointBackgroundColor = [];
      let pointHoverBackgroundColor = "rgb(255,255,255)";
      for (let i = 0; i < data.length; i++) {
        if (this.chartType === "CYCLE_TIME") {
          if (data[i].resinCodeChangeData) {
            pointStyles.push(this.getIndicatorIcon());
            pointBackgroundColor.push("rgb(65,153,255)");
            pointRadius.push(2);
            pointHoverRadius.push(2);
          } else {
            pointStyles.push("circle");
            pointRadius.push(2);
            pointHoverRadius.push(2);
            pointBackgroundColor.push(getStyle("--info"));
          }
        } else {
          pointStyles.push("circle");
          pointRadius.push(2);
          pointHoverRadius.push(2);
          pointBackgroundColor.push(getStyle("--info"));
        }
      }
      return {
        pointStyles,
        pointRadius,
        pointHoverRadius,
        pointBackgroundColor,
        pointHoverBackgroundColor,
      };
    },
    bindCycleTimeAnalysisChartData(data) {
      let cycleAxis = [];
      let chartData = [];

      let countAxis = 24;
      for (let i = 0; i < countAxis; i++) {
        chartData.push([0, 0, 0, 0]);
      }
      if (data.length > 0) {
        const {
          cycleTimeMinusL1,
          cycleTimeMinusL2,
          cycleTimePlusL1,
          cycleTimePlusL2,
        } = data[0];

        cycleAxis = [
          cycleTimeMinusL2,
          cycleTimeMinusL1,
          cycleTimePlusL1,
          cycleTimePlusL2,
        ];

        data.forEach((item) => {
          let llct = item.llct ? item.llct : 0;
          let mfct = item.mfct ? item.mfct : 0;
          let ulct = item.ulct ? item.ulct : 0;
          let itemData = item.data ? item.data : 0;
          const title = item.title;
          const titleLength = title.length;
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            [llct, mfct, ulct, itemData];
        });
      } else {
        // cycleAxis = [0, 0, 0, 0, 0];
      }

      const totalInfo = {
        totalShotL2Bottom: cycleAxis[0],
        totalShotL1Bottom: cycleAxis[1],
        totalShotL1Top: cycleAxis[2],
        totalShotL2Top: cycleAxis[3],
      };
      //Todo
      const colorZone = {
        totalShotL2Bottom: "red",
        totalShotL1Bottom: getStyle("--warning"),
        totalShotL1Top: getStyle("--warning"),
        totalShotL2Top: "red",
      };
      this.cycleTimeHourlyChart = new CycleTimeHourlyChart(
        "hourly-chart" + this.mold.id,
        cycleAxis,
        chartData,
        totalInfo,
        colorZone,
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE",
        this
      );
      this.cycleTimeHourlyChart.draw();
    },
    /*
    bindAccelerationChartData: function (dataShow) {
      config.data.labels = [];
      config.data.datasets = [];
      window.chart.update();

      let newDataset = {
        type: "line",
        label: "Value",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      const data = dataShow.filter((value) => {
        if ("DAY" == this.requestParam.dateViewType) {
          const title = value.title.replace(this.requestParam.year, "W-");
          const day = title.replace("W-", "");
          if (Number(day.substring(0, 2)) == this.month) {
            return true;
          }
          return false;
        } else {
          return true;
        }
      });

      for (let i = 0; i < data.length; i++) {
        config.data.labels.push(data[i].title);
        newDataset.data.push(data[i].value);
      }
      if (!data.length) {
        newDataset.data = [];
      }

      config.options.tooltips = {
        callbacks: {
          afterLabel(tooltipItem) {
            return "";
          },
        },
      };
      config.data.datasets.push(newDataset);
      let xAxisTitle = "Time";
      let yAxisTitle = this.yAxisTitleMapping[this.requestParam.chartDataType];
      config.options.scales.xAxes[0].scaleLabel.labelString = xAxisTitle;
      config.options.scales.yAxes[0].scaleLabel.labelString = yAxisTitle;
      // this.setGraphAxisTitle();
      window.chart.config.data.datasets = config.data.datasets;
      window.chart.config.data.labels = config.data.labels;
      window.chart.update();
    },
*/
    bindTemperatureAnalysisChartData(data) {
      if (this.chartTemperature != null && this.chartTemperature.options) {
        try {
          this.chartTemperature.destroy();
        } catch (e) {
          console.log(e);
        }
      }
      let chartData = [];
      let countAxis = 24;
      for (let i = 0; i < countAxis; i++) {
        chartData.push([0, 0, 0, 0]);
      }
      if (data.length > 0) {
        data.forEach((item) => {
          const tlo = (item.tlo ?? 0) / 10;
          const tav = (item.tav ?? 0) / 10;
          const thi = (item.thi ?? 0) / 10;
          const itemData = item.data ?? 0;
          const title = item.title;
          const titleLength = title.length;
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            [tlo, tav, thi, itemData];
        });
      }
      // chartData = this.getTestData();

      this.temperatureHourlyChart = new TemperatureHourlyChart(
        "hourly-chart" + this.mold.id,
        chartData,
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE",
        this
      );
      this.temperatureHourlyChart.draw();
    },
    bindTemperatureBarChart(data) {
      if (
        this.chartTemperature.canvas != null &&
        this.chartTemperature.options
      ) {
        this.chartTemperature.destroy();
      }
      var labels = [
        "00",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
      ];
      let chartData = [];
      let chartDataTemp = [];
      let countAxis = 24;
      let shotQuantity = {
        type: "line",
        label: "Shot",
        backgroundColor: "#B1E0EE",
        fill: false,
        borderColor: "#63C2DE",
        pointRadius: 2,
        borderWidth: 2,
        data: [],
        yAxisID: "shot",
      };
      for (let i = 0; i < countAxis; i++) {
        chartData.push(0);
        chartDataTemp.push(0);
        shotQuantity.data.push(0);
      }
      if (data.length > 0) {
        data.forEach((item) => {
          const tlo = (item.tlo ?? 0) / 10;
          const tav = (item.tav ?? 0) / 10;
          const thi = (item.thi ?? 0) / 10;
          const title = item.title;
          const titleLength = title.length;
          chartDataTemp[
            parseInt(title[titleLength - 2] + title[titleLength - 1])
          ] = [tlo, tav, thi];
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            tav;
          shotQuantity.data[
            parseInt(title[titleLength - 2] + title[titleLength - 1])
          ] = item.data;
        });
      }
      let configTem = {
        type: "bar",
        data: {
          labels: [],
          datasets: [
            {
              type: "bar",
              label: "Temperature (sec)",
              data: [],
              borderWidth: 2,
              backgroundColor: "#97DC98",
              fill: false,
              borderColor: "#38913A",
              pointRadius: 2,
            },
          ],
        },
        options: {
          layout: {
            padding: {
              top: 20,
            },
          },
          animation: {
            duration: 0,
          },
          interaction: {
            mode: "nearest",
          },
          maintainAspectRatio: false,
          legend: {
            display: false,
            position: "bottom",
          },
          legendCallback(chart) {
            let text = [];
            text.push('<div class="chart-legend">');
            for (let i = chart.data.datasets.length - 1; i >= 0; i--) {
              let dataset = chart.data.datasets[i];
              let borderDashClass = "";
              let legendIconHtml = "";
              if (dataset.type === "line") {
                // line
                borderDashClass = "dash";
                if (dataset.borderDash) {
                  // dashed line
                  legendIconHtml =
                    '<div class="legend-icon" style="height: 1px; border-top: ' +
                    dataset.borderWidth +
                    "px  dashed" +
                    dataset.borderColor +
                    ';"></div>';
                } else {
                  // normal line
                  legendIconHtml =
                    '<div class="legend-icon" style="height: 1px; border-top: ' +
                    3 +
                    "px  solid" +
                    dataset.borderColor +
                    ';"></div>';
                }
              } else if (dataset.type === "bar") {
                // bar
                legendIconHtml =
                  '<div class="legend-icon bar" style="background-color:' +
                  dataset.backgroundColor +
                  "; border-color: " +
                  dataset.borderColor +
                  ';"></div>';
              }
              text.push(
                '<div><div class="legend-item" id="legend-item-' +
                  i +
                  '">' +
                  legendIconHtml
              );
              if (chart.data.datasets[i].label) {
                text.push(
                  '<div class="label">' +
                    chart.data.datasets[i].label +
                    "</div>"
                );
              }

              text.push('</div></div><div class="clear"></div>');
            }

            text.push("</div>");
            return text.join("");
          },
          tooltips: {
            mode: "index",
            intersect: false,
          },
          scales: {
            xAxes: [
              {
                stacked: false,
                // stacked: true,
                gridLines: {
                  drawOnChartArea: false,
                },
                scaleLabel: {
                  display: true,
                  labelString: "Hour",
                  fontSize: 13,
                },
              },
            ],
            yAxes: [
              {
                id: "tem",
                type: "linear",
                stacked: false,
                ticks: {
                  beginAtZero: false,
                  min: 0,
                },
                scaleLabel: {
                  display: true,
                  labelString: "Temperature (°C)",
                  fontSize: 13,
                },
              },
              {
                id: "shot",
                type: "linear",
                stacked: false,
                ticks: {
                  beginAtZero: false,
                  min: 0,
                },
                position: "right",
                scaleLabel: {
                  display: true,
                  labelString: "Shot Quantity",
                },
                gridLines: {
                  drawOnChartArea: false,
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
      let tooltipEl = document.getElementById("chartjs-tooltip");
      if (tooltipEl) {
        tooltipEl.remove();
      }
      configTem.options.tooltips = {
        // Disable the on-canvas tooltip
        enabled: false,

        custom: function (tooltipModel) {
          // Tooltip Element

          // Create element on first render
          if (!tooltipEl) {
            tooltipEl = document.createElement("div");
            tooltipEl.id = "chartjs-tooltip";
            tooltipEl.innerHTML = '<table style="margin-bottom: 0;"></table>';
            document.body.appendChild(tooltipEl);
          }

          // Hide if no tooltip
          if (tooltipModel.opacity === 0) {
            tooltipEl.style.opacity = 0;
            return;
          }

          // Set caret Position
          tooltipEl.classList.remove("above", "below", "no-transform");
          if (tooltipModel.yAlign) {
            tooltipEl.classList.add(tooltipModel.yAlign);
          } else {
            tooltipEl.classList.add("no-transform");
          }
          function getBody(bodyItem) {
            return bodyItem.lines;
          }
          // Set Text
          if (
            tooltipModel.body &&
            tooltipModel.body[0].lines[0].includes("Shot")
          ) {
            if (tooltipModel.body) {
              var titleLines = tooltipModel.title || [];
              var bodyLines = tooltipModel.body.map(getBody);

              var innerHtml = "<thead>";

              titleLines.forEach(function (title) {
                innerHtml +=
                  '<tr><th style="font-size: 14px;border: none;">' +
                  title +
                  "</th></tr>";
              });
              innerHtml += "</thead><tbody>";

              bodyLines.forEach(function (body, i) {
                var span = `<div style="display: flex;align-items: center;font-size: 14px"><div class="box-title"></div><div>${body}</div></div>`;
                innerHtml +=
                  '<tr><td style="border: none;">' + span + "</td></tr>";
              });
              innerHtml += "</tbody>";

              var tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
              tableRoot.style.marginBottom = "0!important";
            }
          } else {
            if (tooltipModel.body) {
              var titleLines = tooltipModel.title || [];

              var innerHtml = "<thead>";

              titleLines.forEach(function (title) {
                innerHtml +=
                  '<tr><th style="border: none;">' + title + "</th></tr>";
              });
              innerHtml += "</thead><tbody>";

              if (tooltipModel.title) {
                let data = chartDataTemp[+tooltipModel.title];
                console.log(chartDataTemp, "datadatadata 1");
                console.log(tooltipModel.title, "datadatadata 2");
                console.log(data, "datadatadata 3");
                innerHtml +=
                  '<tr><td style="border: none;">' +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">THI: ${data[2]}°C</div>` +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">TAV: ${data[1]}°C</div>` +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">TLO: ${data[0]}°C</div>` +
                  "</td></tr>";
                innerHtml += "</tbody>";
              }

              var tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
            }
          }

          // `this` will be the overall tooltip
          var position = this._chart.canvas.getBoundingClientRect();

          // Display, position, and set styles for font
          tooltipEl.style.opacity = 0.9;
          tooltipEl.style.borderRadius = "6px";
          tooltipEl.style.position = "absolute";
          tooltipEl.style[`z-index`] = 9999;
          tooltipEl.style.left =
            position.left + window.pageXOffset + tooltipModel.caretX + "px";
          tooltipEl.style.top =
            position.top +
            window.pageYOffset +
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
        },
      };
      shotQuantity.type =
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
          ? "line"
          : "bar";
      const ctxTem = document.querySelector(
        this.getRootToCurrentId() + "#hourly-chart" + this.mold.id
      );
      this.chartTemperature = new Chart(ctxTem, configTem);
      this.chartTemperature.data.labels = labels;
      this.chartTemperature.data.datasets[0].data = chartData;

      configTem.data.datasets.unshift(shotQuantity);
      $(this.getRootToCurrentId() + "#hourly-chart" + this.mold.id).off(
        "click",
        this.onClickHourlyChart
      );
      $(this.getRootToCurrentId() + "#hourly-chart" + this.mold.id).on(
        "click",
        this.onClickHourlyChart
      );
      /*
      let self = this;
      $(this.getRootToCurrentId() +  "#hourly-chart" + this.mold.id).on("click", (e) => {
        console.log("hourly-chart" + this.mold.id);
        let chart = this.chartTemperature;
        let activePoints = chart.getElementsAtEventForMode(
          e,
          "point",
          this.chartTemperature.options
        );
        let firstPoint = activePoints[0];
        if (firstPoint != undefined) {
          let label = chart.data.labels[firstPoint._index];
/!*
          let child = Common.vue.getChild(vm.$children, "chart-mold");
          if (child != null) {
            child.details(label);
          }
*!/
          self.details(label);

        }
      });
*/
      this.chartTemperature.resize();
      this.chartTemperature.update();
    },
    onClickHourlyChart(e) {
      let self = this;
      console.log("hourly-chart" + this.mold.id);
      let chart = this.chartTemperature;
      let activePoints = chart.getElementsAtEventForMode(
        e,
        "point",
        this.chartTemperature.options
      );
      let firstPoint = activePoints[0];
      if (firstPoint != undefined) {
        let label = chart.data.labels[firstPoint._index];
        self.details(label);
      }
    },
    setGraphAxisTitle() {
      let xAxisTitle = this.xAxisTitleMapping[this.requestParam.dateViewType];
      let yAxisTitle = this.yAxisTitleMapping[this.chartType];
      config.options.scales.xAxes[0].scaleLabel.labelString = xAxisTitle;
      config.options.scales.yAxes[0].scaleLabel.labelString = yAxisTitle;
    },
    /*
    findMoldParts() {
      // Parts 조회
      axios.get("/api/molds/" + this.mold.id + "/parts").then((response) => {
        this.parts = response.data.content;

        //$('#op-mold-details').modal('show');
      });
    },
*/
    /*
    getPartChanges(id, startDate, endDate) {
      this.loadingPartChange = true;
      let params = {
        moldId: id,
      };
      if (this.requestParam.dateViewType == "DAY") {
        let fromD = new Date(
          this.yearSelected,
          this.monthSelected - 1,
          this.day
        );
        params.startDate = moment(fromD).format("YYYYMMDDHHmmss");
        params.endDate = moment(
          new Date(this.yearSelected, this.monthSelected, 1)
        ).format("YYYYMMDDHHmmss");
      }
      if (this.requestParam.dateViewType == "HOURLY") {
        let fromD = new Date(
          this.yearSelected,
          this.monthSelected - 1,
          this.day
        );
        params.startDate = moment(fromD).format("YYYYMMDDHHmmss");
        params.endDate = moment(
          new Date(this.yearSelected, this.monthSelected - 1, this.day + 1)
        ).format("YYYYMMDDHHmmss");
      }
      if (startDate) {
        params.startDate = startDate;
      }
      if (endDate) {
        params.endDate = endDate;
      }
      let paramUri = Common.param(params);

      axios
        .get("/api/chart/get-part-changes?" + paramUri)
        .then((res) => {
          this.numberOfPart = res.data.data?.numberOfPart || 0;
          this.singlePartName = res.data.data?.singlePartName;
          this.partChangeData = res.data.data?.list;
        })
        .finally(() => {
          this.loadingPartChange = false;
        });
    },
*/
    details(detailDate) {
      console.log(detailDate, "detailDate");
      if (this.requestParam.dateViewType === "HOUR") {
        if (
          this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS") ||
          this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
        ) {
          if (this.isShowFrequentlyChart === true && isNaN(detailDate)) {
            return;
          }
          this.bindFrequentlyDataChart(detailDate);
          return;
        }
        /*
        let date = `${
          Number(this.monthHour) < 10
            ? `0${Number(this.monthHour)}`
            : this.monthHour
        }/${Number(this.day) < 10 ? `0${this.day}` : this.day}`;
*/
        let date = `${
          Number(this.requestParam.month) < 10
            ? `0${Number(this.requestParam.month)}`
            : this.requestParam.month
        }/${
          Number(this.requestParam.day) < 10
            ? `0${Number(this.requestParam.day)}`
            : this.requestParam.day
        }`;

        this.requestParam.detailDate = date;
      }
    },
    /*
    checkingShowChartDetail(detailDate) {
      let checkingDate = null;
      switch (this.requestParam.dateViewType) {
        case "MONTH":
          checkingDate = `M${this.requestParam.year}${detailDate}`;
          break;
        case "WEEK":
          checkingDate =
            `W${this.requestParam.year}` + detailDate.split("-")[1];
          break;
        case "DAY":
          checkingDate =
            this.requestParam.year + detailDate.split("/").join("");
          break;
        default:
          checkingDate = this.requestParam.year + detailDate;
      }
      let checkingParam = {
        moldId: this.requestParam.moldId,
        dateDetails: checkingDate,
      };

      checkingParam = Common.param(checkingParam);
      axios
        .get("/api/statistics/check-old-counter?" + checkingParam)
        .then((response) => {
          this.isShowTemperatureColumn = !response.data;
          if (["MONTH", "WEEK"].includes(this.requestParam.dateViewType)) {
            // this.paging(1);
          } else {
            // day
            if (response.data) {
              // this.paging(1);
            } else if (this.isNotCMSCounter()) {
              const listDate = detailDate.split("/");
              if (listDate.length === 2) {
                this.monthHour = listDate[0];
                this.day = Number(listDate[1]);
              }
              this.dateViewType("HOUR");
            }
          }
        })
        .catch((error) => {
          console.log("check-old-counter-error", error.response);
        });
    },
*/
    paging(pageNumber) {
      $(this.getRootToCurrentId() + "#op-daily-details").show();
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let paramData = Object.assign({}, this.requestParam);
      if (paramData.dateViewType === "HOUR") {
        paramData.dateViewType = "DAY";
      }
      let param = Common.param(paramData);
      axios.get("/api/statistics?" + param).then(
        (response) => {
          this.total = response.data.totalElements;
          this.results = response.data.content;
          this.pagination = Common.getPagingData(response.data);

          Common.handleNoResults("#op-daily-details", this.results.length);
        },
        (error) => {
          console.log(error.response);
        }
      );
    },
    /*
    closeDatePicker() {
      console.log("close");
    },
*/
    /*
    handleChangeDate(data, tempData) {
      if (data.isCustomRange) {
        if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
        } else {
          this.requestParam.dateViewType = tempData.selectedType.replace(
            "LY",
            ""
          );
        }
        this.requestParam.startDate = data.range.startDate;
        this.requestParam.endDate = data.range.endDate;
        this.requestParam.year = "";
        this.requestParam.month = "";
        this.yearSelected = "";
        this.isSelectedCustomRange = true;
      } else {
        this.requestParam.dateViewType = tempData.selectedType.replace(
          "LY",
          ""
        );
        this.requestParam.year = data.time.slice(0, 4);
        delete this.requestParam.month;
        delete this.requestParam.day;
        delete this.requestParam.startDate;
        delete this.requestParam.endDate;
        if (tempData.selectedType == "HOURLY") {
          this.requestParam.month = tempData.singleDate.slice(4, 6);
          this.requestParam.day = moment(data.time, "YYYYMMDD").format("DD");
        } else if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.requestParam.month = tempData.singleDate.slice(4, 6);
        } else {
          //WEEKLY or MONTHLY
          //doNothing
        }
        this.isSelectedCustomRange = false;
      }

      //  this.bindChartData();

      let startDate = null;
      let endDate = null;
      if (data.isCustomRange) {
        startDate = moment(data.range.startDate, "YYYYMMDD").format(
          "YYYYMMDDHHmmss"
        );
        endDate = moment(data.range.endDate, "YYYYMMDD")
          .add(1, "d")
          .format("YYYYMMDDHHmmss");
      } else {
        if (tempData.selectedType == "HOURLY") {
          this.monthHour = tempData.singleDate.slice(4, 6);
          startDate = moment(data.time, "YYYYMMDD").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMMDD")
            .add(1, "d")
            .format("YYYYMMDDHHmmss");
          // this.hourlyDate = data.time
          this.day = moment(data.time, "YYYYMMDD").format("DD");
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;
        } else if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.monthHour = tempData.singleDate.slice(4, 6);
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;

          startDate = moment(data.time, "YYYYMM").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMM")
            .add(1, "M")
            .format("YYYYMMDDHHmmss");
        } else if (
          tempData.selectedType == "WEEKLY" ||
          tempData.selectedType == "MONTHLY"
        ) {
          startDate = moment(data.time, "YYYY").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYY")
            .add(1, "y")
            .format("YYYYMMDDHHmmss");
        }
      }

      this.bindChartData();
      this.getPartChanges(this.mold.id, startDate, endDate);
    },
*/

    initChart() {
      console.log("init chart");
      const ctx = document.querySelector(
        this.getRootToCurrentId() + "#chart-mold"
      );
      const ctx2 = document.querySelector(
        this.getRootToCurrentId() + "#chart-frequently"
      );

      let self = this;
      self.chart = new Chart(ctx, config);
      // 차트 클릭 시
      $(self.getRootToCurrentId() + "#chart-mold").on("click", (e) => {
        let chart = self.chart;
        //let activePoints = chartOverview.getSegmentsAtEvent(e);
        let activePoints = chart.getElementsAtEventForMode(
          e,
          "point",
          self.chart.options
        );
        let firstPoint = activePoints[0];

        if (firstPoint != undefined) {
          let label = chart.data.labels[firstPoint._index];
          self.details(label);
        }
      });

      $(self.getRootToCurrentId() + ".op-close-details").on("click", (e) => {
        $(self.getRootToCurrentId() + "#op-daily-details").hide();
      });
      // frequently chart

      $(
        self.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      self.frequentlyChart = new Chart(ctx2, frequentlyConfig);
      $(self.getRootToCurrentId() + ".op-close-frequently").on("click", (e) => {
        $(
          self.getRootToCurrentId() + ".frequently-chart-container-wrapper"
        ).hide();
      });
    },
  },
  computed: {
    isHourlyChart() {
      return (
        this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS") ||
        this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")
      );
    },
    isTemBar() {
      return (
        this.chartTypeConfig.length > 0 &&
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "TEMPERATURE"
        )[0].chartType === "BAR" &&
        this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
      );
    },
  },
  mounted() {
    this.initChart();
  },
  watch: {
    monthHour(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
        }
        this.month = parseInt(month);
      }
    },
    isShowFrequentlyChart(newVal) {
      this.$emit("on-show-frequently-chart", newVal);
    },
    currentPage(newVal, oldVal) {
      if (newVal !== oldVal && newVal !== undefined) {
        console.log("currentPage change:", newVal);
        if (
          this.isSelectedCustomRange &&
          this.customRangeData.page !== newVal
        ) {
          this.customRangeData.page = newVal;
          let dataShow = this.getDataShow(this.customRangeData.allData, newVal);
          this.bindAllChartData(dataShow);
        }
      }
    },
    detailCurrentPage(newVal, oldVal) {
      if (newVal !== oldVal && newVal !== undefined) {
        console.log("detailCurrentPage change:", newVal);
        if (
          this.isShowFrequentlyChart &&
          this.frequentlyChartDataIndex !== newVal
        ) {
          this.frequentlyChartDataIndex = newVal;
          this.bindFrequentlyChart(
            this.frequentlyChartData[this.frequentlyChartDataIndex]
          );
        }
      }
    },
  },
};

const config = {
  type: "bar",
  data: {
    labels: [],
    datasets: [],
  },
  options: {
    layout: {
      padding: {
        top: 20,
      },
    },
    animation: {
      duration: 0,
    },
    interaction: {
      mode: "nearest",
    },
    maintainAspectRatio: false,
    legend: {
      display: false,
      position: "bottom",
      // onClick: (e) => e.stopPropagation(), // legend 클릭 시 차트 토글 여부 비활성화 처리 중
    },
    legendCallback(chart) {
      let text = [];
      text.push('<div class="chart-legend">');
      for (let i = 0; i < chart.data.datasets.length; i++) {
        let dataset = chart.data.datasets[i];
        let borderDashClass = "";
        let legendIconHtml = "";
        if (dataset.type === "line") {
          switch (dataset.label) {
            case "Within (sec)":
              legendIconHtml =
                '<img style="max-width: 105px" src="/images/graph/graph-setting-line.svg"/>';
              break;
            case "L1 (sec)":
              break;
            case "L2 (sec)":
              break;
            case "Within":
              legendIconHtml =
                '<img style="max-width: 105px" src="/images/graph/graph-setting-line.svg"/>';
              break;
            case "L1":
              break;
            case "L2":
              break;
            default:
              if (dataset.borderDash) {
                // dashed line
                legendIconHtml =
                  '<div class="legend-icon" style="height: 1px; border-top: ' +
                  dataset.borderWidth +
                  "px  dashed" +
                  dataset.borderColor +
                  ';"></div>';
              } else {
                // normal line
                legendIconHtml =
                  '<div class="legend-icon" style="height: 1px; border-top: ' +
                  3 +
                  "px  solid" +
                  dataset.borderColor +
                  ';"></div>';
              }
          }
        } else if (dataset.type === "bar") {
          // bar
          legendIconHtml =
            '<div class="legend-icon bar" style="background-color:' +
            dataset.backgroundColor +
            "; border-color: " +
            dataset.borderColor +
            ';"></div>';
        }
        text.push(
          '<div><div class="legend-item" id="legend-item-' +
            i +
            '">' +
            legendIconHtml
        );
        if (chart.data.datasets[i].label) {
          if (dataset.type === "line") {
            switch (dataset.label) {
              case "Within (sec)":
                text.push(
                  '<div class="label"> Cycle Time (Within, L1, L2) </div>'
                );
                break;
              case "L1 (sec)":
                break;
              case "L2 (sec)":
                break;
              case "Within":
                text.push('<div class="label"> Uptime (Within, L1, L2) </div>');
                break;
              case "L1":
                break;
              case "L2":
                break;
              default:
                text.push(
                  '<div class="label">' +
                    chart.data.datasets[i].label +
                    "</div>"
                );
            }
          } else {
            text.push(
              '<div class="label">' + chart.data.datasets[i].label + "</div>"
            );
          }
        }

        text.push('</div></div><div class="clear"></div>');
      }

      text.push("</div>");

      return text.join("");
    },
    tooltips: {
      mode: "index",
      intersect: false,
    },
    scales: {
      xAxes: [
        {
          stacked: true,
          gridLines: {
            drawOnChartArea: false,
          },
          scaleLabel: {
            display: true,
            labelString: "Date",
            fontSize: 13,
          },
        },
      ],
      yAxes: [
        {
          id: "first-y-axis",
          type: "linear",
          stacked: true,
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          scaleLabel: {
            display: true,
            labelString: "Shot",
            fontSize: 13,
          },
        },
        {
          id: "second-y-axis",
          type: "linear",
          stacked: false,
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          position: "right",
          scaleLabel: {
            display: true,
            labelString: "Cycle Time",
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

const frequentlyConfig = Object.assign({}, config);
frequentlyConfig.options.scales.yAxes[0].ticks.beginAtZero = false;
// const self = this;
/*
$(function () {
  console.log("init chart");
  const ctx = document.getElementById("chart-mold");
  const ctx2 = document.getElementById("chart-frequently");
  window.chart = new Chart(ctx, config);

  // 차트 클릭 시
  $("#chart-mold").on("click", (e) => {
    let chart = window.chart;
    //let activePoints = chartOverview.getSegmentsAtEvent(e);
    let activePoints = chart.getElementsAtEventForMode(
      e,
      "point",
      window.chart.options
    );
    let firstPoint = activePoints[0];

    if (firstPoint != undefined) {
      let label = chart.data.labels[firstPoint._index];
      let child = Common.vue.getChild(vm.$children, "chart-mold");
      if (child != null) {
        child.details(label);
      }
    }
  });

  $(".op-close-details").on("click", (e) => {
    $("#op-daily-details").hide();
  });
  // frequently chart

  $(".frequently-chart-container-wrapper").hide();
  window.frequentlyChart = new Chart(ctx2, frequentlyConfig);
  $(".op-close-frequently").on("click", (e) => {
    let child = Common.vue.getChild(vm.$children, "chart-mold");
    if (child != null) {
      // child.hideFrequentlyChart();
    }
    $(".frequently-chart-container-wrapper").hide();
  });
});
*/

// end frequently chart
</script>

<style
  cus-no-cache
  src="/components/chart-mold/detail-graph/chart-mold-golbal.css"
></style>
<style
  cus-no-cache
  scoped
  src="/components/chart-mold/detail-graph/chart-mold-scope.css"
></style>
