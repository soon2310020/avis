<template>
  <div class="cap-pln-container">
    <div class="cap-pln-top-bar">
      <!--  Master Filter  -->
      <master-filter-wrapper
        :filter-code="filterCode"
        :notify-filter-updated="fetchAllData"
      ></master-filter-wrapper>

      <!--  Page Filters  -->
      <div class="cap-pln-filters-container">
        <span>View By</span>
        <div>
          <emdn-cta-button
            button-type="dropdown"
            color-type="white"
            :active="isOpendViewByDropdown"
            :click-handler="clickViewByDropdownButton"
            >{{ viewBy }}
          </emdn-cta-button>
          <emdn-dropdown
            id="view-by-dropdown"
            :visible="isOpendViewByDropdown"
            :items="viewByDropdownItems"
            :click-handler="clickViewByDropdownOption"
            :on-close="() => (isOpendViewByDropdown = false)"
          ></emdn-dropdown>
        </div>
        <div>
          <emdn-cta-button
            button-type="date-picker"
            color-type="blue-fill"
            :click-handler="clickDatePickerButton"
          >
            {{ displayDate }}
          </emdn-cta-button>
        </div>
        <emdn-tooltip>
          <template #context>
            <emdn-icon
              button-type="restore-icon"
              :click-handler="resetCapacityData"
            ></emdn-icon>
          </template>
          <template #body>
            <div style="white-space: nowrap">Reset to Default</div>
          </template>
        </emdn-tooltip>
      </div>
    </div>

    <div class="cap-pln-main-container">
      <div class="cap-pln-main-left">
        <!--  List Search Bar  -->
        <div class="cap-pln-list-container">
          <emdn-list-search-bar
            :items="listItems"
            :placeholder="searchbarPlaceholder"
            :click-handler="clickListItem"
            :search-handler="getSearchValue"
          ></emdn-list-search-bar>
          <div class="cap-pln-list-footer">
            <emdn-pagination
              :current-page="listItemsCurrentPage"
              :total-page="listItemsTotalPage"
              :next-click-handler="clickNextPageButton"
              :previous-click-handler="clickPrevPageButton"
            ></emdn-pagination>
          </div>
        </div>
      </div>

      <div class="cap-pln-main-right">
        <!--  Chart  -->
        <div class="cap-pln-chart-container">
          <div class="cap-pln-chart-wrapper">
            <emdn-xy-chart
              style-props="width: 100%; height: 100%;"
              :line-data-binder="lineDataBinder"
              :bar-data-binder="barDataBinder"
              :column-template-set="columnTemplateSet"
              :category="chartCategory"
              :data="chartData"
              :chart-set="chartSet"
              :y-axis-grid-set="yAxisGridSet"
              :y-axis-extra-label-set="yAxisExtraLabelSet"
              :root-number-formatter-set="rootNumberFormatterSet"
              :legend-set="legendSet"
              :previous-button="previousButton"
              :next-button="nextButton"
              :chart-item-click-handler="clickChartItem"
              :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
            ></emdn-xy-chart>
          </div>
          <div class="cap-pln-chart-legend-wrapper">
            <emdn-chart-legend
              v-if="chartData.length"
              style-props="width: 100%; height: 24px"
              :data="chartLegendData"
              :legend-marker-rectangles-stroke-dash-array-adapter="
                legendMarkerRectanglesStrokeDashArrayAdapter
              "
            ></emdn-chart-legend>
          </div>
        </div>

        <!--  Data Table  -->
        <div class="cap-pln-table-container">
          <emdn-data-table
            :scrollable="true"
            :style-props="{ paddingBottom: '1px' }"
          >
            <template #colgroup>
              <col
                v-for="item in tableHeaderList.filter(
                  (item, index) => item.width
                )"
                :style="{ width: item.width }"
              />
            </template>
            <template #thead>
              <tr ref="tableHeaderRef">
                <th v-for="header in tableHeaderList" class="cap-pln-table-th">
                  <div
                    :style="[
                      {
                        justifyContent:
                          header.align === 'left' ? 'flex-start' : 'flex-end',
                      },
                    ]"
                  >
                    <span :style="{ textAlign: header.align }">{{
                      header.title
                    }}</span>
                    <span
                      class="cap-pln-table-sort-icon"
                      @click.prevent.stop="sortTable(header.key)"
                    >
                      <emdn-icon
                        :button-type="
                          tableSort === `${header.key},asc`
                            ? 'sort-asce'
                            : 'sort-desc'
                        "
                        style-props="width: 18px; height: 18px; padding: 0; margin-left: 2px;"
                        :active="tableSort.includes(header.key)"
                      ></emdn-icon>
                    </span>
                  </div>
                </th>
              </tr>
            </template>
            <template #tbody>
              <div
                v-if="!tableData.length"
                class="cap-pln-table-empty"
                :style="{ height: `calc(100% - ${getTableHeaderHeight()}px)` }"
              >
                No Data Available
              </div>
              <template v-else>
                <!--  By Product  -->
                <tr
                  v-if="viewBy === 'Product'"
                  v-for="(data, index) in tableData"
                  key="index"
                >
                  <td
                    :style="{
                      textAlign: alignTdText('partCode'),
                    }"
                  >
                    <div
                      @mouseover.stop="(e) => setTooltip(e, data?.partCode)"
                      @mouseleave.stop="initializeTableTooltip"
                      class="cap-pln-truncate-text"
                    >
                      {{ data?.partCode }}
                    </div>
                  </td>
                  <td
                    :style="{
                      textAlign: alignTdText('suppliers'),
                    }"
                  >
                    <div style="display: flex; align-items: flex-start">
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, data?.suppliers[0]?.name)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="cap-pln-truncate-text"
                      >
                        {{ data?.suppliers[0]?.name }}
                      </div>
                      <emdn-dropdown-root
                        v-if="data.suppliers.length > 1"
                        :on-dropdown-close="onBadgeDropdownClose"
                        position="auto"
                        max-height="200px"
                      >
                        <emdn-dropdown-trigger>
                          <div
                            class="cap-pln-badge-wrapper"
                            @click="
                              showBadgeDropdown(data?.suppliers[0]?.id, index)
                            "
                          >
                            <span
                              class="cap-pln-badge-arrow-icon"
                              :class="
                                visibleBadgeDropdownId ===
                                  data?.suppliers[0]?.id &&
                                visibleBadgeDropdownIndex === index
                                  ? 'active'
                                  : ''
                              "
                            >
                              <img
                                src="/images/more.png"
                                class="cap-pln-show-more-icon"
                              />
                            </span>
                            <span
                              style="width: 20px; height: 20px"
                              class="cap-pln-badge-styles"
                            >
                              +{{ data.suppliers.length - 1 }}
                            </span>
                          </div>
                        </emdn-dropdown-trigger>
                        <emdn-dropdown-portal>
                          <emdn-dropdown-content>
                            <emdn-dropdown-item
                              v-for="(supplier, index) in data?.suppliers"
                              v-if="index !== 0"
                              :key="index"
                              :text="supplier.name"
                            ></emdn-dropdown-item>
                          </emdn-dropdown-content>
                        </emdn-dropdown-portal>
                      </emdn-dropdown-root>
                    </div>
                  </td>
                  <td>
                    <div
                      class="cap-pln-custom-cell"
                      :style="{
                        alignItems:
                          alignTdText('moldCount') === 'right'
                            ? 'flex-end'
                            : 'flex-start',
                      }"
                    >
                      <emdn-cta-button
                        style-props="margin: 0; padding: 0; width: fit-content; height: 20px; align-items: flex-start;"
                        button-type="text"
                        :click-handler="
                          () => openDetailCapacityModal('Part', data?.partId)
                        "
                      >
                        {{ data.moldCount }}
                      </emdn-cta-button>
                    </div>
                  </td>
                  <td :style="{ textAlign: alignTdText('prodQty') }">
                    {{ data.prodQty?.toLocaleString() }}
                  </td>
                  <td :style="{ textAlign: alignTdText('prodDemand') }">
                    {{ data.prodDemand?.toLocaleString() }}
                  </td>
                  <td :style="{ textAlign: alignTdText('prodCapa') }">
                    {{ data.prodCapa?.toLocaleString() }}
                  </td>
                </tr>

                <!--  By Part  -->
                <tr
                  v-if="viewBy === 'Part'"
                  v-for="(data, index) in tableData"
                  key="index"
                >
                  <td
                    :style="{
                      textAlign: alignTdText('suppliers'),
                    }"
                  >
                    <div
                      @mouseover.stop="
                        (e) => setTooltip(e, data?.suppliers[0]?.name)
                      "
                      @mouseleave.stop="initializeTableTooltip"
                      class="cap-pln-truncate-text"
                    >
                      {{ data?.suppliers[0]?.name }}
                    </div>
                  </td>
                  <td :style="{ textAlign: alignTdText('plants') }">
                    <div style="display: flex; align-items: flex-start">
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, data?.plants[0]?.name)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="cap-pln-truncate-text"
                      >
                        {{ data?.plants[0]?.name }}
                      </div>
                      <emdn-dropdown-root
                        v-if="data.plants.length > 1"
                        :on-dropdown-close="onBadgeDropdownClose"
                        position="auto"
                        max-height="200px"
                      >
                        <emdn-dropdown-trigger>
                          <div
                            class="cap-pln-badge-wrapper"
                            @click="
                              showBadgeDropdown(data?.plants[0]?.id, index)
                            "
                          >
                            <span
                              class="cap-pln-badge-arrow-icon"
                              :class="
                                visibleBadgeDropdownId ===
                                  data?.plants[0]?.id &&
                                visibleBadgeDropdownIndex === index
                                  ? 'active'
                                  : ''
                              "
                            >
                              <img
                                src="/images/more.png"
                                class="cap-pln-show-more-icon"
                              />
                            </span>
                            <span
                              style="width: 20px; height: 20px"
                              class="cap-pln-badge-styles"
                            >
                              +{{ data.plants.length - 1 }}
                            </span>
                          </div>
                        </emdn-dropdown-trigger>
                        <emdn-dropdown-portal>
                          <emdn-dropdown-content>
                            <emdn-dropdown-item
                              v-for="(plant, index) in data?.plants"
                              v-if="index !== 0"
                              :key="index"
                              :text="plant.name"
                            ></emdn-dropdown-item>
                          </emdn-dropdown-content>
                        </emdn-dropdown-portal>
                      </emdn-dropdown-root>
                    </div>
                  </td>
                  <td :style="{ textAlign: alignTdText('moldCount') }">
                    <div
                      class="cap-pln-custom-cell"
                      :style="{
                        alignItems:
                          alignTdText('moldCount') === 'right'
                            ? 'flex-end'
                            : 'flex-start',
                      }"
                    >
                      <emdn-cta-button
                        style-props="margin: 0; padding: 0; width: fit-content; height: 20px;"
                        button-type="text"
                        :click-handler="
                          () =>
                            openDetailCapacityModal(
                              'Supplier',
                              data?.partId,
                              data?.suppliers[0]?.id
                            )
                        "
                      >
                        {{ data.moldCount }}
                      </emdn-cta-button>
                    </div>
                  </td>
                  <td :style="{ textAlign: alignTdText('prodQty') }">
                    {{ data.prodQty?.toLocaleString() }}
                  </td>
                  <td :style="{ textAlign: alignTdText('prodDemand') }">
                    {{ data.prodDemand?.toLocaleString() }}
                  </td>
                  <td :style="{ textAlign: alignTdText('prodCapa') }">
                    {{ data.prodCapa?.toLocaleString() }}
                  </td>
                </tr>
              </template>
            </template>
          </emdn-data-table>
        </div>
      </div>
    </div>

    <!--  Date Picker Modal  -->
    <emdn-modal
      style-props="width: fit-content; height: fit-content;"
      :heading-text="`Choose a ${
        tempTimeScale.charAt(0).toUpperCase() +
        tempTimeScale.slice(1).toLowerCase()
      }`"
      :is-opened="isOpendDatePicker"
      :modal-handler="closeDatePickerModal"
    >
      <template #body>
        <emdn-date-picker
          :show-selector="true"
          :time-scale="tempTimeScale"
          :date-range="tempDateRange"
          :selector-options="selectorOptions"
          :modal-handler="closeDatePickerModal"
          @get-time-scale="getTimeScale"
          @get-date-range="getDateRange"
        ></emdn-date-picker>
      </template>
      <template #footer>
        <emdn-cta-button
          color-type="white"
          :click-handler="closeDatePickerModal"
          >Cancel
        </emdn-cta-button>
        <emdn-cta-button
          color-type="blue-fill"
          :click-handler="clickDatePickerSaveButton"
          >Save
        </emdn-cta-button>
      </template>
    </emdn-modal>

    <!--  Detail Capacity Modal  -->
    <detail-capacity-modal
      v-if="isOpendDetailCapacityModal"
      :is-opened="isOpendDetailCapacityModal"
      :modal-handler="closeDetailCapacityModal"
      :info-data="detailCapacityModalInfoData"
    ></detail-capacity-modal>

    <div
      v-if="isOpenedTableTooltip"
      :style="tableTooltipPosition"
      class="cap-pln-table-tooltip-wrapper"
      ref="tooltipRef"
    >
      {{ tableTooltipInfo.text }}
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "cap-pln-page",
  components: {
    "detail-capacity-modal": httpVueLoader(
      "/components/pages/supplychain/cap-pln/detail-capacity-modal.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      filterCode: "COMMON",
      viewBy: "Product",
      viewByForQuery: "products",
      searchQuery: "",
      timeScale: "",
      fromDate: "",
      toDate: "",
      timeValue: "",
      tableSort: "",
      selectedItemId: null,
      selectedWeek: "",
      selectedMonth: "",
      isTableSortByDesc: true,
      listItemsCurrentPage: 1,
      listItemsTotalPage: 1,
      listItemsPageSize: 10,
      displayDate: "",
      dateRange: { start: null, end: null },
      selectorOptions: ["QUARTER", "MONTH", "YEAR"],
      tempDateRange: { start: null, end: null },
      tempTimeScale: "",
      listItems: [],
      chartData: [],
      tableData: [],
      barDataBinder: [
        { key: "prodQty", displayName: "Produced Quantity", color: "#1A2281" },
      ],
      lineDataBinder: [
        { key: "prodDemand", displayName: "Demand", color: "#4EBCD5" },
        { key: "prodCapa", displayName: "Maximum Capacity", color: "#EB709A" },
        {
          key: "predCapa",
          displayName: "Predicted Capacity",
          color: "#EB709A",
          strokeDasharray: [5, 5],
        },
      ],
      chartCategory: "title",
      chartSet: {
        valueAxis: {
          min: 0,
        },
      },
      rootNumberFormatterSet: {
        numberFormat: "#,###.",
      },
      yAxisGridSet: {
        strokeOpacity: 0,
      },
      yAxisExtraLabelSet: {
        text: "Product produced quantity",
        paddingBottom: 20,
      },
      columnTemplateSet: {
        width: 30,
        fillOpacity: 1,
      },
      legendSet: {
        forceHidden: true,
      },
      disabledColumnSettings: {
        strokeWidth: 0.1,
        fillOpacity: 0.2,
      },
      chartLegendData: [
        {
          legendName: "Produced Quantity",
          legendFill: "#1A2281",
          legendStroke: "#1A2281",
        },
        {
          legendName: "Demand",
          legendFill: "#4EBCD5",
          legendStroke: "#4EBCD5",
        },
        {
          legendName: "Maximum Capacity",
          legendFill: "#EB709A",
          legendStroke: "#EB709A",
        },
        {
          legendName: "Predicted Capacity",
          legendFill: "#FFFFFF",
          legendStroke: "#EB709A",
        },
      ],
      nextButton: {
        visible: true,
        disabled: false,
        handler: () => {
          switch (this.timeScale) {
            case "QUARTER":
              this.fromDate = this.dateRange.start
                .add(1, "quarter")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .add(1, "quarter")
                .format("YYYYMMDD");
              break;
            case "MONTH":
              this.fromDate = this.dateRange.start
                .add(1, "month")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .add(1, "month")
                .format("YYYYMMDD");
              break;
            case "YEAR":
              this.fromDate = this.dateRange.start
                .add(1, "year")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .add(1, "year")
                .format("YYYYMMDD");
              break;
          }

          this.selectedWeek = "";
          this.selectedMonth = "";
          this.timeValue = Common.getDatePickerTimeValue(
            this.fromDate,
            this.toDate,
            this.timeScale
          );
          this.displayDate = Common.getDatePickerButtonDisplay(
            this.fromDate,
            this.toDate,
            this.timeScale
          );

          this.fetchCapacityData();
        },
      },
      previousButton: {
        visible: true,
        disabled: false,
        handler: () => {
          switch (this.timeScale) {
            case "QUARTER":
              this.fromDate = this.dateRange.start
                .subtract(1, "quarter")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .subtract(1, "quarter")
                .format("YYYYMMDD");
              break;
            case "MONTH":
              this.fromDate = this.dateRange.start
                .subtract(1, "month")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .subtract(1, "month")
                .format("YYYYMMDD");
              break;
            case "YEAR":
              this.fromDate = this.dateRange.start
                .subtract(1, "year")
                .format("YYYYMMDD");
              this.toDate = this.dateRange.end
                .subtract(1, "year")
                .format("YYYYMMDD");
              break;
          }

          this.selectedWeek = "";
          this.selectedMonth = "";
          this.timeValue = Common.getDatePickerTimeValue(
            this.fromDate,
            this.toDate,
            this.timeScale
          );
          this.displayDate = Common.getDatePickerButtonDisplay(
            this.fromDate,
            this.toDate,
            this.timeScale
          );

          this.fetchCapacityData();
        },
      },
      visibleBadgeDropdownId: null,
      visibleBadgeDropdownIndex: null,
      detailCapacityModalInfoData: {},
      isOpendViewByDropdown: false,
      isOpendDatePicker: false,
      isOpendDetailCapacityModal: false,
      searchbarPlaceholder: "Search product name",
      viewByDropdownItems: [
        {
          id: 1,
          title: "Product",
        },
        {
          id: 2,
          title: "Part",
        },
      ],
      tableHeaderByProductList: [
        {
          order: 1,
          title: "Part ID",
          align: "left",
          key: "partCode",
          width: "18%",
        },
        {
          order: 2,
          title: "Supplier",
          align: "left",
          key: "suppliers",
          width: "20%",
        },
        {
          order: 3,
          title: "Total Tooling",
          align: "right",
          key: "moldCount",
          width: "12%",
        },
        {
          order: 4,
          title: "Produced Quantity",
          align: "right",
          key: "prodQty",
          width: "",
        },
        {
          order: 5,
          title: "Demand",
          align: "right",
          key: "prodDemand",
          width: "",
        },
        {
          order: 6,
          title: "Maximum Capacity",
          align: "right",
          key: "prodCapa",
          width: "",
        },
      ],
      tableHeaderByPartList: [
        {
          order: 1,
          title: "Supplier",
          align: "left",
          key: "suppliers",
          width: "20%",
        },
        {
          order: 2,
          title: "Plant",
          align: "left",
          key: "plants",
          width: "20%",
        },
        {
          order: 3,
          title: "Total Tooling",
          align: "right",
          key: "moldCount",
          width: "12%",
        },
        {
          order: 4,
          title: "Produced Quantity",
          align: "right",
          key: "prodQty",
          width: "",
        },
        {
          order: 5,
          title: "Demand",
          align: "right",
          key: "prodDemand",
          width: "",
        },
        {
          order: 6,
          title: "Maximum Capacity",
          align: "right",
          key: "prodCapa",
          width: "",
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
    tableHeaderList() {
      return this.viewBy === "Product"
        ? this.tableHeaderByProductList
        : this.tableHeaderByPartList;
    },
    timeSettings() {
      return {
        timeScale: this.timeScale,
        timeValue: this.timeValue,
        fromDate: this.fromDate,
        toDate: this.toDate,
        week: this.selectedWeek,
        month: this.selectedMonth,
      };
    },
    tableTooltipPosition() {
      return {
        top: `${this.tableTooltipInfo.top}px`,
        left: `${this.tableTooltipInfo.left}px`,
      };
    },
  },
  methods: {
    fetchAllData() {
      this.tableSort = "";
      this.selectedWeek = "";
      this.selectedMonth = "";
      this.listItemsCurrentPage = 1;
      this.fetchListItemData();
    },
    async fetchListItemData() {
      await axios
        .get(`/api/supplychain/cap-pln/${this.viewByForQuery}`, {
          params: {
            filterCode: this.filterCode,
            query: this.searchQuery,
            page: this.listItemsCurrentPage,
            size: this.listItemsPageSize,
            timeScale: this.timeScale,
            timeValue: this.timeValue,
            fromDate: this.fromDate,
            toDate: this.toDate,
          },
        })
        .then((res) => {
          if (!res.data.content || res.data.totalPages === 0) {
            this.listItems = [];
            this.listItemsCurrentPage = 1;
            this.listItemsTotalPage = 1;
            return;
          }

          this.listItems = res.data.content.map((item, index) => {
            return {
              ...item,
              title: this.viewBy === "Product" ? item.name : item.partCode,
              id: item.id,
              active: index === 0,
              code: item.code,
            };
          });
          this.listItemsTotalPage = res.data.totalPages;
          this.selectedItemId = this.listItems[0]?.id;
        });
    },
    async fetchCapacityData(option) {
      if (!this.selectedItemId) {
        this.chartData = [];
        this.tableData = [];
        return;
      }

      await axios
        .get(`/api/supplychain/cap-pln`, {
          params: {
            filterCode: this.filterCode,
            sort: this.tableSort,
            timeScale: this.timeScale,
            timeValue: this.timeValue,
            fromDate: this.fromDate,
            toDate: this.toDate,
            viewBy: this.viewBy.toUpperCase(),
            id: this.selectedItemId,
            week: this.selectedWeek,
            month: this.selectedMonth,
          },
        })
        .then((res) => {
          if (option === "chart") {
            this.tableData = res.data.content;
            return;
          }

          this.chartData = res.data.chartItems;
          this.tableData = res.data.content;

          if (this.timeScale === "YEAR") {
            this.chartData = this.chartData.map((item) => {
              return {
                ...item,
                title: moment(item.title).format("MMM"),
              };
            });
          }
        });
    },
    resetCapacityData() {
      this.selectedWeek = "";
      this.selectedMonth = "";
      this.fetchCapacityData();
    },
    truncateText(text, length) {
      if (!text) return "";
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    clickViewByDropdownButton() {
      this.isOpendDatePicker = false;
      this.isOpendViewByDropdown = !this.isOpendViewByDropdown;
    },
    clickViewByDropdownOption(params) {
      this.viewBy = params.title;
      this.isOpendViewByDropdown = false;
    },
    clickDatePickerButton() {
      this.tempTimeScale = this.timeScale;
      this.tempDateRange = this.dateRange;
      this.isOpendViewByDropdown = false;
      this.isOpendDatePicker = true;
    },
    openDetailCapacityModal(viewBy, partId, supplierId) {
      this.detailCapacityModalInfoData = {
        viewBy,
        partId,
        supplierId,
        timeSettings: this.timeSettings,
      };
      this.isOpendDetailCapacityModal = true;
    },
    closeDatePickerModal() {
      this.isOpendDatePicker = false;
      this.tempTimeScale = "";
      this.tempDateRange = {
        start: null,
        end: null,
      };
    },
    closeDetailCapacityModal() {
      this.detailCapacityModalInfoData = {};
      this.isOpendDetailCapacityModal = false;
    },
    clickListItem(item) {
      if (this.selectedItemId === item.id) return;
      this.selectedItemId = item.id;
    },
    sortTable(key) {
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableSort = `${key},${this.isTableSortByDesc ? "desc" : "asc"}`;
      this.fetchCapacityData();
    },
    alignTdText(key) {
      return this.tableHeaderList.find((item) => item.key === key).align;
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
    getSearchValue(value) {
      this.searchQuery = value;
      this.listItemsCurrentPage = 1;
      this.fetchListItemData();
    },
    clickNextPageButton() {
      this.selectedWeek = "";
      this.selectedMonth = "";
      this.listItemsCurrentPage++;
      this.fetchListItemData();
    },
    clickPrevPageButton() {
      this.selectedWeek = "";
      this.selectedMonth = "";
      this.listItemsCurrentPage--;
      this.fetchListItemData();
    },
    clickChartItem(item) {
      if (this.timeScale === "YEAR") {
        this.selectedWeek = "";
        this.selectedMonth = item.target.dataItem.dataContext.timeValue;
      } else {
        this.selectedMonth = "";
        this.selectedWeek = item.target.dataItem.dataContext.timeValue;
      }

      this.fetchCapacityData("chart");

      this.chartData = this.chartData.map((data) => {
        if (item.target.dataItem.dataContext.title === data.title) {
          return {
            ...data,
            columnSettings: {},
          };
        }

        return {
          ...data,
          columnSettings: this.disabledColumnSettings,
        };
      });
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      const timeValue = moment(this.toDate).format("YYYY");
      const formattedValueY = target.dataItem.get("valueY")?.toLocaleString();

      return String.raw`
          <div class="chart-tooltip">
            <div class="chart-tooltip-title">{categoryX}, ${timeValue}</div>
            <div class="chart-tooltip-dividing-line"></div>
            <div class="chart-tooltip-contents">
              <span>{name}</span>
              <span>${formattedValueY}</span>
            </div>
          </div>
      `;
    },
    legendMarkerRectanglesStrokeDashArrayAdapter(strokeDasharray, target) {
      if (target.dataItem.dataContext.legendName === "Predicted Capacity") {
        return [2, 3];
      } else {
        return strokeDasharray;
      }
    },
    getCurrentQuarter() {
      const today = moment();
      const quarter = Math.floor((today.month() + 3) / 3);
      const quarterStart = moment().quarter(quarter).startOf("quarter");
      const quarterEnd = moment().quarter(quarter).endOf("quarter");

      this.timeScale = "QUARTER";
      this.fromDate = quarterStart.format("YYYYMMDD");
      this.toDate = quarterEnd.format("YYYYMMDD");
      this.dateRange = {
        start: moment(this.fromDate),
        end: moment(this.toDate),
      };
      this.displayDate = Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
      this.timeValue = Common.getDatePickerTimeValue(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
    },
    getTimeScale(timeScale) {
      this.tempTimeScale = timeScale;
    },
    getDateRange(dateRange) {
      this.tempDateRange = dateRange;
    },
    async clickDatePickerSaveButton() {
      this.timeScale = this.tempTimeScale;
      this.dateRange = this.tempDateRange;
      this.fromDate = this.tempDateRange.start.format("YYYYMMDD");
      this.toDate = this.tempDateRange.end.format("YYYYMMDD");
      this.displayDate = Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
      this.timeValue = Common.getDatePickerTimeValue(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
      this.selectedMonth = "";
      this.selectedWeek = "";
      this.tempTimeScale = "";
      this.tempDateRange = {
        start: null,
        end: null,
      };
      this.isOpendDatePicker = false;
      await this.fetchCapacityData();
    },
    getTableHeaderHeight() {
      return this.$refs.tableHeaderRef?.getBoundingClientRect().height;
    },
  },
  watch: {
    viewBy: {
      async handler(newVal) {
        this.viewByForQuery = newVal.toLowerCase() + "s";
        this.yAxisExtraLabelSet.text = `${this.viewBy} produced quantity`;
        this.searchbarPlaceholder =
          this.viewBy === "Product" ? "Search product name" : "Search part ID";
        await this.fetchAllData();
      },
    },
    selectedItemId: {
      async handler() {
        this.selectedWeek = "";
        this.selectedMonth = "";

        await this.fetchCapacityData();
      },
    },
    timeScale: {
      handler() {
        this.columnTemplateSet.width = this.timeScale === "MONTH" ? 60 : 30;
      },
    },
  },
  mounted() {
    window.addEventListener("resize", () => {
      this.getTableHeaderHeight();
    });
    this.getCurrentQuarter();
    this.fetchAllData();
  },
  beforeDestroy() {
    window.removeEventListener("resize", () => {
      this.getTableHeaderHeight();
    });
  },
};
</script>

<style scoped>
.cap-pln-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 32px 20px;
}

.cap-pln-top-bar {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-shrink: 0;
}

.cap-pln-filters-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: fit-content;
  gap: 8px;
}

.cap-pln-filters-container > span {
  font-size: 14px;
  line-height: 16px;
  color: #4b4b4b;
}

.cap-pln-main-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.cap-pln-main-left {
  width: 256px;
  height: 100%;
  flex-shrink: 0;
}

.cap-pln-list-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 100%;
  padding-top: 16px;
  background: #fbfcfd;
  border-radius: 3px;
}

.cap-pln-list-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  padding: 12px;
}

.cap-pln-main-right {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding-left: 20px;
}

.cap-pln-chart-container {
  width: 100%;
  height: 50%;
}

.cap-pln-chart-wrapper {
  width: 100%;
  height: calc(100% - 64px);
}

.cap-pln-chart-legend-wrapper {
  width: 100%;
  height: 64px;
  padding-top: 12px;
}

.cap-pln-table-container {
  display: flex;
  flex-direction: column;
  height: 50%;
}

.cap-pln-table-container th,
td {
  height: 34px !important;
  vertical-align: top;
}

.cap-pln-table-container th {
  word-break: normal;
}

.cap-pln-table-container td {
  word-break: break-all;
}

.cap-pln-table-th {
  z-index: 10;
  padding: 8px 10px 8px 16px;
}

.cap-pln-table-th > div {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
}

.cap-pln-custom-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
}

.cap-pln-table-sort-icon {
  display: flex;
  align-items: center;
  width: fit-content;
  height: 100%;
  cursor: pointer;
}

.cap-pln-table-empty {
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

.chart-tooltip {
  background-color: #ffffff;
  min-width: 188px;
  padding: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
  color: #4b4b4b;
}

.chart-tooltip > .chart-tooltip-title {
  font-size: 14px;
  font-weight: 700;
}

.chart-tooltip > .chart-tooltip-dividing-line {
  width: 100%;
  height: 1px;
  background-color: #000000;
  margin: 10px 0;
}

.chart-tooltip > .chart-tooltip-contents {
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.chart-tooltip > .chart-tooltip-contents > span {
  font-weight: 400;
  font-size: 14px;
}

.cap-pln-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cap-pln-table-tooltip-wrapper {
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

.cap-pln-table-tooltip-wrapper::after {
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

.cap-pln-show-more-icon {
  width: 10px;
  height: 10px;
  margin-right: 4px;
  margin-left: 4px;
}

.cap-pln-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding-left: 6px;
}

.cap-pln-badge-arrow-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
}

.cap-pln-badge-arrow-icon.active {
  transform: rotate(180deg);
}

.cap-pln-badge-arrow-icon.inactive {
  transform: rotate(180deg);
}

.cap-pln-badge-styles {
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
