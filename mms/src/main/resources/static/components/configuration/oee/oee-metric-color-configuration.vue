<template>
  <div>
    <div class="oee-config-color-container">
      <div class="left-header">
        <h4>OEE Metric Color Configuration</h4>
      </div>
      <div v-for="config in configs" style="margin: 10px 0">
        <label>{{ config.title }}</label>
        <div class="oee-config-color-card">
          <risk-default
            style="margin: 0"
            :is-in-admin="true"
            :key="'risk'"
            :show-risk-level="true"
            :resources="resources"
            :update-risk="config.updateColor"
            :risk-level-data="config.riskLevel"
            :close-risk="() => {}"
          >
          </risk-default>
        </div>
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
    configs: Array,
  },
  methods: {},
};
</script>
