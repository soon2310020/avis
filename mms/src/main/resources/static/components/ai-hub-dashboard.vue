<template>
    <div>
        <div class="row">
            <!-- date-picker -->
            <div class="time-filter-box" style="display: none">
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
                    <a-date-picker v-model="startDate" format="YYYY-MM-DD" @change="onChangeStartDate"> </a-date-picker>
                    <a-date-picker v-model="endDate" format="YYYY-MM-DD" @change="onChangeEndDate"> </a-date-picker>
                </template>
            </div>
            <div v-if="filterFocus === 'category-filter' && selectedCategoryList?.length > 0">
                <!-- category select list -->
                <!-- <div v-for="(item, index) in selectedCategoryList" :key="index"> -->
                <div>
                    <common-button
                        button-type="dropdown"
                        :click-handler="() => showFilter(`category-filter-${1}`)"
                        :active="filterVisible === `category-filter-${1}`"
                        :color-type="firstCategorySelectedList?.length === 0 ? 'blue-fill' : 'blue'"
                    >
                        <template v-if="firstCategorySelectedList?.length === 0">{{
                            selectedCategoryList[1 - 1].title.toLowerCase()
                        }}</template>
                        <template v-if="firstCategorySelectedList?.length > 0"
                            >{{ firstCategorySelectedList?.length }}
                            {{ selectedCategoryList[1 - 1].title.toLowerCase() }} selected</template
                        >
                    </common-button>
                    <common-dropdown
                        v-click-outside="onClickOutsideFirstCategory"
                        v-show="filterVisible === `category-filter-${1}`"
                        :items="categoryItems.first"
                        :wrap-style="`${dropdownPosition} width: 330px;`"
                        checkbox="true"
                        :all-checkbox="false"
                        searchbox="true"
                        placeholder="Search Part name"
                        info-text="Select a maximum of 4 parts you would like to filter"
                        :set-result="setFirstCategoryList"
                        :disabled-handler="setCategoryFilterDisable"
                    ></common-dropdown>
                </div>
            </div>
            <!-- add filter -->
            <div v-if="selectedCategoryList?.length === 0">
                <common-button
                    color-type="blue-fill"
                    :click-handler="() => showFilter('add-filter')"
                    :active="filterVisible === 'add-filter'"
                    >+ Add Filter</common-button
                >
                <common-dropdown
                    v-click-outside="onClickOutsideAddFilter"
                    v-if="filterVisible === 'add-filter'"
                    :click-handler="addFilterHandler"
                    :items="filterCategoryItems"
                    :wrap-style="`${dropdownPosition} width: 145px;`"
                ></common-dropdown>
            </div>
            <!-- delete filter -->
            <div @click="closeFilterHandler" class="close-btn"></div>
        </div>
        <div class="row dashboard-card-wrap">
            <common-card title="Production Pattern Analysis" class-props="card-style-3">
                <div class="chart-wrapper">
                    <common-pie-chart
                        v-if="isAllChart && ppaChartDatasets"
                        style-props="width: 500px; height: 250px;"
                        font-size="11px"
                        :datasets="ppaChartDatasets"
                        :key="ppaPieChartKey"
                    ></common-pie-chart>
                    <template v-if="!isAllChart && ppaChartDatasets">
                        <common-stacked-bar-chart
                            v-if="firstCategorySelectedList?.length > 0"
                            style-props="width: 600px; height: 250px;"
                            font-size="12px"
                            :datasets="ppaChartDatasets"
                            :key="ppaBarChartKey"
                        ></common-stacked-bar-chart>
                        <div class="no-data" v-if="firstCategorySelectedList?.length === 0">
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
                        v-if="isAllChart && qdChartDatasets"
                        style-props="width: 500px; height: 250px;"
                        font-size="12px"
                        :key="qdBarChartKey"
                        :datasets="qdChartDatasets"
                    ></common-stacked-bar-chart>
                    <template v-if="!isAllChart && qdChartDatasets">
                        <common-stacked-bar-chart-horizontal
                            v-if="firstCategorySelectedList?.length > 0"
                            style-props="width: 600px; height: 250px;"
                            font-size="12px"
                            :key="qdHorizontalChartKey"
                            :datasets="qdChartDatasets"
                        ></common-stacked-bar-chart-horizontal>
                        <div class="no-data" v-if="firstCategorySelectedList?.length === 0">
                            <p class="no-data-p">
                                <span>no chart data</span>
                                <span>Please select a filter.</span>
                            </p>
                        </div>
                    </template>
                </div>
            </common-card>
            <common-card title=" " class-props="card-style-3" style-props="flex: auto;">
                <div class="no-data">Upcoming Feature</div>
            </common-card>
            <common-card title="Tooling's End of Life Cycle" style-props="width: 100%;" class-props="card-style-3 title-border-none">
                <div class="no-data" style="position: absolute; z-index: 1">Upcoming Feature</div>
                <common-table class-props="table-style-1" style-props="width: 100%; padding-bottom: 15px;">
                    <table style="width: 100%">
                        <thead>
                            <th v-for="(item, index) in thList" :key="index">
                                <span>{{ item }}</span>
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
                                        <span class="blue-text">{{ item.date }}</span>
                                    </div>
                                </td>
                                <td>
                                    <div class="progress-box">
                                        <p>
                                            <span
                                                ><b>{{ item.value }}</b> / {{ item.total }}</span
                                            >
                                            <span>{{ item.percent }}%</span>
                                        </p>
                                        <a-progress
                                            :percent="item.percent"
                                            stroke-width="5"
                                            :stroke-color="percentColor(item.percent)"
                                            status="active"
                                        ></a-progress>
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
                <div class="view-all-row" style="display: none">
                    <common-button>View all</common-button>
                </div>
            </common-card>
        </div>
    </div>
</template>
<script>
module.exports = {
    components: {
        'common-dropdown': httpVueLoader('/components/common/dropdown-temp.vue'),
        'common-card': httpVueLoader('/components/common/card.vue'),
        'common-button': httpVueLoader('/components/common/custom-button.vue'),
        'common-table': httpVueLoader('/components/common/custom-table.vue'),
        'common-pie-chart': httpVueLoader('/components/common/amcharts5/pie-chart-temp.vue'),
        'common-stacked-bar-chart': httpVueLoader('/components/common/amcharts5/stacked-bar-chart.vue'),
        'common-stacked-bar-chart-horizontal': httpVueLoader('/components/common/amcharts5/stacked-bar-chart-horizontal.vue'),
    },
    watch: {
        selectedCategoryList(newValue) {
            if (newValue?.length === 0) {
                this.isAllChart = true;
            }
        },
        firstCategorySelectedList(newValue, oldValue) {
            if (newValue?.length !== oldValue?.length) {
                this.getPpaChartData();
            }
        },
        secondCategorySelectedList(newValue, oldValue) {
            if (newValue?.length !== oldValue?.length) {
                this.getPpaChartData();
            }
        },
    },
    methods: {
        setCategoryFilterDisable(item) {
            this.firstCategorySelectedList?.length;
            if (this.firstCategorySelectedList?.length >= 4 && !item.checked) {
                return true;
            } else {
                return false;
            }
        },
        onClickOutsideTimefilter() {
            if (this.filterVisible === 'time-filter') {
                this.filterVisible = '';
            }
        },
        onClickOutsideFirstCategory() {
            if (this.filterVisible === 'category-filter-1') {
                this.filterVisible = '';
            }
        },
        // onClickOutsideSecondCategory() {
        //     if (this.filterVisible === 'category-filter-1') {
        //         this.filterVisible = '';
        //     }
        // },
        onClickOutsideAddFilter() {
            if (this.filterVisible === 'add-filter') {
                this.filterVisible = '';
            }
        },
        percentColor(percent) {
            // if (percent <= this.riskPointHigh) {
            if (percent <= 33) {
                return '#E34537';
            }
            // if (this.riskPointHigh < percent && percent <= this.riskPointMedium) {
            if (33 < percent && percent <= 66) {
                return '#F7CC53';
            }
            // if (this.riskPointMedium < percent && percent < 100) {
            if (66 < percent && percent < 100) {
                return '#41CE77';
            }
            if (100) {
                return '#3491FF';
            }
        },
        getPpaChartFilter(param) {
            this.categoryItems.first = [];
            this.secondCategoryItems = [];
            axios.get('/api/analysis/ppa/chart/filter/' + param).then((res) => {
                res.data?.map((item) => {
                    this.categoryItems.first.push({ title: item.displayName, id: item.id });
                    this.secondCategoryItems.push({ title: item.displayName, id: item.id });
                });
            });
        },
        setFirstCategoryList(selectedList) {
            this.firstCategorySelectedList = selectedList;

            if (selectedList?.length === 0) {
                this.ppaChartDatasets = {};
            }

            if (this.indicator) {
                this.isAllChart = false;
            } else {
                this.isAllChart = true;
            }
        },
        setSecondCategoryList(selectedList) {
            this.secondCategorySelectedList = selectedList;

            if (selectedList?.length === 0) {
                this.ppaChartDatasets = {};
            }

            if (this.indicator) {
                this.isAllChart = false;
            } else {
                this.isAllChart = true;
            }
        },
        closeFilterHandler() {
            // 맨 마지막 항목이 무엇인지 체크한 후 맨마지막 항목 다시 필터리스트에 추가
            let lastSelectedCategoryItem = this.selectedCategoryList.pop();
            if (lastSelectedCategoryItem) {
                this.filterCategoryItems.push(lastSelectedCategoryItem);
            }

            // 맨 마지막 항목 카테고리만 제거
            // 맨 마지막 항목이 첫번째 항목일 경우 indicator 지우기

            this.indicator = '';
            this.firstCategoryType = '';
            this.firstCategorySelectedList = [];
            this.secondCategorySelectedList = [];
            this.getPpaChartData();
            this.isAllChart = true;
        },
        getPpaChartData() {
            let param = '';

            let indicatorParam = '';
            if (this.indicator) {
                indicatorParam = `&indicator=${this.indicator}`;
                param += indicatorParam;
            }

            let indicatorNames = [];
            let indicatorNamesParam = '';
            let firstCategoryIdParam = '';

            if (this.firstCategorySelectedList) {
                this.firstCategorySelectedList?.map((item) => {
                    indicatorNames.push(item.title);
                    firstCategoryIdParam += `&${this.firstCategoryType}Id=${item.id}`;
                });
                param += firstCategoryIdParam;
            }

            if (indicatorNames?.length > 0) {
                indicatorNamesParam = `&indicatorNames=${[...indicatorNames]}`;
                param += indicatorNamesParam;
            }

            let dateParam = '';
            if (this.timeFilterValue === 'Custom') {
                dateParam = `&fromDate=${this.startDate.format('YYYYMMDD')}&toDate=${this.endDate.format('YYYYMMDD')}`;
                param += dateParam;
            }

            axios.get(`/api/analysis/ppa/chart?${param}`).then((res) => {
                this.ppaChartDatasets = res.data.ppa;
                this.qdChartDatasets = res.data.qd;
                this.ppaPieChartKey += 1;
                this.ppaBarChartKey += 1;
                this.qdBarChartKey += 1;
                this.qdHorizontalChartKey += 1;
            });
        },
        timeFilterClickHandler(param) {
            if (param.title === 'All Time') {
                this.timeFilterValue = 'All Time';
                this.filterVisible = '';
            }
            if (param.title === 'Custom') {
                this.timeFilterValue = 'Custom';
                this.filterVisible = '';
            }
        },
        addFilterHandler(item) {
            // 선택한 항목 리스트에서 제거하기
            // 선택된 카테고리 리스트에 추가하기
            this.filterCategoryItems = this.filterCategoryItems.filter((filterItem) => {
                if (filterItem.title === item.title) {
                    this.selectedCategoryList.push(item);
                }
                return filterItem.title !== item.title;
            });

            if (this.indicator === '') {
                this.indicator = item.title;
                this.firstCategoryType = item.title.toLowerCase();
            }

            let param = '';
            if (item.title.toLowerCase() === 'tooling') {
                param = 'mold';
            } else {
                param = item.title.toLowerCase();
            }

            // this.categoryItems.first = [];
            // this.secondCategoryItems = [];
            this.getPpaChartFilter(param);

            setTimeout(() => {
                this.filterFocus = 'category-filter';
                this.filterVisible = 'category-filter-1';
            }, 50);
        },
        showFilter(param) {
            if (this.filterVisible !== param) {
                setTimeout(() => {
                    this.filterVisible = param;
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
            selectedCategoryList: [],
            filterNumCount: 0,
            filterSelectedList: [],
            filterFocus: '',
            firstCategoryType: '',
            startDate: moment(),
            endDate: moment(),
            ppaPieChartKey: 0,
            ppaBarChartKey: 0,
            qdBarChartKey: 0,
            qdHorizontalChartKey: 0,
            isAllChart: true,
            firstCategorySelectedList: [],
            secondCategorySelectedList: [],
            indicator: '',
            timeFilterValue: 'All Time',
            dropdownPosition: 'left: 0px; top: calc(100% + 5px);',
            filterVisible: '',
            timeFilterItems: [{ title: 'All Time' }, { title: 'Custom' }],
            filterCategoryItems: [{ title: 'PRODUCT' }, { title: 'PART' }, { title: 'TOOLING' }, { title: 'SUPPLIER' }],

            categoryItems: {
                first: [],
                second: [],
                third: [],
                fourth: [],
            },

            secondCategoryItems: [],
            thList: ['Tooling ID', 'Insights', 'Utilization Rate', 'Priority'],
            tdList: [],
            ppaChartDatasets: {},
            qdChartDatasets: {},
        };
    },
    mounted() {
        axios.get('/json/aihub-tdlist.json').then((res) => {
            this.tdList = res.data.tdList;
        });
        this.getPpaChartData();
    },
};
</script>

<style scpoed>
.chart-wrapper {
    padding: 15px;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
.end-of-life-cycle-content > span {
    margin-right: 5px;
}
.view-all-row {
    display: flex;
    width: 100%;
    justify-content: flex-end;
    padding: 15px;
}
.progress-box {
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
.progress-box .ant-progress {
    height: 7px;
    position: absolute;
    margin-top: 10px;
}
.progress-box .ant-progress-inner {
    background-color: #cbcbcb;
}
.progress-box .ant-progress-outer {
    padding-right: 0px !important;
    margin-right: 0px !important;
}
.progress-box .ant-progress-text {
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
.close-btn:hover {
    background: #daeeff 0% 0% no-repeat padding-box;
    box-shadow: 0px 1px 1px #00000029;
}
.close-btn:hover::after {
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
    margin-right: 8px;
}
.dashboard-card-wrap {
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
.no-data {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    color: #605f5f;
    font: normal normal bold 16px/18px Helvetica Neue;
    letter-spacing: 0px;
    align-items: center;
    background-color: rgba(255, 255, 255, 0.8);
}
.no-data-p {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;
}
.no-data-p > span:first-of-type {
    margin-bottom: 5px;
}
</style>
