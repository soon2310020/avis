<template>
  <div>
    <emdn-xy-chart
      :line-data-binder="lineDataBinder"
      :line-set="lineSet"
      :data="chartData"
      category="title"
      :previous-button="previousButton"
      :next-button="nextButton"
      style-props="height: 300px; width: 100%"
    >
    </emdn-xy-chart>
    <emdn-data-table
      class="ctd-data-table"
      :column-width-array="columnWidthArray"
      style-props="margin-top: 20px;"
    >
      <template #header>
        <tr>
          <th>Part Name<span class="sort-icon"></span></th>
          <th>No. of Toolings<span class="sort-icon"></span></th>
          <th>Required Quantity<span class="sort-icon"></span></th>
          <th>Produced Quantity<span class="sort-icon"></span></th>
          <th>Production Capacity<span class="sort-icon"></span></th>
        </tr>
      </template>
      <template #body>
        <tr v-if="!tableData.length" class="cell-no-data">
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
        <tr v-for="(item, index) in tableData" :key="index">
          <td>{{ item.partName }}</td>
          <td>
            <div style="display: inline-block">
              <emdn-cta-button
                button-type="text hyperlink"
                :click-handler="() => openDetailModal(item)"
              >
                {{ item.moldCount }} Toolings
              </emdn-cta-button>
            </div>
          </td>
          <td style="text-align: right">
            {{ Common.formatNumber(item.requiredQuantity) }}
          </td>
          <td style="text-align: right">
            {{ Common.formatNumber(item.producedQuantity) }}
          </td>
          <td style="text-align: right">
            {{ Common.formatNumber(item.productionCapacity) }}
          </td>
        </tr>
      </template>
    </emdn-data-table>

    <!-- production Bottlenect Details modal -->
    <emdn-modal
      :is-opened="isDetailPartModalOpened"
      heading-text="Production Bottlenect Details"
      :modal-handler="closeDetailPartModal"
    >
      <template #body>
        <div class="prd-btn-detail-modal-content">
          <div>
            <p class="label-text">Supplier(s)</p>
            <p v-if="toolingsDetailData.suppliers">
              <span v-if="toolingsDetailData.suppliers.length === 1"
                >1 Supplier</span
              >
              <span v-else
                >{{ toolingsDetailData.suppliers.length }} Suppliers</span
              >
            </p>
            <span v-else>All Suppliers</span>
          </div>
          <div>
            <p class="label-text">Product</p>
            <span>{{ toolingsDetailData.product.name }}</span>
          </div>
          <div>
            <p class="label-text">Part</p>
            <span>{{ toolingsDetailData.part.name }}</span>
          </div>
          <div>
            <p class="label-text">Date</p>
            <span
              >{{ moment(fromDate, "YYYYMMDD").format("YYYY-MM-DD") }} to
              {{ moment(toDate, "YYYYMMDD").format("YYYY-MM-DD") }}</span
            >
          </div>
        </div>

        <emdn-data-table>
          <template #header>
            <tr>
              <th>Tooling<span class="sort-icon"></span></th>
              <th>Supplier<span class="sort-icon"></span></th>
              <th>Status<span class="sort-icon"></span></th>
              <th>Utilization Rate<span class="sort-icon"></span></th>
              <th>Average Cycle Time<span class="sort-icon"></span></th>
              <th>Producted Quantity<span class="sort-icon"></span></th>
              <th>Production Capacity<span class="sort-icon"></span></th>
            </tr>
          </template>
          <template #body>
            <tr
              v-for="(item, index) in toolingsDetailData.content"
              :key="index"
            >
              <td>{{ item.moldCode }}</td>
              <td>{{ item.supplierName }}</td>
              <td>{{ item.moldStatus.toLowerCase() }}</td>
              <td>
                {{
                  item.utilizationRate &&
                  Common.formatNumber(Number(item.utilizationRate).toFixed(2)) +
                    " %"
                }}
              </td>
              <td>
                {{
                  item.averageCycleTime &&
                  Common.formatNumber(
                    Number(item.averageCycleTime).toFixed(2)
                  ) + " s"
                }}
              </td>
              <td>{{ Common.formatNumber(item.producedQuantity) }}</td>
              <td>{{ Common.formatNumber(item.productionCapacity) }}</td>
            </tr>
          </template>
          <template #footer>
            <div
              :style="`
                                width: 100%;
                                height: 44px;
                                display: flex;
                                align-items: center;
                                justify-content: flex-end;
                                padding-right: 20px;`"
            >
              {{ toolingsDetailData.pageable.pageNumber + 1 }} of
              {{ toolingsDetailData.totalPages }}
              <emdn-icon-button
                :disabled="toolingsDetailData.pageable.pageNumber + 1 === 1"
                button-type="previous"
              ></emdn-icon-button>
              <emdn-icon-button
                :disabled="
                  toolingsDetailData.pageable.pageNumber + 1 ===
                  toolingsDetailData.totalPages
                "
                button-type="next"
              ></emdn-icon-button>
            </div>
          </template>
        </emdn-data-table>
      </template>
    </emdn-modal>
  </div>
</template>

<script>
module.exports = {
  props: {
    fromDate: String,
    toDate: String,
    supplierList: Array,
    selectedSupplier: Array,
    selectedProduct: Object,
    timeScale: {
      type: String,
      default: "WEEK",
    },
    durationHandler: {
      type: Function,
      default: () => {
        console.log("12312312");
      },
    },
  },
  watch: {
    toDate: {
      handler(newVal) {
        // toDate 가 오늘보다 크거나 같으면 next button disabled 처리
        if (Number(newVal) >= Number(moment(new Date()).format("YYYYMMDD"))) {
          this.nextButton.disabled = true;
        }
      },
      immediate: true,
    },
    selectedSupplier(newVal) {
      console.log("watch selectedSupplier: ", newVal);
      this.getBottleneckData();
    },
    selectedProduct(newVal) {
      console.log("watch selectedProduct: ", newVal);
      this.getBottleneckData();
    },
    timeValue(newVal) {
      console.log("watch timeValue: ", newVal);
      this.getBottleneckData();
    },
  },
  data() {
    return {
      lineSet: {
        // isLineVisible: false,
        // strokeWidth: 2,
        // fillOpacity: 0.1,
        // bullet: { strokeWidth: 2, radius: 3 },
        // colorList: ['#f08080', '#fa8072', '#e9967a', '#ff7f50', '#ff6347', '#f4a460', '#ffa07a'],
        colorList: [
          "#CC3E6E",
          "#592A7E",
          "#9521AA",
          "#9D7FEE",
          "#09BD4E",
          "#E2AD1D",
          "#C92617",
          "#94CCA5",
          "#40A2B9",
          "#10155A",
        ],
      },
      previousButton: { visible: true, disabled: false },
      nextButton: { visible: true, disabled: false },
      selectedPart: {},
      tableData: [],
      chartData: [],
      lineDataBinder: [],
      toolingsDetailData: [],
      isDetailPartModalOpened: false,
      axisDataBinder: {
        yAxis: { name: "Total Part Produced" },
      },
      lineDataBinder: [
        { key: "oct", displayName: "Optimal Cycle Time", color: "#4dbd74" },
        { key: "sc", displayName: "Shot Count", color: "#63C2DE" },
      ],
      columnWidthArray: ["20%", "20%", "20%", "20%", "20%"],
    };
  },
  mounted() {
    this.$nextTick(() => {
      const self = this;
      console.log("production buttlenect mounted 77");

      this.previousButton.handler = function () {
        self.durationHandler("previous");
      };
      this.nextButton.handler = function () {
        self.durationHandler("next");
      };

      this.getBottleneckData();
    });
  },
  computed: {
    timeValue() {
      console.log("timeValue 00");
      console.log("this.fromDate: ", this.fromDate);
      console.log("this.toDate: ", this.toDate);
      console.log("this.timeScale: ", this.timeScale);
      return Common.getDatePickerTimeValue(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
    },
  },
  methods: {
    getBottleneckData() {
      console.log("getBottleneckData: ", this.timeScale);

      if (!this.timeScale) {
        return Common.alert("Please set timeScale");
      }
      if (!this.timeValue) {
        return Common.alert("Please set timeValue");
      }
      if (!this.fromDate) {
        return Common.alert("Please set fromDate");
      }
      if (!this.toDate) {
        return Common.alert("Please set toDate");
      }
      if (!this.selectedProduct.id) {
        return Common.alert("Please set productId");
      }

      // required
      let timeScaleParam = this.timeScale ? `&timeScale=${this.timeScale}` : "";
      let timeValueParam = this.timeValue ? `&timeValue=${this.timeValue}` : "";
      let fromDateParam = this.fromDate ? `&fromDate=${this.fromDate}` : "";
      let toDateParam = this.toDate ? `&toDate=${this.toDate}` : "";
      let productIdParam = this.selectedProduct.id
        ? `&productId=${this.selectedProduct.id}`
        : "";

      // optional
      let supplierIdParam = "";
      this.selectedSupplier.map((item) => {
        supplierIdParam += `&supplierId=${item.id}`;
      });

      // optional (pagination)
      let prdBtnOffset = "";
      let prdBtnPageNumber = "";
      let prdBtnPageSize = "";
      let prdBtnPaged = "";
      let prdBtnUnpaged = "";
      let prdBtnSorted = "";
      let prdBtnUnsorted = "";

      let offsetParam = prdBtnOffset ? `&offset=${prdBtnOffset}` : "";
      let pageNumberParam = prdBtnPageNumber
        ? `&pageNumber=${prdBtnPageNumber}`
        : "";
      let pageSizeParam = prdBtnPageSize ? `&pageSize=${prdBtnPageSize}` : "";
      let pagedParam = prdBtnPaged ? `&paged=${prdBtnPaged}` : "";
      let unpagedParam = prdBtnUnpaged ? `&unpaged=${prdBtnUnpaged}` : "";
      let sortedParam = prdBtnSorted ? `&sort.sorted=${prdBtnSorted}` : "";
      let unsortedParam = prdBtnUnsorted
        ? `&sort.unsorted=${prdBtnUnsorted}`
        : "";

      let prdBtnUrl =
        `/api/analysis/prd-btn?` +
        fromDateParam +
        toDateParam +
        timeScaleParam +
        timeValueParam +
        productIdParam +
        supplierIdParam +
        offsetParam +
        pageNumberParam +
        pageSizeParam +
        pagedParam +
        unpagedParam +
        sortedParam +
        unsortedParam;

      console.log("prdBtnUrl: ", prdBtnUrl);

      axios.get(prdBtnUrl).then((res) => {
        console.log("getBottleneckData res: ", res);
        this.tableData = res.data.content;
        this.chartData = res.data.chartItems2;
        let chartLegends = res.data.chartLegends;
        this.lineDataBinder = [];
        chartLegends.map((item) => {
          this.lineDataBinder.push({ key: item.id, displayName: item.name });
        });
      });
    },
    getDetailBottleneckData() {
      if (!this.selectedProduct.id) {
        return Common.alert("productId 를 설정해주세요.");
      }
      if (!this.selectedPart.partId) {
        return Common.alert("partId 를 설정해주세요.");
      }

      // required
      let productIdParam = this.selectedProduct.id
        ? `&productId=${this.selectedProduct.id}`
        : "";
      let partIdParam = this.selectedPart.partId
        ? `&partId=${this.selectedPart.partId}`
        : "";

      // optional
      let timeScaleParam = this.timeScale ? `&timeScale=${this.timeScale}` : "";
      let timeValueParam = this.timeValue ? `&timeValue=${this.timeValue}` : "";
      let fromDateParam = this.fromDate ? `&fromDate=${this.fromDate}` : "";
      let toDateParam = this.toDate ? `&toDate=${this.toDate}` : "";
      let supplierIdParam = "";
      this.selectedSupplier.map((item) => {
        supplierIdParam += `&supplierId=${item.id}`;
      });

      // optional (pagination)
      let offsetParam = this.prdBtnDetailOffset
        ? `&offset=${this.prdBtnDetailOffset}`
        : "";
      let pageNumberParam = this.prdBtnDetailPageNumber
        ? `&pageNumber=${this.prdBtnDetailPageNumber}`
        : "";
      let pageSizeParam = this.prdBtnDetailPageSize
        ? `&pageSize=${this.prdBtnDetailPageSize}`
        : "";
      let pagedParam = this.prdBtnDetailPaged
        ? `&paged=${this.prdBtnDetailPaged}`
        : "";
      let unpagedParam = this.prdBtnDetailUnpaged
        ? `&unpaged=${this.prdBtnDetailUnpaged}`
        : "";

      let prdBtnDetailsUrl =
        "/api/analysis/prd-btn/details?" +
        timeScaleParam +
        timeValueParam +
        fromDateParam +
        toDateParam +
        productIdParam +
        partIdParam +
        offsetParam +
        pageNumberParam +
        pageSizeParam +
        pagedParam +
        supplierIdParam +
        unpagedParam;

      console.log("prdBtnDetailsUrl: ", prdBtnDetailsUrl);

      axios.get(prdBtnDetailsUrl).then((res) => {
        this.toolingsDetailData = res.data;
      });
    },
    openDetailModal(item) {
      this.selectedPart = item;
      console.log("openDetailModal");
      console.log("item: ", item);
      this.getDetailBottleneckData();
      this.isDetailPartModalOpened = true;
    },
    closeDetailPartModal() {
      this.isDetailPartModalOpened = false;
    },
  },
};
</script>

<style scoped>
.prd-btn-detail-modal-content {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.prd-btn-detail-modal-content > div {
  flex: 1 1 33%;
  display: flex;
}

.prd-btn-detail-modal-content .label-text {
  width: 85px;
  margin-bottom: 1rem;
}

.ctd-data-table tr th {
  text-align: center;
  white-space: nowrap;
}

.ctd-data-table .cell-no-data > div {
  height: 400px;
  font-size: 14.66px;
}
</style>
