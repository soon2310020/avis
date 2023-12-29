axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';

window.onload = () => console.log('onload');

var vm = new Vue({
    el: '#app',
    components: {
        'file-management': httpVueLoader('/components/file-management/index.vue'),
    },
});
