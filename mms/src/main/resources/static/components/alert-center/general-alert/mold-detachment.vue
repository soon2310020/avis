<template>
  <div>
    <notify-alert
      type="DETACHMENT"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>

    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 60px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['detachment']"></strong>
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
                  :request-param="requestParam"
                  :placeholder="resources['search_by_columns_shown']"
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
            <i class="fa fa-align-justify"></i> Striped Table
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

              <customization-modal
                v-show="alertOn"
                style="position: absolute; right: 20px"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              />
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
                      style="
                        width: 40px;
                        vertical-align: middle !important;
                        text-align: center;
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
                    </th>
                    <template
                      v-for="(column, index) in allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <th
                        v-if="
                          column.field === 'confirmedAt' &&
                          requestParam.status === 'history'
                        "
                        :class="[
                          { active: currentSortType === 'confirmedAt' },
                          isDesc ? 'desc' : 'asc',
                          'text-left',
                          '__sort',
                        ]"
                        @click="sortBy('confirmDateTime')"
                      >
                        <span v-text="resources['confirmation_date']"></span>
                        <span class="__icon-sort"></span>
                      </th>
                      <th
                        :class="[
                          { active: currentSortType === 'moldLocationStatus' },
                          isDesc ? 'desc' : 'asc',
                          'text-center',
                          '__sort',
                        ]"
                        v-else-if="
                          column.field === 'moldLocationStatus' &&
                          requestParam.status === 'alert'
                        "
                      >
                        <span v-text="resources['status']"></span>
                        <span
                          @click="sortBy('moldLocationStatus')"
                          class="__icon-sort"
                        ></span>
                      </th>

                      <th
                        :key="index"
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
                        v-if="
                          column.field !== 'confirmedAt' &&
                          column.field !== 'moldLocationStatus'
                        "
                      >
                        <span>{{ column.label }}</span>
                        <span v-if="column.sortable" class="__icon-sort"></span>
                      </th>
                    </template>
                    <th
                      class="text-center"
                      v-if="requestParam.status === 'history'"
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
                              <div
                                title="Change status"
                                class="change-status"
                                @click="showDropdown = !showDropdown"
                              >
                                <span>Change Status</span>
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
                  <tr
                    v-show="!isLoading"
                    v-for="(result, index, id) in results"
                    :id="result.id"
                  >
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
                    <template
                      v-for="(column, columnIndex) in allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <td class="text-left" v-if="column.field === 'toolingId'">
                        <a
                          href="#"
                          @click.prevent="showChart(result.mold)"
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
                      <td
                        class="text-left"
                        v-else-if="column.field === 'company'"
                      >
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
                          result?.companyCode || ""
                        }}</span>
                      </td>
                      <!-- Location -->
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
                          >
                            {{ result.locationCode }}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">
                          {{ result.locationCode }}
                        </span>
                      </td>
                      <!-- Detachment Date -->
                      <td
                        class="text-left"
                        v-else-if="column.field === 'detachmentTime'"
                      >
                        {{ convertDate(result.detachmentDateTime) }}
                        <!-- {{formatToDateTime(result.detachmentTime)}} -->
                        <div>
                          <span class="font-size-11-2">
                            {{ convertTimeString(result.detachmentDateTime) }}
                            <!-- {{formatToDateTime(result.detachmentTime)}} -->
                          </span>
                        </div>
                      </td>
                      <!-- Confirmation Date -->
                      <td
                        class="text-left"
                        v-else-if="
                          column.field === 'confirmedAt' &&
                          requestParam.status === 'history'
                        "
                      >
                        <div>
                          {{ convertDate(result.confirmDateTime) }}
                          <!-- {{formatToDateTime(result.detachmentTime)}} -->
                          <div>
                            <span class="font-size-11-2">
                              {{ convertTimeString(result.confirmDateTime) }}
                              <!-- {{formatToDateTime(result.detachmentTime)}} -->
                            </span>
                          </div>
                        </div>
                      </td>

                      <!-- OP -->
                      <!--                      <td style="vertical-align: middle!important;" class="text-center op-status-td"-->
                      <!--                        v-else-if="column.field === 'operatingStatus'">-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus === 'WORKING' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK')"-->
                      <!--                          class="op-active label label-success"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus === 'IDLE' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK')"-->
                      <!--                          class="op-active label label-warning"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus === 'NOT_WORKING' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK')"-->
                      <!--                          class="op-active label label-inactive"></span>-->
                      <!--                        <span-->
                      <!--                          v-if="result.mold.operatingStatus === 'DISCONNECTED' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK')"-->
                      <!--                          class="op-active label label-danger"></span>-->
                      <!--                        <span v-else> </span>-->
                      <!--                      </td>-->
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
                      <!--status -->
                      <td
                        style="vertical-align: middle !important"
                        class="text-center op-status-td"
                        v-else-if="
                          requestParam.status === 'alert' &&
                          column.field === 'moldLocationStatus'
                        "
                      >
                        <span
                          class="label label-danger"
                          v-if="result.equipmentStatus === 'DETACHED'"
                          >Detached</span
                        >
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'machineCode'"
                      >
                        <div v-if="result.machineCode">
                          <a
                            href="#"
                            @click.prevent="
                              showMachineDetails(result.machineId)
                            "
                          >
                            {{ result.machineCode }}
                          </a>
                        </div>
                      </td>
                    </template>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center"
                      v-if="requestParam.status === 'history'"
                    >
                      <div
                        class="d-flex align-items-center justify-content-center"
                      >
                        <span
                          v-if="requestParam.status === 'history'"
                          style="cursor: pointer"
                          v-on:click="
                            showRelocationConfirmHistory({
                              id: result.moldId,
                              equipmentCode: result.moldCode,
                            })
                          "
                          ><img
                            height="18"
                            src="/images/icon/view.png"
                            alt="View"
                        /></span>

                        <a
                          href="javascript:void(0)"
                          @click="
                            showSystemNoteModal({
                              id: result.id,
                              moldId: result.moldId,
                            })
                          "
                          title="Add note"
                          class="table-action-link note"
                        >
                          <img src="/images/icon/note.svg" alt="Add note" />
                        </a>
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

    <detachment-confirm-history
      :resources="resources"
    ></detachment-confirm-history>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
  </div>
</template>

<script>
const ALERT_DETACHMENT_API_BASE = "/api/common/alr-det";

const TAB_CONFIG = [
  {
    name: "Alert",
    key: "alert",
    status: "alert",
  },
  {
    name: "History Log",
    key: "history_log",
    status: "history",
  },
];
module.exports = {
  name: "mold-detachment",
  components: {
    "detachment-confirm-history": httpVueLoader(
      "/components/detachment-confirm-history.vue"
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
      statusCodes: [],
      listCategories: [],
      isLoading: false,
      alertOn: true,
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},

      isAllTracked: [],
      listCheckedFull: [],
      currentSortType: "id",
      isDesc: true,
      cancelToken: undefined,
      allColumnList: [],
      pageType: "DETACHMENT_ALERT",
      listAllIds: [],
      tabConfig: TAB_CONFIG,
      activeTab: TAB_CONFIG[0].key,
      tabs: [],
      selectType: "",
      systemNoteFunction: "",
      idNoteBatchAction: "",
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
          label: this.resources["detachment_time"],
          field: "detachmentTime",
          default: true,
          sortable: true,
          sortField: "detachmentDateTime",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["confirmation_date"],
          field: "confirmedAt",
          default: true,
          sortable: true,
          sortField: "confirmDateTime",
          defaultSelected: true,
          defaultPosition: 4,
        },
        // { label: this.resources['op'], field: 'operatingStatus', default: true, sortable: true, sortField: 'mold.operatingStatus', defaultSelected: true, defaultPosition: 5 },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "toolingStatus",
          defaultPosition: 5,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "sensorStatus",
          defaultPosition: 6,
        },
        // { label: this.resources['status'], field: 'moldLocationStatus', default: true, sortable: true, sortField: 'moldLocationStatus', defaultSelected: true, defaultPosition: 6 },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "machineCode",
        },
      ];
      try {
        this.resetColumnsListSelected();
        this.getColumnListSelected();
        Common.changeDeletedColumn(this, "TOOLING", this.allColumnList, "mold");
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
    changeAlertGeneration(alertType, isInit, alertOn) {
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
    },

    tab(tab) {
      this.total = null;
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

    closeShowDropDown() {
      this.showDropdown = false;
    },
    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });
      return findIndex !== -1;
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
      }
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
    showSystemNoteModal(selectedItem) {
      const config = {
        objectType: null,
        systemNoteFunction: "DETACHMENT_ALERT",
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
        this.idNoteBatchAction = selectedItem?.id || "";
        this.handleShowNoteSystem(
          { id: selectedItem.moldId },
          false,
          config.objectType,
          config.systemNoteFunction
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
        child.showLocationHistory(mold);
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
    showRelocationConfirmHistory(mold) {
      var child = Common.vue.getChild(
        this.$children,
        "detachment-confirm-history"
      );
      if (child != null) {
        console.log("child: ", child);
        child.show(mold);
      }
    },
    convertDate(s) {
      console.log("1234", s);
      if (s) {
        return s?.slice(0, 10);
      } else return "";
    },

    convertTimeString(s) {
      if (s) {
        return s?.slice(10);
      } else return "";
    },
    convertTime(s) {
      const dtFormat = new Intl.DateTimeFormat("en-GB", {
        timeStyle: "medium",
        timeZone: "UTC",
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : "";
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
    changeStatus(data, status) {
      var param = {
        id: data.id,
        misconfigureStatus: status,
        message: "",
      };

      var data = {
        title: "Confirm",
        messageTitle: "Message",
        buttonTitle: "Confirm",

        url: "/api/molds" + "/detachment/" + param.id + "/confirm",
        param: param,
      };

      var messageFormComponent = Common.vue.getChild(
        this.$parent.$children,
        "message-form"
      );
      if (messageFormComponent != null) {
        this.callbackMessageForm = this.callbackMessageForm.bind(this);
        messageFormComponent.showModal(data);
      }
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
      var self = this;
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();

      const params = Common.param(this.requestParam);
      const url = new URL(ALERT_DETACHMENT_API_BASE, location.origin);
      url.search = params;

      this.isLoading = true;
      try {
        const { data: detachmentAlertData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });
        this.isLoading = false;
        this.total = detachmentAlertData.totalElements;
        this.results = detachmentAlertData.content;
        this.tabs = detachmentAlertData.tabs;
        this.pagination = Common.getPagingData(detachmentAlertData);
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
          "mold-detachment:::paging:::this.results:::",
          this.results,
          detachmentAlertData
        );
        // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
        this.setResultObject();

        // 카테고리 정보 저장.
        this.setListCategories();
        Common.handleNoResults("#app", this.results.length);

        let pageInfo =
          this.requestParam.page === 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        this.isLoading = false;
        console.log("mold-detachment:::paging:::error:::", error);
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
            break;
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
      this.paging(1);
      if (this.$parent && typeof this.$parent.reloadAlertCout === "function") {
        this.$parent.reloadAlertCout();
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

      // TODO: recheck listCheckedTracked
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
        query,
        tabName,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]:
          listId.length > 0 ? listId : [this.idNoteBatchAction],
      });
      try {
        await axios.post(
          `${ALERT_DETACHMENT_API_BASE}/note-batch?${param}`,
          data
        );
      } catch (error) {
        console.log("mold-detachment:::handleSubmitNote:::error", error);
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
    this.callSetAllColumnList();
    // this.$nextTick(function () {
    // 모든 화면이 렌더링된 후 실행합니다.
    var self = this;
    var uri = URI.parse(location.href);
    if (uri.fragment == "outside_l1") {
      self.tab("L1");
    } else if (uri.fragment == "outside_l2") {
      self.tab("L2");
    } else {
      self.paging(1);
    }

    // })
  },
};
</script>

<style scoped></style>
