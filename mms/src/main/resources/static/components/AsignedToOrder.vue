<template>
  <div class="parts-filter">
    <div :class="{'none-select': isShowFirstLayer}" class="custom-icon-filter">
      <img :class="{'d-none': isShowFirstLayer && checkedListPart.length === 0}" src="/images/icon/user-request.png" alt="">
      <a-button
          @click.stop="showFirstLayer"
          class="dropdown_button dropdown_button-ab"
          :class="{'d-none': isShowFirstLayer && checkedListPart.length === 0}"
      >
        <div v-if="checkedListPart.length > 0" style="width: 200px;overflow-y: scroll;padding-right: 20px" class="d-flex align-items-center">
          <div v-for="(item, index) in checkedListPart" class="d-flex align-items-center">
            <div style="background: #E8E8E8;font-size: 8px;color: #4B4B4B;padding: 3px 4px 2px 4px;margin-right: 3px;border-radius: 1.7px;">
              {{ item.name }}
            </div>
            <div v-if="!isShowFirstLayer" @click.stop="removeItem(item)" style="background: #E8E8E8;font-size: 8px;font-weight: bold;color: #4B4B4B;padding: 3px 4px 2px 4px;margin-right: 7px;border-radius: 1.7px;">
              <img style="max-width: 100%;min-width: 7px;width: 7px;width: 7px;height: auto;margin: 0;" src="/images/icon/close-request.png" alt="">
            </div>
          </div>
          <div v-if="isShowFirstLayer" @click.stop="done()" class="search-button">
            <a
                href="javascript:void(0)"
                id="done"
                class="btn-custom btn-custom-primary"
                style="font-size: 9px;padding: 2px 4px 1px 4px"
                type="primary"
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
    <!-- <div v-if="!isShowFirstLayer && checkedListPart.length !== 0" class="avatar-zone">
      <div v-for="(item, index) in checkedListPart" class="avatar-item-w">
        <a-tooltip placement="bottom">
          <template slot="title">
            <div style="padding: 6px;font-size: 13px;">
              <div><b>{{item.name}}</b></div>
              <div>
                Company: <span v-if="item.company">{{ item.company.name }}</span>
              </div>
              <div>
                Department: {{ item.department }}
              </div>
              <div style="margin-bottom: 15px">
                Position: {{ item.position }}
              </div>
              <div>
                Email: {{ item.email }}
              </div>
              <div>
                Phone: {{ item.mobileNumber }}
              </div>
            </div>
          </template>
          <div class="avatar-item" :style="{ 'background-color': getRequestedColor(item.name,item.id)}">
            {{ convertName(item.name) }}
          </div>
        </a-tooltip>
      </div>
    </div> -->
    <a-popover placement="bottomLeft" trigger="click" v-model="isShowFirstLayer">
      <template
          slot="content"
          style="padding: 8px 6px;max-width: 250px;"
          class="dropdown-scroll"
      >
        <div style="max-width: 220px;overflow: scroll;" class="content-body card-body-custom">
          <template>
            <a-input v-model="partInput2" style="margin-bottom: 10px;" placeholder="Search worker">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style="max-height: 150px; overflow-y: auto;">
              <template v-for="(item, index) in partFiltered">
                <a-col style="padding: 0 7px;" :key="item.id">
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
    optionArrayPart: Array,
    usersSelected: Array,
    id: Number,
    // getRequestedColor: Function
  },
  data() {
    return {
      checkedListPart: [],
      partInput2: "",
      isShowFirstLayer: false,
      selectedMenu: 'Part',
      isSelect: false
    };
  },
  mounted () {
    this.checkedListPart = this.usersSelected
  },
  methods: {
    getRequestedColor(name, id){
      return Common.getRequestedColor(name, id)
    },
    convertName (name) {
      var split_names = name.trim().split(" ");
      if (split_names.length > 1) {
        return (split_names[0].charAt(0) + split_names[split_names.length - 1].charAt(0));
      }
      return split_names[0].charAt(0);
    },
    showFirstLayer(){
      this.isShowFirstLayer = true;
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
    onMouseLeavePopup: function() {
      this.oldCheckedListPart = this.checkedListPart;
      this.changePart(this.id, this.checkedListPart)
    },
    removeItem (item) {
      this.checkedListPart = this.checkedListPart.filter(
          value => value.id !== item.id
      );
      this.onMouseLeavePopup()
    },
    check (result) {
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
      const str = this.partInput2;
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
<style>
.ant-popover-placement-bottomLeft {
  padding-top: 0!important;
  transform: translate(0, -3px)
}
</style>
<style scoped>
.custom-icon-filter {
  width: 250px;
  margin: unset;
}
.custom-icon-filter img {
  margin: auto 7px;
}
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
  min-width: 222px;
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
.none-select {
  height: 32px;
  background: #fff!important;
}
.none-select .dropdown_button-ab {
  width: 250px;
}
.dropdown_button-ab {
  background: #fff!important;
}
.avatar-zone {
  display: flex;
  align-items: center;
  width: 275px!important;
  padding: 7px 10px!important;
  overflow: scroll;
  margin-top: 7px!important;
  background-image: url("data:image/svg+xml,%3csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3e%3crect width='100%25' height='100%25' fill='none' stroke='%232342' stroke-width='4' stroke-dasharray='10' stroke-dashoffset='5' stroke-linecap='round'/%3e%3c/svg%3e")!important;
}
.avatar-item {
  width: 33px;
  height: 33px;
  padding: 1px;
  font-weight: 500;
  font-size: 15px;
  background: #AEACF9;
  border-radius: 99%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: capitalize;
}
.avatar-item-w:not(:last-child) {
  margin-right: 7px;
}
</style>
