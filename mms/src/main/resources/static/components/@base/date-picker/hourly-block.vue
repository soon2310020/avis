<template>
  <div class="date-block block-container">
    <div class="hourly block" style="margin: 0 auto;">
      
      <div class="wrapper-hour">
        <div v-for="hour in 24" :key="hour">
          <div
            @click="handleSelectHour(hour -1)"
            class="hour-item-style"
            :class="{ 'selected-hour-item': isSelectedHourRange(hour-1) }"
          >
            <span
              class="txt-hour-item"
              :class="{ 'txt-selected-hour': isSelectedHourRange(hour-1) }"
              >{{ getHourRangeTitle(hour-1) }}</span
            >
          </div>
        </div>
      </div>
      
    </div>
  </div>
</template>

<script>
module.exports = {
  data() {
    return {
      selectedHour: null,
    };
  },
  created() {
    this.today = new Date();
  },
  methods: {
    parseHourToString(hourNumber){
      return (hourNumber >= 10 ? hourNumber : "0" + hourNumber) + ''
    },
    getHourTitle(hourNumber) {
      if (hourNumber > 23) {
        return "00:00";
      }
      return this.parseHourToString(hourNumber) + ":00";
    },
    generateRangeFromStartTime (hour){
      return {
        from: this.parseHourToString(hour),
        to: this.parseHourToString(hour+1 >23 ? 0 : hour+1)
      }
    },
    getHourRangeTitle(fromHourItem) {
      const min = this.getHourTitle(fromHourItem);
      const max = this.getHourTitle(fromHourItem+1);
      return min + " - " + max;
    },
    getCurrentTimeRange() {
      this.selectedHour = this.today.getHours();
    },
    isSelectedHourRange(hour) {
      return hour == this.selectedHour;
    },
    handleSelectHour(hour) {
      this.selectedHour = hour;
      this.$emit('change', this.generateRangeFromStartTime(hour))
    },
  },
  mounted() {
    this.getCurrentTimeRange();
  },
};
</script>

<style scoped>
.hour-item-style,
.selected-hour-item {
  border-radius: 3px;
  border: 0.5px solid #8b8b8b;
  cursor: pointer;
}
.selected-hour-item {
  border: 0.5px solid #3585e5;
  background-color: #daeeff;
}
.txt-hour-item {
  color: #4b4b4b;
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  width: 83px;
  display: inline-block;
  padding: 6.34px 8px;
  white-space: nowrap;
}
.txt-selected-hour {
  color: #3585e5;
}
.block-container {
  padding: 12px 0;
}
</style>
<style scoped src="/components/@base/date-picker/date-picker-style.css"></style>
