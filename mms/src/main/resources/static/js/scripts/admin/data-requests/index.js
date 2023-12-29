axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';

window.onload = function () {
    document.title = 'Data Requests';
    setTimeout(() => {
        $("div").removeClass("wave");
        $("div").removeClass("wave1");
        $("div").removeClass("wave_sidebar");
        $("li").removeClass("wave_header");
        $("img").removeClass("wave_img");
    }, 500);
};
var Page = Common.getPage('data-requests');
var vm = new Vue({
    el: '#app',
    components: {
        'data-requests': httpVueLoader('/components/data-requests/data-requests.vue'),
    },
    computed: {
        resources() {
            return headerVm?.resourcesFake || {}
        }
    },
});
