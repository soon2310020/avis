<template>
  <div>
    <!-- <div class="row">
    <div class="col-lg-12">
      <div class="card wave">
          <input type="hidden" v-model="requestParam.sort" />
          <div class="form-row">
            <div class="col-md-12 col-12">

              <div class="form-row">
                <div class="col-md-12 mb-3 mb-md-0 col-12">
                  <common-searchbar
                    :request-param="requestParam"
                    :on-search="search"
                    :placeholder="resources['search_by_selected_column']"
                  ></common-searchbar>
                </div>
              </div>
            </div>
          </div>
        <div class="card-footer" style="display:none">
          <button class="btn btn-sm btn-primary" type="submit">
            <i class="fa fa-dot-circle-o"></i> <span v-text="resources['submit']"></span></button>
          <button class="btn btn-sm btn-danger" type="reset">
            <i class="fa fa-ban"></i> <span v-text="resources['reset']"></span></button>
        </div>
      </div>

    </div>
  </div> -->
    <div class="row">
      <div class="col-lg-12">
        <div class="card wave1">
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span v-text="resources['striped_tbl']"></span>
          </div>
          <div class="card-body card-overflow-reset">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.companyType === 'all' }"
                  href="#"
                  @click.prevent="tab('all')"
                  ><span v-text="resources['enabled']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.companyType === 'all'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.companyType === 'IN_HOUSE' }"
                  href="#"
                  @click.prevent="tab('IN_HOUSE')"
                  ><span v-text="resources['in_house']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.companyType === 'IN_HOUSE'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.companyType === 'SUPPLIER' }"
                  href="#"
                  @click.prevent="tab('SUPPLIER')"
                  ><span v-text="resources['supplier']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.companyType === 'SUPPLIER'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.companyType === '' }"
                  href="#"
                  @click.prevent="tabToDisable('disabled')"
                  ><span v-text="resources['disabled']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status === 'disabled'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <!--                            <show-columns style="position: absolute; right: 20px;" @reset-columns-list="handleResetColumnsListSelected" @change-value-checkbox="handleChangeValueCheckBox" :all-columns-list="allColumnList" v-if="Object.entries(resources).length" :resources="resources"></show-columns>-->
              <div class="nav-item" style="position: absolute; right: 62px">
                <!--                          <a @click="goToNew" id="goToNew" href="javascript:void(0)" class="btn-custom btn-custom-primary">-->
                <!--                            <span v-text="resources['create_checklist']"></span>-->
                <!--                          </a>-->
                <a
                  @click="openCreateListModal(false)"
                  id="open-create-list"
                  href="javascript:void(0)"
                  class="btn-custom btn-custom-primary"
                >
                  <span v-text="resources['create_list']"></span>
                </a>
              </div>
              <create-list-modal
                :is-show="createListModalVisible"
                @close="closeCreateListModal"
                :item-duplicate="itemDuplicate"
                create-type="reject-rate"
                @create-checklist-success="paging(1)"
                :is-edit="isEditCheckList"
                :selected-item="listCheckedFull[0]"
              ></create-list-modal>
              <customization-modal
                style="position: absolute; right: 20px"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              ></customization-modal>
            </ul>
            <table class="table table-responsive-sm table-striped">
              <thead id="thead-actionbar" class="custom-header-table">
                <tr
                  :class="{ invisible: listChecked.length != 0 }"
                  style="height: 57px"
                  class="tr-sort"
                >
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <input
                        class="checkbox"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </div>
                  </th>
                  <th
                    v-for="(item, index) in allColumnList"
                    v-if="item.enabled"
                    :key="index"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'Status' ? '' : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <span>{{ item.label }}</span>
                    <span v-if="item.sortable" class="__icon-sort"></span>
                  </th>
                  <!--                                    <th style="min-width: 130px"><span v-text="resources['action']"></span></th>-->
                </tr>

                <tr
                  :class="{ 'd-none zindexNegative': listChecked.length == 0 }"
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
                          padding-left: 0;
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
                          v-if="listChecked.length == 1"
                          @click="goToEdit"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1"
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1"
                          class="action-item"
                          @click="showSystemNoteModal(listChecked[0])"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length === 1"
                          class="action-item"
                          @click.prevent="duplicateChecklist(listCheckedFull)"
                        >
                          <span>Duplicate Checklist</span>
                          <div class="icon-action duplicate-icon"></div>
                        </div>
                        <div
                          v-if="requestParam.status !== 'disabled'"
                          class="action-item"
                          @click.prevent="disable(listCheckedFull)"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="requestParam.status === 'disabled'"
                          class="action-item"
                          @click.prevent="enable(listCheckedFull)"
                        >
                          <span>Enable</span>
                          <div class="icon-action enable-icon"></div>
                        </div>
                        <!--                                    <div v-if="requestParam.status != 'DELETED'" @click="showDeletePopup" class="action-item">-->
                        <!--                                        <span>Delete</span>-->
                        <!--                                        <svg xmlns="http://www.w3.org/2000/svg" width="6.949" height="8.611" viewBox="0 0 6.949 8.611">-->
                        <!--                                            <g id="Icon__delete" data-name="Icon_ delete" transform="translate(0.25 0.25)">-->
                        <!--                                                <path id="Icon_material-delete" data-name="Icon material-delete" d="M7.961,11.71a.914.914,0,0,0,.921.9h3.685a.914.914,0,0,0,.921-.9V6.3H7.961Zm5.988-6.759H12.337L11.876,4.5h-2.3l-.461.451H7.5v.9h6.449Z" transform="translate(-7.5 -4.5)" fill="none" stroke="#4b4b4b" stroke-width="0.5"/>-->
                        <!--                                            </g>-->
                        <!--                                        </svg>-->
                        <!--                                    </div>-->
                        <!--                                    <div v-if="requestParam.status == 'DELETED' && listChecked.length == 1" class="action-item">-->
                        <!--                                        <span @click.prevent="restoreItem()">Restore</span>-->
                        <!--                                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 68.137 58.403">-->
                        <!--                                            <path id="Icon_material-settings-backup-restore" data-name="Icon material-settings-backup-restore" d="M45.425,33.7a6.489,6.489,0,1,0-6.489,6.489A6.508,6.508,0,0,0,45.425,33.7ZM38.935,4.5a29.2,29.2,0,0,0-29.2,29.2H0L12.978,46.68,25.957,33.7H16.223A22.728,22.728,0,1,1,25.762,52.2l-4.607,4.672A29.2,29.2,0,1,0,38.935,4.5Z" transform="translate(0 -4.5)" fill="#4b4b4b"/>-->
                        <!--                                        </svg>-->
                        <!--                                    </div>-->
                      </div>
                    </div>
                  </th>
                </tr>
              </thead>

              <tbody class="op-list" style="display: none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                  <td class="custom-td">
                    <template v-if="listChecked.length >= 0">
                      <input
                        @click="(e) => check(e, result)"
                        :checked="checkSelect(result.id)"
                        class="checkbox"
                        type="checkbox"
                        :id="result.id + 'checkbox'"
                        :value="result.id"
                      />
                      <!--                                            <label class="custom-control-labelx" :for="result.id + 'checkbox'"></label>-->
                    </template>
                  </td>
                  <template
                    v-for="(column, columnIndex) in allColumnList"
                    v-if="column.enabled"
                  >
                    <td
                      v-if="column.field === 'checklistCode'"
                      class="text-left"
                    >
                      {{ result.checklistCode }}
                      <div class="small text-muted font-size-11-2">
                        <span v-text="resources['registered'] + ':'"></span>
                        {{ formatToDate(result.createdAt) }}
                      </div>
                    </td>
                    <td
                      v-else-if="column.field === 'assignedCompany'"
                      class="text-left"
                    >
                      <upper-company-dropdown
                        v-if="
                          result.assignedCompanies &&
                          result.assignedCompanies.length > 0
                        "
                        :data-list="result.assignedCompanies"
                        :index="index"
                        :show-company-details-by-id="showCompanyDetailsById"
                      >
                      </upper-company-dropdown>
                    </td>
                    <td
                      v-else-if="column.field === 'creator'"
                      class="text-left"
                    >
                      <base-avatar :item="result.creator" class="mr-2">
                      </base-avatar>
                    </td>
                    <td
                      v-else-if="column.field === 'individualUser'"
                      class="text-left"
                    >
                      <!--                  <user-list-cell :resources="resources" :user-list="result.assignedUsers"></user-list-cell>-->
                      <user-list-dropdown
                        v-if="
                          result.assignedUserDataList &&
                          result.assignedUserDataList.length > 0
                        "
                        :user-list="result.assignedUserDataList"
                        :index="index"
                      ></user-list-dropdown>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'enabled'"
                    >
                      <span
                        v-if="result.enabled"
                        class="label label-success"
                        v-text="resources['enabled']"
                      >
                      </span>
                      <span
                        v-else
                        class="label label-danger"
                        v-text="resources['disabled']"
                      >
                      </span>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'companyId'"
                    >
                      {{ result.company.companyCode }}
                    </td>
                    <td class="text-left" v-else>
                      {{ result[column.field] }}
                    </td>
                  </template>
                  <!--                                    <td>-->
                  <!--                                        <a :href="'/admin/checklist-center/reject-rate/' + result.id + '/edit'" class="table-action-link edit" title="Edit">-->
                  <!--                                            <img src="/images/icon/edit.svg" alt="edit" />-->
                  <!--                                        </a>-->

                  <!--                                        <div class="drowdown d-inline">-->
                  <!--                                            <a href="javascript:void(0)" title="Change status" class="table-action-link change-status"-->
                  <!--                                               data-toggle="dropdown" role="button"-->
                  <!--                                               @click="changeShow(result.id)"-->
                  <!--                                               aria-haspopup="true" aria-expanded="false">-->
                  <!--                                                <img src="/images/icon/change-status.svg" alt="Change status" />-->
                  <!--                                            </a>-->
                  <!--                                            <ul class="dropdown-menu" :class="[(parseInt(result.id) === parseInt(showDrowdown)) ? `show` : ``]">-->
                  <!--                                                <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li>-->
                  <!--                                                <li v-if="!result.enabled">-->
                  <!--                                                    <div class="dropdown-item" @click.prevent="enable(result)">-->
                  <!--                                                        <span v-text="resources['enable']"></span>-->
                  <!--                                                    </div>-->
                  <!--                                                </li>-->
                  <!--                                                <li v-if="result.enabled">-->
                  <!--                                                    <div class='dropdown-item' @click.prevent="disable(result)">-->
                  <!--                                                        <span v-text="resources['disable']"></span>-->
                  <!--                                                    </div>-->
                  <!--                                                </li>-->
                  <!--                                                &lt;!&ndash;<li>&ndash;&gt;-->
                  <!--                                                    &lt;!&ndash;<div class="dropdown-item" @click.prevent="deleteChecklist(result)"><span v-text="resources['delete']"></span></div>&ndash;&gt;-->
                  <!--                                                &lt;!&ndash;</li>&ndash;&gt;-->
                  <!--                                            </ul>-->
                  <!--                                        </div>-->
                  <!--                                        <a href="javascript:void(0)"-->
                  <!--                                           @click.prevent = "showRevisionHistory(result.id)"-->
                  <!--                                           title="View edit history"-->
                  <!--                                           class="table-action-link revision-history">-->
                  <!--                                            <img src="/images/icon/revision-history.svg" alt="View edit history" />-->
                  <!--                                        </a>-->
                  <!--                                        <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note">-->
                  <!--                                            <img src="/images/icon/note.svg" alt="Add note" />-->
                  <!--                                        </a>-->
                  <!--                                    </td>-->
                </tr>
              </tbody>
            </table>

            <div
              class="no-results d-none"
              v-text="resources['no_results']"
            ></div>
            <div class="row">
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
              <!--            <div class="col-auto ml-auto">
              <a href="/admin/checklist-center/reject-rate/new" class="btn btn-primary"><i class="fa fa-plus"></i>
                <span v-text="resources['create_checklist']"></span>
              </a>
            </div>-->
            </div>
          </div>
        </div>
      </div>
    </div>
    <company-details :resources="resources"></company-details>
  </div>
</template>

<script>
module.exports = {
  components: {
    "create-list-modal": httpVueLoader(
      "/components/checklist-center/create-list-modal.vue"
    ),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "upper-company-dropdown": httpVueLoader(
      "/components/upper-company-dropdown.vue"
    ),
    "user-list-dropdown": httpVueLoader("/components/user-list-dropdown.vue"),
  },
  props: {
    resources: Object,
    searchText: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      showDrowdown: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        companyType: "all",
        enabled: true,
        sort: "id,desc",
        page: 1,
        id: "",
        checklistType: "REJECT_RATE",
      },
      currentSortType: "id",
      isDesc: true,
      pageType: "CHECKLIST_REJECT_RATE",
      allColumnList: [],
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      listCheckedFull: [],
      cancelToken: undefined,
      createListModalVisible: false,
      itemDuplicate: null,
      isEditCheckList: false,
    };
  },
  watch: {
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
      if (newVal && newVal.length != 0) {
        const self = this;
        const x = this.results.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
    searchText(newVal) {
      this.requestParam.query = newVal;
      this.paging(1);
    },
  },
  created() {},
  mounted() {
    this.callSetAllColumnList();
    this.requestParam.query = this.searchText;
    this.paging(1);
    this.$nextTick(function () {
      Common.removeWave(500);
    });
  },
  methods: {
    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    duplicateChecklist(itemDuplicate) {
      this.isEditCheckList = false;
      this.itemDuplicate = itemDuplicate[0];
      this.createListModalVisible = true;
    },
    closeCreateListModal() {
      this.createListModalVisible = false;
    },
    openCreateListModal(isEdit = false) {
      const self = this;
      this.itemDuplicate = null;
      this.isEditCheckList = isEdit;
      const el = document.getElementById("open-create-list");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        self.createListModalVisible = true;
      }, 700);
    },
    goToNew() {
      const el = document.getElementById("goToNew");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        window.location.href = "/admin/checklist-center/reject-rate/new";
      }, 700);
    },
    closeShowDropDown() {
      this.showDropdown = false;
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
    hasChecked: function () {
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
    check: function (e, result) {
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
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      this.openCreateListModal(true);
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
          label: this.resources["checklist_id"],
          field: "checklistCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["assigned_company"],
          field: "assignedCompany",
          default: true,
          sortable: true,
          sortField: "assignedCompany",
        },
        {
          label: this.resources["individual_user"],
          field: "individualUser",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["created_by"],
          field: "creator",
          default: true,
          sortable: true,
          sortField: "creator",
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["status"],
          field: "enabled",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
      ];
      this.resetColumnsListSelected();
      this.getColumnListSelected();
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    handleResetColumnsListSelected: function () {
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
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
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
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    changeStatus: function () {
      this.paging(1);
    },

    tab: function (companyType) {
      this.total = null;
      this.listChecked = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.enabled = true;
      this.requestParam.status = companyType;
      this.requestParam.companyType = companyType;
      this.paging(1);
    },
    tabToDisable: function (status) {
      this.listChecked = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.requestParam.enabled = false;
      this.requestParam.companyType = "";
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },

    deleteChecklist: function (checklist) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: checklist.id,
      };
      let self = this;
      axios
        .delete("/api/checklist" + "/" + checklist.id, param)
        .then(function (response) {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    enable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = true;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = true;
        });
        this.saveBatch(user, true);
      }
    },
    disable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = false;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = false;
        });
        this.saveBatch(user, false);
      }
    },
    saveBatch(user, boolean) {
      console.log(user, "user");
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      let self = this;
      axios
        .post("/api/checklist/change-status-in-batch", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          self.listChecked = [];
          self.isAll = false;
          self.showDropdown = false;
        });
    },

    save: function (checklist) {
      //var param = Common.param(item);
      var param = {
        id: checklist.id,
        enabled: checklist.enabled,
      };
      let self = this;
      axios
        .put("/api/checklist" + "/" + checklist.id + "/enable", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          self.listChecked = [];
          self.isAll = false;
          self.showDropdown = false;
        });
    },
    paging: function (pageNumber) {
      var self = this;
      if (self.isEditCheckList) {
        self.listCheckedFull = [];
        self.listChecked = [];
      }
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let requestParam = JSON.parse(JSON.stringify(self.requestParam));
      if (requestParam.companyType === "all") {
        delete requestParam.companyType;
      }
      // checking is All
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked
        .filter((item) => item.status === this.requestParam.status)
        .map((item) => item.page);
      // if (pageSelectedAll.includes(this.requestParam.page)) {
      //     this.isAll = true;
      // }
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

      var param = Common.param(requestParam);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/checklist" + "?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
          Common.handleNoResults("#app", self.results.length);

          var pageInfo =
            self.requestParam.page === 1
              ? ""
              : "?page=" + self.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    sortBy: function (type) {
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
    sort: function () {
      this.paging(this.requestParam.page);
    },
    showRevisionHistory: function (id) {
      var child = Common.vue.getChild(
        this.$parent.$children,
        "revision-history"
      );
      if (child != null) {
        child.showRevisionHistories(id, this.pageType);
      }
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$parent.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
  },
};
</script>
<style scoped></style>
