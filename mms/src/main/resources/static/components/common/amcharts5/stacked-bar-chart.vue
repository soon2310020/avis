<template>
    <div :style="styleProps" ref="stackedBarChartDiv"></div>
</template>

<script>
module.exports = {
    props: {
        styleProps: {
            default: 'width: 500px; height: 400px;',
            type: String,
        },
        fontSize: {
            default: '1vw',
            type: String,
        },
        datasets: {
            default: () => {
                return {
                    categoryContent: [
                        {
                            category: '',
                            goodShots: 11,
                            badShots: 3.5,
                        },
                    ],
                    labelContent: [
                        { displayName: 'Good Shots', key: 'goodShots', color: '#4EBCD5' },
                        { displayName: 'Bad Shots', key: 'badShots', color: '#BCE2C7' },
                    ],
                };
            },
            type: Object,
        },
    },
    data() {
        return {
            root: '',
            stackedBarChart: '',
            xAxis: '',
            yAxis: '',
            legend: '',
            tooltip: '',
        };
    },
    mounted() {
        console.log('stacked bar chart mounted !!!');

        this.$nextTick(() => {
            this.root = am5.Root.new(this.$refs.stackedBarChartDiv);

            // 테마 설정
            this.root.setThemes([am5themes_Animated.new(this.root)]);

            this.setChartConfig(); // 기본 차트 설정
            this.setXAxis(); // x축 셋팅
            this.setYAxis(); // y축 셋팅
            this.setLegend(); // 범례 셋팅
            this.setSeriesByLabel(); // 시리즈 셋팅

            this.chart.appear(1000, 100);
        });
    },
    methods: {
        setSeriesByLabel() {
            this.datasets.labelContent.map((item) => {
                this.setSeriesByCategory(item.key, item.displayName, item?.color);
            });
        },
        setSeriesByCategory(dataName, displayName, columnColor) {
            const self = this;

            let series = this.chart.series.push(
                am5xy.ColumnSeries.new(self.root, {
                    name: displayName,
                    xAxis: self.xAxis,
                    yAxis: self.yAxis,
                    valueYField: dataName,
                    valueYShow: 'valueYTotalPercent',
                    categoryXField: 'category',
                    stacked: true,
                    maskBullets: true,
                })
            );

            if (columnColor) {
                series.set('fill', am5.color(columnColor));
                series.set('stroke', am5.color(columnColor));
            }

            this.setTooltip();
            // this.setSeriesColumn(series, displayName);
            this.setSeriesColumn(series);
            this.setSeriesBullet(series);

            this.legend.data.push(series);

            series.data.setAll(this.datasets.categoryContent);
            series.appear();
        },
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
                am5xy.CategoryAxis.new(this.root, {
                    categoryField: 'category',
                    renderer: am5xy.AxisRendererX.new(this.root, {}),
                })
            );
            this.xAxis.data.setAll(this.datasets.categoryContent);
            this.xAxis.get('renderer').labels.template.setAll({
                fontSize: this.fontSize,
                oversizedBehavior: 'truncate',
                maxWidth: 100,
            });
        },
        setYAxis() {
            this.yAxis = this.chart.yAxes.push(
                am5xy.ValueAxis.new(this.root, {
                    min: 0,
                    max: 100,
                    calculateTotals: true, // 합계 계산: 백분율을 위해서 필요한 듯.
                    numberFormat: "#'%'",
                    // strictMinMax: true,
                    renderer: am5xy.AxisRendererY.new(this.root, {}),
                })
            );
            this.yAxis.get('renderer').labels.template.setAll({ fontSize: this.fontSize });
        },
        setLegend() {
            this.legend = this.chart.children.push(
                am5.Legend.new(this.root, {
                    centerX: am5.percent(50),
                    x: am5.percent(50),
                })
            );
            this.legend.labels.template.setAll({
                fontSize: this.fontSize,
                oversizedBehavior: 'wrap',
                // oversizedBehavior: 'truncate',
                width: 100,
                maxWidth: 100,
            });
        },

        setTooltip() {
            this.tooltip = am5.Tooltip.new(this.root, {});
            this.tooltip.label.setAll({ fontSize: this.fontSize });
        },
        setSeriesColumn(series, displayName) {
            series.columns.template.setAll({
                // tooltipText: "{category}, {categoryX}: {valueYTotalPercent.formatNumber('#.#')}%",
                // tooltipText: `${displayName}, {categoryX}: {valueYTotalPercent.formatNumber('#.#')}%`,
                tooltipText: "{name}, {categoryX}: {valueYTotalPercent.formatNumber('#.#')}%",
                tooltipY: am5.percent(10),
                width: am5.percent(30),
                tooltip: this.tooltip,
            });
        },
        setSeriesBullet(series) {
            const self = this;
            series.bullets.push(function () {
                return am5.Bullet.new(self.root, {
                    sprite: am5.Label.new(self.root, {
                        text: "{valueYTotalPercent.formatNumber('#.#')}%",
                        fill: self.root.interfaceColors.get('alternativeText'),
                        centerY: am5.p50,
                        centerX: am5.p50,
                        populateText: true,
                        fontSize: self.fontSize,
                    }),
                });
            });
        },
    },
};
</script>
