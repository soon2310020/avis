axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

var Page = Common.getPage('terminals');

var vm = new Vue({
    el: '#app',
    data: {
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            query: '',
            status: 'all',
            sort: 'id,desc',
            page: 1,

            operatingStatus: 'NOT_WORKING',
            equipmentStatus: 'INSTALLED',
        },
        codes: {},
        sortType: {
            TERMINAL_ID: 'equipmentCode',
            COMPANY: 'location.company.name',
            LOCATION: 'location.name',
            AREA: 'installationArea',
            OP: 'operatingStatus',
            STATUS: 'equipmentStatus',
            LAST_TIME: 'operatedAt',
        },
        currentSortType: 'id',
        isDesc: true,
    },
    methods: {
        tab: function (status) {
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.status = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },

        deleteTerminal: function (terminal) {
            if (!confirm('Are you sure you want to delete?')) {
                return;
            }
            var param = {
                id: terminal.id,
            };
            axios
                .delete(Page.API_BASE + '/' + terminal.id, param)
                .then(function (response) {
                    if (response.data.success) {
                        vm.paging(1);
                    } else {
                        alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },

        changeEquipmentStatus: function (data, status) {
            var param = {
                id: data.id,
                equipmentStatus: status,
            };
            axios
                .put(Page.API_BASE + '/' + param.id + '/equipment-status', param)
                .then(function (response) {
                    vm.paging(1);
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        paging: function (pageNumber) {
            var self = this;
            self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

            var param = Common.param(self.requestParam);
            axios
                .get(Page.API_BASE + '?' + param)
                .then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    Common.handleNoResults('#app', self.results.length);

                    var pageInfo = self.requestParam.page == 1 ? '' : '?page=' + self.requestParam.page;
                    history.pushState(null, null, Common.$uri.pathname + pageInfo);
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        sortBy: function (type) {
            if (this.currentSortType !== type) {
                this.currentSortType = type;
                this.isDesc = true;
            } else {
                this.isDesc = !this.isDesc;
            }
            if (type) {
                this.requestParam.sort = `${type},${this.isDesc ? 'desc' : 'asc'}`;
                this.sort();
            }
        },
        sort: function () {
            this.paging(this.requestParam.page);
        },
    },
    mounted() {
        this.$nextTick(function () {
            // 모든 화면이 렌더링된 후 실행합니다.
            var self = this;
            axios.get('/api/codes').then(function (response) {
                self.codes = response.data;
                self.paging(1);
            });
        });
    },
});
