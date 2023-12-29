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
        'edit-reject': httpVueLoader('/components/edit-reject.vue'),
    },

    data: {
        fixedRejectReasons: [
            'Black Dot',
            'Bubbles',
            'Burn Mark',
            'Dented',
            'Drag Mark',
            'Flashing',
            'Flow Mark',
            'Gas Mark',
            'Loose Burr',
            'Missing',
            'Oily',
            'Parting Line',
            'Pin Mark',
            'Wrinkle',
        ],
        resources: {},
    },

    methods: {
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
