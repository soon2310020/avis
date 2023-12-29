Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-table-status", EmoldinoComponents.TableStatus);
Vue.component("emdn-progress-bar", EmoldinoComponents.ProgressBar);
Vue.component("emdn-dropdown-root", EmoldinoComponents.DropdownRoot);
Vue.component("emdn-dropdown-content", EmoldinoComponents.DropdownContent);
Vue.component("emdn-dropdown-trigger", EmoldinoComponents.DropdownTrigger);
Vue.component("emdn-dropdown-portal", EmoldinoComponents.DropdownPortal);
Vue.component("emdn-dropdown-item", EmoldinoComponents.DropdownItem);

window.onload = function () {
  document.title = "ASSET" + " | eMoldino";
};

new Vue({
  el: "#app",
  components: {
    "tol-eol-page": httpVueLoader("/components/pages/asset/tol-eol/index.vue"),
  },
});
