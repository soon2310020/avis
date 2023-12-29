<template>
    <div id="op-location-search" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLongTitle" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLongTitle" v-text="resources['location_search']"></h5>
                    <!-- <button type="button" class="close" data-dismiss="modal" aria-label="Close"> -->
                    <button type="button" class="close" @click="closeModal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">


                    <div>
                        <div class="input-group mb-3">
                            <div class="input-group-prepend">
                                <div class="input-group-text"><i class="fa fa-search"></i></div>
                            </div>
                            <input type="text" @input="requestParam.query = $event.target.value" @keyup="search" class="form-control"  :placeholder="resources['location_search_placeholder']">
                        </div>


                        <ul class="nav nav-tabs" style="margin-bottom: -1px">
                            <li class="nav-item">
                                <a class="nav-link" :class="{active: requestParam.status == 'active' || userType !== 'OEM'}" href="#" @click.prevent="tab('active')">{{userType == 'OEM' ? 'All': 'All'}}
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'active'">{{ total }}</span>
                                </a>
                            </li>
                            <li class="nav-item dropdown" v-for="code in codes.CompanyType" v-if="userType == 'OEM' && code.enabled">
                                <a class="nav-link" :class="{active: requestParam.status == code.code}" href="#" @click.prevent="tab(code.code)">{{code.title}}
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == code.code">{{ total }}</span>
                                </a>
                            </li>
                        </ul>


                        <table class="table table-responsive-sm table-striped">
                            <colgroup>
                                <col />
                                <col />
                                <col />
                                <col style="width:130px"/>

                            </colgroup>
                            <thead>
                            <tr>
                                <th class="text-left align-middle" v-text="resources['location_name']"></th>
                                <th class="align-middle" v-text="resources['location_code']"></th>
                                <th class="align-middle" v-text="resources['company']"></th>
                                <th class="align-middle" v-text="resources['select']"></th>
                            </tr>
                            </thead>
                            <tbody class="op-list" style="display:none">
                            <tr v-for="(result, index, id) in results" :id="result.id" >
                                <td class="text-left">
                                    {{result.name}}
                                </td>
                                <td>{{result.locationCode}}</td>
                                <td>
                                    <div class="small text-muted">{{result.companyCode}}</div>
                                    <div v-html="companyName(result.companyName, result.companyEnabled)"></div>
                                </td>
                                <td>
                                    <button type="button" class="btn btn-default" @click="selectLocation(result)" v-text="resources['select']"></button>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div class="no-results d-none"  v-text="resources['no_results']">
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <ul class="pagination">
                                    <li v-for="(data, index) in pagination" class="page-item" :class="{active: data.isActive}">
                                        <a class="page-link"   @click="paging(data.pageNumber)">{{data.text}}</a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>


                </div>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
                resources: Object,
                parentName: String,
            },
        data() {
            return {
                results: [],
                total: 0,
                pagination : [],
                requestParam : {

                    query: '',
                    status: 'active',
                    sort: 'id,desc',
                    page: 1
                },
            }
        },
        computed: {
            userType() {
                return headerVm?.userType
            },
            codes() {
                return headerVm?.systemCodes || {}
            }
        },
        methods: {
            tab: function(status) {
                this.requestParam.status = status;
                this.paging(1);
            },
            search: function(page) {
                this.paging(1);
            },
            selectLocation: function(location) {
                console.log(this.parentName)
                this.$emit("setlocation", location)
                if (this.parentName !== "modal") {
                    if (this.$root) {
                        this.$root.callbackLocation(location);
                    }
                } 
                this.closeModal();
            },
            paging: function(pageNumber) {
                this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

                var param = Common.param(this.requestParam);
                var self = this;
                axios.get('/api/locations?' + param).then(function(response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    Common.handleNoResults(self.getRootToCurrentId(), self.results.length);

                })
                    .catch(function (error) {
                        console.log(error.response);
                    });
            },
            closeModal(){
                $(this.getRootToCurrentId()).modal('hide')
            }

        },
        mounted() {
            this.paging(1)
        }
    };
</script>