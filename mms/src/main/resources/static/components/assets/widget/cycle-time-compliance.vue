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
          :pie-set="pieSet"
          :pie-tick-set="{ forceHidden: true }"
          :chart-set="chartSet"
          :legend-set="legendSet"
          :pie-data-binder="pieDataBinder"
          :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
          style-props="width: 100%; height: 206px;"
          :slice-click-event="sliceClickEvent"
          :inner-content-click-event="innerContentClickEvent"
          :data="chartData"
          id="pie-chart"
        >
        </emdn-pie-chart>
      </div>
    </template>
  </emdn-widget>
</template>

<script>
const CYCLE_TIME_TYPE = {
  WITHIN: "within",
  ABOVE: "above",
  BELOW: "below",
};

const BASE_TOOLTIP = [
  {
    key: CYCLE_TIME_TYPE.WITHIN,
    label: "Within",
    category: CYCLE_TIME_TYPE.WITHIN,
    value: 0,
  },
  {
    key: CYCLE_TIME_TYPE.ABOVE,
    label: "Above",
    category: CYCLE_TIME_TYPE.ABOVE,
    value: 0,
  },
  {
    key: CYCLE_TIME_TYPE.BELOW,
    label: "Below",
    category: CYCLE_TIME_TYPE.BELOW,
    value: 0,
  },
];

const summaryTooltipStyles = `
flex: 1;
width: 284px;
padding: 1rem 1rem 0.5rem 1rem;
background: #fff;
width: 317.5px;
z-index: 1000;
position: fixed;
box-shadow: 0 0 3px rgba(0,0,0,.3);
border-radius: 0.25rem;
`;

const summaryContentItemStyles = `
font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif;
display: flex;
justify-content: space-between;
align-items: center;
margin-bottom: 10px;
margin-top: 10px;
font-size: 14.66px;
line-height: 17px;
color: #4b4b4b;
`;

module.exports = {
  name: "CycleTimeComplianceWidget",
  props: {
    label: String,
  },
  data() {
    return {
      category: "displayName",
      pieSet: {},
      pieDataBinder: [
        {
          key: "within",
          displayName: "Within",
          sliceSettings: { fill: "#41CE77" },
          labelSettings: { fill: "transparent" },
          value: 10,
        },
        {
          key: "above",
          displayName: "Above",
          sliceSettings: { fill: "#FBB5B5" },
          labelSettings: { fill: "transparent" },
          value: 10,
        },
        {
          key: "below",
          displayName: "Below",
          sliceSettings: { fill: "#E34537" },
          labelSettings: { fill: "transparent" },
          value: 10,
        },
      ],
      legendSet: {
        forceHidden: false,
        paddingTop: 8,
      },
      chartSet: {
        useTheme: false,
        root: {
          container: {
            layout: "verticalLayout",
          },
        },
        innerContent: {
          html: ``,
        },
        useTheme: false,
      },
      compliance: {
        title: "Compliance",
        value: 0,
        unit: "",
      },
      tooltipTitle: "Cycle Time Compliance",
      tooltipData: [...BASE_TOOLTIP],
      tooltipInfo:
        "This widget shows the average cycle time compliance rate for all toolings accessible to you on the system (taking into consideration the master filter setting).",
    };
  },
  computed: {
    chartData() {
      const pie = {};
      this.pieDataBinder.forEach((item) => {
        pie[item.key] = item.value;
      });
      console.log("chartData: ", [pie]);
      return [pie];
    },
  },
  methods: {
    async fetchData() {
      const params = { filterCode: "COMMON" };
      try {
        const response = await axios.get("/api/analysis/wgt-cyc-tim-cpl", {
          params,
        });
        this.pieDataBinder[0].displayName = response.data.title1;
        this.pieDataBinder[0].value = response.data.value1;
        this.pieDataBinder[1].displayName = response.data.title2;
        this.pieDataBinder[1].value = response.data.value2;
        this.pieDataBinder[2].displayName = response.data.title3;
        this.pieDataBinder[2].value = response.data.value3;

        this.tooltipData[0].value = response.data.value1;
        this.tooltipData[1].value = response.data.value2;
        this.tooltipData[2].value = response.data.value3;

        this.compliance.title = response.data.title;
        this.compliance.unit = response.data.unit;
        this.compliance.value = response.data.value;

        const page = "/common/dat-fam?";
        const query = Common.param({
          mainTab: "MOLD",
          tabType: "DIGITAL",
        });
        const url = page + query;

        this.chartSet.innerContent.html = `
        <div style="display: flex; flex-direction: column; align-items: center;">
          <span style="font-weight: 700; font-size: 50px; color: #3491ff; line-height: 1;">${this.compliance.value}${this.compliance.unit}</span>
          <span style="font-size: 16px;">${this.compliance.title}</span>
        </div>
        `;
      } catch (error) {
        console.log(error);
      }
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      if (target.dataItem.dataContext) {
        const content = [];
        for (let index = 0; index < this.tooltipData.length; index++) {
          const item = this.tooltipData[index];
          let iconStyles =
            "display: inline-block; width: 13px; height: 13px; border-radius: 50%; margin-right: 4px;";
          switch (item.category) {
            case CYCLE_TIME_TYPE.ABOVE:
              iconStyles += "background-color: #F5C7C7;";
              break;
            case CYCLE_TIME_TYPE.BELOW:
              iconStyles += "background-color: #E34537";
              break;
            case CYCLE_TIME_TYPE.WITHIN:
            default:
              iconStyles += "background-color: #41CE77";
              break;
          }
          content.push(`
          <div class="summary-content--item" style="${summaryContentItemStyles}">
              <div class="summary-content--item__label" style="display: inline-flex; align-items: center;">
                <span class="label-icon" style="${iconStyles}"></span>
                <span class="label-text">
                  ${item.category
                    .split("_")
                    .map((i) => i.toLowerCase())
                    .join(" ")
                    .capitalizeAll()}
                </span>
              </div>
              <div class="summary-content--item__value">
                ${item.value}%
              </div>
          </div>
        `);
        }

        return `
        <div class="summary-tooltip" style="${summaryTooltipStyles}">
          <div class="summary-head" style="display: flex; justify-content: space-between;">
            <div class="summary-title" style="font-weight: 700; font-size: 14.66px; line-height: 18px;">
              ${this.tooltipTitle}
            </div>
          </div>
          <div class="divider" style="border-bottom: 1px solid #000; margin: 10px 0; opacity: 0.2;"></div>
          <div class="summary-content">
            ${content.join(" ")}
          </div>
        </div>
        `;
      }
      return tooltipHTML;
    },
    sliceClickEvent(event, instance) {
      const url = Common.PAGE_URL.TOOLING;
      const query = Common.param({
        tab: "DIGITAL",
        from: "WIDGET_CYCLE_TIME_COMPLIANCE",
      });
      if (event?.target?.dataItem?.dataContext) {
        window.open(`${url}?${query}`, "_blank");
      }
    },
    innerContentClickEvent(event) {
      const url = Common.PAGE_URL.TOOLING;
      const query = Common.param({
        tab: "DIGITAL",
        from: "WIDGET_CYCLE_TIME_COMPLIANCE",
      });
      window.open(`${url}?${query}`, "_blank");
    },
  },
  created() {
    this.fetchData();
  },
};
</script>
