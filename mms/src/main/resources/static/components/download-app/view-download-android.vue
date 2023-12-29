<template>
  <div class="download-content-wrapper">
    <img src="/images/icon/download-app/PlayStore.png" alt="" class="img-play-store-logo">
    <div class="download-text-container">
      <img src="/images/icon/download-app/download-android-text.png" alt="" class="download-android-text"/>
      <img src="/images/icon/download-app/GooglePlay.png" alt="" class="download-store-name"/>
    </div>
  </div>
</template>

<script>
module.exports = {
}

</script>
<!-- <style scoped src="/components/download-app/download-app-style.css"></style> -->
<style scoped>
.download-android-text {
  height: 11.63px;
  width: 72.75px;
  margin-bottom: 7.08px
}
.download-store-name {
  height: 31.586px;
  width: 158.76px;
}
.download-content-wrapper {
  padding: 22.68px 28.63px 23.34px 26.43px;
  display: flex;
}
@media(max-width: 481px) {
  .download-content-wrapper {
    padding: 14.24px 19.52px 14.81px 21.2px !important;
  }
}
.apple-logo-icon {
  width: 39.34px;
  height: 48.5px;
  margin-right: 23.83px;
}
.download-text-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.img-play-store-logo {
  height: 46.53px;
  width: 43.24px;
  margin-right: 14.94px
}
</style>