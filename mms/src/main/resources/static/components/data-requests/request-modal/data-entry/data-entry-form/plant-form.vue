<template>
    <div>
        <div class="row">
            <div class="col-md-12">
                <div v-show="isLoading" class="loading-wave" style="height: 500px; margin-bottom: 16px;"></div>
                <div v-show="!isLoading" class="card custom-card">
                    <div class="card-header">
                        <strong v-text="resources['location']"></strong>
                    </div>
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="name">
                                {{ resources['company'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-5">
                                <input type="hidden" v-model="location.companyId" />
                                <input type="text" v-model="location.companyName" readonly="readonly"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            location.companyName == '' ||
                                            location.companyName == null,
                                    }" required />
                            </div>
                            <div class="col-md-5">
                                <button type="button" class="btn btn-outline-success" data-toggle="modal"
                                    @click="showSeachCompany">
                                    {{ resources['company_search']}}
                                </button>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="name">
                                {{ resources['location_name'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-10">
                                <input type="text" id="name" v-model="location.name" class="form-control"
                                    :class="{ 'form-control-warning': location.name == '' || location.name == null, }"
                                    :placeholder="resources['location_name']" required />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="location-code">
                                {{ resources['location_code'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-10">
                                <input type="text" id="location-code" v-model="location.locationCode" :maxlength="20"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            location.locationCode == '' ||
                                            location.locationCode == null,
                                    }" :placeholder="resources['location_code']" required />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="address">
                                {{ resources['address'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-10">
                                <input v-model="location.address" type="text" id="address" class="form-control" :class="{
                                    'form-control-warning':
                                        location.address == '' ||
                                        location.address == null,
                                }" :placeholder="resources['address']" required />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="textarea-input">
                                {{ resources['memo'] }}
                            </label>
                            <div class="col-md-10">
                                <textarea class="form-control"
                                    :class="{ 'form-control-warning': location.memo == '' || location.memo == null }"
                                    id="textarea-input" v-model="location.memo" name="textarea-input" rows="9"
                                    :placeholder="resources['memo']">
                                </textarea>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">{{ resources['enable'] }}</label>
                            <div class="col-md-10 col-form-label">
                                <div class="form-check form-check-inline mr-3">
                                    <label class="form-check-label">
                                        <input type="radio" v-model="location.enabled" class="form-check-input"
                                            value="true" name="enabled" />
                                        <span>{{ resources['enable'] }}</span>
                                    </label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <label class="form-check-label">
                                        <input type="radio" v-model="location.enabled" class="form-check-input"
                                            value="false" name="disabled" />
                                        <span>{{ resources['disable'] }}</span>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <company-search :resources="resources" :visible="visibleSeachCompany"
            @select-company="callbackCompany" @close="closeSearchModal"></company-search>
    </div>
</template>

<script>
module.exports = {
    components: {
        "company-search": httpVueLoader("/components/data-requests/request-modal/data-entry/data-entry-form/search-modal/company-search.vue"),
    },
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        codes: {
            type: Object,
            default: () => ({})
        },
        location: {
            type: Object,
            default: () => ({})
        },
        isLoading: {
            type: Boolean,
            default: () => (true)
        }
    },
    data() {
        return {
            visibleSeachCompany: false
        }
    },
    computed: {

    },
    methods: {
        callbackCompany(cbObject) {
            const company = cbObject.company;
            console.log("company: ", company);
            if (!this.location.company) {
                this.location.company = {
                    id: "",
                    name: "",
                };
            }
            this.location.company.id = company.id;
            this.location.company.name = company.name;
            this.location.companyId = company.id;
            this.location.companyName = company.name;
            this.location.company = company.company;
        },
        showSeachCompany(){
            this.visibleSeachCompany = true
        },
        closeSearchModal() {
            this.visibleSeachCompany = false
        }
    },
}
</script>

<style>

</style>