<template>
    <div class="xychart-div">
        <p class="common-component-desc">xy chart</p>
        <div class="common-component-content">
            <!-- simple mode (only required options, There is no need to add all data binder items, just add the chart binder you want to show.) -->

            <xy-chart
                :data="newData"
                :category="category"
                :bubble-data-binder="bubbleDataBinder"
                :line-data-binder="lineDataBinder"
                :bar-data-binder="barDataBinder"
                :candlestick-data-binder="candlestickDataBinder"
                :chart-item-click-handler="chartItemClickHandler"
            ></xy-chart>

            <!-- detail mode (all options) -->
            <overall-xy-chart
                :style-props="styleProps"
                :chart-layout="chartLayout"
                :pan-x="panX"
                :pan-y="panY"
                :wheel-x="wheelX"
                :wheel-y="wheelY"
                :x-axis-type="xAxisType"
                :y-axis-type="yAxisType"
                :y-second-axis-type="ySecondAxisType"
                :x-axis-name="xAxisName"
                :y-axis-name="yAxisName"
                :y-second-axis-name="ySecondAxisName"
                :is-y-axis-left="isYAxisLeft"
                :is-y-second-axis-left="isYSecondAxisLeft"
                :is-x-axis-tooltip="isXAxisTooltip"
                :is-y-axis-tooltip="isYAxisTooltip"
                :is-scrollbar-x="isScrollbarX"
                :is-scrollbar-y="isScrollbarY"
                :is-scrollbar-start-grip="isScrollbarStartGrip"
                :is-scrollbar-end-grip="isScrollbarEndGrip"
                :scrollbar-thumb-fill="scrollbarThumbFill"
                :scrollbar-thumb-fill-opacity="scrollbarThumbFillOpacity"
                :scrollbar-grip-scale="scrollbarGripScale"
                :scrollbar-start-position="scrollbarStartPosition"
                :scrollbar-end-position="scrollbarEndPosition"
                :zoom-in-range="zoomInRange"
                :zoom-out-range="zoomOutRange"
                :is-scrollbar-x-down="isScrollbarXDown"
                :is-detail-scrollbar-x="isDetailScrollbarX"
                :detail-scrollbar-height="detailScrollbarHeight"
                :detail-scrollbar-line-color="detailScrollbarLineColor"
                :detail-scrollbar-line-stroke-width="detailScrollbarLineStrokeWidth"
                :detail-scrollbar-line-fill-opacity="detailScrollbarLineFillOpacity"
                :detail-scrollbar-background-fill="detailScrollbarBackgroundFill"
                :detail-scrollbar-background-fill-opacity="detailScrollbarBackgroundFillOpacity"
                :detail-scrollbar-background-corner-radius-t-l="detailScrollbarBackgroundCornerRadiusTL"
                :detail-scrollbar-background-corner-radius-t-r="detailScrollbarBackgroundCornerRadiusTR"
                :detail-scrollbar-background-corner-radius-b-l="detailScrollbarBackgroundCornerRadiusBL"
                :detail-scrollbar-background-corner-radius-b-r="detailScrollbarBackgroundCornerRadiusBR"
                :is-overall-scrollbar-x="isOverallScrollbarX"
                :overall-scrollbar-height="overallScrollbarHeight"
                :overall-scrollbar-line-color="overallScrollbarLineColor"
                :overall-scrollbar-line-stroke-width="overallScrollbarLineStrokeWidth"
                :overall-scrollbar-line-fill-opacity="overallScrollbarLineFillOpacity"
                :overall-scrollbar-background-fill="overallScrollbarBackgroundFill"
                :overall-scrollbar-background-fill-opacity="overallScrollbarBackgroundFillOpacity"
                :overall-scrollbar-background-corner-radius-t-l="overallScrollbarBackgroundCornerRadiusTL"
                :overall-scrollbar-background-corner-radius-t-r="overallScrollbarBackgroundCornerRadiusTR"
                :overall-scrollbar-background-corner-radius-b-l="overallScrollbarBackgroundCornerRadiusBL"
                :overall-scrollbar-background-corner-radius-b-r="overallScrollbarBackgroundCornerRadiusBR"
                :zoom-pan-handler="zoomPanHandler"
                :is-bar-stacked="isBarStacked"
                :is-bar-clustered="isBarClustered"
                :bar-width="barWidth"
                :bar-stroke-width="barStrokeWidth"
                :bar-fill-opacity="barFillOpacity"
                :bar-corner-radius-t-l="barCornerRadiusTL"
                :bar-corner-radius-t-r="barCornerRadiusTR"
                :bar-color-list="barColorListComputed"
                :line-stroke-width="lineStrokeWidth"
                :is-line-fill-visible="isLineFillVisible"
                :line-fill-opacity="lineFillOpacity"
                :line-bullet-stroke-width="lineBulletStrokeWidth"
                :line-bullet-radius="lineBulletRadius"
                :line-color-list="lineColorListComputed"
                :bubble-line-stroke-width="bubbleLineStrokeWidth"
                :bubble-bullet-stroke-width="bubbleBulletStrokeWidth"
                :bubble-bullet-radius="bubbleBulletRadius"
                :bubble-color-list="bubbleColorListComputed"
                :candlestick-rise-color="candlestickRiseColor"
                :candlestick-drop-color="candlestickDropColor"
                :is-legend-display="isLegendDisplay"
                :legend-layout="legendLayout"
                :legend-x="legendX"
                :legend-center-x="legendCenterX"
                :legend-y="legendY"
                :legend-center-y="legendCenterY"
                :data="data"
                :category="category"
                :scrollbar-display-value="scrollbarDisplayValue"
                :bubble-data-binder="bubbleDataBinder"
                :line-data-binder="lineDataBinder"
                :bar-data-binder="barDataBinder"
                :candlestick-data-binder="candlestickDataBinder"
                :chart-item-click-handler="chartItemClickHandler"
            ></overall-xy-chart>

            <ul class="props-tab">
                <li @click="setPropsTab('chart-container')" :active="propsTab === 'chart-container' ? true : false">Chart Container</li>
                <li @click="setPropsTab('axis')" :active="propsTab === 'axis' ? true : false">Axis</li>
                <li @click="setPropsTab('scrollbar')" :active="propsTab === 'scrollbar' ? true : false">Scrollbar</li>
                <li @click="setPropsTab('detail-scrollbar')" :active="propsTab === 'detail-scrollbar' ? true : false">Detail Scrollbar</li>
                <li @click="setPropsTab('overall-scrollbar')" :active="propsTab === 'overall-scrollbar' ? true : false">
                    Overall Scrollbar
                </li>
                <li @click="setPropsTab('bar-chart')" :active="propsTab === 'bar-chart' ? true : false">Bar Chart</li>
                <li @click="setPropsTab('line-chart')" :active="propsTab === 'line-chart' ? true : false">Line Chart</li>
                <li @click="setPropsTab('bubble-chart')" :active="propsTab === 'bubble-chart' ? true : false">Bubble Chart</li>
                <li @click="setPropsTab('candlestick-chart')" :active="propsTab === 'candlestick-chart' ? true : false">
                    Candlestick Chart
                </li>
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
                        Specifies the chart container style. (width, height, etc.)
                        <br />
                        <b>{{ styleProps }}</b>
                    </p>
                </div>

                <!-- chartLayout props -->
                <div class="props-select-row">
                    <p><span>chart-layout</span></p>
                    <select v-model="chartLayout">
                        <option value="verticalLayout">verticalLayout</option>
                        <option value="horizontalLayout">horizontalLayout</option>
                        <option value="gridLayout">gridLayout</option>
                        <option value="">not set</option>
                    </select>
                    <p>
                        Defines whether dragging left or right is possible.
                        <br />
                        <b>{{ chartLayout }}</b>
                    </p>
                </div>

                <!-- panX props -->
                <div class="props-select-row">
                    <p><span>pan-x</span></p>
                    <select v-model="panX">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Defines whether dragging left or right is possible.
                        <br />
                        <b>{{ panX }}</b>
                    </p>
                </div>

                <!-- panY props -->
                <div class="props-select-row">
                    <p><span>pan-y</span></p>
                    <select v-model="panY">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Defines whether dragging up and down is possible.
                        <br />
                        <b>{{ panY }}</b>
                    </p>
                </div>

                <!-- wheelX props -->
                <div class="props-select-row">
                    <p><span>wheel-x</span></p>
                    <select v-model="wheelX">
                        <option value="none">none (Do nothing)</option>
                        <option value="zoomX">zoomX (Zoom chart horizontally.)</option>
                        <option value="zoomY">zoomY (Zoom chart vertically.)</option>
                        <option value="zoomXY">zoomXY (Zoom chart both horizontally and vertically. )</option>
                        <option value="panX">panX (Pan chart horizontally. )</option>
                        <option value="panY">panY (Pan chart vertically.)</option>
                        <option value="panXY">panXY (Pan chart both horizontally and vertically.)</option>
                    </select>
                    <p>
                        Defines the action to be taken when the wheel is moved left or right.
                        <br />
                        <b>{{ wheelX }}</b>
                    </p>
                </div>

                <!-- wheelY props -->
                <div class="props-select-row">
                    <p><span>wheel-y</span></p>
                    <select v-model="wheelY">
                        <option value="none">none (Do nothing)</option>
                        <option value="zoomX">zoomX (Zoom chart horizontally.)</option>
                        <option value="zoomY">zoomY (Zoom chart vertically.)</option>
                        <option value="zoomXY">zoomXY (Zoom chart both horizontally and vertically. )</option>
                        <option value="panX">panX (Pan chart horizontally. )</option>
                        <option value="panY">panY (Pan chart vertically.)</option>
                        <option value="panXY">panXY (Pan chart both horizontally and vertically.)</option>
                    </select>
                    <p>
                        Defines the action to be taken when the wheel is moved up and down.
                        <br />
                        <b>{{ wheelY }}</b>
                    </p>
                </div>
            </div>

            <!-- axis -->
            <div class="sub-content" v-if="propsTab === 'axis'">
                <!-- xAxisName props -->
                <div class="props-select-row">
                    <p><span>x-axis-name</span></p>
                    <input v-model="xAxisName" />
                    <p>
                        <br />
                        <b>{{ xAxisName }}</b>
                    </p>
                </div>

                <!-- yAxisName props -->
                <div class="props-select-row">
                    <p><span>y-axis-name</span></p>
                    <input v-model="yAxisName" />
                    <p>
                        <br />
                        <b>{{ yAxisName }}</b>
                    </p>
                </div>

                <!-- ySecondAxisName props -->
                <div class="props-select-row">
                    <p><span>y-second-axis-name</span></p>
                    <input v-model="ySecondAxisName" />
                    <p>
                        <br />
                        <b>{{ ySecondAxisName }}</b>
                    </p>
                </div>

                <!-- isYAxisLeft props -->
                <div class="props-select-row">
                    <p><span>is-y-axis-left</span></p>
                    <select v-model="isYAxisLeft">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        If true, display the y-axis to the left.
                        <br />
                        <b>{{ isYAxisLeft }}</b>
                    </p>
                </div>

                <!-- isYSecondAxisLeft props -->
                <div class="props-select-row">
                    <p><span>is-y-second-y-axis-left</span></p>
                    <select v-model="isYSecondAxisLeft">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        If true, display the second y-axis to the left.
                        <br />
                        <b>{{ isYSecondAxisLeft }}</b>
                    </p>
                </div>

                <!-- isXAxisTooltip props -->
                <div class="props-select-row">
                    <p><span>isXAxisTooltip</span></p>
                    <select v-model="isXAxisTooltip">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <br />
                        <b>{{ isXAxisTooltip }}</b>
                    </p>
                </div>

                <!-- isYAxisTooltip props -->
                <div class="props-select-row">
                    <p><span>isYAxisTooltip</span></p>
                    <select v-model="isYAxisTooltip">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <br />
                        <b>{{ isYAxisTooltip }}</b>
                    </p>
                </div>
            </div>

            <!-- scrollbar -->
            <div class="sub-content" v-if="propsTab === 'scrollbar'">
                <!-- isScrollbarX props -->
                <div class="props-select-row">
                    <p><span>is-scrollbar-x</span></p>
                    <select v-model="isScrollbarX">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Show the default x-axis scroll bar. It cannot be used when the detail scrollbar has been created.
                        <br />
                        <b>{{ isScrollbarX }}</b>
                    </p>
                </div>

                <!-- isScrollbarY props -->
                <div class="props-select-row">
                    <p><span>is-scrollbar-y</span></p>
                    <select v-model="isScrollbarY">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Show the default y-axis scroll bar.
                        <b>{{ isScrollbarY }}</b>
                    </p>
                </div>

                <!-- isScrollbarStartGrip -->
                <div class="props-select-row">
                    <p><span>is-scrollbar-start-grip</span></p>
                    <select v-model="isScrollbarStartGrip">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ isScrollbarStartGrip }}</b>
                    </p>
                </div>

                <!-- isScrollbarEndGrip -->
                <div class="props-select-row">
                    <p><span>is-scrollbar-end-grip</span></p>
                    <select v-model="isScrollbarEndGrip">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ isScrollbarEndGrip }}</b>
                    </p>
                </div>

                <!-- scrollbarThumbFill -->
                <div class="props-select-row">
                    <p><span>scrollbar-thumb-fill</span></p>
                    <input v-model="scrollbarThumbFill" />
                    <p>
                        <b>{{ scrollbarThumbFill }}</b>
                    </p>
                </div>

                <!-- scrollbarThumbFillOpacity -->
                <div class="props-select-row">
                    <p><span>scrollbar-thumb-fill-opacity</span></p>
                    <input v-model="scrollbarThumbFillOpacity" />
                    <p>
                        <b>{{ scrollbarThumbFillOpacity }}</b>
                    </p>
                </div>

                <!-- scrollbarGripScale -->
                <div class="props-select-row">
                    <p><span>scrollbar-grip-scale</span></p>
                    <input v-model="scrollbarGripScale" />
                    <p>
                        <b>{{ scrollbarGripScale }}</b>
                    </p>
                </div>

                <!-- scrollbarStartPosition -->
                <div class="props-select-row">
                    <p><span>scrollbar-start-position</span></p>
                    <input v-model="scrollbarStartPosition" />
                    <p>
                        <b>{{ scrollbarStartPosition }}</b>
                    </p>
                </div>

                <!-- scrollbarEndPosition -->
                <div class="props-select-row">
                    <p><span>scrollbar-end-position</span></p>
                    <input v-model="scrollbarEndPosition" />
                    <p>
                        <b>{{ scrollbarEndPosition }}</b>
                    </p>
                </div>

                <!-- zoomInRange -->
                <div class="props-select-row">
                    <p><span>zoom-in-range</span></p>
                    <input v-model="zoomInRange" />
                    <p>
                        <b>{{ zoomInRange }}</b>
                    </p>
                </div>

                <!-- zoomOutRange -->
                <div class="props-select-row">
                    <p><span>zoom-out-range</span></p>
                    <input v-model="zoomOutRange" />
                    <p>
                        <b>{{ zoomOutRange }}</b>
                    </p>
                </div>

                <!-- isScrollbarXDown props -->
                <div class="props-select-row">
                    <p><span>is-scrollbar-x-down</span></p>
                    <select v-model="isScrollbarXDown">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ isScrollbarXDown }}</b>
                    </p>
                </div>
            </div>

            <!-- detail-scrollbar -->
            <div class="sub-content" v-if="propsTab === 'detail-scrollbar'">
                <!-- isDetailScrollbarX props -->
                <div class="props-select-row">
                    <p><span>is-detail-scrollbar-x</span></p>
                    <select v-model="isDetailScrollbarX">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Set whether to use the detailed scroll bar. (You can use scroll events (zoom, next, previous) only by using the
                        detail scroll bar.)
                        <br />
                        <b>{{ isDetailScrollbarX }}</b>
                    </p>
                </div>

                <!--  detailScrollbarHeight props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-height</span></p>
                    <input v-model="detailScrollbarHeight" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ detailScrollbarHeight }}</b>
                    </p>
                </div>

                <!--  detailScrollbarLineColor props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-line-color</span></p>
                    <input v-model="detailScrollbarLineColor" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ detailScrollbarLineColor }}</b>
                    </p>
                </div>

                <!--  detailScrollbarLineStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-line-stroke-width</span></p>
                    <input v-model="detailScrollbarLineStrokeWidth" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ detailScrollbarLineStrokeWidth }}</b>
                    </p>
                </div>

                <!--  detailScrollbarLineFillOpacity props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-line-fill-opacity</span></p>
                    <input v-model="detailScrollbarLineFillOpacity" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ detailScrollbarLineFillOpacity }}</b>
                    </p>
                </div>

                <!--  detailScrollbarBackgroundFillOpacity props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-background-fill-opacity</span></p>
                    <input v-model="detailScrollbarBackgroundFillOpacity" />
                    <p>
                        desc
                        <br />
                        <b>{{ detailScrollbarBackgroundFillOpacity }}</b>
                    </p>
                </div>

                <!--  detailScrollbarBackgroundCornerRadiusTL props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-background-corner-radius-t-l</span></p>
                    <input v-model="detailScrollbarBackgroundCornerRadiusTL" />
                    <p>
                        desc
                        <br />
                        <b>{{ detailScrollbarBackgroundCornerRadiusTL }}</b>
                    </p>
                </div>

                <!--  detailScrollbarBackgroundCornerRadiusTR props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-background-corner-radius-t-r</span></p>
                    <input v-model="detailScrollbarBackgroundCornerRadiusTR" />
                    <p>
                        desc
                        <br />
                        <b>{{ detailScrollbarBackgroundCornerRadiusTR }}</b>
                    </p>
                </div>

                <!--  detailScrollbarBackgroundCornerRadiusBL props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-background-corner-radius-b-l</span></p>
                    <input v-model="detailScrollbarBackgroundCornerRadiusBL" />
                    <p>
                        desc
                        <br />
                        <b>{{ detailScrollbarBackgroundCornerRadiusBL }}</b>
                    </p>
                </div>

                <!--  detailScrollbarBackgroundCornerRadiusBR props -->
                <div class="props-select-row">
                    <p><span>detail-scrollbar-background-corner-radius-b-r</span></p>
                    <input v-model="detailScrollbarBackgroundCornerRadiusBR" />
                    <p>
                        desc
                        <br />
                        <b>{{ detailScrollbarBackgroundCornerRadiusBR }}</b>
                    </p>
                </div>
            </div>

            <!-- overall-scrollbar -->
            <div class="sub-content" v-if="propsTab === 'overall-scrollbar'">
                <!-- isOverallScrollbarX props -->
                <div class="props-select-row">
                    <p><span>is-overall-scrollbar-x</span></p>
                    <select v-model="isOverallScrollbarX">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Set whether to use the detailed scroll bar. (You can use scroll events (zoom, next, previous) only by using the
                        detail scroll bar.)
                        <br />
                        <b>{{ isOverallScrollbarX }}</b>
                    </p>
                </div>

                <!--  overallScrollbarHeight props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-height</span></p>
                    <input v-model="overallScrollbarHeight" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ overallScrollbarHeight }}</b>
                    </p>
                </div>

                <!--  overallScrollbarLineColor props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-line-color</span></p>
                    <input v-model="overallScrollbarLineColor" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ overallScrollbarLineColor }}</b>
                    </p>
                </div>

                <!--  overallScrollbarLineStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-line-stroke-width</span></p>
                    <input v-model="overallScrollbarLineStrokeWidth" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ overallScrollbarLineStrokeWidth }}</b>
                    </p>
                </div>

                <!--  overallScrollbarLineFillOpacity props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-line-fill-opacity</span></p>
                    <input v-model="overallScrollbarLineFillOpacity" />
                    <p>
                        Defines whether to position the detail scrollbar below.
                        <br />
                        <b>{{ overallScrollbarLineFillOpacity }}</b>
                    </p>
                </div>

                <!--  overallScrollbarBackgroundFillOpacity props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-background-fill-opacity</span></p>
                    <input v-model="overallScrollbarBackgroundFillOpacity" />
                    <p>
                        desc
                        <br />
                        <b>{{ overallScrollbarBackgroundFillOpacity }}</b>
                    </p>
                </div>

                <!--  overallScrollbarBackgroundCornerRadiusTL props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-background-corner-radius-t-l</span></p>
                    <input v-model="overallScrollbarBackgroundCornerRadiusTL" />
                    <p>
                        desc
                        <br />
                        <b>{{ overallScrollbarBackgroundCornerRadiusTL }}</b>
                    </p>
                </div>

                <!--  overallScrollbarBackgroundCornerRadiusTR props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-background-corner-radius-t-r</span></p>
                    <input v-model="overallScrollbarBackgroundCornerRadiusTR" />
                    <p>
                        desc
                        <br />
                        <b>{{ overallScrollbarBackgroundCornerRadiusTR }}</b>
                    </p>
                </div>

                <!--  overallScrollbarBackgroundCornerRadiusBL props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-background-corner-radius-b-l</span></p>
                    <input v-model="overallScrollbarBackgroundCornerRadiusBL" />
                    <p>
                        desc
                        <br />
                        <b>{{ overallScrollbarBackgroundCornerRadiusBL }}</b>
                    </p>
                </div>

                <!--  overallScrollbarBackgroundCornerRadiusBR props -->
                <div class="props-select-row">
                    <p><span>overall-scrollbar-background-corner-radius-b-r</span></p>
                    <input v-model="overallScrollbarBackgroundCornerRadiusBR" />
                    <p>
                        desc
                        <br />
                        <b>{{ overallScrollbarBackgroundCornerRadiusBR }}</b>
                    </p>
                </div>
            </div>

            <!-- bar chart -->
            <div class="sub-content" v-if="propsTab === 'bar-chart'">
                <!-- barColorList props -->
                <div class="props-select-row">
                    <p><span>bar-color-list</span></p>
                    <input v-model="barColorList" />
                    <p>
                        An array to define the color of bar chart items.
                        <br />
                        <b>{{ barColorListComputed }}</b>
                    </p>
                </div>

                <!-- isBarStacked props -->
                <div class="props-select-row">
                    <p><span>is-bar-stacked</span></p>
                    <select v-model="isBarStacked">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        If there are multiple bar items, define whether to stack them.
                        <br />
                        <b>{{ isBarStacked }}</b>
                    </p>
                </div>

                <!-- isBarClustered props -->
                <div class="props-select-row">
                    <p><span>is-bar-clustered</span></p>
                    <select v-model="isBarClustered">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Defines whether or not to overlap if there are multiple bar items.
                        <br />
                        <b>{{ isBarClustered }}</b>
                    </p>
                </div>

                <!-- barWidth props -->
                <div class="props-select-row">
                    <p><span>bar-width</span></p>
                    <input v-model="barWidth" />
                    <p>
                        Sets the width of the bar.
                        <br />
                        <b>{{ barWidth }}</b>
                    </p>
                </div>

                <!-- barStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>bar-stroke-width</span></p>
                    <input v-model="barStrokeWidth" />
                    <p>
                        Defines the border thickness of bar chart items.
                        <br />
                        <b>{{ barStrokeWidth }}</b>
                    </p>
                </div>

                <!-- barFillOpacity props -->
                <div class="props-select-row">
                    <p><span>bar-fill-opacity</span></p>
                    <input v-model="barFillOpacity" />
                    <p>
                        Defines the internal transparency of bar chart items.
                        <br />
                        <b>{{ barFillOpacity }}</b>
                    </p>
                </div>

                <!-- barCornerRadiusTL props -->
                <div class="props-select-row">
                    <p><span>bar-corner-radius-t-l</span></p>
                    <input v-model="barCornerRadiusTL" />
                    <p>
                        Defines the roundness of the upper left corner of bar chart items.
                        <br />
                        <b>{{ barCornerRadiusTL }}</b>
                    </p>
                </div>

                <!-- barCornerRadiusTR props -->
                <div class="props-select-row">
                    <p><span>bar-corner-radius-t-r</span></p>
                    <input v-model="barCornerRadiusTR" />
                    <p>
                        Defines the roundness of the upper right corner of bar chart items.
                        <br />
                        <b>{{ barCornerRadiusTR }}</b>
                    </p>
                </div>
            </div>

            <!-- bubble chart -->
            <div class="sub-content" v-if="propsTab === 'bubble-chart'">
                <!-- bubbleColorList props -->
                <div class="props-select-row">
                    <p><span>bubble-color-list</span></p>
                    <input v-model="bubbleColorList" />
                    <p>
                        An array to define the color of bubble chart items.
                        <br />
                        <b>{{ bubbleColorListComputed }}</b>
                    </p>
                </div>

                <!-- bubbleLineStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>bubble-line-stroke-width</span></p>
                    <input v-model="bubbleLineStrokeWidth" />
                    <p>
                        <br />
                        <b>{{ bubbleLineStrokeWidth }}</b>
                    </p>
                </div>

                <!-- bubbleBulletStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>bubble-bullet-stroke-width</span></p>
                    <input v-model="bubbleBulletStrokeWidth" />
                    <p>
                        Set the bullet thickness of bubble chart items.
                        <br />
                        <b>{{ bubbleBulletStrokeWidth }}</b>
                    </p>
                </div>

                <!-- bubbleBulletRadius props -->
                <div class="props-select-row">
                    <p><span>bubble-bullet-radius</span></p>
                    <input v-model="bubbleBulletRadius" />
                    <p>
                        Sets the bullet size of bubble chart items.
                        <br />
                        <b>{{ bubbleBulletRadius }}</b>
                    </p>
                </div>
            </div>

            <!-- line chart -->
            <div class="sub-content" v-if="propsTab === 'line-chart'">
                <!-- lineColorList props -->
                <div class="props-select-row">
                    <p><span>line-color-list</span></p>
                    <input v-model="lineColorList" />
                    <p>
                        An array to define the color of line chart items.
                        <br />
                        <b>{{ lineColorListComputed }}</b>
                    </p>
                </div>

                <!-- lineStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>line-stroke-width</span></p>
                    <input v-model="lineStrokeWidth" />
                    <p>
                        Set the line thickness of the line chart item.
                        <br />
                        <b>{{ lineStrokeWidth }}</b>
                    </p>
                </div>

                <!-- isLineFillVisible props -->
                <div class="props-select-row">
                    <p><span>is-line-fill-visible</span></p>
                    <select v-model="isLineFillVisible">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        Defines whether to set the background of the line chart item.
                        <br />
                        <b>{{ isLineFillVisible }}</b>
                    </p>
                </div>

                <!-- lineFillOpacity props -->
                <div class="props-select-row">
                    <p><span>line-fill-opacity</span></p>
                    <input v-model="lineFillOpacity" />
                    <p>
                        When line-fill-visible is true, set the background transparency of the line chart item.
                        <br />
                        <b>{{ lineFillOpacity }}</b>
                    </p>
                </div>

                <!-- lineBulletStrokeWidth props -->
                <div class="props-select-row">
                    <p><span>line-bullet-stroke-width</span></p>
                    <input v-model="lineBulletStrokeWidth" />
                    <p>
                        Set the bullet thickness of line chart items.
                        <br />
                        <b>{{ lineBulletStrokeWidth }}</b>
                    </p>
                </div>

                <!-- lineBulletRadius props -->
                <div class="props-select-row">
                    <p><span>line-bullet-radius</span></p>
                    <input v-model="lineBulletRadius" />
                    <p>
                        Sets the bullet size of line chart items.
                        <br />
                        <b>{{ lineBulletRadius }}</b>
                    </p>
                </div>
            </div>

            <!-- candlestick chart -->
            <div class="sub-content" v-if="propsTab === 'candlestick-chart'">
                <!-- candlestickRiseColor props -->
                <div class="props-select-row">
                    <p><span>candlestick-rise-color</span></p>
                    <input v-model="candlestickRiseColor" />
                    <p>
                        Defines the color to show when the value of a candlestick chart item is rising.
                        <br />
                        <b>{{ candlestickRiseColor }}</b>
                    </p>
                </div>

                <!-- candlestickDropColor props -->
                <div class="props-select-row">
                    <p><span>candlestick-drop-color</span></p>
                    <input v-model="candlestickDropColor" />
                    <p>
                        Defines the color to show when the value of a candlestick chart item is falling.
                        <br />
                        <b>{{ candlestickDropColor }}</b>
                    </p>
                </div>
            </div>

            <!-- legend chart -->
            <div class="sub-content" v-if="propsTab === 'legend'">
                <!-- isLegendDisplay props -->
                <div class="props-select-row">
                    <p><span>is-legend-display</span></p>
                    <select v-model="isLegendDisplay">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        legend를 표시할 것인지 여부를 설정합니다.
                        <br />
                        <b>{{ isLegendDisplay }}</b>
                    </p>
                </div>

                <!-- legendLayout props -->
                <div class="props-select-row">
                    <p><span>legend-layout</span></p>
                    <select v-model="legendLayout">
                        <option value="verticalLayout">verticalLayout</option>
                        <option value="horizontalLayout">horizontalLayout</option>
                        <option value="gridLayout">gridLayout</option>
                        <option value="">not set</option>
                    </select>
                    <p>
                        legend를 표시할 것인지 여부를 설정합니다.
                        <br />
                        <b>{{ legendLayout }}</b>
                    </p>
                </div>

                <!-- legendX props -->
                <div class="props-select-row">
                    <p><span>legend-x</span></p>
                    <input v-model="legendX" />
                    <p>
                        legendX
                        <br />
                        <b>{{ legendX }}</b>
                    </p>
                </div>

                <!-- legendCenterX props -->
                <div class="props-select-row">
                    <p><span>legend-center-x</span></p>
                    <input v-model="legendCenterX" />
                    <p>
                        legendCenterX
                        <br />
                        <b>{{ legendCenterX }}</b>
                    </p>
                </div>

                <!-- legendY props -->
                <div class="props-select-row">
                    <p><span>legend-y</span></p>
                    <input v-model="legendY" />
                    <p>
                        legendY
                        <br />
                        <b>{{ legendY }}</b>
                    </p>
                </div>

                <!-- legendCenterY props -->
                <div class="props-select-row">
                    <p><span>legend-center-y</span></p>
                    <input v-model="legendCenterY" />
                    <p>
                        legendCenterY
                        <br />
                        <b>{{ legendCenterY }}</b>
                    </p>
                </div>
            </div>

            <!-- data -->
            <div class="sub-content" v-if="propsTab === 'data'">
                <!-- category props -->
                <div class="props-select-row">
                    <p><span>category</span></p>
                    <p>
                        Defines what to define the x-axis as. (from data items)
                        <br />
                        <b>{{ category }}</b>
                    </p>
                </div>

                <!-- scrollbarDisplayValue props -->
                <div class="props-select-row">
                    <p><span>scrollbar-display-value</span></p>
                    <input v-model="scrollbarDisplayValue" />
                    <p>
                        <br />
                        <b>{{ scrollbarDisplayValue }}</b>
                    </p>
                </div>

                <!-- barDataBinder props -->
                <div class="props-select-row">
                    <p><span>bar-data-binder</span></p>
                    <p>
                        Defines whether to use the bar chart as an item. (from data items)
                        <br /><br />
                        <b>{{ barDataBinder }}</b>
                    </p>
                </div>

                <!-- bubbleDataBinder props -->
                <div class="props-select-row">
                    <p><span>bubble-data-binder</span></p>
                    <p>
                        Defines whether or not to use the bubble chart as an item. (from data items)
                        <br /><br />
                        <b>{{ bubbleDataBinder }}</b>
                    </p>
                </div>

                <!-- lineDataBinder props -->
                <div class="props-select-row">
                    <p><span>line-data-binder</span></p>
                    <p>
                        Defines whether or not to use the line chart as an item. (from data items)
                        <br /><br />
                        <b>{{ lineDataBinder }}</b>
                    </p>
                </div>

                <!-- candlestickDataBinder props -->
                <div class="props-select-row">
                    <p><span>candlestick-data-binder</span></p>
                    <p>
                        Defines which item the candlestick chart should be. (from data items)
                        <br /><br />
                        <b>{{ candlestickDataBinder }}</b>
                    </p>
                </div>

                <!-- data props -->
                <div class="props-select-row">
                    <p><span>data</span></p>
                    <p>
                        rawdata as the basis for the chart
                        <br /><br />
                        <b>{{ data }}</b>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    name: 'overallXYChart',
    components: {
        'overall-xy-chart': overallXYChart,
        // 'xy-chart': XYChart,
    },
    computed: {
        barColorListComputed() {
            if (typeof this.barColorList === 'string') {
                let result = this.barColorList.split(',');
                return result;
            } else {
                return this.barColorList;
            }
        },

        lineColorListComputed() {
            if (typeof this.lineColorList === 'string') {
                let result = this.lineColorList.split(',');
                return result;
            } else {
                return this.lineColorList;
            }
        },

        bubbleColorListComputed() {
            if (typeof this.bubbleColorList === 'string') {
                let result = this.bubbleColorList.split(',');
                return result;
            } else {
                return this.bubbleColorList;
            }
        },
    },
    data() {
        return {
            // chart container
            styleProps: 'width: 100%; height: 400px',
            chartLayout: 'verticalLayout',
            panX: true,
            panY: false,
            wheelX: 'none',
            wheelY: 'zoomX',

            // axis
            xAxisType: 'CategoryAxis',
            yAxisType: 'ValueAxis',
            ySecondAxisType: 'ValueAxis',
            xAxisName: 'xAxisName',
            yAxisName: 'yAxisName',
            ySecondAxisName: 'ySecondAxisName',
            isYAxisLeft: true,
            isYSecondAxisLeft: false,
            isXAxisTooltip: true,
            isYAxisTooltip: true,

            // scrollbar
            isScrollbarX: false,
            isScrollbarY: false,
            isScrollbarStartGrip: true,
            isScrollbarEndGrip: true,
            scrollbarThumbFill: '#550000',
            scrollbarThumbFillOpacity: 0.02,
            scrollbarGripScale: 0.9,
            scrollbarStartPosition: 0.2,
            scrollbarEndPosition: 0.8,
            zoomInRange: 0.2,
            zoomOutRange: 0.99,
            isScrollbarXDown: false,

            // detail scrollbar
            isDetailScrollbarX: true,
            detailScrollbarHeight: 50,
            detailScrollbarLineColor: '#fb9f57',
            detailScrollbarLineStrokeWidth: 2,
            detailScrollbarLineFillOpacity: 0.2,
            detailScrollbarBackgroundFill: '#000000',
            detailScrollbarBackgroundFillOpacity: 0.05,
            detailScrollbarBackgroundCornerRadiusTL: 0,
            detailScrollbarBackgroundCornerRadiusTR: 10,
            detailScrollbarBackgroundCornerRadiusBL: 10,
            detailScrollbarBackgroundCornerRadiusBR: 10,

            // detail scrollbar
            isOverallScrollbarX: true,
            overallScrollbarHeight: 50,
            overallScrollbarLineColor: '#579ffb',
            overallScrollbarLineStrokeWidth: 2,
            overallScrollbarLineFillOpacity: 0.1,
            overallScrollbarBackgroundFill: '#000000',
            overallScrollbarBackgroundFillOpacity: 0.05,
            overallScrollbarBackgroundCornerRadiusTL: 0,
            overallScrollbarBackgroundCornerRadiusTR: 10,
            overallScrollbarBackgroundCornerRadiusBL: 10,
            overallScrollbarBackgroundCornerRadiusBR: 10,

            // bar chart
            isBarStacked: false,
            isBarClustered: false,
            barColorList: ['#BCE2C7', '#4EBCD5', '#1A2281', '#4169e1', '#1e90ff', '#0000cd', '#000080'],
            barWidth: 20,
            barFillOpacity: 0.3,
            barStrokeWidth: 1.5,
            barCornerRadiusTL: 2,
            barCornerRadiusTR: 2,

            // bubble chart
            bubbleLineStrokeWidth: 3,
            bubbleBulletStrokeWidth: 3,
            bubbleBulletRadius: 5,
            bubbleColorList: ['#f08080', '#fa8072', '#e9967a', '#ff7f50', '#ff6347', '#f4a460', '#ffa07a'],

            // line chart
            lineStrokeWidth: 3,
            isLineFillVisible: true,
            lineFillOpacity: 0.1,
            lineBulletStrokeWidth: 3,
            lineBulletRadius: 5,
            lineColorList: ['#f08080', '#fa8072', '#e9967a', '#ff7f50', '#ff6347', '#f4a460', '#ffa07a'],

            // candlestick chart
            candlestickRiseColor: '#ff0000',
            candlestickDropColor: '#0000ff',

            // legend
            isLegendDisplay: true,
            legendLayout: 'gridLayout',
            legendX: 50,
            legendCenterX: 50,
            legendY: 100,
            legendCenterY: 100,

            // data
            category: 'title',
            // scrollbarDisplayValue: 'maxCycleTime',
            scrollbarDisplayValue: 'ctLclL1',
            barDataBinder: [
                { key: 'cycleTime', displayName: 'cycle time', tooltipText: 'barItem tooltip: {name} in {categoryX}: {valueY} {info} 123' },
                {
                    key: 'contractedCycleTime',
                    displayName: 'contracted CycleTime',
                    tooltipText: 'barItem tooltip: {name} in {categoryX}: {valueY} {info}',
                    // color: '#000000',
                },
            ],
            bubbleDataBinder: [
                {
                    key: 'maxCycleTime',
                    displayName: 'Max CT(sec)',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                },
                {
                    key: 'minCycleTime',
                    displayName: 'Min CT(sec)',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                },
                {
                    key: 'uptimeTarget',
                    displayName: 'uptime target',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                    ySecondAxis: true,
                },
            ],
            lineDataBinder: [
                {
                    key: 'maxCycleTime',
                    displayName: 'Max CT(sec)',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                },
                {
                    key: 'minCycleTime',
                    displayName: 'Min CT(sec)',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                },
                {
                    key: 'uptimeTarget',
                    displayName: 'uptime target',
                    tooltipText: 'lineItem tooltip: {name} in {categoryX}: {valueY} {info}',
                    ySecondAxis: true,
                },
            ],
            candlestickDataBinder: {
                displayName: 'Cycle Time (sec)',
                highValue: { key: 'ulct', displayName: 'ulct' },
                defaultValue: { key: 'mfct', displayName: 'mfct' },
                openValue: { key: 'llct', displayName: 'llct' },
                lowValue: { key: 'tempct', displayName: 'tempct' },
            },
            data: [
                {
                    title: '20220101',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220102',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 15,
                    mfct: 11,
                    llct: 12,
                    tempct: 9,
                    uptimeTarget: 21,
                },
                {
                    title: '20220103',
                    cycleTime: 16.07,
                    maxCycleTime: 16.3,
                    minCycleTime: 16.0,
                    cycleTimeWithin: 16.07,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220104',
                    cycleTime: 16.1,
                    maxCycleTime: 16.1,
                    minCycleTime: 16.1,
                    cycleTimeWithin: 16.1,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220105',
                    cycleTime: 16.1,
                    maxCycleTime: 16.1,
                    minCycleTime: 16.1,
                    cycleTimeWithin: 16.1,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220106',
                    cycleTime: 16.14,
                    maxCycleTime: 17.5,
                    minCycleTime: 16.0,
                    cycleTimeWithin: 16.14,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220107',
                    cycleTime: 16.07,
                    maxCycleTime: 16.5,
                    minCycleTime: 16.0,
                    cycleTimeWithin: 16.07,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220108',
                    cycleTime: 15.88,
                    maxCycleTime: 16.0,
                    minCycleTime: 7.7,
                    cycleTimeWithin: 15.88,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    moldId: null,
                    moldCreatedAt: null,
                    title: '20220109',
                    data: 2257,
                    dataPercent: 0.0,
                    trend: null,
                    moldCount: 1,
                    cycleTime: 16.93,
                    maxCycleTime: 17.0,
                    minCycleTime: 16.0,
                    cycleTimeWithin: 16.93,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220110',
                    cycleTime: 17.0,
                    maxCycleTime: 17.0,
                    minCycleTime: 17.0,
                    cycleTimeWithin: 17.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220111',
                    cycleTime: 17.0,
                    maxCycleTime: 17.4,
                    minCycleTime: 17.0,
                    cycleTimeWithin: 17.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220112',
                    cycleTime: 16.94,
                    maxCycleTime: 17.2,
                    minCycleTime: 11.3,
                    cycleTimeWithin: 16.94,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220113',
                    cycleTime: 16.92,
                    maxCycleTime: 17.2,
                    minCycleTime: 16.9,
                    cycleTimeWithin: 16.92,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220114',
                    cycleTime: 16.91,
                    maxCycleTime: 17.0,
                    minCycleTime: 16.9,
                    cycleTimeWithin: 16.91,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220115',
                    cycleTime: 16.9,
                    maxCycleTime: 16.9,
                    minCycleTime: 16.9,
                    cycleTimeWithin: 16.9,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220116',
                    cycleTime: 16.9,
                    maxCycleTime: 16.9,
                    minCycleTime: 16.9,
                    cycleTimeWithin: 16.9,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220117',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220118',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220119',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220120',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220121',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220122',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220123',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220124',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220125',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220126',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220127',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220128',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220129',
                    moldCount: 0,
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220130',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
                {
                    title: '20220131',
                    cycleTime: 0.0,
                    maxCycleTime: 0.0,
                    minCycleTime: 0.0,
                    contractedCycleTime: 17.23647450007895,
                    ulct: 13,
                    mfct: 12,
                    llct: 11,
                    tempct: 10,
                    uptimeTarget: 21,
                },
            ],

            propsTab: 'chart-container',
        };
    },
    methods: {
        setPropsTab(param) {
            this.propsTab = param;
        },
        chartItemClickHandler(ev) {
            console.log('chartItemClickHandler');

            console.log(ev.target.dataItem.component.className);

            let item = ev.target.dataItem.dataContext;
            let userData = ev.target.get('userData');

            console.log('item: ', item);
            console.log('userData: ', userData);

            let context =
                `chart Type: ${userData.chartType}\n\n` +
                `userData category
                    ${userData.category}: ${item[userData.category]}\n\n` +
                `userData value
                    ${userData.value}: ${item[userData.value]}`;

            alert(context);
        },
        zoomPanHandler(param) {
            // zoom in, zoom out, previous, next
            alert(`A param(${param}) is passed to the scrollbarHander function. !!!!`);

            // axios.get('../../json/planned.json').then((res) => {
            //     console.log('res: ', res);
            //     this.data = res.data;
            // });

            // temp
            let savedata = [...this.data];
            this.data = this.data.splice(0, 0);
            this.data = savedata;
        },
    },
};
</script>
