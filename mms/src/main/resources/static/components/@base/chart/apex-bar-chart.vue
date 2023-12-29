<template>
  <div class="chart-new-container" :class="getChartClass">
    <div ref="apexBarChartRef"></div>
    <slot name="custom-legend"></slot>
  </div>
</template>

<script>
module.exports = {
  props: {
    id: {
      type: String,
      default: () => ('')
    },
    options: {
      type: Object,
      default: () => ({})
    },
    customLegendPosition: {
      type: String,
      default: () => ('')
    }
  },
  data() {
    return {
      chart: null,

    }
  },
  computed: {
    getChartDataConfig() {
      return { 
        ...this.options
      }
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
  methods: {
    generateChart() {
      this.chart.updateOptions({ ...this.options })
    }
  },
  mounted() {
    const ctx = this.$refs.apexBarChartRef
    const dataConfig = { ...this.getChartDataConfig }
    this.chart = new ApexCharts(ctx, dataConfig);
    this.chart.render()
  }
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