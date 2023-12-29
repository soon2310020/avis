<template>
  <div>
    <div v-show="isLoading" class="work-order-data-table loading-wave"></div>
    <div v-show="!isLoading" class="work-order-data-table">
      <table v-if="dataList.length > 0" class="table table-striped">
        <thead class="custom-header-table">
          <tr>
            <th
              v-for="(field, index) in fieldList"
              :key="index"
              class="text-left"
            >
              <span>{{ field.title }}</span>
            </th>
          </tr>
        </thead>
        <tbody class="op-list">
          <tr v-for="data in dataList" :key="data.id">
            <td
              v-for="(field, index) in fieldList"
              :key="index"
              class="text-left"
              style="height: 40px"
            >
              <template v-if="'workOrderId' === field.field">
                <span>{{ data[field.field] }}</span>
              </template>
              <template v-else-if="'orderType' === field.field">
                <span>{{ getFormatType(data[field.field]) }}</span>
              </template>
              <template v-else-if="'end' === field.field">
                <span>{{ formatDate(data.end) }}</span>
              </template>
              <template
                v-else-if="
                  ['priority', 'workOrderStatus'].includes(field.field)
                "
              >
                <base-priority-card :color="color[data[field.field]]">
                  {{ data[field.field].toLowerCase() }}
                </base-priority-card>
              </template>
              <template v-else-if="'workOrderUsers' === field.field">
                <base-avatar
                  v-for="(item, index) in getAssigneeUser(data.workOrderUsers)"
                  :key="index"
                  :item="item.user"
                  style="margin-right: 8px"
                >
                </base-avatar>
              </template>
              <template v-else-if="'createdBy' === field.field">
                <base-avatar :item="data.createdBy"> </base-avatar>
              </template>
              <template v-else-if="'action' === field.field">
                <span class="custom-hyper-link" @click="handleViewClick(data)">
                  {{ resources["view_workorder"] }}
                  <div class="hyper-link-icon"></div>
                </span>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="dataList.length == 0" class="no-results-table">
        {{ resources["no_results"] }}
      </div>
    </div>
    <div class="justify-end table-footer">
      <div class="modal-body__content__footer">
        <span>{{ `${getCurrentPage} of ${totalPage}` }}</span>
      </div>
      <a
        href="javascript:void(0)"
        class="paging-button"
        :class="{ 'inactive-button': getCurrentPage <= 1 }"
        @click="changePage('-')"
      >
        <img src="/images/icon/category/paging-arrow.svg" alt="" />
      </a>
      <a
        href="javascript:void(0)"
        class="paging-button"
        @click="changePage('+')"
        :class="{ 'inactive-button': getCurrentPage == totalPage }"
      >
        <img
          src="/images/icon/category/paging-arrow.svg"
          style="transform: rotate(180deg)"
          alt=""
        />
      </a>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    dataList: {
      type: Array,
      default: () => [],
    },
    fieldList: {
      type: Array,
      default: () => [],
    },
    currentPage: {
      type: Number,
      default: () => 1,
    },
    totalPage: {
      type: Number,
      default: () => 1,
    },
    isLoading: Boolean,
  },
  data() {
    return {};
  },
  created() {
    this.color = {
      HIGH: "red",
      MEDIUM: "yellow",
      LOW: "green",
      Upcoming: "yellow",
      UPCOMING: "yellow",
      Requested: "purple",
      REQUESTED: "purple",
      OVERDUE: "red",
      Overdue: "red",
      Declined: "grey",
      DECLINED: "grey",
      Cancelled: "dark-grey",
      CANCELLED: "dark-grey",
      PENDING_APPROVAL: "blue",
      "Pending Approval": "blue",
    };
  },
  computed: {
    getCurrentPage() {
      if (this.currentPage) {
        if (this.totalPage == 0) {
          return 0;
        } else {
          return this.currentPage;
        }
      } else {
        return 1;
      }
    },
  },
  methods: {
    getAssigneeUser(users) {
      return (users || []).filter(
        (item) => item.participantType === "ASSIGNEE"
      );
    },
    changePage(sign) {
      this.$emit("change-page", sign);
    },
    formatDate(timeStamp) {
      const date = new Date(timeStamp * 1000);
      return moment(date).format("YYYY-MM-DD");
    },
    handleViewClick(workOrder) {
      this.$emit("view-click", workOrder);
    },
    getFormatType(type) {
      if (type === "PREVENTATIVE_MAINTENANCE") {
        return "P.M.";
      } else if (type === "CORRECTIVE_MAINTENANCE") {
        return "C.M.";
      } else {
        return Common.formatter.toCase(type, "capitalize");
      }
    },
  },
};
</script>

<style scoped>
.work-order-data-table {
  border: 1px solid #d6dade;
  border-radius: 4px;
  height: 459px;
  overflow-y: auto;
}

.table-footer {
  margin-top: 10px;
  height: 25px;
  display: flex;
}
</style>
