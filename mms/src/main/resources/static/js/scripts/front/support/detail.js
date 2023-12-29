axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

var Page = Common.getPage('topics');

window.onload = function () {
    document.title = 'Support Center' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        $('div').removeClass('wave_support_form');
        $('div').removeClass('wave');
    }, 500);
};

var vm = new Vue({
    el: '#app',
    components: {
        'tabs-table': httpVueLoader('/components/tabs-table.vue'),
    },
    data: {
        resources: {},
        topic: {},
        topicId: null,
        files: [],
        statusMapping: {
            NEW: 'New',
            IN_PROGRESS: 'In Progress',
            RESOLVED: 'Resolved',
        },
        reply: {
            topicId: null,
            userId: null,
            message: null,
        },
        correspondences: [],
        listPagePath: '/support/customer-support',
        isResolved: false,
        roleOptions: {
            NORMAL: 'NORMAL',
            HOST: 'HOST',
            OWNER: 'OWNER',
            HOST_OWNER: 'HOST_OWNER',
        },
        userRole: null,
    },
    methods: {
        canResolve() {
            return [this.roleOptions.HOST, this.roleOptions.HOST_OWNER].includes(this.userRole);
        },
        canReply() {
            return [this.roleOptions.OWNER, this.roleOptions.HOST, this.roleOptions.HOST_OWNER].includes(this.userRole);
        },
        submit() {
            axios
                .post(`${Page.API_BASE}/create-correspondence`, this.requestFormData(), vm.multipartHeader())
                .then((response) => {
                    if (response.data && response.data.success) {
                        location.href = this.listPagePath;
                    }
                    console.log('success', response);
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        getTopicName(name) {
            return name
                .split(' ')
                .map((item) => item.charAt(0).toUpperCase() + item.slice(1).toLowerCase())
                .join(' ');
        },
        changeResolveStatus() {
            if (this.isResolved) {
                this.resolveTopic();
            } else {
                this.unResolveTopic();
            }
        },
        resolveTopic() {
            axios
                .post(`${Page.API_BASE}/${this.topicId}/resolve`)
                .then((response) => {
                    if (response.data) {
                        location.href = this.listPagePath;
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },

        unResolveTopic() {
            axios
                .post(`${Page.API_BASE}/${this.topicId}/unresolve`)
                .then((response) => {
                    if (response.data) {
                        location.href = this.listPagePath;
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },

        selectedFile: function (e) {
            this.files = [];
            var files = e.target.files;

            if (files) {
                var selectedFiles = Array.from(files);

                for (var i = 0; i < selectedFiles.length; i++) {
                    this.files.push(selectedFiles[i]);
                }
            }
        },

        deleteFile: function (f, index) {
            var newFiles = [];
            for (var i = 0; i < this.files.length; i++) {
                var file = this.files[i];
                if (i !== index) {
                    newFiles.push(file);
                }
            }

            this.files = newFiles;
        },
        requestFormData: function () {
            var requestFormData = new FormData();
            for (var i = 0; i < this.files.length; i++) {
                var file = this.files[i];
                requestFormData.append('files', file);
            }
            requestFormData.append('payload', JSON.stringify(this.reply));
            return requestFormData;
        },
        getTopicInfo() {
            axios
                .get(`${Page.API_BASE}/${this.topicId}`)
                .then((response) => {
                    if (response.data) {
                        this.topic = response.data;
                        var res = this.topic.systemNoteFunction.replace(/_/gi, ' ');
                        var res2 = this.topic.objectType.replace(/_/gi, ' ');
                        this.topic.systemNoteFunction = res.replace(/\w\S*/g, function (txt) {
                            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
                        });
                        this.topic.objectType = res2.replace(/\w\S*/g, function (txt) {
                            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
                        });
                        console.log('this is the objectcode', response.data.objectCode);
                        console.log('this is the objectcode', response.data);
                        this.isResolved = this.topic.status === 'RESOLVED';
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        getCorrespondence() {
            axios
                .get(`${Page.API_BASE}/${this.topicId}/correspondences`)
                .then((response) => {
                    if (response.data && response.data.content) {
                        this.correspondences = response.data.content;
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
                });
        },
        initReplyData() {
            this.reply.topicId = this.topicId;
            this.reply.userId = $('.user-id').attr('id');
        },
        getUserRole() {
            axios
                .get(`${Page.API_BASE}/${this.topicId}/role-user-topic`)
                .then((response) => {
                    if (response.data) {
                        this.userRole = response.data;
                    }
                })
                .catch(function (error) {
                    console.log(error.response);
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
    mounted() {
        this.$nextTick(function () {
            vm.getResources();
        });
        this.topicId = Common.$uri.pathname.match(/detail\/(.*)/i)[1];
        this.getTopicInfo();
        this.getCorrespondence();
        this.initReplyData();
        this.getUserRole();
    },
});
