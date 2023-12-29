<template>
  <div>
    <canvas
      ref="chartRef"
      :id="`chart-tooling-${id}`"
      :width="width"
      :height="height"
      role="chart"
    ></canvas>
  </div>
</template>

<script>

module.exports = {
  name: 'tooling-chart',
  props: {
    id: {
      type: String,
      default: () => ('')
    },
    data: {
      type: Object,
      default: () => ({})
    },
    options: {
      type: Object,
      default: () => ({})
    },
    width: [String, Number],
    height: [String, Number],
  },
  setup(_, { refs }) {
    const chart = ref(null);
    const chartRef = ref(null)

    const __updateChart = () => {
      chart.value.update()
      chart.value.resize()
    }

    onMounted(() => {
      const ctx = refs.chartRef
      chart.value = new Chart(ctx, options)
    })

    return {
      chartRef
    }
  }
}
</script>

<style scoped>
</style>