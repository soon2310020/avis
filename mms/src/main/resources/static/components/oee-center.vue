<template>
  <div class="content-container">
    <div>
      <div class="title-row">
        <oee-center-filter
          ref="filter"
          :resources="resources"
          :current-oee-show="currentOeeShow"
          @change="handleChangeFilter"
          @load-shift="handleLoadShift"
        ></oee-center-filter>

        <!--        <div class="navigate-block">-->
        <!--          <div-->
        <!--            class="matching-button"-->
        <!--            @click="changeOeeShow(OEE_SHOW.MACHINE_TOOLING_MATCHES)"-->
        <!--          >-->
        <!--            <span class="matching-icon"></span>-->
        <!--          </div>-->
        <!--        </div>-->
      </div>
      <div v-if="currentOeeShow === OEE_SHOW.DASHBOARD" class="dashboard-zone">
        <div class="mini-card-row">
          <div
            v-if="
              resources &&
              Object.entries(resources).length &&
              permissions[permissionCode.OEE_CENTER_NUMBER_OF_MACHINES]
            "
            class="style-1"
            :class="{
              'disable-click':
                permissions[permissionCode.OEE_CENTER_NUMBER_OF_MACHINES]
                  .items == null ||
                !permissions[permissionCode.OEE_CENTER_NUMBER_OF_MACHINES].items
                  .lnkMachine.permitted,
            }"
            @click="navigateToOverviewMachine"
          >
            <div class="mini-card-container">
              <div class="content-box">
                <img
                  width="45"
                  src="/images/icon/number-of-machine.svg"
                  alt=""
                />
              </div>
              <div class="description-box">
                <p><span v-text="resources['number_of_machines']"></span></p>
                <p>
                  <strong>{{ oeeOverview.machineCount }}</strong>
                  {{ oeeOverview.machineCount > 1 ? "Machines" : "Machine" }}
                </p>
              </div>
            </div>
          </div>
          <chart-card
            v-if="
              resources &&
              Object.entries(resources).length &&
              permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME]
            "
            :class="{
              'disable-click':
                permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME].items ==
                  null ||
                !permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME].items
                  .lnkMachineDowntime.permitted,
            }"
            :label="resources['machine_downtime']"
            :oee-show="OEE_SHOW"
            :get-trend-chart-data="getTrendChartData"
            :change-oee-show="changeOeeShow"
            :oee-overview="oeeOverview"
            :percent="oeeOverview.machineDowntimePercentage"
            :value="oeeOverview.machineDowntimeValue"
            :value-unit="
              (oeeOverview.machineDowntimeValue || 0).toString().includes('.')
                ? ''
                : 'Hour'
            "
            :percent-formatted="machineDowntimePercentageFormatted"
            :value-formatted="machineDowntimeValueFormatted"
            :status="true"
            @click="changeOeeShow(OEE_SHOW.MACHINE_DOWNTIME)"
            :resources="resources"
          ></chart-card>
          <chart-card
            v-if="
              resources &&
              Object.entries(resources).length &&
              permissions[permissionCode.OEE_CENTER_PARTS_PRODUCED]
            "
            :class="{
              'disable-click':
                permissions[permissionCode.OEE_CENTER_PARTS_PRODUCED].items ==
                  null ||
                !permissions[permissionCode.OEE_CENTER_PARTS_PRODUCED].items
                  .lnkPartsProduced.permitted,
            }"
            :label="resources['parts_produced']"
            :oee-show="OEE_SHOW"
            :get-trend-chart-data="getTrendChartData"
            :change-oee-show="changeOeeShow"
            :oee-overview="oeeOverview"
            :status="true"
            :percent="oeeOverview.partsProducedPercentage"
            :value="oeeOverview.partsProducedValue"
            :percent-formatted="partsProducedPercentageFomatted"
            :value-formatted="partsProducedValueFormatted"
            @click="changeOeeShow(OEE_SHOW.PARTS_PRODUCED)"
            :resources="resources"
          ></chart-card>
          <chart-card
            v-if="
              resources &&
              Object.entries(resources).length &&
              permissions[permissionCode.OEE_CENTER_REJECT_RATE]
            "
            :class="{
              'disable-click':
                permissions[permissionCode.OEE_CENTER_REJECT_RATE].items ==
                  null ||
                !permissions[permissionCode.OEE_CENTER_REJECT_RATE].items
                  .lnkRejectRate.permitted,
            }"
            :label="resources['reject_rate']"
            :get-trend-chart-data="getTrendChartData"
            :oee-overview="oeeOverview"
            :oee-show="OEE_SHOW"
            :change-oee-show="changeOeeShow"
            :status="true"
            :percent="oeeOverview.rejectRatePercentage"
            :value="oeeOverview.rejectRateValue"
            :percent-formatted="rejectRatePercentageFormatted"
            :value-formatted="rejectRateValueFormatted"
            @click="changeOeeShow(OEE_SHOW.REJECT_RATE)"
            :resources="resources"
          ></chart-card>
        </div>
        <div class="big-card-row">
          <oee-progress-card
            v-if="permissions[permissionCode.OEE_CENTER_OEE]"
            :progress-list="progressList"
            :percent-color="percentColor"
            :resources="resources"
          >
          </oee-progress-card>
          <machine-downtime-alert-feed
            v-if="
              permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME_EVENTS]
            "
            :key="machineDowntimeKey"
            :resources="resources"
            :list-data="listFeeds"
            :pagination="machineDowntimeAlertPagination"
            :permissions="permissions"
            :on-click-reason="handleShowDowntimeReason"
            @paginate="(page) => fetchDowntimeEvents({ page })"
          >
          </machine-downtime-alert-feed>
        </div>
      </div>
      <div
        v-if="currentOeeShow !== OEE_SHOW.DASHBOARD"
        class="back-to-dashboard"
        style="width: fit-content"
        @click="changeOeeShow(OEE_SHOW.DASHBOARD)"
      >
        <span class="icon back-arrow" style="margin-right: 8px"></span>
        <span><span v-text="resources['back_to_dashboard']"></span></span>
      </div>
      <div
        v-if="currentOeeShow === OEE_SHOW.MACHINE_DOWNTIME"
        style="margin-top: 1rem"
      >
        <machine-downtime-table
          :resources="resources"
          :data-list="listMachineDowntimes"
          :fetch-data="fetchListMachineDowntime"
          :pagination="machineDowntimePagination"
          :permissions="permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME]"
          :detail-permissions="
            permissions[permissionCode.OEE_CENTER_MACHINE_DOWNTIME_DETAIL]
          "
          @show-machine-details="(machineId) => showMachineDetails(machineId)"
          @show-mold-chart="showMoldChart"
          @show-downtime-reason="handleShowDowntimeReason"
        >
        </machine-downtime-table>
      </div>
      <div v-if="currentOeeShow === OEE_SHOW.PARTS_PRODUCED">
        <parts-produced
          ref="partsProducedRef"
          :resources="resources"
          :filter="filter"
          :fetch-data="fetchListPartsProduced"
          :list-displayed-shifts="listDisplayedShifts"
          :list-parts-produced="listPartsProduced"
          :get-risk-color="shiftColor"
          :pagination="partsProducedPagination"
          :permissions="permissions[permissionCode.OEE_CENTER_PARTS_PRODUCED]"
          :percent-color="percentColor"
          :raw-shifts="rawShifts"
          :detail-permissions="
            permissions[permissionCode.OEE_CENTER_PARTS_PRODUCED_DETAIL]
          "
          :color-indicator-types="colorIndicatorTypes"
          :selected-color-type="selectedColorType"
          :color-indicator-hidden="colorIndicatorHidden"
          @show-machine-details="(machineId) => showMachineDetails(machineId)"
          @change-color-config="handleSelectColorIndicator"
        ></parts-produced>
      </div>
      <div
        v-if="currentOeeShow === OEE_SHOW.REJECT_RATE"
        style="margin-top: 1rem"
      >
        <machine-reject-rate
          :resources="resources"
          :is-show="currentOeeShow === OEE_SHOW.REJECT_RATE"
          :list-data="listMachineRejectRate"
          :pagination="machineRejectRatePagination"
          :sort-type="rejectRateParam.sortType"
          :is-desc="rejectRateParam.isDesc"
          @show-mold-chart="showMoldChart"
          :permissions="permissions[permissionCode.OEE_CENTER_REJECT_RATE]"
          :detail-permissions="
            permissions[permissionCode.OEE_CENTER_REJECT_RATE_DETAIL]
          "
          @paginate="(page) => fetchListMachineRejectRate({ page: page })"
          @sort="handleSortRejectRate"
          @show-machine-details="(machineId) => showMachineDetails(machineId)"
          @show-entry-record="
            (entryRecord) => showEntryRecordModal(entryRecord)
          "
        ></machine-reject-rate>
      </div>
      <div
        v-if="currentOeeShow === OEE_SHOW.MACHINE_TOOLING_MATCHES"
        style="margin-top: 1rem"
      >
        <machine-tooling-match :resources="resources"></machine-tooling-match>
      </div>
    </div>
    <machine-details
      ref="machineDetails"
      :resources="resources"
    ></machine-details>
    <entry-record-dialog
      :visible="showEntryRecord"
      :resources="resources"
      :query="queryRecord"
      @close="showEntryRecord = false"
    ></entry-record-dialog>
    <chart-mold
      ref="chartMold"
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></chart-mold>
    <file-previewer :back="backToMoldDetail"></file-previewer>
    <register-machine-downtime
      ref="register-machine-downtime"
      :resources="resources"
      @after-submit="handleAfterSubmitDowntimeReason"
    ></register-machine-downtime>
  </div>
</template>

<script>
const CHART_TYPE = {
  OEE_CENTER: "OEE_CENTER",
  PART_PRODUCE: "PART_PRODUCE",
};

const TAB = {
  OEE: "OEE",
};

const FREQUENCY_OPTION = {
  DAILY: "day",
  WEEKLY: "week",
  MONTHLY: "month",
};

const OEE_SHOW = {
  DASHBOARD: "dashboard",
  MACHINE_DOWNTIME: "machineDowntime",
  PARTS_PRODUCED: "partsProduced",
  REJECT_RATE: "rejectRate",
  MACHINE_TOOLING_MATCHES: "machineToolingMatches",
};

const MOCK_RISK_LEVEL = {
  high: 50,
  medium: 70,
  low: 100,
};

const ascSort = (a, b) => {
  return a.shiftNumber > b.shiftNumber ? 1 : -1;
};

const minutes = 10;
const timeStep = minutes * 60 * 1000;

module.exports = {
  props: {
    resources: Object,
    permissions: {
      type: Object,
      default: () => ({}),
    },
  },
  components: {
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "parts-produced": httpVueLoader(
      "/components/oee-center/parts-produced/index.vue"
    ),
    "machine-downtime-table": httpVueLoader(
      "/components/oee-center/machine-downtime/index.vue"
    ),
    "machine-downtime-alert-feed": httpVueLoader(
      "./oee-center/machine-downtime-alert-feed/index.vue"
    ),
    "machine-reject-rate": httpVueLoader(
      "./oee-center/machine-reject-rate/index.vue"
    ),
    // "oee-dashboard-filter": httpVueLoader(
    //   "./oee-center/oee-center-filter/oee-dashboard-filter.vue"
    // ),
    // "oee-part-produced-filter": httpVueLoader(
    //   "./oee-center/oee-center-filter/oee-part-produced-filter.vue"
    // ),
    // "oee-machine-tooling-match-filter": httpVueLoader(
    //   "./oee-center/oee-center-filter/oee-machine-tooling-match-filter.vue"
    // ),
    "oee-center-filter": httpVueLoader(
      "./oee-center/oee-center-filter/index.vue"
    ),
    "oee-progress-card": httpVueLoader(
      "./oee-center/oee-progress/oee-progress-card.vue"
    ),
    "chart-card": httpVueLoader("./oee-center/chart-card/chart-card.vue"),
    "entry-record-dialog": httpVueLoader(
      "./oee-center/machine-reject-rate/entry-record-dialog.vue"
    ),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "register-machine-downtime": httpVueLoader(
      "/components/alert-center/machine-downtime/register-machine-downtime.vue"
    ),
    "machine-tooling-match": httpVueLoader(
      "./oee-center/machine-tooling-match/index.vue"
    ),
  },
  data() {
    return {
      permissionCode: Common.PERMISSION_CODE,
      progressList: [
        {
          key: "OEE",
          title: "OEE",
          type: "dashboard",
          percent: 0,
        },
        {
          key: "AVAILABILITY",
          title: this.resources["availability"],
          type: "line",
          percent: 0,
        },
        {
          key: "PERFORMANCE",
          title: this.resources["performance"],
          type: "line",
          percent: 0,
        },
        {
          key: "QUALITY",
          title: this.resources["quality"],
          type: "line",
          percent: 0,
        },
      ],

      // filter
      filter: {
        listLocations: [],
        listMachines: [],
        listShifts: [],
        currentDate: {
          from: new Date(),
          to: new Date(),
          fromTitle: moment(new Date()).format("YYYY-MM-DD"),
          toTitle: moment(new Date()).format("YYYY-MM-DD"),
        },
        frequency: "DAILY",
      },

      // dropdown state
      listLocations: [], // base
      listMachines: [], // base
      listShifts: [], // formData

      listPartsProduced: [],

      listFeeds: [],
      listMachineDowntimes: [],
      listMachineRejectRate: [],

      OEE_SHOW,
      oeeRejectRate: 50,
      oeeParts: -100,
      oeeDownTime: 30,

      riskPointHigh: 33,
      riskPointMedium: 66,
      riskPointLow: 80,

      percentOee: 0,
      percentAvailability: 30,
      percentPerformance: 60,
      percentQuality: 81,

      aboveChart: [
        { x: 0, y: 5 },
        { x: 10, y: 10 },
        { x: 20, y: 30 },
        { x: 30, y: 20 },
        { x: 40, y: 50 },
        { x: 50, y: 45 },
      ],
      belowChart: [
        { x: 0, y: 50 },
        { x: 10, y: 40 },
        { x: 20, y: 20 },
        { x: 30, y: 30 },
        { x: 40, y: 20 },
        { x: 50, y: 5 },
      ],
      oeeOverview: {
        machineCount: 0,
        machineDowntimePercentage: 0,
        machineDowntimeValue: 0,
        partsProducedPercentage: 0,
        partsProducedValue: 0,
        rejectRatePercentage: 0,
        rejectRateValue: 0,
        availability: 0,
        oee: 0,
        performance: 0,
        quality: 0,
      },
      currentOeeShow: OEE_SHOW.DASHBOARD,
      machineDowntimePagination: {
        pageNumber: 1,
        totalPages: 1,
      },
      machineRejectRatePagination: {
        pageNumber: 1,
        totalPages: 1,
      },
      partsProducedPagination: {
        pageNumber: 1,
        totalPages: 1,
      },
      machineDowntimeAlertPagination: {
        pageNumber: 1,
        totalPages: 1,
      },
      cancelRequestToken: "",
      cancelToken: undefined,
      defaultPartProducedDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      rejectRateParam: {
        sortType: "",
        isDesc: true,
        sortParam: "",
      },

      // Machine reject rate
      showEntryRecord: false,
      detailMachine: null,
      detailMachineGraph: null,
      queryRecord: {},

      fetchOeeStatisticInterval: null,
      machineDowntimeKey: 0,
      colorIndicatorTypes: [
        { key: "OEE", title: "Hourly OEE Score", checked: true },
        { key: "EHO", title: "Exp. Hourly Output", checked: false },
      ],
      selectedColorType: {
        key: "OEE",
        title: "Hourly OEE Score",
        checked: true,
      },
      rawShifts: [],
      riskLevels: {},
      isLoadingRisk: false,
      options: {},
      usePerformanceColorConfigForEHO: false,
      colorIndicatorHidden: false,
    };
  },
  computed: {
    machineDowntimeValueFormatted() {
      const valuesString = (this.oeeOverview?.machineDowntimeValue || 0)
        .toString()
        .split(".");
      console.log("valuesString", valuesString);
      if (valuesString?.length > 1) {
        const minutes = +("0." + valuesString[1]) * 60;
        console.log(
          "valuesString[0]+' h'+minutes+'m'",
          valuesString[0] + "h" + Number(minutes) + "m"
        );
        return valuesString[0] + " h " + Math.round(minutes) + " m";
      }
      return this.oeeOverview?.machineDowntimeValue;
      // return this.oeeOverview?.machineDowntimeValue?.toFixed(1) || 0
    },
    machineDowntimePercentageFormatted() {
      const percent = this.oeeOverview?.machineDowntimePercentage;
      return percent >= 0 ? `+${percent}` : `${percent}`;
    },
    rejectRateValueFormatted() {
      const _value = this.oeeOverview?.rejectRateValue?.toFixed(1) || "0.0";
      return _value + "%";
    },
    rejectRatePercentageFormatted() {
      const percent = this.oeeOverview?.rejectRatePercentage;
      return percent >= 0 ? `+${percent}` : `${percent}`;
    },
    partsProducedPercentageFomatted() {
      const percent = this.oeeOverview?.partsProducedPercentage;
      return percent >= 0 ? `+${percent}` : `${percent}`;
    },
    partsProducedValueFormatted() {
      return this.oeeOverview?.partsProducedValue || "0";
    },
    listDisplayedShifts() {
      console.log("get listDisplayedShifts", this.filter);
      let arr1 = [...this.listShifts.sort(ascSort)];
      arr1.map((item) => {
        let tempArr = item.name.split(" ");
        item.name = this.resources["shift"] + " " + tempArr[1];
      });
      if (this.filter?.listShifts?.length === 0)
        return [...this.listShifts.sort(ascSort)];
      return [...this.filter.listShifts.sort(ascSort)];
    },
  },
  methods: {
    async getAllRiskLevel() {
      try {
        this.isLoadingRisk = true;
        const res = await axios.get(`/api/production/oee-stp`);
        console.log("getAllRiskLevel", res);
        this.riskLevels = res.data;
        this.isLoadingRisk = false;
      } catch (error) {
        console.log(error);
      }
    },
    handleLoadShift(shifts) {
      this.rawShifts = shifts;
    },
    handleChangeFilter(filter) {
      console.log("handleChangeFilter", filter);
      this.filter = filter;
      if (
        [
          OEE_SHOW.DASHBOARD,
          OEE_SHOW.MACHINE_DOWNTIME,
          OEE_SHOW.REJECT_RATE,
        ].includes(this.currentOeeShow)
      ) {
        this.handleDashboardFilter();
      } else if (this.currentOeeShow === OEE_SHOW.PARTS_PRODUCED) {
        this.handlePartsProducedFilter();
      } else if (this.currentOeeShow === OEE_SHOW.MACHINE_TOOLING_MATCHES) {
      }
    },
    async changeOeeShow(oeeType) {
      if (oeeType === OEE_SHOW.REJECT_RATE) {
        // move into REJECT_RATE
        // const _selectedLocationIds = this.filter.listLocations.map((o) => o.id);
        // const selectedLocations = this.listDashboardLocations.filter((o) =>
        //   _selectedLocationIds?.includes(o.id)
        // );
        // if (selectedLocations?.length > 1 || selectedLocations?.length === 0) {
        //   const firstLocation = _.head(this.listDashboardLocations);
        //   this.filter.listLocations = [firstLocation];
        //   this.listDashboardLocations = this.listDashboardLocations.map((o) => ({
        //     ...o,
        //     checked: o.id === firstLocation.id,
        //   }));
        //   await this.fetchSaveDefaultOptions({ chartType: CHART_TYPE.OEE_CENTER });
        // }
      }
      if (
        oeeType !== OEE_SHOW.REJECT_RATE &&
        this.filter.frequent === "HOURLY"
      ) {
        // from REJECT_RATE
        // this.filter.frequent = "DAILY";
      }
      if (
        this.currentOeeShow === OEE_SHOW.PARTS_PRODUCED &&
        oeeType !== OEE_SHOW.PARTS_PRODUCED
      ) {
        // from PARTS_PRODUCED to another
        // const selectedLocationIds = this.filter.listLocations.map(
        //   (o) => o.id
        // );
        // this.listDashboardLocations = this.listPartsProducedLocations.map((item) => ({
        //   ...item,
        //   checked: selectedLocationIds.includes(item.id),
        // }));
        // this.filter.listLocations = this.filter.listLocations;
      }
      this.currentOeeShow = oeeType;
    },
    async getCurrentFrequency() {
      // let frequency = "HOURLY";
      // try {
      //   const res = await axios.get("/api/rejected-part/get-configuration");
      //   frequency = res.data.data.frequent;
      // } catch (error) {
      //   console.log(error);
      // }
      // return frequency;
    },
    getTrendChartData(trendNumber) {
      return [
        {
          data: [
            0,
            (trendNumber * 15) / 100,
            (trendNumber * 12) / 100,
            (trendNumber * 16) / 100,
            (trendNumber * 17) / 100,
            (trendNumber * 20) / 100,
          ],
          smooth: false,
          fill: true,
        },
      ];
    },
    setProgressList() {
      this.progressList[0].percent = this.oeeOverview.oee;
      this.progressList[1].percent = this.oeeOverview.availability;
      this.progressList[2].percent = this.oeeOverview.performance;
      this.progressList[3].percent = this.oeeOverview.quality;
    },
    percentColor(percent, riskType) {
      let riskPoints = { ...MOCK_RISK_LEVEL };
      if (!this.isLoadingRisk) {
        if (riskType === "OEE") {
          riskPoints = this.riskLevels.oeeScore;
        } else if (riskType === "AVAILABILITY") {
          riskPoints = this.riskLevels.oeeMetricAvailability;
        } else if (riskType === "PERFORMANCE") {
          riskPoints = this.riskLevels.oeeMetricPerformance;
        } else if (riskType === "QUALITY") {
          riskPoints = this.riskLevels.oeeMetricQuality;
        }
      }
      if (percent == null) {
        return "#D9D9D9";
      }
      if (percent <= riskPoints.high) {
        return "#E34537";
      }
      if (riskPoints.high < percent && percent <= riskPoints.medium) {
        return "#F7CC57";
      }
      if (riskPoints.medium < percent && percent < 100) {
        return "#41CE77";
      }
      if (100) {
        return "#41CE77";
      }
    },
    shiftColor(percent) {
      const riskPoints = this.usePerformanceColorConfigForEHO
        ? this.riskLevels.oeeMetricPerformance
        : this.riskLevels.oeeScore;
      if (percent == null) {
        return "oee-bg-null";
      }
      if (percent <= riskPoints.high) {
        return "oee-bg-high";
      }
      if (riskPoints.high < percent && percent <= riskPoints.medium) {
        return "oee-bg-medium";
      }
      if (riskPoints.medium < percent && percent < 100) {
        return "oee-bg-low";
      }
      if (100) {
        return "oee-bg-low";
      }
    },
    navigateToOverviewMachine() {
      const param = Common.param({
        id: this.filter.listMachines?.map((i) => i.id)?.toString(),
        locationIds: this.filter.listLocations?.map((i) => i.id)?.toString(),
      });
      try {
        Common.onChangeHref(`${Common.PAGE_URL.MACHINE}?${param}`);
      } catch (error) {
        console.log(error);
      }
    },
    // DASHBOARD HANDLER
    async handleDashboardFilter(filter) {
      if (this.currentOeeShow === OEE_SHOW.DASHBOARD) {
        this.fetchDowntimeEvents({ page: 1 });
        this.fetchOeeStatistic();
        clearInterval(this.fetchOeeStatisticInterval);
        this.fetchOeeStatisticInterval = setInterval(() => {
          this.fetchOeeStatistic({}, true);
          this.fetchDowntimeEvents({ page: 1 });
        }, timeStep);
      }
      if (this.currentOeeShow === OEE_SHOW.MACHINE_DOWNTIME) {
        this.fetchListMachineDowntime({ page: 1 });
      }
      if (this.currentOeeShow === OEE_SHOW.REJECT_RATE) {
        this.fetchListMachineRejectRate({ page: 1 });
      }
    },

    // PART PRODUCED HANDLER
    async handlePartsProducedFilter() {
      this.fetchListPartsProduced({ page: 1 });
    },

    // REJECT RATE HANDLER

    // MACHINE TOOLING HANDLER
    // handleToolingMachineMatchFilter(filter) { },

    // API LIST
    async fetchOeeStatistic(params, autoReload = false) {
      if (this.filter.frequency === "DAILY" && autoReload) {
        const startDate = Common.formatter.numberOnly(
          moment(this.filter.currentDate.from).format(Common.dateFns.dateFormat)
        );
        const today = new Date();
        const yesterday = today.setDate(today.getDate() - 1);
        if (+startDate === +moment(yesterday).format("YYYYMMDD")) {
          this.filter.currentDate.from = new Date();
          this.filter.currentDate.to = new Date();
        }
      }
      const queryParams = Common.param({
        start: Common.formatter.numberOnly(
          moment(this.filter.currentDate.from).format(Common.dateFns.dateFormat)
        ),
        end: Common.formatter.numberOnly(
          moment(this.filter.currentDate.to).format(Common.dateFns.dateFormat)
        ),
        locationIds: this.filter.listLocations
          .map((item) => item.id)
          .toString(),
        machineIds: this.filter.listMachines.map((item) => item.id).toString(),
        ...params,
      });
      try {
        const response = await axios.get(
          "/api/machines/statistics/oee/overview?" + queryParams
        );
        this.oeeOverview = {
          ...response?.data?.data,
          // rename fields
          partsProducedPercentage:
            response?.data?.data?.["partProducedPercentage"],
          partsProducedValue: response?.data?.data?.["partProducedValue"],
        };
        this.setProgressList();
      } catch (error) {
        console.log(error);
      }
    },
    async fetchListMachineDowntime(params) {
      const { numberOnly } = Common.formatter;
      const { dateFormat } = Common.dateFns;
      const { to, from } = this.filter.currentDate;
      const queryParams = Common.param({
        locationIdList: this.filter.listLocations.map((i) => i.id).toString(),
        machineIdList: this.filter.listMachines.map((i) => i.id)?.toString(),
        fromDate: numberOnly(moment(from).format(dateFormat)),
        toDate: numberOnly(moment(to).format(dateFormat)),
        sort: "startTime,desc",
        page: this.listMachineDowntimes.pageNumber,
        size: 10,
        ...params,
      });
      try {
        const response = await axios.get(
          "/api/machine-downtime-alert?" + queryParams
        );
        const data = response.data;
        this.machineDowntimePagination.totalPages = data.totalPages;
        this.machineDowntimePagination.pageNumber = params.page;
        const loadData = data.content;
        this.listMachineDowntimes = loadData.map((item) => {
          let newItem = {
            id: item.id,
            machineCode: item.machineCode,
            machineId: item.machineId,
            startTime: item.startTime,
            endTime: item.endTime,
            startTimeStr: item.startTimeStr,
            endTimeStr: item.endTimeStr,
            duration: item.duration,
            machineDowntimeReasonList: item.machineDowntimeReasonList,
            downtimeStatus: item.downtimeStatus,
            filteredDuration: item.filteredDuration,
            mold: item.mold,
          };
          if (typeof newItem.mold !== "object") {
            const find = loadData.find(
              (o) => o.mold && o.mold.id === newItem.mold
            );
            if (find) {
              newItem.mold = find.mold;
            }
          }
          return newItem;
        });
      } catch (error) {
        console.log(error);
      }
    },
    async fetchDowntimeEvents(params) {
      const locationIds = this.filter.listLocations
        .map((item) => item.id)
        ?.toString();
      const machineIds = this.filter.listMachines
        .map((item) => item.id)
        ?.toString();
      const queryParams = Common.param({
        machineIdList: machineIds,
        locationIdList: locationIds,
        fromDate: Common.formatter.numberOnly(
          moment(this.filter.currentDate.from).format(Common.dateFns.dateFormat)
        ),
        toDate: Common.formatter.numberOnly(
          moment(this.filter.currentDate.to).format(Common.dateFns.dateFormat)
        ),
        sort: "id,desc",
        page: 1,
        size: 10,
        tab: TAB.OEE,
        ...params,
      });
      try {
        const response = await axios.get(
          "/api/machine-downtime-alert?" + queryParams
        );
        this.machineDowntimeAlertPagination.pageNumber = params.page;
        this.machineDowntimeAlertPagination.totalPages =
          response?.data?.totalPages;
        this.listFeeds = response.data.content;
      } catch (error) {
        console.log(error);
      }
    },
    async fetchListMachines(params) {
      // const queryParams = Common.param({
      //   locationIds: params.locationIds || "",
      //   status: "enabled",
      //   sort: "machineCode,asc",
      //   page: 1,
      //   size: 10000,
      //   ...params,
      // });
      // try {
      //   const response = await axios.get("/api/machines?" + queryParams);
      //   return response?.data?.content;
      // } catch (error) {
      //   console.log(error);
      //   return false;
      // }
    },
    async fetchListLocations(params) {
      // const param = Common.param({
      //   status: "active",
      //   sort: "name,asc",
      //   page: 1,
      //   size: 10000,
      //   ...params,
      // });
      // if (this.listLocations.length) {
      //   // stop if already fetch
      //   return this.listLocations;
      // }
      // try {
      //   const response = await axios.get("/api/locations?" + param);
      //   this.listLocations = response?.data?.content;
      //   return this.listLocations;
      // } catch (error) {
      //   console.log(error);
      //   return false;
      // }
    },
    async fetchDefaultOptions({ chartType }) {
      // const queryParams = Common.param({ chartType });
      // try {
      //   const response = await axios.get(
      //     "/api/dashboard-chart-display-settings/get-by-chart-type?" + queryParams
      //   );
      //   const locationIdList =
      //     response?.data?.data?.dashboardSettingData?.locationIdList?.split(",") || [];
      //   const machineIdList =
      //     response?.data?.data?.dashboardSettingData?.machineIdList?.split(",") || [];
      //   return {
      //     locationIdList: locationIdList?.length > 0 ? [locationIdList[0]] : [],
      //     machineIdList,
      //   };
      // } catch (error) {
      //   console.log(error);
      //   return false;
      // }
    },
    async fetchSaveDefaultOptions({ chartType }) {
      // const payload = { chartType, dashboardSettingData: {} };
      // if (typeof this.cancelToken != typeof undefined) {
      //   this.cancelToken.cancel("new request");
      // }
      // this.cancelToken = axios.CancelToken.source();
      // if (chartType !== CHART_TYPE.PART_PRODUCE) {
      //   payload.dashboardSettingData.locationIdList = this.filter.listLocations
      //     .map((i) => i.id)
      //     .toString();
      //   payload.dashboardSettingData.machineIdList = this.filter.listMachines
      //     .map((i) => i.id)
      //     .toString();
      // } else if (this.filter.listLocations[0]) {
      //   const selectedLocations = [this.filter.listLocations[0]];
      //   payload.dashboardSettingData.locationIdList = selectedLocations
      //     .map((i) => i.id)
      //     .toString();
      //   payload.dashboardSettingData.machineIdList = this.filter.listMachines
      //     .map((i) => i.id)
      //     .toString();
      // }
      // // console.log('payload', payload)
      // try {
      //   await axios.post("/api/dashboard-chart-display-settings", payload, {
      //     cancelToken: this.cancelToken.token,
      //   });
      // } catch (error) {
      //   console.log(error);
      // }
    },
    async fetchListMachineRejectRate(params) {
      if (this.filter.frequency === "CUSTOM") {
        this.filter.frequency = "DAILY"; // back to default case for custom isn't supported
      }
      const frequency = this.filter.frequency;
      const queryParams = {
        page: params.page,
        frequent: frequency,
        locationIdList: this.filter.listLocations.map((i) => i.id)?.toString(),
        machineIdList: this.filter.listMachines.map((i) => i.id)?.toString(),
        size: 10,
        ...params,
      };
      if (frequency === "HOURLY") {
        console.log("this.filter.currentDate", this.filter.currentDate);
        queryParams.hour = `${Common.formatter.numberOnly(
          this.filter.currentDate.fromTitle
        )}${this.filter.currentDate.hour.from}`;
      } else {
        queryParams[FREQUENCY_OPTION[frequency]] = Common.formatter.numberOnly(
          this.filter.currentDate.fromTitle
        );
      }
      try {
        const response = await axios.get(
          "/api/rejected-part/get-reject-rate-oee?" + Common.param(queryParams)
        );
        this.listMachineRejectRate = response?.data?.content?.map((item) => ({
          ...item,
          checked: false,
        }));
        this.machineRejectRatePagination.totalPages = response.data.totalPages;
        this.machineRejectRatePagination.pageNumber = params.page;
      } catch (error) {
        console.log(error);
      }
    },
    async fetchListShifts(params) {
      // const queryParams = Common.param({
      //   locationId: this.filter.listLocations.map((i) => i.id).toString(),
      //   day: Common.formatter.numberOnly(this.filter.currentDate.fromTitle),
      //   ...params,
      // });
      // try {
      //   const response = await axios.get(
      //     "/api/shift-config/get-by-location-and-day?" + queryParams
      //   );
      //   this.listShifts = response.data.data.map((item) => ({
      //     ...item,
      //     title: item.name,
      //     checked: false,
      //     id: item.shiftNumber,
      //   }));
      // } catch (error) {
      //   console.log(error);
      // }
    },
    async fetchRiskLevel() {
      const response = await axios.get(
        "/api/machines/statistics/get-risk-level"
      );
      this.riskPointLow = response.data[0].percent;
      this.riskPointMedium = response.data[1].percent;
      this.riskPointHigh = response.data[2].percent;
    },
    async showMachineDetails(machineId) {
      try {
        this.$refs.machineDetails.showDetailsById(machineId);
      } catch (error) {
        console.log(error);
      }
    },
    closeMachineDetails() {
      this.detailMachine = null;
      this.$refs.machineDetails.dimissModal();
    },
    async showEntryRecordModal(machineId) {
      this.queryRecord = {
        page: 0,
        machineId,
        size: 10,
        day: Common.formatter.numberOnly(this.filter.currentDate.fromTitle),
      };
      this.showEntryRecord = true;
    },
    clostEntryRecord() {
      this.showEntryRecord = false;
    },
    async fetchListPartsProduced(params) {
      const page = params?.page || this.partsProducedPagination.pageNumber;
      const colorCode = this.selectedColorType.key;
      console.log("@fetchListPartsProduced", params);
      const queryParams = {
        machineIds: this.filter.listMachines.map((i) => i.id)?.toString(),
        locationIds: this.filter.listLocations.map((i) => i.id)?.toString(),
        shiftNumbers:
          this.filter.listShifts.length > 0
            ? this.filter.listShifts.map((i) => i.shiftNumber)?.toString()
            : this.rawShifts.map((i) => i.shiftNumber)?.toString(),
        start: Common.formatter.numberOnly(this.filter.currentDate.fromTitle),
        end: Common.formatter.numberOnly(this.filter.currentDate.toTitle),
        page,
        size: 10,
        colorCodeConfig: colorCode,
        ...params,
      };
      try {
        const response = await axios.get(
          `/api/machines/statistics/oee/part-produced?` +
            Common.param(queryParams)
        );
        this.listPartsProduced = response?.data?.data?.content;
        this.partsProducedPagination.pageNumber = page;
        this.partsProducedPagination.totalPages =
          response?.data?.data?.totalPages;
      } catch (error) {
        console.log(error);
      }
    },
    handleSortRejectRate(type) {
      if (this.rejectRateParam.sortType !== type) {
        this.rejectRateParam.sortType = type;
        this.rejectRateParam.isDesc = true;
      } else {
        this.rejectRateParam.isDesc = !this.rejectRateParam.isDesc;
      }
      if (type) {
        this.rejectRateParam.sortParam = `${type},${
          this.rejectRateParam.isDesc ? "desc" : "asc"
        }`;
        this.sortRejectRate();
      }
    },
    handleShowDowntimeReason(item, type) {
      const modal = this?.$refs["register-machine-downtime"];
      if (modal) modal.show(item, type);
    },
    sortRejectRate() {
      this.fetchListMachineRejectRate({
        page: 1,
        sort: this.rejectRateParam.sortParam,
      });
    },
    async showMoldChart(moldId) {
      try {
        const res = await axios.get(`/api/molds/${moldId}`);
        const mold = res.data;
        console.log("load mold", mold);
        const child = Common.vue.getChild(this.$children, "chart-mold");
        console.log("show mold chart", child);
        if (child != null) {
          child.showChart(mold, "QUANTITY", "DAY", "switch-graph");
        }
      } catch (error) {
        console.log(error);
      }
    },
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    backToMoldDetail() {
      console.log("back to molds detail");
      const child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.backFromPreview();
      }
    },
    async getColorConfig() {
      try {
        const res = await axios.get(
          `/api/dashboard-chart-display-settings/get-by-chart-type?chartType=PART_PRODUCED_COLOR_CODE_CONFIG`
        );
        console.log("getColorConfig", res);
        const colorCode = res.data.data.dashboardSettingData.colorCodeConfig;
        this.selectedColorType = this.colorIndicatorTypes.find(
          (item) => item.key === colorCode
        );
      } catch (error) {
        console.log(error);
      }
    },
    async updateColorConfig(item) {
      try {
        const param = {
          chartType: "PART_PRODUCED_COLOR_CODE_CONFIG",
          dashboardSettingData: {
            colorCodeConfig: item.key,
          },
        };
        const res = await axios.post(
          `/api/dashboard-chart-display-settings`,
          param
        );
        return res;
      } catch (error) {
        console.log(error);
      }
    },
    async handleSelectColorIndicator(item) {
      if (this.selectedColorType.key !== item.key) {
        this.selectedColorType = item;
        await this.updateColorConfig(item);
        await this.fetchListPartsProduced();
      }
    },
    handleFilter(filterObj) {
      if (this.currentOeeShow === OEE_SHOW.DASHBOARD) {
        console.log("api in DASHBOARD", filterObj);
      }
      if (this.currentOeeShow === OEE_SHOW.MACHINE_DOWNTIME) {
        console.log("api in MACHINE_DOWNTIME", filterObj);
      }
      if (this.currentOeeShow === OEE_SHOW.PARTS_PRODUCED) {
        console.log("api in PARTS_PRODUCED", filterObj);
      }
      if (this.currentOeeShow === OEE_SHOW.REJECT_RATE) {
        console.log("api in REJECT_RATE", filterObj);
      }
    },
    handleAfterSubmitDowntimeReason() {
      if (this.currentOeeShow === OEE_SHOW.DASHBOARD) {
        this.fetchDowntimeEvents({ page: 1 });
      }
      if (this.currentOeeShow === OEE_SHOW.MACHINE_DOWNTIME) {
        this.fetchListMachineDowntime({ page: 1 });
      }
    },
  },
  watch: {
    currentOeeShow: {
      async handler(newVal) {
        if (!this.breadcrumbEl) return;
        console.log("currentOeeShow", newVal);

        // OEE DASHBOARD
        if (newVal === OEE_SHOW.DASHBOARD) {
          this.breadcrumbEl.css("display", "none");
          this.fetchOeeStatistic();
          clearInterval(this.fetchOeeStatisticInterval);
          this.fetchOeeStatisticInterval = setInterval(() => {
            this.fetchOeeStatistic({}, true);
            this.fetchDowntimeEvents({ page: 1 });
          }, timeStep);
          this.fetchDowntimeEvents({ page: 1 });
          return;
        }

        // OTHERS OEE SCREEN
        this.breadcrumbEl.css("display", "block");

        // MACHINE DOWNTIME
        if (newVal === OEE_SHOW.MACHINE_DOWNTIME) {
          this.fetchListMachineDowntime({ page: 1 });
          this.breadcrumbEl.text("Machine Downtime");
        }

        // PARTS PRODUCED
        if (newVal === OEE_SHOW.PARTS_PRODUCED) {
          // TODO: NEED OPTIMIZE
          this.breadcrumbEl.text("Parts Produced");

          // const defaultOptions = await this.fetchDefaultOptions({
          //   chartType: CHART_TYPE.PART_PRODUCE,
          // });

          //base filter location array
          // const listLocationsResponse = await this.fetchListLocations();
          // const selectedLocations = (item) =>
          //   defaultOptions.locationIdList.some((id) => Number(id) === item.id);
          // this.listPartsProducedLocations = listLocationsResponse.map((item) => {
          //   if (selectedLocations(item))
          //     return { ...item, title: item.name, checked: true };
          //   return { ...item, title: item.name, checked: false };
          // });

          //selected filter location array
          // this.filter.listLocations = listLocationsResponse.filter(
          //   selectedLocations
          // );

          //base filter machine array
          // const locationIds = this.filter.listLocations
          //   .map((i) => i.id)
          //   ?.toString();
          // const listMachinesResponse = await this.fetchListMachines({ locationIds });
          // const selectedMachines = (item) =>
          //   defaultOptions.machineIdList.some((id) => Number(id) === item.id);
          // this.listPartsProducedMachines = listMachinesResponse.map((item) => {
          //   if (selectedMachines(item))
          //     return { ...item, title: item.machineCode, checked: true };
          //   return { ...item, title: item.machineCode, checked: false };
          // });
          // this.listDashboardMachines = listMachinesResponse.map((item) => {
          //   if (selectedMachines(item))
          //     return { ...item, title: item.machineCode, checked: true };
          //   return { ...item, title: item.machineCode, checked: false };
          // });

          //selected filter machine array
          await this.fetchListPartsProduced({ page: 1 });
        }

        // REJECT RATE
        if (newVal === OEE_SHOW.REJECT_RATE) {
          this.fetchListMachineRejectRate({ page: 1 });
          this.breadcrumbEl.text("Reject Rate");
        }

        // MACHINE TOOLING MATCH
        if (newVal === OEE_SHOW.MACHINE_TOOLING_MATCHES) {
          this.breadcrumbEl.text("Machine - Tooling Matches");

          // const defaultOptions = await this.fetchDefaultOptions({
          //   chartType: CHART_TYPE.PART_PRODUCE,
          // });

          //base filter location array
          // const listLocationsResponse = await this.fetchListLocations();
          // const selectedLocations = (item) =>
          //   defaultOptions.locationIdList.some((id) => Number(id) === item.id);
          // this.listMachineToolingMatchLocations = listLocationsResponse.map((item) => {
          //   if (selectedLocations(item))
          //     return { ...item, title: item.name, checked: true };
          //   return { ...item, title: item.name, checked: false };
          // });
        }
      },
      // immediate: true,
    },
    resources(newVal) {
      console.log("resources", newVal);
    },
    permissions(newVal) {
      console.log("permissions", newVal);
    },
  },
  async created() {
    await this.getColorConfig();
    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        this.options = Object.assign({}, this.options, newVal);
        this.usePerformanceColorConfigForEHO =
          newVal.CLIENT.oee.partProduced.usePerformanceColorConfigForEHO;
        this.colorIndicatorHidden =
          newVal.CLIENT.oee.partProduced.colorIndicatorHidden;
        if (this.colorIndicatorHidden) {
          this.selectedColorType = {
            key: "EHO",
            title: "Exp. Hourly Output",
            checked: false,
          };
          this.colorIndicatorTypes = [
            { key: "OEE", title: "Hourly OEE Score", checked: false },
            { key: "EHO", title: "Exp. Hourly Output", checked: true },
          ];
        }
        console.log(
          "options",
          this.options,
          this.colorIndicatorHidden,
          this.usePerformanceColorConfigForEHO
        );
      },
      { immediate: true }
    );
  },
  mounted() {
    this.$nextTick(async () => {
      this.getAllRiskLevel();
      this.percentColor();
      this.fetchRiskLevel();
      this.breadcrumbEl = $(".breadcrumb-item.op-nav-3");
    });
  },
};
</script>

<style src="/css/oee-center/oee-center.css" scoped></style>
<style scoped src="/components/capacity-utilization/chart.css"></style>
