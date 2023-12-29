axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Insights" + " | eMoldino";
  //
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};
const EXPORT_TYPE = {
  PDF: "PDF",
  XLSX: "XLSX",
};
var Page = Common.getPage("end-life-cycle");

var vm = new Vue({
  el: "#app",
  components: {
    "year-picker": httpVueLoader("/components/insight/year-picker.vue"),
    "life-cycle-detail": httpVueLoader(
      "/components/insight/life-cycle-detail.vue"
    ),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    // 'show-columns': httpVueLoader('/components/part/show-columns.vue'),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "mold-status": httpVueLoader("/components/mold-status.vue"),
    "counter-status": httpVueLoader("/components/counter-status.vue"),
  },
  data: {
    results: [],
    total: 0,
    pagination: [],

    codes: {
      PriorityType: [],
      EndOfLifeCycleStatus: [],
    },
    requestParam: {
      query: "",
      page: 1,
      sort: "endLifeAt,asc",
      endOfLifeCycleStatus: "",
      priorityType: "",
      year: "",
    },
    sortType: {
      TOOLING_ID: "mold.equipmentCode",
      INSIGHT: "endLifeAt",
      PRIORITY: "priority",
      UTILIZATION_RATE: "mold.utilizationRate",
    },
    isDesc: true,
    currentSortType: "id",
    visibleCalendar: false,
    resources: {},
    allColumnList: [],
    isLoading: false,
    listChecked: [],
    listCheckedTracked: {},
    isAll: false,
    isAllTracked: [],
    listCheckedFull: [],
    EXPORT_TYPE: EXPORT_TYPE,
    selectedDownloadType: "default",
    currentIntervalIds: [],
    isCallingDone: false,
    isStopedExport: false,
    pageType: "TOOLING_DASHBOARD",
    optionParams: {},
    objectType: "TOOLING",
    lastShotDropdown: false,
    showPriority: false,
    show_Priority: "Search by Priority",
    Priority: [{ title: "Low" }, { title: "Medium" }, { title: "High" }],
    show_Status: "Search by Status",
    showStatus: false,
    showSearchStatus: [
      { title: "Dismissed" },
      { title: "In Communication" },
      { title: "Resolved" },
    ],

    lastShotData: {
      filter: null,
      sort: "",
    },
    cancelToken: undefined,
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
  },
  watch: {},
  methods: {
    // getOpConfig() {
    //   Common.getCurrentOpConfig().then((data) => {
    //     if (data) {
    //       const _inProductStatus = data.filter(
    //         (item) => item.operatingStatus === "WORKING"
    //       )[0];
    //       const _idleStatus = data.filter(
    //         (item) => item.operatingStatus === "IDLE"
    //       )[0];
    //       const _inActiveStatus = data.filter(
    //         (item) => item.operatingStatus === "NOT_WORKING"
    //       )[0];
    //       const _disconnectStatus = data.filter(
    //         (item) => item.operatingStatus === "DISCONNECTED"
    //       )[0];

    //       this.ruleOpStatus.inProduct =
    //         "There has been a shot made within the last " +
    //         _inProductStatus.time +
    //         this.getTimeUnit(_inProductStatus.timeUnit);
    //       this.ruleOpStatus.idle =
    //         "There has been a shot made after " +
    //         _inProductStatus.time +
    //         this.getTimeUnit(_inProductStatus.timeUnit) +
    //         " but within " +
    //         _idleStatus.time +
    //         this.getTimeUnit(_idleStatus.timeUnit);
    //       this.ruleOpStatus.inactive =
    //         "There is no shot made within " +
    //         _inActiveStatus.time +
    //         this.getTimeUnit(_inActiveStatus.timeUnit);
    //       this.ruleOpStatus.sensorOffline =
    //         "There is no signal from the sensor within " +
    //         _disconnectStatus.time +
    //         this.getTimeUnit(_disconnectStatus.timeUnit);
    //     }
    //   });
    // },
    getTimeUnit(timeUnit) {
      return timeUnit === "HOURS" ? " hour(s) " : " day(s) ";
    },
    // <!-- SHOW PERIORITY -->
    select_priorityType(item) {
      // alert(item);

      this.show_Priority = item.title;
      this.showPriority = false;
      // console.log(item)
      this.requestParam.priorityType = item.code;
      this.search();
    },

    handleclosePriority: function () {
      this.showPriority = false;
      // alert(this.showCompanyType);
    },
    handlepriorityToggle: function (isShow) {
      this.showPriority = isShow;
    },
    // <!--  -->
    select_showStatus(item) {
      // alert(item);
      // alert(this.showStatus);
      this.show_Status = item.title;
      this.showStatus = false;
      console.log(item);
      this.requestParam.endOfLifeCycleStatus = item.code;
      this.search();
    },
    handleshowStatus: function () {
      this.showStatus = true;
      // alert(this.showCompanyType);
    },
    handlecloseStatus: function () {
      this.showStatus = false;
      // alert(this.showCompanyType);
    },
    handleStatusToggle: function (isShow) {
      this.showStatus = isShow;
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
          this.currentSortType = "accumulatedShot";
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
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    backToMoldDetail() {
      console.log("back to molds detail");
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.backFromPreview();
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
    stopExport: function () {
      this.isStopedExport = true;
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
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          defaultSelected: true,
          sortable: true,
          sortField: "mold.equipmentCode",
        },
        { label: this.resources["part"], field: "part", sortable: true },
        {
          label: this.resources["utilization_rate"],
          field: "utilizationRate",
          default: true,
          defaultSelected: true,
          sortable: true,
          sortField: "mold.utilizationRate",
        },
        {
          label: this.resources["insights"],
          field: "insight",
          default: true,
          defaultSelected: true,
          sortable: true,
        },
        {
          label: this.resources["priority"],
          field: "priority",
          default: true,
          defaultSelected: true,
          sortable: true,
        },

        {
          label: this.resources["company"],
          field: "company",
          sortable: true,
          sortField: "mold.location.company.name",
        },
        {
          label: this.resources["location"],
          field: "location",
          sortable: true,
          sortField: "mold.location.name",
        },
        {
          label: this.resources["latest_cycle_time"],
          field: "cycleTime",
          sortable: true,
          sortField: "mold.lastCycleTime",
        },
        {
          label: this.resources["weighted_average_cycle_time"],
          field: "weightedAverageCycleTime",
          sortable: true,
          sortField: "mold.weightedAverageCycleTime",
        },

        {
          label: this.resources["accumulated_shots"],
          field: "accumulatedShot",
          sortable: true,
        },
        {
          label: this.resources["uptime_target"],
          field: "uptimeTarget",
          sortable: true,
          sortField: "mold.uptimeTarget",
        },
        {
          label: this.resources["weight_of_runner_system_o"],
          field: "weightOfRunnerSystem",
          sortable: true,
          sortField: "mold.weightRunner",
        },
        {
          label: this.resources["approved_cycle_time"],
          field: "contractedCycleTimeSeconds",
          sortable: true,
          sortField: "mold.contractedCycleTime",
        },
        {
          label: this.resources["counter_id"],
          field: "counterCode",
          sortable: true,
          sortField: "mold.counter.equipmentCode",
        },
        {
          label: this.resources["reset.forecastedMaxShots"],
          field: "designedShot",
          sortable: true,
          sortField: "mold.designedShot",
        },
        {
          label: this.resources["hot_runner_number_of_drop"],
          field: "hotRunnerDrop",
          sortable: true,
          sortField: "mold.hotRunnerDrop",
        },
        {
          label: this.resources["hot_runner_zone"],
          field: "hotRunnerZone",
          sortable: true,
          sortField: "mold.hotRunnerZone",
        },
        {
          label: this.resources["front_injection"],
          field: "injectionMachineId",
          sortable: true,
          sortField: "mold.injectionMachineId",
        },
        {
          label: this.resources["required_labor"],
          field: "labour",
          sortable: true,
          sortField: "mold.labour",
        },
        {
          label: this.resources["last_date_of_shots"],
          field: "lastShotDate",
          sortable: true,
          sortField: "mold.lastShotAt",
        },
        {
          label: this.resources["machine_ton"],
          field: "quotedMachineTonnage",
          sortable: true,
          sortField: "mold.quotedMachineTonnage",
        },
        {
          label: this.resources["maintenance_interval"],
          field: "preventCycle",
          sortable: true,
          sortField: "mold.preventCycle",
        },
        {
          label: this.resources["maker_of_runner_system"],
          field: "runnerMaker",
          sortable: true,
          sortField: "mold.runnerMaker",
        },
        {
          label: this.resources["maximum_capacity_per_week"],
          field: "maxCapacityPerWeek",
          sortable: true,
          sortField: "mold.maxCapacityPerWeek",
        },
        // { label: this.resources['overdue_maintenance_tolerance'], field: 'preventOverdue', sortable: true, sortField: 'mold.preventOverdue'},
        {
          label: this.resources["production_hour_per_day"],
          field: "shiftsPerDay",
          sortable: true,
          sortField: "mold.shiftsPerDay",
        },
        {
          label: this.resources["production_day_per_week"],
          field: "productionDays",
          sortable: true,
          sortField: "mold.productionDays",
        },
        {
          label: this.resources["shot_weight"],
          field: "shotSize",
          sortable: true,
          sortField: "mold.shotSize",
        },
        {
          label: this.resources["supplier"],
          field: "supplierCompanyName",
          sortable: true,
          sortField: "mold.supplier.name",
        },
        {
          label: this.resources["tool_description"],
          field: "toolDescription",
          sortable: true,
          sortField: "mold.toolDescription",
        },
        {
          label: this.resources["front_tool_size"],
          field: "toolingSizeView",
          sortable: true,
          sortField: "mold.size",
        },
        {
          label: this.resources["tool_weight"],
          field: "toolingWeightView",
          sortable: true,
          sortField: "mold.weight",
        },
        {
          label: this.resources["tooling_complexity"],
          field: "toolingComplexity",
          sortable: true,
          sortField: "mold.toolingComplexity",
        },
        {
          label: this.resources["tooling_letter"],
          field: "toolingLetter",
          sortable: true,
          sortField: "mold.toolingLetter",
        },
        {
          label: this.resources["tooling_type"],
          field: "toolingType",
          sortable: true,
          sortField: "mold.toolingType",
        },
        {
          label: this.resources["toolmaker"],
          field: "toolMakerCompanyName",
          sortable: true,
          sortField: "mold.toolMaker.name",
        },
        {
          label: this.resources["type_of_runner_system"],
          field: "runnerTypeTitle",
          sortable: true,
          sortField: "mold.runnerType",
        },
        {
          label: this.resources["upcoming_maintenance_tolerance"],
          field: "preventUpcoming",
          sortable: true,
          sortField: "mold.preventUpcoming",
        },
        {
          label: this.resources["uptime_tolerance_l1"],
          field: "uptimeLimitL1",
          sortable: true,
          sortField: "uptimeLimitL1",
        },
        {
          label: this.resources["uptime_tolerance_l2"],
          field: "uptimeLimitL2",
          sortable: true,
          sortField: "uptimeLimitL2",
        },
        {
          label: this.resources["year_of_tool_made"],
          field: "madeYear",
          sortable: true,
          sortField: "mold.madeYear",
        },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          sortField: "toolingStatus",
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          sortField: "sensorStatus",
        },
      ];
      try {
        this.resetColumnsListSelected();
        this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
      // this.resetColumnsListSelected();
      // this.getColumnListSelected()
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
          // this.resetColumnsListSelected();
          // this.getColumnListSelected();
        })
        .catch((error) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        })
        .finally((err) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    progressBarRun: function () {
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
    stopExport: function () {
      this.isStopedExport = true;
    },
    downloadTooling: function (type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_insights_data`;

      //   if(this.listChecked.length === 0) {
      //     return;
      //   }
      let path = "";

      let listChecked = [];
      Object.keys(this.listCheckedTracked).forEach((status) => {
        if (status === this.requestParam.status) {
          Object.keys(this.listCheckedTracked[status]).forEach((page) => {
            if (this.listCheckedTracked[status][page].length > 0) {
              listChecked = listChecked.concat(
                this.listCheckedTracked[status][page]
              );
            }
          });
        }
      });

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
      axios
        .get(url, { responseType: "blob", observe: "response" })
        .then(function (response) {
          if (!$vm.isStopedExport) {
            $vm.isCallingDone = true;
            setTimeout(function () {
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
        })
        .catch(function (error) {
          console.log(error);
          $vm.isStopedExport = true;
        });
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
    tab(status) {
      this.total = null;
      this.isAll = false;
      this.listChecked = [];
      this.isAllTracked = [];
      this.listCheckedTracked = {};
      this.$forceUpdate();
      this.currentSortType = "id";
      this.requestParam.sort = "lastShotAt,desc";
      this.requestParam.status = status;
      this.paging(1);
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
      this.allColumnList.forEach((item) => {
        if (item.field === value) {
          item.enabled = !item.enabled;
        }
      });
      this.saveSelectedList();
      this.$forceUpdate();
    },
    ratio: function (mold) {
      return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
    },
    ratioStyle: function (mold) {
      return "width: " + this.ratio(mold) + "%";
    },
    ratioColor: function (mold) {
      var ratio = this.ratio(mold);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },
    isPassDate(date) {
      return moment().format("YYYY-MM-DD") > date;
    },
    search: function (page) {
      this.paging(1);
    },
    selectYear: function (year) {
      this.requestParam.year = year;
      this.visibleCalendar = false;
      this.paging(1);
    },
    hideCalendarPicker: function () {
      this.visibleCalendar = false;
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

    select(event) {
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
    tabs() {
      return {
        "": this.resources["overview"],
        IN_COMMUNICATION: this.resources["communication"],
        RESOLVE: this.resources["resolve"],
        DISMISS: this.resources["dismiss"],
      };
    },
    tab: function (status) {
      this.total = null;
      this.listChecked = [];
      this.isAll = false;
      console.log("come");
      if (status == "DELETED") {
        this.requestParam.status = status;
        this.requestParam.deleted = true;
        this.paging(1);
      } else {
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.requestParam.status = status;
        delete this.requestParam.deleted;
        this.paging(1);
      }
      this.changeShow(null);
    },
    paging(pageNumber) {
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      let requestParam = {};
      Object.keys(this.requestParam).forEach((key) => {
        if (this.requestParam[key]) {
          requestParam[key] = this.requestParam[key];
        }
      });
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked
        .filter((item) => item.status === this.requestParam.status)
        .map((item) => item.page);
      if (pageSelectedAll.includes(this.requestParam.page)) {
        this.isAll = true;
      }
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
      self.isLoading = true;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.isLoading = false;
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
          if (
            self.listCheckedTracked[self.requestParam.status][
              self.requestParam.page
            ]
          ) {
            self.listChecked =
              self.listCheckedTracked[self.requestParam.status][
                self.requestParam.page
              ];
          }
          if (self.results != null) {
            self.results.forEach((item, index) => {
              if (item.endLifeAt < moment().unix()) {
                item.endLifeAtOver = true;
              } else item.endLifeAtOver = false;
            });
          }
          Common.handleNoResults("#app", self.results.length);
        })
        .catch(function (error) {
          self.isLoading = false;
          console.log(error.response);
        });
    },
    tab: function (status) {
      this.total = null;
      this.currentSortType = "endLifeAt";
      this.requestParam.sort = "endLifeAt,asc";
      this.requestParam.endOfLifeCycleStatus = status;
      this.paging(1);
    },
    sortBy(type) {
      if (type === "accumulatedShot") {
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
    sort: function () {
      this.paging(this.requestParam.page);
    },
    showDetail: function (data) {
      var lifeCycleDetail = Common.vue.getChild(
        this.$children,
        "life-cycle-detail"
      );
      if (lifeCycleDetail != null) {
        lifeCycleDetail.show(data);
      }
    },
    showToolingChart: function (mold, tab) {
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.showChart(mold, "QUANTITY", "DAY", tab ? tab : "switch-graph");
      }
    },
    showToolingDetail: function (mold) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      const tab = "switch-detail";
      let self = this;
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        if (mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
            .then(function (response) {
              reasonData = response.data;
              // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
              reasonData.approvedAt = reasonData.approvedAt
                ? self.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              mold.notificationStatus = mold.dataSubmission;
              mold.reason = reasonData.reason;
              mold.approvedAt = reasonData.approvedAt;
              mold.approvedBy = reasonData.approvedBy;
              // child.showMoldDetails(mold);
              self.showToolingChart(mold, tab);
            });
        } else {
          this.showToolingChart(mold, tab);
          // child.showMoldDetails(mold);
        }
      }
    },
    changeLifeCycleStatus(result, status) {
      if (result.status === status) {
        status = "NULL";
      }
      axios
        .post(`/api/end-life-cycle/${result.id}/change-status?status=${status}`)
        .then(function () {
          result.status = status;
        });
    },
  },
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
          this.setAllColumnList();
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.codes = Object.assign({}, this.codes, {
            ...JSON.parse(JSON.stringify(newVal)),
            EndOfLifeCycleStatus: newVal.EndOfLifeCycleStatus.filter(
              (i) => i.code && i.title
            ),
          });
          // this.getOpConfig();
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          const isConfigEnabled = newVal?.OP?.enabled;
          const data = newVal?.OP?.rules;
          if (isConfigEnabled) {
            const _inProductStatus = data.filter(
              (item) => item.operatingStatus === "WORKING"
            )[0];
            const _idleStatus = data.filter(
              (item) => item.operatingStatus === "IDLE"
            )[0];
            const _inActiveStatus = data.filter(
              (item) => item.operatingStatus === "NOT_WORKING"
            )[0];
            const _disconnectStatus = data.filter(
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
          }
        }
      },
      { immediate: true }
    );
  },
  mounted() {
    console.log("end of life cycle mounted");
    const uri = URI.parse(location.href);
    const params = Common.parseParams(uri.query);
    if (params.priorType) {
      this.requestParam.priorityType = params.priorType;
    }

    if (params.tab) {
      this.requestParam.endOfLifeCycleStatus = params.tab;
    }

    console.log("params.tab", params.tab);
    this.tab(this.requestParam.endOfLifeCycleStatus);
  },
});
