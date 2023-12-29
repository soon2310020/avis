{
  /* <script type="text/javascript"> */
}
window.onload = function () {
  document.title = "Reject Rate" + " | eMoldino";
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

Vue.prototype.$window = window;
var vm = new Vue({
  el: "#app",
  components: {
    "reject-rates-chart": httpVueLoader(
      "/components/reject-rates/chart/reject-rates-chart.vue"
    ),
  },
  data: {
    resources: {},
    chartParam: "",
  },
  methods: {
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
  },
  async created() {
    await this.getResources();
    let url_string = window.location.href;
    this.chartParam = /[^/]*$/.exec(url_string)[0];
  },
  mounted() {},
});
// </script>
