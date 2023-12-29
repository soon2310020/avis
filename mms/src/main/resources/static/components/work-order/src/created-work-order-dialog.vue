<template>
  <base-dialog
    :visible="visible"
    :title="title"
    dialog-classes="dialog-xl"
    body-classes="custom-body"
    @close="$emit('close')"
  >
    <div class="form-item-wrap align-top">
      <div class="form-item">
        <label>{{ resources["work_order_id"] }}</label>
        <base-input width="200px" :readonly="true" :value="form.workOrderId">
        </base-input>
      </div>
      <div
        v-if="isCreator && form.assignedTo"
        class="form-item form-item__requested align-top"
      >
        <label style="margin-top: 6px">{{ resources["assigned_to"] }}:</label>
        <div>
          <base-avatar
            style="margin-right: 8px"
            v-for="(item, index) in form.assignedTo"
            :key="index"
            :item="item.user"
          >
          </base-avatar>
        </div>
      </div>
      <div
        v-else-if="isAssigned && form.creatorList"
        class="form-item form-item__requested"
      >
        <label>{{ resources["requested_by"] }}</label>
        <div>
          <base-avatar
            style="margin-right: 8px"
            v-for="(item, index) in form.creatorList"
            :key="index"
            :item="item.user"
          >
          </base-avatar>
        </div>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["ref_id"] }}</label>
      <base-input readonly :value="form.refCode"></base-input>
    </div>
    <div class="form-item">
      <label>{{ resources["started_by"] }}</label>
      <div class="d-flex">
        <base-button disabled>{{ getStartedOnTitle }}</base-button>
        <base-button disabled style="margin-left: 8px">
          {{ startHour }}
        </base-button>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["due_by"] }}</label>
      <div class="d-flex">
        <base-button disabled>{{ getCompletedOnTitle }}</base-button>
        <base-button disabled style="margin-left: 8px">
          {{ endHour }}
        </base-button>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["asset_s"] }}</label>
      <div v-if="form.assets">
        <base-chip
          v-for="(item, index) in form.assets"
          :key="index"
          color="blue"
          :selection="{ id: item.id, title: item.name }"
          :close-able="false"
        ></base-chip>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["plant_s"] }}</label>
      <div>{{ listPlants }}</div>
    </div>
    <div class="form-item">
      <label>{{ resources["order_type"] }}</label>
      <base-input readonly :value="getOrderType"></base-input>
    </div>
    <div class="form-item">
      <label>{{ resources["priority"] }}</label>
      <base-priority-card :color="getPriorityColor">
        {{ form.priority.toLowerCase() }}
      </base-priority-card>
    </div>
    <div class="form-item form-item__details">
      <label>{{ resources["details"] }}</label>
      <div style="flex: 1">
        <textarea
          readonly
          style="resize: none; width: 100%; padding: 8px 10px"
          rows="7"
          placeholder="Enter any notes here"
          :value="form.details"
        ></textarea>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["checklist"] }}</label>
      <div v-if="form.checklist">
        <base-chip
          color="blue"
          :selection="{
            id: form.checklist.id,
            title: form.checklist.checklistCode,
          }"
          :close-able="false"
        ></base-chip>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["costs"] }}</label>
      {{ form.cost ? "Required" : "Not Required" }}
    </div>
    <div class="form-item">
      <label>{{ resources["picking_list"] }}</label>
      <span>{{ form.pickingList ? "Required" : "Not Required" }}</span>
    </div>
    <div class="form-item">
      <label>{{ resources["attachments"] }}</label>
      <div :key="renderKey">
        <template v-if="form.attachments">
          <base-chip
            v-for="file in form.attachments"
            :key="file.id"
            color="blue"
            :selection="{ id: file.id, title: file.fileName }"
            :close-able="false"
          ></base-chip>
        </template>
        <template v-else> {{ resources["none"] }} </template>
      </div>
      <base-button
        v-if="form.attachments && form.attachments.length > 0"
        icon="popup"
        @click="handleReviewDocument(form.attachments)"
        >{{ resources["review_document"] }}</base-button
      >
    </div>
    <div
      v-if="
        tab !== 'NONE' &&
        !isCreator &&
        isAssigned &&
        (canAcceptWorkOrder || canDeclineWorkOrder)
      "
      class="form-item form-item__action"
      style="margin-bottom: 0"
    >
      <base-button
        v-if="canDeclineWorkOrder"
        level="secondary"
        type="reject"
        @click="handleShowReasonForm('WO_CREATED')"
        >{{ resources["decline"] }}</base-button
      >
      <base-button
        v-if="canAcceptWorkOrder"
        level="primary"
        type="save"
        @click="handleAccept"
        :disabled="!enableClicked.accept"
        >{{ resources["accept_work_order"] }}</base-button
      >
    </div>
    <div
      v-if="tab !== 'NONE' && (canCancelWorkOrder || canEditWorkOrder)"
      class="form-item form-item__action"
      style="margin-bottom: 0"
    >
      <base-button
        v-if="canCancelWorkOrder"
        level="secondary"
        type="reject"
        @click="handleShowReasonForm('CANCEL_CREATED')"
        >{{ resources["cancel_work_order"] }}</base-button
      >
      <base-button
        v-if="canEditWorkOrder"
        level="primary"
        type="normal"
        @click="handleEdit"
        >{{ resources["edit_work_order"] }}</base-button
      >
    </div>
    <reason-dialog
      :visible="visibleReasonForm"
      :reason-type="reasonType"
      :resources="resources"
      @close="closeReasonForm"
      @cancel-created-work-order="handleCancel"
      @decline-created-work-order="handleDecline"
    ></reason-dialog>
  </base-dialog>
</template>

<script>
const ORDER_TYPE = {
  GENERAL: "General Worker Order",
  INSPECTION: "Inspection Work Order",
  EMERGENCY: "Emergency Work Order",
  PREVENTATIVE_MAINTENANCE: "Preventive Maintenance",
  CORRECTIVE_MAINTENANCE: "Corrective Maintenance",
  DISPOSAL: "Disposal Workorder",
  REFURBISHMENT: "Refurbishment",
};
const COLOR = {
  HIGH: "red",
  MEDIUM: "yellow",
  LOW: "green",
  UPCOMING: "yellow",
  REQUESTED: "purple",
  OVERDUE: "red",
  Overdue: "red",
};
const initalForm = {
  type: "",
  workOrderId: "",
  startedOn: "",
  completedOn: "",
  assets: [],
  plants: [],
  details: "",
  failureTime: "",
  costs: [{ id: "", value: "", details: "" }],
  costFile: [],
  note: "",
  status: "",
  requestedBy: null,
  orderType: "",
  priority: "",
  checklist: [],
  assignedTo: [],
  checklistItems: [],
  checklist: {
    id: "",
    checklistCode: "",
  },
  refCode: "",
};

module.exports = {
  name: "WorkOrderDetailDialog",
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    visible: Boolean,
    selected: Object,
    tab: {
      type: String,
      default: "",
      validator: (value) => {
        return ["CREATED", "ASSIGNED", "NONE", ""].includes(value);
      },
    },
  },
  components: {
    "reason-dialog": httpVueLoader(
      "/components/work-order/src/reason-dialog.vue"
    ),
  },
  setup(props, { emit, root }) {
    // const currentUser = ref(null)
    const currentUser = computed(() => headerVm.currentUser);

    const startHour = ref("");
    const endHour = ref("");
    const visibleReasonForm = ref(false);
    const reasonType = ref("");
    const enableClicked = reactive({
      accept: true,
    });

    const { currentDate: startedOnDate, getTitle: getStartedOnTitle } =
      usePicker();
    const { currentDate: completedOnDate, getTitle: getCompletedOnTitle } =
      usePicker();

    const { time: startTime, timeFormatted: startTimeFormatted } = usePicker();

    const { time: endTime, timeFormatted: endTimeFormatted } = usePicker();

    const renderKey = ref(0);

    const form = reactive({ ...initalForm });

    const mappingForm = (data) => {
      console.log("data", data);
      form.id = data["id"];
      form.workOrderId = data["workOrderId"];
      form.startedOn = data["start"];
      form.completedOn = data["end"];
      form.assets = data["workOrderAssets"].map((item) => ({
        id: item.id,
        name: item["assetCode"],
        assetId: item["assetId"],
      }));
      form.plants = data["plantList"];
      form.workOrderCostList = data["workOrderCostList"];
      form.cost = data["cost"];
      form.pickingList = data["pickingList"];
      form.checklistItems = data["checklistItems"];
      form.checklist = data["checklist"];
      form.costFile = data.costFile;
      form.note = data["note"];
      form.creatorList = _.uniqBy(
        [
          ...data?.workOrderUsers.filter(
            (item) => item.participantType === "CREATOR"
          ),
          {
            user: data?.createdBy,
            userId: data?.createdBy?.id,
            participantType: "CREATOR",
          },
        ],
        (item) => item.participantType + item.userId
      );
      form.requestedBy = data["createdBy"];
      form.assignedTo = data["workOrderUsers"].filter(
        (user) => user.participantType === "ASSIGNEE"
      );
      form.orderType = data["orderType"];
      form.priority = data["priority"];
      form.details = data["details"];
      form.workOrderStatus = data["workOrderStatus"];
      form.status = data["status"];
      form.refCode = data["refCode"];
      const start = Common.convertTimestampToDate(data["start"]);
      const end = Common.convertTimestampToDate(data["end"]);
      startTime.value = {
        hour: start.getHours(),
        minute: start.getMinutes(),
        isAddDay: false,
      };
      endTime.value = {
        hour: end.getHours(),
        minute: end.getMinutes(),
        isAddDay: false,
      };
      startHour.value = startTimeFormatted.value;
      endHour.value = endTimeFormatted.value;
      console.log("mappingForm", form, startTime.value, endTime.value);
    };

    const listPlants = computed(() =>
      form.plants.map((item) => " " + item.name).toString()
    );

    const selfInfor = computed(() => headerVm?.currentUser || {});

    const getOrderType = computed(() => {
      return ORDER_TYPE[form.orderType];
    });

    const getPriorityColor = computed(() => COLOR[form.priority]);

    const getStartHour = computed(() => {});

    const getEndHour = computed(() => {});

    const handleShowReasonForm = (type) => {
      reasonType.value = type;
      visibleReasonForm.value = true;
    };

    const canAcceptWorkOrder = computed(() => {
      return (
        isAssigned.value &&
        ["REQUESTED", "OVERDUE"].includes(form?.workOrderStatus)
      );
    });

    const canDeclineWorkOrder = computed(() => {
      return (
        isAssigned.value &&
        ["REQUESTED", "OVERDUE"].includes(form?.workOrderStatus)
      );
    });

    const canCancelWorkOrder = computed(() => {
      return (
        isCreator.value &&
        ["REQUESTED", "UPCOMING", "OVERDUE"].includes(form?.workOrderStatus)
      );
    });

    const canEditWorkOrder = computed(() => {
      return isCreator.value && form?.workOrderStatus === "REQUESTED";
    });

    const handleDecline = async (reason) => {
      console.log("@handleDecline");
      await handleChangeOrderStatus("DECLINED", reason);
    };

    const handleAccept = async () => {
      console.log("@handleAccept");
      await handleChangeOrderStatus("ACCEPTED");
    };

    const handleCancel = async (reason) => {
      console.log("@handleCancel");
      await handleChangeOrderStatus("CANCELLED", reason);
    };

    const handleReviewDocument = (fileList) => {
      console.log("handleReviewDocument");
      Common.downloadMultipleFile(fileList);
    };

    const handleEdit = () => {
      console.log("@handleEdit");
      const id = form.id;
      emit("open-edit", { id: id });
    };

    const handleChangeOrderStatus = async (status, reason) => {
      try {
        if (status === "ACCEPTED" && enableClicked.accept) {
          if (enableClicked.accept) {
            enableClicked.accept = false;
          } else return;
        }
        const id = form.id;
        const param = {
          status: status,
          reason: reason,
        };
        await axios.post(`/api/work-order/update-my-order/${id}`, param);
        closeReasonForm();
        root.$nextTick(() => {
          emit("reload");
          emit("close");
          handleShowToastCreatedWorkOrder(
            Common.formatter.toCase(status, "capitalize")
          );
        });
      } catch (error) {
        console.log(error);
      } finally {
        enableClicked.accept = true;
      }
    };

    const handleShowToastCreatedWorkOrder = (status) => {
      let snackbar = {
        title: "",
        content: "",
        status: "",
      };
      snackbar.title = `${status}.`;
      if (status === Common.formatter.toCase("CANCELLED", "capitalize")) {
        snackbar.content = `You have ${status.toLowerCase()} ${
          form.workOrderId
        } and notification has been sent to requestees.`;
      } else {
        snackbar.content = `You have ${status.toLowerCase()} ${
          form.workOrderId
        } and notification has been sent to ${
          form.requestedBy.displayName ? form.requestedBy.displayName : ""
        }.`;
      }
      // root.setToastAlertGlobal({ title: snackbar.title, content: snackbar.content, show: true })
      emit("show-toast", {
        title: snackbar.title,
        content: snackbar.content,
        show: true,
      });
    };

    const handleRemoveAsset = () => {
      form.assets = form.assets.filter((item) => item.id !== asset.id);
    };

    const closeReasonForm = () => {
      console.log("@closeReasonForm");
      visibleReasonForm.value = false;
    };

    const isCreator = computed(() => {
      // console.log('isCreator', selfInfor.value)
      if (
        form?.requestedBy?.id === currentUser?.value?.id &&
        props.tab != "ASSIGNED"
      ) {
        return true;
      }
      if (
        form?.creatorList?.some((o) => o.userId === currentUser?.value?.id) &&
        props.tab != "ASSIGNED"
      ) {
        return true;
      }
      if (isAssigned.value) {
        return false;
      }
      if (selfInfor.value.company?.companyType === "IN_HOUSE") {
        return true;
      }
    });

    const isAssigned = computed(() => {
      return form?.assignedTo?.some((o) => o.userId === currentUser?.value?.id);
    });

    const title = computed(() => {
      if (isCreator.value || isAssigned.value)
        return props?.resources["my_work_order"];
      return props.resources["review_work_order"];
    });

    watch(
      () => form.startedOn,
      (newVal) => {
        startedOnDate.value = {
          from: moment.unix(newVal),
          to: moment.unix(newVal),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
      }
    );

    watch(
      () => form.completedOn,
      (newVal) => {
        completedOnDate.value = {
          from: moment.unix(newVal),
          to: moment.unix(newVal),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
      }
    );

    watch(
      () => props.selected,
      async (newVal) => {
        console.log("watch selected", props.selected);
        if (newVal) {
          mappingForm(newVal);
          const id = form.id;
          const attachmentsRes = await axios.get(
            `/api/file-storage?storageType=WORK_ORDER_FILE&refId=${id}`
          );
          const attachments = attachmentsRes.data;
          form.attachments = attachments;
          console.log("watch selected after", form);
          renderKey.value++;
        }
      }
    );

    // DEBUG
    // watchEffect(() => console.log(form));

    onMounted(() => {
      if (root.$el.id === "headersId") {
        // prevent component being nested in another component differ from header
        root.$data.isCreatedWorkOrderModalReady = true;
      }
    });

    return {
      isCreator,
      isAssigned,
      title,
      form,
      enableClicked,
      getStartedOnTitle,
      getCompletedOnTitle,
      listPlants,
      startHour,
      endHour,
      renderKey,
      getOrderType,
      visibleReasonForm,
      reasonType,
      handleDecline,
      handleAccept,
      handleRemoveAsset,
      handleCancel,
      handleShowReasonForm,
      handleEdit,
      handleChangeOrderStatus,
      closeReasonForm,
      getPriorityColor,
      getStartHour,
      getEndHour,
      handleReviewDocument,
      canCancelWorkOrder,
      canEditWorkOrder,
      canAcceptWorkOrder,
      canDeclineWorkOrder,
    };
  },
};
</script>

<style scoped>
.custom-body {
  max-height: calc(100vh- 100px);
  overflow-y: auto;
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
</style>
