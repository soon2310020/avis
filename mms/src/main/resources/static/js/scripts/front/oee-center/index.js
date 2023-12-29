axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

// Declare common components
Vue.component(
  "date-picker-button",
  httpVueLoader("/components/common/date-picker/date-picker-button.vue")
);
Vue.component(
  "date-picker-modal",
  httpVueLoader("/components/common/date-picker/date-picker-modal.vue")
);
Vue.component(
  "primary-button",
  httpVueLoader("/components/@base/primary-button.vue")
);
Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);

let removeWave = function (time) {
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    if (document.getElementById("removee"))
      document.getElementById("removee").remove();
  }, time);
};

window.loading = true;
document.addEventListener("readystatechange", (event) => {
  if (
    event.target.readyState === "complete" &&
    window.headerVm &&
    window.loading
  ) {
    delete window.loading;
    document.title = "OEE Center" + " | eMoldino";
    removeWave(500);
    removeWave(2000);
    if (document.getElementById("remove"))
      document.getElementById("remove").remove();
    new Vue({
      el: "#app",
      components: {
        "oee-center": httpVueLoader("/components/oee-center.vue"),
      },
      setup() {
        const allPermissions = useSystem("menuPermissions");
        const resources = useSystem("messages");

        const permissions = computed(() => {
          if (!allPermissions.value) return {};
          const permissionCode = Common.PERMISSION_CODE.OEE_CENTER;
          const oeePermissions = allPermissions.value[permissionCode]?.children;
          return oeePermissions ?? {};
        });

        return { permissions, resources };
      },
    });
  }
});
