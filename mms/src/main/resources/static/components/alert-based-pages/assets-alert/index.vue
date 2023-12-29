<template>
  <div>
    <div class="d-flex justify-space-between">
      <div>
        <master-filter-wrapper
          :filter-code="param.filterCode"
          :notify-filter-updated="getData"
        ></master-filter-wrapper>
      </div>
      <div class="col-md-6 mb-6 mb-md-0 col-12">
        <base-filter-search-bar
          :resources="resources"
          :placeholder="resources['search_by_selected_column']"
          @on-change="handleChangeSearchText"
        ></base-filter-search-bar>
      </div>
    </div>
    <div style="margin: 20px 0">
      <base-card-tab-list
        :list-tabs="listCards"
        :current-tab="activeCard"
        size="small"
        @change="handleChangeTab"
      ></base-card-tab-list>
    </div>
    <component
      :is="ALERT_COMPONENTS[activeCard]"
      :resources="resources"
      :rule-op-status="ruleOpStatus"
      :search-text="searchText"
      :reload-key="reloadKey"
      :enabled-search-bar="false"
      :show-create-workorder="showCreateWorkOrderModal"
      :show-note="handleShowNoteSystem"
    ></component>

    <!-- popup-->

    <terminal-details
      :resources="resources"
      ref="terminal-detail"
    ></terminal-details>
    <location-history :resources="resources"></location-history>
    <message-form ref="message-form" :resources="resources"></message-form>
    <relocation-confirm-history
      :resources="resources"
    ></relocation-confirm-history>
    <system-note
      ref="system-note"
      :system-note-function="systemNoteFunction"
      :resources="resources"
      :show-note="handleShowNoteSystem"
    ></system-note>
    <company-details :resources="resources"></company-details>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <chart-part :resources="resources"></chart-part>
    <chart-mold
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></chart-mold>
    <message-details :resources="resources"></message-details>

    <file-previewer :back="backToMoldDetail"></file-previewer>
    <creator-work-order-dialog
      :resources="resources"
      :is-show="workorderModalVisible"
      :asset-selected="assetSelected"
      :enable-asset-selection="false"
      :modal-type="'CREATE'"
      :priority-default="priority"
      :work-order-type="selectedWorkOrderType"
      :order-type-default="orderType"
      :is-tooling-asset-selected="isToolingAssetSelected"
      :is-terminal-asset-selected="isTerminalAssetSelected"
      @close="closeCreateWorkOrderModal"
    ></creator-work-order-dialog>
    <machine-details :resources="resources"></machine-details>
  </div>
</template>

<script>
// MF: Master Filter
const ALERT_TYPES = [
  // {
  //   key: "RELOCATION_OLD",
  //   permissionCode: Common.PERMISSION_CODE.ALERT_CENTER_RELOCATION,
  //   title: "Relocation Old",
  //   titleKey: "relocation",
  //   iconSrc: "/components/alert-based-pages/assets/relocation-icon.svg",
  // },
  {
    key: "RELOCATION",
    permissionCode: Common.PERMISSION_CODE.ALERT_CENTER_RELOCATION,
    title: "Relocation",
    titleKey: "relocation",
    iconSrc: "/components/alert-based-pages/assets/relocation-icon.svg",
  },
  // {
  //   key: "REFURBISHMENT_OLD",
  //   permissionCode: Common.PERMISSION_CODE.ALERT_CENTER_REFURBISHMENT,
  //   title: "End Of Life Old",
  //   titleKey: "end_of_life",
  //   iconSrc: "/components/alert-based-pages/assets/end_of_life-icon.svg",
  // },
  {
    key: "REFURBISHMENT",
    permissionCode: Common.PERMISSION_CODE.ALERT_CENTER_REFURBISHMENT,
    title: "End Of Life",
    titleKey: "end_of_life",
    iconSrc: "/components/alert-based-pages/assets/end_of_life-icon.svg",
  },
];

const ALERT_COMPONENTS = {
  // RELOCATION_OLD: "relocation-old",
  RELOCATION: "relocation",
  // REFURBISHMENT_OLD: "end-of-life-old",
  REFURBISHMENT: "end-of-life",
};

module.exports = {
  components: {
    // "end-of-life-old": httpVueLoader(
    //   "/components/alert-center/mold-refurbishment.vue"
    // ),
    "end-of-life": httpVueLoader(
      "/components/alert-center/assets-alert/mold-refurbishment.vue"
    ),
    // "relocation-old": httpVueLoader("/components/alert-center/mold-locations.vue"),
    relocation: httpVueLoader(
      "/components/alert-center/assets-alert/mold-locations.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "terminal-details": httpVueLoader("/components/terminal-details.vue"),
    "workorder-modal": httpVueLoader(
      "/components/work-order/work-order-modal.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      ALERT_COMPONENTS,
      resources: {},
      permissions: {},
      listCards: [...ALERT_TYPES],
      activeCard: "RELOCATION",
      workorderModalVisible: false,
      isToolingAssetSelected: true,
      isTerminalAssetSelected: false,
      assetSelected: [],
      priority: {},
      orderType: {},
      selectedWorkOrderType: null,
      ruleOpStatus: {
        inProduct: "",
        idle: "",
        inactive: "",
        sensorOffline: "",
        sensorDetached: "The sensor was physically detached from the tooling.",
        disabled: "The tooling has been  manually disabled.",
        disposed: "The tooling has been through the disposal process.",
        noSensor: "The tooling has no matched sensor.",
        onStandby:
          "The tooling has been matched with a sensor but not made any shot yet.",
      },
      isHover: null,
      requestParam: {
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
        objectType: null,
      },
      alertCountList: [],
      tab: null,
      machineId: null,
      searchText: "",
      param: {
        filterCode: "COMMON",
        query: "",
        sort: "",
        page: 1,
        size: 10,
      },
      reloadKey: 0,
      systemNoteFunctionMap: {
        relocation: "RELOCATION_ALERT",
        downtime: "DOWNTIME_ALERT",
        disconnection: "DISCONNECTION_ALERT",
        "cycle-time": "CYCLE_TIME_ALERT",
        maintenance: "MAINTENANCE_ALERT",
        uptime: "EFFICIENCY_ALERT",
        reset: "RESET_ALERT",
        "data-approval": "DATA_SUBMISSION_ALERT",
        refurbishment: "REFURBISHMENT_ALERT",
        detachment: "DETACHMENT_ALERT",
        "machine-downtime": "MACHINE_DOWNTIME_ALERT",
      },
      systemNoteFunction: "RELOCATION_ALERT",
    };
  },
  async created() {
    this.$watch(
      () => headerVm?.permissions,
      (newVal) => {
        if (newVal?.[Common.PERMISSION_CODE.ALERT_ASSETS]) {
          this.permissions = Object.assign(
            {},
            this.permissions,
            newVal?.[Common.PERMISSION_CODE.ALERT_ASSETS]?.children
          );

          this.listCards = this.listCards.filter(
            (i) => this.permissions[i.permissionCode]
          );
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );
    this.getOpConfig();
  },
  methods: {
    handleChangeTab(tab) {
      console.log("handleChangeTab", tab);
      this.activeCard = tab.key;
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
      this.selectedWorkOrderType = null;
    },
    showCreateWorkOrderModal(item, isMachine, type, isTerminal) {
      console.log(
        "showCreateWorkOrderModal item",
        item,
        isMachine,
        type,
        isTerminal
      );
      if (type) {
        this.selectedWorkOrderType = type;
      } else {
        this.selectedWorkOrderType = { name: "General", value: "GENERAL" };
        this.orderType = { name: "General", value: "GENERAL" };
      }
      if (isMachine) {
        console.log(1);
        this.assetSelected = [
          {
            id: item.machineId,
            title: item.machineCode,
            checked: true,
            assetView: "machine",
            locationCode: item.locationCode,
            locationName: item.locationName,
            locationId: item.locationId,
          },
        ];
        this.isToolingAssetSelected = false;
        this.isTerminalAssetSelected = false;
        this.workorderModalVisible = true;
      } else {
        if (isTerminal) {
          console.log(2);
          this.assetSelected = [
            {
              id: item.terminal.id,
              title: item.terminal.equipmentCode,
              checked: true,
              assetView: "terminal",
              locationCode: item.terminal.locationCode,
              locationName: item.terminal.locationName,
              locationId: item.terminal.locationId,
            },
          ];
          this.isToolingAssetSelected = false;
          this.isTerminalAssetSelected = true;
          this.workorderModalVisible = true;
        } else {
          console.log(3);
          this.assetSelected = [
            {
              id: item.id,
              title: item.mold.equipmentCode,
              checked: true,
              assetView: "tooling",
              locationCode: item.location
                ? item.location.locationCode
                : item.mold.locationCode,
              locationName: item.location
                ? item.location.locationTitle
                : item.mold.locationName,
              locationId: item.location
                ? item.location.id
                : item.mold.locationId,
            },
          ];
          this.isToolingAssetSelected = true;
          this.isTerminalAssetSelected = false;
          this.workorderModalVisible = true;
        }
      }
    },
    getOpConfig() {
      Common.getCurrentOpConfig().then((data) => {
        if (data) {
          const _inProductStatus = data.filter(
            (item) => item.operatingStatus === "WORKING"
          )[0];
          const _idleStatus = data.filter(
            (item) => item.operatingStatus === "IDLE"
          )[0];
          const _inActiveStatus = data.filter(
            (item) => item.operatingStatus === "NOT_WORKING"
          )[0];
          const _disconnectStatus = data.filter(
            (item) => item.operatingStatus === "DISCONNECTED"
          )[0];

          this.ruleOpStatus.inProduct =
            "There has been a shot made within the last " +
            _inProductStatus.time +
            this.getTimeUnit(_inProductStatus.timeUnit);
          this.ruleOpStatus.idle =
            "There has been a shot made after " +
            _inProductStatus.time +
            this.getTimeUnit(_inProductStatus.timeUnit) +
            " but within " +
            _idleStatus.time +
            this.getTimeUnit(_idleStatus.timeUnit);
          this.ruleOpStatus.inactive =
            "There is no shot made within " +
            _inActiveStatus.time +
            this.getTimeUnit(_inActiveStatus.timeUnit);
          this.ruleOpStatus.sensorOffline =
            "There is no signal from the sensor within " +
            _disconnectStatus.time +
            this.getTimeUnit(_disconnectStatus.timeUnit);
        }
      });
    },
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    backToMoldDetail() {
      console.log("back to molds detail");
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.backFromPreview();
      }
    },
    changeSelect(item) {
      console.log(item);
      this.requestParam.objectType = item;
    },
    handleChangeSearchText(text) {
      console.log("handleChangeSearchText", text);
      this.searchText = text;
    },
    getData() {
      console.log("getdata done");
      this.reloadKey++;
    },
    handleShowNoteSystem(result, isMultiple, objectType, systemNoteFunction) {
      const el = this.$refs["system-note"];
      console.log("handleShowNoteSystem", result, isMultiple, objectType, el);
      if (systemNoteFunction) {
        this.systemNoteFunction = systemNoteFunction;
      }
      if (el) {
        this.$nextTick(() => {
          el.showSystemNote(result, isMultiple, objectType);
        });
      }
    },
    getTimeUnit(timeUnit) {
      return timeUnit === "HOURS" ? " hour(s) " : " day(s) ";
    },
    captureQuery() {
      const [hash, stringParams] = location.hash.slice("#".length).split("?");
      this.activeCard = this.listCards.some((i) => i.key === hash)
        ? hash
        : "RELOCATION"; // default

      const replacedUrl = stringParams
        ? `${Common.PAGE_URL[this.activeCard]}?${stringParams}`
        : Common.PAGE_URL[this.activeCard];

      history.replaceState(null, null, replacedUrl);
    },
  },
  mounted() {
    // TODO(ducnm2010): check if it's work propertly with direct from mail
    this.captureQuery();
    window.addEventListener("popstate", this.captureQuery);
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>
<style src="/components/alert-based-pages/assets/style.css"></style>
