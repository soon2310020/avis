<template>
  <a
    id="reject-rate-date-picker"
    ref="btn"
    @mouseover="isHoverBtn = true"
    @mouseleave="isHoverBtn = false"
    href="javascript:void(0)"
    @click="showAnimation()"
    class="dropdown_button-custom"
  >
    <img v-if="isHoverBtn" src="/images/icon/date-picker-before-hover.svg" />
    <img v-else src="/images/icon/date-picker-before.svg" />
    <span class="truncate">{{ getTitle }}</span>
  </a>
</template>

<script>
module.exports = {
  name: "date-picker-button",
  props: {
    date: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      isHoverBtn: false,
      months: [
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
      ],
    };
  },
  computed: {
    getTitle() {
      if (this.date) {
        switch (this.date.rangeType) {
          case "DAILY":
            return this.date.from;
          case "WEEKLY":
            return `Week ${
              this.date.selectedItem.selectedWeek < 10
                ? "0" + this.date.selectedItem.selectedWeek
                : this.date.selectedItem.selectedWeek
            }, ${this.date.time.from.getFullYear()}`;
          case "MONTHLY":
            let year = this.date.from.split("-")[0];
            return `${
              this.months[this.date.selectedItem.selectedMonth]
            }, ${year}`;
        }
      } else {
        return "YYYY-MM-DD";
      }
    },
  },
  methods: {
    showAnimation() {
      console.log(this.date);
      const el = this.$refs["btn"];
      el.classList.add("dropdown_button-animation");
      setTimeout(() => {
        this.$emit("click");
        setTimeout(() => {
          el.classList.remove("dropdown_button-animation");
        }, 50);
      }, 650);
    },
  },
};
</script>