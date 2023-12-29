<template>
  <div class="ote-status-filter">
    <a-popover v-model="visible" placement="bottomLeft" trigger="click">
      <a
        :id="'legend-dropdown-' + id"
        @click="showAnimation()"
        href="javascript:void(0)"
        style="width: 152px; margin-right: 8px"
        class="dropdown_button-custom-mold"
      >
        <span :class="{ 'selected-dropdown-text': visible }">{{
          "Graph Settings"
        }}</span>
        <div>
          <!-- <a-icon
            class="caret"
            :class="{
              'selected-dropdown-text': visible,
              'caret-show': displayCaret,
            }"
            type="caret-down"
          /> -->
          <svg
            v-if="!displayCaret"
            xmlns="http://www.w3.org/2000/svg"
            width="10.414"
            height="5.707"
            viewBox="0 0 10.414 5.707"
          >
            <path
              id="Icon_feather-chevron-down"
              data-name="Icon feather-chevron-down"
              d="M9,13.5,13.5,18,18,13.5"
              transform="translate(-8.293 -12.793)"
              fill="none"
              stroke="#3491fb"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1"
            />
          </svg>
          <svg
            v-if="displayCaret"
            xmlns="http://www.w3.org/2000/svg"
            width="10.414"
            height="5.707"
            viewBox="0 0 10.414 5.707"
          >
            <path
              id="Icon_feather-chevron-down"
              data-name="Icon feather-chevron-down"
              d="M9,13.5,13.5,18,18,13.5"
              transform="translate(18.707 18.5) rotate(180)"
              fill="none"
              stroke="#3491fb"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1"
            />
          </svg>
        </div>
      </a>
      <div slot="content" style="padding: 8px 6px" class="dropdown-scroll">
        <div class="content-body">
          <template v-for="(legend, index) in legends">
            <a-col
              v-if="isEnable(legend)"
              style="padding: 7px"
              class="dropdown-legend"
              :key="index"
            >
              <!-- <p-check
                :checked="checked(legend)"
                @change="onChange($event, legend)"
                :class="{ 'checkbox-inactive': isDefault(legend) }"
              >
                <div
                  class="legend-item"
                  :id="legend.id"
                  v-html="legend.chartCode"
                ></div>
              </p-check> -->
              <p-check
                :checked="checked(legend)"
                @change="onChange($event, legend)"
              >
                <div
                  class="legend-item"
                  :id="legend.id"
                  v-html="legend.chartCode"
                ></div>
              </p-check>
            </a-col>
          </template>
        </div>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    requestParam: Object,
    id: String,
    updateLegend: Function,
  },
  data() {
    return {
      visible: false,
      statusOptions: {
        WORKING: "Active",
        IDLE: "Idle",
        NOT_WORKING: "Inactive",
        DISCONNECTED: "Disconnected",
      },
      selectedLegends: [],
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
      legends: [
        {
          id: "approved-ct",
          title: "Approved CT (sec)",
          default: ["CYCLE_TIME"],
          enable: [
            { type: "QUANTITY", clickable: false, orders: [1] },
            { type: "CYCLE_TIME", clickable: true, orders: [0] },
          ],
            '<div class="legend-icon" style="height: 1px; border-top: 3px  solid#4dbd74;"></div><div class="left-space">Approved CT (sec)</div>',
        },
        {
          id: "cycle-time",
          title: "Cycle Time (Within, L1, L2)",
          default: ["CYCLE_TIME"],
          enable: [
            { type: "QUANTITY", clickable: false, orders: [1] },
            { type: "CYCLE_TIME", clickable: true, orders: [3, 4, 5] },
          ],
          chartCode:

            '<div class="legend-icon bar" style="background-color:rgba(99, 194, 222, 0.5); border-color: #63c2de; margin: 4px 5px 0 0;"></div>\
            <div class="legend-icon bar" style="background-color:rgba(255, 193, 7, 0.5); border-color: #ffc107; margin: 4px 5px 0 0;"></div>\
            <div class="legend-icon bar" style="background-color:rgba(248, 108, 107, 0.5); border-color: #f86c6b; margin-top: 4px;"></div>\
            <div style="margin-top: 4px;" class="left-space">Cycle Time (Within, L1, L2)</div>',
        },
        {
          id: "uptime",
          title: "Uptime (Within, L1, L2)",
          default: ["UPTIME"],
          enable: [{ type: "UPTIME", clickable: true, orders: [1, 2, 3] }],
          chartCode:
              '<div class="legend-icon bar" style="background-color:rgba(99, 194, 222, 0.5); border-color: #63c2de; margin: 4px 5px 0 0;"></div>\
              <div class="legend-icon bar" style="background-color:rgba(255, 193, 7, 0.5); border-color: #ffc107; margin: 4px 5px 0 0;"></div>\
              <div class="legend-icon bar" style="background-color:rgba(248, 108, 107, 0.5); border-color: #f86c6b; margin-top: 4px;"></div>\
              <div style="margin-top: 4px;" class="left-space">Uptime (Within, L1, L2)</div>',
        },
        {
          id: "min-ct",
          title: "Min CT (sec)",
          default: ["CYCLE_TIME"],
          enable: [
            { type: "QUANTITY", clickable: false, orders: [1] },
            { type: "CYCLE_TIME", clickable: true, orders: [1] },
          ],
          chartCode:
              '<div class="legend-icon" style="height: 1px; border-top: 3px  solid#f86c6b;"></div><div class="left-space">Min CT (sec)</div>',
        },
        {
          id: "max-ct",
          title: "Max CT (sec)",
          default: ["CYCLE_TIME"],
          enable: [
            { type: "QUANTITY", clickable: false, orders: [1] },
            { type: "CYCLE_TIME", clickable: true, orders: [2] },
          ],
          chartCode:
              '<div class="legend-icon" style="height: 1px; border-top: 3px  solid#ffc107;"></div><div class="left-space">Max CT (sec)</div>',
        },
        {
          id: "uptime-targer",
          title: "Uptime Target",
          default: ["UPTIME"],
          enable: [{ type: "UPTIME", clickable: true, orders: [0] }],
          chartCode:
              '<div class="legend-icon" style="height: 1px; border-top: 2px  dashed#4dbd74;"></div><div class="left-space">Uptime Target</div>',
        },
        {
          id: "shot",
          title: "Shot",
          default: ["QUANTITY"],
          enable: [
            { type: "QUANTITY", clickable: true, orders: [0] },
            { type: "CYCLE_TIME", clickable: false, orders: [6] },
            { type: "UPTIME", clickable: false, orders: [4] },
          ],
          chartCode:
              '<div class="legend-icon" style="height: 1px; border-top: 3px  solid#63c2de;"></div><div class="left-space">Shot</div>',
        },
      ],
    };
  },
  methods: {
    initStatusDefaultValue() {
      this.selectedStatus = [this.requestParam.ops];
    },
    checked(legend) {
      return this.selectedLegends.includes(legend) || this.isDefault(legend);
    },
    isEnable(legend) {
      let isEnable = legend.enable.filter(
        (item) => item.type == this.requestParam.chartDataType
      );
      return isEnable.length > 0;
    },
    isDefault(legend) {
      return legend.default.includes(this.requestParam.chartDataType);
    },
    onChange(event, legend) {
      if (event) {
        this.selectedLegends.push(legend);
      } else {
        this.selectedLegends = this.selectedLegends.filter(
          (selected) => selected !== legend
        );
      }
      // this.filter(this.selectedStatus);
      let enableItem = legend.enable.filter(
        (item) => item.type == this.requestParam.chartDataType
      );
      if (enableItem[0].clickable) {
        this.updateLegend(legend, enableItem[0].orders);
      }
      this.displayCaret = true;
    },
    showAnimation() {
      const el = document.getElementById("legend-dropdown-" + this.id);
      if (!this.visible) {
        setTimeout(() => {
          el.classList.add("dropdown_button-animation-mold");
          this.animationTimeout = setTimeout(() => {
            el.classList.remove("dropdown_button-animation-mold");
            el.classList.add("selected-dropdown-text");
            el.classList.add("selected-dropdown-button");
          }, 1600);
        }, 1);
      } else {
        this.closeAnimation();
      }
    },
    closeAnimation() {
      const el = document.getElementById("legend-dropdown-" + this.id);
      if (this.animationTimeout != null) {
        console.log("Here");
        console.log(this.animationTimeout);
        clearTimeout(this.animationTimeout);
        this.animationTimeout = null;
      }
      if (this.caretTimeout != null) {
        clearTimeout(this.caretTimeout);
        this.caretTimeout = null;
      }
      el.classList.remove("dropdown_button-animation-mold");
      el.classList.remove("selected-dropdown-text");
      el.classList.remove("selected-dropdown-button");
      this.displayCaret = false;
    },
  },
  computed: {
    showTitle() {
      // return `${this.selectedStatus.length} Status`
      if (this.selectedStatus.length == 1) {
        if (this.visible) {
          return "1 Status";
        } else {
          // let result = this.statusOptions.filter(item => item)
          return this.statusOptions[`${this.selectedStatus[0]}`];
        }
      } else {
        return `${this.selectedStatus.length} Status`;
      }
    },
  },
  watch: {
    visible: function () {
      if (!this.visible) {
        this.closeAnimation();
      }
    },
    'requestParam.chartDataType': function(newValue){
      console.log('change type', newValue)
      this.selectedLegends = [];
    }
  },
  mounted() {
    this.initStatusDefaultValue();
  },
};
</script>
<style>
.ant-popover {
  /* transform: translate(0%, 4px) !important; */
  top: inherit;
  left: inherit;
  transform-origin: bottom right !important;
}
.legend-item {
  display: flex;
  align-items: center;
  margin-left: 20px;
}
.legend-icon {
  width: 40px;
}
.legend-icon.bar {
  height: 12px;
  border-width: 2px;
  border-style: solid;
}
.left-space {
  margin-left: -10px;
}
.checkbox-inactive {
  pointer-events: none;
}
.dropdown-legend .checkbox-inactive input:checked ~ .state label:after {
  background-color: #c4c4c4 !important;
}
.dropdown-legend .checkbox-inactive input:checked ~ .state label::before {
  border-color: #c4c4c4 !important;
}
.dropdown-legend .pretty .state label:before,
.dropdown-legend .pretty .state label:after {
  top: calc((0% - (100% - 13px)) + 50%) !important;
}
</style>
<style scoped>
[ant-click-animating-without-extra-node]:after {
  -webkit-animation: none !important;
  -moz-animation: none !important;
  -o-animation: none !important;
  -ms-animation: none !important;
  animation: none !important;
}
.dropdown_button-custom-mold {
  display: inline-flex;
  justify-content: space-between;
  align-items: center;
  color: #3491fb;
  transition: 0ms;
  line-height: 1.499;
  position: relative;
  font-weight: 400;
  white-space: nowrap;
  text-align: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
  height: 32px !important;
  padding: 0 8px 0px 10px;
  font-size: 14px;
  border-radius: 3px;
  border: 1px solid #ffffff;
  background-color: #ffffff;
  outline: 1px solid #3491fb;
  text-decoration: none !important;
}
.dropdown_button-custom-mold:hover {
  color: #0e65c7 !important;
  background-color: #deedff;
  outline: 1px solid transparent;
  border: 1px solid transparent;
}
.dropdown_button-animation-mold {
  animation: dropdown_button-mold-animation-primary 1.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  border: 1px solid transparent;
  outline: 1px solid #3491fb;
}
@keyframes dropdown_button-mold-animation-primary {
  0% {
  }
  15% {
    /*  box-shadow: 0 0 0 2px rgba(137, 209, 253, 0.5) !important;
            outline: 1px solid rgba(137, 209, 253, 0.5) !important; */
    color: #0e65c7;
    outline: 3px solid #89d1fd;
  }
  100% {
    color: #3491fb;
    outline: 1px solid #3491fb;
  }
}
.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: 100px;
  background: #f2f5fa;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  fill: #fff !important;
}
.down-icon {
  fill: #564efb;
}
.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}
.first-layer-wrapper {
  position: relative;
}
.first-layer {
  position: absolute;
  top: 0;
}
</style><template>
  <div class="ote-status-filter">
    <a-popover v-model="visible" placement="bottomLeft" trigger="click">
      <a
          :id="'legend-dropdown-' + id"
          @click="showAnimation()"
          href="javascript:void(0)"
          style="width: 152px; margin-right: 8px"
          class="dropdown_button-custom-mold"
      >
        <span :class="{ 'selected-dropdown-text': visible }">{{
            "Graph Settings"
          }}</span>
        <div>
          <!-- <a-icon
            class="caret"
            :class="{
              'selected-dropdown-text': visible,
              'caret-show': displayCaret,
            }"
            type="caret-down"
          /> -->
          <svg
              v-if="!displayCaret"
              xmlns="http://www.w3.org/2000/svg"
              width="10.414"
              height="5.707"
              viewBox="0 0 10.414 5.707"
          >
            <path
                id="Icon_feather-chevron-down"
                data-name="Icon feather-chevron-down"
                d="M9,13.5,13.5,18,18,13.5"
                transform="translate(-8.293 -12.793)"
                fill="none"
                stroke="#3491fb"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1"
            />
          </svg>
          <svg
              v-if="displayCaret"
              xmlns="http://www.w3.org/2000/svg"
              width="10.414"
              height="5.707"
              viewBox="0 0 10.414 5.707"
          >
            <path
                id="Icon_feather-chevron-down"
                data-name="Icon feather-chevron-down"
                d="M9,13.5,13.5,18,18,13.5"
                transform="translate(18.707 18.5) rotate(180)"
                fill="none"
                stroke="#3491fb"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1"
            />
          </svg>
        </div>
      </a>
      <div slot="content" style="padding: 8px 6px" class="dropdown-scroll">
        <div class="content-body">
          <template v-for="(legend, index) in legends">
            <a-col
                v-if="isEnable(legend)"
                style="padding: 7px"
                class="dropdown-legend"
                :key="index"
            >
              <!-- <p-check
                :checked="checked(legend)"
                @change="onChange($event, legend)"
                :class="{ 'checkbox-inactive': isDefault(legend) }"
              >
                <div
                  class="legend-item"
                  :id="legend.id"
                  v-html="legend.chartCode"
                ></div>
              </p-check> -->
              <p-check
                  :checked="checked(legend)"
                  @change="onChange($event, legend)"
              >
                <div
                    class="legend-item"
                    :id="legend.id"
                    v-html="legend.chartCode"
                ></div>
              </p-check>
            </a-col>
          </template>
        </div>
      </div>
    </a-popover>
  </div>
</template>

.down-icon {
  fill: #564efb;
}
.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}
.first-layer-wrapper {
  position: relative;
}
.first-layer {
  position: absolute;
  top: 0;
}
</style>
