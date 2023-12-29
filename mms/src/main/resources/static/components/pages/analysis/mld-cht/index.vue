<template>
  <div>
    <div class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave">
          <div class="card-header">
            <strong>{{ resources["tooling"] }}</strong>
            <div class="card-header-actions">
              <span class="card-header-action">
                <small class="text-muted">
                  <span>{{ resources["total"] + ":" }}</span>
                  {{ total }}
                </small>
              </span>
            </div>
          </div>
          <div class="card-body">
            <input type="hidden" v-model="requestParam.sort" />

            <div class="form-row">
              <div class="col-md-12 col-12">
                <div class="form-row">
                  <div class="col-md-8 mb-3 mb-md-0 col-12">
                    <common-searchbar
                      :placeholder="resources['search_by_selected_column']"
                      :request-param="requestParam"
                      :on-search="search"
                    ></common-searchbar>
                  </div>

                  <div class="col-md-2 mb-3 mb-md-0">
                    <select
                      class="form-control"
                      v-model="requestParam.operatingStatus"
                      @change="search"
                    >
                      <option value="" selected disabled hidden>
                        {{ resources["op"] }}
                      </option>
                      <option value="WORKING">{{ resources["active"] }}</option>
                      <option value="IDLE">{{ resources["idle"] }}</option>
                      <option value="NOT_WORKING">
                        {{ resources["inactive"] }}
                      </option>
                      <option value="DISCONNECTED">
                        {{ resources["disconnected"] }}
                      </option>
                      <option value="ALL">{{ resources["all"] }}</option>
                    </select>
                  </div>

                  <div class="col-md-2 mb-3 mb-md-0">
                    <select
                      class="form-control"
                      v-model="requestParam.equipmentStatus"
                      @change="search"
                    >
                      <option value="" selected disabled hidden>
                        {{ resources["status"] }}
                      </option>
                      <!-- <template v-for="code in codes.EquipmentStatus">
                                                    <option :value="code.code">{{ code.title }}</option>
                                                </template> -->
                      <option
                        v-for="code in codes.EquipmentStatus"
                        :value="code.code"
                      >
                        {{ code.title }}
                      </option>
                      <option value="ALL">{{ resources["all"] }}</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer" style="display: none">
            <button class="btn btn-sm btn-primary" type="submit">
              <i class="fa fa-dot-circle-o"></i>
              <span>{{ resources["submit"] }}</span>
            </button>
            <button class="btn btn-sm btn-danger" type="reset">
              <i class="fa fa-ban"></i> <span>{{ resources["reset"] }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-lg-12">
        <div class="card wave1">
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span>{{ resources["striped_tbl"] }}</span>
          </div>
          <div style="overflow-x: scroll" class="card-body">
            <div class="mb-3">
              <op-status
                v-if="Object.entries(resources).length"
                :resources="resources"
              ></op-status>
            </div>
            <div class="card-tool">
              <ul
                class="nav nav-tabs tooling-tab-hint"
                style="margin-bottom: -1px"
              >
                <li style="min-width: 75px" class="nav-item">
                  <a
                    class="nav-link"
                    :class="{ active: requestParam.status == 'all' }"
                    href="#"
                    @click.prevent="tab('all')"
                    ><span>{{ resources["all"] }}</span>
                    <span
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status == 'all'"
                      >{{ total }}</span
                    >
                  </a>
                </li>
                <li class="nav-item" v-if="checkDyson !== 'dyson'">
                  <a
                    class="nav-link"
                    :class="{ active: requestParam.tabType == 'DIGITAL' }"
                    href="#"
                    @click.prevent="tab('DIGITAL')"
                    ><span>{{ resources["digital"] }}</span>
                    <span
                      class="badge badge-light badge-pill"
                      v-if="requestParam.tabType == 'DIGITAL'"
                      >{{ total }}</span
                    >
                  </a>
                </li>
                <li class="nav-item" v-if="checkDyson !== 'dyson'">
                  <a
                    class="nav-link"
                    :class="{ active: requestParam.tabType == 'NON_DIGITAL' }"
                    href="#"
                    @click.prevent="tab('NON_DIGITAL')"
                    ><span>{{ resources["non_digital"] }}</span>
                    <span
                      class="badge badge-light badge-pill"
                      v-if="requestParam.tabType == 'NON_DIGITAL'"
                      >{{ total }}</span
                    >
                  </a>
                </li>

                <li
                  class="nav-item dropdown"
                  v-for="code in checkDyson == 'dyson'
                    ? codes.DataSubmission
                    : []"
                  v-if="code.enabled"
                >
                  <a
                    class="nav-link"
                    :class="{ active: requestParam.status == code.code }"
                    href="#"
                    @click.prevent="tab(code.code)"
                    >{{ code.title }}
                    <span
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status == code.code"
                      >{{ total }}</span
                    >
                  </a>
                </li>
                <li class="nav-item">
                  <a
                    class="nav-link"
                    :class="{ active: requestParam.status == 'DELETED' }"
                    href="#"
                    @click.prevent="tab('DELETED')"
                    ><span>{{ resources["disabled"] }}</span>
                    <span
                      class="badge badge-light badge-pill"
                      v-if="requestParam.status == 'DELETED'"
                      >{{ total }}</span
                    >
                  </a>
                </li>
              </ul>
              <div class="btn-group btn-group-select-column">
                <div>
                  <emdn-cta-button
                    :click-handler="
                      () => (isVisibleDropdown = !isVisibleDropdown)
                    "
                    button-type="dropdown"
                    color-type="blue-fill"
                    :active="isVisibleDropdown"
                  >
                    {{ resources["new_tooling"] }}
                  </emdn-cta-button>
                  <emdn-dropdown
                    v-if="isVisibleDropdown"
                    :checkbox="false"
                    :visible="true"
                    :items="dropdownItems"
                    :click-handler="
                      (item) => (
                        item.clickHandler(), (isVisibleDropdown = false)
                      )
                    "
                    :on-close="() => console.log('on close !!')"
                  ></emdn-dropdown>
                </div>
              </div>
              <customization-modal
                style="
                  border-bottom: 1px solid rgb(200, 206, 211);
                  margin-bottom: -1px;
                "
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              />
            </div>

            <table
              style="overflow-x: auto !important"
              class="table table-responsive-sm table-striped"
            >
              <colgroup>
                <col />
                <col />
                <col />
                <col />
                <col />
                <col />
              </colgroup>
              <thead id="thead-actionbar" class="custom-header-table">
                <tr
                  :class="{ invisible: listChecked.length != 0 }"
                  style="height: 57px"
                  class="header-s tr-sort"
                >
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <input
                        id="checkbox-all"
                        v-if="allColumnList.length > 0"
                        class="checkbox"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </div>
                  </th>
                  <template
                    v-for="(item, index) in allColumnList"
                    v-if="
                      (item.enableTemp ||
                        (!item.disableTemp && item.enabled)) &&
                      !item.hiddenInToggle
                    "
                  >
                    <th
                      v-if="item.sortField === 'lastShot'"
                      v-click-outside="closeLastShot"
                      :class="[
                        {
                          __sort: item.sortable,
                          active: currentSortType === item.sortField,
                        },
                        isDesc ? 'desc' : 'asc',
                        item.label === 'OP' ||
                        item.label === 'Status' ||
                        item.field === 'dataSubmission'
                          ? ''
                          : 'text-left',
                      ]"
                      @click="sortBy(item.sortField)"
                    >
                      <span> {{ item.label }} </span>
                      <span v-if="item.sortable" class="__icon-sort"></span>
                      <last-shot-filter
                        v-show="
                          lastShotDropdown && item.sortField === 'lastShot'
                        "
                        :last-shot-data="lastShotData"
                        @update-variable="updateVariable"
                      ></last-shot-filter>
                    </th>
                    <th
                      v-else
                      :class="[
                        {
                          __sort: item.sortable,
                          active: currentSortType === item.sortField,
                        },
                        isDesc ? 'desc' : 'asc',
                        item.label === 'OP' ||
                        item.label === 'Status' ||
                        item.field === 'dataSubmission'
                          ? ''
                          : 'text-left',
                      ]"
                      @click="sortBy(item.sortField)"
                    >
                      <span>{{ item.label }}</span>
                      <span v-if="item.sortable" class="__icon-sort"></span>
                    </th>
                  </template>
                </tr>

                <tr
                  :class="{ 'd-none zindexNegative': listChecked.length == 0 }"
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
                        <div
                          v-if="listChecked.length == 1"
                          @click="goToEdit"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length > 0"
                          class="action-item"
                          v-click-outside="closeShowDropDown"
                        >
                          <div class="change-status-dropdown drowdown d-inline">
                            <template v-if="listChecked.length == 1">
                              <template
                                v-if="
                                  listCheckedFull[0].equipmentStatus ==
                                  'DISCARDED'
                                "
                              >
                                <div
                                  title="Change status"
                                  class="change-status"
                                  @click.prevent="
                                    showCorrective(
                                      listCheckedFull[0],
                                      'FAILURE'
                                    )
                                  "
                                >
                                  <span class="change-status-title"
                                    >Change Status</span
                                  >
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                              </template>
                              <template
                                v-else-if="
                                  listCheckedFull[0].equipmentStatus ==
                                  'FAILURE'
                                "
                              >
                                <div
                                  title="Change status"
                                  class="change-status"
                                  @click="showDropdown = !showDropdown"
                                >
                                  <span class="change-status-title"
                                    >Change Status</span
                                  >
                                  <i class="icon-action change-status-icon"></i>
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
                                  <span class="change-status-title"
                                    >Change Status</span
                                  >
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                                <ul
                                  class="dropdown-menu"
                                  :class="[showDropdown ? 'show' : '']"
                                >
                                  <li
                                    v-for="code in codes.EquipmentStatus"
                                    v-if="
                                      code.code != 'INSTALLED' &&
                                      code.code !=
                                        listCheckedFull[0].equipmentStatus &&
                                      listCheckedFull[0].equipmentStatus !=
                                        'FAILURE'
                                    "
                                  >
                                    <div
                                      class="dropdown-item"
                                      @click.prevent="
                                        showDropdown = false;
                                        showCorrective(
                                          listCheckedFull[0],
                                          code.code
                                        );
                                      "
                                      v-if="code.code == 'FAILURE'"
                                    >
                                      {{ resources["failure"] }}
                                    </div>
                                    <div
                                      class="dropdown-item"
                                      @click.prevent="
                                        showDropdown = false;
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        );
                                      "
                                      v-if="
                                        code.code != 'AVAILABLE' &&
                                        code.code != 'FAILURE' &&
                                        code.code != 'DETACHED'
                                      "
                                    >
                                      {{ code.title }}
                                    </div>
                                  </li>
                                  <li
                                    v-if="
                                      listCheckedFull[0].equipmentStatus ==
                                      'FAILURE'
                                    "
                                  >
                                    <div
                                      class="dropdown-item"
                                      @click.prevent="
                                        showDropdown = false;
                                        alert('');
                                      "
                                    >
                                      -
                                    </div>
                                  </li>
                                </ul>
                              </template>
                            </template>
                            <template v-else>
                              <div
                                title="Change status"
                                class="change-status"
                                @click="showDropdown = !showDropdown"
                              >
                                <span class="change-status-title"
                                  >Change Status</span
                                >
                                <i class="icon-action change-status-icon"></i>
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
                          </div>
                        </div>
                        <div
                          v-if="listChecked.length == 1"
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1"
                          class="action-item"
                          @click="showSystemNoteModal(listChecked[0])"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            requestParam.status != 'DELETED'
                          "
                          class="action-item"
                          @click.prevent="disableList(listCheckedFull)"
                        >
                          <span>Disable</span>
                          <i class="icon-action disable-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            requestParam.status === 'DELETED'
                          "
                          class="action-item"
                          @click.prevent="enableList(listCheckedFull)"
                        >
                          <span>Enable</span>
                          <i class="icon-action enable-icon"></i>
                        </div>

                        <div
                          v-click-outside="closeShowExport"
                          v-if="listChecked.length >= 1"
                          @click="showExport = !showExport"
                          class="action-item"
                        >
                          <div
                            class="change-status-dropdown drowdown action-item"
                          >
                            <span>Export</span>
                            <i class="icon-action export-icon"></i>
                          </div>
                          <ul
                            style="left: auto !important"
                            class="dropdown-menu"
                            :class="[showExport ? 'show' : '']"
                          >
                            <li>
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="openExportModal"
                              >
                                Dynamic Data
                              </span>
                            </li>
                            <li>
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="downloadTooling(EXPORT_TYPE.XLSX)"
                              >
                                Static Data
                              </span>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </th>
                </tr>
              </thead>

              <tbody class="op-list" style="display: none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                  <td class="custom-td">
                    <template v-if="listChecked.length >= 0">
                      <input
                        @click="(e) => check(e, result)"
                        :checked="checkSelect(result.id)"
                        class="checkbox"
                        type="checkbox"
                        :id="result.id + 'checkbox'"
                        :value="result.id"
                      />
                    </template>
                  </td>
                  <template
                    v-for="column in allColumnList"
                    v-if="
                      (column.enableTemp ||
                        (!column.disableTemp && column.enabled)) &&
                      !column.hiddenInToggle
                    "
                  >
                    <td class="text-left" v-if="column.field === 'toolingId'">
                      <div>
                        <a
                          href="#"
                          @click.prevent="showChart(result)"
                          style="color: #1aaa55"
                          class="mr-1 font-size-14"
                        >
                          <i class="fa fa-bar-chart"></i>
                        </a>
                        <a href="#" @click.prevent="showMoldDetails(result)">
                          {{ result.equipmentCode }}
                        </a>
                      </div>

                      <div class="small text-muted font-size-11-2">
                        <span>{{ resources["updated_on"] }}</span>
                        {{ formatToDate(result.lastShotAt) }}
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'part'"
                      style="max-width: 600px"
                    ></td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'company'"
                    >
                      <a
                        class="font-size-14"
                        href="#"
                        @click.prevent="
                          showCompanyDetailsById(result.companyIdByLocation)
                        "
                      >
                        {{ result.companyName }}
                      </a>
                      <div class="small text-muted font-size-11-2">
                        {{ result.companyCode }}
                      </div>
                    </td>

                    <td
                      class="text-left"
                      v-else-if="column.field === 'location'"
                    >
                      <div v-if="result.locationCode">
                        <div class="font-size-14">
                          <a
                            href="#"
                            @click.prevent="showLocationHistory(result)"
                          >
                            {{ result.locationName }}
                          </a>
                        </div>
                        <div class="small text-muted font-size-11-2">
                          {{ result.locationCode }}
                        </div>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'lastShot'"
                    >
                      <span>{{ formatNumber(result.accumulatedShot) }}</span>
                      <div>
                        <span class="font-size-11-2">shots</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'cycleTime'"
                    >
                      {{
                        result.lastCycleTime
                          ? Math.round(
                              (result.lastCycleTime / 10 + Number.EPSILON) * 100
                            ) / 100
                          : 0
                      }}
                      <div>
                        <span class="font-size-11-2">seconds</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'weightedAverageCycleTime'"
                    >
                      {{
                        result.weightedAverageCycleTime
                          ? Math.round(
                              (result.weightedAverageCycleTime / 10 +
                                Number.EPSILON) *
                                100
                            ) / 100
                          : 0
                      }}
                      <div>
                        <span class="font-size-11-2">seconds</span>
                      </div>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'op'"
                    >
                      <span
                        v-if="
                          result.operatingStatus == 'WORKING' &&
                          ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                            result.equipmentStatus
                          )
                        "
                        class="op-active label label-success"
                      ></span>
                      <span
                        v-if="
                          result.operatingStatus == 'IDLE' &&
                          ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                            result.equipmentStatus
                          )
                        "
                        class="op-active label label-warning"
                      ></span>
                      <span
                        v-if="
                          result.operatingStatus == 'NOT_WORKING' &&
                          ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                            result.equipmentStatus
                          )
                        "
                        class="op-active label label-inactive"
                      ></span>
                      <span
                        v-if="
                          result.operatingStatus == 'DISCONNECTED' &&
                          ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                            result.equipmentStatus
                          )
                        "
                        class="op-active label label-danger"
                      ></span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'equipmentStatus'"
                    >
                      {{ result.equipmentStatus }}
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'cycleTimeToleranceL1'"
                    >
                      <span class="font-size-14">{{
                        result.cycleTimeLimit1
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.cycleTimeLimit1
                            ? handleCycleTimeTolerance1(result)
                            : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'contractedCycleTimeSeconds'"
                    >
                      <span class="font-size-14">{{
                        result.contractedCycleTimeSeconds
                      }}</span>
                      <div>
                        <span class="font-size-11-2">seconds</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'cycleTimeToleranceL2'"
                    >
                      <span class="font-size-14">{{
                        result.cycleTimeLimit2
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.cycleTimeLimit2
                            ? handleCycleTimeTolerance2(result)
                            : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'uptimeTarget'"
                    >
                      <span class="font-size-14" style="margin-left: 3px"
                        >{{ result.uptimeTarget }}
                        <span class="font-size-11-2">{{
                          result.uptimeTarget ? "%" : ""
                        }}</span>
                      </span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-left"
                      v-else-if="column.field === 'engineerInCharge'"
                    >
                      <user-list-cell
                        :resources="resources"
                        :user-list="result.engineers"
                      ></user-list-cell>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'utilizationRate'"
                    >
                      <div v-if="result.lastShot">
                        <div class="clearfix">
                          <div class="float-left font-size-14">
                            <strong>{{ formatNumber(result.lastShot) }}</strong>
                            <span
                              >/ {{ formatNumber(result.designedShot) }}</span
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
                    <td
                      class="text-left"
                      v-else-if="column.field === 'weightOfRunnerSystem'"
                    >
                      <span class="font-size-14">
                        {{ result.weightRunner ? result.weightRunner : "" }}
                      </span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.weightRunner
                            ? getUnitName(result.weightUnitTitle)
                            : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'designedShot'"
                    >
                      <span class="font-size-14">{{
                        result.designedShot
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.designedShot ? "shots" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'quotedMachineTonnage'"
                    >
                      <span class="font-size-14">{{
                        result.quotedMachineTonnage
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.quotedMachineTonnage ? "tonnes" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'preventCycle'"
                    >
                      <span class="font-size-14">{{
                        result.preventCycle
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.preventCycle ? "shots" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'maxCapacityPerWeek'"
                    >
                      <span class="font-size-14">{{
                        result.maxCapacityPerWeek
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.maxCapacityPerWeek ? "shots" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'preventOverdue'"
                    >
                      <span class="font-size-14">{{
                        result.preventOverdue
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.preventOverdue ? "shots" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'shiftsPerDay'"
                    >
                      <span class="font-size-14">{{
                        result.shiftsPerDay
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.shiftsPerDay ? "hours" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'productionDays'"
                    >
                      <span class="font-size-14">{{
                        result.productionDays
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.productionDays ? "days" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'toolingSizeView'"
                    >
                      <span class="font-size-14">{{ result.size }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.toolingSizeView
                            ? getUnitName(result.sizeUnit)
                            : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'toolingWeightView'"
                    >
                      <span class="font-size-14">{{ result.weight }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.toolingWeightView
                            ? getUnitName(result.weightUnit)
                            : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'preventUpcoming'"
                    >
                      <span class="font-size-14">{{
                        result.preventUpcoming
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.preventUpcoming ? "shots" : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'uptimeLimitL1'"
                    >
                      <span class="font-size-14" style="margin-left: 3px"
                        >{{ result.uptimeLimitL1 }}
                        <span class="font-size-11-2">{{
                          result.uptimeLimitL1 ? "%" : ""
                        }}</span>
                      </span>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'uptimeLimitL2'"
                    >
                      <span class="font-size-14" style="margin-left: 3px"
                        >{{ result.uptimeLimitL2 }}
                        <span class="font-size-11-2">{{
                          result.uptimeLimitL2 ? "%" : ""
                        }}</span>
                      </span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-left"
                      v-else-if="column.field === 'dataSubmission'"
                    >
                      <div
                        :style="{
                          backgroundColor:
                            result.dataSubmission == 'PENDING'
                              ? '#ac24d3'
                              : result.dataSubmission == 'APPROVED'
                              ? '#26a958'
                              : '#b8b8b8',
                        }"
                        class="data-submission"
                      >
                        {{
                          result.dataSubmission == "PENDING"
                            ? "Pending"
                            : result.dataSubmission == "APPROVED"
                            ? "Approved"
                            : "Disapproved"
                        }}
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'inactivePeriod'"
                    >
                      <div style="font-size: 14px">
                        {{ result.inactivePeriod }}
                      </div>
                      <span style="font-size: 11.2px">
                        {{ result.inactivePeriod > 1 ? "Months" : "Month" }}
                      </span>
                    </td>
                    <td class="text-left" v-else-if="column.isCustomField">
                      {{ customFieldValue(result, column.field) }}
                    </td>
                    <td class="text-left" v-else>{{ result[column.field] }}</td>
                  </template>
                </tr>
              </tbody>
            </table>

            <div class="no-results d-none">{{ resources["no_results"] }}</div>

            <div class="row">
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
            <create-property-drawer
              :resources="resources"
              :visible="visible"
              @open="showDrawer()"
              @close="closeDrawer()"
              is-type="TOOLING"
            ></create-property-drawer>

            <button
              type="button"
              class="btn btn-primary"
              data-toggle="modal"
              data-target="#exampleModal"
              id="toggleExportProgressBar"
              style="display: none"
            >
              <span>{{ resources["demo_model"] }}</span>
            </button>

            <!-- Modal -->
            <div
              class="modal fade"
              id="exampleModal"
              tabindex="-1"
              role="dialog"
              aria-labelledby="exampleModalLabel"
              aria-hidden="true"
              style="top: 30%"
            >
              <div class="modal-dialog" role="document" style="width: 350px">
                <div class="modal-content" style="border: none">
                  <div class="modal-header">
                    <h5
                      class="modal-title"
                      id="exampleModalLabel"
                      style="font-weight: bold"
                    >
                      {{ resources["exporting_data"] }}
                    </h5>
                    <button
                      type="button"
                      class="close"
                      data-dismiss="modal"
                      aria-label="Close"
                      @click="stopExport"
                    >
                      <span aria-hidden="true">&times;</span>
                    </button>
                  </div>
                  <div class="modal-body" style="height: 100px">
                    <div style="text-align: center">
                      <p style="color: #615ea9">
                        {{ resources["loading"] + "..." }}
                      </p>
                    </div>
                    <div id="myProgress">
                      <div id="myBar" style="display: inline-block"></div>
                      <div
                        :style="`
                                                    display: inline-block;
                                                    width: 24px;
                                                    height: 24px;
                                                    background: #615ea9;
                                                    margin-left: -16px;
                                                    border-radius: 50%;
                                                `"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <company-details :resources="resources"></company-details>
    <chart-part
      :show-file-previewer="
        (file) => {
          showFilePreviewer(file, 'chart-part');
        }
      "
      :resources="resources"
    ></chart-part>

    <chart-modal
      :key="chartMoldNewKey"
      v-if="moldChartVisible"
      :close-handler="closeModal"
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
      :mold="mold"
      :oct="oct"
      :date-view-type="dateViewType"
      :type="type"
    ></chart-modal>

    <mold-details
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></mold-details>
    <file-previewer :back="backToMoldDetail"></file-previewer>
    <corrective-form :resources="resources"></corrective-form>
    <location-history :resources="resources"></location-history>
    <delete-popup
      :confirm-delete="confirmDelete"
      :is-show-warning-again="isShowWarningAgain"
      :list="listChecked"
      :resources="resources"
    ></delete-popup>
    <revision-history :resources="resources"></revision-history>
    <system-note
      system-note-function="TOOLING_SETTING"
      :resources="resources"
    ></system-note>
    <template v-for="(feature, index) in featureList" :key="index">
      <action-bar-feature
        :feature="feature"
        @feature-notify-done="featureNotifyDone"
      ></action-bar-feature>
    </template>
    <export-data-modal
      v-if="showExportModal"
      :show-export-modal="showExportModal"
      @export-dynamic="exportDynamic"
      @close-export="closeExport"
      :resources="resources"
    ></export-data-modal>
  </div>
</template>

<script>
var Page = Common.getPage("molds");

const EXPORT_TYPE = {
  PDF: "PDF",
  XLSX: "XLSX",
};

module.exports = {
  name: "mld-cht-page",
  components: {
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "chart-modal": httpVueLoader(
      "/components/pages/analysis/mld-cht/chart-modal.vue"
    ),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    "corrective-form": httpVueLoader("/components/corrective-form.vue"),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "location-history": httpVueLoader("/components/location-history.vue"),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "op-status": httpVueLoader("/components/configuration/OpStatus.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "export-data-modal": httpVueLoader("/components/export-data-modal.vue"),
  },
  data() {
    return {
      isVisibleDropdown: false,
      dropdownItems: [],
      chartMoldNewKey: 1,
      oct: {},
      type: "",
      dateViewType: "",
      mold: {},
      moldChartVisible: false,
      isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
      deletePopup: false,
      resources: {},
      checkDyson: Common.checkDyson,
      showDrowdown: null,
      showDropdown: false,
      errorFieldName: false,
      usetType: "",
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        id: "",
        query: "",
        status: "all",
        sort: "lastShotAt,desc",
        page: 1,
        operatingStatus: "",
        equipmentStatus: "",
        locationName: "",
        pageType: "TOOLING_SETTING",
        companyId: "",
        tabType: "",
      },
      firstCompare: "eq",
      secondCompare: "and",
      thirdCompare: "eq",
      firstCaviti: "",
      secondCaviti: "",
      counterId: "",
      partId: "",
      equipmentStatusCodes: [],
      listCategories: [],
      codes: {},
      sortType: {
        TOOLING_ID: "equipmentCode",
        PART: "part",
        COMPANY: "location.company.name",
        LOCATION: "location.name",
        LAST_SHOT: "lastShot",
        OP: "operatingStatus",
        STATUS: "equipmentStatus",
        DATA_SUBMISSION: "dataSubmission",
      },
      currentSortType: "lastShotAt",
      isDesc: true,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      pageType: "TOOLING_SETTING",
      allColumnList: [],
      optionParams: {},
      visible: false,
      dropdownOpening: false,
      objectType: "TOOLING",
      listCheckedFull: [],
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false,
      configDeleteField: {},
      showExportModal: false,
      dataExport: "",
      showExport: false,
      EXPORT_TYPE: EXPORT_TYPE,
      selectedDownloadType: "default",
      currentIntervalIds: [],
      isCallingDone: false,
      isStopedExport: false,
      dashboardChartType: null,
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      featureList: ["ACTION_BAR", "TOOLING_TAB"],
      // visiblePopover: false,
      displayCaret: false,
      showAnimation: false,
    };
  },
  watch: {
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const x = this.results.filter(
          (item) => this.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
    // visiblePopover(newValue, oldValue) {
    //     if (newValue == false && oldValue == true) {
    //         this.displayCaret = false;
    //     }
    // },
  },
  methods: {
    animationOutlineDropdown() {
      this.displayCaret = true;
      // if (!this.visiblePopover) {
      //     this.showAnimation = true;
      //     setTimeout(() => {
      //         this.showAnimation = false;
      //     }, 700);
      // }
    },
    featureNotifyDone(feature) {
      this.featureList = this.featureList.filter((f) => f !== feature);
      if (this.featureList.length > 0) {
        Common.triggerShowNewFeatureNotify(
          this.$children,
          "action-bar-feature",
          null,
          {
            key: "feature",
            value: this.featureList[0],
          }
        );
      }
    },
    updateVariable(data) {
      this.lastShotData = data;
    },
    closeLastShot() {
      if (this.lastShotDropdown) {
        this.requestParam.accumulatedShotFilter =
          (this.lastShotData && this.lastShotData.filter) || "";
        if (this.lastShotData && this.lastShotData.sort) {
          this.currentSortType = "lastShot";
          this.isDesc = this.lastShotData.sort === "desc";
          this.requestParam.sort = `${
            this.lastShotData.filter === null
              ? "lastShot"
              : `accumulatedShot.${this.lastShotData.filter}`
          },${this.isDesc ? "desc" : "asc"}`;
        }
        this.sort();
        this.lastShotDropdown = false;
      }
    },
    enableList(items) {
      items.forEach((item) => {
        item.enabled = true;
      });
      this.saveBatch(items, true);
    },
    disableList(items) {
      items.forEach((item) => {
        item.enabled = false;
      });
      this.saveBatch(items, false);
    },
    saveBatch(item, boolean) {
      const idArr = item.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      axios.post("/api/molds/change-status-in-batch", param).then(
        (response) => {
          this.paging(1);
        },
        (error) => {
          console.log(error.response);
        },
        () => {
          this.listChecked = [];
          this.listCheckedTracked[this.requestParam.status] = [];
          this.isAll = false;
          this.showDropdown = false;
        }
      );
    },
    progressBarRun() {
      this.currentIntervalIds.forEach((id) => {
        clearInterval(id);
      });
      let $vm = this;
      let flag = 0;
      if (flag == 0) {
        flag = 1;
        var elem = document.getElementById("myBar");
        elem.style.width = "0%";
        var width = 1;
        var id = setInterval(frame, 40);

        this.currentIntervalIds = [];
        this.currentIntervalIds.push(id);

        function frame() {
          if (width >= 100) {
            clearInterval(id);
            flag = 0;
            document.getElementById("toggleExportProgressBar").click();
          } else {
            if ($vm.isStopedExport) {
              document.getElementById("toggleExportProgressBar").click();
              $vm.currentIntervalIds.forEach((id) => {
                clearInterval(id);
              });
            }
            if ($vm.isCallingDone) {
              width = width + 1;
            }
            if (width < 30) {
              width = width + 0.5;
            } else if (width >= 30 && width < 50) {
              width = width + 0.3;
            } else if (width >= 50 && width < 75) {
              width = width + 0.1;
            } else if (width >= 75 && width < 85) {
              width = width + 0.05;
            } else if (width >= 85 && width < 99.5) {
              width = width + 0.01;
            }
            elem.style.width = width + "%";
          }
        }
      }
    },

    stopExport() {
      this.isStopedExport = true;
    },
    downloadToolingDynamic(type, timeWaiting) {
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;

      let path = "";

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

      let downloadParams = listChecked.join(",");

      if (type === EXPORT_TYPE.PDF) {
        path = "exportPdfDetailMolds";
      } else {
        path = "exportExcel";
      }
      setTimeout(() => {
        this.isStopedExport = false;
        this.isCallingDone = false;
        $vm.closeExport();

        document.getElementById("toggleExportProgressBar").click();
        this.progressBarRun();
      }, Common.getNumMillisecondsToTime(timeWaiting));
      let $vm = this;
      let sort = this.requestParam.sort
        ? `&sort=${this.requestParam.sort}`
        : ``;
      let params = null;
      if (this.dataExport.rangeType == "CUSTOM_RANGE") {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ? `${this.requestParam.sort}` : ``,
          rangeType: this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          fromDate: this.dataExport.startDate,
          toDate: this.dataExport.endDate,
        };
      } else {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ? `${this.requestParam.sort}` : ``,
          rangeType: this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          time: this.dataExport.time,
        };
      }
      const params2 = Common.param(params);
      let url = `/api/molds/exportExcelDynamicNew?` + params2;
      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          setTimeout(() => {
            if (!$vm.isStopedExport) {
              $vm.isCallingDone = true;
              setTimeout(() => {
                let headerLine = response.headers["content-disposition"];
                if (headerLine != null)
                  fileName =
                    headerLine.split("filename=")[1] || fileName + ".xlsx";

                var blob = new Blob([response.data], {
                  type: "text/plain;charset=utf-8",
                });
                saveAs(blob, `${fileName}`);
              }, 2000);
            }
          }, Common.getNumMillisecondsToTime(timeWaiting) + 10);
        },
        (error) => {
          setTimeout(() => {
            $vm.isStopedExport = true;
            error.response.data.text().then((text) => Common.alert(text));
          }, Common.getNumMillisecondsToTime(timeWaiting) + 1000);
        }
      );
    },
    exportDynamic(data, timeWaiting) {
      this.dataExport = data;
      this.downloadToolingDynamic(EXPORT_TYPE.XLSX, timeWaiting);
    },

    openExportModal() {
      this.showExportModal = true;
      let root = null;
      let modalBody = null;
      let runCount = 0;
      let waitRender = setInterval(() => {
        runCount++;
        root = document.getElementById("export-data-modal");
        if (root) {
          modalBody = root.querySelectorAll(".ant-modal-content");
          if (modalBody) {
            clearInterval(waitRender);
            const el = modalBody[0];
            el.classList.add("fade-in-animate");
            setTimeout(() => {
              el.style.opacity = 1;
              setTimeout(() => {
                el.classList.remove("fade-in-animate");
              }, 50);
            }, 200);
          }
        }
      }, 50);
    },
    closeExport() {
      const root = document.getElementById("export-data-modal");
      const modalBody = root.querySelectorAll(".ant-modal-content");
      if (modalBody) {
        const el = modalBody[0];
        el.classList.add("fade-out-animate");
        setTimeout(() => {
          el.classList.add("hidden");
          setTimeout(() => {
            const btn = document.getElementById("btn-export");
            if (btn && btn.classList && btn.classList.contains("disable-cta")) {
              btn.classList.remove("disable-cta");
            }
            this.showExportModal = false;
            setTimeout(() => {
              el.classList.remove("fade-out-animate");
            }, 50);
          }, 200);
        }, 250);
      }
    },
    closeShowExport() {
      this.showExport = false;
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    goToEdit() {
      window.location.href = `/admin/molds/${this.listChecked[0]}`;
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      const listInstalled = this.listCheckedFull
        .filter((item) => item.equipmentStatus !== "INSTALLED")
        .map((item) => {
          return item.id;
        });
      if (listInstalled.length >= 1) {
        axios
          .post(`/api/molds/delete-in-batch`, listInstalled)
          .then((res) => {
            $("#op-delete-popup").modal("hide");
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      } else {
        $("#op-delete-popup").modal("hide");
        this.paging(1);
      }
    },
    showDeletePopup() {
      if (!localStorage.getItem("dontShowWarning")) {
        var child = Common.vue.getChild(this.$children, "delete-popup");
        if (child != null) {
          child.showDeletePopup();
        }
      } else {
        axios
          .post(`/api/molds/delete-in-batch`, this.listChecked)
          .then((res) => {
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    animationPrimary() {
      $(".animationPrimary").click(() => {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(() => {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("outline-animation");
          }
        );
      });
    },
    deleteItem(result) {
      axios.delete(`/api/molds/delete/${result.id}`).then((res) => {
        this.paging(this.requestParam.page);
      });
    },
    restoreItem() {
      axios.post(`/api/molds/restore/${this.listChecked[0]}`).then((res) => {
        this.paging(this.requestParam.page);
      });
    },

    changeDropdown() {
      this.dropdownOpening = false;
    },
    openDropdown() {
      if (this.dropdownOpening === true) {
        this.dropdownOpening = false;
      } else {
        this.dropdownOpening = true;
      }
    },
    showDrawer() {
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
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
    handleResetColumnsListSelected() {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    setAllColumnList() {
      const self = this;

      this.allColumnList = [
        {
          label: self.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "equipmentCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: self.resources["part"],
          field: "part",
          mandatory: true,
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: self.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "location.company.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: self.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "location.name",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: self.resources["accumulated_shots"],
          field: "lastShot",
          default: true,
          sortable: true,
        },
        {
          label: self.resources["latest_cycle_time"],
          field: "cycleTime",
          sortable: true,
          sortField: "lastCycleTime",
        },
        {
          label: self.resources["op"],
          field: "op",
          default: true,
          sortable: true,
          sortField: "operatingStatus",
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: self.resources["status"],
          field: "equipmentStatus",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: self.resources["cycle_time_tolerance_l1"],
          field: "cycleTimeToleranceL1",
          sortable: true,
          sortField: "cycleTimeLimit1",
        },
        {
          label: self.resources["cycle_time_tolerance_l2"],
          field: "cycleTimeToleranceL2",
          sortable: true,
          sortField: "cycleTimeLimit2",
        },
        {
          label: self.resources["weighted_average_cycle_time"],
          field: "weightedAverageCycleTime",
          sortable: true,
          sortField: "weightedAverageCycleTime",
        },
        {
          label: self.resources["uptime_target"],
          field: "uptimeTarget",
          sortable: true,
        },
        {
          label: self.resources["engineer_in_charge"],
          field: "engineerInCharge",
          sortable: true,
          sortField: "engineer",
        },
        {
          label: self.resources["weight_of_runner_system_o"],
          field: "weightOfRunnerSystem",
          sortable: true,
          sortField: "weightRunner",
        },
        {
          label: self.resources["approved_cycle_time"],
          field: "contractedCycleTimeSeconds",
          sortable: true,
          sortField: "contractedCycleTime",
        },
        {
          label: self.resources["counter_id"],
          field: "counterCode",
          sortable: true,
          sortField: "counter.equipmentCode",
        },
        {
          label: self.resources["reset.forecastedMaxShots"],
          field: "designedShot",
          sortable: true,
        },
        {
          label: self.resources["hot_runner_number_of_drop"],
          field: "hotRunnerDrop",
          sortable: true,
        },
        {
          label: self.resources["hot_runner_zone"],
          field: "hotRunnerZone",
          sortable: true,
        },
        {
          label: self.resources["front_injection"],
          field: "injectionMachineId",
          sortable: true,
        },
        {
          label: self.resources["required_labor"],
          field: "labour",
          sortable: true,
        },
        {
          label: self.resources["last_date_of_shots"],
          field: "lastShotDate",
          sortable: true,
          sortField: "lastShotAt",
        },
        {
          label: self.resources["machine_ton"],
          field: "quotedMachineTonnage",
          sortable: true,
        },
        {
          label: self.resources["maintenance_interval"],
          field: "preventCycle",
          sortable: true,
        },
        {
          label: self.resources["maker_of_runner_system"],
          field: "runnerMaker",
          sortable: true,
        },
        {
          label: self.resources["maximum_capacity_per_week"],
          field: "maxCapacityPerWeek",
          sortable: true,
        },
        {
          label: self.resources["overdue_maintenance_tolerance"],
          field: "preventOverdue",
          sortable: true,
        },
        {
          label: self.resources["production_hour_per_day"],
          field: "shiftsPerDay",
          sortable: true,
        },
        {
          label: self.resources["production_day_per_week"],
          field: "productionDays",
          sortable: true,
        },
        {
          label: self.resources["shot_weight"],
          field: "shotSize",
          sortable: true,
        },
        {
          label: self.resources["supplier"],
          field: "supplierCompanyName",
          sortable: true,
          sortField: "supplier.name",
        },
        {
          label: self.resources["tool_description"],
          field: "toolDescription",
          sortable: true,
        },
        {
          label: self.resources["front_tool_size"],
          field: "toolingSizeView",
          sortable: true,
          sortField: "size",
        },
        {
          label: self.resources["tool_weight"],
          field: "toolingWeightView",
          sortable: true,
          sortField: "weight",
        },
        {
          label: self.resources["tooling_complexity"],
          field: "toolingComplexity",
          sortable: true,
        },
        {
          label: self.resources["tooling_letter"],
          field: "toolingLetter",
          sortable: true,
        },
        {
          label: self.resources["tooling_type"],
          field: "toolingType",
          sortable: true,
        },
        {
          label: self.resources["toolmaker"],
          field: "toolMakerCompanyName",
          sortable: true,
          sortField: "toolMaker.name",
        },
        {
          label: self.resources["type_of_runner_system"],
          field: "runnerTypeTitle",
          sortable: true,
          sortField: "runnerType",
        },
        {
          label: self.resources["upcoming_maintenance_tolerance"],
          field: "preventUpcoming",
          sortable: true,
        },
        {
          label: self.resources["uptime_tolerance_l1"],
          field: "uptimeLimitL1",
          sortable: true,
        },
        {
          label: self.resources["uptime_tolerance_l2"],
          field: "uptimeLimitL2",
          sortable: true,
        },
        {
          label: self.resources["utilization_rate"],
          field: "utilizationRate",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: self.resources["year_of_tool_made"],
          field: "madeYear",
          sortable: true,
        },
        {
          label: self.resources["inactive_period"],
          field: "inactivePeriod",
          sortable: true,
          sortField: "inactivePeriod",
        },
      ];
      this.checkDyson = Common.checkDyson;
      if (this.checkDyson === "dyson") {
        this.allColumnList.push({
          label: self.resources["data_approval"],
          field: "dataSubmission",
          default: true,
          sortable: true,
          sortField: "dataSubmission",
        });
      }
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(
          this,
          this.objectType,
          this.allColumnList,
          null,
          this.configDeleteField
        );
        this.getCustomColumnAndInit();
      } catch (e) {
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
      this.$forceUpdate();
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally(() => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
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
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.$forceUpdate();

            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        })
        .finally(() => {
          this.changeToDisplayTempColumn();
        });
    },
    handleChangeValueCheckBox(value) {
      this.allColumnList.forEach((item) => {
        if (item.field === value) {
          item.enabled = !item.enabled;
        }
      });
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
        .then(
          (response) => {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
          },
          () => {
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.changeToDisplayTempColumn();
            this.$forceUpdate();
          }
        );
    },
    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    showLocationHistory(mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
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
          return "seconds";
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
          return "seconds";
        }
        if (result.cycleTimeLimit2Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
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
    showRevisionHistory(id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MOLD");
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
    check(e, result) {
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
    select(event) {
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
    tab(status) {
      this.requestParam.tabType = "";
      this.total = null;
      this.listChecked = [];
      this.isAll = false;
      if (status == "DELETED") {
        this.requestParam.status = status;
        this.requestParam.deleted = true;
        this.paging(1);
      } else {
        this.currentSortType = "lastShotAt";
        this.requestParam.sort = "lastShotAt,desc";
        this.requestParam.status = status;
        delete this.requestParam.deleted;
        if (["DIGITAL", "NON_DIGITAL"].includes(status)) {
          this.requestParam.status = "";
          this.requestParam.tabType = status;
        }

        this.paging(1);
      }
      this.changeShow(null);
    },
    search(page) {
      this.paging(1);
    },
    changeEquipmentStatus(data, status) {
      var param = {
        id: data.id,
        equipmentStatus: status,
      };
      axios
        .put(Page.API_BASE + "/" + param.id + "/equipment-status", param)
        .then((response) => {
          this.paging(1);
        });
    },
    showChart(mold) {
      console.log("showChart");
      console.log(Common);
      console.log(Common.ChartJS);
      this.moldChartVisible = true;
      this.oct = this.octs[this.optimalCycleTime.strategy];
      this.type = "QUANTITY";
      this.dateViewType = "DAY";
      this.mold = mold;
    },
    closeModal() {
      this.moldChartVisible = false;
      this.chartMoldNewKey += 1;
    },
    downloadTooling(type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;
      let path = "";
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

      let downloadParams = listChecked.join(",");

      if (type === EXPORT_TYPE.PDF) {
        path = "exportPdfDetailMolds";
      } else {
        path = "exportExcel";
      }

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      let $vm = this;
      let sort = this.requestParam.sort
        ? `&sort=${this.requestParam.sort}`
        : ``;
      let url = `${
        Page.API_BASE
      }/${path}?ids=${downloadParams}&fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
      if (this.listChecked.length === 0) {
        url = `${
          Page.API_BASE
        }/${path}?fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
      }
      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          if (!$vm.isStopedExport) {
            $vm.isCallingDone = true;
            setTimeout(() => {
              var blob = new Blob([response.data], {
                type: "text/plain;charset=utf-8",
              });
              if (type === EXPORT_TYPE.PDF) {
                saveAs(blob, `${fileName}.pdf`);
              } else {
                saveAs(blob, `${fileName}.xlsx`);
              }
            }, 2000);
          }
        },
        (error) => {
          $vm.isStopedExport = true;
        }
      );
    },
    enable(checklist) {
      checklist.enabled = true;
      this.save(checklist);
    },
    disable(checklist) {
      checklist.enabled = false;
      this.save(checklist);
    },
    save(checklist) {
      const self = this;
      //var param = Common.param(item);
      var param = {
        id: checklist.id,
        enabled: checklist.enabled,
      };
      axios
        .put(Page.API_BASE + "/" + checklist.id + "/enable", param)
        .then((response) => {
          self.paging(1);
          this.showDropdown = false;
        });
    },
    paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      this.requestParam.id = objectId;
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked
        .filter((item) => item.status === this.requestParam.status)
        .map((item) => item.page);

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

      let requestParamConvert = this.requestParam;
      if (this.requestParam.operatingStatus === "ALL") {
        requestParamConvert.operatingStatus = "";
      }

      if (this.requestParam.equipmentStatus === "ALL") {
        requestParamConvert.equipmentStatus = "";
      }

      var param = Common.param(requestParamConvert);

      const {
        firstCompare,
        secondCompare,
        thirdCompare,
        firstCaviti,
        secondCaviti,
        counterId,
        partId,
        companyId,
      } = this;
      let queryParams = "";
      if (counterId || partId) {
        queryParams = `&where=counter${counterId},part${partId}`;
      } else {
        queryParams = `&where=op${secondCompare},${firstCompare}${firstCaviti},${thirdCompare}${secondCaviti}`;
      }
      param = param + queryParams;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();
      axios
        .get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then((response) => {
          this.total = response.data.totalElements;
          this.results = response.data.content;
          this.results.forEach((item) => {
            item.familyTool = false;
          });

          if (objectId != "") {
            let searchParam = this.results.filter(
              (item) => parseInt(item.id) === parseInt(objectId)
            )[0];
            this.requestParam.query = searchParam.equipmentCode;
            window.history.replaceState(null, null, "/admin/molds");
          }

          this.pagination = Common.getPagingData(response.data);
          if (
            this.listCheckedTracked[this.requestParam.status][
              this.requestParam.page
            ]
          ) {
            this.listChecked =
              this.listCheckedTracked[this.requestParam.status][
                this.requestParam.page
              ];
          }
          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          this.setResultObject();
          // 카테고리 정보 저장.
          this.setCategories();

          Common.handleNoResults("#app", this.results.length);

          var pageInfo =
            this.requestParam.page == 1
              ? ""
              : "?page=" + this.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);

          if (this.results.length > 0) {
            Common.triggerShowNewFeatureNotify(
              this.$children,
              "action-bar-feature",
              null,
              {
                key: "feature",
                value: "ACTION_BAR",
              }
            );
          } else {
            Common.triggerShowNewFeatureNotify(
              this.$children,
              "action-bar-feature",
              null,
              {
                key: "feature",
                value: "TOOLING_TAB",
              }
            );
          }
          this.listChecked = [];
        });
    },
    setResultObject() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i] !== "object") {
          var moldId = this.results[i];
          this.results[i] = this.findMold(this.results, moldId);
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
            break;
          }
        }
      }
    },
    setCategories() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (
            typeof this.results[i].part.category !== "object" &&
            this.results[i].part.categoryId > 0
          ) {
            var categoryId = this.results[i].part.categoryId;
            this.results[i].part.category = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }

      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (typeof this.results[i].part.category.parent !== "object") {
            var categoryId = this.results[i].part.category.parent;
            this.results[i].part.category.parent = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }
    },
    findCategory(results, categoryId) {
      for (var i = 0; i < results.length; i++) {
        var mold = results[i];
        if (
          mold.part == null ||
          mold.part.category == null ||
          typeof mold.part.category !== "object"
        ) {
          continue;
        }

        var category = mold.part.category;
        if (categoryId == category.id) {
          return category;
          break;
        }

        if (category.parent != null && categoryId == category.parent.id) {
          return category.parent;
          break;
        }

        // 부모의 자삭에서 찾기
        if (category.parent != null && category.parent.children != null) {
          for (var j = 0; j < category.parent.children.length; j++) {
            var parentChild = category.parent.children[j];
            if (typeof parentChild !== "object") {
              continue;
            }
            if (categoryId == parentChild.id) {
              return parentChild;
              break;
            }
          }
        }

        // parts에서 검색
        if (mold.part.molds != null) {
          var findCategory = this.findCategory(mold.part.molds, categoryId);
          if (typeof findCategory === "object") {
            return findCategory;
            break;
          }
        }
      }
    },
    showMoldDetails(mold) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        const tab = "switch-detail";
        if (mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
            .then((response) => {
              reasonData = response.data;
              reasonData.approvedAt = reasonData.approvedAt
                ? this.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              mold.notificationStatus = mold.dataSubmission;
              mold.reason = reasonData.reason;
              mold.approvedAt = reasonData.approvedAt;
              mold.approvedBy = reasonData.approvedBy;
              this.showChart(mold, tab);
            });
        } else {
          this.showChart(mold, tab);
        }
      }
    },
    showCorrective(mold) {
      var child = Common.vue.getChild(this.$children, "corrective-form");
      if (child != null) {
        child.showModal(mold);
      }
    },
    callbackCorrectiveForm(mold) {
      this.paging(1);
    },
    sortBy(type) {
      if (type === "lastShot") {
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
    showFilePreviewer(file, fromPopup) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file, fromPopup);
      }
    },
    backToMoldDetail(fromPopup) {
      var child = Common.vue.getChild(
        this.$children,
        fromPopup || "chart-modal"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    changeToDisplayTempColumn() {
      if (this.dashboardChartType != "INACTIVE_TOOLINGS") {
        return;
      }

      let currentColumn = this.allColumnList.filter(
        (item) => item.enabled == true
      );
      let inactivePeriodField = this.allColumnList.find(
        (el) => el.field == "inactivePeriod"
      );
      if (!inactivePeriodField.enable) {
        inactivePeriodField.enableTemp = true;
        if (currentColumn.length > 9) {
          let currentColumnNotMandatory = currentColumn.filter(
            (item) => !item.mandatory
          );
          currentColumnNotMandatory.forEach((item) => {
            item.disableTemp = false;
          });
          if (currentColumnNotMandatory.length > 0)
            currentColumnNotMandatory[
              currentColumnNotMandatory.length - 1
            ].disableTemp = true;
        }
      }
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        this.resources = JSON.parse(messages);
        this.setAllColumnList();

        const self = this;

        this.dropdownItems = [
          {
            title: self.resources["new_tooling"],
            clickHandler: () => Common.onChangeHref("/admin/molds/new"),
          },
          // TODO: RECHECK SINCE /admin/molds/import was remove
          {
            title: self.resources["import_toolings"],
            clickHandler: () => Common.onChangeHref("/admin/molds/import"),
          },
          {
            title: "Create Tooling Property",
            clickHandler: () => self.showDrawer,
          },
        ];
      } catch (error) {
        Common.alert(error?.response?.data);
      }
    },
  },
  async created() {
    let params = Common.parseParams(URI.parse(location.href).query);
    if (params.dashboardChartType) {
      this.dashboardChartType = params.dashboardChartType;
    }
  },
  mounted() {
    Common.removeSkeleton();
    this.$nextTick(() => {
      this.getResources();
      this.animationOutline();
      this.animationPrimary();
      axios.get("/api/users/server").then((response) => {
        this.checkDyson = response.data;
        this.featureList =
          this.checkDyson == "dyson"
            ? ["ACTION_BAR"]
            : ["TOOLING_TAB", "ACTION_BAR"];
        axios.get("/api/users/type").then((response) => {
          this.usetType = response.data;
        });
        axios.get("/api/codes").then((response) => {
          this.codes = response.data;

          var partCode = Common.getParameter("partCode");
          var compnayId = Common.getParameter("companyId");
          var op = Common.getParameter("op");
          var moldCode = Common.getParameter("moldCode");
          var equipmentStatus = Common.getParameter("equipmentStatus");
          this.optionParams.fromPage = Common.getParameter("from");
          this.optionParams.dateViewType = Common.getParameter("dateViewType");
          this.optionParams.fromPage = Common.getParameter("from");
          this.optionParams.dateViewType = Common.getParameter("dateViewType");
          var uri_map = [];

          var uri = URI.parse(location.href);
          if (uri.fragment && uri.fragment.includes("&")) {
            uri_map = uri.fragment
              .replace("%20", " ")
              .replace("%20", " ")
              .replace("%20", " ")
              .replace("%20", " ")
              .split("&");
          }

          if (uri.fragment == "active" || op == "active") {
            this.requestParam.operatingStatus = "WORKING";
          } else if (uri.fragment == "idle" || op == "idle") {
            this.requestParam.operatingStatus = "IDLE";
          } else if (uri.fragment == "inactive" || op == "inactive") {
            this.requestParam.operatingStatus = "NOT_WORKING";
          } else if (uri.fragment == "disconnected" || op == "disconnected") {
            this.requestParam.operatingStatus = "DISCONNECTED";
          } else if (uri_map[0] == "active") {
            this.requestParam.query = uri_map[1];
            this.requestParam.operatingStatus = "WORKING";
          } else if (uri_map[0] == "idle") {
            this.requestParam.query = uri_map[1];
            this.requestParam.operatingStatus = "IDLE";
          } else if (uri_map[0] == "disconnected") {
            this.requestParam.query = uri_map[1];
            this.requestParam.operatingStatus = "DISCONNECTED";
          } else if (uri_map[0] == "inactive") {
            this.requestParam.query = uri_map[1];
            this.requestParam.operatingStatus = "NOT_WORKING";
          }

          if (partCode != "undefined" && partCode != "") {
            this.requestParam.partCode = partCode;
          }
          if (compnayId != "undefined" && compnayId != "") {
            this.requestParam.companyId = compnayId;
          }
          if (moldCode != "undefined" && moldCode != "") {
            this.requestParam.query = moldCode;
          }
          if (equipmentStatus) {
            this.requestParam.equipmentStatus = equipmentStatus;
          }
          let params = Common.parseParams(uri.query);
          if (params.inactiveLevel) {
            this.requestParam.inactiveLevel = params.inactiveLevel;
          }
          if (params.dashboardRedirected) {
            this.requestParam.dashboardRedirected = params.dashboardRedirected;
          }

          axios
            .get("/api/common/cfg-stp?configCategory=OPTIMAL_CYCLE_TIME")
            .then((response) => {
              this.optimalCycleTime = response.data.options.OPTIMAL_CYCLE_TIME;
              this.paging(1);
            });
        });
      });
    });
  },
};
</script>
