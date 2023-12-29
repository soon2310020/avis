<template>
  <a-popover v-model="visible" placement="bottom" trigger="click">
    <a
      id="data-filter-btn"
      href="javascript:void(0)"
      style="margin-right: 5px"
      class="dropdown_button"
      @click="showAnimation"
      @mouseover="isHover = true"
      @mouseleave="isHover = false"
    >
      {{ titleShow() }}
      <!-- <a-icon type="caret-down" /> -->
      <span v-if="isHover && !visible">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#3491ff"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="feather feather-chevron-down"
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </span>
      <span v-else-if="visible">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#3491ff"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="feather feather-chevron-up"
        >
          <polyline points="18 15 12 9 6 15"></polyline>
        </svg>
      </span>
      <span v-else>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="feather feather-chevron-down"
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </span>
    </a>
    <div
      slot="content"
      style="
        padding: 10px 5px 10px 10px !important;
        max-height: 300px;
        overflow-y: scroll;
        margin-right: 1px;
      "
      class="dropdown-scroll"
    >
      <!-- 검색창 제거 요청 -->
      <!-- <a-input
        style="margin-bottom: 10px"
        placeholder="Input search text"
        v-model="input"
      >
        <a-icon slot="prefix" type="search" />
      </a-input> -->
      <a-col style="padding: 7px" class="item-zone" v-if="showAllType">
        <p-check
          :checked="checkedAll()"
          @change="onChange($event, allItem)"
          color="#5756f7"
        >
          <div class="p-radio-icon-wrapper">
            <span> </span>
            <span style="margin-left: 0">{{ allType }}</span>
          </div>
        </p-check>
      </a-col>
      <template v-for="item in filtered">
        <a-col style="padding: 7px" class="item-zone" :key="item.code">
          <p-check v-if="item.default" :checked="true" :disabled="true">
            <div
              :class="{
                'three-icon':
                  item.icon === '/images/graph/three-bar.svg' ||
                  item.icon === '/images/graph/graph-setting-line.svg',
              }"
              class="p-radio-icon-wrapper"
            >
              <img v-if="item.icon" :src="item.icon" />
              <span>{{ item.name }}</span>
            </div>
          </p-check>
          <p-check
            v-if="item.default !== undefined && item.default === false"
            :checked="false"
            :disabled="true"
          >
            <div
              :class="{
                'three-icon':
                  item.icon === '/images/graph/three-bar.svg' ||
                  item.icon === '/images/graph/graph-setting-line.svg',
              }"
              class="p-radio-icon-wrapper"
            >
              <img v-if="item.icon" :src="item.icon" />
              <span>{{ item.name }}</span>
            </div>
          </p-check>
          <p-check
            v-if="item.default === undefined"
            :checked="checked(item)"
            @change="onChange($event, item)"
          >
            <div
              :class="{
                'three-icon':
                  item.icon === '/images/graph/three-bar.svg' ||
                  item.icon === '/images/graph/graph-setting-line.svg',
              }"
              class="p-radio-icon-wrapper"
            >
              <img v-if="item.icon" :src="item.icon" />
              <span>{{ item.name }}</span>
            </div>
          </p-check>
        </a-col>
      </template>
    </div>
  </a-popover>
</template>

<script>
module.exports = {
  props: {
    allType: String,
    optionArray: Array,
    type: String,
    query: Function,
    findMap: Function,
    title: String,
    selectedArray: Array,
    titlePlaceholder: String,
    resources: Object,
  },
  data() {
    return {
      options: this.optionArray,
      allItem: {
        code: null,
        title: this.allType,
      },
      checkedList: [],
      oldCheckedList: [],
      input: "",
      visible: false,
      isHover: false,
      chartTypeConfig: {},
      tracker: 0,
    };
  },
  methods: {
    updateConfig() {
      this.$emit("update-config", this.oldCheckedList);
    },
    showAnimation() {
      const el = document.getElementById("data-filter-btn");
      if (!this.visible) {
        setTimeout(() => {
          el.classList.add("dropdown_button-animation-mold");
          this.animationTimeout = setTimeout(() => {
            this.displayCaret = true;
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
      const el = document.getElementById("data-filter-btn");
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
    // getChartTypeConfig () {
    //   axios.get('/api/config/chart-type-config').then((res) => {
    //     this.chartTypeConfig = res.data.data
    //   })
    // },
    refreshGraphSettings: function (options, selected) {
      this.optionaArray = options;
      this.selectedArray = selected;
    },
    titleShow: function () {
      if (this.checkedList.length == this.optionArray.length) {
        return this.titlePlaceholder;
      } else {
        if (this.checkedList.length < 2) {
          // return `${this.checkedList.length} ${String(
          //   this.title
          // ).toLowerCase()}`;
          return `${String(this.title)}`;
          // 2022.02.22 (그래프 숫자 표시 제거 및 워딩 수정(graph settings => Graph Settings))
        } else {
          if (this.title === "region") {
            if (this.checkedList.length <= 1) {
              return `${this.checkedList.length} region`;
            } else {
              return `${this.checkedList.length} regions`;
            }
          }
          if (this.type === "company") {
            if (this.checkedList.length <= 1) {
              return `${this.checkedList.length} company`;
            } else {
              return `${this.checkedList.length} companies`;
            }
          }
          if (this.type === "category") {
            if (this.checkedList.length <= 1) {
              return `${this.checkedList.length} category`;
            } else {
              return `${this.checkedList.length} categories`;
            }
          }
          // return `${this.checkedList.length} ${this.title}`;
          return this.title;
          // 2022.02.22 (그래프 숫자 표시 제거 및 워딩 수정(graph settings => Graph Settings))
        }
      }
    },

    checked: function (item) {
      if (this.checkedList.length === 0) return false;
      const findIndex = this.checkedList.findIndex(
        (value) => value.code == item.code
      );
      return findIndex !== -1;
    },

    checkedAll: function () {
      return (
        this.optionArray.length > 0 &&
        this.checkedList.length === this.optionArray.length
      );
    },

    onMouseleave: function () {
      console.log("this.checkedList - ", this.checkedList, this.oldCheckedList);
      const objectsEqual = (o1, o2) =>
        typeof o1 === "object" && Object.keys(o1).length > 0
          ? Object.keys(o1).length === Object.keys(o2).length &&
            Object.keys(o1).every((p) => objectsEqual(o1[p], o2[p]))
          : o1 === o2;
      if (objectsEqual(this.checkedList, this.oldCheckedList.data)) {
        return;
      } else {
        this.oldCheckedList = { type: this.type, data: this.checkedList };
        // let items = this.checkedList.map((value) => value.code);
        console.log("operatingstatus data", this.oldCheckedList);
        this.$emit("operatingstatus", this.oldCheckedList);
        this.updateConfig();
      }
    },

    onChange: function (isChecked, item) {
      if (isChecked) {
        if (this.type == "category") {
          if (item.id == null) {
            return (this.checkedList = this.options);
          }
          this.checkedList = this.checkedList.concat(item);
        } else {
          if (item.code == null) {
            return (this.checkedList = this.options);
          }
          this.checkedList = this.checkedList.concat(item);
        }
      } else {
        if (this.type == "category") {
          if (
            item.id == null &&
            this.checkedList.length == this.options.length
          ) {
            this.checkedList = this.options.filter((g) => g.default);
            return this.checkedList;
          }
          this.checkedList = this.checkedList.filter(
            (value) => value.id !== item.id
          );
        } else {
          if (
            item.code == null &&
            this.checkedList.length == this.options.length
          ) {
            this.checkedList = this.options.filter((g) => g.default);
            return this.checkedList;
          }
          this.checkedList = this.checkedList.filter(
            (value) => value.code !== item.code
          );
          console.log(this.checkedList, "this.checkedList");
        }
      }
    },
  },

  computed: {
    filtered() {
      console.log("filtered", this.input);
      const str = this.input.trim();
      if (!str || str == "") {
        return this.optionArray;
      }
      if (this.optionArray != null) {
        if (this.type == "category") {
          return this.optionArray
            .filter(function (e) {
              return (
                e.name != null &&
                e.name.toUpperCase().includes(str.toUpperCase())
              );
            })
            .sort((firstItem, secondItem) =>
              firstItem.title.toUpperCase() > secondItem.title.toUpperCase()
                ? 1
                : -1
            );
        } else {
          return this.optionArray
            .filter(function (e) {
              return (
                e.title != null &&
                e.title.toUpperCase().includes(str.toUpperCase())
              );
            })
            .sort((firstItem, secondItem) =>
              firstItem.title.toUpperCase() > secondItem.title.toUpperCase()
                ? 1
                : -1
            );
        }
      }
      return this.optionArray;
    },
    showAllType() {
      const str = this.input;
      console.log("input", str);
      if (
        str == null ||
        str.trim() == "" ||
        (this.allType != null &&
          this.allType.toUpperCase().includes(str.toUpperCase()))
      ) {
        return true;
      }
      return false;
    },
  },
  watch: {
    optionArray(newValue, oldValue) {
      console.log(newValue, oldValue, "oldValue");
      this.optionArray = newValue;
      this.options = this.optionArray;
    },
    selectedArray(newValue, oldValue) {
      this.selectedArray = newValue;
      console.log("dataFilter.selectedArray: ", this.selectedArray);
      // this.checkedList = this.selectedArray;
    },
    visible(newValue, oldValue) {
      // this.getChartTypeConfig();
      if (newValue) {
        this.checkedList = JSON.parse(JSON.stringify(this.optionArray)).filter(
          (item) => item.selected
        );
        this.oldCheckedList = { type: this.type, data: this.checkedList };
      }
      this.tracker++;
      console.log(this.checkedList, this.optionArray, "checkedListtttttttt");
      if (newValue == false && oldValue == true) {
        this.onMouseleave();
      }
      if (!this.visible) {
        this.closeAnimation();
      }
    },
  },
  mounted() {},
};
</script>
<style>
.item-zone .pretty span {
  margin-left: -15px;
}
.item-zone .pretty .state label {
  overflow: unset;
}
.item-zone .pretty .state label:after,
.item-zone .pretty .state label:before {
  top: 0 !important;
}
.item-zone .p-radio-icon-wrapper {
  margin-left: 20px;
  margin-top: 0px;
  display: flex;
  align-items: center;
}
.item-zone .p-radio-icon-wrapper img {
  max-width: 30px;
}
.item-zone .three-icon img {
  /* max-width: 40px!important; */
  max-width: 90px;
}
p-check img {
  max-width: 100%;
  height: auto;
}
.dropdown-scroll .a-col {
  display: flex;
  align-items: center;
}
.ant-popover-inner-content {
  /* width: 250px; */
  padding: 0px !important;
  margin-right: 0;
}
.pretty .state label:before {
  border: 1px solid #909090;
}
.pretty.p-default .state label {
  opacity: 1 !important;
}
.pretty.p-default input[disabled]:checked ~ .state label:after {
  background-color: #909090 !important;
}
.pretty.p-default input:checked ~ .state label:after {
  background-color: #0075ff !important;
}
.pretty.p-default input[disabled]:checked ~ .state label:before {
  border: 1px solid #909090 !important;
}
.pretty.p-default input:checked ~ .state label:before {
  border: 1px solid #0075ff !important;
}
.pretty input[disabled] ~ * {
  opacity: 1 !important;
}
.pretty input[disabled]:not(:checked) {
  opacity: 0.5 !important;
}
.item-zone:hover .pretty .state label:before {
  border: 1px solid #4b4b4b;
}
.item-zone:hover .pretty .state label:after {
}
.pretty .state label {
  white-space: nowrap;
  text-overflow: ellipsis;
  /* max-width: 230px; */
  overflow: hidden;
  /* 2022.02.22 display: block 추가 (align 수정용) */
  display: block;
}
</style>
<style scoped>
.dropdown_button {
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

.dropdown_button:hover {
  color: #0e65c7 !important;
  background-color: #deedff;
  outline: 1px solid transparent;
  border: 1px solid transparent;
}
.clicked-button {
  color: #3491ff !important;
  background-color: #ffffff;
  outline: 2px solid #98d1fd;
  border: 1px solid transparent;
}
.selected {
  border: 1px solid transparent;
  color: #707070 !important;
  background-color: #ffffff;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  color: black !important;
  fill: black !important;
  /* background-image: linear-gradient(to right, #414ff7, #6a4efb) !important; */
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
  /* background-image: linear-gradient(#414ff7, #6a4efb) !important; */
  background-color: red;
}
.first-layer-wrapper {
  position: relative;
}
.first-layer {
  position: absolute;
  top: 0;
}
.ant-input-affix-wrapper .ant-input:not(:first-child) {
  border-radius: 0px;
  border: unset;
}
.ant-input {
  height: 25px !important;
  font-size: 13px !important;
}
::-webkit-scrollbar {
  width: 3px !important;
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
</style>
