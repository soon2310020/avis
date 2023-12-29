<template>
  <div
    v-if="showRiskLevel"
    class="risk-level"
    :class="{ 'config-area': isInAdmin }"
    id="risk-default"
  >
    <!-- <div v-if="isInAdmin" class="config-header">
      <div class="left-header">
        <h4>{{ title }}</h4>
      </div>
    </div> -->
    <div v-if="!isInAdmin" class="risk-level-header">
      <div></div>
      <div class="risk-level-caption">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="29.001"
          height="10.683"
          viewBox="0 0 29.001 10.683"
        >
          <g id="Group_3497" data-name="Group 3497" transform="translate(0 0)">
            <path
              id="Exclusion_5"
              data-name="Exclusion 5"
              d="M7.039,10.683a.259.259,0,0,1-.211-.1L5.945,9.413a3.95,3.95,0,0,1-.451.026,4.17,4.17,0,0,1-.615-.047l-.926,1.137a.282.282,0,0,1-.21.1.255.255,0,0,1-.138-.04L1.732,9.462a.273.273,0,0,1-.112-.337l.571-1.348A4.275,4.275,0,0,1,1.9,7.328a4.415,4.415,0,0,1-.226-.486L.228,6.612A.272.272,0,0,1,0,6.338L.039,4.155a.267.267,0,0,1,.234-.266l1.454-.182a4.161,4.161,0,0,1,.547-.916l-.52-1.366a.27.27,0,0,1,.123-.331L3.788.035A.277.277,0,0,1,3.923,0a.261.261,0,0,1,.211.105l.884,1.166a3.873,3.873,0,0,1,.454-.027,4.151,4.151,0,0,1,.613.046L7.008.155a.284.284,0,0,1,.211-.1A.256.256,0,0,1,7.358.1L9.23,1.222a.276.276,0,0,1,.113.337L8.771,2.905a4.228,4.228,0,0,1,.293.451,4.566,4.566,0,0,1,.227.486l.868.138h2.105a3.4,3.4,0,0,1,6.237,0h3.977a3.405,3.405,0,1,1,0,2.724H18.5a3.4,3.4,0,0,1-6.237,0H10.859a.268.268,0,0,1-.169.091l-1.454.182a4.127,4.127,0,0,1-.548.915l.52,1.367a.267.267,0,0,1-.122.33L7.173,10.649A.274.274,0,0,1,7.039,10.683Zm-1.916-7.2a1.5,1.5,0,0,0-1.5,1.5V5.7a1.5,1.5,0,0,0,1.5,1.5h.714a1.5,1.5,0,0,0,1.5-1.5V4.985a1.5,1.5,0,0,0-1.5-1.5Z"
              fill="#171717"
            />
          </g>
        </svg>
        <span style="margin-left: 5.9px">Set Risk Level</span>
      </div>
      <div class="risk-level-close" @click="close">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="13.426"
          height="13.423"
          viewBox="0 0 13.426 13.423"
        >
          <path
            id="Icon_ionic-ios-close"
            data-name="Icon ionic-ios-close"
            d="M19.589,18l4.8-4.8A1.124,1.124,0,0,0,22.8,11.616l-4.8,4.8-4.8-4.8A1.124,1.124,0,1,0,11.616,13.2l4.8,4.8-4.8,4.8A1.124,1.124,0,0,0,13.2,24.384l4.8-4.8,4.8,4.8A1.124,1.124,0,1,0,24.384,22.8Z"
            transform="translate(-11.285 -11.289)"
            fill="#5a5a5a"
          />
        </svg>
      </div>
    </div>
    <div class="relative">
      <div class="d-flex slider-lines align-center absolute">
        <div
          v-for="item in 20"
          class="d-flex justify-space-between slider-line-group align-center"
        >
          <div class="slider-line--big"></div>
          <div v-for="i in 5" class="slider-line--small"></div>
        </div>
        <div class="slider-line--big"></div>
      </div>
      <vue-slider
        ref="slider"
        :value="dots"
        :process="processFunc"
        :enable-cross="false"
        :interval="1"
        :marks="marks1"
        @dragend="updateRisk"
        @change="updateRisk"
        :lazy="true"
      >
        <!--      <template v-slot:mark="{ pos, label }">-->
        <!--        <div class="custom-mark" :style="{ left: `${pos}%` }">-->
        <!--          {{ label }} + '1'-->
        <!--        </div>-->
        <!--      </template>-->
      </vue-slider>
    </div>
    <div class="risk-footer">
      <div>
        <span v-text="resources['high']"></span>
        <div>
          0 ~ {{ convertNumber(dots2[0]) }} %
          <span class="bottom-color high"></span>
        </div>
      </div>
      <div>
        <span v-text="resources['medium']"></span>
        <div>
          {{ convertNumber(dots2[0]) }} ~ {{ convertNumber(dots2[1]) }}%
          <span class="bottom-color meidum"></span>
        </div>
      </div>
      <div>
        <span v-text="resources['low']"></span>
        <div>
          {{ convertNumber(dots2[1]) }} ~ 100%
          <span class="bottom-color low"></span>
        </div>
      </div>
    </div>
  </div>
</template>
<!--<script type="module" src="./risk-default"></script>-->
<script>
// const { ThreeValueSlider } = require('./risk-default');
// const ThreeValueSlider = require('./risk-default');
let dotsPosPre = [];
module.exports = {
  name: "risk-default",
  props: {
    resources: Object,
    title: {
      type: String,
      default: "OEE Configuration",
    },
    closeRisk: Function,
    updateRisk: Function,
    riskLevelData: Array,
    showRiskLevel: Boolean,
    isInAdmin: {
      type: Boolean,
      default: false,
    },
  },
  components: { "vue-slider": window["vue-slider-component"] },
  data() {
    return {
      items: ["item_0", "item_1", "item_2"],
      dots: this.riskLevelData,
      dots2: this.riskLevelData,
      active: {
        background: "#707070",
        color: "#707070",
      },
      marks1: {
        0: "0%",
        5: "",
        10: "10%",
        15: "",
        20: "20%",
        25: "",
        30: "30%",
        35: "",
        40: "40%",
        45: "",
        50: "50%",
        55: "",
        60: "60%",
        65: "",
        70: "70%",
        75: "",
        80: "80%",
        85: "",
        90: "90%",
        95: "",
        100: "100%",
      },
      color: ["#E34537", "#F7CC57", "#41CE77"],
    };
  },
  watch: {
    showRiskLevel(newVal) {
      if (!newVal) {
        this.updateRisk(this.dots2);
      }
    },
    dots2(dotsPos) {
      console.log("newval", dotsPos);
      this.marks1 = {
        0: {
          label: "0%",
          activeStyle: dotsPos[0] === 0 || dotsPos[1] === 0 ? this.active : {},
        },
        5: {
          label: "",
          activeStyle: dotsPos[0] === 5 || dotsPos[1] === 5 ? this.active : {},
        },
        10: {
          label: "10%",
          activeStyle:
            dotsPos[0] === 10 || dotsPos[1] === 10 ? this.active : {},
        },
        15: {
          label: "",
          activeStyle:
            dotsPos[0] === 15 || dotsPos[1] === 15 ? this.active : {},
        },
        20: {
          label: "20%",
          activeStyle:
            dotsPos[0] === 20 || dotsPos[1] === 20 ? this.active : {},
        },
        25: {
          label: "",
          activeStyle:
            dotsPos[0] === 25 || dotsPos[1] === 25 ? this.active : {},
        },
        30: {
          label: "30%",
          activeStyle:
            dotsPos[0] === 30 || dotsPos[1] === 30 ? this.active : {},
        },
        35: {
          label: "",
          activeStyle:
            dotsPos[0] === 35 || dotsPos[1] === 35 ? this.active : {},
        },
        40: {
          label: "40%",
          activeStyle:
            dotsPos[0] === 40 || dotsPos[1] === 40 ? this.active : {},
        },
        45: {
          label: "",
          activeStyle:
            dotsPos[0] === 45 || dotsPos[1] === 45 ? this.active : {},
        },
        50: {
          label: "50%",
          activeStyle:
            dotsPos[0] === 50 || dotsPos[1] === 50 ? this.active : {},
        },
        55: {
          label: "",
          activeStyle:
            dotsPos[0] === 55 || dotsPos[1] === 55 ? this.active : {},
        },
        60: {
          label: "60%",
          activeStyle:
            dotsPos[0] === 60 || dotsPos[1] === 60 ? this.active : {},
        },
        65: {
          label: "",
          activeStyle:
            dotsPos[0] === 65 || dotsPos[1] === 65 ? this.active : {},
        },
        70: {
          label: "70%",
          activeStyle:
            dotsPos[0] === 70 || dotsPos[1] === 70 ? this.active : {},
        },
        75: {
          label: "",
          activeStyle:
            dotsPos[0] === 75 || dotsPos[1] === 75 ? this.active : {},
        },
        80: {
          label: "80%",
          activeStyle:
            dotsPos[0] === 80 || dotsPos[1] === 80 ? this.active : {},
        },
        85: {
          label: "",
          activeStyle:
            dotsPos[0] === 85 || dotsPos[1] === 85 ? this.active : {},
        },
        90: {
          label: "90%",
          activeStyle:
            dotsPos[0] === 90 || dotsPos[1] === 90 ? this.active : {},
        },
        95: {
          label: "",
          activeStyle:
            dotsPos[0] === 95 || dotsPos[1] === 95 ? this.active : {},
        },
        100: {
          label: "100%",
          activeStyle:
            dotsPos[0] === 100 || dotsPos[1] === 100 ? this.active : {},
        },
      };
    },
  },
  mounted() {
    // this.marks1 = {
    //   '0': {
    //     lable: '0%',
    //     activeStyle: (this.dots2[0] === '0' || this.dots2[1] === '0') ? this.active : {}
    //   },
    //   '5': {
    //     lable: '',
    //     activeStyle: (this.dots2[0] === '5' || this.dots2[1] === '5') ? this.active : {}
    //   },
    //   '10': '10%',
    //   '15': '',
    //   '20': '20%',
    //   '25': '',
    //   '30': '30%',
    //   '35': '',
    //   '40': {
    //     lable: '40%',
    //     activeStyle: (this.dots2[0] === '40' || this.dots2[1] === '40') ? this.active : {}
    //   },
    //   '45': '',
    //   '50': '50%',
    //   '55': '',
    //   '60': '60%',
    //   '65': '',
    //   '70': '70%',
    //   '75': '',
    //   '80': '80%',
    //   '85': '',
    //   '90': '90%',
    //   '95': '',
    //   '100': '100%'
    // }
  },
  methods: {
    convertNumber(num) {
      return Math.ceil(num / 1) * 1;
      // return num;
    },
    processFunc: function (dotsPos) {
      var array = [];
      if (dotsPos.length < 1) {
        return array;
      }
      if (dotsPos[0] < 1) {
        dotsPos[0] = 1;
      } else if (dotsPos[1] > 99) {
        dotsPos[1] = 99;
      }
      console.log("=1=1", dotsPos[0], dotsPosPre[1]);
      if (dotsPos[0] === dotsPosPre[1]) {
        dotsPos[0] = dotsPos[0] - 1;
      }
      if (dotsPos[1] === dotsPosPre[0]) {
        dotsPos[1] = dotsPos[1] + 1;
      }
      dotsPosPre = dotsPos;
      var color = this.color;

      array[0] = [0, dotsPos[0], { backgroundColor: color[0] }];
      for (var i = 1; i < dotsPos.length; i++) {
        array[i] = [
          dotsPos[i - 1],
          dotsPos[i],
          { backgroundColor: color[i % 5] },
        ];
      }
      array[dotsPos.length] = [
        dotsPos[dotsPos.length - 1],
        100,
        { backgroundColor: color[dotsPos.length % 5], disabled: true },
      ];
      this.dots2 = dotsPos;
      return array;
    },
    close() {
      console.log(this.dots2, "updateRiskupdateRisk");
      // this.updateRisk(this.dots2)
      this.closeRisk();
    },
  },
  beforeDestroy() {
    this.updateRisk(this.dots2);
  },
  mounted() {
    this.$nextTick(() => {
      Common.anchor($(".app-body"), $("#risk-default"));
    });
  },
};
</script>
<style>
.vue-slider-dot-tooltip {
  display: none;
}
</style>
<style scoped>
html,
body {
  height: 100%;
}
.risk-level {
  position: absolute;
  border: 1px solid #707070;
  padding: 15px 25px;
  width: 110%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);
  border-radius: 11px;
  background: #fff;
  z-index: 999999999;
}
.risk-level-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}
.risk-level-caption {
  font-size: 22px;
  line-height: 100%;
  color: #000;
  font-weight: 500;
  text-align: center;
}
.three-value-slider {
  position: relative;
  max-width: 100%;
  white-space: nowrap;
  display: flex;
  /*-webkit-box-align: center;*/
  /*-moz-box-align: center;*/
  /*-o-box-align: center;*/
  /*-ms-flex-align: center;*/
  /*-webkit-align-items: center;*/
  align-items: center;
  height: 20px;
  margin: 0 25px;
}
.three-value-slider .slider-handle {
  display: block;
  position: absolute;
  top: 0;
  background-color: #fff;
  height: 20px;
  width: 25px;
  border: 1px solid #888;
  /*-webkit-box-sizing: border-box;*/
  /*-moz-box-sizing: border-box;*/
  box-sizing: border-box;
  cursor: pointer;
  border-radius: 15px;
  padding: 0 3px;
}
.three-value-slider .slider-handle.left {
  left: 0;
}
.three-value-slider .slider-handle.right {
  right: 0;
}
.three-value-slider .slider-bar {
  display: inline-block;
  height: 10px;
}
.three-value-slider .slider-bar.left {
  background-color: #f67576;
  border-top-left-radius: calc(10px / 2);
  border-bottom-left-radius: calc(10px / 2);
  padding-right: calc(25px / 2);
}
.three-value-slider .slider-bar.middle {
  background-color: #f7cc53;
  padding: 0 calc(25px / 2);
}
.three-value-slider .slider-bar.right {
  background-color: #41ce77;
  border-top-right-radius: calc(10px / 2);
  border-bottom-right-radius: calc(10px / 2);
  padding-left: calc(25px / 2);
}
.risk-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}
.risk-footer > div {
  display: flex;
  flex-direction: column;
  text-align: center;
  align-items: center;
}
.risk-footer > div > div {
  position: relative;
  display: flex;
  justify-content: center;
  overflow: hidden;
  align-items: center;
  width: 80px;
  height: 30px;
  border: 1px solid #000;
  border-radius: 4px;
}
.bottom-color {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 4px;
}
.bottom-color.high {
  background: #f67576;
}
.bottom-color.meidum {
  background: #f7cc53;
}
.bottom-color.low {
  background: #41ce77;
}
.risk-level-close {
  cursor: pointer;
}
</style>
<style scoped>
/* component style */
.vue-slider {
  height: 18px !important;
}
.vue-slider-disabled .vue-slider-process {
  background-color: #a7a7a7;
}
.vue-slider-disabled .vue-slider-dot-handle {
  border-color: #a7a7a7;
}
.vue-slider-disabled .vue-slider-mark-step-active {
  box-shadow: 0 0 0 2px #a7a7a7;
}
.vue-slider-mark {
  /*position: relative;*/
  z-index: -1;
}
/* rail style */
.vue-slider-rail {
  background-color: whitesmoke;
  border-radius: 15px;
  transition: background-color 0.3s;
}
.vue-slider:hover .vue-slider-rail {
  background-color: #e1e1e1;
}

/* process style */
.vue-slider-process {
  background-color: #9cd5ff;
  /*border-radius: 15px;*/
  transition: background-color 0.3s;
}
.vue-slider:hover .vue-slider-process {
  background-color: #69c0ff;
}

/* mark style */
.vue-slider-mark-step {
  box-shadow: none !important;
  background: #d4d4d4;
  height: 27px;
  width: 1px;
  border-radius: 0;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
.vue-slider-mark-step-active {
  box-shadow: 0 0 0 2px #9cd5ff;
}
.vue-slider:hover .vue-slider-mark-step-active {
  box-shadow: 0 0 0 2px #69c0ff;
}

.vue-slider-mark-label {
  font-size: 9px;
  white-space: nowrap;
  top: unset;
  bottom: 25px;
  margin-top: unset;
}
/* dot style */
.vue-slider-dot-handle {
  cursor: pointer;
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #fff;
  border: 1px solid #707070;
  box-sizing: border-box;
  transition: box-shadow 0.3s, border-color 0.3s;
}
.vue-slider-dot-handle::after {
  background: rgb(212, 212, 212);
  height: 27px;
  width: 1px;
  border-radius: 0px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  box-shadow: none !important;
}
.vue-slider-dot-handle:hover {
  /*border-color: #69c0ff;*/
  box-shadow: 0 0 0 5px rgb(54 171 255 / 40%);
}

.vue-slider-dot-handle-focus {
  /*border-color: #36abff;*/
  box-shadow: 0 0 0 5px rgb(45 134 197 / 60%);
}
.vue-slider:hover .vue-slider-dot-handle-focus {
  border-color: #36abff;
}

.vue-slider-dot-handle:hover {
  border-color: #36abff;
}
.vue-slider:hover .vue-slider-dot-handle:hover {
  border-color: #36abff;
}

.vue-slider-dot-handle-disabled {
  cursor: not-allowed;
  border-color: #ddd !important;
}

.vue-slider-dot-tooltip {
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
}
.vue-slider-dot-tooltip-inner {
  font-size: 14px;
  white-space: nowrap;
  padding: 6px 8px;
  color: #fff;
  border-radius: 5px;
  border-color: rgba(0, 0, 0, 0.75);
  background-color: rgba(0, 0, 0, 0.75);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transform: scale(0.9);
  transition: transform 0.3s;
}
.vue-slider-dot-tooltip-inner::after {
  content: "";
  position: absolute;
}
.vue-slider-dot-tooltip-inner-top::after {
  bottom: 100%;
  left: 50%;
  transform: translate(-50%, 0);
  height: 0;
  width: 0;
  border-color: transparent;
  border-style: solid;
  border-width: 5px;
  border-top-color: inherit;
}
.vue-slider-dot-tooltip-inner-bottom::after {
  bottom: 100%;
  left: 50%;
  transform: translate(-50%, 0);
  height: 0;
  width: 0;
  border-color: transparent;
  border-style: solid;
  border-width: 5px;
  border-bottom-color: inherit;
}
.vue-slider-dot-tooltip-inner-left::after {
  left: 100%;
  top: 50%;
  transform: translate(0, -50%);
  height: 0;
  width: 0;
  border-color: transparent;
  border-style: solid;
  border-width: 5px;
  border-left-color: inherit;
}
.vue-slider-dot-tooltip-inner-right::after {
  right: 100%;
  top: 50%;
  transform: translate(0, -50%);
  height: 0;
  width: 0;
  border-color: transparent;
  border-style: solid;
  border-width: 5px;
  border-right-color: inherit;
}
.vue-slider-dot-tooltip-inner-top {
  transform-origin: 50% 100%;
}
.vue-slider-dot-tooltip-inner-bottom {
  transform-origin: 50% 0;
}
.vue-slider-dot-tooltip-inner-left {
  transform-origin: 100% 50%;
}
.vue-slider-dot-tooltip-inner-right {
  transform-origin: 0% 50%;
}

.vue-slider-dot:hover .vue-slider-dot-tooltip,
.vue-slider-dot-tooltip-show {
  opacity: 1;
  visibility: visible;
}
.vue-slider-dot:hover .vue-slider-dot-tooltip .vue-slider-dot-tooltip-inner,
.vue-slider-dot-tooltip-show .vue-slider-dot-tooltip-inner {
  transform: scale(1);
}
.vue-slider-dot {
  width: 20px !important;
  height: 20px !important;
}

/*# sourceMappingURL=antd.css.map */
</style>

<style scoped>
/* for admin site */
#risk-default.config-area {
  position: static;
  transform: unset;
  width: 100%;
  border: none;
  box-shadow: none;
  padding: 0;
  margin-bottom: 100px;
}
.slider-lines {
  width: 100%;
}
.slider-line-group {
  width: 5%;
}
.slider-line--big {
  height: 32px;
  width: 1px;
  background-color: #4b4b4b;
}
.slider-line--small {
  height: 32px;
  width: 1px;
  background-color: #cccccc;
}
</style>
