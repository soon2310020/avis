axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

window.onload = () => {
  document.title = "Part" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

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

var Page = Common.getPage("parts");

var vm = new Vue({
  el: "#app",
  components: {
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "part-details": httpVueLoader("/components/part-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "part-filter": httpVueLoader("/components/part/part-filter.vue"),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    // "new-dropdown": httpVueLoader("/components/common/dropdown.vue"),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-part-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
  },
  data: {
    showFilter: false,
    dropDownType: "main",
    listOfItemsForCategory: [],
    listOfItemsForProduct: [],
    listOfItemsForFilter: [{ title: "By Category" }, { title: "By Product" }],
    isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
    deletePopup: false,
    showDrowdown: null,
    listOfItems: [
      { title: "Create Part" },
      { title: "Import Part(s)" },
      { title: "Create Part Property" },
    ],
    showPartPicker: false,
    resources: {},
    isLoading: false,
    optimalCycleTime: {},
    octs: {
      ACT: "Approved CT",
      WACT: "Weighted Average CT",
    },
    results: [],
    total: 0,
    dropdownData: null,
    pagination: [],
    requestParam: {
      accessType: "ADMIN_MENU",
      categoryId: "",
      query: "",
      status: "active",
      sort: "id,desc",
      page: 1,
      id: "",
      pageType: "PART_SETTING",
      tabId: "",
    },
    firstCompare: "eq",
    secondCompare: "and",
    thirdCompare: "eq",
    firstCaviti: "",
    secondCaviti: "",
    counterId: "",
    toolingId: "",
    categories: [],
    onAddPart: {
      type: Function,
      default: () => {
        console.log(this.dropdownData);
      },
    },
    sortType: {
      PART_ID: "partCode",
      NAME: "name",
      CATEGORY: "category.name",
      STATUS: "enabled",
    },
    redirectParams: {
      partId: "",
      timePeriod: "",
    },
    isCompareWithPrevious: false,
    currentSortType: "id",
    isDesc: true,
    // loadingAction: false,
    pageType: "PART_SETTING",
    allColumnList: [],
    visible: false,
    dropdownOpening: false,
    objectType: "PART",
    showDropdown: false,
    currentIntervalIds: [],
    isCallingDone: false,
    isStopedExport: false,
    isShowExportButton: false,
    initTimePeriod: null,
    initPeriodType: null,
    listChecked: [],
    listCheckedFull: [],
    configDeleteField: {},
    cancelToken: undefined,
    displayCaret: false,
    showAnimation: false,
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
    isFilterChanging: false,
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
        this.listChecked.length >= 1 && this.requestParam.status !== "disabled"
      );
    },
    enableBatchEnable() {
      return (
        this.listChecked.length >= 1 && this.requestParam.status === "disabled"
      );
    },
  },
  watch: {
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
    showFilter(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.dropDownType = "main";
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
        removeItemIdList:
          this.total === this.listAllIds.length
            ? this.listAllIds
            : this.listChecked,
        addItemIdList: [],
      };
      axios.post("/api/tab-table/save-tab-table-part", params).then(() => {
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
    openExtendedDropdown(item) {
      this.dropDownType = item.title;
    },
    onItemSelect(item) {
      if (item.title === this.listOfItems[0].title) {
        this.onChangeHref("/admin/parts/new");
      } else if (item.title === this.listOfItems[1].title) {
        this.onChangeHref("/admin/parts/import");
      } else if (item.title === this.listOfItems[2].title) {
        this.showDrawer();
      }
    },
    showMachineDetails: function (machineId) {
      let child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetailsById(machineId);
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
    },
    handleShowFilter() {
      this.showFilter = true;
    },
    handleToggleFilter() {
      this.showPartPicker = false;
      if (!this.showFilter) {
        this.handleShowFilter();
      } else {
        this.handleCloseFilter();
      }
    },
    handleCloseFilter() {
      this.showFilter = false;
    },
    handlerForFilter(item) {
      this.requestParam.categoryId = item.id;
      this.paging(1);
      this.showFilter = false;
      this.isFilterChanging = true;
      // console.log(item.title);
    },
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.showPartPicker) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
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
        fromPopup || "chart-part"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    timeFilter(timeParams, isCompareWithPrevious) {
      this.isCompareWithPrevious = isCompareWithPrevious;
      // remove old time params
      let removalFields = ["startDate", "endDate", "timePeriod"];
      removalFields.forEach((removalField) => {
        delete this.requestParam[removalField];
      });

      Object.keys(timeParams).forEach((key) => {
        this.requestParam[key] = timeParams[key];
      });
      this.paging(1);
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    deletePop(user) {
      if (user.length <= 1) {
        this.deleteLocation(user[0]);
      } else {
        this.deleteBatch(user, true);
      }
    },
    deleteBatch(user) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
      };
      axios.post("/api/parts/delete-in-batch", param).then(
        (response) => {
          if (response.data.success) {
            this.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error.response);
        },
        () => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        }
      );
    },
    deleteLocation(location) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: location.id,
      };
      axios.delete(Page.API_BASE + "/" + location.id, param).then(
        (response) => {
          if (response.data.success) {
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error.response);
        }
      );
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }

      const params = {
        ids: this.listChecked,
      };
      axios
        .post(`/api/parts/delete-in-batch`, params)
        .then((res) => {
          $("#op-delete-popup").modal("hide");
          this.paging(1);
        })
        .catch((e) => {
          console.log(e);
        });
    },
    showDeletePopup() {
      if (!localStorage.getItem("dontShowWarning")) {
        var child = Common.vue.getChild(this.$children, "delete-popup");
        if (child != null) {
          child.showDeletePopup();
        }
      } else {
        axios
          .post(`/api/parts/delete-in-batch`, this.listChecked)
          .then((res) => {
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
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
      window.location.href = `/admin/parts/${this.listChecked[0]}`;
    },
    onChangeHref(href) {
      setTimeout(() => {
        window.location.href = href;
      }, 700);
    },
    restoreItem() {
      axios
        .post(`/api/parts/restore/${this.listChecked[0]}`)
        .then((res) => {
          console.log(res, "resss");
          this.paging(this.requestParam.page);
        })
        .finally(() => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
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
    log(res) {
      console.log("res", res);
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
    setAllColumnList() {
      console.log("this.allColumnList", this.allColumnList);
      this.allColumnList = [
        {
          label: this.resources["part_id"],
          field: "partCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["part_name"],
          field: "name",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["category_name"],
          field: "category",
          default: true,
          sortable: true,
          sortField: "category.parent.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["status"],
          field: "enabled",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 3,
        },

        {
          label: this.resources["project_name"],
          field: "projectName",
          sortable: true,
          sortField: "category.name",
        },
        {
          label: this.resources["quantity_required"],
          field: "quantityRequired",
          sortable: true,
          sortField: "quantityRequired",
        },
        {
          label: this.resources["front_total_tool"],
          field: "totalMolds",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["front_active_tool"],
          field: "activeMolds",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["front_idle_tool"],
          field: "idleMolds",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["front_inactive_tool"],
          field: "inactiveMolds",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 7,
        },
        {
          label: this.resources["front_disconnected_tool"],
          field: "disconnectedMolds",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 8,
        },
        {
          label: this.resources["quantity_produced"],
          field: "totalProduced",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 9,
        },
        {
          label: this.resources["design_revision_level"],
          field: "designRevision",
          sortable: true,
        },
        {
          label: this.resources["part_resin_code"],
          field: "resinCode",
          sortable: true,
        },
        {
          label: this.resources["part_resin_grade"],
          field: "resinGrade",
          sortable: true,
        },
        {
          label: this.resources["part_volume"],
          field: "partSize",
          sortable: true,
          sortField: "size",
        },
        {
          label: this.resources["part_weight"],
          field: "partWeight",
          sortable: true,
          sortField: "weight",
        },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "mold.machine.machineCode",
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
    showDrawer() {
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

    handleResetColumnsListSelected() {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    getColumnListSelected() {
      axios.get(`/api/config/column-config?pageType=${this.pageType}`).then(
        (response) => {
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
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
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
      );
    },
    handleChangeValueCheckBox(value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
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
            this.$forceUpdate();
          }
        );
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
        child.showRevisionHistories(id, "PART");
      }
    },
    showChart(part, tab) {
      const child = Common.vue.getChild(this.$children, "chart-part");
      if (child != null) {
        child.showChart(
          part,
          "QUANTITY",
          "DAY",
          this.octs[this.optimalCycleTime.strategy],
          tab
        );
      } else {
        setTimeout(() => {
          this.showChart(
            part,
            "QUANTITY",
            "DAY",
            this.octs[this.optimalCycleTime.strategy],
            tab
          );
        }, 300);
        console.log("cannot found chart part element");
      }
    },
    showPartDetails(part) {
      var child = Common.vue.getChild(this.$children, "part-details");
      if (child != null) {
        child.showPartDetails(part);
      }
    },

    tab(tab) {
      this.currentActiveTab = tab;
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.requestParam.tabId = "";
      if (tab.name === "Disabled") {
        console.log("come1");
        this.requestParam.status = "disabled";
        // this.requestParam.deleted = true;
        this.paging(1);
      } else {
        console.log("come2");
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.requestParam.status = "active";
        delete this.requestParam.deleted;
        if (!tab.isDefaultTab) {
          this.requestParam.tabId = tab.id;
        }
        this.paging(1);
      }
      this.changeShow(null);
    },
    search(page) {
      this.paging(1);
    },

    deletePart(part) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: part.id,
      };

      axios.delete(Page.API_BASE + "/" + part.id, param).then(
        (response) => {
          if (response.data.success) {
            this.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error.response);
        }
      );
    },

    enable() {
      const listItems =
        this.listAllIds.length === this.total
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listItems, true);
    },
    disable() {
      const listItems =
        this.listAllIds.length === this.total
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listItems, false);
    },
    async saveBatch(idArr, boolean) {
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      try {
        await axios.post("/api/parts/change-status-in-batch", param);
        await this.getTabsByCurrentUser();
      } catch (error) {
        console.log(error.response);
      } finally {
        this.listChecked = [];
        this.listCheckedFull = [];
        this.showDropdown = false;
        this.paging(1);
      }
    },
    async paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      let objectId = Common.getParameter("objectId");
      this.requestParam.id = objectId;

      if (!this.requestParam.categoryId) {
        delete this.requestParam.categoryId;
      }
      var param = Common.param(this.requestParam);

      console.log(`param`, param);

      const {
        firstCompare,
        secondCompare,
        thirdCompare,
        firstCaviti,
        secondCaviti,
        counterId,
        toolingId,
      } = this;
      let queryParams = "";
      if (counterId || toolingId) {
        queryParams = `&where=counter${counterId},tool${toolingId}`;
      } else {
        queryParams = `&where=op${secondCompare},${firstCompare}${firstCaviti},${thirdCompare}${secondCaviti}`;
      }

      param = param + queryParams;

      this.isLoading = true;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();

      try {
        const response = await axios.get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        });
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
        if (objectId != "") {
          let searchParam = this.results.filter(
            (item) => parseInt(item.id) === parseInt(objectId)
          )[0];
          this.requestParam.query = searchParam.partCode;
          window.history.replaceState(null, null, "/admin/parts");
        }

        if (this.total === this.listAllIds.length) {
          // refresh checklist in case select all items
          this.listChecked = [];
          this.listCheckedFull = [];
        }

        Common.handleNoResults("#app", this.results.length);
        const pageInfo =
          this.requestParam.page == 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        console.log(error.response);
      } finally {
        this.isLoading = false;
      }
    },

    categoryLocation(categoryId) {
      var location = "";
      for (var i = 0; i < this.categories.length; i++) {
        var parent = this.categories[i];
        for (var j = 0; j < parent.children.length; j++) {
          var category = parent.children[j];

          if (category.id == categoryId) {
            var parentText =
              parent.enabled == false
                ? '<span class="disabled">' + parent.name + "</span>"
                : parent.name;
            var categoryText =
              category.enabled == false
                ? '<span class="disabled">' + category.name + "</span>"
                : category.name;

            return parentText + " > " + categoryText;
          }
        }
      }
      return "";
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
    exportPart() {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_part_data`;

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      let $vm = this;
      let downloadRequestParams = Object.assign({}, this.requestParam);

      const listChecked =
        this.listAllIds.length === this.total
          ? this.listAllIds
          : this.listChecked;
      downloadRequestParams.ids = listChecked.join(",");
      downloadRequestParams = Common.param(downloadRequestParams);
      let url = `${Page.API_BASE}/export-excel?${downloadRequestParams}`;

      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          if (!$vm.isStopedExport) {
            $vm.isCallingDone = true;
            setTimeout(() => {
              var blob = new Blob([response.data], {
                type: "text/plain;charset=utf-8",
              });
              saveAs(blob, `${fileName}.xlsx`);
            }, 2000);
          }
        },
        (error) => {
          console.log(error);
          $vm.isStopedExport = true;
        }
      );
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
          "/api/tab-table/by-current-user?objectType=PART"
        );
        this.listSelectedTabs = res.data.data;
        if (isCallAtMounted) {
          this.setNewActiveTab();
        }
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
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} parts on this current page are selected.`,
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
          title: `All ${this.total} parts are selected.`,
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
          frameType: "PART_SETTING",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + params
        );
        this.listAllIds = response.data.data;
      } catch (error) {
        console.log(error);
      }
    },
  },
  created() {
    // observe global state
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
      () => headerVm?.options,
      (newVal) => {
        if (!newVal) return;
        const options = newVal;
        this.optimalCycleTime = options.OPTIMAL_CYCLE_TIME;
      },
      { immediate: true }
    );

    // capture query
    var uri = URI.parse(location.href);
    var url = new URL(location.href);
    var timePeriod = url.searchParams.get("time-period");
    let periodType = url.searchParams.get("period-type");
    let partId = url.searchParams.get("part-id");
    let toolingId = url.searchParams.get("tooling-id");

    this.initTimePeriod = timePeriod;
    this.initPeriodType = periodType;
    let params = Common.parseParams(uri.query);

    console.log({ url, params, uri });

    if (timePeriod !== null && timePeriod !== "" && periodType !== null) {
      this.redirectParams.partId = partId;

      if (partId !== null && partId) {
        this.requestParam.query = partId;
      }

      if (toolingId) {
        this.toolingId = toolingId;
      }
    }

    if (uri.fragment) {
      this.requestParam.query = unescape(uri.fragment);
    }

    if (params.tabbedDashboardRedirected) {
      this.requestParam.tabbedDashboardRedirected =
        params.tabbedDashboardRedirected;
      if (params.categoryId) {
        this.requestParam.categoryId = params.categoryId;
      } else {
        delete this.requestParam.categoryId;
        this.requestParam.noProjectAssigned = true;
      }
    }

    if (params.dashboardRedirected) {
      this.requestParam.dashboardRedirected = params.dashboardRedirected;
    }

    if (params.partIds) {
      this.requestParam.ids = params.partIds;
    }
  },
  async mounted() {
    this.getTabsByCurrentUser(true);
    this.categories = await headerVm.getListCategories(true);
    this.categories.forEach((item) => {
      this.listOfItemsForCategory.push({ title: item.name, id: item.id });
      item.children.forEach((product) => {
        this.listOfItemsForProduct.push({
          title: product.name,
          id: product.id,
        });
      });
    });
  },
});
