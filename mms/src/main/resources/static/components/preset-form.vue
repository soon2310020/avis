<template>
    <div id="op-preset-form" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
        <form @submit.prevent="submit">
            <div class="modal-dialog modal-md" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="title-part-chart"><span v-text="resources['reset_command']"></span> ({{param.ci}})</h4>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
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
                            <label for="lastShot" v-text="resources['accumulated_shots']"></label>
                            <input type="text" id="lastShot"  v-model="param.lastShot" class="form-control" readonly="readonly">
                        </div>
                        <div class="form-group">
                            <label for="preset" v-text="resources['reset_value']"></label>
                            <input type="number" id="preset" :min="tabType!='counter' ?param.lastShot:0"  max="999999999" oninput="Common.maxLength(this, 9)"
                                   v-model="param.preset" class="form-control" placeholder="Reset Value" required
                                   @keyup="changeResetValue">
                        </div>
                        <div class="form-group" v-if="tabType!='counter' && param.preset!=null && param.preset>0">
                            <label for="preset" v-text="resources['reset.forecastedMaxShots.label']"></label>
                            <input type="number" id="forecastedMaxShots" :min="tabType!='counter' ?param.preset:0"  max="999999999" oninput="Common.maxLength(this, 9)" v-model="param.forecastedMaxShots" class="form-control" :placeholder="resources['reset.forecastedMaxShots']" required>
                        </div>
                        <div class="form-group">
                            <label for="comment" v-text="resources['message']+' ('+resources['optional']+')'"> </label>
                            <textarea class="form-control" id="comment" v-model="param.comment" rows="2"></textarea>
                        </div>

                        <div class="form-group">
                            <span v-if="preset.id != ''" class="label label-danger" v-text="resources['in_progress']"></span>
                            <span >{{preset.createdDateTime}}</span>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal" @click.prevent="closePresetForm()" v-text="resources['close']"></button>
                        <button type="button" v-if="preset.id != ''" class="btn btn-warning" @click.prevent="deletePreset()" v-text="resources['cancel_reset']"></button>
                        <button type="submit" class="btn btn-primary" v-text="resources['save']"></button>
                    </div>
                </div>
            </div>
        </form>
    </div>

</template>

<script>

    module.exports = {
    props: {
                resources: Object,
            },
        data() {
            return {
                'param' : {
                    'ci': '',
                    'preset': '',
                    'toolId': '',
                    'lastPreset': '',
                    'lastShot': '',
                    'shotsFromLastPreset': '',
                    'comment': '',
                    'forecastedMaxShots': ''
                },
                'preset': {
                    'id': ''
                },
                tabType:'tooling'
            }
        },
        methods: {

            showModal: function(item,tabType) {
                this.tabType = tabType;
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



                var self = this;
                axios.get('/api/presets/' + this.param.ci).then(function (response) {
                    if (response.data != '') {
                        self.param.lastPreset = response.data.preset;
                        self.param.comment = response.data.comment;
/*

                        // shotsFromLastPreset
                        self.param.shotsFromLastPreset = self.param.lastShot - response.data.shotCount;

                        // 추천 PRESET
                        self.param.preset = Number(response.data.preset) + self.param.shotsFromLastPreset;
*/
                        self.param.preset = Number(response.data.preset);
                        self.param.forecastedMaxShots = Number(response.data.forecastedMaxShots);
                        console.log("self.param.preset: ",self.param.preset);
                        self.preset = response.data;
                    }

                });

                $('#op-preset-form').modal('show');
            },
            submit: function() {
/*
                if(this.pageType!='counter' && this.param.preset<this.param.lastShot && this.param.preset!=0){
                    Common.alert("Reset Value must be equal or larger than "+ this.param.lastShot +". \n Exception: Reset Value can be 0.");
                    return;
                }
*/

                var self = this;
                axios.post("/api/presets", this.param).then(function(response) {
                    self = {
                        'param' : {
                            'ci': '',
                            'preset': '',
                            'toolId': '',
                            'lastShot': '',
                            'comment': '',
                            'forecastedMaxShots': ''
                        },
                        'preset': {
                            'id': ''
                        }
                    };
                    $('#op-preset-form').modal('hide');
                    vm.callbackPresetForm(true);
                }).catch(function (error) {
                    console.log(error.response);
                });
            },

            deletePreset: function() {
                if (!confirm('Are you sure you want to cancel preset?')) {
                    return;
                }

                axios.put("/api/presets/" + this.preset.id, this.preset).then(function(response) {
                    self = {
                        'param' : {
                            'ci': '',
                            'preset': '',
                            'toolId': '',
                            'lastShot': '',
                            'comment': ''
                        },
                        'preset': {
                            'id': ''
                        }
                    };
                    $('#op-preset-form').modal('hide');
                    vm.callbackPresetForm(true);
                }).catch(function (error) {
                    console.log(error.response);
                });

            },
            closePresetForm: function() {
                try{
                    vm.callbackClosePresetForm(true);
                }catch (e) {

                }
            },
          changeResetValue: function (){
              console.log('this.param.preset:',this.param.preset);
              if(this.tabType=='tooling'){
                if (this.param.preset != null && this.param.preset > 0) {
                  this.param.forecastedMaxShots = 1e6 * (Math.floor(this.param.preset / 1e6) + 1);
                } else {
                  this.param.forecastedMaxShots = 0;
                }
              }
                console.log('this.param.forecastedMaxShots:',this.param.forecastedMaxShots);
          }
        },
      watch: {
        // 'param.preset': function (value) {
        //   if (value != null && value > 0) {
        //     this.param.forecastedMaxShots = 1e6 * (Math.round(value / 1e6) + 1);
        //   } else {
        //     this.param.forecastedMaxShots = 0;
        //   }
        // }
      },
        mounted() {
            this.$nextTick(function () {

            });
        }
    };
</script>
