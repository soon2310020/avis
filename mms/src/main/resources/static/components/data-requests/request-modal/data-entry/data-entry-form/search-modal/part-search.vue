<template>
    <base-dialog :title="'Company Search'" :visible="visible" body-classes="custom-modal__body search-dialog-body"
        dialog-classes="modal-dialog custom-fade-modal" style="z-index: 5005" @close="handleClose">
        <div>
            <div>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i class="fa fa-search"></i>
                        </div>
                    </div>
                    <input type="text" @input="requestParam.query = $event.target.value" @keyup="search"
                        class="form-control" placeholder="Search by name or part ID" />
                </div>


                <ul class="nav nav-tabs" style="margin: 10px 0 -1px">
                    <li class="nav-item">
                        <a class="nav-link" :class="{ active: requestParam.status == 'active' }" href="#"
                            @click.prevent="tab('active')">
                            <span v-text="resources['active']"></span>
                            <span class="badge badge-light badge-pill" v-if="requestParam.status == 'active'">{{
                                total
                            }}</span>
                        </a>
                    </li>
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
                                <th v-text="resources['part_id']"></th>
                                <th v-text="resources['part_name']"></th>
                                <th v-text="resources['category']"></th>
                                <th v-text="resources['edit']"></th>
                            </tr>
                        </thead>
                        <tbody class="op-list">
                            <tr v-for="(result) in results" :key="result.id" :id="result.id">
                                <td>
                                    <div v-if="result.partCode && result.partCode.length > 17">
                                        <a-tooltip placement="bottom">
                                            <template slot="title">
                                                <div style="padding: 6px;font-size: 13px;">
                                                    {{ result.partCode }}
                                                </div>
                                            </template>
                                            {{ truncateText(result.partCode, 17) }}
                                        </a-tooltip>
                                    </div>
                                    <div v-else>
                                        {{ result.partCode }}
                                    </div>
                                </td>
                                <td class="text-left">
                                    <div v-if="result.name && result.name.length > 17">
                                        <a-tooltip placement="bottom">
                                            <template slot="title">
                                                <div style="padding: 6px;font-size: 13px;">
                                                    {{ result.name }}
                                                </div>
                                            </template>
                                            <div>{{ truncateText(result.name, 17) }}</div>
                                        </a-tooltip>
                                    </div>
                                    <div v-else>
                                        {{ result.name }}
                                    </div>
                                </td>
                                <td>{{ categoryLocation(result.categoryId) }}</td>
                                <td>
                                    <button type="button" class="btn btn-default" @click="select(result)"
                                        v-text="resources['select']"></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <div class="no-results" :class="{ 'd-none': total > 0 }" v-text="resources['no_results']"></div>
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
        },
        index: {
            type: [String, Number]
        }
    },
    data() {
        return {
            results: [],
            total: 0,
            pagination: [],
            requestParam: {
                categoryId: "",
                query: "",
                status: "active",
                sort: "id,desc",
                page: 1
            },
            categories: []
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
        truncateText(text, length) {
            if (text.length > length) {
                return text.substring(0, length) + '...';
            } else {
                return text
            }
        },
        category: function () {
            this.paging(1);
        },

        tab: function (status) {
            this.requestParam.status = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },
        select(part) {
            this.$emit("select-part", { index: this.index, part: part })
            this.handleClose();
        },
        handleClose() {
            this.$emit('close')
        },
        paging(pageNumber) {
            this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

            var param = Common.param(this.requestParam);
            var self = this;
            axios
                .get("/api/parts?" + param)
                .then(function (response) {
                    console.log("part-search: ", response);
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    Common.handleNoResults(self.getRootToCurrentId(), self.results.length);
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        categoryLocation: function (categoryId) {
            var location = "";
            for (var i = 0; i < this.categories.length; i++) {
                var parent = this.categories[i];
                for (var j = 0; j < parent.children.length; j++) {
                    var category = parent.children[j];

                    if (category.id == categoryId) {
                        return parent.name + " > " + category.name;
                    }
                }
            }
            return "";
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