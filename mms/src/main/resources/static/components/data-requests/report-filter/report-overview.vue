<template>
    <div>
        <overview-chart :resources="resources" :chart-data="chartData" :render-key="renderKey" :card-data="cardData" @open-data-completion-report="openCompletionReport"></overview-chart>
    </div>
</template>
  
  
<script>

module.exports = {
    name: 'report-overview',
    components: {
        'overview-chart': httpVueLoader('/components/data-requests/report-filter/overview-chart.vue')
    },
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        changeKey: {
          type: [String, Number]
        }
    },
    data() {
        return {
            chartData: [0, 0],
            cardData: {},
            renderKey: 0
        }
    },
    async created() {
      await this.initData()
    },
    mounted() {
    },
    watch: {
      changeKey: async function() {
        await this.initData()
      }
    },
    computed: {

    },
    methods: {
      async initData(){
        const res = await axios.get('/api/data-request/completion-rate-widget-data').then(res => res.data);
        this.cardData = res;
        this.chartData = [res?.overallCompletionRate, 100 - res?.overallCompletionRate];
        this.renderKey ++;
      },
      openCompletionReport(type){
        this.$emit('open-data-completion-report', type)
      }
    },

}
</script>
  
<style scoped>

</style>
  