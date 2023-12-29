<template>
  <div class="backup-tooling-popup-container">
    <div class="d-flex align-center justify-space-between backup-tooling-row">
      <span class="text--bold">{{ resources["tooling"] }}</span>
      <span class="text--bold">{{ resources["utilization_rate"] }}</span>
    </div>
    <div class="backup-tooling-content">
      <div
        v-for="item in backupToolingList"
        :key="item.moldId"
        class="d-flex align-center justify-space-between backup-tooling-row"
      >
        <div class="d-flex">
          <span
            class="custom-hyper-link"
            style="margin-right: 4px"
            @click="$emit('click-item', item)"
            >{{ item.moldCode }}</span
          >
          <span v-if="item.moldId === moldId"
            >*{{ resources["this_tooling"] }}</span
          >
        </div>
        <div>
          <span>{{ item.producedRate }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    moldId: [String, Number],
    backupToolingList: {
      type: Array,
      default: () => [],
    },
  },
  methods: {},
};
</script>

<style scoped>
.backup-tooling-popup-container {
  width: 300px;
  font-size: 14.66px;
  color: #4b4b4b;
}
.backup-tooling-content {
  max-height: 400px;
  overflow: auto;
  padding-right: 5px;
}
.backup-tooling-row {
  margin-bottom: 5px;
}
</style>
