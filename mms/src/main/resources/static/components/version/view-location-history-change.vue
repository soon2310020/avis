<template>
    <div
    id="view-location-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
    >
       <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"  v-text="resources['location']"></h5>
                <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
            </div>
            <div class='modal-body'>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card">
                            <div class="card-header">
                                <strong  v-text="resources['location']"></strong>
                            </div>
                            <div class="card-body" v-if="locationAfter != null && locationBefore !=null">
                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['company']"></span>
                                    <div class="col-md-10 ">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': locationAfter.companyName != locationBefore.companyName}">
                                                    {{locationBefore.companyName}}
                                                </span>
                                                <span v-if="locationAfter.companyName !== locationBefore.companyName" class="version-change-new">{{locationAfter.companyName}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['location_name']"></span>
                                    <div class="col-md-10 ">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': locationAfter.name != locationBefore.name}">
                                                    {{locationBefore.name}}
                                                </span>
                                                <span v-if="locationAfter.name !== locationBefore.name" class="version-change-new">{{locationAfter.name}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['location_code']"></span>
                                    <div class="col-md-10 ">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': locationAfter.locationCode != locationBefore.locationCode}">
                                                    {{locationBefore.locationCode}}
                                                </span>
                                                <span v-if="locationAfter.locationCode !== locationBefore.locationCode" class="version-change-new">{{locationAfter.locationCode}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['address']"></span>
                                    <div class="col-md-10 ">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': locationAfter.address != locationBefore.address}">
                                                    {{locationBefore.address}}
                                                </span>
                                                <span v-if="locationAfter.address !== locationBefore.address" class="version-change-new">{{locationAfter.address}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['memo']"></span>
                                    <div class="col-md-10 ">
                                        <div class="form-control">
                                                <span v-bind:class="{'version-change-old': locationAfter.memo != locationBefore.memo}">
                                                    {{locationBefore.memo}}
                                                </span>
                                                <span v-if="locationAfter.memo !== locationBefore.memo" class="version-change-new">{{locationAfter.memo}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class="col-md-2"  v-text="resources['enable']"></span>
                                    <div class="col-md-10 ">
                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="true" v-model="locationAfter.enabled" disabled>
                                                <span v-if="!locationAfter.enabled && locationBefore.enabled" class='version-change-old'  v-text="resources['enable']"></span>
                                                <span v-if="locationAfter.enabled && !locationBefore.enabled" class='version-change-new'  v-text="resources['enable']"></span>
                                                <span v-if="locationAfter.enabled === locationBefore.enabled"  v-text="resources['enable']"></span>

                                            </div>

                                            <div class="form-check form-check-inline">
                                                <input type="radio" value="false" v-model="locationAfter.enabled" disabled>
                                                <!-- <span v-bind:class="{'version-change-new': !locationAfter.enabled && locationBefore.enabled}">Disable</span> -->
                                                <span v-if="locationAfter.enabled && !locationBefore.enabled" class='version-change-old'  v-text="resources['disable']"></span>
                                                <span v-if="!locationAfter.enabled && locationBefore.enabled" class='version-change-new'  v-text="resources['disable']"></span>
                                                <span v-if="locationAfter.enabled === locationBefore.enabled"  v-text="resources['disable']"></span>

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
            locationAfter: Object,
            locationBefore: Object
        }
    },
    methods: {
        showView: function(item){
            this.initData(item);
            $("#view-location-history-change").modal('show');
        },
        close: function(){
            $("#view-location-history-change").modal('hide');
            this.$emit('open-modal-custom');
        },
        initData: function(revision){
            let sef =this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                sef.locationAfter = result.data.after;
                sef.locationBefore = result.data.before;
                console.log(result);
            });
        }
    }
}
</script>
