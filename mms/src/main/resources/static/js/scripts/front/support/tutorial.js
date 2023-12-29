axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'Tutorials' + ' | eMoldino';

    $('div').removeClass('wave_sidebar');
    $('li').removeClass('wave_header');
    $('img').removeClass('wave_img');
};

var Page = Common.getPage('topics');

var vm = new Vue({
    el: '#app',
    components: {},
    data: {},
    methods: {},
    mounted() {
        this.$nextTick(function () {
            // 모든 화면이 렌더링된 후 실행합니다.
            var self = this;
            axios.get('/api/codes/MAINTENANCE_STATUS').then(function (response) {
                // self.statusCodes = response.data;
                // var uri = URI.parse(location.href);
                // if (uri.fragment === 'cm' || uri.fragment === 'cm-history') {
                //     self.tab(uri.fragment);
                // } else {
                //     self.paging(1);
                // }
            });
        });
    },
});
