<template>
  <div>
    <a-dropdown :trigger="['click']" v-model="isShowFirstLayer">
      <a
      id="completion-data-btn"
        class="dropdown_button-custom"
        @click="showAnimation()"
        style="width: 132px"
      >
        <span class="truncate">{{ titleShow() }}</span>
        <div>
          <a-icon class="caret" :class="{'selected-dropdown-text' : isShowFirstLayer, 'caret-show' : displayCaret}" type="caret-down" />
        </div>
      </a>
      <a-menu
        slot="overlay"
        class="risk-dropdown"
        :defaultSelectedKeys="'all'"
        style="
          border-right: unset !important;
          max-height: 193px;
          overflow: auto;
        "
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
          :key="index"
          @click="selectMenu(item)"
          :class="{'selected-dropdown-item': selectedMenu === item.name}"
        >
          <span>{{ item.name }}</span>
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
    defaultObjectType: String
  },
  data() {
    return {
      visible: false,
      isShowFirstLayer: false,
      selectDateOpening: false,
      selectedMenu: "All",
      animationTimeout: null,
      displayCaret: false,
      caretTimeout: null,
      isLoadedData: false
    };
  },
  methods: {
    titleShow: function () {
      return "Data - " + this.selectedMenu;
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
        this.changeSelect(menu.code);
      } else {
        this.selectedMenu = "All";
        this.changeSelect("");
      }
      this.displayCaret = true;
    },
    showAnimation (){
                const el = document.getElementById('completion-data-btn')
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
                const el = document.getElementById('completion-data-btn')
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
    setDefaultFilter() {
      console.log('setDefaultFilter defaultObjectType', this.defaultObjectType);
      if(!this.isLoadedData && this.data.length > 0 && this.defaultObjectType) {
        const objectType = this.data.filter(item => item.code === this.defaultObjectType);
        if(objectType.length > 0) {
          this.selectedMenu = objectType[0].name
          // this.changeSelect(objectType[0].code)
        } else {
          this.selectedMenu = 'All';
        }
      }
    }
  },

  computed: {},
  watch: {
    data(newVal) {
      if(newVal.length > 0 && !this.isLoadedData) {
        this.setDefaultFilter();
      } else {
        if (this.selectedMenu === "Company") {
          this.selectedMenu = this.data[0].name;
          this.changeSelect(this.data[0].id);
        }
      }

    },
    isShowFirstLayer(newValue) {
      if (!this.isShowFirstLayer) {
                    this.closeAnimation();
                }
    },
  },
  mounted() {
    this.setDefaultFilter();
  }
};
</script>
<style>
/*.ant-popover-inner-content {*/
/*  padding: 0!important;*/
/*}*/
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
</style>
