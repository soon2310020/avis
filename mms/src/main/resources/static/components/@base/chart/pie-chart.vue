<template>
  <div class="chart-new-container" :class="getChartClass">
    <div>
      <canvas
        :id="`chart-pie-${id}`"
        :ref="`chart-pie-${id}`"
        class="chart-pie"
        :width="width"
        :height="height"
        role="chart"
      ></canvas>
    </div>
    <slot v-if="showCustomLegend" name="custom-legend"></slot>
  </div>
</template>

<script>
module.exports = {
  name: 'pie-chart',
  props: {
    id: {
      type: String,
      default: () => ('')
    },
    changeState: {
      type: Number,
      default: () => (0)
    },
    labels: {
      type: Array,
      default: () => ([])
    },
    listDataset: {
      type: Array,
      default: () => ([])
    },
    legendLabel: {
      type: String,
      default: () => ('')
    },
    width: [String, Number],
    height: [String, Number],
    legendConfig: {
      type: Object,
      default: () => ({})
    },
    customLegendPosition: {
      type: String,
      default: () => ('')
    },
    customOptions: {
      type: Object,
      default: () => ({})
    },
    showCustomLegend: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      chart: null,
      config: {
        type: 'pie',
        data: {
          datasets: []
        },
        options: {
          plugins: {
          },
        },
      },
      chartOptions: {
      }
    }
  },
  computed: {
    getChartDataConfig() {
      let config = { ...this.config }
      config.data.labels = this.labels
      config.data.datasets = this.listDataset
      config.options.legend = this.legendConfig
      config.options.onClick = this.handleClick
      config.options = { ...config.options, ...this.customOptions }
      return config
    },
    getChartClass() {
      let resultClass = ''
      switch (this.customLegendPosition) {
        case 'left':
          resultClass = 'chart-row row-reverse'
          break;
        case 'top':
          resultClass = 'chart-row column-reverse'
          break;
        case 'right':
          resultClass = 'chart-row'
          break;
        case 'bottom':
          resultClass = 'chart-row column'
          break;
        default:
          resultClass = ''
          break;
      }
      console.log('class', this.customLegendPosition, resultClass)
      return resultClass
    }
  },
  watch: {
    changeState() {
      this.generateChart()
    }
  },
  methods: {
    generateChart() {
      if (this.chart != null && this.chart.options) {
        this.chart.destroy();
      }
      const ctx = this.$refs[`chart-pie-${this.id}`]
      const dataConfig = { ...this.getChartDataConfig }
      this.chart = new Chart(ctx, dataConfig)
      this.updateChart()
    },
    updateChart() {
      console.log('@update', this.chart)
      this.chart.resize()
      this.chart.update()
    },
    handleClick(e) {
      const activePoints = this.chart.getElementsAtEventForMode(e, 'nearest', { intersect: true }, true);
      if (activePoints.length > 0) {
        const selectedIndex = activePoints[0]._index;
        const datasetIndex = activePoints[0]._datasetIndex;
        this.$emit('chart-click', datasetIndex, selectedIndex)
      }
    }
  },
  mounted() {
    console.log('run mounted chart')
    this.generateChart()
  },
}
</script>

<style scoped>
.chart-new-container {
  display: flex;
  align-items: center;
}
.chart-row {
  display: flex;
  align-items: center;
  height: fit-content;
}
.row-reverse {
  flex-direction: row-reverse;
}
.column-reverse {
  flex-direction: column-reverse;
}
.column {
  flex-direction: column;
}
</style>