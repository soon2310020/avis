<template>
  <div>
    <div class="form-wrapper">
      <div class="form-item">
        <label>{{ resources["work_order_id"] }}</label>
        <base-input width="200px" :readonly="true" :value="form.workOrderId">
        </base-input>
      </div>
      <div class="form-item">
        <label>{{ resources["ref_id"] }}</label>
        <base-input
          width="200px"
          :readonly="!enabledEditRefCode"
          v-model="form.refCode"
        ></base-input>
        <button
          v-if="mode === 'history' && !enabledEditRefCode"
          class="icon-edit"
          @click="handleEnableEditRefCode"
        ></button>
      </div>
      <div class="form-item">
        <label>{{ resources["started_on"] }}</label>
        <base-button
          v-if="mode === 'history' && enabledEditDateTime"
          :icon="!enabledEditDateTime ? undefined : 'calendar'"
          level="secondary"
          @click="handleShowStartedDatePicker"
        >
          {{ getStartedOnTitle }}
        </base-button>
        <base-input
          v-else
          style="width: 109px"
          :readonly="true"
          :value="getStartedOnTitle"
        ></base-input>
        <hour-picker
          style="margin-left: 4px"
          size="medium"
          :disabled="mode === 'view' || !enabledEditDateTime"
          :selected-time="startedOnTime"
          @change-time="handleChangeStartedTime"
        ></hour-picker>
        <button
          v-if="mode === 'history' && !enabledEditDateTime"
          class="icon-edit"
          @click="handleEnableEditDateTime"
        ></button>
      </div>
      <div class="form-item">
        <label>{{ resources["completed_on"] }}</label>
        <base-button
          v-if="mode === 'history' && enabledEditDateTime"
          :icon="!enabledEditDateTime ? undefined : 'calendar'"
          level="secondary"
          @click="handleShowCompletedDatePicker"
        >
          {{ getCompletedOnTitle }}
        </base-button>
        <base-input
          v-else
          style="width: 109px"
          :readonly="true"
          :value="getCompletedOnTitle"
        ></base-input>
        <hour-picker
          style="margin-left: 4px"
          size="medium"
          :disabled="mode === 'view' || !enabledEditDateTime"
          :selected-time="completedOnTime"
          @change-time="handleChangeCompletedTime"
        ></hour-picker>
        <button
          v-if="mode === 'history' && !enabledEditDateTime"
          class="icon-edit"
          @click="handleEnableEditDateTime"
        ></button>
      </div>
      <div class="form-item">
        <label>{{ resources["image_upload"] }}</label>
        <div
          style="flex: 1"
          :style="[isOverview ? { display: 'flex', flexDirection: 'row' } : {}]"
        >
          <div
            v-if="isOverview"
            class="op-upload-button-wrap"
            :style="[isOverview ? { marginRight: '8px' } : {}]"
          >
            <base-button level="secondary">
              {{ resources["upload_image"] }}
              <img class="ml-1" src="/images/icon/upload-blue.svg" alt=""
            /></base-button>
            <input
              type="file"
              ref="fourthFileUpload"
              id="fourthFileUpload"
              accept=".gif, .jpg, .jpeg, .png"
              @click="handleResetImageUpload"
              @change="handleSelectImageUpload"
              multiple
              @delete-img="handleDeleteImageUpload"
              style="height: 40px; width: 100%"
            />
          </div>
          <preview-images-system
            :is-viewing="!isOverview"
            @delete-img="handleDeleteImageUpload"
            :images="formImageUpload"
          ></preview-images-system>
        </div>
      </div>
      <div class="form-item">
        <label>{{ resources["asset_s"] }}</label>
        <div v-if="form.assets">
          <base-chip
            v-for="item in form.assets"
            :key="item.id"
            color="blue"
            :selection="{ id: item.id, title: item.name }"
            :close-able="false"
            @close="handleRemoveAsset"
          ></base-chip>
        </div>
      </div>
      <div v-if="form.orderType === 'REFURBISHMENT'" class="form-item">
        <label>{{ resources["forecasted_max_shot"] }}</label>
        <base-input
          width="200px"
          :readonly="true"
          :value="formatNumber(form.estimatedExtendedLifeShot)"
        >
        </base-input>
      </div>
      <div class="form-item">
        <label>{{ resources["plant_s"] }}</label>
        <div>{{ listPlants }}</div>
      </div>
      <div v-if="isComplete" class="form-item">
        <label>{{ resources["checklist"] }}</label>
        <div>
          <span class="icon-check"></span>
          <base-button icon="popup" @click="handleReviewChecklist">
            {{ resources["review_checklist"] }}
          </base-button>
        </div>
      </div>
      <div v-if="isComplete" class="form-item form-item__upload">
        <label>{{ resources["checklist_files"] }}</label>
        <div class="list-files">
          <template v-if="checklistFile?.length > 0">
            <base-chip
              v-for="(file, index) in checklistFile"
              :key="index"
              :style="{ 'margin-left': index === 0 ? '8px' : '0px;' }"
              color="blue"
              :selection="{ ...file, id: file.id, title: file.name }"
            ></base-chip>
          </template>
          <div v-else>{{ resources["not_included"] }}</div>
        </div>
        <base-button
          v-if="checklistFile?.length > 0"
          icon="popup"
          @click="handleViewChecklistDocument"
          >{{ resources["review_document"] }}</base-button
        >
      </div>
      <div v-if="isComplete && form.pickingList" class="form-item">
        <label>{{ resources["pick_list"] }}</label>
        <!-- <div v-if="!selectedPickList?.id">
          <span style="color: #4b4b4b; font-weight: 300">Not Required</span>
        </div> -->
        <div>
          <span class="icon-check"></span>
          <base-button icon="popup" @click="handleReviewPicklist">{{
            resources["review_picklist"]
          }}</base-button>
        </div>
      </div>
      <div
        class="form-item-wrap"
        v-for="(item, index) in form.costs"
        :key="index"
      >
        <div class="form-item">
          <label>{{ resources["cost_usd"] }}</label>
          <base-input
            :value="item.value"
            @input="item.value = $event"
            :is-number="true"
            :readonly="!enabledEditCost"
            placeholder="Enter Cost"
          ></base-input>
          <button
            v-if="mode === 'history' && !enabledEditCost"
            class="icon-edit"
            @click="handleEnableEditCost"
          ></button>
        </div>
        <div class="form-item form-item__details">
          <label>{{ resources["details"] }}</label>
          <base-input
            full-width
            v-model="item.details"
            :readonly="!enabledEditCost"
            placeholder="Enter details of cost"
          ></base-input>
        </div>
      </div>
      <div
        v-show="enabledEditCost"
        style="margin-left: 106px; margin-top: -10px; margin-bottom: 15px"
      >
        <base-button v-if="form.costs.length > 0" @click="handleAddCost"
          >+ {{ resources["add_more_costs"] }}</base-button
        >
        <base-button v-else @click="handleAddCost"
          >+ {{ resources["add_more_costs"] }}</base-button
        >
      </div>
      <div class="form-item form-item__upload">
        <label>{{ resources["cost_file_upload"] }}</label>
        <base-button
          v-show="mode === 'history'"
          level="secondary"
          icon="upload"
          @click="handleClickUpload"
        >
          {{ resources["document_upload"] }}
        </base-button>
        <input
          type="file"
          ref="inputRef"
          multiple
          @change="handleUploadDocument"
          style="display: none"
        />
        <div class="list-files">
          <template v-if="isOverview || isFinished">
            <base-chip
              v-for="(file, index) in formAttachments"
              :key="index"
              :style="{ 'margin-left': index === 0 ? '8px' : '0px;' }"
              color="blue"
              :selection="{ ...file, id: file.id, title: file.name }"
              @close="handleRemoveDocument"
            ></base-chip>
          </template>
          <template v-if="isComplete && formAttachments.length">
            <base-chip
              v-for="(file, index) in formAttachments"
              :key="index"
              :style="{ 'margin-left': index === 0 ? '8px' : '0px;' }"
              color="blue"
              :selection="{ id: index, title: file.name }"
              :close-able="false"
            ></base-chip>
          </template>
          <div v-if="!formAttachments.length" style="margin-left: 8px">
            {{ resources["not_included"] }}
          </div>
        </div>
        <base-button
          v-if="(isComplete || isFinished) && formAttachments.length"
          icon="popup"
          @click="handleReviewDocument"
          >{{ resources["review_document"] }}</base-button
        >
      </div>
      <div class="form-item form-item__note align-top">
        <label style="margin-top: 8px">{{ resources["note"] }}</label>
        <div style="flex: 1">
          <textarea
            :readonly="isComplete || mode === 'view'"
            style="resize: none"
            rows="7"
            placeholder="Enter any notes here"
            v-model="form.note"
          ></textarea>
        </div>
      </div>
    </div>
    <!-- Request change -->
    <div
      v-if="form.status !== 'PENDING_APPROVAL'"
      class="form-item form-item__action"
    >
      <base-button
        v-if="mode === 'history' && isOverview"
        level="primary"
        type="save"
        @click="handleSaveChanges"
        >{{ resources["save_changes"] }}</base-button
      >
      <base-button
        v-if="mode === 'history' && (isComplete || isFinished)"
        level="primary"
        :disabled="!isEdited"
        @click="handleRequestChange"
        >{{ resources["request_work_order_changes"] }}</base-button
      >
      <template
        v-if="
          mode === 'view' && !isOverview && form.workOrderStatus === 'COMPLETED'
        "
      >
        <base-button
          level="primary"
          type="reject"
          @click="handleOpenRejectForm('WO_MOD_APPR_REQUESTED')"
          >{{ resources["reject_changes"] }}
        </base-button>
        <base-button level="primary" type="save" @click="handleApproveChanges">
          {{ resources["approve_changes"] }}
        </base-button>
      </template>
    </div>
    <!-- Approval -->
    <div
      v-else-if="
        form.orderType.includes('REFURBISHMENT') ||
        form.orderType.includes('DISPOSAL')
      "
      class="form-item form-item__action"
    >
      <template v-if="mode === 'view' && !isOverview">
        <base-button
          level="primary"
          type="reject"
          @click="handleOpenRejectForm('WO_CPT_APPR_REQUESTED')"
          >{{ resources["reject_work_order"] }}
        </base-button>
        <base-button
          level="primary"
          type="save"
          @click="handleApproveWorkorder"
        >
          {{ resources["approve_work_order"] }}
        </base-button>
      </template>
    </div>
    <reason-dialog
      :visible="visibleRejectForm"
      :resources="resources"
      :reason-type="reasonType"
      @close="closeReasonForm"
      @cancel="handleCancel"
      @reject="handleReject"
      @reject-change="handleRejectChangesWithReason"
    ></reason-dialog>
  </div>
</template>

<script>
module.exports = {
  name: "ReportOverview",
  components: {
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "hour-picker": httpVueLoader(
      "/components/@base/hour-picker/hour-picker.vue"
    ),
    "reason-dialog": httpVueLoader(
      "/components/work-order/src/reason-dialog.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  props: {
    mode: String,
    resources: {
      type: Object,
      default: () => ({}),
    },
    isEdited: {
      type: Boolean,
      default: false,
    },
    isVisible: Boolean,
    step: {
      type: String,
      default: "overview",
      validator(val) {
        return ["overview", "complete"].includes(val);
      },
    },
    form: Object,
    formAttachments: Array,
    getStartedOnTitle: [Function, Object, String, Number],
    getCompletedOnTitle: [Function, Object, String, Number],
    startedOnTime: Object,
    completedOnTime: Object,
    listPlants: String,
    handleRemoveAsset: { type: Function, default: () => {} },
    handleShowStartedDatePicker: { type: Function, default: () => {} },
    handleShowCompletedDatePicker: { type: Function, default: () => {} },
    handleChangeStartedTime: { type: Function, default: () => {} },
    handleChangeCompletedTime: { type: Function, default: () => {} },
    handleAddCost: { type: Function, default: () => {} },
    handleUploadDocument: { type: Function, default: () => {} },
    handleReviewDocument: { type: Function, default: () => {} },
    handleRemoveDocument: { type: Function, default: () => {} },
    handleReviewChecklist: { type: Function, default: () => {} },
    handleReviewPicklist: { type: Function, default: () => {} },
    handleSaveChanges: { type: Function, default: () => {} },
    handleRequestChange: { type: Function, default: () => {} },
    handleRejectChanges: { type: Function, default: () => {} },
    handleApproveChanges: { type: Function, default: () => {} },
    handleApproveWorkorder: { type: Function, default: () => {} },
    handleRejectWorkorder: { type: Function, default: () => {} },
    handleCancelWorkorder: { type: Function, default: () => {} },
    handleEditRefCode: { type: Function, default: () => {} },
    selectedPickList: { type: Object, default: () => {} },
    selectedCheckList: { type: Object, default: () => {} },
    checklistFile: { type: Array, default: () => [] },
    formImageUpload: { type: Array, default: () => [] },
    handleDeleteImageUpload: { type: Function, default: () => {} },
    handleSelectImageUpload: { type: Function, default: () => {} },
    handleResetImageUpload: { type: Function, default: () => {} },
  },
  setup(props, ctx) {
    const isFinished = computed(() => props.step === "finished");
    const isOverview = computed(() => props.step === "overview");
    const isComplete = computed(() => props.step === "complete");

    const isGeneralCase = computed(() => props.form?.orderType === "GENERAL");
    const isInspectionCase = computed(
      () => props.form?.orderType === "INSPECTION"
    );
    const isEmergencyCase = computed(
      () => props.form?.orderType === "EMERGENCY"
    );
    const isPMCase = computed(
      () => props.form?.orderType === "PREVENTATIVE_MAINTENANCE"
    );
    const isCMCase = computed(
      () => props.form?.orderType === "CORRECTIVE_MAINTENANCE"
    );

    const handleClickUpload = () => {
      ctx.refs.inputRef.click();
    };

    const startedOnTime = toRef(props, "startedOnTime");
    const completedOnTime = toRef(props, "completedOnTime");

    const startedOnTimeFormatted = computed(() => {
      let hh = startedOnTime.value.hour;
      let mm = startedOnTime.value.minute;
      if (startedOnTime.value.hour < 9) hh = `0${startedOnTime.value.hour}`;
      if (startedOnTime.value.minute < 9) mm = `0${startedOnTime.value.minute}`;
      return `${hh}:${mm}`;
    });

    const completedOnTimeFormatted = computed(() => {
      let hh = completedOnTime.value.hour;
      let mm = completedOnTime.value.minute;
      if (completedOnTime.value.hour < 9) hh = `0${completedOnTime.value.hour}`;
      if (completedOnTime.value.minute < 9)
        mm = `0${completedOnTime.value.minute}`;
      return `${hh}:${mm}`;
    });

    const enabledEditDateTime = ref(false);
    const enabledEditRefCode = ref(false);
    const visibleRejectForm = ref(false);
    const reasonType = ref("");

    const handleEnableEditDateTime = () => {
      enabledEditDateTime.value = true;
      ctx.emit("enable");
    };

    const handleEnableEditRefCode = () => {
      enabledEditRefCode.value = true;
      props.handleEditRefCode();
      ctx.emit("enable");
    };

    const enabledEditCost = ref(false);

    const handleEnableEditCost = () => {
      enabledEditCost.value = true;
      ctx.emit("enable-cost");
    };

    const cleanState = () => {
      console.log("@cleanState");
      enabledEditDateTime.value = false;
      enabledEditCost.value = false;
      enabledEditRefCode.value = false;
      ctx.root.$nextTick(() => {
        ctx.refs.inputRef.value = null;
      });
    };

    const handleOpenRejectForm = (type) => {
      reasonType.value = type;
      visibleRejectForm.value = true;
    };

    const handleCancel = async (reason) => {
      props.handleCancelWorkorder(reason);
      closeReasonForm();
    };

    const handleReject = async (reason) => {
      props.handleRejectWorkorder(reason);
      closeReasonForm();
    };

    const handleRejectChangesWithReason = async (reason) => {
      props.handleRejectChanges(reason);
      closeReasonForm();
    };

    const closeReasonForm = () => {
      visibleRejectForm.value = false;
    };

    const handleViewChecklistDocument = () => {
      Common.downloadDocument(props.checklistFile);
    };

    watch(
      () => props.isVisible,
      (newVal) => {
        if (!newVal) cleanState();
      },
      { immediate: true }
    );

    watch(
      () => props.step,
      () => {
        if (isOverview.value) {
          enabledEditDateTime.value = true;
          enabledEditCost.value = true;
        }
        if (isFinished.value || isComplete.value) {
          enabledEditDateTime.value = false;
          enabledEditCost.value = false;
        }
      },
      { immediate: true }
    );

    // watchEffect(() => console.log('props.isEdited', props.isEdited))
    // watchEffect(() => console.log('isFinished', isFinished.value))
    // watchEffect(() => console.log('isComplete', isComplete.value))

    return {
      isFinished,
      isOverview,
      isComplete,
      isGeneralCase,
      isInspectionCase,
      isEmergencyCase,
      isPMCase,
      isCMCase,
      startedOnTimeFormatted,
      completedOnTimeFormatted,
      enabledEditDateTime,
      enabledEditCost,
      enabledEditRefCode,
      visibleRejectForm,
      reasonType,

      handleClickUpload,
      handleEnableEditDateTime,
      handleEnableEditCost,
      handleEnableEditRefCode,
      handleOpenRejectForm,
      handleReject,
      handleRejectChangesWithReason,
      handleCancel,
      closeReasonForm,
      handleViewChecklistDocument,
    };
  },
};
</script>

<style scoped>
.form-item-wrap {
  display: flex;
}

.form-item {
  display: flex;
}

.form-item label {
  min-width: 125px;
}

.form-item__requested label {
  text-align: right;
  margin-left: 32px;
  margin-right: 20px;
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

.form-item__note textarea:read-only {
  cursor: default;
  background-color: var(--grey-5);
  border-color: var(--grey-5);
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

.list-files {
  display: flex;
}

.input-hour {
  width: 62px;
  margin-left: 4px;
}

.icon-edit {
  background-image: url(/images/icon/new/edit.svg);
  background-size: 14px;
  background-position: center;
  background-repeat: no-repeat;
  width: 25px;
  height: 25px;
  border: unset;
  border-radius: 3px;
  margin-left: 8px;
  align-self: center;
  flex-shrink: 0;
}

.icon-edit:focus-visible {
  outline: none;
}
</style>
