<template>
  <div style="width: 100vw">
    <chart-graph
      :id="'chart-' + requestParam.moldId"
      :is-mobile="isMobile"
      chart-wrap-style="width: 96vw; margin-left: 2vw; height: 90vh"
      legend-icon-style="width: 3vw; height: 1vw;"
      legend-label-style="font-size: 0.8vw;"
      :resources="resources"
      type="mold"
      :show-file-previewer="() => {}"
    >
    </chart-graph>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    chartParam: String,
  },
  components: {
    "chart-graph": httpVueLoader(
      "/components/chart-mold/detail-graph/chart-graph.vue"
    ),
  },
  data() {
    return {
      isMobile: {
        default: true,
        type: Boolean,
      },
      requestParam: [],
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
    };
  },
  methods: {
    async loadChart(requestParam) {
      let optimalCycleTime = [];
      let chartTypeConfig;
      let moldId = requestParam.moldId;
      let mold = { id: moldId };

      let chartType = requestParam.chartType || "QUANTITY";
      let graphSettingDataList;

      try {
        let resList = await Promise.all([
          CommonChart.getOptimalCycleTime(),
          axios.get("/api/molds/" + moldId),
          CommonChart.getCurrentChartSettings(chartType),
          CommonChart.getChartTypeConfig(moldId),
        ]);
        optimalCycleTime = resList[0] || [];
        mold = resList[1]?.data || mold;
        graphSettingDataList = resList[2];
        chartTypeConfig = resList[3];
      } catch (e) {
        console.log(e);
      }

      let oct = this.octs[optimalCycleTime.strategy || "ACT"];
      graphSettingDataList =
        graphSettingDataList ||
        CommonChart.getGraphOptionsDefault(chartType, oct, chartTypeConfig);
      console.log("graphSettingDataList", graphSettingDataList);

      var intervalId = setInterval(async () => {
        var child = Common.vue.getChild(this.$children, "chart-graph");
        console.log("check load chart");
        if (child != null) {
          await child.initGraphData(oct, chartTypeConfig);
          /*
            if (!this.isMobile) {
              child.handleChangeDateForPart(moldId,
                {
                  range: {
                    startDate: requestParam.startDate,
                    endDate: requestParam.endDate
                  }, time: requestParam.date
                }, requestParam.dateViewType);
            }
*/
          child.loadChartGraph(
            mold,
            requestParam,
            chartType,
            oct,
            graphSettingDataList
          );
          clearInterval(intervalId);
        }
      }, 100);
    },
  },
  mounted() {
    console.log("grap mobile");
    this.$nextTick(async () => {
      console.log("show chart graph");
      let paramArray = this.chartParam.split("&");
      paramArray.forEach((item) => {
        let result = item.split("=");
        if (result[1] != "" && result[1] != null)
          this.requestParam[result[0]] = result[1];
      });
      if (this.requestParam.isMobile == "false") {
        this.isMobile = false;
      }
      if (this.requestParam.chartDataType)
        this.requestParam.chartDataType =
          this.requestParam.chartDataType.split(",");
      if (
        this.requestParam.isSelectedCustomRange == "yes" ||
        (this.requestParam.startDate && this.requestParam.endDate)
      ) {
        this.requestParam.isSelectedCustomRange = true;
      } else {
        this.requestParam.isSelectedCustomRange = false;
      }

      this.loadChart(this.requestParam);
    });
  },
};
</script>
<style></style>
