<template>
  <div
    id="view-mold-history-change"
    class="modal fade"
    style="overflow-y: unset"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
    :no-close-on-backdrop="true"
  >
    <div
      class="modal-dialog modal-dialog-centered modal-lg"
      style="margin-bottom: 0; margin-top: 0"
      role="document"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['tooling']"></h5>
          <button type="button" class="close" @click.prevent="close()">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div
          class="modal-body"
          style="overflow-y: auto; height: 700px"
          v-if="!loading"
        >
          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['basic_info_static']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tooling_id']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolingId != moldBefore.toolingId,
                          }"
                        >
                          {{ moldBefore.toolingId }}
                        </span>
                        <span
                          v-if="moldAfter.toolingId !== moldBefore.toolingId"
                          class="version-change-new"
                          >{{ moldAfter.toolingId }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tooling_letter']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolingLetter !=
                              moldBefore.toolingLetter,
                          }"
                        >
                          {{ moldBefore.toolingLetter }}
                        </span>
                        <span
                          v-if="
                            moldAfter.toolingLetter !== moldBefore.toolingLetter
                          "
                          class="version-change-new"
                          >{{ moldAfter.toolingLetter }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tooling_type']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolingType != moldBefore.toolingType,
                          }"
                        >
                          {{ moldBefore.toolingType }}
                        </span>
                        <span
                          v-if="
                            moldAfter.toolingType !== moldBefore.toolingType
                          "
                          class="version-change-new"
                          >{{ moldAfter.toolingType }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tooling_complexity']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolingComplexity !=
                              moldBefore.toolingComplexity,
                          }"
                        >
                          {{ moldBefore.toolingComplexity }}
                        </span>
                        <span
                          v-if="
                            moldAfter.toolingComplexity !==
                            moldBefore.toolingComplexity
                          "
                          class="version-change-new"
                          >{{ moldAfter.toolingComplexity }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['family_tool']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <!-- <span v-bind:class="{'version-change-old': moldAfter.parentName != moldBefore.parentName}">
                                                    {{moldAfter.parentName}}
                                                </span>
                                                <span v-if="moldAfter.parentName !== moldBefore.parentName" class="version-change-new">{{moldBefore.parentName}}</span> -->
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['forecasted_max_shot']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.designedShot != moldBefore.designedShot,
                          }"
                        >
                          {{ moldBefore.designedShot }}
                        </span>
                        <span
                          v-if="
                            moldAfter.designedShot !== moldBefore.designedShot
                          "
                          class="version-change-new"
                          >{{ moldAfter.designedShot }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['year_of_tool_made']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.madeYear != moldBefore.madeYear,
                          }"
                        >
                          {{ moldBefore.madeYear }}
                        </span>
                        <span
                          v-if="moldAfter.madeYear !== moldBefore.madeYear"
                          class="version-change-new"
                          >{{ moldAfter.madeYear }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['approved_cycle_time'] + '(s)'"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.contractedCycleTime !=
                              moldBefore.contractedCycleTime,
                          }"
                        >
                          {{ moldBefore.contractedCycleTime }}
                        </span>
                        <span
                          v-if="
                            moldAfter.contractedCycleTime !==
                            moldBefore.contractedCycleTime
                          "
                          class="version-change-new"
                          >{{ moldAfter.contractedCycleTime }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['location']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.locationName != moldBefore.locationName,
                          }"
                        >
                          {{ moldBefore.locationName }}
                        </span>
                        <span
                          v-if="
                            moldAfter.locationName !== moldBefore.locationName
                          "
                          class="version-change-new"
                          >{{ moldAfter.locationName }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tool_description']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolDescription !=
                              moldBefore.toolDescription,
                          }"
                        >
                          {{ moldBefore.toolDescription }}
                        </span>
                        <span
                          v-if="
                            moldAfter.toolDescription !==
                            moldBefore.toolDescription
                          "
                          class="version-change-new"
                          >{{ moldAfter.toolDescription }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['physical_info']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tool_size']"
                    ></span>
                    <div class="col-md-6">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.size != moldBefore.size,
                          }"
                        >
                          {{ moldBefore.size }}
                        </span>
                        <span
                          v-if="moldAfter.size !== moldBefore.size"
                          class="version-change-new"
                          >{{ moldAfter.size }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                    <div class="col-md-2">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.sizeUnitStr != moldBefore.sizeUnitStr,
                          }"
                        >
                          {{ moldBefore.sizeUnitStr }}
                        </span>
                        <span
                          v-if="
                            moldAfter.sizeUnitStr !== moldBefore.sizeUnitStr
                          "
                          class="version-change-new"
                          >{{ moldAfter.sizeUnitStr }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['tool_weight']"
                    ></span>
                    <div class="col-md-6">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.weight != moldBefore.weight,
                          }"
                        >
                          {{ moldBefore.weight }}
                        </span>
                        <span
                          v-if="moldAfter.weight !== moldBefore.weight"
                          class="version-change-new"
                          >{{ moldAfter.weight }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                    <div class="col-md-2">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.weightUnitStr !=
                              moldBefore.weightUnitStr,
                          }"
                        >
                          {{ moldBefore.weightUnitStr }}
                        </span>
                        <span
                          v-if="
                            moldAfter.weightUnitStr !== moldBefore.weightUnitStr
                          "
                          class="version-change-new"
                          >{{ moldAfter.weightUnitStr }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['shot_weight'] + '(g)'"
                    >
                    </span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.shotSize != moldBefore.shotSize,
                          }"
                        >
                          {{ moldBefore.shotSize }}
                        </span>
                        <span
                          v-if="moldAfter.shotSize !== moldBefore.shotSize"
                          class="version-change-new"
                          >{{ moldAfter.shotSize }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['toolmaker']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.toolMakerName !=
                              moldBefore.toolMakerName,
                          }"
                        >
                          {{ moldBefore.toolMakerName }}
                        </span>
                        <span
                          v-if="
                            moldAfter.toolMakerName !== moldBefore.toolMakerName
                          "
                          class="version-change-new"
                          >{{ moldAfter.toolMakerName }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['injection_molding_machine_id']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.injectionMachineId !=
                              moldBefore.injectionMachineId,
                          }"
                        >
                          {{ moldBefore.injectionMachineId }}
                        </span>
                        <span
                          v-if="
                            moldAfter.injectionMachineId !==
                            moldBefore.injectionMachineId
                          "
                          class="version-change-new"
                          >{{ moldAfter.injectionMachineId }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['total_number_of_cavities']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.totalCavities !=
                              moldBefore.totalCavities,
                          }"
                        >
                          {{ moldBefore.totalCavities }}
                        </span>
                        <span
                          v-if="
                            moldAfter.totalCavities !== moldBefore.totalCavities
                          "
                          class="version-change-new"
                          >{{ moldAfter.totalCavities }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['machine_tonnage']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.quotedMachineTonnage !=
                              moldBefore.quotedMachineTonnage,
                          }"
                        >
                          {{ moldBefore.quotedMachineTonnage }}
                        </span>
                        <span
                          v-if="
                            moldAfter.quotedMachineTonnage !==
                            moldBefore.quotedMachineTonnage
                          "
                          class="version-change-new"
                          >{{ moldAfter.quotedMachineTonnage }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['machine_tone_current_pro']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.currentMachineTonnage !=
                              moldBefore.currentMachineTonnage,
                          }"
                        >
                          {{ moldBefore.currentMachineTonnage }}
                        </span>
                        <span
                          v-if="
                            moldAfter.currentMachineTonnage !==
                            moldBefore.currentMachineTonnage
                          "
                          class="version-change-new"
                          >{{ moldAfter.currentMachineTonnage }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['runner_system_info']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['type_of_runner_system']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.runnerType != moldBefore.runnerType,
                          }"
                        >
                          {{ moldBefore.runnerType }}
                        </span>
                        <span
                          v-if="moldAfter.runnerType !== moldBefore.runnerType"
                          class="version-change-new"
                          >{{ moldAfter.runnerType }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['maker_of_runner_system']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.runnerMaker != moldBefore.runnerMaker,
                          }"
                        >
                          {{ moldBefore.runnerMaker }}
                        </span>
                        <span
                          v-if="
                            moldAfter.runnerMaker !== moldBefore.runnerMaker
                          "
                          class="version-change-new"
                          >{{ moldAfter.runnerMaker }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['weight_of_runner_system_g']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.weightRunner != moldBefore.weightRunner,
                          }"
                        >
                          {{ moldBefore.weightRunner }}
                        </span>
                        <span
                          v-if="
                            moldAfter.weightRunner !== moldBefore.weightRunner
                          "
                          class="version-change-new"
                          >{{ moldAfter.weightRunner }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['hot_runner_number_of_drop']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.hotRunnerDrop !=
                              moldBefore.hotRunnerDrop,
                          }"
                        >
                          {{ moldBefore.hotRunnerDrop }}
                        </span>
                        <span
                          v-if="
                            moldAfter.hotRunnerDrop !== moldBefore.hotRunnerDrop
                          "
                          class="version-change-new"
                          >{{ moldAfter.hotRunnerDrop }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['hot_runner_zone']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.hotRunnerZone !=
                              moldBefore.hotRunnerZone,
                          }"
                        >
                          {{ moldBefore.hotRunnerZone }}
                        </span>
                        <span
                          v-if="
                            moldAfter.hotRunnerZone !== moldBefore.hotRunnerZone
                          "
                          class="version-change-new"
                          >{{ moldAfter.hotRunnerZone }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['maintenance_info']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['condition_of_tooling']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.conditionOfTooling !=
                              moldBefore.conditionOfTooling,
                          }"
                        >
                          {{ moldBefore.conditionOfTooling }}
                        </span>
                        <span
                          v-if="
                            moldAfter.conditionOfTooling !==
                            moldBefore.conditionOfTooling
                          "
                          class="version-change-new"
                          >{{ moldAfter.conditionOfTooling }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['maintenance_interval']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.preventCycle != moldBefore.preventCycle,
                          }"
                        >
                          {{ moldBefore.preventCycle }}
                        </span>
                        <span
                          v-if="
                            moldAfter.preventCycle !== moldBefore.preventCycle
                          "
                          class="version-change-new"
                          >{{ moldAfter.preventCycle }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['upcoming_maintenance_tolerance']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.preventUpcoming !=
                              moldBefore.preventUpcoming,
                          }"
                        >
                          {{ moldBefore.preventUpcoming }}
                        </span>
                        <span
                          v-if="
                            moldAfter.preventUpcoming !==
                            moldBefore.preventUpcoming
                          "
                          class="version-change-new"
                          >{{ moldAfter.preventUpcoming }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <!-- <div class='form-group row'>
                                        <span class='col-md-4' v-text="resources['overdue_maintenance_tolerance']"></span>
                                        <div class='col-md-8'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': moldAfter.preventOverdue != moldBefore.preventOverdue}">
                                                    {{moldBefore.preventOverdue}}
                                                </span>
                                                <span v-if="moldAfter.preventOverdue !== moldBefore.preventOverdue" class="version-change-new">{{moldAfter.preventOverdue}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                        </div>
                                    </div> -->
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['cycle_time_tolerance_l1']"
                    ></span>
                    <div class="col-md-6">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.cycleTimeLimit1 !=
                              moldBefore.cycleTimeLimit1,
                          }"
                        >
                          {{ moldBefore.cycleTimeLimit1 }}
                        </span>
                        <span
                          v-if="
                            moldAfter.cycleTimeLimit1 !==
                            moldBefore.cycleTimeLimit1
                          "
                          class="version-change-new"
                          >{{ moldAfter.cycleTimeLimit1 }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                    <div class="col-md-2">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.cycleTimeLimit1UnitStr !=
                              moldBefore.cycleTimeLimit1UnitStr,
                          }"
                        >
                          {{ moldBefore.cycleTimeLimit1UnitStr }}
                        </span>
                        <span
                          v-if="
                            moldAfter.cycleTimeLimit1UnitStr !==
                            moldBefore.cycleTimeLimit1UnitStr
                          "
                          class="version-change-new"
                          >{{ moldAfter.cycleTimeLimit1UnitStr }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['cycle_time_tolerance_l2']"
                    ></span>
                    <div class="col-md-6">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.cycleTimeLimit2 !=
                              moldBefore.cycleTimeLimit2,
                          }"
                        >
                          {{ moldBefore.cycleTimeLimit2 }}
                        </span>
                        <span
                          v-if="
                            moldAfter.cycleTimeLimit2 !==
                            moldBefore.cycleTimeLimit2
                          "
                          class="version-change-new"
                          >{{ moldAfter.cycleTimeLimit2 }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                    <div class="col-md-2">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.cycleTimeLimit2UnitStr !=
                              moldBefore.cycleTimeLimit2UnitStr,
                          }"
                        >
                          {{ moldBefore.cycleTimeLimit2UnitStr }}
                        </span>
                        <span
                          v-if="
                            moldAfter.cycleTimeLimit2UnitStr !==
                            moldBefore.cycleTimeLimit2UnitStr
                          "
                          class="version-change-new"
                          >{{ moldAfter.cycleTimeLimit2UnitStr }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['engineer_in_charge']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <!-- <span v-bind:class="{'version-change-old': moldAfter.engineers != moldBefore.engineers}">
                                                    {{moldAfter.engineers}}
                                                </span>
                                                <span v-if="moldAfter.engineers !== moldBefore.engineers" class="version-change-new">{{moldBefore.engineers}}</span>
                                                <span style="visibility: hidden">.</span> -->
                        <div class="col-md-12">
                          <div class="form-group row">
                            <div class="col-md-6">
                              <div
                                v-for="(
                                  nameBefore, index
                                ) in moldBefore.engineerNameArr"
                                v-bind:class="{
                                  'version-change-old': !compareEngieer,
                                }"
                                :key="index"
                              >
                                {{ nameBefore }}
                              </div>
                              <span style="visibility: hidden">.</span>
                            </div>
                            <div class="col-md-6" v-if="!compareEngieer">
                              <div
                                v-for="(
                                  nameAfter, index
                                ) in moldAfter.engineerNameArr"
                                class="version-change-new"
                                :key="index"
                              >
                                {{ nameAfter }}
                              </div>
                              <span style="visibility: hidden">.</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['maintenance_document']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.maintenanceDocuments !=
                              moldBefore.maintenanceDocuments,
                          }"
                        >
                          {{ moldBefore.maintenanceDocuments }}
                        </span>
                        <span
                          v-if="
                            moldAfter.maintenanceDocuments !==
                            moldBefore.maintenanceDocuments
                          "
                          class="version-change-new"
                          >{{ moldAfter.maintenanceDocuments }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['instruction_video']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.instructionVideo !=
                              moldBefore.instructionVideo,
                          }"
                        >
                          {{ moldBefore.instructionVideo }}
                        </span>
                        <span
                          v-if="
                            moldAfter.instructionVideo !==
                            moldBefore.instructionVideo
                          "
                          class="version-change-new"
                          >{{ moldAfter.instructionVideo }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- COST INFORMATION -->
          <!-- <div class='row' v-if='isDyson'>
                        <div class='col-md-12'>
                            <div class='card'>
                                <div class='card-header'>
                                    <strong  v-text="resources['cost_info']"></strong>
                                </div>
                                <div class='card-body'>
                                    <div class='form-group row'>
                                        <div class='col-md-4' v-text="resources['cost_tooling_usd']"></div>
                                        <div class='col-md-8'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': moldAfter.cost != moldBefore.cost}">
                                                    {{moldAfter.cost}}
                                                </span>
                                                <span v-if="moldAfter.cost !== moldBefore.cost" class="version-change-new">{{moldBefore.cost}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <div class='col-md-4'  v-text="resources['memo']"></div>
                                        <div class='col-md-8'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': moldAfter.memo != moldBefore.memo}">
                                                    {{moldAfter.memo}}
                                                </span>
                                                <span v-if="moldAfter.memo !== moldBefore.memo" class="version-change-new">{{moldBefore.memo}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div> -->
          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['supplier_info']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['supplier']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.supplierName != moldBefore.supplierName,
                          }"
                        >
                          {{ moldBefore.supplierName }}
                        </span>
                        <span
                          v-if="
                            moldAfter.supplierName !== moldBefore.supplierName
                          "
                          class="version-change-new"
                          >{{ moldAfter.supplierName }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['uptime_target'] + '(%)'"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.uptimeTarget != moldBefore.uptimeTarget,
                          }"
                        >
                          {{ moldBefore.uptimeTarget }}
                        </span>
                        <span
                          v-if="
                            moldAfter.uptimeTarget !== moldBefore.uptimeTarget
                          "
                          class="version-change-new"
                          >{{ moldAfter.uptimeTarget }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['uptime_tolerance_l1'] + '(%)'"
                    >
                    </span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.uptimeLimitL1 !=
                              moldBefore.uptimeLimitL1,
                          }"
                        >
                          {{ moldBefore.uptimeLimitL1 }}
                        </span>
                        <span
                          v-if="
                            moldAfter.uptimeLimitL1 !== moldBefore.uptimeLimitL1
                          "
                          class="version-change-new"
                          >{{ moldAfter.uptimeLimitL1 }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['uptime_tolerance_l2'] + '(%)'"
                    >
                    </span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.uptimeLimitL2 !=
                              moldBefore.uptimeLimitL2,
                          }"
                        >
                          {{ moldBefore.uptimeLimitL2 }}
                        </span>
                        <span
                          v-if="
                            moldAfter.uptimeLimitL2 !== moldBefore.uptimeLimitL2
                          "
                          class="version-change-new"
                          >{{ moldAfter.uptimeLimitL2 }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['required_labor']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.labour != moldBefore.labour,
                          }"
                        >
                          {{ moldBefore.labour }}
                        </span>
                        <span
                          v-if="moldAfter.labour !== moldBefore.labour"
                          class="version-change-new"
                          >{{ moldAfter.labour }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['production_hour_per_day']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.shiftsPerDay != moldBefore.shiftsPerDay,
                          }"
                        >
                          {{ moldBefore.shiftsPerDay }}
                        </span>
                        <span
                          v-if="
                            moldAfter.shiftsPerDay !== moldBefore.shiftsPerDay
                          "
                          class="version-change-new"
                          >{{ moldAfter.shiftsPerDay }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['production_day_per_week']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.productionDays !=
                              moldBefore.productionDays,
                          }"
                        >
                          {{ moldBefore.productionDays }}
                        </span>
                        <span
                          v-if="
                            moldAfter.productionDays !==
                            moldBefore.productionDays
                          "
                          class="version-change-new"
                          >{{ moldAfter.productionDays }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                  <div class="form-group row">
                    <span
                      class="col-md-4"
                      v-text="resources['maximum_capacity_per_week']"
                    ></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <span
                          v-bind:class="{
                            'version-change-old':
                              moldAfter.maxCapacityPerWeek !=
                              moldBefore.maxCapacityPerWeek,
                          }"
                        >
                          {{ moldBefore.maxCapacityPerWeek }}
                        </span>
                        <span
                          v-if="
                            moldAfter.maxCapacityPerWeek !==
                            moldBefore.maxCapacityPerWeek
                          "
                          class="version-change-new"
                          >{{ moldAfter.maxCapacityPerWeek }}</span
                        >
                        <span style="visibility: hidden">.</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['part']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span class="col-md-4" v-text="resources['part_id']"></span>
                    <div class="col-md-8">
                      <div class="form-control">
                        <div class="col-md-12">
                          <div class="form-group row">
                            <div class="col-md-6">
                              <div
                                v-for="(
                                  partBefore, index
                                ) in moldBefore.moldPartNames"
                                :key="index"
                                v-bind:class="{
                                  'version-change-old': !comparePart,
                                }"
                              >
                                {{ partBefore }}
                              </div>
                            </div>
                            <div class="col-md-6" v-if="!comparePart">
                              <div
                                v-for="(
                                  partAfter, index
                                ) in moldAfter.moldPartNames"
                                :key="index"
                                class="version-change-new"
                              >
                                {{ partAfter }}
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['access']"></strong>
                </div>
                <div class="card-body">
                  <div class="form-group row">
                    <span class="col-md-4">
                      <span
                        class="col-md-4"
                        v-text="resources['access_group']"
                      ></span>
                    </span>
                    <div class="col-md-8">
                      <div v-for="(role, authority) in roles" :key="authority">
                        <!-- <div class="form-check checkbox pl-0">
                                                    <label>
                                                        <input type="checkbox" name="authorities" :value="role.authority"
                                                        v-model="moldAfter.authorities"> {{role.name}} ({{role.description}})  ({{role.compare}})
                                                    </label>
                                                </div> -->
                        <div v-if="role.compare === 2">
                          <input
                            type="checkbox"
                            class="input_checkbox"
                            checked
                            disabled
                          />{{ role.name }} ({{ role.description }})
                        </div>
                        <label
                          class="container"
                          v-else-if="role.compare === -1"
                        >
                          <span>{{ role.name }} ({{ role.description }})</span>
                          <input type="checkbox" checked="checked" disabled />
                          <span class="checkmark-custom"></span>
                        </label>
                        <label class="container" v-else-if="role.compare === 1">
                          <span>{{ role.name }} ({{ role.description }})</span>
                          <input type="checkbox" checked="checked" disabled />
                          <span class="checkmark-custom-2"></span>
                        </label>
                        <div v-if="role.compare === -2">
                          <input
                            type="checkbox"
                            class="input_checkbox"
                            disabled
                          />{{ role.name }} ({{ role.description }})
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer card-footer-close-btn">
          <button
            type="button"
            @click.prevent="close()"
            class="btn btn-secondary"
            v-text="resources['close']"
          ></button>
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
  data() {
    return {
      moldAfter: Object,
      moldBefore: Object,
      roles: Object,
      compareEngieer: false,
      comparePart: false,
      loading: true,
      isDyson: false,
    };
  },
  methods: {
    showView: function (revision) {
      this.loading = true;
      this.initData(revision);
      $("#view-mold-history-change").modal("show");
    },

    close: function () {
      this.loading = true;
      $("#view-mold-history-change").modal("hide");
      this.$emit("open-modal-custom");
    },
    initData: function (revision) {
      let sef = this;
      axios
        .get(
          "/api/version/revision-history/top2?id=" +
            revision.id +
            "&revisionObjectType=" +
            revision.revisionObjectType +
            "&originId=" +
            revision.originId
        )
        .then(function (result) {
          sef.moldAfter = result.data.after;
          sef.moldBefore = result.data.before;
          sef.isDyson = "dyson" === result.data.customServerName;
          sef.getRoles();
          if (sef.moldAfter != null && sef.moldBefore != null) {
            try {
              sef.compareEngieer = sef.compare(
                sef.moldAfter.engineerNameArr,
                sef.moldBefore.engineerNameArr
              );
              sef.comparePart = sef.compare(
                sef.moldAfter.moldPartNames,
                sef.moldBefore.moldPartNames
              );
            } catch (e) {
              console.log(e);
            }
          }
          sef.loading = false;
        });
    },
    getRoles: function () {
      // get list role and compare role after and role before
      let sef = this;
      axios.get("/api/roles/my").then(function (result) {
        result.data;
        if (result.data != null && result.data instanceof Array) {
          let roleServer = result.data;
          let roleAfter = sef.moldAfter.authorities;
          let roleBefore = sef.moldBefore.authorities;
          let comapareBA = new Array();
          for (let countSum = 0; countSum < roleServer.length; countSum++) {
            let compareAfterVsServer = false;
            let compareBeforeVsServer = false;

            for (
              let countAfter = 0;
              countAfter < roleAfter.length;
              countAfter++
            ) {
              if (roleAfter[countAfter] === roleServer[countSum].authority) {
                compareAfterVsServer = true;
                break;
              }
            }

            for (
              let countBefore = 0;
              countBefore < roleBefore.length;
              countBefore++
            ) {
              if (roleBefore[countBefore] === roleServer[countSum].authority) {
                compareBeforeVsServer = true;
                break;
              }
            }

            let compareAfterBefore = 0;
            if (!compareAfterVsServer && compareBeforeVsServer) {
              compareAfterBefore = 1;
            } else if (compareAfterVsServer && !compareBeforeVsServer) {
              compareAfterBefore = -1;
            } else if (compareAfterVsServer && compareBeforeVsServer) {
              compareAfterBefore = 2;
            } else if (!compareAfterVsServer && !compareBeforeVsServer) {
              compareAfterBefore = -2;
            }
            comapareBA.push({
              authority: roleServer[countSum].authority,
              name: roleServer[countSum].name,
              description: roleServer[countSum].description,
              compare: compareAfterBefore,
            });
          }
          sef.roles = comapareBA;
        }
      });
    },
    // compare two arrays
    compare: function (arrAfter, arrBefore) {
      if (arrAfter.length != arrBefore.length) {
        return false;
      }

      let check;
      for (let after = 0; after < arrAfter.length; after++) {
        check = false;
        for (let before = 0; before < arrBefore.length; before++) {
          check = arrBefore[before] === arrAfter[after];
          if (check == true) break;
        }
        if (check == false) {
          return false;
        }
      }
      return true;
    },
  },
};
</script>

<style scoped>
.modal-body {
  max-height: calc(100vh - 200px);
}
</style>
