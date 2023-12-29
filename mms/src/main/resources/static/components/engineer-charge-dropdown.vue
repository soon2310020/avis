<template>
  <div>
    <a
      href="javascript:void(0)"
      ref="custom-dropdown-button"
      :disabled="disabled"
      @click="handleClick()"
      class="custom-dropdown-button selected-item-title"
      :class="{ 'hover-btn': !showDropdown, 'disabled-btn': disabled }"
    >
      <template v-if="checkedList.length > 0">
        <div class="w-100">
          <div class="item" v-for="item in checkedList" :key="item.id">
            {{ item.name }}
          </div>
        </div>
      </template>
      <template v-else>
        <div class="w-100">
          <div class="selected-item-title" style="display: inline">
            {{ placeholder }}
          </div>
        </div>
      </template>
      <div
        class="dropdown-carret d-flex justify-content-end float-right"
        :class="{ rotate: showDropdown }"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="13"
          height="6"
          viewBox="0 0 13 6"
        >
          <path
            id="Polygon_1"
            data-name="Polygon 1"
            d="M5.822.626a1,1,0,0,1,1.357,0l3.942,3.639A1,1,0,0,1,10.442,6H2.558a1,1,0,0,1-.678-1.735Z"
            transform="translate(13 6) rotate(180)"
            fill="#909090"
          />
        </svg>
      </div>
    </a>

    <common-select-popover :is-visible="showDropdown" @close="closeDropdown">
      <div class="dropdown-wrap" :style="styleProps">
        <div v-if="items.length >= 5">
          <common-searchbox
            placeholder="Input search text"
            :request-param="requestParam"
          ></common-searchbox>

          <div class="focus-border"></div>
        </div>

        <ul class="dropdown-list">
          <li v-if="filtered?.length >= 5" class="all-select-list">
            <div :disabled="!selectAllEnabled" @click="selectAll()">
              Select All
            </div>
            <div :disabled="!unselectAllEnabled" @click="unselectAll()">
              Unselect All
            </div>
          </li>
          <template v-for="(item, index) in filtered">
            <li :key="index" v-if="item">
              <label
                :for="`dropdown-input-${index}-${id}`"
                :disabled="item?.disabled"
              >
                <input
                  :id="`dropdown-input-${index}-${id}`"
                  type="checkbox"
                  :value="JSON.stringify(item)"
                  :checked="item.checked"
                  @input="handleChange($event)"
                />
                <div class="checkbox-custom"></div>
                {{ truncateText(item?.name, 40) }}
              </label>
            </li>
          </template>
        </ul>
      </div>
    </common-select-popover>
  </div>
</template>

<script>
module.exports = {
  components: {
    "common-searchbox": httpVueLoader(
      "/components/common/dc-component/custom-searchbox.vue"
    ),
  },
  props: {
    resources: Object,
    placeholder: String,
    items: Array,
    value: Array,
    disabled: Boolean,
    styleProps: String,
    id: String,
  },
  data() {
    return {
      showDropdown: false,
      buttonAnimation: null,
      requestParam: {
        query: "",
      },
      cloneItems: [],
      checkedList: [],
    };
  },
  computed: {
    filtered() {
      console.log("trigger filter");
      const str = this.requestParam.query.trim();
      if (!str || str == "") {
        return this.cloneItems.slice();
      }
      if (this.cloneItems != null) {
        let filter = this.cloneItems.filter((item) => {
          return (
            item.title != null &&
            item.title.toUpperCase().includes(str.toUpperCase())
          );
        });
        let result = filter.sort((firstItem, secondItem) => {
          return firstItem.title.toUpperCase() > secondItem.title.toUpperCase()
            ? 1
            : -1;
        });
        return result;
      }
      return this.cloneItems.slice();
    },
    selectAllEnabled() {
      console.log(
        "@computed:selectAllEnabled",
        this.filtered.some((i) => !i.checked)
      );
      return this.filtered.some((i) => !i.checked);
    },
    unselectAllEnabled() {
      console.log(
        "@computed:unselectAllEnabled",
        this.filtered.some((i) => i.checked)
      );
      return this.filtered.some((i) => i.checked);
    },
  },
  watch: {
    items: {
      handler(newVal) {
        const __arr = JSON.parse(JSON.stringify(newVal));
        __arr.forEach((i) => {
          if ((this.value || []).some((id) => i.id === id)) {
            i.checked = true;
          }
        });
        this.cloneItems = __arr;
        this.checkedList = this.cloneItems.filter((i) => i.checked);
      },
      immediate: true,
    },
    value: {
      handler(newVal) {
        const __arr = JSON.parse(JSON.stringify(this.items));
        __arr.forEach((i) => {
          if (newVal.some((id) => i.id === id)) {
            i.checked = true;
          }
        });
        this.cloneItems = __arr;
        this.checkedList = this.cloneItems.filter((i) => i.checked);
      },
      immediate: true,
    },
    showDropdown(newVal) {
      if (!newVal) {
        // should handle change after close
        this.checkedList = this.cloneItems.filter((item) => item.checked);
        this.$emit(
          "input",
          this.checkedList.map((i) => i.id)
        );
      }
    },
  },
  methods: {
    closeDropdown() {
      this.requestParam.query = "";
      const el = this.$refs["custom-dropdown-button"];
      if (this.buttonAnimation != null) {
        clearTimeout(this.buttonAnimation);
        this.buttonAnimation = null;
      }
      el.classList.remove("custom-dropdown-button-animation");
      this.showDropdown = false;
    },
    handleClick() {
      const el = this.$refs["custom-dropdown-button"];
      if (!this.showDropdown) {
        setTimeout(() => {
          el.classList.add("custom-dropdown-button-animation");
          this.buttonAnimation = setTimeout(() => {
            el.classList.remove("custom-dropdown-button-animation");
            this.showDropdown = true;
          }, 350);
        }, 0);
      } else {
        this.closeDropdown();
      }
    },
    truncateText(text, length) {
      return text.length > length ? text.substring(0, length) + "..." : text;
    },
    handleChange(event) {
      const checked = event.target.checked;
      const value = JSON.parse(event.target.value);
      const findIndex = this.cloneItems.findIndex((i) => i.id === value.id);
      if (findIndex > -1) {
        this.$set(this.cloneItems, findIndex, {
          ...this.cloneItems[findIndex],
          checked,
        });
      }
    },
    selectAll() {
      const __arr = [...this.cloneItems];
      this.cloneItems = __arr.map((i) => ({
        ...i,
        checked: this.filtered.some((j) => i.id === j.id) ? true : i.checked,
      }));
    },
    unselectAll() {
      const __arr = [...this.cloneItems];
      this.cloneItems = __arr.map((i) => ({
        ...i,
        checked: this.filtered.some((j) => i.id === j.id) ? false : i.checked,
      }));
    },
  },
};
</script>

<style>
.ant-popover-inner-content,
.custom-style {
  padding: 0px !important;
  border: none !important;
}
</style>
<style scoped>
.rotate {
  transform: rotate(180deg);
}
.custom-dropdown-button {
  width: 100%;
  height: fit-content;
  padding: 6px 8px;
  border: 0.5px solid #909090;
  border-radius: 1.7px;
  background-color: #ffffff;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  text-decoration: none !important;
  align-content: flex-start;
  justify-content: flex-start;
}

.custom-dropdown-button.hover-btn:hover {
  border: 0.5px solid #4b4b4b;
  border-radius: 1.7px;
}

.selected-item-title {
  color: #4b4b4b;
}
.custom-dropdown-button-animation {
  animation: custom-dropdown-button-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  border: 1px solid transparents;
}
.rotate {
  transform: rotate(180deg);
}
.disabled-btn {
  background: #e4e7ea;
  color: #ffffff;
  cursor: no-drop !important;
}

.customClass {
  visibility: hidden;
}
.button-select-options {
  margin-right: 10px;
  width: 100%;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
  min-height: 35px;
  line-height: 20px;
  height: auto;
}

.down-icon {
  fill: #564efb;
}

.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}

.item {
  padding: 3px 5px;
  background: #fafafa;
  border: 0.5px solid #e8e8e8;
  display: inline;
  font-size: 12px;
  margin-right: 5px;
  align-content: flex-start;
}
.ant-btn:focus {
  border: 2px solid #deedff;
}
.dropdown-carret {
  display: flex;
  align-items: center;
  margin-left: 4px;
  transition-property: transform;
  transition-duration: 0.3s;
  transition-timing-function: linear;
}
.button-title {
  line-height: 17px;
}
ul,
li {
  list-style: none;
  margin: 0;
  padding: 0;
}
.dropdown-wrap {
  width: 511px;
  max-height: 234px;
  /*padding: 8px 6px;*/
  padding: 2px 2px;
  box-shadow: 1px 1px 5px rgba(0, 0, 0, 0.1);
  background: white;
  position: static;
  border-radius: 3px;
  z-index: 999;
  top: 44px;
  left: 16px;
}

.search-box {
  width: 100% !important;
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
.dropdown-searchbox input:focus + .focus-border {
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

.dropdown-list {
  overflow-y: auto;
  max-height: 180px;
}

.dropdown-list li {
  /*margin: 4px 0px;*/
}
.dropdown-list li label {
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  font-size: 14.66px;
  font-weight: 400px;
  line-height: 17px;
  height: 32px;
  padding: 0px 8px;
}
.list-hover-dropdown:hover {
  background: #e6f7ff;
}
.dropdown-list li label input {
  display: none;
}
.dropdown-list li label input:checked + .checkbox-custom {
  border-color: #0075ff;
}
.dropdown-list li label input:checked + .checkbox-custom::before {
  content: "";
  width: 9px;
  height: 9px;
  background-color: #0075ff;
}
.dropdown-list li label:hover > .checkbox-custom {
  border: 1px solid #3585e5 !important;
}
.dropdown-list li label .checkbox-custom {
  width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
label {
  margin-bottom: 2px;
}

.dropdown-list li label[disabled] {
  color: #c4c4c4;
  pointer-events: none;
}

.dropdown-list li label[disabled] input:checked + .checkbox-custom {
  border-color: #c4c4c4;
}
.dropdown-list li label[disabled] input:checked + .checkbox-custom::before {
  /* content: '';
  width: 9px;
  height: 9px; */
  background-color: #c4c4c4;
}

.all-select-list {
  display: flex;
  align-items: center;
}
.all-select-list > div:first-of-type {
  margin-right: 15px;
}
.all-select-list > div {
  color: #4f8ff7;
  font-size: 14px;
  font-weight: 400px;
  padding: 5px;
  cursor: pointer;
  min-width: 90px;
}
.all-select-list > [disabled] {
  color: #c4c4c4;
  pointer-events: none;
}

.info-text {
  font: normal normal normal 12px/16px Helvetica Neue;
  letter-spacing: 0px;
  color: #afafaf;
  padding-bottom: 7px;
}
.float-right {
  float: right !important;
  flex: auto;
}
</style>
