<template>
  <div class="date-block">
    <div v-if="!isRange">
      <v-date-picker
        v-model="singleDate"
        locale="us-US"
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :max-date="today"
        @dayclick="changeDate"
      ></v-date-picker>
    </div>
    <div v-else class="date-range">
      <v-date-picker
        v-model="range"
        locale="us-US"
        is-range
        :masks="{ title: 'MMMM YYYY', weekdays: 'WWW' }"
        :first-day-of-week="2"
        :max-date="today"
        :available-dates="{
          start: dateRange.minDate,
          end: dateRange.maxDate,
        }"
        @dayclick="chooseRange"
      ></v-date-picker>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    isRange: {
      type: Boolean,
      default: () => false,
    },
    currentDate: {
      type: Object,
      default: () => ({}),
    },
    dateRange: {
      type: Object,
      default: () => ({
        minDate: null,
        maxDate: new Date(),
      }),
    },
    frequency: {
      type: String,
      default: () => "",
    },
  },
  data() {
    return {
      date: this.currentDate,
      singleDate: new Date(),
      range: {
        start: null,
        end: null,
      },
      dateTitle: {
        start: "",
        end: "",
      },
    };
  },
  created() {
    this.today = new Date();
  },
  mounted(){
    document.getElementsByClassName('vc-arrow is-left')[0].addEventListener('click', this.handleCalendar);
    document.getElementsByClassName('vc-arrow is-right')[0].addEventListener('click', this.handleCalendar);
    var data = document.getElementsByClassName('vc-title')[0].innerHTML;
    var newdata = data.replace(/ /g, '<span style="color:#3491FF; font-weight: 500;"> ') + '</span>';
    console.log(newdata,'yes');
    console.log(data, 'current data is yes');
    document.getElementsByClassName('vc-title')[0].innerHTML = newdata;
  },
  watch: {
    range() {},
    frequency(newValue) {
      if (newValue.includes("DATE")) {
        console.log('%c DATE is selected', 'background : #FF8C2C, color : #ffffff')
        this.clearPickingData();
      }
    },
  },
  methods: {
     handleCalendar(){
    console.log('mouse is up =====================')
    setTimeout(() => {
    var dataa = document.getElementsByClassName('vc-title')[0].innerHTML;
    var newDataa = dataa.replace(/ /g, '<span style="color:#3491FF; font-weight: 500;"> ') + '</span>';
    console.log(dataa, 'current data is yes');
    document.getElementsByClassName('vc-title')[0].innerHTML = newDataa;
    console.log("i am clicked--------")
      }, 180);
    
     },
    clearPickingData() {
      this.singleDate = new Date();
      this.range = {
        start: new Date(),
        end: new Date(),
      };
      this.dateTitle = {
        start: moment(new Date()).format("YYYY-MM-DD"),
        end: moment(new Date()).format("YYYY-MM-DD"),
      };
      // this.singleDate = new Date();
      // this.range = {
      //   start: null,
      //   end: null,
      // };
      // this.dateTitle = {
      //   start: "",
      //   end: "",
      // };
      this.changeDate();
    },
    setDefault(date) {
      console.log('setDefault', date)
      if (this.isRange) {
        this.range = {
          start: date.from,
          end: date.to,
        };
      } else {
        this.singleDate = date.from;
      }
      this.dateTitle = {
        start: date.fromTitle,
        end: date.toTitle,
      };
    },
    getFrequency() {
      return this.isRange ? "DAILY_RANGE" : "DAILY";
    },
    changeDate() {
      let selectRange = this.getDateRange();
      let title = {
        start: selectRange.start
          ? moment(selectRange.start).format("YYYY-MM-DD")
          : "",
        end: selectRange.end
          ? moment(selectRange.end).format("YYYY-MM-DD")
          : "",
      };
      this.$emit("change-date", selectRange, title);
    },
    getDateRange() {
      let startDate = null;
      let endDate = null;
      if (!this.isRange) {
        startDate = this.singleDate;
        endDate = this.singleDate;
      } else {
        startDate = this.range.start;
        endDate = this.range.end;
      }
      return {
        start: startDate,
        end: endDate,
      };
    },
    changeRange(newRange) {
      this.range = {
        start: moment(newRange.start, "YYYY-MM-DD").toDate(),
        end: moment(newRange.end, "YYYY-MM-DD").toDate(),
      };
    },
    chooseRange() {
      if (this.range.start && this.range.end) {
        this.changeDate();
      }
    },
  },
};
</script>

<style scoped>
.vc-popover-caret.direction-bottom.align-center{
  display: none;
}
.vc-popover-content-wrapper {
width: 100%;
transform: translate(0px, 24px) !important;

}

.vc-svg-icon{
  width: 100% !important;
  height: 100%  !important;
}

.vc-nav-popover-container{
background: #4B4B4B  !important;
border-radius: 3px !important;
}
.vc-nav-arrow.is-left{
 position: absolute;
    top: 75px;
    width:17px;
    height: 17px;
    left: 5;
    background: #C4C4C4;
    border-radius: 3px;

}

.vc-nav-arrow.is-right {
    margin-left: auto;
     width:17px;
    height: 17px;
    position: absolute;
    background: #C4C4C4;
    border-radius: 3px;
    top: 75px;
    right: 5px;
}

.vc-nav-title{
      width: 100% !important;
    display: flex !important;
    align-items: center;
    align-content: center;
    justify-content: center;
}



.vc-nav-items{
  justify-items: center;
}

.block-footer {
  font-size: 11px;
  color: #838383;
  position: unset;
}
</style>