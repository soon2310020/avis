<template>
  <div class="config-area alert-configuration">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['optimalCycleTime.title']"></h4>
      </div>
    </div>
    <div class="config-body">
      <div class="">
        <div class="config-header form-check first-check form-check-inline">
          <input
            id="ACT"
            v-model="categoryStatus.strategy"
            type="radio"
            class="form-check-input"
            :value="'ACT'"
            @change="updateConfigData()"
          />
          <div class="left-header">
            <label for="ACT"
              ><strong
                v-text="resources['optimalCycleTime.approvedCycleTime']"
              ></strong
            ></label>
          </div>
        </div>
        <div class="config-header form-check first-check form-check-inline">
          <input
            id="WACT"
            v-model="categoryStatus.strategy"
            type="radio"
            class="form-check-input"
            :value="'WACT'"
            @change="updateConfigData()"
          />
          <div class="left-header">
            <label for="WACT"
              ><strong
                v-text="resources['optimalCycleTime.weightedAverage']"
              ></strong
            ></label>
          </div>

          <common-button
            :title="weighted_average"
            @click="handleWeightedAverageToggle"
            :is-show="showWeightedAverage"
          >
          </common-button>
          <common-select-popover
            :is-visible="showWeightedAverage"
            @close="handlecloseWeightedAverage"
            class="w-95 width-popover"
          >
            <common-select-dropdown
              id="showWeightedAverage"
              :searchbox="false"
              :class="{ show: showWeightedAverage }"
              :items="weightAveergeList"
              :checkbox="false"
              class="dropdown"
              :set-result="() => {}"
              :click-handler="selectWeightedAverage"
            ></common-select-dropdown>
          </common-select-popover>
        </div>
        <div class="enable-text">
          <div
            style="
              display: flex;
              flex-direction: row;
              justify-content: left;
              align-items: center;
            "
          >
            <label
              v-if="!enable"
              for=""
              v-text="resources['optimalCycleTime.note1']"
            ></label>
            <label
              v-if="enable"
              for=""
              v-text="resources['optimalCycleTime.note2']"
            ></label>
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
    enable: Boolean,
    handleIsChange: Function,
  },
  data() {
    return {
      showWeightedAverage: false,
      weighted_average: "",
      weightAveergeList: [
        { title: "3 Months", code: "3" },
        { title: "6 Months", code: "6" },
        { title: "9 Months", code: "9" },
      ],
      configCategory: "ALERT",
      initConfigValue: [],
      alertConfig: {
        percentageThreshold: 1,
        id: null,
      },
    };
  },
  methods: {
    handlecloseWeightedAverage() {
      this.showWeightedAverage = false;
    },
    handleWeightedAverageToggle(isShow) {
      this.showWeightedAverage = isShow;
    },
    selectWeightedAverage(item) {
      this.weighted_average = item.title;
      this.categoryStatus.periodMonths = item.code;
      this.showWeightedAverage = false;
      this.updateConfigData();
    },
    cancelData() {
      this.getCurrentConfig();
    },
    saveData() {
      this.updateCategoryConfigStatus();
    },
    updateConfigData() {
      const configData = this.categoryStatus;
      console.log("test change field", configData);
      this.handleIsChange();
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled
      );
    },
    updateAlertConfiguration(alertConfig) {
      console.log("OptimalCycleTime>updateAlertConfiguration", alertConfig);
      console.log(this.id);
      if (this.id) {
        alertConfig.id = this.id;
      }
      this.$emit("update-config", alertConfig);
    },
    async getCurrentConfig() {
      const data = await Common.getCurrentAlertConfig();
      console.log("OptimalCycleTime>getCurrentConfig>data", data);
      if (data.id) {
        this.alertConfig = data;
      } else {
        this.updateAlertConfiguration(this.alertConfig);
      }
    },
  },
  mounted() {
    // this.getCurrentConfig();
    console.log("OptimalCycleTime:categoryStatus", this.categoryStatus);
    this.weighted_average = this.categoryStatus.periodMonths + " Months";
  },
  watch: {
    categoryStatus(newVal) {
      // console.log("OptimalCycleTime:categoryStatus", newVal);
    },
    id(newVal) {
      console.log("OptimalCycleTime:id", newVal);
    },
  },
};
</script>

<style scoped>
.btn-custom {
  /*width: 100% !important;*/
  text-align: left !important;
  position: relative;
}
.common-popover {
  margin-top: 41px !important;
  margin-left: -134px !important;
  width: 134px;
  min-width: unset !important;
  position: static !important;
  padding: 2px !important;
  border-radius: 3px !important;
  /*position: unset !important;*/
}
.width-popover {
  width: 132px !important;
}
.dropdown-wrap {
  top: 0px !important;
  left: 0px !important;
  position: absolute;
  padding: 2px !important;
  border-radius: 3px !important;
}
.custom-dropdown-button {
  margin-left: 22px;
  width: 134px !important;
  height: 34px !important;
}
.common-dropdown-class {
  position: inherit;
}
.setting-zindex {
  z-index: 10;
}
.dropdown {
  /*width: 248% !important;*/
  width: 100% !important;
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
label {
  margin-bottom: 0;
}
</style>
