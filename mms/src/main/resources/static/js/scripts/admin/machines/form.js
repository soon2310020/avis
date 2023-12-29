var Page = Common.getPage("machines");
const newPageUrl = Common.PAGE_URL;

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
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    // 'mold-search': httpVueLoader('/components/mold-search.vue'),
  },
  data: {
    dropdownVisible: false,
    selectedEngineer: "",
    engineerList: [],
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
    checkedList: [],
    requiredFields: [],
    toolingId: "",
    url_string: "",
    check: false,
    objectId: "",
    thirdFiles: [],
    //edit
    partPictureFiles: [],
    fileTypes: {
      MACHINE_PICTURE: "MACHINE_PICTURE",
    },
  },
  created() {
    let url_string = window.location.href;
    const objectId = /[^/]*$/.exec(url_string)[0];
    this.isCreate = objectId === "new";

    this.objectId = objectId;
    url_string = "http://localhost:8080/admin/machines/" + objectId;
    this.url_string = url_string;

    if (this.isCreate) {
      axios
        .get("/api/custom-field?objectType=MACHINE")
        .then(function (response) {
          vm.customFieldList = response.data;
        });
    } else {
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=MACHINE&objectId=${objectId}`
        )
        .then(function (response) {
          vm.customFieldList = response.data.map((item) => ({
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
  },
  methods: {
    //thirdFiles
    deleteThirdFiles: function (index) {
      vm.thirdFiles = vm.thirdFiles.filter((value, idx) => idx !== index);
      if (vm.thirdFiles.length == 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteFileStorage: function (file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case vm.fileTypes.MACHINE_PICTURE:
              vm.partPictureFiles = response.data;
              break;
          }
        });
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = vm.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = vm.partPictureFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    instructionVideoFiles() {
      var param = {
        storageTypes: "MACHINE_PICTURE",
        refId: this.machine.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          this.partPictureFiles = response.data["MACHINE_PICTURE"]
            ? response.data["MACHINE_PICTURE"]
            : [];
        });
    },
    formData: function () {
      var formData = new FormData();

      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      const machineData = {
        ...vm.machine,
        engineerIds: this.selectedEngineerIdList,
      };
      formData.append("payload", JSON.stringify(machineData));
      return formData;
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
    showDropdown() {
      this.dropdownVisible = true;
    },
    searchChangeFunc(event) {},
    engineerSelect(event) {
      let id = JSON.parse(event.target.value).id;
      let title = JSON.parse(event.target.value).title;
      let checked = event.target.checked;

      if (checked) {
        this.engineerList = this.engineerList.map((item) => {
          if (item.id === id) {
            return { ...item, checked: true };
          } else {
            return item;
          }
        });
      } else {
        this.engineerList = this.engineerList.map((item) => {
          if (item.id === id) {
            return { ...item, checked: false };
          } else {
            return item;
          }
        });
      }
    },
    isNew() {
      return page.IS_NEW;
    },
    isRequiredField(field) {
      if (["MachineId", "Company", "Location"].includes(field)) {
        return true;
      }
      return this.requiredFields.includes(field);
    },
    submit: async function () {
      // if (!this.machine.companyId) {
      //   Common.alert("Company is required");
      //   return;
      // }
      if (!this.machine.locationId) {
        Common.alert("Location is required");
        return;
      }
      if (this.machine.machineTonnage && this.machine.machineTonnage < 1) {
        Common.alert("Machine tonnage must be positive number");
        return;
      }
      if (Page.IS_NEW) {
        this.create();
      } else {
        // await this.updateCustomField();
        this.update();
      }
      // }
    },
    // apiMatchTooling:function(){
    //     let param={
    //         "moldId":0,
    //         "machineId":0
    //     }
    //     param.moldId=this.toolingId;
    //     param.machineId=this.machine.id;
    //     axios.post('/api/machines/match-with-tooling',param).then(async function (response) {
    //         if(response.data.success==true) {
    //             Common.alertCallback = function () {
    //                 location.href = newPageUrl.MACHINE;
    //             }
    //             Common.alert("success");
    //         }
    //         else{
    //             // Common.alertCallback = function () {
    //             //     location.href = newPageUrl.MACHINE;
    //             // }
    //             Common.alert(response.data.message);
    //         }
    //     }).catch(function (error) {
    //             Common.alert("Failed to match with tooling");
    //         });
    // },
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
        .post(
          "/api/machines/add-multipart",
          this.formData(),
          this.multipartHeader()
        )
        .then(function (response) {
          const objectId = response.data.id;
          axios
            .post(`/api/custom-field-value/edit-list/${objectId}`, params)
            .then(() => {
              Common.alertCallback = function () {
                location.href = newPageUrl.MACHINE;
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

    apiUpdate: function () {
      vm.children = null;
      let self = this;
      axios
        .put(
          "/api/machines/edit-multipart/" + this.objectId,
          this.formData(),
          this.multipartHeader()
        )
        .then(async function (response) {
          await self.updateCustomField();
          Common.alertCallback = function () {
            location.href = newPageUrl.MACHINE;
          };
          Common.alert("success");
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },

    update: function () {
      this.apiUpdate();
    },

    cancel: function () {
      location.href = newPageUrl.MACHINE;
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
      //engineer
      // this.engineerIndex = value;
      // this.mold.engineerIds = value;
      // this.machine
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
    New: function () {
      return Page.IS_NEW ? this.resources["create"] : this.resources["edit"];
    },
  },
  mounted() {
    var self = this;
    this.partPictureFiles = [];
    // if(this.check){
    Page.API_GET = "/api/machines/" + this.objectId;
    // }
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
          this.registerSelectedItem();
          this.instructionVideoFiles();
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
