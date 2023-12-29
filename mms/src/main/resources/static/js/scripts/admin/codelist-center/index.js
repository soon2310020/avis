axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Code List Center" + " | eMoldino";
  Common.removeWave(500);
};

Vue.component(
  "show-columns",
  httpVueLoader("/components/part/show-columns.vue")
);
Vue.component(
  "customization-modal",
  httpVueLoader("/components/customization-modal.vue")
);

var vm = new Vue({
  el: "#app",
  components: {
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "downtime-codelist": httpVueLoader(
      "/components/codelist-center/downtime-codelist.vue"
    ),
    "downtime-mainlevel-codelist": httpVueLoader(
      "/components/codelist-center/downtime-mainlevel-codelist.vue"
    ),
    "downtime-sublevel-codelist": httpVueLoader(
      "/components/codelist-center/downtime-sublevel-codelist.vue"
    ),
  },
  data: {
    resources: {},
    menuList: [],
    isHover: null,
    requestParam: {
      query: "",
      status: "active",
      sort: "id,desc",
      page: 1,
      objectType: null,
    },
  },
  async created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );

    //focus menu
    let uri = URI.parse(location.href);
    console.log("uri.query:", uri.query);
    let params = Common.parseParams(uri.query);
    let currentKey = params.systemNoteFunction || "DOWNTIME";
    this.changeSelect(currentKey);
  },
  methods: {
    initMenuList() {
      this.menuList = [
        {
          menuKey: "DOWNTIME",
          iconPath: "/images/icon/codelist/downtime-icon-hover.svg",
          iconPathDefault: "/images/icon/codelist/downtime-icon-default.svg",
          menuName: this.resources["downtime"],
        },
        {
          menuKey: "DOWNTIME_MAIN_LEVEL",
          iconPath: "/images/icon/codelist/downtime-mainlevel-icon-hover.svg",
          iconPathDefault:
            "/images/icon/codelist/downtime-mainlevel-icon-default.svg",
          menuName: this.resources["downtime_main_level"],
        },
        {
          menuKey: "DOWNTIME_SUB_LEVEL",
          iconPath: "/images/icon/codelist/downtime-mainlevel-icon-hover.svg",
          iconPathDefault:
            "/images/icon/codelist/downtime-mainlevel-icon-default.svg",
          menuName: this.resources["downtime_sub_level"],
        },
        // {
        //   menuKey:'CHECKLIST_MAINTENANCE',
        //   iconPath:'/images/icon/checklist/maintenance-icon-hover.svg',
        //   iconPathDefault:'/images/icon/checklist/maintenance-icon-default.svg',
        //   menuName:this.resources['maintenance']
        // },
        // {
        //   menuKey:'CHECKLIST_REJECT_RATE',
        //   iconPath:'/images/icon/checklist/reject-rate-icon-hover.svg',
        //   iconPathDefault:'/images/icon/checklist/reject-rate-icon-default.svg',
        //   menuName:this.resources['reject_rate']
        // }
      ];
    },
    changeSelect(item) {
      if (this.requestParam.objectType !== item) {
        this.requestParam.objectType = item;
      }
    },
    onHover(code) {
      this.isHover = code;
    },
  },
  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
  },
});
