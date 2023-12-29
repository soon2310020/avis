<!--
이 컴포넌트는 공통 컴포넌트 이므로, 수정했을 경우, 이 컴포넌트를 사용한 모든 곳에 영향을 끼칠 수 있습니다.
그렇기 때문에 이 컴포넌트의 수정은 금지하며, 수정이 필요할 경우 woojin에게 요청해주시기 바랍니다.
props에 설명된 주석을 읽고 활용해주세요.

This component is a common component, so if you modify it, it can affect everywhere you use this component.
Therefore, modification of this component is prohibited, and if you need to modify it, please ask woojin.
Please read and use the comments described in props.
- emoldino woojin
-->

<template>
  <div class="dropdown-wrap" :style="styleProps">
    <div v-if="searchbox || items?.length >= 5" class="dropdown-searchbox">
      <div class="search-icon">
        <img src="/images/search-icon.svg" />
      </div>
      <input v-model="inputContent" :placeholder="placeholder || 'Search Code Details'" />
      <div class="focus-border"></div>
    </div>

    <ul class="dropdown-list">
      <li v-if="checkbox && filtered?.length >= 5 && allCheckbox" class="all-select-list">
        <div :disabled="checkedArray?.length === filtered?.length" @click="selectAll()">Select All</div>
        <div :disabled="checkedArray?.length === 0" @click="unselectAll()">Unselect All</div>
      </li>
      <li v-if="infoText" class="info-text">{{ infoText }}</li>
      <template v-for="(item, index) in filtered">
        <li :key="index" v-if="item">
          <!-- <label :for="`dropdown-input-${index}-${id}`" @click="optionClick(item)" :disabled="disabledHandler(item)"> -->
          <label :for="`dropdown-input-${index}-${id}`" @click="optionClick(item)" :disabled="item?.disabled" :class="{'list-hover-dropdown' : !checkbox}">
            <template v-if="checkbox">
              <input
                  :id="`dropdown-input-${index}-${id}`"
                  type="checkbox"
                  :value="JSON.stringify(item)"
                  v-model="item.checked"
                  @input="changeHandler($event, index)"
              />
              <!-- <input
                  :id="`dropdown-input-${index}-${id}`"
                  type="checkbox"
                  :value="JSON.stringify(item)"
                  @input="changeHandler($event, index)"
              /> -->
              <div class="checkbox-custom"></div>
            </template>
            <div v-if="item?.image">
              <img :src="item?.image" alt="" />
            </div>
            {{ item?.title }}
          </label>
        </li>
      </template>
    </ul>
  </div>
</template>

<script>
module.exports = {
  name: 'Dropdown',
  props: {
    // disabledHandler: {
    //     type: Function,
    //     default: () => false,
    // },
    items: Array, // option list (title: name to be displayed in option list, image: option image) (ex: {title: 'option1', [image: imageUrl, ...]})
    setResult: {
      // Save the final result. (When using checkboxes)
      type: Function,
      default: () => 1,
    },
    clickHandler: Function, // Click handler (if using list click without using checkbox)
    checkbox: Boolean, // // Set whether checkbox or not
    allCheckbox: {
      default: true,
      type: Boolean,
    },
    searchbox: Boolean, // // Set whether search-box or not
    styleProps: String, // Set whether dropdown-list wrap-style or not (ex: {top: '10px', left: '10px', width: '10px'})
    placeholder: String, // Set whether placeholder or not
    infoText: {
      default: '',
      Type: String,
    },
    id: String, // Set whether checkbox-id or not
  },
  data() {
    return {
      inputContent: '', // The input value. Shows the input list searched through filtered.
    };
  },
  watch: {
    checkedArray(newArray) {
      this.setResult(newArray);
      console.log('checkedArray newArray: ', newArray);
    },
    filtered(newArray) {
      console.log('filtered newArray: ', newArray);
    },
  },
  computed: {
    // Filter your search terms
    filtered: {
      get: function () {
        if (this.items) {
          const str = this.inputContent.trim();
          if (!str || str == '') {
            return this.items;
          }
          if (this.items != null) {
            let filter = this.items.filter((item) => {
              return item.title != null && item.title.toUpperCase().includes(str.toUpperCase());
            });
            let result = filter.sort((firstItem, secondItem) => {
              return firstItem.title.toUpperCase() > secondItem.title.toUpperCase() ? 1 : -1;
            });
            return result;
          }
          return this.items;
        }
      },
    },
    checkedArray() {
      let newArray = this.filtered.filter((item) => {
        if (item?.checked) {
          return item.checked === true;
        }
      });
      console.log('checkedArray: ', newArray);
      return newArray;
    },
  },
  methods: {
    changeHandler(event, index) {
      if (this.filtered[index] !== undefined) {
        this.filtered[index].checked = event.target.checked;
        let newItem = this.filtered[index];
        this.$set(this.filtered, index, newItem);
      }
    },
    // Click on option. If there is a click handler, the click handler is executed, and if there is no click handler, the checklist is saved.
    optionClick(item) {
      this.$nextTick(() => {
        const self = this;
        if (self.clickHandler) {
          self.clickHandler(item);
        }
      });
      this.$forceUpdate();
    },
    // Full selection function. If you want to use it in the parent component, set refs and use this.$refs.locationDropdown.selectAll("location"); It can be used in the same format as
    // See OeeCenter.vue.
    selectAll() {
      let tempArray = this.filtered.slice();
      this.filtered.splice(0);
      tempArray?.map((item) => {
        item.checked = true;
        this.filtered.push(item);
      });
      this.$forceUpdate();
    },
    // Deselect all.
    unselectAll() {
      let tempArray = this.filtered.slice();
      this.filtered.splice(0);
      tempArray?.map((item) => {
        console.log('item: ', item);
        if (item.default !== true) {
          item.checked = false;
        }
        this.filtered.push(item);
      });
      this.$forceUpdate();
    },
  },
};
</script>

<style scoped>
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
  position: absolute;
  z-index: 999;
  top: 44px;
  left: 16px;
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
  border: solid 1px transparent;f
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
  font-size: 14px;
  line-height: 18px;
  height: 32px;
  padding: 0px 8px;

}
/*.dropdown-list li label:hover {*/
/*  background: #e6f7ff;*/
/*}*/
.dropdown-list li label input {
  display: none;
}
.dropdown-list li label input:checked + .checkbox-custom {
  border-color: #0075ff;
}
.dropdown-list li label input:checked + .checkbox-custom::before {
  content: '';
  width: 9px;
  height: 9px;
  background-color: #0075ff;
}
.dropdown-list li label:hover > .checkbox-custom {
  border: 1px solid #3585E5 !important;
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

.dropdown-list li label[disabled] {
  color: #c4c4c4;
  pointer-events: none;
}

label{
  margin-bottom:2px;
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
  padding: 5px;
  cursor: pointer;
  min-width: 90px;
}
.all-select-list > [disabled] {
  color: #c4c4c4;
  pointer-events: none;
}
.list-hover-dropdown:hover{
  background: #e6f7ff;
}
.info-text {
  font: normal normal normal 12px/16px Helvetica Neue;
  letter-spacing: 0px;
  color: #afafaf;
  padding-bottom: 7px;
}
</style>