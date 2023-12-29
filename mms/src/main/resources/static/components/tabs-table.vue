<template>
    <div class="tabs-table op-user-list">
        <div class="row">
            <div class="col-lg-12">
                <div class="card">
                    <div class="card-header">
                        <strong>{{ title }}</strong>

                        <div class="card-header-actions">
                            <span class="card-header-action">
                                <small class="text-muted"> <span v-text="resources['total']"></span>: {{total}}</small>
                            </span>
                        </div>
                    </div>
                    <div class="card-body">
                        <input type="hidden" v-model="requestParam.sort"/>

                        <div class="form-row">
                            <div class="mb-3 mb-md-0 col-12 ">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <div class="input-group-text"><i class="fa fa-search"></i></div>
                                    </div>
                                    <input type="text" v-model="requestParam.query" @input="requestParam.query = $event.target.value" @keyup="search"
                                           class="form-control"
                                           :placeholder="searchPlaceholder">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <div class="row">
            <div class="col-lg-12">
                <div class="card">
                    <div class="card-header" style="display:none">
                        <i class="fa fa-align-justify"></i> Striped Table
                    </div>
                    <div class="card-body card-overflow-reset">
                        <ul class="nav nav-tabs" style="margin-bottom: -1px">
                            <li class="nav-item" v-for="(tab, index) in tabs" :key="index">
                                <a class="nav-link" :class="{active: requestParam[tabParam] === tab.code}" href="#"
                                   @click.prevent="changeTab(tab.code)">
                                    <span>
                                        {{ tab.name }}
                                        <span class="badge badge-light badge-pill"
                                              v-if="requestParam[tabParam] === tab.code">{{ total }}</span>
                                    </span>
                                </a>
                            </li>
                        </ul>


                        <div class="table table-responsive-sm table-striped">
                            <div class="thead">
                                <div class="tr tr-sort">
                                    <div class="th text-center __sort" v-for="(header, index) in headers" :key="index">{{
                                        header.name }}
                                    </div>
                                </div>
                            </div>

                            <div class="tbody op-list">
                                <slot name="item" v-for="(result, index) in results" :item="result"></slot>
                            </div>
                        </div>

                        <div class="no-results d-none"  v-text="resources['no_results']">
                        </div>

                        <div class="row">
                            <div class="col-md-8">
                                <ul class="pagination">
                                    <li v-for="(data, index) in pagination" class="page-item"
                                        :class="{active: data.isActive}">
                                        <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                                    </li>
                                </ul>
                            </div>
                            <div class="col-auto ml-auto">
                                <slot name="right-pagination"></slot>
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
            title: {
                type: String,
                default: () => "Table title"
            },
            inputTabs: {
                type: Object,
                default: () => {}
            },
            inputHeaders: {
                type: Object,
                default: () => {}
            },
            searchPlaceholder: {
                type: String
            },
            inputRequestParam: {
                type: Object,
                default: () => {}
            },
            tabParam: {
                type: String,
                default: () => 'status'
            },

        },
        computed: {
            tabs() {
                return Object.keys(this.inputTabs).map(key => {
                    return {name: this.inputTabs[key], code: key}
                })
            },
            headers(){
                return Object.keys(this.inputHeaders).map(key => {
                    return {name: this.inputHeaders[key], code: key}
                });
            }
        },
        data() {
            return {
                'results': [],
                'result': {},
                'total': 0,
                'pagination': [],
                'requestParam': {
                    'query': '',
                    'id': '',
                    // 'sort': 'id,desc',
                    'page': 1
                },
            };
        },
        methods: {
            changeTab: function (status) {
                // this.currentSortType = 'id';
                // this.requestParam.sort = 'id,desc';
                this.requestParam[this.tabParam] = status;
                this.paging(1);
            },
            search: function (page) {
                this.paging(1);
            },


            disable: function (user) {
                user.enabled = false;
                //this.save(user);
            },

            paging: function (pageNumber) {
                var self = this;
                self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

                let paramNotificationId = Common.getParameter("param")
                self.requestParam.id = paramNotificationId;
                const requestParam = JSON.parse(JSON.stringify(self.requestParam));
                var param = Common.param(requestParam);

                axios.get(Page.API_BASE +'?'+ param).then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;

                    if (paramNotificationId != '') {
                        let searchParam = self.results.filter(item => parseInt(item.id) === parseInt(paramNotificationId))[0]
                        self.requestParam.query = searchParam.topicId
                        // self.search()
                        window.history.replaceState(null, null, '/support/customer-support')
                    }
                    console.log('response data', self.results);
                    // self.results = [
                    //     {
                    //         id: 1,
                    //         status: 'NEW',
                    //         topicID: 'DS200101',
                    //         subject: 'How can I register counter?',
                    //         creator: {
                    //             name: 'Tea Le',
                    //             email: 'tea.le@emoldino.com'
                    //         },
                    //         companyName: 'ABC',
                    //         lastUpdatedAt: '35 minutes ago',
                    //         replies: []
                    //     },
                    //     {
                    //         id: 2,
                    //         status: 'IN_PROGRESS',
                    //         topicID: 'DS200101-1',
                    //         subject: 'How can I register counter?',
                    //         creator: {
                    //             name: 'Tea Le',
                    //             email: 'tea.le@emoldino.com'
                    //         },
                    //         companyName: 'ABC',
                    //         lastUpdatedAt: '6 hours ago',
                    //         replies: [
                    //             {
                    //                 userName: 'Tea Le',
                    //                 accountType: 'creator',
                    //                 createdAt: '6 hours ago'
                    //             }
                    //         ]
                    //     },
                    //     {
                    //         id: 3,
                    //         status: 'RESOLVED',
                    //         topicID: 'DS200201',
                    //         subject: 'How can I register counter?',
                    //         creator: {
                    //             name: 'Tea Le',
                    //             email: 'tea.le@emoldino.com'
                    //         },
                    //         companyName: 'ABC',
                    //         lastUpdatedAt: '2020-05-01',
                    //         replies: [
                    //             {
                    //                 userName: 'Tea Le',
                    //                 accountType: 'creator',
                    //                 createdAt: '2 months ago'
                    //             },
                    //             {
                    //                 userName: 'eMoldino support team',
                    //                 accountType: 'support',
                    //                 createdAt: '1 month ago'
                    //             }
                    //         ]
                    //     }
                    // ];
                    self.pagination = Common.getPagingData(response.data);
                    Common.handleNoResults('#app', self.results.length);
                })
                    .catch(function (error) {
                        console.log(error.response);
                    });
            },
            sortBy: function (type) {
                if(this.currentSortType !== type) {
                    this.currentSortType = type
                    this.isDesc = true;
                } else {
                    this.isDesc = !this.isDesc
                }
                if(type) {
                    this.requestParam.sort = `${type},${this.isDesc ? 'desc' : 'asc'}`;
                    this.sort()
                }
            },
            sort: function() {
                this.paging(this.requestParam.page);
            }
        },
        mounted(){
            Object.keys(this.inputRequestParam).forEach(key => {
               this.requestParam[key] =  this.inputRequestParam[key];
            });
            this.paging(1);
        },
        watch: {
        },
    };
</script>
<style>
    .op-user-list .form-group {
        margin-right: 10px;
    }

    .op-user-list .form-group label {
        margin-right: 5px;
    }

    .pagination {
        display: none;
    }

    .depth-2 {
        padding-left: 20px;
    }

    .depth-3 {
        padding-left: 40px;
    }

    .depth-4 {
        padding-left: 60px;
    }
    .card-body .nav-link {
        display: flex;
        min-height: 60px;
        align-items: center;
        text-align: center;
        flex-direction: column;
    }
    .card-body .nav-link > span {
        display: inline-block;
        margin: auto;
    }
    .label-primary {
        background: #AE0DD5;
    }

    .label-default {
        background: #BCBCBC;
    }

    .label-success {
        cursor: pointer;
    }
    .card-title {
        margin-left: 5px;
    }
    .table {
        display: table;
        overflow-x: auto;
        box-sizing: border-box;
        border-spacing: 2px;
        border-color: grey;
        border-collapse: collapse;
    }
    .thead {
        display: table-header-group;
        vertical-align: middle;
        border-color: inherit;
    }

    .tr {
        display: table-row;
        vertical-align: inherit;
        border-color: inherit;
    }

    .table .thead .th {
        vertical-align: bottom;
        border-bottom: 2px solid #c8ced3;
        display: table-cell;
        font-weight: bold;
    }

    .table .th, .table .td {
        border-top: 0;
        padding: 0.5em;
        border-bottom: 1px solid #c8ced3;
    }

    .tbody {
        display: table-row-group;
        vertical-align: middle;
        border-color: inherit;
    }

    .table-striped .tbody .tr:nth-of-type(odd) {
        background-color: rgba(0, 0, 0, 0.05);
    }


    .td {
        border-top: 0;
        padding: 0.5em;
        border-bottom: 1px solid #c8ced3;
        vertical-align: middle;
        display: table-cell;
    }


</style>