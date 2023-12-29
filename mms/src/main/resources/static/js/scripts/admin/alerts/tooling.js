$(function () {
    $('#failureDateTime').datetimepicker({
        ignoreReadonly: true,
        focusOnShow: true,
        format: 'YYYY-MM-DD HH:mm:ss',
    });
});

axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

var Page = Common.getPage('molds');

var vm = new Vue({
    el: '#app',
    components: {
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        'corrective-form': httpVueLoader('/components/corrective-form.vue'),
        'location-history': httpVueLoader('/components/location-history.vue'),
    },
    data: {
        resources: {},
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            query: '',
            status: 'all',
            sort: 'id,desc',
            page: 1,

            operatingStatus: '',
            equipmentStatus: 'INSTALLED',
            extraStatus: 'op-status-is-null',
        },

        equipmentStatusCodes: [],
        listCategories: [],
        codes: {},
        sortType: {
            TOOLING_ID: 'equipmentCode',
            COUNTER_ID: 'counter.equipmentCode',
            LOCATION: 'location.name',
            STATUS: 'equipmentStatus',
            UPDATED_AT: 'updatedAt',
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

        showLocationHistory: function (mold) {
            var child = Common.vue.getChild(this.$children, 'location-history');
            if (child != null) {
                child.showLocationHistory(mold);
            }
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

                    // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
                    self.setResultObject();

                    // 카테고리 정보 저장.
                    self.setCategories();

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
                if (typeof this.results[i] !== 'object') {
                    var moldId = this.results[i];
                    this.results[i] = this.findMold(this.results, moldId);
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
        setCategories: function () {
            // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
            for (var i = 0; i < this.results.length; i++) {
                if (this.results[i].part != null && this.results[i].part.category != null) {
                    if (typeof this.results[i].part.category !== 'object' && this.results[i].part.categoryId > 0) {
                        var categoryId = this.results[i].part.categoryId;
                        this.results[i].part.category = this.findCategory(this.results, categoryId);
                    }
                }
            }

            for (var i = 0; i < this.results.length; i++) {
                if (this.results[i].part != null && this.results[i].part.category != null) {
                    if (typeof this.results[i].part.category.parent !== 'object') {
                        var categoryId = this.results[i].part.category.parent;
                        this.results[i].part.category.parent = this.findCategory(this.results, categoryId);
                    }
                }
            }
        },

        findCategory: function (results, categoryId) {
            for (var i = 0; i < results.length; i++) {
                var mold = results[i];
                if (mold.part == null || mold.part.category == null || typeof mold.part.category !== 'object') {
                    continue;
                }

                var category = mold.part.category;
                if (categoryId == category.id) {
                    return category;
                    break;
                }

                if (category.parent != null && categoryId == category.parent.id) {
                    return category.parent;
                    break;
                }

                // 부모의 자삭에서 찾기
                if (category.parent != null && category.parent.children != null) {
                    for (var j = 0; j < category.parent.children.length; j++) {
                        var parentChild = category.parent.children[j];
                        if (typeof parentChild !== 'object') {
                            continue;
                        }
                        if (categoryId == parentChild.id) {
                            return parentChild;
                            break;
                        }
                    }
                }

                // parts에서 검색
                if (mold.part.molds != null) {
                    var findCategory = this.findCategory(mold.part.molds, categoryId);
                    if (typeof findCategory === 'object') {
                        return findCategory;
                        break;
                    }
                }
            }
        },

        showMoldDetails: function (mold) {
            var child = Common.vue.getChild(this.$children, 'mold-details');
            if (child != null) {
                child.showMoldDetails(mold);
            }
        },

        // 'failure' 선택 시 고장시간 등록 Modal
        showCorrective: function (mold) {
            var child = Common.vue.getChild(this.$children, 'corrective-form');
            if (child != null) {
                child.showModal(mold);
            }
        },
        callbackCorrectiveForm: function (mold) {
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
