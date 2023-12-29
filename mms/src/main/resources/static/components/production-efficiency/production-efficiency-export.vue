<template>
	<div class="export-area">
			<div id="export-content">

					<div
									class="group-item"
									v-for="(groupItem, groupIndex) in exportData"
									:key="groupIndex"
					>
							<div class="page-content">
									<div class="summary-area" v-if="groupIndex === 0">
											<div class="card">

                        <div class="card-header">
                          <strong v-text="resources['production_efficiency']"></strong>
                          <div class="card-header-actions">
                              <span class="card-header-action">
                                  <small class="text-muted"></small>
                              </span>
                          </div>
                        </div>

                        <div class="card-body">
                          <!-- <input type="hidden" v-model="requestParam.sort" /> -->

                          <div class="form-row">
                              <div class="col-md-12 col-12">
                                  <div class="form-row">
                                      <div class="col-xl mb-3 mb-md-0 col-12 ">
                                          <div class="input-group">
                                              <div class="input-group-prepend" style="width: 13%">
                                                  <div class="input-group-text" style="width: 100%">
                                                  <i class="fa fa-search"></i></div>
                                              </div>
                                              <production-efficiency-filters
                                              :option-array-productivity="[partIdName]"
                                              :width="87"
                                              filter-dialog="true"
                                              :backgrounds="`white`"
                                              :resources="resources"></production-efficiency-filters>
                                          </div>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
<!--                                        <location-filter title="country" type="countries" title-placeholder='All countries'-->
<!--                                        :option-array="[]" all-type="All countries" :resources="resources">-->
<!--                                        </location-filter>-->
                                        <location-filter :title="resources['supplier']" type="supplier" :title-placeholder="resources['all_suppliers_normal']"
                                                         :option-array="[]" all-type="All suppliers" :resources="resources">
                                        </location-filter>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
                                        <production-efficiency-filters
                                          :option-array-productivity="[timeIdName]"
                                          :backgrounds="`white`"
                                          :width="100"
                                          filter-dialog="false"
                                          :resources="resources"></production-efficiency-filters>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
                                        <location-filter title="status" type="status" title-placeholder='All statuses'
                                        :option-array="[]" all-type="All statuses" :resources="resources">
                                        </location-filter>
                                      </div>

                                      <div class="col-xl mb-3 mb-md-0">
                                        <production-efficiency-filters
                                          :option-array-productivity="[compareId]"
                                          :width="100"
                                          :backgrounds="`white`"
                                          filter-dialog="false"
                                          :cycle-placeholder="`Compare by supplier`"
                                          :resources="resources"></production-efficiency-filters>
                                      </div>
                                  </div>
                              </div>
                          </div>
                        </div>
                      </div>
											<production-efficiency-summary v-if="Object.entries(resources).length" chart-id="chart-pro-export" :compare-id="compareId" :productivity-data="filterSupplier" :change-productivity="filterSupplier" :items="items" :resources="resources"></production-efficiency-summary>
											<div class="row">
													<div class="col-lg-12">
															<div class="card-header card-header-custom">
                                <strong>
                                  <span v-if="filterSupplier.totalElements > 1">There are</span>
                                  <span v-else>There is</span>
                                  <span class="emphasize-text">{{filterSupplier.totalElements}}</span>
                                  <span v-if="filterSupplier.totalElements > 1">results</span>
                                  <span v-else>result</span>
                                  <span>found in</span>
                                  <span class="emphasize-text">Production Efficiency</span>
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
                                  <a href="javascript:void(0)" class="btn-custom btn-group btn-group-select-column btn-group-select-column-last-child btn-custom-primary animationPrimary all-time-filters btn-custom-focus">
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
											<production-efficiency-item
															:chart-id="`export-production-efficiency-chart-${item.index + 1}`"
                              :tooling="item"
															:supplier="item"
															:compare-by="compareBy"
															:index="item.index"
															:resources="resources">
											</production-efficiency-item>
									</template>
							</div>
							<div class="page-footer">
									<div class="page-title" style="color: black">
											{{ exportFileName }}
									</div>
									<div class="page-logo">
											<img
															class="export-logo"
															src="/images/export-report/logo_footer.png"
															alt="export-logo"
											/>
									</div>
									<div class="page-number">
											page {{ groupIndex + 1 }} of {{ totalExportPage }}
									</div>
							</div>
					</div>
			</div>
			<progress-bar   v-if="Object.entries(resources).length"
							:stop="stopExport"
							:progress-status="exportStatus"
							:resources="resources"
			></progress-bar>
	</div>
</template>

<script>
	module.exports = {
			name: "production-efficiency-export",
			components: {
					"progress-bar": httpVueLoader("../progress-bar.vue"),
					'production-efficiency-summary': httpVueLoader('./production-efficiency-summary.vue'),
					"production-efficiency-item": httpVueLoader("./production-efficiency-item.vue"),
          'location-filter': httpVueLoader('/components/cycle-time/location-filter.vue'),
          'production-efficiency-filters': httpVueLoader('/components/cycle-time/cycle-time-filters.vue'),
			},
			props: {
					top5Supplier: Array,
					compareId: Object,
					filterSupplier: Object,
					partIdName: Object,
                    timeIdName: Object,
					sortDirection: String,
					sortDirections: Array,
					compareBy: String,
					getRequestParam: Function,
					items: Array,
					resources: Object,
          sort: String
			},
			data() {
					return {
							exportData: [],
							itemPerExportPage: 5,
							totalExportPage: 0,
							exportFileName: null,
							exportStatus: {
									isCallingDone: false,
									isStoppedExport: false,
									currentIntervalIds: [],
							},
            displayCaret: false,
            showAnimationPopover: false,
            visiblePopover: false
					}
			},
			watch: {
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
					exportSupplierToPdf() {
							if (!this.top5Supplier.length) {
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
									.get(`/api/reports/supplier/production${param ? "?" + param : ""}`)
									.then((response) => {
											let data = response.data.top5Supplier;
											if (data != null) {
													if (!data.length) {
															this.exportStatus.isCallingDone = true;
															this.exportStatus.isStoppedExport = true;
															return;
													}
													this.exportData = [];
													this.exportFileName = moment().format("YYYYMMDD") +
															"_eMoldino_production_efficiency_report.pdf";
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
													setTimeout(() => {
															this.exportPageToPdf();
													}, 3000+(response.data.totalElements || 0)*50);
											}
									})
									.catch((error) => {
											this.exportStatus.isStoppedExport = true;
											console.log("error", error);
									});
					},
					exportPageToPdf() {
							Common.exportPageToPdf(this.exportStatus, this.exportFileName);
					},
					progressBarRun: function () {
							let child = Common.vue.getChild(this.$children, "progress-bar");
							if (child != null) {
									child.progressBarRun();
							}
					},
					stopExport: function () {
							this.exportStatus.isStoppedExport = true;
					}
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
		width: 330px;
	}
}
</style>