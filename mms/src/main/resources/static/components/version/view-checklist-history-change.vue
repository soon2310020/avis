<template>
    <div
    id="view-checklist-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
    >
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">{{displayedTitle}}</h5>
                <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
            </div>
            <div class='modal-body' v-if="!loading">
                <div class='row'>
                    <div class='col-md-12'>
                        <div class='card'>
                            <div class="card-header">
                                <strong>{{displayedTitle}}</strong>
                            </div>
                            <div class='card-body'>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span>Company</span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': checklistAfter.companyId !== checklistBefore.companyId}">
                                                    {{checklistBefore?.company?.name}}
                                                </span>
                                                <span v-if="checklistAfter?.companyId !== checklistBefore?.companyId" class="version-change-new">{{checklistAfter?.company?.name}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span>Checklist ID</span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': checklistAfter.checklistCode !== checklistBefore.checklistCode}">
                                                    {{checklistBefore.checklistCode}}
                                                </span>
                                                <span v-if="checklistAfter.checklistCode !== checklistBefore.checklistCode" class="version-change-new">{{checklistAfter.checklistCode}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span>Checklist</span>
                                    </div>
                                    <div class="col-md-9">
                                            <div class="form-control">
                                                <span v-bind:class="{'version-change-old': checklistAfter.checklistItemStr !== checklistBefore.checklistItemStr}">
                                                    {{checklistBefore.checklistItemStr}}
                                                </span>
                                                <span v-if="checklistAfter.checklistItemStr !== checklistBefore.checklistItemStr" class="version-change-new">{{checklistAfter.checklistItemStr}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <div class='col-md-3'>
                                        <span>Enable</span>
                                    </div>
                                    <div class="col-md-9">
                                        <div class="form-control">
                                            <span v-bind:class="{'version-change-old': checklistAfter.enabled !== checklistBefore.enabled}">
                                                <span v-if="checklistBefore.enabled" class="label label-success">Enabled</span>
                                                <span v-else class="label label-danger">Disabled</span>
                                            </span>
                                            <span v-if="checklistAfter.enabled !== checklistBefore.enabled" class="version-change-new">
                                                <span v-if="checklistAfter.enabled" class="label label-success">Enabled</span>
                                                <span v-else class="label label-danger">Disabled</span>
                                            </span>
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
            checklistAfter: Object,
            checklistBefore: Object,
            // compareFileNames: false,
            loading: true,
            revisionObjectType: String,
        }
    },
    computed: {
      displayedTitle(){
        switch (this.revisionObjectType) {
          case 'CHECKLIST_GENERAL':
            return 'General Checklist';
          case 'CHECKLIST_REJECT_RATE':
            return 'Reject Rate Checklist';
          case 'CHECKLIST_REFURBISHMENT':
            return 'Refurbishment Checklist';
          case 'CHECKLIST_DISPOSAL':
            return 'Disposal Checklist';
          case 'CHECKLIST_MAINTENANCE':
            return 'Maintenance Checklist';
          case 'PICK_LIST':
            return 'Pick List';
          default:
            return 'Maintenance Checklist';
        }
      }
    },
    methods: {
        showView: function(revision){
            this.loading = true;
            this.initData(revision);
            $("#view-checklist-history-change").modal("show")
        },
        close: function(){
            $("#view-checklist-history-change").modal("hide");
            this.$emit('open-modal-custom');

        },
        initData: function(revision){
            console.log('revision', revision);
            let sef =this;
            sef.revisionObjectType = revision.revisionObjectType;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                sef.checklistAfter = result.data.after;
                sef.checklistBefore = result.data.before;
                sef.loading = false;
            });
        },
    }
}
</script>
