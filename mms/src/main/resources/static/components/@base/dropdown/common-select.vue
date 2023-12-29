<!--
  copy from common dropdown from 05/12/2022
-->

<template>
  <div class="dropdown-wrap" :style="wrapStyle">
    <!-- search box -->
    <div v-if="searchbox" class="dropdown-searchbox">
      <div class="search-icon">
        <img src="/images/search-icon.svg" />
      </div>
      <input
        v-if="paginatable"
        v-model="inputContent"
        :placeholder="placeholder || 'Search Code Details'"
        @input="handleSearchPagination"
      />
      <input
        v-else
        v-model="inputContent"
        :placeholder="placeholder || 'Search Code Details'"
        @input="handleSearch"
      />
      <div class="focus-border"></div>
    </div>

    <!-- option dropdown list -->
    <ul class="dropdown-list">
      <!-- checkbox all click handler -->

      <template v-if="filtered.length > 0">
        <li v-if="allcheck">
          <label>
            <input type="checkbox" @click="allClickHandler" />
            <div class="checkbox-custom checkbox-square"></div>
            All Check
          </label>
        </li>
        <li
          v-if="multiple && hasToggledAll && filtered.length > 0"
          class="all-select-list"
        >
          <div @click="selectAll" style="margin-right: 21px; cursor: pointer">
            Select All
          </div>
          <div @click="unselectAll" style="cursor: pointer">Unselect All</div>
        </li>

        <li v-for="(item, index) in filtered" :key="index">
          <label>
            <input
              type="checkbox"
              @click="onClickItem(item)"
              :value="JSON.stringify(item)"
              :checked="item.checked"
              v-model="item.checked"
            />
            <!-- checkbox -->
            <div
              v-if="multiple || hasCheckBox"
              class="checkbox-custom checkbox-square"
            ></div>
            <!-- additional image ex) chart legend image -->
            <div v-if="item.image">
              <img :src="item.image" alt="" />
            </div>
            <span class="label-name-style">{{ item.title }}</span>
          </label>
        </li>
        <li v-if="paginatable && currentPage < totalPage">
          <div
            class="custom-hyper-link"
            style="margin: 0 8px"
            @click="$emit('show-more')"
          >
            Show More
          </div>
        </li>
      </template>
      <li v-else>No option is available</li>
    </ul>
  </div>
</template>

<script>
module.exports = {
  name: "CommonDropdown",
  props: {
    items: [Array, Object],
    checkedItemsList: [Array, Object],
    allcheck: Boolean,
    allClickHandler: Function,
    multiple: Boolean, // true = checkbox
    searchbox: Boolean,
    image: Boolean,
    wrapStyle: Object,
    assetView: String,
    placeholder: String,
    hasToggledAll: Boolean, // true = select all + unsellect all
    paginatable: {
      type: Boolean,
      default: false,
    },
    currentPage: {
      type: [String, Number],
      default: 1,
    },
    totalPage: {
      type: [String, Number],
      default: 1,
    },
    visible: Boolean,
    hasCheckBox: Boolean,
  },
  data() {
    return {
      inputContent: "",
    };
  },
  watch: {
    visible(newVal) {
      if (!newVal) {
        this.inputContent = "";
      }
    },
  },
  methods: {
    onClickItem(item) {
      console.log("tick item", this.multiple, item);
      if (!this.multiple) {
        let results = _.map(this.items, (o) =>
          o.id === item.id
            ? {
                ...item,
                checked: true,
                assetView: o.assetView || this.assetView,
              }
            : { ...o, checked: false, assetView: o.assetView || this.assetView }
        );
        this.$emit("on-select", item);
        this.$emit("on-change", results);
      } else {
        let resultsCheckbox = _.map(this.items, (o) =>
          o.id === item.id ? item : o
        );
        if (this.checkedItemsList == undefined) {
          this.$emit("on-select", item);
          this.$emit("on-change", resultsCheckbox);
          return;
        }
        if (item.checked) {
          let checkedList = this.checkedItemsList.filter(
            (itemList) => itemList.id != item.id
          );
          this.$emit("on-change-checked", checkedList);
        } else {
          let checkedList = this.checkedItemsList;
          item.assetView = this.assetView;
          checkedList.push(item);
          this.$emit("on-change-checked", checkedList);
        }
      }
    },
    selectAll() {
      this.$emit(
        "on-change",
        _.map(this.items, (o) => ({
          ...o,
          checked: true,
          assetView: this.assetView,
        }))
      );
      this.$emit(
        "on-change-checked",
        _.map(this.items, (o) => ({
          ...o,
          checked: true,
          assetView: this.assetView,
        }))
      );
    },
    unselectAll() {
      this.$emit(
        "on-change",
        _.map(this.items, (o) => ({ ...o, checked: false }))
      );
      this.$emit("unselect-all");
    },
    handleSearch: _.debounce(function (event) {
      const text = event.target.value;
      this.$emit("on-search", text);
    }, 500),
    handleSearchPagination: _.debounce(function (event) {
      const text = event.target.value;
      this.$emit("on-search-pagination", text);
    }, 500),
  },
  computed: {
    filtered() {
      const str = this.inputContent.trim();
      if (!_.isArray(this.items)) {
        return [];
      }
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
  height: 32px;
  padding: 0px 8px;
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
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.all-select-list {
  display: flex;
  color: #3491ff;
}
.checkbox-square {
  aspect-ratio: 1;
}
.label-name-style {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
