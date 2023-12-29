<template>
  <div style="width: 100%">
    <div class="couple-table-header row-table">
      <div
        v-for="(item, index) in columns"
        :key="index"
        class="data-column"
        :class="{ 'text-left': item.textLeft }"
      >
        <strong>{{ item.label }}</strong>
      </div>
    </div>
    <div
      v-for="(result, index) in dataList"
      :key="index"
      class="couple-table-body"
    >
      <div class="data-row row-table">
        <div
          v-for="(column, index1) in columns"
          class="data-column"
          :class="{
            'text-left': column.textLeft,
            'content__hyper-link': column.isHyperLink,
          }"
          :key="index1"
        >
          <template v-if="column.field == 'machineCode'">
            <span @click="showMachineDetails(result.machineId)">{{
              textTransfer(result[column.field], column.formatter)
            }}</span>
          </template>
          <template v-else-if="column.field == 'expectedHourlyProduction'">
            <div v-if="result.expectedHourlyProduction">
              <base-drop-count
                :is-hyper-link="false"
                :data-list="result.expectedHourlyProduction"
                :title="
                  result.expectedHourlyProduction &&
                  result.expectedHourlyProduction[
                    result.expectedHourlyProduction.length - 1
                  ]
                    ? result.expectedHourlyProduction[
                        result.expectedHourlyProduction.length - 1
                      ].expectedProducedPart
                    : ''
                "
                @title-click="() => {}"
              >
                <div style="padding: 6px 8px">
                  <div
                    v-for="(item, index) in result.expectedHourlyProduction"
                    :key="index"
                    class="d-flex expected-hourly-prod"
                    style="min-width: 300px; color: #4b4b4b; line-height: 21px"
                  >
                    <span>{{ item.moldCode }}</span>
                    <span>{{ getExpireTime(item) }}</span>
                    <span>{{
                      `${item.expectedProducedPart} ${
                        item.expectedProducedPart > 1 ? "parts" : "part"
                      }`
                    }}</span>
                  </div>
                </div>
              </base-drop-count>
            </div>
          </template>
          <template v-else>
            <span>{{
              textTransfer(result[column.field], column.formatter)
            }}</span>
          </template>

          <template v-if="column.field == 'oee'">
            <div class="status-bar">
              <div
                class="current-status"
                :style="getStatusBarStyle(result.oee)"
              ></div>
            </div>
          </template>
        </div>
      </div>
      <div>
        <div class="shift-row">
          <div class="shift-title-container">
            <div
              v-for="(item, index) in listDisplayedShifts"
              :key="index"
              class="shift-title"
              :style="getTitleWidth"
            >
              {{ item.name }}
            </div>
          </div>
          <div class="shift-container">
            <div
              v-for="(shiftHour, i) in result.shiftData"
              :key="i"
              class="shift-column"
            >
              <div class="shift-hour">
                {{ getHourFormat(shiftHour.start) }}
              </div>
              <div
                class="shift-data"
                :class="[
                  { 'has-data': hasData(shiftHour.oee) },
                  { hover: i === hoveredItem },
                  getRiskColor(shiftHour.oee),
                ]"
                @hover="hoveredItem = i"
                @click="handleClickShift($event, result, shiftHour)"
              >
                <div style="height: 6px; width: 100%"></div>
                <label
                  class="shift-hour__title"
                  :class="{
                    'shift-text-10': `${shiftHour.partProduced}`.length > 3,
                  }"
                  >{{ getDisplayShift(shiftHour.partProduced) }}</label
                >
                <base-tooltip
                  v-if="
                    showShiftProgress &&
                    shiftHour.oee !== null &&
                    shiftHour.progress !== null
                  "
                  color="grey"
                  background="white"
                  style="width: 100%"
                >
                  <template slot="content">
                    <div>
                      <span style="white-space: nowrap"
                        >Until: {{ shiftHour.until }}</span
                      >
                    </div>
                  </template>
                  <div class="shift-progess-bar">
                    <div
                      class="shift-progess-bar--active"
                      :style="getActiveProgressStyle(shiftHour.progress)"
                    ></div>
                  </div>
                </base-tooltip>
                <div v-else style="height: 6px; width: 100%"></div>
                <div
                  v-if="shiftHour.machineMoldHistoryData"
                  class="shift-hour__indicator-container rounded-circle"
                >
                  <a-tooltip placement="bottomRight">
                    <template slot="title">
                      <div class="plugin-tooltip-container">
                        <machine-connect-card
                          v-for="(
                            item, index
                          ) in shiftHour.machineMoldHistoryData"
                          :connection-infor="item"
                          :key="index"
                        ></machine-connect-card>
                      </div>
                    </template>
                    <div class="shift-hour__indicator-icon"></div>
                  </a-tooltip>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    columns: {
      type: Array,
      default: () => [],
    },
    dataList: {
      type: Array,
      default: () => [],
    },
    listDisplayedShifts: {
      type: Array,
      default: () => [],
    },
    getRiskColor: Function,
    showShiftProgress: {
      type: Boolean,
      default: () => false,
    },
    percentColor: Function,
  },
  components: {
    "base-tooltip": httpVueLoader("/components/@base/tooltip/base-tooltip.vue"),
    "machine-connect-card": httpVueLoader(
      "/components/oee-center/parts-produced/machine-connect-card.vue"
    ),
  },
  data() {
    return {
      hoveredItem: -1,
    };
  },
  computed: {
    getTitleWidth() {
      const result = Math.ceil(100 / this.listDisplayedShifts.length) - 1;
      return { width: `${result}%` };
    },
  },
  methods: {
    textTransfer(text, formatter) {
      const result = text === "" ? "-" : text;
      return !formatter ? result : formatter(result);
    },
    getStatusBarStyle(oee) {
      return {
        backgroundColor: this.percentColor(oee, "OEE"),
        width: Math.ceil(oee / 2) + "px",
      };
    },
    showMachineDetails(machineId) {
      this.$emit("show-machine-details", machineId);
    },
    getHourFormat(time) {
      if (time) {
        const hour = time.substr(0, 2);
        const min = time.substr(2, 2);
        return `${hour}:${min}`;
      }
      return "";
    },
    oeeFormat(val) {
      return Common.formatter.formatToDecimal(val, 2);
    },
    // getStyle(percent) {
    //   const colorHex = this.getRiskColor(percent)
    //   return { backgroundColor: colorHex }
    // },
    handleClickShift(event, shiftDay, shiftHour) {
      console.log({ event, shiftDay, shiftHour });
      this.$emit("shift-click", event, shiftDay, shiftHour);
    },
    hasData(oee) {
      return oee !== undefined && oee !== null;
    },
    getActiveProgressStyle(progress) {
      const style = { width: `${progress}%` };
      console.log("getActiveProgressStyle", style);
      return style;
    },
    getDisplayShift(data) {
      if (data) {
        // if (`${data}`.length > 3) {
        //   return `${data}`.charAt(0) + '...'
        // }
        return data;
      }
      return "";
    },
    getMachineMoldHistoryData(shiftHour) {
      if (shiftHour) {
        if (shiftHour.machineMoldHistoryData) {
          return shiftHour.machineMoldHistoryData;
        }
      }
      return [];
    },
    getExpireTime(expectedHourlyProduction) {
      const start = `${expectedHourlyProduction.start.slice(
        0,
        2
      )}:${expectedHourlyProduction.start.slice(2, 4)}`;
      const end = `${expectedHourlyProduction.end.slice(
        0,
        2
      )}:${expectedHourlyProduction.end.slice(2, 4)}`;
      return `${start} - ${end}`;
    },
  },
  watch: {
    columns(newVal) {
      console.log("columns", newVal);
    },
    dataList(newVal) {
      console.log("dataList", newVal);
    },
    listDisplayedShifts(newVal) {
      console.log("listDisplayedShifts", newVal);
    },
  },
};
</script>

<style>
.oee-bg-high-bar {
  background-color: #e34537;
}

.oee-bg-medium-bar {
  background-color: #f7cc57;
}

.oee-bg-low-bar {
  background-color: #41ce77;
}

.status-bar {
  height: 4px;
  width: 50px;
  background-color: #d6dade;
  position: relative;
}

.current-status {
  position: absolute;
  height: 4px;
  left: 0;
  right: 0;
}

.couple-table-header {
  display: flex;
}

.data-column {
  width: 20%;
}

.data-row {
  display: flex;
  background-color: rgba(0, 0, 0, 0.05);
}

.shift-title-container {
  display: flex;
  column-gap: 1%;
  margin-bottom: 20px;
  min-height: 23px;
  height: 45px;
}

.shift-title {
  border-bottom: 2px solid #3491ff;
  text-align: center;
  font-weight: bold;
}

.shift-container {
  display: flex;
}

.shift-column {
  /* width: 45px; */
  flex: 1;
  height: 56px;
  display: flex;
  margin-right: 3px;
  border-left: 1px solid #d6dade;
  align-items: flex-end;
  position: relative;
}

.shift-data {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 40px;
  text-align: center;
  font-size: 12px;
  position: relative;
  flex-direction: column;
}

.shift-data.has-data {
  cursor: pointer;
}

.shift-hour {
  color: #7e7e7e;
  font-size: 10px;
  position: absolute;
  top: -15px;
  left: -15px;
}

.row-table {
  height: 57px;
  padding: 7px;
  border-top: 1px solid #c8ced3;
  border-bottom: 1px solid #c8ced3;
  align-items: center;
}

.shift-row {
  margin: 10px 20px;
}

.ant-tooltip-inner {
  padding: 0;
}
.ant-tooltip-placement-bottomRight .ant-tooltip-arrow {
  border-bottom-color: #ffffff;
}

.expected-hourly-prod > span {
  flex: 1;
  flex-shrink: 0;
}
.d-flex.expected-hourly-prod span:last-child {
  text-align: right;
}
</style>
