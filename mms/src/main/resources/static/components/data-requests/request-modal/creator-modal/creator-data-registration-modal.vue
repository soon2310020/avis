<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div class="create-wo-container">
      <div class="position-relative">
        <div v-if="currentStep.value > 1" class="modal__container__header">
          <div class="back-icon" @click="navigate('back')">
            <span class="icon back-arrow"></span>
            <span class="back-icon__text text--blue">
              {{ resources["back_to_previous_page"] }}
            </span>
          </div>
        </div>
        <div class="steps-zone">
          <div class="custom-base-steps">
            <base-steps
              :list="getListSteps"
              :current="currentStep.key"
              @change="handleChangeStep"
            ></base-steps>
          </div>
          <div v-if="visible" class="steps-content">
            <!--Type-->
            <form-type
              v-show="currentStep.value === 1"
              :request="request"
              :resources="resources"
              :types="getTypesArray"
              :counter-key="counterKey"
              @check="handleSelectDataType"
              @change-data-requests="handleChangeDataRequested"
            ></form-type>
            <!--Assign-->
            <form-assign
              v-show="currentStep.value === 2"
              ref="form-assign"
              :request="request"
              :resources="resources"
              :user-ids="userIds"
              @select-users="handleSelectUsers"
              @select-attachment="handleSelectAttachment"
              @delete-attachment="handleDeleteAttachment"
              @delete-uploaded-attachment="handleDeleteUploadedAttachment"
            >
            </form-assign>
            <!--Schedule-->
            <form-schedule
              v-show="currentStep.value === 3"
              :request="request"
              :resources="resources"
              :request-date="requestDate"
              :get-request-date-title="getRequestDateTitle"
              :request-time="requestTime"
              :request-time-display="requestTimeDisplay"
              :due-date="dueDate"
              :get-due-date-title="getDueDateTitle"
              :due-time="dueTime"
              :due-time-display="dueTimeDisplay"
              :show-date-picker="handleShowDueDatePicker"
              @change-due-time="handleChangeDueTime"
            ></form-schedule>
            <!--Review-->
            <form-review
              v-show="currentStep.value === 4"
              :resources="resources"
              :request="request"
              :types="getTypesArray"
              :selected-users="getSelectedUser"
              :get-request-date-title="getRequestDateTitle"
              :request-time-display="requestTimeDisplay"
              :get-due-date-title="getDueDateTitle"
              :due-time-display="dueTimeDisplay"
              @delete-attachment="handleDeleteAttachment"
              @delete-uploaded-attachment="handleDeleteUploadedAttachment"
            ></form-review>
          </div>
        </div>
      </div>
      <div
        class="d-flex modal__footer"
        style="margin-top: 80px; justify-content: flex-end"
      >
        <base-button level="primary" type="normal" @click="navigate('next')">
          {{ resources[currentStep.btnTitleKey] }}
        </base-button>
      </div>
    </div>
    <date-picker-modal
      ref="datePickerModalRefEnd"
      :select-option="timeOption"
      :resources="resources"
      :date-range="timeRange"
      @change-date="handleChangeDueDate"
    >
    </date-picker-modal>
  </base-dialog>
</template>

<script>
const INIT_REQUEST = {
  requestId: "",
  requestType: "DATA_REGISTRATION",
  companyNumber: 0,
  locationNumber: 0,
  partNumber: 0,
  moldNumber: 0,
  machineNumber: 0,
  assignedToIdList: [],
  detail: "",
  attachment: [],
  uploadedAttachment: [],
  requestDate: Common.convertDateToTimestamp(new Date()),
  dueDate: Common.convertDateToTimestamp(new Date()),
};
const DEFAULT_TYPES = {
  COMPANY: {
    labelKey: "company",
    multipleLabelKey: "companies",
    value: 0,
    checked: false,
  },
  PLANT: {
    labelKey: "plant",
    multipleLabelKey: "plants",
    value: 0,
    checked: false,
  },
  PART: {
    labelKey: "part",
    multipleLabelKey: "parts",
    value: 0,
    checked: false,
  },
  TOOLING: {
    labelKey: "tooling",
    multipleLabelKey: "toolings",
    value: 0,
    checked: false,
  },
  MACHINE: {
    labelKey: "machine",
    multipleLabelKey: "machine",
    value: 0,
    checked: false,
  },
};
const DEFAULT_STEP = [
  {
    key: "TYPE",
    label: "type",
    modalTitle: "select_data_type",
    btnTitleKey: "proceed_to_assign",
    value: 1,
    checked: false,
  },
  {
    key: "ASSIGN",
    label: "assign",
    modalTitle: "assign_items_to_data_request",
    btnTitleKey: "proceed_to_schedule",
    value: 2,
    checked: false,
  },
  {
    key: "SCHEDULE",
    label: "schedule",
    modalTitle: "schedule_data_request",
    btnTitleKey: "proceed_to_review",
    value: 3,
    checked: false,
  },
  {
    key: "REVIEW",
    label: "review",
    modalTitle: "review_data_request",
    btnTitleKey: "create_data_registration",
    value: 4,
    checked: false,
  },
];
const DEFAULT_TIME_OPTION = [{ title: "Daily", type: "DAILY", isRange: false }];
const DEFAULT_TIME_RANGE = {
  minDate: new Date(),
  maxDate: new Date("2100-01-01"),
};

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
    modalType: {
      type: String,
      default: () => "CREATE",
      validator: (value) => {
        return ["CREATE", "EDIT", "REOPEN"].includes(value);
      },
    },
    selectedDataRequest: {
      type: Object,
      default: () => ({}),
    },
  },
  components: {
    "form-type": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/data-registration-form/type.vue"
    ),
    "form-assign": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/data-registration-form/assign.vue"
    ),
    "form-schedule": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/data-registration-form/schedule.vue"
    ),
    "form-review": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/data-registration-form/review.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
  },
  setup(props, ctx) {
    // STATE //
    const currentStep = ref({});
    const listSteps = ref([]);
    const request = ref({});
    const types = ref({});
    const userIds = ref([]);
    const timeOption = ref([]);
    const timeRange = ref({});
    const frequency = ref("DAILY");
    const counterKey = ref(0);
    const isFetching = ref(false);

    const {
      currentDate: requestDate,
      getTitle: getRequestDateTitle,
      time: requestTime,
      timeFormatted: requestTimeDisplay,
      getFromTimestampValue: requestDateTimestamp,
    } = usePicker();

    const {
      currentDate: dueDate,
      getTitle: getDueDateTitle,
      time: dueTime,
      timeFormatted: dueTimeDisplay,
      getFromTimestampValue: dueDateTimestamp,
    } = usePicker();

    // COMPUTED //
    const getListSteps = computed(() => {
      const newStepsList = listSteps.value.map((item) => {
        const newItem = { ...item };
        newItem.label = props.resources[newItem.label];
        return newItem;
      });
      console.log("getListSteps", newStepsList);
      return newStepsList;
    });

    const getModalTitle = computed(() => {
      return props.resources[currentStep.value.modalTitle];
    });

    const getSelectedUser = computed(() => {
      return _.filter(userIds.value, (o) => o.checked);
    });

    const getTypesArray = computed(() => {
      const typeKeys = Object.keys(types.value);
      const result = typeKeys.map((item) => {
        const newItem = {
          ...{
            key: item,
          },
          ...types.value[item],
        };
        return newItem;
      });
      console.log("getTypesArray", result);
      return result;
    });

    // WATCH //
    watch(
      () => request.value.requestDate,
      (newVal) => {
        requestDate.value = {
          from: moment.unix(newVal).toDate(),
          to: moment.unix(newVal).toDate(),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
        requestTime.value = {
          hour: moment.unix(newVal).hours(),
          minute: moment.unix(newVal).minutes(),
          isAddDay: false,
        };
      }
    );

    watch(
      () => request.value.dueDate,
      (newVal) => {
        dueDate.value = {
          from: moment.unix(newVal).toDate(),
          to: moment.unix(newVal).toDate(),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
        dueTime.value = {
          hour: moment.unix(newVal).hours(),
          minute: moment.unix(newVal).minutes(),
          isAddDay: false,
        };
      }
    );

    watch(
      () => props.visible,
      async (newVal) => {
        if (newVal) {
          console.log("request", request.value, props.modalType);
          if (props.modalType === "CREATE") {
            const newId = await generateRequestId();
            request.value.requestId = newId;
            listSteps.value[3].btnTitleKey = "create_data_registration";
            requestDate.value = {
              from: new Date(),
              to: new Date(),
              fromTitle: moment().format("YYYY-MM-DD"),
              toTitle: moment().format("YYYY-MM-DD"),
            };
            dueDate.value = {
              from: new Date(),
              to: new Date(),
              fromTitle: moment().format("YYYY-MM-DD"),
              toTitle: moment().format("YYYY-MM-DD"),
            };
            requestTime.value = {
              hour: moment().hours(),
              minute: moment().minutes(),
              isAddDay: false,
            };
            dueTime.value = {
              hour: moment().hours(),
              minute: moment().minutes(),
              isAddDay: false,
            };
          } else {
            request.value = _.cloneDeep(props.selectedDataRequest);
            // request.value.attachment = []
            if (props.modalType === "EDIT") {
              listSteps.value[3].btnTitleKey = "edit_data_registration";
            }
            if (props.modalType === "REOPEN") {
              listSteps.value[3].btnTitleKey = "re_open_data_registration";
            }
            userIds.value = userIds.value.map((item) => ({
              ...item,
              checked: props.selectedDataRequest.assignedToList
                .map((item) => item.id)
                .includes(item.id),
            }));
            request.value.uploadedAttachment.value =
              props.selectedDataRequest.uploadedAttachment;
            if (props.selectedDataRequest.companyNumber) {
              handleSelectDataType(true, "COMPANY");
              handleChangeDataRequested(
                "COMPANY",
                props.selectedDataRequest.companyNumber
              );
            }
            if (props.selectedDataRequest.locationNumber > 0) {
              handleSelectDataType(true, "PLANT");
              handleChangeDataRequested(
                "PLANT",
                props.selectedDataRequest.locationNumber
              );
            }
            if (props.selectedDataRequest.machineNumber) {
              handleSelectDataType(true, "MACHINE");
              handleChangeDataRequested(
                "MACHINE",
                props.selectedDataRequest.machineNumber
              );
            }
            if (props.selectedDataRequest.moldNumber) {
              handleSelectDataType(true, "TOOLING");
              handleChangeDataRequested(
                "TOOLING",
                props.selectedDataRequest.moldNumber
              );
            }
            if (props.selectedDataRequest.partNumber) {
              handleSelectDataType(true, "PART");
              handleChangeDataRequested(
                "PART",
                props.selectedDataRequest.partNumber
              );
            }
          }
        } else {
          clearData();
          request.value.attachment = [];
        }
      }
    );

    // METHOD //
    const handleClose = () => {
      ctx.emit("close");
    };

    const generateRequestId = async () => {
      try {
        const res = await axios.get(`/api/data-request/get-data-request-id`);
        console.log("generateRequestId", res);
        return res.data;
      } catch (error) {
        console.log(error);
      }
      return "";
    };

    const navigate = (type) => {
      console.log("navigate", type, request.value, types.value);
      if (type === "next") {
        const foundIndex = listSteps.value.findIndex(
          (i) => i.key === currentStep.value?.key
        );
        if (foundIndex > -1)
          listSteps.value.splice(foundIndex, 1, {
            ...listSteps.value[foundIndex],
            checked: true,
          });
        currentStep.value = listSteps.value[foundIndex];
        const isLast = currentStep.value.value === listSteps.value.length;
        currentStep.value = isLast
          ? currentStep.value
          : listSteps.value[currentStep.value.value];
        if (isLast) {
          if (props.modalType === "EDIT") {
            editRequest();
          } else if (props.modalType === "REOPEN") {
            reOpenRequest();
          } else {
            createRequest();
          }
        }
      } else {
        const isFirst = currentStep.value.value === 1;
        const nextIndex = currentStep.value.value - 2;
        currentStep.value = isFirst
          ? currentStep.value
          : listSteps.value[nextIndex];
      }
    };

    const handleChangeStep = (newStep) => {
      currentStep.value = Object.assign({}, currentStep.value, { ...newStep });
    };

    const editRequest = async () => {
      try {
        const payload = setUpPayload();
        const res = await axios.post(`/api/data-request`, payload);
        ctx.emit("close");
        ctx.emit("reload");
        const toastContent = {
          title: `Success!`,
          content: `Your request has been edited.`,
          show: true,
        };
        ctx.emit("show-toast", toastContent);
      } catch (error) {
        console.log(error);
      }
    };

    const reOpenRequest = async () => {
      try {
        const payload = setUpPayload();
        const res = await axios.post(`/api/data-request/re-open`, payload);
        ctx.emit("close");
        ctx.emit("reload");
        const toastContent = {
          title: `Success!`,
          content: `Your request has been edited.`,
          show: true,
        };
        ctx.emit("show-toast", toastContent);
      } catch (error) {
        console.log(error);
      }
    };

    const createRequest = async () => {
      try {
        const payload = setUpPayload();
        if (isFetching.value) return; // avoid duplicate call
        isFetching.value = true;
        const res = await axios.post(`/api/data-request`, payload);
        const requestId = res.data.id;
        await uploadImage(requestId, request.value.attachment);
        ctx.emit("close");
        ctx.emit("reload");
        const toastContent = {
          title: `Success!`,
          content: `Your request has been created.`,
          show: true,
        };
        ctx.emit("show-toast", toastContent);
      } catch (error) {
        console.log(error);
      } finally {
        isFetching.value = false;
      }
    };

    const setUpPayload = () => {
      const payload = {};
      payload.requestId = request.value["requestId"];
      payload.requestType = request.value["requestType"];
      payload.detail = request.value["detail"];

      payload.companyNumber = types.value["COMPANY"].checked
        ? types.value["COMPANY"].value
        : 0;
      payload.locationNumber = types.value["PLANT"].checked
        ? types.value["PLANT"].value
        : 0;
      payload.partNumber = types.value["PART"].checked
        ? types.value["PART"].value
        : 0;
      payload.moldNumber = types.value["TOOLING"].checked
        ? types.value["TOOLING"].value
        : 0;
      payload.machineNumber = types.value["MACHINE"].checked
        ? types.value["MACHINE"].value
        : 0;

      payload.assignedToIdList = _.filter(userIds.value, (o) => o.checked).map(
        (item) => item.id
      );

      payload.requestDate = requestDateTimestamp.value;
      payload.dueDate = dueDateTimestamp.value;
      if (props.modalType === "EDIT" || props.modalType === "REOPEN") {
        payload.id = request.value.id;
      }
      const formData = createFormData(payload);
      console.log("createRequest", payload, formData);
      return formData;
    };

    const uploadImage = async (requestId, files) => {
      const payload = createImageFormData(requestId, files);
      try {
        const res = axios.post("/api/file-storage", payload);
      } catch (error) {
        console.log(error);
      }
    };

    const createFormData = (data) => {
      let formData = new FormData();
      formData.append("payload", JSON.stringify(data));
      return formData;
    };

    const createImageFormData = (requestId, files) => {
      let formData = new FormData();
      (files || []).forEach((element) => {
        formData.append("files", element);
      });
      formData.append("storageType", "DATA_REQUEST_FILE");
      formData.append("refId", `${requestId}`);
      return formData;
    };

    const handleSelectDataType = (checked, key) => {
      if (checked) {
        types.value[key].checked = true;
      } else {
        types.value[key].checked = false;
      }
      console.log("handleSelectDataType", checked, types.value);
    };

    const handleChangeDataRequested = (key, value) => {
      types.value[key].value = value;
    };

    const handleSelectUsers = (users) => {
      userIds.value = users;
    };

    const handleSelectAttachment = (files) => {
      (files || []).forEach((element) => {
        request.value.attachment.push(element);
      });
    };

    const handleDeleteAttachment = (index) => {
      request.value.attachment = request.value.attachment.filter(
        (value, idx) => idx !== index
      );
      const formAssign = ctx.refs["form-assign"];
      if (formAssign) {
        if (request.value.attachment.length === 0) {
          formAssign.handleClearFileUpload("");
        } else {
          formAssign.handleClearFileUpload("1221");
        }
      }
    };

    const handleDeleteUploadedAttachment = async (fileId) => {
      request.value.uploadedAttachment =
        request.value.uploadedAttachment.filter((item) => item.id !== fileId);
      try {
        await axios.delete(`/api/file-storage/${fileId}`);
        root.forceUpdate();
      } catch (error) {
        console.log(error);
      }
    };

    const handleShowDueDatePicker = () => {
      const datePickerModalRefEnd = ctx.refs.datePickerModalRefEnd;
      const dateValue = { ...dueDate.value };
      datePickerModalRefEnd.showDatePicker(frequency.value, dateValue);
    };

    const handleChangeDueDate = (timeValue, frequency) => {
      const datePickerModalRefEnd = ctx.refs.datePickerModalRefEnd;
      dueDate.value = timeValue;
      frequency.value = frequency;
      datePickerModalRefEnd.closeDatePicker();
    };

    const handleChangeDueTime = (value, type) => {
      dueTime.value[type] = value;
      console.log("handleChangeHour", value, type);
    };

    const clearData = () => {
      currentStep.value = { ...DEFAULT_STEP[0] };
      listSteps.value = [...DEFAULT_STEP];
      request.value = { ...INIT_REQUEST };
      timeOption.value = [...DEFAULT_TIME_OPTION];
      timeRange.value = { ...DEFAULT_TIME_RANGE };
      types.value = _.cloneDeep({ ...DEFAULT_TYPES });
      userIds.value = userIds.value.map((item) => ({
        ...item,
        checked: false,
      }));

      requestDate.value = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      };
      dueDate.value = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      };
      requestTime.value = {
        hour: moment().hours(),
        minute: moment().minutes(),
        isAddDay: false,
      };
      dueTime.value = {
        hour: moment().hours(),
        minute: moment().minutes(),
        isAddDay: false,
      };
      counterKey.value++;
      console.log("clearData", types.value, DEFAULT_TYPES);
    };

    // created method //
    const initModal = () => {
      clearData();
    };

    watch(
      () => headerVm?.listUsers,
      (newVal) => {
        if (!newVal) return;
        userIds.value = newVal.map((child) => ({
          ...child,
          title: child.displayName,
          checked: false,
        }));
      },
      { immediate: true }
    );

    // CREATED //
    initModal();

    return {
      // STATE //
      request,
      currentStep,
      types,
      timeOption,
      timeRange,
      requestDate,
      getRequestDateTitle,
      requestTime,
      requestTimeDisplay,
      dueDate,
      getDueDateTitle,
      dueTime,
      dueTimeDisplay,
      userIds,
      counterKey,
      // COMPUTED //
      getListSteps,
      getModalTitle,
      getSelectedUser,
      getTypesArray,
      // METHOD //
      handleClose,
      navigate,
      handleChangeStep,
      handleSelectDataType,
      handleChangeDataRequested,
      handleSelectUsers,
      handleSelectAttachment,
      handleDeleteAttachment,
      handleDeleteUploadedAttachment,
      handleShowDueDatePicker,
      handleChangeDueDate,
      handleChangeDueTime,
    };
  },
};
</script>

<style scoped>
.steps-content {
  overflow-y: scroll;
  max-height: 550px;
  padding-top: 10px;
  min-height: 400px;
}

.custom-dropdown {
  display: inline-block;
}

.custom-base-steps {
  display: flex;
  justify-content: center;
  margin-top: 6px;
  margin-bottom: 24px;
}

.create-wo-container {
  padding: 0 31px;
}

.step-content {
  /* min-height: 400px; */
}

.hidden-zone {
  opacity: 0.5;
  pointer-events: none;
}

.modal__container__header {
  position: absolute;
  left: 0;
  top: 0;
}

.back-icon {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}

.col-form-label {
  min-width: 120px;
}

.modal__footer {
  margin-top: 80px;
  justify-content: flex-end;
}

.modal__footer__button {
  margin-left: 8px;
}

.custom-modal__body {
  padding: 20px 25px !important;
}

.d-flex .modal__footer {
  margin-top: 0;
}

textarea:read-only {
  color: var(--grey-dark);
  background: var(--grey-5);
  border: none;
}

@media (max-height: 800px) {
  .steps-zone .steps-content {
    max-height: 350px;
  }
}

.form-item-wrap {
  display: flex;
}

.form-item {
  display: flex;
}

.form-item__requested label {
  text-align: right;
  margin: 0 20px 0 32px;
}

.form-item__details label {
  align-self: flex-start;
}

.form-item__details textarea {
  display: flex;
  padding: 6px 8px;
}

.form-item__details textarea::placeholder {
  padding: 6px 8px;
  font-size: 14.66px;
  line-height: 17px;
  color: var(--grey-3);
}

.form-item__details textarea:read-only {
  cursor: default;
  color: var(--grey-dark);
  border-color: var(--grey-5);
  background-color: var(--grey-5);
}

.form-item__details textarea:read-only::placeholder {
  color: var(--grey-dark);
}

.form-item__action {
  margin-top: 19px;
  justify-content: flex-end;
}

.form-item__action > * {
  margin-left: 8px;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
</style>
