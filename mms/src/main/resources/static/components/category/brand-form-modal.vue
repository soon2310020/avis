<template>
  <base-dialog
    :title="getModalTitle"
    :visible="isShow"
    :dialog-classes="isInModal ? 'dialog-md' : 'dialog-lg'"
    body-classes="custom-modal__body"
    @close="handleClose"
  >
    <div class="create-brand-container">
      <div class="form-group row line-row-wrapper">
        <label
          class="col-form-label"
          for="name"
          :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
        >
          {{ resources["brand_name"] }}
          <span class="badge-require"></span>
        </label>
        <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
          <input
            type="text"
            id="name"
            v-model="brand.name"
            class="form-control"
            :class="{
              'form-control-warning': brand.name == '' || brand.name == null,
            }"
            :placeholder="resources['brand_name']"
            required
            @keyup="removeErrors('name')"
          />
          <div
            v-if="errors['name']"
            class="error-text"
            v-text="resources['error_input_brand_name']"
          ></div>
        </div>
      </div>
      <div class="form-group row line-row-wrapper">
        <label
          class="col-form-label"
          for="description"
          :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
        >
          {{ resources["description"] }}
        </label>

        <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
          <input
            type="text"
            id="description"
            v-model="brand.description"
            class="form-control"
            :class="{
              'form-control-warning':
                brand.description == '' || brand.description == null,
            }"
            :placeholder="resources['description']"
            required
            @keyup="removeErrors('description')"
          />
        </div>
      </div>
      <div class="form-group row line-row-wrapper">
        <label
          class="col-form-label"
          for="description"
          :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
        >
          {{ resources["parent_category"] }}
        </label>
        <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
          <div class="brand-wrapper-line">
            <custom-dropdown-button
              :title="dropdownTitle"
              :is-selected="true"
              :is-show="categoryVisible"
              @click="toggledCategoryDropdown"
              :disabled="isInModal && selectedCategory"
            ></custom-dropdown-button>
          </div>

          <common-popover
            :is-visible="categoryVisible"
            :position="{ top: '60%', left: '15px' }"
            :style="{ width: '210px' }"
            @close="handleCloseCategorySelect"
          >
            <common-select
              :style="{ position: 'static', width: '100%' }"
              :items="categoryOptionList"
              :searchbox="categoryOptionList.length > 5"
              @on-select="onChangeCategory"
              @close="handleCloseCategorySelect"
            ></common-select>
          </common-popover>
          <div style="margin-top: 8px" v-if="!isInModal">
            <span>
              <i class="cant-find-brand-txt">Can't find category?</i>
            </span>
            <span
              class="custom-hyper-link"
              style="margin-left: 13px"
              @click="goToCategory"
            >
              Create Category
              <div class="hyper-link-icon"></div>
            </span>
          </div>
        </div>
      </div>
      <!-- <div class="form-group row">
          <selection-card @close="handleRemoveSeletion"></selection-card>
          <selection-card @close="handleRemoveSeletion"></selection-card>
          <selection-card @close="handleRemoveSeletion"></selection-card>
        </div> -->
      <div class="form-group row line-row-wrapper">
        <label
          class="col-form-label"
          for="description"
          :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
        >
          {{ resources["division"] }}
        </label>

        <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
          <input
            type="text"
            id="description"
            v-model="brand.division"
            class="form-control"
            :class="{
              'form-control-warning':
                brand.division == '' || brand.division == null,
            }"
            :placeholder="resources['division']"
          />
        </div>
      </div>
      <div class="form-group row line-row-wrapper">
        <label
          class="col-form-label"
          :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
        >
          {{ resources["brand_image"] }}
        </label>
        <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
          <div class="op-upload-button-wrap">
            <button
              v-if="imgName || imgDataUrl"
              class="btn btn-cta-white-disabled"
            >
              Upload Image
              <img
                style="margin-left: 3px"
                src="/images/icon/category/upload-disabled.svg"
                alt=""
              />
            </button>
            <base-button
              v-else
              @click="toggleShow"
              :size="'medium'"
              :type="'normal'"
              :level="'secondary'"
            >
              Upload Image
              <img
                style="margin-left: 3px"
                src="/images/icon/category/upload-active.svg"
                alt=""
              />
            </base-button>
            <template v-if="imgName || imgDataUrl">
              <a-tooltip
                v-if="getTruncateFileName.shouldTruncate"
                placement="bottom"
              >
                <template slot="title">
                  <div style="padding: 6px 8px; word-break: break-word">
                    <span>{{ imgName }}</span>
                  </div>
                </template>
                <span class="image-name-zone">
                  {{ getTruncateFileName.text }}</span
                >
                <button class="icon-button" @click.stop="showWarning()">
                  <span class="icon icon-remove"></span>
                </button>
              </a-tooltip>
              <span v-else class="image-name-zone">
                {{ imgName }}
                <button class="icon-button" @click.stop="showWarning()">
                  <span class="icon icon-remove"></span>
                </button>
              </span>
            </template>
          </div>

          <div>
            <button
              v-for="(file, index) in thirdFiles"
              :key="index"
              class="btn btn-outline-dark btn-sm mr-1"
              @click.prevent="deleteThirdFiles(index)"
            >
              {{ file.name }}
            </button>
          </div>
        </div>
      </div>
      <div
        class="d-flex modal__footer"
        :style="isInModal ? { marginTop: '36px' } : {}"
      >
        <base-button
          :size="'medium'"
          :type="'cancel'"
          :level="'primary'"
          class="modal__footer__button"
          @click="handleClose"
        >
          Cancel
        </base-button>
        <base-button
          :size="'medium'"
          :type="'normal'"
          :level="'primary'"
          class="modal__footer__button"
          @click="saveData"
        >
          {{ brand.id ? "Save Changes" : "Create Brand" }}
        </base-button>
      </div>
      <delete-image
        :confirm-delete="confirmRemove"
        :is-show-warning-again="true"
        :resources="resources"
        :is-remove-image="true"
      ></delete-image>
    </div>
    <vue-upload
      v-model="show"
      ref="uploadModalRefBrand"
      field="img"
      @crop-success="cropSuccess"
      @crop-upload-success="cropUploadSuccess"
      @crop-upload-fail="cropUploadFail"
      @src-file-set="getFileSet"
      @clear="removeImgUploaded"
      :width="250"
      :height="250"
      img-bgc="#2E2E2E"
      :params="params"
      :headers="headers"
      img-format="png"
    ></vue-upload>
  </base-dialog>
</template>

<script>
const FILE_TRUNCATE = 20;
module.exports = {
  props: {
    resources: {
      type: Object,
    },
    isShow: {
      type: Boolean,
      default: () => false,
    },
    selectedBrand: {
      type: Object,
      default: () => {},
    },
    isInModal: {
      type: Boolean,
      default: () => false,
    },
    selectedCategory: Object,
    defaultBrand: {
      type: Object,
      default: () => null,
      validator(value) {
        return value != null;
      },
    },
    openCategoryModal: Function,
    isEdit: {
      type: Boolean,
      default: () => false,
    },
  },
  components: {
    "delete-image": httpVueLoader("/components/delete-image.vue"),
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "selection-card": httpVueLoader(
      "/components/@base/select/selection-card.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
  },
  data() {
    return {
      brand: {
        name: "",
        description: "",
        division: "",
        grandParentId: "",
      },
      errors: [],
      // start upload variable
      show: false,
      params: {
        token: "123456798",
        name: "avatar",
      },
      headers: {
        smail: "*_~",
      },
      imgDataUrl: null,
      imgName: "",
      // end upload variable
      thirdFiles: [],
      partPictureFiles: [],
      categoryVisible: false,
      selectedParentCategory: null,
      categoryOptionList: [],
    };
  },
  computed: {
    dropdownTitle() {
      if (this.isInModal && this.selectedCategory) {
        return (
          this.truncateText(this.selectedCategory?.name, 20) ||
          "No Category Name"
        );
      }
      return this.selectedParentCategory
        ? this.truncateText(this.selectedParentCategory.title, 20)
        : "Select Parent Category";
    },
    getModalTitle() {
      if (this.brand.id) {
        return "Edit Brand";
      }
      return "Create Brand";
    },
    getStyle() {
      if (this.isInModal && this.isShow) {
        return { "z-index": "5000" };
      } else {
        return {};
      }
    },
    getTruncateFileName() {
      return {
        shouldTruncate: this.imgName.length > FILE_TRUNCATE,
        text: this.imgName?.slice(0, FILE_TRUNCATE) + "...",
      };
    },
  },
  watch: {
    isShow: async function (newVal) {
      console.log("isShow", newVal, this.defaultBrand, this.selectedCategory);
      if (newVal) {
        if (this.defaultBrand) {
          this.brand = { ...this.defaultBrand };
          //TODO
          this.categoryOptionList = await this.loadCategoryOption();
          if (this.brand.grandParentId != 0) {
            const find = this.categoryOptionList.find(
              (item) => item.id == this.brand.grandParentId
            );
            console.log(
              "find",
              this.brand.grandParentId,
              this.categoryOptionList,
              find
            );
            this.selectedParentCategory = find;
          }
        } else {
          //TODO
          this.categoryOptionList = await this.loadCategoryOption();
          if (this.selectedCategory.id) {
            this.selectedParentCategory = {
              id: this.selectedCategory.id,
              title: this.selectedCategory.name,
              checked: true,
            };
          } else {
            this.selectedParentCategory = null;
          }
        }
        // TODO: check
        this.loadFiles();
      } else {
        this.clearData();
      }
    },
    selectedParentCategory(newVal) {
      console.log("selectedParentCategory", newVal);
      this.brand.grandParentId = newVal ? newVal.id : "0";
    },
    selectedCategory(newValue) {
      if (!!newValue?.id) {
        this.selectedParentCategory = newValue;
        this.selectedParentCategory.title = this.truncateText(
          newValue?.name,
          20
        );
      }
    },
  },
  methods: {
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    // start upload
    toggleShow() {
      this.removeImgUploaded();
      this.show = !this.show;
    },
    closeUploadModal(event) {
      const wrapperEl = this.$refs.uploadModalRefBrand.$el;
      const mainEl = wrapperEl.children[0]; // point to div.vicp-wrap
      if (!(mainEl === event.target || mainEl?.contains(event.target))) {
        // click ousite main el
        this.toggleShow();
      }
    },
    showWarning() {
      $(this.getRootId() + "#op-delete-image").modal("show");
    },
    cropSuccess(imgDataUrl) {
      this.imgDataUrl = imgDataUrl;
    },
    cropUploadSuccess(jsonData, field) {},
    cropUploadFail(status, field) {},
    getFileSet(name) {
      this.imgName = name;
    },
    confirmRemove() {
      this.removeImgUploaded();
      if (this.partPictureFiles && this.partPictureFiles.length > 0) {
        for (const file of this.partPictureFiles) {
          this.deleteFileStorage(file, Common.Const.StorageType.PROJECT_IMAGE);
        }
      }
      $(this.getRootId() + "#op-delete-image").modal("hide");
    },
    removeImgUploaded() {
      console.log("removeImgUploaded");
      this.imgDataUrl = null;
      this.imgName = null;
    },
    //end upload
    handleClose() {
      this.$emit("close");
      this.clearData();
    },
    removeErrors(type) {
      this.errors[type] = false;
      this.$forceUpdate();
    },
    validForm() {
      console.log("validForm");
      if (!this.brand.name) {
        this.errors["name"] = true;
        return false;
      }
      return true;
    },
    handleClick() {
      console.log("@click");
    },
    //thirdFiles
    loadFiles() {
      //file
      let self = this;
      var param = {
        storageTypes: Common.Const.StorageType.PROJECT_IMAGE,
        // refId: this.category.id,
        // refId: this.brand.parentId
        refId: this.brand.id,
      };
      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then(function (response) {
          console.log("response: ", response);
          const _partPictureFiles =
            response.data[Common.Const.StorageType.PROJECT_IMAGE];
          if (_partPictureFiles) {
            self.partPictureFiles = _partPictureFiles;
            self.imgName = _partPictureFiles[0].fileName;
          } else {
            self.partPictureFiles = [];
          }
        });
    },
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
      if (this.thirdFiles.length == 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteFileStorage: function (file, type) {
      let self = this;
      axios.delete("/api/file-storage/" + file.id).then(function (response) {
        switch (type) {
          case Common.Const.StorageType.PROJECT_IMAGE:
            self.partPictureFiles = response.data;
            break;
        }
      });
      return true;
    },
    selectedThirdFiles: async function (e) {
      console.log("selectedThirdFiles");
      //clear old file for no multiple
      if (!this.multiple) {
        this.thirdFiles = [];
        //remove old file
        if (this.partPictureFiles.length > 0) {
          let accept = this.deleteFileStorage(
            this.partPictureFiles[0],
            Common.Const.StorageType.PROJECT_IMAGE
          );
          if (!accept) return;
        }
      }
      var files = e.target.files;
      var isExitsFile = this.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = this.partPictureFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    formData: async function () {
      let dataForm = "";
      let formData = new FormData();
      const dataBody = {
        ...this.brand,
        grandParentId:
          this.selectedParentCategory && this.isInModal
            ? this.selectedParentCategory.id || 0
            : this.brand.grandParentId || 0,
        children: [],
        enabled: true,
        level: 2,
      };
      if (this.imgDataUrl) {
        dataForm = await fetch(this.imgDataUrl).then(async (res) => {
          const blob = await res.blob();
          formData.append("thirdFiles", blob, this.imgName);
          formData.append("payload", JSON.stringify(dataBody));
          return formData;
        });
      } else {
        formData.append("payload", JSON.stringify(dataBody));
        dataForm = formData;
      }
      return dataForm;
    },
    saveData() {
      if (!this.validForm()) {
        this.$forceUpdate();
        return;
      }
      if (this.brand.id) {
        this.update();
      } else this.create();
    },

    async create() {
      let dataForm = await this.formData();
      const self = this;
      try {
        console.log("dataForm", dataForm);
        const res = await axios.post(
          "/api/categories/add-multipart",
          dataForm,
          this.multipartHeader()
        );
        console.log("res res res", res);
        if (res.data.success) {
          if (this.isInModal) {
            const newSelectedCategory = _.find(this.categoryOptionList, {
              id: this.selectedParentCategory?.id,
            });
            self.$emit(
              "on-success-create-brand",
              res.data.data,
              this.selectedCategory ? null : newSelectedCategory
            );
            self.handleClose();
          }
          Common.alertCallback = function () {
            // location.href = Page.LIST_PAGE;
            self.$emit("reload-page");
            self.handleClose();
          };
          Common.alert("success");
        } else {
          Common.alert(res.data.message);
        }
      } catch (error) {
        Common.alert(error.response.data);
      }
    },
    clearData() {
      this.categoryVisible = false;
      this.selectedParentCategory = null;
      this.categoryOptionList = [];
      this.brand = {
        name: "",
        description: "",
        division: "",
        grandParentId: "",
      };
      this.errors = [];
      this.headers = {
        smail: "*_~",
      };
      this.imgDataUrl = null;
      this.imgName = null;
      this.thirdFiles = [];
    },
    async update() {
      let dataForm = await this.formData();
      const self = this;
      try {
        const res = await axios.put(
          `/api/categories/edit-multipart/${this.brand.id}`,
          dataForm,
          this.multipartHeader()
        );
        console.log("res res res", res);
        if (res.data.success) {
          if (this.isInModal) {
            self.$emit("on-success-create-brand", res.data.data);
          }
          Common.alertCallback = function () {
            // location.href = Page.LIST_PAGE;
            self.$emit("reload-page");
          };
          Common.alert("success");
          self.handleClose();
        } else {
          Common.alert(res.data.message);
        }
      } catch (error) {
        Common.alert(error.response.data);
      }
    },
    handleRemoveSeletion(id) {
      console.log("remove", id);
    },
    toggledCategoryDropdown(value) {
      this.categoryVisible = value;
    },
    onChangeCategory(item) {
      this.selectedParentCategory = item;
      this.handleCloseCategorySelect();
    },
    handleCloseCategorySelect() {
      this.categoryVisible = false;
    },
    async loadCategoryOption() {
      try {
        const res = await axios.get(
          "/api/categories?size=1000&enabled=true&status=1"
        );
        const categoryList = res.data.content.map((c) => {
          return { ...c, title: c.name };
        });
        return categoryList;
      } catch (error) {
        console.log(error);
      }
      return [];
    },
    goToCategory() {
      this.openCategoryModal(true);
    },
  },
};
</script>

<style scoped>
.modal__footer {
  position: absolute;
  right: 0;
  bottom: 0;
  padding: 19px 32px;
}
.modal__footer__button {
  margin-left: 8px;
}
.head-line {
  left: 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title-text {
  color: rgb(75, 75, 75);
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}
.error-text {
  font-size: 11px;
  color: #ef4444;
  text-align: left;
}
.custom-modal__body {
  padding: 20px 25px !important;
  height: 606px;
}
.line-row-wrapper {
  margin-bottom: 31px;
}
.modal-footer {
  border: none;
  padding: 19px 32px;
}
</style>
