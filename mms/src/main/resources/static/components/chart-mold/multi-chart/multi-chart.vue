<template>
  <div class="multi-chart-container">
    <div class="multi-chart-filter">
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
              :class="{ active: chartType === 'CYCLE_TIME' }"
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
            <template v-if="isNotCMSCounter()">
              <label
                class="btn switch-btn"
                v-bind:class="{ active: chartType === 'CYCLE_TIME_ANALYSIS' }"
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
                v-bind:class="{ active: chartType === 'TEMPERATURE_ANALYSIS' }"
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
                  :min-date="minDate"
                  :max-date="maxDate"
                  :exists-hourly="isNotCMSCounter()"
                >
                </date-picker>
              </div>
              <template v-if="chartType === 'ACCELERATION'">
                <a-date-picker
                  v-model="startDate"
                  format="YYYY-MM-DD (dddd)"
                  @change="reLoadChartGraph()"
                >
                </a-date-picker>
                <a-time-picker
                  format="HH"
                  @change="reLoadChartGraph()"
                  v-model="startDate"
                ></a-time-picker>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="multi-chart-content">
      <div class="multi-chart-content__row">
        <div
          v-for="(mold, index) in listSelected"
          :key="`${mold.tempIndex}-${mold.id}`"
          class="multi-chart-content__item"
          :class="[{ empty: !mold.id }, `${mold.tempIndex}-${mold.id}`]"
        >
          <div
            v-if="!mold.id"
            style="display: flex; align-items: center; justify-content: center"
          >
            <base-dropdown
              level="secondary"
              placeholder="Search Tooling Id"
              :options="listAvailableToolings"
              :sort="false"
              :loading="loading"
              @select="(mold) => handleSelectTooling(mold, index)"
            >
              Add Tooling Chart
            </base-dropdown>
          </div>
          <template v-else>
            <div
              class="multi-chart-content__close"
              @click="handleCloseTooling(mold)"
            >
              <img
                src="/images/icon/close.svg"
                style="width: 15.56px; height: 15.56px"
                alt=""
              />
            </div>
            <div class="multi-chart-content__graph">
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
                  <div v-if="numberOfPart[mold.id] === 1">
                    <a-tooltip
                      placement="bottom"
                      overlay-className="custom-ant-tooltip"
                    >
                      <template slot="title">
                        <table
                          v-if="partChangeData[mold.id][0].changed"
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
                                    v-for="(item, index) in partChangeData[
                                      mold.id
                                    ][0].original"
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
                                    v-for="(item, index) in partChangeData[
                                      mold.id
                                    ][0].changed"
                                    :key="index"
                                  >
                                    {{ replaceLongtext(item.code, 20) }}
                                  </span>
                                </div>
                              </div>
                            </td>
                            <td>
                              <span>{{
                                convertDate(partChangeData[mold.id][0].dateTime)
                              }}</span>
                            </td>
                            <td>
                              <span>{{
                                replaceLongtext(
                                  partChangeData[mold.id][0].reportedBy,
                                  40
                                )
                              }}</span>
                            </td>
                          </tr>
                        </table>
                        <table
                          v-else-if="
                            partChangeData[mold.id][0].original != null &&
                            partChangeData[mold.id][0].original.length > 0
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
                                {{
                                  partChangeData[mold.id][0].original[0].name
                                }}
                              </div>
                              <div>
                                {{
                                  replaceLongtext(
                                    partChangeData[mold.id][0].original[0].code,
                                    20
                                  )
                                }}
                              </div>
                            </td>
                          </tr>
                        </table>
                      </template>
                      <span
                        v-if="singlePartName[mold.id]"
                        style="color: rgb(52, 145, 255)"
                        >{{
                          replaceLongtext(singlePartName[mold.id], 20)
                        }}</span
                      >
                    </a-tooltip>
                    was produced in the selected period&nbsp;
                    <a-tooltip placement="bottom">
                      <template slot="title">
                        <table
                          v-if="partChangeData[mold.id][0].changed"
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
                                    v-for="(item, index) in partChangeData[
                                      mold.id
                                    ][0].original"
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
                                    v-for="(item, index) in partChangeData[
                                      mold.id
                                    ][0].changed"
                                    :key="index"
                                  >
                                    {{ replaceLongtext(item.code, 20) }}
                                  </span>
                                </div>
                              </div>
                            </td>
                            <td>
                              <span>{{
                                convertDate(partChangeData[mold.id][0].dateTime)
                              }}</span>
                            </td>
                            <td>
                              <span>{{
                                partChangeData[mold.id][0].reportedBy
                              }}</span>
                            </td>
                          </tr>
                        </table>
                        <table
                          v-else-if="
                            partChangeData[mold.id][0].original != null &&
                            partChangeData[mold.id][0].original.length > 0
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
                                {{
                                  partChangeData[mold.id][0].original[0].name
                                }}
                              </div>
                              <div style="font-size: 11px">
                                {{
                                  replaceLongtext(
                                    partChangeData[mold.id][0].original[0].code,
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
                            v-for="(part, index) in partChangeData[mold.id]"
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
                          numberOfPart[mold.id] === 0
                            ? "No parts were produced in the selected period."
                            : numberOfPart[mold.id] +
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

              <chart-graph
                :id="`chart-${mold.id}`"
                :ref="`chart-${mold.id}`"
                :resources="resources"
                :hide-legend="true"
                :hide-backdrop="true"
                height="275px"
                @on-show-frequently-chart="onShowFrequentlyChart"
                @graph-page="loadGraphPage"
                :current-page="chartPage.currentPage"
                @load-detail-graph-page="loadDetailGraphPage"
                :detail-current-page="chartPage.detailCurrentPage"
              ></chart-graph>
            </div>
          </template>
        </div>
      </div>
    </div>
    <div class="multi-chart-footer">
      <base-button type="cancel" @click="$emit('close')">
        Close Multi-Chart
      </base-button>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "MultiChart",
  props: {
    show: Boolean,
    resources: Object,
    octs: String,
    listChecked: Array,
    listCheckedFull: Array,
  },
  components: {
    "multi-chart-item": httpVueLoader("./multi-chart-item.vue"),
    "filter-graph-chart": httpVueLoader("./filter-graph-chart.vue"),
    "date-picker": httpVueLoader("/components/mold-popup/date-picker.vue"),
    "data-filter": httpVueLoader("/components/dfilter/data-filter.vue"),
    "chart-graph": httpVueLoader(
      "/components/chart-mold/detail-graph/chart-graph.vue"
    ),
  },
  data() {
    return {
      listSelected: [],
      accelerationList: [],
      startDate: moment(),
      bubbleChartComponentKey: 0,
      graphSelectedOptions: ["QUANTITY"],
      shotSelected: [],
      shotDefaulted: [],
      cycleTimeSelected: [],
      cycleTimeDefaulted: [],
      UptimeSelected: [],
      UptimeDefaulted: [],
      datePickerType: "DAILY",
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
        detailDate: "",
        sort: "id,asc",
        size: "50",
      },
      yearSelected: moment().format("YYYY"),
      results: [],
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
      // partChangeData: "",
      partChangeData: [],
      loadingPartChange: true,
      // numberOfPart: 0,
      numberOfPart: [],
      // singlePartName: "",
      singlePartName: [],
      hourlyDate: 0,
      isShowGraphDropDown: true,
      isShowChart: false,
      minDate: null,
      maxDate: null,
      isSelectedCustomRange: false,
      chartTypeConfig: {},
      shotImg: "",
      cycleTimeImg: "",
      uptimeImg: "",
      customRangeData: {
        allData: [],
        page: 0,
      },
      graphSettingDataList: [],
      chartPage: {
        currentPage: 0,
        totalPage: 1,
        detailCurrentPage: 0,
        detailTotalPage: 1,
      },
      listToolings: [],
      loading: false,
      listAvailableToolings: [],
    };
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
    hasChartPageNext() {
      if (this.isShowFrequentlyChart) {
        if (
          this.chartPage.detailCurrentPage + 1 <
          this.chartPage.detailTotalPage
        ) {
          return true;
        }
      }

      if (
        this.isSelectedCustomRange &&
        !["CYCLE_TIME_ANALYSIS", "TEMPERATURE_ANALYSIS"].includes(
          this.chartType
        )
      ) {
        if (this.chartPage.currentPage + 1 < this.chartPage.totalPage) {
          return true;
        }
      }
      return false;
    },
    hasChartPagePre() {
      if (this.isShowFrequentlyChart) {
        if (this.chartPage.detailCurrentPage > 0) {
          return true;
        }
      }
      if (
        this.isSelectedCustomRange &&
        !["CYCLE_TIME_ANALYSIS", "TEMPERATURE_ANALYSIS"].includes(
          this.chartType
        )
      ) {
        if (this.chartPage.currentPage > 0) {
          return true;
        }
      }
      return false;
    },
    // listAvailableToolings() {
    //   return this.listToolings.filter(mold => this.listSelected.some(selected => selected?.id !== mold.id))
    // }
  },
  methods: {
    convertChartType() {
      let type = null;
      console.log("@convertChartType::this.chartType", this.chartType);
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
      console.log("@getCurrentChartSettings:type:::::::::::::;", type);
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
                console.log(
                  "res.data.data[i].selected",
                  res.data.data[i].selected
                );
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
          this.graphSettingDataList = res.data.data;
          this.reLoadChartGraph();
        });
    },
    updateCurrentChartSettings(data) {
      console.log("@updateCurrentChartSettings", data);
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
            icon: "/images/graph/approve-icon.svg",
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
              if (res.data?.data) {
                this.graphSettingDataList = res.data.data;
                this.reLoadChartGraph();
              }
            });
        }, 300);
      }
    },
    getChartTypeConfig(moldId) {
      console.log("@getChartTypeConfig", moldId);
      axios.get(`/api/common/dsp-stp?moldId=${moldId || ""}`).then((res) => {
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
    toPascalCase(param) {
      let lowerParam = param.toLowerCase();
      let result = lowerParam.replace(/^./, lowerParam[0].toUpperCase());
      return result;
    },
    filterQueryFunc(dataCheckList) {
      console.log("@filterQueryFunc", dataCheckList);
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
      // this.requestParam.moldId = this.mold.id;
      this.requestParam.chartDataType = chartDataOptions;
      if (!this.isSelectedCustomRange) {
        delete this.requestParam.startDate;
        delete this.requestParam.endDate;
      }
    },
    convertDate(time) {
      console.log("@convertDate", time);
      return moment(time * 1000).format("YYYY-MM-DD HH:mm");
    },
    changeSwitchTab(id) {
      console.log("@changeSwitchTab", id);
      const el = document.querySelector(this.getRootToCurrentId() + "#" + id);
      el.classList.add("primary-animation-switch");
      setTimeout(() => {
        this.switchTab = id;
        el.classList.remove("primary-animation-switch");
      }, 700);
    },
    isNotCMSCounter() {
      //   return this.mold?.counterCode?.slice(0, 3) !== "CMS";
      return this.listSelected?.some(
        (mold) => mold?.counterCode?.slice(0, 3) !== "CMS"
      );
    },
    isNotNewGenCounter() {
      //   return this.mold?.counterCode?.slice(0, 3) !== "NCM";
      return this.listSelected?.some(
        (mold) => mold?.counterCode?.slice(0, 3) !== "NCM"
      );
    },
    /*isAbb() {
      axios.get("/api/users/server").then((response) => {
        if (response.data !== "abb-pilot") {
          this.isAbbGermany = false;
        } else {
          this.isAbbGermany = true;
        }
      });
    },*/
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
        for (let day = rangeDay[0]; day <= dayOfMonth; day++) {
          days.push(day);
        }
      } else if (month === rangeMonth[1] && year === rangeYear[1]) {
        for (let day = 1; day <= rangeDay[1]; day++) {
          days.push(day);
        }
      } else {
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
    changeMonthOrDay(type) {
      if (this.isSelectedCustomRange) {
        return;
      }
      const dpicker = Common.vue.getChild(this.$children, "date-picker");
      if (type === "PRE") {
        dpicker.previous();
      } else {
        dpicker.next();
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
    initSelected() {
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

      this.cycleTimeSelected = [
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
          default: true,
          icon: this.cycleTimeImg,
          selected: true,
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
          icon: this.shotImg,
          selected: false,
        },
      ];

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
          icon: "/images/graph/approve-icon.svg",
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
    },
    async reLoadChartGraph() {
      let requestParam = {};
      if (
        !this.isSelectedCustomRange &&
        this.requestParam.year &&
        !this.yearSelected
      ) {
        this.yearSelected = this.requestParam.year;
      }
      if (this.requestParam.dateViewType === "HOUR") {
        requestParam = {
          ...this.requestParam,
          year: this.requestParam.year || this.yearSelected,
          date: this.isSelectedCustomRange ? "" : this.getCurrentDate(),
        };
      } else {
        requestParam = {
          ...this.requestParam,
          year: this.requestParam.year || this.yearSelected,
        };
      }
      requestParam.isSelectedCustomRange = this.isSelectedCustomRange;
      this.loadGraphPage(0, 0);
      this.loadDetailGraphPage(0, 0);
      const listExistedTooling = this.listSelected.filter(
        (mold) => mold && mold.id
      );
      for (let i = 0; i < listExistedTooling.length; i++) {
        const mold = listExistedTooling[i];
        this.loadChart(
          mold,
          { ...requestParam, moldId: mold.id },
          this.chartType,
          this.optimalCycleTime,
          this.graphSettingDataList,
          this.chartTypeConfig,
          this.startDate
        );
      }
    },
    async loadChart(
      mold,
      requestParam,
      chartType,
      oct,
      graphSettingDataList,
      chartTypeConfig,
      startDateAcceleraion
    ) {
      console.log(
        "@loadChart",
        mold,
        requestParam,
        chartType,
        oct,
        graphSettingDataList,
        chartTypeConfig,
        startDateAcceleraion
      );
      var intervalId = setInterval(async () => {
        console.log("check load chart");
        const child = this?.$refs[`chart-${mold.id}`]?.[0];
        if (child != null) {
          await child.initGraphData(oct, chartTypeConfig);
          child.loadChartGraph(
            mold,
            requestParam,
            chartType,
            oct,
            graphSettingDataList,
            startDateAcceleraion
          );
          clearInterval(intervalId);
        }
      }, 100);
    },
    loadGraphPage(currentPage, totalPage) {
      console.log("loadGraphPage:", currentPage, totalPage);
      this.chartPage = {
        ...this.chartPage,
        currentPage: currentPage,
        totalPage: totalPage,
      };
    },
    loadDetailGraphPage(currentPage, totalPage) {
      console.log("loadCustomGraphPage:", currentPage, totalPage);
      this.chartPage = {
        ...this.chartPage,
        detailCurrentPage: currentPage,
        detailTotalPage: totalPage,
      };
    },
    onShowFrequentlyChart(isShow) {
      console.log("onShowFrequentlyChart", isShow);
      this.isShowFrequentlyChart = isShow;
    },

    onChartPageNext() {
      if (this.isShowFrequentlyChart) {
        this.chartPage.detailCurrentPage++;
      } else if (
        this.isSelectedCustomRange &&
        !["CYCLE_TIME_ANALYSIS", "TEMPERATURE_ANALYSIS"].includes(
          this.chartType
        )
      ) {
        this.chartPage.currentPage++;
      }
    },
    onChartPagePre() {
      if (this.isShowFrequentlyChart) {
        this.chartPage.detailCurrentPage--;
      } else if (
        this.isSelectedCustomRange &&
        !["CYCLE_TIME_ANALYSIS", "TEMPERATURE_ANALYSIS"].includes(
          this.chartType
        )
      ) {
        this.chartPage.currentPage--;
      }
    },

    showChart(dataType, dateViewType, tab, oct) {
      console.log("show chart");
      this.isShowChart = false;
      //   this.mold = mold;
      if (oct) this.optimalCycleTime = oct;
      this.initSelected();
      this.switchTab = "switch-graph";

      this.requestParam = {
        year: moment().format("YYYY"),
        chartDataType: "QUANTITY",
        dateViewType: dateViewType,
        detailDate: "",
        sort: "id,asc",
        size: "50",
      };
      this.isShowDateWeekMonth = true;
      this.listSelected.forEach((mold) => {
        if (mold && !mold.uptimeTarget) {
          mold.uptimeTarget = 90;
        }
      });

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
          icon: "/images/graph/max-icon.svg",
        },
        {
          id: 3,
          name: "Min CT",
          code: "MIN_CYCLE_TIME",
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
          icon: "/images/graph/approve-icon.svg",
        },
      ];

      this.rangeYear = [];
      this.rangeMonth = [];
      this.yearSelected = moment().format("YYYY");
      if (dateViewType === "HOUR" && this.isNotNewGenCounter()) {
        this.requestParam.dateViewType = "DAY";
      }
      // let param = { moldIdList: this.listSelected.map(t => t.id) };
      const moldIdList = this.listSelected
        .filter((mold) => mold && mold.id)
        .map((mold) => mold.id);

      const param = Common.param({ moldIdList });
      axios
        .get(`/api/statistics/time-range-list${param ? "?" + param : ""}`)
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
          this.requestParam.month = this.rangeMonth[1];
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
          //   this.requestParam.moldId = this.mold.id;
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
          this.getCurrentChartSettings();

          this.isShowChart = true;
        });

      this.listSelected.forEach((mold) => {
        if (mold && mold.id) this.findMoldParts(mold);
      });
      // $(this.getRootToCurrentId()).modal("show");

      this.getChartTypeConfig();
    },
    changeChartDataType(dataType) {
      this.chartType = dataType;
      let oldDateViewType = this.requestParam.dateViewType;
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
        this.requestParam.dateViewType = "DAY";
      }
      if (this.requestParam.dateViewType == "HOUR") {
      }

      if (
        oldDateViewType !== this.requestParam.dateViewType &&
        this.requestParam.dateViewType == "DAY"
      ) {
        delete this.requestParam.day;
        if (this.requestParam.month == null) {
          this.requestParam.month = this.rangeMonth[1];
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
      } else {
        this.reLoadChartGraph();
        this.reloadChangeParam(true);
      }
    },
    dateViewType(viewType) {
      this.requestParam.dateViewType = viewType;
      this.reLoadChartGraph();
      this.reloadChangeParam();
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
    getParam(requestParam) {
      let paramNew = {};
      switch (requestParam.dateViewType) {
        case "HOUR":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: this.requestParam.chartDataType,
            date: this.getCurrentDate(),
            // moldId: this.requestParam.moldId,
          };
          break;
        case "DAY":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            month: requestParam.month,
            // moldId: requestParam.moldId,
          };
          break;
        case "MONTH":
        case "WEEK":
          paramNew = {
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            // moldId: requestParam.moldId,
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
    reloadChangeParam(isChangeTab) {
      if (!this.isSelectedCustomRange && this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }
      this.setDayWhenChangeMonth(this.monthHour);
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
    getResetIcon() {
      let iconUrl = "/images/icon/reset-point-icon2.svg";
      let icon = new Image();
      icon.src = iconUrl;
      icon.width = 20;
      icon.height = 20;
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
    findMoldParts(mold) {
      axios.get("/api/molds/" + mold?.id + "/parts").then((response) => {
        console.log("response::::::::::::::::::::::::::::", response);
        // this.parts = response.data.content;
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
          this.numberOfPart[id] = res.data.data?.numberOfPart || 0;
          this.singlePartName[id] = res.data.data?.singlePartName;
          this.partChangeData[id] = res.data.data?.list;
        })
        .finally(() => {
          this.loadingPartChange = false;
        });
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
          } else {
            if (response.data) {
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
    handleChangeDate(data) {
      if (data.isCustomRange) {
        if (data.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
        } else {
          this.requestParam.dateViewType = data.selectedType.replace("LY", "");
        }
        this.requestParam.startDate = data.range.startDate;
        this.requestParam.endDate = data.range.endDate;
        this.requestParam.year = "";
        this.requestParam.month = "";
        this.yearSelected = "";
        this.isSelectedCustomRange = true;
      } else {
        this.requestParam.dateViewType = data.selectedType.replace("LY", "");
        this.requestParam.year = data.time.slice(0, 4);
        delete this.requestParam.month;
        delete this.requestParam.day;
        delete this.requestParam.startDate;
        delete this.requestParam.endDate;
        if (data.selectedType == "HOURLY") {
          this.requestParam.month = data.singleDate.slice(4, 6);
          this.requestParam.day = moment(data.time, "YYYYMMDD").format("DD");
        } else if (data.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.requestParam.month = data.singleDate.slice(4, 6);
        } else {
        }
        this.isSelectedCustomRange = false;
      }

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
        if (data.selectedType == "HOURLY") {
          this.monthHour = data.singleDate.slice(4, 6);
          startDate = moment(data.time, "YYYYMMDD").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMMDD")
            .add(1, "d")
            .format("YYYYMMDDHHmmss");
          this.day = moment(data.time, "YYYYMMDD").format("DD");
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;
        } else if (data.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.monthHour = data.singleDate.slice(4, 6);
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;

          startDate = moment(data.time, "YYYYMM").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMM")
            .add(1, "M")
            .format("YYYYMMDDHHmmss");
        } else if (
          data.selectedType == "WEEKLY" ||
          data.selectedType == "MONTHLY"
        ) {
          startDate = moment(data.time, "YYYY").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYY")
            .add(1, "y")
            .format("YYYYMMDDHHmmss");
        }
      }
      this.reLoadChartGraph();
      this.reloadChangeParam();

      this.listSelected.forEach((mold) => {
        if (mold && mold.id) this.getPartChanges(mold?.id, startDate, endDate);
      });
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
    handleCloseTooling(mold) {
      this.listSelected = this.listSelected.map((item, index) => {
        if (item?.id === mold.id) return { tempIndex: index };
        else return item;
      });
    },
    async fetchMolds() {
      try {
        console.log("@fetchMolds");
        this.loading = true;
        const query = Common.param({
          ...this.requestParam,
          size: 100000,
          sort: "equipmentCode,asc",
          filterCode: "COMMON",
        });
        const response = await axios.get("/api/common/flt/molds?" + query);
        this.listToolings = response.data.content.map((item) => ({
          ...item,
          label: item.equipmentCode,
        }));
      } catch (error) {
        console.log(error);
      } finally {
        console.log("loading end");
        this.loading = false;
      }
    },
    handleSelectTooling(mold, tempIndex) {
      console.log("mold", mold);
      console.log("tempIndex", tempIndex);

      this.listSelected[tempIndex] = { ...mold, tempIndex };
      this.listAvailableToolings = this.listToolings.filter(
        (mold) =>
          !this.listSelected.some((selected) => selected?.id === mold.id)
      );
      console.log("@select", this.listSelected);

      let requestParam = {};
      if (
        !this.isSelectedCustomRange &&
        this.requestParam.year &&
        !this.yearSelected
      ) {
        this.yearSelected = this.requestParam.year;
      }
      if (this.requestParam.dateViewType === "HOUR") {
        requestParam = {
          ...this.requestParam,
          year: this.requestParam.year || this.yearSelected,
          date: this.isSelectedCustomRange ? "" : this.getCurrentDate(),
        };
      } else {
        requestParam = {
          ...this.requestParam,
          year: this.requestParam.year || this.yearSelected,
        };
      }
      requestParam.isSelectedCustomRange = this.isSelectedCustomRange;
      this.loadChart(
        mold,
        { ...requestParam, moldId: mold.id },
        this.chartType,
        this.optimalCycleTime,
        this.graphSettingDataList,
        this.chartTypeConfig,
        this.startDate
      );
      this.findMoldParts(mold);
      this.getPartChanges(
        mold?.id,
        this.requestParam.startDate,
        this.requestParam.endDate
      );
    },
  },
  mounted() {
    console.log("multi chart mounted");
    this.getCfgStp();
    // this.getChartTypeConfig();
    // this.fetchMolds()
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
    listCheckedFull(newVal) {
      for (let i = 0; i < 4; i++) {
        this.listSelected[i] = { ...newVal[i], tempIndex: i };
      }
      console.log("listCheckedFull>>>listSelected", this.listSelected);
    },
    show(newVal) {
      if (newVal) {
        this.showChart("QUANTITY", "DAY", "switch-graph");
        this.fetchMolds();
      }
    },
    octs(newVal) {
      this.optimalCycleTime = newVal;
    },
    // listSelected: {
    //   handler(newVal, oldVal) {
    //     this.listAvailableToolings = this.listToolings.filter(mold => !newVal.some(selected => selected?.id === mold.id))
    //     console.log('listSelected Change', newVal)
    //     const oldArrId = oldVal.map(i => i?.id)
    //     const newArrId = newVal.map(i => i?.id)
    //     console.log('oldArrId', oldArrId)
    //     console.log('newArrId', newArrId)
    //     for (let i = 0; i < 4; i++) {
    //       if (oldArrId[i] !== newArrId[i] && newArrId[i]) {
    //         const mold = newVal[i]
    //         console.log('mold', mold)
    //         let requestParam = {};
    //         if (!this.isSelectedCustomRange && this.requestParam.year && !this.yearSelected) {
    //           this.yearSelected = this.requestParam.year;
    //         }
    //         if (this.requestParam.dateViewType === "HOUR") {
    //           requestParam = {
    //             ...this.requestParam,
    //             year: this.requestParam.year || this.yearSelected,
    //             date: this.isSelectedCustomRange ? "" : this.getCurrentDate(),
    //           };
    //         } else {
    //           requestParam = {
    //             ...this.requestParam,
    //             year: this.requestParam.year || this.yearSelected,
    //           };
    //         }
    //         requestParam.isSelectedCustomRange = this.isSelectedCustomRange;
    //         this.loadChart(mold, { ...requestParam, moldId: mold.id }, this.chartType, this.optimalCycleTime, this.graphSettingDataList, this.chartTypeConfig, this.startDate);
    //         this.findMoldParts(mold)
    //         this.getPartChanges(mold?.id, this.requestParam.startDate, this.requestParam.endDate)
    //       }
    //     }
    //   },
    //   deep: true
    // },
    partChangeData(newVal) {
      console.log("partChangeData", newVal);
    },
    numberOfPart(newVal) {
      console.log("numberOfPart", newVal);
    },
    singlePartName(newVal) {
      console.log("singlePartName", newVal);
    },
    listToolings(newVal) {
      console.log("listToolings", newVal);
      this.listAvailableToolings = newVal.filter(
        (mold) =>
          !this.listSelected.some((selected) => selected?.id === mold.id)
      );
      console.log("listAvailableToolings", this.listAvailableToolings);
    },
  },
};
</script>

<style
  cus-no-cache
  src="/components/chart-mold/detail-graph/chart-mold-golbal.css"
></style>
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
  color: var(--black);
  margin-right: 6px;
  font-weight: 800;
}

.info-zone .info-zone_tooling {
  margin-right: 43px;
}
#hourly-chart-wrapper {
  position: static !important;
}

.multi-chart-container {
  background-color: #ffffff;
}

.multi-chart-filter {
  margin-top: 13px;
}

.multi-chart-content {
  margin-top: 17px;
}

.multi-chart-content__row {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.multi-chart-content__row > * {
  width: calc(50% - 1px);
  flex-grow: 0;
}

.multi-chart-content__item {
  position: relative;
  background-color: #d6dade;
  min-height: 367.58px;
  padding: 17.15px 34.07px 13.44px 26.68px;
  border-radius: 3px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.multi-chart-content__item.empty {
  background-color: #ffffff;
}

.multi-chart-content__graph {
  background-color: #ffffff;
  border-radius: 3px;
  width: 100%;
}

.info-zone {
  margin-top: 0;
  padding-top: 4px;
  position: relative;
  top: 10px;
  z-index: 1;
}

.info-zone .info-zone_tooling {
  font-size: 12px;
  min-width: fit-content;
  padding-left: 8px;
  margin-right: 0;
}

.info-zone .info-zone_tooling span {
  padding-right: 0;
  padding-left: 0;
  background-color: unset;
}

.info-zone_produce {
  font-size: 12px;
  flex: 1;
  text-align: right;
  padding-right: 6px;
}

.multi-chart-content__close {
  position: absolute;
  vertical-align: middle;
  text-align: center;
  margin-left: auto;
  width: 26.68px;
  height: 26.68px;
  top: 1.73px;
  right: 0.23px;
  cursor: pointer;
}

.graph-header {
  margin: 0;
}
.select-date-group {
  display: flex;
}

table.custom-table-tooltip th,
table.custom-table-tooltip td {
  background: #4b4b4b;
  border-radius: 3px;
}

.multi-chart-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  margin-bottom: 20px;
}
</style>
