<template>
  <div class="step-content">
    <div class="form-group row line-row-wrapper">
      <span class="col-md-2 col-form-label">{{
        resources["work_order_id"]
      }}</span>
      <div class="col-md-9 d-flex align-items-center">
        <base-input
          :value="workOrderId"
          :readonly="true"
          size="small"
        ></base-input>
      </div>
    </div>
    <div class="form-group row line-row-wrapper">
      <span class="col-md-2 col-form-label">{{ resources["asset_s"] }}</span>
      <div class="col-md-9 d-flex align-items-center">
        <div>
          <div>
            <base-chip
              v-for="(item, index) in getSelectedMold"
              :key="item?.id || index"
              :close-able="false"
              :selection="item"
              @close="onRemoveMold"
            >
            </base-chip>
            <base-chip
              v-for="(item, index) in getSelectedMachine"
              :key="item?.id || index"
              :close-able="false"
              :color="'red'"
              :selection="item"
              @close="onRemoveMachine"
            >
            </base-chip>
            <base-chip
              v-for="(item, index) in getSelectedTerminal"
              :key="item?.id || index"
              :close-able="false"
              :color="'teal-deer'"
              :selection="item"
              @close="onRemoveTerminal"
            >
            </base-chip>
            <base-chip
              v-for="(item, index) in getSelectedSensor"
              :key="item?.id || index"
              :close-able="false"
              :color="'atomic-tangerine'"
              :selection="item"
              @close="onRemoveSensor"
            >
            </base-chip>
          </div>
        </div>
      </div>
    </div>
    <div
      v-show="orderType !== 'CORRECTIVE_MAINTENANCE'"
      class="form-group row line-row-wrapper"
    >
      <span class="col-md-2 col-form-label"> {{ resources["plant_s"] }}</span>
      <div class="col-md-9 align-items-center d-flex">
        <i v-if="!hasPlantLocationCode"
          >{{ resources["associated_plant_unavailable"] }}.</i
        >
        <div v-else>
          <span>{{
            _.join(
              checkedItemsList
                .map((item) => item.locationName)
                .filter((item) => !!item),
              ", "
            )
          }}</span>
        </div>
      </div>
    </div>
    <div
      v-show="
        ['CORRECTIVE_MAINTENANCE', 'REFURBISHMENT', 'DISPOSAL'].includes(
          orderType
        )
      "
      class="form-group row line-row-wrapper"
    >
      <span class="col-md-2 col-form-label">
        {{ resources["shots_at_request_time"] }}</span
      >
      <div class="col-md-9 d-flex align-items-center">
        <base-input
          :value="`${reportFailureShot ? formatNumber(reportFailureShot) : 0} ${
            reportFailureShot > 0 ? 'shots' : 'shot'
          }`"
          :readonly="true"
          size="small"
        ></base-input>
      </div>
    </div>
    <div
      v-show="orderType === 'CORRECTIVE_MAINTENANCE'"
      class="align-center form-group row line-row-wrapper"
    >
      <span class="col-md-2 col-form-label">{{
        resources["failure_time"]
      }}</span>
      <div class="col-md-9 d-flex align-items-center">
        <base-button disabled>{{ getDateTitle }}</base-button>
        <base-button disabled style="margin-left: 8px">
          {{ getFailureTime }}
        </base-button>
      </div>
    </div>
    <div class="form-group row line-row-wrapper align-top">
      <span
        class="col-md-2 col-form-label d-flex align-center"
        style="gap: 4px"
      >
        <span>{{ resources["details"] }}</span>
        <div class="dot--red"></div>
      </span>
      <div class="col-md-9 d-flex align-items-center">
        <textarea
          class="custom-textarea"
          rows="7"
          placeholder="Enter any notes here"
          style="resize: none; width: 100%; padding: 4px 8px"
          v-model="item.details"
        ></textarea>
      </div>
    </div>
    <div class="align-center form-group row line-row-wrapper">
      <span class="col-md-2 col-form-label">{{
        resources["attachment_s"]
      }}</span>
      <div class="col-md-9 d-flex align-items-center op-upload-button-wrap">
        <div>
          <div style="marin-bottom: 8px">
            <base-button level="secondary">
              {{ resources["upload_file_image"] }}
              <img class="ml-1" src="/images/icon/upload-blue.svg" alt="" />
            </base-button>
            <input
              type="file"
              ref="fileupload2"
              id="files2"
              @change="selectedThirdFiles2"
              style="height: 40px; width: 100%"
              accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
            />
          </div>
          <preview-images-system
            :is-viewing="false"
            @delete-img="handleDeleteThirdFiles2"
            :images="thirdFiles2"
            :images-uploaded="partPictureFiles2"
          ></preview-images-system>
        </div>
      </div>
    </div>
    <div
      v-show="orderType === 'CORRECTIVE_MAINTENANCE'"
      class="align-center form-group row line-row-wrapper"
    >
      <span class="col-md-2 col-form-label d-flex align-center"
        ><span>{{ resources["number_of_backups"] }}</span>
        <a-tooltip placement="bottomLeft">
          <template slot="title">
            <span style="display: inline-block; padding: 6px 8px">{{
              resources["the_number_of_backup_assets_currently_available"]
            }}</span>
          </template>
          <a-icon type="info-circle" />
        </a-tooltip>
      </span>
      <div class="col-md-9 d-flex align-items-center">
        <base-input
          v-model="item.numberOfBackups"
          size="small"
          placeholder="Number of Backups"
        ></base-input>
      </div>
    </div>
    <div class="align-center form-group row line-row-wrapper">
      <span class="col-md-2 col-form-label">{{
        resources["cost_estimate_usd"]
      }}</span>
      <div class="col-md-3 d-flex align-items-center">
        <base-input
          v-model="item.costEstimate"
          size="small"
          placeholder="Cost Estimate (USD)"
        ></base-input>
      </div>
      <span class="col-md-1 col-form-label">{{ resources["details"] }}</span>
      <div class="col-md-5 d-flex align-items-center">
        <base-input
          v-model="item.costDetails"
          size="small"
          style="width: 100%"
          placeholder="Details"
        ></base-input>
      </div>
    </div>
    <div class="align-top form-group row line-row-wrapper">
      <span class="col-md-2 col-form-label">{{
        resources["cost_file_upload"]
      }}</span>
      <div class="col-md-9 op-upload-button-wrap">
        <div>
          <div style="marin-bottom: 8px">
            <base-button level="secondary">
              {{ resources["document_upload"] }}
              <img class="ml-1" src="/images/icon/upload-blue.svg" alt="" />
            </base-button>
            <input
              type="file"
              ref="fileupload"
              id="files1"
              @change="selectedThirdFiles"
              style="height: 40px; width: 100%"
              accept=".doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
            />
          </div>
        </div>
        <div class="d-flex" style="gap: 4px">
          <base-chip
            v-for="(item, index) in getSelectedDocument"
            :key="item.id"
            :selection="item"
            :close-able="true"
            @close="(file) => handleDeleteDocument(file, index)"
          ></base-chip>
          <base-button
            v-if="thirdFiles.length > 0"
            icon="popup"
            @click="handleDownload"
            >{{ resources["review_document"] }}</base-button
          >
        </div>
      </div>
    </div>
    <date-picker-modal
      ref="datePickerModalRef"
      :select-option="timeOptions"
      @change-date="handleChangeDate"
    >
    </date-picker-modal>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    isShow: Boolean,
    workOrderId: {
      type: [String, Number],
    },
    listMoldIds: Array,
    listMachineIds: Array,
    listTerminalIds: Array,
    listSensorIds: Array,
    checkedItemsList: Array,
    defaultFailureTime: {
      type: Object,
      default: () => ({
        date: {
          from: new Date(),
          to: new Date(),
          fromTitle: moment().format("YYYY-MM-DD"),
          toTitle: moment().format("YYYY-MM-DD"),
        },
        time: {
          hour: "0",
          minute: "0",
          isAddDay: false,
        },
      }),
    },
    getSelectedMold: Array,
    getSelectedMachine: Array,
    getSelectedTerminal: Array,
    getSelectedSensor: Array,
    reportFailureShot: Number,
    item: {
      type: Object,
      default: () => ({
        details: "",
        costEstimate: "",
        numberOfBackups: "",
        costDetails: "",
        failureTime: "",
      }),
    },
    orderType: String,
    thirdFiles: Array,
    thirdFiles2: Array,
    partPictureFiles: Array,
    partPictureFiles2: Array,
    deleteThirdFiles: Function,
    selectedThirdFiles: Function,
    deleteThirdFiles2: Function,
    selectedThirdFiles2: Function,
    hasPlantLocationCode: Boolean,
  },
  components: {
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  data() {
    return {
      moldIds: [],
      machineIds: [],
      terminalIds: [],
      sensorIds: [],
      isAssetView: false,
      visibleAsset: false,
      selectedHour: {
        hour: "0",
        minute: "0",
        isAddDay: false,
      },
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      frequency: "DAILY",
      timeOptions: [{ title: "Daily", type: "DAILY", isRange: false }],
      thirdFiles2Key: 0,
      thirdFilesKey: 0,
    };
  },
  computed: {
    getDateTitle() {
      const displayDate = this.defaultFailureTime.date.to;
      return moment(displayDate).format("YYYY-MM-DD");
    },
    getFailureTime() {
      let hh = this.defaultFailureTime.time.hour;
      let mm = this.defaultFailureTime.time.minute;
      if (this.defaultFailureTime.time.hour < 9) {
        hh = `0${this.defaultFailureTime.time.hour}`;
        if (hh.length > 2) {
          hh = hh.slice(1);
        }
      }
      if (this.defaultFailureTime.time.minute < 9) {
        mm = `0${this.defaultFailureTime.time.minute}`;
        if (mm.length > 2) {
          mm = mm.slice(1);
        }
      }
      return `${hh}:${mm}`;
    },
    getSelectedDocument() {
      return _.map(this.thirdFiles, (o) => {
        return {
          title: o.name,
          id: o.id,
        };
      });
    },
  },
  watch: {
    isShow: async function (newVal) {
      if (newVal) {
        this.moldIds = JSON.parse(JSON.stringify(this.listMoldIds));
        this.machineIds = JSON.parse(JSON.stringify(this.listMachineIds));
        this.terminalIds = JSON.parse(JSON.stringify(this.listTerminalIds));
        this.sensorIds = JSON.parse(JSON.stringify(this.listSensorIds));
      } else {
      }
    },
    listMoldIds(newVal) {
      this.moldIds = JSON.parse(JSON.stringify(newVal));
    },
    listMachineIds(newVal) {
      this.machineIds = JSON.parse(JSON.stringify(newVal));
    },
    listTerminalIds(newVal) {
      this.terminalIds = JSON.parse(JSON.stringify(newVal));
    },
    listSensorIds(newVal) {
      this.sensorIds = JSON.parse(JSON.stringify(newVal));
    },
  },
  methods: {
    handleDownload() {
      console.log("handleDownload", this.thirdFiles);
      this.thirdFiles.forEach(async (element) => {
        await Common.downloadFile(element.saveLocation, element.name);
      });
    },
    clearData() {
      this.selectedHour = {
        hour: "0",
        minute: "0",
        isAddDay: false,
      };
      this.item = {
        details: "",
        costEstimate: "",
        numberOfBackups: "",
        costDetails: "",
        failureTime: "",
      };
      this.thirdFiles = [];
      this.thirdFiles2 = [];
      this.partPictureFiles = [];
      this.partPictureFiles2 = [];
    },
    handleDeleteThirdFiles2(index) {
      this.deleteThirdFiles2(index);
      console.log("this.$refs.fileupload2", this.$refs.fileupload2.value);
      this.thirdFiles2Key++;
    },
    handleDeleteThirdFiles(index) {
      this.deleteThirdFiles(index);
      console.log("this.$refs.fileupload", this.$refs.fileupload.value);
      this.thirdFilesKey++;
    },
    handleChangeHour(value, type) {
      this.selectedHour[type] = value;
      console.log("handleChangeHour", value, type);
    },
    handleSubmitTime() {
      console.log("handleSubmitTime");
    },
    handleOpenPickerModal() {
      const datePickerModalRef = this.$refs.datePickerModalRef;
      const dateValue = { ...this.currentDate };
      datePickerModalRef.showDatePicker(this.frequency, dateValue);
    },
    handleChangeDate(timeValue, frequency) {
      const datePickerModalRef = this.$refs.datePickerModalRef;
      this.currentDate = timeValue;
      this.frequency = frequency;
      datePickerModalRef.closeDatePicker();
    },
    openAssetDropdown() {
      this.isAssetView = false;
      this.visibleAsset = true;
    },
    onRemoveMold(asset) {
      this.moldIds = [...this.moldIds].map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    onRemoveMachine(asset) {
      this.machineIds = [...this.machineIds].map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    onRemoveTerminal(asset) {
      this.terminalIds = [...this.terminalIds].map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    onRemoveSensor(asset) {
      this.sensorIds = [...this.sensorIds].map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    handleDeleteDocument(file, index) {
      this.handleDeleteThirdFiles(index);
    },
  },
};
</script>
