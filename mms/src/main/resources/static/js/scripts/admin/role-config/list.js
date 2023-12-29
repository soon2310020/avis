{
    /* <script type="text/javascript"> */
}
axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';

window.onload = function () {
    document.title = 'Role' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};

var vm = new Vue({
    el: '#app',
    components: {
        'system-note': httpVueLoader('/components/system-note.vue'),
        'edit-role-config': httpVueLoader('/components/user/edit-role-config.vue'),
        'user-role-config': httpVueLoader('/components/user/user-role-config.vue'),
    },
    data: {
        currentValue: '',
        resources: {},
        role_data: {},
        results: [],
        list: [],
        disableList: [],
        total: 0,
        totalValues: 0,
        pagination: [],
        pagination1: [],
        requestParam: {
            id: '',
            query: '',
            status: '',
            companyType: 'admin',
            accessLevel: '',
            sort: 'id,desc',
            page: 1,
        },
        requestParam1: {
            enabled: true,
            sort: '',
            query: '',
            page: 1,
            size: 15,
        },
        requestParam2: {
            enabled: true,
        },
        param: {
            enabled: true,
        },
        isDesc: false,
        sortType: {
            NAME: 'role_name',
            ID: 'role_id',
            Description: 'description',
            STATUS: 'enabled',
        },
        currentSortType: 'id',
        visible: false,
        visibleSuccess: false,
        companies: {},
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        listCheckedFull: [],
        isAdmin: true,
        dataReady: false,
        tabCurrent: '',
        paramID: {
            id: [],
        },
        name: null,
        searchInterval: null,
    },
    watch: {
        listChecked(newVal) {
            if (newVal && newVal.length != 0) {
                const self = this;
                const x = this.results.filter((item) => self.listChecked.indexOf(item.id) > -1);
                this.listCheckedFull = x;
            }
        },
    },
    methods: {
        showUserRoleConfig() {
            var child = Common.vue.getChild(this.$children, 'user-role-config');
            if (child != null) {
                this.name = this.list.find((item) => item.id == this.listChecked[0]);
                child.showUserRoleConfig(this.name);
            }
        },
        select: function (event) {
            let isAll = event.target.checked;
            this.isAll = isAll;
            if (isAll) {
                this.listChecked = this.list.map((value) => value.id);
                Common.setHeightActionBar();
            } else {
                this.listChecked = [];
            }
        },
        checkSelect: function (id) {
            const findIndex = this.listChecked.findIndex((value) => {
                return value == id;
            });
            return findIndex !== -1;
        },
        check: function (e, result) {
            if (e.target.checked) {
                this.listChecked.push(+e.target.value);
                Common.setHeightActionBar();
            } else {
                this.listChecked = this.listChecked.filter((value) => value != e.target.value);
                if (this.listChecked.length === 0) {
                    this.isAll = false;
                }
            }
        },
        getUpdatedData() {
            this.getAllData().then((result) => {
                this.list = result.data.content;
                this.listChecked = [];
                this.total = this.list.length;
            });
        },
        goToEdit() {
            this.role_data = this.list.filter((item) => item.id === this.listChecked[0]);
            var child = Common.vue.getChild(this.$children, 'edit-role-config');
            if (child != null) {
                child.showEditRole(this.role_data);
            }
        },

        async getCurrentUser() {
            try {
                const me = await Common.getSystem('me');
                const __currentUser = JSON.parse(me);
                if (!__currentUser.admin) {
                    vm.isAdmin = false;
                    vm.requestParam.companyType = 'non_admin';
                }
            } catch (error) {
                console.log(error);
            }
        },

        tab: async function (status) {
            this.requestParam1.query = '';

            this.isAll = false;
            this.total = this.list.length;
            this.tabCurrent = status;
            this.requestParam1.page = 1;
            this.listChecked = [];
            this.currentSortType = 'id';
            this.requestParam.companyType = status;
            if (status != 'disabled' && this.requestParam.status == 'disabled') {
                this.requestParam.status = 'all';
                this.requestParam1.enabled = true;
                await this.getAllData().then((result) => {
                    this.pagination = [];
                    this.pagination1 = [];
                    this.list = result.data.content;
                    this.pagination = Common.getPagingData(result.data);
                    this.total = this.list.length;
                });
            }
        },
        tabToDisable: async function (status) {
            this.requestParam1.query = '';
            this.isAll = false;
            this.requestParam1.enabled = false;
            this.requestParam1.page = 1;
            await this.getAllData().then((result) => {
                this.pagination = [];
                this.pagination1 = [];
                this.list = result.data.content;
                this.pagination1 = Common.getPagingData(result.data);
                this.total = this.list.length;
            });
            this.tabCurrent = status;
            this.listChecked = [];
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.status = status;
            this.requestParam.companyType = '';
        },
        search: async function () {
            clearInterval(this.searchInterval);
            if (this.requestParam1.enabled) {
                this.searchInterval = setTimeout(() => {
                    this.paging(1);
                }, 500);
            } else {
                this.searchInterval = setTimeout(() => {
                    this.paging1(1);
                }, 500);
            }
        },
        enable: async function () {
            this.isAll = false;
            this.paramID.id = this.listChecked;
            await axios
                .put('/api/common/rol-stp/enable?' + Common.param(this.paramID))
                .then(function (response) {})
                .catch(function (error) {
                    alert('Error!!! Something went wrong \nTry again, did not enable selected list');
                });
            await this.getAllData().then((result) => {
                this.list = result.data.content;
                this.listChecked = [];
                this.total = this.list.length;
                this.pagination1 = Common.getPagingData(result.data);
            });
        },
        disable: async function () {
            this.isAll = false;
            this.paramID.id = this.listChecked;
            await axios
                .put('/api/common/rol-stp/disable?' + Common.param(this.paramID))
                .then(function (response) {})
                .catch(function (error) {
                    alert('Error!!! Something went wrong \nTry again, did not disable selected list');
                });
            await this.getAllData().then((result) => {
                this.list = result.data.content;
                this.listChecked = [];
                this.pagination = Common.getPagingData(result.data);
                this.total = this.list.length;
            });
        },

        paging: function (pageNumber) {
            this.isAll = false;
            this.listChecked = [];
            this.requestParam1.page = pageNumber == undefined ? 1 : pageNumber;
            var param = Common.param(this.requestParam1);
            axios
                .get('/api/common/rol-stp?' + param)
                .then((response) => {
                    this.list = response.data.content;
                    this.pagination = Common.getPagingData(response.data);
                    this.total = this.list.length;
                })
                .catch(function (error) {
                    Common.alert(error.response, 'Error');
                });
        },
        paging1: function (pageNumber) {
            this.isAll = false;
            this.listChecked = [];
            this.requestParam1.page = pageNumber == undefined ? 1 : pageNumber;
            var param = Common.param(this.requestParam1);
            axios
                .get('/api/common/rol-stp?' + param)
                .then((response) => {
                    this.list = response.data.content;
                    this.pagination1 = Common.getPagingData(response.data);
                    this.total = this.list.length;
                })
                .catch(function (error) {
                    Common.alert(error.response, 'Error');
                });
        },
        sortBy: function (value) {
            this.currentValue = value;
            if (this.isDesc) {
                value = value + ',asc';
            } else {
                value = value + ',desc';
            }
            this.requestParam1.sort = value;

            axios
                .get('/api/common/rol-stp?' + Common.param(this.requestParam1))
                .then((response) => {
                    this.list = response.data.content;
                })
                .catch(function (error) {
                    alert('Error!!! Something went wrong \n failed to load enabled list, Internet server error');
                });
            this.isDesc = this.isDesc ? false : true;
            this.requestParam1.sort = '';
        },
        getAllData() {
            return axios
                .get('/api/common/rol-stp?' + Common.param(this.requestParam1))
                .then(function (response) {
                    return response;
                })
                .catch(function (error) {
                    Common.alert(error.response.data);
                });
        },
        getTotal() {
            return axios
                .get('/api/common/rol-stp?' + Common.param(this.requestParam2))
                .then(function (response) {
                    return response;
                })
                .catch(function (error) {
                    Common.alert(error.response.data);
                });
        },
        goToNew() {
            const el = document.getElementById('goToNew2');
            el.classList.add('primary-animation');
            setTimeout(() => {
                el.classList.remove('primary-animation');
                window.location.href = '/admin/role-config/new';
            }, 500);
        },
    },
    async mounted() {
        await this.getCurrentUser();
        await this.getAllData().then((result) => {
            this.list = result.data.content;
            this.pagination = Common.getPagingData(result.data);
            this.total = this.list.length;
        });

        await this.getTotal().then((result) => {
            this.totalValues = result.data.content.length;
        });
        this.requestParam2.enabled = false;
        await this.getTotal().then((result) => {
            this.totalValues += result.data.content.length;
        });
    },
});
{
    /* </script> */
}
