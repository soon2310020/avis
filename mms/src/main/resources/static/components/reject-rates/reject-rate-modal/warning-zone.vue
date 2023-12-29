<template>
  <div class="warning-zone">
    <div class="warning-zone__container">
      <div>
        <!-- <svg
          class="warning-icon"
          xmlns="http://www.w3.org/2000/svg"
          width="24.419"
          height="21.545"
          viewBox="0 0 24.419 21.545"
        >
          <path
            id="Icon_awesome-exclamation-triangle"
            data-name="Icon awesome-exclamation-triangle"
            d="M24.145,18.516a2.021,2.021,0,0,1-1.763,3.029H2.037A2.021,2.021,0,0,1,.275,18.516L10.447,1.009a2.044,2.044,0,0,1,3.525,0ZM12.21,14.9a1.936,1.936,0,1,0,1.95,1.936A1.943,1.943,0,0,0,12.21,14.9ZM10.358,7.938l.314,5.723a.507.507,0,0,0,.508.477h2.058a.507.507,0,0,0,.508-.477l.314-5.723a.507.507,0,0,0-.508-.532H10.866A.507.507,0,0,0,10.358,7.938Z"
            fill="#db3b21"
          />
        </svg> -->
        <img src="/images/icon/warning-2.svg" alt="warning" />
      </div>
      <div class="divider"></div>
      <div class="warning-text-gray">
        <strong>
          Empty rejected part reasons have been detected for the following
          toolings:
        </strong>
      </div>
      <ul class="warning-content">
        <li
          v-for="item in listRejectRateEmpty"
          :key="item"
          class="warning-content-code"
        >
          {{ item }}
        </li>
      </ul>
      <div class="warning-text-gray">
        <strong>
          The unfilled reasons will be assumed as 0. Do you want to proceed?
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
    listRejectRateEmpty: {
      type: Array,
      default: () => [],
    },
    isInputting: {
      type: Boolean,
      default: false,
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
            this.onProceed();
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