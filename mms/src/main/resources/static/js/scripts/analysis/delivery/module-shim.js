Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-data-table", EmoldinoComponents.DataTable);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-calendar", EmoldinoComponents.Calendar);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-list-group", EmoldinoComponents.ListGroup);

axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

let removeWave = function (time) {
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    if (document.getElementById("removee"))
      document.getElementById("removee").remove();
  }, time);
};

window.onload = function () {
  document.title = "Delivery" + " | eMoldino";
  removeWave(500);
  removeWave(2000);
};

var vm = new Vue({
  el: "#app",
  data: {
    resources: {},
  },
  components: {
    "delivery-page": httpVueLoader(
      "/components/pages/analysis/delivery/index.vue"
    ),
  },
  async created() {
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
