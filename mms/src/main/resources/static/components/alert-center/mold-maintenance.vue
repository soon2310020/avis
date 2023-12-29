<template>
  <div>
    <notify-alert
      type="MAINTENANCE"
      @change-alert-generation="changeAlertGeneration"
      v-if="['alert', 'done'].includes(requestParam.status)"
      :resources="resources"
    ></notify-alert>
    <notify-alert
      @change-alert-generation="changeAlertGeneration"
      type="CORRECTIVE_MAINTENANCE"
      :resources="resources"
      v-else
    ></notify-alert>
    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['maintenance']"></strong>

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
                  :class="{ active: requestParam.status === 'alert' }"
                  href="#"
                  @click.prevent="tab('alert')"
                >
                  <span>
                    <span v-text="resources['pm']"></span>
                    <span
                      v-show="alertOn"
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status === 'alert'"
                      >{{ total }}</span
                    >
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status === 'cm' }"
                  href="#"
                  @click.prevent="tab('cm')"
                >
                  <span>
                    <span v-text="resources['cm']"></span>
                    <span
                      v-show="alertOn"
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status === 'cm'"
                      >{{ total }}</span
                    >
                  </span>
                </a>
              </li>

              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status === 'done' }"
                  href="#"
                  @click.prevent="tab('done')"
                  ><span v-text="resources['pm']"></span>
                  <span>
                    <span v-text="resources['history_log']"></span>
                    <span
                      v-show="alertOn"
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status === 'done'"
                      >{{ total }}</span
                    >
                  </span>
                </a>
              </li>

              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status === 'cm-history' }"
                  href="#"
                  @click.prevent="tab('cm-history')"
                  ><span v-text="resources['cm']"></span>
                  <span>
                    <span v-text="resources['history_log']"></span>
                    <span
                      v-show="alertOn"
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status === 'cm-history'"
                      >{{ total }}</span
                    >
                  </span>
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
                    <th
                      v-if="
                        requestParam.status !== 'done' &&
                        requestParam.status !== 'cm-history'
                      "
                      style="width: 40px; vertical-align: middle !important"
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
                    </th>

                    <template
                      v-for="(column, index) in allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <template
                        v-if="
                          ['utilNextPm', 'shotUntilNextPM'].includes(
                            column.field
                          )
                        "
                      >
                        <th
                          v-if="['alert'].includes(requestParam.status)"
                          :key="index"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <div style="display: inline">
                            <span
                              v-if="'shotUntilNextPM' == column.field"
                              v-text="resources['shot_until_next_pm']"
                            ></span>
                            <span
                              v-else
                              v-text="resources['pm_checkpoint_progress']"
                            ></span>
                            <span
                              v-if="column.sortable"
                              class="__icon-sort"
                              style="
                                position: static;
                                display: inline-block;
                                margin-right: 4px;
                                width: 18px;
                                vertical-align: middle;
                              "
                            ></span>
                            <a-tooltip placement="bottom">
                              <template slot="title">
                                <div style="padding: 16px; font-size: 13px">
                                  {{
                                    "shotUntilNextPM" == column.field
                                      ? resources["shot_until_next_pm_tooltip"]
                                      : resources["util_next_pm_tooltip"]
                                  }}
                                </div>
                              </template>
                              <img
                                style="width: 15px"
                                src="/images/icon/information.svg"
                                alt="error icon"
                              />
                            </a-tooltip>
                          </div>
                        </th>
                      </template>
                      <template
                        v-else-if="
                          [
                            'contractedCycleTime',
                            'cycleTime',
                            'variance',
                            'notificationStatus',
                          ].includes(column.field)
                        "
                      >
                        <th
                          v-if="requestParam.status !== 'confirmed'"
                          :key="index"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            column.label === 'Status' || column.label === 'OP'
                              ? ''
                              : 'text-left',
                          ]"
                          @click="sortBy(column.sortField, requestParam.status)"
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
                          :key="index"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            column.label === 'Status' || column.label === 'OP'
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
                        :key="index"
                        :class="[
                          {
                            __sort: column.sortable,
                            active: currentSortType === column.sortField,
                          },
                          isDesc ? 'desc' : 'asc',
                          column.label === 'Status' || column.label === 'OP'
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
                          column.label === 'Status' || column.label === 'OP'
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
                      v-if="
                        ['done', 'cm-history'].includes(requestParam.status)
                      "
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
                      <div class="first-checkbox-zone2">
                        <div
                          class="first-checkbox2"
                          style="
                            display: flex;
                            align-items: center;
                            width: 110px;
                          "
                        >
                          <div>
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
                        </div>
                        <div class="action-bar">
                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            v-click-outside="closeShowDropDown"
                          >
                            <div
                              class="change-status-dropdown drowdown d-inline"
                            >
                              <template v-if="requestParam.status == 'alert'">
                                <template
                                  v-if="
                                    ['OVERDUE', 'UPCOMING'].includes(
                                      listCheckedFull[0].maintenanceStatus
                                    )
                                  "
                                >
                                  <div
                                    title="Change status"
                                    class="change-status"
                                    @click="showDropdown = !showDropdown"
                                  >
                                    <span>Change Status</span>
                                    <i
                                      class="icon-action change-status-icon"
                                    ></i>
                                  </div>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <li v-if="isBtnPmConfirmPermitted">
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showMaintenanceAlert(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Confirm Alert</a
                                      >
                                    </li>
                                  </ul>
                                </template>
                                <template
                                  v-else-if="
                                    listCheckedFull[0].maintenanceStatus ==
                                    'DONE'
                                  "
                                >
                                  <div
                                    title="Change status"
                                    class="change-status"
                                    @click="showDropdown = !showDropdown"
                                  >
                                    <span>Change Status</span>
                                    <i
                                      class="icon-action change-status-icon"
                                    ></i>
                                  </div>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <li class="dropdown-header">
                                      <span class="op-change-status"
                                        >No option is available</span
                                      >
                                    </li>
                                  </ul>
                                </template>
                                <template v-else>
                                  <div
                                    title="Change status"
                                    class="change-status"
                                    @click="showDropdown = !showDropdown"
                                  >
                                    <span>Change Status</span>
                                    <i
                                      class="icon-action change-status-icon"
                                    ></i>
                                  </div>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'APPROVED' &&
                                        (requestParam.status === 'cm' ||
                                          requestParam.status === 'cm-history')
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          changeStatus(listCheckedFull[0])
                                        "
                                        >Complete Maintenance</a
                                      >
                                    </li>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'REQUESTED' &&
                                        (requestParam.status === 'cm' ||
                                          requestParam.status ===
                                            'cm-history') &&
                                        isBtnCmApprovePermitted
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          changeStatus(listCheckedFull[0])
                                        "
                                        >Approve</a
                                      >
                                    </li>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'REQUESTED' &&
                                        (requestParam.status === 'cm' ||
                                          requestParam.status ===
                                            'cm-history') &&
                                        isBtnCmDisapprovePermitted
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          changeStatus(listCheckedFull[0])
                                        "
                                        >Disapprove</a
                                      >
                                    </li>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'FAILURE' &&
                                        (requestParam.status === 'cm' ||
                                          requestParam.status ===
                                            'cm-history') &&
                                        isBtnCmRequestPermitted
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Request Maintenance</a
                                      >
                                    </li>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'DISAPPROVED' &&
                                        (requestParam.status === 'cm' ||
                                          requestParam.status === 'cm-history')
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Request Maintenance</a
                                      >
                                    </li>

                                    <li
                                      v-if="
                                        listCheckedFull[0].maintenanceStatus ===
                                          'DONE' &&
                                        (requestParam.status === 'done' ||
                                          requestParam.status === 'alert')
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >No option is available</a
                                      >
                                    </li>

                                    <li
                                      v-if="
                                        listCheckedFull[0].maintenanceStatus ===
                                          'OVERDUE' &&
                                        (requestParam.status === 'done' ||
                                          requestParam.status === 'alert')
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          changeStatus(listCheckedFull[0])
                                        "
                                        >Confirm</a
                                      >
                                    </li>

                                    <li
                                      v-if="
                                        listCheckedFull[0].maintenanceStatus ===
                                          'UPCOMING' &&
                                        (requestParam.status === 'done' ||
                                          requestParam.status === 'alert')
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          changeStatus(listCheckedFull[0])
                                        "
                                        >Confirm</a
                                      >
                                    </li>
                                  </ul>
                                </template>
                              </template>
                              <template v-else-if="requestParam.status == 'cm'">
                                <template
                                  v-if="
                                    ['DISAPPROVED', 'FAILURE'].includes(
                                      listCheckedFull[0].correctiveStatus
                                    )
                                  "
                                >
                                  <div
                                    title="Change status"
                                    class="change-status"
                                    @click="showDropdown = !showDropdown"
                                  >
                                    <span
                                      v-text="resources['change_status']"
                                    ></span>
                                    <i
                                      class="icon-action change-status-icon"
                                    ></i>
                                  </div>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <li v-if="isBtnCmRequestPermitted">
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Request Maintenance</a
                                      >
                                    </li>
                                  </ul>
                                </template>
                                <template v-else>
                                  <div
                                    title="Change status"
                                    class="change-status"
                                    @click="showDropdown = !showDropdown"
                                  >
                                    <span>Change Status</span>
                                    <i
                                      class="icon-action change-status-icon"
                                    ></i>
                                  </div>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <li v-if="requestParam.status === 'alert'">
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showMaintenanceAlert(
                                            listCheckedFull[0]
                                          )
                                        "
                                        v-text="resources['confirm']"
                                      ></a>
                                    </li>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                          'FAILURE' ||
                                        listCheckedFull[0].correctiveStatus ===
                                          'DISAPPROVED'
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        v-text="
                                          resources['request_maintenance']
                                        "
                                      ></a>
                                    </li>
                                    <template
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                        'REQUESTED'
                                      "
                                    >
                                      <li>
                                        <a
                                          href="#"
                                          class="dropdown-item"
                                          @click.prevent="
                                            showRequestMaintenance(
                                              listCheckedFull[0]
                                            )
                                          "
                                          v-text="resources['approve']"
                                        ></a>
                                      </li>
                                      <li>
                                        <a
                                          href="#"
                                          class="dropdown-item"
                                          @click.prevent="
                                            changeStatus(listCheckedFull[0])
                                          "
                                          v-text="resources['disapprove']"
                                        ></a>
                                      </li>
                                    </template>
                                    <li
                                      v-if="
                                        listCheckedFull[0].correctiveStatus ===
                                        'APPROVED'
                                      "
                                    >
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Complete Maintenance</a
                                      >
                                    </li>
                                  </ul>
                                </template>
                              </template>
                            </div>
                          </div>

                          <div
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
                              showCreateWorkorder(
                                listCheckedFull[0],
                                requestParam.status
                              )
                            "
                            style="align-items: flex-start"
                          >
                            <span>Create Work order</span>
                            <img
                              src="/images/icon/icon-workorder.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canAcceptWorkOrder"
                            class="action-item"
                            @click="
                              showWorkOrderDetail(listCheckedFull[0].workOrder)
                            "
                            style="align-items: flex-start"
                          >
                            <span>{{ resources["accept_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/handshake-icon.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canCompleteWorkOrder"
                            class="action-item"
                            @click="
                              showCompleteWorkOrder(
                                listCheckedFull[0].workOrder
                              )
                            "
                            style="align-items: flex-start"
                          >
                            <span>{{ resources["complete_work_order"] }}</span>
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
                  <tr
                    v-show="!isLoading"
                    v-for="(result, index) in results"
                    :key="result.id"
                    :id="result.id"
                  >
                    <td
                      v-if="
                        requestParam.status !== 'done' &&
                        requestParam.status !== 'cm-history'
                      "
                    >
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
                      <td v-if="column.field === 'toolingId'" class="text-left">
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
                        v-else-if="column.field === 'location'"
                      >
                        <div v-if="result.mold.locationCode">
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="showLocationHistory(result.mold)"
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
                        style="vertical-align: middle !important"
                        class="text-center op-status-td"
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

                      <template v-else-if="column.field === 'variance'">
                        <td v-if="requestParam.status !== 'confirmed'">
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
                      <!--                      <td style="vertical-align: middle!important;" v-else-if="column.field === 'notificationStatus'"-->
                      <!--                        class="status-cell text-center op-status-td">-->
                      <!--                        <span class="label label-warning" v-if="result.maintenanceStatus === 'UPCOMING'"-->
                      <!--                          v-text="resources['upcoming']"></span>-->
                      <!--                        <span class="label label-danger" v-if="result.maintenanceStatus === 'OVERDUE'"-->
                      <!--                          v-text="resources['overdue']"></span>-->
                      <!--                        <a href="#" @click.prevent="showMaintanceConfirm(result.mold)" title="View"-->
                      <!--                          v-if="result.maintenanceStatus === 'DONE'">-->
                      <!--                          <img height="18" src="/images/icon/view.png" alt="View" /></a>-->
                      <!--                        <a href="#" @click.prevent="showCorrectiveMaintenanceHistory(result)"><span class="label "-->
                      <!--                            title="View" v-if="result.correctiveStatus === 'COMPLETED'"><img height="18"-->
                      <!--                              src="/images/icon/view.png" alt="View" /></span></a>-->
                      <!--                        <span class="label label-danger" v-if="result.correctiveStatus === 'FAILURE'"-->
                      <!--                          v-text="resources['failure']"></span>-->
                      <!--                        <span class="label label-primary" v-if="result.correctiveStatus === 'REQUESTED'"-->
                      <!--                          v-text="resources['maintenace_requested']"></span>-->
                      <!--                        <span class="label label-success"-->
                      <!--                          v-if="result.correctiveStatus === 'APPROVED' && requestParam.status === 'cm'"-->
                      <!--                          v-text="resources['approved']"></span>-->
                      <!--                        <span class="label"-->
                      <!--                          v-if="result.correctiveStatus === 'APPROVED' && requestParam.status === 'cm-history'"-->
                      <!--                          @click.prevent="showCorrectiveMaintenanceHistory(result)"><img height="18"-->
                      <!--                            src="/images/icon/view.png" alt="View" /></span>-->
                      <!--                        <span class="label label-default"-->
                      <!--                          v-if="result.correctiveStatus === 'DISAPPROVED' && requestParam.status === 'cm'"-->
                      <!--                          v-text="resources['disapproved']"></span>-->
                      <!--                        <span class="label"-->
                      <!--                          v-if="result.correctiveStatus === 'DISAPPROVED' && requestParam.status === 'cm-history'"-->
                      <!--                          @click.prevent="showCorrectiveMaintenanceHistory(result)"><img height="18"-->
                      <!--                            src="/images/icon/view.png" alt="View" /></span>-->

                      <!--                      </td>-->
                      <td
                        class="text-left"
                        v-else-if="
                          column.field === 'lastShot' ||
                          column.field === 'mold.lastShot'
                        "
                      >
                        {{ formatNumber(result.mold.accumulatedShot) }}
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'failureTime'"
                      >
                        <span class="font-size-14">
                          {{
                            convertDate(formatToDateTime(result.failureTime))
                          }}
                        </span>
                        <div>
                          <span class="font-size-11-2">
                            {{
                              convertTimeString(
                                formatToDateTime(result.failureTime)
                              )
                            }}
                          </span>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'utilizationRate'"
                      >
                        <div class="clearfix">
                          <div class="float-left font-size-14">
                            <strong>{{
                              formatNumber(result.mold.lastShot)
                            }}</strong>
                            <span
                              >/
                              {{ formatNumber(result.mold.designedShot) }}</span
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
                            <strong>{{ ratioUtilizationRate(result) }}%</strong>
                          </small>
                          <div
                            style="width: 100%; margin-left: 5px"
                            class="progress progress-xs"
                          >
                            <div
                              class="progress-bar"
                              :class="ratioColor(result)"
                              role="progressbar"
                              :style="ratioStyle(result)"
                              :aria-valuenow="ratio(result)"
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
                      <td
                        class="text-left"
                        v-else-if="column.field === 'mold.cycleTimeToleranceL1'"
                      >
                        <span class="font-size-14">{{
                          result.mold.cycleTimeToleranceL1
                        }}</span>
                        <div>
                          <span class="font-size-11-2">{{
                            result.mold.cycleTimeToleranceL1
                              ? handleCycleTimeTolerance1(result.mold)
                              : ""
                          }}</span>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'mold.cycleTimeToleranceL2'"
                      >
                        <span class="font-size-14">{{
                          result.mold.cycleTimeToleranceL2
                        }}</span>
                        <div>
                          <span class="font-size-11-2">{{
                            result.mold.cycleTimeToleranceL2
                              ? handleCycleTimeTolerance2(result.mold)
                              : ""
                          }}</span>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'mold.uptimeTarget'"
                      >
                        <span class="font-size-14">{{
                          result.mold.uptimeTarget
                        }}</span>
                        <div>
                          <span class="font-size-11-2">{{
                            result.mold.uptimeTarget ? "%" : ""
                          }}</span>
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
                        v-else-if="column.field === 'mold.quotedMachineTonnage'"
                      >
                        <span class="font-size-14">{{
                          result.mold.quotedMachineTonnage
                        }}</span>
                        <div>
                          <span class="font-size-11-2">{{
                            result.mold.quotedMachineTonnage ? "tonnes" : ""
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
                        <span class="font-size-14">{{ result.mold.size }}</span>
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
                        class="pm-period text-left"
                        v-else-if="column.field === 'pmCheckpoint'"
                      >
                        <span class="font-size-14">{{
                          formatNumber(result.period)
                        }}</span>
                        <div>
                          <span class="font-size-11-2">{{
                            result.period ? "shots" : ""
                          }}</span>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'utilNextPm'"
                      >
                        <div v-if="result.mold && result.mold.lastShot">
                          <div class="clearfix">
                            <div class="float-left font-size-14">
                              <strong>{{
                                formatNumber(result.lastShotMade)
                              }}</strong>
                              <span
                                >/
                                {{ formatNumber(result.mold.preventCycle) }}
                              </span>
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
                              <strong>{{ ratio2(result) }}%</strong>
                            </small>
                            <div
                              style="width: 100%; margin-left: 5px"
                              class="progress progress-xs"
                            >
                              <div
                                class="progress-bar"
                                :class="ratioColor2(result)"
                                role="progressbar"
                                :style="ratioStyle2(result)"
                                :aria-valuenow="ratio2(result)"
                                aria-valuemin="0"
                                aria-valuemax="100"
                              ></div>
                            </div>
                          </div>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'shotUntilNextPM'"
                      >
                        <span
                          :style="{
                            color:
                              result.maintenanceStatus == 'OVERDUE'
                                ? '#F86C6B'
                                : '#FFC107',
                          }"
                        >
                          {{ formatNumber(result.shotUntilNextPM) }}
                        </span>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'mold.designedShot'"
                      >
                        {{ formatNumber(result.mold.designedShot) }}
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'lastShotCheckpoint'"
                      >
                        <div v-if="result.mold && result.mold.lastShot">
                          <div class="clearfix">
                            <div class="float-left font-size-14">
                              <strong>{{
                                formatNumber(result.mold.lastShot)
                              }}</strong>
                              <span>/ {{ formatNumber(result.period) }}</span>
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
                              <strong>{{ ratio(result) }}%</strong>
                            </small>
                            <div
                              style="width: 100%; margin-left: 5px"
                              class="progress progress-xs"
                            >
                              <div
                                class="progress-bar"
                                :class="ratioColor(result)"
                                role="progressbar"
                                :style="ratioStyle(result)"
                                :aria-valuenow="ratio(result)"
                                aria-valuemin="0"
                                aria-valuemax="100"
                              ></div>
                            </div>
                          </div>
                        </div>
                      </td>
                      <!-- Last Maintenance Time / Due Date-->
                      <td
                        class="last-maintenance-time-cell text-left"
                        v-else-if="column.field === 'dueDate'"
                      >
                        <span>
                          {{
                            showDueDate(
                              result.dueDate,
                              result.maintenanceStatus
                            )
                          }}
                        </span>
                      </td>
                      <!-- PM Compliance Rate -->
                      <td
                        class="pm-complicance-rate-cell text-left"
                        v-else-if="column.field === 'pmComplianceRate'"
                      >
                        <a
                          href="#"
                          @click.prevent="showMaintanceHistory(result)"
                          >{{
                            !isNaN(result.executionRate)
                              ? Math.round(result.executionRate * 100) / 100
                              : result.executionRate
                          }}
                        </a>
                        <div>
                          <span class="font-size-11-2">%</span>
                        </div>
                      </td>
                      <!--                      <td style="vertical-align: middle!important;" v-else-if="column.field === 'op'"-->
                      <!--                        class="op-cell text-center op-status-td">-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus == 'WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                      <!--                          class="op-active label label-success"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus == 'IDLE' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                      <!--                          class="op-active label label-warning"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus == 'NOT_WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                      <!--                          class="op-active label label-inactive"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus == 'DISCONNECTED' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                      <!--                          class="op-active label label-danger"></span>-->
                      <!--                      </td>-->
                      <td
                        style="vertical-align: middle !important"
                        class="text-center op-status-td"
                        v-else-if="column.field === 'detail'"
                      >
                        <span
                          v-if="
                            ['REQUESTED', 'APPROVED', 'DISAPPROVED'].includes(
                              result.correctiveStatus
                            )
                          "
                          style="font-size: 26px; cursor: pointer"
                          @click.prevent="
                            showCorrectiveMaintenanceDetails(result)
                          "
                        >
                          <img
                            height="18"
                            src="/images/icon/text-document.svg"
                            alt="Details"
                          />
                          <!-- <i class="fa fa-file-text-o" aria-hidden="true"></i> -->
                        </span>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'mold.lastMaintenanceDate'"
                      >
                        <div class="font-size-14" style="margin-left: 3px">
                          {{ formatToDate(result.mold.lastMaintenanceDate) }}
                        </div>
                        <div class="font-size-11-2">
                          {{ getTimeData(result.mold.lastMaintenanceDate) }}
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'pmCheckpointPrediction'"
                      >
                        <span>
                          {{
                            showPmCheckpointPrediction(
                              result.pmCheckpointPrediction,
                              result?.maintenanceStatus
                            )
                          }}
                        </span>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="
                          column.field === 'mold.lastRefurbishmentDate'
                        "
                      >
                        <div class="font-size-14" style="margin-left: 3px">
                          {{ formatToDate(result.mold.lastRefurbishmentDate) }}
                        </div>
                        <div class="font-size-11-2">
                          {{ getTimeData(result.mold.lastRefurbishmentDate) }}
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
                      v-if="
                        requestParam.status === 'done' ||
                        requestParam.status === 'cm-history'
                      "
                      style="vertical-align: middle !important"
                      class="status-cell text-center op-status-td"
                    >
                      <a
                        href="javascript:void(0)"
                        @click="showSystemNoteModal(result)"
                        title="Add note"
                        class="table-action-link note"
                      >
                        <img src="/images/icon/note.svg" alt="Add note" />
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>

              <div
                class="no-results d-none"
                v-text="resources['no_results']"
              ></div>

              <div v-show="!isLoading" class="row">
                <div class="col-md-8">
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

    <maintance-confirm-history
      :resources="resources"
    ></maintance-confirm-history>
    <maintenance-alert
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></maintenance-alert>
    <maintenance-history :resources="resources"></maintenance-history>
    <request-maintenance
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></request-maintenance>
    <corrective-maintenance-details
      :resources="resources"
    ></corrective-maintenance-details>
    <corrective-maintenance-history
      @show-detail="showCorrectiveMaintenanceDetails"
      :resources="resources"
    ></corrective-maintenance-history>
    <disapproval-maintenance-form
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></disapproval-maintenance-form>
    <created-work-order-dialog
      :resources="resources"
      :visible="showCreatedWorkorder"
      :selected="selectedWorkOrder"
      :type="detailType"
      @close="handleCloseWorkOrder"
      @open-edit="openEditWorkOrder"
      @show-toast="handleShowToast"
    ></created-work-order-dialog>
    <!-- REPORTED -->
    <reported-work-order-dialog
      :resources="resources"
      :visible="showComplete"
      :selected="selectedWorkOrder"
      :view-only="false"
      @close="handleCloseCompleteWorkOrder"
      @submit="handleSubmitComplete"
    >
    </reported-work-order-dialog>
  </div>
</template>
<script>
const MESSAGES = {
  COMPLETE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: "Your Work Order has been completed.",
  },
  ACCEPT_DECLINE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: `Your Work Order's status has been updated.`,
  },
  REQUEST_CHANGE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: "Your Request Change Work Order has been submitted",
  },
};

const TAB_TYPE = {
  PM: "alert",
  CM: "cm",
  PM_HISTORY: "done",
  CM_HISTORY: "cm-history",
};

module.exports = {
  name: "AlertMaintenance",
  components: {
    "corrective-maintenance-details": httpVueLoader(
      "/components/corrective-maintenance-details.vue"
    ),
    "maintance-confirm-history": httpVueLoader(
      "/components/maintance-confirm-history.vue"
    ),
    "maintenance-alert": httpVueLoader("/components/maintenance-alert.vue"),
    "request-maintenance": httpVueLoader("/components/request-maintenance.vue"),
    "maintenance-history": httpVueLoader("/components/maintenance-history.vue"),
    "corrective-maintenance-history": httpVueLoader(
      "/components/corrective-maintenance-history.vue"
    ),
    "disapproval-maintenance-form": httpVueLoader(
      "/components/disapproval-maintenance-form.vue"
    ),
    "created-work-order-dialog": httpVueLoader(
      "/components/work-order/src/created-work-order-dialog.vue"
    ),
    "reported-work-order-dialog": httpVueLoader(
      "/components/work-order/src/reported-work-order-dialog.vue"
    ),
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
      results: [],
      showDrowdown: null,
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "alert",
        sort: "id,desc",
        page: 1,

        operatingStatus: "",
        maintenanced: true,
        maintenanceStatus: "",
      },

      statusCodes: [],
      listCategories: [],
      sortType: {
        ID: "mold.equipmentCode",
        COMPANY: "mold.location.company.name",
        LOCATION: "mold.location.name",
        LAST_SHOT: "mold.lastShot",
        PERIOD: "period",
        DUE_DATE: "dueDate",
        OP: "mold.operatingStatus",
        PM: "executionRate",
        STATUS: "correctiveStatus",
        FAILURE_TIME: "failureTime",
      },
      currentSortType: "partCode",
      isDesc: true,
      isInitForCm: false,
      isInitForPm: false,
      isLoading: false,
      allColumnList: [],
      pageType: "MAINTENANCE_ALERT",
      alertOn: true,
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      isAllTracked: [],
      listCheckedFull: [],
      loadedTypeServer: false,
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      options: {},
      selectedWorkOrder: {},
      showComplete: false,
      showCreatedWorkorder: false,
      detailType: "ASSIGNED",
      me: {},
      listAllIds: [],
    };
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText(newVal) {
      this.requestParam.query = newVal;
      this.paging(1);
    },
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
      console.log("@watch:listChecked", newVal);
    },
    listCheckedFull(newVal) {
      console.log("@watch:listCheckedFull", newVal);
    },
    listCheckedFull(newVal) {
      console.log("listCheckedFull", newVal);
    },
    listCheckedTracked: {
      handler(newVal) {
        console.log("@watch:listCheckedTrack", newVal);
      },
      immediate: true,
    },
    listAllIds(newVal) {
      console.log("@watch:mold-maintenance>listAllIds", newVal);
    },
  },
  async created() {
    const self = this;
    try {
      const options = await Common.getSystem("options");
      const me = await Common.getSystem("me");
      self.options = JSON.parse(options);
      self.me = JSON.parse(me);
      console.log("me", self.me);
      self.loadedTypeServer = true;
      self.callSetAllColumnList();
    } catch (error) {
      console.log(error);
    }
  },
  computed: {
    isPreventCycleDefaultShow() {
      return Boolean(
        this.options?.CLIENT?.moldMaintenance?.cols?.preventCycle?.show
      );
    },
    isPmCheckpointDefaultShow() {
      return Boolean(
        this.options?.CLIENT?.moldMaintenance?.cols?.pmCheckpoint?.show
      );
    },
    isUtilNextPmDefaultShow() {
      return Boolean(
        this.options?.CLIENT?.moldMaintenance?.cols?.utilNextPm?.show
      );
    },
    isLastShotCheckpoint() {
      return Boolean(
        this.options?.CLIENT?.moldMaintenance?.cols?.lastShotCheckpoint?.show
      );
    },
    isBtnCmApprovePermitted() {
      return Common.isMenuPermitted(
        Common.PERMISSION_CODE.ALERT_CENTER,
        Common.PERMISSION_CODE.ALERT_CENTER_MAINTENANCE,
        "btnCmApprove"
      );
    },
    isBtnCmConfirmPermitted() {
      return Common.isMenuPermitted(
        Common.PERMISSION_CODE.ALERT_CENTER,
        Common.PERMISSION_CODE.ALERT_CENTER_MAINTENANCE,
        "btnCmConfirm"
      );
    },
    isBtnCmDisapprovePermitted() {
      return Common.isMenuPermitted(
        Common.PERMISSION_CODE.ALERT_CENTER,
        Common.PERMISSION_CODE.ALERT_CENTER_MAINTENANCE,
        "btnCmDisapprove"
      );
    },
    isBtnCmRequestPermitted() {
      return Common.isMenuPermitted(
        Common.PERMISSION_CODE.ALERT_CENTER,
        Common.PERMISSION_CODE.ALERT_CENTER_MAINTENANCE,
        "btnCmRequest"
      );
    },
    isBtnPmConfirmPermitted() {
      return Common.isMenuPermitted(
        Common.PERMISSION_CODE.ALERT_CENTER,
        Common.PERMISSION_CODE.ALERT_CENTER_MAINTENANCE,
        "btnPmConfirm"
      );
    },
    canAcceptWorkOrder() {
      if (this.listChecked.length == 1) {
        const workOrder = this.listCheckedFull[0].workOrder;
        if (workOrder) {
          console.log("workOrder", workOrder);
          console.log("me", this.me);
          return workOrder.workOrderStatus === "Requested";
        }
      }
      return false;
    },
    canCompleteWorkOrder() {
      if (this.listChecked.length == 1) {
        const workOrder = this.listCheckedFull[0].workOrder;
        if (workOrder) {
          return ["Upcoming", "Overdue"].includes(workOrder.workOrderStatus);
        }
      }
      return false;
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
  methods: {
    showPmCheckpointPrediction: function (
      pmCheckpointPrediction,
      maintenanceStatus
    ) {
      if (pmCheckpointPrediction == null) return "";
      if (pmCheckpointPrediction > 90) {
        return "More than 90 days";
      }
      let dayName = pmCheckpointPrediction == 1 ? " day" : " days";
      let print =
        pmCheckpointPrediction == 0
          ? "Today"
          : maintenanceStatus == "OVERDUE"
          ? "Over " + pmCheckpointPrediction + dayName
          : "In " + pmCheckpointPrediction + dayName;
      console.log("showPmCheckpointPrediction", print);
      return print;
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
    getTimeData(timeData) {
      if (!timeData) return "";
      let time = this.formatToDateTime(timeData);
      if (time) {
        return time?.slice(10, 16);
      } else return "";
    },
    updateVariable(data) {
      console.log(data, "updateVariable");
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
        this.loadedTypeServer
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
          label: this.resources["part"],
          field: "part",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "mold.location.company.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "mold.location.name",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["accumulated_shots"],
          field: "mold.lastShot",
          default: true,
          sortable: true,
          sortField: "mold.lastShot",
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["failure_time"],
          field: "failureTime",
          default: true,
          sortable: true,
          sortField: "failureTime",
          hiddenInToggle: true,
        },
        {
          label: this.resources["maintenance_interval"],
          default: this.isPreventCycleDefaultShow,
          field: "preventCycle",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["pm_checkpoint"],
          default: this.isPmCheckpointDefaultShow,
          field: "pmCheckpoint",
          sortable: true,
          sortField: "period",
          hiddenInToggle: false,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["pm_checkpoint_progress"],
          default: this.isUtilNextPmDefaultShow,
          field: "utilNextPm",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 7,
        },
        {
          label: this.resources["shot_until_next_pm"],
          default: false,
          field: "shotUntilNextPM",
          sortable: true,
        },
        {
          label: this.resources["accumulated_shots_checkpoint"],
          default: this.isLastShotCheckpoint,
          field: "lastShotCheckpoint",
          sortable: true,
        },
        {
          label: this.resources["pm_checkpoint_prediction"],
          default: true,
          field: "pmCheckpointPrediction",
          sortable: true,
          sortField: "dueDate",
        },
        {
          label: this.resources["pm_compliance_rate"],
          default: true,
          field: "pmComplianceRate",
          sortable: true,
          sortField: "executionRate",
        },
        // { label: this.resources['op'], field: 'op', default: true, sortable: true, sortField: 'mold.operatingStatus' },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "toolingStatus",
          defaultPosition: 8,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "sensorStatus",
          defaultPosition: 9,
        },
        // { label: this.resources['status'], field: 'notificationStatus', default: true, sortable: true, sortField: 'notificationStatus' },
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
          label: this.resources["reset.forecastedMaxShots"],
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
          label: this.resources["machine_ton"],
          field: "mold.quotedMachineTonnage",
          sortable: true,
          sortField: "mold.quotedMachineTonnage",
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
        // {label: this.resources['overdue_maintenance_tolerance'], field: 'mold.preventOverdue', sortable: true, sortField: 'mold.preventOverdue'},

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
          label: this.resources["last_maintenance_date"],
          field: "mold.lastMaintenanceDate",
          sortable: true,
          sortField: "mold.lastMaintenanceDate",
        },
        {
          label: this.resources["last_refurbishment_date"],
          field: "mold.lastRefurbishmentDate",
          sortable: true,
          sortField: "mold.lastRefurbishmentDate",
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
      // this.resetColumnsListSelected();
      // this.getColumnListSelected();
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
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },

    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
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
    showChart: function (mold, type = null, dateViewType = null) {
      if (!type) {
        type = "QUANTITY";
      }
      if (!dateViewType) {
        dateViewType = "DAY";
      }

      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, type, dateViewType);
      } else {
        setTimeout(() => {
          this.showChart(mold, type, dateViewType);
        }, 200);
        console.log("cannot found chart mold element");
      }
    },
    showPartChart: function (part, mold) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$parent.$children, "chart-part");
      if (child != null) {
        child.showChartPartMold(part, mold, "QUANTITY", "DAY");
      }
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
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
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
    closeShowDropDown() {
      this.showDropdown = false;
    },
    caculaterPeriod: function (item) {
      console.log("item: ", item);
      const { mold, preventCycle, preventUpcoming } = item;
      const { lastShot } = mold;
      if (
        lastShot - (preventCycle - preventUpcoming) < 0 ||
        preventCycle - preventUpcoming < 0
      ) {
        return this.formatNumber(preventCycle);
      }

      return this.formatNumber(
        (Math.floor(
          (lastShot - (preventCycle - preventUpcoming)) / preventCycle
        ) +
          1) *
          preventCycle
      );
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
      }
      // TODO: recheck listCheckedTracked
      if (!this.listCheckedTracked[this.requestParam.status])
        this.listCheckedTracked[this.requestParam.status] = {};
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },

    tab(status) {
      console.log("function (status)", status);
      this.total = null;
      if (this.allColumnList) {
        this.allColumnList.forEach((c) => {
          switch (c.field) {
            case "failureTime":
              if (["cm", "cm-history"].includes(status)) {
                c.hiddenInToggle = false;
                c.label =
                  "cm" == status
                    ? this.resources["failure_time"]
                    : this.resources["last_failure_time"];
              } else c.hiddenInToggle = true;

              break;
            case "pmCheckpoint":
            case "utilNextPm":
            case "shotUntilNextPM":
              if (["alert"].includes(status)) {
                c.hiddenInToggle = false;
              } else c.hiddenInToggle = true;
              break;
            case "lastShotCheckpoint":
            case "dueDate":
              if (["alert"].includes(status)) {
                c.hiddenInToggle = false;
              } else c.hiddenInToggle = true;
              break;
            case "pmComplianceRate":
            case "preventCycle":
              if (["alert", "done"].includes(status)) {
                c.hiddenInToggle = false;
              } else c.hiddenInToggle = true;
              break;
            case "notificationStatus":
              if (["alert", "cm"].includes(status)) {
                c.hiddenInToggle = false;
              } else c.hiddenInToggle = true;
              break;
            case "mold.lastMaintenanceDate":
            case "mold.lastRefurbishmentDate":
              if (["alert", "cm"].includes(status)) {
                c.enabled = true;
              } else {
                c.enabled = false;
              }
              break;
            case "":
              break;
          }
        });
      }
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },
    disable: function (user) {
      user.enabled = false;
      //this.save(user);
    },
    convertTime: function (s) {
      const dtFormat = new Intl.DateTimeFormat("en-GB", {
        timeStyle: "medium",
        timeZone: "UTC",
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : "";
    },

    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },

    showChart: function (mold, tab) {
      console.log(mold);
      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "QUANTITY", "DAY", tab ? tab : "switch-graph");
      }
    },
    showMoldDetails: function (mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },
    showCorrectiveMaintenanceDetails: function (maintenance) {
      console.log("vao show detail");
      var child = Common.vue.getChild(
        this.$children,
        "corrective-maintenance-details"
      );
      if (child != null) {
        child.showCorrectiveMaintenanceDetails(maintenance);
      }
    },

    //maintenance-history
    showMaintanceHistory: function (maintenance) {
      var child = Common.vue.getChild(this.$children, "maintenance-history");
      if (child != null) {
        child.show(maintenance);
      }
    },

    showMaintanceConfirm: function (mold) {
      var child = Common.vue.getChild(
        this.$children,
        "maintance-confirm-history"
      );
      if (child != null) {
        child.show(mold);
      }
    },
    showCorrectiveMaintenanceHistory: function (maintenance) {
      var child = Common.vue.getChild(
        this.$children,
        "corrective-maintenance-history"
      );
      if (child != null) {
        child.show(maintenance);
      }
    },

    //maintenance-alert-modal
    showMaintenanceAlert: function (maintenance) {
      console.log("maintenance alert");
      var child = Common.vue.getChild(this.$children, "maintenance-alert");
      if (child != null) {
        child.show(maintenance);
      }
    },

    showRequestMaintenance: function (maintenance) {
      console.log("request maintenance");
      var child = Common.vue.getChild(this.$children, "request-maintenance");
      if (child != null) {
        child.show(maintenance);
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
          title: "Maintenance Information",
          messageTitle: "Checklist",
          equipmentCode: data.mold.equipmentCode,
          alertDate: this.formatToDateTime(data.createdAt),
          confirmDate: this.formatToDateTime(data.maintenancedAt),
          name: data.maintenanceBy,
          message: data.checklist,
        };

        child.showMessageDetails(data);
      }
    },
    changeStatus: function (data) {
      var messageFormComponent = Common.vue.getChild(
        this.$children,
        "disapproval-maintenance-form"
      );
      if (messageFormComponent != null) {
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

    paging(pageNumber) {
      var self = this;
      Common.handleNoResults("#app", 1);
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      let urlPath = null;
      const requestParam = JSON.parse(JSON.stringify(self.requestParam));
      if (["cm", "cm-history"].includes(requestParam.status)) {
        urlPath = "/corrective";
        const statusMapping = {
          cm: "alert",
          "cm-history": "history",
        };
        requestParam.status = statusMapping[requestParam.status];
      } else {
        urlPath = "/maintenance";
      }
      var param = Common.param(requestParam);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      self.isLoading = true;
      axios
        .get("/api/molds" + urlPath + "?lastAlert=true&" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          console.log("response11112: ", response);
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
          if (self.total === self.listAllIds.length) {
            self.listChecked = [];
            self.listCheckedFull = [];
          }
          self.results.forEach((result) => {
            // result.maintenancedAtShow = result.maintenancedAt ? moment.unix(result.maintenancedAt).format("YYYY-MM-DD, HH:mm:ss"): "";
            result.maintenancedAtShow = self.convertTimestampToDateWithFormat(
              result.maintenancedAt,
              "YYYY-MM-DD, HH:mm:ss"
            );
          });
          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          self.setResultObject();

          // 카테고리 정보 저장.
          self.setListCategories();

          Common.handleNoResults("#app", self.results.length);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }

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
          self.isLoading = false;
          console.log(error.response);
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

    findMoldFromList: function (results, moldId) {
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

    findMold: function (results, moldId) {
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

    ratio(result) {
      return ((result.mold.lastShot / result.period) * 100).toFixed(1);
    },
    ratioUtilizationRate(result) {
      return ((result.mold.lastShot / result.mold.designedShot) * 100).toFixed(
        1
      );
    },

    ratio2(result) {
      return ((result.lastShotMade / result.mold.preventCycle) * 100).toFixed(
        2
      );
    },
    ratioStyle(result) {
      return "width: " + this.ratio(result) + "%";
    },
    ratioStyleUtilizationRate(result) {
      return "width: " + this.ratioUtilizationRate(result) + "%";
    },
    ratioStyle2(result) {
      return "width: " + this.ratio2(result) + "%";
    },
    ratioColor(result) {
      if (result.maintenanceStatus === "UPCOMING") return "bg-warning";
      else return "bg-danger";
    },
    ratioColorUtilizationRate: function (result) {
      var ratio = this.ratioUtilizationRate(result);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },
    ratioColor2: function (result) {
      if (result.maintenanceStatus === "UPCOMING") return "bg-warning";
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
    sortBy(type, requestParamStatus) {
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
          if (["notificationStatus"].includes(type)) {
            let sortField = ["cm", "cm-history"].includes(requestParamStatus)
              ? "correctiveStatus"
              : "maintenanceStatus";
            this.requestParam.sort = `${sortField},${
              this.isDesc ? "desc" : "asc"
            }`;
          } else
            this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
          this.sort();
        }
      }
    },
    sortByDueDate(type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort((a, b) => a - b);
      }
    },

    sort() {
      this.paging(1);
    },
    showDueDateOld(dueDate) {
      if (dueDate == null) return "";
      let print =
        dueDate == 0
          ? "Today"
          : dueDate > 0
          ? "In " + dueDate + " days"
          : "Over " + -dueDate + " days";
      return print;
    },
    showDueDate(dueDate, maintenanceStatus) {
      if (dueDate == null) return "";
      let dayName = dueDate == 1 ? " day" : " days";
      let print =
        dueDate == 0
          ? "Today"
          : maintenanceStatus == "OVERDUE"
          ? "Over " + dueDate + dayName
          : "In " + dueDate + dayName;

      return print;
    },
    showSystemNoteModal(selectedItem) {
      console.log("@showSystemNoteModal", {
        selectedItem,
        requestPram: this.requestParam,
      });
      const config = {
        objectType: "TOOLING",
        refId: "",
      };

      if (
        [TAB_TYPE.PM, TAB_TYPE.PM_HISTORY].includes(this.requestParam.status)
      ) {
        config.refId = "moldId";
      }
      if (
        [TAB_TYPE.CM, TAB_TYPE.CM_HISTORY].includes(this.requestParam.status)
      ) {
        config.refId = "mold.id";
      }

      const child = this.$root.$refs["system-note"];
      if (child) {
        if (this.listAllIds.length === this.total) {
          console.log("select-all");
          child.showSystemNote(
            this.listAllIds.map((i) => i.mainObjectId),
            true,
            config.objectType
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          const listIds = this.listCheckedFull.map((i) =>
            config.refId.split(".").reduce((prev, curr) => prev[curr], i)
          );
          child.showSystemNote(listIds, true, config.objectType);
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          const listIds = this.listCheckedFull.map((i) =>
            config.refId.split(".").reduce((prev, curr) => prev[curr], i)
          );
          child.showSystemNote(
            { id: listIds[0], objectType: config.objectType },
            false
          );
        } else {
          console.log("single select in history tab");
          const id = config.refId
            .split(".")
            .reduce((prev, curr) => prev[curr], selectedItem);
          child.showSystemNote({ id, objectType: config.objectType });
        }
      } else {
        if (this.listAllIds.length === this.total) {
          console.log("select-all");
          this.showNote(
            this.listAllIds.map((i) => i.mainObjectId),
            true,
            config.objectType
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          const listIds = this.listCheckedFull.map((i) =>
            config.refId.split(".").reduce((prev, curr) => prev[curr], i)
          );
          this.showNote(listIds, true, config.objectType);
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          const listIds = this.listCheckedFull.map((i) =>
            config.refId.split(".").reduce((prev, curr) => prev[curr], i)
          );
          this.showNote(
            { id: listIds[0], objectType: config.objectType },
            false
          );
        } else {
          console.log("single select in history tab");
          const id = config.refId
            .split(".")
            .reduce((prev, curr) => prev[curr], selectedItem);
          this.showNote({ id, objectType: config.objectType });
        }
      }
    },
    getUnitName(unit) {
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
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
        return "";
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
        return "";
      }
    },
    changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
      if (isInit === false) delete this.requestParam.routeType; //clean
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
      if (specialAlertType != null) {
        this.requestParam.specialAlertType = specialAlertType;
      }
      if (specialAlertType != null) this.paging(1);
    },
    async showWorkOrderDetail(workOrder) {
      const id = workOrder.id;
      this.showCreatedWorkorder = true;
      try {
        const response = await axios.get(`/api/work-order/${id}`);
        this.selectedWorkOrder = response.data.data;
      } catch (error) {
        console.log(error);
        this.showDetailModal = false;
      } finally {
        this.isLoading = false;
      }
    },
    async showCompleteWorkOrder(workOrder) {
      const id = workOrder.id;
      try {
        const response = await axios.get(`/api/work-order/${id}`);
        this.selectedWorkOrder = response.data.data;
        this.showComplete = true;
      } catch (error) {
        console.log(error);
        this.showComplete = false;
      } finally {
        this.isLoading = false;
      }
    },
    openEditWorkOrder() {
      this.showComplete = false;
    },
    handleCloseWorkOrder() {
      this.showCreatedWorkorder = false;
    },
    handleCloseCompleteWorkOrder() {
      this.showComplete = false;
    },
    async handleSubmitComplete(formData, isProgress) {
      try {
        console.log("@handleSubmitComplete:formData", formData);
        await axios.post("/api/work-order/complete", formData);
        this.handleCloseCompleteWorkOrder();
        if (!isProgress) {
          const notification = {};
          notification.title = MESSAGES.COMPLETE_SUCCESS.TITLE;
          notification.content = MESSAGES.COMPLETE_SUCCESS.CONTENT;
          notification.show = true;
          this.setToastAlertGlobal(notification);
        }
      } catch (error) {
        console.log(error);
        Common.alert("error");
      }
    },
    handleShowToast(toast) {
      this.setToastAlertGlobal(toast);
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

      // TODO: NEED RECHECK listCheckedTracked
      // this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
      console.log("@handleSelectAll", {
        actionType,
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
        listCheckedTracked: this.listCheckedTracked,
      });
    },
    async fetchAllListIds() {
      try {
        // TODO: REMERGE PAGE_TYPE, currently using 2 page type independently
        const requestParam = JSON.parse(JSON.stringify(this.requestParam));
        let __pageType = "";
        if (["cm", "cm-history"].includes(requestParam.status)) {
          __pageType = "CORRECTIVE_MAINTENANCE_ALERT";
          const statusMapping = {
            cm: "alert",
            "cm-history": "history",
          };
          requestParam.status = statusMapping[requestParam.status];
        } else {
          __pageType = "MACHINE_DOWNTIME_ALERT";
        }
        var param = Common.param({ ...requestParam, pageType: __pageType });
        const response = await axios.get(
          "/api/batch/all-ids?lastAlert=true&" + param
        );
        console.log("response.data", response.data);
        this.listAllIds = response.data.data;
      } catch (error) {
        console.log(error);
      }
    },
  },
  mounted() {
    this.$nextTick(function () {
      if (this.searchText) {
        this.requestParam.query = this.searchText;
      }
      Common.removeWave(500);
    });

    // this.$nextTick(function () {
    // 모든 화면이 렌더링된 후 실행합니다.
    var self = this;
    var uri = URI.parse(location.href);
    let params = Common.parseParams(uri.query);
    if (params.dashboardRedirected) {
      self.requestParam.dashboardRedirected = params.dashboardRedirected;
    }
    console.log("uri: ", uri);
    console.log("fragment", uri.fragment);
    if (uri.fragment === "cm" || uri.fragment === "cm-history") {
      self.tab(uri.fragment);
    }

    if (uri.fragment === "overdue") {
      self.requestParam.routeType = "OVERDUE";
    }
    if (params.routeType) {
      console.log("routeType", params.routeType);
      this.requestParam.routeType = params.routeType;
    }
    if (params.tabbedDashboardRedirected) {
      this.requestParam.tabbedDashboardRedirected =
        params.tabbedDashboardRedirected;
    }
    if (params.continentName) {
      this.requestParam.continentName = params.continentName;
    }
    console.log("self.requestParam.routeType", self.requestParam.routeType);
  },
};
</script>

<style>
.steps {
  margin: 0 auto 30px;
  width: fit-content;
}
</style>
