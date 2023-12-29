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
                  >
                    {{ total }}
                  </span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'history' }"
                  href="#"
                  @click.prevent="tab('history')"
                  ><span v-text="resources['history_log']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'history'"
                  >
                    {{ total }}
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
                      >
                        <span v-text="resources['confirmation_date']"></span>
                        <span
                          @click="sortBy('confirmedAt')"
                          class="__icon-sort"
                        ></span>
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
                          @click.prevent="showMoldDetails(result.mold)"
                        >
                          {{ result.mold.equipmentCode }}
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
                              showCompanyDetailsById(
                                result.mold.companyIdByLocation
                              )
                            "
                          >
                            {{ result.mold.companyName }}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">{{
                          result.mold.companyCode
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
                            @click.prevent="showLocationHistory(result)"
                          >
                            {{ result.mold.locationName }}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">
                          {{ result.mold.locationCode }}
                        </span>
                      </td>
                      <!-- Detachment Date -->
                      <td
                        class="text-left"
                        v-else-if="column.field === 'detachmentTime'"
                      >
                        {{
                          convertDate(formatToDateTime(result.detachmentTime))
                        }}
                        <!-- {{formatToDateTime(result.detachmentTime)}} -->
                        <div>
                          <span class="font-size-11-2">
                            {{
                              convertTimeString(
                                formatToDateTime(result.detachmentTime)
                              )
                            }}
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
                          <span class="font-size-14">
                            {{ convertTime(result.confirmedAt) }}
                            <!-- {{result.confirmedAt}} -->
                          </span>
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
                          v-on:click="showRelocationConfirmHistory(result.mold)"
                          ><img
                            height="18"
                            src="/images/icon/view.png"
                            alt="View"
                        /></span>

                        <a
                          href="javascript:void(0)"
                          @click="showSystemNoteModal(result)"
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
  </div>
</template>

<script>
module.exports = {
  name: "mold-detachment",
  components: {
    "detachment-confirm-history": httpVueLoader(
      "/components/detachment-confirm-history.vue"
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
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "alert",
        sort: "id,desc",
        page: 1,

        operatingStatus: "",
        notificationStatus: "",
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
    };
  },
  computed: {
    userType() {
      return headerVm?.userType;
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
    resources: {
      handler(newVal) {
        console.log("resource change");
        if (newVal && Object.keys(newVal)?.length) {
          console.log("set columns");
          this.setAllColumnList();
        }
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
          label: this.resources["detachment_time"],
          field: "detachmentTime",
          default: true,
          sortable: true,
          sortField: "detachmentTime",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["confirmation_date"],
          field: "confirmedAt",
          default: true,
          sortable: true,
          sortField: "confirmedAt",
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
          sortField: "mold.machine.machineCode",
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

    tab(status) {
      this.total = null;
      this.requestParam.status = status;
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
        this.showNote(
          this.listAllIds.map((i) => i.mainObjectId),
          true,
          config.objectType,
          config.systemNoteFunction
        );
      } else if (this.listCheckedFull.length > 1) {
        console.log("multiple select");
        this.showNote(
          this.listCheckedFull.map((i) => i.moldId),
          true,
          config.objectType,
          config.systemNoteFunction
        );
      } else if (this.listCheckedFull.length === 1) {
        console.log("single select");
        this.showNote(
          { id: this.listCheckedFull[0].moldId },
          false,
          config.objectType,
          config.systemNoteFunction
        );
      } else {
        console.log("single select in history tab");
        this.showNote(
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

    paging(pageNumber) {
      Common.handleNoResults("#app", 1);
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(self.requestParam);
      self.isLoading = true;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/molds" + "/detachment?lastAlert=true&" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);

          if (self.total === self.listAllIds.length) {
            self.listChecked = [];
            self.listCheckedFull = [];
          }

          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          self.setResultObject();

          // 카테고리 정보 저장.
          self.setListCategories();

          Common.handleNoResults("#app", self.results.length);

          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          self.isLoading = false;
          console.log(error.response);
        });
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
    captureQuery() {
      const [hash, stringParams] = location.hash.slice("#".length).split("?");
      const params = Common.parseParams(stringParams);
      // TODO(ducnm2010): update to list after having api
      // if (params?.id) {
      //   this.requestParam.id = params.id;
      // }
      if (params?.tab) {
        this.tab(params.tab);
      } else {
        this.tab(this.requestParam.status);
      }
    },
  },
  created() {
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
  destroyed() {
    window.addEventListener("popstate", this.captureQuery);
  },
};
</script>

<style scoped></style>
