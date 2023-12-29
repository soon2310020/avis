<template>
  <div
    id="op-category-form-modal"
    class="modal fade"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
    :class="{ 'vue-image-crop-upload': isInModal }"
    :style="getStyle"
  >
    <div
      class="modal-dialog modal-lg"
      role="document"
      :style="
        isInModal
          ? { width: '607px', height: '401px', marginTop: '140px' }
          : { height: '662px' }
      "
    >
      <div class="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">
              {{
                isEdit
                  ? resources["edit_category"]
                  : resources["create_category"]
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

        <div
          class="d-modal-body"
          style="height: 511px; position: relative"
          :style="isInModal ? { height: '100%' } : {}"
        >
          <div class="card-body" style="height: 100%">
            <div class="form-group row line-row-wrapper">
              <label
                class="col-form-label"
                for="name"
                :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
              >
                {{ resources["category_name"] }}
                <span class="badge-require"></span>
              </label>
              <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
                <input
                  type="text"
                  id="name"
                  v-model="category.name"
                  class="form-control"
                  :class="{
                    'form-control-warning':
                      category.name == '' || category.name == null,
                  }"
                  :placeholder="resources['category_name']"
                  required
                  @keyup="removeErrors('name')"
                />
                <div
                  v-if="errors['name']"
                  class="error-text"
                  v-text="resources['error_input_category_name']"
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
                <div
                  v-if="errors['description']"
                  class="error-text"
                  v-text="resources['error_input_category_desc']"
                ></div>
              </div>
            </div>

            <div class="form-group row line-row-wrapper">
              <label
                class="col-form-label"
                for="description"
                :class="{ 'col-md-3': isInModal, 'col-md-2': !isInModal }"
              >
                {{ resources["brand"] }}
              </label>

              <div :class="{ 'col-md-9': isInModal, 'col-md-10': !isInModal }">
                <div class="brand-wrapper-line">
                  <!-- <a-button
                    style="margin-right: 10px; background: white; width: 126px"
                    class="dropdown_button"
                    @click="toggledBrandDropdown"
                  >
                    <span class="truncate">Select Brands</span>
                    <a-icon type="caret-down" />
                  </a-button> -->
                  <div style="margin-right: 8px">
                    <custom-dropdown-button
                      :title="'Select Brand'"
                      :is-selected="true"
                      :is-show="brandVisible"
                      @click="toggledBrandDropdown"
                    ></custom-dropdown-button>

                    <common-popover
                      @close="handleCloseDropdown"
                      :is-visible="brandVisible"
                      :style="{
                        width: '210px',
                        position: 'fixed',
                        marginTop: '4px',
                      }"
                    >
                      <common-select
                        :style="{ position: 'static', width: '100%' }"
                        :items="brandList"
                        :searchbox="brandList.length >= 5"
                        :multiple="true"
                        :has-toggled-all="true"
                        :placeholder="'Search brand name'"
                        :visible="brandVisible"
                        @on-change="onChangeBrand"
                      ></common-select>
                    </common-popover>
                  </div>
                  <selection-card
                    v-for="(brand, index) in getSelectedBrands"
                    :key="index"
                    :selection="brand"
                    @close="onRemoveBrand"
                  >
                  </selection-card>
                  <!-- <div
                    class="d-flex brand-wrapper__card"
                    :class="{
                      'brand-wrapper__card__large': !isInModal,
                      'brand-wrapper__card__mini': isInModal,
                    }"
                  >
                    <selection-card
                      v-for="(brand, index) in getSelectedBrands"
                      :key="index"
                      :selection="brand"
                      @close="onRemoveBrand"
                    >
                    </selection-card>
                  </div> -->
                </div>

                <div style="margin-top: 8px" v-if="!isInModal">
                  <i class="cant-find-brand-txt">Can't find brand?</i>
                  <span
                    class="custom-hyper-link"
                    style="margin-left: 13px"
                    @click="onCreateBrandFromCategory"
                  >
                    Create Brand
                    <div class="hyper-link-icon" style="margin-left: 3px"></div>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer text-center">
          <base-button
            :style="{ padding: '4px 8px' }"
            @click="dimissModal"
            :size="'medium'"
            :type="'cancel'"
            :level="'secondary'"
          >
            <span>{{ resources["cancel"] }}</span>
          </base-button>
          <a
            @click="saveData"
            id="createCategoryId"
            href="javascript:void(0)"
            class="btn-custom btn-custom-primary"
          >
            <span>{{
              isEdit ? resources["save_changes"] : resources["create_category"]
            }}</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    onCreateBrand: {
      type: Function,
      default: () => {},
    },
    defaultSelectedBrands: {
      type: Array,
      default: () => [],
    },
    noCategoryData: {
      type: Object,
      default: () => {},
    },
  },
  components: {
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "selection-card": httpVueLoader(
      "/components/@base/select/selection-card.vue"
    ),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
  },
  data() {
    return {
      category: {
        name: "",
        description: "",
        parentId: "",
        enabled: true,
        children: [],
      },
      isTable: "",
      isEdit: false,
      errors: [],
      customFieldList: [],
      brandList: [],
      brandVisible: false,
      isInModal: false,
      rawBrandList: [],
    };
  },
  computed: {
    getSelectedBrands() {
      return _.filter(this.brandList, (o) => o.checked);
    },
    categoryBody() {
      return {
        name: this.category.name,
        description: this.category.description,
        childIds: this.getSelectedBrands.map((brand) => brand.id),
        enabled: true,
        level: 1,
      };
    },
    getBrandList() {
      console.log("this.noCategoryData", this.noCategoryData);
      if (this.category.id && !_.isEmpty(this.noCategoryData)) {
        const noParentBrand = this.noCategoryData.children
          .filter((child) => child.id !== null)
          .map((child) => ({
            id: child.id,
            title: child.name,
            checked: false,
          }));
        return [...this.brandList, ...noParentBrand];
      }
      return [...this.brandList];
    },
    getStyle() {
      if (this.isInModal) {
        return { "z-index": "5001" };
      } else {
        return {};
      }
    },
  },
  watch: {
    defaultSelectedBrands(newValue) {
      let currentList = [...this.brandList];
      const newDifferentValue = newValue.filter(
        (o) => !_.find(currentList, { id: o.id })
      );
      if (newDifferentValue?.length) {
        console.log("change defaultSelectedBrands");
        currentList = currentList.concat(
          _.map(newDifferentValue, (o) => ({
            id: o.id,
            title: o.name,
            checked: true,
          }))
        );
        this.brandList = currentList;
      }
    },
  },
  methods: {
    toggledBrandDropdown(value) {
      this.brandVisible = value;
    },
    handleCloseDropdown() {
      this.toggledBrandDropdown(false);
    },
    onChangeBrand(results) {
      console.log("onChangeBrand", results, this.brandList);
      this.brandList = [...results];
      console.log("onChangeBrand after", this.brandList);
    },
    onRemoveBrand(brand) {
      this.brandList = this.brandList.map((item) =>
        item.id == brand.id ? { ...item, checked: false } : item
      );
    },
    onCreateBrandFromCategory() {
      this.onCreateBrand({ ...this.category, id: this.category.id || 0 });
    },
    showModal(category, inCategoryList, isInModal) {
      let self = this;
      this.clearForm();
      this.isInModal = isInModal;
      this.isTable = inCategoryList;
      if (category) {
        this.category = { ...category };
      }
      this.isEdit =
        this.category && this.category.id != null && this.category.id != 0;
      if (this.isEdit || this.category.id == 0) {
        this.brandList = category.children
          .filter((child) => child.id !== null)
          .map((child) => ({
            id: child.id,
            title: child.name,
            checked: this.isEdit,
          }));
        this.rawBrandList = [...this.brandList];
        if (this.category.id && !_.isEmpty(this.noCategoryData)) {
          const noParentBrand = this.noCategoryData.children
            .filter((child) => child.id !== null)
            .map((child) => ({
              id: child.id,
              title: child.name,
              checked: false,
            }));
          this.brandList = [...this.brandList, ...noParentBrand];
        }
      }
      if (this.category.id == 0) {
        this.category.name = "";
        this.category.description = "";
      }

      $(self.getRootId() + "#op-category-form-modal").modal("show");
    },
    dimissModal: function () {
      this.brandVisible = false;
      $(this.getRootId() + "#op-category-form-modal").modal("hide");
    },
    clearForm() {
      this.category = {
        name: "",
        description: "",
        parentId: "",
        enabled: true,
        children: [],
      };
      this.errors = [];
      this.brandList = [];
      this.brandVisible = false;
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
    create() {
      let self = this;
      axios
        .post(`/api/categories`, this.categoryBody)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            self.dimissModal();
            if (self.isInModal) {
              self.$emit("on-success-create-category", response.data.data);
              self.clearForm();
            } else {
              self.$emit("reset-props");
            }
            if (self.isTable) {
              Common.alertCallback = function () {
                self.$emit("reload-page");
              };
              Common.alert("success");
            }
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    getBrandUpdateBody(brandId) {
      const selectedItem = _.find(this.category.children, { id: brandId });
      console.log(
        "getBrandUpdateBody",
        brandId,
        selectedItem,
        this.category.children
      );
      if (selectedItem) {
        const formData = new FormData();
        const bodyBrand = {
          name: selectedItem.name,
          description: selectedItem.description,
          parentId: null,
          grandParentId: 0,
          division: selectedItem.division,
          enabled: true,
          productionDemand: selectedItem?.productionDemand,
          children: selectedItem.children,
          level: 2,
        };
        formData.append("payload", JSON.stringify(bodyBrand));
        return formData;
      }
      return null;
    },
    update() {
      let self = this;
      const body = {
        name: this.category.name,
        description: this.category.description,
        childIds: this.getSelectedBrands.map((brand) => brand.id),
        enabled: true,
      };
      const unCheckedBrandIds = (
        this.brandList.filter((brand) => !brand.checked) || []
      ).map((brand) => brand.id);
      if (unCheckedBrandIds?.length) {
        axios.all(
          unCheckedBrandIds.map((item) => {
            const body = this.getBrandUpdateBody(item);
            if (body) {
              return axios.put(`/api/categories/edit-multipart/${item}`, body);
            }
          })
        );
      }
      axios
        .put(`/api/categories/${this.category.id}`, body)
        .then(function (response) {
          if (response.data.success) {
            self.dimissModal();

            if (self.isTable) {
              Common.alertCallback = function () {
                self.$emit("reload-page");
                if (!this.isInModal) {
                  self.$emit("reset-props");
                  this.clearForm();
                }
              };
              Common.alert("success");
            }
          } else {
            self.dimissModal();
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
      console.log("validForm");
      if (!this.category.name) {
        this.errors["name"] = true;
        return false;
      }
      if (!this.validateCustomField()) {
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
  },
  created() {},
  mounted() {},
};
</script>
<style scoped src="/css/data-modal-style.css" cus-no-cache></style>
<style scoped>
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
.brand-wrapper-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  min-height: 33px;
}
.brand-item {
  background: #deedff;
  border-radius: 3px;
  padding: 4px 8px;
  cursor: pointer;
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  margin-right: 8px;
}
.remove-icon {
  opacity: 0.7;
  width: 6px;
  height: 6px;
  margin-left: 5px;
}
.brand-wrapper__card {
  margin-left: 15px;
  overflow-x: auto;
}
.brand-wrapper__card__large {
  max-width: 620px;
}
.brand-wrapper__card__mini {
  max-width: 295px;
}
.brand-wrapper__card::-webkit-scrollbar {
  width: 1px;
  height: 5px;
}
.line-row-wrapper {
  margin-bottom: 31px;
}
</style>
