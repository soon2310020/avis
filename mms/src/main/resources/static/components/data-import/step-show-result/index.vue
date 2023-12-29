<template>
  <div class="upload-result">
    <div v-show="isProcessing" class="upload-result__processing">
      <img
        style="max-width: 2rem"
        src="/images/icon/spinner.gif"
        alt="loading"
      />
    </div>
    <div v-if="currentFileData">
      <div class="file-item__name">
        <span class="font-weight-bold">{{ resources["file_name"] }}</span
        >: {{ currentFileData.name || currentFileData.fileId }}
      </div>
    </div>
    <div v-show="!isProcessing" class="upload-result__overview">
      <div class="overview-title" style="margin-top: 16px">
        {{ capitalize(resources["result"]) }}
      </div>
      <div v-if="currentFileData" class="overview-detail">
        <span
          v-if="currentFileData.errorCount > 0"
          class="has-error"
          :key="currentFileData?.fileId"
          v-html="getErrorCountMessage()"
        ></span>
        <span v-else-if="noDataFound" class="no-data">{{
          resources["no_data_row_found_please_update_the_file_and_import_again"]
        }}</span>
        <span v-else-if="dataAlreadyExisted" class="has-error">
          {{
            resources[
              "data_already_exists_in_the_system_please_go_back_and_select_overwriting_existing_data_option"
            ]
          }}
        </span>
        <span v-else class="no-error">
          {{ resources["no_error_found_data_was_imported_success"] }}
        </span>
      </div>
    </div>
    <div
      v-if="currentFileData"
      class="upload-result__files"
      :class="{ 'multiple-file': hasMultipleFile }"
    >
      <span
        class="navigate-arrow navigate-arrow__back"
        :class="{ disabled: currentFileIdx < 1 }"
        @click="handleNavigateBetweenFiles('back')"
      ></span>
      <span
        class="navigate-arrow navigate-arrow__next"
        :class="{ disabled: currentFileIdx >= listValidateResults.length - 1 }"
        @click="handleNavigateBetweenFiles('next')"
      ></span>
      <div class="file-item">
        <div class="file-item__validation">
          <!-- Display in case error only -->
          <template v-if="dataAlreadyExisted">
            <div class="file-item__import-number">
              <span class="icon icon-success"></span>
              <div class="content">
                <span class="highlight">
                  {{ getPostCountMessage(null, currentFileData.postCount) }}
                </span>
              </div>
            </div>
            <div class="file-item__import-number">
              <span class="icon icon-success"></span>
              <div class="content">
                <span class="highlight">
                  {{ getPutCountMessage(null, currentFileData.putCount) }}
                </span>
              </div>
            </div>
          </template>
          <!-- Count the number of success import by category -->
          <template v-else>
            <template v-for="sheet in currentFileSuccess">
              <div v-show="sheet.postCount" class="file-item__import-number">
                <span class="icon icon-success"></span>
                <div class="content">
                  <span class="highlight">
                    <a
                      :href="
                        getHyperlinkByObjectType(sheet.sheetName, sheet.postIds)
                      "
                    >
                      {{
                        getUnitByObjectType(sheet.sheetName, sheet.postCount)
                      }}
                    </a>
                    {{
                      getPostCountMessage(sheet.sheetName, sheet.postCount)
                    }}</span
                  >
                </div>
              </div>
              <div v-show="sheet.putCount" class="file-item__import-number">
                <span class="icon icon-success"></span>
                <div class="content">
                  <span class="highlight">
                    <a
                      :href="
                        getHyperlinkByObjectType(sheet.sheetName, sheet.putIds)
                      "
                    >
                      {{ getUnitByObjectType(sheet.sheetName, sheet.putCount) }}
                    </a>
                    {{
                      getPutCountMessage(sheet.sheetName, sheet.putCount)
                    }}</span
                  >
                </div>
              </div>
            </template>
          </template>
          <div
            v-for="item in currentFileErrors"
            :key="item.rowNo"
            class="file-item__error"
          >
            <span class="icon icon-error"></span>
            <div class="file-item__error-description">
              <span class="error-row"
                >{{ capitalize(resources["row"]) }} {{ item.rowNo }}</span
              >
              {{ resources["in"] }} {{ resources["sheet"] }}
              <span class="error-sheet"> {{ item.sheetName }}:</span>
              <span class="error-message">{{
                getSpecificErrorMessage(item.message)
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="action-bar">
      <base-button
        level="secondary"
        type="normal"
        :disabled="isProcessing"
        @click="handleBack"
      >
        {{ resources["previous"] }}
      </base-button>
      <base-button
        level="primary"
        type="normal"
        :disabled="isProcessing"
        @click="handleClickImportAnother"
      >
        {{ capitalizeAll(resources["import_another_data"]) }}
      </base-button>
    </div>
  </div>
</template>

<script>
const HYPER_LINK_BY_OBJECT = (ids = []) => ({
  Company: `${Common.PAGE_URL.COMPANY}?ids=${ids.toString()}`,
  Plant: `${Common.PAGE_URL.LOCATION}?ids=${ids.toString()}`,
  Part: `${Common.PAGE_URL.PART}?ids=${ids.toString()}`,
  User: `${Common.PAGE_URL.USER}?userIds=${ids.toString()}`,
  Machine: `${Common.PAGE_URL.MACHINE}?ids=${ids.toString()}`,
  Tooling: `${Common.PAGE_URL.TOOLING}?ids=${ids.toString()}`,
});

const UNIT_BY_OBJECT = {
  Company: ["Company", "Companies"],
  Plant: ["Plant", "Plants"],
  Part: ["Part", "Parts"],
  User: ["User", "Users"],
  Machine: ["Machine", "Machines"],
  Tooling: ["Tooling", "Toolings"],
};

module.exports = {
  name: "StepShowResult",
  props: {
    resources: Object,
    // overview: {
    //     errorCount: 0,
    //     postCount: 0,
    //     putCount: 0,
    //     skippedCount: 0,
    //     totalCount: 0,
    //     fileId: ''
    // },
    listValidateResults: Array,
    isProcessing: Boolean,
    isOverwrite: Boolean,
  },
  setup(props, { refs, emit, root }) {
    const hasMultipleFile = computed(
      () => props.listValidateResults.length > 1
    );
    const listValidateResults = toRef(props, "listValidateResults");

    const currentFileIdx = ref(0);
    const currentFileData = ref(null);
    const currentFileErrors = ref([]);
    const currentFileSuccess = ref([]);
    watch([listValidateResults, currentFileIdx], ([newList, newIndex]) => {
      console.log("::::::::::::", newList, newIndex);
      currentFileData.value = Object.assign({}, currentFileData.value, {
        ...newList[newIndex],
      });
    });
    watch(currentFileData, (newVal) => {
      currentFileErrors.value =
        newVal?.sheetResults?.reduce((prev, curr) => {
          return [
            ...prev,
            ...curr.errors.map((i) => ({ ...i, sheetName: curr.sheetName })),
          ];
        }, []) || [];
      currentFileSuccess.value = newVal?.sheetResults;
    });

    const handleNavigateBetweenFiles = (direct) => {
      if (direct === "next") {
        if (currentFileIdx.value >= listValidateResults.value.length) return;
        currentFileIdx.value++;
      }
      if (direct === "back") {
        if (currentFileIdx.value <= 0) return;
        currentFileIdx.value--;
      }
    };

    const cleanState = () => {
      currentFileIdx.value = 0;
      currentFileData.value = null;
      currentFileErrors.value = [];
    };

    const handleBack = () => {
      emit("prev");
      cleanState();
    };

    const handleClickImportAnother = () => {
      emit("reset");
      cleanState();
    };

    const getPostCountMessage = (objectType, postCount) => {
      let message = ["singular", "plural"];
      if (!objectType) {
        message = [
          `${postCount} ${props?.resources?.["item_has_been_created"]}.`,
          `${postCount} ${props?.resources?.["items_have_been_created"]}.`,
        ];
      } else {
        message = [
          `${props?.resources?.["has_been_created"]}.`,
          `${props?.resources?.["have_been_created"]}.`,
        ];
      }
      console.log("message", message);
      if (postCount === 0 || postCount === 1) return message[0];
      if (postCount > 1) return message[1];
    };

    const getPutCountMessage = (objectType, putCount) => {
      let message = ["singular", "plural"];
      if (!objectType) {
        message = [
          `${putCount} ${props?.resources?.["item_has_been_updated"]}.`,
          `${putCount} ${props?.resources?.["items_have_been_updated"]}.`,
        ];
      } else {
        message = [
          `${props?.resources?.["has_been_updated"]}.`,
          `${props?.resources?.["have_been_updated"]}.`,
        ];
      }
      console.log("message", message);
      if (putCount === 0 || putCount === 1) return message[0];
      if (putCount > 1) return message[1];
    };

    const getErrorCountMessage = () => {
      const errCount = currentFileData.value.errorCount;
      if (errCount === 0 || errCount === 1)
        return (
          errCount.toString() +
          " " +
          props.resources["error_detected"] +
          ". " +
          root.capitalize(
            props.resources["please_fix_the_data_and_import_again"]
          ) +
          "."
        );
      if (errCount < 20)
        return (
          errCount.toString() +
          " " +
          props.resources["errors_detected"] +
          ". " +
          root.capitalize(
            props.resources["please_fix_the_data_and_import_again"]
          ) +
          "."
        );
      if (errCount >= 20) {
        const reviewedRows = currentFileData.value?.sheetResults?.reduce(
          (prev, curr) => {
            if (!curr.lastRowDataRead) return prev;
            return (
              prev +
              `<b>row ${curr.firstDataRowNo} to row ${curr.lastRowDataRead} in sheet ${curr.sheetName}</b>, `
            );
          },
          ""
        );
        let message = "";
        if (reviewedRows && reviewedRows?.charAt(3)) {
          message =
            `<b>` +
            reviewedRows.charAt(3).capitalize() +
            reviewedRows.slice(4, -2) +
            " have been reviewed. ";
        }
        return (
          errCount.toString() +
          " " +
          props.resources["errors_exceeded"] +
          ". " +
          message +
          root.capitalize(
            props.resources["please_fix_the_data_and_import_again"]
          ) +
          "."
        );
      }
      return;
    };

    const getSpecificErrorMessage = (errorMessage) => {
      console.log(errorMessage);
      if (errorMessage === "System Error Occurred. Ask System Admin Please.") {
        return "system error occurred. Please contact support@emoldino.com for further assistance.";
      }
      return errorMessage + ".";
    };

    const getHyperlinkByObjectType = (objType, ids) => {
      return HYPER_LINK_BY_OBJECT(ids)[objType];
    };

    const getUnitByObjectType = (objType, objNum) => {
      const isSingular = objNum === 0 || objNum === 1;
      const SINGULAR = 0;
      const PLURAL = 1;
      return `${objNum} ${
        UNIT_BY_OBJECT[objType][isSingular ? SINGULAR : PLURAL]
      }`;
    };

    const noDataFound = computed(() => currentFileData.value?.totalCount === 0);
    const dataAlreadyExisted = computed(
      () =>
        !props?.isOverwrite &&
        currentFileData.value?.postCount === 0 &&
        currentFileData.value?.putCount === 0
    );

    watchEffect(() => console.log("noDataFound", noDataFound.value));
    watchEffect(() =>
      console.log("dataAlreadyExisted", dataAlreadyExisted.value)
    );

    return {
      noDataFound,
      dataAlreadyExisted,

      currentFileData,
      currentFileErrors,
      currentFileSuccess,
      hasMultipleFile,
      currentFileIdx,

      handleBack,
      handleClickImportAnother,
      handleNavigateBetweenFiles,

      getErrorCountMessage,
      getPostCountMessage,
      getPutCountMessage,
      getSpecificErrorMessage,
      getHyperlinkByObjectType,
      getUnitByObjectType,
    };
  },
};
</script>

<style scoped>
.upload-result__processing {
  text-align: center;
}

.upload-result__overview .overview-title {
  font-weight: bold;
}

.upload-result__overview .overview-detail .has-error {
  color: var(--red);
}

.upload-result__overview .overview-detail .no-data {
  color: var(--red);
}

.upload-result__overview .overview-detail .no-error {
  color: var(--green-4);
}

.upload-result__files {
  position: relative;
}

.upload-result__files.multiple-file .file-item__validation {
  margin-left: 2rem;
  margin-right: 2rem;
}

.file-item__validation {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 14px 14px 14px 14px;
  margin-top: 1rem;
  gap: 5px;
  background: var(--white);
  border: 1px solid var(--grey-light);
  border-radius: 3px;
  height: 400px;
  overflow: auto;
}

@media (max-height: 900px) {
  .file-item__validation {
    max-height: 40vh;
  }
}

@media (max-height: 768px) {
  .file-item__validation {
    max-height: 250px;
  }
}

.file-item__import-number,
.file-item__error {
  display: inline-flex;
  align-items: center;
  width: 100%;
  font-size: 14.66px;
  padding: 7px 13px;
  border: 1px solid var(--grey-light);
}

.file-item__error-description .error-row {
  font-weight: bold;
}

.file-item__error-description .error-sheet {
  font-weight: bold;
}

.file-item__error-description .error-message {
}

.file-item__import-number .content {
}

.file-item__import-number .content .highlight {
}

.file-item__error {
}

.icon {
  display: inline-block;
  width: 15px;
  height: 15px;
  margin-right: 8px;
  mask-size: contain;
  mask-repeat: no-repeat;
  mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
  -webkit-mask-position: center;
}

.icon.icon-success {
  mask-image: url("/images/icon/checkmark.svg");
  -webkit-mask-image: url("/images/icon/checkmark.svg");
  background-color: var(--green);
}

.icon.icon-error {
  mask-image: url("/images/icon/warning-3.svg");
  -webkit-mask-image: url("/images/icon/warning-3.svg");
  background-color: var(--red);
}

.navigate-arrow {
  position: absolute;
  top: 50%;
  display: none;
  width: 18px;
  height: 27px;
  background-color: var(--blue);
  mask-image: url("/images/icon/backward.svg");
  mask-size: contain;
  mask-repeat: no-repeat;
  mask-position: center;
  -webkit-mask-image: url("/images/icon/backward.svg");
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
  -webkit-mask-position: center;
  cursor: pointer;
}

.upload-result__files.multiple-file .navigate-arrow {
  display: inline-block;
}

.navigate-arrow.disabled {
  cursor: not-allowed;
  pointer-events: none;
  background-color: var(--grey-light);
}

.navigate-arrow__back {
  left: 0;
}

.navigate-arrow__next {
  right: 0;
  transform: rotate(180deg);
}

.action-bar {
  position: fixed;
  bottom: calc(46px + 2rem);
  right: 2rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
