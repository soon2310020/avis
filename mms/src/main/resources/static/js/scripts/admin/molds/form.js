var Page = Common.getPage("molds");
const newPageUrl = Common.PAGE_URL;

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

window.onload = function () {
  document.title = "Tooling" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_mold1");
    $("div").removeClass("wave_mold2");
    $("div").removeClass("profile_wave");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    if (document.getElementById("remove_profile")) {
      document.getElementById("remove_profile").remove();
    }
    $("div").removeClass("hide_account");
  }, 200);
};

const DEFAULT_TIME_OPTION = [{ title: "Daily", type: "DAILY", isRange: false }];
const DEFAULT_TIME_RANGE = {
  minDate: new Date(),
  maxDate: new Date("2100-01-01"),
};
const DEFAULT_MONTHLY_SCHEDULE_DAY = 3;
const DEFAULT_MONTHLY_UPCOMMING_TOLENRANCE = 14;
const DEFAULT_WEEKLY_SCHEDULE_DAY = 3;
const DEFAULT_WEEKLY_UPCOMMING_TOLENRANCE = 0;

var vm = new Vue({
  el: "#app",
  components: {
    "part-search": httpVueLoader("/components/part-search.vue"),
    "location-search": httpVueLoader("/components/location-search.vue"),
    "company-search": httpVueLoader("/components/company-search.vue"),
    "engineer-charge-dropdown": httpVueLoader(
      "/components/engineer-charge-dropdown.vue"
    ),
    "counter-search-tooling": httpVueLoader(
      "/components/counter-search-tooling.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "date-picker-button": httpVueLoader(
      "/components/@base/date-picker/date-picker-button.vue"
    ),
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
    "weekly-field": httpVueLoader(
      "/components/@base/frequency-field/weekly-field.vue"
    ),
    "monthly-field": httpVueLoader(
      "/components/@base/frequency-field/monthly-field.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "warning-modal": httpVueLoader(
      "/components/@base/dialog/warning-dialog.vue"
    ),
  },

  data: {
    timeOptions: [...DEFAULT_TIME_OPTION],
    timeRange: {
      minDate: null,
      maxDate: new Date("2100-01-01"),
    },
    cycle_time2: "Unit",
    showCycleTime2: false,
    cycle_time: "Unit",
    showCycleTime: false,
    cycleTimeList: [],
    cost_currency: "Unit",
    showCostCurrency: false,
    costCurrencyList: [],
    weight_unit: "Unit",
    showWeightUnit: false,
    weightUnitList: [],
    tool_size: "Unit",
    showToolSize: false,
    toolSizeList: [],
    tooling_complexity: "",
    showToolingComplexity: false,
    toolingComplexityList: [],
    resources: {},
    isToolMaker: false,
    serverName: "",
    userType: "",
    customFieldList: null,
    moldCorrective: {
      id: "",
      moldId: "",
      failureTimeText: "",
      repairDateTime: "",
      cost: "",
      currencyType: "USD",
      memo: "",
      mold: {
        equipmentCode: "",
      },
    },
    mold: {
      counterId: "",
      counterCode: "",
      accumulatedShots: 0,
      //new feature
      supplierMoldCode: "",
      toolingComplexity: "",
      toolingLetter: "",
      weightRunner: "",
      hotRunnerDrop: "",
      hotRunnerZone: "",
      quotedMachineTonnage: "",
      accumulatedShots: 0,
      // currentMachineTonnage: '',
      maxCapacityPerWeek: "0",
      // shotSize: '0',
      // size old
      sizeWidth: "",
      sizeHeight: "",
      sizeDepth: "",
      injectionMachineId: "",
      totalCavities: "",
      cycleTimeLimit2Unit: null,
      cycleTimeLimit1Unit: null,
      assetNumber: "",
      equipmentCode: "",
      equipmentStatus: "AVAILABLE",

      name: "",
      toolingType: "",
      lifeYears: "",
      madeYear: "",

      // 'familyTool': false,
      lastShot: "",
      warrantedShot: "",

      designedShot: "",
      approvedCycleTime: "",
      contractedCycleTime: "",
      toolmakerApprovedCycleTime: "",
      toolmakerContractedCycleTime: "",
      scrapRate: "",

      toolingCondition: null,
      preventCycle: "50000",
      preventUpcoming: "10000",
      preventOverdue: "0",
      cycleTimeLimit1: "",
      cycleTimeLimit2: "",

      upperLimitTemperature: "",
      upperLimitTemperatureUnit: null,
      lowerLimitTemperature: "",
      lowerLimitTemperatureUnit: null,
      engineerIds: [],
      plantEngineerIds: [],
      engineerDate: "",

      size: "",
      sizeUnit: "MM",
      weight: "",
      weightUnit: "KILOGRAMS",
      runnerType: null,
      runnerMaker: "",
      maker: "",

      toolMakerCompanyId: "",
      toolMakerCompanyName: "",

      supplierCompanyId: "",
      supplierCompanyName: "",

      supplierForToolMaker: "",

      useageType: null,
      resin: "",
      cost: "",
      accumulatedMaintenanceCost: "0",
      accumulatedMaintenanceCostView: "$0",
      costCurrencyType: "USD",
      salvageValue: "",
      salvageCurrency: "USD",
      memo: "",

      // supplier
      uptimeTarget: "90",
      uptimeLimitL1: "",
      uptimeLimitL2: "",
      labour: "",
      hourPerShift: "",
      shiftsPerDay: "24",
      productionDays: "7",
      productionWeeks: "",
      supplierTagId: "",

      toolDescription: "",
      locationId: "",
      location: { name: "" },

      authorities: [],
      parts: [
        {
          moldId: "",
          partId: "",
          partName: "",
          cavity: "",
          totalCavities: "",
        },
      ],
      moldParts: [
        {
          moldId: "",
          partId: "",
          partName: "",
          cavity: "",
          totalCavities: "",
        },
      ],
      WLH: "",
      backup: false,
    },
    partValueInit: {
      cavity: "",
      totalCavities: "",
    },
    files: [],
    selectedFiles: [],

    secondFiles: [],
    thirdFiles: [],
    forthFiles: [],

    //edit
    toolingConditionFiles: [],
    toolingPictureFile: [],
    documentFiles: [],

    roles: [],
    codes: {
      SizeUnit: [],
      OutsideUnit: [],
      CurrencyType: [],
      WeightUnit: [],
    },
    engineers: [],
    size: "default",
    // engineerIndex: [],
    // checkedList: [],

    reasonData: {},
    requiredFields: [],
    isCreate: false,
    configCategory: "TOOLING",
    fileTypes: {
      MOLD_CONDITION: "MOLD_CONDITION",
      MOLD_INSTRUCTION_VIDEO: "MOLD_INSTRUCTION_VIDEO",
      MOLD_MAINTENANCE_DOCUMENT: "MOLD_MAINTENANCE_DOCUMENT",
      MOLD_PO_DOCUMENT: "MOLD_PO_DOCUMENT",
    },
    checkDeleteTooling: {},
    document: {
      documentUrl: null,
      docName: null,
    },
    salvageCurrencyVisible: false,
    costDocumentFiles: [],
    options: {},
    backUpListItem: [
      { id: 1, title: "Not a backup tooling", value: false },
      { id: 2, title: "Is a backup tooling", value: true },
    ],
    selectedBackUpItem: { id: 1, title: "Not a backup tooling", value: false },
    selectedMaintenanceIntervalType: "SHOT_BASED",
    defaultMaintenanceIntervalType: null,
    maintenanceIntervalTypes: [
      {
        title: "Shot Based",
        titleKey: "shot_based",
        value: "SHOT_BASED",
      },
      {
        title: "Time Based",
        titleKey: "time_based",
        value: "TIME_BASED",
      },
    ],
    maintenanceFrequencies: [
      { title: "Weekly", frequency: "WEEKLY" },
      { title: "Monthly", frequency: "MONTHLY" },
    ],
    maintenanceFrequency: { title: "Weekly", frequency: "WEEKLY" },
    numberOfSchedule: 3,
    scheduleAt: "1",
    scheduleDay: "MONDAY",
    continueFrequencies: [
      { title: "forever", value: "FOREVER" },
      { title: "for", value: "FOR" },
      { title: "until", value: "UNTIL" },
    ],
    continueFrequency: { title: "forever", value: "FOREVER" },
    continueTime: 3,
    currentDate: {
      from: new Date(),
      to: new Date(),
      fromTitle: moment().format("YYYY-MM-DD"),
      toTitle: moment().format("YYYY-MM-DD"),
    },
    continueDate: {
      from: moment().add(3, "months").toDate(),
      to: moment().add(3, "months").toDate(),
      fromTitle: moment().add(3, "months").format("YYYY-MM-DD"),
      toTitle: moment().add(3, "months").format("YYYY-MM-DD"),
    },
    maintenanceDateFrequency: "DAILY",
    maintenanceContinueDateFrequency: "DAILY",
    schedUpcomingTolerance: 0,
    showConfirm: false,
    existedWorkOrder: 0,
  },
  methods: {
    handleConfirm(isContinue) {
      if (isContinue) {
        this.showConfirm = false;
        this.updateCustomField();
        this.update();
      }
    },
    handleShowDueDatePicker() {
      const datePickerModalRef = this.$refs.datePickerModalRef;
      const dateValue = { ...this.currentDate };
      datePickerModalRef.showDatePicker(
        this.maintenanceDateFrequency,
        dateValue
      );
    },
    handleChangeMaintenanceDate(timeValue, frequency) {
      const datePickerModalRef = this.$refs.datePickerModalRef;
      this.currentDate = timeValue;
      this.maintenanceDateFrequency = frequency;
      datePickerModalRef.closeDatePicker();
    },
    handleContinueDatePicker() {
      const datePickerModalRef = this.$refs.datePickerModalContinueRef;
      const dateValue = { ...this.continueDate };
      datePickerModalRef.showDatePicker(
        this.maintenanceDateFrequency,
        dateValue
      );
    },
    handleChangeMaintenanceContinueDate(timeValue, frequency) {
      const datePickerModalRef = this.$refs.datePickerModalContinueRef;
      this.continueDate = timeValue;
      this.maintenanceContinueDateFrequency = frequency;
      datePickerModalRef.closeDatePicker();
    },
    handleChangeContinueFrequency(item) {
      this.continueFrequency = item;
    },
    handleChangeMaintainanceWeek(number, day) {
      console.log("handleChangeMaintainanceWeek", number, day);
      this.numberOfSchedule = number;
      this.scheduleDay = day.value;
    },
    handleChangeMaintainanceMonth(number, day, at) {
      this.numberOfSchedule = number;
      this.scheduleDay = day.value;
      this.scheduleAt = at.value;
    },
    handleChangeFrequency(item) {
      console.log("handleChangeFrequency", item);
      this.maintenanceFrequency = item;
      if (item.frequency === "WEEKLY") {
        this.numberOfSchedule = DEFAULT_WEEKLY_SCHEDULE_DAY;
        this.schedUpcomingTolerance = DEFAULT_WEEKLY_UPCOMMING_TOLENRANCE;
      } else if (item.frequency === "MONTHLY") {
        this.numberOfSchedule = DEFAULT_MONTHLY_SCHEDULE_DAY;
        this.schedUpcomingTolerance = DEFAULT_MONTHLY_UPCOMMING_TOLENRANCE;
      }
    },
    handleChangeBackUpItem(item) {
      this.selectedBackUpItem = item;
      this.mold.backup = item.value;
    },
    onOpenDatePicker() {
      this.$refs.datePickerRef.focus();
      console.log("check date picker refs", this.$refs.datePickerRef);
      if (document.getElementsByClassName("ant-calendar-picker-input")) {
        document.getElementsByClassName("ant-calendar-picker-input")[0].click();
      }
    },
    toggleSalvageCurrencyVisible(value) {
      if (value !== undefined) {
        this.salvageCurrencyVisible = value;
      } else this.salvageCurrencyVisible = !this.salvageCurrencyVisible;
    },
    onSelectSalvageCurrency(result) {
      this.mold.costCurrencyType = result.code;
      this.toggleSalvageCurrencyVisible(false);
    },
    toggleUploadDocument() {
      // this.$refs.uploadDocumentRef.click();
      $("#uploadDocumentRef").click();
    },
    onUploadFile(event) {
      this.forthFiles.push(event.target.files[0]);
      console.log("file", event.target.files);
    },
    removeImgUploaded() {
      this.document = {
        documentUrl: null,
        docName: null,
      };
    },
    selectCycleTime2(item) {
      this.mold.cycleTimeLimit2Unit = item.code;
      console.log(this.mold.cycleTimeLimit2Unit);
      this.showCycleTime2 = false;
      this.cycle_time2 = item.title;
    },
    closeCycleTime2() {
      this.showCycleTime2 = false;
    },
    handleTogleCycleTime2(flag) {
      this.showCycleTime2 = flag;
    },
    selectCycleTime(item) {
      this.mold.cycleTimeLimit1Unit = item.code;
      this.showCycleTime = false;
      this.cycle_time = item.title;
    },
    closeCycleTime() {
      this.showCycleTime = false;
    },
    handleTogleCycleTime(flag) {
      this.showCycleTime = flag;
    },
    selectCostCurrency(item) {
      this.mold.costCurrencyType = item.code;
      this.showCostCurrency = false;
      this.cost_currency = item.title;
    },
    closeCostCurrency() {
      this.showWeightUnit = false;
    },
    handleTogleCostCurrency(flag) {
      this.showCostCurrency = flag;
    },

    selectWeightUnit(item) {
      this.mold.weightUnit = item.code;
      this.showWeightUnit = false;
      this.weight_unit = item.title;
    },
    closeWeightUnit() {
      this.showWeightUnit = false;
    },
    handleTogleWeightUnit(flag) {
      this.showWeightUnit = flag;
    },
    selectToolSize(item) {
      this.mold.sizeUnit = item.code;
      this.showToolSize = false;
      this.tool_size = item.title;
    },
    closeToolSize() {
      this.showToolSize = false;
    },
    handleTogleToolSize(flag) {
      this.showToolSize = flag;
    },

    selectToolingComplexity(item) {
      this.mold.toolingComplexity = item.title;
      this.showToolingComplexity = false;
    },
    closeToolingComplexity() {
      this.showToolingComplexity = false;
    },
    handleTogleToolingComplexity(flag) {
      this.showToolingComplexity = flag;
    },
    mappingListConfigCustomField(category) {
      if (this.customFieldList) {
        let _category = "TOOLING_" + category;
        return this.customFieldList.filter(
          (item) => item.propertyGroup === _category
        );
      }
    },
    isRequiredField(field) {
      if (
        [
          "upcomingMaintenanceTolerance",
          "equipmentCode",
          "designedShot",
          "locationName",
          "cycleTimeLimit1",
          "cycleTimeLimit2",
          "uptimeLimitL1",
          "uptimeLimitL2",
          "toolMakerCompanyName",
          "supplierCompanyName",
          "uptimeTarget",
          "totalCavities",
          "maintenanceInterval",
        ].includes(field)
      ) {
        return true;
      }
      return this.requiredFields.includes(field);
    },
    // handleChange(value) {
    //   console.log("handleChange", value);
    //   //engineer
    //   this.engineerIndex = value;
    //   this.mold.engineerIds = value;
    //   this.checkedList = [];
    //   value.map((id) => {
    //     this.engineers.map((item) => {
    //       if (id === item.id) {
    //         this.checkedList.push(item);
    //       }
    //     });
    //   });
    //   this.engineerIndex = [...value];
    //   // console.log("idddd",this.checkedList)
    // },
    handleChange(value) {
      console.log("@handleChange", value);
    },

    submit: async function () {
      console.log("this.mold: ", this.mold);
      if (this.isErrorSalvageValue) {
        return Common.alert(
          "Salvage value can not exceed 'Cost of Tooling' value"
        );
      }
      if (
        Number(this.mold.preventCycle) <= Number(this.mold.preventUpcoming) &&
        this.selectedMaintenanceIntervalType === "SHOT_BASED"
      ) {
        return Common.alert(
          "Maintenance tolerance must not be larger or equal to maintenance interval"
        );
      }
      //by order
      if (!this.mold.locationId) {
        Common.alert("Please select location.");
        return;
      }
      if (!this.mold.toolMakerCompanyId) {
        Common.alert("Please select Toolmaker.");
        return;
      }

      if (
        !(
          this.mold.toolmakerContractedCycleTime ||
          this.mold.contractedCycleTime
        )
      ) {
        Common.alert("At least one approved cycle time is required");
        return;
      }

      if (!this.isToolMaker && !this.mold.supplierCompanyId) {
        Common.alert("Please select supplier.");
        return;
      }

      if (this.mold.parts.length == 0) {
        Common.alert("Please select part.");
        return;
      }

      if (
        this.requiredFields.includes("engineers") &&
        (this.mold.engineerIds == null || this.mold.engineerIds.length == 0)
      ) {
        Common.alert("Please select engineer.");
        return;
      }

      if (
        "Select Tooling complexity" === this.mold.toolingComplexity &&
        this.isToolingComplexityEnabled
      ) {
        this.mold.toolingComplexity = "";
      }

      if (
        this.mold.salvageValue &&
        (this.mold.salvageValue < 0 || isNaN(this.mold.salvageValue))
      ) {
        Common.alert(
          "Invalid data format. Salvage value can only contain positive numbers"
        );
        return;
      }

      for (var i = 0; i < this.mold.parts.length; i++) {
        if (this.mold.parts[i].partId == "") {
          Common.alert("Please select part.");
          return;
        }
      }

      // if (this.userType == 'OEM' && this.mold.authorities.length == 0) {
      //     Common.alert('Please check access group');
      //     return;
      // }

      // 등록 처리용 part 정보 설정.
      this.mold.moldParts = this.mold.parts;
      console.log("this.mold.moldParts", this.mold.moldParts);
      const { sizeWidth, sizeHeight, sizeDepth, WLH } = this.mold;
      // this.mold.size = WLH
      this.mold.size = `${sizeWidth}x${sizeDepth}x${sizeHeight}`;

      console.log(this.mold.uptimeTarget);
      const maintainaceData = this.getMaintainaceData();
      console.log("maintainanceData when submit", maintainaceData);
      if (
        maintainaceData.pmStrategy === "TIME_BASED" &&
        maintainaceData.pmFrequency === "WEEKLY" &&
        maintainaceData.schedUpcomingTolerance > 6
      ) {
        Common.alert(
          "Upcoming Maintenance Tolerance must be smaller than 7 days."
        );
      } else if (
        maintainaceData.pmStrategy === "TIME_BASED" &&
        maintainaceData.pmFrequency === "MONTHLY" &&
        maintainaceData.schedUpcomingTolerance > 27
      ) {
        Common.alert(
          "Upcoming Maintenance Tolerance must be smaller than 28 days."
        );
      } else {
        if (Page.IS_NEW) {
          this.create();
        } else {
          if (
            this.defaultMaintenanceIntervalType === maintainaceData.pmStrategy
          ) {
            this.updateCustomField();
            this.update();
          } else {
            this.checkValidWorkOrder();
          }
        }
      }
    },
    async checkValidWorkOrder() {
      try {
        const id = this.mold.id;
        const res = await axios.get(
          `/api/work-order/maintenance/exist?moldId=${id}`
        );
        console.log("checkValidWorkOrder", res);
        this.existedWorkOrder = res.data;
        if (res.data > 0) {
          this.showConfirm = true;
        } else {
          this.updateCustomField();
          this.update();
        }
      } catch (error) {
        console.log(error);
      }
    },
    create: function () {
      const arr = this.customFieldList.map((item) => ({
        id: item.id,
        customFieldValueDTOList: [
          {
            value: item.defaultInputValue,
          },
        ],
      }));
      const params = {
        customFieldDTOList: arr,
      };
      const payload = this.formData();

      axios
        .post(Page.API_POST, payload, this.multipartHeader())
        .then(function (response) {
          const objectId = response.data.id;
          axios
            .post(`/api/custom-field-value/edit-list/${objectId}`, params)
            .then(() => {
              Common.alertCallback = function () {
                location.href = newPageUrl.TOOLING;
              };
              Common.alert("success");
            })
            .catch((e) => {
              console.log(e);
            });
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },
    update: function () {
      vm.children = null;

      const payload = this.formData();
      axios
        .put(Page.API_PUT, payload, this.multipartHeader())
        .then(function (response) {
          console.log(response.data);
          // if (response.data.success) {

          Common.alertCallback = function () {
            // location.href = newPageUrl.TOOLING;
            history.back();
          };
          Common.alert("success");
          // } else {
          //     Common.alert(response.data.message);
          // }
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },
    updateCustomField() {
      let url_string = window.location.href;
      var objectId = /[^/]*$/.exec(url_string)[0];
      const arr = this.customFieldList.map((item) => ({
        id: item.id,
        customFieldValueDTOList: [
          {
            value: item.defaultInputValue,
          },
        ],
      }));
      const params = {
        customFieldDTOList: arr,
      };
      axios
        .post(`/api/custom-field-value/edit-list/${objectId}`, params)
        .then(() => {});
    },

    formData: function () {
      var formData = new FormData();
      for (var i = 0; i < this.files.length; i++) {
        let file = this.files[i];
        formData.append("files", file);
      }
      for (var i = 0; i < this.secondFiles.length; i++) {
        let file = this.secondFiles[i];
        formData.append("secondFiles", file);
      }
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      for (var i = 0; i < this.forthFiles.length; i++) {
        let file = this.forthFiles[i];
        formData.append("forthFiles", file);
      }
      const maintenanceState = this.getMaintainaceData();
      const body = {
        ...vm.mold,
        ...maintenanceState,
        poDate: vm.mold.poDate
          ? moment(vm.mold.poDate).format("YYYYMMDD")
          : null,
      };
      console.log("getMaintainaceData", maintenanceState);
      console.log("body", body);
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      formData.append("payload", JSON.stringify(body));
      return formData;
    },

    getMaintainaceData() {
      const data = {};
      data.pmStrategy = this.selectedMaintenanceIntervalType;
      if (data.pmStrategy === "TIME_BASED") {
        data.pmFrequency = this.maintenanceFrequency.frequency;
        data.schedStartDate = moment(
          this.currentDate.fromTitle,
          "YYYY-MM-DD"
        ).format("YYYYMMDD");
        data.schedInterval = this.numberOfSchedule;
        data.schedOrdinalNum = this.scheduleAt;
        if (data.pmFrequency === "WEEKLY") {
          delete data.schedOrdinalNum;
        }
        data.schedDayOfWeek = this.scheduleDay;
        data.recurrConstraintType = this.continueFrequency.value;
        data.recurrNum = this.continueTime;
        data.recurrDueDate = moment(
          this.continueDate.fromTitle,
          "YYYY-MM-DD"
        ).format("YYYYMMDD");
        if (data.recurrConstraintType === "FOREVER") {
          delete data.recurrNum;
          delete data.recurrDueDate;
        } else if (data.recurrConstraintType === "UNTIL") {
          delete data.recurrNum;
        } else if (data.recurrConstraintType === "FOR") {
          delete data.recurrDueDate;
        }
        data.schedUpcomingTolerance = this.schedUpcomingTolerance;
        return data;
      } else {
        return data;
      }
    },

    findPart: function (index) {
      var child = Common.vue.getChild(this.$children, "part-search");
      if (child != null) {
        child.findPart(index);
      }
    },

    addPart: function () {
      var part = {
        moldId: "",
        partId: "",
        partName: "",
        cavity: this.partValueInit.cavity || "",
        totalCavities:
          this.partValueInit.totalCavities || this.mold.totalCavities || "",
        weight: 1,
      };
      this.mold.parts.push(part);
      //this.mold.parts.push(part);
    },
    removePart: function (index) {
      if (this.mold.parts.length > 1) {
        this.mold.parts.splice(index, 1);
        //this.mold.parts.splice(index, 1);
      }
    },

    cancel: function () {
      // location.href = newPageUrl.TOOLING;
      history.back();
    },

    findLocation: function () {
      var child = Common.vue.getChild(this.$children, "location-search");
      if (child != null) {
        child.tab("active");
      }
    },

    findCounter: function () {
      var child = Common.vue.getChild(this.$children, "counter-search-tooling");
      if (child != null) {
        child.tab("all");
      }
    },
    callbackCounterTooling: function (counter) {
      console.log("callbackCounter2" + JSON.stringify(counter));
      vm.mold.counterId = counter.id;
      vm.mold.counterCode = counter.equipmentCode;
    },
    callbackLocation: function (location) {
      console.log("location" + JSON.stringify(location));
      vm.mold.locationId = location.id;
      vm.mold.locationName = location.name;
    },

    callbackLocation: function (location) {
      console.log("location2" + JSON.stringify(location));
      this.mold.locationId = location.id;
      this.mold.locationName = location.name;
    },
    callbackPart: function (index, part) {
      for (var i = 0; i < this.mold.parts.length; i++) {
        if (i == index) {
          continue;
        }

        if (this.mold.parts[i].partId == part.id) {
          Common.alert("This part has already been added.");
          return false;
        }
      }

      //this.mold.moldParts[index].partId = part.id;
      //this.mold.moldParts[index].partName= part.name;

      this.mold.parts[index].partId = part.id;
      this.mold.parts[index].partName = part.name;
      this.mold.parts[index].weight = part.weight;
      this.mold.parts[index].partWeight = part.partWeight;
      this.mold.parts[index].nameShow = part.name
        ? `${part.partCode} (${part.name})`
        : part.partCode;
      this.autoShotSize();
      console.log("auto shot size parts", part);
      this.autoMaxCapacityPerWeek();
      return true;
    },
    callbackCounter: function (counter) {
      this.mold.counterId = counter.id;
      this.mold.counterCode = counter.equipmentCode;
    },

    findCompany: function (searchType, valueType) {
      var child = Common.vue.getChild(this.$children, "company-search");
      if (child != null) {
        child.findCompany(searchType, valueType);
      }
    },
    callbackCompany: function (company, searchType, valueType) {
      if ("TOOL_MAKER" == valueType) {
        this.mold.toolMakerCompanyId = company.id;
        this.mold.toolMakerCompanyName = company.name;
      } else if ("SUPPLIER" == valueType) {
        this.mold.supplierCompanyId = company.id;
        this.mold.supplierCompanyName = company.name;
      }
    },

    //second
    deleteSecondFiles: function (index) {
      vm.secondFiles = vm.secondFiles.filter((value, idx) => idx !== index);
    },

    selectedSecondFiles: function (e) {
      var files = e.target.files;
      if (files) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.secondFiles.push(selectedFiles[i]);
        }
      }
    },

    //thirdFiles
    deleteThirdFiles: function (index) {
      vm.thirdFiles = vm.thirdFiles.filter((value, idx) => idx !== index);
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      if (files) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.thirdFiles.push(selectedFiles[i]);
        }
      }
    },

    selectedFile: function (e) {
      var files = e.target.files;

      if (files) {
        var selectedFiles = Array.from(files);

        for (var i = 0; i < selectedFiles.length; i++) {
          vm.files.push(selectedFiles[i]);
        }
      }
    },
    deleteFile: function (f, index) {
      var newFiles = [];
      for (var i = 0; i < vm.files.length; i++) {
        var file = vm.files[i];
        if (i != index) {
          console.log("splice: " + i + " : " + (i + 1));
          //vm.files.splice(i, 1);

          newFiles.push(file);
        }
      }

      vm.files = newFiles;
    },
    deleteFileStorage: function (file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case vm.fileTypes.MOLD_CONDITION:
              vm.toolingConditionFiles = response.data;
              break;
            case vm.fileTypes.MOLD_MAINTENANCE_DOCUMENT:
              vm.documentFiles = response.data;
              break;
            case vm.fileTypes.MOLD_INSTRUCTION_VIDEO:
              vm.toolingPictureFile = response.data;
              break;
          }
        });
    },

    autoMaxCapacityPerWeek: function () {
      const {
        productionDays,
        contractedCycleTime,
        toolmakerContractedCycleTime,
        parts: partsMold,
        shiftsPerDay,
        uptimeTarget,
      } = this.mold;
      let parts = partsMold || [];
      let cavity =
        parts.length > 0 && parts[0].cavity ? Number(parts[0].cavity) : 1;
      parts.forEach((value, index) => {
        if (!isNaN(value.cavity) && index > 0) {
          cavity += Number(value.cavity);
        }
      });
      console.log("cavity", cavity);
      this.autoShotSize();
      console.log(
        !isNaN(productionDays),
        !isNaN(shiftsPerDay),
        !isNaN(cavity),
        !isNaN(contractedCycleTime),
        !isNaN(uptimeTarget)
      );

      if (
        productionDays &&
        !isNaN(productionDays) &&
        shiftsPerDay &&
        !isNaN(shiftsPerDay) &&
        cavity &&
        !isNaN(cavity) &&
        ((contractedCycleTime && !isNaN(contractedCycleTime)) ||
          (toolmakerContractedCycleTime &&
            !isNaN(toolmakerContractedCycleTime))) &&
        uptimeTarget &&
        !isNaN(uptimeTarget)
      )
        this.mold.maxCapacityPerWeek =
          ((Number(productionDays) *
            Number(shiftsPerDay) *
            3600 *
            (Number(uptimeTarget) / 100)) /
            (Number(
              this.getContractedCycleTime(
                contractedCycleTime,
                toolmakerContractedCycleTime
              )
            ) /
              10)) *
          Number(cavity);
      this.mold.maxCapacityPerWeek = Math.ceil(
        Number(this.mold.maxCapacityPerWeek)
      );
    },
    getContractedCycleTime: function (
      contractedCycleTime,
      toolmakerContractedCycleTime
    ) {
      return contractedCycleTime
        ? contractedCycleTime
        : toolmakerContractedCycleTime;
    },
    autoShotSize: function () {
      const { weightRunner, parts: partsMold } = this.mold;
      let parts = partsMold || [];

      let totalPartWeightCaviteis = 0;
      console.log("parts", parts);

      let sumCavities = 0;
      parts.forEach((part) => {
        if (part.partWeight) {
          let partWeightPart = part.partWeight.split(" ");

          if (partWeightPart.length === 2) {
            let number = Number(partWeightPart[0]);
            let unit = partWeightPart[1];
            totalPartWeightCaviteis +=
              this.convertWeightUnitToGram(unit, number) * part.cavity;
          }
        }
        if (!isNaN(part.cavity)) {
          sumCavities += part.cavity;
        }
      });
      console.log("weightRunner", weightRunner);
      let shotWeight = null;
      if (
        weightRunner != null &&
        !isNaN(weightRunner) &&
        String(weightRunner).trim() != ""
      ) {
        shotWeight =
          totalPartWeightCaviteis + Number(weightRunner) * sumCavities;
      }
      console.log("shotWeight", shotWeight);
      this.mold.shotSize = shotWeight;
    },

    convertWeightUnitToGram(unit, value) {
      switch (unit.toLowerCase()) {
        case "ounce":
          return value * 28.3495;
        case "pound":
          return value * 453.592;
        case "kg":
          return value * 1000;
        case "gram":
          return value;
        default:
          return value;
      }
    },

    initDefaultValue() {
      const fields = [
        "equipmentCode",
        "accumulatedShots",
        "supplierMoldCode",
        "toolingLetter",
        "toolingType",
        "toolingComplexity",
        "designedShot",
        "lifeYears",
        "madeYear",
        "approvedCycleTime",
        "toolmakerApprovedCycleTime",
        "locationId",
        "toolDescription",
        "size",
        "weight",
        "shotSize",
        "toolMakerCompanyName",
        "injectionMachineId",
        "quotedMachineTonnage",
        "runnerType",
        "runnerMaker",
        "weightRunner",
        "hotRunnerDrop",
        "hotRunnerZone",
        "toolingPictureFile",
        "preventCycle",
        "preventUpcoming",
        "preventOverdue",
        "cycleTimeLimit1",
        "cycleTimeLimit2",
        "useageType",
        "engineers",
        "documentFiles",
        "instructionVideo",
        "cost",
        "memo",
        "supplierCompanyName",
        "uptimeTarget",
        "uptimeLimitL1",
        "uptimeLimitL2",
        "labour",
        "shiftsPerDay",
        "productionDays",
        "maxCapacityPerWeek",
        "partId",
        "cavity",
        "totalCavities",
      ];
      // get current config value
      axios
        .get("/api/config?configCategory=" + this.configCategory)
        .then((response) => {
          if (response.data && response.data.length > 0) {
            // console.log('form response data', this.categories);
            console.log("form response data", response.data);
            response.data.forEach((field) => {
              if (field.required) {
                this.requiredFields.push(field.fieldName);
              }
              if (this.checkDeleteTooling != null)
                this.checkDeleteTooling[field.fieldName] = field.deletedField;
            });
            if (!Page.IS_EDIT) {
              fields.forEach((field) => {
                let defaultField = response.data.filter(
                  (item) => item.fieldName === field
                )[0];
                console.log("defaultField", defaultField);
                if (!defaultField) return;
                if (
                  defaultField.defaultInput &&
                  defaultField.defaultInputValue
                ) {
                  if (defaultField.fieldName === "cavity") {
                    this.mold.parts.forEach((part) => {
                      part.cavity = defaultField.defaultInputValue;
                    });
                    this.partValueInit.cavity = defaultField.defaultInputValue;
                  } else if (defaultField.fieldName === "totalCavities") {
                    this.mold.parts.forEach((part) => {
                      part.totalCavities = defaultField.defaultInputValue;
                    });
                    this.partValueInit.totalCavities =
                      defaultField.defaultInputValue;
                  } else {
                    this.mold[defaultField.fieldName] =
                      defaultField.defaultInputValue;
                    if (this.mold.size) {
                      const sizes = this.mold.size.split("x");
                      if (sizes.length >= 3) {
                        this.mold.sizeWidth = sizes[0].trim();
                        this.mold.sizeDepth = sizes[1].trim();
                        this.mold.sizeHeight = sizes[2].trim();
                      }
                    }
                  }
                }
              });
            } else {
              fields.forEach((field) => {
                let defaultField = response.data.filter(
                  (item) => item.fieldName === field
                )[0];
                console.log("defaultField", defaultField);
                if (!defaultField) return;
                if (
                  defaultField.defaultInput &&
                  defaultField.defaultInputValue
                ) {
                  if (defaultField.fieldName === "cavity") {
                    this.partValueInit.cavity = defaultField.defaultInputValue;
                  } else if (defaultField.fieldName === "totalCavities") {
                    this.partValueInit.totalCavities =
                      defaultField.defaultInputValue;
                  }
                }
              });
            }
          }
          //fix lost required
          if (
            this.checkDeleteTooling != null &&
            Object.entries(this.checkDeleteTooling).length > 0
          ) {
            setTimeout(function () {
              // Common.initRequireBadge();
            }, 100);
          }
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    accumulatedMaintenanceCostView(mold) {
      mold.accumulatedMaintenanceCost = mold.accumulatedMaintenanceCost || 0;
      let currencyTitle =
        mold.costCurrencyTypeTitle != null ? mold.costCurrencyTypeTitle : "$";
      if (this.codes) {
        this.codes.CurrencyType.forEach((code) => {
          if (code.code == mold.costCurrencyType) currencyTitle = code.title;
        });
      }
      mold.accumulatedMaintenanceCostView =
        currencyTitle + mold.accumulatedMaintenanceCost;
      return mold.accumulatedMaintenanceCostView;
    },
  },
  computed: {
    isNew() {
      return Page.IS_NEW;
    },
    getListFrequency() {
      const listFrequency = this.maintenanceFrequencies.filter((item) =>
        ["WEEKLY", "MONTHLY"].includes(item.value)
      );
      return listFrequency;
    },
    readonly: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },
    emptyValue: function () {},
    currencyTypeList() {
      if (this.codes.CurrencyType) {
        console.log("this.codes.CurrencyType", this.codes);
        return this.codes.CurrencyType.map((o) => ({
          ...o,
          value: o.code,
          title: o.code,
        }));
      } else return [];
    },
    poDateDisplay() {
      console.log("this.mold.poDate", this.mold.poDate);
      if (!this.mold.poDate) return "Select Date";
      else {
        return moment(this.mold.poDate).format("YYYY-MM-DD");
      }
    },
    isErrorSalvageValue() {
      return (
        this.mold.salvageValue && +this.mold.salvageValue > +this.mold.cost
      );
    },
    isToolingComplexityEnabled() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.toolingComplexityEnabled
      );
    },
    isLifeYearsEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.lifeYearsEnabled);
    },
    isUptimeTargetRequired() {
      return Boolean(this.options?.CLIENT?.moldDetail?.uptimeTargetRequired);
    },
    isDisapprovedReasonRequired() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.disapprovedReasonRequired
      );
    },
  },

  watch: {
    "mold.approvedCycleTime": function (newValue) {
      if (newValue === "") {
        this.mold.contractedCycleTime = newValue;
      } else {
        this.mold.contractedCycleTime = Number(newValue) * 10;
      }
    },
    "mold.toolmakerApprovedCycleTime": function (newValue) {
      if (newValue === "") {
        this.mold.toolmakerContractedCycleTime = newValue;
      } else {
        this.mold.toolmakerContractedCycleTime = Number(newValue) * 10;
      }
    },
    "mold.shiftsPerDay": function (newValue) {
      console.log("newValue: ", newValue);
      this.autoMaxCapacityPerWeek();
    },
    "mold.productionDays": function (newValue) {
      this.autoMaxCapacityPerWeek();
    },
    "mold.contractedCycleTime": function (newValue) {
      this.autoMaxCapacityPerWeek();
    },
    "mold.uptimeTarget": function () {
      this.autoMaxCapacityPerWeek();
    },
    "mold.parts": function (newValue) {
      this.autoMaxCapacityPerWeek();
      console.log("parts list");
      this.autoShotSize();
    },

    "mold.weightRunner": function () {
      console.log("mold.weightRunner trigger");
      this.autoShotSize();
    },
    "mold.totalCavities": function (newValue) {
      if (this.mold.parts) {
        this.mold.parts.forEach((element) => {
          element.totalCavities = newValue;
        });
      }
    },

    mold: function (newValue) {
      console.log("new mold", newValue);
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.codes = Object.assign({}, this.codes, newVal);
          this.codes.OutsideUnit.forEach((item) => {
            if (item.title === "s") {
              this.cycleTimeList.push({ title: "second", code: item.code });
            } else {
              this.cycleTimeList.push({ title: item.title, code: item.code });
            }
          });
          this.codes.CurrencyType.forEach((item) => {
            this.costCurrencyList.push({ title: item.code, code: item.code });
          });
          this.cost_currency = this.codes.CurrencyType.find(
            (item) => item.code === this.mold.costCurrencyType
          ).code;
          const findWreightUnit = this.codes.WeightUnit.find(
            (item) => item.code === this.mold.weightUnit
          );
          this.weight_unit = findWreightUnit ? findWreightUnit.title : "";
          this.tool_size = this.codes.SizeUnit.find(
            (item) => item.code === this.mold.sizeUnit
          ).title;
          const findIndexUnit = this.codes.OutsideUnit.findIndex(
            (value) => value.code == "SECOND"
          );
          if (findIndexUnit !== -1) {
            this.mold.cycleTimeLimit2Unit =
              this.codes.OutsideUnit[findIndexUnit].code;
            this.mold.cycleTimeLimit1Unit =
              this.codes.OutsideUnit[findIndexUnit].code;
          }
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.options = Object.assign({}, this.options, newVal);
          const condition = Boolean(
            this.options?.CLIENT?.moldDetail?.uptimeTargetRequired
          );
          if (condition) {
            this.mold.preventUpcoming = "10000";
            this.mold.preventOverdue = "5000";
            const currentUrl = window.location.href;
            if (currentUrl.endsWith("/new")) {
              this.mold.cycleTimeLimit1 = "2";
              this.mold.cycleTimeLimit2 = "5";
              this.mold.uptimeLimitL1 = "3";
              this.mold.uptimeLimitL2 = "5";
            }
          }
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.userType,
      (newVal) => {
        if (newVal) {
          this.userType = newVal;
          this.isToolMaker = this.userType === "TOOL_MAKER";
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.currentUser,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.roles = newVal.roles;
          this.company = newVal.company;
          if (this.company.companyType === "TOOL_MAKER") {
            this.isToolMaker = true;
            this.mold.toolMakerCompanyId = this.company.id;
            this.mold.toolMakerCompanyName = this.company.name;
          }
        }
      },
      { immediate: true }
    );
  },
  mounted() {
    // TODO: NEED REFACTOR
    // USING DEPRECATED METHOD
    // CONFUSING IMPLEMENT
    // TOO MANY NESTED FUNCTION
    this.$nextTick(async function () {
      // 데이터 조회
      var self = this;
      let url_string = window.location.href;
      const objectId = /[^/]*$/.exec(url_string)[0];
      this.isCreate = objectId === "new";
      if (this.isCreate) {
        axios
          .get("/api/custom-field?objectType=TOOLING")
          .then(function (response) {
            self.customFieldList = response.data;
          });
      } else {
        axios
          .get(
            `/api/custom-field-value/list-by-object?objectType=TOOLING&objectId=${objectId}`
          )
          .then(function (response) {
            self.customFieldList = response.data.map((item) => ({
              propertyGroup: item.propertyGroup,
              fieldName: item.fieldName,
              defaultInputValue:
                item.customFieldValueDTOList.length !== 0
                  ? item.customFieldValueDTOList[0].value
                  : null,
              id: item.id,
              required: item.required,
            }));
          });
      }

      try {
        const server = await Common.getSystem("server");
        self.serverName = server;
        console.log("self.serverName", server);
        if (server === "dyson") {
          self.toolingComplexityList = [
            { title: "A+" },
            { title: "A" },
            { title: "B" },
            { title: "C" },
            { title: "D" },
          ];
        } else {
          self.toolingComplexityList = [
            { title: "A+" },
            { title: "A" },
            { title: "B" },
            { title: "C" },
          ];
        }

        // set default value
        Common.getCategoryConfigStatus().then((data) => {
          console.log(
            "🚀 ~ file: form.js:1552 ~ Common.getCategoryConfigStatus ~ data:",
            data
          );
          if (data && Object.keys(data).length > 0) {
            const currentConfig = data[self.configCategory];
            if (currentConfig && currentConfig.enabled) {
              self.initDefaultValue();
            }
          }
        });

        // TODO: NEED REFACTOR: CONFUSING SCOPE
        // NESTED WITH SAME NAME response
        await axios
          .get("/api/users/engineers")
          .then(async function (responseEngineers) {
            console.log(
              "%c engineers ",
              "background: green",
              responseEngineers
            );
            self.engineers = responseEngineers.data;
            self.engineers.map((item) => (item.title = item.name));
            if (Page.IS_EDIT) {
              await axios.get(Page.API_GET).then(async function (response) {
                response.data.approvedCycleTime =
                  response.data.contractedCycleTimeSeconds;
                response.data.toolmakerApprovedCycleTime =
                  response.data.toolmakerContractedCycleTimeSeconds;

                self.mold = Object.assign({}, self.mold, {
                  ...response.data,
                  poDate: response.data.poDateFormat
                    ? moment(response.data.poDateFormat, "YYYYMMDD")
                    : undefined,
                });
                self.mold.accumulatedShots = response.data.lastShot || 0;
                self.selectedBackUpItem = response.data.backup
                  ? { id: 2, title: "Is a backup tooling", value: true }
                  : { id: 1, title: "Not a backup tooling", value: false };
                if (self.mold.dataSubmission === "DISAPPROVED") {
                  await axios
                    .get(
                      `/api/molds/data-submission/${self.mold.id}/disapproval-reason`
                    )
                    .then(async function (response) {
                      console.log("response: reson: ", response);
                      self.reasonData = response.data;
                      self.reasonData.approvedAt = self.reasonData.approvedAt
                        ? moment
                            .unix(self.reasonData.approvedAt)
                            .format("MMMM DD YYYY HH:mm:ss")
                        : "-";
                    });
                } else if (self.mold.dataSubmission === "APPROVED") {
                  axios
                    .get(
                      `/api/molds/data-submission/${self.mold.id}/approval-reason`
                    )
                    .then(function (response) {
                      console.log("response: reson: ", response);
                      self.reasonData = response.data;
                      self.reasonData.approvedAt = self.reasonData.approvedAt
                        ? moment
                            .unix(self.reasonData.approvedAt)
                            .format("MMMM DD YYYY HH:mm:ss")
                        : "-";
                    });
                }
                console.log("response.data: ", response.data, self.mold);
                console.log("response.data mold ", self.mold);

                // PM mapping field //

                let moldPmPlanResponse = await axios.get(
                  `/api/molds/${self.mold.id}/mold-pm-plan`
                );
                let moldPmPlan = moldPmPlanResponse?.data?.data;
                self.selectedMaintenanceIntervalType = moldPmPlan
                  ? moldPmPlan.pmStrategy
                  : "SHOT_BASED";
                self.defaultMaintenanceIntervalType = "SHOT_BASED";
                if (moldPmPlan) {
                  if (moldPmPlan.pmStrategy === "TIME_BASED") {
                    self.defaultMaintenanceIntervalType = "TIME_BASED";
                  }
                }
                console.log(
                  "defaultMaintenanceIntervalType",
                  self.defaultMaintenanceIntervalType
                );
                if (moldPmPlan && moldPmPlan.pmFrequency) {
                  self.maintenanceFrequency = self.maintenanceFrequencies.find(
                    (item) => item.frequency === moldPmPlan.pmFrequency
                  );
                }
                if (moldPmPlan) {
                  if (moldPmPlan.recurrDueDate) {
                    self.currentDate = {
                      from: moment(
                        moldPmPlan.recurrDueDate,
                        "YYYYMMDD"
                      ).toDate(),
                      to: moment(moldPmPlan.recurrDueDate, "YYYYMMDD").toDate(),
                      fromTitle: moment(
                        moldPmPlan.recurrDueDate,
                        "YYYYMMDD"
                      ).format("YYYY-MM-DD"),
                      toTitle: moment(
                        moldPmPlan.recurrDueDate,
                        "YYYYMMDD"
                      ).format("YYYY-MM-DD"),
                    };
                  }
                  if (moldPmPlan.schedInterval) {
                    self.numberOfSchedule = moldPmPlan.schedInterval;
                  }
                  if (moldPmPlan.schedOrdinalNum) {
                    self.scheduleAt = moldPmPlan.schedOrdinalNum;
                  }
                  if (moldPmPlan.schedDayOfWeek) {
                    self.scheduleDay = moldPmPlan.schedDayOfWeek;
                  }
                  if (moldPmPlan.recurrConstraintType) {
                    self.continueFrequency = self.continueFrequencies.find(
                      (item) => item.value === moldPmPlan.recurrConstraintType
                    );
                  }
                  if (moldPmPlan.schedStartDate) {
                    self.currentDate = {
                      from: moment(
                        moldPmPlan.schedStartDate,
                        "YYYYMMDD"
                      ).toDate(),
                      to: moment(
                        moldPmPlan.schedStartDate,
                        "YYYYMMDD"
                      ).toDate(),
                      fromTitle: moment(
                        moldPmPlan.schedStartDate,
                        "YYYYMMDD"
                      ).format("YYYY-MM-DD"),
                      toTitle: moment(
                        moldPmPlan.schedStartDate,
                        "YYYYMMDD"
                      ).format("YYYY-MM-DD"),
                    };
                  }
                  self.schedUpcomingTolerance =
                    moldPmPlan.schedUpcomingTolerance || 0;
                  self.continueTime = moldPmPlan.recurrNum || 3;
                }

                // END PM mapping field //
                if (response.data.size) {
                  const sizes = response.data.size.split("x");
                  if (sizes.length >= 3) {
                    self.mold.sizeWidth = sizes[0].trim();
                    self.mold.sizeDepth = sizes[1].trim();
                    self.mold.sizeHeight = sizes[2].trim();
                  }
                }
                self.mold.costCurrencyType =
                  self.mold.costCurrencyType || "USD";
                self.mold.accumulatedMaintenanceCost =
                  self.mold.accumulatedMaintenanceCost || 0;

                if (response?.data?.engineers?.length) {
                  self.mold.engineerIds = response.data.engineers.map(
                    (i) => i.id
                  );
                }

                if (response?.data?.plantEngineers?.length) {
                  self.mold.plantEngineerIds = response.data.plantEngineers.map(
                    (i) => i.id
                  );
                }

                //uptimeTarget
                if (self.mold.uptimeTarget == null) {
                  self.mold.uptimeTarget = 90;
                }

                //uptimeLimitL1
                if (self.mold.uptimeLimitL1 == null) {
                  self.mold.uptimeLimitL1 = 5;
                }
                if (self.mold.uptimeLimitL2 == null) {
                  self.mold.uptimeLimitL2 = 10;
                }

                var authorities = [];
                for (var i = 0; i < response.data.moldAuthorities.length; i++) {
                  var moldAuthority = response.data.moldAuthorities[i];
                  authorities.push(moldAuthority.authority);
                }
                self.mold.authorities = authorities;

                if (self.mold.counter != null) {
                  self.mold.counterId = self.mold.counter.id;
                  self.mold.counterCode = self.mold.counter.equipmentCode;
                }

                if (self.mold.part != null) {
                  self.mold.partId = self.mold.part.id;
                  self.mold.partName = self.mold.part.name;
                }

                console.log("self.mold.parts", self.mold.parts);
                if (self.mold.parts.length > 0) {
                  self.mold.parts = self.mold.parts.map((value) => {
                    value.nameShow = value.partName
                      ? `${value.partCode} (${value.partName})`
                      : value.partCode;
                    return value;
                  });
                } else {
                  self.mold.parts = [
                    {
                      moldId: "",
                      partId: "",
                      partName: "",
                      cavity: "",
                      totalCavities: "",
                    },
                  ];
                }
                console.log("self.mold.parts after update", self.mold.parts);
                if (self.mold.upperLimitTemperatureUnit == null) {
                  self.mold.upperLimitTemperatureUnit = "CELSIUS";
                }
                if (self.mold.lowerLimitTemperatureUnit == null) {
                  self.mold.lowerLimitTemperatureUnit = "CELSIUS";
                }

                var param = {
                  storageTypes:
                    "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION,MOLD_PO_DOCUMENT",
                  refId: self.mold.id,
                };

                await axios
                  .get("/api/file-storage/mold?" + Common.param(param))
                  .then(function (response) {
                    console.log("response: ", response);
                    vm.documentFiles = response.data[
                      "MOLD_MAINTENANCE_DOCUMENT"
                    ]
                      ? response.data["MOLD_MAINTENANCE_DOCUMENT"]
                      : [];
                    vm.toolingConditionFiles = response.data["MOLD_CONDITION"]
                      ? response.data["MOLD_CONDITION"]
                      : [];
                    vm.toolingPictureFile = response.data[
                      "MOLD_INSTRUCTION_VIDEO"
                    ]
                      ? response.data["MOLD_INSTRUCTION_VIDEO"]
                      : [];
                    vm.costDocumentFiles = response.data["MOLD_PO_DOCUMENT"]
                      ? response.data["MOLD_PO_DOCUMENT"]
                      : [];
                  });
              });
            }
          });
      } catch (error) {
        console.log(error);
      }
      self.cycle_time =
        self.cycleTimeList.find(
          (item) => item.code === self.mold.cycleTimeLimit1Unit
        )?.title || "Unit";
      self.cycle_time2 =
        self.cycleTimeList.find(
          (item) => item.code === self.mold.cycleTimeLimit2Unit
        )?.title || "Unit";
      if (
        self.mold.toolingComplexity == "" &&
        self.isToolingComplexityEnabled
      ) {
        self.mold.toolingComplexity = "Select Tooling complexity";
      }
    });
  },
});
