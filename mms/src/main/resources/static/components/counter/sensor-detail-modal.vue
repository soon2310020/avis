<template>
  <div v-show="isModalOpened">
    <emdn-modal
      v-if="isModalOpened && !showWorkOrderDialog"
      :is-opened="isModalOpened && !showWorkOrderDialog"
      :heading-text="resources['counter_id'] + ': ' + counter.equipmentCode"
      :modal-handler="dimissModal"
    >
      <template #body>
        <div class="d-flex justify-space-between">
          <div style="margin-right: 19px">
            <base-images-slider
              :images="counter.attachment"
              :key="viewImageKey"
              :clear-key="clearImageKey"
              :handle-submit-file="handleSubmitImage"
            ></base-images-slider>
          </div>
          <div
            style="width: 520px; height: 555px; padding: 0 10px; overflow: auto"
          >
            <div style="">
              <div class="card">
                <div
                  class="card-header"
                  @click="expand(`sensor-detail-information`)"
                >
                  <i class="fa fa-align-justify"></i>
                  <span v-text="resources['counter']"></span>
                  <img
                    id="sensor-detail-information-image"
                    style="position: absolute; right: 20px; top: 1rem"
                    src="/images/icon/angle-arrow-down.svg"
                    width="8"
                    height="8"
                  />
                </div>

                <table
                  id="sensor-detail-information"
                  class="table table-mold-details"
                  style="display: table"
                >
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
                <div
                  class="card-header"
                  @click="expand(`sensor-detail-mold-information`)"
                >
                  <i class="fa fa-align-justify"></i>
                  <span v-text="resources['installation_info']"></span>
                  <img
                    id="sensor-detail-mold-information-image"
                    style="position: absolute; right: 20px; top: 1rem"
                    src="/images/icon/angle-arrow-down.svg"
                    width="8"
                    height="8"
                  />
                </div>

                <table
                  id="sensor-detail-mold-information"
                  class="table table-mold-details"
                  style="display: none"
                >
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
                        <!-- <preview-images-system :images-uploaded="counterFiles"></preview-images-system> -->
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
                <div
                  class="card-header"
                  @click="expand(`sensor-detail-subscription-information`)"
                >
                  <i class="fa fa-align-justify"></i>
                  <span v-text="resources['subscription_information']"></span>
                  <img
                    id="sensor-detail-subscription-information-image"
                    style="position: absolute; right: 20px; top: 1rem"
                    src="/images/icon/angle-arrow-down.svg"
                    width="8"
                    height="8"
                  />
                </div>

                <table
                  id="sensor-detail-subscription-information"
                  class="table table-mold-details"
                  style="display: none"
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
                <div
                  class="card-header"
                  @click="expand(`sensor-detail-work-order-information`)"
                >
                  <i class="fa fa-align-justify"></i>
                  <span v-text="resources['work_order_information']"></span>
                  <img
                    id="sensor-detail-work-order-information-image"
                    style="position: absolute; right: 20px; top: 1rem"
                    src="/images/icon/angle-arrow-down.svg"
                    width="8"
                    height="8"
                  />
                </div>

                <table
                  id="sensor-detail-work-order-information"
                  class="table table-mold-details"
                  style="display: none"
                >
                  <tbody>
                    <tr>
                      <th v-text="resources['last_work_order_date']"></th>
                      <td>
                        {{ formatToDate(counter?.lastWorkOrder?.completedOn) }}
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
                          v-show="counter.workOrderHistory > 0"
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
          </div>
        </div>
      </template>
    </emdn-modal>

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
    "reported-work-order-dialog": httpVueLoader(
      "/components/work-order/src/reported-work-order-dialog.vue"
    ),
  },
  data() {
    return {
      isModalOpened: false,
      isDoashBoard: false,
      counter: {
        equipmentCode: "",
        attachment: [],
      },
      counterFiles: [],
      showWorkOrderDialog: false,
      viewImageKey: 0,
      clearImageKey: 0,
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
        this.counter.workOrderHistory +
        " " +
        (this.counter.workOrderHistory > 1
          ? this.resources["workorders"]
          : this.resources["workorder"])
      );
    },
  },
  methods: {
    expand(elementId) {
      var content = document.getElementById(elementId);
      var contentImage = document.getElementById(`${elementId}-image`);
      // basic info and part is default
      if (content) {
        if (content.style.display === "table") {
          content.style.display = "none";
          contentImage.src = "/images/icon/angle-arrow-down.svg";
        } else {
          content.style.display = "table";
          contentImage.src = "/images/icon/angle-arrow-up.svg";
        }
      }
    },
    async handleSubmitImage(file) {
      const refId = this.counter.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.counter.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "MOLD_COUNTER");
      formData.append("refId", `${refId}`);
      return formData;
    },
    async getCounterAttachment() {
      try {
        const type = {
          storageType: "MOLD_COUNTER",
          refId: this.counter.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.counter.attachment = res.data;
        this.viewImageKey++;
        console.log("getCounterAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
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
    async showDetails(counterId, isDoashBoard) {
      try {
        const response = await axios.get("/api/counters/" + counterId);
        this.counter = response.data;
        if (this.counter.lastWorkOrderId) {
          const id = this.counter.lastWorkOrderId;
          const workOrder = await this.fetchWorkOrderDetail(id);
          this.counter.lastWorkOrder = workOrder;
        }
        // console.log("counter: ", counter);
        this.isDoashBoard = isDoashBoard;
        this.getFile();
        await this.getCounterAttachment();
      } catch (error) {
        console.log("sensor-detail-modal:::showDetails:::error", error);
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
        this.showDetails(id);
      } catch (error) {
        console.log(error);
      }
    },
    async getFile() {
      var param = {
        storageType: "MOLD_COUNTER",
        refId2: this.counter.id,
      };
      try {
        const response = await axios.get(
          "/api/file-storage?" + Common.param(param)
        );
        console.log("response: ", response);
        this.counterFiles = response.data;
      } catch (error) {
        console.log(error);
      } finally {
        this.isModalOpened = true;
      }
    },

    getPic(file) {
      return "http://49.247.200.147" + file.saveLocation;
    },

    dimissModal: function () {
      this.isModalOpened = false;
      if (this.isDoashBoard) {
        // $("#op-counter-details").modal("hide");
        return (location.href = Common.PAGE_URL.TERMINAL);
      }
      this.clearImageKey++;
      // $("#op-counter-details").modal("hide");
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
.custom-plant-modal .modal-body-area {
  overflow-y: unset !important;
}
.emdn-modal-wrapper .modal-body-area {
  overflow-y: unset !important;
}
</style>
