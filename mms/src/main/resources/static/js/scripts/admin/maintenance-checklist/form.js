window.onload = function () {
    document.title = 'Checklist' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('div').removeClass('profile_wave1');
        $('div').removeClass('profile_wave2');
        $('div').removeClass('profile_wave3');
        $('div').removeClass('profile_wave4');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        document.getElementById('remove_profile').remove();
        $('div').removeClass('hide_account');
    }, 500);
};
var Page = Common.getPage('checklist');
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
var vm = new Vue({
    el: '#app',
    components: {
        'company-search': httpVueLoader('/components/company-search.vue'),
    },
    data: {
        resources: {},
        reason: [],
        checklists: [],
        isFocusChecklistInput: false,
        reasonText: '',
        form: {
            id: '',
            company: {},
            companyId: '',
            checklistCode: '',
            enabled: true,
            checklistItemValue: [],
            checklistType: 'MAINTENANCE',
        },
        tooling: {},
        files: [],
        maxHour: 23,
        maxMinute: 59,
        hourFormat: 'HH',
        minuteFormat: 'mm',
        dateFormat: 'YYYY-MM-DD',
        title: '',
        focusChecklistIndex: null,
    },

    methods: {
        isFocusClass(index) {
            return this.isFocusChecklistInput && index === this.checklists.length - 1;
        },
        addChecklist: function () {
            this.checklists.push({
                content: '',
                tmpContent: '',
                isConfirmed: false,
            });
            this.focusChecklistIndex = this.checklists.length - 1;
            this.isFocusChecklistInput = true;
            this.$nextTick(() => {
                this.$refs['checklist-' + (this.checklists.length - 1)][0].focus();
            });
        },
        focusOutChecklist: function (addedChecklist) {
            if (this.isNewChecklist(addedChecklist)) {
                return;
            }
            if (this.isValidAllAddedChecklist()) {
                this.saveChecklist();
            }
        },
        isNewChecklist(addedChecklist) {
            return !addedChecklist.isConfirmed;
        },
        inputChecklist: function (e, addedChecklist) {
            if (this.isNewChecklist(addedChecklist)) {
                return;
            }
            if (e.code === 'Enter') {
                if (this.isValidAllAddedChecklist()) {
                    this.saveChecklist();
                    $('.reason-edit-input').blur();
                }
            }
        },
        deleteChecklist(index) {
            this.checklists.splice(index, 1);
            this.isFocusChecklistInput = false;
        },
        focusChecklist(index) {
            if (this.focusChecklistIndex !== null) {
                let tmpContent = this.checklists[this.focusChecklistIndex].tmpContent;
                if (tmpContent && tmpContent.trim()) {
                    this.checklists[this.focusChecklistIndex].content = tmpContent.trim();
                } else {
                    this.checklists[this.focusChecklistIndex].tmpContent = this.checklists[this.focusChecklistIndex].content;
                }
            }
            this.focusChecklistIndex = index;
            this.$nextTick(() => {
                this.$refs['checklist-' + index][0].focus();
            });
        },
        confirmChecklist(addedChecklist) {
            if (!addedChecklist.tmpContent || !addedChecklist.tmpContent.trim()) {
                addedChecklist.tmpContent = addedChecklist.content;
                return;
            }
            addedChecklist.content = addedChecklist.tmpContent.trim();
            addedChecklist.isConfirmed = true;
            this.isFocusChecklistInput = false;
        },
        confirmEditChecklist(addedChecklist, index) {
            if (!addedChecklist.tmpContent || !addedChecklist.tmpContent.trim()) {
                if (!addedChecklist.isConfirmed) {
                    this.showValidChecklist(index);
                    // this.cancelEditChecklist(addedChecklist, index);
                    return;
                    addedChecklist.content = 'Checklist ' + (index + 1);
                    addedChecklist.isConfirmed = true;
                    this.isFocusChecklistInput = false;
                    this.focusChecklistIndex = null;
                }
                addedChecklist.tmpContent = addedChecklist.content;
                return;
            }
            addedChecklist.content = addedChecklist.tmpContent.trim();
            addedChecklist.isConfirmed = true;
            this.isFocusChecklistInput = false;
            this.focusChecklistIndex = null;
        },
        showValidChecklist(index) {
            this.$refs['checklist-' + index][0].required = true;
            this.$refs['checklist-' + index][0].setCustomValidity('Please input the checklist name.');
            this.$refs['btn-checklist-' + index][0].click();
        },
        cancelEditChecklist(addedChecklist, index) {
            if (!addedChecklist.isConfirmed) {
                this.deleteChecklist(index);
                return;
            }
            this.focusChecklistIndex = null;
            if (!addedChecklist.tmpContent || !addedChecklist.tmpContent.trim()) {
                addedChecklist.tmpContent = addedChecklist.content;
                return;
            }
            addedChecklist.tmpContent = addedChecklist.content;
        },
        saveChecklist: function () {
            this.isFocusChecklistInput = false;
            this.checklists.forEach((reason) => {
                if (!reason.isConfirmed) {
                    reason.isConfirmed = true;
                }
            });
        },
        isValidAllAddedChecklist() {
            for (let i = 0; i < this.checklists.length; i++) {
                if (!this.checklists[i].content) {
                    return false;
                }
            }
            return true;
        },
        submitChecklist() {
            if (!this.form.companyId) {
                Common.alert('Please select Company');
                return;
            }
            if (!this.form.checklistCode) {
                Common.alert('Please select Checklist ID');
                return;
            }
            this.form.checklistItemValue = this.checklists
                .filter((checklist) => checklist.isConfirmed)
                .map((checklist) => checklist.content);
            if (!this.form.checklistItemValue.length) {
                Common.alert('Please create at least one checklist item.');
                return;
            }
            if (!Page.IS_NEW) {
                this.update();
                return;
            }
            axios
                .post(`/api/checklist/create`, this.form)
                .then(function (response) {
                    if (response.data.success) {
                        Common.alertCallback = function () {
                            // location.href = Page.LIST_PAGE;
                            vm.callbackMessageForm();
                        };
                        Common.alert('success');
                    } else {
                        Common.alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        update: function () {
            vm.children = null;
            axios
                .put(Page.API_BASE + '/' + vm.form.id, vm.form)
                .then(function (response) {
                    console.log(response.data);

                    if (response.data.success) {
                        Common.alertCallback = function () {
                            // location.href = Page.LIST_PAGE;
                            vm.callbackMessageForm();
                        };
                        Common.alert('success');
                    } else {
                        Common.alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        callbackMessageForm: function () {
            location.href = document.referrer.includes('checklist-center')
                ? document.referrer + '?systemNoteFunction=CHECKLIST_MAINTENANCE'
                : '/admin/maintenance-checklist';
            // location.href = "/admin/maintenance-checklist";
            //   history.back();
        },
        callbackCompany: function (company) {
            vm.form.companyId = company.id;
            vm.form.company = company;
        },
        async getResources() {
            try {
                const stringifiedMessages = await Common.getSystem('messages');
                vm.resources = JSON.parse(stringifiedMessages);
            } catch (error) {
                Common.alert(error.response.data);
            }
        },
    },
    mounted() {
        this.$nextTick(function () {
            vm.getResources();
            Page.ID = Common.$uri.pathname
                .replace('/admin/maintenance-checklist/', '')
                .replace('/admin/checklist-center/maintenance/', '')
                .replace('/edit', '');
            Page.IS_NEW = Page.ID === 'new';
            this.title = Page.IS_NEW ? 'New' : 'Edit';
            Page.IS_EDIT = !Page.IS_NEW;
            if (Page.IS_EDIT) {
                vm.form.id = Page.ID;
                // 데이터 조회
                axios.get(Page.API_BASE + '/' + vm.form.id).then(function (response) {
                    vm.form = response.data;
                    console.log('update', response.data);
                    if (response.data && response.data.checklistItems) {
                        vm.checklists = response.data.checklistItems.map((checklist) => {
                            return {
                                content: checklist,
                                isConfirmed: true,
                                tmpContent: checklist,
                            };
                        });
                        // vm.form.checklistItemValue=response.data.checklistItems;
                    }
                });
            }
        });
    },
});
