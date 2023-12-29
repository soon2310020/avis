axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

window.onload = function () {
  document.title = "User" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
  //
};

var Page = Common.getPage("users");

const EXPORT_TYPE = {
  PDF: "PDF",
  XLSX: "XLSX",
};

var vm = new Vue({
  el: "#app",
  components: {
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "invite-user": httpVueLoader("/components/user/invite-user.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-user-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
  },
  data: {
    options: {},
    resources: {},
    showDrowdown: null,
    dialog: true,
    results: [],
    total: 0,
    pagination: [],
    requestParam: {
      id: "",
      query: "",
      status: "active",
      companyType: "admin",
      accessLevel: "",
      sort: "id,desc",
      page: 1,
      pageType: "TOOLING_DASHBOARD",
      tabId: "",
    },
    sortType: {
      NAME: "name",
      EMAIL: "email",
      COMPANY: "company.name",
      STATUS: "enabled",
    },
    currentSortType: "id",
    isDesc: true,
    revisionHistory: Object,
    visible: false,
    visibleSuccess: false,
    inviteUsers: [
      {
        index: 1,
        email: "",
        type: "one",
        companyId: undefined,
        isValid: false,
        message: "",
      },
    ],
    companies: {},
    showDropdown: false,
    listAllIds: [],
    listChecked: [],
    listCheckedFull: [],
    // listCheckedTracked: {},
    // isAll: false,
    // isAllTracked: [],
    isAdmin: true,
    dataReady: false,
    tabCurrent: "",
    cancelToken: undefined,

    Access_Level: "Access Level",
    showAccessLevel: false,
    AccessLevel: [
      { title: "Admin", code: "all_admins" },
      { title: "Regular", code: "regular" },
      { title: "Rest User", code: "rest_user" },
      { title: "All", code: "all" },
    ],
    NotAccessLevel: [
      { title: "Regular", code: "regular" },
      { title: "Rest User", code: "regular" },
      { title: "All", code: "regular" },
    ],
    Status: "Access Status",
    ShowStatus: false,
    StatusLevel: [],
    NotAccess_Level: "Access Level",
    showNotAdmin: false,

    EXPORT_TYPE: EXPORT_TYPE,
    selectedDownloadType: "default",
    currentIntervalIds: [],
    isCallingDone: false,
    isStopedExport: false,
    pageType: "TOOLING_DASHBOARD",
    optionParams: {},
    objectType: "TOOLING",
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
    currentUser: null,
  },
  watch: {
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
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
    selectedUserType: function () {
      if (this.listCheckedFull.length > 0) {
        if (
          this.listCheckedFull.every(
            (item) => (!item.requested && item.enabled) || item.requested
          )
        ) {
          return "enable";
        }
        if (
          this.listCheckedFull.every((item) => !item.requested && !item.enabled)
        ) {
          return "disabled";
        }
      }
      return "both";
    },
    isloginAuditTrailEnabled() {
      return this.options?.CLIENT?.userList?.loginAuditTrailEnabled || false;
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
        removeItemIdList:
          this.total === this.listAllIds.length
            ? this.listAllIds
            : this.listChecked,
        addItemIdList: [],
      };
      axios.post("/api/tab-table/save-tab-table-user", params).then(() => {
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
        if (this.isAdmin) {
          child.showCreateTabView(
            "DUPLICATE",
            tab,
            this.getCompanyTypeAdmin(tab.name)
          );
        } else {
          child.showCreateTabView(
            "DUPLICATE",
            tab,
            this.getCompanyType(tab.name)
          );
        }
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
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      await axios
        .get("/api/tab-table/by-current-user?objectType=USER")
        .then((res) => {
          this.listSelectedTabs = res.data.data;

          if (isCallAtMounted) {
            this.setNewActiveTab();
          }
        });
    },
    setNewActiveTab() {
      this.tab(this.listSelectedTabs[0]);
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
    select_access(item) {
      // alert(item);

      this.Access_Level = item.title;
      this.showAccessLevel = false;
      this.requestParam.accessLevel = item.code;
      this.search();
    },
    handleshowAccessLevel: function () {
      this.showAccessLevel = true;
    },
    handlecloseAccessLevel() {
      this.showAccessLevel = false;
    },
    handleaccesslevelToggle(isShow) {
      this.showAccessLevel = isShow;
    },

    select_Status(item) {
      // alert(item);

      this.Status = item.title;
      this.ShowStatus = false;
      this.requestParam.status = item.code;
      this.search();
    },
    handlecloseStatus() {
      this.ShowStatus = false;
    },
    handleStatusToggle(isShow) {
      this.ShowStatus = isShow;
    },

    handlenotadminToggle(isShow) {
      this.showNotAdmin = isShow;
    },
    handlecloseNotAdmin() {
      this.showNotAdmin = false;
    },

    select_NotAdmin(item) {
      this.NotAccess_Level = item.title;
      this.showNotAdmin = false;
      this.requestParam.accessLevel = item.code;
      this.search();
    },

    progressBarRun() {
      this.currentIntervalIds.forEach((id) => {
        clearInterval(id);
      });
      let $vm = this;
      let flag = 0;
      if (flag == 0) {
        flag = 1;
        var elem = document.getElementById("myBar");
        elem.style.width = "0%";
        var width = 1;
        var id = setInterval(frame, 40);

        this.currentIntervalIds = [];
        this.currentIntervalIds.push(id);

        function frame() {
          if (width >= 100) {
            clearInterval(id);
            flag = 0;
            document.getElementById("toggleExportProgressBar").click();
          } else {
            if ($vm.isStopedExport) {
              document.getElementById("toggleExportProgressBar").click();
              $vm.currentIntervalIds.forEach((id) => {
                clearInterval(id);
              });
            }
            // console.log($vm.isCallingDone)
            if ($vm.isCallingDone) {
              width = width + 1;
            }
            if (width < 30) {
              width = width + 0.5;
            } else if (width >= 30 && width < 50) {
              width = width + 0.3;
            } else if (width >= 50 && width < 75) {
              width = width + 0.1;
            } else if (width >= 75 && width < 85) {
              width = width + 0.05;
            } else if (width >= 85 && width < 99.5) {
              width = width + 0.01;
            }
            elem.style.width = width + "%";
          }
        }
      }
    },
    stopExport() {
      this.isStopedExport = true;
    },
    downloadTooling(type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldinoo_user_data`;

      let path = "";

      const listChecked =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;

      let downloadParams = listChecked.join(",");

      if (type === EXPORT_TYPE.PDF) {
        path = "exportPdfDetailMolds";
      } else {
        path = "exportExcel";
      }

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      let $vm = this;
      let sort = this.requestParam.sort
        ? `&sort=${this.requestParam.sort}`
        : ``;
      let url = `${
        Page.API_BASE
      }/${path}?ids=${downloadParams}&fileName=${fileName}=${new Date().getTimezoneOffset()}${sort}`;
      if (this.listChecked.length === 0) {
        url = `${
          Page.API_BASE
        }/${path}?fileName=${fileName}=${new Date().getTimezoneOffset()}${sort}`;
      }
      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          if (!$vm.isStopedExport) {
            $vm.isCallingDone = true;
            setTimeout(() => {
              var blob = new Blob([response.data], {
                type: "text/plain;charset=utf-8",
              });
              if (type === EXPORT_TYPE.PDF) {
                saveAs(blob, `${fileName}.pdf`);
              } else {
                saveAs(blob, `${fileName}.xlsx`);
              }
            }, 2000);
          }
        },
        (error) => {
          console.log(error);
          $vm.isStopedExport = true;
        }
      );
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    hasChecked: function () {
      return this.listChecked.length > 0;
    },
    check(e) {
      console.log("@check", {
        value: e.target.value,
        checked: e.target.checked,
      });
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
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/users/${this.listChecked[0]}`;
    },
    changeButtonColor() {
      $(".nav-btn-light").click(function () {
        $(this).addClass("nav-btn-light-active");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("nav-btn-light-active");
          }
        );
      });
      $(".nav-btn-primary").click(function () {
        $(this).addClass("nav-btn-primary-active");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("nav-btn-primary-active");
          }
        );
      });
    },
    async getCurrentUser() {
      try {
        const me = await Common.getSystem("me");
        const __currentUser = JSON.parse(me);
        if (!__currentUser.admin) {
          vm.isAdmin = false;
          vm.requestParam.companyType = "non_admin";
          vm.StatusLevel = [
            { title: "Enabled", code: "active" },
            { title: "Disabled", code: "disabled" },
            { title: "All", code: "all" },
          ];
        } else {
          vm.StatusLevel = [
            { title: "Enabled", code: "active" },
            { title: "Disabled", code: "disabled" },
            { title: "Request Access", code: "requested" },
            { title: "All", code: "all" },
          ];
        }
      } catch (error) {
        console.log(error);
      }
    },
    changeShow(resultId) {
      //console.log('swwwww chúng con nhớ mĩan', resultId)
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
    update: function (id, item) {
      alert(id);
      item.email = "dbclose@gmail.com";
    },

    getCompanyTypeAdmin(tabname) {
      let companyType = "";
      switch (tabname) {
        case "Enabled":
          companyType = "admin";
          break;
        case "In-house":
          companyType = "in_house";
          break;
        case "Supplier":
          companyType = "supplier";
          break;
        case "Toolmaker":
          companyType = "tool_maker";
          break;
        case "Disabled":
          companyType = "";
          break;
      }
      return companyType;
    },
    getCompanyType(tabname) {
      let companyType = "";
      switch (tabname) {
        case "Enabled":
          companyType = "non_admin";
          break;
        case "In-house":
          companyType = "non_inhouse";
          break;
        case "Supplier":
          companyType = "supplier";
          break;
        case "Toolmaker":
          companyType = "tool_maker";
          break;
        case "Disabled":
          companyType = "";
          break;
      }
      return companyType;
    },
    tab(tab) {
      this.currentActiveTab = tab;
      this.total = null;
      this.requestParam.tabId = "";
      this.tabCurrent = tab.name;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.listAllIds = [];
      if (this.isAdmin) {
        this.requestParam.companyType = this.getCompanyTypeAdmin(tab.name);
      } else {
        this.requestParam.companyType = this.getCompanyType(tab.name);
      }
      if (tab.name === "Disabled") {
        this.requestParam.status = "disabled";
        this.paging(1);
      } else {
        // TODO(Ducnm2010): recheck
        if (this.requestParam.status) {
          //
        } else if (tab.name === "Enabled" || !tab.isDefaultTab) {
          this.requestParam.status = "active";
        } else {
          this.requestParam.status = "";
        }

        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        delete this.requestParam.deleted;
        if (!tab.isDefaultTab) {
          this.requestParam.tabId = tab.id;
        }
        this.paging(1);
      }
      delete this.requestParam.userIds;
      this.changeShow(null);
    },
    tabToDisable(status) {
      this.tabCurrent = status;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.requestParam.companyType = "";
      this.paging(1);
    },
    search() {
      this.paging(this.requestParam.page);
    },
    enable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, true);
    },
    disable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, false);
    },
    showRevisionHistory: function (id) {
      //console.log(id);
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "USER");
      }
    },
    saveBatch(idArr, boolean) {
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      const self = this;
      axios
        .post("/api/users/change-status-in-batch", param)
        .then(function (response) {
          self.getTabsByCurrentUser();
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          self.listChecked = [];
          self.listCheckedFull = [];
          self.isAll = false;
          self.showDropdown = false;
        });
    },
    paging(pageNumber) {
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      this.requestParam.id = objectId;

      let notificationType = Common.getParameter("notificationType");
      if (["INVITE_USER"].includes(notificationType) && !vm.requestParam.id) {
        this.requestParam.id = Common.getParameter("param");
      }

      const param = Common.param(this.requestParam);
      console.log({ param });
      if (typeof this.cancelToken !== typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/users?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          vm.total = response.data.totalElements;
          const currentTabIndex = vm.listSelectedTabs.findIndex(
            (item) => item.id === vm.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            vm.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          vm.results = response.data.content;
          if (objectId !== "") {
            let searchParam = vm.results.filter(
              (item) => parseInt(item.id) === parseInt(objectId)
            )[0];
            vm.requestParam.query = searchParam.name;
          }

          vm.pagination = Common.getPagingData(response.data);
          document.querySelector("#app .op-list").style.display =
            "table-row-group";
          document.querySelector(".pagination").style.display = "flex";

          var noResult = document.querySelector(".no-results");
          if (noResult != null) {
            if (vm.results.length === 0) {
              noResult.className = noResult.className.replace("d-none", "");
            } else {
              noResult.className =
                noResult.className.replace("d-none", "") + " d-none";
            }
          }
          if (vm.results.length > 0) {
            Common.triggerShowActionbarFeature(vm.$children);
          }
        })
        .catch(function (error) {
          console.log(error.response);
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
      this.paging(this.requestParam.page);
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    showInviteUserModal() {
      const el = document.getElementById("goToNew");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        var child = Common.vue.getChild(this.$children, "invite-user");
        if (child != null) {
          child.showInviteUser();
        }
      }, 500);
    },
    goToNew() {
      const el = document.getElementById("goToNew2");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        window.location.href = "/admin/users/new";
      }, 500);
    },
    async showModal() {
      await axios
        .get("/api/companies/companies-group")
        .then((response) => {
          this.companies = response.data.data;
          console.log(this.companies);
        })
        .catch((error) => {
          console.log(error);
        });
      this.visible = true;
    },
    async submitUsers() {
      let param = this.inviteUsers.map((item) => {
        let newObject = {};
        if (item.type == "multiple") {
          const patten = /\s*,\s*/;
          let emails = item.email.split(patten);
          newObject = {
            index: item.index,
            companyId: item.companyId,
            emailList: emails,
          };
        } else {
          newObject = {
            index: item.index,
            companyId: item.companyId,
            email: item.email,
          };
        }
        return newObject;
      });
      console.log(param);
      await axios
        .post("/api/users/invite-user", { userInviteList: param })
        .then((response) => {
          console.log(response.data);
          let data = response.data;
          if (!data.success) {
            data.data.forEach((element) => {
              let wrongData = this.inviteUsers.filter(
                (item) => item.index === element.index
              );
              if (wrongData.length > 0) {
                wrongData[0].isValid = false;
                if (wrongData[0].type == "one") {
                  wrongData[0].message = `Email address is already registered in the system.`;
                } else {
                  let wrongUsers = element.data.join(", ");
                  wrongData[0].message = `User ${wrongUsers} are already registered in the system.`;
                }
              }
            });
          } else {
            this.visible = false;
            this.visibleSuccess = true;
          }
        })
        .catch((error) => console.log(error));
    },
    resetInviteData() {
      this.inviteUsers = [
        {
          index: 1,
          email: "",
          type: "one",
          companyId: undefined,
          isValid: false,
          message: "",
        },
      ];
    },
    handleOkay(e) {
      console.log(e);
      this.resetInviteData();
      this.visibleSuccess = false;
    },
    addUser(type) {
      let newIndex = +this.inviteUsers[this.inviteUsers.length - 1].index + 1;
      this.inviteUsers.push({
        index: newIndex,
        email: "",
        type: `${type}`,
        companyId: undefined,
        isValid: false,
        message: "",
      });
    },
    deleteUser(index) {
      this.inviteUsers.splice(index, 1);
    },
    checkValidAllUser() {
      let enable = this.inviteUsers.filter(
        (item) => !item.isValid || item.companyId == undefined
      );
      return enable.length == 0;
    },
    checkValidEmail(inviteUser, type) {
      if (inviteUser.email == "") {
        inviteUser.isValid = false;
        inviteUser.message = "";
      } else {
        if (type == "one") {
          if (
            /^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)$/.test(
              inviteUser.email
            )
          ) {
            inviteUser.isValid = true;
            inviteUser.message = "";
          } else {
            inviteUser.isValid = false;
            inviteUser.message =
              "Please enter a valid email format (e.g.example@email.com).";
          }
        } else {
          const patten =
            /^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)+((\s*,\s*)([A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)))*$/;
          if (patten.test(inviteUser.email)) {
            inviteUser.isValid = true;
            inviteUser.message = "";
          } else {
            inviteUser.isValid = false;
            inviteUser.message =
              "Please enter a valid email format (e.g.example@email.com).";
          }
        }
      }
    },

    handleChangeCompany(index, companyId) {
      this.inviteUsers[index].companyId = companyId;
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} users on this current page are selected.`,
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
          title: `All ${this.total} users are selected.`,
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
        var param = Common.param({ ...this.requestParam, frameType: "USER" });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + param
        );
        this.listAllIds = response.data.data;
        console.log("response.data", response.data);
        console.log("this.listAllIds.length", this.listAllIds);
        console.log("this.total", this.total);
      } catch (error) {
        console.log(error);
      }
    },
    captureQuery() {
      const url = new URL(location.href);
      const params = url.searchParams;
      const status = params.get("status");
      const userIds = params.get("userIds");

      if (userIds) {
        this.requestParam.userIds = userIds;
      }

      if (status) {
        this.requestParam.status = status;
        this.Status = this.StatusLevel.find(
          (item) => item.code === status
        ).title;
      }
    },
  },
  created() {
    this.changeButtonColor();

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (!newVal) return;
        this.resources = Object.assign({}, this.resources, newVal);
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.currentUser,
      (newVal) => {
        if (!newVal) return;
        this.currentUser = Object.assign({}, this.currentUser, newVal);
        if (!this.currentUser?.admin) {
          this.isAdmin = false;
          this.requestParam.companyType = "non_admin";
          this.StatusLevel = [
            { title: "Enabled", code: "active" },
            { title: "Disabled", code: "disabled" },
            { title: "All", code: "all" },
          ];
        } else {
          this.StatusLevel = [
            { title: "Enabled", code: "active" },
            { title: "Disabled", code: "disabled" },
            { title: "Request Access", code: "requested" },
            { title: "All", code: "all" },
          ];
        }

        //
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (!newVal) return;
        this.options = Object.assign({}, this.options, newVal);
      },
      { immediate: true }
    );

    this.captureQuery();
    this.getTabsByCurrentUser(true);
  },
  // async mounted() {
  // console.log("@mounted");
  // this.getTabsByCurrentUser(true);
  // },
});
