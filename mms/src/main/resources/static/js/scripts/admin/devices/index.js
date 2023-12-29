Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component(
  "preview-images-system",
  httpVueLoader("/components/common/preview-images-system.vue")
);
Vue.component(
  "reported-work-order-dialog",
  httpVueLoader("/components/work-order/src/reported-work-order-dialog.vue")
);
Vue.component(
  "base-images-slider",
  httpVueLoader("/components/@base/images-preview/base-images-slider.vue")
);
Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Devices";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};

var Page = Common.getPage("data-requests");
var Vm = new Vue({
  el: "#app",
  components: {
    devices: httpVueLoader("/components/devices/index.vue"),
  },
});
