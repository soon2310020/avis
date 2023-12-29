<template>
  <div :style="{ width: `${size}px`, height: `${size}px` }">
    <div class="lds-dual-ring"></div>
  </div>
</template>
<script>
module.exports = {
  props: {
    size: {
      type: [String, Number],
      default: 20,
    },
  },
};
</script>
<style scoped>
.lds-dual-ring {
  display: inline-block;
  width: 100%;
  height: 100%;
}
.lds-dual-ring:after {
  content: " ";
  display: block;
  width: 80%;
  height: 80%;
  margin: 8px;
  border-radius: 50%;
  border: 6px solid #4c4c4c;
  border-color: #4c4c4c transparent #4c4c4c transparent;
  animation: lds-dual-ring 1.2s linear infinite;
}
@keyframes lds-dual-ring {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
