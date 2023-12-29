<template>
  <div>
    <notify-alert
      type="DATA_SUBMISSION"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>

    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 60px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['data_approval']"></strong>

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
                v-if="userType !== tabItem?.excludeUserType"
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
                    <th style="width: 40px; vertical-align: middle !important">
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
                      v-if="userType == 'OEM' || userType == 'SUPPLIER'"
                      class="text-left __sort"
                      v-bind:class="[
                        {
                          active:
                            currentSortType === sortType.DATE ||
                            currentSortType === sortType.APPROVED_AT,
                        },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="
                        sortBy(
                          userType == 'OEM'
                            ? sortType.DATE
                            : sortType.APPROVED_AT
                        )
                      "
                    >
                      <span v-text="resources['alert_date']"></span>
                      <span class="__icon-sort"> </span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.APPROVED_AT },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.APPROVED_AT)"
                      v-if="
                        userType == 'OEM' && requestParam.status == 'confirmed'
                      "
                    >
                      <span
                        v-text="resources['approval_disapproval_date']"
                      ></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.CONFIRM_AT },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.CONFIRM_AT)"
                      v-if="
                        requestParam.status == 'confirmed' && userType !== 'OEM'
                      "
                    >
                      <span v-text="resources['confirmation_date']"></span
                      ><span class="__icon-sort"></span>
                    </th>
                    <!--                    <th class="text-center __sort"-->
                    <!--                      v-bind:class="[{ 'active': currentSortType === sortType.OP }, isDesc ? 'desc' : 'asc']"-->
                    <!--                      @click="sortBy(sortType.OP)" v-if="requestParam.status !== 'confirmed'"><span-->
                    <!--                        v-text="resources['op']"></span><span class="__icon-sort"></span></th>-->
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
                        { active: currentSortType === sortType.STATUS },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click="sortBy(sortType.STATUS)"
                    >
                      Status<span class="__icon-sort"></span>
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
                              listChecked.length >= 1 &&
                              requestParam.status != 'DELETED' &&
                              requestParam.status != 'confirmed'
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
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul
                                v-if="listChecked.length == 1"
                                class="dropdown-menu"
                                :class="[showDropdown ? 'show' : '']"
                              >
                                <li v-if="userType !== 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeStatus(
                                        listChecked[0],
                                        'CONFIRM',
                                        false
                                      )
                                    "
                                    >Confirm</a
                                  >
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeStatus(
                                        listChecked[0],
                                        'APPROVE',
                                        false
                                      )
                                    "
                                    >Approve</a
                                  >
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeStatus(
                                        listChecked[0],
                                        'DISAPPROVE',
                                        false
                                      )
                                    "
                                    >Disapprove</a
                                  >
                                </li>
                              </ul>
                              <ul
                                v-else
                                class="dropdown-menu"
                                :class="[showDropdown ? 'show' : '']"
                              >
                                <li v-if="userType !== 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeSelectedStatus('CONFIRM')
                                    "
                                    >Confirm</a
                                  >
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeSelectedStatus('APPROVE')
                                    "
                                    >Approve</a
                                  >
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a
                                    href="#"
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeSelectedStatus('DISAPPROVE')
                                    "
                                    >Disapprove</a
                                  >
                                </li>
                              </ul>
                            </div>
                          </div>
                          <!-- <div
                            v-if="
                              listChecked.length == 1 &&
                              requestParam.status != 'DELETED' &&
                              requestParam.status == 'confirmed'
                            "
                            class="action-item"
                            @click="showHistories({
                              id: listCheckedFull[0].moldId,
                              equipmentCode: listCheckedFull[0].moldCode,
                            })"
                          >
                            <span>View</span>
                            <i class="icon-action icon-view"></i>
                          </div> -->
                          <div
                            v-if="requestParam.status != 'DELETED'"
                            class="action-item"
                            @click="showSystemNoteModal()"
                          >
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                        </div>
                      </div>
                    </th>
                  </tr>
                </thead>

                <tbody v-show="!isLoading" class="op-list">
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
                    <td class="text-left">
                      <a
                        href="#"
                        @click.prevent="showChart(result.mold)"
                        style="color: #1aaa55"
                        class="mr-1"
                        ><i class="fa fa-bar-chart"></i
                      ></a>
                      <a
                        href="#"
                        @click.prevent="showMoldDetails({ id: result.moldId })"
                      >
                        {{ result.moldCode }}
                      </a>
                    </td>
                    <td class="text-left">
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
                        result.companyType
                      }}</span>
                    </td>
                    <td class="text-left">
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
                    <td
                      class="text-left"
                      v-if="userType == 'OEM' || userType == 'SUPPLIER'"
                    >
                      <span class="font-size-14">
                        {{ convertDate(result.creationDateTime) }}
                      </span>
                      <div>
                        <span class="font-size-11-2">
                          {{ convertTimeString(result.creationDateTime) }}
                        </span>
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-if="requestParam.status === 'confirmed'"
                    >
                      <span class="font-size-14">
                        {{ convertDate(result.approvalDateTime) }}
                      </span>
                      <div>
                        <span class="font-size-11-2">
                          {{ convertTimeString(result.approvalDateTime) }}
                        </span>
                      </div>
                    </td>
                    <!--                    <td style="vertical-align: middle!important;" class="text-center op-status-td"-->
                    <!--                      v-if="requestParam.status !== 'confirmed'">-->
                    <!--                      <span-->
                    <!--                        v-if="result.mold.operatingStatus == 'WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                    <!--                        class="op-active label label-success"></span>-->
                    <!--                      <span-->
                    <!--                        v-if="result.mold.operatingStatus == 'IDLE' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                    <!--                        class="op-active label label-warning"></span>-->
                    <!--                      <span-->
                    <!--                        v-if="result.mold.operatingStatus == 'NOT_WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                    <!--                        class="op-active label label-inactive"></span>-->
                    <!--                      <span-->
                    <!--                        v-if="result.mold.operatingStatus == 'DISCONNECTED' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')"-->
                    <!--                        class="op-active label label-danger"></span>-->
                    <!--                    </td>-->
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
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                    >
                      <div
                        class="data-submission"
                        :style="{
                          width: '87px',
                          height: '31px',
                          margin: 'auto',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          backgroundColor:
                            result.alertStatus == 'PENDING'
                              ? '#ac24d3'
                              : result.alertStatus == 'APPROVED'
                              ? '#26a958'
                              : '#b8b8b8',
                          borderRadius: '4px',
                        }"
                      >
                        <span
                          :style="{
                            color: '#fff',
                          }"
                        >
                          {{
                            result.alertStatus == "PENDING"
                              ? "Pending"
                              : result.alertStatus == "DISAPPROVED"
                              ? "Disapproved"
                              : "Approved"
                          }}
                        </span>
                      </div>
                    </td>
                  </tr>
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
    <submission-history
      id="data-submission-history"
      :resources="resources"
    ></submission-history>
    <message-confirm
      :is-visible="isShowReason"
      :resources="resources"
      :data="reasonParam"
      @close="handleCloseReasonDialog"
      :callback-message-form="callbackMessageForm"
    ></message-confirm>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
  </div>
</template>

<script>
const ALERT_DATA_APPROVAL_API_BASE = "/api/common/alr-dat-apr";
const TAB_CONFIG = [
  {
    name: "Alert",
    key: "alert",
    status: "alert",
  },
  {
    name: "Approved",
    key: "approved",
    status: "APPROVED",
    excludeUserType: "OEM",
  },
  {
    name: "Disapproved",
    key: "disapproved",
    status: "DISAPPROVED",
    excludeUserType: "OEM",
  },
  {
    name: "History Log",
    key: "history_log",
    status: "confirmed",
  },
];

module.exports = {
  name: "mold-data-submission",
  components: {
    "submission-history": httpVueLoader(
      "/components/data-submission-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
  },
  props: {
    resources: Object,
    ruleOpStatus: Object,
    searchText: String,
    reloadKey: [String, Number],
    enabledSearchBar: {
      type: Boolean,
      default: true,
    },
    showNote: Function,
    filterCode: String,
  },
  data() {
    return {
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
        tabName: TAB_CONFIG[0].name,
      },

      listCategories: [],
      sortType: {
        ID: "moldCode",
        COMPANY: "companyName",
        LOCATION: "locationName",
        CT: "cycleTimeStatus",
        APPROVED_AT: "approveDateTime",
        CONFIRM_AT: "confirmedAt",
        OP: "mold.operatingStatus",
        VARIANCE: "variance",
        DATE: "createDateTime",
        STATUS: "alertStatus",
        TOOLING_STATUS: "toolingStatus",
        COUNTER_STATUS: "sensorStatus",
      },
      currentSortType: "id",
      isDesc: true,
      listChecked: [],
      listCheckedTracked: {},
      isAllTracked: [],
      isLoading: false,
      alertOn: true,
      showDropdown: false,
      listCheckedFull: [],
      cancelToken: undefined,
      isShowReason: false,
      reasonParam: {},
      listAllIds: [],
      tabConfig: TAB_CONFIG,
      activeTab: TAB_CONFIG[0].key,
      tabs: [],
      selectType: "",
      systemNoteFunction: "",
    };
  },
  computed: {
    userType() {
      return headerVm?.userType;
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
    isAll(newVal) {
      console.log("@watch:isAll", newVal);
    },
  },
  methods: {
    changeAlertGeneration(alertType, isInit, alertOn) {
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
    },

    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value === id;
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

    changeSelectedStatus(status) {
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
      this.changeStatus(listChecked, status, true);
    },

    closeShowDropDown() {
      this.showDropdown = false;
    },

    tab(tab) {
      this.total = null;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.tabName = tab.name;
      this.activeTab = tab.key;
      if (tab.status == "APPROVED" || tab.status == "DISAPPROVED") {
        this.requestParam.notificationStatus = tab.status;
        this.requestParam.status = "alert";
      } else {
        this.requestParam.notificationStatus = "";
        this.requestParam.status = tab.status;
      }
      this.listChecked = [];
      this.listCheckedFull = [];
      this.listCheckedTracked[this.requestParam.status] = {};
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
        child.showChart(mold, "UPTIME", "DAY", tab ? tab : "switch-graph");
      }
    },
    showMoldDetails(mold, data) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },

    showHistories(mold) {
      console.log("mold: ", mold);

      var child = Common.vue.getChild(this.$children, "submission-history");
      if (child != null) {
        child.show({
          ...mold,
          userType: this.userType,
        });
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
          equipmentTitle: "Sensor ID",
          equipmentCode: data.counterCode,

          title: "Misconfigure Information",
          messageTitle: "Message",
          alertDate: data.notificationDateTime,
          confirmDate: data.confirmedDateTime,
          name: data.confirmedBy,
          message: data.message,
        };

        child.showMessageDetails(data);
      }
    },
    changeStatusOld(data, status, isBottom) {
      let param = {};
      if (isBottom) {
        param = {
          moldIds: data,
          action: status,
          message: "",
        };
      } else {
        param = {
          moldIds: [data],
          action: status,
          message: "",
        };
      }

      var data = {
        title: "Message",
        messageTitle: "",
        buttonTitle: "Submit",
        placeholder: "Please provide reason for disapproval.",
        url: "/api/molds/data-submission/confirm",
        param: param,
        method: "POST",
      };

      if (status == "DISAPPROVE") {
        var messageFormComponent = Common.vue.getChild(
          this.$parent.$children,
          "message-form"
        );
        if (messageFormComponent != null) {
          this.callbackMessageForm = this.callbackMessageForm.bind(this);
          messageFormComponent.showModal(data);
        }
      } else {
        if (status == "CONFIRM") {
          data = {
            title: "Confirm",
            messageTitle: "Message",
            buttonTitle: "Confirm",
            placeholder: "",
            url: "/api/molds/data-submission/confirm",
            param: param,
            method: "POST",
          };
          var messageFormComponent = Common.vue.getChild(
            this.$parent.$children,
            "message-form"
          );
          if (messageFormComponent != null) {
            this.callbackMessageForm = this.callbackMessageForm.bind(this);
            messageFormComponent.showModal(data);
          }

          return;
        }
        axios
          .post(data.url, data.param)
          .then((response) => {
            Object.keys(this.listCheckedTracked).forEach((status) => {
              if (status === this.requestParam.status) {
                this.listCheckedTracked[status] = {};
              }
            });
            this.isAllTracked = this.isAllTracked.filter(
              (item) =>
                item.page !== this.requestParam.page &&
                item.status !== this.requestParam.status
            );

            this.tab(this.requestParam.status);
          })
          .catch(function (error) {
            console.log(error.response);
          });
      }
    },
    changeStatus(data, status, isBottom) {
      let param = {};
      if (isBottom) {
        param = {
          moldIds: data,
          action: status,
          message: "",
        };
      } else {
        param = {
          moldIds: [data],
          action: status,
          message: "",
        };
      }

      var data = {
        title: "Message",
        messageTitle: "",
        buttonTitle: "Submit",
        placeholder: "Please provide reason for disapproval.",
        url: "/api/molds/data-submission/confirm",
        param: param,
        method: "POST",
      };

      if (status == "DISAPPROVE") {
        data.title = this.resources["reason_for_disapproval"];

        this.isShowReason = true;
        this.reasonParam = data;
      } else if (status == "CONFIRM") {
        data = {
          title: "Confirm",
          messageTitle: "Message",
          buttonTitle: "Confirm",
          placeholder: "",
          url: "/api/molds/data-submission/confirm",
          param: param,
          method: "POST",
        };
        this.isShowReason = true;
        this.reasonParam = data;

        return;
      } else {
        data = {
          ...data,
          title: this.resources["reason_for_approval"],
          messageTitle: "Message",
          buttonTitle: "Submit",
          placeholder: "Please provide reason for approval.",
          url: "/api/molds/data-submission/confirm",
          param: param,
          method: "POST",
        };
        this.isShowReason = true;
        this.reasonParam = data;
      }
    },

    handleCloseReasonDialog() {
      this.isShowReason = false;
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

    convertTime(s) {
      const dtFormat = new Intl.DateTimeFormat("en-GB", {
        timeStyle: "medium",
        timeZone: "UTC",
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : "";
    },

    convertDate(s) {
      return s?.slice(0, 10);
    },

    convertTimeString(s) {
      return s?.slice(10);
    },

    log(res) {
      console.log("res", res);
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
      const url = new URL(ALERT_DATA_APPROVAL_API_BASE, location.origin);
      url.search = params;

      this.isLoading = true;

      try {
        const { data: dataApprovalAlertData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });

        this.isLoading = false;
        this.total = dataApprovalAlertData.totalElements;
        this.results = dataApprovalAlertData.content;
        this.tabs = dataApprovalAlertData.tabs;

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

        console.log(
          "mold-data-submission:::paging:::this.results:::",
          this.results,
          dataApprovalAlertData
        );

        this.pagination = Common.getPagingData(dataApprovalAlertData);
        Common.handleNoResults("#app", this.results.length);
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
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        this.isLoading = false;
        console.log("mold-data-submission:::paging:::error:::", error);
      } finally {
        this.isLoading = false;
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

    efficiencyRatio(moldMisconfigured) {
      return (
        (moldMisconfigured.uptimeSeconds / moldMisconfigured.currentSeconds) *
        100
      ).toFixed(1);
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
      this.listChecked = [];
      this.listCheckedFull = [];
      this.paging(1);
      if (this.$parent && typeof this.$parent.reloadAlertCout === "function") {
        this.$parent.reloadAlertCout();
      }
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
    showSystemNoteModal(selectedItem) {
      const config = {
        objectType: null,
        systemNoteFunction: "DATA_SUBMISSION_ALERT",
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
        console.log("single select in history tab");
        this.handleShowNoteSystem(
          { id: selectedItem.moldId },
          config.objectType,
          config.systemNoteFunction
        );
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
        const param = Common.param({
          ...this.requestParam,
          pageType: "DATA_APPROVAL_ALERT",
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
      this.systemNoteFunction = systemNoteFunction;
      const el = this.$refs["system-note"];
      console.log("handleShowNoteSystem", result, isMultiple, objectType, el);
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
          `${ALERT_DATA_APPROVAL_API_BASE}/note-batch?${param}`,
          data
        );
      } catch (error) {
        console.log("mold-data-submission:::handleSubmitNote:::error", error);
      }
    },
  },
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
    if (this.searchText) {
      this.requestParam.query = this.searchText;
    }
    this.paging(1);
  },
};
</script>

<style scoped></style>
