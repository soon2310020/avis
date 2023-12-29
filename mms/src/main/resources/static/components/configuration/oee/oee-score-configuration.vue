<template>
  <div>
    <div class="config-header">
      <div class="left-header">
        <h4>OEE Score Configuration</h4>
      </div>
    </div>
    <div class="oee-config-color-container">
      <div class="oee-config-color-card">
        <risk-default
          style="margin: 0"
          :is-in-admin="true"
          :key="'risk'"
          :show-risk-level="true"
          :resources="resources"
          :update-risk="updateRisk"
          :risk-level-data="riskLevelData"
          :close-risk="() => {}"
        >
        </risk-default>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  components: {
    "risk-default": httpVueLoader("/components/chart-new/oee/risk-default.vue"),
  },
  props: {
    resources: Object,
    updateRisk: Function,
    riskLevelData: Array,
  },
  methods: {},
};
</script>
