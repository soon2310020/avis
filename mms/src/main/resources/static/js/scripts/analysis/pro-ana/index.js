Vue.component("emdn-tab-button", EmoldinoComponents.TabButton);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-overall-xy-chart", EmoldinoComponents.OverallXYChart);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-search-bar", EmoldinoComponents.SearchBar);
Vue.component("emdn-popover", EmoldinoComponents.Popover);
Vue.component("emdn-icon-button", EmoldinoComponents.IconButton);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-list-search-bar", EmoldinoComponents.ListSearchBar);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-progress-bar", EmoldinoComponents.ProgressBar);
Vue.component("emdn-custom-list", EmoldinoComponents.CustomList);
Vue.component("content-modal", EmoldinoComponents.ContentModal);
Vue.component("emdn-table-status", EmoldinoComponents.TableStatus);
Vue.component("emdn-dot-spinner", EmoldinoComponents.DotSpinner);
Vue.component("emdn-chart-legend", EmoldinoComponents.ChartLegend);

function updateDocumentTitle(title) {
  document.title = `${title} | eMoldino`;
}

updateDocumentTitle("Process Analysis");

new Vue({
  el: "#app",
  components: {
    "process-analysis-main": httpVueLoader(
      "/components/pages/analysis/pro-ana/index.vue"
    ),
  },
});
