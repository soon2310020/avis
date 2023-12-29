<template>
  <div class="ovr-utl-main-container">
    <!-- detail modal -->
    <emdn-modal
      :is-opened="isModalOpened"
      heading-text="Overall Utilization Details"
      :modal-handler="closeModal"
    >
      <template #body>
        <div class="ovr-utl-detail-container">
          <div class="ovr-utl-detail-header">
            <div style="width: fit-content; height: 16px">
              <span class="label-text -supplier">Supplier</span>
              <span>{{ utilizationDetailData.supplier?.name }}</span>
            </div>
          </div>
          <div style="width: 100%; height: 445px">
            <emdn-data-table
              v-if="!isLoading"
              :scrollable="true"
              class="ovr-detail-data-table"
            >
              <template #colgroup>
                <col
                  v-for="item in columnWidthArrayForDetail"
                  :style="{ width: item }"
                />
              </template>
              <template #thead>
                <tr>
                  <th
                    v-for="(tableHeader, index) in detailTableHeaders"
                    :key="index"
                    style="z-index: 20; padding: 8px 12px 8px 16px"
                  >
                    <div
                      class="header-section"
                      :style="{
                        justifyContent:
                          tableHeader.align === 'left'
                            ? 'flex-start'
                            : 'flex-end',
                        textAlign: tableHeader.align,
                      }"
                    >
                      {{ tableHeader.title }}
                      <span
                        @click="sortDetailTable(tableHeader.key)"
                        :class="
                          detailTableSort === `${tableHeader.key},asc`
                            ? 'active'
                            : ''
                        "
                      ></span>
                    </div>
                  </th>
                </tr>
              </template>
              <template #tbody>
                <tr v-for="(item, index) in utilizationDetailData.content">
                  <td>
                    <div
                      @mouseover.stop="(e) => setTooltip(e, item.moldCode)"
                      @mouseleave.stop="initializeTableTooltip"
                      class="ovr-utl-truncate-text"
                    >
                      {{ item.moldCode }}
                    </div>
                  </td>
                  <td>
                    <div style="display: flex; align-items: flex-start">
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, item.parts[0]?.name)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="ovr-utl-truncate-text"
                      >
                        {{ item.parts[0]?.name }}
                      </div>
                      <emdn-dropdown-root
                        v-if="item.parts.length > 1"
                        :on-dropdown-close="onBadgeDropdownClose"
                        position="auto"
                      >
                        <emdn-dropdown-trigger>
                          <div
                            class="ovr-utl-badge-wrapper"
                            @click="showBadgeDropdown(item.parts[0]?.id, index)"
                          >
                            <span
                              class="ovr-utl-badge-arrow-icon"
                              :class="
                                visibleBadgeDropdownId === item.parts[0]?.id &&
                                visibleBadgeDropdownIndex === index
                                  ? 'active'
                                  : ''
                              "
                            >
                              <img
                                src="/images/more.png"
                                class="ovr-utl-show-more"
                              />
                            </span>
                            <span
                              :style="{ width: '20px', height: '20px' }"
                              class="ovr-utl-badge-styles"
                            >
                              +{{ item.parts.length - 1 }}
                            </span>
                          </div>
                        </emdn-dropdown-trigger>
                        <emdn-dropdown-portal>
                          <emdn-dropdown-content>
                            <emdn-dropdown-item
                              v-for="(part, index) in item?.parts"
                              v-if="index !== 0"
                              :key="index"
                              :text="part.name"
                            ></emdn-dropdown-item>
                          </emdn-dropdown-content>
                        </emdn-dropdown-portal>
                      </emdn-dropdown-root>
                    </div>
                  </td>
                  <td style="text-align: right">
                    {{ item.accumShotCount?.toLocaleString() }}
                  </td>
                  <td style="text-align: right">
                    {{ item.designedShotCount?.toLocaleString() }}
                  </td>
                  <td style="text-align: right">
                    {{ item.utilizationRate?.toLocaleString() }}%
                  </td>
                  <td>
                    <div
                      style="
                        display: flex;
                        align-items: center;
                        justify-content: flex-start;
                      "
                    >
                      <span
                        class="status-icon"
                        :class="item.utilizationStatus.toLowerCase()"
                      ></span>
                      <span style="text-align: left"
                        >{{ capitalizeFirstLetter(item.utilizationStatus) }}
                      </span>
                    </div>
                  </td>
                </tr>
              </template>
            </emdn-data-table>
          </div>
        </div>
      </template>
      <template #footer>
        <div>
          <emdn-pagination
            :current-page="detailPageNum"
            :total-page="detailTotalPages"
            :next-click-handler="nextButtonHandlerForDetail"
            :previous-click-handler="prevButtonHandlerForDetail"
          ></emdn-pagination>
        </div>
      </template>
    </emdn-modal>

    <!-- chart -->
    <div class="ovr-chart-container">
      <emdn-xy-chart
        style-props="width: 100%; height: 100%;"
        :data="utilizationData"
        :axis-data-binder="axisDataBinder"
        :bar-data-binder="barDataBinder"
        category="supplierName"
        :column-set="columnSet"
        :column-template-set="columnTemplateSet"
        :root-number-formatter-set="rootNumberFormatterSet"
        :y-axis-extra-label-set="yAxisExtraLabelSet"
        :chart-set="chartSet"
        :legend-set="legendSet"
        :previous-button="previousButton"
        :next-button="nextButton"
        :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
        :x-axis-grid-set="xAxisGridSet"
      >
      </emdn-xy-chart>
    </div>

    <!-- data table -->
    <div class="ovr-data-table-container">
      <div class="ovr-data-table-wrapper">
        <emdn-data-table :scrollable="true" class="ovr-data-table">
          <template #colgroup>
            <col v-for="item in columnWidthArray" :style="{ width: item }" />
          </template>
          <template #thead>
            <tr ref="tableHeaderRef">
              <th
                v-for="(tableHeader, index) in mainTableHeaders"
                :key="index"
                style="z-index: 10; padding: 8px 12px 8px 16px"
              >
                <div class="header-section">
                  {{ tableHeader.title }}
                  <span
                    @click="sortTable(tableHeader.key)"
                    :class="
                      tableSort === `${tableHeader.key},asc` ? 'active' : ''
                    "
                  ></span>
                </div>
              </th>
            </tr>
          </template>
          <template #tbody>
            <div
              v-if="!utilizationData?.length"
              class="ovr-utl-table-empty"
              :style="{ height: `calc(100% - ${getTableHeaderHeight()}px)` }"
            >
              No Data Available
            </div>
            <template v-else>
              <tr v-for="item in utilizationData">
                <td>
                  <div
                    @mouseover.stop="(e) => setTooltip(e, item.supplierName)"
                    @mouseleave.stop="initializeTableTooltip"
                    class="ovr-utl-truncate-text"
                  >
                    {{ item.supplierName }}
                  </div>
                </td>
                <td>
                  <div
                    style="
                      display: flex;
                      justify-content: flex-start;
                      align-items: center;
                    "
                  >
                    <emdn-cta-button
                      style-props="height: 18px; padding: 0; margin: 0;"
                      button-type="text hyperlink"
                      :click-handler="() => openModal(item.supplierId)"
                      >{{
                        `${item.totalMoldCount} ${
                          item.totalMoldCount === 1 ? "Tooling" : "Toolings"
                        }`
                      }}
                    </emdn-cta-button>
                  </div>
                </td>
                <td style="text-align: right">
                  {{ item.lowMoldCount }}
                </td>
                <td style="text-align: right">
                  {{ item.mediumMoldCount }}
                </td>
                <td style="text-align: right">
                  {{ item.highMoldCount }}
                </td>
                <td style="text-align: right">
                  {{ item.prolongedMoldCount }}
                </td>
              </tr>
            </template>
          </template>
        </emdn-data-table>
      </div>
      <div class="ovr-data-table-pagination-wrapper">
        <emdn-pagination
          :current-page="pageNum"
          :total-page="totalPages"
          :next-click-handler="nextButtonHandler"
          :previous-click-handler="previousButtonHandler"
        ></emdn-pagination>
      </div>
    </div>

    <emdn-portal v-if="isOpenedTableTooltip">
      <div
        :style="tableTooltipPosition"
        class="ovr-utl-table-tooltip-wrapper"
        ref="tooltipRef"
      >
        {{ tableTooltipInfo.text }}
      </div>
    </emdn-portal>
  </div>
</template>

<script>
module.exports = {
  props: {
    tableColumnList: Array,
    fetchData: Function,
  },
  data() {
    return {
      isLoading: false,
      columnWidthArray: ["16%", "16%", "17%", "17%", "17%", "17%"],
      columnWidthArrayForDetail: ["16%", "16%", "17%", "17%", "16%", "18%"],
      isModalOpened: false,
      utilizationData: [],
      utilizationDetailData: [],
      selectedOneSupplier: "",
      totalPages: 1,
      pageNum: 1,
      pageSize: 5,
      detailTotalPages: 1,
      detailPageNum: 1,
      detailPageSize: 20,
      tableSort: "",
      detailTableSort: "",
      isTableSortByDesc: true,
      isDetailTableSortByDesc: true,
      previousButton: {
        visible: true,
        disabled: true,
        handler: () => this.previousButtonHandler(),
      },
      nextButton: {
        visible: true,
        disabled: false,
        handler: () => this.nextButtonHandler(),
      },
      axisDataBinder: {
        xAxis: { name: "", type: "CategoryAxis" },
        yAxis: { name: "Number of Toolings", type: "ValueAxis", isLeft: true },
      },
      barDataBinder: [],
      columnSet: {
        stacked: true,
        clustered: false,
      },
      columnTemplateSet: {
        width: 70,
        fillOpacity: 1,
        strokeOpacity: 0,
      },
      rootNumberFormatterSet: {
        numberFormat: "#,###.",
      },
      xAxisGridSet: {
        strokeOpacity: 0,
      },
      chartSet: {
        valueAxis: {
          min: 0,
        },
      },
      legendSet: {
        paddingTop: 16,
      },
      yAxisExtraLabelSet: {
        text: "Number of Toolings",
      },
      mainTableHeaders: [],
      detailTableHeaders: [
        {
          id: 1,
          title: "Tooling",
          key: "moldCode",
          align: "left",
        },
        {
          id: 2,
          title: "Part Name",
          key: "partName",
          align: "left",
        },
        {
          id: 3,
          title: "Accumulated Shots",
          key: "accumShotCount",
          align: "right",
        },
        {
          id: 4,
          title: "Warranty Period",
          key: "designedShotCount",
          align: "right",
        },
        {
          id: 5,
          title: "Utilization Rate",
          key: "utilizationRate",
          align: "right",
        },
        {
          id: 6,
          title: "Utilization Status",
          key: "utilizationStatus",
          align: "left",
        },
      ],
      toggleSortButton: false,
      isOpenedTableTooltip: false,
      tableTooltipInfo: {
        text: null,
        top: null,
        left: null,
      },
      visibleBadgeDropdownId: null,
      visibleBadgeDropdownIndex: null,
    };
  },
  computed: {
    tableTooltipPosition() {
      return {
        top: `${this.tableTooltipInfo.top}px`,
        left: `${this.tableTooltipInfo.left}px`,
      };
    },
  },
  methods: {
    async getUtilizationData(param) {
      if (param === "master-filter-update") this.pageNum = 1;

      await axios
        .get(`/api/analysis/ovr-utl`, {
          params: {
            page: this.pageNum,
            size: this.pageSize,
            sort: this.tableSort,
          },
        })
        .then((res) => {
          const data = res.data;
          const utilizationConfig = data.config;

          this.utilizationData = data.content;
          this.pageNum = data.number + 1;
          this.totalPages = data.totalPages;
          this.barDataBinder = [
            {
              key: "lowMoldCount",
              displayName: `Low (0%-${utilizationConfig.low}%)`,
              color: "#41CE77",
            },
            {
              key: "mediumMoldCount",
              displayName: `Medium (${utilizationConfig.low}%-${utilizationConfig.medium}%)`,
              color: "#F7CC57",
            },
            {
              key: "highMoldCount",
              displayName: `High (${utilizationConfig.medium}%-${utilizationConfig.high}%)`,
              color: "#E34537",
            },
            {
              key: "prolongedMoldCount",
              displayName: `Prolonged (>${utilizationConfig.high}%)`,
              color: "#7A2E17",
            },
          ];
        });
    },
    async getUtilizationDetailData(supplierId) {
      this.isLoading = true;
      await axios
        .get(`/api/analysis/ovr-utl/details`, {
          params: {
            page: this.detailPageNum,
            size: this.detailPageSize,
            sort: this.detailTableSort,
            supplierId: supplierId,
          },
        })
        .then((res) => {
          if (!res.data.content.length) {
            this.detailPageNum = 1;
            this.detailTotalPages = 1;
          }
          this.utilizationDetailData = res.data;
          this.detailPageNum = res.data.number + 1;
          this.detailTotalPages = res.data.totalPages;
          this.isLoading = false;
        })
        .catch((error) => {
          console.warn(error);
          this.isLoading = false;
        });
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      const targetDisplayName = target.dataItem.component.get("name");
      const valueY = target.dataItem.get("valueY");

      return String.raw`
          <div class="ovr-chart-tooltip">
            <div class="ovr-chart-tooltip-contents">
              <span>${targetDisplayName.split(":")[0]}</span>
              <span>{valueY} ${valueY === 1 ? "Tooling" : "Toolings"}</span>
            </div>
          </div>
      `;
    },
    sortTable(key) {
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableSort = `${key},${this.isTableSortByDesc ? "desc" : "asc"}`;
      this.getUtilizationData();
    },
    sortDetailTable(key) {
      this.isDetailTableSortByDesc = !this.isDetailTableSortByDesc;
      this.detailTableSort = `${key},${
        this.isDetailTableSortByDesc ? "desc" : "asc"
      }`;
      this.getUtilizationDetailData(this.utilizationDetailData.supplier.id);
    },
    previousButtonHandler() {
      this.pageNum--;
      this.getUtilizationData();
    },
    nextButtonHandler() {
      this.pageNum++;
      this.getUtilizationData();
    },
    chartPagination() {
      if (this.totalPages === 0 || this.totalPages === 1) {
        this.previousButton = { ...this.previousButton, disabled: true };
        this.nextButton = { ...this.nextButton, disabled: true };
        return;
      }

      if (this.pageNum === 1) {
        this.previousButton = { ...this.previousButton, disabled: true };
      } else {
        this.previousButton = { ...this.previousButton, disabled: false };
      }

      if (this.pageNum === this.totalPages) {
        this.nextButton = { ...this.nextButton, disabled: true };
      } else {
        this.nextButton = { ...this.nextButton, disabled: false };
      }
    },
    openModal(supplierId) {
      this.getUtilizationDetailData(supplierId);
      this.isModalOpened = true;
    },
    closeModal() {
      this.detailPageNum = 1;
      this.isModalOpened = false;
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    filteredTableColumnList() {
      this.mainTableHeaders = this.tableColumnList
        .map((item) => {
          if (item.selected) {
            return item;
          }
        })
        .filter((item) => item);
    },
    capitalizeFirstLetter(str) {
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    },
    nextButtonHandlerForDetail() {
      this.detailPageNum++;
      this.getUtilizationDetailData(this.utilizationDetailData.supplier.id);
    },
    prevButtonHandlerForDetail() {
      this.detailPageNum--;
      this.getUtilizationDetailData(this.utilizationDetailData.supplier.id);
    },
    getTableHeaderHeight() {
      return this.$refs.tableHeaderRef?.getBoundingClientRect().height;
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
  watch: {
    tableColumnList() {
      this.filteredTableColumnList();
    },
    pageNum: {
      handler(newVal) {
        this.chartPagination();
      },
      immediate: true,
    },
    totalPages() {
      this.chartPagination();
    },
  },
  mounted() {
    window.addEventListener("resize", this.getTableHeaderHeight);
    this.getUtilizationData();
    this.filteredTableColumnList();
  },
  destroyed() {
    window.removeEventListener("resize", this.getTableHeaderHeight);
  },
};
</script>

<style scoped>
.ovr-utl-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ovr-chart-container {
  width: 100%;
  height: 55%;
  padding-bottom: 40px;
  flex-shrink: 0;
}

.ovr-data-table-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 45%;
  flex-shrink: 0;
  justify-content: space-between;
}

.ovr-data-table-wrapper {
  width: 100%;
  height: 100%;
}

.ovr-data-table-pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  width: 100%;
  height: 44px;
  flex-shrink: 0;
}

.ovr-data-table table th {
  padding: 10px 14px 10px 18px !important;
}

.ovr-data-table table td {
  padding: 10px 18px !important;
}

.ovr-data-table table thead tr th {
  text-align: right;
}

.ovr-data-table table thead tr th > div {
  text-align: right;
}

.ovr-data-table table thead tr th:nth-child(-n + 2) > div {
  justify-content: flex-start;
}

.ovr-data-table table tbody tr td {
  text-align: right;
  vertical-align: top;
}

.ovr-data-table table tbody tr td:first-child {
  text-align: left;
}

.ovr-data-table .cell-no-data td {
  padding: 0;
  background-color: #efefef;
}

.ovr-data-table .cell-no-data td div {
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
}

.ovr-data-table .header-section {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: end;
}

.ovr-data-table .header-section span {
  cursor: pointer;
  display: block;
  width: 14px;
  height: 14px;
  flex: none;
  border-radius: 14px;
}

.ovr-data-table .header-section span:hover {
  background: #deedff;
}

.ovr-data-table .header-section span::before {
  content: "";
  display: block;
  width: 14px;
  height: 14px;
  background-size: 14px 14px;
  background: url("/images/icon/Icon-ionic-ios-arrow-round-up.svg") center
    no-repeat;
}

.ovr-data-table .header-section span.active {
  rotate: 180deg;
}

.label-text {
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
}

.label-text.-supplier {
  margin-right: 26px;
}

.label-text.-part {
  margin-right: 38px;
}

.ovr-utl-detail-container {
  width: 100%;
  height: 502px;
}

.ovr-utl-detail-header {
  display: flex;
  gap: 232px;
  align-items: center;
  margin-bottom: 37px;
}

.status-icon {
  display: inline-block;
  flex-shrink: 0;
  width: 15px;
  height: 15px;
  border-radius: 30px;
}

.status-icon {
  margin-right: 4px;
}

.status-icon.low {
  background: #41ce77;
}

.status-icon.medium {
  background: #f7cc57;
}

.status-icon.high {
  background: #e34537;
}

.status-icon.prolonged {
  background: #7a2e17;
}

.ovr-detail-data-table .header-section {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: end;
}

.ovr-detail-data-table .header-section span {
  cursor: pointer;
  display: block;
  width: 14px;
  height: 14px;
  flex: none;
  border-radius: 14px;
}

.ovr-detail-data-table .header-section span:hover {
  background: #deedff;
}

.ovr-detail-data-table .header-section span::before {
  content: "";
  display: block;
  width: 14px;
  height: 14px;
  background-size: 14px 14px;
  background: url("/images/icon/Icon-ionic-ios-arrow-round-up.svg") center
    no-repeat;
}

.ovr-detail-data-table .header-section span.active {
  rotate: 180deg;
}

.ovr-detail-data-table th,
.ovr-detail-data-table td {
  text-align: right;
}

.ovr-detail-data-table th:nth-child(-n + 3),
.ovr-detail-data-table td:nth-child(-n + 3) {
  text-align: left;
}

.ovr-chart-tooltip {
  background-color: #ffffff;
  min-width: 188px;
  padding: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
  color: #4b4b4b;
}

.ovr-chart-tooltip > .ovr-chart-tooltip-contents {
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.ovr-utl-table-empty {
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

.ovr-utl-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ovr-utl-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.ovr-utl-badge-arrow-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
}

.ovr-utl-badge-arrow-icon.active {
  transform: rotate(180deg);
}

.ovr-utl-show-more {
  width: 10px;
  height: 10px;
  margin-right: 4px;
  margin-left: 4px;
}

.ovr-utl-badge-styles {
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

<style>
.ovr-utl-table-tooltip-wrapper {
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

.ovr-utl-table-tooltip-wrapper:after {
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
</style>
