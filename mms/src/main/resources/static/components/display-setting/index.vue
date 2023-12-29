<template>
  <div class="custom-body d-flex flex-column justify-space-between">
    <chart-type
      ref="chart-type"
      :resources="resources"
      :handle-is-change="handleIsChange"
    ></chart-type>
    <div class="d-flex flex-row-reverse footer-style">
      <div>
        <a-button
          type="link"
          class="save-btn custom-save"
          @click.stop="saveHandler"
        >
          <span>Save</span>
        </a-button>
      </div>
      <div>
        <a-button type="link" class="cancel-btn custom-cancel" @click="cancel">
          <span>Cancel</span>
        </a-button>
      </div>
    </div>
    <save-alert :save-handler="saveHandler" :cancel-handler="cancel">
    </save-alert>
  </div>
</template>
<script>
module.exports = {
  components: {
    "chart-type": httpVueLoader("/components/configuration/ChartType.vue"),
    "save-alert": httpVueLoader("/components/configuration/SaveAlert.vue"),
  },
  computed: {
    resources() {
      return headerVm?.resourcesFake || {};
    },
  },
  data() {
    return {
      isChanged: false,
    };
  },
  methods: {
    handleIsChange(isChange) {
      this.isChange = isChange;
    },
    saveHandler() {
      var displayChild = this.$refs["chart-type"];
      if (displayChild != null) {
        displayChild.saveData();
      }
      this.isChanged = false;
      Common.alert("Saved Successfully");
    },
    cancel() {
      var displayChild = this.$refs["chart-type"];
      if (displayChild != null) {
        displayChild.getChartTypeConfig();
      }
      this.isChanged = false;
    },
  },
};
</script>
