<template>
  <div class="diamond">
    <div class="diamond-content">{{content}}</div>
    <div class="diamond-border border-left-above"></div>
    <div class="diamond-border border-right-above"></div>
    <div class="diamond-border border-left-below"></div>
    <div class="diamond-border border-right-below"></div>

    <template v-if="index == 0">
      <div class="diamond-line first-line diamond-left-line"></div>
      <div class="diamond-line first-line diamond-right-line"></div>
    </template>

    <template v-else>
      <div v-if="index !== 3" class="diamond-line diamond-left-line"></div>
      <div v-if="index == 3" class="diamond-line last-line diamond-left-line"></div>
      <div class="diamond-line middle-line"></div>
      <div v-if="index !== 3" class="diamond-line diamond-right-line"></div>
      <div v-if="index == 3" class="diamond-line last-line diamond-right-line"></div>
    </template>
  </div>
</template>

<script>
module.exports = {
  props: {
    image: String,
    index: Number,
    content: String
  },
  data() {
    return {};
  }
};
</script>

<style scoped>
.diamond {
  display: flex;
  position: relative;
  width: 140px;
}
.diamond-content {
  position: absolute;
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.middle {
  display: flex;
  position: relative;
  width: 140px;
  height: 20px;
}
.diamond::before,
.diamond::after {
  content: "";
  border: 50px solid transparent;
  border-bottom: 35px solid transparent;
  border-top: 35px solid transparent;
  width: 0;
  height: 20px;
  box-sizing: content-box !important;
}

.diamond::before {
  border-left: 0px;
  border-right: 70px solid red;
  box-sizing: content-box !important;
}

.diamond::after {
  border-left: 70px solid red;
  border-right: 0px;
}

.diamond-border {
  position: absolute;
  width: 78px;
  border-bottom: 1px dashed black;
}

.border-left-above,
.border-left-below {
  transform: rotate(26deg);
  transform-origin: left;
}

.border-right-above,
.border-right-below {
  right: 0px;
  transform: rotate(-26deg);
  transform-origin: right;
}

.border-left-above,
.border-right-above {
  top: calc(50% - 10px);
}

.border-left-below,
.border-right-below {
  top: calc(50% + 10px);
}

.diamond-line {
  position: absolute;
  height: 90px;
  border-left: 1px dashed gray;
}
.first-line {
  height: 50px;
  bottom: 0;
}
.last-line {
  height: 45px;
}
.middle-line {
  left: 50%;
}
.diamond-right-line {
  right: 0;
}
.diamond-left-line {
  left: 0;
}
.down {
  position: absolute;
  left: -6px;
  font-size: 20px;
}
</style>