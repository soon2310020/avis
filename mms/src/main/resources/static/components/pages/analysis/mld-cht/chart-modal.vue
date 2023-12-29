<template>
  <emdn-modal
    :is-opened="true"
    :modal-handler="closeHandler"
    heading-text="Tooling Report"
    style-props="height: 763px;"
  >
    <template #switch-tab>
      <emdn-tab-button
        tab-type="primary-tab"
        tab-style="horizontal"
        :tab-buttons="graphOrDetailTabData"
        :click-handler="graphOrDetailTabHandler"
      ></emdn-tab-button>
    </template>
    <template #body>
      <div v-show="switchTab === 'Graph'" style="position: relative">
        <div
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
          "
        >
          <emdn-tab-button
            tab-type="secondary-tab"
            tab-style="horizontal"
            :tab-buttons="tabData"
            :click-handler="graphTypeTabHandler"
            @tab-buttons-emit="tabButtonsEmit"
          ></emdn-tab-button>
          <div style="position: relative">
            <emdn-cta-button :click-handler="showDatePicker"
              >date-picker</emdn-cta-button
            >
            <emdn-date-picker
              style-props="position: absolute; top: 100%; right: 0px; z-index: 1"
              v-if="isDatePickerVisible"
            ></emdn-date-picker>
          </div>
        </div>
        <div class="info-zone">
          <div class="info-zone_tooling">
            <b>Tooling ID</b>
            <span
              v-if="mold.equipmentCode && mold.equipmentCode.length > 20"
              v-bind:data-tooltip-text="mold.equipmentCode"
              style="width: auto; margin: 4px 104px 2px 15px; padding: 0"
            >
              {{ replaceLongtext(mold.equipmentCode, 20) }}</span
            >
            <span v-else>{{ mold.equipmentCode }}</span>
          </div>
        </div>

        <div style="min-height: 500px">
          <div v-if="isLoading" class="loading-container">
            <!-- <loading :active.sync="isLoading" loader="bars" color="#579ffb"></loading> -->
          </div>
          <emdn-overall-xy-chart
            v-if="!isIncludeAcceleration"
            :key="moldChartKey"
            :default-data="defaultData"
            :overall-data="overallData"
            :category="category"
            :time-scale="timeScale"
            :axis-data-binder="axisDataBinder"
            :line-data-binder="Common.ChartJS.lineDataBinder"
            :bar-data-binder="Common.ChartJS.barDataBinder"
            :overall-chart-set="overallChartSet"
            :default-chart-set="defaultChartSet"
            :x-scrollbar-set="xScrollbarSet"
            :selection-min="selectionMin"
            :selection-max="selectionMax"
            :set-selection-min="setSelectionMin"
            :set-selection-max="setSelectionMax"
            :chart-item-click-handler="Common.ChartJS.chartItemClickHandler"
            :overall-pan-handler="overallPanHandler"
            :default-zoom-handler="defaultZoomHandler"
            :default-pan-handler="defaultPanHandler"
            :x-scrollbar-x-axis-label-text-adapter="
              xScrollbarXAxisLabelTextAdapter
            "
            :x-axis-grid-stroke-opacity-adapter="
              Common.ChartJS.xAxisGridStrokeOpacityAdapter
            "
            :x-axis-label-text-adapter="Common.ChartJS.xAxisLabelTextAdapter"
            :x-axis-tooltip-label-text-adapter="
              Common.ChartJS.xAxisTooltipLabelTextAdapter
            "
            :series-tooltip-label-html-adapter="
              Common.ChartJS.seriesTooltipLabelHtmlAdapter
            "
            :series-tooltip-force-hidden-adapter="
              Common.ChartJS.seriesTooltipForceHiddenAdapter
            "
          ></emdn-overall-xy-chart>
          <div v-if="isIncludeAcceleration">Acceleration Chart</div>
        </div>
      </div>
      <tooling-detail
        v-if="switchTab === 'Detail'"
        :show-file-previewer="showFilePreviewer"
        :resources="resources"
        :mold="mold"
      ></tooling-detail>
    </template>
  </emdn-modal>
</template>
<script>
module.exports = {
  props: {
    mold: Object,
    resources: Object,
    showFilePreviewer: Function,
    closeHandler: Function,
  },
  components: {
    "tooling-detail": httpVueLoader("/components/tooling-detail.vue"), // detail 탭을 누르면 보여지는 tooling report 상세내용
  },
  data() {
    return {
      isDatePickerVisible: false,
      isIncludeAccelerationKey: 1,
      moldChartKey: 1,
      graphOrDetailTabKey: 1,
      graphOrDetailTabData: [
        { title: "Graph", active: true },
        { title: "Detail" },
      ],
      tabData: [
        { title: "shot", active: true },
        { title: "cycle_time" },
        { title: "uptime" },
        { title: "cycle_time_analysis" },
        { title: "temperature" },
        { title: "acceleration" },
      ],

      switchTab: "Graph",
      category: "time",
      isZoomPanEventAvailable: true,
      isLoading: false,

      defaultData: [],
      overallData: [],

      dataGroup: "SHOT_COUNT", // SHOT_COUNT, CYCLE_TIME
      timeScale: "", // MONTHLY, WEEKLY, DAILY, HOURLY, MINUTELY
      oldTimeScale: "",
      overallTimeScale: "",
      overallFromDate: 0,
      overallToDate: 0,

      availableTimeScales: [],

      selectionMin: null,
      selectionMax: null,

      xScrollbarSet: {
        detail: {
          mainValue: {
            key: "sc",
            displayName: "Shot Count",
          },
        },
      },

      axisDataBinder: {
        xAxis: {
          name: "",
          // type: 'CategoryAxis',
        },
        yAxis: {
          name: "Shot Count",
          // type: 'ValueAxis',
          isLeft: true,
        },
        ySecondAxis: {
          name: "Cycle Time",
          // type: 'ValueAxis',
          isLeft: false,
          isAvailable: true,
        },
      },
    };
  },
  watch: {
    // timeScale이 변경되면 axisDataBinder를 변경해서 Chart의 xAxis에 나타날 timeScale을 수정합니다.
    timeScale(newVal) {
      this.axisDataBinder.xAxis.name = newVal;
    },
    // dataGroup(ex: 'cycle time', 'shot Count') 가 변경되면 데이터를 다시 불러옵니다.
    dataGroup() {
      this.getOverallChartData();
      this.getDefaultChartData(this.selectionMin, this.selectionMax);
    },
    "xScrollbarSet.detail.mainValue.displayName": {
      handler() {},
    },
    tabData: {
      handler(oldVal, newVal) {
        console.log("watch tabData");
        console.log("oldVal: ", oldVal);
        console.log("newVal: ", newVal);
      },
      deep: true,
    },
  },
  computed: {
    isIncludeAcceleration() {
      console.log("isIncludeAcceleration");
      // computed dependency control
      console.log(
        "this.isIncludeAccelerationKey: ",
        this.isIncludeAccelerationKey
      );
      let result = false;
      this.tabData.map((tab) => {
        if (tab.active === true && tab.title === "acceleration") {
          result = true;
        }
      });
      return result;
    },
    // selectionMin의 timestamp 값을 계산합니다.
    selectionMinTs() {
      return moment(this.selectionMin, "YYYYMMDDhhmm").unix();
    },
    // selectionMax의 timestamp 값을 계산합니다.
    selectionMaxTs() {
      return moment(this.selectionMax, "YYYYMMDDhhmm").unix();
    },
    // x축에 표시될 timeScale과 timeScale에 따른 날짜를 정의합니다.
    defaultChartSet() {
      return {
        titleLabel: {
          text:
            `${Common.ChartJS.getDataTitle(
              this.selectionMin,
              this.timeScale
            )} ~ ` +
            `${Common.ChartJS.getDataTitle(this.selectionMax, this.timeScale)}`,
        },
      };
    },
    // overall chart에 표시될 main Value 를 알려줍니다.
    overallChartSet() {
      return {
        titleLabel: {
          text: `main value: ${this.xScrollbarSet.detail.mainValue.displayName}`,
        },
      };
    },
  },
  mounted() {
    console.log("mounted 6666");
    console.log(Common);
    console.log(Common.ChartJS);
    console.log(Common.ChartJS.lineDataBinder);

    this.graphOrDetailTabHandler({ title: "Graph", active: true });
    this.graphTypeTabHandler({ title: "Shot", active: true });

    // this.switchTab.title = 'Graph';

    this.graphOrDetailTabKey = this.graphOrDetailTabKey + 1;
    this.xScrollbarSet.mainValue = Common.ChartJS.valueType.sc;

    let todayDate = moment().format("YYYYMMDD0000");

    this.overallFromDate = moment(todayDate, "YYYYMMDDhhmm")
      .subtract(2, "years")
      .format("YYYYMMDD0000");
    this.overallToDate = moment(todayDate, "YYYYMMDDhhmm")
      .add(2, "months")
      .format("YYYYMMDD0000");
    this.setSelectionMax(todayDate);
    this.setSelectionMin(
      moment(todayDate, "YYYYMMDDhhmm")
        .subtract(1, "months")
        .format("YYYYMMDD0000")
    );

    this.getAvailableTimeScales().then(() => {
      let difference = this.selectionMaxTs - this.selectionMinTs;

      console.log("this.selectionMaxTs: ", this.selectionMaxTs);
      console.log("this.selectionMinTs: ", this.selectionMinTs);
      console.log("difference: ", difference);

      Common.ChartJS.getTimeScale(difference).then((res) => {
        if (this.availableTimeScales.includes(res)) {
          this.timeScale = res;
          this.oldTimeScale = res;
          this.getOverallChartData();
          this.getDefaultChartData(this.selectionMin, this.selectionMax);
        } else {
          console.log("실패");
          alert(`mold id: ${this.mold.id}에서 ${res}를 사용할 수 없습니다.`);
          this.isZoomPanEventAvailable = true;
          this.isLoading = false;
        }
      });
    });
  },
  methods: {
    showDatePicker() {
      console.log("showDatePicker");
      this.isDatePickerVisible = !this.isDatePickerVisible;
    },
    tabButtonsEmit(buttons) {
      console.log("tabButtonsEmit");
      console.log(buttons);
      this.tabData = buttons;
      this.isIncludeAccelerationKey += 1;
    },
    // 넘어온 parameter로 selectionMin을 정의합니다.
    setSelectionMin(param) {
      this.selectionMin = param;
    },
    // 넘어온 parameter로 selectionMax을 정의합니다.
    setSelectionMax(param) {
      this.selectionMax = param;
    },
    // modal tab을 선택합니다 ('graph' 또는 'detail')
    graphOrDetailTabHandler(item) {
      console.log("graphOrDetailTabHandler: ", item);
      this.switchTab = item.title;
    },
    // graph type을 선택합니다. ('Cycle Time' 또는 'Shot Count' ,...)
    graphTypeTabHandler(item) {
      console.log("graphTypeTabHandler");
      console.log(item);
      console.log(item.title);

      // shot count
      if (item.title === "shot") {
        this.dataGroup = "SHOT_COUNT";
        this.xScrollbarSet.detail.mainValue = Common.ChartJS.valueType.sc;
      }

      // cycle time
      if (item.title === "cycle_time") {
        this.dataGroup = "CYCLE_TIME";
        this.xScrollbarSet.detail.mainValue = Common.ChartJS.valueType.oct;
      }

      // uptime (todo, 현재 shot count)
      if (item.title === "uptime") {
        this.dataGroup = "SHOT_COUNT";
        this.xScrollbarSet.detail.mainValue = Common.ChartJS.valueType.sc;
      }

      // cycle time analysis (todo, 현재 shot count)
      if (item.title === "cycle_time_analysis") {
        this.dataGroup = "SHOT_COUNT";
        this.xScrollbarSet.detail.mainValue = Common.ChartJS.valueType.sc;
      }

      // temperature (todo, 현재 shot count)
      if (item.title === "temperature") {
        this.dataGroup = "SHOT_COUNT";
        this.xScrollbarSet.detail.mainValue = Common.ChartJS.valueType.sc;
      }

      // acceleration
      if (item.title === "acceleration") {
        console.log("acceleration");
      }
    },

    // scrollbar의 x축 label을 설정하는 adapter 입니다.
    // 첫 데이터와 마지막 데이터, 그리고 title이 1일 경우(매년 1월) grouping을 합쳐서 보여줍니다.
    xScrollbarXAxisLabelTextAdapter(text, target) {
      return Common.ChartJS.xScrollbarXAxisLabelTextAdapter(
        text,
        target,
        this.overallData
      );
    },

    // overall chart의 scrollbar를 panning 했을 때 동작할 함수입니다.
    // selectionMin, selectionMax에 맞춰서 차트 데이터를 새로 불러옵니다.
    overallPanHandler() {
      console.log("overallPanHandler: ", this.selectionMin, this.selectionMax);
      this.getDefaultChartData(this.selectionMin, this.selectionMax);
    },

    // default chart를 panning 했을 때 동작할 함수 입니다.
    // 종료일이 현재일을 초과할 경우 alert를 보여줍니다.
    // 정상적으로 panning 했을 경우 selectionMin, selectionMax에 맞춰서 차트 데이터를 새로 불러옵니다.
    setPanningEvent(newFromDateTs, newToDateTs) {
      let todayTs = moment().unix();
      if (todayTs < newToDateTs) {
        alert("종료일이 현재일을 초과했습니다. 날짜를 확인해주세요.");
        this.isZoomPanEventAvailable = true;
        this.isLoading = false;
      } else {
        let selectionMin = moment.unix(newFromDateTs).format("YYYYMMDD0000");
        let selectionMax = moment.unix(newToDateTs).format("YYYYMMDD0000");
        this.getDefaultChartData(selectionMin, selectionMax);
      }
    },

    // default chart에서 zoomming 시에 동작할 함수 입니다.
    // timeScale이 변경 될 시에 차트데이터를 새로 불러옵니다.
    defaultZoomHandler(zoomSelectionMin, zoomSelectionMax) {
      console.log("defaultZoomHandler");
      console.log("zoomSelectionMin: ", zoomSelectionMin);
      console.log("zoomSelectionMax: ", zoomSelectionMax);

      if (this.isZoomPanEventAvailable) {
        let zoomSelectionMinTs = moment(
          zoomSelectionMin,
          "YYYYMMDDHHmm"
        ).unix();
        let zoomSelectionMaxTs = moment(
          zoomSelectionMax,
          "YYYYMMDDHHmm"
        ).unix();

        let difference = zoomSelectionMaxTs - zoomSelectionMinTs;

        console.log("zoomSelectionMaxTs 2: ", zoomSelectionMaxTs);
        console.log("zoomSelectionMinTs 2: ", zoomSelectionMinTs);
        console.log("difference: ", difference);

        Common.ChartJS.getTimeScale(difference).then((res) => {
          if (res !== this.oldTimeScale) {
            this.isZoomPanEventAvailable = false;
            this.isLoading = true;
            if (this.availableTimeScales.includes(res)) {
              this.timeScale = res;
              this.oldTimeScale = res;
              this.setSelectionMin(zoomSelectionMin);
              this.setSelectionMax(zoomSelectionMax);
              this.getDefaultChartData(zoomSelectionMin, zoomSelectionMax);
            } else {
              console.log("실패 ");
              alert(
                `mold id: ${this.mold.id}에서 ${res}를 사용할 수 없습니다.`
              );
              this.getDefaultChartData(this.selectionMin, this.selectionMax);
            }
          }
        });
      }
    },

    // default chart를 panning 할 경우 동작할 함수입니다.
    // param으로 'next'가 넘어올 경우 다음 데이터를 일부 포함한 데이터를 불러옵니다.
    // param으로 'previous'가 넘어올 경우 이전 데이터를 일부 포함한 데이터를 불러옵니다.
    defaultPanHandler(param) {
      console.log("defaultPanHandler", this.selectionMin, this.selectionMax);

      let difference = this.selectionMaxTs - this.selectionMinTs;
      if (this.isZoomPanEventAvailable) {
        if (param === "next") {
          this.isZoomPanEventAvailable = false;
          this.isLoading = true;
          let newFromDateTs = this.selectionMinTs + difference / 2;
          let newToDateTs = this.selectionMaxTs + difference / 2;
          this.setPanningEvent(newFromDateTs, newToDateTs);
        }
        if (param === "previous") {
          this.isZoomPanEventAvailable = false;
          this.isLoading = true;
          let newFromDateTs = this.selectionMinTs - difference / 2;
          let newToDateTs = this.selectionMaxTs - difference / 2;
          this.setPanningEvent(newFromDateTs, newToDateTs);
        }
      }
    },

    // default chart에 넣을 data를 가져옵니다.
    getDefaultChartData(selectionMin, selectionMax) {
      console.log("getDefaultChartData: ", selectionMin, selectionMax);

      return new Promise((resolve) => {
        let calcRange = Common.ChartJS.setComputedDate(
          selectionMin,
          selectionMax,
          this.timeScale
        );
        this.isLoading = true;

        let url =
          `/api/analysis/mld-cht` +
          `?dataGroup=${this.dataGroup}` +
          `&moldId=${this.mold.id}` +
          `&fromDate=${calcRange[0]}` +
          `&toDate=${calcRange[1]}` +
          `&timeScale=${this.timeScale}`;

        console.log("url: ", url);

        axios
          .get(url)
          .then((res) => {
            console.log("res: ", res);
            if (res.data.content.length > 0) {
              this.defaultData = res.data.content;
              this.isZoomPanEventAvailable = true;
              this.isLoading = false;
              resolve(this.defaultData);
            } else {
              alert("데이터가 존재하지 않습니다.");
              this.isZoomPanEventAvailable = true;
              this.isLoading = false;
            }
          })
          .catch((err) => {
            alert(err);
            this.isZoomPanEventAvailable = true;
            this.isLoading = false;
          });
      });
    },

    // overall chart에 넘겨줄 data를 가져옵니다.
    getOverallChartData() {
      if (this.availableTimeScales.includes("MONTHLY")) {
        this.overallTimeScale = "MONTHLY";
      } else {
        alert(
          "overall chart를 설정할 수 없습니다. (timescale MONTHLY를 사용할 수 없습니다)"
        );
      }

      let overallUrl =
        `/api/analysis/mld-cht` +
        `?dataGroup=${this.dataGroup}` + // 이게 메인 overall 이 되면 되지 않을까?
        `&moldId=${this.mold.id}` +
        `&fromDate=${this.overallFromDate}` +
        `&toDate=${this.overallToDate}` +
        `&timeScale=${this.overallTimeScale}`;

      axios.get(overallUrl).then((res) => {
        this.overallData = res.data.content;
      });
    },

    // 사용가능한 timeScale이 무엇인지에 대한 데이터를 가져옵니다.
    getAvailableTimeScales() {
      return new Promise((resolve) => {
        return axios
          .get(
            `/api/analysis/mld-cht/options` +
              `?dataGroup=${this.dataGroup}` +
              `&moldId=${this.mold.id}`
          )
          .then((res) => {
            this.availableTimeScales = res.data.availableTimeScales;
            resolve(res);
          });
      });
    },
  },
};
</script>
<style scoped>
.loading-container {
  width: 100%;
  height: 100%;
  position: absolute;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}
.t-icon-close {
  width: 12px;
  height: 12px;
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.info-zone {
  margin-top: 25px;
  margin-bottom: 25px;
  font-size: 16px;
  display: flex;
}

.info-zone .info-zone_tooling span {
  background: #fbfcfd;
  width: 199px;
  height: 24px;
  padding: 4px 104px 2px 15px;
}

.info-zone .info-zone_tooling b {
  color: #4b4b4b;
  margin-right: 6px;
}

.info-zone .info-zone_tooling {
  margin-right: 43px;
}
</style>
