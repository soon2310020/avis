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
          id="pie-chart"
          :category="category"
          :chart-set="chartSet"
          :pie-tick-set="{ forceHidden: true }"
          :legend-set="legendSet"
          :pie-data-binder="pieDataBinder"
          :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
          style-props="width: 100%; height: 206px; margin-top: 20px; margin-bottom: 20px;"
          :data="chartData"
          :slice-click-event="sliceClickEvent"
          :inner-content-click-event="innerContentClickEvent"
        >
        </emdn-pie-chart>
      </div>
    </template>
  </emdn-widget>
</template>

<script>
const RELOCATION_STATUS = {
  APPROVED: "approved",
  PENDING: "pendingApproval",
  DISAPPROVED: "disapproved",
};

const ALERT_TAB = {
  ALERT: "alert",
  HISTORY: "confirmed",
};

const BASE_TOOLTIP = [
  {
    key: RELOCATION_STATUS.APPROVED,
    label: "Approved",
    labelAlt: "approved",
    category: "APPROVED",
    value: 0,
  },
  {
    key: RELOCATION_STATUS.PENDING,
    label: "Pending",
    labelAlt: "pending",
    category: "PENDING",
    value: 0,
  },
  {
    key: RELOCATION_STATUS.DISAPPROVED,
    label: "Disapproved",
    labelAlt: "disapproved",
    category: "DISAPPROVED",
    value: 0,
  },
];

const summaryTooltipStyles = `
flex: 1; 
width: 284px; 
padding: 10px; 
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
color: #4b4b4b;`;

module.exports = {
  name: "RelocationsWidget",
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
      pieDataBinder: [
        {
          key: RELOCATION_STATUS.APPROVED,
          displayName: "Approved",
          displayNameAlt: "approved",
          sliceSettings: { fill: "#41CE77" },
          labelSettings: { fill: "transparent" },
          value: 0,
          detail: "",
        },
        {
          key: RELOCATION_STATUS.PENDING,
          displayName: "Pending",
          displayNameAlt: "pending",
          sliceSettings: { fill: "#C4C4C4" },
          labelSettings: { fill: "transparent" },
          value: 0,
          detail: "",
        },
        {
          key: RELOCATION_STATUS.DISAPPROVED,
          displayName: "Disapproved",
          displayNameAlt: "disapproved",
          sliceSettings: { fill: "#E34537" },
          labelSettings: { fill: "transparent" },
          value: 0,
          detail: "",
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
        innerContent: {
          html: ``,
        },
        useTheme: false,
      },
      tooltipTitle: "",
      tooltipData: [],
      tooltipDataBase: [...BASE_TOOLTIP],
      tooltipInfo:
        "This widget displays the confirmation rate of inter-plant relocations of toolings currently accessible to you on the system.",
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
        const response = await axios.get("/api/asset/wgt-tol-rlo", { params });

        this.pieDataBinder[0].displayName = "Approved";
        this.pieDataBinder[0].value = response.data.value1;
        this.pieDataBinder[0].detail = response.data.detail1;

        this.pieDataBinder[1].displayName = "Pending";
        this.pieDataBinder[1].value = response.data.value2;
        this.pieDataBinder[1].detail = response.data.detail2;

        this.pieDataBinder[2].displayName = "Disapproved";
        this.pieDataBinder[2].value = response.data.value3;
        this.pieDataBinder[2].detail = response.data.detail3;

        this.tooltipDataBase[0].value = response.data.detail1;
        this.tooltipDataBase[1].value = response.data.detail2;
        this.tooltipDataBase[2].value = response.data.detail3;

        this.chartSet.innerContent.html = `
        <div style="display: flex; flex-direction: column; align-items: center;">
          <span style="font-weight: 700; font-size: 50px; color: #3491ff; line-height: 1;">${response.data.value}${response.data.unit}</span>
          <span style="font-size: 16px;">${response.data.title}</span>
        </div>
        `;
      } catch (error) {
        console.log(error);
      }
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      if (target.dataItem.dataContext) {
        const targetItem = target.dataItem.dataContext;
        if (targetItem.key === RELOCATION_STATUS.APPROVED) {
          this.tooltipData = this.tooltipDataBase.filter(
            (item) => item.key === RELOCATION_STATUS.APPROVED
          );
          this.tooltipTitle = this.resources["approved_relocations"];
        }
        if (
          [RELOCATION_STATUS.DISAPPROVED, RELOCATION_STATUS.PENDING].includes(
            targetItem.key
          )
        ) {
          this.tooltipData = this.tooltipDataBase.filter((item) =>
            [RELOCATION_STATUS.DISAPPROVED, RELOCATION_STATUS.PENDING].includes(
              item.key
            )
          );
          this.tooltipTitle = this.resources["pending"];
        }

        const content = [];
        for (let index = 0; index < this.tooltipData.length; index++) {
          const item = this.tooltipData[index];
          let iconStyles =
            "display: inline-block; width: 13px; height: 13px; border-radius: 50%; margin-right: 4px;";
          switch (item.category) {
            case "APPROVED":
              iconStyles += "background-color: #41CE77;";
              break;
            case "PENDING":
              iconStyles += "background-color: #D3D1D9";
              break;
            case "DISAPPROVED":
            default:
              iconStyles += "background-color: #E34537";
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
                ${item.value}
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
    sliceClickEvent(event) {
      if (!event?.target?.dataItem?.dataContext) return;
      let status, moldLocationStatus;
      const item = event.target.dataItem.dataContext;
      if (item.key === RELOCATION_STATUS.APPROVED) {
        status = ALERT_TAB.HISTORY;
        moldLocationStatus = RELOCATION_STATUS.APPROVED;
      }
      if (item.key === RELOCATION_STATUS.DISAPPROVED) {
        status = ALERT_TAB.HISTORY;
        moldLocationStatus = RELOCATION_STATUS.DISAPPROVED;
      }
      if (item.key === RELOCATION_STATUS.PENDING) {
        status = ALERT_TAB.ALERT;
        moldLocationStatus = RELOCATION_STATUS.PENDING;
      }
      const url = Common.PAGE_URL.ASSET_ALERT_RELOCATION;
      const query = Common.param({
        status,
        moldLocationStatus,
      });
      window.open(`${url}?${query}`, "_blank");
    },

    innerContentClickEvent(event) {
      const targetUrl = Common.PAGE_URL.ASSET_ALERT_RELOCATION;
      window.open(targetUrl, "_blank");
    },
  },
  watch: {
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.pieDataBinder = this.pieDataBinder.map((i) => ({
            ...i,
            displayName: newVal[i.displayNameAlt],
          }));

          this.tooltipDataBase = this.tooltipDataBase.map((i) => ({
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
