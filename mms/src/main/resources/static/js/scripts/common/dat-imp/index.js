axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Overview" + " | eMoldino";
  Common.removeWave(500);
  Common.removeWave(2000);
};

var vm = new Vue({
  el: "#app",
  components: {
    VueApp: httpVueLoader("/components/data-import/index.vue"),
  },
});
