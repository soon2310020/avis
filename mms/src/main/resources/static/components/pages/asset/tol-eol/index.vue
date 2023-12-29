<template>
  <div class="tol-eol-main-container">
    <div class="tol-eol-header-container">
      <master-filter-wrapper
        :filter-code="filterCode"
        :notify-filter-updated="() => fetchData(null, 'master-filter')"
      ></master-filter-wrapper>
    </div>
    <div class="tol-eol-body-container">
      <div
        class="tol-eol-chart-wrapper"
        :style="{
          padding: chartData.length ? '0 40px 40px 40px' : '0 0 40px 0',
        }"
      >
        <emdn-xy-chart
          style-props="width: 100%; height: 100%; min-width: 600px;"
          :category="chartCategory"
          :root-number-formatter-set="rootNumberFormatterSet"
          :data="chartData"
          :bar-data-binder="barDataBinder"
          :chart-set="chartSet"
          :x-axis-grid-set="xAxisGridSet"
          :x-axis-extra-label-set="xAxisExtraLabelSet"
          :y-axis-extra-label-set="yAxisExtraLabelSet"
          :column-template-set="columnTemplateSet"
          :legend-set="legendSet"
          :chart-item-click-handler="openDetailModal"
          :x-axis-label-text-adapter="xAxisLabelTextAdapter"
          :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
        >
        </emdn-xy-chart>
      </div>
      <div class="tol-eol-table-wrapper">
        <emdn-data-table
          :scrollable="true"
          :style-props="{ paddingBottom: '1px' }"
        >
          <template #colgroup>
            <col
              v-for="item in tableHeaderWidthArray"
              :style="{ width: item }"
            />
          </template>
          <template #thead>
            <tr ref="tableHeaderRef">
              <th
                v-for="header in tableHeaders"
                style="z-index: 20; padding: 8px 4px 8px 16px"
              >
                <div class="tol-eol-table-th">
                  <span>{{ header.title }}</span>
                  <span @click="sortTable(header.key)">
                    <emdn-icon
                      :button-type="
                        pagination.sort === `${header.key},asc`
                          ? 'sort-asce'
                          : 'sort-desc'
                      "
                      :active="pagination.sort?.includes(header.key)"
                    ></emdn-icon>
                  </span>
                </div>
              </th>
            </tr>
          </template>
          <template #tbody>
            <div
              v-if="!tableData.length"
              class="tol-eol-table-empty"
              :style="{ height: `calc(100% - ${getTableHeaderHeight()}px)` }"
            >
              No Data Available
            </div>
            <template v-else>
              <tr v-for="(data, index) in tableData">
                <td>
                  <div
                    @mouseover.stop="(e) => setTooltip(e, data?.moldCode)"
                    @mouseleave.stop="initializeTableTooltip"
                    class="tol-eol-truncate-text"
                  >
                    {{ data?.moldCode }}
                  </div>
                </td>
                <td>
                  <div style="display: flex">
                    <div style="min-width: 0">
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, data.parts[0]?.name)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="tol-eol-truncate-text"
                      >
                        {{ data.parts[0]?.name }}
                      </div>
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, data.parts[0]?.id)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="tol-eol-truncate-text"
                        style="font-size: 11.25px"
                      >
                        {{ data.parts[0]?.id }}
                      </div>
                    </div>
                    <div style="flex-shrink: 0">
                      <emdn-dropdown-root
                        v-if="data.parts.length > 1"
                        :on-dropdown-close="onBadgeDropdownClose"
                        position="auto"
                        max-height="200px"
                      >
                        <emdn-dropdown-trigger>
                          <div
                            class="tol-eol-badge-wrapper"
                            @click="showBadgeDropdown(data.parts[0]?.id, index)"
                          >
                            <span
                              class="tol-eol-badge-arrow-icon"
                              :class="
                                visibleBadgeDropdownId === data.parts[0]?.id &&
                                visibleBadgeDropdownIndex === index
                                  ? 'active'
                                  : ''
                              "
                            >
                              <img
                                src="/images/more.png"
                                class="tol-eol-show-more-icon"
                              />
                            </span>
                            <span class="tol-eol-badge-styles">
                              +{{ data.parts.length - 1 }}
                            </span>
                          </div>
                        </emdn-dropdown-trigger>
                        <emdn-dropdown-portal>
                          <emdn-dropdown-content>
                            <emdn-dropdown-item
                              v-for="(part, index) in data.parts"
                              v-if="index !== 0"
                              :key="index"
                              :text="part.name"
                            ></emdn-dropdown-item>
                          </emdn-dropdown-content>
                        </emdn-dropdown-portal>
                      </emdn-dropdown-root>
                    </div>
                  </div>
                </td>
                <td>
                  <div style="display: flex; flex-direction: column">
                    <div class="tol-eol-shot-count-ratio">
                      <div>
                        {{
                          data.accumShotCount
                            ? data.accumShotCount.toLocaleString()
                            : 0
                        }}
                      </div>
                      <div>&nbsp;&#47;&nbsp;</div>
                      <div>
                        {{
                          data.designedShotCount
                            ? data.designedShotCount.toLocaleString()
                            : 0
                        }}
                      </div>
                    </div>
                    <div class="tol-eol-utilization-rate-bar">
                      <div>{{ data.utilizationRate ?? 0 }}&#37;</div>
                      <div>
                        <emdn-progress-bar
                          style-props="height: 4px"
                          :value="
                            getUtilizationRateForProgressBar(
                              data.utilizationRate
                            )
                          "
                          :buffer-value="100"
                          :bg-color="
                            getUtilizationStatusColor(data.utilizationStatus)
                          "
                        ></emdn-progress-bar>
                      </div>
                    </div>
                  </div>
                </td>
                <td>
                  <span>{{ getInsightsStatus(data.eolType) }}</span>
                  <span>{{
                    data.eolType === "UNPREDICTABLE"
                      ? "cycle after 10 years"
                      : `cycle on ${data.eolDate}`
                  }}</span>
                </td>
                <td>
                  <emdn-table-status
                    :category="data?.refurbPriority"
                  ></emdn-table-status>
                </td>
                <td>
                  <emdn-table-status
                    :category="data?.toolingStatus"
                  ></emdn-table-status>
                </td>
              </tr>
            </template>
          </template>
        </emdn-data-table>
      </div>
      <div class="tol-eol-pagination-wrapper">
        <emdn-pagination
          :current-page="pagination.page"
          :total-page="totalPages"
          :next-click-handler="clickNextPageButton"
          :previous-click-handler="clickPrevPageButton"
        >
        </emdn-pagination>
      </div>
    </div>

    <div
      v-if="isOpenedTableTooltip"
      :style="tableTooltipPosition"
      class="tol-eol-table-tooltip-wrapper"
      ref="tooltipRef"
    >
      {{ tableTooltipInfo.text }}
    </div>

    <life-cycle-detail-popup
      v-if="isOpendDetailModal"
      :is-opened="isOpendDetailModal"
      :close-handler="closeDetailModal"
      :time-settings="timeSettings"
    >
    </life-cycle-detail-popup>
  </div>
</template>

<script>
module.exports = {
  name: "tol-eol-page",
  components: {
    "life-cycle-detail-popup": httpVueLoader(
      "/components/pages/asset/tol-eol/life-cycle-detail-popup.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      isSortTableByDesc: false,

      filterCode: "COMMON",
      pagination: {
        page: 1,
        size: 20,
        sort: null,
      },
      totalPages: 1,
      timeSettings: {
        timeScale: "HALF",
        timeValue: null,
      },
      requestParam: {},

      isOpendDetailModal: false,

      chartData: [],
      rootNumberFormatterSet: {
        numberFormat: "#,###.",
      },
      chartCategory: "title",
      barDataBinder: [
        {
          key: "eolMoldCount",
          displayName: "",
          color: "#4EBCD5",
        },
      ],
      chartSet: {
        valueAxis: {
          min: 0,
        },
      },
      xAxisGridSet: {
        strokeOpacity: 0,
      },
      xAxisExtraLabelSet: {
        text: "Within 5 Years",
      },
      yAxisExtraLabelSet: {
        text: "Number of Toolings at End of Life",
      },
      columnTemplateSet: {
        width: 40,
        fillOpacity: 1,
        strokeOpacity: 0,
      },
      legendSet: {
        forceHidden: true,
      },

      tableData: [],
      detailTableData: [],
      tableHeaders: [
        {
          id: 1,
          key: "moldCode",
          title: "Tooling ID",
          style: {
            width: "19%",
          },
        },
        {
          id: 2,
          key: "parts",
          title: "Part",
          style: {
            width: "19%",
          },
        },
        {
          id: 3,
          key: "utilizationRate",
          title: "Utilization Rate",
          style: {
            width: "14%",
          },
        },
        {
          id: 4,
          key: "eolDate",
          title: "Insights",
          style: {
            width: "12%",
          },
        },
        {
          id: 5,
          key: "refurbPriority",
          title: "Refurbishment Priority",
          style: {
            width: "18%",
          },
        },
        {
          id: 6,
          key: "toolingStatus",
          title: "Tooling Status",
          style: {
            width: "18%",
          },
        },
      ],
      tableTooltipInfo: {
        text: null,
        top: null,
        left: null,
      },
      isOpenedTableTooltip: false,
      visibleBadgeDropdownId: null,
      visibleBadgeDropdownIndex: null,
    };
  },
  computed: {
    tableHeaderWidthArray() {
      return this.tableHeaders.map((item, index) => item.style.width);
    },
    tableTooltipPosition() {
      return {
        top: `${this.tableTooltipInfo.top}px`,
        left: `${this.tableTooltipInfo.left}px`,
      };
    },
  },
  methods: {
    async fetchData(timeValue, options) {
      if (timeValue) {
        this.timeSettings.timeValue = timeValue;
        this.pagination.size = null;
      }
      if (options === "master-filter") {
        this.pagination.page = 1;
      }

      await axios
        .get(`/api/asset/tol-eol`, {
          params: {
            ...this.pagination,
            ...this.timeSettings,
            ...this.requestParam,
          },
        })
        .then((response) => {
          const data = response.data;

          if (timeValue) {
            this.detailTableData = data.content;
            this.timeSettings.timeValue = null;
            this.pagination.size = 20;
            return;
          }

          if (!data.chartItems.length || !data.content.length) {
            this.tableData = [];
            this.chartData = [];
            this.pagination.page = 1;
            this.totalPages = 1;
            return;
          }

          this.tableData = data.content;
          this.chartData = data.chartItems;
          this.pagination.page = data.number + 1;
          this.totalPages = data.totalPages;
        })
        .catch((error) => {
          console.warn(error);
        });
    },
    getInsightsStatus(type) {
      switch (type) {
        case "COMPLETED":
          return "Completed end of life";
        case "REACHING":
          return "Reaches end of life";
        case "UNPREDICTABLE":
          return "Reaches end of life";
        default:
          return "";
      }
    },
    getUtilizationStatusColor(status) {
      switch (status) {
        case "HIGH":
          return "#E34537";
        case "MEDIUM":
          return "#F7CC57";
        case "LOW":
          return "#41CE77";
        case "PROLONGED":
          return "#7A2E17";
        default:
          return "transparent";
      }
    },
    sortTable(key) {
      this.isSortTableByDesc = !this.isSortTableByDesc;
      this.pagination.sort = `${key},${
        this.isSortTableByDesc ? "desc" : "asc"
      }`;
      this.fetchData();
    },
    clickNextPageButton() {
      const tableWrapper = this.$parent.$children[0].$children.filter(
        (item) => item.$refs.tableWrapper
      );
      if (tableWrapper) tableWrapper[0].$refs.tableWrapper.scrollTo(0, 0);
      this.pagination.page++;
      this.fetchData();
    },
    clickPrevPageButton() {
      const tableWrapper = this.$parent.$children[0].$children.filter(
        (item) => item.$refs.tableWrapper
      );
      if (tableWrapper) tableWrapper[0].$refs.tableWrapper.scrollTo(0, 0);
      this.pagination.page--;
      this.fetchData();
    },
    openDetailModal(event) {
      this.timeSettings.timeValue = event.target.dataItem.dataContext?.title;
      this.isOpendDetailModal = true;
    },
    closeDetailModal() {
      this.timeSettings.timeValue = null;
      this.isOpendDetailModal = false;
    },
    xAxisLabelTextAdapter(text, target) {
      if (target.dataItem && target.dataItem.dataContext) {
        const year = target.dataItem.dataContext.title?.slice(0, 4);
        const half = target.dataItem.dataContext.title?.slice(5, 6);

        return `${half === `1` ? "1st" : "2nd"} half\n${year}`;
      } else {
        return text;
      }
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      const moldCount = target.dataItem.dataContext?.eolMoldCount;
      const title = target.dataItem.dataContext?.title;
      const year = title.slice(0, 4);
      const half = title.slice(5, 6) === "1" ? "1st half " : "2nd half ";

      return String.raw`
        <div class="chart-tooltip">
          <span>${half + year}</span>
          <span>${moldCount}&nbsp;${
        moldCount === 1 ? "Tooling" : "Toolings"
      }</span>
        </div>
      `;
    },
    getUtilizationRateForProgressBar(data) {
      if (!data) return 0;

      return data >= 100 ? 100 : data;
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    getTableHeaderHeight() {
      return this.$refs.tableHeaderRef?.getBoundingClientRect().height;
    },
    handleUrl() {
      const url = new URL(location.href);
      const params = url.searchParams;
      const refurbPriority = params.get("refurbPriority");

      this.requestParam = { refurbPriority: refurbPriority };
    },
    setTooltip(e, text) {
      if (e.target.clientWidth >= e.target.scrollWidth || !text) return;

      this.tableTooltipInfo.text = text;
      this.isOpenedTableTooltip = true;

      this.$nextTick(() => {
        const targetRect = e.target.getBoundingClientRect();
        const tooltipRect = this.$refs.tooltipRef.getBoundingClientRect();

        this.tableTooltipInfo.top =
          targetRect.top + targetRect.height / 2 - tooltipRect.height / 2;
        this.tableTooltipInfo.left = targetRect.left + targetRect.width + 8;
      });
    },
    initializeTableTooltip() {
      this.tableTooltipInfo = {
        text: null,
        top: null,
        left: null,
      };
      this.isOpenedTableTooltip = false;
    },
    showBadgeDropdown(id, index) {
      if (
        this.visibleBadgeDropdownId === id &&
        this.visibleBadgeDropdownIndex === index
      ) {
        this.visibleBadgeDropdownId = null;
        this.visibleBadgeDropdownIndex = null;
        return;
      }
      this.visibleBadgeDropdownId = id;
      this.visibleBadgeDropdownIndex = index;
    },
    onBadgeDropdownClose() {
      this.visibleBadgeDropdownId = null;
      this.visibleBadgeDropdownIndex = null;
    },
  },
  mounted() {
    this.handleUrl();
    window.addEventListener("resize", () => {
      this.getTableHeaderHeight();
    });
    this.fetchData();
  },
};
</script>

<style scoped>
.tol-eol-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 32px 20px;
}

.tol-eol-header-container {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  flex-shrink: 0;
}

.tol-eol-body-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.tol-eol-chart-wrapper {
  width: 100%;
  height: 50%;
  padding-bottom: 40px;
}

.tol-eol-table-wrapper {
  height: 50%;
}

.tol-eol-table-empty {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #efefef;
  position: absolute;
  font-family: Helvetica Neue, Helvetica, Arial, sans-serif;
  font-size: 12px;
  color: #4b4b4b;
}

.tol-eol-table-th {
  display: flex;
  align-items: center;
}

.tol-eol-shot-count-ratio {
  width: 100%;
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.tol-eol-shot-count-ratio > div {
  width: fit-content;
  height: 100%;
  display: flex;
  align-items: flex-end;
}

.tol-eol-shot-count-ratio > div:nth-of-type(1) {
  font-size: 14.66px;
}

.tol-eol-shot-count-ratio > div:nth-of-type(n + 2) {
  font-size: 11.25px;
}

.tol-eol-utilization-rate-bar {
  width: 100%;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11.25px;
}

.tol-eol-utilization-rate-bar > div:first-of-type {
  width: 40px;
  flex-shrink: 0;
}

.tol-eol-utilization-rate-bar > div:last-of-type {
  width: 100%;
}

.tol-eol-pagination-wrapper {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-shrink: 0;
}

.chart-tooltip {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 14.66px;
  font-weight: 400;
  background-color: #ffffff;
  gap: 48px;
  box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
  color: #4b4b4b;
  display: flex;
  align-content: center;
  justify-content: space-between;
  padding: 10px;
}

.chart-tooltip > span:nth-of-type(1) {
  font-weight: 700;
}

.tol-eol-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tol-eol-table-tooltip-wrapper {
  width: fit-content;
  min-width: 24px;
  height: fit-content;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 12px;
  line-height: 14px;
  white-space: nowrap;
  color: #ffffff;
  padding: 6px 8px;
  position: absolute;
  border-radius: 3px;
  background: rgba(75, 75, 75, 0.9);
  z-index: 9999;
  transition: all 0.2s ease-in-out;
}

.tol-eol-table-tooltip-wrapper::after {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  border: 6px solid transparent;
  border-right-color: rgba(75, 75, 75, 0.9);
  left: 0;
  top: 50%;
  border-left: 0;
  margin-top: -6px;
  margin-left: -6px;
}

.tol-eol-show-more-icon {
  width: 10px;
  height: 10px;
  margin-right: 4px;
  margin-left: 4px;
}

.tol-eol-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding-left: 6px;
}

.tol-eol-badge-arrow-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
}

.tol-eol-badge-arrow-icon.active {
  transform: rotate(180deg);
}

.tol-eol-badge-arrow-icon.inactive {
  transform: rotate(180deg);
}

.tol-eol-badge-styles {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #3491ff;
  color: #ffffff;
  font-size: 11.25px;
  font-style: normal;
}
</style>
