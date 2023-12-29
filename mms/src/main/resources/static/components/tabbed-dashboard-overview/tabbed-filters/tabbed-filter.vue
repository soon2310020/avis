<template>
  <a-popover v-model="visible" placement="bottomLeft" trigger="click">
    <a @click="animationOutline()" :class="{ 'animation-secondary': showAnimation }" href="javascript:void(0)" id="dropdown-dashboard" class="btn-custom btn-outline-custom-primary dropdown-dashboard-filter">
      <span class="title-show">{{ titleShow() }}</span>
      <div>
        <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{ 'caret-show': displayCaret }" alt="" />
      </div>
    </a>
    <div slot="content" style="
        padding: 8px 6px;
        max-height: 300px;
        overflow-y: scroll;
        min-width: 230px;
        border: 1px solid #c1c7cd;
      " class="dropdown-scroll">
      <div v-if="hasSearchbox" class="dropdown-searchbox">
        <div class="search-icon">
          <img src="/images/search-icon.svg" />
        </div>
        <input v-model.trim="input" :placeholder="placeholder" />
        <div class="focus-border"></div>
      </div>
      <div class="all-select-list">
        <div v-if="optionArray?.length >= 5" @click="checkedAll()"> Select All </div>
        <div v-if="optionArray?.length >= 5" @click="unselectAll()"> Unselect All </div>
      </div>
      <a-col v-for="item in filtered" class="tabbed-check" style="padding: 7px" :key="item.index">
        <p-check :checked="checked(item)" @change="onChange($event, item)"> {{ item.name }} </p-check>
      </a-col>
    </div>
  </a-popover>
</template>

<script>
module.exports = {
  props: {
    allType: String,
    optionArray: {
      type: Array, default: () => []
    },
    type: String,
    query: Function,
    title: String,
    selectedData: {
      type: Array,
      default: () => [],
    },
    selectedAll: {
      type: Boolean,
      default: true,
    },
    allLength: Number,
    placeholder: String
  },
  data() {
    return {
      allItem: {
        id: null,
        name: this.allType,
      },
      checkedList: [],
      oldCheckedList: [],
      input: "",
      visible: false,
      displayCaret: false,
      showAnimation: false
    };
  },
  methods: {
    animationOutline() {
      this.displayCaret = true;
      if (!this.visible) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700)
      }
    },
    titleShow() {
      if (this.checkedList.length === this.optionArray.length) {
        return this.allType;
      } else {
        if (this.checkedList.length < 2) {
          return `${this.checkedList.length} ${this.title}`;
        } else {
          if (this.title === "Category") {
            return `${this.checkedList.length} Categories`;
          }
          if (this.title === 'Company') {
            return `${this.checkedList.length} Companies`;
          }
          if (this.title === 'Priority') {
            return `${this.checkedList.length} Priorities`;
          }
          return `${this.checkedList.length} ${this.title}s`;
        }
      }
    },

    checked: function (item) {
      const findIndex = this.checkedList.findIndex(
        (value) => value.id === item.id
      );
      return findIndex !== -1;
    },
    checkedAll() {
      this.checkedList = [...this.optionArray]
      console.log('@checkedAll:this.checkedList', this.checkedList)
    },
    unselectAll() {
      console.log('unselect all')
      this.checkedList = []
      console.log('@unselectAll:this.checkedList', this.checkedList)
    },

    onMouseleave: function () {
      if (this.checkedList.map((value) => value.id).toString() === this.oldCheckedList.map((value) => value.id).toString()) return
      this.oldCheckedList = this.checkedList;
      this.query(
        this.type,
        this.checkedList.map((value) => value.id),
        this.checkedList.length === this.optionArray.length
      );
    },

    onChange: function (isChecked, item) {
      if (isChecked) {
        this.checkedList = this.checkedList.concat(item);
      } else {
        this.checkedList = this.checkedList.filter(
          (value) => value.id !== item.id
        );
      }
    },
  },

  computed: {
    filtered() {
      const str = this.input;
      if (!str) {
        return [...this.optionArray];
      }
      var options = {
        keys: ["name"],
      };
      var searcher = new Fuse(this.optionArray, options);
      return searcher.search(str);
    },
    hasSearchbox() {
      return this.optionArray?.length >= 4
    }
  },
  watch: {
    optionArray: function (newValue, oldValue) {
      if (Array.isArray(this.selectedData)) {
        const data = this.optionArray.filter(o => this.selectedData.includes(o.id))
        this.checkedList = data;
        this.oldCheckedList = data;
      }
    },
    visible: function (newValue, oldValue) {
      if (!newValue) {
        this.onMouseleave();
        this.displayCaret = false;
      }
    },
    selectedData(newVal) {
      if (Array.isArray(newVal)) {
        const data = this.optionArray.filter(o => this.selectedData.includes(o.id))
        this.checkedList = data;
        this.oldCheckedList = data;
        console.log('@change:selectedData', newVal)
      }
    },
  },
  mounted() {
    this.checkedList = this.selectedData;
    this.oldCheckedList = this.selectedData;
    console.log('mounted', { checkedList: this.checkedList, oldCheckedList: this.oldCheckedList })
  },
};
</script>
<style>
.all-select-list {
  display: flex;
  align-items: center;
}

.all-select-list>div:first-of-type {
  margin-right: 8px;
}

.all-select-list>div {
  color: #4f8ff7;
  font-size: 12px;
  padding: 5px;
  cursor: pointer;
}

.all-select-list>[disabled] {
  color: #c4c4c4;
  pointer-events: none;
}

.dropdown-searchbox {
  height: 32px;
  border: 1px solid #d6dade;
  border-radius: 3px;
  display: flex;
  position: relative;
}

.dropdown-searchbox .search-icon {
  position: absolute;
  z-index: 1;
  width: 28px;
  height: 100%;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
}

.dropdown-searchbox .search-icon img {
  width: 16.42px;
  height: 16.42px;
}

.dropdown-searchbox input {
  position: absolute;
  z-index: 2;
  width: 100%;
  height: 100%;
  padding-left: 35px;
  box-sizing: border-box;
  background-color: inherit;
  border: solid 1px transparent;
  border-radius: 2px;
  font-size: 14px;
}

.dropdown-searchbox input::placeholder {
  color: #9d9d9d;
}

.dropdown-searchbox input:hover {
  border-color: #4b83de;
}

.dropdown-searchbox input:focus {
  outline: solid 1px #3491ff;
}

.dropdown-searchbox input:focus+.focus-border {
  width: 100%;
  height: 100%;
  position: absolute;
  z-index: -1;
  box-sizing: content-box;
  opacity: 0.2;
  border: 4px solid #579ffb;
  border-radius: 5px;
  top: -4px;
  left: -4px;
}

.pretty .state label:before {
  border: 1px solid #909090;
}

.tabbed-check .pretty .state {
  padding-left: 10px;
}

.pretty.p-default .state label {
  opacity: 1 !important;
}

.pretty.p-default input[disabled]:checked~.state label:after {
  background-color: #909090 !important;
}

.pretty.p-default input:checked~.state label:after {
  background-color: #0075ff !important;
}

.pretty.p-default input[disabled]:checked~.state label:before {
  border: 1px solid #909090 !important;
}

.pretty.p-default input:checked~.state label:before {
  border: 1px solid #0075ff !important;
}

.pretty input[disabled]~* {
  opacity: 1 !important;
}

.pretty input[disabled]:not(:checked) {
  opacity: 0.5 !important;
}
</style>
<style scoped>
.ant-dropdown-trigger1 {
  /*border-radius: 0px;*/
  /*border: none;*/
  /*padding: 5px 0px !important;*/
  margin-right: 20px;
  /*border-radius: 7px;*/
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  /*border: none;*/
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d1d1d1;
  border-radius: 2px;
  box-shadow: unset !important;
  padding: 5px !important;
}

.ant-dropdown-trigger1:last-child {
  /*border-radius: 0px;*/
  /*border: none;*/
  /*padding: 5px 0px !important;*/
  margin-right: 0px;
  /*border-radius: 7px;*/
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  /*border: none;*/
  align-items: center;
  justify-content: space-between;
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  color: #fff !important;
  fill: #fff !important;
  background-image: linear-gradient(to right, #414ff7, #6a4efb) !important;
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus>svg {
  fill: #fff !important;
}

.down-icon {
  fill: #564efb;
}

.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}
</style>
