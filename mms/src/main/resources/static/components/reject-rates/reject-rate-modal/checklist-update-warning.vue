<template>
  <div class="warning-zone">
    <div class="warning-zone__container">
      <div>
        <img src="/images/icon/warning-2.svg" alt="warning" />
      </div>
      <div class="divider"></div>
      <div class="warning-text-gray">
        <strong>
          Editing checklist will result in deletion of previously entered reject
          part reasons. Do you want to proceed?
        </strong>
      </div>
      <div class="warning-action">
        <a
          ref="warning-light-btn"
          href="javascript:void(0)"
          class="warning-button warning-button-light"
          @click="showWarningAnimation('warning-light-btn', false)"
        >
          Cancel</a
        >
        <a
          ref="warning-confirm-btn"
          href="javascript:void(0)"
          class="warning-button warning-button-confirm"
          @click="showWarningAnimation('warning-confirm-btn', true)"
          >Yes. Proceed</a
        >
        <!-- <a-button
          id="warning-danger-btn"
          class="warning-button warning-button-light"
          @click="onCancel"
        >
          Cancel
        </a-button>
        <a-button
          id="warning-confirm-btn"
          class="warning-button warning-button-confirm"
          @click="onProceed"
        >
          Yes. Proceed
        </a-button> -->
      </div>
    </div>
  </div>
</template>

<script>
// warning zone
module.exports = {
  name: "WarningZone",
  props: {
    onCancel: {
      type: Function,
      default: () => {},
    },
    onProceed: {
      type: Function,
      default: () => {},
    },
  },
  methods: {
    showWarningAnimation(id, type) {
      const el = this.$refs[id];
      if (el) {
        if (type) {
          el.classList.add("warning-button-success-animation");
          setTimeout(() => {
            el.classList.remove("warning-button-success-animation");
            // force
            this.onProceed(true);
          }, 700);
        } else {
          el.classList.add("warning-button-light-animation");
          setTimeout(() => {
            el.classList.remove("warning-button-light-animation");
            this.onCancel();
          }, 700);
        }
      }
    },
  },
};
</script>

<style scoped>

</style>