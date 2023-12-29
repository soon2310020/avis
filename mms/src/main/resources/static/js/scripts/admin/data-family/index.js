axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));

setTimeout(() => {
  $("div").removeClass("wave");
  $("div").removeClass("wave1");
  $("div").removeClass("wave_sidebar");
  $("li").removeClass("wave_header");
  $("img").removeClass("wave_img");
}, 500);

document.title = "Data Family";
var Page = Common.getPage("data-family");
var vm = new Vue({
  el: "#app",
  components: {
    "data-family": httpVueLoader("/components/data-family/index.vue"),
  },
});
