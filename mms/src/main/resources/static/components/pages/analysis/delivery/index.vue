<template>
  <div class="delivery-container">
    <!-- custom column modal  -->
    <emdn-modal
      v-if="isCustomColumnModalOpened"
      :is-opened="isCustomColumnModalOpened"
      heading-text="Customize Your Columns"
      :modal-handler="() => (isCustomColumnModalOpened = false)"
    >
      <template #body>
        <div class="column-modal-container">
          <div>
            <p>Available Columns</p>
            <emdn-list-group
              :items="columnList"
              :set-result="(item) => selectColumn(item)"
              style-props="width: 200px;"
            >
            </emdn-list-group>
          </div>
          <div>
            <p>Selected Columns</p>
            <emdn-list-group
              :items="columnList"
              :set-result="(item) => selectColumn(item)"
              id-field="true"
              is-selected="true"
            >
            </emdn-list-group>
          </div>
        </div>
      </template>
    </emdn-modal>

    <!-- calendar modal -->
    <emdn-modal
      v-if="isCalendarModalOpened"
      :is-opened="isCalendarModalOpened"
      :heading-text="`Choose a ${timeScaleDisplayName}`"
      :modal-handler="datepickerCancelHandler"
      style-props="width: fit-content; height: fit-content;"
    >
      <template #body>
        <emdn-calendar
          show-selector="true"
          date-format="YYYYMMDD"
          :max-date="maxDate"
          :selector-options="selectorOptions"
          :click-handler="datepickerChangeHandler"
          :date-range="dateRange"
          :time-scale="timeScale.toLowerCase()"
        ></emdn-calendar>
      </template>
      <template #footer>
        <emdn-cta-button
          color-type="white"
          :click-handler="datepickerCancelHandler"
          >Cancel</emdn-cta-button
        >
        <emdn-cta-button
          color-type="blue-fill"
          :click-handler="datepickerSaveHandler"
          >Save</emdn-cta-button
        >
      </template>
    </emdn-modal>

    <!--  header  -->
    <div class="delivery-header">
      <p>Production Bottleneck</p>
      <div>
        <!-- <emdn-icon-button button-type="custom-colum" :click-handler="() => (isCustomColumnModalOpened = true)"></emdn-icon-button> -->
        <emdn-cta-button
          button-type="date-picker"
          color-type="blue-fill"
          :click-handler="() => (isCalendarModalOpened = true)"
        >
          {{ selectedDateDisplayName }}
        </emdn-cta-button>
        <!-- September, 2023 -->
      </div>
    </div>

    <!--  filter dropdowns  -->
    <div class="delivery-filter-wrapper">
      <div style="position: relative">
        <span class="label-text">Supplier(s)</span>
        <emdn-cta-button
          :active="isSupplierListOpened"
          :click-handler="
            () => (this.isSupplierListOpened = !this.isSupplierListOpened)
          "
          color-type="white"
          button-type="dropdown"
        >
          <span v-if="selectedSupplier.length === 0">All Suppliers</span>
          <span v-else-if="selectedSupplier.length === 1">1 Supplier</span>
          <span v-else-if="selectedSupplier.length > 1"
            >{{ selectedSupplier.length }} Suppliers</span
          >
        </emdn-cta-button>
        <emdn-dropdown
          checkbox="true"
          :set-result="selectSupplier"
          :visible="isSupplierListOpened"
          :on-close="() => (this.isSupplierListOpened = false)"
          style-props="width: 207px; top: 17.5px; left: 83px;"
          :items="supplierList"
          id="supplier"
        ></emdn-dropdown>
      </div>
      <div style="position: relative">
        <span class="label-text">Product Name</span>
        <emdn-cta-button
          :active="isProductListOpened"
          :click-handler="
            () => (this.isProductListOpened = !this.isProductListOpened)
          "
          color-type="white"
          button-type="dropdown"
        >
          {{ selectedProduct.title ? selectedProduct.title : "All Products" }}
        </emdn-cta-button>
        <emdn-dropdown
          :visible="isProductListOpened"
          :on-close="() => (this.isProductListOpened = false)"
          :click-handler="selectProduct"
          style-props="width: 207px; top: 17.5px; left: 106px;"
          :items="productList"
        ></emdn-dropdown>
      </div>
    </div>

    <prd-btn
      v-if="selectedProduct.id"
      :time-scale="timeScale"
      :from-date="fromDate"
      :to-date="toDate"
      :supplier-list="supplierList"
      :selected-supplier="selectedSupplier"
      :selected-product="selectedProduct"
      :duration-handler="durationHandler"
    >
    </prd-btn>
  </div>
</template>

<script>
module.exports = {
  name: "delivery-page",
  components: {
    "prd-btn": httpVueLoader("/components/pages/analysis/delivery/prd-btn.vue"),
  },
  mounted() {
    console.log("delivery mounted 55");

    let currentWeek = moment().format("YYYYWW");
    let previousWeek = parseInt(currentWeek) - 1;

    this.fromDate = moment(previousWeek, "YYYYWW")
      .subtract(1, "days")
      .format("YYYYMMDD");
    this.toDate = moment(previousWeek, "YYYYWW")
      .add(5, "days")
      .format("YYYYMMDD");

    // this.dateRange = { start: moment(this.fromDate, 'YYYYMMDD'), end: moment(this.toDate, 'YYYYMMDD') };

    this.getProductList();
    this.getSupplierList();
  },
  computed: {
    selectedDateDisplayName() {
      // return Common.getDatePickerButtonDisplay(this.dateRange.start, this.dateRange.end, this.timeScale);
      return Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.timeScale
      );
    },
    timeScaleDisplayName() {
      return (
        this.tempTimeScale.charAt(0) + this.tempTimeScale.slice(1).toLowerCase()
      );
    },
    dateRange() {
      return {
        start: moment(this.fromDate, "YYYYMMDD"),
        end: moment(this.toDate, "YYYYMMDD"),
      };
    },
  },
  data() {
    return {
      // dateRange: {
      //     start: new Date(),
      //     end: new Date(),
      // },
      tempTimeScale: "",
      tempFromDate: "",
      tempToDate: "",
      timeScale: "WEEK",
      isCustomColumnModalOpened: false,
      isCalendarModalOpened: false,

      // parameter
      fromDate: "",
      toDate: "",
      selectedDate: "",

      // dropdown open value
      isSupplierListOpened: false,
      isProductListOpened: false,

      // dropdown list
      productList: [],
      supplierList: [],

      // selected dropdown item
      selectedSupplier: [],
      selectedProduct: {},
      columnList: [
        {
          id: "1",
          title: "item 1",
          selected: false,
        },
        {
          title: "item 2",
          id: "2",
          selected: false,
        },
        {
          title: "item 3",
          id: "3",
          selected: false,
        },
      ],
      selectorOptions: ["week", "month", "year", "custom"],
      maxDate: new Date(),
    };
  },
  methods: {
    durationHandler(nextOfPrevious) {
      console.log("durationHandler 11");
      console.log("nextOfPrevious", nextOfPrevious);
      let result = this.datepickerDurationHandler(
        nextOfPrevious,
        this.fromDate,
        this.toDate,
        this.timeScale
      );

      this.fromDate = result.fromDate;
      this.toDate = result.toDate;
    },
    datepickerDurationHandler(nextOfPrevious, fromDate, toDate, timeScale) {
      let addSubstract = "";
      if (nextOfPrevious === "next") {
        console.log("next Handler");
        addSubstract = "add";
      }

      if (nextOfPrevious === "previous") {
        console.log("previous Handler");
        addSubstract = "subtract";
      }

      // WEEK, MONTH, YEAR

      console.log("fromDate 11: ", fromDate);
      console.log("toDate 11: ", toDate);

      if (timeScale === "WEEK") {
        fromDate = moment(fromDate, "YYYYMMDD")
          [addSubstract](7, "days")
          .format("YYYYMMDD");
        toDate = moment(toDate, "YYYYMMDD")
          [addSubstract](7, "days")
          .format("YYYYMMDD");
      }

      if (timeScale === "MONTH") {
        fromDate = moment(fromDate, "YYYYMMDD")
          [addSubstract](1, "months")
          .format("YYYYMMDD");
        toDate = moment(toDate, "YYYYMMDD")
          [addSubstract](1, "months")
          .format("YYYYMMDD");
      }

      if (timeScale === "YEAR") {
        fromDate = moment(fromDate, "YYYYMMDD")
          [addSubstract](1, "years")
          .format("YYYYMMDD");
        toDate = moment(toDate, "YYYYMMDD")
          [addSubstract](1, "years")
          .format("YYYYMMDD");
      }

      console.log("fromDate 22: ", fromDate);
      console.log("toDate 22: ", toDate);

      // CUSTOM

      if (timeScale === "CUSTOM") {
        let toDateTs = moment(toDate, "YYYYMMDD").unix();
        let fromDateTs = moment(fromDate, "YYYYMMDD").unix();
        let difference = toDateTs - fromDateTs;
        console.log("difference: ", difference);

        fromDate = moment(fromDate, "YYYYMMDD")
          .add(1, "years")
          .format("YYYYMMDD");
        toDate = moment(toDate, "YYYYMMDD").add(1, "years").format("YYYYMMDD");
      }

      console.log("fromDate 33: ", fromDate);
      console.log("toDate 33: ", toDate);
      return { fromDate, toDate };
    },
    datepickerSaveHandler() {
      this.timeScale = this.tempTimeScale;
      this.fromDate = this.tempFromDate;
      this.toDate = this.tempToDate;
      this.dateRange = {
        start: moment(this.fromDate, "YYYYMMDD"),
        end: moment(this.toDate, "YYYYMMDD"),
      };
      this.isCalendarModalOpened = false;
    },
    datepickerCancelHandler() {
      this.tempTimeScale = "";
      this.tempFromDate = "";
      this.tempToDate = "";
      this.isCalendarModalOpened = false;
    },
    datepickerChangeHandler(rangeDate, timeScale) {
      this.tempTimeScale = timeScale;
      this.tempFromDate = rangeDate.start;
      this.tempToDate = rangeDate.end;
    },
    getProductList() {
      axios.get("/api/common/flt/products?size=5000").then((res) => {
        this.productList = res.data.content;
        this.productList.map((item) => {
          item.title = item.name;
        });
        console.log("this.productList: ", this.productList);
        this.selectedProduct = this.productList[0];
      });
    },
    getSupplierList() {
      axios.get("/api/common/flt/suppliers?size=5000").then((res) => {
        this.supplierList = res.data.content;
        this.supplierList.map((item) => {
          item.title = item.name;
        });
        console.log("this.supplierList: ", this.supplierList);
      });
    },

    selectSupplier(param) {
      this.selectedSupplier = param;
    },
    selectProduct(param) {
      this.selectedProduct = param;
      this.isProductListOpened = false;
    },
    selectColumn(param) {},
  },
};
</script>

<style scoped>
.delivery-container {
  width: 100%;
  padding-bottom: 34px;
}

.delivery-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 19px;
}

.delivery-header > :last-child {
  display: flex;
  gap: 8px;
}

.delivery-header > p {
  margin: 0;
  padding: 0;
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 17px;
  color: #4b4b4b;
}

.delivery-filter-wrapper {
  width: 100%;
  height: auto;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 26px;
}

.delivery-filter-wrapper > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-text {
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
}

.column-modal-container {
  width: 100%;
  display: flex;
}

.column-modal-container > div {
  width: 50%;
}
</style>
