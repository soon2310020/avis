<template>
  <div>
    <data-completion-report
      v-show="showDataCompletionReport"
      :resources="resources"
      @back="handleCloseDataCompletionReport"
      :object-type="completionReportType"
      @handle-click-request="handleClickRequestOnReport"
    >
    </data-completion-report>
    <div v-show="!showDataCompletionReport">
      <report-overview
        :resources="resources"
        :change-key="changeReportChartKey"
        @open-data-completion-report="handleOpenDataCompletionReport"
      ></report-overview>
      <div>
        <data-request-table
          :resources="resources"
          @show-my-requests="handleShowMyRequests"
          :render-table-key="renderTableKey"
          @show-create-request="handleShowCreateRequest"
          @show-request-detail-modal="handleOpenDetailModal"
          @show-request-list-modal="handleOpenRequestListModal"
          @reopen="handleReopenDataRequest"
        >
        </data-request-table>
      </div>
    </div>

    <my-requests
      ref="my-request"
      :resources="resources"
      :visible="isShowMyRequests"
      @close="handleCloseMyRequests"
      @show-detail="handleOpenDetailModal"
      @show-request-list="handleOpenRequestListModal"
    ></my-requests>

    <creator-data-registration-modal
      :resources="resources"
      :visible="isShowCreateDataRegistration"
      :modal-type="registrationModalType"
      @close="handleCloseRegistrationCreator"
      @reload="handleReloadDataTable"
      @show-toast="handleShowToast"
      :selected-data-request="selectedDataRequest"
    >
    </creator-data-registration-modal>

    <creator-data-completion-modal
      :resources="resources"
      :visible="isShowCreateDataCompletion"
      :modal-type="completionModalType"
      @close="handleCloseCompletionCreator"
      @reload="handleReloadDataTable"
      @show-toast="handleShowToast"
      :selected-data-request="selectedDataRequest"
    >
    </creator-data-completion-modal>

    <request-detail-modal
      :resources="resources"
      :visible="isShowDetailModal"
      :selected-data-request="selectedDataRequest"
      @reload="handleReloadDataTable"
      @close="handleCloseDetailModal"
      @show-toast="handleShowToast"
      @on-declined="handleShowMyRequests"
      @on-accept="handleOpenRequestListModal"
      @on-edit="handleOpenEditRequest"
    >
    </request-detail-modal>

    <request-list-modal
      :resources="resources"
      :visible="isShowRequestListModal"
      :reload-key="reloadListModalKey"
      :is-viewing="isViewingRequestList"
      :selected-data-request="selectedDataRequest"
      @close="handleCloseRequestListModal"
      @complete-data="handleShowMultipleCompletion"
      @reload="handleReloadDataTable"
      @show-toast="handleShowToast"
    >
    </request-list-modal>

    <data-entry-modal
      :resources="resources"
      :visible="visibleDataEntry"
      :type="dataEntryType"
      :modal-type="dataEntryModalType"
      :selected-data-request="selectedDataRequest"
      @close="handleCloseDataEntryModal"
      @reload-detail="handleReloadDetail"
    ></data-entry-modal>

    <toast-alert
      @close-toast="closeToast"
      :show="toastVisible"
      :title="toastData.title"
      :content="toastData.content"
    >
    </toast-alert>
  </div>
</template>

<script>
// TODO(sun.lee): Check a direct link is working correctly
module.exports = {
  name: "data-request",
  components: {
    "report-overview": httpVueLoader(
      "/components/data-requests/report-filter/report-overview.vue"
    ),
    "data-completion-report": httpVueLoader(
      "/components/data-requests/data-completion-report/data-completion-report.vue"
    ),
    "data-request-table": httpVueLoader(
      "/components/data-requests/data-table/data-request-table.vue"
    ),
    "my-requests": httpVueLoader(
      "/components/data-requests/request-modal/my-requests/my-requests.vue"
    ),
    "creator-data-registration-modal": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/creator-data-registration-modal.vue"
    ),
    "creator-data-completion-modal": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/creator-data-completion-modal.vue"
    ),
    "request-detail-modal": httpVueLoader(
      "/components/data-requests/request-modal/editor-modal/request-detail-modal.vue"
    ),
    "request-list-modal": httpVueLoader(
      "/components/data-requests/request-modal/editor-modal/request-list-modal.vue"
    ),
    "data-entry-modal": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-modal.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      isShowMyRequests: false,
      isShowCreateDataRegistration: false,
      isShowCreateDataCompletion: false,
      registrationModalType: "CREATE",
      completionModalType: "CREATE",
      isShowDetailModal: false,
      selectedDataRequest: {},
      toastVisible: false,
      toastData: {
        title: "",
        content: "",
      },
      renderTableKey: 0,
      isShowRequestListModal: false,
      visibleDataEntry: false,
      showDataCompletionReport: false,
      completionReportType: "",
      dataEntryType: "",
      currentUser: {},
      detailLoading: false,
      reloadListModalKey: 0,
      changeReportChartKey: 0,
      isViewingRequestList: false,
      dataEntryModalType: "COMPLETE",
    };
  },
  mounted() {
    this.handleUrl();

    headerVm?.getListUsers(true); // force
    headerVm?.getListCategories(true);
    headerVm?.getListCompanies(true);
  },
  computed: {
    currentUser() {
      return headerVm?.currentUser;
    },
  },
  methods: {
    async handleClickRequestOnReport(item) {
      const isCreator = item.createdByUser.id === this.currentUser.id;
      if (
        !isCreator &&
        ["IN_PROGRESS", "OVERDUE"].includes(item?.dataRequestStatus)
      ) {
        await this.handleOpenRequestListModal(item);
        return;
      }
      await this.handleOpenDetailModal(item);
    },
    handleUrl() {
      const url = new URL(location.href);
      const params = url.searchParams;
      const id = params.get("id");
      if (!id) return;

      headerVm?.handleShowDataRequest(id);
    },
    async handleOpenEditRequest(item) {
      this.selectedDataRequest = item;
      if (item.requestType === "DATA_REGISTRATION") {
        this.isShowCreateDataRegistration = true;
      } else {
        this.isShowCreateDataCompletion = true;
      }
      this.isShowDetailModal = false;
      this.completionModalType = "EDIT";
      this.registrationModalType = "EDIT";
    },
    async handleReopenDataRequest(item) {
      console.log("handleOpenEditRequest", item);
      this.selectedDataRequest = await this.getDataRequestById(item.id);
      if (this.selectedDataRequest.requestType === "DATA_REGISTRATION") {
        this.isShowCreateDataRegistration = true;
      } else {
        this.isShowCreateDataCompletion = true;
      }
      this.isShowDetailModal = false;
      this.completionModalType = "REOPEN";
      this.registrationModalType = "REOPEN";
    },
    handleCloseDataCompletionReport() {
      this.showDataCompletionReport = false;
      this.completionReportType = "";
      this.changeReportChartKey++;
      // this.handleReloadDataTable()
    },
    handleOpenDataCompletionReport(type) {
      this.showDataCompletionReport = true;
      this.completionReportType = type;
    },
    handleCloseRequestListModal() {
      this.isShowRequestListModal = false;
      this.selectedDataRequest = {};
    },
    async handleOpenRequestListModal(item, isViewing) {
      this.selectedDataRequest = await this.getDataRequestById(item.id);
      this.isViewingRequestList = isViewing;
      console.log("handleOpenRequestListModal", item, isViewing);
      this.isShowRequestListModal = true;
    },
    closeToast() {
      this.toastVisible = false;
      setTimeout(() => {
        this.toastData.title = "";
        this.toastData.content = "";
        this.toastData.status = "";
      }, 1000);
    },

    async handleOpenDetailModal(item) {
      console.log("handleOpenDetailModal", item);
      this.selectedDataRequest = await this.getDataRequestById(item.id);
      this.isShowDetailModal = true;
    },
    handleCloseDetailModal() {
      this.isShowDetailModal = false;
      this.selectedDataRequest = {};
    },
    handleShowMyRequests() {
      console.log("handleShowMyRequests");
      this.isShowMyRequests = true;
    },
    handleCloseMyRequests() {
      this.isShowMyRequests = false;
    },
    handleShowCreateRequest(type) {
      console.log("handleShowCreateRequest", type);
      if (type.key === "DATA_REGISTRATION") {
        this.isShowCreateDataRegistration = true;
      } else if (type.key === "DATA_COMPLETION") {
        this.isShowCreateDataCompletion = true;
      } else if (type.key === "COMPLETE_DATA") {
        this.visibleDataEntry = true;
      }
      this.completionModalType = "CREATE";
      this.registrationModalType = "CREATE";
    },
    handleCloseRegistrationCreator() {
      this.isShowCreateDataRegistration = false;
    },
    handleCloseCompletionCreator() {
      this.isShowCreateDataCompletion = false;
    },
    handleReloadDataTable() {
      this.renderTableKey++;
    },
    handleShowToast(toastContent) {
      if (this.isShowMyRequests) {
        const el = this.$refs["my-request"];
        if (el) {
          el.handleShowToast(toastContent);
        }
      } else {
        this.toastData.title = toastContent.title;
        this.toastData.content = toastContent.content;
        this.toastData.status = toastContent.status;
        this.$nextTick(() => {
          this.toastVisible = true;
          setTimeout(() => {
            this.closeToast();
          }, 3000);
        });
      }
    },
    async getDataRequestById(id) {
      try {
        this.detailLoading = true;
        const requestResponse = await axios.get(`/api/data-request/${id}`);
        const request = requestResponse.data;
        const attachmentResponse = await axios.get(
          `/api/file-storage?storageType=DATA_REQUEST_FILE&refId=${id}`
        );
        const attachment = attachmentResponse.data;
        request.uploadedAttachment = attachment;
        request.attachment = [];
        this.detailLoading = false;
        return request;
      } catch (error) {
        console.log(error);
      }
    },
    handleCloseDataEntryModal() {
      this.visibleDataEntry = false;
    },
    handleShowMultipleCompletion(type, modalType) {
      console.log("handleShowMultipleCompletion", type);
      this.dataEntryModalType = modalType;
      this.dataEntryType = type;
      this.visibleDataEntry = true;
    },
    async handleReloadDetail(id) {
      this.selectedDataRequest = await this.getDataRequestById(id);
      this.reloadListModalKey++;
    },
  },
};
</script>
