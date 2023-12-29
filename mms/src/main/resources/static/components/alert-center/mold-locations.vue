<template>
  <div>
    <notify-alert
      type="RELOCATION"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>
    <!-- <div class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['relocation']"></strong>

            <div class="card-header-actions">
              <span class="card-header-action">
                <small v-show="alertOn" class="text-muted"
                  ><span v-text="resources['total'] + ':'"></span>
                  {{ total }}</small
                >
              </span>
            </div>
          </div>
          <div class="card-body">
            <input type="hidden" v-model="requestParam.sort" />

            <div class="form-row">
              <div class="col-12 mb-6 mb-md-0">
                <common-searchbar
                  :placeholder="resources['search_by_selected_column']"
                  :request-param="requestParam"
                  :on-search="search"
                ></common-searchbar>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div> -->

    <div class="row">
      <div class="col-lg-12">
        <div class="card wave1">
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span v-text="resources['striped_tbl']"></span>
          </div>
          <div style="overflow-x: auto !important" class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'alert' }"
                  href="#"
                  @click.prevent="tab('alert')"
                >
                  <span v-text="resources['alert']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'alert'"
                  >
                    {{ total }}
                  </span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'confirmed' }"
                  href="#"
                  @click.prevent="tab('confirmed')"
                >
                  <span v-text="resources['history_log']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'confirmed'"
                    >{{ total }}</span
                  >
                </a>
              </li>

              <customization-modal
                v-show="alertOn"
                style="position: absolute; right: 20px"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              />
            </ul>

            <div v-show="alertOn">
              <table
                style="overflow-x: auto !important"
                class="table table-responsive-sm table-striped"
              >
                <thead id="thead-actionbar" class="custom-header-table">
                  <tr
                    :class="{ invisible: listChecked.length != 0 }"
                    style="height: 57px"
                    class="tr-sort"
                  >
                    <th style="width: 40px; vertical-align: middle !important">
                      <select-all
                        :resources="resources"
                        :show="allColumnList.length > 0"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['tooling', 'toolings']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </th>
                    <template
                      v-for="(column, index) in allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <template v-if="column.field === 'confirmedAtShow'">
                        <th
                          v-if="requestParam.status === 'confirmed'"
                          :key="index"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                              'text-left': ['OP', 'Status'].includes(
                                column.label
                              ),
                            },
                            isDesc ? 'desc' : 'asc',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ column.label }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template
                        v-else-if="column.field === 'moldLocationStatus'"
                      >
                        <th
                          :key="index"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ column.label }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <th
                        v-else-if="column.sortField === 'mold.lastShot'"
                        v-click-outside="closeLastShot"
                        :key="index"
                        :class="[
                          {
                            __sort: column.sortable,
                            active: currentSortType === column.sortField,
                            'text-left': ['OP', 'Status', 'Action'].includes(
                              column.label
                            ),
                          },
                          isDesc ? 'desc' : 'asc',
                        ]"
                        @click="sortBy(column.sortField)"
                      >
                        <span>
                          {{ column.label }}
                        </span>
                        <span v-if="column.sortable" class="__icon-sort"></span>
                        <last-shot-filter
                          v-show="
                            lastShotDropdown &&
                            column.sortField === 'mold.lastShot'
                          "
                          :last-shot-data="lastShotData"
                          @update-variable="updateVariable"
                        ></last-shot-filter>
                      </th>
                      <th
                        v-else
                        :class="[
                          {
                            __sort: column.sortable,
                            active: currentSortType === column.sortField,
                            'text-left': ['OP', 'Status'].includes(
                              column.label
                            ),
                          },
                          isDesc ? 'desc' : 'asc',
                        ]"
                        :key="column.field + index"
                        @click="sortBy(column.sortField)"
                      >
                        <span>{{ column.label }}</span>
                        <span v-if="column.sortable" class="__icon-sort"></span>
                      </th>
                    </template>
                  </tr>
                  <tr
                    :class="{
                      'd-none zindexNegative': listChecked.length == 0,
                    }"
                    id="action-bar"
                    class="tr-sort empty-tr"
                  >
                    <th class="empty-th">
                      <div class="first-checkbox-zone2">
                        <div
                          class="first-checkbox2"
                          style="
                            display: flex;
                            align-items: center;
                            width: 110px;
                          "
                        >
                          <select-all
                            :resources="resources"
                            :show="allColumnList.length > 0"
                            :checked="isAll"
                            :total="total"
                            :count="results.length"
                            :unit="['tooling', 'toolings']"
                            @select-all="handleSelectAll('all')"
                            @select-page="handleSelectAll('page')"
                            @deselect="handleSelectAll('deselect')"
                          ></select-all>
                        </div>
                        <div
                          v-if="listChecked.length > 1"
                          class="d-flex align-center"
                        >
                          <span style="height: 20px"
                            >No batch action is available</span
                          >
                        </div>
                        <div v-if="listChecked.length == 1" class="action-bar">
                          <div
                            class="action-item"
                            v-click-outside="closeShowDropDown"
                          >
                            <div
                              class="change-status-dropdown drowdown d-inline"
                            >
                              <div
                                title="Change status"
                                class="change-status"
                                @click="handleShowAuthorizeModal"
                              >
                                <span
                                  v-if="requestParam.status !== 'confirmed'"
                                  >{{ resources["approve"] }}</span
                                >
                                <span v-else>{{
                                  resources["edit_approval_status"]
                                }}</span>
                                <div class="icon-check__circle"></div>
                              </div>
                              <ul
                                v-if="requestParam.status !== 'confirmed'"
                                class="dropdown-menu"
                                :class="[showDropdown ? 'show' : '']"
                              >
                                <li v-if="isBtnAlertConfirmPermitted">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeLocationStatus('CONFIRMED')
                                    "
                                  >
                                    Confirm Alert
                                  </a>
                                </li>
                              </ul>
                            </div>
                          </div>

                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            @click="showSystemNoteModal()"
                          >
                            <span>Note</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            @click="showCreateWorkorder(listCheckedFull[0])"
                            style="align-items: flex-start"
                          >
                            <span>Create Workorder</span>
                            <img
                              src="/images/icon/icon-workorder.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                        </div>
                      </div>
                    </th>
                  </tr>
                </thead>

                <tbody class="op-list" style="display: none">
                  <template>
                    <tr
                      v-show="!isLoading"
                      v-for="(result, index) in results"
                      :key="result.id"
                      :id="result.id"
                    >
                      <td>
                        <template v-if="listChecked.length >= 0">
                          <input
                            @click="check"
                            :checked="checkSelect(result.id)"
                            class="checkbox"
                            type="checkbox"
                            :value="result.id"
                          />
                        </template>
                      </td>
                      <template
                        v-for="(column, columnIndex) in allColumnList"
                        v-if="column.enabled && !column.hiddenInToggle"
                      >
                        <td
                          v-if="column.field === 'toolingId'"
                          class="text-left"
                          :key="'toolingId' + columnIndex"
                        >
                          <a
                            href="#"
                            @click.prevent="showChart(result)"
                            style="color: #1aaa55"
                            class="mr-1"
                            ><i class="fa fa-bar-chart"></i
                          ></a>
                          <a href="#" @click.prevent="showMoldDetails(result)">
                            {{ result.mold.equipmentCode }}
                          </a>
                          <div class="small text-muted font-size-11-2">
                            <span v-text="resources['updated_on']"></span>
                            {{ formatToDate(result.mold.lastShotAt) }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'company'"
                          :key="'company' + columnIndex"
                        >
                          <a
                            class="font-size-14"
                            href="#"
                            @click.prevent="
                              showCompanyDetailsById(
                                result.mold.companyIdByLocation
                              )
                            "
                          >
                            {{ result.mold.companyName }}
                          </a>
                          <div class="small text-muted font-size-11-2">
                            {{ result.mold.companyCode }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'previousLocation'"
                          :key="'previousLocation' + columnIndex"
                        >
                          <div v-if="result.previousLocation">
                            <div class="font-size-14">
                              <a
                                href="#"
                                @click.prevent="
                                  showLocationHistory(result.mold)
                                "
                              >
                                {{ result.previousLocation.name }}
                              </a>
                            </div>
                            <span class="font-size-11-2">
                              {{ result.previousLocation.locationCode }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'location'"
                          :key="'location' + columnIndex"
                        >
                          <div v-if="result.mold.locationCode">
                            <div class="font-size-14">
                              <a
                                href="#"
                                @click.prevent="
                                  showLocationHistory(result.mold)
                                "
                              >
                                {{ result.mold.locationName }}
                              </a>
                            </div>
                            <span class="font-size-11-2">{{
                              result.mold.locationCode
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'updatedAt'"
                          :key="'updatedAt' + columnIndex"
                        >
                          <a
                            v-if="result.updatedDateTime"
                            href="#"
                            @click.prevent="showLocationHistory(result.mold)"
                          >
                            {{ result.updatedDate }}
                          </a>
                          <div>
                            <span class="font-size-11-2">
                              {{ convertTime(result.updatedAt) }}
                            </span>
                          </div>
                        </td>
                        <template
                          v-else-if="column.field === 'confirmedAtShow'"
                        >
                          <td
                            class="text-left"
                            v-if="requestParam.status === 'confirmed'"
                            :key="'confirmedAtShow' + columnIndex"
                          >
                            {{ result.confirmedAtShow }}
                          </td>
                        </template>
                        <td
                          v-else-if="column.field === 'operatingStatus'"
                          :key="'operatingStatus' + columnIndex"
                          style="vertical-align: middle !important"
                          class="text-center op-status-td"
                        >
                          <span
                            v-if="
                              result.mold.operatingStatus == 'WORKING' &&
                              ['INSTALLED', 'CHECK'].includes(
                                result.mold.equipmentStatus
                              )
                            "
                            class="op-active label label-success"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'IDLE' &&
                              ['INSTALLED', 'CHECK'].includes(
                                result.mold.equipmentStatus
                              )
                            "
                            class="op-active label label-warning"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'NOT_WORKING' &&
                              ['INSTALLED', 'CHECK'].includes(
                                result.mold.equipmentStatus
                              )
                            "
                            class="op-active label label-inactive"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'DISCONNECTED' &&
                              ['INSTALLED', 'CHECK'].includes(
                                result.mold.equipmentStatus
                              )
                            "
                            class="op-active label label-danger"
                          ></span>
                        </td>
                        <!-- <template v-else-if="column.field === 'moldLocationStatus'">
                          <td v-if="result.moldLocationStatus === 'CHANGED'" :key="columnIndex"
                            style="vertical-align: middle !important" class="text-center op-status-td">
                            <span class="label label-danger" v-text="resources['changed']"></span>
                          </td>
                        </template> -->
                        <td
                          class="text-left tooling-status"
                          v-else-if="column.field === 'toolingStatus'"
                        >
                          <mold-status
                            :mold="result.mold"
                            :resources="resources"
                            :rule-op-status="ruleOpStatus"
                          />
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'sensorStatus'"
                        >
                          <counter-status
                            :mold="result.mold"
                            :resources="resources"
                          />
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'moldLocationStatus'"
                        >
                          <base-priority-card
                            :color="
                              prioritymoldLocationStatus[result[column.field]]
                                .color
                            "
                          >
                            <span>
                              {{
                                prioritymoldLocationStatus[result[column.field]]
                                  .title
                              }}
                            </span>
                          </base-priority-card>
                        </td>
                        <td
                          v-else-if="
                            ['lastShot', 'mold.lastShot'].includes(column.field)
                          "
                          :key="'lastShot' + columnIndex"
                          class="text-left"
                        >
                          {{ formatNumber(result.mold.accumulatedShot) }}
                        </td>
                        <td
                          v-else-if="
                            ['mold.utilizationRate'].includes(column.field)
                          "
                          :key="'moldUtilization' + columnIndex"
                          class="text-left"
                        >
                          <div class="clearfix">
                            <div class="float-left font-size-14">
                              <strong>
                                {{ formatNumber(result.mold.lastShot) }}
                              </strong>
                              <span
                                >/{{
                                  formatNumber(result.mold.designedShot)
                                }}</span
                              >
                            </div>
                          </div>
                          <div
                            style="
                              display: flex;
                              align-items: center;
                              width: 120px;
                            "
                          >
                            <small class="text-muted font-size-11-2">
                              <strong>{{ ratio(result.mold) }}%</strong>
                            </small>
                            <div
                              style="width: 100%; margin-left: 5px"
                              class="progress progress-xs"
                            >
                              <div
                                class="progress-bar"
                                :class="ratioColor(result.mold)"
                                role="progressbar"
                                :style="ratioStyle(result.mold)"
                                :aria-valuenow="ratio(result.mold)"
                                aria-valuemin="0"
                                aria-valuemax="100"
                              ></div>
                            </div>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'part' && result.mold"
                          style="max-width: 600px"
                          :key="'part' + columnIndex"
                        >
                          <mold-parts-dropdown-view
                            :parts="result.mold.parts"
                            :mold="result.mold"
                            :index="index"
                            :show-part-chart="showPartChart"
                          ></mold-parts-dropdown-view>
                        </td>
                        <td
                          v-else-if="column.field === 'mold.cycleTime'"
                          :key="'moldCycleTime' + columnIndex"
                          class="text-left"
                        >
                          <span class="font-size-14">
                            {{ result.mold.lastCycleTime / 10 }}
                          </span>
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.weightedAverageCycleTime'
                          "
                          :key="'moldWeightedAverageCycleTime' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.weightedAverageCycleTime / 10 }}
                          </span>
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.cycleTimeToleranceL1'
                          "
                          :key="'moldCycleTimeToleranceL1' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.cycleTimeToleranceL1 }}</span
                          >
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.cycleTimeToleranceL1
                                  ? handleCycleTimeTolerance1(result.mold)
                                  : ""
                              }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.cycleTimeToleranceL2'
                          "
                          :key="'moldCycleTimeToleranceL2' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.cycleTimeToleranceL2 }}</span
                          >
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.cycleTimeToleranceL2
                                  ? handleCycleTimeTolerance2(result.mold)
                                  : ""
                              }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.uptimeTarget'"
                          :key="'moldUptimeTarget' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.uptimeTarget
                          }}</span>
                          <div>
                            <span class="font-size-11-2">
                              {{ result.mold.uptimeTarget ? "%" : "" }}</span
                            >
                          </div>
                        </td>
                        <td
                          v-else-if="column.field === 'mold.engineerInCharge'"
                          :key="'moldEngineerInCharge' + columnIndex"
                          style="vertical-align: middle !important"
                          class="text-left"
                        >
                          <user-list-cell
                            :resources="resources"
                            :user-list="result.mold.engineers"
                          ></user-list-cell>
                        </td>
                        <td
                          v-else-if="column.field === 'weightOfRunnerSystem'"
                          :key="'weightOfRunnerSystem' + columnIndex"
                          class="text-left"
                        >
                          <span class="font-size-14">
                            {{
                              result.mold.weightRunner
                                ? result.mold.weightRunner
                                : ""
                            }}
                          </span>
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.weightRunner
                                  ? getUnitName(result.mold.weightUnitTitle)
                                  : ""
                              }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.contractedCycleTimeSeconds'
                          "
                          :key="'moldContractedCycleTimeSeconds' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.contractedCycleTimeSeconds }}
                          </span>
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.quotedMachineTonnage'
                          "
                          :key="'moldQuotedMachineTonnage' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.quotedMachineTonnage }}</span
                          >
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.quotedMachineTonnage ? "tonnes" : ""
                              }}</span
                            >
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.preventCycle'"
                          :key="'moldPreventCycle' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.preventCycle }}</span
                          >
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.preventCycle ? "shots" : ""
                              }}</span
                            >
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.maxCapacityPerWeek'"
                          :key="'moldMaxCapacityPerWeek' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.maxCapacityPerWeek }}</span
                          >
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.maxCapacityPerWeek ? "shots" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.preventOverdue'"
                          :key="'moldPreventOverdue' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.preventOverdue
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.preventOverdue ? "shots" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.shiftsPerDay'"
                          :key="'moldShiftsPerDay' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.shiftsPerDay
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.shiftsPerDay ? "hours" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.productionDays'"
                          :key="'moldProductionDays' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.productionDays
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.productionDays ? "days" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.toolingSizeView'"
                          :key="'moldToolingSizeView' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.size
                          }}</span>
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.size
                                  ? getUnitName(result.mold.sizeUnit)
                                  : ""
                              }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.toolingWeightView'"
                          :key="'moldToolingWeightView' + columnIndex"
                        >
                          <span class="font-size-14">
                            {{ result.mold.weight }}</span
                          >
                          <div>
                            <span class="font-size-11-2">
                              {{
                                result.mold.weight
                                  ? getUnitName(result.mold.weightUnit)
                                  : ""
                              }}
                            </span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.uptimeLimitL1'"
                          :key="'moldUptimeLimitL1' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.uptimeLimitL1
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.uptimeLimitL1 ? "%" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.uptimeLimitL2'"
                          :key="'moldUptimeLimitL2' + columnIndex"
                        >
                          <span class="font-size-14">{{
                            result.mold.uptimeLimitL2
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.uptimeLimitL2 ? "%" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'machineCode'"
                          :key="'machineCode' + columnIndex"
                        >
                          <div v-if="result.machine">
                            <a
                              href="#"
                              @click.prevent="
                                showMachineDetails(result.machine.id)
                              "
                            >
                              {{ result.machine.machineCode }}
                            </a>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field && column.field.includes('.')"
                          :key="'mold' + columnIndex"
                        >
                          {{
                            valueWithKeyMultiPath(
                              result,
                              column.field,
                              column.isCustomField
                            )
                          }}
                        </td>

                        <td class="text-left" v-else :key="columnIndex">
                          {{ result[column.field] }}
                        </td>
                      </template>
                      <!-- <td style="vertical-align: middle !important" class="text-center"
                        v-if="requestParam.status === 'confirmed'">
                        <a href="javascript:void(0)" class="table-action-link note"
                          v-on:click="showRelocationConfirmHistory(result.mold)" title="View"
                          v-if="result.moldLocationStatus == 'CONFIRMED'">
                          <img height="18" src="/images/icon/view.png" alt="View" />
                        </a>
                        <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note"
                          class="table-action-link note">
                          <img src="/images/icon/note.svg" alt="Add note" />
                        </a>
                      </td> -->
                    </tr>
                  </template>
                </tbody>
              </table>
              <div
                class="no-results d-none"
                v-text="resources['no_results']"
              ></div>
              <div v-show="!isLoading" class="row">
                <div class="col-md-10 col-sm-10 col-xs-10">
                  <ul class="pagination">
                    <li
                      v-for="(data, index) in pagination"
                      :key="index"
                      class="page-item"
                      :class="{ active: data.isActive }"
                    >
                      <a class="page-link" @click="paging(data.pageNumber)">
                        {{ data.text }}
                      </a>
                    </li>
                  </ul>
                </div>
              </div>
              <div v-show="!alertOn" class="card-body empty-wrap">
                <div class="square-empty"></div>
                <div class="empty-wrap-caption">
                  <span v-text="resources['alert_turn_off_message_r1']"></span>
                </div>
                <div class="empty-wrap-sub">
                  <span v-text="resources['alert_turn_off_message_r2']"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <authorize-modal
      :resources="resources"
      :visible="visibleAuthorizeModal"
      :item="listCheckedFull[0]"
      :type="authorizeModalType"
      @close="handleCloseAuthorizeModal"
      @reload="handleReloadTable"
    ></authorize-modal>
  </div>
</template>
<script>
module.exports = {
  name: "AlertRelocation",
  components: {
    "authorize-modal": httpVueLoader(
      "/components/alert-center/components/authorize-modal.vue"
    ),
  },
  props: {
    resources: Object,
    permission: Object,
    showCreateWorkorder: Function,
    ruleOpStatus: Object,
    searchText: String,
    reloadKey: [String, Number],
    enabledSearchBar: {
      type: Boolean,
      default: true,
    },
    showNote: Function,
  },
  data() {
    return {
      listChecked: [],
      listCheckedTracked: {},
      showDrowdown: null,
      isAllTracked: [],
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "alert", // alert|confirmed
        sort: "id,desc",
        page: 1,

        locationChanged: true,
        operatingStatus: "",
        equipmentStatus: "",
        inList: true,
      },

      equipmentStatusCodes: [],
      listCategories: [],
      sortType: {
        TOOLING_ID: "mold.equipmentCode",
        COMPANY: "mold.location.company.name",
        LOCATION: "mold.location.name",
        DATE: "updatedAt",
        OP: "mold.operatingStatus",
        STATUS: "moldLocationStatus",
        CONFIRMED_AT: "confirmedAt",
      },
      pageType: "RELOCATION_ALERT",
      allColumnList: [],
      currentSortType: "id",
      isDesc: true,
      isLoading: false,
      alertOn: true,
      showDropdown: false,
      listCheckedFull: [],
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      listAllIds: [],
      visibleAuthorizeModal: false,
      authorizeModalType: "CREATE",
      prioritymoldLocationStatus: {
        UNAPPROVED: {
          title: "Disapproved",
          color: "red",
        },
        APPROVED: {
          title: "Approved",
          color: "green",
        },
        PENDING: {
          title: "Approval Pending",
          color: "grey",
        },
      },
      isBtnAlertConfirmPermitted: false,
    };
  },
  computed: {
    isAll() {
      if (!this.results.length) return false;
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText(newVal) {
      this.requestParam.query = newVal;
      this.paging(1);
    },
    listChecked(newVal) {
      console.log("@watch:listChecked", newVal);
    },
    listCheckedFull(newVal) {
      console.log("@watch:listCheckedFull", newVal);
    },
    listCheckedTracked: {
      handler(newVal) {
        console.log("@watch:listCheckedTrack", newVal);
      },
      immediate: true,
    },
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.setAllColumnList();
        }
      },
      immediate: true,
    },
  },
  methods: {
    handleReloadTable() {
      this.paging();
      this.listCheckedFull = [];
      this.listChecked = [];
      this.listCheckedTracked = [];
      this.isAllTracked = [];
    },
    handleShowAuthorizeModal() {
      this.authorizeModalType =
        this.requestParam.status === "confirmed" ? "EDIT" : "CREATE";
      this.visibleAuthorizeModal = true;
    },
    handleCloseAuthorizeModal() {
      this.visibleAuthorizeModal = false;
    },
    showMachineDetails: function (machineId) {
      let child = Common.vue.getChild(
        this.$parent.$children,
        "machine-details"
      );
      if (child != null) {
        child.showDetailsById(machineId);
      }
    },
    updateVariable(data) {
      this.lastShotData = data;
    },
    closeLastShot() {
      if (this.lastShotDropdown) {
        console.log(this.lastShotData, "closeLastShot");
        this.requestParam.accumulatedShotFilter =
          (this.lastShotData && this.lastShotData.filter) || "";
        if (this.lastShotData && this.lastShotData.sort) {
          this.currentSortType = "mold.lastShot";
          this.isDesc = this.lastShotData.sort === "desc";
          this.requestParam.sort = `${
            this.lastShotData.filter === null
              ? "mold.lastShot"
              : `accumulatedShot.${this.lastShotData.filter}`
          }`;
          this.requestParam.sort += `,${this.isDesc ? "desc" : "asc"}`;
        }
        this.sort();
        this.lastShotDropdown = false;
      }
    },
    showPartChart(part, mold) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$parent.$children, "chart-part");
      if (child != null) {
        child.showChartPartMold(part, mold, "QUANTITY", "DAY");
      }
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "mold.equipmentCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "mold.location.company.name",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["previous_location"],
          field: "previousLocation",
          default: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["current_location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "mold.location.name",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["relocation_date"],
          field: "updatedAt",
          default: true,
          sortable: true,
          sortField: "updatedAt",
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["checked_date"],
          field: "confirmedAtShow",
          default: true,
          sortable: true,
          sortField: "confirmedAt",
          hiddenInToggle: true,
        },
        // { label: this.resources['op'], field: 'operatingStatus', default: true, sortable: true, sortField: 'mold.operatingStatus', defaultSelected: true, defaultPosition: 5 },
        // { label: this.resources['status'], field: 'moldLocationStatus', default: true, sortable: true, sortField: 'moldLocationStatus', defaultSelected: true, defaultPosition: 6 },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          sortField: "toolingStatus",
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          sortField: "sensorStatus",
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["status"],
          field: "moldLocationStatus",
          sortable: true,
          sortField: "moldLocationStatus",
          defaultSelected: true,
          defaultPosition: 7,
        },
        {
          label: this.resources["approved_cycle_time"],
          field: "mold.contractedCycleTimeSeconds",
          sortable: true,
          sortField: "mold.contractedCycleTime",
        },
        {
          label: this.resources["counter_id"],
          field: "mold.counterCode",
          sortable: true,
          sortField: "mold.counter.equipmentCode",
        },
        {
          label: this.resources["latest_cycle_time"],
          field: "mold.cycleTime",
          sortable: true,
          sortField: "mold.lastCycleTime",
        },
        {
          label: this.resources["accumulated_shots"],
          field: "lastShot",
          sortable: true,
          sortField: "mold.lastShot",
        },
        {
          label: this.resources["cycle_time_tolerance_l1"],
          field: "mold.cycleTimeToleranceL1",
          sortable: true,
          sortField: "mold.cycleTimeLimit1",
        },
        {
          label: this.resources["cycle_time_tolerance_l2"],
          field: "mold.cycleTimeToleranceL2",
          sortable: true,
          sortField: "mold.cycleTimeLimit2",
        },
        {
          label: this.resources["weighted_average_cycle_time"],
          field: "mold.weightedAverageCycleTime",
          sortable: true,
          sortField: "mold.weightedAverageCycleTime",
        },
        {
          label: this.resources["engineer_in_charge"],
          field: "mold.engineerInCharge",
          sortable: true,
          sortField: "mold.engineer",
        },
        {
          label: this.resources["hot_runner_number_of_drop"],
          field: "mold.hotRunnerDrop",
          sortable: true,
          sortField: "mold.hotRunnerDrop",
        },
        {
          label: this.resources["hot_runner_zone"],
          field: "mold.hotRunnerZone",
          sortable: true,
          sortField: "mold.hotRunnerZone",
        },
        {
          label: this.resources["front_injection"],
          field: "mold.injectionMachineId",
          sortable: true,
          sortField: "mold.injectionMachineId",
        },
        {
          label: this.resources["required_labor"],
          field: "mold.labour",
          sortable: true,
          sortField: "mold.labour",
        },
        {
          label: this.resources["last_date_of_shots"],
          field: "mold.lastShotDate",
          sortable: true,
          sortField: "mold.lastShotAt",
        },
        // {label: this.resources['location'], field: 'location'},
        {
          label: this.resources["machine_ton"],
          field: "mold.quotedMachineTonnage",
          sortable: true,
          sortField: "mold.quotedMachineTonnage",
        },
        {
          label: this.resources["maintenance_interval"],
          field: "mold.preventCycle",
          sortable: true,
          sortField: "mold.preventCycle",
        },
        {
          label: this.resources["maker_of_runner_system"],
          field: "mold.runnerMaker",
          sortable: true,
          sortField: "mold.runnerMaker",
        },
        {
          label: this.resources["maximum_capacity_per_week"],
          field: "mold.maxCapacityPerWeek",
          sortable: true,
          sortField: "mold.maxCapacityPerWeek",
        },
        // { label: this.resources['overdue_maintenance_tolerance'], field: 'mold.preventOverdue', sortable: true, sortField: 'mold.preventOverdue' },
        { label: this.resources["parts"], field: "part", sortable: true },
        {
          label: this.resources["production_day_per_week"],
          field: "mold.productionDays",
          sortable: true,
          sortField: "mold.productionDays",
        },
        {
          label: this.resources["production_hour_per_day"],
          field: "mold.shiftsPerDay",
          sortable: true,
          sortField: "mold.shiftsPerDay",
        },
        {
          label: this.resources["shot_weight"],
          field: "mold.shotSize",
          sortable: true,
          sortField: "mold.shotSize",
        },
        {
          label: this.resources["supplier"],
          field: "mold.supplierCompanyName",
          sortable: true,
          sortField: "mold.supplier.name",
        },
        {
          label: this.resources["tool_description"],
          field: "mold.toolDescription",
          sortable: true,
          sortField: "mold.toolDescription",
        },
        {
          label: this.resources["front_tool_size"],
          field: "mold.toolingSizeView",
          sortable: true,
          sortField: "mold.size",
        },
        {
          label: this.resources["tool_weight"],
          field: "mold.toolingWeightView",
          sortable: true,
          sortField: "mold.weight",
        },
        {
          label: this.resources["tooling_complexity"],
          field: "mold.toolingComplexity",
          sortable: true,
          sortField: "mold.toolingComplexity",
        },
        {
          label: this.resources["tooling_letter"],
          field: "mold.toolingLetter",
          sortable: true,
          sortField: "mold.toolingLetter",
        },
        {
          label: this.resources["tooling_type"],
          field: "mold.toolingType",
          sortable: true,
          sortField: "mold.toolingType",
        },
        {
          label: this.resources["toolmaker"],
          field: "mold.toolMakerCompanyName",
          sortable: true,
          sortField: "mold.toolMaker.name",
        },
        {
          label: this.resources["type_of_runner_system"],
          field: "mold.runnerTypeTitle",
          sortable: true,
          sortField: "mold.runnerType",
        },
        {
          label: this.resources["upcoming_maintenance_tolerance"],
          field: "mold.preventUpcoming",
          sortable: true,
          sortField: "mold.preventUpcoming",
        },
        {
          label: this.resources["uptime_target"],
          field: "mold.uptimeTarget",
          sortable: true,
          sortField: "mold.uptimeTarget",
        },
        {
          label: this.resources["uptime_tolerance_l1"],
          field: "mold.uptimeLimitL1",
          sortable: true,
          sortField: "mold.uptimeLimitL1",
        },
        {
          label: this.resources["uptime_tolerance_l2"],
          field: "mold.uptimeLimitL2",
          sortable: true,
          sortField: "mold.uptimeLimitL2",
        },
        {
          label: this.resources["utilization_rate"],
          field: "mold.utilizationRate",
          sortable: true,
          sortField: "mold.utilizationRate",
        },
        {
          label: this.resources["weight_of_runner_system_o"],
          field: "weightOfRunnerSystem",
          sortable: true,
          sortField: "mold.weightRunner",
        },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "mold.machine.machineCode",
        },
      ];
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(this, "TOOLING", this.allColumnList, "mold");
        this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn("TOOLING", "mold")
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally((error) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    handleResetColumnsListSelected() {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    getColumnListSelected() {
      axios
        .get(`/api/config/column-config?pageType=${this.pageType}`)
        .then((response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            this.allColumnList.sort(function (a, b) {
              return a.position - b.position;
            });
            this.$forceUpdate();
            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        })
        .catch(function (error) {
          // Common.alert(error.response.data);
        });
    },
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
    },

    saveSelectedList(dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });
      this.allColumnList = dataFake;
      const self = this;
      let data = list.map((item, index) => {
        let response = {
          columnName: item.field,
          enabled: item.enabled,
          position: index,
        };
        if (item.id) {
          response.id = item.id;
        }
        return response;
      });
      axios
        .post("/api/config/update-column-config", {
          pageType: this.pageType,
          columnConfig: data,
        })
        .then((response) => {
          let hashedByColumnName = {};
          response.data.forEach((item) => {
            hashedByColumnName[item.columnName] = item;
          });
          self.allColumnList.forEach((item) => {
            if (hashedByColumnName[item.field] && !item.id) {
              item.id = hashedByColumnName[item.field].id;
              item.position = hashedByColumnName[item.field].position;
            }
          });
        })
        .finally(() => {
          self.allColumnList.sort(function (a, b) {
            return a.position - b.position;
          });
          self.$forceUpdate();
        });
    },

    ratio(mold) {
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

    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    hideCalendarPicker(event) {
      if (event.toElement && event.toElement.localName != "img") {
        this.changeShow(null);
      }
    },
    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },

    hasChecked: function () {
      let isExistedChecked = false;
      Object.keys(this.listCheckedTracked).forEach((status) => {
        Object.keys(this.listCheckedTracked[status]).forEach((page) => {
          if (this.listCheckedTracked[status][page].length > 0) {
            isExistedChecked = true;
          }
        });
      });
      return isExistedChecked;
    },

    check(e) {
      console.log("@check", {
        value: e.target.value,
        checked: e.target.checked,
      });
      const { checked, value } = e.target;
      const itemId = Number(value);
      if (checked) {
        this.listChecked.push(itemId);
        const foundIndex = this.results.findIndex((item) => item.id === itemId);
        this.listCheckedFull.push(this.results[foundIndex]);
        Common.setHeightActionBar();
      } else {
        this.listAllIds = [];
        this.listChecked = this.listChecked.filter((id) => id !== itemId);
        this.listCheckedFull = this.listCheckedFull.filter(
          (item) => item.id !== itemId
        );
        // TODO: recheck this.isAllTracked
        if (this.listChecked.length === 0) {
          this.isAllTracked = this.isAllTracked.filter(
            (item) =>
              item.page !== this.requestParam.page &&
              item.status !== this.requestParam.status
          );
        }
      }
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },

    closeShowDropDown() {
      this.showDropdown = false;
    },

    tab(status) {
      this.total = null;
      if (this.allColumnList) {
        this.allColumnList.forEach((c) => {
          if (c.field == "confirmedAtShow") {
            c.hiddenInToggle = status == "confirmed" ? false : true;
          }
        });
      }

      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.listChecked = [];
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    disable: function (user) {
      user.enabled = false;
      //this.save(user);
    },

    showChart: function (moldLocation, tab) {
      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(
          moldLocation.mold,
          "QUANTITY",
          "DAY",
          tab ? tab : "switch-graph"
        );
      }
    },
    showMoldDetails(mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },
    showLocationHistory(mold) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "location-history"
      );
      if (child != null) {
        console.log("child: ", child);
        child.showLocationHistory(mold, "Relocation History");
      }
    },
    showRelocationConfirmHistory(mold) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "relocation-confirm-history"
      );
      if (child != null) {
        console.log("child: ", child);
        child.show(mold);
      }
    },
    changeLocationStatus(status) {
      let payload = [];
      const record = {
        id: "",
        moldLocationStatus: status,
        message: "",
      };

      if (this.listAllIds.length === this.total) {
        // case change all
        console.log("@changeLocationStatus:case-all");
        payload = this.listAllIds.map((id) => ({ ...record, id }));
      } else if (this.listCheckedFull.length > 1) {
        console.log("@changeLocationStatus:case-multiple");
        payload = this.listCheckedFull.map((item) => ({
          ...record,
          id: item.id,
        }));
      } else {
        console.log("@changeLocationStatus:case-single");
        payload = this.listCheckedFull.map((item) => ({
          ...record,
          id: item.id,
          message: item?.location?.address
            ? `Tooling’s location is changed to ${item.location.address}`
            : "",
        }));
      }

      const modalConfig = {
        title: "Confirm",
        messageTitle: "Message",
        buttonTitle: "Correct",
        url: "/api/molds/locations/confirm",
        param: payload,
      };

      const messageFormComponent = this.$root.$refs["message-form"];
      console.log("messageFormComponent", messageFormComponent);
      this.$root.callbackMessageForm = this.callbackMessageForm.bind(this);
      messageFormComponent.showModal(modalConfig);
    },

    setListCategories: function () {
      for (var i = 0; i < this.results.length; i++) {
        var mold = this.results[i].mold;
        if (mold.part != null) {
          if (typeof mold.part.category === "object") {
            this.listCategories.push(mold.part.category);
          } else {
            var categoryId = mold.part.category;

            for (var j = 0; j < this.listCategories.length; j++) {
              var category = this.listCategories[j];

              if (categoryId == category.id) {
                mold.part.category = category;
                break;
              }
            }
          }
        }
      }
    },
    paging(pageNumber) {
      var self = this;
      Common.handleNoResults("#app", 1);
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      if (!this.listCheckedTracked[this.requestParam.status]) {
        this.listCheckedTracked[this.requestParam.status] = {};
      }
      if (
        !this.listCheckedTracked[this.requestParam.status][
          this.requestParam.page
        ]
      ) {
        this.listCheckedTracked[this.requestParam.status][
          this.requestParam.page
        ] = [];
      }

      var param = Common.param(self.requestParam);
      let url = "/api/molds/locations?lastAlert=true&" + param;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();
      self.isLoading = true;
      axios
        .get(url, { cancelToken: this.cancelToken.token })
        .then(function (response) {
          self.isLoading = false;

          // TODO: RECHECK RESET
          if (self.total === self.listAllIds.length) {
            // refresh checklist in case select all items
            self.listChecked = [];
            self.listCheckedFull = [];
          }

          self.total = response.data.totalElements;
          self.results = response.data.content.map((value) => {
            value.updatedDateTime = value.notificationAt
              ? self.formatToDateTime(value.notificationAt)
              : "";
            value.confirmedAtShow = value.confirmedAt
              ? self.formatToDateTime(value.confirmedAt)
              : "";
            return value;
          });
          self.pagination = Common.getPagingData(response.data);

          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          self.setResultObject();

          // 카테고리 정보 저장.
          //self.setListCategories();

          Common.handleNoResults("#app", self.results.length);

          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error.response);
          self.isLoading = false;
        });
    },

    setResultObject: function () {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i].mold !== "object") {
          var moldId = this.results[i].mold;
          this.results[i].mold = this.findMoldFromList(this.results, moldId);
        }
      }
    },

    findMoldFromList(results, moldId) {
      for (var j = 0; j < results.length; j++) {
        if (typeof results[j].mold !== "object") {
          continue;
        }

        var mold = results[j].mold;
        if (moldId == mold.id) {
          return mold;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findMold = this.findMold(mold.part.molds, moldId);
          if (typeof findMold === "object") {
            return findMold;
          }
        }
      }
    },

    findMold(results, moldId) {
      for (var j = 0; j < results.length; j++) {
        if (typeof results[j] !== "object") {
          continue;
        }

        var mold = results[j];
        if (moldId == mold.id) {
          return mold;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findMold = this.findMold(mold.part.molds, moldId);
          if (typeof findMold === "object") {
            return findMold;
          }
        }
      }
    },

    callbackMessageForm: function () {
      this.resetAfterUpdate();
      this.paging(1);
      if (this.$parent && typeof this.$parent.reloadAlertCout === "function") {
        this.$parent.reloadAlertCout();
      }
    },
    sortBy(type) {
      if (type === "mold.lastShot") {
        this.lastShotDropdown = true;
      } else {
        this.lastShotData.sort = "";
        if (this.currentSortType !== type) {
          this.currentSortType = type;
          this.isDesc = true;
        } else {
          this.isDesc = !this.isDesc;
        }
        if (type) {
          this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
          this.sort();
        }
      }
    },
    sort() {
      this.paging(1);
    },
    showSystemNoteModal(selectedItem) {
      const child = this.$root.$refs["system-note"];
      console.log("@showSystemNoteModal:selectedItem", child);
      if (child) {
        if (this.listAllIds.length === this.total) {
          console.log("select-all");
          child.showSystemNote(
            this.listAllIds.map((i) => i.mainObjectId),
            true
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          child.showSystemNote(
            this.listCheckedFull.map((i) => i.moldId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          child.showSystemNote({ id: this.listCheckedFull[0].moldId }, false);
        } else {
          console.log("single select in history tab");
          child.showSystemNote({ id: selectedItem.moldId });
        }
      } else {
        if (this.listAllIds.length === this.total) {
          console.log("select-all");
          this.showNote(
            this.listAllIds.map((i) => i.mainObjectId),
            true
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          this.showNote(
            this.listCheckedFull.map((i) => i.moldId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          this.showNote({ id: this.listCheckedFull[0].moldId }, false);
        } else {
          console.log("single select in history tab");
          this.showNote({ id: selectedItem.moldId });
        }
      }
    },

    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
    },

    convertTime(s) {
      const dtFormat = new Intl.DateTimeFormat("en-GB", {
        timeStyle: "medium",
        timeZone: "UTC",
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : "";
    },

    handleCycleTimeTolerance1: function (result) {
      if (!result.cycleTimeLimit1) {
        return "";
      } else {
        if (result.cycleTimeLimit1Unit === "SECOND") {
          return "s";
        }
        if (result.cycleTimeLimit1Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
    },

    handleCycleTimeTolerance2: function (result) {
      if (!result.cycleTimeLimit2) {
        return "";
      } else {
        if (result.cycleTimeLimit2Unit === "SECOND") {
          return "s";
        }
        if (result.cycleTimeLimit2Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
    },
    changeAlertGeneration(alertType, isInit, alertOn) {
      //todo: alertOn is false to no show list
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
    },
    resetAfterUpdate() {
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} toolings on this current page are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.requestParam.status,
        });
      }
      if (actionType === "all") {
        this.fetchAllListIds();
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.total} toolings are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.requestParam.status,
        });
      }
      if (actionType === "deselect") {
        this.listAllIds = [];
        this.listChecked = [];
        this.listCheckedFull = [];
        this.isAllTracked = this.isAllTracked.filter(
          (item) =>
            item.page !== this.requestParam.page &&
            item.status !== this.requestParam.status
        );
      }

      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
      console.log("@handleSelectAll", {
        actionType,
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
        listCheckedTracked: this.listCheckedTracked,
      });
    },
    async fetchAllListIds() {
      try {
        var param = Common.param({
          ...this.requestParam,
          pageType: this.pageType,
        });
        const response = await axios.get(
          "/api/batch/all-ids?lastAlert=true&" + param
        );
        console.log("response.data", response.data);
        this.listAllIds = response.data.data;
      } catch (error) {
        console.log(error);
      }
    },
    captureQuery() {
      const [hash, stringParams] = location.hash.slice("#".length).split("?");
      const params = Common.parseParams(stringParams);
      // TODO(ducnm2010): update to list after having api
      // if (params?.id) {
      //   this.requestParam.id = params.id;
      // }
      if (
        params?.moldLocationStatus &&
        params?.moldLocationStatus !== "pendingApproval"
      ) {
        this.requestParam.moldLocationStatus =
          params.moldLocationStatus.toUpperCase();
      }
      const targetTab = params?.tab || params?.status;
      if (targetTab) {
        this.tab(targetTab);
      } else {
        this.tab(this.requestParam.status);
      }
    },
  },
  created() {
    this.$watch(
      () => headerVm?.permissions,
      async () => {
        this.isBtnAlertConfirmPermitted = await Common.isMenuPermitted(
          Common.PERMISSION_CODE.ALERT_ASSETS,
          Common.PERMISSION_CODE.ALERT_CENTER_RELOCATION,
          "btnAlertConfirm"
        );
      },
      { immediate: true }
    );
    this.captureQuery();
    window.addEventListener("popstate", this.captureQuery);
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>
