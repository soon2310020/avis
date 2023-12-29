var Page = Common.getPage('roles');
var tab = location.href.split('#').length > 1 ? window.location.href.split('#')[1] : null;

window.onload = function () {
    document.title = 'Access Group' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('div').removeClass('profile_wave1');
        $('div').removeClass('profile_wave');
        $('div').removeClass('wave_save');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        document.getElementById('remove_profile').remove();
        $('div').removeClass('hide_account');
    }, 500);
};

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

var vm = new Vue({
    el: '#app',
    data: {
        showRoleDropdown: false,
        select_type: 'Select Type',
        role: {
            name: '',
            description: '',
            authority: '',
            roleType: '',
            menuIds: [],
        },
        graphTypes: [],
        isAccFeature: false,
        graphs: [
            {
                id: 'QUICK_STATS',
                name: 'Quick Fact',
                checked: false,
            },
            {
                id: 'DISTRIBUTION',
                name: 'Distribution of Tooling',
                checked: false,
            },
            {
                id: 'HIERARCHY',
                name: 'Project Hierarchy',
                checked: false,
            },
            // {
            //     id: 'CAPACITY',
            //     name: 'Capacity Utilization',
            //     checked: false
            // },
            {
                id: 'CYCLE_TIME_STATUS',
                name: 'Cycle Time Status',
                checked: false,
            },
            {
                id: 'TOOLING',
                name: 'Tooling Status',
                checked: false,
            },
            {
                id: 'MAINTENANCE_STATUS',
                name: 'Preventive Maintenance',
                checked: false,
            },
            {
                id: 'UPTIME_STATUS',
                name: 'Uptime Status',
                checked: false,
            },
            // {
            //     id: 'COMMON_PARTS',
            //     name: 'Common Parts / Individual Parts',
            //     checked: false
            // },
            {
                id: 'DOWNTIME',
                name: 'Downtime',
                checked: false,
            },
            /*
                                        {
                                            id: 'OEE',
                                            name: 'Overall Equipment Effectiveness',
                                            checked: false
                                        },
                    */
            // {
            //     id: 'TOOLING_COST',
            //     name: 'Total Tooling Cost',
            //     checked: false
            // },
            // {
            //     id: 'RECENT_ACTIVITIES',
            //     name: 'Recent Activities',
            //     checked: false
            // },
            // {
            //     id: 'DEVICE_INSTALLATION',
            //     name: 'Device Installation',
            //     checked: false
            // },
            {
                id: 'PRODUCTION_RATE',
                name: 'Production Quantity',
                checked: false,
            },
            {
                id: 'UTILIZATION_RATE',
                name: 'Utilization Rate',
                checked: false,
            },
            {
                id: 'IMPLEMENTATION_STATUS',
                name: 'Implementation Status',
                checked: false,
            },
            // {
            //     id: 'MAINTENANCE_EFFECTIVENESS',
            //     name: 'Maintenance Effectiveness',
            //     checked: false
            // },
            // {
            //     id: 'SUPPORTING_DOCUMENT',
            //     name: 'Supporting Document',
            //     checked: false
            // }
        ],
        authCode: '',
        isEdit: Page.IS_EDIT,
        isUpdate: false,
        dateType: 'Individual',
        sectionDialog: false,
        visibleSuccess: false,
        isCheckedAllGraphs: true,
    },
    watch: {
        // isCheckedAllGraphs(newVal) {
        //     const newGraphs = this.graphs.map(item => ({...item, checked: newVal}))
        //     this.graphs = newGraphs
        //     console.log('isCheckedAllGraphs 2', newVal, newGraphs, this.graphs)
        // },
    },
    methods: {
        chooseIndividual(item) {
            this.dateType = 'Individual';
            delete this.role.dashboardGroupType;
            $('#checkbox-all').prop('checked', false);
            const newGraphsSorted = [...this.graphsSorted].map((o) => {
                if (o.id === item.id) return { ...o, checked: !o.checked };
                else return o;
            });
            const isAllChecked = newGraphsSorted.filter((item) => !item.checked)?.length <= 0;
            this.graphs = newGraphsSorted;
            this.isCheckedAllGraphs = isAllChecked;
        },
        onChange(isChecked, data) {
            $('#checkbox-all').prop('checked', false);
            if (isChecked) {
                if (data === null) {
                    this.graphs = this.graphs.map((value) => {
                        value.checked = true;
                        return value;
                    });
                    delete this.role.dashboardGroupType;
                } else {
                    this.graphs = this.graphs.map((value) => {
                        value.checked = false;
                        return value;
                    });
                    this.role.dashboardGroupType = data;
                }
            }
        },
        changeSectionDialog() {
            this.sectionDialog = !this.sectionDialog;
        },
        onCheckAllChart: function (event) {
            if (event.target.checked) {
                this.graphs = this.graphs.map((value) => {
                    value.checked = true;
                    return value;
                });
            } else {
                this.graphs = this.graphs.map((value) => {
                    value.checked = false;
                    return value;
                });
            }
        },
        checkAll: function () {
            var menuIds = [];

            $('input[name=menuId]:checked').each(function (index) {
                menuIds[index] = $(this).val();
            });

            vm.role.menuIds = menuIds;
        },
        onClickIndividual: function (e) {
            const listInputValue = [];
            const parentId = e.target.getAttribute('parent-id');
            const childrenNodes = document.querySelectorAll(`input[parent-id="${parentId}"]`);
            let isCheckAll = true;
            childrenNodes.forEach((node) => {
                if (!node.checked) {
                    isCheckAll = false;
                }
                listInputValue.push(node.value);
            });
            $('#' + parentId).prop('checked', isCheckAll);
            console.log('listInputValue', listInputValue, isCheckAll, this.role.menuIds, e.target.value);
        },
        submit: function () {
            if (Page.IS_NEW) {
                this.create();
            } else {
                this.update();
            }
        },

        cancel: function () {
            var url = '/admin/roles';
            if (vm.role.roleType == 'ROLE_MENU') {
                url = url + '#menu';
            }
            location.href = url;
        },
        create: function () {
            if (vm.role.roleType != 'ROLE_MENU') {
                vm.role.menuIds = [];
            }
            vm.authCode = vm.authCode ? vm.authCode.trim() : vm.authCode;
            vm.role.authority = vm.role.roleType + '_' + vm.authCode;
            let graphTypes = this.graphs.filter((item) => item.checked).map((value) => value.id);
            vm.role.graphTypes = graphTypes;
            axios
                .post(Page.API_POST, vm.role)
                .then(function (response) {
                    console.log(response.data);

                    if (response.data.success) {
                        Common.alertCallback = function () {
                            location.href = '/admin/roles';
                        };
                        Common.alert('success');
                    } else {
                        Common.alert(response.data.message);
                    }
                    // vm.total = response.data.totalElements;
                    // vm.results = response.data.content;
                    // vm.pagination = Common.getPagingData(response.data);
                })
                .catch(function (error) {
                    Common.alert('Fail!');
                    console.log(error.response);
                });
        },
        update: function () {
            if (vm.role.roleType != 'ROLE_MENU') {
                vm.role.menuIds = [];
            }
            vm.authCode = vm.authCode ? vm.authCode.trim() : vm.authCode;
            vm.role.authority = vm.role.roleType + '_' + vm.authCode;
            let graphTypes = this.graphs.filter((item) => item.checked).map((value) => value.id);
            vm.role.graphTypes = graphTypes;
            axios
                .put(Page.API_PUT, vm.role)
                .then(function (response) {
                    console.log(response.data);

                    if (response.data.success) {
                        Common.alertCallback = function () {
                            //history.pushState(null, null, `/admin/roles?tab=${tab}`);
                            location.href = `/admin/roles#${tab}`;
                        };
                        Common.alert('success');
                    } else {
                        Common.alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    Common.alert('Fail!');
                    console.log(error.response);
                });
        },
    },
    computed: {
        New: function () {
            return Page.IS_NEW ? 'New' : 'Edit';
        },
        graphLength() {
            const graphLength = this.graphs.filter((item) => item.checked).length;
            return graphLength <= 0 && this.dateType === 'Individual';
        },
        graphsSorted() {
            return this.graphs.sort((a, b) => (a.name !== b.name ? (a.name < b.name ? -1 : 1) : 0));
        },
    },
    created() {
        var self = this;
        axios.get('/api/codes').then(function (response) {
            self.codes = response.data;
            if (self.codes || self.codes.GraphType) {
                self.codes.GraphType.forEach((item) => {
                    if (item.enabled == false) return;
                    const findIndex = self.graphs.findIndex((g) => g.id == item.code && item.enabled);
                    if (findIndex == -1) {
                        let itemNew = {
                            id: item.code,
                            name: item.title,
                            checked: true,
                        };
                        self.graphs.push(itemNew);
                        console.log('graphs itemNew', itemNew);
                    }
                });
            }
        });
    },
    mounted() {
        console.log(Page.API_GET, 'Page.API_GET');
        console.log(Page.API_PUT, 'Page.API_PUT1');
        console.log(Page.API_POST, 'Page.API_PUT');
        let url_string = window.location.href;
        this.isAccFeature = url_string.includes('true');
        this.$nextTick(function () {
            vm.isUpdate = false;
            // vm.graphs = vm.graphs.sort((a, b) => a.name !== b.name ? a.name < b.name ? -1 : 1 : 0)
            if (Page.IS_EDIT) {
                console.log('come 1111');
                vm.isUpdate = true;

                // 데이터 조회
                axios.get(Page.API_GET).then(function (response) {
                    vm.role = response.data.role;
                    vm.graphTypes = response.data.graphTypes;
                    // vm.dateType = response.data.role.dashboardGroupType
                    if (response.data.role.dashboardGroupType === null) {
                        // vm.graphs = vm.graphs.map(value => {
                        //     value.checked = true;
                        //     return value;
                        // })
                        vm.dateType = 'Individual';
                        delete vm.role.dashboardGroupType;
                    } else {
                        vm.dateType = response.data.role.dashboardGroupType;
                        vm.role.dashboardGroupType = response.data.role.dashboardGroupType;
                    }
                    vm.graphs = vm.graphs.map((value) => {
                        const findIndex = response.data.graphTypes.findIndex((chartId) => chartId === value.id);
                        if (findIndex !== -1) {
                            value.checked = true;
                        } else {
                            value.checked = false;
                        }
                        return value;
                    });
                    // vm.graphs = vm.graphs.sort((a, b) => a.name !== b.name ? a.name < b.name ? -1 : 1 : 0)
                    // console.log(vm.graphs, 'gggggggggg')
                    var pattern = new RegExp(vm.role.roleType + '_', 'gi');
                    if (vm.role.authority != null) {
                        vm.authCode = vm.role.authority.replace(pattern, '');
                    }
                });
            } else {
                this.graphs.map((value) => {
                    value.checked = true;
                    return value;
                });
            }
        });
    },
});

$(function () {
    $('.check-all').on('click', function () {
        var isChecked = $(this).prop('checked');
        var $children = $(this).closest('div').parent().find('input[type=checkbox]');
        $children.prop('checked', isChecked);
        vm.dateType = 'Individual';
        vm.checkAll();
        const event = {
            target: {
                checked: isChecked,
            },
        };
        vm.onCheckAllChart(event);
    });
    $('.check-all-children').on('click', function () {
        var isChecked = $(this).prop('checked');
        var $children = $(this).closest('li').find('input[type=checkbox]');
        $children.prop('checked', isChecked);
        vm.checkAll();
    });
    $('#checkbox-dashboard').on('click', function () {
        var isChecked = $(this).prop('checked');
        const newGraphs = vm.graphs.map((item) => ({ ...item, checked: isChecked }));
        vm.graphs = newGraphs;
    });
});
