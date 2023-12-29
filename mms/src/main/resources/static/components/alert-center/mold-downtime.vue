<template>
  <div>
    <notify-alert
      type="DOWNTIME"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    >
    </notify-alert>

    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 60px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['tooling_downtime']"></strong>

            <div class="card-header-actions">
              <span class="card-header-action">
                <small v-show="alertOn" class="text-muted"
                  ><span v-text="resources['total'] + ':'"></span>
                  {{ total }}
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
          <div class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{
                    active:
                      requestParam.status == 'alert' &&
                      requestParam.notificationStatus == '',
                  }"
                  href="#"
                  @click.prevent="tab('alert')"
                  ><span v-text="resources['alert']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="
                      requestParam.status == 'alert' &&
                      requestParam.notificationStatus == ''
                    "
                    >{{ total }}</span
                  >
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
                    >{{ total }}</span
                  >
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
                <colgroup>
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
                    class="tr-sort"
                  >
                    <th style="width: 40px; vertical-align: middle !important">
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
                          ['toolingId', 'company', 'machineCode'].includes(
                            column.field
                          )
                        "
                      >
                        <th
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
                          <span>{{ column.label }}</span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
                      <template
                        v-if="
                          [
                            'location',
                            'lastUptime',
                            'downtimeDuration',
                          ].includes(column.field)
                        "
                      >
                        <th
                          @click="sortBy(column.sortField)"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                        >
                          <span>{{ column.label }}</span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
                      <template
                        v-else-if="['confirmationDate'].includes(column.field)"
                      >
                        <th
                          @click="sortBy(column.sortField)"
                          v-if="requestParam.status == 'history'"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                        >
                          <span>{{ column.label }}</span>
                          <span
                            style="margin-left: 15px !important"
                            class="__icon-sort"
                          ></span>
                          <a-tooltip placement="bottom">
                            <template slot="title">
                              <div style="padding: 6px 8px; font-size: 13px">
                                Downtime alert will be automatically confirmed
                                once the tooling starts running again.
                              </div>
                            </template>
                            <span>
                              <img
                                style="width: 11px"
                                src="/images/icon/information.svg"
                                alt="error icon"
                              />
                            </span>
                          </a-tooltip>
                        </th>
                      </template>
                      <template
                        v-else-if="
                          ['toolingStatus', 'sensorStatus'].includes(
                            column.field
                          )
                        "
                      >
                        <th
                          @click="sortBy(column.sortField)"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            'text-left',
                          ]"
                        >
                          <span>{{ column.label }}</span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
                      <template
                        v-else-if="
                          ['notificationStatus'].includes(column.field)
                        "
                      >
                        <th
                          @click="sortBy(column.sortField)"
                          v-if="requestParam.status == 'alert'"
                          :class="[
                            {
                              __sort: column.sortable,
                              active: currentSortType === column.sortField,
                            },
                            isDesc ? 'desc' : 'asc',
                            'text-center',
                          ]"
                        >
                          <span>{{ column.label }}</span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
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
                            v-if="requestParam.status != 'DELETED'"
                            class="action-item"
                            @click="showSystemNoteModal(listChecked[0])"
                          >
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            @click="showCreateWorkorder(listChecked[0])"
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

                <tbody v-show="!isLoading" class="op-list">
                  <tr v-for="(result, index, id) in results" :id="result.id">
                    <td class="table-checkbox-head">
                      <template v-if="listChecked.length >= 0">
                        <div class="table-checkbox-head-content"></div>
                        <input
                          @click="check"
                          :checked="checkSelect(result.moldId)"
                          class="checkbox"
                          type="checkbox"
                          :value="result.moldId"
                        />
                      </template>
                    </td>
                    <template
                      v-for="(column, columnIndex) in allColumnList"
                      v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <td v-if="column.field === 'toolingId'" class="text-left">
                        <div>
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
                              showMoldDetails(result.mold, result)
                            "
                          >
                            {{ result.mold.equipmentCode }}
                          </a>
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'company'"
                      >
                        <div class="font-size-14">
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
                        <div class="small text-muted font-size-11-2">
                          {{ result.mold.locationCode }}
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'lastUptime'"
                      >
                        <div
                          v-if="requestParam.status == 'history'"
                          class="font-size-14"
                        >
                          <a
                            href="#"
                            @click.prevent="showDowntimeDetails(result.mold)"
                          >
                            {{
                              convertDate(
                                getConvertTime(result.mold.lastShotMadeAt)
                              )
                            }}
                          </a>
                        </div>
                        <div
                          v-if="requestParam.status !== 'history'"
                          class="font-size-14"
                        >
                          {{
                            convertDate(
                              getConvertTime(result.mold.lastShotMadeAt)
                            )
                          }}
                        </div>
                        <div class="font-size-11-2">
                          {{
                            convertTimeString(
                              getConvertTime(result.mold.lastShotMadeAt)
                            )
                          }}
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="column.field === 'downtimeDuration'"
                      >
                        <div class="font-size-14">
                          {{ getDowntimeDuration(result.downtimeSeconds) }}
                        </div>
                      </td>
                      <td
                        class="text-left"
                        v-else-if="
                          column.field === 'confirmationDate' &&
                          requestParam.status == 'history'
                        "
                      >
                        <div class="font-size-14">
                          {{ convertDate(getConvertTime(result.confirmedAt)) }}
                        </div>
                        <div class="font-size-11-2">
                          {{
                            convertTimeString(
                              getConvertTime(result.confirmedAt)
                            )
                          }}
                        </div>
                      </td>
                      <!--                      <td style="vertical-align: middle!important;" v-else-if="column.field === 'op'">-->
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
                        style="vertical-align: middle !important"
                        v-if="
                          requestParam.status !== 'history' &&
                          column.field === 'notificationStatus'
                        "
                      >
                        <div
                          style="background-color: #db3c22; font-size: 12px"
                          class="data-submission"
                        >
                          {{ "Downtime" }}
                        </div>
                      </td>
                    </template>
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
                <!-- change-alert-generation -->
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
    <downtime-history-details :resources="resources"></downtime-history-details>
  </div>
</template>

<script>
module.exports = {
  name: "mold-downtime",
  components: {
    "downtime-history-details": httpVueLoader(
      "/components/downtime-history-details.vue"
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
      userType: "",
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        lastAlert: true,
        query: "",
        status: "alert",

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
        APPROVED_AT: "approvedAt",
        CONFIRM_AT: "confirmedAt",
        OP: "mold.operatingStatus",
        VARIANCE: "variance",
        DATE: "notificationAt",
        STATUS: "notificationStatus",
      },
      currentSortType: "id",
      isDesc: true,
      listChecked: [],
      listCheckedTracked: {},
      isAllTracked: [],
      isLoading: false,
      alertOn: true,
      isInitForCm: false,
      isInitForPm: false,
      allColumnList: [],
      pageType: "DOWNTIME_ALERT",
      cancelToken: undefined,
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
    listCheckedTracked: {
      handler(newVal) {
        console.log("@watch:listCheckedTrack", newVal);
      },
      immediate: true,
    },
  },
  async created() {
    this.getDowntimeDuration = this.getDowntimeDuration.bind(this);
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
        },

        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "mold.location.company.name",
          defaultSelected: true,
          defaultPosition: 0,
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
          label: this.resources["last_uptime"],
          default: true,
          field: "lastUptime",
          sortable: true,
          sortField: "lastUptime",
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["downtime_duration"],
          default: true,
          field: "downtimeDuration",
          sortable: true,
          sortField: "downtimeSeconds",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["confirmation_date"],
          field: "confirmationDate",
          default: true,
          sortable: true,
          sortField: "confirmedAt",
          hiddenInToggle: true,
        },
        // { label: this.resources['op'], field: 'op', default: true, sortable: true, sortField: 'mold.operatingStatus', defaultSelected: true, defaultPosition: 4 },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "toolingStatus",
          defaultPosition: 6,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "sensorStatus",
          defaultPosition: 7,
        },
        // { label: this.resources['status'], field: 'notificationStatus', default: true, sortable: true, sortField: 'notificationStatus', defaultSelected: true, defaultPosition: 5 },
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
            this.allColumnList = this.allColumnList.filter(
              (item) => !item.isCustomField
            );
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
    handleResetColumnsListSelected() {
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
    handleChangeValueCheckBox(value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      console.log("allColumnList", this.allColumnList);
      this.$forceUpdate();
    },
    changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
      this.alertType = alertType;
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log("this.alertOn", this.alertOn);
      if (specialAlertType != null) {
        this.requestParam.specialAlertType = specialAlertType;
      }
      if (!isInit || specialAlertType != null) this.paging(1);
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

    check(e) {
      const { checked, value } = e.target;
      const moldId = Number(value);
      if (checked) {
        this.listChecked.push(moldId);
        const foundIndex = this.results.findIndex(
          (item) => item.moldId === moldId
        );
        this.listCheckedFull.push(this.results[foundIndex]);
        Common.setHeightActionBar();
      } else {
        this.listAllIds = [];
        this.listChecked = this.listChecked.filter((id) => id !== moldId);
        this.listCheckedFull = this.listCheckedFull.filter(
          (item) => item.moldId !== moldId
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

      // TODO: need recheck listCheckedTracked
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },
    convertDate(s) {
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

    // changeSelectedStatus(status) {
    //   let listChecked = [];
    //   Object.keys(this.listCheckedTracked).forEach(status => {
    //     if (status === this.requestParam.status) {
    //       Object.keys(this.listCheckedTracked[status]).forEach(page => {
    //         if (this.listCheckedTracked[status][page].length > 0) {
    //           listChecked = listChecked.concat(this.listCheckedTracked[status][page]);
    //         }
    //       });
    //     }
    //   });
    //   this.changeStatus(listChecked, status, true);
    // },

    tab(status) {
      this.total = null;
      console.log("status", status, "alert" == status, "history" == status);
      if (this.allColumnList) {
        this.allColumnList.forEach((c) => {
          switch (c.field) {
            case "confirmationDate":
              console.log("Field", c);
              if ("alert" == status) {
                c.hiddenInToggle = true;
              } else c.hiddenInToggle = false;
              break;
            case "notificationStatus":
              console.log("Field", c);
              if ("history" == status) {
                c.hiddenInToggle = true;
              } else c.hiddenInToggle = false;
              break;
          }
        });
      }
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      // this.requestParam.notificationStatus = '';
      this.requestParam.status = status;
      console.log("All Column List", this.allColumnList);
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
      var child = Common.vue.getChild(this.$parent.$children, "mold-details");
      if (child != null) {
        mold.reason = data.reason;
        mold.notificationStatus = data.notificationStatus;
        mold.approvedAt = data.approvedAt
          ? this.convertTimestampToDateWithFormat(
              data.approvedAt,
              "MMMM DD YYYY HH:mm:ss"
            )
          : "-";
        mold.approvedBy = data.approvedBy;
        const tab = "switch-detail";
        this.showChart(mold, tab);
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
    showDowntimeDetails(mold) {
      var child = Common.vue.getChild(
        this.$children,
        "downtime-history-details"
      );
      if (child != null) {
        child.showDowntimeDetails(
          mold,
          this.tabType,
          this.requestParam.status == "all"
        );
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
          moldIds: [data.id],
          action: status,
          message: "",
        };
      }

      var data = {
        title: "Message",
        messageTitle: "",
        buttonTitle: "Submit",
        placeholder: "Please provide reason for disapproval.",
        url: `/api/molds/data-submission/confirm`,
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
            url: `/api/molds/data-submission/confirm`,
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
            this.listChecked = [];
            this.listCheckedFull = [];
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
    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
    },

    log(result) {
      console.log("result", result);
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
      // checking is All
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
      self.isLoading = true;
      console.log("param: ", param);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();

      axios
        .get(`/api/molds/downtime?` + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          const { userType } = self;
          self.total = response.data.totalElements;
          self.results = response.data.content.map((value) => {
            value.confirmedAtShow = value.approvedAt
              ? this.formatToDateTime(value.approvedAt)
              : value.confirmedAt
              ? this.formatToDateTime(value.confirmedAt)
              : "";
            value.alertDateShow =
              userType == "OEM"
                ? value.notificationAt
                  ? this.formatToDateTime(value.notificationAt)
                  : ""
                : value.approvedAt
                ? this.formatToDateTime(value.approvedAt)
                : "";
            return value;
          });

          self.pagination = Common.getPagingData(response.data);

          if (self.total === self.listAllIds.length) {
            self.listChecked = [];
            self.listCheckedFull = [];
          }

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
        let sortFields = type;
        if (type == "mold.operatingStatus") {
          sortFields = "mold.equipmentStatus,mold.operatingStatus";
        }
        this.requestParam.sort = `${sortFields},${
          this.isDesc ? "desc" : "asc"
        }`;
        this.sort();
      }
    },
    sort() {
      this.paging(1);
    },
    showSystemNoteModal(selectedItem) {
      const child = this.$root.$refs["system-note"];
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
            this.listCheckedFull.map((i) => i.moldId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          child.showSystemNote({ id: this.listCheckedFull[0].moldId }, false);
        } else {
          console.log("single select in history tab");
          child.showSystemNote({ id: selectedItem.moldId });
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
            this.listCheckedFull.map((i) => i.moldId),
            true
          );
        } else if (this.listCheckedFull.length === 1) {
          console.log("single select");
          this.showNote({ id: this.listCheckedFull[0].moldId }, false);
        } else {
          console.log("single select in history tab");
          this.showNote({ id: selectedItem.moldId });
        }
      }
    },
    getDowntimeDuration(downtimeSecond) {
      let result = "";
      if (downtimeSecond < 86400) {
        let hours = (downtimeSecond - (downtimeSecond % 3600)) / 3600;
        let remainSecond = downtimeSecond % 3600;
        let minutes = (remainSecond - (remainSecond % 60)) / 60;
        result = `${hours} ${hours > 1 ? "hours" : "hour"} ${minutes} ${
          minutes > 1 ? "minutes" : "minute"
        }`;
      } else if (downtimeSecond < 604800) {
        let days = (downtimeSecond - (downtimeSecond % 86400)) / 86400;
        result = `${days} ${days > 1 ? "days" : "day"}`;
      } else if (downtimeSecond < 2592000) {
        let weeks = (downtimeSecond - (downtimeSecond % 604800)) / 604800;
        result = `${weeks} ${weeks > 1 ? "weeks" : "week"}`;
      } else if (downtimeSecond < 31536000) {
        let months = (downtimeSecond - (downtimeSecond % 2592000)) / 2592000;
        result = `${months} ${months > 1 ? "months" : "month"}`;
      } else {
        let years = (downtimeSecond - (downtimeSecond % 31536000)) / 31536000;
        result = `${years} ${years > 1 ? "years" : "year"}`;
      }
      return result;
    },
    getConvertTime(rawTime) {
      let result = "";
      if (rawTime) {
        result = this.convertTimestampToDateWithFormat(
          rawTime,
          "YYYY-MM-DD  HH:mm:ss"
        );
      }
      return result;
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.moldId);
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
        this.listChecked = this.results.map((i) => i.moldId);
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
    if (uri.fragment) {
      self.tab(uri.fragment);
    } else {
      self.paging(1);
    }
  },
};
</script>

<style scoped></style>
