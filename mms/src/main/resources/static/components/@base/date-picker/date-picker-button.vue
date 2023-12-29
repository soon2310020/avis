<template>
  <div>
    <!-- <a
    :ref="'btn' + parent"
    @mouseover="isHoverBtn = true"
    @mouseleave="isHoverBtn = false"
    href="javascript:void(0)"
    @click="showAnimation()"
    class="dropdown_button-custom"
  >
    <img v-if="isHoverBtn" src="" />
    <img v-else src="" />
    <span class="truncate">{{ getTitle }}</span>
  </a> -->
    <primary-button
      :title="getTitle"
      @on-active="showAnimation()"
      :prefix="prefix"
      :hover-prefix="hoverPrefix"
      :prefix-position="prefixPosition"
      :custom-style="customStyle"
      :default-class="defaultClass"
      :active-class="activeClass"
    ></primary-button>
  </div>
</template>

<script>
module.exports = {
  name: "date-picker-button",
  components: {
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
  },
  props: {
    isRange: {
      type: Boolean,
      default: () => false,
    },
    date: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      }),
    },
    frequency: {
      type: String,
      default: () => "",
    },
    parent: {
      type: String,
      default: () => ('')
    },
    prefix: { type: String, default: () => null },
    hoverPrefix: { type: String, default: () => null },
    prefixPosition: { type: String, default: () => "left" },
    customStyle: {
      type: Object,
      default: () => ({})
    },
    defaultClass: { type: String, default: () => "" },
    activeClass: { type: String, default: () => "" },
  },
  data() {
    return {
      isHoverBtn: false,
    };
  },
  created() {
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
  computed: {
    getTitle() {
      let dateData = { ...this.date };
      if(!this.date.from){
        return 'Select Date'
      }
      if (this.frequency.includes("MONTHLY")) {
        dateData.fromTitle = moment(dateData.from).format("MMMM, YYYY");
        dateData.toTitle = moment(dateData.to).format("MMMM, YYYY");
      } else if (this.frequency.includes("WEEKLY")) {
        let yearFrom = dateData.fromTitle.split("-")[0];
        let yearTo = dateData.toTitle.split("-")[0];
        let weekFrom = dateData.fromTitle.split("-")[1];
        let weekTo = dateData.toTitle.split("-")[1];
        dateData.fromTitle = `Week ${weekFrom}, ${yearFrom}`
        dateData.toTitle = `Week ${weekTo}, ${yearTo}`
      }
      if (this.isRange) {
        return `${dateData.fromTitle} - ${dateData.toTitle}`;
      } else {
        return `${dateData.fromTitle}`;
      }
    },
  },
  methods: {
    showAnimation() {
      this.$emit("click");
    },
  },
};
</script>