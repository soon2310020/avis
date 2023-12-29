Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-date-picker", EmoldinoComponents.Calendar);
Vue.component("emdn-dropdown-root", EmoldinoComponents.DropdownRoot);
Vue.component("emdn-dropdown-content", EmoldinoComponents.DropdownContent);
Vue.component("emdn-dropdown-trigger", EmoldinoComponents.DropdownTrigger);
Vue.component("emdn-dropdown-portal", EmoldinoComponents.DropdownPortal);
Vue.component(
  "part-quality-risk-content",
  EmoldinoComponents.PartQualityRiskContent
);

window.onload = function () {
  document.title = "SCM" + " | eMoldino";
};

new Vue({
  el: "#app",
  components: {
    "par-qua-rsk-page": httpVueLoader(
      "/components/pages/supplychain/par-qua-rsk/index.vue"
    ),
  },
});
