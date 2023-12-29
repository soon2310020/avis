<template>
  <div class="new-content-detail">
    <div style="width: 68%" @click="removeErrorMess">
      <a-date-picker
        id="downtime-end-planned"
        @change="handleChangeDate($event, type)"
        :allow-clear="false"
        format="YYYY-MM-DD (dddd)"
        v-model="dateTime.date"
        style="width: 100%"
        :disabled-date="disableDate"
        :disabled="disabled"

      >
        <svg
          slot="suffixIcon"
          xmlns="http://www.w3.org/2000/svg"
          width="14.188"
          height="14.426"
          viewBox="0 0 14.188 14.426"
        >
          <g
            id="Component_1_9"
            data-name="Component 1 – 9"
            transform="translate(0.25 0.25)"
          >
            <path
              id="Subtraction_4"
              data-name="Subtraction 4"
              d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
              transform="translate(0 0)"
              fill="#9d9d9d"
              stroke="#9d9d9d"
              stroke-width="0.5"
            />
            <path
              id="Subtraction_2"
              data-name="Subtraction 2"
              d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
              transform="translate(7.504 7.742)"
              fill="#9d9d9d"
            />
          </g>
        </svg>
      </a-date-picker>
    </div>
    <div class="dropdown-container" v-click-outside="closeTimeModel">
      <a
        href="javascript:void(0)"
        class="time-dropdown"
        @click="openCloseTimeDropdown(type, true)"
      >
        {{ dateTime.hour >= 10 ? dateTime.hour : "0" + dateTime.hour }}:{{
          dateTime.minute >= 10 ? dateTime.minute : "0" + dateTime.minute
        }}
      </a>
      <div
        style="
          left: auto !important;
          width: 105px;
          transform: translate(0px, 10px);
          overflow-y: unset;
        "
        class="dropdown-menu"
        :class="{ show: isShowDropdown }"
      >
        <div style="display: flex">
          <div class="hour-dropdown-col">
            <ul class="hour-dropdown">
              <li
                v-for="index in 24"
                :key="index"
                :class="{ 'selected-item': index === dateTime.hour + 1 }"
              >
                <span
                  class="dropdown-item"
                  @click="handleChangeTime(index - 1, type, 'hour')"
                >
                  {{ `${index - 1 >= 10 ? index - 1 : "0" + (index - 1)}` }}
                </span>
              </li>
            </ul>
          </div>
          <div class="hour-dropdown-col">
            <ul class="hour-dropdown">
              <li
                v-for="index in 60"
                :key="index"
                :class="{ 'selected-item': index === dateTime.minute + 1 }"
              >
                <span
                  class="dropdown-item"
                  @click="handleChangeTime(index - 1, type, 'minute')"
                >
                  {{ `${index - 1 >= 10 ? index - 1 : "0" + (index - 1)}` }}
                </span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    resources: Object,
    handleChangeTime: Function,
    handleChangeDate: Function,
    removeErrorMess: Function,
    type: '',
    isShowDropdown: {
      type: Boolean,
      default: false
    },
    openCloseTimeDropdown: Function,
    dateTime: {
      date: Object,
      hour: Number,
      minute: Number,
    },
    startDowntime: Object,
    endDowntime: Object
  },
  data() {
    return {
    }
  },
  methods: {
    closeTimeModel() {
      this.openCloseTimeDropdown(this.type, false)
    },
    disableDate(current) {
      return current < this.startDowntime.startOf('day') || current > this.endDowntime.endOf('day');
    }
  },
  watch: {
    startDowntime(newTime, oldTime){
      if(!this.dateTime.date && !!newTime && this.type == 'start'){
        this.dateTime.date = newTime;
        this.dateTime.hour = moment(newTime).hours();
        this.dateTime.minute = moment(newTime).minutes();

      }
    },
    endDowntime(newTime, oldTime){
      if(!this.dateTime.date && !!newTime && this.type == 'end'){
        this.dateTime.date = newTime;
        this.dateTime.hour = moment(newTime).hours();
        this.dateTime.minute = moment(newTime).minutes();
      }
    }
  }
}
</script>

<style scoped>
.dropdown-menu:not(.header-dropdown) {
  padding: 1px !important;
  box-shadow: rgb(100 100 111 / 20%) 0px 7px 29px 0px;
  border: none !important;
  border-radius: 3px !important;
  transform: translate(-10px, 10px);
  min-width: 0px !important;
}
.time-dropdown,
.ant-input:not(.tool-wrapper .ant-input) {
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
  width: 105px;
}
.time-dropdown:hover {
  text-decoration: underline !important;
  color: #0e65c7;
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
.dropdown-menu:not(.header-dropdown) {
  transform: translate(1px, 10px);
  max-height: 145px;
  overflow-y: auto;
}
.dropdown-container {
  position: relative;
  width: 105px;
  height: 34px;
}
.dropdown-container a {
  position: absolute;
}
.new-content-detail {
  display: flex;
  justify-content: space-between;
}
.ant-calendar-picker-icon {
  left: 10px;
}
.ant-input {
  padding: 9px 0 9px 35px;
  width: 340px;
  height: 35px;
  margin-right: 10px;
}
</style>