Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-search-bar", EmoldinoComponents.SearchBar);
Vue.component("emdn-popover", EmoldinoComponents.Popover);
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-table-input", EmoldinoComponents.TableInput);
Vue.component("emdn-alert-box", EmoldinoComponents.AlertBox);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-line-spinner", EmoldinoComponents.LineSpinner);

window.onload = function () {
  document.title = "SCM" + " | eMoldino";
};

new Vue({
  el: "#app",
  components: {
    "dem-cpl-input-page": httpVueLoader(
      "/components/pages/supplychain/dem-cpl/dem-cpl-input/index.vue"
    ),
  },
});
