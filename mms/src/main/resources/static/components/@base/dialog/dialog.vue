<!-- 
    @deprecated 
    from 2022/10/05 please use base-dialog.vue instead
    this component will be archived for app stable purpose
    and will be remove after
-->

<template>
  <div
    ref="dialogRef"
    class="custom-dialog dialog"
    :class="{ 'child-dialog': isChildDialog }"
    :style="dialogStyles"
  >
    <div ref="dialogContentRef" class="dialog-content" :class="[dialogClasses]">
      <!-- MODAL HEAD START-->
      <div class="dialog-head" :class="titleClasses" :style="titleStyles">
        <slot v-if="hasCustomTitle" name="title"></slot>
        <div v-else style="line-height: 25px">
          <span class="title">{{ title }}</span>
        </div>
        <span class="close-button" @click="handleClose">
          <span class="t-icon-close"></span>
        </span>
      </div>
      <!-- MODAL HEAD END -->

      <!-- MODAL BODY START -->
      <div class="dialog-body" :class="bodyClasses" :style="bodyStyles">
        <slot></slot>
      </div>
      <!-- MODAL BODY END -->
    </div>

    <div ref="backdropRef" class="dialog-backdrop"></div>
  </div>
</template>

<script>
// defineEmits = ['close']
module.exports = {
  name: "CommonDialog",
  props: {
    // Display state
    visible: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: "",
    },

    // Styles
    dialogClasses: {
      type: String,
      default: "",
    },
    dialogStyles: {
      type: String,
      default: "",
    },
    titleClasses: {
      type: String,
      default: "",
    },
    titleStyles: {
      type: String,
      default: "",
    },
    bodyClasses: {
      type: String,
      default: "",
    },
    bodyStyles: {
      type: String,
      default: "",
    },
  },
  setup(props, ctx /** attrs, slots, emit, expose, root, refs */) {
    const visible = toRef(props, "visible");

    const handleClose = () => {
      ctx.emit("update:visible", !visible.value);
      ctx.emit("close");
    };

    const handleClickBackdrop = () => {
      handleClose();
    };

    const showDialog = () => {
      ctx.refs?.dialogRef?.classList?.add("show");
    };

    const hideDialog = () => {
      ctx.refs?.dialogRef?.classList?.remove("show");
    };

    const showDialogContent = () => {
      ctx.refs?.dialogContentRef.classList?.add("show");
    };

    const hideDialogContent = () => {
      ctx.refs?.dialogContentRef?.classList?.remove("show");
    };

    const showBackdrop = () => {
      ctx.refs?.backdropRef?.classList?.add("show");
    };
    const hideBackdrop = () => {
      ctx.refs?.backdropRef?.classList?.remove("show");
    };

    watch(visible, (newVal) => {
      console.log("visible", newVal);
      if (newVal) {
        ctx.refs?.backdropRef?.addEventListener("click", handleClickBackdrop);
        showDialog();
        showBackdrop();
        setTimeout(() => showDialogContent(), 300);
      } else {
        ctx.refs?.backdropRef?.removeEventListener(
          "click",
          handleClickBackdrop
        );
        hideDialogContent();
        setTimeout(() => hideDialog(), 300);
        setTimeout(() => hideBackdrop(), 500);
      }
    });

    const hasCustomTitle = computed(() => ctx.slots?.title || null);
    const isChildDialog = computed(
      () =>
        ctx.refs?.dialogRef?.parentNode.closest(".custom-dialog") ||
        ctx.refs?.dialogRef?.parentNode.closest(".modal")
    );

    return {
      hasCustomTitle,
      handleClose,
      isChildDialog,
    };
  },
};
</script>

<style scoped>
.custom-dialog.dialog {
  visibility: hidden;
  pointer-events: none;
  opacity: 0;
  position: fixed;
  left: 0;
  top: 0;
  height: 100%;
  width: 100%;
}

.show.custom-dialog.dialog {
  visibility: visible;
  pointer-events: auto;
  opacity: 1;
  z-index: 5000;
}

.custom-dialog.dialog .dialog-xl {
  max-width: 1024px;
}

.custom-dialog.dialog .dialog-lg {
  max-width: 800px;
}

.custom-dialog.dialog .dialog-md {
  max-width: 620px;
}

/* DIALOG CONTENT */
.dialog-content {
  opacity: 0;
  visibility: hidden;
  transform: translateY(25%);
  border-radius: 0.3rem;
  margin: 2.25rem auto 0;
  position: relative;
  z-index: 1500;
  border-radius: 10px 10px 4px 4px;
  transition: transform 0.3s ease-out, opacity 0.15s linear,
    visibility 0.15s linear;
  box-shadow: rgba(100, 100, 111, 1) 0px 7px 29px 0px;
}

.dialog-content.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  transition: transform 0.3s ease-out 0.3s, opacity 0.15s linear 0.3s,
    visibility 0.15s linear 0.3s;
}

.child-dialog .dialog-content {
  top: 50%;
  transform: translateY(-75%);
}

.child-dialog .dialog-content.show {
  top: 50%;
  transform: translateY(-50%);
}

/* DIALOG HEAD */
.custom-dialog .dialog-head {
  background: rgb(245, 248, 255);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11.5px 25.5px 11.5px;
  margin-bottom: 0;
  line-height: 1.5;
}

.custom-dialog .dialog-head::before {
  content: "";
  position: absolute;
  background: rgb(82, 161, 255);
  height: 8px;
  border-radius: unset;
  top: -8px;
  left: 0px;
  width: 100%;
  border-radius: 0.3rem 0.3rem 0 0;
}

.custom-dialog .dialog-head span.title {
  color: rgb(75, 75, 75);
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}

.custom-dialog .dialog-head .close-button {
  font-size: 25px;
  display: flex;
  align-items: center;
  height: 17px;
  cursor: pointer;
}

.custom-dialog .dialog-head .t-icon-close {
  width: 12px;
  height: 12px;
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

/* DIALOG TITLE END */

/* DIALOG BODY */
.custom-dialog .dialog-body {
  padding: 20px 52px;
  overflow-x: initial;
  flex: 1 1 auto;
  position: relative;
  background-color: var(--white);
  border-radius: 0 0 0.3rem 0.3rem;
}

/* DIALOG BODY END */

/* BACKDROP */
.dialog-backdrop {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
  /* z-index: 4999; */
  z-index: 1030;
  pointer-events: none;
  visibility: hidden;
  transition: opacity 0.15s linear 0.3s, visibility 0.15s linear 0.3s;
  opacity: 0;
}

.dialog-backdrop.show {
  pointer-events: auto;
  opacity: 0.5;
  visibility: visible;
  transition: opacity 0.15s linear, visibility 0.15s linear;
}
</style>
