<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div class="position-relative" style="height: 600px">
      <div class="steps-zone">
        <div v-if="visible" class="steps-content">
          <div class="step-content">
            <div class="form-item align-top" style="margin-bottom: 40px">
              <div>
                <label style="width: 170px"
                  >{{ resources["asset"] }} <span class="badge-require"></span
                ></label>
              </div>
              <div>
                <assets-dropdown
                  :resources="resources"
                  :clear-key="clearAssetsKey"
                  :asset-types="assetTypes"
                  :mutil="true"
                  @change="handleChangeAssets"
                ></assets-dropdown>
              </div>
            </div>
            <div class="form-item align-top" style="margin-bottom: 40px">
              <label style="width: 170px"
                >{{ resources["work_order_type"] }}
                <span class="badge-require"></span
              ></label>
              <div class="grid" style="grid-template-columns: 1fr 1fr">
                <div
                  v-for="item in workOrderTypes"
                  :key="item.value"
                  style="width: 300px; margin-bottom: 20px"
                >
                  <div class="choosing-item" style="margin-right: 60px">
                    <p-check
                      :checked="item.checked"
                      @change="handleSelectWorkOrderType($event, item.value)"
                      :key="item.key"
                    >
                      <span style="font-size: 14.66px; line-height: 25px">
                        {{ resources[item.labelKey] }}
                      </span>
                    </p-check>
                  </div>
                </div>
              </div>
            </div>
            <div class="form-item" style="margin-bottom: 40px">
              <label style="width: 170px"
                >{{ resources["time_range"] }}
                <span class="badge-require"></span
              ></label>
              <div>
                <base-button
                  level="secondary"
                  icon="calendar"
                  @click="showDatePicker"
                >
                  {{ currentDateTitle }}
                </base-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div
        class="d-flex justify-end position-absolute"
        style="bottom: 0; right: 0"
      >
        <base-button level="secondary" type="cancel" @click="handleClose">
          {{ resources["cancel"] }}
        </base-button>
        <base-button
          level="primary"
          style="margin-left: 8px"
          :disabled="!checkEnable"
          @click="handleExport"
        >
          {{ resources["export"] }}
        </base-button>
      </div>
    </div>
    <date-picker-modal
      ref="datePickerModalRef"
      :select-option="timeOptions"
      :resources="resources"
      :date-range="timeRange"
      @change-date="handleChangeDate"
    >
    </date-picker-modal>
  </base-dialog>
</template>
<script>
const DEFAULT_TIME_OPTION = [
  { title: "Daily", type: "DAILY", isRange: false },
  { title: "Weekly", type: "WEEKLY", isRange: false },
  { title: "Monthly", type: "MONTHLY", isRange: false },
  { title: "Yearly", type: "YEARLY", isRange: false },
  { title: "Custom Range", type: "CUSTOM", isRange: true },
];

const DEFAULT_TIME_RANGE = {
  minDate: null,
  maxDate: new Date("2100-01-01"),
};

const DEFAULT_DATE = {
  from: new Date(),
  to: new Date(),
  fromTitle: moment().format("YYYY-MM-DD"),
  toTitle: moment().format("YYYY-MM-DD"),
};

const WORK_ORDER_TYPES = [
  {
    title: "General",
    labelKey: "general",
    value: "GENERAL",
    checked: false,
  },
  {
    title: "Corrective Maintenance",
    labelKey: "corrective_maintenance",
    value: "CORRECTIVE_MAINTENANCE",
    checked: false,
  },
  {
    title: "Inspection",
    labelKey: "inspection",
    value: "INSPECTION",
    checked: false,
  },
  {
    title: "Disposal",
    labelKey: "disposal",
    value: "DISPOSAL",
    checked: false,
  },
  {
    title: "Emergency",
    labelKey: "emergency",
    value: "EMERGENCY",
    checked: false,
  },
  {
    title: "Refurbishment",
    labelKey: "refurbishment",
    value: "REFURBISHMENT",
    checked: false,
  },
  {
    title: "Preventive Maintenance",
    labelKey: "preventive_maintenance",
    value: "PREVENTATIVE_MAINTENANCE",
    checked: false,
  },
];
const ASSET_TYPES = ["TOOLING", "MACHINE", "TERMINAL", "SENSOR"];
module.exports = {
  props: {
    resources: Object,
    visible: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "assets-dropdown": httpVueLoader(
      "/components/@base/dropdown/assets-dropdown.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
  },
  setup(props, ctx) {
    const workOrderTypes = ref([...WORK_ORDER_TYPES]);
    const selectedAssets = ref({});
    const clearAssetsKey = ref(0);

    const timeOptions = ref([...DEFAULT_TIME_OPTION]);
    const timeRange = ref({ ...DEFAULT_TIME_RANGE });
    const assetTypes = ref([...ASSET_TYPES]);
    const selectedFrequency = ref("DAILY");
    const {
      currentDate: selectedDate,
      getTitle: currentDateTitle,
      isRange: isRangeDate,
    } = usePicker();

    const getModalTitle = computed(() => {
      return props.resources["export_work_order"];
    });

    const checkEnable = computed(() => {
      const selectedTypes = workOrderTypes.value.filter((item) => item.checked);

      const assetValues = Object.values(selectedAssets.value);
      const isSelectedAssets = assetValues.filter(
        (item) => item && item.length > 0
      );
      console.log(
        "checkEnable",
        selectedTypes.length > 0 && isSelectedAssets.length > 0,
        selectedTypes.length,
        isSelectedAssets.length
      );
      return selectedTypes.length > 0 && isSelectedAssets.length > 0;
    });

    watch(
      () => props.visible,
      async (newVal) => {
        if (!newVal) {
          clearData();
        }
      }
    );

    const handleSelectWorkOrderType = (checked, key) => {
      const selectedItem = workOrderTypes.value.find(
        (item) => item.value === key
      );
      if (selectedItem) {
        if (checked) {
          selectedItem.checked = true;
        } else {
          selectedItem.checked = false;
        }
      }
    };

    const handleChangeDate = (timeValue, frequency) => {
      console.log("handleChangeDate", timeValue, frequency);
      const datePickerModalRef = ctx.refs.datePickerModalRef;
      selectedDate.value = timeValue;
      selectedFrequency.value = frequency;
      isRangeDate.value = frequency === "CUSTOM";
      datePickerModalRef.closeDatePicker();
    };

    const showDatePicker = () => {
      const datePickerModalRef = ctx.refs.datePickerModalRef;
      const dateValue = { ...selectedDate.value };
      datePickerModalRef.showDatePicker(selectedFrequency.value, dateValue);
    };

    const handleChangeAssets = (assets) => {
      console.log("handleChangeAssets", assets);
      selectedAssets.value = { ...assets };
    };

    const getParams = () => {
      const rawMoldIds = selectedAssets.value.mold.map((item) => item.id);
      const rawMachineIds = selectedAssets.value.machine.map((item) => item.id);
      const rawTerminalIds = selectedAssets.value.terminal.map(
        (item) => item.id
      );
      const rawCounterIds = selectedAssets.value.sensor.map((item) => item.id);

      const moldIds = rawMoldIds.join(",");
      const machineIds = rawMachineIds.join(",");
      const terminalIds = rawTerminalIds.join(",");
      const counterIds = rawCounterIds.join(",");

      const rangeType =
        selectedFrequency.value === "CUSTOM"
          ? "CUSTOM_RANGE"
          : selectedFrequency.value;

      const selectedTypes = workOrderTypes.value
        .filter((item) => item.checked)
        .map((i) => i.value);

      const typeList = selectedTypes.join(",");

      const requestData = {
        moldIds,
        machineIds,
        terminalIds,
        counterIds,
        rangeType,
        typeList,
      };

      if (
        selectedFrequency.value === "CUSTOM" ||
        selectedFrequency.value === "DAILY"
      ) {
        requestData.fromDate = selectedDate.value.fromTitle.replaceAll("-", "");
        requestData.toDate = selectedDate.value.toTitle.replaceAll("-", "");
        requestData.rangeType = "CUSTOM_RANGE";
        delete requestData.time;
      } else {
        requestData.time = selectedDate.value.fromTitle.replaceAll("-", "");
        delete requestData.fromDate;
        delete requestData.toDate;
      }
      return requestData;
    };

    const handleExport = async () => {
      const requestData = getParams();
      const param = Common.param(requestData);
      try {
        const response = await axios.get(`/api/work-order/export?${param}`, {
          responseType: "blob",
        });
        const fileNameIndex =
          response.headers["content-disposition"].indexOf("filename");
        const fileName = response.headers["content-disposition"].slice(
          fileNameIndex + "filename=".length
        );
        const fileUrl = URL.createObjectURL(response.data);
        Common.download(fileUrl, fileName);
        handleClose();
      } catch (error) {
        console.log(error);
      }
    };

    const clearData = () => {
      selectedDate.value = { ...DEFAULT_DATE };
      workOrderTypes.value = [...WORK_ORDER_TYPES].map((item) => ({
        ...item,
        checked: false,
      }));
      selectedAssets.value = {};
      timeOptions.value = [...DEFAULT_TIME_OPTION];
      timeRange.value = { ...DEFAULT_TIME_RANGE };
      selectedFrequency.value = "DAILY";
      isRangeDate.value = false;
      clearAssetsKey.value++;
    };

    const handleClose = () => {
      ctx.emit("close");
      clearData();
    };

    return {
      // STATE //
      workOrderTypes,
      timeOptions,
      selectedDate,
      currentDateTitle,
      timeRange,
      selectedFrequency,
      isRangeDate,
      clearAssetsKey,
      assetTypes,
      // COMPUTED //
      getModalTitle,
      checkEnable,
      // METHOD //
      handleClose,
      handleSelectWorkOrderType,
      handleChangeAssets,
      handleChangeDate,
      showDatePicker,
      handleExport,
    };
  },
};
</script>
<style scoped>
.create-wo-container {
  padding: 0 31px;
}

.pretty .state label:after,
.pretty .state label:before {
  content: "";
  width: calc(1em + 2px);
  height: calc(1em + 2px);
  display: block;
  box-sizing: border-box;
  border-radius: 0;
  z-index: 0;
  position: absolute;
  left: 0;
  top: 3px;
  background-color: transparent;
}
label > .badge-require {
  position: absolute;
  width: 5px;
  height: 5px;
  border-radius: 5px;
  margin-top: 6px;
  margin-left: 4px;
  color: #fff;
  background-color: #f86c6b;
}
</style>
