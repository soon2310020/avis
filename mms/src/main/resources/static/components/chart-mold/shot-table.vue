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
                    totalShot: 'Total Shots',
                    cavity: 'No. of Cavities',
                    part: 'Produced Parts',
                    maxCapacity: 'Max Capacity',
                    capacityDiscrepancy: 'Capacity Discrepancy'
                }
            };
        },
    };
</script>

<style scoped>

</style>