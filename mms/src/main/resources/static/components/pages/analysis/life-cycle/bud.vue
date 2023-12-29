<template>
  <div>
    <!-- detail modal -->
    <emdn-modal
      :is-opened="isModalOpened"
      heading-text="Budgeting Details"
      :modal-handler="closeModal"
    >
      <template #body>
        <div class="detail-modal-header">
          <div>
            <span class="label-text -supplier">Supplier</span>
            <span>{{ budgetingDetailData.supplier?.name }}</span>
          </div>
        </div>
        <emdn-data-table body-height="none">
          <template #header>
            <tr>
              <th
                v-for="(tableHeader, index) in detailTableHeaders"
                :key="index"
              >
                {{ tableHeader }}
              </th>
            </tr>
          </template>
          <template #body>
            <tr v-for="item in budgetingDetailData.content">
              <td>
                {{ truncateText(item.moldCode, 12) }}
              </td>
              <td style="text-align: right">{{ item.utilizationRate }}%</td>
              <td style="text-align: right">
                {{ `${item.cost.toLocaleString()} ${item.costType}` }}
              </td>
              <td style="text-align: right">
                {{
                  `${Number(item.salvage).toLocaleString()} ${item.costType}`
                }}
              </td>
              <td
                style="
                  display: flex;
                  align-items: center;
                  justify-content: end;
                  gap: 4px;
                  text-align: right;
                "
              >
                <span
                  class="status-icon"
                  :class="item.lifeCycleStatus.toLowerCase()"
                ></span>
                {{ item.lifeCycleStatus }} Utilization
              </td>
            </tr>
          </template>
        </emdn-data-table>
      </template>
    </emdn-modal>

    <!-- chart -->
    <emdn-xy-chart
      :data="chartDataConverter"
      :axis-data-binder="axisDataBinder"
      :bar-data-binder="barDataBinder"
      category="year"
      :bar-set="barSet"
      :x-axis-label-text-adapter="xAxisLabelTextAdapter"
      :legend-set="legendSet"
    >
    </emdn-xy-chart>

    <!-- data table -->
    <emdn-data-table
      class="ovr-data-table"
      :column-width-array="mainColumnWidthArray"
      style-props="margin-top: 20px;"
    >
      <template #header>
        <tr>
          <th v-for="(tableHeader, index) in mainTableHeaders" :key="index">
            {{ tableHeader }}
          </th>
        </tr>
      </template>
      <template #body>
        <tr v-if="!tableDataConverter?.length" class="cell-no-data">
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
        <tr v-for="item in tableDataConverter">
          <td>
            {{ item.supplierCode }}
          </td>
          <td>
            <div style="display: inline-block">
              <emdn-cta-button
                button-type="text hyperlink"
                :click-handler="() => openModal(item.supplierId)"
                >{{ item.moldCount }} Toolings</emdn-cta-button
              >
            </div>
          </td>
          <td>
            {{ `${item.totalCost} ${item.costType}` }}
          </td>
          <td>
            {{ `${item.totalSalvage} ${item.costType}` }}
          </td>
        </tr>
      </template>
    </emdn-data-table>
  </div>
</template>

<script>
module.exports = {
  props: {
    tableColumnList: Array,
    selectedSupplier: Object,
  },
  data() {
    return {
      isModalOpened: false,
      budgetingDetailData: [],
      budgetingTableData: [],
      budgetingChartData: [],
      axisDataBinder: {
        xAxis: { name: "Within 5 Years", type: "CategoryAxis" },
        yAxis: {
          name: "Number of Toolings at End of Life",
          type: "ValueAxis",
          isLeft: true,
        },
      },
      barDataBinder: [
        {
          key: "totalMoldCount",
          displayName: "",
          color: "#4EBCD5",
        },
      ],
      barSet: {
        isStacked: true,
        isClustered: false,
        width: 40,
        strokeWidth: 0,
        fillOpacity: 1,
      },
      legendSet: {
        isVisible: false,
      },
      mainTableHeaders: [],
      detailTableHeaders: [
        "Tooling",
        "Utilization Rate",
        "Cost of Tooling",
        "Salvage Value",
        "Life Cycle Status",
      ],
      mainColumnWidthArray: ["20%", "20%", "30%", "30%"],
      detailColumnWidthArray: ["10%", "10%", "25%", "25%", "30%"],
    };
  },
  methods: {
    getBudgetingData() {
      let supplierId = this.selectedSupplier.id;

      axios
        .get(`/api/analysis/bud?page=1&size=11&sort=supplierId`, {
          params: {
            supplierId: supplierId,
          },
        })
        .then((res) => {
          this.budgetingTableData = res.data;
          this.budgetingChartData = res.data.graphContent;
        });
    },
    getBudgetingDetailData(supplierId) {
      axios
        .get(`/api/analysis/bud/details`, {
          params: {
            supplierId: supplierId,
          },
        })
        .then((res) => {
          this.budgetingDetailData = res.data;
        });
    },
    openModal(supplierId) {
      this.isModalOpened = true;
      this.getBudgetingDetailData(supplierId);
    },
    closeModal() {
      this.isModalOpened = false;
    },
    filteredTableColumnList() {
      this.mainTableHeaders = this.tableColumnList
        .map((item) => {
          if (item.selected) {
            return item.title;
          }
        })
        .filter((item) => item);
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    xAxisLabelTextAdapter(text, target) {
      if (target.dataItem && target.dataItem.dataContext) {
        return `${
          target.dataItem.dataContext.half === `1` ? "1st" : "2nd"
        } half\n${target.dataItem.dataContext.year}`;
      } else {
        return text;
      }
    },
  },
  computed: {
    chartDataConverter() {
      return Object.values(this.budgetingChartData).map((content) => {
        let selectedSupplierMoldCount = content?.supplierItems?.filter(
          (item) => item.supplierId === this.selectedSupplier.id
        )[0]?.moldCount;
        return {
          ...content,
          moldCount: selectedSupplierMoldCount ? selectedSupplierMoldCount : 0,
        };
      });
    },
    tableDataConverter() {
      if (this.selectedSupplier.id) {
        return this.budgetingTableData.content.filter(
          (item) => item.supplierId === this.selectedSupplier.id
        );
      }
      return this.budgetingTableData.content;
    },
  },
  watch: {
    tableColumnList() {
      this.filteredTableColumnList();
    },
    selectedSupplier() {
      if (this.selectedSupplier.id) {
        this.$set(this.legendSet, "isVisible", true);
        // this.legendSet = Object.assign({}, this.legendSet, { isVisible: true });
        // this.legendSet.isVisible = true;
        this.barDataBinder = [
          {
            key: "moldCount",
            displayName: this.selectedSupplier.title,
            color: "#4EBCD5",
          },
          {
            key: "totalMoldCount",
            displayName: "Other Suppliers",
            color: "#C4C4C4",
          },
        ];
      } else {
        this.$set(this.legendSet, "isVisible", false);
        // this.legendSet = Object.assign({}, this.legendSet, { isVisible: false });
        // this.legendSet.isVisible = false;
        this.barDataBinder = [
          {
            key: "totalMoldCount",
            displayName: "test",
            color: "#4EBCD5",
          },
        ];
      }
    },
  },
  mounted() {
    this.filteredTableColumnList();
    this.getBudgetingData();
  },
};
</script>

<style scoped>
.ovr-data-table table thead tr th {
  text-align: right;
}

.ovr-data-table table thead tr th:first-child {
  text-align: center;
}

.ovr-data-table table tbody tr td {
  text-align: right;
}

.ovr-data-table table tbody tr td:first-child {
  text-align: center;
}

.ovr-data-table .cell-no-data > div {
  height: 400px;
  font-size: 14.66px;
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

.detail-modal-header {
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
</style>
