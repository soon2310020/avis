<template>
  <a
    href="javascript:void(0)"
    class="btn-custom custom-select-button"
    :style="customStyles"
    :class="[
      type,
      isHover ? 'hover' : '',
      isActive ? 'active' : '',
      selected ? 'has-value' : '',
      customClasses,
    ]"
    v-bind="$props"
    @mouseover="isHover = true"
    @mouseout="isHover = false"
    v-on="$listeners"
  >
    <template v-if="!shouldBeTruncated">
      <span v-if="selected">{{ label }}</span>
      <slot v-else></slot>
      <span
        class="dropdown-carret"
        :class="{ rotate: isActive }"
      >
        <img
          v-if="type === 'outline'"
          src="/images/icon/icon-cta-blue.svg"
          alt=""
        >
        <img
          v-if="type === 'secondary'"
          src="/images/icon/Polygon 1.svg"
          alt=""
        />
      </span>
    </template>
    <template v-else>
      <a-tooltip>
        <template slot="title"><div style="padding: 6px 8px">{{ label }}</div></template>
        <span v-if="selected">{{ truncatedLabel }}</span>
        <slot v-else></slot>
        <span
          class="dropdown-carret"
          :class="{ rotate: isActive }"
        >
          <img
            v-if="type === 'outline'"
            src="/images/icon/icon-cta-blue.svg"
            alt=""
          >
          <img
            v-if="type === 'secondary'"
            src="/images/icon/Polygon 1.svg"
            alt=""
          />
        </span>
      </a-tooltip>
    </template>
  </a>
</template>

<script>
module.exports = {
  name: "CommonSelectButton",
  props: {
    type: String, // 'primary'|'secondary'
    isActive: Boolean,
    customStyles: String,
    customClasses: String,
    selected: {
      type: [Number, String, Object, Array],
      default: null,
    },
    getLabel: {
      type: Function,
      default: (val) => val,
    },
    maxChar: {
      type: Number,
      default: 10
    }
  },
  setup(props, ctx) {
    const isHover = ref(false);
    const label = computed(() => {
      if (props.selected && typeof props.getLabel(props.selected) === 'string') return props.getLabel(props.selected)
      return ''
    })
    const shouldBeTruncated = computed(() => label.value.length > props.maxChar)
    const truncatedLabel = computed(() => label.value.slice(0, props.maxChar) + '...')
    return {
      isHover,
      shouldBeTruncated,
      label,
      truncatedLabel
    };
  },
};
</script>

<style scoped>
.custom-select-button {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 3px;
  font-size: 11pt;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  text-decoration: none !important;
  line-height: 17px;
}
/* primary */

/* secondary */
.custom-select-button.secondary {
  display: flex;
  align-items: center;
  width: fit-content;
  height: fit-content;
  padding: 6px 8px;
  text-decoration: none !important;
  line-height: 17px;
  color: rgb(201, 201, 201);
  border: unset !important;
  border-radius: 3px;
  background-color: rgb(255, 255, 255);
  outline: rgb(144, 144, 144) solid 1px;
}
.custom-select-button.secondary:hover {
  outline: rgb(75, 75, 75) solid 1px;
}
.custom-select-button.secondary.has-value {
  color: #4b4b4b;
}
/* outline */
.custom-select-button.outline {
  color: #3491ff;
  background-color: #fff;
  background-image: none;
  border: 1px solid #3491ff;
}
.custom-select-button.outline:hover {
  color: #0e65c7;
  background-color: #daeeff;
  border-color: #daeeff;
}
.custom-select-button.outline.has-value {
}

.dropdown-carret {
  display: inline;
  align-items: center;
  margin-left: 6px;
  transition-property: transform;
  transition-duration: 0.3s;
  transition-timing-function: linear;
}
.dropdown-carret.rotate {
  transform: rotate(180deg);
}
</style>