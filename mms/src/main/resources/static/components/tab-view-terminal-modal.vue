<template>
  <div
    id="create-tab-view"
    style="overflow-y: scroll"
    class="modal fade"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">
              {{
                tabViewModalType === "EDIT"
                  ? resources["edit_tab_view"]
                  : resources["create_tab_view"]
              }}
            </span>
          </div>

          <div
            class="t-close-button close-button"
            @click="dimissModal"
            aria-hidden="true"
          >
            <i class="icon-close-black"></i>
          </div>
          <div class="head-line"></div>
        </div>
        <div class="modal-body" style="padding-left: 40px; padding-right: 40px">
          <div class="form-group row line-row-wrapper">
            <label
              for="name"
              class="col-md-3 col-form-label"
              style="margin-right: 0px"
            >
              {{ resources["tab_name"] }}
              <span class="badge-require"></span>
            </label>
            <div class="col-md-9">
              <input
                type="text"
                id="name"
                v-model="tab.name"
                class="form-control"
                :placeholder="resources['tab_name']"
                required
                @keyup="removeErrors('name')"
              />
              <div
                class="error-text"
                :style="{ visibility: errors['name'] ? 'visible' : 'hidden' }"
                v-text="resources['error_input_tab_name']"
              ></div>
            </div>
          </div>
          <div class="form-group row line-row-wrapper">
            <label
              for="name"
              class="col-md-3 col-form-label"
              style="margin-right: 0px"
            >
              {{ resources["share_with"] }}
            </label>
            <div class="col-md-9">
              <div
                class="d-flex align-items-center h-100 justify-content-start"
              >
                <input class="mr-1" type="radio" name="onlyMe" checked />
                <span v-text="resources['only_me']"></span>
              </div>
            </div>
          </div>
          <div class="d-flex align-items-center justify-content-between">
            <common-searchbar
              style="width: 60%"
              :placeholder="resources['search_by_selected_column']"
              :request-param="requestParam"
              :on-search="search"
            ></common-searchbar>
            <div class="d-flex align-items-baseline">
              <div class="add-tooling-wrapper">
                <custom-dropdown-button
                  :title="tab.addItemIdList.length + ' Added'"
                  :is-selected="true"
                  :is-show="toolingVisible"
                  @click="toggledToolingDropdown"
                ></custom-dropdown-button>

                <common-popover
                  @close="handleCloseDropdown"
                  :is-visible="toolingVisible"
                  :style="{
                    width: '180px',
                    position: 'fixed',
                    marginTop: '4px',
                  }"
                >
                  <common-select
                    :style="{ position: 'static', width: '100%' }"
                    :items="dataOptionsAdd"
                    @on-select="onChangeMethodAdd"
                  ></common-select>
                </common-popover>
              </div>
              <a href="javascript:void(0)" @click="removeAllToolingSelected()"
                >Remove All</a
              >
            </div>
          </div>
          <div class="container content-group">
            <div class="content-group-header">
              <div class="row">
                <div class="col-md-3 tab-group-title active">
                  <p v-text="resources['terminal_id']"></p>
                  <span
                    class="__icon-sort"
                    :class="[
                      sortField === 'equipmentCode' && sortType === 'asc'
                        ? 'desc'
                        : 'asc',
                    ]"
                    @click="sort('equipmentCode')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['company']"></p>
                  <span
                    class="__icon-sort"
                    :class="[
                      sortField === 'company' && sortType === 'asc'
                        ? 'desc'
                        : 'asc',
                    ]"
                    @click="sort('company')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['status']"></p>
                  <span
                    class="__icon-sort"
                    :class="[
                      sortField === 'equipmentStatus' && sortType === 'asc'
                        ? 'desc'
                        : 'asc',
                    ]"
                    @click="sort('equipmentStatus')"
                  ></span>
                </div>
              </div>
            </div>
            <div
              v-if="dataPerPageList.length > 0"
              class="content-group-wrapper"
            >
              <div
                v-for="(result, index) in dataPerPageList"
                :key="result.terminal?.id"
                class="row"
              >
                <div class="col-md-3">
                  <div>
                    <a
                      href="#"
                      @click.prevent="showTerminalDetails(result.terminal)"
                    >
                      {{ result.terminal?.equipmentCode }}
                    </a>

                    <div class="small text-muted font-size-11-2">
                      <span th:text="#{registered}+':'"></span>
                      {{ formatToDate(result.terminal?.createdAt) }}
                    </div>
                  </div>
                </div>
                <div class="col-md-3">
                  <div v-if="result.terminal?.companyName">
                    <a
                      class="font-size-14"
                      href="#"
                      @click.prevent="
                        showCompanyDetailsById(result.terminal?.companyId)
                      "
                    >
                      {{ result.terminal?.companyName }}
                    </a>
                    <div class="small text-muted font-size-11-2">
                      {{ result.terminal?.companyCode }}
                    </div>
                  </div>
                </div>
                <div class="col-md-3 d-flex align-items-center">
                  {{
                    result.terminal?.equipmentStatus === "INSTALLED"
                      ? "REGISTERED"
                      : result.terminal?.equipmentStatus
                  }}
                </div>
                <div class="col-md-3 action-column">
                  <div
                    v-if="checkDataSelected(result.terminal?.id)"
                    class="d-flex align-items-center h-100 justify-content-start"
                    @click="addDataToTab(result.terminal?.id)"
                  >
                    <span style="margin-right: 4px">Add to tab</span>
                    <img src="/images/icon/add-circle-white.svg" alt="" />
                  </div>
                  <div
                    v-else
                    class="d-flex align-items-center h-100 justify-content-start"
                    @click="removeDataFromTab(result.terminal?.id)"
                  >
                    <span style="margin-right: 4px">Remove from tab</span>
                    <img src="/images/icon/sub-circle-white.svg" alt="" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="col-md-12">
              <ul
                v-if="pagination.totalPages > 0"
                class="pagination d-flex justify-content-end mt-2"
              >
                <div class="modal-body__content__footer">
                  <span>{{
                    `${pagination.pageable.pageNumber + 1} of ${
                      pagination.totalPages
                    }`
                  }}</span>
                </div>
                <div class="paging__arrow">
                  <a
                    href="javascript:void(0)"
                    class="paging-button"
                    :class="{
                      'inactive-button': pagination.pageable.pageNumber < 1,
                    }"
                    @click="getData(pagination.pageable.pageNumber)"
                  >
                    <img src="/images/icon/category/paging-arrow.svg" alt="" />
                  </a>
                  <a
                    href="javascript:void(0)"
                    class="paging-button"
                    :class="{
                      'inactive-button':
                        pagination.pageable.pageNumber + 1 >=
                        pagination.totalPages,
                    }"
                    @click="getData(pagination.pageable.pageNumber + 2)"
                  >
                    <img
                      src="/images/icon/category/paging-arrow.svg"
                      style="transform: rotate(180deg)"
                      alt=""
                    />
                  </a>
                </div>
              </ul>
            </div>
          </div>
          <div class="d-flex modal__footer">
            <base-button
              id="createProjectId"
              :style="{ padding: '4px 8px', marginRight: '8px' }"
              @click="handleClose"
              :size="'medium'"
              :type="'cancel'"
              :level="'secondary'"
            >
              <span>{{
                tabViewModalType === "EDIT" ? "Go Back" : "Cancel"
              }}</span>
            </base-button>
            <a
              @click="saveData"
              id="createTabView"
              href="javascript:void(0)"
              class="btn-custom btn-custom-primary"
            >
              <span>{{
                tabViewModalType === "EDIT" ? "Save Edit" : "Create Tab View"
              }}</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    objectType: String,
  },
  components: {
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
  },
  data() {
    return {
      requestParam: {
        query: "",
        status: "All",
        page: 1,
        isModalSelected: true,
      },
      searchedTabList: [],
      sortType: "asc",
      sortField: "id",
      tab: {
        id: null,
        name: "",
        isDuplicate: false,
        removeItemIdList: [],
        addItemIdList: [],
      },
      errors: [],
      tabViewModalType: "CREATE",
      pagination: [],
      toolingVisible: false,
      dataOptionsAdd: [
        {
          title: "",
          name: "page",
        },
        {
          title: "",
          name: "all",
        },
      ],
      allDataList: [],
      dataPerPageList: [],
      choosedData: [],
      pageType: "TERMINAL_SETTING",
    };
  },
  computed: {
    toolingList() {
      return this.searchedTabList.filter((tab) => !tab.isDefaultTab);
    },
    sortData() {
      return this.sortField + "," + this.sortType;
    },
  },
  created() {
    this.getData(1);
    this.getAllDataList();
  },
  methods: {
    showTerminalDetails: function (part) {
      this.$emit("show-terminal-details", part);
    },
    showCompanyDetailsById: function (company) {
      this.$emit("show-company-details-by-id", company);
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    toggledToolingDropdown(value) {
      this.toolingVisible = value;
    },
    handleCloseDropdown() {
      this.toggledToolingDropdown(false);
    },
    onChangeMethodAdd(value) {
      if (value.name === "page") {
        this.dataPerPageList.forEach((value) => {
          if (this.tab.addItemIdList.indexOf(value.terminal.id) === -1)
            this.tab.addItemIdList.push(value.terminal.id);
        });
      } else {
        this.tab.addItemIdList = this.allDataList.map((i) => i.terminal.id);
      }
      this.$forceUpdate();
    },
    removeAllToolingSelected() {
      this.tab.addItemIdList = [];
      if (this.tabViewModalType === "EDIT") {
        this.tab.removeItemIdList = this.choosedData;
      }
      this.$forceUpdate();
    },
    dimissModal() {
      $("#create-tab-view").modal("hide");
    },
    checkDataSelected(item) {
      return !this.tab.addItemIdList.includes(item);
    },
    addDataToTab(item) {
      this.tab.addItemIdList.push(item);
      this.checkDataSelected(item);
      this.$forceUpdate();
    },
    removeDataFromTab(item) {
      this.tab.addItemIdList = this.tab.addItemIdList.filter((t) => t !== item);
      this.tab.removeItemIdList.push(item);
      this.checkDataSelected(item);
      this.$forceUpdate();
    },
    handleClose() {
      this.$emit("close");
      this.clearData();
    },
    saveData() {
      const el = document.getElementById("createTabView");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        if (!self.validForm()) {
          self.$forceUpdate();
          return;
        }
        self.saveTab();
        el.classList.remove("primary-animation");
      }, 700);
    },
    clearData() {
      this.tab = {
        id: null,
        name: "",
        isDuplicate: false,
        removeItemIdList: [],
        addItemIdList: [],
      };
      this.errors = [];
      this.choosedData = [];
      this.requestParam = {
        query: "",
        status: "All",
        page: 1,
      };
      this.sortField = "id";
      this.sortType = "desc";
      $("#create-tab-view").modal("hide");
    },
    saveTab() {
      const tab = this.tab;
      const isCreate = this.tabViewModalType !== "EDIT";
      const params = {
        id: isCreate ? null : tab.id,
        name: tab.name,
        isDuplicate: false,
        removeItemIdList: isCreate ? [] : tab.removeItemIdList,
        addItemIdList: tab.addItemIdList,
      };
      console.log(params, "params");
      axios
        .post("/api/tab-table/save-tab-table-terminal", params)
        .then((res) => {
          this.handleClose();
          if (isCreate) {
            this.$emit("create-success", res.data.data, {
              title: "Success!",
              content: `Your tab view has been created`,
            });
          } else {
            this.$emit("edit-success", res.data.data, {
              title: "Success!",
              content: `Your tab view has been edited`,
            });
          }
        });
    },
    validForm() {
      if (!this.tab.name) {
        this.errors["name"] = true;
        return false;
      }
      return true;
    },
    removeErrors(type) {
      this.errors[type] = false;
      this.$forceUpdate();
    },
    async getListDataSelectedByTab(isDisable = false, tabType) {
      const res = await axios.get(
        `/api/terminals?tabId=${
          !this.tab.id ? "" : this.tab.id
        }&id=&query=&status=${
          isDisable ? "disabled" : tabType
        }&page=1&pageType=${this.pageType}&size=9999`
      );
      this.tab.addItemIdList = res.data.content.map((i) => i.terminal.id);
      if (this.tabViewModalType === "EDIT") {
        this.choosedData = res.data.content.map((i) => i.terminal.id);
      }
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getAllDataList() {
      const res = await axios.get(
        `/api/terminals?tabId=&id=&query=&status=active&sort=${this.sortData}&page=1&pageType=${this.pageType}&isModalSelected=true&size=99999`
      );
      this.allDataList = res.data.content;
    },
    async getAllDataListSearching(pageNumber) {
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      const res = await axios.get(
        `/api/terminals?tabId=&id=&query=${this.requestParam.query}&status=active&sort=${this.sortData}&page=${this.requestParam.page}&pageType=${this.pageType}&isModalSelected=true`
      );
      this.dataOptionsAdd[0].title =
        "Add Page (" + res.data.numberOfElements + " items)";
      this.dataOptionsAdd[1].title =
        "Add All (" +
        res.data.totalElements +
        (res.data.totalElements > 1 ? " items)" : " item)");
      this.dataPerPageList = res.data.content;
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    async getData(pageNumber) {
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      const res = await axios.get(
        `/api/terminals?tabId=&id=&query=${this.requestParam.query}&status=active&sort=${this.sortData}&page=${this.requestParam.page}&pageType=${this.pageType}&isModalSelected=true`
      );
      this.dataPerPageList = res.data.content;
      console.log(res, this.dataPerPageList, "dataPerPageListdataPerPageList");
      this.dataOptionsAdd[0].title =
        "Add Page (" +
        res.data.numberOfElements +
        (res.data.numberOfElements > 1 ? " items)" : " item)");
      this.dataOptionsAdd[1].title =
        "Add All (" +
        res.data.totalElements +
        (res.data.totalElements > 1 ? " items)" : " item)");
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    showCreateTabView(type, tab, tabName) {
      this.clearData();
      this.getData(1);
      this.tabViewModalType = type;
      if (type === "EDIT") {
        this.tab = tab;
        this.tab.addItemIdList = [];
        this.tab.removeItemIdList = [];
        this.getListDataSelectedByTab();
      } else if (type === "DUPLICATE") {
        this.tab.id = tab.id;
        this.getListDataSelectedByTab(tab.name === "Disabled", tabName);
      } else {
        this.searchedTabList = this.dataPerPageList;
        $("#create-tab-view").modal("show");
      }
    },
    search() {
      this.getAllDataListSearching(1);
    },
    sort: function (type) {
      if (this.sortField === type) {
        this.sortType = this.sortType === "asc" ? "desc" : "asc";
      } else {
        this.sortField = type;
        this.sortType = "desc";
      }
      this.getData(1);
    },
  },
};
</script>
<style>
.add-tooling-wrapper .common-popover {
  top: unset !important;
  left: unset !important;
  padding: unset !important;
}
</style>
<style scoped>
.add-tooling-wrapper .dropdown-wrap .dropdown-list li {
  margin: unset !important;
}
.add-tooling-wrapper .dropdown-wrap label {
  margin-bottom: 0 !important;
}
.add-tooling-wrapper .dropdown-wrap {
  padding: 3px !important;
}
.add-tooling-wrapper {
  margin-right: 12px;
}
.head-line {
  left: 0;
}
.error-text {
  font-size: 11px;
  color: #ef4444;
  text-align: left;
}
.content-group {
  margin-top: 20px;
  border: solid 1px #d0d0d0;
  padding: unset;
  border-radius: 3px;
  overflow-x: hidden;
}
.tab-group-title {
  color: #4b4b4b;
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  height: 50px;
  background-color: #f5f8ff;
  display: flex;
  align-items: center;
}
.tab-group-title p {
  margin-bottom: 0px;
}
.tab-group-item {
  padding-top: 10px;
  padding-bottom: 10px;
  color: #4b4b4b;
  font-size: 15px;
  display: flex;
  align-items: center;
  height: 45px;
  width: 90%;
}
.image-duplicate {
  margin-left: auto;
}
.__icon-sort {
  content: "";
  background: url("../images/icon/new/sort-down-icon-active.svg");
  background-size: 100% 100%;
  transform: rotate(180deg);
  width: 20px;
  height: 20px;
}
.desc.__icon-sort {
  transform: rotate(180deg);
}
.asc.__icon-sort {
  transform: rotate(0deg);
}
.modal-title {
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  color: #4b4b4b;
}
.modal__footer {
  margin-top: 20px;
  justify-content: flex-end;
}
.modal__footer__button {
  margin-left: 8px;
}
.content-group-wrapper {
  height: 315px;
  overflow-y: scroll;
  overflow-x: hidden;
  padding-left: 34px;
  color: #4b4b4b;
}
.content-group-wrapper > .row {
  margin-left: -31px;
  padding-left: 19px;
}
.content-group-wrapper > .row:nth-of-type(odd) {
  background: #fbfcfd;
}
.content-group-header {
  background-color: rgb(245, 248, 255);
  padding-left: 34px;
}
.action-column > div {
  cursor: pointer;
}
.custom-modal-title {
  border-radius: 6px 6px 0px 0px;
  background: rgb(245, 248, 255);
  color: rgb(75, 75, 75);
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 8px 15px 0px 0px;
  align-items: center;
  margin-bottom: 0px;
  width: 100%;
}
.modal-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 30px;
  margin-bottom: 12px;
  font-size: 16px;
}
</style>
