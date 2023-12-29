<template>
  <div
    class="custom-dropdown"
    :class="{ 'disable-click': disabled }"
    :style="customStyle"
  >
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
          :disabled="disabled"
          :class="btnClass"
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
      "/components/@base/selection-dropdown/custom-dropdown-button.vue"
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
      default: () => ({
        frequency: "",
        title: "",
      }),
    },
    btnClass: {
      type: String,
      default: "",
    },
    disabled: Boolean,
    customStyle: {
      type: Object,
      default: () => ({}),
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
  mounted() {
    this.initdropdown();
  },
  methods: {
    select(item, index) {
      console.log("select item", item, index);
      this.isSelected = true;
      this.selectedItem = item;
      this.selectedIndex = index;
      this.$emit("change", item, this.defaultItem);
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
        // if (newValue.length > 0) {
        //   this.selectedItem = newValue[0];
        //   this.selectedIndex = 0;
        //   this.isSelected = true;
        // }
        this.selectedItem = {};
        this.selectedIndex = null;
        this.isSelected = false;
      }
    },
  },
  watch: {
    defaultItem: {
      handler(newVal) {
        console.log("default item", newVal);
        this.initdropdown();
      },
      immediate: true,
    },
  },
};
</script>
<style>
.ant-dropdown {
  /* min-width: 145px !important; */
}
.ant-dropdown-menu {
  /* width: 145px; */
  padding: 1px;
  max-height: 212px;
  overflow: auto;
}
.selected-item {
  background-color: #e6f7ff !important;
  pointer-events: none;
}
</style>
<style scoped>
.ant-dropdown-menu {
  /* width: 145px; */
  padding: 1px;
}
</style>
