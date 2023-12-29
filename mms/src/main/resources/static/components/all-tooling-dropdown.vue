<template>
  <div class="tooling-filter companies-dropdown">
    <a-dropdown v-model="visible" class="all-companies-dropdown" :trigger="['click']">
      <a style="border: 1px solid #909090;color: #707070;font-weight: 400" href="javascript:void(0)" @mouseover="handleMouseOver()" @mouseleave="handleMouseOut()" :class="{'active-dropdown': visible}" id="dropdown-parent" class="btn-custom btn-custom-order">
        {{ titleShow }}
        <svg v-show="visible" xmlns="http://www.w3.org/2000/svg" width="11.475" height="6.238" viewBox="0 0 11.475 6.238">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l5.031,5.031L19.061,13.5" transform="translate(19.768 19.031) rotate(180)" fill="none" stroke="#3491ff" stroke-linecap="round" stroke-linejoin="round" stroke-width="1"/>
        </svg>
        <svg v-show="over && !visible" xmlns="http://www.w3.org/2000/svg" width="11.475" height="6.238" viewBox="0 0 11.475 6.238">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l5.031,5.031L19.061,13.5" transform="translate(-8.293 -12.793)" fill="none" stroke="#3491ff" stroke-linecap="round" stroke-linejoin="round" stroke-width="1"/>
        </svg>
<!--        <svg v-show="!visible" xmlns="http://www.w3.org/2000/svg" width="11.475" height="6.238" viewBox="0 0 11.475 6.238">-->
<!--          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l5.031,5.031L19.061,13.5" transform="translate(-8.293 -12.793)" fill="none" stroke="#909090" stroke-linecap="round" stroke-linejoin="round" stroke-width="1"/>-->
<!--        </svg>-->
        <svg v-show="!visible && !over" class="custom-dropdown" xmlns="http://www.w3.org/2000/svg" width="11.475" height="6.238" viewBox="0 0 11.475 6.238">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l5.031,5.031L19.061,13.5" transform="translate(-8.293 -12.793)" fill="none" stroke="#909090" stroke-linecap="round" stroke-linejoin="round" stroke-width="1"/>
        </svg>
        <svg v-show="!visible && !over" class="custom-dropdown-hover" xmlns="http://www.w3.org/2000/svg" width="11.475" height="6.238" viewBox="0 0 11.475 6.238">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l5.031,5.031L19.061,13.5" transform="translate(-8.293 -12.793)" fill="none" stroke="#909090" stroke-linecap="round" stroke-linejoin="round" stroke-width="1"/>
        </svg>
      </a>
      <div class="companies-dropdown-child" slot="overlay">
        <div style="margin-bottom: 10px;padding: 7px 5px 0 6px;">
          <a-input placeholder="Input search text" v-model="toolingInput">
            <a-icon slot="prefix" type="search"/>
          </a-input>
        </div>
        <template v-if="typeName != 'CATEGORY' && typeName != 'PART'">
            <a-menu style="max-height: 168px;max-width: 230px;overflow-y: auto;" class="dashboard-drawer">
              <a-menu-item style="background-color: unset!important;color: #4B4B4B!important; padding: 0px;" class="companies-item" v-for="item in toolingFiltered" :class="{'selected': checkedTooling(item)}" :key="item.id">
                <p-check :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">
                      <span style="font-size: 11px;margin-left: 23px;">
                        {{ item.name }}
                      </span>
                </p-check>
              </a-menu-item>
            </a-menu>
        </template>
        <template v-else-if="typeName == 'PART'">
            <a-menu style="max-height: 168px;max-width: 230px;overflow-y: auto;" class="dashboard-drawer">
              <a-menu-item style="background-color: unset!important;color: #4B4B4B!important; padding: 0px;" class="companies-item" v-for="item in toolingFiltered" :class="{'selected': checkedTooling(item)}" :key="item.id">
                <p-check :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">
                      <span style="font-size: 11px;margin-left: 23px;">
                        {{ item.code }}
                      </span>
                </p-check>
              </a-menu-item>
            </a-menu>
        </template>
        <template v-else>
          <a-menu style="max-height: 168px;max-width: 230px;overflow-y: auto;" class="dashboard-drawer">
            <template v-for="item in toolingFiltered">
              <a-menu-item v-if="item.data.level == 1" style="background-color: unset!important;color: #4B4B4B!important; padding: 0px;" class="companies-item" :class="{'selected': checkedTooling(item)}" :key="item.id">
                <p-check :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">
                      <template v-if="item.data.level > 1">
                          <span style="font-size: 11px; margin-left: 23px;">
                            <span> ㄴ </span> {{ item.name }}
                          </span>
                      </template>
                      <span v-else style="font-size: 11px;margin-left: 23px;">
                        {{ item.name }}
                      </span>
                </p-check>
              </a-menu-item>
              <template v-if="item.data.level == 1 && item.data.children.length > 0">
                <a-menu-item v-for="subItem in item.data.children" style="background-color: unset!important;color: #4B4B4B!important; padding: 0px;" class="companies-item" :class="{'selected': checkedTooling(item)}" :key="subItem.id"
                v-if="checkContainCategory(subItem)">
                  <p-check :checked="checkedTooling(subItem)" @change="onChangeTooling($event, subItem)">
                        <span style="font-size: 11px; margin-left: 23px;">
                          <span> ㄴ </span> {{ subItem.name }}
                        </span>
                  </p-check>
                </a-menu-item>
              </template>
            </template>
            </a-menu>
        </template>
      </div>
    </a-dropdown>
  </div>
</template>

<script>
module.exports = {
  props: {
    optionArrayTooling: Array,
    query: Function,
    resources: Object,
    companySelectedList: Array,
    typeName: String
  },
  data() {
    return {
      checkedListTooling: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
      dropdownOpening: false,
      over: false,
    };
  },
  methods: {
    selectAll () {
      this.checkedListTooling = this.optionArrayTooling
    },
    animationOutline () {
      const el = document.getElementById('dropdown-parent-companies')
      el.classList.add('outline-animation')
      setTimeout(() => {
        el.classList.remove('outline-animation')
      }, 700)
    },
    changeDropdown() {
      this.dropdownOpening = false
    },
    openDropdown () {
      const self = this
      setTimeout(() => {
        self.animationOutline()
      }, 50)
      if (this.dropdownOpening === true) {
        this.dropdownOpening = false
      } else {
        this.dropdownOpening = true
      }
    },
    onChangeTooling: function(isChecked, item) {
      if (this.typeName != 'CATEGORY') {
        if (isChecked) {
          this.checkedListTooling = this.checkedListTooling.concat(item);
        } else {
          this.checkedListTooling = this.checkedListTooling.filter(
              value => value.id !== item.id
          );
        }
      } else {
        console.log('item', item)
        let level = item.data ? item.data.level : item.level
        if (isChecked) {
            if (level == 1) {
              this.checkedListTooling = this.checkedListTooling.concat(item);
            } else if (level > 1){
              let flag = 0;
              let parent = this.optionArrayTooling.filter(el => el.id == item.parent)[0]
              if (parent) {
                console.log('parent - checked',parent)
                parent.data.children.forEach(element => {
                  if (this.checkedTooling(element)) {
                    flag++;
                  }
                });
                if (flag == parent.data.children.length - 1) {
                  parent.data.children.forEach(element => {
                    if (this.checkedTooling(element)) {
                      this.checkedListTooling = this.checkedListTooling.filter(
                          value => value.id !== element.id
                      );
                    }
                    this.checkedListTooling = this.checkedListTooling.concat(this.optionArrayTooling.filter(el => el.id == item.parent)[0]);
                  });
                } else {
                  this.checkedListTooling = this.checkedListTooling.concat(item);
                }
              } else {
                this.checkedListTooling = this.checkedListTooling.concat(item);
              }
            }
          
        } else {
            if (level == 1) {
              this.checkedListTooling = this.checkedListTooling.filter(
                value => value.id !== item.id
              );
            } else if (level > 1){
              let parent = this.optionArrayTooling.filter(el => el.id == item.parent)[0]
              if (parent) {
                console.log('parent', parent)
                if (this.checkedTooling(parent)) {
                  parent.data.children.forEach(element => {
                    if (element.id != item.id) {
                      this.checkedListTooling = this.checkedListTooling.concat(element);
                    }
                  });
                  this.checkedListTooling = this.checkedListTooling.filter(
                    value => value.id !== item.id
                  );
                } else {
                  this.checkedListTooling = this.checkedListTooling.filter(
                    value => value.id !== item.id
                  );
                }
              } else {
                this.checkedListTooling = this.checkedListTooling.filter(
                    value => value.id !== item.id
                  );
              }
              
            }
          
        }
        
      }
      console.log('checkedListTooling',this.checkedListTooling)
    },
    selectMenu(menu) {
      this.visible = true;
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu){
      return this.selectedMenu === menu;
    },
    checkedTooling: function(item) {
      console.log('item - check', item)
      if (this.typeName != 'CATEGORY') {
        const findIndex = this.checkedListTooling.findIndex(
          value => value.id == item.id
        );
        return findIndex !== -1;
      } else {
        let level = item.data ? item.data.level : item.level
        if (level == 1) {
          const findIndex = this.checkedListTooling.findIndex(
            value => value.id == item.id
          );
          return findIndex !== -1;
        } else if(level > 1){
          let parent = this.optionArrayTooling.filter(el => el.id == item.parent)[0]
          if (parent) {
            console.log('parent check', parent, item)
            if (this.checkedTooling(parent)) {
              return true;
            } else {
              const findIndex = this.checkedListTooling.findIndex(
                value => value.id == item.id
              );
              return findIndex !== -1;
            }
          }
          
        }
      }
      
    },
    onMouseleaveTooling: function() {
      if (this.checkedListTooling !== this.companySelectedList) {
        this.query(this.checkedListTooling);
      }
    },
    handleMouseOver(){
      this.over = true;
    },
    handleMouseOut(){
      this.over = false;
    },
    checkContainCategory(item){
      if(this.toolingFiltered){
       return this.toolingFiltered.map(t=>t.id).includes(item.id);
      }
    }
  },

  computed: {
    toolingFiltered() {
      console.log('typeName', this.typeName)
      console.log('companySelectedList', this.companySelectedList)
      console.log('optionArrayTooling', this.optionArrayTooling)
      const str = this.toolingInput;
      if (str) {
        var options
        if (this.typeName == 'PART') {
          options = {
            threshold: 0,
            location: 0,
            distance: 100,
            keys: ["code"]
          };
        } else {
          options = {
            threshold: 0,
            location: 0,
            distance: 100,
            keys: ["name"]
          };
        }
        
        var searcher = new Fuse(this.optionArrayTooling, options);
        return searcher.search(str).sort((firstItem, secondItem) => firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1);
      } else {
        return this.optionArrayTooling;
      }
    },
    titleShow: function () {
      // if (this.checkedListTooling.length === 0) {
      // return 'Add ' + this.typeName.toUpperCase() + this.typeName.slice(1);
      return 'Add ' + this.typeName.slice(0, 1) + this.typeName.slice(1).toLocaleLowerCase()
      // }
      // return this.checkedListTooling.length + ' Tooling';
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseleaveTooling();
      }
    },
    companySelectedList (newVal) {
      this.checkedListTooling = newVal
    }
  },
  mounted() {
    this.checkedListTooling = this.companySelectedList
    console.log(this.companySelectedList, 'this.companySelectedListthis.companySelectedList')
    // if (this.optionArrayTooling && this.optionArrayTooling.length > 0) {
    //   this.checkedListTooling = [this.optionArrayTooling[0]];
    //   this.onMouseleaveTooling();
    // }
  }
};
</script>
<style>
.pretty .state label{
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  width: 180px;
}
</style>
<style src="../css/companies-dropdown.css"></style>
<style scoped>
.btn-custom-order:hover {
  background: #DEEDFF!important;
  border: 1px solid transparent!important;
  color: #3491FF!important;
}
.active-dropdown {
  color: #3491FF!important;
  outline: 2px solid #98D1FD;
  border: 1px solid transparent !important;
  /* background: #DEEDFF!important; */
}
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
