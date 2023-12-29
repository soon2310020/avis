var vm = new Vue({
    el: '#app',
    components: {
        'common-component-test-page': httpVueLoader('/components/common-test-page/common-test-page.vue'),
    },
    data: {
        resources: {},
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
