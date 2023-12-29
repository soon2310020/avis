<template>
  <div class="config-area op-configuration chart-type-wrapper">
    <div class="config-header">
      <div class="left-header">
        <span class="title-style" v-text="resources['chart_type']"></span>
      </div>
    </div>
    <div class="config-body">
      <div class="chart-type-items">
        <div class="chart-type-item">
          <div class="chart-type-item-left">
            <span v-text="resources['total_part_quantity']"></span>
          </div>
          <div class="chart-type-item-right">
            <p-radio v-model="totalPart" @change="changeHandler" value="BAR">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/bar-icon.svg" alt="" />
                <span v-text="resources['bar']"></span>
              </div>
            </p-radio>
            <p-radio v-model="totalPart" @change="changeHandler" value="LINE">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/line-icon.svg" alt="" />
                <span v-text="resources['line']"></span>
              </div>
            </p-radio>
          </div>
        </div>
        <div class="chart-type-item">
          <div class="chart-type-item-left">
            <span v-text="resources['cap_shot']"></span>
          </div>
          <div class="chart-type-item-right">
            <p-radio v-model="shot" @change="changeHandler" value="BAR">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/bar-icon.svg" alt="" />
                <span v-text="resources['bar']"></span>
              </div>
            </p-radio>
            <p-radio v-model="shot" @change="changeHandler" value="LINE">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/line-icon.svg" alt="" />
                <span v-text="resources['line']"></span>
              </div>
            </p-radio>
          </div>
        </div>
        <div class="chart-type-item">
          <div class="chart-type-item-left">
            <span v-text="resources['cycle_time_within_l1_l2']"></span>
          </div>
          <div class="chart-type-item-right">
            <p-radio v-model="cycleTime" @change="changeHandler" value="BAR">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/bar-icon.svg" alt="" />
                <span v-text="resources['bar']"></span>
              </div>
            </p-radio>
            <p-radio v-model="cycleTime" @change="changeHandler" value="LINE">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/line-icon.svg" alt="" />
                <span v-text="resources['line']"></span>
              </div>
            </p-radio>
          </div>
        </div>
        <div class="chart-type-item">
          <div class="chart-type-item-left">
            <span v-text="resources['uptime_within_l1_l2']"></span>
          </div>
          <div class="chart-type-item-right">
            <p-radio v-model="upTime" @change="changeHandler" value="BAR">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/bar-icon.svg" alt="" />
                <span v-text="resources['bar']"></span>
              </div>
            </p-radio>
            <p-radio v-model="upTime" @change="changeHandler" value="LINE">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/line-icon.svg" alt="" />
                <span v-text="resources['line']"></span>
              </div>
            </p-radio>
          </div>
        </div>
        <div class="chart-type-item">
          <div class="chart-type-item-left">
            <span v-text="resources['temperature']"></span>
          </div>
          <div class="chart-type-item-right">
            <p-radio v-model="temperature" @change="changeHandler" value="BAR">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/bar-icon.svg" alt="" />
                <span v-text="resources['bar']"></span>
              </div>
            </p-radio>
            <p-radio v-model="temperature" @change="changeHandler" value="LINE">
              <div class="p-radio-icon-wrapper">
                <img src="/images/icon/line-icon.svg" alt="" />
                <span v-text="resources['line']"></span>
              </div>
            </p-radio>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "ChartType",
  props: {
    resources: Object,
    handleIsChange: Function,
  },
  data() {
    return {
      totalPart: "BAR",
      shot: "BAR",
      cycleTime: "LINE",
      upTime: "LINE",
      temperature: "LINE",
    };
  },
  methods: {
    changeHandler() {
      this.handleIsChange();
    },
    saveData() {
      const params = {
        chartTypeConfig: [
          {
            chartDataType: "PART_QUANTITY",
            chartType: this.totalPart,
            chartDataTypeName: "Total Part Quantity",
          },
          {
            chartDataType: "SHOT",
            chartType: this.shot,
            chartDataTypeName: "Shot",
          },
          {
            chartDataType: "CYCLE_TIME",
            chartType: this.cycleTime,
            chartDataTypeName: "Cycle Time (Within, L1, L2)",
          },
          {
            chartDataType: "UPTIME",
            chartType: this.upTime,
            chartDataTypeName: "Uptime (Within, L1, L2)",
          },
          {
            chartDataType: "TEMPERATURE",
            chartType: this.temperature,
            chartDataTypeName: "Temperature",
          },
        ],
      };
      axios.post("/api/common/dsp-stp", params).catch((err) => {
        console.log(err);
      });
    },
    getChartTypeConfig() {
      axios.get("/api/common/dsp-stp").then((res) => {
        this.totalPart = res.data.chartTypeConfig.filter(
          (item) => item.chartDataType === "PART_QUANTITY"
        )[0].chartType;
        this.shot = res.data.chartTypeConfig.filter(
          (item) => item.chartDataType === "SHOT"
        )[0].chartType;
        this.cycleTime = res.data.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType;
        this.upTime = res.data.chartTypeConfig.filter(
          (item) => item.chartDataType === "UPTIME"
        )[0].chartType;
        this.temperature = res.data.chartTypeConfig.filter(
          (item) => item.chartDataType === "TEMPERATURE"
        )[0].chartType;
      });
    },
  },
  mounted() {
    this.getChartTypeConfig();
  },
  watch: {},
};
</script>
<style>
.p-radio-icon-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  right: -42px;
  top: -22px;
}
.p-radio-icon-wrapper img {
  max-width: 30px;
}
.chart-type-wrapper .pretty.p-default input:checked ~ .state label:after {
  background-color: #0075ff !important;
  background: #0075ff !important;
  width: 18px;
  height: 18px;
  /* top: calc((0% - (100% - 0.87em)) - 8%); */
  top: -4px;
  left: -2px;
}
.chart-type-wrapper .pretty .state label {
  margin-left: 4px;
  text-indent: unset !important;
}
.chart-type-wrapper .pretty .state label:before,
.chart-type-wrapper .pretty .state label:after {
  width: 14px;
  height: 14px;
}
.chart-type-wrapper .p-round .state label:before {
  border: 0.7px solid #909090;
}
.chart-type-wrapper .pretty .state label:before {
  border-color: #909090;
}
.chart-type-wrapper .pretty:hover .state label:before {
  border-color: #4b4b4b;
}
.chart-type-wrapper .pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff;
  /*border-radius: 1px;*/
}
.chart-type-wrapper .pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff;
  /*border-radius: 1px;*/
}
.chart-type-wrapper .pretty span {
  margin-left: 0;
  font-size: 12px;
  margin-top: 3px;
}

.chart-type-wrapper .pretty {
  margin-right: unset;
}
</style>
<style scoped>
.config-header {
  margin-bottom: 30px;
}
.chart-type-item {
  margin-bottom: 50px;
  display: flex;
  justify-content: space-between;
  max-width: 500px;
}
.chart-type-item-left {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px !important;
  line-height: 17px;

  color: #4b4b4b;
}
.chart-type-item-right {
  color: #4b4b4b;
  display: flex;
  align-items: center;
}
.chart-type-item-right .pretty:first-child {
  margin-right: 101px;
}
.title-style {
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 700;
  font-size: 16px;
  line-height: 20px;
  /* identical to box height */

  color: #000000;
}
</style>
