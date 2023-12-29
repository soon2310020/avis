<template>
  <div v-if="visible" ref="dialog" class="base-dialog">
    <div
      ref="content"
      class="base-dialogContent"
      :class="computedClasses"
      :style="dialogStyles"
      @click.stop="() => {}"
    >
      <!-- HEADER -->
      <div
        class="base-dialogContentHeader"
        :class="titleClasses"
        :style="titleStyles"
      >
        <slot v-if="hasCustomTitle" name="title"></slot>
        <div v-else style="line-height: 25px">
          <span>{{ title }}</span>
        </div>
        <span class="close-button" @click="handleClose">
          <span class="t-icon-close"></span>
        </span>
      </div>
      <!-- END HEADER -->
      <!-- BODY -->
      <div
        class="base-dialogContentBody"
        :class="bodyClasses"
        :style="bodyStyles"
      >
        <slot></slot>
      </div>
      <!-- END BODY -->
      <!-- FOOTER -->
      <div class="base-dialogContentFooter">
        <slot name="footer"></slot>
      </div>
      <!-- END FOOTER -->
    </div>
    <div ref="overlay" class="base-dialogOverlay" @click="handleClose"></div>
  </div>
</template>

<script>
// defineEmits = ['close']
// type DialogSize = "xl" | "lg" | "md" | "sm";
const DURATION = 700;
module.exports = {
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    size: {
      type: String,
      default: "md",
    },
    title: {
      type: String,
    },
    nested: {
      type: Boolean,
      default: false,
    },
    dialogStyles: [String, Object],
    bodyClasses: [String, Array],
    bodyStyles: [String, Object],
    titleClasses: [String, Array],
    titleStyles: [String, Object],
  },
  data() {
    return {
      dialogContainer: null,
      debounce: null,
      preventClose: false,
    };
  },
  computed: {
    computedClasses() {
      return `base-dialogContent__${this.size} ${
        this.nested ? "base-dialogContent__nested" : ""
      }`;
    },
    hasCustomTitle() {
      return this?.$slots?.title;
    },
  },
  methods: {
    handleOpen() {
      console.log("open");
      this.preventClose = true;
      clearTimeout(this.debounce);
      if (!this.dialogContainer) {
        this.dialogContainer = document.createElement("div");
      }
      document.body.appendChild(this.dialogContainer);
      this.$nextTick(() => {
        const dialogEl = this.$refs.dialog;
        const overlayEl = this.$refs.overlay;
        const contentEl = this.$refs.content;
        if (dialogEl && this.dialogContainer) {
          this.dialogContainer.appendChild(dialogEl);
          overlayEl.classList.add("show");

          setTimeout(() => {
            contentEl.classList.add("show");
            contentEl.classList.add("slide-top");
            this.preventClose = false;
          }, DURATION / 2);
        }
      });
    },
    handleClose() {
      console.log("close");
      const contentEl = this.$refs?.content;
      if (this.preventClose || !contentEl) return;
      contentEl.classList.remove("show");
      contentEl.classList.remove("slide-top");
      contentEl.classList.add("slide-bottom");
      setTimeout(() => {
        const overlayEl = this.$refs.overlay;
        overlayEl.classList.remove("show");
      }, DURATION / 1.5);

      this.debounce = setTimeout(() => {
        if (this.dialogContainer) {
          document.body.removeChild(this.dialogContainer);
          this.$emit("update:visible", false); // using by v-model:visible
          this.$emit("close"); // using by @close
          this.dialogContainer = null;
        }
      }, DURATION);
    },
  },
  watch: {
    visible: {
      handler(newVal) {
        if (newVal) {
          this.handleOpen();
        }
      },
      immediate: true,
    },
  },
};
</script>

<style scoped>
.base-dialog {
  position: fixed;
  top: 0;
  left: 0;
  display: flex;
  width: 100%;
  /* height: 100%; */
  height: 100vh;
  justify-content: center;
  /* align-items: center; */
  align-items: flex-start;
  z-index: 5000;
  max-height: 100%;
  overflow-y: auto;
}

.base-dialogOverlay {
  /* position: absolute; */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  opacity: 0;
  visibility: hidden;
  transition: 300ms cubic-bezier(0.2, 0.4, 0.4, 0.9);
}

.base-dialogOverlay.show {
  opacity: 1;
  visibility: visible;
}

.base-dialogContent {
  background-color: #fff;
  margin-top: 28px;
  margin-bottom: 52px;
  width: 100%;
  border-radius: 8px;
  opacity: 0;
  visibility: hidden;
  transition: 300ms cubic-bezier(0.2, 0.4, 0.4, 0.9);
  z-index: 500;
  position: relative;
  transform: translateY(0);
  box-shadow: 1px 3px 10px -1px rgba(0, 0, 0, 0.7);
}

.base-dialogContent__xl {
  max-width: 90%;
}

.base-dialogContent__lg {
  max-width: 1200px;
}

.base-dialogContent__md {
  /* max-width: 800px; */
  max-width: 1024px;
}

.base-dialogContent__sm {
  /* max-width: 400px; */
  max-width: 600px;
}

.base-dialogContent__nested {
  margin-top: auto !important;
  margin-bottom: auto !important;
}

.base-dialogContent.show {
  opacity: 1;
  visibility: visible;
}

.base-dialogContent .base-dialogContentHeader {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /* padding: 19.5px 25.5px 11.5px 25px; */
  padding: 15.5px 25.5px 11.5px 25px;
  border-radius: 6px 6px 0px 0px;
  background-color: rgb(245, 248, 255);
  font-weight: bold;
  font-size: 1rem;
}

.base-dialogContent .base-dialogContentHeader::after {
  content: "";
  position: absolute;
  background: rgb(82, 161, 255);
  height: 8px;
  border-radius: 6px 6px 0px 0px;
  top: -5px;
  left: 0px;
  width: 100%;
}

.base-dialogContent .base-dialogContentBody {
  min-height: 300px;
  padding: 20px 52px;
  overflow-x: initial;
  flex: 1 1 auto;
  position: relative;
  background-color: var(--white);
  border-radius: 0 0 0.3rem 0.3rem;
}

.base-dialogContent .base-dialogContentFooter {
}

.slide-top {
  -webkit-animation: slide-top 0.5s cubic-bezier(0.2, 0.4, 0.4, 0.9) both;
  animation: slide-top 0.5s cubic-bezier(0.2, 0.4, 0.4, 0.9) both;
}

.slide-bottom {
  -webkit-animation: slide-bottom 0.5s cubic-bezier(0.2, 0.4, 0.4, 0.9) both;
  animation: slide-bottom 0.5s cubic-bezier(0.2, 0.4, 0.4, 0.9) both;
}

@keyframes slide-top {
  0% {
    /* -webkit-transform: translateY(0); */
    /* transform: translateY(0); */
    -webkit-transform: translateY(28px);
    transform: translateY(28px);
  }

  100% {
    /* -webkit-transform: translateY(-100px); */
    /* transform: translateY(-100px); */
    -webkit-transform: translateY(0);
    transform: translate(0);
  }
}

@keyframes slide-bottom {
  0% {
    /* -webkit-transform: translateY(0); */
    /* transform: translateY(0); */
    -webkit-transform: translateY(0);
    transform: translateY(0);
  }

  100% {
    /* -webkit-transform: translateY(100px); */
    /* transform: translateY(100px); */
    -webkit-transform: translateY(28px);
    transform: translateY(28px);
  }
}
</style>
