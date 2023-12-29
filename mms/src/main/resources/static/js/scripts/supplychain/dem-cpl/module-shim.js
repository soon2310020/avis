Vue.component("emdn-tab-button", EmoldinoComponents.TabButton);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-overall-xy-chart", EmoldinoComponents.OverallXYChart);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-search-bar", EmoldinoComponents.SearchBar);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-popover", EmoldinoComponents.Popover);
Vue.component("emdn-list-search-bar", EmoldinoComponents.ListSearchBar);
Vue.component("emdn-alert-box", EmoldinoComponents.AlertBox);
Vue.component("emdn-table-input", EmoldinoComponents.TableInput);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-portal", EmoldinoComponents.Portal);
Vue.component("emdn-table-status", EmoldinoComponents.TableStatus);
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
    "dem-cpl-page": httpVueLoader(
      "/components/pages/supplychain/dem-cpl/index.vue"
    ),
  },
});
