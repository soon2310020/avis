<template>
  <div ref="popupRef" class="custom-popup popup" :style="popupStyles">
    <div class="popup-content">
      <div class="popup-inner" :class="[popupClasses]">
        <div class="popup-title">
          <span class="popup-title__type" :class="type"></span>
          <span class="popup-title__text"> {{ title }}</span>
        </div>
        <div class="popup-message">
          <slot></slot>
        </div>
        <div class="popup-action">
          <base-button type="cancel" @click="$emit('close')">Cancel</base-button>
          <base-button :type="buttonType" :level="buttonLevel" @click="$emit('confirm')">
            {{ label || "Confirm" }}
          </base-button>
        </div>
      </div>
    </div>
    <div class="popup-backdrop" @click="handleClose"></div>
  </div>
</template>

<script>
module.exports = {
  name: 'BasePopup',
  props: {
    visible: Boolean,
    title: String,
    label: String,
    popupClasses: String,
    popupStyles: String,
    type: {
      type: String,
      validator(val) {
        return ['warning', 'notification'].includes(val)
      },
      default: 'warning'
    },
    buttonType: {
      type: String,
      default: 'success',
      validator(val) {
        return ['success', 'reject', 'cancel'].includes(val)
      },
    },
    buttonLevel: {
      type: String,
      default: 'primary',
      validator(val) {
        return ['primary', 'secondary'].includes(val)
      },
    }
  },
  setup(props, ctx) {

    const clonePopup = ref(null)
    const contentPopup = ref(null)
    const innerPopup = ref(null)
    const backdropPopup = ref(null)

    const visible = toRef(props, 'visible')

    const handleClickOnBackdrop = (event) => {
      !Boolean(event.target.closest('.popup-inner')) && handleClose()
    }

    const handleClose = () => {
      console.log('close')
      ctx.emit("update:visible", !visible.value)
      ctx.emit("close");
    };

    watch(visible, (newVal) => {
      if (newVal) {
        clonePopup.value = ctx.refs.popupRef
        document.body.appendChild(clonePopup.value)
        clonePopup.value.classList.add('show')
        contentPopup.value = clonePopup.value.childNodes[0]
        innerPopup.value = contentPopup.value.childNodes[0]
        backdropPopup.value = clonePopup.value.childNodes[2]
        backdropPopup.value.classList.add('show')
        contentPopup.value.classList.add('show')
        setTimeout(() => {
          contentPopup.value.addEventListener('click', handleClickOnBackdrop, true)
          innerPopup.value.classList.add('show')
        }, 300)
      } else {
        innerPopup.value.classList.remove('show')
        contentPopup.value.removeEventListener('click', handleClickOnBackdrop, true)
        setTimeout(() => {
          contentPopup.value.classList.remove('show')
          backdropPopup.value.classList.remove('show')
          setTimeout(() => {
            clonePopup.value.classList.remove('show')
            clonePopup.value.remove()
          }, 200)
        }, 300)
      }
    });

    return {
      handleClose
    }
  }
}
</script>

<style scoped>
.custom-popup.popup {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  visibility: hidden;
  z-index: 100;
}

.custom-popup.popup.show {
  height: 100%;
  visibility: visible;
}

.custom-popup.popup .popup-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-popup.popup .popup-content.show {
  z-index: 101;
  height: 100vh;
  overflow: auto;
}

/* popup inner */
.custom-popup.popup .popup-content .popup-inner {
  position: relative;
  min-width: 360px;
  padding: 25px 25px 18px 25px;
  margin-left: auto;
  margin-right: auto;
  background-color: #fff;
  border-radius: 3px;
  box-shadow: rgb(100 100 111 / 20%) 0px 7px 29px 0px;
  top: -25%;
  visibility: hidden;
  opacity: 0;
  pointer-events: auto;
  transition: top 0.3s ease-out, opacity 0.15s ease, visibility 0.15s ease;
}

.custom-popup.popup .popup-content .popup-inner.show {
  position: relative;
  top: -10%;
  visibility: visible;
  opacity: 1;
}

/* popup title */
.popup-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 11px;
}
.popup-title .popup-title__type {
  display: inline-block;
  width: 15px;
  height: 15px;
  mask-size: contain;
  mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}

.popup-title .popup-title__type.warning {
  background-color: var(--red);
  mask-image: url("/assets/icons/warning.svg");
  -webkit-mask-image: url("/assets/icons/warning.svg");
}

.popup-title .popup-title__text {
  font-weight: bold;
  font-size: 14.66;
}

.popup-message {
  font-size: 14.66px;
}

/* popup action */
.popup-action {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 23px;
}

/* popup backdrop */
.custom-popup .popup-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  background: #000;
  width: 100%;
  height: 100%;
  opacity: 0;
  visibility: hidden;
  transition: all 150ms ease;
}

.custom-popup .popup-backdrop.show {
  opacity: 0.5;
  visibility: visible;
}
.custom-popup.popup.popup-nested.show {
  backdrop-filter: blur(1px);
}

.custom-popup.popup.popup-nested.show .popup-content .popup-inner.show {
  top: calc(50% - 100px);
  transform: translateY(-50% + 200px);
}

.custom-popup.popup.popup-nested.show .popup-backdrop.show {
  opacity: 0.1;
}
</style>