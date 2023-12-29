<template>
  <div>
    <a-modal
      :visible="visible"
      :closable="false"
      @close="closeDatePicker"
      centered
      :body-style="{ padding: '0' }"
      :footer="null"
    >
      <div
        class="custom-modal-title"
        style="
          border-radius: 6px 6px 0 0;
          background: #f5f8ff;
          font-size: 16px;
          color: #4b4b4b;
          display: flex;
          justify-content: space-between;
          position: relative;
          align-items: center;
          padding: 25px 23px 12px;
        "
      >
        <span style="font-weight: bold; line-height: 100%">{{ getTitle }}</span>
        <span
          class="close-button"
          style="
            font-size: 25px;
            display: flex;
            align-items: center;
            height: 17px;
          "
          @click="closeDatePicker"
          aria-hidden="true"
        >
          <span class="t-icon-close"></span>
        </span>
        <div
          class="head-line"
          style="
            position: absolute;
            background: #52a1ff;
            height: 8px;
            border-radius: 6px 6px 0 0;
            top: 0;
            left: 0;
            width: 100%;
          "
        ></div>
      </div>
      <div class="custom-modal-body" style="padding: 16px 24px">
        <div class="custom-select-date-picker">
          <div class="container-date-pick">
            <!--DATE COMPONENT HERE! -->
            <daily-block
              v-show="
                selectedFrequency.includes('DAILY') ||
                selectedFrequency == 'CUSTOM'
              "
              :is-range="selectedButton.isRange"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
              ref="daily-block"
            ></daily-block>
            <weekly-block
              v-show="selectedFrequency.includes('WEEKLY')"
              ref="weekly-block"
              :is-range="selectedButton.isRange"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
            ></weekly-block>
            <monthly-block
              v-show="selectedFrequency.includes('MONTHLY')"
              ref="monthly-block"
              :is-range="selectedButton.isRange"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
            ></monthly-block>
            <yearly-block
              v-show="selectedFrequency.includes('YEARLY')"
              :is-range="selectedButton.isRange"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
            ></yearly-block>
            <div class="infor-block">
              <!-- <div
                style="
                  display: flex;
                  justify-content: space-around;
                  margin-bottom: -30px;
                "
              >
                <div>
                  <input
                    type="radio"
                    id="single"
                    :value="false"
                    v-model="isRange"
                  />
                  <label for="single">Single</label>
                </div>
                <div>
                  <input
                    type="radio"
                    id="range"
                    :value="true"
                    v-model="isRange"
                  />
                  <label for="range">Range</label>
                </div>
              </div> -->
              <!-- BUTTON COMPONENT -->
              <frequency-button
                :is-range="selectedButton.isRange"
                :list-button="selectOption"
                :selected-frequency="selectedFrequency"
                @change-frequency="handleChangeFrequency"
              ></frequency-button>
              <!-- TITLE COMPONENT -->
              <date-title
                :enable-edit="getDateBarStatus"
                :date-title="{
                  from: currentDate.fromTitle,
                  to: currentDate.toTitle,
                }"
                :date-value="{
                  from: currentDate.from,
                  to: currentDate.to,
                }"
                :frequency="selectedFrequency"
                :tooltip-message="getDateBarTooltipMessage"
                :change-title="changeTitleValue"
                :clear-data="clearData"
                @title-change="handleTitleChange"
              ></date-title>
            </div>
          </div>
        </div>
        <div
          class="d-flex footer-btn"
          style="margin-top: 20px; justify-content: flex-end"
        >
          <a
            @click="closeDatePicker()"
            style="font-size: 11px"
            id="light-animation"
            class="btn-custom btn-custom-date btn-custom-date-light animationPrimary animationOutline"
          >
            <span v-text="resources['cancel']"></span>
          </a>
          <a
            id="btn-update"
            class="btn-custom btn-custom-primary btn-custom-date animationPrimary animationOutline"
            @click="submitDate"
            :class="{ 'btn-disable': !checkEnableSubmit }"
          >
            <span v-text="resources['update']"></span>
          </a>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    selectOption: {
      type: Array,
      default: () => [],
    },
    defaultDate: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment(new Date()).format("YYYY-MM-DD"),
        toTitle: moment(new Date()).format("YYYY-MM-DD"),
      }),
    },
  },
  components: {
    "frequency-button": httpVueLoader(
      "/components/common/date-picker/frequency-button.vue"
    ),
    "date-title": httpVueLoader(
      "/components/common/date-picker/date-title.vue"
    ),
    "daily-block": httpVueLoader(
      "/components/common/date-picker/daily-block.vue"
    ),
    "weekly-block": httpVueLoader(
      "/components/common/date-picker/weekly-block.vue"
    ),
    "monthly-block": httpVueLoader(
      "/components/common/date-picker/monthly-block.vue"
    ),
    "yearly-block": httpVueLoader(
      "/components/common/date-picker/yearly-block.vue"
    ),
  },
  data() {
    return {
      visible: false,
      // frequency: "DAILY",
      currentDate: {
        from: null,
        to: null,
        fromTitle: "",
        toTitle: "",
      },
      dateRange: {
        minDate: null,
        maxDate: new Date(),
      },
      today: new Date(),
      selectedFrequency: "DAILY",
      selectedButton: { title: "Daily", type: "DAILY", isRange: false },
    };
  },
  computed: {
    getTitle() {
      return this.resources["pick_time_range"];
    },
    checkEnableDatePicker() {
      return false;
    },
    getDateBarStatus() {
      if (this.selectedFrequency == "CUSTOM_RANGE") {
        return true;
      }
      return false;
    },
    getDateBarTooltipMessage() {
      if (this.selectedFrequency != "DAILY_RANGE") {
        return "Time range is fixed for this option.";
      }
      return "";
    },
    checkEnableSubmit() {
      if (this.currentDate.from && this.currentDate.to) {
        return true;
      }
      return false;
    },
  },
  watch: {
    // isRange(newValue) {
    //   if (newValue) {
    //     this.selectedFrequency = "DAILY_RANGE";
    //   } else {
    //     this.selectedFrequency = "DAILY";
    //   }
    // },
  },
  methods: {
    closeDatePicker() {
      this.visible = false;
    },
    getDefaultData(frequency, dateData) {
      console.log(
        "getDefaultData",
        frequency,
        this.selectOption,
        this.selectedButton
      );
      let date = dateData;
      this.selectedFrequency = frequency;
      const searchBtn = this.selectOption.filter(
        (item) => item.type == frequency
      );
      if (searchBtn) {
        this.selectedButton = this.selectOption.filter(
          (item) => item.type == frequency
        )[0];
      } else {
        this.selectedButton = this.selectOption[0];
      }

      if (date) {
        this.currentDate = { ...date };
        let childName = "";
        if (frequency.includes("DAILY")) {
          childName = "daily-block";
        } else if (frequency.includes("WEEKLY")) {
          childName = "weekly-block";
        } else if (frequency.includes("MONTHLY")) {
          childName = "monthly-block";
        }
        let newDate = { ...date };
        var child = this.$refs[childName];
        if (child != null) {
          child.setDefault(newDate);
        }
      }
    },
    showDatePicker(frequency, date) {
      this.getDefaultData(frequency, date);
      this.visible = true;
    },
    handleChangeFrequency(button) {
      if (this.selectedButton != button) {
        console.log(button);
        this.selectedFrequency = button.type;
        this.selectedButton = button;
      }
    },
    handleChangeDate(data, title) {
      console.log("handleChangeDate", data, title);
      this.currentDate.from = data.start;
      this.currentDate.to = data.end;
      this.changeTitleValue(title);
      console.log("handleChangeDate after", this.currentDate);
    },
    clearData() {
      this.currentDate = {
        from: null,
        to: null,
        fromTitle: "",
        toTitle: "",
      };
    },
    changeTitleValue(newValue) {
      console.log("change title", newValue);
      this.currentDate.fromTitle = newValue.start;
      this.currentDate.toTitle = newValue.end;
    },
    handleTitleChange(isValid, newValue) {
      console.log("receive range", isValid, newValue);
      if (isValid) {
        this.currentDate.from = moment(newValue.start, "YYYY-MM-DD").toDate();
        this.currentDate.to = moment(newValue.end, "YYYY-MM-DD").toDate();
        // var child = Common.vue.getChild(this.$children, "daily-block");
        var child = this.$refs["daily-block"];
        if (child != null) {
          child.changeRange({
            start: this.currentDate.fromTitle,
            end: this.currentDate.toTitle,
          });
        }
      } else {
        this.currentDate.from = null;
        this.currentDate.to = null;
      }
    },
    submitDate() {
      let frequency = this.selectedFrequency.replace("_RANGE", "");
      let dateData = { ...this.currentDate };
      this.$emit("change-date", dateData, frequency);
    },
  },
};
</script>

<style>
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
.container-date-pick {
  display: flex;
  justify-content: space-around;
  height: fit-content;
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
.footer-btn {
  margin-top: 20px;
  justify-content: flex-end;
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

.link-button {
  cursor: pointer;
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

/* .ant-tooltip-inner {
  width: 180px;
  padding: 5px 4px 4px 6px;
} */

.ant-tooltip-arrow {
  top: 0px;
}

.light-animation-button {
  color: #464646;
  background-color: #fff;
  border: 1px solid #e8e8e8 !important;
  outline: 1px solid #e8e8e8 !important;
}
.block-footer {
  bottom: 0;
  margin-left: 10px;
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
