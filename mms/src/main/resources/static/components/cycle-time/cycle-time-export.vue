<template>
	<div class="export-area" >
			<div id="export-content">
					<div class="group-item" v-for="(groupItem, groupIndex) in exportData" :key="groupIndex">
							<div class="page-content">
									<div class="summary-area" v-if="groupIndex === 0">
										<div class="card">
											<div class="card-header">
												<strong v-text="resources['cycle_time']"></strong>
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
																								<div class="input-group-text" style="width: 100%"><i class="fa fa-search"></i></div>
																						</div>
																						<cycle-filters
																						:option-array-productivity="[partIdName]"
																						:width="87"
																						:backgrounds="`white`"
																						filter-dialog="true"
																						:cycle-placeholder="resources['all_parts']"
																						:resources="resources"></cycle-filters>
																				</div>
																		</div>

																		<div class="col-xl mb-3 mb-md-0">
<!--																			<location-filter title="country" type="countries" :title-placeholder="resources['all_countries']"-->
<!--																			:option-array="[]" all-type="All countries" :resources="resources">-->
<!--																			</location-filter>-->
                                      <location-filter :title="resources['supplier']" type="supplier" :title-placeholder="resources['all_suppliers_normal']"
                                       :option-array="[]" all-type="All suppliers" :resources="resources">
                                      </location-filter>
																		</div>

																		<div class="col-xl mb-3 mb-md-0">
																			<cycle-filters
																				:option-array-productivity="[timeIdName]"
																				:backgrounds="`white`"
																				:width="100"
																				filter-dialog="false"
																				:resources="resources"></cycle-filters>
																		</div>

																		<div class="col-xl mb-3 mb-md-0">
																			<location-filter title="status" type="status" :title-placeholder="resources['all_statuses']"
																			:option-array="[]" all-type="All statuses" :resources="resources">
																			</location-filter>
																		</div>

																		<div class="col-xl mb-3 mb-md-0">
																			<cycle-filters
																				:option-array-productivity="[compareBy]"
																				:width="100"
																				filter-dialog="false"
																				:backgrounds="`white`"
																				:cycle-placeholder="resources['compare_by_toolmaker']"
																				:resources="resources"></cycle-filters>
																		</div>
																</div>
														</div>
												</div>
											</div>
										</div>
											<cycle-time-summary v-if="Object.entries(resources).length" chart-id="custom-id-export" :compare-id="compareBy" :items="items" :trend-data="trendData" :resources="resources"></cycle-time-summary>
											<div class="row">
													<div class="col-lg-12">
															<div class="card-header card-header-custom">
                                <strong>
                                  <span v-if="filterCycle.totalElements > 1">There are</span>
                                  <span v-else>There is</span>
                                  <span class="emphasize-text" style="text-decoration: underline">{{filterCycle.totalElements}}</span>
                                  <span v-if="filterCycle.totalElements > 1">results</span>
                                  <span v-else>result</span>
                                  <span>found in</span>
                                  <span class="emphasize-text" style="text-decoration: underline" v-text="resources['cycle_time']"> </span>
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
<!--																	<div class="col-md-3 mb-3 mb-md-0">-->
<!--																			<select class="form-control" v-model="sort">-->
<!--                                        <option :value="sort">{{ sort === 'asc' ? sortDirections[1].label : sortDirections[0].label}}</option>-->
<!--																			</select>-->
<!--																	</div>-->
															</div>
													</div>
											</div>
									</div>
									<template v-for="item in groupItem">
											<cycle-time-item
											v-if="Object.entries(resources).length"
                      :compare-id="compareId"
															:chart-id="`export-cycle-time-chart-${item.index + 1}`"
															:tooling="item"
															:show-chart="showChart"
															:compare-by="compareBy"
															:index="item.index"
															:resources="resources">
											</cycle-time-item>
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
			<progress-bar v-if="Object.entries(resources).length" :stop="stopExport" :progress-status="exportStatus" :resources="resources"></progress-bar>
	</div>
</template>

<script>
	module.exports = {
			name: "cycle-time-export",
			components: {
					"progress-bar": httpVueLoader("../progress-bar.vue"),
					'cycle-time-summary': httpVueLoader('./cycle-time-summary.vue'),
					"cycle-time-item": httpVueLoader("./cycle-time-item.vue"),
					'cycle-filters': httpVueLoader('/components/cycle-time/cycle-time-filters.vue'),
					'location-filter': httpVueLoader('/components/cycle-time/location-filter.vue'),
			},
			props: {
					showChart: Function,
					compareBy: Object,
					sortDirections: Array,
					sortDirection: String,
					filterCycle:Object,
					partIdName: Object,
					timeIdName: Object,
					getRequestParam: Function,
					items:Array,
					top5Tooling: Array,
					resources: Object,
          trend: Object,
          sort: String,
          compareId: Object
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
									currentIntervalIds: []
							},
              trendData: {},
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
					exportCycleTimeToPdf() {
							if (!this.top5Tooling.length) {
									Common.alert("There is no data to export");
									return;
							}
							this.exportStatus.isStoppedExport = false;
							this.exportStatus.isCallingDone = false;
							if (this.top5Tooling.length > 0) {
									this.progressBarRun();
							}
							let param = this.getRequestParam();
							// delete param.size;
							param.size=9999;
              param.page=1;

							param = Common.param(param);
							axios.get(`/api/reports/cycle-time/tooling${param ? '?' + param: ''}`).then(response => {
									let data = response.data.top5Tooling;
                  this.trendData = this.trend
									if (!data.length) {
											Common.alert('There is no data to export');
											this.exportStatus.isCallingDone = true;
											this.exportStatus.isStoppedExport = true;
											return;
									}

									// if (this.compareBy.key === '' || this.compareBy.key === 'Tool') {

									// }

									data.forEach(item => {
											response.data.trendMolds.forEach(value => {
													if (this.compareBy.key === '' || this.compareBy.key === 'Tool') {
															if (item.moldId === value.moldId) {
																	item.trend = value.trend;
															}
													} else if (item.id === value.id) {
															item.trend = value.trend
													}
											})
									});
									this.exportData = [];
									this.exportFileName = moment().format('YYYYMMDD') + '_eMoldino_cycle_time_report.pdf';
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
							}).catch(error => {
									this.exportStatus.isStoppedExport = true;
									console.log('error', error);
							});
					},
					exportPageToPdf() {
							Common.exportPageToPdf(this.exportStatus, this.exportFileName);
					},
					progressBarRun: function () {
							let child = Common.vue.getChild(this.$children, 'progress-bar');
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
		width: 220px;
	}
}
</style>
