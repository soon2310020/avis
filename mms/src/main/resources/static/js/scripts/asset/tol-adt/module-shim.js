Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-progress-bar", EmoldinoComponents.ProgressBar);
Vue.component("emdn-widget", EmoldinoComponents.Widget);
Vue.component("emdn-timeline-stepper", EmoldinoComponents.TimelineStepper);
Vue.component("emdn-table-status", EmoldinoComponents.TableStatus);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-search-bar", EmoldinoComponents.SearchBar);
Vue.component("emdn-widget-group", EmoldinoComponents.WidgetGroup);
Vue.component("emdn-widget-area", EmoldinoComponents.WidgetArea);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);

Vue.component("emdn-bingmap-provider", EmoldinoComponents.BingMapProvider);
Vue.component("emdn-bingmap", EmoldinoComponents.BingMap);
Vue.component("emdn-googlemap-provider", EmoldinoComponents.GoogleMapProvider);
Vue.component("emdn-googlemap", EmoldinoComponents.GoogleMap);

window.onload = function () {
  document.title = "Audit" + " | eMoldino";
  Common.removeWave(500);
  Common.removeWave(2000);
};

new Vue({
  el: "#app",
  components: {
    "audit-page": httpVueLoader("/components/pages/asset/tol-adt/index.vue"),
  },
  data() {
    return {};
  },
});
