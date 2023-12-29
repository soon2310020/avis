<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg custom-fade-modal"
    body-classes="custom-modal__body"
    @close="handleClose"
  >
    <div class="modal__container">
      <div class="modal__container__header">
        <div
          style="
            display: flex;
            margin-bottom: 20px;
            justify-content: space-between;
          "
        >
          <ul class="type-bar">
            <li>
              <a
                href="javascript:void(0)"
                class="nav-button"
                :class="{ 'nav-button-selected': type == 'ASSIGNED' }"
                @click="handleChangeType('ASSIGNED')"
                >{{ resources["assigned_to_me"] }}</a
              >
            </li>
            <li>
              <a
                href="javascript:void(0)"
                class="nav-button"
                :class="{ 'nav-button-selected': type == 'CREATED' }"
                @click="handleChangeType('CREATED')"
                >{{ resources["created_by_me"] }}</a
              >
            </li>
          </ul>
        </div>
      </div>
      <div class="">
        <template v-if="getDataTable?.dataList?.length > 0 || isLoading">
          <my-requests-data-table
            :resources="resources"
            :data-list="getDataTable.dataList"
            :field-list="getDataTable.fieldList"
            :current-page="getDataTable.page"
            :total-page="getDataTable.totalPage"
            :is-loading="isLoading"
            @view-click="handleViewClick"
          ></my-requests-data-table>
        </template>
        <div
          v-else
          class="no-data-table d-flex no-data-table d-flex align-center justify-content-center"
        >
          <div style="text-align: center">
            <img src="/images/icon/data-requests/note.svg" alt="" />
            <div style="font-size: 14.66px; color: #b2b6ba; margin-top: 8px">
              {{ `${resources["you_have_no_requests"]}.` }}
            </div>
          </div>
        </div>
      </div>
      <div class="d-flex justify-end" style="margin-top: 20px">
        <base-paging
          :current-page="getDataTable.page"
          :total-page="getDataTable.totalPage"
          @next="handleNextPage"
          @back="handleBackPage"
        ></base-paging>
      </div>
    </div>
    <toast-alert
      @close-toast="closeToast"
      :show="toastVisible"
      :title="toastData.title"
      :content="toastData.content"
    >
    </toast-alert>
  </base-dialog>
</template>

<script>
const PAGE_SIZE = 10;
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    visible: {
      type: Boolean,
      default: () => false,
    },
    myOrderKey: {
      type: [Number, String],
    },
  },
  components: {
    "my-requests-data-table": httpVueLoader(
      "/components/data-requests/request-modal/my-requests/my-requests-data-table.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
  },
  data() {
    return {
      type: "ASSIGNED",
      assignedList: [],
      createdList: [],
      assignedPage: 1,
      assignedTotalPage: 1,
      createdPage: 1,
      createdTotalPage: 1,
      isLoading: false,
      tableKey: 0,
      page: 1,
      toastVisible: false,
      toastData: {
        title: "",
        content: "",
      },
    };
  },
  mounted() {
    this.assignedFields = [
      { title: this.resources["request_id"], field: "requestId" },
      { title: this.resources["type"], field: "requestDataType" },
      { title: this.resources["assigned_by"], field: "createdByUser" },
      { title: this.resources["due_date"], field: "dueDate" },
      // { title: this.resources['progress'], field: "progress" },
      { title: this.resources["status"], field: "status" },
      { title: this.resources["action"], field: "action" },
    ];
    this.createdFields = [
      { title: this.resources["request_id"], field: "requestId" },
      { title: this.resources["type"], field: "requestDataType" },
      { title: this.resources["requested_to"], field: "dataRequestUserList" },
      { title: this.resources["due_date"], field: "dueDate" },
      // { title: this.resources['progress'], field: "progress" },
      { title: this.resources["status"], field: "status" },
      { title: this.resources["action"], field: "action" },
    ];
    this.assignedList = [];
    this.createdList = [];
  },
  computed: {
    getModalTitle() {
      return this.resources["my_requests"];
    },
    getDataTable() {
      let result = {};
      if (this.type === "ASSIGNED") {
        result.fieldList = this.assignedFields;
        result.dataList = this.assignedList;
        result.page = this.assignedPage;
        result.totalPage = this.assignedTotalPage;
      } else if (this.type === "CREATED") {
        result.fieldList = this.createdFields;
        result.dataList = this.createdList;
        result.page = this.createdPage;
        result.totalPage = this.createdTotalPage;
      }
      // this.tableKey++;
      console.log("getDataTable", result);
      return result;
    },
  },
  watch: {
    visible(newVal) {
      console.log("visible", newVal);
      if (newVal) {
        this.initData();
      }
    },
    myOrderKey() {
      this.initData();
    },
  },
  methods: {
    handleNextPage() {
      this.handleChangePage("+");
    },
    handleBackPage() {
      this.handleChangePage("-");
    },
    async handleChangePage(sign) {
      if (sign === "+") {
        if (this.type === "ASSIGNED") {
          this.assignedPage++;
          await this.fetchAssignedOrders(this.assignedPage);
        } else if (this.type === "CREATED") {
          this.createdPage++;
          await this.fetchCreatedOrders(this.createdPage);
        }
      } else {
        if (this.type === "ASSIGNED") {
          this.assignedPage--;
          await this.fetchAssignedOrders(this.assignedPage);
        } else if (this.type === "CREATED") {
          this.createdPage--;
          await this.fetchCreatedOrders(this.createdPage);
        }
      }
    },
    handleClose() {
      this.$emit("close");
    },
    clearData() {},
    handleChangeType(type) {
      this.type = type;
    },
    async fetchAssignedOrders(page) {
      try {
        this.isLoading = true;
        const selectPage = page ? page : 1;
        const res = await axios.get(
          `/api/data-request?size=${PAGE_SIZE}&page=${selectPage}&sort=id%2Cdesc&isCreatedByMe=false&isAssignedToMe=true`
        );
        console.log("fetchAssignedOrders", res);
        this.assignedList = res.data.data.content;
        this.assignedPage = selectPage;
        this.assignedTotalPage = res.data.data.totalPages;
        this.page = selectPage;
      } catch (error) {
        console.log(error);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchCreatedOrders(page) {
      try {
        const selectPage = page ? page : 1;
        const res = await axios.get(
          `/api/data-request?size=${PAGE_SIZE}&page=${selectPage}&sort=id%2Cdesc&isCreatedByMe=true&isAssignedToMe=false`
        );
        console.log("fetchCreatedOrders", res);
        this.createdList = res.data.data.content;
        this.createdPage = selectPage;
        this.createdTotalPage = res.data.data.totalPages;
      } catch (error) {
        console.log(error);
      }
    },
    async initData() {
      this.isLoading = true;
      await this.fetchAssignedOrders();
      await this.fetchCreatedOrders();
      this.isLoading = false;
    },
    handleViewClick(request) {
      if (["OVERDUE", "IN_PROGRESS"].includes(request.status)) {
        this.$emit("show-request-list", request, true);
      } else {
        this.$emit("show-detail", request);
      }
    },
    closeToast() {
      this.toastVisible = false;
      setTimeout(() => {
        this.toastData.title = "";
        this.toastData.content = "";
        this.toastData.status = "";
      }, 1000);
    },
    handleShowToast(toastContent) {
      this.toastData.title = toastContent.title;
      this.toastData.content = toastContent.content;
      this.toastData.status = toastContent.status;
      this.$nextTick(() => {
        this.toastVisible = true;
        setTimeout(() => {
          this.closeToast();
        }, 3000);
      });
    },
  },
};
</script>

<style scoped>
.modal__footer {
  margin-top: 100px;
  justify-content: flex-end;
}

.modal__footer__button {
  margin-left: 8px;
}

.custom-modal__body {
  padding: 20px 25px !important;
}

.modal__container__form__row {
  margin: 10px 0;
  display: flex;
}

.modal__container__form__row label {
  width: 110px;
}

.back-icon {
  display: inline-flex;
  align-items: center;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}

.custom-fade-modal {
  z-index: 5000;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.back-icon {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}
.no-data-table {
  height: 459px;
  width: 100%;
}
</style>
