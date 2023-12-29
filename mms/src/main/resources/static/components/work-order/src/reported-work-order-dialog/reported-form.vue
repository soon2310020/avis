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
          :readonly="!form.allowInputRefCode || isComplete"
          v-model="form.refCode"
          :placeholder="resources['order_reference_id']"
        >
        </base-input>
      </div>
      <div
        v-if="'CORRECTIVE_MAINTENANCE' === form?.orderType"
        class="form-item"
      >
        <label>{{ resources["failure_time"] }}</label>
        <base-input
          style="width: 109px"
          :readonly="true"
          :value="getFailureTime.date"
        ></base-input>
        <base-input
          style="width: 64px; margin-left: 4px"
          :readonly="true"
          :value="getFailureTime.time"
        ></base-input>
        <div>
          <span style="margin: 0 8px">{{ resources["at"] }}:</span>
          <base-input
            style="width: 109px"
            :readonly="true"
            :value="`${
              form.reportFailureShot > 0
                ? form.reportFailureShot + ' shots'
                : '0 shot'
            }`"
          ></base-input>
        </div>
      </div>
      <div class="form-item">
        <div>
          <label class="d-flex align-center">
            <span>{{ resources["started_on"] }}</span>
            <div class="dot--red" style="margin: 0 5px"></div>
          </label>
        </div>
        <base-button
          v-if="isOverview"
          :disabled="isComplete"
          :icon="isComplete ? undefined : 'calendar'"
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
          :selected-time="startedOnTime"
          @change-time="handleChangeStartedTime"
          :disabled="viewOnly || isComplete"
        ></hour-picker>
        <div v-if="isComplete && 'CORRECTIVE_MAINTENANCE' === form?.orderType">
          <span style="margin: 0 8px">{{ resources["at"] }}:</span>
          <base-input
            style="width: 109px"
            :readonly="true"
            :value="`${
              form.startWorkOrderShot > 0
                ? form.startWorkOrderShot + ' shots'
                : '0 shot'
            }`"
          ></base-input>
        </div>
        <div
          v-else-if="
            isComplete &&
            'PREVENTATIVE_MAINTENANCE' === form?.orderType &&
            getAccumulatedShotFormat
          "
        >
          <span style="margin: 0 8px">{{ resources["at"] }}:</span>
          <base-input
            style="width: 109px"
            :readonly="true"
            :value="`${
              accumulatedShot > 0
                ? getAccumulatedShotFormat + ' shots'
                : '0 shot'
            }`"
          ></base-input>
        </div>
      </div>
      <div class="form-item">
        <div>
          <label class="d-flex align-center">
            <span>{{ resources["completed_on"] }}</span>
            <div class="dot--red" style="margin: 0 5px"></div>
          </label>
        </div>
        <base-button
          v-if="isOverview"
          :disabled="isComplete"
          :icon="isComplete ? undefined : 'calendar'"
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
          :selected-time="completedOnTime"
          @change-time="handleChangeCompletedTime"
          :disabled="viewOnly || isComplete"
        ></hour-picker>
      </div>
      <div class="form-item align-top">
        <label style="margin-top: 8px">{{ resources["image_upload"] }}</label>
        <div style="flex: 1">
          <div v-if="isOverview" class="op-upload-button-wrap">
            <base-button level="secondary">
              {{ resources["upload_image"] }}
              <img class="ml-1" src="/images/icon/upload-blue.svg" alt=""
            /></base-button>
            <input
              type="file"
              ref="fourthFileupload"
              id="fourthFileupload"
              accept=".gif, .jpg, .jpeg, .png"
              @click="resetFileEvent"
              @change="selectedFourthFiles"
              multiple
              @delete-img="deleteUploadedFile"
              style="height: 40px; width: 100%"
            />
          </div>
          <preview-images-system
            :is-viewing="!isOverview"
            @delete-img="deleteFourthFiles"
            :images="fourthFiles"
          ></preview-images-system>
        </div>
      </div>
      <div v-if="'REFURBISHMENT' === form.orderType" class="form-item">
        <label>{{ resources["tooling_id"] }}</label>
        <div v-if="form.assets">
          <base-input
            style="width: 200px"
            :readonly="true"
            :value="getDisplayToolingID"
          ></base-input>
        </div>
      </div>
      <div v-if="form.orderType === 'REFURBISHMENT'" class="form-item">
        <label class="">
          {{ resources["forecasted_max_shot"] }}
          <span
            class="dot--red"
            style="margin: 0 5px; display: inline-flex"
          ></span>
          <a-tooltip placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px; text-align: left">
                {{ resources["forecasted_max_shots_message"] }}
              </div>
            </template>
            <span style="margin-top: -7px">
              <a-icon type="info-circle" />
            </span>
          </a-tooltip>
        </label>
        <base-input
          v-if="isOverview"
          width="200px"
          :placeholder="isComplete ? '' : 'Number of shots'"
          v-model="form.estimatedExtendedLifeShot"
        >
        </base-input>
        <base-input
          v-else
          width="200px"
          :readonly="isComplete"
          :placeholder="isComplete ? '' : 'Number of shots'"
          :value="formatNumber(form.estimatedExtendedLifeShot)"
        >
        </base-input>
      </div>
      <div v-else class="form-item">
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
      <div class="form-item">
        <label>{{ resources["plant_s"] }}</label>
        <div>{{ listPlants }}</div>
      </div>
      <div v-if="isComplete" class="form-item">
        <label>{{ resources["checklist"] }}</label>
        <div v-if="!selectedCheckList?.id && viewOnly">
          <span style="color: #4b4b4b; font-weight: 300">{{
            resources["not_required"]
          }}</span>
        </div>
        <div v-else>
          <span class="icon-check"></span>
          <base-button icon="popup" @click="handleReviewChecklist">
            {{ resources["review_checklist"] }}</base-button
          >
        </div>
      </div>
      <div v-if="isComplete && form.pickingList" class="form-item">
        <label>{{ resources["pick_list"] }}</label>
        <div v-if="!selectedPickList?.id && viewOnly">
          <span style="color: #4b4b4b; font-weight: 300">Not Required</span>
        </div>
        <div v-else>
          <span class="icon-check"></span>
          <base-button icon="popup" @click="handleReviewPicklist">{{
            resources["review_picklist"]
          }}</base-button>
        </div>
      </div>
      <template v-if="form.costs">
        <div
          v-for="(item, index) in form.costs"
          :key="index"
          class="form-item-wrap"
        >
          <div class="form-item">
            <div>
              <label class="d-flex align-center">
                <span>{{ resources["cost_usd"] }}</span>
                <div
                  v-show="form.cost && index === 0"
                  class="dot--red"
                  style="margin: 0 5px"
                ></div>
              </label>
            </div>
            <base-input
              :is-number="true"
              :readonly="isComplete"
              :value="form.costs[index].value"
              width="130px"
              placeholder="Enter Cost"
              @input="(value) => handleCostInput(index, value)"
            ></base-input>
          </div>
          <div class="form-item form-item__details" style="margin-left: 20px">
            <label class="d-flex align-center" style="margin-top: 6px">
              <span>{{ resources["details"] }}</span>
              <div
                v-show="form.cost && index === 0"
                class="dot--red"
                style="margin: 0 5px"
              ></div>
            </label>
            <base-input
              full-width
              :readonly="isComplete"
              :value="form.costs[index].details"
              placeholder="Enter details of cost"
              style="width: 100%; resize: none"
              @input="(value) => handleCostDetailInput(index, value)"
            ></base-input>
          </div>
        </div>
        <div
          v-show="isOverview && form.cost && form.costs.length && !validCosts"
          style="margin: -8px 8px 8px 125px"
        >
          <i
            class="text--red"
            v-text="resources['cost_information_required_message']"
          ></i>
        </div>
      </template>
      <div v-if="isOverview" :class="{ 'add-cost-item': form.costs.length }">
        <label v-show="!form.costs.length" style="margin-right: 40px">{{
          resources["cost_usd"]
        }}</label>
        <base-button
          v-if="form.costs.length > 0 && shouldShowAddMoreCost"
          @click="handleAddCost"
          >+ {{ resources["add_more_costs"] }}</base-button
        >
        <base-button v-else-if="form.costs.length <= 0" @click="handleAddCost"
          >+ {{ resources["add_cost_information"] }}</base-button
        >
      </div>
      <!-- <div v-if="isOverview" class="form-item">
                <label>{{ resources['request_s'] }}</label>
                <base-input type="checkbox" v-model="approvalProcess"></base-input>approval_process
            </div> -->
      <div class="form-item form-item__upload">
        <label v-if="isInspectionCase">{{
          resources["just_file_upload"]
        }}</label>
        <label v-else>{{ resources["cost_file_upload"] }}</label>
        <base-button
          v-show="isOverview"
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
          <template v-if="isOverview">
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
          <div v-if="isComplete && !formAttachments.length">
            {{ resources["not_included"] }}
          </div>
        </div>
        <base-button
          v-if="formAttachments.length"
          icon="popup"
          @click="handleReviewDocument"
          >{{ resources["review_document"] }}</base-button
        >
      </div>
      <div
        v-if="['REFURBISHMENT', 'DISPOSAL'].includes(form.orderType)"
        class="form-item"
        style="align-items: flex-start"
      >
        <label>{{ resources["request_s"] }}</label>
        <template v-if="step === 'overview'">
          <div>
            <div
              class="mb-1 d-flex align-items-center"
              style="margin-top: -4px"
            >
              <input
                v-model="form.requestApproval"
                type="checkbox"
                class="mr-2"
              />
              <span>{{ resources["approval_process"] }}</span>
            </div>
            <i>*{{ resources["complete_wo_request_message"] }}.</i>
          </div>
        </template>
        <template v-else-if="step === 'complete'">
          <div>
            <div
              class="mb-1 d-flex align-items-center"
              style="margin-top: -4px"
            >
              <span>{{
                form.requestApproval
                  ? "Approval Process Required"
                  : "Approval Process Not Required"
              }}</span>
            </div>
          </div>
        </template>
      </div>
      <div class="form-item form-item__note align-top">
        <label style="margin-top: 8px">{{ resources["note"] }}</label>
        <div style="flex: 1">
          <textarea
            :readonly="isComplete || viewOnly"
            style="resize: none"
            rows="7"
            placeholder="Enter any notes here"
            v-model="form.note"
          ></textarea>
        </div>
      </div>
    </div>
    <div class="form-item form-item__action">
      <base-button
        v-if="!viewOnly && form.status !== 'COMPLETED'"
        style="margin-right: 8px"
        level="secondary"
        type="save"
        :disabled="isLoading"
        @click="handleSaveDraft"
        >{{ resources["save_progress"] }}</base-button
      >
      <base-button
        v-if="isOverview && form.status !== 'COMPLETED'"
        :disabled="!canProceedToCheckList"
        level="primary"
        @click="handleProceedToChecklist"
      >
        {{ resources["proceed_to_checklist"] }}
      </base-button>
      <base-button
        v-if="isComplete && !viewOnly && form.status !== 'COMPLETED'"
        level="primary"
        :disabled="isLoading"
        @click="handleSubmit"
        >{{ resources["complete_work_order"] }}</base-button
      >
    </div>
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
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    visible: Boolean,
    step: {
      type: String,
      default: "overview",
      validator(val) {
        return ["overview", "complete"].includes(val);
      },
    },
    resources: {
      type: Object,
      default: () => ({}),
    },
    form: Object,
    formAttachments: Array,
    getStartedOnTitle: [Function, Object, String, Number],
    getCompletedOnTitle: [Function, Object, String, Number],
    startedOnTime: Object,
    completedOnTime: Object,
    listPlants: String,
    fourthFiles: { type: Array, default: () => [] },
    isLoading: { type: Boolean, default: false },
    handleRemoveAsset: { type: Function, default: () => {} },
    handleShowStartedDatePicker: { type: Function, default: () => {} },
    handleShowCompletedDatePicker: { type: Function, default: () => {} },
    handleChangeStartedTime: { type: Function, default: () => {} },
    handleChangeCompletedTime: { type: Function, default: () => {} },
    handleAddCost: { type: Function, default: () => {} },
    handleUploadDocument: { type: Function, default: () => {} },
    handleReviewDocument: { type: Function, default: () => {} },
    handleRemoveDocument: { type: Function, default: () => {} },
    handleProceedToChecklist: { type: Function, default: () => {} },
    handleReviewChecklist: { type: Function, default: () => {} },
    handleReviewPicklist: { type: Function, default: () => {} },
    selectedFourthFiles: { type: Function, default: () => {} },
    deleteFourthFiles: { type: Function, default: () => {} },
    handleComplete: { type: Function, default: () => {} },
    viewOnly: { type: Boolean, default: () => false },
    selectedPickList: { type: Object, default: () => {} },
    selectedCheckList: { type: Object, default: () => {} },
    checklistFile: { type: Array, default: () => [] },
  },
  setup(props, ctx) {
    const approvalProcess = ref(false);
    const tooltipStyle = ref({ width: "300px" });

    const firstFillCostField = ref(null);

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
    const getDisplayToolingID = computed(() => {
      if (props.form.assets && props.form.assets[0]?.name) {
        return props.form.assets[0]?.name;
      }
      return "";
    });
    const canProceedToCheckList = computed(() => {
      if ("REFURBISHMENT" === props.form.orderType) {
        if (!props.form.estimatedExtendedLifeShot) {
          return false;
        }
      }
      if (!props?.form?.cost) {
        return true;
      }
      const hasValidCost = props.form.costs.find(
        (item) => !!item.value && !!item.details
      );
      return hasValidCost;
    });

    const validCosts = computed(() => {
      if (!props?.form?.cost) {
        return true;
      }
      const hasOneValidCost = props.form.costs.find(
        (item) => !!item.value && !!item.details
      );
      return hasOneValidCost;
    });

    const shouldShowAddMoreCost = computed(() => {
      return !_.isEmpty(
        (props.form.costs || []).find((item) => !!item.value && !!item.details)
      );
    });

    const accumulatedShot = computed(() => props.form?.accumulatedShot);

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

    const getAccumulatedShotFormat = computed(() => {
      const accumulatedShots = props.form.accumulatedShot;
      console.log("getAccumulatedShotFormat", accumulatedShots);
      return Common.formatNumber(accumulatedShots);
    });

    const handleCostInput = (index, value) => {
      const newItem = { ...props.form.costs[index], value: value };
      Vue.set(props.form.costs, index, newItem);
    };
    const handleCostDetailInput = (index, value) => {
      const newItem = { ...props.form.costs[index], details: value };
      Vue.set(props.form.costs, index, newItem);
    };

    const handleViewChecklistDocument = () => {
      Common.downloadDocument(props.checklistFile);
    };

    const handleSubmit = () => {
      props.handleComplete();
    };

    const resetFileEvent = () => {
      const target = ctx.refs?.fourthFileupload;
      if (target) {
        target.value = null;
      }
    };

    const deleteUploadedFile = (index) => {
      console.log("deleteUploadedFile", index);
      this.deleteFourthFiles(index);
    };

    const handleSaveDraft = () => {
      ctx.emit("save-draft");
    };

    const getFailureTime = computed(() => {
      const failureTime = Common.convertTimestampToDate(props.form.failureTime);
      const formattedDate = moment(failureTime).format("YYYY-MM-DD");
      const formattedTime = moment(failureTime).format("hh:mm");
      return {
        date: formattedDate,
        time: formattedTime,
      };
    });

    watchEffect(() => isComplete.value);

    watch(
      () => props.visible,
      (newVal) => {
        if (!newVal) {
          ctx.root.$nextTick(() => {
            ctx.refs.inputRef.value = null;
          });
        }
      }
    );

    // watchEffect(() => console.log("props.form.costs change", props.form.costs));
    watchEffect(() => console.log("props.form", props.form));

    return {
      approvalProcess,
      tooltipStyle,
      isOverview,
      isComplete,
      isGeneralCase,
      isInspectionCase,
      isEmergencyCase,
      isPMCase,
      isCMCase,
      startedOnTimeFormatted,
      completedOnTimeFormatted,
      getDisplayToolingID,
      getAccumulatedShotFormat,
      getFailureTime,
      validCosts,
      canProceedToCheckList,
      handleCostInput,
      handleCostDetailInput,
      deleteUploadedFile,
      handleClickUpload,
      handleSubmit,
      handleViewChecklistDocument,
      resetFileEvent,
      handleSaveDraft,
      accumulatedShot,
      shouldShowAddMoreCost,
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
.add-cost-item {
  margin-left: 125px;
  margin-top: -10px;
  margin-bottom: 15px;
}
</style>
