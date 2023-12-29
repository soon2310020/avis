<template>
  <base-dialog
    :visible="visible"
    :title="dialogTitle"
    dialog-classes="dialog-xl"
    body-classes="custom-body"
    @close="$emit('close')"
    :dialog-styles="{ zIndex: 6000 }"
  >
    <base-button
      v-show="!isOverview"
      class="back-button"
      @click="handleBackStep"
    >
      <span name="prefix" class="icon back-arrow"></span>
      <span style="">{{ resources["back_to_previous_page"] }}</span>
    </base-button>
    <div class="steps" v-if="!viewOnly">
      <base-steps
        :list="listSteps"
        :current="currentStep"
        @change="handleChangeStep"
      ></base-steps>
    </div>
    <div v-else style="height: 54px"></div>
    <div v-show="isOverview" class="form-report form-report__overview">
      <reported-form
        step="overview"
        ref="reportRef"
        :resources="resources"
        :visible="visible"
        :form="form"
        :form-attachments="formAttachments"
        :get-started-on-title="getStartedOnTitle"
        :get-completed-on-title="getCompletedOnTitle"
        :started-on-time="startedOnTime"
        :completed-on-time="completedOnTime"
        :list-plants="listPlants"
        :fourth-files="fourthFiles"
        :handle-remove-asset="handleRemoveAsset"
        :handle-show-started-date-picker="handleShowStartedDatePicker"
        :handle-show-completed-date-picker="handleShowCompletedDatePicker"
        :handle-change-started-time="handleChangeStartedTime"
        :handle-change-completed-time="handleChangeCompletedTime"
        :handle-add-cost="handleAddCost"
        :handle-upload-document="handleUploadDocument"
        :handle-review-document="handleReviewDocument"
        :handle-remove-document="handleRemoveDocument"
        :handle-proceed-to-checklist="handleProceedToChecklist"
        :handle-review-checklist="handleReviewChecklist"
        :handle-review-picklist="handleReviewPicklist"
        :selected-fourth-files="selectedFourthFiles"
        :delete-fourth-files="deleteFourthFiles"
        :view-only="viewOnly"
        @save-draft="handleSaveDraft"
      ></reported-form>
    </div>
    <div v-show="isChecklist" class="form-report form-report__checklist">
      <div class="form-item" v-if="!viewOnly">
        <label>{{ resources["checklist_id"] }}</label>
        <base-dropdown
          v-show="!selectedChecklist.id"
          name="checklist-type"
          :options="getChecklist"
          :default="false"
          :multiple="false"
          :value="checklistType"
          @select="handleSelectChecklistType"
        >
          {{ resources["choose_checklist_type"] }}
        </base-dropdown>
        <base-dropdown
          v-show="selectedChecklist.id"
          name="checklist-option"
          :options="checklistOptions"
          :default="false"
          :multiple="false"
          :value="selectedChecklist"
          :disabled="isFixedChecklist"
          @select="handleSelectChecklist"
        >
          {{ resources["choose_checklist"] }}
        </base-dropdown>
        <base-chip
          v-show="selectedChecklist.id"
          title-field="label"
          class="ml-2"
          :selection="selectedChecklist"
          :allow-tooltips="false"
          color="blue-purple"
          :close-able="true"
          @close="handleClearSelectedChecklist"
        ></base-chip>
      </div>
      <div class="checklist-table">
        <ul v-show="selectedChecklist" class="checklist-list">
          <li
            v-for="(item, index) in selectedChecklist.listItems"
            :key="index"
            class="checklist-item"
          >
            <label>
              <input
                :disabled="viewOnly"
                type="checkbox"
                v-model="selectedChecklist.listItems[index].checked"
              />
              <span> {{ item.label }} </span>
            </label>
          </li>
          <li v-show="isCreatingChecklistItem" class="checklist-item">
            <label>
              <base-input
                v-model="newChecklistItem.value"
                @keyup.enter="handleAddChecklistItem"
              >
              </base-input>
            </label>
          </li>
          <li v-show="selectedChecklist.id && !viewOnly" class="checklist-item">
            <base-button @click="handleCreateChecklistItem">
              + {{ resources["add_checklist_item"] }}
            </base-button>
          </li>
        </ul>
      </div>
      <div
        v-if="['REFURBISHMENT', 'DISPOSAL'].includes(form.orderType)"
        class="form-item form-item__note align-top"
      >
        <label style="margin-top: 8px">{{ resources["attachment_s"] }}</label>
        <div style="flex: 1">
          <div class="op-upload-button-wrap">
            <base-button level="secondary">
              {{ resources["upload_file_image"] }}
              <img class="ml-1" src="/images/icon/upload-blue.svg" alt="" />
            </base-button>
            <input
              type="file"
              ref="fileupload"
              id="files1"
              @change="selectedThirdFiles"
              multiple
              style="height: 40px; width: 100%"
              accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
            />
          </div>
          <preview-images-system
            :is-viewing="false"
            @delete-img="deleteThirdFiles"
            :images="thirdFiles"
          >
          </preview-images-system>
        </div>
      </div>
      <div class="form-item form-item__action">
        <base-button
          v-if="form.status !== 'COMPLETED'"
          style="margin-right: 8px"
          level="secondary"
          type="save"
          @click="handleSaveDraft"
          >{{ resources["save_progress"] }}</base-button
        >
        <base-button
          v-if="form.status !== 'COMPLETED'"
          level="primary"
          @click="handleConfirmChecklist"
          >{{ resources["confirm_checklist"] }}</base-button
        >
      </div>
    </div>
    <div v-show="isPicklist" class="form-report form-report__picklist">
      <div class="form-item" v-if="!viewOnly">
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
              v-if="!viewOnly"
              class="icon-remove"
              @click="handleRemovePicklistItem(item)"
            ></span>
          </li>
        </ul>
      </div>
      <div class="form-item form-item__action">
        <base-button
          v-if="form.status !== 'COMPLETED'"
          style="margin-right: 8px"
          level="secondary"
          type="save"
          @click="handleSaveDraft"
          >{{ resources["save_progress"] }}</base-button
        >
        <base-button
          v-if="form.status !== 'COMPLETED'"
          style="margin-right: 8px"
          level="secondary"
          @click="handleSkipPicklist"
          >{{ resources["skip_this_step"] }}</base-button
        >
        <base-button
          v-if="form.status !== 'COMPLETED'"
          level="primary"
          @click="handleConfirmPicklist"
          >{{ resources["confirm_picklist"] }}</base-button
        >
      </div>
    </div>
    <div v-show="isComplete" class="form-report form-report__complete">
      <reported-form
        step="complete"
        ref="reportRef"
        :resources="resources"
        :visible="visible"
        :form="form"
        :form-attachments="formAttachments"
        :checklist-file="checkListFiles"
        :get-started-on-title="getStartedOnTitle"
        :get-completed-on-title="getCompletedOnTitle"
        :started-on-time="startedOnTime"
        :completed-on-time="completedOnTime"
        :list-plants="listPlants"
        :fourth-files="fourthFiles"
        :is-loading="isLoading"
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
        :handle-complete="handleComplete"
        :selected-fourth-files="selectedFourthFiles"
        :delete-fourth-files="deleteFourthFiles"
        :view-only="viewOnly"
        :selected-pick-list="selectedPicklist"
        :selected-check-list="selectedChecklist"
        @save-draft="handleSaveDraft"
      >
      </reported-form>
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
  workOrderUsers: [],
  requestApproval: false,
  estimatedExtendedLifeShot: "",
  accumulatedShot: "",
  refCode: "",
  allowInputRefCode: true,
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
  GENERAL: {
    value: "GENERAL",
    label: "General",
  },
  MAINTENANCE: {
    value: "MAINTENANCE",
    label: "Maintenance",
  },
  REJECT_RATE: {
    value: "REJECT_RATE",
    label: "Reject Rate",
  },
  REFURBISHMENT: {
    value: "REFURBISHMENT",
    label: "Refurbishment",
  },
  DISPOSAL: {
    value: "DISPOSAL",
    label: "Disposal",
  },
  QUALITY_ASSURANCE: {
    value: "QUALITY_ASSURANCE",
    label: "Quality Assurance",
  },
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

const initCurrentDate = () => ({
  from: new Date(),
  to: new Date(),
  fromTitle: moment().format("YYYY-MM-DD"),
  toTitle: moment().format("YYYY-MM-DD"),
});

const initDefaultTime = () => ({ hour: "0", minute: "0", isAddDay: false });

module.exports = {
  components: {
    "reported-form": httpVueLoader(
      "/components/work-order/src/reported-work-order-dialog/reported-form.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "hour-picker": httpVueLoader(
      "/components/@base/hour-picker/hour-picker.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
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
    viewOnly: {
      type: Boolean,
      default: () => false,
    },
    handleSubmit: Function,
  },
  setup(props, ctx) {
    // for steps

    const fullSteps = [
      { label: "Overview", key: STEP_KEY.OVERVIEW, checked: false },
      { label: "Checklist", key: STEP_KEY.CHECKLIST, checked: false },
      { label: "Pick List", key: STEP_KEY.PICKLIST, checked: false },
      { label: "Complete", key: STEP_KEY.COMPLETE, checked: false },
    ];

    const checklistCountByType = ref([]);
    const checklistAvailable = ref([]); // non-empty checklist
    const isLoading = ref(false);

    const stepsWithoutPicklist = fullSteps.filter(
      (i) => i.key !== STEP_KEY.PICKLIST
    );

    const listSteps = ref([
      ...fullSteps.map((i) => ({ ...i, checked: false })),
    ]);
    const thirdFiles = ref([]);
    // const fileupload = ref()
    const checkListFiles = ref([]);
    const fourthFiles = ref([]);

    const currentStep = ref(STEP_KEY.OVERVIEW);
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
      if (
        props.viewOnly &&
        [STEP_KEY.CHECKLIST, STEP_KEY.PICKLIST].includes(currentStep.value)
      ) {
        currentStep.value = STEP_KEY.COMPLETE;
        return;
      }
      if (props.viewOnly && STEP_KEY.COMPLETE) {
        cleanState();
        return ctx.emit("close");
      }
      const currentIndex = listSteps.value.findIndex(
        (i) => i.key === currentStep.value
      );
      if (currentIndex < 0) {
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
        listSteps.value = Vue.set(listSteps.value, currentIndex, {
          ...listSteps.value[currentIndex],
          checked: true,
        });
        return (currentStep.value = STEP_KEY.FINISHED);
      }

      listSteps.value.splice(currentIndex, 1, {
        ...listSteps.value[currentIndex],
        checked: true,
      });
      currentStep.value = listSteps.value[currentIndex + 1].key;
      console.log("@handleNextStep", { listSteps, currentStep });
    };

    const handleChangeStep = (newStep) => {
      currentStep.value = newStep.key;
    };

    // control form state
    const form = reactive({ ...initalForm });
    const formAttachments = ref([]);
    const isFixedChecklist = ref(false);
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

    const fetchImageFiles = async (workOrderId) => {
      try {
        const response = await axios.get(
          `/api/file-storage?storageType=WORK_ORDER_POW_FILE&refId=${workOrderId}`
        );
        const imageFiles = response.data.map((file) => ({
          ...file,
          name: file.fileName,
        }));
        fourthFiles.value = imageFiles;
      } catch (error) {
        console.log(error);
      }
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

    const handleUploadDocument = (event) => {
      for (let i = 0; i < event.target.files.length; i++) {
        const file = event.target.files[i];
        file.id = `temp-${file.lastModified}`;
        file.isNew = true;
        formAttachments.value.push(file);
      }
      console.log("@handleUploadDocument", formAttachments.value);
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
      // formAttachments.value.forEach((f) => {
      //   let fileURL = "";
      //   if (!f.saveLocation) {
      //     fileURL = URL.createObjectURL(f);
      //   } else {
      //     fileURL = f.saveLocation;
      //   }
      //   console.log("fileURL", fileURL);
      //   Common.download(fileURL, f.name);
      // });
      formAttachments.value.forEach(async (element) => {
        await Common.downloadFile(element.saveLocation, element.fileName);
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

    const handleProceedToChecklist = () => {
      console.log("@handleProceedToChecklist");
      handleNextStep();
    };

    const handleComplete = async (isProgress) => {
      isLoading.value = true;
      const formData = new FormData();
      console.log("startedOnDate", startedOnDate.value);
      console.log("completedOnDate", completedOnDate.value);
      console.log("startedOnTimeFormatted", startedOnTimeFormatted.value);
      console.log("completedOnTimeFormatted", completedOnTimeFormatted.value);
      console.log(
        "startedOn",
        moment(
          startedOnDate.value.fromTitle +
            " " +
            startedOnTimeFormatted.value +
            ":00"
        ).format("X")
      );
      console.log(
        "completedOn",
        moment(
          completedOnDate.value.fromTitle +
            " " +
            completedOnTimeFormatted.value +
            ":00"
        ).format("X")
      );
      let payload = {
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
        estimatedExtendedLifeShot: form.estimatedExtendedLifeShot,
        note: form.note,
        refCode: form.refCode,
      };
      if (["REFURBISHMENT", "DISPOSAL"].includes(form.orderType)) {
        payload.requestApproval = form.requestApproval;
      }
      if ("REFURBISHMENT" === form.orderType) {
        payload.estimatedExtendedLifeShot = form.estimatedExtendedLifeShot;
      }
      if (isProgress) {
        payload.inProcessing = true;
      } else {
        delete payload.inProcessing;
      }
      if (isSkipPicklist.value) {
        delete payload.picklistId;
        delete payload.picklistItems;
      }
      console.log("@handleComplete", formAttachments.value, thirdFiles.value);
      for (let i = 0; i < formAttachments.value.length; i++) {
        const currentFile = formAttachments.value[i];
        const isAlreadyUpload = Boolean(
          !currentFile?.id?.toString().includes("temp")
        );
        if (!isAlreadyUpload)
          formData.append("secondFiles", formAttachments.value[i]);
      }

      for (let j = 0; j < thirdFiles.value.length; j++) {
        const currentFile = thirdFiles.value[j];
        const isAlreadyUpload = Boolean(
          !currentFile?.id?.toString().includes("temp")
        );
        console.log("before append: isAlreadyUpload: ", isAlreadyUpload);
        if (!isAlreadyUpload) {
          console.log("append", thirdFiles.value[j]);
          formData.append("files", thirdFiles.value[j]);
        }
      }
      if (fourthFiles.value) {
        for (let j = 0; j < fourthFiles.value.length; j++) {
          const currentFile = fourthFiles.value[j];
          const isAlreadyUpload = Boolean(
            !currentFile?.id?.toString().includes("temp")
          );
          console.log("before append: isAlreadyUpload: ", isAlreadyUpload);
          if (!isAlreadyUpload) {
            console.log("append", fourthFiles.value[j]);
            formData.append("forthFiles", fourthFiles.value[j]);
          }
        }
      }
      formData.append("payload", JSON.stringify(payload));
      console.log("@handleComplete after", formData);
      ctx.emit("submit", formData, isProgress);
      await props.handleSubmit(formData, isProgress);
      isLoading.value = false;
    };

    const mappingForm = async (data) => {
      console.log("@mappingForm:data", { data });
      form.id = data["id"];
      form.workOrderId = data["workOrderId"];
      form.start = data["start"];
      form.startedOn = !data["startedOn"] ? data["start"] : data["startedOn"];
      form.end = data["end"];
      form.completedOn = !data["completedOn"]
        ? Date.now() / 1000
        : data["completedOn"];
      form.pickingList = data["pickingList"];
      form.status = data["status"];
      form.orderType = data["orderType"];
      form.plants = data["plantList"];
      form.cost = data["cost"];
      form.note = data["note"];
      form.requestedBy = data["createdBy"];
      form.priority = data["priority"];
      form.accumulatedShot = data["accumulatedShot"];
      form.reportFailureShot = data["reportFailureShot"];
      form.startWorkOrderShot = data["startWorkOrderShot"];
      form.failureTime = data["failureTime"];
      form.assets = data["workOrderAssets"].map((item) => ({
        id: item["id"],
        name: item["assetCode"],
        assetId: item["assetId"],
        type: item["type"],
      }));
      form.requestApproval = data["requestApproval"];
      form.refCode = data["refCode"];
      form.estimatedExtendedLifeShot = data["estimatedExtendedLifeShot"];
      console.log("check refcode", data["refCode"], data["refCode"] == "");
      if (data["workOrderCostList"].length) {
        form.costs = data["workOrderCostList"].map((item) => ({
          value: item["cost"],
          details: item["details"],
        }));
      } else {
        if (!form.cost) {
          form.costs = [];
        } else {
          form.costs = [{ value: "", details: "" }]; // set default
        }
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
        isFixedChecklist.value = true;
        console.log(`fixed checklist`, selectedChecklist);
        ctx.root.$nextTick(() => {
          selectedChecklist.listItems = listChecklistAvailable.map((i) => ({
            value: i,
            label: i,
            checked: data["checklistItems"].includes(i),
          }));
        });
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
        listSteps.value = [
          ...stepsWithoutPicklist.map((i) => ({ ...i, checked: false })),
        ];
      }

      if (data["attachments"]) {
        await fetchCostFiles(form.id);
        await fetchChecklistFile(form.id);
        await fetchImageFiles(form.id);
      }

      console.log("@mappingForm:form", { form });
    };

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
        if (props.viewOnly) {
          if ([STEP_KEY.CHECKLIST, STEP_KEY.PICKLIST].includes(newVal)) {
            dialogTitle.value =
              newVal === STEP_KEY.PICKLIST
                ? selectedPicklist?.label
                : selectedChecklist?.label;
          } else {
            dialogTitle.value =
              props.resources[CLONE_DIALOG_TITLE[form.orderType]];
          }
        }
      }
    );

    // checklist
    const checklistTypeOptions = ref([]);
    const checklistType = ref("");
    const checklistOptions = ref([]);
    const selectedChecklist = reactive({ ...defaultChecklist });
    watch(selectedChecklist, (newVal) =>
      console.log("selectedChecklist", newVal)
    );
    // const hasSelectChecklistItem = computed(
    //   () => selectedChecklist?.listItems?.filter((item) => item.checked).length
    // );
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
    };

    const handleSelectChecklist = (checklist) => {
      console.log("@handleSelectChecklist", checklist);
      Object.assign(selectedChecklist, checklist);
      isFixedChecklist.value = true;
    };

    const handleClearSelectedChecklist = () => {
      Object.assign(selectedChecklist, { ...defaultChecklist });
      isFixedChecklist.value = false;
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
    };

    const handleConfirmChecklist = () => {
      console.log("@handleConfirmChecklist", selectedChecklist);
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

    const handleConfirmPicklist = () => {
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
    };

    const handleRemovePicklistItem = (picklistItem) => {
      listSelectedPicklistItems.value = listSelectedPicklistItems.value.filter(
        (item) => item.value !== picklistItem.value
      );
      console.log(
        "handleRemovePicklistItem",
        listSelectedPicklistItems.value,
        selectedPicklist.id
      );
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
      Object.assign(startedOnDate.value, dateVal);
      startedDatePickerRef.value.closeDatePicker();
      console.log("@handleChangeStartedDate", startedOnDate.value);
    };
    const handleChangeCompletedDate = (dateVal) => {
      Object.assign(completedOnDate.value, dateVal);
      completedDatePickerRef.value.closeDatePicker();
      console.log("@handleChangeCompletedDate", completedOnDate.value);
    };

    const handleChangeStartedTime = (val, type) => {
      console.log("@handleChangeStartedTime", val, type);
      startedOnTime.value[type] = val;
    };

    const handleChangeCompletedTime = (val, type) => {
      console.log("@handleChangeCompletedTime", val, type);
      completedOnTime.value[type] = val;
    };

    const deleteThirdFiles = (index) => {
      console.log("deleteThirdFiles", index);
      thirdFiles.value = thirdFiles.value.filter((value, idx) => idx !== index);
      if (thirdFiles.value.length === 0) {
        ctx.refs.fileupload.value = "";
      } else {
        ctx.refs.fileupload.value = "1221";
      }
    };

    const selectedThirdFiles = (e) => {
      for (let i = 0; i < e.target.files.length; i++) {
        const file = e.target.files[i];
        file.id = `temp-${file.lastModified}`;
        file.isNew = true;
        thirdFiles.value.push(file);
      }
      console.log("@selectedThirdFiles", thirdFiles.value);
    };

    const deleteFourthFiles = (index) => {
      console.log("deleteFourthFiles", index);
      fourthFiles.value = fourthFiles.value.filter(
        (value, idx) => idx !== index
      );
    };

    const selectedFourthFiles = (e) => {
      console.log("selectedFourthFiles", e.target.files);
      for (let i = 0; i < e.target.files.length; i++) {
        const file = e.target.files[i];
        file.id = `temp-${file.lastModified}`;
        file.isNew = true;
        fourthFiles.value.push(file);
      }
      console.log("@selectedThirdFiles", fourthFiles.value);
    };

    // const getDraftData = async (id) => {
    //     try {
    //         const res = await axios.get(`/api/work-order/get-draft-complete/${id}`)
    //         console.log('getDraftData', res)
    //         return res.data
    //     } catch (error) {
    //         console.log(error)
    //     }
    // }

    // const getDraftFormData = () => {
    //     const formData = new FormData()
    //     let payload = {
    //         id: form.id,
    //         startedOn: Number(moment(startedOnDate.value.fromTitle + ' ' + startedOnTimeFormatted.value + ':00').format("X")),
    //         completedOn: Number(moment(completedOnDate.value.fromTitle + ' ' + completedOnTimeFormatted.value + ':00').format('X')),
    //         pickingList: form.pickingList,
    //         workOrderCostList: form.costs.map(i => ({ cost: i.value, details: i.details })),
    //         checklistId: selectedChecklist.id,
    //         checklistItems: selectedChecklist.listItems.filter(i => i.checked).map(i => i.value),
    //         picklistId: selectedPicklist.id,
    //         picklistItems: listSelectedPicklistItems.value.map(i => i.value),
    //         note: form.note
    //     }
    //     console.log('getDraftFormData', payload)
    //     if (['REFURBISHMENT', 'DISPOSAL'].includes(form.orderType)) {
    //         payload.requestApproval = form.requestApproval
    //     }
    //     if (isSkipPicklist.value) {
    //         delete payload.picklistId
    //         delete payload.picklistItems
    //     }
    //     formData.append('payload', JSON.stringify(payload))
    //     return formData
    // }

    const handleSaveDraft = async () => {
      handleComplete(true);
    };

    const getChecklistsCount = async () => {
      try {
        const res = await axios.get(`/api/checklist/checkListTypeCount`);
        checklistCountByType.value = res.data;
        console.log("getChecklistsCount", checklistCountByType.value);
        for (const [key, value] of Object.entries(checklistCountByType.value)) {
          if (value > 0) {
            checklistAvailable.value.push(key);
          }
        }
      } catch (error) {
        console.log(error);
      }
    };

    const getChecklist = computed(() => {
      let availableByType = [];
      switch (form.orderType) {
        case "GENERAL":
          availableByType = [CHECKLIST_TYPE.GENERAL];
          break;
        case "INSPECTION":
          availableByType = [
            CHECKLIST_TYPE.GENERAL,
            CHECKLIST_TYPE.QUALITY_ASSURANCE,
            CHECKLIST_TYPE.MAINTENANCE,
          ];
          break;
        case "EMERGENCY":
          availableByType = [
            CHECKLIST_TYPE.GENERAL,
            CHECKLIST_TYPE.MAINTENANCE,
          ];
          break;
        case "PREVENTATIVE_MAINTENANCE":
        case "CORRECTIVE_MAINTENANCE":
          availableByType = [CHECKLIST_TYPE.MAINTENANCE];
          break;
        case "REFURBISHMENT":
          availableByType = [CHECKLIST_TYPE.REFURBISHMENT];
          break;
        case "DISPOSAL":
          availableByType = [CHECKLIST_TYPE.DISPOSAL];
          break;
        default:
          break;
      }

      return availableByType.filter((i) =>
        checklistAvailable.value.some((j) => i.value === j)
      );
    });

    watch(
      () => form.startedOn,
      (newVal) => {
        const dateVal = {
          from: new Date(newVal * 1000),
          to: new Date(newVal * 1000),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
        const timeVal = {
          hour: moment.unix(newVal).format("H"),
          minute: moment.unix(newVal).format("m"),
        };
        startedOnDate.value = Object.assign({}, startedOnDate.value, dateVal);
        startedOnTime.value = Object.assign({}, startedOnTime.value, timeVal);
      }
    );

    watch(
      () => form.completedOn,
      (newVal) => {
        const dateVal = {
          from: new Date(newVal * 1000),
          to: new Date(newVal * 1000),
          fromTitle: moment.unix(newVal).format("YYYY-MM-DD"),
          toTitle: moment.unix(newVal).format("YYYY-MM-DD"),
        };
        const timeVal = {
          hour: moment.unix(newVal).format("H"),
          minute: moment.unix(newVal).format("m"),
        };
        completedOnDate.value = Object.assign(
          {},
          completedOnDate.value,
          dateVal
        );
        completedOnTime.value = Object.assign(
          {},
          completedOnTime.value,
          timeVal
        );
        console.log();
      }
    );

    // handle effect
    const cleanState = () => {
      Object.assign(form, { ...initalForm });
      currentStep.value = STEP_KEY.OVERVIEW;
      listSteps.value = [...fullSteps.map((i) => ({ ...i, checked: false }))];
      dialogTitle.value = "";
      formAttachments.value = [];
      startedOnDate.value = initCurrentDate();
      completedOnDate.value = initCurrentDate();
      startedOnTime.value = initDefaultTime();
      completedOnTime.value = initDefaultTime();
      console.log("ctx.refs.reportRef", ctx.refs.reportRef);

      // checklist
      // checklist
      checklistAvailable.value = [];
      checklistType.value = null;
      checklistOptions.value = [];
      Object.assign(selectedChecklist, { ...defaultChecklist });
      Object.assign(newChecklistItem, { ...defaultChecklistItem });
      isCreatingChecklistItem.value = false;
      isFixedChecklist.value = false;

      // picklist
      Object.assign(selectedPicklist, { ...defaultPicklist });
      listPicklistItems.value = [];
      listSelectedPicklistItems.value = [];

      //File
      fourthFiles.value = [];
      thirdFiles.value = [];
    };

    watch(
      [() => props.visible, () => props.selected],
      async ([newVisible, newSelected]) => {
        if (!newVisible) cleanState();
        if (newVisible) {
          if (props.viewOnly) {
            currentStep.value = STEP_KEY.COMPLETE;
          }
          if (newSelected) {
            await mappingForm(newSelected);
            checklistTypeOptions.value = [
              CHECKLIST_TYPE.GENERAL,
              CHECKLIST_TYPE.MAINTENANCE,
              CHECKLIST_TYPE.REJECT_RATE,
              CHECKLIST_TYPE.REFURBISHMENT,
              CHECKLIST_TYPE.DISPOSAL,
            ]; // set default
            await getChecklistsCount();
            await fetchPicklist();
          }
        }
      }
    );

    // watchEffect(() => console.log('form.costs change', form.costs))
    watch(
      () => form.costs,
      (newVal) => console.log("form.costs", newVal),
      { deep: true }
    );
    console.log("form", form.costs);

    return {
      CHECKLIST_TYPE,
      dialogTitle,
      isOverview,
      isChecklist,
      isPicklist,
      isComplete,
      isLoading,

      form,
      formAttachments,
      isSkipPicklist,
      listPlants,
      listSteps,
      currentStep,
      thirdFiles,
      fourthFiles,
      checkListFiles,
      getChecklist,

      handleBackStep,
      handleNextStep,
      handleChangeStep,
      handleAddCost,
      handleUploadDocument,
      handleRemoveDocument,
      handleReviewDocument,
      handleCancelReport,
      handleRemoveAsset,
      handleProceedToChecklist,
      handleComplete,
      deleteThirdFiles,
      selectedThirdFiles,
      selectedFourthFiles,
      deleteFourthFiles,

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
      handleSaveDraft,

      // checklist
      checklistType,
      checklistTypeOptions,
      checklistOptions,
      selectedChecklist,
      // hasSelectChecklistItem,
      isFixedChecklist,
      isCreatingChecklistItem,
      newChecklistItem,
      handleSelectChecklist,
      handleSelectChecklistType,
      handleClearSelectedChecklist,
      handleAddChecklistItem,
      handleCreateChecklistItem,
      handleConfirmChecklist,
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
      handleConfirmPicklist,
      handleReviewPicklist,
    };
  },
};
</script>

<style scoped>
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
</style>
