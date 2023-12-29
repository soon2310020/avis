axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'Checklist' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};

$(function () {
    var selectTarget = $('.selectbox select');

    selectTarget.on({
        focus: function () {
            $(this).parent().addClass('focus');
        },
        blur: function () {
            $(this).parent().removeClass('focus');
        },
    });

    selectTarget.change(function () {
        var select_name = $(this).children('option:selected').text();
        $(this).siblings('label').text(select_name);
    });
});

var Page = Common.getPage('checklist');

var vm = new Vue({
    el: '#app',
    components: {
        'revision-history': httpVueLoader('/components/version/view-revision-history.vue'),
        'system-note': httpVueLoader('/components/system-note.vue'),
        'show-columns': httpVueLoader('/components/part/show-columns.vue'),
        'customization-modal': httpVueLoader('/components/customization-modal.vue'),
        'action-bar-feature': httpVueLoader('/components/new-feature/new-feature.vue'),
    },
    data: {
        showDrowdown: null,
        resources: {},
        results: [],
        total: 0,
        pagination: [],
        requestParam: {
            query: '',
            companyType: 'all',
            enabled: true,
            sort: 'id,desc',
            page: 1,
            id: '',
        },
        currentSortType: 'id',
        isDesc: true,
        pageType: 'CHECKLIST_MAINTENANCE',
        allColumnList: [],
        showDropdown: false,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        listCheckedFull: [],
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
        closeShowDropDown() {
            this.showDropdown = false;
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
        goToEdit() {
            console.log(this.listChecked[0], 'this.listChecked[0]');
            window.location.href = `/admin/maintenance-checklist/${this.listChecked[0]}/edit`;
        },
        setAllColumnList() {
            vm.allColumnList = [
                {
                    label: vm.resources['checklist_id'],
                    field: 'checklistCode',
                    default: true,
                    mandatory: true,
                    sortable: true,
                    defaultSelected: true,
                    defaultPosition: 0,
                },
                {
                    label: vm.resources['company_name'],
                    field: 'companyName',
                    default: true,
                    sortable: true,
                    sortField: 'company.name',
                    defaultSelected: true,
                    defaultPosition: 1,
                },
                {
                    label: vm.resources['company_id'],
                    field: 'companyId',
                    default: true,
                    sortable: true,
                    defaultSelected: true,
                    defaultPosition: 2,
                },
                {
                    label: vm.resources['address'],
                    field: 'address',
                    default: true,
                    sortable: true,
                    sortField: 'company.address',
                    defaultSelected: true,
                    defaultPosition: 3,
                },
                {
                    label: vm.resources['status'],
                    field: 'enabled',
                    default: true,
                    sortable: true,
                    defaultSelected: true,
                    defaultPosition: 4,
                },
            ];
            this.resetColumnsListSelected();
            this.getColumnListSelected();
        },
        resetColumnsListSelected() {
            this.allColumnList.forEach((item) => {
                item.enabled = !!item.default;
                if (item.sortable && !item.sortField) {
                    item.sortField = item.field;
                }
            });
        },
        handleResetColumnsListSelected: function () {
            this.resetColumnsListSelected();
            this.saveSelectedList();
            this.$forceUpdate();
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
        handleChangeValueCheckBox: function (value) {
            let column = this.allColumnList.filter((item) => item.field === value)[0];
            column.enabled = !column.enabled;
            this.saveSelectedList();
            this.$forceUpdate();
        },

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
        changeShow(resultId) {
            if (resultId != null) {
                this.showDrowdown = resultId;
            } else {
                this.showDrowdown = null;
            }
        },
        changeStatus: function () {
            this.paging(1);
        },

        tab: function (companyType) {
            this.total = null;
            this.listChecked = [];
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.enabled = true;
            this.requestParam.status = companyType;
            this.requestParam.companyType = companyType;
            this.paging(1);
        },
        tabToDisable: function (status) {
            this.listChecked = [];
            this.currentSortType = 'id';
            this.requestParam.sort = 'id,desc';
            this.requestParam.status = status;
            this.requestParam.enabled = false;
            this.requestParam.companyType = '';
            this.paging(1);
        },
        search: function (page) {
            this.paging(1);
        },

        deleteChecklist: function (checklist) {
            if (!confirm('Are you sure you want to delete?')) {
                return;
            }
            var param = {
                id: checklist.id,
            };
            axios
                .delete(Page.API_BASE + '/' + checklist.id, param)
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

        enable: function (user) {
            if (user.length <= 1) {
                user[0].enabled = true;
                this.save(user[0]);
            } else {
                user.forEach(function (item) {
                    item.enabled = true;
                });
                this.saveBatch(user, true);
            }
        },
        disable: function (user) {
            if (user.length <= 1) {
                user[0].enabled = false;
                this.save(user[0]);
            } else {
                user.forEach(function (item) {
                    item.enabled = false;
                });
                this.saveBatch(user, false);
            }
        },
        saveBatch(user, boolean) {
            console.log(user, 'user');
            const idArr = user.map((item) => {
                return item.id;
            });
            var param = {
                ids: idArr,
                enabled: boolean,
            };
            axios
                .post('/api/checklist/change-status-in-batch', param)
                .then(function (response) {
                    vm.paging(1);
                })
                .catch(function (error) {
                    console.log(error.response);
                })
                .finally(() => {
                    this.listChecked = [];
                    this.isAll = false;
                    this.showDropdown = false;
                });
        },

        save: function (checklist) {
            //var param = Common.param(item);
            var param = {
                id: checklist.id,
                enabled: checklist.enabled,
            };
            axios
                .put(Page.API_BASE + '/' + checklist.id + '/enable', param)
                .then(function (response) {
                    vm.paging(1);
                })
                .catch(function (error) {
                    console.log(error.response);
                })
                .finally(() => {
                    this.listChecked = [];
                    this.isAll = false;
                    this.showDropdown = false;
                });
        },
        paging: function (pageNumber) {
            var self = this;
            self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
            let requestParam = JSON.parse(JSON.stringify(self.requestParam));
            if (requestParam.companyType === 'all') {
                delete requestParam.companyType;
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

            var param = Common.param(requestParam);
            axios
                .get(Page.API_BASE + '?' + param)
                .then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);
                    Common.handleNoResults('#app', self.results.length);

                    var pageInfo = self.requestParam.page === 1 ? '' : '?page=' + self.requestParam.page;
                    history.pushState(null, null, Common.$uri.pathname + pageInfo);
                    if (self.results.length > 0) {
                        Common.triggerShowActionbarFeature(self.$children);
                    }
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
        showRevisionHistory: function (id) {
            var child = Common.vue.getChild(this.$children, 'revision-history');
            if (child != null) {
                child.showRevisionHistories(id, this.pageType);
            }
        },
        showSystemNoteModal(result) {
            var child = Common.vue.getChild(this.$children, 'system-note');
            if (child != null) {
                child.showSystemNote({ id: result });
            }
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
                vm.setAllColumnList();
            } catch (error) {
                console.log(error);
            }
        },
    },
    mounted() {
        this.$nextTick(function () {
            // 모든 화면이 렌더링된 후 실행합니다.

            this.paging(1);
            vm.getResources();
        });
    },
});
