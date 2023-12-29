<template>
  <div class="parts-filter">
    <a-popover placement="bottom" trigger="click" v-model="isShowFirstLayer">
      <a :id="'parts-filters-' + id" href="javascript:void(0)" @click="showAnimation(); showFirstLayer();"
        style="margin-right: 10px; width: 110px;" class="dropdown_button-custom">
        <span :class="{ 'selected-dropdown-text': isShowFirstLayer }">{{ titleShow }}</span>
        <div>
          <a-icon class="caret" :class="{ 'selected-dropdown-text': isShowFirstLayer, 'caret-show': displayCaret }"
            type="caret-down" />
        </div>
      </a>
      <div v-if="isShowFirstLayer" slot="content">
        <a-button style="margin-bottom: 9px;" key="PART" class="dropdown_button inside-dropdown-button"
          :class="{ 'selected-button': selectedMenu == menuOptions.PART }"
          @click="setChangeData(); selectMenu(menuOptions.PART)" v-text="resources['part']"></a-button><br>
        <a-button key="TOOLING" class="dropdown_button inside-dropdown-button"
          :class="{ 'selected-button': selectedMenu == menuOptions.TOOLING }"
          @click="setChangeData(); selectMenu(menuOptions.TOOLING)" v-text="resources['tooling']"></a-button>
      </div>
    </a-popover>
    <a-popover v-model="visible" placement="bottom" trigger="click">
      <div slot="content" style="padding: 8px 6px;" class="dropdown-scroll">
        <div class="content-header" id="back-popup-parts" @click="showFirstLayer()">
          Back
        </div>
        <div class="content-body">
          <template v-if="selectedMenu === menuOptions.PART">
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="partInput">
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div style=" color: red; padding: 7px;">* Select maximum 4 parts</div>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="item in partFiltered" :key="item.id">
                <a-col style="padding: 7px;">
                  <p-check :checked="checkedPart(item)" @change="setChangeData(); onChangePart($event, item)">
                    {{ item.name }}
                  </p-check>
                </a-col>
              </template>
            </div>
          </template>
          <template v-else>
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div style=" color: red; padding: 7px;">* Select maximum 4 toolings</div>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="item in toolingFiltered" :key="item.id">
                <a-col style="padding: 7px;">
                  <p-check :checked="checkedTooling(item)" @change="setChangeData(); onChangeTooling($event, item)">
                    {{ item.name }}</p-check>
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
    optionArrayPart: Array,
    optionArrayTooling: Array,
    query: Function,
    resources: Object,
    id: String,
    setChangeData: Function,
    defaultIdList: String,
    defaultType: String,
    defaultFrequent: String,
  },
  data() {
    return {
      checkedListPart: [],
      oldCheckedListPart: [],
      partInput: "",
      checkedListTooling: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      isShowFirstLayer: false,
      menuOptions: {
        PART: 'PART',
        TOOLING: 'TOOLING'
      },
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
      clickBack: false,
      isLoadedData: false,

      currentType: '',
      selectedMenu: 'PART',
    };
  },
  methods: {
    showFirstLayer() {
      this.isShowFirstLayer = true;
      this.visible = false;
    },
    selectMenu(menu) {
      this.visible = true;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu) {
      return this.selectedMenu === menu;
    },
    checkedPart(item) {
      const findIndex = this.checkedListPart.findIndex(
        value => value.id == item.id
      );
      return findIndex !== -1;
    },

    checkedTooling(item) {
      const findIndex = this.checkedListTooling.findIndex(
        value => value.id == item.id
      );
      return findIndex !== -1;
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
      this.query(this.checkedListPart, this.frequent, this.menuOptions.PART);
    },
    onMouseleaveTooling() {
      console.log('@onMouseleaveTooling:parts-filter');
      if (this.checkedListTooling.length != 0) {
        this.oldCheckedListTooling = this.checkedListTooling;
        this.query(this.checkedListTooling, this.frequent, this.menuOptions.TOOLING);
      } else {
        this.selectedMenu = this.menuOptions.PART;
      }
    },

    onChangePart(isChecked, item) {
      console.log('@onChangePart:parts-filter>', isChecked, item);
      if (isChecked) {
        if (this.checkedListPart.length >= 4) {
          this.checkedListPart = this.checkedListPart.filter((value, index) => index !== 0);
        }
        this.checkedListPart = this.checkedListPart.concat(item);
      } else {
        this.checkedListPart = this.checkedListPart.filter(value => value.id !== item.id);
      }
      this.displayCaret = true;
    },

    onChangeTooling(isChecked, item) {
      if (isChecked) {
        if (this.checkedListTooling.length >= 4) {
          this.checkedListTooling = this.checkedListTooling.filter(
            (value, index) => index !== 0
          );
        }
        this.checkedListTooling = this.checkedListTooling.concat(item);
      } else {
        this.checkedListTooling = this.checkedListTooling.filter(
          value => value.id !== item.id
        );
      }
      this.displayCaret = true;
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
    setDataDefault() {
      console.log(
        '@setDataDefault:parts-filter',
        {
          partInput: this.partInput,
          toolingInput: this.toolingInput,
          checkedListPart: this.checkedListPart,
          checkedListTooling: this.checkedListTooling,
          defaultFrequent: this.defaultFrequent,
          defaultType: this.defaultType,
          defaultIdList: this.defaultIdList,
          optionArrayPart: this.optionArrayPart,
          optionArrayTooling: this.optionArrayTooling
        }
      )

      // TOOLING
      if (this.selectedMenu === this.menuOptions.TOOLING && this.optionArrayTooling?.length) {
        if (this.listIds) {
          // has default selected
          const listIds = this.listIds.split(',').map(id => Number(id))
          const listFound = this.optionArrayTooling.filter(item => listIds.includes(item.id))
          this.checkedListTooling = listFound.length ? listFound : [this.optionArrayTooling[0]]
        }
        else {
          // select first item as default case
          this.checkedListTooling = [this.optionArrayTooling[0]]
        }
        this.listIds = this.checkedListTooling.map(i => i.id).toString()
        this.checkedListPart = []
        this.query(this.checkedListTooling, this.frequent, this.selectedMenu)
      }

      // PART
      if (this.selectedMenu === this.menuOptions.PART && this.optionArrayPart?.length) {
        if (this.listIds) {
          // has default selected
          const listIds = this.listIds.split(',').map(id => Number(id))
          const listFound = this.optionArrayPart.filter(item => listIds.includes(item.id))
          this.checkedListPart = listFound.length ? listFound : [this.optionArrayPart[0]]
        }
        else {
          // select first item as default case
          this.checkedListPart = [this.optionArrayPart[0]]
        }
        this.listIds = this.checkedListPart.map(i => i.id).toString()
        this.checkedListTooling = []
        this.query(this.checkedListPart, this.frequent, this.selectedMenu)
      }
    }
  },
  computed: {
    partFiltered() {
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
        return this.optionArrayTooling;
      }
      var options = {
        keys: ["name"]
      };
      var searcher = new Fuse(this.optionArrayTooling, options);
      return searcher.search(str);
    },
    titleShow() {
      console.log('@computed:parts-filter>titleShow', {
        selectedMenu: this.selectMenu,
        menuOptions: this.menuOptions,
        checkedListPart: this.checkedListPart,
        checkedListTooling: this.checkedListTooling
      })
      if (this.selectedMenu === this.menuOptions.PART) {
        if (this.checkedListPart.length === 0) {
          return this.resources['part'];
        }

        if (this.checkedListPart.length === 1) {
          return "1 " + this.resources['part'];
        }

        return `${this.checkedListPart.length} Parts`;
      } else {
        if (this.checkedListTooling.length === 0) {
          return this.resources['tooling'];
        }
        if (this.checkedListTooling.length === 1) {
          return "1 " + this.resources['tooling'];
        }
        return `${this.checkedListTooling.length} ` + this.resources['tooling'] + "s";
      }
    },
  },
  watch: {
    visible(newValue, oldValue) {
      if (!newValue && !this.isShowFirstLayer) {
        this.closeAnimation();
        this.onMouseLeavePopup()
      }
    },
    isShowFirstLayer(newValue, oldValue) {
      let self = this
      console.log(`There - ${self.isShowFirstLayer} - ${newValue} - ${oldValue}`)
      if (!self.isShowFirstLayer) {
        this.closeAnimation();
      } else {
        setTimeout(() => {
          const el = document.getElementById('parts-filters-' + this.id)
          el.classList.add("selected-dropdown-text");
          el.classList.add("selected-dropdown-button");
        }, 1);
      }
    },
    optionArrayTooling(newVal, oldVal) {
      console.log('@watch:parts-filter>optionArrayTooling', newVal, oldVal)
      if (this.frequent && this.selectedMenu) {
        this.setDataDefault()
      }
    },
    defaultFrequent(newVal) {
      console.log('@watch:parts-filter>defaultFrequent', newVal)
      const hasOptions = this.optionArrayTooling.length || this.optionArrayPart.length
      if (newVal && hasOptions) {
        this.frequent = newVal
        this.selectedMenu = this.defaultType
        this.listIds = this.defaultIdList
        this.setDataDefault()
      }
    }
  },
  mounted() {
    console.log(
      '@mounted:parts-filter',
      {
        partInput: this.partInput,
        toolingInput: this.toolingInput,
        checkedListPart: this.checkedListPart,
        checkedListTooling: this.checkedListTooling,
        defaultFrequent: this.defaultFrequent,
        defaultType: this.defaultType,
        defaultIdList: this.defaultIdList,
        optionArrayPart: this.optionArrayPart,
        optionArrayTooling: this.optionArrayTooling
      }
    )
    if (this.defaultFrequent) {
      this.frequent = this.defaultFrequent
      this.selectedMenu = this.defaultType
      this.listIds = this.defaultIdList
      this.setDataDefault()
    }
  }
};
</script>
<style>
.inside-button {
  width: 184px;
  border: 1px solid #A2A2A2;
  color: #595959;
}

.inside-button:hover {
  border: 1px solid #3491FF;
}

.ant-menu-item-selected {
  background: none !important;
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
  width: 100px;
  background: #f2f5fa;
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
