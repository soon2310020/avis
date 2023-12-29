<template>
  <span>
    <base-button level="secondary" icon="upload" @click="handleClickUpload">
      <slot></slot>
    </base-button>
    <input
      type="file"
      ref="inputRef"
      multiple
      @change="handleUploadDocument"
      style="display: none"
    />
  </span>
</template>

<script>
module.exports = {
  name: 'ButtonUpload',
  setup(props, { emit, refs }) {
    const handleClickUpload = () => {
      refs.inputRef.click()
    }

    const handleUploadDocument = (event) => {
      emit('upload', event)
    }

    return {
      handleClickUpload,
      handleUploadDocument
    }
  }
}
</script>
