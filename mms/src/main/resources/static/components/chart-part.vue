<template>
  <div
    id="op-chart-part"
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
            <span class="span-title">{{ resources["part_qlty_report"] }}</span>
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
          >
            <span class="t-icon-close"></span>
          </span>
        </div>
        <div
          v-show="switchTab === 'switch-graph'"
          style="padding: 20px 56px; overflow: scroll"
          class="modal-body"
        >
          <div>
            <div style="align-items: center">
              <div class="row graph-header">
                <data-filter
                  class="select-graph-group"
                  :title="resources['graph_settings']"
                  :title-placeholder="resources['graph_settings']"
                  type="Graph Settings"
                  all-type="All graphs"
                  :resources="resources"
                  :option-array="graphOptions"
                  :selected-array="graphSelected"
                  @operatingstatus="filterQueryFunc"
                  @update-config="updateCurrentChartSettings"
                >
                </data-filter>
                <div class="select-date-group">
                  <date-picker
                    v-if="isShowChart"
                    id="part-front"
                    :resources="resources"
                    type="NO_HOURLY"
                    :default-selected-date="datePickerType"
                    :cancel-calendar="closeDatePicker"
                    :handle-submit="handleChangeDate"
                    :min-date="minDate"
                    :max-date="maxDate"
                    :current-date-data="currentDateData"
                  >
                  </date-picker>
                </div>
              </div>
              <div class="info-zone">
                <div class="item part-id">
                  <div class="head"><b v-text="resources['part_id']"></b></div>
                  <div class="desc-zone">
                    <div
                      v-if="part.partCode && part.partCode.length > 20"
                      class="desc"
                      v-bind:data-tooltip-text="part.partCode"
                    >
                      {{ replaceLongtext(part.partCode, 20) }}
                    </div>
                    <div v-else class="desc">
                      {{ part.partCode }}
                    </div>
                  </div>
                  <div class="head"><b v-text="resources['category']"></b></div>
                  <div class="desc-zone">
                    <div
                      v-if="part.categoryName && part.categoryName.length > 20"
                      class="desc"
                      v-bind:data-tooltip-text="part.categoryName"
                    >
                      {{ replaceLongtext(part.categoryName, 20) }}
                    </div>
                    <div v-else class="desc">
                      {{ part.categoryName }}
                    </div>
                  </div>
                </div>
                <div class="item part-name">
                  <div class="head">
                    <b v-text="resources['part_name']"></b>
                  </div>
                  <div v-if="part.partName" class="desc-zone">
                    <div
                      v-if="part.partName && part.partName.length > 20"
                      class="desc"
                      v-bind:data-tooltip-text="part.partName"
                    >
                      {{ replaceLongtext(part.partName, 20) }}
                    </div>
                    <div v-else class="desc">
                      {{ part.partName }}
                    </div>
                  </div>
                  <div v-else class="desc-zone">
                    <div
                      v-if="part.name && part.name.length > 20"
                      class="desc"
                      v-bind:data-tooltip-text="part.name"
                    >
                      {{ replaceLongtext(part.name, 20) }}
                    </div>
                    <div v-else class="desc">
                      {{ part.name }}
                    </div>
                  </div>
                  <div class="head"><b v-text="resources['project']"></b></div>
                  <div class="desc-zone">
                    <div
                      v-if="part.projectName && part.projectName.length > 20"
                      class="desc"
                      v-bind:data-tooltip-text="part.projectName"
                    >
                      {{ replaceLongtext(part.projectName, 20) }}
                    </div>
                    <div v-else class="desc">
                      {{ part.projectName }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="next-chart-container">
              <a-icon
                v-on:click="changeMonth('PRE')"
                v-if="
                  (requestParam.dateViewType === 'DAY' &&
                    !disablePre() &&
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
                  <canvas class="chart" id="chart-part"></canvas>
                </div>
                <div id="legend-chart-part"></div>
              </div>
              <a-icon
                v-on:click="changeMonth('NEXT')"
                v-if="
                  (requestParam.dateViewType === 'DAY' &&
                    !disableNext() &&
                    !this.isSelectedCustomRange) ||
                  hasCustomRangeNext
                "
                style="fontsize: 23px"
                class="icon"
                type="caret-right"
              ></a-icon>
              <div v-else style="width: 23px; height: 23px">{{ " " }}</div>
              <span
                v-if="noDataAvailable"
                style="
                  position: absolute;
                  left: 50%;
                  top: calc(50% + 20px);
                  transform: translate(-50%, -50%);
                "
                >No Data Available</span
              >
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
                :images="part.attachment"
                :key="viewImageKey"
                :clear-key="clearImageKey"
                :handle-submit-file="handleSubmitImage"
              ></base-images-slider>
            </div>
            <div>
              <part-detail-popup
                :resources="resources"
                :part="part"
                :show-file-previewer="showFilePreviewer"
                :style-custom="{
                  overflow: 'scroll',
                  height: '600px',
                  width: '520px',
                  'padding-right': '10px',
                }"
              >
              </part-detail-popup>
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
            system-note-function="PART_SETTING"
            :total-notes="notesTotal"
            :is-dont-show-warning-again="isDontShowWarningAgain"
            :is-show="switchTab === 'switch-notes'"
            :object-function="part"
            type="PART"
            :is-loading="loadingNote"
            :get-system-note-data="getSystemNoteData"
            @change-tab="changeNoteTab"
            :handle-submit="handleSubmit"
          ></note-tab>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
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
                '<img style="max-width: 50px" src="/images/graph/graph-setting-line.svg"/>';
              break;
            case "L1 (sec)":
              break;
            case "L2 (sec)":
              break;
            case "Within":
              legendIconHtml =
                '<img style="max-width: 50px" src="/images/graph/graph-setting-line.svg"/>';
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
          },
          scaleLabel: {
            display: true,
            labelString: "Total Part Quantity",
          },
        },
        {
          id: "second-y-axis",
          type: "linear",
          stacked: true,
          position: "right",
          ticks: {
            beginAtZero: true,
          },
          scaleLabel: {
            display: true,
            labelString: "Cycle Time",
          },
          gridLines: {
            display: false,
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
module.exports = {
  props: {
    resources: Object,
    showFilePreviewer: Function,
    handleSubmit: Function,
  },
  components: {
    "part-detail-popup": httpVueLoader("/components/part-detail-popup.vue"),
    "date-picker": httpVueLoader("/components/mold-popup/date-picker.vue"),
    "data-filter": httpVueLoader("/components/dfilter/data-filter.vue"),
    "note-tab": httpVueLoader("/components/chart-mold/note-tab.vue"),
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  data() {
    return {
      part: {},
      optimalCycleTime: "Approved CT",
      requestParam: {
        year: moment().format("YYYY"),
        month: moment().format("MM"),
        chartDataType: "QUANTITY",
        dateViewType: "DAY",
        moldId: "",
      },
      yearSelected: moment().format("YYYY"),
      // details
      results: [],
      total: 0,
      pagination: [],
      month: 1,
      day: 1,
      hour: "00",
      monthHour: "01",
      monthSelected: "01",
      rangeYear: [],
      rangeMonth: [],
      responseDataChart: [],
      checkedList: [], // 차트 표시 리스트
      showSelectGraph: false,
      graphSelected: [],
      graphOptions: [],
      isShowChart: false,
      xAxisTitleMapping: {
        HOUR: "Hour",
        DAY: "Date",
        WEEK: "Week",
        MONTH: "Month",
      },
      yAxisTitleMapping: {
        APPROVED_CT: this.optimalCycleTime,
        CYCLE_TIME: "Cycle Time",
        CAPACITY: "Maximum Capacity",
        QUANTITY: "Total Part Quantity",
      },
      switchTab: "switch-graph",
      datePickerType: "DAILY",
      currentDateData: null,
      minDate: null,
      maxDate: null,
      chartTypeConfig: {},
      shotImg: "",
      cycleTimeImg: "",
      totalPartImg: "",
      isSelectedCustomRange: false,
      customRangeData: {
        allData: [],
        page: 0,
      },
      noDataAvailable: false,
      currentUser: {},
      isDontShowWarningAgain: false,
      showAddNoteView: false,
      notes: [],
      notesTotal: 0,
      loadingNote: false,
      totalUnreadNumber: 0,
      param: {
        moldId: "",
        systemNoteId: "",
        systemNoteFunction: "",
      },
      viewImageKey: 0,
      clearImageKey: 0,
    };
  },
  methods: {
    async handleSubmitImage(file) {
      const refId = this.part.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.part.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "PART_PICTURE");
      formData.append("refId", `${refId}`);
      return formData;
    },

    async getPartAttachment() {
      try {
        const type = {
          storageType: "PART_PICTURE",
          refId: this.part.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.part.attachment = res.data;
        this.viewImageKey++;
        console.log("getPartAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
    },
    async getPartById(id) {
      try {
        const res = await axios.get(`/api/parts/${id}`);
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
        objectFunctionId: this.part.id,
        systemNoteFunction: "PART_SETTING",
        trashBin: isDeleted,
      };
      params = Common.param(params);
      axios
        .get(`/api/system-note/list?${params}`)
        .then((response) => {
          console.log("changeNoteTab", response);
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
        objectFunctionId: this.part.id,
        systemNoteFunction: "PART_SETTING",
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
    // async getCurrentUser() {
    //   try {
    //     const me = await Common.getSystem("me");
    //     this.currentUser = JSON.parse(me);
    //   } catch (error) {
    //     console.log(error);
    //   }
    // },
    backFromPreview() {
      $(this.getRootId() + "#op-chart-part").modal("show");
    },
    getCurrentChartSettings() {
      axios
        .get(`/api/config/graph-settings-config?chartDataType=PART_QUANTITY`)
        .then((res) => {
          let settings = { type: "", data: [] };
          for (let i = 0; i < res.data.data.length; i++) {
            let find = (element) =>
              element.code === res.data.data[i].graphItemType;
            this.graphOptions[this.graphOptions.findIndex(find)].selected =
              res.data.data[i].selected;
          }
          settings.data = this.graphOptions.filter((item) => item.selected);
          this.filterQueryFunc(settings);
        });
    },
    updateCurrentChartSettings(data) {
      if (data.data.length > 0) {
        let graphOptions = [
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
            name: "Maximum Capacity",
            code: "MAXIMUM_CAPACITY",
            icon: "/images/graph/max-capa.svg",
            selected: false,
          },
          {
            id: 3,
            name: "Total Part Quantity",
            code: "QUANTITY",
            default: true,
            icon: this.totalPartImg,
            selected: true,
          },
        ];
        for (let i = 0; i < data.data.length; i++) {
          let find = (element) => element.code === data.data[i].code;
          graphOptions[graphOptions.findIndex(find)].selected = true;
        }
        setTimeout(() => {
          let params = graphOptions.map((item) => {
            return {
              chartDataType: "PART_QUANTITY",
              graphItemType: item.code,
              selected: item.selected,
            };
          });
          axios
            .post("/api/config/graph-settings-config", params)
            .then((res) => {
              this.graphOptions = graphOptions;
            });
        }, 300);
      }
    },
    getChartTypeConfig() {
      axios.get("/api/common/dsp-stp").then((res) => {
        this.chartTypeConfig = res.data.chartTypeConfig;
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
        this.totalPartImg =
          this.chartTypeConfig.filter(
            (item) => item.chartDataType === "PART_QUANTITY"
          )[0].chartType === "LINE"
            ? "/images/graph/shot-icon.svg"
            : "/images/graph/graph-setting-rec.svg";
      });
    },
    toPascalCase(param) {
      let lowerParam = param.toLowerCase();
      let result = lowerParam.replace(/^./, lowerParam[0].toUpperCase());
      return result;
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
      console.log("months: ", months);
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
    setTooltipForMainChart(dateViewType, inputData) {
      return {
        enabled: true,
        footerFontStyle: "normal",
        intersect: true,
        callbacks: {
          afterLabel(tooltipItem) {
            const { index } = tooltipItem;
            let avgCavities = 0;
            if (inputData[index]) {
              avgCavities = inputData[index].avgCavities;
            }
            let avgCavityTitle = `Avg.Cavities: ${Number(avgCavities)
              .toFixed(2)
              .toString()
              .replace(/\B(?=(\d{3})+(?!\d))/g, ",")}`;
            return `${avgCavityTitle}`;
          },
          beforeFooter(tooltipItem) {
            const { index } = tooltipItem[0];
            let resetLabel = null;
            if (inputData[index]) {
              let reset = inputData[index].resetValue;
              let lastShot = inputData[index].lastShot;
              if (reset != null && reset !== 0) {
                dataValue = inputData[itemIndex].data;
                /*
                let resetTo= dataValue + reset;
*/
                resetLabel = `On this ${dateViewType.toLowerCase()}, the tooling was reset to ${lastShot} shots`;
              }
            }
            return resetLabel;
          },
          label(tooltipItem, data) {
            const label = data.datasets[tooltipItem.datasetIndex].label;
            const value = Number(tooltipItem.yLabel).toString();

            const { index } = tooltipItem;
            let dataValue = value;
            if (inputData[index]) {
              let reset = inputData[index].resetValue;
              if (reset != null && reset != 0) {
                dataValue =
                  inputData[index].resetValue > 0
                    ? inputData[index].data - inputData[index].resetValue
                    : dataValue;
              }
            }
            return `${label}: ${dataValue}`;
          },
        },
      };
    },
    filterQueryFunc(dataCheckList) {
      console.log("filterQueryFunc");
      const options = dataCheckList.data.map((g) => g.code);
      this.graphSelected = dataCheckList.data;
      this.bindChartData(options);
    },
    changeSwitchTab(id) {
      console.log("changeSwitchTab");
      const el = document.getElementById(id);
      el.classList.add("primary-animation-switch");
      setTimeout(() => {
        this.switchTab = id;
        el.classList.remove("primary-animation-switch");
      }, 700);
    },
    changeMonth(type) {
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

    disableNext: function () {
      const { rangeYear, rangeMonth } = this;
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
      const { rangeYear, rangeMonth } = this;
      if (rangeYear.length > 0 && rangeMonth.length > 0) {
        return (
          (this.month <= Number(rangeMonth[0]) &&
            Number(this.yearSelected) == Number(rangeYear[0])) ||
          Number(this.yearSelected) < Number(rangeYear[0])
        );
      }
      return false;
    },
    async showChartPartMold(part, mold, dataType, dateViewType, oct) {
      await this.showChartCommon(part, mold, dataType, dateViewType, oct);
    },
    showChart(part, dataType, dateViewType, oct, tab, isUsingNewNoteBatchApi) {
      this.showChartCommon(
        part,
        null,
        dataType,
        dateViewType,
        oct,
        tab,
        isUsingNewNoteBatchApi
      );
    },
    async showChartCommon(
      part,
      mold,
      dataType,
      dateViewType,
      oct,
      tab,
      isUsingNewNoteBatchApi
    ) {
      console.log("showChart", tab, part, isUsingNewNoteBatchApi);
      this.getChartTypeConfig();
      this.isShowChart = false;
      this.part = part;
      if (oct != null) this.optimalCycleTime = oct;
      this.graphOptions = [
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
          name: "Maximum Capacity",
          code: "MAXIMUM_CAPACITY",
          icon: "/images/graph/max-capa.svg",
          selected: false,
        },
        {
          id: 3,
          name: "Total Part Quantity",
          code: "QUANTITY",
          default: true,
          icon: this.totalPartImg,
          selected: true,
        },
      ];
      // this.getCurrentChartSettings();
      this.graphSelected = [
        {
          id: 3,
          name: "Total Part Quantity",
          code: "QUANTITY",
          default: true,
          icon: this.shotImg,
        },
      ];

      this.requestParam.partId = part.id;
      if (mold) this.requestParam.moldId = mold.id;
      else this.requestParam.moldId = "";
      this.requestParam.chartDataType = dataType;
      this.requestParam.dateViewType = dateViewType;

      this.rangeYear = [];
      this.rangeMonth = [];
      this.yearSelected = moment().format("YYYY");
      if (
        dateViewType === "HOUR" &&
        (mold == null ||
          mold.counterCode == null ||
          (mold.counterCode.slice(0, 3) !== "NCM" &&
            mold.counterCode.slice(0, 3) !== "EMA"))
      ) {
        this.requestParam.dateViewType = "DAY";
      }

      axios
        .get(`/api/statistics/time-range?partId=${part.id}`)
        .then((response) => {
          console.log("range-date: ", response);
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
          // this.bindChartData();
          this.isShowChart = true;
        });

      this.switchTab = tab ? tab : "switch-graph";
      if (this.switchTab === "switch-notes") {
        const noteTabChild = this.$refs["note-tab"];
        if (noteTabChild) {
          noteTabChild.changeShowAddNoteView(true);
        }
      }
      $("#op-chart-part").modal("show");
      await this.getSystemNoteData();
      this.clearImageKey++;
      await this.getPartAttachment();
      if (this.switchTab === "switch-notes") {
        const noteTabChild = this.$refs["note-tab"];
        if (noteTabChild) {
          noteTabChild.changeShowAddNoteView(true);
        }
      }
    },
    chartDataType(dataType) {
      console.log("chartDataType");
      this.requestParam.chartDataType = dataType;
      this.bindChartData();
    },
    dateViewType: function (viewType) {
      this.requestParam.dateViewType = viewType;
      this.bindChartData();
    },
    getParam(requestParam) {
      switch (requestParam.dateViewType) {
        case "HOUR":
          return Common.param({
            dateViewType: requestParam.dateViewType,
            chartDataType: this.requestParam.chartDataType,
            date:
              this.requestParam.year +
              this.requestParam.month +
              this.requestParam.day,
            partId: this.requestParam.partId,
          });
        case "DAY":
          return Common.param({
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            month: requestParam.month,
            partId: requestParam.partId,
          });
        case "MONTH":
        case "WEEK":
          return Common.param({
            dateViewType: requestParam.dateViewType,
            chartDataType: requestParam.chartDataType,
            year: requestParam.year,
            partId: requestParam.partId,
          });
      }
    },
    bindChartData(options) {
      if (options === undefined || options === null) {
        options = this.graphSelected.map((g) => g.code);
      }
      this.requestParam.chartDataType = options;
      this.requestParam.partId = this.part.id;
      if (this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }
      this.isShowFrequentlyChart = false;
      this.frequentlyChartData = [];
      this.setDayWhenChangeMonth(this.monthHour);
      let param = {};
      if (this.requestParam.dateViewType === "HOUR") {
        param = Common.param({
          ...this.requestParam,
          year: this.yearSelected,
          date: this.getCurrentDate(),
        });
      } else {
        param = Common.param({
          ...this.requestParam,
          year: this.yearSelected,
        });
      }

      config.options.scales.yAxes[0].ticks.beginAtZero = true;
      window.partChart.update();
      console.log("param", param);
      axios.get("/api/chart/part?" + param).then(
        (response) => {
          this.responseDataChart = response.data;
          /*
                    config.data.labels = [];
                    config.data.datasets = [];
          */
          let dataShow = this.getDataShow(response.data, 0);
          this.bindAllChartData(dataShow);
        },
        (error) => {
          console.log(error.response);
        }
      );
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
      config.data.labels = [];
      config.data.datasets = [];
      console.log("bindAllChartData");

      if (this.requestParam.year) {
        this.yearSelected = this.requestParam.year;
      }
      if (!this.isValidMonth()) {
        this.resetMonth();
      }

      this.isShowFrequentlyChart = false;
      this.frequentlyChartData = [];
      this.setDayWhenChangeMonth(this.monthHour);

      const totalQuantity = {
        type: "line",
        label: "Total Part Quantity",
        backgroundColor: "#B1E0EE",
        fill: false,
        borderColor: "#63C2DE",
        pointRadius: 2,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
        yAxisID: "first-y-axis",
        stack: "totalQuantity",
      };
      const totalMoldQuantity = {
        type: "line",
        label: "Total Mold Quantity",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        fill: false,
        borderColor: getStyle("--danger"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        data: [],
        yAxisID: "first-y-axis",
        stack: "totalMoldQuantity",
      };
      const approvedCycleTime = {
        type: "line",
        label: this.optimalCycleTime + " (sec)",
        backgroundColor: "transparent",
        fill: false,
        borderColor: getStyle("--success"),
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 2,
        data: [],
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
      const maximumCapacity = {
        type: "line",
        label: "Maximum Capacity",
        backgroundColor: hexToRgba(getStyle("--info"), 10),
        fill: false,
        // borderColor: getStyle("--danger"),
        borderColor: "#656565",
        pointRadius: 0,
        pointHoverBackgroundColor: "#fff",
        borderWidth: 1,
        data: [],
        yAxisID: "first-y-axis",
        stack: "maximumCapacity",
        options: {
          tooltips: {
            enabled: true,
            custom: null,
          },
        },
      };
      totalQuantity.type =
        this.chartTypeConfig.filter(
          (item) => item.chartDataType === "PART_QUANTITY"
        )[0].chartType === "LINE"
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
      let contranctedCycleTime = 0;

      if (totalQuantity.type === "bar" && maximumCapacity.type === "bar") {
        config.options.scales.yAxes[0].stacked = true;
      } else {
        config.options.scales.yAxes[0].stacked = false;
      }

      if (
        cycleTimeWithin.type === "bar" &&
        cycleTimeL1.type === "bar" &&
        cycleTimeL2.type === "bar"
      ) {
        config.options.scales.yAxes[1].stacked = true;
      } else {
        config.options.scales.yAxes[1].stacked = false;
      }
      const data = dataShow;

      for (var i = 0; i < data.length; i++) {
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
            title = title.replace("W-", "");
          } else if ("HOUR" == this.requestParam.dateViewType) {
            title = moment(data[i].title, "YYYYMMDDHH").format("HH");
          } else if ("WEEK" == this.requestParam.dateViewType) {
            let year = data[i].title.substring(0, 4);
            let week = data[i].title.replace(year, "");
            title = `W${week}, ${year}`;
            console.log(`check week data - ${i}`, data[i].title);
          }
        }

        config.data.labels.push(title);

        if (data[i].contractedCycleTime > 0 && contranctedCycleTime === 0) {
          contranctedCycleTime = data[i].contractedCycleTime;
        }
        if (this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")) {
          approvedCycleTime.data.push(contranctedCycleTime);
        }
        if (this.requestParam.chartDataType.includes("CYCLE_TIME")) {
          maxCycleTime.data.push(data[i].maxCycleTime);
          minCycleTime.data.push(data[i].minCycleTime);
          cycleTimeWithin.data.push(data[i].cycleTimeWithin);
          cycleTimeL1.data.push(data[i].cycleTimeL1);
          cycleTimeL2.data.push(data[i].cycleTimeL2);
        }
        if (this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY")) {
          maximumCapacity.data.push(data[i].maxCapacity);
        }
        if (this.requestParam.chartDataType.includes("QUANTITY")) {
          totalQuantity.data.push(data[i].data);
        }
      }

      // config.options.tooltips = this.setTooltipForMainChart(
      //   this.requestParam.dateViewType,
      //   data
      // );
      config.options.tooltips = {
        enabled: false,
        custom: function (tooltipModel) {
          console.log(tooltipModel, "tooltipModeltooltipModel");
          if (
            tooltipModel.body &&
            !tooltipModel.body[0].lines[0].includes("Total Part Quantity")
          ) {
            // Tooltip Element
            var tooltipEl = document.getElementById("chartjs-tooltip");
            // Create element on first render
            let xLabel = "";
            let yLabel = 0;
            let itemIndex = 0;
            let name = "";
            if (tooltipModel.body[0].lines[0].includes("Min CT")) {
              name = "Min CT";
            } else if (
              tooltipModel.body[0].lines[0].includes("Maximum Capacity")
            ) {
              name = "Maximum Capacity";
            } else if (tooltipModel.body[0].lines[0].includes("Within")) {
              name = "Within";
            } else if (tooltipModel.body[0].lines[0].includes("L2")) {
              name = "L2";
            } else if (tooltipModel.body[0].lines[0].includes("L1")) {
              name = "L1";
            } else if (tooltipModel.body[0].lines[0].includes("Approved CT")) {
              name = "Approved CT";
            } else if (
              tooltipModel.body[0].lines[0].includes("Weighted Average CT")
            ) {
              name = "Weighted Average CT";
            } else if (tooltipModel.body[0].lines[0].includes("Max CT")) {
              name = "Max CT";
            }
            const { dataPoints } = tooltipModel;
            if (dataPoints && dataPoints.length > 0) {
              xLabel = Math.round(dataPoints[0].xLabel * 100) / 100;
              yLabel = Math.round(dataPoints[0].yLabel * 100) / 100;
              itemIndex = dataPoints[0].index;
            }
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div class="tooltips-title">
                                                    <div style="background-color: ${tooltipModel.labelColors[0].backgroundColor};border-color: ${tooltipModel.labelColors[0].borderColor}" class="box-title"></div>
                                                    ${name}: ${yLabel} </div>
                                                    </div>`;
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }

            tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div class="tooltips-title">
                                                    <div style="background-color: ${
                                                      tooltipModel
                                                        .labelColors[0]
                                                        .backgroundColor
                                                    };border-color: ${
              tooltipModel.labelColors[0].borderColor
            }" class="box-title"></div>
                                                    ${name}: ${yLabel
              .toString()
              .replace(/\B(?=(\d{3})+(?!\d))/g, ",")} </div>
                                                    </div>`;
            var position = window.partChart.canvas.getBoundingClientRect();
            console.log("position: ", position);
            console.log("window.pageXOffset: ", window.pageXOffset);
            console.log("tooltipModel.caretX: ", tooltipModel.caretX);
            tooltipEl.style.opacity = 0.9;
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
            tooltipEl.style.borderRadius = "6px";
            tooltipEl.style.pointerEvents = "none";
            tooltipEl.style.transform = "translateX(-25%)";
          } else {
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
            if (!tooltipEl) {
              tooltipEl = document.createElement("div");
              tooltipEl.id = "chartjs-tooltip";
              tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div style="margin: 5px 0px;">${xLabel}</div>
                                                    <div class="tooltips-title">
                                                    <div class="box-title"></div>
                                                    Total Part Quantity: ${yLabel} </div>
                                                    <table class="table">
                                                    </table>
                                                    </div>`;
              document.body.appendChild(tooltipEl);
            }

            if (tooltipModel.opacity === 0) {
              tooltipEl.style.opacity = 0;
              return;
            }

            tooltipEl.innerHTML = `<div class="tooltips-container" >
                                                    <div style="margin: 5px 0px;">${xLabel}</div>
                                                    <div class="tooltips-title">
                                                    <div class="box-title"></div>
                                                    Total Part Quantity: ${yLabel
                                                      .toString()
                                                      .replace(
                                                        /\B(?=(\d{3})+(?!\d))/g,
                                                        ","
                                                      )} </div>
                                                    <table class="table">
                                                    </table>
                                                    </div>`;

            if (tooltipModel.body) {
              var innerHtml = "<thead>";
              innerHtml += "<tr>";
              ["Tool ID", "Shots", "Avg.Cavities", "Quantity"].forEach(
                function (value) {
                  innerHtml += "<th>" + value + "</th>";
                }
              );
              innerHtml += "</tr></thead>";
              if (data.length > itemIndex && itemIndex >= 0) {
                data[itemIndex].moldShots &&
                  data[itemIndex].moldShots.map((value) => {
                    innerHtml +=
                      '<tr><td style="text-align: center;">' +
                      value.moldCode +
                      "</td>";
                    innerHtml +=
                      '<td style="text-align: center;">' +
                      value.shotCount
                        .toString()
                        .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                      "</td>";
                    innerHtml +=
                      '<td style="text-align: center;">' +
                      Number(value.avgCavity)
                        .toFixed(2)
                        .toString()
                        .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                      "</td>";
                    innerHtml +=
                      '<td style="text-align: center;">' +
                      value.quantity
                        .toString()
                        .replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
                      "</td></tr>";
                  });
              }
              var tableRoot = tooltipEl.querySelector("table");
              tableRoot.innerHTML = innerHtml;
            }

            var position = window.partChart.canvas.getBoundingClientRect();
            console.log("position: ", position);
            console.log("window.pageXOffset: ", window.pageXOffset);
            console.log("tooltipModel.caretX: ", tooltipModel.caretX);
            tooltipEl.style.opacity = 1;
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
          }
        },
      };

      if (this.requestParam.chartDataType.includes("QUANTITY")) {
        config.data.datasets.push(totalQuantity);
      }
      if (this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")) {
        config.data.datasets.push(approvedCycleTime);
      }
      if (this.requestParam.chartDataType.includes("CYCLE_TIME")) {
        config.data.datasets.push(minCycleTime);
        config.data.datasets.push(maxCycleTime);
        config.data.datasets.push(cycleTimeWithin);
        config.data.datasets.push(cycleTimeL1);
        config.data.datasets.push(cycleTimeL2);
      }
      if (this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY")) {
        config.data.datasets.push(maximumCapacity);
      }

      if (!dataShow.length) {
        let defaultData = this.getChartDefaultData();
        config.data.labels = defaultData.labels;
      }
      this.updateChartPartLegend();
      if (
        !this.requestParam.chartDataType.includes("CYCLE_TIME") &&
        !this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME")
      ) {
        config.options.scales.yAxes[1].scaleLabel.labelString = "";
        config.options.scales.yAxes[1].display = false;
      } else {
        config.options.scales.yAxes[1].scaleLabel.labelString = "Cycle Time";
        config.options.scales.yAxes[1].display = true;
      }
      const isDataAvailable = config.data.datasets.some((set) =>
        set.data.some((item) => !!item)
      );
      config.options.scales.yAxes[0].gridLines.display = isDataAvailable;
      config.options.scales.yAxes[1].gridLines.display = false;
      this.noDataAvailable = !isDataAvailable;
      config.options.scales.xAxes[0].scaleLabel.labelString = this.toPascalCase(
        this.requestParam.dateViewType
      );

      window.partChart.update();
    },
    updateChartPartLegend() {
      console.log("updateChartPartLegend");
      $("#legend-chart-part").html(partChart.generateLegend());
    },
    setGraphAxisTitle() {
      console.log("setGraphAxisTitle");
      let xAxisTitle = this.xAxisTitleMapping[this.requestParam.dateViewType];
      let yAxisTitle = this.yAxisTitleMapping[this.requestParam.chartDataType];
      if (
        this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY") &&
        this.requestParam.chartDataType.includes("QUANTITY")
      ) {
        config.options.scales.yAxes[0].scaleLabel.labelString =
          "Capacity or Quantity";
      } else if (this.requestParam.chartDataType.includes("MAXIMUM_CAPACITY")) {
        config.options.scales.yAxes[0].scaleLabel.labelString = "Capacity";
      } else if (this.requestParam.chartDataType.includes("QUANTITY")) {
        config.options.scales.yAxes[0].scaleLabel.labelString = "Quantity";
      }

      if (
        this.requestParam.chartDataType.includes("APPROVED_CYCLE_TIME") ||
        this.requestParam.chartDataType.includes("CYCLE_TIME")
      ) {
        config.options.scales.yAxes[1].scaleLabel.labelString = "CycleTime";
      }
      const isDataAvailable = config.data.datasets.some((set) =>
        set.data.some((item) => !!item)
      );
      config.options.scales.yAxes[0].gridLines.display = isDataAvailable;
      config.options.scales.yAxes[1].gridLines.display = false;
      this.noDataAvailable = !isDataAvailable;
      config.options.scales.xAxes[0].scaleLabel.labelString = xAxisTitle;
    },
    getChartDefaultData() {
      console.log("getChartDefaultData");
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
    details(detailDate) {
      console.log("details");
      if (this.requestParam.dateViewType === "HOUR") {
        if (
          this.requestParam.chartDataType === "CYCLE_TIME_ANALYSIS" ||
          this.requestParam.chartDataType === "TEMPERATURE_ANALYSIS"
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
    closeDatePicker() {
      console.log("closeDatePicker");
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
        if (tempData.selectedType == "HOURLY") {
          this.requestParam.month = tempData.singleDate.slice(4, 6);
          this.requestParam.day = moment(data.time, "YYYYMMDD").format("DD");
          this.day = moment(data.time, "YYYYMMDD").format("DD");
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;
        } else if (tempData.selectedType == "DAILY") {
          this.requestParam.dateViewType = "DAY";
          this.requestParam.month = tempData.singleDate.slice(4, 6);
          this.month = moment(data.time, "YYYYMMDD").format("MM");
          this.monthSelected = this.monthHour;
          delete this.requestParam.startDate;
          delete this.requestParam.endDate;
        } else if (tempData.selectedType == "MONTHLY") {
          //WEEKLY or MONTHLY
          //doNothing
          this.requestParam.month = "";
          delete this.requestParam.startDate;
          delete this.requestParam.endDate;
        }
        this.isSelectedCustomRange = false;
      }

      this.bindChartData();
    },
    handleChangeType(data) {
      console.log("change type", data);
      this.requestParam.dateViewType = data.dataFrequency;
    },
    setTimeRange: function (partId) {
      this.rangeYear = [];
      this.rangeMonth = [];
      this.yearSelected = "2020";
      return axios
        .get(`/api/statistics/time-range?partId=${partId}`)
        .then((response) => {
          console.log("range-date: ", response);
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
  },
  computed: {
    getNoteNotiNumber() {
      const total = _.sumBy(this.notes, (item) => item.numUnread);
      console.log("getNoteNotiNumber", total);
      return total;
    },
    categoryLocation() {
      if (this.part === null || this.part.category === null) {
        return "";
      }

      try {
        var category = this.part.category;
        var parentCategory = category.parent;

        var displayCategory = "";
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
  },
  created() {
    this.$watch(
      () => headerVm?.currentUser,
      (newVal) => {
        if (newVal) {
          this.currentUser = Object.assign({}, this.currentUser, newVal);
        }
      },
      { immediate: true }
    );
  },
  mounted() {
    this.getCfgStp();
    // this.getChartTypeConfig();
    this.$nextTick(() => {
      $("#hourly-chart-wrapper").hide();
      const ctx = document.getElementById("chart-part");
      window.partChart = new Chart(ctx, config);
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
    "param.partId": async function (newVal) {
      console.log("watch param.partId", newVal);
      if (newVal && this.param.systemNoteFunction === "PART_SETTING") {
        const part = await this.getPartById(newVal);
        await this.showChart(part, null, null, null, "switch-notes");
        const selectedNote = await this.findNoteById(this.param.systemNoteId);
        if (selectedNote.id !== this.param.systemNoteId) {
          // noteTabChild.toggleShowAllReplies()
        }
        this.switchTab = "switch-notes";
        const noteTabChild = this.$refs["note-tab"];
        if (noteTabChild) {
          noteTabChild.onSelectViewNote(selectedNote);
        }
      }
    },
    part(newVal, oldVal) {
      console.log("part", newVal);
    },
  },
};
$(function () {
  const ctx = document.getElementById("chart-part");
  window.partChart = new Chart(ctx, config);
});
</script>

<style scoped>
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

.btn:focus {
  box-shadow: unset;
}

.select-graph-group {
  width: 152px;
  height: 32px;
  margin-right: 10px;
}

.select-date-group {
  display: flex;
  grid-template-columns: 1fr 1fr;
}

.graph-header {
  display: flex;
  justify-content: end;
  width: 100%;
}

.graph-header .dropdown_button:hover {
  color: #0e65c7 !important;
  background-color: #deedff;
  outline: 1px solid transparent;
  border: 1px solid transparent;
}

.graph-header .dropdown_button {
  display: inline-flex;
  justify-content: space-between;
  align-items: center;
  color: rgb(52, 145, 251);
  transition: all 0ms ease 0s;
  line-height: 1.499;
  position: relative;
  font-weight: 400;
  white-space: nowrap;
  text-align: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
  padding: 0px 8px 0px 10px;
  font-size: 14px;
  border-radius: 3px;
  border: 1px solid rgb(255, 255, 255);
  background-color: rgb(255, 255, 255);
  outline: rgb(52, 145, 251) solid 1px;
  height: 32px !important;
  text-decoration: none !important;
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

/* .modal-body {
  background: white;
} */

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
  font-size: 14px;
  display: table;
  width: 75%;
}

.info-zone .item {
  height: 35px;
  /*line-height: 35px;*/
  display: table-row;
  /*background: rgb(251, 252, 253);*/
}
.info-zone .item .head {
  width: 12%;
  display: table-cell;
  text-align: left;
}
.info-zone .item .head b {
  color: #4b4b4b;
}

.info-zone .item .desc-zone {
  background: rgb(251, 252, 253);
  margin-right: 20px;
  padding: 0 10px;
}
.info-zone .item .desc {
  /*background: #fbfcfd;*/
  display: inline-block;
  margin-right: 70px;
  text-align: left;
}

.graph-dropdown-wrap label {
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li {
  list-style: none;
  color: #4b4b4b;
  font-size: 11px;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li input {
  margin-right: 8px;
}

.next-chart-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.month-class {
  height: calc(2.0625rem + 2px);
  border: 1px solid #e4e7ea;
  border-radius: 0.25rem;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0px !important;
  width: 63px;
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
