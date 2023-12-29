<template>
    <div
    id="view-terminal-history-change"
    class="modal fade"
    style="overflow-y: unset;"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
    :no-close-on-backdrop="true"
    >
    <div class="modal-dialog modal-lg modal-dialog-centered" style="margin-bottom: 0; margin-top: 0;" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" v-text="resources['terminal']"></h5>
                <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
            </div>
            <div class='modal-body' style="overflow-y: auto; height: calc(100vh - 200px);" v-if="!loading">
                <div class='row'>
                    <div class='col-md-12'>
                        <div class='card'>
                            <div class="card-header">
                                <strong v-text="resources['terminal']"></strong>
                            </div>
                            <div class='card-body'>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span v-text="resources['terminal_id']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.terminalId != terminalBefore.terminalId}">
                                                    {{terminalBefore.terminalId}}
                                                </span>
                                                <span v-if="terminalAfter.terminalId !== terminalBefore.terminalId" class="version-change-new">{{terminalAfter.terminalId}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span v-text="resources['purchased_date']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.purchasedAt != terminalBefore.purchasedAt}">
                                                    {{terminalBefore.purchasedAt}}
                                                </span>
                                                <span v-if="terminalAfter.purchasedAt !== terminalBefore.purchasedAt" class="version-change-new">{{terminalAfter.purchasedAt}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span  v-text="resources['memo']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.memo != terminalBefore.memo}">
                                                    {{terminalBefore.memo}}
                                                </span>
                                                <span v-if="terminalAfter.memo !== terminalBefore.memo" class="version-change-new">{{terminalAfter.memo}}</span>
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
                            <div class="card-header">
                                <strong  v-text="resources['installation_info']"></strong>
                            </div>
                            <div class='card-body'>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span  v-text="resources['location']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.locationName != terminalBefore.locationName}">
                                                    {{terminalBefore.locationName}}
                                                </span>
                                                <span v-if="terminalAfter.locationName !== terminalBefore.locationName" class="version-change-new">{{terminalAfter.locationName}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span v-text="resources['installation_area']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.installationArea != terminalBefore.installationArea}">
                                                    {{terminalBefore.installationArea}}
                                                </span>
                                                <span v-if="terminalAfter.installationArea !== terminalBefore.installationArea" class="version-change-new">{{terminalAfter.installationArea}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span  v-text="resources['installation_date']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.installedAt != terminalBefore.installedAt}">
                                                    {{terminalBefore.installedAt}}
                                                </span>
                                                <span v-if="terminalAfter.installedAt !== terminalBefore.installedAt" class="version-change-new">{{terminalAfter.installedAt}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                </div>

                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span  v-text="resources['installation_personnel']"></span>
                                    </div>
                                     <div class="col-md-9">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': terminalAfter.installedBy != terminalBefore.installedBy}">
                                                    {{terminalBefore.installedBy}}
                                                </span>
                                                <span v-if="terminalAfter.installedBy !== terminalBefore.installedBy" class="version-change-new">{{terminalAfter.installedBy}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <!--<div class="form-group row">-->
                                    <!--<div class='col-md-3'>-->
                                        <!--<span>Photo</span>-->
                                    <!--</div>-->
                                    <!--<div class="col-md-9">-->
                                            <!--<div class="form-control">-->
                                                <!--<div class='col-md-12'>-->
                                                    <!--<div class='form-group row'>-->
                                                        <!--<div class='col-md-6'>-->
                                                            <!--<span style="visibility: hidden">.</span>-->
                                                            <!--<div v-for="(photoBefore, index) in terminalBefore.fileNameArray" v-bind:class="{'version-change-old': !compareFileNames}" :key='index'>{{photoBefore}}</div>-->
                                                         <!--</div>-->
                                                        <!--<div class='col-md-6' v-if="!compareFileNames">-->
                                                            <!--<span style="visibility: hidden">.</span>-->
                                                            <!--<div v-for="(photoAfter, index) in terminalAfter.fileNameArray" class='version-change-new' :key='index'>{{photoAfter}}</div>-->
                                                         <!--</div>-->
                                                    <!--</div>-->
                                                <!--</div>-->
                                            <!--</div>-->
                                    <!--</div>-->
                                <!--</div>-->
                            </div>
                        </div>
                    </div>
                </div>
                <div class='row'>
                    <div class='col-md-12'>
                        <div class='card'>
                            <div class="card-header">
                                <strong v-text="resources['ip_address']"></strong>
                            </div>
                            <div class='card-body'>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span v-text="resources['ip_address_type']"></span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="Dynamic" v-model="terminalBefore.ipTypeString" disabled>
                                                <span v-if="terminalBefore.ipTypeString === 'Dynamic' &&  terminalAfter.ipTypeString === 'Static'" class='version-change-old' v-text="resources['dynamic']"></span>
                                                <span v-if="terminalBefore.ipTypeString === 'Static' && terminalAfter.ipTypeString === 'Dynamic'" class='version-change-new' v-text="resources['dynamic']"></span>
                                                <span v-if="terminalAfter.ipTypeString === terminalBefore.ipTypeString" v-text="resources['dynamic']"></span>

                                            </div>

                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="Static" v-model="terminalAfter.ipTypeString" disabled>
                                                <span v-if="terminalBefore.ipTypeString === 'Static' && terminalAfter.ipTypeString === 'Dynamic'" class='version-change-old' v-text="resources['static']"></span>
                                                <span v-if="terminalBefore.ipTypeString === 'Dynamic' &&  terminalAfter.ipTypeString === 'Static'" class='version-change-new' v-text="resources['static']"></span>
                                                <span v-if="terminalAfter.ipTypeString === terminalBefore.ipTypeString" v-text="resources['static']"></span>

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
            terminalAfter: Object,
            terminalBefore: Object,
            // compareFileNames: false,
            loading: true
        }
    },
    methods: {
        showView: function(revision){
            this.loading = true;
            this.initData(revision);
            $("#view-terminal-history-change").modal("show")
        },
        close: function(){
            $("#view-terminal-history-change").modal("hide");
            this.$emit('open-modal-custom');

        },
        initData: function(revision){
            let sef =this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                sef.terminalAfter = result.data.after;
                sef.terminalBefore = result.data.before;
                // sef.compareFileNames = sef.compareFile();
                sef.loading = false;
            });
        },
        // compareFile: function(){
        //     let afterFile = this.terminalAfter;
        //     let beforeFile = this.terminalBefore;
        //
        //     if(afterFile != null && beforeFile !=null && afterFile.fileNameArray != null && beforeFile.fileNameArray != null){
        //
        //         let fileNameAfter = afterFile.fileNameArray;
        //         let fileNameBefore = beforeFile.fileNameArray;
        //         if(fileNameAfter.length != fileNameBefore.length){
        //             return false;
        //         }
        //
        //         let check;
        //         for(let countAfter = 0; countAfter < fileNameAfter.length; countAfter++){
        //             check = false;
        //             for(let countBefore = 0; countBefore < fileNameBefore.length; countBefore++){
        //                 check = fileNameBefore[countBefore] === fileNameAfter[countAfter];
        //                 if(check == true) break;
        //             }
        //             if(check == false){
        //                 return false;
        //             }
        //         }
        //     }
        //     return true;
        // }
    }
}
</script>
