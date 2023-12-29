<template>
  <div class="detail-supplier production-efficiency">
    <div class="row">
      <div class="col-lg-12">
        <template v-for="(item, index) in productivity.top5Supplier">
          <production-efficiency-item
                  :supplier="item"
                  :index="index"
                  :page-number="page"
                  :page-size="pageSize"
                  :tooling="item"
                  :compare-by="compareBy"
                  :show-chart="showChart"
                  :chart-id="'production-efficiency-chart-' + index"
                  :show-company="showCompany"
                  :resources="resources">
          </production-efficiency-item>
        </template>
      </div>
    </div>
    <div v-if="top5Supplier.length === 0" class="no-results" v-text="resources['no_results']">
    </div>
    <production-efficiency-export
            :top5-supplier="top5Supplier"
            :compare-id="compareId"
            :filter-supplier="filterSupplier"
            :part-id-name="partIdName"
            :time-id-name="timeIdName"
            :sort-direction="sortDirection"
            :sort-directions="sortDirections"
            :compare-by="compareBy"
            :get-request-param="getRequestParam"
            :items="items"
            :resources="resources"
            :sort="sort"
    ></production-efficiency-export>

    <div class="row">
      <div class="col-md-8">
        <ul class="pagination">
          <li v-for="(data, index) in pagination" class="page-item" :class="{active: data.isActive}">
            <a class="page-link"   @click="changePage(data.pageNumber)">{{data.text}}</a>
          </li>
        </ul>
      </div>
<!--      <div class="col-auto ml-auto">-->
<!--        <div class="export-action">-->
<!--          <button-->
<!--              @click="exportSupplierToPdf"-->
<!--              class="btn btn-success"-->
<!--              type="button"-->
<!--          >-->
<!--            <span v-text="resources['export']"></span>-->
<!--          </button>-->
<!--        </div>-->
<!--      </div>-->
    </div>

  </div>
</template>
<script>
module.exports = {
  components: {
    "progress-bar": httpVueLoader("../progress-bar.vue"),
    "production-efficiency-item": httpVueLoader("./production-efficiency-item.vue"),
    "production-efficiency-export": httpVueLoader("./production-efficiency-export.vue"),
  },
  data() {
    return {
      charts: [],
    };
  },
  props: {
    compareBy: String,
    compareId: Object,
    getRequestParam: Function,
    partIdName: Object,
    timeIdName: Object,
    filterSupplier: Object,
    sortDirection: String,
    sortDirections: Array,
    items: Array,
    productivity: Object,
    resources: Object,
    showChart: Function,
    page: Number,
    pageSize: Number,
    totalPage: Number,
    pagination: Array,
    showCompany: Function,
    sort: String
  },
  methods: {
    changePage(number) {
      this.$emit('change-page', number)
    },
    exportSupplierToPdf() {
        let child = Common.vue.getChild(this.$children, "production-efficiency-export");
        if (child != null) {
            child.exportSupplierToPdf();
        }
    }
  },
  computed: {
    top5Supplier() {
      if (this.productivity.top5Supplier === null) {
        return "";
      }
      return this.productivity.top5Supplier;
    },
  }
};
</script>
<style scoped>
.chart-progress-container {
  align-items: center;
  padding-bottom: 55px;
}
.chart-supplier .chartjs-render-monitor {
  height: 150px !important;
}
.text {
  margin-bottom: 0px;
}
.font-size-10 {
  font-size: 15px;
}
.char-line {
  width: 13px;
  height: 13px;
  -moz-border-radius: 25px;
  -webkit-border-radius: 25px;
  --border-radius: 25px;
  border-radius: 25px;
}
.font-productivity {
  font-size: 55px;
  font-family: initial;
}
.for-production {
  width: 7px;
  height: 7px;
  background: black;
  margin-top: 10px;
}
.progress-full {
  height: 23px;
  width: 100%;
  border-radius: 15px;
  background: #e4e7ef;
  position: relative;
  overflow: hidden;
}
.progress {
  height: 23px;
  border-radius: 15px;
}
.button {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  width: 70%;
  border-radius: 17px;
  height: 45px;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  margin: 20px 0px;
}
.body {
  display: flex;
  flex-direction: column;
  padding: 0rem 0px;
  margin-left: 20px;
  justify-content: space-around;
  padding: 20px;
  /* //align-items: center; */
}
.ratio-percent {
  position: absolute;
  top: -25px;
  right: 10px;
  font-size: 12px;
  font-weight: 600;
}
</style>
