<template>
  <div class="upload-file">
    <div class="lead">{{ capitalize(resources["upload_your_files"]) }}</div>
    <guide-import :resources="resources"></guide-import>
    <div class="upload-area">
      <base-upload
        ref="baseUploadRef"
        class="custom-base-upload"
        :replace="false"
        :multiple="true"
        :accept="listAccepted"
        :list-files="listFiles"
        @change="handleChangeFile"
      >
        <span class="custom-base-upload__icon">
          <span class="icon icon-upload"></span>
        </span>
        <span class="custom-base-upload__line">
          {{ capitalize(resources["drag_and_drop"]) }} {{ resources["or"] }}
          <span class="highlight"> {{ resources["browse_file"] }}</span>
          {{ resources["to_upload"] }}
        </span>
        <span class="custom-base-upload__note">
          {{ capitalizeAll(resources["accepted_file_types"]) }}:
          {{ listAcceptedDisplayed }}
        </span>
      </base-upload>
      <div class="upload-option">
        <label>
          <input type="checkbox" v-model="shouldOverwriting" />
          <span>{{ capitalize(resources["overwriting_existing_data"]) }}</span>
        </label>
      </div>
      <div class="upload-processing">
        <div
          v-for="(file, index) in listFiles"
          :key="index"
          class="upload-processing-item"
        >
          <div
            v-if="isFetching[index]"
            class="loading-wave"
            style="height: 76px; width: 100%"
          ></div>
          <template v-else>
            <div class="upload-processing__icon">
              <span class="icon icon-single-document"></span>
            </div>
            <div class="upload-processing__file">
              <span class="file-name">
                {{ capitalize(resources["file_successfully_uploaded"]) }} "{{
                  file.name || file.fileName
                }}"
              </span>
              <span class="file-loading"></span>
              <span class="file-size"
                >{{ appendThousandSeperator(file.size) }} KB</span
              >
              <span class="file-remove" @click="handleRemoveFile(index)">
                <span class="icon icon-close"></span>
              </span>
            </div>
          </template>
        </div>
      </div>
    </div>
    <div class="action-bar">
      <base-button level="secondary" @click="handleBack">{{
        resources["previous"]
      }}</base-button>
      <base-button
        level="primary"
        :disabled="!isNextAvailable"
        @click="handleNext"
        >{{ resources["next"] }}</base-button
      >
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "StepUpload",
  components: {
    "guide-import": httpVueLoader("../guide-import/guide-import.vue"),
  },
  props: {
    resources: Object,
  },
  setup(props, { refs, emit }) {
    const listFiles = ref([]);
    const listFilesCloned = ref([]); // using for re-upload
    const listAccepted = ref(["xlsx"]);
    const listAcceptedDisplayed = computed(() => {
      if (!listAccepted.value.length) return "*";
      return listAccepted.value.map((ext) => `.${ext}`).join(", ");
    });
    const shouldOverwriting = ref(false);
    const isFetching = ref([]);
    const isNextAvailable = computed(
      () => !isFetching.value.some((i) => i === true) && listFiles.value.length
    );

    const fetchUploadFiles = async (file, index) => {
      const formData = new FormData();
      formData.append("file", file);
      Vue.set(isFetching.value, index, true);
      listFiles.value.push(file);
      try {
        const response = await axios.post("/api/common/fle-tmp", formData);
        console.log("response", response);
        file.id = response.data.id;
        listFiles.value.splice(index, 1, file);
      } catch (error) {
        console.log(error);
      } finally {
        isFetching.value.splice(index, 1, false);
      }
    };

    const handleChangeFile = (selectedFiles = []) => {
      console.log("@handleChange:selectedFiles", selectedFiles);
      const offset = listFiles.value.length;
      selectedFiles.forEach((file, index) => {
        fetchUploadFiles(file, offset + index);
        listFilesCloned.value.push(file);
      });
    };

    const handleRemoveFile = (index) => {
      listFiles.value.splice(index, 1);
      listFilesCloned.value.splice(index, 1);
    };

    const handleNext = () => {
      emit("next", {
        shouldOverwriting: shouldOverwriting.value,
        listFiles: listFiles.value,
      });
    };
    const handleBack = () => {
      emit("prev");
    };

    //
    const cleanState = () => {
      listFiles.value = [];
      listFilesCloned.value = [];
      shouldOverwriting.value = false;
      isFetching.value = [];
    };

    return {
      isFetching,
      isNextAvailable,
      listFiles,
      listAccepted,
      listAcceptedDisplayed,
      shouldOverwriting,
      handleChangeFile,
      handleRemoveFile,
      handleNext,
      handleBack,

      // expose to parent component
      listFilesCloned,
      cleanState,
      fetchUploadFiles,
    };
  },
};
</script>

<style scoped>
label {
  display: inline-flex;
  margin-bottom: 0;
  align-items: center;
  gap: 0.5rem;
}

.upload-file .lead {
  font-size: 14.66px;
  font-weight: bold;
  text-align: center;
}

.custom-base-upload {
  max-width: 644px;
  min-height: 124px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  margin: auto;
}

.custom-base-upload__line {
  font-size: 14.66px;
  line-height: 17px;
  color: var(--grey-dark);
}

.upload-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14.66px;
  max-width: 644px;
  margin: auto;
  margin-top: 0.5rem;
  margin-bottom: 2rem;
}

.custom-base-upload .custom-base-upload__note {
  position: absolute;
  left: 6px;
  bottom: 6px;
  font-size: 12px;
  font-weight: 400;
  line-height: 13px;
  color: var(--grey-8);
}

/* proccessing */
.upload-processing {
  max-height: 150px;
  overflow: auto;
  max-width: 644px;
  margin: auto;
}

@media (max-height: 768px) {
  .upload-processing {
    max-height: 80px;
  }
}

.upload-processing .upload-processing-item {
  position: relative;
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 14px 28px;
  margin-left: 26px;
  margin-right: 26px;
}

.upload-processing .upload-processing-item::after {
  position: absolute;
  bottom: 0;
  left: 0;
  content: "";
  display: block;
  background-color: var(--line-d6);
  height: 1px;
  width: 100%;
}

.upload-processing .upload-processing-item:last-child::after {
  display: none;
}

.upload-processing .upload-processing-item:last-child {
  margin-bottom: 0;
}

.upload-processing .upload-processing__file .file-name {
  display: block;
  font-weight: 600;
}

.upload-processing .upload-processing__file .file-size {
  font-size: 11.25px;
  color: var(--grey-8b);
}

.upload-processing .upload-processing__file .file-remove {
  position: absolute;
  right: 28px;
  top: 14px;
  display: flex;
  width: 1rem;
  height: 1rem;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.action-bar {
  position: fixed;
  bottom: calc(46px + 2rem);
  right: 2rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.highlight {
  color: var(--blue);
}

.highlight:hover {
  color: var(--blue-dark);
  text-decoration: underline;
}

/* icon */
.upload-processing__icon .loading-gif {
  width: 34px;
}

.icon {
  display: inline-block;
  mask-repeat: no-repeat;
  mask-size: contain;
  mask-position: center;
  -webkit-mask-repeat: no-repeat;
  width: 34px;
  height: 42px;
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  background: var(--grey-dark);
}

.icon.icon-single-document {
  width: 34px;
  height: 42px;
  mask-image: url("/images/icon/file-new.svg");
  -webkit-mask-image: url("/images/icon/file-new.svg");
}

.icon.icon-close {
  width: 8px;
  height: 8px;
  mask-image: url("/images/icon/close.svg");
  -webkit-mask-image: url("/images/icon/close.svg");
}

.icon.icon-upload {
  width: 34px;
  height: 42px;
  mask-image: url("/images/icon/upload-3.svg");
  -webkit-mask-image: url("/images/icon/upload-3.svg");
}
</style>
