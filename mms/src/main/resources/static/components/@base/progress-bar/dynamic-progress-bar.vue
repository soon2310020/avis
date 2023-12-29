<template>
  <div class="dynamic-progress-bar-wrapper">
    <div class="active-wrapper" :class="`active-progress-${color}`" :style="{ width: percentageValue+'%'}"></div>
    <label class="percentage-txt">{{ getPercentageTitle }}</label>
  </div>
</template>

<script>

module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({})
    },
    percentage: {
      type: Number,
      default: () => 0
    },
    color: {
      type: String,
      default: () => 'purple',
      validator: (value) => {
        return ["purple"].includes(value);
      },
    }
  },
  components: {
  },
  setup(props, ctx) {

    const percentageValue = computed(() => {
      return Math.min(100, props.percentage)
    });

    const getPercentageTitle = computed(() => {
      return percentageValue.value + '%'
    });
    return {
      // STATE //
      // COMPUTED //
      percentageValue,
      getPercentageTitle,
      // METHOD //
    }
  }
}
</script>

<style scoped>
.dynamic-progress-bar-wrapper {
  width: 100%;
  background-color: #D6DADE;
  min-height: 32.92px;
  position: relative
}
.active-wrapper {
  position: absolute;
  height: 100%;
  top: 0
}
.active-progress-purple {
  background-color: #C0ADF2;
}
.percentage-txt {
  font-weight: 700;
  font-size: 17px;
  line-height: 20px;
  text-align: center;
  color: #4B4B4B;
  width: 100%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  position: absolute;
}
</style>