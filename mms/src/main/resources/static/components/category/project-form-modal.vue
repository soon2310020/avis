<template>
  <div
    id="op-project-form"
    class="modal fade"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content" style="height: 662px">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">
              {{
                isEdit ? resources["edit_project"] : resources["create_project"]
              }}</span
            >
          </div>

          <div
            class="t-close-button close-button"
            @click="dimissModal"
            aria-hidden="true"
          >
            <i class="icon-close-black"></i>
          </div>
          <div class="head-line"></div>
        </div>

        <div class="d-modal-body" :style="{ 'overflow-y': 'unset' }">
          <div class="card-body" :style="{ 'overflow-x': 'unset !important' }">
            <div class="form-group row line-row-wrapper">
              <label class="col-md-2 col-form-label" for="name">
                {{ resources["project_name"] }}
                <span class="badge-require"></span>
              </label>
              <div class="col">
                <input
                  type="text"
                  id="name"
                  v-model="category.name"
                  class="form-control"
                  :class="{
                    'form-control-warning':
                      category.name == '' || category.name == null,
                  }"
                  :placeholder="resources['project_name']"
                  required
                  @keyup="removeErrors('name')"
                />
                <div
                  v-if="errors['name']"
                  class="error-text"
                  v-text="resources['error_input_project_name']"
                ></div>
              </div>
            </div>

            <div class="form-group row line-row-wrapper">
              <label class="col-md-2 col-form-label" for="description">
                {{ resources["description"] }}
              </label>

              <div class="col">
                <input
                  type="text"
                  id="description"
                  v-model="category.description"
                  class="form-control"
                  :class="{
                    'form-control-warning':
                      category.description == '' ||
                      category.description == null,
                  }"
                  :placeholder="resources['description']"
                  required
                  @keyup="removeErrors('description')"
                />
              </div>
            </div>

            <div class="form-group row line-row-wrapper">
              <label class="col-md-2 col-form-label" for="grandParentId">
                {{ resources["parent_category"] }}
              </label>

              <div class="col">
                <custom-dropdown-button
                  :title="
                    category.grandParentId === ''
                      ? 'Select Parent Category'
                      : category.grandParentName
                  "
                  :is-selected="true"
                  :is-show="selectedDropdown === 'grandParentId'"
                  @click="showCategory"
                ></custom-dropdown-button>
                <common-popover
                  :is-visible="selectedDropdown === 'grandParentId'"
                  :position="{ top: '60%', left: '15px' }"
                  @close="showCategory(false)"
                >
                  <common-select
                    :style="{ position: 'static', width: '216px' }"
                    :items="categoryOptionTruncateList || []"
                    :searchbox="categoryOptionList?.length >= 5"
                    @on-change="categorySelect"
                    :placeholder="resources['search_category']"
                  ></common-select>
                </common-popover>
                <div style="margin-top: 8px">
                  <i class="cant-find-brand-txt">Can't find category?</i>
                  <span
                    class="custom-hyper-link"
                    @click="onCreateCategory(true)"
                    style="margin-left: 13px"
                  >
                    Create Category
                    <div class="hyper-link-icon" style="margin-left: 3px"></div>
                  </span>
                </div>
              </div>
            </div>

            <div class="form-group row line-row-wrapper">
              <label class="col-md-2 col-form-label" for="parentId">
                {{ resources["brand"] }}
              </label>
              <div class="col">
                <custom-dropdown-button
                  :title="
                    category.parentId == '' ? 'Select Brand' : brand.title
                  "
                  :is-selected="true"
                  :is-show="selectedDropdown === 'parentId'"
                  @click="showBrands"
                ></custom-dropdown-button>
                <common-popover
                  :is-visible="selectedDropdown === 'parentId'"
                  :position="{ top: '60%', left: '15px' }"
                  :style="{ width: '210px' }"
                  @close="showBrands(false)"
                >
                  <common-select
                    :style="{ position: 'static', width: '100%' }"
                    :items="brandOptionTruncateList"
                    :searchbox="brandOptionList.length >= 5"
                    @on-change="onChangeBrand"
                  ></common-select>
                </common-popover>
                <div style="margin-top: 8px">
                  <i class="cant-find-brand-txt">Can't find brand?</i>
                  <span
                    class="custom-hyper-link"
                    @click="onCreateBrandFromProduct"
                    style="margin-left: 13px"
                  >
                    Create Brand
                    <div class="hyper-link-icon" style="margin-left: 3px"></div>
                  </span>
                </div>
              </div>
            </div>

            <div class="form-group row line-row-wrapper">
              <label class="col-md-2 col-form-label">
                {{ resources["project_image"] }}
              </label>
              <div class="col">
                <div class="op-upload-button-wrap">
                  <!--                  start upload-->
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
                  <!-- <vue-upload
                    v-model="show"
                    ref="uploadModalRef"
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
                  ></vue-upload> -->
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
          </div>
        </div>
        <div
          class="modal-footer text-center absolute"
          style="bottom: 0; right: 0"
        >
          <base-button
            id="createProjectId"
            :style="{ padding: '4px 8px' }"
            @click="dimissModal"
            :size="'medium'"
            :type="'cancel'"
            :level="'secondary'"
          >
            <span>Cancel</span>
          </base-button>
          <a
            @click="saveData"
            id="createCategoryId"
            href="javascript:void(0)"
            class="btn-custom btn-custom-primary"
          >
            <span>{{
              isEdit ? resources["save_changes"] : resources["create_project"]
            }}</span>
          </a>
        </div>
        <delete-image
          :confirm-delete="confirmRemove"
          :is-show-warning-again="true"
          :resources="resources"
          :is-remove-image="true"
        ></delete-image>
      </div>
    </div>
    <vue-upload
      v-model="show"
      ref="uploadModalRef"
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
  </div>
</template>

<script>
const regExNotNumber = /\D/g; // not number
const FILE_TRUNCATE = 20;
module.exports = {
  name: "ProjecFormModal",
  props: {
    resources: Object,
    categoryModalRef: {
      type: Object,
      default: () => ({}),
    },
    onCreateCategory: Function,
    onCreateBrand: {
      type: Function,
      default: () => {},
    },
    defaultSelectedBrands: {
      type: Array,
      default: () => [],
    },
    selectedCategory: {
      type: Object,
      default: () => ({ name: "", id: "" }),
    },
  },
  components: {
    "delete-image": httpVueLoader("/components/delete-image.vue"),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
  },
  data() {
    return {
      category: {
        name: "",
        description: "",
        parentId: "",
        productionDemand: "",
        enabled: true,
        children: [],
        grandParentId: "",
      },
      isTable: "",
      isEdit: false,
      errors: [],
      categoryOptionList: null,
      selectedDropdown: "",

      thirdFiles: [],
      //edit
      partPictureFiles: [],
      multiple: false,
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
      productionDemandNumber: "",
      customFieldList: [],
      brand: {
        id: "",
        title: "",
      },
      brandOptionList: [],
      noCategoryData: Object,
      brandNoCategoryList: [],
    };
  },
  computed: {
    hideSuggestCreateNewCategory() {
      return !this.categoryOptionList || this.categoryOptionList?.length > 0;
    },
    getTruncateFileName() {
      return {
        shouldTruncate: this.imgName.length > FILE_TRUNCATE,
        text: this.imgName?.slice(0, FILE_TRUNCATE) + "...",
      };
    },
    brandOptionTruncateList() {
      let _brandOptionTruncateList = [];
      this.brandOptionList.forEach((brandItem) => {
        _brandOptionTruncateList.push({
          ...brandItem,
          title: this.truncateText(brandItem.title, 20),
        });
      });
      return _brandOptionTruncateList;
    },
    categoryOptionTruncateList() {
      let _categoryOptionTruncateList = [];
      this.categoryOptionList?.forEach((categoryItem) => {
        _categoryOptionTruncateList.push({
          ...categoryItem,
          title: this.truncateText(categoryItem.title, 20),
        });
      });
      return _categoryOptionTruncateList;
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
    loadBrandNoCategoryList() {
      axios
        .get("/api/categories?size=1&enabled=true&sort=id,asc&status=1")
        .then((res) => {
          this.brandNoCategoryList = res.data.content[0].children.map((c) => {
            return { ...c, title: c.name };
          });
        });
    },
    onChangeBrand(brandResults) {
      let selectedBrand = brandResults.find((o) => o.checked);
      if (!!selectedBrand) {
        this.category = {
          ...this.category,
          parentId: selectedBrand.id,
        };
        this.brand = {
          title: selectedBrand.title,
          id: selectedBrand.id,
        };
        if (this.category.grandParentId != this.brand.parentId) {
          const brandInList = _.find(this.brandOptionList, {
            id: selectedBrand.id,
          });
          const newCategory = _.find(this.categoryOptionList, {
            id: brandInList.parentId,
          });
          this.category = {
            ...this.category,
            grandParentId: newCategory.id,
            grandParentName: newCategory.title,
          };
        }
      }
      this.showBrands(false);
    },
    // start upload
    toggleShow() {
      this.removeImgUploaded();
      this.show = !this.show;
    },
    closeUploadModal(event) {
      const wrapperEl = this.$refs.uploadModalRef.$el;
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
    showCategory(status) {
      if (status) {
        this.selectedDropdown = "grandParentId";
        console.log("run 1");
      } else {
        this.selectedDropdown = "";
        console.log("run 2");
      }
      console.log("showCategory", status, this.selectedDropdown);
    },
    showBrands(status) {
      if (status) {
        this.selectedDropdown = "parentId";
        console.log("run 1");
      } else {
        this.selectedDropdown = "";
        console.log("run 2");
      }
      console.log("showBrands", status, this.selectedDropdown);
    },
    categorySelect(results) {
      let item = _.find(results, (o) => o.checked);
      console.log("item", item);
      if (
        this.category.grandParentId &&
        this.category.grandParentId != item.id
      ) {
        this.category.parentId = "";
        this.category.parentName = "";
        this.brand = { title: "", id: "" };
      }
      this.category.grandParentId = item.id;
      this.category.grandParentName = this.truncateText(item.name, 20);
      if (item.children)
        this.brandOptionList = item.children.map((o) => ({
          ...o,
          title: o.name,
          id: o.id,
        }));
      this.selectedDropdown = "";
      this.removeErrors("grandParentId");
    },
    showModal(category, inCategoryList, grandParent) {
      console.log(category);
      let self = this;
      // Common.formatter?.appendThousandSeperator(removedComma)
      this.clearForm();
      this.loadCategoryOption();
      // this.loadBrandOption();
      this.isTable = inCategoryList;
      if (category) {
        this.category = {
          ...category,
          grandParentId: grandParent?.id,
          grandParentName: this.truncateText(grandParent?.name, 20) || "",
        };
        this.productionDemandNumber = Common.formatter?.appendThousandSeperator(
          category.weeklyProductionDemand
        );
        this.loadFiles();
      }
      this.isEdit = this.category && this.category.id != null;
      if (this.isEdit || grandParent) {
        const brandList = (grandParent.children || []).map((brand) => ({
          ...brand,
          title: brand.name,
          id: brand.id,
        }));
        this.brandOptionList = brandList;
        const parent = _.find(brandList, (o) => this.category.parentId == o.id);
        if (parent) {
          this.category.parentName = parent?.name;
          this.brand = {
            title: this.truncateText(parent?.title, 20),
            id: parent?.id,
          };
        }
      }
      this.loadBrandNoCategoryList();
      $(self.getRootId() + "#op-project-form").modal("show");
    },
    dimissModal: function () {
      this.clearForm();
      $(this.getRootId() + "#op-project-form").modal("hide");
    },
    clearForm() {
      this.category = {
        name: "",
        description: "",
        parentId: "",
        enabled: true,
        productionDemand: "",
        children: [],
        grandParentId: "",
      };
      this.productionDemandNumber = "";
      this.show = false;
      this.errors = [];
      this.thirdFiles = [];
      this.removeImgUploaded();
      this.categoryOptionList = null;
      this.brandOptionList = [];
      this.selectedDropdown = "";
    },
    loadCategoryOption() {
      axios
        .get("/api/categories?size=1000&enabled=true&sort=id,asc&status=1")
        .then((res) => {
          this.categoryOptionList = res.data.content.map((c) => {
            return { ...c, title: c.name };
          });
        });
    },
    loadBrandOption() {
      axios
        .get("/api/categories?size=1000&enabled=true&status=2")
        .then((res) => {
          this.brandOptionList = res.data.content.map((c) => {
            return { ...c, title: c.name };
          });
        });
    },
    loadFiles() {
      //file
      let self = this;
      var param = {
        storageTypes: Common.Const.StorageType.PROJECT_IMAGE,
        refId: this.category.id,
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
    //thirdFiles
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
    onCreateBrandFromProduct() {
      // const grandParentItem = this.categoryOptionList.find(
      //   (o) => o.id == this.category.grandParentId
      // );
      this.onCreateBrand();
    },
    formData: async function () {
      let dataForm = "";
      let formData = new FormData();
      const body = {
        name: this.category.name,
        description: this.category.description || "",
        enabled: true,
        grandParentId: this.category.grandParentId || 0,
        parentId: this.category.parentId || null,
        productionDemand: this.category.productionDemand,
        children: [],
        level: 3,
      };
      if (this.imgDataUrl) {
        dataForm = await fetch(this.imgDataUrl).then(async (res) => {
          const blob = await res.blob();
          formData.append("thirdFiles", blob, this.imgName);
          formData.append("payload", JSON.stringify(body));
          return formData;
        });
      } else {
        formData.append("payload", JSON.stringify(body));
        dataForm = formData;
      }
      return dataForm;
      /*
      var formData = new FormData();
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      formData.append('payload', JSON.stringify(this.category));
      return formData;
*/
    },
    saveData() {
      if (!this.validForm()) {
        this.$forceUpdate();
        return;
      }
      if (this.category.id) {
        this.update();
      } else this.create();
    },
    async create() {
      let self = this;
      let dataForm = await this.formData();
      axios
        .post("/api/categories/add-multipart", dataForm, this.multipartHeader())
        .then(function (response) {
          if (response.data.success) {
            self.dimissModal();
            if (self.isTable) {
              Common.alertCallback = function () {
                // location.href = Page.LIST_PAGE;
                self.$emit("reload-page");
              };
              Common.alert("success");
              self.$emit("on-close");
            }
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },

    async update() {
      let self = this;
      let dataForm = await this.formData();

      axios
        .put(
          "/api/categories/edit-multipart/" + this.category.id,
          dataForm,
          this.multipartHeader()
        )
        .then(async function (response) {
          console.log(response.data);

          if (response.data.success) {
            if (self.isTable) {
              self.dimissModal();
              Common.alertCallback = function () {
                // location.href = Page.LIST_PAGE;
                self.$emit("reload-page");
              };
              Common.alert("success");
              self.$emit("on-close");
            }
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    removeErrors(type) {
      this.errors[type] = false;
      this.$forceUpdate();
    },
    validForm() {
      if (!this.category.name) {
        this.errors["name"] = true;
        return false;
      }
      return true;
    },
    validateCustomField() {
      let valid = true;
      for (const index in this.customFieldList) {
        const item = this.customFieldList[index];
        if (
          item.required &&
          (item.defaultInputValue === "" || item.defaultInputValue == null)
        ) {
          this.errors["customFieldList" + index] = true;
          valid = false;
        }
      }
      return valid;
    },
    handleCreateNewCategory() {
      this.$parent.$refs.categoryModalRef.showModal();
      this.dimissModal();
    },
    preventInputNotNumber(event) {
      console.log("@preventInputNotNumber", event);
      console.log("@preventInputNotNumber:event.key", event.key);
      console.log(
        "@preventInputNotNumber:regExNotNumber.test(event.key)",
        regExNotNumber.test(event.key)
      );
      // TODO: need recheck
      if (
        regExNotNumber.test(event.key) ||
        (!regExNotNumber.test(event.key) && event.shiftKey)
      ) {
        console.log("preventDefault");
        event.preventDefault();
      }
    },
    handleChangeProductionDemand(event) {
      console.log(
        "@handleChangeProductionDemand:event.target.value",
        event.target.value
      );
      const removedComma = event.target.value.replaceAll(regExNotNumber, "");
      this.productionDemandNumber =
        Common.formatter?.appendThousandSeperator(removedComma);
    },
  },
  watch: {
    show(newVal) {
      // uploadModalShow
      if (newVal) {
        this.$refs.uploadModalRef.$el.addEventListener(
          "click",
          this.closeUploadModal
        );
      } else {
        this.$refs.uploadModalRef.$el.removeEventListener(
          "click",
          this.closeUploadModal
        );
      }
    },
    productionDemandNumber(newVal) {
      console.log("@productionDemandNumber", newVal);
      this.category.productionDemand = newVal.replaceAll(regExNotNumber, "");
    },
    defaultSelectedBrands(newValue) {
      let currentList = [...this.brandOptionList];
      const newDifferentValue = _.last(
        newValue.filter((o) => !_.find(currentList, { id: o.id }))
      );
      if (newDifferentValue) {
        currentList = currentList.concat([
          {
            id: newDifferentValue.id,
            title: newDifferentValue.name,
            checked: true,
          },
        ]);
        this.brand = {
          id: newDifferentValue?.id,
          title: newDifferentValue.name,
        };
        this.category.parentId = newDifferentValue?.id;
        this.brandOptionList = currentList;
      }
    },
    selectedCategory(newValue) {
      if (newValue?.name) {
        console.log("newValue selectedCategory", newValue);
        if (
          this.category.grandParentId !== null &&
          this.category.grandParentId != newValue.id
        ) {
          this.category.parentId = "";
          this.category.parentName = "";
          this.brand = { title: "", id: "" };
        }
        this.category.grandParentId = newValue?.id;
        this.category.grandParentName = newValue?.name;
        this.categoryOptionList.push({ ...newValue, title: newValue.name });
        if (newValue.children.length) {
          this.brandOptionList = newValue.children.map((o) => ({
            ...o,
            title: o.name,
            id: o.id,
          }));
        } else {
          this.brandOptionList = this.brandNoCategoryList;
        }
      }
    },
  },
};
</script>
<style scoped src="/css/data-modal-style.css" cus-no-cache></style>

<style scoped>
/*@import '/css/data-modal-style.css';*/

.head-line {
  left: 0;
}
.modal-footer {
  border: none;
  padding: 19px 32px;
}
.error-text {
  font-size: 11px;
  color: #ef4444;
  text-align: left;
}
.custom-modal-title .modal-title {
  margin-left: 25px;
  display: inline-flex;
  align-items: center;
}
.image-name-zone {
  color: #3491ff;
  margin-left: 15px;
}
.image-name-zone img {
  cursor: pointer;
}
.truncate {
  width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}

.form-check-tooltip {
  display: flex;
  align-items: center;
}

.form-check-tooltip span {
  padding-left: 8px;
  padding-right: 8px;
}
.warning-txt {
  color: #ef4444;
}

.btn-custom.btn-custom-primary {
  color: #3491ff;
}

.icon-duplicate {
  display: inline-block;
  mask-image: url(/images/icon/duplicate.svg);
  mask-size: auto;
  mask-position: center center;
  mask-repeat: no-repeat;
  -webkit-mask-image: url(/images/icon/duplicate.svg);
  -webkit-mask-size: auto;
  -webkit-mask-position: center center;
  -webkit-mask-repeat: no-repeat;
  width: 10px;
  height: 10px;
  margin-left: 4px;
  background: #3491ff;
  vertical-align: super;
}

.btn-custom.btn-custom-primary:hover {
  color: #3491ff;
}

.btn-custom.btn-custom-primary:hover .icon-duplicate {
  background: #3585e5;
}
.icon-button {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  padding: 6px 8px;
  border: none;
  border-radius: 3px;
  background-color: transparent;
  transition: all 300ms ease;
}

.icon-button:hover,
.icon-button:focus {
  background-color: #daeeff;
  box-shadow: 0px 1px 1px #00000029;
  outline: none;
}

.icon-button:disabled {
  background-color: transparent;
}

.icon-button:hover .icon {
  box-shadow: 0 1px 1px #00000029;
  background-color: #3585e5;
}

.icon-button:disabled .icon {
  background-color: #c4c4c4;
}
.buttons-confirm {
  display: flex;
  gap: 8px;
}
.buttons-confirm > button:disabled {
  cursor: url("/images/icon/no-clicking-icon.svg"), auto !important;
}

.buttons-confirm > button:disabled:hover {
  box-shadow: none;
}
.buttons-confirm button:focus {
  outline: none;
  border: none;
  background-color: #daeeff;
}
button > .icon.icon-remove {
  mask-image: url("/images/icon/awesome-trash.svg");
  -webkit-mask-image: url("/images/icon/awesome-trash.svg");
}
button > .icon {
  display: inline-block;
  width: 15px;
  padding-bottom: 100%;
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
  background-color: #595959;
}
.cant-find-brand-txt {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 18px;
  color: #4b4b4b;
}
.create-brand-txt {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #3491ff;
  cursor: pointer;
  margin-left: 13px;
  margin-right: 3px;
  position: relative;
}
.line-row-wrapper {
  margin-bottom: 31px;
}
</style>
