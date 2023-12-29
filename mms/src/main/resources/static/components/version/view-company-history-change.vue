<template>
    <div
    id="view-company-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
      <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Company</h5>
                    <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
                </div>
                <div class="modal-body" v-if="companyAfter != null && companyBefore != null">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="card">
                                <div class="card-header">
                                    <h5 class="modal-title"  v-text="resources['company']"></h5>
                                </div>
                                <div class="card-body">
                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['company_type']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.companyTypeTitle }}</span> -->
                                            <!-- <span class="form-control"> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.companyTypeTitle != companyBefore.companyTypeTitle}">{{companyBefore.companyTypeTitle}}</span>
                                                <span v-if="companyAfter.companyTypeTitle !== companyBefore.companyTypeTitle" class="version-change-new">{{companyAfter.companyTypeTitle}}</span>
                                                <span style="visibility: hidden">.</span>
                                            <!-- </span> -->
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['company_code']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.companyCode }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.companyCode != companyBefore.companyCode}">
                                                    {{companyBefore.companyTypeTitle}}
                                                </span>
                                                <span v-if="companyAfter.companyCode !== companyBefore.companyCode" class="version-change-new">{{companyAfter.companyCode}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['company_name']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.name }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.name != companyBefore.name}">
                                                    {{companyBefore.name}}
                                                </span>
                                                <span v-if="companyAfter.name !== companyBefore.name" class="version-change-new">{{companyAfter.name}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['address']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.address }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.address != companyBefore.address}">
                                                    {{companyBefore.address}}
                                                </span>
                                                <span v-if="companyAfter.address !== companyBefore.address" class="version-change-new">{{companyAfter.address}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['manager']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.manager }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.manager != companyBefore.manager}">
                                                    {{companyAfter.manager}}
                                                </span>
                                                <span v-if="companyAfter.manager !== companyBefore.manager" class="version-change-new">{{companyAfter.manager}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['phone_number']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.phone }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.phone != companyBefore.phone}">
                                                    {{companyBefore.phone}}
                                                </span>
                                                <span v-if="companyAfter.phone !== companyBefore.phone" class="version-change-new">{{companyAfter.phone}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label" v-text="resources['email']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <span>{{ companyAfter.email }}</span> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.email != companyBefore.email}">
                                                    {{companyBefore.email}}
                                                </span>
                                                <span v-if="companyAfter.email !== companyBefore.email" class="version-change-new">{{companyAfter.email}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['memo']"></span>
                                        <div class="col-md-10 form-control">
                                            <!-- <textarea name="" placeholder="Memo" v-model="companyAfter.memo" disabled rows="9"></textarea> -->
                                                <span v-bind:class="{'version-change-old': companyAfter.memo != companyBefore.memo}">
                                                    {{companyBefore.memo}}
                                                </span>
                                                <span v-if="companyAfter.memo !== companyBefore.memo" class="version-change-new">{{companyAfter.memo}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <span class="col-md-2 col-form-label"  v-text="resources['enable']"></span>
                                        <div class="col-md-10">
                                            <!-- <div class="radio"> -->
                                                <!-- <label><input type="radio" checked>Enable</label> -->
                                                <!-- <label><input type="radio" checked>Disable</label> -->
                                            <!-- </div> -->
                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="true" v-model="companyAfter.enabled" disabled>
                                                <span v-if="!companyAfter.enabled && companyBefore.enabled" class='version-change-old'  v-text="resources['enable']"></span>
                                                <span v-if="companyAfter.enabled && !companyBefore.enabled" class='version-change-new'  v-text="resources['enable']"></span>
                                                <span v-if="companyAfter.enabled === companyBefore.enabled"  v-text="resources['enable']"></span>

                                            </div>

                                            <div class="form-check form-check-inline">
                                                <input type="radio" value="false" v-model="companyAfter.enabled" disabled>
                                                <!-- <span v-bind:class="{'version-change-new': !companyAfter.enabled && companyBefore.enabled}">Disable</span> -->
                                                <span v-if="companyAfter.enabled && !companyBefore.enabled" class='version-change-old'  v-text="resources['disable']"></span>
                                                <span v-if="!companyAfter.enabled && companyBefore.enabled" class='version-change-new'  v-text="resources['disable']"></span>
                                                <span v-if="companyAfter.enabled === companyBefore.enabled"  v-text="resources['disable']"></span>

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
        companyAfter: Object,
        companyBefore: Object
        }
    },

    methods: {
        showView: function(revision){
            let sef = this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                console.log(result);
                sef.companyAfter = result.data.after;
                sef.companyBefore =result.data.before;
            })
            $("#view-company-history-change").modal('show');
        },
        close: function(){
            $("#view-company-history-change").modal('hide');
            this.$emit('open-modal-custom');

        }
    }
}

</script>
