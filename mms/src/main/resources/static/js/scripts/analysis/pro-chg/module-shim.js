Vue.component("emdn-xy-chart", EmoldinoComponents.XYChart);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-pagination", EmoldinoComponents.Pagination);
Vue.component("emdn-chart-legend", EmoldinoComponents.ChartLegend);
Vue.component("emdn-widget", EmoldinoComponents.Widget);
Vue.component("emdn-icon", EmoldinoComponents.Icon);
Vue.component("emdn-tooltip", EmoldinoComponents.Tooltip);
Vue.component("emdn-data-table", EmoldinoComponents.DataTableV3);
Vue.component("emdn-calendar", EmoldinoComponents.Calendar);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-widget-group", EmoldinoComponents.WidgetGroup);
Vue.component("emdn-widget-area", EmoldinoComponents.WidgetArea);
Vue.component(
  "emdn-process-change-content",
  EmoldinoComponents.ProcessChangeContent
);

function updateDocumentTitle(title) {
  document.title = `${title} | eMoldino`;
}

updateDocumentTitle("Process Change");

new Vue({
  el: "#app",
  components: {
    "process-change-page": httpVueLoader(
      "/components/pages/analysis/pro-chg/index.vue"
    ),
  },
});
