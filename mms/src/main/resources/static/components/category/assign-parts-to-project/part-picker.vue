<template>
  <div
    class="part-picker"
    :class="{ show: isVisible }"
    v-click-outside="handleClose"
  >
    <div class="search-bar">
      <a-input v-model="searchString">
        <span slot="addonBefore" class="icon-search"></span>
      </a-input>
    </div>
    <div class="search-list">
      <ul class="search-list__parts" ref="listRef">
        <li
          v-for="(item, index) in listUnassignedParts"
          :key="index"
          @click="() => onPick(item)"
        >
          {{ item.name }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "PartPicker",
  props: {
    isVisible: {
      type: Boolean,
      default: false,
    },
    listUnassignedParts: {
      type: Array,
      default: [],
    },
    onSearch: {
      type: Function,
      default: () => {},
    },
    onPick: {
      type: Function,
      default: () => {},
    },
    onClose: {
      type: Function,
      default: () => {},
    },
  },
  data() {
    return {
      listRef: null,
      searchString: "",
      page: 0,
      size: 20,
    };
  },
  methods: {
    handleClose() {
      if (this.isVisible) this.onClose();
    },
    handleShowMore() {
      this.isReachEnd = Common.listener.onReachEnd(this.listRef);
    },
    handleSearch: _.debounce(function () {
      this.onSearch({
        searchString: this.searchString,
        page: this.page,
        size: this.size,
      });
    }, 500),
  },
  watch: {
    isVisible(newVal) {
      if (!newVal) {
        this.searchString = "";
        this.page = 0;
      }
    },
    searchString(newVal) {
      if (newVal) this.handleSearch();
    },
    isReachEnd(newVal) {
      if (newVal) {
        this.page += 1;
        this.handleSearch();
      }
    },
    listUnassignedParts(newVal) {
      console.log('listUnassignedParts in part pickers', newVal)
    }
  },
  mounted() {
    this.listRef = this.$refs.listRef;
    this.$refs.listRef.addEventListener("scroll", this.handleShowMore);
  },
  unmounted() {
    this.$refs.listRef.removeEventListener("scroll", this.handleShowMore);
  },
};
</script>

<style scoped src="/components/category/assign-parts-to-project/part-picker.css"></style>
