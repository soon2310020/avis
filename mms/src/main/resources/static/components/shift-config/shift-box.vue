<template>
  <div
    class="shift-box-container"
    :class="{
      'row-size-3': size == 24,
      'row-size-2 mg-r-164': size == 12,
      'row-size-1': size == 8 || size == 6,
    }"
  >
    <hour-row
      v-for="(shiftHour, index) in shift"
      :key="shiftHour.id"
      :index="index"
      :editable="editable"
      :shift-hour="shiftHour"
      :shift-name="shiftName"
      :get-hour-range="getHourRange"
      @change-shift-hour="handleChangeHour"
      @submit-hour="handleSubmitHour"
      @reset-row="handleResetRow"
    ></hour-row>
  </div>
</template>

<script>
module.exports = {
  props: {
    size: {
      type: Number,
      default: () => 6,
    },
    editable: {
      type: Boolean,
      default: () => false,
    },
    shift: {
      type: Object,
      default: () => ({}),
    },
    shiftName: {
      type: String,
      default: () => "shift1",
    },
    getHourRange: Function,
  },
  components: {
    "hour-row": httpVueLoader(
      "/components/shift-config/shift-hour-row.vue"
    ),
  },
  data() {
    return {};
  },
  watch:{
    shift(newValue){
      console.log('watch shift', newValue)
    }
  },
  mounted() {},
  methods: {
    handleChangeHour(shiftName, data, index, type) {
      this.$emit("change-hour", shiftName, data, index, type);
    },
    handleSubmitHour(){
      this.$emit("submit-hour");
    },
    handleResetRow(shiftName, index){
      this.$emit("reset-row", shiftName, index);
    }
  },
};
</script>

<style scoped>
.shift-row {
  width: 170px;
  justify-content: space-between;
  align-items: center;
}
.time-input {
  width: 56px;
  height: 30px;
  border: none;
  outline: 1px solid #909090;
  border-radius: 2px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.time-input:hover {
  outline: 1px solid #4b4b4b;
}
.time-input:focus {
  outline: 2px solid #deedff;
}
.shift-box-container {
  display: grid;
  row-gap: 10px;
  column-gap: 52px;
}
.row-size-3 {
  grid-template-rows: 30px 30px 30px 30px 30px 30px 30px 30px;
  grid-auto-flow: column;
}
.row-size-4 {
  grid-template-columns: 150px 150px 150px 150px;
}
.row-size-2 {
  grid-template-columns: 150px 150px;
}
.row-size-1 {
  grid-template-columns: 150px;
}
.time-input input {
  border: unset !important;
  outline: unset !important;
  width: 25px;
  text-align: center;
}
.time-input input::-webkit-outer-spin-button,
.time-input input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.inactive-shift {
  color: #c9c9c9 !important;
  border: 1px solid #909090;
  pointer-events: none;
}
</style>