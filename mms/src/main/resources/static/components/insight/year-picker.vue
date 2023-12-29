<template>
    <div class="ant-popover ant-popover-placement-bottom" v-show="visible">
        <div class="time-picker yearly-option">
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
        name: "year-picker",
        props: {
            query: Function,
            visible: Boolean,
            hidePicker: Function,
            resources: Object
        },
        data() {
            return {
                selectedYear: {
                    startYear: null,
                    selected: null,
                    startDate: null,
                    endDate: null
                }
            }
        },
        mounted() {
            this.initOptionValue();
        },
        computed: {
        },
        watch: {

        },
        methods: {
            initOptionValue() {
                let defaultYear = this.getCurrentYear();
                this.selectedYear.startYear = Math.floor(defaultYear / 10) * 10;
            },
            getCurrentYear() {
                return new Date().getFullYear();
            },
            togglePopup() {
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
                return this.selectedYear.startYear + 9 >= year;
            },
            hidePopup() {
                this.hidePicker();
            },

            hidePopupFromOutside() {
                this.hidePopup();
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
                this.query(year);
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
        padding-top: 0
    }

    button {
        border: 1px solid #c8ced3;
        padding: 5px 10px !important;
        border-radius: 0;
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
        left: 0;
        border: 1px solid #CCC;
        border-radius: 5px;
        top: 10px;
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

    .yearly-option .time-option-item .option-item-content {
        padding: 20px 5px;
    }
</style>