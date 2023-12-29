<template>
  <div class="date-block">
    <div class="monthly block">
      <div class="header">
        <span
          class="link-button"
          :class="{ disable: currentYear == 0 }"
          @click="currentYear--"
          >&#10094;</span
        >
        <span class="title">{{ currentYear }}</span>
        <span
          class="link-button"
          :class="{
            disable: currentYear == today.getFullYear(),
          }"
          @click="currentYear++"
          >&#10095;</span
        >
      </div>
      <div v-if="currentYear > today.getFullYear()" class="wrapper-month">
        <div v-for="(month, index) in months" :key="index" class="month-item">
          <span class="disable month-text">{{ month }}</span>
        </div>
      </div>
      <div v-else-if="currentYear == today.getFullYear()" class="wrapper-month">
        <div v-for="(month, index) in months" :key="index" class="month-item">
          <span v-if="index > today.getMonth()" class="disable month-text">{{
            month
          }}</span>
          <span
            v-else-if="index == today.getMonth()"
            class="today month-text"
            @click="handleSelectMonth(index, currentYear)"
            :class="{
              'selected-month':
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              selected:
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(index),
              'selected-second': isSelectedSecond(index),
              'in-range': checkInRange(index),
            }"
            >{{ month }}</span
          >
          <span
            v-else
            class="month-text"
            @click="handleSelectMonth(index, currentYear)"
            :class="{
              'selected-month':
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              selected:
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(index),
              'selected-second': isSelectedSecond(index),
              'in-range': checkInRange(index),
            }"
            >{{ month }}</span
          >
        </div>
      </div>
      <div v-else class="wrapper-month">
        <div v-for="(month, index) in months" :key="index" class="month-item">
          <span
            class="month-text"
            @click="handleSelectMonth(index, currentYear)"
            :class="{
              'selected-month':
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              selected:
                (index == selectedMonth && currentYear == selectedYear) ||
                (index == selectedSecondMonth &&
                  currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(index),
              'selected-second': isSelectedSecond(index),
              'in-range': checkInRange(index),
            }"
            >{{ month }}</span
          >
        </div>
      </div>
      <div class="block-footer">
        <span>*Click on the month in the calendar to choose the month</span>
      </div>
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
      currentYear: null,
      selectedMonth: null,
      selectedYear: null,
      selectedSecondMonth: null,
      selectedSecondYear: null,
      pickStep: 0,
    };
  },
  created() {
    this.today = new Date();
    this.getCurrentYear(this.today);
    this.months = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
    ];
  },
  mounted() {
    this.getCurrentYear(this.currentDate);
  },
  watch: {
    currentDate(newValue) {
      this.getCurrentYear(newValue);
    },
    frequency(newValue) {
      this.clearPickingData();
    },
  },
  methods: {
    setDefault(date) {
      if (date) {
        console.log("setDefault month", date);
        let yearFrom = date.fromTitle.split("-")[0];
        let yearTo = date.toTitle.split("-")[0];
        let monthFrom = date.fromTitle.split("-")[1];
        let monthTo = date.toTitle.split("-")[1];
        if (this.isRange) {
          this.selectedMonth = +monthFrom - 1;
          this.selectedSecondMonth = +monthTo - 1;
          this.selectedYear = +yearFrom;
          this.selectedSecondYear = +yearTo;
          this.pickStep = 2;
        } else {
          this.selectedMonth = +monthFrom - 1;
          this.selectedYear = +yearFrom;
        }
        console.log("setDefault month 2", date);
      }
    },
    isSelectedFirst(index) {
      if (
        this.selectedMonth == this.selectedSecondMonth &&
        this.selectedYear == this.selectedSecondYear
      ) {
        return false;
      } else {
        return (
          index == this.selectedMonth &&
          this.currentYear == this.selectedYear &&
          this.selectedSecondMonth !== null
        );
      }
    },
    isSelectedSecond(index) {
      if (
        this.selectedMonth == this.selectedSecondMonth &&
        this.selectedYear == this.selectedSecondYear
      ) {
        return false;
      } else {
        return (
          index == this.selectedSecondMonth &&
          this.currentYear == this.selectedSecondYear &&
          this.selectedMonth !== null
        );
      }
    },
    checkInRange(index) {
      if (this.isRange) {
        if (this.selectedMonth != null && this.selectedSecondMonth != null) {
          if (
            this.currentYear < this.selectedYear ||
            this.currentYear > this.selectedSecondYear
          ) {
            return false;
          } else {
            if (this.selectedYear == this.selectedSecondYear) {
              if (
                index > this.selectedMonth &&
                index < this.selectedSecondMonth
              ) {
                return true;
              } else {
                return false;
              }
            } else {
              if (this.currentYear == this.selectedYear) {
                if (index > this.selectedMonth) {
                  return true;
                } else {
                  return false;
                }
              } else if (this.currentYear == this.selectedSecondYear) {
                if (index < this.selectedSecondMonth) {
                  return true;
                } else {
                  return false;
                }
              } else {
                return true;
              }
            }
          }
        }
      }
    },
    getFrequency() {
      return this.isRange ? "MONTHLY_RANGE" : "MONTHLY";
    },
    changeDate() {
      let frequency = this.getFrequency();
      let selectRange = this.getDateRange();
      let title = {
        start: selectRange.start
          ? moment(selectRange.start).format("YYYY-MM")
          : "",
        end: selectRange.end ? moment(selectRange.end).format("YYYY-MM") : "",
      };
      this.$emit("change-date", selectRange, title);
    },
    getCurrentYear(currentDate) {
      let year = moment(currentDate.from).format("YYYY");
      this.currentYear = isNaN(year) ? moment(new Date()).format("YYYY") : year;
    },
    clearPickingData() {
      this.selectedMonth = null;
      this.selectedYear = this.currentYear;
      this.selectedSecondMonth = null;
      this.selectedSecondYear = null;
      this.pickStep = 0;
    },
    handleSelectMonth(month, currentYear) {
      if (this.isRange) {
        if (this.pickStep == 2) {
          this.clearPickingData();
        } else if (this.pickStep == 0) {
          if (month != this.selectedMonth) {
            this.selectedYear = currentYear;
            this.selectedMonth = month;
            this.pickStep++;
          } else {
            this.clearPickingData();
          }
        } else if (this.pickStep == 1) {
          if (month != this.selectedMonth) {
            if (
              currentYear < this.selectedYear ||
              (currentYear == this.selectedYear && month < this.selectedMonth)
            ) {
              this.selectedSecondMonth = this.selectedMonth;
              this.selectedSecondYear = this.selectedYear;
              this.selectedYear = currentYear;
              this.selectedMonth = month;
            } else {
              this.selectedSecondYear = currentYear;
              this.selectedSecondMonth = month;
            }
            this.pickStep++;
          } else {
            this.clearPickingData();
          }
        }
      } else {
        if (month == this.selectedMonth && currentYear == this.selectedYear) {
          this.selectedMonth = null;
          this.selectedYear = null;
        } else {
          this.selectedMonth = month;
          this.selectedYear = currentYear;
          let selectRange = this.getDateRange();
          console.log(`From ${selectRange.start} - To ${selectRange.end}`);
        }
      }
      this.changeDate();
    },
    getDateRange() {
      let start = null;
      let end = null;
      if (this.isRange) {
        if (this.selectedMonth != null) {
          start = new Date(this.selectedYear, this.selectedMonth, 1);
        }
        if (this.selectedSecondMonth != null) {
          end = new Date(
            this.selectedSecondYear,
            +this.selectedSecondMonth + 1,
            0
          );
        }
      } else {
        if (this.selectedMonth != null) {
          start = new Date(this.selectedYear, this.selectedMonth, 1);
          end = new Date(this.selectedYear, +this.selectedMonth + 1, 0);
        }
      }
      return {
        start: start,
        end: end,
      };
    },
  },
};
</script>
<style scoped>
.block-footer {
  font-size: 11px;
  color: #838383;
  position: unset;
}
.month-item {
  position: relative;
}
.month-text {
  z-index: 2;
}
.month-text.in-range::after {
  content: "";
  width: 100%;
  background: #519ffc66;
  height: 12px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
.month-text.selected-first::before,
.month-text.selected-second::before {
  content: "";
  width: 50%;
  background: #519ffc66;
  height: 12px;
  position: absolute;
  right: 0;
  top: 50%;
  transform: translate(0, -50%);
  z-index: -1;
}
.month-text.selected-second::before {
  right: unset !important;
  left: 0 !important;
}
</style>