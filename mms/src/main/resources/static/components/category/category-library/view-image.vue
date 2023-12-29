<template>
  <div class="preview-images" v-show="value">
    <div class="vicp-wrap">
      <div class="custom-modal-title">
        <div>
          <span class="modal-title-text">{{ title }}</span>
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
      <div class="vicp-step2">
        <div class="vicp-crop">
          <div class="vicp-crop-left" v-show="true">
            <div
              v-for="(image, index) in allImages"
              :key="index"
              :class="{
                show: image?.saveLocation + index === imageSelected,
                'vicp-img-container': true,
              }"
            >
              <img :src="image?.saveLocation" class="vicp-img" />
            </div>
          </div>
        </div>
      </div>
      <div class="pagination-zone">
        <div class="btn-zone">
          <a-tooltip color="white">
            <template slot="title">
              <div
                style="
                  padding: 6px 8px;
                  font-size: 13px;
                  color: #3491ff;
                  background: #fff;
                  border-radius: 4px;
                  height: 32px;
                  box-shadow: 0 0 4px 1px #e5dfdf;
                "
              >
                Download Image
                <div class="custom-arrow-tooltip"></div>
              </div>
            </template>
            <a
              v-if="currentImage.id"
              @click="downloadImage()"
              id="download-preview"
              href="javascript:void(0)"
              class="btn-custom btn-outline-custom-primary customize-btn"
            >
              <img src="/images/icon/download-preview.svg" alt="" />
            </a>
          </a-tooltip>

          <div class="d-flex align-items-center">
            <div class="pagination-number">
              {{ currentIndexImage + 1 }} of {{ allImages.length }}
            </div>
            <button
              type="button"
              class="btn-custom btn-custom-primary mr-2"
              :disabled="currentIndexImage === 0"
              outline-animation
              @click="updateCurrentImage(currentIndexImage - 1)"
            >
              <img src="/images/icon/back-preview.svg" alt="" />
            </button>
            <button
              type="button"
              class="btn-custom btn-custom-primary"
              :disabled="currentIndexImage === allImages.length - 1"
              outline-animation
              @click="updateCurrentImage(currentIndexImage + 1)"
            >
              <img src="/images/icon/next-preview.svg" alt="" />
            </button>
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
      return this.currentImage.fileName
        ? this.currentImage.fileName
        : this.currentImage.name;
    },
  },
  watch: {
    value(newValue) {
      if (newValue) {
        this.currentIndexImage = 0;
        this.currentImage = this.allImages[0];
        console.log(
          this.currentImage.saveLocation,
          "this.currentImage.saveLocation"
        );
        this.imageSelected = this.currentImage.saveLocation + "0";
      }
    },
    allImages(newVal) {
      newVal.forEach((res) => {
        if (
          !res?.saveLocation &&
          (res instanceof Blob || res instanceof File)
        ) {
          this.viewImgLocal(res);
        }
      });
      if (newVal[0]) {
        this.currentImage = newVal[0];
        console.log(
          this.currentImage.saveLocation,
          "this.currentImage.saveLocation 22"
        );
        this.imageSelected = this.currentImage.saveLocation + "0";
      }
    },
  },
  methods: {
    viewImgLocal(file) {
      const fr = new FileReader();
      fr.onload = function (e) {
        let sourceImgUrl = fr.result;
        console.log(file.saveLocation, "this.currentImage.saveLocation");
        file.saveLocation = sourceImgUrl;
      };
      fr.readAsDataURL(file);
    },
    off() {
      const that = this;
      setTimeout(() => {
        this.$emit("input", false);
      }, 200);
    },
    updateCurrentImage(index) {
      this.currentIndexImage = index;
      this.currentImage = this.allImages[index];
      console.log(
        this.currentImage.saveLocation,
        "this.currentImage.saveLocation 33"
      );
      this.imageSelected = this.currentImage.saveLocation + index;
    },
    downloadImage() {
      const self = this;
      const el = document.getElementById("download-preview");
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        console.log(
          this.currentImage.saveLocation,
          "this.currentImage.saveLocation 44"
        );
        const url = this.currentImage.saveLocation;
        window.open(url, "_blank");
      }, 700);
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
.ant-tooltip-arrow {
  /*top: unset!important;*/
}
</style>
<style scoped>
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
  padding: 24px 24px 16px 24px !important;
}
img {
  max-width: 100%;
  max-height: 100%;
}
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
  width: 700px;
  height: 551px;
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
