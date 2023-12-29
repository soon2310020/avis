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
    <template #body>
      <div
        style="
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column;
          justify-content: center;
        "
      >
        <emdn-pie-chart
          :category="category"
          :pie-chart-set="pieChartSet"
          :chart-set="chartSet"
          :legend-set="legendSet"
          :pie-data-binder="pieDataBinder"
          style-props="width: 100%; height: 206px;"
          :data="chartData"
          :slice-click-event="sliceClickEvent"
          :tooltip-set="tooltipSet"
          id="pie-chart"
        >
        </emdn-pie-chart>
      </div>
    </template>
  </emdn-widget>
</template>
<script>
module.exports = {
  name: "EndOfLifeWidget",
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    label: String,
  },
  data() {
    return {
      category: "displayName",
      pieChartSet: {
        innerRadius: 0,
      },
      pieDataBinder: [
        {
          key: "value1",
          displayName: "",
          sliceSettings: {
            fill: "#F7CC57",
          },
          labelSettings: {
            text: "{value}",
            fill: "#F7CC57",
          },
          value: 0,
        },
        {
          key: "value2",
          displayName: "",
          sliceSettings: {
            fill: "#E34537",
          },
          labelSettings: {
            text: "{value}",
            fill: "#E34537",
          },
          value: 0,
        },
      ],
      legendSet: {
        forceHidden: false,
        paddingTop: 8,
      },
      chartSet: {
        root: {
          container: {
            layout: "verticalLayout",
          },
        },
      },
      tooltipSet: {
        forceHidden: true,
      },
      tooltipInfo:
        "This widget shows the number of toolings that are approaching or have passed their estimated end of life  (taking into consideration the master filter setting).",
    };
  },
  computed: {
    chartData() {
      const pie = {};
      this.pieDataBinder.forEach((item) => {
        pie[item.key] = item.value;
      });

      console.log([pie]);
      return [pie];
    },
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol-eol", { params });
        console.log("response", response);
        this.pieDataBinder.forEach((item, index) => {
          item.displayName = response.data[`title${index + 1}`];
          item.value = response.data[`value${index + 1}`];
        });
      } catch (error) {
        console.log(error);
      }
    },
    sliceClickEvent(event) {
      // IN_COMMUNICATION
      // RESOLVE
      // DISMISS
      if (!event?.target?.dataItem?.dataContext) return;
      const item = event?.target?.dataItem?.dataContext;
      let refurbPriority = "";
      if (item.key === "value1") {
        refurbPriority = "MEDIUM";
      }
      if (item.key === "value2") {
        refurbPriority = "HIGH";
      }
      if (item.key === "value3") {
        refurbPriority = "LOW";
      }
      const url = Common.PAGE_URL.END_OF_LIFE_CYCLE;
      const query = Common.param({
        refurbPriority,
      });
      window.open(`${url}?${query}`, "_blank");
    },
  },
  watch: {
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.tooltipInfo =
            newVal[
              "this_widget_shows_the_number_of_toolings_that_are_approaching__"
            ];
        }
      },
      immediate: true,
    },
  },
  created() {
    this.fetchData();
  },
  mounted() {},
};
</script>
