<template>
  <base-dialog
    :visible="visible"
    :title="dialogTitle"
    dialog-classes="dialog-xl"
    body-classes="custom-body"
    @close="$emit('close')"
  >
    <base-button class="back-button" @click="handleBackStep">
      <span name="prefix" class="icon back-arrow"></span>
      <span style="">{{ resources["back_to_previous_page"] }}</span>
    </base-button>
    <div class="steps">
      <base-steps
        :list="listSteps"
        :current="currentStep"
        @change="handleChangeStep"
      ></base-steps>
    </div>
    <div v-show="isOverview" class="form-report form-report__overview">
      <request-change-form
        step="overview"
        :is-edited="isEdited"
        :is-visible="visible"
        :mode="mode"
        :form="form"
        :resources="resources"
        :form-attachments="formAttachments"
        :checklist-file="checkListFiles"
        :get-started-on-title="getStartedOnTitle"
        :get-completed-on-title="getCompletedOnTitle"
        :started-on-time="startedOnTime"
        :completed-on-time="completedOnTime"
        :list-plants="listPlants"
        :handle-remove-asset="handleRemoveAsset"
        :handle-show-started-date-picker="handleShowStartedDatePicker"
        :handle-show-completed-date-picker="handleShowCompletedDatePicker"
        :handle-change-started-time="handleChangeStartedTime"
        :handle-change-completed-time="handleChangeCompletedTime"
        :handle-add-cost="handleAddCost"
        :handle-upload-document="handleUploadDocument"
        :handle-review-document="handleReviewDocument"
        :handle-remove-document="handleRemoveDocument"
        :handle-review-checklist="handleReviewChecklist"
        :handle-review-picklist="handleReviewPicklist"
        :handle-save-changes="handleSaveChanges"
        :handle-request-change="handleRequestChange"
        :handle-reject-changes="handleRejectChanges"
        :handle-approve-changes="handleApproveChanges"
        :handle-edit-ref-code="handleEditRefCode"
        :form-image-upload="fourthFiles"
        :handle-delete-image-upload="deleteFourthFile"
        :handle-select-image-upload="selectFourthFiles"
        :handle-reset-image-upload="resetFourthFileEvent"
      ></request-change-form>
    </div>
    <div v-show="isChecklist" class="form-report form-report__checklist">
      <div class="form-item">
        <label>Checklist ID</label>
        <base-dropdown
          v-show="!selectedChecklist.id"
          name="checklist-type"
          :options="checklistTypeOptions"
          :default="false"
          :multiple="false"
          :value="checklistType"
          @select="handleSelectChecklistType"
          >Choose Checklist Type</base-dropdown
        >
        <base-dropdown
          v-show="selectedChecklist.id"
          name="checklist-option"
          :options="checklistOptions"
          :default="false"
          :multiple="false"
          :value="selectedChecklist"
          :disabled="true"
          @select="handleSelectChecklist"
          >Choose Checklist</base-dropdown
        >
      </div>
      <div class="checklist-table">
        <ul v-if="selectedChecklist" class="checklist-list">
          <li
            v-for="(item, index) in selectedChecklist.listItems"
            :key="`${item.value}-${item.checked ? 1 : 2}`"
            class="checklist-item"
          >
            <label>
              <input
                type="checkbox"
                :readonly="mode === 'view'"
                :checked="selectedChecklist.listItems[index].checked"
                @input="handleSelectChecklistItem($event, index)"
              />
              <span> {{ item.label }} </span>
            </label>
          </li>
          <li v-if="isCreatingChecklistItem" class="checklist-item">
            <label>
              <base-input
                v-model="newChecklistItem.value"
                @keyup.enter="handleAddChecklistItem"
              ></base-input>
            </label>
          </li>
          <li v-show="selectedChecklist.id" class="checklist-item">
            <base-button @click="handleCreateChecklistItem">
              + Add checklist item
            </base-button>
          </li>
        </ul>
      </div>
      <div v-if="mode === 'history'" class="form-item form-item__action">
        <base-button
          :disabled="!hasSelectChecklistItem"
          level="primary"
          type="save"
          @click="handleSaveChecklist"
          >Save Changes</base-button
        >
      </div>
    </div>
    <div v-show="isPicklist" class="form-report form-report__picklist">
      <div class="form-item">
        <label>{{ resources["picklist_items"] }}</label>
        <base-dropdown
          v-show="!selectedPicklist.id"
          :multiple="false"
          :options="picklistOptions"
          :default="false"
          :value="selectedPicklist"
          @select="handleSelectPicklist"
          >{{ resources["choose_a_picklist"] }}</base-dropdown
        >
        <base-dropdown
          v-show="selectedPicklist.id"
          :multiple="false"
          :options="selectedPicklist.listItems"
          :default="false"
          :value="selectedPicklist"
          @select="handleSelectPicklistItem"
        >
          {{ resources["add_more_items"] }}
        </base-dropdown>
      </div>
      <div class="picklist-table">
        <div
          v-show="!listSelectedPicklistItems.length"
          class="picklist-table__note"
        >
          {{
            resources["please_choose_a_list_and_proceed_to_add_your_picklist"]
          }}
        </div>
        <ul class="picklist-list">
          <li
            v-for="(item, index) in listSelectedPicklistItems"
            :key="index"
            class="picklist-item"
          >
            <span>{{ item.label }}</span>
            <span
              class="icon-remove"
              @click="handleRemovePicklistItem(item)"
            ></span>
          </li>
        </ul>
      </div>
      <div v-if="mode === 'history'" class="form-item form-item__action">
        <base-button level="primary" type="save" @click="handleSavePicklist">{{
          resources["save_changes"]
        }}</base-button>
      </div>
    </div>
    <div v-show="isComplete" class="form-report form-report__complete">
      <request-change-form
        step="complete"
        :is-edited="isEdited"
        :is-visible="visible"
        :mode="mode"
        :form="form"
        :resources="resources"
        :checklist-file="checkListFiles"
        :form-attachments="formAttachments"
        :get-started-on-title="getStartedOnTitle"
        :get-completed-on-title="getCompletedOnTitle"
        :started-on-time="startedOnTime"
        :completed-on-time="completedOnTime"
        :list-plants="listPlants"
        :handle-remove-asset="handleRemoveAsset"
        :handle-show-started-date-picker="handleShowStartedDatePicker"
        :handle-show-completed-date-picker="handleShowCompletedDatePicker"
        :handle-change-started-time="handleChangeStartedTime"
        :handle-change-completed-time="handleChangeCompletedTime"
        :handle-add-cost="handleAddCost"
        :handle-upload-document="handleUploadDocument"
        :handle-review-document="handleReviewDocument"
        :handle-remove-document="handleRemoveDocument"
        :handle-review-checklist="handleReviewChecklist"
        :handle-review-picklist="handleReviewPicklist"
        :handle-save-changes="handleSaveChanges"
        :handle-request-change="handleRequestChange"
        :handle-reject-changes="handleRejectChanges"
        :handle-approve-changes="handleApproveChanges"
        :handle-approve-workorder="handleApproveWorkorder"
        :handle-reject-workorder="handleRejectWorkorder"
        :handle-cancel-workorder="handleCancelWorkorder"
        :selected-pick-list="selectedPicklist"
        :selected-check-list="selectedChecklist"
        :handle-edit-ref-code="handleEditRefCode"
        :form-image-upload="fourthFiles"
        :handle-delete-image-upload="deleteFourthFile"
        :handle-select-image-upload="selectFourthFiles"
        :handle-reset-image-upload="resetFourthFileEvent"
        @enable-cost="handleEnableCost"
      >
      </request-change-form>
    </div>
    <date-picker-modal
      ref="startedDatePickerRef"
      :resources="resources"
      :select-option="startedOnOptions"
      @change-date="handleChangeStartedDate"
    ></date-picker-modal>
    <date-picker-modal
      ref="completedDatePickerRef"
      :resources="resources"
      :select-option="completedOnOptions"
      @change-date="handleChangeCompletedDate"
    ></date-picker-modal>
  </base-dialog>
</template>

<script>
const initalForm = {
  workOrderId: "",
  start: Date.now(),
  end: Date.now(),
  startedOn: Date.now(),
  completedOn: Date.now(),
  assets: [],
  plants: [],
  failureTime: "",
  costs: [{ value: "", details: "" }],
  note: "",
  orderType: "",
  status: "",
  requestedBy: null,
  requestApproval: false,
  forecastedMaxShot: null,
};

const defaultChecklist = {
  id: "",
  label: "",
  value: "",
  listItems: [],
};

const defaultPicklist = {
  id: "",
  label: "",
  value: "",
  listItems: [],
};

const STEP_KEY = {
  OVERVIEW: "overview",
  CHECKLIST: "checklist",
  PICKLIST: "picklist",
  COMPLETE: "complete",
  FINISHED: "finished",
};

const CHECKLIST_TYPE = {
  MAINTENANCE: {
    value: "MAINTENANCE",
    label: "Maintenance",
  },
  REJECT_RATE: {
    value: "REJECT_RATE",
    label: "Reject Rate",
  },
};

const DIALOG_TITLE = {
  GENERAL: "Review General Work Order",
  INSPECTION: "Review Inspection Work Order",
  EMERGENCY: "Review Emergency Work Order",
  PREVENTATIVE_MAINTENANCE: "Review Preventive Maintenance",
  CORRECTIVE_MAINTENANCE: "Review Corrective Maintenance",
  REFURBISHMENT: "Review Refurbishment Workorder",
  DISPOSAL: "Review Disposal Workorder",
};

const CLONE_DIALOG_TITLE = {
  GENERAL: "report_general_work_order",
  INSPECTION: "report_inspection_work_order",
  EMERGENCY: "report_emergency_work_order",
  PREVENTATIVE_MAINTENANCE: "report_preventive_maintenance",
  CORRECTIVE_MAINTENANCE: "report_corrective_maintenance",
  REFURBISHMENT: "report_refurbishment_work_order",
  DISPOSAL: "report_disposal_work_order",
  HISTORY_GENERAL: "review_general_work_order",
  HISTORY_INSPECTION: "review_inspection_work_order",
  HISTORY_EMERGENCY: "review_emergency_work_order",
  HISTORY_PREVENTATIVE_MAINTENANCE: "review_preventive_maintenance",
  HISTORY_CORRECTIVE_MAINTENANCE: "review_corrective_maintenance",
};

module.exports = {
  name: "WorkOrderRequestChangeDialog",
  components: {
    "request-change-form": httpVueLoader(
      "/components/work-order/src/request-change-work-order-dialog/request-change-form.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "hour-picker": httpVueLoader(
      "/components/@base/hour-picker/hour-picker.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    mode: {
      type: String,
      default: "history",
      validator(val) {
        return ["history", "view", "review"].includes(val);
      },
    },
    visible: {
      type: Boolean,
      default: false,
    },
    selected: {
      type: Object,
      default: null,
    },
    type: {
      type: [],
      default: "",
      validator(val) {
        return [""].includes(val);
      },
    },
    notification: {
      type: Object,
      default: () => ({
        show: false,
        title: "",
        content: "",
      }),
    },
    handleCloseAlert: {
      type: Function,
      default: () => {},
    },
  },
  setup(props, ctx) {
    // for steps

    const fullSteps = [
      { label: "Overview", key: STEP_KEY.OVERVIEW, checked: true },
      { label: "Checklist", key: STEP_KEY.CHECKLIST, checked: true },
      { label: "Pick List", key: STEP_KEY.PICKLIST, checked: true },
      { label: "Complete", key: STEP_KEY.COMPLETE, checked: true },
    ];

    const stepsWithoutPicklist = fullSteps.filter(
      (i) => i.key !== STEP_KEY.PICKLIST
    );

    const listSteps = ref([...fullSteps]);
    const checkListFiles = ref([]);
    const isLoading = ref(false);

    const currentStep = ref(STEP_KEY.FINISHED);
    const dialogTitle = ref("");
    const isOverview = computed(() => currentStep.value === STEP_KEY.OVERVIEW);
    const isChecklist = computed(
      () => currentStep.value === STEP_KEY.CHECKLIST
    );
    const isPicklist = computed(() => currentStep.value === STEP_KEY.PICKLIST);
    const isComplete = computed(() =>
      [STEP_KEY.COMPLETE, STEP_KEY.FINISHED].includes(currentStep.value)
    );

    const handleBackStep = () => {
      const currentIndex = listSteps.value.findIndex(
        (i) => i.key === currentStep.value
      );
      if (currentIndex <= 0) {
        cleanState();
        return ctx.emit("close");
      }

      currentStep.value = listSteps.value[currentIndex - 1].key;
      console.log("@handleBackStep", { listSteps, currentStep });
    };

    const handleNextStep = () => {
      const currentIndex = listSteps.value.findIndex(
        (i) => i.key === currentStep.value
      );
      if (currentIndex >= listSteps.value.length - 1) {
        return (currentStep.value = STEP_KEY.FINISHED);
      }
      currentStep.value = listSteps.value[currentIndex + 1].key;
      console.log("@handleNextStep", { listSteps, currentStep });
    };

    const handleChangeStep = (newStep) => {
      currentStep.value = newStep.key;
    };

    const handleSaveChanges = () => {
      handleNextStep();
    };

    const isEdited = ref(false);

    // control form state
    const form = reactive({ ...initalForm });
    const formAttachments = ref([]);
    const fourthFiles = ref([]);

    const isSkipPicklist = ref(false);

    const listPlants = computed(() =>
      form.plants.map((item) => " " + item.name).toString()
    );

    const fetchCostFiles = async (workOrderId) => {
      try {
        const response = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_COST_FILE&refId=${workOrderId}`
        );
        const costFile = response.data.map((file) => ({
          ...file,
          name: file.fileName,
        }));
        formAttachments.value = costFile;
      } catch (error) {
        console.log(error);
      }
    };

    const fetchFourthFiles = async (workOrderId) => {
      const imageUploadRes = await axios.get(
        `/api/file-storage?storageType=WORK_ORDER_POW_FILE&refId=${workOrderId}`
      );
      fourthFiles.value = (imageUploadRes.data || []).map((file) => ({
        ...file,
        name: file.fileName,
      }));
    };

    const fetchChecklistFile = async (workOrderId) => {
      try {
        const response = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_CHECKLIST_FILE&refId=${workOrderId}`
        );
        checkListFiles.value = response.data.map((file) => ({
          ...file,
          name: file.fileName,
        }));
        console.log("fetchChecklistFile", checkListFiles.value);
      } catch (error) {
        console.log(error);
      }
    };

    const handleAddCost = () => {
      form.costs.push({ value: "", details: "" });
    };
    const handleEnableCost = () => {
      isEdited.value = true;
    };
    const selectFourthFiles = (e) => {
      const files = e.target.files;
      const isExitsFile = fourthFiles.value.filter(
        (item) => item.name === files[0].name
      );
      if (files && isExitsFile.length === 0) {
        const selectedFiles = Array.from(files);
        for (let i = 0; i < selectedFiles.length; i++) {
          selectedFiles[i].id = `temp-${selectedFiles[i].lastModified}`;
          fourthFiles.value.push(selectedFiles[i]);
        }
        isEdited.value = true;
      }
    };
    const resetFourthFileEvent = () => {
      const target = ctx.refs?.fourthFileUpload;
      if (target) {
        target.value = null;
      }
    };
    const deleteFourthFile = (index) => {
      const selectedFile = fourthFiles.value[index];
      if (selectedFile && !(selectedFile.id + "").includes("temp")) {
        if (_.isEmpty(form.removeFileId)) {
          form.removeFileId = [fourthFiles.value[index].id];
        } else {
          form.removeFileId.push(selectedFile.id);
        }
      }
      fourthFiles.value = fourthFiles.value.filter(
        (value, idx) => idx !== index
      );
      isEdited.value = true;
    };

    const handleUploadDocument = (event) => {
      console.log("@handleUploadDocument", event.target, formAttachments.value);
      for (let i = 0; i < event.target.files.length; i++) {
        const file = event.target.files[i];
        file.id = `temp-${file.lastModified}`;
        file.isNew = true;
        formAttachments.value.push(file);
      }
      isEdited.value = true;
    };

    const __deleteFileInStorage = async (fileId) => {
      try {
        const response = await axios.delete(`/api/file-storage/${fileId}`);
        console.log("__deleteFileInStorage", response);
      } catch (error) {
        console.log(error);
      }
    };

    const handleRemoveDocument = (doc) => {
      if (!doc.isNew) {
        __deleteFileInStorage(doc.id);
      }
      formAttachments.value = formAttachments.value.filter(
        (item) => item.id !== doc.id
      );
      console.log("@handleRemoveDocument", { doc, formAttachments });
    };

    const handleReviewDocument = () => {
      formAttachments.value.forEach((f) => {
        let fileURL = "";
        if (!f.saveLocation) {
          fileURL = URL.createObjectURL(f);
        } else {
          fileURL = f.saveLocation;
        }
        console.log("fileURL", fileURL);
        Common.download(fileURL, f.name);
      });
      console.log("@handleReviewDocument", formAttachments.value);
    };

    const handleCancelReport = () => {
      console.log("@handleCancel");
    };

    const handleRemoveAsset = (asset) => {
      console.log("@handleRemoveAsset", asset);
      form.assets = form.assets.filter((item) => item.id !== asset.id);
    };

    const handleRequestChange = () => {
      console.log("startedOnDate", startedOnDate.value);
      console.log("startedOnTime", startedOnTime.value);
      console.log("completedOnDate", completedOnDate.value);
      console.log("completedOnTime", completedOnTime.value);
      const formData = new FormData();
      const payload = {
        id: form.id,
        priority: form.priority,
        status: form.status,
        start: form.start,
        end: form.end,
        startedOn: Number(
          moment(
            startedOnDate.value.fromTitle +
              " " +
              startedOnTimeFormatted.value +
              ":00"
          ).format("X")
        ),
        completedOn: Number(
          moment(
            completedOnDate.value.fromTitle +
              " " +
              completedOnTimeFormatted.value +
              ":00"
          ).format("X")
        ),
        cost: form.cost,
        pickingList: form.pickingList,
        workOrderAssets: form.assets,
        workOrderCostList: form.costs.map((i) => ({
          cost: i.value,
          details: i.details,
        })),
        picklistId: selectedPicklist.id,
        checklistId: selectedChecklist.id,
        checklistItems: selectedChecklist.listItems
          .filter((i) => i.checked)
          .map((i) => i.value),
        picklistItems: listSelectedPicklistItems.value.map((i) => i.value),
        note: form.note,
        estimatedExtendedLifeShot: form.estimatedExtendedLifeShot,
        refCode: form.refCode,
        removeFileId: form.removeFileId,
      };
      if (isSkipPicklist.value) {
        delete payload.picklistId;
        delete payload.picklistItems;
      }
      console.log("@handleRequestChange", {
        form,
        formAttachments,
        payload,
        completedOnDate,
        startedOnDate,
      });
      for (let i = 0; i < formAttachments.value.length; i++) {
        const currentFile = formAttachments.value[i];
        const isAlreadyUpload = Boolean(
          !currentFile?.id?.toString().includes("temp")
        );
        if (!isAlreadyUpload)
          formData.append("secondFiles", formAttachments.value[i]);
      }
      for (let i = 0; i < fourthFiles.value.length; i++) {
        const currentFile = fourthFiles.value[i];
        const isAlreadyUpload = Boolean(
          !currentFile?.id?.toString().includes("temp")
        );
        if (!isAlreadyUpload)
          formData.append("forthFiles", fourthFiles.value[i]);
      }
      formData.append("payload", JSON.stringify(payload));
      ctx.emit("submit", formData);
    };

    const handleRejectChanges = (reason) => {
      ctx.emit("decline", form, reason);
    };
    const handleApproveChanges = () => {
      ctx.emit("accept", form);
    };

    const mappingForm = async (data) => {
      console.log("@mappingForm:data", { data });
      form.id = data["id"];
      form.workOrderId = data["workOrderId"];
      form.start = data["start"];
      form.end = data["end"];
      form.startedOn = data["startedOn"]
        ? data["startedOn"]
        : Date.now() / 1000; // need recheck
      form.completedOn = data["completedOn"]
        ? data["completedOn"]
        : Date.now() / 1000; // need recheck
      form.pickingList = data["pickingList"];
      form.status = data["status"];
      form.orderType = data["orderType"];
      form.plants = data["plantList"];
      form.cost = data["cost"];
      form.note = data["note"];
      form.requestedBy = data["createdBy"];
      form.priority = data["priority"];
      form.estimatedExtendedLifeShot = data["estimatedExtendedLifeShot"];
      form.assets = data["workOrderAssets"].map((item) => ({
        id: item["id"],
        name: item["assetCode"],
        assetId: item["assetId"],
        type: item["type"],
      }));
      form.requestApproval = data["requestApproval"];
      form.createdBy = data["createdBy"];
      form.refCode = data["refCode"];
      form.forecastedMaxShot = data["forecastedMaxShot"];
      form.workOrderStatus = data["workOrderStatus"];

      if (data["workOrderCostList"].length) {
        form.costs = data["workOrderCostList"].map((item) => ({
          ...item,
          value: item["cost"],
          details: item["details"],
        }));
      } else {
        form.costs = [{ value: "", details: "" }]; // set default
      }

      if (data["checklist"]) {
        selectedChecklist.id = data["checklistId"];
        selectedChecklist.value = data["checklistId"];
        selectedChecklist.label = data["checklist"]["checklistCode"];
        const __listAllItems = [];
        if (data["checklist"]["checklistItems"])
          __listAllItems.push(...data["checklist"]["checklistItems"]);
        if (data["checklistItems"])
          __listAllItems.push(...data["checklistItems"]);
        const __setItems = new Set([...__listAllItems]);
        const listChecklistAvailable = Array.from(__setItems);
        console.log("listChecklistAvailable", listChecklistAvailable);
        selectedChecklist.listItems = listChecklistAvailable.map((i) => ({
          value: i,
          label: i,
          checked: data["checklistItems"].includes(i),
        }));
      }
      if (data["picklist"] && data["pickingList"]) {
        selectedPicklist.id = data["picklistId"];
        selectedPicklist.value = data["picklistId"];
        selectedPicklist.label = data["picklist"]["checklistCode"];
        selectedPicklist.listItems = data["picklist"]["checklistItems"].map(
          (i) => ({
            value: i,
            label: i,
            checked: data["picklistItems"].includes(i),
          })
        );
        listSelectedPicklistItems.value = data["picklistItems"].map((o) => ({
          value: o,
          label: o,
          checked: true,
        }));
        console.log(`fixed pickList`, selectedChecklist);
      }
      if (!data["pickingList"]) {
        listSteps.value = [...stepsWithoutPicklist];
      }
      await fetchChecklistFile(form.id);
      await fetchFourthFiles(form.id);
      if (data["attachments"]) {
        await fetchCostFiles(form.id);
      }

      console.log("@mappingForm:form", { form });
    };

    const getDateTimeByTimeStamp = (timestamp) => {
      // timestamp must be in second
      const dateVal = {
        from: new Date(timestamp * 1000),
        to: new Date(timestamp * 1000),
        fromTitle: moment.unix(timestamp).format("YYYY-MM-DD"),
        toTitle: moment.unix(timestamp).format("YYYY-MM-DD"),
      };
      const timeVal = {
        hour: moment.unix(timestamp).format("H"),
        minute: moment.unix(timestamp).format("m"),
      };
      return { dateVal, timeVal };
    };

    watch(
      () => form.startedOn,
      (newVal) => {
        const { dateVal, timeVal } = getDateTimeByTimeStamp(newVal);
        Object.assign(startedOnDate.value, dateVal);
        Object.assign(startedOnTime.value, timeVal);
      }
    );

    watch(
      () => form.completedOn,
      (newVal) => {
        const { dateVal, timeVal } = getDateTimeByTimeStamp(newVal);
        Object.assign(completedOnDate.value, dateVal);
        Object.assign(completedOnTime.value, timeVal);
      }
    );

    watch(
      () => form.orderType,
      (newVal) => {
        if (newVal) {
          dialogTitle.value =
            props.resources[CLONE_DIALOG_TITLE[form.orderType]];
        }
      }
    );

    watch(
      () => currentStep.value,
      (newVal) => {
        dialogTitle.value = props.resources[CLONE_DIALOG_TITLE[form.orderType]];
      }
    );

    // checklist
    const checklistTypeOptions = ref([]);
    const checklistType = ref("");
    const checklistOptions = ref([]);
    const selectedChecklist = reactive({ ...defaultChecklist });
    const hasSelectChecklistItem = computed(
      () => selectedChecklist?.listItems?.filter((item) => item.checked).length
    );
    const isCreatingChecklistItem = ref(false);
    const defaultChecklistItem = {
      id: "",
      value: "",
      label: "",
      checked: true,
    };
    const newChecklistItem = reactive({ ...defaultChecklistItem });

    const handleSelectChecklistType = (selected) => {
      console.log("@handleSelectChecklistType", selected);
      checklistType.value = selected.value;
      fetchChecklistByType({ checklistType: selected.value });
      isEdited.value = true;
    };

    const handleSelectChecklist = (checklist) => {
      console.log("@handleSelectChecklist", checklist);
      Object.assign(selectedChecklist, checklist);
      isEdited.value = true;
    };

    const handleSelectChecklistItem = (event, index) => {
      console.log("@handleSelectChecklistItem", event, index);
      selectedChecklist.listItems[index].checked = event.target.checked;
      isEdited.value = true;
    };

    const handleCreateChecklistItem = () => {
      console.log("@handleCreateChecklistItem");
      isCreatingChecklistItem.value = true;
    };

    const handleAddChecklistItem = () => {
      console.log("@handleAddChecklistItem");
      selectedChecklist.listItems.push({
        ...newChecklistItem,
        label: newChecklistItem.value,
        id: newChecklistItem.value,
      });
      Object.assign(newChecklistItem, { ...defaultChecklistItem });
      isCreatingChecklistItem.value = false;
      isEdited.value = true;
    };

    const handleSaveChecklist = () => {
      console.log("@handleSaveChecklist", selectedChecklist);
      handleNextStep();
    };

    const handleSkipPicklist = () => {
      isSkipPicklist.value = true;
      handleNextStep();
    };

    const handleReviewChecklist = () => {
      console.log("@handleReviewChecklist");
      currentStep.value = STEP_KEY.CHECKLIST;
    };

    const fetchChecklistByType = async (query) => {
      const queryParams = Common.param({ type: "CHECK_LIST", ...query });
      try {
        const response = await axios.get("/api/checklist/list?" + queryParams);
        console.log("@fetchChecklist", response);
        checklistOptions.value = response.data.data.map((item) => ({
          id: item["id"],
          value: item["id"],
          label: item["checklistCode"],
          listItems: item["checklistItems"].map((i) => ({
            label: i,
            value: i,
            checked: false,
          })),
        })); // set list checklist
      } catch (error) {
        console.log(error);
      }
    };

    watch(
      checklistOptions,
      (newVal) => {
        if (newVal && newVal.length) {
          Object.assign(selectedChecklist, newVal[0]);
        }
      },
      { immediate: true }
    );

    // picklist
    const picklistOptions = ref([]);
    const selectedPicklist = reactive({ ...defaultPicklist });
    const listPicklistItems = ref([]);
    const listSelectedPicklistItems = ref([]);

    const handleSavePicklist = () => {
      handleNextStep();
    };

    const handleReviewPicklist = () => {
      currentStep.value = STEP_KEY.PICKLIST;
    };

    const fetchPicklist = async () => {
      try {
        const response = await axios.get("/api/checklist/list?type=PICK_LIST");
        console.log("response", response);
        picklistOptions.value = response.data.data.map((item) => ({
          id: item["id"],
          value: item["id"],
          label: item["checklistCode"],
          listItems: item["checklistItems"].map((i) => ({
            label: i,
            value: i,
          })),
        }));
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectPicklist = (picklist) => {
      Object.assign(selectedPicklist, picklist);
      console.log("@handleSelectPicklist:selectedPicklist", selectedPicklist);
      isEdited.value = true;
    };

    const handleSelectPicklistItem = (picklistItem) => {
      console.log("@handleSelectPicklistItem", picklistItem);
      if (
        !listSelectedPicklistItems.value.some(
          (item) => picklistItem.value === item.value
        )
      ) {
        listSelectedPicklistItems.value.push(picklistItem);
      }
      isEdited.value = true;
    };

    const handleRemovePicklistItem = (picklistItem) => {
      listSelectedPicklistItems.value = listSelectedPicklistItems.value.filter(
        (item) => item.value !== picklistItem.value
      );
      isEdited.value = true;
    };

    // control time-date state
    const startedDatePickerRef = ref(null);
    const completedDatePickerRef = ref(null);
    const {
      pickerOptions: startedOnOptions,
      currentDate: startedOnDate,
      getTitle: getStartedOnTitle,
      frequency: startedOnFrequency,
      isRange: isRangeStarted,
      time: startedOnTime,
      timeFormatted: startedOnTimeFormatted,
    } = usePicker();
    const {
      pickerOptions: completedOnOptions,
      currentDate: completedOnDate,
      getTitle: getCompletedOnTitle,
      frequency: completedOnFrequency,
      isRange: isRangeCompleted,
      time: completedOnTime,
      timeFormatted: completedOnTimeFormatted,
    } = usePicker();

    startedOnOptions.value = startedOnOptions.value.filter(
      (item) => item.type === "DAILY"
    );
    completedOnOptions.value = completedOnOptions.value.filter(
      (item) => item.type === "DAILY"
    );

    const handleShowStartedDatePicker = () => {
      startedDatePickerRef.value.showDatePicker(
        startedOnFrequency.value,
        startedOnDate.value
      );
    };

    const handleShowCompletedDatePicker = () => {
      completedDatePickerRef.value.showDatePicker(
        completedOnFrequency.value,
        completedOnDate.value
      );
    };

    const handleChangeStartedDate = (dateVal) => {
      startedOnDate.value = Object.assign({}, startedOnDate.value, dateVal);
      startedDatePickerRef.value.closeDatePicker();
      console.log("@handleChangeStartedDate", startedOnDate.value);
      isEdited.value = true;
    };
    const handleChangeCompletedDate = (dateVal) => {
      completedOnDate.value = Object.assign({}, completedOnDate.value, dateVal);
      completedDatePickerRef.value.closeDatePicker();
      console.log("@handleChangeCompletedDate", completedOnDate.value);
      isEdited.value = true;
    };

    const handleChangeStartedTime = (val, type) => {
      console.log("@handleChangeStartedTime", val, type);
      startedOnTime.value[type] = val;
      isEdited.value = true;
    };

    const handleChangeCompletedTime = (val, type) => {
      console.log("@handleChangeCompletedTime", val, type);
      completedOnTime.value[type] = val;
      isEdited.value = true;
    };

    const handleEditRefCode = () => {
      isEdited.value = true;
    };

    const handleApproveWorkorder = async () => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      const payload = {
        id: form.id,
        action: "APPROVED",
        reportedBy: form.requestedBy.id,
      };
      const res = await axios.post(
        `/api/work-order/review-complete-request-approval`,
        payload
      );
      console.log("handleApproveWorkorder", res);
      const typeText = Common.formatter.removeSeperator(form.orderType, "_");
      const toastTitle = "Success.";
      const toastMessage = `${Common.formatter.toCase(
        typeText,
        "capitalize"
      )} ${form.workOrderId} has been approved.`;
      isLoading.value = false;
      ctx.emit("show-toast", toastTitle, toastMessage);
      ctx.emit("close");
      ctx.emit("reload");
    };

    const handleCancelWorkorder = async (reason) => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      const payload = {
        id: form.id,
        action: "CANCELLED",
        reportedBy: form.requestedBy.id,
        rejectedReason: reason,
      };
      const res = await axios.post(
        `/api/work-order/review-complete-request-approval`,
        payload
      );
      isLoading.value = false;
      console.log("handleCancelWorkorder", res);
      const toastTitle = "Success.";
      const toastMessage = `Work Order ${form.workOrderId} has been cancelled.`;
      ctx.emit("show-toast", toastTitle, toastMessage);
      ctx.emit("close");
      ctx.emit("reload");
    };

    const handleRejectWorkorder = async (reason) => {
      if (isLoading.value) {
        return;
      }
      isLoading.value = true;
      const payload = {
        id: form.id,
        action: "REJECTED",
        reportedBy: form.requestedBy.id,
        rejectedReason: reason,
      };
      console.log("payload", payload);

      const res = await axios.post(
        `/api/work-order/review-complete-request-approval`,
        payload
      );
      isLoading.value = false;
      console.log("handleRejectWorkorder", res);
      const toastTitle = "Success.";
      const toastMessage = `Work Order ${form.workOrderId} has been rejected with re-submission requested.`;
      ctx.emit("show-toast", toastTitle, toastMessage);
      ctx.emit("close");
      ctx.emit("reload");
    };

    // handle effect
    const cleanState = () => {
      Object.assign(form, { ...initalForm });
      currentStep.value = STEP_KEY.FINISHED;
      listSteps.value = [...fullSteps.map((i) => ({ ...i, checked: true }))];
      dialogTitle.value = "";
      formAttachments.value = [];
      isEdited.value = false;

      // checklist
      checklistType.value = "";
      checklistOptions.value = [];
      Object.assign(selectedChecklist, { ...defaultChecklist });
      Object.assign(newChecklistItem, { ...defaultChecklistItem });
      isCreatingChecklistItem.value = false;

      // picklist
      Object.assign(selectedPicklist, { ...defaultPicklist });
      listPicklistItems.value = [];
      listSelectedPicklistItems.value = [];
    };

    watch(
      [() => props.visible, () => props.selected],
      async ([newVisible, newSelected]) => {
        if (!newVisible) cleanState();
        if (newVisible) {
          checklistTypeOptions.value = [
            CHECKLIST_TYPE.MAINTENANCE,
            CHECKLIST_TYPE.REJECT_RATE,
          ]; // set default
          fetchPicklist();

          if (newSelected) {
            mappingForm(newSelected);
          }
        }
      }
    );

    onMounted(() => {
      // checklistTypeOptions.value = [CHECKLIST_TYPE.MAINTENANCE, CHECKLIST_TYPE.REJECT_RATE] // set default
      // fetchPicklist()
    });

    // DEBUG
    // watchEffect(() => console.log(form))
    // watchEffect(() => console.log('selectedChecklist.listItems', selectedChecklist.listItems))

    return {
      CHECKLIST_TYPE,
      dialogTitle,
      isOverview,
      isChecklist,
      isPicklist,
      isComplete,

      isEdited,
      form,
      formAttachments,
      fourthFiles,

      isSkipPicklist,
      listPlants,
      listSteps,
      currentStep,
      checkListFiles,

      handleBackStep,
      handleNextStep,
      handleChangeStep,
      handleSaveChanges,
      handleAddCost,
      handleUploadDocument,
      handleRemoveDocument,
      handleReviewDocument,
      handleCancelReport,
      handleRemoveAsset,
      handleRequestChange,
      handleRejectChanges,
      handleApproveChanges,
      handleApproveWorkorder,
      handleCancelWorkorder,
      handleRejectWorkorder,
      handleEditRefCode,
      handleEnableCost,

      deleteFourthFile,
      selectFourthFiles,
      resetFourthFileEvent,

      startedOnOptions,
      completedOnOptions,

      startedOnDate,
      completedOnDate,
      startedOnTime,
      completedOnTime,

      getStartedOnTitle,
      getCompletedOnTitle,

      startedDatePickerRef,
      completedDatePickerRef,

      handleShowStartedDatePicker,
      handleShowCompletedDatePicker,
      handleChangeStartedDate,
      handleChangeCompletedDate,
      handleChangeStartedTime,
      handleChangeCompletedTime,

      // checklist
      checklistType,
      checklistTypeOptions,
      checklistOptions,
      selectedChecklist,
      hasSelectChecklistItem,
      isCreatingChecklistItem,
      newChecklistItem,
      handleSelectChecklist,
      handleSelectChecklistType,
      handleSelectChecklistItem,
      handleAddChecklistItem,
      handleCreateChecklistItem,
      handleSaveChecklist,
      handleReviewChecklist,

      // picklist
      picklistOptions,
      selectedPicklist,
      listPicklistItems,
      listSelectedPicklistItems,
      handleSelectPicklist,
      handleSelectPicklistItem,
      handleRemovePicklistItem,
      handleSkipPicklist,
      handleSavePicklist,
      handleReviewPicklist,
    };
  },
};
</script>

<style scoped>
.ant-popover-inner-content {
  padding: 0 !important;
  border: none !important;
}

.status-card-container {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  grid-column-gap: 17.25px;
}

.bottom-status-card-wrapper {
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 18px;
  /* CTA Colour/Primary CTA, Hyperlink */
  color: #3491ff;
}

.steps {
  margin: 0 auto 30px;
  width: fit-content;
}

.custom-dialog.dialog .dialog-body.custom-body {
  padding: 19px 48px 20px;
}

.form-report {
  margin-left: 8px;
  margin-bottom: 11px;
}

.form-line {
  display: flex;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.form-item label {
  width: 125px;
  min-width: 106px;
  font-size: 14.66px;
  line-height: 17px;
  margin-bottom: 0;
  text-align: left;
}

.back-button {
  position: absolute;
}

.back-button .button-base__text {
  align-items: center;
  display: inline-flex;
  gap: 8px;
}

.form-wrapper {
  max-height: 500px;
  overflow-y: auto;
}

@media (max-height: 800px) {
  .form-report .form-wrapper {
    max-height: 350px;
  }
}

.form-item-wrap {
  display: flex;
}

.form-item__details {
  width: 100%;
}

.form-item__details label {
  text-align: right;
  margin-right: 4px;
}

.form-item__upload label {
  word-break: break-word;
}

.form-item__note.form-item:last-child {
  margin-bottom: 0px;
}

.form-item__note textarea {
  display: flex;
  width: 100%;
  padding: 6px 8px;
}

.form-item__note textarea::placeholder {
  color: var(--grey-3);
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
}

.form-item__action {
  margin-top: 19px;
  justify-content: flex-end;
}

.checklist-table,
.picklist-table {
  height: 441px;
  padding-top: 30px;
  padding-left: 40px;
  overflow-y: auto;
  background: var(--grey-6);
}

@media (max-height: 800px) {
  .checklist-table,
  .picklist-table {
    max-height: 300px;
  }
}

.picklist-table__note {
  font-size: 14.66px;
  line-height: 17px;
}

.checklist-list li,
.picklist-list li {
  margin-bottom: 14px;
}

.checklist-item,
.picklist-item {
  display: flex;
  align-items: center;
}

.checklist-item label span {
  font-size: 14.66px;
  line-height: 17px;
  margin-left: 13px;
  vertical-align: text-bottom;
}

.icon-check {
  display: inline-block;
  width: 12px;
  height: 9px;
  background-color: var(--green);
  mask-image: url("/images/icon/check.svg");
  mask-position: center;
  mask-size: contain;
  -webkit-mask-image: url("/images/icon/check.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
}

.icon-remove {
  display: inline-block;
  margin-left: 15px;
  width: 8px;
  height: 8px;
  background-color: var(--grey-8);
  mask-image: url("/images/icon/remove.svg");
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/remove.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
  cursor: pointer;
}
</style>
