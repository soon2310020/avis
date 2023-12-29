<template>
    <div>
        <div class="row">
            <div class="col-lg-12">
                <div class="row mb-3 d-flex" >
                    <a-popover v-for="(item, index) in items" :key="item.id" v-model="item.visible" placement="bottom" trigger="click">
                        <a-button class="dropdown_button statistical-item-container my-0 py-0" @click="changeTooLing(item.id)">
                            <div class="chart-zone-custom-fake"></div>
                            <div :class="{'line-custom-zone': index === 3, 'd-none': index === 3 && item.total === '0%'}" class="chart-zone-custom">
                              <canvas v-if="index === 0" :id="chartId + '1'"></canvas>
                              <canvas v-if="index === 1" :id="chartId + '2'"></canvas>
                              <canvas v-if="index === 2" :id="chartId + '3'"></canvas>
                              <trend-chart  v-if="index === 3" :datasets="chartTrendData" :interactive="false" class="trend-chart" :class="productivityData && productivityData.trend >= 0 ? 'up' : 'down'"
                                            padding="8 20 8 35" ref="trendChartCapa"></trend-chart>
                            </div>
                            <div class="total-container">
                                <div class="total-title">{{item.title}}</div>
                                <div class="total-value" v-if="item.total === '0%'">N/A</div>
                                <div class="total-value" v-else>{{item.total}}</div>
                            </div>
                        </a-button>
                        <div slot="content">
                            <div class="content-body">
                                <template>
                                    <div style="max-height: 250px; width: 300px; overflow-y: scroll;">
                                        <div class="row mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                            <div class="col-5 mx-0 px-0 mb-1 text-center font-weight-bold">
                                                {{productivityQuery.Compare.key === 'Tool' ? resources['tooling_id'] : productivityQuery.Compare.key}}</div>
                                            <div class="col-7 mx-0 px-0 text-center font-weight-bold">{{titleTooling}}</div>
                                        </div>
                                        <div class="row mt-2 mx-1" v-for="(item, index) in listTooling" :key="index">
                                            <div class="col-5 mx-0 px-0 text-center">
                                              <a v-if="productivityQuery.Compare.key === 'Tool'" href="#" @click.prevent="showMoldDetails(item.moldId)">
                                              {{item.id}}
                                              </a>
                                              <span v-else>{{item.id}}</span>
                                            </div>
                                            <div class="col-7 mx-0 px-0 text-center">{{item.name}}</div>
                                        </div>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </a-popover>
                </div>
            </div>
        </div>
      <chart-part :resources="resources"></chart-part>
<!--      <chart-mold :show-file-previewer="showFilePreviewer" :resources="resources"></chart-mold>-->
<!--      <mold-details :show-file-previewer="showFilePreviewer" :resources="resources"></mold-details>-->
      <file-previewer :back="backToMoldDetail"></file-previewer>
    </div>
</template>

<script>
    module.exports = {
      components: {
        'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
        'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        "file-previewer": httpVueLoader("/components/mold-detail/file-previewer.vue"),
      },
        data() {
            return {
                chart1: null,
                chart2: null,
                chart3: null,
                chart4: null,
                titleTooling: "",
                itemid: "",
                listTooling: {},
                items: [
                    {
                        id: 1,
                        key: 'percentageProductivity',
                        icons: "/images/icon/TotalProductivity.svg",
                        title: vm.resources['overall_productivity'],
                        moldcode: "moldCode",
                        name: 'name',
                        total: "",
                        visible: false,
                        moldId: 'id',
                    },
                    {
                        id: 2,
                        key: 'producedQuantity',
                        icons: "/images/icon/TotalProducedAmount.svg",
                        title: vm.resources['total_produced_amount'],
                        moldcode: "moldCode",
                        name: 'name',
                        total: "",
                        visible: false,
                        moldId: 'id',
                    },
                    {
                        id: 3,
                        key: 'maxCapacity',
                        moldcode: "moldCode",
                        name: 'name',
                        icons: "/images/icon/Availableproductivity.svg",
                        title: vm.resources['remaining_capacity'],
                        total: "",
                        visible: false,
                        moldId: 'id',
                    },
                    {
                        id: 4,
                        key: 'trend',
                        icons: "/images/icon/Trend.svg",
                        title: vm.resources['overall_trend'],
                        moldcode: "moldCode",
                        name: 'name',
                        total: "",
                        visible: false,
                        moldId: 'id',
                    },
                ],
              gradient: null,
              chartTrendData:[{
                data: [0,0],
                smooth: true,
                fill: true
              }],
            };
        },
        props: {
            chartId: String,
            changeProductivity: [Array, Object],
            gettooling: Array,
            query: Function,
            toolingQuery: Function,
            productivityQuery: Object,
            isExport: Boolean,
            resources: Object,
            productivityData: Object,
            showFilePreviewer: Function,
        },
        mounted() {
          const self = this
          this.$nextTick(function () {
            setTimeout(()=>{
              self.$refs.trendChartCapa[0]?.onWindowResize();
            },2000);
          })
        },
        methods: {
          setupChart () {
            this.chart1 = new Chart(this.chartId + '1', this.getChartConfig());
            this.chart2 = new Chart(this.chartId + '2', this.getChartConfig());
            this.chart3 = new Chart(this.chartId + '3', this.getChartConfig());
            var backgroundColors = ["#2F31EE", "#D8D8D8"];
            this.chart1.data.datasets[0].backgroundColor = backgroundColors;
            this.chart2.data.datasets[0].backgroundColor = backgroundColors;
            this.chart3.data.datasets[0].backgroundColor = backgroundColors;
          },
          roundNum (num) {
            return Math.round(num * 100) / 100
          },
          changeTooLing(item) {
              this.itemid = item;
              let param = null;
              if (item === 1) {
                  this.titleTooling = vm.resources['overall_productivity'];
                  param = 'PRODUCTIVITY';
              }
              if (item === 2) {
                  this.titleTooling = vm.resources['total_produced_amount'];
                  param = 'PRODUCED_QUANTITY';
              }
              if (item === 3) {
                  this.titleTooling = vm.resources['remaining_capacity'];
                  param = 'AVAILABLE_PRODUCTIVITY';
              }
              if (item === 4) {
                  this.titleTooling = vm.resources['overall_trend'];
                  param = 'TREND';
              }
              if (this.isExport)
                  return;
              this.toolingQuery('variable', param);
          },
          getChartConfig() {
            return {
              type: "doughnut",
                data: {
                plugins: {
                  datalabels: {
                    display: false
                  }
                },
                datasets: [
                  {
                    data: [],
                    borderWidth: 0,
                    backgroundColor: []
                  }
                ]
              },
              options: {
                cutoutPercentage: 75,
                    cutout: 75,
                    plugins: {
                  labels: {
                    display: false,
                        enabled: false,
                        render: "label",
                        arc: false,
                        position: "outside",
                        fontSize: 13,
                        outsidePadding: 4,
                        textMargin: 4
                  }
                },
                tooltips: {
                  enabled: false
                },
                legend: {
                  display: false
                },
                title: {
                  display: false
                }
              }
            }
          },
          bindChartPreventive: function() {
            var dataset1 = this.chart1.data.datasets[0];
            var dataset2 = this.chart2.data.datasets[0];
            var dataset3 = this.chart3.data.datasets[0];
            if (this.productivityData) {
              const dataTrend = this.roundNum(this.productivityData.trend)
              let trendValue = 0;
              if (dataTrend < -100) {
                trendValue = -100
              } else if (dataTrend > 100) {
                trendValue = 100
              } else {
                trendValue = dataTrend
              }
              let datasetChart = []
              dataset1.data = [
                this.roundNum(this.productivityData.avgProductivity),
                this.roundNum(100 - this.productivityData.avgProductivity)
              ];
              dataset2.data = [
                this.roundNum(this.productivityData.totalProductivityPercent),
                this.roundNum(100 - this.productivityData.totalProductivityPercent)
              ];
              dataset3.data = [
                this.roundNum(this.productivityData.availableProductivityPercent),
                this.roundNum(100 - this.productivityData.availableProductivityPercent)
              ];
              datasetChart = [
                0,
                trendValue * 40 / 100,
                trendValue * 30 / 100,
                trendValue * 50 / 100,
                trendValue * 60 / 100,
                trendValue
              ]
              if (dataTrend > 0) {
                if (dataTrend < 30) {
                  datasetChart = [
                    0,
                    trendValue * 15 / 100,
                    trendValue * 12 / 100,
                    trendValue * 16 / 100,
                    trendValue * 17 / 100,
                    trendValue * 20 / 100
                  ]
                }
              }
              else {
                if (dataTrend > -30) {
                  datasetChart = [
                    0,
                    trendValue * 15 / 100,
                    trendValue * 12 / 100,
                    trendValue * 16 / 100,
                    trendValue * 17 / 100,
                    trendValue * 20 / 100
                  ]
                }
              }
              this.chart1.update();
              this.chart2.update();
              this.chart3.update();
              this.chart1.resize();
              this.chart2.resize();
              this.chart3.resize();
              this.chartTrendData=[{
                data: datasetChart,
                smooth: true,
                fill: true
              }];
              this.$refs.trendChartCapa[0]?.onWindowResize();

            }
          },
          backToMoldDetail() {
            console.log('back to molds detail');
            var child = Common.vue.getChild(this.$children, 'mold-details');
            if (child != null) {
              child.backFromPreview();
            }
          },
          showChart: function (mold, tab) {
            const child = Common.vue.getChild(this.$parent.$parent.$children, 'chart-mold');
            if (child != null) {
              child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
            }
          },
          showMoldDetails: function (moldId) {
            console.log('showMoldDetails')
            let param = `id=${moldId}&page=1`;
            axios.get('/api/molds?' + param).then( response => {

              let mold = response.data.content[0];

              const child = Common.vue.getChild(this.$parent.$parent.$children, 'chart-mold');
              if (child != null) {
                let reasonData = { reason: '', approvedAt: '' }
                const tab = 'switch-detail'
                if (mold.dataSubmission === 'DISAPPROVED') {
                  axios.get(`/api/molds/data-submission/${mold.id}/disapproval-reason`).then(function (response) {
                    reasonData = response.data;
                    // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
                    reasonData.approvedAt = reasonData.approvedAt ? vm.convertTimestampToDateWithFormat(reasonData.approvedAt, 'MMMM DD YYYY HH:mm:ss') : '-';
                    mold.notificationStatus = mold.dataSubmission;
                    mold.reason = reasonData.reason;
                    mold.approvedAt = reasonData.approvedAt;
                    mold.approvedBy = reasonData.approvedBy;
                    // child.showMoldDetails(mold);
                    this.showChart(mold, tab)
                  });
                } else {
                  // child.showMoldDetails(mold);
                  this.showChart(mold, tab)
                }

              }
            }).catch( error => {
              console.log(error.response);
            });

            let elements = document.getElementsByClassName('ant-popover-placement-bottom');
            for (let i = 0; i < elements.length; i++)
            {
              elements[i].style.display = "none";
            }
          },
        },
        watch: {
            gettooling (item) {
                let itemObject = this.items.filter((item) => item.id === this.itemid)[0];
                console.log('watch itemObject',itemObject);
                if(itemObject==null) return;
                if (this.itemid === 1) {
                    this.listTooling = item.map(item => {
                        return {
                            id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
                            name: item[itemObject.key] == null || item[itemObject.key]==Infinity ? 'N/A' :(
                                item[itemObject.key] === -999999 ? 'INF'
                                :item[itemObject.key].toFixed(2) + '%' ),
                            moldId: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldId] : null
                        }
                    })
                }else if (this.itemid === 4) {
                    this.listTooling = item.map(item => {
                        if(item[itemObject.key] === null || item[itemObject.key]==Infinity ) {
                            return {
                                id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
                                name: 'N/A',
                                moldId: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldId] : null
                            }
                        } else if (item[itemObject.key] === -999999) {
                            return {
                                id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
                                name: 'INF',
                                moldId: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldId] : null
                            }
                        } else {
                            return {
                                id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
                                name: item[itemObject.key].toFixed(2) + '%',
                                moldId: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldId] : null
                            }
                        }
                    })
                } else {
                    this.listTooling = item.map(item => {
                        return {
                            id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
                            name: item[itemObject.key].toLocaleString("en-US"),
                            moldId: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldId] : null
                        }
                    })
                }
              this.bindChartPreventive()
            },
            changeProductivity (items) {
                let self = this
                self.productivity = items
                for (const i in self.items) {
                    self.items[0]["total"] = self.productivity.avgProductivity.toFixed(2) + "%";
                    self.items[1]["total"] = Number(self.productivity.totalProductivity).toLocaleString("en-US");
                    self.items[2]["total"] = Number(self.productivity.availableProductivity).toLocaleString("en-US");
                    self.items[3]["total"] = self.productivity.trend === null ? 'N/A' : self.productivity.trend.toFixed(2) + "%";
                }
                self.bindChartPreventive();
            },
            items: {
              handler(value) {
                if (value.length) {
                  const self = this
                  setTimeout(() => {
                    self.setupChart();
                    self.bindChartPreventive();
                    self.$refs.trendChartCycle[0]?.onWindowResize();
                  }, 3000)
                }
              },
              immediate: true
            }
        },
    };
</script>

<style scoped src="/components/capacity-utilization/chart.css"></style>

<style scoped>
    #chartCapa4 {
      width: 110px!important;
    }
    #undefined4 {
      width: 110px!important;
    }
    .chart-zone-custom-fake {
      position: relative;
      width: 50%;
      height: 100%;
    }
    .chart-zone-custom {
      position: absolute;
      width: 175px;
      height: 87px;
      left: -25px;
      top: 50%;
      transform: translate(0, -50%);
    }
    .total-container {
      width: 50%;
      white-space: break-spaces;
      text-align: right;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 78px;
    }
    .chart-zone-custom canvas {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
    }
    .statistical-item-container {
      /*position: relative;*/
    }
    @media (max-width: 576px) {
        .statistical-item-container {
            width: 100%;
            height: 109px;
            min-width: 220px;
            border-radius: 10px;
            display: flex;
            justify-content: space-between;
            padding: 0px 26px;
            color: black;
            margin-top: 20px;
            align-items: center;
            margin-bottom: 5px;
            -moz-box-shadow: 3px 3px #e8e8e8;
            -webkit-box-shadow: 3px 3px #e8e8e8;
            box-shadow: 3px 3px #e8e8e8;
        }
    }
    @media (min-width: 576px) {
        .statistical-item-container {
            width: 23%;
            margin: 1%;
            height: 109px;
            min-width: 220px;
            border-radius: 10px;
            display: flex;
            justify-content: space-between;
            padding: 0px 26px;
            background: white;
            color: black;
            border: 0.5px solid #e8e8e8;
            align-items: center;
            margin-bottom: 5px;
            -moz-box-shadow: 3px 3px #e8e8e8;
            -webkit-box-shadow: 3px 3px #e8e8e8;
            box-shadow: 3px 3px 3px 3px #e8e8e8;
        }
    }
    ::-webkit-scrollbar{
        width: 20px;
    }
    ::-webkit-scrollbar-thumb {
        background: #c8ced3;
    }
    .total-value {
        font-size: 26px;
        /*font-weight: 900;*/
      line-height: 100%;
        /*font-family: Raleway;*/
      font-weight: 600;
      font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;

    }
    .total-title {
        font-size: 16px;
        font-weight: 600;
        font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
    }
</style>
