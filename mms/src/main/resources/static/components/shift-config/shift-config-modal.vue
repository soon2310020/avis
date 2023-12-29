<template>
  <div
    id="shift-config-modal"
    class="modal fade"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content" ref="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">{{
              "Plants Shift Configuration"
            }}</span>
          </div>
          <div
            class="t-close-button close-button"
            @click="dimissModal"
            aria-hidden="true"
          >
            <img src="/images/icon/category/close.svg" alt="" />
          </div>
          <div class="head-line"></div>
        </div>
        <div v-show="isLoading" class="shift-config-loading-wave"></div>
        <div v-show="!isLoading" class="modal-body">
          <div class="modal-body__header__infor">
            <div class="modal-body__title">
              <div class="title__name">
                {{ locations.length > 1 ? "Plants" : "Plant" }}
              </div>
              <span v-if="locations.length == 1">{{
                location.locationTitle
              }}</span>
              <location-selection
                v-else
                :location-list="locations"
                :selected-location="location"
                @select="handleChangeLocation"
              ></location-selection>
            </div>
            <div v-if="locations.length == 1" class="modal-body__title">
              <div class="title__name">Plant ID</div>
              <span class="">{{ location.locationCode }}</span>
            </div>
          </div>
          <option-button-group
            :option-list="dayList"
            :selected-option="selectedDayOption"
            @change-option="changeDays"
          ></option-button-group>
          <div class="d-flex hour-picker-container">
            <div class="single-container">
              <div class="title__name" style="margin-bottom: 10px">
                Day Start
              </div>
              <hour-picker
                @change-time="handleChangeStartDate"
                @submit-time="changeShiftTime"
                :min-time="'0000'"
                :max-time="getStartHourRangePicker"
                :selected-time="dayStart"
              ></hour-picker>
            </div>
            <div class="single-container">
              <div class="title__name" style="margin-bottom: 10px">Day End</div>
              <hour-picker
                @change-time="handleChangeEndDate"
                @submit-time="changeShiftTime"
                :min-time="getEndHourRangePicker"
                :max-time="getEndHourRangePicker"
                :selected-time="dayEnd"
              ></hour-picker> 
            </div>
            <div class="single-container" style="margin-left: 15px">
              <div class="title__name" style="margin-bottom: 13px">
                Number of Shifts
              </div>
              <custom-dropdown
                :list-item="shiftOptions"
                :title-field="'title'"
                :default-title="'Select shift'"
                :default-item="selectedShiftType"
                @change="handleSelectShift"
              ></custom-dropdown>
            </div>
          </div>
          <div
            class="d-flex shift-hour-container"
            style="margin-top: 15px; margin-bottom: 15px"
          >
            <div class="title__name">Shift Hours</div>
            <div class="d-flex shift-type">
              <input
                id="auto-type-rd"
                type="radio"
                v-model="assignType"
                :value="true"
              /><label for="auto-type-rd" style="margin: 0 0 0 5px"
                >Automatically Assigned</label
              >
            </div>
            <div class="d-flex shift-type">
              <input
                id="manual-type-rd"
                type="radio"
                v-model="assignType"
                :value="false"
              /><label for="manual-type-rd" style="margin: 0 0 0 5px"
                >Manually Assigned</label
              >
            </div>
          </div>
          <div
            class="d-flex"
            :class="{
              'w-870': selectedShiftType.value < 3,
              'w-840': selectedShiftType.value == 4,
              'w-610': selectedShiftType.value == 3,
            }"
            :key="shiftBoxKey"
            style="justify-content: space-between"
          >
            <div v-for="index in selectedShiftType.value" :key="index">
              <div class="title__name" style="margin-bottom: 8px">
                {{ `Shift ${index}` }}
              </div>
              <shift-box
                :size="getShiftSize"
                :editable="isEnableEdit"
                :shift="selectedShiftData[`shift${index}`]"
                :shift-name="`shift${index}`"
                :get-hour-range="getHourRange1"
                @change-hour="handleChangeHour1"
                @reset-row="handleResetRow"
              ></shift-box>
            </div>
          </div>
          <div class="shift-config-modal__footer">
            <primary-button
              :title="resources['save_shift_config']"
              @on-active="saveConfig()"
              :custom-style="{ height: '30px' }"
              :default-class="
                'btn-custom ' +
                `${
                  !isEnalbeSubmit
                    ? 'inactive-btn'
                    : 'btn-custom-primary animationPrimary all-time-filters'
                }`
              "
              :active-class="'outline-animation'"
            ></primary-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  components: {
    "option-button-group": httpVueLoader(
      "/components/shift-config/option-button-group.vue"
    ),
    "hour-picker": httpVueLoader("/components/shift-config/hour-picker.vue"),
    "shift-box": httpVueLoader("/components/shift-config/shift-box.vue"),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
    "location-selection": httpVueLoader("/components/shift-config/location-selection.vue"),

  },
  created() {
    this.dayList = [
      "Default Shift",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday",
      "Sunday",
    ];
    this.shiftOptions = [
      { title: "1 Shift", value: 1 },
      { title: "2 Shifts", value: 2 },
      { title: "3 Shifts", value: 3 },
      { title: "4 Shifts", value: 4 },
    ];
  },
  data() {
    return {
      selectedDayOption: "Default Shift",
      dayStart: {
        hour: 0,
        minute: 0,
        isAddDay: false,
      },
      dayEnd: {
        hour: 0,
        minute: 0,
        isAddDay: false,
      },
      assignType: true,
      location: {},
      locations: [],
      shiftData: {
        dayShiftData: [
          {
            automatic: true,
            dayShiftType: "",
            end: "",
            hourShiftData: [{}],
            id: 1,
            locationId: "",
            numberOfShifts: 1,
            shift1: [],
            shift2: [],
            shift3: [],
            shift4: [],
            start: "",
          },
        ],
        locationCode: "",
        locationId: "",
        locationName: "",
      },
      isLoading: true,
      selectedShiftType: {},
      selectedShiftData: {},
      shiftBoxKey: 0,
    };
  },
  mounted() { },
  computed: {
    isEnalbeSubmit() {
      if (this.selectedShiftData.hourShiftData) {
        const check = this.selectedShiftData.hourShiftData.filter(item => (item.start == '' && item.end == '') || (item.start != '' && item.end != ''))
        console.log('check', check.length == 0)
        return check.length == 24
      }
      return false
    },
    isEnableEdit() {
      console.log('isEnableEdit', this.assignType, this.assignType == false)
      return this.assignType == false;
    },
    getShiftSize() {
      // return 6
      return 24 / +this.selectedShiftType.value;
    },
    currentShiftData() {
      return this.shiftData.dayShiftData[0];
    },
    getEndHourRangePicker() {
      let result = `${+this.dayStart.hour + 1 > 9 ? (+this.dayStart.hour + 1) : +this.dayStart.hour + 1 < 0 ? '00' : "0" + (+this.dayStart.hour + 1)}00`;
      console.log("getEndHourRangePicker", result);
      return result;
    },
    getStartHourRangePicker() {
      let result = '2400'
      if (!this.dayEnd.isAddDay) {
        result = `${+this.dayEnd.hour > 9 ? +this.dayEnd.hour : +this.dayEnd.hour < 0 ? '00' : '0' + +this.dayEnd.hour}00`
      }
      console.log("getEndHourRangePicker", result);
      return result;
    },

  },
  watch: {
    assignType: function (newValue) {
      console.log('change', this.assignType)
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      currentShiftData.automatic = this.assignType
      if (newValue) {
        this.resetShiftConfig(currentShiftData)
      }
    }
  },
  methods: {
    async changeShiftConfig() {
      let response = await this.initConfig();
      this.shiftData = response.data.data;
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      this.selectedShiftData = { ...currentShiftData };
      this.assignType = this.selectedShiftData.automatic
    },
    showModal: async function (locations) {
      console.log('list Locations', locations)
      this.locations = locations
      this.location = locations[0];
      $("#shift-config-modal").modal("show");
      await this.initData()
    },
    async handleChangeLocation(location) {
      this.location = location
      await this.initData()
    },
    async initData() {
      this.isLoading = true;
      this.selectedDayOption = "Default Shift"
      let response = await this.initConfig();
      this.shiftData = response.data.data;
      console.log('open modal', this.shiftData)
      this.selectedShiftData = { ...this.shiftData.dayShiftData[0] };
      this.selectedShiftType = {
        ...this.shiftOptions.filter(
          (item) => item.value == this.selectedShiftData.numberOfShifts
        )[0],
      };
      this.initHourPicker();
      console.log("show", this.selectedShiftData);
      this.assignType = this.selectedShiftData.automatic
      this.isLoading = false;
    },
    dimissModal: function () {
      $("#shift-config-modal").modal("hide");
    },
    async initConfig() {
      let locationData = { ...this.location };
      let localParam = {
        locationId: locationData.id,
      };
      const params = Common.param(localParam);
      const result = await axios.get(`/api/shift-config?${params}`);
      return result;
    },
    changeDays(option) {
      if (this.selectedDayOption != option) {
        this.selectedDayOption = option;
        let selectedDay =
          this.selectedDayOption == "Default Shift"
            ? "DEFAULT"
            : this.selectedDayOption.toUpperCase();
        let searchedShift = this.shiftData.dayShiftData.filter(
          (item) => item.dayShiftType == selectedDay
        );
        if (searchedShift.length == 0) {
          let currentMaxId = this.shiftData.dayShiftData.reduce(
            (acc, el) => (acc = acc > el.id ? acc : el.id),
            0
          );
          let newShift = {
            automatic: true,
            dayShiftType: selectedDay,
            end: "0000",
            hourShiftData: [],
            id: +currentMaxId + 1,
            locationId: this.location.id,
            numberOfShifts: 1,
            shift1: [],
            start: "0000",
          };
          for (let index = 0; index < 24; index++) {
            let newHourStart = index > 9 ? `${index}00` : `0${index}00`;
            let newHourEnd =
              index + 1 > 9 ? `${index + 1}00` : `0${index + 1}00`;
            newShift.hourShiftData[index] = {
              dayShiftId: 1,
              end: newHourEnd,
              shiftNumber: 4,
              start: newHourStart,
            };
            newShift.shift1[index] = {
              dayShiftId: 1,
              end: newHourEnd,
              shiftNumber: 4,
              start: newHourStart,
            };
          }

          this.shiftData.dayShiftData.push(newShift);
        }
        let currentShift = this.shiftData.dayShiftData.filter(
          (item) => item.dayShiftType == selectedDay
        )[0];
        this.selectedShiftData = { ...currentShift };
        this.selectedShiftType = {
          ...this.shiftOptions.filter(
            (item) => item.value == this.selectedShiftData.numberOfShifts
          )[0],
        };
      }
      this.initHourPicker()
      this.assignType = this.selectedShiftData.automatic
      console.log("select option", this.selectedDayOption);
    },
    handleChangeStartDate(time, type) {
      this.dayStart[type] = time;
      console.log("change time", this.dayStart, this.dayEnd);
    },
    handleChangeEndDate(time, type) {
      if (type == "hour") {
        if (time >= 24) {
          time -= 24;
          this.dayEnd.isAddDay = true;
        } else {
          this.dayEnd.isAddDay = false;
        }
      }

      this.dayEnd[type] = time;
    },
    handleSelectShift(shift) {
      if (!_.isEqual(this.selectedShiftType, shift)) {
        this.selectedShiftType = shift;
        let selectedDay =
          this.selectedDayOption == "Default Shift"
            ? "DEFAULT"
            : this.selectedDayOption.toUpperCase();
        let currentShiftData = this.shiftData.dayShiftData.filter(
          (item) => item.dayShiftType == selectedDay
        )[0];
        let allShift = [...currentShiftData.hourShiftData];
        currentShiftData.numberOfShifts = shift.value;
        for (let index = 1; index <= shift.value; index++) {
          currentShiftData[`shift${index}`] = allShift.splice(
            0,
            +this.getShiftSize
          );
          for (let j = 0; j < this.getShiftSize; j++) {
            console.log((index - 1) * this.getShiftSize + j, currentShiftData.hourShiftData[(index - 1) * this.getShiftSize + j])
            currentShiftData.hourShiftData[(index - 1) * this.getShiftSize + j].shiftNumber = index
          }
        }
        for (let index = 4; index > shift.value; index--) {
          delete currentShiftData[`shift${index}`];
        }
        let currentShift = this.shiftData.dayShiftData.filter(
          (item) => item.dayShiftType == selectedDay
        )[0];
        this.selectedShiftData = { ...currentShift };
      }
    },
    handleChangeHour1(shiftName, data, index, type) {
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      let hour = `${+data > 9 ? +data : '0' + +data}00`
      let indexOfTotal = this.getShiftSize * (+shiftName.replaceAll("shift", "") - 1) + index;
      currentShiftData[shiftName][index][type] = `${hour}`;
      currentShiftData.hourShiftData[indexOfTotal][type] = `${hour}`;
      this.selectedShiftData = { ...currentShiftData };
    },
    handleResetRow(shiftName, index) {
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      let hour = ''
      let indexOfTotal = this.getShiftSize * (+shiftName.replaceAll("shift", "") - 1) + index;
      currentShiftData[shiftName][index].start = `${hour}`;
      currentShiftData[shiftName][index].end = `${hour}`;
      currentShiftData.hourShiftData[indexOfTotal].start = `${hour}`;
      currentShiftData.hourShiftData[indexOfTotal].end = `${hour}`;
      this.selectedShiftData = { ...currentShiftData };
    },
    getHourRange1(shiftName, index, type) {
      //check in row
      let indexOfTotal = this.getShiftSize * (+shiftName.replaceAll("shift", "") - 1) + index;
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      let min = '0000'
      let max = '2300'
      let isAddDay = false
      const start = currentShiftData.hourShiftData[indexOfTotal].start
      const end = currentShiftData.hourShiftData[indexOfTotal].end
      if (type == 'start') {
        min = this.selectedShiftData.start
        if (end !== '') {
          max = +end.substr(0, 2) == 0 ? '2300' : `${+end.substr(0, 2) - 1 > 9 ? +end.substr(0, 2) - 1 : '0' + (+end.substr(0, 2) - 1)}00`
        } else {
          max = `${+this.selectedShiftData.end.substr(0, 2) - 1 > 9 ? +this.selectedShiftData.end.substr(0, 2) - 1 : +this.selectedShiftData.end.substr(0, 2) - 1 < 0 ? "23" : '0' + (+this.selectedShiftData.end.substr(0, 2) - 1)}00`
        }
      } else if (type == 'end') {
        max = this.selectedShiftData.end
        if (start !== '') {
          min = +start.substr(0, 2) == 23 ? '0000' : `${+start.substr(0, 2) + 1 > 9 ? +start.substr(0, 2) + 1 : '0' + (+start.substr(0, 2) + 1)}00`
        } else {
          min = `${+this.selectedShiftData.start.substr(0, 2) + 1 > 9 ? +this.selectedShiftData.start.substr(0, 2) + 1 : '0' + (+this.selectedShiftData.start.substr(0, 2) + 1)}00`
        }
        isAddDay = true
      }
      return { min: min, max: max, isAddDay: isAddDay }
    },
    getHourRange(shiftName, index) {
      let indexOfTotal = this.getShiftSize * (+shiftName.replaceAll("shift", "") - 1) + index;
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      let min = ''
      let max = ''
      if (indexOfTotal == 0) {
        min = this.selectedShiftData.start
      } else {
        for (let index = indexOfTotal - 1; index >= 0; index--) {
          if (currentShiftData.hourShiftData[index].end !== '') {
            min = `${currentShiftData.hourShiftData[index].end}`
            break;
          }
        }
      }
      if (indexOfTotal == 23) {
        max = this.selectedShiftData.end
      } else {
        for (let index = indexOfTotal + 1; index < 24; index++) {
          if (currentShiftData.hourShiftData[index].start !== '') {
            max = `${currentShiftData.hourShiftData[index].start}`
            break;
          }
        }
      }
      return {
        min: min,
        max: max
      }
    },
    handleChangeHour(shiftName, data, index) {
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];
      let startHour = `${data.start.hour == '' || data.start.minute == '' ? '' : data.start.hour}${data.start.hour == '' || data.start.minute == '' ? '' : data.start.minute}`;
      let endHour = `${data.end.hour == '' || data.end.minute == '' ? '' : data.end.hour}${data.end.hour == '' || data.end.minute == '' ? '' : data.end.minute}`;
      currentShiftData[shiftName][
        index
      ].start = `${startHour}`;
      currentShiftData[shiftName][
        index
      ].end = `${endHour}`;
      let indexOfTotal =
        this.getShiftSize * (+shiftName.replaceAll("shift", "") - 1) + index;
      console.log("handleChangeHour 1", indexOfTotal, this.shiftData);
      currentShiftData.hourShiftData[
        indexOfTotal
      ].start = `${data.start.hour}${data.start.minute}`;
      currentShiftData.hourShiftData[
        indexOfTotal
      ].end = `${data.end.hour}${data.end.minute}`;
      this.selectedShiftData = { ...currentShiftData };
    },
    changeShiftTime() {
      if (this.dayEnd.isAddDay && +this.dayStart.hour < +this.dayEnd.hour) {
        this.dayEnd.hour = this.dayStart.hour
      }
      let selectedDay =
        this.selectedDayOption == "Default Shift"
          ? "DEFAULT"
          : this.selectedDayOption.toUpperCase();
      let currentShiftData = this.shiftData.dayShiftData.filter(
        (item) => item.dayShiftType == selectedDay
      )[0];

      const start = +this.dayStart.hour
      let end = this.dayEnd.hour
      if (end >= 24) {
        end -= 24;
      }
      currentShiftData.start = `${start > 9 ? start : '0' + start}00`;
      currentShiftData.end = `${end > 9 ? end : '0' + end}00`;
      console.log("change shift time", this.dayStart, this.dayEnd, currentShiftData);
      this.resetShiftConfig(currentShiftData)
    },
    resetShiftConfig(currentShiftData) {
      const end = this.dayEnd.isAddDay ? +this.dayEnd.hour + 24 : +this.dayEnd.hour
      const start = +this.dayStart.hour
      const range = end - start
      const shiftSize = this.getShiftSize
      for (let index = 0; index < range; index++) {
        const baseStart = +start + index > 23 ? +start + index - 24 : +start + index
        const baseEnd = baseStart + 1 > 23 ? baseStart + 1 - 24 : baseStart + 1
        currentShiftData.hourShiftData[index].start = `${baseStart > 9 ? baseStart : '0' + (baseStart)}00`;
        currentShiftData.hourShiftData[index].end = `${baseEnd > 9 ? baseEnd : '0' + baseEnd}00`;
        const shiftOrder = Math.floor(index / shiftSize) + 1
        const shiftIndex = index % shiftSize
        currentShiftData[`shift${shiftOrder}`][shiftIndex].start = `${baseStart > 9 ? baseStart : '0' + baseStart}00`;
        currentShiftData[`shift${shiftOrder}`][shiftIndex].end = `${baseEnd > 9 ? baseEnd : '0' + baseEnd}00`;
      }
      for (let index = range; index < 24; index++) {
        currentShiftData.hourShiftData[index].start = ``;
        currentShiftData.hourShiftData[index].end = ``;
        const shiftOrder = Math.floor(index / shiftSize) + 1
        const shiftIndex = index % shiftSize
        currentShiftData[`shift${shiftOrder}`][shiftIndex].start = ``;
        currentShiftData[`shift${shiftOrder}`][shiftIndex].end = ``;
      }
      this.selectedShiftData = { ...currentShiftData };
      this.shiftBoxKey++;
    },
    initHourPicker() {
      this.dayStart.hour = this.selectedShiftData.start.substr(0, 2);
      // this.dayStart.minute = this.selectedShiftData.start.substr(2, 2);
      this.dayStart.minute = '00';
      this.dayEnd.hour = this.selectedShiftData.end.substr(0, 2);
      // this.dayEnd.minute = this.selectedShiftData.end.substr(2, 2);
      this.dayEnd.minute = '00';
      console.log('initHourPicker', this.selectedShiftData.start, this.selectedShiftData.end, this.dayStart, this.dayEnd)
      if (+this.dayStart.hour >= +this.dayEnd.hour) {
        this.dayEnd.isAddDay = true
      } else {
        this.dayEnd.isAddDay = false
      }
    },
    async saveConfig() {
      let data = { ...this.shiftData }
      data.dayShiftData = [{ ...this.selectedShiftData }]
      this.isLoading = true
      console.log('post data', data)
      const response = await axios.post(`/api/shift-config/save`, data)
      await this.changeShiftConfig()
      this.isLoading = false
      console.log('submit', response)
    },
  },
};
</script>

<style scoped>
.modal-title {
  background: rgb(245, 248, 255);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title-text {
  color: rgb(75, 75, 75);
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: unset;
  top: 0;
  left: 0;
  width: 100%;
  border-radius: 6px 6px 0 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title .close-button {
  font-size: 25px;
  display: flex;
  align-items: center;
  height: 17px;
  cursor: pointer;
}
/* MODAL BODY */
.modal-body {
  padding: 20px 52px;
}

.modal-body__title .form-item.reject-rate-checklist {
  margin: 15px 0;
}

.modal-body__title .form-item__label {
  font-size: 1rem;
  font-weight: bold;
  margin-right: 8px;
}
.modal-body__title .form-item__value {
  font-size: 1rem;
  font-weight: normal;
}
.modal-body__list {
  max-height: 320px;
  overflow: auto;
  position: relative;
  border: 1px solid #d0d0d0;
}
.modal-body__list table {
  width: 100%;
}

.btn-submit {
  padding: 6px 8px;
  border-radius: 3px;
  color: #fff;
  background-color: #3491ff;
  border: none;
}

.btn-submit:focus {
  outline-color: rgb(161 204 255 / 67%);
}

.modal-body__pagination {
  margin: 15px 0 36px;
  gap: 10px;
}

.modal-body__pagination button {
  background-color: #3491ff;
  color: #fff;
  border: none;
  border-radius: 3px;
}

.modal-body__pagination button:disabled {
  background-color: #c4c4c4;
  color: white;
}

.modal-body__action {
  margin-top: 60px;
}

.custom-ant-btn {
  display: inline-flex;
  justify-content: center;
  align-items: center;
}
.modal-body__header__infor {
  width: 60%;
  font-size: 16px;
}
.modal-body__title {
  margin-bottom: 10px;
  display: flex;
}
.title__name {
  font-weight: bold;
  width: 100px;
  white-space: nowrap;
}
.hour-picker-container .single-container {
  margin-right: 23px;
}
.shift-type {
  align-items: center;
  margin-right: 10px;
}
.shift-config-modal__footer {
  margin-top: 51px;
  display: flex;
  justify-content: flex-end;
}
.shift-config-loading-wave {
  height: 627px;
  width: 920px;
  animation: wave 4s infinite linear forwards;
  -webkit-animation: wave 4s infinite linear forwards;
  background: linear-gradient(to right, #fcfcfc 8%, #ededed 18%, #fcfcfc 33%);
  border: none;
}
.mg-r-164 {
  margin-right: 164px;
}
.mg-r-80 {
  margin-right: 80px;
}
.w-610 {
  width: 610px;
}
.w-840 {
  width: 840px;
}
.w-870 {
  width: 870px;
}
.inactive-btn {
  background-color: #c4c4c4;
  pointer-events: none;
  color: #fff;
}
</style>