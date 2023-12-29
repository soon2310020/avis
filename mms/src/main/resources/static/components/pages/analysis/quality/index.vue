<template>
  <div class="quality-container">
    <div class="d-flex justify-space-between" style="margin-bottom: 10px">
      <master-filter-wrapper
        :filter-code="filterCode"
        :notify-filter-updated="filterRefetch"
      ></master-filter-wrapper>
      <div>
        <!-- <customization-modal :all-columns-list="allColumnsList" @save="handleSaveColConfig" :resources="resources"></customization-modal> -->
        <emdn-cta-button
          button-type="date-picker"
          color-type="blue-fill"
          :click-handler="handleShowCalendar"
        >
          {{ displayedDateTime }}
        </emdn-cta-button>
      </div>
    </div>
    <!--  HEADER  -->
    <div class="d-flex align-center">
      <emdn-tab-button
        tab-type="secondary-tab"
        tab-style="horizontal"
        :tab-buttons="tabButtons"
        :click-handler="handleClickTab"
      >
      </emdn-tab-button>
    </div>
    <!-- CYCLE TIME DEVIATION -->
    <cyc-tim-dev
      v-if="currentHash === 'cyc-tim-dev'"
      :resources="resources"
      :data="listCycleTimeDeviation"
      :filter="filter"
      :all-columns-list="allColumnsList"
      :pagination="paginationCycleTimeDeviation"
      :is-loading="isLoadingCycleTimeDeviation"
      @sort="(value) => handleSort(value)"
      @click-supplier="handleClickSupplier"
      @click-tooling="handleClickTooling"
    ></cyc-tim-dev>
    <!-- CYCLE TIME FLUCTUATION -->
    <cyc-tim-flu
      v-if="currentHash === 'cyc-tim-flu'"
      :resources="resources"
      :data="listCycleTimeFluctuation"
      :filter="filter"
      :all-columns-list="allColumnsList"
      :pagination="paginationCycleTimeFluctuation"
      :is-loading="isLoadingCycleTimeFluctuation"
      @sort="(value) => handleSort(value)"
      @click-supplier="handleClickSupplier"
      @click-tooling="handleClickTooling"
    ></cyc-tim-flu>
    <!-- MODAL -->
    <deviation-detail
      :resources="resources"
      :visible="showCycleTimeDeviationDetail"
      :filter="filter"
      :record="selectedCycleTimeDeviation"
      :duration="timeFilter.dateRange"
      :time-scale="timeFilter.timeScale"
      :fetch-method="fetchDetailAnalysisCycleTimeDeviation"
      @show-tooling="handleShowToolingDetail"
      @close="handleCloseCycleTime('DEVIATION')"
    >
    </deviation-detail>
    <fluctuation-detail
      :resources="resources"
      :visible="showCycleTimeFluctuationDetail"
      :filter="filter"
      :record="selectedCycleTimeFluctuation"
      :duration="timeFilter.dateRange"
      :fetch-method="fetchDetailAnalysisCycleTimeFluctuation"
      @show-tooling="handleShowToolingDetail"
      @close="handleCloseCycleTime('FLUCTUATION')"
    ></fluctuation-detail>
    <company-details
      ref="company-dialog"
      :resources="resources"
    ></company-details>
    <chart-mold-modal
      ref="mold-dialog"
      :resources="resources"
      style="z-index: 9999; position: fixed"
    ></chart-mold-modal>
    <!-- DATE PICKER -->
    <emdn-modal
      v-if="showCalendar"
      :is-opened="showCalendar"
      :heading-text="calendarTitle"
      :modal-handler="handleCancelCalendar"
      style-props="width: fit-content; height: fit-content;"
    >
      <template #body>
        <emdn-calendar
          :show-selector="true"
          :max-date="calendarConfig.maxDate"
          :selector-options="calendarConfig.options"
          :time-scale="timeFilterTemp.timeScale"
          :date-range="timeFilter.dateRange"
          @get-date-range="handleGetDateRange"
          @get-time-scale="handleGetTimeScale"
        ></emdn-calendar>
      </template>
      <template #footer>
        <emdn-cta-button
          color-type="white"
          :click-handler="handleCancelCalendar"
        >
          {{ resources["cancel"] }}
        </emdn-cta-button>
        <emdn-cta-button
          color-type="blue-fill"
          :click-handler="handleSaveCalendar"
        >
          {{ resources["save"] }}
        </emdn-cta-button>
      </template>
    </emdn-modal>
  </div>
</template>

<script>
const TAB_CONSTANT = {
  CYCLE_TIME_DEVIATION: {
    LABEL: "Cycle Time Deviation",
    HASH: "cyc-tim-dev",
    PAGE_TYPE: "CYCLE_TIME_DEV",
  },
  CYCLE_TIME_FLUCTUATION: {
    LABEL: "Cycle Time Fluctuation",
    HASH: "cyc-tim-flu",
    PAGE_TYPE: "CYCLE_TIME_FLUCTUATION",
  },
};

const CALENDAR = {
  DAY: {
    TIME_SCALE: "DATE",
    FORMAT: "YYYYMMDD",
    TITLE: "a Day",
  },
  WEEK: {
    TIME_SCALE: "WEEK",
    FORMAT: "YYYYWW",
    TITLE: "a Week",
  },
  MONTH: {
    TIME_SCALE: "MONTH",
    FORMAT: "YYYYMM",
    TITLE: "a Month",
  },
  YEAR: {
    TIME_SCALE: "YEAR",
    FORMAT: "YYYY",
    TITLE: "a Year",
  },
  CUSTOM: {
    TIME_SCALE: "CUSTOM",
    FORMAT: "YYYYMMDD",
    TITLE: "a Time Range",
  },
};

const DROPDOWN_ID = {
  FILTER_SUPPLIER: "filter-supplier",
  FILTER_PART: "filter-part",
};

module.exports = {
  name: "quality-page",
  components: {
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
    "cyc-tim-dev": httpVueLoader(
      "/components/pages/analysis/quality/cyc-tim-dev.vue"
    ),
    "cyc-tim-flu": httpVueLoader(
      "/components/pages/analysis/quality/cyc-tim-flu.vue"
    ),
    "date-picker-button": httpVueLoader(
      "/components/@base/date-picker/date-picker-button.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "deviation-detail": httpVueLoader(
      "/components/pages/analysis/quality/deviation-detail/index.vue"
    ),
    "fluctuation-detail": httpVueLoader(
      "/components/pages/analysis/quality/fluctuation-detail/index.vue"
    ),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "chart-mold-modal": httpVueLoader(
      "/components/chart-mold/chart-mold-modal.vue"
    ),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
  },
  props: {
    resources: Object,
  },
  data() {
    return {
      currentHash: TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH,
      isModalOpened: false,
      tabButtons: [
        {
          title: TAB_CONSTANT.CYCLE_TIME_DEVIATION.LABEL,
          hash: TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH,
          active: true,
        },
        {
          title: TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.LABEL,
          hash: TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.HASH,
          active: false,
        },
      ],
      data: [],
      filter: {
        sort: "",
        //   supplierId: [],
        //   partId: "",
      },
      filterCode: "COMMON",
      // cycle time deviation
      listCycleTimeDeviation: [],
      paginationCycleTimeDeviation: {
        page: 1,
        size: 5,
        totalPages: 0,
      },
      // cycle time fluctuation
      listCycleTimeFluctuation: [],
      paginationCycleTimeFluctuation: {
        page: 1,
        size: 5,
        totalPages: 0,
      },
      // date picker
      showCalendar: false,
      calendarConfig: {
        maxDate: moment(new Date()),
        // maxDate: undefined,
        options: [
          CALENDAR.WEEK.TIME_SCALE,
          CALENDAR.MONTH.TIME_SCALE,
          CALENDAR.YEAR.TIME_SCALE,
          CALENDAR.CUSTOM.TIME_SCALE,
        ],
      },
      timeFilterTemp: {
        dateRange: {
          start: moment().subtract(7, "days").weekday(1),
          end: moment().subtract(7, "days").weekday(7),
        },
        timeScale: CALENDAR.WEEK.TIME_SCALE,
      },
      timeFilter: {
        dateRange: {
          start: moment().subtract(7, "days").weekday(1),
          end: moment().subtract(7, "days").weekday(7),
        },
        timeScale: CALENDAR.WEEK.TIME_SCALE,
      },

      // light list for dropdown
      DROPDOWN_ID,
      showListSuppliers: false,
      showListParts: false,
      listSuppliersLight: [],
      listSelectedSuppliers: [],
      listPartsLight: [],
      selectedPart: {},

      // modal
      showCycleTimeDeviationDetail: false,
      showCycleTimeFluctuationDetail: false,
      selectedCycleTimeDeviation: {
        selectedPart: "",
        supplierName: "",
        supplierId: "",
        listToolings: [],
        page: 1,
        totalPages: 1,
        size: 20,
      },
      selectedCycleTimeFluctuation: {
        selectedPart: "",
        supplierName: "",
        supplierId: "",
        listToolings: [],
        page: 1,
        totalPages: 1,
        size: 20,
      },

      // customize columns
      pageType: TAB_CONSTANT.CYCLE_TIME_DEVIATION.PAGE_TYPE,
      allColumnsList: [],

      allColumnsListDeviation: [
        {
          label: "Supplier",
          field: "supplierName",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
          textAlign: "left",
          hyperlink: true,
          maxLength: 20, // this field needed to support truncated
          onClick: (row, col) => this.handleClickSupplier(row, col),
        },
        {
          label: "No. of Toolings",
          field: "moldCount",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
          textAlign: "left",
          hyperlink: true,
          popup: true,
          formatter: (val) => {
            if (val > 1) return val + " toolings";
            if (val <= 1) return val + " tooling";
            return " - ";
          },
          onClick: (row, col) => this.handleClickTooling(row, col),
        },
        {
          label: "Proportion of Shots Below Tolerance",
          field: "belowTolerance",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 2,
          textAlign: "right",
          formatter: (val) => {
            if (val == null) return " - ";
            return val.toFixed(1) + "%";
          },
        },
        {
          label: "Proportion of Shots Above Tolerance",
          field: "aboveTolerance",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 2,
          textAlign: "right",
          formatter: (val) => {
            if (val == null) return " - ";
            return val.toFixed(1) + "%";
          },
        },
        {
          label: "Normalized CT Deviation",
          field: "nctd",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 3,
          textAlign: "right",
          tooltip: `Normalized CT Deviation is represented as an index. The lower the value, the higher the score in terms of optimal, consistent production.`,
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + "%";
          },
        },
        {
          label: "Trend",
          field: "nctdTrend",
          default: true,
          sortable: false,
          defaultSelected: true,
          defaultPosition: 4,
          textAlign: "right",
          trend: true,
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1).toString().replace("-", "") + "%";
          },
        },
      ],
      allColumnsListFluctuation: [
        {
          label: "Supplier",
          field: "supplierName",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
          textAlign: "left",
          hyperlink: true,
          maxLength: 20,
          onClick: (row, col) => this.handleClickSupplier(row, col),
        },
        {
          label: "No. of Toolings",
          field: "moldCount",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
          textAlign: "left",
          hyperlink: true,
          popup: true,
          formatter: (val) => {
            if (val > 1) return val + " toolings";
            if (val <= 1) return val + " tooling";
            return " - ";
          },
          onClick: (row, col) => this.handleClickTooling(row, col),
        },
        {
          label: "Normalized CT Fluctuation",
          field: "nctf",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
          textAlign: "right",
          tooltip: `Normalized CT Fluctuation is represented as an index. The lower the value, the higher the score in terms of optimal, consistent production`,
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + "%";
          },
        },
        {
          label: "Trend",
          field: "ctfTrend",
          default: true,
          sortable: false,
          defaultSelected: true,
          defaultPosition: 6,
          textAlign: "right",
          trend: true,
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1).toString().replace("-", "") + "%";
          },
        },
      ],
      isLoadingCycleTimeDeviation: false,
      isLoadingCycleTimeFluctuation: false,
    };
  },
  computed: {
    supplierLabelDropdown() {
      if (
        !this.listSelectedSuppliers.length ||
        this.listSelectedSuppliers.length === this.listSuppliersLight.length
      )
        return "All Suppliers";
      if (this.listSelectedSuppliers.length === 1)
        return this.listSelectedSuppliers[0].title;
      else return this.listSelectedSuppliers.length + " Suppliers";
    },
    partLabelDropdown() {
      if (!this.selectedPart?.title) return "All Parts";
      else return this.selectedPart.title;
    },
    displayedDateTime() {
      return Common.getDatePickerButtonDisplay(
        this.timeFilter.dateRange.start,
        this.timeFilter.dateRange.end,
        this.timeFilter.timeScale
      );
    },
    calendarTitle() {
      const TITLE = {
        WEEK: "Choose a week",
        MONTH: "Choose a month",
        YEAR: "Choose a year",
        CUSTOM: "Choose a range time",
      };
      return TITLE[this.timeFilterTemp.timeScale];
    },
  },
  methods: {
    // API
    // deviation
    async fetchListAnalysisCycleTimeDeviation(param = {}) {
      this.isLoadingCycleTimeDeviation = true;
      const dateTimeValue = this.getDateTimeValue();
      const queryParamsString = Common.param({
        filterCode: this.filterCode,
        page: this.paginationCycleTimeDeviation.page,
        size: this.paginationCycleTimeDeviation.size,
        // supplierId: this.filter.supplierId,
        // partId: this.filter.partId,
        sort: this.filter.sort,
        ...dateTimeValue,
        ...param,
      });
      console.log("@fetchListAnalysisCycleTimeDeviation");
      try {
        const response = await axios.get(
          "/api/analysis/cyc-tim-dev?" + queryParamsString
        );
        this.listCycleTimeDeviation = response.data.content;
        this.paginationCycleTimeDeviation.totalPages = response.data.totalPages;
        if (param?.page) this.paginationCycleTimeDeviation.page = param.page;
        console.log("response", response);
        this.isLoadingCycleTimeDeviation = false;
      } catch (error) {
        console.log(error);
      }
    },
    async fetchDetailAnalysisCycleTimeDeviation(param = {}) {
      const dateTimeValue = this.getDateTimeValue();
      const queryParamsString = Common.param({
        filterCode: this.filterCode,
        page: this.selectedCycleTimeDeviation.page,
        size: this.selectedCycleTimeDeviation.size,
        supplierId: this.selectedCycleTimeDeviation.supplierId,
        // partId: this.filter.partId,
        ...dateTimeValue,
        ...param,
      });
      try {
        const response = await axios.get(
          "/api/analysis/cyc-tim-dev/details?" + queryParamsString
        );
        this.selectedCycleTimeDeviation.selectedPart = this.selectedPart;
        this.selectedCycleTimeDeviation.totalPages = response.data.totalPages;
        this.selectedCycleTimeDeviation.page =
          param?.page ?? response.data.pageable.pageNumber;
        this.$set(
          this.selectedCycleTimeDeviation,
          "listToolings",
          response.data.content
        );
      } catch (error) {
        console.log(error);
      }
    },

    // fluctuation
    async fetchListAnalysisCycleTimeFluctuation(param = {}) {
      const dateTimeValue = this.getDateTimeValue();
      const queryParamsString = Common.param({
        filterCode: this.filterCode,
        page: this.paginationCycleTimeFluctuation.page,
        size: this.paginationCycleTimeFluctuation.size,
        sort: this.filter.sort,
        ...dateTimeValue,
        ...param,
      });
      console.log("@fetchListAnalysisCycleTimeFluctuation");
      try {
        const response = await axios.get(
          "/api/analysis/cyc-tim-flu?" + queryParamsString
        );
        this.listCycleTimeFluctuation = response?.data?.content;
        this.paginationCycleTimeFluctuation.totalPages =
          response.data.totalPages;
        if (param?.page) this.paginationCycleTimeFluctuation.page = param.page;
        console.log("response", response);
      } catch (error) {
        console.log(error);
      }
    },
    async fetchDetailAnalysisCycleTimeFluctuation(param = {}) {
      this.isLoadingCycleTimeFluctuation = true;
      const dateTimeValue = this.getDateTimeValue();
      const queryParamsString = Common.param({
        filterCode: this.filterCode,
        page: this.selectedCycleTimeFluctuation.page,
        size: this.selectedCycleTimeFluctuation.size,
        supplierId: this.selectedCycleTimeFluctuation.supplierId,
        // partId: this.filter.partId,
        ...dateTimeValue,
        ...param,
      });
      try {
        const response = await axios.get(
          "/api/analysis/cyc-tim-flu/details?" + queryParamsString
        );
        this.selectedCycleTimeFluctuation.selectedPart = this.selectedPart;
        this.selectedCycleTimeFluctuation.totalPages = response.data.totalPages;
        this.selectedCycleTimeFluctuation.page =
          param?.page ?? response.data.pageable.pageNumber;
        this.$set(
          this.selectedCycleTimeFluctuation,
          "listToolings",
          response.data.content
        );
        this.isLoadingCycleTimeFluctuation = false;
      } catch (error) {
        console.log(error);
      }
    },

    // column
    // TODO: REOPEN WHEN READY
    // async fetchColumnConfig() {
    //   try {
    //     const response = await axios.get(`/api/config/column-config?pageType=${this.pageType}`)
    //     console.log('@getColumnConfig')
    //     if (!response?.data?.length) throw 'No data'
    //     const hashedByColumnName = {};
    //     response.data.forEach(item => {
    //       hashedByColumnName[item.columnName] = item;
    //     });
    //     this.allColumnsList.forEach(item => {
    //       if (hashedByColumnName[item.field]) {
    //         item.enabled = hashedByColumnName[item.field].enabled;
    //         item.id = hashedByColumnName[item.field].id
    //         item.position = hashedByColumnName[item.field].position
    //       }
    //     });
    //     this.allColumnsList.sort((a, b) => a.position - b.position);
    //     console.log('@fetchColumnConfig>this.allColumnsList', this.allColumnsList)
    //   } catch (error) {
    //     console.log(error)
    //   } finally {
    //     this.$forceUpdate()
    //   }
    // },
    async fetchDetailTooling(moldId) {
      return await axios.get(`/api/molds/${moldId}`);
    },

    // Routing
    // getHash(itemUrl) {
    //   console.log(itemUrl, "아이템 URL");
    //   return itemUrl?.split("#")?.[1];
    // },
    captureQuery() {
      // const hash = itemUrl
      //   ? this.getHash(itemUrl)
      //   : this.getHash(window.location.hash);
      // console.log("hash", hash);
      // if (!hash) {
      //   // default case
      //   this.currentHash = this.tabButtons[0].hash;
      //   window.location.href =
      //     window.location.href.split("#")[0] + `#${this.currentHash}`;
      //   window.location.hash = `#${this.currentHash}`;
      // } else {
      //   const found = this.tabButtons.findIndex((i) => i.hash === hash);
      //   if (found === -1) {
      //     // back to defeault route
      //     this.changeTab();
      //     return;
      //   }
      //   this.currentHash = hash;
      // }

      const [hash, params] = location.hash.slice(0, "#".length).split("?");
      if (hash) {
        const foundIndex = this.tabButtons.findIndex((i) => i.hash === hash);
        if (foundIndex > -1) {
          this.tabButtons = this.tabButtons.map((i) => ({
            ...i,
            active: i.hash === hash,
          }));
        } else {
          this.tabButtons = this.tabButtons.map((i, index) => ({
            ...i,
            active: index === 0,
          })); // default
        }
      }
    },
    handleClickTab(selectedTab) {
      console.log("@handleClickTab", selectedTab);
      this.currentHash = this.tabButtons.filter((item) => item.active)[0].hash;
    },

    // Dropdown
    // handleToggleDropdown(type) {
    //   this.showListSuppliers = false;
    //   this.showListParts = false;
    //   if (type === "SUPPLIER") this.showListSuppliers = true;
    //   if (type === "PART") this.showListParts = true;
    // },
    // handleSelectSupplier(listValue) {
    //   console.log("@handleSelectSupplier", listValue);
    //   this.listSelectedSuppliers = listValue.filter((i) => i.checked);
    //   this.filter.supplierId = this.listSelectedSuppliers.map((i) => i.id);
    // },
    // handleSelectPart(item) {
    //   console.log("@handleSelectPart", item);
    //   this.selectedPart = Object.assign(
    //     {},
    //     this.selectedPart,
    //     item.value ? item : { ...item, id: "" }
    //   );
    //   this.filter.partId = this.selectedPart?.id ?? "";
    //   this.showListParts = false;
    // },

    // Calendar handling
    handleChangeCalendar(rangeDate, timeScale) {
      this.timeFilterTemp = Object.assign({}, this.timeFilterTemp, {
        timeScale,
        fromDate: rangeDate.start,
        toDate: rangeDate.end,
      });
      console.log("@handleChangeCalendar", this.timeFilterTemp);
    },
    handleShowCalendar() {
      this.timeFilterTemp = Object.assign(
        {},
        this.timeFilterTemp,
        this.timeFilter
      );
      this.showCalendar = true;
    },
    handleSaveCalendar() {
      this.timeFilter = Object.assign({}, this.timeFilter, {
        ...this.timeFilterTemp,
      });
      this.showCalendar = false;
    },
    handleCancelCalendar() {
      this.timeFilterTemp = Object.assign({}, this.timeFilterTemp, {
        timeScale: "",
        fromDate: "",
        toDate: "",
      });
      this.showCalendar = false;
      console.log(
        "@handleCancelCalendar",
        this.timeFilterTemp,
        this.timeFilter
      );
    },
    initCalendar() {
      const currentWeek = moment().format("YYYYWW");
      const previousWeek = parseInt(currentWeek) - 1;
      this.timeFilter.fromDate = moment(previousWeek, CALENDAR.WEEK.FORMAT)
        .subtract(1, "days")
        .format(CALENDAR.DAY.FORMAT);
      this.timeFilter.toDate = moment(previousWeek, CALENDAR.WEEK.FORMAT)
        .add(5, "days")
        .format(CALENDAR.DAY.FORMAT);
    },
    getDateTimeValue() {
      const dateTimeValue = { timeScale: this.timeFilter.timeScale };
      const __timeValue = Common.getDatePickerTimeValue(
        this.timeFilter.dateRange.start,
        this.timeFilter.dateRange.end,
        this.timeFilter.timeScale
      );
      if (
        [
          CALENDAR.WEEK.TIME_SCALE,
          CALENDAR.MONTH.TIME_SCALE,
          CALENDAR.YEAR.TIME_SCALE,
        ].includes(dateTimeValue.timeScale)
      ) {
        dateTimeValue.timeValue = __timeValue;
      }
      if (dateTimeValue.timeScale === CALENDAR.CUSTOM.TIME_SCALE) {
        dateTimeValue.fromDate = __timeValue.slice(0, "YYYYMMDD".length);
        dateTimeValue.toDate = __timeValue.slice(-"YYYYMMDD".length);
      }
      return dateTimeValue;
    },
    handleGetDateRange(dateRange) {
      console.log("dateRange", dateRange);
      this.timeFilterTemp.dateRange = Object.assign(
        {},
        this.timeFilterTemp.dateRange,
        dateRange
      );
    },
    handleGetTimeScale(timeScale) {
      console.log("timeScale", timeScale);
      this.timeFilterTemp.timeScale = timeScale;
    },

    // Sort
    handleSort(sortValue) {
      this.filter.sort = `${sortValue.field},${
        sortValue.desc ? "desc" : "asc"
      }`;
      if (this.currentHash === "cyc-tim-dev") {
        this.fetchListAnalysisCycleTimeDeviation({ page: 1 });
      } else if (this.currentHash === "cyc-tim-flu") {
        this.fetchListAnalysisCycleTimeFluctuation({ page: 1 });
      }
    },

    // Col config
    async handleSaveColConfig(dataCustomize, list) {
      console.log("@handleSaveColConfig", dataCustomize.list);

      // data config for saving in DB
      const data = list.map((item, index) => {
        console.log("item", item);
        const response = {};
        response.columnName = item.field;
        response.enabled = item.enabled;
        response.position = index;
        if (item.id) response.id = item.id;
        return response;
      });

      try {
        // save config
        const response = await axios.post("/api/config/update-column-config", {
          pageType: this.pageType,
          columnConfig: data,
        });

        // re-assign value
        this.allColumnsList.forEach((col) => {
          response.data.forEach((item) => {
            if (item.columnName === col.field) {
              col.id = item.id;
              col.position = item.position;
            }
          });
        });

        // re-sort
        this.allColumnsList.sort((a, b) => a.position - b.position);
        console.log(
          "@handleSaveColConfig>this.allColumnsList",
          this.allColumnsList
        );
      } catch (error) {
        console.log(error);
      } finally {
        this.$forceUpdate();
      }
    },

    // Supplier Detail
    handleShowCycleTime(type) {
      if (type === "DEVIATION") this.showCycleTimeDeviationDetail = true;
      if (type === "FLUCTUATION") this.showCycleTimeFluctuationDetail = true;
    },

    handleCloseCycleTime(type) {
      if (type === "DEVIATION") this.showCycleTimeDeviationDetail = false;
      if (type === "FLUCTUATION") this.showCycleTimeFluctuationDetail = false;
    },

    // Mold Detail
    async handleShowToolingDetail(row, col) {
      const response = await this.fetchDetailTooling(row.moldId);
      console.log("@handleShowToolingDetail", response);
      const detailTooling = response.data;
      this.$refs["mold-dialog"].showChart(
        detailTooling,
        "QUANTITY",
        "DAY",
        "switch-graph"
      );
    },

    // Table Handler
    handleClickSupplier(row, col) {
      console.log(row, col);
      this.$refs["company-dialog"].showDetailsById(row.supplierId);
    },
    handleClickTooling(row, col) {
      console.log("@handleClickTooling", row, col);
      if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH) {
        this.showCycleTimeDeviationDetail = true;
        this.selectedCycleTimeDeviation.supplierId = row.supplierId;
        this.selectedCycleTimeDeviation.supplierName = row.supplierName;
        this.fetchDetailAnalysisCycleTimeDeviation({ page: 1 });
        this.handleShowCycleTime("DEVIATION");
      }
      if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.HASH) {
        this.showCycleTimeFluctuationDetail = true;
        this.selectedCycleTimeFluctuation.supplierId = row.supplierId;
        this.selectedCycleTimeFluctuation.supplierName = row.supplierName;
        this.fetchDetailAnalysisCycleTimeFluctuation({ page: 1 });
        this.handleShowCycleTime("FLUCTUATION");
      }
    },

    // Filter
    filterRefetch() {
      console.log("filterRefetch");
      if (this.currentHash === "cyc-tim-dev") {
        this.fetchListAnalysisCycleTimeDeviation({ page: 1 });
      } else if (this.currentHash === "cyc-tim-flu") {
        this.fetchListAnalysisCycleTimeFluctuation({ page: 1 });
      }
    },
  },
  created() {
    this.initCalendar();
    this.captureQuery();
  },
  mounted() {
    // this.fetchColumnConfig()
  },
  watch: {
    currentHash: {
      handler(newVal) {
        console.log("currentHash", newVal);
        this.tabButtons = this.tabButtons.map((item) => {
          if (item.hash === newVal) return { ...item, active: true };
          else return { ...item, active: false };
        });

        if (newVal === TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH) {
          this.pageType = TAB_CONSTANT.CYCLE_TIME_DEVIATION.PAGE_TYPE;
          this.allColumnsList = this.allColumnsListDeviation;
          this.fetchListAnalysisCycleTimeDeviation();
        }
        if (newVal === TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.HASH) {
          this.pageType = TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.PAGE_TYPE;
          this.allColumnsList = this.allColumnsListFluctuation;
          this.fetchListAnalysisCycleTimeFluctuation();
        }

        window.location.hash = `#${newVal}`;
        // this.fetchColumnConfig()
      },
      immediate: true,
    },
    timeFilter(newVal) {
      console.log("@watch:timeFilter", newVal);
      if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH) {
        this.fetchListAnalysisCycleTimeDeviation({ page: 1 });
      }
      if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.HASH) {
        this.fetchListAnalysisCycleTimeFluctuation({ page: 1 });
      }
    },
    // filter: {
    //   handler(newVal) {
    //     console.log("@filter", newVal);
    //     if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_DEVIATION.HASH) {
    //       this.fetchListAnalysisCycleTimeDeviation({ page: 1 });
    //     }
    //     if (this.currentHash === TAB_CONSTANT.CYCLE_TIME_FLUCTUATION.HASH) {
    //       this.fetchListAnalysisCycleTimeFluctuation({ page: 1 });
    //     }
    //   },
    //   deep: true,
    // },
    "paginationCycleTimeDeviation.page": {
      handler(newVal) {
        console.log("paginationCycleTimeDeviation.page", newVal);
        this.fetchListAnalysisCycleTimeDeviation({ page: newVal });
      },
      deep: true,
    },
    "paginationCycleTimeFluctuation.page": {
      handler(newVal) {
        console.log("paginationCycleTimeFluctuation.page", newVal);
        this.fetchListAnalysisCycleTimeFluctuation({ page: newVal });
      },
      deep: true,
    },
  },
};
</script>

<style scoped>
.quality-container {
  padding-bottom: 34px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 130px);
}

.quality-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 19px;
}

.quality-header > :last-child {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quality-filter-wrapper {
  width: 100%;
  height: auto;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 26px;
}

.quality-filter-wrapper > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item {
  position: relative;
}

.filter-label {
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
}
</style>
