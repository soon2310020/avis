<template>
    <div :style="styleProps" ref="pieChartDiv"></div>
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
                            category: 'Part-001',
                            processingTime: 1,
                            warmupTime: 2,
                            cooldownTime: 3,
                        },
                    ],
                    labelContent: [
                        { displayName: 'Processing Time', key: 'processingTime', color: '#7B3AAD' },
                        { displayName: 'Warmup Time', key: 'warmupTime', color: '#C25FD3' },
                        { displayName: 'Cool Down Time', key: 'cooldownTime', color: '#C0ADF2' },
                    ],
                };
            },
            type: Object,
        },
        colorList: {
            default: () => ['#7B3AAD', '#C25FD3', '#C0ADF2'],
            type: Array,
        },
    },
    data() {
        return {
            root: '',
            legend: '',
            chart: '',
            series: '',
            tooltip: '',
            categoryName: '',
            pieChartData: [],
            // datasets: {
            //     categoryContent: [
            //         {
            //             category: 'Part-001',
            //             processingTime: 1,
            //             warmupTime: 2,
            //             cooldownTime: 3,
            //         },
            //     ],
            //     labelContent: [
            //         { displayName: 'Processing Time', key: 'processingTime', color: '#7B3AAD' },
            //         { displayName: 'Warmup Time', key: 'warmupTime', color: '#C25FD3' },
            //         { displayName: 'Cool Down Time', key: 'cooldownTime', color: '#C0ADF2' },
            //     ],
            // },
        };
    },

    mounted() {
        console.log('pie chart mounted !!!');
        this.root = am5.Root.new(this.$refs.pieChartDiv);
        this.root.setThemes([am5themes_Animated.new(this.root)]);

        this.setPieChartData();
    },
    methods: {
        // 시리즈 설정
        setSeries() {
            this.series = this.chart.series.push(
                am5percent.PieSeries.new(this.root, {
                    valueField: 'value',
                    categoryField: 'name',
                    legendValueText: '',
                    alignLabels: false,
                })
            );

            let seriesColorList = [];

            this.datasets.labelContent?.map((item) => {
                seriesColorList.push(am5.color(item.color));
            });

            this.series.get('colors').set('colors', seriesColorList);

            console.log('this.categoryName: ', this.categoryName);

            this.series.slices.template.setAll({
                stroke: am5.color(0xffffff),
                strokeWidth: 2,
                tooltip: this.tooltip,
                tooltipText: `{name}, ${this.categoryName}: {valuePercentTotal.formatNumber('#.#')}%`,
            });

            this.series.labels.template.setAll({
                fontSize: this.fontSize,
                text: "{valuePercentTotal.formatNumber('#.#')}%",
                inside: true,
                fill: am5.color('#ffffff'),
                radius: 30,
                centerX: am5.percent(50),
            });

            // 데이터 파이 차트에 맞게 변경 시키기
            this.series.data.setAll(this.pieChartData);
        },
        setPieChartData() {
            const self = this;
            let categoryItem = this.datasets.categoryContent[0];

            let categoryArray = [];
            this.categoryName = '';

            Object.keys(categoryItem).forEach(function (key) {
                if (key !== 'category') {
                    categoryArray.push({ name: key, value: categoryItem[key] });
                } else {
                    self.categoryName = categoryItem[key];
                }
            });

            this.datasets.labelContent.map((labelItem) => {
                Object.keys(labelItem).forEach(function (key) {
                    categoryArray.map((categoryArrayItem) => {
                        if (labelItem[key] === categoryArrayItem.name) {
                            self.pieChartData.push({
                                category: this.categoryName,
                                name: labelItem.displayName,
                                value: categoryArrayItem.value,
                                color: labelItem.color,
                            });
                        }
                    });
                });
            });

            this.setTooltip();
            this.setChartConfig();
            this.setSeries();
            this.setLegend(this.series);

            this.series.appear(1000, 100);
        },
        // 기본 차트 설정
        setChartConfig() {
            this.chart = this.root.container.children.push(
                am5percent.PieChart.new(this.root, {
                    layout: this.root.verticalLayout,
                })
            );
        },
        setTooltip() {
            this.tooltip = am5.Tooltip.new(this.root, {});
            // this.tooltip.label.setAll({ fontSize: this.fontSize, labelText: '{category} 123123' });
        },

        setLegend() {
            this.legend = this.chart.children.push(
                am5.Legend.new(this.root, {
                    centerX: am5.percent(50),
                    x: am5.percent(50),
                    marginTop: 15,
                    marginBottom: 15,
                    text: '{name}',
                })
            );
            this.legend.labels.template.setAll({
                fontSize: this.fontSize,
                width: 50,
            });
            this.legend.data.setAll(this.series.dataItems);
        },
    },
};
</script>
