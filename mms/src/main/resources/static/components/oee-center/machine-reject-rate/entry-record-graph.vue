<template>
  <div class="dashboard-card">
    <div class="dashboard-card__content">
      <div class="mb-3">
        Reject Rate Reasons
      </div>
      <apex-bar-chart
        v-if="isShow"
        ref="apexBarChartRef"
        :options="options"
      ></apex-bar-chart>
      
      <div style="color: rgb(75, 75, 75); font-weight: 400; font-size: 13px; text-align: center;">Number of Parts</div>
    </div>

    <div
        class="pagination"
        v-if="totalPages !== 0"
    >
            <span class="pagination-page">
                {{ pageNumber }} of {{ totalPages }}
            </span>
      <span class="pagination-bar">
                <a
                    class="pagination-button arrow-left"
                    :class="{ inactive: pageNumber <= 1 }"
                    href="javascript:;"
                    @click="handleChangePage(pageNumber - 1)"
                >
                    <img
                        src="/images/icon/category/paging-arrow.svg"
                        alt=""
                    />
                </a>
                <a
                    class="pagination-button arrow-right"
                    :class="{ inactive: pageNumber >= totalPages }"
                    href="javascript:;"
                    @click="handleChangePage(pageNumber + 1)"
                >
                    <img
                        src="/images/icon/category/paging-arrow.svg"
                        style="transform: rotate(180deg)"
                        alt=""
                    />
                </a>
            </span>
    </div>
  </div>
</template>

<script>
const COLORS = [__greenGraphLight, __blueGraphMedium, __blueGraphDark]
module.exports = {
  components: {
    'apex-bar-chart': httpVueLoader('/components/@base/chart/apex-bar-chart.vue'),
  },
  props: {
    listActiveTabs: {
      type: Array,
      default: () => ([])
    },
    filter: {
      type: Object,
      default: () => { }
    },
    isShow: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      options: {
        series: [
          {
            data: []
          }
        ],
        chart: {
          type: 'bar',
          width: 620,
          toolbar: {
            show: false
          }
        },
        colors: [],
        plotOptions: {
          bar: {
            borderRadius: 0,
            horizontal: true,
            distributed: true,
          }
        },
        dataLabels: {
          enabled: false
        },
        xaxis: {
          categories: [
          ],
          labels: {
            formatter: function (val) {
              return val // return val.toFixed();
            }
          },
          tickAmount: 1,
        },
        legend: {
          show: false
        },
        tooltip: {
          enabled: false
        },
        states: {
          hover: {
            filter: {
              type: 'none',
              value: 1
            }
          },
        }
      },
      listParts: [],
      pageNumber: 0,
      totalPages: 0,
      pageSize: 8,
      mark1: 0,
      mark2: 0
    }
  },
  watch: {
    filter: {
      handler(newVal) {
        this.getParts({ page: 1 })
      },
      deep: true
    }
  },
  methods: {
    async fetchParts(query) {
      return await axios.get('/api/rejected-part/get-reject-part-summary?' + Common.param({
        ...query, size: this.pageSize
      }))
    },
    async getParts(query) {
      try {
        const response = await this.fetchParts({ ...this.filter, ...query })
        console.log('response', response)
        this.listParts = response.data.data.content.map((item, index) => ({ ...item, index }))
        const __listCount = response.data.data.content.map(item => item.rejectedAmount);
        const __listProd = response.data.data.content.map(item => item.reason)
        // const level1 = response.data.data.level1
        // const level2 = response.data.data.level2
        this.pageNumber = query?.page || this.pageNumber
        this.totalPages = response.data.data.totalPages
        this.renderData(__listCount)
        this.renderLabel(__listProd)
        // this.renderLegend(level1, level2)
        this.generateColor()
        this.updateChart()
        console.log()
      } catch (error) {
        console.log(error)
      }
    },
    handleChartClick(datasetIndex, index) {
      console.log(datasetIndex, index)
    },
    generateColor() {
      let color = []
      for (let index = 0; index < this.pageSize; index++) {
        const value = this.options.series[0].data[index];
        if (0 <= value && value < this.mark1) color.push(COLORS[0])
        if (this.mark1 <= value && value < this.mark2) color.push(COLORS[1])
        if (value >= this.mark2) color.push(COLORS[2])
      }
      this.options.colors = [...color]
    },
    renderData(arr) {
      this.options.series[0].data = arr
    },
    renderLabel(arr) {
      this.options.xaxis.categories = arr
    },
    getDataFromLabel() {

    },
    renderLegend(level1, level2) {
      this.mark1 = level1
      this.mark2 = level2
      this.options.legend.customLegendItems = [`0 - ${level1}`, `${level1} - ${level2}`, `> ${level2}`]
    },
    updateChart() {
      console.log('options', this.options)
      if (this.$refs.apexBarChartRef) this.$refs.apexBarChartRef.generateChart()
    },
    handleChangePage(page) {
      if (page > this.totalPages || page < 0) return
      this.getParts({ page })
    }
  },
  mounted() {
    this.getParts({ page: 1 })
  },
}
</script>

<style scoped>
.dashboard-card__content {
  border: 1px solid #C4C4C4;
  border-radius: 2px;
  padding: 16px 12px;
  font-weight: 700;
  color: #4B4B4B;
  font-size: 14px;
  margin-bottom: 20px;
}
.chart-new-container {
  justify-content: center;
  height: 450px;
}
.pagination {
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 35px;
}

.pagination-button.inactive {
  background-color: rgb(196, 196, 196);
  pointer-events: none;
  padding: 6px 8px;
  border-radius: 3px;
  margin-left: 8px;
  display: inline-flex;
}

.pagination-button {
  background-color: rgb(52, 145, 255);
  padding: 6px 8px;
  border-radius: 3px;
  margin-left: 8px;
  display: inline-flex;
}
</style>