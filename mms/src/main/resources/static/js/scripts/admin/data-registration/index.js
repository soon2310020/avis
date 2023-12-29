axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
window.onload = function () {
    document.title = 'Overall Data Registration Statistics';
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
    }, 500);
};
var Page = Common.getPage('data-registration');
var vm = new Vue({
    el: '#app',
    components: {
        'company-details': httpVueLoader('/components/company-details.vue'),
        'chart-part': httpVueLoader('/components/chart-part.vue'),
        'part-details': httpVueLoader('/components/part-details.vue'),
        'file-previewer': httpVueLoader('/components/mold-detail/file-previewer.vue'),
        'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
        'mold-details': httpVueLoader('/components/mold-details.vue'),
        'machine-details': httpVueLoader('/components/machine-details.vue'),
        'create-property-drawer': httpVueLoader('/components/import-tooling/CreatePropertyDrawer.vue'),
        'action-bar-feature': httpVueLoader('/components/new-feature/new-feature.vue'),
        'code-list-temp': httpVueLoader('/components/common/data-list-modal.vue'),
        'category-form-modal': httpVueLoader('/components/category/category-form-modal.vue'),
        'project-form-modal': httpVueLoader('/components/category/project-form-modal.vue'),
    },
    data: {
        categoryList: [
            { plural: 'Companies', singular: 'Company', code: 'companies', img: 'globe', number: 0, id: 1 },
            { plural: 'Locations', singular: 'Location', code: 'locations', img: 'push-pin', number: 0, id: 2 },
            { plural: 'Categories', singular: 'Category', code: 'categories', img: 'list', number: 0, id: 3 },
            { plural: 'Parts', singular: 'Part', code: 'parts', img: 'pie-chart', number: 0, id: 4 },
            { plural: 'Toolings', singular: 'Tooling', code: 'molds', img: 'four-squares-button-of-view-options', number: 0, id: 5 },
            { plural: 'Machines', singular: 'Machine', code: 'machines', img: 'robotics', number: 0, id: 6 },
        ],
        categorySelected: 'companies',
        companies: null,
        locations: null,
        categories: null,
        parts: null,
        molds: null,
        machines: null,
        codes: {},
        userType: '',
        total: 0,
        results: [],
        pagination: [],
        // serverName: '',
        isHover: 1,
        requestParam: {
            query: '',
            status: 'active',
            sort: 'id,desc',
            page: 1,
        },
        sortType: {
            NAME: 'name',
            COMPANY_CODE: 'companyCode',
            ADDRESS: 'address',
            STATUS: 'enabled',
            LOCATION_CODE: 'locationCode',
            COMPANY: 'company.name',
            NO_TERMINAL: 'numberTerminal',
            MANAGER: 'manager',
            DESCRIPTION: 'description',
            CATEGORY: 'category.parent.name',
            PART: 'partCode',
            EQUIPMENT_CODE: 'equipmentCode',
            COMPANY_LOCATION: 'location.company.name',
            LOCATION_NAME: 'location.name',
            PART2: 'part',
        },
        requestId: '',
        currentSortType: 'id',
        isDesc: true,
        allColumnList: [],
        pageType: 'CHECKLIST_MAINTENANCE',
        resources: {},
        visible: false,
        dropdownOpening: false,
        isType: 'TOOLING',
        href: '',
        createText: '',
        importText: '',
        isLoading: false,
        currentUser: null,
        currentUserType: null,
        displayCaret: false,
        showAnimation: false,
        visiblePopover: false,
        nameDrawer: '',
        options: {},
    },
    async created() {
        try {
            await Promise.all([
                this.getCurrentUserType(),
                this.getCurrentUser(),
                this.getCompany(),
                this.getLocations(),
                this.getCategories(),
                this.getParts(),
                this.getMolds(),
                this.getMachines(),
            ]);
        } catch (e) {
            console.log(e);
        } finally {
            this.getResources();
        }
        /*
                Common.getCategoryConfigStatus().then(data => {
                    if (data && data.length > 0) {
                        let currentConfig = data.filter(item => item.configCategory === 'PART')[0];
                        if (currentConfig && currentConfig.enabled) {
                            axios.get('/api/config?configCategory=' + currentConfig.configCategory)
                                .then((response) => {
                                    if (response.data && response.data.length > 0) {
                                        console.log('form response data', response.data);
                                        response.data.forEach(field => {
                                            if(this.configDeleteField!=null) this.configDeleteField[field.fieldName]=field.deletedField;
                                        });
                                    }
                                    return new Promise(resolve => resolve(response.data));
                                }).catch((error) => {
                                console.log(error.response);
                            });
                        }
                    }
                });
*/
    },
    watch: {
        visiblePopover(newValue, oldValue) {
            if (newValue == false && oldValue == true) {
                this.displayCaret = false;
            }
        },
    },
    computed: {
        isDataSumissionDefaultShow() {
            return Boolean(this.options?.CLIENT?.dataRegistration?.cols?.dataSubmission?.show);
        },
    },
    methods: {
        goToCreateProject() {
            var child = Common.vue.getChild(this.$children, 'project-form-modal');
            if (child != null) {
                child.showModal(null, true);
            }
        },
        animationOutlineDropdown() {
            this.displayCaret = true;
            if (!this.visiblePopover) {
                this.showAnimation = true;
                setTimeout(() => {
                    this.showAnimation = false;
                }, 700);
            }
        },
        async getCurrentUser() {
            try {
                const me = await Common.getSystem('me');
                this.currentUser = JSON.parse(me);
            } catch (error) {
                console.log(error);
            }
        },
        async getCurrentUserType() {
            try {
                const userType = await Common.getSystem('type');
                this.currentUserType = userType;
                if (userType === 'OEM') this.requestParam.companyType = 'IN_HOUSE';
                if (userType === 'SUPPLIER') this.requestParam.companyType = 'SUPPLIER';
                if (userType === 'TOOL_MAKER') this.requestParam.companyType = 'TOOL_MAKER';
                this.$forceUpdate();
            } catch (error) {
                console.log(error);
            }
        },
        changeDropdown() {
            this.dropdownOpening = false;
        },
        openDropdown() {
            if (this.dropdownOpening === true) {
                this.dropdownOpening = false;
            } else {
                this.dropdownOpening = true;
            }
        },
        showDrawer(type) {
            this.isType = type;
            this.nameDrawer = this.isType !== 'TOOLING' && this.isType !== 'PART' ? this.isType : null;
            this.visible = true;
            let drawerContent = document.getElementsByClassName('ant-drawer-content-wrapper');
            setTimeout(() => {
                drawerContent[0].classList.add('drawer-show');
            }, 50);
        },
        closeDrawer() {
            let drawerContent = document.getElementsByClassName('ant-drawer-content-wrapper');
            drawerContent[0].classList.remove('drawer-show');
            setTimeout(() => {
                this.visible = false;
            }, 1000);
        },
        onChangeHref(href) {
            switch (this.categorySelected) {
                case 'categories':
                    setTimeout(() => {
                        var child = Common.vue.getChild(this.$children, 'category-form-modal');
                        if (child != null) {
                            child.showModal(null, true);
                        }
                    }, 500);
                    break;
                default:
                    setTimeout(() => {
                        window.location.href = href;
                    }, 700);
                    break;
            }
        },
        // onImportClick(href){
        //     const el = document.getElementById('import-button')
        //     el.classList.add('primary-animation')
        //     setTimeout(() => {
        //         el.classList.remove('primary-animation')
        //         this.onChangeHref(href)
        //     }, 500)
        // },
        animationPrimary() {
            $('.animationPrimary').click(function () {
                $(this).addClass('primary-animation');
                $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function (event) {
                    $(this).removeClass('primary-animation');
                });
            });
        },
        animationOutline() {
            $('.animationOutline').click(function () {
                $(this).addClass('outline-animation');
                $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function (event) {
                    $(this).removeClass('outline-animation');
                });
            });
            $('.animationOutlineCta').click(function () {
                $(this).addClass('animation-secondary');
                $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function (event) {
                    $(this).removeClass('animation-secondary');
                });
            });
        },
        categoryLocation: function (categoryId) {
            var location = '';
            for (var i = 0; i < vm.categories.content.length; i++) {
                var parent = vm.categories.content[i];
                for (var j = 0; j < parent.children.length; j++) {
                    var category = parent.children[j];

                    if (category.id == categoryId) {
                        var parentText = parent.enabled == false ? '<span class="disabled">' + parent.name + '</span>' : parent.name;
                        var categoryText =
                            category.enabled == false ? '<span class="disabled">' + category.name + '</span>' : category.name;

                        return parentText + ' > ' + categoryText;
                    }
                }
            }
            return '';
        },
        getSelectedCase() {
            switch (this.categorySelected) {
                case 'companies':
                    this.href = '/admin/companies/new';
                    this.createText = vm.resources['new_company'];
                    this.importText = null;
                    break;
                case 'locations':
                    this.href = '/admin/locations/new';
                    this.createText = vm.resources['new_location'];
                    this.importText = null;
                    break;
                case 'categories':
                    this.href = '/admin/categories/new';
                    this.createText = vm.resources['create_category'];
                    this.importText = null;
                    break;
                case 'parts':
                    this.href = '/admin/parts/new';
                    this.createText = vm.resources['create_part'];
                    this.importText = 'Import Part';
                    break;
                case 'molds':
                    this.href = '/admin/molds/new';
                    this.createText = vm.resources['new_tooling'];
                    this.importText = 'Import Tooling';
                    break;
                case 'machines':
                    this.href = '/admin/machines/new';
                    this.createText = vm.resources['create_machine'];
                    this.importText = null;
                    break;
            }
        },
        changeSelectedCategory(code) {
            this.categorySelected = code;
            this.getSelectedCase();
            this.isDesc = true;
            if (code === 'molds') {
                this.currentSortType = 'lastShotAt';
                this.requestParam = {
                    id: '',
                    query: '',
                    status: 'all',
                    sort: 'lastShotAt,desc',
                    page: 1,
                    operatingStatus: '',
                    equipmentStatus: '',
                    where: 'opand,eq,eq',
                };
            } else if (code === 'parts') {
                this.currentSortType = 'id';
                this.requestParam = {
                    accessType: 'ADMIN_MENU',
                    categoryId: '',
                    query: '',
                    status: 'active',
                    sort: 'id,desc',
                    page: 1,
                    id: '',
                    where: 'opand,eq,eq',
                };
            } else if (code === 'machines') {
                this.currentSortType = 'id';
                this.requestParam = {
                    query: '',
                    status: 'enabled',
                    sort: 'id,desc',
                    page: 1,
                    id: '',
                };
            } else if (code === 'categories') {
                (this.currentSortType = 'sortOrder'), (this.isDesc = false);
                this.requestParam = {
                    query: '',
                    status: '1',
                    sort: 'sortOrder,asc',
                    page: 1,
                    id: '',
                };
            } else {
                this.currentSortType = 'id';
                this.requestParam = {
                    query: '',
                    status: 'active',
                    sort: 'id,desc',
                    page: 1,
                };
            }
            this.paging(1);
        },
        showCompanyDetails: function (company) {
            var child = Common.vue.getChild(this.$children, 'company-details');
            if (child != null) {
                child.showDetails(company);
            }
        },
        showChartPart: function (part) {
            var child = Common.vue.getChild(this.$children, 'chart-part');
            if (child != null) {
                child.showChart(part, 'QUANTITY', 'DAY');
            }
        },
        showMachineDetails: function (company) {
            var child = Common.vue.getChild(this.$children, 'machine-details');
            if (child != null) {
                child.showDetails(company);
            }
        },
        showPartDetails: function (part) {
            var child = Common.vue.getChild(this.$children, 'part-details');
            if (child != null) {
                child.showPartDetails(part);
            }
        },
        showChart: function (mold, tab) {
            console.log(mold, 'molddd');
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
                child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
            }
        },
        backToMoldDetail() {
            console.log('back to molds detail');
            var child = Common.vue.getChild(this.$children, 'chart-mold');
            if (child != null) {
                child.backFromPreview();
            }
        },
        showFilePreviewer(file) {
            var child = Common.vue.getChild(this.$children, 'file-previewer');
            if (child != null) {
                child.showFilePreviewer(file);
            }
        },
        showMoldDetails: function (mold) {
            console.log(mold);
            var child = Common.vue.getChild(this.$children, 'mold-details');
            const tab = 'switch-detail';
            if (child != null) {
                let reasonData = { reason: '', approvedAt: '' };
                if (mold.dataSubmission === 'DISAPPROVED') {
                    axios.get(`/api/molds/data-submission/${mold.id}/disapproval-reason`).then(function (response) {
                        reasonData = response.data;
                        // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
                        reasonData.approvedAt = reasonData.approvedAt
                            ? vm.convertTimestampToDateWithFormat(reasonData.approvedAt, 'MMMM DD YYYY HH:mm:ss')
                            : '-';
                        mold.notificationStatus = mold.dataSubmission;
                        mold.reason = reasonData.reason;
                        mold.approvedAt = reasonData.approvedAt;
                        mold.approvedBy = reasonData.approvedBy;
                        // child.showMoldDetails(mold);
                        this.showChart(mold, tab);
                    });
                } else {
                    this.showChart(mold, tab);
                    // child.showMoldDetails(mold);
                }
            }
        },
        showPartChart: function (part) {
            part.id = part.partId;
            var child = Common.vue.getChild(this.$children, 'chart-part');
            if (child != null) {
                child.showChart(part, 'QUANTITY', 'DAY');
            }
        },
        onHover(code) {
            this.isHover = code;
        },
        async setAllColumnList() {
            vm.allColumnList = [
                { label: vm.resources['machine_id'], field: 'machineCode', default: true, mandatory: true, sortable: true, enabled: true },
                {
                    label: vm.resources['company'],
                    field: 'company',
                    default: true,
                    sortable: true,
                    sortField: 'location.company.name',
                    enabled: true,
                },
                {
                    label: vm.resources['location'],
                    field: 'location',
                    default: true,
                    sortable: true,
                    sortField: 'location.name',
                    enabled: true,
                },
                { label: vm.resources['machine_maker'], field: 'machineMaker', default: true, sortable: true, enabled: true },
            ];
            // this.serverName = await Common.getSystem('server')
            const options = await Common.getSystem('options');
            this.options = JSON.parse(options);
            // const condition = this.serverName === 'dyson'
            const condition = Boolean(this.options?.CLIENT?.dataRegistration?.cols?.dataSubmission?.show);
            if (condition) {
                this.allColumnList.push({
                    label: vm.resources['data_submission'],
                    field: 'dataSubmission',
                    default: true,
                    sortable: true,
                    sortField: 'dataSubmission',
                });
            }
        },
        clearPage(self) {
            self.total = 0;
            self.results = [];
            self.pagination = [];
        },
        async paging(pageNumber) {
            let table = document.getElementById('table');
            if (table) {
                table.classList.add('loading');
                table.classList.add('wave1');
            }
            const apiUrl = '/api/' + this.categorySelected;
            var self = this;
            self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
            let requestParam = JSON.parse(JSON.stringify(self.requestParam));

            var param = Common.param(requestParam);
            await axios
                .get(apiUrl + '?' + param)
                .then(function (response) {
                    self.total = response.data.totalElements;
                    self.results = response.data.content;
                    self.pagination = Common.getPagingData(response.data);
                    document.querySelector('#app .op-list').style.display = 'table-row-group';
                    document.querySelector('.pagination').style.display = 'flex';
                    var noResult = document.querySelector('.no-results');
                    if (noResult != null) {
                        if (vm.results.length == 0) {
                            noResult.className = noResult.className.replace('d-none', '');
                        } else {
                            noResult.className = noResult.className.replace('d-none', '') + ' d-none';
                        }
                    }

                    var pageInfo = self.requestParam.page === 1 ? '' : '?page=' + self.requestParam.page;
                    history.pushState(null, null, Common.$uri.pathname + pageInfo);
                    if (self.results.length > 0) {
                        Common.triggerShowActionbarFeature(self.$children);
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                    self.clearPage(self);
                })
                .finally(() => {
                    setTimeout(() => {
                        if (table) {
                            table.classList.remove('wave1');
                            table.classList.remove('loading');
                        }
                    }, 1000);
                });
        },
        async getResources() {
            try {
                const messages = await Common.getSystem('messages');
                vm.resources = JSON.parse(messages);
                vm.categoryList = [
                    {
                        plural: vm.resources['companies'],
                        singular: vm.resources['company'],
                        code: 'companies',
                        img: 'globe',
                        number: vm.companies?.totalElements || 0,
                        id: 1,
                    },
                    {
                        plural: vm.resources['locations'],
                        singular: vm.resources['location'],
                        code: 'locations',
                        img: 'push-pin',
                        number: vm.locations?.totalElements || 0,
                        id: 2,
                    },
                    {
                        plural: vm.resources['categories'],
                        singular: vm.resources['category'],
                        code: 'categories',
                        img: 'list',
                        number: vm.categories?.totalElements || 0,
                        id: 3,
                    },
                    {
                        plural: vm.resources['parts'],
                        singular: vm.resources['part'],
                        code: 'parts',
                        img: 'pie-chart',
                        number: vm.parts?.totalElements || 0,
                        id: 4,
                    },
                    {
                        plural: vm.resources['toolings'],
                        singular: vm.resources['tooling'],
                        code: 'molds',
                        img: 'four-squares-button-of-view-options',
                        number: vm.molds?.totalElements || 0,
                        id: 5,
                    },
                    {
                        plural: vm.resources['machines'],
                        singular: vm.resources['machine'],
                        code: 'machines',
                        img: 'robotics',
                        number: vm.machines?.totalElements || 0,
                        id: 6,
                    },
                ];
                vm.setAllColumnList();
                vm.getSelectedCase();
            } catch (error) {
                console.log(error);
            }
        },
        tab: function (status) {
            this.total = null;
            if (status === 'DELETED') {
                this.requestParam.status = status;
                this.requestParam.deleted = true;
                this.paging(1);
            } else {
                this.currentSortType = 'id';
                this.requestParam.sort = 'id,desc';
                this.requestParam.status = status;
                delete this.requestParam.deleted;
                this.paging(1);
            }
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
        getCompany() {
            return axios.get('/api/companies?query=&status=active&sort=id%2Cdesc&page=1').then((res) => {
                this.companies = res.data;
            });
        },
        getLocations() {
            return axios.get('/api/locations?query=&status=active&sort=id%2Cdesc&page=1').then((res) => {
                this.locations = res.data;
            });
        },
        getCategories() {
            return axios.get('/api/categories?query=&status=1&sort=sortOrder%2Casc&page=1').then((res) => {
                this.categories = res.data;
            });
        },
        getParts() {
            return axios.get('/api/parts?accessType=ADMIN_MENU&status=active&sort=id%2Cdesc&page=1').then((res) => {
                this.parts = res.data;
            });
        },
        getMolds() {
            return axios.get('/api/molds?status=all&sort=lastShotAt%2Cdesc&page=1').then((res) => {
                this.molds = res.data;
            });
        },
        getMachines() {
            return axios.get('/api/machines?query=&status=enabled&sort=id%2Cdesc&page=1&id=').then((res) => {
                this.machines = res.data;
            });
        },
        getRequestId() {
            return axios.get('/api/data-registration/generate-request_id').then((res) => {
                this.requestId = res.data;
            });
        },
    },

    mounted() {
        this.$nextTick(async function () {
            // 모든 화면이 렌더링된 후 실행합니다.
            const self = this;
            vm.animationOutline();
            vm.animationPrimary();
            const tab = Common.getParameter('tab');
            if (tab) {
                this.categorySelected = tab;
            }

            try {
                // const server = await Common.getSystem('server')
                const options = await Common.getSystem('options');
                const type = await Common.getSystem('type');
                const resCodes = await axios.get('/api/codes');
                // self.serverName = server
                self.options = JSON.parse(options);
                self.userType = type;
                self.codes = resCodes.data;
                self.paging(1);
            } catch (error) {
                console.log(error);
            }
        });
    },
});
