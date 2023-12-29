<template>
    <div
    id="view-category-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
    >
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" v-text="resources['category']"></h5>
                <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
            </div>
            <div class='modal-body' v-if='!loading'>
                <div class="row">
                    <div class='col-md-12'>
                        <div class="card">
                            <div class='card-header'>
                                <strong v-text="resources['category']"></strong>
                            </div>
                            <div class='card-body'>
                                <div class="form-group row">
                                    <span class='col-md-2' v-text="resources['name']"></span>
                                    <div class='col-md-10'>
                                        <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': categoryAfter.name != categoryBefore.name}">
                                                    {{categoryBefore.name}}
                                                </span>
                                                <span v-if="categoryAfter.name !== categoryBefore.name" class="version-change-new">{{categoryAfter.name}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class='col-md-2' v-text="resources['description']"></span>
                                    <div class='col-md-10'>
                                        <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': categoryAfter.description != categoryBefore.description}">
                                                    {{categoryBefore.description}}
                                                </span>
                                                <span v-if="categoryAfter.description !== categoryBefore.description" class="version-change-new">{{categoryAfter.description}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class='col-md-2' v-text="resources['parent_category']"></span>
                                    <div class='col-md-10'>
                                        <div class='form-control'>
                                                <span v-bind:class="{'version-change-old': categoryAfter.parentName != categoryBefore.parentName}">
                                                    {{categoryBefore.parentName}}
                                                </span>
                                                <span v-if="categoryAfter.parentName !== categoryBefore.parentName" class="version-change-new">{{categoryAfter.parentName}}</span>
                                                <span style="visibility: hidden">.</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <span class='col-md-2' v-text="resources['enable']"></span>
                                    <div class='col-md-10'>
                                        <div class="form-check form-check-inline mr-3">
                                                <input type="radio" value="true" v-model="categoryAfter.enabled" disabled>
                                                <span v-if="!categoryAfter.enabled && categoryBefore.enabled" class='version-change-old' v-text="resources['enable']"></span>
                                                <span v-if="categoryAfter.enabled && !categoryBefore.enabled" class='version-change-new' v-text="resources['enable']"></span>
                                                <span v-if="categoryAfter.enabled === categoryBefore.enabled" v-text="resources['enable']"></span>

                                            </div>

                                            <div class="form-check form-check-inline">
                                                <input type="radio" value="false" v-model="categoryAfter.enabled" disabled>
                                                <!-- <span v-bind:class="{'version-change-new': !locationAfter.enabled && locationBefore.enabled}">Disable</span> -->
                                                <span v-if="categoryAfter.enabled && !categoryBefore.enabled" class='version-change-old' v-text="resources['disable']"></span>
                                                <span v-if="!categoryAfter.enabled && categoryBefore.enabled" class='version-change-new' v-text="resources['disable']"></span>
                                                <span v-if="categoryAfter.enabled === categoryBefore.enabled" v-text="resources['disable']"></span>

                                            </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class='modal-footer card-footer-close-btn'>
                    <button type="button" @click.prevent='close()' class="btn btn-secondary" v-text="resources['close']"></button>
            </div>

        </div>
    </div>
    </div>

</template>

<script>
module.exports =  {
props: {
        resources: Object,
    },
    data(){
        return {
            categoryAfter: Object,
            categoryBefore: Object,
            loading: true
        }
    },
    methods: {
        showView: function(revision){
            this.loading = true
            this.initData(revision);
            $("#view-category-history-change").modal("show");
        },
        close: function(){
            $("#view-category-history-change").modal("hide");
            this.$emit('open-modal-custom');
        },
        initData: function(revision){
            let sef =this;
            axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
                sef.categoryAfter = result.data.after;
                sef.categoryBefore = result.data.before;
                sef.loading = false;
                console.log(result);
            });
        }
    }
}
</script>
