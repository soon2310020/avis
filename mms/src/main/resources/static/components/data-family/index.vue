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
        @change="onChangeTab"
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
const DATA_FAMILY_TYPES = {
  COMPANY: "COMPANY",
  // COMPANY_NEW: "COMPANY_NEW",
  LOCATION: "LOCATION",
  // LOCATION_NEW: "LOCATION_NEW",
  CATEGORY: "CATEGORY",
  PART: "PART",
  // PART_NEW: "PART_NEW",
  TOOLING: "TOOLING",
  // TOOLING_OLD: "TOOLING_OLD",
  MACHINE: "MACHINE",
  // MACHINE_NEW: "MACHINE_NEW",
};

const DATA_FAMILY_COMPONENTS = {
  COMPANY: "data-family-company",
  // COMPANY_NEW: "data-family-company-new",
  LOCATION: "data-family-plant",
  // LOCATION_NEW: "data-family-plant-new",
  CATEGORY: "data-family-category",
  PART: "data-family-part",
  // PART_NEW: "data-family-part-new",
  TOOLING: "data-family-tooling",
  // TOOLING_OLD: "data-family-tooling-old",
  MACHINE: "data-family-machine",
  // MACHINE_NEW: "data-family-machine-new",
};
module.exports = {
  components: {
    "data-family-company": httpVueLoader(
      "/components/data-family/src/table/data-family-company-new.vue"
    ),
    // "data-family-company-new": httpVueLoader(
    //   "/components/data-family/src/table/data-family-company-new.vue"
    // ),
    "data-family-plant": httpVueLoader(
      "/components/data-family/src/table/data-family-plant-new.vue"
    ),
    // "data-family-plant-new": httpVueLoader(
    //   "/components/data-family/src/table/data-family-plant-new.vue"
    // ),
    "data-family-machine": httpVueLoader(
      "/components/data-family/src/table/data-family-machine-new.vue"
    ),
    // "data-family-machine-new": httpVueLoader(
    //   "/components/data-family/src/table/data-family-machine-new.vue"
    // ),
    "data-family-part": httpVueLoader(
      "/components/data-family/src/table/data-family-part-new.vue"
    ),
    // "data-family-part-new": httpVueLoader(
    //   "/components/data-family/src/table/data-family-part-new.vue"
    // ),
    "data-family-category": httpVueLoader(
      "/components/data-family/src/table/data-family-category.vue"
    ),
    "data-family-tooling": httpVueLoader(
      "/components/data-family/src/table/data-family-tooling.vue"
    ),
    // "data-family-tooling-old": httpVueLoader(
    //   "/components/data-family/src/table/data-family-tooling-old.vue"
    // ),
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
          key: DATA_FAMILY_TYPES.COMPANY,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_COMPANY,
          titleKey: "company",
          iconSrc: "/components/data-family/assets/company-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        },
        {
          key: DATA_FAMILY_TYPES.LOCATION,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_PLANT,
          titleKey: "plant",
          iconSrc: "/components/data-family/assets/plant-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        },
        // {
        //   key: DATA_FAMILY_TYPES.LOCATION_NEW,
        //   permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_PLANT,
        //   title: "Plant New",
        //   titleKey: "plantNew",
        //   iconSrc: "/components/data-family/assets/plant-icon.svg",
        //   iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        // },
        {
          key: DATA_FAMILY_TYPES.CATEGORY,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_CATEGORY,
          titleKey: "product_and_category",
          iconSrc: "/components/data-family/assets/product_category-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        },
        {
          key: DATA_FAMILY_TYPES.PART,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_PART,
          titleKey: "part",
          iconSrc: "/components/data-family/assets/part-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        },
        // {
        //   key: DATA_FAMILY_TYPES.PART_NEW,
        //   permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_PART,
        //   title: "Part New",
        //   titleKey: "partNew",
        //   iconSrc: "/components/data-family/assets/part-icon.svg",
        //   iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        // },
        {
          key: DATA_FAMILY_TYPES.TOOLING,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_TOOLING,
          titleKey: "tooling",
          iconSrc: "/components/data-family/assets/tooling-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 18px;",
        },
        // {
        //   key: DATA_FAMILY_TYPES.TOOLING_OLD,
        //   permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_TOOLING,
        //   titleKey: "tooling_old",
        //   iconSrc: "/components/data-family/assets/tooling-icon.svg",
        //   iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 18px;",
        // },
        {
          key: DATA_FAMILY_TYPES.MACHINE,
          permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_MACHINE,
          // title: "Machine",
          titleKey: "machine",
          iconSrc: "/components/data-family/assets/machine-icon.svg",
          iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        },
        // {
        //   key: DATA_FAMILY_TYPES.MACHINE_NEW,
        //   permissionCode: Common.PERMISSION_CODE.DATA_FAMILY_MACHINE,
        //   title: "Machine New",
        //   titleKey: "machine_new",
        //   iconSrc: "/components/data-family/assets/machine-icon.svg",
        //   iconStyle: "width: 32px; height: 32px; -webkit-mask-size: 27px;",
        // },
      ],
      activeCard: DATA_FAMILY_TYPES.PART_NEW,
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
      return DATA_FAMILY_COMPONENTS[this.activeCard];
    },
  },
  methods: {
    handleChangeSearchText(text) {
      this.searchText = text;
    },
    getData() {
      this.reloadKey++;
    },
    captureQuery() {
      console.log("🚀 ~ file: index.vue:143 ~ captureQuery ~ captureQuery:");
      const [hash, stringParams] = location.hash.slice("#".length).split("?");
      const params = Common.parseParams(stringParams);
      this.activeCard = this.listCards.some((i) => i.key === hash)
        ? hash // if selected is available
        : DATA_FAMILY_TYPES.TOOLING; // default is TOOLING
      console.log("stringParams", stringParams);
      const replacedUrl = stringParams
        ? // ? `/common/dat-fam#${this.activeCard}?${stringParams}`
          // : `/common/dat-fam#${this.activeCard}`
          `${Common.PAGE_URL[this.activeCard]}?${stringParams}`
        : Common.PAGE_URL[this.activeCard];
      history.replaceState(null, null, replacedUrl);

      if (params?.query) {
        this.searchText = params?.query;
      }
    },
    onChangeTab(tab) {
      console.log("🚀 ~ file: index.vue:162 ~ onChangeTab ~ tab:", tab);
      const targetUrl = Common.PAGE_URL[tab.key];
      window.location.href = targetUrl;
    },
  },
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (!newVal || !Object.keys(newVal).length) return;
        this.resources = Object.assign({}, this.resources, newVal);
        this.listCards = this.listCards.map((item) => ({
          ...item,
          title: this.resources[item.titleKey],
        }));
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.permissions,
      (newVal) => {
        if (newVal?.[Common?.PERMISSION_CODE?.DATA_FAMILY]) {
          this.permissions = Object.assign(
            {},
            this.permissions,
            newVal?.[Common?.PERMISSION_CODE?.DATA_FAMILY]?.children
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
    this.captureQuery();
    window.addEventListener("popstate", this.captureQuery);
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>

<style src="/components/data-family/assets/style.css"></style>
<style src="/css/data-table.css"></style>
