<!-- 
이 컴포넌트는 공통 컴포넌트 이므로, 수정했을 경우, 이 컴포넌트를 사용한 모든 곳에 영향을 끼칠 수 있습니다.
그렇기 때문에 이 컴포넌트의 수정은 금지하며, 수정이 필요할 경우 woojin에게 요청해주시기 바랍니다. 

This component is a common component, so if you modify it, it can affect everywhere you use this component.
Therefore, modification of this component is prohibited, and if you need to modify it, please ask woojin.

Design reference link:
https://www.figma.com/file/UftykJ37WoqcvLUgmEOWYX/eMoldino-Design-System?node-id=0%3A1

Component docs link: 
https://docs.google.com/presentation/d/1XaU9Orvhj8qIFkbvlfqtnArz6QWV6m2t_k4W6cvzwPc/edit#slide=id.g131e2b4a0aa_0_17


- emoldino woojin
-->

<template>
    <div :style="styleProps" ref="pieChartDiv"></div>
</template>

<script>
module.exports = {
    name: 'PieChart',
    props: {
        // container
        styleProps: {
            type: String,
            default: 'width: 100%; height: 400px',
        },
        chartLayout: {
            type: String,
            default: 'verticalLayout',
        },
        // pie chart

        pieColorList: {
            type: Array,
            default: ['#095256', '#087f8c', '#5aaa95', '#86a873', '#bb9f06'],
        },
        pieRadius: {
            type: [String, Number],
            default: 100,
        },
        pieInnerRadius: {
            type: [String, Number],
            default: 0,
        },
        pieStartAngle: {
            type: [String, Number],
            default: -90,
        },
        pieEndAngle: {
            type: [String, Number],
            default: 270,
        },

        // pie slice
        isPieSlicesToggleKey: {
            type: Boolean,
            default: true,
        },
        pieSliceBorderColor: {
            type: String,
            default: '#000000',
        },
        pieSliceBorderWidth: {
            type: [String, Number],
            default: 2,
        },
        pieSliceShiftRadius: {
            type: [String, Number],
            default: 0,
        },
        pieSliceFillOpacity: {
            type: [String, Number],
            default: 0.5,
        },
        pieSliceTooltipText: {
            type: String,
            default: "{category}: [bold]{valuePercentTotal.formatNumber('0.00')}%[/] ({value})",
        },

        // pie label
        isPieLabelAlign: {
            type: Boolean,
            default: true,
        },
        isPieLabelInside: {
            type: Boolean,
            default: true,
        },
        isPieLabelTextVisible: {
            type: Boolean,
            default: true,
        },
        pieLabelFill: {
            type: String,
            default: '#550000',
        },
        pieLabelText: {
            type: String,
            default: '{category}',
        },
        pieLabelTextType: {
            type: String,
            default: 'circular',
        },
        pieLabelRadius: {
            type: [String, Number],
            default: 50,
        },

        // pie tick
        isPieTickVisible: {
            type: Boolean,
            default: true,
        },
        pieTickStroke: {
            type: String,
            default: '#550000',
        },
        pieTickStrokeWidth: {
            type: [String, Number],
            default: 2,
        },
        pieTickLocation: {
            type: [String, Number],
            default: 0.5,
        },

        // legend
        legendPositionX: {
            type: [String, Number],
            default: 50,
        },
        legendCenterX: {
            type: [String, Number],
            default: 50,
        },
        legendMarginTop: {
            type: [String, Number],
            default: 15,
        },
        legendMarginBottom: {
            type: [String, Number],
            default: 15,
        },

        // data
        data: Array,
        pieDataBinder: Array,
        pieCategoryField: {
            type: String,
            default: 'displayName',
        },
    },
    data() {
        return {
            root: '',
            legend: '',
            chart: '',
            series: '',
        };
    },
    watch: {
        // container
        styleProps() {
            this.resetChart();
        },
        chartLayout() {
            this.resetChart();
        },
        // pie chart
        pieColorList() {
            this.resetChart();
        },
        pieRadius() {
            this.resetChart();
        },
        pieInnerRadius() {
            this.resetChart();
        },
        pieStartAngle() {
            this.resetChart();
        },
        pieEndAngle() {
            this.resetChart();
        },
        isPieLabelAlign() {
            this.resetChart();
        },
        // pie slice
        pieSliceBorderColor() {
            this.resetChart();
        },
        pieSliceBorderWidth() {
            this.resetChart();
        },
        pieSliceShiftRadius() {
            this.resetChart();
        },
        pieSliceFillOpacity() {
            this.resetChart();
        },
        pieSliceTooltipText() {
            this.resetChart();
        },
        isPieSlicesToggleKey() {
            this.resetChart();
        },
        // pie label
        pieLabelFill() {
            this.resetChart();
        },
        pieLabelText() {
            this.resetChart();
        },
        pieLabelTextType() {
            this.resetChart();
        },
        pieLabelRadius() {
            this.resetChart();
        },
        isPieLabelInside() {
            this.resetChart();
        },
        isPieLabelTextVisible() {
            this.resetChart();
        },
        // pie tick
        pieTickStroke() {
            this.resetChart();
        },
        pieTickStrokeWidth() {
            this.resetChart();
        },
        pieTickLocation() {
            this.resetChart();
        },
        isPieTickVisible() {
            this.resetChart();
        },
        // legend
        legendPositionX() {
            this.resetChart();
        },
        legendCenterX() {
            this.resetChart();
        },
        legendMarginTop() {
            this.resetChart();
        },
        legendMarginBottom() {
            this.resetChart();
        },
    },
    mounted() {
        this.createChart();
    },
    computed: {
        computedPieData() {
            let result = [];
            this.pieDataBinder.map((item) => {
                result.push({ ...item, value: this.data[0][item.key] });
            });
            return result;
        },
    },
    methods: {
        createChart() {
            this.root = am5.Root.new(this.$refs.pieChartDiv);
            this.root.setThemes([am5themes_Animated.new(this.root)]);

            this.setChartConfig();
            this.setSeries();
            this.setLegend(this.series);

            this.series.appear(1000, 100);
        },
        resetChart() {
            if (this.root) {
                this.root.dispose();
            }
            this.createChart();
        },
        setChartConfig() {
            const self = this;
            this.chart = this.root.container.children.push(
                am5percent.PieChart.new(self.root, {
                    layout: self.root[this.chartLayout],
                })
            );
        },
        setSeries() {
            this.pieDataBinder.map(() => {
                this.series = this.chart.series.push(
                    am5percent.PieSeries.new(this.root, {
                        name: '123',
                        categoryField: this.pieCategoryField,
                        valueField: 'value',
                        radius: am5.percent(Number(this.pieRadius)),
                        innerRadius: am5.percent(Number(this.pieInnerRadius)),
                        startAngle: Number(this.pieStartAngle),
                        endAngle: Number(this.pieEndAngle),
                        alignLabels: this.isPieLabelAlign,
                    })
                );
            });

            this.setColor();
            this.setSlice();
            this.setLabel();
            this.setTicks();

            this.series.data.setAll(this.computedPieData);
        },
        setColor() {
            let pieColors = [];

            if (typeof this.pieColorList === 'string') {
                this.pieColorList = this.pieColorList.split(',');
            }

            this.pieColorList.map((item) => {
                pieColors.push(am5.color(item));
            });
            this.series.get('colors').set('colors', [...pieColors]);
        },
        setSlice() {
            this.series.slices.template.setAll({
                shiftRadius: Number(this.pieSliceShiftRadius),
                fillOpacity: Number(this.pieSliceFillOpacity),
                stroke: am5.color(this.pieSliceBorderColor),
                strokeWidth: Number(this.pieSliceBorderWidth),
            });

            if (!this.isPieSlicesToggleKey) {
                this.series.slices.template.set('toggleKey', 'none');
            }

            this.series.slices.template.set('tooltipText', this.pieSliceTooltipText);

            // this.series.slices.template.states.create('active', {
            //     shiftRadius: 0,
            //     strokeWidth: 5,
            // });
        },
        setLabel() {
            this.series.labels.template.setAll({
                // fontSize: 20,
                fill: am5.color(this.pieLabelFill),
                text: this.pieLabelText,
                textType: this.pieLabelTextType,
                radius: Number(this.pieLabelRadius),
                inside: this.isPieLabelInside,
                centerX: am5.percent(100),
            });

            this.series.labels.template.set('forceHidden', !this.isPieLabelTextVisible);
        },
        setTicks() {
            this.series.ticks.template.setAll({
                stroke: am5.color(this.pieTickStroke),
                strokeWidth: Number(this.pieTickStrokeWidth),
            });

            this.series.ticks.template.set('visible', this.isPieTickVisible);

            this.series.ticks.template.setAll({
                location: Number(this.pieTickLocation),
            });
        },

        setLegend() {
            this.legend = this.chart.children.push(
                am5.Legend.new(this.root, {
                    x: am5.percent(Number(this.legendPositionX)),
                    centerX: am5.percent(Number(this.legendCenterX)),
                    marginTop: Number(Number(this.legendMarginTop)),
                    marginBottom: Number(Number(this.legendMarginBottom)),
                })
            );
            this.legend.data.setAll(this.series.dataItems);
        },
    },
};
</script>
