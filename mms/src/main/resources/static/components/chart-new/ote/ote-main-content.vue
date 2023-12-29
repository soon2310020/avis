<template>
    <div class="ote-main-content">
        <div class="ote-chart-container flex-center">
            <div class="ote-chart-content" :style="{width: totalWidth + 'px', height: totalHeight + 'px'}" v-if="total">
                <div v-if="mediumData.data" class="left-content flex-center" :style="{width: mediumData.width + 'px', marginRight: mediumData.marginRight + 'px'}" @click="toTable(mediumData.details)">
                    <div class="text-center title">
                        <template v-if="mediumData.isShowTitle">
                            <span>{{ mediumData.title }}</span><br />
                        </template>
                        <span>({{ mediumData.data }}/{{ total }})</span>
                    </div>
                </div>
                <div class="right-content">
                    <div v-if="highData.data" class="top-content flex-center" :style="{height: highData.height + 'px', marginBottom: highData.marginBottom + 'px'}" @click="toTable(highData.details)">
                        <div class="text-center title">
                            <template v-if="highData.isShowTitle">
                                <span>{{ highData.title }}</span><br />
                            </template>
                            <span>({{ highData.data }}/{{ total }})</span>
                        </div>
                    </div>
                    <div v-if="lowData.data" class="bottom-content flex-center" @click="toTable(lowData.details)">
                        <div class="text-center title">
                            <template v-if="lowData.isShowTitle">
                                <span>{{ lowData.title }}</span><br />
                            </template>
                            <span>({{ lowData.data }}/{{ total }})</span>
                        </div>
                    </div>
                </div>
            </div>
            <div v-else class="no-data-result">No results</div>
        </div>
        <div class="legend-container">
            <div class="ote-legend-item legend-high">
                <div class="legend-left">
                    <div class="legend-icon"></div>
                </div>
                <div class="legend-middle" v-text="resources['high']"></div>
                <div class="legend-right">70% - 100%</div>
            </div>
            <div class="ote-legend-item legend-medium">
                <div class="legend-left">
                    <div class="legend-icon"></div>
                </div>
                <div class="legend-middle" v-text="resources['medium']"></div>
                <div class="legend-right">30% - 70%</div>
            </div>
            <div class="ote-legend-item legend-low">
                <div class="legend-left">
                    <div class="legend-icon"></div>
                </div>
                <div class="legend-middle" v-text="resources['low']"></div>
                <div class="legend-right">0% - 30%</div>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            resources:Object,
            toTable: Function
        },
        data() {
            return {
                totalWidth: 200,
                totalHeight: 200,
                total: 20,
                lowData: {
                    data: 3,
                    title: 'Low',
                    details: [],
                    isShowTitle: true
                },
                mediumData: {
                    width: 0,
                    data: 12,
                    title: 'Medium',
                    marginRight: 2,
                    details: [],
                    isShowTitle: true
                },
                highData: {
                    height: 0,
                    data: 5,
                    title: 'High',
                    marginBottom: 2,
                    details: [],
                    isShowTitle: true
                },
            };
        },
        methods: {
            resetData() {
                this.total = 0;
                this.highData.data = 0;
                this.mediumData.data = 0;
                this.lowData.data = 0;
            },
            setOverallChatData(data) {
                this.resetData();
                data.forEach(item => {
                    this.total = item.total;
                    if (item.title === this.highData.title) {
                        this.highData.data = item.data;
                        this.highData.details = item.chartDataOteList;
                    } else if (item.title === this.mediumData.title) {
                        this.mediumData.data = item.data;
                        this.mediumData.details = item.chartDataOteList;
                    } else if (item.title === this.lowData.title) {
                        this.lowData.data = item.data;
                        this.lowData.details = item.chartDataOteList;
                    }
                });
                this.calculateDimension();
            },
            calculateDimension() {
                if (!this.total) {
                    return;
                }
                this.mediumData.width = this.mediumData.data / this.total * this.totalWidth;
                this.highData.height = this.highData.data / (this.lowData.data + this.highData.data) * this.totalHeight;
                if (this.highData.data || this.lowData.data) {
                    this.mediumData.marginRight = 2;
                    if (this.lowData.data) {
                        this.highData.marginBottom = 2;
                    } else {
                        this.highData.marginBottom = 0;
                    }
                } else {
                    this.mediumData.marginRight = 0;
                }
                if (this.mediumData.data) {
                    if (this.mediumData.data * 100 / this.total >= 15) {
                        this.mediumData.isShowTitle = true;
                    } else {
                        this.mediumData.isShowTitle = false;
                    }
                }

                let summaryLowHigh = this.highData.data + this.lowData.data;
                if (summaryLowHigh) {
                    if (this.highData.data * 100 / summaryLowHigh >= 10) {
                        this.highData.isShowTitle = true;
                    } else {
                        this.highData.isShowTitle = false;
                    }

                    if (this.lowData.data * 100 / summaryLowHigh >= 15) {
                        this.lowData.isShowTitle = true;
                    } else {
                        this.lowData.isShowTitle = false;
                    }

                }
            }
        },

        computed: {
        },
        watch: {
        },
        mounted() {
        }
    };
</script>

<style scoped>
    .flex-center {
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .ote-chart-container {
        padding: 1rem;
    }
    .ote-chart-content {
        display: flex;
        font-weight: 600;
    }
    .left-content {
        background: #f7cc53;
        height: 100%;
    }
    .right-content {
        flex: 1;
        display: flex;
        flex-direction: column;
    }
    .top-content {
        background: #6fb97d;
        width: 100%;
    }
    .bottom-content {
        background: #f67576;
        width: 100%;
        flex: 1;
    }
    .no-data-result {
        width: 200px;
        height: 200px;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .legend-container {
        margin-top: 1rem;
    }
    .ote-legend-item {
        display: flex;
        border-bottom: 1px solid #CDCDCD;
        padding: 7px 0;
    }
    .legend-left {
        width: 50px;
        display: flex;
        justify-content: center;
    }
    .legend-left .legend-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;
    }
    .legend-high .legend-icon {
        background: #6fb97d;
    }
    .legend-medium .legend-icon {
        background: #f7cc53;
    }
    .legend-low .legend-icon {
        background: #f67576;
    }
    .legend-middle {
        flex: 1;
        text-align: center;
    }
    .legend-right {
        width: 100px;
        text-align: right;
        padding-right: 7px;
    }
    .title {
        z-index: 1;
    }
</style>
