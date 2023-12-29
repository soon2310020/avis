axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
window.onload = function () {
    document.title = 'Currency' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};
var Page = Common.getPage('currency');
var vm = new Vue({
    el: '#app',
    components: {},
    data: {
        currencyColumn: [],
        currencyList: [],
        defaultCurrency: [],
    },
    created() {
        this.getCurrency();
    },
    methods: {
        getCurrency() {
            axios.get('/api/currency-config').then((res) => {
                this.defaultCurrency = res.data;
            });
        },
        setAllcurrencyColumn() {
            vm.currencyColumn = [vm.resources['currency_code'], vm.resources['flag'], vm.resources['name'], vm.resources['symbol'], ''];
        },
        setMainCurrency(currencyType) {
            axios.post(`/api/currency-config/set-as-main/${currencyType}`).then((res) => {
                this.getCurrency();
            });
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
                vm.setAllcurrencyColumn();
            } catch (error) {
                console.log(error);
            }
        },
    },
    mounted() {
        this.$nextTick(function () {
            this.getResources();
        });
    },
});
