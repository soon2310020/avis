<template>
  <div>
    <div
      v-show="isLoading"
      class="modal-body__content__infor d-flex loading-wave"
    ></div>
    <div v-show="!isLoading" class="modal-body__content__infor d-flex">
      <div
        @click="viewImage(getImage(project.projectImage).status == 'EXIST')"
        class="block__image"
      >
        <img
          v-if="!loadingImage"
          :src="getImage(project.projectImage).url"
          :class="{
            'exsisted-image': getImage(project.projectImage).status == 'EXIST',
          }"
          alt=""
        />
        <div v-else class="spinner-zone">
          <a-spin>
            <a-icon
              slot="indicator"
              type="loading"
              style="font-size: 44px"
              spin
            />
          </a-spin>
        </div>
        <div class="edit-zone">
          <img
            @click.stop="showDropdown()"
            src="/images/icon/category/edit.svg"
            alt=""
          />
        </div>
        <div
          v-if="isShowDropdown"
          v-click-outside="closeDropdown"
          class="edit-dropdown"
        >
          <div @click.stop="toggleShow" class="edit-dropdown-item">
            Upload Photo
          </div>
          <div
            v-show="getImage(project.projectImage).status === 'EXIST'"
            @click.stop="showWarning()"
            class="edit-dropdown-item"
          >
            Remove
          </div>
        </div>
      </div>
      <div class="block__container block__infor">
        <div class="block__content">
          <div class="block__content__title">Number of Parts</div>
          <div
            class="content__hyper-link"
            @click="
              handleChangeRoute({
                route: 'list-part',
                data: { project: project },
              })
            "
          >
            {{
              `${project.partCount} ${project.partCount > 1 ? "Parts" : "Part"}`
            }}
            <img src="/images/icon/category/expand-arrow.svg" alt="" />
          </div>
        </div>
        <div class="block__content">
          <div class="block__content__title">Number of Toolings</div>
          <div
            class="content__hyper-link"
            @click="
              handleChangeRoute({
                route: 'list-mold',
                data: { project: project },
              })
            "
          >
            {{
              `${project.moldCount} ${
                project.moldCount > 1 ? "Toolings" : "Tooling"
              }`
            }}
            <img src="/images/icon/category/expand-arrow.svg" alt="" />
          </div>
        </div>
        <div class="block__content">
          <div class="block__content__title">Number of Suppliers</div>
          <div
            class="content__hyper-link"
            @click="
              handleChangeRoute({
                route: 'list-supplier',
                data: { project: project },
              })
            "
          >
            {{
              `${project.supplierCount} ${
                project.supplierCount > 1 ? "Suppliers" : "Supplier"
              }`
            }}
            <img src="/images/icon/category/expand-arrow.svg" alt="" />
          </div>
        </div>
      </div>
      <product-statistic
        :resources="resources"
        :project="project"
        :fields="statisticFields"
      ></product-statistic>
      <!-- <div>
        <div class="block__content">
          <div class="block__content__title">Product Name</div>
          <div class="">
            {{ project.name }}
          </div>
        </div>
        <div class="block__content">
          <div class="block__content__title">Total Produced</div>
          <div class="">
            {{ project.totalProduced }}
          </div>
        </div>
        <div class="block__content">
          <div class="block__content__title d-flex item-middle">
            Product Production Demand
            <a-tooltip placement="bottom">
              <template slot="title">
                <p>
                  {{
                    `Project Production Demand is calculated based on a manually input weekly project demand.`
                  }}
                </p>
                <p>{{'The timeline begins from the start date of production of any parts belonging to the project, until, and including the current date.'}}</p>
              </template>
                <a-icon style="margin-left: 10px" type="info-circle" />
            </a-tooltip>
          </div>
          <div class="">
            {{ appendThousandSeperator(project.productionDemand) }}
          </div>
        </div>
      </div> -->
      <delete-image
        :confirm-delete="confirmRemove"
        :is-show-warning-again="true"
        :resources="resources"
        :is-remove-image="true"
      ></delete-image>
      <vue-upload
        field="img"
        @crop-success="cropSuccess"
        @crop-upload-success="cropUploadSuccess"
        @crop-upload-fail="cropUploadFail"
        @src-file-set="getFileSet"
        @clear="removeImgUploaded"
        v-model="show"
        :width="250"
        :height="250"
        img-bgc="#2E2E2E"
        :params="params"
        :headers="headers"
        img-format="png"
      ></vue-upload>
      <view-image
        :all-images="[project.projectImage]"
        v-model="showViewImage"
      ></view-image>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    project: {
      type: Object,
      default: () => ({
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      }),
    },
    category: Object,
    loadingImage: Boolean,
    partFields: {
      type: Array,
      default: () => [],
    },
    getPagingPart: Function,
    tableData: Array,
    enableBack: Boolean,
    isLoading: Boolean,
  },
  components: {
    "data-table": httpVueLoader(
      "/components/category/category-library/project-data-table.vue"
    ),
    "delete-image": httpVueLoader("/components/delete-image.vue"),
    "view-image": httpVueLoader(
      "/components/category/category-library/view-image.vue"
    ),
    "product-statistic": httpVueLoader(
      "/components/category/category-library/product-statistic.vue"
    ),
  },
  data() {
    return {
      partData: [],
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
      imgName: null,
      // end upload variable
      isShowDropdown: false,
      statisticFields: [
        {
          title: this?.resources["product_produced"] || "Produced Quantity",
          field: "totalProduced",
          isNumber: true,
        },
        {
          title:
            this?.resources["predicted_product_quantity"] ||
            "Predicted Quantity",
          field: "predictedQuantity",
          isNumber: true,
        },
        {
          title: this?.resources["product_demand"] || "Product Demand",
          field: "totalProductionDemand",
          isNumber: true,
        },
        {
          title:
            this?.resources["product_production_capacity"] ||
            "Production Capacity",
          field: "totalMaxCapacity",
          isNumber: true,
        },
        {
          title: this?.resources["delivery_risk"] || "Delivery Risk",
          field: "deliveryRiskLevel",
          isCenter: true,
        },
        // {title: 'Product Production Demand', field: 'totalProductionDemand'},
        // {title: this.resources['weekly_product_production_capacity'], field: 'weeklyMaxCapacity'},
        // {title: this.resources['weekly_production_demand'], field: 'weeklyProductionDemand'},
      ],
      showViewImage: false
    };
  },
  computed: {
    // getCategoryName() {
    //   return this.categoryName;
    // },
  },
  methods: {
    closeView() {
      $(this.getRootId() + "#op-view-image").modal("hide");
    },
    viewImage(exist) {
      this.showViewImage = true
      // if (exist) {
      //   $("#op-view-image").modal("show");
      // }
    },
    closeDropdown() {
      this.isShowDropdown = false;
    },
    showDropdown() {
      this.isShowDropdown = true;
    },
    removeImgUploaded() {},
    async cropSuccess(imgDataUrl) {
      if (imgDataUrl) {
        let formData = new FormData();
        fetch(imgDataUrl).then(async (res) => {
          const blob = await res.blob();
          formData.append("thirdFiles", blob);
          axios
            .post(`/api/categories/upload-photo/${this.project.id}`, formData)
            .then((res) => {
              this.$emit("fetch-image", "uploaded");
            });
        });
      }
    },
    cropUploadSuccess(jsonData, field) {},
    cropUploadFail(status, field) {},
    getFileSet(name) {},
    toggleShow() {
      this.show = !this.show;
      this.closeDropdown();
    },
    showWarning() {
      var child = Common.vue.getChild(this.$children, "delete-image");
      if (child != null) {
        this.closeDropdown();
        child.showDeletePopup();
      }
    },
    confirmRemove() {
      axios
        .delete(`/api/file-storage/${this.project.projectImage.id}`)
        .then((res) => {
          this.$emit("fetch-image", "removed");
          $(this.getRootId() + "#op-delete-image").modal("hide");
        });
    },
    back() {
      this.$emit("change-step", "backward");
    },
    getImage(projectImage) {
      if (projectImage) {
        return {
          url: projectImage.saveLocation,
          status: "EXIST",
        };
      }
      return {
        url: "/images/icon/category/not-found-image.svg",
        status: "NOT_EXIST",
      };
    },
    handleChangeRoute(route) {
      this.$emit("change-route", route);
    },
    changeProject(project) {
      this.project = project;
    },
  },
  watch: {
    // project(newVal){
    //   this.statisticFields = [
    //     {title: this.resources['product_produced'], field: this.project.totalProduced },
    //     {title: this.resources['product_production_capacity'], field: this.project.totalMaxCapacity },
    //     {title: 'Product Production Demand', field: this.project.totalProductionDemand },
    //     {title: this.resources['weekly_product_production_capacity'], field: this.project.weeklyMaxCapacity },
    //     {title: this.resources['weekly_production_demand'], field: this.project.weeklyProductionDemand },
    //   ]
    // }
    project(newVal) {
      console.log("project============================", newVal);
    },
    fields(newVal) {
      console.log("fields============================", newVal);
    },
  },
};
</script>
<style scoped>
.edit-zone {
  position: absolute;
  bottom: 6px;
  right: 6px;
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
.spinner-zone {
  position: absolute;
  bottom: 50%;
  right: 50%;
  transform: translate(50%, 50%);
}
</style>
<style scoped src="/components/category/category-project-modal.css"></style>