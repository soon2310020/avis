<template>
  <div class="date-block">
    <div class="weekly block">
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
            disable: currentYear == getMaxYear,
          }"
          @click="currentYear++"
          >&#10095;</span
        >
      </div>

      <div v-if="currentYear > getMaxYear" class="wrapper">
        <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
          <span class="week-number disable">{{ i }}</span>
        </div>
      </div>
      <div v-else-if="currentYear == getMaxYear" class="wrapper">
        <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
          <span v-if="i > getCurrentWeek" class="week-number disable">{{
            i
          }}</span>
          <span
            v-else-if="i == getCurrentWeek"
            class="week-number today"
            :class="{
              'selected-week':
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              selected:
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(i),
              'selected-second': isSelectedSecond(i),
              'in-range': checkInRange(i),
            }"
            @click="handleSelectWeek(i, currentYear)"
            >{{ i }}</span
          >
          <span
            v-else
            class="week-number"
            :class="{
              'selected-week':
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              selected:
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(i),
              'selected-second': isSelectedSecond(i),
              'in-range': checkInRange(i),
            }"
            @click="handleSelectWeek(i, currentYear)"
            >{{ i }}</span
          >
        </div>
      </div>
      <div v-else class="wrapper">
        <div v-for="i in getNumberOfWeek()" :key="i" class="week-item">
          <span
            class="week-number"
            :class="{
              'selected-week':
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              selected:
                (i == selectedWeek && currentYear == selectedYear) ||
                (i == selectedSecondWeek && currentYear == selectedSecondYear),
              'selected-first': isSelectedFirst(i),
              'selected-second': isSelectedSecond(i),
              'in-range': checkInRange(i),
            }"
            @click="handleSelectWeek(i, currentYear)"
            >{{ i }}</span
          >
        </div>
      </div>
      <div v-if="showFooter" class="block-footer">
        <span>*Click on the number in the calendar to choose the week</span>
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
    showFooter: {
      type: Boolean,
      default: () => true,
    },
    enableNull: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      currentYear: moment(new Date()).format("YYYY"),
      selectedWeek: null,
      selectedYear: null,
      selectedSecondWeek: null,
      selectedSecondYear: null,
      pickStep: 0,
    };
  },
  created() {
    this.today = new Date();
    this.getCurrentYear({ from: this.today });
  },
  mounted() {
    // this.getCurrentYear(this.currentDate);
  },
  computed: {
    getCurrentWeek() {
      let weekNum = this.getCurrentWeekNumber(this.today);
      console.log("getCurrentWeek", weekNum, this.today);
      return weekNum;
    },
    getMaxYear() {
      if (this.dateRange) {
        return this.dateRange.maxDate.getFullYear();
      }
      console.log("this.dateRange", this.dateRange);
      return new Date().getFullYear();
    },
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
        let yearFrom = date.fromTitle.split("-")[0];
        let yearTo = date.toTitle.split("-")[0];
        let weekFrom = date.fromTitle.split("-")[1];
        let weekTo = date.toTitle.split("-")[1];
        this.selectedWeek = +weekFrom;
        this.selectedYear = +yearFrom;
        if (this.isRange) {
          this.selectedSecondYear = +yearTo;
          this.selectedSecondWeek = +weekTo;
          this.pickStep = 2;
        }
      }
    },
    isSelectedFirst(index) {
      if (
        this.selectedWeek == this.selectedSecondWeek &&
        this.selectedYear == this.selectedSecondYear
      ) {
        return false;
      } else {
        return (
          index === this.selectedWeek &&
          this.currentYear == this.selectedYear &&
          this.selectedSecondWeek
        );
      }
    },
    isSelectedSecond(index) {
      if (
        this.selectedWeek == this.selectedSecondWeek &&
        this.selectedYear == this.selectedSecondYear
      ) {
        return false;
      } else {
        return (
          index === this.selectedSecondWeek &&
          this.currentYear == this.selectedSecondYear &&
          this.selectedWeek
        );
      }
    },
    checkInRange(index) {
      if (this.isRange) {
        if (this.selectedWeek != null && this.selectedSecondWeek != null) {
          if (
            this.currentYear < this.selectedYear ||
            this.currentYear > this.selectedSecondYear
          ) {
            return false;
          } else {
            if (this.selectedYear == this.selectedSecondYear) {
              if (
                index > this.selectedWeek &&
                index < this.selectedSecondWeek
              ) {
                return true;
              } else {
                return false;
              }
            } else {
              if (this.currentYear == this.selectedYear) {
                if (index > this.selectedWeek) {
                  return true;
                } else {
                  return false;
                }
              } else if (this.currentYear == this.selectedSecondYear) {
                if (index < this.selectedSecondWeek) {
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
    changeDate() {
      let frequency = this.getFrequency();
      let selectRange = this.getDateRange();
      let title = this.getTitle();
      // let title = {
      //   start: selectRange.start ? moment(selectRange.start).format("YYYY-ww") : '',
      //   end: selectRange.end ? moment(selectRange.end).format("YYYY-ww") : '',
      // };
      this.$emit("change-date", selectRange, title);
      this.$forceUpdate();
    },
    getFrequency() {
      return this.isRange ? "WEEKLY_RANGE" : "WEEKLY";
    },
    getCurrentWeekNumber(date) {
      /* let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));

      if (oneJan.getDay() < 4) {
        numberOfDays -= oneJan.getDay();
      } else {
        if (numberOfDays < 7 - oneJan.getDay()) {
          oneJan = new Date(date.getFullYear() - 1, 0, 1);
          numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
        } else {
          numberOfDays += oneJan.getDay();
        }
      }
      let result = Math.floor((numberOfDays + 1) / 7);
      return result; */

      // Get the year
      const year = date.getFullYear();

      // Get the day of the week for January 1st
      const january1st = new Date(year, 0, 1).getDay();

      // Determine the day to start counting the first week
      let startDay = 1; // By default, assume the first week starts on January 1st

      if (january1st >= 4) {
        // If January 1st is on a Wednesday, Thursday, Friday, or Saturday,
        // the first week starts on the previous year
        startDay = -((7 - january1st) % 7) + 2;
      }

      // Get the day of the year (1-366)
      const dayOfYear =
        Math.ceil((date - new Date(year, 0, startDay)) / 86400000) + 1;

      // Calculate the week number
      const weekNumber = Math.floor((dayOfYear + 6) / 7);

      return weekNumber;
    },
    getNumberOfWeek() {
      let result = this.getCurrentWeekNumber(
        moment("12-31-" + this.currentYear, "MM-DD-YYYY").toDate()
      );
      if (
        moment("01-01-" + this.currentYear, "MM-DD-YYYY")
          .toDate()
          .getDay() >= 4
      ) {
        result--;
      }
      if (
        moment("12-31-" + this.currentYear, "MM-DD-YYYY")
          .toDate()
          .getDay() < 4
      ) {
        result--;
      }
      return result;
    },
    getCurrentYear(currentDate) {
      const from = currentDate.from || this.today;
      let year = from.getFullYear();
      let oneJan = new Date(from.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((from - oneJan) / (24 * 60 * 60 * 1000));
      let result = isNaN(year) ? today.getFullYear() : year;
      if (oneJan.getDay() >= 4 && numberOfDays < 7 - oneJan.getDay()) {
        result--;
      }
      this.currentYear = result;
    },
    getDateRange() {
      let startOfWeek = null;
      let endOfWeek = null;
      let title = this.getTitle();
      if (this.isRange) {
        if (this.selectedWeek) {
          const { start } = Common.getWeekStartEndDates(
            this.selectedYear,
            this.selectedWeek
          );
          startOfWeek = start;
          // let startDate = null;
          // let firstDayOfYear = new Date(this.selectedYear, 0, 1).getDay();
          // if (firstDayOfYear < 4) {
          //   startDate = new Date(
          //     this.selectedYear,
          //     0,
          //     (this.selectedWeek - 1) * 7
          //   );
          //   startOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     (this.selectedWeek - 1) * 7 - startDate.getDay()
          //   );
          // } else {
          //   startDate = new Date(this.selectedYear, 0, this.selectedWeek * 7);
          //   startOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     this.selectedWeek * 7 - startDate.getDay()
          //   );
          // }
        }
        if (this.selectedSecondWeek) {
          const { end } = Common.getWeekStartEndDates(
            this.selectedSecondYear,
            this.selectedSecondWeek
          );
          endOfWeek = end;
          // let startDate = null;
          // let firstDayOfYear = new Date(this.selectedSecondYear, 0, 1).getDay();
          // if (firstDayOfYear < 4) {
          //   startDate = new Date(
          //     this.selectedSecondYear,
          //     0,
          //     (this.selectedSecondWeek - 1) * 7
          //   );
          //   endOfWeek = new Date(
          //     this.selectedSecondYear,
          //     0,
          //     this.selectedSecondWeek * 7 - startDate.getDay() - 1
          //   );
          // } else {
          //   startDate = new Date(
          //     this.selectedSecondYear,
          //     0,
          //     this.selectedSecondWeek * 7
          //   );
          //   endOfWeek = new Date(
          //     this.selectedSecondYear,
          //     0,
          //     (this.selectedSecondWeek + 1) * 7 - startDate.getDay() - 1
          //   );
          // }
        }
      } else {
        if (this.selectedWeek) {
          const { start, end } = Common.getWeekStartEndDates(
            this.selectedYear,
            this.selectedWeek
          );
          startOfWeek = start;
          endOfWeek = end;
          // let startDate = null;
          // let firstDayOfYear = new Date(this.selectedYear, 0, 1).getDay();
          // if (firstDayOfYear < 4) {
          //   startDate = new Date(
          //     this.selectedYear,
          //     0,
          //     (this.selectedWeek - 1) * 7
          //   );
          //   startOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     (this.selectedWeek - 1) * 7 - startDate.getDay()
          //   );
          //   endOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     this.selectedWeek * 7 - startDate.getDay() - 1
          //   );
          // } else {
          //   startDate = new Date(this.selectedYear, 0, this.selectedWeek * 7);
          //   startOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     this.selectedWeek * 7 - startDate.getDay()
          //   );
          //   endOfWeek = new Date(
          //     this.selectedYear,
          //     0,
          //     (this.selectedWeek + 1) * 7 - startDate.getDay() - 1
          //   );
          // }
        }
      }
      return {
        start: startOfWeek,
        end: endOfWeek,
      };
    },
    getTitle() {
      let start = "";
      let end = "";
      if (this.isRange) {
        if (this.selectedWeek) {
          start = `${this.selectedYear}-${
            this.selectedWeek > 9 ? this.selectedWeek : "0" + this.selectedWeek
          }`;
        }
        if (this.selectedSecondWeek) {
          end = `${this.selectedSecondYear}-${
            this.selectedSecondWeek > 9
              ? this.selectedSecondWeek
              : "0" + this.selectedSecondWeek
          }`;
        }
      } else {
        if (this.selectedWeek) {
          start = `${this.selectedYear}-${
            this.selectedWeek > 9 ? this.selectedWeek : "0" + this.selectedWeek
          }`;
          end = `${this.selectedYear}-${
            this.selectedWeek > 9 ? this.selectedWeek : "0" + this.selectedWeek
          }`;
        }
      }
      return {
        start: start,
        end: end,
      };
    },
    clearPickingData() {
      this.selectedWeek = null;
      this.selectedYear = this.currentYear;
      this.selectedSecondWeek = null;
      this.selectedSecondYear = null;
      this.pickStep = 0;
      this.$emit("clear-data");
    },
    handleSelectWeek(weekNumber, currentYear) {
      if (!this.isRange) {
        if (weekNumber != this.selectedWeek) {
          this.selectedWeek = weekNumber;
          this.selectedYear = currentYear;
        } else {
          if (this.enableNull) {
            this.selectedYear = currentYear;
            this.selectedWeek = null;
          }
        }
      } else {
        if (this.pickStep == 2) {
          this.clearPickingData();
        } else if (this.pickStep == 0) {
          if (weekNumber != this.selectedWeek) {
            this.selectedYear = currentYear;
            this.selectedWeek = weekNumber;
            this.pickStep++;
          } else {
            this.clearPickingData();
          }
        } else if (this.pickStep == 1) {
          if (weekNumber != this.selectedWeek) {
            if (
              currentYear < this.selectedYear ||
              (currentYear == this.selectedYear &&
                weekNumber < this.selectedWeek)
            ) {
              this.selectedSecondWeek = this.selectedWeek;
              this.selectedSecondYear = this.selectedYear;
              this.selectedYear = currentYear;
              this.selectedWeek = weekNumber;
            } else {
              this.selectedSecondYear = currentYear;
              this.selectedSecondWeek = weekNumber;
            }
            this.pickStep++;
          } else {
            this.clearPickingData();
          }
        }
      }
      this.changeDate();
    },
  },
};
</script>

<style scoped>
.wrapper {
  font-family: Helvetica Neue, Regular;
  display: grid;
  position: relative;
  padding: 5px;
  grid-template-columns: repeat(9, 1fr);
  grid-template-rows: repeat(6, 30px);
  font-size: 12px;
  color: #4b4b4b;
}
.week-item {
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: 50% 50%;
  height: 32px;
  cursor: pointer;
}
.selected {
  color: #fff !important;
  background-color: #519ffc;
}
.selected:hover {
  background-color: #519ffc !important;
}

.week-number {
  width: 17px;
  height: 17px;
  text-align: center;
  line-height: 17px;
}
.selected-week {
  border-radius: 17px;
}

.disable {
  color: #d5d5d5;
  pointer-events: none;
}
.header {
  display: flex;
  justify-content: space-between;
  height: 20px;
  align-items: center;
  padding: 0px 7px 0px 7px;
}

.block {
  margin-right: 50px;
  min-width: 222px;
  position: relative;
}
.block-footer {
  font-size: 11px;
  color: #838383;
  position: unset;
}
.week-item {
  position: relative;
}
.week-number {
  z-index: 2;
}
.week-number.in-range::after {
  content: "";
  width: 100%;
  background: #519ffc66;
  height: 12px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
.week-number.selected-first::before,
.week-number.selected-second::before {
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
.week-number.selected-second::before {
  right: unset !important;
  left: 0 !important;
}
</style>
