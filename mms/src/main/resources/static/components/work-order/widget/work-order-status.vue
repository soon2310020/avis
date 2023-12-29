<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #body>
      <emdn-xy-chart
        :data="results"
        :category="category"
        :bar-data-binder="barDataBinder"
        :chart-set="chartSet"
        :legend-set="legendSet"
        :style-props="{ height: '200px', width: '90%' }"
      ></emdn-xy-chart>
    </template>
    <template #footer>
      <base-paging
        :total-page="pagination.totalPages"
        :current-page="pagination.page"
        @next="() => handlePaginate('NEXT')"
        @back="() => handlePaginate('BACK')"
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
  name: "WorkOrderStatusWidget",
  props: {
    label: String,
    key: String,
  },
  data() {
    return {
      results: [],
      pagination: {
        page: 1,
        size: 5,
        totalPages: 0,
      },
      category: "supplierCode",
      barDataBinder: [
        {
          key: "overdueRate",
          displayName: "Overdue Rate",
          color: "#E34537",
        },
      ],
      barSet: {
        isStacked: false,
        isClustered: false,
        strokeWidth: 0,
        fillOpacity: 1,
      },
      legendSet: {
        isVisible: true,
        // paddingLeft: "20px",
        root: {
          container: {
            layout: "horizontalLayout",
          },
        },
      },
      chartSet: {
        xAxis: {
          type: "CategoryAxis",
        },
        yAxis: {
          type: "ValueAxis",
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
          "/api/production/work-order/work-order-status",
          { params }
        );
        this.results = response.data.content;
        this.pagination.totalPages = response.data.totalPages;
      } catch (error) {
        console.log(error);
      }
    },
    handlePaginate(type) {
      if (type === "NEXT") {
        this.pagination.page++;
      }
      if (type === "PREV") {
        this.pagination.page--;
      }
    },
  },
  watch: {
    "pagination.page"(newVal) {
      this.fetch({ page: newVal });
    },
  },
  created() {
    this.fetch();
  },
};
</script>
