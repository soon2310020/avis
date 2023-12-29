Vue.component("widget", EmoldinoComponents.Widget);
Vue.component("progress-bar", EmoldinoComponents.ProgressBar);
Vue.component("tooltip", EmoldinoComponents.Tooltip);
Vue.component("spinner", EmoldinoComponents.Spinner);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("select-all", httpVueLoader("/components/@kit/select-all.vue"));
Vue.component(
  "base-images-slider",
  httpVueLoader("/components/@base/images-preview/base-images-slider.vue")
);
axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

let removeWave = function (time) {
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    if (document.getElementById("removee"))
      document.getElementById("removee").remove();
  }, time);
};

const WO_STATUS = {
  OVERDUE: {
    value: "Overdue",
    label: "Overdue",
  },
  UPCOMING: {
    value: "Upcoming",
    label: "Upcoming",
  },
  REQUESTED: {
    value: "Requested",
    label: "Requested",
  },
  DECLINED: {
    value: "Declined",
    label: "Declined",
  },
  CANCELLED: {
    value: "Cancelled",
    label: "Cancelled",
  },
  COMPLETED: {
    value: "Completed",
    label: "Completed",
  },
  CANCELLED: {
    value: "Cancelled",
    label: "Cancelled",
  },
  PENDING_APPROVAL: {
    value: "PENDING_APPROVAL",
    label: "Pending Approval",
  },
};

const WO_TYPE = {
  GENERAL: {
    label: "General",
    value: "GENERAL",
  },
  INSPECTION: {
    label: "Inspection",
    value: "INSPECTION",
  },
  EMERGENCY: {
    label: "Emergency",
    value: "EMERGENCY",
  },
  PREVENTATIVE_MAINTENANCE: {
    label: "Preventive Maintenance",
    value: "PREVENTATIVE_MAINTENANCE",
  },
  CORRECTIVE_MAINTENANCE: {
    label: "Corrective Mainenance",
    value: "CORRECTIVE_MAINTENANCE",
  },
  REFURBISHMENT: {
    label: "Refurbishment",
    value: "REFURBISHMENT",
  },
  DISPOSAL: {
    label: "Disposal",
    value: "DISPOSAL",
  },
};

const MESSAGES = {
  COMPLETE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: "Your Work Order has been completed.",
  },
  ACCEPT_DECLINE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: `Your Work Order's status has been updated.`,
  },
  REQUEST_CHANGE_SUCCESS: {
    TITLE: "Success!",
    CONTENT: "Your Request Change Work Order has been submitted",
  },
};

const TIME_OPTION = [{ title: "Monthly", type: "MONTHLY", isRange: false }];

const typeOptions = [
  { id: WO_TYPE.GENERAL.value, name: WO_TYPE.GENERAL.label },
  { id: WO_TYPE.INSPECTION.value, name: WO_TYPE.INSPECTION.label },
  { id: WO_TYPE.EMERGENCY.value, name: WO_TYPE.EMERGENCY.label },
  {
    id: WO_TYPE.PREVENTATIVE_MAINTENANCE.value,
    name: WO_TYPE.PREVENTATIVE_MAINTENANCE.label,
  },
  {
    id: WO_TYPE.CORRECTIVE_MAINTENANCE.value,
    name: WO_TYPE.CORRECTIVE_MAINTENANCE.label,
  },
  {
    id: WO_TYPE.REFURBISHMENT.value,
    name: WO_TYPE.REFURBISHMENT.label,
  },
  { id: WO_TYPE.DISPOSAL.value, name: WO_TYPE.DISPOSAL.label },
];

const priorityOptions = [
  { id: "LOW", name: "Low" },
  { id: "MEDIUM", name: "Medium" },
  { id: "HIGH", name: "High" },
];

window.loading = true;
document.addEventListener("readystatechange", (event) => {
  if (
    event.target.readyState === "complete" &&
    window.headerVm &&
    window.loading
  ) {
    document.title = "Workorder" + " | eMoldino";
    removeWave(500);
    removeWave(2000);
    delete window.loading;
    var vm = new Vue({
      el: "#app",
      components: {
        "filter-work-orders": httpVueLoader(
          "/components/work-order/filter-work-orders.vue"
        ),
        "status-card": httpVueLoader("/components/@base/status-card.vue"),
        "list-work-orders": httpVueLoader(
          "/components/work-order/list-work-orders.vue"
        ),
        "creator-work-order-dialog": httpVueLoader(
          "/components/work-order/creator/creator-work-order-dialog.vue"
        ),
        "created-work-order-dialog": httpVueLoader(
          "/components/work-order/src/created-work-order-dialog.vue"
        ),
        "reported-work-order-dialog": httpVueLoader(
          "/components/work-order/src/reported-work-order-dialog.vue"
        ),
        "request-change-work-order-dialog": httpVueLoader(
          "/components/work-order/src/request-change-work-order-dialog.vue"
        ),
        "machine-details": httpVueLoader("/components/machine-details.vue"),
        "chart-mold": httpVueLoader(
          "/components/chart-mold/chart-mold-modal.vue"
        ),
        "file-previewer": httpVueLoader(
          "/components/mold-detail/file-previewer.vue"
        ),
        "my-work-order-dialog": httpVueLoader(
          "/components/work-order/owner/my-work-order-dialog.vue"
        ),
        "my-work-orders": httpVueLoader(
          "/components/work-order/owner/my-work-orders.vue"
        ),
        "work-order-export-modal": httpVueLoader(
          "/components/work-order/src/work-order-export/work-order-export-modal.vue"
        ),
        // base
        "preview-images-system": httpVueLoader(
          "/components/common/preview-images-system.vue"
        ),
        "common-select": httpVueLoader(
          "/components/@base/dropdown/common-select.vue"
        ),
        "custom-dropdown": httpVueLoader(
          "/components/common/selection-dropdown/custom-dropdown.vue"
        ),
        "custom-dropdown-button": httpVueLoader(
          "/components/@base/button/custom-dropdown-button.vue"
        ),
        "date-picker-modal": httpVueLoader(
          "/components/@base/date-picker/date-picker-modal.vue"
        ),
        "customize-button": httpVueLoader(
          "/components/@base/button/customize-button.vue"
        ),
        "terminal-details": httpVueLoader("/components/terminal-details.vue"),
        "sensor-detail-modal": httpVueLoader(
          "/components/counter/sensor-detail-modal.vue"
        ),
      },
      data() {
        return {
          resources: {},
          currentUser: null,
          typeOptions: [],
          priorityOptions: [],
          statusOptions: [
            { id: WO_STATUS.OVERDUE.value, name: WO_STATUS.OVERDUE.label },
            { id: WO_STATUS.UPCOMING.value, name: WO_STATUS.UPCOMING.label },
            { id: WO_STATUS.REQUESTED.value, name: WO_STATUS.REQUESTED.label },
            { id: WO_STATUS.DECLINED.value, name: WO_STATUS.DECLINED.label },
            { id: WO_STATUS.CANCELLED.value, name: WO_STATUS.CANCELLED.label },
          ],
          companyOptions: [],
          data: {
            pmSummary: {
              open: 0,
              overdue: 0,
              completed: 0,
              openPercent: 0,
              completedPercent: 0,
              completedPercent: 0,
            },
          },
          requestParam: {
            typeList: [],
            priorityTypeList: [],
            companyIdList: [],
            orderType: [],
            page: 1,
            isHistory: false,
            status: "",
          },
          selectedWorkOrder: null,
          showReportDialog: false,
          showDetailDialog: false,
          showRequestChangeDialog: false,
          showCreatorDialog: false,
          isLoading: false,
          myOrderKey: 0,
          changedType: "",
          notification: {
            show: false,
            title: "",
            content: "",
          },
          shouldViewOnly: false,
          workOrderType: "EDIT",
          isShowWorkOrderEdit: true,
          selectedTab: "",
          showWorkOrder: false,
          visibleExportModal: false,
          timeOption: [...TIME_OPTION],
          selectedDate: {
            from: moment().startOf("month").toDate(),
            to: moment().endOf("month").toDate(),
            fromTitle: moment().startOf("month").format("YYYY-MM"),
            toTitle: moment().endOf("month").format("YYYY-MM"),
          },
          datepickerFrequency: "MONTHLY",
          results: [],
          isLoadingTable: false,
          resultsStat: {},
          total: 0,
          pagination: [],
          tooltipStyle: {
            width: "300px",
          },
          reloadCheckKey: 0,
          totalPages: 0,
          assetSelectedList: [],
          tabName: "",
          isRemovePmWorkOrderFlag: false,
          requestChangeViewMode: "history",
          isLoadingRequestChange: false,
        };
      },
      async created() {
        try {
          const res = await axios.get("/api/companies?size=9999&page=0");
          this.companyOptions = res.data.content;
          this.requestParam.companyIdList = res.data.content.map((o) => o.id);
        } catch (error) {
          console.log(error);
        }
        this.requestParam.priorityTypeList = (this.priorityOptions || []).map(
          (o) => o.id
        );
        this.requestParam.typeList = this.typeOptions.map((o) => o.id);
        let params = Common.parseParams(URI.parse(location.href).query);
        this.requestParam.isHistory =
          params.isHistory === "true" ? true : false;
        if (!!params.assetIds) {
          this.requestParam.assetIds = [params.assetIds];
        }
        if (!!params.start && !!params.end) {
          const startDate = moment.unix(+params.start).startOf("month");
          const endDate = moment.unix(+params.end).endOf("month");
          this.selectedDate = {
            from: startDate.toDate(),
            to: endDate.toDate(),
            fromTitle: startDate.format("YYYY-MM"),
            toTitle: endDate.format("YYYY-MM"),
          };
        } else {
          this.selectedDate = {
            from: moment().startOf("month").toDate(),
            to: moment().endOf("month").toDate(),
            fromTitle: moment().startOf("month").format("YYYY-MM"),
            toTitle: moment().endOf("month").format("YYYY-MM"),
          };
        }

        this.fetchWorkOrderList();
      },
      methods: {
        showMyWorkOrder() {
          this.showWorkOrder = true;
        },
        getCreatorUsers(result) {
          if (!result) return [];
          return _.uniqBy(
            [
              ...result?.workOrderUsers.filter(
                (item) => item.participantType === "CREATOR"
              ),
              { user: result?.createdBy, userId: result?.createdBy?.id },
            ],
            "userId"
          );
        },
        showCounterDetails: function (id) {
          const child = this.$refs["sensor-detail-modal"];
          child && child.showDetailsById(id);
        },
        showTerminalDetails: function (id) {
          const child = this.$refs["terminal-details"];
          child && child.showDetailsById(id);
        },
        async handleWidgetClick(params) {
          console.log("handleWidgetClick", params);
          if (params) {
            this.requestParam = { ...this.requestParam, ...params };
            if (!params.orderType) {
              this.requestParam.orderType = [];
            }
            if (!params.status) {
              this.requestParam.status = "";
            }
            if (!params.isHistory) {
              this.requestParam.isHistory = false;
            }
          } else {
            this.requestParam = {
              ...this.requestParam,
              orderType: "",
              status: "",
              isHistory: false,
            };
          }
          this.isRemovePmWorkOrderFlag = false;
          await this.fetchWorkOrderList(1);
        },
        captureQuery() {
          const url = new URL(location.href);
          const params = url.searchParams;
          const popupType = params.get("popup");
          const isHistory = params.get("isHistory") === "true";
          const workOrderId = params.get("id");
          this.requestParam.isHistory = isHistory;
          if (!popupType) return;
          this.popup(popupType, workOrderId);
        },
        /**
         * Show modal popup by type.
         * `headerVm` is a global variable in `headerSettings.js`.
         * @param {string} type - Type of popup.
         * @param {string} id - Work order id.
         */
        popup(type, id) {
          console.log("🚀 popup", id, type);
          switch (type) {
            // NEW
            case "WO_CREATED":
            case "WO_ACCEPTED":
            case "WO_CPT_APPROVED":
              headerVm?.showDetail(id);
              break;
            case "WO_CPT_CANCELLED":
            case "WO_CPT_REJECTED":
            case "WO_CRT_DECLINED":
            case "WO_DECLINED":
            case "WO_CANCELLED":
            case "WO_MOD_REJECTED":
              headerVm?.handleViewReason(id, type);
              break;
            case "WO_CRT_APPROVED":
              headerVm?.openEditWorkOrder({ id });
              break;
            case "WO_CRT_APPR_REQUESTED":
              headerVm?.handleOpenCmView(id);
              break;
            case "WO_MOD_APPR_REQUESTED":
            case "WO_MOD_APPROVED":
              headerVm?.handleOpenRequestChange(id);
              break;
            case "WO_CPT_APPR_REQUESTED": // wrong case
              headerVm?.handleReviewWorkOrder(id);
              break;
            case "WO_COMPLETED": // owner receive noti
              headerVm?.handleShowCompletedWorkOrder(id);
            default:
              console.log("🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀🚀");
              break;
          }
          // TODO(ducnm2010) remove this when deployed to client
          history.replaceState(null, null, Common.PAGE_URL.WORK_ORDER);
        },
        async fetchCompaniesList() {
          try {
            // TODO(anntptwendeesoft): If the company list is too long, use pagination
            const res = await axios.get("/api/companies", {
              params: {
                size: 9999,
                page: 0,
              },
            });
            this.companyOptions = res.data.content;
            this.requestParam.companyIdList = res.data.content.map((o) => o.id);
          } catch (error) {
            console.error("fetchCompaniesList() error:", error);
          }
        },
        handleShowWorkOrder() {
          const datepicker = this.$refs["base-filter-date-picker"];
          const selectedDateParam = { ...this.selectedDate };
          datepicker.showDatePicker(
            this.datepickerFrequency,
            selectedDateParam
          );
        },
        handleChangeDate(timeValue, frequency) {
          this.selectedDate = { ...timeValue };
          this.datepickerFrequency = frequency;
          const datepicker = this.$refs["base-filter-date-picker"];
          datepicker.closeDatePicker();
          this.fetchWorkOrderList();
        },
        handleExportWorkOrder() {
          this.visibleExportModal = true;
        },
        closeExportWorkOrder() {
          this.visibleExportModal = false;
        },
        closeMyOrder() {
          this.showWorkOrder = false;
        },
        showCreateWorkOrderModal(item) {
          this.workOrderType = "CREATE";
          this.assetSelectedList = [];
          this.showCreatorDialog = true;
        },
        filterQueryFunc(type, value, isCheckAll) {
          switch (type) {
            case "types":
              if (value?.length === 0) {
                this.requestParam.typeList = this.typeOptions.map((o) => o.id);
              } else {
                this.requestParam.typeList = [...value];
              }
              break;
            case "priority":
              if (value?.length === 0) {
                this.requestParam.priorityTypeList = (
                  this.priorityOptions || []
                ).map((o) => o.id);
              } else {
                this.requestParam.priorityTypeList = [...value];
              }
              break;
            case "companies":
              this.requestParam.companyIdList = [...value];

              break;
          }
          this.fetchWorkOrderList();
          console.log({ type, value, isCheckAll });
        },
        async fetchWorkOrderList(pageNumber) {
          const { from, to } = this.selectedDate;
          const start = moment(from).unix();
          const end = moment(to).unix();
          const page = pageNumber ? pageNumber : 1;
          this.requestParam.page = page;
          if (this.requestParam.isPmWorkOrder && this.isRemovePmWorkOrderFlag) {
            delete this.requestParam.isPmWorkOrder;
            delete this.requestParam.status;
          }
          const param = Common.param({
            ...this.requestParam,
            isAllCompany: this.isAllCompany,
            start,
            end,
          });
          try {
            this.isLoadingTable = true;
            const response = await axios.get(`/api/work-order?${param}`);
            console.log("response", response);
            this.data = response.data;
            this.results = response.data.workOrderList.content;
            this.pagination = Common.getPagingData(response.data.workOrderList);
            this.totalPages = response.data.workOrderList.totalPages;
            if (this.data.pmSummary) {
              this.data.pmSummary.openPercent = this.data.pmSummary?.openPercent
                ? +this.data.pmSummary.openPercent.toFixed(2)
                : 0;
              this.data.pmSummary.overduePercent = this.data.pmSummary
                ?.overduePercent
                ? +this.data.pmSummary.overduePercent.toFixed(2)
                : 0;
              this.data.pmSummary.completedPercent = this.data.pmSummary
                ?.completedPercent
                ? +this.data.pmSummary.completedPercent.toFixed(2)
                : 0;
            }
            this.resultsStat = {
              general: response.data.general,
              inspection: response.data.inspection,
              emergency: response.data.emergency,
              pm: response.data.pm,
              cm: response.data.cm,
              refurbishment: response.data.refurbishment,
              disposal: response.data.disposal,
              total: response.data.total,
              history: response.data.totalHistory,
            };
            this.total = response.data.workOrderList.totalElements;
            this.isLoadingTable = false;
            return response;
          } catch (error) {
            console.log(error);
          }
        },
        async fetchWorkOrderDetail(workOrderId) {
          this.isLoading = true;
          try {
            const response = await axios.get(`/api/work-order/${workOrderId}`);
            this.selectedWorkOrder = response.data.data;
            this.assetSelectedList =
              this.selectedWorkOrder?.workOrderAssets.map((wo) => ({
                ...wo,
                title: wo.assetCode,
                checked: true,
                assetView: wo.type.toLowerCase(),
                id: wo.assetId,
              }));
          } catch (error) {
            console.log(error);
          } finally {
            this.isLoading = false;
          }
        },
        handleViewWorkOrder(item, tab, action) {
          if (tab) {
            this.selectedTab = tab;
          } else {
            this.selectedTab = "";
          }
          this.shouldViewOnly = true;
          this.fetchWorkOrderDetail(item.id);
          const {
            CANCELLED,
            COMPLETED,
            // DECLINED,
            // OVERDUE,
            PENDING_APPROVAL,
            // REQUESTED,
            // UPCOMING,
          } = WO_STATUS;
          const hasCompleted = [
            "PENDING_APPROVAL",
            "COMPLETED",
            "CANCELLED",
          ].includes(item.workOrderStatus);
          if (!hasCompleted) {
            this.showDetailDialog = true;
          } else {
            if (action === "approve_reject_report") {
              this.requestChangeViewMode = "view";
              this.showRequestChangeDialog = true;
            } else {
              this.requestChangeViewMode = "history";
              this.showReportDialog = true;
            }
          }
        },
        handleRequestChange(item) {
          this.requestChangeViewMode = "history";
          this.fetchWorkOrderDetail(item.id);
          this.showRequestChangeDialog = true;
        },
        handleCompleteWorkOrder(item) {
          this.shouldViewOnly = false;
          console.log("@handleCompleteWorkOrder", item);
          this.showReportDialog = true;
          this.fetchWorkOrderDetail(item.id);
        },
        handleUpdateStatusWorkOrder(item, tab) {
          console.log("@handleUpdateStatusWorkOrder");
          if (tab) {
            this.selectedTab = tab;
          } else {
            this.selectedTab = "";
          }
          this.fetchWorkOrderDetail(item.id);
          this.showDetailDialog = true;
        },
        handleEditWorkOrder(item) {
          const creatorList = this.getCreatorUsers(item);
          const isCreator = creatorList.some(
            (user) => user.userId === this.currentUser.id
          );
          if (isCreator) {
            // edit one's own order
            this.openEditWorkOrder(item);
          } else {
            // request change
            this.handleRequestChange(item);
          }
        },
        handleCloseDialog() {
          this.showReportDialog = false;
          this.showDetailDialog = false;
          this.showRequestChangeDialog = false;
          this.showCreatorDialog = false;
          this.requestChangeViewMode = "history";
        },
        handleReloadMyOrderTable() {
          this.myOrderKey++;
          this.reloadCheckKey++;
          this.fetchWorkOrderList();
        },
        async handleSubmitComplete(formData, isProgress) {
          try {
            console.log("@handleSubmitComplete:formData", formData);
            await axios.post("/api/work-order/complete", formData);
            this.handleCloseDialog();
            if (!isProgress) {
              this.notification.title = MESSAGES.COMPLETE_SUCCESS.TITLE;
              this.notification.content = MESSAGES.COMPLETE_SUCCESS.CONTENT;
              this.$nextTick(() => {
                console.log("show notification");
                this.notification.show = true;
                this.handleReloadMyOrderTable();
                setTimeout(() => {
                  console.log("handleSubmitComplete run");
                  this.handleCloseNotification();
                }, 3000);
              });
            }
          } catch (error) {
            console.log(error);
            Common.alert("error");
          }
        },
        async handleApproveRejectChangeRequested(workOrder) {
          this.requestChangeViewMode = "view";
          this.isLoading = true;
          try {
            const response = await axios.get(
              `/api/work-order/request-change-work-order?originId=${workOrder.id}`
            );
            this.selectedWorkOrder = response.data.data;
            this.assetSelectedList =
              this.selectedWorkOrder?.workOrderAssets.map((wo) => ({
                ...wo,
                title: wo.assetCode,
                checked: true,
                assetView: wo.type.toLowerCase(),
                id: wo.assetId,
              }));
          } catch (error) {
            console.log(error);
          } finally {
            this.isLoading = false;
          }
          this.showRequestChangeDialog = true;
        },
        async handleOpenChangeRequested(workOrder) {
          this.requestChangeViewMode = "history";
          this.isLoading = true;
          try {
            const response = await axios.get(`/api/work-order/${workOrder.id}`);
            this.selectedWorkOrder = response.data.data;
            this.assetSelectedList =
              this.selectedWorkOrder?.workOrderAssets.map((wo) => ({
                ...wo,
                title: wo.assetCode,
                checked: true,
                assetView: wo.type.toLowerCase(),
                id: wo.assetId,
              }));
          } catch (error) {
            console.log(error);
          } finally {
            this.isLoading = false;
          }
          this.showRequestChangeDialog = true;
        },
        async handleDeclineRequestChange(workOrder, reason) {
          try {
            if (this.isLoadingRequestChange) {
              return;
            }
            this.isLoadingRequestChange = false;
            this.isLoadingRequestChange = true;
            const params = { approved: false, reason: reason };
            await axios.post(
              `/api/work-order/approve-change?id=${workOrder.id}`,
              params
            );
            this.handleCloseRequestChange();
            this.notification.title = "Rejected!";
            this.notification.content = `You have rejected changes to ${workOrder.workOrderId} and notification has been sent to ${workOrder.createdBy.displayName}.`;
            this.$nextTick(() => {
              this.notification.show = true;
            });
            this.handleReloadMyOrderTable();
          } catch (error) {
            console.log(error);
          } finally {
            setTimeout(() => {
              this.handleCloseNotification();
            }, 3000);
          }
        },
        handleCloseRequestChange() {
          this.showRequestChangeDialog = false;
        },
        async handleAcceptRequestChange(workOrder) {
          if (this.isLoadingRequestChange) {
            return;
          }
          this.isLoadingRequestChange = true;
          try {
            const params = { approved: true, reason: "" };
            const response = await axios.post(
              `/api/work-order/approve-change?id=${workOrder.id}`,
              params
            );
            this.handleCloseRequestChange();
            this.notification.title = "Approved";
            this.notification.content = `Changes to ${workOrder.workOrderId} have been approved.`;
            this.$nextTick(() => {
              this.notification.show = true;
            });
            this.isLoadingRequestChange = false;
            this.handleReloadMyOrderTable();
          } catch (error) {
            console.log(error);
            Common.alert("error");
          } finally {
            setTimeout(() => {
              this.handleCloseNotification();
            }, 3000);
          }
        },
        async handleSubmitRequestChange(formData) {
          console.log("@handleSubmitRequestChange", formData);
          if (this.isLoadingRequestChange) {
            return;
          }
          this.isLoadingRequestChange = true;
          try {
            await axios.post("/api/work-order/request-change", formData);
            this.handleCloseDialog();
            this.notification.title = MESSAGES.REQUEST_CHANGE_SUCCESS.TITLE;
            this.notification.content = MESSAGES.REQUEST_CHANGE_SUCCESS.CONTENT;
            this.$nextTick(() => {
              this.notification.show = true;
              this.handleReloadMyOrderTable();
              setTimeout(() => {
                this.handleCloseNotification();
              }, 3000);
            });
            this.isLoadingRequestChange = false;
          } catch (error) {
            Common.alert("error");
            console.log(error);
          }
        },
        // async fetchUpdateWorkOrderStatus(workOrder, status) {
        //   const params = Common.param({ status });
        //   try {
        //     const response = await axios.post(
        //       `/api/work-order/update-my-order/${workOrder.id}?` + params
        //     );
        //     this.handleCloseDialog();
        //     this.notification.title = MESSAGES.ACCEPT_DECLINE_SUCCESS.TITLE;
        //     this.notification.content = MESSAGES.ACCEPT_DECLINE_SUCCESS.CONTENT;
        //     this.$nextTick(() => {
        //       console.log("show notification");
        //       this.notification.show = true;
        //       this.handleReloadMyOrderTable();
        //       setTimeout(() => {
        //         this.handleCloseNotification();
        //       }, 3000);
        //     });
        //   } catch (error) {
        //     console.log(error);
        //     Common.alert("error");
        //   }
        // },
        // handleDeclineWorkOrder(workOrder) {
        //   this.fetchUpdateWorkOrderStatus(workOrder, "DECLINED");
        // },
        // handleAcceptWorkOrder(workOrder) {
        //   this.fetchUpdateWorkOrderStatus(workOrder, "ACCEPTED");
        // },
        handleCloseNotification() {
          this.notification.show = false;
          setTimeout(() => {
            this.notification.title = "";
            this.notification.content = "";
          }, 1000);
        },
        async openEditWorkOrder(item) {
          // await this.setSelectedWorkOrder(item.id)
          await this.fetchWorkOrderDetail(item.id);
          this.workOrderType = "EDIT";
          this.showCreatorDialog = true;
        },
        async openReopenWorkOrder(item) {
          // await this.setSelectedWorkOrder(item.id)
          await this.fetchWorkOrderDetail(item.id);
          this.workOrderType = "REOPEN";
          this.showCreatorDialog = true;
        },
        closeEditWorkOrder() {
          this.showCreatorDialog = false;
          this.workOrderType = "CREATE";
        },
        handleShowToast(toastData) {
          this.notification.title = toastData.title;
          this.notification.content = toastData.content;
          this.$nextTick(() => {
            this.notification.show = true;
            setTimeout(() => {
              this.handleCloseNotification();
            }, 3000);
          });
        },
        handleClickOnCard(type) {
          if (type === "TOTAL") {
            this.requestParam.status = "";
            this.changedType = "ALL";
            this.requestParam.typeList = this.typeOptions.map((o) => o.id);
          }

          if (type === "OVERDUE") {
            this.requestParam.status = "OVERDUE";
            this.changedType = "ALL";
            this.requestParam.typeList = this.typeOptions.map((o) => o.id);
          }

          if (type === "EMERGENCY") {
            this.requestParam.status = "";
            this.changedType = WO_TYPE.EMERGENCY.value;
            this.requestParam.typeList = this.typeOptions.map((o) => o.id);
          }

          if (
            [
              WO_TYPE.CORRECTIVE_MAINTENANCE.value,
              WO_TYPE.PREVENTATIVE_MAINTENANCE.value,
            ].includes(type)
          ) {
            this.requestParam.status = "";
            this.changedType = "ALL";
            this.requestParam.typeList = [type];
          }
        },
        async showMachineDetails(machineId) {
          try {
            // const res = await axios.get(`/api/machines/${machineId}`)
            // const machine = res.data
            this.$refs.machineDetails.showDetailsById(machineId);
          } catch (error) {
            console.log(error);
          }
        },
        async showMoldChart(moldId) {
          try {
            const res = await axios.get(`/api/molds/${moldId}`);
            const mold = res.data;
            console.log("load mold", mold);
            const child = Common.vue.getChild(this.$children, "chart-mold");
            console.log("show mold chart", child);
            if (child != null) {
              child.showChart(mold, "QUANTITY", "DAY", "switch-graph");
            }
          } catch (error) {
            console.log(error);
          }
        },
        showFilePreviewer(file) {
          var child = Common.vue.getChild(this.$children, "file-previewer");
          if (child != null) {
            child.showFilePreviewer(file);
          }
        },
        backToMoldDetail() {
          console.log("back to molds detail");
          const child = Common.vue.getChild(this.$children, "chart-mold");
          if (child != null) {
            child.backFromPreview();
          }
        },
        handleChangeTab(tabName) {
          this.tabName = tabName;
          this.isRemovePmWorkOrderFlag = true;
        },
      },
      created() {
        this.$watch(
          () => headerVm?.resourcesFake,
          (newVal) => {
            if (newVal && Object.keys(newVal)?.length) {
              this.resources = Object.assign({}, this.resources, newVal);
            }
          },
          { immediate: true }
        );

        this.$watch(
          () => headerVm?.currentUser,
          (newVal) => {
            if (newVal && Object.keys(newVal)?.length) {
              this.currentUser = Object.assign({}, this.currentUser, newVal);
            }
          },
          { immediate: true }
        );

        this.$watch(
          () => headerVm?.isReady,
          (newVal) => {
            console.log("🚀 🚀 🚀 🚀 🚀 HEADER is ready!!!", newVal);
            if (newVal) this.captureQuery();
          },
          { immediate: true }
        );

        this.$watch(
          () => headerVm?.myOrderKey,
          (newVal) => {
            console.log("headerVm?.orderKey -> change");
            this.myOrderKey++;
          }
        );
      },
      async mounted() {
        await this.fetchCompaniesList();
        this.fetchWorkOrderList();

        this.typeOptions = [...typeOptions];
        this.priorityOptions = [...priorityOptions];
        this.$nextTick(() => {
          this.requestParam.typeList = this.typeOptions.map((i) => i.id);
          this.requestParam.priorityTypeList = this.priorityOptions.map(
            (i) => i.id
          );
        });
      },
      computed: {
        isAllCompany() {
          return (
            this.companyOptions.length ===
            this.requestParam.companyIdList.length
          );
        },
        getDatePickerDisplay() {
          if (this.datepickerFrequency === "MONTHLY") {
            return moment(this.selectedDate.from).format("MMM, YYYY");
          }
          if (this.datepickerFrequency === "WEEKLY") {
            return `Week ${moment(this.selectedDate.from, "YYYY-WW").format(
              "WW, YYYY"
            )}`;
          }
          return this.selectedDate.fromTitle;
        },
      },
    });
  }
});
