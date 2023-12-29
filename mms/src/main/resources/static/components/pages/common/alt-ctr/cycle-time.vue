<template>
  <div>
    <notify-alert
      type="CYCLE_TIME"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>
    <div class="row">
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
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'alert' }"
                  href="#"
                  @click.prevent="tab('alert')"
                  ><span v-text="resources['alert']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'alert'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'L1' }"
                  href="#"
                  @click.prevent="tab('L1')"
                  ><span v-text="resources['outside_l1']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'L1'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'L2' }"
                  href="#"
                  @click.prevent="tab('L2')"
                  ><span v-text="resources['outside_l2']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'L2'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'confirmed' }"
                  href="#"
                  @click.prevent="tab('confirmed')"
                  ><span v-text="resources['history_log']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'confirmed'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <!--                            <show-columns v-show="alertOn" style="position: absolute; right: 20px;" @reset-columns-list="handleResetColumnsListSelected" @change-value-checkbox="handleChangeValueCheckBox" :all-columns-list="allColumnList" v-if="Object.entries(resources).length" :resources="resources"></show-columns>-->
              <customization-modal
                v-show="alertOn"
                style="position: absolute; right: 20px"
                :all-columns-list="allColumnList"
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
                      <input
                        v-if="allColumnList.length > 0"
                        style="vertical-align: middle !important"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </th>
                    <template
                      v-for="(column, index) in allColumnList"
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
                        <span> {{ column.label }} </span>
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
                    <!-- <th class="text-center" v-if="requestParam.status !== 'confirmed'"><span v-text="resources['action']"></span></th> -->
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
                            <input
                              id="checkbox-all-2"
                              class="checkbox"
                              type="checkbox"
                              v-model="isAll"
                              @change="select"
                            />
                          </div>
                        </div>
                        <div class="action-bar">
                          <div v-if="listChecked.length > 1">
                            No batch action is available
                          </div>
                          <div
                            v-if="
                              listChecked.length == 1 &&
                              requestParam.status != 'DELETED'
                            "
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
                                <li>
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeStatus(
                                        listCheckedFull[0],
                                        'CONFIRMED'
                                      )
                                    "
                                    >Confirm Alert</a
                                  >
                                </li>
                              </ul>
                            </div>
                          </div>
                          <div
                            v-if="
                              listChecked.length == 1 &&
                              requestParam.status != 'DELETED'
                            "
                            class="action-item"
                            @click="showSystemNoteModal(listCheckedFull[0])"
                          >
                            <span>Memo</span>
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
                        v-for="(column, columnIndex) in allColumnList"
                        v-if="column.enabled && !column.hiddenInToggle"
                      >
                        <td
                          v-if="column.field === 'toolingId'"
                          class="text-left"
                        >
                          <a
                            href="#"
                            @click.prevent="showChart(result.mold)"
                            style="color: #1aaa55"
                            class="mr-1"
                            ><i class="fa fa-bar-chart"></i
                          ></a>
                          <a
                            href="#"
                            @click.prevent="showMoldDetails(result.mold)"
                          >
                            {{ result.mold.equipmentCode }}
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
                                showCompanyDetailsById(
                                  result.mold.companyIdByLocation
                                )
                              "
                            >
                              {{ result.mold.companyName }}
                            </a>
                          </div>
                          <div class="small text-muted font-size-11-2">
                            {{ result.mold.companyCode }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'location'"
                        >
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="showLocationHistory(result.mold)"
                              >{{ result.mold.locationName }}</a
                            >
                          </div>
                          <span class="small text-muted font-size-11-2">{{
                            result.mold.locationCode
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
                                @click.prevent="showCycleTime(result.mold)"
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
                            {{
                              (
                                ((Number(result.cycleTime) -
                                  Number(result.contractedCycleTime)) *
                                  100) /
                                Number(result.contractedCycleTime)
                              ).toFixed(2)
                            }}
                            <div>
                              <span class="font-size-11-2">%</span>
                            </div>
                          </td>
                        </template>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'notificationAt'"
                        >
                          {{ result.notificationDateTimeShow }}
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
                            v-if="
                              result.mold.operatingStatus == 'WORKING' &&
                              (result.mold.equipmentStatus == 'INSTALLED' ||
                                result.mold.equipmentStatus == 'CHECK')
                            "
                            class="op-active label label-success"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'IDLE' &&
                              (result.mold.equipmentStatus == 'INSTALLED' ||
                                result.mold.equipmentStatus == 'CHECK')
                            "
                            class="op-active label label-warning"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'NOT_WORKING' &&
                              (result.mold.equipmentStatus == 'INSTALLED' ||
                                result.mold.equipmentStatus == 'CHECK')
                            "
                            class="op-active label label-inactive"
                          ></span>
                          <span
                            v-if="
                              result.mold.operatingStatus == 'DISCONNECTED' &&
                              (result.mold.equipmentStatus == 'INSTALLED' ||
                                result.mold.equipmentStatus == 'CHECK')
                            "
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
                              v-if="result.notificationStatus === 'ALERT'"
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
                            showCycleTimeConfirmHistory(result.mold)
                          "
                          ><span
                            class="label"
                            v-if="result.notificationStatus == 'CONFIRMED'"
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
  </div>
</template>
<script>
module.exports = {
  components: {
    "cycle-time-details": httpVueLoader("/components/cycle-time-alerts.vue"),
    "cycle-time-confirm-history": httpVueLoader(
      "/components/cycle-time-confirm-history.vue"
    ),
  },
  props: {
    resources: Object,
    showCreateWorkorder: Function,
    currentTabData: Object,
  },
  data() {
    return {
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      showDrowdown: null,
      isLoading: false,
      isAllTracked: [],
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "alert",
        extraStatus: "op-status-is-not-null",
        sort: "id,desc",
        page: 1,

        operatingStatus: "",
        notificationStatus: "",
      },

      statusCodes: [],
      listCategories: [],
      sortType: {
        ID: "mold.equipmentCode",
        COMPANY: "mold.location.company.name",
        LOCATION: "mold.location.name",
        CT: "cycleTimeStatus",
        FINAL_APPROVED: "contractedCycleTime",
        CYCLE_TIME: "cycleTime",
        OP: "mold.operatingStatus",
        VARIANCE: "variance",
        DATE: "notificationAt",
        STATUS: "notificationStatus",
        CONFIRM_DATE: "confirmedAt",
      },
      pageType: "CYCLE_TIME_ALERT",
      allColumnList: [],
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
    };
  },
  computed: {
    isSubmenuPermitted() {
      return Boolean(
        Common.isMenuPermitted(
          this.currentTabData.menuId,
          this.currentTabData.submenuId
        )
      );
    },
    isBtnAlertConfirmPermitted() {
      return Common.isMenuPermitted(
        this.currentTabData.menuId,
        this.currentTabData.submenuId,
        "btnAlertConfirm"
      );
    },
    isBtnOutsideL1ConfirmPermitted() {
      return Common.isMenuPermitted(
        this.currentTabData.menuId,
        this.currentTabData.submenuId,
        "btnOutsideL1Confirm"
      );
    },
    isBtnOutsideL2ConfirmPermitted() {
      return Common.isMenuPermitted(
        this.currentTabData.menuId,
        this.currentTabData.submenuId,
        "btnOutsideL2Confirm"
      );
    },
  },
  watch: {
    resources(newVal, oldVal) {
      if (
        newVal &&
        Object.entries(newVal).length > 0 &&
        (oldVal == null || Object.entries(oldVal).length == 0)
      ) {
        console.log("change resources");
        this.callSetAllColumnList();
      }
    },
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this;
        console.log(this.results, "xxx");
        console.log(self.listChecked, "xxx");
        const x = this.results.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        console.log(x, "xxx");
        this.listCheckedFull = x;
      }
    },
  },
  created() {},
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
    this.callSetAllColumnList();
    // this.$nextTick(function () {
    // 모든 화면이 렌더링된 후 실행합니다.
    var self = this;

    var uri = URI.parse(location.href);
    let params = Common.parseParams(uri.query);
    console.log("uri", uri);
    if (params.tabbedDashboardRedirected) {
      self.requestParam.tabbedDashboardRedirected =
        params.tabbedDashboardRedirected;
      if (params.tab === "OUTSIDE_L1") {
        this.requestParam.status = "L1";
        this.tab("L1");
      }
      if (params.tab === "OUTSIDE_L2") {
        this.requestParam.status = "L2";
        this.tab("L2");
      }
    }
    if (params.dashboardRedirected) {
      self.requestParam.dashboardRedirected = params.dashboardRedirected;
    }
    if (uri.fragment == "outside_l1") {
      self.tab("L1");
    } else if (uri.fragment == "outside_l2") {
      self.tab("L2");
    } else {
      // self.paging(1);
    }
    // })
  },
  methods: {
    showMachineDetails: function (machineId) {
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
    showPartChart: function (part, mold) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$parent.$children, "chart-part");
      if (child != null) {
        child.showChartPartMold(part, mold, "QUANTITY", "DAY");
      }
    },
    callSetAllColumnList() {
      if (
        this.resources &&
        Object.entries(this.resources).length > 0 &&
        this.allColumnList.length == 0
      ) {
        this.setAllColumnList();
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
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "mold.location.name",
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
          sortField: "notificationAt",
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
          sortField: "mold.operatingStatus",
        },
        {
          label: this.resources["status"],
          field: "notificationStatus",
          default: true,
          sortable: true,
          sortField: "notificationStatus",
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
          label: this.resources["forecasted_max_shot"],
          field: "mold.designedShot",
          sortable: true,
          sortField: "mold.designedShot",
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
        {
          label: this.resources["accumulated_shots"],
          field: "lastShot",
          sortable: true,
          sortField: "mold.lastShot",
        },
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
          label: this.resources["year_of_tool_made"],
          field: "mold.madeYear",
          sortable: true,
          sortField: "mold.madeYear",
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
    handleResetColumnsListSelected: function () {
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

    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    log: function (s, e) {
      console.log("molds log", s, e);
    },
    getUnitName: function (unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
    },
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
    },

    saveSelectedList: function (dataCustomize, list) {
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
    checkSelect: function (id) {
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

    closeShowDropDown() {
      this.showDropdown = false;
    },

    check: function (e) {
      if (e.target.checked) {
        this.listChecked.push(+e.target.value);
        Common.setHeightActionBar();
      } else {
        this.listChecked = this.listChecked.filter(
          (value) => value != e.target.value
        );
        if (this.listChecked.length === 0) {
          this.isAll = false;
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

    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.results.map((value) => value.id);
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.requestParam.status,
        });
      } else {
        this.listChecked = [];
        this.isAllTracked = this.isAllTracked.filter(
          (item) =>
            item.page !== this.requestParam.page &&
            item.status !== this.requestParam.status
        );
      }
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },

    tab: function (status) {
      this.total = null;
      if (this.allColumnList) {
        this.allColumnList.forEach((c) => {
          if (c.field == "confirmedAt") {
            c.hiddenInToggle = status == "confirmed" ? false : true;
          }
        });
      }
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.isAll = false;
      this.listChecked = [];
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },

    disable: function (user) {
      user.enabled = false;
      //this.save(user);
    },

    showChart: function (mold, tab) {
      console.log(mold);
      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "CYCLE_TIME", "DAY", tab ? tab : "switch-graph");
      }
    },
    showMoldDetails: function (mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },

    showCycleTime: function (mold) {
      var child = Common.vue.getChild(this.$children, "cycle-time-details");
      if (child != null) {
        child.showCycleTimeDetails(mold);
      }
    },
    showCycleTimeConfirmHistory: function (mold) {
      var child = Common.vue.getChild(
        this.$children,
        "cycle-time-confirm-history"
      );
      if (child != null) {
        child.show(mold);
      }
    },
    showLocationHistory: function (mold) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "location-history"
      );
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    showMessageDetails: function (data) {
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
    changeStatus: function (dataParam, status) {
      var param = {
        id: dataParam ? dataParam.id : null,
        moldLocationStatus: status,
        message: "",
      };

      var data = {
        title: "Confirm",
        messageTitle: "Message",
        buttonTitle: "Confirm",

        url: "/api/molds/cycle-time/" + param.id + "/confirm",
        param: param,
      };
      console.log("dataParam; ", dataParam);
      if (!dataParam) {
        let listChecked = [];
        Object.keys(this.listCheckedTracked).forEach((status) => {
          if (status === this.requestParam.status) {
            Object.keys(this.listCheckedTracked[status]).forEach((page) => {
              if (this.listCheckedTracked[status][page].length > 0) {
                listChecked = listChecked.concat(
                  this.listCheckedTracked[status][page]
                );
              }
            });
          }
        });
        if (listChecked.length === 0) {
          return;
        }
        // open for all select
        data = {
          title: "Confirm",
          messageTitle: "Message",
          buttonTitle: "Confirm",

          url: "/api/molds/cycle-time/confirm",
          param: listChecked.map((value) => {
            return {
              id: value,
              message: "",
            };
          }),
        };
      }
      var messageFormComponent = Common.vue.getChild(
        this.$parent.$children,
        "message-form"
      );
      if (messageFormComponent != null) {
        vm.callbackMessageForm = this.callbackMessageForm.bind(this);
        messageFormComponent.showModal(data);
      }
    },

    setListCategories: function () {
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

    weekConvert: function (dateString) {
      return `Week ${dateString.substring(4, 6)} ${dateString.substring(0, 4)}`;
    },

    paging: function (pageNumber) {
      Common.handleNoResults("#app", 1);
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      // checking is All
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked
        .filter((item) => item.status === this.requestParam.status)
        .map((item) => item.page);
      // if (pageSelectedAll.includes(this.requestParam.page)) {
      //     this.isAll = true;
      // }
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
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      self.isLoading = true;
      axios
        .get("/api/molds/cycle-time?lastAlert=true&" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          self.total = response.data.totalElements;
          self.results = response.data.content.map((value) => {
            value.confirmedAtShow = self.convertTimestampToDateWithFormat(
              value.confirmedAt,
              "YYYY-MM-DD HH:mm:ss"
            );
            value.notificationDateTimeShow = self.getNotificationDate(
              value.createdAt,
              value.periodType
            );
            return value;
          });
          self.pagination = Common.getPagingData(response.data);
          // if (self.listCheckedTracked[self.requestParam.status][self.requestParam.page]) {
          //     self.listChecked = self.listCheckedTracked[self.requestParam.status][self.requestParam.page];
          // }

          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          self.setResultObject();

          // 카테고리 정보 저장.
          self.setListCategories();

          Common.handleNoResults("#app", self.results.length);

          var pageInfo =
            self.requestParam.page == 1
              ? ""
              : "?page=" + self.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
          self.isLoading = false;
        });
    },
    getNotificationDate(timestamp, alertType) {
      timestamp = timestamp * 1000;
      if (alertType === this.alertTypeOptions.MONTHLY)
        return moment(timestamp).format("MMM YYYY");
      if (alertType === this.alertTypeOptions.WEEKLY) {
        let week = moment(timestamp).week();
        if (week < 10) {
          week = "0" + week;
        }
        return (
          "W-" +
          week +
          ", " +
          moment(timestamp).subtract(1, "day").format("YYYY")
        );
      }
      return moment(timestamp).subtract(1, "day").format("MMM DD, YYYY");
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

    findMoldFromList: function (results, moldId) {
      for (var j = 0; j < results.length; j++) {
        if (typeof results[j].mold !== "object") {
          continue;
        }

        var mold = results[j].mold;
        if (moldId == mold.id) {
          return mold;
          break;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findMold = this.findMold(mold.part.molds, moldId);
          if (typeof findMold === "object") {
            return findMold;
            break;
          }
        }
      }
    },

    findMold: function (results, moldId) {
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
            break;
          }
        }
      }
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
    callbackMessageForm: function () {
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
    sortBy: function (type) {
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
    sort: function () {
      this.paging(1);
    },
    showSystemNoteModal(result) {
      console.log(this.listCheckedFull, "listCheckedFull");
      console.log(result, "result");
      var child = Common.vue.getChild(this.$parent.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result.moldId });
      }
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
      }
    },
  },
};
</script>
