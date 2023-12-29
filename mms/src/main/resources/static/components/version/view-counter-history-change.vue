<template>
    <div
    id="view-counter-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
        <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
            <div class="modal-content">
                <div class='modal-header'>
                    <h5 class="modal-title"  v-text="resources['counter']"></h5>
                    <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
                </div>
                <div class='modal-body' v-if='!loading'>
                    <div class='row'>
                        <div class='col-md-12'>
                            <div class='card'>
                                <div class='card-header'>
                                    <strong  v-text="resources['counter']"></strong>
                                </div>
                                <div class='card-body'>
                                    <div class='form-group row'>
                                        <span class='col-md-3' v-text="resources['counter_id']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.counterId != counterAfter.counterId}">
                                                    {{counterBefore.counterId}}
                                                </span>
                                                <span v-if="counterBefore.counterId !== counterAfter.counterId" class="version-change-new">{{counterAfter.counterId}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-3' v-text="resources['tooling_id']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.toolingName != counterAfter.toolingName}">
                                                    {{counterBefore.toolingName}}
                                                </span>
                                                <span v-if="counterBefore.toolingName !== counterAfter.toolingName" class="version-change-new">{{counterAfter.toolingName}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class='row'>
                        <div class='col-md-12'>
                            <div class='card'>
                                <div class='card-header'>
                                    <strong  v-text="resources['installation_info']"></strong>
                                </div>
                                <div class='card-body'>
                                    <div class='form-group row'>
                                        <span class='col-md-3'  v-text="resources['preset']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.presetCount != counterAfter.presetCount}">
                                                    {{counterBefore.presetCount}}
                                                </span>
                                                <span v-if="counterBefore.presetCount !== counterAfter.presetCount" class="version-change-new">{{counterAfter.presetCount}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-3'  v-text="resources['installation_date']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.installedAt != counterAfter.installedAt}">
                                                    {{counterBefore.installedAt}}
                                                </span>
                                                <span v-if="counterBefore.installedAt !== counterAfter.installedAt" class="version-change-new">{{counterAfter.installedAt}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-3'  v-text="resources['installation_personnel']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.installedBy != counterAfter.installedBy}">
                                                    {{counterBefore.installedBy}}
                                                </span>
                                                <span v-if="counterBefore.installedBy !== counterAfter.installedBy" class="version-change-new">{{counterAfter.installedBy}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <!--<div class='form-group row'>-->
                                        <!--<span class='col-md-3'>Photo</span>-->
                                        <!--<div class='col-md-9'>-->
                                            <!--<div class='form-control'>-->
                                                <!--&lt;!&ndash; <span v-bind:class="{'version-change-old': counterBefore.photo != counterAfter.photo}">-->
                                                    <!--{{counterBefore.photo}}-->
                                                <!--</span>-->
                                                <!--<span v-if="counterBefore.photo !== counterAfter.photo" class="version-change-new">{{counterAfter.photo}}</span>-->
                                                <!--<span style="visibility: hidden">.</span> &ndash;&gt;-->
                                                <!--<div class='form-group row'>-->
                                                    <!--<div class='col-md-6'>-->
                                                        <!--<div  v-bind:class="{'version-change-old': counterBefore.photo != counterAfter.photo}"-->
                                                        <!--v-for="(url, index) in counterBefore.photos" :key="index">{{ url }}</div>-->
                                                    <!--</div>-->

                                                    <!--<div class='col-md-6' v-if="counterBefore.photo != counterAfter.photo">-->
                                                        <!--<div class='version-change-new' v-for="(url, index) in counterAfter.photos" :key="index">{{ url }}</div>-->
                                                    <!--</div>-->
                                                <!--</div>-->

                                            <!--</div>-->
                                        <!--</div>-->
                                    <!--</div>-->
                                    <div class='form-group row'>
                                        <span class='col-md-3'  v-text="resources['memo']"></span>
                                        <div class='col-md-9'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': counterBefore.memo != counterAfter.memo}">
                                                    {{counterBefore.memo}}
                                                </span>
                                                <span v-if="counterBefore.memo !== counterAfter.memo" class="version-change-new">{{counterAfter.memo}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class='modal-footer card-footer-close-btn'>
                    <button type="button" @click.prevent='close()' class="btn btn-secondary"  v-text="resources['close']"></button>
                </div>

            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
props: {
        resources: Object,
    },
    data(){
        return {
            counterBefore: Object,
            counterAfter: Object,
            loading: true
        }
    },
    methods: {
        showView: function(revision){
            this.loading = true;
            this.initData(revision);
            $("#view-counter-history-change").modal('show');
        },

        close: function(){
            self.loading =  true;
            $("#view-counter-history-change").modal('hide');
            this.$emit('open-modal-custom');

        },
        initData: function(revision){
            let self = this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                self.counterAfter = result.data.after;
                self.counterBefore = result.data.before;
                console.log(result)
                self.loading = false;
            });
        }
    }
}
</script>
