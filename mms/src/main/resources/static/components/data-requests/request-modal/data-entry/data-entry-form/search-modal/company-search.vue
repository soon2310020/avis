<template>
    <base-dialog :title="'Company Search'" :visible="visible" body-classes="custom-modal__body search-dialog-body"
        dialog-classes="modal-dialog custom-fade-modal" style="z-index: 5005" @close="handleClose">
        <div>
            <div>
                <common-searchbar :request-param="requestParam" :on-search="search"
                    placeholder="Search by company name or company ID"></common-searchbar>


                <ul class="nav nav-tabs" style="margin: 10px 0 -1px">
                    <li class="nav-item">
                        <a class="nav-link"
                            :class="{ active: requestParam.status == 'active' || requestParam.status == '' || requestParam.status == null || userType !== 'OEM' }"
                            href="#" @click.prevent="tab('active')">
                            <span v-text="resources['all']"></span>
                            <span class="badge badge-light badge-pill" v-if="requestParam.status == 'active'">{{
                                total
                            }}</span>
                        </a>
                    </li>
                    <template v-for="code in codes.CompanyType">
                        <li class="nav-item" :key="code.code" v-if="userType == 'OEM' && code.enabled">
                            <a class="nav-link" :class="{ active: requestParam.status == code.code }" href="#"
                                @click.prevent="tab(code.code)">{{ code.title }}
                                <span class="badge badge-light badge-pill" v-if="requestParam.status == code.code">{{
                                    total
                                }}</span>
                            </a>
                        </li>
                    </template>
                </ul>
                <div style="height: 524px; overflow-x: auto; margin-bottom: 10px;">
                    <table class="table table-responsive-sm table-striped">
                        <colgroup>
                            <col />
                            <col />
                            <col />
                            <col style="width:70px" />

                        </colgroup>
                        <thead>
                            <tr>
                                <th class="text-left align-middle" v-text="resources['company_name']"></th>
                                <th v-text="resources['company_code']" class="align-middle"></th>
                                <th v-text="resources['company_type']" class="align-middle"></th>
                                <th v-text="resources['select']" class="align-middle"></th>
                            </tr>
                        </thead>
                        <tbody class="op-list">
                            <tr v-for="(result) in results" :key="result.id" :id="result.id">
                                <td class="text-left">
                                    <div class="text-truncate" style="max-width: 100px">{{ result.name }}</div>
                                </td>
                                <td>
                                    <div class="text-truncate" style="max-width: 100px">{{ result.companyCode }}</div>
                                </td>
                                <td>
                                    <div class="text-truncate" style="max-width: 100px">{{ result.companyTypeText }}
                                    </div>
                                </td>
                                <td>
                                    <button type="button" class="btn btn-default" @click="selectCompany(result)"
                                        v-text="resources['select']"></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="no-results" :class="{ 'd-none': total > 0 }" v-text="resources['no_results']">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <ul class="pagination">
                            <li v-for="(data, index) in pagination" :key="index" class="page-item"
                                :class="{ active: data.isActive }">
                                <a class="page-link" @click="paging(data.pageNumber)">{{ data.text }}</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </base-dialog>
</template>

<script>
module.exports = {
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        visible: {
            type: Boolean,
            default: () => (false)
        },
        searchType: {
            type: String,
            default: () => ('')
        }
    },
    data() {
        return {
            userType: '',
            results: [],
            total: 0,
            pagination: [],
            requestParam: {

                query: '',
                status: 'active',
                sort: 'id,desc',
                page: 1
            },
            codes: {},
            cancelToken: undefined
        }
    },
    watch: {
        visible: async function (newVal) {
            if (newVal) {
                this.paging(1);
            }
        }
    },
    methods: {
        findCompany: function () {
            // if (this.userType == 'OEM') {
            //     this.requestParam.status = status;
            // } else {
            //     this.requestParam.status = this.searchType;
            // }
            this.paging(1);
        },
        tab: function (status) {
            if (this.userType == 'OEM') {
                this.requestParam.status = status;
            } else {
                this.requestParam.status = this.searchType;
            }

            this.paging(1);
        },
        search: function (page) {
            this.paging(page);
        },
        selectCompany(company) {
            this.$emit("select-company", { company: company, searchType: this.searchType })
            this.handleClose()
        },
        paging: function (pageNumber) {
            this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

            var param = Common.param(this.requestParam);
            var self = this;

            if (typeof this.cancelToken != typeof undefined) {
                this.cancelToken.cancel('new request')
            }
            this.cancelToken = axios.CancelToken.source()
            axios.get('/api/companies?' + param, {
                cancelToken: this.cancelToken.token
            }).then(function (response) {
                self.total = response.data.totalElements;
                self.results = response.data.content;
                self.pagination = Common.getPagingData(response.data);

                Common.handleNoResults(self.getRootToCurrentId(), self.results.length);
            })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        handleClose() {
            this.$emit('close')
        }

    },
    created() {
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
}
</script>

<style>

</style>