<template>
  <div>
    <div class="d-flex justify-center">
      <div class="preview-image">
        <template v-if="selectedImage?.saveLocation">
          <div>
            <div v-for="image in images" :key="image.id">
              <img
                v-show="image.id === selectedImage.id"
                :src="image.saveLocation"
                alt=""
                style="width: 331px; height: 230px; object-fit: contain"
              />
            </div>
          </div>
          <div class="expand-icon-container">
            <base-tooltip color="grey" background="white">
              <template slot="content">
                <div>
                  <span style="white-space: nowrap">Expand View</span>
                </div>
              </template>
              <a
                ref="customization-modal"
                href="javascript:void(0)"
                class="btn-custom btn-outline-custom-primary customize-btn alingment-item--center"
                style="width: 26px; height: 26px; padding: 5px"
                @click="handleShowExpandImage"
              >
                <div class="expand-icon"></div>
              </a>
            </base-tooltip>
          </div>
        </template>
        <div v-else class="default-image--large"></div>

        <div v-click-outside="closeDropdown">
          <div class="edit-icon-container" @click="showDropdown">
            <div class="edit-icon-blue"></div>
          </div>
          <div v-if="isShowDropdown" class="edit-dropdown">
            <div @click.stop="toggleShow" class="edit-dropdown-item">
              Upload Photo
            </div>
            <div @click.stop="showWarning()" class="edit-dropdown-item">
              Remove
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="carousel-container" :key="renderCarouselKey">
      <images-carousel
        :list-item="images"
        :selected-item="selectedImage"
        :change-key="changeCarouselKey"
        @change-image="handleSelectImage"
      ></images-carousel>
    </div>
    <div
      v-if="images.length > 4"
      class="d-flex justify-end"
      style="margin-top: 8px"
    >
      Total: {{ images.length }}
    </div>
    <preview :all-images="images" v-model="showPreview"></preview>

    <delete-image
      ref="delete-image"
      :confirm-delete="confirmRemove"
      :is-show-warning-again="true"
      :resources="resources"
      :is-remove-image="true"
    ></delete-image>
    <vue-upload
      field="img"
      @crop-success="cropSuccess"
      @src-file-set="getFileSet"
      v-model="showUpload"
      :width="250"
      :height="250"
      img-bgc="#2E2E2E"
      :params="params"
      :headers="headers"
    ></vue-upload>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="manageToast"
    ></toast-alert>
  </div>
</template>

<script>
module.exports = {
  components: {
    preview: httpVueLoader(
      "/components/common/preview-images-system/preview.vue"
    ),
    "images-carousel": httpVueLoader(
      "/components/@base/images-preview/images-carousel.vue"
    ),
    "delete-image": httpVueLoader("/components/delete-image.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
  },
  props: {
    images: {
      type: Array,
      default: () => [],
    },
    clearKey: {
      type: [Number, String],
    },
    handleSubmitFile: {
      type: Function,
    },
  },
  setup(props, ctx) {
    // STATE //
    const showPreview = ref(false);
    const selectedImage = ref(null);
    const isShowDropdown = ref(false);
    const showUpload = ref(false);
    const imgDataUrl = ref(null);
    const imgName = ref("");
    const changeCarouselKey = ref(0);
    const renderCarouselKey = ref(0);
    const toastAlert = ref({
      value: false,
      title: "",
      content: "",
    });
    const params = ref({
      token: "123456798",
      name: "avatar",
    });
    const headers = {
      smail: "*_~",
    };

    // COMPUTED //
    const resources = computed(() => {
      return headerVm?.resourcesFake || {};
    });

    watch(
      () => props.clearKey,
      () => {
        showPreview.value = false;
        selectedImage.value = null;
        isShowDropdown.value = false;
      }
    );

    // METHODS //

    const handleSelectImage = (item) => {
      console.log("handleSelectImage", item);
      selectedImage.value = item;
    };

    const handleShowExpandImage = () => {
      console.log("handleShowExpandImage");
      const el = ctx.refs["customization-modal"];
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        showPreview.value = true;
      }, 700);
    };

    const showDropdown = () => {
      console.log("showDropdown");
      isShowDropdown.value = true;
    };

    const closeDropdown = () => {
      isShowDropdown.value = false;
    };

    const toggleShow = () => {
      showUpload.value = true;
      closeDropdown();
      console.log("toggleShow");
    };

    const showWarning = () => {
      const child = ctx.refs["delete-image"];
      console.log("showWarning", child);
      if (child) {
        closeDropdown();
        child.showDeletePopup();
      }
    };

    const confirmRemove = async () => {
      try {
        const currentIndex = props.images.findIndex(
          (item) => item.id === selectedImage.value.id
        );
        const nextIndex =
          props.images.length === currentIndex + 1
            ? currentIndex - 1
            : currentIndex;
        console.log("confirmRemove after", nextIndex, selectedImage.value);
        const res = await axios.delete(
          `/api/file-storage/${selectedImage.value.id}`
        );
        // ctx.emit('remove-image', res.data)
        props.images = res.data;
        selectedImage.value = props.images[nextIndex];
        console.log("confirmRemove after", nextIndex, selectedImage.value);
        changeCarouselKey.value++;
        const child = ctx.refs["delete-image"];
        if (child) {
          child.onCloseModal();
        }
        const toastContent = {
          title: "Success!",
          content: "Your photo has been removed successfully.",
        };
        handleShowToast(toastContent);
      } catch (error) {
        console.log(error);
      }
    };

    const cropSuccess = async (imgDataUrl, field) => {
      console.log(
        "🚀 ~ file: base-images-slider.vue:235 ~ cropSuccess ~ field:",
        field
      );
      fetch(imgDataUrl).then(async (res) => {
        const blob = await res.blob();
        console.log("cropSuccess", blob);
        const beforeUpdateSize = props.images.length;
        if (props.handleSubmitFile) {
          const file = blobToFile(blob, imgName.value);
          await props.handleSubmitFile(file);
          selectedImage.value = [...props.images].pop();
          console.log(
            "handle submit file",
            props.images,
            selectedImage.value,
            [...props.images].pop()
          );
          if (beforeUpdateSize === 0) {
            renderCarouselKey.value++;
          }
          const toastContent = {
            title: "Success!",
            content: "Your photo has been uploaded successfully.",
          };
          handleShowToast(toastContent);
          changeCarouselKey.value++;
        }
      });
    };

    function blobToFile(theBlob, fileName) {
      return new File([theBlob], fileName, {
        lastModified: new Date().getTime(),
        type: theBlob.type,
      });
    }

    const getFileSet = (name) => {
      console.log("getFileSet");
      imgName.value = name;
    };

    const removeImgUploaded = () => {
      console.log("removeImgUploaded");
      imgDataUrl.value = null;
      imgName.value = null;
    };

    const handleShowToast = (toastContent) => {
      toastAlert.value.title = toastContent.title;
      toastAlert.value.content = toastContent.content;
      ctx.root.$nextTick(() => {
        toastAlert.value.value = true;
        setTimeout(() => {
          closeToastAlert();
        }, 3000);
      });
    };

    const closeToastAlert = () => {
      toastAlert.value.value = false;
      setTimeout(() => {
        toastAlert.value.title = "";
        toastAlert.value.content = "";
      }, 1000);
    };

    watch(
      () => props.images,
      (newVal) => {
        if (newVal && newVal?.[0]) {
          selectedImage.value = newVal[0];
        }
      },
      { immediate: true }
    );

    return {
      showPreview,
      selectedImage,
      isShowDropdown,
      showUpload,
      changeCarouselKey,
      renderCarouselKey,
      params,
      headers,
      toastAlert,
      // COMPUTED //
      resources,
      // METHODS //
      handleSelectImage,
      handleShowExpandImage,
      closeDropdown,
      showWarning,
      confirmRemove,
      showDropdown,
      toggleShow,
      getFileSet,
      removeImgUploaded,
      cropSuccess,
      handleShowToast,
      closeToastAlert,
    };
  },
};
</script>

<style scoped>
.preview-image {
  width: 331px;
  height: 230px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f2f2f2;
  position: relative;
}

.carousel-container {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}

.edit-icon-container {
  position: absolute;
  right: 9px;
  bottom: 9px;
  cursor: pointer;
}

.expand-icon-container {
  position: absolute;
  right: 6px;
  top: 6px;
  cursor: pointer;
}

.edit-dropdown {
  width: 160px;
  position: absolute;
  top: 200px;
  right: -80px;
  box-shadow: 0px 3px 6px #00000029;
  z-index: 4000;
}

.edit-dropdown .edit-dropdown-item:hover {
  background: #e6f7ff;
}

.edit-dropdown .edit-dropdown-item {
  cursor: pointer;
  padding: 7px 14px;
  color: #595959;
  background: #fff;
}
</style>
