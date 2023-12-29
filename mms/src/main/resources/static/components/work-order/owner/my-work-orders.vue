<template>
  <div class="my-work-orders-container-wrapper" @close="handleClose">
    <div class="header-title-wrapper">
      <div style="display: flex; justify-content: space-between">
        <div class="d-flex">
          <base-button
            :size="'medium'"
            :type="type == 'ASSIGNED' ? 'normal' : 'cancel'"
            :level="type == 'ASSIGNED' ? 'primary' : 'secondary'"
            @click="handleChangeType('ASSIGNED')"
            style="margin-right: 10px; width: 156px"
          >
            {{ resources["assigned_to_me"] }}
          </base-button>
          <base-button
            :size="'medium'"
            :type="type == 'CREATED' ? 'normal' : 'cancel'"
            :level="type == 'CREATED' ? 'primary' : 'secondary'"
            @click="handleChangeType('CREATED')"
            style="width: 156px"
          >
            {{ resources["created_by_me"] }}
          </base-button>
        </div>
        <div
          class="close-my-work-orders-wrapper d-flex flex-row align-items-center"
          @click="handleClose"
        >
          <span class="t-icon-close"></span>
          <span>{{ resources["close_my_orders"] }}</span>
        </div>
      </div>
    </div>
    <div class="list-work-orders-container">
      <list-work-orders
        :resources="resources"
        :handle-complete-work-order="handleCompleteWorkOrder"
        :handle-view-work-order="handleViewWorkOrder"
        :handle-update-status-work-order="handleUpdateStatusWorkOrder"
        :handle-edit-work-order="handleEditWorkOrder"
        :handle-reopen-work-order="handleReopenWorkOrder"
        :handle-close-edit-work-order="handleCloseEditWorkOrder"
        :handle-approve-reject-change-requested="
          handleApproveRejectChangeRequested
        "
        :is-all-company="true"
        :current-user="currentUser"
        :results="results"
        :is-loading="isLoading"
        :results-stat="resultsStat"
        :total="total"
        :pagination="pagination"
        :request-param="requestParam"
        :reload-check-key="reloadListKey"
        :total-pages="totalPages"
        @on-paging="fetchWorkOrderList"
        :fixed-columns="fixedColumns"
        :handle-open-change-requested="handleOpenChangeRequested"
      >
      </list-work-orders>
    </div>
    <div style="height: 43px"></div>
  </div>
</template>

<script>
const PAGE_SIZE = 10;

const FIXED_COLUMN_LIST = [
  {
    label: "Work Order ID",
    labelKey: "work_order_id",
    field: "workOrderId",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "workOrderId",
    position: 1,
    defaultPosition: 1,
  },
  {
    label: "Type",
    labelKey: "type",
    field: "orderType",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "orderType",
    position: 2,
    defaultPosition: 2,
  },
  {
    label: "Priority",
    labelKey: "priority",
    field: "priority",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "priority",
    position: 3,
    defaultPosition: 3,
  },
  {
    label: "Created by",
    labelKey: "created_by",
    field: "createdBy",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "createdBy",
    position: 4,
    defaultPosition: 4,
  },
  {
    label: "Assigned to",
    labelKey: "assigned_to",
    field: "workOrderUsers",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "workOrderUsers",
    position: 5,
    defaultPosition: 5,
  },
  {
    label: "Start Date",
    labelKey: "start_date",
    field: "start",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "start",
    position: 6,
    defaultPosition: 6,
  },
  {
    label: "Due Date",
    labelKey: "due_date",
    field: "end",
    default: true,
    defaultSelected: true,
    enabled: true,
    sortable: false,
    sortField: "end",
    position: 7,
    defaultPosition: 7,
  },

  {
    label: "Status",
    labelKey: "status",
    field: "workOrderStatus",
    default: true,
    enabled: true,
    defaultSelected: true,
    sortable: false,
    sortField: "workOrderStatus",
    position: 8,
    defaultPosition: 8,
  },
];
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    showDetail: Function,
    myOrderKey: {
      type: [Number, String],
    },
    handleCompleteWorkOrder: Function,
    handleViewWorkOrder: Function,
    handleUpdateStatusWorkOrder: Function,
    handleEditWorkOrder: Function,
    handleReopenWorkOrder: Function,
    handleCloseEditWorkOrder: Function,
    handleApproveRejectChangeRequested: Function,
    handleOpenChangeRequested: Function,
    currentUser: Object,
    reloadCheckKey: [Number, String],
  },
  components: {
    "my-list-work-orders": httpVueLoader(
      "/components/work-order/owner/my-work-order-dialog/my-list-work-orders.vue"
    ),
    "list-work-orders": httpVueLoader(
      "/components/work-order/list-work-orders.vue"
    ),
  },
  data() {
    return {
      type: "ASSIGNED",
      isLoading: false,
      results: [],
      resultsStat: {},
      total: 0,
      pagination: [],
      requestParam: {
        page: 1,
        isHistory: false,
        orderType: [],
      },
      totalPages: 0,
      tabName: "",
      fixedColumns: FIXED_COLUMN_LIST,
      reloadListKey: 0,
    };
  },
  mounted() {},
  created() {
    this.initData();
  },
  computed: {
    getModalTitle() {
      return this.resources["my_work_orders"];
    },
  },

  watch: {
    myOrderKey() {
      this.initData();
    },
    type() {
      this.fetchWorkOrderList(1);
    },
    reloadCheckKey() {
      this.reloadListKey++;
    },
    "requestParam.isHistory": function (newVal) {
      this.fixedColumns = this.fixedColumns.map((column) => {
        if (["start", "startedOn"].includes(column.field)) {
          return {
            ...column,
            labelKey: newVal ? "started_on" : "start_date",
            field: newVal ? "startedOn" : "start",
          };
        }
        if (["end", "completedOn"].includes(column.field)) {
          return {
            ...column,
            labelKey: newVal ? "completed_on" : "due_date",
            field: newVal ? "completedOn" : "end",
          };
        }
        return column;
      });
    },
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    clearData() {},
    handleChangeType(type) {
      this.type = type;
      this.reloadListKey++;
    },
    async fetchWorkOrderList(page) {
      try {
        this.isLoading = true;
        this.requestParam.page = page ? page : 1;
        const selectPage = page ? page : 1;
        let params = {
          orderType: this.requestParam.orderType,
          size: PAGE_SIZE,
          page: selectPage,
          isHistory: this.requestParam.isHistory,
        };
        if (this.type === "ASSIGNED") {
          params = { ...params, assignedToMe: true };
        } else {
          params = { ...params, createdByMe: true };
        }
        const res = await axios.get(`/api/work-order?${Common.param(params)}`);
        this.requestParam.page = selectPage;
        this.results = res.data.workOrderList.content;
        this.resultsStat = {
          general: res.data.general,
          inspection: res.data.inspection,
          emergency: res.data.emergency,
          pm: res.data.pm,
          cm: res.data.cm,
          refurbishment: res.data.refurbishment,
          disposal: res.data.disposal,
          total: res.data.total,
          history: res.data.totalHistory,
        };
        this.total = res.data.workOrderList.totalElements;
        this.pagination = Common.getPagingData(res.data.workOrderList);
        this.totalPages = res.data.workOrderList.totalPages;
      } catch (error) {
        console.log(error);
      } finally {
        this.isLoading = false;
      }
    },
    async initData() {
      await this.fetchWorkOrderList(1);
    },
  },
};
</script>

<style scoped>
.my-work-orders-container-wrapper {
  margin-left: -37px;
  width: calc(100% + 74px);
  margin-top: -22px;
}
.header-title-wrapper {
  background-color: #f5f8ff;
  padding: 11px 20px;
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
.t-icon-close {
  filter: invert(60%) sepia(50%) saturate(4901%) hue-rotate(192deg)
    brightness(96%) contrast(110%);
  margin-right: 4px;
}
.close-my-work-orders-wrapper {
  color: #3491ff;
  font-size: 14.66px;
  cursor: pointer;
}
.list-work-orders-container {
  margin: 0 20px;
}
.button-base__text {
  width: 100%;
  text-align: center;
}
</style>
