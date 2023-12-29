<template>
  <div>
  <div class="caption-zone">
    <b class="caption-title">{{ resources['overall_completion_rate'] }}
    </b>
  </div>
  <div class="data-request-overview-wrapper">
    <div style="text-align: center;margin-right: 20px;">

      <div class="data-request-chart">
        <canvas ref="chartDoughnut" width="202" height="202"></canvas>
        <div class="chart-title">
          <b>{{ _.head(chartData) + '%'}}</b>
        </div>
      </div>
    </div>
    <div class="card-section-wrapper">
      <base-card-tab
          v-for="card in cardList"
          size="medium"
          :active="false"
          :icon-src="card.iconSrc"
          :icon-style="card?.iconStyle"
          :key="card.id"
          :title="getCardTitle(card)"
          :content="getCardContent(card)"
          :style="{width: '100% !important', height: `100% !important`}"
          @click="$emit('open-data-completion-report', card.id)"
      ></base-card-tab>
    </div>
  </div>
  </div>
</template>

<script>
module.exports = {
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        chartData: {
            type: Array,
            default: () => ([])
        },
        renderKey: {
            type: [Number, String],
            default: () => (0)
        },
        cardData: {
          type: Object,
          default: () => ({})
        }
    },
    data() {
        return {
            chart: null,
            chartConfig: {
                type: 'doughnut',
                data: {
                    datasets: [{
                        data: [0, 0],
                        backgroundColor: [
                            '#4ebcd5',
                            '#d9d9d9'
                        ],
                        borderWidth: 0
                    }]
                },
                options: {
                    cutoutPercentage: 60,
                    responsive: false,
                    legend: {
                        display: false,
                    },
                    tooltips: {
                        enabled: false
                    },
                    title: {
                        display: false
                    },
                  plugins: {
                    labels: {
                      display: false,
                      enabled: false,
                      render: "label",
                      arc: false,
                      position: "outside",
                      fontSize: 13,
                      outsidePadding: 4,
                      textMargin: 4
                    }
                  },
                }
            },
            cardList: [
              {id: 'COMPANY', field: 'company', iconSrc: '/images/icon/icon-company-grey.svg', unit: 'Company'},
              {id: 'LOCATION', field: 'location', iconSrc: '/images/icon/icon-plants-grey.svg', unit: 'Plant'},
              {id: 'PART', field: 'part', iconSrc: '/images/icon/icon-part-grey.svg', unit: 'Part'},
              {id: 'TOOLING', field: 'mold', iconSrc: '/images/icon/icon-tooling-grey.svg', unit: 'Tooling'},
              {id: 'MACHINE', field: 'machine', iconSrc: '/images/icon/icon-machines-grey.svg', unit: 'Machine'},
            ]
        }
    },
    async created(){
    },
    mounted() {
        this.generateChart()
    },
    computed: {
        getChartConfig() {
            const currentConfig = { ...this.chartConfig }
            currentConfig.data.datasets[0].data = this.chartData
            return currentConfig
        }
    },
    watch: {
        renderKey() {
            this.generateChart();
        },
        cardList: {
          handler(newVal) {
            console.log('cardList', newVal)
          },
          immediate: true
        }
    },
    methods: {
        getCardTitle (card){
          const total =  this.cardData[card.field]?.numberItem || 0;
          let unit = card.unit
          if(total > 1){
            unit = card.unit === 'Company' ? ' Companies' : card.unit + 's'
          }
          return total + ' ' + unit;
        },
        getCardContent(card){
          const percentage = this.cardData[card.field]?.completedPercent || 0;
          return percentage + '% ' + this.resources['completed']
        },
        generateChart() {
            if (this.chart != null && this.chart.options) {
                this.chart.destroy();
            }
            const ctx = this.$refs[`chartDoughnut`]
            const dataConfig = { ...this.getChartConfig }
            this.chart = new Chart(ctx, dataConfig)
            this.updateChart()
        },
        updateChart() {
            this.chart.resize()
            this.chart.update()
        },

    },
}
</script>

<style scoped>
.data-request-chart {
    width: 269px;
    height: 269px;
    position: relative;
    margin: auto;
}
.data-request-chart canvas {
    width: 100%;
}
.data-request-overview-wrapper {
  display: grid;
  grid-template-columns: 269px 3fr;
  grid-gap: 24px;
}
.chart-title {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  font-size: 24px
}
.caption-title {
  font-size: 14.66px;
}
.card-section-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-gap: 24px;
}
.caption-zone {
  width: 269px;
  text-align: center;
}
</style>