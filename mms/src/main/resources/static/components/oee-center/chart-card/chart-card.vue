<template>
  <div
    class="style-1 machine-downtime-card"
    v-on="$listeners"
  >
    <div class="mini-card-container">
      <div class="trend-chart-wrapper">
        <div
          v-if="status"
          class="trend-status"
        >
          {{resources['trend']}} {{ percentFormatted }}%
        </div>
        <trend-chart
          :datasets="getTrendChartData(percent)"
          :interactive="false"
          class="trend-chart"
          key="oeeDownTime"
          padding="8 20 8 35"
          :class="percent >= 0 ? 'up' : 'down'"
          ref="trendChart"
        >
        </trend-chart>
      </div>
      <div class="description-box">
        <p>{{ label }}</p>
        <p>
          <strong>{{ valueFormatted }}</strong>
          <span v-if="valueUnit">
            {{ value > 1 ? `${valueUnit}s` : valueUnit }}
          </span>
        </p>
      </div>
    </div>
  </div>
</template>

<script>

const waitForMounting = 500 // ms
module.exports = {
  name: 'ChartCard',
  props: {
    getTrendChartData: {
      type: Function,
      default: () => { }
    },
    changeOeeShow: {
      type: Function,
      default: () => { }
    },
    resources: Object,
    label: String,
    oeeOverview: Object,
    oeeShow: Object,
    status: Boolean,
    percent: [Number, String],
    value: [Number, String],
    valueUnit: String,
    percentFormatted: [Number, String],
    valueFormatted: [Number, String]
  },
  setup(props) {
    const trendChart = ref(null)

    onMounted(() => {
      setTimeout(() => {
        trendChart.value.onWindowResize()
      }, waitForMounting)
    })

    watchEffect(() => {
      console.log('watchEffect chart card')
      console.log(props.label, {
        percent: props.percent,
        value: props.value,
        percentFormatted: props.percentFormatted,
        valueFormatted: props.valueFormatted
      })
    })

    return {
      trendChart
    }
  }
}
</script>
