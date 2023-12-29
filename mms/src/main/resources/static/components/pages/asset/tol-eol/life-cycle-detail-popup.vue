<template>
  <div>
    <emdn-modal
      :is-opened="isOpened"
      :modal-handler="closeHandler"
      heading-text="Life Cycle Details"
    >
      <template #body>
        <div class="life-cycle-popup-container">
          <div class="life-cycle-popup-header">
            <span>Time Period</span>
            <span>{{ displayTimePeriod }}</span>
          </div>
          <div class="life-cycle-popup-table">
            <emdn-data-table :scrollable="true">
              <template #colgroup>
                <col
                  v-for="item in tableHeaderWidthArray"
                  :style="{ width: item.width }"
                />
              </template>
              <template #thead>
                <tr>
                  <th
                    v-for="header in tableHeaders"
                    style="padding: 8px 4px 8px 16px"
                  >
                    <div
                      class="tol-eol-table-th"
                      :style="{ justifyContent: header.style.justifyContent }"
                    >
                      <span>{{ header.title }}</span>
                      <span @click="sortTable(header.key)">
                        <emdn-icon
                          :button-type="
                            tableSort === `${header.key},asc`
                              ? 'sort-asce'
                              : 'sort-desc'
                          "
                          :active="tableSort?.includes(header.key)"
                        ></emdn-icon>
                      </span>
                    </div>
                  </th>
                </tr>
              </template>
              <template #tbody>
                <tr v-for="(data, index) in tableData">
                  <td>
                    <span v-if="data.moldCode.length <= 15">{{
                      data.moldCode
                    }}</span>
                    <emdn-tooltip
                      v-else
                      position="right"
                      style="justify-content: flex-start; z-index: 10"
                    >
                      <template #context>
                        <!--                      <span class="tol-eol-ellipsis-text">{{-->
                        <!--                        data.moldCode-->
                        <!--                      }}</span>-->
                        <span>{{ truncateText(data.moldCode, 15) }}</span>
                      </template>
                      <template #body>
                        <span style="white-space: nowrap">{{
                          data.moldCode
                        }}</span>
                      </template>
                    </emdn-tooltip>
                  </td>
                  <td style="text-align: right">
                    <span
                      >{{
                        data.utilizationRate
                          ? data.utilizationRate.toLocaleString()
                          : 0
                      }}&#37;</span
                    >
                  </td>
                  <td style="text-align: right">
                    <span
                      >{{ data.cost ? data.cost.toLocaleString() : 0 }}&nbsp;{{
                        data.currencyCode
                      }}</span
                    >
                  </td>
                  <td style="text-align: right">
                    <span
                      >{{
                        data.salvageValue
                          ? data.salvageValue.toLocaleString()
                          : 0
                      }}&nbsp;{{ data.currencyCode }}</span
                    >
                  </td>
                  <td>
                    <emdn-table-status
                      :category="data?.utilizationStatus"
                    ></emdn-table-status>
                  </td>
                </tr>
              </template>
            </emdn-data-table>
          </div>
        </div>
      </template>
    </emdn-modal>
  </div>
</template>

<script>
module.exports = {
  name: "life-cycle-detail-popup",
  props: {
    isOpened: Boolean,
    closeHandler: Function,
    timeSettings: Object,
  },
  data() {
    return {
      tableData: [],
      isSortTableByDesc: false,
      tableSort: null,
      tableHeaders: [
        {
          id: 1,
          key: "moldCode",
          title: "Tooling",
          style: {
            width: "20%",
            justifyContent: "flex-start",
          },
        },
        {
          id: 2,
          key: "utilizationRate",
          title: "Utilization Rate",
          style: {
            width: "20%",
            justifyContent: "flex-end",
          },
        },
        {
          id: 3,
          key: "cost",
          title: "Cost of Tooling",
          style: {
            width: "20%",
            justifyContent: "flex-end",
          },
        },
        {
          id: 4,
          key: "salvageValue",
          title: "Salvage Value",
          style: {
            width: "20%",
            justifyContent: "flex-end",
          },
        },
        {
          id: 5,
          key: "utilizationStatus",
          title: "Utilization Status",
          style: {
            width: "20%",
            justifyContent: "flex-start",
          },
        },
      ],
    };
  },
  methods: {
    async fetchData() {
      await axios
        .get(`/api/asset/tol-eol`, {
          params: {
            ...this.timeSettings,
            sort: this.tableSort,
            size: 99999,
            page: 1,
          },
        })
        .then((response) => {
          const data = response.data;

          if (!data.content.length) {
            this.tableData = [];
          }

          this.tableData = data.content;
        })
        .catch((error) => {
          console.warn(error);
        });
    },
    sortTable(key) {
      this.isSortTableByDesc = !this.isSortTableByDesc;
      this.tableSort = `${key},${this.isSortTableByDesc ? "desc" : "asc"}`;
      this.fetchData();
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
  },
  computed: {
    displayTimePeriod() {
      const year = this.timeSettings.timeValue?.slice(0, 4);
      const half = this.timeSettings.timeValue?.slice(4, 5);

      return `${half === "1" ? "1st" : "2nd"} half ${year}`;
    },
    tableHeaderWidthArray() {
      return this.tableHeaders.filter((item, index) => item.width);
    },
  },
  mounted() {
    this.fetchData();
  },
};
</script>

<style scoped>
.life-cycle-popup-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.life-cycle-popup-header {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-size: 14px;
  font-weight: 400;
  width: 100%;
  height: 28px;
  display: flex;
  justify-content: flex-start;
  gap: 50px;
  align-items: center;
  margin-bottom: 15px;
}

.life-cycle-popup-header > span:first-of-type {
  font-weight: 700;
}

.life-cycle-popup-table {
  width: 100%;
  height: 445px;
}

.life-cycle-popup-table table th {
  z-index: 20;
}
</style>
