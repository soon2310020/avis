<template>
  <div class="date-block">
    <div class="yearly block">
      <div class="header">
        <span
          class="link-button"
          :class="{ disable: yearRange == 0 }"
          @click="prevYearRange()"
          >&#10094;</span
        >
        <span class="title">{{ yearRange }}-{{ yearRange + 11 }}</span>
        <span
          class="link-button"
          :class="{
            disable:
              yearRange <= today.getFullYear() &&
              yearRange + 11 >= today.getFullYear(),
          }"
          @click="nextYearRange()"
          >&#10095;</span
        >
      </div>
      <div v-if="today.getFullYear() < yearRange" class="wrapper-year">
        <div v-for="index in 12" :key="index" class="year-item">
          <span class="disable year-text">{{ yearRange + index - 1 }}</span>
        </div>
      </div>
      <div
        v-else-if="
          today.getFullYear() <= yearRange + 11 &&
          today.getFullYear() >= yearRange
        "
        class="wrapper-year"
      >
        <div v-for="index in 12" :key="index" class="year-item">
          <span
            v-if="yearRange + index - 1 > today.getFullYear()"
            class="disable year-text"
            >{{ yearRange + index - 1 }}</span
          >
          <span
            v-else-if="yearRange + index - 1 == today.getFullYear()"
            class="today year-text"
            :class="{
              'selected-year':
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
              selected:
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
            }"
            @click="handleSelectYear(yearRange + index - 1)"
            >{{ yearRange + index - 1 }}</span
          >
          <span
            v-else
            class="year-text"
            @click="handleSelectYear(yearRange + index - 1)"
            :class="{
              'selected-year':
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
              selected:
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
            }"
            >{{ yearRange + index - 1 }}</span
          >
        </div>
      </div>
      <div v-else class="wrapper-year">
        <div v-for="index in 12" :key="index" class="year-item">
          <span
            class="year-text"
            @click="handleSelectYear(yearRange + index - 1)"
            :class="{
              'selected-year':
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
              selected:
                yearRange + index - 1 == selectedYear ||
                yearRange + index - 1 == selectedSecondYear,
            }"
            >{{ yearRange + index - 1 }}</span
          >
        </div>
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
      date: {
        start: null,
        end: null,
      },
      dateTitle: {
        start: "",
        end: "",
      },
      yearRange: 2016,
      selectedYear: null,
      selectedSecondYear: null,
      pickStep: 0,
    };
  },
  created() {
    this.today = new Date();
  },
  watch: {
    frequency(newValue) {
       if (newValue.includes("YEAR")) {
        console.log('%c YEAR is selected', 'background : #FF8C2C, color : #ffffff')
        this.clearPickingData();
      }
    },
  },
  methods: {
    getFrequency() {
      return this.isRange ? "YEARLY_RANGE" : "YEARLY";
    },
    changeDate() {
      let frequency = this.getFrequency();
      let selectRange = this.getDateRange();
      // let title = {
      //   start: selectRange.start
      //     ? moment(selectRange.start).format("YYYY")
      //     : "",
      //   end: selectRange.end ? moment(selectRange.end).format("YYYY") : "",
      // };
       let title =  {
        start: selectRange.start
          ? moment(selectRange.start).format("YYYY-MM-DD")
          : "",
        end: selectRange.end
          ? moment(selectRange.end).format("YYYY-MM-DD")
          : "",
      };
      this.$emit("change-date", selectRange, title);
    },
    prevYearRange() {
      if (this.yearRange <= 18) {
        this.yearRange = 1;
      } else {
        this.yearRange -= 12;
      }
    },
    nextYearRange() {
      if (this.yearRange == 1) {
        this.yearRange = 18;
      } else {
        this.yearRange += 12;
      }
    },
    handleSelectYear(year) {
      if (!this.isRange) {
        if (this.selectedYear != year) {
          this.selectedYear = year;
          console.log(`Selected year: ${this.selectedYear}`);
        } else {
          this.selectedYear = null;
          console.log("Deselected year");
        }
      } else {
        if (this.pickStep == 2) {
          this.clearPickingData();
        } else if (this.pickStep == 0) {
          if (year != this.selectedYear) {
            this.selectedYear = year;
            this.pickStep++;
          } else {
            this.clearPickingData();
            console.log("deselect year!");
          }
        } else if (this.pickStep == 1) {
          if (year != this.selectedYear) {
            if (year < this.selectedYear) {
              this.selectedSecondYear = this.selectedYear;
              this.selectedYear = year;
            } else {
              this.selectedSecondYear = year;
              console.log(`Selected year Change to ${this.selectedSecondYear}`);
            }
            this.pickStep++;
          } else {
            this.clearPickingData();
            console.log("deselect year!");
          }
        }
      }
      this.changeDate();
    },
    getDateRange() {
      let start = null;
      let end = null;
      if (!this.isRange) {
        if (this.selectedYear) {
          start = new Date(this.selectedYear, 0, 1);
          end = new Date(this.selectedYear, 11, 31);
        }
      } else {
        if (this.selectedYear) {
          start = new Date(this.selectedYear, 0, 1);
        }
        if (this.selectedSecondYear) {
          end = new Date(this.selectedSecondYear, 0, 1);
        }
      }
      return {
        start: start,
        end: end,
      };
    },
    clearPickingData() {
      this.selectedYear = new Date().getFullYear();
      this.changeDate()
      this.selectedSecondYear = null;
      this.pickStep = 0;
    },
  },
};
</script>
<style scoped>
.block{
  margin-right: 0px !important;
  min-width: 250px !important;
}
.block-footer {
  font-size: 11px;
  color: #838383;
  position: unset;
}


.wrapper-year {
    display: grid;
    grid-template-columns: repeat(3, 1fr) !important;

}
</style>