<script>
module.exports = {
  name: "InputFileViewImage",
  components: {
    "emdn-cta-button": EmoldinoComponents.CtaButton,
    "emdn-modal": EmoldinoComponents.Modal,
    "emdn-portal": EmoldinoComponents.Portal,
  },
  props: {
    fileList: {
      type: Array,
      default: () => [],
    },
  },
  setup(props, { emit }) {
    const isOpened = ref(false);
    const openModal = () => {
      isOpened.value = true;
    };
    const closeModal = () => {
      isOpened.value = false;
    };
    const imageSrc = ref("");
    const imageIndex = ref(0);

    const currentImage = computed(() => {
      return props.fileList?.[imageIndex.value] || null;
    });

    const readImage = async (currImg) => {
      if (!currImg) return;
      const reader = new FileReader();
      reader.onload = () => {
        imageSrc.value = reader.result;
      };
      const blobFile = currImg.fileRaw
        ? currImg.fileRaw
        : await axios.get(currImg.filePath);
      reader.readAsDataURL(blobFile);
    };

    const onDownloadImage = () => {
      Common.download(imageSrc.value, currentImage.value.fileName);
    };

    const totalPage = computed(() => props.fileList.length);
    const currentPage = computed(() => imageIndex.value + 1);

    watch(isOpened, (newVal) => {
      if (!newVal) {
        // clean
        imageIndex.value = 0;
      }
    });

    watch(currentImage, (newVal) => readImage(newVal), {
      immediate: true,
    });

    return {
      isOpened,
      currentImage,
      imageSrc,
      imageIndex,
      totalPage,
      currentPage,
      openModal,
      closeModal,
      onDownloadImage,
    };
  },
};
</script>

<template>
  <span>
    <emdn-cta-button
      variant="text hyperlink"
      color-type="blue"
      :click-handler="() => openModal()"
    >
      View Image(s)
    </emdn-cta-button>
    <emdn-portal v-if="isOpened">
      <emdn-modal
        :is-opened="isOpened"
        :heading-text="currentImage?.fileName || 'Preview Image'"
        :modal-handler="closeModal"
      >
        <template #body>
          <div
            class="d-flex justify-content-center align-items-center h-100 w-100"
          >
            <img
              :src="imageSrc"
              alt="Image Preview"
              class="w-100 h-100"
              style="object-fit: contain"
            />
          </div>
        </template>
        <template #footer>
          <div class="w-100 d-flex justify-content-between">
            <base-button
              size="medium"
              type="normal"
              level="secondary"
              @click="onDownloadImage"
            >
              <img src="/images/icon/download-preview.svg" alt="" />
            </base-button>
            <base-paging
              :total-page="totalPage"
              :current-page="currentPage"
              @next="imageIndex++"
              @back="imageIndex--"
            ></base-paging>
          </div>
        </template>
      </emdn-modal>
    </emdn-portal>
  </span>
</template>
