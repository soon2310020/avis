<template>
  <div style="flex: 1; display: flex; flex-direction: column">
    <!-- CHART -->
    <emdn-xy-chart
      :data="chartData"
      :chart-set="chartSet"
      :bar-data-binder="barDataBinder"
      category="supplierName"
      :column-set="columnSet"
      :column-template-set="columnTemplateSet"
      :previous-button="previousButton"
      :next-button="nextButton"
      :legend-set="legendSet"
      :series-tooltip-h-t-m-l-adapter="seriesTooltipHTMLAdapter"
      :style-props="chartStyles"
      style="flex: 1"
    >
    </emdn-xy-chart>
    <emdn-data-table
      ref="data-table"
      bg-color="#F5F8FF"
      class="ctd-data-table"
      style="display: flex; flex-direction: column; flex: 1; position: relative"
      :column-width-array="columnWidthArray"
    >
      <template #header>
        <tr>
          <th
            v-for="(column, columnIndex) in displayColumns"
            :key="column.field"
            :class="[
              `text-${column.textAlign}`,
              `cell-${column.field}`,
              { 'cell-active': column.field === sort.field },
            ]"
          >
            <div class="cell-inner">
              <span class="cell-inner__label"> {{ column.label }} </span>
              <span
                v-if="column.sortable"
                class="cell-inner__sort"
                :class="{ desc: sort.desc && column.field === sort.field }"
                @click="() => handleSort(column.field)"
              >
                <span class="cell-inner__sort-icon"></span>
              </span>
              <span v-if="column.tooltip" class="cell-inner__tooltip">
                <a-tooltip placement="bottom">
                  <template slot="title">{{ column.tooltip }}</template>
                  <img
                    src="/images/icon/tooltip_info.svg"
                    width="15"
                    height="15"
                  />
                </a-tooltip>
              </span>
            </div>
          </th>
        </tr>
      </template>
      <template #body>
        <tr v-if="!data.length" class="cell-no-data">
          <td colspan="100">
            <div
              style="
                height: 150px;
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              No data
            </div>
          </td>
        </tr>
        <tr v-for="(row, rowIndex) in data" class="data-grid__row">
          <td
            v-for="(column, columnIndex) in displayColumns"
            :key="`${rowIndex}-${columnIndex}`"
            class="data-grid__cell"
            :class="[`text-${column.textAlign}`]"
          >
            <div
              class="cell-inner"
              :class="{ hyperlink: column.hyperlink }"
              @click="() => column.onClick && column.onClick(row, column)"
            >
              <span
                v-if="column.trend && !Number.isNaN(row[column.field])"
                class="inner-trend"
              >
                <span
                  class="trend-icon"
                  :class="getTrendClass(row[column.field])"
                ></span>
              </span>
              <span class="inner-text">
                <!-- NEED TRUNCATED -->
                <emdn-tooltip
                  v-if="row[column.field]?.length > column.maxLength"
                  position="right"
                  style-props="white-space: nowrap;"
                >
                  <template #context>
                    <span>
                      {{ row[column.field].slice(0, column.maxLength) + "..." }}
                    </span>
                  </template>
                  <template #body>
                    <span>{{ row[column.field] }}</span>
                  </template>
                </emdn-tooltip>
                <!-- DEFAULT CASE -->
                <span v-else>
                  {{
                    column.formatter
                      ? column.formatter(row[column.field])
                      : row[column.field]
                  }}
                </span>
                <span v-show="column.popup" class="popup-icon"></span>
              </span>
            </div>
          </td>
        </tr>
      </template>
      <!-- <template #footer>
        <div v-if="pagination.totalPages > 1" class="pagination d-flex">
          <div class="pagination-page">
            <span>{{ `${pagination.page} of ${pagination.totalPages}` }}</span>
          </div>
          <div class="pagination-arrow">
            <a
              href="javascript:void(0)"
              class="pagination-button"
              :class="{ 'inactive-button': pagination.page <= 1 }"
              @click="handleChangePage('PREV')"
            >
              <img src="/images/icon/category/paging-arrow.svg" alt="" />
            </a>
            <a
              href="javascript:void(0)"
              class="pagination-button"
              @click="handleChangePage('NEXT')"
              :class="{
                'inactive-button': pagination.page === pagination.totalPages,
              }"
            >
              <img
                src="/images/icon/category/paging-arrow.svg"
                style="transform: rotate(180deg)"
                alt=""
              />
            </a>
          </div>
        </div>
      </template> -->
    </emdn-data-table>
    <div v-if="pagination.totalPages > 1" class="pagination d-flex">
      <div class="pagination-page">
        <span>{{ `${pagination.page} of ${pagination.totalPages}` }}</span>
      </div>
      <div class="pagination-arrow">
        <a
          href="javascript:void(0)"
          class="pagination-button"
          :class="{ 'inactive-button': pagination.page <= 1 }"
          @click="handleChangePage('PREV')"
        >
          <img src="/images/icon/category/paging-arrow.svg" alt="" />
        </a>
        <a
          href="javascript:void(0)"
          class="pagination-button"
          @click="handleChangePage('NEXT')"
          :class="{
            'inactive-button': pagination.page === pagination.totalPages,
          }"
        >
          <img
            src="/images/icon/category/paging-arrow.svg"
            style="transform: rotate(180deg)"
            alt=""
          />
        </a>
      </div>
    </div>
  </div>
</template>

<script>
const MAPPING_FIELD_NAME = {
  ["Below Tolerance"]: "belowToleranceSc",
  ["Within Lower L2 Tolerance"]: "withinLowerL2ToleranceSc",
  ["Within L1 Tolerance"]: "withinL1ToleranceSc",
  ["Within Upper L2 Tolerance"]: "withinUpperL2ToleranceSc",
  ["Above Tolerance"]: "aboveToleranceSc",
};

module.exports = {
  props: {
    resources: Object,
    filter: Object,
    data: Array,
    pagination: Object,
    allColumnsList: Array,
    isLoading: Boolean,
  },
  data() {
    return {
      previousButton: {
        visible: true,
        disabled: true,
        handler: () => this.previousHandler(),
      },
      nextButton: {
        visible: true,
        disabled: true,
        handler: () => this.nextHandler(),
      },
      category: "supplierName",
      barDataBinder: [
        {
          key: "belowTolerance",
          displayName: "Below Tolerance",
          color: "#F7CC57",
        },
        {
          key: "withinLowerL2Tolerance",
          displayName: "Within Lower L2 Tolerance",
          color: "#2B8C50",
        },
        {
          key: "withinL1Tolerance",
          displayName: "Within L1 Tolerance",
          color: "#41CE77",
        },
        {
          // TODO: RECHECK WITH BE AND DESIGN
          key: "withinUpperL2Tolerance",
          displayName: "Within Upper L2 Tolerance",
          color: "#005922",
        },
        {
          key: "aboveTolerance",
          displayName: "Above Tolerance",
          color: "#E34537",
        },
      ],
      chartSet: {
        yAxis: {
          grid: {
            strokeWidth: 1,
            strokeOpacity: 0.1,
            location: 0,
          },
        },
        yAxisList: [
          {
            extraLabelText: "Proportion of Shot",
          },
        ],
        valueAxis: {
          isPercent: true,
        },
        xAxis: {
          grid: {
            strokeWidth: 1,
            strokeOpacity: 0.1,
            location: 0,
          },
        },
      },
      columnSet: {
        stacked: true,
        clustered: true,
      },
      columnTemplateSet: {
        strokeWidth: 1.5,
        width: 100,
        fillOpacity: 1,
      },
      chartData: [],
      legendSet: {
        isVisible: true,
        paddingLeft: 60,
      },
      columnWidthArray: ["15%", "15%", "20%", "20%", "20%", "10%"],
      objectType: "CYCLE_TIME_DEVIATION",
      sort: {
        field: "",
        desc: false,
      },
      showCycleTimeDeviationDetail: false,
      selectedRecord: null,
      dataTableRef: null,
      dataTableBodyRef: null,
      chartHeight: 300,
    };
  },
  computed: {
    displayColumns() {
      if (this.allColumnsList?.[0]?.hasOwnProperty("enabled"))
        return this.allColumnsList.filter((item) => item.enabled);
      else return this.allColumnsList.filter((item) => item.default);
    },
    chartStyles() {
      return `height: ${this.chartHeight}px; width: 100%;`;
    },
  },
  methods: {
    previousHandler: function () {
      console.log("previous Handler");
      if (this.pagination.page > 1) this.pagination.page--;
    },
    nextHandler: function () {
      console.log("next Handler");
      if (this.pagination.page < this.pagination.totalPages)
        this.pagination.page++;
    },
    getTrendClass(trendValue) {
      if (trendValue === 0) return "balance-trend";
      if (trendValue > 0) return "up-trend";
      if (trendValue < 0) return "down-trend";
      return; // case null
    },
    handleSort(sortField) {
      if (this.sort.field === sortField) this.sort.desc = !this.sort.desc;
      this.sort.field = sortField;
      this.$emit("sort", this.sort);
    },
    handleChangePage(type) {
      if (!this.isLoading) {
        if (type === "NEXT") {
          this.pagination.page++;
        }
        if (type === "PREV") {
          this.pagination.page--;
        }
      }
    },
    seriesTooltipHTMLAdapter(tooltipHTML, target) {
      console.log("name", target.dataItem.component.get("name"));
      const name = target.dataItem.component.get("name");
      const item = target.dataItem.dataContext;
      const value = item[MAPPING_FIELD_NAME[name]];

      const tooltipStyles =
        "width: 268px; display: flex; justify-content: space-between; align-items: center; padding: 10px;";
      const titleStyles = "color: #4b4b4b;";
      const valueStyles = "color: #4b4b4b;";
      const unit = value > 1 ? "Shots" : "Shot";
      const html = `
        <div class="custom-tooltip" style="${tooltipStyles}">
          <div class="custom-tooltip__title" style="${titleStyles}">{name}</div>
          <div class="custom-tooltip__value" style="${valueStyles}">
            <span class="custom-tooltip__count">${Common.formatter.appendThousandSeperator(
              value
            )}</span>
            <span class="custom-tooltip__unit">${unit}</span>
          </div>
        </div>
      `;
      return html;
    },
    resizeTable() {
      const tableHeader = this.dataTableRef.$el.children[0];
      const tableHeaderHeight = tableHeader.clientHeight;
      const tableHeight = this.dataTableRef.$el.clientHeight;
      this.dataTableBodyRef.style.maxHeight =
        tableHeight - tableHeaderHeight + "px";
      const chartSize = { lg: 300, md: 250, sm: 200 };
      if (innerHeight > 900) {
        this.chartHeight = chartSize.lg;
      } else if (innerHeight > 800) {
        this.chartHeight = chartSize.md;
      } else {
        this.chartHeight = chartSize.sm;
      }
    },
  },
  watch: {
    filter(newVal) {
      console.log("@filter", newVal);
      if (newVal?.sort) this.sort.field = newVal.sort.split(".")[0];
    },
    data(newVal) {
      console.log("@data", newVal);
      console.log("@chartData", this.chartData);

      this.chartData = newVal;
    },
    pagination: {
      handler(newVal) {
        console.log("pagination", newVal);
        this.previousButton = Object.assign({}, this.previousButton, {
          visible: true,
          disabled: newVal.page <= 1,
        });
        this.nextButton = Object.assign({}, this.nextButton, {
          visible: true,
          disabled: newVal.page >= newVal.totalPages,
        });
      },
      deep: true,
    },
  },
  created() {
    this.MAX_LENGTH = 40;
  },
  mounted() {
    const dataTableEl = this.$refs["data-table"];
    if (dataTableEl) {
      this.dataTableRef = dataTableEl;
      this.dataTableBodyRef = dataTableEl.$refs.bodyRef;
      this.$nextTick(() => {
        this.resizeTable();
      });
      window.addEventListener("resize", this.resizeTable);
    }
  },
};
</script>

<style scoped>
.ctd-data-table {
  flex: 1;
  margin-top: 20px;
}

.ctd-data-table tr th {
  text-align: center;
}

.ctd-data-table .data-table-header tr {
  display: table-row;
}

.ctd-data-table .data-table-header th {
  padding: 0.5rem;
}

.ctd-data-table .cell-inner {
  padding: 6px 8px;
  position: relative;
  display: inline-flex;
  align-items: center;
}

.cell-inner.hyperlink .popup-icon {
  mask-image: url("/images/icon/popup.svg");
  -webkit-mask-image: url("/images/icon/popup.svg");
  mask-size: initial;
  -webkit-mask-size: initial;
  mask-position: center;
  -webkit-mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-repeat: no-repeat;
  display: inline-block;
  width: 10px;
  height: 10px;
  position: absolute;
  right: -1rem;
  background-color: var(--blue);
}

.cell-inner.hyperlink:hover .popup-icon {
  mask-image: url("/images/icon/popup-hover.svg");
  -webkit-mask-image: url("/images/icon/popup-hover.svg");
}

.cell-inner__label {
}

.cell-inner__sort,
.cell-inner__tooltip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
  border-radius: 50%;
  overflow: hidden;
  width: 15px;
  height: 15px;
  cursor: pointer;
}

.cell-inner__tooltip {
  /* position: absolute; */
}

.cell-active .cell-inner__sort {
  background-color: #f2f2f2;
}

.cell-inner__sort.desc {
  transform: rotate(180deg);
}

.cell-inner__sort-icon {
  display: inline-block;
  width: 100%;
  height: 100%;
  background-color: var(--grey-8);
  mask-image: url("/images/icon/sort.svg");
  mask-size: contain;
  mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/sort.svg");
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}

.data-grid__cell {
  padding: 0.5rem;
}

.text-left {
  text-align: left;
}

.text-left > .cell-inner {
  padding-left: 1rem;
}

.text-right {
  text-align: right;
}

.text-right > .cell-inner {
  padding-right: 1rem;
}

.text-align > .cell-inner {
  padding-right: 1rem;
}

.text-center {
  text-align: center;
}

.pagination {
  height: 52px;
  padding-top: 0.5rem;
  padding-right: 0.5rem;
  align-items: center;
  justify-content: flex-end;
}

.pagination-button {
  padding: 6px 8px;
  border-radius: 3px;
  background-color: rgb(52, 145, 255);
  margin-left: 0.5rem;
  display: inline-flex;
}

.inactive-button {
  background-color: #c4c4c4;
  pointer-events: none;
}

.inner-text {
  position: relative;
}

.inner-trend .trend-icon {
  display: inline-block;
  width: 12px;
  height: 12px;
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
}

.inner-trend .trend-icon.up-trend {
  background: #41ce77;
  mask-image: url("/images/icon/trend-2.svg");
  -webkit-mask-image: url("/images/icon/trend-2.svg");
}

.inner-trend .trend-icon.down-trend {
  background: #e34537;
  rotate: 180deg;
  mask-image: url("/images/icon/trend-2.svg");
  -webkit-mask-image: url("/images/icon/trend-2.svg");
}

.inner-trend .trend-icon.balance-trend {
  mask-image: url("/images/icon/balance-trend.svg");
  -webkit-mask-image: url("/images/icon/balance-trend.svg");
  background: #aca6bc;
}

.cell-no-data > div {
  height: 400px;
  font-size: 14.66px;
}

.custom-tooltip {
  position: relative;
  z-index: 40;
}

.custom-tooltip::after {
  content: "";
  display: block;
  width: calc(100% + 10px);
  position: absolute;
  background-color: white;
  z-index: 10;
  left: 0;
  top: 0;
  box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
}
</style>
