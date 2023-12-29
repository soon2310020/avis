// 0.4.1 VERSION
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-data-table", EmoldinoComponents.DataTable);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component(
  "emdn-tooltip-floating-vue",
  EmoldinoComponents.TooltipFloatingVue
);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-calendar", EmoldinoComponents.Calendar);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-list-group", EmoldinoComponents.ListGroup);
Vue.component("emdn-widget", EmoldinoComponents.Widget);
Vue.component("emdn-pie-chart", EmoldinoComponents.PieChart);
Vue.component("emdn-progress-bar", EmoldinoComponents.ProgressBar);
Vue.component("emdn-table-status", EmoldinoComponents.TableStatus);

// page contents
Vue.component(
  "dashboard-overview-template",
  httpVueLoader("/components/templates/dashboard-overview-template.vue")
);
Vue.component(
  "customization-widget",
  httpVueLoader("/components/customization-widget/index.vue")
);
// Vue.component(
//   "tooling-distribution-widget",
//   httpVueLoader("/components/tooling-distribution/widget/index.vue")
// );
Vue.component(
  "overall-utilization-rate-widget",
  httpVueLoader("/components/assets/widget/overall-utilization.vue")
);
Vue.component(
  "demand-compliance-tracker-widget",
  httpVueLoader("/components/demand-compliance/widget/index.vue")
);
Vue.component(
  "pm-compliance-widget",
  httpVueLoader("/components/pm-compliance/widget/index.vue")
);
Vue.component(
  "work-order-status-widget",
  httpVueLoader("/components/work-order/widget/work-order-status.vue")
);
Vue.component(
  "inactive-toolings-widget",
  httpVueLoader("/components/assets/widget/inactive-toolings.vue")
);
Vue.component(
  "operational-summary-widget",
  httpVueLoader("/components/assets/widget/operational-summary.vue")
);
Vue.component(
  "total-toolings-widget",
  httpVueLoader("/components/assets/widget/total-toolings.vue")
);
Vue.component(
  "total-cost-of-ownership-widget",
  httpVueLoader("/components/assets/widget/total-cost-of-ownership.vue")
);
Vue.component(
  "end-of-life-widget",
  httpVueLoader("/components/assets/widget/end-of-life.vue")
);
Vue.component(
  "relocations-widget",
  httpVueLoader("/components/assets/widget/relocations.vue")
);
Vue.component(
  "cycle-time-compliance-widget",
  httpVueLoader("/components/assets/widget/cycle-time-compliance.vue")
);
Vue.component(
  "maintenance-duration-widget",
  httpVueLoader("/components/assets/widget/maintenance-duration.vue")
);

// kit
Vue.component(
  "base-paging",
  httpVueLoader("/components/@base/paging/base-paging.vue")
);

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

new Vue({
  el: "#app",
  data() {
    return {
      resources: {},
    };
  },
  components: {
    DashboardPage: httpVueLoader("/components/pages/common/dsh/index.vue"),
  },
  methods: {},
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (!newVal) return;
        this.resources = Object.assign({}, this.resources, newVal);
      },
      { immediate: true }
    );
  },
  mounted() {
    this.$nextTick(function () {
      removeWave(500);
    });
  },
});
