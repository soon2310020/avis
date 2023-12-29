<template>
    <div>
        <p class="common-component-desc"></p>
       <datepicker  :select-option="timeOption"  :selected-frequency="calendarMode" :is-range="isRange" @change-date="handlePickDate"></datepicker>
        <div class="common-component-content">
            <!-- simple (only required) -->
            <!-- <basic-dropdown-calendar :data="data" :pie-data-binder="pieDataBinder" :pie-category-field="pieCategoryField"></basic-dropdown-calendar> -->
            <!-- detail (all options) -->
            <ul class="props-tab">
                <li @click="setPropsTab('datepicker')" :active="propsTab === 'datepicker' ? true : false">Switch Modal Container</li>
                <!-- <li @click="setPropsTab('basic-dropdown-calendar')" :active="propsTab === 'basic-dropdown-calendar' ? true : false">Basic Dropdown Calendar</li>
                <li @click="setPropsTab('chart-date-display-picker')" :active="propsTab === 'chart-date-display-picker' ? true : false">Chart Date and Display picker</li> -->
            </ul>

            <!-- datepicker -->
             <div class="sub-content" v-if="propsTab === 'datepicker'">
               <div class="props-select-row">
                    <p><span>Switch Modal Calendar</span></p>
                        <cta-button :click-handler="openDatePickerModal1">Open/Close Calendar</cta-button>
                    <p>
                        <span class="mb-0">Open and Close Date Picker</span>
                    </p>
                </div>

               <div class="props-select-row">
                    <p><span>Buttons Option</span></p>
                    <input v-model="timeOptions"  />
                    <p>
                        <span>Set the Options (date, week, month, year, custom) with comma</span>
                        <b>{{ timeOption }}</b>
                    </p>
                </div>
               <div class="props-select-row">
                 <p><span>Mode</span></p>
                 <select v-model="calendarMode">
                 <option v-for="list in timeOption" :key="list" :value="list">{{ list.charAt(0).toUpperCase() + list.toLowerCase().slice(1) }}</option>
                   <!-- <option value="DATE">Date</option>
                   <option value="WEEK">Week</option>
                   <option value="MONTH">Month</option>
                   <option value="YEAR">Year</option>
                   <option value="CUSTOM">Custom</option> -->
                 </select>
                 <p>
                   <b>{{ calendarMode }}</b>
                 </p>
               </div>
               <div class="props-select-row">
                 <p><span>Is Range</span></p>
                 <select v-model="isRange">
                   <option :value="true">true</option>
                   <option :value="false">false</option>
                 </select>
                 <p>
                     <span style="color : red">Work on Custom Calendar</span>
                   <b>{{ isRange }}</b>
                 </p>
               </div>
            </div>

        </div>


    </div>
</template>

<script>
module.exports = {
   name: 'DatePickerTest',
   
    components: {
         'datepicker': httpVueLoader('/components/common/new-date-picker/date-picker.vue'),
         'cta-button': httpVueLoader('/components/common/cta-button.vue'),
    },
    created() {
            this.timeOption = [
              "DATE",
              "WEEK",
              "MONTH",
              "YEAR",
              "CUSTOM"
            ];
          },
           computed:{
            getModalFrequency(){
              console.log(this.requestParam.frequent)
              return [`${this.requestParam.frequent}_RANGE`]
            }
            },
    data() {
        return {
            timeOptions : "DATE, WEEK, MONTH, YEAR, CUSTOM",
            timeOption:[],
            showDatePicker : false ,
            isRange:false,
           tempDateData: {
                from: new Date(),
                to: new Date(),
                fromTitle: moment().format("YYYY-MM-DD"),
                toTitle: moment().format("YYYY-MM-DD"),
              },
             calendarMode:'DATE',
               requestParam: {
                    // accessType: 'ADMIN_MENU',
                    day: moment(new Date()).format("YYYYMMDD"),
                    // query: '',
                    rejectedRateStatus: null,
                    // sort: 'id,desc',
                    page: 1,
                    frequent: 'DATE'
                    // id: '',
                },



         
          propsTab: 'datepicker',
            // active: false,
        };
    },
    watch: {
        timeOptions(value){
            if(value !== ""){
            this.timeOptions = value.toUpperCase();
            this.timeOption = this.timeOptions.split(",");
            }
            else {
                this.timeOption = [];
            }
        }
    },

    methods: {
        handleChangeDate(data){
                    console.log('change date', data)
                    this.tempDateData = data;
                    switch (data.rangeType) {
                        case 'WEEK':
                            delete this.requestParam.month
                            delete this.requestParam.day
                            this.requestParam.week = data.fromTitle.replaceAll('-', '')
                            this.requestParam.startDate = data.from.replaceAll('-', '')
                            break;
                        case 'MONTH':
                            delete this.requestParam.day
                            delete this.requestParam.week
                            delete this.requestParam.startDate
                            this.requestParam.month = data.fromTitle.replaceAll('-', '')
                            break;
                        case 'DATE':
                            delete this.requestParam.month
                            delete this.requestParam.week
                            delete this.requestParam.startDate
                            this.requestParam.day = data.from.replaceAll('-', '')
                            break;
                    }
                    delete this.requestParam.endDate
                    this.requestParam.frequent = data.rangeType
                    console.log('change date after', this.requestParam)

                    let child = Common.vue.getChild(this.$children, 'date-picker');
                    if (child != null) {
                        child.closeDatePicker();
                    }
                    this.isChangingDate = true;
                    this.paging(1);
                    this.listChecked = []
                    this.listCheckedFull = []
                    this.isAll = false
                    this.isAllTracked = []
                    this.listCheckedToolings = []
                },

        handlePickDate(data, frequency){
                    console.log('change date', data, frequency)
                    this.tempDateData = data;
                    switch (frequency) {
                        case 'WEEK':
                            delete this.requestParam.month
                            delete this.requestParam.day
                            this.requestParam.week = data.fromTitle.replaceAll('-', '')
                            this.requestParam.startDate = moment(data.from).format('YYYYMMDD')
                            delete this.requestParam.start
                            delete this.requestParam.end
                            break;
                        case 'MONTH':
                            delete this.requestParam.day
                            delete this.requestParam.week
                            delete this.requestParam.startDate
                            this.requestParam.month = data.fromTitle.replaceAll('-', '')
                            delete this.requestParam.start
                            delete this.requestParam.end
                            break;
                        case 'DATE':
                            delete this.requestParam.month
                            delete this.requestParam.week
                            delete this.requestParam.startDate
                            this.requestParam.day = moment(data.from).format('YYYYMMDD')
                            delete this.requestParam.start
                            delete this.requestParam.end
                            break;
                    }
                    delete this.requestParam.endDate
                    this.requestParam.frequent = frequency
                    console.log('change date after', this.requestParam)

                    this.isChangingDate = true;
                    this.listChecked = []
                    this.listCheckedFull = []
                    this.isAll = false
                    this.isAllTracked = []
                    this.listCheckedToolings = []
                    let child = Common.vue.getChild(this.$children, 'datepicker');
                    if (child != null) {
                        child.closeDatePicker();
                    }
                },

        setPropsTab(param) {
            this.propsTab = param;
        },
    openDatePickerModal1(){
                    let child = Common.vue.getChild(this.$children, 'datepicker');
                    if (child != null) {
                        child.showDatePicker(this.requestParam.frequent);
                    }
                },
    },
     mounted() {
                console.log('%c track', 'background: green; color: white')
                console.log('check date', this.tempDateData)
            },
             }
</script>
