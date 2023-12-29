<template>
  <div>
    <notify-alert
      type="MISCONFIGURE"
      @change-alert-generation="changeAlertGeneration"
      :resources="resources"
    ></notify-alert>
    <div v-show="enabledSearchBar" class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 47px"></div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['reset']"></strong>

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
            <i class="fa fa-align-justify"></i>
            <span v-text="resources['striped_tbl']"></span>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'misconfigured' }"
                  href="#"
                  @click.prevent="tab('misconfigured')"
                  ><span v-text="resources['alert']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'misconfigured'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'confirmed' }"
                  href="#"
                  @click.prevent="tab('confirmed')"
                  ><span v-text="resources['history_log']"></span>
                  <span
                    v-show="alertOn"
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'confirmed'"
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
                      <template v-if="column.field === sortType.CONFIRM_DATE">
                        <th
                          v-if="requestParam.status === 'confirmed'"
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
                          <span v-text="column.label"></span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
                      <template v-else>
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
                          <span v-text="column.label"></span>
                          <span class="__icon-sort"></span>
                        </th>
                      </template>
                    </template>
                    <th
                      class="text-center"
                      v-if="requestParam.status === 'confirmed'"
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
                                @click="openDropDown"
                              >
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul
                                class="dropdown-menu"
                                :class="[dropDownShow ? 'show' : '']"
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
                            v-if="requestParam.status != 'DELETED'"
                            class="action-item"
                            @click="showSystemNoteModal()"
                          >
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                            v-if="listChecked.length == 1"
                            class="action-item"
                            @click="showCreateWorkorder()"
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
                    <td v-if="requestParam.status !== 'confirmed'">
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
                      <td v-if="column.field === sortType.ID" class="text-left">
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
                        v-if="column.field === sortType.COUNTER"
                        class="text-left"
                      >
                        {{ result.counterCode }}
                      </td>
                      <td
                        v-if="column.field === sortType.COMPANY"
                        class="text-left"
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
                        v-if="column.field === sortType.LOCATION"
                        class="text-left"
                      >
                        <div v-if="result.mold.locationCode">
                          <div class="font-size-14">
                            <a
                              href="#"
                              @click.prevent="showLocationHistory(result.mold)"
                              >{{ result.mold.locationName }}</a
                            >
                          </div>
                          <span class="small text-muted font-size-11-2">{{
                            result.mold.locationCode
                          }}</span>
                        </div>
                      </td>
                      <td
                        v-if="column.field === sortType.LAST_SHOT"
                        class="text-left"
                      >
                        {{
                          formatNumber(
                            result.mold.accumulatedShot
                              ? result.mold.accumulatedShot
                              : "0"
                          )
                        }}
                        <div>
                          <span class="font-size-11-2">
                            {{
                              formatNumber(
                                result.mold.accumulatedShot ? "shots" : ""
                              )
                            }}
                          </span>
                        </div>
                      </td>
                      <td
                        v-if="column.field === sortType.RESET"
                        class="text-left"
                      >
                        <a href="#" @click.prevent="showResetDetail(result)">
                          {{ formatNumber(result.preset) }}
                        </a>
                      </td>

                      <td
                        v-if="column.field === sortType.DATE"
                        class="text-left"
                      >
                        <span class="font-size-14">
                          {{ result.createdDate }}
                        </span>
                        <div>
                          <span class="font-size-11-2">
                            {{ convertTime(result.createdAt) }}
                          </span>
                        </div>
                      </td>

                      <td
                        v-if="
                          column.field === sortType.CONFIRM_DATE &&
                          requestParam.status === 'confirmed'
                        "
                        class="text-left"
                      >
                        <span class="font-size-14">
                          {{ convertDate(result.confirmedAtShow) }}
                        </span>
                        <div>
                          <span class="font-size-11-2">
                            {{ convertTimeString(result.confirmedAtShow) }}
                          </span>
                        </div>
                      </td>
                      <td
                        v-if="column.field === sortType.TOOLING_STATUS"
                        class="text-left tooling-status"
                      >
                        <mold-status
                          :mold="result.mold"
                          :resources="resources"
                          :rule-op-status="ruleOpStatus"
                        />
                      </td>
                      <td
                        v-if="column.field === sortType.COUNTER_STATUS"
                        class="text-left"
                      >
                        <counter-status
                          :mold="result.mold"
                          :resources="resources"
                        />
                      </td>
                    </template>

                    <td
                      v-if="requestParam.status === 'confirmed'"
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                    >
                      <a
                        href="#"
                        @click.prevent="showResetConfirmHistory(result)"
                        title="View"
                        v-if="result.misconfigureStatus === 'CONFIRMED'"
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
    <reset-confirm-history :resources="resources"></reset-confirm-history>
    <reset-detail :resources="resources"></reset-detail>
  </div>
</template>

<script>
module.exports = {
  name: "mold-misconfigured",
  components: {
    "reset-confirm-history": httpVueLoader(
      "/components/reset-confirm-history.vue"
    ),
    "reset-detail": httpVueLoader("/components/reset-detail.vue"),
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
        status: "misconfigured",
        //'extraStatus': 'op-status-is-not-null',
        sort: "id,desc",
        page: 1,

        operatingStatus: "",
        notificationStatus: "",
      },

      statusCodes: [],
      listCategories: [],
      sortType: {
        ID: "mold.equipmentCode",
        COUNTER: "counterCode",
        COMPANY: "mold.location.company.name",
        LOCATION: "mold.location.name",
        LAST_SHOT: "mold.lastShot",
        RESET: "preset",
        CYCLE_TIME: "cycleTime",
        OP: "mold.operatingStatus",
        DATE: "notificationAt",
        STATUS: "misconfigureStatus",
        CONFIRM_DATE: "confirmedAt",
        TOOLING_STATUS: "toolingStatus",
        COUNTER_STATUS: "counterStatus",
      },
      currentSortType: "id",
      isDesc: true,
      isLoading: false,
      alertOn: true,
      listChecked: [],
      listCheckedTracked: {},
      isAllTracked: [],
      listCheckedFull: [],
      dropDownShow: false,
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      allColumnList: [],
      cancelToken: undefined,
      listAllIds: [],
      pageType: "MISCONFIGURED",
    };
  },
  computed: {
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
  },
  methods: {
    callSetAllColumnList() {
      if (
        this.resources &&
        Object.entries(this.resources).length > 0 &&
        this.allColumnList.length === 0
      ) {
        this.setAllColumnList();
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
          field: this.sortType.ID,
          mandatory: true,
          default: true,
          sortable: true,
          sortField: this.sortType.ID,
        },
        {
          label: this.resources["counter_id"],
          field: this.sortType.COUNTER,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.COUNTER,
        },
        {
          label: this.resources["company"],
          field: this.sortType.COMPANY,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.COMPANY,
        },
        {
          label: this.resources["location"],
          field: this.sortType.LOCATION,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.LOCATION,
        },
        {
          label: this.resources["accumulated_shots"],
          field: this.sortType.LAST_SHOT,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.LAST_SHOT,
        },
        {
          label: this.resources["reset_value"],
          field: this.sortType.RESET,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.RESET,
        },
        {
          label: this.resources["alert_date"],
          field: this.sortType.DATE,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.DATE,
        },
        // { label: this.resources['completion_date'], field: this.sortType.CONFIRM_DATE, default: true, sortable: true, sortField: this.sortType.CONFIRM_DATE },
        {
          label: this.resources["tooling_status"],
          field: this.sortType.TOOLING_STATUS,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.TOOLING_STATUS,
        },
        {
          label: this.resources["sensor_status"],
          field: this.sortType.COUNTER_STATUS,
          defaultSelected: true,
          default: true,
          sortable: true,
          sortField: this.sortType.COUNTER_STATUS,
        },
      ];
      try {
        this.resetColumnsListSelected();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
      }
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    updateVariable(data) {
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
    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "company-details"
      );
      if (child != null) {
        child.showDetailsById(company);
      }
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
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.listChecked = [];
      this.listCheckedFull = [];
      if (status !== "confirmed") {
        this.allColumnList = this.allColumnList.filter(
          (column) => column.field !== this.sortType.CONFIRM_DATE
        );
      } else {
        let indexColumn = this.allColumnList.findIndex(
          (column) => column.field === this.sortType.CONFIRM_DATE
        );
        if (indexColumn === -1) {
          this.allColumnList = [
            ...this.allColumnList,
            {
              label: this.resources["completion_date"],
              field: this.sortType.CONFIRM_DATE,
              default: true,
              sortable: true,
              sortField: this.sortType.CONFIRM_DATE,
            },
          ];
        }
      }
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    disable(user) {
      user.enabled = false;
      //this.save(user);
    },
    openDropDown() {
      this.dropDownShow = !this.dropDownShow;
    },

    closeShowDropDown() {
      this.dropDownShow = false;
    },

    showChart(mold, tab) {
      console.log(mold);
      var child = Common.vue.getChild(this.$parent.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "UPTIME", "DAY", tab ? tab : "switch-graph");
      }
    },
    showMoldDetails(mold) {
      const tab = "switch-detail";
      this.showChart(mold, tab);
    },
    //reset-confirm-history
    showResetConfirmHistory(mold) {
      var child = Common.vue.getChild(this.$children, "reset-confirm-history");
      if (child != null) {
        child.show(mold);
      }
    },

    //reset-detail
    showResetDetail(item) {
      var child = Common.vue.getChild(this.$children, "reset-detail");
      if (child != null) {
        child.show(item);
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
          alertDate: self.formatToDateTime(data.notificationAt),
          confirmDate: self.formatToDateTime(data.confirmedAt),
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

        url: "/api/molds/misconfigure/" + param.id + "/confirm",
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

    paging(pageNumber) {
      var self = this;
      Common.handleNoResults("#app", 1);
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(self.requestParam);
      self.isLoading = true;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/molds/misconfigure?lastAlert=true&" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          self.total = response.data.totalElements;
          self.results = response.data.content.map((value) => {
            // value.confirmedAtShow = value.confirmedAt ? moment.unix(value.confirmedAt).format('YYYY-MM-DD HH:mm:ss'): '';
            value.confirmedAtShow = self.formatToDateTime(value.confirmedAt);
            return value;
          });
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

          // var pageInfo =
          //   self.requestParam.page == 1
          //     ? ""
          //     : "?page=" + self.requestParam.page;
          // history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
          self.isLoading = false;
        });
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
    sort() {
      this.paging(1);
    },
    showSystemNoteModal(selectedItem) {
      const config = {
        objectType: null,
        systemNoteFunction: "RESET_ALERT",
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
          this.listCheckedFull.map((i) => i.mold.id),
          true,
          config.objectType,
          config.systemNoteFunction
        );
      } else if (this.listCheckedFull.length === 1) {
        console.log("single select");
        this.showNote(
          { id: this.listCheckedFull[0].mold.id },
          false,
          config.objectType,
          config.systemNoteFunction
        );
      } else {
        console.log("single select in history tab");
        this.showNote(
          { id: selectedItem.mold.id },
          false,
          config.objectType,
          config.systemNoteFunction
        );
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
          pageType: "RESET_ALERT",
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
    this.callSetAllColumnList();
    if (this.searchText) {
      this.requestParam.query = this.searchText;
    }
    var self = this;
    var uri = URI.parse(location.href);
    if (uri.fragment == "outside_l1") {
      self.tab("L1");
    } else if (uri.fragment == "outside_l2") {
      self.tab("L2");
    } else {
      self.paging(1);
    }
  },
};
</script>

<style scoped></style>
