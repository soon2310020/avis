<template>
    <div>
        <p class="common-component-desc"></p>
        <div class="common-component-content">
            <!-- simple (only required) -->
            <!-- <pie-chart :data="data" :pie-data-binder="pieDataBinder" :pie-category-field="pieCategoryField"></pie-chart> -->
            <!-- detail (all options) -->
            <pie-chart
                :style-props="styleProps"
                :chart-layout="chartLayout"
                :pie-color-list="pieColorList"
                :pie-radius="pieRadius"
                :pie-inner-radius="pieInnerRadius"
                :pie-start-angle="pieStartAngle"
                :pie-end-angle="pieEndAngle"
                :is-pie-slices-toggle-key="isPieSlicesToggleKey"
                :pie-slice-border-color="pieSliceBorderColor"
                :pie-slice-border-width="pieSliceBorderWidth"
                :pie-slice-shift-radius="pieSliceShiftRadius"
                :pie-slice-fill-opacity="pieSliceFillOpacity"
                :pie-slice-tooltip-text="pieSliceTooltipText"
                :is-pie-label-inside="isPieLabelInside"
                :is-pie-label-text-visible="isPieLabelTextVisible"
                :is-pie-label-align="isPieLabelAlign"
                :pie-label-fill="pieLabelFill"
                :pie-label-text="pieLabelText"
                :pie-label-text-type="pieLabelTextType"
                :pie-label-radius="pieLabelRadius"
                :is-pie-tick-visible="isPieTickVisible"
                :pie-tick-stroke="pieTickStroke"
                :pie-tick-stroke-width="pieTickStrokeWidth"
                :pie-tick-location="pieTickLocation"
                :legend-position-x="legendPositionX"
                :legend-center-x="legendCenterX"
                :legend-margin-top="legendMarginTop"
                :legend-margin-bottom="legendMarginBottom"
                :data="data"
                :pie-data-binder="pieDataBinder"
                :pie-category-field="pieCategoryField"
            ></pie-chart>

            <ul class="props-tab">
                <li @click="setPropsTab('chart-container')" :active="propsTab === 'chart-container' ? true : false">Chart Container</li>
                <li @click="setPropsTab('pie-chart')" :active="propsTab === 'pie-chart' ? true : false">Pie Chart</li>
                <li @click="setPropsTab('pie-slice')" :active="propsTab === 'pie-slice' ? true : false">Pie Slice</li>
                <li @click="setPropsTab('pie-label')" :active="propsTab === 'pie-label' ? true : false">Pie Label</li>
                <li @click="setPropsTab('pie-tick')" :active="propsTab === 'pie-tick' ? true : false">Pie Tick</li>
                <li @click="setPropsTab('legend')" :active="propsTab === 'legend' ? true : false">Legend</li>
                <li @click="setPropsTab('data')" :active="propsTab === 'data' ? true : false">Data</li>
            </ul>

            <!-- chart container -->
            <div class="sub-content" v-if="propsTab === 'chart-container'">
                <!-- styleProps props -->
                <div class="props-select-row">
                    <p><span>style-props</span></p>
                    <input v-model="styleProps" />
                    <p>
                        <span>Specifies the chart container style. (width, height, etc.)</span>
                        <b>{{ styleProps }}</b>
                    </p>
                </div>

                <!-- chartLayout props -->
                <div class="props-select-row">
                    <p><span>chart-layout</span></p>
                    <select v-model="chartLayout">
                        <option value="verticalLayout">verticalLayout</option>
                        <option value="horizontalLayout">horizontalLayout</option>
                    </select>
                    <p>
                        <span>Set the container layout.</span>
                        <b>{{ chartLayout }}</b>
                    </p>
                </div>
            </div>

            <!-- pie chart -->
            <div class="sub-content" v-if="propsTab === 'pie-chart'">
                <!-- pieColorList props -->
                <div class="props-select-row">
                    <p><span>pie-color-list</span></p>
                    <input v-model="pieColorList" />
                    <p>
                        <span>This is the basic color list to be displayed on the pie chart.</span>
                        <b>{{ pieColorList }}</b>
                    </p>
                </div>

                <!-- pieRadius props -->
                <div class="props-select-row">
                    <p><span>pie-radius</span></p>
                    <input v-model="pieRadius" />
                    <p>
                        <span>Sets the radius of the pie chart.</span>
                        <b>{{ pieRadius }}</b>
                    </p>
                </div>

                <!-- pieInnerRadius props -->
                <div class="props-select-row">
                    <p><span>pie-inner-radius</span></p>
                    <input v-model="pieInnerRadius" />
                    <p>
                        <span>Sets the inner radius of the pie chart.</span>
                        <b>{{ pieInnerRadius }}</b>
                    </p>
                </div>

                <!-- pieStartAngle props -->
                <div class="props-select-row">
                    <p><span>pie-start-angle</span></p>
                    <input v-model="pieStartAngle" />
                    <p>
                        <span>Set the starting angle of the pie chart.</span>
                        <b>{{ pieStartAngle }}</b>
                    </p>
                </div>

                <!-- pieEndAngle props -->
                <div class="props-select-row">
                    <p><span>pie-end-angle</span></p>
                    <input v-model="pieEndAngle" />
                    <p>
                        <span>Sets the ending angle of the pie chart.</span>
                        <b>{{ pieEndAngle }}</b>
                    </p>
                </div>
            </div>

            <!-- pie slice -->
            <div class="sub-content" v-if="propsTab === 'pie-slice'">
                <!-- isPieSlicesToggleKey props -->
                <div class="props-select-row">
                    <p><span>is-pie-slices-toggle-key</span></p>
                    <select v-model="isPieSlicesToggleKey">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <span>Defines whether pie slices will pop out when pressed.</span>
                        <b>{{ isPieSlicesToggleKey }}</b>
                    </p>
                </div>

                <!-- pieSliceBorderColor props -->
                <div class="props-select-row">
                    <p><span>pie-slice-border-color</span></p>
                    <input v-model="pieSliceBorderColor" />
                    <p>
                        <span>The border color of the pie slice.</span>
                        <b>{{ pieSliceBorderColor }}</b>
                    </p>
                </div>

                <!-- pieSliceBorderWidth props -->
                <div class="props-select-row">
                    <p><span>pie-slice-border-width</span></p>
                    <input v-model="pieSliceBorderWidth" />
                    <p>
                        <span>The thickness of the border line of the pie slice.</span>
                        <b>{{ pieSliceBorderWidth }}</b>
                    </p>
                </div>

                <!-- pieSliceShiftRadius props -->
                <div class="props-select-row">
                    <p><span>pie-slice-shift-radius</span></p>
                    <input v-model="pieSliceShiftRadius" />
                    <p>
                        <b>{{ pieSliceShiftRadius }}</b>
                    </p>
                </div>

                <!-- pieSliceFillOpacity props -->
                <div class="props-select-row">
                    <p><span>pie-slice-fill-opacity</span></p>
                    <input v-model="pieSliceFillOpacity" />
                    <p>
                        <span>The transparency of the pie slice. (0 to 1) </span>
                        <b>{{ pieSliceFillOpacity }}</b>
                    </p>
                </div>

                <!-- pieSliceTooltipText props -->
                <div class="props-select-row">
                    <p><span>pie-slice-tooltip-text</span></p>
                    <input v-model="pieSliceTooltipText" />
                    <p>
                        <span>This is the tooltip content to be displayed above the pie slice.</span>
                        <b>{{ pieSliceTooltipText }}</b>
                    </p>
                </div>
            </div>

            <!-- pie label -->
            <div class="sub-content" v-if="propsTab === 'pie-label'">
                <!-- isPieLabelTextVisible props -->
                <div class="props-select-row">
                    <p><span>is-pie-label-text-visible</span></p>
                    <select v-model="isPieLabelTextVisible">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <span>Defines whether to display label text.</span>
                        <b>{{ isPieLabelTextVisible }}</b>
                    </p>
                </div>

                <!-- isPieLabelAlign props -->
                <div class="props-select-row">
                    <p><span>is-pie-label-align</span></p>
                    <select v-model="isPieLabelAlign">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <span>Set label align.</span>
                        <b>{{ isPieLabelAlign }}</b>
                    </p>
                </div>

                <!-- isPieLabelInside props -->
                <div class="props-select-row">
                    <p><span>is-pie-label-inside</span></p>
                    <select v-model="isPieLabelInside">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <span>Defines whether the label is displayed in the pie chart.</span>
                        <b>{{ isPieLabelInside }}</b>
                    </p>
                </div>

                <!-- pieLabelFill props -->
                <div class="props-select-row">
                    <p><span>pie-label-fill</span></p>
                    <input v-model="pieLabelFill" />
                    <p>
                        <span>The label color.</span>
                        <b>{{ pieLabelFill }}</b>
                    </p>
                </div>

                <!-- pieLabelText props -->
                <div class="props-select-row">
                    <p><span>pie-label-text</span></p>
                    <input v-model="pieLabelText" />
                    <p>
                        <span>The content to be displayed on the label.</span>
                        <b>{{ pieLabelText }}</b>
                    </p>
                </div>

                <!-- pieLabelTextType props -->
                <div class="props-select-row">
                    <p><span>pie-label-text-type</span></p>
                    <select v-model="pieLabelTextType">
                        <option value="radial">radial</option>
                        <option value="circular">circular</option>
                    </select>
                    <p>
                        <span>Defines the format in which the label is displayed.</span>
                        <b>{{ pieLabelTextType }}</b>
                    </p>
                </div>

                <!-- pieLabelRadius props -->
                <div class="props-select-row">
                    <p><span>pie-label-radius</span></p>
                    <input v-model="pieLabelRadius" />
                    <p>
                        <span>Sets the radius of label .</span>
                        <b>{{ pieLabelRadius }}</b>
                    </p>
                </div>
            </div>

            <!-- pie tick -->
            <div class="sub-content" v-if="propsTab === 'pie-tick'">
                <!-- isPieTickVisible props -->
                <div class="props-select-row">
                    <p><span>is-pie-tick-visible</span></p>
                    <select v-model="isPieTickVisible">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <span>label Defines whether gridlines are displayed.</span>
                        <b>{{ isPieTickVisible }}</b>
                    </p>
                </div>

                <!-- pieTickLocation props -->
                <div class="props-select-row">
                    <p><span>pie-tick-location</span></p>
                    <input v-model="pieTickLocation" />
                    <p>
                        <span>label Sets the starting point of the grid lines. (0 to 1)</span>
                        <b>{{ pieTickLocation }}</b>
                    </p>
                </div>

                <!-- pieTickStroke props -->
                <div class="props-select-row">
                    <p><span>pie-tick-stroke</span></p>
                    <input v-model="pieTickStroke" />
                    <p>
                        <span>label Sets the color of the gridlines.</span>
                        <b>{{ pieTickStroke }}</b>
                    </p>
                </div>

                <!-- pieTickStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>pie-tick-stroke-width</span></p>
                    <input v-model="pieTickStrokeWidth" />
                    <p>
                        <span>label Sets the thickness of grid lines.</span>
                        <b>{{ pieTickStrokeWidth }}</b>
                    </p>
                </div>
            </div>

            <!-- legend -->
            <div class="sub-content" v-if="propsTab === 'legend'">
                <!-- legendPositionX props -->
                <div class="props-select-row">
                    <p><span>legend-position-x</span></p>
                    <input v-model="legendPositionX" />
                    <p>
                        <span>Sets the x-axis position of the legend.</span>
                        <b>{{ legendPositionX }}</b>
                    </p>
                </div>

                <!-- legendCenterX props -->
                <div class="props-select-row">
                    <p><span>legend-center-x</span></p>
                    <input v-model="legendCenterX" />
                    <p>
                        <span>Sort legends by legend-position-x.</span>
                        <b>{{ legendCenterX }}</b>
                    </p>
                </div>

                <!-- legendMarginTop props -->
                <div class="props-select-row">
                    <p><span>legend-margin-top</span></p>
                    <input v-model="legendMarginTop" />
                    <p>
                        <span>Sets the margin above the legend.</span>
                        <b>{{ legendMarginTop }}</b>
                    </p>
                </div>

                <!-- legendMarginBottom props -->
                <div class="props-select-row">
                    <p><span>legend-margin-bottom</span></p>
                    <input v-model="legendMarginBottom" />
                    <p>
                        <span>Sets the margin below the legend.</span>
                        <b>{{ legendMarginBottom }}</b>
                    </p>
                </div>
            </div>

            <!-- data -->
            <div class="sub-content" v-if="propsTab === 'data'">
                <!-- data props -->
                <div class="props-select-row">
                    <p><span>data</span></p>
                    <p>
                        <span>Raw data to put in the pie chart</span>
                        <b>{{ data }}</b>
                    </p>
                </div>

                <!-- pieDataBinder props -->
                <div class="props-select-row">
                    <p><span>pie-items</span></p>
                    <p>
                        <span>A binder that defines how to represent data as a pie chart</span>
                        <b>{{ pieDataBinder }}</b>
                    </p>
                </div>

                <!-- pieCategoryField props -->
                <div class="props-select-row">
                    <p><span>pie-category-field</span></p>
                    <p>
                        <span>Set the key of the item to be displayed on the screen among data.</span>
                        <b>{{ pieCategoryField }}</b>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    name: 'PieChartTest',
    components: {
        'pie-chart': httpVueLoader('/components/common/amcharts5/pie-chart.vue'),
    },
    data() {
        return {
            // chart container
            styleProps: 'width: 100%; height: 400px',
            chartLayout: 'verticalLayout',

            // pie chart
            pieCategoryField: 'displayName',
            pieColorList: ['#095256', '#087f8c', '#5aaa95', '#86a873', '#bb9f06'],
            pieRadius: 100,
            pieInnerRadius: 0,
            pieStartAngle: -90,
            pieEndAngle: 270,

            // pie slice
            isPieSlicesToggleKey: true,
            pieSliceBorderColor: '#000000',
            pieSliceBorderWidth: 2,
            pieSliceShiftRadius: 0,
            pieSliceFillOpacity: 0.5,
            pieSliceTooltipText: "{category}: [bold]{valuePercentTotal.formatNumber('0.00')}%[/] ({value})",

            // pie label
            isPieLabelAlign: true,
            isPieLabelInside: true,
            isPieLabelTextVisible: true,
            pieLabelFill: '#550000',
            pieLabelText: '{category}',
            pieLabelTextType: 'circular',
            pieLabelRadius: 50,

            // pie tick
            isPieTickVisible: true,
            pieTickStroke: '#550000',
            pieTickStrokeWidth: 2,
            pieTickLocation: 0.5,

            // legend
            legendPositionX: 50,
            legendCenterX: 50,
            legendMarginTop: 15,
            legendMarginBottom: 15,

            // data
            data: [
                {
                    title: '20220101',
                    processingTime: 10,
                    warmupTime: 9,
                    cooldownTime: 6,
                },
            ],

            pieDataBinder: [
                {
                    key: 'processingTime', // required
                    displayName: 'Processing Time', // required
                    tooltipText: '{name} in {categoryX}: {valueY} {info}', // options
                    yAxis2: true, // options
                    color: '#000000', // options
                },
                {
                    key: 'warmupTime',
                    displayName: 'Warmup Time',
                },
                {
                    key: 'cooldownTime',
                    displayName: 'Cooldown Time',
                },
            ],

            propsTab: 'chart-container',
            // active: false,
        };
    },
    methods: {
        setPropsTab(param) {
            this.propsTab = param;
        },
    },
};
</script>
