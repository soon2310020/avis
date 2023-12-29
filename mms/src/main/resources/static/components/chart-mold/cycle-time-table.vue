<template>
    <mold-table :headers="headers" :results="results" :total="total" :pagination="pagination" :paging="paging"></mold-table>
</template>

<script>
    module.exports = {
        components: {
            'mold-table': httpVueLoader('/components/chart-mold/mold-table.vue'),
        },
        props: {
            results: Array,
            pagination: Array,
            total: Number,
            paging: Function
        },
        data() {
            return {
                headers: {
                    dateTime: 'Date',
                    modeCT: 'Mode CT',
                    approvedCT: 'Approved CT',
                    part: 'Produced Parts',
                    ctVariance: 'CT Variance',
                    maximumCT: 'Maximum CT',
                    minimumCT: 'Minimum CT'
                }
            };
        },
    };
</script>

<style scoped>

</style>