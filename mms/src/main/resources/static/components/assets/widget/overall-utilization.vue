<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #tooltip>
      <emdn-tooltip-floating-vue theme="dark">
        <template #tooltip-target>
          <div class="icon-info"><span class="icon"></span></div>
        </template>
        <template #tooltip-content>
          <div style="width: 300px; text-align: left">{{ tooltipInfo }}</div>
        </template>
      </emdn-tooltip-floating-vue>
    </template>
    <!-- <template #header>
      <a
        @click="onRedirect"
        href="javascript:void(0)"
        class="btn-custom customize-button"
      >
        <img
          style="margin-bottom: 3px"
          width="16"
          height="16"
          src="/images/icon/settings.svg"
        />
      </a>
    </template> -->

    <!-- MAIN -->
    <template #body>
      <div class="content-container">
        <div v-for="item in listData">
          <div class="progress-item" @mouseover="onHover(item)">
            <div class="progress-item__label">
              <span>{{ item.label }}</span>
              <span>({{ item.percentLabel }})</span>
            </div>
            <emdn-tooltip-floating-vue style="width: 100%">
              <template #tooltip-target>
                <div @click="onClick(item)" style="cursor: pointer">
                  <emdn-progress-bar
                    :style-props="'height: 31px'"
                    :value="item.rate"
                    :buffer-value="100"
                    :bg-color="item.backgroundColor"
                    :border-color="item.borderColor"
                  >
                    <template #value>
                      {{ item.value }} ({{ item.rate }}%)
                    </template>
                  </emdn-progress-bar>
                </div>
              </template>
              <template #tooltip-content>
                <summary-tooltip
                  style="width: 300px"
                  :title="tooltipTitle"
                  :unit="['tooling', 'toolings']"
                  :col-labels="[
                    resources['status'],
                    resources['no_of_toolings'],
                  ]"
                  :list-items="
                    tooltipData.map((i) => ({
                      ...i,
                      value: formatNumber(i.value),
                    }))
                  "
                ></summary-tooltip>
              </template>
            </emdn-tooltip-floating-vue>
          </div>
        </div>
        <div class="graph-title">
          {{ resources["number_of_toolings"] }}
        </div>
      </div>
    </template>
    <!-- END MAIN -->
  </emdn-widget>
</template>

<script>
const TOOLTIP_BASE = [
  {
    key: "inProduction",
    label: "In Production",
    labelAlt: "in_production",
    category: "IN_PRODUCTION",
    value: 0,
  },
  {
    key: "idle",
    label: "Idle",
    labelAlt: "idle",
    category: "IDLE",
    value: 0,
  },
  {
    key: "inactive",
    label: "Inactive",
    labelAlt: "inactive",
    category: "INACTIVE",
    value: 0,
  },
  {
    key: "onStandby",
    label: "On Standby",
    labelAlt: "on_standby",
    category: "ON_STANDBY",
    value: 0,
  },
  {
    key: "sensorOffline",
    label: "Sensor Offline",
    labelAlt: "sensor_offline",
    category: "SENSOR_OFFLINE",
    value: 0,
  },
  {
    key: "sensorDetached",
    label: "Sensor Detached",
    label: "sensor_detached",
    category: "SENSOR_DETACHED",
    value: 0,
  },
  {
    key: "noSensor",
    label: "No Sensor",
    labelAlt: "no_sensor",
    category: "NO_SENSOR",
    value: 0,
  },
];
module.exports = {
  name: "OveralllUtilizationRateWidget",
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    label: String,
  },
  components: {
    "summary-tooltip": httpVueLoader(
      "/components/assets/widget/tooltip/summary-tooltip.vue"
    ),
  },
  data() {
    return {
      tooltipInfo: `This widget shows the distribution of toolings across different utilization rate categories, based on the current master filter setting. The utilization rate categories are determined by a system configuration setting.`,
      listData: [
        {
          key: "low",
          label: "Low TEST",
          labelAlt: "low",
          backgroundColor: "#41CE77",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          rate: 0,
          statusSummary: {},
          percentLabel: `0-40%`,
          totalValue: 0,
        },
        {
          key: "medium",
          label: "Medium TEST",
          labelAlt: "medium",
          backgroundColor: "#F7CC57",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          rate: 0,
          statusSummary: {},
          percentLabel: `40-80%`,
          totalValue: 0,
        },
        {
          key: "high",
          label: "High TEST",
          labelAlt: "high",
          backgroundColor: "#E34537",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          rate: 0,
          statusSummary: {},
          percentLabel: `80-100%`,
          totalValue: 0,
        },
        {
          key: "prolonged",
          label: "Prolonged TEST",
          labelAlt: "prolonged",
          backgroundColor: "#7A2E17",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          rate: 0,
          statusSummary: {},
          percentLabel: `>100%`,
          totalValue: 0,
        },
      ],
      tooltipTitle: "Overall Utilization",
      tooltipActive: "",
      tooltipPosition: "left",
      tooltipData: [...TOOLTIP_BASE],
    };
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol-utl", { params });
        console.log("response.data", response.data);
        if (response.data) {
          this.listData.forEach((item) => {
            if (Object.keys(response.data).some((key) => key === item.key)) {
              item.value = response.data[item.key];
              item.totalValue = response.data.total;
              item.rate = response.data[`${item.key}Rate`]; // example: highRate
              item.percentLabel = response.data[`${item.key}Std`]; // example: highStd
              item.statusSummary = response.data[`${item.key}StatusSummary`]; // example: highStatusSummary
            }
          });
        }
      } catch (error) {
        console.log(error);
      }
    },
    onHover(item) {
      this.identifyTooltipPosition();
      if (this.tooltipActive === item.key) return; // prevent unnecessary update
      this.tooltipActive = item.key;
      this.tooltipData.forEach((tooltipItem) => {
        if (item.statusSummary.hasOwnProperty(tooltipItem.key)) {
          tooltipItem.value = item.statusSummary[tooltipItem.key];
        }
      });
    },
    onRedirect() {
      window.open("/admin/configuration?tab=productionTab", "_blank");
    },
    onClick(item) {
      const url = Common.PAGE_URL.TOOLING;
      const query = Common.param({
        utilizationStatus: item.key.toUpperCase(),
        from: "WIDGET_OVERALL_UTILIZATION",
      });
      window.open(`${url}?${query}`, "_blank");
    },
    identifyTooltipPosition() {
      const rect = this.$el.getBoundingClientRect();
      const elementX = rect.left;
      const elementY = rect.top;
      const centerX = window.innerWidth / 2;
      const centerY = window.innerHeight / 2;

      if (elementX < centerX && elementY < centerY) {
        console.log("top-left");
        this.tooltipPosition = "right"; // top-left
      } else if (elementX > centerX && elementY < centerY) {
        console.log("top-right");
        this.tooltipPosition = "left"; // top-right
      } else if (elementX < centerX && elementY > centerY) {
        console.log("bottom-left");
        this.tooltipPosition = "right"; // bottom-left
      } else if (elementX > centerX && elementY > centerY) {
        console.log("bottom-right");
        this.tooltipPosition = "left"; // bottom-right
      }
    },
  },
  watch: {
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.tooltipInfo = newVal["this_widget_shows_the_distribution__"];
          this.tooltipTitle = newVal["overall_utilization"];
          this.tooltipData = TOOLTIP_BASE.map((i) => ({
            ...i,
            label: newVal[i.labelAlt],
          }));
          this.listData = this.listData.map((i) => ({
            ...i,
            label: newVal[i.labelAlt],
          }));
        }
      },
      immediate: true,
    },
  },
  created() {
    this.fetchData();
  },
};
</script>

<style scoped>
.content-container {
  padding-left: 20px;
  padding-right: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.5rem;
}

.progress-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-item__label {
  width: 120px;
  font-size: 11.25px;
}

.graph-title {
  text-align: center;
  font-size: 11.25px;
}

.tooltip-anchor {
  flex: 1;
}

.tooltip-anchor > div:nth-child(1) {
  width: 100%;
}
</style>
