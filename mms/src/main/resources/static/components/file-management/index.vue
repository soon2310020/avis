<template>
  <div class="file-management-container">
    <div class="page-title">File Management</div>
    <div class="section-tabs">
      <base-card-tab-list
        :list-tabs="listCards"
        :current-tab="activeCard"
        item-width="160px"
        size="small"
        @change="(tab) => (activeCard = tab.key)"
      ></base-card-tab-list>
    </div>
    <div class="section-data">
      <div class="search-bar">
        <base-search
          v-model="queries.query"
          placeholder="Search by selected columns below"
          class="search-bar__input"
        ></base-search>
      </div>

      <div class="data-table">
        <div class="data-table__head">
          <div class="table-tab-list">
            <span
              v-for="tab in listTabs"
              class="table-tab-item"
              :class="{ 'tab-active': activeTab === tab.key }"
              :key="tab.key"
              @click="activeTab = tab.key"
            >
              <span class="table-tab-item__label">{{ tab.label }}</span>
              <span class="table-tab-item__count">{{ tab.count }}</span>
            </span>
          </div>

          <div class="table-actions">
            <base-button
              level="primary"
              @click="handleShowFileManage('create')"
            >
              Create New
            </base-button>
          </div>
        </div>

        <file-manage-table
          :resources="resources"
          :active-tab="activeTab"
          :list-data="listFiles"
          :pagination="pagination"
          @enable="handleEnable"
          @disable="handleDisable"
          @view="handleView"
          @manage="handleManageVersion"
          @update="handleUpdate"
          @sort="handleSort"
          @paginate="handlePagination"
        ></file-manage-table>

        <div class="data-table__foot"></div>
      </div>
    </div>

    <!-- DIALOGS -->
    <file-manage-dialog
      :resources="resources"
      :mode="dialogManageFile.mode"
      :visible="dialogManageFile.show"
      :title="dialogManageFile.title"
      :data="dialogManageFile.data"
      :refetch="fetchListFiles"
      :file-group-type="activeCard"
      @close="handleCloseFileManage"
      @after-create="handleCloseFileManage"
    ></file-manage-dialog>

    <version-manage-dialog
      mode="view"
      :resources="resources"
      :visible="dialogManageVersion.show"
      :title="dialogManageVersion.title"
      :data="dialogManageVersion.data"
      :refetch="fetchVersions"
      @close="handleCloseVersionManageDialog"
    ></version-manage-dialog>
  </div>
</template>

<script>
const FILE_GROUP_TYPE = {
  UNMANAGED: "UNMANAGED",
  DATA_IMPORT_TEMPLATE: "DATA_IMPORT_TEMPLATE",
  DATA_EXPORT_TEMPLATE: "DATA_EXPORT_TEMPLATE",
  SOFTWARE: "SOFTWARE",
};

module.exports = {
  name: "FileManagement",
  components: {
    "file-manage-table": httpVueLoader("./file-manage-table.vue"),
    "file-manage-dialog": httpVueLoader("./file-manage-dialog.vue"),
    "version-manage-dialog": httpVueLoader("./version-manage-dialog.vue"),
  },
  setup(props, { root }) {
    const resources = ref({});

    const fetchMessages = async () => {
      const messages = await Common.getSystem("messages");
      resources.value = Object.assign(
        {},
        resources.value,
        JSON.parse(messages)
      );
      console.log("resources", resources);
    };

    // CARD TAB
    const listCards = ref([
      {
        key: FILE_GROUP_TYPE.DATA_IMPORT_TEMPLATE,
        title: "Data Import Template",
        iconSrc: "/images/icon/import-template.svg",
      },
      {
        key: FILE_GROUP_TYPE.DATA_EXPORT_TEMPLATE,
        title: "Data Export Template",
        iconSrc: "/images/icon/export-actionbar.svg",
      },
      {
        key: FILE_GROUP_TYPE.SOFTWARE,
        title: "Software",
        iconSrc: "/images/icon/software.svg",
      },
    ]);
    const listTabs = ref([
      {
        key: "enabled",
        label: "Enabled",
        count: 0,
      },
      {
        key: "disabled",
        label: "Disabled",
        count: 0,
      },
    ]);
    // TABLE STATE
    const listFiles = ref([]);
    const queries = ref({
      enabled: true,
      query: "",
      sort: "",
      fileGroupType: computed(() => activeCard.value),
      page: 1,
      size: 10,
    });

    // const pagination = ref({
    //     page: 0,
    //     size: 10,
    //     totalPages: 0,
    // })
    const pagination = ref([]);

    const activeCard = ref(FILE_GROUP_TYPE.DATA_IMPORT_TEMPLATE);
    const activeTab = ref("enabled");
    const activeTabIndex = computed(() =>
      listTabs.value.findIndex((i) => i.key === activeTab.value)
    );

    // DIALOG STATE
    const dialogManageFile = ref({
      mode: "create",
      title: "Create new file",
      data: null,
      show: false,
    });

    const dialogManageVersion = ref({
      mode: "view",
      title: "Manage Version",
      data: null,
      show: false,
    });

    // FETCH
    const isFetching = ref(false);

    const fetchListFiles = async (params) => {
      queries.value.enabled = activeTab.value === "enabled";
      queries.value.page = params?.page ? params.page : queries.value.page;
      const queryParam = Common.param({ ...queries.value, ...params });
      isFetching.value = true;
      try {
        const response = await axios.get("/api/common/fle-mng?" + queryParam);
        listFiles.value = response.data.content;
        listTabs.value.splice(activeTabIndex.value, 1, {
          ...listTabs.value[activeTabIndex.value],
          count: response.data.totalElements,
        });
        pagination.value = Common.getPagingData(response.data);
      } catch (error) {
        console.log(error);
      } finally {
        isFetching.value = false;
      }
    };

    const fetchUpdatePublishedStatus = async (status, listIds) => {
      const queryParam = Common.param({ id: listIds });
      try {
        await axios.put(`/api/common/fle-mng/${status}?` + queryParam);
        root.setToastAlertGlobal({
          title: "Success!",
          content: `Update file's published status`,
          show: true,
        });
        fetchListFiles();
      } catch (error) {
        console.log(error);
      }
    };

    const fetchVersions = async (file, params) => {
      console.log("@fetchVersions", file, params);
      try {
        const queryParam = Common.param({ ...params });
        const response = await axios.get(
          `/api/common/fle-mng/${file.id}/versions?` + queryParam
        );
        dialogManageVersion.value.data = {
          id: file.id,
          enabled: file.enabled,
          fileGroupType: file.fileGroupType,
          fileGroupName: file.fileGroupName,
          fileGroupCode: file.fileGroupCode,
          fileGroupStatus: file.fileGroupStatus,
          version: file.version,
          description: file.description,
          fileRefs: file.fileRefs,
          listVersions: response.data,
        };
        console.log("response", response);
      } catch (error) {
        console.log(error);
      }
    };

    const handleShowFileManage = (type, item) => {
      console.log(type, item);
      dialogManageFile.value.show = true;
      dialogManageFile.value.mode = type;

      if (type === "create") {
        dialogManageFile.value.title = "Create new file";
      }
      if (type === "view") {
        dialogManageFile.value.title = "View file";
        dialogManageFile.value.data = item;
      }
      if (type === "update") {
        dialogManageFile.value.title = "Update file";
        dialogManageFile.value.data = item;
      }

      Vue.nextTick(() => {
        dialogManageFile.value.show = true;
      });
    };
    const handleCloseFileManage = () => {
      dialogManageFile.value.show = false;
      dialogManageFile.value.title = "";
      dialogManageFile.value.data = null;
    };

    const handleShowVersionManageDialog = (item) => {
      dialogManageVersion.value.show = true;
      fetchVersions(item);
    };
    const handleCloseVersionManageDialog = () => {
      dialogManageVersion.value.show = false;
      dialogManageVersion.value.data = null;
    };

    const handleView = (items) => {
      console.log("@handleView", items);
      handleShowFileManage("view", items[0]);
    };
    const handleDisable = (items) => {
      console.log("@handleDisable", items);
      const listIds = items.map((i) => i.id);
      fetchUpdatePublishedStatus("disable", listIds);
    };
    const handleEnable = (items) => {
      console.log("@handleEnable", items);
      const listIds = items.map((i) => i.id);
      fetchUpdatePublishedStatus("enable", listIds);
    };
    const handleManageVersion = (items) => {
      console.log("@handleManageVersion", items);
      handleShowVersionManageDialog(items[0]);
    };
    const handleUpdate = (items) => {
      console.log("@handleUpdate", items);
      handleShowFileManage("update", items[0]);
    };
    // Col custom
    const showColCustomModal = ref(false);
    const handleClickColCustom = () => {
      showColCustomModal.value = true;
    };
    const handleSort = ({ sortDesc, field }) => {
      console.log("@handleSort", sortDesc, field);
      queries.value.sort = `${field},${sortDesc ? "desc" : "asc"}`;
      fetchListFiles();
    };
    const handlePagination = (pageNumber) => {
      fetchListFiles({ page: pageNumber });
    };
    watch([activeTab, activeCard, () => queries.value.query], () => {
      fetchListFiles({ page: 1 });
    });
    onMounted(() => {
      fetchMessages();
      fetchListFiles({ page: 1 });
    });

    return {
      queries,
      resources,
      listFiles,
      pagination,
      fetchVersions,
      fetchListFiles,

      listCards,
      activeCard,

      listTabs,
      activeTab,

      showColCustomModal,
      handleClickColCustom,

      handleSort,
      handleView,
      handleUpdate,
      handleEnable,
      handleDisable,
      handlePagination,
      handleManageVersion,

      dialogManageFile,
      handleShowFileManage,
      handleCloseFileManage,

      dialogManageVersion,
      handleShowVersionManageDialog,
      handleCloseVersionManageDialog,
    };
  },
};
</script>

<style scoped>
.file-management-container {
  padding-bottom: 40px;
}
.page-title {
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  margin-top: 40px;
  margin-bottom: 12px;
}
.section-tabs {
  margin-bottom: 27px;
}

/* SECTION DATA */
.section-data .search-bar {
  margin-bottom: 19px;
}
.section-data .search-bar .search-bar__input {
  width: 50%;
}

/* DATA TABLE */
.section-data .data-table__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* DATA TABLE TABS */
.section-data .table-tab-list {
  display: flex;
}

.section-data .table-tab-item {
  /* border: 1px solid #eeeeee; */
  border-radius: 3px 3px 0px 0px;
  padding: 11px 10px;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.section-data .table-tab-item__label {
}
.section-data .table-tab-item__count {
  display: none;
  justify-content: center;
  align-items: center;
  font-weight: 400;
  font-size: 11.22px;
  margin-left: 12px;
  padding-right: 0.6em;
  padding-left: 0.6em;
}

/* TAB ACTIVE */
.section-data .table-tab-item.tab-active {
  border: 1px solid #c9ced2;
  color: var(--grey-dark);
  background-color: #fff;
}
.section-data .table-tab-item.tab-active .table-tab-item__label {
  font-weight: 700;
  font-size: 14px;
  line-height: 17px;
}
.section-data .table-tab-item.tab-active .table-tab-item__count {
  display: flex;
  border-radius: 10rem;
  background-color: rgba(0, 0, 0, 0.05);
  font-weight: 700;
}

.table-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.button-icon {
  display: inline-flex;
  width: 29px;
  height: 29px;
  justify-content: center;
  align-items: center;
  background-color: #f0f1f3;
}

.action-bar {
  font-weight: 400;
}
</style>
