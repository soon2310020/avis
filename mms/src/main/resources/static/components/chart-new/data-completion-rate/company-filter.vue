<template>
  <div style="margin-right: 10px">
    <a-dropdown :trigger="['click']" v-model="isShowFirstLayer">
      <a
        id="completion-company-btn"
        :class="{disable : data.length <= 1}"
        @click="showAnimation()"
        class="dropdown_button-custom"
        style="width: 132px"
      >
        <span class="truncate" style="width: 110px;">{{ titleShow() }}</span>
        <div>
          <a-icon class="caret" :class="{'selected-dropdown-text' : isShowFirstLayer, 'caret-show' : displayCaret}" type="caret-down" />
        </div>
      </a>

      <a-menu
        v-if="data.length > 1"
        :key="renderKey"
        slot="overlay"
        class="risk-dropdown elipse-dropdown"
        :defaultSelectedKeys="'all'"
        style="border-right: unset!important;max-height: 250px;overflow: auto; max-width: 132px;"
      >
        <a-menu-item
          key="all"
          @click="selectMenu(null)"
          :class="{'selected-dropdown-item': selectedMenu === 'All'}"
        >
          All
        </a-menu-item>
        <a-menu-item
          v-for="(item, index) in data"
          v-if="item"
          :key="index"
          @click="selectMenu(item)"
          :class="{'selected-dropdown-item': selectedId === item.id}"
        >
          <a-tooltip placement="left">
            <template slot="title">
              <span v-if="item.isShow">{{ `${item.name}` }}</span>
            </template>
            <span :id="`label-company-${index}`" class="truncate-content">{{ item.name }}</span>
          </a-tooltip>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
  </div>
</template>

<script>
module.exports = {
  props: {
    data: Array,
    page: Number,
    resources: Object,
    changeSelect: Function,
    defaultCompanyId: String
  },
  data() {
    return {
      visible: false,
      isShowFirstLayer: false,
      selectDateOpening: false,
      selectedMenu: "All",
      renderKey: 0,
      animationTimeout: null,
      displayCaret: false,
      caretTimeout: null,
      isLoadedData: false,
      selectedId: null
    };
  },
  methods: {
    // animationBtn() {
    //   const el = document.getElementById("completion-btn");
    //   el.classList.add("primary-animation-completion");
    //   // this.getCurrentUser();
    //   console.log(this.data);
    //   console.log("USER DATA");
    //   console.log(vm.currentUser);
    //   setTimeout(() => {
    //     el.classList.remove("primary-animation-completion");
    //   }, 700);
    // },
    titleShow: function () {
      if (this.data.length > 1) {
        if (this.selectedMenu == "All") {
          return "Company - " + this.selectedMenu;
        } else {
          return this.selectedMenu;
        }
      } else if(this.data.length==1){
        return this.data[0].name;
      } else return "";
    },
    showFirstLayer() {
      this.isShowFirstLayer = true;
      this.visible = false;
    },
    selectMenu(menu) {
      console.log(menu, "menu");
      // this.isShowFirstLayer = false;
      if (menu) {
        this.selectedMenu = menu.name;
        this.selectedId = menu.id;
        this.changeSelect(menu.id);
      } else {
        this.selectedMenu = "All";
        this.selectedId = null;
        this.changeSelect("");
      }
      this.displayCaret = true;
    },
    // isShowToolTip(index) {
    //   console.log(`${index} - ${this.data[index].isShow} - hi`)
    //   return this.data[index].isShow;
    // },
    isEllipsisActive() {
      setTimeout(() => {
        for (let index = 0; index < this.data.length; index++) {
          let itemNode = document.getElementById(`label-company-${index}`);
          console.log;
          if (itemNode != null) {
            if (itemNode.offsetWidth > 193) {
              this.data[index].isShow = true;
            } else {
              this.data[index].isShow = false;
            }
          }
          console.log(`${index} - ${this.data[index].isShow}`);
        }
        this.renderKey++;
      }, 50);
    },
    getCurrentUser() {
      try {
        const me = await Common.getSystem('me')
        vm.currentUser = JSON.parse(me)
      } catch (error) {
        console.log(error)
      }
    },
    showAnimation (){
                const el = document.getElementById('completion-company-btn')
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
                const el = document.getElementById('completion-company-btn')
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
      console.log('setDefaultFilter defaultCompanyId', this.defaultCompanyId)
      if(!this.isLoadedData && this.defaultCompanyId && this.data.length > 0) {
        this.isLoadedData = true;
        const company = this.data.filter(companyData => companyData.id === +this.defaultCompanyId);
        if(company.length > 0) {
          this.selectedMenu = company[0].name;
          this.selectedId = company[0].id;
          // this.changeSelect(company[0].id)
        } else {
          this.selectedMenu = 'All';
          this.selectedId = null
        }
      }
      if (watchData && !this.data.some(companyData => companyData.id === +this.selectedId) && this.selectedId) {
        this.selectedMenu = 'All';
        this.selectedId = null
        this.changeSelect('');
      }

    }
  },
  mounted() {
    this.getCurrentUser();
    this.setDefaultFilter();
  },
  computed: {},
  watch: {
    data(newVal) {
      if(newVal.length > 0) {
        this.setDefaultFilter(true)
      } else {
        if (this.selectedMenu === "Company") {
          this.selectedMenu = this.data[0].name;
          this.selectedId = this.data[0].id;
          this.changeSelect(this.data[0].id);
        }
      }

    },
    isShowFirstLayer(newValue) {
      if (newValue) {
        this.isEllipsisActive();
      }
      if (!this.isShowFirstLayer) {
                    this.closeAnimation();
                }
    },
    defaultCompanyId(newVal) {
      this.setDefaultFilter();
    }
  },
};
</script>
<style>
.truncate {
        width: 100px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
}
.truncate-content {
  width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
<style scoped>
.primary-animation-completion {
  outline: 2px solid #98d1fd !important;
  border: 1px solid #98d1fd !important;
  color: #0075ff;
}
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
.disable{
  pointer-events: none;
}

</style>
