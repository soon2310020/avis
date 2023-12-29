<template>
  <div>
    <emdn-modal
      :heading-text="`Detail Capacity for ${infoData.viewBy}`"
      :is-opened="isOpened"
      :modal-handler="modalHandler"
    >
      <template #body>
        <div class="detail-capacity-modal-container">
          <div class="detail-capacity-modal-header">
            <div v-if="infoData.viewBy === 'Part'">
              <span>Part</span>
              <span>{{ detailCapacityData.partCode }}</span>
            </div>
            <div v-else>
              <span>Supplier</span>
              <span>{{ detailCapacityData.suppliers?.[0]?.name }}</span>
            </div>
            <div>
              <span>Demand</span>
              <span>{{ detailCapacityData.prodDemand }}</span>
            </div>
            <div>
              <span>Time Range</span>
              <span>{{ displayTimeRange }}</span>
            </div>
          </div>
          <div
            class="detail-capacity-modal-table"
            :style="{
              height: detailCapacityData.content?.length ? '440px' : '45px',
            }"
          >
            <emdn-data-table
              v-if="detailCapacityData.content?.length"
              :scrollable="true"
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
                <tr>
                  <th
                    v-for="header in tableHeaderList"
                    class="detail-capacity-table-th"
                  >
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
                          :active="tableSort?.includes(header.key)"
                        ></emdn-icon>
                      </span>
                    </div>
                  </th>
                </tr>
              </template>
              <template #tbody>
                <tr
                  v-for="(data, index) in detailCapacityData.content"
                  key="index"
                >
                  <td :style="{ textAlign: alignTdText('moldCode') }">
                    <div
                      :style="{
                        justifyContent:
                          alignTdText('moldCode') === 'left'
                            ? 'flex-start'
                            : 'flex-end',
                        display: 'flex',
                      }"
                    >
                      <div
                        @mouseover.stop="(e) => setTooltip(e, data?.moldCode)"
                        @mouseleave.stop="initializeTableTooltip"
                        class="cap-pln-detail-truncate-text"
                      >
                        {{ data?.moldCode }}
                      </div>
                    </div>
                  </td>
                  <td
                    :style="{ textAlign: alignTdText('weightedAvgCycleTime') }"
                  >
                    {{ data.weightedAvgCycleTime?.toFixed(2) }} s
                  </td>
                  <td :style="{ textAlign: alignTdText('avgCycleTime') }">
                    {{ data.avgCycleTime?.toFixed(2) }} s
                  </td>
                  <td :style="{ textAlign: alignTdText('prodQty') }">
                    {{ data.prodQty?.toLocaleString() }}
                  </td>
                  <td :style="{ textAlign: alignTdText('prodCapa') }">
                    {{ data.prodCapa?.toLocaleString() }}
                  </td>
                </tr>
              </template>
            </emdn-data-table>
            <div v-else class="detail-capacity-no-data-table">
              <span>No Data Available</span>
            </div>
          </div>
        </div>
      </template>
    </emdn-modal>
    <div
      v-if="isOpenedTableTooltip"
      :style="tableTooltipPosition"
      class="cap-pln-detail-table-tooltip-wrapper"
      ref="tooltipRef"
    >
      {{ tableTooltipInfo.text }}
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "detail-capacity-modal",
  props: {
    isOpened: {
      type: Boolean,
    },
    modalHandler: {
      type: Function,
    },
    infoData: {
      type: Object,
    },
    timeSettings: {
      type: Object,
    },
  },
  data() {
    return {
      tableSort: null,
      isTableSortByDesc: true,
      currentPage: 1,
      pageSize: 99999,
      detailCapacityData: [],
      tableHeaderList: [
        {
          order: 1,
          title: "Tooling ID",
          align: "left",
          key: "moldCode",
          width: "18%",
        },
        {
          order: 2,
          title: "WACT",
          align: "right",
          key: "weightedAvgCycleTime",
          width: "18%",
        },
        {
          order: 3,
          title: "Avg. CT",
          align: "right",
          key: "avgCycleTime",
          width: "18%",
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
    displayTimeRange() {
      const timeSettings = this.infoData.timeSettings;
      const timeRange = Common.getDatePickerButtonDisplay(
        timeSettings.fromDate,
        timeSettings.toDate,
        timeSettings.timeScale
      );

      if (timeSettings.week) {
        return `
          W${timeSettings.week.substring(4, 6).replace("0", "")},
          ${timeSettings.week.substring(0, 4)}
        `;
      }
      if (timeSettings.month) {
        return `
          ${moment(timeSettings.month.substring(4, 6)).format(
            "MMM"
          )}, ${timeSettings.month.substring(0, 4)}
        `;
      }
      return timeRange;
    },
    tableTooltipPosition() {
      return {
        top: `${this.tableTooltipInfo.top}px`,
        left: `${this.tableTooltipInfo.left}px`,
      };
    },
  },
  methods: {
    async fetchDetailCapacityData() {
      if (!this.infoData.partId) {
        this.detailCapacityData = [];
        return;
      }
      await axios
        .get(`/api/supplychain/cap-pln/details`, {
          params: {
            partId: this.infoData.partId,
            supplierId: this.infoData.supplierId,
            sort: this.tableSort,
            page: this.currentPage,
            size: this.pageSize,
            ...this.infoData.timeSettings,
          },
        })
        .then((res) => {
          this.detailCapacityData = res.data;
        });
    },
    alignTdText(key) {
      return this.tableHeaderList.find((item) => item.key === key).align;
    },
    async sortTable(key) {
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableSort = `${key},${this.isTableSortByDesc ? "desc" : "asc"}`;
      await this.fetchDetailCapacityData();
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
  },
  async mounted() {
    await this.fetchDetailCapacityData();
  },
};
</script>

<style scoped>
.detail-capacity-modal-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding-right: 4px;
  padding-bottom: 4px;
}

.detail-capacity-modal-container th,
td {
  padding: 12px 16px !important;
}

.detail-capacity-modal-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
  height: 26px;
  margin-bottom: 30px;
}

.detail-capacity-modal-table {
  width: 100%;
}

.detail-capacity-table-th {
  z-index: 20;
}

.detail-capacity-table-th > div {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
}

.detail-capacity-modal-header > div {
  display: flex;
  align-items: center;
  width: 32%;
  gap: 54px;
  margin-right: 12px;
}

.detail-capacity-modal-header > div > span {
  font-size: 14.66px;
  color: #4b4b4b;
}

.detail-capacity-modal-header > div > span:first-of-type {
  font-weight: 700;
}

.detail-capacity-modal-header > div > span:last-of-type {
  font-weight: 400;
}

.detail-capacity-no-data-table {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 380px;
  background: #f2f2f2;
  font-size: 12px;
  border-width: 0 1px 1px 1px;
  border-style: solid;
  border-color: #d6dade;
}

.cap-pln-detail-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cap-pln-detail-table-tooltip-wrapper {
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

.cap-pln-detail-table-tooltip-wrapper::after {
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
