<template>
    <div
        class="chart-new-container"
        :class="getChartClass"
    >
        <div style="position: relative;">
            <div v-if="title" class="graph-title">{{title}}</div>
            <canvas
                :id="`chart-line-${id}`"
                :ref="`chart-line-${id}`"
                class="chart-line"
                :width="width"
                :height="height"
                role="chart"
            ></canvas>
        </div>
        <slot
            v-if="showCustomLegend"
            name="custom-legend"
        ></slot>
    </div>
</template>

<script>
module.exports = {
    name: 'line-chart',
    props: {
        id: {
            type: String,
            default: () => ('')
        },
        title: {
            type: String,
            default: ''
        },
        labels: {
            type: Array,
            default: () => ([])
        },
        listDataset: {
            type: Array,
            default: () => ([])
        },
        legendLabel: {
            type: String,
            default: () => ('')
        },
        width: [String, Number],
        height: [String, Number],
        legendConfig: {
            type: Object,
            default: () => ({})
        },
        customLegendPosition: {
            type: String,
            default: () => ('')
        },
        customOptions: {
            type: Object,
            default: () => ({})
        },
        showCustomLegend: {
            type: Boolean,
            default: true
        },
        changeState: {
            type: Number,
            default: 0
        }
    },
    data() {
        return {
            chart: null,
            config: {
                type: 'line',
                data: {
                    datasets: []
                },
                options: {
                    plugins: {

                    },
                },
            },
            chartOptions: {
            }
        }
    },
    computed: {
        getChartDataConfig() {
            let config = { ...this.config }
            config.data.labels = this.labels
            config.data.datasets = this.listDataset
            config.options.legend = this.legendConfig
            config.options.onClick = this.handleClick
            config.options = { ...config.options, ...this.customOptions }
            return config
        },
        getChartClass() {
            let resultClass = ''
            switch (this.customLegendPosition) {
                case 'left':
                    resultClass = 'chart-row row-reverse'
                    break;
                case 'top':
                    resultClass = 'chart-row column-reverse'
                    break;
                case 'right':
                    resultClass = 'chart-row'
                    break;
                case 'bottom':
                    resultClass = 'chart-row column'
                    break;
                default:
                    resultClass = ''
                    break;
            }
            console.log('class', this.customLegendPosition, resultClass)
            return resultClass
        }
    },
    watch: {
        changeState() {
            this.generateChart()
        }
    },
    methods: {
        generateChart() {
            if (this.chart != null && this.chart.options) {
                this.chart.destroy();
            }
            const ctx = this.$refs[`chart-line-${this.id}`]
            console.log('generateChart', this.width, this.height, ctx,)
            const dataConfig = { ...this.getChartDataConfig }
            console.log('dataConfig', dataConfig)
            this.chart = new Chart(ctx, dataConfig)
            this.updateChart()
        },
        updateChart() {
            console.log('@update', this.chart)
            this.chart.resize()
            this.chart.update()
        },
        handleClick(e) {
            const activePoints = this.chart.getElementsAtEventForMode(e, 'nearest', { intersect: true }, true);
            if (activePoints.length > 0) {
                const selectedIndex = activePoints[0]._index;
                this.$emit('chart-click', selectedIndex)
            }
        }
    },
    mounted() {
        console.log('run mounted chart')
        this.generateChart()
    },
}
</script>
<style scoped>
.chart-new-container {
    display: flex;
    align-items: center;
}
.chart-row {
    display: flex;
    align-items: center;
}
.row-reverse {
    flex-direction: row-reverse;
}
.column-reverse {
    flex-direction: column-reverse;
}
.column {
    flex-direction: column;
}
.graph-title {
  height: fit-content;
  transform: rotate(-90deg) translate(50% , -80px);
  margin-top: 0;
  margin-right: 0;
  position: absolute;
  top: 50%;
  left: 0;
}
</style>