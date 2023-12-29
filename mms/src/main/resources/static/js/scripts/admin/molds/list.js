axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

// register for page
Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

// TODO: RECHECK
window.onload = () => {
  document.title = "Tooling" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    $("#disable-modal").removeClass("d-none");
  }, 500);
};

// TODO: RECHECK
$(() => {
  var selectTarget = $(".selectbox select");

  selectTarget.on({
    focus: () => {
      $(this).parent().addClass("focus");
    },
    blur: () => {
      $(this).parent().removeClass("focus");
    },
  });

  selectTarget.change(() => {
    var select_name = $(this).children("option:selected").text();
    $(this).siblings("label").text(select_name);
  });
});

var Page = Common.getPage("molds");

const EXPORT_TYPE = {
  PDF: "PDF",
  XLSX: "XLSX",
};

var vm = new Vue({
  el: "#app",
  components: {
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    "corrective-form": httpVueLoader("/components/corrective-form.vue"),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "location-history": httpVueLoader("/components/location-history.vue"),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "op-status": httpVueLoader("/components/configuration/OpStatus.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "export-data-modal": httpVueLoader("/components/export-data-modal.vue"),
    "custom-export-data-modal": httpVueLoader(
      "/components/custom-export-data-modal.vue"
    ),
    "common-select-popover": httpVueLoader(
      "/components/common/dc-component/common-select-popover.vue"
    ),
    "category-product-dropdown": httpVueLoader(
      "/components/category-product-dropdown.vue"
    ),
    "category-library-modal": httpVueLoader(
      "/components/category/category-library/index.vue"
    ),
    "edit-product-demand": httpVueLoader(
      "/components/category/edit-product-demand.vue"
    ),
    "progress-bar": httpVueLoader(
      "/components/@base/progress-bar/progress-bar.vue"
    ),
    "depreciation-popup": httpVueLoader(
      "/components/mold-popup/depreciation-popup.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
    "multi-chart": httpVueLoader(
      "/components/chart-mold/multi-chart/multi-chart.vue"
    ),
    "upper-company-dropdown": httpVueLoader(
      "/components/upper-company-dropdown.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
  },
  data() {
    return {
      listOfItems: [
        { title: "Create Tooling" },
        { title: "Import Tooling(s)" },
        { title: "Create Tooling Property" },
      ],
      showPartPicker: false,
      ListFilterItems: [
        {
          id: "OP",
          title: "By Tooling Status",
          childrens: [
            { title: "On Standby", code: "ON_STANDBY" },
            { title: "In Production", code: "IN_PRODUCTION" },
            { title: "Idle", code: "IDLE" },
            { title: "Inactive", code: "NOT_WORKING" },
            { title: "Sensor Offline", code: "SENSOR_OFFLINE" },
            { title: "Sensor Detached", code: "SENSOR_DETACHED" },
            { title: "No Sensor", code: "NO_SENSOR" },
          ],
        },
        {
          id: "status",
          // title:'Status',
          title: "By Sensor Status",
          childrens: [
            { code: "INSTALLED", title: "Installed" },
            { code: "NOT_INSTALLED", title: "Not Installed" },
            { code: "DETACHED", title: "Detached" },
          ],
        },
      ],
      dropDownType: "main",
      showPartPicker: false,
      showFilter: false,
      cost: false,
      tabStatus: "All",
      digitalactive: "",
      isactive: "",
      isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
      deletePopup: false,
      resources: {},
      serverName: "",
      showDrowdown: null,
      showDropdown: false,
      errorFieldName: false,
      usetType: "",
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        id: "",
        query: "",
        status: "All",
        sort: "lastShotAt,desc",
        page: 1,
        operatingStatus: "",
        operatingStatuses: [],
        equipmentStatuses: [],
        equipmentStatus: "",
        locationName: "",
        pageType: "TOOLING_SETTING",
        companyId: "",
        tabType: "",
        inactiveLevel: "",
        tabbedDashboardRedirected: "",
        isMapRedirect: false,
        moldStatusList: [
          "IN_PRODUCTION",
          "IDLE",
          "NOT_WORKING",
          "SENSOR_OFFLINE",
          "SENSOR_DETACHED",
          "NO_SENSOR",
          "ON_STANDBY",
        ],
        counterStatusList: ["INSTALLED", "NOT_INSTALLED", "DETACHED"],
      },

      firstCompare: "eq",
      secondCompare: "and",
      thirdCompare: "eq",
      firstCaviti: "",
      secondCaviti: "",
      counterId: "",
      partId: "",
      equipmentStatusCodes: [],
      listCategories: [],
      codes: {},
      sortType: {
        TOOLING_ID: "equipmentCode",
        // COUNTER: 'counter.equipmentCode',
        PART: "part",
        COMPANY: "location.company.name",
        LOCATION: "location.name",
        LAST_SHOT: "lastShot",
        OP: "operatingStatus",
        STATUS: "equipmentStatus",
        DATA_SUBMISSION: "dataSubmission",
      },
      currentSortType: "lastShotAt",
      isDesc: true,
      listChecked: [],
      // loadingAction: false,
      pageType: "TOOLING_SETTING",
      allColumnList: [],
      optionParams: {},
      visible: false,
      dropdownOpening: false,
      objectType: "TOOLING",
      listCheckedFull: [],
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false,
      configDeleteField: {},
      showExportModal: false,
      dataExport: "",
      showExport: false,
      EXPORT_TYPE: EXPORT_TYPE,
      selectedDownloadType: "default",
      currentIntervalIds: [],
      isCallingDone: false,
      isStopedExport: false,
      dashboardChartType: null,
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      featureList: ["ACTION_BAR", "TOOLING_TAB", "DATA_TABLE_TAB"],
      visiblePopover: false,
      displayCaret: false,
      showAnimation: false,
      //
      triggerSaveModal: 0,
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      },
      editFromChild: false,
      userType: null,
      listSelectedTabs: [],
      visibleMoreAction: false,
      currentActiveTab: {
        id: null,
        isDefaultTab: true,
        name: "All",
        show: true,
      },
      isTabView: false,
      allToolingList: [],
      toastAlert: {
        value: false,
        title: "",
        content: "",
      },
      visibleMove: false,
      showMultiChart: false,
      workorderModalVisible: false,
      showUpDepreciationPopup: false,
      showSlDepreciationPopup: false,
      selectedToolingId: null,
      options: {},
      triggerUpdateManageTab: 0,
      showWorkOrderDropdown: false,
      workOrderType: [
        {
          name: "General",
          value: "GENERAL",
        },
        {
          name: "Inspection",
          value: "INSPECTION",
        },
        {
          name: "Emergency",
          value: "EMERGENCY",
        },
        {
          name: "Preventive Maintenance",
          value: "PREVENTATIVE_MAINTENANCE",
        },
        {
          name: "Corrective Maintenance",
          value: "CORRECTIVE_MAINTENANCE",
        },
        {
          name: "Disposal",
          value: "DISPOSAL",
        },
        {
          name: "Refurbishment",
          value: "REFURBISHMENT",
        },
      ],
      selectedWorkOrderType: {},
      isShowDisableWarning: false,
      ruleOpStatus: {
        inProduct: "",
        idle: "",
        inactive: "",
        sensorOffline: "",
        sensorDetached: "The sensor was physically detached from the tooling.",
        disabled: "The tooling has been  manually disabled.",
        disposed: "The tooling has been through the disposal process.",
        noSensor: "The tooling has no matched sensor.",
        onStandby:
          "The tooling has been matched with a sensor but not made any shot yet.",
      },
      isFilterChanging: false,
      defaultStatusListLength: 0,
      listAllIds: [],
    };
  },

  watch: {
    isShowDisableWarning(newVal) {
      // console.log("isShowDisableWarning", newVal);
    },
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
    visiblePopover(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    },
  },
  methods: {
    getTooltipCounterStatus(status) {
      switch (status) {
        case "INSTALLED":
          return "The sensor has been matched with a tooling.";
        case "NOT_INSTALLED":
          return "The sensor has no matched tooling.";
        case "DETACHED":
          return "The sensor has become physically detached from the tooling.";
        default:
          return "";
      }
    },
    getTooltipMoldStatus(status) {
      switch (status) {
        case "WORKING":
          return this.ruleOpStatus.inProduct;
        case "IDLE":
          return this.ruleOpStatus.idle;
        case "NOT_WORKING":
          return this.ruleOpStatus.inactive;
        case "SENSOR_OFFLINE":
          return this.ruleOpStatus.sensorOffline;
        case "SENSOR_DETACHED":
          return this.ruleOpStatus.sensorDetached;
        case "NO_SENSOR":
          return this.ruleOpStatus.noSensor;
        case "DISABLED":
          return this.ruleOpStatus.disabled;
        case "ON_STANDBY":
          return this.ruleOpStatus.onStandby;
        default:
          return "";
      }
    },
    getTimeUnit(timeUnit) {
      return timeUnit === "HOURS" ? " hour(s) " : " day(s) ";
    },
    getMoldStatusText(status) {
      switch (status) {
        case "WORKING":
          return "In Production";
        case "IDLE":
          return "Idle";
        case "NOT_WORKING":
          return "Inactive";
        case "SENSOR_OFFLINE":
          return "Sensor Offline";
        case "SENSOR_DETACHED":
          return "Sensor detached";
        case "NO_SENSOR":
          return "No Sensor";
        case "DISABLED":
          return "Disabled";
        case "ON_STANDBY":
          return "On Standby";
      }
    },
    closeWarning() {
      this.isShowDisableWarning = false;
    },
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
    showMachineDetails: function (machineId) {
      let child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetailsById(machineId);
      }
    },
    closeWorkorderDropdown() {
      this.showWorkOrderDropdown = false;
    },
    showCreateWorkorder(item, type) {
      console.log("item", item, type);
      this.selectedWorkOrderType = type;
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    toggleUpDepreciationPopup(toolingId) {
      console.log("toolingId", toolingId);

      if (!this.showUpDepreciationPopup) {
        setTimeout(() => {
          this.showUpDepreciationPopup = true;
          this.selectedToolingId = toolingId;
        }, 200);
        this.showSlDepreciationPopup = false;
      } else {
        this.showUpDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    toggleSlDepreciationPopup(toolingId) {
      if (!this.showSlDepreciationPopup) {
        setTimeout(() => {
          this.selectedToolingId = toolingId;
          this.showSlDepreciationPopup = true;
        }, 200);
        this.showUpDepreciationPopup = false;
      } else {
        this.showSlDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    getDepreciationPercentage(numerator, denominator) {
      return Math.min(100, Math.round((numerator / denominator) * 1000) / 10);
    },
    removeFromTab() {
      const params = {
        id: this.currentActiveTab.id,
        name: this.currentActiveTab.name,
        isDuplicate: false,
        removeItemIdList: this.listChecked,
        addItemIdList: [],
      };
      console.log(params, "params");
      axios.post("/api/tab-table/save-tab-table-mold", params).then(() => {
        this.createSuccess(params, {
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      });
    },
    openCustomizationModal() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "customization-modal");
      if (child != null) {
        child.openModal();
      }
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
    truncateText(text, size) {
      text = text && text.trim();
      if (text && text.length > size) {
        text = text.slice(0, size) + "...";
      }
      return text;
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
    closeToastAlert() {
      this.toastAlert.value = false;
    },
    createTabView() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("CREATE");
      }
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
    async getTabsByCurrentUser(isCallAtMounted) {
      await axios
        .get("/api/tab-table/by-current-user?objectType=TOOLING")
        .then((res) => {
          this.listSelectedTabs = res.data.data;
          this.currentActiveTab = this.listSelectedTabs[0];
          if (isCallAtMounted) {
            this.setNewActiveTab();
          }
        });
    },
    setNewActiveTab() {
      const listShowed = this.listSelectedTabs.filter((t) => t.show);
      if (listShowed.length) {
        this.tab(listShowed[0]);
      }
    },
    isTabShowed(tabName) {
      return this.listSelectedTabs.includes(tabName);
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
    checkStatusParam(status) {
      return (
        this.requestParam.status === status ||
        this.requestParam.tabType === status
      );
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    toggleUpDepreciationPopup(toolingId) {
      console.log("toolingId", toolingId);

      if (!this.showUpDepreciationPopup) {
        setTimeout(() => {
          this.showUpDepreciationPopup = true;
          this.selectedToolingId = toolingId;
        }, 200);
        this.showSlDepreciationPopup = false;
      } else {
        this.showUpDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    toggleSlDepreciationPopup(toolingId) {
      if (!this.showSlDepreciationPopup) {
        setTimeout(() => {
          this.selectedToolingId = toolingId;
          this.showSlDepreciationPopup = true;
        }, 200);
        this.showUpDepreciationPopup = false;
      } else {
        this.showSlDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    getDepreciationPercentage(numerator, denominator) {
      return Math.min(100, Math.round((numerator / denominator) * 1000) / 10);
    },
    getTimeData(timeData) {
      if (!timeData) return "";
      let time = this.formatToDateTime(timeData);
      if (time) {
        return time?.slice(10, 16);
      } else return "";
    },
    onItemSelect(item) {
      if (item.title === this.listOfItems[0].title) {
        Common.onChangeHref("/admin/molds/new");
      } else if (item.title === this.listOfItems[1].title) {
        Common.onChangeHref("/admin/molds/import");
      } else if (item.title === this.listOfItems[2].title) {
        this.showDrawer();
      }
    },
    handleShowPartPicker() {
      this.showPartPicker = true;
    },
    handleClosePartPicker() {
      this.showPartPicker = false;
    },
    handleToggle() {
      this.showFilter = false;
      if (!this.showPartPicker) {
        this.handleShowPartPicker();
      } else {
        this.handleClosePartPicker();
      }
      this.displayCaret = true;
      if (!this.showPartPicker) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
    },
    handleCheckStatus(items) {
      console.log("handleCheckStatus", items);
      this.showFilter = false;
      let opStatus = [];
      let eqStatus = [];
      let isAllEqStatus = false;
      let isAllOpStatus = false;
      items.forEach((item) => {
        if (
          item.title === this.ListFilterItems[0].title &&
          item.childrens.length
        ) {
          item.childrens.forEach((children) => {
            if (children.title === "All") {
              isAllOpStatus = true;
            } else {
              opStatus.push(children.code);
            }
          });
        }
      });
      if (isAllOpStatus) {
        opStatus = [];
      }
      this.requestParam.moldStatusList = opStatus;

      items.forEach((item) => {
        if (
          item.title == this.ListFilterItems[1].title &&
          item.childrens.length > 0
        ) {
          item.childrens.forEach((children) => {
            if (children.title == "All") {
              isAllEqStatus = true;
            } else {
              eqStatus.push(children.code);
            }
          });
        }
      });
      if (isAllEqStatus) {
        eqStatus = [];
      }

      this.requestParam.counterStatusList = eqStatus;
      const currentStatusListLength =
        this.requestParam.moldStatusList.length +
        this.requestParam.counterStatusList.length;
      if (this.defaultStatusListLength > currentStatusListLength) {
        this.isFilterChanging = true;
      } else {
        this.isFilterChanging = false;
      }
    },
    checkedItems(items) {
      this.handleCheckStatus(items);

      this.search();
    },
    resetHandler(items) {
      console.log("resetHandler", items);
      this.showFilter = false;
      this.requestParam.moldStatusList = [
        "ON_STANDBY",
        "IN_PRODUCTION",
        "IDLE",
        "NOT_WORKING",
        "SENSOR_OFFLINE",
        "SENSOR_DETACHED",
        "NO_SENSOR",
      ];
      this.requestParam.counterStatusList = [
        "INSTALLED",
        "NOT_INSTALLED",
        "DETACHED",
      ];
      this.isFilterChanging = false;
      this.search();
    },
    handleCloseFilter() {
      this.showFilter = false;
      this.search();
    },
    handleToggleFilter() {
      this.showPartPicker = false;
      if (!this.showFilter) {
        this.handleShowFilter();
      } else {
        this.handleCloseFilter();
      }
    },
    handleShowFilter() {
      this.showFilter = true;
    },
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
    },
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.visiblePopover) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
    },
    featureNotifyNext() {
      Common.triggerShowNewFeatureNotify(
        this.$children,
        "action-bar-feature",
        null,
        { key: "feature", value: "DATA_TABLE_TAB" }
      );
    },
    featureNotifyDone(feature) {
      this.featureList = this.featureList.filter((f) => f !== feature);
      if (this.featureList.length > 0) {
        Common.triggerShowNewFeatureNotify(
          this.$children,
          "action-bar-feature",
          null,
          { key: "feature", value: this.featureList[0] }
        );
      }
    },
    updateVariable(data) {
      this.lastShotData = data;
    },
    closeLastShot() {
      if (this.lastShotDropdown) {
        console.log(this.lastShotData, "closeLastShot");
        this.requestParam.accumulatedShotFilter =
          (this.lastShotData && this.lastShotData.filter) || "";
        if (this.lastShotData && this.lastShotData.sort) {
          this.currentSortType = "lastShot";
          this.isDesc = this.lastShotData.sort === "desc";
          this.requestParam.sort = `${
            this.lastShotData.filter === null
              ? "lastShot"
              : `accumulatedShot.${this.lastShotData.filter}`
          },${this.isDesc ? "desc" : "asc"}`;
        }

        this.sort();
        this.lastShotDropdown = false;
      }
    },
    enableList(items) {
      items.forEach((item) => {
        item.enabled = true;
      });
      this.saveBatch(items, true);
    },
    showDisableWarning() {
      console.log("showDisableWarning", this.isShowDisableWarning);
      if (this.listCheckedFull.some((item) => item.counterId)) {
        this.isShowDisableWarning = true;
      } else {
        // TODO: handle case select all items
        this.disableList(this.listCheckedFull);
      }
    },
    disableList(items) {
      this.closeWarning();
      items.forEach((item) => {
        item.enabled = false;
      });
      this.saveBatch(items, false);
    },
    saveBatch(item, boolean) {
      console.log(item, "user");
      const idArr = item.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      axios.post("/api/molds/change-status-in-batch", param).then(
        (response) => {
          this.paging(1);
          this.getTabsByCurrentUser();
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        },
        (error) => {
          console.log(error.response);
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        },
        () => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        }
      );
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
            console.log($vm.isCallingDone);
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
    downloadToolingDynamic(type, timeWaiting) {
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;

      const isTotal = this.listAllIds.length === this.total;
      const listChecked = isTotal ? this.listAllIds : this.listChecked;

      let downloadParams = listChecked.join(",");

      if (type === EXPORT_TYPE.PDF) {
        path = "exportPdfDetailMolds";
      } else {
        path = "exportExcel";
      }
      setTimeout(() => {
        this.isStopedExport = false;
        this.isCallingDone = false;
        $vm.closeExport();

        document.getElementById("toggleExportProgressBar").click();
        this.progressBarRun();
      }, Common.getNumMillisecondsToTime(timeWaiting));
      let $vm = this;
      let sort = this.requestParam.sort
        ? `&sort=${this.requestParam.sort}`
        : ``;
      let params = null;
      if (["CUSTOM_RANGE", "DAILY"].includes(this.dataExport.rangeType)) {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ? `${this.requestParam.sort}` : ``,
          rangeType:
            this.dataExport.rangeType === "DAILY"
              ? ""
              : this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          fromDate: this.dataExport.startDate,
          toDate: this.dataExport.endDate,
        };
      } else {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ? `${this.requestParam.sort}` : ``,
          rangeType: this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          time: this.dataExport.time,
          // EVERY_SHOT
          // fromData toDate
        };
      }

      const params2 = Common.param(params);
      let url = `/api/molds/exportExcelDynamicNew?` + params2;

      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          setTimeout(() => {
            if (!$vm.isStopedExport) {
              $vm.isCallingDone = true;
              // $vm.closeExport()
              setTimeout(() => {
                let headerLine = response.headers["content-disposition"];
                console.log("res:", headerLine);
                if (headerLine != null)
                  fileName =
                    headerLine.split("filename=")[1] || fileName + ".xlsx";

                var blob = new Blob([response.data], {
                  type: "text/plain;charset=utf-8",
                });
                saveAs(blob, `${fileName}`);
              }, 2000);
            }
          }, Common.getNumMillisecondsToTime(timeWaiting) + 10);
        },
        (error) => {
          setTimeout(() => {
            console.log(error.response);
            $vm.isStopedExport = true;
            // $vm.closeExport();
            error.response.data.text().then((text) => Common.alert(text));
          }, Common.getNumMillisecondsToTime(timeWaiting) + 1000);
        }
      );
    },
    exportDynamic(data, timeWaiting) {
      console.log("@exportDynamic", { data, timeWaiting });
      this.dataExport = data;
      this.downloadToolingDynamic(EXPORT_TYPE.XLSX, timeWaiting);
    },
    openExportModal() {
      this.showExportModal = true;
    },
    closeExport() {
      const root = document.getElementById("export-data-modal");
      const modalBody = root.querySelectorAll(".ant-modal-content");
      if (modalBody) {
        const el = modalBody[0];
        el.classList.add("fade-out-animate");
        setTimeout(() => {
          el.classList.add("hidden");
          setTimeout(() => {
            const btn = document.getElementById("btn-export");
            console.log("btn classlist", btn && btn.classList);
            if (btn && btn.classList && btn.classList.contains("disable-cta")) {
              btn.classList.remove("disable-cta");
            }
            this.showExportModal = false;
            setTimeout(() => {
              el.classList.remove("fade-out-animate");
            }, 50);
          }, 200);
        }, 250);
      }
    },
    closeShowExport() {
      this.showExport = false;
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/molds/${this.listChecked[0]}`;
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      const listInstalled = this.listCheckedFull
        .filter((item) => item.equipmentStatus !== "INSTALLED")
        .map((item) => {
          return item.id;
        });
      if (listInstalled.length >= 1) {
        axios
          .post(`/api/molds/delete-in-batch`, listInstalled)
          .then((res) => {
            $("#op-delete-popup").modal("hide");
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      } else {
        $("#op-delete-popup").modal("hide");
        this.paging(1);
      }
    },
    showDeletePopup() {
      if (!localStorage.getItem("dontShowWarning")) {
        var child = Common.vue.getChild(this.$children, "delete-popup");
        if (child != null) {
          child.showDeletePopup();
        }
      } else {
        axios
          .post(`/api/molds/delete-in-batch`, this.listChecked)
          .then((res) => {
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    animationPrimary() {
      $(".animationPrimary").click(() => {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(() => {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("outline-animation");
          }
        );
      });
    },

    deleteItem(result) {
      console.log(result, "result");
      axios.delete(`/api/molds/delete/${result.id}`).then((res) => {
        console.log(res, "resss");
        this.paging(this.requestParam.page);
      });
    },
    restoreItem() {
      axios.post(`/api/molds/restore/${this.listChecked[0]}`).then((res) => {
        console.log(res, "resss");
        this.paging(this.requestParam.page);
      });
    },
    changeDropdown() {
      this.dropdownOpening = false;
    },
    openDropdown() {
      if (this.dropdownOpening === true) {
        this.dropdownOpening = false;
      } else {
        this.dropdownOpening = true;
      }
    },
    showDrawer() {
      // this.resetDrawer()
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
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
    handleResetColumnsListSelected() {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    async setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "equipmentCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["part"],
          field: "part",
          mandatory: true,
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "location.company.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["location"],
          field: "location",
          sortable: true,
          sortField: "location.name",
        },
        {
          label: this.resources["accumulated_shots"],
          field: "lastShot",
          default: true,
          sortable: true,
        },
        {
          label: this.resources["latest_cycle_time"],
          field: "cycleTime",
          sortable: true,
          sortField: "lastCycleTime",
        },
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
        // { label: this.resources['op'], field: 'op', default: true, sortable: true, sortField: 'operatingStatus', defaultSelected: true, defaultPosition: 5 },
        // { label: this.resources['status'], field: 'equipmentStatus', default: true, sortable: true, defaultSelected: true, defaultPosition: 6 },
        {
          label: this.resources["cycle_time_tolerance_l1"],
          field: "cycleTimeToleranceL1",
          sortable: true,
          sortField: "cycleTimeLimit1",
        },
        {
          label: this.resources["cycle_time_tolerance_l2"],
          field: "cycleTimeToleranceL2",
          sortable: true,
          sortField: "cycleTimeLimit2",
        },
        {
          label: this.resources["weighted_average_cycle_time"],
          field: "weightedAverageCycleTime",
          sortable: true,
          sortField: "weightedAverageCycleTime",
        },
        {
          label: this.resources["uptime_target"],
          field: "uptimeTarget",
          sortable: true,
        },
        {
          label: this.resources["engineer_in_charge"],
          field: "engineerInCharge",
          sortable: true,
          sortField: "engineer",
        },
        {
          label: this.resources["weight_of_runner_system_o"],
          field: "weightOfRunnerSystem",
          sortable: true,
          sortField: "weightRunner",
        },
        {
          label: this.resources["supplier_approved_cycle_time"],
          field: "contractedCycleTimeSeconds",
          sortable: true,
          sortField: "contractedCycleTime",
        },
        {
          label: this.resources["toolmaker_approved_cycle_time"],
          field: "toolmakerContractedCycleTimeSeconds",
          sortable: true,
          sortField: "toolmakerContractedCycleTime",
        },
        {
          label: this.resources["counter_id"],
          field: "counterCode",
          sortable: true,
          sortField: "counter.equipmentCode",
        },
        {
          label: this.resources["reset.forecastedMaxShots"],
          field: "designedShot",
          sortable: true,
        },
        {
          label: this.resources["hot_runner_number_of_drop"],
          field: "hotRunnerDrop",
          sortable: true,
        },
        {
          label: this.resources["hot_runner_zone"],
          field: "hotRunnerZone",
          sortable: true,
        },
        {
          label: this.resources["front_injection"],
          field: "injectionMachineId",
          sortable: true,
        },
        {
          label: this.resources["required_labor"],
          field: "labour",
          sortable: true,
        },
        {
          label: this.resources["last_date_of_shots"],
          field: "lastShotDate",
          sortable: true,
          sortField: "lastShotAt",
        },
        {
          label: this.resources["machine_ton"],
          field: "quotedMachineTonnage",
          sortable: true,
        },
        {
          label: this.resources["maintenance_interval"],
          field: "preventCycle",
          sortable: true,
        },
        {
          label: this.resources["maker_of_runner_system"],
          field: "runnerMaker",
          sortable: true,
        },
        {
          label: this.resources["maximum_capacity_per_week"],
          field: "maxCapacityPerWeek",
          sortable: true,
        },
        // { label: this.resources['overdue_maintenance_tolerance'], field: 'preventOverdue', sortable: true },
        {
          label: this.resources["production_hour_per_day"],
          field: "shiftsPerDay",
          sortable: true,
        },
        {
          label: this.resources["production_day_per_week"],
          field: "productionDays",
          sortable: true,
        },
        {
          label: this.resources["shot_weight"],
          field: "shotSize",
          sortable: true,
        },
        {
          label: this.resources["supplier"],
          field: "supplierCompanyName",
          sortable: true,
          sortField: "supplier.name",
        },
        {
          label: this.resources["tool_description"],
          field: "toolDescription",
          sortable: true,
        },
        {
          label: this.resources["front_tool_size"],
          field: "toolingSizeView",
          sortable: true,
          sortField: "size",
        },
        {
          label: this.resources["tool_weight"],
          field: "toolingWeightView",
          sortable: true,
          sortField: "weight",
        },
        {
          label: this.resources["tooling_complexity"],
          field: "toolingComplexity",
          sortable: true,
        },
        {
          label: this.resources["tooling_letter"],
          field: "toolingLetter",
          sortable: true,
        },
        {
          label: this.resources["tooling_type"],
          field: "toolingType",
          sortable: true,
        },
        {
          label: this.resources["toolmaker"],
          field: "toolMakerCompanyName",
          sortable: true,
          sortField: "toolMaker.name",
        },
        {
          label: this.resources["type_of_runner_system"],
          field: "runnerTypeTitle",
          sortable: true,
          sortField: "runnerType",
        },
        {
          label: this.resources["upcoming_maintenance_tolerance"],
          field: "preventUpcoming",
          sortable: true,
        },
        {
          label: this.resources["uptime_tolerance_l1"],
          field: "uptimeLimitL1",
          sortable: true,
        },
        {
          label: this.resources["uptime_tolerance_l2"],
          field: "uptimeLimitL2",
          sortable: true,
        },
        {
          label: this.resources["utilization_rate"],
          field: "utilizationRate",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["year_of_tool_made"],
          field: "madeYear",
          sortable: true,
        },
        {
          label: this.resources["inactive_period"],
          field: "inactivePeriod",
          sortable: true,
          sortField: "inactivePeriod",
        },
        {
          label: this.resources["product_and_category"],
          field: "productAndCategory",
          sortable: true,
          sortField: "productAndCategory",
        },
        {
          label: this.resources["memo"],
          field: "memo",
          sortable: true,
          sortField: "memo",
        },
        {
          label: this.resources["cost_tooling"],
          field: "costTooling",
          sortable: true,
          sortField: "cost",
        },
        {
          label: this.resources["accumulated_maintenance_cost"],
          field: "accumulatedMaintenanceCost",
          sortable: true,
          sortField: "accumulatedMaintenanceCost",
        },
        {
          label: this.resources["last_maintenance_date"],
          field: "lastMaintenanceDate",
          sortable: true,
          sortField: "lastMaintenanceDate",
        },
        {
          label: this.resources["last_refurbishment_date"],
          field: "lastRefurbishmentDate",
          sortable: true,
          sortField: "lastRefurbishmentDate",
        },
        {
          label: this.resources["upper_tier_company"],
          field: "upperTierCompanies",
          sortable: true,
          sortField: "upperTierCompanies",
        },
        {
          label: this.resources["sl_depreciation"] || "S.L. Depreciation",
          field: "slDepreciation",
          sortable: true,
          sortField: "slDepreciation",
        },
        {
          label: this.resources["up_depreciation"] || "U.P. Depreciation",
          field: "upDepreciation",
          sortable: true,
          sortField: "upDepreciation",
        },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "machine.machineCode",
        },
      ];

      try {
        const condition = Boolean(
          this.options?.CLIENT?.moldList?.cols?.dataSubmission?.show
        );
        if (condition) {
          this.allColumnList.push({
            label: this.resources["data_approval"],
            field: "dataSubmission",
            default: true,
            sortable: true,
            sortField: "dataSubmission",
          });
        }
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
      this.$forceUpdate();
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally(() => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    getColumnListSelected() {
      axios
        .get(`/api/config/column-config?pageType=${this.pageType}`)
        .then(
          (response) => {
            if (response.data.length) {
              let hashedByColumnName = {};
              response.data.forEach((item) => {
                hashedByColumnName[item.columnName] = item;
              });
              this.allColumnList.forEach((item) => {
                if (hashedByColumnName[item.field]) {
                  // #930 require to show the selective columns for cost
                  item.enabled = hashedByColumnName[item.field].enabled;
                  item.id = hashedByColumnName[item.field].id;
                  item.position = hashedByColumnName[item.field].position;
                }
              });
              this.allColumnList.sort((a, b) => {
                return a.position - b.position;
              });
              // #930 sorting the selective order
              if (this.cost) {
                let toolingCost = 0;
                let acc = 0;
                this.allColumnList.forEach((item, index) => {
                  if (item.enabled) {
                    count = item.position;
                  }
                  if (item.label === this.resources["cost_tooling"]) {
                    toolingCost = index;
                  }
                  if (
                    item.label ===
                    this.resources["accumulated_maintenance_cost"]
                  ) {
                    acc = index;
                  }
                });

                this.allColumnList[toolingCost].enabled = true;
                this.allColumnList[toolingCost].position = count + 1;
                this.allColumnList[acc].enabled = true;
                this.allColumnList[acc].position = count + 2;

                this.allColumnList.sort((a, b) => {
                  return a.position - b.position;
                });
              }
              this.$forceUpdate();
              let child = Common.vue.getChild(this.$children, "show-columns");
              if (child != null) {
                child.$forceUpdate();
              }
            }
          },
          (error) => {
            // Common.alert(error.response.data);
          }
        )
        .finally(() => {
          this.changeToDisplayTempColumn();
        });
    },
    handleChangeValueCheckBox(value) {
      this.allColumnList.forEach((item) => {
        if (item.field === value) {
          item.enabled = !item.enabled;
        }
      });
      this.saveSelectedList();
      this.$forceUpdate();
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
            this.changeToDisplayTempColumn();
            this.$forceUpdate();
          }
        );
    },
    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    showLocationHistory(mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
    },
    handleCycleTimeTolerance1(result) {
      if (!result.cycleTimeLimit1) {
        return "";
      } else {
        if (result.cycleTimeLimit1Unit === "SECOND") {
          return "seconds";
        }
        if (result.cycleTimeLimit1Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
    },

    handleCycleTimeTolerance2(result) {
      if (!result.cycleTimeLimit2) {
        return "";
      } else {
        if (result.cycleTimeLimit2Unit === "SECOND") {
          return "seconds";
        }
        if (result.cycleTimeLimit2Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
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
    showRevisionHistory(id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MOLD");
      }
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
    },
    tab(tab) {
      this.currentActiveTab = tab;
      console.log("tab", this.currentActiveTab, this.listSelectedTabs);
      this.tabStatus = tab.name;
      this.requestParam.tabType = "";
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      if (tab.name === "DELETED") {
        this.requestParam.equipmentStatus = "ALL";
        this.requestParam.status = tab.name;
        this.requestParam.deleted = true;
        this.paging(1);
        this.isactive = false;
      } else {
        this.requestParam.equipmentStatus = "ALL";
        this.currentSortType = "lastShotAt";
        this.requestParam.sort = "lastShotAt,desc";
        this.requestParam.status = tab.name;
        delete this.requestParam.deleted;
        if (["DIGITAL", "NON_DIGITAL"].includes(tab.name)) {
          this.requestParam.status = "";
          this.requestParam.tabType = tab.name;
        }
        this.isactive = false;
        this.paging(1);
      }
      this.changeShow(null);
    },
    search(page) {
      this.paging(1);
    },

    changeEquipmentStatus(data, status) {
      var param = {
        id: data.id,
        equipmentStatus: status,
      };
      axios
        .put(Page.API_BASE + "/" + param.id + "/equipment-status", param)
        .then(
          (response) => {
            this.paging(1);
          },
          (error) => {
            console.log(error.response);
          }
        );
    },
    showChart: function (mold, type = null, dateViewType = null, tab, oct) {
      if (oct === undefined || oct === null) {
        oct = this.octs[this.optimalCycleTime.strategy];
      }

      if (!type) {
        type = "QUANTITY";
      }
      if (!dateViewType) {
        dateViewType = "DAY";
      }

      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.showChart(
          mold,
          type,
          dateViewType,
          tab ? tab : "switch-graph",
          oct
        );
      } else {
        setTimeout(() => {
          this.showChart(
            mold,
            type,
            dateViewType,
            tab ? tab : "switch-graph",
            oct
          );
        }, 200);
        console.log("cannot found chart mold element");
      }
    },
    showPartChart(part, mold) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$children, "chart-part");
      if (child != null) {
        child.showChartPartMold(part, mold, "QUANTITY", "DAY");
      }
    },
    showProjectDetail(result) {
      const child = Common.vue.getChild(
        this.$children,
        "category-library-modal"
      );
      const firstStep = "project-profile";
      if (child != null) {
        const date = { ...this.currentDate };
        child.showModal(result, date, firstStep);
      }
    },
    downloadTooling(type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;
      let path = "";

      const isTotal = this.listAllIds.length === this.total;
      const listChecked = isTotal ? this.listAllIds : this.listChecked;

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
      }/${path}?ids=${downloadParams}&fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
      if (this.listChecked.length === 0) {
        url = `${
          Page.API_BASE
        }/${path}?fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
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
    paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      this.requestParam.id = objectId;

      let requestParamConvert = _.cloneDeep(this.requestParam);

      if (
        this.requestParam.equipmentStatuses.length <= 0 &&
        this.requestParam.operatingStatuses.length != 0
      ) {
        delete requestParamConvert.equipmentStatuses;
        requestParamConvert.equipmentStatus = "";
      } else if (
        this.requestParam.equipmentStatuses.length != 0 &&
        this.requestParam.operatingStatuses.length <= 0
      ) {
        requestParamConvert.operatingStatus = "";
        delete requestParamConvert.operatingStatuses;
      } else if (
        this.requestParam.equipmentStatuses.length <= 0 &&
        this.requestParam.operatingStatuses.length <= 0
      ) {
        delete requestParamConvert.operatingStatuses;
        delete requestParamConvert.equipmentStatuses;
      } else {
        requestParamConvert = this.requestParam;
      }
      if (this.requestParam.operatingStatus === "ALL") {
        requestParamConvert.operatingStatus = "";
      }

      if (this.requestParam.equipmentStatus === "ALL") {
        requestParamConvert.equipmentStatus = "";
      }
      requestParamConvert.isToolingScreen = true;

      var param = Common.param(requestParamConvert);

      const {
        firstCompare,
        secondCompare,
        thirdCompare,
        firstCaviti,
        secondCaviti,
        counterId,
        partId,
        companyId,
      } = this;
      let queryParams = "";
      if (counterId || partId) {
        queryParams = `&where=counter${counterId},part${partId}`;
      } else {
        queryParams = `&where=op${secondCompare},${firstCompare}${firstCaviti},${thirdCompare}${secondCaviti}`;
      }
      param = param + queryParams;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();
      axios
        .get(
          Page.API_BASE +
            `?tabId=${
              !this.currentActiveTab.id ? "" : this.currentActiveTab.id
            }` +
            "&" +
            param,
          {
            cancelToken: this.cancelToken.token,
          }
        )
        .then(
          (response) => {
            this.total = response.data.totalElements;
            console.log(
              "paging find tab",
              this.currentActiveTab,
              this.listSelectedTabs
            );
            const currentTabIndex = this.listSelectedTabs.findIndex(
              (item) => item.id === this.currentActiveTab.id
            );
            console.log("currentTabIndex", currentTabIndex);
            if (currentTabIndex >= 0) {
              this.listSelectedTabs[currentTabIndex].totalItem =
                response.data.totalElements;
              console.log("currentTab", this.listSelectedTabs[currentTabIndex]);
            }
            this.results = response.data.content;
            if (this.requestParam.status === "All") {
              this.allToolingList = this.results;
            }
            this.results.forEach((item) => {
              item.familyTool = false;
            });

            if (objectId != "") {
              let searchParam = this.results.filter(
                (item) => parseInt(item.id) === parseInt(objectId)
              )[0];
              this.requestParam.query = searchParam.equipmentCode;
              window.history.replaceState(null, null, "/admin/molds");
            }

            if (this.total === this.listAllIds.length) {
              // refresh checklist in case select all items
              this.listChecked = [];
              this.listCheckedFull = [];
            }

            this.pagination = Common.getPagingData(response.data);
            // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
            this.setResultObject();

            // 카테고리 정보 저장.
            this.setCategories();

            Common.handleNoResults("#app", this.results.length);

            var pageInfo =
              this.requestParam.page == 1
                ? ""
                : "?page=" + this.requestParam.page;
            history.pushState(null, null, Common.$uri.pathname + pageInfo);
            // open chart when from dashboard
            if (this.optionParams.fromPage === "DASHBOARD") {
              this.showChart(
                this.results[0],
                "CYCLE_TIME",
                this.optionParams.dateViewType,
                null,
                this.octs[this.optimalCycleTime.strategy]
              );
            }
            if (this.results.length > 0) {
              Common.triggerShowNewFeatureNotify(
                this.$children,
                "action-bar-feature",
                null,
                { key: "feature", value: "DATA_TABLE_TAB" }
              );
            } else {
              Common.triggerShowNewFeatureNotify(
                this.$children,
                "action-bar-feature",
                null,
                { key: "feature", value: "TOOLING_TAB" }
              );
            }
            if (this.requestParam.isMapRedirect) {
              this.requestParam.isMapRedirect = false;
              this.requestParam.locationName = "";
              this.requestParam.operatingStatus = "";
            }
          },
          (error) => {
            console.log(error.response);
          }
        );
    },
    setResultObject() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i] !== "object") {
          var moldId = this.results[i];
          this.results[i] = this.findMold(this.results, moldId);
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
          break;
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
    setCategories() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (
            typeof this.results[i].part.category !== "object" &&
            this.results[i].part.categoryId > 0
          ) {
            var categoryId = this.results[i].part.categoryId;
            this.results[i].part.category = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }

      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (typeof this.results[i].part.category.parent !== "object") {
            var categoryId = this.results[i].part.category.parent;
            this.results[i].part.category.parent = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }
    },
    findCategory(results, categoryId) {
      for (var i = 0; i < results.length; i++) {
        var mold = results[i];
        if (
          mold.part == null ||
          mold.part.category == null ||
          typeof mold.part.category !== "object"
        ) {
          continue;
        }

        var category = mold.part.category;
        if (categoryId == category.id) {
          return category;
          break;
        }

        if (category.parent != null && categoryId == category.parent.id) {
          return category.parent;
          break;
        }

        // 부모의 자삭에서 찾기
        if (category.parent != null && category.parent.children != null) {
          for (var j = 0; j < category.parent.children.length; j++) {
            var parentChild = category.parent.children[j];
            if (typeof parentChild !== "object") {
              continue;
            }
            if (categoryId == parentChild.id) {
              return parentChild;
              break;
            }
          }
        }

        // parts에서 검색
        if (mold.part.molds != null) {
          var findCategory = this.findCategory(mold.part.molds, categoryId);
          if (typeof findCategory === "object") {
            return findCategory;
            break;
          }
        }
      }
    },
    showMoldDetails(mold) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      console.log("child", child);
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        const tab = "switch-detail";
        if (
          mold.dataSubmission === "DISAPPROVED" ||
          mold.dataSubmission === "APPROVED"
        ) {
          let urlDataSubmission = `/api/molds/data-submission/${mold.id}/${
            mold.dataSubmission === "APPROVED"
              ? "approval-reason"
              : "disapproval-reason"
          }`;
          axios.get(urlDataSubmission).then((response) => {
            reasonData = response.data;
            // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
            reasonData.approvedAt = reasonData.approvedAt
              ? this.convertTimestampToDateWithFormat(
                  reasonData.approvedAt,
                  "MMMM DD YYYY HH:mm:ss"
                )
              : "-";
            mold.notificationStatus = mold.dataSubmission;
            mold.reason = reasonData.reason;
            mold.approvedAt = reasonData.approvedAt;
            mold.approvedBy = reasonData.approvedBy;
            // child.showMoldDetails(mold);
            this.showChart(mold, tab);
          });
        } else {
          // child.showMoldDetails(mold);
          this.showChart(mold, tab);
        }
      }
    },

    // 'failure' 선택 시 고장시간 등록 Modal
    showCorrective(mold) {
      var child = Common.vue.getChild(this.$children, "corrective-form");
      if (child != null) {
        child.showModal(mold);
      }
    },
    callbackCorrectiveForm(mold) {
      this.paging(1);
    },
    sortBy(type) {
      if (type === "lastShot") {
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
    showFilePreviewer(file, fromPopup) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file, fromPopup);
      }
    },
    backToMoldDetail(fromPopup) {
      console.log("back to molds detail");
      // var child = Common.vue.getChild(this.$children, 'mold-details');
      var child = Common.vue.getChild(
        this.$children,
        fromPopup || "chart-mold"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    changeToDisplayTempColumn() {
      if (this.dashboardChartType != "INACTIVE_TOOLINGS") {
        return;
      }
      console.log("change enabled inactivePeriod");

      let currentColumn = this.allColumnList.filter(
        (item) => item.enabled == true
      );
      let inactivePeriodField = this.allColumnList.find(
        (el) => el.field == "inactivePeriod"
      );
      if (!inactivePeriodField.enable) {
        inactivePeriodField.enableTemp = true;
        console.log("change 1");
        if (currentColumn.length > 9) {
          let currentColumnNotMandatory = currentColumn.filter(
            (item) => !item.mandatory
          );
          currentColumnNotMandatory.forEach((item) => {
            item.disableTemp = false;
          });
          if (currentColumnNotMandatory.length > 0)
            currentColumnNotMandatory[
              currentColumnNotMandatory.length - 1
            ].disableTemp = true;
        }
      }
    },

    initCurrentDate() {
      if (!this.currentDate) return;
      let today = new Date();
      this.currentDate.from = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay()
      );
      this.currentDate.to = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay() + 6
      );
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.currentDate.fromTitle.replaceAll(
        "-",
        ""
      );
    },

    openEditProductDemand(dataFromChild) {
      this.editFromChild = !!dataFromChild;
      const currentData = dataFromChild
        ? dataFromChild
        : this.listCheckedFull[0];
      var child = Common.vue.getChild(this.$children, "edit-product-demand");
      if (child != null) {
        child.showModal(currentData);
      }
    },

    loadSuccess() {
      this.paging(1);
      if (this.editFromChild) {
        this.triggerSaveModal++;
      }
    },
    SearchColumn() {
      let searchQuery = true;
      if (this.searchQuery) {
        console.log(
          allColumnList.filter((item) => {
            item.enableTemp ||
              (!item.disableTemp && item.enabled && !item.hiddenInToggle);
          })
        );
        return allColumnList.filter((item) => {
          item.enableTemp ||
            (!item.disableTemp && item.enabled && !item.hiddenInToggle);
        });
      }
    },
    async getCurrentUser() {
      await Common.getCurrentUser().then((data) => {
        this.currentUser = data;
      });
    },
    openManageTabView() {
      this.visibleMoreAction = false;
      let child = Common.vue.getChild(this.$children, "manage-tab-view");
      if (child != null) {
        child.show();
      }
    },
    closeMultiChart() {
      this.listChecked = [];
      this.listCheckedFull = [];
      this.showMultiChart = false;
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
          frameType: "TOOLING_SETTING",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + params
        );
        this.listAllIds = response.data.data;
        console.log("this.listAllIds", this.listAllIds);
      } catch (error) {
        console.log(error);
      }
    },
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
        this.listAllIds.length !== this.total &&
        this.listChecked.length >= 1 &&
        this.requestParam.status != "DELETED"
      );
    },
    enableShowMultiChart() {
      return this.listChecked.length >= 1 && this.listChecked.length <= 4;
    },
    isDigitalEnabled() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.digital?.enabled);
    },
    isNonDigitalEnabled() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.nonDigital?.enabled);
    },
    isDigitablActive() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.digital?.active);
    },
    isNonDigitalActive() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.nonDigital?.active);
    },
    isDataSubmissionEnabled() {
      return Boolean(
        this.options?.CLIENT?.moldList?.cols?.dataSubmission?.show
      );
    },
    isFilterApplied() {
      return this.isFilterChanging;
    },
    isOldGenIncluded() {
      const oldGen = ["CMS", "NCM"];
      const hasOldGenCounter = this.listCheckedFull.some((o) => {
        return (
          !o?.counterCode || oldGen.some((i) => o?.counterCode?.startsWith(i))
        );
      });
      return hasOldGenCounter;
    },
    listStatusAvailable() {
      const statusOfSelected = this.listCheckedFull[0].equipmentStatus;
      const isValid = (item) =>
        item.code !== "INSTALLED" &&
        statusOfSelected !== "FAILURE" &&
        item.code !== statusOfSelected;
      return this.codes.EquipmentStatus.filter(isValid);
    },
  },
  created() {
    // global watchers
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
        if (!newVal) return;
        this.userType = newVal;
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (!newVal) return;
        this.options = Object.assign({}, this.options, newVal);
        this.optimalCycleTime = this.options.OPTIMAL_CYCLE_TIME;
        const isToolingBarEnabled = Boolean(
          this.options?.CLIENT?.moldList?.features?.toolingBarEnabled
        );
        const isStatusEnabled = Boolean(
          this.options?.CLIENT?.moldDetail?.statusEnabled
        );
        this.featureList = !isToolingBarEnabled
          ? ["ACTION_BAR", "DATA_TABLE_TAB"]
          : ["TOOLING_TAB", "ACTION_BAR", "DATA_TABLE_TAB"];

        if (this.digitalactive) {
          this.requestParam.tabType = "DIGITAL";
          if (isStatusEnabled) {
            this.requestParam.status = "";
          }
          this.isactive = true;
        }

        // get OP config
        const { rules } = this.options.OP;
        const _inProductStatus = rules.filter(
          (item) => item.operatingStatus === "WORKING"
        )[0];
        const _idleStatus = rules.filter(
          (item) => item.operatingStatus === "IDLE"
        )[0];
        const _inActiveStatus = rules.filter(
          (item) => item.operatingStatus === "NOT_WORKING"
        )[0];
        const _disconnectStatus = rules.filter(
          (item) => item.operatingStatus === "DISCONNECTED"
        )[0];

        this.ruleOpStatus.inProduct =
          "There has been a shot made within the last " +
          _inProductStatus.time +
          this.getTimeUnit(_inProductStatus.timeUnit);
        this.ruleOpStatus.idle =
          "There has been a shot made after " +
          _inProductStatus.time +
          this.getTimeUnit(_inProductStatus.timeUnit) +
          " but within " +
          _idleStatus.time +
          this.getTimeUnit(_idleStatus.timeUnit);
        this.ruleOpStatus.inactive =
          "There is no shot made within " +
          _inActiveStatus.time +
          this.getTimeUnit(_inActiveStatus.timeUnit);
        this.ruleOpStatus.sensorOffline =
          "There is no signal from the sensor within " +
          _disconnectStatus.time +
          this.getTimeUnit(_disconnectStatus.timeUnit);
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (!newVal) return;
        this.codes = Object.assign({}, this.codes, newVal);
        console.log("this.codes", this.codes);
      },
      { immediate: true }
    );

    // capture query
    let params = Common.parseParams(URI.parse(location.href).query);
    if (params.dashboardChartType) {
      this.dashboardChartType = params.dashboardChartType;
    }
    if (params.tabbedDashboardRedirected) {
      this.requestParam.tabbedDashboardRedirected =
        params.tabbedDashboardRedirected;

      if (params.server) {
        if (!["dyson", "dyson2"].includes(params.server)) {
          this.tab(params.tab);
        } else {
          this.requestParam.tabType = params.tab;
          this.tab("All");
        }
      }

      if (params.dashboardChartType === "INACTIVE_TOOLINGS") {
        this.requestParam.inactiveLevel = params.inactiveLevel;
        this.tab(params.tab);
      }

      if (params.query) {
        this.requestParam.query = params.query;
        this.tab("All");
      }
    }

    this.initCurrentDate();
  },
  mounted() {
    this.defaultStatusListLength =
      this.requestParam.moldStatusList.length +
      this.requestParam.counterStatusList.length;
    // 모든 화면이 렌더링된 후 실행합니다.
    let params = new URLSearchParams(window.location.search);
    console.log("@mounted:params", params);
    this.digitalactive = params.get("active");
    let cost = params.get("cost");
    this.cost = cost;

    this.animationOutline();
    this.animationPrimary();

    if (params.get("dashboardRedirected") === "true") {
      this.requestParam.isMapRedirect = true;
      if (params.get("operatingStatus")) {
        this.requestParam.operatingStatus = params.get("operatingStatus");
      }

      if (params.get("locationName")) {
        this.requestParam.locationName = params.get("locationName");
      }
    }

    try {
      var partCode = Common.getParameter("partCode");
      var partId = Common.getParameter("partId");
      var compnayId = Common.getParameter("companyId");
      var op = Common.getParameter("op");
      var moldCode = Common.getParameter("moldCode");
      var equipmentStatus = Common.getParameter("equipmentStatus");
      this.optionParams.fromPage = Common.getParameter("from");
      this.optionParams.dateViewType = Common.getParameter("dateViewType");
      this.optionParams.fromPage = Common.getParameter("from");
      this.optionParams.dateViewType = Common.getParameter("dateViewType");
      var uri_map = [];

      var uri = URI.parse(location.href);
      if (uri.fragment && uri.fragment.includes("&")) {
        uri_map = uri.fragment
          .replace("%20", " ")
          .replace("%20", " ")
          .replace("%20", " ")
          .replace("%20", " ")
          .split("&");
      }

      if (uri.fragment == "active" || op == "active") {
        this.requestParam.operatingStatus = "WORKING";
      } else if (uri.fragment == "idle" || op == "idle") {
        this.requestParam.operatingStatus = "IDLE";
      } else if (uri.fragment == "inactive" || op == "inactive") {
        this.requestParam.operatingStatus = "NOT_WORKING";
      } else if (uri.fragment == "disconnected" || op == "disconnected") {
        this.requestParam.operatingStatus = "DISCONNECTED";
      } else if (uri_map[0] == "active") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "WORKING";
      } else if (uri_map[0] == "idle") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "IDLE";
      } else if (uri_map[0] == "disconnected") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "DISCONNECTED";
      } else if (uri_map[0] == "inactive") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "NOT_WORKING";
      }

      if (partCode != "undefined" && partCode != "") {
        this.requestParam.partCode = partCode;
      }
      if (partId != "undefined" && partId != "") {
        this.requestParam.partId = partId;
      }
      if (compnayId != "undefined" && compnayId != "") {
        this.requestParam.companyId = compnayId;
      }
      if (moldCode != "undefined" && moldCode != "") {
        this.requestParam.query = moldCode;
      }
      if (equipmentStatus) {
        this.requestParam.equipmentStatus = equipmentStatus;
      }
      const params = Common.parseParams(uri.query);

      if (params.inactiveLevel) {
        this.requestParam.inactiveLevel = params.inactiveLevel;
      }
      if (params.dashboardRedirected) {
        this.requestParam.dashboardRedirected = params.dashboardRedirected;
      }
      if (params.ids) {
        this.requestParam.ids = params.ids;
      }

      this.getTabsByCurrentUser(true);
    } catch (error) {
      console.log(error);
    }
  },
});
