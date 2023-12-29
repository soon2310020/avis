Vue.component("emdn-tab-button", EmoldinoComponents.TabButton);
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-data-table", EmoldinoComponents.DataTable);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-calendar", EmoldinoComponents.Calendar);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-modal", EmoldinoComponents.Modal);

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

window.onload = () => {
  document.title = "Analysis Quality" + " | eMoldino";
  removeWave(500);
  removeWave(2000);
};

window.loading = true;
document.addEventListener("readystatechange", (event) => {
  if (
    event.target.readyState === "complete" &&
    window.loading &&
    window.headerVm
  ) {
    delete window.loading;
    var vm = new Vue({
      el: "#app",
      data: {
        resources: {},
      },
      components: {
        "quality-page": httpVueLoader(
          "/components/pages/analysis/quality/index.vue"
        ),
      },
      async created() {
        this.getResources();
      },
      methods: {
        async getResources() {
          let self = this;
          try {
            const messages = await Common.getSystem("messages");
            vm.resources = JSON.parse(messages);
            // self.updateMenuName()
          } catch (error) {
            console.log(error);
          }
        },
      },
      mounted() {
        this.$nextTick(function () {
          removeWave(500);
          this.$forceUpdate();
        });
      },
    });
  }
});
