<template>
  <div class="toolings-productivity report-chart">
    <div class="row">
      <div class="col-lg-12">
        <div class="card-header card-header-custom">
          <strong>
            <span v-if="totalElements > 1" v-text="resources['there_are']"></span>
            <span v-else v-text="resources['there_is']"></span>
            <span class="emphasize-text">{{totalElements}}</span>
            <span v-if="totalElements > 1" v-text="resources['results']"></span>
            <span v-else v-text="resources['result']"></span>
            <span v-text="resources['found_in']"></span>
            <span class="emphasize-text" v-text="resources['capacity_utilization']"></span>
          </strong>
          <div>
            <!--            <a-popover v-model="visiblePopover" placement="bottomLeft" trigger="click">-->
            <!--              <a @click="animationOutlineDropdown()" :class="{'animation-secondary': showAnimationPopover}" href="javascript:void(0)" class="btn-custom btn-outline-custom-primary animationOutline btn-group btn-group-select-column all-time-filters">-->
            <!--                                                <span>-->
            <!--                                                    {{ sortDirection === 'desc' ? 'Sort - Highest to Lowest' : 'Sort - Lowest to Highest' }}-->
            <!--                                                </span>-->
            <!--                <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{'caret-show' : displayCaret}" alt="">-->
            <!--              </a>-->
            <!--              <a-menu-->
            <!--                  slot="content"-->
            <!--                  class="wrapper-action-dropdown"-->
            <!--                  style="border-right: unset!important;max-height: 250px;overflow: auto"-->
            <!--                  @click="visiblePopover = false"-->
            <!--              >-->
            <!--                <a-menu-item @click="sortDirection = 'desc'">-->
            <!--                  <span>Sort - Highest to Lowest</span>-->
            <!--                </a-menu-item>-->
            <!--                <a-menu-item @click="sortDirection = 'asc'">-->
            <!--                  <span>Sort - Lowest to Highest</span>-->
            <!--                </a-menu-item>-->
            <!--              </a-menu>-->
            <!--            </a-popover>-->
            <div class="d-flex align-items-center justify-content-end ">
              <div class="">
                <common-button
                    :title="Sort_Direction"
                    :is-show="showSortDirection"
                    is-blue="true"
                    @click="handleToggle"
                    class="btn-custom btn-custom-primary animationprimary btn-custom1 btn-custom1-primary"
                >
                  <!--              <span>{{Sort_Direction}}</span>-->
                  <!--              <span class="dropdown-icon-custom">-->
                  <!--                <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{'caret-show' : displayCaret}" alt="">-->
                  <!--              </span>-->
                  <!--                class=" btn-custom1 btn-custom1-primary"-->
                  <!--                :class="{'outline-animation': showAnimation}"-->
                </common-button>
                <common-select-popover
                    :is-visible="showSortDirection"
                    @close="handlecloseSortDirection">

                  <common-select-dropdown
                      :searchbox="false"
                      :class="{show:showSortDirection}"
                      :style="{position:'static'}"
                      :items="ItemsortDirections"
                      :checkbox="false"
                      class="dropdown"
                      :set-result="()=>{}"
                      :click-handler="select"
                  ></common-select-dropdown>

                </common-select-popover>
              </div>
              <div class="">
                <a @click="exportProductivityToPdf" href="javascript:void(0)"
                   class="btn-custom btn-group btn-group-select-column btn-group-select-column-last-child btn-custom-primary animationPrimary all-time-filters btn-custom-focus">
              <span>
                  <img src="/images/icon/export.svg" alt="">
                  <span v-text="resources['export']"></span>
              </span>
                </a>
              </div>
            </div>
          </div>
        </div>
        <template v-for="(item, index) in top5Tooling">
          <capacity-utilization-item
              v-if = "Object.entries(resources).length"
              :chart-id="`capacity-utilization-chart-${index}`"
              :tooling="item"
              :show-chart="showChart"
              :compare-by="compareBy"
              :resources="resources"
              :index="index"
              :page-number="page"
              :show-company="showCompany"
              :page-size="pageSize"
          >
          </capacity-utilization-item>
        </template>


        <div v-if="totalElements === 0" class="no-results" v-text="resources['no_results']">
          .
        </div>

        <capacity-utilization-export
            v-if = "Object.entries(resources).length"
            :get-request-param="getRequestParam"
            :tooling="tooling"
            :top5-tooling="top5Tooling"
            :change-chart-mold="changeChartMold"
            :change-productivity="changeProductivity"
            :toolingspart-id="toolingspartId"
            :sort-directions="sortDirections"
            :sort-direction="sortDirection"
            :compare-by="compareBy"
            :productivity-query="productivityQuery"
            :tooling-query="toolingQuery"
            :items="items"
            :show-chart="showChart"
            :sort="sortDirection"
            :resources="resources">
        </capacity-utilization-export>
        <div class="row">
          <div class="col-md-8">
            <ul class="pagination">
              <li v-for="(data, index) in pagination" class="page-item" :class="{active: data.isActive}">
                <a class="page-link"   @click="changePage(data.pageNumber)">{{data.text}}</a>
              </li>
            </ul>
          </div>
          <!--          <div class="col-auto ml-auto">-->
          <!--            <div class="export-action">-->
          <!--              <button-->
          <!--                  @click="exportProductivityToPdf"-->
          <!--                  class="btn btn-success"-->
          <!--                  type="button"-->
          <!--              >-->
          <!--                <span v-text="resources['export']"></span>-->
          <!--              </button>-->
          <!--            </div>-->
          <!--          </div>-->
        </div>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  components: {
    "capacity-utilization-export": httpVueLoader("./capacity-utilization-export.vue"),
    "capacity-utilization-item": httpVueLoader("./capacity-utilization-item.vue")
  },
  props: {
    resources: Object,
    changeProductivity: {
      type: Object,
      default: () => {
        return {
          top5Tooling: [{}, {}, {}, {}, {}],
          totalElements: 0
        };
      },
    },
    changePartId: [Number, String],
    filterTop: Function,
    getRequestParam: Function,
    changeChartMold: Function,
    tooling: Array,
    showCompare: String,
    productivityQuery: Object,
    toolingQuery: Function,
    items: Array,
    page: Number,
    pageSize: Number,
    totalPage: Number,
    pagination: Array,
    showCompany: Function,
    trend: Object,
    sort: String
  },
  computed: {
    top5Tooling() {
      return this.productivity.top5Tooling;
    },
    totalElements() {
      return this.productivity.totalElements;
    }
  },
  data() {
    return {
      charts: [],
      compareBy: {
        key: ''
      },
      filterCompare: [
        {
          id: 'TOOL',
          name: this.resources['compare_by_tooling'],
          key: 'Tool'
        },
        {
          id: 'SUPPLIER',
          name: this.resources['compare_by_supplier'],
          key: 'Supplier'
        },
        {
          id: 'TOOLMAKER',
          name: this.resources['compare_by_toolmaker'],
          key: 'Toolmaker'
        }
      ],
      toolingspartId: {},
      sortDirection: "desc",
      sortDirections: [
        {
          value: "desc",
          label: this.resources['highest_lowest'],
        },
        {
          value: "asc",
          label: this.resources['lowest_highest'],
        },
      ],
      productivity: {
        top5Tooling: [],
        totalElements: 0
      },
      displayCaret: false,
      showAnimationPopover: false,
      visiblePopover: false,
      showSortDirection: false,
      Sort_Direction: 'Sort - Highest to Lowest',
      ItemsortDirections: [
        {
          value: "desc",
          title: 'Sort - ' + this.resources['highest_lowest'],
        },
        {
          value: "asc",
          title: 'Sort - ' + this.resources['lowest_highest'],
        },
      ],
      displayCaret: false,
      showAnimation: false,
      visiblePopover: false
    };
  },
  methods: {
    handleshowSortDirection: function () {

      this.showSortDirection = !this.showSortDirection;

    },
    handlecloseSortDirection: function () {

      this.showSortDirection = false;
    },
    handleToggle: function (isShow) {
      this.showSortDirection = isShow;
      // if (!this.showSortDirection) {
      //
      //   this.handleshowSortDirection();
      //
      // } else {
      //
      //   this.handlecloseSortDirection();
      // }
      // this.displayCaret = true;
      // if (!this.showSortDirection) {
      //   this.showAnimation = true;
      //   setTimeout(() => {
      //     this.showAnimation = false;
      //   }, 700)
      // }

    },
    select(item) {
      // alert(item);

      this.Sort_Direction = item.title;
      this.showSortDirection = false;
      // console.log(item)
      this.sortDirection = item.value;

    },
    animationOutlineDropdown () {
      this.displayCaret = true;
      if (!this.visiblePopover) {
        this.showAnimationPopover = true;
        setTimeout(() => {
          this.showAnimationPopover = false;
        }, 700)
      }
    },
    changePage(number) {
      this.$emit('change-page', number)
    },
    showChart(chart) {
      this.changeChartMold(chart);
    },
    changePart(partId) {
      axios.get("/api/chart/dashboard-data").then((response) => {
        this.toolingspartId = response.data["list-search"].Part.filter(
            (value) => value.id === partId
        );
      });
    },
    exportProductivityToPdf() {
      let child = Common.vue.getChild(this.$children, "capacity-utilization-export");
      if (child != null) {
        child.exportProductivityToPdf();
      }
    }
  },
  watch: {
    showCompare (compare) {
      if(this.compareBy != null && compare != null) {
        this.compareBy = this.filterCompare.filter(value => value.id === compare)[0]
      }
    },
    changePartId(partId) {
      // this.changePart(partId);
    },
    changeProductivity(items) {
      this.productivity = items;
    },
    sortDirection(value) {
      this.filterTop(value);
    },
    showSortDirection(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    }
    // visiblePopover (newValue, oldValue) {
    //   if (newValue == false && oldValue == true) {
    //     this.displayCaret = false;
    //   }
    // }
  }
};
</script>
<style scoped>
.custom-dropdown-button {
  width: 200px !important;
  margin-top:-9px !important;
}

.caret-show {
  transform: rotate(180deg);
}

.set-dropdown-width {
  width: 200px;
}
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
/*summary content */
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
    margin: 1%;
    height: 109px;
    min-width: 220px;
    border-radius: 10px;
    display: flex;
    justify-content: space-between;
    padding: 0px 26px;
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
  width: 20px;
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
.btn-custom1 {
  /*text-align: left !important;*/
  border: 1px solid rgb(75 145 255) !important;
}

.dropdown-wrap {
  /*min-width: unset !important;*/
  width: 100% !important;
  border-radius: 3px !important;
  padding: 2px !important;
}

.common-popover {
  /*min-width: unset !important;*/
  min-width: 193px !important;
  width: 193px;
  margin-top: 3px;
  border-radius: 3px !important;
  padding: 2px !important;
}

/*.dropdown {*/
/*  !*width: 248% !important;*!*/
/*  !*max-width: 158% !important;*!*/
/*  width: calc(100% - 100px) !important;*/
/*}*/

.btn-custom1-primary {
  border: 1px solid #3491FF !important;
  border-radius: 3px !important;
  color: #3491FF !important;
  background: #ffffff !important;
  background-color: #ffffff !important;
  margin-right: 10px;
  top: -4px;
}

.img-transition {
  float: right !important;
  margin-top: 8px !important;
}

.dropdown-icon-custom {
  float: right;
}

.btn-custom1-primary:hover {
  background: #DEEDFF !important;
  outline: none !important;
  border: 1px solid #DEEDFF!important;

}

.btn-custom1-primary:focus {
  border: 2px solid #3491FF!important;
  background: #ffffff !important;
  /*background-color: transparent !important;*/
}

.dropdown-toggle::after {
  margin-left: 91%;
}
/*/summary content  */
</style>
