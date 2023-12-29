<template>
    <div id="op-company-search" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLongTitle" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLongTitle">Company Search</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">


                    <div>
                        <div class="input-group mb-3">
                            <div class="input-group-prepend">
                                <div class="input-group-text"><i class="fa fa-search"></i></div>
                            </div>
                            <input type="text" @input="requestParam.query = $event.target.value" @keyup="search" class="form-control" placeholder="Search by company name or company ID">
                        </div>


                        <ul class="nav nav-tabs" style="margin-bottom: -1px">
                            <li class="nav-item">
                                <a class="nav-link" :class="{active: requestParam.status == 'active' || userType !== 'OEM'}" href="#" @click.prevent="tab('active')">
                                    <span v-text="resources['all']"></span>
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'active'">{{ total }}</span>
                                </a>
                            </li>
                            <li class="nav-item dropdown" v-for="code in codes.CompanyType" :key="code.code" v-if="userType == 'OEM' && code.enabled">
                                <a class="nav-link" :class="{active: requestParam.status == code.code}" href="#"@click.prevent="tab(code.code)">{{code.title}}
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == code.code">{{ total }}</span>
                                </a>
                            </li>
                            <!--<li class="nav-item dropdown">
                                <a class="nav-link" :class="{active: requestParam.status == 'hq'}" href="#"@click.prevent="tab('hq')">HQ
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'hq'">{{ total }}</span>
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" :class="{active: requestParam.status == 'branch'}" href="#" @click.prevent="tab('branch')">Branch
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'partner'">{{ branch }}</span>
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" :class="{active: requestParam.status == 'supplier'}" href="#" @click.prevent="tab('supplier')">Suppliers
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'supplier'">{{ total }}</span>
                                </a>
                            </li>-->
                        </ul>


                        <table class="table table-responsive-sm table-striped">
                            <colgroup>
                                <col />
                                <col />
                                <col />
                                <col style="width:70px"/>

                            </colgroup>
                            <thead>
                            <tr>
                                <th class="text-left"  v-text="resources['company_name']"></th>
                                <th v-text="resources['company_code']"></th>
                                <th v-text="resources['company_type']"></th>
                                <th v-text="resources['select']"></th>
                            </tr>
                            </thead>
                            <tbody class="op-list" style="display:none">
                            <tr v-for="(result, index, id) in results" :id="result.id" >
                                <td class="text-left">
                                    {{result.name}}
                                </td>
                                <td>{{result.companyCode}}</td>
                                <td>{{result.companyTypeText}}</td>
                                <td>
                                    <button type="button" class="btn btn-default" data-dismiss="modal" @click="selectCompany(result)" v-text="resources['select']"></button>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div class="no-results" :class="{'d-none': total > 0}" v-text="resources['no_results']">

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
            },
        data() {
            return {
                userType: '',
                'searchType': '',
                'results': [],
                'total': 0,
                'pagination': [],
                'requestParam': {

                    'query': '',
                    'status': 'active',
                    'sort': 'id,desc',
                    'page': 1
                },
                'codes': {}
            }
        },
        methods: {
            findCompany: function(searchType, id) {
                this.searchType = searchType;
                this.requestParam.status = this.searchType;
                if (id) {
                  this.requestParam.companyId = id;
                } else {
                  delete this.requestParam.companyId;
                }
                $('#op-company-search').modal('show');
                this.paging(1);
            },
            tab: function(status) {
                if(this.userType === 'OEM'){
                     this.requestParam.status = status;
                } else {
                    this.requestParam.status = this.searchType;
                }
               
                this.paging(1);
            },
            search: function(page) {
                this.paging(1);
            },
            selectCompany: function(company) {
                vm.callbackCompany(company, this.searchType);
            },
            paging: function(pageNumber) {
                this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

                var param = Common.param(this.requestParam);
                var self = this;
                axios.get('/api/access-hierarchy/companies?' + param).then(function(response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    Common.handleNoResults('#op-company-search', self.results.length);
                })
                    .catch(function (error) {
                        console.log(error.response);
                    });
            }

        },
        mounted() {
            this.$nextTick(async function () {
                // 모든 화면이 렌더링된 후 실행합니다.
                var self = this;
                try {
                    const userType = await Common.getSystem('type')
                    const resCodes = await axios.get('/api/codes')
                    self.userType = userType
                    self.codes = resCodes.data
                } catch (error) {
                    console.log(error)
                } finally {
                    self.paging(1)
                }
            })
        }
    };
</script>