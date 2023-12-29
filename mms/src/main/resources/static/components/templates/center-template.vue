<template>
  <div class="center-template">
    <div class="page-header">
      <div class="page-header-title">
        <slot name="header" v-bind="$props"></slot>
      </div>
      <div class="page-header-tabs">
        <slot name="tabs" v-bind="$props"></slot>
      </div>
    </div>
    <div class="page-content">
      <slot name="content" v-bind="$props"></slot>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'CenterTemplate',
  props: {
    resourceId: String,
    resources: Object,
    permission: Object
  },
};
</script>

<style scoped>
.center-template {}

.center-template .page-header {
  padding-top: 8px;
  margin-bottom: 24px;
}

.center-template .page-header-title {
  font-weight: bold;
}

.center-template .page-header-tabs {
  margin-top: 1rem;
}

.center-template .page-content {}
</style>