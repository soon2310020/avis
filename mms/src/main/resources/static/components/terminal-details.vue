<template>
  <div
    id="op-terminal-details"
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
            <span class="span-title">Terminal Details</span>
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
                  :images="terminal.attachment"
                  :key="viewImageKey"
                  :clear-key="clearKey"
                  :handle-submit-file="handleSubmitImage"
                ></base-images-slider>
              </div>
              <div style="width: 520px">
                <div style="height: 600px; padding: 0 10px; overflow: auto">
                  <div class="card">
                    <div
                      class="card-header"
                      @click="expand(`terminal-installation-information`)"
                    >
                      <i class="fa fa-align-justify"></i>
                      <span v-text="resources['installation_info']"></span>
                      <img
                        id="terminal-installation-information-image"
                        style="position: absolute; right: 20px; top: 1rem"
                        src="/images/icon/angle-arrow-up.svg"
                        width="8"
                        height="8"
                      />
                    </div>

                    <table
                      id="terminal-installation-information"
                      class="table table-mold-details"
                      style="display: table"
                    >
                      <tbody>
                        <tr>
                          <th
                            v-text="resources['company_name'] + ' (Company ID)'"
                          ></th>
                          <td>
                            {{ terminal.companyName }} ({{
                              terminal.companyCode
                            }})
                          </td>
                        </tr>
                        <tr>
                          <th
                            v-text="
                              resources['location_name'] + ' (Location ID)'
                            "
                          ></th>
                          <td>
                            <div>
                              {{ terminal.locationName }} ({{
                                terminal.locationCode
                              }})
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <th v-text="resources['plant_area']"></th>
                          <td>
                            <span>{{ terminal.areaName }}</span>
                            <base-chip
                              :selection="selectionType[terminal.areaType]"
                              :allow-tooltips="false"
                              :color="'wheat'"
                              :close-able="false"
                            >
                            </base-chip>
                          </td>
                        </tr>
                        <tr>
                          <th v-text="resources['installation_date']"></th>
                          <td>{{ terminal.installedAt }}</td>
                        </tr>
                        <tr>
                          <th v-text="resources['activation_date']"></th>
                          <td>{{ formatDate(terminal.activatedAt) }}</td>
                        </tr>
                        <tr>
                          <th v-text="resources['installation_personnel']"></th>
                          <td>
                            <div v-if="terminal.installedBy">
                              <base-chip
                                :selection="{ title: terminal.installedBy }"
                                :allow-tooltips="false"
                                :color="'aquamarine'"
                                :close-able="false"
                              >
                              </base-chip>
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <th
                            v-text="resources['terminal_status']"
                            class="titleCase"
                          ></th>
                          <td class="titleCase">
                            <!-- {{ getStatusTitle() }} -->
                            <div
                              v-if="terminal.equipmentStatus == 'INSTALLED'"
                              class="d-flex align-center"
                            >
                              <div class="dot-status dot-status-type1"></div>
                              <span>Registered</span>
                            </div>
                            <div
                              v-if="terminal.equipmentStatus == 'AVAILABLE'"
                              class="d-flex align-center"
                            >
                              <div class="dot-status dot-status-type2"></div>
                              <span>Available</span>
                            </div>
                            <div
                              v-if="terminal.equipmentStatus == 'FAILURE'"
                              class="d-flex align-center"
                            >
                              <div class="dot-status dot-status-type3"></div>
                              <span>Failure</span>
                            </div>
                            <div
                              v-if="terminal.equipmentStatus == 'DISCARDED'"
                              class="d-flex align-center"
                            >
                              <div class="dot-status dot-status-type4"></div>
                              <span>Discarded</span>
                            </div>
                          </td>
                        </tr>
                        <!-- <tr>
                          <th v-text="resources['status']" class="titleCase"></th>
                          <td class="titleCase">
                            {{
                              terminal.equipmentStatus == "INSTALLED"
                                ? "REGISTERED"
                                : terminal.equipmentStatus
                            }}
                          </td>
                        </tr> -->
                        <!-- <tr>
                          <th v-text="resources['operation_status']"></th>
                          <td
                            v-if="terminal.operatingStatus === 'WORKING'"
                            v-text="resources['active']"
                          ></td>
                          <td
                            v-else-if="terminal.operatingStatus === 'NOT_WORKING'"
                            v-text="resources['inactive']"
                          ></td>
                          <td v-else>{{ terminal.operatingStatus }}</td>
                        </tr> -->
                        <tr>
                          <th v-text="resources['connection_status']"></th>
                          <td>
                            <span
                              v-if="terminal.connectionStatus == 'OFFLINE'"
                              style="background: #ff8489"
                              class="on-off-tag"
                              >Offline</span
                            >
                            <span
                              v-else-if="terminal.connectionStatus == 'ONLINE'"
                              style="background: #d2f8e2"
                              class="on-off-tag"
                              >Online</span
                            >
                          </td>
                        </tr>
                        <tr>
                          <th v-text="resources['memo']"></th>
                          <td>{{ terminal.memo }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="card">
                    <div
                      class="card-header"
                      @click="expand(`terminal-ip-address`)"
                    >
                      <i class="fa fa-align-justify"></i>
                      <span v-text="resources['ip_address']"></span>
                      <img
                        id="terminal-ip-address-image"
                        style="position: absolute; right: 20px; top: 1rem"
                        src="/images/icon/angle-arrow-down.svg"
                        width="8"
                        height="8"
                      />
                    </div>

                    <table
                      id="terminal-ip-address"
                      class="table table-mold-details"
                      style="display: none"
                    >
                      <tbody>
                        <tr>
                          <th v-text="resources['ip_type']"></th>
                          <td class="titleCase">{{ terminal.ipType }}</td>
                        </tr>
                        <template v-if="terminal.ipType === 'STATIC'">
                          <tr>
                            <th v-text="resources['ip_address']"></th>
                            <td>{{ terminal.ipAddress }}</td>
                          </tr>
                          <tr>
                            <th v-text="resources['subnet_mask']"></th>
                            <td>{{ terminal.subnetMask }}</td>
                          </tr>
                          <tr>
                            <th v-text="resources['gateway']"></th>
                            <td>{{ terminal.gateway }}</td>
                          </tr>
                          <tr>
                            <th v-text="resources['dns']"></th>
                            <td>{{ terminal.dns }}</td>
                          </tr>
                        </template>
                      </tbody>
                    </table>
                  </div>

                  <div class="card">
                    <div
                      class="card-header"
                      @click="expand(`terminal-work-order-information`)"
                    >
                      <i class="fa fa-align-justify"></i>
                      <span v-text="resources['work_order_information']"></span>
                      <img
                        id="terminal-work-order-information-image"
                        style="position: absolute; right: 20px; top: 1rem"
                        src="/images/icon/angle-arrow-down.svg"
                        width="8"
                        height="8"
                      />
                    </div>

                    <table
                      id="terminal-work-order-information"
                      class="table table-mold-details"
                      style="display: none"
                    >
                      <tbody>
                        <tr>
                          <th v-text="resources['last_work_order_date']"></th>
                          <td>
                            {{
                              formatDate(terminal?.lastWorkOrder?.completedOn)
                            }}
                            <span
                              class="custom-hyper-link"
                              style="margin-left: 4px"
                              @click="showWorkOrderDialog = true"
                              v-show="!!terminal?.lastWorkOrder"
                            >
                              {{
                                lastWorkOrderTypeTitle(
                                  terminal?.lastWorkOrder?.orderType
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
                              v-show="terminal.workOrderHistory > 0"
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
    <reported-work-order-dialog
      :resources="resources"
      :visible="showWorkOrderDialog"
      :selected="terminal?.lastWorkOrder"
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
const TYPES = {
  MAINTENANCE_AREA: {
    id: 1,
    title: "Maintenance",
  },
  PRODUCTION_AREA: {
    id: 2,
    title: "Production",
  },
  WAREHOUSE: {
    id: 3,
    title: "Warehouse",
  },
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
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  data() {
    return {
      isDashBoard: false,
      terminal: {
        equipmentCode: "",
      },
      terminalFiles: [],
      showWorkOrderDialog: false,
      viewImageKey: 0,
      clearKey: 0,
      selectionType: { ...TYPES },
    };
  },
  computed: {
    getStatusTitle() {
      return Common.formatter.toCase(
        this.terminal.equipmentStatus,
        "capitalize"
      );
    },
    workOrderHistoryText() {
      return (
        this.terminal.workOrderHistory +
        " " +
        (this.terminal.workOrderHistory > 1
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
    formatDate(timestamp) {
      if (timestamp) {
        return moment.unix(timestamp).format("YYYY-MM-DD");
      }
      return "";
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
    async handleSubmitImage(file) {
      const refId = this.terminal.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.terminal.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "TERMINAL");
      formData.append("refId", `${refId}`);
      return formData;
    },
    async getTerminalAttachment() {
      try {
        const type = {
          storageType: "TERMINAL",
          refId: this.terminal.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.terminal.attachment = res.data;
        this.viewImageKey++;
        console.log("getTerminalAttachment", res.data);
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
        assetIds: this.terminal.id,
      });
      location.href = Common.URL.WORK_ORDER + "?" + query;
    },
    async showDetails(terminal, isDashBoard) {
      const id = terminal.id;
      console.log(
        "🚀 ~ file: terminal-details.vue:542 ~ showDetails ~ id:",
        id
      );
      this.terminal = await this.fetchTerminalDetail(id);
      this.isDashBoard = isDashBoard;
      this.getFile();
      this.clearKey++;
      await this.getTerminalAttachment();
    },

    async showDetailsById(id) {
      try {
        const response = await axios.get("/api/terminals/" + id);
        this.showDetails(response.data);
      } catch (error) {
        console.log(error);
      }
    },
    async fetchTerminalDetail(id) {
      try {
        const response = await axios.get(`/api/terminals/${id}`);
        return response.data;
      } catch (error) {
        console.log(error);
      }
    },

    // getPic(file) {
    //   return "http://49.247.200.147" + file.saveLocation;
    // },

    getFile: function () {
      var param = {
        storageTypes: "TERMINAL",
        refId: this.terminal.id,
      };
      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          console.log(response.data, "TERMINAL");
          this.terminalFiles = response.data.TERMINAL;
          $("#op-terminal-details").modal("show");
        });
    },

    dimissModal: function () {
      if (this.isDashBoard) {
        $("#op-terminal-details").modal("hide");
        return (location.href = Common.PAGE_URL.TERMINAL);
      }
      $("#op-terminal-details").modal("hide");
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
