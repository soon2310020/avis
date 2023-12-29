axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
window.onload = function () {
  document.title = "Overall Data Registration Statistics";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};
var Page = Common.getPage("data-registration");
var vm = new Vue({
  el: "#app",
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "company-details-form": httpVueLoader(
      "/components/company-details-form.vue"
    ),
    "all-companies-dropdown": httpVueLoader(
      "/components/all-companies-dropdown.vue"
    ),
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "part-details": httpVueLoader("/components/part-details.vue"),
    "part-details-form": httpVueLoader("/components/part-details-form.vue"),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "file-previewer2": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    "mold-details-form": httpVueLoader("/components/mold-details-form.vue"),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "machine-details-form": httpVueLoader(
      "/components/machine-details-form.vue"
    ),
    "location-form": httpVueLoader("/components/location-form.vue"),
    "category-form": httpVueLoader("/components/category-form.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "order-modal": httpVueLoader("/components/order-modal.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "company-filter": httpVueLoader(
      "/components/chart-new/data-completion-rate/company-filter.vue"
    ),
    "category-form-modal": httpVueLoader(
      "/components/category/category-form-modal.vue"
    ),
    "project-form-modal": httpVueLoader(
      "/components/category/project-form-modal.vue"
    ),
  },
  data: {
    categoryList: [
      {
        plural: "Companies",
        singular: "Company",
        code: "companies",
        img: "globe@2x",
        number: 0,
        id: 1,
      },
      {
        plural: "Locations",
        singular: "Location",
        code: "locations",
        img: "push-pin@2x",
        number: 0,
        id: 2,
      },
      // { plural: 'Categories', singular: 'Category', code: 'categories', img: 'list@2x', number: 0, id: 3 },
      {
        plural: "Parts",
        singular: "Part",
        code: "parts",
        img: "pie-chart@2x",
        number: 0,
        id: 4,
      },
      {
        plural: "Toolings",
        singular: "Tooling",
        code: "molds",
        img: "four-squares-button-of-view-options@2x",
        number: 0,
        id: 5,
      },
      {
        plural: "Machines",
        singular: "Machine",
        code: "machines",
        img: "robotics@2x",
        number: 0,
        id: 6,
      },
    ],
    categorySelected: "companies",
    companies: null,
    locations: null,
    categories: null,
    parts: null,
    molds: null,
    machines: null,
    codes: {},
    userType: "",
    total: 0,
    results: [],
    pagination: [],
    // serverName: '',
    requestParam: {
      query: "",
      status: "active",
      sort: "id,desc",
      page: 1,
      objectType: "COMPANY",
      companyType: "IN_HOUSE",
    },
    sortType: {
      NAME: "name",
      RATE: "rate",
      RQ: "",
      COMPANY_CODE: "companyCode",
      ADDRESS: "address",
      STATUS: "enabled",
      LOCATION_CODE: "locationCode",
      COMPANY: "company.name",
      NO_TERMINAL: "numberTerminal",
      MANAGER: "manager",
      DESCRIPTION: "description",
      CATEGORY: "category.parent.name",
      PART: "partCode",
      EQUIPMENT_CODE: "equipmentCode",
      COMPANY_LOCATION: "location.company.name",
      LOCATION_NAME: "location.name",
      PART2: "part",
    },
    requestId: "",
    currentSortType: "id",
    isDesc: true,
    allColumnList: [],
    pageType: "CHECKLIST_MAINTENANCE",
    resources: {},
    visible: false,
    dropdownOpening: false,
    isType: "TOOLING",
    href: "",
    createText: "",
    importText: "",
    configChart: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            backgroundColor: [],
          },
        ],
      },
      options: {
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart1: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart2: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart3: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart4: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart5: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    configChart6: {
      type: "doughnut",
      data: {
        plugins: {
          datalabels: {
            display: false,
          },
        },
        datasets: [
          {
            data: [],
            borderWidth: 0,
            backgroundColor: [],
          },
        ],
      },
      options: {
        cutoutPercentage: 75,
        cutout: 75,
        plugins: {
          labels: {
            display: false,
            enabled: false,
            render: "label",
            arc: false,
            position: "outside",
            fontSize: 13,
            outsidePadding: 4,
            textMargin: 4,
          },
        },
        tooltips: {
          enabled: false,
        },
        legend: {
          display: false,
        },
        title: {
          display: false,
        },
        responsive: true,
      },
    },
    avgRate: 0,
    dataConvert: {
      data: [
        { title: "Incomplete", data: this.avgRate },
        { title: "Complete", data: 100 - this.avgRate },
      ],
      // colors: ["#5D43F7", "#C1C7CD"]
      // colors: ["#707070", "#C1C7CD"]
    },
    chartData: [],
    filterStatus: {
      sort: false,
    },
    loading: false,
    companyList: [],
    listRate: {},
    companySelectedList: "",
    listChecked: [],
    listCheckedTracked: {},
    listCheckedFull: [],
    isAll: false,
    isAllTracked: [],
    toolingItem: null,
    x: false,
    listReceivedOrder: [],
    listCreatedOrder: {},
    totalKey: 0,
    currentUser: null,
    currentUserType: null,
    companyFullData: [],
    locationFullData: [],
    toolingFullData: [],
    machineFullData: [],
    openOrderModal: false,
    stop: false,
    colorList: [
      "#AFACFF",
      "#FF7171",
      "#DB91FC",
      "#FFACAC",
      "#FFB371",
      "#FFACF2",
      "#FFACE5",
      "#86C7FF",
      "#62B0D9",
      "#ACBFFF",
      "#FF8699",
      "#90E5AA",
      "#CCACFF",
      "#74AEFC",
      "#E0CD7E",
      "#FF8489",
      "#FFA167",
      "#64D0C3",
      "#86B0FF",
      "#D2F8E2",
      "#C69E9E",
      "#FFC68D",
      "#F8D2D2",
      "#E58383",
    ],
    isHover: null,
    categoryDropdown: [],
    displayCaret: false,
    showAnimationPopover: null,
    visiblePopover: false,
    options: {},
  },
  async created() {
    this.getCurrentUser();
    await this.getCurrentUserType();
    this.getListReceived();
    this.getListCreated();
    this.getCompanyFull();
    this.getLocationFull();
    this.getToolingFull();
    this.getMachineFull();
    await Promise.all([
      this.getCompany(),
      this.getCompanyDropdown(),
      this.getLocations(),
      this.getCategories(),
      this.getParts(),
      this.getMolds(),
      this.getMachines(),
    ]);
    this.categoryList = [
      {
        plural: "Companies",
        singular: "Company",
        code: "companies",
        img: "globe@2x",
        number: this.companies.totalElements,
        id: 1,
      },
      {
        plural: "Locations",
        singular: "Location",
        code: "locations",
        img: "push-pin@2x",
        number: this.locations.totalElements,
        id: 2,
      },
      // { plural: 'Categories', singular: 'Category', code: 'categories', img: 'list@2x', number: this.categories.totalElements, id: 3 },
      {
        plural: "Parts",
        singular: "Part",
        code: "parts",
        img: "pie-chart@2x",
        number: this.parts.totalElements,
        id: 4,
      },
      {
        plural: "Toolings",
        singular: "Tooling",
        code: "molds",
        img: "four-squares-button-of-view-options@2x",
        number: this.molds.totalElements,
        id: 5,
      },
      {
        plural: "Machines",
        singular: "Machine",
        code: "machines",
        img: "robotics@2x",
        number: this.machines.totalElements,
        id: 6,
      },
    ];
    this.$forceUpdate();
    console.log("Current User: ", this.currentUser);
  },
  watch: {
    avgRate: function (newVal) {
      this.dataConvert = {
        data: [
          { title: "Incomplete", data: this.avgRate },
          { title: "Complete", data: 100 - this.avgRate },
        ],
      };
      this.bindChartPreventive(this.dataConvert.data);
      this.changeColor(this.requestParam.objectType);
    },
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this;
        const x = this.chartData.data.content.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
    visiblePopover(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    },
  },
  methods: {
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.visiblePopover) {
        this.showAnimationPopover = true;
        setTimeout(() => {
          this.showAnimationPopover = false;
        }, 700);
      }
    },
    convertName(name) {
      return Common.getAvatarName(name);
    },
    checkFlag(flag, item) {
      console.log("come");
      if (flag === null) {
        window.setTimeout(this.checkFlag(flag, item), 1000);
      } else {
        this.stop = true;
        this.showOrderPopup(item);
      }
    },
    repeatPending(self, item) {
      console.log(Common.vue.getChild(this.$children, "order-modal"));
      setTimeout(() => {
        if (Common.vue.getChild(this.$children, "order-modal") !== null) {
          self.showOrderPopup(item);
          return;
        } else {
          return this.repeatPending(self, item);
        }
      }, 2000);
    },
    getDataOrder() {
      if (this.listChecked.length > 0) {
        if (this.requestParam.objectType === "COMPANY") {
          return this.companyFullData;
        } else if (this.requestParam.objectType === "LOCATION") {
          return this.locationFullData;
        } else if (this.requestParam.objectType === "TOOLING") {
          return this.toolingFullData;
        } else if (this.requestParam.objectType === "MACHINE") {
          return this.machineFullData;
        } else {
          const dataFilter = this.chartData.data.content.filter(
            (res) => +res.rate < 100
          );
          return dataFilter;
        }
      } else {
        return [];
      }
    },
    async getDropdownCategory() {
      let self = this;
      await axios
        .get("/api/categories/uncompleted?size=1000")
        .then(function (response) {
          self.categoryDropdown = response.data.content;
          console.log("/api/categories/uncompleted 1", response);
          console.log("/api/categories/uncompleted 2", self.categoryDropdown);
        })
        .catch((error) => {
          console.log("/api/categories/uncompleted fail", response);
        });
    },
    async resetAfterUpdate() {
      console.log(this.chartData);
      // await this.getDataCompletion()
      let child = Common.vue.getChild(this.$children, "all-companies-dropdown");
      if (child != null) {
        // child.selectAll();
        child.onMouseleaveTooling("Reset");
      }
      // setTimeout(() => {
      document.querySelector("#app .op-list").style.display = "table-row-group";
      document.querySelector(".pagination").style.display = "flex";
      this.getRate();
      // }, 200);
    },
    getListCreated() {
      axios.get("/api/data-completion/list-created-orders").then((res) => {
        console.log(res.data, "res");
        this.listCreatedOrder = res.data.data;
      });
    },
    getListReceived() {
      axios.get("/api/data-completion/list-received-orders").then((res) => {
        this.listReceivedOrder = res.data.data;
      });
    },
    showOrderPopup(tab) {
      console.log(tab, "tabbbbbbbb");
      this.openOrderModal = true;
      var child = Common.vue.getChild(this.$children, "order-modal");
      console.log(child, "tabbbbbbbb");
      if (child != null) {
        child.showOrderPopup(tab);
      }
    },
    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.chartData.data.content.map((value) => value.id);
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
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
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
    },
    async getRate() {
      let self = this;
      return await axios
        .get("/api/data-completion/all-types")
        .then((data) => {
          const res = data.data.data;
          console.log("/api/data-completion/all-types", data);
          self.listRate = {
            companyRate: res[0].avgRate,
            locationRate: res[1].avgRate,
            categoryRate: res[2].avgRate,
            partRate: res[4].avgRate,
            toolingRate: res[5].avgRate,
            machineRate: res[3].avgRate,
          };
          let avgRate = self.roundNum(data.data.overall.toFixed(1));
          self.avgRate = avgRate;
          var overallChart = self.overallChart.data.datasets[0];
          overallChart.data = [avgRate, 100 - avgRate];
        })
        .catch((e) => {
          console.log(e, "getCompanyDropdown");
        })
        .finally(() => {});
    },
    getCompanyDropdown() {
      return axios
        .get(
          "/api/companies?query&size=999999&status=active&sort=id%2Cdesc&page=1&forReport=true"
        )
        .then((res) => {
          this.companyList = res.data.content;
        })
        .catch((e) => {
          console.log(e, "getCompanyDropdown");
        })
        .finally(() => {});
    },
    changeColor(item) {
      var backgroundColors = ["#5D43F7", "#C1C7CD"];
      var backgroundColorsDefault = ["#5D43F7", "#C1C7CD"];
    },
    updateAvg() {
      switch (this.requestParam.objectType) {
        case "COMPANY":
          this.listRate.companyRate = this.avgRate;
          break;
        case "TOOLING":
          this.listRate.toolingRate = this.avgRate;
          break;
        case "MACHINE":
          this.listRate.machineRate = this.avgRate;
          break;
        case "PART":
          this.listRate.partRate = this.avgRate;
          break;
        case "LOCATION":
          this.listRate.locationRate = this.avgRate;
          break;
      }
    },
    changeSelect(item) {
      if (this.requestParam.objectType !== item) {
        this.openOrderModal = false;
        // this.requestParam.page = 1;
        this.listChecked = [];
        this.isAll = false;
        this.currentSortType = "id";
        this.changeColor(item);
        this.requestParam.objectType = item;

        switch (item) {
          case "COMPANY":
            if (this.currentUserType === "OEM") {
              this.requestParam.companyType = "IN_HOUSE";
            } else if (this.currentUserType === "SUPPLIER") {
              this.requestParam.companyType = "SUPPLIER";
            } else if (this.currentUserType === "TOOL_MAKER") {
              this.requestParam.companyType = "TOOL_MAKER";
            }
            break;
          case "LOCATION":
            if (this.currentUserType === "OEM") {
              this.requestParam.companyType = "IN_HOUSE";
            } else if (this.currentUserType === "SUPPLIER") {
              this.requestParam.companyType = "SUPPLIER";
            } else if (this.currentUserType === "TOOL_MAKER") {
              this.requestParam.companyType = "TOOL_MAKER";
            }
            break;
          case "MACHINE":
            if (this.currentUserType === "OEM") {
              this.requestParam.companyType = "IN_HOUSE";
            } else if (this.currentUserType === "SUPPLIER") {
              this.requestParam.companyType = "SUPPLIER";
            } else if (this.currentUserType === "TOOL_MAKER") {
              this.requestParam.companyType = "TOOL_MAKER";
            }
            break;
          case "TOOLING":
            if (this.currentUserType === "OEM") {
              this.requestParam.companyType = "IN_HOUSE";
            } else if (this.currentUserType === "SUPPLIER") {
              this.requestParam.companyType = "SUPPLIER";
            } else if (this.currentUserType === "TOOL_MAKER") {
              this.requestParam.companyType = "TOOL_MAKER";
            }
            break;
          default:
            delete this.requestParam.companyType;
        }
        this.getDataCompletionData(1);
        // this.resetAfterUpdate();
      }
    },
    changeSelectCompany(item) {
      this.companySelectedList = item;
      this.requestParam.companyId = this.companySelectedList;
      if (this.requestParam.objectType) {
        this.getDataCompletionData();
      } else {
        delete this.requestParam.objectType;
        this.getDataCompletion();
      }
    },
    getCompanyFull() {
      axios
        .get(
          `/api/data-completion?query=&status=active&sort=id%2Casc&page=1&size=999999&objectType=COMPANY`
        )
        .then((res) => {
          const dataFilter = res.data.data.content.filter(
            (res) => +res.rate < 100
          );
          this.companyFullData = dataFilter;
        })
        .catch((e) => {
          console.log(e, "getDataCompletionData error");
        });
    },
    getLocationFull() {
      axios
        .get(
          `/api/data-completion?query=&status=active&sort=id%2Casc&page=1&size=999999&objectType=LOCATION`
        )
        .then((res) => {
          const dataFilter = res.data.data.content.filter(
            (res) => +res.rate < 100
          );
          this.locationFullData = dataFilter;
        })
        .catch((e) => {
          console.log(e, "getDataCompletionData error");
        });
    },
    getToolingFull() {
      axios
        .get(
          `/api/data-completion?query=&status=active&sort=id%2Casc&page=1&size=999999&objectType=TOOLING`
        )
        .then((res) => {
          const dataFilter = res.data.data.content.filter(
            (res) => +res.rate < 100
          );
          this.toolingFullData = dataFilter;
        })
        .catch((e) => {
          console.log(e, "getDataCompletionData error");
        });
    },
    getMachineFull() {
      axios
        .get(
          `/api/data-completion?query=&status=active&sort=id%2Casc&page=1&size=999999&objectType=MACHINE`
        )
        .then((res) => {
          const dataFilter = res.data.data.content.filter(
            (res) => +res.rate < 100
          );
          this.machineFullData = dataFilter;
        })
        .catch((e) => {
          console.log(e, "getDataCompletionData error");
        });
    },
    roundNum(num) {
      return Math.round(num * 100) / 100;
    },
    async getDataCompletionData(pageNumber) {
      console.log("come 1111");
      this.loading = true;
      const sort = this.filterStatus.sort ? "desc" : "asc";
      this.requestParam.sort = this.currentSortType + "," + sort;
      var param = Common.param(this.requestParam);
      await axios
        .get(`/api/data-completion?${param}`)
        .then((res) => {
          this.chartData = res.data;
          this.pagination = Common.getPagingData(res.data.data);
          this.total = res.data.data.totalElements;
          this.$forceUpdate();
        })
        .catch((e) => {
          console.log(e, "getDataCompletionData error");
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getDataCompletion(pageNumber) {
      this.loading = true;
      const sort = this.filterStatus.sort ? "desc" : "asc";
      this.requestParam.sort = this.currentSortType + "," + sort;
      var param = Common.param(this.requestParam);
      console.log(`/api/data-completion/companies?${param}`);
      axios
        .get(`/api/data-completion/companies?${param}`)
        .then((res) => {
          this.chartData = res.data;
          // this.avgRate = this.roundNum(res.data.avgRate.toFixed(1));
          // this.updateAvg();
          // this.pagination = Common.getPagingData(res.data.details);
          this.$forceUpdate();
        })
        .catch((e) => {
          console.log(e, "production-pattern-analysis");
        })
        .finally(() => {
          this.loading = false;
        });
    },
    bindChartPreventive: function (result) {
      var labels = ["Incomplete", "Complete"];
      var dataset = this.overallChart.data.datasets[0];

      for (var i = 0; i < labels.length; i++) {
        dataset.data[i] = 0;
        for (var j = 0; j < result.length; j++) {
          if (labels[i] == result[j].title) {
            dataset.data[i] = result[j].data;
            break;
          }
        }
      }
      this.overallChart.update();
    },
    changeDropdown() {
      this.dropdownOpening = false;
    },
    openDropdown() {
      Common.changeClickFromPin(false);
      if (this.dropdownOpening === true) {
        this.dropdownOpening = false;
      } else {
        this.dropdownOpening = true;
      }
    },
    showDrawer(type) {
      let drawerContent = document.getElementsByClassName(
        "ant-drawer-content-wrapper"
      );
      console.log(drawerContent);
      this.isType = type;
      this.visible = true;
      if (drawerContent) {
        setTimeout(() => {
          if (drawerContent.length == 0) {
            setTimeout(() => {
              drawerContent = document.getElementsByClassName(
                "ant-drawer-content-wrapper"
              );
              if (drawerContent.length > 0)
                drawerContent[0].classList.add("drawer-show");
            }, 1000);
          }
          if (drawerContent.length > 0)
            drawerContent[0].classList.add("drawer-show");
        }, 200);
      }
    },
    closeDrawer() {
      let drawerContent = document.getElementsByClassName(
        "ant-drawer-content-wrapper"
      );
      drawerContent[0].classList.remove("drawer-show");
      if (drawerContent) {
        setTimeout(() => {
          this.visible = false;
        }, 1000);
      }
    },
    onChangeHref(href) {
      setTimeout(() => {
        window.location.href = href;
      }, 700);
    },
    showAnimation(id, item) {
      let page = "table";
      console.log("@showAnimation", id, item);
      this.openOrderModal = false;
      const el = document.getElementById(id);
      el.classList.add("outline-animation");
      const self = this;
      setTimeout(() => {
        switch (this.requestParam.objectType) {
          case "COMPANY":
            self.showCompanyDetailsForm(item.data, page);
            break;
          case "TOOLING":
            self.showMoldDetailsForm(item, page);
            break;
          case "MACHINE":
            self.showMachineDetailsForm(item.data, page);
            break;
          case "PART":
            self.showPartDetailsForm(item.data, page);
            break;
          case "LOCATION":
            self.showLocationForm(item.data, page);
            break;
        }

        el.classList.remove("outline-animation");
      }, 700);
    },
    changeOpenOrderModal(boolean) {
      this.openOrderModal = boolean;
    },
    showCompleteData(item, name) {
      let page = "modal";
      console.log("@showCompleteData", item, name);
      this.openOrderModal = true;
      switch (name) {
        case "COMPANY":
          this.showCompanyDetailsForm(item.data, page);
          break;
        case "TOOLING":
          let dataFake = item;
          dataFake.id = item.data.id;
          this.showMoldDetailsForm(dataFake, page);
          break;
        case "MACHINE":
          this.showMachineDetailsForm(item.data, page);
          break;
        case "PART":
          this.showPartDetailsForm(item.data, page);
          break;
        case "LOCATION":
          this.showLocationForm(item.data, page);
          break;
      }
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
    categoryLocation: function (categoryId) {
      var location = "";
      for (var i = 0; i < vm.categories.content.length; i++) {
        var parent = vm.categories.content[i];
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
    getSelectedCase() {
      switch (this.categorySelected) {
        case "companies":
          this.href = "/admin/companies/new";
          this.createText = "Create Company";
          this.importText = null;
          break;
        case "locations":
          this.href = "/admin/locations/new";
          this.createText = "Create Location";
          this.importText = null;
          break;
        case "categories":
          this.href = "/admin/categories/new";
          this.createText = "Create Category";
          this.importText = null;
          break;
        case "parts":
          this.href = "/admin/parts/new";
          this.createText = "Create Part";
          this.importText = "Import Part";
          break;
        case "molds":
          this.href = "/admin/molds/new";
          this.createText = "Create Tooling";
          this.importText = "Import Tooling";
          break;
        case "machines":
          this.href = "/admin/machines/new";
          this.createText = "Create Machine";
          this.importText = null;
          break;
      }
    },
    showCompanyDetails: function (company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetails(company.data);
      }
    },
    showCompanyDetailsForm: function (company, page) {
      console.log(`Company: `, company);
      var child = Common.vue.getChild(this.$children, "company-details-form");
      if (child != null) {
        child.showDetails(company, page);
      }
    },
    showChartPart: function (part) {
      var child = Common.vue.getChild(this.$children, "chart-part");
      if (child != null) {
        child.showChart(part, "QUANTITY", "DAY");
      }
    },
    showMachineDetails: function (company) {
      var child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetails(company.data);
      }
    },
    showMachineDetailsForm: function (company, page) {
      var child = Common.vue.getChild(this.$children, "machine-details-form");
      if (child != null) {
        child.showDetails(company, page);
      }
    },
    showPartDetails: function (part) {
      var child = Common.vue.getChild(this.$children, "part-details");
      if (child != null) {
        child.showPartDetails(part.data);
      }
    },
    showPartDetailsForm: function (part, page) {
      var child = Common.vue.getChild(this.$children, "part-details-form");
      if (child != null) {
        child.showPartDetails(part, page);
      }
    },
    showLocationForm: function (location, page) {
      console.log(location, "location");
      var child = Common.vue.getChild(this.$children, "location-form");
      if (child != null) {
        child.showForm(location, page);
      }
    },
    showCategoryForm: function (category, page) {
      if (category.parentId) {
        var child = Common.vue.getChild(this.$children, "project-form-modal");
        if (child != null) {
          child.showModal(category, page == "table");
        }
        return;
      }
      var child = Common.vue.getChild(this.$children, "category-form");
      if (child != null) {
        child.showForm(category, page);
      }
    },
    showChart: function (result, tab) {
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.showChart(
          result.data,
          "QUANTITY",
          "DAY",
          tab ? tab : "switch-graph"
        );
      }
    },
    backToMoldDetail() {
      console.log("back to molds detail");
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.backFromPreview();
      }
    },
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    showMoldDetails: function (result) {
      console.log("@showMoldDetails", result);
      var child = Common.vue.getChild(this.$children, "mold-details");
      const tab = "switch-detail";
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        if (result.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${result.id}/disapproval-reason`)
            .then(function (response) {
              reasonData = response.data;
              // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
              reasonData.approvedAt = reasonData.approvedAt
                ? vm.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              result.notificationStatus = result.dataSubmission;
              result.reason = reasonData.reason;
              result.approvedAt = reasonData.approvedAt;
              result.approvedBy = reasonData.approvedBy;
              // child.showMoldDetails(mold.data);
              this.showChart(result, tab);
            });
        } else {
          this.showChart(result, tab);
          // child.showMoldDetails(mold.data);
        }
      }
    },
    showMoldDetailsForm(mold, page) {
      var child = Common.vue.getChild(this.$children, "mold-details-form");
      console.log("@showMoldDetailsForm", child, mold, page);
      if (child != null) {
        console.log("as");
        let reasonData = { reason: "", approvedAt: "" };
        if (mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
            .then(function (response) {
              reasonData = response.data;
              // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
              reasonData.approvedAt = reasonData.approvedAt
                ? vm.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              mold.notificationStatus = mold.dataSubmission;
              mold.reason = reasonData.reason;
              mold.approvedAt = reasonData.approvedAt;
              mold.approvedBy = reasonData.approvedBy;
              child.showMoldDetails(mold, page);
            });
        } else {
          child.showMoldDetails(mold, page);
        }
      }
    },
    showPartChart: function (part) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$children, "chart-part");
      if (child != null) {
        child.showChart(part.data, "QUANTITY", "DAY");
      }
    },
    onHover(code) {
      this.isHover = code;
    },
    async setAllColumnList() {
      vm.allColumnList = [
        {
          label: vm.resources["machine_id"],
          field: "machineCode",
          default: true,
          mandatory: true,
          sortable: true,
          enabled: true,
        },
      ];
      // this.serverName = await Common.getSystem('server')
      const options = await Common.getSystem("options");
      this.options = JSON.parse(options);
      // const condition = this.serverName === 'dyson'
      const condition = Boolean(
        this.options?.CLIENT?.cols?.dataCompletion?.show
      );
      console.log("condition", condition);
      if (condition) {
        this.allColumnList.push({
          label: vm.resources["data_submission"],
          field: "dataSubmission",
          default: true,
          sortable: true,
          sortField: "dataSubmission",
        });
      }
    },
    paging: function (pageNumber) {
      var self = this;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let requestParam = JSON.parse(JSON.stringify(self.requestParam));

      var param = Common.param(requestParam);
      axios
        .get(`/api/data-completion?${param}`)
        .then(function (response) {
          console.log(response.data.data, "responseresponseresponse");
          self.total = response.data.data.totalElements;
          self.chartData = response.data;
          self.pagination = Common.getPagingData(response.data.data);
          self.$forceUpdate();
          document.querySelector("#app .op-list").style.display =
            "table-row-group";
          document.querySelector(".pagination").style.display = "flex";
          if (
            response.data.data &&
            response.data.data.content &&
            response.data.data.content.length > 0
          ) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    async getResources() {
      try {
        const stringifiedMessages = await Common.getSystem("messages");
        vm.resources = JSON.parse(stringifiedMessages);
        vm.setAllColumnList();
      } catch (error) {
        Common.alert(error.response.data);
      }
    },
    tab: function (status) {
      this.total = null;
      this.listChecked = [];
      this.isAll = false;
      if (status === "DELETED") {
        this.requestParam.companyType = status;
        this.requestParam.deleted = true;
        this.getDataCompletionData();
      } else {
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.requestParam.companyType = status;
        delete this.requestParam.deleted;
        this.getDataCompletionData();
      }
    },
    sortBy(sort) {
      if (this.currentSortType != sort) {
        this.isDesc = true;
        this.currentSortType = sort;
        this.filterStatus.sort = true;
        if (this.requestParam.objectType) {
          this.getDataCompletionData();
        } else {
          delete this.requestParam.objectType;
          this.getDataCompletion();
        }
      } else {
        this.isDesc = !this.isDesc;
        this.currentSortType = sort;
        this.filterStatus.sort = !this.filterStatus.sort;
        if (this.requestParam.objectType) {
          this.getDataCompletionData();
        } else {
          delete this.requestParam.objectType;
          this.getDataCompletion();
        }
      }
    },
    sort: function () {
      this.paging(this.requestParam.page);
    },
    getCompany() {
      return axios
        .get("/api/companies?query=&status=active&sort=id%2Cdesc&page=1")
        .then((res) => {
          this.companies = res.data;
        });
    },
    getLocations() {
      return axios
        .get("/api/locations?query=&status=active&sort=id%2Cdesc&page=1")
        .then((res) => {
          this.locations = res.data;
        });
    },
    getCategories() {
      return axios
        .get(
          "/api/categories/uncompleted?query=&status=1&sort=sortOrder%2Casc&page=1"
        )
        .then((res) => {
          this.categories = res.data;
        });
    },
    getParts() {
      return axios
        .get(
          "/api/parts?accessType=ADMIN_MENU&categoryId=&query=&status=active&sort=id%2Cdesc&page=1"
        )
        .then((res) => {
          this.parts = res.data;
        });
    },
    getMolds() {
      return axios
        .get("/api/molds?id=&query=&status=all&sort=lastShotAt%2Cdesc&page=1")
        .then((res) => {
          this.molds = res.data;
        });
    },
    getMachines() {
      return axios
        .get("/api/machines?query=&status=enabled&sort=id%2Cdesc&page=1&id=")
        .then((res) => {
          this.machines = res.data;
        });
    },
    getRequestId() {
      return axios
        .get("/api/data-registration/generate-request_id")
        .then((res) => {
          this.requestId = res.data;
        });
    },
    async getCurrentUser() {
      try {
        const me = await Common.getSystem("me");
        this.currentUser = JSON.parse(me);
      } catch (error) {
        console.log(error);
      }
    },
    async getCurrentUserType() {
      try {
        const userType = await Common.getSystem("type");
        this.currentUserType = userType;
        if (userType === "OEM") this.requestParam.companyType = "IN_HOUSE";
        if (userType === "SUPPLIER") this.requestParam.companyType = "SUPPLIER";
        if (userType === "TOOL_MAKER")
          this.requestParam.companyType = "TOOL_MAKER";
        this.$forceUpdate();
      } catch (error) {
        console.log(error);
      }
    },
    getRequestedColor(name, id) {
      return Common.getRequestedColor(name, id);
    },
    getChildrenByParentID(parentId) {
      console.log("chartData", this.chartData.data.content);
      let children = this.chartData.data.content.filter(
        (item) => item.data.parentId == parentId
      );
      console.log("children search", parentId, children);
      if (children) {
        return children;
      } else {
        return [];
      }
    },
  },
  computed: {
    isOEMUser() {
      let x = this.currentUser.roles.filter(
        (item) => item.authority == "ROLE_MENU_1"
      );
      console.log(x);
      return x.length > 0;
    },
  },
  async mounted() {
    this.$nextTick(async function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      const self = this;
      self.animationOutline();
      self.animationPrimary();
      // const server = await Common.getSystem('server')
      const options = await Common.getSystem("options");
      const userType = await Common.getSystem("type");
      const resCodes = await axios.get("/api/codes");
      // self.serverName = server
      self.options = JSON.parse(options);
      self.userType = userType;
      self.codes = resCodes.data;
      self.paging(1);

      let url = new URL(window.location.href);
      let type = url.searchParams.get("objectType");
      if (type) {
        self.requestParam.objectType = type;
        console.log("requestParam.objectType", self.requestParam.objectType);
      }
      // this.paging(1);
      self.getSelectedCase();
      self.getResources();
      self.getDataCompletionData();
      self.getDropdownCategory();
      self.overallChart = new Chart(
        $("#chart-data-completion"),
        this.configChart
      );
      self.dataConvert = {
        data: [
          { title: "Incomplete", data: this.avgRate },
          { title: "Complete", data: 100 - this.avgRate },
        ],
      };
      var backgroundColors = ["#707070", "#C1C7CD"];
      self.overallChart.data.datasets[0].backgroundColor = [
        "#5D43F7",
        "#C1C7CD",
      ];
      self.dataConvert.colors = backgroundColors;
      self.getRate();
      self.bindChartPreventive(this.dataConvert.data);

      //
      let url_string = window.location.href;
      const objectId = /[^/]*$/.exec(url_string)[0];
      if (objectId.includes("receiver")) {
        self.repeatPending(self, "received");
      } else if (objectId.includes("creator")) {
        self.repeatPending(self, "created");
      }
    });
  },
});
