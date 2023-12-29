<template>
  <div class="life-cycle-container">
    <!--  header  -->
    <div class="life-cycle-header">
      <master-filter-wrapper
        :filter-code="filterCode"
        :notify-filter-updated="fetchData"
      ></master-filter-wrapper>
    </div>

    <!--  Overall Utilization  -->
    <ovr-utl
      ref="over-utl-page"
      v-if="currentHash === 'ovr-utl'"
      :table-column-list="tableColumnList"
      :fetch-data="fetchData"
    >
    </ovr-utl>
  </div>
</template>

<script>
module.exports = {
  name: "life-cycle-page",
  components: {
    "ovr-utl": httpVueLoader(
      "/components/pages/analysis/life-cycle/ovr-utl.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      filterCode: "COMMON",
      currentHash: "",
      isModalOpened: false,
      tabButtons: [
        {
          title: "Overall Utilization",
          hash: "ovr-utl",
        },
      ],
      tableColumnList: [],
    };
  },
  methods: {
    getHash(itemUrl) {
      return itemUrl.split("#")[1];
    },
    changeTab(itemUrl) {
      this.currentHash = this.getHash(itemUrl);
    },
    getTableColumnList() {
      if (this.currentHash === "ovr-utl") {
        this.tableColumnList = [
          {
            id: 1,
            title: "Supplier",
            selected: true,
            fixed: true,
            default: true,
            key: "supplierName",
          },
          {
            id: 2,
            title: "Plant",
            selected: false,
            default: false,
            key: "plants",
          },
          {
            id: 3,
            title: "No. of Tooling",
            selected: true,
            default: true,
            key: "totalMoldCount",
          },
          {
            id: 4,
            title: "Low Utilization Toolings",
            selected: true,
            default: true,
            key: "lowMoldCount",
          },
          {
            id: 5,
            title: "Medium Utilization Toolings",
            selected: true,
            default: true,
            key: "mediumMoldCount",
          },
          {
            id: 6,
            title: "High Utilization Toolings",
            selected: true,
            default: true,
            key: "highMoldCount",
          },
          {
            id: 7,
            title: "Prolong Usage Toolings",
            selected: true,
            default: true,
            key: "prolongedMoldCount",
          },
        ];
      }
    },
    fetchData() {
      this.$refs["over-utl-page"].getUtilizationData("master-filter-update");
    },
  },
  watch: {
    currentHash: {
      handler(newVal) {
        window.location.hash = newVal;

        this.tabButtons = this.tabButtons.map((item) => {
          if (item.hash === this.currentHash) {
            return {
              ...item,
              active: true,
            };
          }
          return item;
        });

        this.getTableColumnList();
      },
      immediate: true,
    },
  },
  mounted() {
    const hashList = this.tabButtons.map((item) => item.hash);
    if (!window.location.hash) {
      window.location.hash = hashList[0];
    }

    this.tabButtons = this.tabButtons.map((item) => {
      if (item.hash === this.currentHash) {
        return {
          ...item,
          active: true,
        };
      }
      return item;
    });

    this.changeTab(window.location.href);
  },
};
</script>

<style scoped>
.life-cycle-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 32px 20px;
}

.life-cycle-header {
  width: 100%;
  height: 44px;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  flex-shrink: 0;
}
</style>
