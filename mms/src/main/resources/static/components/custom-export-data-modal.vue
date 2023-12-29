<template>
  <a-modal
    id="export-data-modal"
    v-if="showExportModal"
    :visible="showExportModal"
    :closable="false"
    @close="close"
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
        @click="close"
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
    <div
      v-if="!calendarShow"
      class="custom-modal-body"
      style="padding: 20px 25px"
    >
      <div class="export-field-item">
        <div class="export-field-item_left">
          <label class="export-filed-title"
            >Data Frequency <span class="badge-require"></span
          ></label>
        </div>
        <div class="export-field-item_right">
          <common-button
            :title="choosedDataFrequency"
            :is-show="showSortDataFrequency"
            :is-blue="false"
            @click="handleToggleDataFrequency"
            class="btn-custom animationprimary btn-custom1 btn-custom1-primary"
          >
          </common-button>
          <common-select-popover
            :is-visible="showSortDataFrequency"
            @close="handlecloseDataFrequency"
          >
            <common-select-dropdown
              :style="{ position: 'static' }"
              :class="{ show: showSortDataFrequency }"
              :searchbox="false"
              :checkbox="false"
              :items="listOfDataFrequency"
              :set-result="() => {}"
              :click-handler="onItemSelect"
              class="dropdown-wrap"
            ></common-select-dropdown>
          </common-select-popover>
        </div>
      </div>

      <div v-if="dataFrequency !== 'EVERY_SHOT'" class="export-field-item">
        <div class="export-field-item_left">
          <label class="export-filed-title"
            >Data Type<span class="badge-require"></span
          ></label>
        </div>
        <div class="export-field-item_right" style="display: flex; width: 100%">
          <div class="choosing-items" style="margin-bottom: 0px">
            <div class="choosing-item">
              <p-check
                :checked="dataType.includes('SHORT_COUNT')"
                @change="chooseDataType($event, 'SHORT_COUNT')"
                key="SHORT_COUNT"
              >
                <span style="font-size: 14.66px; height: 1.1rem"
                  >Shot Count</span
                >
              </p-check>
            </div>
            <div class="choosing-item">
              <p-check
                :checked="dataType.includes('CYCLE_TIME')"
                @change="chooseDataType($event, 'CYCLE_TIME')"
                key="CYCLE_TIME"
              >
                <span style="font-size: 14.66px; height: 1.1rem">
                  Cycle Time
                </span>
              </p-check>
            </div>
          </div>
          <div style="margin-bottom: 0" class="choosing-items">
            <div class="choosing-item">
              <p-check
                :checked="dataType.includes('UPTIME')"
                key="UPTIME"
                @change="chooseDataType($event, 'UPTIME')"
              >
                <span style="font-size: 14.66px; height: 1.1rem">Uptime</span>
              </p-check>
            </div>

            <div class="choosing-item">
              <p-check
                :checked="dataType.includes('TEMPERATURE')"
                key="TEMPERATURE"
                @change="chooseDataType($event, 'TEMPERATURE')"
              >
                <span style="font-size: 14.66px; height: 1.1rem"
                  >Temperature</span
                >
              </p-check>
            </div>
          </div>
        </div>
      </div>

      <div
        style="margin-bottom: 0 !important"
        class="export-field-item row-time"
      >
        <div class="export-field-item_left">
          <label class="export-filed-title"
            >Time Range
            <span class="badge-require"></span>
          </label>
        </div>
        <div class="export-field-item_right">
          <base-button level="secondary" icon="calendar" @click="showCalendar">
            {{ getDisplayDatePicker }}
          </base-button>
        </div>
      </div>
    </div>
    <div
      style="
        display: flex;
        justify-content: flex-end;
        margin-right: 25px;
        padding-bottom: 20px;
      "
    >
      <base-button level="secondary" type="cancel" @click="close">
        {{ resources["cancel"] }}
      </base-button>
      <base-button
        level="primary"
        style="margin-left: 8px"
        :disabled="!checkEnable"
        @click="exportDynamic"
      >
        {{ resources["export"] }}
      </base-button>
    </div>

    <date-picker-modal
      ref="datePickerModalRefEnd"
      :select-option="timeOption"
      :resources="resources"
      @change-date="handleChangeDueDate"
    >
    </date-picker-modal>
  </a-modal>
</template>

<script>
const LIST_OF_DATA_FREQUENCY = [
  { title: "Every Shot", value: "EVERY_SHOT" },
  { title: "Hourly Summary", value: "HOUR" },
  { title: "Daily Summary", value: "DAY" },
  { title: "Weekly Summary", value: "WEEK" },
  { title: "Monthly Summary", value: "MONTH" },
];

module.exports = {
  props: {
    showExportModal: {
      type: Boolean,
      default: false,
    },
    resources: Object,
    isOldGenIncluded: {
      default: false,
      type: Boolean,
    },
  },
  components: {
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
  },
  data() {
    return {
      dataType: [],
      dataFrequency: "EVERY_SHOT",
      startDate: null,
      endDate: null,
      time: "",
      calendarShow: false,
      defaultValue: moment(),
      errorToDate: false,
      errorFromDate: false,
      showDatePickerModal: false,
      range: {
        start: null,
        end: null,
        startMessage: "",
        endMessage: "",
      },
      timeOption: [
        { title: "Daily", type: "DAILY", isRange: false },
        { title: "Weekly", type: "WEEKLY", isRange: false },
        { title: "Monthly", type: "MONTHLY", isRange: false },
        { title: "Yearly", type: "YEARLY", isRange: false },
        { title: "Custom Range", type: "CUSTOM", isRange: true },
      ],
      time: null,
      rangeStart: null,
      rangeEnd: null,
      today: new Date(),
      currentYearForWeek: new Date().getFullYear(),
      currentYearForMonth: new Date().getFullYear(),
      currentMonth: new Date().getMonth(),
      yearRange: 2010,
      showSortDataFrequency: false,
      months: [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
      ],
      listOfDataFrequency: [...LIST_OF_DATA_FREQUENCY],
      choosedDataFrequency: "Every Shot",
      selectedItem: {
        selectedMonth: null,
        selectedYear: null,
        selectedWeek: null,
      },
      selectedType: "",
      displayDatePicker: "",
      dateFrom: "",
      dateTo: "",
      trigger: 0,
      debounceTimeOut: null,
      customKey: 0,
      inputType: null,
      textboxKey: 0,
      tempData: {
        range: {
          start: null,
          end: null,
          startMessage: "",
          endMessage: "",
        },
        selectedItem: {
          selectedMonth: null,
          selectedYear: null,
          selectedWeek: null,
        },
        selectedType: "",
        dateFrom: "",
        dateTo: "",
      },
      isRange: false,
    };
  },
  computed: {
    checkEnable() {
      let result = (this.rangeStart && this.rangeEnd) || this.time;
      if (this.dataFrequency === "EVERY_SHOT")
        return this.dataFrequency && result;
      return (
        this.dataFrequency &&
        this.dataType &&
        this.dataType.length > 0 &&
        result
      );
    },
    getCurrentWeek() {
      return this.getCurrentWeekNumber(this.today);
    },
    getTitle() {
      if (this.calendarShow) {
        return "Pick A Time Range";
      } else {
        return "Export Dynamic Tooling Data";
      }
    },
    isEveryShotCase() {
      return this.dataFrequency === "EVERY_SHOT";
    },
    getDisplayDatePicker() {
      return this.displayDatePicker || "Select Date";
    },
  },
  methods: {
    handleChangeDueDate(timeValue, frequency) {
      this.selectedType = frequency;
      if (frequency == "DAILY" || frequency == "EVERY_SHOT") {
        this.displayDatePicker = moment(timeValue.from).format("YYYY-MM-DD");
        this.rangeStart = `${moment(timeValue.from).format("YYYYMMDD")}`;
        this.rangeEnd = `${moment(timeValue.from).format("YYYYMMDD")}`;
      } else if (frequency == "MONTHLY") {
        this.displayDatePicker = moment(timeValue.from).format("YYYY-MM");
        this.time = `${moment(timeValue.from).format("YYYYMM")}`;
      } else if (frequency == "WEEKLY") {
        this.displayDatePicker = `Week ${timeValue.fromTitle.split("-")[1]}, ${
          timeValue.fromTitle.split("-")[0]
        }`;
        this.time = `${timeValue.fromTitle.split("-")[0]}${
          timeValue.fromTitle.split("-")[1]
        }`;
      } else if (frequency == "YEARLY") {
        this.displayDatePicker = moment(timeValue.from).format("YYYY");
        this.time = `${moment(timeValue.from).format("YYYY")}`;
      } else if (frequency == "CUSTOM") {
        this.displayDatePicker = `From ${timeValue.fromTitle} to ${timeValue.toTitle}`;
        this.rangeStart = `${moment(timeValue.from).format("YYYYMMDD")}`;
        this.rangeEnd = `${moment(timeValue.to).format("YYYYMMDD")}`;
      }
      this.$refs.datePickerModalRefEnd.closeDatePicker();
    },
    chooseDataFrequency(item) {
      this.dataFrequency = item;
    },
    chooseDataType(checked, data) {
      // this.dataType = 'CYCLE_TIME'
      if (checked) {
        if (
          !this.dataType ||
          this.dataType.filter((item) => item == data).length == 0
        ) {
          this.dataType.push(data);
        }
      } else {
        this.dataType = this.dataType.filter((item) => item != data);
      }
      console.log(this.dataType);
    },
    setDataType(isChecked, type) {
      if (isChecked) {
        this.dataType = type;
      } else {
        this.dataType = null;
      }
    },
    setDataFrequency(type) {
      this.dataFrequency = type;
    },
    handlecloseDataFrequency() {
      this.showSortDataFrequency = false;
    },
    handleToggleDataFrequency(isShow) {
      this.showSortDataFrequency = isShow;
    },
    animationPrimary() {
      $(".animationPrimary").click(function () {
        console.log($(".animationPrimary"));
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(function () {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("outline-animation");
          }
        );
      });
    },
    close() {
      console.log("close-export");
      this.$emit("close-export");
    },
    convertTimestamp(timestamp) {
      return new Date(timestamp * 1000)
        .toISOString()
        .slice(0, 10)
        .split("-")
        .join("");
    },
    showCalendar() {
      const datePickerModalRefEnd = this.$refs.datePickerModalRefEnd;
      datePickerModalRefEnd.showDatePicker(
        this.dataFrequency == "EVERY_SHOT" ? "EVERY_SHOT" : "DAILY",
        {
          from: new Date(),
          to: new Date(),
          fromTitle: moment().format("YYYY-MM-DD"),
          toTitle: moment().format("YYYY-MM-DD"),
        }
      );
    },
    convertDate(date) {
      return `${date.getFullYear()}${
        +date.getMonth() + 1 < 10
          ? "0" + (+date.getMonth() + 1)
          : +date.getMonth() + 1
      }${date.getDate() < 10 ? "0" + date.getDate() : date.getDate()}`;
    },
    exportDynamic() {
      let data = null;
      if (
        this.selectedType == "CUSTOM" ||
        this.selectedType === "DAILY" ||
        this.selectedType === "EVERY_SHOT"
      ) {
        data = {
          rangeType: "CUSTOM_RANGE",
          dataType: this.dataType,
          dataFrequency: this.dataFrequency,
          startDate: this.rangeStart,
          endDate: this.rangeEnd,
        };
      } else {
        data = {
          rangeType: this.selectedType,
          dataType: this.dataType,
          dataFrequency: this.dataFrequency,
          time: this.time,
        };
      }
      console.log(data, "data");
      this.$emit("export-dynamic", data, moment().add(700, "ms"));
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
    },
    onItemSelect(item) {
      this.choosedDataFrequency = item.title;
      this.dataFrequency = item.value;
      this.showSortDataFrequency = false;
    },
    getNumberOfWeek() {
      let result = this.getCurrentWeekNumber(
        moment("12-31-" + this.currentYearForWeek, "MM-DD-YYYY").toDate()
      );
      if (
        moment("01-01-" + this.currentYearForWeek, "MM-DD-YYYY")
          .toDate()
          .getDay() >= 4
      ) {
        result--;
      }
      if (
        moment("12-31-" + this.currentYearForWeek, "MM-DD-YYYY")
          .toDate()
          .getDay() < 4
      ) {
        result--;
      }
      return result;
    },
    getDateRange() {
      let n1 = null;
      let n2 = null;
      switch (this.selectedType) {
        case "DAILY":
          console.log("this.selectedType", this.selectedType);

        case "WEEKLY":
          if (this.selectedItem.selectedWeek != null) {
            var firstDay = new Date(
              this.selectedItem.selectedYear,
              0,
              1
            ).getDay();
            var d = new Date(
              "Jan 01, " + this.selectedItem.selectedYear + " 00:00:00"
            );
            var lastWeekDate =
              d.getTime() -
              3600000 * 24 * (firstDay - 1) +
              604800000 * this.getNumberOfWeek();
            var w =
              d.getTime() -
              3600000 * 24 * (firstDay - 1) +
              604800000 * (this.selectedItem.selectedWeek - 1);

            if (this.selectedItem.selectedYear == this.getNumberOfWeek()) {
              if (new Date(lastWeekDate).getDay() < 4) {
                w =
                  d.getTime() -
                  3600000 * 24 * (firstDay - 1) +
                  604800000 * (this.selectedItem.selectedWeek - 1);
              }
            } else {
              if (d.getDay() > 3) {
                w =
                  d.getTime() -
                  3600000 * 24 * (firstDay - 1) +
                  604800000 * this.selectedItem.selectedWeek;
              }
            }

            n1 = new Date(w - 86400000);
            n2 = new Date(w + 432000000);
          }
          break;
        case "MONTHLY":
          if (this.selectedItem.selectedMonth != null) {
            n1 = new Date(
              this.selectedItem.selectedYear,
              this.selectedItem.selectedMonth,
              1
            );
            n2 = new Date(
              this.selectedItem.selectedYear,
              +this.selectedItem.selectedMonth + 1,
              0
            );
          }
          break;
        case "YEARLY":
          if (this.selectedItem.selectedYear) {
            n1 = new Date(this.selectedItem.selectedYear, 0, 1);
            n2 = new Date(this.selectedItem.selectedYear, 11, 31);
          }
          break;
      }
      if (n1 != null) {
        this.dateFrom = `${n1.getFullYear()}-${
          n1.getMonth() < 9 ? "0" + (+n1.getMonth() + 1) : +n1.getMonth() + 1
        }-${n1.getDate() < 10 ? "0" + +n1.getDate() : n1.getDate()}`;
      } else {
        this.dateFrom = "";
      }
      if (n2 != null) {
        this.dateTo = `${n2.getFullYear()}-${
          n2.getMonth() < 9 ? "0" + (+n2.getMonth() + 1) : +n2.getMonth() + 1
        }-${n2.getDate() < 10 ? "0" + +n2.getDate() : n2.getDate()}`;
      } else {
        this.dateTo = "";
      }
      this.textboxKey++;
      console.log(`From ${this.dateFrom} to ${this.dateTo}`);
    },
    resetDateData() {
      this.range = {
        start: null,
        end: null,
        startMessage: "",
        endMessage: "",
      };
      (this.today = new Date()),
        (this.currentYearForWeek = new Date().getFullYear());
      this.currentYearForMonth = new Date().getFullYear();
      this.currentMonth = new Date().getMonth();
      this.yearRange = 2010;
      this.selectedItem = {
        selectedMonth: null,
        selectedYear: null,
        selectedWeek: null,
      };
      // this.selectedType = "";
      this.dateFrom = "";
      this.dateTo = "";
      this.inputType = "";
    },
    checkIsCheckedDataType(dataType) {
      return this.dataType.filter((item) => item == dataType).length > 0;
    },
    changeRangeType(type) {
      this.resetDateData();
      this.selectedType = type;
      console.log(this.selectedType);
    },

    parseDate(inputDate, dateType) {
      if (dateType == "from") {
        if (inputDate == "") {
          this.range.startMessage = "";
        } else {
          let dateParts = inputDate.split("-");
          let checkDate = new Date(
            dateParts[0],
            +dateParts[1] - 1,
            dateParts[2]
          );
          if (dateParts.length == 3 && checkDate != "Invalid Date") {
            if (
              checkDate.getDate() == dateParts[2] &&
              checkDate.getMonth() == +dateParts[1] - 1 &&
              checkDate.getFullYear() == dateParts[0]
            ) {
              if (checkDate.getTime() <= this.today.getTime()) {
                return checkDate;
              } else {
                this.range.startMessage =
                  "‘From’ date must be earlier or equal than Today.";
              }
            } else {
              this.range.startMessage =
                "Please enter the date in the format ‘yyyy-mm-dd’.";
            }
          } else {
            console.log(`this - ${this.inputType}`);
            this.range.startMessage =
              "Please enter the date in the format ‘yyyy-mm-dd’.";
          }
        }
      } else {
        if (inputDate == "") {
          this.range.endMessage = "";
        } else {
          let dateParts = inputDate.split("-");
          let checkDate = new Date(
            dateParts[0],
            +dateParts[1] - 1,
            dateParts[2]
          );
          if (dateParts.length == 3 && checkDate != "Invalid Date") {
            if (
              checkDate.getDate() == dateParts[2] &&
              checkDate.getMonth() == +dateParts[1] - 1 &&
              checkDate.getFullYear() == dateParts[0]
            ) {
              if (checkDate.getTime() <= this.today.getTime()) {
                return checkDate;
              } else {
                this.range.endMessage =
                  "‘To’ date must be earlier or equal than Today.";
              }
            } else {
              this.range.endMessage =
                "Please enter the date in the format ‘yyyy-mm-dd’.";
              console.log(
                `${checkDate.getFullYear()}-${checkDate.getMonth()}-${checkDate.getDate()}`
              );
            }
          } else {
            this.range.endMessage =
              "Please enter the date in the format ‘yyyy-mm-dd’.";
          }
        }
      }
      return null;
    },
    checkDateFrom() {
      let checkDate = this.parseDate(this.dateFrom, "from");
      let endDate = this.parseDate(this.dateTo, "to");
      if (checkDate) {
        if (this.dateTo != "" && endDate != null) {
          // if (checkDate != null && this.dateTo != "" && endDate != null) {
          if (checkDate.getTime() > endDate.getTime()) {
            this.range.startMessage =
              "‘From’ date must be earlier than ‘To’ date.";
          } else {
            console.log("1-1");
            this.range.startMessage = "valid";
            this.range.start = checkDate;

            if (this.inputType == "from") {
              this.checkDateTo();
            }
          }
        } else {
          console.log("1-2");
          this.range.startMessage = "valid";
          this.range.start = checkDate;
        }
      }
    },
    checkDateTo() {
      let checkDate = this.parseDate(this.dateTo, "to");
      let startDate = this.parseDate(this.dateFrom, "from");
      if (checkDate) {
        if (this.dateFrom != "" && startDate != null) {
          if (checkDate.getTime() < startDate.getTime()) {
            this.range.endMessage = "‘To’ date must be later than ‘From’ date.";
          } else {
            console.log("2-1");
            this.range.endMessage = "valid";
            this.range.end = checkDate;

            if (this.inputType == "to") {
              this.checkDateFrom();
            }
          }
        } else {
          console.log("2-2");
          this.range.endMessage = "valid";
          this.range.end = checkDate;
        }
      }
    },
    handleDateFromInput: _.debounce(function (event) {
      this.inputType = "from";
      this.range.start = null;
      this.dateFrom = event.target.value;
      this.checkDateFrom();
      console.log(this.range);
      this.customKey++;
      this.trigger++;
    }, 500),
    handleDateToInput: _.debounce(function (event) {
      this.inputType = "to";
      this.range.end = null;
      this.dateTo = event.target.value;
      this.checkDateTo();
      console.log(this.range);
      this.customKey++;
      this.trigger++;
    }, 500),
    prevYearRange() {
      if (this.yearRange <= 10) {
        this.yearRange = 1;
      } else {
        this.yearRange -= 20;
      }
    },
    nextYearRange() {
      if (this.yearRange == 1) {
        this.yearRange = 10;
      } else {
        this.yearRange += 20;
      }
    },
  },
  watch: {
    startDate(newValue, oldValue) {
      console.log(newValue, newValue.unix(), "startDatestartDatestartDate");
      this.errorFromDate = false;
      this.errorToDate = false;
      if (!newValue) {
        return;
      }
      if (this.endDate && newValue.unix() > this.endDate.unix()) {
        this.errorFromDate = true;
      }
    },
    endDate(newValue, oldValue) {
      this.errorFromDate = false;
      this.errorToDate = false;
      if (!newValue) {
        return;
      }
      if (this.startDate && newValue.unix() < this.startDate.unix()) {
        this.errorToDate = true;
      }
    },
    selectedType(newValue) {
      if (newValue == "CUSTOM_RANGE") {
        setTimeout(() => {
          let className = `id-${this.today.getFullYear()}-${
            this.today.getMonth() < 9
              ? "0" + (+this.today.getMonth() + 1)
              : +this.today.getMonth() + 1
          }-${
            this.today.getDate() < 10
              ? "0" + this.today.getDate()
              : this.today.getDate()
          }`;
          let element = document.getElementsByClassName(className);
          console.log(element);
          console.log(element[0]);
          if (element[0].children[0].tagName == "SPAN") {
            element[0].children[0].classList.add("today-range");
          } else {
            element[0].children[1].classList.add("today-range");
          }
        }, 100);
      }
    },
    range: {
      handler: function (newValue) {
        console.log(newValue);
        if (newValue != null) {
          if (newValue.start != null) {
            this.dateFrom = `${newValue.start.getFullYear()}-${
              newValue.start.getMonth() < 9
                ? "0" + (+newValue.start.getMonth() + 1)
                : +newValue.start.getMonth() + 1
            }-${
              newValue.start.getDate() < 10
                ? "0" + +newValue.start.getDate()
                : newValue.start.getDate()
            }`;
            this.range.startMessage = "valid";
          }
          if (newValue.end != null) {
            this.dateTo = `${newValue.end.getFullYear()}-${
              newValue.end.getMonth() < 9
                ? "0" + (+newValue.end.getMonth() + 1)
                : +newValue.end.getMonth() + 1
            }-${
              newValue.end.getDate() < 10
                ? "0" + +newValue.end.getDate()
                : newValue.end.getDate()
            }`;
            this.range.endMessage = "valid";
          }
        }
      },
      deep: true,
    },
    isOldGenIncluded: {
      handler(newValue) {
        console.log("isOldGenIncluded>newValue", newValue);
        const selectedItem = LIST_OF_DATA_FREQUENCY[newValue ? 1 : 0];
        this.dataFrequency = selectedItem.value;
        this.choosedDataFrequency = selectedItem.title;

        if (newValue)
          this.listOfDataFrequency = LIST_OF_DATA_FREQUENCY.filter(
            (o) => o.value !== "EVERY_SHOT"
          );
        else this.listOfDataFrequency = LIST_OF_DATA_FREQUENCY.slice();
      },
      immediate: true,
    },
  },
  mounted() {
    this.animationPrimary();
    this.animationOutline();
    console.log("custom-export-data-modal");
  },
};
</script>
<style>
.ant-modal-body {
  padding: 0;
  border-radius: 6px;
}

.ant-modal {
  width: fit-content !important;
}

.common-popover {
  min-width: unset !important;
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

.calendar-picker-input {
  width: 145px;
  height: 27px;
  color: #4b4b4b;
  border: 0.5px solid #52a1ff;
  border-radius: 2px;
  font-size: 13px;
  padding: 8px 25px 8px 7px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.calendar-picker-input::placeholder {
  color: #52a1ff;
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
  justify-content: space-around;
  height: 257px;
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

.block-button {
  display: block;
}

#export-data-modal .select-type-button {
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
  /* justify-content: space-between; */
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
}

.export-field-item_left {
  min-width: 120px;
}

.dropdown-wrap {
  width: 180px !important;
}

.pretty .state label:after,
.pretty .state label:before {
  top: unset !important;
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

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: 0;
  width: 100%;
}

.ant-modal {
  width: fit-content !important;
}

.dropdown-legend .checkbox-inactive input:checked ~ .state label:after {
  background-color: #c4c4c4 !important;
}

.dropdown-legend .checkbox-inactive input:checked ~ .state label::before {
  border-color: #c4c4c4 !important;
}
</style>

<style>
#export-data-modal .custom-modal-body {
  min-height: unset !important;
  width: fit-content !important;
}

@media (min-height: 800px) and (min-width: 1200px) {
  #export-data-modal .custom-modal-body {
    padding: 24px;
    min-height: 530px !important;
    width: 1024px !important;
  }
}

.ant-modal-wrap {
  z-index: 1100;
}
</style>
