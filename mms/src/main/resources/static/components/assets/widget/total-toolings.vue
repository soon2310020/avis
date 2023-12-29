<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #tooltip>
      <emdn-tooltip-floating-vue theme="dark">
        <template #tooltip-target>
          <div class="icon-info"><span class="icon"></span></div>
        </template>
        <template #tooltip-content>
          <div style="text-align: left; width: 300px">
            {{ tooltipInfo }}
          </div>
        </template>
      </emdn-tooltip-floating-vue>
    </template>
    <template #body>
      <div
        style="
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column;
          justify-content: center;
          position: relative;
        "
      >
        <div
          class="d-flex justify-content-center align-items-center"
          style="height: 200px"
        >
          <emdn-tooltip-floating-vue>
            <template #tooltip-target>
              <div class="total-count" @mouseover="onHover(TOOLING_TYPE.TOTAL)">
                <span
                  class="count-number"
                  @click="onClick($event, TOOLING_TYPE.TOTAL)"
                >
                  {{ formatNumber(result.total) }}
                </span>
                <span class="count-unit"> {{ resources["toolings"] }} </span>
              </div>
            </template>
            <template #tooltip-content>
              <summary-tooltip
                style="width: 300px"
                :title="tooltipTitle"
                :total="tooltipTotal"
                :unit="[resources['tooling'], resources['toolings']]"
                :col-labels="[resources['status'], resources['no_of_toolings']]"
                :list-items="tooltipData"
              ></summary-tooltip>
            </template>
          </emdn-tooltip-floating-vue>
        </div>

        <div class="block-container">
          <emdn-tooltip-floating-vue>
            <template #tooltip-target>
              <div
                class="block-count card-button primary"
                @mouseover="onHover(TOOLING_TYPE.DIGITAL)"
                @click="onClick($event, TOOLING_TYPE.DIGITAL)"
              >
                <span class="count-number">
                  {{ formatNumber(result.digitalized) }}
                </span>
                <span class="count-unit"> {{ resources["digitalized"] }} </span>
              </div>
            </template>
            <template #tooltip-content>
              <summary-tooltip
                style="width: 300px"
                :title="tooltipTitle"
                :total="tooltipTotal"
                :unit="[resources['tooling'], resources['toolings']]"
                :col-labels="[resources['status'], resources['no_of_toolings']]"
                :list-items="tooltipData"
              ></summary-tooltip>
            </template>
          </emdn-tooltip-floating-vue>

          <emdn-tooltip-floating-vue>
            <template #tooltip-target>
              <div
                class="block-count card-button primary"
                @mouseover="onHover(TOOLING_TYPE.NON_DIGITAL)"
                @click="onClick($event, TOOLING_TYPE.NON_DIGITAL)"
              >
                <span class="count-number">
                  {{ formatNumber(result.nonDigitalized) }}</span
                >
                <span class="count-unit">
                  {{ resources["non_digitalized"] }}
                </span>
              </div>
            </template>
            <template #tooltip-content>
              <summary-tooltip
                style="width: 300px; padding: 10px"
                :title="tooltipTitle"
                :total="tooltipTotal"
                :unit="[resources['tooling'], resources['toolings']]"
                :col-labels="[resources['status'], resources['no_of_toolings']]"
                :list-items="tooltipData"
              ></summary-tooltip>
            </template>
          </emdn-tooltip-floating-vue>
        </div>
      </div>
    </template>
  </emdn-widget>
</template>
<script>
const TOOLING_TYPE = {
  TOTAL: "All",
  DIGITAL: "DIGITAL",
  NON_DIGITAL: "NON_DIGITAL",
};

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
    label: "On Stanbdy",
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
  name: "TotalToolingsWidget",
  props: {
    resources: {
      type: Object,
      default: () => ({}), // return empty object to prevent property access throw error
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
      result: {
        total: 0,
        digitalized: 0,
        nonDigitalized: 0,
        inProduction: 0,
        idle: 0,
        active: 0,
        onStandby: 0,
        sensorOffline: 0,
        sensorDetached: 0,
        noSensor: 0,
      },
      tooltipPosition: "left",
      tooltipInfo: `This widget shows the total tooling number accessible to you, taking into consideration the master filter setting currently in use.`,
      tooltipData: [],
      tooltipDataBase: [...TOOLTIP_BASE],
      tooltipTitle: "Total Tooltip",
      tooltipTotal: 0,
    };
  },
  computed: {},
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol", { params });
        console.log(response.data);
        this.result = Object.assign({}, this.result, {
          total: response.data.total,
          idle: response.data.idle,
          inProduction: response.data.inProduction,
          inactive: response.data.inactive,
          noSensor: response.data.noSensor,
          onStandby: response.data.onStandby,
          digitalized: response.data.digitalized,
          nonDigitalized: response.data.nonDigitalized,
          sensorDetached: response.data.sensorDetached,
          sensorOffline: response.data.sensorOffline,
        });

        this.tooltipDataBase.forEach((item) => {
          if (this.result.hasOwnProperty(item.key)) {
            item.value = this.result[item.key];
          }
        });
      } catch (error) {
        console.log(error);
      }
    },
    onHover(type) {
      this.identifyTooltipPosition();
      if (type === TOOLING_TYPE.TOTAL) {
        this.tooltipData = [...this.tooltipDataBase];
        this.tooltipTitle = this.resources["total_tooling"];
        this.tooltipTotal = this.result.total;
      }
      if (type === TOOLING_TYPE.DIGITAL) {
        this.tooltipData = this.tooltipDataBase.filter(
          (o) => !["noSensor"].includes(o.key)
        );
        this.tooltipTitle = this.resources["digitalized"];
        this.tooltipTotal = this.result.digitalized;
      }
      if (type === TOOLING_TYPE.NON_DIGITAL) {
        this.tooltipData = this.tooltipDataBase.filter((o) =>
          ["noSensor"].includes(o.key)
        );
        this.tooltipTitle = this.resources["non_digitalized"];
        this.tooltipTotal = this.result.nonDigitalized;
      }
    },
    onClick(event, type) {
      // animation
      const el = event.target;
      const cardButton = el.closest(".card-button");
      if (cardButton) {
        cardButton.classList.add("block-count-animation");
        setTimeout(() => {
          cardButton.classList.remove("block-count-animation");
        }, 700);
      }
      // end animation
      const url = Common.PAGE_URL.TOOLING;
      const query = Common.param({ tab: type, from: "WIDGET_TOTAL_TOOLINGS" });
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
          this.tooltipInfo = newVal["this_widget_shows_the_total_tooling__"];
          this.tooltipDataBase = this.tooltipDataBase.map((i) => ({
            ...i,
            label: newVal[i.labelAlt] || i.label,
          }));
        }
      },
      immediate: true,
    },
  },
  created() {
    this.TOOLING_TYPE = TOOLING_TYPE;
    this.fetchData();
  },
};
</script>

<style scoped>
.info-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 15px;
  height: 15px;
}

.info-icon .icon {
  mask-image: url("/images/icon/information.svg");
  -webkit-mask-image: url("/images/icon/information.svg");
  mask-size: contain;
  mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}

.total-count {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.total-count .count-number {
  font-size: 50px;
  font-weight: 700;
  line-height: 57.5px;
  color: #3491ff;
}

.total-count .count-number:hover {
  color: #3585e5;
  cursor: pointer;
}

.block-container {
  display: flex;
  justify-content: space-between;
  gap: 4px;
  padding-left: 4px;
  padding-right: 4px;
  padding-bottom: 6px;
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
}

.block-container > * {
  flex: 1;
}

.block-container > * > * {
  flex: 1;
}

.block-count {
  background-color: #d6dade;
  width: 100%;
  padding: 3px 5px;
  display: flex;
  flex-direction: column;
  font-size: 14.66px;
  line-height: 17.66px;
}

.card-button {
  cursor: pointer;
  transition: 100ms ease;
}

.card-button.primary {
  color: #4b4b4b;
  background-color: #d6dade;
}

.card-button.primary.block-count-animation {
  animation: card-button-click 0.5s;
}

.card-button.primary:hover {
  color: var(--blue-dark);
  background-color: var(--blue-light);
  box-shadow: 0px 1px 3px rgba(0, 0, 0, 0.3);
}

.card-button.danger:hover {
  color: var(--red-dark);
  background-color: var(--red-light);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.block-count .count-number {
  color: inherit;
  font-weight: bold;
}

@keyframes card-button-click {
  0% {
  }

  10% {
    outline: 2px solid var(--blue);
  }

  66% {
    outline: 2px solid var(--blue);
  }

  100% {
  }
}
</style>
