<template>
  <div>
    <a-dropdown :trigger="['click']" v-model="isShowFirstLayer">
<!--      <div class="risk-level-btn first-layer-wrapper" style="margin: -4px;">-->
<!--        <div class="first-layer" v-show="isShowFirstLayer">-->
          <a-menu @click="selectMenu" :defaultSelectedKeys="['Last Week']" class="risk-dropdown" style="border-right: unset!important;" slot="overlay">
            <a-menu-item @click="setChangeData();handleChangeTime('LAST_WEEK')" :class="{'risk-selected': selectedMenu === 'Last Week'}" key="Last Week" v-text="resources['last_week']"></a-menu-item>
            <a-menu-item @click="setChangeData();handleChangeTime('LAST_MONTH')" :class="{'risk-selected': selectedMenu === 'Last Month'}" key="Last Month" v-text="resources['last_month']"></a-menu-item>
            <a-menu-item @click="setChangeData();handleChangeTime('LAST_YEAR')" :class="{'risk-selected': selectedMenu === 'Last Year'}" key="Last Year" v-text="resources['last_year_cap']"></a-menu-item>
            <a-menu-item key="CUSTOM" :class="{'risk-selected': selectedMenu === ''}" v-text="resources['custom']"></a-menu-item>
          </a-menu>
<!--        </div>-->
<!--      </div>-->
      <a :id="'custom-time-dropdown-' + id" @click="showAnimation()" style="font-size: 14px; width: 120px;" class="dropdown_button-custom ">
        
          <span :class="{'selected-dropdown-text' : isShowFirstLayer}" class="truncate">{{titleShow()}}</span>
        
        <div>
          <a-icon class="caret" :class="{'selected-dropdown-text' : isShowFirstLayer, 'caret-show' : displayCaret}" type="caret-down" />
        </div>
      </a>
<!--      <div style="padding: 0px!important;" slot="content">-->
<!--        <div class="risk-level-btn first-layer-wrapper" style="margin: -4px;">-->
<!--          <div class="first-layer" v-show="isShowFirstLayer">-->
<!--            <a-menu @click="selectMenu" :defaultSelectedKeys="['Last Week']" style="border-right: unset!important;" slot="overlay">-->
<!--              <a-menu-item @click="handleChangeTime('LAST_WEEK')" key="Last Week" v-text="resources['last_week']"></a-menu-item>-->
<!--              <a-menu-item @click="handleChangeTime('LAST_MONTH')" key="Last Month" v-text="resources['last_month']"></a-menu-item>-->
<!--              <a-menu-item @click="handleChangeTime('LAST_YEAR')" key="Last Year" v-text="resources['last_year']"></a-menu-item>-->
<!--              <a-menu-item key="CUSTOM" v-text="resources['custom']"></a-menu-item>-->
<!--            </a-menu>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
    </a-dropdown>
    <a-popover v-if="visible" v-model="visible" placement="bottom" trigger="click">
      <div style="padding: 0px!important;" slot="content" style="width: 285px">
        <div class="date-container date-container-custom">
                  <div style="display: flex;flex-direction: column;">
                    <div style="margin-bottom: 5px;font-weight: 500;" class="text" v-text="resources['from']"></div>
                    <a-date-picker
                        :allow-clear="false"
                        class="date-picker-customize"
                        :default-value="defaultValue"
                        format="MMM Do, YYYY"
                        v-model="startDate"
                    ></a-date-picker>
                  </div>
                  <div class="date-minus">
                    <div></div>
                  </div>
                  <div style="display: flex;flex-direction: column; align-items: flex-end;">
                    <div style="width: 100%;padding-left: 5px;margin-bottom: 5px;font-weight: 500;" class="text" v-text="resources['to']"></div>
                    <a-date-picker
                        :allow-clear="false"
                        class="date-picker-customize"
                        :default-value="defaultValue"
                        format="MMM Do, YYYY"
                        v-model="endTime"
                    ></a-date-picker>
                  </div>

          <!--        :ranges="{ Today: [moment(), moment()], 'This Month': [moment(), moment().endOf('month')] }"-->
        </div>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    page: Number,
    resources: Object,
    changeDate: Function,
    id: String,
    defaultDataFilter: String,
    setChangeData: Function
  },
  data() {
    return {
      startDate: moment(),
      endTime: moment(),
      visible: false,
      isShowFirstLayer: false,
      defaultValue: moment(),
      selectDateOpening: false,
      selectedMenu: '',
      rangePickerValue: '',
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
    };
  },
  computed: {
    getMoment() {
      return moment()
    }
  },
  methods: {
    handleChangeTime: function(type) {
      this.displayCaret = true;
      let startTime, endTime;
      if (type == "LAST_WEEK") {
        startTime = moment()
            .subtract(7, "day")
            .startOf("week");

        endTime = moment()
            .subtract(7, "day")
            .endOf("week");
      } else if (type == "LAST_MONTH") {
        startTime = moment()
            .subtract(30, "day")
            .startOf("month");

        endTime = moment()
            .subtract(30, "day")
            .endOf("month");
      } else if (type == "LAST_YEAR") {
        startTime = moment()
            .subtract(365, "day")
            .startOf("year");

        endTime = moment()
            .subtract(365, "day")
            .endOf("year");
      } else {
        startTime = null;
        endTime = null;
      }
      this.startDate = startTime.endOf('day');
      this.endTime = endTime;
      this.changeDate(
          this.page,
          this.startDate.unix(),
          this.endTime.unix(),
          type
      )
      // this.visible = false;
    },
    showFirstLayer(){
      this.isShowFirstLayer = true;
      this.visible = false;
    },
    selectMenu(menu) {
      console.log(menu, 'menu')
      this.isShowFirstLayer = false;
      if (menu.key === 'CUSTOM') {
        this.visible = true;
        this.selectedMenu = ''
      } else {
        // this.changeDate(menu)
        this.selectedMenu = menu.key;
      }
    },
    titleShow: function() {
      if (!this.startDate || !this.endTime || this.selectedMenu) {
        return this.selectedMenu
      } else {
        return `${this.startDate.format(
            "MMM Do, YYYY"
        )} - ${this.endTime.format("MMM Do, YYYY")}`;
      }
    },
    
    showAnimation (){
                const el = document.getElementById('custom-time-dropdown-'+ this.id)
                if (!this.isShowFirstLayer) {
                    setTimeout(() => {
                      el.classList.add("dropdown_button-animation");
                      this.animationTimeout = setTimeout(() => {
                      el.classList.remove("dropdown_button-animation");
                      el.classList.add("selected-dropdown-text");
                      el.classList.add("selected-dropdown-button");
                      }, 1600);
                    } , 1);
                    
                } else {
                    this.closeAnimation()
                }
            },
            closeAnimation(){
                console.log('Close')
                const el = document.getElementById('custom-time-dropdown-'+ this.id)
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

    // handleChangeTime: function(type) {
    //   let startTime, endTime;
    //   startTime = null;
    //   endTime = null;
    //   // }
    //   this.startDate = startTime;
    //   this.endTime = endTime;
    //   this.visible = false;
    // }
  },
  watch: {
    startDate: function(newValue, oldValue) {
      if (!newValue) {
        return;
      }
      if (!this.endTime || newValue.unix() > this.endTime.unix()) {
        this.endTime = newValue;
      }
    },
    endTime: function(newValue, oldValue) {
      if (!newValue) {
        return;
      }
      if (!this.startDate || newValue.unix() < this.startDate.unix()) {
        this.startDate = newValue;
      }
    },
    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        if (!this.startDate || !this.endTime) {
          this.startDate = moment()
          this.endTime = moment()
          // return this.changeDate(this.page, "", "");
        }
        // if (this.selectedMenu === '') {
        //   // this.handleChangeTime('LAST_WEEK')
        //   // this.selectedMenu = 'Last Week'
        //   this.titleShow()
        // }
        this.changeDate(
            // this.type,
            this.page,
            this.startDate.endOf('day').unix(),
            this.endTime.unix()
        );
      }
    },
    isShowFirstLayer: function(oldValue, newValue) {
                if (!this.isShowFirstLayer) {
                    this.closeAnimation();
                }
            }
  },
  mounted() {
    switch (this.defaultDataFilter) {
      case 'LAST_YEAR':
        this.selectedMenu = 'Last Year';
        break;
      case 'LAST_MONTH':
        this.selectedMenu = 'Last Month';
        break;
      default:
        this.selectedMenu = 'Last Week';
    }
  },
};
</script>
<style>
/*.ant-popover-inner-content {*/
/*  padding: 0;*/
/*}*/
.ant-calendar-footer-extra {
  width: 100%;
}
.ant-calendar-time-picker-btn {
  display: none!important;
}
.ant-calendar-ok-btn {
  display: none!important;
}
.truncate {
        width: 68px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
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
/*.ant-btn.active,*/
/*.ant-btn:active,*/
/*.ant-btn:focus {*/
/*  color: #fff !important;*/
/*  fill: #fff !important;*/
/*  background: #fff !important;*/
/*}*/
/*.ant-btn.active,*/
/*.ant-btn:active,*/
/*.ant-btn:focus > svg {*/
/*  fill: #fff !important;*/
/*}*/

/*.down-icon {*/
/*  fill: #fff;*/
/*}*/

.dropdown_button {
  /*display: flex;*/
  /*justify-content: space-between;*/
  /*padding: 0px 10px;*/
  /*align-items: center;*/
  /*width: unset;*/
  /*border: .5px solid #A2A2A2;*/
  /*background: #f2f5fa;*/
  /*border-radius: 4px;*/
  /*z-index: 1;*/
  /*height: 100%;*/
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
.button-disable {
  background: #D9D9D9!important;
  color: #B9B9B9!important;
  border-color: #C4C4C4!important;
}
</style>
