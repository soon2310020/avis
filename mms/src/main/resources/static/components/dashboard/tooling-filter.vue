<template>
  <div class="tooling-filter">
    <a-popover v-model="visible" placement="bottomLeft" trigger="click">
      <a :id="'tooling-filter-' + id" href="javascript:void(0)" style="margin-right: 10px; width: 110px;"
        @click="showAnimation()" class="dropdown_button-custom">
        <span class="truncate" :class="{ 'selected-dropdown-text': visible }">{{ titleShow }}</span>
        <div>
          <a-icon class="caret" :class="{ 'selected-dropdown-text': visible, 'caret-show': displayCaret }"
            type="caret-down" />
        </div>
      </a>
      <div slot="content" style="padding: 8px 6px;" class="dropdown-scroll">
        <div class="content-body">
          <template>
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="item in toolingFiltered" :key="item.id">
                <a-col style="padding: 7px;">
                  <p-radio v-model="checkedTooling" @change="displayCaret = true" :value="item.id">{{ item.name
                    }}</p-radio>
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
    optionArrayTooling: Array,
    query: Function,
    frequent: String,
    resources: Object,
    id: String,
    defaultToolingId: Number
  },
  data() {
    return {
      checkedListTooling: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      animationTimeout: null,
      checkedTooling: null,
      displayCaret: false,
      caretTimeout: null,
      isLoadedData: false
    };
  },
  methods: {
    selectMenu(menu) {
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu) {
      return this.selectedMenu === menu;
    },
    checkTooling: function (item) {
      const findIndex = this.checkedListTooling.findIndex(
        value => value.id == item.id
      );
      return findIndex !== -1;
    },
    onMouseLeave() {
      console.log('@onMouseLeave:tooling-filter')
      this.query(this.checkedListTooling, this.frequent, 'TOOLING');
    },
    showAnimation() {
      const el = document.getElementById('tooling-filter-' + this.id)
      if (!this.visible) {
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
      const el = document.getElementById('tooling-filter-' + this.id)
      if (this.animationTimeout != null) {
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

    setDefaultFilter(isWatchToolingList = false) {
      console.log('@setDefaultFilter:tooling-filter')
      const hasListOptions = this.optionArrayTooling.length > 0
      if (hasListOptions) {
        const defaultTooling = this.optionArrayTooling.filter(tooling => tooling.id === +this.defaultToolingId);
        if (defaultTooling.length > 0) {
          // found case
          this.checkedTooling = defaultTooling[0].id;
          this.checkedListTooling = [defaultTooling[0]];
        } else {
          // not found case
          this.checkedTooling = this.optionArrayTooling[0].id;
          this.checkedListTooling = [this.optionArrayTooling[0]];
        }
      } else {
        console.log('no option availabled')
      }
    },

    focusSelected(checkedTooling) {
      if (checkedTooling) {
        this.checkedTooling = checkedTooling.id;
        this.checkedListTooling = [checkedTooling];
      }
    }
  },

  computed: {
    toolingFiltered() {
      const str = this.toolingInput;
      if (!str) {
        return this.optionArrayTooling;
      }
      var options = {
        threshold: 0,
        location: 0,
        distance: 100,
        keys: ["name"]
      };
      var searcher = new Fuse(this.optionArrayTooling, options);
      return searcher.search(str).sort((firstItem, secondItem) => firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1);

    },
    titleShow() {
      if (this.checkedListTooling.length === 0) {
        return this.resources['tooling'];
      }
      return this.checkedListTooling[0].name;
    },
  },
  watch: {
    visible(newValue, oldValue) {
      if (!newValue) {
        this.closeAnimation()
      }
    },
    checkedTooling(value, oldValue) {
      const tooling = this.optionArrayTooling.filter(tooling => tooling.id === value)[0];
      this.checkedListTooling = [tooling];
      this.$emit('change-tooling', this.checkedListTooling, this.frequent);
    },
    optionArrayTooling(newVal, oldVal) {
      console.log('@watch:tooling-filter>optionArrayTooling', newVal)
      if (this.defaultToolingId) {
        this.setDefaultFilter();
      }
    },
    defaultToolingId(newVal, oldVal) {
      console.log('@watch:tooling-filter>defaultToolingId', newVal)
      if (this.optionArrayTooling.length) {
        this.setDefaultFilter()
      }
    }
  },
  mounted() {
    console.log('@mounted:tooling-filter', this.defaultToolingId, this.optionArrayTooling)
    if (this.frequent && this.optionArrayTooling.length && this.defaultToolingId) {
      this.setDefaultFilter()
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
.ant-btn:focus>svg {
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
