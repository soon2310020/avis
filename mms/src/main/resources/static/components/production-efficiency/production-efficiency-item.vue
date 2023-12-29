<template>
  <div class="row mx-1"
       style="border-bottom: 2px solid #c8ced3"
       :key="index">
    <div class="col-2 d-flex align-items-center justify-content-center my-4 px-0 mx-0"
         style="border-right: 2px solid #c8ced3">
      <div style="justify-content: center; width: 100%"
           class="body mx-0 my-0 py-0 px-0">
              <span class="font-productivity"
                    style="color: black; height: 74px; margin: auto">{{ getIndex() }}</span>
        <span style="color: black; margin: auto; text-align: center;overflow: hidden;max-width: 100%;text-overflow: ellipsis;">
          <small
                 class="report-item-hyper-link my-0 font-size-10" @click.prevent="showCompanyDetails(supplier)">
            {{ supplier.companyName }}
          </small>
        </span>
      </div>
    </div>
    <div class="col-3 d-flex align-items-center justify-content-center">
      <div style="
                padding: 0px 10px;
                padding-top: 53px;
                justify-content: center;
              "
           class="card-body body mt-1">
        <div class="chart-progress-container">
          <div style="
                    text-align: center;
                    font-weight: bold;
                    font-size: 18px;
                    color: #000000;
                  " class="mb-1">
            <span v-text="resources['production_efficiency']+':'"></span>
            {{ Math.round(supplier.normalProductionPercent).toLocaleString()}}%
          </div>
          <div class="text-center">
            <small class="font-weight-nora font-size-10"
                   style="color: black">
            </small>
          </div>
          <div class="progress" style="background: #f0f3f5">
            <div class="progress-bar bg-warning"
                 role="progressbar"
                 @mouseover="hoverProgressItem"
                 :style="{width: `${supplier.warmUpProductionPercent * 100 }%`}"
                 aria-valuenow="15"
                 aria-valuemin="0"
                 aria-valuemax="100">
              <div class="progress-hint">
                <div class="hint-icon">
                  <div class="over-icon" :style="{'background': '#ffc107'}"></div>
                </div>
                <div class="hint-title">
                  Warm Up Time: {{ Math.round(supplier.warmUpProductionPercent) }}%
                </div>
              </div>
            </div>
            <div class="progress-bar bg-success"
                 @mouseover="hoverProgressItem"
                 role="progressbar"
                 :style="{width: `${supplier.normalProductionPercent * 100 }%`}"
                 aria-valuenow="30"
                 aria-valuemin="0"
                 aria-valuemax="100">
              <div class="progress-hint">
                <div class="hint-icon">
                  <div class="over-icon" :style="{'background': '#4dbd74'}"></div>
                </div>
                <div class="hint-title">
                  <span v-text="resources['normal_production']+':'"></span>
                  {{Math.round(supplier.normalProductionPercent)}}%
                </div>
              </div>
            </div>

            <div class="progress-bar"
                 @mouseover="hoverProgressItem"
                 role="progressbar"
                 style="background: grey"
                 :style="{width: `${supplier.coolDownProductionPercent * 100}%`}"
                 aria-valuenow="20"
                 aria-valuemin="0"
                 aria-valuemax="100">
              <div class="progress-hint">
                <div class="hint-icon">
                  <div class="over-icon" :style="{'background': 'grey'}"></div>
                </div>
                <div class="hint-title">
                  <span v-text="resources['cool_down']+':'"></span>
                  {{ Math.round(supplier.coolDownProductionPercent) }}%
                </div>
              </div>
            </div>
            <div class="progress-bar bg-danger"
                 @mouseover="hoverProgressItem"
                 role="progressbar"
                 :style="{width: `${supplier.abnormalProductionPercent * 100}%`}"
                 aria-valuenow="20"
                 aria-valuemin="0"
                 aria-valuemax="100">
              <div class="progress-hint">
                <div class="hint-icon">
                  <div class="over-icon" :style="{'background': '#f86c6b'}"></div>
                </div>
                <div class="hint-title">
                  <span v-text="resources['abnormal_time']+':'"></span>
                  {{ Math.round(supplier.abnormalProductionPercent) }}%
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-4 mx-0 px-0 d-flex align-items-center justify-content-center summary-info-block">
      <div class="justify-content-center mt-3 mx-3" style="width: 24rem">
        <div class="font-weight-normal my-0 font-size-10 text-left"
           style="color: black">
          {{compareBy === '' ? 'Suplier' : compareBy === 'Tool' ? resources['tooling'] : compareBy}}
          <a
             class="font-weight-nora my-0 font-size-10 "
             @click="handleLableClick(tooling)">
              <span class="report-item-hyper-link">{{companyType(supplier)}}</span><span>{{getSuffixName(supplier)}}</span>
             </a>
          <span style="font-weight: bold;" href>{{ Math.round(supplier.normalProductionPercent).toLocaleString()}}%</span>
        </div>
        <div class="mt-1">
          <div class="font-weight-nora my-0 font-size-10 number-part-hint"
               style="color: black">
<!--            <span>Number of part: </span>-->
            <part-tooltip :parts="tooling.partProductionList" :data="tooling.numberPart" :total-title="''"/>
          </div>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora my-0 font-size-10"
             style="color: black">
            <span style="font-weight: bold;"
                  v-if="supplier.normalEvaluationPercent === null">N/A</span>
            <span style="font-weight: bold;" v-else>
              {{Math.abs(Math.round(supplier.normalEvaluationPercent)).toLocaleString()}}%
              <span v-if="supplier.normalEvaluationPercent > 0">
                increment
              </span>
              <span v-else>decrement</span>
            </span>
            <span v-text="resources['in_normal']"></span>
          </p>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <div class="font-weight-nora my-0 font-size-10"
             style="color: black; display: flex">
            <span style="font-weight: bold;"
                  v-if="supplier.abnormalEvaluationPercent === null">N/A</span>
            <span style="font-weight: bold;" v-else>
                    {{Math.abs(Math.round(supplier.abnormalEvaluationPercent)).toLocaleString()}}%
              <span v-if="supplier.abnormalEvaluationPercent > 0" v-text="resources['increment']"></span>
              <span v-else v-text="resources['decrement']"> </span>
            </span>
            <span style="margin-left: 5px" v-text="resources['in_abnormal']"></span>
          </div>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora my-0 font-size-10"
             style="color: black">
            <span style="font-weight: bold;"
                  v-if="supplier.warmUpEvaluationPercent === null">N/A</span>
            <span style="font-weight: bold;" v-else>
              {{Math.abs(Math.round(supplier.warmUpEvaluationPercent)).toLocaleString()}}%
              <span v-if="supplier.warmUpEvaluationPercent > 0" v-text="resources['increment']">
              </span>
              <span v-else v-text="resources['decrement']"> </span>
            </span>
            <span v-text="resources['in_warm_up']"></span>
          </p>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora font-size-10 mb-0 pb-0"
             style="color: black">
            <span style="font-weight: bold;"
                  v-if="supplier.coolDownEvaluationPercent === null">N/A</span>
            <span style="font-weight: bold;" v-else>
              {{Math.abs(Math.round(supplier.coolDownEvaluationPercent)).toLocaleString()}}%
              <span v-if="supplier.coolDownEvaluationPercent > 0" v-text="resources['increment']"></span>
              <span v-else v-text="resources['decrement']"> </span>
            </span>
            <span v-text="resources['in_cool_down']"></span>
          </p>
        </div>
      </div>
    </div>
    <div class="col-3 chart-supplier mt-3">
      <div class="chart-item">
        <div class="expand-icon" @click.prevent="expandChart">
          <img src="/images/icon/expand-icon.svg" alt="expand icon"/>
        </div>
        <div class="d-flex mt-2 justify-content-center"
             style="color: black; font-weight: 500">
          <div class="char-line mx-1 mt-1"
               :style="[supplier.normalEvaluationPercent >= 0 ? { background: '#68B277' } : { background: '#E2716E' }]"></div>
          <span v-text="resources['trend']+':'"></span>
          <span class="mx-1" v-text="checkTrend(supplier.normalEvaluationPercent)"></span>
        </div>
        <canvas :id="chartId" class="chartSupplier"></canvas>
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
        expandedChart: null,
        parts: []
      };
    },
    props: {
      tooling: Object,
      supplier: Object,
      chartId: String,
      compareBy: String,
      showChart: Function,
      index: Number,
      pageNumber: Number,
      pageSize: Number,
      resources: Object,
      showCompany: Function
    },
    mounted() {
        this.$nextTick(() => {
            this.setupChart();
            this.bindChartData();
        });
    },
    watch: {
      supplier() {
/*
        if (this.firstTime) {
          this.firstTime = false;
          return;
        }
*/
        this.bindChartData();
      }
    },
    computed: {
      expandedChartTitle() {
        let prefixTitle = this.compareBy === '' ? "Supplier's Name" : this.compareBy === 'Tool' ? this.resources['tooling_id'] : this.compareBy + "'s Name";
        return prefixTitle + ': ' + this.companyType(this.supplier);
      },
      expandedChartId() {
        return 'expanded-' + this.chartId;
      },
      expandedModalId() {
        return 'op-production-efficiency-chart-' + this.expandedChartId;
      }
    },
    methods: {
        getIndex() {
            let size = this.pageSize || 5;
            let num = (this.pageNumber || 1) - 1;
            return size * num + this.index + 1;
        },
      getSuffixName(supplier) {
        let objectCode = this.companyType(supplier);
        let extended = this.resources['apostrophe'];
        if (objectCode && objectCode.toString().toUpperCase().endsWith('S')) {
          extended = "'";
        }
        return extended + ' '+ this.resources['production_efficiency'].toLowerCase()+':' ;
      },
      setupChart() {
        this.chart = new Chart($("#" + this.chartId), this.getChartConfig());
      },
      showExpandedModal() {
        $("#" + this.expandedModalId).modal("show");
        this.bindExpandedChart();
      },
      bindExpandedChart() {
        this.bindChart(this.expandedChart);
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
      checkTrend(trend) {
        if (trend == undefined) {
          return 'N/A';
        }
        return trend > 0 ? `+${trend.toFixed(2)}%` :
          (trend === -999999 ? 'INF' : `${trend.toFixed(2)}%`);
      },
      companyType(value) {
        if (!value) {
          return
        }
        let companyNameCode = (this.compareBy === 'Tool' || this.compareBy === '') ? value.code : value.companyName
        return companyNameCode
      },
      bindChartData() {
        this.bindChart(this.chart);
      },
      bindChart(chart) {
        if (
          !this.supplier.chartAbnormalData &&
          !this.supplier.chartCoolDownData &&
          !this.supplier.chartNormalData &&
          !this.supplier.chartWarmUpData
        ) {
          return;
        }
        chart.data.datasets[0].data = this.supplier.chartWarmUpData.map(
          (item) => item.trend
        );
        chart.data.labels = this.supplier.chartWarmUpData.map(
          (item) => item.title
        );

        chart.data.datasets[1].data = this.supplier.chartNormalData.map(
          (item) => item.trend
        );
        chart.data.labels = this.supplier.chartNormalData.map(
          (item) => item.title
        );

        chart.data.datasets[2].data = this.supplier.chartCoolDownData.map(
          (item) => item.trend
        );
        chart.data.labels = this.supplier.chartCoolDownData.map(
          (item) => item.title
        );

        chart.data.datasets[3].data = this.supplier.chartAbnormalData.map(
          (item) => item.trend
        );
        chart.data.labels = this.supplier.chartAbnormalData.map(
          (item) => item.title
        );
        chart.update();
      },
      hoverNumberOfPart(e) {
        $(e.target).find('.number-part-hint-tooltip').css({
          "left": (e.clientX - 150) + 'px',
          "top": (e.clientY - e.layerY + 75) + 'px',
        });
        console.log('hover from hint')
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
        return {
          type: "bar",
          height: 161,
          data: {
            labels: [],
            datasets: [
              {
                data: [],
                fill: true,
                label: "Warm Up Time",
                backgroundColor: hexToRgba(getStyle("--warning"), 50),
                borderColor: "#ffc107",
                lineTension: 0
              },
              {
                data: [],
                fill: true,
                label: "Normal Production",
                backgroundColor: hexToRgba(getStyle("--success"), 50),
                borderColor: "#4dbd74",
                lineTension: 0
              },
              {
                data: [],
                fill: true,
                label: "Cool Down Time",
                backgroundColor: hexToRgba(getStyle("--gray"), 50),
                borderColor: "grey",
                lineTension: 0
              },
              {
                data: [],
                fill: true,
                label: "Abnormal Production",
                backgroundColor: hexToRgba(getStyle("--danger"), 50),
                borderColor: "#f86c6b",
                lineTension: 0
              },
            ],
          },
          options: {
            legend: {
              display: true,
              position: "bottom"
            },
            tooltips: {
              callbacks: {
                label: function (tooltipItem, data) {
                  return ' ' + data.datasets[tooltipItem.datasetIndex].label + ": " + tooltipItem.yLabel.toFixed(2) + "%";
                },
                title: function (tooltipItem, data) {
                  return "";
                },
              },
            },
            scales: {
              yAxes: [
                {
                  stacked: true,
                  scaleLabel: {
                    display: true,
                    labelString: "%",
                    fontColor: "black",
                  },
                  ticks: {
                    beginAtZero: true,
                    steps: 10,
                    stepValue: 5,
                    max: 100
                  }
                },
              ],
              xAxes: [
                {
                  stacked: true,
                },
              ],
            },
          },
        };
      },
      hoverProgressItem(e) {
        $(e.target).find('.progress-hint').css({
          "left": (e.clientX - 65) + 'px',
          "top": (e.clientY - e.layerY + 35) + 'px',
        });
      },
      handleLableClick(tooling){
        console.log(this.compareBy)
                if (this.compareBy === '' || this.compareBy === 'Tool') {
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
    font-family: initial;
  }
</style>
