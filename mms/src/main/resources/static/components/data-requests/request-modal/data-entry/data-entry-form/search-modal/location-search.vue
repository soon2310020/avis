<template>
    <base-dialog :title="'Company Search'" :visible="visible" body-classes="custom-modal__body search-dialog-body"
        dialog-classes="modal-dialog custom-fade-modal" style="z-index: 5005" @close="handleClose">
        <div>
            <div>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text"><i class="fa fa-search"></i></div>
                    </div>
                    <input type="text" @input="requestParam.query = $event.target.value" @keyup="search"
                        class="form-control" :placeholder="resources['location_search_placeholder']">
                </div>


                <ul class="nav nav-tabs" style="margin: 10px 0 -1px">
                    <li class="nav-item">
                        <a class="nav-link" :class="{ active: requestParam.status == 'active' || userType !== 'OEM' }"
                            href="#" @click.prevent="tab('active')">{{ userType == 'OEM' ? 'All' : 'All'}}
                            <span class="badge badge-light badge-pill" v-if="requestParam.status == 'active'">{{
                                total
                            }}</span>
                        </a>
                    </li>
                    <template v-for="code in codes.CompanyType">
                        <li class="nav-item dropdown" v-if="userType == 'OEM' && code.enabled" :key="code.code">
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
                            <col style="width:130px" />

                        </colgroup>
                        <thead>
                            <tr>
                                <th class="text-left align-middle" v-text="resources['location_name']"></th>
                                <th class="align-middle" v-text="resources['location_code']"></th>
                                <th class="align-middle" v-text="resources['company']"></th>
                                <th class="align-middle" v-text="resources['select']"></th>
                            </tr>
                        </thead>
                        <tbody class="op-list">
                            <tr v-for="(result) in results" :key="result.id" :id="result.id">
                                <td class="text-truncate text-left" style="max-width: 100px">
                                    {{ result.name }}
                                </td>
                                <td class="text-truncate" style="max-width: 100px">{{ result.locationCode }}</td>
                                <td class="text-truncate" style="max-width: 100px">
                                    <div class="small text-muted">{{ result.companyCode }}</div>
                                    <div v-html="companyName(result.companyName, result.companyEnabled)"></div>
                                </td>
                                <td>
                                    <button type="button" class="btn btn-default" @click="selectLocation(result)"
                                        v-text="resources['select']"></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <div class="no-results d-none" :class="{'d-none': total > 0}" v-text="resources['no_results']">
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
        tab: function (status) {
            this.requestParam.status = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },
        selectLocation: function (location) {
            this.$emit("select-location", location)
            this.handleClose();
        },
        paging: function (pageNumber) {
            this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

            var param = Common.param(this.requestParam);
            var self = this;
            axios.get('/api/locations?' + param).then(function (response) {
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
            const self = this
            try {
                const userType = await Common.getSystem('type')
                const resCodes = await axios.get('/api/codes')
                self.userType = userType
                self.codes = resCodes.data;
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