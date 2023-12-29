<template>
  <div>
    <a
      ref="btn"
      @mouseover="isHover = true"
      @mouseleave="isHover = false"
      href="javascript:void(0)"
      class="d-flex"
      style="align-items: center; width: fit-content;"
      @click="handleClick()"
      :class="defaultClass"
      :style="customStyle"
    >
      <span class="truncate">{{ title }}</span>
      <div class="btn-prefix" :class="{ rotate: isActive }">
        <img v-if="getPrefixSrc" :src="getPrefixSrc" />
      </div>
    </a>
  </div>
</template>

<script>
module.exports = {
  props: {
    isActive: { type: Boolean, default: () => false },
    title: { type: String, default: () => "" },
    prefix: { type: String, default: () => null },
    hoverPrefix: { type: String, default: () => null },
    customStyle: {
      type: Object,
      default: () => ({}),
    },
    defaultClass: { type: String, default: () => "" },
    activeClass: { type: String, default: () => "" },
  },
  data() {
    return {
      isHover: false,
    };
  },
  computed: {
    getPrefixSrc() {
      if (this.prefix) {
        if (this.isHover && this.hoverPrefix) {
          return this.hoverPrefix;
        }
        return this.prefix;
      }
      return null;
    },
  },
  methods: {
    handleClick() {
      const el = this.$refs["btn"];
      el.classList.add(this.activeClass);
      setTimeout(() => {
        this.$emit("on-active");
        el.classList.remove(this.activeClass);
      }, 800);
    },
  },
};
</script>

<style scoped>
.btn-prefix {
  margin-left: 4px;
  transition: 0.3s ease-in-out;
  display: flex;
  align-items: center;
  margin-bottom: 0 !important;
}
.rotate {
  transform: rotate(180deg);
}
</style>