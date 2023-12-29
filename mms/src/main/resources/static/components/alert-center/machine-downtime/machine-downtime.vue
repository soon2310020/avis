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
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.tab === 'ALERT' }"
                  href="#"
                  @click.prevent="tab('ALERT')"
                >
                  <span v-text="resources['alert']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.tab === 'ALERT'"
                  >
                    {{ total }}
                  </span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.tab === 'HISTORY' }"
                  href="#"
                  @click.prevent="tab('HISTORY')"
                >
                  <span v-text="resources['history_log']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.tab === 'HISTORY'"
                  >
                    {{ total }}
                  </span>
                </a>
              </li>
              <customization-modal
                v-show="alertOn && requestParam.tab === 'HISTORY'"
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
                      v-for="(column, index) in requestParam.tab === 'ALERT'
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
                        v-for="(column, columnIndex) in requestParam.tab ===
                        'ALERT'
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
                            {{
                              convertDateString(
                                formatToDateTime(result.updatedAt)
                              )
                            }}
                          </div>
                        </td>
                        <td
                          class="text-left"
                          v-else-if="column.field === 'toolingId'"
                        >
                          <div v-if="result.mold != null">
                            <a
                              href="#"
                              @click.prevent="showChart(result.mold)"
                              style="color: #1aaa55"
                              class="mr-1 font-size-14"
                              ><i class="fa fa-bar-chart"></i
                            ></a>
                            <a
                              href="#"
                              @click.prevent="showMoldDetails(result.mold)"
                            >
                              {{ result.mold.equipmentCode }}
                            </a>
                            <div class="small text-muted font-size-11-2">
                              <!--Updated at {{result.updatedDate}}-->
                              <span v-text="resources['updated_on']"></span>
                              <!--{{result.lastShotDate}}-->
                              {{ formatToDate(result.mold.lastShotAt) }}
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
                            {{
                              convertDateString(
                                formatToDateTime(result.startTime)
                              )
                            }}
                            <div class="small text-muted font-size-11-2">
                              {{
                                convertTimeString(
                                  formatToDateTime(result.startTime)
                                )
                              }}
                            </div>
                          </div>
                        </td>
                        <td
                          v-else-if="
                            column.field === 'endDowntime' &&
                            requestParam.tab === 'HISTORY'
                          "
                          class="text-left"
                        >
                          <div class="font-size-14">
                            {{
                              convertDateString(
                                formatToDateTime(result.endTime)
                              )
                            }}
                            <div class="small text-muted font-size-11-2">
                              {{
                                convertTimeString(
                                  formatToDateTime(result.endTime)
                                )
                              }}
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
                            :reasons="result.machineDowntimeReasonList"
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
                                result.downtimeStatus === 'DOWNTIME'
                                  ? 'created-icon'
                                  : result.downtimeStatus === 'REGISTERED'
                                  ? 'in_progress-icon'
                                  : result.downtimeStatus === 'UNCONFIRMED'
                                  ? 'confirming-icon'
                                  : 'completed-icon',
                              ]"
                            ></div>
                            <div
                              v-text="
                                result.downtimeStatus === 'DOWNTIME'
                                  ? 'Downtime'
                                  : result.downtimeStatus === 'REGISTERED'
                                  ? 'Registered'
                                  : result.downtimeStatus === 'UNCONFIRMED'
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
                            v-if="result.confirmedBy"
                            :resources="resources"
                            :user-list="[result.confirmedBy]"
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
                          <a-tooltip placement="bottom">
                            {{ countNotes(result.machineDowntimeReasonList) }}
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
                                    reason, index
                                  ) in result.machineDowntimeReasonList"
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
                                      >{{ reason.codeData.title }}</span
                                    >
                                    <span class="list-note-item__note">{{
                                      reason.note
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
    <chart-mold :resources="resources" :user-type="userType"></chart-mold>
    <file-previewer></file-previewer>
    <mold-details :resources="resources"></mold-details>
  </div>
</template>

<script>
module.exports = {
  components: {
    "register-machine-downtime": httpVueLoader(
      "/components/alert-center/machine-downtime/register-machine-downtime.vue"
    ),
    "reason-dropdown": httpVueLoader(
      "/components/alert-center/machine-downtime/reason-dropdown.vue"
    ),
  },
  props: {
    resources: Object,
    machineId: Number,
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
        query: "",
        tab: "ALERT",
        sort: "id,desc",
        page: 1,
        machineIdList: [],
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
    };
  },
  computed: {
    downtimeStatus() {
      if (this.listChecked.length === 1) {
        const result = this.results.filter(
          (data) => data.id === this.listChecked[0]
        );
        if (result) {
          return result[0].downtimeStatus;
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
      if (!this.results.length) return false;
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
    enableShowNote() {
      return this.requestParam.tab === "ALERT";
    },
    enableCreateWorkOrder() {
      return this.listChecked.length === 1 && this.requestParam.tab === "ALERT";
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
      const isInHistory = this.requestParam.tab === "HISTORY";
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
            this.requestParam.machineIdList = [this.machineId];
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
          sortField: "toolingId",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "location",
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
          sortField: "downtimeStatus",
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
          sortField: "toolingId",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["location"],
          field: "location",
          default: true,
          sortable: true,
          sortField: "location",
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
          sortField: "downtimeStatus",
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

    log(s, e) {
      console.log("molds log", s, e);
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

    tab(status) {
      this.total = null;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.tab = status;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    paging(pageNumber, uncheck = false) {
      console.log("paging", pageNumber);

      Common.handleNoResults("#app", 1);
      const self = this;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      const param = Common.param(self.requestParam);
      self.isLoading = true;
      axios
        .get("/api/machine-downtime-alert?lastAlert=true&" + param)
        .then(function (response) {
          self.isLoading = false;
          if (self.requestParam.machineIdList) {
            self.requestParam.machineIdList = [];
          }
          self.total = response.data.totalElements;
          if (self.requestParam.tab === "HISTORY") {
            const loadData = response.data.content;
            self.results = loadData.map((item) => {
              if (typeof item.mold !== "object") {
                const find = loadData.find(
                  (o) => o.mold && o.mold.id === item.mold
                );
                if (find) {
                  item.mold = find.mold;
                }
              }
              return item;
            });
            console.log("map item", loadData, self.results);
          } else {
            self.results = response.data.content;
          }
          self.pagination = Common.getPagingData(response.data);
          if (uncheck) {
            self.resetAfterUpdate();
          }

          if (self.total === self.listAllIds.length) {
            self.listChecked = [];
            self.listCheckedFull = [];
          }

          Common.handleNoResults("#app", self.results.length);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
          self.isLoading = false;
        });
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
          this.showNote(
            this.listAllIds.map((i) => i.mainObjectId),
            true
          );
        } else if (this.listCheckedFull.length > 1) {
          console.log("multiple select");
          this.showNote(
            this.listCheckedFull.map((i) => i.machineId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          this.showNote({ id: this.listCheckedFull[0].machineId }, false);
        } else {
          console.log("single select in history tab");
          this.showNote({ id: selectedItem.machineId });
        }
      }
    },

    handleSelectAll(actionType) {
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
        this.tab(params.tab);
      } else {
        this.tab(this.requestParam.tab);
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
