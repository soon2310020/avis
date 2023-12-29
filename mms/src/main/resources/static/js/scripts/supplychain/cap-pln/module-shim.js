Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-list-search-bar", EmoldinoComponents.ListSearchBar);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-chart-legend", EmoldinoComponents.ChartLegend);
Vue.component("emdn-dropdown-root", EmoldinoComponents.DropdownRoot);
Vue.component("emdn-dropdown-content", EmoldinoComponents.DropdownContent);
Vue.component("emdn-dropdown-trigger", EmoldinoComponents.DropdownTrigger);
Vue.component("emdn-dropdown-portal", EmoldinoComponents.DropdownPortal);
Vue.component("emdn-dropdown-item", EmoldinoComponents.DropdownItem);

window.onload = function () {
  document.title = "SCM" + " | eMoldino";
};

new Vue({
  el: "#app",
  components: {
    "cap-pln-page": httpVueLoader(
      "/components/pages/supplychain/cap-pln/index.vue"
    ),
  },
});
