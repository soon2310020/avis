axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

window.onload = function () {
  document.title = "Companies" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

var Page = Common.getPage("companies");

var vm = new Vue({
  el: "#app",
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "upper-company-dropdown": httpVueLoader(
      "/components/upper-company-dropdown.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-company-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
  },
  data: {
    // it's not worked until we import Common here
    Common,

    // end
    userType: "",
    showDrowdown: null,
    results: [],
    total: 0,
    pagination: [],
    requestParam: {
      query: "",
      status: "active",
      sort: "id,desc",
      page: 1,
      tabId: "",
    },
    codes: {},
    sortType: {
      NAME: "name",
      COMPANY_CODE: "companyCode",
      ADDRESS: "address",
      STATUS: "enabled",
      TOTAL_MOLD_COUNT: "total_mold_count",
    },
    currentSortType: "id",
    isDesc: true,
    resources: {},
    showDropdown: false,
    listChecked: [],
    listCheckedTracked: {},
    listCheckedFull: [],
    allColumnList: [],
    pageType: "COMPANY_SETTING",
    objectType: "COMPANY",
    cancelToken: undefined,
    visiblePopover: false,
    listSelectedTabs: [],
    currentActiveTab: null,
    visibleMoreAction: false,
    isTabView: false,
    allToolingList: [],
    triggerUpdateManageTab: 0,
    toastAlert: {
      value: false,
      title: "",
      content: "",
    },
    visibleMove: false,
    userType: "",
    listAllIds: [],
  },
  computed: {
    isAll() {
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
    enableBatchDisable() {
      return (
        this.requestParam.status !== "disabled" &&
        this.requestParam.status != "DELETED"
      );
    },
    enableBatchEnable() {
      return (
        this.requestParam.status === "disabled" &&
        this.requestParam.status != "DELETED"
      );
    },
  },
  watch: {
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
  },
  methods: {
    selectTab(tabCheck, index) {
      let __listSelectedTabs = this.listSelectedTabs;
      __listSelectedTabs[index].show = !tabCheck.show;
      if (
        !tabCheck.show &&
        ((tabCheck.isDefaultTab &&
          this.currentActiveTab.isDefaultTab &&
          tabCheck.name === this.currentActiveTab.name) ||
          (!tabCheck.isDefaultTab &&
            !this.currentActiveTab.isDefaultTab &&
            tabCheck.id === this.currentActiveTab.id))
      ) {
        this.closeTab(tabCheck);
      }
      this.listSelectedTabs = __listSelectedTabs;
    },
    removeFromTab() {
      const params = {
        id: this.currentActiveTab.id,
        name: this.currentActiveTab.name,
        isDuplicate: false,
        removeItemIdList: this.listChecked,
        addItemIdList: [],
      };
      axios.post("/api/tab-table/save-tab-table-company", params).then(() => {
        this.createSuccess(params, {
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      });
    },
    moveSuccess(toast) {
      this.getTabsByCurrentUser();
      this.paging(1);
      this.listChecked = [];
      this.listCheckedFull = [];
      if (toast) {
        this.showToastAlert(toast);
        setTimeout(() => {
          this.closeToastAlert();
        }, 3000);
      }
    },
    closeTab(tab) {
      tab.show = false;
      if (tab.name === this.currentActiveTab.name) {
        this.setNewActiveTab();
      }
      let tabChange = [
        {
          id: tab.id,
          isShow: false,
          deleted: tab.deleted,
          name: tab.name,
          isDefaultTab: tab.isDefaultTab,
          objectType: tab.objectType,
        },
      ];
      axios.put("/api/tab-table", tabChange);
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      try {
        const res = await axios.get(
          "/api/tab-table/by-current-user?objectType=COMPANY"
        );
        this.listSelectedTabs = res.data.data;
        this.currentActiveTab = this.listSelectedTabs[0];
        console.log(
          `@getTabsByCurrentUser:this.listSelectedTabs`,
          this.listSelectedTabs
        );
        console.log(
          `@getTabsByCurrentUser:this.currentActiveTab`,
          this.currentActiveTab
        );
        if (isCallAtMounted) this.setNewActiveTab();
      } catch (error) {
        console.log(error);
      }
    },
    openManageTabView() {
      this.visibleMoreAction = false;
      let child = Common.vue.getChild(this.$children, "manage-tab-view");
      if (child != null) {
        child.show();
      }
    },
    setNewActiveTab() {
      const listShowed = this.listSelectedTabs.filter((t) => t.show);
      if (listShowed.length) {
        this.tab(listShowed[0]);
      }
    },
    checkStatusParam(tabItem) {
      return (
        (this.currentActiveTab.id != null &&
          tabItem.id != null &&
          this.currentActiveTab.id === tabItem.id) ||
        (this.currentActiveTab.id == null &&
          tabItem.id == null &&
          this.currentActiveTab.name === tabItem.name)
      );
    },
    closeToastAlert() {
      this.toastAlert.value = false;
    },
    showToastAlert(toast) {
      this.toastAlert.value = true;
      this.toastAlert.title = toast.title;
      this.toastAlert.content = toast.content;
    },
    editSuccess(tabCreated, toast) {
      this.getTabsByCurrentUser();
      this.tab(tabCreated);
      this.showToastAlert(toast);
      this.triggerUpdateManageTab++;
      setTimeout(() => {
        this.closeToastAlert();
      }, 3000);
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    createSuccess(tabCreated, toast) {
      this.getTabsByCurrentUser();
      this.tab(tabCreated);
      this.showToastAlert(toast);
      setTimeout(() => {
        this.closeToastAlert();
      }, 3000);
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    openDuplicate(tab) {
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("DUPLICATE", tab);
      }
    },
    openEdit(tab) {
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("EDIT", tab);
      }
    },
    openManageTabView() {
      this.visibleMoreAction = false;
      let child = Common.vue.getChild(this.$children, "manage-tab-view");
      if (child != null) {
        child.show();
      }
    },
    createTabView() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("CREATE");
      }
    },
    openCustomizationModal() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "customization-modal");
      if (child != null) {
        child.openModal();
      }
    },
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
    },
    async updateTabStatus() {
      let tabsChange = this.listSelectedTabs.map((tab) => {
        return {
          id: tab.id,
          isShow: tab.show,
          deleted: tab.deleted,
          name: tab.name,
          isDefaultTab: tab.isDefaultTab,
          objectType: tab.objectType,
        };
      });
      axios.put("/api/tab-table", tabsChange);
    },
    convertCodeToName(tabItem) {
      if (tabItem.isDefaultTab) {
        if (tabItem.name === "DELETED") {
          return "Disabled";
        } else {
          return (
            tabItem.name.charAt(0).toUpperCase() +
            tabItem.name.slice(1).toLocaleLowerCase().replace("_", " ")
          );
        }
      } else {
        return this.truncateText(tabItem.name, 20);
      }
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
      console.log({
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
      });
    },
    goToEdit() {
      window.location.href = `/admin/companies/${this.listChecked[0]}/edit`;
    },
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    hideCalendarPicker(event) {
      if (event.toElement && event.toElement.localName != "img") {
        this.changeShow(null);
      }
    },
    showRevisionHistory: function (id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "COMPANY");
      }
    },
    tab(tab) {
      console.log("@tab");
      this.currentActiveTab = tab;
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = this.getStatus(tab);
      if (!tab.isDefaultTab) {
        this.requestParam.tabId = tab.id;
      } else {
        this.requestParam.tabId = "";
      }
      this.paging(1);
    },
    getStatus(tab) {
      switch (tab.name) {
        case "Enabled":
          return "active";
        case "In-house":
          return "IN_HOUSE";
        case "Supplier":
          return "SUPPLIER";
        case "Toolmaker":
          return "TOOL_MAKER";
        case "Disabled":
          return "disabled";
        default:
          return "active";
      }
    },
    search(page) {
      this.paging(1);
    },
    deleteCompany: function (company) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: company.id,
      };
      axios
        .delete(Page.API_BASE + "/" + company.id, param)
        .then(function (response) {
          if (response.data.success) {
            this.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    enable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, true);
    },

    showCompanyDetails(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetails(company);
      }
    },
    showCompanyDetailsById(companyId) {
      let child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(companyId);
      }
    },

    disable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, false);
    },
    saveBatch(idArr, boolean) {
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      axios
        .post("/api/companies/change-status-in-batch", param)
        .then(() => {
          this.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
    },
    paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(this.requestParam);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();

      axios
        .get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then((response) => {
          this.total = response.data.totalElements;
          const currentTabIndex = this.listSelectedTabs.findIndex(
            (item) => item.id === this.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            this.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          this.results = response.data.content;
          this.pagination = Common.getPagingData(response.data);

          document.querySelector("#app .op-list").style.display =
            "table-row-group";
          document.querySelector(".pagination").style.display = "flex";

          var noResult = document.querySelector(".no-results");
          if (noResult != null) {
            if (this.results && this.results.length == 0) {
              noResult.className = noResult.className.replace("d-none", "");
            } else {
              noResult.className =
                noResult.className.replace("d-none", "") + " d-none";
            }
          }

          if (this.total === this.listAllIds.length) {
            // refresh checklist in case select all items
            this.listChecked = [];
            this.listCheckedFull = [];
          }

          var pageInfo =
            this.requestParam.page == 1
              ? ""
              : "?page=" + this.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (this.results && this.results.length > 0) {
            Common.triggerShowActionbarFeature(this.$children);
          }
        })
        .catch((error) => {
          console.log(error);
        });
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

    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },

    // #850 Add Total Tooling column for Company tab
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["company_name"],
          field: "name",
          sortable: true,
          default: true,
          mandatory: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["company_id"],
          field: "companyCode",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["address"],
          field: "address",
          sortable: true,
          default: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["total_mold_count"],
          field: "moldCount",
          sortable: true,
          default: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["status"],
          field: "enable",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["upper_tier_company"],
          field: "upperTierCompanies",
          sortable: true,
          sortField: "upperTierCompanies",
        },
      ];
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(
          this,
          this.objectType,
          this.allColumnList,
          null,
          this.configDeleteField
        );
        this.getCustomColumnAndInit();
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
    },
    getColumnListSelected() {
      console.log("/api/config/column-config?pageType=", this.pageType);
      axios.get(`/api/config/column-config?pageType=${this.pageType}`).then(
        (response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              console.log(item.columnName);
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.$forceUpdate();
            console.log("this is the child view obj", this.$children);
            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        },
        (error) => {
          Common.alert(error.response.data);
        }
      );
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally((error) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    saveSelectedList(dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });
      this.allColumnList = dataFake;
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
        .then(
          (response) => {
            console.log("this is the current pagetype:", this.pageType);
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
          },
          () => {
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.$forceUpdate();
          }
        );
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} companies on this current page are selected.`,
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
          title: `All ${this.total} companies are selected.`,
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
        const params = Common.param({
          ...this.requestParam,
          pageType: this.pageType,
          frameType: "COMPANY_SETTING",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + params
        );
        this.listAllIds = response.data.data;
        console.log(this.listAllIds);
      } catch (error) {
        console.log(error);
      }
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCode,
      (newVal) => {
        this.codes = Object.assign({}, this.codes, newVal);
      },
      { immediate: true }
    );
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (!newVal) return;
        this.resources = Object.assign({}, this.resources, newVal);
        this.setAllColumnList();
      },
      { immediate: true }
    );
    this.$watch(
      () => headerVm?.userType,
      (newVal) => {
        if (newVal) this.userType = newVal;
      },
      { immediate: true }
    );

    // capture query
    const params = Common.parseParams(URI.parse(location.href).query);
    if (params.ids) this.requestParam.ids = params.ids;
  },
  mounted() {
    this.getTabsByCurrentUser(true);
  },
});
