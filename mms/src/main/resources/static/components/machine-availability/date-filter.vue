<template>
  <div
    id="date-picker-button"
    v-click-outside="close"
    @mousemove="handleMouseOver"
    @mouseleave="handleMouseLeave"
    @click="show"
    :class="{ selected: isClicked }"
    class="date-filter-top-modal"
  >
    <a-date-picker
      format="YYYY-MM-DD"
      @focus="focusCalendar"
      v-model="selectedDate"
      :default-value="defaultDate"
      :default-picker-value="defaultDate"
      :allow-clear="false"
    >
      <svg
        v-if="!isClicked && !isHover"
        slot="suffixIcon"
        style="left: unset"
        xmlns="http://www.w3.org/2000/svg"
        width="10.414"
        height="5.707"
        viewBox="0 0 10.414 5.707"
      >
        <path
          id="Icon_feather-chevron-down"
          data-name="Icon feather-chevron-down"
          d="M9,13.5,13.5,18,18,13.5"
          transform="translate(-8.293 -12.793)"
          fill="none"
          stroke="#3491fb"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1"
        />
      </svg>

      <svg
        v-if="!isClicked && isHover"
        slot="suffixIcon"
        style="left: unset"
        xmlns="http://www.w3.org/2000/svg"
        width="10.414"
        height="5.707"
        viewBox="0 0 10.414 5.707"
      >
        <path
          id="Icon_feather-chevron-down"
          data-name="Icon feather-chevron-down"
          d="M9,13.5,13.5,18,18,13.5"
          transform="translate(-8.293 -12.793)"
          fill="none"
          stroke="#0e65c7"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1"
        />
      </svg>

      <svg
        v-if="isClicked"
        slot="suffixIcon"
        style="left: unset"
        xmlns="http://www.w3.org/2000/svg"
        width="10.414"
        height="5.707"
        viewBox="0 0 10.414 5.707"
      >
        <path
          id="Icon_feather-chevron-down"
          data-name="Icon feather-chevron-down"
          d="M9,13.5,13.5,18,18,13.5"
          transform="translate(18.707 18.5) rotate(180)"
          fill="none"
          stroke="#0e65c7"
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="1"
        />
      </svg>
    </a-date-picker>
  </div>
</template>

<script>
module.exports = {
  name: "date-filter",
  props: {
    defaultDate: Object,
  },
  data() {
    return {
      selectedDate: null,
      isHover: false,
      isClicked: false,
    };
  },
  mounted() {
    this.selectedDate = this.defaultDate;
  },
  computed: {},
  watch: {
    selectedDate(value) {
      console.log(value);
      if (value) {
        this.$emit("change-date", moment(value).format("YYYYMMDD"), value);
      }
    },
    defaultDate(value){
      this.selectedDate = value;
    }
  },
  methods: {
    focusCalendar() {
      $(".tool-wrapper").css("box-shadow", "0 0 0 2px rgba(24,144,255,.2)");
      setTimeout(() => {
        $(".tool-wrapper").css("box-shadow", "none");
      }, 100);
    },
    disabledDate(current) {
      return current && current > moment().endOf("day");
    },
    handleMouseOver() {
      console.log("hover");
      this.isHover = true;
    },
    handleMouseLeave() {
      console.log("out hover");
      this.isHover = false;
    },
    show() {
      const rootEl = document.getElementById("date-picker-button");
      if (rootEl) {
        const elDom = rootEl.querySelectorAll(".ant-calendar-picker-input");
        if (elDom.length > 0) {
          const el = elDom[0];
          el.classList.add("datepicker-button-animation");
          setTimeout(() => {
            el.classList.remove("datepicker-button-animation");
            this.isClicked = true;
          }, 700);
        }
      }
    },
    close() {
      this.isClicked = false;
    },
  },
};
</script>

<style scoped>
.ant-input:focus {
  outline: 0;
  /*box-shadow: 0 0 0 2px rgba(24,144,255,.2);*/
  box-shadow: none;
}
.date-filter-top-modal .ant-input {
  padding: 6px 8px;
  background-color: #fff;
  color: #3491ff;
  border-radius: 3px;
  border: 1px solid #3491fb;
  width: 125px;
  text-decoration: none !important;
}
.date-filter-top-modal .ant-input:hover {
  background-color: #deedff;
  color: #0e65c7;
  border: 1px solid transparent;
  text-decoration: none !important;
}
.selected .ant-input {
  color: #0e65c7 !important;
}
.datepicker-button-animation {
  animation: footer-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: 3px solid transparent;
  background-color: #fff !important;
  border: 1px solid transparent;
}
@keyframes footer-primary-animation {
  0% {
  }
  33% {
    outline: 3px solid #89d1fd;
  }
  66% {
    outline: 3px solid #89d1fd;
  }
  100% {
  }
}
</style>