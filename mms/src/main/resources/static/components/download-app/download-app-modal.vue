<template>
  <div
      id="op-download-app-modal"
      class="modal fade"
      role="dialog"
      style="overflow-y: scroll"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="custom-modal-title modal-download-header">
          <div class="modal-title">
            <span class="modal-title-text modal-download-header-text">Download the Mobile App</span>
          </div>

          <div
              class="t-close-button close-button"
              @click="dismissModal"
              aria-hidden="true"
          >
            <i class="icon-close-black"></i>
          </div>
          <div class="head-line"></div>
        </div>

        <div class="d-modal-body">
          <div class="modal-content-container">
            <div class="content-wrapper">
              <div class="button-wrapper">
                <div
                    class="download-button-container"
                    :class="{'download-button-container-active': selectedPlatform == 'ios'}"
                    @click="onSelectPlatform('ios')"
                >
                  <view-download-ios></view-download-ios>
                </div>
                <div
                    class="download-button-container"
                    :class="{'download-button-container-active': selectedPlatform == 'android'}"
                    @click="onSelectPlatform('android')"
                >
                  <view-download-android></view-download-android>
                </div>
                <download-china-apk @on-select="onSelectPlatform" :active="selectedPlatform == 'apk'"></download-china-apk>
              </div>
              <div class="client-QR-wrapper">
                <download-client-info></download-client-info>
                  <img :src="qrCodePath" alt="" class="qr-code-wrapper"/>
                  <span class="scan-QR-text">Scan the QR Code</span>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  data() {
    return {
      selectedPlatform: 'ios',
      qrCodePath: '/images/icon/download-app/qr-download-ios.png',

    }
  },
  components:{
    "download-china-apk": httpVueLoader(
        "/components/download-app/download-china-apk.vue"
    ),
    "download-client-info": httpVueLoader(
        "/components/download-app/download-client-info.vue"
    ),
    "view-download-ios": httpVueLoader(
        "/components/download-app/view-download-ios.vue"
    ),
    "view-download-android": httpVueLoader(
        "/components/download-app/view-download-android.vue"
    )
  },
  mounted() {
    this.onSelectPlatform('ios');
  },
  methods: {
    showModal() {
      $(this.getRootId() + "#op-download-app-modal").modal("show");
    },
    dismissModal() {
      $(this.getRootId() + "#op-download-app-modal").modal("hide");
    },
    onSelectPlatform(platform){
      const qrCodeDirPath = {
            'ios': '/images/icon/download-app/qr-download-ios.png',
            'android': '/images/icon/download-app/qr-download-android.png',
            'apk': '/images/icon/download-app/qr-download-apk.png'
          }
      this.selectedPlatform = platform;
      this.qrCodePath = _.get(qrCodeDirPath, platform)
    },
  },
}

</script>
<style scoped>
.head-line {
  left: 0;
}
.custom-modal-title .modal-title{
  margin-left: 25px;
  display: inline-flex;
  align-items: center;
}
.modal-content-container {
  padding: 19px 48px 87px;
}
.modal-download-header {
  padding: 11px 25px 11px 1px;
  border-radius: 6px
}
.modal-download-header-text{
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4B4B4B;
}
.button-wrapper {
  display: grid;
  grid-template-rows: 1fr 1fr 1fr;
  grid-gap: 21.65px;
}
.content-wrapper {
  display: flex;
  padding-top: 91px;
  padding-bottom: 89px;
  justify-content: center;
}
.client-QR-wrapper {
  margin-left: 70px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.qr-code-wrapper {
  width: 220px;
  aspect-ratio: 1;
  border: 1px solid #8B8B8B;
  border-radius: 3px;
  padding: 10px;
  margin-top: 4.46px
}
.scan-QR-text{
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #000000;
  margin-top: 1px
}
.download-button-container {
  background: #FFFFFF;
  box-shadow: 0px 3px 4px rgba(0, 0, 0, 0.15);
  border-radius: 3px;
  border-width: 0;
  width: 272px;
  cursor: pointer;
  height: 92.25px;
  border: 4px solid white;
  padding: 0
}
.download-button-container-active {
  border: 4px solid #DAEEFF;
}
</style>