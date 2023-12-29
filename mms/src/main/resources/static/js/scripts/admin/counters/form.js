var Page = Common.getPage("counters");
const newPageUrl = Common.PAGE_URL;
var paramId =
  location.href.split("?").length > 1
    ? window.location.href.split("?")[1]
    : null;
var fromScanQr = null;
if (paramId) {
  fromScanQr = paramId.includes("counterId");
  if (fromScanQr) {
    paramId =
      paramId.split("=").length > 1 ? unescape(paramId.split("=")[1]) : null;
  }
}

window.onload = function () {
  document.title = "Counter" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_counter1");
    $("div").removeClass("wave_counter2");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    if (document.getElementById("remove_profile"))
      document.getElementById("remove_profile").remove();
    $("div").removeClass("hide_account");
  }, 500);
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

var vm = new Vue({
  el: "#app",
  components: {
    "mold-search": httpVueLoader("/components/mold-search.vue"),
    "qr-scan": httpVueLoader("/components/scan-qr.vue"),
    "confirm-modal": httpVueLoader("/components/confirm-modal.vue"),
    "terminal-details": httpVueLoader("/components/terminal-details.vue"),
    "preset-form": httpVueLoader("/components/preset-form.vue"),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    CustomDropdown: httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
    "time-picker": httpVueLoader(
      "/components/alert-center/machine-downtime/time-picker.vue"
    ),
  },
  data: {
    resources: {},
    isNew: Page.IS_NEW,
    mode: Page.MODE,
    isMobile: Common.checkMobile(),
    counter: {
      equipmentCode: "",
      equipmentStatus: "INSTALLED",

      presetCount: "",
      moldId: "",
      moldName: "",
      moldCode: "",
      installedAt: "",
      installedBy: "",
      memo: "",
      files: [],
    },

    files: {},
    selectedFiles: [],
    counterFiles: [],
    moldSelected: {},
    thirdFiles: [],
    //edit
    partPictureFiles: [],
    fileTypes: {
      TERMINAL_PICTURE: "TERMINAL_PICTURE",
    },
    listTerms: [
      { title: "None", value: null },
      { title: "1 Year", value: 1 },
      { title: "2 Years", value: 2 },
      { title: "3 Years", value: 3 },
      { title: "4 Years", value: 4 },
      { title: "6 Years", value: 6 },
      { title: "7 Years", value: 7 },
      { title: "8 Years", value: 8 },
      { title: "9 Years", value: 9 },
      { title: "10 Years", value: 10 },
    ],
    subscriptionTermSelected: {},
    installedAtValue: null,
    currentUser: null,
  },
  methods: {
    onChangeInstalledAt(date) {
      this.installedAtValue = date;
      this.counter.installedAt = date?.format("YYYY-MM-DD");
    },
    handleChangeTerm(item, prevItem) {
      console.log("item", item);
      this.subscriptionTermSelected = item;
      vm.counter.subscriptionTerm = item.value;
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
      formData.append("payload", JSON.stringify(vm.counter));
      return formData;
    },
    resetData: function () {
      //window.scrollTo({ top: 0, behavior: 'smooth' });
      this.counter = {
        equipmentCode: "",
        equipmentStatus: "INSTALLED",

        presetCount: "",
        moldId: "",
        moldName: "",
        installedAt: "",
        installedBy: "",
        memo: "",
        files: [],
      };
      this.files = {};
      this.selectedFiles = {};
      this.counterFiles = {};
      const element = document.getElementById("main");
      console.log("element.offsetTop", element.offsetTop);
      element.scrollIntoView();
      $("body, html, #main").scrollTop(0);
    },

    openQrScan: function () {
      var child = Common.vue.getChild(this.$children, "qr-scan");
      if (child != null) {
        child.showQrScan(this.setCounterId, this, false, "COUNTERS");
      }
    },

    showConfirmModal: function () {
      const data = {
        title: "Success!",
        content: "Success",
        titleButton: "Install another counter",
        titleButton2: "Done",
        finish: () => {
          location.href = newPageUrl.SENSOR;
        },
        reInstall: this.resetData,
      };
      var childConfirm = Common.vue.getChild(this.$children, "confirm-modal");
      if (childConfirm != null) {
        childConfirm.showConfirm(data);
      }
    },

    setCounterId: function (code) {
      this.counter.equipmentCode = code;
    },

    submit: function () {
      if (fromScanQr) {
        return this.create();
      }
      if (paramId) {
        return this.update();
      }
      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
    },

    create: function () {
      // var formData = new FormData();
      // for (var i = 0; i < this.files.length; i++) {
      //     let file = this.files[i];
      //     //formData.append('files[' + i + ']', file);
      //     formData.append("files", file);
      // }
      //
      // formData.append('payload', JSON.stringify(vm.counter));

      axios
        .post(Page.API_POST, this.formData(), this.multipartHeader())
        .then((response) => {
          console.log("response.data: ", response.data);
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
      if (vm.counter.moldId) {
        vm.counter.equipmentStatus = "INSTALLED";
      }

      // formData.append('payload', JSON.stringify(vm.counter));
      // console.log('vm.counter: ', vm.counter)
      let url = Page.API_PUT;
      if (paramId) {
        url = Page.API_BASE + `/${paramId}`;
      }
      let self = this;
      axios
        .put(url, this.formData(), this.multipartHeader())
        .then(function (response) {
          console.log(response.data);
          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = newPageUrl.SENSOR;
            };
            Common.alertCallbackClose = function () {
              location.href = newPageUrl.SENSOR;
            };
            Common.alert("success");
            // location.href = newPageUrl.SENSOR;
            /*
                            try{

                            //show reset value
                                vm.moldSelected.counterCode = vm.counter.equipmentCode;
                                self.showPresetForm(vm.moldSelected);
                            }catch (e) {
                                console.log(e);
                            }
*/
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    cancel: function () {
      location.href = newPageUrl.SENSOR;
    },

    callbackMold: function (mold) {
      vm.counter = Object.assign({}, vm.counter);
      vm.counter.moldName = mold.name;
      vm.counter.moldId = mold.id;
      vm.counter.moldCode = mold.equipmentCode;
      vm.counter.presetCount = mold.lastShot;
      vm.moldSelected = mold;
    },

    selectedFile: function (e) {
      var files = e.target.files;
      if (files) {
        vm.files = files;
        /*for (var i = 0; i < files.length; i++){
                            var fileInfo = {
                                'file': files[i]
                            };

                            var reader = new FileReader();


                            reader.onload = function(){
                                fileInfo.readerResult = reader.result;
                                vm.selectedFiles.push(fileInfo);
                            }
                            reader.readAsDataURL(files[i]);


                        }*/
      }
    },
    deleteFile: function (f) {
      //lastModified
      alert(f.lastModified);

      for (var i = vm.files.length - 1; i >= 0; i--) {
        var file = vm.files[i];
        if (file.lastModified == f.lastModified) {
          vm.files.splice(i, 1);
          //vm.selectedFiles.splice(i, 1);
        }
      }
    },
    //show preset from
    showPresetForm: function (mold) {
      console.log("mold:", mold);
      //alert(counter.equipmentCode);
      var child = Common.vue.getChild(this.$children, "preset-form");
      if (child != null) {
        child.showModal(mold);
      }
    },
    callbackPresetForm: function (result) {
      if (result == true) {
        Common.alertCallback = function () {
          location.href = newPageUrl.SENSOR;
        };
        Common.alertCallbackClose = function () {
          location.href = newPageUrl.SENSOR;
        };
        Common.alert("success");
      }
      // this.paging(1);
    },
    callbackClosePresetForm: function (result) {
      if (result == true) {
        location.href = newPageUrl.SENSOR;
      }
      // this.paging(1);
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
    async getCurrentUser() {
      const me = await Common.getSystem("me");
      this.currentUser = JSON.parse(me);
    },
  },
  computed: {
    readonly: function () {
      return Page.IS_EDIT;
    },
    isNewPage: function () {
      return !Page.IS_EDIT;
    },
    isEditPage: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "Install" : "Edit";
    },

    hasParam: function () {
      return paramId !== null && !fromScanQr;
    },

    isShowQrScan: function () {
      return (
        Page.IS_NEW && Common.checkMobile() && (paramId === null || fromScanQr)
      );
      return paramId !== null;
    },

    toolingShow: function () {
      if (this.counter.moldCode) {
        return (
          this.counter.moldCode +
          (this.counter.moldName ? ` ( ${this.counter.moldName} )` : "")
        );
      }
      return "";
    },
  },
  mounted() {
    this.$nextTick(function () {
      if (fromScanQr) {
        return (vm.counter.equipmentCode = paramId);
      }
      this.subscriptionTermSelected = this.listTerms[0];
      if (Page.IS_EDIT) {
        // 데이터 조회
        axios.get(Page.API_GET).then(function (response) {
          console.log("response.data: ", response.data);
          vm.counter = response.data;
          vm.counter.moldName = vm.counter.mold ? vm.counter.mold.name : "";
          if (vm.counter.subscriptionTerm != null) {
            this.subscriptionTermSelected = this.listTerms.filter(
              (t) => (t.value = vm.counter.subscriptionTerm)
            )[0];
          }
          var param = {
            storageType: "MOLD_COUNTER",
            refId2: vm.counter.id,
          };
          //var param = Common.param(self.requestParam);
          axios
            .get("/api/file-storage?" + Common.param(param))
            .then(function (response) {
              vm.counterFiles = response.data;
            });
        });
      }

      if (paramId && !fromScanQr) {
        // 데이터 조회
        axios.get(Page.API_BASE + `/${paramId}`).then(function (response) {
          console.log("response.data: ", response.data);
          vm.counter = response.data;
          vm.counter.moldName = vm.counter.mold ? vm.counter.mold.name : "";

          var param = {
            storageType: "MOLD_COUNTER",
            refId2: vm.counter.id,
          };
          //var param = Common.param(self.requestParam);
          axios
            .get("/api/file-storage?" + Common.param(param))
            .then(function (response) {
              vm.counterFiles = response.data;
            });
        });
      }
      vm.getResources();
      vm.getCurrentUser();
    });
  },
  watch: {
    "counter.equipmentCode": function (newVal, oldVal) {
      console.log("this.counter.moldId: ", this.counter.moldId);
      if (newVal && this.counter.moldId) {
        this.counter.installedAt = moment().format("YYYY-MM-DD");
        this.installedAtValue = moment();
      }
    },

    "counter.moldId": function (newVal, oldVal) {
      console.log("this.counter.equipmentCode: ", this.counter.equipmentCode);
      if (newVal && this.counter.equipmentCode) {
        this.counter.installedAt = moment().format("YYYY-MM-DD");
        this.installedAtValue = moment();
      }
    },
  },
});
