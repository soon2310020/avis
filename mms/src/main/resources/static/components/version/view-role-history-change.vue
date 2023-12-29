<template>
  <div
    id="view-role-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['role']"></h5>
          <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
        </div>
        <div class="modal-body">
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <strong v-text="resources['role']"></strong>
                        </div>
                        <div class="card-body" v-show="!loading">
                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" v-text="resources['type']"></span>
                                <div class="col-md-10">
                                    <span type="text" class="form-control">
                                        <span type="text" class="form-control" v-text="resources['tooling_access_group']">
                                    </span>
                                </div>
                            </div>
                             <div class="form-group row">
                                <span class="col-md-2 col-form-label" v-text="resources['code']"></span>
                                <div class="col-md-10">
                                    <span type="text" class="form-control">
                                        <span v-bind:class="{'version-change-old': after.authority != before.authority}">{{before.authority}}</span>
                                        <span v-if="after.authority !== before.authority" class="version-change-new">{{after.authority}}</span>
                                        <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>
                             <div class="form-group row">
                                <span class="col-md-2 col-form-label" v-text="resources['name']"></span>
                                <div class="col-md-10">
                                    <span type="text" class="form-control">
                                        <span v-bind:class="{'version-change-old': after.name != before.name}">{{before.name}}</span>
                                        <span v-if="after.name !== before.name" class="version-change-new">{{after.name}}</span>
                                        <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>
                             <div class="form-group row">
                                <span class="col-md-2 col-form-label" v-text="resources['description']"></span>
                                <div class="col-md-10">
                                    <span type="text" class="form-control">
                                        <span v-bind:class="{'version-change-old': after.description != before.description}">{{before.description}}</span>
                                        <span v-if="after.description !== before.description" class="version-change-new">{{after.description}}</span>
                                        <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>
                        </div>
                        </div>
                    </div>
                </div>
            <div class="row" v-if="roleType === 'ROLE_MENU'">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <strong>
                                <span>
                                    <span v-text="resources['mms_menu_access']">
                                </span>
                                <span style="color: lightgrey">
                                    <input type="checkbox" class="input_checkbox" disabled/> <span v-text="resources['check_all']"></span>
                                </span>
                            </strong>
                        </div>
                        <div class="card-body" v-if="after.mmsMenuAccessDto != null && after.adminMenuAccessDto != null
                                                        && before.mmsMenuAccessDto != null && before.adminMenuAccessDto">
                            <!-- <div class="form-group row"> -->
                                <div class="form-group row">
                                    <span class="col-md-12">
                                        <input type="checkbox" class="input_checkbox" disabled/> <b v-text="resources['dashboard']"></b>
                                    </span>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                        <!-- After: {{after.mmsMenuAccessDto.quickStats}} Before{{before.mmsMenuAccessDto.quickStats}} -->
                                        <div v-if="after.mmsMenuAccessDto.quickStats === before.mmsMenuAccessDto.quickStats">
                                            <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.quickStats" disabled/>
                                            <span v-text="resources['quick_fact']"></span>
                                        </div>
                                        <label class="container" v-if="after.mmsMenuAccessDto.quickStats && !before.mmsMenuAccessDto.quickStats">
                                            <span v-text="resources['quick_fact']"></span>
                                            <input type="checkbox" checked="checked" disabled>
                                            <span class="checkmark-custom"></span>
                                        </label>
                                        <label class="container" v-if="!after.mmsMenuAccessDto.quickStats && before.mmsMenuAccessDto.quickStats">
                                            <span v-text="resources['quick_fact']"></span>
                                            <input type="checkbox" checked="checked" disabled>
                                            <span class="checkmark-custom-2"></span>
                                        </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                        <div v-if="after.mmsMenuAccessDto.distribute === before.mmsMenuAccessDto.distribute">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.distribute" disabled/>
                                                <span v-text="resources['distribution_of_tooling']"></span>
                                            </div>
                                            <label class="container" v-if="after.mmsMenuAccessDto.distribute && !before.mmsMenuAccessDto.distribute">
                                                <span v-text="resources['distribution_of_tooling']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-if="!after.mmsMenuAccessDto.distribute && before.mmsMenuAccessDto.distribute">
                                                <span v-text="resources['distribution_of_tooling']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>

                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                        <div v-if="after.mmsMenuAccessDto.projectHierarchy === before.mmsMenuAccessDto.projectHierarchy">
                                            <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.projectHierarchy" disabled/>
                                            <span v-text="resources['project_hierarchy']"></span>
                                        </div>
                                        <label class="container" v-if="after.mmsMenuAccessDto.projectHierarchy && !before.mmsMenuAccessDto.projectHierarchy">
                                            <span v-text="resources['project_hierarchy']"></span>
                                            <input type="checkbox" checked="checked" disabled>
                                            <span class="checkmark-custom"></span>
                                        </label>
                                        <label class="container" v-if="!after.mmsMenuAccessDto.projectHierarchy && before.mmsMenuAccessDto.projectHierarchy">
                                            <span v-text="resources['project_hierarchy']"></span>
                                            <input type="checkbox" checked="checked" disabled>
                                            <span class="checkmark-custom-2"></span>
                                        </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.capacityUtilization === before.mmsMenuAccessDto.capacityUtilization">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.capacityUtilization" disabled/>
                                                <span v-text="resources['capacity_utilization']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.capacityUtilization && !before.mmsMenuAccessDto.capacityUtilization">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.capacityUtilization && before.mmsMenuAccessDto.capacityUtilization">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.cycleTimeStatus === before.mmsMenuAccessDto.cycleTimeStatus">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.cycleTimeStatus" disabled/>
                                                <span v-text="resources['cycle_time_status']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.cycleTimeStatus && !before.mmsMenuAccessDto.cycleTimeStatus">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.cycleTimeStatus && before.mmsMenuAccessDto.cycleTimeStatus">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.toolingStatus === before.mmsMenuAccessDto.toolingStatus">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.toolingStatus" disabled/>
                                                <span v-text="resources['tooling_status']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.toolingStatus && !before.mmsMenuAccessDto.toolingStatus">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.toolingStatus && before.mmsMenuAccessDto.toolingStatus">
                                                <span v-text="resources['capacity_utilization']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.preventiveMaintenance === before.mmsMenuAccessDto.preventiveMaintenance">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.preventiveMaintenance" disabled/>
                                                <span v-text="resources['preventive_maintenance']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.preventiveMaintenance && !before.mmsMenuAccessDto.preventiveMaintenance">
                                                <span v-text="resources['preventive_maintenance']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.preventiveMaintenance && before.mmsMenuAccessDto.preventiveMaintenance">
                                                <span v-text="resources['preventive_maintenance']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.uptimeStatus === before.mmsMenuAccessDto.uptimeStatus">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.uptimeStatus" disabled/>
                                                <span v-text="resources['uptime_status']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.uptimeStatus && !before.mmsMenuAccessDto.uptimeStatus">
                                                <span v-text="resources['uptime_status']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.uptimeStatus && before.mmsMenuAccessDto.uptimeStatus">
                                                <span v-text="resources['uptime_status']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.downtime === before.mmsMenuAccessDto.downtime">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.downtime" disabled/>
                                                <span v-text="resources['downtime']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.downtime && !before.mmsMenuAccessDto.downtime">
                                                <span v-text="resources['downtime']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.downtime && before.mmsMenuAccessDto.downtime">
                                                <span v-text="resources['downtime']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.overallEquipmentEffectiveness === before.mmsMenuAccessDto.overallEquipmentEffectiveness">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.overallEquipmentEffectiveness" disabled/>
                                                <span v-text="resources['overall_equipment_effectiveness']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.overallEquipmentEffectiveness && !before.mmsMenuAccessDto.overallEquipmentEffectiveness">
                                                <span v-text="resources['overall_equipment_effectiveness']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.overallEquipmentEffectiveness && before.mmsMenuAccessDto.overallEquipmentEffectiveness">
                                                <span v-text="resources['overall_equipment_effectiveness']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.productionRate === before.mmsMenuAccessDto.productionRate">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.productionRate" disabled/>
                                                <span v-text="resources['production_rate']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.productionRate && !before.mmsMenuAccessDto.productionRate">
                                                <span v-text="resources['production_rate']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.productionRate && before.mmsMenuAccessDto.productionRate">
                                                <span v-text="resources['production_rate']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <div class="col-md-11">
                                            <div v-if="after.mmsMenuAccessDto.utilizationRate === before.mmsMenuAccessDto.utilizationRate">
                                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.utilizationRate" disabled/>
                                                <span v-text="resources['utilization_rate']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.mmsMenuAccessDto.utilizationRate && !before.mmsMenuAccessDto.utilizationRate">
                                                <span v-text="resources['utilization_rate']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.mmsMenuAccessDto.utilizationRate && before.mmsMenuAccessDto.utilizationRate">
                                                <span v-text="resources['utilization_rate']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </div>
                                </div>

                          <!-- Overview -->
                          <div class="form-group row">
                                      <span class="col-md-12">
                                          <input type="checkbox" class="input_checkbox" disabled/> <b v-text="resources['overview']"></b>
                                      </span>
                          </div>
                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.parts === before.mmsMenuAccessDto.parts">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.parts" disabled/>
                                <span v-text="resources['parts']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.parts && !before.mmsMenuAccessDto.parts">
                                <span v-text="resources['parts']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.parts && before.mmsMenuAccessDto.parts">
                                <span v-text="resources['parts']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.tooling === before.mmsMenuAccessDto.tooling">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.tooling" disabled/>
                                <span  v-text="resources['tooling']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.tooling && !before.mmsMenuAccessDto.tooling">
                                <span  v-text="resources['tooling']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.tooling && before.mmsMenuAccessDto.tooling">
                                <span  v-text="resources['tooling']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <!-- Alerts -->
                          <div class="form-group row">
                                          <span class="col-md-12">
                                              <input type="checkbox" class="input_checkbox" disabled/> <b v-text="resources['alert']"></b>
                                          </span>
                          </div>
                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.relocation === before.mmsMenuAccessDto.relocation">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.relocation" disabled/>
                                <span v-text="resources['relocation']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.relocation && !before.mmsMenuAccessDto.relocation">
                                <span v-text="resources['relocation']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.relocation && before.mmsMenuAccessDto.relocation">
                                <span v-text="resources['relocation']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.disconnection === before.mmsMenuAccessDto.disconnection">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.disconnection" disabled/>
                                <span v-text="resources['disconnection']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.disconnection && !before.mmsMenuAccessDto.disconnection">
                                <span v-text="resources['disconnection']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.disconnection && before.mmsMenuAccessDto.disconnection">
                                <span v-text="resources['disconnection']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.cycleTime === before.mmsMenuAccessDto.cycleTime">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.cycleTime" disabled/>
                                <span v-text="resources['cycle_time']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.cycleTime && !before.mmsMenuAccessDto.cycleTime">
                                <span v-text="resources['cycle_time']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.cycleTime && before.mmsMenuAccessDto.cycleTime">
                                <span v-text="resources['cycle_time']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.maintenance === before.mmsMenuAccessDto.maintenance">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.maintenance" disabled/>
                                <span v-text="resources['maintenance']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.maintenance && !before.mmsMenuAccessDto.maintenance">
                                <span v-text="resources['maintenance']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.maintenance && before.mmsMenuAccessDto.maintenance">
                                <span v-text="resources['maintenance']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.uptime === before.mmsMenuAccessDto.uptime">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.uptime" disabled/>
                                <span v-text="resources['uptime']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.uptime && !before.mmsMenuAccessDto.uptime">
                                <span v-text="resources['uptime']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.uptime && before.mmsMenuAccessDto.uptime">
                                <span v-text="resources['uptime']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.reset === before.mmsMenuAccessDto.reset">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.reset" disabled/>
                                <span v-text="resources['reset']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.reset && !before.mmsMenuAccessDto.reset">
                                <span v-text="resources['reset']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.reset && before.mmsMenuAccessDto.reset">
                                <span v-text="resources['reset']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div v-if="serverName == 'dyson'" class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.dataSubmission === before.mmsMenuAccessDto.dataSubmission">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.dataSubmission" disabled/>
                                <span v-text="resources['data_submission']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.dataSubmission && !before.mmsMenuAccessDto.dataSubmission">
                                <span v-text="resources['data_submission']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.dataSubmission && before.mmsMenuAccessDto.dataSubmission">
                                <span v-text="resources['data_submission']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <!-- Reports -->
                          <div class="form-group row">
                                          <span class="col-md-12">
                                              <input type="checkbox" class="input_checkbox" disabled/>
                                              <b v-text="resources['report']"></b>
                                          </span>
                          </div>
                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.toolingBenchmarking === before.mmsMenuAccessDto.toolingBenchmarking">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.toolingBenchmarking" disabled/>
                                <span v-text="resources['tooling_benchmarking']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.toolingBenchmarking && !before.mmsMenuAccessDto.toolingBenchmarking">
                                <span v-text="resources['reset']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.toolingBenchmarking && before.mmsMenuAccessDto.toolingBenchmarking">
                                <span v-text="resources['reset']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <!-- Search -->
                          <div class="form-group row">
                                          <span class="col-md-12">
                                              <input type="checkbox" class="input_checkbox" disabled/> <b  v-text="resources['search']"></b>
                                          </span>
                          </div>
                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.mmsMenuAccessDto.advancedSearch === before.mmsMenuAccessDto.advancedSearch">
                                <input type="checkbox" class="input_checkbox" :checked="after.mmsMenuAccessDto.advancedSearch" disabled/>
                                <span v-text="resources['advance_search']"></span>
                              </div>
                              <label class="container" v-else-if="after.mmsMenuAccessDto.advancedSearch && !before.mmsMenuAccessDto.advancedSearch">
                                <span v-text="resources['advance_search']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.mmsMenuAccessDto.advancedSearch && before.mmsMenuAccessDto.advancedSearch">
                                <span v-text="resources['advance_search']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>
                      </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <strong>
                                <span>
                                    <span v-text="resources['admin_menu_access']">
                                </span>
                                <span style="color: lightgrey">
                                    <input type="checkbox" class="input_checkbox" disabled/>
                                    <span v-text="resources['check_all']"></span>
                                </span>
                            </strong>
                        </div>
                        <div class="card-body">
                            <div class="form-group row">
                                    <span class="col-md-12">
                                        <input type="checkbox" class="input_checkbox" disabled/>
                                        <b  v-text="resources['base_data']"></b>
                                    </span>
                            </div>
                            <div class="form-group row">
                                    <span class="col-md-1"></span>
                                    <span class="col-md-11">
                                            <div v-if="after.adminMenuAccessDto.companies === before.adminMenuAccessDto.companies">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.companies" disabled/>
                                                <span v-text="resources['companies']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.companies && !before.adminMenuAccessDto.companies">
                                                <span v-text="resources['companies']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.companies && before.adminMenuAccessDto.companies">
                                                <span v-text="resources['companies']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                    </span>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                            <div v-if="after.adminMenuAccessDto.locations === before.adminMenuAccessDto.locations">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.locations" disabled/>
                                                <span v-text="resources['locations']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.locations && !before.adminMenuAccessDto.locations">
                                                <span v-text="resources['locations']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.locations && before.adminMenuAccessDto.locations">
                                                <span v-text="resources['locations']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                            <div v-if="after.adminMenuAccessDto.category === before.adminMenuAccessDto.category">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.category" disabled/>
                                                <span v-text="resources['category']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.category && !before.adminMenuAccessDto.category">
                                                <span v-text="resources['category']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.category && before.adminMenuAccessDto.category">
                                                <span v-text="resources['category']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                    <div v-if="after.adminMenuAccessDto.parts === before.adminMenuAccessDto.parts">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.parts" disabled/>
                                                <span v-text="resources['parts']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.parts && !before.adminMenuAccessDto.parts">
                                                <span v-text="resources['parts']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.parts && before.adminMenuAccessDto.parts">
                                                <span v-text="resources['parts']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-12"></span>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-12">
                                    <input type="checkbox" class="input_checkbox" disabled/>
                                    <b v-text="resources['equipment']"></b>
                                </span>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                    <div v-if="after.adminMenuAccessDto.terminals === before.adminMenuAccessDto.terminals">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.terminals" disabled/>
                                                <span v-text="resources['terminals']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.terminals && !before.adminMenuAccessDto.terminals">
                                                <span v-text="resources['terminals']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.terminals && before.adminMenuAccessDto.terminals">
                                                <span v-text="resources['terminals']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                    <div v-if="after.adminMenuAccessDto.tooling === before.adminMenuAccessDto.tooling">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.tooling" disabled/>
                                                <span  v-text="resources['tooling']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.tooling && !before.adminMenuAccessDto.tooling">
                                                <span  v-text="resources['tooling']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.tooling && before.adminMenuAccessDto.tooling">
                                                <span  v-text="resources['tooling']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                    <div v-if="after.adminMenuAccessDto.counters === before.adminMenuAccessDto.counters">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.counters" disabled/>
                                                <span v-text="resources['counters']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.counters && !before.adminMenuAccessDto.counters">
                                                <span v-text="resources['counters']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.counters && before.adminMenuAccessDto.counters">
                                                <span v-text="resources['counters']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-1"></span>
                                <div class="col-md-11">
                                    <div v-if="after.adminMenuAccessDto.reset === before.adminMenuAccessDto.reset">
                                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.reset" disabled/>
                                                <span v-text="resources['reset']"></span>
                                            </div>
                                            <label class="container" v-else-if="after.adminMenuAccessDto.reset && !before.adminMenuAccessDto.reset">
                                                <span v-text="resources['reset']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom"></span>
                                            </label>
                                            <label class="container" v-else-if="!after.adminMenuAccessDto.reset && before.adminMenuAccessDto.reset">
                                                <span v-text="resources['reset']"></span>
                                                <input type="checkbox" checked="checked" disabled>
                                                <span class="checkmark-custom-2"></span>
                                            </label>
                                </div>
                            </div>
                            <div class="form-group row">
                                <span class="col-md-12"></span>
                            </div>
                            <!--<div class="form-group row">-->
                                <!--<span class="col-md-12">-->
                                    <!--<input type="checkbox" class="input_checkbox" disabled/>-->
                                    <!--<b>Maintenance</b>-->
                                <!--</span>-->
                            <!--</div>-->
                            <!--<div class="form-group row">-->
                                <!--<span class="col-md-1"></span>-->
                                <!--<div class="col-md-11">-->
                                            <!--<div v-if="after.adminMenuAccessDto.corrective === before.adminMenuAccessDto.corrective">-->
                                                <!--<input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.corrective" disabled/>Corrective-->
                                            <!--</div>-->
                                            <!--<label class="container" v-else-if="after.adminMenuAccessDto.corrective && !before.adminMenuAccessDto.corrective">-->
                                                <!--<span>Corrective</span>-->
                                                <!--<input type="checkbox" checked="checked" disabled>-->
                                                <!--<span class="checkmark-custom"></span>-->
                                            <!--</label>-->
                                            <!--<label class="container" v-else-if="!after.adminMenuAccessDto.corrective && before.adminMenuAccessDto.corrective">-->
                                                <!--<span>Corrective</span>-->
                                                <!--<input type="checkbox" checked="checked" disabled>-->
                                                <!--<span class="checkmark-custom-2"></span>-->
                                            <!--</label>-->
                                <!--</div>-->
                            <!--</div>-->

                          <!-- Alerts -->
                          <div class="form-group row">
                                          <span class="col-md-12">
                                              <input type="checkbox" class="input_checkbox" disabled/> <b v-text="resources['alerts']"></b>
                                          </span>
                          </div>
                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.adminMenuAccessDto.terminalAlert === before.adminMenuAccessDto.terminalAlert">
                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.terminalAlert" disabled/>
                                <span v-text="resources['terminal']"></span>
                              </div>
                              <label class="container" v-else-if="after.adminMenuAccessDto.terminalAlert && !before.adminMenuAccessDto.terminalAlert">
                                <span v-text="resources['terminal']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.adminMenuAccessDto.terminalAlert && before.adminMenuAccessDto.terminalAlert">
                                <span v-text="resources['terminal']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.adminMenuAccessDto.counterAlert === before.adminMenuAccessDto.counterAlert">
                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.counterAlert" disabled/>
                                <span  v-text="resources['counter']"></span>
                              </div>
                              <label class="container" v-else-if="after.adminMenuAccessDto.counterAlert && !before.adminMenuAccessDto.counterAlert">
                                <span  v-text="resources['counter']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.adminMenuAccessDto.counterAlert && before.adminMenuAccessDto.counterAlert">
                                <span  v-text="resources['counter']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>

                          <div class="form-group row">
                            <span class="col-md-1"></span>
                            <div class="col-md-11">
                              <div v-if="after.adminMenuAccessDto.toolingAlert === before.adminMenuAccessDto.toolingAlert">
                                <input type="checkbox" class="input_checkbox" :checked="after.adminMenuAccessDto.toolingAlert" disabled/>
                                <span  v-text="resources['tooling']"></span>
                              </div>
                              <label class="container" v-else-if="after.adminMenuAccessDto.toolingAlert && !before.adminMenuAccessDto.toolingAlert">
                                <span  v-text="resources['tooling']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="!after.adminMenuAccessDto.toolingAlert && before.adminMenuAccessDto.toolingAlert">
                                <span  v-text="resources['tooling']"></span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                            </div>
                          </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class='modal-footer card-footer-close-btn'>
            <button type="button" @click.prevent='close()' class="btn btn-secondary"  v-text="resources['close']"></button>
        </div>
    </div>
      </div>
    </div>
</template>

<script>

    module.exports = {
        props: {
                    resources: Object,
                },
        data(){
            return {
                name: "",
                after: Object,
                before: Object,
                loading: false,
                roleType: '',
                serverName: ''
            }
        },
        methods: {
            showView: function(revision, roleType){
                console.log('ROle type: '+roleType)
                let sef = this;
                sef.loading = true;
                sef.roleType = roleType;
                axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                    sef.after = result.data.after;
                    sef.before = result.data.before;
                    sef.loading = false;
                });

                $("#view-role-history-change").modal('show');
            },
            close: function(){
                $("#view-role-history-change").modal('hide');
                this.$emit('open-modal-custom');

            }
        }
    };

</script>
