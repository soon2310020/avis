<template>
  <div
    id="op-chart-part"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-part-chart"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="title-part-chart" v-text="resources['part_qlty_report']"></h4>

          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="row">
            <div class="col-lg-6 pb-3 pb-lg-0">
              <h5 class="card-title mb-0">{{part.partCode}}</h5>
              <div class="small text-muted">{{part.categoryName}} > {{part.projectName}}</div>
            </div>
            <!-- /.col-->

            <div class="col-3 col-lg-2">
              <select class="form-control" v-model="requestParam.year" @change="bindChartData()">
                <option v-for="result in displayYears()" :value="result">{{result}}</option>
              </select>
            </div>
            <div
                    class="col-3 col-lg-1"
                    v-if="requestParam.dateViewType == 'DAY'"
                    style="padding: 0px;"
            >
              <div v-if="month < 10" class="month-class">{{`0${month}`}}</div>
              <div v-else class="month-class">{{month}}</div>
            </div>

            <div class="col-6 offset-1 col-lg-3 ml-auto">
              <div class="btn-group btn-group-toggle float-right" data-toggle="buttons">
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

          <div class="next-chart-container">
            <a-icon
                    v-on:click="changeMonth('PRE')"
                    v-if="requestParam.dateViewType == 'DAY' && !disablePre()"
                    style="fontSize: 23px;"
                    class="icon"
                    type="caret-left"
            ></a-icon>
            <div v-else style="width: 23px; height: 23px;">{{' '}}</div>
            <div style="width: 96%;" class="op-chart-wrapper">
              <canvas class="chart" id="chart-part"></canvas>
            </div>
            <a-icon
                    v-on:click="changeMonth('NEXT')"
                    v-if="requestParam.dateViewType == 'DAY' && !disableNext()"
                    style="fontSize: 23px;"
                    class="icon"
                    type="caret-right"
            ></a-icon>
            <div v-else style="width: 23px; height: 23px;">{{' '}}</div>
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
      part: {
        partCode: "",
        name: "",
        category: {}
      },
      mold: {
              equipmentCode: "",
              name: "",
            },

      requestParam: {
        year: moment().format("YYYY"),
        chartDataType: "QUANTITY",
        dateViewType: "WEEK",
        partId: "",
        moldId: ""
      },
      month: 1,
      yearSelected: "2020",
      rangeYear: [],
      rangeMonth: [],
      responseDataChart: []
    };
  },
  methods: {
    changeMonth: function(type) {
      if (type === "PRE") {
        if (this.month > 1) {
          this.month = this.month - 1;
        } else {
          this.month = 12;
          const listYearDropdown = this.displayYears();
          this.yearSelected = (Number(this.yearSelected) - 1).toString();
          const findIndex = listYearDropdown.findIndex(
                  value => value == this.yearSelected
          );
          if (findIndex !== -1) {
            this.requestParam.year = this.yearSelected;
            return this.bindChartData();
          } else {
            this.requestParam.year = "";
            return this.bindChartData();
          }
        }
        console.log('changeMonth', `${this.requestParam.year}${this.month}`)
      } else {
        if (this.month < 12) {
          if (
                  this.month === Number(moment().format("MM")) &&
                  Number(this.yearSelected) == Number(moment().format("YYYY"))
          ) {
            return;
          }
          this.month = this.month + 1;
        } else {
          this.month = 1;
          const listYearDropdown = this.displayYears();
          this.yearSelected = (Number(this.yearSelected) + 1).toString();
          const findIndex = listYearDropdown.findIndex(
                  value => value == this.yearSelected
          );
          if (findIndex !== -1) {
            this.requestParam.year = this.yearSelected;
            return this.bindChartData();
          } else {
            this.requestParam.year = "";
            return this.bindChartData();
          }
        }
      }
      console.log("this.responseDataChart: ", this.responseDataChart);
      if ("QUANTITY" == this.requestParam.chartDataType) {
        this.bindQuantityChartData(this.responseDataChart);
      }
    },
    displayYears: function() {
      let years = [];
      if (this.rangeYear.length > 0) {
        for (let i = this.rangeYear[0]; i <= this.rangeYear[1]; i++) {
          years.push(i);
        }
      }
      return years;
    },
    disableNext: function() {
      const { rangeYear, rangeMonth } = this;
      if (rangeYear.length > 0 && rangeMonth.length > 0) {
        return (
                this.month >= Number(rangeMonth[1]) &&
                Number(this.yearSelected) == Number(rangeYear[1]) || Number(this.yearSelected) > Number(rangeYear[1])
        );
      }
      return false;
    },

    disablePre: function() {
      const { rangeYear, rangeMonth } = this;
      if (rangeYear.length > 0 && rangeMonth.length > 0) {
        return (
                this.month <= Number(rangeMonth[0]) &&
                Number(this.yearSelected) == Number(rangeYear[0]) || Number(this.yearSelected) < Number(rangeYear[0])
        );
      }
      return false;
    },
    showChart: function(part, mold,  dataType, viewType) {
      this.part = part;
      this.mold = mold;
      //this.requestParam.partCode = part.partCode;
      this.requestParam.partId = part.id;
      this.requestParam.moldId = mold.id;

      this.requestParam.chartDataType = dataType;
      this.requestParam.dateViewType = viewType;

      this.setTimeRange(part.id, mold.id).then(() => {
        this.requestParam.year = this.rangeYear[1];
        console.log('bind Chart Data');
        this.bindChartData();
      });
      this.findMoldParts();
      $("#op-chart-part").modal("show");
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
      axios
        .get("/api/chart/part?" + param)
        .then(function(response) {
          self.responseDataChart = response.data;
          if ("QUANTITY" == self.requestParam.chartDataType) {
            self.bindQuantityChartData(response.data);
          } else if ("UPTIME" == self.requestParam.chartDataType) {
            self.bindUptimeChartData(response.data);
          }
        })
        .catch(function(error) {
          console.log(error.response);
        });
    },

    bindQuantityChartData: function(dataShow) {
      console.log(dataShow);

      config.data.labels = [];
      config.data.datasets = [];
      window.partChart.update();

      var newDataset = {
        label: "Total Part Quantity",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      var newDataset2 = {
        label: "Tool Count",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--danger"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        data: []
      };

      const data = dataShow.filter(value => {
        if ("DAY" == this.requestParam.dateViewType) {
          const title = value.title.replace(this.requestParam.year, "W-");
          const day = title.replace("W-", "");
          if (Number(day.substring(0, 2)) == this.month) {
            return true;
          }
          return false;
        } else {
          return true;
        }
      });

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
        newDataset2.data.push(data[i].moldCount);
      }

      config.options.tooltips = {
        enabled: false,
        custom: function(tooltipModel) {
          // Tooltip Element
          var tooltipEl = document.getElementById("chartjs-tooltip");
          // Create element on first render
          let xLabel = "";
          let yLabel = 0;
          let itemIndex = 0;
          const { dataPoints } = tooltipModel;
          if (dataPoints && dataPoints.length > 0) {
            xLabel = dataPoints[0].xLabel;
            yLabel = dataPoints[0].yLabel;
            itemIndex = dataPoints[0].index;
          }
          if (!tooltipEl) {
            tooltipEl = document.createElement("div");
            tooltipEl.id = "chartjs-tooltip";
            tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div style="margin: 5px 0px;">${xLabel}</div>
                                                    <div class="tooltips-title">
                                                    <div class="box-title"></div>
                                                    Total Part Quantity: ${yLabel} </div>
                                                    <table class="table">
                                                    </table>
                                                    </div>`;
            document.body.appendChild(tooltipEl);
          }

          if (tooltipModel.opacity === 0) {
            tooltipEl.style.opacity = 0;
            return;
          }

          tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div style="margin: 5px 0px;">${xLabel}</div>
                                                    <div class="tooltips-title">
                                                    <div class="box-title"></div>
                                                    Total Part Quantity: ${yLabel
                                                      .toString()
                                                      .replace(
                                                        /\B(?=(\d{3})+(?!\d))/g,
                                                        ","
                                                      )} </div>
                                                    <table class="table">
                                                    </table>
                                                    </div>`;

          if (tooltipModel.body) {
            var innerHtml = "<thead>";
            innerHtml += "<tr>";
            ["Tool ID", "Shots", "Avg.Cavities", "Quantity"].forEach(function(
              value
            ) {
              innerHtml += "<th>" + value + "</th>";
            });
            innerHtml += "</tr></thead>";
            if (data.length > itemIndex && itemIndex >= 0) {
              data[itemIndex].moldShots &&
                data[itemIndex].moldShots.map(value => {
                  innerHtml +=
                    '<tr><td style="text-align: center;">' +
                    value.moldCode +
                    "</td>";
                  innerHtml +=
                    '<td style="text-align: center;">' +
                    value.shotCount
                      .toString()
                      .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                    "</td>";
                  innerHtml +=
                    '<td style="text-align: center;">' +
                    Number(value.avgCavity)
                      .toFixed(2)
                      .toString()
                      .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                    "</td>";
                  innerHtml +=
                    '<td style="text-align: center;">' +
                    value.quantity
                      .toString()
                      .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                    "</td></tr>";
                });
            }
            var tableRoot = tooltipEl.querySelector("table");
            tableRoot.innerHTML = innerHtml;
          }

          var position = window.partChart.canvas.getBoundingClientRect();
          console.log("position: ", position);
          console.log("window.pageXOffset: ", window.pageXOffset);
          console.log("tooltipModel.caretX: ", tooltipModel.caretX);
          tooltipEl.style.opacity = 1;
          tooltipEl.style.position = "absolute";
          tooltipEl.style[`z-index`] = 9999;
          tooltipEl.style.left =
            position.left + window.pageXOffset + tooltipModel.caretX + "px";
          tooltipEl.style.top =
            position.top + window.pageYOffset + tooltipModel.caretY + "px";
          tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
          tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
          tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
          tooltipEl.style.padding =
            tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
          tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
          tooltipEl.style.pointerEvents = "none";
        }
      };

      config.data.datasets.push(newDataset);
      //config.data.datasets.push(newDataset2);
      window.partChart.update();
    },


     findMoldParts: function() {
          // Parts 조회
          var self = this;
          axios
            .get("/api/molds/" + this.mold.id + "/parts")
            .then(function(response) {
              self.parts = response.data.content;

              //$('#op-mold-details').modal('show');
            });
        },

    bindUptimeChartData: function(data) {
      config.data.labels = [];
      config.data.datasets = [];
      window.partChart.update();

      var uptime = {
        label: "Uptime (minutes)",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: []
      };

      var moldCount = {
        label: "Mold Count",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--danger"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
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
        moldCount.data.push(data[i].moldCount);
      }

      config.data.datasets.push(uptime);
      config.data.datasets.push(moldCount);

      window.partChart.update();
    },
    setTimeRange: function (partId, moldId) {
      this.rangeYear = [];
      this.rangeMonth = [];
      this.yearSelected = "2020";
      return axios.get(`/api/statistics/time-range?moldId=${moldId}`)
              .then(response => {
                console.log("range-date: ", response.data.moldId);
                const { data } = response;
                this.rangeYear = data.map(value => {
                  return value.year;
                });
                this.yearSelected = this.rangeYear[1];
                this.requestParam.year = this.rangeYear[1];
                this.rangeMonth = data.map(value => {
                  return value.month;
                });
                this.month = this.rangeMonth[1];
      });

    }

  },


  computed: {
    categoryLocation: function() {
      if (this.part == null || this.part.category == null) {
        return "";
      }

      try {
        var category = this.part.category;
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
  }
};

var config = {
  type: "line",
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
          stacked: false,
          ticks: {
            beginAtZero: true,
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
  window.partChart = new Chart($("#chart-part"), config);
});
</script>
<style scoped>
  .next-chart-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .month-class {
    height: calc(2.0625rem + 2px);
    width: 100%;
    border: 1px solid #e4e7ea;
    border-radius: 0.25rem;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0px !important;
  }

  .icon {
    cursor: pointer;
    font-size: 23px;
  }
  .icon:hover {
    color: #3ec1d4;
  }
  .icon:active {
    color: #3ec1d4;
  }
</style>
