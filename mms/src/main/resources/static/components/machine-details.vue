<template>
  <div
    id="op-machine-details"
    class="modal fade"
    role="dialog"
    aria-labelledby="title-machine-detail"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-title">
          <div
            class="head-line"
            style="
              position: absolute;
              background: #52a1ff;
              height: 8px;
              border-radius: 6px 6px 0 0;
              top: 0;
              left: 0;
              width: 100%;
            "
          ></div>
          <div>
            <!-- <span class="span-title">{{ resources["machine_details"] }}</span> -->
            <span class="span-title">Machine Details</span>
          </div>
          <span
            class="close-button"
            style="
              font-size: 25px;
              display: flex;
              align-items: center;
              height: 17px;
              cursor: pointer;
            "
            data-dismiss="modal"
            aria-label="Close"
            @click="dimissModal"
          >
            <span class="t-icon-close"></span>
          </span>
        </div>
        <div style="padding: 20px 56px">
          <div>
            <div class="d-flex justify-space-between">
              <div style="margin-right: 19px">
                <base-images-slider
                  :images="machine.attachment"
                  :key="viewImageKey"
                  :clear-key="clearImageKey"
                  :handle-submit-file="handleSubmitImage"
                ></base-images-slider>
              </div>
              <div style="width: 520px">
                <div style="height: 600px; padding: 0 10px; overflow: auto">
                  <div class="card">
                    <div
                      v-on:click="expand(`machine-detail-information`)"
                      class="card-header"
                    >
                      <i class="fa fa-align-justify"></i>
                      <span v-text="resources['machine']"></span>
                      <img
                        id="machine-detail-information-image"
                        style="position: absolute; right: 20px; top: 1rem"
                        src="/images/icon/angle-arrow-up.svg"
                        width="8"
                        height="8"
                      />
                    </div>

                    <table
                      id="machine-detail-information"
                      class="table table-mold-details"
                      style="display: table"
                    >
                      <tbody>
                        <tr>
                          <th>{{ resources["machine_id"] }}</th>
                          <td>{{ machine.machineCode }}</td>
                        </tr>
                        <tr>
                          <th>{{ resources["tooling_id"] }}</th>
                          <td>
                            <div v-if="mold != null">
                              {{ mold.equipmentCode }}
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["Company_Name_Company_ID"] }}
                          </th>
                          <td>
                            {{
                              machine.companyName +
                              " (" +
                              machine.companyCode +
                              ")"
                            }}
                          </td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["Location_Name_Location_ID"] }}
                          </th>
                          <td>
                            {{
                              machine.locationName +
                              " (" +
                              machine.locationCode +
                              ")"
                            }}
                          </td>
                        </tr>
                        <tr>
                          <th>{{ resources["line"] }}</th>
                          <td>{{ machine.line }}</td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["machine_maker"] }}
                          </th>
                          <td>{{ machine.machineMaker }}</td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["machine_type"] }}
                          </th>
                          <td>{{ machine.machineType }}</td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["machine_model"] }}
                          </th>
                          <td>{{ machine.machineModel }}</td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["machine_ton"] }}
                          </th>
                          <td>
                            {{
                              machine.machineTonnage != null
                                ? machine.machineTonnage + " ton"
                                : ""
                            }}
                          </td>
                        </tr>
                        <tr>
                          <th>
                            {{ resources["machine_image"] }}
                          </th>
                          <td>
                            <preview-images-system
                              :images-uploaded="conditionFiles"
                            ></preview-images-system>
                          </td>
                        </tr>
                        <!--                <tr>-->
                        <!--                  <th v-text="resources['status']"></th>-->
                        <!--                  <td>{{machine.enabled ? "Enabled" : "Disabled"}}</td>-->
                        <!--                </tr>-->
                        <tr
                          v-for="(item, index) in listConfigCustomField"
                          :key="index"
                        >
                          <th>{{ item.fieldName }}</th>
                          <td>
                            {{ item.defaultInputValue }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div class="card">
                    <div
                      v-on:click="
                        expand(`machine-detail-work-order-information`)
                      "
                      class="card-header"
                    >
                      <i class="fa fa-align-justify"></i>
                      <span v-text="resources['work_order_information']"></span>
                      <img
                        id="machine-detail-work-order-information-image"
                        style="position: absolute; right: 20px; top: 1rem"
                        src="/images/icon/angle-arrow-down.svg"
                        width="8"
                        height="8"
                      />
                    </div>

                    <table
                      id="machine-detail-work-order-information"
                      style="display: none"
                      class="table table-mold-details"
                    >
                      <tbody>
                        <tr>
                          <th v-text="resources['last_work_order_date']"></th>
                          <td>
                            {{
                              formatWorkOrderDate(
                                machine?.lastWorkOrder?.completedOn
                              )
                            }}
                            <span
                              class="custom-hyper-link"
                              style="margin-left: 4px"
                              @click="showWorkOrderDialog = true"
                              v-show="!!machine?.lastWorkOrder"
                            >
                              {{
                                lastWorkOrderTypeTitle(
                                  machine.lastWorkOrder?.orderType
                                )
                              }}
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
                              v-show="machine.workOrderHistory > 0"
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
          </div>
        </div>
      </div>
    </div>
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
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  data() {
    return {
      isDoashBoard: false,
      machine: {
        machineCode: "",
        id: "",
        attachment: [],
      },
      mold: {},
      listConfigCustomField: null,
      conditionFiles: [],
      viewImageKey: 0,
      clearImageKey: 0,
    };
  },
  computed: {
    workOrderHistoryText() {
      return (
        this.machine.workOrderHistory +
        " " +
        (this.machine.workOrderHistory > 1
          ? this.resources["workorders"]
          : this.resources["workorder"])
      );
    },
  },
  methods: {
    lastWorkOrderTypeTitle(type) {
      switch (type) {
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
    formatWorkOrderDate(timestamp) {
      if (timestamp) {
        return moment.unix(timestamp).format("YYYY-MM-DD");
      }
      return "";
    },
    async handleSubmitImage(file) {
      console.log("handleSubmitImage", file);
      const refId = this.machine.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        // await this.getMachineAttachment()
        const last = [...res.data].pop();
        this.machine.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "MACHINE_PICTURE");
      formData.append("refId", `${refId}`);
      return formData;
    },
    async getMachineAttachment() {
      try {
        const type = {
          storageType: "MACHINE_PICTURE",
          refId: this.machine.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.machine.attachment = res.data;
        this.viewImageKey++;
        console.log("getMachineAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
    },
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
    goToWorkOrderDetailPage() {
      const query = Common.param({
        orderType: "",
        page: 1,
        isHistory: true,
        size: 20,
        isAllCompany: true,
        status: "",
        assetIds: this.machine.id,
      });
      location.href = Common.URL.WORK_ORDER + "?" + query;
    },
    instructionVideoFiles() {
      var param = {
        storageTypes: "MACHINE_PICTURE",
        refId: this.machine.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          this.conditionFiles = response.data["MACHINE_PICTURE"]
            ? response.data["MACHINE_PICTURE"]
            : [];
        });
    },
    getListConfigCustomField() {
      var self = this;
      const url = `/api/custom-field-value/list-by-object?objectType=MACHINE&objectId=${self.machine.id}`;
      axios
        .get(url)
        .then((response) => {
          self.listConfigCustomField = response.data.map((item) => ({
            propertyGroup: item.propertyGroup,
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        })
        .catch((error) => {
          console.log(error.response);
        })
        .finally(() => {
          console.log(self.listConfigCustomField, "listConfigCustomField");
        });
    },
    async fetchWorkOrderDetail(workOrderId) {
      try {
        const response = await axios.get(`/api/work-order/${workOrderId}`);
        return response.data.data;
      } catch (error) {
        console.log(error);
      } finally {
      }
    },
    showDetails: async function (machine, isDoashBoard, mold) {
      console.log(machine, "machine here");
      const { data: machineData } = await axios.get(
        "/api/machines/" + machine.id
      );
      this.machine = { ...machineData, ...machine };
      this.isDoashBoard = isDoashBoard;
      this.mold = mold;
      this.getListConfigCustomField();
      this.instructionVideoFiles();
      await this.getMachineAttachment();
      if (this.machine.lastWorkOrderId) {
        const id = this.machine.lastWorkOrderId;
        const workOrder = await this.fetchWorkOrderDetail(id);
        this.machine.lastWorkOrder = workOrder;
      }
      $("#op-machine-details").modal("show");
    },
    async showDetailsById(machineId) {
      try {
        const response = await axios.get("/api/machines/" + machineId);
        this.machine = response.data;
        this.mold = response.data.mold;
        this.isDoashBoard = false;
        this.getListConfigCustomField();
        await this.getMachineAttachment();
        $("#op-machine-details").modal("show");
      } catch (error) {
        console.log(error);
      }
    },

    // getPic(file) {
    //     return "http://49.247.200.147" + file.saveLocation;
    // },

    dimissModal: function () {
      if (this.isDoashBoard) {
        $("#op-machine-details").modal("hide");
        return (location.href = `${Common.PAGE_URL.MACHINE}`);
      }
      $("#op-machine-details").modal("hide");
      this.clearImageKey++;
    },
  },
  mounted() {},
};
</script>
<style scoped>
.modal-title {
  background: rgb(245, 248, 255);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 25.5px 11.5px 31px;
  border-radius: 6px 6px 0px 0px;
}

.modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}
</style>
