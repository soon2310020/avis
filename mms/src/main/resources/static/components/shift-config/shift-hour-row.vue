<template>
  <div class="d-flex shift-row">
    <hour-input
      :key="shiftHour.id"
      :editable="editable"
      :selected-time="getSelectedTime.start"
      :type="'start'"
      :min-time="hourRange.start.min"
      :max-time="hourRange.start.max"
      :is-add-day="hourRange.start.isAddDay"
      @change-time="handleChangeHour"
      @submit-time="changeShiftTime"
    ></hour-input>
    <div class="minus-sign"></div>
    <hour-input
      :key="shiftHour.id"
      :editable="editable"
      :selected-time="getSelectedTime.end"
      :type="'end'"
      :min-time="hourRange.end.min"
      :max-time="hourRange.end.max"
      :is-add-day="hourRange.end.isAddDay"
      @change-time="handleChangeHour"
      @submit-time="changeShiftTime"
    ></hour-input>
    <div style="margin-left: 4px; cursor: pointer" >
      <img v-show="enableDelete" @click="resetRowData" src="/images/icon/shift-config/delete-icon.svg" alt="" />
      <div v-show="!enableDelete" style="width: 20px; height: 20px;">

      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    index: {
      type: Number,
      default: () => (0)
    },
    shiftName: {
      type: String,
      default: () => ('')
    },
    shiftHour: {
      type: Object,
      default: () => ({}),
    },
    editable: {
      type: Boolean,
      default: () => (false)
    },
    getHourRange: Function
  },
  components: {
    "hour-input": httpVueLoader(
      "/components/shift-config/shift-hour-input.vue"
    ),
  },
  computed: {
    enableDelete(){
      if ((this.shiftHour.start == '' && this.shiftHour.end == '') || !this.editable) {
        return false
      }
      return true
    },
    getSelectedTime() {
      let result = {
        start: {
          hour: '',
          minute: '',
          isAddDay: false
        },
        end: {
          hour: '',
          minute: '',
          isAddDay: false
        },
      }
      if (this.shiftHour.start) {
        result.start.hour = +this.shiftHour.start.substr(0, 2)
        result.start.minute = +this.shiftHour.start.substr(2, 2)
      }
      if (this.shiftHour.end) {
        result.end.hour = +this.shiftHour.end.substr(0, 2)
        result.end.minute = +this.shiftHour.end.substr(2, 2)
      }
      return result
    },
    hourRange(){
      let range = {
        start: this.getHourRange(this.shiftName, this.index, 'start'),
        end: this.getHourRange(this.shiftName, this.index, 'end')
      }
      return range
    }
  },
  methods: {
    handleChangeHour(data, type) {
      this.$emit("change-shift-hour", this.shiftName, data, this.index, type)
    },
    changeShiftTime() {
      this.$emit("submit-time")
    },
    resetRowData() {
      console.log('reset row data', this.shiftName, this.index)
      this.$emit('reset-row', this.shiftName, this.index)
    }
  },
}
</script>

<style scoped>
.minus-sign {
  height: 1px;
  width: 9px;
  background-color: #707070;
}
</style>