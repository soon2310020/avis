<template>
    <div class="mold-table">
        <table
                class="table table-responsive-sm table-striped"
                style="border-top: 1px solid #c8ced3"
        >
            <thead>
            <tr>
                <th v-for="(headerLabel, index) in headerLabels" :key="index">{{ headerLabel }}</th>
            </tr>
            </thead>
            <tbody class="op-list" style="display:none;">
            <tr v-for="(result, index) in results" :key="index">
                <td v-for="(field, fieldIndex) in fields" :key="fieldIndex">
                    {{ result[field] }}
                </td>
            </tr>
            </tbody>
        </table>
        <div class="no-results" :class="{'d-none': total > 0}">No results.</div>

        <div style="flex-wrap: nowrap;" class="row">
            <div class="col-md-8">
                <ul class="pagination">
                    <li
                            v-for="(data, index) in pagination"
                            class="page-item"
                            :class="{active: data.isActive}"
                            :key="index"
                    >
                        <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            results: Array,
            headers: Object,
            pagination: Array,
            total: Number
        },
        data() {
            return {
                headerLabels: [],
                fields: []
            };
        },
        methods: {
        },
        computed: {
        },
        mounted() {
            Object.keys(this.headers).forEach(field => {
               this.fields.push(field);
               this.headerLabels.push(this.headers[field]);
            });
        }
    };
</script>

<style scoped>

</style>