<template>
  <div class="custom-dropdown">
    <a-dropdown v-model="isShow" :trigger="['click']" style="min-width: 145px" id="plant-areas-combination-dropdown">
      <a-menu slot="overlay" style="width: 225px;">
        <a-menu-item
          :class="{ 'selected-item': selectedIndex === 0 }"
          @click="handleUnSelect()"
        >
          None Selected
        </a-menu-item>
        <a-menu-item
          v-for="(item, index) in listItem"
          :key="index"
          :class="{ 'selected-item': selectedIndex === index + 1 }"
          @click="select(item, index + 1)"
        >
          <div class="d-flex justify-space-between align-center">
            <div>
              {{ titleField ? item[titleField] : item }}
            </div>
            <div class="dropdown-chip">
              <base-chip
                :selection="selectionType[item.areaType]"
                :allow-tooltips="false"
                :color="'wheat'"
                :close-able="false"
              >
              </base-chip>
            </div>
          </div>
        </a-menu-item>
        <a-menu-item
          v-show="plantId"
         class="ant-dropdown-menu-item--hover-none"
        >
          <span class="custom-hyper-link" @click="handleCreatePlantArea()">+ Create plant area</span>
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
const TYPES = {
    MAINTENANCE_AREA: {
        id: 1,
        title: 'Maintenance'
    },
    PRODUCTION_AREA: {
        id: 2,
        title: 'Production'
    },
    WAREHOUSE: {
        id: 3,
        title: 'Warehouse'
    },
}
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
      default: () => ({
        frequency: "",
        title: "",
      }),
    },
    plantId: [String, Number]
  },
  data() {
    return {
      isShow: false,
      isSelected: false,
      selectedItem: {},
      selectedIndex: -1,
      selectionType: {...TYPES}
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
    handleCreatePlantArea() {
      console.log('handleCreatePlantArea')
      const el = document.getElementById('plant-areas-combination-dropdown')
      if (el) {
        el.click()
      }
      this.isShow = false
      this.$emit('create-new')
    },
    handleUnSelect() {
      this.isSelected = true;
      this.selectedItem = {name: 'None Selected'};
      this.selectedIndex = 0;
      this.$emit("change", {name: 'None Selected'});
      this.isShow = false;
    },
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
        if (this.selectedItem.name === 'None Selected') {
          this.selectedIndex = index >= 0 ? index + 1 : 0;
          this.isSelected = true;
        } else {
          const index = this.listItem.length > 0 ? this.listItem.findIndex((item) =>
            item.id === this.defaultItem.id
          ) : 0
          console.log("initdropdown", index);
          this.selectedIndex = index >= 0 ? index + 1 : 0;
          this.isSelected = true;
        }
      } else {
        this.isSelected = true;
        this.selectedItem = null;
        this.selectedIndex = -1;
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
  min-width: 145px !important;
}
.ant-dropdown-menu {
  width: 225px;
  padding: 1px;
}
.selected-item {
  background-color: #e6f7ff !important;
  pointer-events: none;
}
.ant-dropdown-menu-item--hover-none:hover {
  background-color: unset !important;
}
</style>
