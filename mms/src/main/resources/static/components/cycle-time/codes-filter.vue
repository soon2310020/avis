<template>
  <div class="parts-filter">
    <a-popover placement="bottom" trigger="click" v-model="isShowFirstLayer">
      <a-button @click.prevent="showFirstLayer" style="margin-right: 10px; background: white; width: 100%" class="dropdown_button">
        {{ titleShow }} 
        <a-icon type="caret-down" />
      </a-button>
      <div slot="content">
        <div class="first-layer-wrapper">
          <div class="first-layer" v-show="isShowFirstLayer">
            <a-menu style="border-right: none">
              <a-menu-item key="PART" class="dropdown_button" @click="selectMenu(menuOptions.PART)">Country</a-menu-item>
              <a-menu-item key="TOOLING" class="dropdown_button" @click="selectMenu(menuOptions.TOOLING)">Continent</a-menu-item>
            </a-menu>
          </div>
        </div>
      </div>
    </a-popover>
    <a-popover v-model="visible" placement="bottom" trigger="click">
      <div
        slot="content"
        style="padding: 8px 6px;"
        class="dropdown-scroll"
      >
        <div class="content-header" id="back-popup" @click="showFirstLayer">
          Back
        </div>
        <div class="content-body">
          <template v-if="selectedMenu === menuOptions.PART">
            <a-input style="margin-bottom: 10px;" :placeholder=resources["input_search"] v-model.trim="partInput">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style=" color: red; padding: 7px;" :placeholder=resources["select"]></div>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="(item, index) in countryFiltered">
                <a-col style="padding: 7px;" :key="index">
                  <p-radio v-model="checkedcountry" :value="item.code">{{item.title}}</p-radio>
                  <!-- <p-check  :checked="checkedPart(item)" @change="onChangePart($event, item)">{{item.title}}</p-check> -->
                </a-col>
              </template>
            </div>
          </template>
          <template v-else>
            <a-input style="margin-bottom: 10px;" :placeholder=resources["input_search"] v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style=" color: red; padding: 7px;" :placeholder=resources["select"]></div>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="(item, index) in continentFiltered">
                <a-col style="padding: 7px;" :key="index">
                  <p-radio v-model="checkedContinent" :value="item.code">{{item.title}}</p-radio>
                  <!-- <p-radio :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">{{item.title}}</p-radio> -->
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
    optionArrayCountrycode: [Array, Object],
    optionArrayContinentname: [Array, Object],
    query: Function,
    frequent: String,
    resources: Object
  },
  data() {
    return {
      checkedListPart: [],
      oldCheckedListPart: [],
      partInput: "",
      checkedProductivity: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      isShowFirstLayer: false,
      menuOptions: {
        PART: 'Part',
        TOOLING: 'Tooling'
      },
      selectedMenu: 'Part',
      checkedTooling: null,
      checkedContinent: null,
      checkedcountry: null,
      titleShow: 'Search by Country/Continent'
    };
  },
  methods: {
    showFirstLayer(){
      this.isShowFirstLayer = true;
      this.visible = false;
    },
    selectMenu(menu) {
      this.visible = true;
      this.isShowFirstLayer = false;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu){
      return this.selectedMenu === menu;
    }
  },
  computed: {
    countryFiltered() {
      const str = this.partInput;
      if (!str) {
        return this.optionArrayCountrycode;
      }
      var options = {
        keys: ["title"]
      };
      var searcher = new Fuse(this.optionArrayCountrycode, options);
      return searcher.search(str);
    },
    continentFiltered() {
      const str = this.toolingInput;
      if (!str) {
        return this.optionArrayContinentname;
      }
      var options = {
        keys: ["title"]
      };
      var searcher = new Fuse(this.optionArrayContinentname, options);
      return searcher.search(str);
    },
  },
  watch: {
    checkedcountry: function(value) {
      if (value) {
        const productivity = this.optionArrayCountrycode.filter(productivity => productivity.code === value)[0];
        // let all = 'null'
        // this.$emit('changecontinent', all)
        this.$emit('changcountrycode', productivity.code)
        this.checkedProductivity = [productivity];
        this.titleShow = this.checkedProductivity[0].title
      }
    },
    checkedContinent: function(value) {
      if (value) {
        const productivity = this.optionArrayContinentname.filter(productivity => productivity.code === value)[0];
        // let all = 'null'
        // this.$emit('changcountrycode', all)
        this.$emit('changecontinent', productivity.code)
        this.checkedProductivity = [productivity];
        this.titleShow = this.checkedProductivity[0].title
      }
    }
  },
   mounted() {
    // if (this.optionArrayCountrycode && this.optionArrayCountrycode.length > 0) {
    //   this.checkedcountry = this.optionArrayCountrycode[0].id;
    //   this.checkedProductivity = [this.optionArrayCountrycode[0]];
    // }
    // if (this.optionArrayContinentname && this.optionArrayContinentname.length > 0) {
    //   this.checkedContinent = this.optionArrayContinentname[0].id;
    //   this.checkedProductivity = [this.optionArrayContinentname[0]];
    // }
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
.ant-btn:focus {
  color: #fff !important;
  fill: #fff !important;
  background-image: linear-gradient(to right, #414ff7, #6a4efb) !important;
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