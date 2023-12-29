<template>
  <div id="manage-tab-view" class="modal fade" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg" style="margin: 9vh auto" role="document">
      <div class="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <span class="modal-title-text">
              {{ resources["manage_tab_view"] }}
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
        <div
          class="modal-body"
          style="padding-left: 40px; padding-right: 40px; min-height: 70vh"
        >
          <common-searchbar
            style="width: 60%"
            :placeholder="resources['search_by_selected_column']"
            :request-param="requestParam"
            :on-search="search"
          ></common-searchbar>
          <div class="container content-group">
            <div class="row">
              <div class="col-md-6 tab-group-title active">
                <p v-text="resources['standard']"></p>
                <span
                  class="__icon-sort"
                  :class="standardSortType"
                  @click="sort('standard')"
                ></span>
              </div>
              <div class="col-md-6 tab-group-title">
                <p v-text="resources['created_by_me']"></p>
                <span
                  class="__icon-sort"
                  :class="requestByMeSortType"
                  @click="sort('created')"
                ></span>
              </div>
            </div>
            <div class="row">
              <div
                class="col-md-6"
                style="padding-left: 0px; padding-right: 0px"
              >
                <div
                  class="tab-group-item"
                  v-for="(tab, index) in standardTabList"
                  :class="{ 'odd-column': index % 2 === 1 }"
                >
                  <div class="col-md-8">
                    {{ convertCodeToName(tab) }}
                  </div>
                  <div class="col-md-4">
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
                            box-shadow: 0px 0px 4px 1px #e5dfdf;
                          "
                        >
                          Duplicate
                          <div class="custom-arrow-tooltip"></div>
                        </div>
                      </template>
                      <a
                        v-if="tab.name"
                        @click="openDuplicate(tab)"
                        href="javascript:void(0)"
                        class="btn-custom btn-outline-custom-primary action-btn"
                      >
                        <img
                          width="11"
                          height="11"
                          class="image-duplicate"
                          src="/images/new-feature/duplicate.svg"
                        />
                      </a>
                    </a-tooltip>
                  </div>
                </div>
              </div>
              <div class="col-md-6" style="padding-left: 0; padding-right: 0">
                <div
                  class="tab-group-item"
                  v-for="(tab, index) in createByMeTabList"
                  :class="{ 'odd-column': index % 2 === 1 }"
                >
                  <div class="col-md-8">
                    <div
                      v-if="tab.name && tab.name.length > 20"
                      class="part-name-drop-down one-line"
                    >
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ tab.name }}
                          </div>
                        </template>
                        <span>
                          {{ convertCodeToName(tab) }}
                        </span>
                      </a-tooltip>
                    </div>
                    <span v-else>
                      {{ convertCodeToName(tab) }}
                    </span>
                  </div>
                  <div class="col-md-4">
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
                          Edit
                          <div class="custom-arrow-tooltip"></div>
                        </div>
                      </template>
                      <a
                        v-if="tab.name"
                        style="margin-right: 14px"
                        @click="openEdit(tab)"
                        href="javascript:void(0)"
                        class="btn-custom btn-outline-custom-primary action-btn"
                      >
                        <img
                          width="11"
                          height="11"
                          class="image-duplicate"
                          src="/images/new-feature/pencil.svg"
                        />
                      </a>
                    </a-tooltip>
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
                            box-shadow: 0px 0px 4px 1px #e5dfdf;
                          "
                        >
                          Delete
                          <div class="custom-arrow-tooltip"></div>
                        </div>
                      </template>
                      <a
                        v-if="tab.name"
                        @click="checkDeleteTab(tab)"
                        href="javascript:void(0)"
                        class="btn-custom btn-outline-custom-primary action-btn"
                      >
                        <img
                          width="11"
                          height="11"
                          class="image-duplicate"
                          src="/images/new-feature/delete.svg"
                        />
                      </a>
                    </a-tooltip>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="matching-modal" v-if="deleteTabAlert">
      <div class="modal-wrapper duplicated-modal" style="color: #4b4b4b">
        <div class="modal-container">
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="warning-title">
                <img
                  src="/images/icon/warning-2.svg"
                  width="16"
                  height="16"
                  alt="warning"
                />
                <p>Delete Tab Permanently?</p>
              </div>
              <div class="duplicated-title" style="font-size: 19px">
                <p>
                  Tooling data from deleted tab can still be accessible via
                  eMoldino standard tabs.
                </p>
                <div class="d-flex align-items-center my-2">
                  <input
                    v-model="isDontShowWarningAgain"
                    style="width: 10px; height: 10px"
                    type="checkbox"
                  />
                  <span
                    class="check-box-title"
                    style="margin-left: 7px"
                    v-text="resources['dont_show_warning_again']"
                  ></span>
                </div>
              </div>
              <div class="duplicated-action">
                <button
                  class="btn btn-back"
                  @click="closeWarning"
                  v-text="resources['no_delete']"
                ></button>
                <button
                  class="btn btn-delete"
                  style="margin-left: 5px"
                  @click="deleteTab"
                  v-text="resources['yes_delete']"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="manageToast"
    ></toast-alert>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    objectType: String,
    triggerUpdate: Number,
  },
  data() {
    return {
      requestParam: {
        query: "",
      },
      allTabList: [],
      searchedTabList: [],
      standardSortType: "asc",
      requestByMeSortType: "asc",
      standardTabList: [],
      createByMeTabList: [],
      deleteTabAlert: false,
      tabDelete: {},
      toastAlert: {
        value: false,
        title: "",
        content: "",
      },
      isDontShowWarningAgain: false,
      emptyList: ["", "", "", "", "", "", "", "", "", "", "", "", ""],
    };
  },
  components: {
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
  },
  computed: {},
  watch: {
    triggerUpdate(newVal) {
      if (newVal) {
        this.show();
      }
    },
    searchedTabList: {
      handler: function () {
        let __standardTabList = this.searchedTabList.filter(
          (tab) => tab.isDefaultTab
        );
        let __createByMeTabList = this.searchedTabList.filter(
          (tab) => !tab.isDefaultTab
        );
        const maxLength =
          __createByMeTabList.length > 4 ? __createByMeTabList.length : 4;
        if (__standardTabList.length <= 13) {
          __standardTabList = __standardTabList
            .concat(this.emptyList)
            .splice(0, maxLength);
        }
        if (__createByMeTabList.length <= 13) {
          console.log(__createByMeTabList, "__createByMeTabList");
          console.log(this.emptyList, "this.emptyList");
          __createByMeTabList = __createByMeTabList
            .concat(this.emptyList)
            .splice(0, maxLength);
          console.log(this.createByMeTabList, "this.createByMeTabList 1");
        }

        if (__standardTabList.length !== __createByMeTabList.length) {
          let distantLength = Math.abs(
            __standardTabList.length - __createByMeTabList.length
          );
          if (__standardTabList.length < __createByMeTabList.length) {
            __standardTabList = __standardTabList.concat(
              Array.from({ length: distantLength }).map((a) => "")
            );
          } else {
            __createByMeTabList = __createByMeTabList.concat(
              Array.from({ length: distantLength }).map((a) => "")
            );
            console.log(this.createByMeTabList, "this.createByMeTabList 2");
          }
        }
        this.standardTabList = __standardTabList.filter(
          (tabData) => tabData.name !== "DELETED" && tabData.name !== "Disabled"
        );
        this.createByMeTabList = __createByMeTabList;
      },
      immediate: true,
    },
  },
  methods: {
    closeToastAlert() {
      this.toastAlert.value = false;
    },
    showToastAlert(toast) {
      this.toastAlert.value = true;
      this.toastAlert.title = toast.title;
      this.toastAlert.content = toast.content;
    },
    dimissModal() {
      $("#manage-tab-view").modal("hide");
    },
    openEdit(tab) {
      this.$emit("open-edit", tab);
      // $("#manage-tab-view").modal("hide");
    },
    openDuplicate(tab) {
      this.$emit("open-duplicate", tab);
      // $("#manage-tab-view").modal("hide");
    },
    truncateText(text, size) {
      if (text) {
        const trimText = text.trim();
        if (trimText) {
          if (trimText.length > size) {
            return text.slice(0, size) + "...";
          }
          return trimText;
        }
      }
      return "";
    },
    convertCodeToName(tabItem) {
      if (tabItem.isDefaultTab) {
        if (tabItem.name === "DELETED") {
          return "Disabled";
        } else {
          return (
            tabItem.name.charAt(0).toUpperCase() +
            tabItem.name.slice(1).toLocaleLowerCase().replace("_", " ")
          );
        }
      } else {
        return this.truncateText(tabItem.name, 20);
      }
    },
    show: function () {
      axios
        .get("/api/tab-table/by-current-user?objectType=" + this.objectType)
        .then((res) => {
          this.allTabList = res.data.data;
          this.searchedTabList = res.data.data;
          this.checkIsShowWarningAgain();
          $("#manage-tab-view").modal("show");
        });
    },
    search: function () {
      console.log("search");
      const str = this.requestParam.query.trim();
      if (!str) {
        this.searchedTabList = this.allTabList;
        return;
      }
      const options = {
        keys: ["name"],
      };
      let searcher = new Fuse(this.allTabList, options);
      this.searchedTabList = searcher.search(str);
    },
    sort: function (type) {
      console.log("type");
      if (type === "standard") {
        this.standardSortType =
          this.standardSortType === "asc" ? "desc" : "asc";
        let __sortList = this.standardTabList;
        this.standardTabList = __sortList.sort((a, b) => {
          if (this.standardSortType === "asc") {
            return a.name.toUpperCase().localeCompare(b.name.toUpperCase());
          } else {
            return b.name.toUpperCase().localeCompare(a.name.toUpperCase());
          }
        });
      } else {
        this.requestByMeSortType =
          this.requestByMeSortType === "asc" ? "desc" : "asc";
        let __sortList = this.createByMeTabList;
        this.createByMeTabList = __sortList.sort((a, b) => {
          if (this.requestByMeSortType === "asc") {
            return a.toUpperCase().name.localeCompare(b.name.toUpperCase());
          } else {
            return b.toUpperCase().name.localeCompare(a.name.toUpperCase());
          }
        });
      }
    },
    checkDeleteTab: function (tabItem) {
      this.tabDelete = tabItem;
      if (!this.isDontShowWarningAgain) {
        this.deleteTabAlert = true;
      } else {
        this.deleteTab();
      }
    },
    closeWarning() {
      this.tabDelete = {};
      this.deleteTabAlert = false;
    },
    deleteTab() {
      let tabItem = this.tabDelete;
      let deleteItem = {
        id: tabItem.id,
        isShow: tabItem.isShow,
        deleted: true,
        objectType: tabItem.objectType,
        isDefaultTab: tabItem.isDefaultTab,
        name: tabItem.name,
      };
      axios
        .put("/api/tab-table", [deleteItem])
        .then((response) => {
          this.tabDelete = {};
          this.deleteTabAlert = false;
          this.showToastAlert({
            title: "Success!",
            content: `Your tab view has been deleted`,
          });
          setTimeout(() => {
            this.closeToastAlert();
          }, 3000);
          this.$emit("move-success", false);
          this.refreshPage();
          if (this.isDontShowWarningAgain) {
            this.updateIsShowWarningAgain();
          }
        })
        .catch((exception) => {
          console.log(exception.data);
          this.tabDelete = {};
          this.deleteTabAlert = false;
        });
    },
    refreshPage() {
      axios
        .get("/api/tab-table/by-current-user?objectType=" + this.objectType)
        .then((res) => {
          this.allTabList = res.data.data;
          this.searchedTabList = res.data.data;
        });
    },
    checkIsShowWarningAgain() {
      axios
        .get("/api/on-boarding/get?feature=SHOW_WARNING_DELETE_TAB")
        .then((response) => {
          this.isDontShowWarningAgain = response.data.data.seen;
        });
    },
    updateIsShowWarningAgain() {
      axios
        .post("/api/on-boarding/update", {
          feature: "SHOW_WARNING_DELETE_TAB",
          seen: true,
        })
        .then((response) => {});
    },
  },
  mounted() {},
};
</script>
<style scoped>
.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  height: 25px;
  width: 25px;
}

.action-btn {
  background: #d9d9d9;
  border-color: transparent;
}

.action-btn:focus {
  background: #daeeff;
}

.action-btn:hover img {
  filter: sepia(206%) hue-rotate(163deg) saturate(900%);
}

.action-btn:hover {
  background: #daeeff;
}

.content-group {
  margin-top: 20px;
  border: solid 1px #d0d0d0;
  border-radius: 3px;
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
}

.tab-group-item.odd-column {
  background-color: #fbfcfd;
}

.image-duplicate {
  cursor: pointer;
}

.__icon-sort {
  content: "";
  background: url("../images/icon/new/sort-down-icon-active.svg");
  background-size: 100% 100%;
  width: 20px;
  height: 20px;
  cursor: pointer;
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

.matching-modal {
  position: absolute;
  background: rgba(0, 0, 0, 0.1);
  top: 0;
  width: 100%;
  height: 100%;
  padding-top: 50px;
  z-index: 5;
}

.matching-modal .modal-wrapper {
  width: 300px;
  /*margin: 33vh auto;*/
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  border-radius: 5px;
}

.matching-modal .modal-container .modal-header {
  padding: 5px 15px 10px;
}

.matching-modal .modal-container .modal-header .modal-title {
  font-size: 20px;
  font-weight: bold;
}

.matching-modal .modal-container .modal-header button {
  color: #777;
  cursor: pointer;
}

.matching-modal .importing-modal .modal-container .modal-body {
  padding: 15px 5px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content {
  text-align: center;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-title {
  color: #463da5;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress {
  padding: 15px 15px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper {
  border: 1px solid #463da5;
  height: 23px;
  border-radius: 15px;
  padding: 2px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper
  .progress-bar {
  background: #263162;
  height: 100%;
  border-radius: 15px;
  position: relative;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper
  .progress-bar
  .direction-icon {
  position: absolute;
  height: 100%;
  width: 20px;
  border-radius: 15px;
  background: #615ea8;
  right: 0;
}

.matching-modal .notification-modal {
  width: 400px;
}

.matching-modal .duplicated-modal {
  width: 400px;
}

.matching-modal .warning-delete-modal {
  width: 350px;
}

.btn-delete {
  background-color: #e34537;
  color: #ffffff;
  border-radius: 3px;
}

.btn-delete :hover,
.btn-delete :active {
  background-color: #c92617;
}

.btn-back {
  background-color: #ffffff;
  border: 1px solid #d6dade;
  color: #595959;
  padding: 6px 8px;
}

.btn-back:focus {
  /*
  background-color: #F4F4F4;
  border: 2px solid #D6DADE;
  */
  box-shadow: unset;
}

.btn-back:active {
  background-color: #f4f4f4;
  border: 1px solid #d6dade !important;
  box-shadow: 0 0 0 1px #d6dade;
}

.btn-back:hover {
  background-color: #f4f4f4;
  border: 1px solid #595959;
}

.btn {
  border-radius: 3px;
}

.duplicated-action {
  text-align: end;
}

.check-box-title {
  color: #757575;
  font-size: 11px;
  line-height: 13px;
}

.warning-title {
  display: flex;
  margin-bottom: 11px;
}

.warning-title p {
  margin-left: 7px;
  margin-bottom: 0px;
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  color: #4b4b4b;
}

.duplicated-title p {
  font-weight: 400;
  font-size: 15px;
  line-height: 17px;
  color: #4b4b4b;
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
  width: 100%;
}

.modal-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 30px;
  margin-bottom: 12px;
  font-size: 16px;
}
</style>
