<template>
  <base-dialog
    :title="getModalTitle"
    :visible="isShow"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    @close="handleClose"
  >
    <div class="modal__container" :key="loadKey">
      <div class="modal__container__form">
        <div class="form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label">{{
            resources["work_order_id"]
          }}</span>
          <div class="col-md-9 d-flex align-items-center">
            <base-input
              :value="item.workOrderId"
              :readonly="true"
              size="small"
            ></base-input>
          </div>
        </div>
        <div class="form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label">{{
            resources["asset_s"]
          }}</span>
          <div class="col-md-9 align-items-center">
            <div>
              <base-chip
                v-for="(item, index) in getWorkOrderAssets"
                :key="index"
                :selection="item"
                :color="item.color"
                :close-able="false"
              >
              </base-chip>
            </div>
          </div>
        </div>
        <div
          v-show="
            ['CORRECTIVE_MAINTENANCE', 'REFURBISHMENT', 'DISPOSAL'].includes(
              item.orderType
            )
          "
          class="form-group row line-row-wrapper"
        >
          <span class="col-md-2 col-form-label">
            {{ resources["shots_at_request_time"] }}
          </span>
          <div class="col-md-9 d-flex align-items-center">
            <base-input
              :value="`${
                item.reportFailureShot
                  ? formatNumber(item.reportFailureShot)
                  : 0
              } ${item.reportFailureShot > 0 ? 'shots' : 'shot'}`"
              :readonly="true"
              size="small"
            ></base-input>
          </div>
        </div>
        <div class="form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label">{{
            resources["plant_s"]
          }}</span>
          <div class="col-md-9 align-items-center">
            <div>
              {{
                _.join(
                  _.map(item.plantList, (plant) => plant.name),
                  ", "
                )
              }}
            </div>
          </div>
        </div>
        <div
          v-show="item.orderType === 'CORRECTIVE_MAINTENANCE'"
          class="align-center form-group row line-row-wrapper"
        >
          <span class="col-md-2 col-form-label">{{
            resources["failure_time"]
          }}</span>
          <div class="col-md-9 d-flex align-items-center">
            <div>
              <base-button level="secondary" disabled>
                {{ getDateTitle.date }}
              </base-button>
            </div>
            <div style="margin-left: 8px">
              <base-button level="secondary" disabled>
                {{ getDateTitle.time }}
              </base-button>
            </div>
          </div>
        </div>
        <div class="form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label">{{
            resources["priority"]
          }}</span>
          <div class="col-md-9 d-flex align-items-center">
            <base-priority-card :color="color[item.priority]">
              {{ item.priority }}
            </base-priority-card>
          </div>
        </div>
        <div class="form-group row line-row-wrapper align-top">
          <span class="col-md-2 col-form-label">{{
            resources["details"]
          }}</span>
          <div class="col-md-9 d-flex align-items-center">
            <textarea
              class="custom-textarea text-area--disable"
              rows="7"
              style="resize: none"
              :readonly="true"
              disabled
              placeholder="Enter any notes here"
              v-model="item.details"
            ></textarea>
          </div>
        </div>
        <div class="align-center form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label d-flex align-center"
            ><span>{{ resources["attachment_s"] }} </span></span
          >
          <div class="col-md-9 d-flex align-items-center">
            <preview-images-system
              :is-viewing="true"
              :images-uploaded="thirdFiles"
            ></preview-images-system>
          </div>
        </div>
        <div
          v-show="item.orderType === 'CORRECTIVE_MAINTENANCE'"
          class="align-center form-group row line-row-wrapper"
        >
          <span class="col-md-2 col-form-label d-flex align-center"
            ><span>{{ resources["number_of_backups"] }} </span>
            <a-icon type="info-circle" />
          </span>
          <div class="col-md-9 d-flex align-items-center">
            <base-input
              v-model="item.numberOfBackups"
              :readonly="true"
              size="small"
              placeholder="Number of Backups"
            >
            </base-input>
          </div>
        </div>
        <div class="align-center form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label">{{
            resources["cost_estimate_usd"]
          }}</span>
          <div class="col-md-3 d-flex align-items-center">
            <base-input
              v-model="getCostEstimate.cost"
              :readonly="true"
              size="small"
              placeholder="Cost Estimate (USD)"
            >
            </base-input>
          </div>
          <span class="col-md-1 col-form-label">{{
            resources["details"]
          }}</span>
          <div class="col-md-5 d-flex align-items-center">
            <base-input
              v-model="getCostEstimate.details"
              :readonly="true"
              size="small"
              style="width: 100%"
              placeholder="Details"
            ></base-input>
          </div>
        </div>
        <div class="align-center form-group row line-row-wrapper">
          <span class="col-md-2 col-form-label d-flex align-center"
            ><span>{{ resources["const_file_s"] }} </span></span
          >
          <div class="col-md-9 d-flex align-items-center">
            <template v-if="costFiles.length">
              <base-chip
                v-for="file in costFiles"
                :key="file.id"
                color="atomic-tangerine"
                :selection="{ id: file.id, title: file.fileName }"
                :close-able="false"
              ></base-chip>
              <a
                href="javascript:void(0)"
                class="btn-custom customize-btn btn-outline-custom-primary position-relative"
                @click="handleDownload"
              >
                <div class="icon-download--small"></div>
              </a>
            </template>
            <template v-else> {{ resources["none"] }} </template>
          </div>
        </div>
      </div>
      <div
        v-if="item.status === 'APPROVAL_REQUESTED'"
        class="d-flex modal__footer justify-end"
      >
        <base-button
          level="secondary"
          type="reject"
          @click="handleShowReasonForm"
          >{{ resources["decline"] }}</base-button
        >
        <base-button
          level="primary"
          type="save"
          style="margin-left: 8px"
          @click="handleApproved"
          >{{ resources["approve"] }}</base-button
        >
      </div>
    </div>
    <reason-dialog
      :visible="visibleReasonForm"
      :reason-type="reasonType"
      :resources="resources"
      :item="item"
      @close="closeReasonForm"
      @decline-approval="handleDecline"
    ></reason-dialog>
  </base-dialog>
</template>

<script>
const defaultOptions = [{ title: "Daily", type: "DAILY", isRange: false }];
const COLOR_TYPE = {
  TOOLING: "lavender",
  MACHINE: "melon",
  TERMINAL: "teal-deer",
  COUNTER: "atomic-tangerine",
};
module.exports = {
  props: {
    resources: Object,
    isShow: {
      type: Boolean,
    },
    workOrderId: {
      type: [String, Number],
    },
  },
  components: {
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "reason-dialog": httpVueLoader(
      "/components/work-order/src/reason-dialog.vue"
    ),
  },
  data() {
    return {
      selectedHour: {
        hour: "0",
        minute: "0",
        isAddDay: false,
      },
      assetsOption: [],
      selectedAssets: [],
      item: {
        orderType: "CORRECTIVE_MAINTENANCE",
        details: "",
        costEstimate: "",
        numberOfBackups: "",
        costDetails: "",
        failureTime: "",
        plants: [],
        priority: "",
        workOrderId: "",
        workOrderAssets: [],
        status: "",
      },
      frequency: "DAILY",
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      isVisibleMoldDropdown: false,
      isVisibleMachineDropdown: false,
      visibleAsset: false,
      moldIds: [],
      machineIds: [],
      isAssetView: false,
      thirdFiles: [],
      loadKey: 0,
      costFiles: [],
      visibleReasonForm: false,
      reasonType: "WO_CRT_APPR_REQUESTED",
    };
  },
  created() {
    this.timeOptions = defaultOptions;
    this.color = {
      HIGH: "red",
      MEDIUM: "yellow",
      LOW: "green",
      UPCOMING: "yellow",
      REQUESTED: "purple",
      OVERDUE: "red",
      Overdue: "red",
      Declined: "grey",
      DECLINED: "grey",
      Cancelled: "dark-grey",
      CANCELLED: "dark-grey",
      PENDING_APPROVAL: "blue",
    };
    console.log("@created:cm-approval-dialog.vue");
  },
  computed: {
    getModalTitle() {
      const orderTypeTitle = this.item.orderType.toLowerCase();
      return `${this.resources["approval_request_for"]} ${this.resources[orderTypeTitle]} ${this.resources["workorder"]}`;
    },
    getDateTitle() {
      const failureTime = this.item.failureTime;
      // console.log('getDateTitle', failureTime)
      if (failureTime) {
        const convertedDate = Common.convertTimestampToDate(failureTime);
        return {
          date: moment(convertedDate).format("YYYY-MM-DD"),
          time: moment(convertedDate).format("hh:mm"),
        };
      }
      return "";
    },
    getWorkOrderAssets() {
      if (this.item.workOrderAssets.length == 0) {
        return [];
      }
      return _.map(this.item.workOrderAssets, (o) => {
        return {
          id: o.id,
          title: o.assetCode,
          color: COLOR_TYPE[o.type],
        };
      });
    },
    getCostEstimate() {
      if (
        this.item &&
        this.item.workOrderCostList &&
        this.item.workOrderCostList[0]
      ) {
        return {
          cost: this.item.workOrderCostList[0].cost,
          details: this.item.workOrderCostList[0].details,
        };
      }
      return {
        cost: "",
        details: "",
      };
    },
  },
  watch: {
    isShow: async function (newVal) {
      if (newVal) {
        console.log("visible");
        await this.fetchWorkOrder();
        await this.fetchCostFiles();
        this.fetchWorkOrderFile();
      }
    },
  },
  methods: {
    handleShowReasonForm() {
      this.visibleReasonForm = true;
    },
    closeReasonForm() {
      this.visibleReasonForm = false;
    },
    handleDownload() {
      this.costFiles.forEach(async (element) => {
        await Common.downloadFile(element.saveLocation, element.fileName);
      });
    },
    async downloadFile(url) {
      const response = await fetch(url);
      const blob = await response.blob();

      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = url.substring(url.lastIndexOf("/") + 1);
      a.style.display = "none";
      document.body.appendChild(a);

      a.click();

      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    },
    handleClose() {
      this.$emit("close");
      this.selectedAssets = [];
      this.visibleReasonForm = false;
      this.selectedHour = {
        hour: "0",
        minute: "0",
        isAddDay: false,
      };
    },
    async fetchCostFiles() {
      try {
        const response = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_COST_FILE&refId=${this.workOrderId}`
        );
        const costFile = response.data.map((file) => ({
          ...file,
          name: file.fileName,
        }));
        this.costFiles = costFile;
      } catch (error) {
        console.log(error);
      }
    },
    async fetchWorkOrder() {
      const id = this.workOrderId;
      try {
        const res = await axios.get(`/api/work-order/${id}`);
        this.item = res.data.data;
        console.log("fetchWorkOrder", this.item, res);
        this.loadKey++;
      } catch (error) {
        console.log(error);
      }
    },
    async fetchWorkOrderFile() {
      const id = this.workOrderId;
      try {
        const res = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_FILE&refId=${id}`
        );
        this.thirdFiles = res.data;
        console.log("fetchWorkOrderFile", this.thirdFiles, res);
        this.loadKey++;
      } catch (error) {
        console.log(error);
      }
    },
    async handleDecline(reason) {
      await this.changeStatus(false, reason);
      this.handleClose();
    },
    async handleApproved() {
      await this.changeStatus(true);
      this.handleClose();
    },
    async changeStatus(isApproved, reason) {
      try {
        const id = this.workOrderId;
        const payload = {};
        if (!isApproved) {
          payload.cancelledReason = reason;
        }

        const res = await axios.post(
          `/api/work-order/approve-request-approval/${id}?approved=${isApproved}`,
          payload
        );
        console.log(res);
      } catch (error) {
        console.log(error);
      }
    },
  },
  mounted() {
    if (this.$root?.$el?.id === "headersId") {
      // prevent component being nested in another component differ from header
      this.$root.$data.isCmWorkOrderModalReady = true;
    }
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

.form-group {
  margin-bottom: 0.5rem;
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

.custom-textarea {
  resize: none;
  width: 100%;
  border: 1px solid #e3e3e3;
  border-radius: 3px;
  outline: none;
  padding: 4px 8px;
}
</style>
