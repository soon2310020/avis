<template>
  <div
    id="op-reject-detail"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-reject-detail"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title"><span v-text="resources['tooling_id']"></span>: {{ rejectPart.mold && rejectPart.mold.equipmentCode }}</h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div style="overflow:auto;" class="modal-body">
          <div class="row">
            <div class="col-md-6">
              <div class="table-header">
                <div class="table-time">{{ selectedTimeLabel }}</div>
              </div>
              <div class="table-content">
                <table
                  class="table table-responsive-sm table-bordered">
                  <thead>
                  <tr>
                    <th></th>
                    <template v-if="tableHeaders.length > 0">
                      <th style="width: 80px; min-width:80px" v-for="(header,index) in tableHeaders" :key="index">{{ header }}</th>
                    </template>
                    <template v-else>
                      <th></th>
                    </template>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(field, fieldIndex) in Object.keys(tableData)" :key="fieldIndex">
                    <td class="row-title">{{ tableFields[field] }}</td>
                    <template v-if="tableHeaders.length > 0">
                      <td v-for="(item, index) in tableData[field]" :key="index">{{ item === null ? '-' : item }}</td>
                    </template>
                    <template v-if="tableHeaders.length === 0 && fieldIndex === 0">
                      <td class="detail-no-data" :rowspan="Object.keys(tableData).length">No data</td>
                    </template>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div class="col-md-6">
              <div class="chart-header">
                <div class="chart-header-tab">
                  <div class="btn-group btn-group-toggle"
                       data-toggle="buttons">
                    <label class="btn btn-outline-info"
                           :class="{active: chartType === chartTypeOption.YIELD_RATE}"
                           @click.prevent="changeChartType(chartTypeOption.YIELD_RATE)">
                      <input type="radio" name="options" :value="chartTypeOption.YIELD_RATE"
                             autocomplete="off"/>
                      <span v-text="resources['yield_trend']"></span>
                    </label>
                    <label class="btn btn-outline-info"
                           :class="{active: chartType === chartTypeOption.BREAKDOWN}"
                           @click.prevent="changeChartType(chartTypeOption.BREAKDOWN)">
                      <input type="radio" name="options" :value="chartTypeOption.BREAKDOWN"
                             autocomplete="off"/>
                      <span v-text="resources['reject_rate_breakdown']"></span>
                    </label>
                  </div>
                </div>
                <div class="chart-header-time">
                  <date-detail-filter @time-filter="changeDateFilter"
                                      @show-change-time-action="showChangeTimeAction"
                                      @change-selected-time-label="changeSelectedTimeLabel"
                                      :time-options="timeOptions"
                                      :time-change-type="timeChangeType"
                                      @check-current-time="hideIcon"></date-detail-filter>
                </div>
              </div>
              <div class="chart-content">
                <div class="icon-wrapper">
                  <a-icon
                    v-show="isShowChangeTimeAction"
                    style="font-size: 23px;"
                    class="icon"
                    type="caret-left"
                    @click.prevent="changeTime(timeChangeType.PREV)"
                  ></a-icon>
                </div>
                <div class="reject-chart">
                  <canvas id="reject-rate-chart"></canvas>
                  <div id="reject-rate-chart-legend" v-if="chartType === chartTypeOption.BREAKDOWN">
                    <div class="line-legend-wrapper">
                      <div class="legend-icon">
                        <div class="icon-line"></div>
                        <div class="icon-line"></div>
                        <div class="icon-line"></div>
                        <div class="icon-line"></div>
                      </div>
                      <div class="legend-title">
                        <span v-text="resources['cum_percentage_curve']"></span>
                      </div>
                    </div>
                    <div class="bar-legend-wrapper">
                      <div class="legend-item" v-for="(legend, index) in breakdownBarLegends" :key="index">
                        <div class="legend-icon" :style="{backgroundColor: legend.color}"></div>
                        <div class="legend-title">{{ legend.name }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="icon-wrapper">
                  <a-icon
                    v-show="isShowChangeTimeAction && isShowIcon"
                    @click.prevent="changeTime(timeChangeType.NEXT)"
                    style="font-size: 23px;"
                    class="icon"
                    type="caret-right"
                  ></a-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <div class="modal-footer-content">
            <button class="btn btn-default" @click.prevent="closeRejectDetailModal" v-text="resources['close']"></button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  module.exports = {
    components: {
      'date-detail-filter': httpVueLoader('/components/reject-rates/date-detail-filter.vue')
    },
    props: {
        resources: Object,
    },
    data() {
      return {
        rejectPart: {},
        requestParam: {
          moldId: null,
          partId: null,
          startDate: null,
          endDate: null
        },
        chartType: null,
        chartTypeOption: {
          YIELD_RATE: 'YIELD_RATE',
          BREAKDOWN: 'BREAKDOWN'
        },
        timeOptions: {
          DATE: 'DATE',
          WEEK: 'WEEK',
          MONTH: 'MONTH',
        },
        timeChangeType: {
          NEXT: 'NEXT',
          PREV: 'PREV'
        },
        initTimePeriod: null,
        initPeriodType: null,
        selectedTimeLabel: null,
        chart: null,
        chartConfig: null,
        dateFormat: 'YYYYMMDD',
        breakdownColors: ['#f1cc68', '#8fe6d5', '#a2daf1', '#e67c7a',
          '#b3225d', '#8bb487', '#5a4645', '#808080',
          '#f85a51', '#7383c1', '#795cc8', '#efeeee'],
        tableHeaders: [],
        tableData: [],
        tableFields: {
          totalProducedAmount: this.resources['produced_part'],
          totalRejectedAmount: this.resources['rejected_part'],
          yieldRate: this.resources['yield_rate'],
          rejectedRate: this.resources['rejected_rate'],
          editedBy: this.resources['edited_by']
        },
        breakdownBarLegends: [],
        isShowChangeTimeAction: true,
        isShowIcon: true
      };
    },
    methods: {
      changeDateFilter(dateParam) {
        if (dateParam.startDate && dateParam.endDate) {
          this.requestParam.startDate = dateParam.startDate;
          this.requestParam.endDate = dateParam.endDate;
          this.bindDetailData();
        }
      },
      changeTime(changeType) {
        let child = Common.vue.getChild(this.$children, 'date-detail-filter');
        if (child != null) {
          child.changeTime(changeType);
        }
      },
      changeChartType(chartType) {
        this.chartType = chartType;
        this.bindDetailData();
      },
      showRejectDetail(data, startDate) {
        $('#op-reject-detail').modal('show');
        if (this.rejectPart.id === data.id) {
          // keep previous data
          return;
        }
        this.rejectPart = data;
        this.tableData = [];
        this.requestParam.moldId = data.moldId;
        this.requestParam.partId = data.partId;
        this.chartType = this.chartTypeOption.YIELD_RATE;
        this.setDefaultStartDateAndEndDate(startDate);
        // this.bindDetailData();
      },
      bindDetailData() {
        if (!this.requestParam.moldId || !this.requestParam.partId || !this.requestParam.startDate || !this.requestParam.endDate) {
          return;
        }
        let param = Common.param(this.requestParam);
        axios.get("/api/rejected-part?" + param).then(response => {
          this.bindTableData(response.data.content);
          if (this.chartType === this.chartTypeOption.YIELD_RATE) {
            this.bindYieldRateChart(response.data.content);
          }
        }).catch(error => {
          console.log(error.response);
        });
        if (this.chartType === this.chartTypeOption.BREAKDOWN) {
          axios.get("/api/rejected-part/breakdown-chart?" + param).then((response) => {
            this.bindBreakdownChart(response.data);
          }).catch((error) => {
            console.log(error.response);
          });
        }
      },
      setDefaultStartDateAndEndDate(startDate) {
        let previousWeek = moment(startDate, this.dateFormat).week() - 1;
        if (previousWeek === moment().week()) {
          previousWeek = previousWeek - 1;
        }
        let year = moment(startDate, this.dateFormat).year();
        if (previousWeek < 1) {
          year = year - 1;
          previousWeek = moment(year + '-31-12', 'YYYY-MM-DD').week() - 1;
        }
        this.requestParam.startDate = moment().week(previousWeek).year(year).weekday(1).format(this.dateFormat);
        this.requestParam.endDate = moment().week(previousWeek).year(year).weekday(7).format(this.dateFormat);
        let child = Common.vue.getChild(this.$children, 'date-detail-filter');
        if (child != null) {
          child.initOptionValue(previousWeek, year);
        }
      },
      showChangeTimeAction(status){
        this.isShowChangeTimeAction = status;
      },
          hideIcon(pload){
        this.isShowIcon = pload;
      },
      changeSelectedTimeLabel(label) {
        this.selectedTimeLabel = label;
      },
      // format YYYYMMDD
      enumerateDaysBetweenDates (startDate, endDate) {
        var currDate = moment(startDate).startOf('day');
        var lastDate = moment(endDate).startOf('day');

        let response = [startDate];
        while(currDate.add(1, 'days').diff(lastDate) < 0) {
          response.push(currDate.format(this.dateFormat));
        }
        response.push(endDate);

        return response;
      },
      bindTableData(tableData) {
        tableData.sort((first, second) => {
          return first.day > second.day ? 1: -1;
        });
        this.tableHeaders = [];
        this.tableData = {};
        let startDate = this.requestParam.startDate;
        let endDate = this.requestParam.endDate;
        let days = this.enumerateDaysBetweenDates(startDate, endDate);
        days.forEach(item => {
          this.tableHeaders.push(moment(item, 'YYYYMMDD').format('MM/DD'))
        });


        Object.keys(this.tableFields).forEach(field => {
          this.tableData[field] = [];
          this.tableHeaders.forEach(() => {
            this.tableData[field].push('-');
          })
        });
        tableData.forEach(item => {
          let date = moment(item.day, this.dateFormat).format("MM/DD");
          let dataIndex = null;

          this.tableHeaders.forEach((header, index) => {
            if (header === date) {
              dataIndex = index;
            }
          });
          Object.keys(this.tableFields).forEach(field => {
            let value = item[field];
            if (field.endsWith("Rate")) {
              if(value !== null){
                value = value + '%';
              }
            }
            if (dataIndex !== null) {
              this.tableData[field][dataIndex] = value;
            }
          });
        });
        this.$forceUpdate();
      },
      setForTest() {
        ['Off set', 'Short Mould', 'Dented', 'Flow Mark'].forEach((item, index) => {
          this.breakdownBarLegends.push({
            name: item,
            color: this.breakdownColors[index]
          });
        });
      },
      bindYieldRateChart(chartData) {
        this.resetConfigForYieldRateChart();
        this.chartConfig.data.labels = [];
        this.chartConfig.data.datasets = [];
        this.chart.update();
        let dataset = {
          type: "line",
          label: this.resources['yield_rate'],
          backgroundColor: hexToRgba(getStyle("--info"), 10),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: []
        };

        // sort
        chartData.sort((first, second) => {
          return first.day > second.day ? 1: -1;
        });
        let days = this.enumerateDaysBetweenDates(this.requestParam.startDate, this.requestParam.endDate);
        days.forEach(day => {
          let date = moment(day, this.dateFormat).format("MM/DD");
          this.chartConfig.data.labels.push(date);
          dataset.data.push(null);
        });
        chartData.forEach(item => {
          let date = moment(item.day, this.dateFormat).format("MM/DD");
          let dataIndex = null;
          this.chartConfig.data.labels.forEach((label, index) => {
            if (label === date) {
              dataIndex = index;
            }});
          if (dataIndex !== null) {
            dataset.data[dataIndex] = item.yieldRate < 100 ? item.yieldRate : 100;
          }
        });

        // dataset.data.push(dataset);
        this.chartConfig.data.datasets.push(dataset);
        this.chart.update();
      },
      bindBreakdownChart(chartData) {
        chartData = chartData.filter(item => item.rejectedAmount);
        chartData.sort((first, second) => {
          return first.rejectedRate < second.rejectedRate ? 1: -1;
        });
        this.resetConfigForBreakdownChart();
        this.chartConfig.data.labels = [];
        this.chartConfig.data.datasets = [];
        this.chart.update();

        this.breakdownBarLegends = [];
        let tmpRejectedRateRate = 0;
        // line chart
        let lineDataset = {
          type: "line",
          label: this.resources['cum_percentage_curve'],
          yAxisID: 'right',
          backgroundColor: 'transparent',
          borderColor: 'black',
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: []
        };

        let dataset = {
          type: "bar",
          label: '',
          yAxisID: 'left',
          backgroundColor: [],
          borderColor: [],
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: []
        };
        chartData.forEach((item, index) => {
          let color = this.breakdownColors[index];
          if (!color) {
            color = Common.getRandomElement(this.breakdownColors);
          }

          // bar chart

          tmpRejectedRateRate += item.rejectedRate;
          if (tmpRejectedRateRate > 100) {
            tmpRejectedRateRate = 100;
          }

          lineDataset.data.push(tmpRejectedRateRate);

          dataset.backgroundColor.push(color);
          dataset.borderColor.push(color);
          dataset.data.push(item.rejectedAmount);
          this.chartConfig.data.labels.push(item.reason);
          this.breakdownBarLegends.push({
            name: item.reason,
            color: color
          });
        });

        this.chartConfig.data.datasets.push(lineDataset);
        this.chartConfig.data.datasets.push(dataset);
        this.chart.update();
      },
      setupRejectChart() {
        this.chartConfig = this.getChartConfig();
        this.chart = new Chart($("#reject-rate-chart"), this.chartConfig);
      },
      getChartConfig() {
        return {
          type: "bar",
          data: {
            labels: [],
            datasets: []
          },
          options: {
            maintainAspectRatio: false,
            legend: {
              display: true,
              position: "bottom"
            },
            tooltips: {
              mode: "index",
              intersect: false
            },
            scales: {
              xAxes: [
                {
                  scaleLabel: {
                    display: true,
                  }
                }
              ],
              yAxes: [
                {
                  stacked: false,
                  position: 'left',
                  id: 'left',
                  ticks: {
                    beginAtZero: true,
                    min: 0
                  },
                  scaleLabel: {
                    display: true
                  }
                }
              ]
            },
            elements: {
              point: {
                radius: 0,
                hitRadius: 10,
                hoverRadius: 4,
                hoverBorderWidth: 3
              }
            }
          }
        };
      },
      resetConfigForYieldRateChart() {
        delete this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString;
        this.chartConfig.options.scales.xAxes = [
          {
            scaleLabel: {
              display: true,
            }
          }
        ];
        this.chartConfig.options.scales.yAxes = [
          {
            stacked: false,
            position: 'left',
            id: 'left',
            ticks: {
              beginAtZero: true,
              callback: function(value) {
                if (value > 100)
                  return null;
                return value + '%';
              },
              min: 0,
              max: 105
            },
            scaleLabel: {
              display: true
            }
          }];
        this.chartConfig.options.legend.display = true;
        this.chartConfig.options.tooltips = {
          mode: "index",
          intersect: false,
          callbacks: {
            afterLabel: function() {
              return '';
            },
            label: function(tooltipItem, data) {
              const label = data.datasets[tooltipItem.datasetIndex].label;
              const value = Number(tooltipItem.yLabel).toFixed(2).replace('.00', '').toString();
              return `${label}: ${value}%`;
            }
          }
        };
      },
      resetConfigForBreakdownChart() {
        this.chartConfig.options.scales.xAxes = [
          {
            scaleLabel: {
              display: false,
            },
            ticks: {
              display: false
            }
          }
        ];
        this.chartConfig.options.scales.yAxes[0].scaleLabel.labelString = 'Frequency';
        this.chartConfig.options.scales.yAxes = [
          {
            stacked: false,
            position: 'left',
            id: 'left',
            ticks: {
              beginAtZero: true,
              min: 0
            },
            scaleLabel: {
              display: true,
              labelString: this.resources['frequency']
            }
          }
          ,{
          stacked: false,
          id: 'right',
          position: 'right',
          ticks: {
            beginAtZero: true,
            callback: function(value) {
              if (value > 100)
                return null;
              return value + '%';
            },
            min: 0,
            max: 105
          },
          gridLines: {
            display: false
          },
          scaleLabel: {
            display: true,
            labelString: this.resources['cum_percentage']
          }
        }];
        this.chartConfig.options.legend.display = false;
        this.chartConfig.options.tooltips = {
          enabled: true,
          intersect: true,
          callbacks: {
            afterLabel: function() {
              return '';
            },
            title: function() {},
            label: function(tooltipItem, data) {
              let label = data.datasets[tooltipItem.datasetIndex].label;
              const value = Number(tooltipItem.yLabel).toFixed(2).replace('.00', '').toString();
              if (tooltipItem.datasetIndex === 1) {
                label = tooltipItem.xLabel;
                return `${label}: ${value}`;
              }
              return `${label}: ${value}%`;
            }
          }
        };
      },
      closeRejectDetailModal() {
        $('#op-reject-detail').modal('hide');
        let child = Common.vue.getChild(this.$children, 'date-detail-filter');
                if (child != null) {
                  child.initOptionValue();
                }
      }
    },

    computed: {},
    mounted() {
      this.chartType = this.chartTypeOption.YIELD_RATE;
      this.setupRejectChart();
    },

    watch: {}
  };

</script>
