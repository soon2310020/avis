axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'Reset' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};

var Page = Common.getPage('molds');

var vm = new Vue({
    el: '#app',
    components: {
        'company-details': httpVueLoader('/components/company-details.vue'),
        'preset-form': httpVueLoader('/components/preset-form.vue'),
        'system-note': httpVueLoader('/components/system-note.vue'),
        'op-status': httpVueLoader('/components/configuration/OpStatus.vue'),
        'reset-drawer': httpVueLoader('/components/counter/ResetDrawer.vue'),
        'action-bar-feature': httpVueLoader('/components/new-feature/new-feature.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
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
            equipmentStatus: '',
        },

        codes: {},
        sortType: {
            TOOLING_ID: 'equipmentCode',
            COUNTER_ID: 'counter.equipmentCode',
            COMPANY: 'location.company.name',
            LOCATION: 'location.name',
            LAST_SHOT: 'lastShot',
            OP: 'operatingStatus',
            STATUS: 'counter.presetStatus',
        },
        sortTypeCounter: {
            COUNTER_ID: 'equipmentCode',
            TOOLING_ID: 'mold.equipmentCode',
            LOCATION: 'location.name',
            LAST_SHOT: 'shotCount',
            OP: 'operatingStatus',
            STATUS: 'presetStatus',
        },
        currentSortType: 'id',
        isDesc: true,
        tabType: 'tooling',
        showDropdown: false,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        listCheckedFull: [],
        // renderKey: true,
    },
    watch: {
        listChecked(newVal) {
            if (newVal && newVal.length != 0) {
                const self = this;
                console.log(vm.results, 'xxx');
                console.log(self.listChecked, 'xxx');
                const x = vm.results.filter((item) => self.listChecked.indexOf(item.id) > -1);
                console.log(x, 'xxx');
                this.listCheckedFull = x;
            }
        },
    },
    methods: {
        showMoldDetails: function (mold) {
            var child = Common.vue.getChild(this.$children, 'mold-details');
            if (child != null) {
                child.showMoldDetails(mold);
            }
        },
        showLocationHistory: function (mold) {
            var child = Common.vue.getChild(this.$children, 'location-history');
            if (child != null) {
                child.showLocationHistory(mold);
            }
        },

        showCompanyDetailsById: function (company) {
            var child = Common.vue.getChild(this.$children, 'company-details');
            if (child != null) {
                child.showDetailsById(company);
            }
        },

        select: function (event) {
            let isAll = event.target.checked;
            this.isAll = isAll;
            if (isAll) {
                this.listChecked = this.results.map((value) => value.id);
                Common.setHeightActionBar();
                this.isAllTracked.push({
                    page: this.requestParam.page,
                    status: this.requestParam.status,
                });
            } else {
                this.listChecked = [];
                this.isAllTracked = this.isAllTracked.filter(
                    (item) => item.page !== this.requestParam.page && item.status !== this.requestParam.status
                );
            }
            this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
        },
        checkSelect: function (id) {
            const findIndex = this.listChecked.findIndex((value) => {
                return value == id;
            });

            return findIndex !== -1;
        },
        hasChecked: function () {
            let isExistedChecked = false;
            Object.keys(this.listCheckedTracked).forEach((status) => {
                Object.keys(this.listCheckedTracked[status]).forEach((page) => {
                    if (this.listCheckedTracked[status][page].length > 0) {
                        isExistedChecked = true;
                    }
                });
            });
            return isExistedChecked;
        },
        check: function (e, result) {
            if (e.target.checked) {
                this.listChecked.push(+e.target.value);
                Common.setHeightActionBar();
            } else {
                this.listChecked = this.listChecked.filter((value) => value != e.target.value);
                if (this.listChecked.length === 0) {
                    this.isAll = false;
                    this.isAllTracked = this.isAllTracked.filter(
                        (item) => item.page !== this.requestParam.page && item.status !== this.requestParam.status
                    );
                }
            }
            this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
        },
        tab: function (status) {
            this.total = null;
            this.listChecked = [];
            this.listCheckedFull = [];
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.status = status;
            this.tabType = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },

        disable: function (user) {
            user.enabled = false;
            //this.save(user);
        },

        closeShowResetDropDown() {
            this.showResetDropdown = false;
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

            let requestParamConvert = self.requestParam;
            if (self.requestParam.operatingStatus === 'ALL') {
                requestParamConvert.operatingStatus = '';
            }

            if (self.requestParam.equipmentStatus === 'ALL') {
                requestParamConvert.equipmentStatus = '';
            }

            var apiLink = Page.API_BASE;
            if (this.tabType == 'counter') {
                apiLink = '/api/counters';
                requestParamConvert.notMold = true;
            }
            // checking is All
            this.isAll = false;
            let pageSelectedAll = this.isAllTracked.filter((item) => item.status === this.requestParam.status).map((item) => item.page);
            // if (pageSelectedAll.includes(this.requestParam.page)) {
            //     this.isAll = true;
            // }
            if (!this.listCheckedTracked[this.requestParam.status]) {
                this.listCheckedTracked[this.requestParam.status] = {};
            }
            if (!this.listCheckedTracked[this.requestParam.status][this.requestParam.page]) {
                this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = [];
            }
            var param = Common.param(requestParamConvert);

            axios
                .get(apiLink + '?' + param)
                .then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);

                    // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
                    self.setResultObject();

                    Common.handleNoResults('#app', self.results.length);

                    var pageInfo = self.requestParam.page == 1 ? '' : '?page=' + self.requestParam.page;
                    history.pushState(null, null, Common.$uri.pathname + pageInfo);
                    if (self.results.length > 0) {
                        Common.triggerShowActionbarFeature(self.$children);
                    }
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

        showPresetForm: function (mold) {
            console.log(mold);
            //alert(counter.equipmentCode);
            var child = Common.vue.getChild(this.$children, 'reset-drawer');
            if (child != null) {
                child.onShow(mold, this.tabType);
            }
        },

        // Preset 신청 정보
        showPresetDetails: function (mold) {
            var self = this;
            axios.get('/api/presets/' + mold.counterCode).then(function (response) {});

            var child = Common.vue.getChild(this.$children, 'preset-form');
            if (child != null) {
                child.showModal(mold);
            }
        },

        callbackPresetForm: function (result) {
            if (result == true) {
                Common.alert('success');
            }
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
        showSystemNoteModal(result) {
            console.log(this.listCheckedFull, 'listCheckedFull');
            console.log(result, 'result');
            var child = Common.vue.getChild(this.$children, 'system-note');
            if (child != null) {
                child.showSystemNote({ ...result, objectType: this.tabType == 'counter' ? 'COUNTER' : 'TOOLING' });
            }
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
            } catch (error) {
                console.log(error);
            }
        },

        reloadPage() {
            this.paging(this.requestParam.page);
        },
        handleClickReset(action, item) {
            if (item) {
                let tabType = null;
                let itemReset = null;
                switch (action) {
                    case this.ACTION.RESET:
                    case this.ACTION.EDIT_RESET:
                        tabType = item.mold && item.equipmentStatus != 'AVAILABLE' ? 'mold' : 'counter';
                        itemReset = tabType == 'counter' ? item : item.mold;
                        this.onShowResetModal(itemReset, tabType);
                        break;
                    case this.ACTION.CANCEL_RESET:
                        //todo: call show popup action
                        tabType = item.mold && item.equipmentStatus != 'AVAILABLE' ? 'mold' : 'counter';
                        itemReset = tabType == 'counter' ? item : item.mold;
                        this.onShowCancelPopup(itemReset, tabType);
                        break;
                }
            }
        },
        getSingleCheckedItemInfo() {
            let el = this.results.filter((element) => element.id == this.listChecked[0]);
            console.log(el);
            return el[0];
        },
        getPresetData() {
            let el = this.results.filter((item) => item.id == this.listChecked[0]);
            if (el) {
                return el[0];
            } else {
                return '';
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
