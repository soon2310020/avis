<template>
  <div class="row mx-1" style="border-bottom: 2px solid #c8ced3" :key="index">
    <div
      class="col-2 d-flex align-items-center my-4 px-0"
      style="border-right: 2px solid #c8ced3"
    >
      <div
        style="justify-content: center"
        class="card-body body mx-0 px-0 my-0 py-0 text-center"
      >
        <span
          class="font-productivity"
          style="color: black; height: 74px; margin: auto"
          >{{getIndex()  }}</span
        >
        <span style="color: black; margin: auto;overflow: hidden;max-width: 100%;text-overflow: ellipsis;">
          <small
            class="report-item-hyper-link my-0 font-size-10" @click.prevent="showCompanyDetails(tooling)"
            v-text="companyCycleTime(tooling)"
          >
          </small>
        </span>
      </div>
    </div>
    <div class="col-3">
      <div
        style="padding: 0px 10px; padding-top: 53px; justify-content: center"
        class="card-body body mt-1"
      >
        <div class="">
          <div
            style="
              text-align: center;
              font-weight: 400;
              font-size: 18px;
              color: #1890ff;
            "
            class="mb-1"
            v-if="tooling.percentageCompliance === null"
          >
          <!-- <span v-text="resources['cycle_time_compliance'] + ': '"></span> N/A% -->
            <a-popover v-model="visible" placement="bottom" trigger="click">
              <span @click="showInfor()" v-text="resources['cycle_time_compliance'] + ': '"></span> N/A%
              <div slot="content">
                  <div class="content-body">
                      <template>
                          <div v-if="(compareId.key === '' || compareId.key === 'Tool')" style="max-height: 250px; width: 450px; overflow-y: scroll;">
                              <div class="row mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                  <div class="col-4 mx-0 px-0 mb-1 text-center font-weight-bold">
                                      {{resources['tooling_id']}}
                                  </div>
                                  <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                      {{ "Shots Number"}}</div>
                                  <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                      {{ resources['variance']}}</div>
                              </div>
                              <div class="row mt-2 mx-1" v-for="(i, index) in [tooling]" :key="index">
                                  <div class="col-4 mx-0 px-0 text-center">
                                      {{ i.moldCode}}
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
                                      {{ (compareId.key === '' || compareId.key === 'Tool') ? "Shots Number" : "Compliant Tools"}}</div>
                              </div>
                              <div class="row mt-2 mx-1" v-for="(i, index) in [tooling]" :key="index">
                                  <div class="col-5 mx-0 px-0 text-center">
                                      {{i.moldCode}}
                                  </div>
                                  <div class="col-7 mx-0 px-0 text-center" >{{checkModId(i, defaultItem)}}</div>
                              </div>
                          </div>
                      </template>
                  </div>
              </div>
          </a-popover>
          </div>

          <div
            style="
              text-align: center;
              font-weight: 400;
              font-size: 18px;
            "
            class="mb-1 report-item-hyper-link"
            v-else
          >
            <!-- <span v-text="resources['cycle_time_compliance'] + ': '"></span> -->
            <a-popover v-model="visible" placement="bottom" trigger="click">
              <span @click="showInfor()" v-text="resources['cycle_time_compliance'] + ': '"></span>
              <div slot="content">
                  <div class="content-body">
                      <template>
                          <div v-if="(compareId.key === '' || compareId.key === 'Tool')" style="max-height: 250px; width: 450px; overflow-y: scroll;">
                              <div class="row mx-1" style="border-bottom: 1px solid #e8e8e8;">
                                  <div class="col-4 mx-0 px-0 mb-1 text-center font-weight-bold">
                                      {{resources['tooling_id']}}
                                  </div>
                                  <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                      {{ "Shots Number"}}</div>
                                  <div class="col-4 mx-0 px-0 text-center font-weight-bold">
                                      {{ resources['variance']}}</div>
                              </div>
                              <div class="row mt-2 mx-1" v-for="(i, index) in [tooling]" :key="index">
                                  <div class="col-4 mx-0 px-0 text-center">
                                      {{ i.moldCode}}
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
                                      {{ (compareId.key === '' || compareId.key === 'Tool') ? "Shots Number" : "Compliant Tools"}}</div>
                              </div>
                              <div class="row mt-2 mx-1" v-for="(i, index) in [tooling]" :key="index">
                                  <div class="col-5 mx-0 px-0 text-center">
                                      {{i.moldCode}}
                                  </div>
                                  <div class="col-7 mx-0 px-0 text-center" >{{checkModId(i, defaultItem)}}</div>
                              </div>
                          </div>
                      </template>
                  </div>
              </div>
          </a-popover>
            {{ Math.round(tooling.percentageCompliance) }}%
          </div>
          <!-- (tooling.percentageCompliance > 0)
                      ? {'background': '#68B277'}
                      : {'background': '#E2716E'}, -->
          <div class="progress">
            <div
              class="progress-item"
              :style="[
                { background: 'rgb(127, 184, 129)' },
                // {'background': checkPercentageCompliance(tooling.percentageCompliance)},
                { width: `${tooling.percentageCompliance}%` },
              ]"
            ></div>
            <div
              class="progress-item"
              :style="[
                { background: 'rgb(241, 207, 96)' },
                { width: `${tooling.percentageL1}%` },
              ]"
            ></div>
            <div
              class="progress-item"
              :style="[
                { background: 'rgb(216, 122, 116)' },
                { width: `${tooling.percentageL2}%` },
              ]"
            ></div>
          </div>
          <!--                    <div class="progress-full">-->
          <!--                        <div-->
          <!--                                :style="[-->
          <!--                  {'background': checkPercentageCompliance(tooling.percentageCompliance)},-->
          <!--                  {width: `${tooling.percentageCompliance}%`}]"-->
          <!--                                class="progress"-->
          <!--                        ></div>-->
          <!--                    </div>-->
        </div>
      </div>
    </div>
    <div
      class="col-4 mx-0 px-0 d-flex align-items-center justify-content-center"
    >
      <div class="mx-3" style="width: 24rem">
        <p class="font-weight-normal my-0 font-size-10" style="color: black">
          {{
            compareBy.key === "" || compareBy.key === "Tool"
              ? resources["tooling"]
              : compareBy.key
          }}
          <a
            class="font-weight-nora my-0 font-size-10 "
            @click="handleLableClick(tooling)"
            ><span class="report-item-hyper-link">{{ tooling.moldCode }}</span></a
          ><span>{{ getSuffixName(tooling) }}</span>
          <span
            style="font-weight: bold;"
            v-text="compliantType(tooling)"
          ></span>
        </p>
        <div class="mt-1">
          <div class="font-weight-nora my-0 font-size-10 number-part-hint"
               style="color: black">
<!--            <span>Number of part: </span>-->
            <part-tooltip :parts="tooling.partProductionList" :data="tooling.numberPart" :total-title="''"/>
          </div>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora my-0 font-size-10" style="color: black">
            <span
              class="report-item-hyper-link"
              v-if="tooling.percentageCompliance === null"
              href
              >N/A%</span>
            <a-tooltip v-else placement="bottom">
              <template slot="title">
                <table>
                  <tr>
                    <th>Tooling ID</th>
                    <th>Shots Within Limits</th>
                  </tr>
                  <tr v-for="(detail, index) in tooling.details" :key="index">
                    <td>{{ detail.moldCode }}</td>
                    <td>
                      {{ formatNumber(detail.percentageCompliance) }}% ({{
                        formatNumber(detail.shotCountCompliance)
                      }})
                    </td>
                  </tr>
                </table>
              </template>
              <span class="report-item-hyper-link" href
                >{{ Math.round(tooling.percentageCompliance) }}%
              </span>
              <span v-text="resources['shots_done_within']"></span>
              <span class="report-item-hyper-link">
                <span>&nbsp;</span><span v-text="resources['limits']"></span
              ></span>
            </a-tooltip>
          </p>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora my-0 font-size-10" style="color: black">
            <span
              class="report-item-hyper-link"
              v-if="tooling.percentageL1 === null"
              href
              >N/A%</span
            >
            <a-tooltip v-else placement="bottom">
              <template slot="title">
                <table>
                  <tr>
                    <th>Tooling ID</th>
                    <th>Shots Above L1</th>
                    <th>Shots Below L1</th>
                  </tr>
                  <tr v-for="(detail, index) in tooling.details" :key="index">
                    <td>{{ detail.moldCode }}</td>
                    <td>
                      {{ formatNumber(detail.percentageAboveL1) }}% ({{
                        formatNumber(detail.shotCountAboveL1)
                      }})
                    </td>
                    <td>
                      {{ formatNumber(detail.percentageBelowL1) }}% ({{
                        formatNumber(detail.shotCountBelowL1)
                      }})
                    </td>
                  </tr>
                </table>
              </template>
              <span class="report-item-hyper-link" href
                >{{ Math.round(tooling.percentageL1) }}%
              </span>
              <span v-text="resources['shots_done_beyond']"></span>
              <span class="report-item-hyper-link"> L1</span>
            </a-tooltip>
          </p>
        </div>
        <div class="row mt-1">
          <div class="mx-2 for-production"></div>
          <p class="font-weight-nora my-0 font-size-10" style="color: black">
            <span
              class="report-item-hyper-link"
              v-if="tooling.percentageL2 === null"
              href
              >N/A%</span
            >
            <a-tooltip v-else placement="bottom">
              <template slot="title">
                <table>
                  <tr>
                    <th>Tooling ID</th>
                    <th>Shots Above L2</th>
                    <th>Shots Below L2</th>
                  </tr>
                  <tr v-for="(detail, index) in tooling.details" :key="index">
                    <td>{{ detail.moldCode }}</td>
                    <td>
                      {{ formatNumber(detail.percentageAboveL2) }}% ({{
                        formatNumber(detail.shotCountAboveL2)
                      }})
                    </td>
                    <td>
                      {{ formatNumber(detail.percentageBelowL2) }}% ({{
                        formatNumber(detail.shotCountBelowL2)
                      }})
                    </td>
                  </tr>
                </table>
              </template>
              <span class="report-item-hyper-link" href
                >{{ Math.round(tooling.percentageL2) }}%
              </span>
              <span v-text="resources['shots_done_beyond']"></span>
              <span class="report-item-hyper-link"> L2</span>
            </a-tooltip>
          </p>
        </div>
      </div>
    </div>
    <div class="col-3 align-items-center chart-cycle-time">
      <div class="chart-item">
        <div class="expand-icon" @click.prevent="expandChart">
          <img src="/images/icon/expand-icon.svg" alt="expand icon" />
        </div>
        <div
          class="d-flex mt-2 justify-content-center"
          style="width: 165px; margin: 0px auto; font-weight: 500; color: #000"
        >
          <div
            class="char-line mx-1 mt-1"
            :style="[
              tooling.trend >= 0
                ? { background: '#68B277' }
                : { background: '#E2716E' },
            ]"
          ></div>
          <span v-text="resources['trend'] + ':'"></span>
          {{ chartTrend(tooling) }}
        </div>
        <canvas :id="chartId"></canvas>
      </div>
    </div>
    <report-item-chart
      :chart-title="expandedChartTitle"
      :chart-id="expandedChartId"
      :modal-id="expandedModalId"
      :resources="resources"
    ></report-item-chart>
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
      visible: false,
      defaultItem: {
                  id: 1,
                  key: 'complianceMoldCount',
                  icons: "/images/icon/CT_Compliance.svg",
                  title: vm.resources['ct_compliance'],
                  moldcode: "moldCode",
                  total: "",
                  titleTooling: vm.resources['shots_number'],
                  titlesSuppler: 'Compliant Tools',
                  toolings: vm.resources['overall_trend'],
                  tooling: vm.resources['tooling'],
                  toolingList: [],
                  visible: false
                },
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
    compareId: Object,
    showCompany: Function
  },
  mounted() {
    console.log('init cycle-time-item');
    this.setupChart();
    this.bindChartData();
  },
  watch: {
    tooling() {
/*
      if (this.firstTime) {
        this.firstTime = false;
        return;
      }
*/
      this.bindChartData();
    },
  },
  computed: {
    expandedChartTitle() {
      let prefixTitle =
        this.compareBy.key === "" || this.compareBy.key === "Tool"
          ? this.resources["tooling_id"]
          : this.compareBy.key + "'s Name";
      return prefixTitle + ": " + this.tooling.moldCode;
    },
    expandedChartId() {
      return "expanded-" + this.chartId;
    },
    expandedModalId() {
      return "op-cycle-time-chart-" + this.expandedChartId;
    },
  },
  methods: {
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
    hoverNumberOfPart(e) {
      $(e.target).find('.number-part-hint-tooltip').css({
        "left": (e.clientX - 150) + 'px',
        "top": (e.clientY - e.layerY + 75) + 'px',
      });
      console.log('hover from hint')
    },
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
        this.expandedChart = new Chart(
          $("#" + this.expandedChartId),
          this.getBaseChartConfig()
        );
      }
      let child = Common.vue.getChild(this.$children, "report-item-chart");
      if (child != null) {
        this.showExpandedModal();
      }
    },
    companyCycleTime(value) {
      if (!value) {
        return;
      }
      let company =
        this.compareBy.key === "" || this.compareBy.key === "Tool"
          ? value.mold != null
            ? value.mold.companyName
            : "N/A"
          : value.moldCode;
      return company;
    },
    getSuffixName(mold) {
      let extended = this.resources["apostrophe"];
      if (
        mold.moldCode &&
        mold.moldCode.toString().toUpperCase().endsWith("S")
      ) {
        extended = "'";
      }
      return this.compareBy.key === "" || this.compareBy.key === "Tool"
        ? extended + " " + this.resources["cycle_time"].toLowerCase() + ":"
        : extended + " " + this.resources["compliant_tools"] + ":";
    },
    checkPercentageCompliance(value) {
      if (!value) {
        return;
      }
      return value > 0 && value <= 30
        ? "#F67576"
        : value >= 70
        ? "#6FB97D"
        : "#F7CC53";
    },
    compliantType(value) {
      if (!value) {
        return;
      }
      let cycleTimeType = value.numMolds + "/" + value.totalMolds;
      let compliedValue =
        this.compareBy.key === "" || this.compareBy.key === "Tool"
          ? value.complianceValue
          : cycleTimeType;
      return compliedValue;
    },
    chartTrend(value) {
      if (!value || value.trend==undefined) {
        return "N/A";
      }
      return value.trend > 0
        ? "+" + value.trend.toFixed(2) + "%"
        : value.trend.toFixed(2) + "%";
    },
    bindChartData() {
      this.bindChart(this.chart, (item) => item.title);
    },
    bindChart(chart, labelCallback) {
      if (!this.tooling.chartData) {
        return;
      }
      if (chart != null) {
        chart.type = "bar";
        let arr1 = [];
        let arr2 = [];
        let arr3 = [];
        this.tooling.chartData.map((item) => {
          arr1.push(item.cycleTime.toFixed(2));
          arr2.push(item.cycleTimeL1);
          arr3.push(item.cycleTimeL2);
        });
        chart.data.datasets[0].data = arr1;
        chart.data.datasets[1].data = arr2;
        chart.data.datasets[2].data = arr3;
        chart.data.labels = this.tooling.chartData.map(labelCallback);

        // if (this.tooling.trend > 0) {
        //     chart.data.datasets[0].borderColor = '#68B277';
        //     chart.data.datasets[0].backgroundColor = hexToRgba(getStyle("--success"), 50);
        // } else {
        //     chart.data.datasets[0].borderColor = '#E2716E';
        //     chart.data.datasets[0].backgroundColor = hexToRgba(getStyle("--danger"), 50);
        // }
        chart.update();
      }
    },

    getChartConfig() {
      let chartConfig = this.getBaseChartConfig();
      chartConfig.options.scales.xAxes[0].ticks = {
        callback: function (value, index, values) {
          return " ";
        },
      };
      chartConfig.options.legend.display = false;
      chartConfig.options.scales.yAxes[0].scaleLabel.labelString = "(%)";
      return chartConfig;
    },
    getBaseChartConfig() {
      let chartConfig = {
        type: "bar",
        height: 161,
        data: {
          labels: [],
          datasets: [
            {
              label: this.resources["within_limits"],
              data: [],
              backgroundColor: "rgb(127, 184, 129)",
              hoverBackgroundColor: "#6FBC73",
            },
            {
              label: this.resources["l1"],
              data: [],
              backgroundColor: "rgb(241, 207, 96)",
              hoverBackgroundColor: "#FFCA37",
            },
            {
              label: this.resources["l2"],
              data: [],
              backgroundColor: "rgb(216, 122, 116)",
              hoverBackgroundColor: "#FC5B57",
            },
          ],
        },
        options: {
          hover: {
            mode: "nearest",
          },
          tooltips: {
            callbacks: {
              label: function (tooltipItems, data) {
                let labelPerfix=" Cycle Time Compliance: ";
                if( data.datasets[tooltipItems.datasetIndex] && data.datasets[tooltipItems.datasetIndex].label){
                  labelPerfix=data.datasets[tooltipItems.datasetIndex].label +":";
                }
                return " "+labelPerfix+" " + tooltipItems.yLabel + "% ";
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
                  labelString: "Cycle Time Compliance (%)",
                  fontColor: "black",
                },
                ticks: {
                  beginAtZero: true,
                  steps: 10,
                  stepValue: 5,
                  max: 100,
                },
              },
            ],
            xAxes: [
              {
                stacked: true,
                scaleLabel: {
                  display: true,
                  fontColor: "black",
                },
                ticks: {
                  beginAtZero: true,
                },
              },
            ],
          },
          legend: {
            display: true,
            position: "bottom",
            labels: {
              boxWidth: 45,
              padding: 15,
              fontSize: 13,
            },
          },
        },
      };
      return chartConfig;
      // return {
      //     type: "bar",
      //     height: 161,
      //     data: {
      //         labels: [],
      //         datasets: [
      //             {
      //                 data: [],
      //                 fill: true,
      //                 lineTension: 0, // draw straight line
      //                 label: vm.resources['trend'],
      //                 borderColor: "",
      //                 backgroundColor: "",
      //             }
      //         ],
      //     },
      //     options: {
      //         legend: {display: false, },
      //         tooltips: {
      //             callbacks: {
      //                 label: function (tooltipItems, data) {
      //                     return ' Cycle Time Compliance: ' + (tooltipItems.yLabel) + "% ";
      //                 },
      //                 title: function (tooltipItem, data) {
      //                     return "";
      //                 },
      //             },
      //         },
      //         scales: {
      //             ticks: {
      //                 display: false,
      //                 beginAtZero: false,
      //             },
      //             yAxes: [{
      //                 scaleLabel: {
      //                     display: true,
      //                     labelString: "Cycle Time Compliance (%)",
      //                     fontColor: "black"
      //                 },
      //                 ticks: {
      //                     beginAtZero: true,
      //                     steps: 10,
      //                     stepValue: 5,
      //                     max: 100
      //                 }
      //             }],
      //             xAxes: [{
      //             }]
      //         },
      //     },
      // };
    },
    formatNumber(rawNumber) {
      return new Intl.NumberFormat("en-EN", {
        maximumSignificantDigits: 3,
      }).format(rawNumber);
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
            },
            showInfor(){
              console.log(this.tooling, this.compareId)
            }
  },
};
</script>

<style scoped>
.font-productivity {
  font-family: font;
}
</style>
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
