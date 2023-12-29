<template>
    <div class="delivery-week-filter" v-click-outside="hidePopupFromOutside">
        <a  href="javascript:void(0)"
            @click="togglePopup"
            class="dropdown_button-custom"
            :id="'delivery-week-filter-'+id"
            @mouseover="mouseOverButton()"
            @mouseleave="mouseleaveButton()"
            style="width: 170px"
        >
            <div style="display: flex;align-items: center">
                <img id="calendar-icon" :class="{'card-shadow': displayShadowTimeBox}" height="18" :src="showIcon ? '/images/icon/press.svg' : '/images/icon/Icon feather-calendar-3.svg'" alt="calender" />
                <label :key="timeLabel" style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px;margin-bottom: 0"
                    :class="{'selected-dropdown-text' : visible}"
                >{{selectedTimeLabel}}</label>
            </div>
                
                <!-- <span :key="timeLabel"  style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px;"
                    :class="{'selected-dropdown-text' : visible}"
                >Week {{selectedWeek.week}},  {{selectedWeek.year}}</span> -->
                <!-- <span :key="timeLabel"  style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px;"
                    :class="{'selected-dropdown-text' : visible}"
                >{{ selectedTimeLabel }}</span> -->
                <div>
                    <a-icon class="caret" :class="{'selected-dropdown-text' : visible, 'caret-show' : displayCaret}" type="caret-down" />
                </div>
                <!-- <div v-show="displayCaret" class="caret" :class="{'selected-dropdown-text' : visible}">
                    <a-icon type="caret-up" />
                </div> -->
            <!-- <a-icon v-if="!visible" type="caret-down"/>
            <a-icon v-else type="caret-up"/> -->
        </a>
        <div class="time-picker weekly-option" v-show="visible">
            <div class="header-option">
                <div class="prev-btn" @click="weekPreviousYear()">
                    <a class="calendar-prev-btn"></a>
                </div>
                <div class="title">{{ selectedWeek.tmpYear }}</div>
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
    </div>
</template>

<script>
    module.exports = {
        name: "delivery-week-filter",
        props: {
            resources:Object,
            defaultValue: Object,
            weekFilter: Function,
            id: String,
        },
        data() {
            return {
                displayShadowTimeBox: false,
                selectedWeek: {
                    year: null,
                    week: null,
                    tmpYear: null,
                    tmpWeek: null
                },
                isShowFirstOption: false,
                isShowSecondOption: false,
                visible: false,
                showIcon: false,
                animationTimeout: null,
                timeLabel: 0,
                displayCaret: false,
                caretTimeout: null,
            }
        },
        mounted() {
            this.$nextTick(() => {
              console.log('initOptionValue 1111')
                this.initOptionValue();
            });
        },
        watch:{
            visible: function() {
                if (!this.visible) {
                    this.closeAnimation();
                }
            },
            selectedWeek: function(){
                this.timeLabel++;
                this.displayCaret = true;
                console.log(this.selectedWeek)
                console.log(this.timeLabel)
            },
          defaultValue: function (value, oldValue) {
              if(value) {
                this.initOptionValue();
              }
          }

        },
        computed: {
            selectedTimeLabel() {
                return 'Week ' + this.selectedWeek.week + ', ' + this.selectedWeek.year;
            }
        },
        methods: {
            initOptionValue() {
                this.selectedWeek.week = this.defaultValue.week;
                this.selectedWeek.year = this.defaultValue.year;
                this.selectedWeek.tmpYear = this.selectedWeek.year;
                this.selectedWeek.tmpWeek = this.selectedWeek.week;
                console.log(this.defaultValue.week, '11')
                console.log(this.defaultValue.year, '22')
            },
            getCurrentYear() {
                return new Date().getFullYear();
            },
            getCurrentWeek() {
                return moment().week() - 1;
            },
            togglePopup() {
                this.showAnimation();
                this.visible = !this.visible;
                // this.showIcon = !this.showIcon
            },
            weekOptions() {
                let res = [];
                for (let week = 1; week <= 52; week++) {
                    if (week >= 10) {
                        res.push(week.toString())
                    } else {
                        res.push('0' + week);
                    }
                }
                return res;
            },
            hidePopup() {
                this.visible = false;
                // this.showIcon = false;
                // this.closeAnimation();
            },

            hidePopupFromOutside() {
                if (this.visible && this.selectedWeek.tmpYear !== this.selectedWeek.year || this.selectedWeek.tmpWeek !== this.selectedWeek.week) {
                    this.selectedWeek.year = this.selectedWeek.tmpYear;
                    this.selectedWeek.week = this.selectedWeek.tmpWeek;
                    this.weekFilter(this.selectedWeek.year + this.selectedWeek.week);
                }
                this.showIcon = false;
                this.hidePopup();
            },
            weekPreviousYear() {
                this.selectedWeek.tmpYear--;
            },
            weekNextYear() {
                this.selectedWeek.tmpYear++;
            },
            weekSelectOption(week) {
                if (!this.weekCanChoosingWeek(week)) {
                    return;
                }
                this.selectedWeek.tmpWeek = week;
                this.displayCaret = true;
            },
            isWeekSelectOption(week) {
                return this.selectedWeek.tmpWeek === week;
            },
            weekCanChoosingWeek(week) {
                return this.selectedWeek.tmpYear < moment().weekYear() || (+this.selectedWeek.tmpYear ===  moment().weekYear() && week <= this.getCurrentWeek());
            },
            showAnimation (){
                const el = document.getElementById('delivery-week-filter-'+ this.id)
                if (!this.visible) {
                    el.classList.add("dropdown_button-animation");
                    this.animationTimeout = setTimeout(() => {
                    el.classList.remove("dropdown_button-animation");
                    el.classList.add("selected-dropdown-text");
                    el.classList.add("selected-dropdown-button");
                    }, 1600);
                } else {
                    this.closeAnimation()
                }
            },
            closeAnimation(){
                console.log('Close')
                const el = document.getElementById('delivery-week-filter-'+ this.id)
                if (this.animationTimeout != null) {
                    clearTimeout(this.animationTimeout);
                    this.animationTimeout = null;
                } 
                if (this.caretTimeout != null) {
                    clearTimeout(this.caretTimeout);
                    this.caretTimeout = null;
                }
                el.classList.remove("dropdown_button-animation");
                    el.classList.remove("selected-dropdown-text");
                    el.classList.remove("selected-dropdown-button");
                    this.displayCaret = false
            },
            mouseOverButton(){
                this.showIcon = true;
            },
            mouseleaveButton(){
                if (!this.visible) {
                    this.showIcon = false;
                }
            }
        },
    }
</script>
<style>
</style>
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

    /*button {*/
    /*    border: 1px solid #c8ced3;*/
    /*    padding: 5px 10px !important;*/
    /*    border-radius: 0;*/
    /*}*/

    /*.ant-btn.active,*/
    /*.ant-btn:active,*/
    /*.ant-btn:focus {*/
    /*    border-radius: 0;*/
    /*}*/

    /*.ant-btn:focus, .ant-btn:hover {*/
    /*    color: #000;*/
    /*}*/

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
    .delivery-week-filter {
        position: relative;
    }
    
</style>