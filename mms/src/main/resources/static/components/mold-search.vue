search<template>
    <div id="op-mold-search" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLongTitle" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content" style="width: 111%">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLongTitle" v-text="resources['tooling_search']"></h5>
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
                            <input type="text" @input="requestParam.query = $event.target.value" @keyup="search" class="form-control" placeholder="Search by tooling ID, part ID, part name, category name or project name">
                        </div>


                        <ul class="nav nav-tabs" style="margin-bottom: -1px">
                            <li class="nav-item">
                                <a class="nav-link" :class="{active: requestParam.status == 'all' || userType !== 'OEM'}" href="#" @click.prevent="tab('all')"><span v-text="resources['all']"></span>
                                    <span class="badge badge-light badge-pill" v-if="requestParam.status == 'all'">{{ total }}</span>
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
                                <th class="text-left align-middle" v-text="resources['tooling_id']"></th>
                                <th class="align-middle" v-text="resources['part']"></th>
                                <th class="align-middle" v-text="resources['category'] + ' Name > Product Name'"></th>
                                <th class="align-middle" v-text="resources['select']"></th>
                            </tr>
                            </thead>
                            <tbody class="op-list" style="display:none">
                            <tr v-for="(result, index, id) in results" :id="result.id" >
                                <td class="text-left">
                                    <div class="small text-muted">{{result.equipmentCode}}</div>
                                    {{result.name}}

                                </td>

                                <td>
                                    <div v-for="(part, index, id) in result.parts" :id="part.partId" >
                                        <div class="small text-muted">
                                            {{part.partCode}}
                                        </div>
                                        <div v-bind:data-tooltip-text="part.partName">{{replaceLongtext(part.partName, 40)}}</div>
                                    </div>
                                </td>
                                <td>
                                    <div v-for="(part, index, id) in result.parts" :id="part.partId" >
                                        <div class="small text-muted">
                                            {{part.categoryName}}
                                        </div>
                                        {{part.projectName}}
                                    </div>
                                </td>

                                <td>
                                    <button type="button" class="btn btn-default" data-dismiss="modal" @click="select(result)" v-text="resources['select']"></button>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div class="no-results" :class="{'d-none': total > 0}"  v-text="resources['no_results']"></div>

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
              callFrom: {
                type: String,
                default: ''
              }
            },
        data() {
            return {
                results: [],
                total: 0,
                pagination : [],
                requestParam : {

                    query: '',
                    status: 'all',
                    sort: 'id,desc',
                    page: 1,
                    active: '',
                    equipmentStatus: 'AVAILABLE'
                },
                listCategories: []
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
            select: function(mold) {
                vm.callbackMold(mold);
            },
            setListCategories: function() {
                for (var i = 0; i < this.results.length; i++) {
                    var mold = this.results[i];
                    if (mold.part != null) {
                        if (typeof mold.part.category === 'object') {
                            this.listCategories.push(mold.part.category);
                        } else {
                            var categoryId = mold.part.category;

                            for (var j = 0; j < this.listCategories.length; j++) {
                                var category = this.listCategories[j];

                                if (categoryId == category.id) {
                                    mold.part.category = category;
                                    break;
                                }
                            }
                        }
                    }
                }
            },
            setResultObject: function() {
                // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
                for (var i = 0; i < this.results.length; i++) {
                    if (typeof this.results[i] !== 'object') {
                        var moldId = this.results[i];
                        this.results[i] = this.findMold(this.results, moldId);
                    }
                }
            },
            findMold: function(results, moldId) {
                for (var j = 0; j < results.length; j++) {
                    if (typeof results[j] !== 'object') {
                        continue;
                    }

                    var mold = results[j];
                    if (moldId == mold.id) {
                        return mold;
                        break;
                    }

                    // parts에서 검색
                    if (mold.part != null && mold.part.molds != null) {
                        var findMold = this.findMold(mold.part.molds, moldId);
                        if (typeof findMold === 'object') {
                            return findMold;
                            break;
                        }
                    }
                }
            },

            paging: function(pageNumber) {
                if (this.callFrom === 'match-tooling') {
                  delete this.requestParam.equipmentStatus
                } else {
                  this.requestParam.equipmentStatus = 'AVAILABLE'
                }
                this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;

                var param = Common.param(this.requestParam);
                var self = this;
                axios.get('/api/molds?' + param).then(function(response) {
                  console.log(response,'------------response')

                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
                    self.setResultObject();

                    // 카테고리 정보 저장.
                    self.setListCategories();


                    Common.handleNoResults('#op-mold-search', self.results.length);

                })
                    .catch(function (error) {
                        console.log(error.response);
                    });
            }

        },
        mounted() {
            this.paging(1)
        }
    };
</script>