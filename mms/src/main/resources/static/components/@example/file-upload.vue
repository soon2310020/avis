<script>
module.exports = {
  name: "FileUploadExample",
  components: {
    "input-file": httpVueLoader("/components/@compose/input-file.vue"),
  },
  setup() {
    const fileInput = ref([]);
    const imageInput = ref([]);

    return {
      fileInput,
      imageInput,
    };
  },
};
</script>

<template>
  <div>
    <div class="d-flex mb-2">
      <label>Image Upload:</label>
      <input-file
        v-model="fileInput"
        type="document"
        label="Upload Document"
        class="flex-1"
        :multiple="true"
        :accept="'.gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx'"
      ></input-file>
    </div>

    <div class="d-flex mb-2">
      <label>File Upload:</label>
      <input-file
        v-model="imageInput"
        type="image"
        label="Upload Image"
        class="flex-1"
        :multiple="true"
        :accept="'image/*'"
      ></input-file>
    </div>
  </div>
</template>

<style scoped>
label {
  width: 100px;
  margin-bottom: 0;
}
</style>
