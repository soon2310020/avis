<template>
  <div class="tooling-filter" :style="{width: width + '%',}">
    <a-popover v-model="visible" placement="bottom" trigger="click">
      <a-button  style="margin-right: 10px; background: white; width: 100%;" :style="{background: backgrounds}" class="dropdown_button">
        {{ titleShow }}
        <a-icon type="caret-down" />
      </a-button>
      <div
          slot="content"
          style="padding: 8px 6px;"
          class="dropdown-scroll"
      >
        <div class="content-body">
          <template>
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="item in toolingFiltered">
                <a-col style="padding: 7px;" :key="item.id">
                  <p-radio v-model="checkedProductivity" :value="item.id">{{item.name}}</p-radio>
                </a-col>
              </template>
            </div>
          </template>
        </div>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    // optionArrayPart: Array,
    optionArrayProductivity: [Array, Object],
    // query: Function,
    // frequent: String,
    allproductivity: Object,
    width: Number,
    backgrounds: String,
    resources: Object,
  },
  data() {
    return {
      checkedListProductivity: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      allPart: {
        id: 'null',
        name: this.resources['all_part'],
      },
      checkedProductivity: null
    };
  },
  computed: {
    toolingFiltered() {
      const str = this.toolingInput;
      if (!str) {
        return this.optionArrayProductivity;
      }
      var options = {
        keys: ["name"]
      };
      var searcher = new Fuse(this.optionArrayProductivity, options);
      return searcher.search(str).sort((firstItem, secondItem) => firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1);
    },
    titleShow: function () {
      if (this.checkedListProductivity.length === 0) {
        return "Search by Part";
      }
      return this.checkedListProductivity[0].name;
    },
  },
   methods: {
    selectMenu(menu) {
      this.visible = true;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu){
      return this.selectedMenu === menu;
    },
    checkTooling: function(item) {
      const findIndex = this.checkedListProductivity.findIndex(
        value => value.id == item.id
      );
      return findIndex !== -1;
    }
  },
  watch: {
    checkedProductivity: function(value, oldValue) {
      if (value) {
        const tooling = this.optionArrayProductivity.filter(tooling => tooling.id === value)[0];
        this.$emit('changdays', tooling.id)
        this.$emit('changpartname', tooling.name);
        this.checkedListProductivity = [tooling];
      }
    }
  },
  mounted() {
    if (this.optionArrayProductivity && this.optionArrayProductivity.length > 0) {
      this.checkedProductivity = this.optionArrayProductivity[0].id;
      this.checkedListProductivity = [this.optionArrayProductivity[0]];
    }
  }
};
</script>

<style scoped>
.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: 100px;
  background: #f2f5fa;
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
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}

.first-layer-wrapper {
  position: relative;
}

.first-layer {
  position: absolute;
  top: 0;
}
</style>