<script>
module.exports = {
  props: {
    isModalOpened: {
      type: Boolean,
      default: false,
    },
    closeModal: {
      type: Function,
    },
    params: {
      type: Object,
      default: () => ({
        partId: null,
        supplierId: null,
        timeScale: null,
        fromDate: null,
        toDate: null,
        timeValue: null,
      }),
    },
    timeRange: {
      type: String,
    },
  },
  data() {
    return {
      supplierName: "",
      tableData: [],
      tableHeaderList: [
        {
          order: 1,
          title: "Tooling",
          align: "left",
          key: "moldCode",
          width: "18%",
        },
        {
          order: 2,
          title: "Utilization Rate",
          align: "right",
          key: "utilizationRate",
          width: "",
        },
        {
          order: 3,
          title: "Weighted Average CT",
          align: "right",
          key: "averageCycleTime",
          width: "",
        },
        {
          order: 4,
          title: "Produced Quantity",
          align: "right",
          key: "producedQuantity",
          width: "",
        },
        {
          order: 5,
          title: "Production Capacity",
          align: "right",
          key: "productionCapacity",
          width: "",
        },
        {
          order: 6,
          title: "Tooling Status",
          align: "left",
          key: "toolingStatus",
          width: "18%",
        },
      ],
      tableSort: "",
      isTableSortByDesc: true,
      isOpenedTableTooltip: false,
      tableTooltipInfo: {
        text: null,
        top: null,
        left: null,
      },
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
    async fetchDetailData() {
      await axios
        .get("/api/supplychain/dem-cpl/details", {
          params: {
            sort: this.tableSort,
            ...this.params,
          },
        })
        .then((response) => {
          const data = response.data;
          this.tableData = data.content ?? [];
          this.supplierName = data.suppliers[0].name ?? "";
        })
        .catch((error) => {
          console.warn(error);
        });
    },
    setTableSort(key) {
      if (!key) {
        this.tableSort = null;
        return;
      }
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableSort = `${key},${this.isTableSortByDesc ? "desc" : "asc"}`;
      this.fetchDetailData();
    },
    setTooltip(e, text) {
      if (e.target.clientWidth >= e.target.scrollWidth || !text) return;

      this.tableTooltipInfo.text = text;
      this.isOpenedTableTooltip = true;

      this.$nextTick(() => {
        const targetRect = e.target?.getBoundingClientRect();
        const tooltipRect = this.$refs.tooltipRef?.getBoundingClientRect();

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
    roundToOneDecimal(num) {
      return Math.round(Number(num) * 10) / 10;
    },
  },
  mounted() {
    this.fetchDetailData();
  },
};
</script>

<template>
  <emdn-portal v-if="isModalOpened">
    <emdn-modal
      :is-opened="isModalOpened"
      heading-text="Demand Compliance"
      :modal-handler="closeModal"
    >
      <template #body>
        <div class="dem-cpl-detail-main-container">
          <div class="dem-cpl-detail-header">
            <div>
              <span>Supplier</span>
              <span>{{ supplierName }}</span>
            </div>
            <div>
              <span>Time Range</span>
              <span>{{ timeRange }}</span>
            </div>
          </div>
          <div class="dem-cpl-detail-table-container">
            <emdn-data-table>
              <template #colgroup>
                <col
                  v-for="(header, index) in tableHeaderList"
                  :key="index"
                  :style="{ width: header.width }"
                />
              </template>
              <template #thead>
                <tr ref="tableHeaderRef">
                  <th
                    v-for="(header, index) in tableHeaderList"
                    :style="{
                      padding: '0.5rem 0.75rem 0.5rem 1rem',
                    }"
                    :key="index"
                  >
                    <div
                      :style="{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent:
                          header.align === 'left' ? 'flex-start' : 'flex-end',
                        textAlign: header.align,
                      }"
                    >
                      <span>{{ header.title }}</span>
                      <emdn-icon
                        :button-type="
                          tableSort === `${header.key},asc`
                            ? 'sort-asce'
                            : 'sort-desc'
                        "
                        :click-handler="() => setTableSort(header.key)"
                        :active="tableSort?.includes(header.key)"
                        :disabled="!header.key"
                        style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.25rem;"
                      ></emdn-icon>
                    </div>
                  </th>
                </tr>
              </template>
              <template #tbody>
                <tr v-for="(data, index) in tableData" :key="index">
                  <td>
                    <div
                      class="dem-cpl-detail-truncate-text"
                      @mouseover.stop="(e) => setTooltip(e, data.moldCode)"
                      @mouseleave.stop="initializeTableTooltip"
                    >
                      {{ data.moldCode }}
                    </div>
                  </td>
                  <td style="text-align: end">
                    <div>{{ data.utilizationRate }}%</div>
                  </td>
                  <td style="text-align: end">
                    <div>{{ roundToOneDecimal(data?.averageCycleTime) }} s</div>
                  </td>
                  <td style="text-align: end">
                    <div>
                      {{ data.producedQuantity?.toLocaleString() }}
                    </div>
                  </td>
                  <td style="text-align: end">
                    <div>
                      {{ data.productionCapacity?.toLocaleString() }}
                    </div>
                  </td>
                  <td>
                    <div style="white-space: nowrap">
                      <emdn-table-status
                        :category="data.toolingStatus"
                      ></emdn-table-status>
                    </div>
                  </td>
                </tr>
              </template>
            </emdn-data-table>
            <emdn-portal v-if="isOpenedTableTooltip">
              <div
                :style="tableTooltipPosition"
                class="dem-cpl-detail-table-tooltip-wrapper"
                ref="tooltipRef"
              >
                {{ tableTooltipInfo.text }}
              </div>
            </emdn-portal>
          </div>
        </div>
      </template>
    </emdn-modal>
  </emdn-portal>
</template>

<style>
.dem-cpl-detail-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dem-cpl-detail-header {
  display: flex;
  flex-shrink: 0;
  width: 100%;
  height: 3rem;
  padding-bottom: 1.25rem;
  font-family: "Helvetica Neue", "Helvetica", "Arial", sans-serif;
  font-size: 0.938rem;

  & > div {
    display: flex;
    align-items: center;
    width: 50%;
    gap: 1.75rem;

    & > span:first-child {
      font-weight: 700;
    }
  }
}

.dem-cpl-detail-table-container {
  width: 100%;
  height: calc(100% - 3rem);

  td {
    vertical-align: top;
  }
}

.dem-cpl-detail-truncate-text {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  word-break: break-all;
}

.dem-cpl-detail-table-tooltip-wrapper {
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
</style>
