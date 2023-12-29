axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'New ticket' + ' | eMoldino';
    setTimeout(() => {
        $('div').removeClass('wave_sidebar');
        $('div').removeClass('wave_support_form');
        $('div').removeClass('profile_wave4');
        $('div').removeClass('profile_wave');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        document.getElementById('remove_profile').remove();
        $('div').removeClass('hide_account');
    }, 500);
};

var vm = new Vue({
    el: '#app',
    components: {
        'user-filter': httpVueLoader('/components/support/user-filter.vue'),
        'object-filter': httpVueLoader('/components/support/object-filter.vue'),
        'topic-filter': httpVueLoader('/components/support/topic-filter.vue'),
    },
    data: {
        selectedObject: null,
        topic: {
            value: null,
            subject: null,
            message: null,
            recipient: {},
            systemNoteFunction: null,
            recipientId: null,
        },
        files: [],
        listPagePath: '/support/customer-support',
        systemNoteFunctions: [],
    },
    computed: {},
    methods: {
        setTopicValue(value) {
            let topicValue = value != null ? value : 'Item';
            return topicValue;
        },
        submit: function () {
            console.log('this.topic.value', this.topic.value);
            if (!this.topic.recipient.id) {
                Common.alert('Please select recipient');
                return;
            }
            if (!this.topic.systemNoteFunction) {
                Common.alert('Please select Topic');
                return;
            }
            if (!this.topic.value) {
                Common.alert('Please select Item');
                return;
            }
            axios
                .post(`/api/topics/create-topic`, this.requestFormData(), vm.multipartHeader())
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

        selectedFile: function (e) {
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
            this.topic.recipientId = this.topic.recipient.id;
            if (this.selectedObject) {
                this.topic.objectType = this.selectedObject.type.value;
                this.topic.objectId = this.selectedObject.id;
                let item = this.selectedObject.type.items.filter((item) => item.id === this.selectedObject.id)[0];
                if (item) {
                    this.topic.objectCode = item[this.selectedObject.type.labelField];
                }
            }

            requestFormData.append('payload', JSON.stringify(this.topic));
            console.log('requestFormData', requestFormData);
            return requestFormData;
        },
        showUserFilterPopup() {
            let child = Common.vue.getChild(this.$children, 'user-filter');
            if (child != null) {
                child.showUserFilterPopup();
            }
        },
        hideUserFilterPopup() {
            let child = Common.vue.getChild(this.$children, 'user-filter');
            if (child != null) {
                child.hideUserFilterPopup();
            }
        },
    },
});
