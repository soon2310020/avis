<template>
  <div class="parts-filter">
    <div style="margin-right: 10px" :class="{'open': isShowFirstLayer && checkedListPart.length <= 0}" class="custom-icon-filter">
      <img src="/images/icon/user-request.png" alt="">
      <a-button
          @click.prevent="showFirstLayer"
          class="dropdown_button dropdown_button-ab"
      >
        <div v-if="checkedListPart.length > 0" style="width: 200px;overflow-y: scroll;" class="d-flex align-items-center">
          <div v-for="(item, index) in checkedListPart" class="d-flex align-items-center">
            <div style="background: #E8E8E8;font-size: 8px;color: #4B4B4B;padding: 3px 4px 2px 4px;margin-right: 3px;border-radius: 1.7px;">
              {{ item.name }}
            </div>
            <div v-if="!isShowFirstLayer" @click.stop="removeItem(item)" style="background: #E8E8E8;font-size: 8px;font-weight: bold;color: #4B4B4B;padding: 3px 4px 2px 4px;margin-right: 7px;border-radius: 1.7px;">
              <img style="max-width: 100%;min-width: 7px;width: 7px;width: 7px;height: auto;margin: 0;" src="/images/icon/close-request.png" alt="">
            </div>
          </div>
          <div v-if="isShowFirstLayer" class="search-button">
            <a
                href="javascript:void(0)"
                id="done"
                class="btn-custom btn-custom-primary"
                style="font-size: 9px;padding: 2px 4px 1px 4px"
                type="primary"
                @click.stop="done()"
            >
              <div v-show="isShowFirstLayer">Done</div>
              <img v-show="!isShowFirstLayer" style="max-width: 100%;width: 10px;height: auto;margin: 0;" src="/images/icon/plus-request.png" alt="">
            </a>
          </div>
          <div v-else class="search-button">
            <a
                href="javascript:void(0)"
                id="plus"
                class="btn-custom btn-custom-primary"
                type="primary"
                @click.stop="plus()"
            >
              <img style="max-width: 100%;width: 10px;height: auto;margin: 0;" src="/images/icon/plus-request.png" alt="">
            </a>
          </div>
        </div>
        <div v-else-if="isShowFirstLayer" style="font-size: 13px;color: #BDC3C8">
          <span>Assign User</span>
          <div v-if="isShowFirstLayer" class="search-button">
            <a
                href="javascript:void(0)"
                style="background: #E8E8E8;border-color: #C1C1C1;color: #909090;font-size: 9px"
                type="primary"
            >
              Done
            </a>
          </div>
        </div>
        <div v-else style="font-size: 13px;color: #BDC3C8">
          Search Registered User
        </div>
      </a-button>
    </div>
    <a-popover placement="left" trigger="click" v-model="isShowFirstLayer">
      <template
          slot="content"
          style="padding: 8px 6px;max-width: 250px;"
          class="dropdown-scroll"
      >
        <div style="max-width: 220px;overflow: scroll;" class="content-body card-body-custom">
          <template>
            <a-input style="margin-bottom: 10px;" placeholder="Search worker" v-model="partInput">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="(item, index) in partFiltered">
                <a-col style="padding: 7px;" :key="item.id">
                  <div @click.stop="check(item)" class="d-inline-flex align-items-center card-body-custom">
                    <input :id="'checkbox-all-' + index" class="checkbox d-none" type="checkbox" :checked="checkedPart(item)" />
                    <label class="custom-control-label custom-control-label-asign"></label>
                    <span style="margin-left: 7px;line-height: 100%;">{{item.name}}</span>
                  </div>
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
    changePart: Function,
    resources: Object,
    optionArrayPart: Array
  },
  data() {
    return {
      checkedListPart: [],
      partInput: "",
      isShowFirstLayer: false,
      selectedMenu: 'Part',
      isSelect: false
    };
  },
  mounted () {
    this.checkedListPart = []
  },
  methods: {
    showFirstLayer(){
      this.isShowFirstLayer = !this.isShowFirstLayer;
    },
    done () {
      this.isShowFirstLayer = false
    },
    plus () {
      this.isShowFirstLayer = true
    },
    selectMenu(menu) {
      this.selectedMenu = menu;
    },
    isSelectedMenu(menu){
      return this.selectedMenu === menu;
    },
    onMouseLeavePopup: function(){
      this.onMouseleavePart();
    },
    onMouseleavePart: function() {
      this.oldCheckedListPart = this.checkedListPart;
      this.changePart(this.checkedListPart)
    },
    removeItem (item) {
      this.checkedListPart = this.checkedListPart.filter(
          value => value.id !== item.id
      );
      this.onMouseleavePart()
    },
    check (result) {
      console.log('comee')
      if (!this.checkedPart(result)) {
        this.checkedListPart = this.checkedListPart.concat(result);
        console.log(this.checkedListPart, 'this.checkedListPart1')
        this.onMouseLeavePopup();
      } else {
        this.checkedListPart = this.checkedListPart.filter(
            value => value.id !== result.id
        );
        console.log(this.checkedListPart, 'this.checkedListPart2')
        this.onMouseLeavePopup();
      }
    },
    checkedPart (item) {
      const findIndex = this.checkedListPart.findIndex(
          value => value.id == item.id
      );
      return findIndex !== -1;
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
  width: unset;
  border: .5px solid #A2A2A2;
  background: #fff;
  /*position: absolute;*/
  border-radius: 4px;
  /*top: 0;*/
  /*right: 0;*/
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
  /*margin-right: -1px;*/
  height: calc(100% + 2px);
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  background: #f2f5fa;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  background: #f2f5fa;
}

/*.down-icon {*/
/*  fill: #564efb;*/
/*}*/

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
  background-color: #5D43F7!important;
  color: #fff!important;
}
.search-button {
  position: absolute;
  top: 50%;
  transform: translate(0, -50%);
  right: 8px;
  /*width: 14pt;*/
  height: 14pt;
}
.search-button a {
  height: 100%;
  line-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  font-weight: bold;
}
</style>
