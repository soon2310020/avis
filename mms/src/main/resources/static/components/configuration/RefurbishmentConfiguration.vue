<template>
  <div class="config-area refurbishment-configuration">
    <div class="config-header" style="width: 600px">
      <div class="left-header">
        <h4
          :class="{
            'disable-text': !categoryStatus.enabled,
            'enable-text': categoryStatus.enabled,
          }"
        >
          Refurbishment Priority
        </h4>
      </div>
      <!-- <div class="right-header">
        <div class="switch-status" :class="{'disable-text' : categoryStatus.enabled, 'enable-text' : !categoryStatus.enabled}">Off </div>
        <div class="switch-button">
          <div class="toggleWrapper">
            <input type="checkbox" v-model="categoryStatus.enabled" class="mobileToggle" id="part-toggle" value="1">
            <label class="change-checkout-status" @click="updateConfigStatus"></label>
          </div>
        </div>
        <div class="switch-status" :class="{'disable-text' : !categoryStatus.enabled, 'enable-text' : categoryStatus.enabled}"> On</div>
      </div> -->
    </div>
    <div
      class="config-body"
      :class="{
        'disable-text': !categoryStatus.enabled,
        'enable-text': categoryStatus.enabled,
      }"
    >
      <div class="trigger-action">
        <div class="action-item">
          <input
            name="refurbishment-priority"
            type="radio"
            v-model="categoryStatus.configOption"
            :value="triggerOptions.UTILIZATION_RATE"
            @change="updateConfigOption"
            :disabled="!categoryStatus.enabled"
          />
          Utilization Rate
        </div>
        <div class="action-item">
          <input
            name="refurbishment-priority"
            type="radio"
            v-model="categoryStatus.configOption"
            :value="triggerOptions.REMAINING_DAY"
            @change="updateConfigOption"
            :disabled="!categoryStatus.enabled"
          />
          Remaining Days
        </div>
      </div>
      <utilization-rate
        v-show="categoryStatus.configOption === triggerOptions.UTILIZATION_RATE"
        @set-total-config="setTotalConfig"
        :resources="resources"
        :update-config="updateConfig"
        :category-status="categoryStatus"
      ></utilization-rate>
      <remaining-day
        v-show="categoryStatus.configOption === triggerOptions.REMAINING_DAY"
        :resources="resources"
        :update-config="updateConfig"
        :category-status="categoryStatus"
      ></remaining-day>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "RefurbishmentConfiguration",
  props: {
    resources: Object,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    handleIsChange: Function,
  },
  components: {
    "utilization-rate": httpVueLoader(
      "/components/configuration/refurbishment/utilization-rate.vue"
    ),
    "remaining-day": httpVueLoader(
      "/components/configuration/refurbishment/remaining-day.vue"
    ),
  },
  data() {
    return {
      configCategory: "REFURBISHMENT",
      initConfigValue: [],
      triggerOptions: {
        UTILIZATION_RATE: "UTILIZATION_RATE",
        REMAINING_DAY: "REMAINING_DAY",
      },
      totalConfig: {},
    };
  },
  methods: {
    cancelData() {
      var utilizRate = Common.vue.getChild(this.$children, "utilization-rate");
      if (utilizRate != null) {
        utilizRate.getAllData();
      }
      var remainingDay = Common.vue.getChild(this.$children, "remaining-day");
      if (remainingDay != null) {
        remainingDay.getAllData();
      }
    },
    saveData() {
      this.$emit("update-config", this.totalConfig);
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled,
        this.categoryStatus.configOption
      );
    },
    setTotalConfig(totalConfig) {
      this.totalConfig = totalConfig;
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.updateConfigOption();
    },
    updateConfigOption() {
      // this.handleIsChange();
    },
    updateConfig(data) {
      Object.keys(data).forEach((field) => {
        this.totalConfig[field] = data[field];
      });
    },
  },
  mounted() {},
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
.save-btn:hover {
  background: #3ea662;
  border-radius: 3px;
}
.save-btn:active {
  background: #3ea662;
  border: 2px solid #c3f2d7;
}
.cancel-btn:hover {
  background: #f4f4f4;
  /* Background Colour/TOOLTIP BKG */

  border: 1px solid #4b4b4b;
}
.cancel-btn:active {
  background: #f4f4f4;
  /* Background Colour/Light Grey_Bkg_border */

  border: 2px solid #d6dade;
}
.save-btn {
  height: 29px;
  width: 49px;
  background: #47b576;
  border-radius: 3px;
  padding: 6px 8px;
  gap: 10px;
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #ffffff;
}
.cancel-btn {
  height: 29px;
  width: 62px;
  background: #ffffff;
  border: 1px solid #d6dade;
  border-radius: 3px;
  padding: 6px 8px;
  gap: 10px;
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #595959;
  margin-right: 10px;
}
.footer-style {
  margin-right: 63px;
  padding-bottom: 28px;
}
.config-area {
  padding-top: 44px !important;
}
</style>
