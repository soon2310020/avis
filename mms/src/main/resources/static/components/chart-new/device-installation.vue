<template>
  <div id="DEVICE_INSTALLATION" :class="className" class="card card-customize">
    <div class="top-container title-container">
      <div class="line-title"></div>
      <div class="title-top" v-text="resources['device_installation']"></div>
      <span
        class="close-button"
        v-on:click="dimiss('DEVICE_INSTALLATION', 'Device Installation')"
        style="font-size: 25px;"
        aria-hidden="true"
      >&times;</span>
    </div>
    <div class="card-body body device-wapper">
      <div class="text" v-text="resources['terminal']"></div>
      <div class="chart-child">
        <canvas id="terminal"></canvas>
      </div>
      <div style="margin-top: 0px;" class="text" v-text="resources['counter']"></div>
      <div class="chart-child">
        <canvas id="counter"></canvas>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    data: Array,
    dimiss: Function,
    className: String
  },
  data() {
    return {
      resources: this.data.resources,
      dataConvert: {
        data: this.data,
        colors: ["#04cbd6", "#f3ab17", "#ccc", "#fd3da5"]
      },
      terminal: null,
      counter: null
    };
  },
  methods: {
    goToPage: function() {
      location.href = Common.URL.MOLD;
    },

    countTotal: function(data) {
      return 25;
      let sum = 0;
      data.data.forEach(value => {
        sum += value.data;
      });

      return sum;
    },

    bindChartPreventive: function(result) {},

    createChart: function(id, color, count) {
      let labels = [];
      if (id == "terminal") {
        labels = ["INACTIVE", "ACTIVE"];
      } else {
        labels = ["AVAILABLE", "INSTALLED"];
      }
      this[id] = new Chart($(`#${id}`), {
        type: "doughnut",
        data: {
          labels: labels,
          datasets: [
            {
              data: [count, 100],
              backgroundColor: [color, "#dddddd"]
            }
          ]
        },
        options: {
          legend: {
            display: false,
            position: "right"
          },
          elements: {
            arc: {
              borderWidth: 0
            },
            center: {
              text: "0",
              fontStyle: "Helvetica", //Default Arial
              sidePadding: 15 //Default 20 (as a percentage)
            }
          },
          tooltips: {
            callbacks: {
              label: function(tooltipItems, data) {
                return data.labels[tooltipItems.index];
              }
            }
          },
          responsive: true,
          maintainAspectRatio: false,
          // circumference: (4 / 3) * Math.PI,
          // rotation: (2.5 / 3) * Math.PI,
          cutoutPercentage: 50,
          segmentShowStroke: false,
          plugins: {
            datalabels: {
              display: false
            },
            labels: {
              render: "label",
              arc: true,
              position: "border"
            }
          }
        }
      });
    }
  },

  computed: {},
  watch: {
    data: function(newVal) {
      this.dataConvert = {
        data: [
          { title: "WORKING", data: 13 },
          { title: "IDLE", data: 5 },
          { title: "NOT_WORKING", data: 3 },
          { title: "DISCONNECTED", data: 4 }
        ],
        colors: ["#04cbd6", "#f3ab17", "#ccc", "#fd3da5"]
      };
      this.bindChartPreventive(this.dataConvert.data);
    }
  },
  mounted() {
    this.createChart("terminal", "#6fb97d", 70);
    this.createChart("counter", "#f7cc53", 60);
    this.bindChartPreventive(this.data);
    $("#chart-tooling").on("click", function(e) {
      //var activePoints = chartOverview.getSegmentsAtEvent(e);
      var activePoints = this.chart.getElementsAtEventForMode(
        e,
        "point",
        this.chart.options
      );
      var firstPoint = activePoints[0];

      if (firstPoint != undefined) {
        var label = this.chart.data.labels[firstPoint._index];
        var value = this.chart.data.datasets[firstPoint._datasetIndex].data[
          firstPoint._index
        ];
        location.href = Common.URL.MOLD+"#" + label.toLowerCase();
      } else {
        location.href = Common.URL.MOLD;
      }
    });
  }
};
</script>

<style scoped>
.text {
  color: #000;
  font-weight: 600;
  font-size: 12px;
  margin-top: 20px;
  padding-left: 10px;
  margin-bottom: 0px;
  width: 100%;
}

.device-wapper {
  flex-direction: column;
  max-height: 450px;
  align-items: center;
  justify-content: center;
  padding: 0px 1.25rem;
}

.body {
  display: flex;
  justify-content: space-between;
  padding: 0rem 0px;
  margin-left: 20px;
  /* //align-items: center; */
}

.chart-child {
  display: flex;
  width: 100%;
  align-items: center;
  margin-bottom: 20px;
}
</style>