<template>
  <div class="parts-filter">
    <a-popover placement="bottomLeft" trigger="click" v-model="isShowFirstLayer">
      <a :id="'parts-filters-' + id" href="javascript:void(0)" @click="showAnimation(); showFirstLayer();"
        style="margin-right: 10px; width: 121px" class="dropdown_button-custom">
        <span :class="{ 'selected-dropdown-text': isShowFirstLayer }"
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0px 3px 0px; max-width: 100px;">
          {{ titleShow }}
        </span>
        <div>
          <a-icon class="caret" :class="{ 'selected-dropdown-text': isShowFirstLayer, 'caret-show': displayCaret }"
            type="caret-down" />
        </div>
      </a>
      <template v-if="!visible" slot="content" style="width: 230px;">
        <a-button style="margin-top: 0; margin-bottom: 9px;" @click="selectMenu(menuOptions.PART)"
          class="inside-dropdown-button" :class="{ 'selected-button': selectedMenu == 'Part' }"><span
            v-text="resources['by_part']"></span></a-button><br>
        <a-button @click="selectMenu(menuOptions.TOOLING)" class="inside-dropdown-button"
          :class="{ 'selected-button': selectedMenu == 'Tooling' }"><span
            v-text="resources['by_supplier']"></span></a-button>
      </template>
      <template v-if="visible" slot="content" style="padding: 8px 6px;max-width: 250px;" class="dropdown-scroll">
        <div class="content-body">
          <template v-if="selectedMenu === menuOptions.PART">
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="partInput">
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div class="custom-radio-uptime"
              style="max-height: 250px;max-width: 220px;overflow-y: auto;overflow-x: hidden;">
              <a-col style="padding: 7px;" :key="'part-all'">
                <p-radio v-model="checkedPart" @change="displayCaret = true" :value="'null'">
                  {{ resources['dashboard_all_parts'] }}
                </p-radio>
              </a-col>
              <template v-for="(item, index) in partFiltered" :key="'part' + index">
                <a-col style="padding: 7px;">
                  <p-radio v-model="checkedPart" @change="displayCaret = true" :value="item.id">
                    {{ item.name }}
                  </p-radio>
                </a-col>
              </template>
            </div>
          </template>
          <template v-else>
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div class="custom-radio-uptime"
              style="max-height: 250px;max-width: 220px; overflow-y: auto;overflow-x: hidden;">
              <a-col style="padding: 7px;" :key="'supplier-all'">
                <p-radio v-model="checkedTooling" @change="displayCaret = true" :value="'null2'">
                  {{ resources['all_suppliers'] }}
                </p-radio>
              </a-col>
              <template v-for="(item, index2) in toolingFiltered" :key="'supplier' + index2">
                <a-col style="padding: 7px;">
                  <p-radio v-model="checkedTooling" @change="displayCaret = true" :value="item.id">
                    {{ item.name }}
                  </p-radio>
                </a-col>
              </template>
            </div>
          </template>
        </div>
      </template>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    optionArrayPart: Array,
    optionArraySupplier: Array,
    changePart: Function,
    changeSupplier: Function,
    query: Function,
    frequent: String,
    resources: Object,
    id: String,
    defaultId: Number,
    defaultType: String,
  },
  data() {
    return {
      // optionArrayPart: [],
      checkedPart: 'null',
      checkedTooling: 'null2',
      checkedListPart: null,
      oldCheckedListPart: null,
      partInput: "",
      checkedListTooling: null,
      oldCheckedListTooling: null,
      toolingInput: "",
      visible: false,
      isShowFirstLayer: false,
      menuOptions: {
        PART: 'Part',
        TOOLING: 'Tooling'
      },
      allPart: {
        id: 'null',
        name: 'dashboard_all_parts'
      },
      allSupplier: {
        id: 'null2',
        name: 'all_suppliers'
      },
      selectedMenu: 'Part',
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
      isLoadedData: false
    };
  },
  methods: {
    showFirstLayer() {
      this.isShowFirstLayer = true;
      this.visible = false;
    },
    selectMenu(menu) {
      this.visible = true;
      // this.isShowFirstLayer = false;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu) {
      return this.selectedMenu === menu;
    },
    onMouseLeavePopup() {
      if (this.selectedMenu === this.menuOptions.PART) {
        this.onMouseleavePart();
      } else {
        this.onMouseleaveTooling();
      }
    },

    onMouseleavePart() {
      this.oldCheckedListPart = this.checkedListPart;
      this.changePart(this.checkedListPart);
    },
    onMouseleaveTooling() {
      console.log(this.checkedListTooling, 'checkedListTooling');
      this.oldCheckedListTooling = this.checkedListTooling;
      this.changeSupplier(this.checkedListTooling);
    },
    showAnimation() {
      const el = document.getElementById('parts-filters-' + this.id)
      if (!this.isShowFirstLayer) {
        setTimeout(() => {
          el.classList.add("dropdown_button-animation");
          this.animationTimeout = setTimeout(() => {
            el.classList.remove("dropdown_button-animation");
            el.classList.add("selected-dropdown-text");
            el.classList.add("selected-dropdown-button");
          }, 1600);
        }, 1);

      } else {
        this.closeAnimation()
      }
    },
    closeAnimation() {
      const el = document.getElementById('parts-filters-' + this.id)
      if (this.animationTimeout != null) {
        console.log("Here");
        console.log(this.animationTimeout);
        clearTimeout(this.animationTimeout);
        this.animationTimeout = null;
      }
      if (this.caretTimeout != null) {
        clearTimeout(this.caretTimeout);
        this.caretTimeout = null;
      }
      el.classList.remove("dropdown_button-animation");
      el.classList.remove("selected-dropdown-text");
      el.classList.remove("selected-dropdown-button");
      this.displayCaret = false
    },
    setDefaultFilter(watchData = false) {
      console.log('setDefaultFilter', this.defaultType)
      if (this.defaultType && !this.isLoadedData && (this.optionArraySupplier || this.optionArrayPart)) {
        this.isLoadedData = true;
        this.selectedMenu = this.defaultType === 'PART' ? this.menuOptions.PART : this.menuOptions.TOOLING;
        if (this.defaultId) {
          if (this.selectedMenu === this.menuOptions.TOOLING) {
            const toolingData = this.optionArraySupplier.filter(tooling => tooling.id === +this.defaultId);
            if (toolingData && toolingData.length > 0) {
              this.checkedListTooling = toolingData[0];
              this.checkedTooling = toolingData[0].id;
            }
          } else {
            const partData = this.optionArrayPart.filter(part => part.id === +this.defaultId);
            if (partData && partData.length > 0) {
              this.checkedListPart = partData[0];
              this.checkedPart = partData[0].id;
            }
          }
        }
      }

      if (watchData) {
        if (this.selectedMenu === this.menuOptions.PART && !this.optionArrayPart.some(part => part.id === this.checkedPart)) {
          this.checkedListPart = 'null';
          this.checkedPart = 'null';
          this.changePart('null')
        } else if (this.selectedMenu === this.menuOptions.TOOLING && !this.optionArraySupplier.some(supplier => supplier.id === this.checkedTooling)) {
          this.checkedListTooling = 'null2';
          this.checkedTooling = 'null2';
          this.changeSupplier(null)
        }
      }
    }
  },

  computed: {
    partFiltered() {
      // this.
      const str = this.partInput;
      if (!str) {
        return this.optionArrayPart;
      }
      var options = {
        keys: ["name"]
      };
      var searcher = new Fuse(this.optionArrayPart, options);
      return searcher.search(str);
    },

    toolingFiltered() {
      const str = this.toolingInput;
      if (!str) {
        return this.optionArraySupplier;
      }
      var options = {
        keys: ["name"]
      };
      var searcher = new Fuse(this.optionArraySupplier, options);
      return searcher.search(str);
    },
    titleShow: function () {
      console.log(this.checkedListPart, 'titleShow', this.selectedMenu)
      console.log(this.checkedListTooling, '2')
      if (this.selectedMenu === this.menuOptions.PART) {
        if (!this.checkedListPart || this.checkedListPart === 'null') {
          return this.resources['dashboard_all_parts'];
        }

        if (this.checkedListPart) {
          return this.resources['part'] + '-' + this.checkedListPart.name;
        }
      } else {
        if (!this.checkedListTooling || this.checkedListTooling === 'null2') {
          return this.resources['all_suppliers'];
        }
        if (this.checkedListTooling) {
          return this.resources['supplier'] + '-' + this.checkedListTooling.name;
        }
      }
    },
  },
  watch: {
    visible(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseLeavePopup();
      }
    },
    isShowFirstLayer(newValue, oldValue) {
      if (newValue == false) {
        this.visible = false
      }
      if (!this.visible) {
        this.closeAnimation();
      }
    },
    checkedPart(value) {
      if (value) {
        const part = this.optionArrayPart.filter(part => part.id === value)[0];
        console.log(part, 'part ne')
        this.checkedListPart = part;
        // this.changePart(part);
      }
    },
    checkedTooling(value) {
      if (value) {
        const supplier = this.optionArraySupplier.filter(supplier => supplier.id === value)[0];
        console.log(supplier, 'supplier ne')
        this.checkedListTooling = supplier;
        // this.changeSupplier(supplier);
      }
    },
    defaultType(newVal) {
      this.setDefaultFilter();
    },
    optionArraySupplier(value) {
      this.setDefaultFilter(true);
    },
    resources(newValue) {
      if (newValue) {
        this.allPart.name = newValue[this.allPart.name]
        this.allSupplier.name = newValue[this.allSupplier.name]
      }
    }
  },
  async mounted() {
    this.setDefaultFilter();
    this.checkedListPart = 'null';
  }
};
</script>
<style>
.custom-radio-uptime .pretty .state label {
  width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.inside-dropdown-button {
  width: 184px;
  padding-bottom: 6px;
  padding-top: 6px;
}

.inside-dropdown-button:hover {
  border: 1px solid #3491FF !important;
  color: #3491FF !important;
  background-color: #FFFFFF !important;
  transition: 0s;
}

.selected-button:hover {
  border: 1px solid #A2A2A2 !important;
  background-color: #3491FF !important;
}

.ant-popover-inner-content {
  padding: 12px 10px 12px 10px !important;
}
</style>
<style scoped>
.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: unset;
  border: .5px solid #A2A2A2;
  background: #fff;
  border-radius: 4px;
  z-index: 1;
  height: 100%;
}

.dropdown_button span {
  margin-right: 9px;
  display: block;
  max-width: 160px;
  display: block;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown_button-ab {
  position: absolute;
  top: 50%;
  right: -1px;
  transform: translate(0, -50%);
  height: calc(100% + 2px);
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  background: #f2f5fa;
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus>svg {
  background: #f2f5fa;
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

.selectedMenu-uptime {
  background-color: #5D43F7 !important;
  color: #fff !important;
}
</style>
