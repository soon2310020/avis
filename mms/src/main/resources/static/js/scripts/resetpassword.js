window.onload = function () {
  document.title = "Reset Password" + " | eMoldino";
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";
var vm = new Vue({
  el: "#app",
  data: {
    password_n: "",
    password_c: "",
    has_number: false,
    has_lowercase: false,
    has_uppercase: false,
    has_special: false,
    typing: false,
  },
  methods: {
    password_check: function () {
      this.typing = true;
      this.has_number = /\d/.test(this.password_n);
      this.has_lowercase = /[a-z]/.test(this.password_n);
      this.has_uppercase = /[A-Z]/.test(this.password_n);
      this.has_special =
        /(?=.*[.,!@#$%^&*\+=\-_])[A-Za-z\d.,!@#$%^&*\+=\-_]/.test(
          this.password_n
        );
    },

    submit: function () {
      var token = $("#token").text();

      if (this.password_n != this.password_c) {
        Common.alert("The entered passwords do not match");
        return;
      }

      axios
        .post("/api/users/reset_password/" + token + "/" + this.password_n)
        .then(function (response) {
          if (response.status == 200) {
            window.location.href = "/api/users/password_changed";
          }
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
  },
});
