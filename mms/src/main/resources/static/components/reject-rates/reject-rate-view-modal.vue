<template>
  <div
    id="op-reject-rate-view"
    class="modal fade"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div
          class="custom-modal-title"
          style="padding: 0px 30px 0px 0px; height: 57px"
        >
          <div class="modal-title" style="height: 100%">
            <span class="modal-title-text">View Reject Rate</span>
            <div class="d-flex modal-header__garp">
              <a
                ref="graph-btn"
                href="javascript:void(0)"
                class="header-button"
                :class="{
                  'header-button-active': selectedButton == 'graph-btn',
                }"
                style="
                  width: 132px;
                  height: 26px;
                  padding: 3px 0px;
                  text-align: center;
                  font-size: 16px;
                  border: 1px solid #d0d0d0;
                  font-weight: 400;
                "
                @click="buttonAnimation('graph-btn')"
                >Graph</a
              >
              <a
                ref="detail-btn"
                href="javascript:void(0)"
                class="header-button"
                :class="{
                  'header-button-active': selectedButton == 'detail-btn',
                }"
                style="
                  width: 132px;
                  height: 26px;
                  padding: 3px 0px;
                  text-align: center;
                  font-size: 16px;
                  border: 1px solid #d0d0d0;
                  font-weight: 400;
                "
                @click="buttonAnimation('detail-btn')"
                >Detail</a
              >
            </div>
          </div>
          <div
            class="t-close-button close-button"
            @click="dimissModal"
            aria-hidden="true"
          >
            <img src="/images/icon/close-modal.svg" />
          </div>
          <div class="head-line"></div>
        </div>
        <div class="modal-body" style="height: 560px">
          <div class="modal-body__title">
            <!-- <div class="chart-header">
              <div class="chart-header-tab"></div>
            </div> -->
            <div class="d-flex justify-content-between">
              <div
                v-if="selectedButton == 'graph-btn'"
                class="btn-group btn-group-toggle"
                data-toggle="buttons"
              >
                <label
                  class="btn switch-btn"
                  :class="{
                    active: chartType === chartTypeOption.YIELD_RATE,
                  }"
                  @click.prevent="changeChartType(chartTypeOption.YIELD_RATE)"
                >
                  <input
                    type="radio"
                    name="options"
                    :value="chartTypeOption.YIELD_RATE"
                    autocomplete="off"
                  />
                  <span v-text="resources['yield_trend']"></span>
                </label>
                <label
                  class="btn switch-btn"
                  :class="{ active: chartType === chartTypeOption.BREAKDOWN }"
                  @click.prevent="changeChartType(chartTypeOption.BREAKDOWN)"
                >
                  <input
                    type="radio"
                    name="options"
                    :value="chartTypeOption.BREAKDOWN"
                    autocomplete="off"
                  />
                  <span v-text="resources['reject_rate_breakdown']"></span>
                </label>
              </div>
              <div v-if="selectedButton == 'detail-btn'" class="form-item">
                <span style="margin-right: 32px" class="form-item__label">
                  Tooling ID
                </span>
                <span
                  style="
                    background: rgb(251, 252, 253);
                    width: 199px;
                    height: 24px;
                    padding: 4px 104px 2px 15px;
                  "
                  class="form-item__value"
                >
                  {{ currentToolingId }}
                </span>
              </div>
              <div class="date-picker">
                <div class="reject-rate-date-picker">
                  <!-- <date-picker-button
                    :date="tempDateData"
                    @click="openDatePickerModal"
                  ></date-picker-button> -->
                  <!-- <date-picker-button
                    :date="tempDateData"
                    @click="openDatePickerModal"
                    :is-range="false"
                    :prefix="'/images/icon/date-picker-before.svg'"
                    :hover-prefix="'/images/icon/date-picker-before-hover.svg'"
                    :prefix-position="'left'"
                    :custom-style="{ height: '31px' }"
                    :default-class="'btn-custom btn-outline-custom-primary customize-btn'"
                    :active-class="'outline-animation-2'"
                    :frequency="requestParam.frequent"
                    :parent="'modal'"
                  ></date-picker-button> -->
                  <date-picker-popup
                    @on-close="handleChangeDate"
                    :current-date="tempDateData"
                    :button-type="'primary'"
                    :date-range="timeRange"
                    :default-hour="hour"
                    :current-frequency="requestParam.frequent"
                  ></date-picker-popup>
                </div>
              </div>
            </div>
          </div>
          <div v-show="selectedButton == 'graph-btn'">
            <div class="chart-content">
              <div v-if="false" class="icon-wrapper">
                <a-icon
                  v-show="isShowChangeTimeAction"
                  style="font-size: 23px"
                  class="icon"
                  type="caret-left"
                  @click.prevent="changeTime(timeChangeType.PREV)"
                ></a-icon>
              </div>
              <div class="reject-chart">
                <canvas id="reject-rate-chart-view"></canvas>
                <div
                  id="reject-rate-chart-view-legend"
                  v-if="chartType === chartTypeOption.YIELD_RATE"
                >
                  <div class="line-legend-wrapper">
                    <div class="legend-icon">
                      <img
                        style="width: 54px; height: 8px"
                        src="/images/graph/shot-icon.svg"
                      />
                    </div>
                    <div class="legend-title">
                      <span v-text="resources['yield_rate']"></span>
                    </div>
                  </div>
                </div>
                <div
                  id="reject-rate-chart-view-legend"
                  v-if="chartType === chartTypeOption.BREAKDOWN"
                >
                  <div class="line-legend-wrapper">
                    <div class="legend-icon">
                      <div class="icon-line"></div>
                      <div class="icon-line"></div>
                      <div class="icon-line"></div>
                      <div class="icon-line"></div>
                    </div>
                    <div class="legend-title">
                      <span v-text="resources['cum_percentage_curve']"></span>
                    </div>
                  </div>
                  <div class="bar-legend-wrapper">
                    <div
                      class="legend-item"
                      v-for="(legend, index) in breakdownBarLegends"
                      :key="index"
                    >
                      <div
                        class="legend-icon"
                        :style="{ backgroundColor: legend.color }"
                      ></div>
                      <div class="legend-title">{{ legend.name }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="false" class="icon-wrapper">
                <a-icon
                  v-show="isShowChangeTimeAction && isShowIcon"
                  @click.prevent="changeTime(timeChangeType.NEXT)"
                  style="font-size: 23px"
                  class="icon"
                  type="caret-right"
                ></a-icon>
              </div>
            </div>
          </div>
          <div v-show="selectedButton == 'detail-btn'">
            <time-data-table
              :table-headers="tableHeaders"
              :date="getListTableTitle"
              :resources="resources"
              :table-fields="tableFields"
              :type="requestParam.frequent"
            ></time-data-table>
          </div>
        </div>
      </div>
    </div>
    <!-- <date-picker-modal
      ref="date-picker-modal"
      :resources="resources"
      :select-option="timeOptions"
      @change-date="handleChangeDate"
    ></date-picker-modal> -->
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    timeOptions: Array,
  },
  components: {
    "date-picker": httpVueLoader(
      "/components/reject-rates/components/date-picker.vue"
    ),
    "date-picker-button": httpVueLoader(
      "/components/common/date-picker/date-picker-button.vue"
    ),
    "time-data-table": httpVueLoader(
      "/components/reject-rates/components/time-data-table.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/common/date-picker/date-picker-modal.vue"
    ),
    "date-picker-popup": httpVueLoader(
      "/components/reject-rates/date-picker-popup.vue"
    ),
  },
  data() {
    return {
      selectedButton: "graph-btn",
      tempDateData: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      rejectPart: {},
      requestParam: {
        moldId: null,
        partId: null,
        startDate: null,
        frequent: "",
      },
      chartType: null,
      chartTypeOption: {
        YIELD_RATE: "YIELD_RATE",
        BREAKDOWN: "BREAKDOWN",
      },
      timeChangeType: {
        NEXT: "NEXT",
        PREV: "PREV",
      },
      initTimePeriod: null,
      initPeriodType: null,
      selectedTimeLabel: null,
      chart: null,
      chartConfig: null,
      dateFormat: "YYYYMMDD",
      breakdownColors: [
        "#f1cc68",
        "#8fe6d5",
        "#a2daf1",
        "#e67c7a",
        "#b3225d",
        "#8bb487",
        "#5a4645",
        "#808080",
        "#f85a51",
        "#7383c1",
        "#795cc8",
        "#efeeee",
      ],
      tableHeaders: [],
      tableData: [],
      tableFields: {
        totalProducedAmount: this.resources["produced_part"],
        totalRejectedAmount: this.resources["rejected_part"],
        yieldRate: this.resources["yield_rate"],
        rejectedRate: this.resources["rejected_rate"],
        editedBy: this.resources["edited_by"],
      },
      breakdownBarLegends: [],
      isShowChangeTimeAction: true,
      isShowIcon: true,
      currentToolingCode: null,
      rangeGap: 1,
      hour: {},
    };
  },
  created() {
    // this.timeOption = ["DAILY_RANGE", "WEEKLY_RANGE", "MONTHLY_RANGE"];
    this.months = [
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
    ];
    this.timeRange = {
      minDate: null,
      maxDate: new Date("2100-01-01"),
    };
  },
  computed: {
    getFrequency() {
      return this.requestParam.frequent.replaceAll("_RANGE", "");
    },
    currentToolingId() {
      if (this.currentToolingCode) {
        return this.currentToolingCode;
      }
      return;
    },
    getTitle() {
      let dateData = { ...this.tempDateData };
      if (this.requestParam.frequent.includes("MONTHLY")) {
        dateData.fromTitle = moment(dateData.from).format("YYYY-MMM");
        dateData.toTitle = moment(dateData.to).format("YYYY-MMM");
      }
      return `${dateData.fromTitle} - ${dateData.toTitle}`;
    },
    getListTableTitle() {
      let days = this.getDateBetween();
      return days;
    },
  },
  watch: {
    tableData(newValue) {
      let child = Common.vue.getChild(this.$children, "time-data-table");
      if (child != null) {
        child.initTable(newValue);
      }
    },
  },
  methods: {
    showRejectDetail(data, passParam, dateData) {
      $("#op-reject-rate-view").modal("show");
      console.log("open modal", data, passParam, dateData);
      // if (this.rejectPart.id === data.id) {
      //   // keep previous data
      //   return;
      // }
      if (data.mold) {
        this.currentToolingCode = data.mold.equipmentCode;
      }
      this.rejectPart = data;
      this.tempDateData = dateData;
      this.tableData = [];
      this.requestParam.moldId = data.moldId;
      this.requestParam.partId = data.partId;
      this.requestParam.frequent = passParam.currentParam.frequent;
      this.requestParam.start = passParam.startDate;
      this.requestParam.end = passParam.endDate;
      switch (passParam.currentParam.frequent) {
        case "WEEKLY":
          delete this.requestParam.month;
          delete this.requestParam.day;
          this.requestParam.startDate = moment(this.tempDateData.from).format(
            "YYYYMMDD"
          );
          break;
        case "MONTHLY":
          delete this.requestParam.day;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          break;
        case "DAILY":
          delete this.requestParam.month;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          break;
        case "HOURLY":
          delete this.requestParam.month;
          delete this.requestParam.week;
          delete this.requestParam.startDate;
          delete this.requestParam.day;
          this.hour = passParam.hour;
          this.requestParam.hour = `${moment(this.tempDateData.from).format(
            "YYYYMMDD"
          )}${passParam.hour.from}`;
          break;
      }
      this.chartType = this.chartTypeOption.YIELD_RATE;
      var child = this.$refs["date-picker-modal"];
      if (child != null) {
        console.log("getDefaultData", this.requestParam);
        child.getDefaultData(this.requestParam.frequent, this.tempDateData);
      }
      //   this.setDefaultStartDateAndEndDate(passParam.startDate);
      this.bindDetailData();
    },
    dimissModal: function () {
      $("#op-reject-rate-view").modal("hide");
    },
    buttonAnimation(id) {
      const el = this.$refs[id];
      if (el) {
        el.classList.add("header-button-primary-animation");
        setTimeout(() => {
          el.classList.remove("header-button-primary-animation");
          if (id == "detail-btn") {
            let child = Common.vue.getChild(this.$children, "time-data-table");
            if (child != null) {
              child.initTable(this.tableData);
            }
          }
          this.selectedButton = id;
        }, 700);
      }
    },
    openDatePickerModal() {
      let child = Common.vue.getChild(this.$children, "date-picker-modal");
      if (child != null) {
        child.showDatePicker(this.requestParam.frequent, this.tempDateData);
      }
    },
    handleChangeDate(data, frequency) {
      console.log("change date");
      if (
        !(
          this.tempDateData.fromTitle == data.from &&
          this.requestParam.frequent == frequency
        )
      ) {
        console.log("run there!");
        this.tempDateData = data;
        this.requestParam.start = data.fromTitle.replaceAll("-", "");
        this.requestParam.end = data.toTitle.replaceAll("-", "");
        switch (frequency) {
          case "WEEKLY":
            delete this.requestParam.month;
            delete this.requestParam.day;
            this.requestParam.startDate = moment(data.from).format("YYYYMMDD");
            break;
          case "MONTHLY":
            delete this.requestParam.day;
            delete this.requestParam.week;
            delete this.requestParam.startDate;
            break;
          case "DAILY":
            delete this.requestParam.month;
            delete this.requestParam.week;
            delete this.requestParam.startDate;
            break;
        }
        delete this.requestParam.endDate;
        this.requestParam.frequent = frequency;
        switch (frequency) {
          case "DAILY":
            this.rangeGap =
              moment(this.tempDateData.to).diff(this.tempDateData.from, "day") +
              1;
            console.log("day gap", this.rangeGap);
            break;
          case "WEEKLY":
            let gap = +moment(this.tempDateData.to).diff(
              this.tempDateData.from,
              "day"
            );
            this.rangeGap = Math.floor(gap / 7);
            console.log("weekly gap", gap, this.rangeGap);
            break;
          case "MONTHLY":
            this.rangeGap =
              moment(this.tempDateData.to).diff(
                this.tempDateData.from,
                "month"
              ) + 1;
            console.log("month gap", this.rangeGap);
            break;
        }
        console.log("change date after", this.requestParam);
        this.bindDetailData();
      }
      let child = Common.vue.getChild(this.$children, "date-picker-modal");
      if (child != null) {
        child.closeDatePicker();
      }
    },
    changeDateFilter(dateParam) {
      if (dateParam.startDate && dateParam.endDate) {
        this.requestParam.startDate = dateParam.startDate;
        this.requestParam.endDate = dateParam.endDate;
        this.bindDetailData();
      }
    },
    changeTime(changeType) {
      let format = "";
      let step = "";
      let param = { ...this.requestParam };
      console.log("changeTime aaaa", param.frequent);
      switch (param.frequent) {
        case "DAILY":
          format = "YYYY-MM-DD";
          step = "day";
          break;
        case "WEEKLY":
          format = "YYYY-ww";
          step = "week";
          break;
        case "MONTHLY":
          format = "YYYY-MM";
          step = "month";
          break;
      }

      let newDateData = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      };

      let child = Common.vue.getChild(this.$children, "date-picker-modal");
      if (changeType == this.timeChangeType.NEXT) {
        if (step == "week") {
          let current = { ...this.tempDateData };
          newDateData.from = moment(current.from)
            .add(this.rangeGap * 7, "days")
            .toDate();
          newDateData.to = moment(current.to)
            .add(this.rangeGap * 7, "days")
            .toDate();
          let firstWeek = this.getCurrentWeekNumber(newDateData.from);
          let secondWeek = this.getCurrentWeekNumber(newDateData.to);
          newDateData.fromTitle = `${newDateData.from.getFullYear()}-${
            firstWeek > 9 ? firstWeek : "0" + firstWeek
          }`;
          newDateData.toTitle = `${newDateData.to.getFullYear()}-${
            secondWeek > 9 ? secondWeek : "0" + secondWeek
          }`;
          console.log("next", firstWeek, secondWeek);
        } else {
          newDateData.fromTitle = moment(this.tempDateData.fromTitle, format)
            .add(this.rangeGap, step + "s")
            .format(format);
          newDateData.toTitle = moment(this.tempDateData.toTitle, format)
            .add(this.rangeGap, step + "s")
            .format(format);
          newDateData.from = moment(newDateData.fromTitle, format).toDate();
          newDateData.to = moment(newDateData.toTitle, format).toDate();
        }
      } else {
        if (step == "week") {
          let current = { ...this.tempDateData };
          newDateData.from = moment(current.from)
            .subtract(this.rangeGap * 7, "days")
            .toDate();
          newDateData.to = moment(current.to)
            .subtract(this.rangeGap * 7, "days")
            .toDate();
          let firstWeek = this.getCurrentWeekNumber(newDateData.from);
          let secondWeek = this.getCurrentWeekNumber(newDateData.to);
          newDateData.fromTitle = `${newDateData.from.getFullYear()}-${
            firstWeek > 9 ? firstWeek : "0" + firstWeek
          }`;
          newDateData.toTitle = `${newDateData.to.getFullYear()}-${
            secondWeek > 9 ? secondWeek : "0" + secondWeek
          }`;
          console.log("back", firstWeek, secondWeek);
        } else {
          newDateData.fromTitle = moment(this.tempDateData.fromTitle, format)
            .subtract(this.rangeGap, step + "s")
            .format(format);
          newDateData.toTitle = moment(this.tempDateData.toTitle, format)
            .subtract(this.rangeGap, step + "s")
            .format(format);
          newDateData.from = moment(newDateData.fromTitle, format).toDate();
          newDateData.to = moment(newDateData.toTitle, format).toDate();
        }
      }
      if (child != null) {
        child.handleChangeDate(
          { start: newDateData.from, end: newDateData.to },
          { start: newDateData.fromTitle, end: newDateData.toTitle }
        );
        child.submitDate();
      }
      console.log("after change next or prev", this.tempDateData, newDateData);
      // this.handleChangeDate(newDateData);
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      if (oneJan.getDay() > 3) {
        let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      } else {
      }
      return result;
    },
    changeChartType(chartType) {
      this.chartType = chartType;
      this.bindDetailData();
    },
    bindDetailData() {
      if (!this.requestParam.moldId || !this.requestParam.partId) {
        return;
      }
      let param = Common.param(this.requestParam);
      axios
        .get("/api/rejected-part?" + param)
        .then((response) => {
          this.bindTableData(response.data.content);
          if (this.chartType === this.chartTypeOption.YIELD_RATE) {
            this.bindYieldRateChart(response.data.content);
          }
        })
        .catch((error) => {
          console.log(error);
        });
      if (this.chartType === this.chartTypeOption.BREAKDOWN) {
        axios
          .get("/api/rejected-part/breakdown-chart?" + param)
          .then((response) => {
            console.log("bindBreakdownChart", response.data);
            this.bindBreakdownChart(response.data);
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    setDefaultStartDateAndEndDate(startDate) {
      let previousWeek = moment(startDate, this.dateFormat).week() - 1;
      if (previousWeek === moment().week()) {
        previousWeek = previousWeek - 1;
      }
      let year = moment(startDate, this.dateFormat).year();
      if (previousWeek < 1) {
        year = year - 1;
        previousWeek = moment(year + "-31-12", "YYYY-MM-DD").week() - 1;
      }
      this.requestParam.startDate = moment()
        .week(previousWeek)
        .year(year)
        .weekday(1)
        .format(this.dateFormat);
      this.requestParam.endDate = moment()
        .week(previousWeek)
        .year(year)
        .weekday(7)
        .format(this.dateFormat);
      let child = Common.vue.getChild(this.$children, "date-detail-filter");
      if (child != null) {
        child.initOptionValue(previousWeek, year);
      }
    },
    showChangeTimeAction(status) {
      this.isShowChangeTimeAction = status;
    },
    hideIcon(pload) {
      this.isShowIcon = pload;
    },
    changeSelectedTimeLabel(label) {
      this.selectedTimeLabel = label;
    },
    // format YYYYMMDD
    enumerateDaysBetweenDates(startDate, endDate, frequent) {
      console.log("enumerateDaysBetweenDates", startDate, endDate, frequent);

      if (this.requestParam.frequent != "WEEKLY") {
        let currDate = null;
        let lastDate = null;
        let format = "";
        let step = "";
        let numberOfDate = 0;
        switch (this.requestParam.frequent) {
          case "DAILY":
            format = "MM/DD";
            step = "days";
            currDate = moment(startDate, "YYYY-MM-DD").startOf("day");
            lastDate = moment(endDate, "YYYY-MM-DD").startOf("day");
            numberOfDate = lastDate.diff(currDate, "days") + 1;
            break;
          case "WEEKLY":
            format = "YYYY/WW";
            step = "weeks";
            currDate = moment(startDate, "YYYY-ww").startOf("weeks");
            lastDate = moment(endDate, "YYYY-ww").startOf("weeks");
            console.log("check date weekly", currDate, lastDate);
            numberOfDate = lastDate.diff(currDate, "weeks") + 1;
            break;
          case "MONTHLY":
            format = "YYYY/MM";
            step = "months";
            currDate = moment(startDate, "YYYY-MM").startOf("month");
            lastDate = moment(endDate, "YYYY-MM").startOf("month");
            numberOfDate = lastDate.diff(currDate, "months") + 1;
            break;
        }

        console.log(
          "enumerateDaysBetweenDates 2",
          currDate,
          lastDate,
          numberOfDate
        );

        let response = [];
        for (let index = 0; index < numberOfDate; index++) {
          response.push(currDate.format(format));
          currDate.add(1, step);
        }
        // while (currDate.diff(lastDate) < 0) {
        //   response.push(currDate.format(this.dateFormat));
        //   currDate.add(1, "days");
        // }
        console.log(response, "respone");
        return response;
      } else {
        let firstYear = startDate.split("-")[0];
        let firstWeek = startDate.split("-")[1];
        let secondYear = endDate.split("-")[0];
        let secondWeek = endDate.split("-")[1];
        let response = [];

        if (firstYear == secondYear) {
          while (firstWeek <= secondWeek) {
            response.push(
              `${firstYear}/${+firstWeek > 9 ? +firstWeek : "0" + +firstWeek}`
            );
            firstWeek = +firstWeek + 1;
          }
        } else {
          let yearGarp = secondYear - firstYear;
          for (
            let index = firstWeek;
            index <= this.getNumberOfWeek(firstYear);
            index++
          ) {
            response.push(`${firstYear}/${index > 9 ? index : "0" + index}`);
          }
          if (yearGarp > 1) {
            for (let i = 1; i < yearGarp; i++) {
              for (let j = 1; j < this.getNumberOfWeek(firstYear + i); j++) {
                response.push(`${firstYear + i}/${j > 9 ? j : "0" + j}`);
              }
            }
          }
          for (let index = 0; index <= secondWeek; index++) {
            response.push(`${secondYear}/${index > 9 ? index : "0" + index}`);
          }
        }
        console.log(response, "respone");
        return response;
      }
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
    },
    getNumberOfWeek(currentYear) {
      let result = this.getCurrentWeekNumber(
        moment("12-31-" + currentYear, "MM-DD-YYYY").toDate()
      );
      if (
        moment("01-01-" + currentYear, "MM-DD-YYYY")
          .toDate()
          .getDay() >= 4
      ) {
        result--;
      }
      if (
        moment("12-31-" + currentYear, "MM-DD-YYYY")
          .toDate()
          .getDay() < 4
      ) {
        result--;
      }
      console.log("Number of week", result);
      return result;
    },
    getDateBetween() {
      let startDate = null;
      let endDate = null;
      if (["MONTHLY", "WEEKLY", "DAILY"].includes(this.requestParam.frequent)) {
        startDate = this.tempDateData.fromTitle;
        endDate = this.tempDateData.toTitle;
      }
      let days = this.enumerateDaysBetweenDates(
        startDate,
        endDate,
        this.requestParam.frequent
      );
      return days;
    },
    bindTableData(tableData) {
      tableData.sort((first, second) => {
        return first.day > second.day ? 1 : -1;
      });
      this.tableHeaders = [];
      this.tableData = {};

      let days = this.getDateBetween();
      days.forEach((item) => {
        this.tableHeaders.push(item);
      });

      Object.keys(this.tableFields).forEach((field) => {
        this.tableData[field] = [];
        this.tableHeaders.forEach(() => {
          this.tableData[field].push("-");
        });
      });
      tableData.forEach((item) => {
        let date = null;
        if (this.requestParam.frequent.includes("WEEKLY")) {
          // date = item.week
          date = moment(item.week, this.dateFormat).format("MM/DD");
        } else if (this.requestParam.frequent.includes("MONTHLY")) {
          // date = item.month
          date = moment(item.month, this.dateFormat).format("MM/DD");
        } else if (this.requestParam.frequent.includes("DAILY")) {
          date = moment(item.day, this.dateFormat).format("MM/DD");
        }
        // let date = moment(item.day, this.dateFormat).format("MM/DD");
        let dataIndex = null;

        this.tableHeaders.forEach((header, index) => {
          console.log(date, "date");
          console.log(header, "header");
          // if (header === date) {
          //   dataIndex = index;
          // }
          dataIndex = index;
        });
        Object.keys(this.tableFields).forEach((field) => {
          let value = item[field];
          if (field.endsWith("Rate")) {
            if (value !== null) {
              value = value + "%";
            }
          }
          if (dataIndex !== null) {
            this.tableData[field][dataIndex] = value;
          }
        });
      });
      this.$forceUpdate();
    },
    setForTest() {
      ["Off set", "Short Mould", "Dented", "Flow Mark"].forEach(
        (item, index) => {
          this.breakdownBarLegends.push({
            name: item,
            color: this.breakdownColors[index],
          });
        }
      );
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
    },
    getNumberOfWeek(year) {
      let result = this.getCurrentWeekNumber(
        moment("12-31-" + year, "MM-DD-YYYY").toDate()
      );
      if (
        moment("01-01-" + year, "MM-DD-YYYY")
          .toDate()
          .getDay() >= 4
      ) {
        result--;
      }
      if (
        moment("12-31-" + year, "MM-DD-YYYY")
          .toDate()
          .getDay() < 4
      ) {
        result--;
      }
      return result;
    },
    bindYieldRateChart(chartData) {
      this.resetConfigForYieldRateChart();
      this.chartConfig.data.labels = [];
      this.chartConfig.data.datasets = [];
      this.chart.update();
      let dataset = {
        type: "line",
        label: this.resources["yield_rate"],
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      // sort
      chartData.sort((first, second) => {
        return first.day > second.day ? 1 : -1;
      });
      let days = this.getDateBetween();
      console.log("Date Between", days);
      days.forEach((day) => {
        // let date = moment(day, this.dateFormat).format("MM/DD");
        // if (this.requestParam.frequent.includes("DAILY")) {
        //   this.chartConfig.data.labels.push(day);
        // } else {
        //   this.chartConfig.data.labels.push(this.getTitle);
        // }
        this.chartConfig.data.labels.push(day);
        dataset.data.push(null);
      });
      console.log("chartData", chartData);
      chartData.forEach((item) => {
        let date = this.getTitle;
        if (this.requestParam.frequent.includes("DAILY")) {
          date = moment(item.day, this.dateFormat).format("MM/DD");
        } else if (this.requestParam.frequent.includes("WEEKLY")) {
          date = item.week;
        } else if (this.requestParam.frequent.includes("MONTHLY")) {
          date = item.month;
        }
        let dataIndex = null;
        this.chartConfig.data.labels.forEach((label, index) => {
          // if (label === date) {
          //   dataIndex = index;
          // }
          console.log("index", index);
          dataIndex = index;
        });
        if (dataIndex !== null) {
          dataset.data[dataIndex] = item.yieldRate < 100 ? item.yieldRate : 100;
        }
      });

      // dataset.data.push(dataset);
      this.chartConfig.data.datasets.push(dataset);
      this.chart.update();
    },
    bindBreakdownChart(chartData) {
      chartData = chartData.filter((item) => item.rejectedAmount);
      chartData.sort((first, second) => {
        return first.rejectedRate < second.rejectedRate ? 1 : -1;
      });
      this.resetConfigForBreakdownChart();
      this.chartConfig.data.labels = [];
      this.chartConfig.data.datasets = [];
      this.chart.update();

      this.breakdownBarLegends = [];
      let tmpRejectedRateRate = 0;
      // line chart
      let lineDataset = {
        type: "line",
        label: this.resources["cum_percentage_curve"],
        yAxisID: "right",
        backgroundColor: "transparent",
        borderColor: "black",
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      let dataset = {
        type: "bar",
        label: "",
        yAxisID: "left",
        backgroundColor: [],
        borderColor: [],
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };
      chartData.forEach((item, index) => {
        let color = this.breakdownColors[index];
        if (!color) {
          color = Common.getRandomElement(this.breakdownColors);
        }

        // bar chart

        tmpRejectedRateRate += item.rejectedRate;
        if (tmpRejectedRateRate > 100) {
          tmpRejectedRateRate = 100;
        }

        lineDataset.data.push(tmpRejectedRateRate);

        dataset.backgroundColor.push(color);
        dataset.borderColor.push(color);
        dataset.data.push(item.rejectedAmount);
        this.chartConfig.data.labels.push(item.reason);
        this.breakdownBarLegends.push({
          name: item.reason,
          color: color,
        });
      });

      this.chartConfig.data.datasets.push(lineDataset);
      this.chartConfig.data.datasets.push(dataset);
      this.chart.update();
    },
    setupRejectChart() {
      this.chartConfig = this.getChartConfig();
      this.chart = new Chart($("#reject-rate-chart-view"), this.chartConfig);
    },
    getChartConfig() {
      return {
        type: "bar",
        data: {
          labels: [],
          datasets: [],
        },
        options: {
          maintainAspectRatio: false,
          legend: {
            display: true,
            position: "bottom",
          },
          tooltips: {
            mode: "index",
            intersect: false,
          },
          scales: {
            xAxes: [
              {
                scaleLabel: {
                  display: true,
                },
              },
            ],
            yAxes: [
              {
                stacked: false,
                position: "left",
                id: "left",
                ticks: {
                  beginAtZero: true,
                  min: 0,
                },
                scaleLabel: {
                  display: true,
                },
              },
            ],
          },
          elements: {
            point: {
              radius: 0,
              hitRadius: 10,
              hoverRadius: 4,
              hoverBorderWidth: 3,
            },
          },
        },
      };
    },
    resetConfigForYieldRateChart() {
      if (this.chartConfig && this.chartConfig.options) {
        delete this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString;
      }
      this.chartConfig.options.scales.xAxes = [
        {
          scaleLabel: {
            display: true,
          },
        },
      ];

      this.chartConfig.options.scales.yAxes = [
        {
          stacked: false,
          position: "left",
          id: "left",
          ticks: {
            beginAtZero: true,
            callback: function (value) {
              if (value > 100) return null;
              return value + "%";
            },
            min: 0,
            max: 105,
          },
          scaleLabel: {
            display: true,
          },
        },
      ];

      this.chartConfig.options.legend.display = false;
      this.chartConfig.options.legend.legendCallback = function (chart) {
        let text = `<div>
              <div class="legend-item" id="legend-item-0">
                  <div class="chart-legend">
                      <div class="legend-icon" style="height: 1px; border-top: 3px solid #63C2DE"></div>
                      <div class="label">Yield Rate</div>
                  </div>
              </div>
              <div class="clear"></div>
          </div>`;
        return text;
      };

      this.chartConfig.options.tooltips = {
        mode: "index",
        intersect: false,
        callbacks: {
          afterLabel: function () {
            return "";
          },
          label: function (tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel)
              .toFixed(2)
              .replace(".00", "")
              .toString();
            return `${label}: ${value}%`;
          },
        },
      };
      console.log("config", this.chartConfig);
    },
    resetConfigForBreakdownChart() {
      this.chartConfig.options.scales.xAxes = [
        {
          scaleLabel: {
            display: false,
          },
          ticks: {
            display: false,
          },
        },
      ];
      this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString =
        "Frequency";
      this.chartConfig.options.scales.yAxes = [
        {
          stacked: false,
          position: "left",
          id: "left",
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          scaleLabel: {
            display: true,
            labelString: this.resources["frequency"],
          },
        },
        {
          stacked: false,
          id: "right",
          position: "right",
          ticks: {
            beginAtZero: true,
            callback: function (value) {
              if (value > 100) return null;
              return value + "%";
            },
            min: 0,
            max: 105,
          },
          gridLines: {
            display: false,
          },
          scaleLabel: {
            display: true,
            labelString: this.resources["cum_percentage"],
          },
        },
      ];
      this.chartConfig.options.legend.display = false;
      this.chartConfig.options.tooltips = {
        enabled: true,
        intersect: true,
        callbacks: {
          afterLabel: function () {
            return "";
          },
          title: function () {},
          label: function (tooltipItem, data) {
            let label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel)
              .toFixed(2)
              .replace(".00", "")
              .toString();
            if (tooltipItem.datasetIndex === 1) {
              label = tooltipItem.xLabel;
              return `${label}: ${value}`;
            }
            return `${label}: ${value}%`;
          },
        },
      };
    },
  },
  mounted() {
    this.chartType = this.chartTypeOption.YIELD_RATE;
    this.setupRejectChart();
  },
};
</script>
<style>
.ant-select-dropdown {
  z-index: 99999999;
}
.ant-select-dropdown-menu-item-selected {
  font-weight: 400;
  background-color: #fff;
}
.custom-modal-body-user {
  padding: 23px 32px 26px 32px;
  overflow-y: auto;
  max-height: 600px;
}
.head-line {
  left: 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title {
  font-weight: bold;
  font-size: 16px;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
  background: #f5f8ff;
  color: #4b4b4b;
  display: flex;
  justify-content: space-between;
  position: relative;
  align-items: end;
  padding: 8px 15px 0 0;
  align-items: center;
}
.modal-title-text {
  margin-right: 15px;
}
.header-button {
  padding: 5px 40px;
  font-size: 16px;
  border: 1px solid #d0d0d0;
  background-color: #fff;
  color: #888888;
  border-radius: 3px;
  text-decoration: none !important;
  font-weight: 500;
  line-height: 18px;
}
.header-button:first-child {
  margin-right: 6px;
}
.header-button:hover {
  color: #3491ff;
  background-color: #f2f5fa;
  border: 1px solid #3491ff;
}
.header-button-active {
  background-color: #3491ff !important;
  color: #fff !important;
  border: 1px solid #3491ff;
}
.header-button-primary-animation {
  animation: header-button-primary 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  border: 1px solid transparent;
  outline: 1px solid transparent;
}
@keyframes header-button-primary {
  0% {
  }
  33% {
    color: #0279fe;
    outline: 3px solid #89d1fd;
  }
  66% {
    color: #0279fe;
    outline: 3px solid #89d1fd;
  }
  100% {
  }
}
.header-button-disable {
  opacity: 0.7;
  pointer-events: none;
}
.title__garp {
  margin: 20px 0;
}
.modal-header__garp {
  margin-left: 49.5px;
}
.reject-chart {
  margin-top: 20px;
}
</style>
<style scoped src="/components/reject-rates/reject-rate-modal.css"></style>
