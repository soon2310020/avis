{
    /* <script type="text/javascript"> */
}
window.onload = function () {
    document.title = 'Tooling' + ' | eMoldino';
};

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

Vue.prototype.$window = window;
var vm = new Vue({
    el: '#app',
    components: {
        // 'chart-detail': httpVueLoader('/components/chart-detail2.vue'),
        'chart-graph-mobile': httpVueLoader('/components/chart-mold/detail-graph/chart-graph-mobile.vue'),
    },
    data: {
        resources: {},
        chartParam: '',
    },
    created() {
        let url_string = window.location.href;
        this.chartParam = /[^/]*$/.exec(url_string)[0];
    },
    methods: {
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
            } catch (error) {
                console.log(error);
            }
        },
    },
    mounted() {
        this.$nextTick(function () {
            vm.getResources();
        });
    },
});
// </script>
