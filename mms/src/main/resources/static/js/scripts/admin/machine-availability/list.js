axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
  document.title = "Machine Availability" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

$(function () {
  var selectTarget = $(".selectbox select");

  selectTarget.on({
    focus: function () {
      $(this).parent().addClass("focus");
    },
    blur: function () {
      $(this).parent().removeClass("focus");
    },
  });

  selectTarget.change(function () {
    var select_name = $(this).children("option:selected").text();
    $(this).siblings("label").text(select_name);
  });
});

var Page = Common.getPage("machines");

var vm = new Vue({
  el: "#app",
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "machine-availability-modal": httpVueLoader(
      "/components/machine-availability/machine-availability-modal.vue"
    ),
    "machine-availability-details": httpVueLoader(
      "/components/machine-availability/machine-availability-details.vue"
    ),
    "date-filter": httpVueLoader("/components/reject-rates/date-filter.vue"),
  },
  data: {
    isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
    deletePopup: false,
    visible: false,
    dropdownOpening: false,
    resources: {},
    results: [],
    showDropdown: null,
    total: 0,
    pagination: [],
    requestParam: {
      query: "",
      status: "all",
      sort: "machineCode,desc",
      page: 1,
      day: "20220110",
      size: 20,
      id: "",
    },
    codes: {},
    tabOptions: [
      {
        label: "All",
        value: "all",
      },
      // {
      //     label: 'Disabled',
      //     value: 'disabled'
      // }
    ],
    sortType: {
      MACHINE_ID: "machineCode",
      COMPANY: "location.company.name",
      LOCATION: "location.name",
      MACHINE_MAKER: "machineMaker",
      MACHINE_TYPE: "machineMaker",
      MACHINE_MODEL: "machineMaker",
      STATUS: "enabled",
    },
    pageType: "MACHINE_SETTING",
    objectType: "MACHINE",
    allColumnList: [],
    currentSortType: "id",
    isDesc: true,
    showDropdown: false,
    listChecked: [],
    listCheckedTracked: {},
    isAll: false,
    isAllTracked: [],
    listCheckedFull: [],
    isClicked: false,
    openingPopup: null,
    nextSelectDropdown: null,
    rawDate: null,
    isHover: false,
    cancelToken: undefined,
  },
  watch: {
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this;
        const x = this.results.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
  },
  methods: {
    closeShowDropDown() {
      this.showDropdown = false;
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      // const listId = this.listChecked.join(',');
      console.log(this.listChecked, "this.listChecked");
      const x = this.listChecked.map((item) => {
        return parseInt(item);
      });
      const params = {
        ids: x,
      };
      axios
        .post(`/api/machines/delete-in-batch`, params)
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
          .post(`/api/machines/delete-in-batch`, this.listChecked)
          .then((res) => {
            this.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    restoreItem() {
      const self = this;
      axios
        .post(`/api/machines/restore/${this.listChecked[0]}`)
        .then((res) => {
          console.log(res, "resss");
          self.paging(self.requestParam.page);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
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
      console.log(e, result);
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
      window.location.href = `/admin/machines/${this.listChecked[0]}`;
    },
    showDrawer() {
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    animationPrimary() {
      $(".animationPrimary").click(function () {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(function () {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
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
      vm.allColumnList = [
        {
          label: vm.resources["machine_id"],
          field: "machineCode",
          default: true,
          mandatory: true,
          sortable: true,
          sortField: "machine.machineCode",
        },
        {
          label: vm.resources["daily_work_hrs"],
          field: "dailyWorkingHour",
          default: true,
          sortable: true,
          sortField: "dailyWorkingHour",
        },
        {
          label: vm.resources["planned_downtime"],
          field: "plannedDowntime",
          default: true,
          sortable: true,
          sortField: "plannedDowntime",
        },
        {
          label: vm.resources["unplanned_downtime"],
          field: "unplannedDowntime",
          default: true,
          sortable: true,
          sortField: "unplannedDowntime",
        },
        {
          label: vm.resources["actual_work_hours"],
          field: "actualWorkingHour",
          default: true,
          sortable: true,
          sortField: "actualWorkingHour",
        },
      ];
      // this.resetColumnsListSelected();
      // this.getColumnListSelected();
      try {
        this.resetColumnsListSelected();
        // this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    setTabTitle() {
      this.tabOptions.forEach((tab) => {
        tab.label = vm.resources[tab.value];
      });
    },
    showCompanyDetails: function (company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetails(company);
      }
    },
    showLocationHistory: function (mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
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
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
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
        this.showDropdown = resultId;
      } else {
        this.showDropdown = null;
      }
    },
    showRevisionHistory: function (id, equipmentStatus) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MACHINE", equipmentStatus);
      }
    },
    tab: function (status) {
      this.listChecked = [];
      if (status == "DELETED") {
        console.log("come1");
        this.requestParam.status = status;
        this.requestParam.deleted = true;
        this.paging(1);
      } else {
        console.log("come2");
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.requestParam.status = status;
        delete this.requestParam.deleted;
        this.paging(1);
      }
      this.changeShow(null);
    },
    search: function (page) {
      this.paging(1);
    },

    paging: function (pageNumber) {
      var self = this;
      this.showDropdown = null;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      self.requestParam.id = objectId;
      let requestParamConvert = self.requestParam;
      this.listChecked = [];
      this.listCheckedTracked = {};
      this.isAll = false;
      this.isAllTracked = [];
      this.listCheckedFull = [];
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
      var param = Common.param(requestParamConvert);
      //console.log('param: ', param)
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get(`/api/machines/statistics/all?${param}`, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          //console.log('response: ', response)
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);

          if (objectId !== "") {
            let searchParam = self.results.filter(
              (item) => parseInt(item.id) === parseInt(objectId)
            )[0];
            self.requestParam.query = searchParam.machineCode;
            //console.log('searchParam.id-----------------------', self.requestParam.query)
            window.history.replaceState(null, null, Common.PAGE_URL.MACHINE);
          }

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

    findCounter: function (molds, counterId) {
      //console.log(">> " + counterId);
      for (var k = 0; k < molds.length; k++) {
        var mold = molds[k];

        if (typeof mold !== "object" || typeof mold.counter !== "object") {
          continue;
        }

        //console.log(">> " + counterId);
        if (counterId === mold.counter.id) {
          return mold.counter;
          break;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findCounter = this.findCounter(mold.part.molds, counterId);
          if (typeof findCounter === "object") {
            return findCounter;
            break;
          }
        }
      }
    },

    findMold: function (results, moldId) {
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
    installCounter: function (id) {
      location.href = "/admin/counters/new?" + id;
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
        vm.setAllColumnList();
        vm.setTabTitle();
      } catch (error) {
        console.log(error);
      }
    },
    deleteMachine: function (company) {
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
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    showMachineDetails: function (company) {
      var child = Common.vue.getChild(
        this.$children,
        "machine-availability-details"
      );
      if (child != null) {
        child.showDetails(company);
      }
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
      axios
        .post("/api/machines/change-status-in-batch", param)
        .then(function (response) {
          vm.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    save: function (location) {
      var param = {
        id: location.id,
        enabled: location.enabled,
      };
      axios
        .put(Page.API_BASE + "/" + location.id + "/enable", param)
        .then(function (response) {
          self.showDrowdown = null;
          vm.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    // handleChangePopup(index){
    //     this.isClicked = !this.isClicked
    //     if (this.isClicked) {
    //         this.openingPopup = index
    //     } else {
    //         this.openingPopup = null
    //     }
    // },
    // closePopup(){
    //     this.isClicked = false;
    // },
    showPopup(index) {
      console.log("Click");
      if (this.isClicked) {
        console.log("1-1");
        if (this.selectedDropdown != index) {
          this.nextSelectDropdown = index;
        } else {
          this.isShowingDropDown = false;
          this.openingPopup = null;
          this.nextSelectDropdown = null;
        }
      } else {
        console.log("1-2");
        this.openingPopup = index;
        this.isClicked = true;
      }
      setTimeout(() => {
        let dropDown = document.getElementById("actual-hour-popup");
        if (dropDown) {
          dropDown.scrollIntoView({
            behavior: "smooth",
            block: "center",
            inline: "nearest",
          });
        }
      }, 1);
    },
    close() {
      console.log("2-2");
      this.openingPopup = null;
      this.isClicked = false;
    },
    stopEvent(event) {
      event.preventDefault();
      event.stopPropagation();
    },
    closeDropDown(event) {
      if (this.openingPopup != null) {
        if (this.nextSelectDropdown != null) {
          console.log("2-1");
          this.openingPopup = this.nextSelectDropdown;
          this.nextSelectDropdown = null;
        } else {
          console.log("2-2");
          this.openingPopup = null;
          this.isClicked = false;
        }
      }
      this.stopEvent(event);
      console.log("Outside");
    },
    handleMouseOver(index) {
      if (!this.isClicked) {
        this.isHover = true;
        this.openingPopup = index;
      }
    },
    handleMouseLeave() {
      if (!this.isClicked) {
        this.isHover = false;
        this.openingPopup = null;
      }
    },
    openModal(type) {
      var child = Common.vue.getChild(
        this.$children,
        "machine-availability-modal"
      );
      if (child != null) {
        child.showModal(this.getCheckedItem(), type, {
          date: this.requestParam.day,
          rawDate: this.rawDate,
        });
      }
    },
    getCheckedItem() {
      const el = this.results.filter(
        (item) => item.machine.id == this.listChecked[0]
      );
      if (el.length > 0) {
        return el[0];
      }
      return null;
    },
    getNoteItemByType(machineAvailability, type) {
      if (machineAvailability.downtimeItems) {
        let element = machineAvailability.downtimeItems.filter(
          (item) => item.type == type
        );
        return element;
      }
      return null;
    },
    filterWithDate(date, rawDate) {
      // this.requestParam.startDate = date;
      // this.requestParam.day = date;
      // this.getDate = date;
      this.listChecked = [];
      this.listCheckedTracked = {};
      this.isAll = false;
      this.isAllTracked = [];
      this.listCheckedFull = [];
      this.requestParam.day = date;
      this.rawDate = rawDate;
      console.log(date, rawDate);
      this.paging(1);
    },
  },
  mounted() {
    this.$nextTick(function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      vm.animationOutline();
      vm.animationPrimary();
      var self = this;
      var uri = URI.parse(location.href);
      //console.log('paramIduri: ', uri);
      if (uri.fragment) {
        self.requestParam.query = unescape(uri.fragment);
      }
      axios.get("/api/codes").then(function (response) {
        //console.log('response.data: ', response.data)
        self.codes = response.data;
        self.paging(1);
      });
      vm.getResources();
    });
  },
});
