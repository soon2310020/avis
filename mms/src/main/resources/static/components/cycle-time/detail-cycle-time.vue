<template>
  <div class="detail-cycle-time">
    <div class="row">
      <div class="col-lg-12">
        <template v-for="(item, index) in top5Tooling">
          <cycle-time-item
                  :compare-id="compareId"
                  :chart-id="`cycle-time-chart-${index}`"
                  :tooling="item"
                  :show-chart="showChart"
                  :compare-by="compareBy"
                  :index="index"
                  :page-number="page"
                  :page-size="pageSize"
                  :show-company="showCompany"
                  :resources="resources">
          </cycle-time-item>
        </template>
      </div>
    </div>
    <div v-if="top5Tooling.length === 0" class="no-results" v-text="resources['no_results']">
        .
    </div>
    <div>
      <cycle-time-export :show-chart="showChart"
                         :compare-id="compareId"
                         :compare-by="compareBy"
                         :sort-directions="sortDirections"
                         :sort-direction="sortDirection"
                         :filter-cycle="changeProductivity"
                         :part-id-name="partIdName"
                         :time-id-name="timeIdName"
                         :trend="trend"
                         :sort="sort"
                         :items="items"
                         :top5-tooling="top5Tooling"
                         :get-request-param="getRequestParam"
                         :resources="resources">
      </cycle-time-export>
      <div class="row">
        <div class="col-md-8">
          <ul class="pagination">
            <li v-for="(data, index) in pagination" class="page-item" :class="{active: data.isActive}">
              <a class="page-link"   @click="changePage(data.pageNumber)">{{data.text}}</a>
            </li>
          </ul>
        </div>
<!--        <div class="col-auto ml-auto">-->
<!--          <div class="export-action">-->
<!--            <button @click="exportCycleTimeToPdf" class="btn btn-success" type="button" v-text="resources['export']">-->
<!--            </button>-->
<!--          </div>-->
<!--        </div>-->
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  data() {
    return {
      charts: [],
      partTitle: {},
      sortDirection: 'desc',
      sortDirections: [
        {
          value: 'desc',
          label: "Highest to lowest"
        },
        {
          value: 'asc',
          label: "Lowest to highest"
        }
      ],
      productivity: {
        top5Tooling: [],
        trendMolds: []
      },
      initProductivity: {
        top5Tooling: [{}, {}, {}, {}, {}, {}, {}]
      },
    };
  },
  components: {
    'progress-bar': httpVueLoader('../progress-bar.vue'),
    "cycle-time-export": httpVueLoader("./cycle-time-export.vue"),
    "cycle-time-item": httpVueLoader("./cycle-time-item.vue")
  },
  props: {
    changeProductivity: Object,
    compareBy: [Array, Object],
    titlePart: Object,
    filterTop: Function,
    getRequestParam: Function,
    changeChartMold: Function,
    partIdName: Object,
    timeIdName: Object,
    items: Array,
    resources: Object,
    page: Number,
      pageSize: Number,
    totalPage: Number,
    pagination: Array,
    compareId: Object,
    showCompany: Function,
    trend: Object,
    sort: String
  },
  methods: {
    changePage(number) {
      this.$emit('change-page', number)
    },
    showChart (items) {
      this.changeChartMold(items)
    },
    exportCycleTimeToPdf() {
      let child = Common.vue.getChild(this.$children, "cycle-time-export");
      if (child != null) {
        child.exportCycleTimeToPdf();
      }

    },
  },
  computed: {
    top5Tooling() {
      if(this.productivity.top5Tooling != 'undefined') {
        return this.productivity.top5Tooling;
      } else {
        return ''
      }
    }
  },
  watch: {
    titlePart(item) {
      this.partTitle = item
    },
    changeProductivity(items) {
      this.productivity = items;
      if (this.compareBy.key === '' || this.compareBy.key === 'Tool') {

      }
      this.productivity.top5Tooling.forEach(item => {
        this.productivity.trendMolds .forEach(value => {
          (this.compareBy.key === '' || this.compareBy.key === 'Tool') ?
            (item.moldId === value.moldId ? item.trend = value.trend : '') :
            (item.id === value.id ? item.trend = value.trend : '')
        })
      })
    },
    sortDirection(value) {
      this.filterTop(value);
    },
  },
};
</script>
<style scoped>
.chart-progress-container {
  align-items: center;
  padding-bottom: 55px;
  cursor: pointer;
}
.chart-cycle-time .chartjs-render-monitor {
  height: 150px !important;
}
.text {
  margin-bottom: 0px;
}
.font-size-10 {
  font-size: 15px;
}
#production-chart {
  height: 177px !important;
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

  /*summary content*/
@media (max-width: 576px) {
  .statistical-item-container {
    width: 100%;
    height: 109px;
    min-width: 220px;
    border-radius: 10px;
    display: flex;
    justify-content: space-between;
    padding: 0px 26px;
    color: black;
    margin-top: 20px;
    margin: 1%;
    align-items: center;
    margin-bottom: 5px;
    -moz-box-shadow: 3px 3px #e8e8e8;
    -webkit-box-shadow: 3px 3px #e8e8e8;
    box-shadow: 3px 3px #e8e8e8;
  }
}
@media (min-width: 576px) {
  .statistical-item-container {
    width: 23%;
    height: 109px;
    min-width: 220px;
    border-radius: 10px;
    display: flex;
    justify-content: space-between;
    padding: 0px 26px;
    margin: 1%;
    margin-left: 10p;
    background: white;
    color: black;
    border: 0.5px solid #e8e8e8;
    align-items: center;
    margin-bottom: 5px;
    -moz-box-shadow: 3px 3px #e8e8e8;
    -webkit-box-shadow: 3px 3px #e8e8e8;
    box-shadow: 3px 3px 3px 3px #e8e8e8;
  }
}
::-webkit-scrollbar{
  width: 10px;
}
::-webkit-scrollbar-thumb {
  background: #c8ced3;
}
.total-value {
  font-size: 30px;
  /*font-weight: 900;*/
  line-height: 100%;
  /*font-family: Raleway;*/
  font-weight: 600;
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;

}
.total-title {
  font-size: 15px;
  font-weight: 600;
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
}
  /*/summary content*/
</style>
