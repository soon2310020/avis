// import { InputIpAddress } from '@emoldino/components'

// Vue.component('input-ip-address', InputIpAddress)
const newPageUrl = Common.PAGE_URL;
var Page = Common.getPage("terminals");
function removeWave() {
  document.title = "Terminal" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_terminal1");
    $("div").removeClass("wave_terminal2");
    $("div").removeClass("wave_terminal3");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    document.getElementById("remove_profile").remove();
    $("div").removeClass("hide_account");
  }, 500);
}

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";
var paramId =
  location.href.split("?").length > 1
    ? window.location.href.split("?")[1]
    : null;
var fromScanQr = null;
if (paramId) {
  fromScanQr = paramId.includes("terminalId");
  if (fromScanQr) {
    paramId =
      paramId.split("=").length > 1 ? unescape(paramId.split("=")[1]) : null;
  }
}
var vm = new Vue({
  el: "#app",
  components: {
    "location-search": httpVueLoader("/components/location-search.vue"),
    "qr-scan": httpVueLoader("/components/scan-qr.vue"),
    "confirm-modal": httpVueLoader("/components/confirm-modal.vue"),
    "terminal-details": httpVueLoader("/components/terminal-details.vue"),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "daily-dropdown": httpVueLoader(
      "/components/@base/date-picker/daily-dropdown.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "plant-areas-combination-dropdown": httpVueLoader(
      "/components/terminals/plant-areas-combination-dropdown.vue"
    ),
    "plant-areas-dialog": httpVueLoader(
      "/components/terminals/plant-areas-dialog.vue"
    ),
  },
  data: {
    resources: {},
    isNew: Page.IS_NEW,
    isMobile: Common.checkMobile(),
    mode: Page.MODE,
    terminal: {
      equipmentCode: "",
      equipmentStatus: "INSTALLED",
      purchasedAt: "",
      locationId: "",
      location: { name: "" },
      installedAt: "",
      installedBy: "",
      ipType: "DYNAMIC",
      memo: "",
    },
    instalationDate: {
      from: new Date(),
      to: new Date(),
      fromTitle: moment().format("YYYY-MM-DD"),
      toTitle: moment().format("YYYY-MM-DD"),
    },
    timeRange: {
      minDate: null,
      maxDate: new Date("2100-01-01"),
    },
    files: {},
    selectedFiles: [],
    thirdFiles: [],
    //edit
    partPictureFiles: [],
    fileTypes: {
      TERMINAL_PICTURE: "TERMINAL_PICTURE",
    },
    visibleUserDropdown: false,
    userIds: [],
    plantAreas: [],
    selectedPlantArea: null,
    visiblePlantAreaDialog: false,
  },
  methods: {
    handleChangeLocation(location) {
      console.log("handleChangeLocation", location);
      this.terminal.locationId = location.id;
    },
    handleChangeIpAddress(id, value, error) {
      console.log("handleChangeIpAddress", id, value, error);
      if (!error) {
        this.terminal.ipAddress = value;
      }
    },
    handleChangeSubnetMask(id, value, error) {
      console.log("handleChangeSubnetMask", id, value, error);
      if (!error) {
        this.terminal.subnetMask = value;
      }
    },
    handleChangeGateway(id, value, error) {
      console.log("handleChangeGateway", id, value, error);
      if (!error) {
        this.terminal.gateway = value;
      }
    },
    handleChangeDns(id, value, error) {
      console.log("handleChangeDns", id, value, error);
      if (!error) {
        this.terminal.dns = value;
      }
    },
    handleShowPlantAreaPopup() {
      this.visiblePlantAreaDialog = true;
    },
    handleClosePlantAreaPopup() {
      this.visiblePlantAreaDialog = false;
    },
    async getAreasByPlant() {
      const id = this.terminal.locationId;
      if (id) {
        try {
          const res = await axios.get(`/api/common/plt-stp/${id}/areas`);
          console.log("getPlantAreasByPlantId", res);
          this.plantAreas = res.data.content.map((item) => item);
        } catch (error) {
          console.log(error);
        }
      }
    },
    async getUsers() {
      try {
        const res = await axios.get(
          "/api/users?status=active&query=&page=1&size=9999"
        );
        const rawIds = res.data.content.map((child) => ({
          ...child,
          title: child.displayName,
          checked: false,
        }));
        this.userIds = JSON.parse(JSON.stringify(rawIds));
        console.log("getUsers", this.userIds);
      } catch (error) {
        console.log(error);
      }
    },
    toggleUserDropdown() {
      this.visibleUserDropdown = !this.visibleUserDropdown;
    },

    closeUserDropdown() {
      this.visibleUserDropdown = false;
    },

    handleChangeUser(userIds) {
      this.userIds = userIds;
    },
    handleChangePlantArea(item) {
      this.selectedPlantArea = item;
    },
    handleChangeDate(date) {
      this.instalationDate = date;
      this.terminal.installedAt = this.instalationDate.fromTitle;
      console.log("handleChangeDate in terminal form", this.instalationDate);
    },
    getFile: function () {
      var param = {
        storageTypes: "TERMINAL",
        refId: this.terminal.id,
      };
      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          console.log(response.data, "TERMINAL");
          this.terminalFiles = response.data.TERMINAL;
          $("#op-terminal-details").modal("show");
        });
    },
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
            case vm.fileTypes.TERMINAL_PICTURE:
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
    formData: function () {
      var formData = new FormData();

      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("files", file);
      }
      const terminal = { ...vm.terminal };
      terminal.areaId = this.selectedPlantArea?.id
        ? this.selectedPlantArea?.id
        : "";
      terminal.installedAt = this.instalationDate.fromTitle;
      formData.append("payload", JSON.stringify(terminal));
      return formData;
    },
    resetData: function () {
      location.reload();
    },

    openQrScan: function () {
      var child = Common.vue.getChild(this.$children, "qr-scan");
      if (child != null) {
        child.showQrScan(this.setTerminalId, this, false, "TERMINALS");
      }
    },

    showConfirmModal: function () {
      const data = {
        title: "Success!",
        content: "New terminal has been successfully installed",
        titleButton: "Install another terminal",
        titleButton2: "Done",
        finish: () => {
          location.href = newPageUrl.TERMINAL;
        },
        reInstall: this.resetData,
      };

      console.log("childConfirm: ", childConfirm);
      var childConfirm = Common.vue.getChild(this.$children, "confirm-modal");
      if (childConfirm != null) {
        childConfirm.showConfirm(data);
      }
    },

    setTerminalId: function (code) {
      this.terminal.equipmentCode = code;
    },

    submit: function () {
      this.setIp();

      if (this.terminal.locationId == "") {
        alert("Please select location.");
        return;
      }
      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
    },

    setIp: function () {
      if (this.terminal.ipType == "DYNAMIC") {
        this.terminal.ipAddress = "";
        this.terminal.subnetMask = "";
        this.terminal.gateway = "";
        this.terminal.dns = "";
      }
    },

    create: function () {
      // var formData = new FormData();
      // for (var i = 0; i < this.files.length; i++) {
      //     let file = this.files[i];
      //     formData.append("files", file);
      // }
      // formData.append('payload', JSON.stringify(vm.terminal));
      axios
        .post(Page.API_POST, this.formData(), this.multipartHeader())
        .then((response) => {
          console.log(response.data);
          if (response.data.success) {
            this.showConfirmModal();
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    update: function () {
      // var formData = new FormData();
      // for (var i = 0; i < this.files.length; i++) {
      //     let file = this.files[i];
      //     formData.append("files", file);
      // }
      // formData.append('payload', JSON.stringify(vm.terminal));

      axios
        .put(Page.API_PUT, this.formData(), this.multipartHeader())
        .then(function (response) {
          console.log(response.data);
          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = newPageUrl.TERMINAL;
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    cancel: function () {
      location.href = newPageUrl.TERMINAL;
    },
    /* callbackCompany: function(company) {
                     vm.terminal.company.id = company.id;
                     vm.terminal.company.name = company.name;
                 },*/

    callbackLocation: function (location) {
      vm.terminal.locationId = location.id;
      vm.terminal.locationName = location.name;
    },

    test: function () {
      var child = Common.vue.getChild(this.$children, "location-search");
      if (child != null) {
        child.tab("active");
      }
    },
    selectedFile: function (e) {
      var files = e.target.files;
      if (files) {
        vm.files = files;
      }
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
        console.log("getResources", vm.resources);
      } catch (error) {
        console.log(error);
      }
    },
  },
  computed: {
    readonly: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },

    isShowQrScan: function () {
      return Page.IS_NEW && Common.checkMobile();
    },
    getSelectedUser() {
      return _.filter(this.userIds, (o) => o.checked);
    },
  },
  watch: {
    "terminal.locationId": async (newVal, oldVal) => {
      console.log("terminal.locationId", newVal);
      if (newVal) {
        await vm.getAreasByPlant();
        if (oldVal) {
          vm.selectedPlantArea = null;
        } else {
          if (vm.terminal.areaId) {
            vm.selectedPlantArea = {
              id: vm.terminal.areaId,
              name: vm.terminal.areaName,
              type: vm.terminal.areaType,
            };
          } else {
            vm.selectedPlantArea = null;
          }
        }
      }
    },
  },
  async mounted() {
    console.log("OKOK");
    removeWave();
    this.$nextTick(function () {
      if (fromScanQr) {
        return (vm.terminal.equipmentCode = paramId);
      }

      if (Page.IS_EDIT) {
        // 데이터 조회
        axios.get(Page.API_GET).then(function (response) {
          vm.terminal = response.data;
          vm.instalationDate = {
            from: moment(vm.terminal.installedAt, "YYYY-MM-DD").toDate(),
            to: moment(vm.terminal.installedAt, "YYYY-MM-DD").toDate(),
            fromTitle: vm.terminal.installedAt,
            toTitle: vm.terminal.installedAt,
          };
          var param = {
            storageTypes: "TERMINAL",
            refId: vm.terminal.id,
          };

          axios
            .get("/api/file-storage/mold?" + Common.param(param))
            .then(function (response) {
              vm.partPictureFiles = response.data.TERMINAL;
            });
        });
      }
      vm.getResources();
    });
    await this.getUsers();
  },
});
