<template>
    <div>
        <a-drawer
            :title="nameDrawer"
            placement="right"
            :visible="visible"
            :class="{'hidden' : !visible}"
            class="tooling-drawer"
            width="378"
            :body-style="{
                marginBottom: '50px',
                padding: '20px 50px',
                minHeight: '700px'
            }"
            @close="onClose"
            :wrap-class-name="'drawer-custom'"
        >
                 <div class="form-group" v-if="tabType!='counter'">
                    <label for="toolId" v-text="resources['tooling_id']"></label>
                    <input type="text" id="toolId"  v-model="param.toolId" class="form-control" readonly="readonly">
                </div>
                <div class="form-group">
                    <label for="counterId" v-text="resources['counter_id']"></label>
                    <input type="text" id="counterId"  v-model="param.ci" class="form-control" readonly="readonly">
                </div>
                 <div class="form-group" v-if="tabType!='counter'">
                    <label for="companyName" v-text="resources['company']"></label>
                    <input type="text" id="companyName"  v-model="param.company" class="form-control" readonly="readonly">
                </div>
                <div class="form-group">
                    <label for="location" v-text="resources['location']"></label>
                    <input type="text" id="location"  v-model="param.location" class="form-control" readonly="readonly">
                </div>
                <div class="form-group">
                    <label for="lastShot" v-text="tabType==='mold' ? resources['accumulated_shots'] : resources['shot_number']"></label>
                    <input type="text" id="lastShot"  v-model="param.lastShot" class="form-control" readonly="readonly">
                </div>
                <div class="form-group">
                    <label class="preset" for="preset" >
                        <span v-text="resources['reset_value']"></span>
                        <span class="badge-require"></span>
                    </label>
                    <input type="number" id="preset" :min="tabType!='counter' ?param.lastShot:0"  max="999999999" oninput="Common.maxLength(this, 9)"
                           v-model="param.preset" class="form-control" placeholder="Reset Value" required
                           @change="changeResetValue" @keyup="changeResetValue">
                </div>
                 <div class="form-group" v-if="tabType!='counter' && param.preset!=null && param.preset>0">
                    <label class="preset" for="preset">
                      <span>{{resources['reset.forecastedMaxShots.label']}}</span>
                      <span class="badge-require"></span>
                    </label>
                    <input type="number" id="forecastedMaxShots" :min="tabType!='counter' ? param.preset:0"  max="999999999" oninput="Common.maxLength(this, 9)" v-model="param.forecastedMaxShots" class="form-control" :placeholder="resources['reset.forecastedMaxShots']" required>
                </div>
                <div class="form-group">
                    <label for="comment" v-text="resources['message']+' ('+resources['optional']+')'"> </label>
                    <textarea class="form-control" id="comment" v-model="param.comment" rows="2"></textarea>
                </div>

                <div class="form-group">
                    <span v-if="preset.id != ''" class="label label-danger" v-text="resources['in_progress']"></span>
                    <span >{{preset.createdDateTime}}</span>
                </div>
            <div
                :style="{
                  position: 'absolute',
                  right: 0,
                  bottom: 0,
                  width: '100%',
                  padding: '24px 24px 55px 24px',
                  background: '#fff',
                  display: 'flex',
                  justifyContent: 'flex-end',
                  alignItems: 'center',
                  textAlign: 'right',
                  zIndex: 4002,
                }"
            >
                <div>
                    <a
                        href="javascript:void(0)"
                        id="close-request"
                        style="margin-right: 8px"
                        class="btn-custom btn-custom-close"
                        type="primary"
                        @click="closeAnimation()"
                    >
                        {{ resources['cancel'] }}
                    </a>
                </div>
                <div style="margin-right: 11px;">
                    <a
                        v-if="isValidRequest"
                        href="javascript:void(0)"
                        id="create-request"
                        class="btn-custom btn-custom-primary"
                        type="primary"
                        @click="showAnimation()"
                    >
                        {{ nameDrawer !== resources['edit_counter_reset']  ?  resources['reset_counter']  :  resources['save_changes']}}
                    </a>
                    <a v-else class="btn-custom"
                       style="background: #C4C4C4;border-color: #C4C4C4;border: 1px solid #C4C4C4;color: #ffffff"
                       type="primary">
                      {{ nameDrawer !== resources['edit_counter_reset']  ?  resources['reset_counter']  :  resources['save_changes']}}
                    </a>
                </div>
            </div>
        </a-drawer>
    </div>
</template>
<script>
module.exports = {
    name: "ResetDrawer",
    props: {
        resources: Object,
      reloadPage:Function,
    },
    data() {
        return {
            visible: false,
            trigger: false,
            loadingAction: false,
            nameDrawer: this.resources['reset_command'],
            tabType:'counter',
            param : {
                'ci': '',
                'preset': '',
                'toolId': '',
                'lastPreset': '',
                'lastShot': '',
                'shotsFromLastPreset': '',
                'comment': '',
                'forecastedMaxShots': ''
            },
            preset: {
                'id': ''
            },
        }
    },
    computed: {
        isValidRequest () {
            return this.isValidForm();
        },
    },
    created() {
        // this.initForm({})
        // this.getRequestId()
    },
    mounted() {
        this.trigger = true
    },
    methods: {
        resetDrawer () {
            this.param = {
                'ci': '',
                'preset': '',
                'toolId': '',
                'lastPreset': '',
                'lastShot': '',
                'company': '',
                'location': '',
                'shotsFromLastPreset': '',
                'forecastedMaxShots': '',
                'comment': ''
            };
            this.preset = {
                'id': ''
            };
        },
        initForm(item,tabType) {
            this.tabType=tabType;
            this.resetDrawer();
            console.log('item ',item)
            if(this.tabType=='counter'){
                this.param.ci = item.equipmentCode;
                this.param.lastShot = item.shotCount;
                this.param.location = item.locationName;
            }else {

                this.param.ci = item.counterCode;
                this.param.toolId = item.equipmentCode;
                this.param.lastShot = item.lastShot;
                this.param.company = item.companyName;
                this.param.location = item.locationName;
            }
            //alert(mold.lastShot);
            if (this.param.lastShot == null) {
                this.param.lastShot = '0'
            }
            // this.nameDrawer = item.presetStatus == 'READY' ? this.resources['edit_counter_reset'] : this.resources['reset_command'];
            this.nameDrawer = item.presetStatus == 'READY' ? this.resources['edit_counter_reset'] : this.resources['reset_counter'];

            var self = this;
            axios.get('/api/presets/' + this.param.ci).then(function (response) {
                if (response.data != '') {
                    self.param.lastPreset = response.data.preset;
                    self.param.comment = response.data.comment;
                    self.param.preset = Number(response.data.preset);
                    self.param.forecastedMaxShots = Number(response.data.forecastedMaxShots);
                    console.log("self.param.preset: ",self.param.preset);
                    self.preset = response.data;
                    if(self.preset.id && self.preset.id!=''){
                      self.nameDrawer = self.resources['edit_counter_reset'];
                    }
                }
            });
        },

        onShow(item,tabType){
            this.initForm(item,tabType);
            this.visible=true;
        },
        onClose() {
            this.visible=false;
            // this.$emit('close')
            setTimeout(() => {
                this.resetDrawer()
            }, 300);
        },
        submitRequest() {
          console.log('submitRequest');
            var self = this;
            if (this.param.preset === 0 && this.tabType==='mold') {
              this.param.forecastedMaxShots == null;
            }
            axios.post("/api/presets", this.param).then(function(response) {
                self.resetDrawer()
                // self.$emit('reload-page');
                self.reloadPage();
                self.onClose();
            }).catch(function (error) {
                console.log(error);
            });
        },
        callbackPresetForm: function(result) {
            if (result == true) {
                Common.alert("success");
            }
            this.paging(1);
        },
        changeResetValue: function (){
            console.log('this.param.preset:',this.param.preset);
            if(this.tabType==='mold'){
                if (this.param.preset != null && this.param.preset > 0) {
                    this.param.forecastedMaxShots = 1e6 * (Math.floor(this.param.preset / 1e6) + 1);
                } else {
                    this.param.forecastedMaxShots = 0;
                }
            }
            console.log('this.param.forecastedMaxShots:',this.param.forecastedMaxShots);
        },
            isValidForm(){
            if(this.param.preset==null || this.param.preset==="" || this.param.preset<(this.tabType!=='counter' ?this.param.lastShot:0)){
                return false;
            }
            if(this.tabType!=='counter' && this.param.preset!=null && this.param.preset>0){
                if (this.param.forecastedMaxShots == null || this.param.forecastedMaxShots === "" || this.param.forecastedMaxShots < (this.tabType !== 'counter' ? this.param.lastShot : 0)) {
                    return false;
                }
            }
            return true;
        },
        showAnimation(){
            const el = document.getElementById('create-request');
            el.classList.add('primary-animation')
            setTimeout(() => {
                el.classList.remove('primary-animation')
                this.submitRequest();
            }, 700);
        },
        closeAnimation(){
            const el = document.getElementById('close-request');
            el.classList.add('light-animation')
            setTimeout(() => {
                el.classList.remove('light-animation')
                    this.onClose();
            }, 700);
        }
    }

}
</script>
<style>
.primary-animation{
    animation: button-animation-primary 0.7s;
    animation-iteration-count: 1;
    animation-direction: alternate;
    animation-timing-function: ease-in-out;
    background-color: #3585E5;
    border: 1px solid transparent;
    outline: 2px solid transparent;
}
.light-animation{
    animation: reset-animation-primary 0.7s;
    animation-iteration-count: 1;
    animation-direction: alternate;
    animation-timing-function: ease-in-out;
    background-color: #FFFFFF;
    border: 1px solid transparent;
    outline: 2px solid transparent;
}
@keyframes reset-animation-primary {
  0%{

  }
  /* 33%{
    outline: 2px solid #E51616;
  } */
  66%{
    outline: 2px solid #E51616;
  }
  100%{

  }
}
@keyframes button-animation-primary {
  0% {
  }
  33% {
    outline: 2px solid #89d1fd;
    background-color: #3585e5 !important;
  }
  66% {
    outline: 2px solid #89d1fd;
    background-color: #3585e5 !important;
  }
  100% {
    background-color: #3491ff !important;
  }
}
.drawer-custom .form-group label{
    font-weight: bold;
}
.preset{
    position: relative;
}
</style>
<style scoped>
/*@import "/css/drawer.css";*/
.modal-body{
    padding: 0;
}
</style>
<style src="/css/drawer.css" cus-no-cache  rel="stylesheet" type="text/css" scoped> </style>
