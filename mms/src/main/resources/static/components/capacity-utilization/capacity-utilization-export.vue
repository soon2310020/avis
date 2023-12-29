<template>
  <div class="export-area">
      <div id="export-content">
          <div class="chart-area">
              <div
                      class="group-item"
                      v-for="(groupItem, groupIndex) in exportData"
                      :key="groupIndex"
              >
                  <div class="page-content">
                      <div class="summary-area" v-if="groupIndex === 0">
                        <div class="row">
                          <div class="col-lg-12">
                            <div class="card">
                              <div class="card-header">
                                <strong v-text="resources['capacity_utilization']"></strong>
                                <div class="card-header-actions">
                                  <span class="card-header-action">
                                    <!-- <small class="text-muted">Total</small> -->
                                  </span>
                                </div>
                              </div>
                              <div class="card-body">
                                <input type="hidden" />

                                <div class="form-row">
                                  <div class="col-md-12 col-12">
                                    <div class="form-row">
                                      <div class="col-xl mb-3 mb-md-0 col-12">
                                        <div class="input-group">
                                          <div class="input-group-prepend" style="width: 13%">
                                            <div class="input-group-text" style="width: 100%">
                                              <i class="fa fa-search"></i>
                                            </div>
                                          </div>
                                          <cycle-filters
                                          v-if = "Object.entries(resources).length"
                                            :option-array-productivity="[productivityQuery.partName]"
                                            :width="87"
                                            :backgrounds="`white`"
                                            filter-dialog="true"
                                            :cycle-placeholder="resources['all_parts']"
                                            :resources="resources"></cycle-filters>
                                        </div>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
<!--                                        <location-filter title="country"-->
<!--                                         v-if = "Object.entries(resources).length" type="countries" :title-placeholder="resources['all_countries']"-->
<!--                                        :option-array="[]" all-type="All countries" :resources="resources">-->
<!--                                        </location-filter>-->
                                        <location-filter v-if = "Object.entries(resources).length"
                                         :title="resources['supplier']" type="supplier" :title-placeholder="resources['all_suppliers_normal']"
                                         :option-array="[]" all-type="All suppliers" :resources="resources">
                                        </location-filter>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
                                        <cycle-filters
                                        v-if = "Object.entries(resources).length"
                                        :option-array-productivity="[productivityQuery.lastdays]"
                                        :width="100"
                                        :backgrounds="`white`"
                                        filter-dialog="false"
                                        :resources="resources"></cycle-filters>
                                      </div>

                                      <div class="col-xl">
                                        <location-filter
                                        v-if = "Object.entries(resources).length" title="status" type="status" :title-placeholder="resources['all_statuses']"
                                        :option-array="[]" all-type="All statuses" :resources="resources">
                                        </location-filter>
                                      </div>

                                      <div class="col-xl">
                                        <cycle-filters
                                        v-if = "Object.entries(resources).length"
                                          :option-array-productivity="[productivityQuery.Compare]"
                                          :width="100"
                                          :backgrounds="`white`"
                                          filter-dialog="false"
                                          :cycle-placeholder="`Compare by tooling`"
                                          :resources="resources"></cycle-filters>
                                      </div>

                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                          <capacity-utilization-summary
                          v-if = "Object.entries(resources).length"
                                  :chart-id="`chartCapa`"
                                  :change-productivity="changeProductivity"
                                  :gettooling="tooling"
                                  :query="query"
                                  :tooling-query="toolingQuery"
                                  :productivity-Query="productivityQuery"
                                  :productivity-data="productivity"
                                  :items="items"
                                  :is-export="true"
                                  :resources="resources"
                          ></capacity-utilization-summary>
                          <div class="row">
                              <div class="col-lg-12">
                                  <div class="card-header card-header-custom">
                                    <strong>
                                      <span v-if="totalElements > 1">There are</span>
                                      <span v-else>There is</span>
                                      <span class="emphasize-text">{{totalElements}}</span>
                                      <span v-if="totalElements > 1">results</span>
                                      <span v-else>result</span>
                                      <span>found in</span>
                                      <span class="emphasize-text">Capacity Utilization</span>
                                    </strong>
                                    <div>
                                      <a-popover v-model="visiblePopover" placement="bottomLeft" trigger="click">
                                        <a @click="animationOutlineDropdown()" :class="{'animation-secondary': showAnimationPopover}" href="javascript:void(0)" class="btn-custom btn-outline-custom-primary animationOutline btn-group btn-group-select-column all-time-filters">
                                                <span>
                                                    {{ sort === 'desc' ? 'Sort - Highest to Lowest' : 'Sort - Lowest to Highest' }}
                                                </span>
                                          <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{'caret-show' : displayCaret}" alt="">
                                        </a>
                                        <a-menu
                                            slot="content"
                                            class="wrapper-action-dropdown"
                                            style="border-right: unset!important;max-height: 250px;overflow: auto"
                                            @click="visiblePopover = false"
                                        >
                                          <a-menu-item @click="sort = 'desc'">
                                            <span>Sort - Highest to Lowest</span>
                                          </a-menu-item>
                                          <a-menu-item @click="sort = 'asc'">
                                            <span>Sort - Lowest to Highest</span>
                                          </a-menu-item>
                                        </a-menu>
                                      </a-popover>
                                      <a @click="exportProductivityToPdf" href="javascript:void(0)" class="btn-custom btn-group btn-group-select-column btn-group-select-column-last-child btn-custom-primary animationPrimary all-time-filters btn-custom-focus">
                                        <span>
                                            <img style="margin-left: 10px" src="/images/icon/export.svg" alt="">
                                            <span v-text="resources['export']"></span>
                                        </span>
                                      </a>
                                    </div>
                                  </div>
                              </div>
                          </div>
                      </div>
                      <template v-for="item in groupItem">
                          <capacity-utilization-item
                                  v-if="item.id"
                                  v-if = "Object.entries(resources).length"
                                  :resources="resources"
                                  :chart-id="`export-capacity-utilization-chart-${item.index + 1}`"
                                  :tooling="item"
                                  :show-chart="showChart"
                                  :compare-by="compareBy"
                                  :index="item.index">
                          </capacity-utilization-item>
                      </template>
                  </div>
                  <div class="page-footer">
                      <div class="page-title">
                          {{ exportFileName }}
                      </div>
                      <div class="page-logo">
                          <img class="export-logo" src="/images/export-report/logo_footer.png" alt="export-logo" />
                      </div>
                      <div class="page-number">
                          page {{ groupIndex + 1 }} of {{ totalExportPage }}
                      </div>
                  </div>
              </div>
          </div>
      </div>
      <progress-bar v-if="Object.entries(resources).length" :stop="stopExport" :progress-status="exportStatus" :resources="resources"></progress-bar>
  </div>
</template>

<script>
  module.exports = {
      name: "capacity-utilization-export",
      components: {
          "progress-bar": httpVueLoader("../progress-bar.vue"),
          'capacity-utilization-summary': httpVueLoader('./capacity-utilization-summary.vue'),
          "capacity-utilization-item": httpVueLoader("./capacity-utilization-item.vue"),
          'cycle-filters': httpVueLoader('/components/cycle-time/cycle-time-filters.vue'),
          'location-filter': httpVueLoader('/components/cycle-time/location-filter.vue'),
      },
      props: {
          changeChartMold: Function,
          getRequestParam: Function,
          tooling: Array,
          changeProductivity: Object,
          top5Tooling: Array,
          totalElements:0,
          toolingspartId: [Object, Array],
          sortDirections: Array,
          sortDirection: String,
          compareBy: Object,
          query: Function,
          toolingQuery: Function,
          productivityQuery: Object,
          showChart: Function,
          items: Array,
          resources: Object,
          trend: Object,
          sort: String,
          displayCaret: false,
          showAnimationPopover: false,
          visiblePopover: false
      },
      data() {
          return {
              productivity: {
                  top5Tooling: [],
              },
              exportData: [[{}]],
              itemPerExportPage: 5,
              totalExportPage: 0,
              exportFileName: null,
              exportStatus: {
                  isCallingDone: false,
                  isStoppedExport: false,
                  currentIntervalIds: [],
              },
              titleTooling: "",
              listTooling: {}
          }
      },
      watch: {
          changeProductivity(items) {
              this.productivity = items;
          },
          tooling (item) {
              let itemObject = this.items.filter((item) => item.id === this.itemid)[0];
            if (itemObject == null) {
              console.log("tooling null in capacity utilization export")
              return;
            }
              if (this.itemid === 1) {
                  this.listTooling = item.map(item => {
                      return {
                          id: item[itemObject.moldcode],
                          name: item[itemObject.key].toPrecision(2) + '%'
                      }
                  })
              }else if (this.itemid === 4) {
                  this.listTooling = item.map(item => {
                      if(item[itemObject.key] === null) {
                          return {
                              id: item[itemObject.moldcode],
                              name: 'N/A'
                          }
                      } else if (item[itemObject.key] === -999999) {
                          return {
                              id: item[itemObject.moldcode],
                              name: 'INF'
                          }
                      } else {
                          return {
                              id: item[itemObject.moldcode],
                              name: item[itemObject.key].toFixed(0) + '%'
                          }
                      }
                  })
              } else {
                  this.listTooling = item.map(item => {
                      return {
                          id: item[itemObject.moldcode],
                          name: item[itemObject.key].toLocaleString("en-US")
                      }
                  })
              }
          },
          visiblePopover (newValue, oldValue) {
            if (newValue == false && oldValue == true) {
              this.displayCaret = false;
            }
          }
      },
      methods: {
        animationOutlineDropdown () {
          this.displayCaret = true;
          if (!this.visiblePopover) {
            this.showAnimationPopover = true;
            setTimeout(() => {
              this.showAnimationPopover = false;
            }, 700)
          }
        },
          checkMoldCode (mold) {
              if (!mold) {
                  return
              }
              return this.compareBy.key === '' || this.compareBy.key === 'Tool' ? mold.moldCode : mold.name
          },
          checkTrend (trend) {
              if (trend == undefined) {
                  return 'N/A';
              }

              return trend > 0 ? `+${trend.toFixed(2)}%` :
                  (trend === -999999 ? 'INF' : `${trend.toFixed(2)}%` );
          },
          progressBarRun: function () {
              let child = Common.vue.getChild(this.$children, "progress-bar");
              if (child != null) {
                  child.progressBarRun();
              }
          },
          exportProductivityToPdf() {
              if (!this.top5Tooling.length) {
                  Common.alert("There is no data to export");
                  return;
              }
              this.exportStatus.isStoppedExport = false;
              this.exportStatus.isCallingDone = false;
              this.progressBarRun();
              let param = this.getRequestParam();
              // delete param.size;
              param.size=9999;
              param.page=1;
              param = Common.param(param);
              axios
                  .get(`/api/reports/productivity/tooling${param ? "?" + param : ""}`)
                  .then((response) => {
                      let data = response.data.top5Tooling;
                      let totalElements = response.data.totalElements;
                      if (!data.length) {
                          this.exportStatus.isCallingDone = true;
                          this.exportStatus.isStoppedExport = true;
                          return;
                      }
                      this.exportData = [];
                      this.exportFileName =
                          moment().format("YYYYMMDD") +
                          "_eMoldino_capacity_utilization_report.pdf";
                      data.forEach((item, index) => {
                          let computedIndex = index;
                          if (this.exportData.length > 1 || index === this.itemPerExportPage - 1) {
                              computedIndex = index + 1;
                          }
                          item.index = index;
                          let groupIndex = Math.floor(computedIndex / this.itemPerExportPage);
                          if (!this.exportData[groupIndex]) {
                              this.exportData[groupIndex] = [];
                          }
                          this.exportData[groupIndex].push(item);
                      });
                      this.totalExportPage = this.exportData.length;
                      // group data
                      setTimeout(() => {
                          this.exportPageToPdf();
                      }, 3000+(response.data.totalElements || 0)*50);
                  })
                  .catch((error) => {
                      this.exportStatus.isStoppedExport = true;
                      console.log("error", error);
                  });
          },
          exportPageToPdf() {
              Common.exportPageToPdf(this.exportStatus, this.exportFileName);
          },
          stopExport: function () {
              this.exportStatus.isStoppedExport = true;
          },
      }
  }
</script>

<style scoped>
.col-lg-12 {
  padding-left: 0!important;
  padding-right: 0!important;
}
.total-container .total-title {
  font-size: 13px;
}
.total-container .total-value {
  font-size: 23px;
}
.font-size-10 {
  font-size: 12px!important;
}
@media only screen and (min-width: 768px) {
	.strong-text {
		width: 275px;
	}
}
</style>
