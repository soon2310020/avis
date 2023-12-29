<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #tooltip>
      <emdn-tooltip-floating-vue theme="dark">
        <template #tooltip-target>
          <div class="icon-info"><span class="icon"></span></div>
        </template>
        <template #tooltip-content>
          <div style="width: 300px" class="tooltip-custom">
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
          <div class="total-count" @mouseover="onHover('TOTAL')">
            <span class="count-number" @click="onClick($event, 'TOTAL')">
              {{
                result.currencySymbol +
                $formatter.numberShorten(result.totalCost, 2)
              }}
            </span>
            <!-- <span class="count-unit">{{ result.currencyCode }}</span> -->
          </div>
        </div>

        <div class="block-container">
          <div
            class="block-count card-button primary"
            @mouseover="onHover('ACQUISITION_COST')"
            @click="onClick($event, 'ACQUISITION_COST')"
          >
            <span class="count-number card-button">
              {{ result.currencySymbol }}
              {{ $formatter.numberShorten(result.acquisitionCost, 2) }}
            </span>

            <span class="count-unit"> {{ resources["acquisition_cost"] }}</span>
          </div>
          <div
            class="block-count card-button primary"
            @mouseover="onHover('MAINTENANCE_COST')"
            @click="onClick($event, 'MAINTENANCE_COST')"
          >
            <span class="count-number">
              {{ result.currencySymbol }}
              {{ $formatter.numberShorten(result.maintenanceCost, 2) }}
            </span>
            <span class="count-unit">
              {{ resources["maintenance_cost"] }}
            </span>
          </div>
        </div>
      </div>
    </template>
  </emdn-widget>
</template>

<script>
module.exports = {
  name: "TotalCostOfOwnerShip",
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    label: String,
  },
  data() {
    return {
      tooltipInfo:
        "This widget shows the Total Cost of Ownership (TCO) for toolings accessible to you, taking into consideration the master filter setting currently in use. \n\n TCO includes acquisition cost, and maintenance costs logged through work orders for both corrective & preventive maintenance.",
      tooltipPosition: "left",
      tooltipTitle: "",
      tooltipData: [],
      result: {
        totalCost: 0,
        acquisitionCost: 0,
        maintenanceCost: 0,
        currencyCode: "",
        currencySymbol: "",
      },
    };
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol-tco", { params });
        console.log("response", response);
        if (!response.data) throw new Error("No data");
        this.result = Object.assign({}, this.result, {
          totalCost: response.data.tco,
          acquisitionCost: response.data.acquisitionCost,
          maintenanceCost: response.data.maintenanceCost,
          currencyCode: response.data.currencyCode,
          currencySymbol: response.data.currencySymbol,
        });
      } catch (error) {
        console.log(error);
      }
    },
    onHover(type) {
      console.log("type", type);
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
      const query = Common.param({
        from: "WIDGET_TOTAL_COST_OWNER_SHIP",
      });
      window.open(`${url}?${query}`, "_blank");
    },
  },
  watch: {
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.tooltipInfo =
            newVal["this_widget_shows_the_total_cost_of_ownership__"];
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
.tooltip-custom {
  text-align: left;
  white-space: pre-wrap;
}

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
