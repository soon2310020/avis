<template>
  <div
    style="overflow: scroll"
    id="op-mold-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['tooling_id']"></span> :
              {{ mold.equipmentCode }}
            </h4>
            <button
              type="button"
              class="close"
              data-dismiss="modal"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <!--            BASIC-->
            <div class="card">
              <div
                v-on:click="expand('mold-detail-basic-info')"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <strong v-text="resources['basic_info_static']"></strong>
                <img
                  id="mold-detail-basic-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-up.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                style="display: table"
                id="mold-detail-basic-info"
                class="table table-mold-details"
              >
                <tbody>
                  <tr>
                    <th v-text="resources['tooling_id']"></th>
                    <td>{{ mold.equipmentCode }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['supplier_tooling_id']"></th>
                    <td>{{ mold.supplierMoldCode }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['counter_id']"></th>
                    <td>{{ mold.counterCode }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['toolingLetter']">
                    <th v-text="resources['tooling_letter']"></th>
                    <td>{{ mold.toolingLetter }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['toolingType']">
                    <th v-text="resources['tooling_type']"></th>
                    <td>{{ mold.toolingType }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['toolingComplexity']">
                    <th v-text="resources['tooling_complexity']"></th>
                    <td>{{ mold.toolingComplexity }}</td>
                  </tr>
                  <!--<tr>-->
                  <!--<th>Family Tool</th>-->
                  <!--<td>{{mold.familyTool ? 'Yes' : 'No'}}</td>-->
                  <!--</tr>-->
                  <tr v-if="!checkDeleteTooling['designedShot']">
                    <th v-text="resources['forecasted_max_shot']"></th>
                    <td>{{ formatNumber(mold.designedShot) }}</td>
                  </tr>
                  <tr
                    v-if="
                      isLifeYearsEnabled && !checkDeleteTooling['lifeYears']
                    "
                  >
                    <th v-text="resources['forecasted_tool_life']"></th>
                    <td>{{ mold.lifeYears }}</td>
                  </tr>

                  <tr v-if="!checkDeleteTooling['madeYear']">
                    <th v-text="resources['year_of_tool_made']"></th>
                    <td>{{ mold.madeYear }}</td>
                  </tr>
                  <!--                  <tr>-->
                  <!--                    <th  v-text="resources['approved_cycle_time']"></th>-->
                  <!--                    <td>{{mold.contractedCycleTimeSeconds}}{{mold.contractedCycleTime ? 's' : ''}}</td>-->
                  <!--                    <th v-text="resources['location']"></th>-->
                  <!--                    <td>-->
                  <!--                      {{mold.locationName}}-->
                  <!--                      <button-->
                  <!--                          v-on:click="showLocationHistory()"-->
                  <!--                          type="button"-->
                  <!--                          class="btn btn-primary btn-sm"-->
                  <!--                          v-text="resources['history']"></button>-->
                  <!--                    </td>-->
                  <!--                  </tr>-->
                  <!-- <tr>
                    <th  v-text="resources['tool_description']"></th>
                    <td>
                      <span v-if="mold.scrapRate">{{mold.scrapRate}}%</span>
                    </td>
                  </tr>-->
                  <tr v-if="!checkDeleteTooling['engineers']">
                    <th v-text="resources['engineer_in_charge']"></th>
                    <td>
                      <span v-for="(engineerName, index) in mold.engineerNames">
                        <span v-if="index != 0">, </span
                        ><span>{{ engineerName }}</span>
                      </span>
                      <!--{{mold.engineerNames ? mold.engineerNames.toString(): ''}}-->
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['toolDescription']">
                    <th v-text="resources['tool_description']"></th>
                    <td>
                      <textarea
                        readonly
                        style="
                          border: none;
                          resize: none;
                          outline: none;
                          width: 100%;
                        "
                        rows="4"
                        >{{ mold.toolDescription }}</textarea
                      >
                    </td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'BASIC'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                  <!--<tr>
                                        <th>Forecasted Tool Life</th>
                                        <td>{{mold.lifeYears}} years</td>
                  </tr>-->
                </tbody>
              </table>
            </div>
            <!--            PART-->
            <div class="card" v-for="(moldParts, index, id) in parts">
              <div
                v-on:click="expand(`mold-detail-part-${index}`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['part']"></span> #{{ index + 1 }}
                <img
                  v-bind:id="'mold-detail-part-' + index + '-image'"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-up.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                style="display: table"
                v-bind:id="'mold-detail-part-' + index"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeletePart['category']">
                    <th v-text="resources['category']"></th>
                    <td>{{ moldParts.categoryName }}</td>
                  </tr>
                  <tr v-if="!checkDeletePart['projectName']">
                    <th v-text="resources['project']"></th>
                    <td>{{ moldParts.projectName }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['part_id']"></th>
                    <td>{{ moldParts.partCode }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['part_name']"></th>
                    <td v-bind:data-tooltip-text="moldParts.partName">
                      {{ replaceLongtext(moldParts.partName, 40) }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeletePart['resinCode']">
                    <th v-text="resources['part_resin_code']"></th>
                    <td>{{ moldParts.resinCode }}</td>
                  </tr>
                  <tr v-if="!checkDeletePart['resinGrade']">
                    <th v-text="resources['part_resin_grade']"></th>
                    <td>{{ moldParts.resinGrade }}</td>
                  </tr>
                  <tr v-if="!checkDeletePart['designRevision']">
                    <th v-text="resources['design_revision_level']"></th>
                    <td>{{ moldParts.designRevision }}</td>
                  </tr>

                  <tr v-if="!checkDeletePart['size']">
                    <th v-text="resources['part_volume_w_d_h']"></th>
                    <td>{{ moldParts.partSize }}</td>
                    <!--<td v-if="moldParts.sizeUnit == 'MM' || moldParts.sizeUnit == 'CM' || moldParts.sizeUnit == 'M'">{{moldParts.partSize}}³</td>-->
                    <!--<td v-else>{{moldParts.partSize}}</td>-->
                  </tr>
                  <tr v-if="!checkDeletePart['weight']">
                    <th v-text="resources['part_weight']"></th>
                    <td>{{ moldParts.partWeight }}</td>
                  </tr>
                  <tr v-if="!checkDeletePart['weeklyDemand']">
                    <th v-text="resources['weekly_demand']"></th>
                    <td>{{ moldParts.weeklyDemand }}</td>
                  </tr>
                  <tr>
                    <!--                  <th  v-text="resources['no_of_cavity']"></th>-->
                    <th>
                      {{ resources["working_cavities"] }} /
                      {{ resources["total_number_of_cavities"] }}
                    </th>
                    <td>
                      {{ moldParts.cavity
                      }}{{
                        moldParts.cavity != null ||
                        moldParts.totalCavities != null
                          ? " / "
                          : ""
                      }}{{ moldParts.totalCavities }}
                    </td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in customFieldListPart[
                      moldParts.partId
                    ]"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!--            PHYSICAL-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-physical-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['physical_info']"></span>
                <img
                  id="mold-detail-physical-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-physical-info"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeleteTooling['size']">
                    <th v-text="resources['tool_size']"></th>
                    <td>{{ mold.toolingSizeView }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['weight']">
                    <th v-text="resources['tool_weight']"></th>
                    <td>{{ mold.toolingWeightView }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['shotSize']">
                    <th v-text="resources['shot_weight']"></th>
                    <td>
                      {{ mold.shotSize }}
                      {{ mold.shotSize || mold.shotSize === 0 ? "gram" : "" }}
                    </td>
                  </tr>

                  <tr v-if="!checkDeleteTooling['toolMakerCompanyName']">
                    <th v-text="resources['toolmaker']"></th>
                    <td>{{ mold.toolMakerCompanyName }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['injectionMachineId']">
                    <th v-text="resources['injection_molding_machine_id']"></th>
                    <td>{{ mold.injectionMachineId }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['totalCavities']">
                    <th v-text="resources['total_number_of_cavities']"></th>
                    <td>{{ mold.totalCavities }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['quotedMachineTonnage']">
                    <th v-text="resources['machine_ton']"></th>
                    <td>
                      {{ mold.quotedMachineTonnage }}
                      {{ mold.quotedMachineTonnage ? "ton" : "" }}
                    </td>
                  </tr>
                  <!--<tr>-->
                  <!--<th>Machine Tonnage - current production</th>-->
                  <!--<td>{{mold.currentMachineTonnage}} {{mold.currentMachineTonnage ? 'ton' : ''}}</td>-->
                  <!--</tr>-->
                  <tr v-if="!checkDeleteTooling['toolingPictureFile']">
                    <th v-text="resources['tooling_picture']"></th>
                    <td>
                      <a
                        v-for="(file, index) in conditionFiles"
                        :key="index"
                        href="javascript:void(0)"
                        class="btn btn-outline-dark btn-sm mr-1 mb-1"
                        @click="showFile(file)"
                        >{{ file.fileName }}</a
                      >
                    </td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'PHYSICAL'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            RUNNER-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-runner-system-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['runner_system_info']"></span>
                <img
                  id="mold-detail-runner-system-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-runner-system-info"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeleteTooling['runnerType']">
                    <th v-text="resources['type_of_runner_system']"></th>
                    <td>{{ mold.runnerTypeTitle }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['runnerMaker']">
                    <th v-text="resources['maker_of_runner_system']"></th>
                    <td>{{ mold.runnerMaker }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['weightRunner']">
                    <th v-text="resources['weight_of_runner_system']"></th>
                    <td>
                      {{ mold.weightRunner }}
                      {{ mold.weightRunner ? "gram" : "" }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['hotRunnerDrop']">
                    <th v-text="resources['hot_runner_number_of_drop']"></th>
                    <td>{{ mold.hotRunnerDrop }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['hotRunnerZone']">
                    <th v-text="resources['hot_runner_zone']"></th>
                    <td>{{ mold.hotRunnerZone }}</td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'RUNNER_SYSTEM'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            DYNAMIC-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-dynamic-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['dynamic_info']"></span>
                <img
                  id="mold-detail-dynamic-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-dynamic-info"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <!--                  <tr>-->
                  <!--                    <th v-text="resources['op']"></th>-->
                  <!--                    <td>-->
                  <!--                      <div-->
                  <!--                        v-if="mold.operatingStatus == 'WORKING' && (mold.equipmentStatus == 'INSTALLED' || mold.equipmentStatus == 'CHECK' )"-->
                  <!--                      >-->
                  <!--                        <span-->
                  <!--                          class="op-active label label-success"-->
                  <!--                          style="position: absolute; margin-top: 0px;"-->
                  <!--                        ></span>-->
                  <!--                        <span class="ml-4" v-text="resources['active']"></span>-->
                  <!--                      </div>-->
                  <!--                      <div-->
                  <!--                        v-if="mold.operatingStatus == 'IDLE' && (mold.equipmentStatus == 'INSTALLED' || mold.equipmentStatus == 'CHECK' )"-->
                  <!--                      >-->
                  <!--                        <span-->
                  <!--                          class="op-active label label-warning"-->
                  <!--                          style="position: absolute; margin-top: 0px;"-->
                  <!--                        ></span>-->
                  <!--                        <span class="ml-4" v-text="resources['idle']"></span>-->
                  <!--                      </div>-->
                  <!--                      <div-->
                  <!--                        v-if="mold.operatingStatus == 'NOT_WORKING' && (mold.equipmentStatus == 'INSTALLED' || mold.equipmentStatus == 'CHECK' )"-->
                  <!--                      >-->
                  <!--                        <span-->
                  <!--                          class="op-active label label-inactive"-->
                  <!--                          style="position: absolute; margin-top: 0px;"-->
                  <!--                        ></span>-->
                  <!--                        <span class="ml-4" v-text="resources['inactive']"></span>-->
                  <!--                      </div>-->
                  <!--                      <div-->
                  <!--                        v-if="mold.operatingStatus == 'DISCONNECTED' && (mold.equipmentStatus == 'INSTALLED' || mold.equipmentStatus == 'CHECK' )"-->
                  <!--                      >-->
                  <!--                        <span-->
                  <!--                          class="op-active label label-danger"-->
                  <!--                          style="position: absolute; margin-top: 0px;"-->
                  <!--                        ></span>-->
                  <!--                        <span class="ml-4" v-text="resources['disconnected']"></span>-->
                  <!--                      </div>-->
                  <!--                    </td>-->
                  <!--                  </tr>-->
                  <tr>
                    <th v-text="resources['accumulated_shots']"></th>
                    <td>{{ formatNumber(mold.lastShot) }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['last_date_of_shots']"></th>
                    <!--<td>{{mold.lastShotDateTime}}</td>-->
                    <td>
                      {{
                        formatToDateTime(
                          mold.dataFilterEnabled
                            ? mold.lastShotAtVal
                            : mold.lastShotAt
                        )
                      }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['utilisation_rate']"></th>
                    <td>
                      <div class="clearfix">
                        <div class="float-left">
                          <strong>{{ formatNumber(mold.lastShot) }}</strong>
                          <small class="text-muted"
                            >/ {{ formatNumber(mold.designedShot) }}</small
                          >
                        </div>
                        <div class="float-right">
                          <small class="text-muted">
                            <strong>{{ ratio(mold) }}%</strong>
                          </small>
                        </div>
                      </div>
                      <div class="progress progress-xs">
                        <div
                          class="progress-bar"
                          :class="ratioColor(mold)"
                          role="progressbar"
                          :style="ratioStyle(mold)"
                          :aria-valuenow="ratio(mold)"
                          aria-valuemin="0"
                          aria-valuemax="100"
                        ></div>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['location']"></th>
                    <td>
                      {{ mold.locationName }}
                      <button
                        v-on:click="showLocationHistory()"
                        type="button"
                        class="btn btn-primary btn-sm"
                        v-text="resources['history']"
                      ></button>
                    </td>
                  </tr>

                  <tr>
                    <th v-text="resources['latest_cycle_time']"></th>
                    <td>
                      {{ mold.lastCycleTime ? mold.lastCycleTime / 10 : ""
                      }}{{ mold.lastCycleTime ? "s" : "" }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['weighted_average_cycle_time']"></th>
                    <td>
                      {{
                        mold.weightedAverageCycleTime
                          ? mold.weightedAverageCycleTime / 10
                          : ""
                      }}{{ mold.weightedAverageCycleTime ? "s" : "" }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            COST-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-cost-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['cost_info']"></span>
                <img
                  id="mold-detail-cost-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                style="display: none"
                id="mold-detail-cost-info"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeleteTooling['cost']">
                    <th v-text="resources['cost_of_tooling']"></th>
                    <td>
                      {{
                        mold.cost == null
                          ? ""
                          : mold.costCurrencyTypeTitle != null
                          ? mold.costCurrencyTypeTitle
                          : "$"
                      }}{{ formatNumber(mold.cost) }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['accumulatedMaintenanceCost']">
                    <th v-text="resources['accumulated_maintenance_cost']"></th>
                    <td>
                      {{
                        mold.accumulatedMaintenanceCost == null
                          ? ""
                          : mold.costCurrencyTypeTitle != null
                          ? mold.costCurrencyTypeTitle
                          : "$"
                      }}{{ formatNumber(mold.accumulatedMaintenanceCost) }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['memo']">
                    <th v-text="resources['memo']"></th>
                    <td>{{ mold.memo }}</td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'COST'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            Supplier-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-supplier-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['supplier_info']"></span>
                <img
                  id="mold-detail-supplier-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-supplier-info"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeleteTooling['supplierCompanyName']">
                    <th v-text="resources['supplier']"></th>
                    <td>{{ mold.supplierCompanyName }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['labour']">
                    <th v-text="resources['required_labor']"></th>
                    <td>{{ mold.labour }}</td>
                  </tr>
                  <!--                  <tr>-->
                  <!--                    <th v-text="resources['uptime_target']"></th>-->
                  <!--                    <td>{{mold.uptimeTarget}}%</td>-->
                  <!--                  </tr>-->
                  <!--                  <tr>-->
                  <!--                    <th v-text="resources['uptime_tolerance_l1']"></th>-->
                  <!--                    <td>{{formatNumber(mold.uptimeLimitL1)}}% ~ {{formatNumber(mold.uptimeLimitL2)}}%</td>-->
                  <!--                  </tr>-->
                  <!--                  <tr>-->
                  <!--                    <th v-text="resources['uptime_tolerance_l2']"></th>-->
                  <!--                    <td>{{formatNumber(mold.uptimeLimitL2)}}%</td>-->
                  <!--                  </tr>-->
                  <!--<tr>-->
                  <!--<th>Supplier Code</th>-->
                  <!--<td>{{mold.supplierCompanyCode}}</td>-->
                  <!--</tr>-->
                  <!-- <tr>
                    <th>Hours per Shift</th>
                    <td>{{mold.hourPerShift}}</td>
                  </tr>-->
                  <tr v-if="!checkDeleteTooling['shiftsPerDay']">
                    <th v-text="resources['production_hour_per_day']"></th>
                    <td>{{ mold.shiftsPerDay }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['productionDays']">
                    <th v-text="resources['production_day_per_week']"></th>
                    <td>{{ mold.productionDays }}</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['maxCapacityPerWeek']">
                    <th v-text="resources['maximum_capacity_per_week']"></th>
                    <td>{{ mold.maxCapacityPerWeek }}</td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'SUPPLIER'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                  <!-- <tr>
                    <th>Production Week per Year</th>
                    <td>{{mold.productionWeeks}}</td>
                  </tr>-->
                  <!-- <tr>
                    <th>Supplier Tag ID on Tooling</th>
                    <td>{{mold.supplierTagId}}</td>
                  </tr>-->
                  <!--
                                <tr>
                                    <th>Supplier address</th>
                                    <td>London, UK</td>
                  </tr>-->
                </tbody>
              </table>
            </div>
            <!--            PRODUCT HERE-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-production-infomation`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['production_information']"></span>
                <img
                  id="mold-detail-production-infomation-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-production-infomation"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <tr v-if="!checkDeleteTooling['approvedCycleTime']">
                    <th v-text="resources['approved_cycle_time']"></th>
                    <td>
                      {{ getViewTimeSecond(mold.contractedCycleTimeSeconds) }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['cycleTimeLimit1']">
                    <th v-text="resources['cycle_time_tolerance_l1']"></th>
                    <td>
                      {{ formatNumber(mold.cycleTimeLimit1)
                      }}{{
                        mold.cycleTimeLimit1Unit
                          ? mold.cycleTimeLimit1Unit === "SECOND"
                            ? " " +
                              getViewUnitTimeSecondForValue(
                                mold.cycleTimeLimit1
                              )
                            : "%"
                          : "%"
                      }}
                      ~ {{ formatNumber(mold.cycleTimeLimit2)
                      }}{{
                        mold.cycleTimeLimit1Unit
                          ? mold.cycleTimeLimit1Unit === "SECOND"
                            ? " " +
                              getViewUnitTimeSecondForValue(
                                mold.cycleTimeLimit2
                              )
                            : "%"
                          : "%"
                      }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['cycleTimeLimit2']">
                    <th v-text="resources['cycle_time_tolerance_l2']"></th>
                    <td>
                      {{ formatNumber(mold.cycleTimeLimit2)
                      }}{{
                        mold.cycleTimeLimit2Unit
                          ? mold.cycleTimeLimit2Unit === "SECOND"
                            ? " " +
                              getViewUnitTimeSecondForValue(
                                mold.cycleTimeLimit2
                              )
                            : "%"
                          : "%"
                      }}
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['uptimeTarget']">
                    <th v-text="resources['uptime_target']"></th>
                    <td>{{ mold.uptimeTarget }}%</td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['uptimeLimitL1']">
                    <th v-text="resources['uptime_tolerance_l1']"></th>
                    <td>
                      {{ formatNumber(mold.uptimeLimitL1) }}% ~
                      {{ formatNumber(mold.uptimeLimitL2) }}%
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['uptimeLimitL2']">
                    <th v-text="resources['uptime_tolerance_l2']"></th>
                    <td>{{ formatNumber(mold.uptimeLimitL2) }}%</td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'PRODUCTION'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            MAINtance-->
            <div class="card">
              <div
                v-on:click="expand(`mold-detail-maintenance-info`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['maintenance_info']"></span>
                <img
                  id="mold-detail-maintenance-info-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                id="mold-detail-maintenance-info"
                style="display: none"
                class="table table-mold-details"
              >
                <tbody>
                  <!--<tr>
                                  <th>Total number of warranted shots</th>
                                  <td>{{formatNumber(mold.warrantedShot)}}</td>
                </tr>-->

                  <tr>
                    <th v-text="resources['maintenance_interval']"></th>
                    <td>{{ formatNumber(mold.preventCycle) }}</td>
                  </tr>
                  <tr>
                    <th
                      v-text="resources['upcoming_maintenance_tolerance']"
                    ></th>
                    <td>{{ formatNumber(mold.preventUpcoming) }}</td>
                  </tr>
                  <!-- <tr>
                  <th v-text="resources['overdue_maintenance_tolerance']"></th>
                  <td>{{formatNumber(mold.preventOverdue)}}</td>
                </tr> -->
                  <!--                <tr>-->
                  <!--                  <th v-text="resources['cycle_time_tolerance_l1']"></th>-->
                  <!--                  <td>{{formatNumber(mold.cycleTimeLimit1)}}{{mold.cycleTimeLimit1Unit ?-->
                  <!--                      (mold.cycleTimeLimit1Unit === 'SECOND' ? 's' : '%')-->
                  <!--                      : '%'}} ~ {{formatNumber(mold.cycleTimeLimit2)}}{{mold.cycleTimeLimit1Unit ?-->
                  <!--                      (mold.cycleTimeLimit1Unit === 'SECOND' ? 's' : '%')-->
                  <!--                      : '%'}}</td>-->
                  <!--                </tr>-->
                  <!--                <tr>-->
                  <!--                  <th v-text="resources['cycle_time_tolerance_l2']"></th>-->
                  <!--                  <td>{{formatNumber(mold.cycleTimeLimit2)}}{{mold.cycleTimeLimit2Unit ?-->
                  <!--                      (mold.cycleTimeLimit2Unit === 'SECOND' ? 's' : '%')-->
                  <!--                      : '%'}}</td>-->
                  <!--                </tr>-->
                  <tr v-if="isStatusEnabled">
                    <th v-text="resources['status']"></th>
                    <td>
                      {{ mold.useageTypeName }}
                    </td>
                  </tr>
                  <!--
                <tr>
                  <th v-text="resources['engineer_in_charge']"></th>
                  <td>
                      <span v-for="(engineerName , index) in mold.engineerNames">
                        <span v-if="index != 0">, </span><span>{{engineerName}}</span>
                      </span>
                    &lt;!&ndash;{{mold.engineerNames ? mold.engineerNames.toString(): ''}}&ndash;&gt;
                  </td>
                </tr>
-->

                  <!-- <tr>
                  <th>Engineer in Charge</th>
                  <td>Name: {{mold.engineer}}, Date: {{mold.engineerDate}}</td>
                </tr>-->
                  <tr v-if="!checkDeleteTooling['documentFiles']">
                    <th v-text="resources['maintenance_document']"></th>
                    <td>
                      <a
                        v-for="file in documentFiles"
                        href="javascript:void(0)"
                        class="btn btn-outline-dark btn-sm mr-1 mb-1"
                        @click="showFile(file)"
                        >{{ file.fileName }}</a
                      >
                    </td>
                  </tr>
                  <tr v-if="!checkDeleteTooling['instructionVideo']">
                    <th v-text="resources['instruction_video']"></th>
                    <td>
                      <a
                        v-for="file in instructionFiles"
                        href="javascript:void(0)"
                        class="btn btn-outline-dark btn-sm mr-1 mb-1"
                        @click="showFile(file)"
                        >{{ file.fileName }}</a
                      >
                    </td>
                  </tr>
                  <!--Custom field-->
                  <tr
                    v-for="(item, index) in mappingListConfigCustomField(
                      'MAINTENANCE'
                    )"
                    :key="index"
                  >
                    <th>{{ item.fieldName }}</th>
                    <td>
                      {{ item.defaultInputValue }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!--            REason-->
            <div
              v-if="
                mold.notificationStatus == 'DISAPPROVED' &&
                isDisapprovedReasonRequired
              "
              class="card"
            >
              <div
                v-on:click="expand(`mold-detail-reson-disapproval`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['reason_for_disapproval']"></span>
                <img
                  id="mold-detail-reson-disapproval-image"
                  style="position: absolute; right: 20px; top: 1rem"
                  src="/images/icon/angle-arrow-down.svg"
                  width="8"
                  height="8"
                />
              </div>

              <table
                style="display: none"
                id="mold-detail-reson-disapproval"
                class="table table-mold-details"
              >
                <tbody>
                  <tr>
                    <th v-text="resources['engineer_in_charge']"></th>
                    <td>{{ mold.approvedBy }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['date_time']"></th>
                    <td>{{ mold.approvedAt }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['reason_for_disapproval']"></th>
                    <td>{{ mold.reason }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer text-center">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
              v-text="resources['close']"
            ></button>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
module.exports = {
  props: {
    showFilePreviewer: Function,
    resources: Object,
    configDeleteField: {
      default: () => ({}),
      type: Object,
    },
    // configDeletePartField:{
    //   default: () => ({}),
    //   type: Object
    // }
  },
  components: {
    "location-history1": httpVueLoader("./location-history.vue"),
  },
  data() {
    return {
      mold: {
        approvedAt: 0,
        approvedBy: "",
        reason: "",
        equipmentCode: "",
        counter: {
          equipmentCode: "",
        },
        part: {
          partCode: "",
          category: {
            parent: {},
          },
        },
        location: {
          name: "",
        },
      },
      func: null,
      parts: [],
      documentFiles: [],
      instructionFiles: [],
      conditionFiles: [],
      locations: [1, 2, 3],
      // serverName: '',
      customFieldList: null,
      customFieldListPart: {},
      configCategoryList: null,
      checkDeleteTooling: {},
      checkDeletePart: {},
      options: {},
    };
  },
  computed: {
    isLifeYearsEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.lifeYearsEnabled);
    },
    isDisapprovedReasonRequired() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.disapprovedReasonRequired
      );
    },
    isStatusEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.statusEnabled);
    },
  },
  methods: {
    mappingListConfigCustomField(category) {
      if (this.customFieldList) {
        let _category = "TOOLING_" + category;
        return this.customFieldList.filter(
          (item) => item.propertyGroup === _category
        );
      }
    },
    getListConfigCustomFieldPart() {
      var self = this;
      if (this.mold.parts) {
        this.mold.parts.forEach((p) => {
          axios
            .get(
              `/api/custom-field-value/list-by-object?objectType=PART&objectId=${p.partId}`
            )
            .then(function (response) {
              self.customFieldListPart[p.partId] = response.data.map(
                (item) => ({
                  fieldName: item.fieldName,
                  defaultInputValue:
                    item.customFieldValueDTOList.length !== 0
                      ? item.customFieldValueDTOList[0].value
                      : null,
                  id: item.id,
                  required: item.required,
                })
              );
            });
        });
      }
    },
    getListConfigCustomField() {
      var self = this;
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=TOOLING&objectId=${this.mold.id}`
        )
        .then(function (response) {
          console.log(response, "hey this is your response");
          self.customFieldList = response.data.map((item) => ({
            propertyGroup: item.propertyGroup,
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        })
        .finally(() => {
          console.log(self.customFieldList, "customFieldList");
        });
    },
    //exxpand
    expand: function (elementId) {
      var content = document.getElementById(elementId);
      var contentImage = document.getElementById(`${elementId}-image`);
      // basic info and part is default
      if (content) {
        if (content.style.display === "table") {
          content.style.display = "none";
          contentImage.src = "/images/icon/angle-arrow-down.svg";
        } else {
          content.style.display = "table";
          contentImage.src = "/images/icon/angle-arrow-up.svg";
        }
      }
    },
    showLocationHistory: function () {
      if (this.func) {
        return this.func();
      }
      vm.showLocationHistory && vm.showLocationHistory(this.mold);
    },

    showMoldDetails: async function (mold, func) {
      console.log("mold: ", mold);
      this.mold = mold;
      this.func = func;
      // this.serverName = await Common.getSystem('server')
      const options = await Common.getSystem("options");
      this.options = JSON.parse(options);
      this.findMoldParts();
      //this.findDocumentFiles();
      this.instructionVideoFiles();
      this.getListConfigCustomField();
      this.getListConfigCustomFieldPart();
    },
    ratio: function (mold) {
      return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
    },
    ratioStyle: function (mold) {
      return "width: " + this.ratio(mold) + "%";
    },
    ratioColor: function (mold) {
      var ratio = this.ratio(mold);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },

    findMoldParts: function () {
      // Parts 조회
      var self = this;
      axios
        .get("/api/molds/" + this.mold.id + "/parts")
        .then(function (response) {
          console.log("response:parts ", response);
          self.parts = response.data.content;
          console.log("findMoldParts");
          console.log($("#op-mold-details"));
          $("#op-mold-details").modal("show");
          console.log($("#op-mold-details"));
        });
    },

    findDocumentFiles: function () {
      var self = this;

      var param = {
        storageType: "MOLD_MAINTENANCE_DOCUMENT",
        refId: self.mold.id,
      };

      axios
        .get("/api/file-storage?" + Common.param(param))
        .then(function (response) {
          self.documentFiles = response.data;
        });
    },

    instructionVideoFiles: function () {
      var self = this;

      var param = {
        storageTypes:
          "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION",
        refId: self.mold.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then(function (response) {
          self.conditionFiles = response.data["MOLD_CONDITION"]
            ? response.data["MOLD_CONDITION"]
            : [];
          self.documentFiles = response.data["MOLD_MAINTENANCE_DOCUMENT"]
            ? response.data["MOLD_MAINTENANCE_DOCUMENT"]
            : [];
          self.instructionFiles = response.data["MOLD_INSTRUCTION_VIDEO"]
            ? response.data["MOLD_INSTRUCTION_VIDEO"]
            : [];
        });
    },
    showFile(file) {
      $("#op-mold-details").modal("hide");
      this.showFilePreviewer(file);
      console.log("showFile");
    },
    backFromPreview() {
      $("#op-mold-details").modal("show");
      console.log("backFromPreview");
    },
    /*
    getViewToolingSize(mold){
      let sizeView='';
      if (mold.size != null && mold.size !== '' && mold.size.trim() != '' && mold.size.trim() !== 'xx' && mold.size != 'undefinedxundefinedxundefined') {
        sizeView = mold.size.replace('undefined','') + ' ' + mold.sizeUnitTitle;
        sizeView=sizeView.replaceAll('x',' x ').replace(/ +/,' ');
      }
      console.log('mold.size:',mold.size);
      console.log('sizeView:',sizeView);
      return sizeView;
    },
*/
    getViewTimeSecond(value) {
      if (value != null) {
        if (value == 0 || value == 1) {
          return value + " second";
        }
        return value + " seconds";
      }
      return "";
    },
    getViewUnitTimeSecondForValue(value) {
      if (value != null) {
        if (value == 0 || value == 1) {
          return "second";
        }
        return "seconds";
      }
      return "";
    },
    async checkCategoryConfigStatus(configCategory) {
      let listConfig = [];
      if (this.configCategoryList == null) {
        let res = await axios.get("/api/config/config-enable");
        if (res.data && res.data.length > 0) {
          listConfig = res.data;
        }
        this.configCategoryList = listConfig;
      }
      let currentConfig = this.configCategoryList.filter(
        (item) => item.configCategory === configCategory
      )[0];

      return currentConfig && currentConfig.enabled;
    },
    async getConfigDelete(configCategory) {
      let deleteField = {};
      try {
        if (this.checkCategoryConfigStatus(configCategory)) {
          let response = await axios.get(
            "/api/config?configCategory=" + configCategory
          );
          if (response.data && response.data.length > 0) {
            console.log("form response data", response.data);
            response.data.forEach((field) => {
              deleteField[field.fieldName] = field.deletedField;
            });
          }
        }
      } catch (e) {
        console.log(e);
      }
      return deleteField;
    },
  },
  async mounted() {
    this.checkDeletePart = await Common.getConfigDelete("PART");
    this.checkDeleteTooling = await Common.getConfigDelete("TOOLING");
    console.log("mounted this.checkDeletePart", this.checkDeletePart);
    console.log("mounted this.checkDeleteTooling", this.checkDeleteTooling);
  },
};
</script>
