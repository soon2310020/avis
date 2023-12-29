<template>
  <div :ref="'toast' + refCustom" class="success-toast toast-hidden">
    <div class="success-toast-content position-relative" :class="status">
      <b>{{ title }}</b>
      <div style="margin-top: 5px">
        {{ content }}
      </div>
      <div class="exit-button">
        <img
          @click="closeSuccess"
          src="/images/icon/category/exit.svg"
          alt=""
        />
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "toast",
  components: {},
  props: {
    resources: Object,
    show: Boolean,
    type: String,
    title: String,
    content: String,
    refCustom: String,
    status: String,
  },
  data() {
    return {};
  },
  computed: {},
  watch: {
    show(newVal) {
      const toast = this.$refs["toast" + this.refCustom];
      if (toast) {
        if (newVal) {
          document.body.appendChild(toast);
          toast.classList.remove("toast-hidden");
          toast.classList.add("float-in-animation");
          setTimeout(() => {
            toast.classList.remove("float-in-animation");
          }, 700);
        } else {
          toast.classList.add("float-out-animation");
          setTimeout(() => {
            toast.classList.remove("float-out-animation");
            toast.classList.add("toast-hidden");
            document.body?.removeChild(toast);
          }, 700);
        }
      }
    },
  },
  methods: {
    closeSuccess() {
      this.$emit("close-toast");
    },
  },
};
</script>

<style scoped>
.success-toast {
  position: fixed;
  bottom: 60px;
  right: 50%;
  transform: translate(50%, 0px);
  font-size: 12px;
  z-index: 999999;
  box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px;
}
.success-toast-content {
  border: 1px solid var(--blue);
  background-color: var(--blue-light-2);
  border-radius: 2px;
  padding: 8px 14px;
  line-height: 100%;
  color: var(--blue);
  min-width: 424px;
}
.success-toast-content.fail {
  border: 1px solid #c92617;
  background: #f5c7c7;
  color: #c92617;
}
.exit-button {
  position: absolute;
  top: 6px;
  right: 8px;
}
.exit-button img {
  cursor: pointer;
}
.fail .exit-button img {
  filter: grayscale(100%) brightness(40%) sepia(100%) hue-rotate(-50deg)
    saturate(600%) contrast(0.8);
}
.float-in-animation {
  animation-name: float-in;
  animation-duration: 0.7s;
  animation-iteration-count: 1;
  animation-timing-function: ease-in-out;
}
@keyframes float-in {
  0% {
    opacity: 0;
    transform: translate(50%, 100px) scale(0);
  }
  100% {
    opacity: 1;
    transform: translate(50%, 0px) scale(1);
  }
}
.float-out-animation {
  animation-name: float-out;
  animation-duration: 0.7s;
  animation-iteration-count: 1;
  animation-timing-function: ease-in-out;
}
@keyframes float-out {
  0% {
    opacity: 1;
    transform: translate(50%, 0px);
  }
  100% {
    opacity: 0;
    transform: translate(50%, 100px);
  }
}
.toast-hidden {
  display: none;
}
</style>
