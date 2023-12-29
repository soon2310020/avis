<template>
    <div>
        <div class="row">
            <div class="col-md-12">
                <div v-show="isLoading" class="loading-wave" style="height: 500px; margin-bottom: 16px;"></div>
                <div v-show="!isLoading" class="card custom-card">
                    <div class="card-header">
                        <strong v-text="resources['machine']"></strong>
                    </div>
                    <div class="card-body">
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="machine-code">
                                {{ resources['machine_id'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-10">
                                <input v-model="machine.machineCode" type="text" id="machine-code" class="form-control"
                                    :class="{
                                        'form-control-warning':
                                            machine.machineCode == '' ||
                                            machine.machineCode == null,
                                    }" :placeholder="resources['machine_id']" required />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">
                                {{ resources['company'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-5">
                                <input type="hidden" v-model="machine.companyId" />
                                <input type="text" v-model="machine.companyName" readonly="readonly"
                                    :placeholder="resources['company']" class="form-control" :class="{
                                        'form-control-warning':
                                            machine.companyName == '' ||
                                            machine.companyName == null,
                                    }" required />
                            </div>
                            <div class="col-md-5">
                                <button type="button" class="btn btn-outline-success" data-toggle="modal"
                                    @click="showSearchCompanyModal">
                                    {{ resources['company_search'] }}
                                </button>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">
                                {{ resources['location'] }}
                                <span class="avatar-status badge-danger"></span></label>
                            <div class="col-md-5">
                                <input type="hidden" v-model="machine.locationId" />
                                <input type="text" v-model="machine.locationName" readonly="readonly"
                                    :placeholder="resources['location']" class="form-control" :class="{
                                        'form-control-warning':
                                            machine.locationName == '' ||
                                            machine.locationName == null,
                                    }" required />
                            </div>
                            <div class="col-md-5">
                                <button type="button" class="btn btn-outline-success" data-toggle="modal"
                                    @click="showSearchLocationModal">
                                    {{ resources['location_search'] }}
                                </button>
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="line">{{ resources['line'] }}<span
                                    class="avatar-status badge-danger"></span></label>
                            <div class="col-md-10">
                                <input v-model="machine.line" type="text" id="line" class="form-control" :class="{
                                    'form-control-warning':
                                        machine.line == '' || machine.line == null,
                                }" :placeholder="resources['Line']" required />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="machine-maker">{{
                                resources['machine_maker']
                            }}</label>
                            <div class="col-md-10">
                                <input v-model="machine.machineMaker" type="text" id="machine-maker"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            machine.machineMaker == '' ||
                                            machine.machineMaker == null,
                                    }" :placeholder="resources['machine_maker']" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="machine-type">
                                {{ resources['machine_type'] }}</label>
                            <div class="col-md-10">
                                <input v-model="machine.machineType" type="text" id="machine-type" class="form-control"
                                    :class="{
                                        'form-control-warning':
                                            machine.machineType == '' ||
                                            machine.machineType == null,
                                    }" :placeholder="resources['machine_type']" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="machine-model">{{
                                resources['machine_model']
                            }}</label>
                            <div class="col-md-10">
                                <input v-model="machine.machineModel" type="text" id="machine-model"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            machine.machineModel == '' ||
                                            machine.machineModel == null,
                                    }" :placeholder="resources['machine_model']" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="machine-tonnage">{{
                            resources['machine_tonnage']}}</label>
                            <div class="col-md-10">
                                <input v-model="machine.machineTonnage" min="1" type="number" id="machine-tonnage"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            machine.machineTonnage == '' ||
                                            machine.machineTonnage == null,
                                    }" :placeholder="resources['machine_tonnage'] + ' (ton)'" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label" for="engineer-in-charge">{{
                            resources['engineer_in_charge'] }}</label>
                            <div class="col-md-10">
                                <input id="engineer-in-charge" class="form-control"
                                    :placeholder="resources['engineer_in_charge']" />
                            </div>
                        </div>
                        <div v-for="(item, index) in customFieldList" :key="index" class="form-group row">
                            <label class="col-md-2 col-form-label" :for="'customFieldList' + index">
                                {{ item.fieldName }}
                                <span class="avatar-status"></span>
                                <span class="badge-require" v-if="item.required"></span>
                            </label>
                            <div class="col-md-10">
                                <input :id="'customFieldList' + index" type="text" v-model="item.defaultInputValue"
                                    class="form-control" :class="{
                                        'form-control-warning':
                                            item.defaultInputValue == '' ||
                                            item.defaultInputValue == null,
                                    }" :placeholder="item.fieldName" :required="item.required" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <company-search :resources="resources" :visible="visibleSeachCompany" @select-company="callbackCompany"
            @close="closeSearchCompanyModal"></company-search>
        <location-search :resources="resources" :visible="visibleSeachLocation" @select-location="callbackLocation"
            @close="closeSearchLocationModal"></location-search>
    </div>
</template>

<script>
module.exports = {
    components: {
        "company-search": httpVueLoader("/components/data-requests/request-modal/data-entry/data-entry-form/search-modal/company-search.vue"),
        "location-search": httpVueLoader("/components/data-requests/request-modal/data-entry/data-entry-form/search-modal/location-search.vue"),
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
        machine: {
            type: Object,
            default: () => ({})
        },
        customFieldList: {
            type: Array,
            default: () => ([])
        },
        isLoading: {
            type: Boolean,
            default: () => (true)
        }
    },
    data() {
        return {
            visibleSeachCompany: false,
            visibleSeachLocation: false,
        }
    },
    computed: {

    },
    methods: {
        callbackLocation(location) {
            if (!this.machine.location) {
                this.machine.location = {
                    id: "",
                    name: "",
                };
            }
            this.machine.locationId = location.id;
            this.machine.locationName = location.name;
            this.machine.location = location;
            console.log('callbackLocation', location, this.machine)
        },
        callbackCompany: function (searchCompany) {
            let company = searchCompany.company;
            if (!this.machine.company) {
                this.machine.company = {
                    id: "",
                    name: "",
                };
            }
            this.machine.companyId = company.id;
            this.machine.companyName = company.name;
            this.machine.company = company;
            console.log("After ", this.machine.company);
        },
        showSearchCompanyModal() {
            this.visibleSeachCompany = true
        },
        closeSearchCompanyModal() {
            this.visibleSeachCompany = false
        },
        showSearchLocationModal() {
            this.visibleSeachLocation = true
        },
        closeSearchLocationModal() {
            this.visibleSeachLocation = false
        },
    },
}
</script>

<style>

</style>