<template>
  <div class="dem-cpl-page-container">
    <div class="dem-cpl-header-container">
      <div>
        <master-filter-wrapper
          :filter-code="param.filterCode"
          :notify-filter-updated="initializeData"
        ></master-filter-wrapper>
      </div>
      <div class="dem-cpl-header-right">
        <span>View By</span>
        <span>
          <emdn-cta-button
            button-type="dropdown"
            color-type="white"
            :active="isOpenedViewByDropdown"
            :click-handler="buttonToggleForProduct"
            style-props="height: 29px; margin: 0; box-sizing: border-box;"
            >{{ viewByDropdownTitle }}</emdn-cta-button
          >
          <emdn-dropdown
            id="view-by-dropdown"
            :visible="isOpenedViewByDropdown"
            :items="items"
            :click-handler="clickHandlerForProduct"
            :on-close="onCloseForProduct"
            style-props="margin-top: 2px;"
          ></emdn-dropdown>
        </span>
        <span>
          <emdn-cta-button
            button-type="date-picker"
            color-type="blue"
            :click-handler="toggleDatePicker"
            style-props="height: 29px; margin: 0; box-sizing: border-box;"
            >{{ selectedDateDisplayName }}</emdn-cta-button
          >
          <emdn-popover
            :visible="showCalendar"
            @close="closeClanderHandler"
            :position="{ right: '0px' }"
            style="margin-top: 2px"
          >
            <emdn-date-picker
              :time-scale="timeScale"
              :date-range="dateRange"
              :min-date="minDate"
              @get-date-range="getDateRange"
            ></emdn-date-picker>
          </emdn-popover>
        </span>
        <span>
          <emdn-cta-button
            color-type="blue-fill"
            :click-handler="inputDemandHandler"
            href="javascript:void(0)"
            style-props="height: 29px; margin: 0; box-sizing: border-box;"
            >Input Demand</emdn-cta-button
          >
        </span>
      </div>
    </div>

    <div class="dem-cpl-main-container">
      <div class="dem-cpl-main-left-container">
        <div class="dem-cpl-list-container">
          <emdn-list-search-bar
            :items="listItems"
            :click-handler="clickHandler"
            :search-handler="searchHandler"
            :placeholder="listBarPlaceHolder"
            style-props="padding: 0;"
          ></emdn-list-search-bar>
          <div class="dem-cpl-list-footer">
            <emdn-pagination
              :current-page="param.page"
              :total-page="totalPagesForListItems"
              :disabled="false"
              :next-click-handler="nextItemListClickHandler"
              :previous-click-handler="prevListItemClickHandler"
            >
            </emdn-pagination>
          </div>
        </div>
      </div>

      <div class="dem-cpl-main-right-container">
        <div class="dem-cpl-chart-container">
          <emdn-xy-chart
            style-props="width: 100%; height: 100%; min-width: 400px;"
            :y-axis-extra-label-set="yAxisExtraLabelSet"
            :data="chartData"
            :line-data-binder="lineDataBinder"
            :category="chartCategory"
            :legend-set="legendSet"
            :chart-set="chartSet"
            :root-number-formatter-set="rootNumberFormatterSet"
            :series-tooltip-label-html-adapter="
              (data) => seriesTooltipLabelHtmlAdapter(data)
            "
          >
          </emdn-xy-chart>
        </div>

        <div class="dem-cpl-table-container">
          <div class="dem-cpl-table-wrapper">
            <emdn-data-table :scrollable="true" ref="tableRef">
              <template #thead>
                <tr ref="tableHeaderRef">
                  <th
                    v-for="(tableHeader, index) in tableHeaderList"
                    :key="index"
                    style="z-index: 10; padding: 0.5rem 0.75rem 0.5rem 1rem"
                  >
                    <div
                      class="dem-cpl-th-wrapper"
                      :style="{ justifyContent: tableHeader.justifyContent }"
                    >
                      <span
                        :style="{
                          textAlign:
                            tableHeader.justifyContent === 'flex-start'
                              ? 'left'
                              : 'right',
                        }"
                        >{{ tableHeader.title }}</span
                      >
                      <div
                        class="dem-cpl-table-sort-icon"
                        @click.prevent.stop="sortTable(tableHeader.key)"
                      >
                        <emdn-icon
                          :button-type="
                            tableParam.sort === `${tableHeader.key},asc`
                              ? 'sort-asce'
                              : 'sort-desc'
                          "
                          style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.25rem;"
                          :active="tableParam.sort.includes(tableHeader.key)"
                        ></emdn-icon>
                      </div>
                    </div>
                  </th>
                </tr>
              </template>
              <template #tbody>
                <div
                  v-if="!tableData.length"
                  class="dem-cpl-table-empty"
                  :style="{
                    height: `calc(100% - ${tableHeaderHeight}px)`,
                  }"
                >
                  No Data Available
                </div>
                <template v-else-if="tableParam.viewBy === 'PRODUCT'">
                  <tr v-for="(item, index) in tableData">
                    <td>
                      <div
                        @mouseover.stop="(e) => setTooltip(e, item.partCode)"
                        @mouseleave.stop="initializeTableTooltip"
                        class="dem-cpl-truncate-text"
                      >
                        {{ item.partCode }}
                      </div>
                    </td>
                    <td>
                      <div style="display: flex; align-items: flex-start">
                        <div
                          @mouseover.stop="
                            (e) => setTooltip(e, item.suppliers[0]?.name)
                          "
                          @mouseleave.stop="initializeTableTooltip"
                          class="dem-cpl-truncate-text"
                        >
                          {{ item.suppliers[0]?.name }}
                        </div>
                        <emdn-dropdown-root
                          v-if="item.suppliers.length > 1"
                          :on-dropdown-close="onBadgeDropdownClose"
                          position="auto"
                          max-height="12.5rem"
                        >
                          <emdn-dropdown-trigger>
                            <div
                              class="dem-cpl-badge-wrapper"
                              @click="
                                showBadgeDropdown(item.suppliers[0]?.id, index)
                              "
                            >
                              <span
                                class="dem-cpl-badge-arrow-icon"
                                :class="
                                  visibleBadgeDropdownId ===
                                    item.suppliers[0]?.id &&
                                  visibleBadgeDropdownIndex === index
                                    ? 'active'
                                    : ''
                                "
                              >
                                <img src="/images/more.png" class="show-more" />
                              </span>
                              <span
                                :style="badgeStyle"
                                class="dem-cpl-badge-styles"
                              >
                                +{{ item.suppliers.length - 1 }}
                              </span>
                            </div>
                          </emdn-dropdown-trigger>
                          <emdn-dropdown-portal>
                            <emdn-dropdown-content>
                              <emdn-dropdown-item
                                v-for="(supplier, index) in item?.suppliers"
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
                      <div style="display: flex; align-items: flex-start">
                        <div
                          @mouseover.stop="
                            (e) => setTooltip(e, item.plants[0]?.name)
                          "
                          @mouseleave.stop="initializeTableTooltip"
                          class="dem-cpl-truncate-text"
                        >
                          {{ item.plants[0]?.name }}
                        </div>
                        <emdn-dropdown-root
                          v-if="item.plants.length > 1"
                          :on-dropdown-close="onBadgeDropdownClose"
                          position="auto"
                          max-height="12.5rem"
                        >
                          <emdn-dropdown-trigger>
                            <div
                              class="dem-cpl-badge-wrapper"
                              @click="
                                showBadgeDropdown(item.plants[0]?.id, index)
                              "
                            >
                              <span
                                class="dem-cpl-badge-arrow-icon"
                                :class="
                                  visibleBadgeDropdownId ===
                                    item.plants[0]?.id &&
                                  visibleBadgeDropdownIndex === index
                                    ? 'active'
                                    : ''
                                "
                              >
                                <img src="/images/more.png" class="show-more" />
                              </span>
                              <span
                                :style="badgeStyle"
                                class="dem-cpl-badge-styles"
                              >
                                +{{ item.plants.length - 1 }}
                              </span>
                            </div>
                          </emdn-dropdown-trigger>
                          <emdn-dropdown-portal>
                            <emdn-dropdown-content>
                              <emdn-dropdown-item
                                v-for="(plant, index) in item?.plants"
                                v-if="index !== 0"
                                :key="index"
                                :text="plant.name"
                              ></emdn-dropdown-item>
                            </emdn-dropdown-content>
                          </emdn-dropdown-portal>
                        </emdn-dropdown-root>
                      </div>
                    </td>
                    <td>
                      <div style="text-align: right">
                        <span>
                          {{ item.producedQuantity?.toLocaleString() }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div style="text-align: right">
                        <span>
                          {{ item.productionDemand?.toLocaleString() }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div class="demand-status-icon">
                        <span
                          :style="{
                            background: demandStatusIconColor(
                              item?.demandCompliance
                            ),
                          }"
                        ></span>
                        <span>{{
                          capitalizeFirstLetter(item?.demandCompliance)
                        }}</span>
                      </div>
                    </td>
                  </tr>
                </template>
                <template v-else-if="tableParam.viewBy === 'PART'">
                  <tr v-for="(item, index) in tableData">
                    <td>
                      <div
                        @mouseover.stop="
                          (e) => setTooltip(e, item.suppliers[0]?.name)
                        "
                        @mouseleave.stop="initializeTableTooltip"
                        class="dem-cpl-truncate-text"
                      >
                        {{ item.suppliers[0]?.name }}
                      </div>
                    </td>
                    <td>
                      <div style="display: flex; align-items: flex-start">
                        <div
                          @mouseover.stop="
                            (e) => setTooltip(e, item.plants[0]?.name)
                          "
                          @mouseleave.stop="initializeTableTooltip"
                          class="dem-cpl-truncate-text"
                        >
                          {{ item.plants[0]?.name }}
                        </div>
                        <emdn-dropdown-root
                          v-if="item.plants.length > 1"
                          :on-dropdown-close="onBadgeDropdownClose"
                          position="auto"
                          max-height="12.5rem"
                        >
                          <emdn-dropdown-trigger>
                            <div
                              class="dem-cpl-badge-wrapper"
                              @click="
                                showBadgeDropdown(item.plants[0]?.id, index)
                              "
                            >
                              <span
                                class="dem-cpl-badge-arrow-icon"
                                :class="
                                  visibleBadgeDropdownId ===
                                    item.plants[0]?.id &&
                                  visibleBadgeDropdownIndex === index
                                    ? 'active'
                                    : ''
                                "
                              >
                                <img src="/images/more.png" class="show-more" />
                              </span>
                              <span
                                :style="badgeStyle"
                                class="dem-cpl-badge-styles"
                              >
                                +{{ item.plants.length - 1 }}
                              </span>
                            </div>
                          </emdn-dropdown-trigger>
                          <emdn-dropdown-portal>
                            <emdn-dropdown-content>
                              <emdn-dropdown-item
                                v-for="(plant, index) in item?.plants"
                                v-if="index !== 0"
                                :key="index"
                                :text="plant.name"
                              ></emdn-dropdown-item>
                            </emdn-dropdown-content>
                          </emdn-dropdown-portal>
                        </emdn-dropdown-root>
                      </div>
                    </td>
                    <td>
                      <div
                        style="
                          display: flex;
                          justify-content: flex-end;
                          align-items: flex-start;
                        "
                      >
                        <emdn-cta-button
                          variant="text"
                          style-props="width: fit-content; height: fit-content; padding: 0"
                          :click-handler="
                            () => openDetailModal(item.partId, item.suppliers)
                          "
                        >
                          {{ item.moldCount }}
                        </emdn-cta-button>
                      </div>
                    </td>
                    <td>
                      <div style="text-align: right">
                        <span>
                          {{ item.producedQuantity?.toLocaleString() }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div style="text-align: right">
                        <span>
                          {{ item.productionDemand?.toLocaleString() }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div>
                        <div class="demand-status-icon">
                          <span
                            :style="{
                              background: demandStatusIconColor(
                                item?.demandCompliance
                              ),
                            }"
                          ></span>
                          <span>{{
                            capitalizeFirstLetter(item?.demandCompliance)
                          }}</span>
                        </div>
                      </div>
                    </td>
                  </tr>
                </template>
              </template>
            </emdn-data-table>
          </div>
        </div>
      </div>
      <div
        v-if="isOpenedTableTooltip"
        :style="tableTooltipPosition"
        class="dem-cpl-table-tooltip-wrapper"
        ref="tooltipRef"
      >
        {{ tableTooltipInfo.text }}
      </div>
    </div>
    <total-tooling-detail-modal
      v-if="isOpenedDetailModal"
      :is-modal-opened="isOpenedDetailModal"
      :params="detailModalParams"
      :close-modal="closeDetailModal"
      :time-range="selectedDateDisplayName"
    />
  </div>
</template>

<script>
let timer;

module.exports = {
  name: "dem-cpl-page",
  components: {
    "total-tooling-detail-modal": httpVueLoader(
      "/components/pages/supplychain/dem-cpl/total-tooling-detail-modal.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      resizeTimer: null,
      tableHeaderList: [],
      tableHeaderListByProduct: [
        {
          id: 1,
          title: "Part ID",
          key: "partCode",
          justifyContent: "flex-start",
        },
        {
          id: 2,
          title: "Supplier",
          key: "suppliers",
          justifyContent: "flex-start",
        },
        {
          id: 3,
          title: "Plant",
          key: "plants",
          justifyContent: "flex-start",
        },
        {
          id: 4,
          title: "Acc. Weekly Production",
          key: "producedQuantity",
          justifyContent: "flex-end",
        },
        {
          id: 5,
          title: "Weekly Demand",
          key: "productionDemand",
          justifyContent: "flex-end",
        },
        {
          id: 6,
          title: "Demand Compliance",
          key: "demandCompliance",
          justifyContent: "flex-start",
        },
      ],
      tableHeaderListByPart: [
        {
          id: 1,
          title: "Supplier",
          key: "suppliers",
          justifyContent: "flex-start",
        },
        {
          id: 2,
          title: "Plant",
          key: "plants",
          justifyContent: "flex-start",
        },
        {
          id: 3,
          title: "Total Tooling",
          key: "moldCount",
          justifyContent: "flex-end",
        },
        {
          id: 4,
          title: "Acc. Weekly Production",
          key: "producedQuantity",
          justifyContent: "flex-end",
        },
        {
          id: 5,
          title: "Weekly Demand",
          key: "productionDemand",
          justifyContent: "flex-end",
        },
        {
          id: 6,
          title: "Demand Compliance",
          key: "demandCompliance",
          justifyContent: "flex-start",
        },
      ],
      tableTooltipInfo: {
        text: null,
        top: null,
        left: null,
      },
      isOpenedTableTooltip: false,
      tableHeaderHeight: 0,
      isTableSortByDesc: true,
      masterFilterPlaceHolder: "Search Filter",
      listBarPlaceHolder: "Search product name",
      totalPagesForListItems: 1,
      listItems: [],
      badgeStyle: {
        width: "20px",
        height: "20px",
      },
      tableData: [],
      currentSortType: "",
      isDesc: true,
      isOpenedViewByDropdown: false,
      items: [
        { title: "Product", id: "1", placeholder: "Search product name" },
        { title: "Part", id: "2", placeholder: "Search part ID" },
      ],

      itemList: [
        { title: "Input Demand", id: "1" },
        { title: "Import Demand", id: "2" },
      ],
      showCalendar: false,
      timeScale: "WEEK",
      dateRange: {
        start: moment(),
        end: moment(),
      },
      minDate: null,
      toDate: "",
      fromDate: "",
      yAxisExtraLabelSet: {
        text: "Total Part Produced",
      },
      chartSet: {
        valueAxis: {
          min: 0,
        },
      },
      rootNumberFormatterSet: {
        numberFormat: "#,###.",
      },
      legendSet: { forceHidden: true },
      lineDataBinder: [],
      chartCategory: "title",
      chartData: [],

      param: {
        filterCode: "COMMON",
        query: "",
        sort: "",
        page: 1,
        size: 10,
      },
      tableParam: {
        sort: "",
        viewBy: "",
        id: null,
      },
      visibleBadgeDropdownId: null,
      visibleBadgeDropdownIndex: null,
      isOpenedDetailModal: false,
      detailModalParams: {
        partId: null,
        supplierId: null,
        timeScale: null,
        fromDate: null,
        toDate: null,
        timeValue: null,
      },
    };
  },
  computed: {
    selectedDateDisplayName() {
      return Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.timeScale.toUpperCase()
      );
    },
    viewByDropdownTitle() {
      const viewBy = this.tableParam.viewBy;
      return viewBy.charAt(0).toUpperCase() + viewBy.slice(1).toLowerCase();
    },
    viewByForQuery() {
      switch (this.tableParam.viewBy) {
        case "PRODUCT":
          return "products";
        case "PART":
          return "parts";
        default:
          return "";
      }
    },
    timeSettings() {
      return {
        timeScale: this.timeScale.toUpperCase(),
        fromDate: this.fromDate,
        toDate: this.toDate,
        timeValue: Common.getDatePickerTimeValue(
          this.fromDate,
          this.toDate,
          this.timeScale.toUpperCase()
        ),
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
    getInitialTimeSettings() {
      const today = moment();
      const startOfWeek = today.clone().startOf("isoWeek");
      const endOfWeek = today.clone().endOf("isoWeek");

      this.fromDate = startOfWeek.format("YYYYMMDD");
      this.toDate = endOfWeek.format("YYYYMMDD");
    },
    getListItems(listItemId) {
      axios
        .get(`/api/supplychain/dem-cpl/${this.viewByForQuery}`, {
          params: { ...this.param, ...this.timeSettings },
        })
        .then((response) => {
          this.listItems = response.data.content.map((item, index) => {
            if (listItemId) {
              return {
                title:
                  this.tableParam.viewBy === "PRODUCT"
                    ? item.name
                    : item.partCode,
                id: item.id,
                active: item.id === listItemId,
                isLow: item.compLevel === "LOW",
                code: item.code,
              };
            }

            return {
              title:
                this.tableParam.viewBy === "PRODUCT"
                  ? item.name
                  : item.partCode,
              id: item.id,
              active: index === 0,
              isLow: item.compLevel === "LOW",
              code: item.code,
            };
          });
          this.tableParam.id = listItemId ? listItemId : this.listItems[0]?.id;
          this.totalPagesForListItems = response.data.totalPages
            ? response.data.totalPages
            : 1;

          if (!listItemId) this.getData();
        })
        .catch((error) => {
          console.log(error);
        });
    },
    searchHandler(value) {
      this.param.query = value;
      this.paging(1);
    },
    closeClanderHandler() {
      this.showCalendar = false;
    },
    checkStatusColor(viewBy, code, content) {
      let filteredItem = [];

      if (viewBy === "PRODUCT") {
        filteredItem = content.filter((item) => item.partCode === code)[0];
      }

      if (viewBy === "PART") {
        filteredItem = content.filter(
          (item) => item.suppliers[0].companyCode === code
        )[0];
      }

      if (filteredItem) {
        const complianceLevel = filteredItem["demandCompliance"];
        if (complianceLevel === "LOW") {
          return "#E34537";
        } else if (complianceLevel === "MEDIUM") {
          return "#F7CC57";
        } else {
          return "#D3D1D9";
        }
      }
    },
    openDetailModal(partId, suppliers) {
      this.detailModalParams = {
        partId,
        supplierId: suppliers.map((item) => item.id).join(","),
        ...this.timeSettings,
      };
      this.isOpenedDetailModal = true;
    },
    closeDetailModal() {
      this.detailModalParams = null;
      this.isOpenedDetailModal = false;
    },
    buttonToggleForProduct() {
      this.isOpenedViewByDropdown = !this.isOpenedViewByDropdown;
      this.showCalendar = false;
    },
    clickHandlerForProduct(item) {
      this.product = item.title;
      this.listBarPlaceHolder = item.placeholder;
      this.isOpenedViewByDropdown = false;
      if (item.title === "Product") {
        this.tableParam.viewBy = "PRODUCT";
        window.location.hash = "products";
      } else {
        this.tableParam.viewBy = "PART";
        window.location.hash = "parts";
      }
      this.showCalendar = false;
    },
    onCloseForProduct() {
      this.isOpenedViewByDropdown = false;
    },
    toggleDatePicker() {
      this.showCalendar = !this.showCalendar;
      this.isOpenedViewByDropdown = false;
    },
    getDateRange(dateRange) {
      this.fromDate = dateRange.start.format("YYYYMMDD");
      this.toDate = dateRange.end.format("YYYYMMDD");
      this.dateRange = dateRange;
      this.getListItems(this.tableParam.id);
      this.getData();
    },
    inputDemandHandler() {
      window.location.href = `/supplychain/dem-cpl/input#${this.viewByForQuery}`;
    },
    nextItemListClickHandler() {
      this.paging(this.param.page + 1);
    },
    prevListItemClickHandler() {
      this.paging(this.param.page - 1);
    },
    paging(pageNumber) {
      this.param.page = pageNumber === undefined ? 1 : pageNumber;
      this.getListItems();
    },
    clickHandler(item) {
      this.tableParam.id = item.id;
      this.getData();
    },
    getData() {
      if (!this.tableParam.id) return;

      axios
        .get("/api/supplychain/dem-cpl", {
          params: { ...this.tableParam, ...this.timeSettings },
        })
        .then((response) => {
          const data = response.data;

          if (!data) {
            this.tableData = [];
            this.chartData = [];
            return;
          }

          this.lineDataBinder = data.chartItems.map((item) => {
            if (this.tableParam.viewBy === "PRODUCT") {
              return {
                key: item.partId,
                displayName: item.partCode,
                color: this.checkStatusColor(
                  this.tableParam.viewBy,
                  item.partCode,
                  data.content
                ),
              };
            }

            if (this.tableParam.viewBy === "PART") {
              return {
                key: item.supplierId,
                displayName: item.supplierName,
                color: this.checkStatusColor(
                  this.tableParam.viewBy,
                  item.supplierCode,
                  data.content
                ),
              };
            }
          });

          this.chartData = data.chartItems2;
          this.tableData = data.content;
        })
        .catch((error) => {
          console.log("error", error);
          this.tableData = [];
          this.chartData = [];
        });
    },
    initializeData() {
      this.tableData = [];
      this.chartData = [];
      this.lineDataBinder = [];
      this.tableParam = {
        viewBy: this.tableParam.viewBy,
        id: "",
        sort: this.tableParam.sort,
      };

      this.paging(1);
      this.getData();
    },
    seriesTooltipLabelHtmlAdapter(data, dataBinder) {
      return String.raw`
        <div class="dem-cpl-chart-tooltip">
          <div class="dem-cpl-chart-tooltip-title">{name}</div>
          <div class="dem-cpl-chart-tooltip-dividing-line"></div>
          <div class="dem-cpl-chart-tooltip-contents">
            <span>{categoryX}</span>
            <span>{valueY}</span>
          </div>
        </div>
    `;
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
    tableScrollToTop() {
      const tableWrapperRef = this.$refs.tableRef?.tableWrapper;

      if (
        tableWrapperRef &&
        tableWrapperRef.scrollHeight > tableWrapperRef.clientHeight
      ) {
        tableWrapperRef.scrollTo({ top: 0, behavior: "smooth" });
      }
    },
    sortTable(key) {
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableParam.sort = `${key},${
        this.isTableSortByDesc ? "desc" : "asc"
      }`;
      this.getData();
      this.tableScrollToTop();
    },
    getTableHeaderHeight() {
      if (this.resizeTimer) clearTimeout(this.resizeTimer);
      this.resizeTimer = setTimeout(() => {
        this.tableHeaderHeight =
          this.$refs.tableHeaderRef?.getBoundingClientRect().height;
      }, 100);
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
    demandStatusIconColor(status) {
      switch (status) {
        case "HIGH":
          return "#41CE77";
        case "MEDIUM":
          return "#F7CC57";
        case "LOW":
          return "#E34537";
        default:
          return "";
      }
    },
    capitalizeFirstLetter(str) {
      if (!str) return "";
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    },
    onBadgeDropdownClose() {
      this.visibleBadgeDropdownId = null;
      this.visibleBadgeDropdownIndex = null;
    },
  },
  watch: {
    viewByForQuery() {
      this.tableHeaderList =
        this.viewByForQuery === "products"
          ? this.tableHeaderListByProduct
          : this.tableHeaderListByPart;
      this.paging(1);
      this.visibleBadgeDropdownId = null;
      this.showCalendar = false;
    },
  },
  mounted() {
    window.addEventListener("resize", this.getTableHeaderHeight, {
      passive: true,
    });

    this.tableHeaderList = this.tableHeaderListByProduct;
    this.getInitialTimeSettings();

    if (window.location.hash === "#parts") {
      this.tableParam.viewBy = "PART";
    } else {
      window.location.hash = "products";
      this.tableParam.viewBy = "PRODUCT";
    }

    this.$nextTick(() => {
      this.tableHeaderHeight =
        this.$refs.tableHeaderRef?.getBoundingClientRect().height;
    });
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.getTableHeaderHeight);
  },
};
</script>

<style scoped>
.dem-cpl-chart-tooltip {
  background-color: #ffffff;
  min-width: 188px;
  padding: 10px;
  box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
  color: #4b4b4b;
}

.dem-cpl-chart-tooltip > .dem-cpl-chart-tooltip-title {
  font-size: 15px;
  font-weight: 700;
}

.dem-cpl-chart-tooltip > .dem-cpl-chart-tooltip-dividing-line {
  width: 100%;
  height: 0.2px;
  background-color: #000000;
  margin: 10px 0;
}

.dem-cpl-chart-tooltip > .dem-cpl-chart-tooltip-contents {
  display: flex;
  justify-content: space-between;
}

.dem-cpl-chart-tooltip > .dem-cpl-chart-tooltip-contents > span {
  font-weight: 400;
  font-size: 14px;
}

.dem-cpl-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dem-cpl-list-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  padding: 12px;
}

.show-more {
  width: 10px;
  height: 10px;
  margin-right: 4px;
  margin-left: 4px;
}

.dem-cpl-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding-left: 6px;
}

.dem-cpl-badge-arrow-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
}

.dem-cpl-badge-arrow-icon.active {
  transform: rotate(180deg);
}

.dem-cpl-badge-arrow-icon.inactive {
  transform: rotate(180deg);
}

.dem-cpl-badge-styles {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #3491ff;
  color: #ffffff;
  font-size: 11.25px;
  font-style: normal;
}

.dem-cpl-page-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 32px 20px;
}

.dem-cpl-header-container {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-shrink: 0;
}

.dem-cpl-header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: fit-content;
  gap: 8px;
}

.dem-cpl-main-container {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 620px;
}

.dem-cpl-main-left-container {
  width: 256px;
  height: 100%;
  flex-shrink: 0;
}

.dem-cpl-list-container {
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

.dem-cpl-main-right-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding-left: 20px;
}

.dem-cpl-chart-container {
  width: 100%;
  height: 50%;
  padding-top: 24px;
  padding-bottom: 38px;
}

.dem-cpl-table-container {
  width: 100%;
  height: 50%;
  display: flex;
  flex-direction: column;
}

.dem-cpl-table-wrapper {
  height: 100%;
  width: 100%;
}

.dem-cpl-table-wrapper td {
  vertical-align: top;
  font-size: 0.916rem;
}

.dem-cpl-th-wrapper {
  display: flex;
  align-items: center;
}

.dem-cpl-table-sort-icon {
  display: flex;
  align-items: center;
  width: fit-content;
  height: 100%;
  cursor: pointer;
}

.dem-cpl-table-empty {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #efefef;
  position: absolute;
  font-family: Helvetica Neue, Helvetica, Arial, sans-serif;
  font-size: 0.75rem;
  color: #4b4b4b;
}

.dem-cpl-table-tooltip-wrapper {
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

.dem-cpl-table-tooltip-wrapper:after {
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

.dem-cpl-header-right:first-child {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 17px;
  color: #4b4b4b;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.badge-dropdown-wrapper {
  width: 100%;
  height: auto;
  box-shadow: rgba(0, 0, 0, 0.25) 0px 1px 4px 0px;
  border-radius: 3px;
  margin-top: 8px;
  background: #ffffff;
}

.badge-dropdown-wrapper > ul {
  margin: 0;
  padding: 2px;
  min-width: 100px;
}

.badge-dropdown-wrapper > ul > li {
  font-size: 14px;
  padding: 8px 6px;
  word-break: break-word;
  white-space: normal;
}

.badge-dropdown-wrapper > ul > li:hover {
  background: #e6f7ff;
}

.demand-status-icon {
  display: flex;
  align-items: center;
  width: fit-content;
  height: fit-content;
  gap: 4px;
}

.demand-status-icon > span:nth-child(1) {
  width: 13px;
  height: 13px;
  border-radius: 25px;
}

.demand-status-icon > span:nth-child(2) {
  font-size: 14.66px;
}
</style>
