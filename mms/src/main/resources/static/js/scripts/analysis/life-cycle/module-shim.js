Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-calendar", EmoldinoComponents.Calendar);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-portal", EmoldinoComponents.Portal);
Vue.component("emdn-dropdown-root", EmoldinoComponents.DropdownRoot);
Vue.component("emdn-dropdown-content", EmoldinoComponents.DropdownContent);
Vue.component("emdn-dropdown-trigger", EmoldinoComponents.DropdownTrigger);
Vue.component("emdn-dropdown-portal", EmoldinoComponents.DropdownPortal);
Vue.component("emdn-dropdown-item", EmoldinoComponents.DropdownItem);

window.onload = function () {
  document.title = "Life Cycle" + " | eMoldino";
};

new Vue({
  el: "#app",
  components: {
    "life-cycle-page": httpVueLoader(
      "/components/pages/analysis/life-cycle/index.vue"
    ),
  },
});
