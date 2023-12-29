axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

var Page = Common.getPage('counters');

var vm = new Vue({
    el: '#app',
    data: {
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            query: '',
            status: 'all',
            sort: 'ci,desc',
            page: 1,

            operatingStatus: '',
            equipmentStatus: '',
        },

        codes: {},
        sortType: {
            CDATA: 'ci',
            COUNTER_ID: 'counter.equipmentCode',
            TOOLING_ID: 'counter.mold.equipmentCode',
            MESSAGE: 'message',
            LAST_TIME: 'cdata.createdAt',
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

        disable: function (user) {
            user.enabled = false;
            //this.save(user);
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
                .get('/api/counters/cdata-counters?' + param)
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
                // 1. 카운터 정보가 없는 경우 (숫자)
                if (typeof this.results[i] !== 'object') {
                    var counterId = this.results[i];
                    for (var j = 0; j < this.results.length; j++) {
                        if (typeof this.results[j] !== 'object') {
                            continue;
                        }

                        var counter = this.results[j];
                        if (counterId == counter.id) {
                            this.results[i] = counter;
                            break;
                        }

                        // parts에서 counter 검색
                        if (counter.mold != null && counter.mold.part != null && counter.mold.part.molds != null) {
                            this.results[i] = this.findCounter(counter.mold.part.molds, counterId);

                            if (typeof this.results[i] === 'object') {
                                break;
                            }

                            /*for (var k = 0; k < counter.mold.part.molds.length; k++) {
                                var partMold = counter.mold.part.molds[k];

                                if (typeof partMold !== 'object' || typeof partMold.counter !== 'object') {
                                    continue;
                                }
                                if (counterId == partMold.counter.id) {
                                    this.results[i] = partMold.counter;
                                    break;
                                }

                            }*/
                        }
                    }
                }
            }

            // 2. 카운터 > 몰드 정보가 없는 경우 (숫자)
            for (var i = 0; i < this.results.length; i++) {
                if (typeof this.results[i].mold !== 'object') {
                    var moldId = this.results[i].mold;
                    for (var j = 0; j < this.results.length; j++) {
                        if (typeof this.results[j].mold !== 'object' || this.results[j].mold == null) {
                            continue;
                        }

                        var mold = this.results[j].mold;
                        if (moldId == mold.id) {
                            this.results[i].mold = mold;
                            break;
                        }

                        // parts에서 검색
                        if (mold.part != null && mold.part.molds != null) {
                            this.results[i].mold = this.findMold(mold.part.molds, moldId);

                            if (typeof this.results[i].mold === 'object') {
                                break;
                            }
                        }
                    }
                }
            }
        },

        findCounter: function (molds, counterId) {
            console.log('>> ' + counterId);
            for (var k = 0; k < molds.length; k++) {
                var mold = molds[k];

                if (typeof mold !== 'object' || typeof mold.counter !== 'object') {
                    continue;
                }

                console.log('>> ' + counterId);
                if (counterId == mold.counter.id) {
                    return mold.counter;
                    break;
                }

                // parts에서 검색
                if (mold.part != null && mold.part.molds != null) {
                    var findCounter = this.findCounter(mold.part.molds, counterId);
                    if (typeof findCounter === 'object') {
                        return findCounter;
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

        isUnchangeableEquipmentStatus: function (result) {
            var opStatus = result.operatingStatus;
            var equipmentStatus = result.equipmentStatus;

            if (equipmentStatus == 'DISCARDED' || (opStatus == 'ACTIVE' && equipmentStatus == 'INSTALLED')) {
                return true;
            }

            return false;
        },
        isChangeableEquipmentStatus: function (result, status) {
            var opStatus = result.operatingStatus;
            var equipmentStatus = result.equipmentStatus;

            if ('INSTALLED' == status) {
                return false;
            }

            if ('ACTIVE' == opStatus) {
                return false;
            }

            // 상태코드
            if (equipmentStatus == status || 'DISCARDED' == equipmentStatus) {
                return false;
            }

            return true;
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
