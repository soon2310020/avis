<template>
  <div class="middle">
    <div class="line left-line">
      <div class="down">&#x2304;</div>
    </div>
    <div class="line middle-line">
      <div class="down">&#x2304;</div>
    </div>
    <div class="line right-line">
      <div class="down">&#x2304;</div>
    </div>
  </div>
</template>

<script>
module.exports = {};
</script>

<style scoped>
.diamond {
  display: flex;
  position: relative;
  width: 140px;
}
.content {
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
}

.diamond::before {
  border-left: 0px;
  border-right: 70px solid red;
}

.diamond::after {
  border-left: 70px solid red;
  border-right: 0px;
}

.border {
  position: absolute;
  width: 78px;
  border-bottom: 1px dashed black;
}

.border-left-above,
.border-left-below {
  transform: rotate(27deg);
  transform-origin: left;
}

.border-right-above,
.border-right-below {
  right: 0px;
  transform: rotate(-27deg);
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

.line {
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
.right-line {
  right: 0;
}
.down {
  position: absolute;
  left: -6px;
  font-size: 20px;
}
</style>