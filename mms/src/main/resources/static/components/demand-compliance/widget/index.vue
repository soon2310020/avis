<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #body>
      <div>
        <emdn-xy-chart
          category="supplierCode"
          :data="results"
          :bar-data-binder="barDataBinder"
          :bar-set="barSet"
          :legend-set="legendSet"
          :chart-set="chartSet"
          :style-props="{ height: '200px', width: '90%' }"
        ></emdn-xy-chart>
      </div>
    </template>
    <template #footer>
      <base-paging
        :total-page="pagination.totalPages"
        :current-page="pagination.page"
        @next="handleNextPage"
        @back="handleBackPage"
        :style="{
          height: '46px',
          justifyContent: 'flex-end',
          marginRight: '8px',
        }"
      >
      </base-paging>
    </template>
  </emdn-widget>
</template>

<script>
module.exports = {
  name: "DemandComplianceTrackerWidget",
  props: {
    key: String,
    label: String,
  },
  data() {
    return {
      results: [],
      pagination: {
        page: 1,
        size: 5,
        totalPages: 0,
      },
      barSet: {
        isStacked: true,
        isClustered: false,
        strokeWidth: 0,
        fillOpacity: 1,
      },
      barDataBinder: [
        {
          key: "numHighCompliance",
          displayName: "High Compliance",
          color: "#41CE77",
        },
        {
          key: "numMediumCompliance",
          displayName: "Medium Compliance",
          color: "#F7CC57",
        },
        {
          key: "numLowCompliance",
          displayName: "Low Compliance",
          color: "#E34537",
        },
      ],
      legendSet: {
        isVisible: true,
        maxColumns: 3,
        justifyContent: "center",
      },
      chartSet: {
        xAxis: {
          type: "ValueAxis",
          renderer: {
            minGridDistance: 30,
          },
        },
        yAxis: {
          type: "CategoryAxis",
          renderer: {
            minGridDistance: 5,
          },
        },
        valueAxis: {
          isPercent: true,
        },
      },
    };
  },
  methods: {
    async fetchData(query) {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        ...query,
      };
      try {
        const response = await axios.get("/api/common/dsh/dem-cpl-trk", {
          params,
        });
        this.results = response.data.content;
        this.pagination.totalPages = response.data.totalPages;
        console.log(response);
      } catch (error) {
        console.log(error);
      }
    },
    handleNextPage() {
      if (this.pagination.page < this.pagination.totalPages) {
        this.pagination.page++;
        console.log("this.pagination.page", this.pagination.page);
      }
    },
    handleBackPage() {
      if (this.pagination.page > 1) {
        this.pagination.page--;
        console.log("this.pagination.page", this.pagination.page);
      }
    },
  },
  watch: {
    "pagination.page"(newVal) {
      this.fetchData({ page: newVal });
    },
  },
  created() {
    this.fetchData();
  },
};
</script>
