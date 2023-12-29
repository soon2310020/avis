<template>
  <div
      ref="commonPopover"
      class="common-popover"
      :class="{ show: isVisible }"
      :style="{
      left: position.left,
      right: position.right,
      top: position.top,
      bottom: position.bottom,
    }"
  >
    <slot></slot>
  </div>
</template>

<script>
module.exports = {
  name: "CommonPopover",
  props: {
    isVisible: {
      type: Boolean,
      default: false,
    },
    classes: {
      type: String,
      default: "",
    },
    position: {
      type: Object,
      default: () => ({ top: "", left: "", right: "", bottom: "" }),
    },
  },
  methods: {
    handleClickOutside(event) {
      const el = this.$refs.commonPopover;
      if (!(el === event.target || el?.contains(event.target))) {
        this.handleClose();
      }
    },
    handleClose() {
      console.log('run close')
      this.$emit("close");
    },
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        console.log('add listener')
        window.addEventListener("click", this.handleClickOutside, true);
      } else {
        console.log('remove listener')
        window.removeEventListener("click", this.handleClickOutside, true);
      }
    }
  },
};
</script>

<style scoped>
.common-popover {
  position: absolute;
  min-width: 463px;
  z-index: 100;
  background-color: #fff;
  visibility: hidden;
  opacity: 0;
  transform: translateY(-20px);
  pointer-events: none;
  transition: 300ms ease;
  background: #fff;
  box-shadow: 1px 1px 5px rgba(0, 0, 0, 0.1);
}
.common-popover.show {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
</style>