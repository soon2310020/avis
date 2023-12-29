<template>
  <div style="margin-top: 24px">
    <div class="row">
      <div class="col-lg-12">
        <div class="card wave1" style="border: none">
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span v-text="resources['striped_tbl']"></span>
          </div>
          <div style="padding: 0" class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.ALL }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.ALL)"
                >
                  <span>{{ resources["all"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.total || 0 }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.GENERAL }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.GENERAL)"
                >
                  <span>{{ resources["general"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.general || 0 }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.MAINTENANCE }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.MAINTENANCE)"
                >
                  <span>{{ resources["maintainence"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{
                      resultsStat.pm +
                        resultsStat.cm +
                        resultsStat.inspection || 0
                    }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.END_OF_LIFE }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.END_OF_LIFE)"
                >
                  <span>{{ resources["end_of_life"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.refurbishment + resultsStat.disposal || 0 }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.EMERGENCY }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.EMERGENCY)"
                >
                  <span>{{ resources["emergency"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.emergency || 0 }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === TAB_TYPE.HISTORY }"
                  href="#"
                  @click.prevent="tab(TAB_TYPE.HISTORY)"
                >
                  <span v-text="resources['history_log']"></span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.history || 0 }}
                  </span>
                </a>
              </li>
              <div
                v-if="!isFixedColumns"
                class="d-flex"
                style="position: absolute; right: 20px; align-items: center"
              >
                <customization-modal
                  style="margin-left: 8px"
                  :all-columns-list="currentColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                ></customization-modal>
              </div>
            </ul>
            <div style="border: 1px solid #c9ced2; border-top: none">
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
                        :show="true"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['work order', 'work orders']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </th>
                    <template v-for="(column, index) in getDisplayColumns">
                      <template
                        v-if="column.field === 'pmCheckpointPrediction'"
                      >
                        <th
                          :key="index"
                          :class="[
                            { __sort: column.sortable },
                            { active: currentSortType === column.sortField },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ resources[column.labelKey] }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template
                        v-else-if="column.field === 'lastShotCheckpoint'"
                      >
                        <th
                          :key="index"
                          :class="[
                            { __sort: column.sortable },
                            { active: currentSortType === column.sortField },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ resources[column.labelKey] }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template v-else-if="column.field === 'utilNextPm'">
                        <th
                          :key="index"
                          :class="[
                            { __sort: column.sortable },
                            { active: currentSortType === column.sortField },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ resources[column.labelKey] }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template
                        v-else-if="
                          [
                            'reportedUsers',
                            'startedOn',
                            'completedOn',
                          ].includes(column.field)
                        "
                      >
                        <th
                          :key="index"
                          :class="[
                            { __sort: column.sortable },
                            { active: currentSortType === column.sortField },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ resources[column.labelKey] }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template
                        v-else-if="['start', 'end'].includes(column.field)"
                      >
                        <th
                          :key="index"
                          :class="[
                            { __sort: column.sortable },
                            { active: currentSortType === column.sortField },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                          @click="sortBy(column.sortField)"
                        >
                          <span>{{ resources[column.labelKey] }}</span>
                          <span
                            v-if="column.sortable"
                            class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <th
                        v-else
                        :key="index"
                        :class="[
                          { __sort: column.sortable },
                          { active: currentSortType === column.sortField },
                          isDesc ? 'desc' : 'asc',
                          'text-left',
                        ]"
                        @click="sortBy(column.sortField)"
                      >
                        <span>{{ resources[column.labelKey] }}</span>
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
                          <div>
                            <select-all
                              :resources="resources"
                              :show="true"
                              :checked="isAll"
                              :total="total"
                              :count="results.length"
                              :unit="['work order', 'work orders']"
                              @select-all="handleSelectAll('all')"
                              @select-page="handleSelectAll('page')"
                              @deselect="handleSelectAll('deselect')"
                            ></select-all>
                          </div>
                        </div>
                        <div class="action-bar">
                          <div v-if="!canAction">
                            {{ resources["no_action_is_available"] }}
                          </div>
                          <div
                            v-if="canCompleteOrder"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () => handleCompleteWorkOrder(listCheckedFull[0])
                            "
                          >
                            <span>{{ resources["complete_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/checkmark.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canAcceptOrDeclineWorkOrder"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () =>
                                handleUpdateStatusWorkOrder(
                                  listCheckedFull[0],
                                  'ASSIGNED'
                                )
                            "
                          >
                            <span>{{
                              resources["accept_decline_work_order"]
                            }}</span>
                            <img
                              src="/images/icon/work-order/handshake-icon.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <!-- <div
                            v-if="canDeclineWorkOrder"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () =>
                                handleUpdateStatusWorkOrder(
                                  listCheckedFull[0],
                                  'ASSIGNED'
                                )
                            "
                          >
                            <span>{{ resources["decline_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/decline.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div> -->
                          <div
                            v-if="canEditWorkOrder"
                            class="action-item"
                            style="align-item: flex-start; margin-left: 12px"
                            @click="
                              () => handleEditWorkOrder(listCheckedFull[0])
                            "
                          >
                            <span>{{ resources["edit_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/edit.svg"
                              alt="edit"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canCancelWorkOrder"
                            class="action-item"
                            style="align-item: flex-start; margin-left: 12px"
                            @click="
                              () =>
                                handleUpdateStatusWorkOrder(
                                  listCheckedFull[0],
                                  'CREATED'
                                )
                            "
                          >
                            <span>{{ resources["cancel_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/cancel.svg"
                              alt="edit"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canRequestChange"
                            class="action-item"
                            style="align-item: flex-start; margin-left: 12px"
                            @click="
                              handleOpenChangeRequested(listCheckedFull[0])
                            "
                          >
                            <span>{{ resources["request_changes"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/edit.svg"
                              alt="edit"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canReopenWorkOrder"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () => handleReopenWorkOrder(listCheckedFull[0])
                            "
                          >
                            <span>{{ resources["re_open_work_order"] }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/reopen.svg"
                              alt="re-open"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canApproveRejectChanges"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () =>
                                handleApproveRejectChangeRequested(
                                  listCheckedFull[0]
                                )
                            "
                          >
                            <span>{{
                              resources["approve_reject_changes"]
                            }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/checkmark.svg"
                              alt="re-open"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            v-if="canApproveRejectReport"
                            class="action-item"
                            style="margin-left: 12px"
                            @click="
                              () =>
                                handleViewWorkOrder(
                                  listCheckedFull[0],
                                  undefined,
                                  'approve_reject_report'
                                )
                            "
                          >
                            <span>{{
                              resources["approve_reject_report"]
                            }}</span>
                            <img
                              src="/images/icon/work-order/action-bar/checkmark.svg"
                              alt="re-open"
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
                <tbody class="op-list">
                  <template>
                    <tr
                      v-show="!isLoading"
                      v-for="(result, rowIndex) in results"
                      :key="result.id"
                      :id="result.id"
                    >
                      <td
                        v-if="requestParam.status !== 'confirmed'"
                        :key="`${result.id}`"
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
                        v-for="(column, columnIndex) in getDisplayColumns"
                      >
                        <template
                          v-if="['start', 'end'].includes(column.field)"
                        >
                          <td
                            class="text-left"
                            :key="`${rowIndex}-${columnIndex}`"
                          >
                            <div v-if="!requestParam.isHistory">
                              <span v-if="column.field === 'start'">
                                {{ formatToDate(result[column.field]) }}
                              </span>
                              <span v-else>
                                {{
                                  result.pmCheckpointPrediction > 90
                                    ? "More than 90 days"
                                    : formatToDate(result[column.field])
                                }}
                              </span>
                            </div>
                          </td>
                        </template>
                        <template
                          v-else-if="
                            ['startedOn', 'completedOn'].includes(column.field)
                          "
                        >
                          <td
                            class="text-left"
                            :key="`${rowIndex}-${columnIndex}`"
                          >
                            <div v-if="requestParam.isHistory">
                              {{ formatToDate(result[column.field]) }}
                            </div>
                          </td>
                        </template>
                        <template
                          v-else-if="column.field === 'lastShotCheckpoint'"
                        >
                          <td
                            class="text-left"
                            :key="`${rowIndex}-${columnIndex}`"
                          >
                            <div
                              v-if="
                                activeTab === TAB_TYPE.MAINTENANCE &&
                                !shouldShowNoDataForTimeBaseMaintenance(result)
                              "
                            >
                              <div class="clearfix">
                                <div class="float-left font-size-14">
                                  <strong>{{
                                    result.lastShot
                                      ? formatNumber(result.lastShot)
                                      : 0
                                  }}</strong>
                                  <span
                                    >/
                                    {{
                                      formatNumber(result.pmCheckpoint)
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
                                  <strong
                                    >{{ ratioMaintenance2(result) }}%</strong
                                  >
                                </small>
                                <div
                                  style="width: 100%; margin-left: 5px"
                                  class="progress progress-xs"
                                >
                                  <div
                                    class="progress-bar"
                                    :class="ratioMaintenanceColor2(result)"
                                    role="progressbar"
                                    :style="ratioMaintenanceStyle2(result)"
                                    :aria-valuenow="ratioMaintenance2(result)"
                                    aria-valuemin="0"
                                    aria-valuemax="100"
                                  ></div>
                                </div>
                              </div>
                            </div>
                            <div
                              v-else-if="
                                activeTab === TAB_TYPE.MAINTENANCE &&
                                shouldShowNoDataForTimeBaseMaintenance(result)
                              "
                              class="float-left font-size-14"
                            >
                              -
                            </div>
                          </td>
                        </template>
                        <template v-else-if="column.field === 'utilNextPm'">
                          <td
                            class="text-left"
                            :key="`${rowIndex}-${columnIndex}`"
                          >
                            <div
                              v-if="
                                activeTab === TAB_TYPE.MAINTENANCE &&
                                !shouldShowNoDataForTimeBaseMaintenance(result)
                              "
                            >
                              <div class="clearfix">
                                <div class="float-left font-size-14">
                                  <strong>{{
                                    formatNumber(result.untilNextPm)
                                  }}</strong>
                                  <span
                                    >/
                                    {{ formatNumber(result.preventCycle) }}
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
                                  <strong
                                    >{{ ratioMaintenance2(result) }}%</strong
                                  >
                                </small>
                                <div
                                  style="width: 100%; margin-left: 5px"
                                  class="progress progress-xs"
                                >
                                  <div
                                    class="progress-bar"
                                    :class="ratioMaintenanceColor2(result)"
                                    role="progressbar"
                                    :style="ratioMaintenanceStyle2(result)"
                                    :aria-valuenow="ratioMaintenance2(result)"
                                    aria-valuemin="0"
                                    aria-valuemax="100"
                                  ></div>
                                </div>
                              </div>
                            </div>
                            <div
                              v-else-if="
                                activeTab === TAB_TYPE.MAINTENANCE &&
                                shouldShowNoDataForTimeBaseMaintenance(result)
                              "
                              class="float-left font-size-14"
                            >
                              -
                            </div>
                          </td>
                        </template>

                        <td
                          v-else-if="column.field === 'orderType'"
                          :key="`${rowIndex}-${columnIndex}`"
                          class="text-left"
                        >
                          {{ getOrderType(result[column.field]) }}
                        </td>
                        <td
                          v-else-if="column.field === 'workOrderId'"
                          :key="`workOrderId-${rowIndex}-${columnIndex}`"
                          class="text-left custom-hyper-link"
                          @click="() => handleViewWorkOrder(result, 'NONE')"
                        >
                          {{ result[column.field] }}
                        </td>
                        <td
                          v-else-if="column.field === 'createdBy'"
                          :key="`createdBy-${rowIndex}-${columnIndex}`"
                          class="text-left"
                        >
                          <!-- <base-avatar :item="result.createdBy"></base-avatar> -->
                          <base-avatar
                            v-for="(user, index) in getCreatorUsers(result)"
                            :key="index"
                            :item="user.user"
                            style="margin-right: 8px; margin-bottom: 8px"
                          ></base-avatar>
                        </td>
                        <td
                          v-else-if="column.field === 'workOrderAssets'"
                          class="text-left"
                          :key="`workOrderAssets-${rowIndex}-${columnIndex}`"
                        >
                          <base-drop-count
                            :is-hyper-link="true"
                            :data-list="result.workOrderAssets"
                            type="HYPER_LINK"
                            :title="`${result.workOrderAssets.length} ${
                              result.workOrderAssets.length > 1
                                ? 'assets'
                                : 'asset'
                            }`"
                            :custom-style="{ left: '0' }"
                          >
                            <div style="padding: 6px 8px">
                              <div
                                v-if="
                                  getAssetsIdsByType(result, 'TOOLING').length >
                                  0
                                "
                              >
                                <div
                                  style="
                                    padding-bottom: 3px;
                                    border-bottom: 1px solid #c8ced3;
                                    color: #5f5f5f;
                                  "
                                >
                                  <strong>Tooling(s)</strong>
                                </div>
                                <div
                                  v-for="(item, index) in getAssetsIdsByType(
                                    result,
                                    'TOOLING'
                                  )"
                                  :key="index"
                                  class="single-line-item"
                                  style="padding-top: 3px"
                                >
                                  <span
                                    v-if="item"
                                    class="custom-hyper-link"
                                    @click="showMoldChart(item.id)"
                                    >{{ item.code }}</span
                                  >
                                </div>
                              </div>
                              <div
                                v-if="
                                  getAssetsIdsByType(result, 'MACHINE').length >
                                  0
                                "
                              >
                                <div
                                  style="
                                    padding-bottom: 3px;
                                    border-bottom: 1px solid #c8ced3;
                                    color: #5f5f5f;
                                  "
                                >
                                  <strong>Machine(s)</strong>
                                </div>
                                <div
                                  v-for="(item, index) in getAssetsIdsByType(
                                    result,
                                    'MACHINE'
                                  )"
                                  :key="index"
                                  class="single-line-item"
                                  style="padding-top: 3px"
                                >
                                  <span
                                    v-if="item"
                                    class="custom-hyper-link"
                                    @click="showMachineDetails(item.id)"
                                    >{{ item.code }}</span
                                  >
                                </div>
                              </div>
                              <div
                                v-if="
                                  getAssetsIdsByType(result, 'COUNTER').length >
                                  0
                                "
                              >
                                <div
                                  style="
                                    padding-bottom: 3px;
                                    border-bottom: 1px solid #c8ced3;
                                    color: #5f5f5f;
                                  "
                                >
                                  <strong>Sensor(s)</strong>
                                </div>
                                <div
                                  v-for="(item, index) in getAssetsIdsByType(
                                    result,
                                    'COUNTER'
                                  )"
                                  :key="index"
                                  class="single-line-item"
                                  style="padding-top: 3px"
                                >
                                  <span
                                    v-if="item"
                                    class="custom-hyper-link"
                                    @click="showCounterDetails(item.id)"
                                    >{{ item.code }}</span
                                  >
                                </div>
                              </div>
                              <div
                                v-if="
                                  getAssetsIdsByType(result, 'TERMINAL')
                                    .length > 0
                                "
                              >
                                <div
                                  style="
                                    padding-bottom: 3px;
                                    border-bottom: 1px solid #c8ced3;
                                    color: #5f5f5f;
                                  "
                                >
                                  <strong>Terminal(s)</strong>
                                </div>
                                <div
                                  v-for="(item, index) in getAssetsIdsByType(
                                    result,
                                    'TERMINAL'
                                  )"
                                  :key="index"
                                  class="single-line-item"
                                  style="padding-top: 3px"
                                >
                                  <span
                                    v-if="item"
                                    class="custom-hyper-link"
                                    @click="showTerminalDetails(item.id)"
                                    >{{ item.code }}</span
                                  >
                                </div>
                              </div>
                            </div>
                          </base-drop-count>
                        </td>
                        <td
                          v-else-if="column.field === 'workOrderUsers'"
                          :key="`workOrderUsers-${rowIndex}-${columnIndex}`"
                          class="text-left"
                        >
                          <base-avatar
                            v-for="(user, index) in getAssignedUsers(result)"
                            :key="index"
                            :item="user.user"
                            style="margin-right: 8px; margin-bottom: 8px"
                          ></base-avatar>
                        </td>
                        <template v-else-if="column.field === 'reportedUsers'">
                          <td
                            class="text-left"
                            :key="`${rowIndex}-${columnIndex}`"
                          >
                            <div
                              v-if="
                                requestParam.isHistory &&
                                getReportedUsers(result)
                              "
                            >
                              <base-avatar
                                v-for="(user, index) in getReportedUsers(
                                  result
                                )"
                                :key="index"
                                :item="user.user"
                                style="margin-right: 8px"
                              ></base-avatar>
                            </div>
                          </td>
                        </template>

                        <td
                          v-else-if="column.field === 'priority'"
                          :key="`priority-${rowIndex}-${columnIndex}`"
                          class="text-left"
                        >
                          <base-priority-card
                            :color="
                              priorityStatusMatchColor[result[column.field]]
                            "
                          >
                            <span>
                              {{ _.startCase(_.toLower(result[column.field])) }}
                            </span>
                          </base-priority-card>
                        </td>
                        <td
                          v-else-if="column.field === 'workOrderStatus'"
                          class="text-left"
                        >
                          <base-priority-card
                            :color="
                              priorityStatusMatchColor[result[column.field]]
                            "
                          >
                            <span>
                              {{ _.startCase(_.toLower(result[column.field])) }}
                            </span>
                          </base-priority-card>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'pmCheckpointPrediction'"
                        >
                          <span
                            v-if="
                              result.orderType === 'PREVENTATIVE_MAINTENANCE'
                            "
                          >
                            {{
                              shouldShowNoDataForTimeBaseMaintenance(result)
                                ? "-"
                                : showPmCheckpointPrediction(
                                    result[column.field],
                                    result?.maintenanceStatus
                                  )
                            }}
                          </span>
                        </td>
                        <td v-else class="text-left">
                          {{ result[column.field] }}
                        </td>
                      </template>
                    </tr>
                  </template>
                </tbody>
              </table>
              <div
                v-show="results?.length === 0"
                class="no-results d-none"
                v-text="resources['no_results']"
              ></div>
              <div v-show="!isLoading" class="d-flex">
                <!-- <div class="col-md-10 col-sm-10 col-xs-10">
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
                </div> -->
                <div
                  class="col-12 d-flex justify-end align-center"
                  style="padding: 10px"
                >
                  <base-paging
                    :total-page="totalPages"
                    :current-page="getCurrentPage"
                    @next="handleNextPage"
                    @back="handleBackPage"
                  ></base-paging>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
const TAB_TYPE = {
  ALL: "WORK_ORDER_ALL",
  GENERAL: "WORK_ORDER_GENERAL",
  MAINTENANCE: "WORK_ORDER_MAINTENANCE",
  END_OF_LIFE: "WORK_ORDER_EOL",
  EMERGENCY: "WORK_ORDER_EMERGENCY",
  HISTORY: "WORK_ORDER_HISTORY",
};
const orderTypeIsMaintenance = [
  "PREVENTATIVE_MAINTENANCE",
  "CORRECTIVE_MAINTENANCE",
  "INSPECTION",
];

const orderTypeIsEndOfLife = ["REFURBISHMENT", "DISPOSAL"];

// list of all columns available
const BASE_COLUMNS_LIST = [
  {
    label: "Work Order ID",
    labelKey: "work_order_id",
    field: "workOrderId",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "workOrderId",
    position: 1,
    defaultPosition: 1,
  },
  {
    label: "Asset ID",
    labelKey: "asset_id",
    field: "workOrderAssets",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "workOrderAssets",
    position: 2,
    defaultPosition: 2,
  },
  {
    label: "Type",
    labelKey: "type",
    field: "orderType",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "orderType",
    position: 3,
    defaultPosition: 3,
  },
  {
    label: "Accumulated Shot/ PM checkpoint",
    labelKey: "accumulated_shots_checkpoint",
    field: "lastShotCheckpoint",
    default: false,
    enabled: false,
    defaultSelected: false,
    sortable: true,
    sortField: "lastShotCheckpoint",
    position: 4,
    defaultPosition: 4,
  },
  {
    label: "Priority",
    labelKey: "priority",
    field: "priority",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "priority",
    position: 5,
    defaultPosition: 5,
  },
  {
    label: "Created by",
    labelKey: "created_by",
    field: "createdBy",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "createdBy",
    position: 6,
    defaultPosition: 6,
  },
  {
    label: "Assigned to",
    labelKey: "assigned_to",
    field: "workOrderUsers",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "workOrderUsers",
    position: 7,
    defaultPosition: 7,
  },
  {
    label: "Reported by",
    labelKey: "reported_by",
    field: "reportedUsers",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "reportedUsers",
    position: 8,
    defaultPosition: 8,
  },
  {
    label: "Start Date",
    labelKey: "start_date",
    field: "start",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "start",
    position: 9,
    defaultPosition: 9,
  },
  {
    label: "Due Date",
    labelKey: "due_date",
    field: "end",
    default: true,
    defaultSelected: true,
    enabled: true,
    sortable: true,
    sortField: "end",
    position: 10,
    defaultPosition: 10,
  },
  {
    label: "Started On",
    labelKey: "started_on",
    field: "startedOn",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "startedOn",
    position: 11,
    defaultPosition: 11,
  },
  {
    label: "Completed On",
    labelKey: "completed_on",
    field: "completedOn",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "completedOn",
    position: 12,
    defaultPosition: 12,
  },
  {
    label: "Status",
    labelKey: "status",
    field: "workOrderStatus",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "workOrderStatus",
    position: 13,
    defaultPosition: 13,
  },
  {
    label: "PM Checkpoint Prediction",
    labelKey: "pm_checkpoint_prediction",
    field: "pmCheckpointPrediction",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: true,
    sortField: "pmCheckpointPrediction",
    position: 14,
    defaultPosition: 14,
  },
  {
    label: "PM Checkpoint Progress",
    labelKey: "pm_checkpoint_progress",
    field: "utilNextPm",
    default: false,
    enabled: false,
    defaultSelected: false,
    sortable: true,
    sortField: "utilNextPm",
    position: 15,
    defaultPosition: 15,
  },
];
DEFAULT_TAB_COLUMNS = [
  "workOrderId",
  "workOrderAssets",
  "orderType",
  "priority",
  "createdBy",
  "workOrderUsers",
  "start",
  "end",
  "workOrderStatus",
];

TAB_COLUMNS = {
  WORK_ORDER_ALL: [...DEFAULT_TAB_COLUMNS],
  WORK_ORDER_GENERAL: [...DEFAULT_TAB_COLUMNS],
  WORK_ORDER_MAINTENANCE: [
    ...DEFAULT_TAB_COLUMNS,
    "pmCheckpointPrediction",
    "utilNextPm",
    "lastShotCheckpoint",
  ],
  WORK_ORDER_EOL: [...DEFAULT_TAB_COLUMNS],
  WORK_ORDER_EMERGENCY: [...DEFAULT_TAB_COLUMNS],
  WORK_ORDER_HISTORY: [
    "workOrderId",
    "workOrderAssets",
    "orderType",
    "priority",
    "createdBy",
    "reportedUsers",
    "startedOn",
    "completedOn",
    "workOrderStatus",
  ],
};
// list of all columns which can be selected as default
const DEFAULT_COLUMNS_LIST = [
  "workOrderId",
  "workOrderAssets",
  "orderType",
  "priority",
  "createdBy",
  "reportedUsers",
  "start",
  "end",
  "startedOn",
  "completedOn",
  "pmCheckpointPrediction",
  "workOrderStatus",
];

module.exports = {
  components: {
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
  },
  props: {
    resources: Object,
    showCreateWorkorder: Function,
    typeList: {
      type: Array,
      default: () => [],
    },
    priorityTypeList: {
      type: Array,
      default: () => [],
    },
    companyIdList: {
      type: Array,
      default: () => [],
    },
    handleViewWorkOrder: {
      type: Function,
      default: () => {},
    },
    handleCompleteWorkOrder: {
      type: Function,
      default: () => {},
    },
    handleUpdateStatusWorkOrder: {
      type: Function,
      default: () => {},
    },
    myOrderKey: {
      type: [Number, String],
    },
    isAllCompany: {
      type: Boolean,
      default: () => true,
    },
    changedType: {
      type: String,
      default: () => "",
    },
    handleEditWorkOrder: Function,
    handleReopenWorkOrder: Function,
    handleCloseEditWorkOrder: Function,
    handleApproveRejectChangeRequested: Function,
    handleOpenChangeRequested: Function,
    showMachineDetails: {
      type: Function,
      default: () => {},
    },
    showMoldChart: {
      type: Function,
      default: () => {},
    },
    showCounterDetails: {
      type: Function,
      default: () => {},
    },
    showTerminalDetails: {
      type: Function,
      default: () => {},
    },
    currentUser: Object,
    status: String,
    results: Array,
    resultsStat: Object,
    total: Number,
    requestParam: Object,
    pagination: Array,
    selectedDate: Object,
    reloadCheckKey: [Number, String],
    totalPages: Number,
    fixedColumns: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      listChecked: [],
      listCheckedFull: [],
      sortType: {},
      pageType: "WORK_ORDER",
      allColumnList: [],
      currentColumnList: [],
      currentSortType: "id",
      isDesc: true,
      isLoading: false,
      alertOn: true,
      showDropdown: false,
      listCheckedFull: [],
      cancelToken: undefined,
      priorityStatusMatchColor: {
        HIGH: "red",
        LOW: "green",
        MEDIUM: "yellow",
        Requested: "purple",
        REQUESTED: "purple",
        Overdue: "red",
        OVERDUE: "red",
        Upcoming: "yellow",
        UPCOMING: "yellow",
        Declined: "grey",
        DECLINED: "grey",
        Cancelled: "dark-grey",
        CANCELLED: "dark-grey",
        PENDING_APPROVAL: "blue",
        "Pending Approval": "blue",
        CHANGE_REQUESTED: "pink",
      },
      showWorkOrder: false,
      workorderModalVisible: false,
      selectedWorkOrder: {},
      workOrderType: "EDIT",
      allColumnListDefault: [],
    };
  },
  watch: {
    reloadCheckKey() {
      this.clearCheck();
    },
    changedType(newVal) {
      if (newVal.length) {
        this.tab(newVal);
      }
    },
    typeList(newVal) {
      this.requestParam.typeList = newVal;
      this.listChecked = [];
    },
    priorityTypeList(newVal) {
      this.requestParam.priorityTypeList = newVal;
      this.listChecked = [];
    },
    companyIdList(newVal) {
      this.requestParam.companyIdList = newVal;
      this.listChecked = [];
    },
    isAllCompany(newVal) {
      this.requestParam.isAllCompany = newVal;
    },
    activeTab: async function (newVal) {
      if (this.isFixedColumns) return;
      const response = await this.fetchColumnConfigs();
      if (!response.length) {
        if (this.isHistory) {
          this.currentColumnList = [...BASE_COLUMNS_LIST].filter((item) =>
            TAB_COLUMNS["WORK_ORDER_HISTORY"].includes(item.field)
          );
          console.log(
            "activeTab",
            "history",
            TAB_COLUMNS[newVal],
            BASE_COLUMNS_LIST,
            this.currentColumnList
          );
        } else {
          this.currentColumnList = [...BASE_COLUMNS_LIST].filter((item) =>
            TAB_COLUMNS[newVal].includes(item.field)
          );
          console.log(
            "activeTab",
            newVal,
            TAB_COLUMNS[newVal],
            BASE_COLUMNS_LIST,
            this.currentColumnList
          );
        }
      } else {
        this.mapColumnConfig(response);
      }
    },
    resources: {
      handler(newVal) {
        if (newVal && Object.keys(newVal)?.length) {
          // this.getColumnListSelected();
        }
      },
      immediate: true,
    },
  },
  computed: {
    isOEMAdmin() {
      return (
        this.currentUser.company.companyType === "IN_HOUSE" &&
        this.currentUser.admin
      );
    },
    isFixedColumns() {
      return this.fixedColumns && this.fixedColumns.length > 0;
    },
    getDisplayColumns() {
      if (this.isFixedColumns) {
        return this.fixedColumns;
      }
      const enabledColumns = this.currentColumnList.filter(
        (item) => item.enabled
      );
      console.log("getDisplayColumns", enabledColumns, this.currentColumnList);
      return enabledColumns;
    },
    getTotalPage() {
      return this.pagination.length;
    },
    getCurrentPage() {
      console.log("getCurrentPage", this.requestParam.page);
      return this.requestParam.page;
    },
    isAll() {
      if (!this.results.length) return false;
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
    canCompleteOrder() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (!["UPCOMING", "OVERDUE"].includes(wo.workOrderStatus)) {
        return false;
      }
      if (wo.workOrderStatus === "OVERDUE" && wo.status === "REQUESTED") {
        return false;
      }
      return wo.workOrderUsers?.some(
        (requestedUser) => requestedUser.userId === this.currentUser.id
      );
    },
    canAcceptOrDeclineWorkOrder() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (!["REQUESTED", "OVERDUE"].includes(wo.workOrderStatus)) {
        return false;
      }

      if (wo.workOrderStatus === "OVERDUE" && wo.status !== "REQUESTED") {
        return false;
      }

      return wo.workOrderUsers?.some(
        (requestedUser) =>
          requestedUser?.participantType !== "CREATOR" &&
          requestedUser.userId === this.currentUser.id
      );
    },
    canDeclineWorkOrder() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (wo.workOrderStatus !== "REQUESTED") {
        return false;
      }

      return wo.workOrderUsers?.some(
        (requestedUser) => requestedUser.userId === this.currentUser.id
      );
    },
    canEditWorkOrder() {
      if (this.listCheckedFull.length > 1) return false;
      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (!["REQUESTED", "COMPLETED"].includes(wo.workOrderStatus)) {
        return false;
      }

      const isCreatedByMe = this.getCreatorUsers(wo).some(
        (item) => item.userId === this.currentUser.id
      );

      return isCreatedByMe;
    },
    canCancelWorkOrder() {
      if (this.listCheckedFull.length > 1) return false;
      const wo = this.listCheckedFull[0];
      const isCreatedByMe = this.getCreatorUsers(wo).some(
        (item) => item.userId === this.currentUser.id
      );
      return (
        isCreatedByMe &&
        ["REQUESTED", "UPCOMING", "OVERDUE"].includes(wo?.workOrderStatus)
      );
    },
    canRequestChange() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (wo.workOrderStatus !== "COMPLETED") {
        return false;
      }

      const assignToMe = wo.workOrderUsers.find(
        (item) => item.userId === this.currentUser.id
      );

      return assignToMe;
    },
    canReopenWorkOrder() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (!["DECLINED", "CANCELLED"].includes(wo.workOrderStatus)) return false;

      const isCreatedByMe = this.getCreatorUsers(wo).some(
        (item) => item.userId === this.currentUser.id
      );

      return this.requestParam.isHistory && (this.isOEMAdmin || isCreatedByMe);
    },
    canApproveRejectChanges() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (wo.workOrderStatus !== "CHANGE_REQUESTED") return false;

      const isCreatedByMe = this.getCreatorUsers(wo).some(
        (item) => item.userId === this.currentUser.id
      );

      return this.isOEMAdmin || isCreatedByMe;
    },
    canApproveRejectReport() {
      if (this.listCheckedFull.length > 1) return false;

      const wo = this.listCheckedFull[0];

      if (!wo) return false;

      if (wo.workOrderStatus !== "PENDING_APPROVAL") return false;

      if (!["REFURBISHMENT", "DISPOSAL"].includes(wo.orderType)) return false;

      return this.isOEMAdmin;
    },
    canAction() {
      return (
        this.canCompleteOrder ||
        this.canAcceptOrDeclineWorkOrder ||
        this.canEditWorkOrder ||
        this.canCancelWorkOrder ||
        this.canRequestChange ||
        this.canReopenWorkOrder ||
        this.canApproveRejectChanges ||
        this.canApproveRejectReport
      );
    },
    activeTab() {
      const firstRecordType = this.requestParam?.orderType?.[0];
      if (this.requestParam.isHistory) {
        return TAB_TYPE.HISTORY;
      }
      if (!this?.requestParam?.orderType?.length) {
        return TAB_TYPE.ALL;
      }
      if (orderTypeIsMaintenance.includes(firstRecordType)) {
        return TAB_TYPE.MAINTENANCE;
      }
      if (orderTypeIsEndOfLife.includes(firstRecordType)) {
        return TAB_TYPE.END_OF_LIFE;
      }
      return TAB_TYPE[firstRecordType];
    },
  },
  async created() {
    this.TAB_TYPE = TAB_TYPE;
    try {
      console.log("this.currentUser", this.currentUser);
      let params = Common.parseParams(URI.parse(location.href).query);
      this.requestParam.isHistory = params.isHistory === "true" ? true : false;
      this.requestParam.isAllCompany = true;
      if (!!params.assetIds) {
        this.requestParam.assetIds = [params.assetIds];
      }
      console.log("params123", params, this.requestParam);
      this.initColumnList();
    } catch (error) {
      console.log(error);
    }
  },
  methods: {
    shouldShowNoDataForTimeBaseMaintenance(result) {
      return (
        ["PREVENTATIVE_MAINTENANCE", "CORRECTIVE_MAINTENANCE"].includes(
          result.orderType
        ) && result.pmStrategy === "TIME_BASED"
      );
    },
    getReportedUsers(result) {
      return result.workOrderUsers.filter(
        (item) => item.participantType === "REPORTER"
      );
    },
    getAssignedUsers(result) {
      return result.workOrderUsers.filter(
        (item) => item.participantType === "ASSIGNEE"
      );
    },
    getCreatorUsers(result) {
      if (!result) return [];
      return _.uniqBy(
        [
          ...result?.workOrderUsers.filter(
            (item) => item.participantType === "CREATOR"
          ),
          {
            user: result?.createdBy,
            userId: result?.createdBy?.id,
            participantType: "CREATOR",
          },
        ],
        (item) => item.participantType + item.userId
      );
    },
    ratioMaintenance(result) {
      return ((result.lastShot / result.pmCheckpoint) * 100).toFixed(1);
    },
    ratioMaintenanceStyle(result) {
      return "width: " + this.ratioMaintenance(result) + "%";
    },
    ratioMaintenanceColor(result) {
      if (result.moldMaintenanceData.maintenanceStatus === "UPCOMING")
        return "bg-warning";
      else return "bg-danger";
    },
    ratioMaintenanceColor2: function (result) {
      if (result.moldMaintenanceData.maintenanceStatus === "UPCOMING")
        return "bg-warning";
      else return "bg-danger";
    },
    ratioMaintenanceStyle2(result) {
      return "width: " + this.ratioMaintenance2(result) + "%";
    },
    ratioMaintenance2(result) {
      if (
        result.preventCycle &&
        result.preventCycle !== 0 &&
        result.untilNextPm
      )
        return ((result.untilNextPm / result.preventCycle) * 100).toFixed(2);
      else return 0;
    },
    handleNextPage() {
      const newPage = this.requestParam.page + 1;
      this.paging(newPage);
    },
    handleBackPage() {
      const newPage = this.requestParam.page - 1;
      this.paging(newPage);
    },
    showPmCheckpointPrediction: function (
      pmCheckpointPrediction,
      maintenanceStatus
    ) {
      if (_.isNil(pmCheckpointPrediction)) return "-";
      if (pmCheckpointPrediction > 90) {
        return "More than 90 days";
      }
      let dayName = pmCheckpointPrediction == 1 ? " day" : " days";
      let print =
        pmCheckpointPrediction == 0
          ? "Today"
          : maintenanceStatus == "OVERDUE"
          ? "Overdue " + pmCheckpointPrediction + dayName
          : "In " + pmCheckpointPrediction + dayName;

      return print;
    },
    async fetchColumnConfigs() {
      try {
        const url = "/api/config/column-config?";
        const query = Common.param({ pageType: this.activeTab });
        const response = await axios.get(url + query);
        console.log("fetchColumnConfigs", response.data);
        return response.data;
      } catch (error) {
        console.log(error);
      }
    },
    async getColumnListSelected() {
      try {
        const response = await this.fetchColumnConfigs();
        this.mapColumnConfig(response);
      } catch (error) {
        console.log(error);
      }
    },
    mapColumnConfig(data) {
      if (data.length) {
        // let hashedByColumnName = {};
        // response.data.forEach((item) => {
        //   hashedByColumnName[item.columnName] = item;
        // });
        this.currentColumnList = [...data].map((item) => {
          const findItem = [...BASE_COLUMNS_LIST].find(
            (col) => col.field === item.columnName
          );
          if (findItem) {
            return { ...findItem, enabled: item.enabled };
          }
        });
      }
      // this.currentColumnList.forEach((item) => {
      //   if (hashedByColumnName[item.field]) {
      //     item.enabled = hashedByColumnName[item.field].enabled;
      //     item.id = hashedByColumnName[item.field].id;
      //     item.position = hashedByColumnName[item.field].position;
      //   }
      // });
      this.$forceUpdate();
      const child = Common.vue.getChild(this.$children, "show-columns");
      if (child !== null) {
        child.$forceUpdate();
      }
    },
    async initColumnList() {
      if (this.isFixedColumns) {
        this.currentColumnList = [...this.fixedColumns];
        return;
      }
      this.allColumnList = [...BASE_COLUMNS_LIST];
      const response = await this.fetchColumnConfigs();
      console.log("initColumnList before", response);
      if (!response.length) {
        this.currentColumnList = [...BASE_COLUMNS_LIST].filter((item) =>
          TAB_COLUMNS["WORK_ORDER_ALL"].includes(item.field)
        );
      } else {
        this.mapColumnConfig(response);
      }

      console.log("initColumnList", this.currentColumnList);
    },

    async saveSelectedList(dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });

      console.log("@saveSelectedList:dataFake", dataFake);

      this.currentColumnList = dataFake;
      const data = list.map((item, index) => {
        const col = {
          columnName: item.field,
          enabled: item.enabled,
          position: index,
        };
        if (item.id) {
          col.id = item.id;
        }
        return col;
      });

      console.log("@saveSelectedList:data", data);

      try {
        const response = await axios.post("/api/config/update-column-config", {
          pageType: this.activeTab,
          columnConfig: data,
        });
        const hashedByColumnName = {};
        response.data.forEach((item) => {
          hashedByColumnName[item.columnName] = item;
        });

        console.log("@saveSelectedList:hashedByColumnName", hashedByColumnName);
        this.currentColumnList.forEach((item) => {
          if (hashedByColumnName[item.field] && !item.id) {
            item.id = hashedByColumnName[item.field].id;
            item.position = hashedByColumnName[item.field].position;
          }
        });
        console.log("saveSelectedList after all", this.currentColumnList);
      } catch (error) {
        console.log(error);
      } finally {
        this.currentColumnList.sort((a, b) => {
          return a.position - b.position;
        });
        this.$forceUpdate();
      }
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
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
    },
    clearCheck() {
      this.listAllIds = [];
      this.listChecked = [];
      this.listCheckedFull = [];
    },

    tab(status) {
      this.requestParam.assetIds = [];
      this.requestParam.page = 1;
      this.requestParam.isHistory = status === TAB_TYPE.HISTORY;
      this.requestParam.status = "";
      if (TAB_TYPE.GENERAL === status) {
        this.requestParam.orderType = ["GENERAL"]; // default cases
      }
      if (TAB_TYPE.EMERGENCY === status) {
        this.requestParam.orderType = ["EMERGENCY"]; // default cases
      }
      if ([TAB_TYPE.HISTORY, TAB_TYPE.ALL].includes(status)) {
        this.requestParam.orderType = [];
        if (TAB_TYPE.ALL === status) {
          this.requestParam.status = "";
        }
      }
      if ([TAB_TYPE.MAINTENANCE].includes(status)) {
        this.requestParam.orderType = [...orderTypeIsMaintenance];
      }
      if ([TAB_TYPE.END_OF_LIFE].includes(status)) {
        this.requestParam.orderType = [...orderTypeIsEndOfLife];
      }
      this.listChecked = [];
      this.listCheckedFull = [];
      this.$emit("on-change-tab", status);
      this.paging(1);
    },

    async paging(pageNumber) {
      this.listChecked = [];
      this.listCheckedFull = [];
      this.$emit("on-paging", pageNumber);
    },
    sortBy(type) {
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
    },
    sort() {
      this.paging(1);
    },
    closeMyOrder() {
      this.showWorkOrder = false;
    },
    getOrderType(orderType) {
      if (orderType === "CORRECTIVE_MAINTENANCE") return "C.M.";
      else if (orderType === "PREVENTATIVE_MAINTENANCE") return "P.M.";
      else {
        return _.upperFirst(_.toLower(orderType));
      }
    },
    getAssetsIdsByType(workOrder, type) {
      const assets = workOrder.workOrderAssets;
      let result = [];
      if (assets) {
        const find = assets.filter((item) => item.type === type);
        if (find.length > 0) {
          result = assets.map((item) => {
            if (item.type === type) {
              return {
                id: item.assetId,
                code: item.assetCode,
              };
            }
          });
        }
      }
      return result;
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} work orders on this current page are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "all") {
        this.fetchAllListIds();
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.total} work orders are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "deselect") {
        this.listAllIds = [];
        this.listChecked = [];
        this.listCheckedFull = [];
      }

      console.log("@handleSelectAll", {
        actionType,
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
      });
    },
    async fetchAllListIds() {
      const { from, to } = this.selectedDate;
      const start = moment(from).unix();
      const end = moment(to).unix();
      try {
        var param = Common.param({
          ...this.requestParam,
          status: this.status.toLowerCase(),
          start,
          end,
          frameType: "WORK_ORDER",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + param
        );
        this.listAllIds = response.data.data;
        console.log("response.data", response.data);
        console.log("this.listAllIds.length", this.listAllIds);
        console.log("this.total", this.total);
      } catch (error) {
        console.log(error);
      }
    },
  },
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
  },
};
</script>
<style>
.custom-arrow-tooltip {
  background: #fff;
  left: 51%;
  bottom: -4px;
  margin-left: -5px;
  position: absolute;
  transform: rotate(45deg);
  width: 8px;
  height: 8px;
  box-shadow: 1px 1px 1px rgb(229 223 223 / 50%),
    1px 1px 1px rgb(229 223 223 / 50%);
}
</style>
