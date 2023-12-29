<template>
  <div>
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
        style="padding: 21px 24px 16px 24px"
      >
        <div class="export-field-item">
          <div class="export-field-item_left">
            <span class="export-filed-title">Data Frequency</span>
          </div>
          <div class="export-field-item_right">
            <div class="choosing-items">
              <div class="choosing-item">
                <p-radio
                  :checked="dataFrequency === 'EVERY_SHOT'"
                  @change="chooseDataFrequency('EVERY_SHOT')"
                >
                  <span @click="chooseDataFrequency('EVERY_SHOT')">
                    Every Shot
                  </span>
                </p-radio>
              </div>
            </div>
            <div class="choosing-items">
              <div class="choosing-item">
                <p-radio
                  :checked="dataFrequency === 'HOUR'"
                  @change="chooseDataFrequency('HOUR')"
                >
                  <span @click="chooseDataFrequency('HOUR')"> Hourly </span>
                </p-radio>
              </div>
              <div class="choosing-item">
                <p-radio
                  :checked="dataFrequency === 'DAY'"
                  @change="chooseDataFrequency('DAY')"
                >
                  <span @click="chooseDataFrequency('DAY')"> Daily </span>
                </p-radio>
              </div>
            </div>
            <div style="margin-bottom: 0" class="choosing-items">
              <div class="choosing-item">
                <p-radio
                  :checked="dataFrequency === 'WEEK'"
                  @change="chooseDataFrequency('WEEK')"
                >
                  <span @click="chooseDataFrequency('WEEK')"> Weekly </span>
                </p-radio>
              </div>
              <div class="choosing-item">
                <p-radio
                  :checked="dataFrequency === 'MONTH'"
                  @change="chooseDataFrequency('MONTH')"
                >
                  <span @click="chooseDataFrequency('MONTH')"> Monthly </span>
                </p-radio>
              </div>
            </div>
          </div>
        </div>

        <div class="export-field-item">
          <div class="export-field-item_left">
            <span class="export-filed-title">Data Type</span>
          </div>
          <div class="export-field-item_right">
            <div class="choosing-items">
              <div class="choosing-item">
                <!--                  <label>-->
                <!--                    <input type="checkbox" disabled>-->
                <p-check
                  :checked="dataType.includes('SHORT_COUNT')"
                  @change="chooseDataType($event, 'SHORT_COUNT')"
                  key="SHORT_COUNT"
                >
                  <!-- <span @click="chooseDataType2('SHORT_COUNT')">Shot Count</span> -->
                  <span>Shot Count</span>
                </p-check>
                <!--                    <span>Shot Count</span>-->
                <!--                  </label>-->
              </div>
              <div class="choosing-item">
                <!--                @change="setDataType($event, 'CYCLE_TIME')"-->
                <p-check
                  :checked="dataType.includes('CYCLE_TIME')"
                  @change="chooseDataType($event, 'CYCLE_TIME')"
                  key="CYCLE_TIME"
                >
                  <!-- <span @click="chooseDataType2('CYCLE_TIME')"> Cycle Time </span> -->
                  <span> Cycle Time </span>
                </p-check>
              </div>
            </div>
            <div style="margin-bottom: 0" class="choosing-items">
              <div class="choosing-item">
                <!--                  <label>-->
                <p-check
                  :checked="dataType.includes('UPTIME')"
                  key="UPTIME"
                  @change="chooseDataType($event, 'UPTIME')"
                >
                  <span>Uptime</span>
                </p-check>
                <!--                    <span>Uptime</span>-->
                <!--                  </label>-->
              </div>

              <div class="choosing-item">
                <!--                  <label>-->
                <!--                    <input type="checkbox" disabled>-->
                <p-check
                  :checked="dataType.includes('TEMPERATURE')"
                  key="TEMPERATURE"
                  @change="chooseDataType($event, 'TEMPERATURE')"
                >
                  <span>Temperature</span>
                </p-check>
                <!--                    <span>Temperature</span>-->
                <!--                  </label>-->
              </div>
            </div>
          </div>
        </div>

        <div
          style="margin-bottom: 0 !important"
          class="export-field-item row-time"
        >
          <div class="export-field-item_left">
            <span class="export-filed-title">Time Range</span>
          </div>
          <div class="export-field-item_right">
            <div
              @click="showCalendar()"
              style="display: flex; position: relative"
            >
              <div
                style="
                  display: flex;
                  flex-direction: column;
                  margin-right: 10px;
                "
              >
                <a-input
                  id="date-picker"
                  type="text"
                  class="calendar-picker-input"
                  readonly
                  placeholder="Pick a time range"
                  v-model="displayDatePicker"
                />
              </div>
              <div
                style="
                  position: absolute;
                  top: 50%;
                  right: 23px;
                  transform: translate(0px, -50%);
                "
                type="calendar"
              >
                <img
                  width="12.57px"
                  height="12.57px"
                  src="/images/icon/calendar_date_picker.svg"
                />
              </div>

              <!-- <div style="display: flex; flex-direction: column; width: 40%">
                <div style="font-size: 13px" v-text="resources['to']"></div>
                <a-date-picker
                  :class="{ errorFieldName: errorToDate }"
                  :allow-clear="false"
                  format="YYYY-MM-DD"
                  v-model="endDate"
                ></a-date-picker>
                               :default-value="defaultValue"
                <div
                  v-if="errorToDate"
                  style="
                    font-size: 11px;
                    color: #ef4444;
                    text-align: left;
                    line-height: 14px;
                  "
                  v-text="resources['To_date_must_be_later_than_From_date']"
                ></div>
              </div> -->
            </div>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 9px">
          <a
            v-if="checkEnable"
            id="btn-export"
            @click="exportDynamic"
            class="btn-custom btn-custom-primary btn-custom-date animationPrimary animationOutline2"
          >
            <span v-text="resources['export']"></span>
          </a>
          <a
            v-if="!checkEnable"
            class="disable-cta btn-custom btn-custom-primary btn-custom-date animationPrimary animationOutline2"
          >
            <span v-text="resources['export']"></span>
          </a>
        </div>
      </div>
      <div
        v-if="calendarShow"
        class="custom-modal-body"
        style="padding: 16px 24px"
      >
        <div class="custom-select-date-picker">
          <div class="container-date-pick">
            <div
              v-if="['CUSTOM_RANGE', 'DAILY'].includes(selectedType)"
              class="custom-range block"
              :style="{
                'margin-right': dataFrequency === 'EVERY_SHOT' ? '0' : '50px',
              }"
            >
              <div>
                <v-date-picker
                  v-model="range"
                  locale="us-US"
                  :is-range="isRange"
                  :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
                  :first-day-of-week="2"
                  :max-date="today"
                  :key="customKey"
                  :available-dates="{
                    start: null,
                    end: new Date(),
                  }"
                ></v-date-picker>
              </div>
              <div class="block-footer">
                <span
                  >*Click on the date in the calendar to choose the custom
                  range</span
                >
              </div>
            </div>
            <div v-if="selectedType == 'WEEKLY'" class="weekly block">
              <div class="header">
                <span
                  class="link-button"
                  :class="{ disable: currentYearForWeek == 0 }"
                  @click="currentYearForWeek--"
                  >&#10094;</span
                >
                <span class="title">{{ currentYearForWeek }}</span>
                <span
                  class="link-button"
                  :class="{
                    disable: currentYearForWeek == today.getFullYear(),
                  }"
                  @click="currentYearForWeek++"
                  >&#10095;</span
                >
              </div>

              <div
                v-if="currentYearForWeek > today.getFullYear()"
                class="wrapper"
              >
                <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
                  <span class="week-number disable">{{ i }}</span>
                </div>
              </div>
              <div
                v-else-if="today.getFullYear() == currentYearForWeek"
                class="wrapper"
              >
                <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
                  <span v-if="i > getCurrentWeek" class="week-number disable">{{
                    i
                  }}</span>
                  <span
                    v-else-if="i == getCurrentWeek"
                    class="week-number today"
                    :class="{
                      'selected-week':
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                      selected:
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                    }"
                    @click="handleSelectWeek(i, currentYearForWeek)"
                    >{{ i }}</span
                  >
                  <span
                    v-else
                    class="week-number"
                    :class="{
                      'selected-week':
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                      selected:
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                    }"
                    @click="handleSelectWeek(i, currentYearForWeek)"
                    >{{ i }}</span
                  >
                </div>
              </div>
              <div v-else class="wrapper">
                <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
                  <span
                    class="week-number"
                    :class="{
                      'selected-week':
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                      selected:
                        i == selectedItem.selectedWeek &&
                        currentYearForWeek == selectedItem.selectedYear,
                    }"
                    @click="handleSelectWeek(i, currentYearForWeek)"
                    >{{ i }}</span
                  >
                </div>
              </div>
              <div class="block-footer">
                <span
                  >*Click on the number in the calendar to choose the week</span
                >
              </div>
            </div>
            <div v-if="selectedType == 'MONTHLY'" class="monthly block">
              <div class="header">
                <span
                  class="link-button"
                  :class="{ disable: currentYearForMonth == 0 }"
                  @click="currentYearForMonth--"
                  >&#10094;</span
                >
                <span class="title">{{ currentYearForMonth }}</span>
                <span
                  class="link-button"
                  :class="{
                    disable: currentYearForMonth == today.getFullYear(),
                  }"
                  @click="currentYearForMonth++"
                  >&#10095;</span
                >
              </div>
              <div
                v-if="currentYearForMonth > today.getFullYear()"
                class="wrapper-month"
              >
                <div
                  v-for="(month, index) in months"
                  :key="index"
                  class="month-item"
                >
                  <span class="disable month-text">{{ month }}</span>
                </div>
              </div>
              <div
                v-else-if="currentYearForMonth == today.getFullYear()"
                class="wrapper-month"
              >
                <div
                  v-for="(month, index) in months"
                  :key="index"
                  class="month-item"
                >
                  <span
                    v-if="index > getCurrentMonth"
                    class="disable month-text"
                    >{{ month }}</span
                  >
                  <span
                    v-else-if="index == getCurrentMonth"
                    class="today month-text"
                    @click="handleSelectMonth(index, currentYearForMonth)"
                    :class="{
                      'selected-month':
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                      selected:
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                    }"
                    >{{ month }}</span
                  >
                  <span
                    v-else
                    class="month-text"
                    @click="handleSelectMonth(index, currentYearForMonth)"
                    :class="{
                      'selected-month':
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                      selected:
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                    }"
                    >{{ month }}</span
                  >
                </div>
              </div>
              <div v-else class="wrapper-month">
                <div
                  v-for="(month, index) in months"
                  :key="index"
                  class="month-item"
                >
                  <span
                    class="month-text"
                    @click="handleSelectMonth(index, currentYearForMonth)"
                    :class="{
                      'selected-month':
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                      selected:
                        index == selectedItem.selectedMonth &&
                        currentYearForMonth == selectedItem.selectedYear,
                    }"
                    >{{ month }}</span
                  >
                </div>
              </div>
              <div class="block-footer">
                <span
                  >*Click on the month in the calendar to choose the month</span
                >
              </div>
            </div>
            <div v-if="selectedType == 'YEARLY'" class="yearly block">
              <div class="header">
                <span
                  class="link-button"
                  :class="{ disable: yearRange == 0 }"
                  @click="prevYearRange()"
                  >&#10094;</span
                >
                <span class="title">{{ yearRange }}-{{ yearRange + 19 }}</span>
                <span
                  class="link-button"
                  :class="{
                    disable:
                      yearRange <= today.getFullYear() &&
                      yearRange + 19 >= today.getFullYear(),
                  }"
                  @click="nextYearRange()"
                  >&#10095;</span
                >
              </div>
              <div v-if="today.getFullYear() < yearRange" class="wrapper-year">
                <div v-for="index in 20" :key="index" class="year-item">
                  <span class="disable year-text">{{
                    yearRange + index - 1
                  }}</span>
                </div>
              </div>
              <div
                v-else-if="
                  today.getFullYear() <= yearRange + 19 &&
                  today.getFullYear() >= yearRange
                "
                class="wrapper-year"
              >
                <div v-for="index in 20" :key="index" class="year-item">
                  <span
                    v-if="yearRange + index - 1 > today.getFullYear()"
                    class="disable year-text"
                    >{{ yearRange + index - 1 }}</span
                  >
                  <span
                    v-else-if="yearRange + index - 1 == today.getFullYear()"
                    class="today year-text"
                    :class="{
                      'selected-year':
                        yearRange + index - 1 == selectedItem.selectedYear,
                      selected:
                        yearRange + index - 1 == selectedItem.selectedYear,
                    }"
                    @click="handleSelectYear(yearRange + index - 1)"
                    >{{ yearRange + index - 1 }}</span
                  >
                  <span
                    v-else
                    class="year-text"
                    @click="handleSelectYear(yearRange + index - 1)"
                    :class="{
                      'selected-year':
                        yearRange + index - 1 == selectedItem.selectedYear,
                      selected:
                        yearRange + index - 1 == selectedItem.selectedYear,
                    }"
                    >{{ yearRange + index - 1 }}</span
                  >
                </div>
              </div>
              <div v-else class="wrapper-year">
                <div v-for="index in 20" :key="index" class="year-item">
                  <span
                    class="year-text"
                    @click="handleSelectYear(yearRange + index - 1)"
                    :class="{
                      'selected-year':
                        yearRange + index - 1 == selectedItem.selectedYear,
                      selected:
                        yearRange + index - 1 == selectedItem.selectedYear,
                    }"
                    >{{ yearRange + index - 1 }}</span
                  >
                </div>
              </div>
              <div class="block-footer">
                <span
                  >*Click on the number in the calendar to choose the year</span
                >
              </div>
            </div>
            <div v-if="!isEveryShotCase" class="block-button">
              <!-- <button
                v-if="isEveryShotCase"
                class="select-type-button"
                :class="{ 'selected-button': selectedType == 'DAILY' }"
                @click="changeRangeType('DAILY')"
              >
                DATE
              </button> -->
              <button
                class="select-type-button"
                :class="{ 'selected-button': selectedType == 'WEEKLY' }"
                @click="changeRangeType('WEEKLY')"
              >
                Weekly
              </button>
              <button
                class="select-type-button"
                :class="{ 'selected-button': selectedType == 'MONTHLY' }"
                @click="changeRangeType('MONTHLY')"
              >
                Monthly
              </button>
              <button
                class="select-type-button"
                :class="{ 'selected-button': selectedType == 'YEARLY' }"
                @click="changeRangeType('YEARLY')"
              >
                Yearly
              </button>
              <button
                class="select-type-button"
                :class="{ 'selected-button': selectedType == 'CUSTOM_RANGE' }"
                @click="changeRangeType('CUSTOM_RANGE')"
              >
                Custom Range
              </button>
              <div v-if="selectedType == 'WEEKLY'" class="d-flex">
                <div :key="textboxKey">
                  <span class="right-title">From</span><br />
                  <a-tooltip placement="bottomLeft">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Weekly option. To edit the date,
                        please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateFrom"
                    />
                  </a-tooltip>
                </div>
                <div>
                  <span class="right-title">To</span><br />
                  <a-tooltip placement="bottomRight">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Weekly option. To edit the date,
                        please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateTo"
                    />
                  </a-tooltip>
                </div>
              </div>
              <div v-else-if="selectedType == 'MONTHLY'" class="d-flex">
                <div>
                  <span class="right-title">From</span><br />
                  <a-tooltip placement="bottomLeft">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Monthly option. To edit the
                        date, please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateFrom"
                    />
                  </a-tooltip>
                </div>
                <div :key="textboxKey">
                  <span class="right-title">To</span><br />
                  <a-tooltip placement="bottomRight">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Monthly option. To edit the
                        date, please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateTo"
                    />
                  </a-tooltip>
                </div>
              </div>
              <div v-else-if="selectedType == 'YEARLY'" class="d-flex">
                <div :key="textboxKey">
                  <span class="right-title">From</span><br />
                  <a-tooltip placement="bottomLeft">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Yearly option. To edit the date,
                        please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateFrom"
                    />
                  </a-tooltip>
                </div>
                <div>
                  <span class="right-title">To</span><br />
                  <a-tooltip placement="bottomRight">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 11px;
                          color: #ebebeb;
                        "
                      >
                        Time range is fixed for Yearly option. To edit the date,
                        please go to Custom Range.
                      </div>
                    </template>
                    <input
                      type="text"
                      class="date-bar id-input date-bar-disable warning-cursor"
                      placeholder="yyyy-mm-dd"
                      readonly
                      :value="dateTo"
                    />
                  </a-tooltip>
                </div>
              </div>
              <div v-else class="d-flex">
                <div>
                  <span class="right-title">From</span><br />
                  <input
                    type="text"
                    class="date-bar id-input"
                    :class="{
                      'warning-input':
                        range.startMessage != 'valid' &&
                        range.startMessage != '',
                    }"
                    placeholder="yyyy-mm-dd"
                    @input="handleDateFromInput($event)"
                    :value="dateFrom"
                  /><br />
                  <p
                    v-if="
                      range.startMessage != 'valid' && range.startMessage != ''
                    "
                    class="warning-text"
                  >
                    {{ range.startMessage }}
                  </p>
                </div>
                <div>
                  <span class="right-title">To</span><br />
                  <input
                    type="text"
                    class="date-bar id-input"
                    :class="{
                      'warning-input':
                        range.endMessage != 'valid' && range.endMessage != '',
                    }"
                    placeholder="yyyy-mm-dd"
                    @input="handleDateToInput($event)"
                    :value="dateTo"
                  /><br />
                  <p
                    v-if="range.endMessage != 'valid' && range.endMessage != ''"
                    class="warning-text"
                  >
                    {{ range.endMessage }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 9px">
          <a
            @click="closeCalendar()"
            style="font-size: 11px"
            id="light-animation"
            class="btn-custom btn-custom-date btn-custom-date-light animationPrimary animationOutline"
          >
            <span v-text="resources['cancel']"></span>
            <!-- Cancel -->
          </a>
          <a
            @click="updateTimePicker()"
            style="font-size: 11px"
            id="btn-update"
            class="btn-custom btn-custom-primary btn-custom-date animationPrimary animationOutline"
            :class="{ 'btn-disable': !checkEnableDatePicker }"
          >
            <span>Update</span>
            <!-- Update -->
          </a>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  props: {
    showExportModal: {
      type: Boolean,
      default: false,
    },
    resources: Object,
  },
  data() {
    return {
      dataType: [],
      dataFrequency: null,
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
      today: new Date(),
      currentYearForWeek: new Date().getFullYear(),
      currentYearForMonth: new Date().getFullYear(),
      currentMonth: new Date().getMonth(),
      yearRange: 2010,
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
      let result = false;
      switch (this.selectedType) {
        case "CUSTOM_RANGE":
          if (
            this.range.startMessage == "valid" &&
            this.range.endMessage == "valid"
          ) {
            result = true;
          }
          break;
        case "WEEKLY":
          if (
            this.selectedItem.selectedWeek != null &&
            this.selectedItem.selectedYear != null
          ) {
            result = true;
          }
          break;
        case "MONTHLY":
          if (
            this.selectedItem.selectedMonth != null &&
            this.selectedItem.selectedYear != null
          ) {
            result = true;
          }
          break;
        case "YEARLY":
          if (this.selectedItem.selectedYear != null) {
            result = true;
          }
          break;
        case "DAILY":
          if (this.range) {
            result = true;
          }
          break;
      }
      return (
        this.dataFrequency &&
        this.dataType &&
        this.dataType.length > 0 &&
        result
      );
      // return (
      //   this.dataFrequency &&
      //   this.dataType &&
      //   ((this.range.startDate &&
      //     this.range.endDate &&
      //     this.startDate.unix() <= this.endDate.unix()) ||
      //     this.today)
      // );
    },
    getCurrentWeek() {
      return this.getCurrentWeekNumber(this.today);
    },
    getCurrentMonth() {
      return this.today.getMonth();
    },
    checkEnableDatePicker() {
      let result = false;
      switch (this.selectedType) {
        case "CUSTOM_RANGE":
          if (
            this.range.startMessage == "valid" &&
            this.range.endMessage == "valid"
          ) {
            result = true;
          } else {
            result = false;
          }
          break;
        case "WEEKLY":
          if (
            this.selectedItem.selectedWeek != null &&
            this.selectedItem.selectedYear != null
          ) {
            result = true;
          }
          break;
        case "MONTHLY":
          if (
            this.selectedItem.selectedMonth != null &&
            this.selectedItem.selectedYear != null
          ) {
            result = true;
          }
          break;
        case "YEARLY":
          if (this.selectedItem.selectedYear != null) {
            result = true;
          }
          break;
        case "DAILY":
          // if (this.selectedItem.selectedYear && this.selectedItem.selectedMonth && this.selectedItem.selectedDate){
          //   result = true;
          // }
          console.log("this.selectedItem", this.selectedItem);
          result = true;
          break;
      }
      console.log(this.trigger);
      console.log(result);
      return result;
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
  },
  mounted() {
    this.animationPrimary();
    this.animationOutline();
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
        // this.endDate = newValue;
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
        // this.startDate = newValue;
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
  },
  methods: {
    datePickerMove: function (event) {
      // pass event object, bound to mouse move with updat
      let x = event.clientX;
      let y = event.clientY;
      console.log("x:", x, "y:", y);
      let arrRangeTemp = document.querySelectorAll(
        '.vc-pane-container .vc-day span[style*="color: var(--blue-900);"]'
      );
      if (arrRangeTemp && arrRangeTemp.length > 0) {
        arrRangeTemp[0].style.color = " var(--white)";
        arrRangeTemp[arrRangeTemp.length - 1].style.color = " var(--white)";
      }
    },
    // checkEnable () {
    //   if ()
    // },
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
    chooseDataType2(data) {
      // this.dataType = 'CYCLE_TIME'
      if (this.dataType.filter((item) => item == data).length == 0) {
        this.dataType.push(data);
      } else {
        this.dataType = this.dataType.filter((item) => item != data);
      }
    },
    // onChangeStart(date, dateString) {
    //   console.log(date, dateString);
    //   this.startDate = dateString.replaceAll('-', '')
    // },
    // onChangeEnd(date, dateString) {
    //   console.log(date, dateString);
    //   this.endDate = dateString.replaceAll('-', '')
    // },
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
    // animationOutline() {
    //   $(".animationOutline2").click(function () {
    //     $(this).addClass("outline-animation");
    //     $(this).one(
    //       "webkitAnimationEnd oanimationend msAnimationEnd animationend",
    //       function (event) {
    //         $(this).removeClass("outline-animation");
    //       }
    //     );
    //   });
    // },
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
      const element = document.getElementById("date-picker");
      element.classList.add("calendar-picker-input-active");
      setTimeout(() => {
        this.calendarShow = true;
        if (this.displayDatePicker == "" || this.selectedType == "") {
          this.selectedType = "DAILY";
          this.isRange = false;
        }

        // Get DOM of today (Custom Range)
        // let className = `id-${this.today.getFullYear()}-${
        //     this.today.getMonth() < 9
        //       ? "0" + (+this.today.getMonth() + 1)
        //       : +this.today.getMonth() + 1
        //   }-${
        //     this.today.getDate() < 10
        //       ? "0" + this.today.getDate()
        //       : this.today.getDate()
        //   }`;
        //   let element = document.getElementsByClassName(className).item(0);
        //   console.log(element);

        element.classList.remove("calendar-picker-input-active");
      }, 700);
    },
    convertDate(date) {
      return `${date.getFullYear()}${
        +date.getMonth() + 1 < 10
          ? "0" + (+date.getMonth() + 1)
          : +date.getMonth() + 1
      }${date.getDate() < 10 ? "0" + date.getDate() : date.getDate()}`;
    },
    exportDynamic() {
      const element = document.getElementById("btn-export");
      element.classList.add("btn-custom-primary-active");
      element.classList.add("disable-cta");
      console.log(element);
      setTimeout(() => {
        element.classList.remove("btn-custom-primary-active");
        setTimeout(() => {}, 300);
      }, 400);

      let data = null;
      let time = "";
      if (this.selectedType == "CUSTOM_RANGE") {
        data = {
          rangeType: this.selectedType,
          dataType: this.dataType,
          dataFrequency: this.dataFrequency,
          startDate: this.convertDate(this.range.start),
          endDate: this.convertDate(this.range.end),
        };
      } else if (this.selectedType === "DAILY") {
        data = {
          rangeType: this.selectedType,
          dataType: this.dataType,
          dataFrequency: this.dataFrequency,
          // startDate: this.convertDate(this.range),
          // endDate: this.convertDate(this.range),
          startDate: moment(this.range).format("YYYYMMDD"),
          endDate: moment(this.range).format("YYYYMMDD"),
        };
      } else {
        switch (this.selectedType) {
          case "WEEKLY":
            time = `${this.selectedItem.selectedYear}${
              this.selectedItem.selectedWeek < 10
                ? "0" + this.selectedItem.selectedWeek
                : this.selectedItem.selectedWeek
            }`;
            break;
          case "MONTHLY":
            time = `${this.selectedItem.selectedYear}${
              +this.selectedItem.selectedMonth + 1 < 10
                ? "0" + (+this.selectedItem.selectedMonth + 1)
                : +this.selectedItem.selectedMonth + 1
            }`;
            break;
          case "YEARLY":
            time = `${this.selectedItem.selectedYear}`;
            break;
        }
        data = {
          rangeType: this.selectedType,
          dataType: this.dataType,
          dataFrequency: this.dataFrequency,
          time: time,
        };
      }
      // console.log(data, "data");
      this.$emit("export-dynamic", data, moment().add(700, "ms"));
    },
    getDateOfISOWeek(w, y) {
      let simple = null;
      if (
        moment("01-01-" + y, "MM-DD-YYYY")
          .toDate()
          .getDay() < 4
      ) {
        simple = new Date(y, 0, 1 + (w - 2) * 7);
      } else {
        simple = new Date(y, 0, 1 + (w - 1) * 7);
      }

      let dow = simple.getDay();
      let ISOweekStart = simple;
      if (dow <= 4)
        ISOweekStart.setDate(simple.getDate() - simple.getDay() + 1);
      else ISOweekStart.setDate(simple.getDate() + 8 - simple.getDay());
      return ISOweekStart;
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
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
    handleSelectWeek(weekNumber, currentYearForWeek) {
      console.log(`${weekNumber} - ${currentYearForWeek}`);
      if (weekNumber != this.selectedItem.selectedWeek) {
        this.selectedItem = {
          selectedMonth: null,
          selectedYear: currentYearForWeek,
          selectedWeek: weekNumber,
        };
        console.log(
          `Selected Week Change to ${this.selectedItem.selectedWeek}`
        );
      } else {
        this.selectedItem = {
          selectedMonth: null,
          selectedYear: null,
          selectedWeek: null,
        };
        this.dateFrom = "";
        this.dateTo = "";
        console.log("deselect week!");
      }
      this.getDateRange();
    },
    handleSelectMonth(month, currentYearForMonth) {
      console.log(`${month} - ${currentYearForMonth}`);
      if (
        month == this.selectedItem.selectedMonth &&
        currentYearForMonth == this.selectedItem.selectedYear
      ) {
        this.selectedItem = {
          selectedMonth: null,
          selectedYear: null,
          selectedWeek: null,
        };
        console.log("deselect month!");
      } else {
        this.selectedItem = {
          selectedMonth: month,
          selectedYear: currentYearForMonth,
          selectedWeek: null,
        };
        this.dateFrom = "";
        this.dateTo = "";
        console.log(
          `Selected Month Change to ${+this.selectedItem.selectedMonth + 1} - ${
            this.selectedItem.selectedYear
          }`
        );
      }
      this.getDateRange();
    },
    handleSelectYear(year) {
      if (this.selectedItem.selectedYear != year) {
        this.selectedItem.selectedYear = year;
        console.log(`Selected year: ${this.selectedItem.selectedYear}`);
      } else {
        this.selectedItem.selectedYear = null;
        this.dateFrom = "";
        this.dateTo = "";
        console.log("Deselected year");
      }
      this.getDateRange();
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
                // if ((new Date(firstWeek) - d) / 86400000 < 4) {
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
    updateTimePicker() {
      const element = document.getElementById("btn-update");
      element.classList.add("btn-custom-primary-active");
      console.log(element);
      setTimeout(() => {
        element.classList.remove("btn-custom-primary-active");
        setTimeout(() => {
          switch (this.selectedType) {
            case "CUSTOM_RANGE":
              this.displayDatePicker = `From ${this.dateFrom} to ${this.dateTo}`;
              break;
            case "DAILY":
              // TODO:
              console.log("this.range", this.range);
              // this.displayDatePicker = this.range.date
              this.displayDatePicker = moment(this.range).format("YYYY-MM-DD");
              break;
            case "WEEKLY":
              this.displayDatePicker = `Week ${
                this.selectedItem.selectedWeek < 10
                  ? "0" + this.selectedItem.selectedWeek
                  : this.selectedItem.selectedWeek
              }, ${this.selectedItem.selectedYear}`;
              break;
            case "MONTHLY":
              this.displayDatePicker = `${
                this.months[this.selectedItem.selectedMonth]
              }, ${this.selectedItem.selectedYear}`;
              break;
            case "YEARLY":
              this.displayDatePicker = `Year ${this.selectedItem.selectedYear}`;
              break;
          }
          this.tempData = {
            range: this.range,
            selectedItem: this.selectedItem,
            selectedType: this.selectedType,
            dateFrom: this.dateFrom,
            dateTo: this.dateTo,
          };
          this.calendarShow = false;
        }, 300);
      }, 400);
    },
    closeCalendar() {
      const element = document.getElementById("light-animation");
      console.log(element);
      element.classList.add("light-animation-button");
      console.log(element);
      setTimeout(() => {
        element.classList.remove("light-animation-button");
        setTimeout(() => {
          this.calendarShow = false;
          this.range = this.tempData.range;
          this.selectedItem = this.tempData.selectedItem;
          this.selectedType = this.tempData.selectedType;
          this.dateFrom = this.tempData.dateFrom;
          this.dateTo = this.tempData.dateTo;
        }, 300);
      }, 400);
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
          // if (checkDate != null && this.dateFrom != "" && startDate != null) {
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
.pretty.p-default input:checked ~ .state label:after {
  background-color: #0075ff;
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
}
.pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff;
}
.pretty span {
  margin-left: 0;
}

.pretty {
  margin-right: unset;
}
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
.custom-modal-body {
  padding: 24px;
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
