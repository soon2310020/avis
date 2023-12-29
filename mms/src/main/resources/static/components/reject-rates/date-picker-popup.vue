<template>
  <a-popover
    placement="bottomRight"
    v-model="visible"
    trigger="click"
    :overlay-style="
      currentFrequency == 'HOURLY' ? { width: '470px' } : undefined
    "
  >
    <template slot="content">
      <div class="week-picker-popup-container">
        <weekly-block
          v-show="currentFrequency == 'WEEKLY'"
          ref="weekly-block"
          :is-range="false"
          :current-date="tempDate"
          :date-range="dateRange"
          :show-footer="false"
          :enable-null="false"
          @change-date="handleChangeDate"
        ></weekly-block>
        <daily-block
          v-show="currentFrequency == 'DAILY'"
          ref="daily-block"
          :is-range="false"
          :current-date="tempDate"
          :date-range="dateRange"
          :show-footer="false"
          :enable-null="false"
          @change-date="handleChangeDate"
        ></daily-block>
        <monthly-block
          v-show="currentFrequency == 'MONTHLY'"
          ref="monthly-block"
          :is-range="false"
          :current-date="tempDate"
          :date-range="dateRange"
          :show-footer="false"
          :enable-null="false"
          @change-date="handleChangeDate"
        ></monthly-block>
        <hourly-block
          v-show="currentFrequency == 'HOURLY'"
          :current-date="tempDate"
          :date-range="dateRange"
          @change-date="handleChangeDate"
          @change-hour="handleChangeHour"
        ></hourly-block>
      </div>
    </template>
    <primary-button
      :is-active="visible"
      :title="getTitle"
      :prefix="'/images/icon/date-picker-before.svg'"
      :hover-prefix="'/images/icon/date-picker-before-hover.svg'"
      :prefix-position="'left'"
      :custom-style="{ height: '31px' }"
      :default-class="'btn-custom btn-outline-custom-primary customize-btn'"
      :active-class="'outline-animation-2'"
      @on-active="handleClick"
    ></primary-button>
  </a-popover>
</template>

<script>
module.exports = {
  components: {
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
    "weekly-block": httpVueLoader(
      "/components/@base/date-picker/weekly-block.vue"
    ),
    "daily-block": httpVueLoader(
      "/components/@base/date-picker/daily-block.vue"
    ),
    "monthly-block": httpVueLoader(
      "/components/@base/date-picker/monthly-block.vue"
    ),
    "monthly-block": httpVueLoader(
      "/components/@base/date-picker/monthly-block.vue"
    ),
    "hourly-block": httpVueLoader(
      "/components/reject-rates/hourly-popup-content.vue"
    ),
  },
  props: {
    currentDate: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      }),
    },
    dateRange: {
      type: Object,
      default: () => ({
        minDate: null,
        maxDate: new Date(),
      }),
    },
    currentFrequency: {
      type: String,
      default: () => "DAILY",
    },
    defaultHour: {
      type: Object,
      default: () => {},
    },
    buttonType: String,
  },
  data() {
    return {
      tempDate: { ...this.currentDate },
      hour: { ...this.defaultHour },
      title: {
        from: "",
        to: "",
      },
      visible: false,
    };
  },
  computed: {
    getTitle() {
      let dateData = { ...this.currentDate };
      if (this.currentFrequency.includes("MONTHLY")) {
        dateData.fromTitle = moment(dateData.from).format("MMMM, YYYY");
        dateData.toTitle = moment(dateData.to).format("MMMM, YYYY");
      } else if (this.currentFrequency.includes("WEEKLY")) {
        let yearFrom = dateData.fromTitle.split("-")[0];
        let yearTo = dateData.toTitle.split("-")[0];
        let weekFrom = dateData.fromTitle.split("-")[1];
        let weekTo = dateData.toTitle.split("-")[1];
        dateData.fromTitle = `Week ${weekFrom}, ${yearFrom}`;
        dateData.toTitle = `Week ${weekTo}, ${yearTo}`;
      } else if (this.currentFrequency.includes("HOURLY")) {
        return `${this.hour.from}:00, ${dateData.fromTitle}`;
      }
      if (this.isRange) {
        return `${dateData.fromTitle} - ${dateData.toTitle}`;
      } else {
        return `${dateData.fromTitle}`;
      }
    },
    getButtonClass() {
      if (this.buttonType == "primary") {
        return {
          default:
            "btn-custom btn-custom-primary animationPrimary all-time-filters",
          active: "outline-animation",
        };
      } else if (this.buttonType == "secondary") {
        return {
          default: "btn-custom btn-outline-custom-primary",
          active: "animation-secondary",
        };
      }
    },
    getPrefixButton() {
      if (this.buttonType == "primary") {
        return {
          default: "/images/icon/cta-icon/inactive-arrow-down.svg",
          active: "",
        };
      } else if (this.buttonType == "secondary") {
        return {
          default: "/images/icon/cta-icon/secondary-arrow-down.svg",
          active: "/images/icon/cta-icon/secondary-arrow-up.svg",
        };
      }
    },
  },
  mounted() {
    this.initCurrentDate();
  },
  watch: {
    visible(newValue) {
      if (!newValue) {
        let passDate = { ...this.tempDate };
        passDate.hour = { ...this.hour };
        this.$emit("on-close", passDate, this.currentFrequency);
      } else {
        // this.tempDate = this.currentDate;
        // let child = this.$refs["weekly-block"];
        // if (child != null) {
        //   let passDate = { ...this.tempDate };
        //   console.log("watch visible", passDate);
        //   child.setDefault(passDate);
        // }
      }
    },
  },
  methods: {
    initCurrentDate() {
      if (this.currentFrequency == "WEEKLY") {
        console.log("currentDate", this.currentDate, this.tempDate);
        if (this.tempDate) {
          this.tempDate.fromTitle = `${this.tempDate.from.getFullYear()}-${this.getCurrentWeekNumber(
            this.tempDate.from
          )}`;
          this.tempDate.toTitle = `${this.tempDate.to.getFullYear()}-${this.getCurrentWeekNumber(
            this.tempDate.to
          )}`;
        }
      }
    },
    handleClick() {
      this.visible = !this.visible;
      if (this.visible) {
        setTimeout(() => {
          const childName = `${this.currentFrequency.toLowerCase}-block`;
          let child = this.$refs[childName];
          console.log("mounted", child);
          if (child != null) {
            let passDate = { ...this.tempDate };
            child.setDefault(passDate);
          }
        }, 0);
      }
    },
    handleChangeDate(data, title) {
      console.log("handleChangeDate", data, title);
      this.tempDate.from = data.start;
      this.tempDate.to = data.end;
      this.changeTitleValue(title);
      console.log("handleChangeDate after", this.currentDate);
    },

    changeTitleValue(newValue) {
      console.log("change title", newValue);
      this.tempDate.fromTitle = newValue.start;
      this.tempDate.toTitle = newValue.end;
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
    },
    handleChangeHour(hour) {
      this.hour = hour;
    },
  },
};
</script>

<style>
.week-picker-popup-container .block {
  margin-right: unset !important;
}
.ant-popover-inner-content {
  padding: 12px !important;
}
.date-block {
  padding: 12px;
}
.ant-modal-body {
  padding: 0;
  border-radius: 6px;
}

.ant-modal-content {
  width: fit-content;
  height: auto;
  border-radius: 6px;
}

.ant-modal-centered {
  top: -100px;
  vertical-align: text-top;
}

.custom-modal-title {
  border-radius: 6px 6px 0 0;
  background: #f5f8ff;
  font-size: 16px;
  color: #4b4b4b;
  display: flex;
  justify-content: space-between;
  position: relative;
  align-items: center;
  padding: 25px 23px 12px;
}

.close-button {
  font-size: 25px;
  display: flex;
  align-items: center;
  height: 17px;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: 0;
  left: 0;
  width: 100%;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.custom-modal-title {
  background: #f5f8ff;
  font-size: 16px;
  color: #4b4b4b;
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 24px;
  height: 56px;
}

.date-block {
  margin-right: 20px;
}

.footer-btn {
  margin-top: 20px;
  justify-content: flex-end;
}

.link-button {
  cursor: pointer;
}
.title {
  font-size: 13px;
  line-height: 16px;
  text-align: center;
  color: #696969;
}
.block {
  margin-right: 50px;
  min-width: 222px;
  position: relative;
}
.wrapper {
  font-family: Helvetica Neue, Regular;
  display: grid;
  position: relative;
  padding: 5px;
  grid-template-columns: repeat(9, 1fr);
  grid-template-rows: repeat(6, 30px);
  font-size: 12px;
  color: #4b4b4b;
}
.week-item {
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: 50% 50%;
  height: 32px;
  cursor: pointer;
}
.selected {
  color: #fff !important;
  background-color: #519ffc;
}
.selected:hover {
  background-color: #519ffc !important;
}

.week-number {
  width: 17px;
  height: 17px;
  text-align: center;
  line-height: 17px;
}

.month-item {
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: 50% 50%;
  height: 32px;
  cursor: pointer;
  margin-bottom: 20px;
}

.month-text {
  width: 25px;
  height: 25px;
  line-height: 25px;
  text-align: center;
  color: #4b4b4b;
  font-size: 12px;
}

.selected-month {
  border-radius: 25px;
}

.selected-week {
  border-radius: 17px;
}

.selected-year {
  border-radius: 34px;
}

.year-item {
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: 50% 50%;
  height: 32px;
  cursor: pointer;
  margin-bottom: 24px;
}

.year-text {
  width: 34px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  font-size: 12px;
}

.selected-month {
  color: #fff;
  background-color: #519ffc;
  border-radius: 34px;
}

.disable {
  color: #d5d5d5;
  pointer-events: none;
}

.today {
  color: #2aa3ff;
}

.wrapper-month {
  display: grid;
  position: relative;
  padding: 5px;
  grid-template-columns: repeat(3, 1fr);
  font-size: 12px;
  color: #4b4b4b;
}

.wrapper-year {
  display: grid;
  position: relative;
  padding: 5px;
  grid-template-columns: repeat(5, 1fr);
  font-size: 12px;
  color: #4b4b4b;
}

.header {
  display: flex;
  justify-content: space-between;
  height: 20px;
  align-items: center;
  padding: 0px 7px 0px 7px;
}

.date-bar {
  height: 22px;
  width: 76px;
  color: #4b4b4b;
  font-size: 11px;
  border: 0.5px solid #909090;
  border-radius: 2px;
  padding: 5px 6px 5px 6px;
}

.date-bar:hover {
  border: 0.5px solid #4b4b4b;
}

.date-bar:focus {
  outline: none;
  border: 1.5px solid #98d1fd;
}

.date-bar-disable {
  background-color: #e8e8e8;
  border: 1px solid transparent !important;
}

.date-bar::placeholder {
  color: #c9c9c9;
}

.date-bar-disable::placeholder {
  color: #909090;
}

.btn-custom-date {
  font-size: 11px;
  padding: 6px 8px;
  height: 24px;
  line-height: 11px;
  margin-left: 8px;
}

.btn-custom-date-light {
  background-color: #fff;
  border: 1px solid #7c7c7c;
  color: #7c7c7c !important;
}

.btn-custom-date-light:hover {
  background-color: #fafafa;
  border: 1px solid #7c7c7c;
  color: #464646 !important;
}

.btn-custom-primary-active {
  background-color: #3585e5;
  /* border: 2px solid #89d1fd !important; */
  outline: 2px solid #89d1fd !important;
}

.btn-disable {
  background-color: #c4c4c4 !important;
  border: 1px solid #c4c4c4 !important;
  pointer-events: none;
}

.warning-text {
  color: #ef4444;
  font-size: 7px;
  line-height: 11px;
  width: 80px;
}

.warning-input {
  border: 0.5px solid #ef4444 !important;
}

.disable-cta {
  pointer-events: none;
}

.ant-tooltip-arrow {
  top: 0px;
}

.light-animation-button {
  color: #464646;
  background-color: #fff;
  border: 1px solid #e8e8e8 !important;
  outline: 1px solid #e8e8e8 !important;
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
