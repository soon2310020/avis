const logoImgSrc = {
  dyson: "/images/common/logo/dyson-logo.svg",
  ns0407: "/images/common/logo/nestle-logo.svg",
  "p&g": "/images/common/logo/p-&-g-logo.svg",
  zebra: "/images/common/logo/zebra-logo.svg",
  fc0616: "/images/common/logo/denso-logo.svg",
  mm0427: "/images/common/logo/mabe-logo.svg",
  lf0408: "/images/common/logo/loreal-logo.svg",
  di0402: "/images/common/logo/delonghi-logo.svg",
  beiesdoft: "/images/common/logo/beiersdorf-logo.svg",
  schaeffler: "/images/common/logo/schaeffler-logo.svg",
  bb0703: "/images/common/logo/boticario-logo.svg",
  cepheid: "/images/common/logo/cepheid-logo.svg",
  cb0413: "/images/common/logo/continental-logo.svg",
  eaton: "/images/common/logo/eaton-logo.svg",
  eb0702: "/images/common/logo/electrolux-logo.svg",
  tesla: "/images/common/logo/tesla-logo.svg",
  volvo: "/images/common/logo/volvo-logo.svg",
  ju0316: "/images/common/logo/jaguar-logo.svg",
  "abb-pilot": "/images/common/logo/abb-logo.svg",
  "abb-china": "/images/common/logo/abb-logo.svg",
  stanley: "/images/common/logo/stanley-logo.svg",
  "en-usa": "/images/common/logo/electrolux-logo.svg",
  nice: "/images/common/logo/nice-logo.svg",
  "abb-china": "/images/common/logo/abb-logo.svg",
  "adient-0422": "/images/common/logo/adient-logo.svg",
  philips: "/images/common/logo/versuni-logo.svg",
  ju0728: "/images/common/logo/jabil-logo.svg",
  di0711: "/images/common/logo/denso-logo.svg",
  default: "/images/common/logo/emoldino-logo.svg",
};
let downloadAppHeader = document.getElementById("buttonDownload");

if (downloadAppHeader) {
  var downloadApp = new Vue({
    el: "#buttonDownload",
    components: {
      "download-app-modal": httpVueLoader(
        "/components/download-app/download-app-modal.vue"
      ),
    },
    methods: {
      onOpenDownloadAppModal: function () {
        var child = Common.vue.getChild(this.$children, "download-app-modal");
        if (child != null) {
          child.showModal();
        }
      },
    },
  });
}
let headerElement = document.getElementById("headersId");

if (headerElement) {
  var headerVm = new Vue({
    el: "#headersId",
    components: {
      "all-companies-dropdown": httpVueLoader(
        "/components/all-companies-dropdown.vue"
      ),
      "category-form": httpVueLoader("/components/category-form.vue"),
      "cm-approval-dialog": httpVueLoader(
        "/components/work-order/src/cm-approval-dialog.vue"
      ),
      "company-details-form": httpVueLoader(
        "/components/company-details-form.vue"
      ),
      "company-filter": httpVueLoader(
        "/components/chart-new/data-completion-rate/company-filter.vue"
      ),
      "created-work-order-dialog": httpVueLoader(
        "/components/work-order/src/created-work-order-dialog.vue"
      ),
      "creator-data-completion-modal": httpVueLoader(
        "/components/data-requests/request-modal/creator-modal/creator-data-completion-modal.vue"
      ),
      "creator-data-registration-modal": httpVueLoader(
        "/components/data-requests/request-modal/creator-modal/creator-data-registration-modal.vue"
      ),
      "creator-work-order-dialog": httpVueLoader(
        "/components/work-order/creator/creator-work-order-dialog.vue"
      ),
      "data-entry-modal": httpVueLoader(
        "/components/data-requests/request-modal/data-entry/data-entry-modal.vue"
      ),
      "location-form": httpVueLoader("/components/location-form.vue"),
      "machine-details-form": httpVueLoader(
        "/components/machine-details-form.vue"
      ),
      "mold-details-form": httpVueLoader("/components/mold-details-form.vue"),
      "my-requests": httpVueLoader(
        "/components/data-requests/request-modal/my-requests/my-requests.vue"
      ),
      "my-work-order-dialog": httpVueLoader(
        "/components/work-order/owner/my-work-order-dialog.vue"
      ),
      "notification-wrapper": httpVueLoader(
        "/components/notification/notification-wrapper.vue"
      ),
      "order-modal": httpVueLoader("/components/order-modal.vue"),
      "part-details-form": httpVueLoader("/components/part-details-form.vue"),
      "reported-work-order-dialog": httpVueLoader(
        "/components/work-order/src/reported-work-order-dialog.vue"
      ),
      "reason-dialog": httpVueLoader(
        "/components/work-order/src/reason-dialog.vue"
      ),
      "request-change-work-order-dialog": httpVueLoader(
        "/components/work-order/src/request-change-work-order-dialog.vue"
      ),
      "request-detail-modal": httpVueLoader(
        "/components/data-requests/request-modal/editor-modal/request-detail-modal.vue"
      ),
      "request-list-modal": httpVueLoader(
        "/components/data-requests/request-modal/editor-modal/request-list-modal.vue"
      ),
      "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
      "view-reason": httpVueLoader(
        "/components/data-requests/request-modal/editor-modal/view-reason.vue"
      ),
    },

    data() {
      return {
        visible: false,
        clickFromPinLocal: Common.getClickFromPin(),
        isDisplay: "#BD0060",
        disable: false,
        href: "",
        monthDay: [
          "Jan",
          "Feb",
          "Mar",
          "Apr",
          "May",
          "Jun",
          "Jul",
          "Aug",
          "Sep",
          "Oct",
          "Nov",
          "Dec",
        ],
        listNotifications: {},
        resourcesFake: {},
        objectId: "",
        options: {},
        showWorkOrdersModal: false,
        isVisibleCm: false,
        workOrderId: "",
        showDetailModal: false,
        myOrderKey: 0,
        selectedDetail: {},
        chooseWorkOrder: {},
        chooseWorkOrderAssets: [],
        workorderModalVisible: false,
        isVisibleRequestChange: false,
        workOrderDetail: null,
        snackbar: {
          show: false,
          title: "",
          content: "",
          status: "",
        },
        reviewMode: "history",
        workOrderType: {
          REFURBISHMENT: "Refurbishment",
          DISPOSAL: "Disposal",
        },
        visibleForReason: false,
        workOrderReason: {},
        showCompleteDialog: false,
        isCompleteViewOnly: false,
        workOrderReported: {},
        reasonType: "",
        currentUser: null,
        userType: "",
        versions: [],
        permissions: {},
        showDataRequestDetail: false,
        selectedDataRequest: {},
        isShowMyRequests: false,
        isShowRequestListModal: false,
        visibleDataEntry: false,
        dataEntryType: "",
        completionModalType: "CREATE",
        registrationModalType: "CREATE",
        isShowCreateDataRegistration: false,
        isShowCreateDataCompletion: false,
        selectedDataRequestReason: {},
        showDataRequestReason: false,
        reloadListModalKey: 0,
        //
        systemCodes: null,
        listUsers: [],
        listCompanies: [],
        listLocations: [],
        listCategories: [],
        dataEntryModalType: "COMPLETE",

        // popup status
        isCmWorkOrderModalReady: false, // refer: components\work-order\src\cm-approval-dialog.vue
        isCreatedWorkOrderModalReady: false, // refer: components\work-order\src\created-work-order-dialog.vue
        isLoadingRequestChange: false,
      };
    },
    computed: {
      logoImageSrc() {
        return this.options?.CLIENT?.logoUrl;
      },
      isReady() {
        console.log("isCmWorkOrderModalReady", this.isCmWorkOrderModalReady);
        console.log(
          "isCreatedWorkOrderModalReady",
          this.isCreatedWorkOrderModalReady
        );
        return (
          this.isCmWorkOrderModalReady && this.isCreatedWorkOrderModalReady
        );
      },
    },
    created() {
      this.changeNotifications();
      this.isCompletionPage();
      this.getResources();
    },
    watch: {
      visible(newVal) {
        if (newVal) {
          Common.changeClickFromPin(true);
          this.clickFromPinLocal = Common.getClickFromPin();
        } else {
          if ($("#op-order-popup").hasClass("show")) {
            Common.changeClickFromPin(false);
            self.clickFromPinLocal = Common.getClickFromPin();
          }
        }
      },
    },
    methods: {
      getWorkOrderTypeTitle(type) {
        switch (type) {
          case "GENERAL":
            return "General Workorder";
          case "INSPECTION":
            return "Inspection Workorder";
          case "EMERGENCY":
            return "Emergency Workorder";
          case "PREVENTATIVE_MAINTENANCE":
            return "P.M. Workorder";
          case "CORRECTIVE_MAINTENANCE":
            return "C.M. Workorder";
          case "DISPOSAL":
            return "Disposal Workorder";
          case "REFURBISHMENT":
            return "Refurbishment Workorder";
          default:
            return "General Workorder";
        }
      },
      async resetAfterUpdate() {},
      getDatetime($timestamp) {
        var today = new Date();
        var today1 = new Date($timestamp.createdDateTime);
        var date = today.getDate() - today1.getDate();
        var month = today.getMonth() - today1.getMonth();
        var year = today.getFullYear() - today1.getFullYear();
        if (date >= 1 || month >= 1 || year >= 1) {
          let updatedAt =
            today1.getDate() +
            " " +
            this.monthDay[today1.getMonth()] +
            " " +
            today1.getFullYear();
          return updatedAt;
        } else {
          return moment($timestamp.updatedAt * 1000).fromNow();
        }
      },
      changeNotifications() {
        axios
          .get("/api/notification/list-of-user")
          .then((response) => {
            this.listNotifications = response.data;
            if (this.listNotifications && this.listNotifications.dataList) {
              this.listNotifications.dataList.forEach((item) => {
                let hrefLink = item.isAlertCenter
                  ? "/front/alert-center?menuKey=" +
                    encodeURI(item.menuKey) +
                    "&systemNoteFunction=" +
                    item.systemNoteFunction +
                    "&"
                  : item.menuKey + (!item.menuKey.includes("?") ? "?" : "&");
                if (item.systemNoteFunction === "SUPPORT_CENTER") {
                  hrefLink += "param=" + item.objectFunctionId;
                } else if (item.notificationType === "DATA_COMPLETION") {
                } else {
                  hrefLink +=
                    "param=" +
                    item.objectFunctionId +
                    "&systemNoteFunction=" +
                    item.systemNoteFunction +
                    "&notificationType=" +
                    item.notificationType +
                    "&systemNoteId=" +
                    item.systemNoteId;
                }
                item.hrefLink = hrefLink;
              });
            }
            console.log("listNotifications", response.data);
          })
          .catch((error) => {
            console.log(error.response);
          });
      },
      statusNotifications() {
        this.disable = !this.disable;
        this.isDisplay = this.disable ? "none" : "#BD0060";
        axios
          .post(
            "/api/notification/change-status-notification-system-node?disable=" +
              this.disable
          )
          .then((response) => {
            this.changeNotifications();
          })
          .catch((error) => {
            console.log(error.response);
          });
      },
      clearNotification(item) {
        axios
          .post("/api/notification/clear/" + item)
          .then((response) => {
            console.log("clear notifications", response);
            this.changeNotifications();
          })
          .catch((error) => {
            console.log(error.response);
          });
      },
      clearAll() {
        axios
          .post("/api/notification/clear-all")
          .then((response) => {
            console.log(response);
            this.changeNotifications();
          })
          .catch((error) => {
            console.log(error.response);
          });
      },
      showDataCompletion() {
        this.visible = false;
        var child = Common.vue.getChild(this.$children, "order-modal");
        console.log("showDataCompletion2 ", child);
        if (child != null) {
          child.showOrderPopup("received");
        }
      },
      isCompletionPage() {
        let url_string = window.location.href;
        this.objectId = /[^/]*$/.exec(url_string)[0];
      },
      showCompleteData2(item, name) {
        console.log(item, name, "heeheh");
        switch (name) {
          case "COMPANY":
            this.showCompanyDetailsForm2(item.data);
            break;
          case "TOOLING":
            let dataFake = item;
            dataFake.id = item.data.id;
            this.showMoldDetailsForm2(dataFake);
            break;
          case "MACHINE":
            this.showMachineDetailsForm2(item.data);
            break;
          case "PART":
            this.showPartDetailsForm2(item.data);
            break;
          case "LOCATION":
            this.showLocationForm2(item.data);
            break;
          case "CATEGORY":
            let dataFake2 = item;
            dataFake2.id = item.data.id;
            this.showCategoryForm2(dataFake2);
            break;
        }
      },
      showCompanyDetails: function (company) {
        var child = Common.vue.getChild(this.$children, "company-details");
        if (child != null) {
          child.showDetails(company.data);
        }
      },
      showCompanyDetailsForm2: function (company) {
        var child = Common.vue.getChild(this.$children, "company-details-form");
        console.log(`Company: `, child);
        if (child != null) {
          child.showDetails(company);
        }
      },
      showChartPart: function (part) {
        var child = Common.vue.getChild(this.$children, "chart-part");
        if (child != null) {
          child.showChart(part, "QUANTITY", "DAY");
        }
      },
      showMachineDetails: function (company) {
        var child = Common.vue.getChild(this.$children, "machine-details");
        if (child != null) {
          child.showDetails(company.data);
        }
      },
      showMachineDetailsForm2: function (company) {
        var child = Common.vue.getChild(this.$children, "machine-details-form");
        if (child != null) {
          child.showDetails(company);
        }
      },
      showPartDetails: function (part) {
        var child = Common.vue.getChild(this.$children, "part-details");
        if (child != null) {
          child.showPartDetails(part.data);
        }
      },
      showPartDetailsForm2: function (part) {
        var child = Common.vue.getChild(this.$children, "part-details-form");
        if (child != null) {
          child.showPartDetails(part);
        }
      },
      showLocationForm2: function (location) {
        console.log(location, "location");
        var child = Common.vue.getChild(this.$children, "location-form");
        if (child != null) {
          child.showForm(location);
        }
      },
      showCategoryForm2: function (category) {
        var child = Common.vue.getChild(this.$children, "category-form");
        if (child != null) {
          child.showForm(category);
        }
      },
      showChart: function (mold) {
        var child = Common.vue.getChild(this.$children, "chart-mold");
        if (child != null) {
          child.showChart(mold.data, "QUANTITY", "DAY");
        }
      },
      backToMoldDetail() {
        console.log("back to molds detail");
        var child = Common.vue.getChild(this.$children, "chart-mold");
        if (child != null) {
          child.backFromPreview();
        }
      },
      backToMoldDetail2() {
        console.log("back to molds detail");
        var child = Common.vue.getChild(this.$children, "mold-details-form");
        if (child != null) {
          child.backFromPreview();
        }
      },
      showFilePreviewer(file) {
        var child = Common.vue.getChild(this.$children, "file-previewer");
        if (child != null) {
          child.showFilePreviewer(file);
        }
      },
      showMoldDetails: function (mold) {
        console.log("Mold ne:", mold);
        var child = Common.vue.getChild(this.$children, "mold-details");
        if (child != null) {
          let reasonData = { reason: "", approvedAt: "" };
          if (mold.dataSubmission === "DISAPPROVED") {
            axios
              .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
              .then(function (response) {
                reasonData = response.data;
                // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
                reasonData.approvedAt = reasonData.approvedAt
                  ? vm.convertTimestampToDateWithFormat(
                      reasonData.approvedAt,
                      "MMMM DD YYYY HH:mm:ss"
                    )
                  : "-";
                mold.notificationStatus = mold.dataSubmission;
                mold.reason = reasonData.reason;
                mold.approvedAt = reasonData.approvedAt;
                mold.approvedBy = reasonData.approvedBy;
                child.showMoldDetails(mold.data);
              });
          } else {
            child.showMoldDetails(mold.data);
          }
        }
      },
      showMoldDetailsForm2: function (mold) {
        console.log(mold);
        var child = Common.vue.getChild(this.$children, "mold-details-form");
        if (child != null) {
          // this.changeItem(mold.id)
          console.log("as");
          let reasonData = { reason: "", approvedAt: "" };
          if (mold.dataSubmission === "DISAPPROVED") {
            axios
              .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
              .then(function (response) {
                reasonData = response.data;
                // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
                reasonData.approvedAt = reasonData.approvedAt
                  ? vm.convertTimestampToDateWithFormat(
                      reasonData.approvedAt,
                      "MMMM DD YYYY HH:mm:ss"
                    )
                  : "-";
                mold.notificationStatus = mold.dataSubmission;
                mold.reason = reasonData.reason;
                mold.approvedAt = reasonData.approvedAt;
                mold.approvedBy = reasonData.approvedBy;
                child.showMoldDetails(mold);
              });
          } else {
            child.showMoldDetails(mold);
          }
        }
      },
      showPartChart: function (part) {
        part.id = part.partId;
        var child = Common.vue.getChild(this.$children, "chart-part");
        if (child != null) {
          child.showChart(part.data, "QUANTITY", "DAY");
        }
      },
      changeOpenOrderModal(boolean) {},
      handleOpenWorkOrderModal() {
        this.showWorkOrdersModal = true;
        this.visible = false;
      },
      handleCloseWorkOrderModal() {
        this.showWorkOrdersModal = false;
      },
      handleOpenCmView(id) {
        this.workOrderId = id;
        this.isVisibleCm = true;
        this.visible = false;
      },
      async handleOpenEditCM(id) {
        this.visible = false;
        await this.openEditWorkOrder({ id: id });
      },
      handleCloseCmView() {
        console.log("@handleCloseCmView");
        this.isVisibleCm = false;
      },
      async handleOpenRequestChange(id) {
        console.log("@handleOpenRequestChange", id);
        this.workOrderId = id;
        this.visible = false;
        this.reviewMode = "view";
        this.isVisibleRequestChange = true;
        try {
          const response = await axios.get(`/api/work-order/${id}`);
          this.workOrderDetail = response.data.data;
        } catch (err) {
          console.log(err);
        }
      },
      async handleAcceptRequestChange(workOrder) {
        console.log("@handleAcceptRequestChange", workOrder);
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
          console.log(response);
          this.handleCloseRequestChange();
          this.snackbar.title = "Success!";
          this.snackbar.content = "Approved Work Order changes";
          this.snackbar.title = "Approved";
          this.snackbar.content = `Changes to ${workOrder.workOrderId} have been approved.`;
          this.$nextTick(() => {
            this.snackbar.show = true;
          });
          this.isLoadingRequestChange = false;
        } catch (error) {
          console.log(error);
          Common.alert("error");
        } finally {
          setTimeout(() => {
            this.handleCloseNotification();
          }, 3000);
        }
      },

      async handleDeclineRequestChange(workOrder, reason) {
        if (this.isLoadingRequestChange) {
          return;
        }
        this.isLoadingRequestChange = true;
        console.log("@handleDeclineRequestChange", workOrder, reason);
        try {
          const params = { approved: false, reason: reason };
          const response = await axios.post(
            `/api/work-order/approve-change?id=${workOrder.id}`,
            params
          );
          console.log(response);
          this.handleCloseRequestChange();
          this.snackbar.title = "Rejected!";
          this.snackbar.content = `You have rejected changes to ${workOrder.workOrderId} and notification has been sent to ${workOrder.createdBy.displayName}.`;
          this.$nextTick(() => {
            this.snackbar.show = true;
          });
          this.isLoadingRequestChange = false;
        } catch (error) {
          console.log(error);
        } finally {
          setTimeout(() => {
            this.handleCloseNotification();
          }, 3000);
        }
      },
      handleCloseRequestChange() {
        this.isVisibleRequestChange = false;
      },
      handleCloseNotification() {
        this.snackbar.show = false;
        // setTimeout(() => {
        //     this.snackbar.title = '';
        //     this.snackbar.content = '';
        //     this.snackbar.status = '';
        // }, 1000);
      },
      async showDetail(item) {
        const workOrderId = item.id ? item.id : item;
        this.isLoading = true;
        this.showDetailModal = true;
        this.visible = false;
        try {
          const response = await axios.get(`/api/work-order/${workOrderId}`);
          this.selectedDetail = response.data.data;
        } catch (error) {
          console.log(error);
          this.showDetailModal = false;
        } finally {
          this.isLoading = false;
        }
      },
      handleCloseDetail() {
        this.showDetailModal = false;
      },
      handleReloadMyOrderTable() {
        this.myOrderKey++;
      },
      closeCreateWorkOrderModal() {
        this.workorderModalVisible = false;
      },
      async openEditWorkOrder(item) {
        await this.setSelectedWorkOrder(item.id);
        this.workorderModalVisible = true;
      },
      closeEditWorkOrder() {
        this.workorderModalVisible = false;
        this.chooseWorkOrder = {};
      },
      async setSelectedWorkOrder(id) {
        const res = await axios.get(`/api/work-order/${id}`);
        const workOrder = res.data.data;
        const attachmentsRes = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_FILE&refId=${id}`
        );
        const attachments = attachmentsRes.data;
        this.chooseWorkOrder = workOrder;
        this.chooseWorkOrder.attachments = attachments;
        this.chooseWorkOrderAssets =
          this.chooseWorkOrder?.workOrderAssets?.map((wo) => ({
            ...wo,
            title: wo.assetCode,
            checked: true,
            assetView: wo.type.toLowerCase(),
            id: wo.assetId,
          })) || [];
        console.log("setSelectedWorkOrder", this.chooseWorkOrder, res);
      },
      async getListUsers(isForced = false) {
        if (localStorage.getItem("users") && !isForced) {
          this.listUsers = JSON.parse(localStorage.getItem("users"));
          return JSON.parse(localStorage.getItem("users"));
        }
        try {
          // TODO: RECHECK ENDPOINT
          // const response = await axios.get(
          //   "/api/users?status=active&query=&page=1&size=9999"
          // );
          const response = await axios.get("/api/system-note/user-lite-list");
          localStorage.setItem("users", JSON.stringify(response.data.dataList));
          this.listUsers = response.data.dataList;
          return response.data.dataList;
        } catch (err) {
          console.log(err);
        }
      },
      async getListCompanies(isForced = false) {
        if (localStorage.getItem("companies") && !isForced) {
          this.listCompanies = JSON.parse(localStorage.getItem("companies"));
          return JSON.parse(localStorage.getItem("companies"));
        }
        try {
          const response = await axios.get(
            "/api/companies?query&size=999999&status=active&sort=id%2Cdesc&page=1"
          );
          localStorage.setItem(
            "companies",
            JSON.stringify(response.data.content)
          );
          this.listCompanies = response.data.content;
          return response.data.content;
        } catch (error) {
          console.log(error);
        }
      },
      async getListCategories(isForced = false) {
        if (localStorage.getItem("categories") && !isForced) {
          this.listCategories = JSON.parse(localStorage.getItem("categories"));
          return JSON.parse(localStorage.getItem("categories"));
        }
        try {
          const response = await axios.get("/api/categories?size=1000");
          localStorage.setItem(
            "categories",
            JSON.stringify(response.data.content)
          );
          this.listCategories = response.data.content;
          return response.data.content;
        } catch (error) {
          console.log(error);
        }
      },
      async getListLocations(isForced = false) {
        if (localStorage.getItem("locations") && !isForced) {
          this.listLocations = JSON.parse(localStorage.getItem("locations"));
          return JSON.parse(localStorage.getItem("locations"));
        }
        try {
          const response = await axios.get(
            "/api/locations?query=&status=active&sort=id%2Cdesc&page=1"
          );
          localStorage.setItem(
            "locations",
            JSON.stringify(response.data.content)
          );
          this.listLocations = response.data.content;
          return response.data.content;
        } catch (error) {
          console.log(error);
        }
      },
      async getCodes(isForced = false) {
        if (localStorage.getItem("codes") && !isForced) {
          return JSON.parse(localStorage.getItem("codes"));
        }
        try {
          const response = await axios.get("/api/codes");
          localStorage.setItem("codes", JSON.stringify(response.data));
          return response.data;
        } catch (error) {
          console.log(error);
        }
      },
      async getResources() {
        let globalState = {};
        if (!sessionStorage.getItem("me")) {
          globalState = await Common.getSystem();
        } else {
          globalState = { ...sessionStorage };
        }
        const codes = await this.getCodes();

        this.resourcesFake = JSON.parse(globalState.messages);
        console.log("this.resourcesFake", this.resourcesFake);
        this.currentUser = JSON.parse(globalState.me);
        this.options = JSON.parse(globalState.options);
        this.versions = JSON.parse(globalState.versions);
        this.permissions = JSON.parse(globalState.menuPermissions);
        this.userType = globalState.type;
        this.systemCodes = codes;

        //
        this.$nextTick(() => {
          let label = "";
          const userTag = this.$refs["user-type"];
          const headerTabs = this.$refs["header-tabs"];

          if (this.userType === "OEM") {
            label = "OEM";
            userTag.style.backgroundImage =
              "linear-gradient(to right, #1d62bf, #0caded)";
            userTag.classList.add("oem");
          }
          if (this.userType === "SUPPLIER") {
            label = "SUPPLIER";
            userTag.style.backgroundImage =
              "linear-gradient(to right, #f3b966, #ed8060)";
            userTag.classList.add("supplier");
            headerTabs.style.width = "calc(100% - 350px)";
          }
          if (this.userType === "TOOLMAKER") {
            label = "TOOLMAKER";
            userTag.style.backgroundImage =
              "linear-gradient(to right, #ec7b5f, #cf526e)";
            userTag.classList.add("toolmaker");
            headerTabs.style.width = "calc(100% - 380px)";
          }

          const text = document.createTextNode(label);
          userTag.appendChild(text);
        });
      },
      async handleReviewWorkOrder(id) {
        console.log("@handleReviewWorkOrder", id);
        this.workOrderId = id;
        this.visible = false;
        this.reviewMode = "view";
        this.isVisibleRequestChange = true;
        try {
          const response = await axios.get(`/api/work-order/${id}`);
          this.workOrderDetail = response.data.data;
        } catch (err) {
          console.log(err);
        }
      },
      async handleViewReason(id, reasonType) {
        console.log("@handleViewReason", id);
        this.visible = false;
        this.visibleForReason = true;
        this.reasonType = reasonType;
        try {
          const response = await axios.get(`/api/work-order/${id}`);
          this.workOrderReason = response.data.data;
        } catch (err) {
          console.log(err);
        }
      },
      closeReasonView() {
        this.visibleForReason = false;
      },
      showToast(title, message, status) {
        this.snackbar.title = title;
        this.snackbar.content = message;
        this.snackbar.status = status;
        this.$nextTick(() => {
          this.snackbar.show = true;
          setTimeout(() => {
            this.handleCloseNotification();
          }, 3000);
        });
      },
      async handleResubmitWorkOrder(id) {
        try {
          const response = await axios.get(`/api/work-order/${id}`);
          this.workOrderReported = response.data.data;
          this.showCompleteDialog = true;
          this.isCompleteViewOnly = false;
          this.closeReasonView();
        } catch (err) {
          console.log(err);
        }
      },
      async handleShowCompletedWorkOrder(id) {
        try {
          const response = await axios.get(`/api/work-order/${id}`);
          this.workOrderReported = response.data.data;
          this.showCompleteDialog = true;
          this.isCompleteViewOnly = true;
        } catch (err) {
          console.log(err);
        }
      },
      handleCloseCompleteDialog() {
        this.showCompleteDialog = false;
        this.workOrderReported = null;
      },
      async handleSubmitComplete(formData, isProgress) {
        try {
          console.log("@handleSubmitComplete:formData", formData);
          await axios.post("/api/work-order/complete", formData);
          this.handleCloseCompleteDialog();
          if (!isProgress) {
            this.showToast("Success!", "Your Work Order has been completed.");
          }
        } catch (error) {
          console.log(error);
          Common.alert("error");
        }
      },
      handleCloseCompleteDialog() {
        this.showCompleteDialog = false;
        this.workOrderReported = null;
      },
      handleShowToast(snackbar) {
        this.showToast(snackbar.title, snackbar.content);
      },

      // DATA REQUEST //
      async handleShowDataRequest(id) {
        this.selectedDataRequest = await this.getDataRequestById(id);
        this.showDataRequestDetail = true;
        this.visible = false;
      },

      handleCloseDetailModal() {
        this.showDataRequestDetail = false;
        this.selectedDataRequest = {};
      },

      async getDataRequestById(id) {
        try {
          const requestResponse = await axios.get(`/api/data-request/${id}`);
          const request = requestResponse.data;
          const attachmentResponse = await axios.get(
            `/api/file-storage?storageType=DATA_REQUEST_FILE&refId=${id}`
          );
          const attachment = attachmentResponse.data;
          request.uploadedAttachment = attachment;
          request.attachment = [];
          return request;
        } catch (error) {
          console.error("getDataRequestById() error:", error);
        }
      },

      handleShowMyRequests() {
        console.log("handleShowMyRequests");
        this.isShowMyRequests = true;
      },

      async handleShowDetailFromMyRequests(item) {
        this.selectedDataRequest = await this.handleShowDataRequest(item.id);
      },
      handleCloseMyRequests() {
        this.isShowMyRequests = false;
      },

      async handleOpenRequestListModal(item) {
        this.selectedDataRequest = await this.getDataRequestById(item.id);
        this.isShowRequestListModal = true;
      },
      handleCloseRequestListModal() {
        this.isShowRequestListModal = false;
      },
      handleShowMultipleCompletion(type, modalType) {
        console.log("handleShowMultipleCompletion", type);
        this.dataEntryType = type;
        this.dataEntryModalType = modalType;
        this.visibleDataEntry = true;
      },
      handleCloseDataEntryModal() {
        this.visibleDataEntry = false;
      },
      async handleOpenEditRequest(item) {
        this.selectedDataRequest = item;
        if (item.requestType === "DATA_REGISTRATION") {
          this.isShowCreateDataRegistration = true;
        } else {
          this.isShowCreateDataCompletion = true;
        }
        this.showDataRequestDetail = false;
        this.completionModalType = "EDIT";
        this.registrationModalType = "EDIT";
      },
      handleCloseRegistrationCreator() {
        this.isShowCreateDataRegistration = false;
      },
      handleCloseCompletionCreator() {
        this.isShowCreateDataCompletion = false;
      },
      async handleViewDataRequestReason(id) {
        console.log("handleViewDataRequestReason", id);
        const request = await this.getDataRequestById(id);
        this.selectedDataRequestReason = request;
        this.showDataRequestReason = true;
        this.visible = false;
      },
      handleCloseViewDataReason() {
        this.showDataRequestReason = false;
      },
      async handleReloadDetail(id) {
        this.selectedDataRequest = await this.getDataRequestById(id);
        this.reloadListModalKey++;
      },
      // END DATA REQUEST //
    },
    mounted() {
      this.$nextTick(async function () {
        Common.removeWave(500);
      });
    },
  });
}
