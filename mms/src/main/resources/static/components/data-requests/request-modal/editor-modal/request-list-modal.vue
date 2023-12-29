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
      <review-request
        :resources="resources"
        :request="request"
        :selected-type="getSelectedType"
        :selected-users="request.assignedToList"
        :get-request-date-title="getRequestDateTitle"
        :request-time-display="requestTimeDisplay"
        :get-due-date-title="getDueDateTitle"
        :due-time-display="dueTimeDisplay"
        @complete-data="handleCompleteData"
      >
      </review-request>
    </div>
    <div v-if="!isViewing" class="button-wrapper">
      <base-button
        level="primary"
        type="save"
        @click="handleSubmit"
        :disabled="!hasCompletedAllDataTypes"
      >
        {{ resources["save"] }}
      </base-button>
    </div>
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
    total: 0,
  },
  {
    key: "PLANT",
    labelKey: "plant",
    multipleLabelKey: "plants",
    value: 0,
    total: 0,
  },
  {
    key: "PART",
    labelKey: "part",
    multipleLabelKey: "parts",
    value: 0,
    total: 0,
  },
  {
    key: "TOOLING",
    labelKey: "tooling",
    multipleLabelKey: "toolings",
    value: 0,
    total: 0,
  },
  {
    key: "MACHINE",
    labelKey: "machine",
    multipleLabelKey: "machine",
    value: 0,
    total: 0,
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
    reloadKey: {
      type: Number,
      default: 0,
    },
    isViewing: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    "review-request": httpVueLoader(
      "/components/data-requests/request-modal/editor-modal/request-list/review.vue"
    ),
  },
  setup(props, ctx) {
    // STATE //
    const request = ref({});
    const types = ref([...TYPES]);

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
    const getModalTitle = computed(() => {
      return (
        props.resources["request_id"] +
        ": " +
        props.selectedDataRequest.requestId
      );
    });

    const getSelectedType = computed(() => {
      return types.value;
    });

    const hasCompletedAllDataTypes = computed(() => {
      return !_.find(
        types.value,
        (type) => type.total !== 0 && type.value < type.total
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
        console.log("show modal", newVal, props.isViewing);
        if (newVal) {
          initData();
        }
      }
    );

    watch(
      () => props.reloadKey,
      (newVal) => {
        if (newVal) {
          initData();
        }
      }
    );

    // METHOD //
    const handleClose = () => {
      ctx.emit("close");
    };

    const handleSubmit = async () => {
      try {
        const params = {
          id: props.selectedDataRequest.id,
          dataRequestStatus: "COMPLETED",
        };
        await axios.post("/api/data-request/changeStatus", params);
        ctx.emit("reload");
        const toastData = {
          title: "Success!",
          content:
            props.selectedDataRequest?.requestType === "DATA_REGISTRATION"
              ? `Your Data Registration has been completed.`
              : `Your Data Completion has been completed.`,
        };
        ctx.emit("show-toast", toastData);
        ctx.emit("close");
      } catch (e) {
      } finally {
        ctx.emit("close");
      }
    };

    const initData = () => {
      request.value = { ...props.selectedDataRequest };

      const requestDateDefault = Common.convertTimestampToDate(
        props.selectedDataRequest?.requestDate
      );
      requestDate.value = {
        from: requestDateDefault,
        to: requestDateDefault,
        fromTitle: moment(requestDateDefault).format("YYYY-MM-DD"),
        toTitle: moment(requestDateDefault).format("YYYY-MM-DD"),
      };

      requestTime.value = {
        hour: moment(requestDateDefault).hour(),
        minute: moment(requestDateDefault).minute(),
        isAddDay: false,
      };

      const dueDateDefault = Common.convertTimestampToDate(
        props.selectedDataRequest?.dueDate
      );
      dueDate.value = {
        from: dueDateDefault,
        to: dueDateDefault,
        fromTitle: moment(dueDateDefault).format("YYYY-MM-DD"),
        toTitle: moment(dueDateDefault).format("YYYY-MM-DD"),
      };
      dueTime.value = {
        hour: moment(dueDateDefault).hour(),
        minute: moment(dueDateDefault).minute(),
        isAddDay: false,
      };

      types.value = types.value.map((item) => {
        const newItem = { ...item };
        switch (newItem.key) {
          case "COMPANY":
            newItem.value = request.value.companyDoneNumber;
            newItem.total = request.value.companyNumber;
            break;
          case "PLANT":
            newItem.value = request.value.locationDoneNumber;
            newItem.total = request.value.locationNumber;
            break;
          case "PART":
            newItem.value = request.value.partDoneNumber;
            newItem.total = request.value.partNumber;
            break;
          case "TOOLING":
            newItem.value = request.value.moldDoneNumber;
            newItem.total = request.value.moldNumber;
            break;
          case "MACHINE":
            newItem.value = request.value.machineDoneNumber;
            newItem.total = request.value.machineNumber;
            break;
        }
        return newItem;
      });
    };

    const handleCompleteData = (key) => {
      const modalType =
        props.selectedDataRequest?.requestType === "DATA_REGISTRATION"
          ? "CREATE"
          : "COMPLETE";
      ctx.emit("complete-data", key, modalType);
    };

    // created method //
    const initModal = () => {
      request.value = { ...INIT_REQUEST };
    };

    // CREATED //
    initModal();

    return {
      // STATE //
      request,
      getRequestDateTitle,
      getDueDateTitle,
      requestTimeDisplay,
      dueTimeDisplay,
      // COMPUTED //
      getModalTitle,
      getSelectedType,
      hasCompletedAllDataTypes,
      // METHOD //
      handleClose,
      handleSubmit,
      handleCompleteData,
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
  width: 100%;
  padding: 6px 8px;
}

.form-item__details textarea::placeholder {
  padding: 8px 6px;
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
