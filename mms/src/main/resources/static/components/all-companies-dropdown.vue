<template>
  <div class="dropdown-dashboard-filter btn-group btn-group-select-column all-time-filters">
    <a v-if="optionArrayTooling.length === 1" href="javascript:void(0)" class="btn-custom btn-outline-custom-primary all-time-filters">
      <span class="title-show">{{ titleShow }}</span>
      <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{'caret-show' : displayCaret}" alt="">
    </a>
    <a-popover v-else v-model="visible" placement="bottom" trigger="click">
      <a @click="openDropdown" :class="{'animation-secondary': showAnimation}" href="javascript:void(0)" class="btn-custom btn-outline-custom-primary all-time-filters">
        <span class="title-show">{{ titleShow }}</span>
        <img class="img-transition" src="/images/icon/icon-cta-blue.svg" :class="{'caret-show' : displayCaret}" alt="">
      </a>

      <div
          slot="content"
          style="padding: 8px 6px; max-height: 300px; overflow-y: scroll"
          class="dropdown-scroll"
      >
        <a-input
            style="margin-bottom: 10px"
            placeholder="Search company"
            v-model.trim="toolingInput"
        >
          <a-icon slot="prefix" type="search" />
        </a-input>
        <a-col style="padding: 7px">
          <p-check
              @change="selectAll($event)"
              :checked="checkedAll"
          >All companies</p-check>
        </a-col>
        <template v-for="item in toolingFiltered">
          <a-col style="padding: 7px" :key="item.index">
            <p-check :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">
              {{ item.name }}
            </p-check>
          </a-col>
        </template>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    optionArrayTooling: Array,
    query: Function,
    // frequent: String,
    resources: Object,
    companySelectedList: String,
  },
  data() {
    return {
      checkedListTooling: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      dropdownOpening: false,
      displayCaret: false,
      showAnimation: false
    };
  },
  methods: {
    animationOutline () {
      this.displayCaret = true;
      if (!this.visible) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700)
      }
    },
    selectAll (isChecked) {
      if (isChecked) {
        this.checkedListTooling = this.optionArrayTooling
      } else {
        return this.checkedListTooling = [];
      }
    },
    changeDropdown() {
      this.dropdownOpening = false
    },
    openDropdown () {
      // const self = this
      this.displayCaret = true;
      if (!this.visible) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700)
      }
      // if (this.dropdownOpening === true) {
      //   this.dropdownOpening = false
      // } else {
      //   this.dropdownOpening = true
      // }
    },
    onChangeTooling: function(isChecked, item) {
      if (isChecked) {
        this.checkedListTooling = this.checkedListTooling.concat(item);
      } else {
        this.checkedListTooling = this.checkedListTooling.filter(
            value => value.id !== item.id
        );
      }
    },
    selectMenu(menu) {
      this.visible = true;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu){
      return this.selectedMenu === menu;
    },
    checkedTooling: function(item) {
      const findIndex = this.checkedListTooling.findIndex(
          value => value.id == item.id
      );
      return findIndex !== -1;
    },
    onMouseleaveTooling: function(type) {
      const itemJoined = this.checkedListTooling.map((res) => res.id).join(',')
      console.log(this.type)
      if (type == 'Reset') {
        console.log(1)
        this.query(itemJoined);
      } else {
        if (itemJoined !== this.companySelectedList) {
          console.log(2)
          this.query(itemJoined);
        }
      }
    }
  },

  computed: {
    checkedAll () {
      return this.checkedListTooling.length === this.optionArrayTooling.length;
    },
    toolingFiltered() {
      const str = this.toolingInput.trim();
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
    titleShow: function () {
      if (this.optionArrayTooling.length === 1) {
        return this.optionArrayTooling[0].name
      } else if (this.checkedListTooling.length === 0 || this.checkedListTooling.length === this.optionArrayTooling.length) {
        return 'All Companies'
      }
      return this.checkedListTooling.length <= 1 ? this.checkedListTooling.length + ' Company' : this.checkedListTooling.length + ' Companies';
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseleaveTooling("");
        this.displayCaret = false;
      }
    }
  }
  // mounted() {
  //   if (this.optionArrayTooling && this.optionArrayTooling.length > 0) {
  //     this.checkedListTooling = [this.optionArrayTooling[0]];
  //     this.onMouseleaveTooling();
  //   }
  // }
};
</script>
<style src="../css/companies-dropdown.css"></style>
<style>
.companies-item:hover {
  color: rgb(112, 112, 112) !important;
  background-color: #EAF4FF !important;
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
.title-show{
  display: inline-block;
  width: 97px;
}
</style>
