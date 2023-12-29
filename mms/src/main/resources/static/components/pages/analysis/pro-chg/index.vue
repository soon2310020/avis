<script>
module.exports = {
  name: "process-change-page",
  components: {
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      filterCode: "COMMON",
      timeScale: "MONTH",
      fromDate: "",
      toDate: "",
      notifyCount: 0,
      dateRange: { start: null, end: null },
      tempDateRange: { start: null, end: null },
      tempTimeScale: "",
      isOpenedCalendarModal: false,
      calendarSelectorOptionList: ["WEEK", "MONTH", "QUARTER"],
      maxDate: moment().format("YYYYMMDD"),
      resetTrigger: 0,
    };
  },
  computed: {
    displayDate() {
      return Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
    },
    timeValue() {
      return Common.getDatePickerTimeValue(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
    },
    selectedYear() {
      return this.timeValue.slice(0, 4);
    },
  },
  watch: {
    dateRange: {
      handler(newVal, oldVal) {
        this.fromDate = newVal.start?.format("YYYYMMDD");
        this.toDate = newVal.end?.format("YYYYMMDD");

        if (
          newVal.start &&
          newVal.end &&
          (newVal.start !== oldVal.start || newVal.end !== oldVal.end)
        ) {
          this.notifyCount++;
        }
      },
      deep: true,
      immediate: true,
    },
  },
  methods: {
    async fetchData({ page, size, sort, month, day, moldId }) {
      const json = await axios.get(`/api/analysis/pro-chg`, {
        params: {
          filterCode: this.filterCode,
          page,
          size,
          sort,
          month,
          day,
          moldId,
          timeScale: this.timeScale,
          timeValue: this.timeValue,
          fromDate: this.fromDate,
          toDate: this.toDate,
        },
      });
      return json.data;
    },
    async fetchDetailData({ dateHourRange, moldId, page, size, sort }) {
      const json = await axios.get(`/api/analysis/pro-chg/details`, {
        params: {
          filterCode: this.filterCode,
          dateHourRange,
          moldId,
          page,
          size,
          sort,
        },
      });
      return json.data;
    },
    initDateRange() {
      const today = moment();
      const startOfMonth = today.clone().startOf("month");
      const endOfMonth = today.clone().endOf("month");

      this.dateRange = {
        start: startOfMonth,
        end: endOfMonth,
      };
    },
    openCalendarModal() {
      this.tempTimeScale = this.timeScale;
      this.tempDateRange = this.dateRange;
      this.isOpenedCalendarModal = true;
    },
    closeCalendarModal() {
      this.tempTimeScale = "";
      this.tempDateRange = { start: null, end: null };
      this.isOpenedCalendarModal = false;
    },
    getDateRange(dateRange) {
      this.tempDateRange = dateRange;
    },
    getTimeScale(timeScale) {
      this.tempTimeScale = timeScale;
    },
    saveSelectedDate() {
      this.dateRange = this.tempDateRange;
      this.timeScale = this.tempTimeScale;
      this.isOpenedCalendarModal = false;
    },
    convertToTitleCase(string) {
      if (typeof string !== "string" || string.length === 0) return;

      return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
    },
  },
  created() {
    this.initDateRange();
  },
};
</script>
<template>
  <div class="pc-main-container">
    <div class="pc-header-container">
      <div>
        <master-filter-wrapper
          :filter-code="filterCode"
          :notify-filter-updated="() => notifyCount++"
        ></master-filter-wrapper>
      </div>
      <div>
        <emdn-cta-button
          button-type="date-picker"
          color-type="blue"
          :click-handler="openCalendarModal"
          >{{ displayDate }}
        </emdn-cta-button>
        <emdn-tooltip>
          <template #context>
            <emdn-icon
              button-type="restore-icon"
              :click-handler="() => resetTrigger++"
            ></emdn-icon>
          </template>
          <template #body>
            <div style="white-space: nowrap">Reset</div>
          </template>
        </emdn-tooltip>
      </div>
    </div>
    <div style="height: 1.25rem"></div>
    <div v-if="fromDate && toDate" class="pc-content">
      <emdn-process-change-content
        :count="notifyCount"
        :selected-year="selectedYear"
        :time-scale="timeScale"
        :reset-trigger="resetTrigger"
        :get-process-changes="fetchData"
        :get-process-change-details="fetchDetailData"
      />
    </div>
    <div>
      <emdn-modal
        :is-opened="isOpenedCalendarModal"
        :heading-text="`Choose a ${convertToTitleCase(tempTimeScale)}`"
        :modal-handler="closeCalendarModal"
        style-props="width: fit-content; height: fit-content"
      >
        <template #body>
          <emdn-calendar
            :show-selector="true"
            :time-scale="tempTimeScale"
            :date-range="tempDateRange"
            :selector-options="calendarSelectorOptionList"
            @get-date-range="getDateRange"
            @get-time-scale="getTimeScale"
          ></emdn-calendar>
        </template>
        <template #footer>
          <cta-button color-type="white" :click-handler="closeCalendarModal"
            >Cancel
          </cta-button>
          <cta-button color-type="blue-fill" :click-handler="saveSelectedDate"
            >Save
          </cta-button>
        </template>
      </emdn-modal>
    </div>
  </div>
</template>

<style scoped>
.pc-main-container {
  padding: 2rem 1.25rem;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pc-header-container {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.pc-header-container > div:last-child {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pc-content {
  flex-grow: 1;
  overflow: hidden;
}
</style>
