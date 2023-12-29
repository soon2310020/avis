<template>
  <div>
    <notify-alert
      type="REFURBISHMENT"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>

    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['refurbishment']"></strong>

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
                  :placeholder="resources['search_by_columns_shown']"
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
          <div class="card-body">
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
            </ul>
            <div v-show="alertOn">
              <table class="table table-responsive-sm table-striped">
                <thead id="thead-actionbar" class="custom-header-table">
                  <tr
                    :class="{ invisible: listChecked.length != 0 }"
                    style="height: 57px"
                    class="tr-sort"
                  >
                    <th
                      v-if="requestParam.status !== 'history'"
                      style="width: 40px; vertical-align: middle !important"
                    >
                      <select-all
                        :resources="resources"
                        :show="true"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['tooling', 'toolings']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.ID },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.ID)"
                    >
                      <span v-text="resources['tooling_id']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.COMPANY },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.COMPANY)"
                    >
                      <span v-text="resources['company']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.LOCATION },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.LOCATION)"
                    >
                      <span v-text="resources['location']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-click-outside="closeLastShot"
                      @click="sortBy(sortType.LAST_SHOT)"
                      v-bind:class="[
                        { active: currentSortType === sortType.LAST_SHOT },
                        isDesc ? 'desc' : 'asc',
                      ]"
                    >
                      <span v-text="resources['accumulated_shots']"></span>
                      <span class="__icon-sort"></span>
                      <last-shot-filter
                        v-show="lastShotDropdown"
                        @update-variable="updateVariable"
                        :last-shot-data="lastShotData"
                      ></last-shot-filter>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        {
                          active:
                            currentSortType ===
                            sortType.ESTIMATED_EXTENDED_LIFE,
                        },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.ESTIMATED_EXTENDED_LIFE)"
                    >
                      <span v-text="resources['estimated_extended_life']"></span
                      ><span class="__icon-sort"></span>
                    </th>

                    <th
                      class="text-left __sort"
                      v-if="
                        ['alert', 'approved', 'disapproved'].includes(
                          requestParam.status
                        )
                      "
                      v-bind:class="[
                        { active: currentSortType === sortType.FAILURE_TIME },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.FAILURE_TIME)"
                    >
                      <span v-text="resources['est_end_of_life']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-if="['history'].includes(requestParam.status)"
                      v-bind:class="[
                        { active: currentSortType === sortType.FAILURE_TIME },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.FAILURE_TIME)"
                    >
                      <span v-text="resources['last_refurbishment_time']"></span
                      ><span class="__icon-sort"></span>
                    </th>

                    <!--                    <th class="text-center __sort"-->
                    <!--                      v-bind:class="[{ 'active': currentSortType === sortType.OP }, isDesc ? 'desc' : 'asc']"-->
                    <!--                      @click="sortBy(sortType.OP)"><span v-text="resources['op']"></span><span-->
                    <!--                        class="__icon-sort"></span></th>-->
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.TOOLING_STATUS },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.TOOLING_STATUS)"
                    >
                      <span v-text="resources['tooling_status']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.COUNTER_STATUS },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.COUNTER_STATUS)"
                    >
                      <span v-text="resources['sensor_status']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-center __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.PRIORITY },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.PRIORITY)"
                      v-if="
                        ['alert', 'approved', 'disapproved'].includes(
                          requestParam.status
                        )
                      "
                    >
                      <span v-text="resources['priority']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-center __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.STATUS },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.STATUS)"
                      v-if="requestParam.status !== 'history'"
                    >
                      <span v-text="resources['status']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-center __sort"
                      v-if="
                        requestParam.status === 'alert' ||
                        requestParam.status === 'approved' ||
                        requestParam.status === 'disapproved'
                      "
                    >
                      <span v-text="resources['details']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <!-- <th class="text-center" v-if="['alert', 'approved','disapproved'].includes(requestParam.status)">Action</th> -->
                    <th class="text-center" v-else>
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
                              :show="true"
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
                              <template
                                v-if="
                                  ['alert', 'disapproved'].includes(
                                    requestParam.status
                                  )
                                "
                              >
                                <div
                                  title="Change status"
                                  class="change-status"
                                  @click="showDropdown = !showDropdown"
                                >
                                  <span>Change Status</span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                                <template
                                  v-if="
                                    listCheckedFull[0].refurbishmentStatus ==
                                    'REQUESTED'
                                  "
                                >
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <!-- <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li> -->
                                    <li>
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance({
                                            ...listCheckedFull[0],
                                            mold: {
                                              equipmentCode:
                                                listCheckedFull[0].moldCode,
                                            },
                                          })
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
                                  </ul>
                                </template>
                                <template v-else>
                                  <ul
                                    class="dropdown-menu"
                                    :class="[showDropdown ? 'show' : '']"
                                  >
                                    <!-- <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li> -->
                                    <li>
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showRequestMaintenance({
                                            ...listCheckedFull[0],
                                            mold: {
                                              equipmentCode:
                                                listCheckedFull[0].moldCode,
                                            },
                                          })
                                        "
                                        >Request Refurbishment</a
                                      >
                                    </li>
                                    <li>
                                      <a
                                        href="#"
                                        class="dropdown-item"
                                        @click.prevent="
                                          showDiscardRefurbishment(
                                            listCheckedFull[0]
                                          )
                                        "
                                        >Discard Tooling</a
                                      >
                                    </li>
                                  </ul>
                                </template>
                              </template>
                              <template
                                v-else-if="requestParam.status == 'approved'"
                              >
                                <div
                                  title="Change status"
                                  class="change-status"
                                  @click.prevent="
                                    showRequestMaintenance({
                                      ...listCheckedFull[0],
                                      mold: {
                                        equipmentCode:
                                          listCheckedFull[0].moldCode,
                                      },
                                    })
                                  "
                                >
                                  <span>Change Status</span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                              </template>
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
                            class="change-status-dropdown drowdown d-inline"
                            v-click-outside="closeWorkorderDropdown"
                          >
                            <div
                              v-if="listChecked.length == 1"
                              class="action-item"
                              @click="
                                showWorkOrderDropdown = !showWorkOrderDropdown
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
                            <ul
                              class="dropdown-menu"
                              :class="[showWorkOrderDropdown ? 'show' : '']"
                            >
                              <li
                                v-for="item in workOrderType"
                                :key="item.value"
                              >
                                <div
                                  class="dropdown-item"
                                  @click.prevent="
                                    showWorkOrderDropdown = false;
                                    showCreateWorkorder(
                                      {
                                        ...listCheckedFull[0],
                                        id: listCheckedFull[0].moldId,
                                        mold: {
                                          equipmentCode:
                                            listCheckedFull[0].moldCode,
                                          locationCode:
                                            listCheckedFull[0].locationCode,
                                          locationName:
                                            listCheckedFull[0].locationName,
                                          locationId:
                                            listCheckedFull[0].locationId,
                                        },
                                      },
                                      false,
                                      item
                                    );
                                  "
                                >
                                  {{ `Request ${item.name}` }}
                                </div>
                              </li>
                            </ul>
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
                      v-for="(result, index, id) in results"
                      :id="result.id"
                    >
                      <!-- Tooling ID -->
                      <td v-if="requestParam.status !== 'history'">
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
                      <td class="text-left tooling-id-cell">
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
                      <!-- Company -->
                      <td class="company-cell text-left">
                        <div>
                          <a
                            class="font-size-14"
                            href="#"
                            @click.prevent="
                              showCompanyDetailsById(result.companyId)
                            "
                          >
                            {{ result.companyName }}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">{{
                          result.companyCode
                        }}</span>
                      </td>

                      <!-- Location -->
                      <td class="location-cell text-left">
                        <div class="font-size-14">
                          <a
                            href="#"
                            @click.prevent="
                              showLocationHistory({
                                id: result.moldId,
                                equipmentCode: result.moldCode,
                              })
                            "
                          >
                            {{ result.locationName }}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">
                          {{ result.locationCode }}
                        </span>
                      </td>
                      <!-- Last Shot -->

                      <td class="last-shot-cell text-left">
                        <span>{{ formatNumber(result.accumShotCount) }}</span>
                        <div>
                          <span class="small text-muted font-size-11-2">
                            shots
                          </span>
                        </div>
                      </td>
                      <td class="last-shot-cell text-left">
                        <span>{{
                          formatNumber(result.estimateExtendedLife)
                        }}</span>
                        <div>
                          <span
                            class="small text-muted font-size-11-2"
                            v-if="result.estimateExtendedLife"
                          >
                            shots
                          </span>
                        </div>
                      </td>
                      <!-- Refurbishment time -->
                      <td class="falure-time-cell text-left">
                        {{ result.endLifeDate }}
                      </td>
                      <td class="text-left tooling-status">
                        <mold-status
                          :mold="{
                            ...result,
                            moldStatus: result.toolingStatus,
                          }"
                          :resources="resources"
                          :rule-op-status="ruleOpStatus"
                        />
                      </td>
                      <td class="text-left">
                        <counter-status
                          :mold="{
                            ...result,
                            counterStatus: result.sensorStatus,
                          }"
                          :resources="resources"
                        />
                      </td>
                      <!-- Priority -->
                      <td
                        style="vertical-align: middle !important"
                        class="status-cell text-center"
                        v-if="requestParam.status !== 'history'"
                      >
                        <span
                          class="label label-danger"
                          v-if="result.priority == 'HIGH'"
                          v-text="resources['high'] + ' Priority'"
                        ></span>
                        <span
                          class="label label-warning"
                          v-if="result.priority == 'MEDIUM'"
                          v-text="resources['medium'] + ' Priority'"
                        ></span>
                      </td>
                      <!-- Status -->
                      <td
                        style="vertical-align: middle !important"
                        class="status-cell text-center"
                        v-if="requestParam.status !== 'history'"
                      >
                        <span
                          class="label label-primary"
                          v-if="result.refurbishmentStatus === 'REQUESTED'"
                          v-text="resources['requested_refurbishment']"
                        ></span>
                        <span
                          class="label label-success"
                          v-if="
                            result.refurbishmentStatus === 'APPROVED' &&
                            requestParam.status === 'approved'
                          "
                          v-text="resources['approved']"
                        ></span>

                        <span
                          class="label label-inactive"
                          v-if="
                            result.refurbishmentStatus === 'DISAPPROVED' &&
                            requestParam.status === 'disapproved'
                          "
                          v-text="resources['disapproved']"
                        ></span>
                      </td>

                      <td
                        style="vertical-align: middle !important"
                        class="status-cell text-center"
                        v-if="requestParam.status === 'history'"
                      >
                        <a
                          href="#"
                          @click.prevent="
                            showCorrectiveMaintenanceHistory({
                              mold: {
                                id: result.moldId,
                                equipmentCode: result.moldCode,
                              },
                            })
                          "
                          title="View"
                          v-if="
                            result.refurbishmentStatus === 'COMPLETED' ||
                            result.refurbishmentStatus === 'DISCARDED' ||
                            result.refurbishmentStatus === 'DISAPPROVED' ||
                            result.refurbishmentStatus === 'APPROVED'
                          "
                        >
                          <img
                            height="18"
                            src="/images/icon/view.png"
                            alt="View"
                          />
                        </a>
                        <a
                          href="javascript:void(0)"
                          @click="showSystemNoteModal(result)"
                          title="Add note"
                          class="table-action-link note"
                        >
                          <img src="/images/icon/note.svg" alt="Add note" />
                        </a>
                      </td>

                      <!-- Details -->
                      <td
                        style="vertical-align: middle !important"
                        class="detail-cell text-center"
                        v-if="
                          requestParam.status === 'alert' ||
                          requestParam.status === 'approved' ||
                          requestParam.status === 'disapproved'
                        "
                      >
                        <span
                          v-if="
                            ['REQUESTED', 'APPROVED', 'DISAPPROVED'].includes(
                              result.refurbishmentStatus
                            )
                          "
                          style="font-size: 26px; cursor: pointer"
                          @click.prevent="
                            showCorrectiveMaintenanceDetails(result)
                          "
                        >
                          <i class="fa fa-file-text-o" aria-hidden="true"></i>
                        </span>
                      </td>
                    </tr>
                  </template>
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

    <request-refurbishment
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></request-refurbishment>
    <refurbishment-details :resources="resources"></refurbishment-details>
    <refurbishment-history
      @show-detail="showCorrectiveMaintenanceDetails"
      :resources="resources"
    ></refurbishment-history>
    <disapproval-refurbishment-form
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></disapproval-refurbishment-form>
    <confirm-discard-refurbishment-modal
      :resources="resources"
      @callback-message-form="callbackMessageForm"
    ></confirm-discard-refurbishment-modal>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
  </div>
</template>

<script>
const ALERT_END_OF_LIFE_API_BASE = "/api/asset/alr-eol";
const TAB_CONFIG = [
  {
    name: "Alert",
    key: "alert",
    status: "alert",
  },
  {
    name: "Approved",
    key: "approved",
    status: "approved",
  },
  {
    name: "Disapproved",
    key: "disapproved",
    status: "disapproved",
  },
  {
    name: "History Log",
    key: "history_log",
    status: "history",
  },
];

module.exports = {
  name: "mold-refurbishment",
  components: {
    "refurbishment-details": httpVueLoader(
      "/components/refurbishment-details.vue"
    ),

    "request-refurbishment": httpVueLoader(
      "/components/request-refurbishment.vue"
    ),

    "refurbishment-history": httpVueLoader(
      "/components/refurbishment-history.vue"
    ),
    "disapproval-refurbishment-form": httpVueLoader(
      "/components/disapproval-refurbishment-form.vue"
    ),
    "confirm-discard-refurbishment-modal": httpVueLoader(
      "/components/confirm-discard-refurbishment-modal.vue"
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
      results: [],
      total: 0,

      showDrowdown: null,
      pagination: [],
      requestParam: {
        filterCode: "COMMON",
        query: "",
        status: "alert",
        sort: "id,desc",
        page: 1,
        size: 20,
        tabName: TAB_CONFIG[0].name,
      },

      statusCodes: [],
      listCategories: [],
      sortType: {
        ID: "moldCode",
        COMPANY: "companyName",
        LOCATION: "locationName",
        LAST_SHOT: "accumShotCount",
        PERIOD: "createdAt",
        OP: "operatingStatus",
        PM: "executionRate",
        STATUS: "refurbishmentStatus",
        ESTIMATED_EXTENDED_LIFE: "estimateExtendedLife",
        FAILURE_TIME: "endLifeDate",
        TOOLING_STATUS: "toolingStatus",
        COUNTER_STATUS: "sensorStatus",
        PRIORITY: "priority",
      },
      currentSortType: "partCode",
      isDesc: true,
      isLoading: false,
      alertOn: true,
      listChecked: [],
      listCheckedFull: [],
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      showWorkOrderDropdown: false,
      workOrderType: [
        {
          name: "Disposal",
          value: "DISPOSAL",
        },
        {
          name: "Refurbishment",
          value: "REFURBISHMENT",
        },
      ],
      listAllIds: [],
      pageType: "REFURBISHMENT_ALERT",
      showDropdown: false,
      tabConfig: TAB_CONFIG,
      activeTab: TAB_CONFIG[0].key,
      tabs: [],
      selectType: "",
      systemNoteFunction: "",
      idNoteBatchAction: "",
    };
  },
  computed: {
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
    "requestParam.status"(newVal) {
      console.log("@watch:requestParam.status", newVal);
    },
  },
  methods: {
    closeWorkorderDropdown() {
      this.showWorkOrderDropdown = false;
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
    changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
      if (alertOn != null) {
        this.alertOn = alertOn;
      }
      console.log("this.alertOn", this.alertOn);
      if (specialAlertType != null) {
        this.requestParam.specialAlertType = specialAlertType;
      }
      if (specialAlertType != null) this.paging(1);
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
    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },

    tab(tab = TAB_CONFIG[0]) {
      this.total = null;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = tab.status;
      this.requestParam.tabName = tab.name;
      this.activeTab = tab.key;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },

    closeShowDropDown() {
      this.showDropdown = false;
    },

    disable: function (user) {
      user.enabled = false;
      //this.save(user);
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
      var child = Common.vue.getChild(this.$children, "refurbishment-details");
      if (child != null) {
        child.showCorrectiveMaintenanceDetails(maintenance);
      }
    },

    showCorrectiveMaintenanceHistory: function (maintenance) {
      var child = Common.vue.getChild(this.$children, "refurbishment-history");
      if (child != null) {
        child.show(maintenance);
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

    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
    },

    showMessageDetails: function (data) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "message-details"
      );
      if (child != null) {
        var data = {
          title: "Refurbishment Information",
          messageTitle: "Checklist",
          equipmentCode: data.mold.equipmentCode,
          alertDate: data.createdDateTime,
          confirmDate: data.maintenancedDateTime,
          name: data.maintenanceBy,
          message: data.checklist,
        };

        child.showMessageDetails(data);
      }
    },
    changeStatus: function (data) {
      var messageFormComponent = Common.vue.getChild(
        this.$children,
        "disapproval-refurbishment-form"
      );
      if (messageFormComponent != null) {
        messageFormComponent.showModal(data);
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

    async paging(pageNumber) {
      Common.handleNoResults("#app", 1);
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();
      const params = Common.param(this.requestParam);
      const url = new URL(ALERT_END_OF_LIFE_API_BASE, location.origin);
      url.search = params;
      this.isLoading = true;
      try {
        const { data: endOfLifeAlertData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });
        this.isLoading = false;
        this.total = endOfLifeAlertData.totalElements;
        this.results = endOfLifeAlertData.content;
        this.tabs = endOfLifeAlertData.tabs;
        this.pagination = Common.getPagingData(endOfLifeAlertData);

        // TODO: RECHECK RESET
        if (this.total === this.listAllIds.length) {
          // refresh checklist in case select all items
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
        // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
        this.setResultObject();
        this.setListCategories();

        Common.handleNoResults("#app", this.results.length);

        const pageInfo =
          this.requestParam.page == 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        this.isLoading = false;
        console.log("mold-refurbishment:::paging:::error:::", error);
      } finally {
        this.isLoading = false;
      }
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

    ratio: function (mold) {
      return ((mold.lastShot / mold.nextMaintenanceShot) * 100).toFixed(1);
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
    sort: function () {
      this.paging(1);
    },
    showSystemNoteModal(selectedItem) {
      const config = {
        objectType: null,
        systemNoteFunction: "REFURBISHMENT_ALERT",
      };
      if (this.isAll && this.listAllIds.length === this.total) {
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
        console.log("single select in history tab");
        this.idNoteBatchAction = selectedItem?.id || "";
        this.handleShowNoteSystem(
          { id: selectedItem.moldId },
          false,
          config.objectType,
          config.systemNoteFunction
        );
      }
    },
    handleSelectAll(actionType) {
      this.selectType = actionType;
      if (actionType === "page") {
        this.isAll = true;
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
        this.isAll = true;
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
        this.isAll = false;
        this.listAllIds = [];
        this.listChecked = [];
        this.listCheckedFull = [];
        this.isAllTracked = this.isAllTracked.filter(
          (item) =>
            item.page !== this.requestParam.page &&
            item.status !== this.requestParam.status
        );

        // TODO: need recheck listCheckedTracked
        // this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
      }
      if (actionType === "deselect") {
        this.listAllIds = [];
        this.listChecked = [];
        this.listCheckedFull = [];
      }

      console.log("@handleSelectAll", {
        actionType,
        isAll: this.isAll,
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
      const param = Common.param({
        ...this.requestParam,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]:
          listId.length > 0 ? listId : [this.idNoteBatchAction],
      });
      try {
        await axios.post(
          `${ALERT_END_OF_LIFE_API_BASE}/note-batch?${param}`,
          data
        );
      } catch (error) {
        console.log("mold-refurbishment:::handleSubmitNote:::error", error);
      }
    },
    showRequestMaintenance: function (maintenance) {
      console.log("request-refurbishment");
      var child = Common.vue.getChild(this.$children, "request-refurbishment");
      if (child != null) {
        child.show(maintenance);
      }
    },
    showDiscardRefurbishment: function (refurbishment) {
      console.log("vao show discard modal");
      var child = Common.vue.getChild(
        this.$children,
        "confirm-discard-refurbishment-modal"
      );
      if (child != null) {
        child.showConfirm(refurbishment);
      }
    },
  },
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });

    const uri = URI.parse(location.href);
    if (["approved", "history"].includes(uri.fragment)) {
      this.tab(uri.fragment);
    }
    var self = this;
    // 모든 화면이 렌더링된 후 실행합니다.
    if (uri.fragment === "approved" || uri.fragment === "history") {
      self.tab(uri.fragment);
    }
  },
};
</script>
