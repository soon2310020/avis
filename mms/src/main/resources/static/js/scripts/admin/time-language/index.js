axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
  document.title = "Time & Language" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

var Page = Common.getPage("molds");

var vm = new Vue({
  el: "#app",
  components: {},
  data: {
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
    ],
    language: "EN",
    localTimeZone: true,
    language_option: "Languages",
    showlanguage: false,
  },
  methods: {
    selectLanguage(item) {
      this.language_option = item.title;
      this.language = item.value;
      // console.error("Language selected ..", item.value)
      this.updateLanguage();
      // alert( this.language+this.language_option);
      this.showlanguage = false;
    },
    handleshowlanguage() {
      this.showlanguage = true;
    },
    handlecloselanguage() {
      this.showlanguage = false;
    },
    handleLanguageToggle: function (isShow) {
      this.showlanguage = isShow;
    },
    // setBrowserTime() {
    //     let element = document.getElementById('current-browser-time');
    //     let timezoneName = Intl.DateTimeFormat().resolvedOptions().timeZone;
    //     let timezone = 'UTC' + moment().format('ZZ');
    //     setInterval(function () {
    //         let datetime = moment().format('YYYY-MM-DD HH:mm:ss');
    //         element.innerHTML = `(${timezone}) ${timezoneName} ${datetime}`;
    //     }, 1000);
    // },
    updateConfigData() {
      let params = {
        language: this.language,
        localTimeZone: this.localTimeZone,
      };
      axios
        .post("/api/config/time-language/update", params)
        .then(function (response) {
          console.log("success", response);
          //update for common
          updateCommonConfig(response);
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    setCurrentLanguage() {
      axios
        .get("/api/users/get_lang")
        .then(function (response) {
          this.language = response.data;
          // $('#language').val(this.language);
          var valObj = vm.languageOptions.filter(function (elem) {
            if (elem.value == response.data) {
              vm.language_option = elem.title;
            }
          });
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
    updateLanguage() {
      // axios.post("/api/users/change_language/" + $('#language').val())
      axios
        .post("/api/users/change_language/" + this.language)
        .then(function (response) {
          location.reload();
        })
        .catch(function (error) {
          Common.alert(error.response.data);
        });
    },
  },
  mounted() {
    this.setCurrentLanguage();
  },
});
