<template>
  <div v-on:click="func(index)" :style="{opacity: active ? 1 : 0.5}" class="diamond">
    <img style="height: 80px;" :src="image" />
    <div class="diamond-border border-left-above left-above"></div>
    <div class="diamond-border border-right-above right-above"></div>
    <div class="diamond-border border-left-below left-below"></div>
    <div class="diamond-border border-right-below right-below"></div>
    <template v-if="index < 4">
      <div class="line-left"></div>
      <!-- <div class="line-mid"></div> -->
      <div class="line-right"></div>
      <img class="down-left" style="height: 15px;" src="/images/dashboard/down-icon-diamond.svg" />
      <img class="down-right" style="height: 15px;" src="/images/dashboard/down-icon-diamond.svg" />
    </template>
    <div class="diamond-content">
      <div class="diamond-text">{{getTitle()}}</div>
      <div class="diamond-text">{{content}}</div>
    </div>
    <template v-if="index > 1">
      <div class="top-line-left"></div>
      <!-- <div class="top-line-mid"></div> -->
      <div class="top-line-right"></div>
    </template>
  </div>
</template>

<script>
module.exports = {
  props: {
    image: String,
    index: Number,
    content: String,
    active: Boolean,
    func: Function,
    resources:Object,
  },
  methods: {
    getTitle: function() {
      if (this.index == 1) {
        return this.resources['category'];
      } else if (this.index == 2) {
        return this.resources['project'];
      } else if (this.index == 3) {
        return this.resources['part'];
      } else {
        return this.resources['tooling'];
      }
    }
  },
  data() {
    return {};
  }
};
</script>

<style scoped>
.diamond {
  position: relative;
  display: inline-block;
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.down-left {
  position: absolute;
  left: -3.5px;
  top: calc(100%);
}

.down-right {
  position: absolute;
  right: -3.5px;
  top: calc(100%);
}

.line-left {
  margin-top: 5px;
  position: absolute;
  height: calc(50% + 21px);
  left: 3px;
  top: calc(50% - 14px);
  left: 3px;
  width: 1px;
  border-left: 1.5px dashed #c7c3c3;
}
.line-mid {
  position: absolute;
  width: 1px;
  top: calc(100% - 5px);
  height: 12px;
  left: calc(50% - 1px);
  border-left: 1.5px dashed #c7c3c3;
}
.line-right {
  margin-top: 5px;
  position: absolute;
  top: calc(50% - 14px);
  height: calc(50% + 21px);
  right: 3px;
  width: 1px;
  border-left: 1.5px dashed #c7c3c3;
}
.top-line-left {
  position: absolute;
  height: calc(50%);
  top: calc(-50% + 39px);
  left: 3px;
  left: 3px;
  width: 1px;
  border-left: 1.5px dashed #c7c3c3;
}
.top-line-mid {
  position: absolute;
  width: 1px;
  left: calc(50% - 1px);
  top: calc(-6px);
  height: 12px;
  height: 100%;
  border-left: 1.5px dashed #c7c3c3;
}
.top-line-right {
  position: absolute;
  height: calc(50%);
  top: calc(-50% + 42px);
  right: 3px;
  width: 1px;
  border-left: 1.5px dashed #c7c3c3;
}

.diamond-text {
  font-size: 12px;
  font-weight: 900;
  color: black;
}

.diamond-content {
  position: absolute;
  top: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 2;
}
.diamond-border {
  position: absolute;
  width: 69px;
  height: 0px;
  border-top: 1px dashed #c7c3c3;
  z-index: 2;
}

.border-left-above,
.border-left-below {
  transform: rotate(30deg);
  transform-origin: left;
  left: 3px;
  width: 69px;
}

.left-above {
  top: calc(50% - 10px);
}

.left-below {
  top: calc(50% + 2px);
}

.right-above {
  top: calc(100% - 16px);
}

.right-below {
  top: calc(100% - 5px);
}
.border-right-above,
.border-right-below {
  width: 69px;
  right: 50%;
  transform: rotate(150deg);
  transform-origin: right;
  z-index: 2;
}
</style>