axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

setTimeout(() => {
  $("div").removeClass("wave");
  $("div").removeClass("wave1");
  $("div").removeClass("wave_sidebar");
  $("li").removeClass("wave_header");
  $("img").removeClass("wave_img");
}, 500);

document.title = "Display Setting";
var Page = Common.getPage("display-setting");
var vm = new Vue({
  el: "#app",
  components: {
    "display-setting": httpVueLoader("/components/display-setting/index.vue"),
  },
});
