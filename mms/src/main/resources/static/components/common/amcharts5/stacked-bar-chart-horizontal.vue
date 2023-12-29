<template>
    <div :style="styleProps" ref="stackedBarHorizontalChartDiv"></div>
</template>

<script>
module.exports = {
    props: {
        styleProps: {
            default: '',
            Type: String,
        },
        datasets: {
            default: () => {
                return {};
            },
            type: Object,
        },
        // datasets example
        // {
        //     categoryContent: [
        //         {
        //             category: 'Part-001',
        //             goodShots: 7,
        //             badShots: 1.5,
        //         },
        //         {
        //             category: 'Part-002',
        //             goodShots: 6,
        //             badShots: 2.7,
        //         },
        //         {
        //             category: 'Part-003',
        //             goodShots: 7,
        //             badShots: 2.9,
        //         },
        //     ],
        //     labelContent: [
        //         { displayName: 'Good Shots', key: 'goodShots', color: '#7B3AAD' },
        //         { displayName: 'Bad Shots', key: 'badShots', color: '#C25FD3' },
        //     ],
        // };
    },
    data() {
        return {
            root: '',
            stackedBarChart: '',
            xAxis: '',
            yAxis: '',
            legend: '',
        };
    },
    mounted() {
        console.log('stacked horizontal bar chart mounted !!!');

        this.$nextTick(() => {
            this.root = am5.Root.new(this.$refs.stackedBarHorizontalChartDiv);
            // 테마 설정
            const myTheme = am5.Theme.new(this.root);

            this.root.setThemes([am5themes_Animated.new(this.root), myTheme]);

            this.setChartConfig(); // 기본 차트 설정
            this.setXAxis();
            this.setYAxis();
            this.setLegend();

            this.setSeriesByLabel();

            this.chart.appear(1000, 100);
        });
    },
    methods: {
        setSeriesByLabel() {
            this.datasets.labelContent.map((item) => {
                this.setSeriesByCategory(item.key, item.displayName, item?.color);
            });
        },
        setSeriesByCategory(dataName, legendName, columnColor) {
            const self = this;
            let series = this.chart.series.push(
                am5xy.ColumnSeries.new(self.root, {
                    name: legendName,
                    stacked: true,
                    xAxis: self.xAxis,
                    yAxis: self.yAxis,
                    valueXField: dataName,
                    valueXShow: 'valueXTotalPercent',
                    categoryYField: 'category',
                    fill: am5.color(columnColor),
                })
            );
            series.columns.template.setAll({
                tooltipText: "{name}, {categoryY}: {valueXTotalPercent.formatNumber('#.#')}%",
                tooltipY: am5.percent(100),
                height: am5.percent(30),
            });
            series.data.setAll(this.datasets.categoryContent);
            series.appear();
            this.legend.data.push(series);
        },
        // 기본 차트 설정
        setChartConfig() {
            this.chart = this.root.container.children.push(
                am5xy.XYChart.new(this.root, {
                    panX: false,
                    panY: false,
                    layout: this.root.verticalLayout,
                })
            );
        },
        setXAxis() {
            this.xAxis = this.chart.xAxes.push(
                am5xy.ValueAxis.new(this.root, {
                    min: 0,
                    max: 100,
                    numberFormat: "#'%'",
                    strictMinMax: true,
                    calculateTotals: true,
                    renderer: am5xy.AxisRendererX.new(this.root, {}),
                })
            );
        },
        setYAxis() {
            this.yAxis = this.chart.yAxes.push(
                am5xy.CategoryAxis.new(this.root, {
                    categoryField: 'category',
                    renderer: am5xy.AxisRendererY.new(this.root, {}),
                    tooltip: am5.Tooltip.new(this.root, {}),
                })
            );
            this.yAxis.data.setAll(this.datasets.categoryContent);
        },
        setLegend() {
            this.legend = this.chart.children.push(
                am5.Legend.new(this.root, {
                    centerX: am5.p50,
                    x: am5.p50,
                })
            );
        },
    },
};
</script>
