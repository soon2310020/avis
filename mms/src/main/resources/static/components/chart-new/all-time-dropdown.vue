<template>
  <a-popover v-model="visible" placement="bottomRight" trigger="click">
    <div slot="content" style="width: 305px">
      <div class="date-container date-container-custom">
        <div style="display: flex;flex-direction: column;">
          <div style="margin-bottom: 5px;font-weight: 500;" class="text" v-text="resources['from']"></div>
          <a-date-picker
              :allow-clear="false"
              class="date-picker-customize"
              format="YYYY-MMM-DD"
              v-model="startDate"
              :placeholder="'YYYY-MM-DD'"
          >
            <img slot="suffixIcon" height="13" :src="startDate == null ? '/images/icon/empty-default.svg' : '/images/icon/empty-4b.svg'" alt="calender" />
          </a-date-picker>
        </div>
        <div class="date-minus">
          <div></div>
        </div>
        <div style="display: flex;flex-direction: column; align-items: flex-end;">
          <div style="width: 100%;padding-left: 5px;margin-bottom: 5px;font-weight: 500;" class="text" v-text="resources['to']"></div>
          <a-date-picker
              :allow-clear="false"
              class="date-picker-customize"
              format="YYYY-MMM-DD"
              v-model="endTime"
              :placeholder="'YYYY-MM-DD'"
          >
            <img slot="suffixIcon" height="13" :src="endTime == null ? '/images/icon/empty-default.svg' : '/images/icon/empty-4b.svg'" alt="calender" />
          </a-date-picker>
        </div>
      </div>
      <a-button v-on:click="handleChangeTime('ALL')" :class="{'button-unselected': startDate != null}" class="button-custom"><span  v-text="resources['all_time']"></span></a-button>
    </div>
    <div style="margin-right: 0;" :class="{'open': visible}" class="delivery-week-filter">
      <!-- <img src="/images/icon/filter-all-time.svg" alt=""> -->
      <!-- <a-button
          style="visibility: hidden;font-size: 14px;max-width: 185px;"
          class="dropdown_button"
      >
        <span style="color: #656566!important">{{titleShow()}}</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(-7.939 -12.439)" fill="none" stroke="#595959" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"/>
        </svg>
      </a-button> -->
      <a href="javascript:void(0)"
            class="dropdown_button-custom"
            :id="'all-time-dropdown-'+id"
            style="width: 170px"
            @click="showAnimation()"
            @mouseover="mouseOverButton()"
            @mouseleave="mouseleaveButton()"
        >
          <div style="display: flex;align-items: center">
            <img id="calendar-icon" :class="{'card-shadow': displayShadowTimeBox}" height="18" :src="showIcon ? '/images/icon/Icon feather-calendar-selected.svg' : '/images/icon/Icon feather-calendar-default.svg'" alt="calender" />
                <label :key="timeLabel" style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px; max-width: 100px;margin-bottom: 0"
                    :class="{'selected-dropdown-text' : visible}"
                >{{titleShow()}}</label>
          </div>
                
                <!-- <span :key="timeLabel"  style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px;"
                    :class="{'selected-dropdown-text' : visible}"
                >Week {{selectedWeek.week}},  {{selectedWeek.year}}</span> -->
                <!-- <span :key="timeLabel"  style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding: 3px 0 3px 5px;"
                    :class="{'selected-dropdown-text' : visible}"
                >{{ selectedTimeLabel }}</span> -->
                <div>
                    <a-icon class="caret" :class="{'selected-dropdown-text' : visible, 'caret-show' : displayCaret}" type="caret-down" />
                </div>
                <!-- <div v-show="displayCaret" class="caret" :class="{'selected-dropdown-text' : visible}">
                    <a-icon type="caret-up" />
                </div> -->
            <!-- <a-icon v-if="!visible" type="caret-down"/>
            <a-icon v-else type="caret-up"/> -->
        </a>
      <!-- <a-button
          v-if="!visible"
          style="font-size: 14px;max-width: 185px;"
          class="dropdown_button dropdown_button-ab "
      >
        <span style="color: #656566!important">{{titleShow()}}</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(-7.939 -12.439)" fill="none" stroke="#595959" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"/>
        </svg>
      </a-button>
      <a-button
          v-if="visible"
          style="font-size: 14px;max-width: 185px;"
          class="dropdown_button dropdown_button-ab "
      >
        <span style="color: #656566!important">{{titleShow()}}</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737">
          <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(17.912 18.176) rotate(-180)" fill="none" stroke="#595959" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"/>
        </svg>
      </a-button> -->
    </div>
  </a-popover>
</template>

<script>
module.exports = {
  props: {
    type: String,
    page: Number,
    resources: Object,
    changeDate: Function,
    id: String,
  },
  data() {
    return {
      startDate: null,
      endTime: null,
      visible: false,
      defaultValue: moment(),
      visible: false,
      showIcon: false,
      animationTimeout: null,
      timeLabel: 0,
      displayCaret: false,
      caretTimeout: null,
      displayShadowTimeBox: false,
      click: false,
    };
  },
  methods: {
    titleShow: function() {
      if (!this.startDate) {
        return "All Time";
      } else {
        return `${this.startDate.format(
            "MMM Do, YYYY"
        )} - ${this.endTime.format("MMM Do, YYYY")}`;
      }
    },

    handleChangeTime: function(type) {
      let self = this;
      let startTime, endTime;
        startTime = null;
        endTime = null;
      // }
      this.startDate = startTime;
      this.endTime = endTime;
      self.visible = false;
      self.closeAnimation();
      self.showIcon = false;
    },
    mouseOverButton(){
                this.showIcon = true;
            },
            mouseleaveButton(){
                if (!this.visible) {
                    this.showIcon = false;
                }
            },
            hidePopupFromOutside() {
                this.showIcon = false;
                this.closeAnimation();
            },
            showAnimation (){
                this.click = true;
                const el = document.getElementById('all-time-dropdown-'+ this.id)
                if (!this.visible) {
                    el.classList.add("dropdown_button-animation");
                    this.animationTimeout = setTimeout(() => {
                    el.classList.remove("dropdown_button-animation");
                    el.classList.add("selected-dropdown-text");
                    el.classList.add("selected-dropdown-button");
                    }, 1600);
                } else {
                    this.closeAnimation()
                    // this.showIcon = true;
                    this.click = false;
                }
                
            },
            closeAnimation(){
                console.log(`Close - ${this.click}`)
                const el = document.getElementById('all-time-dropdown-'+ this.id)
                if (this.animationTimeout != null) {
                    clearTimeout(this.animationTimeout);
                    this.animationTimeout = null;
                } 
                if (this.caretTimeout != null) {
                    clearTimeout(this.caretTimeout);
                    this.caretTimeout = null;
                }
                if (!this.click) {
                  this.mouseOverButton()
                  this.click = false;
                }
                el.classList.remove("dropdown_button-animation");
                    el.classList.remove("selected-dropdown-text");
                    el.classList.remove("selected-dropdown-button");
                    this.displayCaret = false
                if (!this.click) {
                  this.mouseOverButton()
                }
            },
  },

  computed: {},
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
      console.log(oldValue)
      console.log(newValue)
      if (!newValue) {
        this.hidePopupFromOutside()
      }
      if (newValue == false && oldValue == true) {
        if (!this.startDate) {
          return this.changeDate(this.page, "", "");
        }
        this.changeDate(
            // this.type,
            this.page,
            this.startDate,
            this.endTime
        );
      }
    }
  },
  mounted() {}
};
</script>
<style>
.date-container-custom .ant-calendar-picker-input{
  border: 1px solid #0279FF !important;
}
.button-custom{
  padding: 6px 0px 6px 0px;
  border: 1px solid #A2A2A2;
  background-color: #0279FF !important;
  color: #FFFFFF !important;
  margin-top: 10px;
  height: auto !important;
  width: 100%;
}
.button-unselected{
  border: 1px solid #A2A2A2;
  color: #4B4B4B !important;
  background-color: #FFFFFF !important;
}
.button-unselected:hover{
  color: #0279FF !important;
}
/* .ant-popover{
  transform: translate(0px, 150%);
  transform-origin: 0px !important;
} */
.ant-calendar-picker-container{
  z-index: 9999999999 !important;
}
/* .ant-calendar-picker-container{
  transform: translate(0%,50%);
} */
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
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  color: #fff !important;
  fill: #fff !important;
  background: #fff !important;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  fill: #fff !important;
}

/*.down-icon {*/
/*  fill: #fff;*/
/*}*/

.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: unset;
  border: .5px solid #A2A2A2;
  background: rgb(255, 255, 255);
  /*position: absolute;*/
  border-radius: 4px;
  /*top: 0px;*/
  /*right: 0px;*/
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
  right: -2px;
  transform: translate(0, -50%);
  /*margin-right: -1px;*/
  height: calc(100% + 2px);
}
/*.custom-icon-filter.open .dropdown_button-ab {*/
/*  top: 0!important;*/
/*  height: 100%!important;*/
/*}*/
</style>
