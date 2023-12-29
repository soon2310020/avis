axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

Vue.component("emdn-search-bar", EmoldinoComponents.SearchBar);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-check-list", EmoldinoComponents.CheckList);
Vue.component("emdn-alert-box", EmoldinoComponents.AlertBox);

Common.SearchJS = EmoldinoComponents.SearchJS;

console.log(Common.SearchJS);

window.onload = function () {
  document.title = "Role" + " | eMoldino";
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
    "permission-config-page": httpVueLoader(
      "/components/pages/permission-config.vue"
    ),
  },
  data() {
    return {
      resources: {},
    };
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
});
