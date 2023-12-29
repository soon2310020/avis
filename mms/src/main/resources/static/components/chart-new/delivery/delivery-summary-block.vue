<template>
    <div class="delivery-summary-block">
        <delivery-summary-item :value="demand" icon="/images/dashboard/icon-demand.svg" :title="resources['demand']"></delivery-summary-item>
        <delivery-summary-item :value="output" icon="/images/dashboard/icon-output.svg" :title="resources['output']"></delivery-summary-item>
        <delivery-summary-item :value="overall" icon="/images/dashboard/icon-overall-otd.svg" :title="resources['overall_otd']"></delivery-summary-item>
    </div>
</template>

<script>
    module.exports = {
        props: {
            resources: Object
        },
        components: {
            "delivery-summary-item": httpVueLoader("/components/chart-new/delivery/delivery-summary-item.vue"),
        },
        data() {
            return {
                demand: '',
                output: '',
                overall: ''
            };
        },
        methods: {
            setSummaryBlockData(data) {
                this.demand = Common.formatNumber(data.demand);
                this.output = Common.formatNumber(data.output);
                this.overall = data.overall + '%';
            },
            formatWeeklyDemand(weeklyDemand) {
                if (weeklyDemand < 1000) {
                    return weeklyDemand;
                }
                return weeklyDemand/1000 + 'K';
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
.delivery-summary-block {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin: 1rem 0;
}
</style>
