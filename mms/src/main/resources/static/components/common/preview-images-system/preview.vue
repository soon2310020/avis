<template>
  <div class="preview-images" v-show="value">
    <div class="vicp-wrap">
      <div class="custom-modal-title">
        <div>
          <span
            class="modal-title-text"
            style="
              display: block;
              width: 600px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            "
          >
            {{ title }}
          </span>
        </div>

        <div
          class="t-close-button close-button"
          @click="off"
          aria-hidden="true"
        >
          <i class="icon-close-black"></i>
        </div>
        <div class="head-line"></div>
      </div>
      <div class="d-flex flex-column">
        <div class="vicp-step2">
          <div class="vicp-crop">
            <div class="vicp-crop-left" v-show="true">
              <div
                v-for="(image, index) in allImages"
                :key="index"
                :class="{
                  show: image.saveLocation + index === imageSelected,
                  'vicp-img-container': true,
                }"
              >
                <img :src="image.saveLocation" class="vicp-img" />
              </div>
            </div>
          </div>
        </div>
        <div class="pagination-zone">
          <div class="btn-zone">
            <a-tooltip color="white">
              <template slot="title">
                <div>
                  Download Image
                  <div class="custom-arrow-tooltip"></div>
                </div>
              </template>
              <!-- <a v-if="currentImage.id" @click="downloadImage()" id="download-preview" href="javascript:void(0)"
                class="btn-custom btn-outline-custom-primary customize-btn">
                <img src="/images/icon/download-preview.svg" alt="">
              </a> -->
              <base-button
                id="download-preview-btn"
                @click="downloadImage"
                :size="'medium'"
                :type="'normal'"
                :level="'secondary'"
              >
                <img src="/images/icon/download-preview.svg" alt="" />
              </base-button>
            </a-tooltip>
            <div class="d-flex align-items-center">
              <!-- <div class="pagination-number">
                {{ currentIndexImage + 1 }} of {{ allImages.length }}
              </div> -->
              <!-- <button type="button" class="btn-custom btn-custom-primary mr-2" :disabled="currentIndexImage === 0"
                outline-animation @click="updateCurrentImage(currentIndexImage - 1)">
                <img src="/images/icon/back-preview.svg" alt="">
              </button>
              <button type="button" class="btn-custom btn-custom-primary"
                :disabled="currentIndexImage === allImages.length - 1" outline-animation
                @click="updateCurrentImage(currentIndexImage + 1)">
                <img src="/images/icon/next-preview.svg" alt="">
              </button> -->
              <base-paging
                :total-page="allImages.length"
                :current-page="currentIndexImage + 1"
                @next="handleNextPage"
                @back="handleBackPage"
              ></base-paging>
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
    value: Boolean,
    allImages: Array,
  },
  components: {
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
  },
  data() {
    return {
      currentImage: {
        fileName: "",
        saveLocation: "",
      },
      currentIndexImage: 0,
      imageSelected: "",
    };
  },
  computed: {
    title() {
      return this.currentImage?.fileName
        ? this.currentImage.fileName
        : this.currentImage.name;
    },
  },
  watch: {
    value(newValue) {
      if (newValue) {
        console.log("view", this.allImages);
        this.currentIndexImage = 0;
        this.currentImage = this.allImages[0];
        this.imageSelected = this.currentImage.saveLocation + "0";
      }
    },
    allImages(newVal) {
      newVal.forEach((res) => {
        if (!res.saveLocation && (res instanceof Blob || res instanceof File)) {
          this.viewImgLocal(res);
        }
      });
      if (newVal[0]) {
        this.currentImage = newVal[0];
        this.imageSelected = this.currentImage.saveLocation + "0";
      }
    },
  },
  methods: {
    viewImgLocal(file) {
      fr = new FileReader();
      fr.onload = function (e) {
        let sourceImgUrl = fr.result;
        file.saveLocation = sourceImgUrl;
      };
      fr.readAsDataURL(file);
    },
    off() {
      // const that = this
      setTimeout(() => {
        this.$emit("input", false);
        console.log("off");
      }, 200);
    },
    handleNextPage() {
      this.updateCurrentImage(this.currentIndexImage + 1);
    },
    handleBackPage() {
      this.updateCurrentImage(this.currentIndexImage - 1);
    },
    updateCurrentImage(index) {
      this.currentIndexImage = index;
      this.currentImage = this.allImages[index];
      this.imageSelected = this.currentImage.saveLocation + index;
    },
    downloadImage() {
      const el = document.getElementById("download-preview-btn");
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        const url = this.currentImage.saveLocation;
        const fileName = this.currentImage.fileName || this.currentImage.name;
        console.log("🚀 ~ file: preview.vue:176 ~ setTimeout ~ url:", url);
        // console.log("🚀 ~ file: preview.vue:176 ~ setTimeout ~ url:", url);
        // const image = document.createElement("a");
        // image.download = "image.png";
        // image.target = "_self";
        // image.href = url;
        // image.click();
        this.downloadImageByURL(url, fileName);
      }, 700);
    },
    async downloadImageByURL(imageSrc, nameOfDownload = "image.png") {
      const response = await fetch(imageSrc);
      const blobImage = await response.blob();
      const href = URL.createObjectURL(blobImage);
      const anchorElement = document.createElement("a");
      anchorElement.href = href;
      anchorElement.download = nameOfDownload;
      document.body.appendChild(anchorElement);
      anchorElement.click();
      document.body.removeChild(anchorElement);
      window.URL.revokeObjectURL(href);
    },
  },
  handleEscClose(e) {
    if (this.value && (e.key === "Escape" || e.keyCode === 27)) {
      this.off();
    }
  },
  created() {
    document.addEventListener("keyup", this.handleEscClose);
  },
  beforeDestroy() {
    document.removeEventListener("keyup", this.handleEscClose);
  },
};
</script>
<style>
/* TODO NEED RECHECK */
.ant-tooltip-arrow {
  top: unset !important;
}
</style>
<style>
.customize-btn {
  background: rgb(214 218 222 / 37%);
  border-color: transparent;
}

.customize-btn:hover {
  background: #daeeff;
  border-color: #3585e5;
}

.pagination-zone {
  padding: 14px;
}

.pagination-zone button {
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-number {
  margin-right: 7px;
  font-size: 16px;
  color: #8b8b8b;
}

.modal-title-text {
  font-weight: 700;
  font-size: 15px;
}

.custom-modal-title {
  border-radius: 6px 6px 0 0;
  padding-bottom: 16px;
}

/* TODO: RECHECK IMPACT */
/* img {
  max-width: 100%;
  max-height: 100%;
} */

button:disabled,
button[disabled] {
  border: 1px solid #c4c4c4 !important;
  background-color: #c4c4c4 !important;
}

.btn-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@-webkit-keyframes vicp {
  0% {
    opacity: 0;
    -webkit-transform: scale(0) translatey(-60px);
    transform: scale(0) translatey(-60px);
  }

  100% {
    opacity: 1;
    -webkit-transform: scale(1) translatey(0);
    transform: scale(1) translatey(0);
  }
}

@keyframes vicp {
  0% {
    opacity: 0;
    -webkit-transform: scale(0) translatey(-60px);
    transform: scale(0) translatey(-60px);
  }

  100% {
    opacity: 1;
    -webkit-transform: scale(1) translatey(0);
    transform: scale(1) translatey(0);
  }
}

.preview-images {
  backdrop-filter: blur(2px);
  position: fixed;
  display: block;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  z-index: 10000;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  -webkit-tap-highlight-color: transparent;
  -moz-tap-highlight-color: transparent;
}

.preview-images .vicp-wrap {
  -webkit-box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.23);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.23);
  position: fixed;
  display: block;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  z-index: 10000;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  margin: auto;
  width: 700px !important;
  height: 551px !important;
  background-color: #fff;
  border-radius: 6px;
}

.preview-images .vicp-wrap .vicp-step2 .vicp-crop {
  overflow: hidden;
}

.preview-images .vicp-wrap .vicp-step2 .vicp-crop .vicp-crop-left {
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-img-container {
  position: relative;
  margin: auto;
  height: 400px;
  overflow: hidden;
  display: none;
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-img-container.show {
  display: flex;
  justify-content: center;
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-rotate
  i {
  display: block;
  width: 18px;
  height: 18px;
  border-radius: 100%;
  line-height: 18px;
  text-align: center;
  font-size: 12px;
  font-weight: bold;
  /*background-color: rgba(0, 0, 0, 0.08);*/
  color: #fff;
  overflow: hidden;
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-rotate
  i:hover {
  -webkit-box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12);
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.14);
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-rotate
  i:first-child {
  float: left;
}

.preview-images
  .vicp-wrap
  .vicp-step2
  .vicp-crop
  .vicp-crop-left
  .vicp-rotate
  i:last-child {
  float: right;
}

.vicp-step2 {
  padding: 10px 0;
}
</style>
