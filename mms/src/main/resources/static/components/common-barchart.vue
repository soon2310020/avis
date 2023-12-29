<template>
  <div>
    <canvas width="350" id="common-bar-chart"></canvas>
    <div class="legend-zone">
      <b>Product:</b>
      <span>{{ data.name }}</span>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    data: Array
  },
  data() {
    return {};
  },
  methods: {
    bindMaximumCapacityChart: function() {
      console.log(this.data)
      if (this.data) {
        const totalProduced = [this.data.totalProduced];
        const predictedQuantity = [this.data.predictedQuantity];
        const totalProductionDemand = [this.data.totalProductionDemand];
        const totalMaxCapacity = [this.data.totalMaxCapacity];
        this.chart.data.labels = ['Produced', 'Predicted', 'Demand', 'Capacity'];

        this.chart.data.datasets[0].data = [totalProduced, predictedQuantity, totalProductionDemand, totalMaxCapacity];
        this.chart.resize();
        this.chart.update();
      } else {
        this.initChart()
      }
    },
    initChart() {
      this.chart.data.labels = ['Produced', 'Predicted', 'Demand', 'Capacity'];
      this.chart.data.datasets[0].data = [0, 0, 0, 0];
      this.chart.resize();
      this.chart.update();
    }
  },
  mounted() {
    let chartConfig = {
      type: 'bar',
      data: {
        labels: ['Produced', 'Predicted', 'Demand', 'Capacity'],
        datasets: [
          {
            data: [],
            backgroundColor: ['#7B3AAD', '#C0ADF2', '#C25FD3', '#EB709A' ],
            hoverBackgroundColor: ['#592A7E', '#9D7FEE', '#9521AA', '#EB709A'],
            borderRadius: 5
          }
        ]
      },
      options: {
        scales: {
          yAxes: [{
            scaleLabel: {
              display: true,
              labelString: 'Quantity'
            },
            ticks: {
              beginAtZero: true,
              min: 0
            }
          }],
          xAxes: [{
            ticks: {
              beginAtZero: true,
              min: 0
            }
          }]
        },
        responsive: false,
        tooltips: {
          displayColors: false,
          cornerRadius: 2
        },
        legend: {
          display: false
        },
        maintainAspectRatio: false
      }
    }
    this.chart = new Chart($("#common-bar-chart"), chartConfig);
    this.bindMaximumCapacityChart()
  }
};
</script>
<style scoped>
.legend-zone {
  text-align: center;
  color: #000;
}
</style>

