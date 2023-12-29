<template>
  <div
    id="op-chart-mold"
    class="modal fade"
    role="dialog"
    aria-labelledby="title-part-chart"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-title">
          <div
            class="head-line"
            style="
              position: absolute;
              background: #52a1ff;
              height: 8px;
              border-radius: 6px 6px 0 0;
              top: 0;
              left: 0;
              width: 100%;
            "
          ></div>
          <div>
            <span class="span-title">{{ resources["tooling_report"] }}</span>
            <div class="switch-zone">
              <div
                id="switch-graph"
                :class="{ active: switchTab === 'switch-graph' }"
                @click="changeSwitchTab('switch-graph')"
              >
                <span v-text="resources['graph']"></span>
              </div>
              <div
                id="switch-detail"
                :class="{ active: switchTab === 'switch-detail' }"
                @click="changeSwitchTab('switch-detail')"
              >
                <span v-text="resources['detail']"></span>
              </div>
            </div>
          </div>
          <span
            class="close-button"
            style="
              font-size: 25px;
              display: flex;
              align-items: center;
              height: 17px;
              cursor: pointer;
            "
            data-dismiss="modal"
            aria-label="Close"
          >
            <span class="t-icon-close"></span>
          </span>
        </div>
        <div
          v-show="switchTab === 'switch-graph'"
          style="padding: 20px 56px; overflow: scroll; min-height: 660px"
          class="modal-body"
        >
          <div>
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
              "
            >
              <div
                style="flex-wrap: wrap"
                class="btn-group btn-group-toggle float-left"
                data-toggle="buttons"
              >
                <label
                  class="btn switch-btn"
                  v-bind:class="{ active: chartType === 'QUANTITY' }"
                  @click.prevent="changeChartDataType('QUANTITY')"
                >
                  <input
                    type="radio"
                    name="options"
                    value="DAY"
                    autocomplete="off"
                  />
                  <span v-text="resources['cap_shot']"></span>
                </label>
                <label
                  class="btn switch-btn"
                  v-bind:class="{
                    active: chartType === 'CYCLE_TIME',
                  }"
                  @click.prevent="changeChartDataType('CYCLE_TIME')"
                >
                  <input
                    type="radio"
                    name="options"
                    value="WEEK"
                    autocomplete="off"
                  />
                  <span v-text="resources['cycle_time']"></span>
                </label>
                <label
                  class="btn switch-btn"
                  v-bind:class="{ active: chartType === 'UPTIME' }"
                  @click.prevent="changeChartDataType('UPTIME')"
                >
                  <input
                    type="radio"
                    name="options"
                    value="WEEK"
                    autocomplete="off"
                  />
                  <span v-text="resources['uptime']"></span>
                </label>
                <template v-if="isNotCMSCounter() && !isHourlyEnabled">
                  <label
                    class="btn switch-btn"
                    v-bind:class="{
                      active: chartType === 'CYCLE_TIME_ANALYSIS',
                    }"
                    @click.prevent="changeChartDataType('CYCLE_TIME_ANALYSIS')"
                  >
                    <input
                      type="radio"
                      name="options"
                      value="WEEK"
                      autocomplete="off"
                    />
                    <span v-text="resources['cycle_time_analysis']"></span>
                  </label>
                  <label
                    class="btn switch-btn"
                    v-bind:class="{
                      active: chartType === 'TEMPERATURE_ANALYSIS',
                    }"
                    @click.prevent="changeChartDataType('TEMPERATURE_ANALYSIS')"
                  >
                    <input
                      type="radio"
                      name="options"
                      value="WEEK"
                      autocomplete="off"
                    />
                    <span v-text="resources['temperature']"></span>
                  </label>
                </template>
                <!-- <label
                  v-if="accelerationList.length > 0 && isNotNewGenCounter()"
                  class="btn switch-btn"
                  :class="{ active: chartType === 'ACCELERATION' }"
                  @click.prevent="changeChartDataType('ACCELERATION')"
                >
                  <input
                    type="radio"
                    name="options"
                    value="WEEK"
                    autocomplete="off"
                  />
                  <span v-text="resources['acceleration']"></span>
                </label> -->
              </div>
              <div class="row graph-header">
                <div class="select-date-group">
                  <data-filter
                    class="select-graph-group"
                    v-show="chartType === 'QUANTITY'"
                    :title="resources['graph_settings']"
                    :title-placeholder="resources['graph_settings']"
                    type="Graph Settings"
                    all-type="All graphs"
                    :resources="resources"
                    :option-array="shotSelected"
                    :selected-array="shotDefaulted"
                    @operatingstatus="filterQueryFunc"
                    @update-config="updateCurrentChartSettings"
                  >
                  </data-filter>
                  <data-filter
                    class="select-graph-group"
                    v-show="chartType === 'CYCLE_TIME'"
                    ref="graph"
                    :title="resources['graph_settings']"
                    :title-placeholder="resources['graph_settings']"
                    type="Graph Settings"
                    all-type="All graphs"
                    :resources="resources"
                    :option-array="cycleTimeSelected"
                    :selected-array="cycleTimeDefaulted"
                    @operatingstatus="filterQueryFunc"
                    @update-config="updateCurrentChartSettings"
                  >
                  </data-filter>
                  <data-filter
                    class="select-graph-group"
                    v-show="chartType === 'UPTIME'"
                    title="Graph Settings"
                    title-placeholder="Graph Settings"
                    type="Graph Settings"
                    all-type="All graphs"
                    :resources="resources"
                    :option-array="UptimeSelected"
                    :selected-array="UptimeDefaulted"
                    @operatingstatus="filterQueryFunc"
                    @update-config="updateCurrentChartSettings"
                  >
                  </data-filter>
                  <div v-show="chartType !== 'ACCELERATION'">
                    <date-picker
                      v-if="isShowChart"
                      id="mold-front"
                      :resources="resources"
                      :type="chartType"
                      :default-selected-date="datePickerType"
                      :cancel-calendar="closeDatePicker"
                      :handle-submit="handleChangeDate"
                      :current-date-data="currentDateData"
                      :min-date="minDate"
                      :max-date="maxDate"
                      :exists-hourly="isNotCMSCounter() && !isHourlyEnabled"
                    >
                    </date-picker>
                  </div>
                  <template v-if="chartType === 'ACCELERATION'">
                    <a-date-picker
                      v-model="startDate"
                      format="YYYY-MM-DD (dddd)"
                      @change="getAcceleration"
                    >
                    </a-date-picker>
                    <a-time-picker
                      format="HH"
                      @change="getAcceleration"
                      v-model="startDate"
                    ></a-time-picker>
                  </template>
                </div>
              </div>
            </div>
            <div class="info-zone">
              <div class="info-zone_tooling">
                <b>Tooling ID</b>
                <span
                  v-if="mold.equipmentCode && mold.equipmentCode.length > 20"
                  v-bind:data-tooltip-text="mold.equipmentCode"
                  style="width: auto; margin: 4px 104px 2px 15px; padding: 0"
                >
                  {{ replaceLongtext(mold.equipmentCode, 20) }}</span
                >
                <span v-else>{{ mold.equipmentCode }}</span>
              </div>
              <div v-if="!loadingPartChange" class="info-zone_produce">
                <div v-if="numberOfPart === 1">
                  <a-tooltip placement="bottom">
                    <template slot="title">
                      <table
                        v-if="partChangeData[0].changed"
                        class="custom-table-tooltip"
                      >
                        <tr>
                          <th>Part ID Change</th>
                          <th>Date/Time</th>
                          <th>Reported By</th>
                        </tr>
                        <tr>
                          <td>
                            <div style="display: flex">
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in partChangeData[0]
                                    .original"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                              <div
                                style="
                                  margin: 0 12px;
                                  display: flex;
                                  align-items: center;
                                "
                              >
                                <img
                                  style="width: 11px; height: 11px"
                                  src="/images/icon/new/change-status-white.svg"
                                />
                              </div>
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in partChangeData[0]
                                    .changed"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                            </div>
                          </td>
                          <td>
                            <span>{{
                              convertDate(partChangeData[0].dateTime)
                            }}</span>
                          </td>
                          <td>
                            <span>{{
                              replaceLongtext(partChangeData[0].reportedBy, 40)
                            }}</span>
                          </td>
                        </tr>
                      </table>
                      <table
                        v-else-if="
                          partChangeData[0].original != null &&
                          partChangeData[0].original.length > 0
                        "
                        class="custom-table-tooltip"
                      >
                        <tr>
                          <th>Part Name/ID</th>
                        </tr>
                        <tr>
                          <td>
                            <!--                            <div>{{ partChangeData[0].partName }}</div>-->
                            <div>
                              {{ partChangeData[0].original[0].name }}
                            </div>
                            <div>
                              {{
                                replaceLongtext(
                                  partChangeData[0].original[0].code,
                                  20
                                )
                              }}
                            </div>
                          </td>
                        </tr>
                      </table>
                    </template>
                    <span
                      v-if="singlePartName"
                      style="color: rgb(52, 145, 255)"
                      >{{ replaceLongtext(singlePartName, 20) }}</span
                    >
                  </a-tooltip>
                  was produced in the selected period
                  <a-tooltip placement="bottom">
                    <template slot="title">
                      <table
                        v-if="partChangeData[0].changed"
                        class="custom-table-tooltip"
                      >
                        <tr>
                          <th>Part ID Change</th>
                          <th>Date/Time</th>
                          <th>Reported By</th>
                        </tr>
                        <tr>
                          <td>
                            <div style="display: flex">
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in partChangeData[0]
                                    .original"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                              <div
                                style="
                                  margin: 0 12px;
                                  display: flex;
                                  align-items: center;
                                "
                              >
                                <img
                                  style="width: 11px; height: 11px"
                                  src="/images/icon/new/change-status-white.svg"
                                />
                              </div>
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in partChangeData[0]
                                    .changed"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                            </div>
                          </td>
                          <td>
                            <span>{{
                              convertDate(partChangeData[0].dateTime)
                            }}</span>
                          </td>
                          <td>
                            <span>{{ partChangeData[0].reportedBy }}</span>
                          </td>
                        </tr>
                      </table>
                      <table
                        v-else-if="
                          partChangeData[0].original != null &&
                          partChangeData[0].original.length > 0
                        "
                        class="custom-table-tooltip"
                      >
                        <tr>
                          <th>Part Name/ID</th>
                        </tr>
                        <tr>
                          <td>
                            <!--                            <div>{{ partChangeData[0].partName }}</div>-->
                            <div>
                              {{ partChangeData[0].original[0].name }}
                            </div>
                            <div style="font-size: 11px">
                              {{
                                replaceLongtext(
                                  partChangeData[0].original[0].code,
                                  20
                                )
                              }}
                            </div>
                          </td>
                        </tr>
                      </table>
                    </template>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="13.463"
                      height="13.463"
                      viewBox="0 0 13.463 13.463"
                    >
                      <g
                        id="Icon_ionic-ios-information-circle-outline"
                        data-name="Icon ionic-ios-information-circle-outline"
                        transform="translate(-3.375 -3.375)"
                      >
                        <path
                          id="Path_2002"
                          data-name="Path 2002"
                          d="M16.552,11.353a.667.667,0,1,1,.663.647A.648.648,0,0,1,16.552,11.353Zm.045,1.155h1.236v4.663H16.6Z"
                          transform="translate(-7.112 -3.958)"
                          fill="#3491ff"
                        />
                        <path
                          id="Path_2003"
                          data-name="Path 2003"
                          d="M10.106,4.281a5.823,5.823,0,1,1-4.12,1.706,5.787,5.787,0,0,1,4.12-1.706m0-.906a6.731,6.731,0,1,0,6.731,6.731,6.73,6.73,0,0,0-6.731-6.731Z"
                          transform="translate(0 0)"
                          fill="#3491ff"
                        />
                      </g>
                    </svg>
                  </a-tooltip>
                </div>
                <div v-else>
                  <a-tooltip placement="bottom">
                    <template slot="title">
                      <table
                        class="custom-table-tooltip"
                        style="width: 370px !important"
                      >
                        <tr>
                          <th>Part ID Change</th>
                          <th>Date/Time</th>
                          <th>Reported By</th>
                        </tr>
                        <tr
                          v-for="(part, index) in partChangeData"
                          :key="index"
                        >
                          <td>
                            <div style="display: flex">
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in part.original"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                              <div
                                style="
                                  margin: 0 12px;
                                  display: flex;
                                  align-items: center;
                                "
                              >
                                <img
                                  style="width: 11px; height: 11px"
                                  src="/images/icon/new/change-status-white.svg"
                                />
                              </div>
                              <div
                                style="
                                  display: flex;
                                  align-items: flex-start;
                                  flex-direction: column;
                                  justify-content: center;
                                "
                              >
                                <span
                                  v-for="(item, index) in part.changed"
                                  :key="index"
                                >
                                  {{ replaceLongtext(item.code, 20) }}
                                </span>
                              </div>
                            </div>
                          </td>
                          <td style="min-width: 126px">
                            <span>{{ convertDate(part.dateTime) }}</span>
                          </td>
                          <td>
                            <span>{{
                              replaceLongtext(part.reportedBy, 40)
                            }}</span>
                          </td>
                        </tr>
                      </table>
                    </template>
                    <span style="color: #3491ff">
                      {{
                        numberOfPart === 0
                          ? "No parts were produced in the selected period."
                          : numberOfPart +
                            " Parts produced in the selected period"
                      }}
                    </span>
                    <span>
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="13.463"
                        height="13.463"
                        viewBox="0 0 13.463 13.463"
                      >
                        <g
                          id="Icon_ionic-ios-information-circle-outline"
                          data-name="Icon ionic-ios-information-circle-outline"
                          transform="translate(-3.375 -3.375)"
                        >
                          <path
                            id="Path_2002"
                            data-name="Path 2002"
                            d="M16.552,11.353a.667.667,0,1,1,.663.647A.648.648,0,0,1,16.552,11.353Zm.045,1.155h1.236v4.663H16.6Z"
                            transform="translate(-7.112 -3.958)"
                            fill="#3491ff"
                          />
                          <path
                            id="Path_2003"
                            data-name="Path 2003"
                            d="M10.106,4.281a5.823,5.823,0,1,1-4.12,1.706,5.787,5.787,0,0,1,4.12-1.706m0-.906a6.731,6.731,0,1,0,6.731,6.731,6.73,6.73,0,0,0-6.731-6.731Z"
                            transform="translate(0 0)"
                            fill="#3491ff"
                          />
                        </g>
                      </svg>
                    </span>
                  </a-tooltip>
                </div>
              </div>
            </div>
          </div>
          <div
            class="frequently-chart-container-wrapper"
            style="
              position: absolute;
              left: 0px;
              width: 100%;
              background: rgb(255, 255, 255);
              z-index: 1;
              background: #fff;
              padding-bottom: 15px;
            "
          >
            <div class="frequently-chart-container">
              <a-icon
                v-on:click="changeFrequentlyChart('PRE')"
                v-if="!disablePreFrequently() && this.isShowFrequentlyChart"
                style="fontsize: 23px"
                class="icon frequently"
                type="caret-left"
              ></a-icon>
              <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              <div style="width: 96%" class="op-chart-wrapper">
                <div class="op-chart-content">
                  <canvas class="chart" id="chart-frequently"></canvas>
                  <div id="legend-chart-frequently"></div>
                </div>
              </div>
              <a-icon
                v-on:click="changeFrequentlyChart('NEXT')"
                v-if="!disableNextFrequently() && this.isShowFrequentlyChart"
                style="fontsize: 23px"
                class="icon frequently"
                type="caret-right"
              ></a-icon>
              <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
            </div>

            <div class="col-auto ml-auto text-right" style="position: relative">
              <button
                type="button"
                class="btn btn-default op-close-frequently"
                style="position: absolute; right: 15px; bottom: -2px"
                @click="hideFrequentlyChart"
              >
                Back
              </button>
            </div>
          </div>

          <!-- temp chart.js & amchart5 wrapper -->
          <div style="min-height: 500px">
            <!-- 기존 차트 컨테이너 -->
            <div
              class="next-chart-container"
              :class="{ 'hourly-custom-legend': isTemBar }"
              style="position: relative"
              v-show="chartType !== 'ACCELERATION'"
            >
              <a-icon
                v-on:click="changeMonthOrDay('PRE')"
                v-if="
                  ((requestParam.dateViewType === 'DAY' ||
                    requestParam.dateViewType === 'HOUR') &&
                    !disablePre() &&
                    !this.isShowFrequentlyChart &&
                    !this.isSelectedCustomRange) ||
                  hasCustomRangePre
                "
                style="fontsize: 23px"
                class="icon"
                type="caret-left"
              ></a-icon>
              <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              <div style="width: 96%" class="op-chart-wrapper">
                <div class="op-chart-content">
                  <canvas class="chart" id="chart-mold"></canvas>
                  <div id="chart-tooltips"></div>
                </div>
                <div id="legend-chart-mold"></div>
              </div>
              <div id="hourly-chart-wrapper">
                <div class="chart-wrapper-inner">
                  <canvas class="other-chart" id="hourly-chart"></canvas>
                  <div id="chart-hint"></div>
                  <div id="circle-point">
                    <div id="shot-count-wrapper">
                      <div id="shot-count-content">
                        <div id="count-header"></div>
                        <div id="count-content">
                          <div id="count-content-left">
                            <div id="square-hint"></div>
                          </div>
                          <div id="count-content-right">
                            <div id="shot-count"></div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <a-icon
                v-on:click="changeMonthOrDay('NEXT')"
                v-if="
                  ((requestParam.dateViewType === 'DAY' ||
                    requestParam.dateViewType === 'HOUR') &&
                    !disableNext() &&
                    !this.isShowFrequentlyChart &&
                    !this.isSelectedCustomRange) ||
                  hasCustomRangeNext
                "
                style="fontsize: 23px"
                class="icon"
                type="caret-right"
              ></a-icon>
              <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              <div
                class="analysis-hint"
                v-if="
                  requestParam.dateViewType === 'HOUR' &&
                  (requestParam.chartDataType === 'CYCLE_TIME_ANALYSIS' ||
                    requestParam.chartDataType === 'TEMPERATURE_ANALYSIS')
                "
              >
                *Click on the data point to view details
              </div>

              <span
                v-if="noDataAvailable"
                style="
                  position: absolute;
                  left: 50%;
                  top: calc(50% - 40px);
                  transform: translate(-50%, -50%);
                "
                >No Data Available</span
              >
            </div>

            <!-- new chart container -->
            <bubble-chart
              v-if="chartType === 'ACCELERATION'"
              :chart-data="amchartData"
              x-axis-name="Elapsed time"
              y-axis-name="Acceleration"
              more-series-data-name="Occurred time"
              :key="bubbleChartComponentKey"
            ></bubble-chart>
          </div>

          <div
            v-if="
              isShowNoteWeek &&
              requestParam.dateViewType == 'WEEK' &&
              requestParam.chartDataType == 'UPTIME'
            "
            style="text-align: right; font-size: 13px"
            v-text="resources['weekly_uptime_target_msg']"
          ></div>
          <div
            v-if="
              isShowNoteMonth &&
              requestParam.dateViewType == 'MONTH' &&
              requestParam.chartDataType == 'UPTIME'
            "
            style="text-align: right; font-size: 13px"
            v-text="resources['monthly_uptime_target_msg']"
          ></div>
          <div
            id="op-daily-details"
            style="
              display: none;
              position: absolute;
              left: 0;
              top: 0;
              width: 100%;
              height: 100%;
              padding: 20px;
              background: #fff;
              z-index: 1;
            "
          >
            <h5>
              {{ mold.equipmentCode }} ({{ requestParam.detailDate }},
              {{ requestParam.year }})
            </h5>
            <table
              class="table table-responsive-sm table-striped"
              style="border-top: 1px solid #c8ced3"
            >
              <thead>
                <tr>
                  <th v-text="resources['start_time']"></th>
                  <th v-text="resources['end_time']"></th>
                  <th v-text="resources['shots']"></th>
                  <th v-text="resources['no_of_cavity']"></th>
                  <th v-text="resources['cycle_time'] + '(sec)'"></th>
                  <th v-text="resources['uptime'] + '(min)'"></th>
                  <th
                    v-if="isShowTemperatureColumn"
                    v-text="resources['uptime'] + '(Â°C)'"
                  ></th>
                </tr>
              </thead>
              <tbody class="op-list" style="display: none">
                <tr
                  v-for="(result, index, id) in results"
                  :id="result.id"
                  :key="id"
                >
                  <td>{{ result.firstShotDateTime }}</td>
                  <td>{{ result.lastShotDateTime }}</td>
                  <td>{{ result.shotCount }}</td>
                  <td>{{ result.cavity }}</td>
                  <td>{{ result.cycleTimeSeconds }}</td>
                  <td>{{ result.uptimeMinutes }}</td>
                  <td v-if="isShowTemperatureColumn">
                    {{ result.temperature / 10 }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div
              class="no-results"
              :class="{ 'd-none': total > 0 }"
              v-text="resources['no_results']"
            ></div>

            <div style="flex-wrap: nowrap" class="row">
              <div class="col-md-8">
                <ul class="pagination">
                  <li
                    v-for="(data, index) in pagination"
                    :key="index"
                    class="page-item"
                    :class="{ active: data.isActive }"
                  >
                    <a class="page-link" @click="paging(data.pageNumber)">{{
                      data.text
                    }}</a>
                  </li>
                </ul>
              </div>
              <div class="col-auto ml-auto">
                <button
                  type="button"
                  class="btn btn-success op-close-details"
                  v-text="resources['ok']"
                ></button>
              </div>
            </div>
          </div>
        </div>
        <div
          v-show="switchTab === 'switch-detail'"
          style="padding: 20px 56px; overflow: scroll; height: 600px"
          class="modal-body"
        >
          <tooling-detail
            :show-file-previewer="showFilePreviewer"
            :resources="resources"
            :mold="mold"
            :user-type="userType"
          ></tooling-detail>
        </div>
        <div
          v-if="isTemBar"
          id="legend-chart-temp"
          style="
            position: absolute;
            bottom: 30px;
            width: 100%;
            left: 0;
            padding: 10px 0 25px;
            background: rgb(255, 255, 255);
            height: 50px;
          "
        ></div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * @param {Number|String} val
 * @param {Number} digitNumber
 * @returns {String}
 */
const formatToDecimal = (val, digitNumber = 0) => {
  return Number(val).toFixed(digitNumber);
};

/**
 * @param {Number|String} val
 * @returns {String}
 */
const appendThousandSeperator = (val, separator = ",") => {
  return val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, separator);
};

const generateTooltipHTML = ({
  xLabel,
  iconCode,
  title,
  dataValue,
  avgCavityTitle,
  totalPartProducedTitle = undefined,
  resetLabel,
}) => `
 <div class="tooltips-container">
        <div style="font-weight: bold;">${xLabel}</div>
        <div class="tooltips-title">
            ${iconCode} ${title} ${dataValue}
        </div>
        <div style="margin-left: 16px;">${avgCavityTitle}</div>
        ${
          totalPartProducedTitle
            ? `<div style="margin-left: 16px;">${totalPartProducedTitle}</div>`
            : ""
        }
        ${resetLabel ? `<div>${resetLabel}</div>` : ""}
    </div>
    <table class="table"> </table>
  </div>
`;

module.exports = {
  props: {
    resources: Object,
    showFilePreviewer: Function,
    userType: String,
  },
  components: {
    "tooling-detail": httpVueLoader("/components/tooling-detail.vue"),
    "date-picker": httpVueLoader("/components/mold-popup/date-picker.vue"),
    "data-filter": httpVueLoader("/components/dfilter/data-filter.vue"),
    "bubble-chart": httpVueLoader("/components/amchart5/BubbleChart.vue"),
  },
  data() {
    return {
      accelerationList: [],
      startDate: moment(),
      bubbleChartComponentKey: 0,
      amchartData: {},
      graphSelectedOptions: ["QUANTITY"],
      shotSelected: [],
      shotDefaulted: [],
      cycleTimeSelected: [],
      cycleTimeDefaulted: [],
      UptimeSelected: [],
      UptimeDefaulted: [],
      datePickerType: "DAILY",
      currentDateData: null,
      chartType: "QUANTITY",
      mold: {},
      optimalCycleTime: "Approved CT",
      parts: [],
      isShowDateWeekMonth: true,
      isShowNoteWeek: false,
      isShowNoteMonth: false,
      part: {},
      requestParam: {
        year: moment().format("YYYY"),
        month: moment().format("MM"),
        chartDataType: "QUANTITY",
        dateViewType: "DAY",
        moldId: "",

        // details
        detailDate: "",
        sort: "id,asc",
        size: "50",
      },
      yearSelected: moment().format("YYYY"),
      // details
      results: [],
      // isAbbGermany: false,
      total: 0,
      pagination: [],
      month: 1,
      day: 1,
      hour: "00",
      responseDataChart: null,
      rangeYear: [],
      rangeMonth: [],
      rangeDay: [],
      hourlyChartManager: null,

      //for hour
      monthHour: "01",
      monthSelected: "01",
      isShowFrequentlyChart: false,
      frequentlyChartData: [],
      frequentlyChartDataIndex: 0,
      analysisChartData: [],
      xAxisTitleMapping: {
        HOUR: "Hour",
        DAY: "Date",
        WEEK: "Week",
        MONTH: "Month",
      },
      yAxisTitleMapping: {
        QUANTITY: "Shot",
        CYCLE_TIME: "Cycle Time",
        UPTIME: "Uptime",
        ACCELERATION: "Acceleration",
      },
      // for detail
      xAxisDetailTitleMapping: {
        CYCLE_TIME_ANALYSIS: "Cycle Time",
        TEMPERATURE_ANALYSIS: "Time",
      },
      yAxisDetailTitleMapping: {
        CYCLE_TIME_ANALYSIS: "Quantity",
        TEMPERATURE_ANALYSIS: "Temperature",
      },
      isShowTemperatureColumn: false,
      switchTab: "switch-graph",
      partChangeData: "",
      loadingPartChange: true,
      numberOfPart: 0,
      singlePartName: "",
      hourlyDate: 0,
      isShowGraphDropDown: true,
      isShowChart: false,
      minDate: null,
      maxDate: null,
      isSelectedCustomRange: false,
      chartTypeConfig: {},
      chartTemperature: {},
      shotImg: "",
      cycleTimeImg: "",
      uptimeImg: "",
      customRangeData: {
        allData: [],
        page: 0,
      },
      noDataAvailable: false,
      chart: null,
      frequentlyChart: null,
    };
  },
  methods: {
    getAcceleration() {
      console.log("startDate: ", this.startDate);

      let endDateTimeStamp = this.startDate.unix() + 3600;

      let startDate = Number(this.startDate.format("YYYYMMDDHHmmss"));
      let endDate = Number(
        moment.unix(endDateTimeStamp).format("YYYYMMDDHHmmss")
      );

      console.log("startDate: ", startDate);
      console.log("endDate: ", endDate);
      console.log("startDate: ", Math.floor(startDate / 10000) * 10000);
      console.log("endDate: ", Math.floor(endDate / 10000) * 10000);
      console.log("mold id: ", this.mold.id);

      // todo: get date
      let moldId = `moldId=${this.mold.id}`;
      let fromDateStr = `&fromDateStr=${Math.floor(startDate / 10000) * 10000}`;
      let toDateStr = `&toDateStr=${Math.floor(endDate / 10000) * 10000}`;
      let param = moldId + fromDateStr + toDateStr;

      axios
        .get("/api/analysis/trd-sen/acceleration-chart?" + param)
        .then((res) => {
          this.amchartData = res.data;
          console.log("axios complete amchartData: ", this.amchartData);
          this.bubbleChartComponentKey += 1;
        });
    },
    convertChartType() {
      let type = null;
      switch (this.chartType) {
        case "QUANTITY":
          type = "SHOT";
          break;
        case "PART_QUANTITY":
          type = "PART_QUANTITY";
          break;
        case "CYCLE_TIME":
          type = "CYCLE_TIME";
          break;
        case "CYCLE_TIME_ANALYSIS":
          type = "CYCLE_TIME";
          break;
        case "UPTIME":
          type = "UPTIME";
          break;
        case "TEMPERATURE_ANALYSIS":
          type = "TEMPERATURE";
          break;
      }
      return type;
    },
    getCurrentChartSettings() {
      const type = this.convertChartType();
      axios
        .get(`/api/config/graph-settings-config?chartDataType=${type}`)
        .then((res) => {
          for (let i = 0; i < res.data.data.length; i++) {
            let find = (element) =>
              element.code === res.data.data[i].graphItemType;
            switch (res.data.data[i].chartDataType) {
              case "SHOT":
                this.shotSelected[this.shotSelected.findIndex(find)].selected =
                  res.data.data[i].selected;
                break;
              case "PART_QUANTITY":
                break;
              case "CYCLE_TIME":
                this.cycleTimeSelected[
                  this.cycleTimeSelected.findIndex(find)
                ].selected = res.data.data[i].selected;
                break;
              case "UPTIME":
                this.UptimeSelected[
                  this.UptimeSelected.findIndex(find)
                ].selected = res.data.data[i].selected;
                break;
              case "TEMPERATURE":
                break;
            }
          }
          let settings = { type: this.chartType, data: [] };
          if (this.chartType === "QUANTITY") {
            settings.data = this.shotSelected.filter((item) => item.selected);
            this.filterQueryFunc(settings);
          } else if (this.chartType === "CYCLE_TIME") {
            settings.data = this.cycleTimeSelected.filter(
              (item) => item.selected
            );
            this.filterQueryFunc(settings);
          } else if (this.chartType === "UPTIME") {
            settings.data = this.UptimeSelected.filter((item) => item.selected);
            this.filterQueryFunc(settings);
          }
        });
    },
    updateCurrentChartSettings(data) {
      if (data.data.length > 0) {
        let shotSelected = [
          {
            id: 0,
            name: this.optimalCycleTime,
            code: "APPROVED_CYCLE_TIME",
            icon: "/images/graph/approve-icon.svg",
            selected: false,
          },
          {
            id: 1,
            name: "Cycle Time (Within, L1, L2)",
            code: "CYCLE_TIME",
            icon: this.cycleTimeImg,
            selected: false,
          },
          {
            id: 2,
            name: "Max CT",
            code: "MAX_CYCLE_TIME",
            icon: "/images/graph/max-icon.svg",
            selected: false,
          },
          {
            id: 3,
            name: "Min CT",
            code: "MIN_CYCLE_TIME",
            icon: "/images/graph/min-icon.svg",
            selected: false,
          },
          {
            id: 4,
            name: "Shot",
            code: "QUANTITY",
            default: true,
            icon: this.shotImg,
            selected: true,
          },
        ];
        let cycleTimeSelected = [
          {
            id: 0,
            name: this.optimalCycleTime,
            code: "APPROVED_CYCLE_TIME",
            // default: true,
            icon: "/images/graph/approve-icon.svg",
            selected: false,
          },
          {
            id: 1,
            name: "Cycle Time (Within, L1, L2)",
            code: "CYCLE_TIME",
            default: true,
            icon: this.cycleTimeImg,
            selected: true,
          },
          {
            id: 2,
            name: "Max CT",
            code: "MAX_CYCLE_TIME",
            // default: true,
            icon: "/images/graph/max-icon.svg",
            selected: false,
          },
          {
            id: 3,
            name: "Min CT",
            code: "MIN_CYCLE_TIME",
            // default: true,
            icon: "/images/graph/min-icon.svg",
            selected: false,
          },
          {
            id: 4,
            name: "Shot",
            code: "QUANTITY",
            icon: this.shotImg,
            selected: false,
          },
        ];
        let UptimeSelected = [
          {
            id: 5,
            name: "Uptime (Within, L1, L2)",
            code: "UPTIME",
            default: true,
            icon: this.uptimeImg,
            selected: true,
          },
          {
            id: 6,
            name: "Uptime Target",
            code: "UPTIME_TARGET",
            // default: true,
            icon: "/images/graph/approve-icon.svg",
            selected: false,
          },
          {
            id: 4,
            // name: "Shot",
            name: "Shot",
            code: "QUANTITY",
            icon: this.shotImg,
            selected: false,
          },
        ];
        for (let i = 0; i < data.data.length; i++) {
          let find = (element) => element.code === data.data[i].code;
          switch (this.convertChartType()) {
            case "SHOT":
              shotSelected[shotSelected.findIndex(find)].selected = true;
              break;
            case "CYCLE_TIME":
              cycleTimeSelected[
                cycleTimeSelected.findIndex(find)
              ].selected = true;
              break;
            case "UPTIME":
              UptimeSelected[UptimeSelected.findIndex(find)].selected = true;
              break;
          }
        }
        const self = this;
        setTimeout(() => {
          let params = [];
          switch (self.convertChartType()) {
            case "SHOT":
              params = shotSelected.map((item) => {
                return {
                  chartDataType: "SHOT",
                  graphItemType: item.code,
                  selected: item.selected,
                };
              });
              break;
            case "CYCLE_TIME":
              params = cycleTimeSelected.map((item) => {
                return {
                  chartDataType: "CYCLE_TIME",
                  graphItemType: item.code,
                  selected: item.selected,
                };
              });
              break;
            case "UPTIME":
              params = UptimeSelected.map((item) => {
                return {
                  chartDataType: "UPTIME",
                  graphItemType: item.code,
                  selected: item.selected,
                };
              });
              break;
          }
          axios
            .post("/api/config/graph-settings-config", params)
            .then((res) => {
              switch (self.convertChartType()) {
                case "SHOT":
                  this.shotSelected = shotSelected;
                  break;
                case "CYCLE_TIME":
                  this.cycleTimeSelected = cycleTimeSelected;
                  break;
                case "UPTIME":
                  this.UptimeSelected = UptimeSelected;
                  break;
              }
            });
        }, 300);
      }
    },
    backFromPreview() {
      $(this.getRootToCurrentId()).modal("show");
    },
    getChartTypeConfig() {
      axios
        .get(`/api/common/dsp-stp?moldId=${this.mold?.id || ""}`)
        .then((res) => {
          this.chartTypeConfig = res.data.chartTypeConfig;
          this.accelerationList = this.chartTypeConfig.filter(
            (item) => item.chartDataType === "ACCELERATION"
          );

          this.shotImg =
            this.chartTypeConfig.filter(
              (item) => item.chartDataType === "SHOT"
            )[0].chartType === "LINE"
              ? "/images/graph/shot-icon.svg"
              : "/images/graph/graph-setting-rec.svg";
          this.cycleTimeImg =
            this.chartTypeConfig.filter(
              (item) => item.chartDataType === "CYCLE_TIME"
            )[0].chartType === "LINE"
              ? "/images/graph/graph-setting-line.svg"
              : "/images/graph/three-bar.svg";
          this.uptimeImg =
            this.chartTypeConfig.filter(
              (item) => item.chartDataType === "UPTIME"
            )[0].chartType === "LINE"
              ? "/images/graph/graph-setting-line.svg"
              : "/images/graph/three-bar.svg";
        });
    },
    // toPascalCase 함수 추가 (DAY => Day 차트 표시용)
    toPascalCase(param) {
      // lowercase 처리
      let lowerParam = param.toLowerCase();
      // 첫번째 글자만 대문자
      let result = lowerParam.replace(/^./, lowerParam[0].toUpperCase());
      return result;
    },

    // 그래프 드롭다운 설정
    findMapData(
      zoom,
      country,
      locationId,
      isFromDropdown,
      locationString,
      isOther
    ) {},
    filterQueryFunc(dataCheckList) {
      switch (this.chartType) {
        case "QUANTITY":
          this.shotDefaulted = dataCheckList.data;
          this.graphSelectedOptions = dataCheckList.data.map((g) => g.code);
          break;
        case "CYCLE_TIME":
          this.cycleTimeDefaulted = dataCheckList.data;
          this.graphSelectedOptions = dataCheckList.data.map((g) => g.code);
          break;
        case "UPTIME":
          this.UptimeDefaulted = dataCheckList.data;
          this.graphSelectedOptions = dataCheckList.data.map((g) => g.code);
      }
      const chartDataOptions = [];
      if (this.graphSelectedOptions.includes("QUANTITY")) {
        chartDataOptions.push("QUANTITY");
      }
      if (
        this.graphSelectedOptions.includes("CYCLE_TIME") ||
        this.graphSelectedOptions.includes("MAX_CYCLE_TIME") ||
        this.graphSelectedOptions.includes("MIN_CYCLE_TIME")
      ) {
        chartDataOptions.push("CYCLE_TIME");
      }
      if (
        this.graphSelectedOptions.includes("UPTIME") ||
        this.graphSelectedOptions.includes("UPTIME_TARGET")
      ) {
        chartDataOptions.push("UPTIME");
      }
      this.requestParam.moldId = this.mold.id;
      this.requestParam.chartDataType = chartDataOptions;
      if (!this.isSelectedCustomRange) {
        delete this.requestParam.startDate;
        delete this.requestParam.endDate;
      }

      const param = this.getParam(this.requestParam);

      axios.get(`/api/chart/molds?` + param).then((response) => {
        let dataShow = this.getDataShow(response.data, 0);
        this.bindAllChartData(dataShow);
        // this.bindAllChartData(response.data);
      });
    },
    convertDate(time) {
      return moment(time * 1000).format("YYYY-MM-DD HH:mm");
    },
    changeSwitchTab(id) {
      const el = document.querySelector(this.getRootToCurrentId() + "#" + id);
      el.classList.add("primary-animation-switch");
      setTimeout(() => {
        this.switchTab = id;
        el.classList.remove("primary-animation-switch");
      }, 700);
    },
    // isNCMCounter() {
    //   return this.mold?.counterCode?.slice(0, 3) === "NCM";
    // },
    isNotCMSCounter() {
      return this.mold?.counterCode?.slice(0, 3) !== "CMS";
    },
    isNotNewGenCounter() {
      return this.mold?.counterCode?.slice(0, 3) !== "NCM";
    },
    // async isAbb() {
    //   try {
    //     const server = await Common.getSystem('server')
    //     this.isAbbGermany = server === 'abb-pilot'
    //   } catch (error) {
    //     console.log(error)
    //   }
    // },
    displayYears: function () {
      let years = [];
      if (this.rangeYear.length > 0) {
        for (let i = this.rangeYear[0]; i <= this.rangeYear[1]; i++) {
          years.push(i);
        }
      }
      return years;
    },
    displayMonths() {
      let months = [];
      const { yearSelected, rangeYear, rangeMonth } = this;

      /*
            for (let i = 1; i <= 12; i++) {
              months.push(i < 10 ? `0${i}` : i);
            }
      */
      if (rangeYear[0] !== rangeYear[1]) {
        if (Number(yearSelected) === Number(rangeYear[1])) {
          for (let i = 1; i <= rangeMonth[1]; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        } else if (Number(yearSelected) === Number(rangeYear[0])) {
          for (let i = rangeMonth[0]; i <= 12; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        } else {
          for (let i = 1; i <= 12; i++) {
            months.push(i < 10 ? `0${i}` : i);
          }
        }
      } else if (rangeYear.length > 0) {
        for (let i = rangeMonth[0]; i <= rangeMonth[1]; i++) {
          months.push(i < 10 ? `0${i}` : i);
        }
      }
      return months;
    },
    displayDays() {
      const { monthHour, yearSelected, rangeYear, rangeMonth, rangeDay } = this;
      const month = parseInt(monthHour);
      const year = parseInt(yearSelected);
      const dayOfMonth = this.getDaysOfMonth(month, year);
      let days = [];
      if (month === rangeMonth[0] && year === rangeYear[0]) {
        // reach start time
        for (let day = rangeDay[0]; day <= dayOfMonth; day++) {
          days.push(day);
        }
      } else if (month === rangeMonth[1] && year === rangeYear[1]) {
        // reach end time
        for (let day = 1; day <= rangeDay[1]; day++) {
          days.push(day);
        }
      } else {
        // between
        for (let day = 1; day <= dayOfMonth; day++) {
          days.push(day);
        }
      }
      return days;
    },
    getHour() {
      return [
        "00",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
      ];
    },
    getDaysOfMonth(month, year) {
      return month === 2
        ? year % 4
          ? 28
          : year % 100
          ? 29
          : year % 400
          ? 28
          : 29
        : ((month - 1) % 7) % 2
        ? 30
        : 31;
    },
    updateChartMoldLegend() {
      $(this.getRootToCurrentId() + "#legend-chart-mold").html(
        this.chart.generateLegend()
      );
    },
    updateChartTempLegend() {
      $(this.getRootToCurrentId() + "#legend-chart-temp").html(
        this.chartTemperature.generateLegend()
      );
    },
    updateChartFrequentlyLegend() {
      let self = this;
      let frequentlyHtmlItem = self.frequentlyChart
        .generateLegend()
        .replace(/legend-item/g, "legend-frequently-item");
      $(this.getRootToCurrentId() + "#legend-chart-frequently").html(
        frequentlyHtmlItem
      );

      // set click frequently legend event
      for (let i = 0; i < self.frequentlyChart.data.datasets.length; i++) {
        $(this.getRootToCurrentId() + "#legend-frequently-item-" + i).on(
          "click",
          () => {
            let meta = self.frequentlyChart.getDatasetMeta(i);
            if (meta.hidden) {
              // show if is hiding
              meta.hidden = null;
              $(this).removeClass("removed");
            } else {
              // hide if is not hiding
              meta.hidden = true;
              $(this).addClass("removed");
            }
            self.frequentlyChart.update();
          }
        );
      }
    },
    changeMonthOrDay(type) {
      if (this.isSelectedCustomRange) {
        if (type === "PRE") {
          this.customRangeData.page--;
        } else {
          this.customRangeData.page++;
        }
        let dataShow = this.getDataShow(
          this.customRangeData.allData,
          this.customRangeData.page
        );
        this.bindAllChartData(dataShow);
        return;
      }
      const dpicker = Common.vue.getChild(this.$children, "date-picker");
      if (type === "PRE") {
        dpicker.previous();
      } else {
        //NEXT
        dpicker.next();
      }
    },
    hideHourlyChart() {
      $(this.getRootToCurrentId() + "#hourly-chart-wrapper").hide();
    },
    resetHourlyChart() {
      // reset analysis chart data
      if (this.requestParam.chartDataType === "CYCLE_TIME_ANALYSIS") {
        this.bindCycleTimeAnalysisChartData([]);
      } else {
        this.bindTemperatureBarChart([]);
        this.bindTemperatureAnalysisChartData([]);
        // if (this.isTemBar) {
        //   this.bindTemperatureBarChart([]);
        // } else {
        //   this.bindTemperatureAnalysisChartData([]);
        // }
        // this.bindTemperatureAnalysisChartData([]);
      }
    },
    disableNext: function () {
      const {
        rangeYear,
        rangeMonth,
        monthHour,
        yearSelected,
        requestParam,
        day,
        rangeDay,
      } = this;
      const endDayOfMonth = moment(`${yearSelected}/${monthHour}`, "YYYY/MM")
        .endOf("month")
        .format("DD");
      const startDayOfMonth = moment(`${yearSelected}/${monthHour}`, "YYYY/MM")
        .startOf("month")
        .format("DD");
      if (requestParam.dateViewType == "HOUR") {
        if (rangeYear.length > 0 && rangeMonth.length > 0) {
          if (
            Number(yearSelected) >= rangeYear[1] &&
            Number(monthHour) >= rangeMonth[1] &&
            Number(day) >= Number(rangeDay[1])
          ) {
            return true;
          }
        }
        return false;
      }
      if (rangeYear.length > 0 && rangeMonth.length > 0) {
        return (
          (this.month >= Number(rangeMonth[1]) &&
            Number(this.yearSelected) == Number(rangeYear[1])) ||
          Number(this.yearSelected) > Number(rangeYear[1])
        );
      }
      return false;
    },
    disablePre: function () {
      const {
        rangeYear,
        rangeMonth,
        monthHour,
        yearSelected,
        requestParam,
        day,
        rangeDay,
      } = this;
      const endDayOfMonth = moment(`${yearSelected}/${monthHour}`, "YYYY/MM")
        .endOf("month")
        .format("DD");
      const startDayOfMonth = moment(`${yearSelected}/${monthHour}`, "YYYY/MM")
        .startOf("month")
        .format("DD");
      if (requestParam.dateViewType == "HOUR") {
        if (rangeYear.length > 0 && rangeMonth.length > 0) {
          if (
            Number(yearSelected) <= Number(rangeYear[0]) &&
            Number(monthHour) <= Number(rangeMonth[0]) &&
            Number(day) <= Number(rangeDay[0])
          ) {
            return true;
          }
        }

        return false;
      }

      if (rangeYear.length > 0 && rangeMonth.length > 0) {
        return (
          (this.month <= Number(rangeMonth[0]) &&
            Number(this.yearSelected) == Number(rangeYear[0])) ||
          Number(this.yearSelected) < Number(rangeYear[0])
        );
      }
      return false;
    },
    disablePreFrequently: function () {
      return (
        this.frequentlyChartData.length === 0 ||
        this.frequentlyChartDataIndex === 0
      );
    },
    disableNextFrequently: function () {
      return (
        this.frequentlyChartData.length === 0 ||
        this.frequentlyChartDataIndex === this.frequentlyChartData.length - 1
      );
    },
    changeFrequentlyChart: function (type) {
      if (type === "PRE" && !this.disablePreFrequently()) {
        this.frequentlyChartDataIndex--;
        this.bindFrequentlyChart(
          this.frequentlyChartData[this.frequentlyChartDataIndex]
        );
      } else if (type === "NEXT" && !this.disableNextFrequently()) {
        this.frequentlyChartDataIndex++;
        this.bindFrequentlyChart(
          this.frequentlyChartData[this.frequentlyChartDataIndex]
        );
      }
    },
    initSelected() {
      // Shot Graph Options
      this.shotSelected = [
        {
          id: 0,
          name: this.optimalCycleTime,
          code: "APPROVED_CYCLE_TIME",
          icon: "/images/graph/approve-icon.svg",
          selected: false,
        },
        {
          id: 1,
          name: "Cycle Time (Within, L1, L2)",
          code: "CYCLE_TIME",
          icon: this.cycleTimeImg,
          selected: false,
        },
        {
          id: 2,
          name: "Max CT",
          code: "MAX_CYCLE_TIME",
          icon: "/images/graph/max-icon.svg",
          selected: false,
        },
        {
          id: 3,
          name: "Min CT",
          code: "MIN_CYCLE_TIME",
          icon: "/images/graph/min-icon.svg",
          selected: false,
        },
        {
          id: 4,
          name: "Shot",
          code: "QUANTITY",
          default: true,
          icon: this.shotImg,
          selected: true,
        },
      ];

      // Cycle Time Graph Options
      this.cycleTimeSelected = [
        {
          id: 0,
          name: this.optimalCycleTime,
          code: "APPROVED_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/approve-icon.svg",
          selected: false,
        },
        {
          id: 1,
          name: "Cycle Time (Within, L1, L2)",
          code: "CYCLE_TIME",
          default: true,
          icon: this.cycleTimeImg,
          selected: true,
        },
        {
          id: 2,
          name: "Max CT",
          code: "MAX_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/max-icon.svg",
          selected: false,
        },
        {
          id: 3,
          name: "Min CT",
          code: "MIN_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/min-icon.svg",
          selected: false,
        },
        {
          id: 4,
          name: "Shot",
          code: "QUANTITY",
          icon: this.shotImg,
          selected: false,
        },
      ];

      // Uptime Graph Options
      this.UptimeSelected = [
        {
          id: 5,
          name: "Uptime (Within, L1, L2)",
          code: "UPTIME",
          default: true,
          icon: this.uptimeImg,
          selected: true,
        },
        {
          id: 6,
          name: "Uptime Target",
          code: "UPTIME_TARGET",
          // default: true,
          icon: "/images/graph/approve-icon.svg",
          selected: false,
        },
        {
          id: 4,
          // name: "Shot",
          name: "Shot",
          code: "QUANTITY",
          icon: this.shotImg,
          selected: false,
        },
      ];
    },
    async showChart(mold, dataType, dateViewType, tab, oct) {
      // this.getChartTypeConfig();
      let toolingData = await this.getMoldById(mold.id);
      this.isShowChart = false;
      this.mold = { mold, toolingData };
      if (oct) this.optimalCycleTime = oct;
      this.initSelected();
      // this.getCurrentChartSettings();
      this.switchTab = "switch-graph";

      if (mold && mold.id) {
        var child = Common.vue.getChild(this.$children, "tooling-detail");
        if (child != null) {
          child.showToolingDetail(mold);
        }
      }

      this.requestParam = {
        year: moment().format("YYYY"),
        chartDataType: "QUANTITY",
        dateViewType: dateViewType,
        moldId: "",
        detailDate: "",
        sort: "id,asc",
        size: "50",
      };
      this.isShowDateWeekMonth = true;
      if (!this.mold.uptimeTarget) {
        this.mold.uptimeTarget = 90;
      }
      this.shotDefaulted = [
        {
          id: 4,
          name: "Shot",
          code: "QUANTITY",
          default: true,
          icon: this.shotImg,
        },
      ];
      this.cycleTimeDefaulted = [
        {
          id: 0,
          name: this.optimalCycleTime,
          code: "APPROVED_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/approve-icon.svg",
        },
        {
          id: 1,
          name: "Cycle Time (Within, L1, L2)",
          code: "CYCLE_TIME",
          default: true,
          icon: this.cycleTimeImg,
        },
        {
          id: 2,
          name: "Max CT",
          code: "MAX_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/max-icon.svg",
        },
        {
          id: 3,
          name: "Min CT",
          code: "MIN_CYCLE_TIME",
          // default: true,
          icon: "/images/graph/min-icon.svg",
        },
      ];
      this.UptimeDefaulted = [
        {
          id: 5,
          name: "Uptime (Within, L1, L2)",
          code: "UPTIME",
          default: true,
          icon: this.uptimeImg,
        },
        {
          id: 6,
          name: "Uptime Target",
          code: "UPTIME_TARGET",
          // default: true,
          icon: "/images/graph/approve-icon.svg",
        },
      ];

      this.rangeYear = [];
      this.rangeMonth = [];
      this.yearSelected = moment().format("YYYY");
      if (dateViewType === "HOUR" && mold.counterCode.slice(0, 3) !== "NCM") {
        this.requestParam.dateViewType = "DAY";
      }
      axios
        .get(`/api/statistics/time-range?moldId=${mold.id}`)
        .then((response) => {
          const { data } = response;
          this.rangeYear = data.map((value) => {
            return value.year;
          });
          this.yearSelected = this.rangeYear[1];
          this.requestParam.year = this.rangeYear[1];
          this.rangeMonth = data.map((value) => {
            return value.month;
          });
          this.month = this.rangeMonth[1];
          this.requestParam.month = this.rangeMonth[1]; // 초반 데이터 불러올 시 달(개월) param 추가
          this.monthHour =
            this.rangeMonth[1] < 10
              ? `0${this.rangeMonth[1]}`
              : this.rangeMonth[1];
          this.monthSelected =
            this.rangeMonth[1] < 10
              ? `0${this.rangeMonth[1]}`
              : this.rangeMonth[1];
          this.rangeDay = data.map((value) => {
            return value.day;
          });
          this.day = this.rangeDay[1];
          this.chartType = "QUANTITY";
          this.graphSelectedOptions = this.shotDefaulted.map((g) => g.code);

          const chartDataOptions = ["QUANTITY"];
          this.requestParam.moldId = this.mold.id;
          this.requestParam.chartDataType = chartDataOptions;
          this.requestParam.dateViewType = dateViewType;
          this.minDate = new Date(
            this.rangeYear[0],
            this.rangeMonth[0] - 1,
            this.rangeDay[0]
          );
          this.maxDate = new Date(
            this.rangeYear[1],
            this.rangeMonth[1] - 1,
            this.rangeDay[1]
          );
          // this.bindChartData();
          this.getCurrentChartSettings();

          this.isShowChart = true;
        });

      this.findMoldParts();
      // $("#op-daily-details").hide();
      $(this.getRootToCurrentId()).modal("show");

      this.getChartTypeConfig();
    },
    changeChartDataType(dataType) {
      this.isSelectedCustomRange = false;
      this.chartType = dataType;
      let oldDateViewType = this.requestParam.dateViewType;
      // this.getCurrentChartSettings();
      switch (this.chartType) {
        case "QUANTITY":
          this.graphSelectedOptions = this.shotDefaulted.map((g) => g.code);
          break;
        case "CYCLE_TIME":
          this.graphSelectedOptions = this.cycleTimeDefaulted.map(
            (g) => g.code
          );
          break;
        case "UPTIME":
          this.graphSelectedOptions = this.UptimeDefaulted.map((g) => g.code);
          break;
        default:
          this.graphSelectedOptions = [this.chartType];
      }
      //start add old logic
      if (
        dataType === "TEMPERATURE_ANALYSIS" ||
        dataType === "CYCLE_TIME_ANALYSIS" ||
        dataType === "ACCELERATION"
      ) {
        this.isShowDateWeekMonth = false;
        this.requestParam.dateViewType = "HOUR";
      } else {
        this.isShowDateWeekMonth = true;
      }
      // $("#op-daily-details").hide();
      if (
        oldDateViewType !== this.requestParam.dateViewType &&
        (dataType === "TEMPERATURE_ANALYSIS" ||
          dataType === "CYCLE_TIME_ANALYSIS")
      ) {
        var child = Common.vue.getChild(this.$children, "date-picker");
        if (child != null) {
          child.changeType("HOURLY");
        }
      }
      if (dataType === "UPTIME" && this.requestParam.dateViewType === "HOUR") {
        var child = Common.vue.getChild(this.$children, "date-picker");
        if (child != null) {
          child.changeType("DAILY");
        }
        // this.dateViewType('DAY');
        this.requestParam.dateViewType = "DAY";
        // return;
      }
      //end add old logic
      if (this.requestParam.dateViewType == "HOUR") {
        // if(this.requestParam.day==null){
        //     this.requestParam.day = ; // 초반 데이터 불러올 시 달(개월) param 추가
        // }
      }

      if (
        oldDateViewType !== this.requestParam.dateViewType &&
        this.requestParam.dateViewType == "DAY"
      ) {
        delete this.requestParam.day;
        if (this.requestParam.month == null) {
          this.requestParam.month = this.rangeMonth[1]; // 초반 데이터 불러올 시 달(개월) param 추가
        }
      }
      if (
        this.requestParam.dateViewType == "WEEK" ||
        this.requestParam.dateViewType == "MONTH"
      ) {
        delete this.requestParam.month;
        delete this.requestParam.day;
      }

      const chartDataOptions = [];
      if (this.graphSelectedOptions.includes("QUANTITY")) {
        chartDataOptions.push("QUANTITY");
      }
      if (
        this.graphSelectedOptions.includes("CYCLE_TIME") ||
        this.graphSelectedOptions.includes("MAX_CYCLE_TIME") ||
        this.graphSelectedOptions.includes("MIN_CYCLE_TIME")
      ) {
        chartDataOptions.push("CYCLE_TIME");
      }
      if (
        this.graphSelectedOptions.includes("UPTIME") ||
        this.graphSelectedOptions.includes("UPTIME_TARGET")
      ) {
        chartDataOptions.push("UPTIME");
      }

      switch (this.chartType) {
        case "QUANTITY":
        case "CYCLE_TIME":
        case "UPTIME":
          this.requestParam.chartDataType = chartDataOptions;
          break;
        default:
          this.requestParam.chartDataType = this.graphSelectedOptions;
      }
      if (["QUANTITY", "CYCLE_TIME", "UPTIME"].includes(this.chartType)) {
        this.getCurrentChartSettings();
      } else this.bindChartData(true);
    },
    dateViewType(viewType) {
      this.requestParam.dateViewType = viewType;
      this.bindChartData();
    },
    setDayWhenChangeMonth(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
        }
      }
    },
    isValidMonth() {
      return (
        this.monthHour != null &&
        this.displayMonths()
          .map((m) => Number(m))
          .includes(Number(this.monthHour))
      );
    },
    resetMonth() {
      let months = this.displayMonths();
      if (months.length > 0) {
        this.monthHour = months[0];
      }
    },
    getDataShow(allData, pageIndex) {
      if (this.isSelectedCustomRange) {
        this.customRangeData.allData = allData;
        this.customRangeData.page = pageIndex;
        let indexStart = Common.MAX_VIEW_POINT * pageIndex;
        let indexEnd = Math.min(
          Common.MAX_VIEW_POINT * pageIndex + Common.MAX_VIEW_POINT,
          allData.length
        );
        if (allData.length) {
          return allData.slice(indexStart, indexEnd);
        }
      } else {
        this.customRangeData = {
          allData: [],
          page: 0,
        };
      }
      return allData;
    },
    bindAllChartData(dataShow) {
      console.log("Run there!", dataShow);
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      config.data.labels = [];
      config.data.datasets = [];
      this.chart.update();
      if (this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }

      this.isShowFrequentlyChart = false;
      this.frequentlyChartData = [];
      this.setDayWhenChangeMonth(this.monthHour);

      if (
        "CYCLE_TIME_ANALYSIS" === this.requestParam.chartDataType ||
        "TEMPERATURE_ANALYSIS" === this.requestParam.chartDataType
      ) {
        $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
        this.resetHourlyChart();
      } else {
        this.hideHourlyChart();
      }

      let shotQuantity = {
        type: "line",
        label: "Shot",
        backgroundColor: "#B1E0EE",
        fill: false,
        borderColor: "#63C2DE",
        pointRadius: 2,
        borderWidth: 2,
        data: [],
        yAxisID: "first-y-axis",
        stack: "shotQuantity",
      };

      let approvedCycleTime = {
        type: "line",
        label: this.optimalCycleTime + " (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--success"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "approvedCycleTime",
      };

      const maxCycleTime = {
        type: "line",
        label: "Max CT (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--warning"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "maxCycleTime",
      };

      const minCycleTime = {
        type: "line",
        label: "Min CT (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--danger"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "minCycleTime",
      };

      let cycleTimeWithin = {
        type: "bar",
        label: "Within (sec)",
        fill: false,
        backgroundColor: "#B1E0EE",
        borderColor: "#63C2DE",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      let cycleTimeL1 = {
        type: "bar",
        label: "L1 (sec)",
        fill: false,
        backgroundColor: "#FFE083",
        borderColor: "#FFC107",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      let cycleTimeL2 = {
        type: "bar",
        label: "L2 (sec)",
        fill: false,
        backgroundColor: "#FBB5B5",
        borderColor: "#F86C6B",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "cycleTime",
      };

      const uptimeTarget = {
        type: "line",
        fill: false,
        label:
          "MONTH" === this.requestParam.dateViewType ||
          "WEEK" === this.requestParam.dateViewType
            ? "Uptime Limit (hour)"
            : "Uptime Target (" + this.mold.uptimeTarget + "%)",
        backgroundColor: hexToRgba(getStyle("--success"), 0),
        borderColor: getStyle("--success"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptimeTarget",
      };

      let uptimeWithin = {
        type: "bar",
        label: "Within",
        fill: false,
        backgroundColor: "#B1E0EE",
        borderColor: "#63C2DE",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };
      let uptimeL1 = {
        type: "bar",
        label: "L1",
        fill: false,
        backgroundColor: "#FFE083",
        borderColor: "#FFC107",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };
      let uptimeL2 = {
        type: "bar",
        label: "L2",
        fill: false,
        backgroundColor: "#FBB5B5",
        borderColor: "#F86C6B",
        borderWidth: 2,
        data: [],
        // yAxisID: "first-y-axis",
        yAxisID: "second-y-axis",
        stack: "uptime",
      };

      shotQuantity.type =
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
          ? "line"
          : "bar";
      cycleTimeWithin.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";
      cycleTimeL1.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";
      cycleTimeL2.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "CYCLE_TIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";
      uptimeWithin.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "UPTIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";
      uptimeL1.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "UPTIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";
      uptimeL2.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "UPTIME"
        )[0].chartType === "LINE"
          ? "line"
          : "bar";

      let contranctedCycleTime = 0;

      const data = dataShow;

      for (let i = 0; i < data.length; i++) {
        let title = data[i].title.replace(this.requestParam.year, "W-");

        if (this.isSelectedCustomRange) {
          if ("DAY" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(6);
          } else if ("MONTH" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(4);
          } else if ("HOUR" == this.requestParam.dateViewType) {
            title = moment(data[i].title, "YYYYMMDDHH").format("HH");
          } else if ("WEEK" == this.requestParam.dateViewType) {
            let year = data[i].title.substring(0, 4);
            let week = data[i].title.replace(year, "");
            title = `W${week}, ${year}`;
            console.log(`check week data - ${i}`, data[i].title);
          }
        } else {
          if ("DAY" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(2, 4);
          } else if ("MONTH" === this.requestParam.dateViewType) {
            title = title.replace("W-", "").substring(0, 2);
          } else if ("HOUR" == this.requestParam.dateViewType) {
            title = moment(data[i].title, "YYYYMMDDHH").format("HH");
          } else if ("WEEK" == this.requestParam.dateViewType) {
            let year = data[i].title.substring(0, 4);
            let week = data[i].title.replace(year, "");
            title = `W${week}, ${year}`;
            console.log(`check week data - ${i}`, data[i].title);
          }
        }

        if (data[i].contractedCycleTime > 0 && contranctedCycleTime === 0) {
          contranctedCycleTime = data[i].contractedCycleTime;
          if (
            contranctedCycleTime &&
            !isNaN(contranctedCycleTime) &&
            contranctedCycleTime % 1 !== 0
          )
            contranctedCycleTime = Math.round(contranctedCycleTime * 100) / 100;
        }

        config.data.labels.push(title);
        if (this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")) {
          approvedCycleTime.data.push(contranctedCycleTime);
        }
        if (this.graphSelectedOptions.includes("MAX_CYCLE_TIME")) {
          maxCycleTime.data.push(data[i].maxCycleTime);
        }
        if (this.graphSelectedOptions.includes("MIN_CYCLE_TIME")) {
          minCycleTime.data.push(data[i].minCycleTime);
        }
        if (this.graphSelectedOptions.includes("CYCLE_TIME")) {
          cycleTimeL1.data.push(data[i].cycleTimeL1);
          cycleTimeL2.data.push(data[i].cycleTimeL2);
          cycleTimeWithin.data.push(data[i].cycleTimeWithin);
        }
        if (this.graphSelectedOptions.includes("QUANTITY")) {
          shotQuantity.data.push(data[i].data);
        }

        if (
          this.graphSelectedOptions.includes("UPTIME") ||
          this.graphSelectedOptions.includes("UPTIME_TARGET")
        ) {
          const ratioL1 = this.mold.uptimeLimitL1;
          const ratioL2 = this.mold.uptimeLimitL2;

          let displayTime = "HOUR";
          let uptime = data[i].uptime;
          let basicTime = 86400;

          if ("HOUR" === displayTime) {
            uptime = data[i].uptimeHour;
            basicTime = 24;
          } else if ("MINUTE" === displayTime) {
            uptime = data[i].uptimeMinute;
            basicTime = 1440;
          }

          if ("WEEK" === this.requestParam.dateViewType) {
            basicTime = basicTime * 7;
          } else if ("MONTH" === this.requestParam.dateViewType) {
            basicTime = basicTime * 30;
          }

          let utarget = basicTime * this.mold.uptimeTarget * 0.01;
          let hourPerShift = 24;
          let shiftsPerDay = 1;
          let productionDays = 7;
          let productionWeeks = 1;

          if (this.mold.hourPerShift && !isNaN(this.mold.hourPerShift)) {
            hourPerShift = this.mold.hourPerShift;
          } else {
            this.isShowNoteWeek = true;
            this.isShowNoteMonth = true;
          }
          if (this.mold.shiftsPerDay && !isNaN(this.mold.shiftsPerDay)) {
            shiftsPerDay = this.mold.shiftsPerDay;
          } else {
            this.isShowNoteWeek = true;
            this.isShowNoteMonth = true;
          }
          if (this.mold.productionDays && !isNaN(this.mold.productionDays)) {
            if (this.mold.productionDays > 7) {
              productionDays = 7;
              this.isShowNoteWeek = true;
            } else {
              productionDays = this.mold.productionDays;
            }
          } else {
            this.isShowNoteWeek = true;
          }

          if ("DAY" === this.requestParam.dateViewType) {
            // !!!: alertuptimeGoal = hourPerShift * shiftsPerDay;
          } else if ("WEEK" === this.requestParam.dateViewType) {
            if (hourPerShift * shiftsPerDay > 24) {
              this.isShowNoteWeek = true;
              utarget = 24 * productionDays * this.mold.uptimeTarget * 0.01;
            } else {
              utarget =
                hourPerShift *
                shiftsPerDay *
                productionDays *
                this.mold.uptimeTarget *
                0.01;
            }
          } else if ("MONTH" === this.requestParam.dateViewType) {
            if (hourPerShift * shiftsPerDay > 24) {
              this.isShowNoteMonth = true;
              utarget = 24 * 30 * this.mold.uptimeTarget * 0.01;
            } else {
              utarget =
                hourPerShift *
                shiftsPerDay *
                30 *
                this.mold.uptimeTarget *
                0.01;
            }
          } else if ("HOUR" == this.requestParam.dateViewType) {
            utarget = 1 * this.mold.uptimeTarget * 0.01;
          }
          let uptime1 = utarget * (ratioL1 / 100);
          let uptime2 = utarget * (ratioL2 / 100);

          let minusL2 = utarget - uptime2;
          let minusL1 = utarget - uptime1;
          let plusL1 = utarget + uptime1;
          let plusL2 = utarget + uptime2;
          let w = 0;
          let l1 = 0;
          let l2 = 0;
          if (uptime < minusL2 || uptime > plusL2) {
            l2 = uptime;
          } else if (
            (minusL2 <= uptime && uptime < minusL1) ||
            (uptime > plusL1 && uptime <= plusL2)
          ) {
            l1 = uptime;
          } else {
            w = uptime;
          }
          if (this.graphSelectedOptions.includes("UPTIME")) {
            uptimeWithin.data.push(w);
            uptimeL1.data.push(l1);
            uptimeL2.data.push(l2);
          }
          if (this.graphSelectedOptions.includes("UPTIME_TARGET")) {
            uptimeTarget.data.push(utarget.toFixed(1));
          }
        }
      }

      if (shotQuantity) {
        let pointConfig = this.getQuantityPointConfig(data);
        shotQuantity.pointStyle = pointConfig.pointStyles;
        shotQuantity.pointRadius = pointConfig.pointRadius;
        shotQuantity.pointHitRadius = pointConfig.pointHoverRadius;
        shotQuantity.pointBackgroundColor = pointConfig.pointBackgroundColor;
        shotQuantity.pointHoverBackgroundColor =
          pointConfig.pointHoverBackgroundColor;
      }
      config.options.tooltips = this.setTooltipForMainChart(
        this.requestParam.dateViewType,
        data
      );

      if (this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")) {
        config.data.datasets.push(approvedCycleTime);
      }
      if (this.graphSelectedOptions.includes("MAX_CYCLE_TIME")) {
        config.data.datasets.push(maxCycleTime);
      }
      if (this.graphSelectedOptions.includes("MIN_CYCLE_TIME")) {
        config.data.datasets.push(minCycleTime);
      }
      if (this.graphSelectedOptions.includes("QUANTITY")) {
        config.data.datasets.push(shotQuantity);
      }
      if (this.graphSelectedOptions.includes("CYCLE_TIME")) {
        config.data.datasets.push(cycleTimeWithin);
        config.data.datasets.push(cycleTimeL1);
        config.data.datasets.push(cycleTimeL2);
      }
      if (this.graphSelectedOptions.includes("UPTIME")) {
        config.data.datasets.push(uptimeWithin);
        config.data.datasets.push(uptimeL1);
        config.data.datasets.push(uptimeL2);
      }
      if (this.graphSelectedOptions.includes("UPTIME_TARGET")) {
        config.data.datasets.push(uptimeTarget);
      }

      if (!dataShow.length) {
        let defaultData = this.getChartDefaultData();
        config.data.labels = defaultData.labels;
      }
      //this.setGraphAxisTitle();
      this.updateChartMoldLegend();
      switch (this.chartType) {
        case "QUANTITY":
          // for cycle time
          config.options.scales.yAxes[1].position = "right";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = false;
          // for shot
          config.options.scales.yAxes[0].position = "left";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
          config.options.scales.yAxes[0].display = true;
          if (
            !this.graphSelectedOptions.includes("CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("MIN_CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("MAX_CYCLE_TIME") &&
            !this.graphSelectedOptions.includes("APPROVED_CYCLE_TIME")
          ) {
            config.options.scales.yAxes[1].scaleLabel.labelString = "";
            config.options.scales.yAxes[1].display = false;
          } else {
            config.options.scales.yAxes[1].scaleLabel.labelString =
              "Cycle Time";
            config.options.scales.yAxes[1].display = true;
          }
          break;
        case "CYCLE_TIME":
          // for shot
          config.options.scales.yAxes[0].position = "right";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = false;
          // for cycle time
          config.options.scales.yAxes[1].scaleLabel.labelString = "Cycle Time";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[1].position = "left";
          config.options.scales.yAxes[1].display = true;
          if (!this.graphSelectedOptions.includes("QUANTITY")) {
            config.options.scales.yAxes[0].scaleLabel.labelString = "";
            config.options.scales.yAxes[0].display = false;
          } else {
            config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
            config.options.scales.yAxes[0].display = true;
          }
          break;
        case "UPTIME":
          // for shot
          config.options.scales.yAxes[0].position = "right";
          config.options.scales.yAxes[0].gridLines.drawOnChartArea = false;
          // for uptime
          config.options.scales.yAxes[1].position = "left";
          config.options.scales.yAxes[1].gridLines.drawOnChartArea = true;
          config.options.scales.yAxes[1].scaleLabel.labelString = "Uptime";
          config.options.scales.yAxes[1].display = true;
          if (!this.graphSelectedOptions.includes("QUANTITY")) {
            config.options.scales.yAxes[0].scaleLabel.labelString = "";
            config.options.scales.yAxes[0].display = false;
          } else {
            config.options.scales.yAxes[0].scaleLabel.labelString = "Shot";
            config.options.scales.yAxes[0].display = true;
          }
          break;
      }
      config.options.scales.xAxes[0].scaleLabel.labelString = this.toPascalCase(
        this.requestParam.dateViewType
      );
      console.log("config.data.datasets:::::::::::::::", config.data.datasets);
      // const isDataAvailable = config.data.datasets[0].data.some(item => !!item)
      const isDataAvailable = config.data.datasets.some((set) =>
        set.data.some((item) => !!item)
      );
      config.options.scales.yAxes[0].gridLines.display = isDataAvailable;
      config.options.scales.yAxes[1].gridLines.display = isDataAvailable;
      this.noDataAvailable = !isDataAvailable;
      this.chart.config.data.datasets = config.data.datasets;
      this.chart.config.data.labels = config.data.labels;
      this.chart.resize();
      this.chart.update();
      this.$forceUpdate();
      console.log("update for chart done!");
    },
    getParam(requestParam) {
      let paramNew = {};
      switch (requestParam.dateViewType) {
        case "HOUR":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: this.requestParam.chartDataType,
            /*
            date:
              this.requestParam.year +
              this.requestParam.month +
              this.requestParam.day,
*/
            date: this.getCurrentDate(),
            moldId: this.requestParam.moldId,
          };
          break;
        case "DAY":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            month: requestParam.month,
            moldId: requestParam.moldId,
          };
          break;
        case "MONTH":
        case "WEEK":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            moldId: requestParam.moldId,
          };
          break;
      }
      if (
        this.isSelectedCustomRange &&
        requestParam.startDate &&
        requestParam.endDate
      ) {
        paramNew = {
          ...paramNew,
          startDate: requestParam.startDate,
          endDate: requestParam.endDate,
        };
      }
      return Common.param(paramNew);
    },
    bindChartData(isChangeTab) {
      let self = this;
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      $(this.getRootToCurrentId() + "#hourly-chart-wrapper").hide();
      config.data.labels = [];
      config.data.datasets = [];
      this.chart.resize();
      this.chart.update();
      if (this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }
      this.isShowFrequentlyChart = false;
      this.frequentlyChartData = [];
      this.setDayWhenChangeMonth(this.monthHour);
      var param = {};

      if (
        "CYCLE_TIME_ANALYSIS" === this.chartType ||
        "TEMPERATURE_ANALYSIS" === this.chartType
      ) {
        // $("#hourly-chart-wrapper").show();
        this.resetHourlyChart();
      } else {
        this.hideHourlyChart();
      }

      let originParam = JSON.parse(JSON.stringify(this.requestParam));

      if (this.requestParam.dateViewType === "HOUR") {
        param = Common.param({
          ...this.requestParam,
          year: this.yearSelected,
          // date: this.hourlyDate || this.getCurrentDate()
          date: this.isSelectedCustomRange ? "" : this.getCurrentDate(),
        });
        originParam.date = this.getCurrentDate();
      } else {
        // ???
        // if ("CYCLE_TIME_ANALYSIS" === this.chartType || "TEMPERATURE_ANALYSIS" === this.chartType) {
        //   this.requestParam.chartDataType = ['QUANTITY'];
        // }
        param = Common.param({
          ...this.requestParam,
          year: this.yearSelected,
        });
      }

      if (this.chartType === "ACCELERATION") {
        this.getAcceleration();
      } else {
        config.options.scales.yAxes[0].ticks.beginAtZero = true;
        this.chart.resize();
        this.chart.update();
        axios
          .get(`/api/chart/molds?` + param)
          .then((response) => {
            if (
              ["CYCLE_TIME_ANALYSIS", "TEMPERATURE_ANALYSIS"].includes(
                this.requestParam.chartDataType
              )
            ) {
              let currentDate = this.getCurrentDate();
              if (originParam.date !== currentDate) {
                return;
              }
            }

            this.responseDataChart = response.data;
            if (this.chartType === "CYCLE_TIME_ANALYSIS") {
              $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
              if (
                this.chartTemperature != null &&
                this.chartTemperature.options
              ) {
                this.chartTemperature.destroy();
              }
              this.bindCycleTimeAnalysisChartData(response.data);
              this.analysisChartData = response.data;
            } else if (this.chartType === "TEMPERATURE_ANALYSIS") {
              $(this.getRootToCurrentId() + "#hourly-chart-wrapper").show();
              if (this.isTemBar) {
                this.bindTemperatureBarChart(response.data);
                this.analysisChartData = response.data;
                this.updateChartTempLegend();
              } else {
                this.bindTemperatureAnalysisChartData(response.data);
                this.analysisChartData = response.data;
              }
            } else {
              let dataShow = this.getDataShow(response.data, 0);
              this.bindAllChartData(dataShow);
              // this.bindAllChartData(response.data);
              this.updateChartMoldLegend();
            }
            self.chart.config.data.datasets = config.data.datasets;
            self.chart.config.data.labels = config.data.labels;
            self.chart.resize();
            self.chart.update();
          })
          .catch((e) => console.log(e));
      }
      if (isChangeTab) {
        this.changeDateDataPicker();
      }
    },
    changeDateDataPicker() {
      let tempDate = {
        rangeYear: this.rangeYear,
        rangeMonth: this.rangeMonth,
        monthHour: this.monthHour,
        yearSelected: this.yearSelected,
        requestParam: this.requestParam,
        day: this.day,
      };
      var child = Common.vue.getChild(this.$children, "date-picker");
      if (child != null) {
        child.changeDateData(tempDate);
      }
    },
    getCurrentDate() {
      return `${this.yearSelected}${
        Number(this.monthHour) < 10
          ? `0${Number(this.monthHour)}`
          : this.monthHour
      }${Number(this.day) < 10 ? `0${Number(this.day)}` : this.day}`;
    },
    bindFrequentlyDataChart(detailDate) {
      this.frequentlyChartData = [];
      let param = {
        moldId: this.requestParam.moldId,
        chartDataType: this.requestParam.chartDataType,
      };
      let date = `${this.yearSelected}${
        Number(this.monthHour) < 10
          ? `0${Number(this.monthHour)}`
          : this.monthHour
      }${Number(this.day) < 10 ? `0${this.day}` : this.day}`;
      if (this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")) {
        date = `${date}${
          Number(detailDate) < 10 ? `0${Number(detailDate)}` : detailDate
        }`;
      }
      param.date = date;

      param = Common.param(param);
      axios.get("/api/chart/hour-details?" + param).then((response) => {
        if (!response.data || response.data.length === 0) {
          return;
        }
        this.isShowFrequentlyChart = true;
        if (this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")) {
          this.bindFrequentlyChart(response.data);
        } else if (
          this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
        ) {
          let chartData = [];
          let hashDataByMinute = {};
          response.data.forEach((item) => {
            let splitParams = item.title.split(" ")[1].split(":");
            let minute = splitParams[0] + ":" + splitParams[1];
            hashDataByMinute[minute] = item.data / 10;
          });
          let blockCount = 4; // 1, 2, 3, 4, 6 hour per block, block 1 is 00 - 05h59, 06h00 - 11h59, 12h00 - 17h59, 18h00 - 23h59, ()
          // group data by block, index will be 0, 1, 2, 3
          let hourPerBlock = 24 / blockCount;
          let groupData = [];
          for (let i = 0; i < blockCount; i++) {
            groupData.push([]);
          }

          Object.keys(hashDataByMinute).forEach((minute) => {
            let hour = parseInt(minute.split(":")[0]);
            let indexOfBlock = Math.floor(hour / hourPerBlock);
            groupData[indexOfBlock].push({
              data: hashDataByMinute[minute],
              title: minute,
            });
          });

          let realGroupData = [];
          groupData.forEach((item) => {
            if (item.length > 0) {
              realGroupData.push(item);
            }
          });

          this.frequentlyChartData = realGroupData;
          this.frequentlyChartDataIndex = 0;
          this.bindFrequentlyChart(groupData[0]);
        }
      });
    },
    bindFrequentlyChart(dataShow) {
      $(
        this.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).show();
      frequentlyConfig.data.labels = [];
      frequentlyConfig.data.datasets = [];
      this.frequentlyChart.update();

      let newDataset = null;
      if (this.requestParam.chartDataType === "CYCLE_TIME_ANALYSIS") {
        newDataset = {
          type: "bar",
          label: "Shot",
          backgroundColor: hexToRgba(getStyle("--info"), 50),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      } else {
        newDataset = {
          type: "line",
          label: "Temperature",
          backgroundColor: hexToRgba(getStyle("--info"), 10),
          borderColor: getStyle("--info"),
          pointRadius: 2,
          pointHoverBackgroundColor: "#fff",
          borderWidth: 2,
          data: [],
        };
      }

      const data = dataShow;

      for (let i = 0; i < data.length; i++) {
        let title = data[i].title;
        frequentlyConfig.data.labels.push(title);
        newDataset.data.push(data[i].data);
      }
      frequentlyConfig.options.tooltips = {
        enabled: true,
        intersect: true,
        callbacks: {
          afterLabel(tooltipItem) {
            const { index } = tooltipItem;
            let avgCavities = 0;
            if (data[index]) {
            }

            return ``;
          },
          label(tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel).toString();
            return `${label}: ${value}`;
          },
        },
      };
      console.log("newDataset", newDataset);
      frequentlyConfig.data.datasets.push(newDataset);
      //set caption
      frequentlyConfig.options.scales.xAxes[0].scaleLabel.labelString =
        this.xAxisDetailTitleMapping[this.requestParam.chartDataType];
      frequentlyConfig.options.scales.yAxes[0].scaleLabel.labelString =
        this.yAxisDetailTitleMapping[this.requestParam.chartDataType];
      frequentlyConfig.options.scales.yAxes[0].fontSize = 13;
      frequentlyConfig.options.scales.xAxes[0].fontSize = 13;

      this.frequentlyChart.update();
      this.updateChartFrequentlyLegend();
    },
    hideFrequentlyChart() {
      this.isShowFrequentlyChart = false;
      this.$nextTick(() => {
        if ("CYCLE_TIME_ANALYSIS" === this.requestParam.chartDataType) {
          if (this.chartTemperature != null && this.chartTemperature.options) {
            this.chartTemperature.destroy();
          }
          this.bindCycleTimeAnalysisChartData(this.analysisChartData);
        } else if ("TEMPERATURE_ANALYSIS" === this.requestParam.chartDataType) {
          if (this.isTemBar) {
            this.bindTemperatureBarChart(this.analysisChartData);
          } else {
            this.bindTemperatureAnalysisChartData(this.analysisChartData);
          }
          // this.bindTemperatureAnalysisChartData(this.analysisChartData);
        }
      });
    },
    getChartDefaultData() {
      let data = [];
      let labels = [];
      if ("HOUR" === this.requestParam.dateViewType) {
        for (let i = 0; i <= 23; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      } else if ("DAY" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 28; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      } else if ("WEEK" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 52; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push("W-" + title);
          data.push(0);
        }
      } else if ("MONTH" === this.requestParam.dateViewType) {
        for (let i = 1; i <= 12; i++) {
          let title = i < 10 ? "0" + i : i;
          labels.push(title);
          data.push(0);
        }
      }
      return {
        labels: labels,
        data: data,
      };
    },
    resetConfig(config) {
      config.options.tooltips = {
        callbacks: {
          afterLabel(tooltipItem) {
            return "";
          },
        },
      };
    },
    getChartInfor(type) {
      let title = "";
      let iconCode = "";
      const regexp = /([A-Za-z0-9 ()]+):(.*)/;
      const match = type.match(regexp);
      title = `${match[1]}: `;
      if (type.contain("Shot")) {
        // title = 'Shot'
        iconCode = `<div style="border: 1px solid #63C2DE; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("Weighted Average CT (sec)")) {
        // title = 'Weighted Average CT (sec)'
        iconCode = `<div style="border: 1px solid #4dbd74; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("Max CT (sec)")) {
        // title = 'Max CT (sec)'
        iconCode = `<div style="border: 1px solid #ffc107; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("Min CT (sec)")) {
        // title = 'Min CT (sec)'
        iconCode = `<div style="border: 1px solid #f86c6b; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("Within (sec)")) {
        // title = 'Within (sec)'
        iconCode = `<div style="border: 1px solid #63C2DE; background-color: #B1E0EE; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("L1 (sec)")) {
        // title = 'L1 (sec)'
        iconCode = `<div style="border: 1px solid #FFC107; background-color: #FFE083; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      } else if (type.contain("L2 (sec)")) {
        // title = 'L2 (sec)'
        iconCode = `<div style="border: 1px solid #F86C6B; background-color: #FBB5B5; height: 12px; width: 12px; margin-right: 5px;"></div>`;
      }
      return {
        title: title,
        iconCode: iconCode,
      };
    },
    setTooltipForMainChart(dateViewType, inputData) {
      const self = this;
      return {
        enabled: false,
        footerFontStyle: "normal",
        position: "nearest",
        intersect: true,
        // callbacks: {
        //   afterLabel(tooltipItem) {
        //     const { index } = tooltipItem;
        //     let avgCavities = 0;
        //     if (inputData[index]) {
        //       avgCavities = inputData[index].avgCavities;
        //     }
        //     let avgCavityTitle = `Avg.Cavities: ${Number(avgCavities)
        //       .toFixed(2)
        //       .toString()
        //       .replace(/\B(?=(\d{3})+(?!\d))/g, ",")}`;
        //     return `${avgCavityTitle}`;
        //   },
        //   beforeFooter(tooltipItem) {
        //     const { index } = tooltipItem[0];
        //     let resetLabel = null;
        //     if (inputData[index]) {
        //       let reset = inputData[index].resetValue;
        //       let lastShot = inputData[index].lastShot;
        //       if (reset != null && reset !== 0) {
        //         resetLabel = `On this ${dateViewType.toLowerCase()}, the tooling was reset to ${lastShot} shots`;
        //       }
        //     }
        //     return resetLabel;
        //   },
        //   label(tooltipItem, data) {
        //     const label = data.datasets[tooltipItem.datasetIndex].label;
        //     const value = Number(tooltipItem.yLabel).toString();
        //     console.log('tooltipItem', data)
        //     const { index } = tooltipItem;
        //     let dataValue = value;
        //     if (inputData[index]) {
        //       let reset = inputData[index].resetValue;
        //       if (reset != null && reset != 0) {
        //         dataValue =
        //           inputData[index].resetValue > 0
        //             ? inputData[index].data - inputData[index].resetValue
        //             : dataValue;
        //       }
        //     }
        //     return `${label}: ${dataValue}`;
        //   },
        // },
        custom: function (tooltipModel) {
          // Tooltip Element

          var tooltipEl = document.getElementById("chartjs-tooltip");
          // Create element on first render
          let xLabel = "";
          let yLabel = 0;
          let itemIndex = 0;
          const { dataPoints } = tooltipModel;
          if (dataPoints && dataPoints.length > 0) {
            xLabel = dataPoints[0].xLabel;
            yLabel = dataPoints[0].yLabel;
            itemIndex = dataPoints[0].index;
          }
          let avgCavities = 0;
          let totalPartProduced = 0;
          let dataValue = yLabel;
          let parts = [];

          let type = "";
          let title = "";
          let iconCode = "";

          if (tooltipModel.body) {
            type = tooltipModel.body[0].lines[0];
            const regexp = /([A-Za-z0-9 ()%]+):(.*)/;
            const match = type.match(regexp);
            title = `${match[1]}: `;
            if (type.includes("Shot")) {
              // title = 'Shot'
              iconCode = `<div style="border: 1px solid #63C2DE; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (
              type.includes("Weighted Average CT") ||
              type.includes("Uptime Target") ||
              type.includes("Approved CT")
            ) {
              // title = 'Weighted Average CT (sec)'
              iconCode = `<div style="border: 1px solid #4dbd74; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Max CT")) {
              // title = 'Max CT (sec)'
              iconCode = `<div style="border: 1px solid #ffc107; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Min CT")) {
              // title = 'Min CT (sec)'
              iconCode = `<div style="border: 1px solid #f86c6b; background-color: #fff; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("Within")) {
              // title = 'Within (sec)'
              iconCode = `<div style="border: 1px solid #63C2DE; background-color: #B1E0EE; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("L1")) {
              // title = 'L1 (sec)'
              iconCode = `<div style="border: 1px solid #FFC107; background-color: #FFE083; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            } else if (type.includes("L2")) {
              // title = 'L2 (sec)'
              iconCode = `<div style="border: 1px solid #F86C6B; background-color: #FBB5B5; height: 12px; width: 12px; margin-right: 5px;"></div>`;
            }
          }

          let resetLabel = null;
          if (inputData[itemIndex]) {
            avgCavities = inputData[itemIndex].avgCavities;
            totalPartProduced = inputData[itemIndex].totalPartProduced;
            let reset = inputData[itemIndex].resetValue;
            let lastShot = inputData[itemIndex].lastShot;
            if (reset != null && reset != 0) {
              /*
              dataValue =
                inputData[itemIndex].resetValue > 0
                  ? inputData[itemIndex].data - inputData[itemIndex].resetValue
                  : dataValue;
*/
              dataValue = inputData[itemIndex].data;
              /*
              let resetTo= dataValue + reset;
*/
              resetLabel = `On this ${dateViewType.toLowerCase()}, the tooling was reset to ${lastShot} shots`;
            }
            parts = inputData[itemIndex].partData;
          }

          const avgCavityTitle =
            "Avg.Cavities: " +
            appendThousandSeperator(formatToDecimal(avgCavities, 2));
          const totalPartProducedTitle =
            "Total Part Produced: " +
            appendThousandSeperator(totalPartProduced);
          if (title.includes("Shot")) {
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = generateTooltipHTML({
                xLabel,
                iconCode,
                title,
                dataValue,
                avgCavityTitle,
                totalPartProducedTitle,
                resetLabel,
              });
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }
            tooltipEl.innerHTML = generateTooltipHTML({
              xLabel,
              iconCode,
              title,
              dataValue,
              avgCavityTitle,
              totalPartProducedTitle,
              resetLabel,
            });
            var innerHtml = "<thead>";
            innerHtml += "<tr>";
            ["Part ID", "Shots", "Part Produced"].forEach(function (value) {
              innerHtml += "<th>" + value + "</th>";
            });
            innerHtml += "</tr></thead>";
            parts.forEach((element) => {
              let content = `
                    <tr>
                        <td style="text-align: center;">${element.partCode}</td>
                        <td style="text-align: center;">${appendThousandSeperator(
                          element.shot
                        )}</td>
                        <td style="text-align: center;">${appendThousandSeperator(
                          element.partProduced
                        )}</td>
                    </tr>
                  `;
              innerHtml += content;
            });
            var tableRoot = tooltipEl.querySelector("table");
            tableRoot.innerHTML = innerHtml;
          } else {
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = generateTooltipHTML({
                xLabel,
                iconCode,
                title,
                dataValue,
                avgCavityTitle,
              });
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }
            tooltipEl.innerHTML = generateTooltipHTML({
              xLabel,
              iconCode,
              title,
              dataValue,
              avgCavityTitle,
            });
          }
          var position = self.chart.canvas.getBoundingClientRect();
          tooltipEl.style.opacity = 1;
          tooltipEl.style.position = "absolute";
          tooltipEl.style[`z-index`] = 9999;
          tooltipEl.style.left =
            position.left + window.pageXOffset + +tooltipModel.caretX + "px";
          tooltipEl.style.top =
            position.top +
            window.pageYOffset +
            (tooltipModel.caretY + 20) +
            "px";
          tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
          tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
          tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
          tooltipEl.style.padding =
            tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
          tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
          tooltipEl.style.pointerEvents = "none";
          tooltipEl.style.transform = "translateX(-25%)";
        },
      };
    },
    getResetIcon() {
      let iconUrl = "/images/icon/reset-point-icon2.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 20;
      icon.height = 20;
      return icon;
    },
    getResetIconDefault() {
      // let iconUrl = '/images/icon/reset-point-icon.png';
      let iconUrl = "/images/icon/reset-point-white-ic.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 16;
      icon.height = 16;
      icon.setAttribute("background", "#4199FF 0% 0% no-repeat padding-box;");
      return icon;
    },
    getResetIconHover() {
      // let iconUrl = '/images/icon/reset-point-icon.png';
      let iconUrl = "/images/icon/reset-point-blue-ic.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 16;
      icon.height = 16;
      icon.setAttribute("background", "#FFFFFF 0% 0% no-repeat padding-box;");
      icon.setAttribute("border", "0.5px solid #4199FF;");
      return icon;
    },
    isResetItem(item, i) {
      if (item.resetValue && item.resetValue > 0) {
        return true;
      }
      return false;
    },
    getQuantityPointConfig(data) {
      let pointStyles = [];
      let pointRadius = [];
      let pointHoverRadius = [];
      let pointBackgroundColor = [];
      let pointHoverBackgroundColor = "rgb(255,255,255)";
      for (let i = 0; i < data.length; i++) {
        if (this.isResetItem(data[i], i)) {
          pointStyles.push(this.getResetIcon());
          pointBackgroundColor.push("rgb(65,153,255)");
          let radius = 2;
          pointRadius.push(radius);
          pointHoverRadius.push(radius * 5);
        } else {
          pointStyles.push("circle");
          pointRadius.push(2);
          pointHoverRadius.push(2);
          pointBackgroundColor.push(getStyle("--info"));
        }
      }
      return {
        pointStyles,
        pointRadius,
        pointHoverRadius,
        pointBackgroundColor,
        pointHoverBackgroundColor,
      };
    },
    getPartPointConfig(data) {
      let pointStyles = [];
      let pointRadius = [];
      let pointHoverRadius = [];
      let pointBackgroundColor = [];
      let pointHoverBackgroundColor = "rgb(255,255,255)";
      for (let i = 0; i < data.length; i++) {
        pointStyles.push(this.getResetIcon());
        pointBackgroundColor.push("rgb(65,153,255)");
        let radius = 2;
        pointRadius.push(radius);
        pointHoverRadius.push(radius * 5);
      }
      return {
        pointStyles,
        pointRadius,
        pointHoverRadius,
        pointBackgroundColor,
        pointHoverBackgroundColor,
      };
    },
    getMaxDataValue(data) {
      let max = 0;
      for (let i = 0; i < data.length; i++) {
        if (max < data[i].data) {
          max = data[i].data;
        }
      }
      return Math.round(max + max / 5);
    },
    bindCycleTimeAnalysisChartData(data) {
      let cycleAxis = [];
      let chartData = [];

      let countAxis = 24;
      for (let i = 0; i < countAxis; i++) {
        chartData.push([0, 0, 0, 0]);
      }
      if (data.length > 0) {
        const {
          cycleTimeMinusL1,
          cycleTimeMinusL2,
          cycleTimePlusL1,
          cycleTimePlusL2,
        } = data[0];

        cycleAxis = [
          cycleTimeMinusL2,
          cycleTimeMinusL1,
          cycleTimePlusL1,
          cycleTimePlusL2,
        ];

        data.forEach((item) => {
          let llct = item.llct ? item.llct : 0;
          let mfct = item.mfct ? item.mfct : 0;
          let ulct = item.ulct ? item.ulct : 0;
          let itemData = item.data ? item.data : 0;
          const title = item.title;
          const titleLength = title.length;
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            [llct, mfct, ulct, itemData];
        });
      } else {
        // cycleAxis = [0, 0, 0, 0, 0];
      }

      const totalInfo = {
        totalShotL2Bottom: cycleAxis[0],
        totalShotL1Bottom: cycleAxis[1],
        totalShotL1Top: cycleAxis[2],
        totalShotL2Top: cycleAxis[3],
      };
      //Todo
      const colorZone = {
        totalShotL2Bottom: "red",
        totalShotL1Bottom: getStyle("--warning"),
        totalShotL1Top: getStyle("--warning"),
        totalShotL2Top: "red",
      };
      this.cycleTimeHourlyChart = new CycleTimeHourlyChart(
        "hourly-chart",
        cycleAxis,
        chartData,
        totalInfo,
        colorZone,
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
      );
      this.cycleTimeHourlyChart.draw();
    },
    bindAccelerationChartData: function (dataShow) {
      config.data.labels = [];
      config.data.datasets = [];
      this.chart.resize();
      this.chart.update();

      let newDataset = {
        type: "line",
        label: "Value",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        borderColor: getStyle("--info"),
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
      };

      const data = dataShow.filter((value) => {
        if ("DAY" == this.requestParam.dateViewType) {
          const title = value.title.replace(this.requestParam.year, "W-");
          const day = title.replace("W-", "");
          if (Number(day.substring(0, 2)) == this.month) {
            return true;
          }
          return false;
        } else {
          return true;
        }
      });

      for (let i = 0; i < data.length; i++) {
        config.data.labels.push(data[i].title);
        newDataset.data.push(data[i].value);
      }
      if (!data.length) {
        newDataset.data = [];
      }

      config.options.tooltips = {
        callbacks: {
          afterLabel(tooltipItem) {
            return "";
          },
        },
      };
      config.data.datasets.push(newDataset);
      let xAxisTitle = "Time";
      let yAxisTitle = this.yAxisTitleMapping[this.requestParam.chartDataType];
      config.options.scales.xAxes[0].scaleLabel.labelString = xAxisTitle;
      config.options.scales.yAxes[0].scaleLabel.labelString = yAxisTitle;
      // this.setGraphAxisTitle();
      this.chart.config.data.datasets = config.data.datasets;
      this.chart.config.data.labels = config.data.labels;
      this.chart.resize();
      this.chart.update();
    },
    bindTemperatureAnalysisChartData(data) {
      if (this.chartTemperature != null && this.chartTemperature.options) {
        this.chartTemperature.destroy();
      }
      let chartData = [];
      let countAxis = 24;
      for (let i = 0; i < countAxis; i++) {
        chartData.push([0, 0, 0, 0]);
      }
      if (data.length > 0) {
        data.forEach((item) => {
          const tlo = (item.tlo ?? 0) / 10;
          const tav = (item.tav ?? 0) / 10;
          const thi = (item.thi ?? 0) / 10;
          const itemData = item.data ?? 0;
          const title = item.title;
          const titleLength = title.length;
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            [tlo, tav, thi, itemData];
        });
      }
      // chartData = this.getTestData();

      this.temperatureHourlyChart = new TemperatureHourlyChart(
        "hourly-chart",
        chartData,
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
      );
      this.temperatureHourlyChart.draw();
    },
    bindTemperatureBarChart(data) {
      if (
        this.chartTemperature.canvas != null &&
        this.chartTemperature.options
      ) {
        this.chartTemperature.destroy();
      }
      var labels = [
        "00",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
      ];
      let chartData = [];
      let chartDataTemp = [];
      let countAxis = 24;
      let shotQuantity = {
        type: "line",
        label: "Shot",
        backgroundColor: "#B1E0EE",
        fill: false,
        borderColor: "#63C2DE",
        pointRadius: 2,
        borderWidth: 2,
        data: [],
        yAxisID: "shot",
      };
      for (let i = 0; i < countAxis; i++) {
        chartData.push(0);
        chartDataTemp.push(0);
        shotQuantity.data.push(0);
      }
      if (data.length > 0) {
        data.forEach((item) => {
          const tlo = (item.tlo ?? 0) / 10;
          const tav = (item.tav ?? 0) / 10;
          const thi = (item.thi ?? 0) / 10;
          const title = item.title;
          const titleLength = title.length;
          chartDataTemp[
            parseInt(title[titleLength - 2] + title[titleLength - 1])
          ] = [tlo, tav, thi];
          chartData[parseInt(title[titleLength - 2] + title[titleLength - 1])] =
            tav;
          shotQuantity.data[
            parseInt(title[titleLength - 2] + title[titleLength - 1])
          ] = item.data;
        });
      }
      let configTem = {
        type: "bar",
        data: {
          labels: [],
          datasets: [
            {
              type: "bar",
              label: "Temperature (sec)",
              data: [],
              borderWidth: 2,
              backgroundColor: "#97DC98",
              fill: false,
              borderColor: "#38913A",
              pointRadius: 2,
            },
          ],
        },
        options: {
          animation: {
            duration: 0,
          },
          interaction: {
            mode: "nearest",
          },
          maintainAspectRatio: false,
          legend: {
            display: false,
            position: "bottom",
          },
          legendCallback(chart) {
            let text = [];
            text.push('<div class="chart-legend">');
            for (let i = chart.data.datasets.length - 1; i >= 0; i--) {
              let dataset = chart.data.datasets[i];
              let borderDashClass = "";
              let legendIconHtml = "";
              if (dataset.type === "line") {
                // line
                borderDashClass = "dash";
                if (dataset.borderDash) {
                  // dashed line
                  legendIconHtml =
                    '<div class="legend-icon" style="height: 1px; border-top: ' +
                    dataset.borderWidth +
                    "px  dashed" +
                    dataset.borderColor +
                    ';"></div>';
                } else {
                  // normal line
                  legendIconHtml =
                    '<div class="legend-icon" style="height: 1px; border-top: ' +
                    3 +
                    "px  solid" +
                    dataset.borderColor +
                    ';"></div>';
                }
              } else if (dataset.type === "bar") {
                // bar
                legendIconHtml =
                  '<div class="legend-icon bar" style="background-color:' +
                  dataset.backgroundColor +
                  "; border-color: " +
                  dataset.borderColor +
                  ';"></div>';
              }
              text.push(
                '<div><div class="legend-item" id="legend-item-' +
                  i +
                  '">' +
                  legendIconHtml
              );
              if (chart.data.datasets[i].label) {
                text.push(
                  '<div class="label">' +
                    chart.data.datasets[i].label +
                    "</div>"
                );
              }

              text.push('</div></div><div class="clear"></div>');
            }

            text.push("</div>");
            return text.join("");
          },
          tooltips: {
            mode: "index",
            intersect: false,
          },
          scales: {
            xAxes: [
              {
                stacked: false,
                // stacked: true,
                gridLines: {
                  drawOnChartArea: false,
                },
                scaleLabel: {
                  display: true,
                  labelString: "Hour",
                  fontSize: 13,
                },
              },
            ],
            yAxes: [
              {
                id: "tem",
                type: "linear",
                stacked: false,
                ticks: {
                  beginAtZero: false,
                  min: 0,
                },
                scaleLabel: {
                  display: true,
                  labelString: "Temperature (°C)",
                  fontSize: 13,
                },
              },
              {
                id: "shot",
                type: "linear",
                stacked: false,
                ticks: {
                  beginAtZero: false,
                  min: 0,
                },
                position: "right",
                scaleLabel: {
                  display: true,
                  labelString: "Shot Quantity",
                },
                gridLines: {
                  drawOnChartArea: false,
                },
              },
            ],
          },
          elements: {
            point: {
              radius: 0,
              hitRadius: 10,
              hoverRadius: 4,
              hoverBorderWidth: 3,
            },
          },
        },
      };
      let tooltipEl = document.getElementById("chartjs-tooltip");
      if (tooltipEl) {
        tooltipEl.remove();
      }
      configTem.options.tooltips = {
        // Disable the on-canvas tooltip
        enabled: false,

        custom: function (tooltipModel) {
          // Tooltip Element

          // Create element on first render
          if (!tooltipEl) {
            tooltipEl = document.createElement("div");
            tooltipEl.id = "chartjs-tooltip";
            tooltipEl.innerHTML = '<table style="margin-bottom: 0;"></table>';
            document.body.appendChild(tooltipEl);
          }

          // Hide if no tooltip
          if (tooltipModel.opacity === 0) {
            tooltipEl.style.opacity = 0;
            return;
          }

          // Set caret Position
          tooltipEl.classList.remove("above", "below", "no-transform");
          if (tooltipModel.yAlign) {
            tooltipEl.classList.add(tooltipModel.yAlign);
          } else {
            tooltipEl.classList.add("no-transform");
          }
          function getBody(bodyItem) {
            return bodyItem.lines;
          }
          // Set Text
          if (
            tooltipModel.body &&
            tooltipModel.body[0].lines[0].includes("Shot")
          ) {
            if (tooltipModel.body) {
              var titleLines = tooltipModel.title || [];
              var bodyLines = tooltipModel.body.map(getBody);

              var innerHtml = "<thead>";

              titleLines.forEach(function (title) {
                innerHtml +=
                  '<tr><th style="font-size: 14px;border: none;">' +
                  title +
                  "</th></tr>";
              });
              innerHtml += "</thead><tbody>";

              bodyLines.forEach(function (body, i) {
                var span = `<div style="display: flex;align-items: center;font-size: 14px"><div class="box-title"></div><div>${body}</div></div>`;
                innerHtml +=
                  '<tr><td style="border: none;">' + span + "</td></tr>";
              });
              innerHtml += "</tbody>";

              var tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
              tableRoot.style.marginBottom = "0!important";
            }
          } else {
            if (tooltipModel.body) {
              var titleLines = tooltipModel.title || [];

              var innerHtml = "<thead>";

              titleLines.forEach(function (title) {
                innerHtml +=
                  '<tr><th style="border: none;">' + title + "</th></tr>";
              });
              innerHtml += "</thead><tbody>";

              if (tooltipModel.title) {
                let data = chartDataTemp[+tooltipModel.title];
                console.log(chartDataTemp, "datadatadata 1");
                console.log(tooltipModel.title, "datadatadata 2");
                console.log(data, "datadatadata 3");
                innerHtml +=
                  '<tr><td style="border: none;">' +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">THI: ${data[2]}°C</div>` +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">TAV: ${data[1]}°C</div>` +
                  `<div style="padding-left: 10px;font-weight: 400;font-size: 14px;">TLO: ${data[0]}°C</div>` +
                  "</td></tr>";
                innerHtml += "</tbody>";
              }

              var tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
            }
          }

          // `this` will be the overall tooltip
          var position = this._chart.canvas.getBoundingClientRect();

          // Display, position, and set styles for font
          tooltipEl.style.opacity = 0.9;
          tooltipEl.style.borderRadius = "6px";
          tooltipEl.style.position = "absolute";
          tooltipEl.style[`z-index`] = 9999;
          tooltipEl.style.left =
            position.left + window.pageXOffset + tooltipModel.caretX + "px";
          tooltipEl.style.top =
            position.top +
            window.pageYOffset +
            (tooltipModel.caretY + 20) +
            "px";
          tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
          tooltipEl.style.fontSize = tooltipModel.bodyFontSize + "px";
          tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
          tooltipEl.style.padding =
            tooltipModel.yPadding + "px " + tooltipModel.xPadding + "px";
          tooltipEl.style["background-color"] = "rgba(51,51,51, 1)";
          tooltipEl.style.pointerEvents = "none";
          tooltipEl.style.transform = "translateX(-25%)";
        },
      };
      shotQuantity.type =
        this.chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
          .chartType === "LINE"
          ? "line"
          : "bar";
      const ctxTem = document.querySelector(
        this.getRootToCurrentId() + "#hourly-chart"
      );
      this.chartTemperature = new Chart(ctxTem, configTem);
      this.chartTemperature.data.labels = labels;
      this.chartTemperature.data.datasets[0].data = chartData;

      configTem.data.datasets.unshift(shotQuantity);
      let self = this;
      $(this.getRootToCurrentId() + "#hourly-chart").on("click", (e) => {
        console.log("hourly-chart");
        let chart = this.chartTemperature;
        let activePoints = chart.getElementsAtEventForMode(
          e,
          "point",
          this.chartTemperature.options
        );
        let firstPoint = activePoints[0];
        if (firstPoint != undefined) {
          let label = chart.data.labels[firstPoint._index];
          /*
          let child = Common.vue.getChild(vm.$children, "chart-mold");
          if (child != null) {
            child.details(label);
          }
*/
          self.details(label);
        }
      });
      this.chartTemperature.resize();
      this.chartTemperature.update();
    },
    setGraphAxisTitle() {
      let xAxisTitle = this.xAxisTitleMapping[this.requestParam.dateViewType];
      let yAxisTitle = this.yAxisTitleMapping[this.requestParam.chartDataType];
      config.options.scales.xAxes[0].scaleLabel.labelString = xAxisTitle;
      config.options.scales.yAxes[0].scaleLabel.labelString = yAxisTitle;
    },
    findMoldParts() {
      // Parts 조회
      axios.get("/api/molds/" + this.mold.id + "/parts").then((response) => {
        console.log("response::::::::::::::::::::::::::::", response);
        this.parts = response.data.content;

        //$('#op-mold-details').modal('show');
      });
    },
    getPartChanges(id, startDate, endDate) {
      this.loadingPartChange = true;
      let params = {
        moldId: id,
      };
      if (this.requestParam.dateViewType == "DAY") {
        let fromD = new Date(
          this.yearSelected,
          this.monthSelected - 1,
          this.day
        );
        params.startDate = moment(fromD).format("YYYYMMDDHHmmss");
        params.endDate = moment(
          new Date(this.yearSelected, this.monthSelected, 1)
        ).format("YYYYMMDDHHmmss");
      }
      if (this.requestParam.dateViewType == "HOURLY") {
        let fromD = new Date(
          this.yearSelected,
          this.monthSelected - 1,
          this.day
        );
        params.startDate = moment(fromD).format("YYYYMMDDHHmmss");
        params.endDate = moment(
          new Date(this.yearSelected, this.monthSelected - 1, this.day + 1)
        ).format("YYYYMMDDHHmmss");
      }
      if (startDate) {
        params.startDate = startDate;
      }
      if (endDate) {
        params.endDate = endDate;
      }
      let paramUri = Common.param(params);

      axios
        .get("/api/chart/get-part-changes?" + paramUri)
        .then((res) => {
          this.numberOfPart = res.data.data?.numberOfPart || 0;
          this.singlePartName = res.data.data?.singlePartName;
          this.partChangeData = res.data.data?.list;
        })
        .finally(() => {
          this.loadingPartChange = false;
        });
    },
    details(detailDate) {
      console.log(detailDate, "detailDate");
      if (this.requestParam.dateViewType === "HOUR") {
        if (
          this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS") ||
          this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
        ) {
          if (this.isShowFrequentlyChart === true && isNaN(detailDate)) {
            return;
          }
          this.bindFrequentlyDataChart(detailDate);
          return;
        }
        let date = `${
          Number(this.monthHour) < 10
            ? `0${Number(this.monthHour)}`
            : this.monthHour
        }/${Number(this.day) < 10 ? `0${this.day}` : this.day}`;
        this.requestParam.detailDate = date;
      }
    },
    checkingShowChartDetail(detailDate) {
      let checkingDate = null;
      switch (this.requestParam.dateViewType) {
        case "MONTH":
          checkingDate = `M${this.requestParam.year}${detailDate}`;
          break;
        case "WEEK":
          checkingDate =
            `W${this.requestParam.year}` + detailDate.split("-")[1];
          break;
        case "DAY":
          checkingDate =
            this.requestParam.year + detailDate.split("/").join("");
          break;
        default:
          checkingDate = this.requestParam.year + detailDate;
      }
      let checkingParam = {
        moldId: this.requestParam.moldId,
        dateDetails: checkingDate,
      };

      checkingParam = Common.param(checkingParam);
      axios
        .get("/api/statistics/check-old-counter?" + checkingParam)
        .then((response) => {
          this.isShowTemperatureColumn = !response.data;
          if (["MONTH", "WEEK"].includes(this.requestParam.dateViewType)) {
            // this.paging(1);
          } else {
            // day
            if (response.data) {
              // this.paging(1);
            } else if (this.isNotCMSCounter()) {
              const listDate = detailDate.split("/");
              if (listDate.length === 2) {
                this.monthHour = listDate[0];
                this.day = Number(listDate[1]);
              }
              this.dateViewType("HOUR");
            }
          }
        })
        .catch((error) => {
          console.log("check-old-counter-error", error.response);
        });
    },
    paging(pageNumber) {
      $(this.getRootToCurrentId() + "#op-daily-details").show();
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let paramData = Object.assign({}, this.requestParam);
      if (paramData.dateViewType === "HOUR") {
        paramData.dateViewType = "DAY";
      }
      let param = Common.param(paramData);
      axios.get("/api/statistics?" + param).then(
        (response) => {
          this.total = response.data.totalElements;
          this.results = response.data.content;
          this.pagination = Common.getPagingData(response.data);

          Common.handleNoResults("#op-daily-details", this.results.length);
        },
        (error) => {
          console.log(error.response);
        }
      );
    },
    closeDatePicker() {
      console.log("close");
    },
    handleChangeDate(data, tempData) {
      if (data.isCustomRange) {
        if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
        } else {
          this.requestParam.dateViewType = tempData.selectedType.replace(
            "LY",
            ""
          );
        }
        this.requestParam.startDate = data.range.startDate;
        this.requestParam.endDate = data.range.endDate;
        this.requestParam.year = "";
        this.requestParam.month = "";
        this.yearSelected = "";
        this.isSelectedCustomRange = true;
      } else {
        this.requestParam.dateViewType = tempData.selectedType.replace(
          "LY",
          ""
        );
        this.requestParam.year = data.time.slice(0, 4);
        delete this.requestParam.month;
        delete this.requestParam.day;
        delete this.requestParam.startDate;
        delete this.requestParam.endDate;
        if (tempData.selectedType == "HOURLY") {
          this.requestParam.month = tempData.singleDate.slice(4, 6);
          this.requestParam.day = moment(data.time, "YYYYMMDD").format("DD");
        } else if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.requestParam.month = tempData.singleDate.slice(4, 6);
        } else {
          //WEEKLY or MONTHLY
          //doNothing
        }
        this.isSelectedCustomRange = false;
      }

      //  this.bindChartData();

      let startDate = null;
      let endDate = null;
      if (data.isCustomRange) {
        startDate = moment(data.range.startDate, "YYYYMMDD").format(
          "YYYYMMDDHHmmss"
        );
        endDate = moment(data.range.endDate, "YYYYMMDD")
          .add(1, "d")
          .format("YYYYMMDDHHmmss");
      } else {
        if (tempData.selectedType == "HOURLY") {
          this.monthHour = tempData.singleDate.slice(4, 6);
          startDate = moment(data.time, "YYYYMMDD").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMMDD")
            .add(1, "d")
            .format("YYYYMMDDHHmmss");
          // this.hourlyDate = data.time
          this.day = moment(data.time, "YYYYMMDD").format("DD");
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;
        } else if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.monthHour = tempData.singleDate.slice(4, 6);
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;

          startDate = moment(data.time, "YYYYMM").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMM")
            .add(1, "M")
            .format("YYYYMMDDHHmmss");
        } else if (
          tempData.selectedType == "WEEKLY" ||
          tempData.selectedType == "MONTHLY"
        ) {
          startDate = moment(data.time, "YYYY").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYY")
            .add(1, "y")
            .format("YYYYMMDDHHmmss");
        }
      }

      this.bindChartData();
      this.getPartChanges(this.mold.id, startDate, endDate);
    },
    async getCfgStp() {
      try {
        const options = await Common.getSystem("options");
        const listConfigs = JSON.parse(options);
        if (
          listConfigs.OPTIMAL_CYCLE_TIME &&
          Common.octs[listConfigs.OPTIMAL_CYCLE_TIME.strategy]
        ) {
          this.optimalCycleTime =
            Common.octs[listConfigs.OPTIMAL_CYCLE_TIME.strategy];
        }
      } catch (error) {
        console.log(error);
      }
    },

    initChart() {
      console.log("init chart");
      const ctx = document.querySelector(
        this.getRootToCurrentId() + "#chart-mold"
      );
      const ctx2 = document.querySelector(
        this.getRootToCurrentId() + "#chart-frequently"
      );

      let self = this;
      self.chart = new Chart(ctx, config);
      // 차트 클릭 시
      $(self.getRootToCurrentId() + "#chart-mold").on("click", (e) => {
        let chart = self.chart;
        //let activePoints = chartOverview.getSegmentsAtEvent(e);
        let activePoints = chart.getElementsAtEventForMode(
          e,
          "point",
          self.chart.options
        );
        let firstPoint = activePoints[0];

        if (firstPoint != undefined) {
          let label = chart.data.labels[firstPoint._index];
          /*
          let child = Common.vue.getChild(vm.$children, "chart-mold");
          if (child != null) {
            child.details(label);
          }
*/
          self.details(label);
        }
      });

      $(self.getRootToCurrentId() + ".op-close-details").on("click", (e) => {
        $(self.getRootToCurrentId() + "#op-daily-details").hide();
      });
      // frequently chart

      $(
        self.getRootToCurrentId() + ".frequently-chart-container-wrapper"
      ).hide();
      self.frequentlyChart = new Chart(ctx2, frequentlyConfig);
      $(self.getRootToCurrentId() + ".op-close-frequently").on("click", (e) => {
        /*
        let child = Common.vue.getChild(vm.$children, "chart-mold");
        if (child != null) {
          // child.hideFrequentlyChart();
        }
*/
        $(
          self.getRootToCurrentId() + ".frequently-chart-container-wrapper"
        ).hide();
      });
    },
    async getMoldById(id) {
      try {
        const res = await axios.get(`/api/molds/${id}`);
        return res.data;
      } catch (error) {}
      return {};
    },
  },
  computed: {
    isTemBar() {
      return (
        this.chartTypeConfig.length > 0 &&
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "TEMPERATURE"
        )[0].chartType === "BAR" &&
        this.requestParam.chartDataType.includes("TEMPERATURE_ANALYSIS")
      );
    },
    isCycleBar() {
      return (
        this.chartTypeConfig.length > 0 &&
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "TEMPERATURE"
        )[0].chartType === "BAR" &&
        this.requestParam.chartDataType.includes("CYCLE_TIME_ANALYSIS")
      );
    },
    categoryLocation() {
      if (this.mold === null || this.mold.part.category === null) {
        return "";
      }

      try {
        let category = this.mold.part.category;
        let parentCategory = category.parent;

        let displayCategory = "";
        if (parentCategory != null && parentCategory.name != null) {
          displayCategory = parentCategory.name + " > ";
        }

        return "(" + displayCategory + category.name + ")";
      } catch (e) {
        return "";
      }
    },
    hasCustomRangeNext() {
      if (this.isSelectedCustomRange) {
        if (
          this.customRangeData.page + 1 <
          this.customRangeData.allData.length / Common.MAX_VIEW_POINT
        ) {
          return true;
        }
      }
      return false;
    },
    hasCustomRangePre() {
      if (this.isSelectedCustomRange) {
        if (this.customRangeData.page > 0) {
          return true;
        }
      }
      return false;
    },
    isHourlyEnabled() {
      return (
        this.options?.CLIENT?.chartMold?.datePicker?.hourlyEnabled || false
      );
    },
  },
  mounted() {
    this.initChart();
    this.getCfgStp();
    // this.isAbb();
    this.getOptions();
    this.getChartTypeConfig();
  },
  watch: {
    monthHour(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
        }
        this.month = parseInt(month);
      }
    },
  },
};

const config = {
  type: "bar",
  data: {
    labels: [],
    datasets: [],
  },
  options: {
    animation: {
      duration: 0,
    },
    interaction: {
      mode: "nearest",
    },
    maintainAspectRatio: false,
    legend: {
      display: false,
      position: "bottom",
      // onClick: (e) => e.stopPropagation(), // legend 클릭 시 차트 토글 여부 비활성화 처리 중
    },
    legendCallback(chart) {
      let text = [];
      text.push('<div class="chart-legend">');
      for (let i = 0; i < chart.data.datasets.length; i++) {
        let dataset = chart.data.datasets[i];
        let borderDashClass = "";
        let legendIconHtml = "";
        if (dataset.type === "line") {
          switch (dataset.label) {
            case "Within (sec)":
              legendIconHtml =
                '<img style="max-width: 105px" src="/images/graph/graph-setting-line.svg"/>';
              break;
            case "L1 (sec)":
              break;
            case "L2 (sec)":
              break;
            case "Within":
              legendIconHtml =
                '<img style="max-width: 105px" src="/images/graph/graph-setting-line.svg"/>';
              break;
            case "L1":
              break;
            case "L2":
              break;
            default:
              if (dataset.borderDash) {
                // dashed line
                legendIconHtml =
                  '<div class="legend-icon" style="height: 1px; border-top: ' +
                  dataset.borderWidth +
                  "px  dashed" +
                  dataset.borderColor +
                  ';"></div>';
              } else {
                // normal line
                legendIconHtml =
                  '<div class="legend-icon" style="height: 1px; border-top: ' +
                  3 +
                  "px  solid" +
                  dataset.borderColor +
                  ';"></div>';
              }
          }
        } else if (dataset.type === "bar") {
          // bar
          legendIconHtml =
            '<div class="legend-icon bar" style="background-color:' +
            dataset.backgroundColor +
            "; border-color: " +
            dataset.borderColor +
            ';"></div>';
        }
        text.push(
          '<div><div class="legend-item" id="legend-item-' +
            i +
            '">' +
            legendIconHtml
        );
        if (chart.data.datasets[i].label) {
          if (dataset.type === "line") {
            switch (dataset.label) {
              case "Within (sec)":
                text.push(
                  '<div class="label"> Cycle Time (Within, L1, L2) </div>'
                );
                break;
              case "L1 (sec)":
                break;
              case "L2 (sec)":
                break;
              case "Within":
                text.push('<div class="label"> Uptime (Within, L1, L2) </div>');
                break;
              case "L1":
                break;
              case "L2":
                break;
              default:
                text.push(
                  '<div class="label">' +
                    chart.data.datasets[i].label +
                    "</div>"
                );
            }
          } else {
            text.push(
              '<div class="label">' + chart.data.datasets[i].label + "</div>"
            );
          }
        }

        text.push('</div></div><div class="clear"></div>');
      }

      text.push("</div>");

      return text.join("");
    },
    tooltips: {
      mode: "index",
      intersect: false,
    },
    scales: {
      xAxes: [
        {
          stacked: true,
          gridLines: {
            drawOnChartArea: false,
          },
          scaleLabel: {
            display: true,
            labelString: "Date",
            fontSize: 13,
          },
        },
      ],
      yAxes: [
        {
          id: "first-y-axis",
          type: "linear",
          stacked: true,
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          scaleLabel: {
            display: true,
            labelString: "CycleTime",
            fontSize: 13,
          },
        },
        {
          id: "second-y-axis",
          type: "linear",
          stacked: false,
          ticks: {
            beginAtZero: true,
            min: 0,
          },
          position: "right",
          scaleLabel: {
            display: true,
            labelString: "Shot",
          },
        },
      ],
    },
    elements: {
      point: {
        radius: 0,
        hitRadius: 10,
        hoverRadius: 4,
        hoverBorderWidth: 3,
      },
    },
  },
};

const frequentlyConfig = Object.assign({}, config);
frequentlyConfig.options.scales.yAxes[0].ticks.beginAtZero = false;

// end frequently chart
</script>
<style>
.ant-time-picker-panel {
  z-index: 5900 !important;
}

.select-graph-group {
  width: 152px;
  height: 32px;
  margin-right: 10px;
}

.custom-table-tooltip {
  width: auto;
  border-collapse: collapse;
  border-spacing: 0;
  font-size: 13px;
}

.custom-table-tooltip td,
.custom-table-tooltip th {
  padding: 0 1rem 0 0.25rem;
  border: 1px solid white;
  /*max-width: 150px;*/
  /* -webkit-line-clamp: 2; */
}

.custom-table-tooltip tr:first-child td,
.custom-table-tooltip tr:first-child th {
  border-top: 0;
  white-space: nowrap;
}

.custom-table-tooltip tr td:first-child,
.custom-table-tooltip tr th:first-child {
  border-left: 0;
}

.custom-table-tooltip tr:last-child td,
.custom-table-tooltip tr:last-child th {
  border-bottom: 0;
}

.custom-table-tooltip tr td:last-child,
.custom-table-tooltip tr th:last-child {
  border-right: 0;
}

.ant-tooltip-inner {
  width: fit-content !important;
}
</style>
<style scoped>
.btn:focus {
  box-shadow: unset;
}

.select-date-group {
  display: flex;
  grid-template-columns: 1fr 1fr;
}

.graph-left-header {
  padding-left: 10px;
}

.graph-middle-header {
  display: flex;
}

@media (max-width: 991px) {
  .graph-left-header {
    width: 100%;
  }

  .graph-middle-header,
  .graph-right-header {
    margin-top: 10px;
    padding-left: 10px;
  }
}

.next-chart-container,
.frequently-chart-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.month-class {
  height: calc(2.0625rem + 2px);
  width: 100%;
  border: 1px solid #e4e7ea;
  border-radius: 0.25rem;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0px !important;
}

.icon {
  cursor: pointer;
  font-size: 23px;
}

.icon:hover {
  color: #3ec1d4;
}

.icon:active {
  color: #3ec1d4;
}

#hourly-chart-wrapper {
  width: 95%;
  position: absolute;
  height: 370px;
  left: 25px;
  bottom: 10px;
  background: rgb(255, 255, 255);
}

@media (min-width: 992px) {
  #hourly-chart-wrapper {
    height: 400px;
  }
}

@media (min-width: 1100px) {
  div:not(.hourly-custom-legend) #hourly-chart-wrapper {
    height: 500px;
  }
  .hourly-custom-legend #hourly-chart-wrapper {
    height: 450px !important;
  }
}
.hourly-custom-legend .op-chart-wrapper {
  height: 450px !important;
}

.chart-wrapper-inner {
  position: relative;
  height: 100%;
  width: 100%;
}

#chart-hint {
  position: absolute;
  padding: 5px;
  background: rgba(51, 51, 51, 1);
  color: #fff;
  text-transform: capitalize;
  display: none;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  transform: translate(-25%, 15px);
  border-radius: 6px;
}

#chart-hint::before {
  content: "";
  position: absolute;
  left: 25%;
  top: -10px;
  display: block;
  width: 0;
  height: 0;
  border-left: 5px solid rgba(0, 0, 0, 0);
  border-right: 5px solid rgba(0, 0, 0, 0);
  border-bottom: 10px solid rgba(51, 51, 51, 1);
  transform: translateX(-100%);
}

#circle-point {
  width: 7px;
  height: 7px;
  border: 2px solid #62c1dd;
  border-radius: 50%;
  background: #fff;
  position: absolute;
  display: none;
}

#shot-count-wrapper {
  position: relative;
}

#shot-count-wrapper #shot-count-content {
  position: absolute;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  padding: 5px;
  left: 0px;
  top: 10px;
  border-radius: 3px;
  transform: translate(-25%, 10px);
}

#shot-count-wrapper #shot-count-content::before {
  content: "";
  position: absolute;
  left: 25%;
  top: -10px;
  display: block;
  width: 0;
  height: 0;
  border-left: 5px solid rgba(0, 0, 0, 0);
  border-right: 5px solid rgba(0, 0, 0, 0);
  border-bottom: 10px solid rgba(0, 0, 0, 0.7);
  transform: translateX(-50%);
}

#shot-count-wrapper #shot-count-content #count-content {
  display: flex;
  padding: 7px 0;
}

#shot-count-wrapper #shot-count-content #count-content #count-content-left {
  width: 20px;
  padding-top: 3px;
}

#shot-count-wrapper
  #shot-count-content
  #count-content
  #count-content-left
  #square-hint {
  width: 15px;
  height: 15px;
  border: 1px solid #62c1dd;
  background: #f0f8fb;
}

#shot-count-wrapper #shot-count-content #count-content #count-content-right {
  padding: 2px 2px;
  min-width: 90px;
  width: auto;
}

.transparent {
  opacity: 0;
}

.btn-group-toggle .btn-outline-success {
  max-height: 35px;
}

.chart-legend {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  padding-top: 5px;
}

.chart-legend .legend-item,
.chart-legend .legend-frequently-item {
  margin-right: 5px;
  display: flex;
  align-items: center;
}

.legend-item .legend-icon,
.legend-frequently-item .legend-icon {
  width: 35px;
}

.legend-icon.bar {
  height: 12px;
  border-width: 2px;
  border-style: solid;
}

.legend-item .label,
.legend-frequently-item .label {
  color: #666666;
  font-size: 13px;
  cursor: default;
}

.clear {
  clear: both;
}

.analysis-hint {
  position: absolute;
  right: -5px;
  bottom: -5px;
  font-size: 13px;
}

.legend-item.removed,
.legend-frequently-item.removed {
  text-decoration: line-through;
}

.modal-title {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 31px;
  border-radius: 6px 6px 0 0;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: 0;
  width: 100%;
}

.modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}

.switch-zone {
  display: inline-flex;
  align-items: center;
}

.switch-zone div {
  cursor: pointer;
  width: 132px;
  height: 26px;
  background: #fff;
  border-radius: 3px;
  border: 1px solid #d0d0d0;
  font-size: 16px;
  color: #888888;
  padding: 4px 0;
  margin-right: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.switch-zone div:not(.active):hover {
  background: #f2f5fa;
  border-color: #3491ff;
  color: #3491ff;
}

.switch-zone div.active {
  background: #3491ff;
  color: #fff;
  border-color: #d0d0d0;
}

.primary-animation-switch {
  animation: primary-active-switch 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  border-color: #89d1fd !important;
  color: #0279fe !important;
}

@keyframes primary-active-switch {
  0% {
  }
  33% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  66% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  100% {
  }
}

.switch-btn {
  padding: 5px 10px;
  border: 1px solid #d0d0d0;
  font-size: 12px;
  color: #8b8b8b;
}

.switch-btn:hover {
  background: #deedff;
  color: #3585e5;
}

.switch-btn.active {
  border: 1px solid #3491ff;
  background: rgb(222, 237, 255);
  color: #3491ff;
}

.info-zone {
  margin-top: 25px;
  font-size: 16px;
  display: flex;
}

.info-zone .info-zone_tooling span {
  background: #fbfcfd;
  width: 199px;
  height: 24px;
  padding: 4px 104px 2px 15px;
}

.info-zone .info-zone_tooling b {
  color: #4b4b4b;
  margin-right: 6px;
}

.info-zone .info-zone_tooling {
  margin-right: 43px;
}
</style>
<style>
#chartjs-tooltip {
  border-radius: 6px;
  color: #ffffff;
}
#chartjs-tooltip::before {
  content: "";
  position: absolute;
  left: 25%;
  top: -10px;
  display: block;
  width: 0;
  height: 0;
  border-left: 5px solid rgba(0, 0, 0, 0);
  border-right: 5px solid rgba(0, 0, 0, 0);
  border-bottom: 10px solid rgba(51, 51, 51, 1);
  transform: translateX(-50%);
}
</style>
