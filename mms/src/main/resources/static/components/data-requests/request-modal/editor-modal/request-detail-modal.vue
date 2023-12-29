<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div class="accept-request-wrapper">
      <review-completion
        :resources="resources"
        :request="request"
        :types="getSelectedType"
        :selected-users="selectedDataRequest.assignedToList || []"
        :get-request-date-title="getRequestDateTitle"
        :request-time-display="requestTimeDisplay"
        :get-due-date-title="getDueDateTitle"
        :due-time-display="dueTimeDisplay"
      >
      </review-completion>
    </div>
    <div class="button-wrapper" v-show="showAcceptDeclined">
      <base-button
        level="secondary"
        type="reject"
        @click="handleDecline"
        style="margin-right: 8px"
      >
        {{ resources["decline"] }}
      </base-button>
      <base-button level="primary" type="save" @click="handleAccept">
        {{ resources["accept_request"] }}
      </base-button>
    </div>
    <div class="button-wrapper" v-show="showCancelEdit">
      <base-button
        level="secondary"
        type="reject"
        @click="handleCancel"
        style="margin-right: 8px"
      >
        {{ resources["cancel_request"] }}
      </base-button>
      <base-button level="primary" @click="handleEditRequest">
        {{ resources["edit_request"] }}
      </base-button>
    </div>
    <request-decline-reason
      :resources="resources"
      :visible="isShowDeclineReasonModal"
      @close="handleCloseDeclineReasonModal"
      style="z-index: 5002"
      @decline-data-request="onDeclineDataRequest"
    ></request-decline-reason>
    <request-cancel-reason
      :resources="resources"
      :visible="isShowCancelReasonModal"
      @close="handleCloseCancelReasonModal"
      style="z-index: 5002"
      @cancel-data-request="onCancelDataRequest"
    ></request-cancel-reason>
  </base-dialog>
</template>

<script>
const INIT_REQUEST = {
  requestType: "DATA_REGISTRATION",
};
const TYPES = [
  {
    key: "COMPANY",
    labelKey: "company",
    multipleLabelKey: "companies",
    value: 0,
  },
  { key: "PLANT", labelKey: "plant", multipleLabelKey: "plants", value: 0 },
  { key: "PART", labelKey: "part", multipleLabelKey: "parts", value: 0 },
  {
    key: "TOOLING",
    labelKey: "tooling",
    multipleLabelKey: "toolings",
    value: 0,
  },
  {
    key: "MACHINE",
    labelKey: "machine",
    multipleLabelKey: "machine",
    value: 0,
  },
];

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
    selectedDataRequest: {
      type: Object,
      default: () => ({}),
    },
  },
  components: {
    "review-completion": httpVueLoader(
      "/components/data-requests/request-modal/creator-modal/data-completion-form/review.vue"
    ),
    "request-decline-reason": httpVueLoader(
      "/components/data-requests/request-modal/editor-modal/request-decline-reason.vue"
    ),
    "request-cancel-reason": httpVueLoader(
      "/components/data-requests/request-modal/editor-modal/request-cancel-reason.vue"
    ),
  },
  setup(props, ctx) {
    // STATE //
    const request = ref({});
    const types = ref([...TYPES]);
    const isShowDeclineReasonModal = ref(false);
    const isShowCancelReasonModal = ref(false);
    const incompleteDataNumber = ref({});
    // const currentUser = ref({})
    const currentUser = computed(() => headerVm?.currentUser);

    const {
      time: requestDateTime,
      currentDate: requestDate,
      getTitle: getRequestDateTitle,
      timeFormatted: requestTimeDisplay,
    } = usePicker();

    const {
      time: dueDateTime,
      currentDate: dueDate,
      getTitle: getDueDateTitle,
      timeFormatted: dueTimeDisplay,
    } = usePicker();

    // COMPUTED //
    const getModalTitle = computed(() => {
      return (
        props.resources["request_id"] +
        ": " +
        props.selectedDataRequest.requestId
      );
    });

    const getSelectedType = computed(() => {
      return _.filter(types.value, (item) => item.checked);
    });

    const isCreator = computed(
      () => props.selectedDataRequest?.createdBy?.id === currentUser?.value?.id
    );

    const showAcceptDeclined = computed(() => {
      return (
        !isCreator.value &&
        props.selectedDataRequest.dataRequestStatus === "REQUESTED"
      );
    });

    const showCancelEdit = computed(() => {
      return (
        isCreator.value &&
        props.selectedDataRequest.dataRequestStatus === "REQUESTED"
      );
    });
    // WATCH //
    watch(
      () => request.requestDate,
      (newVal) => {}
    );

    watch(
      () => props.visible,
      (newVal) => {
        console.log("show modal", newVal, props.selectedDataRequest);
        if (newVal) {
          initData();
          getDataTypeNumbers();
        }
      }
    );

    // METHOD //
    const handleClose = () => {
      ctx.emit("close");
    };

    const handleDecline = () => {
      handleOpenDeclineReasonModal();
    };

    const handleAccept = () => {
      onAcceptDataRequest();
    };

    const handleEditRequest = () => {
      ctx.emit("on-edit", props.selectedDataRequest);
    };
    const handleCancel = () => {
      handleOpenCancelReasonModal();
    };

    const handleOpenDeclineReasonModal = () => {
      isShowDeclineReasonModal.value = true;
    };
    const handleCloseDeclineReasonModal = () => {
      isShowDeclineReasonModal.value = false;
    };

    const handleOpenCancelReasonModal = () => {
      isShowCancelReasonModal.value = true;
    };
    const handleCloseCancelReasonModal = () => {
      isShowCancelReasonModal.value = false;
    };

    const onDeclineDataRequest = async (reason) => {
      try {
        const params = {
          id: props.selectedDataRequest.id,
          reason,
          dataRequestStatus: "DECLINED",
        };
        await axios.post("/api/data-request/changeStatus", params);
        ctx.emit("reload");
        const toastData = {
          title: "Declined.",
          content: `You have declined ${props.selectedDataRequest.requestId} and notification has been sent to ${props.selectedDataRequest.createdBy?.name}.`,
        };
        ctx.emit("show-toast", toastData);
        ctx.emit("on-declined");
      } catch (e) {
      } finally {
        handleCloseDeclineReasonModal();
        ctx.emit("close");
      }
    };

    const onCancelDataRequest = async (reason) => {
      try {
        const params = {
          id: props.selectedDataRequest.id,
          reason,
          dataRequestStatus: "CANCELLED",
        };
        await axios.post("/api/data-request/changeStatus", params);
        ctx.emit("reload");
        const toastData = {
          title: "Canceled.",
          content: `You have canceled ${props.selectedDataRequest.requestId} and notification has been sent to ${props.selectedDataRequest.createdBy?.name}.`,
        };
        ctx.emit("show-toast", toastData);
        ctx.emit("on-canceled");
      } catch (e) {
      } finally {
        handleCloseCancelReasonModal();
        ctx.emit("close");
      }
    };

    const onAcceptDataRequest = async () => {
      try {
        const params = {
          id: props.selectedDataRequest.id,
          dataRequestStatus: "IN_PROGRESS",
        };
        await axios.post("/api/data-request/changeStatus", params);
        ctx.emit("reload");
        const toastData = {
          title: "Accepted.",
          content: `You have accepted ${props.selectedDataRequest.requestId} and notification has been sent to ${props.selectedDataRequest.createdBy?.name}.`,
        };
        // ctx.emit('show-toast', toastData);
        ctx.emit("on-accept", params);
      } catch (e) {
      } finally {
        ctx.emit("close");
      }
    };

    const getDataTypeNumbers = async () => {
      try {
        const res = await axios.get(
          `/api/data-request/get-number-incomplete-data`
        );
        incompleteDataNumber.value = res.data;
      } catch (error) {
        console.log(error);
      }
    };

    const getTypeTitle = (item, valueNumber) => {
      console.log("getTypeTitle", item, valueNumber);
      return (
        (valueNumber > 1
          ? props.resources[item.multipleLabelKey]
          : props.resources[item.labelKey]) + ` (${valueNumber})`
      );
    };

    const initData = () => {
      request.value = {
        requestType: props.selectedDataRequest.requestType,
        requestId: props.selectedDataRequest.requestId,
      };
      request.value.uploadedAttachment =
        props.selectedDataRequest.uploadedAttachment;
      request.value.detail = props.selectedDataRequest?.detail;

      const dueDateDefault = moment.unix(props.selectedDataRequest?.dueDate);
      const dueDateDefaultTitle = dueDateDefault.format("YYYY-MM-DD");
      dueDate.value = {
        from: dueDateDefault,
        to: dueDateDefault,
        fromTitle: dueDateDefaultTitle,
        toTitle: dueDateDefaultTitle,
      };
      dueDateTime.value = {
        hour: dueDateDefault.hour(),
        minute: dueDateDefault.minute(),
        isAddDay: false,
      };

      const requestDateDefault = moment.unix(
        props.selectedDataRequest?.requestDate
      );
      const requestDateTitle = requestDateDefault.format("YYYY-MM-DD");
      requestDate.value = {
        from: requestDateDefault,
        to: requestDateDefault,
        fromTitle: requestDateTitle,
        toTitle: requestDateTitle,
      };
      requestDateTime.value = {
        hour: requestDateDefault.hour(),
        minute: dueDateDefault.minute(),
        isAddDay: false,
      };

      types.value = types.value.map((item) => {
        switch (item.key) {
          case "COMPANY":
            const companyValue =
              props.selectedDataRequest.requestType === "DATA_COMPLETION"
                ? incompleteDataNumber.value.companyNumber || 0
                : props.selectedDataRequest?.companyNumber || 0;
            return {
              ...item,
              value: companyValue,
              checked:
                props?.selectedDataRequest?.isSelectedCompany ||
                props.selectedDataRequest.companyNumber > 0,
              title: getTypeTitle(item, companyValue),
            };
          case "PLANT":
            const plantValue =
              props.selectedDataRequest.requestType === "DATA_COMPLETION"
                ? incompleteDataNumber.value.locationNumber || 0
                : props.selectedDataRequest?.locationNumber || 0;
            return {
              ...item,
              value: plantValue,
              checked:
                props?.selectedDataRequest?.isSelectedLocation ||
                props.selectedDataRequest?.locationNumber > 0,
              title: getTypeTitle(item, plantValue),
            };
          case "PART":
            const partValue =
              props.selectedDataRequest.requestType === "DATA_COMPLETION"
                ? incompleteDataNumber.value.partNumber || 0
                : props.selectedDataRequest?.partNumber || 0;
            return {
              ...item,
              value: partValue,
              checked:
                props?.selectedDataRequest?.isSelectedPart ||
                props.selectedDataRequest?.partNumber,
              title: getTypeTitle(item, partValue),
            };
          case "TOOLING":
            const moldValue =
              props.selectedDataRequest.requestType === "DATA_COMPLETION"
                ? incompleteDataNumber.value.moldNumber || 0
                : props.selectedDataRequest?.moldNumber || 0;
            return {
              ...item,
              value: moldValue,
              checked:
                props?.selectedDataRequest?.isSelectedMold ||
                props.selectedDataRequest?.moldNumber,
              title: getTypeTitle(item, moldValue),
            };
          case "MACHINE":
            const machineValue =
              props.selectedDataRequest.requestType === "DATA_COMPLETION"
                ? incompleteDataNumber.value.machineNumber || 0
                : props.selectedDataRequest?.machineNumber || 0;
            return {
              ...item,
              value: machineValue,
              checked:
                props?.selectedDataRequest?.isSelectedMachine ||
                props.selectedDataRequest?.machineNumber > 0,
              title: getTypeTitle(item, machineValue),
            };
        }
      });
    };

    // created method //
    const initModal = async () => {
      request.value = { ...INIT_REQUEST };
    };

    // CREATED //
    initModal();

    return {
      // STATE //
      request,
      isShowDeclineReasonModal,
      isShowCancelReasonModal,
      getRequestDateTitle,
      getDueDateTitle,
      requestTimeDisplay,
      dueTimeDisplay,
      // COMPUTED //
      getModalTitle,
      getSelectedType,
      showAcceptDeclined,
      showCancelEdit,
      // METHOD //
      handleClose,
      handleDecline,
      handleAccept,
      handleCloseDeclineReasonModal,
      handleOpenDeclineReasonModal,
      onDeclineDataRequest,
      onCancelDataRequest,
      onAcceptDataRequest,
      handleEditRequest,
      handleCancel,
      handleOpenCancelReasonModal,
      handleCloseCancelReasonModal,
    };
  },
};
</script>

<style scoped>
.button-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

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

.accept-request-wrapper {
  padding: 60px 48px;
}
</style>
