<template>
  <div>
    <notify-alert
      type="CYCLE_TIME"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>

    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['cycle_time']"></strong>

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
              <div class="mb-3 mb-md-0 col-12">
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
    </div>

    <div class="row">
      <div class="col-lg-12">
        <div class="card wave1">
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span v-text="resources['striped_tbl']"></span>
          </div>
          <div style="overflow-x: unset !important" class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li
                class="nav-item"
                v-for="(tabItem, index) in tabConfig"
                :key="index"
              >
                <a
                  class="nav-link customize-tab"
                  :class="{
                    active: tabItem.key === activeTab,
                  }"
                  href="#"
                  @click.prevent="tab(tabItem)"
                >
                  <span v-text="resources[tabItem.key]"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill">{{
                    tabs.find((item) => item.tabName === tabItem.name)
                      ?.totalElements || 0
                  }}</span>
                </a>
              </li>
              <!--                            <show-columns v-show="alertOn" style="position: absolute; right: 20px;" @reset-columns-list="handleResetColumnsListSelected" @change-value-checkbox="handleChangeValueCheckBox" :all-columns-list="allColumnList" v-if="Object.entries(resources).length" :resources="resources"></show-columns>-->
              <customization-modal
                v-show="alertOn"
                style="position: absolute; right: 20px"
                :all-columns-list="
                  requestParam.status !== 'confirmed'
                    ? allColumnList
                    : allHistoryColumnList
                "
                @save="saveSelectedList"
                :resources="resources"
              ></customization-modal>
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
                    <th
                      v-if="requestParam.status !== 'confirmed'"
                      style="width: 40px; vertical-align: middle !important"
                    >
                      <select-all
                        :resources="resources"
                        :show="allColumnSelected.length > 0"
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
                      v-for="(column, index) in allColumnSelected"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <template
                        v-if="
                          [
                            'contractedCycleTime',
                            'cycleTime',
                            'variance',
                          ].includes(column.field)
                        "
                      >
                        <th
                          v-if="requestParam.status !== 'confirmed'"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            column.label === 'OP' || column.label === 'Status'
                              ? ''
                              : 'text-left',
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
                      <template v-else-if="column.field === 'confirmedAt'">
                        <th
                          v-if="requestParam.status === 'confirmed'"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            column.label === 'OP' ||
                            column.label === 'Status' ||
                            column.label === 'Action'
                              ? ''
                              : 'text-left',
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
                        :class="[
                          {
                            __sort: column.sortable,
                            active: currentSortType === column.sortField,
                          },
                          isDesc ? 'desc' : 'asc',
                          column.label === 'OP' ||
                          column.label === 'Status' ||
                          column.label === 'Action'
                            ? ''
                            : 'text-left',
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
                          },
                          isDesc ? 'desc' : 'asc',
                          column.label === 'OP' ||
                          column.label === 'Status' ||
                          column.label === 'Action'
                            ? ''
                            : 'text-left',
                        ]"
                        @click="sortBy(column.sortField)"
                      >
                        <span>{{ column.label }}</span>
                        <span v-if="column.sortable" class="__icon-sort"></span>
                      </th>
                    </template>
                    <th
                      class="text-center"
                      v-if="requestParam.status === 'confirmed'"
                    >
                      <span v-text="resources['action']"></span>
                    </th>
                  </tr>

                  <tr
                    :class="{
                      'd-none zindexNegative': listChecked.length == 0,
                    }"
                    id="action-bar"
                    class="tr-sort empty-tr"
                  >
                    <th class="empty-th">
                      <div
                        class="first-checkbox-zone2"
                        style="left: unset !important"
                      >
                        <div
                          class="first-checkbox2"
                          style="
                            display: flex;
                            align-items: center;
                            padding-left: 0;
                            width: 110px;
                          "
                        >
                          <div>
                            <select-all
                              :resources="resources"
                              :show="allColumnSelected.length > 0"
                              :checked="isAll"
                              :total="total"
                              :count="results.length"
                              :unit="['tooling', 'toolings']"
                              @select-all="handleSelectAll('all')"
                              @select-page="handleSelectAll('page')"
                              @deselect="handleSelectAll('deselect')"
                            ></select-all>
                          </div>
                        </div>
                        <div class="action-bar">
                          <div
                            v-if="requestParam.status != 'DELETED'"
                            class="action-item"
                            v-click-outside="closeShowDropDown"
                          >
                            <div
                              class="change-status-dropdown drowdown d-inline"
                            >
                              <div
                                title="Change status"
                                class="change-status"
                                @click="showDropdown = !showDropdown"
                              >
                                <span
                                  v-text="resources['change_status']"
                                ></span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul
                                class="dropdown-menu"
                                :class="[showDropdown ? 'show' : '']"
                              >
                                <li
                                  v-if="
                                    (requestParam.status === 'alert' &&
                                      isBtnAlertConfirmPermitted) ||
                                    (requestParam.status === 'L1' &&
                                      isBtnOutsideL1ConfirmPermitted) ||
                                    (requestParam.status === 'L2' &&
                                      isBtnOutsideL2ConfirmPermitted)
                                  "
                                >
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="changeStatus('CONFIRMED')"
                                    >Confirm Alert</a
                                  >
                                </li>
                                <li v-else class="dropdown-header">
                                  <span class="op-change-status">
                                    No option is available
                                  </span>
                                </li>
                              </ul>
                            </div>
                          </div>

                          <div
                            v-if="requestParam.status != 'DELETED'"
                            class="action-item"
                            @click="showSystemNoteModal()"
                          >
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>

                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            @click="
                              showCreateWorkorder({
                                ...listCheckedFull[0],
                                id: listCheckedFull[0].moldId,
                                mold: {
                                  equipmentCode: listCheckedFull[0].moldCode,
                                  locationCode: listCheckedFull[0].locationCode,
                                  locationName: listCheckedFull[0].locationName,
                                  locationId: listCheckedFull[0].locationId,
                                },
                              })
                            "
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
                  <template v-if="!isLoading">
                    <tr v-for="(result, index, id) in results" :id="result.id">
                      <td v-if="requestParam.status !== 'confirmed'">
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
                        v-for="(column, columnIndex) in allColumnSelected"
                        v-if="column.enabled && !column.hiddenInToggle"
                      >
                        <td
                          v-if="column.field === 'toolingId'"
                          class="text-left"
                        >
                          <a
                            href="#"
                            @click.prevent="showChart({ id: result.moldId })"
                            style="color: #1aaa55"
                            class="mr-1"
                            ><i class="fa fa-bar-chart"></i
                          ></a>
                          <a
                            href="#"
                            @click.prevent="
                              showMoldDetails({ id: result.moldId })
                            "
                          >
                            {{ result.moldCode }}
                          </a>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'company'"
                        >
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="
                                showCompanyDetailsById(result.companyId)
                              "
                            >
                              {{ result.companyName }}
                            </a>
                          </div>
                          <div class="small text-muted font-size-11-2">
                            {{ result.companyCode }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'location'"
                        >
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="
                                showLocationHistory({
                                  id: result.moldId,
                                  equipmentCode: result.moldCode,
                                })
                              "
                              >{{ result.locationName }}</a
                            >
                          </div>
                          <span class="small text-muted font-size-11-2">{{
                            result.locationCode
                          }}</span>
                        </td>
                        <td
                          style="vertical-align: middle !important"
                          class="text-left"
                          v-else-if="column.field === 'cycleTimeStatus'"
                        >
                          <span
                            class="label label-warning"
                            v-if="result.cycleTimeStatus === 'OUTSIDE_L1'"
                            v-text="resources['outside_l1']"
                          ></span>
                          <span
                            class="label label-danger"
                            v-if="result.cycleTimeStatus === 'OUTSIDE_L2'"
                            v-text="resources['outside_l2']"
                          ></span>
                        </td>
                        <template
                          v-else-if="column.field === 'contractedCycleTime'"
                        >
                          <td
                            class="text-left"
                            v-if="requestParam.status !== 'confirmed'"
                          >
                            {{
                              Number(result.contractedCycleTime / 10).toFixed(2)
                            }}
                            <div>
                              <span class="font-size-11-2">s</span>
                            </div>
                          </td>
                        </template>
                        <template v-else-if="column.field === 'cycleTime'">
                          <td
                            class="text-left"
                            v-if="requestParam.status !== 'confirmed'"
                          >
                            <div class="font-size-14">
                              <a
                                href="#"
                                @click.prevent="
                                  showCycleTime({
                                    id: result.moldId,
                                    equipmentCode: result.moldCode,
                                  })
                                "
                              >
                                {{ Number(result.cycleTime / 10).toFixed(2) }}
                              </a>
                            </div>
                            <div>
                              <span class="font-size-11-2">s</span>
                            </div>
                          </td>
                        </template>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.cycleTime'"
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
                        >
                          <span class="font-size-14">
                            {{ result.mold.weightedAverageCycleTime / 10 }}
                          </span>
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <template v-else-if="column.field === 'variance'">
                          <td
                            class="text-left"
                            v-if="requestParam.status !== 'confirmed'"
                          >
                            {{ result.variance.toFixed(2) }}
                            <div>
                              <span class="font-size-11-2">%</span>
                            </div>
                          </td>
                        </template>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'notificationAt'"
                        >
                          <span>
                            {{ convertDate(result.creationDateTime) }}
                          </span>
                          <div>
                            <span class="font-size-11-2">
                              {{ convertTimeString(result.creationDateTime) }}
                            </span>
                          </div>
                        </td>
                        <template v-else-if="column.field === 'confirmedAt'">
                          <td
                            class="text-left"
                            v-if="requestParam.status === 'confirmed'"
                          >
                            {{ result.confirmedAtShow }}
                          </td>
                        </template>
                        <td
                          style="vertical-align: middle !important"
                          class="text-center op-status-td"
                          v-else-if="column.field === 'operatingStatus'"
                        >
                          <span
                            v-if="result.operatingStatus == 'WORKING'"
                            class="op-active label label-success"
                          ></span>
                          <span
                            v-if="result.operatingStatus == 'IDLE'"
                            class="op-active label label-warning"
                          ></span>
                          <span
                            v-if="result.operatingStatus == 'NOT_WORKING'"
                            class="op-active label label-inactive"
                          ></span>
                          <span
                            v-if="result.operatingStatus == 'DISCONNECTED'"
                            class="op-active label label-danger"
                          ></span>
                        </td>
                        <template
                          v-else-if="column.field === 'notificationStatus'"
                        >
                          <td
                            style="vertical-align: middle !important"
                            class="text-center op-status-td"
                          >
                            <span
                              class="label label-danger"
                              v-if="result.alertStatus === 'ALERT'"
                              v-text="resources['alert']"
                            ></span>
                          </td>
                        </template>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'lastShot' ||
                            column.field === 'mold.lastShot'
                          "
                        >
                          {{ formatNumber(result.mold.accumulatedShot) }}
                          <!-- <div>
                            <span class="font-size-11-2">shots</span>
                        </div> -->
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.utilizationRate'"
                        >
                          <div class="clearfix">
                            <div class="float-left font-size-14">
                              <strong>{{
                                formatNumber(result.mold.lastShot)
                              }}</strong>
                              <span
                                >/
                                {{
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
                        >
                          <mold-parts-dropdown-view
                            :parts="result.mold.parts"
                            :mold="result.mold"
                            :index="index"
                            :show-part-chart="showPartChart"
                          ></mold-parts-dropdown-view>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.cycleTime'"
                        >
                          {{ result.mold.lastCycleTime / 10 }}
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.cycleTimeToleranceL1'
                          "
                        >
                          {{ result.mold.cycleTimeLimit1 }}
                          <div>
                            <span class="font-size-11-2">{{
                              handleCycleTimeTolerance1(result.mold)
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.cycleTimeToleranceL2'
                          "
                        >
                          {{ result.mold.cycleTimeLimit2 }}
                          <div>
                            <span class="font-size-11-2">{{
                              handleCycleTimeTolerance2(result.mold)
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.uptimeTarget'"
                        >
                          {{ result.mold.uptimeTarget }}
                          <div>
                            <span class="font-size-11-2">
                              {{ result.mold.uptimeTarget ? "%" : "" }}</span
                            >
                          </div>
                        </td>
                        <td
                          style="vertical-align: middle !important"
                          class="text-left"
                          v-else-if="column.field === 'mold.engineerInCharge'"
                        >
                          <user-list-cell
                            :resources="resources"
                            :user-list="result.mold.engineers"
                          ></user-list-cell>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'weightOfRunnerSystem'"
                        >
                          {{
                            result.mold && result.mold.weightRunner
                              ? result.mold.weightRunner
                              : ""
                          }}
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold && result.mold.weightRunner
                                ? result.mold.weightUnitTitle
                                : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.contractedCycleTimeSeconds'
                          "
                        >
                          <span class="font-size-14">{{
                            result.mold.contractedCycleTimeSeconds
                          }}</span>
                          <div>
                            <span class="font-size-11-2">s</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="
                            column.field === 'mold.quotedMachineTonnage'
                          "
                        >
                          <span class="font-size-14">{{
                            result.mold.quotedMachineTonnage
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold && result.mold.quotedMachineTonnage
                                ? "tonnes"
                                : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.preventCycle'"
                        >
                          <span class="font-size-14">{{
                            result.mold.preventCycle
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.preventCycle ? "shots" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.maxCapacityPerWeek'"
                        >
                          <span class="font-size-14">{{
                            result.mold.maxCapacityPerWeek
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.maxCapacityPerWeek ? "shots" : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.preventOverdue'"
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
                        >
                          <span class="font-size-14">{{
                            result.mold.size
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.size
                                ? getUnitName(result.mold.sizeUnit)
                                : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.toolingWeightView'"
                        >
                          <span class="font-size-14">{{
                            result.mold.weight
                          }}</span>
                          <div>
                            <span class="font-size-11-2">{{
                              result.mold.weight
                                ? getUnitName(result.mold.weightUnit)
                                : ""
                            }}</span>
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'mold.uptimeLimitL1'"
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
                          class="text-left tooling-status"
                          v-else-if="column.field === 'toolingStatus'"
                        >
                          <mold-status
                            :mold="{
                              ...result,
                              moldStatus: result.toolingStatus,
                            }"
                            :resources="resources"
                            :rule-op-status="ruleOpStatus"
                          />
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'sensorStatus'"
                        >
                          <counter-status
                            :mold="{
                              ...result,
                              counterStatus: result.sensorStatus,
                            }"
                            :resources="resources"
                          />
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field && column.field.includes('.')"
                        >
                          {{
                            valueWithKeyMultiPath(
                              result,
                              column.field,
                              column.isCustomField
                            )
                          }}
                        </td>
                        <td class="text-left" v-else>
                          {{ result[column.field] }}
                        </td>
                      </template>
                      <td
                        style="vertical-align: middle !important"
                        class="text-center op-status-td"
                        v-if="requestParam.status === 'confirmed'"
                      >
                        <a
                          style="cursor: pointer"
                          href="#"
                          title="View"
                          @click.prevent="
                            showCycleTimeConfirmHistory({
                              id: result.moldId,
                              equipmentCode: result.moldCode,
                            })
                          "
                          ><span
                            class="label"
                            v-if="result.alertStatus == 'CONFIRMED'"
                            ><img
                              height="18"
                              src="/images/icon/view.png"
                              alt="View" /></span
                        ></a>
                        <a
                          v-if="requestParam.status == 'confirmed'"
                          href="javascript:void(0)"
                          @click="showSystemNoteModal(result)"
                          title="Add note"
                          class="table-action-link note"
                        >
                          <img src="/images/icon/note.svg" alt="Add note" />
                        </a>
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>

              <div
                class="no-results d-none"
                v-text="resources['no_results']"
              ></div>
              <div class="row">
                <div class="col-md-10 col-sm-10 col-xs-10">
                  <ul class="pagination">
                    <li
                      v-for="(data, index) in pagination"
                      class="page-item"
                      :class="{ active: data.isActive }"
                    >
                      <a class="page-link" @click="paging(data.pageNumber)">{{
                        data.text
                      }}</a>
                    </li>
                  </ul>
                </div>
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

    <cycle-time-details
      :resources="resources"
      :alert-type="alertType"
    ></cycle-time-details>
    <cycle-time-confirm-history
      :resources="resources"
      :alert-type="alertType"
    ></cycle-time-confirm-history>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
    <message-form
      ref="message-form"
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></message-form>
  </div>
</template>
<script>
const ALERT_CYCLE_TIME_API_BASE = "/api/production/alr-cyc-tim";

const TAB_CONFIG = [
  {
    name: "Alert",
    key: "alert",
    status: "alert",
  },
  {
    name: "Outside L1",
    key: "outside_l1",
    status: "L1",
  },
  {
    name: "Outside L2",
    key: "outside_l2",
    status: "L2",
  },
  {
    name: "History Log",
    key: "history_log",
    status: "confirmed",
  },
];
module.exports = {
  components: {
    "cycle-time-details": httpVueLoader("/components/cycle-time-alerts.vue"),
    "cycle-time-confirm-history": httpVueLoader(
      "/components/cycle-time-confirm-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
  },
  props: {
    resources: Object,
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
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      showDrowdown: null,
      isLoading: false,
      isAllTracked: [],
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        filterCode: "COMMON",
        query: "",
        status: "alert",
        sort: "id,desc",
        page: 1,
        size: 20,
      },
      statusCodes: [],
      listCategories: [],
      sortType: {
        ID: "mold.equipmentCode",
        COMPANY: "companyName",
        LOCATION: "locationName",
        CT: "cycleTimeStatus",
        FINAL_APPROVED: "contractedCycleTime",
        CYCLE_TIME: "cycleTime",
        OP: "operatingStatus",
        VARIANCE: "variance",
        DATE: "creationDateTime",
        STATUS: "alertStatus",
        CONFIRM_DATE: "confirmedAt",
      },
      pageType: "CYCLE_TIME_ALERT",
      pageTypeHistory: "CYCLE_TIME_ALERT_HISTORY",
      allColumnList: [],
      allHistoryColumnList: [],
      currentSortType: "id",
      isDesc: true,
      alertType: "DAILY",
      alertTypeOptions: {
        DAILY: "DAILY",
        WEEKLY: "WEEKLY",
        MONTHLY: "MONTHLY",
      },
      alertOn: true,
      listCheckedFull: [],
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      listAllIds: [],
      isBtnAlertConfirmPermitted: false,
      isBtnOutsideL1ConfirmPermitted: false,
      isBtnOutsideL2ConfirmPermitted: false,
      tabConfig: TAB_CONFIG,
      activeTab: TAB_CONFIG[0].key,
      tabs: [],
      selectType: "",
      systemNoteFunction: "",
      idNoteBatchAction: "",
    };
  },
  computed: {
    allColumnSelected() {
      return this.requestParam.status === "confirmed"
        ? this.allHistoryColumnList
        : this.allColumnList;
    },
    isAll() {
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
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          this.setAllColumnList();
        }
      },
      immediate: true,
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
    listAllIds(newVal) {
      console.log("@watch:listAllIds", newVal);
    },
  },
  created() {
    this.$watch(
      () => headerVm?.permissions,
      async () => {
        [
          this.isBtnAlertConfirmPermitted,
          this.isBtnOutsideL1ConfirmPermitted,
          this.isBtnOutsideL2ConfirmPermitted,
        ] = await Promise.all([
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_CYCLE_TIME,
            "btnAlertConfirm"
          ),
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_CYCLE_TIME,
            "btnOutsideL1Confirm"
          ),
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_CYCLE_TIME,
            "btnOutsideL2Confirm"
          ),
        ]);
      },
      { immediate: true }
    );
    this.captureQuery();
    window.addEventListener("popstate", this.captureQuery);
  },
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
    if (this.searchText) {
      this.requestParam.query = this.searchText;
    }
  },
  methods: {
    showMachineDetails(machineId) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "machine-details"
      );
      if (child != null) {
        child.showDetailsById(machineId);
      }
    },
    updateVariable(data) {
      this.lastShotData = data;
      // this.isDesc = data.sort === 'desc'
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
          sortField: "moldCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "companyName",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "locationName",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["ct_status"],
          field: "cycleTimeStatus",
          default: true,
          sortable: true,
          sortField: "cycleTimeStatus",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["final_approved_ct"],
          field: "contractedCycleTime",
          default: true,
          sortable: true,
          sortField: "contractedCycleTime",
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["cycle_time"],
          field: "cycleTime",
          default: true,
          sortable: true,
          sortField: "cycleTime",
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["variance"],
          field: "variance",
          default: true,
          sortable: true,
          sortField: "variance",
        },
        {
          label: this.resources["alert_date"],
          field: "notificationAt",
          default: true,
          sortable: true,
          sortField: "creationDateTime",
        },
        // { label: this.resources['confirmation_date'], field: 'confirmedAt', default: true, sortable: true, sortField: 'confirmedAt', hiddenInToggle: true },
        // { label: this.resources['op'], field: 'operatingStatus', default: true, sortable: true, sortField: 'mold.operatingStatus' },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "toolingStatus",
          defaultPosition: 6,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "sensorStatus",
          defaultPosition: 7,
        },
        // // { label: this.resources['status'], field: 'notificationStatus', default: true, sortable: true, sortField: 'notificationStatus' },
        // {
        //   label: this.resources["counter_id"],
        //   field: "mold.counterCode",
        //   sortable: true,
        //   sortField: "mold.counter.equipmentCode",
        // },
        // {
        //   label: this.resources["latest_cycle_time"],
        //   field: "mold.cycleTime",
        //   sortable: true,
        //   sortField: "mold.lastCycleTime",
        // },
        // {
        //   label: this.resources["cycle_time_tolerance_l1"],
        //   field: "mold.cycleTimeToleranceL1",
        //   sortable: true,
        //   sortField: "mold.cycleTimeLimit1",
        // },
        // {
        //   label: this.resources["cycle_time_tolerance_l2"],
        //   field: "mold.cycleTimeToleranceL2",
        //   sortable: true,
        //   sortField: "mold.cycleTimeLimit2",
        // },
        // {
        //   label: this.resources["weighted_average_cycle_time"],
        //   field: "mold.weightedAverageCycleTime",
        //   sortable: true,
        //   sortField: "mold.weightedAverageCycleTime",
        // },

        // {
        //   label: this.resources["engineer_in_charge"],
        //   field: "mold.engineerInCharge",
        //   sortable: true,
        //   sortField: "mold.engineer",
        // },

        // {
        //   label: this.resources["forecasted_max_shots"],
        //   field: "mold.designedShot",
        //   sortable: true,
        //   sortField: "mold.designedShot",
        // },

        // {
        //   label: this.resources["hot_runner_number_of_drop"],
        //   field: "mold.hotRunnerDrop",
        //   sortable: true,
        //   sortField: "mold.hotRunnerDrop",
        // },
        // {
        //   label: this.resources["hot_runner_zone"],
        //   field: "mold.hotRunnerZone",
        //   sortable: true,
        //   sortField: "mold.hotRunnerZone",
        // },
        // {
        //   label: this.resources["front_injection"],
        //   field: "mold.injectionMachineId",
        //   sortable: true,
        //   sortField: "mold.injectionMachineId",
        // },
        // {
        //   label: this.resources["required_labor"],
        //   field: "mold.labour",
        //   sortable: true,
        //   sortField: "mold.labour",
        // },
        // {
        //   label: this.resources["last_date_of_shots"],
        //   field: "mold.lastShotDate",
        //   sortable: true,
        //   sortField: "mold.lastShotAt",
        // },

        // {
        //   label: this.resources["accumulated_shots"],
        //   field: "lastShot",
        //   sortable: true,
        //   sortField: "mold.lastShot",
        // },

        // {
        //   label: this.resources["machine_ton"],
        //   field: "mold.quotedMachineTonnage",
        //   sortable: true,
        //   sortField: "mold.quotedMachineTonnage",
        // },
        // {
        //   label: this.resources["maintenance_interval"],
        //   field: "mold.preventCycle",
        //   sortable: true,
        //   sortField: "mold.preventCycle",
        // },
        // {
        //   label: this.resources["maker_of_runner_system"],
        //   field: "mold.runnerMaker",
        //   sortable: true,
        //   sortField: "mold.runnerMaker",
        // },
        // {
        //   label: this.resources["maximum_capacity_per_week"],
        //   field: "mold.maxCapacityPerWeek",
        //   sortable: true,
        //   sortField: "mold.maxCapacityPerWeek",
        // },
        // // { label: this.resources['overdue_maintenance_tolerance'], field: 'mold.preventOverdue', sortable: true, sortField: 'mold.preventOverdue' },

        // { label: this.resources["parts"], field: "part", sortable: true },

        // {
        //   label: this.resources["production_day_per_week"],
        //   field: "mold.productionDays",
        //   sortable: true,
        //   sortField: "mold.productionDays",
        // },
        // {
        //   label: this.resources["production_hour_per_day"],
        //   field: "mold.shiftsPerDay",
        //   sortable: true,
        //   sortField: "mold.shiftsPerDay",
        // },
        // {
        //   label: this.resources["shot_weight"],
        //   field: "mold.shotSize",
        //   sortable: true,
        //   sortField: "mold.shotSize",
        // },
        // {
        //   label: this.resources["supplier"],
        //   field: "mold.supplierCompanyName",
        //   sortable: true,
        //   sortField: "mold.supplier.name",
        // },
        // {
        //   label: this.resources["tool_description"],
        //   field: "mold.toolDescription",
        //   sortable: true,
        //   sortField: "mold.toolDescription",
        // },
        // {
        //   label: this.resources["front_tool_size"],
        //   field: "mold.toolingSizeView",
        //   sortable: true,
        //   sortField: "mold.size",
        // },
        // {
        //   label: this.resources["tool_weight"],
        //   field: "mold.toolingWeightView",
        //   sortable: true,
        //   sortField: "mold.weight",
        // },
        // {
        //   label: this.resources["tooling_complexity"],
        //   field: "mold.toolingComplexity",
        //   sortable: true,
        //   sortField: "mold.toolingComplexity",
        // },
        // {
        //   label: this.resources["tooling_letter"],
        //   field: "mold.toolingLetter",
        //   sortable: true,
        //   sortField: "mold.toolingLetter",
        // },
        // {
        //   label: this.resources["tooling_type"],
        //   field: "mold.toolingType",
        //   sortable: true,
        //   sortField: "mold.toolingType",
        // },
        // {
        //   label: this.resources["toolmaker"],
        //   field: "mold.toolMakerCompanyName",
        //   sortable: true,
        //   sortField: "mold.toolMaker.name",
        // },
        // {
        //   label: this.resources["type_of_runner_system"],
        //   field: "mold.runnerTypeTitle",
        //   sortable: true,
        //   sortField: "mold.runnerType",
        // },
        // {
        //   label: this.resources["upcoming_maintenance_tolerance"],
        //   field: "mold.preventUpcoming",
        //   sortable: true,
        //   sortField: "mold.preventUpcoming",
        // },
        // {
        //   label: this.resources["uptime_target"],
        //   field: "mold.uptimeTarget",
        //   sortable: true,
        //   sortField: "mold.uptimeTarget",
        // },
        // {
        //   label: this.resources["uptime_tolerance_l1"],
        //   field: "mold.uptimeLimitL1",
        //   sortable: true,
        //   sortField: "mold.uptimeLimitL1",
        // },
        // {
        //   label: this.resources["uptime_tolerance_l2"],
        //   field: "mold.uptimeLimitL2",
        //   sortable: true,
        //   sortField: "mold.uptimeLimitL2",
        // },
        // {
        //   label: this.resources["utilization_rate"],
        //   field: "mold.utilizationRate",
        //   sortable: true,
        //   sortField: "mold.utilizationRate",
        // },
        // {
        //   label: this.resources["weight_of_runner_system_o"],
        //   field: "weightOfRunnerSystem",
        //   sortable: true,
        //   sortField: "mold.weightRunner",
        // },
        // {
        //   label: this.resources["year_of_tool_made"],
        //   field: "mold.madeYear",
        //   sortable: true,
        //   sortField: "mold.madeYear",
        // },
        // {
        //   label: this.resources["machine_id"],
        //   field: "machineCode",
        //   sortable: true,
        //   sortField: "mold.machine.machineCode",
        // },
      ];

      this.allHistoryColumnList = [
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "moldCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "companyName",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "locationName",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["ct_status"],
          field: "cycleTimeStatus",
          default: true,
          sortable: true,
          sortField: "cycleTimeStatus",
          defaultSelected: true,
          defaultPosition: 3,
        },
        // { label: this.resources['final_approved_ct'], field: 'contractedCycleTime', default: true, sortable: true, sortField: 'contractedCycleTime', defaultSelected: true, defaultPosition: 4 },
        // { label: this.resources['cycle_time'], field: 'cycleTime', default: true, sortable: true, sortField: 'cycleTime', defaultSelected: true, defaultPosition: 5 },
        // { label: this.resources['variance'], field: 'variance', default: true, sortable: true, sortField: 'variance' },
        {
          label: this.resources["alert_date"],
          field: "notificationAt",
          default: true,
          sortable: true,
          sortField: "creationDateTime",
        },
        {
          label: this.resources["confirmation_date"],
          field: "confirmedAt",
          default: true,
          sortable: true,
          sortField: "confirmedAt",
          hiddenInToggle: true,
        },
        {
          label: this.resources["op"],
          field: "operatingStatus",
          default: true,
          sortable: true,
          sortField: "operatingStatus",
        },
        {
          label: this.resources["status"],
          field: "notificationStatus",
          default: true,
          sortable: true,
          sortField: "alertStatus",
        },
        // {
        //   label: this.resources["counter_id"],
        //   field: "mold.counterCode",
        //   sortable: true,
        //   sortField: "mold.counter.equipmentCode",
        // },
        // {
        //   label: this.resources["latest_cycle_time"],
        //   field: "mold.cycleTime",
        //   sortable: true,
        //   sortField: "mold.lastCycleTime",
        // },
        // {
        //   label: this.resources["cycle_time_tolerance_l1"],
        //   field: "mold.cycleTimeToleranceL1",
        //   sortable: true,
        //   sortField: "mold.cycleTimeLimit1",
        // },
        // {
        //   label: this.resources["cycle_time_tolerance_l2"],
        //   field: "mold.cycleTimeToleranceL2",
        //   sortable: true,
        //   sortField: "mold.cycleTimeLimit2",
        // },
        // {
        //   label: this.resources["weighted_average_cycle_time"],
        //   field: "mold.weightedAverageCycleTime",
        //   sortable: true,
        //   sortField: "mold.weightedAverageCycleTime",
        // },

        // {
        //   label: this.resources["engineer_in_charge"],
        //   field: "mold.engineerInCharge",
        //   sortable: true,
        //   sortField: "mold.engineer",
        // },

        // {
        //   label: this.resources["forecasted_max_shots"],
        //   field: "mold.designedShot",
        //   sortable: true,
        //   sortField: "mold.designedShot",
        // },

        // {
        //   label: this.resources["hot_runner_number_of_drop"],
        //   field: "mold.hotRunnerDrop",
        //   sortable: true,
        //   sortField: "mold.hotRunnerDrop",
        // },
        // {
        //   label: this.resources["hot_runner_zone"],
        //   field: "mold.hotRunnerZone",
        //   sortable: true,
        //   sortField: "mold.hotRunnerZone",
        // },
        // {
        //   label: this.resources["front_injection"],
        //   field: "mold.injectionMachineId",
        //   sortable: true,
        //   sortField: "mold.injectionMachineId",
        // },
        // {
        //   label: this.resources["required_labor"],
        //   field: "mold.labour",
        //   sortable: true,
        //   sortField: "mold.labour",
        // },
        // {
        //   label: this.resources["last_date_of_shots"],
        //   field: "mold.lastShotDate",
        //   sortable: true,
        //   sortField: "mold.lastShotAt",
        // },

        // {
        //   label: this.resources["accumulated_shots"],
        //   field: "lastShot",
        //   sortable: true,
        //   sortField: "mold.lastShot",
        // },

        // {
        //   label: this.resources["machine_ton"],
        //   field: "mold.quotedMachineTonnage",
        //   sortable: true,
        //   sortField: "mold.quotedMachineTonnage",
        // },
        // {
        //   label: this.resources["maintenance_interval"],
        //   field: "mold.preventCycle",
        //   sortable: true,
        //   sortField: "mold.preventCycle",
        // },
        // {
        //   label: this.resources["maker_of_runner_system"],
        //   field: "mold.runnerMaker",
        //   sortable: true,
        //   sortField: "mold.runnerMaker",
        // },
        // {
        //   label: this.resources["maximum_capacity_per_week"],
        //   field: "mold.maxCapacityPerWeek",
        //   sortable: true,
        //   sortField: "mold.maxCapacityPerWeek",
        // },
        // // { label: this.resources['overdue_maintenance_tolerance'], field: 'mold.preventOverdue', sortable: true, sortField: 'mold.preventOverdue' },

        // { label: this.resources["parts"], field: "part", sortable: true },

        // {
        //   label: this.resources["production_day_per_week"],
        //   field: "mold.productionDays",
        //   sortable: true,
        //   sortField: "mold.productionDays",
        // },
        // {
        //   label: this.resources["production_hour_per_day"],
        //   field: "mold.shiftsPerDay",
        //   sortable: true,
        //   sortField: "mold.shiftsPerDay",
        // },
        // {
        //   label: this.resources["shot_weight"],
        //   field: "mold.shotSize",
        //   sortable: true,
        //   sortField: "mold.shotSize",
        // },
        // {
        //   label: this.resources["supplier"],
        //   field: "mold.supplierCompanyName",
        //   sortable: true,
        //   sortField: "mold.supplier.name",
        // },
        // {
        //   label: this.resources["tool_description"],
        //   field: "mold.toolDescription",
        //   sortable: true,
        //   sortField: "mold.toolDescription",
        // },
        // {
        //   label: this.resources["front_tool_size"],
        //   field: "mold.toolingSizeView",
        //   sortable: true,
        //   sortField: "mold.size",
        // },
        // {
        //   label: this.resources["tool_weight"],
        //   field: "mold.toolingWeightView",
        //   sortable: true,
        //   sortField: "mold.weight",
        // },
        // {
        //   label: this.resources["tooling_complexity"],
        //   field: "mold.toolingComplexity",
        //   sortable: true,
        //   sortField: "mold.toolingComplexity",
        // },
        // {
        //   label: this.resources["tooling_letter"],
        //   field: "mold.toolingLetter",
        //   sortable: true,
        //   sortField: "mold.toolingLetter",
        // },
        // {
        //   label: this.resources["tooling_type"],
        //   field: "mold.toolingType",
        //   sortable: true,
        //   sortField: "mold.toolingType",
        // },
        // {
        //   label: this.resources["toolmaker"],
        //   field: "mold.toolMakerCompanyName",
        //   sortable: true,
        //   sortField: "mold.toolMaker.name",
        // },
        // {
        //   label: this.resources["type_of_runner_system"],
        //   field: "mold.runnerTypeTitle",
        //   sortable: true,
        //   sortField: "mold.runnerType",
        // },
        // {
        //   label: this.resources["upcoming_maintenance_tolerance"],
        //   field: "mold.preventUpcoming",
        //   sortable: true,
        //   sortField: "mold.preventUpcoming",
        // },
        // {
        //   label: this.resources["uptime_target"],
        //   field: "mold.uptimeTarget",
        //   sortable: true,
        //   sortField: "mold.uptimeTarget",
        // },
        // {
        //   label: this.resources["uptime_tolerance_l1"],
        //   field: "mold.uptimeLimitL1",
        //   sortable: true,
        //   sortField: "mold.uptimeLimitL1",
        // },
        // {
        //   label: this.resources["uptime_tolerance_l2"],
        //   field: "mold.uptimeLimitL2",
        //   sortable: true,
        //   sortField: "mold.uptimeLimitL2",
        // },
        // {
        //   label: this.resources["utilization_rate"],
        //   field: "mold.utilizationRate",
        //   sortable: true,
        //   sortField: "mold.utilizationRate",
        // },
        // {
        //   label: this.resources["weight_of_runner_system_o"],
        //   field: "weightOfRunnerSystem",
        //   sortable: true,
        //   sortField: "mold.weightRunner",
        // },
        // {
        //   label: this.resources["year_of_tool_made"],
        //   field: "mold.madeYear",
        //   sortable: true,
        //   sortField: "mold.madeYear",
        // },
        // {
        //   label: this.resources["machine_id"],
        //   field: "machineCode",
        //   sortable: true,
        //   sortField: "mold.machine.machineCode",
        // },
      ];
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(this, "TOOLING", this.allColumnList, "mold");
        this.getColumnListSelected();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
      // this.resetColumnsListSelected();
      // this.getColumnListSelected();
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn("TOOLING", "mold")
        .then(async (data) => {
          this.allColumnList.push(...data);
          this.allHistoryColumnList.push(...data);
          // this.resetColumnsListSelected();
          // this.getColumnListSelected();
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
      this.allHistoryColumnList.forEach((item) => {
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
      axios
        .get(`/api/config/column-config?pageType=${this.pageTypeHistory}`)
        .then((response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allHistoryColumnList.forEach((item) => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            this.allHistoryColumnList.sort(function (a, b) {
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

    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    log(s, e) {
      console.log("molds log", s, e);
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
    },
    handleChangeValueCheckBox(value) {
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
      if (this.requestParam.status === "confirmed") {
        this.allHistoryColumnList = dataFake;
      } else {
        this.allColumnList = dataFake;
      }

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
          pageType:
            this.requestParam.status === "confirmed"
              ? this.pageTypeHistory
              : this.pageType,
          columnConfig: data,
        })
        .then((response) => {
          let hashedByColumnName = {};
          response.data.forEach((item) => {
            hashedByColumnName[item.columnName] = item;
          });
          if (this.requestParam.status === "confirmed") {
            self.allHistoryColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
          } else {
            self.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
          }
        })
        .finally(() => {
          if (this.requestParam.status === "confirmed") {
            self.allHistoryColumnList.sort(function (a, b) {
              return a.position - b.position;
            });
          } else {
            self.allColumnList.sort(function (a, b) {
              return a.position - b.position;
            });
          }

          self.$forceUpdate();
        });
    },

    changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
      console.log("changeAlertGeneration", alertType);
      this.alertType = alertType;
      //todo: alertOn is false to no show list
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
      if (specialAlertType != null) {
        this.requestParam.specialAlertType = specialAlertType;
      }
      if ((!isInit && alertOn != false) || specialAlertType != null)
        this.paging(1);
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

    hasChecked() {
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

    closeShowDropDown() {
      this.showDropdown = false;
    },

    check(e) {
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
        // TODO: recheck isAllTracked and listCheckedTracked
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

    tab(tab) {
      this.total = null;
      if (this.allColumnList) {
        this.allColumnList.forEach((c) => {
          if (c.field == "confirmedAt") {
            c.hiddenInToggle = tab.status !== "confirmed";
          }
        });
      }
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = tab.status;
      this.requestParam.tabName = tab.name;
      this.activeTab = tab.key;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    disable(user) {
      user.enabled = false;
      //this.save(user);
    },

    showChart(mold, tab) {
      console.log(mold);
      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "CYCLE_TIME", "DAY", tab ? tab : "switch-graph");
      }
    },
    showMoldDetails(mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },

    showCycleTime(mold) {
      var child = Common.vue.getChild(this.$children, "cycle-time-details");
      if (child != null) {
        child.showCycleTimeDetails(mold);
      }
    },
    showCycleTimeConfirmHistory(mold) {
      var child = Common.vue.getChild(
        this.$children,
        "cycle-time-confirm-history"
      );
      if (child != null) {
        child.show(mold);
      }
    },
    showLocationHistory(mold) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "location-history"
      );
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    showMessageDetails(data) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "message-details"
      );
      if (child != null) {
        var data = {
          title: "Cycle Time Information",
          messageTitle: "Message",
          equipmentCode: data.mold.equipmentCode,
          alertDate: data.notificationDateTime,
          confirmDate: data.confirmedDateTime,
          name: data.confirmedBy,
          message: data.message,
        };

        child.showMessageDetails(data);
      }
    },
    changeStatus(status) {
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
        }));
      }

      const modalConfig = {
        title: "Confirm",
        messageTitle: "Message",
        buttonTitle: "Correct",
        url: "/api/molds/cycle-time/confirm",
        param: payload,
      };

      const messageFormComponent = this.$refs["message-form"];
      console.log("messageFormComponent", messageFormComponent);
      // this.$parent.callbackMessageForm = this.callbackMessageForm.bind(this)
      messageFormComponent.showModal(modalConfig);
    },

    setListCategories() {
      for (var i = 0; i < this.results.length; i++) {
        var mold = this.results[i];
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

    weekConvert(dateString) {
      return `Week ${dateString.substring(4, 6)} ${dateString.substring(0, 4)}`;
    },
    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },

    async paging(pageNumber) {
      Common.handleNoResults("#app", 1);
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;

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
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();
      const params = Common.param(this.requestParam);
      const url = new URL(ALERT_CYCLE_TIME_API_BASE, location.origin);
      url.search = params;
      try {
        const { data: cycleTimeAlertData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });
        this.isLoading = false;
        this.total = cycleTimeAlertData.totalElements;
        this.results = cycleTimeAlertData.content;
        this.tabs = cycleTimeAlertData.tabs;
        this.pagination = Common.getPagingData(cycleTimeAlertData);
        if (this.total === this.listAllIds.length) {
          this.listChecked = [];
          this.listCheckedFull = [];
        }

        // Reset select all when change page
        if (this.selectType === "all") {
          this.listAllIds = [];
          this.listChecked = [];
          this.listCheckedFull = [];
          this.selectType = "";
        }
        this.setResultObject();
        this.setListCategories();
        Common.handleNoResults("#app", this.results.length);

        const pageInfo =
          this.requestParam.page === 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        this.isLoading = false;
        console.log("mold-cycle-time:::paging:::error:::", error);
      }
    },
    setResultObject() {
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
          break;
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
    callbackMessageForm() {
      this.resetAfterUpdate();
      this.paging(1);
      if (this.$parent && typeof this.$parent.reloadAlertCout === "function") {
        this.$parent.reloadAlertCout();
      }
    },
    resetAfterUpdate() {
      this.listChecked = [];
      this.listCheckedFull = [];
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
      const config = {
        objectType: null,
        systemNoteFunction: "CYCLE_TIME_ALERT",
      };
      if (this.listAllIds.length === this.total) {
        console.log("select-all");
        this.handleShowNoteSystem(
          this.listAllIds.map((i) => i.mainObjectId),
          true,
          config.objectType,
          config.systemNoteFunction
        );
      } else if (this.listCheckedFull.length > 1) {
        console.log("multiple select");
        this.handleShowNoteSystem(
          this.listCheckedFull.map((i) => i.moldId),
          true,
          config.objectType,
          config.systemNoteFunction
        );
      } else if (this.listCheckedFull.length === 1) {
        console.log("single select");
        this.handleShowNoteSystem(
          { id: this.listCheckedFull[0].moldId },
          false,
          config.objectType,
          config.systemNoteFunction
        );
      } else {
        this.idNoteBatchAction = selectedItem?.id || "";
        console.log("single select in history tab");
        this.handleShowNoteSystem(
          { id: selectedItem.moldId },
          false,
          config.objectType,
          config.systemNoteFunction
        );
      }
    },
    handleCycleTimeTolerance1(result) {
      if (!result.cycleTimeLimit1) {
        return "";
      } else {
        if (result.cycleTimeLimit1Unit === "SECOND") {
          return "s";
        }
        if (result.cycleTimeLimit1Unit === "PERCENTAGE") {
          return "%";
        }
      }
    },

    handleCycleTimeTolerance2(result) {
      if (!result.cycleTimeLimit2) {
        return "";
      } else {
        if (result.cycleTimeLimit2Unit === "SECOND") {
          return "s";
        }
        if (result.cycleTimeLimit2Unit === "PERCENTAGE") {
          return "%";
        }
      }
    },

    handleSelectAll(actionType) {
      this.selectType = actionType;
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
      const [hash, stringParams] = location.hash
        .slice("#".length) // exclude #
        .split("?");

      const params = Common.parseParams(stringParams);
      // TODO(ducnm2010): update to list after having api
      // if (params?.id) {
      //   this.requestParam.id = params.id;
      // }
      if (params?.tab) {
        this.tab(TAB_CONFIG.find((tab) => tab.status === params.tab)); // L1|L2
      } else {
        this.tab(
          TAB_CONFIG.find((tab) => tab.status === this.requestParam.status)
        );
      }
    },
    handleShowNoteSystem(result, isMultiple, objectType, systemNoteFunction) {
      const el = this.$refs["system-note"];
      console.log("handleShowNoteSystem", result, isMultiple, objectType, el);
      if (systemNoteFunction) {
        this.systemNoteFunction = systemNoteFunction;
      }
      if (el) {
        this.$nextTick(() => {
          el.showSystemNote(result, isMultiple, objectType);
        });
      }
    },
    async handleSubmitNote(data) {
      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const { filterCode, query, tabName, specialAlertType } =
        this.requestParam;
      const param = Common.param({
        filterCode,
        query,
        tabName,
        specialAlertType,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]:
          listId.length > 0 ? listId : [this.idNoteBatchAction],
      });
      try {
        await axios.post(
          `${ALERT_CYCLE_TIME_API_BASE}/note-batch?${param}`,
          data
        );
      } catch (error) {
        console.log("mold-cycle-time:::handleSubmitNote:::error", error);
      }
    },
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>
