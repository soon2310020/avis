var Page = Common.getPage("machines");

window.onload = function () {
  document.title = "machines" + " | eMoldino";
  Common.removeWave();
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

var vm = new Vue({
  el: "#app",
  components: {
    "company-search": httpVueLoader("/components/company-search.vue"),
    "location-search": httpVueLoader("/components/location-search.vue"),
    "engineer-charge-dropdown": httpVueLoader(
      "/components/engineer-charge-dropdown.vue"
    ),
    "mold-search": httpVueLoader("/components/mold-search.vue"),
  },
  data: {
    dropdownVisible: false,
    selectedEngineer: "",
    engineerList: [],
    checkedList: [],
    selectedEngineerIdList: [],
    selectedEngineerNameList: [],
    resources: {},
    // mode: Page.MODE,
    machine: {
      id: "",
      machineCode: "",
      locationId: "",
      locationName: "",
      locationCode: "",
      companyId: "",
      companyName: "",
      countryCode: "",
      line: "",
      machineMaker: "",
      machineType: "",
      machineModel: "",
      machineTonnage: "",
      enabled: true,
    },
    customFieldList: null,
    engineers: [],
    engineerIndex: [],
    requiredFields: [],
    toolingId: "",
    url_string: "",
    check: false,
    objectId: "",
    equipmentCode: "",
  },
  created() {
    let url_string = window.location.href;
    const objectId = /[^/]*$/.exec(url_string)[0];
    this.check = true;
    this.objectId = objectId;
    this.url_string = url_string;
  },
  methods: {
    callbackMold: function (mold) {
      console.log("mold-----------", mold);
      this.toolingId = mold.id;
      this.equipmentCode = mold.equipmentCode;
    },
    registerSelectedItem() {
      this.dropdownVisible = false;
      let checkedEngineers = this.engineerList.filter(
        (item) => item.checked === true
      );
      this.selectedEngineerIdList = checkedEngineers.map((item) => {
        return item.id;
      });
      this.selectedEngineerNameList = checkedEngineers.map((item) => {
        return item.title;
      });
    },
    searchChangeFunc(event) {},
    isRequiredField(field) {
      if (["Line", "MachineId", "Company", "Location"].includes(field)) {
        return true;
      }
      return this.requiredFields.includes(field);
    },
    submit: async function () {
      if (this.toolingId) {
        this.apiMatchTooling();
      } else {
        Common.alert("Please select the tooling first");
      }
    },
    apiMatchTooling: function () {
      let param = {
        moldId: 0,
        machineId: 0,
      };
      param.moldId = this.toolingId;
      param.machineId = this.machine.id;
      axios
        .post("/api/machines/match-with-tooling", param)
        .then(async function (response) {
          if (response.data.success == true) {
            Common.alertCallback = function () {
              location.href = Common.PAGE_URL.MACHINE;
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          Common.alert("Failed to match with tooling");
        });
    },
    callbackCompany: function (company) {
      if (!vm.machine.company) {
        vm.machine.company = {
          id: "",
          name: "",
        };
      }
      vm.machine.company.id = company.id;
      vm.machine.company.name = company.name;
      vm.machine.companyId = company.id;
      vm.machine.companyName = company.name;
    },
    callbackLocation: function (location) {
      if (!vm.machine.location) {
        vm.machine.location = {
          id: "",
          name: "",
        };
      }
      vm.machine.location.id = location.id;
      vm.machine.location.name = location.name;
      vm.machine.locationId = location.id;
      vm.machine.locationName = location.name;
    },
    create: function () {
      this.createApi();
    },

    createApi: function () {
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
        .post("/api/machines", {
          ...vm.machine,
          engineerIds: this.selectedEngineerIdList,
        })
        .then(function (response) {
          const objectId = response.data.id;
          axios
            .post(`/api/custom-field-value/edit-list/${objectId}`, params)
            .then(() => {
              Common.alertCallback = function () {
                location.href = Common.PAGE_URL.MACHINE;
              };
              Common.alert("success");
            })
            .catch((e) => {
              console.log(e);
            });
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
    async updateCustomField() {
      let url_string = this.url_string;
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
      await axios.post(`/api/custom-field-value/edit-list/${objectId}`, {
        ...params,
        engineerIds: this.selectedEngineerIdList,
      });
    },
    cancel: function () {
      location.href = Common.PAGE_URL.MACHINE;
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
    handleChange(value, options) {
      this.engineerIndex = value;
      this.selectedEngineerIdList = value;
      this.checkedList = [];
      value.map((id) => {
        this.engineerList.map((item) => {
          if (id === item.id) {
            this.checkedList.push(item);
          }
        });
      });
    },
  },
  computed: {
    readonlyLocationCode: function () {
      return Page.IS_EDIT;
    },
  },
  mounted() {
    var self = this;
    Page.API_GET = "/api/machines/" + this.objectId;
    if (Page.IS_EDIT) {
      // 데이터 조회
      axios.get(Page.API_GET).then((response) => {
        vm.machine = response.data;
        this.selectedEngineerIdList = vm.machine.engineerIds;
        this.engineerIndex = vm.machine.engineerIds;
        axios.get("/api/common/usr-viw/users?size=500").then((res) => {
          this.engineerList = res.data.content.map((item) => {
            // selectedEngieerIdList 에 포함된 id를 가지고 있으면 vm.machines 에 checkbox true로 바꿔주기
            if (this.selectedEngineerIdList.includes(item.id)) {
              return { ...item, title: item.name, checked: true };
            } else {
              return { ...item, title: item.name, checked: false };
            }
          });
          this.engineerList.map((item) => (item.title = item.name));
          this.engineerIndex.map((id) => {
            this.engineerList.map((value) => {
              if (id === value.id) {
                this.checkedList.push(value);
              }
            });
          });
        });
      });
    } else {
      axios.get("/api/common/usr-viw/users?size=500").then((res) => {
        this.engineerList = res.data.content.map((item) => {
          return { ...item, title: item.name, checked: false };
        });
      });
    }
    this.$nextTick(function () {
      vm.getResources();
    });
  },
});
