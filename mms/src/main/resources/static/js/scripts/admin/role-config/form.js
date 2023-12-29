window.onload = function () {
    document.title = 'Role' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('div').removeClass('profile_wave1');
        $('div').removeClass('profile_wave2');
        $('div').removeClass('profile_wave3');
        $('div').removeClass('profile_wave4');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        $('div').removeClass('hide_account');
    }, 200);
    document.getElementById('remove').remove();
};

var timeout = null;
var Page = Common.getPage('users');
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
var intervalCheckAccessFeature;
var vm = new Vue({
    el: '#app',
    data: {
        resources: {},
        isNew: Page.IS_NEW,
        show: false,
        role: {
            name: '',
            description: '',
        },
        validateR: false,
        disable: true,
        shadow: 'none',
    },
    methods: {
        submit: function () {
            if (!this.validateR) {
                this.create();
            } else {
                this.role.name = '';
                Common.alert('Please enter correct input');
            }
        },
        create: async function () {
            await axios
                .post('/api/common/rol-stp/one', vm.role)
                .then(function (response) {
                    vm.role.name = '';
                    vm.role.description = '';
                    if (response.data.success) {
                        Common.alertCallback = function () {
                            history.back();
                        };
                        Common.alert('success');
                    }
                })
                .catch(function (error) {
                    Common.alert('Either Role is Duplicate or Internal Error');
                });
        },
        getResources() {
            axios
                .get('/api/resources/getAllMessagesOfCurrentUser')
                .then(function (response) {
                    vm.resources = response.data;
                })
                .catch(function (error) {
                    Common.alert(error.response.data);
                });
        },
        validate() {
            if (this.role.name == '') {
                this.validateR = false;
            }
            const reg = /^[A-Za-z]+$/;
            if (reg.test(this.role.name)) {
                this.validateR = false;
                this.disable = false;
            } else if (this.role.name != '') {
                this.validateR = true;
                this.disable = true;
            }
        },
    },
    mounted() {
        this.getResources();
    },
});
