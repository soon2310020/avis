let pic = null;
axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  let self = this;
  document.title = "Profile" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

var vm = new Vue({
  el: "#app",
  components: {
    "delete-image": httpVueLoader("/components/profile/image-delete.vue"),
    "cta-button": httpVueLoader("/components/common/cta-button.vue"),
  },
  data: {
    enable: 1,
    resources: Object,
    disable: true,
    mob: "",
    bindProps: {
      enabledCountryCode: false,
      autocomplete: "off",
      mode: "international",
    },
    dropdown: false,
    user: {
      name: "",
      email: "",
      company: {
        name: "",
      },
    },
    contact: null,
    password: "",
    confirmPassword: "",
    typing: false,
    show: false,
    params: {
      token: "123456798",
      name: "avatar",
    },
    headers: {
      smail: "*_~",
    },
    imgDataUrl: null,
    imgName: "",
    success: false,
    close: false,
    position: "",
    userName: "",
    dept: "",
    position1: "",
    contactNumber: "",
    eye: true,
    type: "password",
    eye1: true,
    type1: "password",
    language: "",
    isOpenedLanguageDropdown: false,
    languageOptions: [
      {
        title: "English",
        value: "EN",
      },
      {
        title: "Chinese",
        value: "ZH",
      },
      {
        title: "German",
        value: "DE",
      },
      {
        title: "Portuguese",
        value: "PT",
      },
      {
        title: "French",
        value: "FR",
      },
      {
        title: "Italian",
        value: "IT",
      },
      {
        title: "Spanish",
        value: "ES",
      },
      {
        title: "Japanese",
        value: "JA",
      },
      {
        title: "Turkish",
        value: "TR",
      },
      {
        title: "Korean",
        value: "KO",
      },
    ],
    passwordProperties: {
      hasNumber: false,
      hasLowercase: false,
      hasUppercase: false,
      hasSpecial: false,
    },
    confirmPasswordProperties: {
      hasNumber: false,
      hasLowercase: false,
      hasUppercase: false,
      hasSpecial: false,
    },
  },
  computed: {
    displayLanguage() {
      return this.language
        ? this.languageOptions.find((item) => item.value === this.language)
            .title
        : this.languageOptions[0].title;
    },
  },
  watch: {
    disable: function (val) {},
    password: function (val) {
      if (this.enable == 2) {
        this.disable = false;
      } else {
        this.enable = 2;
      }
    },
    confirmPassword: function (val) {
      this.disable = false;
    },
    dept: function (val) {
      vm.user.department = val;
      this.disable = false;
    },
    position1: function (val) {
      vm.user.position = val;
      this.disable = false;
    },
    contactNumber: function (val) {
      vm.user.mobileNumber = val;
      this.disable = false;
    },
  },

  methods: {
    eyeChange() {
      this.eye = !this.eye;
      if (this.eye) {
        this.type = "password";
      } else {
        this.type = "text";
      }
    },
    passwordCheck() {
      this.passwordProperties.hasNumber = /\d/.test(this.password);
      this.passwordProperties.hasLowercase = /[a-z]/.test(this.password);
      this.passwordProperties.hasUppercase = /[A-Z]/.test(this.password);
      this.passwordProperties.hasSpecial = /[!@#\$%\^\&*\)\(+=._-]/.test(
        this.password
      );
    },
    confirmPasswordCheck() {
      this.confirmPasswordProperties.hasNumber = /\d/.test(
        this.confirmPassword
      );
      this.confirmPasswordProperties.hasLowercase = /[a-z]/.test(
        this.confirmPassword
      );
      this.confirmPasswordProperties.hasUppercase = /[A-Z]/.test(
        this.confirmPassword
      );
      this.confirmPasswordProperties.hasSpecial = /[!@#\$%\^\&*\)\(+=._-]/.test(
        this.confirmPassword
      );
    },
    eyeChange1() {
      this.eye1 = !this.eye1;
      if (this.eye1) {
        this.type1 = "password";
      } else {
        this.type1 = "text";
      }
    },
    closeSuccess() {
      this.success = false;
    },
    showWarning() {
      $(this.getRootId() + "#op-image-delete").modal("show");
      this.handleDropdown();
    },
    confirmRemove() {
      this.removeImgUploaded();
      $(this.getRootId() + "#op-image-delete").modal("hide");
      this.success = true;
      setTimeout(() => {
        this.success = false;
      }, 5000);
      this.disable = true;
    },
    removeProfile() {
      this.imgDataUrl = null;
      this.imgName = null;
    },
    removeImgUploaded() {
      this.imgDataUrl = null;
      this.imgName = null;
    },
    cropSuccess(imgDataUrl) {
      this.imgDataUrl = imgDataUrl;
      this.success = true;
      setTimeout(() => {
        this.success = false;
      }, 5000);
      this.disable = true;
    },
    cropUploadSuccess(jsonData, field) {},
    cropUploadFail(status, field) {},
    getFileSet(name) {
      this.imgName = name;
    },
    submit() {
      if (this.password == "" && this.confirmPassword == "") {
        axios
          .put("/api/users/my", vm.user)
          .then(function (response) {
            if (response.data.success) {
              vm.userName = vm.user.name;
              vm.position = vm.user.position;
              vm.language = vm.user.language;
              vm.success = true;
              setTimeout(() => {
                vm.success = false;
              }, 5000);
            } else {
              Common.alert(response.data.message);
            }
          })
          .catch(function (error) {
            console.log(error.response);
          });
        this.disable = true;
      } else if (this.password == this.confirmPassword) {
        this.user.password = this.password;
        axios
          .put("/api/users/my", vm.user)
          .then(function (response) {
            if (response.data.success) {
              vm.success = true;
              vm.userName = vm.user.name;
              vm.position = vm.user.position;
              vm.language = vm.user.language;
              setTimeout(() => {
                vm.success = false;
              }, 5000);
            } else {
              Common.alert(response.data.message);
            }
          })
          .catch(function (error) {
            console.log(error.response);
          });
        this.disable = true;
      } else {
        Common.alert("Confirm password not matched with password");
      }
    },
    showUploadProfile() {
      this.show = !this.show;
      this.handleDropdown();
    },
    changeCountryMobile: function (value) {
      this.user.mobileDialingCode = value.dialCode;
      this.disable = false;
    },

    handleDropdown() {
      this.dropdown = !this.dropdown;
      this.disable = true;
    },
    getResources() {
      return axios
        .get("/api/resources/getAllMessagesOfCurrentUser")
        .then(function (response) {
          return response;
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
    fetchData() {
      return axios
        .get("/api/users/my")
        .then(function (response) {
          vm.user = response.data;
          vm.userName = vm.user?.name;
          vm.position = vm.user?.position;
          vm.position1 = vm.user?.position;
          vm.dept = vm.user?.department;
          vm.contactNumber = vm.user?.mobileNumber;
          vm.language = vm.user?.language;
          return response;
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
    setLanguage(e, item) {
      this.language = item.value;
      this.user.language = this.language;
      this.disable = false;
    },
  },
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );
  },
  async mounted() {
    // await this.getResources()
    //   .then((result) => {
    //     this.resources = result.data;
    //   })
    //   .catch((error) => {
    //     Common.alert(error.response.data, "Error");
    //   });

    await this.fetchData();

    this.disable = true;
  },
});
