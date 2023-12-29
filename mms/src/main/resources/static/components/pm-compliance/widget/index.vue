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
          height: '47px',
          justifyContent: 'flex-end',
          marginRight: '8px',
        }"
      ></base-paging>
    </template>
  </emdn-widget>
</template>

<script>
module.exports = {
  name: "PmComplianceWidget",
  props: {
    label: String,
    key: String,
  },
  components: {},
  data() {
    return {
      results: [],
      pagination: {
        page: 0,
        size: 5,
        totalPages: 0,
      },
      barSet: {
        isStacked: false,
        isClustered: false,
        strokeWidth: 0,
        fillOpacity: 1,
      },
      barDataBinder: [
        {
          key: "complianceRateHigh",
          displayName: "High",
          color: "#41CE77",
        },
        {
          key: "complianceRateMedium",
          displayName: "Medium",
          color: "#F7CC57",
        },
        {
          key: "complianceRateLow",
          displayName: "Low",
          color: "#E34537",
        },
      ],
      legendSet: {
        isVisible: true,
        maxColumns: 3,
        paddingLeft: "20px",
      },
      chartSet: {
        xAxis: {
          type: "ValueAxis",
        },
        yAxis: {
          type: "CategoryAxis",
        },
        valueAxis: {
          isPercent: true,
        },
      },
    };
  },
  methods: {
    async fetch(query) {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        ...query,
      };
      try {
        const response = await axios.get(
          "/api/production/work-order/pm-compliance",
          { params }
        );

        this.results = response.data.content.map((i) => {
          const newObj = {
            ...i,
            complianceRateLow: 0,
            complianceRateMedium: 0,
            complianceRateHigh: 0,
          };
          if (i.complianceRateLevel === "LOW") {
            newObj.complianceRateLow = i.complianceRate;
          }
          if (i.complianceRateLevel === "MEDIUM") {
            newObj.complianceRateMedium = i.complianceRate;
          }
          if (i.complianceRateLevel === "HIGH") {
            newObj.complianceRateHigh = i.complianceRate;
          }
          return newObj;
        });

        this.pagination.page = response.data.pageable.pageNumber + 1;
        this.pagination.totalPages = response.data.totalPages;
      } catch (error) {
        console.log(error);
      }
    },
    xAxisLabelTextAdapter(text, target) {
      return text;
    },
    handleNextPage() {
      if (this.pagination.page < this.pagination.totalPages)
        this.pagination.page++;
    },
    handleBackPage() {
      if (this.pagination.page > 1) this.pagination.page--;
    },
    handleChangePage(type) {
      if (type === "NEXT") this.pagination.page++;
      if (type === "PREV") this.pagination.page--;
    },
  },
  watch: {
    pagination: {
      handler(newVal, oldVal) {
        if (newVal.page !== oldVal.page || newVal.size !== oldVal.size) {
          this.fetch({ page: newVal.page, size: newVal.size });
        }
      },
      deep: true,
    },
  },
  created() {
    this.fetch();
  },
};
</script>
