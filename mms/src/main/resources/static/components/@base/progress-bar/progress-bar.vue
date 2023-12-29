<template>
  <div>
    <span class="progress-title" v-if="!clickable && title" :style="titleStyle">{{ title }}</span>
    <div v-if="clickable" style="position: relative; width: fit-content" @click="handleClick">
      <span class="progress-title-active" :style="titleStyle">{{title}}</span>
      <div class="hyper-link-icon" style="margin-left: 3px"></div>
    </div>
    <div class="progress-wrapper">
      <div class="active-progress-wrapper" :class="`active-progress-${color}`" :style="{width: Math.min(100, progressPercentage)+'%'}"></div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: 'ProgressBar',
  props: {
    title: {
      type: String,
      default: ''
    },
    progressPercentage: {
      type: Number,
      default: 0
    },
    clickable: {
      type: Boolean,
      default: () => false
    },
    color: {
      type: String,
      default: () => 'dark-blue',
      validator(val) {
        return ['dark-blue', 'pink'].includes(val)
      }
    },
    titleStyle: {
      type: Object,
      default: () => ({})
    }

  },
  setup(props, ctx /** attrs, slots, emit, expose, root, refs */) {
    const handleClick = () => {
      ctx.emit("on-click")
    }
    return {
      handleClick
    };
  }
}
</script>

<style scoped>
.progress-wrapper {
  background-color: #D6DADE;
  height: 4px;
  width: 100%;
}
.active-progress-dark-blue {
  background-color: #1A2281;
}
.active-progress-pink {
  background-color: #EB709A;
}
.active-progress-wrapper {
  height: 100%;
}
.progress-title, .progress-title-active {
  margin-bottom: 4px;
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #4B4B4B;
}
.progress-title-active {
  color: #3491FF;
  cursor: pointer;
}
.hyper-link-icon {
  background-image: url('/images/icon/hyperlink-icon.svg');
  position: absolute;
  width: 7px;
  height: 7px;
  background-repeat: no-repeat;
  top: 0;
  right: -10px;
}
.hyper-link-icon:hover {
  background-image: url('/images/icon/hyperlink-icon-hover.svg');
  width: 11px;
  height: 13px;
  right: -14px;
  top: -2px;
}
</style>
