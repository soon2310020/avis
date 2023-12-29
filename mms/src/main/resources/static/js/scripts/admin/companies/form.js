var Page = Common.getPage("companies");
const newPageUrl = Common.PAGE_URL;
window.onload = function () {
  document.title = "Company" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_company");
    $("div").removeClass("profile_wave");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    document.getElementById("remove_profile").remove();
    $("div").removeClass("hide_account");
  }, 500);
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";
var vm = new Vue({
  el: "#app",
  components: {
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  data: {
    CompanyType: [
      {
        code: "IN_HOUSE",
        title: "In-house",
      },
      {
        code: "TOOL_MAKER",
        title: "Toolmaker",
      },
      {
        code: "SUPPLIER",
        title: "Supplier",
      },
    ],
    showCompanyType: false,
    company_type: "Company Type",
    mode: Page.MODE,
    isNew: Page.IS_NEW,
    company: {
      companyType: "",
      companyCode: "",
      name: "",
      address: "",
      manager: "",
      phone: "",
      email: "",
      memo: "",
      enabled: true,
    },
    thirdFiles: [],
    uploadedFiles: [],
  },
  methods: {
    cancel() {
      location.href = newPage.COMPANY;
    },
    select(item) {
      // alert(item);

      this.company_type = item.title;
      this.showCompanyType = false;
      // console.log(item)
      this.company.companyType = item.code;
      // console.log(this.company.companyType)
    },
    handleshowCompanyType: function () {
      this.showCompanyType = true;
      // alert(this.showCompanyType);
    },
    handlecloseCompanyType: function () {
      this.showCompanyType = false;
      // alert(this.showCompanyType);
    },
    handleToggle: function (isShow) {
      // showCompanyType=!showCompanyType;
      this.showCompanyType = isShow;
    },
    submit: function () {
      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
    },
    getFormData() {
      var formData = new FormData();
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("files", file);
      }
      formData.append("payload", JSON.stringify(this.company));
      return formData;
    },
    async create() {
      try {
        console.log("create companies", this.thirdFiles);
        const payload = this.getFormData();
        const response = await axios.post(
          `/api/companies/add-multipart`,
          payload
        );
        if (response.data.success) {
          Common.alertCallback = function () {
            location.href = newPageUrl.COMPANY;
          };
          Common.alert("success");
        } else {
          Common.alert(response.data.message);
        }
      } catch (error) {
        console.log(error);
      }
      // axios
      //     .post(`/api/companies/add-multipart`, vm.company)
      //     .then(function (response) {
      //         console.log(response.data);
      //         if (response.data.success) {
      //             Common.alertCallback = function () {
      //                 location.href = newPageUrl.COMPANY;
      //             };
      //             Common.alert('success');
      //         } else {
      //             Common.alert(response.data.message);
      //         }
      //     })
      //     .catch(function (error) {
      //         console.log(error.response);
      //     });
    },
    async update() {
      vm.children = null;
      try {
        console.log("update companies", this.thirdFiles);
        const payload = this.getFormData();
        const response = await axios.put(
          `/api/companies/edit-multipart/${vm.company.id}`,
          payload
        );
        if (response.data.success) {
          Common.alertCallback = function () {
            location.href = newPageUrl.COMPANY;
          };
          Common.alert("success");
        } else {
          Common.alert(response.data.message);
        }
      } catch (error) {
        console.log(error);
      }
    },
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
            case "COMPANY_PICTURE":
              vm.uploadedFiles = response.data;
              break;
          }
        });
    },
    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = vm.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = vm.uploadedFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    async initFiles() {
      try {
        const param = {
          storageTypes: "COMPANY_PICTURE",
          refId: this.company.id,
        };
        const response = await axios.get(
          "/api/file-storage/mold?" + Common.param(param)
        );
        this.uploadedFiles = response.data["COMPANY_PICTURE"]
          ? response.data["COMPANY_PICTURE"]
          : [];
      } catch (error) {
        console.log(error);
      }
    },
  },
  computed: {
    readonlyCompanyCode: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },
  },
  mounted() {
    this.$nextTick(function () {
      if (Page.IS_EDIT) {
        // 데이터 조회
        axios.get(Page.API_GET).then(function (response) {
          vm.company = response.data;
          vm.company_type = vm.CompanyType.filter(
            (item) => item.code === vm.company.companyType
          )[0].title;
          vm.initFiles();
        });
      }
    });
  },
});
