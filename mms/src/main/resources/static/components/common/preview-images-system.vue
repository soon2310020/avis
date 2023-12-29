<template>
  <div>
    <div>
      <base-chip v-for="(file, index) in images" :key="index" :selection="file" :title-field="'name'" @close="item => handleDelete(item, index)"></base-chip>
      <base-chip v-for="file in imagesUploaded" :key="file.id" :selection="file" :title-field="'fileName'" @close="deleteUploadedImage"></base-chip>
      <span v-if="images.length > 0 || imagesUploaded.length > 0" class="custom-hyper-link" @click="showModal()">
        {{ allImages.length > 1 ? 'View Images' : 'View Image' }}
        <div class="hyper-link-icon"></div>
      </span>
    </div>
    <preview :all-images="allImages" v-model="show"></preview>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    images: {
      type: Array,
      default: () => []
    },
    imagesUploaded: {
      type: Array,
      default: () => []
    },
    fileTypes: String,
    isViewing: {
      type: Boolean,
      default: true
    },
  },
  components: {
    "preview": httpVueLoader("/components/common/preview-images-system/preview.vue"),
  },
  data() {
    return {
      imageTitle: 'imageTitle',
      show: false,
      allImages: []
    }
  },
  computed: {

  },
  watch: {
    images () {
      this.allImages = this.images.concat(this.imagesUploaded);
    },
    imagesUploaded () {
      this.allImages = this.images.concat(this.imagesUploaded);
    }
  },
  mounted () {
    if (Array.isArray(this.images.value)) this.allImages = this.images.value.concat(this.imagesUploaded);
    if (Array.isArray(this.images)) this.allImages = this.images.concat(this.imagesUploaded);
  },
  methods: {
    showModal() {
      this.show = true
    },
    dimissModal () {
      this.show = false
    },
    handleDelete(item, index) {
      console.log('handleDelete', item)
      // this.deleteImage(index)
      this.$emit('delete-img', index)
    },
    // deleteImage (index) {
    //   this.$emit('delete-img', index)
    // },
    deleteUploadedImage (file) {
      this.$emit('delete-uploaded-img', file, this.fileTypes)
    }
  }
}

</script>
<style scoped>
.brand-item {
  background: rgb(222, 237, 255);
  border-radius: 3px;
  padding: 4px 8px;
  cursor: pointer;
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: rgb(75, 75, 75);
  display: inline-flex;
  align-items: center;
  margin: 0 8px 3px 0;
}
.remove-icon {
  opacity: 0.7;
  width: 6px;
  height: 6px;
  margin-left: 5px
}
.icon-close-black {
  background-image: url('/images/icon/category/close.svg');
  background-repeat: no-repeat;
  background-size: 100%;
  display: inline-block;
}
.head-line {
  left: 0;
}
.modal-footer{
  border: none;
}
.error-text{
  font-size: 11px;color: #EF4444;text-align: left
}
.custom-modal-title .modal-title{
  margin-left: 25px;
  display: inline-flex;
  align-items: center;
}
.custom-hyper-link {
  color: #3491FF;
  cursor: pointer;
  position: relative;
  white-space: nowrap;
}
.custom-hyper-link:hover {
  color: #3585e5;
  text-decoration: underline;
}
.custom-hyper-link .hyper-link-icon {
  background-image: url('/images/icon/hyperlink-icon.svg');
  position: absolute;
  width: 7px;
  height: 7px;
  background-repeat: no-repeat;
  top: 0;
  right: -10px;
}
.custom-hyper-link:hover .hyper-link-icon {
  background-image: url('/images/icon/hyperlink-icon-hover.svg');
  width: 11px;
  height: 13px;
  right: -14px;
  top: -2px;
}
</style>