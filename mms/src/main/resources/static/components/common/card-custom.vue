<template>
  <div :class="cardStyle">
    <div v-if="title" class="title">{{ title }}</div>
    <slot></slot>
  </div>
</template>

<script>
module.exports = {
  props: {
    cardStyle: String,
    title: String,
  },
};
</script>

<style scoped>
.style-1 {
  padding: 12px 6px;
  background: #ffffff 0% 0% no-repeat padding-box;
  box-shadow: 0px 3px 6px #00000029;
  border: 1px solid #C4C4C4;
  border-radius: 3px;
  cursor: pointer;
}
.style-2 {
  border: 1px solid #C4C4C4;
  border-radius: 2px;
  width: 50%;
  /*align-self: stretch;*/
}
.style-2 > .title {
  text-align: center;
  font-weight: 700;
  padding-top: 10px;
  color: #4B4B4B;
  font-size: 14px;
}
</style>