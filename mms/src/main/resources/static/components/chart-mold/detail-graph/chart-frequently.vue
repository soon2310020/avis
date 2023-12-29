<template>
  <div style="width: 96%" class="op-chart-wrapper">
    <div class="op-chart-content">
      <canvas class="chart" id="chart-frequently"></canvas>
      <div id="legend-chart-frequently"></div>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
  },
  components: {

  },
  data() {
    return {};
  },
  methods: {
    bindFrequentlyChart(dataShow) {
      $(".frequently-chart-container-wrapper").show();
      frequentlyConfig.data.labels = [];
      frequentlyConfig.data.datasets = [];
      window.frequentlyChart.update();

      let newDataset = null;
      if (this.requestParam.chartDataType === "CYCLE_TIME_ANALYSIS") {
        newDataset = {
          type: "bar",
          label: "Shot",
          backgroundColor: hexToRgba(getStyle("--info"), 50),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      } else {
        newDataset = {
          type: "line",
          label: "Temperature",
          backgroundColor: hexToRgba(getStyle("--info"), 10),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      }

      const data = dataShow;

      for (let i = 0; i < data.length; i++) {
        let title = data[i].title;
        frequentlyConfig.data.labels.push(title);
        newDataset.data.push(data[i].data);
      }
      frequentlyConfig.options.tooltips = {
        enabled: true,
        intersect: true,
        callbacks: {
          afterLabel(tooltipItem) {
            const { index } = tooltipItem;
            let avgCavities = 0;
            if (data[index]) {
            }

            return ``;
          },
          label(tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel).toString();
            return `${label}: ${value}`;
          },
        },
      };
      frequentlyConfig.data.datasets.push(newDataset);
      //set caption
      frequentlyConfig.options.scales.xAxes[0].scaleLabel.labelString =
          this.xAxisDetailTitleMapping[this.requestParam.chartDataType];
      frequentlyConfig.options.scales.yAxes[0].scaleLabel.labelString =
          this.yAxisDetailTitleMapping[this.requestParam.chartDataType];
      frequentlyConfig.options.scales.yAxes[0].fontSize = 13;
      frequentlyConfig.options.scales.xAxes[0].fontSize = 13;
      window.frequentlyChart.update();
      this.updateChartFrequentlyLegend();
    },

    updateChartFrequentlyLegend() {
      let frequentlyHtmlItem = frequentlyChart
          .generateLegend()
          .replace(/legend-item/g, "legend-frequently-item");
      $("#legend-chart-frequently").html(frequentlyHtmlItem);

      // set click frequently legend event
      for (let i = 0; i < frequentlyChart.data.datasets.length; i++) {
        $("#legend-frequently-item-" + i).on("click", () => {
          let meta = frequentlyChart.getDatasetMeta(i);
          if (meta.hidden) {
            // show if is hiding
            meta.hidden = null;
            $(this).removeClass("removed");
          } else {
            // hide if is not hiding
            meta.hidden = true;
            $(this).addClass("removed");
          }
          frequentlyChart.update();
        });
      }
    },

  },
  mounted() {

  }
}

</script>