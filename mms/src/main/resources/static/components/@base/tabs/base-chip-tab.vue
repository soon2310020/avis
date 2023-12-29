<template>
  <div
    ref="baseChipTab"
    class="base-chip-tab"
    :class="[
      `base-chip-tab-${level}`,
      { 'base-chip-tab-active': active },
      { 'base-chip-tab-animate': animate },
    ]"
    v-on="$listeners"
  >
    <span><slot></slot></span>
  </div>
</template>

<script>
module.exports = {
  name: "BaseChipTab",
  props: {
    level: {
      type: String,
      default: "primary",
      validator(val) {
        return ["primary", "secondary"].includes(val);
      },
    },
    label: String,
    active: Boolean,
  },
  data() {
    return {
      animate: false,
    };
  },
  methods: {
    handleClick() {
      this.animate = true;
      setTimeout(() => {
        this.animate = false;
      }, 200);
    },
  },
  mounted() {
    const baseChipTab = this.$refs.baseChipTab;
    if (baseChipTab) {
      baseChipTab.addEventListener("click", this.handleClick);
    }
  },
  destroyed() {
    const baseChipTab = this.$refs.baseChipTab;
    if (baseChipTab) {
      baseChipTab.removeEventListener("click", this.handleClick);
    }
  },
};
</script>
<style scoped>
.base-chip-tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0.5px solid #d0d0d0;
  border-radius: 3px;
  min-height: 26px;
  cursor: pointer;
  transition: all 200ms ease;
}

.base-chip-tab span {
  font-size: 14.66px;
  line-height: 17px;
}

/* PRIMARY */
.base-chip-tab-primary {
  background-color: var(--white);
  color: var(--grey-8);
  padding-left: 1rem;
  padding-right: 1rem;
}

.base-chip-tab-primary.base-chip-tab-active {
  background: var(--blue);
  color: var(--white);
}

/* SECONDARY */
.base-chip-tab-secondary {
  padding: 6px 8px;
  color: var(--grey-8b);
  border: 0.5px solid #d0d0d0;
}

.base-chip-tab-secondary.base-chip-tab-active {
  color: var(--blue-dark);
  background-color: var(--blue-light);
  border-color: #d0d0d0;
}

.base-chip-tab-secondary.base-chip-tab-animate {
  color: var(--blue);
  border-color: var(--blue);
  background-color: var(--blue-light);
}
</style>
