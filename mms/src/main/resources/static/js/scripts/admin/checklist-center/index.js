axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Checklist Center" + " | eMoldino";
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
    "maintenance-checklist": httpVueLoader(
      "/components/checklist-center/maintenance-checklist.vue"
    ),
    "quality-assurance-checklist": httpVueLoader(
      "/components/checklist-center/quality-assurance-checklist.vue"
    ),
    "reject-rate-checklist": httpVueLoader(
      "/components/checklist-center/reject-rate-checklist.vue"
    ),
    "refurbishment-checklist": httpVueLoader(
      "/components/checklist-center/refurbishment-checklist.vue"
    ),
    "disposal-checklist": httpVueLoader(
      "/components/checklist-center/disposal-checklist.vue"
    ),
    "general-checklist": httpVueLoader(
      "/components/checklist-center/general-checklist.vue"
    ),
    picklist: httpVueLoader("/components/checklist-center/picklist.vue"),
  },
  data() {
    return {
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
      searchText: "",
    };
  },
  methods: {
    handleChangeSearchText(text) {
      console.log("handleChangeSearchText", text);
      this.searchText = text;
    },
    initLoad() {
      this.menuList = [
        {
          key: "CHECKLIST_GENERAL",
          menuKey: "CHECKLIST_GENERAL",
          iconPath: "/images/icon/checklist/general-icon.svg",
          iconSrc: "/images/icon/checklist/general-icon.svg",
          iconStyle: "width: 16px; height: 21px; -webkit-mask-size: 14px;",
          menuName: this.resources["general"],
          title: this.resources["general"],
        },
        {
          key: "CHECKLIST_MAINTENANCE",
          menuKey: "CHECKLIST_MAINTENANCE",
          iconPath: "/images/icon/checklist/maintenance-icon.svg",
          iconSrc: "/images/icon/checklist/maintenance-icon.svg",
          iconStyle: "width: 17px; height: 17px; -webkit-mask-size: 17px;",
          menuName: this.resources["maintenance"],
          title: this.resources["maintenance"],
        },
        {
          key: "CHECKLIST_QUALITY_ASSURANCE",
          menuKey: "CHECKLIST_QUALITY_ASSURANCE",
          iconPath: "/images/icon/checklist/quality-assurance-icon.svg",
          iconSrc: "/images/icon/checklist/quality-assurance-icon.svg",
          iconStyle: "width: 17px; height: 17px; -webkit-mask-size: 14px;",
          menuName: this.resources["quality_assurance"],
          title: this.resources["quality_assurance"],
        },
        {
          key: "PICK_LIST",
          menuKey: "PICK_LIST",
          iconPath: "/images/icon/checklist/picklist-icon.svg",
          iconSrc: "/images/icon/checklist/picklist-icon.svg",
          iconStyle: "width: 17px; height: 17px;-webkit-mask-size: 13px;",
          menuName: this.resources["pick_list"],
          title: this.resources["pick_list"],
        },
        {
          key: "CHECKLIST_REFURBISHMENT",
          menuKey: "CHECKLIST_REFURBISHMENT",
          iconPath: "/images/icon/checklist/refurbishment-icon.svg",
          iconSrc: "/images/icon/checklist/refurbishment-icon.svg",
          iconStyle: "width: 17px; height: 16px;-webkit-mask-size: 15px;",
          menuName: this.resources["refurbishment"],
          title: this.resources["refurbishment"],
        },
        {
          key: "CHECKLIST_DISPOSAL",
          menuKey: "CHECKLIST_DISPOSAL",
          iconPath: "/images/icon/checklist/disposal-icon.svg",
          iconSrc: "/images/icon/checklist/disposal-icon.svg",
          iconStyle: "width: 18px; height: 17px;-webkit-mask-size: 15px;",
          menuName: this.resources["disposal"],
          title: this.resources["disposal"],
        },
        {
          key: "CHECKLIST_REJECT_RATE",
          menuKey: "CHECKLIST_REJECT_RATE",
          iconPath: "/images/icon/checklist/reject-rate-icon.svg",
          iconSrc: "/images/icon/checklist/reject-rate-icon.svg",
          iconStyle: "width: 17px; height: 19px;-webkit-mask-size: 14px;",
          menuName: this.resources["reject_rate"],
          title: this.resources["reject_rate"],
        },
      ];
    },
    changeSelect(tab) {
      const item = tab.key;
      if (this.requestParam.objectType !== item) {
        this.requestParam.objectType = item;
      }
    },
    onHover(code) {
      this.isHover = code;
    },

    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        this.resources = JSON.parse(messages);
        this.initLoad();
      } catch (error) {
        console.log(error);
      }
    },
  },
  async created() {
    await this.getResources(); //load in common]
    //focus menu
    let uri = URI.parse(location.href);
    console.log("uri.query:", uri.query);
    let params = Common.parseParams(uri.query);
    let currentKey = params.systemNoteFunction || "CHECKLIST_GENERAL";
    this.changeSelect({ key: currentKey });
  },
  mounted() {
    this.$nextTick(async function () {
      Common.removeWave(500);
    });
  },
});
