<template>
  <div class="tol-adt-main-container">
    <div class="tol-adt-header-container">
      <div class="tol-adt-header-left-wrapper">
        <master-filter-wrapper
          :filter-code="filterCode"
          :notify-filter-updated="() => getToolingAuditData('master-filter')"
        ></master-filter-wrapper>
      </div>

      <div class="tol-adt-header-right-wrapper">
        <emdn-search-bar
          :set-search-complete-keyword="setSearchCompleteKeyword"
          placeholder-text="Search by tooling details"
          style-props="width: 18.25rem"
        ></emdn-search-bar>
        <emdn-tooltip color="white">
          <template #context>
            <emdn-icon
              button-type="restore-icon"
              style-props="width: 1.875rem; height: 1.875rem"
              :click-handler="resetPageHandler"
            ></emdn-icon>
          </template>
          <template #body>
            <span style="white-space: nowrap">Reset Page</span>
          </template>
        </emdn-tooltip>
      </div>
    </div>

    <div class="tol-adt-body-container">
      <emdn-widget-group
        :style-props="{
          gap: '0.75rem',
        }"
        :columns="widgetColumnSize"
        :on-size-change="onSizeChange"
      >
        <emdn-widget-area
          :id="widgetSizeList[0].id"
          :size="widgetSizeList[0].size"
        >
          <emdn-widget
            :header-text="widgetSizeList[0].title"
            style-props="height: 100%"
          >
            <template #tooltip>
              <emdn-tooltip position="bottom" style-props="width: 12.5rem">
                <template #context>
                  <emdn-icon button-type="information-icon"></emdn-icon>
                </template>
                <template #body>
                  <div class="tol-adt-tooltip-body-wrapper">
                    <p>{{ widgetInfoTextList.toolingDistribution }}</p>
                  </div>
                </template>
              </emdn-tooltip>
            </template>
            <template #body>
              <!-- microsoft bingmap -->
              <emdn-bingmap-provider v-if="isChinaTimezone">
                <emdn-bingmap
                  :pushpin-list="pushpinList"
                  :pushpin-event="{ click: pushpinClickHandler }"
                ></emdn-bingmap>
              </emdn-bingmap-provider>
              <!-- google map -->
              <emdn-googlemap-provider v-else>
                <emdn-googlemap
                  :marker-list="googleMarkerList"
                  :marker-event="{ click: googleMarkerClickHandler }"
                ></emdn-googlemap>
              </emdn-googlemap-provider>
              <!-- (emoldino-woojin) bingmap 중국 서비스 종료 시 사용하기 위해 amchart4 map 임시주석 처리 -->
              <!-- <div style="overflow: hidden" ref="am-map"></div> -->
            </template>
          </emdn-widget>
        </emdn-widget-area>
        <emdn-widget-area
          :id="widgetSizeList[1].id"
          :size="widgetSizeList[1].size"
        >
          <emdn-widget
            :header-text="widgetSizeList[1].title"
            style-props="height: 100%;"
          >
            <template #tooltip>
              <emdn-tooltip position="bottom" style-props="width: 12.5rem">
                <template #context>
                  <emdn-icon button-type="information-icon"></emdn-icon>
                </template>
                <template #body>
                  <div class="tol-adt-tooltip-body-wrapper">
                    <p>
                      {{ widgetInfoTextList.overallUtilization[0] }}
                    </p>
                    <p>
                      {{ widgetInfoTextList.overallUtilization[1] }}
                    </p>
                  </div>
                </template>
              </emdn-tooltip>
            </template>
            <template #body>
              <div class="tol-adt-progress-bar-container">
                <div
                  class="tol-adt-progress-bar-wrapper"
                  v-for="(item, index) in overAllUtilizationList"
                  :key="index"
                >
                  <emdn-cta-button
                    :click-handler="() => overallUtilizationHandler(item.code)"
                    :active="utilizationStatus === item.code"
                    :class="{
                      'tol-adt-progress-button-active':
                        utilizationStatus === item.code,
                    }"
                    :style-props="'width: 5rem; height: 1rem; margin: 0; box-shadow: 0 0.063rem 0.25rem rgba(0, 0, 0, 0.25); font-size: 0.875rem; white-space: nowrap;'"
                    >{{ item.title }}
                  </emdn-cta-button>
                  <div class="tol-adt-progress-bar-styles">
                    <emdn-progress-bar
                      :value="item.value"
                      :buffer-value="item.totalValue"
                      :bg-color="item.bgColor"
                      :border-color="
                        item.borderColor ? item.borderColor : undefined
                      "
                    >
                      <template #value
                        ><span>{{ item.value }}</span></template
                      >
                    </emdn-progress-bar>
                  </div>
                </div>
              </div>
            </template>
          </emdn-widget>
        </emdn-widget-area>
        <emdn-widget-area
          :id="widgetSizeList[2].id"
          :size="widgetSizeList[2].size"
        >
          <emdn-widget
            :header-text="widgetSizeList[2].title"
            style-props="height: 100%;"
          >
            <template #tooltip>
              <emdn-tooltip position="bottom" style-props="width: 12.5rem">
                <template #context>
                  <emdn-icon button-type="information-icon"></emdn-icon>
                </template>
                <template #body>
                  <div class="tol-adt-tooltip-body-wrapper">
                    <p>{{ widgetInfoTextList.toolingStatus[0] }}</p>
                    <p>{{ widgetInfoTextList.toolingStatus[1] }}</p>
                  </div>
                </template>
              </emdn-tooltip>
            </template>
            <template #body>
              <div class="tol-adt-progress-bar-container">
                <div
                  class="tol-adt-progress-bar-wrapper"
                  v-for="(item, index) in toolingStatusList"
                  :key="index"
                >
                  <emdn-cta-button
                    :click-handler="() => toolingStatusHandler(item.code)"
                    :active="toolingStatus === item.code"
                    :class="{
                      'tol-adt-progress-button-active':
                        toolingStatus === item.code,
                    }"
                    style-props="width: 5rem; height: 1rem; margin: 0; box-shadow: 0 0.063rem 0.25rem rgba(0, 0, 0, 0.25); font-size: 0.875rem; white-space: nowrap;"
                    >{{ item.title }}
                  </emdn-cta-button>
                  <div class="tol-adt-progress-bar-styles">
                    <emdn-progress-bar
                      :value="item.value"
                      :buffer-value="item.totalValue"
                      :bg-color="item.bgColor"
                      :border-color="
                        item.borderColor ? item.borderColor : undefined
                      "
                    >
                      <template #value
                        ><span>{{ item.value }}</span></template
                      >
                    </emdn-progress-bar>
                  </div>
                </div>
              </div>
            </template>
          </emdn-widget>
        </emdn-widget-area>
        <emdn-widget-area
          :id="widgetSizeList[3].id"
          :size="widgetSizeList[3].size"
        >
          <div class="tol-adt-table-container">
            <div class="tol-adt-table-wrapper">
              <emdn-data-table :scrollable="true" ref="tableRef">
                <template #thead>
                  <tr ref="tableHeaderRef">
                    <th
                      v-for="(tableHeader, index) in mainTableHeaderList"
                      :key="index"
                      style="z-index: 10; padding: 0.5rem 0.75rem 0.5rem 1rem"
                    >
                      <div
                        class="tol-adt-th-wrapper"
                        :style="{ justifyContent: tableHeader.justifyContent }"
                      >
                        <span>{{ tableHeader.title }}</span>
                        <div
                          class="tol-adt-table-sort-icon"
                          @click.prevent.stop="sortTable(tableHeader.key)"
                        >
                          <emdn-icon
                            :button-type="
                              tableSort === `${tableHeader.key},asc`
                                ? 'sort-asce'
                                : 'sort-desc'
                            "
                            style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.125rem;"
                            :active="tableSort.includes(tableHeader.key)"
                          ></emdn-icon>
                        </div>
                      </div>
                    </th>
                  </tr>
                </template>
                <template #tbody>
                  <div
                    v-if="!tableContent.length"
                    class="tol-adt-table-empty"
                    :style="{
                      height: `calc(100% - ${getTableHeaderHeight()}px)`,
                    }"
                  >
                    No Data Available
                  </div>
                  <template v-else>
                    <tr v-for="item in tableContent">
                      <td>
                        <div
                          v-if="item.moldCode?.length > 15"
                          style="width: fit-content"
                        >
                          <emdn-tooltip
                            position="right"
                            style-props="white-space: nowrap;"
                          >
                            <template #context>
                              <span>{{ truncateText(item.moldCode, 15) }}</span>
                            </template>
                            <template #body>
                              <span>{{ item.moldCode }}</span>
                            </template>
                          </emdn-tooltip>
                        </div>
                        <span v-else>{{ item.moldCode }}</span>
                      </td>
                      <td>
                        <div
                          v-if="item.locationName?.length > 15"
                          style="width: fit-content"
                        >
                          <emdn-tooltip
                            position="right"
                            style-props="white-space: nowrap;"
                          >
                            <template #context>
                              <span>{{
                                truncateText(item.locationName, 15)
                              }}</span>
                            </template>
                            <template #body>
                              <span>{{ item.locationName }}</span>
                            </template>
                          </emdn-tooltip>
                        </div>
                        <span v-else>{{ item.locationName }}</span>
                      </td>
                      <td>
                        <div
                          v-if="item.areaName?.length > 15"
                          style="width: fit-content"
                        >
                          <emdn-tooltip
                            position="right"
                            style-props="white-space: nowrap;"
                          >
                            <template #context>
                              <span>{{ truncateText(item.areaName, 15) }}</span>
                            </template>
                            <template #body>
                              <span>{{ item.areaName }}</span>
                            </template>
                          </emdn-tooltip>
                        </div>
                        <span v-else>{{ item.areaName }}</span>
                      </td>
                      <td>
                        <div>
                          <emdn-table-status :category="item.toolingStatus" />
                        </div>
                      </td>
                      <td style="text-align: right">
                        <span>
                          {{
                            item.utilizationRate
                              ? `${item.utilizationRate}%`
                              : "-"
                          }}
                        </span>
                      </td>
                      <td>
                        <div>
                          <emdn-cta-button
                            variant="text"
                            style-props="padding: 0; height: fit-content;"
                            :click-handler="() => getRelocationHistory(item.id)"
                          >
                            {{ item.relocatedDateTime }}
                          </emdn-cta-button>
                        </div>
                      </td>
                    </tr>
                  </template>
                </template>
              </emdn-data-table>
            </div>
            <!--TODO(emoldino.jun): Need to Fix - Table Layout-->
            <div style="height: 0.5rem; flex-shrink: 0"></div>
            <div class="tol-adt-pagination-wrapper">
              <emdn-pagination
                :current-page="pageNumber"
                :total-page="totalPages"
                :next-click-handler="paginationHandler"
                :previous-click-handler="paginationHandler"
              >
              </emdn-pagination>
            </div>
          </div>
        </emdn-widget-area>
        <emdn-widget-area
          :id="widgetSizeList[4].id"
          :size="widgetSizeList[4].size"
          :style="{
            paddingBottom: windowSize === 'large' ? '2.375rem' : '1.875rem',
          }"
        >
          <emdn-widget
            :header-text="widgetSizeList[4].title"
            style-props="height: 100%;"
          >
            <template #tooltip>
              <emdn-tooltip position="bottom" style-props="width: 12.5rem">
                <template #context>
                  <emdn-icon button-type="information-icon"></emdn-icon>
                </template>
                <template #body>
                  <div class="tol-adt-tooltip-body-wrapper">
                    <p>
                      {{ widgetInfoTextList.plantArea[0] }}
                    </p>
                    <p>
                      {{ widgetInfoTextList.plantArea[1] }}
                    </p>
                  </div>
                </template>
              </emdn-tooltip>
            </template>
            <template #body>
              <div class="tol-adt-progress-bar-container">
                <div
                  class="tol-adt-progress-bar-wrapper"
                  v-for="(item, index) in plantAreaList"
                  :key="index"
                >
                  <emdn-cta-button
                    :click-handler="() => plantAreaHandler(item.code)"
                    :active="plantAreaStatus === item.code"
                    :class="{
                      'tol-adt-progress-button-active':
                        plantAreaStatus === item.code,
                    }"
                    :style-props="'width: 5rem; height: 1rem; margin: 0; box-shadow: 0 0.063rem 0.25rem rgba(0, 0, 0, 0.25); font-size: 0.875rem; white-space: nowrap;'"
                    >{{ item.title }}
                  </emdn-cta-button>
                  <div class="tol-adt-progress-bar-styles">
                    <emdn-progress-bar
                      :value="item.value"
                      :buffer-value="item.totalValue"
                      :bg-color="item.bgColor"
                      :border-color="
                        item.borderColor ? item.borderColor : undefined
                      "
                    >
                      <template #value
                        ><span>{{ item.value }}</span></template
                      >
                    </emdn-progress-bar>
                  </div>
                </div>
              </div>
            </template>
          </emdn-widget>
        </emdn-widget-area>
      </emdn-widget-group>
    </div>

    <emdn-modal
      :is-opened="isRelocationModalOpened"
      :heading-text="'Relocation History'"
      :modal-handler="relocationModalHandler"
    >
      <template #body>
        <div class="tol-adt-modal-header">
          <span>Tooling ID</span>
          <span>{{ historyMoldCode }}</span>
        </div>
        <emdn-timeline-stepper
          v-for="(item, index) in relocationHistoryList"
          :key="index"
          :step="getStepValue(item, index, relocationHistoryList.length)"
          :last="relocationHistoryList.length - 1 === index"
          :opposite="true"
        >
          <template #content>
            <div class="timeline-content">
              <div :style="{ fontWeight: 'bold', height: '1.125rem' }">
                {{ item.relocatedDateTime }}
              </div>
              <!-- table status 에 CHANGED 추가하기 -->
              <emdn-table-status
                :category="item.relocationStatus"
              ></emdn-table-status>
              <table>
                <tr>
                  <th>Plant</th>
                  <td>{{ item.locationName }}</td>
                  <th>Area</th>
                  <td>{{ item.areaName }}</td>
                </tr>
                <tr>
                  <th>Company</th>
                  <td>{{ item.companyName }}</td>
                </tr>
              </table>
            </div>
          </template>
        </emdn-timeline-stepper>
      </template>
      <template #footer>
        <emdn-pagination
          v-if="historyTotalPage > 1"
          :current-page="historyPageNumber"
          :total-page="historyTotalPage"
          :disabled="false"
          :next-click-handler="historyPaginationHandler"
          :previous-click-handler="historyPaginationHandler"
        >
        </emdn-pagination>
      </template>
    </emdn-modal>
  </div>
</template>

<script>
const initialWidgetSizeList = [
  {
    id: "1",
    size: 2,
    title: "Tooling Distribution",
  },
  {
    id: "2",
    size: 1,
    title: "Overall Utilization",
  },

  {
    id: "3",
    size: 1,
    title: "Tooling Status",
  },

  {
    id: "4",
    size: 3,
    title: "",
  },

  {
    id: "5",
    size: 1,
    title: "Plant Area",
  },
];

module.exports = {
  name: "audit-page",
  components: {
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      // google map
      googleMarkerList: [],
      // bing map
      pushpinList: [],

      isChinaTimezone: false,
      actionState: "",
      amChart: null,
      googleMap: "",
      polygonSeries: "",
      markerSeries: "",
      windowSize: "",
      widgetColumnSize: 4,
      widgetSizeList: initialWidgetSizeList,
      mainTableHeaderList: [
        {
          id: 1,
          title: "Tooling ID",
          key: "moldCode",
          justifyContent: "flex-start",
        },
        {
          id: 2,
          title: "Current Plant",
          key: "locationName",
          justifyContent: "flex-start",
        },
        {
          id: 3,
          title: "Plant Area",
          key: "areaName",
          justifyContent: "flex-start",
        },
        {
          id: 4,
          title: "Tooling Status",
          key: "toolingStatus",
          justifyContent: "flex-start",
        },
        {
          id: 5,
          title: "Utilization Rate",
          key: "utilizationRate",
          justifyContent: "flex-end",
        },
        {
          id: 6,
          title: "Relocation Details",
          key: "relocatedDateTime",
          justifyContent: "flex-start",
        },
      ],
      tableSort: "",
      isTableSortByDesc: true,
      isRelocationModalOpened: false,
      historyPageNumber: 1,
      historyTotalPage: 0,
      historyPageLast: false,
      historyMoldId: 0,
      historyMoldCode: "",

      // parameters ========================================================================

      filterCode: "COMMON", // currently not use
      locationId: null,
      toolingStatus: "",
      utilizationStatus: "",
      plantAreaStatus: "",
      searchQuery: "",
      pageNumber: 1,

      // response (output) =================================================================

      // tooling audit response
      tableContent: [],
      distributions: [],
      utilizationSummary: {
        high: 0,
        low: 0,
        medium: 0,
        prolonged: 0,
        total: 0,
      },
      statusSummary: {
        idle: 0,
        inProduction: 0,
        inactive: 0,
        noSensor: 0,
        onStandby: 0,
        sensorDetached: 0,
        sensorOffline: 0,
        total: 0,
      },
      areaSummary: {
        maintenance: 0,
        production: 0,
        total: 0,
        unknown: 0,
        warehouse: 0,
      },
      relocationHistoryList: [],

      // pagination response
      totalPages: 1,

      iconUrl: "/images/icon/note-restore.svg",

      widgetInfoTextList: {
        toolingDistribution:
          "This widget gives you a quick view of your tooling distribution in plants around the world.",
        overallUtilization: [
          "This widget is calculated by taking the current cumulative shot count and dividing it by the forecasted maximum shots.",
          "Utilization rate is divided into 4 categories: Low, Medium, High, and Prolonged.",
        ],
        toolingStatus: [
          "In this widget, the system groups the current tooling status into seven categories: In Production, Idle, Inactive, Detached, Offline, On Standby, and No Sensor.",
          "You can configure the definitions of the Tooling Status in System Settings.",
        ],
        plantArea: [
          "This widget gives you an overview of your toolings categorized by plant area.",
          "You can assign each terminal to a plant area from the terminal ‘edit’ form.",
        ],
      },
    };
  },
  computed: {
    toolingStatusList() {
      return [
        {
          title: "Total Tooling",
          code: "total",
          value: this.statusSummary.total,
          totalValue: this.statusSummary.total,
          bgColor: "#1A2281",
        },
        {
          title: "In Production",
          code: "inProduction",
          value: this.statusSummary.inProduction,
          totalValue: this.statusSummary.total,
          bgColor: "#41CE77",
        },
        {
          title: "Idle",
          code: "idle",
          value: this.statusSummary.idle,
          totalValue: this.statusSummary.total,
          bgColor: "#F7CC57",
        },
        {
          title: "Inactive",
          code: "inactive",
          value: this.statusSummary.inactive,
          totalValue: this.statusSummary.total,
          bgColor: "#707070",
        },
        {
          title: "Detached",
          code: "sensorDetached",
          value: this.statusSummary.sensorDetached,
          totalValue: this.statusSummary.total,
          bgColor: "#FFFFFF",
          borderColor: "#DB3B21",
        },
        {
          title: "Offline",
          code: "sensorOffline",
          value: this.statusSummary.sensorOffline,
          totalValue: this.statusSummary.total,
          bgColor: "#DB3B21",
        },
        {
          title: "On Standby",
          code: "onStandby",
          value: this.statusSummary.onStandby,
          totalValue: this.statusSummary.total,
          bgColor: "#FFFFFF",
          borderColor: "#4B4B4B",
        },
        {
          title: "No Sensor",
          code: "noSensor",
          value: this.statusSummary.noSensor,
          totalValue: this.statusSummary.total,
          bgColor: "#8B8B8B",
        },
      ];
    },
    overAllUtilizationList() {
      return [
        {
          title: "Total Tooling",
          code: "total",
          value: this.utilizationSummary.total,
          totalValue: this.utilizationSummary.total,
          bgColor: "#1A2281",
        },
        {
          title: "Low",
          code: "low",
          value: this.utilizationSummary.low,
          totalValue: this.utilizationSummary.total,
          bgColor: "#41CE77",
        },
        {
          title: "Medium",
          code: "medium",
          value: this.utilizationSummary.medium,
          totalValue: this.utilizationSummary.total,
          bgColor: "#F7CC57",
        },
        {
          title: "High",
          code: "high",
          value: this.utilizationSummary.high,
          totalValue: this.utilizationSummary.total,
          bgColor: "#C92617",
        },
        {
          title: "Prolonged",
          code: "prolonged",
          value: this.utilizationSummary.prolonged,
          totalValue: this.utilizationSummary.total,
          bgColor: "#7A2E17",
        },
      ];
    },
    plantAreaList() {
      return [
        {
          title: "Total Tooling",
          code: "total",
          value: this.areaSummary.total,
          totalValue: this.areaSummary.total,
          bgColor: "#1A2281",
        },
        {
          title: "Production",
          code: "productionArea",
          value: this.areaSummary.production,
          totalValue: this.areaSummary.total,
          bgColor: "#41CE77",
        },
        {
          title: "Warehouse",
          code: "warehouse",
          value: this.areaSummary.warehouse,
          totalValue: this.areaSummary.total,
          bgColor: "#4EBCD5",
        },
        {
          title: "Maintenance",
          code: "maintenanceArea",
          value: this.areaSummary.maintenance,
          totalValue: this.areaSummary.total,
          bgColor: "#1A2281",
        },
        {
          title: "Unknown",
          code: "unknown",
          value: this.areaSummary.unknown,
          totalValue: this.areaSummary.total,
          bgColor: "#8B8B8B",
        },
      ];
    },
    selectedDateDisplayName() {
      return Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.tempTimeScale
      );
    },
  },
  methods: {
    getToolingAuditData(param) {
      this.actionState = param;
      let utilizationStatusParam =
        !this.utilizationStatus || this.utilizationStatus === "total"
          ? ""
          : `&utilizationStatus=${Common.camelToSnake(
              this.utilizationStatus
            ).toUpperCase()}`;

      let toolingStatusParam =
        !this.toolingStatus || this.toolingStatus === "total"
          ? ""
          : `&toolingStatus=${Common.camelToSnake(
              this.toolingStatus
            ).toUpperCase()}`;

      let pageNumberParam = this.pageNumber ? `&page=${this.pageNumber}` : "";

      let locationIdParam = this.locationId
        ? `&locationId=${this.locationId}`
        : "";

      let areaTypeParam =
        !this.plantAreaStatus || this.plantAreaStatus === "total"
          ? ""
          : `&areaType=${Common.camelToSnake(
              this.plantAreaStatus
            ).toUpperCase()}`;

      let sortParam = `&sort=${this.tableSort}`;

      let searchQueryParam = `&query=${this.searchQuery}`;

      let url =
        "/api/asset/tol-adt?timeScale=DATE" +
        utilizationStatusParam +
        toolingStatusParam +
        areaTypeParam +
        pageNumberParam +
        locationIdParam +
        sortParam +
        searchQueryParam;

      axios.get(url).then((res) => {
        if (!res.data.content.length) {
          this.totalPages = 1;
          this.pageNumber = 1;
        } else {
          this.totalPages = res.data.totalPages;
        }
        this.tableContent = res.data.content;
        this.totalElements = res.data.totalElements;
        this.numberOfElements = res.data.numberOfElements;
        this.utilizationSummary = res.data.utilizationSummary;
        this.statusSummary = res.data.statusSummary;
        this.areaSummary = res.data.areaSummary;

        console.log("this.utilizationSummary", this.utilizationSummary);
        console.log("this.statusSummary", this.statusSummary);
        console.log("this.areaSummary", this.areaSummary);

        this.size = res.data.size;
        this.first = res.data.first;
        this.last = res.data.last;
        if (
          this.actionState === "mounted" ||
          this.actionState === "master-filter"
        ) {
          this.distributions = res.data.distributions;
          this.initMap();
        } else {
          this.updateMap(res.data.distributions);
        }
      });
    },
    getStepValue(item, index, listLength) {
      let result =
        item.relocationStatus === "CONFIRMED" ? "confirmed" : "unconfirmed";
      if (listLength - 1 === index) {
        result = "start";
      }
      // TODO: current 는 어떻게 구하지? 현재 api 에 current 를 나타내 줄 수 있는 게 없음. 문의필요 (steve님)
      // => 이번 popup 에서 current 를 보여주는 건 무의미하다는 결론.
      return result;
    },
    setSearchCompleteKeyword(keyword) {
      this.searchQuery = keyword;
      this.pageNumber = 1;
      this.getToolingAuditData();
    },
    relocationModalHandler() {
      this.isRelocationModalOpened = false;
    },
    getRelocationHistory(id) {
      if (id) {
        this.historyMoldId = id;
      }

      let pageNumberParam = this.historyPageNumber
        ? `?page=${this.historyPageNumber}`
        : "";
      let url =
        `/api/asset/tol-adt/${this.historyMoldId}/relocation-histories` +
        pageNumberParam;

      axios.get(url).then((res) => {
        this.relocationHistoryList = res.data.content;
        this.historyTotalPage = res.data.totalPages;
        this.historyPageLast = res.data.last;
        this.historyMoldId = res.data.moldId;
        this.historyMoldCode = res.data.moldCode;
        this.isRelocationModalOpened = true;
      });
    },
    resetPageHandler() {
      const searchBarComponent = this.$children.filter(
        (el) => el.$refs.searchInputRef
      )?.[0];
      const searchBarInputRef = searchBarComponent.$refs?.searchInputRef;
      searchBarInputRef.value = "";

      let newMarkerList = this.googleMarkerList.map((marker) => {
        marker.custom.disabled = false;
        return marker;
      });
      this.googleMarkerList.splice(0);
      this.googleMarkerList = newMarkerList;

      this.toolingStatus = "";
      this.utilizationStatus = "";
      this.plantAreaStatus = "";
      this.searchQuery = "";
      this.pageNumber = 1;
      this.locationId = 0;
      this.getToolingAuditData();
    },
    toolingStatusHandler(param) {
      this.pageNumber = 1;
      this.toolingStatus = param;
      this.getToolingAuditData();
    },
    overallUtilizationHandler(param) {
      this.pageNumber = 1;
      this.utilizationStatus = param;
      this.getToolingAuditData();
    },
    plantAreaHandler(param) {
      this.pageNumber = 1;
      this.plantAreaStatus = param;
      this.getToolingAuditData();
    },
    initMap() {
      this.isChinaTimezone = isChinaTimezone;
      if (isChinaTimezone) {
        this.initBingMap();
        // (emoldino-woojin) bingmap 중국 서비스 종료 시 사용하기 위해 amchart4 map 임시주석 처리
        // this.initAmchartMap();
      } else {
        this.initGoogleMap();
      }
    },
    initBingMap() {
      this.initBingPushpin();
    },
    initBingPushpin() {
      this.pushpinList.splice(0);
      this.distributions.map((distribution) => {
        if (
          distribution.latitude &&
          distribution.longitude &&
          distribution.locationName
        ) {
          // latitude, longitude, locationName 속성이 있을 경우 marker 생성
          this.pushpinList.push({
            location: {
              latitude: distribution.latitude,
              longitude: distribution.longitude,
            },
            options: {
              title: distribution.locationName,
            },
            custom: {
              data: {
                locationName: distribution.locationName,
              },
            },
          });
        }
      });
    },
    pushpinClickHandler({ event, pushpin, data }) {
      console.log("data: ", data);
      this.distributions.map((distribution) => {
        if (data.locationName === distribution.locationName) {
          this.locationId = distribution.locationId;
          this.pageNumber = 1;
          this.getToolingAuditData();
        }
      });
    },
    initGoogleMap() {
      this.initGoogleMarker(); // google marker 초기화
    },
    initGoogleMarker() {
      this.googleMarkerList.splice(0); // marker list 초기화
      // 좌표에 따라 marker list 재설정
      this.distributions.map((distribution) => {
        if (
          distribution.latitude &&
          distribution.longitude &&
          distribution.locationName &&
          distribution.locationId
        ) {
          // set marker
          let currentMarker = {
            custom: {
              data: {
                locationId: distribution.locationId,
              },
              disabled: false,
            },
            markerOptions: {
              position: {
                lat: distribution.latitude,
                lng: distribution.longitude,
              },
              title: distribution.locationName,
            },
          };
          this.googleMarkerList.push(currentMarker);
        }
      });
    },
    googleMarkerClickHandler({ data }) {
      let newMarkerList = this.googleMarkerList.map((marker) => {
        if (marker.custom.data.locationId === data.locationId) {
          marker.custom.disabled = false;
        } else {
          marker.custom.disabled = true;
        }
        return marker;
      });
      this.googleMarkerList.splice(0);
      this.googleMarkerList = newMarkerList;
      this.locationId = data.locationId;
      this.pageNumber = 1;
      this.getToolingAuditData();
    },
    async initAmchartMap() {
      am4core.useTheme(am4themes_animated); // Theme
      this.setAmChart();
      this.setAmChartReadyEvent();
      this.setAmchartMapPositionChanged(); // 지도를 이동하거나 확대/축소할 때 marker 위치를 다시 계산
      this.setPolygonSeries();
      this.setPolygonTemplate();
      this.setMarkerSeries();
    },
    setAmChart() {
      if (this.amChart) this.amChart.dispose();
      this.amChart = am4core.create(this.$refs["am-map"], am4maps.MapChart); // 지도 인스턴스 만들기
      this.amChart.geodata = am4geodata_worldLow; // 지도 정의 설정
      this.amChart.projection = new am4maps.projections.Mercator(); // 프로젝션 설정
      this.amChart.maxPanOut = 0; // 드래그를 map 영역 이상으로 되지 않도록 설정
    },
    setAmChartReadyEvent() {
      this.amChart.events.on("ready", this.updateAmChartMarkers, {
        passive: false,
      });
    },
    setAmchartMapPositionChanged() {
      this.amChart.events.on("mappositionchanged", this.updateAmChartMarkers, {
        passive: false,
      });
    },
    setPolygonSeries() {
      this.polygonSeries = this.amChart.series.push(
        new am4maps.MapPolygonSeries()
      );
      this.polygonSeries.useGeodata = true; // GeoJSON에서 지도 로드 폴리곤 데이터(상태 모양 및 이름) 만들기
      this.polygonSeries.exclude = ["AQ"]; // Remove Antarctica
    },
    setPolygonTemplate() {
      let polygonTemplate = this.polygonSeries.mapPolygons.template;
      polygonTemplate.fill = am4core.color("#579ffb");
      polygonTemplate.tooltipText = "{name}";
      polygonTemplate.nonScalingStroke = true;
      polygonTemplate.strokeWidth = 0.5;
      polygonTemplate.adapter.add("tooltipText", (text, target) => {
        let data = target.tooltipDataItem.dataContext;
        return `${data.name}: ${data.value > 0 ? data.value : 0}`;
      });
    },
    setMarkerSeries() {
      this.markerSeries = this.amChart.series.push(
        new am4maps.MapImageSeries()
      );
      this.markerSeries.mapImages.template.propertyFields.longitude =
        "longitude";
      this.markerSeries.mapImages.template.propertyFields.latitude = "latitude";
      this.markerSeries.mapImages.template.propertyFields.address = "address";
      this.markerSeries.data = this.distributions;
    },
    // 이 기능은 지도에서 현재 이미지를 가져오고 이를 위한 HTML 요소를 생성합니다.
    updateAmChartMarkers() {
      // 모든 이미지를 통과
      this.markerSeries.mapImages.each((marker) => {
        if (marker.longitude && marker.latitude) {
          // 해당 HTML 요소가 있는지 확인하십시오.
          if (!marker.dummyData || !marker.dummyData.externalElement) {
            //오넥스 만들기
            marker.dummyData = {
              externalElement: this.createAmchartMarker(marker),
            };
          }
          // 좌표에 따라 요소 위치 변경
          let xy = this.amChart.geoPointToSVG({
            longitude: marker.longitude,
            latitude: marker.latitude,
          });
          marker.dummyData.externalElement.style.top = xy.y + "px";
          marker.dummyData.externalElement.style.left = xy.x + "px";
        }
      });
    },
    // 이 함수는 새 마커 요소를 생성하고 반환합니다.
    createAmchartMarker(marker) {
      let chart = marker.dataItem.component.chart;

      let currentMarker = document.createElement("div"); // create marker
      currentMarker.className = "amchart-map-marker"; // set class name

      let tooltip = document.createElement("div"); // create tooltip
      tooltip.className = "amchart-map-marker-tooltip";

      // set marker title
      if (marker.dataItem.dataContext?.locationName) {
        currentMarker.dataset.title = marker.dataItem.dataContext.locationName;
        tooltip.innerHTML = marker.dataItem.dataContext.locationName;
      }

      currentMarker.appendChild(tooltip);

      if (!this.amchartSvgContainerElement) {
        this.amchartSvgContainerElement = chart.svgContainer.htmlElement;
      }

      this.amchartMarkerClickHandler(currentMarker); // amchart marker click

      this.amchartSvgContainerElement.appendChild(currentMarker); // 지도 컨테이너에 마커 추가

      return currentMarker;
    },
    amchartMarkerClickHandler(currentMarker) {
      currentMarker.onclick = () => {
        this.distributions.map((distribution) => {
          if (currentMarker.dataset.title === distribution.locationName) {
            this.locationId = distribution.locationId;
            this.pageNumber = 1;
            this.getToolingAuditData();
          }
        });
      };
    },
    updateMap(distributions) {
      if (isChinaTimezone) {
        this.updateBingMap(distributions);
        // this.updateAmchartMap(distributions);
      } else {
        this.updateGoogleMap(distributions);
      }
    },
    updateAmchartMap(distributions) {
      let selectedMarkerList = [];
      for (let markerNode of this.amchartSvgContainerElement.childNodes) {
        if (markerNode.classList.contains("amchart-map-marker")) {
          // markerNode 의 클래스에 "amchart-map-marker" 가 있다면 opaicty 를 0.2로 전시
          markerNode.style.opacity = 0.2;
          markerNode.classList.remove("focus");
          // title 이 일치하면 opaicty를 1로 전시
          distributions.map((distribution) => {
            if (distribution.locationName === markerNode.dataset.title) {
              markerNode.style.opacity = 1;
              selectedMarkerList.push(markerNode);
            }
          });
        }
      }
      // 선택된 marker 가 한개 일 때만 focus 클래스 추가
      if (selectedMarkerList.length === 1) {
        selectedMarkerList[0].classList.add("focus");
      }
    },
    updateGoogleMap(distributions) {
      let newMarkerList = this.googleMarkerList.map((marker) => {
        marker.custom.disabled = true;
        return marker;
      });
      distributions.map((distribution) => {
        newMarkerList.map((marker) => {
          if (distribution.locationId === marker.custom.data.locationId) {
            marker.custom.disabled = false;
          }
        });
      });
      this.googleMarkerList.splice(0);
      this.googleMarkerList = newMarkerList;
    },
    updateBingMap(distributions) {
      let newPushpinList = this.pushpinList.map((pushpin) => {
        pushpin.custom.disabled = true;
        return pushpin;
      });

      distributions.map((distribution) => {
        newPushpinList.map((pushpin) => {
          if (distribution.locationName === pushpin.custom.data.locationName) {
            pushpin.custom.disabled = false;
          }
        });
      });

      this.pushpinList.splice(0);
      this.pushpinList = newPushpinList;
    },
    paginationHandler(pagination) {
      this.scrollToTopForMainTable();
      this.pageNumber = pagination.currentPage;
      this.getToolingAuditData();
    },
    scrollToTopForMainTable() {
      const tableWrapperRef = this.$refs.tableRef?.tableWrapper;

      if (
        tableWrapperRef &&
        tableWrapperRef.scrollHeight > tableWrapperRef.clientHeight
      ) {
        tableWrapperRef.scrollTo({ top: 0, behavior: "smooth" });
      }
    },
    historyPaginationHandler(pagination) {
      this.historyPageNumber = pagination.currentPage;
      this.getRelocationHistory();
    },
    sortTable(key) {
      this.isTableSortByDesc = !this.isTableSortByDesc;
      this.tableSort = `${key},${this.isTableSortByDesc ? "desc" : "asc"}`;
      this.getToolingAuditData();
      this.scrollToTopForMainTable();
    },
    truncateText(text, length) {
      if (text && text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    getTableHeaderHeight() {
      return this.$refs.tableHeaderRef?.getBoundingClientRect().height;
    },
    onSizeChange(size) {
      this.windowSize = size;

      if (size === "large") {
        this.widgetColumnSize = 4;
        this.widgetSizeList = initialWidgetSizeList;
      }
      if (size === "medium") {
        const sizeList = [2, 1, 1, 2, 1];
        this.widgetColumnSize = 2;
        this.widgetSizeList = initialWidgetSizeList.map((item, index) => {
          return {
            ...item,
            size: sizeList[index],
          };
        });
      }
    },
  },
  mounted() {
    window.addEventListener("resize", () => {
      this.getTableHeaderHeight();
    });
    this.getToolingAuditData("mounted");
  },
  destroyed() {
    window.removeEventListener("resize", () => {
      this.getTableHeaderHeight();
    });
  },
};
</script>

<style scoped>
.tol-adt-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 2rem 1.25rem;
}

.tol-adt-header-container {
  width: 100%;
  height: 3.25rem;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-shrink: 0;
}

.tol-adt-header-left-wrapper,
.tol-adt-header-right-wrapper {
  width: fit-content;
  height: fit-content;
  align-items: center;
}

.tol-adt-header-right-wrapper {
  display: flex;
  gap: 0.5rem;
}

.tol-adt-body-container {
  width: 100%;
  height: 100%;
  min-height: 0;
}

/* amchart map marker */
.amchart-map-marker {
  position: absolute;
  width: 1.125rem;
  height: 1.563rem;
  background: url("/images/icon/map-maker.svg");
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  box-sizing: border-box;
  cursor: pointer;
}

/* .amchart-map-marker:hover {
  -webkit-filter: brightness(150%);
} */
/* amchart map marker tooltip */
.amchart-map-marker-tooltip {
  width: max-content;
  background: white;
  padding: 0.313rem;
  position: absolute;
  top: -2.188rem;
  left: -100%;
  border: solid 0.063rem #ddd;
  border-radius: 0.188rem;
  display: none;
}

.amchart-map-marker:hover .amchart-map-marker-tooltip {
  display: block;
}

.amchart-map-marker.focus .amchart-map-marker-tooltip {
  display: block;
}

.timeline-content {
  padding-bottom: 1.875rem;
}

.timeline-content > div {
  margin-bottom: 0.625rem;
}

.timeline-content table tr {
  height: 1.875rem;
}

.timeline-content table th {
  min-width: 9.375rem;
}

.timeline-content table td {
  min-width: 15.625rem;
  background: #fbfcfd;
}

.tol-adt-main-container {
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
  padding: 2rem 1.25rem;
}

#google-map {
  width: 100%;
  height: 100%;
}

.tol-adt-progress-bar-container {
  padding: 1.375rem 0.625rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  height: auto;
  gap: 0.5rem;
}

.tol-adt-progress-bar-styles {
  flex: 1;
  display: flex;
  align-items: center;
}

.tol-adt-progress-bar-wrapper {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 0.25rem;
}

.tol-adt-progress-bar-wrapper .tol-adt-progress-button-active {
  background: #3491ff;
  border: 0.031rem solid #d0d0d0;
  border-radius: 0.188rem;
  color: #ffffff !important;
}

.tol-adt-progress-bar-wrapper .tol-adt-progress-button-active:hover {
  background: #3585e5 !important;
}

.tol-adt-tooltip-body-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tol-adt-tooltip-body-wrapper > p {
  text-align: left;
}

.tol-adt-table-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tol-adt-table-wrapper {
  height: 100%;
  width: 100%;
  overflow: hidden;
  flex-grow: 1;
}

.tol-adt-table-wrapper td {
  vertical-align: top;
  font-size: 0.916rem;
}

.tol-adt-th-wrapper {
  display: flex;
  align-items: center;
}

.tol-adt-table-sort-icon {
  display: flex;
  align-items: center;
  width: fit-content;
  height: 100%;
  cursor: pointer;
}

.tol-adt-pagination-wrapper {
  width: 100%;
  height: 1.875rem;
  display: flex;
  justify-content: end;
  align-items: center;
  flex-shrink: 0;
}

.tol-adt-table-empty {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #efefef;
  position: absolute;
  font-family: Helvetica Neue, Helvetica, Arial, sans-serif;
  font-size: 0.75rem;
  color: #4b4b4b;
}

.tol-adt-modal-header {
  width: 100%;
  height: 1.75rem;
  display: flex;
  align-items: center;
  gap: 3rem;
  font-size: 0.916rem;
  color: #4b4b4b;
  margin-bottom: 4rem;
}

.tol-adt-modal-header span:first-of-type {
  font-weight: 700;
}
</style>
