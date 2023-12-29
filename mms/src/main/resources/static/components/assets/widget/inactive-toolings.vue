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
          style-props="width: 100%; height: 206px; margin-top: 20px; margin-bottom: 20px;"
          :data="results"
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
  name: "InactiveAssets",
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
          displayName: "> 12 months",
          sliceSettings: {
            fill: "#BCE2C7",
          },
          labelSettings: {
            text: "{value}",
            fill: "#4b4b4b",
          },
          value: 0,
        },
        {
          key: "value2",
          displayName: "> 24 months",
          sliceSettings: {
            fill: "#4EBCD5",
          },
          labelSettings: {
            text: "{value}",
            fill: "#4EBCD5",
          },
          value: 0,
        },
        {
          key: "value3",
          displayName: "> 36 months",
          sliceSettings: {
            fill: "#1A2281",
          },
          labelSettings: {
            text: "{value}",
            fill: "#1A2281",
          },
          value: 0,
        },
      ],
      legendSet: {
        forceHidden: false,
        paddingTop: 8,
      },
      tooltipSet: {
        forceHidden: true,
      },
      chartSet: {
        root: {
          container: {
            layout: "verticalLayout",
          },
        },
      },
      tooltipInfo: `This widget shows the number of toolings that have been inactive for different periods of time, categorized by the number of months  (taking into consideration the master filter setting).`,
    };
  },
  computed: {
    results() {
      console.log("compute results");
      const pie = {};
      this.pieDataBinder.forEach((item) => {
        pie[item.key] = item.value;
      });
      return [pie];
    },
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/asset/wgt-tol-ina", { params });
        console.log("response.data", response.data);
        this.pieDataBinder.forEach((item, index) => {
          console.log("----------------");
          item.displayName = response.data[`title${index + 1}`];
          item.value = response.data[`value${index + 1}`]; // open this line to integrate
        });
        console.log("this.pieDataBinder", this.pieDataBinder);
      } catch (error) {
        console.log(error);
      }
    },
    onRedirect() {
      // TODO: RECHECK WHEN APPLY NEW REDIRECTION SOLUTION
      // location.href = '/admin/configuration?tab=productionTab'
      window.open("/admin/configuration?tab=productionTab", "_blank");
    },
    sliceClickEvent(event) {
      if (!event?.target?.dataItem?.dataContext) {
        return;
      }
      let inactiveLevel;
      const item = event.target.dataItem.dataContext;
      if (item.key === "value1") {
        inactiveLevel = "FIRST_LEVEL";
      }
      if (item.key === "value2") {
        inactiveLevel = "SECOND_LEVEL";
      }
      if (item.key === "value3") {
        inactiveLevel = "THIRD_LEVEL";
      }
      const url = Common.PAGE_URL.TOOLING;
      const query = Common.param({
        inactiveLevel,
        from: "WIDGET_INACTIVE_TOOLING",
      });
      const targetUrl = `${url}?${query}`;
      window.open(targetUrl, "_blank");
    },
  },
  watch: {
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.tooltipInfo =
            newVal["this_widget_shows_the_number_of_toolings_that_have__"];
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

<style scoped></style>
