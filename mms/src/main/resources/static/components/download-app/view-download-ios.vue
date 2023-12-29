<template>
  <div class="download-content-wrapper">
    <img src="/images/icon/download-app/apple-icon.svg" alt="" class="apple-logo-icon">
    <div class="download-text-container">
      <img src="/images/icon/download-app/download-ios-text.png" alt="" class="download-ios-text"/>
      <img src="/images/icon/download-app/AppStore.png" alt="" class="download-store-name"/>
    </div>
  </div>
</template>

<script>
module.exports = {

}

</script>
<style scoped>
.download-ios-text {
  width: 150.67px;
  height: 13.28px;
  margin-bottom: 8.27px
}
.download-store-name {
  width: 154.98px;
  height: 33.11px;
}
.download-content-wrapper {
  display: flex;
  justify-content: space-between;
  padding: 17.56px 28.27px 24.16px 26.43px;
  cursor: pointer;
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
@media(max-width: 481px){
  .download-content-wrapper {
    padding: 11.89px 25.53px 7.85px 27.01px !important;
  }
}
</style>