<template>
  <emdn-modal
    :is-opened="visible"
    :heading-text="modalTitle"
    :style-props="{ width: '1200px', 'max-width': '1200px' }"
    :modal-handler="() => $emit('close')"
  >
    <template #body>
      <div v-if="record" class="summary">
        <div class="summary-item-wrap">
          <div class="summary-item">
            <label class="summary-item__label">
              {{ resources["supplier"] }}
            </label>
            <emdn-tooltip
              v-if="displaySupplier.length > MAX_LENGTH"
              position="bottom"
              style="display: inline-flex"
            >
              <template #context>
                <span>
                  {{ displaySupplier.slice(0, MAX_LENGTH) + "..." }}
                </span>
              </template>
              <template #body>
                <span>{{ displaySupplier }}</span>
              </template>
            </emdn-tooltip>
            <span v-else>{{ displaySupplier }}</span>
          </div>
          <!-- <div class="summary-item">
            <label class="summary-item__label">{{ resources["parts"] }}</label>
            <span>{{ displayPart }}</span>
          </div> -->
          <div class="summary-item">
            <label class="summary-item__label">{{ resources["date"] }}</label>
            <span>{{ displayDuration }}</span>
          </div>
        </div>
      </div>
      <emdn-data-table body-height="300px">
        <template #header>
          <tr>
            <th
              v-for="(column, columnIndex) in listColumns"
              :key="column.field"
              class="data-grid__cell"
              :class="[
                `text-${column.textAlign}`,
                `cell-${column.field}`,
                { 'cell-active': column.field === sort.field },
              ]"
            >
              <div class="cell-inner" :style="`width: ${column.width}`">
                <span class="cell-inner__label">{{ column.label }}</span>
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
          <tr v-for="(row, rowIndex) in listToolings" class="data-grid__row">
            <td
              v-for="(column, columnIndex) in listColumns"
              :key="`${rowIndex}-${columnIndex}`"
              class="data-grid__cell"
              :class="[`text-${column.textAlign}`]"
            >
              <template v-if="column.field === 'parts'">
                <!-- <base-drop-count
                  :is-hyper-link="false"
                  :data-list="row.parts"
                  :title="row.parts[0].name"
                  style="margin-left: 1rem;"
                >
                  <div style="padding: 6px 8px">
                    <div
                      v-for="(i, index) in row.parts"
                      :key="index"
                      @click="() => {}"
                      style="
                        width: fit-content;
                        white-space: nowrap;
                        cursor: pointer;
                        padding: 4px
                      "
                    >
                      {{ i.name }}
                    </div>
                  </div>
                </base-drop-count> -->
                <div v-if="row.parts?.length" style="margin-left: 1rem">
                  <mold-parts-dropdown-view
                    :parts="row.parts"
                    :mold="row"
                    :index="rowIndex"
                    :show-part-chart="showPartChart"
                    title-field="name"
                    :hyperlink="false"
                    :custom-style="{
                      position: 'fixed',
                      transform: 'translate(-150%, 0)',
                      height: '300px',
                      overflow: 'auto',
                    }"
                  ></mold-parts-dropdown-view>
                </div>
              </template>
              <div
                v-else
                class="cell-inner"
                :class="{ hyperlink: column.hyperlink }"
                :style="`width: ${column.width}`"
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
                  {{
                    column.formatter
                      ? column.formatter(row[column.field])
                      : row[column.field]
                  }}
                </span>
              </div>
            </td>
          </tr>
        </template>
        <template #footer>
          <div v-if="pagination.totalPages > 1" class="pagination d-flex">
            <div class="pagination-page">
              <span>{{
                `${pagination.page} of ${pagination.totalPages}`
              }}</span>
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
        </template>
      </emdn-data-table>
    </template>
  </emdn-modal>
</template>

<script>
module.exports = {
  components: {},
  props: {
    resources: Object,
    record: Object,
    visible: Boolean,
    filter: Object,
    duration: Object,
    frequency: String,
    fetchMethod: Function,
  },
  data() {
    return {
      modalTitle: "Cycle Time Fluctuation Details",
      listColumns: [
        {
          label: "Toolings",
          field: "moldCode",
          sortable: true,
          hyperlink: true,
          textAlign: "left",
          onClick: (row, col) => this.handleClickTooling(row, col),
        },
        {
          label: "Part Name",
          field: "parts",
          sortable: true,
          hyperlink: true,
          textAlign: "left",
        },
        {
          label: "ACT",
          field: "approvedCycleTimeSecond",
          sortable: true,
          textAlign: "right",
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + " s";
          },
        },
        {
          label: "Avg. CT",
          field: "averageCycleTimeSecond",
          sortable: true,
          textAlign: "right",
          formatter: (val) => {
            console.log(val + "averageCycleTime");
            if (val == null) return "-";
            return val.toFixed(1) + " s";
          },
        },
        {
          label: "CT Fluctuation",
          field: "ctFluctuationSecond",
          sortable: true,
          textAlign: "right",
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + " s";
          },
        },
        {
          label: "L1 Limit (sec)",
          field: "l1LimitSecond",
          sortable: true,
          textAlign: "right",
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + " s";
          },
        },
        {
          label: "Normalized CT Fluctuation",
          field: "nctf",
          sortable: true,
          textAlign: "right",
          width: "200px",
          formatter: (val) => {
            if (val == null) return "-";
            return val.toFixed(1) + "%";
          },
          tooltip:
            "Normalized CT Fluctuation is represented as an index. The lower the percentage, the higher the score in terms of optimal, consistent production.",
        },
        {
          label: "Trend",
          field: "ctfTrend",
          sortable: false,
          trend: true,
          textAlign: "right",
          formatter: (val) => {
            console.log(val + "ctfTrend");
            if (val == null) return "-";
            return val.toFixed(1).toString().replace("-", "") + "%";
          },
        },
      ],
      // listColumns: [],
      sort: {
        field: "",
        desc: false,
      },
      pagination: {
        page: 1,
        size: 20,
        totalPages: 1,
      },
    };
  },
  computed: {
    listToolings() {
      return this.record?.listToolings || [];
    },
    displaySupplier() {
      return this.record?.supplierName;
    },
    displayPart() {
      return this.record?.selectedPart?.title || "All parts";
    },
    displayDuration() {
      return (
        this.duration.start.format("DD-MM-YYYY") +
        " to " +
        this.duration.end.format("DD-MM-YYYY")
      );
    },
    sortField() {
      let sortVal = this.sort.field;
      if (sortVal.endsWith("Second")) {
        sortVal = sortVal.slice(0, -"Second".length);
      }
      return `${sortVal},${this.sort.desc ? "desc" : "asc"}`;
    },
  },
  methods: {
    showPartChart() {
      console.log("showPartChart");
    },
    handleSort(sortField) {
      if (this.sort.field === sortField) this.sort.desc = !this.sort.desc;
      this.sort.field = sortField;
    },
    handleClickTooling(row, col) {
      this.$emit("show-tooling", row, col);
    },
    handleCloseModal() {
      console.log("@handleCloseModal");
    },
    handlePagination(pageNumber) {
      this.fetchMethod({ page: pageNumber, sort: this.sort.query });
    },
    handleChangePage(type) {
      if (type === "NEXT") {
        this.pagination.page++;
      }
      if (type === "PREV") {
        this.pagination.page--;
      }
      this.fetchMethod({ page: this.pagination.page, sort: this.sortField });
    },
    getTrendClass(trendValue) {
      if (trendValue == 0) return "balance-trend";
      if (trendValue > 0) return "up-trend";
      if (trendValue < 0) return "down-trend";
      return; // case null
    },
  },
  watch: {
    record: {
      handler(newVal) {
        if (!newVal?.supplierId) return;
        this.pagination.totalPages = newVal.totalPages;
        this.pagination.size = newVal.size;
      },
      deep: true,
    },
    // "pagination.page"(newVal) {
    //   this.fetchMethod({ page: newVal, sort: this.sortField });
    // },
    sortField(newVal) {
      this.fetchMethod({ sort: newVal });
    },
    visible(newVal) {
      if (!newVal) {
        this.pagination.page = 1;
        this.pagination.totalPages;
      }
    },
  },
  created() {
    this.MAX_LENGTH = 30;
  },
};
</script>

<style scoped>
.summary {
  margin-bottom: 28px;
}

.summary-item-wrap {
  display: flex;
}

.summary-item {
  flex: 1;
}

.summary-item label {
  font-weight: bold;
  width: 80px;
  margin-bottom: 0;
}

.data-grid__cell {
  width: 20%;
  text-align: center;
  padding: 0 !important;
}

.cell-inner__sort {
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
  flex-shrink: 0;
}

th .cell-inner {
  display: inline-flex;
  align-items: center;
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

.cell-inner__tooltip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  width: 15px;
  height: 15px;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 4px;
}
</style>
