<template>
  <div>
    <notify-alert
      type="DOWNTIME_MACHINE"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>
    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['machine_downtime']"></strong>

            <div class="card-header-actions">
              <span class="card-header-action">
                <small v-show="alertOn" class="text-muted">
                  <span v-text="resources['total'] + ':'"></span> {{ total }}
                </small>
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
                  @click.prevent="selectTab(tabItem)"
                >
                  <span v-text="resources[tabItem.key]"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill">{{
                    tabs.find((item) => item.tabName === tabItem.name)
                      ?.totalElements || 0
                  }}</span>
                </a>
              </li>
              <customization-modal
                v-show="alertOn && requestParam.tabName === 'History Log'"
                style="position: absolute; right: 20px"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              >
              </customization-modal>
            </ul>
            <div v-show="alertOn">
              <table
                style="overflow-x: auto !important"
                class="table table-responsive-sm table-striped"
              >
                <thead id="thead-actionbar" class="custom-header-table">
                  <tr
                    :class="{ invisible: listChecked.length !== 0 }"
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
                        :unit="['machine', 'machines']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </th>
                    <template
                      v-for="(column, index) in requestParam.tabName === 'Alert'
                        ? allColumnAlertList
                        : allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <th
                        :class="[
                          {
                            __sort: column.sortable,
                            active: currentSortType === column.sortField,
                          },
                          isDesc ? 'desc' : 'asc',
                        ]"
                        class="text-left"
                        @click="sortBy(column.sortField)"
                      >
                        <span>{{ column.label }}</span>
                        <span v-if="column.sortable" class="__icon-sort"></span>
                      </th>
                    </template>
                  </tr>

                  <tr
                    :class="{
                      'd-none zindexNegative': listChecked.length === 0,
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
                            v-if="enableRegisterReason"
                            class="action-item"
                            @click="openModel('REGISTER')"
                          >
                            <span>{{
                              resources["register_downtime_reason"]
                            }}</span>
                            <i class="icon-action register-icon"></i>
                          </div>
                          <div
                            v-if="enableShowNote"
                            class="action-item"
                            @click="showSystemNoteModal()"
                          >
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                            v-if="enableCreateWorkOrder"
                            class="action-item"
                            @click="
                              showCreateWorkorder(listCheckedFull[0], true)
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
                          <div
                            class="action-item"
                            v-if="enableConfirmReason"
                            @click="openModel('CONFIRM')"
                          >
                            <span>{{ resources["confirm_downtime"] }}</span>
                            <i class="icon-action confirm-icon"></i>
                          </div>
                          <div
                            class="action-item"
                            v-if="enableEditReason || enableEditEndedReason"
                            @click="openModel('EDIT')"
                          >
                            <span>{{ resources["edit"] }}</span>
                            <i class="icon-action edit-icon"></i>
                          </div>
                          <div v-if="noActionAvailable" class="action-item">
                            No action is available
                          </div>
                        </div>
                      </div>
                    </th>
                  </tr>
                </thead>

                <tbody class="op-list" style="display: none">
                  <template v-if="!isLoading">
                    <tr v-for="(result, index, id) in results" :id="result.id">
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
                        v-for="(column, columnIndex) in requestParam.tabName ===
                        'Alert'
                          ? allColumnAlertList
                          : allColumnList"
                        v-if="column.enabled && !column.hiddenInToggle"
                      >
                        <td
                          v-if="column.field === 'machineId'"
                          class="text-left"
                        >
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="
                                showMachineDetails(result.machineId)
                              "
                            >
                              {{ result.machineCode }}
                            </a>
                          </div>
                          <div class="small text-muted font-size-11-2">
                            {{ resources["updated_on"] }}
                            {{ convertDateString(result.creationDateTime) }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'toolingId'"
                        >
                          <div v-if="result.moldId !== null">
                            <a
                              href="#"
                              @click.prevent="showChart({ id: result.moldId })"
                              style="color: #1aaa55"
                              class="mr-1 font-size-14"
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
                            <div class="small text-muted font-size-11-2">
                              <span v-text="resources['updated_on']"></span>
                              {{ convertDateString(result.creationDateTime) }}
                            </div>
                          </div>
                        </td>
                        <td
                          v-else-if="column.field === 'location'"
                          class="text-left"
                        >
                          <div class="font-size-14">
                            {{ result.locationCode }}
                          </div>
                          <div class="small text-muted font-size-11-2">
                            {{ result.locationName }}
                          </div>
                        </td>
                        <td
                          v-else-if="column.field === 'startDowntime'"
                          class="text-left"
                        >
                          <div class="font-size-14">
                            {{ convertDateString(result.startTime) }}
                            <div class="small text-muted font-size-11-2">
                              {{ convertTimeString(result.startTime) }}
                            </div>
                          </div>
                        </td>
                        <td
                          v-else-if="
                            column.field === 'endDowntime' &&
                            requestParam.tabName === 'History Log'
                          "
                          class="text-left"
                        >
                          <div class="font-size-14">
                            {{ convertDateString(result.endTime) }}
                            <div class="small text-muted font-size-11-2">
                              {{ convertTimeString(result.endTime) }}
                            </div>
                          </div>
                        </td>
                        <td
                          v-else-if="column.field === 'duration'"
                          class="text-left"
                        >
                          {{ convertSecondToHoursString(result.duration) }}
                        </td>
                        <td
                          v-else-if="column.field === 'downtimeReason'"
                          class="text-left"
                        >
                          <reason-dropdown
                            v-if="result.downtimeReasonList !== null"
                            :reasons="result.downtimeReasonList"
                            :index="index"
                          />
                        </td>
                        <td
                          v-else-if="column.field === 'downtimeStatus'"
                          class="text-center op-status-td"
                        >
                          <div class="status-column">
                            <div
                              class="icon-downtime-status"
                              :class="[
                                result.alertStatus === 'DOWNTIME'
                                  ? 'created-icon'
                                  : result.alertStatus === 'REGISTERED'
                                  ? 'in_progress-icon'
                                  : result.alertStatus === 'UNCONFIRMED'
                                  ? 'confirming-icon'
                                  : 'completed-icon',
                              ]"
                            ></div>
                            <div
                              v-text="
                                result.alertStatus === 'DOWNTIME'
                                  ? 'Downtime'
                                  : result.alertStatus === 'REGISTERED'
                                  ? 'Registered'
                                  : result.alertStatus === 'UNCONFIRMED'
                                  ? 'Unconfirmed'
                                  : 'Confirmed'
                              "
                            ></div>
                          </div>
                        </td>
                        <td
                          v-else-if="column.field === 'downtimeType'"
                          class="text-left"
                        >
                          {{
                            result.downtimeType === "UNPLANNED_DOWNTIME"
                              ? "UNPLANNED"
                              : "PLANNED"
                          }}
                        </td>
                        <td
                          v-else-if="column.field === 'confirmBy'"
                          class="text-left"
                        >
                          <user-list-cell
                            v-if="result.confirmBy"
                            :resources="resources"
                            :user-list="[result.confirmBy]"
                          ></user-list-cell>
                        </td>
                        <td
                          v-else-if="column.field === 'reportedBy'"
                          class="text-left"
                        >
                          <user-list-cell
                            v-if="result.reportedBy"
                            :resources="resources"
                            :user-list="[result.reportedBy]"
                          ></user-list-cell>
                        </td>
                        <td
                          v-else-if="
                            column.field === 'machineDowntimeReasonList'
                          "
                          class="text-left"
                        >
                          <a-tooltip
                            placement="bottom"
                            v-if="result.downtimeReasonList !== null"
                          >
                            {{ countNotes(result.downtimeReasonList) }}
                            <template slot="title">
                              <ul
                                class="list-note"
                                style="
                                  list-style-type: none;
                                  margin-bottom: 0;
                                  padding: 6px 8px;
                                "
                              >
                                <li
                                  v-for="(
                                    downtime, index
                                  ) in result.downtimeReasonList"
                                  :style="{
                                    'padding-top': index > 0 ? '0.5rem' : '',
                                  }"
                                >
                                  <div
                                    class="list-note-item"
                                    style="
                                      display: flex;
                                      flex-direction: column;
                                    "
                                  >
                                    <span
                                      class="list-note-item__title"
                                      style="font-weight: bold"
                                      >{{ downtime.reason }}</span
                                    >
                                    <span class="list-note-item__note">{{
                                      downtime.note
                                    }}</span>
                                  </div>
                                </li>
                              </ul>
                            </template>
                          </a-tooltip>
                        </td>
                      </template>
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
    <register-machine-downtime
      ref="register-machine-downtime"
      :resources="resources"
      @after-submit="paging(1, true)"
    ></register-machine-downtime>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
  </div>
  <chart-mold
    :show-file-previewer="showFilePreviewer"
    :resources="resources"
    :user-type="userType"
  ></chart-mold>
  <file-previewer :back="backToMoldDetail"></file-previewer>
  <mold-details
    :show-file-previewer="showFilePreviewer"
    :resources="resources"
  ></mold-details>
</template>

<script>
const ALERT_MACHINE_DOWNTIME_API_BASE = "/api/production/alr-mch-dtm";

const TAB_CONFIG = [
  {
    name: "Alert",
    key: "alert",
    status: "ALERT",
  },
  {
    name: "History Log",
    key: "history_log",
    status: "HISTORY",
  },
];
module.exports = {
  components: {
    "register-machine-downtime": httpVueLoader(
      "/components/alert-center/production-alert/machine-downtime/register-machine-downtime.vue"
    ),
    "reason-dropdown": httpVueLoader(
      "/components/alert-center/production-alert/machine-downtime/reason-dropdown.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
  },
  props: {
    resources: Object,
    machineId: Number,
    tab: String,
    searchText: String,
    reloadKey: [String, Number],
    enabledSearchBar: {
      type: Boolean,
      default: true,
    },
    showNote: Function,
    showCreateWorkorder: Function,
  },
  data() {
    return {
      listChecked: [],
      isLoading: false,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        filterCode: "COMMON",
        query: "",
        tab: "ALERT",
        sort: "id,desc",
        page: 1,
        size: 20,
        tabName: "Alert",
        id: [],
      },
      pageType: "MACHINE_DOWNTIME_ALERT",
      allColumnList: [],
      allColumnAlertList: [],
      currentSortType: "id",
      isDesc: true,
      alertOn: true,
      listCheckedFull: [],
      openToast: false,
      userType: null,
      machineNameReadSuccess: "",
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
      listAllIds: [],
      isBtnAlertEditRegisteredReasonPermitted: false,
      isBtnAlertRegisterReasonPermitted: false,
      isBtnHistoryLogConfirmDowntimePermitted: false,
      isBtnHistoryLogEditDowntimeReasonPermitted: false,
      tabConfig: TAB_CONFIG,
      activeTab: TAB_CONFIG[0].key,
      tabs: [],
      selectType: "",
      systemNoteFunction: "MACHINE_DOWNTIME_ALERT",
    };
  },
  computed: {
    downtimeStatus() {
      if (this.listChecked.length === 1) {
        const result = this.results.filter(
          (data) => data.id === this.listChecked[0]
        );
        // Item cant be found when user change page
        if (result.length === 0) {
          return null;
        }
        if (result) {
          return result[0].alertStatus;
        }
      }
    },
    selectedItem() {
      if (this.listChecked.length === 1) {
        const result = this.results.filter(
          (data) => data.id === this.listChecked[0]
        );
        if (result) {
          return result[0];
        }
      }
    },
    isAll() {
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
    enableShowNote() {
      return this.requestParam.tabName === "Alert";
    },
    enableCreateWorkOrder() {
      return (
        this.listChecked.length === 1 && this.requestParam.tabName === "Alert"
      );
    },
    enableEditReason() {
      const isUnconfirmed =
        this.listChecked.length === 1 && this.downtimeStatus === "REGISTERED";
      return isUnconfirmed && this.isBtnAlertEditRegisteredReasonPermitted;
    },
    enableEditEndedReason() {
      const isEnded =
        this.listChecked.length === 1 &&
        ["UNCONFIRMED", "CONFIRMED"].includes(this.downtimeStatus);
      return isEnded && this.isBtnHistoryLogEditDowntimeReasonPermitted;
    },
    enableConfirmReason() {
      const isUnconfirmed =
        this.listChecked.length === 1 && this.downtimeStatus === "UNCONFIRMED";
      return isUnconfirmed && this.isBtnHistoryLogConfirmDowntimePermitted;
    },
    enableRegisterReason() {
      const isDowntime =
        this.listChecked.length === 1 && this.downtimeStatus === "DOWNTIME";
      return isDowntime && this.isBtnAlertRegisterReasonPermitted;
    },
    noActionAvailable() {
      const isInHistory = this.requestParam.tabName === "History Log";
      const cannotShowReason =
        !this.enableEditReason &&
        !this.enableEditEndedReason &&
        !this.enableConfirmReason &&
        !this.enableRegisterReason;
      return isInHistory && cannotShowReason;
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
  },
  created() {
    this.$watch(
      () => headerVm?.permissions,
      async (newVal) => {
        console.log("headerVm?.permissions", newVal);
        if (newVal && Object.keys(newVal).length) {
          [
            this.isBtnAlertEditRegisteredReasonPermitted,
            this.isBtnAlertRegisterReasonPermitted,
            this.isBtnHistoryLogConfirmDowntimePermitted,
            this.isBtnHistoryLogEditDowntimeReasonPermitted,
          ] = await Promise.all([
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.ALERT_PRODUCTION,
              Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
              "btnAlertEditRegisteredReason"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.ALERT_PRODUCTION,
              Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
              "btnAlertRegisterReason"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.ALERT_PRODUCTION,
              Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
              "btnHistoryLogConfirmDowntime"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.ALERT_PRODUCTION,
              Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
              "btnHistoryLogEditDowntimeReason"
            ),
          ]);
          console.warn(
            this.isBtnAlertEditRegisteredReasonPermitted,
            this.isBtnAlertRegisterReasonPermitted,
            this.isBtnHistoryLogConfirmDowntimePermitted,
            this.isBtnHistoryLogEditDowntimeReasonPermitted
          );
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          const options = { ...newVal };
          this.optimalCycleTime = options.OPTIMAL_CYCLE_TIME;
          if (this.machineId) {
            this.requestParam.id = [this.machineId];
          }
        }
      },
      { immediate: true }
    );
    this.captureQuery();
    window.addEventListener("popstate", this.captureQuery);
  },
  async mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
  },
  methods: {
    showMachineDetails(machineId) {
      let child = Common.vue.getChild(
        this.$parent.$children,
        "machine-details"
      );
      if (child != null) {
        child.showDetailsById(machineId);
      }
    },
    showMoldDetails(mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },
    showChart(mold, tab) {
      let child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "QUANTITY", "DAY", tab ? tab : "switch-graph");
      }
    },
    closeToast() {
      this.openToast = false;
      this.resetAfterUpdate();
      this.paging(1);
    },
    readAlert() {
      const downtime = this.results.filter(
        (downtimeData) => downtimeData.id === this.listChecked[0]
      )[0];
      axios
        .put(`/api/asset/mch-dwn-alr/${downtime.id}/read`)
        .then((response) => {
          if (response) {
            this.openToast = true;
            this.machineNameReadSuccess = downtime.machineCode;
          }
        });
    },
    changeAlertGeneration(alertType, isInit, alertOn) {
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
    },
    openModel(type) {
      console.log("registerDowntime");
      const registerMachineDowntime = this.$refs["register-machine-downtime"];
      registerMachineDowntime.show(this.selectedItem, type);
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["machine_id"],
          field: "machineId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "machineCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "moldId",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "locationName",
          defaultSelected: false,
          defaultPosition: 2,
        },
        {
          label: this.resources["start_downtime"],
          field: "startDowntime",
          default: true,
          sortable: true,
          sortField: "startTime",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["end_downtime"],
          field: "endDowntime",
          default: true,
          sortable: true,
          sortField: "endTime",
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["duration"],
          field: "duration",
          default: true,
          sortable: true,
          sortField: "duration",
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["reason"],
          field: "downtimeReason",
          default: true,
          sortable: true,
          sortField: "downtimeReason",
          defaultSelected: false,
          defaultPosition: 6,
        },
        {
          label: this.resources["status"],
          field: "downtimeStatus",
          default: true,
          sortable: true,
          sortField: "alertStatus",
          defaultSelected: true,
          defaultPosition: 7,
        },
        {
          label: this.resources["type"],
          field: "downtimeType",
          default: false,
          sortable: true,
          sortField: "downtimeType",
          defaultSelected: false,
          defaultPosition: 8,
        },
        {
          label: this.resources["confirmed_by"],
          field: "confirmBy",
          default: false,
          sortable: true,
          sortField: "confirmBy",
          defaultSelected: false,
          defaultPosition: 9,
        },
        {
          label: this.resources["reported_by"],
          field: "reportedBy",
          default: false,
          sortable: true,
          sortField: "reportedBy",
          defaultSelected: false,
          defaultPosition: 10,
        },
      ];
      this.allColumnAlertList = [
        {
          label: this.resources["machine_id"],
          field: "machineId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "machineCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "moldId",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "locationName",
          defaultSelected: false,
          defaultPosition: 2,
        },
        {
          label: this.resources["start_downtime"],
          field: "startDowntime",
          default: true,
          sortable: true,
          sortField: "startTime",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["duration"],
          field: "duration",
          default: true,
          sortable: true,
          sortField: "duration",
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["reason"],
          field: "downtimeReason",
          default: true,
          sortable: true,
          sortField: "downtimeReason",
          defaultSelected: false,
          defaultPosition: 5,
        },
        {
          label: this.resources["note"],
          field: "machineDowntimeReasonList",
          default: true,
          sortable: false,
          sortField: "",
          defaultSelected: false,
          defaultPosition: 6,
        },
        {
          label: this.resources["status"],
          field: "downtimeStatus",
          default: true,
          sortable: true,
          sortField: "alertStatus",
          defaultSelected: true,
          defaultPosition: 7,
        },
      ];
      try {
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
      this.allColumnAlertList.forEach((item) => {
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
    // TODO: async await
    async getColumnListSelected() {
      try {
        const { data } = await axios.get(
          `/api/config/column-config?pageType=${this.pageType}`
        );
        if (data.length) {
          let hashedByColumnName = {};
          data.forEach((item) => {
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
      } catch (e) {
        return new Error();
      }
    },
    handleChangeValueCheckBox(value) {
      console.log(
        "handleChangeValueCheckBox:::allColumnList",
        this.allColumnList
      );
      console.log("handleChangeValueCheckBox:::value", value);
      let column =
        this.allColumnList?.filter((item) => item.field === value)[0] || [];
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
    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value === id;
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

    selectTab(tab) {
      this.total = null;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.tab = tab.status;
      this.requestParam.tabName = tab.name;
      this.activeTab = tab.key;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    async paging(pageNumber, uncheck = false) {
      Common.handleNoResults("#app", 1);
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();

      const params = Common.param(this.requestParam);
      const url = new URL(ALERT_MACHINE_DOWNTIME_API_BASE, location.origin);
      url.search = params;

      this.isLoading = true;
      try {
        const { data: machineDowntimeAlertData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });
        this.isLoading = false;
        if (this.requestParam.id) {
          this.requestParam.id = [];
        }
        this.total = machineDowntimeAlertData.totalElements;
        this.results = machineDowntimeAlertData.content;
        this.tabs = machineDowntimeAlertData.tabs;
        this.pagination = Common.getPagingData(machineDowntimeAlertData);
        if (uncheck) {
          this.resetAfterUpdate();
        }

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

        Common.handleNoResults("#app", this.results.length);
        const pageInfo =
          this.requestParam.page === 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        this.isLoading = false;
        console.log("machine-downtime:::paging:::error:::", error);
      }
    },
    resetAfterUpdate() {
      this.listChecked = [];
      this.listCheckedFull = [];
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
    convertTimeString(s) {
      if (s) {
        return s?.slice(11, 16);
      } else return "";
    },
    convertDateString(s) {
      if (s) {
        return s?.slice(0, 10);
      } else return "";
    },
    convertSecondToHoursString(second) {
      let responseString = "";
      if (second) {
        const days = Math.floor(second / 86400);
        second -= days * 86400;

        const dayUnit = days > 1 ? " days" : " day";

        const hours = Math.floor(second / 3600) % 24;
        second -= hours * 3600;
        const hourUnit = hours > 1 ? " hrs" : " hr";

        let minutes = Math.floor(second / 60) % 60;
        second -= minutes * 60;
        if (second > 30) {
          minutes += 1;
        }
        const minuteUnit = minutes > 1 ? " mins" : " min";
        if (days > 0) {
          responseString += days + dayUnit;

          if (hours > 0) {
            responseString += ", " + hours + hourUnit;
          }
        } else if (hours > 0) {
          responseString = hours + hourUnit;
          if (minutes > 0) {
            responseString += ", " + minutes + minuteUnit;
          }
        } else if (minutes > 0) {
          responseString = minutes + minuteUnit;
        } else {
          responseString = "0 min";
        }
      }
      return responseString;
    },

    showSystemNoteModal(selectedItem) {
      console.log("@showSystemNoteModal:selectedItem");
      const child = this.$root.$refs["system-note"];
      console.log("@showSystemNoteModal:root", child, this.$root);
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
            this.listCheckedFull.map((i) => i.machineId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          child.showSystemNote(
            { id: this.listCheckedFull[0].machineId },
            false
          );
        } else {
          console.log("single select in history tab");
          child.showSystemNote({ id: selectedItem.machineId });
        }
      } else {
        if (this.listAllIds.length === this.total) {
          console.log("select-all");
          this.handleShowNoteSystem(
            this.listAllIds.map((i) => i.mainObjectId),
            true
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          this.handleShowNoteSystem(
            this.listCheckedFull.map((i) => i.machineId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          this.handleShowNoteSystem(
            { id: this.listCheckedFull[0].machineId },
            false
          );
        } else {
          console.log("single select in history tab");
          this.handleShowNoteSystem({ id: selectedItem.machineId });
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
          title: `All ${this.listCheckedFull.length} machines on this current page are selected.`,
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
          title: `All ${this.total} machines are selected.`,
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
    countNotes(listReasons) {
      const count = listReasons.filter((reason) => reason.note).length;
      if (count > 1) return count + " notes";
      if (count === 1) return count + " note";
      return "";
    },
    captureQuery() {
      const [hash, stringParams] = location.hash
        .slice("#".length) // exclude #
        .split("?");

      const params = Common.parseParams(stringParams);

      if (params?.tab) {
        this.selectTab(TAB_CONFIG.find((tab) => tab.status === params.tab));
      } else {
        this.selectTab(
          TAB_CONFIG.find((tab) => tab.status === this.requestParam.tab)
        );
      }
    },
    handleShowNoteSystem(result, isMultiple, objectType, systemNoteFunction) {
      const el = this.$refs["system-note"];

      console.log(
        "🚀 ~ file: machine-downtime.vue:1363 ~ handleShowNoteSystem ~ el:",
        el
      );
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
      const { filterCode, query, tabName } = this.requestParam;
      const param = Common.param({
        filterCode,
        tabName,
        query,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.post(
          `${ALERT_MACHINE_DOWNTIME_API_BASE}/note-batch?${param}`,
          data
        );
      } catch (error) {
        console.log("machine-downtime:::handleSubmitNote:::error", error);
      }
    },
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>
<style scoped>
.label-register {
  background-color: #9521aa;
}

.label-confirmed {
  background-color: #18aa55;
}

.label-unconfirmed {
  background-color: #ff6e00;
}

.note-number {
  color: #ffffff;
  background-color: #43a1fc;
  font-size: 9px;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  text-align: center;
  margin-top: 5px;
  margin-left: 5px;
}

.icon-downtime-status {
  margin-top: 4px;
  margin-right: 3px;
  width: 15px;
  height: 15px;
  border-radius: 50%;
}

.created-icon {
  background-color: #db3b21;
}

.in_progress-icon {
  background-color: #9521aa;
}

.confirming-icon {
  background-color: #ff6e00;
}

.completed-icon {
  background-color: #18aa55;
}

.status-column {
  display: flex;
}
</style>
<style>
.dropdown-reason-item.ant-dropdown-menu-item {
  cursor: default !important;
  height: 35px;
  font-size: 15px;
}

.dropdown-reason-item.ant-dropdown-menu-item:hover {
  background-color: #ffffff !important;
}

.reason-line {
  margin-bottom: 0px !important;
  line-height: initial !important;
}
</style>
