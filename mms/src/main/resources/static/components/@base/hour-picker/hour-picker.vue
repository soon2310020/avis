<template>
  <div class="dropdown-container" v-click-outside="closeTimeDropdownStart">
    <base-button
      v-if="!disabled"
      :size="size"
      :type="type"
      :level="level"
      @click="handleClick"
    >
      <span>{{ getTitle }}</span>
      <span v-show="selectedTime.isAddDay" class="bonus-time">{{ "+1" }} </span>
    </base-button>
    <base-input
      v-else
      style="width: 64px"
      :readonly="true"
      :value="getTitle"
    ></base-input>
    <div
      style="
        left: auto !important;
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
              v-for="index in 60"
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
    minTime: {
      type: String,
      default: "0000",
    },
    maxTime: {
      type: String,
      default: "2359",
    },
    selectedTime: {
      type: Object,
      default: () => ({
        hour: "0",
        minute: "0",
        isAddDay: false,
      }),
    },
    size: {
      type: String,
      default: "small",
      validator(val) {
        return ["large", "medium", "small"].includes(val);
      },
    },
    type: {
      type: String,
      default: "cancel",
    },
    level: {
      type: String,
      default: "secondary",
    },
    canDoublicate: {
      type: Boolean,
      default: () => true,
    },
    disabled: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      visible: false,
    };
  },
  computed: {
    getTitle() {
      return `${
        this.selectedTime.hour > 9
          ? this.selectedTime.hour
          : "0" + +this.selectedTime.hour
      }:${
        this.selectedTime.minute > 9
          ? this.selectedTime.minute
          : "0" + +this.selectedTime.minute
      }`;
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
      if (range.min.hour == range.max.hour) {
        return 24;
      } else {
        // console.log("getHourRange 1");
        if (+range.min.hour < +range.max.hour) {
          // console.log("getHourRange 2", range);
          if (this.canDoublicate) {
            return +range.max.hour - +range.min.hour + 1;
          }
          return +range.max.hour - +range.min.hour;
        }
        return 24;
      }
    },
  },
  methods: {
    handleClick() {
      console.log("handleClick");
      this.visible = true;
      this.$emit("click");
    },
    closeTimeDropdownStart() {
      // console.log("closeTimeDropdownStart");
      if (this.visible) {
        this.$emit("submit-time");
      }
      this.visible = false;
    },
    checkSelectedHour(index) {
      let hourRange = this.getTimeRange;
      if (this.selectedTime.isAddDay) {
        return index == +this.selectedTime.hour + 1 - +hourRange.min.hour + 24;
      } else {
        return index == +this.selectedTime.hour + 1 - +hourRange.min.hour;
      }
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
      if (this.selectedTime.isAddDay) {
        if (this.selectedTime.hour == hourRange.max.hour) {
          if (index > hourRange.max.minute) {
            return false;
          }
        }
      }
      return true;
    },
    handleChangeTime(time, type) {
      let result = time;
      if (type == "hour") {
        let hourRange = this.getTimeRange;
        result += +hourRange.min.hour;
      }
      this.$emit("change-time", result, type);
    },
  },
};
</script>

<style scoped>
.dropdown-menu {
  min-width: unset;
}
.dropdown-menu:not(.header-dropdown) {
  padding: 1px !important;
  box-shadow: rgb(100 100 111 / 20%) 0px 7px 29px 0px;
  border: none !important;
  border-radius: 3px !important;
  transform: translate(-10px, 10px);
  min-width: 0px !important;
}
.dropdown-menu .hour-dropdown .selected-item,
.dropdown-menu:not(.header-dropdown) .hour-dropdown .dropdown-item {
  width: 46px !important;
  min-width: 46px !important;
}
.dropdown-menu .dropdown-item {
  padding: 11px 14px 11px 14px;
  display: inline-block;
  text-align: center;
  vertical-align: middle;
  width: unset;
  font-size: 0.875rem;
  line-height: 1.5;
}
.dropdown-item,
.selected-item {
  /* width: 46px !important; */
  min-width: 46px !important;
}
.dropdown-menu:not(.header-dropdown) .dropdown-item:hover,
.dropdown-menu .selected-item {
  background-color: #e6f7ff !important;
}
.dropdown-menu:not(.header-dropdown) .dropdown-item {
  padding: 4px 9px !important;
  color: rgba(0, 0, 0, 0.65) !important;
}
.hour-dropdown {
  list-style: none;
  padding: 0;
  margin-bottom: 0;
}
.hour-dropdown-col {
  overflow-y: auto;
  max-height: 145px;
}
/* .hour-dropdown-col:last-child {
  margin-left: 1px;
} */
.hour-dropdown-col::-webkit-scrollbar {
  width: 4px;
  background-color: #fff;
}
.hour-dropdown-col::-webkit-scrollbar-thumb {
  background: #f5f5f5;
}

.time-dropdown span:not(.bonus-time):hover {
  text-decoration: underline !important;
  color: #0e65c7;
}
.dropdown-container {
  position: relative;
}

.time-dropdown {
  font-size: 14px;
  color: #3491fb;
  text-decoration: none;
  background-color: #f5f5f5;
  border: none;
  line-height: 16px;
  border-radius: 3px;
  cursor: pointer;
  text-align: left !important;
}
.time-dropdown {
  padding: 9px 0 9px 15px;
}

.time-dropdown-btn {
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
</style>
