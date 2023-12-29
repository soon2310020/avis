<template>
  <div class="tol-config-container">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['inactive_tooling']"></h4>
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
          :style="`backgroundColor: var(${item.color})`"
        ></span>
        <span>{{ item.label }}</span>
      </div>

      <span class="tol-config-description">{{ item.description }}</span>
      <!-- 
      <base-input 
        class="tol-config-start-value"
        v-if="item.key !== 'level1'" 
        :style="{width: '82px', height: '26px', backgroundColor: '#c4c4c4'}" 
        :readonly="true" 
        :value="`${config[index - 1].value} Months`"
      ></base-input>
      <span v-if="item.key !== 'level1'" class="spacing-value">and</span> -->

      <div style="position: relative">
        <base-button
          type="dropdown"
          :style="{ height: '26px', lineHeight: '14.32px', fontSize: '12px' }"
          @click="handleToggleDropdown(item.key)"
        >
          {{ `${item.value} Months` }}
        </base-button>
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
      <span v-if="item.key !== 'level3'" class="spacing-value">to</span>
      <base-input
        class="tol-config-start-value"
        v-if="item.key !== 'level3'"
        :style="{ width: '82px', height: '26px', backgroundColor: '#c4c4c4' }"
        :readonly="true"
        :value="`${config[index + 1].value} Months`"
      ></base-input>
    </div>
  </div>
</template>

<script>
const createValueDropdown = (startMonth, endMonth) => {
  return Array(endMonth - startMonth)
    .fill(0)
    .map((_, index) => ({
      title: `${startMonth + index + 1} ${
        startMonth === 0 && index === 0 ? "Month" : "Months"
      }`,
      value: startMonth + index + 1,
    }));
};

const API_TOL_INA_STP = "/api/asset/tol-ina-stp";
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
      console.log("opdownValue");
      return {
        level1: createValueDropdown(0, 12),
        level2: createValueDropdown(12, 24),
        level3: createValueDropdown(24, 36),
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
      config.value[index].value = itemSelect.value;
      handleCloseDropdown();
    };

    const saveData = async () => {
      try {
        await axios.post(API_TOL_INA_STP, {
          level1: config.value.find((e) => e.key === "level1").value,
          level2: config.value.find((e) => e.key === "level2").value,
          level3: config.value.find((e) => e.key === "level3").value,
          timeScale: "MONTH",
        });
      } catch (error) {
        console.log(error);
      }
    };

    onMounted(async () => {
      const { data } = await axios.get(API_TOL_INA_STP);
      const dataConfig = [
        {
          label: "Level 1",
          key: "level1",
          description: "inactive for more than",
          value: data.level1,
          color: "--green-graph-light-1",
        },
        {
          label: "Level 2",
          key: "level2",
          description: "inactive for more than",
          value: data.level2,
          color: "--blue-graph-medium-1",
        },
        {
          label: "Level 3",
          key: "level3",
          description: "inactive for more than",
          value: data.level3,
          color: "--blue-graph-dark-1",
        },
      ];
      config.value = dataConfig;
    });
    return {
      // STATE //
      config,
      dropdownKey,
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
  gap: 50px;
}
.even-row {
  background-color: #f2f2f2;
}

.tol-config-label {
  display: flex;
  align-items: center;
  width: 110px;
  height: 17px;
}
.square-icon {
  width: 12px;
  height: 12px;
  margin-right: 4px;
  margin-left: 11px;
  display: inline-block;
}
.tol-config-description {
  width: 178px;
  height: 17px;
}
.tol-config-start-value > input {
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
  width: max-content;
  justify-content: center;
  display: flex;
  align-items: center;
}
.dropdown {
  width: 100% !important;
}
</style>
