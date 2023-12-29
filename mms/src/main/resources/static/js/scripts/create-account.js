var callbackResult = false;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

window.onload = function () {
    document.title = 'Create Account' + ' | eMoldino';
    setTimeout(() => {
        $('.wave_register').removeClass('wave_register');
    }, 80);
};

var vm = new Vue({
    el: '#app',
    data: {
        user: {
            name: '',
            email: '',
            password: '',
            repeatPassword: '',
            accessLevel: 'REGULAR',
            enabled: false,
            companyName: '',
            department: '',
            position: '',
            memo: '',
            // feild name for new feature
            contactDialingCode: '82',
            contactNumber: '',
            mobileDialingCode: '82',
            mobileNumber: '',
            requested: true,
            companyId: '',
            hashCode: Number,
        },
        show: false,
        type: 'password',
        bindProps: {
            enabledCountryCode: true,
            defaultCountry: 'KR',
            autocomplete: 'off',
            mode: '',
        },
        has_number: false,
        has_lowercase: false,
        has_uppercase: false,
        has_special: false,
        typing: false,
    },
    methods: {
        changeCountryMobile: function (value) {
            console.log('value: ', value);
            this.user.mobileDialingCode = value.dialCode;
        },

        password_check: function () {
            this.has_number = /\d/.test(this.user.password);
            this.has_lowercase = /[a-z]/.test(this.user.password);
            this.has_uppercase = /[A-Z]/.test(this.user.password);
            this.has_special = /[,!@#\$%\^\&*\)\(+=._-]/.test(this.user.password);
        },

        changeCountryPhone: function (value) {
            this.user.contactDialingCode = value.dialCode;
        },

        goBack: function () {
            this.user.password = '';
            this.user.email = '';
            location.href = '/login';
        },

        showPassword: function () {
            if (this.type === 'password') {
                this.type = 'text';
                this.show = true;
            } else {
                this.type = 'password';
                this.show = false;
            }
        },

        submit: function () {
            if (this.user.password != this.user.repeatPassword) {
                Common.alert('The entered passwords do not match.');
                return;
            }

            // this.user.phone_number = this.phoneCountrycode + this.user.phone_number;
            // this.user.mobile_number = this.mobileCountryCode + this.user.mobile_number;
            // this.user.memo = `Company Name: ${this.user.companyName} \n ${this.user.memo}`;
            let paramUser = {};
            paramUser = {
                companyId: this.user.companyId,
                contactDialingCode: this.user.contactDialingCode,
                contactNumber: this.user.contactNumber,
                department: this.user.department,
                email: this.user.email,
                password: this.user.password,
                memo: this.user.memo,
                mobileDialingCode: this.user.mobileDialingCode,
                mobileNumber: this.user.mobileNumber,
                name: this.user.name,
                position: this.user.position,
                hashCode: this.user.hashCode,
            };

            console.log('this.user: ', paramUser);

            axios
                .post('/api/users/create-user', paramUser)
                .then(function (response) {
                    vm.user.memo = '';
                    if (response.data.success) {
                        callbackResult = true;
                        Common.alert(
                            'Your request access has been successfully submitted. \n Once approved, we will send you a notification through email'
                        );

                        // setTimeout(function() {location.href = '/login'}, 3000);
                    } else {
                        callbackResult = false;
                        Common.alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    vm.user.memo = '';
                    console.log(error.response);
                });
            return;
        },

        clearForm: function () {
            Object.keys(vm.user).map((key) => {
                vm.user[key] = '';
            });
        },
        getInviteUser() {
            let paths = location.pathname.split('/');
            let hashCode = paths[paths.length - 1];
            axios
                .get(`/api/users/create-user/${hashCode}`)
                .then((response) => {
                    console.log(response.data);
                    if (response.data.success) {
                        this.user.companyName = response.data.data.company.name;
                        this.user.email = response.data.data.email;
                        this.user.hashCode = +hashCode;
                        this.user.companyId = response.data.data.companyId;
                    }
                })
                .catch((error) => {
                    console.log(error);
                });
        },
    },

    watch: {
        'user.contactNumber': function (newVal, oldVal) {
            console.log('newVal: ', newVal);
        },
    },

    mounted() {
        this.getInviteUser();
    },
});

Common.alertCallback = function () {
    if (callbackResult) {
        location.href = '/login';
    } else {
        vm.user.password = '';
        vm.user.repeatPassword = '';
    }
};
