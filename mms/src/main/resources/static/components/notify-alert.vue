<template>
  <div class="row">
<!--    <div class="col" :class="{'hiddenNotify':hiddenNotify}">-->
    <div class="col" v-show="!hiddenNotify" v-if="Object.entries(resources).length">
      <div style="display: flex;padding: 0;margin: 0.75rem 0;border: unset" class="alert alert-notify" role="alert" :class="{'disabled-alert':!userAlert.alertOn}">
        <div style="margin-right: 13px" class="form-check form-check-inline">
          <label style="margin: 0 5px 0 0;" class="form-check-label-group">
            <strong v-text="resources['alert_generation']"></strong>
          </label>
          <a-tooltip placement="bottom">
            <template slot="title">
              <div style="padding: 6px;font-size: 13px;">
                Alerts will be generated based on the time frequency (Daily/Weekly/Monthly) set by the user. If you wish to receive no alert, simply switch off the Alert Generation button.</div>
            </template>
            <div>
              <img style="width: 11px" src="/images/icon/information.svg" alt="error icon"/>
            </div>
          </a-tooltip>
        </div>

        <div style="margin-right: 16px" class="form-check form-check-inline">
          <div class="only-show-error">
            <div class="switch-button">
              <div class="toggleWrapper1">
                <input type="checkbox" v-model="userAlert.alertOn" class="mobileToggle" id="alert-btn" value="1">
                <label style="text-align: right;display: flex;justify-content: center;align-items: center;font-size: 9px;font-weight: 400" class="change-checkout-status" @click="changeAlertStatus()">
                  <div v-if="!userAlert.alertOn" style="color: #4B4B4B;line-height: 100%;padding: 0px 4px;">Off</div>
                  <div v-else style="color: #FFFFFF;width: 100%;text-align: left;z-index: 999;line-height: 100%;padding: 0px 4px;">On</div>
                </label>
              </div>
            </div>
          </div>

        </div>
        <div v-if="checkPageShow()" class="form-check form-check-inline mr-2">
          <a-tooltip placement="bottom">
            <template v-if="!userAlert.alertOn" slot="title">
              <div style="padding: 6px;font-size: 13px;">
                Time configuration is disabled because the Alert Generation has been switched off. Switch it back on to configure the interval of your alerts.
              </div>
            </template>
            <div>
          <label class="form-check-label radio">
            <strong v-text="resources['real_time']"></strong>
            <input type="radio" value="REAL_TIME" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>
            <span class="checkround"></span>
          </label>
            </div>
          </a-tooltip>
        </div>
        <template v-if="!checkPageShow()">
          <div style="margin-right: 30px" class="form-check form-check-inline">
            <a-tooltip placement="bottom">
              <template v-if="!userAlert.alertOn" slot="title">
                <div style="padding: 6px;font-size: 13px;">
                  Time configuration is disabled because the Alert Generation has been switched off. Switch it back on to configure the interval of your alerts.
                </div>
              </template>
              <div>
                <label class="form-check-label radio">
                  <strong v-text="resources['daily']"></strong>
                  <input type="radio" value="DAILY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>
                  <span class="checkround"></span>
                </label>
              </div>
            </a-tooltip>
<!--            <label class="form-check-label radio">-->
<!--              <strong v-text="resources['daily']"></strong>-->
<!--              <input type="radio" value="DAILY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>-->
<!--              <span class="checkround"></span>-->
<!--            </label>-->
          </div>
          <div style="margin-right: 30px" class="form-check form-check-inline">
            <a-tooltip placement="bottom">
              <template v-if="!userAlert.alertOn" slot="title">
                <div style="padding: 6px;font-size: 13px">
                  Time configuration is disabled because the Alert Generation has been switched off. Switch it back on to configure the interval of your alerts.
                </div>
              </template>
              <div>
                <label class="form-check-label radio">
                  <strong v-text="resources['weekly']"></strong>
                  <input type="radio" checked="checked" value="WEEKLY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>
                  <span class="checkround"></span>
                </label>
              </div>
            </a-tooltip>
<!--            <label class="form-check-label radio">-->
<!--              <strong v-text="resources['weekly']"></strong>-->
<!--              <input type="radio" checked="checked" value="WEEKLY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>-->
<!--              <span class="checkround"></span>-->
<!--            </label>-->
          </div>
          <div style="margin-right: 0" class="form-check form-check-inline">
            <a-tooltip placement="bottom">
              <template v-if="!userAlert.alertOn" slot="title">
                <div style="padding: 6px;font-size: 13px;">
                  Time configuration is disabled because the Alert Generation has been switched off. Switch it back on to configure the interval of your alerts.
                </div>
              </template>
              <div>
                <label class="form-check-label radio">
                  <strong v-text="resources['monthly']"></strong>
                  <input type="radio" checked="checked" value="MONTHLY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>
                  <span class="checkround"></span>
                </label>
              </div>
            </a-tooltip>
<!--            <label class="form-check-label radio">-->
<!--              <strong v-text="resources['monthly']"></strong>-->
<!--              <input type="radio" checked="checked" value="MONTHLY" v-model="userAlert.periodType" :disabled="userAlert.alertOn==false"/>-->
<!--              <span class="checkround"></span>-->
<!--            </label>-->
          </div>
        </template>
        <div style="margin-right: 13px;margin-left: 72px;" class="form-check form-check-inline">
          <label style="margin: 0 5px 0 0" class="form-check-label-group">
            <strong v-text="resources['email_notification']"></strong>
          </label>
          <a-tooltip placement="bottom">
            <template slot="title">
              <div style="padding: 6px;font-size: 13px;">
                {{ resources['email_notification_will_be_send'] }}
              </div>
            </template>
            <div>
              <img style="width: 11px" src="/images/icon/information.svg" alt="error icon"/>
            </div>
          </a-tooltip>
        </div>
        <div style="margin-right: 0" class="form-check form-check-inline">
          <a-tooltip placement="bottom">
            <template v-if="!userAlert.alertOn" slot="title">
              <div style="padding: 6px;font-size: 13px;">
                {{ resources['email_notification_is_disabled_because'] }}
              </div>
            </template>
            <div>
              <div class="only-show-error">
                <div class="switch-button" id="switch-email">
                  <div class="toggleWrapper1">
                    <input type="checkbox" :checked="userAlert.email" class="mobileToggle" id="alert-email-btn" :disabled="userAlert.alertOn==false">
                    <label style="text-align: right;display: flex;justify-content: center;align-items: center;font-size: 9px;font-weight: 400" class="change-checkout-status" @click="changeEmail()">
                      <div v-if="userAlert.email" style="color: #FFFFFF;width: 100%;text-align: left;z-index: 999;line-height: 100%;padding: 0px 4px;">On</div>
                      <div v-else style="color: #4B4B4B;line-height: 100%;padding: 0px 4px;">Off</div>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </a-tooltip>
        </div>
        <div style="display: initial; font-size: 13px;" class="form-check form-check-inline" hidden>
          <span v-text="resources['email_alert_genneration_configuration']"></span>
        </div>
        <template v-if="checkPageShowAlertType()">
          <div style="margin-right: 13px;margin-left: 72px" class="form-check form-check-inline">
            <label style="margin-bottom: 0" class="form-check-label-group">
              <strong v-text="resources['alert_type']"></strong>
            </label>
          </div>
          <div style="margin: 0" class="form-check form-check-inline" v-for="(item, index) in listAlertType" :key="index">
            <a-tooltip placement="bottom">
              <template v-if="!userAlert.alertOn" slot="title">
                <div style="padding: 6px;font-size: 13px;">
                  Alert Type is disabled because the Alert Generation has been switched off. Switch it back on to configure the type of alert you would like to receive.
                </div>
              </template>
              <div>
                <label :class="{'mr30px': index === 0}" class="form-check-label radio" @change="submit(false,true)">
                  <strong v-text="item.name"></strong>
                  <input
                      type="radio"
                      class="form-check-input"
                      :name="item.name"
                      :value="item.value"
                      v-model="userAlert.specialAlertType"
                      :disabled="userAlert.alertOn==false"
                  />
                  <span class="checkround"></span>
                </label>
              </div>
            </a-tooltip>
<!--            <label class="form-check-label radio" @change="submit()">-->
<!--              <strong v-text="item.name"></strong>-->
<!--              <input-->
<!--                  type="radio"-->
<!--                  class="form-check-input"-->
<!--                  :name="item.name"-->
<!--                  :value="item.value"-->
<!--                  v-model="userAlert.specialAlertType"-->
<!--                  :disabled="userAlert.alertOn==false"-->
<!--              />-->
<!--              <span class="checkround"></span>-->
<!--            </label>-->
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    resources: Object,
  },
  data() {
    return {
      userAlert: {
        id: 0,
        alertType: "",
        user: {
          id: "0"
        },
        email: false,
        periodType: "",
        alertOn: true,
        specialAlertType:null
      },

      pageType: "",
      listAlertType:[],
      hiddenNotify:true
    };
  },
  computed: {
    // emailView(){
    //   let emailView=this.userAlert.alertOn && this.userAlert.email;
    //   console.log(emailView);
    //   return emailView;
    // }
  },
  methods: {
    init: function(alertType) {
      console.log("alertType: ", alertType);
      console.log("Type: ", this.type);
      this.pageType = alertType;
      switch (alertType) {
          case 'CYCLE_TIME':
          case 'EFFICIENCY':
            this.listAlertType=[
                {
                  value:'L2',
                  name:'L2'
                },
                {
                  value:'L1L2',
                  name:'L1+L2'
                },
            ]
            break;
          case 'MAINTENANCE':
          case 'CORRECTIVE_MAINTENANCE':
            this.listAlertType=[
              {
                value:'OVERDUE',
                name:'Overdue'
              },
              {
                value:'UPCOMING_OVERDUE',
                name:'Upcoming + Overdue'
              },
            ]
            break;
        case 'REFURBISHMENT':
          this.listAlertType=[
            {
              value:'HIGH',
              name:'High Priority'
            },
            {
              value:'MEDIUM_HIGH',
              name:'Medium Priority + High Priority'
            },
          ]
          break;
        default:
              this.listAlertType=[];
      }
      var self = this;
      axios.get("/api/users/notify/" + alertType).then(function(response) {
        console.log("response: ", response);
        self.userAlert = response.data;
        self.userAlert.alertOn = self.userAlert.alertOn == null ? true : self.userAlert.alertOn;
        let isNeedSubmit = false;
        if (self.checkPageShow()) {
          if (self.userAlert.periodType !== "REAL_TIME") {
            self.userAlert.periodType = "REAL_TIME";
            isNeedSubmit = true;
          }
        }

        if (!response.data.email && response.data.email !== self.userAlert.email) {
          self.userAlert.email = false;
          isNeedSubmit = true;
        }

        if (!response.data.periodType) {
          if (self.checkPageShow()) {
            self.userAlert.periodType = "REAL_TIME";
          } else {
            self.userAlert.periodType = "DAILY";
          }
          isNeedSubmit = true;
        } else{
            if(!self.checkPageShow() && self.userAlert.periodType === "REAL_TIME"){
                self.userAlert.periodType = "DAILY";
                isNeedSubmit = true;
            }
        }
        self.hiddenNotify=false;
        if (isNeedSubmit) {
          self.submit();
        }
        self.$emit('change-alert-generation', self.userAlert.periodType, true,self.userAlert.alertOn,self.userAlert.specialAlertType);
      });
    },

    checkPageShow: function() {
      console.log('Type ne', this.pageType, this.type)
      if (
        this.pageType === "DISCONNECTED" ||
        this.pageType === "MISCONFIGURE" ||
        this.pageType === "DATA_SUBMISSION" ||
        this.type === "DISCONNECTED" ||
        this.type === "MISCONFIGURE" ||
        this.type === "DATA_SUBMISSION" ||
        this.type === 'CORRECTIVE_MAINTENANCE'||
        this.type === 'REFURBISHMENT'||
        this.type === 'DETACHMENT'||
        this.pageType === "REFURBISHMENT"
      ) {
        return true;
      }
      return false;
    },
    checkPageShowAlertType: function() {
      if (
         // this.pageType === "MAINTENANCE" ||
         this.type === "MAINTENANCE" ||
         // this.type === 'CORRECTIVE_MAINTENANCE'||
         this.type === 'CYCLE_TIME'||
         this.type === 'EFFICIENCY'||
          this.type === 'REFURBISHMENT'||
          this.pageType === 'REFURBISHMENT'||
          // this.pageType === "DOWNTIME" ||
         this.pageType === "EFFICIENCY"
      ) {
        return true;
      }
      return false;
    },
    changeAlertStatus: function (){
      this.userAlert.alertOn = !this.userAlert.alertOn;
      this.submit(true);
    },
    changeEmail: function (){
      this.userAlert.email = !this.userAlert.email;
      this.submit();
    },
    submit: function(pushChangeAlert=false,changeSpecialAlertType, reloadAlertCount=false) {
      var self = this;
      let params = Object.assign(this.userAlert, {});
      if (this.userAlert.email === "true") {
        params.email = true;
      } else if (this.userAlert.email === "false") {
        params.email = false;
      }
      if (!params.periodType) {
        delete params.periodType;
      }
      if (pushChangeAlert || changeSpecialAlertType){
        self.$emit('change-alert-generation', self.userAlert.periodType, false, self.userAlert.alertOn,changeSpecialAlertType? self.userAlert.specialAlertType:null );
        // Common.onOffNumAlertOnSidebar(self.userAlert.alertOn);
        // Common.initAlertCount();
      }

      return axios
        .put("/api/users/notify/" + this.userAlert.alertType, params)
        .then(function(response) {
          self.userAlert = response.data;
          return new Promise(resolve => {
            resolve(response.data);
            if (pushChangeAlert || changeSpecialAlertType || reloadAlertCount) {
              //reload alert count on alert center
              if(self.$parent?.$parent && typeof self.$parent?.$parent.reloadAlertCout === 'function'){
                self.$parent.$parent.reloadAlertCout();
              }else
              Common.initAlertCount(true);
            }
          });
        })
        .catch(function(error) {
          console.log(error.response);
        });
    }
  },
  mounted() {
    this.init(this.type);
  },

  watch: {
    "userAlert.periodType": function(newVal, oldVal) {
      if(newVal && oldVal){
        if (newVal.toString() == oldVal.toString()) {
            return;
        }
        if (newVal !== oldVal) {
            this.submit(false,false, true).then(() => {
              this.$emit('change-alert-generation', newVal,null, this.userAlert.alertOn);
            });
        }
      }
    },
    "type": function (newVal,oldVal){
      if(newVal && oldVal) {
        if (newVal.toString() == oldVal.toString()) {
          return;
        }
        if (newVal !== oldVal && ['MAINTENANCE','CORRECTIVE_MAINTENANCE'].includes(newVal)) {
          // this.hiddenNotify=true;//fix update config different tab
          console.log("this.hiddenNotify: ", this.hiddenNotify);
          this.init(newVal);
        }
      }
    },
    "userAlert.alertOn"(newVal) {
      if (!newVal) this.userAlert.email = false
    },
    'userAlert.email'(newVal) {
      console.log('userAlert.email', newVal)
    }
  }
};
</script>
<style>
.ant-tooltip-inner {
  padding: 0;
}
</style>
<style scoped>
.row {
  font-family: Helvetica, Arial, "Lucida Grande", sans-serif;
}
label.form-check-label-group{
  font-size: 16px;
  /*font-weight: bold;*/
  text-align: left;
  letter-spacing: 0px;
  color: #4B4B4B;
  opacity: 1;
}
.disabled-alert #switch-email .toggleWrapper1{
  pointer-events:none;
}
.disabled-alert label.radio,.disabled-alert #switch-email {
  opacity: 0.5;
  /*pointer-events:none;*/
  /*cursor: not-allowed ;*/
  cursor: url("/images/icon/no-clicking-icon.svg"), auto!important;
}
.only-show-error {
  display: flex;
}
.only-show-error .switch-button {
  /*margin-right: 10px;*/
  display: flex;
  align-items: center;
}

.only-show-error .switch-status {
  color: #000000;
  font-size: 12pt;
  line-height: 100%;
}
.only-show-error .toggleWrapper1 {
  display: flex;
  align-items: center;
}
.only-show-error .toggleWrapper1 input.mobileToggle + .change-checkout-status {
  width: 53px;
  height: 20px;
  border: 1px solid #4b4b4b;
}
.only-show-error .toggleWrapper1 input.mobileToggle + .change-checkout-status:before {
  width: 53px;
  height: 20px;
}
.only-show-error .toggleWrapper1 input.mobileToggle + .change-checkout-status:after {
  height: 14px;
  width: 14px;
  transform: translate(0, -50%);
  top: 50%;
  left: 2px;
  background: #4B4B4B;
}
.only-show-error .toggleWrapper1 input.mobileToggle:checked + .change-checkout-status:after {
  left: 35px;
  top: 2px;
  transform: unset;
  background: #F5F5F5;
}
.radio {
  padding-left: 18px!important;
}
.radio .checkround:after {
  width: 8px!important;
  height: 8px!important;
  left: 50%;
  transform: translate(-50%, -50%);
  top: 50%;
  background: #4B4B4B!important;
}
.checkround {
  top: 3px!important;
  height: 12px!important;
  width: 12px!important;
  border-color: #4B4B4B!important;
  border-width: 1px!important;
}
.mr30px {
  margin-right: 30px;
}
.radio strong {
  color: #4B4B4B;
  font-weight: 500;
}
.hiddenNotify {
  visibility: hidden;
}
</style>