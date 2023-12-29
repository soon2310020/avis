<template>
    <div class="row mx-1"
         style="border-bottom: 2px solid #c8ced3">
        <div class="col-2 d-flex align-items-center my-4 mx-0 px-0"
             style="border-right: 2px solid #c8ced3">
            <div style="justify-content: center; width: 100%"
                 class="body mx-0 px-0 my-0 py-0">
                <span class="font-productivity" style="color: black; height: 74px; margin: auto">
                    {{ getIndex() }}
                </span>
                <span style="margin: auto; text-align: center;overflow: hidden;max-width: 100%;text-overflow: ellipsis;">
                    <small class="font-size-10 report-item-hyper-link" @click.prevent="showCompanyDetails(tooling)">
                        {{ tooling.name === null ? tooling.mold.companyName : tooling.name}}
                    </small>
              </span>
            </div>
        </div>
        <div class="col-3">
            <div style="padding: 53px 10px 0 10px; justify-content: center;"
                 class="card-body body mt-1">
                <div class="">
                    <div style="text-align: center; font-size: 18px;"
                         class="mb-1">
                         <part-tooltip :parts="tooling.partProductionList" :data="tooling.numberPart" :total-title="`${resources['total_produced']}: ${tooling.producedQuantity.toLocaleString('en-US')}`"/>
                        <!-- <span v-text="resources['total_produced']+':'" ></span> {{ tooling.producedQuantity.toLocaleString('en-US') }} -->
                    </div>
                    <div class="progress">
                        <div class="progress-item" @mouseover="hoverProgressItem"
                             :style="[(tooling.percentageProductivity >= 70 && tooling.percentageProductivity <= 100)
                                        ? {'background': '#6FB97D', width: `${tooling.percentageProductivity}%`} :
                                        (tooling.percentageProductivity <= 30)
                                        ? {'background': '#F67576', width: `${tooling.percentageProductivity}%`} :
                                        (tooling.percentageProductivity > 100)
                                        ? {'background': '#6FB97D', 'width': '100%'} : {'background': '#F7CC53', width: `${tooling.percentageProductivity}%`}]">
                            <div class="progress-hint">
                                <div class="hint-icon">
                                    <div class="within-icon" :style="
                                        (tooling.percentageProductivity >= 70 && tooling.percentageProductivity <= 100)
                                        ? {'background': '#6FB97D'} :
                                        (tooling.percentageProductivity <= 30)
                                        ? {'background': '#F67576'} :
                                        (tooling.percentageProductivity > 100)
                                        ? {'background': '#6FB97D'} : {'background': '#F7CC53'} "></div>
                                </div>
                                <div class="hint-title">{{ 'Within Capacity: ' + getWithinCapacity(tooling) }}</div>
                            </div>
                        </div>

                        <div class="progress-item" @mouseover="hoverProgressItem" :class="{'empty-bar': !parseInt(tooling.percentageProductivity)}" :style="[
                             (tooling.percentageProductivity > 100)
                             ? {'background': '#8752D7'} : {'background': '#ccc'},
                             {width: Math.abs(parseInt(tooling.percentageProductivity) - 100) + '%'}]">
                            <div class="progress-hint" v-if="tooling.percentageProductivity > 100">
                                <div class="hint-icon">
                                    <div class="over-icon" :style="
                                         (tooling.percentageProductivity > 100)
                                         ? {'background': '#8752D7'} : {'background': '#ccc'}"></div>
                                </div>
                                <div class="hint-title">{{ 'Over Capacity: ' + getOverCapacity(tooling) }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-4 mx-0 px-0 d-flex align-items-center justify-content-center">
            <div class="mx-2" style="width: 24rem">
                <div class="font-weight-normal my-0 font-size-10" style="color: black">
                    {{(compareBy.key === '' || compareBy.key === 'Tool') ? resources['tooling'] : compareBy.key }}
                    <a class="font-weight-nora my-0 font-size-10 " @click.prevent="handleLableClick(tooling)">
                        <span class="report-item-hyper-link" v-text="checkMoldCode(tooling)"></span>
                    </a><span class="move-left lower-case" v-text="getSuffixName(tooling)"></span>
                    <span class="move-left" style="font-weight: bold;" href>{{ Math.round(tooling.percentageProductivity)}}%</span>
                </div>
                <div class="mt-1">
                    <div class="font-weight-nora my-0 font-size-10 number-part-hint"
                       style="color: black">
<!--                        <span>Number of part: </span>-->
                        <part-tooltip :parts="tooling.partProductionList" :data="tooling.numberPart" :total-title="''"/>
                    </div>
                </div>
                <div class="row mt-1">
                    <div class="mx-2 for-production"></div>
                    <p class="font-weight-nora my-0 font-size-10"
                       style="color: black">
                        <span v-text="resources['production_target']+':'" ></span>
                        <span style="font-weight: bold;" href>
                            {{roundDayText(tooling.meetTargetDay)}}
                        </span>
                    </p>
                </div>
                <div class="row mt-1">
                    <div class="mx-2 for-production"></div>
                    <p class="font-weight-nora my-0 font-size-10"
                       style="color: black">
                        <span v-text="resources['below_production_target']+':'" ></span>
                        <span style="font-weight: bold;" href>
                            {{roundDayText(tooling.lowProductivityDay)}}
                        </span>
                    </p>
                </div>
                <div class="row mt-1">
                    <div class="mx-2 for-production"></div>
                    <p class="font-weight-nora my-0 font-size-10"
                       style="color: black">
                        <span v-text="resources['no_operation']+':'" ></span>
                        <span style="font-weight: bold;" href>
                            {{roundDayText(tooling.noOperationDay)}}
                        </span>
                    </p>
                </div>
                <div class="row mt-1">
                    <div class="mx-2 for-production"></div>
                    <p class="font-weight-nora font-size-10 mb-0 pb-0"
                       style="color: black">
                        <span v-text="resources['portion_part']+':'" ></span>
                        <span style="font-weight: bold;" href>
                            {{ Math.round(tooling.percentageTotalProductivity)}}%
                        </span>
                    </p>
                </div>
            </div>
        </div>
        <div class="col-3 align-items-center chart-cycle-time mt-3">
            <div class="chart-item">
                <div class="expand-icon" @click.prevent = "expandChart">
                    <img src="/images/icon/expand-icon.svg" alt="expand icon" />
                </div>
                <div class="d-flex justify-content-center" style="color: #000; font-weight: 500;">
                    <div class="char-line mx-1 mt-1"
                         :style="[tooling.trend >= 0 ? { background: '#68B277' } : { background: '#E2716E' }]"
                         style="font-weight: 500">
                    </div>
                    <span v-text="resources['trend']+':'" ></span>
                    <span class="mx-1" v-text="checkTrend( tooling.trend)"></span>
                </div>
                <canvas :id="chartId"></canvas>
            </div>
        </div>
        <report-item-chart :chart-title="expandedChartTitle" :chart-id="expandedChartId" :modal-id="expandedModalId" :resources="resources"></report-item-chart>
    </div>
</template>

<script>
    module.exports = {
        components: {
            "report-item-chart": httpVueLoader("/components/report-item-chart.vue"),
            'part-tooltip': httpVueLoader("/components/report/part-number-tooltip.vue"),
        },
        data() {
            return {
                firstTime: true,
                chart: null,
                expandedChart: null
            };
        },
        props: {
            tooling: Object,
            showChart: Function,
            chartId: String,
            compareBy: Object,
            index: Number,
            pageNumber: Number,
            pageSize: Number,
            resources: Object,
            showCompany: Function
        },
        mounted() {
            this.setupChart();
            this.bindChartData();
        },
        watch: {
            tooling() {
                // if (this.firstTime) {
                //     this.firstTime = false;
                //     return;
                // }
                this.bindChartData();
            }
        },
        computed: {
            expandedChartTitle() {
                let prefixTitle = (this.compareBy.key === '' || this.compareBy.key === 'Tool') ? this.resources['tooling_id'] : this.compareBy.key + "'s Name";
                return prefixTitle + ': ' + this.checkMoldCode(this.tooling);
            },
            expandedChartId() {
                return 'expanded-' + this.chartId;
            },
            expandedModalId() {
                return 'op-capacity-chart-' + this.expandedChartId;
            }
        },
        methods: {
            getIndex() {
                let size = this.pageSize || 5;
                let num = (this.pageNumber || 1) - 1;
                return size * num + this.index + 1;
            },
            setupChart() {
                this.chart = new Chart($("#" + this.chartId), this.getChartConfig());
            },
            showExpandedModal() {
                $("#" + this.expandedModalId).modal("show");
                this.bindExpandedChart();
            },
            bindExpandedChart() {
                this.bindChart(this.expandedChart, (item) => item.title);
            },
            expandChart() {
                if (!this.expandedChart) {
                    this.expandedChart = new Chart($("#" + this.expandedChartId), this.getBaseChartConfig());
                }
                let child = Common.vue.getChild(this.$children, 'report-item-chart');
                if (child != null) {
                    this.showExpandedModal();
                }
            },

            checkMoldCode(mold) {
                if (!mold) {
                    return
                }
                return this.compareBy.key === '' || this.compareBy.key === 'Tool' ? mold.moldCode : mold.name
            },
            getSuffixName(mold) {
                let objectCode = this.checkMoldCode(mold);
                let extended = this.resources['apostrophe'];
                if (objectCode && objectCode.toString().toUpperCase().endsWith('S')) {
                    extended = "'";
                }
                return extended + ' ' + this.resources['capacity_utilization']+':';
            },
            checkTrend(trend) {
                if (trend == undefined) {
                    return 'N/A';
                }

                return trend > 0 ? `+${trend.toFixed(2)}%` :
                  (trend === -999999 ? 'INF' : `${trend.toFixed(2)}%`);
            },
            roundDayText(day) {
                return ((this.compareBy.key === '' || this.compareBy.key === 'Tool') ?
                  Math.round(day) : Math.round(day * 10) / 10) + ' ' +
                  this.resources['report_day'] + (day > 1 ? this.resources['days'] : '');
            },
            hoverProgressItem(e) {
                $(e.target).find('.progress-hint').css({
                    "left": (e.clientX - 65) + 'px',
                    "top": (e.clientY - e.layerY + 35) + 'px',
                });
            },
            hoverNumberOfPart(e) {
                $(e.target).find('.number-part-hint-tooltip').css({
                    "left": (e.clientX - 150) + 'px',
                    "top": (e.clientY - e.layerY + 75) + 'px',
                });
            },
            getWithinCapacity(tooling) {
                if (!tooling) {
                    return '';
                }
                return tooling.percentageProductivity > 100 ? tooling.maxCapacity : tooling.producedQuantity;
            },
            getOverCapacity(tooling) {
                if (!tooling) {
                    return '';
                }
                return tooling.percentageProductivity > 100 ? tooling.producedQuantity - tooling.maxCapacity : '';
            },
            bindChartData() {
                this.bindChart(this.chart, item => item.title);
            },
            bindChart(chart, labelCallback){
                if (!this.tooling.capacityReportDataList) {
                    return;
                }
                chart.type = 'bar';
                let arrAvailable = []
                let arrOver = []
                let arrCurrent = []
                let arrDownTime = []
                this.tooling.capacityReportDataList.map((item) => {
                  arrAvailable.push( item.availableOutputPercent)
                  arrOver.push(item.overCapacityPercent)
                  let avail=item.overCapacityPercent!=null && item.overCapacityPercent>0 ? 100:item.outputCapacityPercent;
                  arrCurrent.push(avail)
                  arrDownTime.push(item.availableDowntimePercent)
                });
                chart.data.datasets[0].data = arrCurrent
                chart.data.datasets[1].data = arrAvailable
                chart.data.datasets[2].data = arrDownTime
                chart.data.datasets[3].data = arrOver

                // chart.data.datasets[0].data = this.tooling.chartData.map((item) => Math.round(item.dataPercent*100)/100);
                if (labelCallback) {
                  chart.data.labels = this.tooling.capacityReportDataList.map(labelCallback);
                }
                // if (this.tooling.trend > 0) {
                //     chart.data.datasets[0].borderColor = '#68B277';
                //     chart.data.datasets[0].backgroundColor = hexToRgba(getStyle("--success"), 50);
                // } else {
                //     chart.data.datasets[0].borderColor = '#E2716E';
                //     chart.data.datasets[0].backgroundColor = hexToRgba(getStyle("--danger"), 50);
                // }
                // }
                chart.update();
            },
            getChartConfig() {
                let chartConfig = this.getBaseChartConfig();
                chartConfig.options.scales.xAxes[0].ticks = {
                    callback: function (value, index, values) {
                        return " ";
                    }
                };
                chartConfig.options.legend.display = false;
                chartConfig.options.scales.yAxes[0].scaleLabel.labelString = '(%)';
                return chartConfig;
            },
            getBaseChartConfig() {
              let chartConfig = {
                type: 'bar',
                data: {
                  labels: [],
                  datasets: [
                    {
                      label: this.resources['current_capacity'],
                      data: [],
                      backgroundColor: '#6FB87C',
                      hoverBackgroundColor: '#6FBC73'
                    },
                    {
                      label: this.resources['available_capacity'],
                      data: [],
                      backgroundColor: '#F6CB53',
                      hoverBackgroundColor: '#FFCA37'
                    },
                    {
                      label: this.resources['planned_downtime'],
                      data: [],
                      backgroundColor: '#808080',
                      hoverBackgroundColor: '#727272'
                    },
                    {
                      label: this.resources['over_capacity'],
                      data: [],
                      backgroundColor: '#8651D7',
                      hoverBackgroundColor: '#9857FB'
                    },
                  ]
                },
                options: {
                  hover: {
                    mode: 'nearest'
                  },
                  scales: {
                    yAxes: [{
                      stacked: true,
                      scaleLabel: {
                        display: true,
                        labelString: "Capacity Utilization (%)",
                        fontColor: "black",
                      },
                      ticks: {
                          beginAtZero: true,
                          steps: 10,
                          stepValue: 5
                      },

                    }],
                    xAxes: [{
                      stacked: true,
                      scaleLabel: {
                          display: true,
                          fontColor: "black",
                      },
                      ticks: {
                        beginAtZero: true
                      }
                    }]
                  },
                  tooltips: {
                    callbacks: {
                      label: function(tooltipItems, data) {
                        console.log(tooltipItems, data)
                        return data.datasets[tooltipItems.datasetIndex].label + ': ' + tooltipItems.yLabel +'%';
                      }
                    }
                  },
                  legend: {
                    display: true,
                    position: "bottom",
                    labels: {
                      boxWidth: 45,
                      padding: 15,
                      fontSize: 13
                    }
                  }
                }
              };
              return  chartConfig
            },
            handleLableClick(tooling){
                if (this.compareBy.key === '' || this.compareBy.key === 'Tool') {
                    this.showChart(tooling)
                } else {
                    this.showCompanyDetails(tooling)
                }
            },
            showCompanyDetails: function (tooling) {
                console.log(tooling)
                let companyId = ''
                if (tooling.mold) {
                    companyId = tooling.mold.companyId;
                } else {
                    companyId = tooling.id;
                }
                this.getSingleCompany(companyId)
                .then(response => {
                    console.log(response.data)
                    this.showCompany(response.data)
                })
                .catch(error => {
                    console.log(error)
                })
            },
            async getSingleCompany(companyId){
                const response = await axios.get(`/api/companies/${companyId}`);
                return response;
            }
        },

    };
</script>
<style>
table {
  width: auto;
  border-collapse: collapse;
  border-spacing: 0;
  font-size: 13px;
}
table td,
th {
  padding: 0 1rem 0 0.25rem;
  border: 1px solid white;
  max-width: 150px;
  /* -webkit-line-clamp: 2; */
}
table tr:first-child td,
th {
  border-top: 0;
  /* white-space: nowrap; */
}
table tr td:first-child,
table tr th:first-child {
  border-left: 0;
}
table tr:last-child td,
table tr:last-child th {
  border-bottom: 0;
}
table tr td:last-child,
table tr th:last-child {
  border-right: 0;
}
.ant-tooltip-inner {
  width: fit-content;
}
</style>
<style scoped>
.font-productivity {
    font-family: font;
}


</style>
