axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
    document.title = 'Support Center' + ' | eMoldino';
    $('div').removeClass('wave');
    $('div').removeClass('wave_sidebar');
    $('div').removeClass('wave1');
    $('li').removeClass('wave_header');
    $('img').removeClass('wave_img');
    document.getElementById('remove1').remove();
    document.getElementById('remove2').remove();
};

var Page = Common.getPage('topics');

var vm = new Vue({
    el: '#app',
    components: {
        'tabs-table': httpVueLoader('/components/tabs-table.vue'),
    },
    data: {
        resources: {},
        tabParam: 'topicStatus',
        searchPlaceholder: 'Search by ticket ID, subject, creator or company',
        requestParam: {
            topicStatus: '',
            sort: 'id,desc',
            page: 1,
        },
        tabs: [],
        headers: [],
        sortType: {},
        currentSortType: 'partCode',
        isDesc: true,
    },
    methods: {
        getDetailLink(props) {
            return '/support/customer-support/detail/' + props.item.id;
        },
        getRelatedTime($timestamp) {
            return moment($timestamp * 1000).fromNow();
        },
        countReplyLabel(props) {
            const numberOfReply = props.item.numberOfReply;
            if (numberOfReply === 0) return this.resources['no_reply'];
            if (numberOfReply === 1) {
                return '1 reply';
            }

            return numberOfReply + ' replies';
        },
        getHeaders() {
            return {
                status: this.resources['status'],
                topicId: this.resources['topic_id'],
                subject: this.resources['subject'],
                creator: this.resources['creator'],
                company: this.resources['company'],
                data: this.resources['date'],
                activity: this.resources['activity'],
            };
        },
        getTabs() {
            return {
                '': this.resources['all'],
                NEW: this.resources['new'],
                IN_PROGRESS: this.resources['in_progress'],
                RESOLVED: this.resources['resolved'],
            };
        },
        lastReply(props) {
            const numberOfReply = props.item.numberOfReply;
            if (numberOfReply === 0) {
                return null;
            }
            return props.item;
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
            axios.get('/api/codes/MAINTENANCE_STATUS').then(function (response) {
                // self.statusCodes = response.data;
                // var uri = URI.parse(location.href);
                // if (uri.fragment === 'cm' || uri.fragment === 'cm-history') {
                //     self.tab(uri.fragment);
                // } else {
                //     self.paging(1);
                // }
            });
            vm.getResources();
        });
    },
});
