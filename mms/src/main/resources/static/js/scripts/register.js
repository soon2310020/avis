var callbackResult = false;
axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

window.onload = function () {
  document.title = "Register" + " | eMoldino";
};

var vm = new Vue({
  el: "#app",
  data: {
    user: {
      name: "",
      email: "",
      password: "",
      repeatPassword: "",
      accessLevel: "REGULAR",
      enabled: false,
      companyName: "",
      department: "",
      position: "",
      memo: "",
      // feild name for new feature
      contactDialingCode: "1",
      contactNumber: "",
      mobileDialingCode: "1",
      mobileNumber: "",
      requested: true,
    },
    show: false,
    type: "password",
    bindProps: {
      autocomplete: "off",
      mode: "international",
    },
    has_number: false,
    has_lowercase: false,
    has_uppercase: false,
    has_special: false,
    typing: false,
    pattern:
      "^(?=.*[A-Z])(?=.*\\d)(?=.*[.,!@#$%^&*\\+=\\-_])[A-Za-z\\d.,!@#$%^&*\\+=\\-_]{8,}$",
    isSubmitting: false,
  },
  methods: {
    changeCountryMobile: function (value) {
      console.log("value: ", value);
      this.user.mobileDialingCode = value.dialCode;
    },

    password_check: function () {
      this.typing = true;
      this.has_number = /\d/.test(this.user.password);
      this.has_lowercase = /[a-z]/.test(this.user.password);
      this.has_uppercase = /[A-Z]/.test(this.user.password);
      this.has_special =
        /(?=.*[.,!@#$%^&*\+=\-_])[A-Za-z\d.,!@#$%^&*\+=\-_]/.test(
          this.user.password
        );
    },

    changeCountryPhone: function (value) {
      this.user.contactDialingCode = value.dialCode;
    },

    goBack: function () {
      this.user.password = "";
      this.user.email = "";
      location.href = "/login";
    },

    showPassword: function () {
      if (this.type === "password") {
        this.type = "text";
        this.show = true;
      } else {
        this.type = "password";
        this.show = false;
      }
    },

    async submit() {
      console.log("submit");
      if (this.isSubmitting) {
        console.log("isSubmitting");
        return;
      }
      this.isSubmitting = true;
      if (this.user.password != this.user.repeatPassword) {
        Common.alert("The entered passwords do not match.");
        return;
      }

      this.user.memo = `Company Name: ${this.user.companyName} \n ${this.user.memo}`;
      console.log("this.user: ", this.user);

      try {
        const response = await axios.post("/api/users", this.user);
        if (response?.data?.success) {
          callbackResult = true;
          Common.alert(
            "Your request access has been successfully submitted. \n Once approved, we will send you a notification through email"
          );
        } else {
          callbackResult = false;
          Common.alert(response.data.message);
        }
      } catch (error) {
        console.log(error.response);
      } finally {
        this.user.memo = "";
        this.isSubmitting = false;
      }
    },

    clearForm: function () {
      Object.keys(vm.user).map((key) => {
        vm.user[key] = "";
      });
    },
  },

  watch: {
    "user.contactNumber": function (newVal, oldVal) {
      console.log("newVal: ", newVal);
    },
  },
  mounted() {
    this.$nextTick(() => Common.removeWave(500));
  },
});

Common.alertCallback = function () {
  if (callbackResult) {
    location.href = "/login";
  } else {
    vm.user.password = "";
    vm.user.repeatPassword = "";
  }
};
