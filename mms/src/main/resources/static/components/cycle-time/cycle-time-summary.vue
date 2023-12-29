<template>
  <div>
    <div class="row">
      <div class="col-lg-12">
        <div class="row mb-3 d-flex">
        <a-popover v-for="(item, index) in items" :key="item.id" v-model="item.visible" placement="bottom" trigger="click">
            <a-button class="dropdown_button statistical-item-container my- py-0" @click="showItem(item)">
                  <div class="chart-zone-custom-fake"></div>
                  <div :class="{'line-custom-zone': index === 3, 'd-none': index === 3 && item.total === '0%'}" class="chart-zone-custom">
                    <canvas v-if="index === 0" :id="chartId + '1'"></canvas>
                    <canvas v-if="index === 1" :id="chartId + '2'"></canvas>
                    <canvas v-if="index === 2" :id="chartId + '3'"></canvas>
                    <trend-chart  v-if="index === 3"  :datasets="chartTrendData" :interactive="false" class="trend-chart" :class="trendData && trendData.trend>=0?'up':'down'"
                                  padding="8 20 8 35" ref="trendChartCycle"></trend-chart>
                  </div>
                  <span class="percent-chart-zone" :style="[(index === 3 || isNaN(item.percent)) ? {'display': 'none'} : {}]">{{item.percent}}%</span>
                <div class="total-container">
                    <div class="total-title">{{item.title}}</div>
                    <div class="total-value mt-1">
                        <span class="mt-1">{{item.total}}</span>
                        <span
                                v-if="item.key != 'trend'"
                                class="font-weight-normal" style="font-size: 16px;">
                    {{item.tooling}}{{ ![0, 1].includes(item.total) ? 's': '' }}
                  </span>
                    </div>
                </div>
            </a-button>
            <div slot="content">
                <div class="content-body">
                    <template>
                        <div v-if="(compareId.key === '' || compareId.key === 'Tool') && item.key !== 'trend'" style="max-height: 250px; width: 450px; overflow-y: scroll;">
                            <div class="row mt-2 mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                <div class="col-4 mx-0 px-0 mb-1 text-center font-weight-bold">
                                    {{resources['tooling_id']}}
                                </div>
                                <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                    {{ item.titleTooling}}</div>
                                <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                    {{ resources['variance']}}</div>
                            </div>
                            <div class="row mt-2 mx-1" v-for="(i, index) in item.toolingList" :key="index">
                                <div class="col-4 mx-0 px-0 text-center">
                                  <a href="#" @click.prevent="showMoldDetails(i.moldId)">
                                    {{ i.moldCode}}
                                  </a>
                                </div>
                                <div class="col-4 mx-0 px-0 text-center" >{{i.shotCount === null ? 'N/A' : formatNumber(i.shotCount)}}</div>
                                <div class="col-4 mx-0 px-0 text-center" >{{i.variance === null ? 'N/A' : i.variance.toFixed(2).toLocaleString("en-US")+ "%"}}</div>
                            </div>
                        </div>
                        <div v-else style="max-height: 250px; width: 300px; overflow-y: scroll;">
                            <div class="row mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                <div class="col-5 mx-0 px-0 mb-1 text-center font-weight-bold">
                                    {{(compareId.key === '' || compareId.key === 'Tool') ? resources['tooling_id'] : compareId.key}}
                                </div>
                                <div class="col-7 mx-0 px-0 text-center font-weight-bold">
                                    {{ (compareId.key === '' || compareId.key === 'Tool') ? item.titleTooling : item.titlesSuppler}}</div>
                            </div>
                            <div class="row mt-2 mx-1" v-for="(i, index) in item.toolingList" :key="index">
                              <div v-if="compareId.key === '' || compareId.key === 'Tool'" class="col-5 mx-0 px-0 text-center">
                                <a href="#" @click.prevent="showMoldDetails(i.moldId)">
                                  {{i.moldCode}}
                                </a>
                              </div>
                              <div v-else class="col-5 mx-0 px-0 text-center">
                                {{i.name}}
                              </div>
                                <div class="col-7 mx-0 px-0 text-center" >{{checkModId(i, item)}}</div>
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
<!--    <chart-mold :show-file-previewer="showFilePreviewer" :resources="resources"></chart-mold>-->
<!--    <mold-details :show-file-previewer="showFilePreviewer" :resources="resources"></mold-details>-->
    <file-previewer :back="backToMoldDetail"></file-previewer>
  </div>
</template>

<script>
    module.exports = {
      components: {
        'chart-part': httpVueLoader('/components/chart-part.vue'),
        // 'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
        'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        "file-previewer": httpVueLoader("/components/mold-detail/file-previewer.vue"),
      },
        data() {
            return {
              chart1: null,
              chart2: null,
              chart3: null,
              gradient: null,
              chartTrendData:[{
                data: [0,0],
                smooth: true,
                fill: true
              }],
            }
        },
        props: {
          chartId: String,
          changeProductivity: [Array, Object],
          compareId: Object,
          items: Array,
          resources: Object,
          trendData: Object,
          showFilePreviewer: Function,
        },
        methods: {
          showItem(item){
            console.log('click item', item, this.compareId)
          },
          setupChart () {
            this.chart1 = new Chart(this.chartId + '1', this.getChartConfig());
            this.chart2 = new Chart(this.chartId + '2', this.getChartConfig());
            this.chart3 = new Chart(this.chartId + '3', this.getChartConfig());
            let backgroundColors = ["#2F31EE", "#D8D8D8"];
            this.chart1.data.datasets[0].backgroundColor = backgroundColors;
            this.chart2.data.datasets[0].backgroundColor = backgroundColors;
            this.chart3.data.datasets[0].backgroundColor = backgroundColors;
          },
          backToMoldDetail() {
            console.log('back to molds detail');
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
              child.backFromPreview();
            }
          },
          roundNum (num) {
            return Math.round(num * 100) / 100
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
            if (this.trendData) {
              const dataTrend = this.roundNum(this.trendData.trend)
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
                this.roundNum(this.trendData.complianceMoldCountPercent),
                this.roundNum(100 - this.trendData.complianceMoldCountPercent)
              ];
              dataset2.data = [
                this.roundNum(this.trendData.belowMoldCountPercent),
                this.roundNum(100 - this.trendData.belowMoldCountPercent)
              ];
              dataset3.data = [
                this.roundNum(this.trendData.aboveMoldCountPercent),
                this.roundNum(100 - this.trendData.aboveMoldCountPercent)
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
              this.$refs.trendChartCycle[0]?.onWindowResize();
            }
          },
          checkModId (trendMold, value) {
                if (!value) {
                    return
                }
                if (!trendMold) {
                    return
                }
                if (this.compareId.key === '' || this.compareId.key === 'Tool') {
                    if(value.key === 'trend') {
                        return trendMold.trend === null ? 'N/A' : (trendMold.trend > 0 ? ("+" + trendMold.trend.toFixed(2) + "%") : trendMold.trend.toFixed(2) + "%" )
                    } else {
                        return trendMold.cycleTime === null ? 'N/A' : trendMold.cycleTime.toFixed(2)
                    }
                } else {
                    if(value.key === 'trend') {
                        return trendMold.trend === null ? 'N/A' : (trendMold.trend > 0 ? ("+" + trendMold.trend.toFixed(2) + "%") : trendMold.trend.toFixed(2) + "%")
                    } else {
                        let total = trendMold.totalMolds === null ? '0' : trendMold.totalMolds
                        let num = trendMold.numMolds === null ? '0' : trendMold.numMolds
                        return num + '/' + total
                    }
                }
            },
          showChart: function (mold, tab) {
            var child = Common.vue.getChild(this.$parent.$children, 'chart-mold');
            if (child != null) {
              child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
            }
          },
          showMoldDetails: function (moldId) {
            console.log('showMoldDetails')
            let param = `id=${moldId}&page=1`;
            axios.get('/api/molds?' + param).then( response => {

              let mold = response.data.content[0];

              var child = Common.vue.getChild(this.$parent.$children, 'chart-mold');
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
          changeProductivity (items) {
            const self = this
            if (!self.chart1) {
              setTimeout(() => {
                self.setupChart();
                self.bindChartPreventive();
              }, 2000)
            } else {
              self.bindChartPreventive();
            }
          },
          items: {
            handler(value) {
              if (value.length) {
                const self = this
                setTimeout(() => {
                  self.setupChart();
                  self.bindChartPreventive();
                  self.$refs.trendChartCycle[0]?.onWindowResize();
                }, 2000)
              }
            },
            immediate: true
          }
        }
    };
</script>

<style scoped src="/components/capacity-utilization/chart.css"></style>
<style scoped>
#customID {
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
  /*white-space: break-spaces;*/
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
            margin: 1%;
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
            height: 109px;
            min-width: 220px;
            border-radius: 10px;
            display: flex;
            justify-content: space-between;
            padding: 0px 26px;
            margin: 1%;
            margin-left: 10p;
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
        width: 10px;
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
        white-space: break-spaces;
        font-weight: 600;
        font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
    }
    .percent-chart-zone {
      position: absolute !important;
      left: -25px;
      width: 175px;
      font-size: 20px;
    }
</style>
