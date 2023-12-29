<template>
  <div class="download-content-wrapper"
  :class="{'download-button-container-active': active, 'download-button-container': !isMobile}"
  @click="onSelect">
    <img src="/images/common/emoldino_short_logo.png" alt="" class="apk-logo-icon">
  <div class="download-text-container">
    <span class="download-text-sub">{{ getSubtitle }}</span>
    <span class="app-name-text">{{ getAppName }}</span>
  </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    onSelectPlatform: {type: Function},
    active: Boolean,
    isMobile: Boolean

  },
  methods: {
    onSelect(){
      this.$emit('on-select', 'apk');
    }
  },
  computed:{
    getSubtitle(){
      return this.isMobile ? 'Download the' : 'Direct Download'
    },
    getAppName(){
      return this.isMobile ? 'eMoldino Mobile App APK' : 'Android APK'
    }
  },
}

</script>
<style scoped>

.download-content-wrapper {
  padding: 24px 38.54px 24.71px 26.43px;
}

.download-text-sub {
  font-family: Helvetica Neue;
  font-size: 11px;
  font-weight: 400;
  line-height: 13px;
  letter-spacing: 0em;
  text-align: left;
  color: #000000
}
.app-name-text {
  font-family: Helvetica Neue;
  font-size: 22px;
  font-weight: 700;
  line-height: 27px;
  letter-spacing: 0em;
  text-align: left;
  color: #000000
}
@media (max-width: 481px){
  .app-name-text {
    font-size: 17px !important;
    line-height: 20.76px !important;
  }
  .download-text-sub {
    font-size: 11px;
    line-height: 13px;
    letter-spacing: 0em;
    margin-top: 0 !important;
    margin-bottom: 0 !important;
  }
  .download-content-wrapper {
    padding: 9.45px 9.42px 15px 17px !important;
    display: flex;
  }
  .apk-logo-icon {
    width: 50px !important;
    height: 52px !important;
    margin-right: 20px !important;
  }
}
.download-button-container {
  background: #FFFFFF;
  box-shadow: 0px 3px 4px rgba(0, 0, 0, 0.15);
  border-radius: 3px;
  padding-left: 26.43px;
  width: 272px;
  cursor: pointer;
  height: 92.25px;
  border: 4px solid white;
  display: flex;
}
.download-button-container:hover {
  border: 4px solid #DAEEFF;
}
.download-button-container-active {
  border: 4px solid #DAEEFF;
}
.apk-logo-icon {
  width: 37.44px;
  height: 37.88px;
  margin-right: 20.92px;
}

.download-text-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  color: #000000
}
</style>