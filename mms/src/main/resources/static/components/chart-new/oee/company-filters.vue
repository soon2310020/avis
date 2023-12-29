<template>
  <div>
    <a-dropdown   :trigger="['click']" v-model="isShowFirstLayer">
      <a :id="'company-filters-' + id" :class="{'company-dropdown' : type === 'COMPANY'}" href="javascript:void(0)" style="font-size: 14px;" @click="showAnimation()" class="dropdown_button-custom">
        <span class="truncate" style="cursor: pointer" :class="{'selected-dropdown-text' : isShowFirstLayer}">{{titleShow}}</span>
        <div>
          <a-icon class="caret" :class="{'selected-dropdown-text' : isShowFirstLayer, 'caret-show' : displayCaret}" type="caret-down" />
        </div>
      </a>
<!--      <div style="padding: 0px!important;" slot="content">-->
<!--        <div class="risk-level-btn first-layer-wrapper" style="margin: -4px;">-->
<!--          <div class="first-layer" v-show="isShowFirstLayer">-->
            <a-menu slot="overlay" class="risk-dropdown" :defaultSelectedKeys="type === 'COMPANY' ? ['0'] : 'all'" style="border-right: unset!important;max-height: 250px;overflow: auto;">
              <a-menu-item
                  v-if="type === 'LINE'"
                  key="all"
                  :class="{'selected-dropdown-item': selectedMenu === 'All'}"
                  @click="setChangeData();selectMenu(null)"
              >
                All
              </a-menu-item>
              <a-menu-item v-for="(item, index) in data" :key="index" @click="setChangeData();selectMenu(item)"
                           :class="{'selected-dropdown-item': type === 'COMPANY' ? selectedMenu === item.name : selectedMenu === item}">
                <label v-if="type === 'COMPANY'" class="truncate-company-content">{{ item.name }}</label>
                <label v-if="type === 'LINE'" class="truncate-company-content">{{ item }}</label>
              </a-menu-item>
            </a-menu>
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
    </a-dropdown>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    data: Array,
    page: Number,
    resources: Object,
    changeSelect: Function,
    // showDetaisl: String
    id:String,
    defaultDataId: String,
    setChangeData: Function
  },
  data() {
    return {
      isShowFirstLayer: false,
      selectDateOpening: false,
      selectedMenu: 'All',
      animationTimeout: null,
      displayCaret: false,
      caretTimeout: null,
    };
  },
  methods: {
/*
    titleShow: function() {
      return this.selectedMenu
    },
*/
    showFirstLayer(){
      this.isShowFirstLayer = true;
    },
    selectMenu(menu) {
      console.log(menu, 'menu')
      // this.isShowFirstLayer = false;
      // if (menu) {
      if (this.type === 'COMPANY') {
        this.selectedMenu = menu.name;
        this.changeSelect(menu.id, this.type)
      } else {
        this.selectedMenu = menu ? menu : 'All';
        console.log(this.selectedMenu, 'this.selectedMenu')
        this.changeSelect(menu, this.type)
      }
      // } else {
      //   this.selectedMenu = 'All';
      // }
      this.displayCaret = true;
    },
    showAnimation (){
                const el = document.getElementById('company-filters-'+ this.id)
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
            closeAnimation(){
                console.log('Close')
                const el = document.getElementById('company-filters-'+ this.id)
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
  },

  computed: {
    titleShow: function() {
      return this.selectedMenu
    },
  },
  watch: {
    data (newVal) {
      if (this.type === 'COMPANY' && this.selectedMenu === 'Company') {
        console.log('this.defaultDataId'+this.defaultDataId);
        const defaultData = this.data.filter(company => company.id === +this.defaultDataId)[0];
        console.log('defaultData'+JSON.stringify(defaultData));
        this.selectedMenu = defaultData ? defaultData.name : this.data[0].name
        this.changeSelect(defaultData ? defaultData.id : this.data[0].id, this.type)
      } else if (this.type === 'LINE' && this.selectedMenu === 'Line') {
        this.selectedMenu = this.defaultDataId ? this.defaultDataId : 'All';
      }else
      if (this.type === 'COMPANY' && !this.data.map(d=>d.name).includes(this.selectedMenu)) {
        this.selectedMenu = this.data.length>0 ? this.data[0].name : 'Company';
      }
    },
    isShowFirstLayer: function(oldValue, newValue) {
                if (!this.isShowFirstLayer) {
                    this.closeAnimation();
                }
            }
  },
  // showDetaisl (newVal) {
  //   if (newVal && this.type === 'LINE') {
  //     this.selectedMenu = newVal
  //   }
  // },
  created () {
    this.selectedMenu = this.type === 'COMPANY' ? 'Company' : 'Line'
  }
};
</script>
<style>
.company-dropdown{
  width: 120px;
}
.ant-dropdown-menu-item{
  max-width: 100%;
}
.truncate-company-content {
  width: 102px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
label{
  margin-bottom: -4px;
  line-height: 100%;
}
.unset-color-hover:hover{
  border: 1px solid #f2f5fa;
  background-color: #f2f5fa;
}
</style>
<style scoped>
.ant-dropdown-trigger1 {
  border-radius: 0px;
  border: none;
  padding: 5px 0px !important;
  margin-right: 20px;
  border-radius: 7px;
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  border: none;
  align-items: center;
  justify-content: space-between;
}

.ant-dropdown-trigger1:last-child {
  border-radius: 0px;
  border: none;
  padding: 5px 0px !important;
  margin-right: 0px;
  border-radius: 7px;
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  border: none;
  align-items: center;
  justify-content: space-between;
}

.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  min-width: 80px;
  background: #f2f5fa;
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

.dropdown_button-custom{
  width: 120px;
}
</style>
