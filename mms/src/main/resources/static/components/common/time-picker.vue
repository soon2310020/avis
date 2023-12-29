<template>
  <div class="dropdown-container" :id="id" v-click-outside="closeTimeDropdownStart">
    <a
        href="javascript:void(0)"
        class="time-dropdown time-dropdown-btn"
        :active="visible"
        @click="visible = true"
        :disabled="disabled"
        :style="styleProps"



    ><span class="span-height">{{ getTitle }}</span
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
              <span v-show="isRange && addDay(index, 'hour')"
                    class="bonus-time"
                >{{
                    "+1"
                  }}</span>
              </span>

            </li>
          </ul>
        </div>
        <div class="hour-dropdown-col">
          <ul class="hour-dropdown">
<!--            <span>{{EnableMinute}}</span>-->
            <li
                v-for="index in enableMinute ? 60 : 1"
                :key="index"
                :class="{
                'selected-item': index == +selectedTime.minute + 1,
              }"

            >
              <span
                  class="dropdown-item"
                  onmouseover="this.style.color='black';"
                  onmouseout="this.style.color='black';"
                  :class="{ 'disable-item': !checkEnableMinute(index - 1)}"
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
    id:{
      type:String,
      default: () => "",
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
    disabled: Boolean,
    enableMinute:{
      type: Boolean,
      default:()=> false,
    },
    styleProps: {
      default: '',
      Type: String,
    },
    isRange: {
      type:Boolean,
      default:()=>false
    }

  },
  data() {
    return {
      visible: false,
    };
  },
  watch:{
    isRange(){
      if(this.isRange){
        this.EnableMinute=false
      }

    }
  },
  computed: {

    getTitle() {
      return `${this.selectedTime.hour > 9
          ? this.selectedTime.hour
          : "0" + +this.selectedTime.hour
      }:${this.selectedTime.minute > 9
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
        if (+range.min.hour < +range.max.hour) {
          return +range.max.hour - +range.min.hour

        }
        return 24;
      }
    },

  },
  methods: {
    addDay(index,type){
      let hourRange = this.getTimeRange;
      let result =this.getTimeTitle(index,type)
      let min =+hourRange.min.hour
      if(result <  min ){
        return true
      }else{
        return false
      }

    },
    closeTimeDropdownStart() {
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
        this.selectedTime[type]=result;
          if (this.selectedTime.hour >= 24) {
            this.selectedTime.hour -= 24;
            this.selectedTime.isAddDay = true;
          }
          else{
            this.selectedTime.isAddDay = false;
          }
      }
      else{
        let hourRange = this.getTimeRange;
        console.log(hourRange)
        result += +hourRange.min.hour;
        this.selectedTime[type]=result;
      }
      this.$emit("change-time", this.selectedTime, type);
    },
  },
  mounted(){
    if(this.isRange){
      this.EnableMinute=false
    }
  }


};
</script>

<style scoped>
.span-height{
  height: 12px !important;
  font-weight: 400 !important;
}
.dropdown-menu{
  top: 27px;
  border: unset !important;
  box-shadow: 0px 3px 6px rgb(0 0 0 / 16%) !important;
  border-radius: 3px !important;
}
.dropdown-menu .hour-dropdown .selected-item,
.dropdown-menu:not(.header-dropdown) .hour-dropdown .dropdown-item {
  width: 46px !important;
  min-width: 46px !important;
}
.hour-dropdown{
  margin-left: 2px !important;
  margin-top: 2px !important;
}
.dropdown-item {
  padding: 10px 16px !important;
  color: black;
  margin-right: 8px !important;
  padding-right: 14px !important;
  padding-left: 14px !important;
  padding-top: 11px !important;
  padding-bottom: 11px !important;
  margin-bottom: 2px !important;

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

.time-dropdown span:not(.bonus-time):hover {
  /*text-decoration: underline !important;*/
  color: #0e65c7;
}


.dropdown-container {
  position: relative;
  width: 105px;
  height: 34px;
}
.dropdown-container a {
  position: absolute;
}
.time-dropdown {
  height: unset !important;
  color: #3491fb;
  text-decoration: none;
  background-color: #f5f5f5;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  text-align: right !important;
  font-family: 'Helvetica Neue' !important;
  font-style: normal;
  font-weight: 400 !important;
  font-size: 11.25px !important;
  line-height: 13px;
  padding: 6px 8px;


}
.time-dropdown-btn[active]{
background: #DAEEFF;
}
.time-dropdown {
  padding: 9px 0 9px 15px;
  width: 105px;
}
.dropdown-container a {
  position: absolute;
}
.time-dropdown-btn {
  display: flex;
  border: 0.5px solid #909090;
  border-radius: 3px;
  color: #4B4B4B;
  flex: none;
  order: 0;
  flex-grow: 0;

}
.time-dropdown-btn:hover{
  background: #DAEEFF;
  border-radius: 3px;
  border: 0.5px solid #3585E5 !important;
  color: #3585E5;
}
.time-dropdown-btn[active]{
  background: #DAEEFF;
  border: 1px solid #3585E5;
  border-radius: 3px;
  color: #3585E5;
}
.time-dropdown-active{
  background: #DAEEFF;
  border: 1px solid #3585E5;
  border-radius: 3px;
  color: #3585E5;
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