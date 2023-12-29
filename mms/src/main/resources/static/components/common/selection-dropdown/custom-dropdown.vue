<template>
  <div class="custom-dropdown">
    <a-dropdown v-model="isShow" :trigger="['click']">
      <a-menu slot="overlay">
        <a-menu-item
          v-for="(item, index) in listItem"
          :key="index"
          :class="{ 'selected-item': selectedIndex === index }"
          @click="select(item, index)"
        >
          {{ titleField ? item[titleField] : item }}
        </a-menu-item>
      </a-menu>
      <div>
        <custom-dropdown-button
          :title="getTitle"
          :is-selected="isSelected"
          :is-show="isShow"
          @click="handleShowDropdown"
        ></custom-dropdown-button>
      </div>
    </a-dropdown>
  </div>
</template>

<script>
module.exports = {
  components: {
    "custom-dropdown-button": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown-button.vue"
    ),
  },
  props: {
    listItem: {
      type: Array,
      default: () => [],
    },
    defaultTitle: {
      type: String,
      default: () => "",
    },
    titleField: {
      type: String,
      default: () => "",
    },
    defaultItem: {
      type: Object,
      default: () => null,
    },
  },
  data() {
    return {
      isShow: false,
      isSelected: false,
      selectedItem: {},
      selectedIndex: null,
    };
  },
  computed: {
    getTitle() {
      if (this.isSelected && this.selectedItem) {
        return this.titleField
          ? this.selectedItem[this.titleField]
          : this.selectedItem;
      } else {
        return this.defaultTitle;
      }
    },
  },
  watch: {
    defaultItem() {
      this.initdropdown();
    },
  },
  mounted() {
    this.initdropdown();
  },
  methods: {
    select(item, index) {
      console.log("select item", item, index);
      this.isSelected = true;
      this.selectedItem = item;
      this.selectedIndex = index;
      this.$emit("change", item);
      this.isShow = false;
    },
    handleShowDropdown(isShow) {
      this.isShow = isShow;
    },
    initdropdown() {
      console.log("initdropdown 1", this.defaultItem);
      if (this.defaultItem) {
        this.selectedItem = this.defaultItem;
        let index =
          this.listItem.length > 0
            ? this.listItem.findIndex((item) =>
                _.isEqual(item, this.defaultItem)
              )
            : 0;
        console.log("initdropdown", index);
        this.selectedIndex = index ? index : 0;
        this.isSelected = true;
      } else {
        if (this.listItem.length > 0) {
          this.selectedItem = this.listItem[0];
          this.selectedIndex = 0;
          this.isSelected = true;
        }
      }
    },
  },
};
</script>
<style>
.ant-dropdown {
  min-width: 145px !important;
}
.ant-dropdown-menu {
  min-width: 145px;
  padding: 1px;
}
.selected-item {
  background-color: #e6f7ff !important;
  pointer-events: none;
}
</style>
<style scoped>
.ant-dropdown-menu {
  padding: 1px;
}
</style>
