$(function () {
    $('#repairDateTime').datetimepicker({
        ignoreReadonly: true,
        focusOnShow: true,
        format: 'YYYY-MM-DD HH:mm:ss',
    });
    $('#repairDateTime').on('hide.datetimepicker', function (e) {
        vm.moldCorrective.repairDateTime = $(this).val();
    });
});

var Page = Common.getPage('corrective');
var isDetail = window.location.href.split('?').length > 1 ? window.location.href.split('?')[1] : null;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

var vm = new Vue({
    el: '#app',
    components: {
        'part-search': httpVueLoader('/components/part-search.vue'),
        'location-search': httpVueLoader('/components/location-search.vue'),
    },

    data: {
        resources: {},
        moldCorrective: {
            id: '',
            moldId: '',
            failureTimeText: '',
            repairDateTime: '',
            cost: '',
            currencyType: 'USD',
            memo: '',
            mold: {
                equipmentCode: '',
            },
        },
        files: [],
        uploadedFiles: [],

        codes: {},
    },
    methods: {
        submit: function () {
            this.update();
        },

        update: function () {
            vm.children = null;

            this.moldCorrective.repairDateTime = $('#repairDateTime').val();

            if (this.moldCorrective.repairDateTime == '') {
                alert('Please enter the repair time.');
                return;
            }

            //axios.put("/api/molds/corrective/" + Page.ID , vm.moldCorrective).then(function(response) {
            axios
                .put('/api/molds/corrective/' + Page.ID, this.formData(), this.multipartHeader())
                .then(function (response) {
                    console.log(response.data);
                    if (response.data.success) {
                        Common.alert('success');
                    } else {
                        Common.alert(response.data.message);
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },

        formData: function () {
            var formData = new FormData();
            for (var i = 0; i < this.files.length; i++) {
                var file = this.files[i];
                formData.append('files', file);
            }
            // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
            formData.append('payload', JSON.stringify(vm.moldCorrective));
            return formData;
        },

        cancel: function () {
            location.href = Page.LIST_PAGE;
        },

        selectedFile: function (e) {
            if (isDetail) {
                return;
            }
            var files = e.target.files;

            if (files) {
                var selectedFiles = Array.from(files);

                for (var i = 0; i < selectedFiles.length; i++) {
                    vm.files.push(selectedFiles[i]);
                }
            }
        },
        deleteFile: function (f, index) {
            var newFiles = [];
            for (var i = 0; i < vm.files.length; i++) {
                var file = vm.files[i];
                if (i != index) {
                    console.log('splice: ' + i + ' : ' + (i + 1));
                    //vm.files.splice(i, 1);
                    newFiles.push(file);
                }
            }

            vm.files = newFiles;
        },
        deleteFileStorage: function (file) {
            if (confirm('Are you sure you want to delete this file?'))
                axios.delete('/api/file-storage/' + file.id).then(function (response) {
                    vm.uploadedFiles = response.data;
                });
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
    computed: {
        readonly: function () {
            return Page.IS_EDIT;
        },
        New: function () {
            return Page.IS_NEW ? 'New' : 'Edit';
        },
        isDetail: function () {
            return isDetail !== null;
        },
    },
    mounted() {
        this.$nextTick(function () {
            // 데이터 조회
            var self = this;
            axios.get('/api/codes').then(function (response) {
                self.codes = response.data;
            });

            if (Page.IS_EDIT) {
                axios.get('/api/molds/corrective/' + Page.ID).then(function (response) {
                    var moldCorrective = response.data;

                    self.moldCorrective = moldCorrective;
                    self.moldCorrective.failureTimeText = moldCorrective.failureDateTime;
                    self.moldCorrective.failureDateTime = '';
                    self.moldCorrective.moldId = moldCorrective.mold.id;

                    var param = {
                        storageType: 'MOLD_CORRECTIVE',
                        refId: self.moldCorrective.id,
                    };
                    axios.get('/api/file-storage?' + Common.param(param)).then(function (response) {
                        vm.uploadedFiles = response.data;
                    });
                });
            }
            vm.getResources();
        });
    },
});
