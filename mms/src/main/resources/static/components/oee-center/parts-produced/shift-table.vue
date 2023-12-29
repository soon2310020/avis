<template>
  <div class="parts-produced-shift" style="margin-bottom: 10px">
    <div class="shift-title-container">
      <div
        v-for="(item, index) in getShowedShift"
        :key="index"
        class="shift-title"
        :style="getTitleWidth(item.elements)"
      >
        <span v-if="item.elements">
          {{ item.name }}
        </span>
      </div>
    </div>
    <div
      v-for="(shiftDay, index) in dataList"
      :key="index"
      class="shift-container"
    >
      <div
        v-for="(shiftHour, i) in shiftDay.shiftData"
        :key="i"
        class="shift-column"
      >
        <div v-if="index == 0" class="shift-hour">
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
          @click="handleClickShift($event, shiftDay, shiftHour)"
        >
          <div style="height: 6px; width: 100%"></div>
          <label
            class="shift-hour__title"
            :class="{ 'shift-text-10': `${shiftHour.partProduced}`.length > 3 }"
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
                    v-for="(item, index) in shiftHour.machineMoldHistoryData"
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
</template>

<script>
module.exports = {
  props: {
    dataList: {
      type: Array,
      default: () => [],
    },
    numberOfShift: {
      type: Number,
      default: () => 4,
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
    rawShifts: Array,
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
    data() {
      console.log("this.listDisplayedShifts", this.listDisplayedShifts);
      console.log("this.dataList", this.dataList);
      return "";
    },
    getShowedShift() {
      const checkedItems = this.listDisplayedShifts.filter(
        (item) => item.checked
      );
      if (checkedItems.length === 0) {
        return this.rawShifts.map((item, index) => {
          const newItem = { ...item };
          newItem.element = this.dataList[0].shiftData.filter(
            (i) => i.shiftNumber === index + 1
          ).length;
          return newItem;
        });
      }
      console.log("getShowedShift", checkedItems, this.rawShifts);
      return checkedItems.map((item, index) => {
        const newItem = { ...item };
        newItem.elements = this.dataList[0].shiftData.filter(
          (i) => i.shiftNumber === index + 1
        ).length;
        return newItem;
      });
    },
    totalShiftCount() {
      return this.dataList[0].shiftData.length;
    },
  },
  methods: {
    getTitleWidth(element) {
      const result = Math.ceil((element * 100) / this.totalShiftCount);
      return { width: `${result > 0 ? result - 1 : 0}%` };
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
      this.$emit("click", event, shiftDay, shiftHour);
    },
    hasData(oee) {
      return oee !== undefined && oee !== null;
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
    getActiveProgressStyle(progress) {
      const style = { width: `${progress}%` };
      console.log("getActiveProgressStyle", style);
      return style;
    },
    getMachineMoldHistoryData(shiftHour) {
      if (shiftHour) {
        if (shiftHour.machineMoldHistoryData) {
          return shiftHour.machineMoldHistoryData;
        }
      }
      return [];
    },
  },
  watch: {
    // dataList(newVal) { console.log('dataList', newVal) },
    // numberOfShift(newVal) { console.log('numberOfShift', newVal) },
    listDisplayedShifts(newVal) {
      console.log("listDisplayedShifts", newVal);
    },
  },
};
</script>

<style scoped>
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

.ant-tooltip-inner {
  padding: 0;
}
.ant-tooltip-placement-bottomRight .ant-tooltip-arrow {
  border-bottom-color: #ffffff;
}
</style>
