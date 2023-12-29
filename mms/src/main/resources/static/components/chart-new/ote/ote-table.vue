<template>
    <div class="ote-table" id="ote-table">
        <div class="table-wrapper">
            <table class="table table-responsive-sm table-striped">
                <thead>
                <tr class="tr-sort">
                    <th class="text-left">
                        <span>Tooling ID</span>
                    </th>
                    <th>
                        <span>Company</span>
                    </th>
                    <th>
                        <span>OTE</span>
                    </th>
                </tr>
                </thead>
                <tbody class="op-list" style="display:none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                    <td class="text-left">{{ result.title }}</td>
                    <td class="text-left">{{ result.companyName }}</td>
                    <td class="text-left">{{ result.dataPercent + '%' }}</td>
                </tr>
                </tbody>
            </table>

            <div class="no-results d-none" v-text="resources['no_results']"></div>
        </div>
        <div class="row">
            <div class="col-md-10">
                <ul class="pagination">
                    <li v-for="(data, index) in pagination" class="page-item"
                        :class="{active: data.isActive}">
                        <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                    </li>
                </ul>
            </div>
            <div class="col-md-2 action-content">
                <span @click="backToMain">Main</span>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            resources: Object,
            requestParam: Object,
            backToMain: Function
        },
        components: {
        },
        data() {
            return {
                results: [],
                pagination: [],
                tableData: [],
                pagingParam: {
                    pageNumber: 0,
                    size: 9
                }
            };
        },
        methods: {
            setOverallTableData(data) {
              this.tableData = data;
              this.paging();
            },
            paging(pageNumber) {
                this.pagingParam.pageNumber = pageNumber ? pageNumber: 1;
                let smallIndex = (this.pagingParam.pageNumber - 1) * this.pagingParam.size;
                let bigIndex = this.pagingParam.pageNumber * this.pagingParam.size - 1;
                this.results = this.tableData.filter((item, index) => smallIndex <= index && index <= bigIndex);
                this.pagination = Common.getPagingData({
                    number: this.pagingParam.pageNumber - 1,
                    totalPages: Math.ceil(this.tableData.length / this.pagingParam.size),
                    limitPage: 3
                });
                Common.handleNoResults('#ote-table', this.tableData.length);
            },
            setTableData() {
                this.tableData = [
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                    {
                        title: 'T-0001',
                        companyName: 'XYZ',
                        dataPercent: '95,61'
                    },
                ];
            },
        },

        computed: {
        },
        watch: {

        },
        mounted() {
        }
    };
</script>

<style scoped>
    .pagination {
        overflow-x: auto;
    }
    .action-content {
        display: flex;
        align-items: center;
        height: 30px;
        text-align: right;
        justify-content: flex-end;
        padding-right: 20px;
    }
    .table-wrapper {
        min-height: 380px;
        max-height: 380px;
        overflow-y: auto;
    }
</style>
