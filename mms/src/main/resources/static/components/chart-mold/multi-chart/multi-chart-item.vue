<template>
  <div class="multi-chart-item">
    <chart-graph
      ref="chartGraphRef"
      :id="'chart-'+ toolingId"
      :is-mobile="true"
      chart-wrap-style="width: 100%"
      legend-icon-style="width: 300px; height: 20px;"
      legend-label-style="font-size: 0.8vw;"
      :resources="resources"
      type="mold"
      :show-file-previewer="() => {}"
    >
    </chart-graph>
  </div>
</template>

<script cus-no-cache src="chart-mold.js"></script>
<script>
module.exports = {
  name: 'MultiChartItem',
  components: {
    'chart-graph': httpVueLoader('/components/chart-mold/detail-graph/chart-graph.vue'),
  },
  props: {
    resources: Object,
    requestParam: Object,
    optimalCycleTime: [Array, Object],
    // graphSettingDataList: Array,
    toolingId: Number,
  },
  setup(props, ctx) {
    const loadChart = async (requestParam) => {
      let optimalCycleTime = [];
      let chartTypeConfig;
      let moldId = requestParam.moldId;
      let mold = { id: moldId };

      let chartType = requestParam.chartType || "QUANTITY";
      let graphSettingDataList;

      try {
        let resList = await Promise.all([
          CommonChart.getOptimalCycleTime(),
          axios.get('/api/molds/' + moldId),
          CommonChart.getCurrentChartSettings(chartType),
          CommonChart.getChartTypeConfig(moldId),
        ]);
        optimalCycleTime = resList[0] || [];
        mold = resList[1]?.data || mold;
        graphSettingDataList = resList[2];
        chartTypeConfig = resList[3];
      } catch (e) {
        console.log(e)
      }


      let oct = this.octs[optimalCycleTime.strategy || 'ACT'];
      graphSettingDataList = graphSettingDataList || CommonChart.getGraphOptionsDefault(chartType, oct, chartTypeConfig);
      console.log("graphSettingDataList", graphSettingDataList);

      var intervalId = setInterval(async () => {
        var child = Common.vue.getChild(this.$children, 'chart-graph');
        console.log("check load chart")
        if (child != null) {
          await child.initGraphData(oct, chartTypeConfig);
          child.loadChartGraph(mold, requestParam, chartType, oct, graphSettingDataList);
          clearInterval(intervalId)
        }
      }, 100)
    }

    watch(() => props?.requestParam, newVal => {
      if (newVal?.chartDataType) {
        console.log('props?.requestParam', newVal)
        __requestParam = { ...newVal, moldId: props.toolingId }
        loadChart(__requestParam)
      }
    }, { immediate: true, deep: true })

    return {
      chartGraphRef
    }
  }
}
</script>
