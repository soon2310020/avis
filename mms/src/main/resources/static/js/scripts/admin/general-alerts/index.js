axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "General Alerts";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};
Vue.component(
  "company-details",
  httpVueLoader("/components/company-details.vue")
);
Vue.component("notify-alert", httpVueLoader("/components/notify-alert.vue"));
Vue.component(
  "chart-mold",
  httpVueLoader("/components/chart-mold/chart-mold-modal.vue")
);
Vue.component("mold-details", httpVueLoader("/components/mold-details.vue"));
Vue.component("message-form", httpVueLoader("/components/message-form.vue"));
Vue.component(
  "location-history",
  httpVueLoader("/components/location-history.vue")
);
Vue.component(
  "relocation-confirm-history",
  httpVueLoader("/components/relocation-confirm-history.vue")
);
Vue.component(
  "customization-modal",
  httpVueLoader("/components/customization-modal.vue")
);
Vue.component(
  "action-bar-feature",
  httpVueLoader("/components/new-feature/new-feature.vue")
);
Vue.component("chart-part", httpVueLoader("/components/chart-part.vue"));

Vue.component(
  "message-details",
  httpVueLoader("/components/message-details.vue")
);
Vue.component(
  "message-confirm",
  httpVueLoader("/components/alert-center/data-approval/message-confirm.vue")
);
Vue.component("mold-status", httpVueLoader("/components/mold-status.vue"));
Vue.component(
  "counter-status",
  httpVueLoader("/components/counter-status.vue")
);

Vue.component(
  "select-all",
  httpVueLoader("/components/alert-center/components/select-all.vue")
);

var Page = Common.getPage("general-alerts");
var vm = new Vue({
  el: "#app",
  components: {
    "general-alert": httpVueLoader(
      "/components/alert-based-pages/general-alert/index.vue"
    ),
  },
});
