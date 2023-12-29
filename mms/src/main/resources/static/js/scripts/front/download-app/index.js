new Vue({
    el: '#downloadApp',
    data: {
        country: null,
        platform: null,
    },
    components: {
        'view-download-ios': httpVueLoader('/components/download-app/view-download-ios.vue'),
        'view-download-android': httpVueLoader('/components/download-app/view-download-android.vue'),
        'download-china-apk': httpVueLoader('/components/download-app/download-china-apk.vue'),
        'download-client-info': httpVueLoader('/components/download-app/download-client-info.vue'),
    },
    computed: {
        downloadInstruction() {
            if (this.platform == 'ios') {
                return 'Tap above to download directly from the Apple App Store';
            } else {
                return this.country == 'China'
                    ? 'Tap above to download the APK installation file for your Android Device'
                    : 'Tap above to download directly from the Google Play Store';
            }
            return '';
        },
        downloadTextHeader() {
            if (this.platform == 'ios') {
                return 'Download the eMoldino mobile optimized App directly from the App Store';
            } else {
                return this.country == 'China'
                    ? 'Download the eMoldino mobile optimized App directly for your Android Device'
                    : 'Download the eMoldino mobile optimized App directly from the Playstore';
            }
            return '';
        },
    },
    methods: {
        getOperatingSystem() {
            const userAgent = navigator.userAgent || navigator.vendor || window.opera;
            if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
                this.platform = 'ios';
            } else this.platform = 'android';
        },
        async getCountry() {
            const ipData = await axios.get('http://ip-api.com/json');
            this.country = ipData.data.country;
        },
    },
    mounted() {
        this.getOperatingSystem();
        this.getCountry();
    },
});
