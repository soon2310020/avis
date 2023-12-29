<template>
  <div class="guide"> {{ capitalize(resources['need_help_getting_started']) }}? 
    <!-- <base-button icon="popup" @click="handleShowImportGuide"> {{ capitalizeAll(resources['view_import_guide']) }} </base-button> -->
    <base-button level="tertiary" @click="handleDownloadImportTemplate"> {{ capitalize(resources['download_data_import_template']) }} </base-button>
  </div>
</template>

<script>
module.exports = {
  name: 'GuideImport',
  props: {
    resources: Object
  },
  setup() {
    const handleShowImportGuide = () => { }
    const handleDownloadImportTemplate = () => {
      window.open('/api/common/dat-imp/templates')
    }
    return {
      handleShowImportGuide,
      handleDownloadImportTemplate
    }
  }
}
</script>

<style scoped>
.guide {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  font-size: 14.66px;
  margin-top: 40px;
  margin-bottom: 40px;
}
</style>