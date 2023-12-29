<template>
  <div class="date-block">
    <div v-if="!isRange">
      <v-date-picker
        v-model="singleDate"
        locale="us-US"
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :max-date="today"
        @dayclick="changeDate"
      ></v-date-picker>
    </div>
    <div v-else class="date-range">
      <v-date-picker
        v-model="range"
        locale="us-US"
        is-range
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :max-date="today"
        :available-dates="{
          start: dateRange.minDate,
          end: dateRange.maxDate,
        }"
        @dayclick="chooseRange"
      ></v-date-picker>
    </div>
    <div class="block-footer">
      <span>*Click on the number in the calendar to choose the date.</span>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    isRange: {
      type: Boolean,
      default: () => false,
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
      default: () => "",
    },
  },
  data() {
    return {
      date: this.currentDate,
      singleDate: null,
      range: {
        start: null,
        end: null,
      },
      dateTitle: {
        start: "",
        end: "",
      },
    };
  },
  created() {
    this.today = new Date();
  },
  watch: {
    range() {},
    frequency(newValue) {
      if (newValue.includes("DAILY")) {
        this.clearPickingData();
      }
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
      console.log('setDefault', date)
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
</style>