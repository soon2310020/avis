axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';

window.onload = function () {
    document.title = 'Reject Rate | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};

var Page = Common.getPage('rejected-part');

var vm = new Vue({
    el: '#app',
    components: {
        'op-status': httpVueLoader('/components/configuration/OpStatus.vue'),
        'date-filter': httpVueLoader('/components/reject-rates/date-filter.vue'),
        'reject-detail': httpVueLoader('/components/reject-rates/reject-detail.vue'),
        'reject-edit': httpVueLoader('/components/reject-rates/reject-edit.vue'),
        'reject-edit-multiple': httpVueLoader('/components/reject-rates/reject-edit-multiple.vue'),
        'action-bar-feature': httpVueLoader('/components/new-feature/new-feature.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        'chart-part': httpVueLoader('/components/chart-part.vue'),
        // 'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
        'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
        'location-history': httpVueLoader('/components/location-history.vue'),
        'file-previewer': httpVueLoader('/components/mold-detail/file-previewer.vue'),

        // 'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
        // 'chart-mold': httpVueLoader('/components/chart-mold.vue'),
        // 'date-picker': httpVueLoader('/components/reject-rates/date-picker.vue'),

        // update modal
        'reject-rate-modal': httpVueLoader('/components/reject-rates/reject-rate-modal.vue'),
        'date-picker': httpVueLoader('/components/reject-rates/components/date-picker.vue'),
        'date-picker-button': httpVueLoader('/components/common/date-picker/date-picker-button.vue'),
        'view-modal': httpVueLoader('/components/reject-rates/reject-rate-view-modal.vue'),
        // 'chart-part': httpVueLoader('/components/chart-part.vue'),
        // 'chart-part': httpVueLoader('/components/chart-part-tool.vue'),
        // 'chart-mold': httpVueLoader('/components/chart-mold.vue'),
        'location-history': httpVueLoader('/components/location-history.vue'),
        'file-previewer': httpVueLoader('/components/mold-detail/file-previewer.vue'),
        'date-picker-modal': httpVueLoader('/components/common/date-picker/date-picker-modal.vue'),
        'date-picker-popup': httpVueLoader('/components/reject-rates/date-picker-popup.vue'),
        'machine-details': httpVueLoader('/components/machine-details.vue'),
        'customization-modal': httpVueLoader('/components/customization-modal.vue'),
    },
    data: {
        resources: {},
        getDate: '',
        showDrowdown: null,
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            // accessType: 'ADMIN_MENU',
            day: moment(new Date()).format('YYYYMMDD'),
            // query: '',
            rejectedRateStatus: null,
            // sort: 'id,desc',
            page: 1,
            frequent: 'DAILY',
            // id: '',
        },
        toolingId: '',
        sortType: {
            MACHINE_ID: 'machineCode',
            PART_ID: 'part.partCode',
            TOOLING_ID: 'mold.equipmentCode',
            COMPANY_NAME: 'mold.location.company.name',
            PRODUCED_AMOUNT: 'totalProducedAmount',
            DAILY_REJECT_RATE: 'rejectedRate',
            AVG_REJECT_RATE: 'avgRejectedRate',
            STATUS: 'rejectedRateStatus',
        },
        statusType: {
            ALL: null,
            COMPLETED: 'COMPLETED',
            UNCOMPLETED: 'UNCOMPLETED',
        },
        comparisonTrend: {
            UP: 'UP',
            DOWN: 'DOWN',
        },
        currentSortType: 'id',
        isDesc: true,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        fixedRejectReasons: [
            'Black Dot',
            'Bubbles',
            'Burn Mark',
            'Dented',
            'Drag Mark',
            'Flashing',
            'Flow Mark',
            'Gas Mark',
            'Loose Burr',
            'Missing',
            'Oily',
            'Parting Line',
            'Pin Mark',
            'Wrinkle',
        ],
        cancelToken: undefined,
        showDatePicker: false,
        tempDateData: {
            from: new Date(),
            to: new Date(),
            fromTitle: moment().format('YYYY-MM-DD'),
            toTitle: moment().format('YYYY-MM-DD'),
        },
        hour: {
            from: '00',
            to: '01',
        },
        showRejectRateModal: false,
        listCheckedToolings: [],
        isChangingDate: false,
        timeRange: {
            minDate: null,
            maxDate: new Date('2100-01-01'),
        },
        allColumnList: [],
        pageType: 'REJECT_RATE',
    },
    async created() {
        this.timeOption = [
            { title: 'Daily', type: 'DAILY', isRange: false },
            { title: 'Weekly', type: 'WEEKLY', isRange: false },
            { title: 'Monthly', type: 'MONTHLY', isRange: false },
        ];
        this.today = new Date();
        const response = await this.getCurrentFrequency();
        const frequency = response.data.data.frequent;
        console.log('updateRejectRateConfig', frequency, response);
        this.requestParam.frequent = frequency;
        this.initCurrentDate();
    },
    computed: {
        getModalFrequency() {
            console.log(this.requestParam.frequent);
            return [`${this.requestParam.frequent}_RANGE`];
        },
    },
    watch: {
        listChecked(newVal) {
            if (newVal && newVal.length != 0) {
                const self = this;
                const x = this.results.filter((item) => self.listChecked.indexOf(item.id) > -1);
                this.listCheckedFull = x;
            }
        },
        tempDateData(newValue) {
            console.log('change temp date data', newValue);
        },
    },
    methods: {
        saveSelectedList: function (dataCustomize, list) {
            const dataFake = list.map((item, index) => {
                if (item.field) {
                    return { ...item, position: index };
                }
            });
            this.allColumnList = dataFake;
            const self = this;
            let data = list.map((item, index) => {
                let response = {
                    columnName: item.field,
                    enabled: item.enabled,
                    position: index,
                };
                if (item.id) {
                    response.id = item.id;
                }
                return response;
            });
            axios
                .post('/api/config/update-column-config', {
                    pageType: this.pageType,
                    columnConfig: data,
                })
                .then((response) => {
                    let hashedByColumnName = {};
                    response.data.forEach((item) => {
                        hashedByColumnName[item.columnName] = item;
                    });
                    self.allColumnList.forEach((item) => {
                        if (hashedByColumnName[item.field] && !item.id) {
                            item.id = hashedByColumnName[item.field].id;
                            item.position = hashedByColumnName[item.field].position;
                        }
                    });
                })
                .finally(() => {
                    self.allColumnList.sort(function (a, b) {
                        return a.position - b.position;
                    });
                    self.$forceUpdate();
                });
        },
        callSetAllColumnList() {
            if (this.resources && Object.entries(this.resources).length > 0 && this.allColumnList.length === 0) {
                this.setAllColumnList();
            }
        },
        setAllColumnList() {
            this.allColumnList = [
                {
                    label: this.resources['tooling_id'],
                    field: this.sortType.TOOLING_ID,
                    mandatory: true,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.TOOLING_ID,
                    defaultSelected: true,
                    defaultPosition: 0,
                },
                {
                    label: this.resources['machine_id'],
                    field: this.sortType.MACHINE_ID,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.MACHINE_ID,
                    defaultSelected: true,
                    defaultPosition: 1,
                },
                {
                    label: this.resources['parts'],
                    field: this.sortType.PART_ID,
                    sortable: true,
                    default: true,
                    sortField: this.sortType.PART_ID,
                    defaultSelected: true,
                    defaultPosition: 2,
                },
                {
                    label: this.resources['company'],
                    field: this.sortType.COMPANY_NAME,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.COMPANY_NAME,
                    defaultSelected: true,
                    defaultPosition: 3,
                },
                {
                    label: this.resources['total_produced_amount'],
                    field: this.sortType.PRODUCED_AMOUNT,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.PRODUCED_AMOUNT,
                    defaultSelected: true,
                    defaultPosition: 4,
                },
                {
                    label: this.resources['daily_reject_rate'],
                    field: this.sortType.DAILY_REJECT_RATE,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.DAILY_REJECT_RATE,
                    defaultSelected: true,
                    defaultPosition: 5,
                },
                {
                    label: this.resources['avg_reject_rate'],
                    field: this.sortType.AVG_REJECT_RATE,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.AVG_REJECT_RATE,
                    defaultSelected: true,
                    defaultPosition: 6,
                },
                {
                    label: this.resources['status'],
                    field: this.sortType.STATUS,
                    default: true,
                    sortable: true,
                    sortField: this.sortType.STATUS,
                    defaultSelected: true,
                    defaultPosition: 7,
                },
            ];
            try {
                this.resetColumnsListSelected();
                this.getColumnListSelected();
            } catch (e) {
                console.log(e);
                this.resetColumnsListSelected();
                this.getColumnListSelected();
            }
            // this.resetColumnsListSelected();
            // this.getColumnListSelected();
        },
        resetColumnsListSelected() {
            this.allColumnList.forEach((item) => {
                item.enabled = !!item.default;
                if (item.sortable && !item.sortField) {
                    item.sortField = item.field;
                }
            });
        },
        getColumnListSelected() {
            axios
                .get(`/api/config/column-config?pageType=${this.pageType}`)
                .then((response) => {
                    if (response.data.length) {
                        let hashedByColumnName = {};
                        response.data.forEach((item) => {
                            hashedByColumnName[item.columnName] = item;
                        });
                        this.allColumnList.forEach((item) => {
                            if (hashedByColumnName[item.field]) {
                                item.enabled = hashedByColumnName[item.field].enabled;
                                item.id = hashedByColumnName[item.field].id;
                                item.position = hashedByColumnName[item.field].position;
                            }
                        });
                        this.allColumnList.sort(function (a, b) {
                            return a.position - b.position;
                        });
                        this.$forceUpdate();
                        let child = Common.vue.getChild(this.$children, 'show-columns');
                        if (child != null) {
                            child.$forceUpdate();
                        }
                    }
                })
                .catch(function (error) {
                    // Common.alert(error.response.data);
                });
        },
        showFilePreviewer(file) {
            var child = Common.vue.getChild(this.$children, 'file-previewer');
            if (child != null) {
                child.showFilePreviewer(file);
            }
        },
        backToMoldDetail() {
            console.log('back to molds detail');
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
                child.backFromPreview();
            }
        },

        showPartChart: function (part, mold) {
            part.id = part.partId;
            var child = Common.vue.getChild(this.$children, 'chart-part');
            if (child != null) {
                child.showChartPartMold(part, mold, 'QUANTITY', 'DAY');
            }
        },
        showChart: function (mold, tab) {
            console.log(mold, 'molddd');
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
                child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
            }
        },
        showMoldDetailsById: function (moldId) {
            let param = `id=${moldId}&page=1`;
            axios.get('/api/molds?' + param).then((response) => {
                let mold = response.data.content[0];
                this.showMoldDetails(mold);
            });
        },
        showMoldDetails: function (mold) {
            // var child = Common.vue.getChild(this.$children, 'mold-details');
            // if (child != null) {
            //     child.showMoldDetails(mold);
            // }
            const tab = 'switch-detail';
            this.showChart(mold, tab);
        },
        goToEdit() {
            console.log(this.listChecked[0], 'this.listChecked[0]');
            window.location.href = `/admin/reject-rates/edit?id=${this.listChecked[0]}` + '&date=' + this.requestParam.startDate;
        },
        category: function () {
            this.paging(1);
        },

        tab: function (status) {
            this.total = null;
            this.isAll = false;
            this.listChecked = [];
            this.isAllTracked = [];
            this.listCheckedTracked = {};
            this.$forceUpdate();
            this.currentSortType = 'id';
            // this.requestParam.sort = 'id,desc';
            this.requestParam.rejectedRateStatus = status;
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
                vm.callSetAllColumnList();
            } catch (error) {
                console.log(error);
            }
        },
        disable: function (part) {
            part.enabled = false;
            this.save(part);
        },
        paging: function (pageNumber) {
            let self = this;

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
            // this.listChecked = []
            // this.listCheckedFull = []
            // this.isAll = false
            // this.isAllTracked = []
            // this.listCheckedToolings = []
            let requestParam = Object.assign({}, self.requestParam);
            requestParam.page = pageNumber === undefined ? 1 : pageNumber;
            if (!requestParam.rejectedRateStatus) {
                delete requestParam.rejectedRateStatus;
            }
            let param = Common.param(requestParam);
            if (typeof this.cancelToken != typeof undefined) {
                this.cancelToken.cancel('new request');
            }
            this.cancelToken = axios.CancelToken.source();
            axios
                .get(Page.API_BASE + '?' + param, {
                    cancelToken: this.cancelToken.token,
                })
                .then((response) => {
                    let moldByMoldIds = {};
                    response.data.content.forEach((item) => {
                        if (!moldByMoldIds[item.moldId] && item.mold && item.mold.id) {
                            moldByMoldIds[item.moldId] = item.mold;
                        }
                    });
                    // response.data.content.sort((first, second) => {
                    //     return first.moldId < second.moldId ? 1 : -1;
                    // });
                    response.data.content.forEach((item, index) => {
                        if (index === 0) {
                            item.identifiedClass = 'odd';
                        } else {
                            let previousResult = response.data.content[index - 1];
                            if (item.moldId === previousResult.moldId) {
                                item.identifiedClass = previousResult.identifiedClass;
                            } else {
                                item.identifiedClass = previousResult.identifiedClass === 'odd' ? 'even' : 'odd';
                                item.positionClass = 'first';
                                previousResult.positionClass = 'last';
                            }
                            if (index === response.data.content.length - 1) {
                                item.positionClass = 'last';
                            }
                        }
                        item.mold = moldByMoldIds[item.moldId];
                    });
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);
                    Common.handleNoResults('#app', self.results.length);

                    if (this.isChangingDate) {
                        this.isChangingDate = false;
                    } else {
                        if (self.listCheckedTracked[self.requestParam.status][self.requestParam.page]) {
                            self.listChecked = self.listCheckedTracked[self.requestParam.status][self.requestParam.page];
                        }
                    }

                    if (self.results.length > 0) {
                        self.$nextTick(() => {
                            Common.triggerShowActionbarFeature(self.$children);
                        });
                    }

                    self.listCheckedFull = self.listCheckedFull.map((item) => {
                        const updatedItemIndex = self.results.findIndex((newItem) => newItem.id === item.id);
                        const updatedItem = updatedItemIndex > -1 ? self.results[updatedItemIndex] : item;
                        return updatedItem;
                    });
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
        showRejectDetail(result) {
            let child = Common.vue.getChild(this.$children, 'reject-detail');
            if (child != null) {
                let startDate = this.tempDateData.fromTitle.replaceAll('-', '');
                let passDateData = {
                    currentParam: this.requestParam,
                    startDate: startDate,
                };
                child.showRejectDetail(result, passDateData);
            }
        },
        showRejectDetail2(result) {
            let child = Common.vue.getChild(this.$children, 'view-modal');
            if (child != null) {
                let startDate = this.tempDateData.fromTitle.replaceAll('-', '');
                let endDate = this.tempDateData.toTitle.replaceAll('-', '');
                let passDateData = {
                    currentParam: this.requestParam,
                    startDate: startDate,
                    endDate: endDate,
                    hour: { ...this.hour },
                };
                child.showRejectDetail(result, passDateData, this.tempDateData);
            }
        },
        filterWithDate(date) {
            this.requestParam.startDate = date;
            this.requestParam.endDate = date;
            this.getDate = date;
            this.paging(1);
        },
        isToday(day) {
            return day === moment().format('YYYYMMDD');
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
        check: function (e) {
            console.log('check: function (e');
            if (e.target.checked) {
                this.listChecked.push(parseInt(e.target.value));
                Common.setHeightActionBar();
            } else {
                this.listChecked = this.listChecked.filter((value) => value != e.target.value);
                if (this.listChecked.length == 0) {
                    this.isAll = false;
                    this.isAllTracked = this.isAllTracked.filter(
                        (item) => item.page !== this.requestParam.page && item.status !== this.requestParam.status
                    );
                }
            }
            this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
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
        editReject(result) {
            let child = Common.vue.getChild(this.$children, 'reject-edit');
            if (child != null) {
                child.showRejectDetail(result);
            }
        },
        editRejectMultiple() {
            let child = Common.vue.getChild(this.$children, 'reject-edit-multiple');
            let url = this.results
                .filter((result) => this.listChecked.includes(result.id))
                .map((item) => item.id)
                .toString();
            window.location.href = `reject-rates/multiple-edit?ids=${url}&startDate=${this.getDate}&endDate=${this.getDate}`;
            // let data = this.results.filter(result => this.listChecked.includes(result.id));
            // if (child != null) {
            //     child.showRejectMultipleModal(data);
            // }
        },
        updateDataDone() {
            Common.alert('Success');
            this.isAll = false;
            this.listChecked = [];
            this.isAllTracked = [];
            this.listCheckedTracked = {};
            this.paging(this.requestParam.page);
        },

        openRejectRateModal() {
            var child = Common.vue.getChild(this.$children, 'reject-rate-modal');
            if (child != null) {
                child.openRejectRateModal(this.listCheckedFull);
            }
            /*
                                $('#op-reject-rate-modal').modal('show')
                                this.showRejectRateModal = true
                                this.listCheckedToolings = this.listCheckedFull
            */
        },
        /*
                        closeRejectRateModal() {
                            $('#op-reject-rate-modal').modal('hide')
                            this.showRejectRateModal = false
                        },
        */
        openDatePickerModal() {
            let child = Common.vue.getChild(this.$children, 'date-picker');
            if (child != null) {
                child.handleShowModal(this.tempDateData);
            }
            let root = null;
            let modalBody = null;
            let runCount = 0;
            let waitRender = setInterval(() => {
                runCount++;
                root = document.getElementById('date-picker-modal');
                if (root) {
                    modalBody = root.querySelectorAll('.ant-modal-content');
                    if (modalBody) {
                        clearInterval(waitRender);
                        const el = modalBody[0];
                        el.classList.add('fade-in-animate');
                        setTimeout(() => {
                            el.style.opacity = 1;
                            setTimeout(() => {
                                el.classList.remove('fade-in-animate');
                            }, 50);
                        }, 200);
                    }
                }
            }, 50);
        },
        openDatePickerModal1() {
            let child = Common.vue.getChild(this.$children, 'date-picker-modal');
            if (child != null) {
                child.showDatePicker(this.requestParam.frequent);
            }
        },
        showAnimation() {
            const el = document.getElementById('reject-rate-date-picker');
            el.classList.add('dropdown_button-animation');
            setTimeout(() => {
                this.openDatePickerModal1();
                setTimeout(() => {
                    el.classList.remove('dropdown_button-animation');
                }, 250);
            }, 450);
        },
        handleChangeDate(data) {
            console.log('change date', data);
            this.tempDateData = data;
            // switch (data.rangeType) {
            //     case 'WEEKLY':
            //         delete this.requestParam.month
            //         delete this.requestParam.day
            //         this.requestParam.week = data.fromTitle.replaceAll('-', '')
            //         this.requestParam.startDate = data.from.replaceAll('-', '')
            //         break;
            //     case 'MONTHLY':
            //         delete this.requestParam.day
            //         delete this.requestParam.week
            //         delete this.requestParam.startDate
            //         this.requestParam.month = data.fromTitle.replaceAll('-', '')
            //         break;
            //     case 'DAILY':
            //         delete this.requestParam.month
            //         delete this.requestParam.week
            //         delete this.requestParam.startDate
            //         this.requestParam.day = data.from.replaceAll('-', '')
            //         break;
            // }
            // delete this.requestParam.endDate
            // this.requestParam.frequent = data.rangeType
            // console.log('change date after', this.requestParam)

            // let child = Common.vue.getChild(this.$children, 'date-picker');
            // if (child != null) {
            //     child.closeDatePicker();
            // }
            // this.isChangingDate = true;
            // this.paging(1);
            // this.listChecked = []
            // this.listCheckedFull = []
            // this.isAll = false
            // this.isAllTracked = []
            // this.listCheckedToolings = []
        },
        handlePickDate(data, frequency) {
            console.log('change date', data, frequency);
            this.tempDateData = data;
            switch (frequency) {
                case 'WEEKLY':
                    delete this.requestParam.month;
                    delete this.requestParam.day;
                    this.requestParam.week = data.fromTitle.replaceAll('-', '');
                    this.requestParam.startDate = moment(data.from).format('YYYYMMDD');
                    delete this.requestParam.start;
                    delete this.requestParam.end;
                    break;
                case 'MONTHLY':
                    delete this.requestParam.day;
                    delete this.requestParam.week;
                    delete this.requestParam.startDate;
                    this.requestParam.month = data.fromTitle.replaceAll('-', '');
                    delete this.requestParam.start;
                    delete this.requestParam.end;
                    break;
                case 'DAILY':
                    delete this.requestParam.month;
                    delete this.requestParam.week;
                    delete this.requestParam.startDate;
                    this.requestParam.day = moment(data.from).format('YYYYMMDD');
                    delete this.requestParam.start;
                    delete this.requestParam.end;
                    break;
                case 'HOURLY':
                    this.hour = data.hour;
                    delete this.requestParam.month;
                    delete this.requestParam.week;
                    delete this.requestParam.startDate;
                    delete this.requestParam.day;
                    this.requestParam.hour = `${moment(data.from).format('YYYYMMDD')}${data.hour.from}`;
                    delete this.requestParam.start;
                    delete this.requestParam.end;
                    break;
            }
            delete this.requestParam.endDate;
            this.requestParam.frequent = frequency;
            console.log('change date after', this.requestParam);

            this.isChangingDate = true;
            this.paging(1);
            this.listChecked = [];
            this.listCheckedFull = [];
            this.isAll = false;
            this.isAllTracked = [];
            this.listCheckedToolings = [];
            let child = Common.vue.getChild(this.$children, 'date-picker-modal');
            if (child != null) {
                child.closeDatePicker();
            }
        },
        async getCurrentFrequency() {
            const res = await axios.get('/api/rejected-part/get-configuration');
            return res;
        },
        initCurrentDate() {
            let fromTitle = '';
            let toTitle = '';
            if (this.requestParam.frequent == 'DAILY' || this.requestParam.frequent == 'HOURLY') {
                fromTitle = moment().format('YYYY-MM-DD');
                toTitle = moment().format('YYYY-MM-DD');
            } else if (this.requestParam.frequent == 'MONTHLY') {
                fromTitle = moment().format('YYYY-MM');
                toTitle = moment().format('YYYY-MM');
            } else if (this.requestParam.frequent == 'WEEKLY') {
                const currentWeek = Common.getCurrentWeekNumber(new Date());
                fromTitle = `${this.today.getFullYear()}-${currentWeek}`;
                toTitle = `${this.today.getFullYear()}-${currentWeek}`;
            }
            const hour = this.today.getHours();
            this.hour.from = `${hour < 10 ? '0' + hour : hour}`;
            this.hour.to = `${hour + 1 < 10 ? '0' + (hour + 1) : hour + 1}`;
            this.tempDateData = {
                from: new Date(),
                to: new Date(),
                fromTitle: fromTitle,
                toTitle: toTitle,
            };
            console.log('initCurrentDate', this.requestParam.frequent, this.tempDateData);
        },
        async showMachineDetails(item) {
            console.log('showMachineDetails', item);
            const id = item.id;
            try {
                // const res = await axios.get(`/api/machines/${machineId}`)
                // const machine = res.data
                this.$refs.machineDetails.showDetailsById(id);
            } catch (error) {
                console.log(error);
            }
        },
        closeMachineDetails() {
            this.$refs.machineDetails.dimissModal();
        },
    },
    mounted() {
        console.log('%c track', 'background: green; color: white');
        console.log('check date', this.tempDateData);
        this.$nextTick(function () {
            vm.getResources();
        });
        this.paging(1);
    },
});
