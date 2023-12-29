axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';

window.onload = function () {
    document.title = 'Reject Rate | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
    //
};
var Page = Common.getPage('rejected-part');

var vm = new Vue({
    el: '#app',
    components: {
        'edit-reject-multiple': httpVueLoader('/components/edit-reject-multiple.vue'),
    },

    data: {
        resources: {},
        fixedRejectReasons: [],
    },

    methods: {
        setFixedRejectReasons() {
            vm.fixedRejectReasons = [
                vm.resources['black_dot'],
                vm.resources['bubbles'],
                vm.resources['burn_mark'],
                vm.resources['dented'],
                vm.resources['drag_mark'],
                vm.resources['flashing'],
                vm.resources['flow_mark'],
                vm.resources['gas_mark'],
                vm.resources['loose_burr'],
                vm.resources['missing'],
                vm.resources['oily'],
                vm.resources['parting_line'],
                vm.resources['pin_mark'],
                vm.resources['wrinkle'],
            ];
        },
        updateDataDone() {
            Common.alert('Success');
            Common.alertCallback = function () {
                window.location.href = '/admin/reject-rates';
            };
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
                vm.setFixedRejectReasons();
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
