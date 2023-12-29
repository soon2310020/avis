<template>
      <div
    id="view-part-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
        <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
            <div class="modal-content">
                <div class='modal-header'>
                    <h5 class="modal-title" v-text="resources['part']"></h5>
                    <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
                </div>
                <div class='modal-body'>
                    <div class='row'>
                        <div class='col-md-12'>
                            <div class='card'>
                                <div class='card-header'>
                                    <strong  v-text="resources['part']"></strong>
                                </div>
                                <div class='card-body'>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['category']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.categoryName != partBefore.categoryName}">
                                                    {{partBefore.categoryName}}
                                                </span>
                                                <span v-if="partAfter.categoryName !== partBefore.categoryName" class="version-change-new">{{partAfter.categoryName}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_id']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.partCode != partBefore.partCode}">
                                                    {{partBefore.partCode}}
                                                </span>
                                                <span v-if="partAfter.partCode !== partBefore.partCode" class="version-change-new">{{partAfter.partCode}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_name']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.name != partBefore.name}">
                                                    {{partBefore.name}}
                                                </span>
                                                <span v-if="partAfter.name !== partBefore.name" class="version-change-new">{{partAfter.name}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_resin_code']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.resinCode != partBefore.resinCode}">
                                                    {{partBefore.resinCode}}
                                                </span>
                                                <span v-if="partAfter.resinCode !== partBefore.resinCode" class="version-change-new">{{partAfter.resinCode}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_resin_grade']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.resinGrade != partBefore.resinGrade}">
                                                    {{partBefore.resinGrade}}
                                                </span>
                                                <span v-if="partAfter.resinGrade !== partBefore.resinGrade" class="version-change-new">{{partAfter.resinGrade}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['design_revision_level']"></span>
                                        <div class='col-md-10'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.designRevision != partBefore.designRevision}">
                                                    {{partBefore.designRevision}}
                                                </span>
                                                <span v-if="partAfter.designRevision !== partBefore.designRevision" class="version-change-new">{{partAfter.designRevision}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_volume']+'(W x D x H)'"> </span>
                                        <div class='col-md-8'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.size != partBefore.size}">
                                                    {{partBefore.size}}
                                                </span>
                                                <span v-if="partAfter.size !== partBefore.size" class="version-change-new">{{partAfter.size}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                        <div class='col-md-2'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.sizeUnit != partBefore.sizeUnit}">
                                                    {{partBefore.sizeUnit}}
                                                </span>
                                                <span v-if="partAfter.sizeUnit !== partBefore.sizeUnit" class="version-change-new">{{partAfter.sizeUnit}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2' v-text="resources['part_weight']"></span>
                                        <div class='col-md-8'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.weight != partBefore.weight}">
                                                    {{partBefore.weight}}
                                                </span>
                                                <span v-if="partAfter.weight !== partBefore.weight" class="version-change-new">{{partAfter.weight}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                        <div class='col-md-2'>
                                            <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': partAfter.weightUnit != partBefore.weightUnit}">
                                                    {{partBefore.weightUnit}}
                                                </span>
                                                <span v-if="partAfter.weightUnit !== partBefore.weightUnit" class="version-change-new">{{partAfter.weightUnit}}</span>
                                                <span style="visibility: hidden">.</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class='form-group row'>
                                        <span class='col-md-2'  v-text="resources['enable']"></span>
                                        <div class='col-md-10'>
                                            <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="true" v-model="partAfter.enabled" disabled>
                                                <span v-if="!partAfter.enabled && partBefore.enabled" class='version-change-old' v-text="resources['enable']"></span>
                                                <span v-if="partAfter.enabled && !partBefore.enabled" class='version-change-new' v-text="resources['enable']"></span>
                                                <span v-if="partAfter.enabled === partBefore.enabled" v-text="resources['enable']"></span>

                                            </div>

                                            <div class="form-check form-check-inline">
                                                <input type="radio" value="false" v-model="partAfter.enabled" disabled>
                                                <!-- <span v-bind:class="{'version-change-new': !locationAfter.enabled && locationBefore.enabled}">Disable</span> -->
                                                <span v-if="partAfter.enabled && !partBefore.enabled" class='version-change-old' v-text="resources['disable']"></span>
                                                <span v-if="!partAfter.enabled && partBefore.enabled" class='version-change-new' v-text="resources['disable']"></span>
                                                <span v-if="partAfter.enabled === partBefore.enabled" v-text="resources['disable']"></span>

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
            partAfter: Object,
            partBefore: Object
        }
    },
    methods: {
        showView: function(revision){
            console.log(revision)
            this.initData(revision);
            $("#view-part-history-change").modal('show')
        },
        close: function(){
            $("#view-part-history-change").modal('hide');
            this.$emit('open-modal-custom');
        },
        initData: function(revision){
            let sef =this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                sef.partAfter = result.data.after;
                sef.partBefore = result.data.before;
                console.log(result);
            });
        }
    }
}

</script>
