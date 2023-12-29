<template>
  <a-tooltip color="white">
    <template slot="title">
      <div
        style="
          padding: 6px 8px;
          font-size: 13px;
          color: #4b4b4b;
          background: #fff;
          border-radius: 4px;
          box-shadow: 0px 0px 4px 1px #e5dfdf;
        "
      >
        <slot name="tooltips"></slot>
        <div class="custom-arrow-tooltip"></div>
      </div>
    </template>
    <a
      id="customization-modal"
      @click="handleClick()"
      href="javascript:void(0)"
      class="btn-custom btn-outline-custom-primary customize-btn"
    >
      <slot></slot>
    </a>
  </a-tooltip>
</template>
<script>
module.exports = {
  methods: {
    handleClick() {
      this.$emit("click");
    },
  },
};
</script>
