<template>
  <a-tooltip class="mold-status-col" placement="bottomLeft">
    <template slot="title">
      <div
        class="tooltip-label"
        v-text="getTooltipMoldStatus(mold.moldStatus)"
      ></div>
    </template>
    <span
      v-if="mold.moldStatus === 'WORKING'"
      class="op-active label label-working"
    ></span>
    <span
      v-if="mold.moldStatus === 'IDLE'"
      class="op-active label label-idle"
    ></span>
    <span
      v-if="mold.moldStatus === 'NOT_WORKING'"
      class="op-active label label-not-working"
    ></span>
    <span
      v-if="mold.moldStatus === 'SENSOR_OFFLINE'"
      class="op-active label label-sensor-offline"
    ></span>
    <span
      v-if="mold.moldStatus === 'SENSOR_DETACHED'"
      class="op-active label label-sensor-detached"
    ></span>
    <span
      v-if="mold.moldStatus === 'NO_SENSOR'"
      class="op-active label label-no-sensor"
    ></span>
    <span
      v-if="mold.moldStatus === 'DISABLED'"
      class="op-active label label-disabled"
    ></span>
    <span
      v-if="mold.moldStatus === 'ON_STANDBY'"
      class="op-active label label-on-standby"
    ></span>
    <span
      v-if="mold.moldStatus === 'DISPOSED'"
      class="op-active label label-disposed"
    ></span>
    <span
      class="mold-status-text"
      v-text="getMoldStatusText(mold.moldStatus)"
    ></span>
  </a-tooltip>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    mold: Object,
    ruleOpStatus: Object,
  },
  data() {
    return {};
  },
  watch: {},
  methods: {
    getTooltipMoldStatus(status) {
      switch (status) {
        case "WORKING":
          return this.ruleOpStatus.inProduct;
        case "IDLE":
          return this.ruleOpStatus.idle;
        case "NOT_WORKING":
          return this.ruleOpStatus.inactive;
        case "SENSOR_OFFLINE":
          return this.ruleOpStatus.sensorOffline;
        case "SENSOR_DETACHED":
          return this.ruleOpStatus.sensorDetached;
        case "NO_SENSOR":
          return this.ruleOpStatus.noSensor;
        case "DISABLED":
          return this.ruleOpStatus.disabled;
        case "ON_STANDBY":
          return this.ruleOpStatus.onStandby;
        case "DISPOSED":
          return this.ruleOpStatus.disposed;
        default:
          return "";
      }
    },
    getMoldStatusText(status) {
      switch (status) {
        case "WORKING":
          return "In Production";
        case "IDLE":
          return "Idle";
        case "NOT_WORKING":
          return "Inactive";
        case "SENSOR_OFFLINE":
          return "Sensor Offline";
        case "SENSOR_DETACHED":
          return "Sensor detached";
        case "NO_SENSOR":
          return "No Sensor";
        case "DISABLED":
          return "Disabled";
        case "ON_STANDBY":
          return "On Standby";
        case "DISPOSED":
          return "Disposed";
      }
    },
  },
  mounted() {},
};
</script>

<style scoped>
.tooling-status {
  min-width: 130px;
}
.label-working {
  background: #41ce77;
}
.label-idle {
  background: #f7cc57;
}
.label-not-working {
  background: #aca6bc;
}
.label-sensor-offline {
  background: #e34537;
}
.label-disposed {
  background: #000000;
}
.label-sensor-detached {
  border: 1px dashed #e34537;
}
.label-no-sensor {
  border: 1px solid #4b4b4b;
}
.label-disabled {
  background: #4b4b4b;
}
.label-on-standby {
  background-color: #c4c4c4;
  border: 1px solid #4b4b4b;
}
.op-active {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
  padding: 0px;
  margin-top: 5px;
  display: inline-block;
}
.tooltip-label {
  padding: 6px 8px;
}
</style>
