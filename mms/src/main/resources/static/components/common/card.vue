<template>
  <div :class="classProps" :style="styleProps">
    <div v-if="title" class="title">{{ title }}</div>
    <slot></slot>
  </div>
</template>

<script>
module.exports = {
  props: {
    classProps: {
      default: "style-1",
      type: String,
    },
    title: {
      default: "",
      type: String,
    },
    styleProps: {
      default: "",
      type: String,
    },
  },
};
</script>

<style scoped>
.style-1 {
  padding: 8px;
  background: #ffffff 0% 0% no-repeat padding-box;
  box-shadow: 0px 3px 6px #00000029;
  border: 0.5px solid #c8ced3;
  border-radius: 5px;
}
.style-2 {
  border: 1px solid #d6dade;
  border-radius: 3px;
}
.style-2 > .title {
  width: 100%;
  height: 33px;
  padding: 0px 8px;
  color: #595959;
  font: 16px/18px Helvetica Neue;
  display: flex;
  align-items: center;
  background: #f0f3f5;
}

.card-style-3 {
  width: fit-content;
  min-width: 280px;
  min-height: 280px;
  border: solid 1px #d6d6d6;
  box-shadow: 0px 3px 6px #00000029;
  border-radius: 3px;
}
.card-style-3 > .title {
  width: 100%;
  min-height: 40px;
  color: #4b4b4b;
  font: 16px/18px Helvetica Neue;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: solid 2px #f3f3f3;
}

.title-border-none > .title {
  border-bottom: none;
}
</style>
