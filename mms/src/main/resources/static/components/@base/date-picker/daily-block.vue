<template>
  <div class="date-block" :key="isRange">
    <div v-if="!isRange" class="daily-container">
      <v-date-picker
        v-model="singleDate"
        locale="us-US"
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :available-dates="{
          start: dateRange.minDate,
          end: dateRange.maxDate,
        }"
        @dayclick="changeDate"
      ></v-date-picker>
    </div>
    <div v-else class="date-range daily-container">
      <v-date-picker
        v-model="range"
        locale="us-US"
        is-range
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :available-dates="{
          start: dateRange.minDate,
          end: dateRange.maxDate,
        }"
        @dayclick="chooseRange"
      ></v-date-picker>
    </div>
    <div v-if="showFooter" class="block-footer">
      <span>*Click on the number in the calendar to choose the date.</span>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    isRange: {
      type: Boolean,
      default: false,
    },
    currentDate: {
      type: Object,
      default: () => ({}),
    },
    dateRange: {
      type: Object,
      default: () => ({
        minDate: null,
        maxDate: new Date(),
      }),
    },
    frequency: {
      type: String,
      default: "",
    },
    showFooter: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      date: { ...this.currentDate },
      singleDate: this.currentDate.from,
      range: {
        start: this.currentDate.from,
        end: this.currentDate.to,
      },
      dateTitle: {
        start: this.currentDate.fromTitle,
        end: this.currentDate.toTitle,
      },
    };
  },
  created() {
    this.today = new Date();
    console.log("init daily-block", this.dateRange);
  },
  watch: {
    frequency(newValue) {
      if (newValue.includes("DAILY")) {
        this.clearPickingData();
      }
    },
    isRange(newVal) {
      console.log("watch range", newVal);
    },
  },
  methods: {
    clearPickingData() {
      // this.singleDate = new Date();
      // this.range = {
      //   start: new Date(),
      //   end: new Date(),
      // };
      // this.dateTitle = {
      //   start: moment(new Date()).format("YYYY-MM-DD"),
      //   end: moment(new Date()).format("YYYY-MM-DD"),
      // };
      this.singleDate = null;
      this.range = {
        start: null,
        end: null,
      };
      this.dateTitle = {
        start: "",
        end: "",
      };
      this.changeDate();
    },
    setDefault(date) {
      console.log("setDefault", this.isRange, date);
      if (this.isRange) {
        this.range = {
          start: date.from,
          end: date.to,
        };
      } else {
        this.singleDate = date.from;
      }
      this.dateTitle = {
        start: date.fromTitle,
        end: date.toTitle,
      };
    },
    getFrequency() {
      return this.isRange ? "DAILY_RANGE" : "DAILY";
    },
    changeDate() {
      let selectRange = this.getDateRange();
      let title = {
        start: selectRange.start
          ? moment(selectRange.start).format("YYYY-MM-DD")
          : "",
        end: selectRange.end
          ? moment(selectRange.end).format("YYYY-MM-DD")
          : "",
      };
      this.$emit("change-date", selectRange, title);
    },
    getDateRange() {
      let startDate = null;
      let endDate = null;
      if (!this.isRange) {
        startDate = this.singleDate;
        endDate = this.singleDate;
      } else {
        startDate = this.range.start;
        endDate = this.range.end;
      }
      return {
        start: startDate,
        end: endDate,
      };
    },
    changeRange(newRange) {
      this.range = {
        start: moment(newRange.start, "YYYY-MM-DD").toDate(),
        end: moment(newRange.end, "YYYY-MM-DD").toDate(),
      };
    },
    chooseRange() {
      if (this.range.start && this.range.end) {
        this.changeDate();
      }
    },
  },
};
</script>

<style scoped>
.block-footer {
  font-size: 11px;
  color: #838383;
  position: unset;
}
/*override into library style*/
.vc-pane {
  font-family: Helvetica Neue !important;
}

.vc-header {
  padding-top: 0px !important;
  height: 20px !important;
}

.vc-title {
  font-size: 13px !important;
  line-height: 28px !important;
  text-align: center !important;
  color: #696969 !important;
  font-weight: normal !important;
}

.vc-svg-icon {
  height: 20px;
}

.vc-arrow {
  height: 20px;
}

.vc-weekday {
  color: #4b4b4b !important;
  font-size: 12px;
  font-weight: bold !important;
}

.vc-day-content {
  color: #4b4b4b;
  font-family: Helvetica Neue, Regular !important;
  font-size: 13px !important;
  font-weight: 400 !important;
  padding-bottom: 1px;
}

.vc-arrows-container {
  padding: 0px;
}

.vc-highlight {
  width: 20px !important;
  height: 20px !important;
  background-color: #519ffc !important;
}

.today-range {
  color: #2aa3ff !important;
}

.vc-day {
  min-height: 28px !important;
}

.vc-day-content.is-disabled {
  color: #d5d5d5;
  pointer-events: none;
}

.vc-day-content:hover {
  background-color: lightgray !important;
}

.vc-highlight.vc-highlight-base-middle,
.vc-highlight.vc-highlight-base-end,
.vc-highlight.vc-highlight-base-start {
  height: 15px !important;
  width: 35px !important;
  background-color: #519ffc66 !important;
}

.vc-day-content:focus {
  background: none !important;
  /* color: white !important; */
}
.date-range .vc-day-content:focus {
  color: white !important;
}
.vc-container.vc-blue {
  border: none;
}
.vc-container.vc-blue {
  border: none;
}
/*end override library style */
</style>
