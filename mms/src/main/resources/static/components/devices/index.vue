<template>
  <div>
    <div class="d-flex justify-space-between">
      <div>
        <master-filter-wrapper
          :filter-code="param.filterCode"
          :notify-filter-updated="getData"
        ></master-filter-wrapper>
      </div>
      <div class="col-md-3 mb-3 mb-md-0 col-12">
        <base-filter-search-bar
          :resources="resources"
          :placeholder="resources['search_by_selected_column']"
          :value="searchText"
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
      :is="componentName"
      :resources="resources"
      :search-text="searchText"
      :reload-key="reloadKey"
      :filter-code="param.filterCode"
    ></component>
  </div>
</template>

<script>
const DEVICE_TYPES = {
  TERMINAL: "TERMINAL",
  SENSOR: "SENSOR",
  // SENSOR_NEW: "SENSOR_NEW",
};

const DEVICE_COMPONENTS = {
  TERMINAL: "devices-terminal",
  SENSOR: "devices-sensor",
  // SENSOR_NEW: "devices-sensor-new",
};
module.exports = {
  components: {
    "devices-terminal": httpVueLoader(
      "/components/devices/src/devices-table/devices-terminal-new.vue"
    ),
    // "devices-sensor-new": httpVueLoader(
    //   "/components/devices/src/devices-table/devices-sensor-new.vue"
    // ),
    "devices-sensor": httpVueLoader(
      "/components/devices/src/devices-table/devices-sensor-new.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      resources: {},
      permissions: {},
      listCards: [
        {
          key: DEVICE_TYPES.TERMINAL,
          permissionCode: Common.PERMISSION_CODE.DEVICES_TERMINAL,
          title: "Terminal",
          titleKey: "terminal",
          iconSrc: "/components/devices/assets/terminal-icon.svg",
        },
        {
          key: DEVICE_TYPES.SENSOR,
          permissionCode: Common.PERMISSION_CODE.DEVICES_SENSOR,
          title: "Sensor",
          titleKey: "counter",
          iconSrc: "/components/devices/assets/sensor-icon.svg",
        },
        // {
        //   key: DEVICE_TYPES.SENSOR_NEW,
        //   permissionCode: Common.PERMISSION_CODE.DEVICES_SENSOR,
        //   title: "Sensor New",
        //   titleKey: "counterNew",
        //   iconSrc: "/components/devices/assets/sensor-icon.svg",
        // },
      ],
      activeCard: DEVICE_TYPES.TERMINAL,
      searchText: "",
      param: {
        filterCode: "COMMON",
        query: "",
        sort: "",
        page: 1,
        size: 10,
      },
      reloadKey: 0,
    };
  },
  computed: {
    componentName() {
      return DEVICE_COMPONENTS[this.activeCard];
    },
  },
  watch: {},
  methods: {
    handleChangeSearchText(text) {
      console.log("handleChangeSearchText", text);
      this.searchText = text;
    },
    handleChangeTab(tab) {
      this.activeCard = tab.key;
    },
    getData() {
      console.log("getdata done");
      this.reloadKey++;
    },
  },
  created() {
    console.log("device root vue");
    this.$watch(
      () => headerVm?.permissions,
      (newVal) => {
        if (newVal && newVal?.[Common?.PERMISSION_CODE?.DEVICES]) {
          this.permissions = Object.assign(
            {},
            this.permissions,
            newVal?.[Common?.PERMISSION_CODE?.DEVICES]?.children
          );
          console.log("permissions", this.permissions);
          const listCodes = Object.keys(this.permissions);
          this.listCards = this.listCards.filter((item) =>
            listCodes.some((code) => item.permissionCode === code)
          );
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal).length) {
          this.resources = Object.assign({}, this.resources, newVal);
          if (this.resources) {
            this.listCards = this.listCards.map((item) => ({
              ...item,
              title: this.resources[item.titleKey] || item.title,
            }));
          }
          console.log("created", this.resources);
        }
      },
      { immediate: true }
    );

    window.addEventListener("popstate", () => {
      console.log("locationchange");
      const url = new URL(location.href);
      const [hash, stringParams] = url.hash.replace("#", "").split("?");
      const params = Common.parseParams(stringParams);
      if (params?.query) {
        this.searchText = params.query;
      }
      if (hash) {
        this.handleChangeTab({ key: hash });
      }
    });
  },
  mounted() {
    const url = new URL(location.href);
    const [hash, stringParams] = url.hash.replace("#", "").split("?");
    const params = Common.parseParams(stringParams);
    if (params?.query) {
      this.searchText = params.query;
    }
    if (hash) {
      this.handleChangeTab({ key: hash ? hash : DEVICE_TYPES.TERMINAL });
    }
  },
};
</script>

<style src="/components/devices/assets/style.css"></style>
<style src="/css/data-table.css"></style>
