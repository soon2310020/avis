<!-- 
  copy from common dropdown from 05/12/2022
-->

<template>
  <div class="dropdown-wrap" :style="wrapStyle">
    <!-- search box -->
    <div v-if="hasSearchBox" class="dropdown-searchbox">
      <div class="search-icon">
        <img src="/images/search-icon.svg" />
      </div>
      <input
        v-model="inputContent"
        :placeholder="placeholder || 'Search Code Details'"
      />
      <div class="focus-border"></div>
    </div>

    <!-- option dropdown list -->
    <ul class="dropdown-list">
      <!-- checkbox all click handler -->
      <li v-if="allcheck">
        <label>
          <input
            type="checkbox"
            @click="allClickHandler"
            :checked="isAllChecked"
          />
          <div class="checkbox-custom" v-if="checkbox"></div>
          {{ allCheckLabel || "All Check" }}
        </label>
      </li>
      <li v-for="(item, index) in filtered" :key="index">
        <label>
          <input
            type="checkbox"
            @click="clickHandler"
            :value="JSON.stringify(item)"
            :checked="item.checked"
            v-model="item.checked"
          />
          <!-- checkbox -->
          <div v-if="checkbox" class="checkbox-custom"></div>
          <!-- additional image ex) chart legend image -->
          <div v-if="item.image">
            <img :src="item.image" alt="" />
          </div>
          {{ item.title }}
        </label>
      </li>
    </ul>
  </div>
</template>

<script>
module.exports = {
  name: "CommonDropdown",
  props: {
    items: [Array, Object],
    allcheck: Boolean,
    allClickHandler: Function,
    clickHandler: Function,
    checkbox: Boolean,
    searchbox: Boolean,
    image: Boolean,
    wrapStyle: Object,
    placeholder: String,
    allCheckLabel: String,
  },
  data() {
    return {
      inputContent: "",
    };
  },
  computed: {
    filtered() {
      const str = this.inputContent.trim();

      if (!str || str == "") {
        return [...this.items];
      }

      if (this.items != null) {
        let filter = this.items.filter((item) => {
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

      return [...this.items];
    },
    hasSearchBox() {
      return this.searchbox && this.items.length > 4;
    },
    isAllChecked() {
      return this.items.every((o) => o.checked);
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
  padding: 8px 6px;
  box-shadow: 1px 1px 5px rgba(0, 0, 0, 0.1);
  background: white;
  position: absolute;
  z-index: 999;
  top: 44px;
  left: 16px;
  min-width: 170px;
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
  margin: 4px 0px;
}
.dropdown-list li label {
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  font-size: 14px;
  line-height: 18px;
  /*height: 32px;*/
  padding: 6px 8px;
}
.dropdown-list li label:hover {
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
.dropdown-list li label .checkbox-custom {
  width: 15px;
  min-width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
