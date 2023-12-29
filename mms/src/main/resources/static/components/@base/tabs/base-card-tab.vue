<template>
  <div
    ref="baseCardTab"
    class="base-card-tab"
    :class="[
      { 'base-card-tab__active': active },
      { 'base-card-tab__animate': animate },
      { 'no-content': !content },
      `base-card-tab__${size}`,
    ]"
    v-on="$listeners"
  >
    <div class="base-card-tab__icon">
      <span
        class="base-card-tab__icon-svg"
        :style="`mask-image: url(${iconSrc}); -webkit-mask-image: url(${iconSrc}); ${iconStyle}`"
      ></span>
    </div>
    <div class="base-card-tab__content">
      <div class="content-head">{{ title }}</div>
      <div class="content-sub">{{ content }}</div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "BaseCardTab",
  props: {
    iconSrc: String,
    iconStyle: String,
    arrow: Boolean,
    title: String,
    content: String,
    active: Boolean,
    size: {
      type: String,
      default: "medium",
      validator(val) {
        return ["medium", "small"].includes(val);
      },
    },
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
    const baseCardTab = this.$refs.baseCardTab;
    if (baseCardTab) {
      baseCardTab.addEventListener("click", this.handleClick);
    }
  },
  destroyed() {
    const baseCardTab = this.$refs.baseCardTab;
    if (baseCardTab) {
      baseCardTab.removeEventListener("click", this.handleClick);
    }
  },
};
</script>

<style scoped>
.base-card-tab {
  display: inline-flex;
  align-items: center;
  width: fit-content;

  box-shadow: 0px 3px 4px rgba(0, 0, 0, 0.15);
  border-radius: 3px;
  cursor: pointer;

  border: 1px solid var(--white);
  transition: all 100ms ease;
}

.base-card-tab__medium {
  min-width: 248px;
  width: 19%;
  flex-grow: 0;
  min-height: 88px;
  padding-left: 8px;
  padding-right: 8px;
  padding-top: 21px;
  padding-bottom: 17px;
}

@media (min-width: 1200px) and (max-width: 1400px) {
  .base-card-tab__medium {
    width: 24% !important;
  }
}

@media (max-width: 1200px) {
  .base-card-tab__medium {
    width: 32% !important;
  }
}

.base-card-tab__medium.no-content {
  padding-left: 6px;
}

.base-card-tab__small {
  min-width: 160px;
  max-width: 160px;
  min-height: 45px;
  padding-top: 14px;
  padding-bottom: 14px;
  padding-left: 6px;
  padding-right: 6px;
}

.base-card-tab__icon {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e6e6e6;
  margin-right: 22px;
}

.base-card-tab__small .base-card-tab__icon {
  width: 36px;
  height: 36px;
}

.base-card-tab__medium.no-content .base-card-tab__icon {
  margin-right: 11px;
}

.base-card-tab__small .base-card-tab__content {
  font-size: 15px;
}

.base-card-tab__small .base-card-tab__icon {
  margin-right: 4px;
}

.base-card-tab__icon-svg {
  display: inline-block;
  width: 32px;
  height: 100%;
  mask-position: center;
  /* mask-size: contain; */
  mask-size: 27px 27px;
  mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: 27px 27px;
  /* -webkit-mask-size: contain; */
  -webkit-mask-repeat: no-repeat;
  background-color: #666569;
}

.base-card-tab__content .content-head {
  font-size: 24px;
  font-weight: 700;
  color: var(--grey-dark);
}
.base-card-tab__content .content-sub {
  font-size: 15px;
  font-weight: 500;
  color: var(--grey-a);
}

.base-card-tab__small .base-card-tab__content .content-head {
  font-size: 15px;
}

/* HOVER */
.base-card-tab:hover {
  border: 1px solid var(--blue-dark);
}

.base-card-tab:hover .base-card-tab__icon {
  background-color: var(--blue-light);
}

.base-card-tab:hover .base-card-tab__icon-svg {
  background-color: var(--blue-dark);
  transform: scale(1.1);
}

/* ACTIVE */

.base-card-tab__active {
  /* border: 1px solid var(--blue-dark); */
  border: 1px solid var(--blue-light);
  outline: 3px solid var(--blue-light);
  box-shadow: unset;
}
.base-card-tab__active .base-card-tab__icon {
  background-color: var(--blue-light);
}

.base-card-tab__active .base-card-tab__icon-svg {
  background-color: var(--blue-dark);
  transform: scale(1.1);
}

/* ANIMATE */
.base-card-tab__animate {
  border: 1px solid var(--blue-dark);
  outline: 1px solid var(--blue-dark);
  box-shadow: 0px 3px 4px rgba(0, 0, 0, 0.15);
}
.base-card-tab__animate .base-card-tab__icon {
  background-color: var(--blue-light);
}

.base-card-tab__animate .base-card-tab__icon-svg {
  background-color: var(--blue-dark);
}
</style>
