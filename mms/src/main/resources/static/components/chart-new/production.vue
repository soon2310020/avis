<!-- TODO: REMOVE AFTER APPLY NEW DASHBOARD -->
<template>
  <div
    id="PRODUCTION_RATE"
    :class="className"
    class="card card-customize size-2"
  >
    <div class="top-container title-container">
      <div class="line-title"></div>
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <div
          style="margin-left: 35px"
          class="title-top"
          v-text="resources['production_rate']"
        ></div>
        <a-tooltip placement="bottom">
          <template slot="title">
            <div
              style="padding: 6px; font-size: 13px"
              v-text="resources['production_rate_description']"
            ></div>
          </template>
          <div style="margin-left: 10px">
            <img src="/images/icon/tooltip_info.svg" width="15" height="15" />
          </div>
        </a-tooltip>
      </div>
      <span
        class="close-button"
        v-on:click="dimiss('PRODUCTION_RATE', resources['production_rate'])"
        style="font-size: 25px"
        aria-hidden="true"
        >&times;</span
      >
    </div>
    <div
      ref="production-chart-container"
      style="
        padding: 1.25rem 30px;
        max-height: 450px;
        overflow-x: unset !important;
      "
      class="card-body"
    >
      <div class="dropdown-container">
        <parts-filter
          :resources="resources"
          :set-change-data="setChangeData"
          :default-type="filterData.type"
          :default-id-list="filterData.ids"
          :default-frequent="filterData.frequent"
          :frequent="frequent"
          :query="searchProductionData"
          :option-array-part="parts"
          :option-array-tooling="toolings"
          :id="'production'"
        ></parts-filter>
        <a-dropdown v-model="isShow" :trigger="['click']">
          <a-menu @click="handleClick" slot="overlay">
            <a-menu-item
              key="DAILY"
              :class="{ 'selected-dropdown-item': selectedItem === 'DAILY' }"
              v-text="resources['daily']"
            ></a-menu-item>
            <a-menu-item
              key="WEEKLY"
              :class="{ 'selected-dropdown-item': selectedItem === 'WEEKLY' }"
              v-text="resources['weekly']"
            ></a-menu-item>
            <a-menu-item
              key="MONTHLY"
              :class="{ 'selected-dropdown-item': selectedItem === 'MONTHLY' }"
              v-text="resources['monthly']"
            ></a-menu-item>
            <a-menu-item
              key="YEARLY"
              :class="{ 'selected-dropdown-item': selectedItem === 'YEARLY' }"
              v-text="resources['yearly']"
            ></a-menu-item>
          </a-menu>
          <a
            href="javascript:void(0)"
            id="production-time-rate"
            @click="showAnimation()"
            class="dropdown_button-custom"
            style="width: 132px"
          >
            <span :class="{ 'selected-dropdown-text': isShow }">{{
              titleTime
            }}</span>
            <div>
              <a-icon
                class="caret"
                :class="{
                  'selected-dropdown-text': isShow,
                  'caret-show': displayCaret,
                }"
                type="caret-down"
              />
            </div>
          </a>
        </a-dropdown>
      </div>
      <canvas
        ref="production-chart"
        style="height: 100%"
        id="production-chart"
      ></canvas>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    type: String,
    dimiss: Function,
    parts: Array,
    toolings: Array,
    className: String,
    filterQuery: Object,
    filterData: Object,
  },
  components: {
    "parts-filter": httpVueLoader("/components/dashboard/parts-filter.vue"),
  },
  data() {
    return {
      selectedList: [],
      selectedItem: "",
      frequent: "DAILY",
      filterType: null,
      data1: [
        {
          data: [40, 20, 10, 16, 24, 38],
          fill: false,
          label: "Uptime",
          borderColor: "#864cda",
          backgroundColor: "#864cda",
        },
      ],
      chart: null,
      hoveringBubble: null,
      isShow: false,
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
      isChangedData: false,
    };
  },
  computed: {
    titleTime() {
      let title = "";
      if (this.frequent === "DAILY") title = this.resources["daily"];
      if (this.frequent === "WEEKLY") title = this.resources["weekly"];
      if (this.frequent === "MONTHLY") title = this.resources["monthly"];
      if (this.frequent === "YEARLY") title = this.resources["yearly"];
      if (!title) return "";
      return title.toLowerCase().capitalize();
    },
  },
  methods: {
    setChangeData() {
      this.isChangedData = true;
    },
    handleClick(item) {
      console.log("@handleClick:production>item", item);
      this.isChangedData = true;
      this.searchProductionData(this.selectedList, item.key, this.filterType);
      this.selectedItem = item.key;
      this.displayCaret = true;
    },

    async searchProductionData(selectedList, frequent, filterType = null) {
      console.log("@searchProductionData:production>", {
        selectedList,
        frequent,
        filterType,
      });
      this.selectedList = selectedList;
      this.frequent = frequent;
      this.filterType = filterType;

      const query = Common.param({
        ids: selectedList.map((i) => i.id),
        frequent,
        type: this.filterType,
      });

      try {
        const response = await axios.get(
          "/api/chart/graph-production?" + query
        );
        if (response.data) {
          if (this.isChangedData) {
            this.saveChartFilterSetting();
          }

          console.log("@searchProductionData:production>response", response);
          const datasets = [
            {
              label: "",
              data: [],
              backgroundColor: "#fff",
              borderColor: "#fff",
            },
          ];
          let labels = [];
          const colors = ["#874cda", "#cd767c", "#6fb97d", "#f7cc53"];

          Object.keys(response.data).map((value, index) => {
            const listData = response.data[value].sort((a, b) => {
              let date1 = 0;
              let date2 = 0;

              if (frequent === "MONTHLY") {
                date1 = moment(a.year, "YYYY").month(a.weekOrMonth).unix();
                date2 = moment(b.year, "YYYY").month(b.weekOrMonth).unix();
              } else if (frequent === "WEEKLY") {
                date1 = moment(a.year, "YYYY").week(a.weekOrMonth).unix();
                date2 = moment(b.year, "YYYY").week(b.weekOrMonth).unix();
              } else if (frequent === "DAILY") {
                date1 = moment(a.year, "YYYY")
                  .month(a.weekOrMonth)
                  .date(a.day)
                  .unix();
                date2 = moment(b.year, "YYYY")
                  .month(b.weekOrMonth)
                  .date(b.day)
                  .unix();
              } else {
                date1 = moment(a.year, "YYYY").unix();
                date2 = moment(b.year, "YYYY").unix();
              }

              return date1 - date2;
            });

            console.log("@searchProductionData:production>listData", listData);
            labels = [""].concat(
              listData.map((item) => {
                console.log("item", item);
                if (!item.year) {
                  return ``;
                }
                if (frequent === "WEEKLY") {
                  return `W${
                    item.weekOrMonth >= 10
                      ? item.weekOrMonth
                      : `0${item.weekOrMonth}`
                  } ${item.year}`;
                } else if (frequent === "MONTHLY") {
                  return `${moment(item.year, "YYYY")
                    .month(Number(item.weekOrMonth) - 1)
                    .format("MMM")} ${item.year}`;
                } else if (frequent === "DAILY") {
                  return `${moment(item.year, "YYYY")
                    .month(Number(item.weekOrMonth) - 1)
                    .date(item.day)
                    .format("DD MMM")} ${item.year}`;
                } else {
                  return item.year;
                }
              })
            );

            console.log("@searchProductionData:production>labels", labels);

            let label = "";
            if (value === 0) {
              label = this.resources["all"];
            } else {
              label = selectedList.find((item) => item.id == value)
                ? selectedList.find((item) => item.id == value).name
                : "";
            }

            datasets.push({
              data: [0].concat(
                listData.map((i) => {
                  return i.count < 0 ? 0 : i.count;
                })
              ),
              fill: true,
              label,
              borderColor: colors[index],
              backgroundColor: colors[index],
            });
          });

          console.log("@searchProductionData:production>", {
            labels,
            datasets,
          });
          this.bindChartPreventive(labels, datasets);
        }
      } catch (error) {
        console.log(error);
      } finally {
        this.isChangedData = false;
      }
    },
    async saveChartFilterSetting() {
      const payload = {
        chartType: "PRODUCTION_QUANTITY",
        dashboardSettingData: {
          frequent: this.frequent,
          ids: this.selectedList.map((item) => item.id).join(","),
          type: this.filterType,
        },
      };
      try {
        await axios.post("/api/dashboard-chart-display-settings", payload);
      } catch (error) {
        console.log(error);
      }
    },
    bindChartPreventive(labels, datasets) {
      console.log("@bindChartPreventive:production>", { labels, datasets });
      this.chart.data.labels = labels;
      this.chart.data.datasets = datasets;
      this.chart.options.scales.xAxes = [
        {
          scaleLabel: {
            display: true,
            labelString: this.titleTime,
            padding: 5,
          },
          ticks: {
            stepSize: 1,
            min: 0,
            autoSkip: false,
          },
        },
      ];
      this.chart.options.legend = {
        display: true,
        position: "bottom",
        align: "start",
        rtl: true,
      };

      this.chart.resize();
      this.chart.update();

      console.log("@bindChartPreventive>production", {
        container: this.$refs["production-chart-container"],
        canvas: this.$refs["production-chart"],
        width: this.$refs["production-chart-container"]?.offsetWidth,
        height: this.$refs["production-chart-container"]?.offsetHeight,
      });

      const intervalCheck = setInterval(() => {
        // backup load
        if (this.$refs["production-chart-container"]?.offsetWidth) {
          this.chart.resize();
          this.chart.update();
          clearInterval(intervalCheck);
        }
      }, 2000);
    },

    goToPage: function (partId) {
      // location.href = `/parts#${partId}`;
      location.href = `${Common.PAGE_URL.PART}?from=dashboard&objectId=${partId}`;
    },
    showAnimation() {
      const el = document.getElementById("production-time-rate");
      console.log(el);
      if (!this.isShow) {
        console.log(el);
        setTimeout(() => {
          console.log(el);
          el.classList.add("dropdown_button-animation");
          this.animationTimeout = setTimeout(() => {
            el.classList.remove("dropdown_button-animation");
            el.classList.add("selected-dropdown-text");
            el.classList.add("selected-dropdown-button");
          }, 1600);
        }, 50);
      } else {
        this.closeAnimation();
      }
    },
    closeAnimation() {
      const el = document.getElementById("production-time-rate");
      if (this.animationTimeout != null) {
        console.log("Here");
        console.log(this.animationTimeout);
        clearTimeout(this.animationTimeout);
        this.animationTimeout = null;
      }
      if (this.caretTimeout != null) {
        clearTimeout(this.caretTimeout);
        this.caretTimeout = null;
      }
      el.classList.remove("dropdown_button-animation");
      el.classList.remove("selected-dropdown-text");
      el.classList.remove("selected-dropdown-button");
      this.displayCaret = false;
    },
    isObjectNotEmpty(obj) {
      return obj && Object.keys(obj).length > 0;
    },
  },
  watch: {
    isShow: function () {
      if (!this.isShow) {
        this.closeAnimation();
      }
    },
    filterData(newVal) {
      this.frequent = newVal.frequent;
      this.filterType = newVal.filterType;
    },
  },
  mounted() {
    let $vm = this;
    const chartInstance = this.$refs["production-chart"];
    this.chart = new Chart(chartInstance, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          {
            data: [],
            fill: true,
            label: "Uptime",
            borderColor: "#864cda",
            backgroundColor: "#864cda",
          },
        ],
      },
      options: {
        tooltips: {
          callbacks: {
            label: function (tooltipItem, data) {
              console.log(
                "data.datasets[tooltipItem.datasetIndex]: ",
                tooltipItem
              );
              $vm.hoveringBubble = tooltipItem;
              var label = data.datasets[tooltipItem.datasetIndex].label || "";
              return Number(tooltipItem.yLabel) < 1
                ? ` ${label}: ${tooltipItem.yLabel} part`
                : ` ${label}: ${tooltipItem.yLabel} parts`;
            },
          },
        },
        plugins: {
          datalabels: {
            display: false,
          },
        },
        title: {
          display: false,
        },
        scales: {
          yAxes: [
            {
              stacked: true,
              ticks: {
                beginAtZero: true,
              },
              scaleLabel: {
                display: true,
                labelString: this.resources["quantity_produced"],
              },
            },
          ],
          xAxes: [
            {
              scaleLabel: {
                display: true,
                labelString: this.titleTime,
                padding: 5,
              },
              ticks: {
                stepSize: 1,
                min: 0,
                autoSkip: false,
              },
            },
          ],
        },
        responsive: true,
        legend: {
          display: false,
          position: "bottom",
          align: "start",
          rtl: true,
          padding: 5,
        },
        maintainAspectRatio: false,
      },
    });

    console.log("@mounted:production>chart", {
      chart: this.chart,
      chartInstance,
    });

    chartInstance.addEventListener(
      "click",
      (evt) => {
        var activePoints = this.chart.getElementsAtEvent(evt);

        var activeDataSet = this.chart.getDatasetAtEvent(evt);
        console.log("activeDataSet", activeDataSet);

        if (activePoints.length > 0) {
          var clickedDatasetIndex = activeDataSet[0]._datasetIndex;

          console.log("$vm.hoveringBubble.xLabel: ", $vm.hoveringBubble.xLabel);
          console.log("$vm.selectedList", JSON.stringify($vm.selectedList));
          if (
            $vm.selectedList !== undefined &&
            $vm.selectedList !== null &&
            $vm.selectedList.length > 0 &&
            $vm.selectedList[clickedDatasetIndex] &&
            $vm.selectedList[clickedDatasetIndex].name
          ) {
            if (this.filterType !== "TOOLING") {
              location.href = `${Common.PAGE_URL.PART}?from=dashboard&timePeriod=${$vm.hoveringBubble.xLabel}&periodType=${$vm.frequent}&query=${$vm.selectedList[clickedDatasetIndex].name}`;
            } else {
              location.href = `${Common.PAGE_URL.PART}?from=dashboard&timePeriod=${$vm.hoveringBubble.xLabel}&periodType=${$vm.frequent}&toolingId=${$vm.selectedList[clickedDatasetIndex].name}`;
            }
          } else {
            location.href = `${Common.PAGE_URL.PART}?from=dashboard&timePeriod=${$vm.hoveringBubble.xLabel}&periodType=${$vm.frequent}`;
          }
        }
      },
      false
    );
  },
};
</script>

<style scoped>
.dropdown-container {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  justify-content: flex-end;
}

.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: 100px;
  background: #f2f5fa;
}
</style>
