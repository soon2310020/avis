Vue.component("emdn-tab-button", EmoldinoComponents.TabButton);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-overall-xy-chart", EmoldinoComponents.OverallXYChart);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);

Common.ChartJS = ChartJS;

new Vue({
  el: "#app",
  components: {
    "mld-cht-page": httpVueLoader(
      "/components/pages/analysis/mld-cht/index.vue"
    ),
  },
});
