<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg custom-fade-modal"
    style="z-index: 5002"
    body-classes="custom-modal__body"
    @close="handleShowConfirm"
  >
    <div class="modal__container">
      <div>
        <div class="d-flex justify-space-between">
          <div class="back-icon" @click="handleShowConfirm">
            <span class="icon back-arrow"></span>
            <span class="back-icon__text text--blue">
              {{ resources["back_to_previous_page"] }}
            </span>
          </div>
          <div v-if="listItem.length > 1" :key="counterKey">
            <span>{{ resources["total"] }}</span>
            <span>{{ `${countCompleted}/${listItem.length}` }}</span>
            <span>{{ resources["completed"] }}</span>
          </div>
        </div>
        <div
          v-show="isTotalLoading"
          class="loading-wave"
          style="height: 48px; margin: 20px 30px"
        ></div>
        <div v-show="!isTotalLoading" :key="carouselKey">
          <div v-if="listItem.length > 1" style="margin: 20px 30px">
            <item-carousel
              :resources="resources"
              :list-item="listItem"
              :type="type.toLowerCase()"
              :selected-item-id="selectedItem.id"
              @select-item="handleChangeItem"
            ></item-carousel>
          </div>
          <div v-else-if="listItem.length === 1" style="margin: 20px 30px">
            <strong
              >{{
                resources[
                  `${type === "MOLD" ? "tooling" : type.toLowerCase()}_id`
                ]
              }}:</strong
            >
            <span>{{ listItem[0].code }}</span>
          </div>
        </div>
        <div
          v-show="isTotalLoading"
          class="loading-wave"
          style="height: 500px; margin-bottom: 16px"
        ></div>
        <div v-show="!isTotalLoading">
          <form
            ref="completion-form"
            class="needs-validation"
            style="border: none"
            @submit.prevent="submit"
          >
            <company-form
              v-if="type === 'COMPANY'"
              :resources="resources"
              :codes="codes"
              :company="selectedCompany"
              :is-loading="isLoading"
            ></company-form>
            <plant-form
              v-else-if="type === 'PLANT'"
              :resources="resources"
              :codes="codes"
              :location="selectedPlant"
              :is-loading="isLoading"
            ></plant-form>
            <machine-form
              v-else-if="type === 'MACHINE'"
              :resources="resources"
              :codes="codes"
              :machine="selectedMachine"
              :custom-field-list="customFieldList"
              :is-loading="isLoading"
            ></machine-form>
            <part-form
              v-else-if="type === 'PART'"
              :resources="resources"
              :codes="codes"
              :categories="categories"
              :part="selectedPart"
              :custom-field-list="customFieldList"
              :third-files="partFiles"
              :part-picture-files="partUploadedFile"
              :is-loading="isLoading"
            ></part-form>
            <mold-form
              v-else-if="type === 'MOLD'"
              :resources="resources"
              :codes="codes"
              :mold="selectedMold"
              :custom-field-list="customFieldList"
              :third-files="moldThirdFiles"
              :second-files="moldSecondFiles"
              :files="moldFiles"
              :checked-list="checkedList"
              :engineer-index="engineerIndex"
              :reason-data="reasonData"
              :document-files="documentFiles"
              :tooling-condition-files="toolingConditionFiles"
              :tooling-picture-file="toolingPictureFile"
              :is-loading="isLoading"
            ></mold-form>
            <div class="d-flex justify-end">
              <!-- <base-button level="primary" type="save" @click.prevent="submit">{{
                                resources['save_changes']
                            }}</base-button> -->
              <button
                ref="completion-button"
                type="submit"
                class="btn btn-success"
              >
                <div class="d-flex align-center">
                  <div v-show="isLoadingComplete" style="margin-right: 4px">
                    <div class="loading-spinner"></div>
                  </div>
                  <div
                    ref="white-check-icon"
                    class="d-none"
                    style="margin-right: 4px"
                  >
                    <div class="white-check-icon"></div>
                  </div>
                  <span>{{ resources["save"] }}</span>
                </div>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <warning-modal
      :visible="showConfirmChangeItem"
      dialog-classes="dialog-sm warning-modal-positon"
      @close="showConfirmChangeItem = false"
    >
      <div class="d-flex align-center">
        <div
          class="complete-icon-filled"
          style="height: 16px; width: 15px; margin-right: 4px"
        ></div>
        <strong> {{ resources["save_your_data_input"] }}? </strong>
      </div>
      <div>
        {{
          resources[
            "save_your_input_before_changing_tabs_you_can_submit_upon_completing_all_required_forms"
          ]
        }}.
      </div>
      <div class="d-flex justify-end" style="margin-top: 42px">
        <base-button
          level="secondary"
          @click="showConfirmChangeItem = false"
          style="margin-right: 8px"
          >{{ resources["stay_on_page"] }}</base-button
        >
        <base-button level="primary" @click="submitFormToChangeItem"
          >{{ resources["save_and_change_tab"] }}
        </base-button>
      </div>
    </warning-modal>
    <warning-modal
      :visible="showValidWarning"
      dialog-classes="dialog-sm warning-modal-positon"
      @close="showValidWarning = false"
    >
      <div class="d-flex align-center">
        <div
          class="red-warning-icon"
          style="height: 18px; width: 18px; margin-right: 4px"
        ></div>
        <strong> {{ resources["invalid_data_can_t_be_saved"] }}? </strong>
      </div>
      <div>
        {{
          resources[
            "some_of_your_data_input_is_invalid_fix_your_input_to_save_the_progress"
          ]
        }}.
      </div>
      <div class="d-flex align-items-center my-2">
        <input
          v-model="isDontShowWarningAgain"
          style="width: 10px; height: 10px"
          type="checkbox"
        />
        <span
          class="check-box-title"
          style="margin-left: 7px"
          v-text="resources['dont_show_warning_again']"
        ></span>
      </div>
      <div class="d-flex justify-end" style="margin-top: 42px">
        <base-button
          level="secondary"
          type="cancel"
          @click="showValidWarning = false"
          style="margin-right: 8px"
        >
          {{ resources["stay_on_page"] }}
        </base-button>
        <base-button
          level="primary"
          type="reject"
          @click="handleDiscardAndChangeItem"
          >{{ resources["leave_anyway"] }}
        </base-button>
      </div>
    </warning-modal>
    <warning-modal
      :visible="showConfirm"
      dialog-classes="dialog-sm warning-modal-positon"
      @close="showConfirm = false"
    >
      <div class="d-flex align-center">
        <div
          class="red-warning-icon"
          style="height: 18px; width: 18px; margin-right: 4px"
        ></div>
        <strong> {{ resources["you_haven_t_saved_your_progress"] }}? </strong>
      </div>
      <div>
        {{
          resources[
            "save_your_progress_to_work_on_it_later_You_can_submit_upon_completing_all_required_forms"
          ]
        }}.
      </div>
      <div class="d-flex justify-end" style="margin-top: 42px">
        <base-button
          level="secondary"
          @click="handleCloseModal"
          style="margin-right: 8px"
          >{{ resources["leave_anyway"] }}</base-button
        >
        <base-button level="primary" @click="showConfirm = false"
          >{{ resources["stay_on_page"] }}
        </base-button>
      </div>
    </warning-modal>
  </base-dialog>
</template>

<script>
const MOCK_MOLD = {
  counterId: "",
  counterCode: "",
  //new feature
  toolingComplexity: "",
  toolingLetter: "",
  weightRunner: "",
  hotRunnerDrop: "",
  hotRunnerZone: "",
  quotedMachineTonnage: "",
  // currentMachineTonnage: '',
  maxCapacityPerWeek: "0",
  // shotSize: '0',
  // size old
  sizeWidth: "",
  sizeHeight: "",
  sizeDepth: "",
  injectionMachineId: "",
  totalCavities: "",
  cycleTimeLimit2Unit: null,
  cycleTimeLimit1Unit: null,
  assetNumber: "",
  equipmentCode: "",
  equipmentStatus: "AVAILABLE",

  name: "",
  toolingType: "",
  lifeYears: "",
  madeYear: "",

  // 'familyTool': false,
  lastShot: "",
  warrantedShot: "",

  designedShot: "",
  approvedCycleTime: "",
  contractedCycleTime: "",
  toolmakerApprovedCycleTime: "",
  toolmakerContractedCycleTime: "",
  scrapRate: "",

  toolingCondition: null,
  preventCycle: "50000",
  preventUpcoming: "",
  preventOverdue: "0",
  cycleTimeLimit1: "",
  cycleTimeLimit2: "",

  upperLimitTemperature: "",
  upperLimitTemperatureUnit: null,
  lowerLimitTemperature: "",
  lowerLimitTemperatureUnit: null,
  engineerIds: [],
  engineerDate: "",

  size: "",
  sizeUnit: "MM",
  weight: "",
  weightUnit: "KILOGRAMS",
  runnerType: null,
  runnerMaker: "",
  maker: "",

  toolMakerCompanyId: "",
  toolMakerCompanyName: "",

  supplierCompanyId: "",
  supplierCompanyName: "",

  supplierForToolMaker: "",

  useageType: null,
  resin: "",
  cost: "",
  accumulatedMaintenanceCost: "0",
  accumulatedMaintenanceCostView: "$0",
  costCurrencyType: "USD",
  salvageValue: "",
  salvageCurrency: "USD",
  memo: "",

  // supplier
  uptimeTarget: "90",
  uptimeLimitL1: "",
  uptimeLimitL2: "",
  labour: "",
  hourPerShift: "",
  shiftsPerDay: "24",
  productionDays: "7",
  productionWeeks: "",
  supplierTagId: "",

  toolDescription: "",
  locationId: "",
  location: { name: "" },

  authorities: [],
  parts: [
    {
      moldId: "",
      partId: "",
      partName: "",
      cavity: "",
      totalCavities: "",
    },
  ],
  moldParts: [
    {
      moldId: "",
      partId: "",
      partName: "",
      cavity: "",
      totalCavities: "",
    },
  ],
  WLH: "",
};
const MOCK_COMPANY = {
  companyType: "",
  companyCode: "",
  name: "",
  address: "",
  manager: "",
  phone: "",
  email: "",
  memo: "",
  enabled: true,
};
const MOCK_MACHINE = {
  machineCode: "",
  locationId: "",
  locationName: "",
  locationCode: "",
  companyId: "",
  companyName: "",
  countryCode: "",
  line: "",
  machineMaker: "",
  machineType: "",
  machineModel: "",
  machineTonnage: "",
  enabled: true,
};
const MOCK_PLANT = {
  locationType: "",
  locationCode: "",
  name: "",
  address: "",
  memo: "",
  companyId: "",
  companyName: "",
  countryCode: "",
  timeZoneId: "",
  enabled: true,
};
const MOCK_PART = {
  sizeWidth: "",
  sizeHeight: "",
  sizeDepth: "",
  name: "",
  partCode: "",
  resinCode: "",
  resinGrade: "",
  designRevision: "",
  size: "",
  sizeUnit: "MM",
  weight: "",
  weightUnit: "GRAMS",
  price: "",
  currencyType: "USD",
  downstreamSite: "",
  memo: "",
  enabled: true,
  categoryId: "",
  category: {},
  weeklyDemand: null,
  project: {
    name: "",
    id: null,
  },
  isSubmitting: false,
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
    type: {
      type: String,
      default: () => "",
    },
    selectedDataRequest: {
      type: Object,
      default: () => ({}),
    },
    modalType: {
      type: String,
      default: "COMPLETE",
    },
  },
  components: {
    "item-carousel": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/item-carousel.vue"
    ),
    "company-form": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/company-form.vue"
    ),
    "plant-form": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/plant-form.vue"
    ),
    "machine-form": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/machine-form.vue"
    ),
    "part-form": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/part-form.vue"
    ),
    "mold-form": httpVueLoader(
      "/components/data-requests/request-modal/data-entry/data-entry-form/mold-form.vue"
    ),
    "warning-modal": httpVueLoader(
      "/components/@base/dialog/warning-dialog.vue"
    ),
  },
  setup(props, ctx) {
    // STATE //
    const listItem = ref([]);
    const codes = computed(() => headerVm?.systemCodes);
    const selectedItem = ref({});

    const showConfirm = ref(false);
    const showConfirmChangeItem = ref(false);
    const showValidWarning = ref(false);

    const selectedCompany = ref({});
    const selectedPlant = ref({});
    const selectedMold = ref({});
    const selectedMachine = ref({});
    const selectedPart = ref({});
    const isLoading = ref(false);
    const isTotalLoading = ref(false);

    const customFieldList = ref([]);

    const partFiles = ref([]);
    const partUploadedFile = ref([]);

    const reasonData = ref({});
    const engineerIndex = ref([]);
    const checkedList = ref([]);
    const moldFiles = ref([]);
    const moldThirdFiles = ref([]);
    const moldSecondFiles = ref([]);
    const documentFiles = ref([]);
    const toolingConditionFiles = ref([]);
    const toolingPictureFile = ref([]);

    const categories = ref([]);

    const tempItem = ref({});

    const carouselKey = ref(0);
    const flag = ref(0);
    const counterKey = ref(0);
    const shouldShowWarning = ref(true);
    const isDontShowWarningAgain = ref(false);
    const isLoadingComplete = ref(false);

    // COMPUTED //
    const getModalTitle = computed(() => {
      return `${props.resources["request_id"]}: ${props.selectedDataRequest.requestId}`;
    });

    const countCompleted = computed(() => {
      if (listItem.value.length > 0) {
        const finds = listItem.value.filter((item) => item.completed);
        console.log("countCompleted", finds, listItem.value);
        return finds.length;
      }
      return 0;
    });

    const getParams = computed(() => {
      const param = {
        id: "",
        type: "",
      };
      switch (props.type) {
        case "COMPANY":
          param.id = selectedCompany.value?.id;
          param.type = "COMPANY";
          break;
        case "PLANT":
          param.id = selectedPlant.value?.id;
          param.type = "LOCATION";
          break;
        case "MOLD":
          param.id = selectedMold.value?.id;
          param.type = "TOOLING";
          break;
        case "MACHINE":
          param.id = selectedMachine.value?.id;
          param.type = "MACHINE";
          break;
        case "PART":
          param.id = selectedPart.value?.id;
          param.type = "PART";
          break;
      }
      return param;
    });

    // API //
    const loadShowWarningAgain = async () => {
      try {
        const response = await axios.get(
          `/api/on-boarding/get?feature=DATA_REQUEST_INVALID_CHECK`
        );
        shouldShowWarning.value = response.data.data.seen === false;
      } catch (error) {
        console.log(error);
      }
    };

    const loadCategories = async () => {
      try {
        const __response = await headerVm?.getListCategories();
        console.log(__response);
        categories.value = __response;
      } catch (error) {
        console.log(error);
      }
    };

    // WATCH //
    watch(
      () => props.visible,
      async (newVal) => {
        if (newVal) {
          await getListItem();
          loadShowWarningAgain();
          loadCategories();
          const baseLengh = listItem.value.length;
          console.log(
            "watch visible before",
            newVal,
            baseLengh,
            props.selectedDataRequest
          );
          switch (props.type) {
            case "COMPANY":
              if (baseLengh < props.selectedDataRequest.companyNumber) {
                const fillNumber =
                  props.selectedDataRequest.companyNumber -
                  listItem.value.length;
                for (let index = 0; index < fillNumber; index++) {
                  listItem.value.push({
                    ...MOCK_COMPANY,
                    ...{ id: `mock-${index}` },
                  });
                }
              }
              selectedItem.value = listItem.value[0];
              if (baseLengh > 0) {
                await handleSelectCompany(selectedItem.value.id);
              } else {
                selectedCompany.value = selectedItem.value;
              }
              break;
            case "PLANT":
              if (baseLengh < props.selectedDataRequest.locationNumber) {
                const fillNumber =
                  props.selectedDataRequest.locationNumber -
                  listItem.value.length;
                for (let index = 0; index < fillNumber; index++) {
                  listItem.value.push({
                    ...MOCK_PLANT,
                    ...{ id: `mock-${index}` },
                  });
                }
              }
              selectedItem.value = listItem.value[0];
              if (baseLengh > 0) {
                await handleSelectPlant(selectedItem.value.id);
              } else {
                selectedPlant.value = selectedItem.value;
              }
              break;
            case "MOLD":
              if (baseLengh < props.selectedDataRequest.moldNumber) {
                const fillNumber =
                  props.selectedDataRequest.moldNumber - listItem.value.length;
                for (let index = 0; index < fillNumber; index++) {
                  listItem.value.push({
                    ...MOCK_MOLD,
                    ...{ id: `mock-${index}` },
                  });
                }
              }
              selectedItem.value = listItem.value[0];
              if (baseLengh > 0) {
                await handleSelectMold(selectedItem.value.id);
              } else {
                selectedMold.value = selectedItem.value;
              }
              break;
            case "MACHINE":
              if (baseLengh < props.selectedDataRequest.machineNumber) {
                const fillNumber =
                  props.selectedDataRequest.machineNumber -
                  listItem.value.length;
                for (let index = 0; index < fillNumber; index++) {
                  listItem.value.push({
                    ...MOCK_MACHINE,
                    ...{ id: `mock-${index}` },
                  });
                }
              }
              selectedItem.value = listItem.value[0];
              if (baseLengh > 0) {
                await handleSelectMachine(selectedItem.value.id);
              } else {
                selectedMachine.value = selectedItem.value;
              }
              break;
            case "PART":
              if (baseLengh < props.selectedDataRequest.partNumber) {
                const fillNumber =
                  props.selectedDataRequest.partNumber - listItem.value.length;
                for (let index = 0; index < fillNumber; index++) {
                  listItem.value.push({
                    ...MOCK_PART,
                    ...{ id: `mock-${index}` },
                  });
                }
              }
              selectedItem.value = listItem.value[0];
              if (baseLengh > 0) {
                await handleSelectPart(selectedItem.value.id);
              } else {
                selectedPart.value = selectedItem.value;
              }
              break;
          }
          if (baseLengh > 0) {
            await getCustomFieldList();
          }
          carouselKey.value++;
          console.log("watch visible", carouselKey.value, listItem.value);
        } else {
          clearData();
        }
      }
    );

    watch(
      () => [
        selectedCompany.value,
        selectedPlant.value,
        selectedMold.value,
        selectedMachine.value,
        selectedPart.value,
      ],
      () => {
        flag.value++;
      },
      { deep: true }
    );

    watch(
      () => partFiles.value,
      (newVal) => {
        console.log("change partFiles", newVal);
      },
      { deep: true }
    );

    // METHODS //
    const handleShowConfirm = () => {
      if (flag.value > 1) {
        showConfirm.value = true;
      } else {
        ctx.emit("close");
      }
    };

    const handleCloseModal = () => {
      showConfirm.value = false;
      ctx.emit("close");
    };

    const clearData = () => {
      listItem.value = [];
      selectedItem.value = {};

      showConfirm.value = false;
      showValidWarning.value = false;
      showConfirmChangeItem.value = false;

      selectedCompany.value = {};
      selectedPlant.value = {};
      selectedMold.value = {};
      selectedMachine.value = {};
      selectedPart.value = {};
      customFieldList.value = [];
      partFiles.value = [];
      partUploadedFile.value = [];
      reasonData.value = {};
      engineerIndex.value = [];
      checkedList.value = [];
      moldFiles.value = [];
      moldThirdFiles.value = [];
      moldSecondFiles.value = [];
      documentFiles.value = [];
      toolingConditionFiles.value = [];
      toolingPictureFile.value = [];
      flag.value = 0;
    };

    const handleSelectItem = async (item) => {
      console.log("handleSelectItem", item);
      selectedItem.value = item;
      if (!`${selectedItem.value.id}`.includes("mock")) {
        switch (props.type) {
          case "COMPANY":
            await handleSelectCompany(item.id);
            break;
          case "PLANT":
            await handleSelectPlant(item.id);
            break;
          case "MOLD":
            await handleSelectMold(item.id);
            break;
          case "MACHINE":
            await handleSelectMachine(item.id);
            break;
          case "PART":
            await handleSelectPart(item.id);
            break;
        }
      } else {
        switch (props.type) {
          case "COMPANY":
            selectedCompany.value = item;
            break;
          case "PLANT":
            selectedPlant.value = item;
            break;
          case "MOLD":
            selectedMold.value = item;
            break;
          case "MACHINE":
            selectedMachine.value = item;
            break;
          case "PART":
            selectedPart.value = item;
            break;
        }
      }
      flag.value = 0;
    };

    const getListItem = async () => {
      try {
        isTotalLoading.value = true;
        const paramData = {
          query: "",
          objectType:
            props.type === "PLANT"
              ? "LOCATION"
              : props.type === "MOLD"
              ? "TOOLING"
              : props.type,
          dataRequestId: props.selectedDataRequest.id,
          size: 2000,
        };
        const param = Common.param(paramData);
        const res = await axios.get(`/api/data-completion?${param}`);
        listItem.value = res.data.data.content.map((item) => {
          const newItem = {
            id: item.data.id,
            code: "",
            completed: true,
          };
          if (props.selectedDataRequest.requestType === "DATA_COMPLETION") {
            newItem.completed = item.rate === 100;
          }
          switch (props.type) {
            case "COMPANY":
              newItem.code = item.data.companyCode;
              break;
            case "PLANT":
              newItem.code = item.data.locationCode;
              break;
            case "MOLD":
              newItem.code = item.data.equipmentCode;
              break;
            case "MACHINE":
              newItem.code = item.data.machineCode;
              break;
            case "PART":
              newItem.code = item.data.partCode;

              break;
          }
          return newItem;
        });
        isTotalLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectCompany = async (id) => {
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/companies/${id}`);
        selectedCompany.value = response.data;
        flag.value = 0;
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectPlant = async (id) => {
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/locations/${id}`);
        selectedPlant.value = response.data;
        flag.value = 0;
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectMold = async (id) => {
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/molds/${id}`);
        response.data.approvedCycleTime =
          response.data.contractedCycleTimeSeconds;
        selectedMold.value = response.data;
        if (selectedMold.value.dataSubmission === "DISAPPROVED") {
          const dataSubmissionRes = await axios.get(
            `/api/molds/data-submission/${selectedMold.value.id}/disapproval-reason`
          );
          reasonData.value = dataSubmissionRes.data;
          reasonData.value.approvedAt = reasonData.value.approvedAt
            ? moment.unix(reasonData.value).format("MMMM DD YYYY HH:mm:ss")
            : "-";
        }

        if (response.data.size) {
          const sizes = response.data.size.split("x");
          if (sizes.length >= 3) {
            selectedMold.value.sizeWidth = sizes[0].trim();
            selectedMold.value.sizeDepth = sizes[1].trim();
            selectedMold.value.sizeHeight = sizes[2].trim();
          }
        }
        selectedMold.value.costCurrencyType =
          selectedMold.value.costCurrencyType || "USD";
        selectedMold.value.accumulatedMaintenanceCost =
          selectedMold.value.accumulatedMaintenanceCost || 0;

        if (response.data.engineers && response.data.engineers.length > 0) {
          engineerIndex.value = response.data.engineers.map(
            (value) => value.id
          );
          selectedMold.value.engineerIds = response.data.engineers.map(
            (value) => value.id
          );
          engineerIndex.value.map((id) => {
            response.data.engineers.map((value) => {
              if (id === value.id) {
                checkedList.value.push(value);
              }
            });
          });
        } else {
          engineerIndex.value = [];
          selectedMold.value.engineerIds = [];
        }

        //uptimeTarget
        if (selectedMold.value.uptimeTarget == null) {
          selectedMold.value.uptimeTarget = 90;
        }

        //uptimeLimitL1
        if (selectedMold.value.uptimeLimitL1 == null) {
          selectedMold.value.uptimeLimitL1 = 5;
        }
        if (selectedMold.value.uptimeLimitL2 == null) {
          selectedMold.value.uptimeLimitL2 = 10;
        }

        var authorities = [];
        for (var i = 0; i < response.data.moldAuthorities.length; i++) {
          var moldAuthority = response.data.moldAuthorities[i];
          authorities.push(moldAuthority.authority);
        }
        selectedMold.value.authorities = authorities;

        if (selectedMold.value.counter != null) {
          selectedMold.value.counterId = selectedMold.value.counter.id;
          selectedMold.value.counterCode =
            selectedMold.value.counter.equipmentCode;
        }

        if (selectedMold.value.part != null) {
          selectedMold.value.partId = selectedMold.value.part.id;
          selectedMold.value.partName = selectedMold.value.part.name;
        }

        if (selectedMold.value.location != null) {
          selectedMold.value.locationId = selectedMold.value.location.id;
          selectedMold.value.locationName = selectedMold.value.location.name;
        }

        if (selectedMold.value.parts.length > 0) {
          selectedMold.value.parts = selectedMold.value.parts.map((value) => {
            value.nameShow = value.partName
              ? `${value.partCode} (${value.partName})`
              : value.partCode;
            return value;
          });
        } else {
          selectedMold.value.parts = [
            {
              moldId: "",
              partId: "",
              partName: "",
              cavity: "",
              totalCavities: "",
            },
          ];
        }
        console.log(
          "selectedMold.value.parts after update",
          selectedMold.value.parts
        );
        if (selectedMold.value.upperLimitTemperatureUnit == null) {
          selectedMold.value.upperLimitTemperatureUnit = "CELSIUS";
        }
        if (selectedMold.value.lowerLimitTemperatureUnit == null) {
          selectedMold.value.lowerLimitTemperatureUnit = "CELSIUS";
        }

        const param = {
          storageTypes:
            "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION",
          refId: selectedMold.value.id,
        };

        const moldFileStoragesRes = await axios.get(
          "/api/file-storage/mold?" + Common.param(param)
        );
        documentFiles.value = moldFileStoragesRes.data[
          "MOLD_MAINTENANCE_DOCUMENT"
        ]
          ? moldFileStoragesRes.data["MOLD_MAINTENANCE_DOCUMENT"]
          : [];
        toolingConditionFiles.value = moldFileStoragesRes.data["MOLD_CONDITION"]
          ? moldFileStoragesRes.data["MOLD_CONDITION"]
          : [];
        toolingPictureFile.value = moldFileStoragesRes.data[
          "MOLD_INSTRUCTION_VIDEO"
        ]
          ? moldFileStoragesRes.data["MOLD_INSTRUCTION_VIDEO"]
          : [];
        flag.value = 0;
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectMachine = async (id) => {
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/machines/${id}`);
        selectedMachine.value = response.data;
        flag.value = 0;
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const handleSelectPart = async (id) => {
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/parts/${id}`);
        selectedPart.value = response.data;
        if (response.data.size) {
          const sizes = response.data.size.split("x");
          if (sizes.length >= 3) {
            selectedPart.value.sizeWidth = sizes[0].trim();
            selectedPart.value.sizeDepth = sizes[1].trim();
            selectedPart.value.sizeHeight = sizes[2].trim();
          }
          if (selectedPart.value.partSizeNoUnit != null) {
            selectedPart.value.size = selectedPart.value.partSizeNoUnit;
          }
        }
        await loadPartFiles(id);
        flag.value = 0;
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const submit = async () => {
      const startTime = performance.now();
      isLoadingComplete.value = true;
      if (!`${selectedItem.value.id}`.includes("mock")) {
        switch (props.type) {
          case "COMPANY":
            console.log("submit", selectedCompany.value);
            await updateCompany();
            break;
          case "PLANT":
            console.log("submit", selectedPlant.value);
            await updatePlant();
            break;
          case "MOLD":
            console.log("submit", selectedMold.value);
            await updateMold();
            break;
          case "MACHINE":
            console.log("submit", selectedMachine.value);
            await updateMachine();
            break;
          case "PART":
            console.log("submit", selectedPart.value);
            await updatePart();
            break;
        }
        await updateCustomField();
        const getItemCompletionData = await getCompletedItem(
          selectedItem.value.id
        );
        const find = listItem.value.find(
          (item) => item.id === selectedItem.value.id
        );
        if (props.modalType === "CREATE") {
          find.completed = true;
        } else if (props.modalType === "COMPLETE") {
          find.completed = getItemCompletionData.rate === 100;
        }
      } else {
        let index = listItem.value.findIndex(
          (item) => item.id === selectedItem.value.id
        );
        let item = {};
        switch (props.type) {
          case "COMPANY":
            console.log("submit", selectedCompany.value);
            item = await createCompany();
            break;
          case "PLANT":
            console.log("submit", selectedPlant.value);
            item = await createPlant();
            break;
          case "MOLD":
            console.log("submit", selectedMold.value);
            item = await createMold();
            break;
          case "MACHINE":
            console.log("submit", selectedMachine.value);
            item = await createMachine();
            break;
          case "PART":
            console.log("submit", selectedPart.value);
            item = await createPart();
            break;
        }
        const getItemCompletionData = await getCompletedItem(item.id);
        const newValue = {
          id: getItemCompletionData.item.id,
          code: "",
          completed: true,
        };
        Vue.set(listItem.value, index, newValue);
        switch (props.type) {
          case "COMPANY":
            listItem.value[index].code = getItemCompletionData.item.companyCode;
            break;
          case "PLANT":
            listItem.value[index].code =
              getItemCompletionData.item.locationCode;
            break;
          case "MOLD":
            listItem.value[index].code =
              getItemCompletionData.item.equipmentCode;
            break;
          case "MACHINE":
            listItem.value[index].code = getItemCompletionData.item.machineCode;
            break;
          case "PART":
            listItem.value[index].code = getItemCompletionData.item.partCode;
            break;
        }
        handleSelectItem(listItem.value[index]);
        console.log(
          "submit find",
          index,
          listItem.value[index],
          listItem.value
        );
      }
      const endTime = performance.now();
      const duration = endTime - startTime;
      if (duration > 3000) {
        const remainTime = 3000 - duration;
        setTimeout(() => {
          isLoadingComplete.value = false;
        }, remainTime);
      } else {
        isLoadingComplete.value = false;
      }
      const whiteCheckIcon = ctx.refs["white-check-icon"];
      if (whiteCheckIcon) {
        whiteCheckIcon.classList.remove("d-none");
        setTimeout(() => {
          whiteCheckIcon.classList.add("d-none");
        }, 2000);
      }

      console.log("submit", duration);
      flag.value = 0;
      carouselKey.value++;
      counterKey.value++;
      const id = props.selectedDataRequest.id;
      ctx.emit("reload-detail", id);
    };

    const updateCompany = async () => {
      try {
        const company = { ...selectedCompany.value };
        const response = await axios.put(
          `/api/companies/${company.id}`,
          company
        );
        console.log("updateCompany", response);
      } catch (error) {
        Common.alert("Update company failed!");
      }
    };

    const updatePlant = async () => {
      try {
        const plant = { ...selectedPlant.value };
        const response = await axios.put(`/api/locations/${plant.id}`, plant);
        console.log("updatePlant", response);
      } catch (error) {
        Common.alert("Update plant failed!");
      }
    };
    const updateMold = async () => {
      try {
        const mold = { ...selectedMold.value };
        const payload = getMoldFormData();
        const response = await axios.put(`/api/molds/${mold.id}`, payload);
        console.log("updateMold", response);
      } catch (error) {
        Common.alert("Update mold failed!");
      }
    };
    const updateMachine = async () => {
      try {
        const machine = { ...selectedMachine.value };
        const response = await axios.put(
          `/api/machines/${machine.id}`,
          machine
        );
        console.log("updateMachine", response);
      } catch (error) {
        Common.alert("Update machine failed!");
      }
    };
    const updatePart = async () => {
      try {
        const part = { ...selectedPart.value };
        const payload = getPartFormData();
        const response = await axios.put(
          `/api/parts/edit-multipart/${part.id}`,
          payload
        );
        await uploadPartImage(part.id);
        console.log("updatePart", response);
      } catch (error) {
        Common.alert("Update part failed!");
      }
    };

    const createCompany = async () => {
      try {
        const company = { ...selectedCompany.value };
        delete company.id;
        company.dataRequestId = props.selectedDataRequest.id;
        const response = await axios.post(`/api/companies`, company);
        console.log("createCompany", response);
        return response.data.data;
      } catch (error) {
        Common.alert("Create company failed!");
      }
    };

    const createPlant = async () => {
      try {
        const plant = { ...selectedPlant.value };
        delete plant.id;
        plant.dataRequestId = props.selectedDataRequest.id;
        const response = await axios.post(`/api/locations`, plant);
        console.log("createPlant", response);
        return response.data.data;
      } catch (error) {
        Common.alert("Create plant failed!");
      }
    };
    const createMold = async () => {
      try {
        const mold = { ...selectedMold.value };
        const payload = getMoldFormData("CREATE");
        const response = await axios.post(`/api/molds`, payload);
        console.log("createMold", response);
        return response.data;
      } catch (error) {
        Common.alert("Create mold failed!");
      }
    };
    const createMachine = async () => {
      try {
        const machine = { ...selectedMachine.value };
        delete machine.id;
        machine.dataRequestId = props.selectedDataRequest.id;
        const response = await axios.post(`/api/machines`, machine);
        console.log("createMachine", response);
        return response.data;
      } catch (error) {
        Common.alert("Create machine failed!");
      }
    };
    const createPart = async () => {
      try {
        const part = { ...selectedPart.value };
        const payload = getPartFormData("CREATE");
        const response = await axios.post(`/api/parts/add-multipart`, payload);
        await uploadPartImage(response.data.id);
        return response.data;
      } catch (error) {
        Common.alert("Create part failed!");
      }
    };

    const getMoldFormData = (type) => {
      var formData = new FormData();
      for (var i = 0; i < moldFiles.value.length; i++) {
        let file = moldFiles.value[i];
        formData.append("files", file);
      }
      for (var i = 0; i < moldSecondFiles.value.length; i++) {
        let file = moldSecondFiles.value[i];
        formData.append("secondFiles", file);
      }
      for (var i = 0; i < moldThirdFiles.value.length; i++) {
        let file = moldThirdFiles.value[i];
        formData.append("thirdFiles", file);
      }
      const selectedMoldParam = { ...selectedMold.value };
      selectedMoldParam.poDate = selectedMoldParam.poDateFormat
        ? selectedMoldParam.poDateFormat
        : null;
      if (type === "CREATE") {
        selectedMoldParam.dataRequestId = props.selectedDataRequest.id;
        delete selectedMoldParam.id;
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      formData.append("payload", JSON.stringify(selectedMoldParam));
      return formData;
    };

    const getPartFormData = (type) => {
      var formData = new FormData();
      const selectedPartParam = { ...selectedPart.value };
      if (type === "CREATE") {
        selectedPartParam.dataRequestId = props.selectedDataRequest.id;
        delete selectedPartParam.id;
      }
      formData.append("payload", JSON.stringify(selectedPartParam));
      return formData;
    };

    const uploadPartImage = async (partId) => {
      const payload = getPartFilesFormData(partId);
      try {
        const res = await axios.post("/api/file-storage", payload);
        console.log("uploadPartImage", res);
      } catch (error) {
        console.log(error);
      }
    };

    const getPartFilesFormData = (partId) => {
      var formData = new FormData();
      partFiles.value.forEach((element) => {
        formData.append("files", element);
      });
      formData.append("storageType", "PART_PICTURE");
      formData.append("refId", partId);
      return formData;
    };

    const getCustomFieldList = async () => {
      const param = getParams.value;
      console.log("getCustomFieldList", param);
      try {
        isLoading.value = true;
        const response = await axios.get(
          `/api/custom-field-value/list-by-object?objectType=${param.type}&objectId=${param.id}`
        );
        customFieldList.value = response.data.map((item) => ({
          fieldName: item.fieldName,
          defaultInputValue:
            item.customFieldValueDTOList.length !== 0
              ? item.customFieldValueDTOList[0].value
              : null,
          id: item.id,
          required: item.required,
        }));
        isLoading.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const updateCustomField = async () => {
      const param = getParams.value;
      console.log("updateCustomField", param);
      try {
        if (customFieldList.value) {
          const customFieldListValue = customFieldList.value.map((item) => ({
            id: item.id,
            customFieldValueDTOList: [
              {
                value: item.defaultInputValue,
              },
            ],
          }));
          const payload = {
            customFieldDTOList: customFieldListValue,
          };
          const res = await axios.post(
            `/api/custom-field-value/edit-list/${param.id}`,
            payload
          );
          console.log(res);
        }
      } catch (error) {
        console.log(error);
      }
    };

    const loadPartFiles = async (id) => {
      try {
        const fileParam = {
          storageTypes: "PART_PICTURE",
          refId: id,
        };
        const fileRes = axios.get(
          "/api/file-storage/mold?" + Common.param(fileParam)
        );
        if (fileRes.data) {
          partUploadedFile.value = fileRes.data["PART_PICTURE"]
            ? fileRes.data["PART_PICTURE"]
            : [];
        }
      } catch (error) {
        console.log(error);
      }
    };

    const handleChangeItem = async (item) => {
      if (flag.value > 1) {
        tempItem.value = item;
        showConfirmChangeItem.value = true;
      } else {
        await handleSelectItem(item);
      }
    };

    const submitFormToChangeItem = async () => {
      showConfirmChangeItem.value = false;
      const el = ctx.refs["completion-form"];
      try {
        if (el) {
          const checkValidity = el.checkValidity();
          if (checkValidity) {
            await submit();
            await handleSelectItem(tempItem.value);
            tempItem.value = {};
          } else {
            console.log("check false");
            if (shouldShowWarning.value) {
              showValidWarning.value = true;
            } else {
              await handleSelectItem(tempItem.value);
              tempItem.value = {};
            }
          }
        }
      } catch (error) {
        console.log(error);
      }
    };

    const handleDiscardAndChangeItem = async () => {
      showValidWarning.value = false;
      await handleSelectItem(tempItem.value);
      if (isDontShowWarningAgain.value) {
        await dontShowAgain();
      }
      tempItem.value = {};
    };

    const dontShowAgain = () => {
      try {
        const payload = { feature: "DATA_REQUEST_INVALID_CHECK", seen: true };
        const res = axios.post(`/api/on-boarding/update`, payload);
        shouldShowWarning.value = false;
      } catch (error) {
        console.log(error);
      }
    };

    const getCompletedItem = async (id) => {
      try {
        const type =
          props.type === "MOLD"
            ? "TOOLING"
            : props.type === "PLANT"
            ? "LOCATION"
            : props.type;
        console.log("getCompletedItem", type, id);
        const params = {
          query: "",
          objectId: id,
          objectType: type,
        };
        const res = await axios.get(`/api/data-completion`, {
          params: params,
        });
        const rate = res.data.avgRate;

        return {
          item: res.data.data.content[0].data,
          rate: rate,
        };
      } catch (error) {
        console.log(error);
      }
    };

    return {
      codes,
      listItem,
      isLoading,
      isTotalLoading,
      showConfirm,
      showConfirmChangeItem,
      showValidWarning,
      customFieldList,
      selectedItem,
      selectedCompany,
      selectedPlant,
      selectedMold,
      selectedMachine,
      selectedPart,
      partFiles,
      partUploadedFile,
      carouselKey,
      counterKey,
      shouldShowWarning,
      isDontShowWarningAgain,
      isLoadingComplete,
      //
      checkedList,
      engineerIndex,
      reasonData,
      documentFiles,
      toolingConditionFiles,
      toolingPictureFile,
      //
      moldFiles,
      moldThirdFiles,
      moldSecondFiles,
      categories,
      getModalTitle,
      countCompleted,
      handleSelectItem,
      handleChangeItem,
      submit,
      handleCloseModal,
      submitFormToChangeItem,
      handleDiscardAndChangeItem,
      handleShowConfirm,
    };
  },
};
</script>

<style scoped>
.modal__container__header {
  position: absolute;
  left: 0;
  top: 0;
}

.modal__footer {
  margin-top: 100px;
  justify-content: flex-end;
}

.modal__footer__button {
  margin-left: 8px;
}

.custom-modal__body {
  padding: 20px 25px !important;
}

.modal__container__form__row {
  margin: 10px 0;
  display: flex;
}

.modal__container__form__row label {
  width: 110px;
}

.back-icon {
  display: inline-flex;
  align-items: center;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}

.custom-fade-modal {
  z-index: 5000;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.back-icon {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}

.no-data-table {
  height: 459px;
  width: 100%;
}

.form-control-warning {
  outline: 0;
  border: 1px solid #5d43f7 !important;
  box-shadow: 0 0 5px 5px rgba(93, 67, 247, 0.3);
}

/* .form-control:invalid,
.form-control:valid,
.was-validated {
    border: 1px solid #e4e7ea;
} */

.form-control:focus {
  border: 1px solid #8ad4ee !important;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgb(32, 168, 216, 0.25) !important;
}

.warning-modal-positon {
  transform: translate(0, 130%) !important;
}
</style>
