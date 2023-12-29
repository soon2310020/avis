window.onload = function () {
    document.title = 'Forgot Password' + ' | eMoldino';
};

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
var vm = new Vue({
    el: '#app',
    data: {
        email: '',
    },
    methods: {
        submit: function () {
            axios
                .post(`/api/users/send_pw_change_mail/${this.email}`)
                .then(function (response) {
                    if (response.status == 200) {
                        window.location.href = '/api/users/pc_mail_sent/' + vm.email;
                    }
                })
                .catch(function (error) {
                    Common.alert(error.response.data);
                });
        },
    },
});
