<template>
  <div>
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
                <a-badge
                  :count="totalUnreadNumber"
                  :overflow-count="99"
                  :number-style="{ backgroundColor: '#C90065' }"
                >
                  <div
                    id="switch-notes"
                    :class="{ active: switchTab === 'switch-notes' }"
                    @click="changeSwitchTab('switch-notes')"
                  >
                    <span v-text="_.upperFirst(resources['notes'])"></span>
                  </div>
                </a-badge>
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
              @click="handleClose"
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
                  <template v-if="isNotCMSCounter()">
                    <label
                      class="btn switch-btn"
                      v-bind:class="{
                        active: chartType === 'CYCLE_TIME_ANALYSIS',
                      }"
                      @click.prevent="
                        changeChartDataType('CYCLE_TIME_ANALYSIS')
                      "
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
                      @click.prevent="
                        changeChartDataType('TEMPERATURE_ANALYSIS')
                      "
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
                  <label
                    v-if="
                      accelerationList.length > 0 &&
                      isNotNewGenCounter() &&
                      (currentUser.email === 'mason.lee@emoldino.com' ||
                        currentUser.email === 'jason.kim@emoldino.com' ||
                        currentUser.email === 'richard.hong@emoldino.com' ||
                        currentUser.email === 'david.holden@emoldino.com' ||
                        currentUser.email === 'usman@emoldino.com')
                    "
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
                  </label>
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
                                replaceLongtext(
                                  partChangeData[0].reportedBy,
                                  40
                                )
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

            <div
              style="
                display: flex;
                align-items: center;
                justify-content: space-between;
                min-height: 475px;
              "
            >
              <div
                v-show="!isShowFrequentlyChart && !isSelectedCustomRange"
                style="display: inline-block"
              >
                <a-icon
                  v-on:click="changeMonthOrDay('PRE')"
                  v-if="
                    (requestParam.dateViewType === 'DAY' ||
                      requestParam.dateViewType === 'HOUR') &&
                    !disablePre()
                  "
                  style="fontsize: 23px"
                  class="icon"
                  type="caret-left"
                ></a-icon>
                <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              </div>
              <div
                v-show="isShowFrequentlyChart || isSelectedCustomRange"
                style="display: inline-block"
              >
                <a-icon
                  v-on:click="onChartPagePre()"
                  v-if="
                    (requestParam.dateViewType === 'DAY' ||
                      requestParam.dateViewType === 'HOUR') &&
                    hasChartPagePre
                  "
                  style="fontsize: 23px"
                  class="icon"
                  type="caret-left"
                ></a-icon>
                <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              </div>

              <chart-graph
                :id="'chart-' + requestParam.moldId"
                :resources="resources"
                style="width: 100%"
                @on-show-frequently-chart="onShowFrequentlyChart"
                @graph-page="loadGraphPage"
                :current-page="chartPage.currentPage"
                @load-detail-graph-page="loadDetailGraphPage"
                :detail-current-page="chartPage.detailCurrentPage"
              >
              </chart-graph>

              <div
                v-show="!isShowFrequentlyChart && !isSelectedCustomRange"
                style="display: inline-block"
              >
                <a-icon
                  v-on:click="changeMonthOrDay('NEXT')"
                  v-if="
                    (requestParam.dateViewType === 'DAY' ||
                      requestParam.dateViewType === 'HOUR') &&
                    !disableNext()
                  "
                  style="fontsize: 23px"
                  class="icon"
                  type="caret-right"
                ></a-icon>
                <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              </div>
              <div
                v-show="isShowFrequentlyChart || isSelectedCustomRange"
                style="display: inline-block"
              >
                <a-icon
                  v-on:click="onChartPageNext()"
                  v-if="
                    (requestParam.dateViewType === 'DAY' ||
                      requestParam.dateViewType === 'HOUR') &&
                    hasChartPageNext
                  "
                  style="fontsize: 23px"
                  class="icon"
                  type="caret-right"
                ></a-icon>
                <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              </div>
            </div>
          </div>
          <div
            v-show="switchTab === 'switch-detail'"
            style="padding: 20px 56px"
            class="modal-body"
          >
            <div class="d-flex justify-space-between">
              <div style="margin-right: 19px">
                <base-images-slider
                  :images="mold.attachment"
                  :key="viewImageKey"
                  :clear-key="clearImageKey"
                  :handle-submit-file="handleSubmitImage"
                ></base-images-slider>
              </div>
              <div>
                <tooling-detail
                  :show-file-previewer="showFilePreviewer"
                  :resources="resources"
                  :mold="mold"
                  :user-type="userTypeVal"
                  :show-histories-data-submission="showHistoriesDataSubmission"
                  :mold-pm-plan="moldPmPlan"
                  :style-custom="{
                    overflow: 'scroll',
                    height: '600px',
                    width: '580px',
                    'padding-right': '10px',
                  }"
                ></tooling-detail>
              </div>
            </div>
          </div>
          <div
            v-show="switchTab === 'switch-notes'"
            style="padding: 20px 56px; overflow: scroll; height: 630px"
            class="modal-body"
          >
            <note-tab
              ref="note-tab"
              :resources="resources"
              :current-user="currentUser"
              :notes="notes"
              :total-notes="notesTotal"
              :is-dont-show-warning-again="isDontShowWarningAgain"
              :is-show="switchTab === 'switch-notes'"
              :object-function="mold"
              system-note-function="TOOLING_SETTING"
              type="TOOLING"
              :is-loading="loadingNote"
              :get-system-note-data="getSystemNoteData"
              @change-tab="changeNoteTab"
              :handle-submit="handleSubmit"
            ></note-tab>
          </div>
        </div>
      </div>
    </div>

    <submission-history
      :resources="resources"
      @back="backToMoldDetail"
    ></submission-history>
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
    handleSubmit: Function,
  },
  components: {
    "tooling-detail": httpVueLoader("/components/tooling-detail.vue"),
    "date-picker": httpVueLoader("/components/mold-popup/date-picker.vue"),
    "data-filter": httpVueLoader("/components/dfilter/data-filter.vue"),
    /*
    "bubble-chart": httpVueLoader("/components/amchart5/BubbleChart.vue"),
*/
    "chart-graph": httpVueLoader(
      "/components/chart-mold/detail-graph/chart-graph.vue"
    ),
    "submission-history": httpVueLoader(
      "/components/data-submission-history.vue"
    ),
    "preview-note-card": httpVueLoader(
      "/components/@base/notes/preview-note-card.vue"
    ),
    "note-full-view": httpVueLoader(
      "/components/@base/notes/note-full-view.vue"
    ),
    "add-note-view": httpVueLoader("/components/@base/notes/add-note-view.vue"),
    "note-tab": httpVueLoader("/components/chart-mold/note-tab.vue"),
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  data() {
    return {
      accelerationList: [],
      startDate: moment(),
      bubbleChartComponentKey: 0,
      /*
      amchartData: {},
*/
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
      // optimalCycleTime: "Approved CT",
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
      /*
      chartTemperature: {},
*/
      shotImg: "",
      cycleTimeImg: "",
      uptimeImg: "",
      customRangeData: {
        allData: [],
        page: 0,
      },
      /*
      noDataAvailable: false,
      chart: null,
      frequentlyChart: null,
*/
      graphSettingDataList: [],
      chartPage: {
        currentPage: 0,
        totalPage: 1,
        detailCurrentPage: 0,
        detailTotalPage: 1,
      },
      // userTypeVal:null,
      currentUser: {},
      isDontShowWarningAgain: false,
      showAddNoteView: false,
      notes: [],
      notesTotal: 0,
      loadingNote: false,
      param: {
        moldId: "",
        systemNoteId: "",
        systemNoteFunction: "",
      },
      totalUnreadNumber: 0,
      viewImageKey: 0,
      clearImageKey: 0,
      moldPmPlan: {},
    };
  },
  methods: {
    handleClose() {
      console.log("handleClose");
    },
    async fetchWorkOrderDetail(workOrderId) {
      try {
        const response = await axios.get(`/api/work-order/${workOrderId}`);
        return response.data.data;
      } catch (error) {
        console.log(error);
      } finally {
      }
    },
    async handleSubmitImage(file) {
      const refId = this.mold.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.mold.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "MOLD_CONDITION");
      formData.append("refId", `${refId}`);
      return formData;
    },
    async getMoldAttachment() {
      try {
        const type = {
          storageType: "MOLD_CONDITION",
          refId: this.mold.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.mold.attachment = res.data;
        this.viewImageKey++;
        console.log("getMoldAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
    },
    // handleRemoveImage(newImages) {
    //   console.log('handleRemoveImage', newImages)
    //   this.mold.attachment = newImages
    // },
    async getMoldById(id) {
      try {
        const res = await axios.get(`/api/molds/${id}`);
        return res.data;
      } catch (error) {}
      return {};
    },
    async findNoteById(id) {
      try {
        const res = await axios.get(`/api/system-note/${id}/detail`);
        const find = res.data;
        console.log("findNoteById", find, id, this.notes);
        return find;
      } catch (error) {
        console.log(error);
      }
      return {};
    },
    changeNoteTab(type, countTotal) {
      const isDeleted = type === "DELETED";
      this.loadingNote = true;
      let params = {
        objectFunctionId: this.mold.id,
        systemNoteFunction: "TOOLING_SETTING",
        trashBin: isDeleted,
      };
      params = Common.param(params);
      axios
        .get(`/api/system-note/list?${params}`)
        .then((response) => {
          this.notes = response.data.dataList;
          this.checkIsShowWarningAgain();
          if (countTotal) {
            this.notesTotal = response.data.total;
            this.totalUnreadNumber = _.sumBy(
              this.notes,
              (item) => item.numUnread
            );
          }
          this.loadingNote = false;
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    async getSystemNoteData() {
      this.loadingNote = true;
      let params = {
        objectFunctionId: this.mold.id,
        systemNoteFunction: "TOOLING_SETTING",
        trashBin: false,
      };
      params = Common.param(params);
      try {
        const response = await axios.get(`/api/system-note/list?${params}`);
        this.notes = response.data.dataList;
        this.notesTotal = response.data.total;
        this.totalUnreadNumber = _.sumBy(this.notes, (item) => item.numUnread);
        this.checkIsShowWarningAgain();
        this.loadingNote = false;
      } catch (error) {
        this.loadingNote = false;
      }
    },
    checkIsShowWarningAgain() {
      axios
        .get("/api/on-boarding/get?feature=SHOW_WARNING_DELETE_NOTE")
        .then((response) => {
          this.isDontShowWarningAgain = response.data?.data?.seen;
        });
    },
    async getCurrentUser() {
      try {
        const me = await Common.getSystem("me");
        this.currentUser = JSON.parse(me);
      } catch (error) {
        console.log(error);
      }
    },
    showHistoriesDataSubmission: function (mold) {
      console.log("mold: ", mold);

      var child = Common.vue.getChild(this.$children, "submission-history");
      if (child != null) {
        $(this.getRootToCurrentId() + "#op-chart-mold").modal("hide");
        child.show(
          {
            ...mold,
            userType: this.userTypeVal,
          },
          "chart-mold"
        );
      }
    },
    backToMoldDetail(fromPopup) {
      console.log("back to molds detail");
      if (fromPopup == "chart-mold") this.backFromPreview();
    },
    /*
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
*/
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
          this.graphSettingDataList = res.data.data;
          this.reLoadChartGraph();
        });
    },
    updateCurrentChartSettings(data) {
      console.log("updateCurrentChartSettings", data);
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
              if (res.data?.data) {
                this.graphSettingDataList = res.data.data;
                this.reLoadChartGraph();
              }
            });
        }, 300);
      }
    },
    backFromPreview() {
      $(this.getRootToCurrentId() + "#op-chart-mold").modal("show");
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
    },
    convertDate(time) {
      return moment(time * 1000).format("YYYY-MM-DD HH:mm");
    },
    changeSwitchTab(id) {
      const el = document.querySelector(
        this.getRootToCurrentId() + "#op-chart-mold " + "#" + id
      );
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
    changeMonthOrDay(type) {
      if (this.isSelectedCustomRange) {
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
    /*
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
*/

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
    async reLoadChartGraph() {
      let requestParam = {};
      // let originParam = JSON.parse(JSON.stringify(this.requestParam));
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
      this.loadChart(
        this.mold,
        requestParam,
        this.chartType,
        this.optimalCycleTime,
        this.graphSettingDataList,
        this.chartTypeConfig,
        this.startDate
      );
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
      var intervalId = setInterval(async () => {
        var child = Common.vue.getChild(this.$children, "chart-graph");
        console.log("check load chart", chartType);
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

    async showChart(mold, dataType, dateViewType, tab, oct) {
      // this.getChartTypeConfig();
      let moldPmPlanResponse = await axios.get(
        `/api/molds/${mold.id}/mold-pm-plan`
      );
      let toolingData = await this.getMoldById(mold.id);
      this.moldPmPlan = moldPmPlanResponse?.data?.data;
      this.isShowChart = false;
      this.mold = { ...mold, ...toolingData };
      if (mold.lastWorkOrderId) {
        const workOrder = await this.fetchWorkOrderDetail(mold.lastWorkOrderId);
        console.log("fetchWorkOrderDetail", workOrder);
        this.mold.lastWorkOrder = workOrder;
        // this.mold = { ...this.mold, ...{ lastWorkOrder: workOrder } };
        console.log("fetchWorkOrderDetail finish", mold);
      }
      if (oct) this.optimalCycleTime = oct;
      this.initSelected();
      // this.getCurrentChartSettings();
      this.switchTab = tab ? tab : "switch-graph";

      if (mold && mold.id) {
        var child = Common.vue.getChild(this.$children, "tooling-detail");
        if (child != null) {
          child.showToolingDetail(mold);
        }
        this.clearImageKey++;
        await this.getMoldAttachment();
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
      if (
        dateViewType === "HOUR" &&
        mold.counterCode.slice(0, 3) !== "NCM" &&
        mold.counterCode.slice(0, 3) !== "EMA"
      ) {
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
          console.log("this.day: ", this.day);

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
      $(this.getRootToCurrentId() + "#op-chart-mold ").modal("show");
      this.getChartTypeConfig();
      await this.getSystemNoteData();
      if (this.switchTab === "switch-notes") {
        const noteTabChild = this.$refs["note-tab"];
        if (noteTabChild) {
          noteTabChild.changeShowAddNoteView(true);
        }
      }
    },
    changeChartDataType(dataType) {
      // this.isSelectedCustomRange = false;
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
      } else {
        //todo
        this.reLoadChartGraph();
        /*
        this.bindChartData(true);
*/
        this.reloadChangeParam(true);
      }
    },
    dateViewType(viewType) {
      this.requestParam.dateViewType = viewType;
      //todo: replace old graph
      this.reLoadChartGraph();
      this.reloadChangeParam();
      /*
      this.bindChartData();
*/
    },
    setDayWhenChangeMonth(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
          console.log("this.day: ", this.day);
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
          console.log("this.day: ", this.day);
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
                console.log("this.day: ", this.day);
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
      $(
        this.getRootToCurrentId() + "#op-chart-mold " + "#op-daily-details"
      ).show();
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
        if (data.selectedType == "HOURLY") {
          this.monthHour = data.singleDate.slice(4, 6);
          startDate = moment(data.time, "YYYYMMDD").format("YYYYMMDDHHmmss");
          endDate = moment(data.time, "YYYYMMDD")
            .add(1, "d")
            .format("YYYYMMDDHHmmss");
          // this.hourlyDate = data.time
          this.day = moment(data.time, "YYYYMMDD").format("DD");
          console.log("this.day: ", this.day);
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

      //todo: replace old graph
      this.reLoadChartGraph();
      this.reloadChangeParam();
      this.getPartChanges(this.mold.id, startDate, endDate);
    },
  },
  computed: {
    getNoteNotiNumber() {
      const total = _.sumBy(this.notes, (item) => item.numUnread);
      console.log("getNoteNotiNumber", total);
      return total;
    },
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

    optimalCycleTime() {
      return Common.octs[headerVm?.options?.OPTIMAL_CYCLE_TIME?.strategy];
    },

    userTypeVal() {
      return headerVm?.userType;
    },
  },
  mounted() {
    // this.getCfgStp();
    // this.getChartTypeConfig();
    // this.getUserType();
    this.getCurrentUser();
    this.$nextTick(function () {
      let notificationType = Common.getParameter("notificationType");
      if (!["INVITE_USER"].includes(notificationType)) {
        this.param.moldId = Common.getParameter("param");
        this.param.systemNoteId = Common.getParameter("systemNoteId");
        this.param.systemNoteFunction =
          Common.getParameter("systemNoteFunction");
      }
    });
  },
  watch: {
    monthHour(month) {
      if (month) {
        if (Number(month) === Number(this.rangeMonth[0])) {
          if (this.day < this.rangeDay[0]) this.day = this.rangeDay[0];
          console.log("this.day: ", this.day);
        }
        if (Number(month) === Number(this.rangeMonth[1])) {
          if (this.day > this.rangeDay[1]) this.day = this.rangeDay[1];
          console.log("this.day: ", this.day);
        }
        this.month = parseInt(month);
      }
    },
    "param.moldId": async function (newVal) {
      console.log("watch param.moldId", newVal);
      if (newVal && this.param.systemNoteFunction === "TOOLING_SETTING") {
        const mold = await this.getMoldById(newVal);
        await this.showChart(mold, "switch-notes");
        const selectedNote = await this.findNoteById(this.param.systemNoteId);
        this.switchTab = "switch-notes";
        const noteTabChild = this.$refs["note-tab"];
        if (noteTabChild) {
          await noteTabChild.onSelectViewNote(selectedNote);
          if (selectedNote.id !== this.param.systemNoteId) {
            console.log(
              "toggleShowAllReplies 1-1",
              this.param.systemNoteId,
              selectedNote.id
            );
            // noteTabChild.toggleShowAllReplies()
          }
        }
      }
    },
  },
};
</script>

<style
  cus-no-cache
  src="/components/chart-mold/detail-graph/chart-mold-golbal.css"
></style>
<style
  cus-no-cache
  scoped
  src="/components/chart-mold/detail-graph/chart-mold-scope.css"
></style>
