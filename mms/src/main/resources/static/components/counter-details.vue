<template>
  <div
    id="op-counter-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['counter_id'] + ' :'"></span>
              {{ counter.equipmentCode }}
            </h4>
            <button
              type="button"
              class="close"
              v-on:click="dimissModal"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['counter']"></span>
              </div>

              <table class="table table-mold-details">
                <tbody>
                  <tr>
                    <th v-text="resources['counter_id']"></th>
                    <td>{{ counter.equipmentCode }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['tooling_id']"></th>
                    <td>
                      {{ counter.mold ? counter.mold.equipmentCode : "" }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['installation_info']"></span>
              </div>

              <table class="table table-mold-details">
                <tbody>
                  <tr>
                    <th v-text="resources['preset']"></th>
                    <td>{{ counter.presetCount }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['installation_date']"></th>
                    <td>{{ counter.installedAt }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['activation_date']"></th>
                    <td>{{ formatToDate(counter.activatedAt) }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['installation_personnel']"></th>
                    <td>{{ counter.installedBy }}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['status']"></th>
                    <td class="titleCase">
                      <span v-if="counter.equipmentStatus == 'INSTALLED'"
                        >MATCHED</span
                      >
                      <span v-if="counter.equipmentStatus == 'AVAILABLE'"
                        >UNMATCHED</span
                      >
                      <span
                        v-else-if="
                          counter.equipmentStatus != 'AVAILABLE' &&
                          counter.equipmentStatus != 'INSTALLED'
                        "
                        >{{ counter.equipmentStatus }}</span
                      >
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['operation_status']"></th>
                    <td
                      class="titleCase"
                      v-if="counter.operatingStatus === 'WORKING'"
                      v-text="resources['active']"
                    ></td>
                    <td
                      class="titleCase"
                      v-else-if="counter.operatingStatus === 'NOT_WORKING'"
                      v-text="resources['inactive']"
                    ></td>
                    <td class="titleCase" v-else>
                      {{ counter.operatingStatus }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['document']"></th>
                    <td>
                      <!--                      <img-->
                      <!--                        v-for="file in counterFiles"-->
                      <!--                        :src="file.saveLocation"-->
                      <!--                        style="height: 50px; width: 50px; margin-right: 5px;"-->
                      <!--                      />-->
                      <preview-images-system
                        :images-uploaded="counterFiles"
                      ></preview-images-system>
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['memo']"></th>
                    <td>{{ counter.memo }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['subscription_information']"></span>
              </div>

              <table
                id="mold-detail-subscription_information"
                class="table table-mold-details"
              >
                <tbody>
                  <tr>
                    <th v-text="resources['subscription_status']"></th>
                    <td>{{ counter.subscriptionStatus }}</td>
                  </tr>

                  <tr>
                    <th v-text="resources['subscription_term']"></th>
                    <td>
                      {{ counter.subscriptionTerm }}
                      {{
                        counter.subscriptionTerm
                          ? counter.subscriptionTerm > 1
                            ? "Years"
                            : "Year"
                          : ""
                      }}
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['subscription_expiry']"></th>
                    <td>{{ formatToDate(counter.subscriptionExpiry) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span v-text="resources['work_order_information']"></span>
              </div>

              <table
                id="mold-detail-work-order-information"
                class="table table-mold-details"
              >
                <tbody>
                  <tr>
                    <th v-text="resources['last_work_order_date']"></th>
                    <td>
                      {{
                        formatWorkOrderDate(counter?.lastWorkOrder?.completedOn)
                      }}
                      <span
                        class="custom-hyper-link"
                        style="margin-left: 4px"
                        @click="showWorkOrderDialog = true"
                        v-show="!!counter?.lastWorkOrder"
                      >
                        {{ lastWorkOrderTypeTitle }}
                        <div
                          class="hyper-link-icon"
                          style="margin-left: 3px"
                        ></div>
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['work_order_history']"></th>
                    <td>
                      <span
                        class="custom-hyper-link"
                        @click="goToWorkOrderDetailPage"
                        v-show="counter?.workOrderHistory > 0"
                      >
                        {{ workOrderHistoryText }}
                        <div
                          class="hyper-link-icon"
                          style="margin-left: 3px"
                        ></div>
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer text-center">
            <button
              type="button"
              class="btn btn-secondary"
              v-on:click="dimissModal"
              v-text="resources['close']"
            ></button>
          </div>
        </div>
      </div>
    </form>
    <reported-work-order-dialog
      :resources="resources"
      :visible="showWorkOrderDialog"
      :selected="counter?.lastWorkOrder"
      :view-only="true"
      @close="showWorkOrderDialog = false"
    ></reported-work-order-dialog>
  </div>
</template>

<script>
const ORDER_TYPE = {
  GENERAL: { value: "GENERAL" },
  INSPECTION: { value: "INSPECTION" },
  EMERGENCY: { value: "EMERGENCY" },
  PREVENTATIVE_MAINTENANCE: { value: "PREVENTATIVE_MAINTENANCE" },
  CORRECTIVE_MAINTENANCE: { value: "CORRECTIVE_MAINTENANCE" },
  DISPOSAL: { value: "DISPOSAL" },
  REFURBISHMENT: { value: "REFURBISHMENT" },
};
module.exports = {
  props: {
    resources: Object,
  },
  components: {
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "reported-work-order-dialog": httpVueLoader(
      "/components/work-order/src/reported-work-order-dialog.vue"
    ),
  },
  data() {
    return {
      isDoashBoard: false,
      counter: {
        equipmentCode: "",
      },
      counterFiles: [],
      showWorkOrderDialog: false,
    };
  },
  computed: {
    lastWorkOrderTypeTitle() {
      switch (this.counter.lastWorkOrder?.orderType) {
        case ORDER_TYPE.GENERAL.value:
          return this.resources["general_work_order"];
        case ORDER_TYPE.INSPECTION.value:
          return this.resources["inspection_work_order"];
        case ORDER_TYPE.EMERGENCY.value:
          return this.resources["emergency_work_order"];
        case ORDER_TYPE.PREVENTATIVE_MAINTENANCE.value:
          return this.resources["pm_work_order"];
        case ORDER_TYPE.CORRECTIVE_MAINTENANCE.value:
          return this.resources["cm_work_order"];
        case ORDER_TYPE.REFURBISHMENT.value:
          return this.resources["refurbishment_work_order"];
        case ORDER_TYPE.DISPOSAL.value:
          return this.resources["disposal_work_order"];

        default:
          return "";
      }
    },
    workOrderHistoryText() {
      return (
        this.machine?.workOrderHistory +
        " " +
        (this.machine?.workOrderHistory > 1
          ? this.resources["workorders"]
          : this.resources["workorder"])
      );
    },
  },
  methods: {
    formatWorkOrderDate(timestamp) {
      if (timestamp) {
        return moment.unix(timestamp).format("YYYY-MM-DD");
      }
      return "";
    },
    goToWorkOrderDetailPage() {
      const query = Common.param({
        orderType: "",
        page: 1,
        isHistory: true,
        size: 20,
        isAllCompany: true,
        status: "",
        assetIds: this.counter.id,
      });
      location.href = Common.URL.WORK_ORDER + "?" + query;
    },
    async showDetails(counter, isDoashBoard) {
      try {
        const { data: counterData } = await axios.get(
          `/api/counters/${counter.id}`
        );
        this.counter = counterData;
        if (this.counter.lastWorkOrderId) {
          const id = this.counter.lastWorkOrderId;
          const workOrder = await this.fetchWorkOrderDetail(id);
          this.counter.lastWorkOrder = workOrder;
        }
        console.log("counter: ", counter);
        this.isDoashBoard = isDoashBoard;
        this.getFile();
      } catch (error) {
        console.log("counter-detail:::showDetails:::error", error);
      }
    },
    async fetchWorkOrderDetail(workOrderId) {
      try {
        const response = await axios.get(`/api/work-order/${workOrderId}`);
        return response.data.data;
      } catch (error) {
        console.log(error);
      }
    },

    async showDetailsById(id) {
      try {
        const response = await axios.get("/api/counters/" + id);
        this.counter = response.data;
        this.getFile();
      } catch (error) {
        console.log(error);
      }
    },
    getFile: function () {
      var param = {
        storageType: "MOLD_COUNTER",
        refId2: this.counter.id,
      };
      axios.get("/api/file-storage?" + Common.param(param)).then((response) => {
        console.log("response: ", response);
        this.counterFiles = response.data;
        $("#op-counter-details").modal("show");
      });
    },

    getPic(file) {
      return "http://49.247.200.147" + file.saveLocation;
    },

    dimissModal: function () {
      if (this.isDoashBoard) {
        $("#op-counter-details").modal("hide");
        return (location.href = Common.PAGE_URL.SENSOR);
      }
      $("#op-counter-details").modal("hide");
    },
  },
  mounted() {},
};
</script>
<style scoped>
.titleCase {
  text-transform: lowercase;
}
.titleCase::first-letter {
  text-transform: uppercase;
}
</style>
