<template>
    <div
    id="view-machine-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
      <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" v-text="resources['machine']">Machine</h5>
                    <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
                </div>
                <div class="modal-body" v-if="machineAfter != null && machineBefore != null">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="card">
                                <div class="card-header">
                                    <h5 class="modal-title"  v-text="resources['machine']"></h5>
                                </div>
                                <div class="card-body">
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['machine_id']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.machineId !== machineBefore.machineId}">
                                                    {{machineBefore.machineId}}
                                                </span>
                                                <span v-if="machineAfter.machineId !== machineBefore.machineId" class="version-change-new">{{machineAfter.machineId}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['company']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.companyName !== machineBefore.companyName}">
                                                    {{machineBefore.companyName}}
                                                </span>
                                                <span v-if="machineAfter.companyName !== machineBefore.companyName" class="version-change-new">{{machineAfter.companyName}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['location']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.locationName !== machineBefore.locationName}">
                                                    {{machineBefore.locationName}}
                                                </span>
                                            <span v-if="machineAfter.locationName !== machineBefore.locationName" class="version-change-new">{{machineAfter.locationName}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['line']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.line !== machineBefore.line}">
                                                    {{machineBefore.line}}
                                                </span>
                                            <span v-if="machineAfter.line !== machineBefore.line" class="version-change-new">{{machineAfter.line}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['machine_maker']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.machineMaker !== machineBefore.machineMaker}">
                                                    {{machineBefore.machineMaker}}
                                                </span>
                                            <span v-if="machineAfter.machineMaker !== machineBefore.machineMaker" class="version-change-new">{{machineAfter.machineMaker}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['machine_type']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.machineType !== machineBefore.machineType}">
                                                    {{machineBefore.machineType}}
                                                </span>
                                            <span v-if="machineAfter.machineType !== machineBefore.machineType" class="version-change-new">{{machineAfter.machineType}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['machine_model']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.machineModel !== machineBefore.machineModel}">
                                                    {{machineBefore.machineModel}}
                                                </span>
                                            <span v-if="machineAfter.machineModel !== machineBefore.machineModel" class="version-change-new">{{machineAfter.machineModel}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['machine_tonnage']"></span>
                                        <div class="col-md-10 form-control">
                                                <span v-bind:class="{'version-change-old': machineAfter.machineTonnage !== machineBefore.machineTonnage}">
                                                    {{machineBefore.machineTonnage}}
                                                </span>
                                            <span v-if="machineAfter.machineTonnage !== machineBefore.machineTonnage" class="version-change-new">{{machineAfter.machineTonnage}}</span>
                                            <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['enable']"></span>
                                        <div class="col-md-10">
                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="true" v-model="machineAfter.enabled" disabled>
                                                <span v-if="!machineAfter.enabled && machineBefore.enabled" class='version-change-old'  v-text="resources['enable']"></span>
                                                <span v-if="machineAfter.enabled && !machineBefore.enabled" class='version-change-new'  v-text="resources['enable']"></span>
                                                <span v-if="machineAfter.enabled === machineBefore.enabled"  v-text="resources['enable']"></span>

                                            </div>

                                            <div class="form-check form-check-inline">
                                                <input type="radio" value="false" v-model="machineAfter.enabled" disabled>
                                                <!-- <span v-bind:class="{'version-change-new': !machineAfter.enabled && machineBefore.enabled}">Disable</span> -->
                                                <span v-if="machineAfter.enabled && !machineBefore.enabled" class='version-change-old'  v-text="resources['disable']"></span>
                                                <span v-if="!machineAfter.enabled && machineBefore.enabled" class='version-change-new'  v-text="resources['disable']"></span>
                                                <span v-if="machineAfter.enabled === machineBefore.enabled"  v-text="resources['disable']"></span>

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
    data() {
        return {
        param: Object,
        machineAfter: Object,
        machineBefore: Object
        }
    },

    methods: {
        showView: function(revision){
            let sef = this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                console.log(result);
                sef.machineAfter = result.data.after;
                sef.machineBefore =result.data.before;
            })
            $("#view-machine-history-change").modal('show');
        },
        close: function(){
            $("#view-machine-history-change").modal('hide');
            this.$emit('open-modal-custom');

        }
    }
}

</script>
