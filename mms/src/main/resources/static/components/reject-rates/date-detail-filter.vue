<template>
  <div class="reject-rate-filter" v-click-outside-except-calendar="hidePopupFromOutside">
    <a-button
      @click="togglePopup"
      class="ant-dropdown-trigger1"
    >
      <svg width="20" height="20" viewBox="0 0 24 24">
        <path d="M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z"/>
      </svg>
      <span style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 5px;">{{ selectedTimeLabel }}</span>
    </a-button>
    <div class="dropdown-scroll" v-show="isActiveFirstPopup()">
      <div class="all-time-option active">
        <div class="from-option">
          <label>From</label>
          <div id="date-from" style="max-width: 150px; z-index: 9999999999999 !important">
            <a-date-picker  format="YYYY-MM-DD" :disabled-date="disabledDate" v-model="selectedDate.startDate"  :allow-clear="false"></a-date-picker>
          </div>
        </div>
        <div class="between-option">
          <div class="divider-line"></div>
        </div>
        <div class="to-option">
          <label>To</label>
          <div id="date-to" style="max-width: 150px">
          <!-- <div id="date-to" style="max-width: 150px; z-index: 9999999999999 !important"> -->
            <a-date-picker  format="YYYY-MM-DD" :align="{points: ['bl', 'tr']}" :disabled-date="disabledDate" v-model="selectedDate.endDate" :allow-clear="false"></a-date-picker>
          </div>
        </div>
      </div>
      <div class="time-option" >
        <div class="option-item" :class="{ active: isActiveOption(timeOptions.WEEK) }" @click="showSecondOption(timeOptions.WEEK)">
          Weekly
        </div>
        <div class="option-item" :class="{ active: isActiveOption(timeOptions.MONTH) }"
             @click="showSecondOption(timeOptions.MONTH)">Monthly
        </div>
      </div>
    </div>
    <div class="time-picker weekly-option" v-show="isActiveSecondPopup(timeOptions.WEEK)">
      <div class="header-option">
        <div class="prev-btn" @click="weekPreviousYear()">
          <a class="calendar-prev-btn"></a>
        </div>
        <div class="title">{{ selectedWeek.tempYear }}</div>
        <div class="next-btn">
          <a class="calendar-next-btn" @click="weekNextYear()"></a>
        </div>
      </div>
      <div class="body-option">
        <div class="time-option-item"
             :class="{'can-choosing': weekCanChoosingWeek(week), 'selected': isWeekSelectOption(week)}"
             @click="weekSelectOption(week)"
             v-for="(week, index) in weekOptions()" :key="index">
          <div class="option-item-content"> {{ week }}</div>
        </div>
      </div>
    </div>

    <div class="time-picker monthly-option" v-show="isActiveSecondPopup(timeOptions.MONTH)">
      <div class="header-option">
        <div class="prev-btn">
          <a class="calendar-prev-btn" @click="monthPreviousYear()"></a>
        </div>
        <div class="title">{{ selectedMonth.tempYear }}</div>
        <div class="next-btn">
          <a class="calendar-next-btn" @click="monthNextYear()"></a>
        </div>
      </div>
      <div class="body-option">
        <div class="time-option-item"
             :class="{'can-choosing': monthCanChoosingMonth(index + 1), 'selected': isMonthSelectOption(index + 1)}"
             @click="monthSelectOption(index + 1)"
             v-for="(month, index) in monthOptions" :key="index">
          <div class="option-item-content"> {{ month }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  module.exports = {
    name: "date-detail-filter",
    props: {
      initPeriodType: {
        type: String,
        default: () => null
      },
      timeOptions: Object,
      timeChangeType: Object
    },
    data() {
      return {
        // apply when click concrete time option in second popup
        currentTimeOption: null,
        // when only click in first popup
        currentTimeOptionTemp: null,
        monthOptions: [
          'Jan', 'Feb', 'Mar', 'Apr',
          'May', 'Jun', 'Jul', 'Aug',
          'Sep', 'Oct', 'Nov', "Dec"
        ],
        selectedDate: {
          startDate: null,
          endDate: null
        },
        selectedWeek: {
          tempYear: null,
          year: null,
          selected: null,
          startDate: null,
          endDate: null
        },
        selectedMonth: {
          tempYear: null,
          year: null,
          selected: null,
          startDate: null,
          endDate: null
        },
        isShowFirstOption: false,
        isShowSecondOption: false,
        maxWeekOfYear: 52,
        maxMonthOfYear: 12,
        isCurr: true
      }
    },
    computed: {
      selectedTimeLabel() {
        let result = '';
        switch (this.currentTimeOption) {
          case this.timeOptions.DATE:
              result = moment.unix(this.selectedDate.startDate / 1000).format("YYYY-MM-DD") + ' - ' + moment.unix(this.selectedDate.endDate / 1000).format("YYYY-MM-DD");
            break;
          case this.timeOptions.WEEK:
            result = 'Week ' + (Number(this.selectedWeek.selected) < 10 ? '0' + this.selectedWeek.selected : this.selectedWeek.selected) + ' ' + this.selectedWeek.year;

            if(this.selectedWeek.year < moment().weekYear() || this.selectedWeek.week < this.getCurrentWeek()){
                this.isCurr = true;
            }else{
                this.isCurr = false;
            }
            break;
          case this.timeOptions.MONTH:
            result = this.monthOptions[this.selectedMonth.selected - 1] + ', ' + this.selectedMonth.year;

            if(this.selectedMonth.year < this.getCurrentYear() || this.selectedMonth.selected < this.getCurrentMonth()){
               this.isCurr = true;
            }else{
                this.isCurr = false;
            }
            break;
        }
        this.$emit('change-selected-time-label', result);
        return result;
      }
    },
    watch: {
      selectedTimeLabel() {
        let isCompareWithPrevious = false;
        let startTime = null;
        let endTime = null;
        switch (this.currentTimeOption) {
          case this.timeOptions.DATE:
            startTime = this.selectedDate.startDate;
            endTime = this.selectedDate.endDate;
            break;
          case this.timeOptions.WEEK:

            // calculating start time

            if (this.selectedWeek.year && this.selectedWeek.selected) {
              startTime = moment().year(this.selectedWeek.year).week(this.selectedWeek.selected + 1).weekday(1);
              endTime = moment().year(this.selectedWeek.year).week(this.selectedWeek.selected + 1).weekday(7);

              if (parseInt(this.selectedWeek.selected) === 1) {
                startTime = moment(`${this.selectedWeek.year}0101`, 'YYYYMMDD');
              }

              if (this.selectedWeek.selected === this.maxWeekOfYear) {
                endTime = moment(`${this.selectedWeek.year}1231`, 'YYYYMMDD');
              }
              if(this.selectedWeek.year < moment().weekYear() || this.selectedWeek.selected < this.getCurrentWeek()){
                this.isCurr = true;
              }else{
                this.isCurr = false;
              }
              this.$emit('check-current-time', this.isCurr);

            }
            this.selectedWeek.startDate = startTime;
            this.selectedWeek.endDate = endTime;

            isCompareWithPrevious = true;
            break;
          case this.timeOptions.MONTH:
            let month = this.selectedMonth.selected;
            if (month < 10) {
              month = '0' + month;
            }
            startTime = moment().year(this.selectedMonth.year).month(this.selectedMonth.selected - 1).startOf('month');
            endTime = moment().year(this.selectedMonth.year).month(this.selectedMonth.selected - 1).endOf('month');
            this.selectedMonth.startDate = startTime;
            this.selectedMonth.endDate = endTime;
            isCompareWithPrevious = true;
            if(this.selectedMonth.year < this.getCurrentYear() || this.selectedMonth.selected < this.getCurrentMonth()){
               this.isCurr = true;
            }
            else{
               this.isCurr = false;
            }
            this.$emit('check-current-time', this.isCurr);

            break;
        }
        this.selectedDate.startDate = startTime;
        this.selectedDate.endDate = endTime;
        this.hidePopup();
        if (startTime && endTime) {
          let params = {
            startDate: startTime.format('YYYYMMDD'),
            endDate: endTime.format('YYYYMMDD')
          };
          this.$emit('time-filter', params);
        }
      },

      'selectedDate.startDate': function (value, oldValue) {
        if (value && oldValue && value.toString() === oldValue.toString()) {
          return true;
        }
        let isChangeType = false;
        switch (this.currentTimeOption) {
          case this.timeOptions.WEEK:
            if (value !== this.selectedWeek.startDate) {
              isChangeType = true;
            }
            break;
          case this.timeOptions.MONTH:
            if (value !== this.selectedMonth.startDate) {
              isChangeType = true;
            }
            break;
        }
        if (isChangeType) {
          this.currentTimeOption = this.timeOptions.DATE;
          this.currentTimeOptionTemp = this.currentTimeOption;
        }
        this.$emit('show-change-time-action', this.currentTimeOption !== this.timeOptions.DATE);

      },

      'selectedDate.endDate': function (value, oldValue) {
        if (value && oldValue && value.toString() === oldValue.toString()) {
          return true;
        }

        let isChangeType = false;
        switch (this.currentTimeOption) {
          case this.timeOptions.WEEK:
            if (value !== this.selectedWeek.endDate) {
              isChangeType = true;
            }
            break;
          case this.timeOptions.MONTH:
            if (value !== this.selectedMonth.endDate) {
              isChangeType = true;
            }
            break;
        }
        if (isChangeType) {
          this.currentTimeOption = this.timeOptions.DATE;
          this.currentTimeOptionTemp = this.currentTimeOption;
        }
        this.$emit('show-change-time-action', this.currentTimeOption !== this.timeOptions.DATE);

      },
    },
    methods: {
      initOptionValue(week, year) {
        this.currentTimeOption = this.timeOptions.WEEK;
        // when only click in first popup
        this.currentTimeOptionTemp = this.timeOptions.WEEK;
        this.selectedWeek.year = year;
        this.selectedWeek.selected = week;
        this.selectedWeek.tempYear = year;
        this.selectedMonth.year = year;
        this.selectedMonth.tempYear = year;
      },
      getCurrentYear() {
        return new Date().getFullYear();
      },
      getCurrentMonth() {
        return moment().month() + 1;
      },
      getCurrentWeek() {
        return moment().week() - 1;
      },
      togglePopup() {
        if (this.isShowSecondOption) {
          this.isShowSecondOption = false;
        }
        if (this.isShowFirstOption) {
          this.isShowFirstOption = false;
        } else {
          this.isShowFirstOption = true;
        }

      },
      showSecondOption(option) {
        this.currentTimeOptionTemp = option;
        this.isShowFirstOption = false;
        this.isShowSecondOption = true;
      },
      isActiveOption(option) {
        return this.currentTimeOptionTemp === option;
      },
      isActiveFirstPopup() {
        return this.isShowFirstOption;
      },
      isActiveSecondPopup(option) {
        return this.isShowSecondOption && this.currentTimeOptionTemp === option;
      },
      weekOptions() {
        let res = [];
        for (let week = 1; week <= this.maxWeekOfYear; week++) {
          if (week >= 10) {
            res.push(week)
          } else {
            res.push('0' + week);
          }
        }
        return res;
      },
      hidePopup() {
        this.isShowFirstOption = false;
        this.isShowSecondOption = false;
      },

      hidePopupFromOutside() {
        this.selectedWeek.tempYear = this.selectedWeek.year;
        this.selectedMonth.tempYear = this.selectedMonth.year;
        this.hidePopup();
      },
      weekPreviousYear() {
        this.selectedWeek.tempYear--;
      },
      weekNextYear() {
        this.selectedWeek.tempYear++;
      },
      weekSelectOption(week) {
        if (!this.weekCanChoosingWeek(week)) {
          return;
        }
        this.selectedWeek.selected = week;
        this.selectedWeek.year = this.selectedWeek.tempYear;
        this.currentTimeOption = this.timeOptions.WEEK;
        this.hidePopup();
      },
      isWeekSelectOption(week) {
        return this.selectedWeek.selected === week;
      },
      weekCanChoosingWeek(week) {
        return this.selectedWeek.tempYear < moment().weekYear() || (this.selectedWeek.tempYear === moment().weekYear() && week <= this.getCurrentWeek());
      },
      monthPreviousYear() {
        this.selectedMonth.tempYear--;
      },
      monthNextYear() {
        this.selectedMonth.tempYear++;
      },
      monthSelectOption(monthIndex) {
        if (!this.monthCanChoosingMonth(monthIndex)) {
          return;
        }
        this.selectedMonth.selected = monthIndex;
        this.selectedMonth.year = this.selectedMonth.tempYear;
        this.currentTimeOption = this.timeOptions.MONTH;
        this.hidePopup();
      },
      isMonthSelectOption(month) {
        return this.selectedMonth.selected === month;
      },
      monthCanChoosingMonth(month) {
        return this.selectedMonth.tempYear < this.getCurrentYear() || (this.selectedMonth.tempYear === this.getCurrentYear() && month <= this.getCurrentMonth());
      },
      disabledDate(current) {
        return current && current > moment().endOf('day');
      },
      changeTime(changeType) {
        if (changeType === this.timeChangeType.NEXT) {
          switch (this.currentTimeOption) {
            case this.timeOptions.WEEK:
              if (this.selectedWeek.selected === this.maxWeekOfYear) {
                this.selectedWeek.selected = 1;
                this.selectedWeek.tempYear++;
                this.selectedWeek.year++;
              } else {
                this.selectedWeek.selected++;
              }
              break;
            case this.timeOptions.MONTH:
              if (this.selectedMonth.selected === this.maxMonthOfYear) {
                this.selectedMonth.selected = 1;
                this.selectedMonth.tempYear++;
                this.selectedMonth.year++;
              }else {
                this.selectedMonth.selected++;
              }
              break;
          }
        } else if (changeType === this.timeChangeType.PREV) {
          switch (this.currentTimeOption) {
            case this.timeOptions.WEEK:
              if (this.selectedWeek.selected === 1) {
                this.selectedWeek.selected = this.maxWeekOfYear;
                this.selectedWeek.tempYear--;
                this.selectedWeek.year--;
              }else {
                this.selectedWeek.selected--;
              }
              break;
            case this.timeOptions.MONTH:
              if (this.selectedMonth.selected === 1) {
                this.selectedMonth.selected = this.maxMonthOfYear;
                this.selectedMonth.tempYear--;
                this.selectedMonth.year--;
              }else {
                this.selectedMonth.selected--;
              }
              break;
          }
        }
      },
      resizeDateContainer(pos){
        setTimeout(() => {
          const el = document.querySelectorAll('.ant-calendar-picker-container')
          if (el) {
            // el.forEach(element => {
            //   element.style.width = "10px"
            // });
            
            if (pos == 'left') {
              el[0].style.width = "10px"
            } 
            else {
              let element;
              if (el.length == 1) {
                element = el[0];
              } else {
                element = el[1];
              }
              let position = element.getBoundingClientRect();
              let leftBoxPosition = document.getElementById('date-from').getBoundingClientRect();
              console.log(position)
              console.log(leftBoxPosition)
              if (position.left < leftBoxPosition.right) {
                element.style.left = `${leftBoxPosition.right}px !important`
                // element.style.width = '200px'

                // let content = element.querySelectorAll('.ant-calendar-picker-container-content')
                // console.log('content',content)
                // if (content) {
                //   content[0].style.top = '0px'
                // }
                // let nextMonth = element.querySelectorAll('.ant-calendar-next-month-btn')
                // nextMonth[0].addEventListener("click", this.changeRightPanel())
              }
            }
          }
        }, 100);
      },
      changeRightPanel(){
        setTimeout(() => {
          const el = document.querySelectorAll('.ant-calendar-picker-container')
          let element;
          if (el.length == 1) {
            element = el[0];
          } else {
            element = el[1];
          }
          let position = element.getBoundingClientRect();
          element.style.top = `${position.top + 36}px`
          let content = element.querySelectorAll('.ant-calendar-picker-container-content')
          console.log('content',content)
          if (content) {
            content[0].style.top = '0px'
          }
        }, 100);
        
      }
    },
  }
</script>
<style scoped>
  .ant-popover {
    position: relative;
    padding-top: 0;
  }

  .ant-dropdown-trigger1 {
    border-radius: 7px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-right: 0px;
    padding: 5px !important;
    border: 1px solid #CDCDCD;
  }

  .ant-dropdown-trigger1:last-child {
    border-radius: 0px;
    border: none;
    padding: 5px 0px !important;
    margin-right: 0px;
    border-radius: 7px;
    display: flex;
    /* padding: 10px !important; */
    border: none;
    align-items: center;
    justify-content: space-between;
  }

  button {
    border: 1px solid #c8ced3;
    padding: 5px 10px !important;
    border-radius: 0;
  }

  .ant-btn.active,
  .ant-btn:active,
  .ant-btn:focus {
    border-radius: 0;
  }

  .ant-btn:focus, .ant-btn:hover {
    color: #000;
  }

  .between-option {
    padding: 0 10px;
  }

  .divider-line {
    margin-top: 30px;
    height: 2px;
    width: 15px;
    background: #c8ced3;
  }

  .dropdown-scroll {
    padding: 10px;
    width: 340px;
    max-height: 300px;
    overflow-y: scroll;
    position: absolute;
    background: #FFF;
    right: 7px;
    border: 1px solid #CCC;
    border-radius: 5px;
    top: 60px;
    z-index: 1;
  }

  .all-time-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .all-time-option.active input {
    border: 1px solid #684DF8;
  }

  .option-item {
    border: 1px solid #c8ced3;
    text-align: center;
    margin-top: 10px;
    padding: 3px;
    border-radius: 5px;
    cursor: pointer;
  }

  .option-item.active {
    color: #FFF;
    background: #684DF8;
  }

  .calendar-prev-btn:after {
    content: "\AB";
  }

  .calendar-next-btn:after {
    content: "\BB";
  }

  .time-picker {
    padding: 10px;
    overflow-y: scroll;
    position: absolute;
    background: #FFF;
    right: 7px;
    border: 1px solid #CCC;
    border-radius: 5px;
    top: 60px;
  }

  .header-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .body-option {
    padding-top: 7px;
    display: grid;
    grid-template-columns: auto auto auto auto;
  }

  .time-option-item {
    padding: 2px;
    text-align: center;
  }

  .time-option-item .option-item-content {
    border-radius: 5px;
    color: #777;
    cursor: not-allowed;
  }

  .time-option-item.can-choosing .option-item-content {
    color: #000;
    cursor: pointer;
  }

  .time-option-item.can-choosing.selected .option-item-content {
    background: #684DF8;
    color: #FFF;
  }

  .time-option-item.can-choosing .option-item-content:hover {
    background: #684DF8;
    color: #FFF;
  }

  .time-option-item.can-choosing .option-item-content.active {
    background: #684DF8;
    color: #FFF;
  }

  .monthly-option .time-option-item .option-item-content {
    padding: 20px 10px;
  }

  .yearly-option .time-option-item .option-item-content {
    padding: 20px 5px;
  }

  .weekly-option .body-option {
    grid-template-columns: auto auto auto auto auto auto auto auto auto;
  }

  .weekly-option .body-option .option-item-content {
    font-weight: normal;
  }

  .weekly-option .time-option-item .option-item-content {
    border-radius: 0;
    padding: 3px;
  }
</style>