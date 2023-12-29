<template>
  <div>
    <div class="dropdown-container" v-click-outside="closeTimeDropdownStart">
    <a
      href="javascript:void(0)"
      class="hour-picker hour-picker-btn"
      :class="{'inactive-shift' : !editable}"
      ><span>{{ getTitle }}</span
      ><span v-show="selectedTime.isAddDay" class="bonus-time">{{
        "+1"
      }}</span></a
    >
    <div
      style="
        left: auto !important;
        width: 105px;
        transform: translate(0px, 10px);
        overflow-y: unset;
      "
      class="dropdown-menu"
      :class="[visible ? 'show' : '']"
    >
      <div style="display: flex">
        <div class="hour-dropdown-col">
          <ul class="hour-dropdown">
            <li
              v-for="index in getHourRange"
              :key="index"
              :class="{
                'selected-item': checkSelectedHour(index),
              }"
            >
              <span
                class="dropdown-item"
                onmouseover="this.style.color='black';"
                onmouseout="this.style.color='black';"
                @click="handleChangeTime(index - 1, 'hour')"
              >
                {{ getTimeTitle(index, "hour") }}
              </span>
            </li>
          </ul>
        </div>
        <div class="hour-dropdown-col">
          <ul class="hour-dropdown">
            <li
              v-for="index in 1"
              :key="index"
              :class="{
                'selected-item': index == +selectedTime.minute + 1,
              }"
            >
              <span
                class="dropdown-item"
                onmouseover="this.style.color='black';"
                onmouseout="this.style.color='black';"
                :class="{ 'disable-item': !checkEnableMinute(index - 1) }"
                @click="handleChangeTime(index - 1, 'minute')"
              >
                {{ getTimeTitle(index, "minute") }}
              </span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    editable: {
      type: Boolean,
      default: () => false,
    },
    shiftData: {
      type: Object,
      default: () => ({}),
    },
    index: {
      type: Number,
      default: () => 0,
    },
    minTime: {
      type: String,
      default: () => "0000",
    },
    maxTime: {
      type: Object,
      default: () => "2359",
    },
    selectedTime: {
      type: Object,
      default: () => ({
        hour: "0",
        minute: "0",
        isAddDay: false,
      }),
    },
    type: String,
    isAddDay: Boolean
  },
  data() {
    return {
      visible: false,
    };
  },
  computed: {
    getTitle() {
      if (this.selectedTime.hour !== '') {
        let hour = this.selectedTime.hour > 23 ? this.selectedTime.hour - 24 : this.selectedTime.hour
        return `${hour > 9
          ? hour
          : "0" + +hour
          }:${this.selectedTime.minute > 9
            ? this.selectedTime.minute
            : "0" + +this.selectedTime.minute
          }`;
      } else {
        return '--:--'
      }

    },
    getTimeRange() {
      let minTime = {
        hour: this.minTime.substr(0, 2),
        minute: this.minTime.substr(2, 2),
      };
      let maxTime = {
        hour: this.maxTime.substr(0, 2),
        minute: this.maxTime.substr(2, 2),
      };
      return {
        min: minTime,
        max: maxTime,
      };
    },
    getHourRange() {
      let range = this.getTimeRange;
      let result = 1
      if (range.min.hour > range.max.hour) {
        result = (+range.max.hour + 24 - +range.min.hour) + 1;
      } else {
        result = (+range.max.hour - +range.min.hour) + 1;
      }
      if (result <= 0) {
        result = 1
      }
      return result 
    },
  },
  methods: {
    closeTimeDropdownStart() {
      if (this.visible) {
        this.$emit("submit-time");
      }
      this.visible = false;
    },
    checkSelectedHour(index) {
      let hourRange = this.getTimeRange;
      if (this.selectedTime.hour != '') {
        if (this.selectedTime.isAddDay) {
          return index == +this.selectedTime.hour + 1 - +hourRange.min.hour + 24;
        } else {
          return index == +this.selectedTime.hour + 1 - +hourRange.min.hour;
        }
      }
      return false
    },
    getTimeTitle(index, type) {
      let result = index;
      let hourRange = this.getTimeRange;
      if (type == "hour") {
        result = +hourRange.min.hour + index;
        if (result > 24) {
          result -= 24;
        }
      }
      return `${result - 1 >= 10 ? result - 1 : "0" + (result - 1)}`;
    },
    checkEnableMinute(index) {
      let hourRange = this.getTimeRange;
      if (this.selectedTime.minute != '') {
        if (this.selectedTime.isAddDay) {
          if (this.selectedTime.hour == hourRange.max.hour) {
            if (index > hourRange.max.minute) {
              return false;
            }
          }
        }
        return true;
      }
      return false

    },
    handleChangeTime(time, type) {
      let result = time;
      if (type == "hour") {
        let hourRange = this.getTimeRange;
        result = +hourRange.min.hour + time;
      }
      this.$emit("change-time", result, this.type);
    },
  },
};
</script>

<style scoped>
.dropdown-menu .hour-dropdown .selected-item,
.dropdown-menu:not(.header-dropdown) .hour-dropdown .dropdown-item {
  width: 46px !important;
  min-width: 46px !important;
}
.hour-dropdown {
  list-style: none;
  padding: 0;
  width: 50px;
}
.hour-dropdown-col {
  overflow-y: auto;
  max-height: 145px;
}
.hour-dropdown-col:last-child {
  margin-left: 1px;
}
.hour-dropdown-col::-webkit-scrollbar {
  width: 4px;
  background-color: #fff;
}
.hour-dropdown-col::-webkit-scrollbar-thumb {
  background: #f5f5f5;
}

.hour-picker span:not(.bonus-time):hover {
  text-decoration: underline !important;
  color: #0e65c7;
}
.dropdown-container {
  position: relative;
  width: 56px;
  height: 31px;
}

.hour-picker {
  font-size: 14px;
  color: #3491fb;
  text-decoration: none !important;
  background-color: #ffffff;
  border: none;
  line-height: 18px;
  border-radius: 3px;
  cursor: pointer;
  text-align: left !important;
}
.hour-picker {
  padding: 9px 0 9px 15px;
  width: 56px;
}
.dropdown-container a {
  position: absolute;
  color: #4b4b4b;
  border: 1px solid #909090;
  padding: 6px 8px;
  display: flex;
  justify-content: center;
}
.dropdown-container a:hover {
  color: #4b4b4b;
  text-decoration: none;
}
.hour-picker-btn {
  display: flex;
}
.bonus-time {
  color: #000000;
  font-size: 11px;
  position: absolute;
  right: 38%;
  top: 2px;
}
.disable-item {
  opacity: 0.7;
  pointer-events: none;
}
.hour-picker-btn:hover {
  color: rgb(75, 75, 75);
}
</style>