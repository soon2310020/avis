<template>
  <div
    id="create-tab-view"
    style="overflow-y: scroll"
    class="modal fade"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">
              {{
                tabViewModalType === "EDIT"
                  ? resources["edit_tab_view"]
                  : resources["create_tab_view"]
              }}
            </span>
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
        <div class="modal-body" style="padding-left: 40px; padding-right: 40px">
          <div class="form-group row line-row-wrapper">
            <label
              for="name"
              class="col-md-3 col-form-label"
              style="margin-right: 0px"
            >
              {{ resources["tab_name"] }}
              <span class="badge-require"></span>
            </label>
            <div class="col-md-9">
              <input
                type="text"
                id="name"
                v-model="tab.name"
                class="form-control"
                :placeholder="resources['tab_name']"
                required
                @keyup="removeErrors('name')"
              />
              <div
                class="error-text"
                :style="{ visibility: errors['name'] ? 'visible' : 'hidden' }"
                v-text="resources['error_input_tab_name']"
              ></div>
            </div>
          </div>
          <div class="form-group row line-row-wrapper">
            <label
              for="name"
              class="col-md-3 col-form-label"
              style="margin-right: 0px"
            >
              {{ resources["share_with"] }}
            </label>
            <div class="col-md-9">
              <div
                class="d-flex align-items-center h-100 justify-content-start"
              >
                <input class="mr-1" type="radio" name="onlyMe" checked />
                <span v-text="resources['only_me']"></span>
              </div>
            </div>
          </div>
          <div class="d-flex align-items-center justify-content-between">
            <common-searchbar
              style="width: 60%"
              :placeholder="resources['search_by_selected_column']"
              :request-param="requestParam"
              :on-search="search"
            ></common-searchbar>
            <div class="d-flex align-items-baseline">
              <div class="add-tooling-wrapper">
                <custom-dropdown-button
                  :title="tab.addItemIdList.length + ' Added'"
                  :is-selected="true"
                  :is-show="toolingVisible"
                  @click="toggledToolingDropdown"
                ></custom-dropdown-button>

                <common-popover
                  @close="handleCloseDropdown"
                  :is-visible="toolingVisible"
                  :style="{
                    width: '180px',
                    position: 'fixed',
                    marginTop: '4px',
                  }"
                >
                  <common-select
                    :style="{ position: 'static', width: '100%' }"
                    :items="toolingOptionsAdd"
                    @on-select="onChangeMethodAdd"
                  ></common-select>
                </common-popover>
              </div>
              <a href="javascript:void(0)" @click="removeAllToolingSelected()"
                >Remove All</a
              >
            </div>
          </div>
          <div class="container content-group">
            <div class="content-group-header">
              <div class="row" v-if="objectType == 'LOCATION'">
                <div class="col-md-3 tab-group-title active">
                  <p v-text="resources['location_name']"></p>
                  <span
                    class="__icon-sort"
                    :class="locationNameSortType"
                    @click="sort('name')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['location_id']"></p>
                  <span
                    class="__icon-sort"
                    :class="locationCodeSortType"
                    @click="sort('locationCode')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['company']"></p>
                  <span
                    class="__icon-sort"
                    :class="companySortType"
                    @click="sort('companyName')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title"></div>
              </div>
              <div v-else class="row">
                <div class="col-md-3 tab-group-title active">
                  <p v-text="resources['tooling_id']"></p>
                  <span
                    class="__icon-sort"
                    :class="toolingSortType"
                    @click="sort('tooling')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['part']"></p>
                  <span
                    class="__icon-sort"
                    :class="partSortType"
                    @click="sort('part')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title">
                  <p v-text="resources['company']"></p>
                  <span
                    class="__icon-sort"
                    :class="companySortType"
                    @click="sort('company')"
                  ></span>
                </div>
                <div class="col-md-3 tab-group-title"></div>
              </div>
            </div>
            <div class="content-group-wrapper">
              <template v-if="objectType === 'LOCATION'">
                <div
                  v-for="(location, locationINDEX) in locationList"
                  :key="location.id"
                  class="row"
                >
                  <div class="col-md-3">
                    <div class="tab-group-item">
                      <div v-if="location.name && location.name.length > 17">
                        <a-tooltip placement="bottom">
                          <template slot="title">
                            <div style="padding: 6px; font-size: 13px">
                              {{ location.name }}
                            </div>
                          </template>
                          <span>
                            {{ truncateText(location.name, 17) }}
                          </span>
                        </a-tooltip>
                      </div>
                      <span v-else>
                        {{ truncateText(location.name, 17) }}
                      </span>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="tab-group-item">
                      <div
                        v-if="
                          location.locationCode &&
                          location.locationCode.length > 17
                        "
                      >
                        <a-tooltip placement="bottom">
                          <template slot="title">
                            <div style="padding: 6px; font-size: 13px">
                              {{ location.locationCode }}
                            </div>
                          </template>
                          <span>
                            {{ truncateText(location.locationCode, 17) }}
                          </span>
                        </a-tooltip>
                      </div>
                      <span v-else>
                        {{ truncateText(location.locationCode, 17) }}
                      </span>
                    </div>
                  </div>

                  <div class="col-md-3">
                    <div
                      v-if="
                        location.companyName && location.companyName.length > 17
                      "
                    >
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ location.companyName }}
                          </div>
                        </template>
                        <div class="font-size-14">
                          {{ truncateText(location.companyName, 17) }}
                        </div>
                      </a-tooltip>
                    </div>
                    <div class="font-size-14" v-else>
                      {{ truncateText(location.companyName, 17) }}
                    </div>

                    <div
                      v-if="
                        location.companyCode && location.companyCode.length > 17
                      "
                    >
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ location.companyCode }}
                          </div>
                        </template>
                        <div class="small text-muted font-size-11-2">
                          {{ truncateText(location.companyCode, 17) }}
                        </div>
                      </a-tooltip>
                    </div>
                    <div class="small text-muted font-size-11-2" v-else>
                      {{ truncateText(location.companyCode, 17) }}
                    </div>
                  </div>
                  <div class="col-md-3 action-column">
                    <div
                      v-if="checkToolingSelected(location.id)"
                      class="d-flex align-items-center h-100 justify-content-start"
                      @click="addToolingToTab(location.id)"
                    >
                      <span style="margin-right: 4px">Add to tab</span>
                      <img src="/images/icon/add-circle-white.svg" alt="" />
                    </div>
                    <div
                      v-else
                      class="d-flex align-items-center h-100 justify-content-start"
                      @click="removeToolingFromTab(location.id)"
                    >
                      <span style="margin-right: 4px">Remove from tab</span>
                      <img src="/images/icon/sub-circle-white.svg" alt="" />
                    </div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div
                  v-for="(tooling, toolingIndex) in toolingList"
                  :key="tooling.id"
                  class="row"
                >
                  <div class="col-md-3">
                    <div class="tab-group-item">
                      <div
                        v-if="
                          tooling.equipmentCode &&
                          tooling.equipmentCode.length > 17
                        "
                      >
                        <a-tooltip placement="bottom">
                          <template slot="title">
                            <div style="padding: 6px; font-size: 13px">
                              {{ tooling.equipmentCode }}
                            </div>
                          </template>
                          <div>
                            {{ truncateText(tooling.equipmentCode, 17) }}
                          </div>
                        </a-tooltip>
                      </div>
                      <div v-else>
                        {{ truncateText(tooling.equipmentCode, 17) }}
                      </div>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="tab-group-item">
                      <mold-parts-dropdown-view
                        :parts="tooling.parts"
                        :mold="tooling"
                        :index="toolingIndex"
                        :show-part-chart="showPartChart"
                      ></mold-parts-dropdown-view>
                    </div>
                  </div>

                  <div class="col-md-3">
                    <div
                      v-if="
                        tooling.companyName && tooling.companyName.length > 17
                      "
                    >
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ tooling.companyName }}
                          </div>
                        </template>
                        <a
                          class="font-size-14"
                          href="#"
                          @click.prevent="
                            showCompanyDetailsById(tooling.companyIdByLocation)
                          "
                        >
                          {{ truncateText(tooling.companyName, 17) }}
                        </a>
                      </a-tooltip>
                    </div>
                    <a
                      v-else
                      class="font-size-14"
                      href="#"
                      @click.prevent="
                        showCompanyDetailsById(tooling.companyIdByLocation)
                      "
                    >
                      {{ truncateText(tooling.companyName, 17) }}
                    </a>

                    <div
                      v-if="
                        tooling.companyCode && tooling.companyCode.length > 17
                      "
                    >
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ tooling.companyCode }}
                          </div>
                        </template>
                        <div class="small text-muted font-size-11-2">
                          {{ truncateText(tooling.companyCode, 17) }}
                        </div>
                      </a-tooltip>
                    </div>
                    <div v-else class="small text-muted font-size-11-2">
                      {{ truncateText(tooling.companyCode, 17) }}
                    </div>
                  </div>
                  <div class="col-md-3 action-column">
                    <div
                      v-if="checkToolingSelected(tooling.id)"
                      class="d-flex align-items-center h-100 justify-content-start"
                      @click="addToolingToTab(tooling.id)"
                    >
                      <span style="margin-right: 4px">Add to tab</span>
                      <img src="/images/icon/add-circle-white.svg" alt="" />
                    </div>
                    <div
                      v-else
                      class="d-flex align-items-center h-100 justify-content-start"
                      @click="removeToolingFromTab(tooling.id)"
                    >
                      <span style="margin-right: 4px">Remove from tab</span>
                      <img src="/images/icon/sub-circle-white.svg" alt="" />
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
          <div class="row">
            <div class="col-md-12">
              <ul
                v-if="pagination.totalPages > 0"
                class="pagination d-flex justify-content-end mt-2"
              >
                <div class="modal-body__content__footer">
                  <span>{{
                    `${pagination.pageable.pageNumber + 1} of ${
                      pagination.totalPages
                    }`
                  }}</span>
                </div>
                <div class="paging__arrow">
                  <a
                    href="javascript:void(0)"
                    class="paging-button"
                    :class="{
                      'inactive-button': pagination.pageable.pageNumber < 1,
                    }"
                    @click="
                      objectType == 'LOCATION'
                        ? getLocationList(pagination.pageable.pageNumber)
                        : getToolingList(pagination.pageable.pageNumber)
                    "
                  >
                    <img src="/images/icon/category/paging-arrow.svg" alt="" />
                  </a>
                  <a
                    href="javascript:void(0)"
                    class="paging-button"
                    :class="{
                      'inactive-button':
                        pagination.pageable.pageNumber + 1 >=
                        pagination.totalPages,
                    }"
                    @click="
                      objectType == 'LOCATION'
                        ? getLocationList(pagination.pageable.pageNumber + 2)
                        : getToolingList(pagination.pageable.pageNumber + 2)
                    "
                  >
                    <img
                      src="/images/icon/category/paging-arrow.svg"
                      style="transform: rotate(180deg)"
                      alt=""
                    />
                  </a>
                </div>
              </ul>
            </div>
          </div>
          <div class="d-flex modal__footer">
            <base-button
              id="createProjectId"
              :style="{ padding: '4px 8px', marginRight: '8px' }"
              @click="handleClose"
              :size="'medium'"
              :type="'cancel'"
              :level="'secondary'"
            >
              <span>{{
                tabViewModalType === "EDIT" ? "Go Back" : "Cancel"
              }}</span>
            </base-button>
            <a
              @click="saveData"
              id="createTabView"
              href="javascript:void(0)"
              class="btn-custom btn-custom-primary"
            >
              <span>{{
                tabViewModalType === "EDIT" ? "Save Edit" : "Create Tab View"
              }}</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
const API_BASE_TOL_STP = Common.getApiBase().MOLD;
const API_BASE_PLT_STP = Common.getApiBase().LOCATION;
const API_BASE_TAB_STP = Common.getApiBase().TAB;
const DEFAULT_PARAMS = {
  query: "",
  sort: "lastShotAt,desc",
  page: 1,
  size: 20,
  toolingStatus: [
    "IN_PRODUCTION",
    "IDLE",
    "INACTIVE",
    "SENSOR_OFFLINE",
    "SENSOR_DETACHED",
    "NO_SENSOR",
    "ON_STANDBY",
  ],
  counterStatus: ["INSTALLED", "NOT_INSTALLED", "DETACHED"],
  filterCode: "COMMON",
  tabName: "All",
};
module.exports = {
  props: {
    resources: Object,
    objectType: String,
    pageType: String,
  },
  components: {
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
  },
  data() {
    return {
      requestParam: {
        filterCode: "COMMON",
        query: "",
        status: "All",
        sort: "equipmentCode,asc",
        page: 1,
        isModalSelected: true,
      },
      searchedTabList: [],
      toolingSortType: "asc",
      partSortType: "asc",
      companySortType: "asc",
      locationNameSortType: "asc",
      locationCodeSortType: "asc",
      tab: {
        id: null,
        name: "",
        isDuplicate: false,
        removeItemIdList: [],
        addItemIdList: [],
      },
      errors: [],
      tabViewModalType: "CREATE",
      pagination: [],
      toolingVisible: false,
      toolingOptionsAdd: [
        {
          title: "",
          name: "page",
        },
        {
          title: "",
          name: "all",
        },
      ],
      allToolingList: [],
      toolingPerPageList: [],
      choosedToolings: [],
      allLocationList: [],
      chosenLocationList: [],
      locationPerPageList: [],
    };
  },
  computed: {
    toolingList() {
      console.log(
        "toolingList",
        this.searchedTabList.filter((tab) => !tab.defaultTab)
      );
      return this.searchedTabList.filter((tab) => !tab.defaultTab);
    },
    locationList() {
      return this.searchedTabList.filter((tab) => !tab.defaultTab);
    },
  },
  // created() {
  //   // TODO: fetch tooling error
  //   this.getToolingList(1);
  //   this.getAllToolingList();
  // },
  mounted() {},
  methods: {
    truncateText(text, length) {
      if (text && text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    toggledToolingDropdown(value) {
      this.toolingVisible = value;
    },
    handleCloseDropdown() {
      this.toggledToolingDropdown(false);
    },
    onChangeMethodAdd(value) {
      if (value.name === "page") {
        if (this.objectType == "LOCATION") {
          this.locationPerPageList.forEach((value) => {
            if (this.tab.addItemIdList.indexOf(value.id) === -1)
              this.tab.addItemIdList.push(value.id);
          });
        } else {
          // this.tab.addItemIdList.push(this.toolingPerPageList.map(i => i.id))
          this.toolingPerPageList.forEach((value) => {
            if (this.tab.addItemIdList.indexOf(value.id) === -1)
              this.tab.addItemIdList.push(value.id);
          });
        }
      } else {
        if (this.objectType == "LOCATION") {
          this.tab.addItemIdList = this.allLocationList.map((i) => i.id);
        } else {
          this.tab.addItemIdList = this.allToolingList.map((i) => i.id);
        }
      }
      this.$forceUpdate();
    },
    removeAllToolingSelected() {
      this.tab.addItemIdList = [];
      if (this.tabViewModalType === "EDIT") {
        this.tab.removeItemIdList = this.choosedToolings;
      }
      this.$forceUpdate();
    },
    showCompanyDetailsById(company) {
      this.$emit("show-company-details", company);
    },
    showPartChart(part, mold) {
      this.$emit("show-part-chart", part, mold);
    },
    dimissModal() {
      $("#create-tab-view").modal("hide");
    },
    checkToolingSelected(tooling) {
      return !this.tab.addItemIdList.includes(tooling);
    },
    addToolingToTab(tooling) {
      this.tab.addItemIdList.push(tooling);
      this.checkToolingSelected(tooling);
      this.$forceUpdate();
    },
    removeToolingFromTab(tooling) {
      this.tab.addItemIdList = this.tab.addItemIdList.filter(
        (t) => t !== tooling
      );
      this.tab.removeItemIdList.push(tooling);
      this.checkToolingSelected(tooling);
      this.$forceUpdate();
    },
    handleClose() {
      this.$emit("close");
      this.clearData();
    },
    saveData() {
      const el = document.getElementById("createTabView");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        if (!self.validForm()) {
          self.$forceUpdate();
          return;
        }
        self.saveTab();
        el.classList.remove("primary-animation");
      }, 700);
    },
    clearData() {
      this.tab = {
        id: null,
        name: "",
        isDuplicate: false,
        removeItemIdList: [],
        addItemIdList: [],
      };
      this.errors = [];
      this.choosedToolings = [];
      this.requestParam = {
        query: "",
        status: "All",
        sort: "equipmentCode,asc",
        page: 1,
      };
      $("#create-tab-view").modal("hide");
    },
    async saveTab() {
      const tab = this.tab;
      const isCreate = this.tabViewModalType !== "EDIT";
      const params = {
        id: isCreate ? null : tab.id,
        name: tab.name,
        isDuplicate: false,
        removeItemIdList: isCreate ? [] : tab.removeItemIdList,
        addItemIdList: tab.addItemIdList,
      };
      console.log(params, "params");
      if (this.objectType == "LOCATION") {
        //   axios
        //     .post("/api/tab-table/save-tab-table-location", params)
        //     .then((res) => {
        //       this.handleClose();
        //       if (isCreate) {
        //         this.$emit("create-success", res.data.data, {
        //           title: "Success!",
        //           content: `Your tab view has been created`,
        //         });
        //       } else {
        //         this.$emit("edit-success", res.data.data, {
        //           title: "Success!",
        //           content: `Your tab view has been edited`,
        //         });
        //       }
        //     });
        const dataBody = {
          name: tab.name,
          selectedIds: tab.addItemIdList,
        };
        console.log("saveTab:::dataBody", dataBody);
        try {
          if (isCreate) {
            const { data: plantTabData } = await axios.post(
              `${API_BASE_TAB_STP}/LOCATION/one`,
              dataBody
            );
            this.handleClose();
            this.$emit("create-success", plantTabData, {
              title: "Success!",
              content: `Your tab view has been created`,
            });
          } else {
            const { data: plantTabData } = await axios.put(
              `${API_BASE_TAB_STP}/LOCATION/${tab.id}`,
              dataBody
            );
            this.$emit("edit-success", plantTabData, {
              title: "Success!",
              content: `Your tab view has been edited`,
            });
          }
        } catch (error) {
          console.log("tab-view-plant-modal:::saveTab:::error", error);
        }
      } else {
        const dataBody = {
          name: tab.name,
          selectedIds: tab.addItemIdList,
        };
        console.log("saveTab:::dataBody", dataBody);
        try {
          if (isCreate) {
            const { data: toolingTabData } = await axios.post(
              `${API_BASE_TAB_STP}/TOOLING/one`,
              dataBody
            );
            this.handleClose();
            this.$emit("create-success", toolingTabData, {
              title: "Success!",
              content: `Your tab view has been created`,
            });
          } else {
            const { data: toolingTabData } = await axios.put(
              `${API_BASE_TAB_STP}/TOOLING/${tab.id}`,
              dataBody
            );
            this.$emit("edit-success", toolingTabData, {
              title: "Success!",
              content: `Your tab view has been edited`,
            });
          }
        } catch (error) {
          console.log("tab-view-tooling-modal:::saveTab:::error", error);
        }
      }
    },
    validForm() {
      if (!this.tab.name) {
        this.errors["name"] = true;
        return false;
      }
      return true;
    },
    removeErrors(type) {
      this.errors[type] = false;
      this.$forceUpdate();
    },
    async getListToolingSelectedByTab(tabName) {
      const requestParam = {
        ...DEFAULT_PARAMS,
        tabName,
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_TOL_STP}?${param}`);
      this.tab.addItemIdList = res.data.content.map((i) => i.id);
      if (this.tabViewModalType === "EDIT") {
        this.choosedToolings = res.data.content.map((i) => i.id);
      }
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getListLocationSelectedByTab(tabName) {
      // // OLD API
      // const res = await axios.get(
      //   `/api/locations?tabId=${
      //     !this.tab.id ? "" : this.tab.id
      //   }&id=&query=&status=${!status ? "All" : status}&page=1&size=9999`
      // );

      // NEW API
      const requestParam = {
        query: "",
        sort: "id,desc",
        page: 1,
        size: 9999,
        tabName,
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);
      this.tab.addItemIdList = res.data.content.map((i) => i.id);
      if (this.tabViewModalType === "EDIT") {
        this.choosedToolings = res.data.content.map((i) => i.id);
      }
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getAllToolingList() {
      const param = Common.param({
        ...DEFAULT_PARAMS,
        query: this.requestParam.query,
        frameType: this.pageType,
      });
      const path = "/api/batch/all-ids-for-full-frame";
      const url = new URL(path, location.origin);
      url.search = param;
      const res = await axios.get(url);
      this.allToolingList = res.data.data.map((i) => ({ id: i }));
    },
    async getAllLocationList() {
      // // OLD API
      // const res = await axios.get(
      //   `/api/locations?tabId=&query=${
      //     this.requestParam.query || ""
      //   }&status=Enabled&sort=${this.requestParam.sort}&page=1&size=99999`
      // );

      // NEW API
      const requestParam = {
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        page: 1,
        size: 99999,
        tabName: "Enabled",
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);
      this.allLocationList = res.data.content;
    },
    async getListToolingTabType(tabName) {
      const requestParam = {
        ...DEFAULT_PARAMS,
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        tabName,
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_TOL_STP}?${param}`);

      this.tab.addItemIdList = res.data.content.map((i) => i.id);
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getListLocationTabType(tabName) {
      // // OLD API
      // const res = await axios.get(
      //   `/api/locations?tabId=${
      //     !this.tab.id ? "" : this.tab.id
      //   }&page=1&size=9999`
      // );

      // NEW API
      const requestParam = {
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        page: 1,
        size: 9999,
        tabName,
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);

      this.tab.addItemIdList = res.data.content.map((i) => i.id);
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getListLocationTabDefault(tabName) {
      // // OLD API
      // const res = await axios.get(
      //   `/api/locations?status=${status}&page=1&size=9999`
      // );

      // NEW API
      const requestParam = {
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        page: 1,
        size: 9999,
        tabName,
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);

      this.tab.addItemIdList = res.data.content.map((i) => i.id);
      this.$forceUpdate();
      $("#create-tab-view").modal("show");
    },
    async getAllToolingListSearching(pageNumber) {
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      const param = Common.param({
        ...DEFAULT_PARAMS,
        page: this.requestParam.page,
        size: 20,
        query: this.requestParam.query,
      });
      const url = new URL(API_BASE_TOL_STP, location.origin);
      url.search = param;
      const res = await axios.get(url);
      this.toolingOptionsAdd[0].title =
        "Add Page (" +
        res.data.numberOfElements +
        (res.data.numberOfElements > 1 ? " items)" : " item)");
      this.toolingOptionsAdd[1].title =
        "Add All (" +
        res.data.totalElements +
        (res.data.totalElements > 1 ? " items)" : " item)");
      this.toolingPerPageList = res.data.content;
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    async getAllLocationListSearching(pageNumber) {
      // // OLD API
      // this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      // const res = await axios.get(
      //   `/api/locations?tabId=&id=&query=${this.requestParam.query}&status=All&sort=${this.requestParam.sort}&page=${this.requestParam.page}`
      // );

      // NEW API
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;
      const requestParam = {
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        page: this.requestParam.page,
        size: 20,
        tabName: "Enabled",
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);

      this.toolingOptionsAdd[0].title =
        "Add Page (" +
        res.data.numberOfElements +
        (res.data.numberOfElements > 1 ? " items)" : " item)");
      this.toolingOptionsAdd[1].title =
        "Add All (" +
        res.data.totalElements +
        (res.data.totalElements > 1 ? " items)" : " item)");
      this.locationPerPageList = res.data.content;
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    async getToolingList(pageNumber) {
      this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      const param = Common.param({
        ...DEFAULT_PARAMS,
        page: this.requestParam.page,
        size: 20,
        query: this.requestParam.query,
        sort: this.requestParam.sort,
      });
      const url = new URL(API_BASE_TOL_STP, location.origin);
      url.search = param;
      const res = await axios.get(url);
      this.toolingPerPageList = res.data.content;
      this.toolingOptionsAdd[0].title =
        "Add Page (" +
        res.data.numberOfElements +
        (res.data.numberOfElements > 1 ? " items)" : " item)");
      this.toolingOptionsAdd[1].title =
        "Add All (" +
        res.data.totalElements +
        (res.data.totalElements > 1 ? " items)" : " item)");
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    async getLocationList(pageNumber) {
      // // OLD API
      // this.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      // const res = await axios.get(
      //   `/api/locations?tabId=&id=&query=${this.requestParam.query}&status=${this.requestParam.status}&sort=${this.requestParam.sort}&page=${this.requestParam.page}`
      // );

      // NEW API
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;
      const requestParam = {
        query: this.requestParam.query,
        sort: this.requestParam.sort,
        page: this.requestParam.page,
        size: 20,
        tabName: "Enabled",
      };
      const param = Common.param(requestParam);
      const res = await axios.get(`${API_BASE_PLT_STP}?${param}`);

      this.locationPerPageList = res.data.content;
      this.toolingOptionsAdd[0].title =
        "Add Page (" + res.data.numberOfElements + " items)";
      this.toolingOptionsAdd[1].title =
        "Add All (" + res.data.totalElements + " items)";
      this.searchedTabList = res.data.content;
      this.pagination = res.data;
    },
    async showCreateTabView(type, tab) {
      this.clearData();
      if (this.objectType === "LOCATION") {
        this.requestParam.sort = "name,asc";
        this.requestParam.status = "Enabled";
        await this.getLocationList(1);
        await this.getAllLocationList();
      } else {
        await this.getToolingList(1);
        await this.getAllToolingList();
      }
      this.tabViewModalType = type;
      if (type === "EDIT") {
        this.tab = tab;
        this.tab.addItemIdList = [];
        this.tab.removeItemIdList = [];
        if (this.objectType == "LOCATION") {
          this.getListLocationSelectedByTab(tab.name);
        } else {
          this.getListToolingSelectedByTab(tab.name);
        }
      } else if (type === "DUPLICATE") {
        this.tab.id = tab.id;
        if (this.objectType == "LOCATION") {
          if (!tab.id) {
            let tabStatus = "active";
            switch (tab.name) {
              case "In-house":
                tabStatus = "IN_HOUSE";
                break;
              case "Toolmaker":
                tabStatus = "TOOL_MAKER";
                break;
              default:
                tabStatus = _.upperCase(tab.name);
                break;
            }
            this.getListLocationTabDefault(tabStatus);
            return;
          }
          this.getListLocationTabType(tab.id);
          return;
        }
        if (["DIGITAL", "NON_DIGITAL"].includes(tab.name)) {
          this.getListToolingTabType(tab.name);
        } else if (tab.name === "DELETED") {
          this.getListToolingSelectedByTab(tab.name, true);
        } else {
          this.getListToolingSelectedByTab(tab.name);
        }
      } else {
        // this.searchedTabList = this.toolingPerPageList;
        $("#create-tab-view").modal("show");
      }
    },
    async search() {
      if (this.objectType == "LOCATION") {
        await this.getAllLocationList();
        await this.getAllLocationListSearching(1);
      } else {
        await this.getAllToolingList();
        await this.getAllToolingListSearching(1);
      }
    },
    sort: function (type) {
      if (this.objectType == "LOCATION") {
        switch (type) {
          case "name":
            this.locationNameSortType =
              this.locationNameSortType === "asc" ? "desc" : "asc";
            this.requestParam.sort = "name," + this.locationNameSortType;
            break;
          case "locationCode":
            this.locationCodeSortType =
              this.locationCodeSortType === "asc" ? "desc" : "asc";
            this.requestParam.sort =
              "locationCode," + this.locationCodeSortType;
            break;
          case "company":
            this.companySortType =
              this.companySortType === "asc" ? "desc" : "asc";
            this.requestParam.sort =
              "location.companyName," + this.companySortType;
            break;
        }
        this.getLocationList(1);
        return;
      }
      if (type === "tooling") {
        this.toolingSortType = this.toolingSortType === "asc" ? "desc" : "asc";
        this.requestParam.sort = "equipmentCode," + this.toolingSortType;
      } else if (type === "company") {
        this.companySortType = this.companySortType === "asc" ? "desc" : "asc";
        this.requestParam.sort =
          "location.company.name," + this.companySortType;
      } else {
        this.partSortType = this.partSortType === "asc" ? "desc" : "asc";
        this.requestParam.sort = "part," + this.partSortType;
      }
      this.getToolingList(1);
    },
  },
};
</script>
<style scoped>
.add-tooling-wrapper .dropdown-wrap .dropdown-list li {
  margin: unset !important;
}
.add-tooling-wrapper .dropdown-wrap label {
  margin-bottom: 0 !important;
}
.add-tooling-wrapper .dropdown-wrap {
  padding: 3px !important;
}
.add-tooling-wrapper {
  margin-right: 12px;
}
.head-line {
  left: 0;
}
.error-text {
  font-size: 11px;
  color: #ef4444;
  text-align: left;
}
.content-group {
  margin-top: 20px;
  border: solid 1px #d0d0d0;
  padding: unset;
  border-radius: 3px;
  overflow-x: hidden;
}
.tab-group-title {
  color: #4b4b4b;
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  height: 50px;
  background-color: #f5f8ff;
  display: flex;
  align-items: center;
}
.tab-group-title p {
  margin-bottom: 0px;
}
.tab-group-item {
  padding-top: 10px;
  padding-bottom: 10px;
  color: #4b4b4b;
  font-size: 15px;
  display: flex;
  align-items: center;
  height: 45px;
  width: 90%;
}
.image-duplicate {
  margin-left: auto;
}
.__icon-sort {
  content: "";
  background: url("../images/icon/new/sort-down-icon-active.svg");
  background-size: 100% 100%;
  transform: rotate(180deg);
  width: 20px;
  height: 20px;
}
.desc.__icon-sort {
  transform: rotate(180deg);
}
.asc.__icon-sort {
  transform: rotate(0deg);
}
.modal-title {
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  color: #4b4b4b;
}
.modal__footer {
  margin-top: 20px;
  justify-content: flex-end;
}
.modal__footer__button {
  margin-left: 8px;
}
.content-group-wrapper {
  height: 315px;
  overflow-y: scroll;
  overflow-x: hidden;
  padding-left: 34px;
  color: #4b4b4b;
}
.content-group-wrapper > .row {
  margin-left: -31px;
  padding-left: 19px;
}
.content-group-wrapper > .row:nth-of-type(odd) {
  background: #fbfcfd;
}
.content-group-header {
  background-color: rgb(245, 248, 255);
  padding-left: 34px;
}
.action-column > div {
  cursor: pointer;
}
.custom-modal-title {
  border-radius: 6px 6px 0px 0px;
  background: rgb(245, 248, 255);
  color: rgb(75, 75, 75);
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 8px 15px 0px 0px;
  align-items: center;
  margin-bottom: 0px;
}
.modal-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 30px;
  margin-bottom: 12px;
  font-size: 16px;
}
</style>
