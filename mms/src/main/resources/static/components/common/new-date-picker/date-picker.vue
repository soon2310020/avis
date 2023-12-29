<template> 
<div :class="visible ? 'd-none' : 'd-flex'" class="w-100  flex-column align-items-center justify-content-center">
  <div class="data-picker-custom">
      <div    
        class="custom-modal-title"
        style="
          border-radius: 6px 6px 0px 0px;
          background: #f5f8ff;
          font-size: 16px;
          color: #4b4b4b;
          display: flex;
          justify-content: space-between;
          position: relative;
          align-items: center;
          padding: 25px 14.34px 12px 10px;
        "
      >
        <span style="font-weight: bold; line-height: 100%">Choose a {{ getTitle }}</span>
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
              v-show="selectedFrequency.includes('DATE')"
              :is-range="false"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
              ref="daily-block"
            ></daily-block>
             <custom-block
              v-show="selectedFrequency.includes('CUSTOM')"
              :is-range="isRange"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
              ref="daily-block"
            ></custom-block>
            <weekly-block
              v-show="selectedFrequency.includes('WEEK')"
              ref="weekly-block"
              :is-range="false"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
            ></weekly-block>
            <monthly-block
              v-show="selectedFrequency.includes('MONTH')"
              ref="monthly-block"
              :is-range="false"
              :current-date="currentDate"
              :date-range="dateRange"
              :frequency="selectedFrequency"
              @change-date="handleChangeDate"
            ></monthly-block>
            <yearly-block
              v-show="selectedFrequency.includes('YEAR')"
              :is-range="false"
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
                :is-range="isRange"
                :list-button="selectOption"
                :selected-frequency="selectedFrequency"
                @change-frequency="handleChangeFrequency"
              ></frequency-button>
             
            </div>
          </div>
        </div>
        <div>
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
        <div class="d-flex footer-btn" style="margin-top: 20px; justify-content: flex-end;">
           <cta-button
                :disabled="false"
                :color-type="cancelBtnColor"
                :button-type="buttonTypeProps"
                :click-handler="closeDatePicker">Cancel</cta-button>
           <cta-button  
                :disabled="!checkEnableSubmit"
                :color-type="primaryBtnColor"
                :button-type="buttonTypeProps"
                :click-handler="submitDate">Save</cta-button>
        </div>
      </div>
  </div>
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
    isRange: {
      type: Boolean,
      default: () => false,
    },
    selectedFrequency: {
      type: String,
      default: () => 'DATE',
    },
  },
  components: {
    "frequency-button": httpVueLoader(
      "/components/common/new-date-picker/frequency-button.vue"
    ),
    "date-title": httpVueLoader(
      "/components/common/new-date-picker/date-title.vue"
    ),
    "daily-block": httpVueLoader(
      "/components/common/new-date-picker/daily-block.vue"
    ),
    "weekly-block": httpVueLoader(
      "/components/common/new-date-picker/weekly-block.vue"
    ),
    "monthly-block": httpVueLoader(
      "/components/common/new-date-picker/monthly-block.vue"
    ),
    "yearly-block": httpVueLoader(
      "/components/common/new-date-picker/yearly-block.vue"
    ),
    "custom-block": httpVueLoader(
      "/components/common/new-date-picker/custom-block.vue"
    ),
     'cta-button': httpVueLoader('/components/common/cta-button.vue'),

  },
  data() {
    return {
      cancelBtnColor : 'white',
      primaryBtnColor : 'blue-fill',
      buttonTypeProps : '',
      getTitle : "Date",
      visible: false,
      frequency: "DAILY",
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      dateRange: {
        minDate: null,
        maxDate: new Date(),
      },
      today: new Date(),
    };
  },
  computed: {
    checkEnableDatePicker() {
      return false;
    },
    getDateBarStatus() {
      if (this.selectedFrequency == "DAILY_RANGE") {
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
      console.log('check submit', this.currentDate)
      if (this.currentDate.from && this.currentDate.to) {
        return true;
      }
      return false;
    },
  },
  watch: {
   selectedFrequency(value){
  function capitalize(str) {
  const lower = str.toLowerCase();
  return str.charAt(0).toUpperCase() + lower.slice(1);
} 
  if(value === 'CUSTOM'){
      this.getTitle = 'Custom Range';
    } else {
      this.getTitle = capitalize(value);
    }
   }
  },
  methods: {
    closeDatePicker() {
      this.visible = !this.visible;
    },
    getDefaultData(frequency, dateData){
      let date = dateData
      this.selectedFrequency = this.isRange ? frequency + "_RANGE" : frequency;
      if (date) {
        this.currentDate = {...date}
        let childName = ''
        if (frequency.includes('DATE')) {
          childName = 'daily-block'
        } else if (frequency.includes('WEEK')){
          childName = 'weekly-block'
        } else if (frequency.includes('MONTH')){
          childName = 'monthly-block'
        }
        else if (frequency.includes('YEAR')){
          childName = 'yearly-block'
        }
         else if (frequency.includes('CUSTOM')){
          childName = 'custom-block'
        }
        let newDate = {...date}
        var child = this.$refs[childName];
        if (child != null) {
          child.setDefault(newDate)
        }
      }
      console.log("frequency", this.frequency);
    },
    showDatePicker(frequency, date) {
      this.getDefaultData(frequency, date);
      this.visible = !this.visible;
    },

    handleChangeFrequency(frequency) {
      if (this.selectedFrequency != frequency) {
        console.log(frequency);
        this.selectedFrequency = frequency;
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
        this.currentDate.from = moment(
          newValue.start,
          "YYYY-MM-DD"
        ).toDate();
        this.currentDate.to = moment(
          newValue.end,
          "YYYY-MM-DD"
        ).toDate();
        // var child = Common.vue.getChild(this.$children, "daily-block");
        var child = this.$refs["daily-block"];
        if (child != null) {
          child.changeRange({
            start: this.currentDate.fromTitle,
            end: this.currentDate.toTitle,
          });
        }
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
  width: 504px;
  height: auto;
  border-radius: 6px;
}
.ant-modal-centered {
  top: -100px;
  vertical-align: text-top;
}
.pretty.p-default input:checked ~ .state label:after {
  background-color: #0075ff;
}
.pretty.p-round {
}
.pretty .state label {
  margin-left: 4px;
}
.pretty .state label:before,
.pretty .state label:after {
  width: 14px;
  height: 14px;
}
:not(.p-round) .state label:before,
:not(.p-round) .state label:after {
  border-radius: 1px;
}
.p-round .state label:before {
  border: 0.7px solid #909090;
}
.pretty .state label:before {
  border-color: #909090;
}
.pretty:hover .state label:before {
  border-color: #4b4b4b;
}
.pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff;
  /*border-radius: 1px;*/
}
.pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff;
  /*border-radius: 1px;*/
}
.pretty span {
  margin-left: 0;
}

.pretty {
  margin-right: unset;
}
/* .row-time{
  height: 27px;
} */
.calendar-picker-input {
  width: 232px;
  height: 27px;
  color: #4b4b4b;
  border: 0.5px solid #909090;
  border-radius: 2px;
  font-size: 13px;
  padding: 8px 25px 8px 7px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.calendar-picker-input::placeholder {
  color: #c9c9c9;
}
.calendar-picker-input:hover {
  border: 0.5px solid #4b4b4b;
}
.calendar-picker-input-active {
  border: 1.5px solid #98d1fd !important;
  outline: none !important;
  box-shadow: none !important;
}
.calendar-picker-input:focus {
  outline: none !important;
  box-shadow: none !important;
}
.export-filed-title {
  font-size: 14px;
  color: #4b4b4b;
}
.choosing-items {
  margin-bottom: 10px;
}
.vc-container.vc-blue {
  border: none;
}
.container-date-pick {
  display: flex;
  justify-content: space-between;
}
.vc-container.vc-blue {
  border: none;
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
/* .week-number:hover {
  color: #53575f;
  background-color: #f0f3f6;
  border-radius: 17px;
} */
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
/* .month-text:hover {
  color: #4b4b4b;
  background-color: #f0f3f6;
  border-radius: 25px;
} */
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
/* .year-text:hover {
  color: #4b4b4b;
  background-color: #f0f3f6;
  border-radius: 34px;
} */
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
.block-button {
  display: block;
}
#date-picker-modal .block-button {
  margin-top: 30px;
}
#export-data-modal .select-type-button, #date-picker-modal .select-type-button{
  width: 184px !important;
  height: 24px !important;
}
.select-type-button {
  font-size: 13px;
  color: #52a1ff;
  border: 1px solid #52a1ff;
  background-color: #fff;
  border-radius: 2px;
  margin-bottom: 20px;
}
.select-type-button:focus {
  outline: none !important;
}
.select-type-button:hover {
  background-color: #ebf4ff;
}
.selected-button {
  background-color: #52a1ff;
  color: #ffffff;
}
.selected-button:hover {
  background-color: #3994ff;
  color: #ffffff;
}
.d-flex {
  display: flex;
  justify-content: space-between;
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
  /* pointer-events: none; */
  background-color: #e8e8e8;
  border: none !important;
}
.date-bar::placeholder {
  color: #c9c9c9;
}
.date-bar-disable::placeholder {
  color: #909090;
}
.right-title {
  float: left;
}
.block-footer {
  font-size: 11px;
  color: #838383;
  position: absolute;
  bottom: 0;
  margin-left: 10px;
}
.btn-custom-date {
  font-size: 11px;
  padding: 6px 8px;
  height: 24px;
  line-height: 11px;
  margin-left: 8px;
  /* margin: 1px 1px 1px 8px; */
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
/* .btn-custom-date-light:active {
  color: #464646;
  background-color: #fff;
  border: 2px solid #e8e8e8 !important;
  padding: 5px 7px 5px 7px;
} */
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
.ant-tooltip-inner {
  width: 180px;
  padding: 5px 4px 4px 6px;
}
.ant-tooltip-arrow {
  top: 0px;
}
.light-animation-button {
  /* animation: light-animation 1s;
  animation-iteration-count: 1;
  animation-direction: alternate; */
  color: #464646;
  background-color: #fff;
  border: 1px solid #e8e8e8 !important;
  outline: 1px solid #e8e8e8 !important;
} /*
@keyframes light-animation {
  0% {
  }
  33% {
    color: #464646;
    background-color: #fff;
    border: 2px solid #e8e8e8 !important;
    padding: 5px 7px 5px 7px;
  }
  66% {
    color: #464646;
    background-color: #fff;
    border: 2px solid #e8e8e8 !important;
    padding: 5px 7px 5px 7px;
  }
  100% {
  }
} */
.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
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

/*end override library style */
</style>

<style scoped>
.export-field-item_left {
  margin-right: 27px;
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

.data-picker-custom{
width: 515px;
border-radius: 6px;
}

.head-line {

  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 6px 6px;
  top: 0;
  width: 100%;
}
.custom-modal-body {
  padding: 24px;
}
.ant-modal {
  width: 504px !important;
}
.dropdown-legend .checkbox-inactive input:checked ~ .state label:after{
  background-color: #C4C4C4 !important;
}
.dropdown-legend .checkbox-inactive input:checked ~ .state label::before{
  border-color: #C4C4C4 !important;
}

.ant-modal-body {
    padding: 0;
    border-radius: 6px;
}

.ant-modal-content {
    width: 504px;
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

.ant-tooltip-inner {
    width: 180px;
    padding: 5px 4px 4px 6px;
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

