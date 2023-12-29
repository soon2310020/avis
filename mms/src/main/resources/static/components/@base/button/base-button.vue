<template>
  <a
    ref="buttonRef"
    href="javascript:;"
    v-on="$listeners"
    class="base-button"
    :class="[
      `button-${level}`,
      `button-${type}`,
      `button-${size}`,
      { 'button-active': active },
      { 'button-disabled': disabled },
      { 'button-animation': animate },
      { 'has-prefix': $slots.prefix },
      { 'has-postfix': $slots.postfix },
    ]"
  >
    <span class="button-base__text">
      <span v-if="$slots.prefix" class="button-prefix">
        <slot name="prefix"></slot>
      </span>
      <slot></slot>
      <span v-if="$slots.postfix" class="button-postfix">
        <slot name="postfix"></slot>
      </span>
      <span
        v-if="icon"
        class="button-base-icon"
        :class="[`icon-${icon}`]"
      ></span>
      <span v-if="hasDropdown" class="button-base__arrow"></span>
    </span>
  </a>
</template>

<script>
module.exports = {
  name: "BaseButton",
  props: {
    level: {
      type: String,
      validator(val) {
        return ["primary", "secondary", "tertiary"].includes(val);
      },
    },
    type: {
      type: String,
      default: "normal",
      validator(val) {
        return ["normal", "save", "reject", "cancel", "dropdown"].includes(val);
      },
    },
    size: {
      type: String,
      default: "medium",
      validator(val) {
        return ["large", "medium", "small"].includes(val);
      },
    },
    icon: {
      type: String,
      default: undefined,
      validator(val) {
        return [
          "calendar",
          "download",
          "upload",
          "popup",
          "add",
          "delete",
          "edit",
          "arrow",
          undefined,
        ].includes(val);
      },
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    active: {
      // manually control active state. support icon: 'arrow'
      type: Boolean,
      default: false,
    },
  },
  setup(props, { slots }) {
    const buttonRef = ref(null);
    // animation
    const animate = ref(false);
    const animateDuration = 700;
    const handleClickAnimation = () => {
      animate.value = true;
      const bubbleAnimation = setTimeout(() => {
        animate.value = false;
        clearTimeout(bubbleAnimation);
      }, animateDuration);
    };
    const hasDropdown = computed(() => props.type === "dropdown");
    // listener
    onMounted(() => {
      buttonRef.value.addEventListener("click", handleClickAnimation, true);
    });
    onUnmounted(() => {
      buttonRef.value.removeEventListener("click", handleClickAnimation, true);
    });
    return {
      buttonRef,
      animate,
      hasDropdown,
    };
  },
};
</script>

<style scoped>
.base-button {
  display: inline-flex;
  text-decoration: none;
  transition: 300ms ease;
  border-radius: 3px;
}

.button-base__text {
  position: relative;
}

.button-animation {
  animation-iteration-count: 1;
  animation-direction: alternate;
}

/* LEVEL */
.button-primary {
  color: var(--white);
  border: 1px solid var(--blue);
  background: var(--blue);
}

.button-primary .button-base-icon {
  background: var(--white);
}

.button-primary:hover {
  background: var(--blue-dark);
}

.button-primary.button-animation {
  animation: primary-animation 0.7s;
}

.button-secondary {
  border: 1px solid var(--blue);
  background: var(--white);
}

.button-secondary .button-base-icon {
  background: var(--blue);
}

.button-secondary:hover {
  border-color: var(--blue-light);
  background: var(--blue-light);
  color: var(--blue-dark);
}

.button-secondary:hover .button-base-icon {
  background: var(--blue-dark);
}

.button-secondary.button-animation {
  /* border-color: var(--blue-light); */
  animation: secondary-animation 0.7s;
}

.button-tertiary .button-base__text {
  color: var(--blue);
}

.button-tertiary:hover .button-base__text {
  color: var(--blue-dark);
  text-decoration: underline;
}

/* TYPE DROPDOWN */
.button-dropdown {
  color: var(--grey-dark);
  border: 0.5px solid var(--grey-light);
  border-radius: 1.7px;
}

.button-primary.button-dropdown {
  color: var(--white);
  border-color: var(--blue);
  background-color: var(--blue);
  border-radius: 3px;
}

.button-secondary.button-dropdown {
  color: var(--blue-dark);
  border-color: var(--blue);
  border-radius: 3px;
}

/* TYPE SAVE */
.button-primary.button-save {
  border: 1px solid var(--green);
  background: var(--green);
}

.button-primary.button-save:hover {
  border: 1px solid var(--green-dark);
  background: var(--green-dark);
}

.button-primary.button-save.button-animation {
  animation: primary-save-animation 0.7s;
}

.button-secondary.button-save {
  color: var(--green);
  border: 1px solid var(--green);
  background: var(--white);
}

.button-secondary.button-save:hover {
  color: var(--green-dark);
  border-color: var(--green-light);
  background: var(--green-light);
}

.button-secondary.button-save.button-animation {
  border-color: var(--green-light);
  animation: secondary-save-animation 0.7s;
}

/* TYPE REJECT */
.button-primary.button-reject {
  color: var(--white);
  border: 1px solid var(--red);
  background: var(--red);
}

.button-primary.button-reject:hover {
  border-color: var(--red-dark);
  background: var(--red-dark);
}

.button-primary.button-reject.button-animation {
  animation: primary-reject-animation 0.7s;
}

.button-secondary.button-reject {
  color: var(--red);
  border: 1px solid var(--red);
  background: var(--white);
}

.button-secondary.button-reject:hover {
  color: var(--red);
  background: var(--red-light);
  border-color: var(--red-light);
}

.button-secondary.button-reject.button-animation {
  animation: secondary-reject-animation 0.7s;
}

/* TYPE CANCEL */
.button-cancel {
  color: var(--grey);
  border: 1px solid var(--grey-light);
  background: var(--white);
}

.button-cancel:hover {
  color: var(--grey-dark);
  border-color: var(--grey-dark);
  background: var(--white-light);
}

.button-cancel.button-animation {
  border-color: var(--grey-light);
  animation: cancel-animation 0.7s;
}

/*  */

/* SIZE */
.button-large {
  font-size: 1.1rem;
  padding: 8px 12px;
  line-height: 17px;
}

.button-large.has-prefix {
  padding-right: 24px;
}

.button-large.has-postfix {
  padding-left: 24px;
}

.button-medium {
  padding: 6px 8px;
  line-height: 17px;
  font-size: 14.66px;
}

.button-medium.has-prefix {
  padding-right: 16px;
}

.button-medium.has-postfix {
  padding-left: 16px;
}

.button-small {
  padding: 2px 6px;
  line-height: 1;
}

.button-small.has-prefix {
  padding-right: 12px;
}

.button-small.has-postfix {
  padding-left: 12px;
}

/* STATE */
.base-button.button-disabled {
  color: var(--white);
  border-color: var(--grey-light);
  background: var(--grey-light);
  pointer-events: none;
}

/* POSTFIX & PREFIX */
.button-prefix,
.button-postfix {
  display: inline-block;
}

.button-prefix {
  margin-right: 4px;
}

.button-postfix {
  margin-left: 4px;
}

.button-prefix *,
.button-postfix * {
  max-width: 20px;
}

/*
ICON
  - for dropdown
  - calendar, upload, download
  - specific size
*/
.button-dropdown .button-base__arrow {
  display: inline-block;
  width: 15px;
  height: 8px;
  margin-left: 2px;
  mask-image: url("/images/icon/arrow-fill.svg");
  -webkit-mask-image: url("/images/icon/arrow-fill.svg");
  mask-position: center;
  -webkit-mask-position: center;
  mask-size: initial;
  -webkit-mask-size: initial;
  mask-repeat: no-repeat;
  -webkit-mask-repeat: no-repeat;
  transition: all 300ms ease;
  transform-origin: center center;
  transform: translateY(-10%);
}

.button-dropdown.button-small .base-button__arrow {
  margin-left: 0;
}

.button-dropdown.button-active .button-base__arrow {
  transform: rotate(180deg) translateY(10%);
}

.button-dropdown .button-base__arrow {
  background-color: var(--grey-dark);
}

.button-dropdown.button-primary .button-base__arrow {
  background-color: var(--white);
}

.button-dropdown.button-secondary .button-base__arrow {
  background-color: var(--blue);
}

.button-dropdown.button-disabled .button-base__arrow {
  background-color: var(--white);
}

.base-button .button-base-icon {
  display: inline-block;
  margin-left: 4px;
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
}

.button-medium .button-base-icon {
  width: 13px;
  height: 13px;
}

.button-small .button-base-icon {
  width: 11px;
  height: 11px;
}

.button-base-icon.icon-calendar {
  mask-image: url("/images/icon/calendar-2.svg");
  -webkit-mask-image: url("/images/icon/calendar-2.svg");
}

.button-base-icon.icon-upload {
  mask-image: url("/images/icon/upload-2.svg");
  -webkit-mask-image: url("/images/icon/upload-2.svg");
}

.button-medium .button-base-icon.icon-upload {
  width: 17px;
  height: 14px;
}

.button-small .button-base-icon.icon-upload {
  width: 14px;
  height: 11px;
}

.button-tertiary .button-base-icon.icon-popup {
  background-color: var(--blue);
  position: absolute;
  right: -0.75rem;
  top: 0;
  width: 8px;
  height: 8px;
}

.button-tertiary .button-base-icon.icon-popup {
  mask-image: url("/images/icon/popup.svg");
  -webkit-mask-image: url("/images/icon/popup.svg");
}

.button-tertiary:hover .button-base-icon.icon-popup {
  mask-image: url("/images/icon/popup-hover.svg");
  -webkit-mask-image: url("/images/icon/popup-hover.svg");
}

.button-base-icon.icon-arrow {
  mask-image: url("/images/icon/arrow-fill.svg");
  -webkit-mask-image: url("/images/icon/arrow-fill.svg");
  transition: transform 300ms ease;
}

.button-medium .button-base-icon.icon-arrow {
  mask-size: initial;
  -webkit-mask-size: initial;
  width: 15px;
  height: 8px;
}

.button-active .button-base-icon.icon-arrow {
  transform: rotate(180deg);
}

/* ANIMATION */
@keyframes primary-animation {
  33% {
    box-shadow: 0 0 0 2px var(--blue-light);
  }

  66% {
    box-shadow: 0 0 0 2px var(--blue-light);
  }
}

@keyframes secondary-animation {
  33% {
    box-shadow: 0 0 0px 2px var(--blue);
  }

  66% {
    box-shadow: 0 0 0px 2px var(--blue);
  }
}

@keyframes primary-save-animation {
  33% {
    box-shadow: 0 0 0 2px var(--green-light);
  }

  66% {
    box-shadow: 0 0 0 2px var(--green-light);
  }
}

@keyframes secondary-save-animation {
  33% {
    box-shadow: 0 0 0 2px var(--green);
  }

  66% {
    box-shadow: 0 0 0 2px var(--green);
  }
}

@keyframes primary-reject-animation {
  33% {
    box-shadow: 0 0 0 2px var(--red-light);
  }

  66% {
    box-shadow: 0 0 0 2px var(--red-light);
  }
}

@keyframes secondary-reject-animation {
  33% {
    box-shadow: 0 0 0 2px var(--red);
  }

  66% {
    box-shadow: 0 0 0 2px var(--red);
  }
}

@keyframes cancel-animation {
  33% {
    box-shadow: 0 0 0 2px var(--grey-light);
  }

  66% {
    box-shadow: 0 0 0 2px var(--grey-light);
  }
}
</style>
