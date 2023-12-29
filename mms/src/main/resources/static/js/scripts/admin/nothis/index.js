new Vue({
  el: "#app",
  components: {
    "notification-history-content": httpVueLoader(
      "/components/pages/admin/not-his/index.vue"
    ),
  },
});
