<template>
  <emdn-widget
    class="operation-summary-widget"
    :header-text="label"
    :info-text="label"
  >
    <template #tooltip>
      <emdn-tooltip-floating-vue theme="dark">
        <template #tooltip-target>
          <div class="icon-info"><span class="icon"></span></div>
        </template>
        <template #tooltip-content>
          <div style="width: 300px" class="tooltip-info">{{ tooltipInfo }}</div>
        </template>
      </emdn-tooltip-floating-vue>
    </template>
    <template #body>
      <div class="content-container">
        <div v-for="item in listData">
          <div class="progress-item" @mouseover="onHover(item)">
            <div class="progress-item__label">{{ item.label }}</div>
            <emdn-tooltip-floating-vue style="width: 100%">
              <template #tooltip-target>
                <div @click="onClick(item)" class="bar-wrap">
                  <emdn-progress-bar
                    :style-props="'height: 31px; width: 100%;'"
                    :value="item.value"
                    :buffer-value="item.totalValue"
                    :bg-color="item.backgroundColor"
                    :border-color="item.borderColor"
                  >
                    <template #value>{{ item.value }}</template>
                  </emdn-progress-bar>
                </div>
              </template>
              <template #tooltip-content>
                <summary-tooltip
                  style="width: 300px"
                  :title="tooltipTitle"
                  :total="tooltipTotal"
                  :unit="['tooling', 'toolings']"
                  :col-labels="[
                    resources?.['status'],
                    resources?.['no_of_toolings'],
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
          {{ resources?.["number_of_toolings"] }}
        </div>
      </div>
    </template>
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
    labelAlt: "sensor_detached",
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
  name: "OperationalSummaryWidget",
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
      listData: [
        {
          key: "inOperation",
          label: "In Operation Test",
          labelAlt: "in_operation",
          backgroundColor: "#41CE77",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          totalValue: 0,
        },
        {
          key: "outOfOperation",
          label: "Out of Operation Test",
          labelAlt: "out_of_operation",
          backgroundColor: "#E34537",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          totalValue: 0,
        },
        {
          key: "unknown",
          label: "Unknown Test",
          labelAlt: "unknown",
          backgroundColor: "#8B8B8B",
          color: "#4b4b4b",
          borderColor: "transparent",
          value: 0,
          totalValue: 0,
        },
      ],
      tooltipInfo: `This widget displays the current status of the toolings accessible to you on the system (taking into consideration the master filter setting).\n Operational Summary is grouped into three categories: In Operation, Out of Operation, and Unknown.`,
      tooltipActive: "",
      tooltipTitle: "In Operation",
      tooltipTotal: 0,
      tooltipPosition: "right",
      tooltipDataBase: [...TOOLTIP_BASE],
      tooltipData: [...TOOLTIP_BASE],
    };
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol-opr", { params });
        // integrate
        console.log("response", response);

        if (response.data) {
          this.listData.forEach((item) => {
            if (response.data.hasOwnProperty(item.key)) {
              item.value = response.data[item.key];
              item.totalValue = response.data.total;
            }
          });

          this.tooltipDataBase.forEach((item) => {
            if (response.data.hasOwnProperty(item.key)) {
              item.value = response.data[item.key];
            }
          });
        }
      } catch (error) {
        console.log(error);
      }
    },
    onHover(item) {
      this.tooltipActive = item.key;
      this.identifyTooltipPosition();
    },
    onClick(item) {
      const url = Common.PAGE_URL.TOOLING;
      let moldStatusList = [];
      if (item.key === "inOperation") {
        moldStatusList = ["IN_PRODUCTION", "IDLE"];
      } else if (item.key === "outOfOperation") {
        moldStatusList = ["INACTIVE", "ON_STANDBY"];
      } else if (item.key === "unknown") {
        moldStatusList = ["SENSOR_OFFLINE", "SENSOR_DETACHED", "NO_SENSOR"];
      }
      const query = Common.param({
        moldStatusList,
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
    tooltipActive(newVal) {
      if (newVal === "inOperation") {
        this.tooltipTitle = this.resources["in_operation"];
        this.tooltipTotal = this.listData[0].value;
        this.tooltipData = this.tooltipDataBase.filter((o) =>
          ["inProduction", "idle"].includes(o.key)
        );
      }
      if (newVal === "outOfOperation") {
        this.tooltipTitle = this.resources["out_of_operation"];
        this.tooltipTotal = this.listData[1].value;
        this.tooltipData = this.tooltipDataBase.filter((o) =>
          ["inactive", "onStandby"].includes(o.key)
        );
      }
      if (newVal === "unknown") {
        this.tooltipTitle = this.resources["unknown"];
        this.tooltipTotal = this.listData[2].value;
        this.tooltipData = this.tooltipDataBase.filter((o) =>
          ["sensorOffline", "sensorDetached", "noSensor"].includes(o.key)
        );
      }
    },
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.tooltipInfo =
            newVal["this_widget_displays_the_current_status_of_toolings__"];
          this.tooltipDataBase = this.tooltipDataBase.map((i) => ({
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

.tooltip-info {
  text-align: left;
  white-space: pre-line;
}

.bar-wrap:hover {
  cursor: pointer;
}
</style>
