<template>
  <div>
    <div class="row">
      <div class="col-lg-12">
        <div class="card" style="border: none">
          <div class="card-body card-overflow-reset">
            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{
                    active:
                      requestParam.status == '1' && tabActive != 'disabled',
                  }"
                  href="#"
                  @click.prevent="tab('1', 'enabled')"
                  ><span>{{ resources["enabled"] }}</span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == '1' && tabActive != 'disabled'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li class="nav-item dropdown">
                <a
                  class="nav-link"
                  :class="{ active: tabActive === 'disabled' }"
                  href="#"
                  @click.prevent="tab('1', 'disabled')"
                >
                  <span>{{ resources["disabled"] }}</span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="tabActive == 'disabled'"
                    >{{ total }}</span
                  >
                </a>
              </li>
            </ul>
            <div
              class="nav-item d-flex"
              style="
                position: absolute;
                right: 18px;
                top: 19px;
                align-items: center;
              "
            >
              <a
                v-if="
                  showSourceSupplierButton && Object.entries(resources).length
                "
                id="sourceSupplierBtn"
                href="javascript:void(0)"
                @click="handleShowSourceSupplier"
                class="btn-custom btn-outline-custom-primary"
                style="height: 31px; margin-right: 8px"
              >
                <span>{{ resources["source_supplier"] }}</span>
              </a>
              <div class="btn-group-create" style="margin-right: 8px">
                <week-picker-popup
                  :key="weekKey"
                  @on-close="handleChangeDate"
                  :current-date="currentDate"
                  :button-type="'secondary'"
                  :date-range="timeRange"
                >
                </week-picker-popup>
              </div>
              <div>
                <div>
                  <a-dropdown :trigger="['click']">
                    <a
                      @click="(e) => e.preventDefault()"
                      :class="{ 'outline-animation': showAnimation }"
                      class="btn-custom btn-custom-primary animationPrimary all-time-filters"
                    >
                      <span>{{ resources["create"] }}</span>
                      <img
                        class="img-transition"
                        src="/images/icon/icon-cta-white.svg"
                        :class="{
                          'caret-show': displayCaret,
                          visible: visibleCreateDropdown,
                        }"
                        alt=""
                      />
                    </a>
                    <a-menu
                      slot="overlay"
                      class="wrapper-action-dropdown"
                      @click="handleCreate"
                      v-model="visibleCreateDropdown"
                    >
                      <a-menu-item
                        key="goToCreateProject"
                        class="menu-item-custom"
                      >
                        <span>{{ resources["create_project"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        key="goToCreateCategory"
                        class="menu-item-custom"
                      >
                        <span>{{ resources["create_category"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        key="showCreateBranch"
                        class="menu-item-custom"
                      >
                        <div>Create Brand</div>
                      </a-menu-item>
                      <a-menu-item key="showDrawer" class="menu-item-custom">
                        <div>Create Property</div>
                      </a-menu-item>
                    </a-menu>
                  </a-dropdown>
                </div>
              </div>
              <customization-modal
                style="margin-left: 8px"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
              ></customization-modal>
            </div>

            <table
              class="table table-responsive-sm table-striped custom-table-container"
            >
              <colgroup>
                <col />
                <col />
                <col />
                <col style="width: 130px" />
              </colgroup>
              <thead id="thead-actionbar" class="custom-header-table">
                <tr
                  :class="{ invisible: listCheckedFull.length != 0 }"
                  style="height: 57px"
                  class="tr-sort"
                >
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <input
                        id="checkbox-all"
                        class="checkbox"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </div>
                  </th>
                  <th
                    v-for="(item, index) in allColumnList"
                    v-if="item.enabled && !item.hiddenInToggle"
                    class="text-left"
                    :class="[
                      {
                        __sort: item.sortable && !item.isCustomField,
                        active: currentSortType === item.sortField,
                        'disable-click': !item.sortable,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.isNumber
                        ? 'text-right'
                        : item.isCenter
                        ? 'text-center'
                        : 'text-left',
                    ]"
                    @click="sortBy(item.field)"
                  >
                    <span>{{ item.label }}</span>
                    <span v-if="item.sortable" class="__icon-sort"></span>
                  </th>
                </tr>

                <tr
                  :class="{
                    'd-none zindexNegative': listCheckedFull.length == 0,
                  }"
                  id="action-bar"
                  class="tr-sort empty-tr"
                >
                  <th class="empty-th">
                    <div class="first-checkbox-zone2">
                      <div
                        class="first-checkbox2"
                        style="
                          display: flex;
                          align-items: center;
                          padding-left: 0;
                          width: 110px;
                        "
                      >
                        <div>
                          <input
                            id="checkbox-all-2"
                            class="checkbox"
                            type="checkbox"
                            v-model="isAll"
                            @change="select"
                          />
                        </div>
                      </div>
                      <div class="action-bar">
                        <div
                          v-if="listCheckedFull.length == 1"
                          @click="showEditModal"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="listCheckedFull.length == 1"
                          @click="showRevisionHistory(listCheckedFull[0].id)"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="listCheckedFull.length == 1"
                          class="action-item"
                          @click="showSystemNoteModal(listCheckedFull[0].id)"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="tabActive != 'disabled' && canDisable"
                          class="action-item"
                          @click.prevent="disable(listCheckedFull)"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="tabActive == 'disabled'"
                          class="action-item"
                          @click.prevent="enable(listCheckedFull)"
                        >
                          <span>Enable</span>
                          <div class="icon-action enable-icon"></div>
                        </div>

                        <div
                          v-if="canAssignPartsToProject"
                          class="action-item"
                          @click.stop="openAssignPartsToProjectModal"
                        >
                          <span>Assign Part(s) to Product</span>
                          <div class="icon-action share-icon"></div>
                        </div>
                        <!-- <div
                          v-if="canAssignPartsToProject"
                          class="action-item"
                          @click.stop="openEditProductDemand(false)"
                        >
                          <span>Edit Product Demand</span>
                          <div class="icon-action product-demand-icon"></div>
                        </div> -->
                      </div>
                    </div>
                  </th>
                </tr>
              </thead>

              <tbody class="op-list" style="display: none">
                <template v-for="(result, index) in results">
                  <category-list
                    :result="result"
                    :id="result.id"
                    :index="index"
                    :list-checked="listChecked"
                    :check-select="checkSelect"
                    :check="check"
                    :show-drowdown="showDrowdown"
                    :change-show="changeShow"
                    :show-system-note-modal="showSystemNoteModal"
                    :show-detail-modal="showDetailModal"
                    :column-list="allColumnList"
                    :is-showing-child="listShowId.includes(`${index}`)"
                    @show-children="handleShowHideRow"
                  >
                  </category-list>
                  <template
                    v-for="(brand, i) in result.children"
                    v-if="listShowId.includes(`${index}`)"
                  >
                    <category-list
                      :result="brand"
                      :id="brand.id"
                      :parent-index="index"
                      :index="i"
                      :list-checked="listChecked"
                      :check-select="checkSelect"
                      :check="check"
                      :show-drowdown="showDrowdown"
                      :change-show="changeShow"
                      :show-system-note-modal="showSystemNoteModal"
                      :show-detail-modal="showDetailModal"
                      :is-showing-child="listShowId.includes(`${index}-${i}`)"
                      :column-list="allColumnList"
                      @show-children="handleShowHideRow"
                    >
                    </category-list>
                    <template
                      v-for="(product, j) in brand.children"
                      v-if="listShowId.includes(`${index}-${i}`)"
                    >
                      <category-list
                        :result="product"
                        :id="product.id"
                        :list-checked="listChecked"
                        :check-select="checkSelect"
                        :check="check"
                        :show-drowdown="showDrowdown"
                        :change-show="changeShow"
                        :show-system-note-modal="showSystemNoteModal"
                        :show-detail-modal="showDetailModal"
                        :column-list="allColumnList"
                      >
                      </category-list>
                    </template>
                  </template>
                </template>
              </tbody>
            </table>

            <div class="no-results d-none">{{ resources["no_results"] }}</div>

            <div class="row">
              <div class="col-md-8">
                <ul class="pagination">
                  <li
                    v-for="(data, index) in pagination"
                    :key="index"
                    class="page-item"
                    :class="{ active: data.isActive }"
                  >
                    <a class="page-link" @click="paging(data.pageNumber)">{{
                      data.text
                    }}</a>
                  </li>
                </ul>
              </div>
              <div class="col-auto ml-auto" v-if="false">
                <a href="/admin/categories/new" class="btn btn-primary"
                  ><i class="fa fa-plus"></i>
                  <span>{{ resources["create_category"] }}</span></a
                >
              </div>
            </div>
          </div>
        </div>
      </div>
      <create-category-property
        :resources="resources"
        :visible="visible"
        @open="showDrawer()"
        @close="closeDrawer()"
        is-type="CATEGORY"
        @reload="setAllColumnList"
      />
    </div>

    <revision-history :resources="resources"></revision-history>
    <system-note
      system-note-function="CATEGORY_SETTING"
      :resources="resources"
    ></system-note>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <category-library-modal
      :show-file-previewer="showFilePreviewer"
      :trigger-save-modal="triggerSaveModal"
      :current-date="currentDate"
      @change-date="handleChangeDate"
      :resources="resources"
      :tab="tabActive"
    ></category-library-modal>

    <assign-parts-to-project
      ref="assignPartsToProjectRef"
      :resources="resources"
      :is-visible="showAssignToProject"
      :selected-project="selectedProject"
      :on-refetch="paging"
      @close="closeAssignPartsToProjectModal"
    >
    </assign-parts-to-project>

    <project-form-modal
      ref="projectModalRef"
      :resources="resources"
      @reload-page="reloadPage"
      :on-create-category="goToCreateCategory"
      :on-create-brand="onCreateBrand"
      :default-selected-brands="defaultSelectedBrands"
      :selected-category="selectedCategory"
      @on-close="onCloseProduct"
      :no-category-data="noCategoryData"
    ></project-form-modal>
    <category-form-modal
      ref="categoryModalRef"
      :resources="resources"
      @reload-page="reloadPage"
      :on-create-brand="onCreateBrand"
      :default-selected-brands="defaultSelectedBrands"
      :no-category-data="noCategoryData"
      @reset-props="resetSelectedCategoryAndBrand"
      @on-success-create-category="onSuccessCreateCategory"
    >
    </category-form-modal>
    <edit-product-demand
      ref="editProductDemand"
      :resources="resources"
      :current-date="currentDate"
      @load-success="loadSuccess"
    >
    </edit-product-demand>

    <file-previewer :back="backToMoldDetail"></file-previewer>

    <source-supplier-modal
      :is-visible="showSourceSupplierModal"
      :current-date="currentDate"
      :selected-product-prop="selectedProduct"
      :resources="resources"
      @close="handleCloseSourceSupplier"
    >
    </source-supplier-modal>
    <brand-form-modal
      :is-show="visibleCreateBranch"
      :resources="resources"
      :default-brand="selectedBrand"
      :selected-category="selectedCategory"
      :is-in-modal="isBrandModalInCategoryModal"
      :is-edit="isEditBrand"
      @on-success-create-brand="onSuccessCreateBrand"
      @close="handleCloseCreateBranch"
      @reload-page="reloadPage"
      :open-category-modal="goToCreateCategory"
    ></brand-form-modal>
  </div>
</template>

<script>
const PAGE_TYPE = "CATEGORY_SETTING";
module.exports = {
  components: {
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "category-library-modal": httpVueLoader(
      "/components/category/category-library/index.vue"
    ),
    "category-form-modal": httpVueLoader(
      "/components/category/category-form-modal.vue"
    ),
    "assign-parts-to-project": httpVueLoader(
      "/components/category/assign-parts-to-project/index.vue"
    ),
    "project-form-modal": httpVueLoader(
      "/components/category/project-form-modal.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "assign-parts-to-project-test": httpVueLoader(
      "/components/category/assign-parts-to-project/test.vue"
    ),
    "category-list": httpVueLoader("/components/category/category-list.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "source-supplier-modal": httpVueLoader(
      "/components/category/source-supplier/source-supplier-modal.vue"
    ),
    "week-picker-popup": httpVueLoader(
      "/components/category/week-picker-popup.vue"
    ),
    "edit-product-demand": httpVueLoader(
      "/components/category/edit-product-demand.vue"
    ),
    "create-category-property": httpVueLoader(
      "/components/category/CreateCategoryProperty.vue"
    ),
    "brand-form-modal": httpVueLoader(
      "/components/category/brand-form-modal.vue"
    ),
  },
  props: {
    resources: Object,
    searchText: String,
    reloadKey: [String, Number],
  },
  data() {
    return {
      showDrowdown: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        enabled: true,
        query: "",
        status: "1",
        sort: "sortOrder,asc",
        page: 1,
      },
      sortType: {
        NAME: "name",
        DESCRIPTION: "description",
        STATUS: "enabled",
      },
      currentSortType: "id",
      isDesc: true,
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      listCheckedFull: [],
      tabActive: "enabled",
      showAssignToProject: false,
      selectedProject: null,
      categoryModalRef: null,
      projectModalRef: null,
      allColumnList: [],
      showSourceSupplierModal: false,
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      },
      editFromChild: false,
      triggerSaveModal: 0,
      selectedProduct: null,
      timeRange: {
        minDate: null,
        maxDate: new Date("2100-01-01"),
      },
      weekKey: 0,
      visible: false,
      displayCaret: false,
      visiblePopover: false,
      showAnimation: false,
      visibleCreateDropdown: false,
      visibleCreateBranch: false,
      listShowId: [],
      isBrandModalInCategoryModal: false,
      selectedCategory: { id: "", name: "" },
      defaultSelectedBrands: [],
      selectedBrand: null,
      noCategoryData: null,
      listCheckedWithParent: [],
      isEditBrand: false,
    };
  },
  created() {
    this.pageType = "CATEGORY_SETTING";
    this.objectType = "CATEGORY";
  },
  computed: {
    canAssignPartsToProject() {
      console.log("canAssignPartsToProject", this.listChecked);
      // only one category which is project can be assigned
      return (
        this.listChecked.length === 1 && this.listCheckedFull?.[0]?.level === 3
      );
    },
    getCheckItem() {
      console.log("getCheckItem", this.listCheckedFull);
      if (this.listCheckedWithParent.length === 1) {
        const checkedItemWithParent = this.listCheckedWithParent[0];
        const find = this.listCheckedFull.find(
          (item) =>
            item.id === checkedItemWithParent.id &&
            item.parentId === checkedItemWithParent.parentId
        );
        if (find) {
          return find;
        }
      }
      return null;
    },
    showSourceSupplierButton() {
      console.log(
        "showSourceSupplierButton",
        this.listCheckedFull,
        this.listCheckedFull[0]?.level
      );
      const selectedNumber = this.listCheckedFull.length;
      const hasSelectedOne =
        selectedNumber === 1 && this.listCheckedFull[0]?.level === 3;
      return selectedNumber === 0 || hasSelectedOne;
    },
    canDisable() {
      const checkedItem = this.getCheckItem;
      console.log("canDisable", checkedItem);
      if (checkedItem && checkedItem.id) {
        return true;
      }
      return false;
    },
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText(newVal) {
      this.requestParam.query = newVal;
      this.paging(1);
    },
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this;
        // const x = this.results.filter(item => self.listChecked.indexOf(item.id) > -1);
        const x = [];
        this.results.forEach((item) => {
          if (self.listChecked.indexOf(item.id) > -1) {
            x.push(item);
          }
          if ((item) => item.children != null && item.children.length > 0) {
            let pList = item.children
              .filter((p) => self.listChecked.indexOf(p.id) > -1)
              .map((project) => ({
                ...project,
                parentName: item.name,
              }));
            x.push(...pList);
          }
        });
        // this.listCheckedFull = x;
      }
    },
    visiblePopover(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    },
    visibleCreateDropdown(newVal) {
      console.log("visibleCreateDropdown", newVal);
    },
  },
  methods: {
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.visiblePopover) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
    },
    showDrawer() {
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    loadSuccess() {
      this.paging();
      this.reloadPage();
      if (this.editFromChild) {
        this.triggerSaveModal++;
      }
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["category_and_product_name"],
          field: "name",
          sortable: true,
          default: true,
          mandatory: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["description"],
          field: "description",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["division"],
          field: "division",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        // { label: this.resources['product_production_demand'], field: 'totalProductionDemand',  default: false, sortable: false, defaultSelected: false, defaultPosition: 2},
        {
          label: this.resources["number_of_parts"],
          field: "partCount",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 3,
          isNumber: true,
          onClick: this.handleClickPart,
        },
        {
          label: this.resources["number_of_suppliers"],
          field: "supplierCount",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 4,
          isNumber: true,
          onClick: this.handleClickSupplier,
        },
        {
          label: this.resources["number_of_tooling"],
          field: "moldCount",
          default: true,
          sortable: false,
          defaultSelected: true,
          defaultPosition: 5,
          isNumber: true,
          onClick: this.handleClickTooling,
        },
        {
          label: this.resources["produced_quantity"],
          field: "totalProduced",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 6,
          isNumber: true,
          onClick: this.handleClickProductProduced,
        },
        {
          label: this.resources["predicted_quantity"],
          field: "predictedQuantity",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 7,
          isNumber: true,
          onClick: this.handleClickProductProduced,
        },
        {
          label: this.resources["production_demand"],
          field: "totalProductionDemand",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 8,
          isNumber: true,
          onClick: this.handleClickProductProduced,
        },
        {
          label: this.resources["project_total_max_capacity"],
          field: "totalMaxCapacity",
          sortable: false,
          default: true,
          defaultSelected: true,
          defaultPosition: 9,
          isNumber: true,
        },
        {
          label: this.resources["delivery_risk"],
          field: "deliveryRiskLevel",
          sortable: false,
          default: false,
          defaultSelected: false,
          defaultPosition: 10,
          isNumber: false,
          isCenter: true,
        },
        // { label: this.resources['project_weekly_max_capacity'], field: 'weeklyMaxCapacity', sortable: false, default: false, defaultSelected: false, defaultPosition: 6, isNumber: true},
        // { label: this.resources['weekly_production_demand'], field: 'weeklyProductionDemand', sortable: false, default: false, defaultSelected: false, defaultPosition: 7, isNumber: true},
        // { label: this.resources['total_project_production_demand'], field: 'totalProductionDemand',  default: false, sortable: false, defaultSelected: false, defaultPosition: 8},
      ];
      this.resetColumnsListSelected();
      console.log("allColumnList", this.allColumnList);
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(
          this,
          this.objectType,
          this.allColumnList,
          null,
          this.configDeleteField
        );
        this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    getColumnListSelected() {
      const self = this;
      console.log("/api/config/column-config?pageType=", PAGE_TYPE);
      axios.get(`/api/config/column-config?pageType=${PAGE_TYPE}`).then(
        (response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              console.log(item.columnName);
              hashedByColumnName[item.columnName] = item;
            });
            self.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            self.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            self.$forceUpdate();
            console.log("this is the child view obj", self.$children);
            let child = Common.vue.getChild(self.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        },
        (error) => {
          Common.alert(error.data);
        }
      );
    },
    getCustomColumnAndInit() {
      const self = this;
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          self.allColumnList.push(...data);
          // this.resetColumnsListSelected();
          // this.getColumnListSelected();
        })
        .finally((error) => {
          self.resetColumnsListSelected();
          self.getColumnListSelected();
        });
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    saveSelectedList(dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });
      this.allColumnList = dataFake;
      let data = list.map((item, index) => {
        let response = {
          columnName: item.field,
          enabled: item.enabled,
          position: index,
        };
        if (item.id) {
          response.id = item.id;
        }
        return response;
      });
      const self = this;
      axios
        .post("/api/config/update-column-config", {
          pageType: PAGE_TYPE,
          columnConfig: data,
        })
        .then(
          (response) => {
            console.log("this is the current pagetype:", PAGE_TYPE);
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            self.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            console.log("saveSelectedList", self.allColumnList, response);
          },
          () => {
            self.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            self.$forceUpdate();
          }
        );
    },
    showFilePreviewer(file, fromPopup) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file, fromPopup);
      }
    },
    backToMoldDetail(fromPopup) {
      console.log("back to molds detail");
      // var child = Common.vue.getChild(this.$children, 'mold-details');
      var child = Common.vue.getChild(
        this.$children,
        fromPopup || "category-library-modal"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },

    goToCreateProject() {
      const el = document.getElementById("createProjectId");
      el.classList.add("outline-animation-2");
      setTimeout(() => {
        el.classList.remove("outline-animation-2");
        var child = Common.vue.getChild(this.$children, "project-form-modal");
        if (child != null) {
          child.showModal(null, true);
        }
      }, 500);
      this.selectedCategory = null;
    },
    goToCreateCategory(isInModal) {
      const el = document.getElementById("createCategoryId");
      el.classList.add("outline-animation-2");
      setTimeout(() => {
        el.classList.remove("outline-animation-2");
        var child = Common.vue.getChild(this.$children, "category-form-modal");
        if (child != null) {
          child.showModal(this.noCategoryData || null, true, isInModal);
        }
      }, 500);
      if (!isInModal) {
        this.selectedCategory = null;
      }
    },
    showEditModal() {
      let result = this.listCheckedFull[0];
      this.selectedCategory = { id: "", name: "" };
      if (result) {
        if (result.level == 1) {
          var child = Common.vue.getChild(
            this.$children,
            "category-form-modal"
          );
          if (child != null) {
            child.showModal(result, true, false, true);
          }
        }
        if (result.level == 2) {
          console.log("result result", result);
          this.selectedBrand = result;
          this.isEditBrand = true;
          this.visibleCreateBranch = true;
          // var child = Common.vue.getChild(this.$children, 'project-form-modal');
          // if (child != null) {
          //     const brand = result
          //     const grandParent = this.results.find(
          //         o => !!(o.children || []).find(category => category.id == brand.parentId)
          //     )

          // }
        }
        if (result.level == 3) {
          console.log("result result", result);
          var child = Common.vue.getChild(this.$children, "project-form-modal");
          if (child != null) {
            const product = result;
            const grandParent = this.results.find(
              (o) => o.id === product.grandParentId
            );
            child.showModal(product, true, grandParent);
          }
        }
      }
    },

    showDetailModal(result) {
      console.log("showDetailModal", result);
      let parent = null;
      let fistStep = "index";
      if (result.level == 2) {
        fistStep = "brand-list";
      }
      if (result.level == 3) {
        fistStep = "project-profile";
      }
      var child = Common.vue.getChild(this.$children, "category-library-modal");
      if (child != null) {
        console.log("current date", this.currentDate);
        child.showModal(result, this.currentDate, fistStep);
      }
    },
    deletePop(user) {
      if (user.length <= 1) {
        this.deleteCategory(user[0]);
      } else {
        this.deleteBatch(user, true);
      }
    },
    deleteCategory: function (category) {
      console.log(category, "categorycategory");

      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: category.id,
      };
      const self = this;
      axios
        .delete("/api/categories/" + category.id, param)
        .then(function (response) {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          self.listChecked = [];
          self.isAll = false;
          self.showDropdown = false;
        });
    },
    deleteBatch(user) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
      };
      const self = this;
      axios
        .post("/api/categories/delete-in-batch", param)
        .then(function (response) {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          self.listChecked = [];
          self.isAll = false;
          self.showDropdown = false;
        });
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    enable: function (user) {
      console.log(this.listCheckedFull, "listCheckedFull");
      console.log(user, "user");
      if (user.length <= 1) {
        user[0].enabled = true;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = true;
        });
        this.saveBatch(user, true);
      }
    },
    disable: function (user) {
      console.log(user, "user");
      if (user.length <= 1) {
        user[0].enabled = false;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = false;
        });
        this.saveBatch(user, false);
      }
    },
    saveBatch(user, boolean) {
      console.log(user, "user");
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      const self = this;
      axios
        .post("/api/categories/change-status-in-batch", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    save: function (category) {
      //var param = Common.param(item);
      var param = {
        id: category.id,
        enabled: category.enabled,
      };
      //alert(category.id);
      const self = this;
      axios
        .put("/api/categories/" + category.id + "/enable", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.results.map((value) => value.id);
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.requestParam.status,
        });
      } else {
        this.listChecked = [];
        this.isAllTracked = this.isAllTracked.filter(
          (item) =>
            item.page !== this.requestParam.page &&
            item.status !== this.requestParam.status
        );
      }
      console.log(this.listChecked, "this.listChecked");
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },
    checkSelect: function (id, parentId) {
      const findIndex = this.listCheckedWithParent.findIndex((value) => {
        return value.id === id && value.parentId === parentId;
      });

      return findIndex !== -1;
    },
    hasChecked: function () {
      let isExistedChecked = false;
      Object.keys(this.listCheckedTracked).forEach((status) => {
        Object.keys(this.listCheckedTracked[status]).forEach((page) => {
          if (this.listCheckedTracked[status][page].length > 0) {
            isExistedChecked = true;
          }
        });
      });
      return isExistedChecked;
    },
    check: function (e, result) {
      if (e.target.checked) {
        this.listChecked.push(+e.target.value);
        this.listCheckedFull.push(result);
        this.listCheckedWithParent.push({
          id: result.id,
          parentId: result.parentId,
        });
        Common.setHeightActionBar();
      } else {
        this.listChecked = this.listChecked.filter(
          (value) => value !== result.id
        );
        this.listCheckedFull = this.listCheckedFull.filter((value) => {
          console.log("listCheckedFull", value.id, value.parentId);
          console.log("listCheckedFull 2", result.id, result.parentId);
          if (value.id === result.id && value.parentId === result.parentId) {
            return false;
          } else {
            return true;
          }
        });
        this.listCheckedWithParent = this.listCheckedWithParent.filter(
          (value) => {
            if (value.id === result.id && value.parentId === result.parentId) {
              return false;
            } else {
              return true;
            }
          }
        );
        if (this.listChecked.length === 0) {
          this.isAll = false;
          this.isAllTracked = this.isAllTracked.filter(
            (item) =>
              item.page !== this.requestParam.page &&
              item.status !== this.requestParam.status
          );
        }
      }
      // this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/categories/${this.listChecked[0]}`;
    },
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    hideCalendarPicker(event) {
      if (event.toElement && event.toElement.localName != "img") {
        this.changeShow(null);
      }
    },
    tab: function (status, tabActive) {
      this.total = null;
      this.tabActive = tabActive;
      if (tabActive == "disabled") this.requestParam.enabled = false;
      else this.requestParam.enabled = true;

      this.listChecked = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = status;
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },

    paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      // checking is All
      this.isAll = false;
      var param = Common.param(this.requestParam);
      //var param = this.requestParam;
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();
      this.clearChecked();
      const self = this;
      axios
        .get("/api/categories?sort=id,asc&size=1", {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.noCategoryData =
            response.data.content?.length > 0 ? response.data.content[0] : null;
        });
      axios
        .get("/api/categories?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          if (self.requestParam.categoryId || self.requestParam.productId) {
            self.requestParam.categoryId = null;
            self.requestParam.productId = null;
          }
          self.total = response.data.totalElements;
          self.results = response.data.content;

          console.log("listShowId", self.listShowId);
          self.pagination = Common.getPagingData(response.data);

          document.querySelector("#app .op-list").style.display =
            "table-row-group";
          document.querySelector(".pagination").style.display = "flex";

          var noResult = document.querySelector(".no-results");
          if (noResult != null) {
            if (self.results.length == 0) {
              noResult.className = noResult.className.replace("d-none", "");
            } else {
              noResult.className =
                noResult.className.replace("d-none", "") + " d-none";
            }
          }

          if (self.results && self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
          console.log("this.listCheckedFull", self.listCheckedFull);
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    sortBy: function (type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort();
      }
    },
    sort: function () {
      this.paging(1);
    },
    showRevisionHistory: function (id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "CATEGORY");
      }
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },

    openAssignPartsToProjectModal() {
      this.selectedProject = this.listCheckedFull[0];
      console.log(
        "@openAssignPartsToProjectModal::this.listCheckedFull",
        this.listCheckedFull
      );
      console.log(
        "@openAssignPartsToProjectModal::selectedProject",
        this.selectedProject
      );
      this.showAssignToProject = true;
    },
    closeAssignPartsToProjectModal() {
      this.showAssignToProject = false;
    },
    handleClickSupplier(item) {
      console.log("@:supplier", item);
      const newRoute = {
        route: "list-supplier",
        data: {
          project: item,
          partId: null,
          supplierId: null,
          page: 1,
          dataDetail: null,
        },
      };
      let child = Common.vue.getChild(this.$children, "category-library-modal");
      if (child != null) {
        let date = { ...this.currentDate };
        child.showModal(item, date, "project-profile", true, newRoute);
      }
    },
    handleClickTooling(item) {
      console.log("@click:tooling", item);
      let newRoute = {
        route: "list-mold",
        data: {
          project: item,
          partId: null,
          supplierId: null,
          page: 1,
          dataDetail: null,
        },
      };
      let child = Common.vue.getChild(this.$children, "category-library-modal");
      if (child != null) {
        let date = { ...this.currentDate };
        child.showModal(item, date, "project-profile", true, newRoute);
      }
    },
    handleClickPart(item) {
      console.log("@click:part", item);
      let newRoute = {
        route: "list-part",
        data: {
          project: item,
          supplierId: null,
          moldId: null,
          page: 1,
          dataDetail: null,
        },
      };
      let child = Common.vue.getChild(this.$children, "category-library-modal");
      if (child != null) {
        let date = { ...this.currentDate };
        child.showModal(item, date, "project-profile", true, newRoute);
      }
    },
    handleClickProductProduced(item) {
      console.log("@click:productProduced", item);
    },
    handleChangeDate(date) {
      console.log("date:::::::", date);
      // Object.assign(this.currentDate, date)
      this.currentDate = { ...date };
      this.requestParam.periodType = "WEEKLY";
      this.requestParam.periodValue = date.fromTitle.replaceAll("-", "");
      this.paging(1);
      console.log("currentDate:::::::::", this.currentDate);
      // this.weekKey++
      // this.$forceUpdate()
    },
    initCurrentDate() {
      let today = new Date();
      this.currentDate.from = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay()
      );
      this.currentDate.to = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay() + 6
      );
      this.requestParam.periodType = "WEEKLY";
      const currentWeek = Common.getCurrentWeekNumber(today);
      this.requestParam.periodValue = `${today.getFullYear()}${currentWeek}`;
    },
    uncheckAll() {
      this.listChecked = [];
      this.listCheckedTracked = {};
      this.isAll = false;
      this.isAllTracked = [];
      this.listCheckedFull = [];
      this.isAll = false;
    },
    reloadPage() {
      this.paging(1);
      this.uncheckAll();
      this.selectedBrand = null;
    },
    handleShowSourceSupplier() {
      this.selectedProduct = this.listCheckedFull[0];
      this.showSourceSupplierModal = true;
      this.$forceUpdate();
    },
    handleCloseSourceSupplier() {
      this.selectedProduct = null;
      this.showSourceSupplierModal = false;
    },
    handleCreate({ key }) {
      // this.visibleCreateDropdown = true
      this?.[key]();
    },
    showCreateBranch() {
      if (!this.isBrandModalInCategoryModal) {
        this.selectedBrand = null;
        this.selectedCategory = null;
      }
      this.isEditBrand = false;
      this.visibleCreateBranch = true;
      console.log("showCreateBranch", this.visibleCreateBranch);
    },
    onCreateBrand(category) {
      this.selectedCategory = category ? category : null;
      this.isBrandModalInCategoryModal = true;
      this.showCreateBranch();
    },
    onCloseProduct() {
      this.listChecked = [];
      this.listCheckedFull = [];
      this.selectedCategory = { name: "", id: 0 };
    },
    handleCloseCreateBranch() {
      this.visibleCreateBranch = false;
      this.isEditBrand = false;
      this.listChecked = [];
      this.listCheckedTracked = {};
      this.isAll = false;
      this.isAllTracked = [];
      this.listCheckedFull = [];
      this.reloadPage();
      if (!this.isBrandModalInCategoryModal) {
        this.selectedCategory = { name: "", id: 0 };
      }
      this.isBrandModalInCategoryModal = false;
    },
    onSuccessCreateBrand(newBrand, category) {
      if (newBrand) {
        this.defaultSelectedBrands.push(newBrand);
      }
      if (category) {
        this.selectedCategory = category;
      }
    },
    onSuccessCreateCategory(newCategory) {
      this.selectedCategory = newCategory;
    },
    resetSelectedCategoryAndBrand() {
      this.defaultSelectedBrands = [];
      this.selectedCategory = {};
    },
    handleShowHideRow(index, parentIndex) {
      console.log(`${parentIndex !== null ? parentIndex + "-" : ""}${index}`);
      const find = this.listShowId.find(
        (item) =>
          item == `${parentIndex !== null ? parentIndex + "-" : ""}${index}`
      );
      if (find) {
        this.listShowId = this.listShowId.filter(
          (item) =>
            item != `${parentIndex !== null ? parentIndex + "-" : ""}${index}`
        );
      } else {
        this.listShowId.push(
          `${parentIndex !== null ? parentIndex + "-" : ""}${index}`
        );
      }
      console.log("this.listShowId", this.listShowId);
    },
    isShowingChildren(id) {
      console.log("check", id, this.listShowId.includes(id));
      return this.listShowId.includes(id);
    },
    clearChecked() {
      console.log("clearChecked");
      this.listChecked = [];
      this.listCheckedTracked = {};
      this.isAll = false;
      this.isAllTracked = [];
      this.listCheckedFull = [];
      this.listCheckedWithParent = [];
      console.log("clearChecked", this.listCheckedFull);
    },
  },
  mounted() {
    // 모든 화면이 렌더링된 후 실행합니다.
    const url = new URL(location.href);
    const urlParam = url.hash.replace("#", "").split("?")[1];
    let params = Common.parseParams(urlParam);
    console.log("requestParam", url, urlParam);
    this.requestParam.query = this.searchText;
    if (params.id) {
      this.requestParam.categoryId = params.id;
    }
    if (params.productId) {
      this.requestParam.grandChildId = params.productId;
      // case: no category but have product
      if (!params.id) this.requestParam.categoryId = "0";
    }
    this.setAllColumnList();
    this.initCurrentDate();
    this.paging(1);
    // history.replaceState(null, null, Common.PAGE_URL.CATEGORY);
  },
};
</script>

<style></style>
