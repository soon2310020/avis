<template>
  <div v-if="configCategory != 'ALERT'" class="config-area alert-configuration">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['alert_configuration.title']"></h4>
      </div>
    </div>
    <div class="config-body">
      <div class="">
        <div class="config-header">
          <div
            class="left-header"
            :class="{
              'disable-text': !categoryStatus.enabled,
              'enable-text': categoryStatus.enabled,
            }"
          >
            <strong
              v-text="resources['alert_configuration.cycleTimeAlert']"
            ></strong>
          </div>
          <div class="right-header">
            <div
              class="switch-status"
              :class="{
                'disable-text': categoryStatus.enabled,
                'enable-text': !categoryStatus.enabled,
              }"
            >
              Off
            </div>
            <div class="switch-button">
              <div class="toggleWrapper">
                <input
                  type="checkbox"
                  v-model="categoryStatus.enabled"
                  class="mobileToggle"
                  id="part-toggle"
                  value="1"
                />
                <label
                  class="change-checkout-status"
                  @click="updateConfigStatus"
                ></label>
              </div>
            </div>
            <div
              class="switch-status"
              :class="{
                'disable-text': !categoryStatus.enabled,
                'enable-text': categoryStatus.enabled,
              }"
            >
              On
            </div>
          </div>
        </div>
        <div
          :class="{
            'disable-text': !categoryStatus.enabled,
            'enable-text': categoryStatus.enabled,
          }"
        >
          <div
            style="
              display: flex;
              flex-direction: row;
              justify-content: left;
              align-items: center;
            "
          >
            <label v-text="resources['alert_configuration.note1']"></label>
            <input
              v-model="alertConfig.percentageThreshold"
              @input="updateAlertConfiguration(alertConfig)"
              class="alert-input form-control"
              style="background-color: #ffffff"
              :readonly="!categoryStatus.enabled"
              :class="{
                'disable-box': !categoryStatus.enabled,
                'enable-box': categoryStatus.enabled,
              }"
            />
            <label v-text="resources['alert_configuration.note2']"></label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "AlertConfiguration",
  props: {
    resources: Object,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    id: Number,
  },
  data() {
    return {
      configCategory: "ALERT",
      initConfigValue: [],
      alertConfig: {
        percentageThreshold: 1,
        id: null,
      },
    };
  },
  methods: {
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled
      );
    },
    updateAlertConfiguration(alertConfig) {
      console.log("updateAlertConfiguration");
      console.log(this.id);
      if (this.id) {
        alertConfig.id = this.id;
      }
      this.$emit("update-config", alertConfig);
    },
    getCurrentConfig() {
      Common.getCurrentAlertConfig().then((data) => {
        console.log("AlertConfiguration.vue>getCurrentConfig", data);
        if (data.id) {
          this.alertConfig = data;
        } else {
          this.updateAlertConfiguration(this.alertConfig);
        }
      });
    },
  },
  mounted() {
    // TODO(ducnm2010) need check since result always empty
    // this.getCurrentConfig();
  },
};
</script>

<style scoped>
.dialogTooling:hover {
  color: blue;
}
.alert-input {
  width: 50px;
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 8px;
  text-align: center;
}
</style>
