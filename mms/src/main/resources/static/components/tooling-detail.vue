<template>
  <div>
    <div class="" :style="styleCustom">
      <!--            BASIC-->
      <div class="card">
        <div v-on:click="expand('mold-detail-basic-info')" class="card-header">
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
              <td>{{ mold?.toolingLetter }}</td>
            </tr>
            <tr v-if="!checkDeleteTooling['toolingType']">
              <th v-text="resources['tooling_type']"></th>
              <td>{{ mold.toolingType }}</td>
            </tr>
            <tr>
              <th v-text="resources['backup_tooling']"></th>
              <td>
                <span style="margin-right: 10px">{{
                  mold.backup ? resources["yes"] : resources["no"]
                }}</span>
                <!--                implement Backup detail later-->
                <!--
                <a-popover id="backup-tooling-popup" trigger="click">
                  <template slot="content">
                    <div style="padding: 6px 8px">
                      <div class="d-flex align-center justify-end">
                        <div
                          class="cursor-pointer"
                          @click="handleCloseBackupTooling"
                        >
                          <i class="icon-close-black&#45;&#45;small"></i>
                        </div>
                      </div>
                      <backup-tooling-content
                        :resources="resources"
                        :mold-id="mold.id"
                        :backup-tooling-list="mold.backupToolings"
                        @click-item="handleShowDetailItem"
                      ></backup-tooling-content>
                    </div>
                  </template>
                  <span v-if="mold.backup && mold.backupToolings" class="custom-hyper-link">
                    {{
                      mold.backupToolings.length > 1
                        ? `${mold.backupToolings.length} ${resources["backup_toolings_available"]}`
                        : `${mold.backupToolings.length} ${resources["backup_tooling_available"]}`
                    }}
                    <div class="hyper-link-icon" style="margin-left: 3px"></div>
                  </span>
                </a-popover>
-->
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['toolingComplexity']">
              <th v-text="resources['tooling_complexity']"></th>
              <td>{{ mold.toolingComplexity }}</td>
            </tr>

            <tr v-if="!checkDeleteTooling['designedShot']">
              <th v-text="resources['forecasted_max_shot']"></th>
              <td>{{ formatNumber(mold.designedShot) }}</td>
            </tr>
            <tr v-if="isLifeYearsEnabled && !checkDeleteTooling['lifeYears']">
              <th v-text="resources['forecasted_tool_life']"></th>
              <td>{{ mold.lifeYears }}</td>
            </tr>

            <tr v-if="!checkDeleteTooling['madeYear']">
              <th v-text="resources['year_of_tool_made']"></th>
              <td>{{ mold.madeYear }}</td>
            </tr>

            <tr v-if="!checkDeleteTooling['engineers']">
              <th v-text="resources['oem_engineer_in_charge']"></th>
              <td>
                <span v-for="(engineerName, index) in mold.engineerNames">
                  <span v-if="index != 0">, </span
                  ><span>{{ engineerName }}</span>
                </span>
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['plantEngineers']">
              <th v-text="resources['plant_engineer_in_charge']"></th>
              <td>
                <span v-for="(engineerName, index) in mold.plantEngineerNames">
                  <span v-if="index != 0">, </span
                  ><span>{{ engineerName }}</span>
                </span>
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['toolDescription']">
              <th v-text="resources['tool_description']"></th>
              <td>
                <textarea
                  readonly
                  style="border: none; resize: none; outline: none; width: 100%"
                  rows="4"
                  >{{ mold.toolDescription }}</textarea
                >
              </td>
            </tr>
            <!--Custom field-->
            <tr
              v-for="(item, index) in mappingListConfigCustomField('BASIC')"
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
            src="/images/icon/angle-arrow-down.svg"
            width="8"
            height="8"
          />
        </div>

        <table
          style="display: none"
          v-bind:id="'mold-detail-part-' + index"
          class="table table-mold-details"
        >
          <tbody>
            <tr v-if="!checkDeletePart['category']">
              <th v-text="resources['category']"></th>
              <td>
                <a
                  v-if="userType === 'OEM'"
                  href="javascript:void(0)"
                  @click="goToPage('CATEGORY', moldParts)"
                  style="display: block; width: 440px"
                >
                  {{ moldParts.categoryName }}
                </a>
                <p
                  v-else
                  class="category-disable"
                  style="display: block; width: 440px"
                >
                  {{ moldParts.categoryName }}
                </p>
              </td>
            </tr>
            <tr v-if="!checkDeletePart['projectName']">
              <th v-text="resources['product']"></th>
              <td>
                <a
                  v-if="userType === 'OEM'"
                  href="javascript:void(0)"
                  @click="goToPage('PRODUCT', moldParts)"
                  style="display: block; width: 440px"
                >
                  {{ moldParts.projectName }}
                </a>
                <p
                  class="category-disable"
                  style="display: block; width: 440px"
                >
                  {{ moldParts.projectName }}
                </p>
              </td>
            </tr>
            <tr>
              <th v-text="resources['part_id']"></th>
              <td>
                <div style="display: block; width: 440px">
                  {{ moldParts.partCode }}
                </div>
              </td>
            </tr>
            <tr>
              <th v-text="resources['part_name']"></th>
              <td v-bind:data-tooltip-text="moldParts.partName">
                {{ replaceLongtext(moldParts.partName, 40) }}
              </td>
            </tr>
            <tr>
              <th v-text="resources['quantity_required']"></th>
              <td>{{ formatNumber(moldParts.quantityRequired) }}</td>
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
              <th>
                {{ resources["working_cavities"] }} /
                {{ resources["total_number_of_cavities"] }}
              </th>
              <td>
                {{ moldParts.cavity
                }}{{
                  moldParts.cavity != null || moldParts.totalCavities != null
                    ? " / "
                    : ""
                }}{{ moldParts.totalCavities }}
              </td>
            </tr>
            <!--Custom field-->
            <tr
              v-for="(item, index) in customFieldListPart[moldParts.partId]"
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

            <tr v-if="!checkDeleteTooling['toolingPictureFile']">
              <th v-text="resources['tooling_picture']"></th>
              <td>
                <preview-images-system
                  :images-uploaded="conditionFiles"
                ></preview-images-system>
              </td>
            </tr>
            <!--Custom field-->
            <tr
              v-for="(item, index) in mappingListConfigCustomField('PHYSICAL')"
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
                {{ mold.weightRunner }} {{ mold.weightRunner ? "gram" : "" }}
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
            <tr>
              <th v-text="resources['tooling_status']"></th>
              <td>
                <span
                  v-if="mold.moldStatus === 'WORKING'"
                  class="op-active label label-working"
                ></span>
                <span
                  v-if="mold.moldStatus === 'IDLE'"
                  class="op-active label label-idle"
                ></span>
                <span
                  v-if="mold.moldStatus === 'NOT_WORKING'"
                  class="op-active label label-not-working"
                ></span>
                <span
                  v-if="mold.moldStatus === 'SENSOR_OFFLINE'"
                  class="op-active label label-sensor-offline"
                ></span>
                <span
                  v-if="mold.moldStatus === 'SENSOR_DETACHED'"
                  class="op-active label label-sensor-detached"
                ></span>
                <span
                  v-if="mold.moldStatus === 'NO_SENSOR'"
                  class="op-active label label-no-sensor"
                ></span>
                <span
                  v-if="mold.moldStatus === 'DISABLED'"
                  class="op-active label label-disabled"
                ></span>
                <span
                  v-if="mold.moldStatus === 'ON_STANDBY'"
                  class="op-active label label-on-standby"
                ></span>
                <span
                  class="mold-status-text"
                  v-text="getMoldStatusText(mold.moldStatus)"
                ></span>
              </td>
            </tr>
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
              <th v-text="resources['remaining_no_of_parts']"></th>
              <td>
                {{ formatNumber(mold.remainingPartsCount) }}
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
        <div v-on:click="expand(`mold-detail-cost-info`)" class="card-header">
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
              <th>{{ resources["acquisition_cost"] }}</th>
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
            <tr v-if="!checkDeleteTooling['tco']">
              <th v-text="resources['total_cost_of_ownership']"></th>
              <td>
                {{
                  mold.tco == null
                    ? ""
                    : mold.costCurrencyTypeTitle != null
                    ? mold.costCurrencyTypeTitle
                    : "$"
                }}{{ formatNumber(mold.tco) }}
              </td>
            </tr>
            <tr>
              <th v-text="resources['salvage_value']"></th>
              <td>
                {{
                  getNumberWithCurrency(
                    mold.salvageValue,
                    mold.costCurrencyType || "USD"
                  )
                }}
              </td>
            </tr>
            <tr>
              <th v-text="resources['po_date']"></th>
              <td>
                <span v-if="mold.poDate">{{
                  moment(mold.poDateFormat, "YYYYMMDD").format("YYYY-MM-DD")
                }}</span>
              </td>
            </tr>
            <tr>
              <th v-text="resources['po_number']"></th>
              <td>{{ mold.poNumber }}</td>
            </tr>
            <tr>
              <th v-text="'PO Document(s)'"></th>
              <td class="document-container">
                <span v-for="doc in costInfoDocFiles" class="document-wrapper">
                  {{ doc.fileName }}
                </span>
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['memo']">
              <th v-text="resources['memo']"></th>
              <td>{{ mold.memo }}</td>
            </tr>
            <!--Custom field-->
            <tr
              v-for="(item, index) in mappingListConfigCustomField('COST')"
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

      <div class="card">
        <div
          v-on:click="expand(`mold-detail-straight-line-depreciation`)"
          class="card-header"
        >
          <i class="fa fa-align-justify"></i>
          <span v-text="'Straight Line (S.L.) Depreciation'"></span>
          <img
            id="mold-detail-straight-line-depreciation-image"
            style="position: absolute; right: 20px; top: 1rem"
            src="/images/icon/angle-arrow-down.svg"
            width="8"
            height="8"
          />
        </div>

        <table
          style="display: none"
          id="mold-detail-straight-line-depreciation"
          class="table table-mold-details"
        >
          <tbody>
            <tr>
              <th>Current Book Value of Asset</th>
              <td>
                {{
                  getNumberWithCurrency(
                    mold.slCurrentBookValue,
                    mold.costCurrencyType || "USD"
                  )
                }}
              </td>
            </tr>
            <tr>
              <th>Depreciation</th>
              <td>
                <progress-bar
                  style="width: 155px"
                  :progress-percentage="
                    (mold.slDepreciation / mold.slDepreciationTerm) * 100
                  "
                  :title="
                    mold.slDepreciation +
                    ' of ' +
                    mold.slDepreciationTerm +
                    ' years'
                  "
                ></progress-bar>
              </td>
            </tr>
            <tr>
              <th>Depreciation Term</th>
              <td>{{ mold.slDepreciationTerm }} Years</td>
            </tr>
            <tr>
              <th>Yearly Depreciation</th>
              <td>
                {{
                  getNumberWithCurrency(
                    mold.slYearlyDepreciation || 0,
                    mold.costCurrencyType || "USD"
                  )
                }}
              </td>
            </tr>
            <tr>
              <th>Latest Depreciation Point</th>
              <td>{{ formatToDate(mold.slLatestDepreciationPoint) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card">
        <div
          v-on:click="expand(`mold-detail-units-production-depreciation`)"
          class="card-header"
        >
          <i class="fa fa-align-justify"></i>
          <span v-text="'Units of Production (U.P.) Depreciation'"></span>
          <img
            id="mold-detail-units-production-depreciation-image"
            style="position: absolute; right: 20px; top: 1rem"
            src="/images/icon/angle-arrow-down.svg"
            width="8"
            height="8"
          />
        </div>

        <table
          style="display: none"
          id="mold-detail-units-production-depreciation"
          class="table table-mold-details"
        >
          <tbody>
            <tr>
              <th>Current Book Value of Asset</th>
              <td>
                {{
                  getNumberWithCurrency(
                    mold.upCurrentBookValue,
                    mold.costCurrencyType || "USD"
                  )
                }}
                {{ mold.costCurrencyType || "USD" }}
              </td>
            </tr>
            <tr>
              <th>Depreciation</th>
              <td>
                <progress-bar
                  style="width: 155px"
                  :progress-percentage="depreciationPercentage"
                  :title="depreciationPercentageTitle"
                ></progress-bar>
              </td>
            </tr>
            <tr>
              <th>Depreciation Term</th>
              <td>{{ mold.upDepreciationTerm }} Shots</td>
            </tr>
            <tr>
              <th>Depreciation per Shot</th>
              <td>
                {{
                  getNumberWithCurrency(
                    mold.upDepreciationPerShot,
                    mold.costCurrencyType
                  )
                }}
              </td>
            </tr>
            <tr>
              <th>Latest Depreciation Point</th>
              <td>{{ formatToDate(mold.upLatestDepreciationPoint) }}</td>
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
              v-for="(item, index) in mappingListConfigCustomField('SUPPLIER')"
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
              <th v-text="resources['supplier_approved_cycle_time']"></th>
              <td>{{ getViewTimeSecond(mold.contractedCycleTimeSeconds) }}</td>
            </tr>
            <tr v-if="!checkDeleteTooling['toolmaker_approved_cycle_time']">
              <th v-text="resources['toolmaker_approved_cycle_time']"></th>
              <td>
                {{
                  getViewTimeSecond(mold.toolmakerContractedCycleTimeSeconds)
                }}
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
                        getViewUnitTimeSecondForValue(mold.cycleTimeLimit1)
                      : "%"
                    : "%"
                }}
                ~ {{ formatNumber(mold.cycleTimeLimit2)
                }}{{
                  mold.cycleTimeLimit1Unit
                    ? mold.cycleTimeLimit1Unit === "SECOND"
                      ? " " +
                        getViewUnitTimeSecondForValue(mold.cycleTimeLimit2)
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
                        getViewUnitTimeSecondForValue(mold.cycleTimeLimit2)
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
          <tbody v-if="moldPmPlan && moldPmPlan.pmStrategy === 'TIME_BASED'">
            <tr>
              <th v-text="resources['maintenance_interval']"></th>
              <td>
                <template v-if="moldPmPlan.pmFrequency === 'WEEKLY'">
                  {{ getWeeklyMaintenanceIntervalDisplay }}
                </template>
                <template v-if="moldPmPlan.pmFrequency === 'MONTHLY'">
                  {{ getMonthlyMaintenanceIntervalDisplay }}
                </template>
              </td>
            </tr>
            <tr>
              <th v-text="resources['first_schedule_starts_on']"></th>
              <td>
                {{
                  formatDate(
                    moldPmPlan.schedStartDate,
                    "YYYYMMDD",
                    "YYYY-MM-DD"
                  )
                }}
              </td>
            </tr>
            <tr>
              <th v-text="resources['continue']"></th>
              <td>{{ getContinueDisplay }}</td>
            </tr>
            <tr>
              <th v-text="resources['upcoming_maintenance_tolerance']"></th>
              <td>
                {{ formatNumber(moldPmPlan.schedUpcomingTolerance) }}
                {{ moldPmPlan.schedUpcomingTolerance > 1 ? "days" : "day" }}
              </td>
            </tr>
            <tr v-if="isStatusEnabled">
              <th v-text="resources['status']"></th>
              <td>
                {{ mold.useageTypeName }}
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['documentFiles']">
              <th v-text="resources['maintenance_document']"></th>
              <td>
                <a
                  v-for="(file, index) in documentFiles"
                  :key="index"
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
                  v-for="(file, index) in instructionFiles"
                  :key="index"
                  href="javascript:void(0)"
                  class="btn btn-outline-dark btn-sm mr-1 mb-1"
                  @click="showFile(file)"
                  >{{ file.fileName }}</a
                >
              </td>
            </tr>
            <tr>
              <th v-text="resources['last_maintenance_date']"></th>
              <td>{{ formatToDateTime(mold.lastMaintenanceDate) }}</td>
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
          <tbody v-else>
            <tr>
              <th v-text="resources['maintenance_interval']"></th>
              <td>{{ formatNumber(mold.preventCycle) }}</td>
            </tr>
            <tr>
              <th v-text="resources['upcoming_maintenance_tolerance']"></th>
              <td>{{ formatNumber(mold.preventUpcoming) }}</td>
            </tr>
            <tr v-if="isStatusEnabled">
              <th v-text="resources['status']"></th>
              <td>
                {{ mold.useageTypeName }}
              </td>
            </tr>
            <tr v-if="!checkDeleteTooling['documentFiles']">
              <th v-text="resources['maintenance_document']"></th>
              <td>
                <a
                  v-for="(file, index) in documentFiles"
                  :key="index"
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
                  v-for="(file, index) in instructionFiles"
                  :key="index"
                  href="javascript:void(0)"
                  class="btn btn-outline-dark btn-sm mr-1 mb-1"
                  @click="showFile(file)"
                  >{{ file.fileName }}</a
                >
              </td>
            </tr>
            <tr>
              <th v-text="resources['last_maintenance_date']"></th>
              <td>{{ formatToDateTime(mold.lastMaintenanceDate) }}</td>
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

      <div class="card">
        <div
          v-on:click="expand(`mold-detail-refurbishment-information`)"
          class="card-header"
        >
          <i class="fa fa-align-justify"></i>
          <span v-text="resources['refurbishment_information']"></span>
          <img
            id="mold-detail-refurbishment-information-image"
            style="position: absolute; right: 20px; top: 1rem"
            src="/images/icon/angle-arrow-down.svg"
            width="8"
            height="8"
          />
        </div>

        <table
          id="mold-detail-refurbishment-information"
          style="display: none"
          class="table table-mold-details"
        >
          <tbody>
            <tr>
              <th v-text="resources['last_refurbishment_date']"></th>
              <td>{{ formatToDateTime(mold.lastRefurbishmentDate) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card">
        <div
          v-on:click="expand(`mold-detail-work-order-information`)"
          class="card-header"
        >
          <i class="fa fa-align-justify"></i>
          <span v-text="resources['work_order_information']"></span>
          <img
            id="mold-detail-work-order-information-image"
            style="position: absolute; right: 20px; top: 1rem"
            src="/images/icon/angle-arrow-down.svg"
            width="8"
            height="8"
          />
        </div>

        <table
          id="mold-detail-work-order-information"
          style="display: none"
          class="table table-mold-details"
        >
          <tbody>
            <tr>
              <th v-text="resources['last_work_order_date']"></th>
              <td>
                {{ formatWorkOrderDate(mold?.lastWorkOrder?.completedOn) }}
                <span
                  class="custom-hyper-link"
                  style="margin-left: 4px"
                  @click="showWorkOrderDialog = true"
                  v-show="!!mold?.lastWorkOrder"
                >
                  {{ lastWorkOrderTypeTitle(mold.lastWorkOrder?.orderType) }}
                  <div class="hyper-link-icon" style="margin-left: 3px"></div>
                </span>
              </td>
            </tr>
            <tr>
              <th v-text="resources['work_order_history']"></th>
              <td>
                <span
                  class="custom-hyper-link"
                  @click="goToWorkOrderDetailPage"
                  v-show="mold.workOrderHistory > 0"
                >
                  {{ workOrderHistoryText }}
                  <div class="hyper-link-icon" style="margin-left: 3px"></div>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!--            REason-->
      <div
        v-if="
          (mold.notificationStatus == 'DISAPPROVED' ||
            mold.notificationStatus == 'APPROVED') &&
          isDisapprovedReasonRequired
        "
        class="card"
      >
        <div
          v-on:click="expand(`mold-detail-reson-disapproval`)"
          class="card-header"
        >
          <i class="fa fa-align-justify"></i>
          <span
            v-if="mold.notificationStatus == 'APPROVED'"
            v-text="resources['reason_for_approval']"
          ></span>
          <span v-else v-text="resources['reason_for_disapproval']"></span>
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
              <td>
                <a
                  @click="showHistoriesDataSubmission(mold)"
                  href="javascript:void(0)"
                  >{{ mold.approvedAt }}
                </a>
              </td>
            </tr>
            <tr>
              <th
                v-if="mold.notificationStatus == 'APPROVED'"
                v-text="resources['reason_for_approval']"
              ></th>
              <th v-else v-text="resources['reason_for_disapproval']"></th>
              <td>{{ mold.reason }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <reported-work-order-dialog
      :resources="resources"
      :visible="showWorkOrderDialog"
      :selected="mold.lastWorkOrder"
      :view-only="true"
      @close="showWorkOrderDialog = false"
    ></reported-work-order-dialog>
  </div>
</template>

<script>
const ORDER_TYPE = {
  GENERAL: { value: "GENERAL" },
  INSPECTION: { value: "INSPECTION" },
  EMERGENCY: { value: "EMERGENCY" },
  PREVENTATIVE_MAINTENANCE: { value: "PREVENTATIVE_MAINTENANCE" },
  CORRECTIVE_MAINTENANCE: { value: "CORRECTIVE_MAINTENANCE" },
  DISPOSAL: { value: "DISPOSAL" },
  REFURBISHMENT: { value: "REFURBISHMENT" },
};
const SUFFIXES = {
  1: "st",
  2: "nd",
  3: "rd",
  4: "th",
};
module.exports = {
  props: {
    mold: Object,
    showFilePreviewer: Function,
    resources: Object,
    configDeleteField: {
      default: () => ({}),
      type: Object,
    },
    styleCustom: Object,
    userType: String,
    showHistoriesDataSubmission: Function,
    moldPmPlan: Object,
  },
  components: {
    "location-history1": httpVueLoader("./location-history.vue"),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "progress-bar": httpVueLoader(
      "/components/@base/progress-bar/progress-bar.vue"
    ),
    "reported-work-order-dialog": httpVueLoader(
      "/components/work-order/src/reported-work-order-dialog.vue"
    ),
    "backup-tooling-content": httpVueLoader(
      "/components/mold-detail/backup-tooling-content.vue"
    ),
  },
  data() {
    return {
      func: null,
      parts: [],
      documentFiles: [],
      instructionFiles: [],
      conditionFiles: [],
      locations: [1, 2, 3],
      customFieldList: null,
      customFieldListPart: {},
      configCategoryList: null,
      costInfoDocFiles: [],
      options: {},
      showWorkOrderDialog: false,
    };
  },
  computed: {
    isToolingComplexityEnabled() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.toolingComplexityEnabled
      );
    },
    isLifeYearsEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.lifeYearsEnabled);
    },
    isUptimeTargetRequired() {
      return Boolean(this.options?.CLIENT?.moldDetail?.uptimeTargetRequired);
    },
    isStatusEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.statusEnabled);
    },
    isDisapprovedReasonRequired() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.disapprovedReasonRequired
      );
    },
    depreciationPercentage() {
      return +new Intl.NumberFormat({ maximumFractionDigits: 1 }).format(
        Math.min(
          100,
          (this.mold.upDepreciation / this.mold.upDepreciationTerm) * 100
        )
      );
    },
    depreciationPercentageTitle() {
      return (
        this.depreciationPercentage +
        "%" +
        ` (${new Intl.NumberFormat().format(
          this.mold.upDepreciation
        )} / ${new Intl.NumberFormat().format(this.mold.upDepreciationTerm)})`
      );
    },
    workOrderHistoryText() {
      return (
        this.mold.workOrderHistory +
        " " +
        (this.mold.workOrderHistory > 1
          ? this.resources["workorders"]
          : this.resources["workorder"])
      );
    },
    checkDeletePart() {
      return headerVm?.options?.PART?.fields?.reduce((prev, curr) => {
        return { ...prev, [curr.fieldName]: curr.deleteField };
      }, {});
    },
    checkDeleteTooling() {
      return headerVm?.options?.TOOLING?.fields?.reduce((prev, curr) => {
        return { ...prev, [curr.fieldName]: curr.deleteField };
      }, {});
    },
    getContinueDisplay() {
      if (this.moldPmPlan.recurrConstraintType === "UNTIL") {
        return `until ${this.formatDate(
          this.moldPmPlan.recurrDueDate,
          "YYYYMMDD",
          "YYYY-MM-DD"
        )}`;
      } else if (this.moldPmPlan.recurrConstraintType === "FOREVER") {
        return "forever";
      } else if (this.moldPmPlan.recurrConstraintType === "FOR") {
        return `for ${this.moldPmPlan.recurrNum} ${
          this.moldPmPlan.recurrNum > 1 ? "times" : "time"
        }`;
      }
    },
    getWeeklyMaintenanceIntervalDisplay() {
      return `Schedule every ${this.moldPmPlan.schedInterval} ${
        this.moldPmPlan.schedInterval > 1 ? "weeks" : "week"
      } on ${Common.formatter.toCase(
        this.moldPmPlan.schedDayOfWeek,
        "capitalize"
      )}`;
    },
    getMonthlyMaintenanceIntervalDisplay() {
      const suffixesNumber =
        this.moldPmPlan.schedOrdinalNum > 3
          ? 4
          : this.moldPmPlan.schedOrdinalNum;
      const suffixe = SUFFIXES[suffixesNumber];
      return `Schedule every ${this.moldPmPlan.schedInterval} ${
        this.moldPmPlan.schedInterval > 1 ? "months" : "month"
      } on the ${
        this.moldPmPlan.schedOrdinalNum
      }${suffixe} ${Common.formatter.toCase(
        this.moldPmPlan.schedDayOfWeek,
        "capitalize"
      )}`;
    },
  },
  methods: {
    handleShowDetailItem(item) {
      this.handleCloseBackupTooling();
      this.$emit("show-detail-item", item);
    },
    handleCloseBackupTooling() {
      const el = document.getElementById("backup-tooling-popup");
      if (el) {
        el.click();
      }
    },
    lastWorkOrderTypeTitle(type) {
      switch (type) {
        case ORDER_TYPE.GENERAL.value:
          return this.resources["general_work_order"];
        case ORDER_TYPE.INSPECTION.value:
          return this.resources["inspection_work_order"];
        case ORDER_TYPE.EMERGENCY.value:
          return this.resources["emergency_work_order"];
        case ORDER_TYPE.PREVENTATIVE_MAINTENANCE.value:
          return this.resources["pm_work_order"];
        case ORDER_TYPE.CORRECTIVE_MAINTENANCE.value:
          return this.resources["cm_work_order"];
        case ORDER_TYPE.REFURBISHMENT.value:
          return this.resources["refurbishment_work_order"];
        case ORDER_TYPE.DISPOSAL.value:
          return this.resources["disposal_work_order"];

        default:
          return "";
      }
    },
    formatWorkOrderDate(timestamp) {
      if (timestamp) {
        return moment.unix(timestamp).format("YYYY-MM-DD");
      }
      return "";
    },
    formatDate(date, oldFormat, newFormat) {
      return moment(date, oldFormat).format(newFormat);
    },
    getMoldStatusText(status) {
      switch (status) {
        case "WORKING":
          return "In Production";
        case "IDLE":
          return "Idle";
        case "NOT_WORKING":
          return "Inactive";
        case "SENSOR_OFFLINE":
          return "Sensor Offline";
        case "SENSOR_DETACHED":
          return "Sensor detached";
        case "NO_SENSOR":
          return "No Sensor";
        case "DISABLED":
          return "Disabled";
        case "ON_STANDBY":
          return "On Standby";
      }
    },
    goToWorkOrderDetailPage() {
      const query = Common.param({
        orderType: "",
        page: 1,
        isHistory: true,
        size: 20,
        isAllCompany: true,
        status: "",
        assetIds: this.mold.id,
        start: this.mold?.lastWorkOrder?.completedOn,
        end: this.mold?.lastWorkOrder?.completedOn,
      });
      location.href = Common.URL.WORK_ORDER + "?" + query;
    },
    getNumberWithCurrency(number, currency) {
      return new Intl.NumberFormat("en", {
        style: "currency",
        currency: currency || "USD",
      }).format(number);
    },
    goToPage(type, item) {
      let param = `id=${item.categoryId || ""}`;
      if (type === "PRODUCT") {
        param = param + `&productId=${item.projectId}`;
      }
      location.href = `${Common.PAGE_URL.CATEGORY}?${param}`;
    },
    mappingListConfigCustomField(category) {
      if (this.customFieldList) {
        let _category = "TOOLING_" + category;
        return this.customFieldList.filter(
          (item) => item.propertyGroup === _category
        );
      }
    },
    getListConfigCustomFieldPart() {
      if (this.mold.parts) {
        this.mold.parts.forEach((p) => {
          axios
            .get(
              `/api/custom-field-value/list-by-object?objectType=PART&objectId=${p.partId}`
            )
            .then((response) => {
              this.customFieldListPart[p.partId] = response.data.map(
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
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=TOOLING&objectId=${this.mold.id}`
        )
        .then((response) => {
          console.log(response, "hey this is your response");
          this.customFieldList = response.data.map((item) => ({
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
          console.log(this.customFieldList, "customFieldList");
        });
    },
    //exxpand
    expand(elementId) {
      var content = document.getElementById(elementId);
      var contentImage = document.getElementById(`${elementId}-image`);
      console.log("expand", elementId, content, contentImage);
      // basic info and part is default
      if (content && contentImage) {
        if (content.style.display === "table") {
          content.style.display = "none";
          contentImage.src = "/images/icon/angle-arrow-down.svg";
        } else {
          content.style.display = "table";
          contentImage.src = "/images/icon/angle-arrow-up.svg";
        }
      }
    },
    showLocationHistory() {
      if (this.func) {
        return this.func();
      }
      vm.showLocationHistory && vm.showLocationHistory(this.mold);
    },
    async showToolingDetail(mold, func) {
      console.log("mold: ", mold);
      console.log("mold!=this.mold: ", mold != this.mold);
      if (mold != this.mold) {
        // this.mold = mold; // TODO(ducnm2010): old code was implemented in anti-pattern way, need recheck
      }
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
    ratio(mold) {
      return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
    },
    ratioStyle(mold) {
      return "width: " + this.ratio(mold) + "%";
    },
    ratioColor(mold) {
      var ratio = this.ratio(mold);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },

    findMoldParts() {
      // Parts 조회
      axios.get("/api/molds/" + this.mold.id + "/parts").then((response) => {
        console.log("response:parts ", response);
        this.parts = response.data.content;
        console.log("findMoldParts");
      });
    },

    findDocumentFiles() {
      var param = {
        storageType: "MOLD_MAINTENANCE_DOCUMENT",
        refId: this.mold.id,
      };

      axios.get("/api/file-storage?" + Common.param(param)).then((response) => {
        this.documentFiles = response.data;
      });
    },

    instructionVideoFiles() {
      const param = {
        storageTypes:
          "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION,MOLD_PO_DOCUMENT",
        refId: this.mold.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          this.conditionFiles = response.data["MOLD_CONDITION"]
            ? response.data["MOLD_CONDITION"]
            : [];
          this.documentFiles = response.data["MOLD_MAINTENANCE_DOCUMENT"]
            ? response.data["MOLD_MAINTENANCE_DOCUMENT"]
            : [];
          this.instructionFiles = response.data["MOLD_INSTRUCTION_VIDEO"]
            ? response.data["MOLD_INSTRUCTION_VIDEO"]
            : [];
          this.costInfoDocFiles = response.data["MOLD_PO_DOCUMENT"]
            ? response.data["MOLD_PO_DOCUMENT"]
            : [];
        });
    },
    showFile(file) {
      this.showFilePreviewer(file);
      console.log("showFile");
    },
    backFromPreview() {
      console.log("backFromPreview");
    },
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
    console.log("mounted this.checkDeletePart", this.checkDeletePart);
  },
};
</script>
<style>
.label-working {
  background: #41ce77;
}

.label-idle {
  background: #f7cc57;
}

.label-not-working {
  background: #aca6bc;
}

.label-sensor-offline {
  background: #e34537;
}

.label-sensor-detached {
  border: 1px dashed #e34537;
}

.label-no-sensor {
  border: 1px solid #4b4b4b;
}

.label-disabled {
  background: #4b4b4b;
}

.label-on-standby {
  background-color: #c4c4c4;
  border: 1px solid #4b4b4b;
}
</style>
<style scoped>
.category-disable {
  margin-bottom: 0px !important;
}

.document-container {
  display: flex;
  flex-wrap: wrap;
}

.document-wrapper {
  background: #ffc68d;
  border-radius: 3px;
  width: auto;
  padding: 4px 8px;
  margin-right: 8px;
  margin-bottom: 8px;
}

.op-active {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  padding: 0px;
}
</style>
