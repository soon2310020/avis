<template>
    <div>
        <div class="row" >
            <!-- date-picker -->
            <div class="time-filter-box" style="display: none;">
                <common-button 
                    button-type="dropdown" 
                    :style-props="timeFilterValue === 'All Time' ? 'margin-right: 0px;' : ''"
                    :click-handler="() => showFilter('time-filter')" 
                    :active="filterVisible === 'time-filter'"
                    
                >{{ timeFilterValue }}
                </common-button>
                <common-dropdown 
                    v-click-outside="onClickOutsideTimefilter"
                    v-if="filterVisible === 'time-filter'" 
                    :items="timeFilterItems" 
                    :click-handler="timeFilterClickHandler"
                    :wrap-style="`${dropdownPosition} width: 145px;`"
                ></common-dropdown>
                <template v-if="timeFilterValue === 'Custom'">
                    <a-date-picker
                      v-model="startDate"
                      format="YYYY-MM-DD"
                      @change="onChangeStartDate"
                    >
                    </a-date-picker>
                     <a-date-picker
                      v-model="endDate"
                      format="YYYY-MM-DD"
                      @change="onChangeEndDate"
                    >
                    </a-date-picker>
                </template>
            </div>
            <!-- first category -->
            <div v-if="indicator">
                <common-button 
                    button-type="dropdown" 
                    :click-handler="() => showFilter('first-category-filter')" 
                    :active="filterVisible === 'first-category-filter'"
                    :color-type="firstCategorySelectedList.length === 0 ? 'blue-fill' : 'blue'"
                >
                <template v-if="firstCategorySelectedList.length === 0">{{indicator.toLowerCase()}}</template>
                <template v-if="firstCategorySelectedList.length > 0">{{firstCategorySelectedList.length}} {{indicator.toLowerCase()}} selected</template>
                </common-button>
                <common-dropdown 
                    v-click-outside="onClickOutsideCategory"
                    ref="firstCategoryFilter"
                    v-show="filterVisible === 'first-category-filter'" 
                    :items="firstCategoryItems"  
                    :wrap-style="`${dropdownPosition} width: 330px;`"
                    checkbox="true" 
                    :all-checkbox="false"
                    searchbox="true" 
                    placeholder="Search Part name"
                    info-text="Select a maximum of 4 parts you would like to filter"
                    :set-result="setFirstCategoryList"
                ></common-dropdown>
            </div>
            <!-- add filter -->
            <div v-if="!indicator">
                <common-button 
                    color-type="blue-fill" 
                    :click-handler="() => showFilter('add-filter')" 
                    :active="filterVisible === 'add-filter'"
                >+ Add Filter</common-button>
                <common-dropdown 
                    v-click-outside="onClickOutsideAddFilter"
                    v-if="filterVisible === 'add-filter'" 
                    :click-handler="addFilterClickHandler"
                    :items="addFilterItems"  
                    :wrap-style="`${dropdownPosition} width: 145px;`"
                ></common-dropdown>
            </div>          
              <!-- delete filter -->
            <div @click="filterCloseHandler" class="close-btn">
            </div>
        </div>
        <div class="row dashboard-card-wrap">
            <common-card title="Production Pattern Analysis" class-props="card-style-3">
                <div class="chart-wrapper">
                    <common-pie-chart 
                        v-if="isPpaAllChart && chartDatasets"
                        style-props="width: 500px; height: 250px;" 
                        font-size="11px" 
                        :datasets="chartDatasets"
                        :key="pieChartKey"
                    ></common-pie-chart>
                    <template v-if="!isPpaAllChart && chartDatasets" >
                        <common-stacked-bar-chart 
                            v-if="firstCategorySelectedList.length > 0" 
                            style-props="width: 600px; height: 250px;" 
                            font-size="12px" 
                            :datasets="chartDatasets"
                            :key="stackedBarChartKey"
                        ></common-stacked-bar-chart>
                        <div class="no-data" v-if="firstCategorySelectedList.length === 0" >
                            <p class="no-data-p">
                                <span>no chart data</span>
                                <span>Please select a filter.</span>
                            </p>
                        </div>
                    </template>
                </div>
            </common-card>
            <common-card title="Quality Detector" class-props="card-style-3">
                <div class="chart-wrapper">
                    <common-stacked-bar-chart 
                        v-if="isPpaAllChart && chartDatasets" 
                        style-props="width: 500px; height: 250px;" 
                        font-size="12px" 
                        :key="qualityDetectorBarChartKey"
                    ></common-stacked-bar-chart> 
                    <template v-if="!isPpaAllChart && chartDatasets">
                         <common-stacked-bar-chart-horizontal
                             v-if="firstCategorySelectedList.length > 0"
                            style-props="width: 600px; height: 250px;" 
                            font-size="12px" 
                            :key="qualityDetectorHorizontalChartKey"
                        ></common-stacked-bar-chart-horizontal>
                        <div class="no-data" v-if="firstCategorySelectedList.length === 0" >
                            <p class="no-data-p">
                                <span>no chart data</span>
                                <span>Please select a filter.</span>
                            </p>
                        </div>
                    </template>
                </div>
            </common-card>
            <common-card title=" " class-props="card-style-3" style-props="flex: auto;">
                <div class="no-data"> Upcoming Feature </div>
            </common-card>
            <common-card title="Tooling's End of Life Cycle" style-props="width: 100%;" class-props="card-style-3 title-border-none">  
                <common-table class-props="table-style-1" style-props="width: 100%; padding-bottom: 15px;">
                    <table style="width: 100%;">
                        <thead>
                            <th v-for="(item, index) in thList" :key="index">
                                <span>{{ item }}<span>
                            </th>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in tdList" :key="index">
                                <td>
                                    <div>
                                        <span class="blue-text">{{ item.toolingId }}</span>
                                    </div>
                                </td>
                                <td>
                                    <div class="end-of-life-cycle-content">
                                        <span>Reaches end of life cycle on </span>
                                        <span class="blue-text">{{item.date}}</span>
                                    </div>
                                </td>
                                <td>
                                    <div class="progress-box">
                                        <p>
                                            <span><b>{{item.value}}</b> / {{item.total}}</span>
                                            <span>{{item.percent}}%</span>
                                        </p>
                                        <!-- <a-progress :percent="item.percent" stroke-width=5 strokeColor="#000" status="active"></a-progress> -->
                                        <a-progress :percent="item.percent" stroke-width=5 :stroke-color="percentColor(item.percent)" status="active"></a-progress>
                                    </div>
                                </td>
                                <td>
                                    <div>
                                        <div class="priority-box" :class="item.priority">{{ item.priority }}</div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </common-table>
                <div class="view-all-row" style="display: none;">
                    <common-button>View all</common-button>
                </div>
            </common-card>
        </div>
        
    </div>
</template>
<script>
module.exports = {
    components: {
        'common-dropdown': httpVueLoader('/components/common/dropdown.vue'),
        'common-card': httpVueLoader('/components/common/card.vue'),
        'common-button': httpVueLoader('/components/common/custom-button.vue'),
        'common-table': httpVueLoader('/components/common/custom-table.vue'),
        'common-pie-chart': httpVueLoader('/components/common/amcharts5/pie-chart.vue'),
        'common-stacked-bar-chart': httpVueLoader('/components/common/amcharts5/stacked-bar-chart.vue'),
        'common-stacked-bar-chart-horizontal': httpVueLoader('/components/common/amcharts5/stacked-bar-chart-horizontal.vue'),
    },
    watch: {
        firstCategorySelectedList(newValue, oldValue){
            if(newValue.length !== oldValue.length){
                this.getPpaChartData();
            }
        }
    },
    methods: {
        onClickOutsideTimefilter(){
            console.log('Timefilter: ', this.filterVisible)
            if(this.filterVisible === 'time-filter'){
                this.filterVisible = '';
            }
        },
        onClickOutsideCategory(){
            console.log('firstCategoryFilter: ', this.filterVisible)
            if(this.filterVisible === 'first-category-filter'){
                this.filterVisible = '';
            }
        },
        onClickOutsideAddFilter(){
            console.log('AddFilter: ', this.filterVisible)
            if(this.filterVisible === 'add-filter'){
                this.filterVisible = '';
            }
        },
        percentColor(percent) {
            // if (percent <= this.riskPointHigh) {
            if (percent <= 33) {
                return "#E34537";
            }
            // if (this.riskPointHigh < percent && percent <= this.riskPointMedium) {
            if (33 < percent && percent <= 66) {
                return "#F7CC53";
            }
            // if (this.riskPointMedium < percent && percent < 100) {
            if (66 < percent && percent < 100) {
                return "#41CE77";
            }
            if (100) {
                return "#3491FF";
            }
        },
        getPpaChartFilter(param){
            axios.get('/api/analysis/ppa/chart/filter/' + param).then(res => {
                res.data.map(item => {
                    this.firstCategoryItems.push({title: item.displayName, id: item.id})
                })
            })
        },
        setFirstCategoryList(selectedList){
            this.firstCategorySelectedList = selectedList;
            
            if(selectedList.length === 0){
                this.chartDatasets = {};
            }

            if(this.indicator){
                this.isPpaAllChart = false;
            }else{
                this.isPpaAllChart = true;
            }
        },
        filterCloseHandler(){
            this.indicator = '';
            this.firstCategoryType = '';
            this.firstCategorySelectedList = [];
            this.getPpaChartData();
            this.isPpaAllChart = true;
        },
        getPpaChartData(){
            let param = '';

            let indicatorParam = '';
            if(this.indicator){
                indicatorParam = `&indicator=${this.indicator}`;
                param += indicatorParam;
            }

            let indicatorNames = [];
            let indicatorNamesParam = ''

            if(this.firstCategorySelectedList){
                this.firstCategorySelectedList.map(item => {
                    indicatorNames.push(item.title);
                })
            }

            if(indicatorNames.length > 0){
                indicatorNamesParam = `&indicatorNames=${[...indicatorNames]}`
                param += indicatorNamesParam
            }
            
            let firstCategoryIdParam = ''
            if(this.firstCategorySelectedList){
                this.firstCategorySelectedList.map(item => {
                    firstCategoryIdParam += `&${this.firstCategoryType}Id=${item.id}`
                })
                param += firstCategoryIdParam;
            }

            let dateParam = '';
            if(this.timeFilterValue === 'Custom'){
                dateParam = `&fromDate=${this.startDate.format('YYYYMMDD')}&toDate=${this.endDate.format('YYYYMMDD')}`;
                param += dateParam;
            }

            axios.get(`/api/analysis/ppa/chart?${param}`).then(res => {
                this.chartDatasets = res.data;
                this.stackedBarChartKey += 1;
                this.pieChartKey += 1;
            })
        },
        timeFilterClickHandler(param){
            if(param.title === 'All Time'){
                this.timeFilterValue = 'All Time'
                this.filterVisible='';
            }
            if(param.title === 'Custom'){
                this.timeFilterValue = 'Custom'
                this.filterVisible='';
            }
        },
        addFilterClickHandler(item){
            if(this.indicator === ''){
                this.indicator = item.title;
                this.firstCategoryType = item.title.toLowerCase();
            }
            let param = ''
            if(item.title.toLowerCase() === 'tooling'){
                param = 'mold'
            }else{
                param = item.title.toLowerCase();
            }
            this.firstCategoryItems = [];
            this.getPpaChartFilter(param);

            setTimeout(() => {
                this.filterVisible = 'first-category-filter';
                console.log('this.filterVisible: ', this.filterVisible);
            }, 50)

        },
        showFilter(param){
            if(this.filterVisible !== param){
                setTimeout(() => {
                    this.filterVisible = param
                }, 50);
            }
        },
        onChangeStartDate(date) {
             this.startDate = date;
        },
        onChangeEndDate(date) {
             this.endDate = date;
        },
    },
    data() {
        return {
            firstCategoryType: '',
            startDate: moment(),
            endDate: moment(),
            pieChartKey: 0,
            stackedBarChartKey: 0,
            qualityDetectorBarChartKey: 0,
            qualityDetectorHorizontalChartKey: 0,
            isPpaAllChart: true,
            isQdAllChart: true,
            firstCategorySelectedList: [],
            indicator: '',
            timeFilterValue: 'All Time',
            dropdownPosition: 'left: 0px; top: calc(100% + 5px);',
            filterVisible: '',
            timeFilterItems: [
                {title: 'All Time'},
                {title: 'Custom'},
            ],
            addFilterItems: [
                {title: 'PRODUCT'},
                {title: 'PART'},
                {title: 'TOOLING'},
                {title: 'SUPPLIER'},
            ],
            firstCategoryItems: [
                {title: 'Part 1 (Part ID)', id: 1},
                {title: 'Part 2 (Part ID)', id: 2},
                {title: 'Part 3 (Part ID)', id: 3},
                {title: 'Part 4 (Part ID)', id: 4},
                {title: 'Part 5 (Part ID)', id: 5},
            ],
            secondCategoryItems: [],
            thList: ['Tooling ID', 'Insights', 'Utilization Rate', 'Priority'],
            tdList: [
                {
                    toolingId: 'T-003',
                    insights: 'Reaches end of life cycle on ',
                    insightsDate: '2022-04-24',
                    value: 992466,
                    total: 1000000,
                    percent: 99.2,
                    priority: 'high',
                },
                 {
                    toolingId: 'T-004',
                    insights: 'Reaches end of life cycle on ',
                    insightsDate: '2022-05-26',
                    value: 975460,
                    total: 1000000,
                    percent: 97.5,
                    priority: 'high',
                }, 
                {
                    toolingId: 'T-005',
                    insights: 'Reaches end of life cycle on ',
                    insightsDate: '2022-06-23',
                    value: 552538,
                    total: 1000000,
                    percent: 55.3,
                    priority: 'high',
                }, 
                {
                    toolingId: 'T-006',
                    insights: 'Reaches end of life cycle on ',
                    insightsDate: '2022-05-05',
                    value: 620233,
                    total: 1000000,
                    percent: 62,
                    priority: 'low',
                }, 
                {
                    toolingId: 'T-007',
                    insights: 'Reaches end of life cycle on ',
                    insightsDate: '2022-09-16',
                    value: 255116,
                    total: 1000000,
                    percent: 25.5,
                    priority: 'low',
                }, 
            ],
            chartDatasets: {},
        };
    },
    mounted() {
        this.getPpaChartData();  
    },
    
};
</script>

<style scpoed>
.chart-wrapper{
    padding: 15px; width: 100%; height: 100%;     display: flex;
    justify-content: center;
    align-items: center;
}
.end-of-life-cycle-content > span{
    margin-right: 5px;
}
.view-all-row{
   display: flex;
    width: 100%;
    justify-content: flex-end;
    padding: 15px;
}
.progress-box{
    height: 100%;
    position: relative;
    align-items: flex-start !important;
    padding-top: 6px;
    }
.progress-box > p {
    width: 100%;
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    margin: 0;
}
.progress-box .ant-progress{
    height: 7px;
    position: absolute;
    margin-top: 10px;    
}
.progress-box .ant-progress-inner{
    background-color: #CBCBCB
}
.progress-box .ant-progress-outer{
    padding-right: 0px !important;
    margin-right: 0px !important;
    }
.progress-box .ant-progress-text{
    display: none;
}
.close-btn {
    width: 25px;
    height: 25px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: 0.5s;
}
.close-btn::after {
    content: '';
    background-image: url(/images/icon/x-icon-black.svg);
    background-size: 10px 10px;
    width: 10px;
    height: 10px;
}
.close-btn:hover{
    background: #DAEEFF 0% 0% no-repeat padding-box;
    box-shadow: 0px 1px 1px #00000029;
}
.close-btn:hover::after{
    background-image: url(/images/icon/x-icon-blue.svg);
}
.row {
    display: flex;
    align-items: center;
    margin-bottom: 18px;
}
.row > div {
    position: relative;
    display: flex;
    margin-right: 4px;
}
.time-filter-box {
    margin-right: 7px;
}
.time-filter-box > div:first-of-type {
    margin-right: 8px
}
.dashboard-card-wrap{
    display: flex;
    flex-wrap: wrap;
    align-items: stretch;
    gap: 18px;
    padding-bottom: 30px;
}
.dashboard-card-wrap > div {
    flex: auto;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.priority-box {
    width: 40px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #fff;
    border-radius: 3px;
}
.priority-box.high {
    background-color: #f67576;
}
.priority-box.low {
    background-color: #41ce77;
}
.blue-text {
    color: #3491ff;
}
.no-data{
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    color: #605F5F;
    font: normal normal bold 16px/18px Helvetica Neue;
    letter-spacing: 0px;
    align-items: center;
}
.no-data-p{
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;
}
.no-data-p > span:first-of-type {
    margin-bottom: 5px;
}
</style>
