axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
axios.defaults.headers.post['Content-Type'] = 'application/json';
axios.defaults.headers.put['Content-Type'] = 'application/json';
axios.defaults.headers.delete['Content-Type'] = 'application/json';
let removeWave = function (time) {
    setTimeout(() => {
        $('div').removeClass('wave');
        $('div').removeClass('wave1');
        $('div').removeClass('wave_sidebar');
        $('li').removeClass('wave_header');
        $('img').removeClass('wave_img');
        if (document.getElementById('removee')) document.getElementById('removee').remove();
    }, time);
};
window.onload = function () {
    document.title = 'Alert Center' + ' | eMoldino';
    removeWave(500);
    removeWave(2000);
};

// alert-center subcomponents
Vue.component('relocation', httpVueLoader('/components/pages/common/alt-ctr/relocation.vue'));
Vue.component('disconnection', httpVueLoader('/components/pages/common/alt-ctr/disconnection.vue'));
Vue.component('cycle-time', httpVueLoader('/components/pages/common/alt-ctr/cycle-time.vue'));
Vue.component('maintenance', httpVueLoader('/components/pages/common/alt-ctr/maintenance.vue'));
Vue.component('uptime', httpVueLoader('/components/pages/common/alt-ctr/uptime.vue'));
Vue.component('reset', httpVueLoader('/components/pages/common/alt-ctr/reset.vue'));
Vue.component('data-approval', httpVueLoader('/components/pages/common/alt-ctr/data-approval.vue'));
Vue.component('refurbishment', httpVueLoader('/components/pages/common/alt-ctr/refurbishment.vue'));
Vue.component('detachment', httpVueLoader('/components/pages/common/alt-ctr/detachment.vue'));
Vue.component('downtime', httpVueLoader('/components/pages/common/alt-ctr/downtime.vue'));
Vue.component('machine-downtime', httpVueLoader('/components/pages/common/alt-ctr/machine-downtime.vue'));

// etc (alert-center 및 alert-center subcomponent 에 전역적으로 필요한 컨테이너)
Vue.component('company-details', httpVueLoader('/components/company-details.vue'));
Vue.component('notify-alert', httpVueLoader('/components/notify-alert.vue'));
Vue.component('chart-mold', httpVueLoader('/components/chart-mold/chart-mold-modal.vue'));
Vue.component('mold-details', httpVueLoader('/components/mold-details.vue'));
Vue.component('message-form', httpVueLoader('/components/message-form.vue'));
Vue.component('location-history', httpVueLoader('/components/location-history.vue'));
Vue.component('relocation-confirm-history', httpVueLoader('/components/relocation-confirm-history.vue'));
Vue.component('system-note', httpVueLoader('/components/system-note.vue'));
Vue.component('customization-modal', httpVueLoader('/components/customization-modal.vue'));
Vue.component('action-bar-feature', httpVueLoader('/components/new-feature/new-feature.vue'));
Vue.component('chart-part', httpVueLoader('/components/chart-part.vue'));
Vue.component('message-details', httpVueLoader('/components/message-details.vue'));
Vue.component('message-confirm', httpVueLoader('/components/alert-center/data-approval/message-confirm.vue'));

var vm = new Vue({
    el: '#app',
    components: {
        'alert-center': httpVueLoader('/components/pages/common/alt-ctr'),
    },
    mounted() {
        this.$nextTick(function () {
            removeWave(500);
        });
    },
});
