<template>
  <div
    :style="{
      height: 'calc(100vh - 120px)',
      overflowY: 'auto',
      padding: '20px',
    }"
  >
    <div
      :style="{
        display: 'flex',
        justifyContent:
          chartTimeScale === 'HOUR' ? 'space-between' : 'flex-end',
      }"
    >
      <div
        v-if="chartTimeScale === 'HOUR'"
        @click="goBackButtonHandler"
        :style="{ display: 'flex', alignItems: 'center' }"
      >
        <emdn-icon-button
          button-type="arrow-backward-skinny"
        ></emdn-icon-button>
        <cta-button button-type="text" :click-handler="goBackButtonHandler">
          Back to Daily View
        </cta-button>
      </div>
      <!-- data test 용 -->
      <!-- <cta-button button-type="text" :click-handler="changeData">
        change data
      </cta-button> -->
      <span class="graph-calendar" v-if="chartTimeScale === 'DATE'">
        <emdn-cta-button
          button-type="date-picker"
          color-type="blue-fill"
          :click-handler="openCalendarHandler"
        >
          {{ selectedDateDisplayName }}
        </emdn-cta-button>
        <emdn-date-picker
          v-show="isCalendarVisible"
          :style="{
            background: 'white',
            position: 'absolute',
            zIndex: 1,
            right: 0,
            top: 'calc(100% + 5px)',
          }"
          :time-scale="'DATE'"
          :date-range="dateRange"
          :max-date="maxDate"
          :min-date="minDate"
          @get-date-range="getDateRange"
          @get-time-scale="getTimeScale"
        ></emdn-date-picker>
      </span>
      <!-- TODO: time picker 컴포넌트로 교체 예정 -->
      <div
        :style="{
          display: 'flex',
          alignItems: 'center',
          fontWeight: 'bold',
          fontSize: '16px',
          marginRight: '15px',
        }"
        v-if="chartTimeScale === 'HOUR'"
      >
        {{
          `${hourChartRange.start.format(
            "YYYY-MM-DD HH:mm"
          )} ~ ${hourChartRange.end.format("HH:mm")}`
        }}
      </div>
    </div>
    <div class="graph-heading">
      <div class="graph-heading-sub">
        <h1 class="graph-first-h">Tooling ID</h1>
        <p class="graph-first-p">{{ mold.moldCode }}</p>
      </div>
      <div v-if="parts.length === 0">
        No parts were produced in the selected period.
      </div>
      <div v-if="parts.length > 0" class="d-flex">
        <emdn-tooltip position="bottom" style-props="width: max-content">
          <template #context>
            <div class="graph-second-h" :style="{ marginRight: '5px' }">
              {{ parts[0].name }}
            </div>
          </template>
          <template #body>
            <div>
              <div :style="{ borderBottom: 'solid 1px #fff' }">
                Part Name / ID
              </div>
              <div v-for="(item, index) in parts" :key="index">
                {{ item.name }} / {{ item.partCode }}
              </div>
            </div>
          </template>
        </emdn-tooltip>
        was produced in the selected period
        <emdn-tooltip position="bottom" style-props="width: max-content">
          <template #context>
            <img
              :style="{ marginLeft: '5px' }"
              src="/images/icon/new-info.svg"
              alt=""
            />
          </template>
          <template #body>
            <div>
              <div :style="{ borderBottom: 'solid 1px #fff' }">
                Part Name / ID
              </div>
              <div v-for="(item, index) in parts" :key="index">
                {{ item.name }} / {{ item.partCode }}
              </div>
            </div>
          </template>
        </emdn-tooltip>
      </div>
    </div>
    <div class="chart-row">
      <!-- TODO: minDate 일 때 -->
      <emdn-icon-button
        v-if="chartTimeScale === 'DATE'"
        :disabled="dateRange.end <= minDate || isLoading"
        :click-handler="previousButtonHandler"
        button-type="arrow-backward"
      ></emdn-icon-button>

      <div class="chart-wrapper">
        <div class="chart-box">
          <h1 class="graph-title">Process Time</h1>
          <div>
            <div v-if="isLoading" class="chart-loading-box">
              <emdn-dot-spinner :width="40" :height="40"></emdn-dot-spinner>
            </div>
            <emdn-xy-chart
              :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
              v-if="chartTimeScale === 'DATE' && !isLoading"
              :style-props="chartStyleProps"
              :line-set="{
                isBullet: true,
                bullet: { isVisible: false, radius: 10 },
              }"
              :log-test="false"
              :chart-set="dateLeftChartSet"
              :x-axis-extra-label-set="{ text: 'Elapsed time (hours)' }"
              :legend-set="{ forceHidden: true }"
              :line-data-binder="processTimeDateDataBinder"
              :category="category"
              :data="chartData"
              :set-x-axis-fill-rule="setXAxisFillRule"
              :set-custom="setDateChartCustom"
              :set-custom-legend="setDateChartCustomLegend"
              :ref="'DateLeftChartWrapper'"
              :id="'DateLeftChartWrapper'"
              :key="'DateLeftChartWrapper'"
            ></emdn-xy-chart>
            <emdn-xy-chart
              :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
              v-if="chartTimeScale === 'HOUR' && !isLoading"
              :style-props="chartStyleProps"
              :bubble-set="{ bullet: { isFilled: true } }"
              :x-axis-label-text-adapter="hourXAxisLabelTextAdapter"
              :log-test="false"
              :chart-set="hourLeftChartSet"
              :x-axis-extra-label-set="{ text: 'Elapsed time (minutes)' }"
              :legend-set="{ forceHidden: true }"
              :category="category"
              :data="chartData"
              :bubble-data-binder="processTimeHourBubbleDataBinder"
              :step-line-data-binder="processTimeHourStepLineDataBinder"
              :set-x-axis-fill-rule="setXAxisFillRule"
              :set-bubble-series-adapter="setBubbleSeriesAdapter"
              :set-custom-legend="setHourChartCustomLegend"
              :ref="'HourLeftChartWrapper'"
              :id="'HourLeftChartWrapper'"
              :key="'HourLeftChartWrapper'"
            ></emdn-xy-chart>
          </div>
        </div>
        <div class="chart-box">
          <h1 class="graph-title">Pressure index</h1>
          <div>
            <div v-if="isLoading" class="chart-loading-box">
              <emdn-dot-spinner :width="40" :height="40"></emdn-dot-spinner>
            </div>
            <emdn-xy-chart
              :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
              v-if="chartTimeScale === 'DATE' && !isLoading"
              :style-props="chartStyleProps"
              :chart-set="dateRightChartSet"
              :x-axis-extra-label-set="{ text: 'Elapsed time (hours)' }"
              :legend-set="{ forceHidden: true }"
              :line-set="{
                isBullet: true,
                bullet: { isVisible: false, radius: 10 },
              }"
              :line-data-binder="PressureDateDataBinder"
              :category="category"
              :data="chartData"
              :set-x-axis-fill-rule="setXAxisFillRule"
              :set-custom="setDateChartCustom"
              :set-custom-legend="setDateChartCustomLegend"
              :ref="'DateRightChartWrapper'"
              :id="'DateRightChartWrapper'"
              :key="'DateRightChartWrapper'"
            ></emdn-xy-chart>
            <emdn-xy-chart
              :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
              v-if="chartTimeScale === 'HOUR' && !isLoading"
              :style-props="chartStyleProps"
              :bubble-set="{ bullet: { isFilled: true } }"
              :x-axis-label-text-adapter="hourXAxisLabelTextAdapter"
              :chart-set="hourRightChartSet"
              :x-axis-extra-label-set="{ text: 'Elapsed time (minutes)' }"
              :legend-set="{ forceHidden: true }"
              :bubble-data-binder="PressureHourBubbleDataBinder"
              :step-line-data-binder="PressureHourStepLineDataBinder"
              :category="category"
              :data="chartData"
              :set-x-axis-fill-rule="setXAxisFillRule"
              :set-bubble-series-adapter="setBubbleSeriesAdapter"
              :set-custom-legend="setHourChartCustomLegend"
              :ref="'HourRightChartWrapper'"
              :id="'HourRightChartWrapper'"
              :key="'HourRightChartWrapper'"
            ></emdn-xy-chart>
          </div>
        </div>
        <emdn-chart-legend
          v-show="!isLoading"
          :style-props="'width: 100%; height: 50px;'"
          :legend-set="{ height: 50, clickTarget: 'none' }"
          :legend-data="legendData"
        ></emdn-chart-legend>
      </div>
      <!-- TODO: maxDate 일 때 -->
      <emdn-icon-button
        v-if="chartTimeScale === 'DATE'"
        :disabled="dateRange.end >= maxDate || isLoading"
        :click-handler="nextButtonHandler"
        button-type="arrow-forward"
      ></emdn-icon-button>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "process-analysis-chart",
  props: {
    mold: Object,
  },
  data() {
    return {
      dateLeftChartSet: {
        xAxis: { extraLabel: { text: "Elapsed time (hours)" } },
        yAxis: {
          isStacked: true,
        },
        yAxisList: [
          {
            extraLabelText: "Injection (sec)",
            opposite: false,
          },
          {
            extraLabelText: "Packing (sec)",
            opposite: false,
          },
          {
            extraLabelText: "Cooling (sec)",
            opposite: false,
          },
        ],
        valueAxis: { min: 0 },
        refProps: "DateLeftChart",
        id: "DateLeftChart",
        key: "DateLeftChart",
        cursor: { isAvailable: false },
      },
      hourLeftChartSet: {
        xAxis: { extraLabel: { text: "Elapsed time (minutes)" } },
        yAxis: {
          isStacked: true,
        },
        yAxisList: [
          {
            extraLabelText: "Injection (sec)",
            opposite: false,
          },
          {
            extraLabelText: "Packing (sec)",
            opposite: false,
          },
          {
            extraLabelText: "Cooling (sec)",
            opposite: false,
          },
        ],
        valueAxis: { min: 0 },
        refProps: "HourLeftChart",
        id: "HourLeftChart",
        key: "HourLeftChart",
        cursor: { isAvailable: false },
      },
      dateRightChartSet: {
        xAxis: { extraLabel: { text: "Elapsed time (hours)" } },
        yAxis: {
          isStacked: true,
        },
        yAxisList: [
          {
            extraLabelText: "Injection Index",
            opposite: false,
          },
          {
            extraLabelText: "Packing Index",
            opposite: false,
          },
        ],
        valueAxis: { min: 0 },
        refProps: "DateRightChart",
        id: "DateRightChart",
        key: "DateRightChart",
      },

      hourRightChartSet: {
        xAxis: { extraLabel: { text: "Elapsed time (minutes)" } },
        yAxis: {
          isStacked: true,
        },
        yAxisList: [
          {
            extraLabelText: "Injection Index",
            opposite: false,
          },
          {
            extraLabelText: "Packing Index",
            opposite: false,
          },
        ],
        valueAxis: { min: 0 },
        refProps: "HourRightChart",
        id: "HourRightChart",
        key: "HourRightChart",
      },
      legendData: [],
      isLoading: false,
      selectedHour: 0,
      parts: [
        // { id: 0, name: 'part Name 1', partCode: '11111' },
        // { id: 0, name: 'part Name 2', partCode: '22222' },
        // { id: 0, name: 'part Name 3', partCode: '33333' },
      ],
      // test
      dateAxisFillEvents: "",
      hourAxisFillEvents: "",
      // end
      isCalendarVisible: false,
      dateRange: {
        start: moment(new Date()),
        end: moment(new Date()),
      },
      hourChartRange: {
        start: moment(new Date()),
        end: moment(new Date()),
      },
      maxDate: moment(new Date()),
      minDate: null,
      chartStyleProps: "width: 100%; height: 100%;",
      chartTimeScale: "DATE",
      // hour data binder =======================================================================
      // process time date
      processTimeDateDataBinder: [
        {
          key: "coolingTime",
          displayName: "Cooling Time",
          color: "#1A2281",
          yAxisListIndex: 0,
        },
        {
          key: "packingTime",
          displayName: "Packing Time",
          color: "#1A2281",
          yAxisListIndex: 1,
        },
        {
          key: "injectionTime",
          displayName: "Injection Time",
          color: "#1A2281",
          yAxisListIndex: 2,
        },
      ],
      // pressure index date
      PressureDateDataBinder: [
        {
          key: "packingPressure",
          displayName: "Packing Pressure",
          // yAxisLabelName: "Packing Index",
          color: "#1A2281",
          yAxisListIndex: 0,
        },
        {
          key: "injectionPressure",
          displayName: "Injection Pressure",
          // yAxisLabelName: "Injection Index",
          color: "#1A2281",
          yAxisListIndex: 1,
        },
      ],
      // time data binder =======================================================================
      // process time hour bubble
      processTimeHourBubbleDataBinder: [
        {
          key: "coolingTime",
          displayName: "Cooling Time",
          // yAxisLabelName: "Cooling (sec)",
          color: "#040F31",
          yAxisListIndex: 0,
        },
        {
          key: "packingTime",
          displayName: "Packing Time",
          // yAxisLabelName: "Packing (sec)",
          color: "#040F31",
          yAxisListIndex: 1,
        },
        {
          key: "injectionTime",
          displayName: "Injection Time",
          // yAxisLabelName: "Injection (sec)",
          color: "#040F31",
          yAxisListIndex: 2,
        },
      ],
      // process time hour step line
      processTimeHourStepLineDataBinder: [
        {
          key: "coolingTimeLl",
          openKey: "coolingTimeUl",
          displayName: "Cooling Time Compilance Range",
          color: "#4DBD74",
          yAxisListIndex: 0,
        },
        {
          key: "packingTimeLl",
          openKey: "packingTimeUl",
          displayName: "Packing Time Compilance Range",
          color: "#4DBD74",
          yAxisListIndex: 1,
        },
        {
          key: "injectionTimeLl",
          openKey: "injectionTimeUl",
          displayName: "Injection Time Compilance Range",
          color: "#4DBD74",
          yAxisListIndex: 2,
        },
      ],
      // pressure index hour bubble
      PressureHourBubbleDataBinder: [
        {
          key: "packingPressure",
          displayName: "Packing Pressure",
          // yAxisLabelName: "Packing Index",
          color: "#040F31",
          yAxisListIndex: 0,
        },
        {
          key: "injectionPressure",
          displayName: "Injection Pressure",
          // yAxisLabelName: "Injection Index",
          color: "#040F31",
          yAxisListIndex: 1,
        },
      ],
      // pressure index hour step line
      PressureHourStepLineDataBinder: [
        {
          key: "packingPressureUl",
          openKey: "packingPressureLl",
          displayName: "Packing Pressure Compilance Range",
          color: "#4DBD74",
          yAxisListIndex: 0,
        },
        {
          key: "injectionPressureUl",
          openKey: "injectionPressureLl",
          displayName: "Injection Pressure Compilance Range",
          color: "#4DBD74",
          yAxisListIndex: 1,
        },
      ],
      category: "time",
      chartData: [
        // {
        //   time: "00",
        //   shotCount: 120,
        //   abnormalCount: 5,
        //   injectionTime: 10.123123123,
        //   injectionTimeUl: 20, // only HOUR
        //   injectionTimeLl: 50, // only HOUR
        //   refInjectionTime: null,
        //   injectionPressure: 20,
        //   injectionPressureUl: 20, // only HOUR
        //   injectionPressureLl: 50, // only HOUR
        //   packingTime: 40,
        //   packingTimeUl: 20, // only HOUR
        //   packingTimeLl: 40, // only HOUR
        //   packingPressure: 50,
        //   packingPressureUl: 20, // only HOUR
        //   packingPressureLl: 50, // only HOUR
        //   coolingTime: 30,
        //   coolingTimeUl: 10, // only HOUR
        //   coolingTimeLl: 40, // only HOUR
        //   abnormal: false, // only DATE
        //   itAbnormal: true, // only DATE
        //   ptAbnormal: true, // only DATE
        //   ctAbnormal: true, // only DATE
        //   ipAbnormal: true, // only DATE
        //   ppAbnormal: false, // only DATE
        // },
        // {
        //   time: "01",
        //   shotCount: 120,
        //   abnormalCount: 5,
        //   injectionTime: 30,
        //   injectionTimeUl: 20, // only HOUR
        //   injectionTimeLl: 50, // only HOUR
        //   refInjectionTime: 0,
        //   injectionPressure: 40,
        //   injectionPressureUl: 20, // only HOUR
        //   injectionPressureLl: 50, // only HOUR
        //   packingTime: 30,
        //   packingTimeUl: 20, // only HOUR
        //   packingTimeLl: 40, // only HOUR
        //   packingPressure: 20,
        //   packingPressureUl: 20, // only HOUR
        //   packingPressureLl: 50, // only HOUR
        //   coolingTime: 20,
        //   coolingTimeUl: 10, // only HOUR
        //   coolingTimeLl: 40, // only HOUR
        //   abnormal: false, // only DATE
        // },
        // {
        //   time: "02",
        //   shotCount: 120,
        //   abnormalCount: 5,
        //   injectionTime: 20,
        //   injectionTimeUl: 20, // only HOUR
        //   injectionTimeLl: 50, // only HOUR
        //   refInjectionTime: 20,
        //   injectionPressure: 20,
        //   injectionPressureUl: 20, // only HOUR
        //   injectionPressureLl: 50, // only HOUR
        //   packingTime: 50,
        //   packingTimeUl: 20, // only HOUR
        //   packingTimeLl: 40, // only HOUR
        //   packingPressure: 60,
        //   packingPressureUl: 20, // only HOUR
        //   packingPressureLl: 50, // only HOUR
        //   coolingTime: 10,
        //   coolingTimeUl: 10, // only HOUR
        //   coolingTimeLl: 40, // only HOUR
        //   abnormal: true, // only DATE
        // },
      ],
      chartData2: [
        {
          time: "00",
          shotCount: 120,
          abnormalCount: 5,
          injectionTime: 40,
          injectionTimeUl: 50, // only HOUR
          injectionTimeLl: 60, // only HOUR
          refInjectionTime: 20,
          injectionPressure: 0,
          injectionPressureUl: 40, // only HOUR
          injectionPressureLl: 50, // only HOUR
          packingTime: 40,
          packingTimeUl: 20, // only HOUR
          packingTimeLl: 60, // only HOUR
          packingPressure: 20,
          packingPressureUl: 30, // only HOUR
          packingPressureLl: 50, // only HOUR
          coolingTime: 50,
          coolingTimeUl: 20, // only HOUR
          coolingTimeLl: 50, // only HOUR
          abnormal: true, // only DATE
          itAbnormal: true, // only DATE
          ptAbnormal: true, // only DATE
          ctAbnormal: true, // only DATE
          ipAbnormal: true, // only DATE
          ppAbnormal: false, // only DATE
        },
        {
          time: "01",
          shotCount: 120,
          abnormalCount: 5,
          injectionTime: 30,
          injectionTimeUl: 20, // only HOUR
          injectionTimeLl: 50, // only HOUR
          refInjectionTime: 20,
          injectionPressure: 40,
          injectionPressureUl: 20, // only HOUR
          injectionPressureLl: 50, // only HOUR
          packingTime: 30,
          packingTimeUl: 20, // only HOUR
          packingTimeLl: 40, // only HOUR
          packingPressure: 20,
          packingPressureUl: 20, // only HOUR
          packingPressureLl: 50, // only HOUR
          coolingTime: 20,
          coolingTimeUl: 10, // only HOUR
          coolingTimeLl: 40, // only HOUR
          abnormal: false, // only DATE
        },
        {
          time: "02",
          shotCount: 120,
          abnormalCount: 5,
          injectionTime: 20,
          injectionTimeUl: 20, // only HOUR
          injectionTimeLl: 50, // only HOUR
          refInjectionTime: 20,
          injectionPressure: 20,
          injectionPressureUl: 20, // only HOUR
          injectionPressureLl: 50, // only HOUR
          packingTime: 50,
          packingTimeUl: 20, // only HOUR
          packingTimeLl: 40, // only HOUR
          packingPressure: 60,
          packingPressureUl: 20, // only HOUR
          packingPressureLl: 50, // only HOUR
          coolingTime: 10,
          coolingTimeUl: 10, // only HOUR
          coolingTimeLl: 40, // only HOUR
          abnormal: true, // only DATE
        },
      ],
    };
  },
  mounted() {
    this.getProcessAnalysis("DATE");
  },
  watch: {
    chartTimeScale: {
      handler(newVal) {
        if (newVal === "DATE") {
          this.legendData.splice(0);
          this.legendData = [
            {
              name: "Shot Data",
              fill: am5.color("#1A2281"),
              stroke: am5.color("#1A2281"),
            },
            // {
            //   name: "Abnormal Data Detected",
            //   fill: am5.Color.lighten(am5.color("#DB3B21"), 0.8),
            //   stroke: am5.Color.lighten(am5.color("#DB3B21"), 0.8),
            // },
          ];
        }
        if (newVal === "HOUR") {
          this.legendData.splice(0);
          this.legendData = [
            {
              name: "Normal Shot Data",
              fill: am5.color("#1A2281"),
              stroke: am5.color("#1A2281"),
            },
            {
              name: "Abnormal Shot Data",
              fill: am5.color("#DB3B21"),
              stroke: am5.color("#DB3B21"),
            },
            {
              name: "Compliance Range",
              fill: am5.color("#C3F2D7"),
              stroke: am5.color("#C3F2D7"),
            },
          ];
        }
      },
      immediate: true,
    },
  },
  computed: {
    selectedDateDisplayName() {
      return Common.getDatePickerButtonDisplay(
        this.dateRange.start,
        this.dateRange.end,
        "DATE"
      );
    },
  },
  methods: {
    getDateRange(dateRange) {
      console.log("getDateRange");
      this.dateRange = dateRange;
      this.getProcessAnalysis("DATE").then(() => {
        this.isCalendarVisible = false;
      });
    },
    getTimeScale(timeScale) {
      this.timeScale = timeScale;
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      console.log("seriesTooltipHTMLAdapter");
      let data = target.dataItem.dataContext;

      let fromDate = moment();
      let toDate = moment();

      if (this.chartTimeScale === "DATE") {
        fromDate.hours(data.time).minutes("00").seconds("00");
        toDate.hours(data.time).minutes("00").seconds("00").add(1, "hours");
        fromDate = moment(fromDate).format("HH:mm");
        toDate = moment(toDate).format("HH:mm");
      }

      if (this.chartTimeScale === "HOUR") {
        console.log("this.selectedHour: ", this.selectedHour);
        fromDate.hours(this.selectedHour).minutes(data.time).seconds("00");
        toDate
          .hours(this.selectedHour)
          .minutes(data.time)
          .seconds("00")
          .add(1, "minutes");
        fromDate = moment(fromDate).format("HH:mm");
        toDate = moment(toDate).format("HH:mm");
      }

      let html = `
        <div style='font-size: 14.66px; width: 300px;'>
            <div style='border-bottom: solid 1px #ddd'>
              ${fromDate} ~ ${toDate}
            </div>
            <div style='font-weight: bold; margin: 10px 0px;'>Process Details</div>
            <div style='display: flex; justify-content: space-between'>
                <div style='display: flex; align-items: center'>
                    <div style='
                        width: 12px;
                        height: 12px;
                        background-color: #ddf1e3;
                        margin-right: 8px;
                    '></div>
                    <span>Injection time</span>
                </div>
                <span>
                  ${data.injectionTime ? `{injectionTime}s` : "-"}
                  ${data.refInjectionTime ? `({refInjectionTime}s)` : ""}
                </span>
            </div>
            <div style='display: flex; justify-content: space-between'>
                <div style='display: flex; align-items: center'>
                    <div style='
                        width: 12px;
                        height: 12px;
                        background-color: #A6DDEA;
                        margin-right: 8px;
                    '></div>
                    <span>Packing Time</span>
                </div>
                <span>
                  ${data.packingTime ? `{packingTime}s` : "-"}
                  ${data.refPackingTime ? `({refPackingTime}s)` : ""}
                </span>
            </div>
            <div style='display: flex; justify-content: space-between'>
                <div style='display: flex; align-items: center'>
                    <div style='
                        width: 12px;
                        height: 12px;
                        background-color: #8C90C0;
                        margin-right: 8px;
                    '></div>
                    <span>Cooling Time</span>
                </div>
                <span>
                  ${data.coolingTime ? `{coolingTime}s` : "-"}
                  ${data.refCoolingTime ? `({refCoolingTime}s)` : ""}
                </span>
            </div>
            ${
              this.chartTimeScale === "DATE"
                ? `<div style='font-weight: bold; margin: 10px 0px;'>Shot Details</div>
            <div style='display: flex; justify-content: space-between'>
                <div style='display: flex; align-items: center'>
                    <div style='
                        width: 12px;
                        height: 12px;
                        background-color: #1A2281;
                        margin-right: 8px;
                    '></div>
                    <span>Total Shots</span>
                </div>
                <span>{shotCount}</span>
            </div>
            <div style='display: flex; justify-content: space-between'>
                <div style='display: flex; align-items: center'>
                    <div style='
                        width: 12px;
                        height: 12px;
                        background-color: #E34537;
                        margin-right: 8px;
                    '></div>
                    <span>Abnormal Shots</span>
                </div>
                <span>{abnormalCount}</span>
            </div>
        </div>`
                : ""
            }
            `;
      return html;
    },
    hourXAxisLabelTextAdapter(text, target) {
      if (target.dataItem) {
        if (
          [
            "00",
            "05",
            "10",
            "15",
            "20",
            "25",
            "30",
            "35",
            "40",
            "45",
            "50",
            "55",
          ].includes(target.dataItem.dataContext.time)
        ) {
          return target.dataItem.dataContext.time;
        }
      }
    },
    // TODO: 임시 함수
    changeData() {
      let chartData = this.chartData.slice();
      let chartData2 = this.chartData2.slice();

      this.chartData.splice(0);
      this.chartData2.splice(0);

      this.chartData = chartData2;
      this.chartData2 = chartData;

      console.log("this.chartData: ", this.chartData);
      console.log("this.chartData2: ", this.chartData2);
    },
    previousButtonHandler() {
      console.log("previousButtonHandler");
      this.dateRange.start.subtract(1, "days");
      this.dateRange.end.subtract(1, "days");
      this.dateRange = Object.assign({}, this.dateRange);
      this.getProcessAnalysis(this.chartTimeScale);
    },
    nextButtonHandler() {
      console.log("nextButtonHandler");
      this.dateRange.start.add(1, "days");
      this.dateRange.end.add(1, "days");
      this.dateRange = Object.assign({}, this.dateRange);
      this.getProcessAnalysis(this.chartTimeScale);
    },
    goBackButtonHandler() {
      console.log("goBackButtonHandler");
      this.getProcessAnalysis("DATE");
    },
    // api ==============================================================================================
    async getProcessAnalysis(chartTimeScale) {
      console.log("getProcessAnalysis");
      let moldIdParam = this.mold.id ? `?moldId=${this.mold.id}` : "";
      let timeScaleParam = chartTimeScale ? `&timeScale=${chartTimeScale}` : "";

      let timeValueParam = "";
      if (chartTimeScale === "DATE") {
        timeValueParam = this.dateRange.start
          ? `&timeValue=${this.dateRange.start.format("YYYYMMDD")}`
          : "";
      }
      if (chartTimeScale === "HOUR") {
        timeValueParam = this.hourChartRange.start
          ? `&timeValue=${this.hourChartRange.start.format("YYYYMMDDHH")}`
          : "";
      }

      let url =
        `/api/analysis/pro-ana` + moldIdParam + timeScaleParam + timeValueParam;

      this.isLoading = true;

      return await axios.get(url).then((res) => {
        console.log(res);
        this.chartData = res.data.content;
        this.parts = res.data.parts;
        this.chartTimeScale = chartTimeScale;
        this.isLoading = false; // TODO: test를 위한 임시 주석
        return res;
      });
    },
    setXAxisFillRule(dataItem, axisFill) {
      // console.log('setXAxisFillRule');
      return axisFill.set(
        "fillOpacity",
        dataItem.dataContext.abnormal ? 0.2 : 0
      );
    },

    fillOpacityControl(fillOpacity, target) {
      // console.log('adpater !!!!!!!');
      return target.dataItem.dataContext.abnormal ? 0.2 : fillOpacity;
    },

    setDateChartCustom(am5, chartSelf) {
      console.log("setDateChartCustom");
      // const self = this;
      // axis fill default set
      chartSelf.xAxis.get("renderer").axisFills.template.setAll({
        visible: true,
        fillOpacity: 0.2,
        fill: am5.color("#FF6961"),
      });

      // set axis fill adapters
      chartSelf.xAxis
        .get("renderer")
        .axisFills.template.adapters.add("fillOpacity", (fillOpacity, target) =>
          this.fillOpacityControl(fillOpacity, target)
        );

      // set axis fill events
      let dateAxisFillEvents =
        chartSelf.xAxis.get("renderer").axisFills.template.events;

      // axis fill hover handler
      dateAxisFillEvents.on("pointerover", (ev) => {
        // console.log('axisfill pointerover: ', ev.target.dataItem.dataContext);
        let color = ev.target.dataItem.dataContext.abnormal
          ? am5.color("#ad5cad")
          : am5.color("#579ffb");

        ev.target.set("fill", color);
        ev.target.set("fillOpacity", 0.2);
      });

      // axis fill mouse out handler
      dateAxisFillEvents.on("pointerout", (ev) => {
        // console.log('axisfill pointerout : ', ev.target.dataItem.dataContext);
        let fillOpacity = ev.target.dataItem.dataContext.abnormal ? 0.2 : 0;

        ev.target.setAll({
          fill: am5.color("#FF6961"),
          fillOpacity: fillOpacity,
        });
      });

      // axis fill click handler
      dateAxisFillEvents.on("click", (ev) => {
        // console.log('axis fill click handler');
        console.log(
          "ev.target.dataItem.dataContext.time: ",
          ev.target.dataItem.dataContext.time
        );

        let hour = Number(ev.target.dataItem.dataContext.time);
        this.selectedHour = hour;

        this.hourChartRange.start = this.dateRange.start;
        this.hourChartRange.end = this.dateRange.end;
        this.hourChartRange.start.set("hour", hour);
        this.hourChartRange.end.set("hour", hour + 1);
        this.hourChartRange.start.set("minute", 0);
        this.hourChartRange.end.set("minute", 0);

        // this.chartTimeScale = "HOUR";
        this.getProcessAnalysis("HOUR");
      });
    },

    getAbnormal(dataBinder, target) {
      let abnormal = false;

      if (dataBinder.key === "injectionTime") {
        abnormal = target.dataItem.dataContext.itAbnormal;
      }

      if (dataBinder.key === "packingTime") {
        abnormal = target.dataItem.dataContext.ptAbnormal;
      }

      if (dataBinder.key === "coolingTime") {
        abnormal = target.dataItem.dataContext.ctAbnormal;
      }

      if (dataBinder.key === "injectionPressure") {
        abnormal = target.dataItem.dataContext.ipAbnormal;
      }

      if (dataBinder.key === "packingPressure") {
        abnormal = target.dataItem.dataContext.ppAbnormal;
      }
      return abnormal;
    },

    setBubbleSeriesAdapter(bulletAdapters, am5, chartSelf, dataBinder) {
      console.log("setBubbleSeriesAdapter");

      bulletAdapters.add("fill", (fill, target) => {
        return this.getAbnormal(dataBinder, target)
          ? am5.color("#DB3B21")
          : fill;
      });
      bulletAdapters.add("stroke", (stroke, target) => {
        return this.getAbnormal(dataBinder, target)
          ? am5.color("#DB3B21")
          : stroke;
      });
    },

    // add legend custom
    setDateChartCustomLegend(am5, chartSelf) {
      console.log("setDateChartCustomLegend");
      chartSelf.legend.data.setAll([
        {
          name: "Shot Data",
          fill: am5.color("#1A2281"),
          stroke: am5.color("#1A2281"),
        },
        // {
        //   name: "Abnormal Data Detected",
        //   fill: am5.Color.lighten(am5.color("#DB3B21"), 0.8),
        //   stroke: am5.Color.lighten(am5.color("#DB3B21"), 0.8),
        // },
      ]);
    },
    setHourChartCustomLegend(am5, chartSelf) {
      console.log("setHourChartCustomLegend");

      chartSelf.legend.data.setAll([
        {
          name: "Normal Shot Data",
          fill: am5.color("#1A2281"),
          stroke: am5.color("#1A2281"),
        },
        {
          name: "Abnormal Shot Data",
          fill: am5.color("#DB3B21"),
          stroke: am5.color("#DB3B21"),
        },
        {
          name: "Compliance Range",
          fill: am5.color("#C3F2D7"),
          stroke: am5.color("#C3F2D7"),
        },
      ]);
    },
    openCalendarHandler() {
      this.isCalendarVisible = !this.isCalendarVisible;
    },
    closeCalenderHandler() {
      this.isCalendarVisible = false;
    },
  },
};
</script>

<style scoped>
.chart-row {
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-wrapper {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.chart-box {
  margin-top: 60px;
  flex: 1;
  min-width: 550px;
}
.chart-box > div {
  /* min-height: 450px; */
  /* min-height: 350px; */
  height: 350px;
}

.chart-loading-box {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.graph-sub-sec {
  display: flex;
  margin-top: 20px;
  justify-content: space-between;
  align-items: center;
}

.graph-calendar {
  position: relative;
}

.graph-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0px 32px;
  margin-top: 25px;
}
.graph-heading-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}
.graph-first-h {
  font-weight: 700;
  font-size: 14.66px;
  color: #4b4b4b;
}

.graph-first-p {
  font-weight: 400;
  font-size: 14.66px;
  color: #4b4b4b;
}

.graph-second-h {
  font-weight: 400;
  font-size: 14.66px;
  color: #3491ff;
}

p,
h1 {
  padding: 0px;
  margin: 0px;
}
.graph-sub-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}
.graph-title {
  font-weight: 700;
  font-size: 14.66px;
  text-align: center;
  width: 100%;
  color: #4b4b4b;
}
</style>
