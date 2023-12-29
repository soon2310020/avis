<template>
  <div class="config-area alert-configuration">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['shot_filter']"></h4>
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
            <strong v-text="resources['hide_testing_blank_shots']"></strong>
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
          class=""
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
            <label v-text="resources['hide_testing_blank_shots_desc']"></label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "DataFilter",
  props: {
    resources: Object,
    handleIsChange: Function,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    id: Number,
  },
  data() {
    return {
      configCategory: "DATA_FILTER",
      initConfigValue: [],
    };
  },
  methods: {
    saveData() {
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled
      );
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.handleIsChange();
    },
  },
};
</script>

<style scoped>
.toggleWrapper input.mobileToggle:checked + .change-checkout-status:before {
  background: #3585e5;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

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
