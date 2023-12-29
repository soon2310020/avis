<template>
    <div class="part-filter" v-click-outside-except-calendar="hidePopupFromOutside">
        <a
            @click="togglePopup"
            class="btn-custom btn-outline-custom-primary btn-animation-shine all-time-filters"
            style="align-items: center"
             :class="{'animation-secondary': showAnimation}"
            href="javascript:void(0)"
            @mouseover="isHoverBtn = true"
            @mouseleave="isHoverBtn = false"
        >
        <img v-if="isHoverBtn" src="/images/icon/date-picker-before-hover.svg" />
        <img v-else src="/images/icon/date-picker-before.svg" />
            <!-- <img :class="{'card-shadow': displayShadowTimeBox}" height="18" src="/images/icon/time_setting.svg" alt="calender" /> -->
            <span style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 5px;">{{ selectedTimeLabel }}</span>
            <!-- <svg class="down-icon" width="16" height="16" viewBox="0 0 24 24"> -->
                <!-- <path d="M16.293 9.293L12 13.586L7.707 9.293l-1.414 1.414L12 16.414l5.707-5.707z"/> -->
            <!-- </svg> -->
        </a>
        <div class="dropdown-scroll" v-show="isActiveFirstPopup()">
            <div class="all-time-option active">
                <div class="from-option">
                    <label v-text="resources['from']"></label>
                    <div style="max-width: 150px">
                        <a-date-picker format="MMM Do, YYYY" v-model="selectedDate.startDate"></a-date-picker>
                    </div>
                </div>
                <div class="between-option">
                    <div class="divider-line"></div>
                </div>
                <div class="to-option">
                    <label v-text="resources['to']"></label>
                    <div style="max-width: 150px">
                        <a-date-picker format="MMM Do, YYYY" v-model="selectedDate.endDate"></a-date-picker>
                    </div>
                </div>
            </div>
            <div class="time-option">
                <div class="option-item" :class="{ active: isActiveOption('ALL') }" @click="activeAllOption()"
                    v-text="resources['all_time']">
                </div>
                <div class="option-item" :class="{ active: isActiveOption('WEEK') }" @click="showSecondOption('WEEK')"
                v-text="resources['weekly']">
                </div>
                <div class="option-item" :class="{ active: isActiveOption('MONTH') }"
                     @click="showSecondOption('MONTH')"
                     v-text="resources['monthly']">
                </div>
                <div class="option-item" :class="{ active: isActiveOption('YEAR') }" @click="showSecondOption('YEAR')"
                    v-text="resources['yearly']">
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

        <div class="time-picker yearly-option" v-show="isActiveSecondPopup(timeOptions.YEAR)">
            <div class="header-option">
                <div class="prev-btn">
                    <a class="calendar-prev-btn" @click="yearPreviousDecade()"></a>
                </div>
                <div class="title">{{ selectedYear.startYear }}-{{ selectedYear.startYear + 9 }}</div>
                <div class="next-btn">
                    <a class="calendar-next-btn" @click="yearNextDecade()"></a>
                </div>
            </div>
            <div class="body-option">
                <div class="time-option-item"
                     :class="{'can-choosing': canChoosingYearInDecade(year), 'selected': isYearSelectOption(year)}"
                     @click="yearSelectOption(year)"
                     v-for="(year, index) in yearOptions()"
                     :key="index">
                    <div class="option-item-content"> {{ year }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        name: "part-filter",
        props: {
            initPeriodType: {
                type: String,
                default: () => null
            },
            initTimePeriod: {
                type: String,
                default: () => null
            },
            resources:Object,
        },
        data() {
            return {
                timeOptions: {
                    DATE: 'DATE',
                    ALL: 'ALL',
                    WEEK: 'WEEK',
                    MONTH: 'MONTH',
                    YEAR: 'YEAR'
                },
                // apply when click concrete time option in second popup
                currentTimeOption: 'ALL',
                // when only click in first popup
                currentTimeOptionTemp: 'ALL',
                monthOptions: [
                    'Jan', 'Feb', 'Mar', 'Apr',
                    'May', 'Jun', 'Jul', 'Aug',
                    'Sep', 'Oct', 'Nov', "Dec"
                ],
                displayShadowTimeBox: false,
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
                selectedYear: {
                    startYear: null,
                    selected: null,
                    startDate: null,
                    endDate: null
                },
                isShowFirstOption: false,
                isShowSecondOption: false,
                isHoverBtn: false,
                showAnimation: false
            }
        },
        mounted() {
            this.initOptionValue();
            if (this.initTimePeriod && this.initPeriodType) {
                switch(this.initPeriodType) {
                    case 'DAILY':
                        this.currentTimeOption = this.timeOptions.DATE;
                        let dateParams = this.initTimePeriod.split(" ");
                        let day = dateParams[0];
                        let year = dateParams[2];
                        let month = this.monthOptions.filter(month => month.toUpperCase() === dateParams[1].toUpperCase())[0] + 1;
                        let timestamps = moment().year(year).month(month).date(day);
                        this.selectedDate.startDate = timestamps;
                        this.selectedDate.endDate = timestamps;

                        break;
                    case 'WEEKLY':
                        this.currentTimeOption = this.timeOptions.WEEK;
                        let partsWeek = this.initTimePeriod.split(" ");
                        let weekNumber = partsWeek[0].substr(1);
                        this.selectedWeek.selected = weekNumber;
                        this.selectedWeek.year = partsWeek[1];
                        break;
                    case 'MONTHLY':
                        this.currentTimeOption = this.timeOptions.MONTH;
                        let parts = this.initTimePeriod.split(" ");
                        let monthNumber = moment().month(parts[0]).format("M");
                        let yearNumber = parts[1];
                        this.selectedMonth.selected = monthNumber;
                        this.selectedMonth.year = yearNumber;
                        break;
                    case 'YEARLY':
                        this.currentTimeOption = this.timeOptions.YEAR;
                        this.selectedYear.selected = this.initTimePeriod;
                        this.selectedYear.startYear = Math.floor(parseInt(this.selectedYear.selected) / 10) * 10;
                        break;
                }
                this.currentTimeOptionTemp = this.currentTimeOption;
            }
        },
        computed: {
            selectedTimeLabel() {
                switch (this.currentTimeOption) {
                    case this.timeOptions.ALL:
                        return this.resources['all_time'];
                    case this.timeOptions.DATE:
                        return moment.unix(this.selectedDate.startDate / 1000).format("MMM Do, YYYY") + ' - ' + moment.unix(this.selectedDate.endDate / 1000).format("MMM Do, YYYY");
                    case this.timeOptions.WEEK:
                        return 'Week ' + this.selectedWeek.selected + ' ' + this.selectedWeek.year;
                    case this.timeOptions.MONTH:
                        return this.monthOptions[this.selectedMonth.selected - 1] + ', ' + this.selectedMonth.year;
                    case this.timeOptions.YEAR:
                        return this.selectedYear.selected;
                }
            }
        },
        watch: {
            selectedTimeLabel(){
                let params = {};
                let isCompareWithPrevious = false;
                let startTime = null;
                let endTime = null;
                switch (this.currentTimeOption) {
                    case this.timeOptions.DATE:
                        params = {
                            startDate: moment.unix(this.selectedDate.startDate/1000).format("YYYYMMDD"),
                            endDate: moment.unix(this.selectedDate.endDate/1000).format("YYYYMMDD"),
                        };
                        startTime = this.selectedDate.startDate;
                        endTime = this.selectedDate.endDate;
                        break;
                    case this.timeOptions.WEEK:
                        params = {
                          timePeriod: 'W' + this.selectedWeek.year + this.selectedWeek.selected
                        };

                        // calculating start time
                        startTime = moment().year(this.selectedWeek.year).week(this.selectedWeek.selected).startOf('week');
                        endTime = moment().year(this.selectedWeek.year).week(this.selectedWeek.selected).endOf('week');
                        this.selectedWeek.startDate = startTime;
                        this.selectedWeek.endDate = endTime;
                        isCompareWithPrevious = true;
                        break;
                    case this.timeOptions.MONTH:
                        let month = this.selectedMonth.selected;
                        if (month < 10) {
                            month = '0' + month;
                        }
                        params = {
                            timePeriod: 'M' + this.selectedMonth.year + month
                        };
                        startTime = moment().year(this.selectedMonth.year).month(this.selectedMonth.selected - 1).startOf('month');
                        endTime = moment().year(this.selectedMonth.year).month(this.selectedMonth.selected - 1).endOf('month');
                        this.selectedMonth.startDate = startTime;
                        this.selectedMonth.endDate = endTime;
                        isCompareWithPrevious = true;
                        break;
                    case this.timeOptions.YEAR:
                        params = {
                            timePeriod: 'Y' + this.selectedYear.selected
                        };
                        startTime = moment().year(this.selectedYear.selected).startOf('year');
                        endTime = moment().year(this.selectedYear.selected).endOf('year');
                        this.selectedYear.startDate = startTime;
                        this.selectedYear.endDate = endTime;
                        isCompareWithPrevious = true;
                        break;
                }
                this.selectedDate.startDate = startTime;
                this.selectedDate.endDate = endTime;
                this.hidePopup();
                this.$emit('time-filter', params, isCompareWithPrevious);
            },

            'selectedDate.startDate': function(value, oldValue) {
                if (value && oldValue && value.toString() === oldValue.toString()) {
                    return true;
                }
                let isChangeType = false;
                switch(this.currentTimeOption){
                    case this.timeOptions.ALL:
                        if (value && this.selectedDate.endDate) {
                            isChangeType = true;
                        }
                        break;
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
                    case this.timeOptions.YEAR:
                        if (value !== this.selectedYear.startDate) {
                            isChangeType = true;
                        }
                        break;
                }
                if (isChangeType) {
                    this.currentTimeOption = this.timeOptions.DATE;
                    this.currentTimeOptionTemp = this.currentTimeOption;
                }

            },

            'selectedDate.endDate': function(value, oldValue) {
                if (value && oldValue && value.toString() === oldValue.toString()) {
                    return true;
                }

                let isChangeType = false;
                switch(this.currentTimeOption){
                    case this.timeOptions.ALL:
                        if (value && this.selectedDate.startDate) {
                            isChangeType = true;
                        }
                        break;
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
                    case this.timeOptions.YEAR:
                        if (value !== this.selectedYear.endDate) {
                            isChangeType = true;
                        }
                        break;
                }
                if (isChangeType) {
                    this.currentTimeOption = this.timeOptions.DATE;
                    this.currentTimeOptionTemp = this.currentTimeOption;
                }
            },
        },
        methods: {
            initOptionValue() {
                let defaultYear = this.getCurrentYear();
                this.selectedWeek.year = defaultYear;
                this.selectedWeek.tempYear = defaultYear;
                this.selectedMonth.year = defaultYear;
                this.selectedMonth.tempYear = defaultYear;
                this.selectedYear.startYear = Math.floor(defaultYear / 10) * 10;
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
                this.displayShadowTimeBox = true
                if (this.isShowSecondOption) {
                    this.isShowSecondOption = false;
                }
                if(this.isShowFirstOption) {
                    this.isShowFirstOption = false;
                }
                else {
                    this.showAnimation = true;
                    setTimeout(() => {
                        this.showAnimation = false;
                    }, 700)
                    this.isShowFirstOption = true;
                }

            },
            activeAllOption() {
                this.currentTimeOptionTemp = this.timeOptions.ALL;
                this.currentTimeOption = this.timeOptions.ALL;
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
            yearOptions() {
                let res = [];
                let startYear = this.selectedYear.startYear;
                for (let year = startYear; year <= startYear + 11; year++) {
                    res.push(year);
                }
                return res;
            },
            canChoosingYearInDecade(year) {
                return this.selectedYear.startYear + 9 >= year && year <= this.getCurrentYear();
            },
            weekOptions() {
                let res = [];
                for (let week = 1; week <= 52; week++) {
                    if (week >= 10) {
                        res.push(week)
                    } else {
                        res.push('0' + week);
                    }
                }
                return res;
            },
            hidePopup() {
                this.displayShadowTimeBox = false
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
                return this.selectedWeek.tempYear < moment().weekYear() || (this.selectedWeek.tempYear ===  moment().weekYear() && week <= this.getCurrentWeek());
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
                return this.selectedMonth.tempYear < this.getCurrentYear() || (this.selectedMonth.tempYear ===  this.getCurrentYear() && month <= this.getCurrentMonth());
            },
            yearPreviousDecade() {
                this.selectedYear.startYear -= 10;
            },
            yearNextDecade() {
                this.selectedYear.startYear += 10;
            },
            yearSelectOption(year) {
                if (!this.canChoosingYearInDecade(year)) {
                    return;
                }
                this.selectedYear.selected = year;
                this.currentTimeOption = this.timeOptions.YEAR;
                this.hidePopup();
            },
            isYearSelectOption(year) {
                return this.selectedYear.selected === year;
            }
        }
    }
</script>

<style scoped>
    .ant-popover {
        position: relative;
        padding-top: 0;
    }

    .card-shadow {
       box-shadow: 0 3px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24);
    }

    .ant-dropdown-trigger1 {
        border: none;
        padding: 5px 0px !important;
        border-radius: 7px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-right: 0;
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
        right: -7px;
        border: 1px solid #CCC;
        border-radius: 5px;
        top: 40px !important;
        z-index: 100 !important;
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
        right: -7px;
        border: 1px solid #CCC;
        border-radius: 5px;
        top: 40px !important;
        z-index: 100 !important;
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