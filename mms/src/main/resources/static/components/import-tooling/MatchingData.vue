<!-- TODO: REMOVE AFTER STABLE VERSION IN DATA IMPORT -->
<template>
  <div class="matching-wrapper">
    <div
      class="direction-arrow previous"
      v-if="canPrevious"
      @click="previousFile"
    >
      <img
        src="/images/import-tooling/back-next-button.svg"
        alt="previous button"
      />
    </div>
    <div v-else class="direction-arrow"></div>
    <div class="matching-data" v-if="!!currentFile && !isLoading">
      <div class="matching-header">
        <span><span v-text="resources['file_name']"></span></span>:
        <span>{{ currentFile.fileName }}</span>
      </div>
      <div class="only-show-error">
        <div class="switch-button">
          <div class="toggleWrapper1">
            <input
              type="checkbox"
              v-model="onlyShowErrors"
              class="mobileToggle"
              id="part-toggle"
              value="1"
            />
            <label
              class="change-checkout-status"
              @click="changeOnlyShowErrors"
            ></label>
          </div>
        </div>
        <div
          class="switch-status"
          v-text="resources['only_show_rows_with_problems']"
        ></div>
      </div>
      <div class="table-content">
        <div ref="table" class="table">
          <div
            class="table-row table-header"
            style="z-index: 1; position: sticky; top: 0"
          >
            <div
              class="table-header-cell tooling-import"
              :style="{ flex: '0 0 71px' }"
            >
              <span v-text="resources['delete']"></span>
            </div>
            <div
              class="table-header-cell tooling-import"
              v-for="(header, index) in currentFile.headers"
              :key="index"
              :style="{ flex: '0 0 ' + lengthOfHeader(header) * 10 + 'px' }"
            >
              <template v-if="header.mappedHeader.type !== 'parent'">
                {{ header.mappedHeader.name }}
              </template>
              <template v-else>
                {{ labelOfChildrenHeader(header) }}
              </template>
            </div>
          </div>
          <div class="table-body">
            <div
              class="table-row"
              v-for="(row, index) in currentFile.data"
              :key="index"
              :class="{
                'd-none': row.hidden,
                'duplicated-tooling':
                  duplicatedForm.isRegRetry ||
                  (duplicatedForm.isRetry &&
                    eqTextKey(duplicatedForm.duplicatedTooling.id, row[0]) &&
                    isDuplicatedTooling(index, currentFileIndex)),
              }"
            >
              <div
                class="table-cell"
                :style="{ flex: '0 0 71px', minHeight: '42px' }"
              >
                <div class="row-action" @click="showWarningDeleteForm(index)">
                  <i class="fa fa-times-circle"></i>
                </div>
              </div>
              <div
                v-for="(header, headerIndex) in currentFile.headers"
                class="table-cell"
                :id="'cell-' + index + '-' + headerIndex"
                :class="{
                  'error-cell':
                    !isValidCell(header, row[headerIndex], row) &&
                    isOpenSelect !== 'cell-' + index + '-' + headerIndex &&
                    isInputFocus !== 'cell-' + index + '-' + headerIndex,
                  'text-left': header.mappedHeader.code === 'toolDescription',
                  active: isFocusCell(index, headerIndex),
                }"
                :key="headerIndex"
                :style="{
                  flex: '0 0 ' + lengthOfHeader(header) * 10 + 'px',
                  maxWidth: lengthOfHeader(header) * 10 + 'px',
                }"
                @mouseleave="
                  isOpenSelect === 'cell-' + index + '-' + headerIndex
                    ? (isOpenSelect = null)
                    : false
                "
                @dblclick="showFormEditCell"
                v-click-outside="hideForm"
              >
                <a-tooltip
                  placement="bottomLeft"
                  :key="'tooltip' + headerIndex"
                >
                  <template
                    v-if="
                      !isValidCell(header, row[headerIndex], row) &&
                      isOpenSelect !== 'cell-' + index + '-' + headerIndex &&
                      isInputFocus !== 'cell-' + index + '-' + headerIndex
                    "
                    slot="title"
                  >
                    <div style="padding: 9px; font-size: 11pt">
                      <div>
                        {{ errorMessage(header, row[headerIndex], row, index) }}
                      </div>
                      <div
                        v-if="getErrorFieldName(header)"
                        class="d-flex justify-content-end"
                        style="margin-top: 8px"
                      >
                        <a
                          style="
                            font-size: 11pt;
                            padding: 6px 8px;
                            border-radius: 2px;
                          "
                          class="btn-custom btn-custom-primary"
                          @click="
                            showDrawer(
                              header,
                              row,
                              headerIndex,
                              getErrorFieldName(header)
                            )
                          "
                        >
                          <div class="d-flex align-items-center">
                            <span style="font-size: 11pt">
                              {{ resources["create"] }}
                              {{
                                getErrorFieldName(header) === "location"
                                  ? "plant"
                                  : getErrorFieldName(header)
                              }}
                            </span>
                          </div>
                        </a>
                      </div>
                    </div>
                  </template>
                  <div
                    class="value-wrapper"
                    :class="{
                      'maching-input': !header.mappedHeader.rules.options,
                    }"
                  >
                    <div
                      @mouseover="isOpenSelect = null"
                      class="value"
                      :class="{ active: isFocusCell(index, headerIndex) }"
                    >
                      {{ labelOfCell(header, row[headerIndex]) }}
                    </div>
                    <div
                      style="height: 100%"
                      class="input-form-wrapper"
                      @click="removeFocusCell"
                    >
                      <div
                        style="padding: 0; max-height: unset"
                        class="left-content"
                      >
                        <div
                          class="dropdown"
                          style="height: 100%"
                          v-if="header.mappedHeader.rules.options"
                          :ref="'cell-dropdown-' + index + '-' + headerIndex"
                          @click="
                            scroll(index + '-' + headerIndex);
                            pageSearch = 0;
                            searchText = '';
                            filterDataCounter(null, header);
                            workingColumnIndex = headerIndex;
                            workingRowIndex = index;
                          "
                        >
                          <div
                            style="height: 100%; border-radius: 0"
                            class="dropbtn"
                            :class="{
                              show:
                                isOpenSelect ===
                                  'cell-' + index + '-' + headerIndex ||
                                isTyping,
                            }"
                            @click="
                              changeSelectState(
                                'cell-' + index + '-' + headerIndex
                              );
                              resultSearching =
                                header.mappedHeader.rules.options;
                            "
                          >
                            <span>{{
                              labelOfCell(header, row[headerIndex])
                            }}</span>
                            <div class="field-icon">
                              <img
                                src="/images/import-tooling/down-arrow-grey.svg"
                                alt="down arrow"
                              />
                            </div>
                          </div>
                          <div
                            v-if="
                              workingColumnIndex === headerIndex &&
                              workingRowIndex === index
                            "
                            @scroll="scrollDropdown($event, header)"
                            @mouseleave="isOpenSelect = null"
                            :ref="'dropdown-' + index + '-' + headerIndex"
                            class="dropdown-content"
                            :class="{
                              'max-min-height':
                                header.mappedHeader.code !==
                                  'cycleTimeLimit1Unit' &&
                                header.mappedHeader.code !==
                                  'cycleTimeLimit2Unit' &&
                                header.mappedHeader.code !== 'weightUnit' &&
                                header.mappedHeader.code !==
                                  'toolingComplexity',
                            }"
                            :style="{
                              right:
                                workingColumnIndex ==
                                currentFile.headers.length - 1
                                  ? '0'
                                  : 'unset',
                            }"
                          >
                            <div
                              class="search-container"
                              v-if="header.mappedHeader.rules.searchable"
                            >
                              <i class="fa fa-search icon-search"></i>
                              <input
                                v-if="!header.mappedHeader.rules.searchApi"
                                class="input-search"
                                value=""
                                onfocus="isTyping = true"
                                onblur="this.value = '';isTyping = false"
                                @input="
                                  filterData(
                                    $event,
                                    header.mappedHeader.rules.options
                                  )
                                "
                                :placeholder="
                                  header.mappedHeader.rules.searchPlaceholder
                                    ? replaceLongtext(
                                        header.mappedHeader.rules
                                          .searchPlaceholder,
                                        24
                                      )
                                    : replaceLongtext(
                                        'Search ' + header.mappedHeader.name,
                                        24
                                      )
                                "
                              />
                              <input
                                v-else
                                class="input-search"
                                value=""
                                onfocus="isTyping = true"
                                onblur="this.value = '';isTyping = false"
                                @input="filterDataCounter($event, header)"
                                :placeholder="
                                  header.mappedHeader.rules.searchPlaceholder
                                    ? replaceLongtext(
                                        header.mappedHeader.rules
                                          .searchPlaceholder,
                                        24
                                      )
                                    : replaceLongtext(
                                        'Search ' + header.mappedHeader.name,
                                        24
                                      )
                                "
                              />
                            </div>
                            <span
                              v-for="(option, dropdownIndex) in header
                                .mappedHeader.rules.searchApi
                                ? searchData
                                : resultSearching"
                              :key="dropdownIndex"
                              :value="option.value"
                              @click="
                                row[headerIndex] = option.value;
                                chooseCounter(option, header);
                                closeOption();
                                isOpenSelect = null;
                              "
                            >
                              {{ replaceLongtext(option.name, 20) }}
                            </span>
                          </div>
                        </div>
                        <textarea
                          v-else-if="
                            header.mappedHeader.rules.inputType === 'textarea'
                          "
                          style="max-height: unset; border-radius: 0"
                          :ref="'cell-' + index + '-' + headerIndex"
                          class="form-control"
                          v-model="row[headerIndex]"
                          @mouseleave="
                            isInputFocus = null;
                            removeFocusInput(index, headerIndex);
                          "
                          @click="
                            isInputFocus = 'cell-' + index + '-' + headerIndex
                          "
                        ></textarea>
                        <input
                          v-else
                          style="max-height: unset; border-radius: 0"
                          :ref="'cell-' + index + '-' + headerIndex"
                          class="form-control"
                          v-model="row[headerIndex]"
                          @mouseleave="
                            isInputFocus = null;
                            removeFocusInput(index, headerIndex);
                          "
                          @click="
                            isInputFocus = 'cell-' + index + '-' + headerIndex
                          "
                        />
                      </div>
                      <div class="right-content" v-if="false">
                        <div class="btn-action save-input">
                          <i class="fa fa-check"></i>
                        </div>
                      </div>
                    </div>
                  </div>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="direction-arrow next" v-if="canNext" @click="nextFile">
      <img
        src="/images/import-tooling/back-next-button.svg"
        alt="next button"
      />
    </div>
    <div v-else class="direction-arrow"></div>
    <div
      class="validation-info"
      v-if="countFailedValidationCurrentFile > 0 && !isLoading"
    >
      <div class="validation-wrapper left-action">
        <a
          class="btn btn-default track-bar left"
          @click="previousInvalidData"
        ></a>
        <a class="btn btn-default track-bar" @click="nextInvalidData"></a>
        <div class="tracking-indicator">
          <div class="tracking-title">
            {{ countInvalidDataResolvedCurrentFile }} of
            {{ countTotalFailedValidationCurrentFile }}
            {{ countTotalFailedValidationCurrentFile > 1 ? "issues" : "issue" }}
            resolved
          </div>
          <div class="progress-tracking-wrapper">
            <div class="progress-tracking">
              <div
                class="progress-bar-tracking"
                :style="{
                  width:
                    (countInvalidDataResolvedCurrentFile * 100) /
                      countTotalFailedValidationCurrentFile +
                    '%',
                }"
                role="progressbar"
                aria-valuenow="15"
                aria-valuemin="0"
                aria-valuemax="100"
              ></div>
            </div>
          </div>
        </div>
      </div>
      <div class="validation-wrapper">
        <div class="failed-info">
          <span class="failed-count">{{
            countFailedValidationCurrentFile
          }}</span>
          data
        </div>
        <span v-text="resources['fail_validation']"> </span>
      </div>
    </div>
    <div
      class="matching-modal"
      v-show="showImportForm"
      style="z-index: 2 !important"
    >
      <div class="importing-modal modal-wrapper importing-modal">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-title" v-text="resources['importing_data']"></div>
            <button type="button" aria-label="Close" class="close">
              <span aria-hidden="true" @click="showImportForm = false">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div
                class="loading-title"
                v-text="resources['loading'] + '...'"
              ></div>
              <div class="loading-progress">
                <div class="progress-bar-wrapper">
                  <div class="progress-bar" id="progress-bar">
                    <div class="direction-icon"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="matching-modal" v-if="notificationForm.isShow">
      <div class="modal-wrapper notification-modal">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-title" v-text="resources['notification']"></div>
            <button type="button" aria-label="Close" class="close">
              <span aria-hidden="true" @click="closeNotificationForm">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="notification-title">
                <span v-text="resources['tooling']"></span>
                {{ this.notificationForm.registeredTooling.id }}
                <span v-text="resources['already_registered']"></span>
              </div>
              <div
                class="notification-action"
                style="/*padding: 10px 21px; !important*/"
              >
                <button
                  class="btn btn-primary"
                  @click="replaceRegisteredTooling"
                  v-text="resources['replace']"
                ></button>
                <button
                  class="btn btn-default"
                  @click="skipRegisteredTooling"
                  v-text="resources['skip']"
                ></button>
              </div>
              <div class="notification-action-all">
                <input
                  type="checkbox"
                  v-model="notificationForm.isApplyToAll"
                />
                <span v-text="resources['apply_all']"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="matching-modal" v-if="duplicatedForm.isShow">
      <div class="modal-wrapper duplicated-modal">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-title" v-text="resources['notification']"></div>
            <button type="button" aria-label="Close" class="close">
              <span aria-hidden="true" @click="closeDuplicatedForm">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="duplicated-title">
                <span v-text="resources['tooling']"></span>
                {{ duplicatedForm.duplicatedTooling.id }}
                <span v-text="resources['is_duplicated']"></span>
              </div>
              <div
                class="duplicated-action"
                style="padding: 10px 21px; !important"
              >
                <button
                  class="btn btn-primary"
                  @click="retryDuplicated"
                  v-text="resources['retry']"
                ></button>
                <button
                  class="btn btn-default"
                  @click="skipDuplicate"
                  v-text="resources['skip']"
                ></button>
              </div>
              <div class="duplicated-action-all">
                <input type="checkbox" v-model="duplicatedForm.isApplyToAll" />
                <span v-text="resources['apply_all']"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="matching-modal" v-if="deleteForm.isShow">
      <div class="modal-wrapper warning-delete-modal">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-title" v-text="resources['warning']"></div>
            <button
              type="button"
              aria-label="Close"
              class="close"
              @click="hideWarningDeleteForm"
            >
              <span aria-hidden="true">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="warning-delete-title">
                <span
                  v-text="
                    resources['delete'] + ' ' + resources['report_tooling']
                  "
                ></span>
                {{ deleteForm.toolingId }}
                <span v-text="resources['from_list'] + '?'"></span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <div>
              <button
                class="btn btn-danger"
                @click="deleteTooling"
                v-text="resources['delete']"
              ></button>
              <button
                class="btn btn-default"
                @click="hideWarningDeleteForm"
                v-text="resources['cancel']"
              ></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="matching-modal" v-if="matchedCounterForm.isShow">
      <div class="modal-wrapper duplicated-modal" style="color: #4b4b4b">
        <div class="modal-container">
          <div class="modal-header" style="padding: 5px 15px 5px">
            <div
              class="modal-title"
              style="font-size: 16px"
              v-text="resources['notification']"
            ></div>
            <button
              type="button"
              aria-label="Close"
              class="close"
              style="padding-top: 16px"
            >
              <span aria-hidden="true" @click="hideCheckMatchedCounterForm"
                >×</span
              >
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="duplicated-title" style="font-size: 19px">
                Counter <span v-text="matchedCounterForm.counterCode"></span> is
                already matched with tooling
                <span v-text="matchedCounterForm.toolingCode"></span>. Do you
                want to overwrite the data?
              </div>
              <div
                class="duplicated-action"
                style="padding: 10px 21px; !important"
              >
                <button
                  class="btn btn-default btn-back"
                  @click="removeCheckMatchedCounterForm"
                  v-text="resources['no_overwrite']"
                ></button>
                <button
                  class="btn btn-default btn-overwrite"
                  @click="overwriteMatchedCounter"
                  v-text="resources['yes_overwrite']"
                ></button>
              </div>
              <div class="duplicated-action-all">
                <input
                  type="checkbox"
                  v-model="matchedCounterForm.isApplyToAll"
                />
                <span v-text="resources['apply_all']"></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="matching-modal" v-if="importError">
      <div class="modal-wrapper duplicated-modal" style="color: #4b4b4b">
        <div class="modal-container">
          <div class="modal-header" style="padding: 5px 15px 5px">
            <div
              class="modal-title"
              style="font-size: 16px"
              v-text="resources['notification']"
            ></div>
            <button
              type="button"
              aria-label="Close"
              class="close"
              style="padding-top: 16px"
            >
              <span aria-hidden="true" @click="goBackImportError">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="duplicated-title" style="font-size: 19px">
                Data errors have been detected in tooling
                <span v-text="moldCodeError"></span> and can't be imported.
                Please contact
                <a href="mailto:support@emoldino.com">support.emoldino.com</a>
                for further support.
              </div>
              <div class="error-detect-action">
                <button
                  class="btn btn-default btn-back"
                  @click="goBackImportError"
                  v-text="resources['go_back']"
                ></button>
                <button
                  class="btn btn-default btn-overwrite"
                  @click="continueImport"
                  v-text="resources['continue_import']"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="matching-modal" v-if="isBadRequest">
      <div class="modal-wrapper duplicated-modal" style="color: #4b4b4b">
        <div class="modal-container">
          <div class="modal-header" style="padding: 5px 15px 5px">
            <div
              class="modal-title"
              style="font-size: 16px"
              v-text="resources['notification']"
            ></div>
            <button
              type="button"
              aria-label="Close"
              class="close"
              style="padding-top: 16px"
            >
              <span aria-hidden="true" @click="goBackBadRequest">×</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="duplicated-title" style="font-size: 19px">
                An error has occurred while importing the data. Please contact
                <a href="mailto:support@emoldino.com">support.emoldino.com</a>
                for further support.
              </div>
              <div
                class="error-detect-action"
                style="margin-left: 160px; justify-content: end"
              >
                <button
                  class="btn btn-default btn-back"
                  @click="goBackBadRequest"
                  v-text="resources['go_back']"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      id="op-confirm-unfilled"
      aria-hidden="true"
      class="modal fade"
      tabindex="-1"
      role="dialog"
    >
      <div class="modal-dialog modal-dialog-centered modal-md" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ resources["notification"] }}</h5>
            <button
              type="button"
              class="close custom-close-btn"
              data-dismiss="modal"
              aria-label="Close"
              @click="closeModal()"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <p>{{ errorImportMessage }}</p>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-primary op-modal-ok"
              @click="closeModal()"
              v-text="resources['ok']"
            ></button>
          </div>
        </div>
      </div>
    </div>
    <create-property-drawer
      :resources="resources"
      :visible="visible"
      @close="closeDrawer"
      @create-tooling-success="createToolingSuccess"
      :name-drawer="nameDrawer"
    />
  </div>
</template>

<script>
module.exports = {
  name: "MatchingData",
  components: {
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
  },
  props: {
    fileListByMappedHeader: {
      type: Array,
      default: () => [],
    },
    originHeader: {
      type: Array,
      default: () => [],
    },
    isCheckingExisted: {
      type: Boolean,
      default: () => false,
    },
    isCheckingDuplicatedId: {
      type: Boolean,
      default: () => false,
    },
    resources: Object,
  },
  data() {
    return {
      isHoverCell: null,
      currentFileIndex: 0,
      isImportDone: false,
      showImportForm: false,
      showNotificationForm: false,
      showDuplicatedForm: false,
      isImportError: false,
      errorImportMessage: null,
      notificationForm: {
        isShow: false,
        isApplyToAll: false,
        allRegisteredTooling: [],
        registeredTooling: {
          id: null, // id in file
          realId: null, // id in server
        },
      },
      duplicatedForm: {
        isShow: false,
        isApplyToAll: false,
        isRetry: false,
        isRegRetry: false,
        duplicatedTooling: {
          id: null,
          rowIndex: null,
          fileIndex: null,
        },
        nextDuplicatedTooling: {
          id: null,
          rowIndex: null,
          fileIndex: null,
        },
      },
      currentIntervalIds: [],
      focus: {
        index: null,
        headerIndex: null,
      },
      tempInput: {},
      deleteForm: {
        isShow: false,
        tooling: null,
      },
      dataImportToServer: [],
      invalidData: {},
      currentInvalidIndex: null,
      focusIndex: null,
      focusHeaderIndex: null,
      isOpenSelect: null,
      isShowing: false,
      userEngineers: [],
      listPart: [],
      listToolMaker: [],
      listSupplier: [],
      invalidEngineersArray: [],
      isLoading: false,
      trigger: 0,
      onlyShowErrors: false,
      visible: false,
      nameDrawer: null,
      createProp: null,
      isInputFocus: null,
      listCounter: [],
      listLocation: [],
      originFileListByMappedHeader: [],
      searchingHeaderIndex: null,
      searchingRowIndex: null,
      resultSearching: "",
      isTyping: false,
      searchData: [],
      pageSearch: 0,
      searchText: "",
      selectedList: {
        counter: [],
        part: [],
        location: [],
        engineer: [],
        toolMaker: [],
        supplier: [],
      },
      workingRowIndex: "",
      workingColumnIndex: "",
      matchedCounterForm: {
        isShow: false,
        isApplyToAll: false,
        counterId: null,
        counterCode: null,
        toolingCode: null,
        counterSkipArr: [],
        counterRemoveArr: [],
      },
      importError: false,
      moldCodeError: "",
      isBadRequest: false,
      existingToolings: [],
    };
  },
  watch: {
    isCheckingExisted(value) {
      console.log("checkMatchedCounter", value);
      if (value) {
        this.checkingExisted();
      }
    },
    isCheckingDuplicatedId(value) {
      console.log("isCheckingDuplicatedId", value);
      if (value) {
        this.checkingDuplicated();
        // if after check duplicate, no duplicate
        if (!this.duplicatedForm.isShow) {
          if (
            this.currentFile.headers.some(
              (data) =>
                data.mappedHeader && data.mappedHeader.name === "Sensor ID"
            )
          ) {
            this.checkMatchedCounter(true);
          } else {
            this.checkingExisted();
          }

          // if (!this.matchedCounterForm.isShow) {
          //   this.checkingExisted();
          // }
        }
      }
    },
  },
  computed: {
    currentFile() {
      if (this.onlyShowErrors) {
        console.log("onlyShowErrors");
        this.fileListByMappedHeader[this.currentFileIndex].data.map(
          (dataItem) => {
            if (
              this.fileListByMappedHeader[
                this.currentFileIndex
              ].headers.findIndex(
                (item, index) =>
                  !this.isValidCell(item, dataItem[index], dataItem)
              ) === -1
            ) {
              dataItem.hidden = true;
            }
          }
        );
      } else {
        this.fileListByMappedHeader[this.currentFileIndex].data.map(
          (dataItem) => {
            dataItem.hidden = false;
          }
        );
      }
      return this.fileListByMappedHeader[this.currentFileIndex];
    },
    canNext() {
      console.log(this.trigger);
      return this.currentFileIndex < this.fileListByMappedHeader.length - 1;
    },
    canPrevious() {
      console.log(this.trigger);
      return this.currentFileIndex > 0;
    },
    countFailedValidationCurrentFile() {
      console.log(this.trigger);
      this.checkingCanNextStep();
      return this.countFailedValidation(this.currentFileIndex);
    },
    countTotalFailedValidationCurrentFile() {
      console.log(this.trigger);
      return this.countTotalFailedValidation(this.currentFileIndex);
    },
    countInvalidDataResolvedCurrentFile() {
      console.log(this.trigger);
      return this.countInvalidDataResolved(this.currentFileIndex);
    },
  },
  async created() {
    this.isLoading = true;
    await Promise.all([
      this.getListPart(),
      this.getUserEngineers(),
      this.getLocation(),
      // this.getToolMaker(),
      // this.getSupplier(),
      this.getAllCompany(),
      this.getCounter(),
      this.getExistingToolings(),
    ]);
    this.initFailedValidation();
    this.isLoading = false;
  },
  methods: {
    chooseCounter(option, header) {
      this.pageSearch = 0;
      this.searchText = "";
      if (header.mappedHeader.code === "counterCode") {
        this.selectedList.counter.push(option);
      } else if (
        header.mappedChildrenField &&
        header.mappedChildrenField === "partCode"
      ) {
        this.selectedList.part.push(option);
      } else if (header.mappedHeader.code === "locationCode") {
        this.selectedList.location.push(option);
      } else if (header.mappedHeader.code === "engineerEmails") {
        this.selectedList.engineer.push(option);
      } else if (header.mappedHeader.code === "toolMakerCompanyCode") {
        this.selectedList.toolMaker.push(option);
      } else if (header.mappedHeader.code === "supplierCompanyCode") {
        this.selectedList.supplier.push(option);
      }
    },
    scrollDropdown(element, header) {
      const scrollTop = element.target.scrollTop;
      const clientHeight = element.target.clientHeight;
      const scrollHeight = element.target.scrollHeight;
      if (scrollTop + clientHeight >= scrollHeight) {
        this.pageSearch = this.pageSearch + 1;
        this.filterDataCounter(null, header);
      }
    },
    filterData(event, array) {
      const str = event.target.value;
      if (!str) {
        return (this.resultSearching = array);
      }
      var options = {
        keys: ["name"],
      };
      var searcher = new Fuse(array, options);
      return (this.resultSearching = searcher.search(str));
    },
    filterDataCounter(event, header) {
      if (header.mappedHeader.rules.searchApi === true) {
        if (event) {
          this.searchText = event.target.value;
          this.pageSearch = 0;
        }
        const size = 20;
        let param = "page=" + this.pageSearch + "&size=" + size;
        let options = [];
        if (this.pageSearch !== 0) {
          options.push(...this.searchData);
        }
        param += "&searchText=" + this.searchText;
        if (header.mappedHeader.code === "counterCode") {
          axios
            .get("/api/counters/get-list-counter?" + param)
            .then((response) => {
              response.data.data.forEach((data) => {
                options.push({
                  name: data.counterCode,
                  value: data.counterCode,
                });
              });
            });
        } else if (
          header.mappedChildrenField &&
          header.mappedChildrenField === "partCode"
        ) {
          axios.get("/api/parts/list-part?" + param).then((response) => {
            response.data.forEach((data) => {
              options.push({
                name: data.code + " - " + data.name,
                value: data.code,
              });
            });
          });
        } else if (header.mappedHeader.code === "locationCode") {
          axios
            .get(
              "/api/locations?query=" +
                this.searchText +
                "&status=active&sort=id%2Cdesc&" +
                param
            )
            .then((response) => {
              response.data.content.forEach((data) => {
                options.push({
                  name: data.locationCode + " - " + data.name,
                  value: data.locationCode,
                });
              });
            });
        } else if (header.mappedHeader.code === "engineerEmails") {
          axios.get("/api/users/engineers?" + param).then((response) => {
            response.data.forEach((data) => {
              options.push({
                name: data.email,
                value: data.email,
              });
            });
          });
        } else if (
          header.mappedHeader.code === "toolMakerCompanyCode" ||
          header.mappedHeader.code === "supplierCompanyCode"
        ) {
          axios
            .get("/api/companies/get-company-list?&" + param)
            .then((response) => {
              response.data.data.forEach((data) => {
                options.push({
                  name: data.companyCode + " - " + data.name,
                  value: data.companyCode,
                });
              });
            });
        }
        return (this.searchData = options);
      }
    },
    setFocus(index, header) {
      setTimeout(() => {
        this.$refs[`cell-${index}-${header}`][0].focus();
      }, 300);
    },
    removeFocusInput(index, header) {
      this.$refs[`cell-${index}-${header}`][0].blur();
    },
    createSuccess() {
      this.$emit("update");
    },
    async createToolingSuccess(value) {
      this.getLocation();
      this.getListPart();
      // this.getToolMaker()
      // this.getSupplier()
      this.getAllCompany();
      this.getCounter();
      this.createProp.row[this.createProp.headerIndex] = value;
    },
    showDrawer(header, row, headerIndex, name) {
      this.isOpenSelect = null;
      this.isInputFocus = null;
      this.createProp = {
        header,
        row,
        headerIndex,
      };
      this.nameDrawer = name;
      this.visible = true;
    },
    closeDrawer() {
      console.log("close");
      this.nameDrawer = "";
      this.isInputFocus = null;
      this.isOpenSelect = null;
      this.visible = false;
    },
    changeOnlyShowErrors() {
      this.onlyShowErrors = !this.onlyShowErrors;
      // this.currentFile.headers.find((item, index) => !this.isValidCell(item, fileitem[index]))
      // if(this.onlyShowErrors) {
      //   console.log(this.currentFile, 'this.currentFilethis.currentFile')
      //   this.currentFile.headers = JSON.parse(JSON.stringify())
      //   // this.currentFile.data =
      // }
    },
    scroll(ref) {
      const self = this;
      setTimeout(() => {
        const dropdown = self.$refs["dropdown-" + ref];
        const div = self.$refs["cell-dropdown-" + ref];
        if (dropdown && dropdown[0]) {
          const bottom = div[0].getBoundingClientRect().bottom;
          const heightDropdown = dropdown[0].getBoundingClientRect().height;
          const containerRect = self.$refs.table.getBoundingClientRect();
          dropdown[0].scrollTop = 0;
          if (bottom + heightDropdown > containerRect.bottom) {
            self.$refs.table.scrollTop += heightDropdown;
          }
        }
      }, 150);
    },
    async getExistingToolings() {
      try {
        console.log("this.fileListByMappedHeader", this.fileListByMappedHeader);
        const param = encodeURI(
          this.fileListByMappedHeader[0].data.map((item) => item[0]).join(",")
        );

        const data = await axios.get(`/api/molds?equipmentCodes=${param}`);
        this.existingToolings = data.data.content;
      } catch (e) {
        console.error("err", e.response);
      }
    },
    async getListPart() {
      try {
        const data = await axios.get("/api/parts/list-part");
        data.data.forEach((res) => {
          this.listPart.push(res.code);
        });
      } catch (e) {
        console.error("err", e.response);
      }
    },
    async getUserEngineers() {
      try {
        const data = await axios.get("/api/users/engineers");
        data.data.forEach((res) => {
          this.userEngineers.push(res.email);
        });
      } catch (e) {
        console.error("err", e.response);
      }
    },
    async getLocation() {
      try {
        const data = await axios.get(
          "/api/locations?query=&status=active&sort=id%2Cdesc&page=1&size=99999"
        );
        data.data.content.forEach((res) => {
          this.listLocation.push(res.locationCode);
        });
      } catch (e) {
        console.error("err", e.response);
      }
    },
    async getCounter() {
      console.log("getCounter");
      let page = 1;
      while (await this.pageCounter(page)) {
        page += 1;
      }
    },
    async pageCounter(page) {
      try {
        const data = await axios.get(
          "/api/counters/get-list-counter?page=" + page + "&size=2000"
        );
        data.data.data.forEach((res) => {
          this.listCounter.push(res);
        });
        return data.data.data.length === 2000;
      } catch (e) {
        console.error("err", e.response);
        return false;
      }
    },
    /*
            async getToolMaker () {
              try {
                const data = await axios.get("/api/companies?query=&status=TOOL_MAKER&sort=id%2Cdesc&page=1&size=999")
                data.data.content.forEach(res => {
                  this.listToolMaker.push(res.companyCode)
                });
              } catch(e) {
                console.error('err', e.response);
              }
            },
            async getSupplier () {
              try {
                const data = await axios.get("/api/companies?query=&status=SUPPLIER&sort=id%2Cdesc&page=1&size=999")
                data.data.content.forEach(res => {
                  this.listSupplier.push(res.companyCode)
                });
              } catch(e) {
                console.error('err', e.response);
              }
            },
*/
    async getAllCompany() {
      try {
        const data = await axios.get(
          "/api/companies?query=&status=&sort=id%2Cdesc&page=1&size=999"
        );
        data.data.content.forEach((res) => {
          this.listToolMaker.push(res.companyCode);
          this.listSupplier.push(res.companyCode);
        });
      } catch (e) {
        console.error("err", e.response);
      }
    },

    checkValidEngineers(email) {
      if (email.indexOf(",") > -1) {
        const emailList = email.split(",");
        let arr = [];
        emailList.map((resEmail) => {
          if (this.userEngineers.indexOf(resEmail.trim()) <= -1) {
            arr.push(resEmail);
          }
        });
        return !arr.length > 0;
      } else {
        const valid = this.userEngineers.filter((res) => res === email.trim());
        return valid.length > 0;
      }
    },
    checkErrorMessageEngineers(email) {
      if (email && email.indexOf(",") > -1) {
        const emailList = email.split(",");
        let arr = [];
        emailList.map((resEmail) => {
          if (this.userEngineers.indexOf(resEmail.trim()) <= -1) {
            arr.push(resEmail);
          }
        });
        return arr;
      } else {
        let invalidArr = [];
        // const invalid = this.userEngineers.filter(res => res === email.trim())
        if (this.userEngineers.indexOf(email.trim()) <= -1) {
          invalidArr.push(email);
        }
        return invalidArr;
      }
    },
    checkValidPart(name) {
      const valid = this.listPart.filter((res) => Common.eqTextKey(res, name));
      return valid.length > 0;
    },
    checkValidLocation(locationCode) {
      const valid = this.listLocation.filter((res) =>
        Common.eqTextKey(res, locationCode)
      );
      return valid.length > 0;
    },
    checkValidCounter(counterName, row, header) {
      const currentFile = this.fileListByMappedHeader[this.currentFileIndex];
      const indexCounterId = currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Sensor ID"
      );
      const counterValue = this.labelOfCell(header, counterName);

      const valid = this.listCounter.filter((res) =>
        Common.eqTextKey(res.counterCode, counterValue)
      );
      if (valid.length === 0) return false;
      // const invalidCounterMatched = valid.filter(res => res.toolingCode && res.toolingCode !== row[indexToolingId]);
      // if (invalidCounterMatched.length > 0) return false;
      let counterIdList = [];
      currentFile.data.forEach((data) =>
        counterIdList.push(this.labelOfCell(header, data[indexCounterId]))
      );
      const duplicate = counterIdList.filter((item) => counterValue === item);
      return duplicate.length === 1;
    },
    checkValidToolMaker(companyCode) {
      const valid = this.listToolMaker.filter((res) =>
        Common.eqTextKey(res, companyCode)
      );
      return valid.length > 0;
    },
    checkValidSupplier(companyCode) {
      const valid = this.listSupplier.filter((res) =>
        Common.eqTextKey(res, companyCode)
      );
      return valid.length > 0;
    },
    countTotalFailedValidation(fileIndex) {
      let countInvalid = 0;
      let fileElement = this.fileListByMappedHeader[fileIndex];
      let invalidDataByFile = this.invalidData[fileIndex];
      const headers = fileElement.headers;
      fileElement.data.forEach((row, index) => {
        headers.forEach((header, headerIndex) => {
          if (
            invalidDataByFile !== undefined &&
            invalidDataByFile[index] !== undefined &&
            invalidDataByFile[index][headerIndex] !== undefined &&
            row[headerIndex] !== undefined
          ) {
            if (
              invalidDataByFile[index][headerIndex] ||
              !this.isValidCell(header, row[headerIndex], row)
            ) {
              countInvalid++;
            }
          }
        });
      });
      return countInvalid;
    },
    countInvalidDataResolved(fileIndex) {
      let countValid = 0;
      let fileElement = this.fileListByMappedHeader[fileIndex];
      let invalidDataByFile = this.invalidData[fileIndex];
      const headers = fileElement.headers;
      fileElement.data.forEach((row, index) => {
        headers.forEach((header, headerIndex) => {
          if (
            invalidDataByFile !== undefined &&
            invalidDataByFile[index] !== undefined &&
            invalidDataByFile[index][headerIndex] !== undefined &&
            row[headerIndex] !== undefined
          ) {
            if (
              invalidDataByFile[index][headerIndex] &&
              this.isValidCell(header, row[headerIndex], row)
            ) {
              countValid++;
            }
          }
        });
      });
      return countValid;
    },
    checkingCanNextStep() {
      let countInvalid = 0;
      const fileLength = this.fileListByMappedHeader.length;
      for (let i = 0; i < fileLength; i++) {
        countInvalid += this.countFailedValidation(i);
      }
      if (countInvalid === 0) {
        // console.log('emit event can next');
        this.$emit("matching-data-done", true);
      } else {
        // console.log('emit event cannot next');
        this.$emit("matching-data-done", false);
      }
    },
    countFailedValidation(fileIndex) {
      let countInvalid = 0;
      let fileElement = this.fileListByMappedHeader[fileIndex];
      const headers = fileElement.headers;
      fileElement.data.forEach((row, index) => {
        headers.forEach((header, headerIndex) => {
          if (!this.isValidCell(header, row[headerIndex], row)) {
            countInvalid++;
          }
        });
      });
      return countInvalid;
    },
    initFailedValidation() {
      this.invalidData = {};
      Object.keys(this.fileListByMappedHeader).forEach((fileIndex) => {
        this.invalidData[fileIndex] = [];
        let fileElement = this.fileListByMappedHeader[fileIndex];
        const headers = fileElement.headers;
        fileElement.data.forEach((row) => {
          let invalidRow = [];
          headers.forEach((header, headerIndex) => {
            if (!row[headerIndex]) {
              row[headerIndex] = "";
            }
            invalidRow.push(!this.isValidCell(header, row[headerIndex], row));
          });
          this.invalidData[fileIndex].push(invalidRow);
        });
      });
    },
    previousFile() {
      if (this.canPrevious) {
        this.currentFileIndex--;
      }
    },
    nextFile() {
      if (this.canNext) {
        this.currentFileIndex++;
      }
    },
    importIntoServer() {
      this.showImportForm = true;
      this.progressBarRun();
      let __importData = [];

      this.dataImportToServer.forEach((dataImport) => {
        let importItem = { ...dataImport };
        if (importItem.poDate) {
          importItem.poDate = importItem.poDate.replaceAll("-", "");
        }
        __importData.push(importItem);
      });

      axios
        .post("/api/molds/import-molds", __importData)
        .then((response) => {
          console.log("importIntoServer response", response);
          if (!response.data.success) {
            console.log(response.data);
            this.importError = true;
            this.moldCodeError = response.data.data;
          } else {
            this.isImportDone = true;
          }
        })
        .catch((error) => {
          this.isBadRequest = true;
        });
    },
    progressBarRun() {
      this.currentIntervalIds.forEach((id) => {
        clearInterval(id);
      });
      let elem = document.getElementById("progress-bar");
      elem.style.width = "7%";
      let width = 7;
      let id = setInterval(frame, 40);

      this.currentIntervalIds = [];
      this.currentIntervalIds.push(id);

      let self = this;
      function frame() {
        if (width >= 100) {
          elem.style.width = width + "%";
        } else {
          if (width < 30) {
            width = width + 0.5;
          } else if (width >= 30 && width < 50) {
            width = width + 0.3;
          } else if (width >= 50 && width < 75) {
            width = width + 0.1;
          } else if (width >= 75 && width < 85) {
            width = width + 0.05;
          } else if (width >= 85 && width < 99.5) {
            width = width + 0.01;
          }
          elem.style.width = width + "%";
        }

        if (self.isImportDone) {
          width = 100;
          elem.style.width = width + "%";
          setTimeout(() => {
            self.$emit("complete-import", true);
          }, 1000);
        }
      }
    },
    setDataToImportServer() {
      let dataSend = [];
      this.fileListByMappedHeader.forEach((fileElement) => {
        const headers = fileElement.headers;
        const sendDataItem = fileElement.data.map((row) => {
          const clonedRow = {};
          row.forEach((item, index) => {
            let header = headers[index];
            if (header.mappedHeader.type !== "parent") {
              // process for size
              if (item && item.toString().trim()) {
                if (header.mappedHeader.isCustomField) {
                  if (clonedRow.customFieldDTOList == null)
                    clonedRow.customFieldDTOList = [];
                  clonedRow.customFieldDTOList.push({
                    id: header.mappedHeader.code,
                    customFieldValueDTOList: [
                      {
                        value: item,
                      },
                    ],
                  });
                } else {
                  if (
                    !(
                      header.mappedHeader.code === "counterCode" &&
                      this.matchedCounterForm.counterRemoveArr.includes(item)
                    )
                  ) {
                    clonedRow[header.mappedHeader.code] = item;
                  }
                }
              }
            } else {
              if (!clonedRow[header.mappedHeader.code])
                clonedRow[header.mappedHeader.code] = [];
              if (
                !clonedRow[header.mappedHeader.code][header.mappedChildrenIndex]
              ) {
                clonedRow[header.mappedHeader.code].push({});
              }
              while (
                header.mappedChildrenIndex >=
                clonedRow[header.mappedHeader.code].length
              ) {
                clonedRow[header.mappedHeader.code].push({});
              }

              if (item && item.toString().trim()) {
                clonedRow[header.mappedHeader.code][header.mappedChildrenIndex][
                  header.mappedChildrenField
                ] = item;
              }
            }
          });

          if (clonedRow.size_h && clonedRow.size_l && clonedRow.size_w) {
            clonedRow.size = `${clonedRow.size_w} x ${clonedRow.size_l} x ${clonedRow.size_h}`;
          }
          delete clonedRow.size_w;
          delete clonedRow.size_l;
          delete clonedRow.size_h;

          if (clonedRow.sizeUnit)
            clonedRow.sizeUnit = clonedRow.sizeUnit.toUpperCase();

          if (clonedRow.engineerEmails)
            clonedRow.engineerEmails =
              clonedRow.engineerEmails && clonedRow.engineerEmails.split(",");

          if (clonedRow.cycleTimeLimit1Unit === "s") {
            clonedRow.cycleTimeLimit1Unit = "SECOND";
          }
          if (clonedRow.cycleTimeLimit1Unit === "%") {
            clonedRow.cycleTimeLimit1Unit = "PERCENTAGE";
          }

          if (clonedRow.cycleTimeLimit2Unit === "s") {
            clonedRow.cycleTimeLimit2Unit = "SECOND";
          }
          if (clonedRow.cycleTimeLimit2Unit === "%") {
            clonedRow.cycleTimeLimit2Unit = "PERCENTAGE";
          }

          if (clonedRow.weightUnit)
            clonedRow.weightUnit = clonedRow.weightUnit.toUpperCase() + "S";

          if (clonedRow.contractedCycleTime)
            clonedRow.contractedCycleTime =
              parseInt(clonedRow.contractedCycleTime) * 10;

          if (clonedRow.toolmakerContractedCycleTime)
            clonedRow.toolmakerContractedCycleTime =
              parseInt(clonedRow.toolmakerContractedCycleTime) * 10;
          clonedRow.equipmentCode = clonedRow.equipmentCode.trim();

          return clonedRow;
        });
        dataSend.push(...sendDataItem);
      });
      this.dataImportToServer = dataSend;
    },
    closeModal() {
      $("#op-confirm-unfilled").modal("hide");
    },
    checkingExisted() {
      this.setDataToImportServer();
      // checking tooling is existed
      let allToolingId = [];
      const indexToolingIdVal = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      const indexToolingId = indexToolingIdVal >= 0 ? indexToolingIdVal : 0;
      this.fileListByMappedHeader.forEach((fileElement) => {
        allToolingId = fileElement.data.map((item) => item[indexToolingId]);
        // allToolingId.push(...fileElement.data.map(item => item[0]));
        // allToolingId.push(x)
      });
      var param = encodeURI(allToolingId.join(","));
      axios
        .get("/api/molds/check-exists?moldCodes=" + param)
        .then((response) => {
          console.log("response check exist", response);
          if (response.data.length === 0) {
            this.importIntoServer();
          } else {
            this.notificationForm.allRegisteredTooling = response.data;
            let toolingRawData = this.notificationForm.allRegisteredTooling[0];
            this.notificationForm.registeredTooling.id = toolingRawData.name;
            this.notificationForm.registeredTooling.realId = toolingRawData.id;
            this.notificationForm.isShow = true;
          }
        })
        .catch(() => {
          this.errorImportMessage =
            "We have detected abnormal data in your tooling form. Please contact our team at support@emoldino.com for further assistance.";
          $("#op-confirm-unfilled").modal("show");
        });
    },
    checkingDuplicated() {
      // checking duplicate until have next duplicate tooling
      const indexToolingIdVal = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      const indexToolingId = indexToolingIdVal >= 0 ? indexToolingIdVal : 0;

      this.duplicatedForm.duplicatedTooling.id = null;
      this.duplicatedForm.duplicatedTooling.rowIndex = null;
      this.duplicatedForm.duplicatedTooling.fileIndex = null;

      this.duplicatedForm.nextDuplicatedTooling.id = null;
      this.duplicatedForm.nextDuplicatedTooling.rowIndex = null;
      this.duplicatedForm.nextDuplicatedTooling.fileIndex = null;

      this.fileListByMappedHeader.forEach((fileElement, fileIndex) => {
        if (
          this.duplicatedForm.duplicatedTooling.id &&
          this.duplicatedForm.nextDuplicatedTooling.id
        ) {
          return;
        }
        fileElement.data.forEach((row, rowIndex) => {
          if (
            this.duplicatedForm.duplicatedTooling.id &&
            this.duplicatedForm.nextDuplicatedTooling.id
          ) {
            return;
          }
          if (this.isDuplicatedTooling(rowIndex, fileIndex)) {
            if (!this.duplicatedForm.duplicatedTooling.id) {
              this.duplicatedForm.duplicatedTooling.id = row[indexToolingId];
              this.duplicatedForm.duplicatedTooling.rowIndex = rowIndex;
              this.duplicatedForm.duplicatedTooling.fileIndex = fileIndex;
            } else if (
              !this.duplicatedForm.nextDuplicatedTooling.id &&
              !Common.eqTextKey(
                this.duplicatedForm.duplicatedTooling.id,
                row[indexToolingId]
              )
            ) {
              this.duplicatedForm.nextDuplicatedTooling.id =
                row[indexToolingId];
              this.duplicatedForm.nextDuplicatedTooling.rowIndex = rowIndex;
              this.duplicatedForm.nextDuplicatedTooling.fileIndex = fileIndex;
            }
          }
        });
      });

      if (this.duplicatedForm.duplicatedTooling.id) {
        this.duplicatedForm.isShow = true;
      } else {
        this.duplicatedForm.isShow = false;
        this.$emit("hide-duplicated-form");
        // this.checkingExisted();
      }
    },
    deleteToolingById(toolingId, deleteDuplicate) {
      const indexToolingIdVal = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      const indexToolingId = indexToolingIdVal >= 0 ? indexToolingIdVal : 0;

      while (true) {
        let findOut = false;
        let numTooling = 0;
        this.fileListByMappedHeader.forEach((fileElement, fileIndex) => {
          fileElement.data.forEach((row, rowIndex) => {
            if (Common.eqTextKey(toolingId, row[indexToolingId])) {
              numTooling += 1;
              if (!deleteDuplicate || numTooling > 1) {
                this.fileListByMappedHeader[fileIndex].data.splice(rowIndex, 1);
                findOut = true;
              }
            }
          });
        });

        if (findOut === false) {
          break;
        }
      }
    },
    closeDuplicatedForm() {
      this.duplicatedForm.isShow = false;
      this.$emit("hide-duplicated-form");
    },
    retryDuplicated() {
      this.duplicatedForm.isRetry = true;
      this.closeDuplicatedForm();
    },
    retryRegistered() {
      this.duplicatedForm.isRegRetry = true;
      this.notificationForm.isShow = false;
    },
    skipDuplicate() {
      // delete all of duplicate for this
      this.deleteToolingById(this.duplicatedForm.duplicatedTooling.id, true);
      if (!this.duplicatedForm.isApplyToAll) {
        this.checkingDuplicated();
      } else {
        while (true) {
          this.checkingDuplicated();
          if (!this.duplicatedForm.isShow) {
            break;
          }
          this.deleteToolingById(
            this.duplicatedForm.duplicatedTooling.id,
            true
          );
        }
      }
    },
    closeNotificationForm() {
      this.notificationForm.isShow = false;
    },
    setRegisteredTooling() {
      // delete element in all registered tooling
      this.notificationForm.allRegisteredTooling.forEach(
        (toolingItem, index) => {
          if (toolingItem.name === this.notificationForm.registeredTooling.id) {
            this.notificationForm.allRegisteredTooling.splice(index, 1);
          }
        }
      );

      this.notificationForm.registeredTooling.id = null;
      this.notificationForm.registeredTooling.realId = null;
      if (this.notificationForm.allRegisteredTooling.length > 0) {
        const newRegisteredTooling =
          this.notificationForm.allRegisteredTooling[0];
        this.notificationForm.registeredTooling.id = newRegisteredTooling.name;
        this.notificationForm.registeredTooling.realId =
          newRegisteredTooling.id;
      }
    },
    isNaturalNumber(str) {
      var pattern = /^(0|([1-9]\d*))$/;
      return pattern.test(str);
    },
    replaceRegisteredTooling() {
      this.dataImportToServer.forEach((importItem) => {
        if (
          this.notificationForm.registeredTooling.id ===
          importItem.equipmentCode
        ) {
          importItem.id = this.notificationForm.registeredTooling.realId;
          this.setRegisteredTooling();
        }
      });

      if (!this.notificationForm.isApplyToAll) {
        if (!this.notificationForm.registeredTooling.id) {
          this.notificationForm.isShow = false;
          this.importIntoServer();
        }
      } else {
        while (true) {
          this.dataImportToServer.forEach((importItem) => {
            if (
              this.notificationForm.registeredTooling.id ===
              importItem.equipmentCode
            ) {
              importItem.id = this.notificationForm.registeredTooling.realId;
            }
          });
          this.setRegisteredTooling();
          if (!this.notificationForm.registeredTooling.id) {
            this.notificationForm.isShow = false;
            this.importIntoServer();
            break;
          }
        }
      }
    },
    skipRegisteredTooling() {
      this.dataImportToServer.forEach((importItem, importItemIndex) => {
        if (
          this.notificationForm.registeredTooling.id ===
          importItem.equipmentCode
        ) {
          // delete in table
          this.deleteToolingById(this.notificationForm.registeredTooling.id);
          // delete in data send in server
          this.dataImportToServer.splice(importItemIndex, 1);
        }
      });
      if (!this.notificationForm.isApplyToAll) {
        this.setRegisteredTooling();
        if (!this.notificationForm.registeredTooling.id) {
          this.notificationForm.isShow = false;
          this.importIntoServer();
        }
      } else {
        while (true) {
          this.setRegisteredTooling();
          if (!this.notificationForm.registeredTooling.id) {
            this.notificationForm.isShow = false;
            this.importIntoServer();
            break;
          }
          this.dataImportToServer.forEach((importItem, importItemIndex) => {
            if (
              this.notificationForm.registeredTooling.id ===
              importItem.equipmentCode
            ) {
              // delete in table
              this.deleteToolingById(
                this.notificationForm.registeredTooling.id
              );
              // delete in data send in server
              this.dataImportToServer.splice(importItemIndex, 1);
            }
          });
        }
      }
    },

    labelOfChildrenHeader(header) {
      const mappedHeader = header.mappedHeader;
      const childrenIndex = header.mappedChildrenIndex;
      const childrenFieldIndex = header.mappedChildrenFieldIndex;
      return `${mappedHeader.name} ${childrenIndex + 1} - ${
        mappedHeader.children[childrenIndex][childrenFieldIndex].name
      }`;
    },

    lengthOfHeader(header) {
      if (header.mappedHeader.type !== "parent") {
        let length =
          header.maxLength > header.mappedHeader.name.length
            ? header.maxLength
            : header.mappedHeader.name.length;
        if (header.mappedHeader.rules.minLength) {
          if (length < header.mappedHeader.rules.minLength) {
            length = header.mappedHeader.rules.minLength;
          }
          if (length > header.mappedHeader.rules.maxLength) {
            length = header.mappedHeader.rules.maxLength;
          }
        }
        return length < 10 ? 10 : length;
      } else {
        return this.labelOfChildrenHeader(header).length;
      }
    },
    isValidCell(header, value, row) {
      if (header.mappedHeader.type !== "parent") {
        let mappedHeader = header.mappedHeader;

        if (mappedHeader.rules.required) {
          if (!value) {
            if (
              header.mappedHeader.code === "contractedCycleTime" ||
              header.mappedHeader.code === "toolmakerContractedCycleTime"
            ) {
              const currentFile =
                this.fileListByMappedHeader[this.currentFileIndex];
              const contractedCycleTimeIndex = currentFile.headers.findIndex(
                (data) =>
                  data.mappedHeader &&
                  data.mappedHeader.code === "contractedCycleTime"
              );
              const toolmakerContractedCycleTimeIndex =
                currentFile.headers.findIndex(
                  (data) =>
                    data.mappedHeader &&
                    data.mappedHeader.code === "toolmakerContractedCycleTime"
                );
              return !(
                this.isEmpty(row[contractedCycleTimeIndex]) &&
                this.isEmpty(row[toolmakerContractedCycleTimeIndex])
              );
            }
            return false;
          }
        } else {
          if (!value) {
            return true;
          }
        }
        if (header.mappedHeader.code === "accumulatedShots") {
          const existingTooling = _.find(this.existingToolings, {
            equipmentCode: row[0],
          });
          if (value && !_.isEmpty(existingTooling)) {
            return (
              Number(existingTooling?.accumulatedShot, 10) <= Number(value, 10)
            );
          }
        }
        if (mappedHeader.code === "productionDays") {
          if (value < 1 || value > 7 || value % 1 !== 0) {
            return false;
          }
        }
        if (header.mappedHeader.code === "engineerEmails") {
          return this.checkValidEngineers(value);
        }
        if (header.mappedHeader.code === "locationCode") {
          return this.checkValidLocation(value);
        }
        if (header.mappedHeader.code === "toolMakerCompanyCode") {
          return this.checkValidToolMaker(value);
        }

        if (header.mappedHeader.code === "supplierCompanyCode") {
          return this.checkValidSupplier(value);
        }

        if (header.mappedHeader.code === "counterCode") {
          return this.checkValidCounter(value, row, header);
        }

        if (header.mappedHeader.code === "uptimeTarget") {
          if (value % 1 !== 0 || value < 0) {
            return false;
          }
        }
        if (mappedHeader.code === "shiftsPerDay") {
          if (value < 1 || value > 24) {
            return false;
          }
        }
        if (mappedHeader.code === "madeYear") {
          if (value < 1900 || value % 1 !== 0) {
            return false;
          }
        }
        if (mappedHeader.code === "totalCavities") {
          if (
            value < 1 ||
            value > 99 ||
            value % 1 !== 0 ||
            value.includes(".")
          ) {
            return false;
          }
        }

        if (
          mappedHeader.code === "size_w" ||
          mappedHeader.code === "size_l" ||
          mappedHeader.code === "size_h" ||
          mappedHeader.code === "weight" ||
          mappedHeader.code === "quotedMachineTonnage" ||
          mappedHeader.code === "weightRunner"
        ) {
          if (value < 0 || isNaN(value)) {
            return false;
          }
        }

        // if (mappedHeader.rules.options) {
        //   const optionValues = mappedHeader.rules.options.map(option => option.value);
        //   if (!optionValues.includes(value.trim())) {
        //     return false;
        //   }
        // }

        if (mappedHeader.rules.options) {
          const optionValues = mappedHeader.rules.options.map(
            (option) => option.value
          );
          // const optionNames = mappedHeader.rules.options.map(option => option.name);
          // if (value && !optionValues.includes(value.trim()) && !optionNames.includes(value.trim())) {
          if (!optionValues.includes(value)) {
            return false;
          }
        }

        if (mappedHeader.rules.isNumber) {
          if (isNaN(value) || value < 0) {
            return false;
          }
        }

        if (mappedHeader.rules.isInteger) {
          if (value % 1 !== 0 || value < 0 || value.includes(".")) {
            return false;
          }
        }

        if (value && mappedHeader.rules.dateFormat != null) {
          if (!moment(value, mappedHeader.rules.dateFormat, true).isValid()) {
            return false;
          }
        }

        if (!isNaN(value)) {
          if (mappedHeader.rules.minValue != null) {
            if (value < mappedHeader.rules.minValue) {
              return false;
            }
          }
          if (mappedHeader.rules.gtValue != null) {
            if (value <= mappedHeader.rules.gtValue) {
              return false;
            }
          }
          if (mappedHeader.rules.maxValue != null) {
            if (value > mappedHeader.rules.maxValue) {
              return false;
            }
          }
        }
      } else {
        const mappedChildrenHeader =
          header.mappedHeader.children[header.mappedChildrenIndex][
            header.mappedChildrenFieldIndex
          ];
        if (
          mappedChildrenHeader.rules.required &&
          header.mappedChildrenIndex === 0
        ) {
          if (!value) return false;
        } /*else {
                      if (!value){
                        return true;
                      }
                    } */
        if (
          mappedChildrenHeader.code === "partCode" &&
          header.mappedChildrenIndex === 0
        ) {
          return this.checkValidPart(value);
        }

        if (
          mappedChildrenHeader.rules.options &&
          header.mappedChildrenIndex === 0
        ) {
          if (!mappedChildrenHeader.rules.options.includes(value)) {
            return false;
          }
        }

        if (mappedChildrenHeader.rules.isNumber) {
          if (isNaN(value)) {
            return false;
          }
        }

        if (mappedChildrenHeader.rules.isInteger) {
          if (value % 1 !== 0 || value < 0 || value.includes(".")) {
            return false;
          }
        }
        if (!isNaN(value)) {
          if (mappedChildrenHeader.rules.minValue != null) {
            if (value < mappedChildrenHeader.rules.minValue) {
              return false;
            }
          }
          if (mappedChildrenHeader.rules.maxValue != null) {
            if (value > mappedChildrenHeader.rules.maxValue) {
              return false;
            }
          }
          if (
            mappedChildrenHeader.rules.referenceConditionField &&
            mappedChildrenHeader.rules.referenceConditionField.length > 0 &&
            row
          ) {
            for (
              let i = 0;
              i < mappedChildrenHeader.rules.referenceConditionField.length;
              i++
            ) {
              let referenceField =
                mappedChildrenHeader.rules.referenceConditionField[i];
              let indexReferenceField = this.fileListByMappedHeader[
                this.currentFileIndex
              ].headers.findIndex(
                (h) =>
                  h.mappedChildrenIndex == header.mappedChildrenIndex &&
                  h.mappedChildrenField == referenceField.code
              );
              if (indexReferenceField == -1) {
                indexReferenceField = this.fileListByMappedHeader[
                  this.currentFileIndex
                ].headers.findIndex(
                  (h) =>
                    h.mappedChildrenIndex == undefined &&
                    h.mappedHeader.code == referenceField.code
                );
              }
              if (indexReferenceField != -1) {
                let referentValue = row[indexReferenceField];
                if (
                  referentValue != null &&
                  !isNaN(referentValue) &&
                  referenceField.operator
                ) {
                  if (
                    !this.compareValue(
                      value,
                      referenceField.operator,
                      referentValue
                    )
                  ) {
                    return false;
                  }
                }
              }
            }
          }
        }
      }
      return true;
    },
    isEmpty(value) {
      return value === undefined || false || value.trim().length === 0;
    },
    compareValue(value1, op, value2) {
      value1 = !isNaN(value1) ? Number(value1) : value1;
      value2 = !isNaN(value2) ? Number(value2) : value2;
      switch ((op || "").toLowerCase()) {
        case "eq":
          return value1 == value2;
        case "gte":
          return value1 >= value2;
        case "gt":
          return value1 > value2;
        case "lt":
          return value1 < value2;
        case "lte":
          return value1 <= value2;
      }
      return true;
    },
    operatorToText(op) {
      switch ((op || "").toLowerCase()) {
        case "eq":
          return "equal";
        case "gte":
          return "greater than or equal";
        case "gt":
          return "greater than";
        case "lt":
          return "less than";
        case "lte":
          return "less than or equal";
      }
      return "";
    },
    getErrorFieldName(header) {
      if (header.mappedHeader.type !== "parent") {
        switch (header.mappedHeader.code) {
          case "locationCode":
            return "location";
            break;
          case "toolMakerCompanyCode":
            return "toolmaker";
            break;
          case "supplierCompanyCode":
            return "supplier";
            break;
          case "partCode":
            return "part";
            break;
          default:
            return null;
        }
      } else {
        const mappedChildrenHeader =
          header.mappedHeader.children[header.mappedChildrenIndex][
            header.mappedChildrenFieldIndex
          ];
        switch (mappedChildrenHeader.code) {
          case "partCode":
            return "part";
            break;
          default:
            return null;
        }
      }
    },
    errorMessage(header, value, row, rowIndex) {
      if (header.mappedHeader.type !== "parent") {
        let mappedHeader = header.mappedHeader;
        if (mappedHeader.rules.required) {
          if (!value) {
            if (
              header.mappedHeader.code === "contractedCycleTime" ||
              header.mappedHeader.code === "toolmakerContractedCycleTime"
            ) {
              const currentFile =
                this.fileListByMappedHeader[this.currentFileIndex];
              const contractedCycleTimeIndex = currentFile.headers.findIndex(
                (data) =>
                  data.mappedHeader &&
                  data.mappedHeader.code === "contractedCycleTime"
              );
              const toolmakerContractedCycleTimeIndex =
                currentFile.headers.findIndex(
                  (data) =>
                    data.mappedHeader &&
                    data.mappedHeader.code === "toolmakerContractedCycleTime"
                );
              if (
                this.isEmpty(row[contractedCycleTimeIndex]) &&
                this.isEmpty(row[toolmakerContractedCycleTimeIndex])
              ) {
                return "At least one approved cycle time is required";
              }
            }
            return `${mappedHeader.name} is required.`;
          }
        }
        if (mappedHeader.code === "engineerEmails") {
          const array = this.checkErrorMessageEngineers(value);
          if (array && array.length > 0) {
            const invalidMessage = array.join(", ");
            return `Engineer ${invalidMessage} is not registered in the system.`;
          }
        }
        if (header.mappedHeader.code === "accumulatedShots") {
          const existingTooling = _.find(this.existingToolings, {
            equipmentCode: row[0],
          });
          return `Invalid data value. Accumulated Shots must be equal or greater than tooling's current accumulated shots (${
            Common.formatNumber(existingTooling?.accumulatedShot) || 0
          } shots)`;
        }
        if (mappedHeader.code === "locationCode") {
          return "Plant is not registered in the system.";
        }

        if (mappedHeader.code === "toolMakerCompanyCode") {
          return "Toolmaker is not registered in the system.";
        }
        if (mappedHeader.code === "supplierCompanyCode") {
          return "Supplier is not registered in the system.";
        }

        if (mappedHeader.rules.options && mappedHeader.code !== "counterCode") {
          if (
            !mappedHeader.rules.options.includes(value) &&
            !mappedHeader.rules.options
              .map((data) => data.value)
              .includes(value)
          ) {
            let validOptionString = mappedHeader.rules.options
              .map((option) => option.name)
              .join(", ");
            return `Invalid data value. ${mappedHeader.name} value includes ${validOptionString}.`;
          }
        }

        if (
          mappedHeader.code === "labour" ||
          mappedHeader.code === "uptimeTarget"
        ) {
          if (value < 0 || value % 1 !== 0) {
            return `Invalid data format. This field can only contain positive integer numbers.`;
          }
        }

        if (mappedHeader.code === "counterCode") {
          const indexToolingId = this.currentFile.headers.findIndex(
            (data) =>
              data.mappedHeader && data.mappedHeader.name === "Tooling ID"
          );
          const indexCounterId = this.currentFile.headers.findIndex(
            (data) =>
              data.mappedHeader && data.mappedHeader.name === "Sensor ID"
          );
          const valueData = this.labelOfCell(header, value);
          const valid = this.listCounter.filter((res) =>
            Common.eqTextKey(res.counterCode, valueData)
          );
          if (valid.length === 0) {
            return "Sensor ID is not registered in the system.";
          }
          // const invalidCounterMatched = valid.filter(res => res.toolingCode && res.toolingCode !== row[indexToolingId]);
          const duplicateRowIndex = this.currentFile.data
            .map((data, indexData) =>
              Common.eqTextKey(
                this.labelOfCell(header, data[indexCounterId]),
                valueData
              ) && indexData !== rowIndex
                ? indexData
                : -1
            )
            .filter((i) => i !== -1);
          const duplicate = [];
          duplicateRowIndex.forEach((index) =>
            duplicate.push(this.currentFile.data[index][indexToolingId])
          );
          let invalidToolingString = [];
          if (duplicate.length > 0) {
            // if (invalidCounterMatched.length > 0) {
            //   invalidCounterMatched.forEach(invalidCounter => invalidToolingString.push(invalidCounter.toolingCode));
            // }
            if (duplicate.length > 0) {
              invalidToolingString.push(...duplicate);
            }
            let uniq = [...new Set(invalidToolingString)];

            if (uniq.length > 0) {
              if (uniq.length === 1) {
                return "Sensor ID is already matched with tooling " + uniq[0];
              } else if (uniq.length === 2) {
                return (
                  "Sensor ID is already matched with tooling " +
                  uniq[0] +
                  " and tooling " +
                  uniq[1]
                );
              } else {
                let errorMessage = "";
                uniq.forEach((data, index) => {
                  if (index === 0) {
                    errorMessage += data;
                  } else if (index === uniq.length - 1) {
                    errorMessage += " and tooling " + data;
                  } else {
                    errorMessage += ", tooling " + data;
                  }
                });
                return (
                  "Sensor ID is already matched with tooling " + errorMessage
                );
              }
            }
          }
        }

        if (mappedHeader.rules.isNumber) {
          if (mappedHeader.code === "shiftsPerDay") {
            if (value < 1 || value > 24) {
              return `Invalid data format. This field can only contain numbers between 1 and 24.`;
            }
          }
          if (isNaN(value) || value < 0) {
            return `Invalid data format. This field can only contain positive numbers.`;
          }
        }
        if (mappedHeader.code === "uptimeTarget") {
          return "Invalid data format. This field can only contain positive integer numbers.";
        }

        if (value && mappedHeader.rules.dateFormat != null) {
          if (!moment(value, mappedHeader.rules.dateFormat, true).isValid()) {
            return `Invalid date format. This date format should be ${mappedHeader.rules.dateFormat.toLowerCase()}.`;
          }
        }

        if (mappedHeader.rules.isInteger) {
          if (mappedHeader.code === "productionDays") {
            if (value < 1 || value > 7 || value % 1 !== 0) {
              return `Invalid data format. This field can only contain integer numbers between 1 and 7.`;
            }
          }
          if (mappedHeader.code === "madeYear") {
            if (value < 1900 || value % 1 !== 0) {
              return `Invalid data format. This field can only contain integer numbers that is greater than 1900.`;
            }
          }
          if (mappedHeader.code === "totalCavities") {
            if (value < 1 || value > 99 || value % 1 !== 0) {
              return `Invalid data format. This field can only contain integer numbers between 1 and 99.`;
            }
          }

          if (value % 1 !== 0 || value < 0 || value.includes(".")) {
            return "Invalid data format. This field can only contain positive integer numbers.";
          }
        }

        if (!isNaN(value)) {
          if (mappedHeader.rules.minValue != null) {
            if (value < mappedHeader.rules.minValue) {
              return `${mappedHeader.name} must be greater than or equal to ${mappedHeader.rules.minValue}.`;
            }
          }
          if (mappedHeader.rules.gtValue != null) {
            if (value <= mappedHeader.rules.gtValue) {
              return `${mappedHeader.name} must be greater than ${mappedHeader.rules.gtValue}.`;
            }
          }
          if (mappedHeader.rules.maxValue != null) {
            if (value > mappedHeader.rules.maxValue) {
              return `${mappedHeader.name} must be less than or equal to ${mappedHeader.rules.maxValue}.`;
            }
          }
        }
      } else {
        const mappedChildrenHeader =
          header.mappedHeader.children[header.mappedChildrenIndex][
            header.mappedChildrenFieldIndex
          ];

        if (
          mappedChildrenHeader.rules.required &&
          header.mappedChildrenIndex === 0
        ) {
          if (!value) {
            return `${header.mappedHeader.name} ${
              header.mappedChildrenIndex + 1
            } - ${mappedChildrenHeader.name} is required.`;
          }
        }
        if (
          mappedChildrenHeader.code === "partCode" &&
          header.mappedChildrenIndex === 0
        ) {
          return `Part is not registered in the system.`;
        }

        if (
          mappedChildrenHeader.rules.options &&
          header.mappedChildrenIndex === 0
        ) {
          if (!mappedChildrenHeader.rules.options.includes(value)) {
            return `${header.mappedHeader.name} ${
              header.mappedChildrenIndex + 1
            } - ${mappedChildrenHeader.name} is required.`;
          }
        }

        if (mappedChildrenHeader.rules.isNumber) {
          if (isNaN(value) || value < 0) {
            return `Invalid data format. This field can only contain positive numbers.`;
          }
        }
        if (mappedChildrenHeader.rules.isInteger) {
          if (value % 1 !== 0 || value < 0 || value.includes(".")) {
            return `Invalid data format. This field can only contain positive integer numbers.`;
          }
        }
        if (!isNaN(value)) {
          if (mappedChildrenHeader.rules.minValue != null) {
            if (value < mappedChildrenHeader.rules.minValue) {
              return `${header.mappedHeader.name} ${
                header.mappedChildrenIndex + 1
              } - ${
                mappedChildrenHeader.name
              } must be greater than or equal to ${
                mappedChildrenHeader.rules.minValue
              }.`;
            }
          }
          if (mappedChildrenHeader.rules.maxValue != null) {
            if (value > mappedChildrenHeader.rules.maxValue) {
              return `${header.mappedHeader.name} ${
                header.mappedChildrenIndex + 1
              } - ${mappedChildrenHeader.name} must be less than or equal to ${
                mappedChildrenHeader.rules.maxValue
              }.`;
            }
          }

          if (
            mappedChildrenHeader.rules.referenceConditionField &&
            mappedChildrenHeader.rules.referenceConditionField.length > 0 &&
            row
          ) {
            for (
              let i = 0;
              i < mappedChildrenHeader.rules.referenceConditionField.length;
              i++
            ) {
              let referenceField =
                mappedChildrenHeader.rules.referenceConditionField[i];
              let indexReferenceField = this.currentFile.headers.findIndex(
                (h) =>
                  h.mappedChildrenIndex == header.mappedChildrenIndex &&
                  h.mappedChildrenField == referenceField.code
              );
              if (indexReferenceField == -1) {
                indexReferenceField = this.fileListByMappedHeader[
                  this.currentFileIndex
                ].headers.findIndex(
                  (h) =>
                    h.mappedChildrenIndex == undefined &&
                    h.mappedHeader.code == referenceField.code
                );
              }
              if (indexReferenceField != -1) {
                let referenceFieldHeader =
                  this.currentFile.headers[indexReferenceField];
                let referentValue = row[indexReferenceField];
                if (
                  referentValue != null &&
                  !isNaN(referentValue) &&
                  referenceField.operator
                ) {
                  if (
                    !this.compareValue(
                      value,
                      referenceField.operator,
                      referentValue
                    )
                  ) {
                    let referentFieldName =
                      referenceFieldHeader.mappedChildrenIndex == null
                        ? `${referenceFieldHeader.name}`
                        : `${referenceFieldHeader.mappedHeader.name} ${
                            referenceFieldHeader.mappedChildrenIndex + 1
                          } - ${referenceFieldHeader.name}`;
                    return (
                      `${header.mappedHeader.name} ${
                        header.mappedChildrenIndex + 1
                      } - ${mappedChildrenHeader.name}` +
                      ` must be ${this.operatorToText(
                        referenceField.operator
                      )} to ` +
                      `${referentFieldName}.`
                    );
                    // + `${referenceFieldHeader.mappedHeader.name} ${referenceFieldHeader.mappedChildrenIndex + 1} - ${referenceFieldHeader.name}.`;
                  }
                }
              }
            }
          }
        }
      }

      return "Unknown error";
    },
    showFormEditCell() {
      console.log("show form edit cell");
    },
    focusInInput(index, headerIndex) {
      this.focus.index = index;
      this.focus.headerIndex = headerIndex;
    },
    isBeingFocus(index, headerIndex) {
      return (
        index === this.focus.index && headerIndex === this.focus.headerIndex
      );
    },
    setTempValue(event, index, headerIndex) {
      if (event.keyCode === 13) {
        this.fileListByMappedHeader[this.currentFileIndex].data[index][
          headerIndex
        ] = event.target.value;
      }
      this.tempInput[index] = { [headerIndex]: event.target.value };
    },
    labelOfCell(header, value) {
      if (header.mappedHeader.code === "counterCode") {
        const counterData = this.selectedList.counter.filter(
          (option) => option.value === value
        );
        if (counterData && counterData.length > 0) {
          return counterData[0].name;
        }
      } else if (
        header.mappedChildrenField &&
        header.mappedChildrenField === "partCode"
      ) {
        const partData = this.selectedList.part.filter(
          (option) => option.value === value
        );
        if (partData && partData.length > 0) {
          return partData[0].name;
        }
      } else if (header.mappedHeader.code === "locationCode") {
        const partData = this.selectedList.location.filter(
          (option) => option.value === value
        );
        if (partData && partData.length > 0) {
          return partData[0].name;
        }
      } else if (header.mappedHeader.code === "engineerEmails") {
        const partData = this.selectedList.engineer.filter(
          (option) => option.value === value
        );
        if (partData && partData.length > 0) {
          return partData[0].name;
        }
      } else if (header.mappedHeader.code === "toolMakerCompanyCode") {
        const partData = this.selectedList.toolMaker.filter(
          (option) => option.value === value
        );
        if (partData && partData.length > 0) {
          return partData[0].name;
        }
      } else if (header.mappedHeader.code === "supplierCompanyCode") {
        const partData = this.selectedList.supplier.filter(
          (option) => option.value === value
        );
        if (partData && partData.length > 0) {
          return partData[0].name;
        }
      }
      if (!header.mappedHeader.rules.options) return value;
      const mappedOption = header.mappedHeader.rules.options.filter(
        (option) => option.value === value
      );
      if (mappedOption && mappedOption.length > 0) {
        return mappedOption[0].name;
      }
      return value;
    },
    isDuplicatedTooling(rowIndex, currentFileIndex) {
      const indexToolingIdVal = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      const indexToolingId = indexToolingIdVal >= 0 ? indexToolingIdVal : 0;

      // checking in this file
      let isDuplicated = false;
      this.fileListByMappedHeader.forEach((fileElement, fileIndex) => {
        if (fileIndex !== currentFileIndex) {
          fileElement.data.forEach((row) => {
            if (
              Common.eqTextKey(
                row[indexToolingId],
                this.fileListByMappedHeader[currentFileIndex].data[rowIndex][
                  indexToolingId
                ]
              )
            ) {
              isDuplicated = true;
            }
          });
        } else {
          fileElement.data.forEach((row, index) => {
            if (
              Common.eqTextKey(
                row[indexToolingId],
                this.fileListByMappedHeader[currentFileIndex].data[rowIndex][
                  indexToolingId
                ]
              ) &&
              rowIndex !== index
            ) {
              isDuplicated = true;
            }
          });
        }
      });
      return isDuplicated;
    },
    showWarningDeleteForm(rowIndex) {
      const indexToolingIdVal = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      const indexToolingId = indexToolingIdVal >= 0 ? indexToolingIdVal : 0;

      this.deleteForm.toolingId =
        this.currentFile.data[rowIndex][indexToolingId];
      this.deleteForm.toolingIndex = rowIndex;
      this.deleteForm.isShow = true;
    },

    hideWarningDeleteForm() {
      this.deleteForm.isShow = false;
    },
    deleteTooling() {
      this.deleteForm.isShow = false;
      this.currentFile.data.splice(this.deleteForm.toolingIndex, 1);
      this.invalidData[this.currentFileIndex].splice(
        this.deleteForm.toolingIndex,
        1
      );
    },
    calcIdInvalidDataElement(isBack = false) {
      let fileElement = this.fileListByMappedHeader[this.currentFileIndex];
      const headers = fileElement.headers;
      let totalFailed = this.countFailedValidationCurrentFile;
      // calculate index of current
      if (isBack) {
        if (this.currentInvalidIndex === null) {
          this.currentInvalidIndex = totalFailed - 1;
        } else {
          this.currentInvalidIndex =
            (this.currentInvalidIndex + totalFailed - 1) % totalFailed;
        }
      } else {
        if (this.currentInvalidIndex === null) {
          this.currentInvalidIndex = 0;
        } else {
          this.currentInvalidIndex =
            (this.currentInvalidIndex + 1) % totalFailed;
        }
      }
      let tmpIndex = 0;
      for (let index = 0; index < fileElement.data.length; index++) {
        let row = fileElement.data[index];
        for (let headerIndex = 0; headerIndex < headers.length; headerIndex++) {
          let cell = row[headerIndex];
          if (!this.isValidCell(headers[headerIndex], cell, row)) {
            tmpIndex = (tmpIndex + 1) % totalFailed;
            this.isOpenSelect = null;
            if (this.currentInvalidIndex === tmpIndex) {
              this.focusIndex = index;
              this.focusHeaderIndex = headerIndex;
              this.setAutoFocus(index, headerIndex);
              return `#cell-${index}-${headerIndex}`;
            }
          }
        }
      }
      return "";
    },
    setAutoFocus(index, headerIndex) {
      this.$nextTick(() => {
        this.isShowing = true;
        this.focusIndex = index;
        this.focusHeaderIndex = headerIndex;
        this.isOpenSelect = `cell-${index}-${headerIndex}`;
        if (this.$refs[`cell-${index}-${headerIndex}`]) {
          setTimeout(() => {
            this.$refs[`cell-${index}-${headerIndex}`][0].focus();
          }, 100);
        } else {
          setTimeout(() => {
            this.scroll(index + "-" + headerIndex);
          }, 100);
        }
      });
    },
    hideForm() {
      if (this.isShowing === true) {
        this.isOpenSelect = null;
        this.isShowing = false;
        this.removeFocusCell();
      }
    },
    changeSelectState(state) {
      if (this.isOpenSelect === state) {
        this.isOpenSelect = null;
      } else {
        this.isOpenSelect = state;
      }
    },
    closeOption() {
      this.isOpenSelect = null;
      this.isShowing = false;
      this.removeFocusCell();
      this.trigger += 1;
    },
    isFocusCell(index, headerIndex) {
      return index === this.focusIndex && headerIndex === this.focusHeaderIndex;
    },
    removeFocusCell() {
      this.focusIndex = null;
      this.focusHeaderIndex = null;
    },
    previousInvalidData() {
      location.hash = this.calcIdInvalidDataElement(true);
    },
    nextInvalidData() {
      location.hash = this.calcIdInvalidDataElement();
    },
    checkMatchedCounter(firstCheck = false) {
      if (firstCheck) {
        this.matchedCounterForm.counterSkipArr = [];
        this.matchedCounterForm.counterRemoveArr = [];
      }
      const indexCounterId = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Sensor ID"
      );
      const indexToolingId = this.currentFile.headers.findIndex(
        (data) => data.mappedHeader && data.mappedHeader.name === "Tooling ID"
      );
      this.matchedCounterForm.counterId = null;
      this.matchedCounterForm.counterCode = null;
      this.matchedCounterForm.toolingCode = null;

      const header = this.currentFile.headers[indexCounterId];

      for (const row of this.currentFile.data) {
        const counterId = row[indexCounterId];
        if (
          this.matchedCounterForm.counterSkipArr.includes(counterId) ||
          this.matchedCounterForm.counterRemoveArr.includes(counterId)
        )
          continue;
        const counterValue = this.labelOfCell(header, counterId);
        const valid = this.listCounter.filter((res) =>
          Common.eqTextKey(res.counterCode, counterValue)
        );
        const invalidCounterMatched = valid.filter(
          (res) => res.toolingCode && res.toolingCode !== row[indexToolingId]
        );
        if (invalidCounterMatched.length > 0) {
          this.matchedCounterForm.counterId = counterId;
          this.matchedCounterForm.counterCode = counterValue;
          this.matchedCounterForm.toolingCode =
            invalidCounterMatched[0].toolingCode;
          break;
        }
      }
      if (this.matchedCounterForm.counterId) {
        this.matchedCounterForm.isShow = true;
      } else {
        this.matchedCounterForm.isShow = false;
        this.$emit("open-check-existed-form");
      }
    },
    hideCheckMatchedCounterForm() {
      this.matchedCounterForm.isShow = false;
    },
    removeCheckMatchedCounterForm() {
      //
      this.matchedCounterForm.counterRemoveArr.push(
        this.matchedCounterForm.counterId
      );
      if (!this.matchedCounterForm.isApplyToAll) {
        this.checkMatchedCounter();
      } else {
        while (true) {
          this.checkMatchedCounter();
          if (!this.matchedCounterForm.isShow) {
            break;
          }
          this.matchedCounterForm.counterRemoveArr.push(
            this.matchedCounterForm.counterId
          );
        }
      }
    },
    overwriteMatchedCounter() {
      this.matchedCounterForm.counterSkipArr.push(
        this.matchedCounterForm.counterId
      );
      if (!this.matchedCounterForm.isApplyToAll) {
        this.checkMatchedCounter();
      } else {
        while (true) {
          this.checkMatchedCounter();
          if (!this.matchedCounterForm.isShow) {
            break;
          }
          this.matchedCounterForm.counterSkipArr.push(
            this.matchedCounterForm.counterId
          );
        }
      }
    },
    goBackImportError() {
      this.importError = false;
      this.moldCodeError = "";
      this.showImportForm = false;
    },
    continueImport() {
      const indexErrorData = this.dataImportToServer.findIndex(
        (data) => data.equipmentCode === this.moldCodeError
      );
      console.log("continueImport", indexErrorData);
      if (this.dataImportToServer.length === 1) {
        this.isImportDone = true;
        return;
      }
      if (indexErrorData !== -1) {
        let _dataImport = this.dataImportToServer;
        _dataImport.splice(0, indexErrorData + 1);
        this.dataImportToServer = _dataImport;
        this.importError = false;
        this.moldCodeError = "";
        this.showImportForm = false;
        this.importIntoServer();
      }
    },
    goBackBadRequest() {
      this.isBadRequest = false;
      this.showImportForm = false;
    },
  },
};
</script>
<style>
.ant-tooltip-placement-bottom .ant-tooltip-arrow,
.ant-tooltip-placement-bottomLeft .ant-tooltip-arrow,
.ant-tooltip-placement-bottomRight .ant-tooltip-arrow {
  border-bottom-color: #4b4b4b;
}
.ant-tooltip-inner {
  /*maching data*/
  background-color: #4b4b4b !important;
  color: #fff;
  padding: 0;
  /*border-radius: 0;*/
  box-shadow: none;
}
</style>
<style scoped>
.only-show-error {
  display: flex;
}
.only-show-error .switch-button {
  margin-right: 13px;
  display: flex;
  align-items: center;
}
.only-show-error .switch-status {
  color: #000000;
  font-size: 12pt;
  line-height: 100%;
}
.only-show-error .toggleWrapper1 {
  display: flex;
  align-items: center;
}
.toggleWrapper1 input.mobileToggle + .change-checkout-status {
  width: 41px;
}
.toggleWrapper1 input.mobileToggle + .change-checkout-status:before {
  width: 41px;
}
.toggleWrapper1 input.mobileToggle + .change-checkout-status:after {
  height: 12px;
  width: 12px;
  transform: translate(0, -50%);
  top: 50%;
  left: 2px;
}
.toggleWrapper1 input.mobileToggle:checked + .change-checkout-status:after {
  left: 26px;
  top: 3px;
  transform: unset;
}
.toggleWrapper1 input.mobileToggle:checked + .change-checkout-status:after {
}
.dropbtn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #000;
  border: 1px solid #6a4efb;
  border-radius: 3px;
  padding: 11px 13px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: #fff;
  max-width: 100%;
}
.dropbtn span {
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropbtn .field-icon img {
  max-width: 12px;
}
.dropbtn.show {
  /*border-color: #8ad4ee;*/
  outline: 0;
  /*box-shadow: 0 0 0 0.2rem rgb(32 168 216 / 25%);*/
  border: 1px solid #6a4efb !important;
}
.dropdown {
  position: relative;
  display: none;
}
.dropdown-content {
  display: none;
  position: absolute;
  min-width: 100%;
  background-color: #ffffff;
  box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2);
  /*max-height: 200px;*/
  /*min-height: 200px;*/
  padding-bottom: 10px;
  overflow-y: auto;
  z-index: 999;
}
.max-min-height {
  max-height: 200px;
  min-height: 200px;
}
.dropdown-content span {
  color: black;
  padding: 5px 16px;
  text-decoration: none;
  display: block;
  height: 32px;
  font-size: 14px;
  white-space: nowrap;
}
.dropdown-content span:hover {
  background: #e6f7ff;
}
.show + .dropdown-content {
  display: block;
}
.value {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px !important;
}
.search-container {
  display: flex;
  border: #d6dade solid 0.5px;
  margin: 10px;
  height: 32px;
}
.search-container:hover {
  border: #4b83de solid 0.5px;
  border-radius: 2px;
}
.search-container:focus,
.search-container:target {
  border: #3491ff solid 0.5px;
  border-radius: 3px;
}
.icon-search {
  text-align: center;
  padding: 6px;
  background-color: #f5f5f5;
  color: #9d9d9d;
  width: 32px;
}
.input-search {
  width: 100%;
  border: none;
  font-size: 14px;
  padding-left: 5px;
  min-width: 140px;
}
.input-search:hover,
.input-search:focus {
  border: none !important;
  outline: none;
}
.btn-overwrite {
  background-color: #3491ff;
  border-color: #3491ff;
  color: #ffffff;
  padding: 6px 8px;
  margin-left: 8px !important;
}

.btn-overwrite:hover {
  background-color: #3585e5;
  border-color: #3585e5;
}
.btn-overwrite:focus {
  background-color: #3585e5;
  border-color: #89d1fd;
}

.btn-back {
  background-color: #ffffff;
  border: 1px solid #d6dade;
  color: #595959;
  padding: 6px 8px;
}
.btn-back:focus {
  /*
      background-color: #F4F4F4;
      border: 2px solid #D6DADE;
      */
  box-shadow: unset;
}
.btn-back:active {
  background-color: #f4f4f4;
  border: 1px solid #d6dade !important;
  box-shadow: 0 0 0 1px #d6dade;
}
.btn-back:hover {
  background-color: #f4f4f4;
  border: 1px solid #595959;
}
.btn {
  border-radius: 3px;
}
.error-detect-action {
  margin-top: 15px;
  margin-left: 110px;
  margin-bottom: 15px;
  width: 170px;
  display: flex;
}
.btn-primary:active {
  box-shadow: 0 0 0 0.2rem rgb(32 168 216 / 50%);
}
.btn-primary:focus {
  box-shadow: unset;
}
</style>
