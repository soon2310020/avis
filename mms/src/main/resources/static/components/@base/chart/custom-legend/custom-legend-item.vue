<template>
  <div
    class="custom-legend-item"
    :style="{ width: width ? `${width}px` : null }"
  >
    <div class="item-baseline">
      <div class="prefix">
        <div
          v-if="icon"
          class="custom-legend__icon"
          :class="`icon-${icon}`"
          :style="{ background: iconColor }"
        ></div>
        <template v-else>
          <slot name="prefix"></slot>
        </template>
      </div>
      <div>
        <span
          style="color: #000000"
          :style="{ 'font-size': `${fontSize}px` }"
          >{{ title }}</span
        >
      </div>
    </div>
    <div
      v-if="hasData"
      class="custom-legen-item__link"
      @mouseover="isHover = true"
      @mouseleave="isHover = false"
      @click="handleLinkClick"
    >
      <span :style="{ 'font-size': `${fontSize}px` }">{{ data }}</span>
      <div class="link-icon item-baseline">
        <div v-show="!isHover">
          <img
            style="max-width: 105px"
            src="/images/icon/tabbed-overview/link-icon.svg"
          />
        </div>
        <div v-show="isHover">
          <img
            style="max-width: 105px"
            src="/images/icon/tabbed-overview/link-icon-hover.svg"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    title: {
      type: String,
      default: () => ('')
    },
    width: {
      type: Number
    },
    fontSize: {
      type: Number,
      default: 13
    },
    data: {
      type: Number,
      default: () => (0)
    },
    hasData: {
      type: Boolean,
      default: true
    },
    icon: {
      type: String,
      default: '',
      validator(val) {
        return ['', 'square', 'circle'].includes(val)
      }
    },
    iconColor: {
      type: String,
      default: '#000'
    }
  },
  data() {
    return {
      isHover: false,
    }
  },
  methods: {
    handleLinkClick() {
      this.$emit('link-click')
    }
  },
}
</script>

<style scoped>
.custom-legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 7px;
  font-size: 13px;
  color: #595959;
  position: relative;
}
.custom-legend__icon {
  background: rgb(188, 226, 199);
  width: 14px;
  height: 14px;
  margin-right: 6px;
}
.custom-legend__icon.icon-square {
  border-radius: 3px;
}
.custom-legend__icon.icon-circle {
  border-radius: 50%;
}
.custom-legen-item__link {
  display: flex;
  color: rgb(52, 145, 255);
  height: 21px;
  cursor: pointer;
}
.link-icon {
  width: 6.749px;
  height: 6.822px;
  margin-left: 2px;
}
.link-icon svg {
  position: absolute;
  top: 3px;
  right: -5px;
}
.item-baseline {
  display: flex;
  align-items: center;
}
</style>