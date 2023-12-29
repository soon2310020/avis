<template>
  <div class="tol-config-container">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['overall_utilization']"></h4>
        <h4
          v-text="resources['graph_configuration']"
          :style="{ fontWeight: 500, marginLeft: '4px' }"
        ></h4>
      </div>
    </div>
    <div
      v-for="(item, index) of config"
      :key="item.key"
      class="tol-config-item"
      :class="{ 'even-row': index % 2 === 1 }"
    >
      <div class="tol-config-label">
        <span
          class="square-icon"
          :style="`backgroundColor: ${item.color}`"
        ></span>
        <span>{{ item.label }}</span>
      </div>
      <base-input
        v-if="item.label !== 'Prolonged'"
        class="tol-config-read-only-value"
        :style="{ width: '84px', height: '26px', backgroundColor: '#c4c4c4' }"
        :readonly="true"
        :value="item.startValue"
      ></base-input>
      <span v-if="item.label === 'Prolonged'" class="start-value-btn">{{
        item.startValue
      }}</span>
      <span class="spacing-value">{{
        item.label !== "Prolonged" ? "to" : ""
      }}</span>

      <div style="position: relative">
        <base-button
          v-if="item.label !== 'Prolonged'"
          type="dropdown"
          :style="{ height: '26px', lineHeight: '14.32px', fontSize: '12px' }"
          @click="handleToggleDropdown(item.key)"
        >
          {{ `${item.endValue}%` }}
        </base-button>
        <base-input
          v-if="item.label === 'Prolonged'"
          class="tol-config-read-only-value"
          :style="{ width: '84px', height: '26px', backgroundColor: '#c4c4c4' }"
          :readonly="true"
          :value="`${item.endValue}%`"
        ></base-input>
        <common-popover
          @close="handleCloseDropdown(item.key)"
          :is-visible="dropdownKey === item.key"
          :style="{ width: '100px', left: '0 !important' }"
        >
          <select-custom
            class="custom-dropdown--assign"
            :style="{ position: 'static', width: '100%' }"
            :items="getDropdownValue[item.key]"
            @close="handleCloseDropdown(item.key)"
            @on-select="(itemSelect) => handleSelectItem(itemSelect, item)"
          >
            >
          </select-custom>
        </common-popover>
      </div>
    </div>
  </div>
</template>

<script>
const createValueDropdown = (start, end, step) => {
  return Array((end - start) / step + 1)
    .fill(0)
    .map((_, index) => ({
      title: `${start + index * step}%`,
      value: start + index * step,
    }));
};

const API_TOL_STP = "/api/asset/tol-utl-stp";
const PROLONGED_INDEX = 3;
module.exports = {
  components: {
    "select-custom": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
  },
  props: {
    resources: Object,
  },
  setup(props) {
    // STATE //
    const config = ref([]);
    const dropdownKey = ref("");
    // COMPUTED //
    const getDropdownValue = computed(() => {
      const startValueMedium =
        config.value.find((e) => e.key === "low").endValue + 5;
      console.log("startValueMedium:::", startValueMedium);
      const startValueHigh =
        config.value.find((e) => e.key === "medium").endValue + 5;
      return {
        low: createValueDropdown(0, 100, 5),
        medium: createValueDropdown(startValueMedium, 100, 5),
        high: createValueDropdown(startValueHigh, 100, 5),
      };
    });
    // METHOD //
    const handleToggleDropdown = (key) => {
      console.log("handleToggleDropdown:::", key);
      console.log("dropdownKey:::before", dropdownKey.value);
      dropdownKey.value = key;
      console.log("dropdownKey:::after", dropdownKey.value);
    };
    const handleCloseDropdown = (key) => {
      dropdownKey.value = "";
    };
    const handleSelectItem = (itemSelect) => {
      console.log("handleSelectItem", itemSelect, config.value);
      const index = config.value.findIndex((e) => e.key === dropdownKey.value);
      if (index === PROLONGED_INDEX - 1) {
        config.value[index].endValue = itemSelect.value;
        config.value[index + 1].endValue = itemSelect.value;
      } else {
        config.value[index].endValue = itemSelect.value;
        config.value[index + 1].startValue = `> ${itemSelect.value}%`;
      }
      handleCloseDropdown();
    };

    const saveData = async () => {
      try {
        await axios.post(API_TOL_STP, {
          low: config.value.find((e) => e.key === "low").endValue,
          medium: config.value.find((e) => e.key === "medium").endValue,
          high: config.value.find((e) => e.key === "high").endValue,
        });
      } catch (error) {
        console.log(error);
      }
    };

    onMounted(async () => {
      const { data } = await axios.get(API_TOL_STP);
      console.log("onMounted:::data", data);
      const dataConfig = [
        {
          label: "Low",
          key: "low",
          startValue: "0%",
          endValue: data.low,
          color: "#41CE77",
        },
        {
          label: "Medium",
          key: "medium",
          startValue: `> ${data.low}%`,
          endValue: data.medium,
          color: "#F7CC57",
        },
        {
          label: "High",
          key: "high",
          startValue: `> ${data.medium}%`,
          endValue: data.high,
          color: "#E34537",
        },
        {
          label: "Prolonged",
          key: "prolonged",
          startValue: "more than",
          endValue: data.high,
          color: "#D56464",
        },
      ];
      config.value = dataConfig;
    });
    return {
      // STATE //
      config,
      dropdownKey,
      // isOpen,
      // COMPUTED //
      getDropdownValue,
      // METHOD //
      handleToggleDropdown,
      handleCloseDropdown,
      handleSelectItem,
      saveData,
    };
  },
};
</script>

<style scoped>
.tol-config-container {
  display: flex;
  flex-direction: column;
  margin-bottom: 100px;
}
.tol-config-item {
  display: flex;
  align-items: center;
  height: 36px;
}
.even-row {
  background-color: #f2f2f2;
}

.tol-config-label {
  display: flex;
  align-items: center;
  width: 120px;
  height: 17px;
}
.square-icon {
  width: 12px;
  height: 12px;
  margin-right: 4px;
  margin-left: 11px;
  display: inline-block;
}
.tol-config-read-only-value > input {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 14.32px !important;
}
.start-value-btn {
  width: 84px;
  display: flex;
  align-items: center;
}
.spacing-value {
  width: 114px;
  justify-content: center;
  display: flex;
  align-items: center;
}
.dropdown {
  /*width: 248% !important;*/
  width: 100% !important;
}
</style>
