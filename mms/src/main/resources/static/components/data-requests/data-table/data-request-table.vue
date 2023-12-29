<template>
  <div style="margin-top: 24px">
    <div class="row">
      <div class="col-lg-12">
        <div class="">
          <div style="overflow-x: auto !important" class="card-body">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === 'All' }"
                  href="#"
                  @click.prevent="tab('All')"
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
                  :class="{ active: activeTab === 'DATA_REGISTRATION' }"
                  href="#"
                  @click.prevent="tab('DATA_REGISTRATION')"
                >
                  <span>{{ resources["data_registration"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.dataRegistration || 0 }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === 'DATA_COMPLETION' }"
                  href="#"
                  @click.prevent="tab('DATA_COMPLETION')"
                >
                  <span>{{ resources["data_completion"] }}</span>
                  <span class="badge badge-light badge-pill">
                    {{ resultsStat.dataCompletion }}
                  </span>
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: activeTab === 'History' }"
                  href="#"
                  @click.prevent="tab('History')"
                >
                  <span v-text="resources['history_log']"></span>
                </a>
              </li>
              <div
                class="d-flex"
                style="position: absolute; right: 20px; align-items: center"
              >
                <base-button
                  level="secondary"
                  type="normal"
                  @click="$emit('show-my-requests')"
                  >{{ resources["my_requests"] }}</base-button
                >
                <div>
                  <cta-button
                    :active="showCreateOption"
                    color-type="blue-fill"
                    style="margin-left: 10px"
                    :click-handler="handleToggleCreateOption"
                  >
                    <span>{{ resources["create_data_request"] }}</span>
                  </cta-button>
                  <common-select-popover
                    :is-visible="showCreateOption"
                    @close="closeCreateOption"
                    class="common-popover-for-tooling"
                  >
                    <common-select-dropdown
                      :style="{ position: 'static' }"
                      :class="{ show: showCreateOption }"
                      :searchbox="false"
                      :checkbox="false"
                      :items="createOptions"
                      :click-handler="handleSelectCreateOption"
                      class="dropdown-wrap-for-tooling"
                    >
                    </common-select-dropdown>
                  </common-select-popover>
                </div>
                <customization-modal
                  style="margin-left: 8px"
                  :all-columns-list="allColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                ></customization-modal>
              </div>
            </ul>
            <div>
              <table
                style="
                  overflow-x: auto !important;
                  border: 1px solid rgb(201, 206, 210);
                  border-top: 0px;
                "
                class="table table-responsive-sm table-striped"
              >
                <thead id="thead-actionbar" class="custom-header-table">
                  <tr
                    :class="{ invisible: listChecked.length != 0 }"
                    style="height: 57px"
                    class="tr-sort"
                  >
                    <th style="width: 40px; vertical-align: middle !important">
                      <input
                        style="vertical-align: middle !important"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </th>
                    <template v-for="(column, index) in allColumnListDisplay">
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
                            v-show="
                              !shouldShowCompleteDataRequest &&
                              !shouldShowAcceptDataRequest &&
                              !shouldShowReopenDataRequest
                            "
                          >
                            {{ resources["no_action_is_available"] }}
                          </div>
                          <div
                            class="action-item"
                            style="align-items: flex-start"
                            v-show="shouldShowAcceptDataRequest"
                            @click="
                              $emit(
                                'show-request-detail-modal',
                                listCheckedFull[0]
                              )
                            "
                          >
                            <span>{{ resources["accept_data_request"] }}</span>
                            <img
                              src="/images/icon/work-order/handshake-icon.svg"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            class="action-item"
                            v-show="shouldShowCompleteDataRequest"
                            @click="
                              $emit(
                                'show-request-list-modal',
                                listCheckedFull[0]
                              )
                            "
                            style="align-items: flex-start; margin-left: 12px"
                          >
                            <span>{{
                              resources["complete_data_request"]
                            }}</span>
                            <img
                              src="/images/icon/complete-data-request.svg"
                              alt="re-open"
                              style="
                                margin-left: 4px;
                                height: 15px;
                                width: 15px;
                              "
                            />
                          </div>
                          <div
                            class="action-item"
                            v-show="shouldShowReopenDataRequest"
                            style="align-items: flex-start; margin-left: 12px"
                            @click="$emit('reopen', listCheckedFull[0])"
                          >
                            <span>{{ resources["reopen_data_request"] }}</span>
                            <img
                              src="/images/icon/work-order/rework-icon.svg"
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
                  <tr
                    v-show="!isLoading"
                    v-for="result in results"
                    :key="result.id"
                    :id="result.id"
                  >
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
                      v-for="(column, columnIndex) in allColumnListDisplay"
                    >
                      <td
                        v-if="['createdByUser'].includes(column.field)"
                        class="text-left"
                      >
                        <base-avatar :item="result.createdByUser"></base-avatar>
                      </td>
                      <td
                        v-else-if="
                          ['dataRequestUserList'].includes(column.field)
                        "
                        class="text-left"
                      >
                        <base-avatar
                          v-for="user in (result.dataRequestUserList || []).map(
                            (user) => user.user
                          )"
                          :item="user"
                          style="margin-right: 8px"
                        ></base-avatar>
                      </td>
                      <td v-else-if="column.field === 'status'">
                        <base-priority-card
                          :color="
                            priorityStatusMatchColor[result[column.field]]
                          "
                        >
                          <span>
                            {{ getStatusText(result[column.field]) }}
                          </span>
                        </base-priority-card>
                      </td>
                      <td
                        v-else-if="column.field === 'dueDate'"
                        class="text-left"
                      >
                        <span>{{ formatToDate(result[column.field]) }}</span>
                      </td>
                      <td
                        v-else-if="column.field === 'requestDataType'"
                        class="text-left"
                      >
                        <span>{{ getDataTypeText(result[column.field]) }}</span>
                      </td>
                      <td
                        v-else-if="column.field === 'requestId'"
                        class="text-left custom-hyper-link"
                        @click="() => handleClickId(result)"
                      >
                        <span>{{ result[column.field] }}</span>
                      </td>
                      <td
                        v-else-if="column.field === 'progress'"
                        class="text-left"
                      >
                        <progress-bar
                          :progress-percentage="result[column.field] || 0"
                          :title="(result[column.field] || 0) + '% completed'"
                          style="max-width: 97px"
                          color="pink"
                          :title-style="{ fontSize: '11.25px' }"
                        ></progress-bar>
                      </td>
                      <td v-else :key="columnIndex" class="text-left">
                        {{ result[column.field] }}
                      </td>
                    </template>
                  </tr>
                </tbody>
              </table>
              <div
                v-show="results?.length === 0"
                class="no-results"
                v-text="resources['no_results']"
              ></div>
              <div v-show="!isLoading" class="row">
                <div class="col-md-10 col-sm-10 col-xs-10">
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
module.exports = {
  name: "data-request-table",
  components: {
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "progress-bar": httpVueLoader(
      "/components/@base/progress-bar/progress-bar.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    renderTableKey: {
      type: Number,
      default: () => 0,
    },
  },
  data() {
    return {
      isLoading: false,
      activeTab: "All",
      pagination: [],
      results: [],
      resultsStat: {
        total: 0,
        dataCompletion: 0,
        dataRegistration: 0,
      },
      allColumnList: [],
      requestParam: {
        size: 15,
      },
      listChecked: [],
      listCheckedFull: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      currentSortType: "id",
      isDesc: true,
      showCreateOption: false,
      priorityStatusMatchColor: {
        OVERDUE: "red",
        IN_PROGRESS: "yellow",
        REQUESTED: "purple",
        COMPLETED: "green",
        DECLINED: "grey",
        CANCELLED: "dark-grey",
      },
      currentUser: {},
    };
  },
  async created() {
    this.createOptions = [
      { title: this.resources["data_registration"], key: "DATA_REGISTRATION" },
      { title: this.resources["data_completion"], key: "DATA_COMPLETION" },
    ];
    const me = await Common.getSystem("me");
    this.currentUser = JSON.parse(me);
  },
  mounted() {
    this.callSetAllColumnList();
    this.paging(1);
  },
  watch: {
    listChecked(newVal) {
      console.log("listChecked change", newVal);
      if (newVal && newVal.length != 0) {
        const self = this;

        const x = this.results.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
    renderTableKey(newVal) {
      this.paging(this.requestParam.page);
    },
  },
  computed: {
    allColumnListDisplay() {
      return this.allColumnList.filter(
        (column) => column.enabled && !column.hiddenInToggle
      );
    },
    shouldShowCompleteDataRequest() {
      const firstChecked = _.head(this.listCheckedFull);
      return (
        this.listChecked?.length === 1 &&
        ["IN_PROGRESS", "OVERDUE"].includes(firstChecked?.status) &&
        !this.isCreator(firstChecked)
      );
    },
    shouldShowAcceptDataRequest() {
      const firstChecked = _.head(this.listCheckedFull);
      return (
        this.listChecked?.length === 1 &&
        ["REQUESTED"].includes(firstChecked?.status) &&
        !this.isCreator(firstChecked)
      );
    },
    shouldShowReopenDataRequest() {
      const firstChecked = _.head(this.listCheckedFull);
      return (
        this.listChecked?.length === 1 &&
        ["CANCELLED"].includes(firstChecked?.status)
      );
    },
  },
  methods: {
    handleClickId(item) {
      const isCreator = this.isCreator(item);
      if (!isCreator && ["IN_PROGRESS", "OVERDUE"].includes(item?.status)) {
        this.$emit("show-request-list-modal", item);
        return;
      }
      this.$emit("show-request-detail-modal", item);
    },
    isCreator(dataItem) {
      return dataItem.createdByUser.id === this.currentUser.id;
    },
    getStatusText(status) {
      //change name
      // if(status === 'IN_PROGRESS') return this.resources['in_progress']
      if (status === "IN_PROGRESS") return this.resources["upcoming"];
      else return _.upperFirst(_.toLower(status));
    },
    handleSelectCreateOption(type) {
      this.$emit("show-create-request", type);
      this.showCreateOption = false;
    },
    handleToggleCreateOption() {
      this.showCreateOption = !this.showCreateOption;
    },
    closeCreateOption() {
      this.showCreateOption = false;
    },
    tab(status) {
      if (this.activeTab != status) {
        this.activeTab = status;
        this.requestParam.type = status;
        this.isAll = false;
        this.listChecked = [];
        this.paging(1);
      }
    },
    sortBy(sortField) {
      this.currentSortType = sortField;
      this.isDesc = !this.isDesc;
      this.paging(this.requestParam?.page);
    },
    paging(pageNumber) {
      let self = this;
      // Common.handleNoResults("#app", 1);
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      if (!this.listCheckedTracked[self.requestParam.status]) {
        self.listCheckedTracked[self.requestParam.status] = {};
      }
      if (
        !this.listCheckedTracked[self.requestParam.status][
          self.requestParam.page
        ]
      ) {
        self.listCheckedTracked[self.requestParam.status][
          self.requestParam.page
        ] = [];
      }

      const param = Common.param({
        ...this.requestParam,
        sort: `${this.currentSortType},${this.isDesc ? "desc" : "asc"}`,
        page: pageNumber,
      });

      let url = "/api/data-request?" + param;
      self.isLoading = true;
      axios
        .get(url)
        .then(function (response) {
          self.isLoading = false;
          self.listChecked = [];
          self.listCheckedFull = [];
          self.isAll = false;
          self.results = response.data.data.content;
          self.pagination = Common.getPagingData(response.data.data);
          self.resultsStat = {
            total: response.data.allCount,
            dataCompletion: response.data.dataCompletionCount,
            dataRegistration: response.data.dataRegistrationCount,
          };
          console.log("results", this.results, this.resultsStat);
          // Common.handleNoResults("#app", this.results.length);
        })
        .catch(function (error) {
          console.log(error.response);
          self.isLoading = false;
        });
      self.$forceUpdate();
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: "Request ID",
          labelKey: "request_id",
          field: "requestId",
          default: true,
          sortable: true,
          sortField: "id",
        },
        {
          label: "Type",
          labelKey: "type",
          field: "requestDataType",
          default: true,
          sortable: true,
          sortField: "requestDataType",
        },
        {
          label: "Created by",
          labelKey: "created_by",
          field: "createdByUser",
          default: true,
          sortable: true,
          sortField: "createdByUser",
        },
        {
          label: "Assigned to",
          labelKey: "assigned_to",
          field: "dataRequestUserList",
          default: true,
          sortable: true,
          sortField: "dataRequestUserList",
        },
        {
          label: "Due Date",
          labelKey: "due_date",
          field: "dueDate",
          default: true,
          sortable: true,
          sortField: "dueDate",
        },
        // { label: "Progress", labelKey: 'progress', field: "progress", default: true, sortable: true, sortField: "progress" },
        {
          label: "Status",
          labelKey: "status",
          field: "status",
          default: true,
          sortable: true,
          sortField: "status",
        },
      ];
      try {
        this.resetColumnsListSelected();
      } catch (e) {
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    getColumnListSelected() {
      axios
        .get(`/api/config/column-config?pageType=DATA_REQUEST`)
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
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    saveSelectedList: function (dataCustomize, list) {
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
          pageType: "DATA_REQUEST",
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
    getDataTypeText(dataType) {
      switch (dataType) {
        case "DATA_COMPLETION":
          return this.resources["data_completion"];
        case "DATA_REGISTRATION":
          return this.resources["data_registration"];
      }
    },
    select: function (event) {
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
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    check: function (e) {
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
    callSetAllColumnList() {
      if (
        this.resources &&
        Object.entries(this.resources).length > 0 &&
        this.allColumnList.length == 0
      ) {
        this.setAllColumnList();
      }
    },
  },
};
</script>

<style scoped>
.list-hover-dropdown:hover {
  background: #e6f7ff;
}
.common-popover-for-tooling {
  min-width: 180px !important;
  width: 180px !important;
  border-radius: 3px !important;
  padding: 2px !important;
}

.dropdown-wrap-for-tooling {
  width: 180px !important;
  border-radius: 3px !important;
  padding: 2px !important;
}
.dropdown-list li label {
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  font-size: 14px;
  line-height: 18px;
  height: 32px;
  padding: 0px 8px;
}
/*.dropdown-list li label:hover {*/
/*  background: #e6f7ff;*/
/*}*/
.dropdown-list li label input {
  display: none;
}
.dropdown-list li label input:checked + .checkbox-custom {
  border-color: #0075ff;
}
.dropdown-list li label input:checked + .checkbox-custom::before {
  content: "";
  width: 9px;
  height: 9px;
  background-color: #0075ff;
}
.dropdown-list li label:hover > .checkbox-custom {
  border: 1px solid #3585e5 !important;
}
.dropdown-list li label .checkbox-custom {
  width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown-list li label[disabled] {
  color: #c4c4c4;
  pointer-events: none;
}

label {
  margin-bottom: 2px;
}
.dropdown-list li label[disabled] input:checked + .checkbox-custom {
  border-color: #c4c4c4;
}
.dropdown-list li label[disabled] input:checked + .checkbox-custom::before {
  /* content: '';
  width: 9px;
  height: 9px; */
  background-color: #c4c4c4;
}
</style>
