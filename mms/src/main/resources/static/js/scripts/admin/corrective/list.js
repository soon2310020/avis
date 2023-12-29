axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'Corrective' + ' | eMoldino';
    var pageTitle = document.title;
};

var Page = Common.getPage('molds');

var vm = new Vue({
    el: '#app',
    components: {
        'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        'message-form': httpVueLoader('/components/message-form.vue'),
        'message-details': httpVueLoader('/components/message-details.vue'),
        'location-history': httpVueLoader('/components/location-history.vue'),
    },
    data: {
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            query: '',
            status: 'all',
            sort: 'id,desc',
            page: 1,

            operatingStatus: '',
            maintenanced: true,
            maintenanceStatus: '',
        },

        codes: {},
        listCategories: [],
        sortType: {
            TOOLING_ID: 'mold.equipmentCode',
            COUNTER_ID: 'mold.counter.equipmentCode',
            FAILURE_TIME: 'failureTime',
            REPAIR_TIME: 'repairTime',
            TIME_REPAIR: 'timeToRepair',
            LOCATION: 'location.name',
            STATUS: 'correctiveStatus',
        },
        currentSortType: 'id',
        isDesc: true,
        resources: {},
    },
    methods: {
        tab: function (status) {
            this.total = null;
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.status = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },

        disable: function (user) {
            user.enabled = false;
            //this.save(user);
        },

        showChart: function (mold) {
            console.log(mold);
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
                child.showChart(mold, 'QUANTITY', 'DAY');
            }
        },

        changeStatus: function (data, status) {
            if ('REPAIRED' == status && confirm('Are you sure you want to cancel?')) {
                var param = {
                    id: data.id,
                };
                var apiUri = Page.API_BASE + '/corrective/' + data.id + '/cancel-repair';
                axios
                    .put(apiUri, param)
                    .then(function (response) {
                        vm.paging(1);
                    })
                    .catch(function (error) {
                        console.log(error.response);
                    });
            }
        },

        paging: function (pageNumber) {
            var self = this;
            self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

            var param = Common.param(self.requestParam);
            axios
                .get(Page.API_BASE + '/corrective?' + param)
                .then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
                    self.setResultObject();

                    Common.handleNoResults('#app', self.results.length);

                    var pageInfo = self.requestParam.page == 1 ? '' : '?page=' + self.requestParam.page;
                    history.pushState(null, null, Common.$uri.pathname + pageInfo);
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        setResultObject: function () {
            // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
            for (var i = 0; i < this.results.length; i++) {
                if (typeof this.results[i].mold !== 'object') {
                    var moldId = this.results[i].mold;
                    this.results[i].mold = this.findMoldFromList(this.results, moldId);
                }
            }
        },

        findMoldFromList: function (results, moldId) {
            for (var j = 0; j < results.length; j++) {
                if (typeof results[j].mold !== 'object') {
                    continue;
                }

                var mold = results[j].mold;
                if (moldId == mold.id) {
                    return mold;
                    break;
                }

                // parts에서 검색
                if (mold.part != null && mold.part.molds != null) {
                    var findMold = this.findMold(mold.part.molds, moldId);
                    if (typeof findMold === 'object') {
                        return findMold;
                        break;
                    }
                }
            }
        },

        findMold: function (results, moldId) {
            for (var j = 0; j < results.length; j++) {
                if (typeof results[j] !== 'object') {
                    continue;
                }

                var mold = results[j];
                if (moldId == mold.id) {
                    return mold;
                    break;
                }

                // parts에서 검색
                if (mold.part != null && mold.part.molds != null) {
                    var findMold = this.findMold(mold.part.molds, moldId);
                    if (typeof findMold === 'object') {
                        return findMold;
                        break;
                    }
                }
            }
        },

        ratio: function (mold) {
            return ((mold.lastShot / mold.nextMaintenanceShot) * 100).toFixed(1);
        },
        ratioStyle: function (mold) {
            return 'width: ' + this.ratio(mold) + '%';
        },
        ratioColor: function (mold) {
            var ratio = this.ratio(mold);

            if (ratio < 25) return 'bg-info';
            else if (ratio < 50) return 'bg-success';
            else if (ratio < 75) return 'bg-warning';
            else return 'bg-danger';
        },

        callbackMessageForm: function () {
            this.paging(1);
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
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
            } catch (error) {
                console.log(error);
            }
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
            vm.getResources();
        });
    },
});
