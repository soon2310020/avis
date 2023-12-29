<template>
  <div class="chart-uptime-status"></div>
</template>

<script>
module.exports = {
  name: 'ChartUptimeStatus',
  components: {
    ChartDoughnut: httpVueLoader('/components/@chart/chart-doughnut.vue')
  },
  setup() {

    return {}
  }
}
</script>
