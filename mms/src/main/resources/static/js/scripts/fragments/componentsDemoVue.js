{
    /* <script> */
}
new Vue({
    el: '#components-demo',
    components: {
        'version-history-modal': httpVueLoader('/components/version-history-modal.vue'),
    },
    computed: {
        listVersions() {
            return headerVm.versions || [];
        },
        currentVersion() {
            return headerVm?.versions?.[0]?.version || 0;
        },
    },
    methods: {
        show() {
            alert('you clicked me!!!');
        },
        showPopup() {
            this.$refs['version-history-modal'].showPopupLargeCode();
        },
    },
});
// </script>
