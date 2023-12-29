axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
var vm = new Vue({
    el: '#app',
    methods: {
        resend: function () {
            var email = $('.ph').html();
            axios
                .post(`/api/users/send_pw_change_mail/` + email)
                .then(function (response) {
                    if (response.status == 200) {
                        window.location.href = '/api/users/pc_mail_sent/' + email;
                    }
                })
                .catch(function (error) {
                    Common.alert(error.response.data);
                });
        },
    },
});
