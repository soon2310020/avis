<template>
  <div>
    <div
      id="category-library-modal"
      class="modal fade"
      role="dialog"
      style="overflow-y: scroll"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content" ref="modal-content" style="height: 784px">
          <div class="custom-modal-title">
            <div class="modal-title">
              <span class="modal-title-text">{{ getModalTitle }}</span>
            </div>
            <div
              class="t-close-button close-button"
              @click="dimissModal"
              aria-hidden="true"
            >
              <img src="/images/icon/category/close.svg" alt="" />
            </div>
            <div class="head-line"></div>
          </div>
          <div
            :style="[
              {
                'overflow-x': [
                  'mold-detail',
                  'part-detail',
                  'company-detail',
                ].includes(getCurrentStep)
                  ? 'auto'
                  : 'unset',
              },
              'max-height: 80vh',
              'overflow-y: auto',
            ]"
            class="modal-body"
          >
            <div class="modal-body__header d-flex">
              <div class="d-flex" style="align-items: center">
                <div
                  v-if="enableBackProfile"
                  class="back-icon d-flex"
                  style="align-items: center; margin: 0 0 0 5px"
                  @click="changeStep('backward')"
                >
                  <span class="icon back-arrow"></span>
                  <div
                    class="modal-body__header__infor d-flex"
                    v-if="
                      ['part-detail', 'company-detail', 'mold-detail'].includes(
                        getCurrentStep
                      )
                    "
                    style="cursor: pointer"
                  >
                    <div class="modal-body__title">
                      <span>
                        <label
                          class="content__hyper-link"
                          style="margin: 0 0 0 10px; line-height: 38px"
                        >
                          {{ getBackTitle }}
                        </label>
                      </span>
                    </div>
                  </div>
                </div>
                <div
                  v-if="!enableBackProfile && getCurrentStep != 'index'"
                  class="back-icon"
                  style="cursor: unset"
                >
                  <span class="icon back-arrow-disable"></span>
                </div>

                <div
                  v-if="
                    !['part-detail', 'company-detail', 'mold-detail'].includes(
                      getCurrentStep
                    )
                  "
                  class="modal-body__header__infor d-flex space-between"
                  :class="{
                    'd-flex space-between': [
                      'list-part',
                      'list-mold',
                      'list-supplier',
                    ].includes(getCurrentStep),
                  }"
                >
                  <div class="modal-body__title" style="margin-right: 15px">
                    <div
                      v-if="category.name"
                      class="title__name title-name-header"
                    >
                      Category
                    </div>
                    <span
                      @click="handleClickCategoryName"
                      class="background-name-header-wrapper"
                    >
                      <a-tooltip v-if="shouldShowTooltip" placement="bottom">
                        <template slot="title">
                          <span
                            style="
                              display: inline-block;
                              padding: 6px 8px;
                              word-break: break-word;
                            "
                            >{{ category.name }}</span
                          >
                        </template>
                        <label
                          :class="{
                            'content__hyper-link': getCurrentStep != 'index',
                          }"
                          class="truncate-text"
                        >
                          {{ truncatedCategoryName }}
                        </label>
                      </a-tooltip>
                      <label
                        v-else
                        class="truncate-text"
                        :class="{
                          'content__hyper-link': getCurrentStep != 'index',
                        }"
                      >
                        {{ category.name }}
                      </label>
                    </span>
                  </div>
                  <div
                    v-if="getBrandTitle"
                    class="modal-body__title"
                    style="margin-right: 15px"
                  >
                    <div class="title__name title-name-header">Brand</div>
                    <span
                      class="background-name-header-wrapper"
                      @click="handleClickBrandName"
                    >
                      <a-tooltip
                        v-if="shouldShowTooltipForBrand"
                        placement="bottom"
                      >
                        <template slot="title">
                          <span
                            style="
                              display: inline-block;
                              padding: 6px 8px;
                              word-break: break-word;
                            "
                            >{{ getBrandTitle }}</span
                          >
                        </template>
                        <label
                          :class="{
                            'content__hyper-link': ![
                              'index',
                              'brand-list',
                            ].includes(getCurrentStep),
                          }"
                          class="truncate-text"
                          >{{ truncatedBrandName }}</label
                        >
                      </a-tooltip>
                      <label
                        v-else
                        :class="{
                          'content__hyper-link': ![
                            'index',
                            'brand-list',
                          ].includes(getCurrentStep),
                        }"
                        class="truncate-text"
                      >
                        {{ getBrandTitle }}
                      </label>
                    </span>
                  </div>
                  <div
                    v-if="getProjectTitle"
                    class="modal-body__title"
                    style="margin-right: 15px"
                  >
                    <div class="title__name title-name-header">Product</div>
                    <span class="background-name-header-wrapper">
                      <a-tooltip
                        v-if="shouldShowTooltipForProduct"
                        placement="bottom"
                      >
                        <template slot="title">
                          <span
                            style="
                              display: inline-block;
                              padding: 6px 8px;
                              word-break: break-word;
                            "
                            >{{ getProjectTitle }}</span
                          >
                        </template>
                        <label class="truncate-text">{{
                          truncatedProductName
                        }}</label>
                      </a-tooltip>
                      <label v-else class="truncate-text">
                        {{ getProjectTitle }}
                      </label>
                    </span>
                  </div>
                </div>
              </div>
              <div
                :class="{
                  'd-flex': getCurrentStep == 'project-profile',
                  'd-none': getCurrentStep !== 'project-profile',
                }"
                style="align-items: center"
              >
                <div>
                  <week-picker-popup
                    @on-close="handleChangeDate"
                    :current-date="profileDate"
                    :date-range="timeRange"
                    :button-type="'secondary'"
                  ></week-picker-popup>
                </div>
              </div>
            </div>
            <table-path
              v-show="
                ['list-part', 'list-mold', 'list-supplier'].includes(
                  getCurrentStep
                )
              "
              :step-list="step"
              :category="category"
              :brand="selectedBrand"
              :project="selectedProject"
              @change-path="handleChangeBreadcrumb"
            ></table-path>
            <div class="modal-body__container">
              <div class="modal-body__content">
                <brand-overview
                  v-show="getCurrentStep == 'index'"
                  :resources="resources"
                  :category="category"
                  :is-loading="isLoadingOverView"
                  :handle-choose-brand="handleChooseBrand"
                  @change-route="handleChangeRoute"
                ></brand-overview>
                <category-library-overview
                  v-show="getCurrentStep == 'brand-list'"
                  :resources="resources"
                  :brand="selectedBrand"
                  :is-loading="isLoadingBrand"
                  :handle-choose-project="handleChooseProject"
                  @change-route="handleChangeRoute"
                ></category-library-overview>
                <project-profile
                  v-show="getCurrentStep == 'project-profile'"
                  :resources="resources"
                  :project="selectedProject"
                  @change-step="changeStep"
                  @fetch-image="fetchImage"
                  :loading-image="loadingImage"
                  @change-route="handleChangeRoute"
                  :category="category"
                  :part-fields="partFields"
                  :get-paging-part="getPagingPart"
                  :table-data="projectPart"
                  :is-loading="isLoadingProfile"
                ></project-profile>
                <data-table
                  v-show="
                    [
                      'project-profile',
                      'list-part',
                      'list-mold',
                      'list-supplier',
                    ].includes(getCurrentStep)
                  "
                  :type="currentTableType"
                  :resources="resources"
                  :project="selectedProject"
                  :brand="selectedBrand"
                  :category="category"
                  :page-size="tableSize"
                  :current-page="this.step[this.step.length - 1].data.page"
                  :data-list="currentTableData"
                  :field-list="currentListField"
                  :total-page="totalPage"
                  :is-loading="isLoadingTable"
                  :step="step"
                  @change-route="handleChangeRoute"
                  @change-page="handlePaging"
                  :set-route-name="setRouteName"
                ></data-table>
                <part-detail-popup
                  :resources="resources"
                  :part="dataDetail"
                  v-if="dataDetail && getCurrentStep == 'part-detail'"
                  :show-file-previewer="showFilePreviewer"
                  :style-custom="{
                    padding: '0',
                    'margin-top': '15px',
                    'max-height': '80vh',
                    overflow: 'hidden',
                  }"
                >
                </part-detail-popup>
                <tooling-detail
                  :show-file-previewer="showFilePreviewer"
                  :resources="resources"
                  :mold="dataDetail"
                  v-if="dataDetail && getCurrentStep == 'mold-detail'"
                  :style-custom="{
                    padding: '0',
                    'margin-top': '15px',
                    'max-height': '57vh',
                  }"
                ></tooling-detail>
                <company-details-data
                  :company="dataDetail"
                  :is-doash-board="false"
                  :resources="resources"
                  v-if="dataDetail && getCurrentStep == 'company-detail'"
                  :style-custom="{ padding: '0', 'margin-top': '15px' }"
                ></company-details-data>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <toast-alert
      @close-toast="closeSuccessToast"
      :show="successToast"
      :title="getToastAlert.title"
      :content="getToastAlert.content"
    ></toast-alert>
  </div>
</template>

<script>
const LARGE_SIZE = 14;
const MINI_SIZE = 6;
const TRUNCATED_SIZE_M = 7;
const TRUNCATED_SIZE_L = 15;
const RISK_LEVEL = {
  LOW: "Low Risk",
  MEDIUM: "Medium Risk",
  HIGH: "High Risk",
};
module.exports = {
  props: {
    resources: Object,
    showFilePreviewer: Function,
    currentDate: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      }),
    },
    triggerSaveModal: Number,
    tab: String,
  },
  components: {
    "category-library-overview": httpVueLoader(
      "/components/category/category-library/overview.vue"
    ),
    "project-profile": httpVueLoader(
      "/components/category/category-library/project-profile.vue"
    ),
    "brand-overview": httpVueLoader(
      "/components/category/category-library/brand-overview.vue"
    ),
    "data-table": httpVueLoader(
      "/components/category/category-library/project-data-table.vue"
    ),
    "part-detail-popup": httpVueLoader("/components/part-detail-popup.vue"),
    "success-toast": httpVueLoader(
      "/components/category/category-library/success-toast.vue"
    ),
    "table-path": httpVueLoader(
      "/components/category/category-library/table-path.vue"
    ),
    "tooling-detail": httpVueLoader("/components/tooling-detail.vue"),
    "company-details-data": httpVueLoader(
      "/components/company/company-details-data.vue"
    ),
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
    "week-picker-popup": httpVueLoader(
      "/components/category/week-picker-popup.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
  },
  data() {
    return {
      category: {
        id: null,
        name: "",
        projectProfiles: [],
      },
      selectedProject: {
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      },
      selectedBrand: {
        id: null,
        molds: [],
        name: "",
        parts: [],
        products: [],
        brandImage: null,
        suppliers: [],
        totalProduced: 0,
      },
      step: [{ route: "index", data: {} }],
      partFields: [
        {
          title: "Part",
          field: "name",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
        },
        {
          title: "Number of Suppliers",
          field: "supplierCount",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
          isNumber: true,
        },
        {
          title: "Number of Toolings",
          field: "moldCount",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
          isNumber: true,
        },
        {
          title: "Required Quantity",
          field: "quantityRequired",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Produced Quantity",
          field: "totalProduced",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          formatter: Common.formatter.appendThousandSeperator,
          isNumber: true,
        },
        {
          title: "Predicted Quantity",
          field: "predictedQuantity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Production Demand",
          field: "totalProductionDemand",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Production Capacity",
          field: "totalMaxCapacity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Delivery Risk",
          field: "deliveryRiskLevel",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isCenter: true,
          formatter: (val) => RISK_LEVEL[val],
        },
      ],
      moldFields: [
        {
          title: "Tooling",
          field: "equipmentCode",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
        },
        {
          title: "Status",
          field: "equipmentStatus",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isCenter: true,
        },
        {
          title: "Utilization Rate",
          field: "utilizationRate",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isPercent: true,
          isNumber: true,
        },
        {
          title: "Late Cycle Time",
          field: "lastCycleTime",
          isHyperLink: false,
          numberField: "sec",
          isArrayField: false,
          isNumber: true,
        },

        {
          title: "Produced Quantity",
          field: "totalProduced",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Predicted Quantity",
          field: "predictedQuantity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Production Capacity",
          field: "totalMaxCapacity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
      ],
      suppliersfields: [
        {
          title: "Supplier",
          field: "name",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
        },
        {
          title: "Number of Parts",
          field: "partCount",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
          isNumber: true,
        },
        {
          title: "Number of Toolings",
          field: "moldCount",
          isHyperLink: true,
          numberField: null,
          isArrayField: false,
          isNumber: true,
        },
        {
          title: "Produced Quantity",
          field: "totalProduced",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Predicted Quantity",
          field: "predictedQuantity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
        {
          title: "Production Capacity",
          field: "totalMaxCapacity",
          isHyperLink: false,
          numberField: null,
          isArrayField: false,
          isNumber: true,
          formatter: Common.formatter.appendThousandSeperator,
        },
      ],
      projectPart: [],
      tablePage: {
        "project-profile": {
          page: 1,
          projectId: "",
        },
        "list-part": {
          page: 1,
          projectId: "",
          moldId: null,
          supplierId: null,
        },
        "list-mold": {
          page: 1,
          projectId: "",
          partId: null,
          supplierId: null,
        },
        "list-supplier": {
          page: 1,
          projectId: "",
          moldId: null,
          partId: null,
        },
      },
      currentTableData: [],
      currentListField: [],
      currentTableType: "",
      loadingImage: false,
      enableBackProfile: false,
      tableSize: 6,
      part: null,
      successToast: false,
      successToastType: "uploaded",
      dataDetail: null,
      currentPage: 1,
      totalPage: 1,
      isLoadingOverView: false,
      isLoadingTable: true,
      isLoadingProfile: true,
      profileDate: this.currentDate,
      requestParam: {},
      timeRange: {
        minDate: null,
        maxDate: new Date("2100-01-01"),
      },
      isLoadingBrand: false,
      toastAlert: {
        title: "Success!",
        content: "",
      },
    };
  },
  computed: {
    getToastAlert() {
      let result = { ...this.toastAlert };
      result.content = `Your photo has been ${this.successToastType} successfully.`;
      return result;
    },
    getCurrentStep() {
      const currentStep = [...this.step].pop();
      console.log("getCurrentStep", currentStep);
      return currentStep.route;
    },
    getProjectTitle() {
      console.log("@getProjectTitle::::::", this.step, this.selectedBrand);
      const currentStep = this.step[this.step.length - 1];
      const prevStep = this.step[this.step.length - 2];
      if (this.getCurrentStep === "brand-list") {
        const count = this.selectedBrand?.childProfiles?.length;
        console.log("into brand-list", count);
        const label = count > 1 ? " Products" : " Product";
        return count + label;
      }
      if (prevStep?.route !== "index" && currentStep.route !== "index") {
        return this.selectedProject?.name;
      }
      if (currentStep.route === "project-profile") {
        return this.selectedProject?.name;
      }
      return;
    },
    getBrandTitle() {
      const lastStep = [...this.step].pop();
      console.log("@getBrandTitle::::::", this.step, this.category);
      if (this.getCurrentStep === "index") {
        const count =
          this.category?.projectProfiles?.length ||
          this.category?.childProfiles?.length;
        const label = count > 1 ? " Brands" : " Brand";
        return count + label;
      } else if (lastStep?.route && lastStep?.route != "index") {
        return this.selectedBrand?.name || "No brand";
      }
      return;
    },
    getCurrentPage() {
      let currentStep = this.step[this.step.length - 1];
      console.log("getCurrentPage", this.step);
      return currentStep.data.page ? currentStep.data.page : 1;
    },
    getModalTitle() {
      const beforePrevStep = this.step[this.step.length - 3] || null;
      const prevStep = this.step[this.step.length - 2] || null;
      const currentStep = this.step[this.step.length - 1];
      console.log("this.step", this.step);
      switch (currentStep.route) {
        case "index":
          return "Category Library";
        case "brand-list":
          return `Brand Profile - ${this.getBrandTitle}`;
        case "project-profile":
          return `Product Profile - ${this.getProjectTitle}`;
        // List
        case "list-part":
          66;
          if (prevStep?.route === "index") return "Brand Profile - Parts";
          else return "Product Profile - Parts";
        case "list-mold":
          if (prevStep?.route === "index") return "Brand Profile - Toolings";
          else return "Product Profile - Toolings";
        case "list-supplier":
          if (prevStep?.route === "index") return "Brand Profile - Suppliers";
          else return "Product Profile - Suppliers";
        // Detail
        case "part-detail":
          if (beforePrevStep?.route === "index")
            return `Brand Profile - ${
              this.dataDetail ? this.dataDetail.name : ""
            } Information`;
          return `Product Profile - ${
            this.dataDetail ? this.dataDetail.name : ""
          } Information`;
        case "mold-detail":
          if (beforePrevStep?.route === "index")
            return `Brand Profile - ${
              this.dataDetail ? this.dataDetail.equipmentCode : ""
            } Information`;
          return `Product Profile - ${
            this.dataDetail ? this.dataDetail.equipmentCode : ""
          } Information`;
        case "company-detail":
          if (beforePrevStep?.route === "index")
            return `Brand Profile - ${
              this.dataDetail ? this.dataDetail.name : ""
            } Information`;
          return `Product Profile - ${
            this.dataDetail ? this.dataDetail.name : ""
          } Information`;
      }
    },
    getBackTitle() {
      if (this.step.length <= 1) return "";
      let backStep = this.step[this.step.length - 2];
      switch (backStep.route) {
        case "index":
          return "Back to Category Library";
        case "project-profile":
          return `Back to Product Profile`;
        case "list-part":
          return `Back to Part List`;
        case "list-mold":
          return `Back to Tooling List`;
        case "list-supplier":
          return `Back to Supplier List`;
        case "part-detail":
          return `Back to Part Information`;
        case "mold-detail":
          return `Back to Tooling Information`;
        case "company-detail":
          return `Product Profile Supplier Information`;
      }
    },
    shouldShowTooltip() {
      if (this.getCurrentStep == "project-profile") {
        return this.category.name.length > TRUNCATED_SIZE_M;
      }
      return this.category.name.length > TRUNCATED_SIZE_L;
    },
    shouldShowTooltipForBrand() {
      if (this.getCurrentStep == "project-profile") {
        return this.getBrandTitle.length > TRUNCATED_SIZE_M;
      }
      return this.getBrandTitle.length > TRUNCATED_SIZE_L;
    },
    shouldShowTooltipForProduct() {
      if (this.getCurrentStep == "project-profile") {
        return this.getProjectTitle.length > TRUNCATED_SIZE_M;
      }
      return this.getProjectTitle.length > TRUNCATED_SIZE_L;
    },
    shouldTitleHyperlinked() {
      return this.currentStep?.route?.includes("project-profile");
    },
    truncatedCategoryName() {
      if (this.getCurrentStep == "project-profile") {
        return this.category?.name?.slice(0, TRUNCATED_SIZE_M) + "...";
      }
      return this.category?.name?.slice(0, TRUNCATED_SIZE_L) + "...";
    },
    truncatedBrandName() {
      if (this.getCurrentStep == "project-profile") {
        return this.getBrandTitle?.slice(0, TRUNCATED_SIZE_M) + "...";
      }
      return this.getBrandTitle?.slice(0, TRUNCATED_SIZE_L) + "...";
    },
    truncatedProductName() {
      if (this.getCurrentStep == "project-profile") {
        return this.getProjectTitle?.slice(0, TRUNCATED_SIZE_M) + "...";
      }
      return this.getProjectTitle?.slice(0, TRUNCATED_SIZE_L) + "...";
    },
    tabStatus() {
      return this.tab === "enabled" ? "true" : "false";
    },
  },
  watch: {
    step(newValue) {
      let newRoute = newValue[newValue.length - 1];

      if (
        ["project-profile", "list-part", "list-mold", "list-supplier"].includes(
          newRoute.route
        )
      ) {
        this.currentTableType = newRoute.route;
      } else {
        this.currentTableType = "";
      }
      if (newValue.length > 1) {
        this.enableBackProfile = true;
      } else {
        this.enableBackProfile = false;
      }
    },
    async triggerSaveModal() {
      this.isLoadingTable = true;
      this.isLoadingProfile = true;
      const response = await this.getProfile(this.selectedProject);
      this.selectedProject = response.data.data;
      const partRes = await this.getPagingPart(
        this.selectedProject.id,
        null,
        null,
        1,
        MINI_SIZE
      );
      this.currentTableData = partRes.data.data.content;
      this.currentListField = this.partFields;
      this.totalPage = partRes.data.data.totalPages;
      this.isLoadingTable = false;
      this.isLoadingProfile = false;
    },
    currentDate(newVal) {
      console.log("newVal", newVal);
    },
  },
  methods: {
    closeSuccessToast() {
      this.successToast = false;
    },
    backFromPreview() {
      $(this.getRootId() + "#category-library-modal").modal("show");
    },
    showModal: async function (
      data,
      date,
      firstStep,
      allowChangeRoute,
      newRoute
    ) {
      this.profileDate = date;
      $("#category-library-modal").modal("show");
      this.clearData();
      if (firstStep == "index") {
        this.isLoadingOverView = true;
        this.step = [{ route: "index", data: {} }];
        axios
          .get(`/api/categories/category-library/${data.id}/${this.tabStatus}`)
          .then((response) => {
            console.log(response.data);
            this.category = response.data.data;
            this.$nextTick(() => {
              let child = Common.vue.getChild(this.$children, "brand-overview");
              if (child != null) {
                child.initCarouselItem();
                this.isLoadingOverView = false;
              }
            });
          })
          .catch((error) => {
            console.log(error);
          });
      } else if (firstStep == "brand-list") {
        this.isLoadingBrand = true;
        this.step = [{ route: "brand-list", data: {} }];
        this.selectedBrand = data.id;
        if (data.id) {
          const brandRes = await axios.get(
            `/api/categories/brand-profile/${data.id}/${this.tabStatus}`
          );
          this.selectedBrand = brandRes.data.data;

          const categoryRes = await axios.get(
            `/api/categories/category-library/${this.selectedBrand.grandParentId}/${this.tabStatus}`
          );
          this.category = categoryRes.data.data;
          this.$nextTick(() => {
            let child = Common.vue.getChild(
              this.$children,
              "category-library-overview"
            );
            if (child != null) {
              child.initCarouselItem();
              this.isLoadingBrand = false;
            }
          });
        }
      } else if (firstStep == "project-profile") {
        this.isLoadingTable = true;
        this.tableSize = MINI_SIZE;
        this.step = [{ route: "project-profile", data: {} }];
        console.log(data, firstStep);
        this.requestParam.periodType = "WEEKLY";
        this.requestParam.periodValue = this.profileDate.fromTitle.replaceAll(
          "-",
          ""
        ); // TODO: RECHECK
        const queryParams = Common.param({
          periodType: this.requestParam.periodType,
          periodValue: this.requestParam.periodValue,
        });
        const response = await axios.get(
          `/api/categories/project-profile/${data.id}?` + queryParams
        );
        console.log(response.data);
        this.selectedProject = response.data.data;
        this.step[0].data.project = response.data.data;
        this.selectedBrand.name = response.data.data.parentName;

        console.log("response.data.data.parentId", response.data.data.parentId);
        this.selectedBrand.id = response.data.data.parentId;

        if (this.selectedBrand.id) {
          const brandRes = await axios.get(
            `/api/categories/brand-profile/${this.selectedBrand.id}/${this.tabStatus}`
          );
          this.selectedBrand = brandRes.data.data;
        } else {
          this.selectedBrand.parentId = null;
        }

        this.category.id = this.selectedProject.grandParentId;
        if (this.selectedProject.grandParentId) {
          const categoryRes = await axios.get(
            `/api/categories/category-library/${this.selectedProject.grandParentId}/${this.tabStatus}`
          );
          this.category = categoryRes.data.data;
        }

        this.getPagingPart(data.id, null, null, 1, this.tableSize)
          .then((response) => {
            console.log(response.data);
            this.isLoadingTable = false;
            this.currentTableData = response.data.data.content;
            this.totalPage = response.data.data.totalPages;
            this.projectPart = response.data.data.content;
            this.currentListField = this.partFields;
            console.log("check allowChangeRoute", allowChangeRoute);
            if (allowChangeRoute) {
              this.changeStep("forward", newRoute);
            }
          })
          .catch((error) => console.log(error));
      }
      this.profileDate = { ...date };
      let weekPicker = Common.vue.getChild(this.$children, "week-picker-popup");
      console.log("weekPicker", weekPicker);
      if (weekPicker != null) {
        weekPicker.handleInit();
      }
    },
    dimissModal: function () {
      $("#category-library-modal").modal("hide");
      this.$emit("finish-open-modal", false);
    },
    async initCategory(id) {
      const data = await axios.get(
        `/api/categories/category-library/${id}/${this.tabStatus}`
      );
      return data;
    },
    async initProject(id) {
      const data = await axios.get(`/api/categories/project-profile/${id}`);
      return data;
    },
    fetchImage(type) {
      const id = this.selectedProject.id;
      const self = this;
      self.loadingImage = true;
      setTimeout(() => {
        axios
          .get(`/api/categories/project-profile/${id}`)
          .then((response) => {
            console.log(response.data);
            self.selectedProject = response.data.data;
          })
          .catch((error) => {
            console.log(error);
          })
          .finally(() => {
            self.loadingImage = false;
            this.successToastType = type;
            this.successToast = true;
            setTimeout(() => {
              this.successToast = false;
            }, 3000);
          });
      }, 3000);
    },
    handleChooseProject(project) {
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.profileDate.fromTitle.replaceAll(
        "-",
        ""
      );
      const params = Common.param({
        periodType: this.requestParam.periodType,
        periodValue: this.requestParam.periodValue,
      });
      axios
        .get(`/api/categories/project-profile/${project.id}?` + params)
        .then((response) => {
          this.selectedProject = response.data.data;
          this.changeStep("forward", {
            route: "project-profile",
            data: { project: this.selectedProject },
          });
        })
        .catch((error) => {
          console.log(error);
        });
    },
    handleChooseBrand(brand) {
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.profileDate.fromTitle.replaceAll(
        "-",
        ""
      );
      this.isLoadingBrand = true;
      const params = Common.param({
        periodType: this.requestParam.periodType,
        periodValue: this.requestParam.periodValue,
      });
      const url = brand?.id
        ? "/api/categories/brand-profile"
        : "/api/categories/no-brand-profile";
      const id = brand.id ? brand.id : brand.grandParentId;
      axios
        .get(`${url}/${id}/${this.tabStatus}?` + params)
        .then((response) => {
          console.log("@handleChooseBrand", response.data.data);
          this.selectedBrand = response.data.data;
          this.changeStep("forward", {
            route: "brand-list",
            data: { brand: this.selectedBrand },
          });
          this.isLoadingBrand = false;
        })
        .catch((error) => {
          console.log(error);
        });
    },
    resetStep() {
      this.step = [];
    },
    handleChangeRoute(route, isBrand, isBackward) {
      console.log("@handleChangeRoute", route, isBrand);
      if (route.data.brand) {
        this.selectedBrand = route.data.brand;
        console.log("@handleChangeRoute:selectedBrand", this.selectedBrand);
      }
      if (route.data.project) {
        this.selectedProject = route.data.project;
        console.log("@handleChangeRotue:selectedProject", this.selectedProject);
      }
      this.changeStep("forward", route, isBrand);
    },
    clearDetailData() {
      this.dataDetail = null;
    },
    async changeStep(type, newStep, isBrand) {
      console.log("@changeStep", type, newStep, isBrand, this.step);
      const currentStep = this.step[this.step.length - 1];
      this.isLoadingTable = true;
      let step = null;
      let brandId = null;
      let projectId = null;
      let moldId = null;
      let supplierId = null;
      let partId = null;
      let page = 1;
      let brand = null;
      this.clearDetailData();
      if (type == "forward") {
        step = newStep;
        projectId = step.data.project ? step.data.project.id : null;
        moldId = step.data.moldId;
        supplierId = step.data.supplierId;
        partId = step.data.partId;
        brand = step.data.brand;
        console.log("handle", projectId, moldId, supplierId, partId);
        page = step.data.page ? step.data.page : 1;
        switch (step.route) {
          case "brand-list":
            this.isLoadingProfile = true;
            if (this.step[this.step.length - 1].route != step.route) {
              this.step.push(step);
              await this.handleChooseBrand(brand);
            }
            console.log(
              "after change in change step",
              this.selectedBrand,
              this.step
            );
            break;
          case "project-profile":
            this.tableSize = MINI_SIZE;
            this.isLoadingProfile = true;
            if (this.step[this.step.length - 1].route != step.route) {
              this.step.push(step);
              console.log("push profile", this.step);
            }
            this.getPagingPart(
              projectId,
              moldId,
              supplierId,
              page,
              this.tableSize
            )
              .then((response) => {
                console.log(response.data);
                this.isLoadingTable = false;
                this.isLoadingProfile = false;
                this.currentTableData = response.data.data.content;
                this.currentListField = this.partFields;
                this.totalPage = response.data.data.totalPages;
              })
              .catch((error) => console.log(error));
            break;
          case "list-part":
            this.tableSize = LARGE_SIZE;
            if (this.step[this.step.length - 1].route != step.route) {
              this.step.push(step);
            }
            if (isBrand) {
              brand = step.data.brand;
              try {
                const response = await this.getPagingPartByBrand(
                  brand.id,
                  moldId,
                  supplierId,
                  page,
                  this.tableSize
                );
                this.isLoadingTable = false;
                this.currentTableData = response.data.data.content;
                this.currentListField = this.partFields;
                this.totalPage = response.data.data.totalPages;
              } catch (error) {}
            } else {
              this.getPagingPart(
                projectId,
                moldId,
                supplierId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.partFields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }

            break;
          case "list-mold":
            this.tableSize = LARGE_SIZE;
            if (this.step[this.step.length - 1].route != step.route) {
              this.step.push(step);
            }
            if (isBrand) {
              brand = step.data.brand;
              try {
                const response = await this.getPagingMoldByBrand(
                  brand.id,
                  moldId,
                  supplierId,
                  page,
                  this.tableSize
                );
                this.isLoadingTable = false;
                this.currentTableData = response.data.data.content;
                this.currentListField = this.moldFields;
                this.totalPage = response.data.data.totalPages;
              } catch (error) {}
            } else {
              this.getPagingMold(
                projectId,
                partId,
                supplierId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.moldFields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }
            break;
          case "list-supplier":
            this.tableSize = LARGE_SIZE;
            if (this.step[this.step.length - 1].route != step.route) {
              this.step.push(step);
            }
            console.log("list-supplier");
            if (isBrand) {
              brand = step.data.brand;
              try {
                const response = await this.getPagingSupplierByBrand(
                  brand.id,
                  moldId,
                  supplierId,
                  page,
                  this.tableSize
                );
                this.isLoadingTable = false;
                this.currentTableData = response.data.data.content;
                this.currentListField = this.suppliersfields;
                this.totalPage = response.data.data.totalPages;
              } catch (error) {}
            } else {
              this.getPagingSupplier(
                projectId,
                moldId,
                partId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.suppliersfields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }

            break;
          case "part-detail":
          case "mold-detail":
            this.dataDetail = step.data.dataDetail;
            this.step.push(step);
            // axios.get(
            //   `/api/parts/${partId}`
            // ).then((response) => {
            //     this.part=response.data;
            // })
            break;
          case "company-detail":
            this.dataDetail = step.data.dataDetail;
            let companyId = step.data.id;
            if (!this.dataDetail && companyId !== null) {
              await axios
                .get(`/api/companies/${companyId}`)
                .then((response) => {
                  this.dataDetail = response.data;
                });
            }
            this.step.push(step);
            break;
        }
      } else if (type == "backward") {
        const prevStep = this.step[this.step.length - 2];
        const beforePrevStep = this.step[this.step.length - 3] || null;
        step = this.step[this.step.length - 2];
        console.log("backward>>>step", step);
        moldId = step.data.moldId;
        supplierId = step.data.supplierId;
        partId = step.data.partId;
        page = step.data.page ? step.data.page : 1;
        let isFromBrand = true;
        const find = this.step.find((item) => item.route === "brand-list");
        if (find) {
          isFromBrand = false;
          console.log("111");
          projectId = step?.data?.project?.id;
        } else {
          console.log("222");
          brandId = step?.data?.brand?.id;
        }

        switch (step.route) {
          case "project-profile":
            this.tableSize = MINI_SIZE;
            this.step.pop();
            console.log("there!");
            this.getPagingPart(
              projectId,
              moldId,
              supplierId,
              page,
              this.tableSize
            )
              .then((response) => {
                console.log(response.data);
                this.isLoadingTable = false;
                this.totalPage = response.data.data.totalPages;
                this.currentTableData = response.data.data.content;
                this.currentListField = this.partFields;
              })
              .catch((error) => console.log(error));
            break;
          case "index":
          case "brand-list":
            this.step.pop();
            break;
          case "list-part":
            this.tableSize = LARGE_SIZE;
            this.step.pop();
            if (isFromBrand) {
              const response = await this.getPagingPartByBrand(
                brandId,
                moldId,
                supplierId,
                page,
                this.tableSize
              );
              this.isLoadingTable = false;
              this.currentTableData = response.data.data.content;
              this.currentListField = this.partFields;
              this.totalPage = response.data.data.totalPages;
            } else {
              this.getPagingPart(
                projectId,
                moldId,
                supplierId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.partFields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }
            break;
          case "list-mold":
            this.tableSize = LARGE_SIZE;
            this.step.pop();
            if (isFromBrand) {
              const response = await this.getPagingMoldByBrand(
                brandId,
                moldId,
                supplierId,
                page,
                this.tableSize
              );
              this.isLoadingTable = false;
              this.currentTableData = response.data.data.content;
              this.currentListField = this.moldFields;
              this.totalPage = response.data.data.totalPages;
            } else {
              this.getPagingMold(
                projectId,
                partId,
                supplierId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.moldFields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }
            break;
          case "list-supplier":
            this.tableSize = LARGE_SIZE;
            this.step.pop();
            if (isFromBrand) {
              const response = await this.getPagingSupplierByBrand(
                brandId,
                moldId,
                supplierId,
                page,
                this.tableSize
              );
              this.isLoadingTable = false;
              this.currentTableData = response.data.data.content;
              this.currentListField = this.suppliersfields;
              this.totalPage = response.data.data.totalPages;
            } else {
              this.getPagingSupplier(
                projectId,
                moldId,
                partId,
                page,
                this.tableSize
              )
                .then((response) => {
                  console.log(response.data);
                  this.isLoadingTable = false;
                  this.currentTableData = response.data.data.content;
                  this.currentListField = this.suppliersfields;
                  this.totalPage = response.data.data.totalPages;
                })
                .catch((error) => console.log(error));
            }
            break;
        }
      }
    },
    clearData() {
      this.category = {
        id: null,
        name: "",
        projectProfiles: [],
      };
      this.selectedProject = {
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      };
      this.step = [{ route: "index", data: {} }];
      this.currentTableData = [];
    },
    async getPagingPart(projectId, moldId, supplierId, pageNum, size) {
      let paramData = {
        projectId: projectId,
        page: pageNum,
        size: size,
      };
      if (moldId) {
        paramData.moldId = moldId;
      } else if (supplierId) {
        paramData.supplierId = supplierId;
      }
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.profileDate.fromTitle.replace(
        /\D/g,
        ""
      );
      const finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(`/api/categories/part-by-project?${param}`);
      return data;
    },
    async getPagingMold(projectId, partId, supplierId, pageNum, size) {
      let paramData = {
        projectId: projectId,
        page: pageNum,
        size: size,
      };
      if (partId) {
        paramData.partId = partId;
      } else if (supplierId) {
        paramData.supplierId = supplierId;
      }
      let finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(`/api/categories/mold-by-project?${param}`);
      return data;
    },
    async getPagingSupplier(projectId, moldId, partId, pageNum, size) {
      let paramData = {
        projectId: projectId,
        page: pageNum,
        size: size,
      };
      if (moldId) {
        paramData.moldId = moldId;
      } else if (partId) {
        paramData.partId = partId;
      }
      let finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(
        `/api/categories/supplier-by-project?${param}`
      );
      return data;
    },
    handlePaging(sign) {
      let currentPage = 1;
      if (this.step[this.step.length - 1].data.page) {
        currentPage = this.step[this.step.length - 1].data.page;
      }
      this.step[this.step.length - 1].data.page = eval(
        `${currentPage}${sign}1`
      );
      console.log(this.step);
      this.changeStep("forward", this.step[this.step.length - 1]);
    },
    setRouteName(name) {
      this.step[this.step.length - 1].data.name = name;
    },
    handleChangeBreadcrumb(step, index, directType) {
      console.log("handleChangeBreadcrumb", step, index, directType, this.step);
      if (directType) {
        if (directType == "list") {
          this.step.splice(index);
          this.changeStep("forward", step);
        } else if (directType == "detail") {
          this.changeStep("forward", step);
        }
      } else {
        if (step.route == "index") {
          this.isLoadingOverView = true;
          this.step = [{ route: "index", data: {} }];
          axios
            .get(
              `/api/categories/category-library/${this.category.id}/${this.tabStatus}`
            )
            .then((response) => {
              console.log(response.data);
              this.category = response.data.data;
              this.$nextTick(() => {
                let child = Common.vue.getChild(
                  this.$children,
                  "brand-overview"
                );
                if (child != null) {
                  child.initCarouselItem();
                  this.isLoadingOverView = false;
                }
              });
            })
            .catch((error) => {
              console.log(error);
            });
        } else if (step.route == "brand-list") {
          const find = this.step.find((item) => item.route == "brand-list");
          const findIndex = _.indexOf(this.step, find, 0);
          console.log("find index", find, findIndex);
          if (findIndex == -1) {
            this.step.splice(1);
            this.changeStep("forward", step);
          } else {
            this.step.splice(findIndex);
            this.changeStep("backward");
          }
        } else if (step.route == "project-profile") {
          if (index == -1) {
            this.step.splice(1);
            this.changeStep("forward", step);
          } else {
            if (index == 0) {
              this.step.splice(2);
            } else if (index == 1) {
              this.step.splice(3);
            }
            this.changeStep("backward");
          }
        }
      }
    },
    handleClickCategoryName() {
      if (this.category.id) {
        if (this.getCurrentStep != "index") {
          // click title back logic
          console.log("this.category", this.category);
          this.showModal(this.category, this.profileDate, "index");
        }
      }
      return;
    },
    handleClickBrandName() {
      if (this.selectedBrand.id) {
        const disableBrand = ["brand-list", "index"].includes(
          this.getCurrentStep
        );
        if (!disableBrand) {
          console.log("this.selectedBrand", this.selectedBrand);
          this.showModal(this.selectedBrand, this.profileDate, "brand-list");
        }
      }
    },
    async handleChangeDate(date) {
      this.profileDate = { ...date };
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = date.fromTitle.replaceAll("-", "");
      console.log("change date", date);
      let newProject = await this.getProfile(this.selectedProject);
      console.log("load", newProject);
      this.getPagingPart(this.selectedProject.id, null, null, 1, MINI_SIZE)
        .then((response) => {
          console.log(response.data);
          this.isLoadingTable = false;
          this.totalPage = response.data.data.totalPages;
          this.currentTableData = response.data.data.content;
          this.currentListField = this.partFields;
        })
        .catch((error) => console.log(error));
      this.selectedProject = newProject.data.data;
      this.$emit("change-date", date);
    },
    async getProfile(project) {
      console.log("getProfile");
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.profileDate.fromTitle.replaceAll(
        "-",
        ""
      ); // TODO: RECHECK
      const paramData = { ...this.requestParam };
      const param = Common.param(paramData);
      const result = await axios.get(
        `/api/categories/project-profile/${project.id}?${param}`
      );
      return result;
    },
    showDetailModal(result) {
      const fistStep = result.level > 1 ? "project-profile" : "index";
      const child = Common.vue.getChild(
        this.$children,
        "category-library-modal"
      );
      if (child != null) {
        const date = { ...this.currentDate };
        child.showModal(result, date, fistStep);
      }
    },
    async getPagingPartByBrand(brandId, moldId, supplierId, pageNum, size) {
      let paramData = {
        brandId: brandId,
        page: pageNum,
        size: size,
      };
      if (moldId) {
        paramData.moldId = moldId;
      } else if (supplierId) {
        paramData.supplierId = supplierId;
      }
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = this.profileDate.fromTitle.replace(
        /\D/g,
        ""
      );
      const finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(`/api/categories/part-by-brand?${param}`);
      return data;
    },
    async getPagingMoldByBrand(brandId, partId, supplierId, pageNum, size) {
      let paramData = {
        brandId: brandId,
        page: pageNum,
        size: size,
      };
      if (partId) {
        paramData.partId = partId;
      } else if (supplierId) {
        paramData.supplierId = supplierId;
      }
      let finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(`/api/categories/mold-by-brand?${param}`);
      return data;
    },
    async getPagingSupplierByBrand(brandId, moldId, partId, pageNum, size) {
      let paramData = {
        brandId: brandId,
        page: pageNum,
        size: size,
      };
      if (moldId) {
        paramData.moldId = moldId;
      } else if (partId) {
        paramData.partId = partId;
      }
      let finalParam = { ...paramData, ...this.requestParam };
      const param = Common.param(finalParam);
      const data = await axios.get(
        `/api/categories/supplier-by-brand?${param}`
      );
      return data;
    },
  },
  mounted() {
    this.requestParam.periodType = "WEEKLY";
    this.requestParam.periodValue = this.profileDate.fromTitle.replaceAll(
      "-",
      ""
    );
  },
};
</script>
<style scoped>
.modal-content {
  width: 1025px;
}

.modal-body__container {
  min-height: 600px;
  position: relative;
}

.modal-body__carret {
  position: absolute;
  top: 50%;
  transform: translate(0, -50%);
  cursor: pointer;
}

.carret-left {
  left: -30px;
}

.carret-right {
  right: -30px;
}

.carousel-cell {
  width: 28%;
  margin-right: 10px;
  color: white;
}

.block__image {
  width: 280px;
  height: 240px;
  background-color: #f2f2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}

.block__button {
  padding: 15px 0;
  text-align: center;
}

.block__container {
  background-color: #fcfcfc;
  padding: 10px;
}

.block__content {
  margin: 10px 0;
  font-size: 16px;
}

.block__content__title {
  font-weight: bold;
  color: #595959;
}

.content__hyper-link {
  color: #3491ff;
  cursor: pointer;
}

.content__hyper-link:hover {
  color: #3585e5;
  text-decoration: underline;
}

.content__hyper-link img {
  margin-bottom: 3px;
}

.inactive {
  pointer-events: none;
}

.carousel,
.flickity-viewport,
.carousel-cell {
  height: 630px;
}

.carousel {
  position: relative;
}

.flickity-viewport {
  overflow: hidden;
  position: relative;
}

.flickity-slider {
  position: absolute;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease-in-out;
}

.carousel-cell {
  width: 280px;
  margin-right: 20px;
  color: white;
}

.block__content:first-child {
  margin-top: 0;
}

.exsisted-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.block__infor {
  width: 205px;
  height: 240px;
  padding: 10px;
}

.d-flex {
  display: flex;
}

.modal-body__content__infor {
  justify-content: space-between;
}

.back-icon {
  margin-right: 25px;
  cursor: pointer;
}

.modal-body__header {
  align-items: center;
  margin-bottom: 20px;
}

.modal-body__content__table {
  margin-top: 15px;
  height: 217px;
}

.loading-wave {
  animation: wave 4s infinite linear forwards;
  -webkit-animation: wave 4s infinite linear forwards;
  background: linear-gradient(to right, #fcfcfc 8%, #ededed 18%, #fcfcfc 33%);
  border: none;
}

.modal-body__header__infor {
}

.modal-body__header__infor .modal-body__title {
  margin-bottom: 0;
}

.space-between {
  justify-content: space-between;
}

.item-middle {
  align-items: center;
}

.truncate-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: -5px;
}

.icon.back-arrow-disable {
  width: 16.5px;
  height: 26.6px;
  background-image: url("/images/icon/category/back-arrow-disable.svg");
  background-repeat: no-repeat;
  background-size: 100%;
  display: inline-block;
}

.title-name-header {
  padding: 6px 8px;
  width: auto !important;
}

.background-name-header-wrapper {
  background: #fbfcfd;
  border-radius: 3px;
  padding: 6px 8px;
}

.truncate-card-title {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  max-width: 220px;
  margin-right: 10px;
}

.custom-header-table th {
  border-top: unset;
}
</style>
<style scoped src="/components/category/category-project-modal.css"></style>
