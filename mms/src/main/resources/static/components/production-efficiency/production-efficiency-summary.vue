<template>
    <div class="row mb-3 d-flex">
        <a-popover v-for="(item, index) in items" :key="item.id" v-model="item.visible" placement="bottom" trigger="click">
            <a-button class="dropdown_button statistical-item-container">
<!--                <img :width="item.sizeIcon" :src="item.icons" />-->
              <div class="chart-zone-custom-fake"></div>
              <div :class="{'line-custom-zone': index === 3}" class="chart-zone-custom">
                <canvas v-if="index === 0" :id="chartId + '1'"></canvas>
                <canvas v-if="index === 1" :id="chartId + '2'"></canvas>
                <canvas v-if="index === 2" :id="chartId + '3'"></canvas>
                <canvas v-if="index === 3" :id="chartId + '4'"></canvas>
              </div>
                <div class="total-container">
                    <div class="total-title">{{item.title}}</div>
                    <div class="total-value">{{item.total}}</div>
                    <!-- <div class="total-value text-center" v-if="item.total === null">N/A</div>
                    <div class="total-value text-center" v-else>{{item.total}}%</div> -->
                </div>
            </a-button>
            <div slot="content">
                <div class="content-body">
                    <template>
                        <div style="max-height: 250px; width: 340px; overflow-y: scroll;">
                            <div class="row mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                <div class="col-6 mx-0 px-0 mb-1 text-center font-weight-bold">
                                    {{compareId.key === '' ? 'Supplier' : compareId.key === 'Tool' ? resources['tooling_id'] : compareId.key}}
                                </div>
                                <div class="col-6 mx-0 px-0 text-center font-weight-bold">{{item.title}}</div>
                            </div>
                            <div class="row mt-2 mx-1" v-for="(i, index) in item.listSupplier" :key="index">
                                <div class="col-6 mx-0 px-0 text-center">
                                  <a v-if="compareId.key === 'Tool'" href="#" @click.prevent="showMoldDetails(i.moldId)">
                                  {{i.id}}
                                  </a>
                                  <span v-else>{{i.id}}</span>
                                </div>
                                <div class="col-6 mx-0 px-0 text-center" v-if="i.name != null">
                                    <span>{{i.name}}%</span>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </a-popover>
      <chart-part :resources="resources"></chart-part>
      <chart-mold :show-file-previewer="showFilePreviewer" :resources="resources"></chart-mold>
      <mold-details :show-file-previewer="showFilePreviewer" :resources="resources"></mold-details>
      <file-previewer :back="backToMoldDetail"></file-previewer>
    </div>
</template>

<script>
    module.exports = {
        components: {
          'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
          // 'chart-mold': httpVueLoader('/components/chart-mold.vue'),
          // 'mold-details': httpVueLoader('/components/mold-details.vue'),
          "file-previewer": httpVueLoader("/components/mold-detail/file-previewer.vue"),
        },
        data() {
            return {
              chart1: null,
              chart2: null,
              chart3: null,
              chart4: null,
              gradient: null
            };
        },
        props: {
            chartId: String,
            changeProductivity: [Array, Object],
            items: Array,
            compareId: Object,
            resources: Object,
            productivityData: Object,
            showFilePreviewer: Function,
        },
        mounted() {
          this.setupChart();
          this.bindChartPreventive();
          // var self = this;
          // setTimeout(() => {
          //   self.bindChartPreventive();
          // }, 3000)
        },
        methods: {
          setupChart () {
            var self = this;
            self.chart1 = new Chart(this.chartId + '1', self.getChartConfig());
            self.chart2 = new Chart(this.chartId + '2', self.getChartConfig());
            self.chart3 = new Chart(this.chartId + '3', self.getChartConfig());
            self.chart4 = new Chart(this.chartId + '4', self.getChartConfig());
            var backgroundColors = ["#2F31EE", "#D8D8D8"];
            self.chart1.data.datasets[0].backgroundColor = backgroundColors;
            self.chart2.data.datasets[0].backgroundColor = backgroundColors;
            self.chart3.data.datasets[0].backgroundColor = backgroundColors;
            self.chart4.data.datasets[0].backgroundColor = backgroundColors;
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
            var dataset4 = this.chart4.data.datasets[0];
            console.log(this.productivityData, 'this.productivityData')
            if (this.productivityData) {
              dataset1.data = [
                this.roundNum(this.productivityData.normalProductionPercent),
                this.roundNum(100 - this.productivityData.normalProductionPercent)
              ];
              dataset2.data = [
                this.roundNum(this.productivityData.warmUpProductionPercent),
                this.roundNum(100 - this.productivityData.warmUpProductionPercent)
              ];
              dataset3.data = [
                this.roundNum(this.productivityData.coolDownProductionPercent),
                this.roundNum(100 - this.productivityData.coolDownProductionPercent)
              ];
              dataset4.data = [
                this.roundNum(this.productivityData.abnormalProductionPercent),
                this.roundNum(100 - this.productivityData.abnormalProductionPercent)
              ];
              this.chart1.update();
              this.chart2.update();
              this.chart3.update();
              this.chart4.update();
              this.chart1.resize();
              this.chart2.resize();
              this.chart3.resize();
              this.chart4.resize();
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
            let self = this
            self.bindChartPreventive();
          }
        }
    };
</script>


<style scoped>
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
