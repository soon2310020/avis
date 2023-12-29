<template>
  <div
    id="op-chart-mold"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-part-chart"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="title-part-chart" v-text="resources['tooling_report']"></h4>

          <div>
            <div class="btn-group btn-group-toggle float-right ml-3" data-toggle="buttons">
              <label
                class="btn btn-outline-info"
                :class="{active: requestParam.chartDataType == 'QUANTITY'}"
                @click.prevent="chartDataType('QUANTITY')"
              >
                <input type="radio" name="options" value="DAY" autocomplete="off" />
                <span v-text="resources['quantity']"></span>
              </label>
              <label
                class="btn btn-outline-info"
                :class="{active: requestParam.chartDataType == 'CYCLE_TIME'}"
                @click.prevent="chartDataType('CYCLE_TIME')"
              >
                <input type="radio" name="options" value="WEEK" autocomplete="off" />
                <span v-text="resources['cycle_time']"></span>
              </label>
              <label
                class="btn btn-outline-info"
                :class="{active: requestParam.chartDataType == 'UPTIME'}"
                @click.prevent="chartDataType('UPTIME')"
              >
                <input type="radio" name="options" value="WEEK" autocomplete="off" />
                <span v-text="resources['uptime']"></span>
              </label>
            </div>
          </div>

          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="row">
            <div class="col-12 col-lg-7 pb-3 pb-lg-0">
              <h5 class="card-title mb-0">{{mold.equipmentCode}}</h5>
              <div class="small text-muted">{{mold.part.partCode}} {{categoryLocation}}</div>
            </div>
            <!-- /.col-->

            <div class="col-6 col-lg-2 pr-4">
              <select class="form-control" v-model="requestParam.year" @change="bindChartData()">
                <option v-for="result in displayYears()" :value="result">{{result}}</option>
              </select>
            </div>
            <div class="col-6 col-lg-3">
              <div class="btn-group btn-group-toggle float-right mr-3" data-toggle="buttons">
                <label
                  class="btn btn-outline-success"
                  :class="{active: requestParam.dateViewType == 'DAY'}"
                  @click.prevent="dateViewType('DAY')"
                >
                  <input id="option1" type="radio" name="options" value="DAY" autocomplete="off" />
                  <span v-text="resources['day']"></span>
                </label>
                <label
                  class="btn btn-outline-success"
                  :class="{active: requestParam.dateViewType == 'WEEK'}"
                  @click.prevent="dateViewType('WEEK')"
                >
                  <input id="option2" type="radio" name="options" value="WEEK" autocomplete="off" />
                  <span v-text="resources['week']"></span>
                </label>
                <label
                  class="btn btn-outline-success"
                  :class="{active: requestParam.dateViewType == 'MONTH'}"
                  @click.prevent="dateViewType('MONTH')"
                >
                  <input id="option3" type="radio" name="options" value="MONTH" autocomplete="off" />
                  <span v-text="resources['mm']"></span>
                </label>
              </div>
            </div>
            <!-- /.col-->
          </div>

          <div class="chart-wrapper" style="height:300px;margin-top:30px;margin-bottom: 10px">
            <canvas class="chart" id="chart-mold" height="300"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
 props: {
        resources: Object,
    },
  data() {
    return {
      mold: {
        equipmentCode: "",
        name: "",
        part: {}
      },
      requestParam: {
        year: moment().format("YYYY"),
        chartDataType: "QUANTITY",
        dateViewType: "WEEK",
        moldId: ""
        //'moldCode': ''
      }
    };
  },
  methods: {
    showChart: function(mold, dataType, viewType) {
      this.mold = mold;
      this.requestParam.moldId = mold.id;
      //this.requestParam.moldCode = mold.equipmentCode;
      this.requestParam.chartDataType = dataType;
      this.requestParam.dateViewType = viewType;

      this.bindChartData();

      $("#op-chart-mold").modal("show");
    },

    chartDataType: function(dataType) {
      this.requestParam.chartDataType = dataType;
      this.bindChartData();
    },

    dateViewType: function(viewType) {
      this.requestParam.dateViewType = viewType;
      this.bindChartData();
    },

    bindChartData: function() {
      var param = Common.param(this.requestParam);
      var self = this;
      axios.get("/api/chart/molds?" + param).then(function(response) {
        if ("QUANTITY" == self.requestParam.chartDataType) {
          self.bindQuantityChartData(response.data);
        } else if ("CYCLE_TIME" == self.requestParam.chartDataType) {
          self.bindCycleTimeChartData(response.data);
        } else if ("UPTIME" == self.requestParam.chartDataType) {
          self.bindUptimeChartData(response.data);
        }
      }) /*.catch(function (error) {
                    console.log(error.response);
                })*/;
    },

    bindQuantityChartData: function(data) {
      config.data.labels = [];
      config.data.datasets = [];
      window.chart.update();

      var newDataset = {
        label: "Shots",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      for (var i = 0; i < data.length; i++) {
        var title = data[i].title.replace(this.requestParam.year, "W-");

        if ("DAY" == this.requestParam.dateViewType) {
          var day = title.replace("W-", "");
          title = day.substring(0, 2) + "/" + day.substring(2, 4);
        } else if ("MONTH" == this.requestParam.dateViewType) {
          title = title.replace("W-", "");
        }

        config.data.labels.push(title);
        newDataset.data.push(data[i].data);
      }

      config.data.datasets.push(newDataset);

      window.chart.update();
    },

    bindCycleTimeChartData: function(data) {
      config.data.labels = [];
      config.data.datasets = [];
      window.chart.update();

      var cycleTime = {
        label: "Cycle Time",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        //backgroundColor: 'transparent',
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      var maxCycleTime = {
        label: "Max CT",
        backgroundColor: "transparent",
        borderColor: getStyle("--warning"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      var minCycleTime = {
        label: "Min CT",
        backgroundColor: "transparent",
        borderColor: getStyle("--danger"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      var plusL1 = {
        label: "+L1",
        backgroundColor: "transparent",
        borderColor: getStyle("--warning"),
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        //borderDash: [8, 5],
        data: []
      };

      var plusL2 = {
        label: "+L2",
        backgroundColor: "transparent",
        borderColor: getStyle("--danger"),
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        //borderDash: [8, 5],
        data: []
      };

      var minusL1 = {
        label: "-L1",
        backgroundColor: "transparent",
        borderColor: getStyle("--warning"),
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        //borderDash: [8, 5],
        data: []
      };

      var minusL2 = {
        label: "-L2",
        backgroundColor: "transparent",
        borderColor: getStyle("--danger"),
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        //borderDash: [8, 5],
        data: []
      };

      /*var contractedCycleTime = {
                    label: 'Contracted Cycle Time',
                    backgroundColor: 'transparent',
                    borderColor: getStyle('--success'),
                    pointHoverBackgroundColor: '#fff',
                    borderWidth: 1,
                    data: []
                }


                */

      for (var i = 0; i < data.length; i++) {
        var title = data[i].title.replace(this.requestParam.year, "W-");

        if ("DAY" == this.requestParam.dateViewType) {
          var day = title.replace("W-", "");
          title = day.substring(0, 2) + "/" + day.substring(2, 4);
        } else if ("MONTH" == this.requestParam.dateViewType) {
          title = title.replace("W-", "");
        }

        config.data.labels.push(title);
        cycleTime.data.push(data[i].cycleTime);
        maxCycleTime.data.push(data[i].maxCycleTime);
        minCycleTime.data.push(data[i].minCycleTime);

        plusL1.data.push(data[i].cycleTimePlusL1);
        plusL2.data.push(data[i].cycleTimePlusL2);
        minusL1.data.push(data[i].cycleTimeMinusL1);
        minusL2.data.push(data[i].cycleTimeMinusL2);

        //contractedCycleTime.data.push(data[i].contractedCycleTime);
      }

      config.data.datasets.push(cycleTime);
      config.data.datasets.push(maxCycleTime);
      config.data.datasets.push(minCycleTime);

      config.data.datasets.push(plusL1);
      config.data.datasets.push(minusL1);
      config.data.datasets.push(plusL2);
      config.data.datasets.push(minusL2);

      //config.data.datasets.push(contractedCycleTime);
      window.chart.update();
    },

    bindUptimeChartData: function(data) {
      config.data.labels = [];
      config.data.datasets = [];
      window.chart.update();

      var uptime = {
        label: "Uptime (minutes)",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      for (var i = 0; i < data.length; i++) {
        var title = data[i].title.replace(this.requestParam.year, "W-");

        if ("DAY" == this.requestParam.dateViewType) {
          var day = title.replace("W-", "");
          title = day.substring(0, 2) + "/" + day.substring(2, 4);
        } else if ("MONTH" == this.requestParam.dateViewType) {
          title = title.replace("W-", "");
        }

        config.data.labels.push(title);
        uptime.data.push(data[i].uptime);
      }

      config.data.datasets.push(uptime);

      window.chart.update();
    }
  },

  computed: {
    categoryLocation: function() {
      if (this.mold == null || this.mold.part.category == null) {
        return "";
      }

      try {
        var category = this.mold.part.category;
        var parentCategory = category.parent;

        var displayCategory = "";
        if (parentCategory != null && parentCategory.name != null) {
          displayCategory = parentCategory.name + " > ";
        }

        return "(" + displayCategory + category.name + ")";
      } catch (e) {
        return "";
      }
    }
  },
  mounted() {}
};

var config = {
  type: "line",
  data: {
    labels: [],
    datasets: []

    /*labels: ['M', 'T', 'W', 'T', 'F', 'S', 'S', 'M', 'T', 'W', 'T', 'F', 'S', 'S', 'M', 'T', 'W', 'T', 'F', 'S', 'S', 'M', 'T', 'W', 'T', 'F', 'S', 'S'],
            datasets: [{
                label: 'My First dataset',
                backgroundColor: hexToRgba(getStyle('--info'), 10),
                borderColor: getStyle('--info'),
                pointHoverBackgroundColor: '#fff',
                borderWidth: 2,
                data: [600, 180, 70, 69, 77, 57, 125, 165, 172, 91, 173, 138, 155, 89, 50, 161, 65, 163, 160, 103, 114, 185, 125, 196, 183, 64, 137, 95, 112, 175]
            }]*/
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

      /*callbacks: {
                    label: function (tooltipItem, data) {
                        var tooltipValue = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                        return parseInt(tooltipValue).toLocaleString();
                    }
                }*/
    },
    scales: {
      xAxes: [
        {
          gridLines: {
            drawOnChartArea: false
          },
          scaleLabel: {
            display: false,
            labelString: "Date"
          }
        }
      ],
      yAxes: [
        {
          ticks: {
            beginAtZero: false,
            callback: function(value, index, values) {
              // Convert the number to a string and splite the string every 3 charaters from the end
              value = value.toString();
              value = value.split(/(?=(?:...)*$)/);
              value = value.join(",");
              return value;
            }
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
$(function() {
  window.chart = new Chart($("#chart-mold"), config);
});
</script>