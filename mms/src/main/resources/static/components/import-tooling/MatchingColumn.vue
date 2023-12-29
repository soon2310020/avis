<!-- TODO: REMOVE AFTER STABLE VERSION IN DATA IMPORT -->
<template>
  <div class="matching-wrapper">
    <div
      v-if="canPrevious"
      class="direction-arrow previous"
      @click="previousFile"
    >
      <img
        src="/images/import-tooling/back-next-button.svg"
        alt="previous button"
      />
    </div>
    <div v-else class="direction-arrow"></div>
    <div class="matching-column" v-if="!!currentFile">
      <div class="table-header" @click="resetFocusIndex">
        <div class="left-header">
          <span> <span v-text="resources['file_name']"></span></span
          ><span>: {{ currentFile.fileName }}</span>
        </div>
        <div class="right-header" style="width: 370px">
          <div
            class="alert alert-success"
            role="alert"
            style="width: 158px; height: 40px; padding-top: 9px"
          >
            <span v-text="resources['no_of_part'] + ':'"></span>
            {{ countPartOfCurrentFile }}
          </div>
          <button
            class="btn btn-primary btn-add"
            @click="addPart"
            v-text="resources['add_part']"
          ></button>
          <button
            class="btn btn-danger"
            @click="removePart"
            v-text="resources['remove_part']"
          ></button>
        </div>
      </div>
      <div ref="customTable" class="table-content" @click="resetFocusIndex">
        <table>
          <thead>
            <tr>
              <th v-text="resources['matched']"></th>
              <th v-text="resources['column_header_from_file']"></th>
              <th v-text="resources['preview_info']"></th>
              <th v-text="resources['emoldino_property']"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(header, index) in currentFile.headers"
              :key="index"
              :id="'property-' + index"
            >
              <td>
                <img
                  src="/images/import-tooling/file-finishes-uploading.svg"
                  alt="success matching column icon"
                  class="success-icon"
                  v-if="header.mappedHeader"
                />
              </td>
              <td v-if="header.isDuplicated">
                {{ header.name }} (#{{ header.order }})
              </td>
              <td v-else>{{ header.name }}</td>
              <td>
                <div class="value">
                  {{ currentFile.data[0] && currentFile.data[0][index] }}
                </div>
                <div class="value">
                  {{ currentFile.data[1] && currentFile.data[1][index] }}
                </div>
                <div class="value">
                  {{ currentFile.data[2] && currentFile.data[2][index] }}
                </div>
              </td>
              <td>
                <div
                  class="property-form"
                  :class="{
                    active: isShowForm(index) || isFocusProperty(index),
                  }"
                >
                  <div
                    class="property-field"
                    :ref="'cell-' + index"
                    @click.stop="toggleShowForm(index, header)"
                    @click="
                      removeFocusProperty(index);
                      scroll(index);
                    "
                  >
                    <div class="field-title">
                      <span v-if="header.mappedHeader">
                        <span v-if="header.mappedHeader.type === 'parent'">
                          {{
                            `${header.mappedHeader.name} ${
                              header.mappedChildrenIndex + 1
                            } - ${
                              header.mappedHeader.children[
                                header.mappedChildrenIndex
                              ][header.mappedChildrenFieldIndex].name
                            }`
                          }}
                          <span
                            v-if="
                              header.mappedHeader.children[
                                header.mappedChildrenIndex
                              ][header.mappedChildrenFieldIndex].rules.required
                            "
                            class="field-required"
                            >•</span
                          >
                        </span>
                        <span v-else>
                          {{ header.mappedHeader.name }}
                          <span
                            v-if="header.mappedHeader.rules.required"
                            class="field-required"
                            >•</span
                          >
                        </span>
                      </span>
                      <span
                        v-else-if="header.isDontIImpot"
                        v-text="resources['dont_import']"
                      >
                      </span>
                      <span v-else v-text="resources['choose_property']">
                      </span>
                    </div>
                    <div class="field-icon" @click.stop="toggleShowForm(index)">
                      <img
                        src="/images/import-tooling/down-arrow-grey.svg"
                        alt="down arrow"
                      />
                    </div>
                  </div>

                  <div
                    :ref="'dropdown-' + index"
                    class="property-option"
                    v-if="isShowForm(index)"
                    v-click-outside="hideForm"
                  >
                    <div class="option-header">
                      <i class="fa fa-search"></i>
                      <input
                        type="text"
                        placeholder="Input search text"
                        v-model="searchingInputText"
                        class="form-control search-property"
                        autofocus="autofocus"
                      />
                    </div>
                    <div class="option-body">
                      <template
                        v-for="(
                          originHeaderItem, originIndex
                        ) in searchedOriginHeader"
                      >
                        <template v-if="originHeaderItem.type === 'parent'">
                          <template
                            v-for="(
                              childrenOriginHeaderItem, childrenIndex
                            ) in originHeaderItem.children"
                          >
                            <div
                              class="option-item"
                              :class="{
                                'option-disabled':
                                  isDisabledChildrenHeaderOption(
                                    header,
                                    originHeaderItem,
                                    childrenIndex,
                                    itemFieldIndex
                                  ),
                              }"
                              v-for="(
                                childrenOriginHeaderItemField, itemFieldIndex
                              ) in childrenOriginHeaderItem"
                              :key="childrenIndex + '-' + itemFieldIndex"
                              v-if="
                                isMappingWithChildren(
                                  originHeaderItem,
                                  childrenIndex,
                                  itemFieldIndex
                                )
                              "
                              @click="
                                selectMappedChildrenHeader(
                                  header,
                                  originHeaderItem,
                                  childrenIndex,
                                  itemFieldIndex
                                )
                              "
                            >
                              <div
                                class="checkbox-square"
                                :class="{
                                  checked: isSelectedChildrenHeaderOption(
                                    header,
                                    originHeaderItem,
                                    childrenIndex,
                                    itemFieldIndex
                                  ),
                                }"
                              >
                                <div class="checkbox-square-inner"></div>
                              </div>
                              <i
                                class="fa fa-exclamation-triangle"
                                v-if="
                                  isDisabledChildrenHeaderOption(
                                    header,
                                    originHeaderItem,
                                    childrenIndex,
                                    itemFieldIndex
                                  )
                                "
                              ></i>
                              <div class="item-title">
                                {{
                                  labelOfChildren(
                                    originHeaderItem,
                                    childrenIndex,
                                    itemFieldIndex
                                  )
                                }}
                                <span
                                  class="option-required"
                                  v-if="
                                    childrenOriginHeaderItemField.rules.required
                                  "
                                  >•</span
                                >
                              </div>
                              <div
                                class="disable-hint"
                                v-if="
                                  isDisabledChildrenHeaderOption(
                                    header,
                                    originHeaderItem,
                                    childrenIndex,
                                    itemFieldIndex
                                  )
                                "
                              >
                                <div class="disable-reason">
                                  {{
                                    labelOfChildren(
                                      originHeaderItem,
                                      childrenIndex,
                                      itemFieldIndex
                                    )
                                  }}
                                  <span
                                    v-text="resources['is_selected']"
                                  ></span>
                                </div>
                                <div class="disable-arrow"></div>
                              </div>
                            </div>
                          </template>
                        </template>
                        <template v-else-if="!originHeaderItem.replaceList">
                          <div
                            class="option-item"
                            :class="{
                              'option-disabled': isDisabledHeaderOption(
                                header,
                                originHeaderItem
                              ),
                            }"
                            @click="
                              selectMappedHeader(header, originHeaderItem)
                            "
                          >
                            <div
                              class="checkbox-square"
                              :class="{
                                checked: isSelectedHeaderOption(
                                  header,
                                  originHeaderItem
                                ),
                              }"
                            >
                              <div class="checkbox-square-inner"></div>
                            </div>
                            <i
                              class="fa fa-exclamation-triangle"
                              v-if="
                                isDisabledHeaderOption(header, originHeaderItem)
                              "
                            ></i>
                            <div class="item-title">
                              {{ originHeaderItem.name }}
                              <span
                                class="option-required"
                                v-if="originHeaderItem.rules.required"
                                >•</span
                              >
                            </div>
                            <div
                              class="disable-hint"
                              v-if="
                                isDisabledHeaderOption(header, originHeaderItem)
                              "
                            >
                              <div class="disable-reason">
                                {{ originHeaderItem.name }}
                                <span v-text="resources['is_selected']"></span>
                              </div>
                              <div class="disable-arrow"></div>
                            </div>
                          </div>
                        </template>
                      </template>
                    </div>
                    <div
                      class="option-footer"
                      @click="setDontImportColumn(header)"
                    >
                      <span v-text="resources['dont_import']"></span>
                    </div>
                    <div
                      style="
                        text-align: left;
                        padding: 0 22px 7px 22px;
                        color: #463da5;
                        cursor: pointer;
                      "
                      @click="showDrawer(index)"
                    >
                      <span v-text="resources['create_new_property']"></span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div
        class="matching-info"
        style="
          justify-content: space-between;
          padding-top: 10px;
          flex-direction: unset;
          align-items: unset;
        "
      >
        <div style="display: block">
          <div class="unmatched-wrapper" v-if="countTotalUnmatched > 0">
            <div class="left-action">
              <a
                class="btn btn-default track-bar left"
                @click="previousColumn"
              ></a>
              <a class="btn btn-default track-bar" @click="nextColumn"></a>
              <div class="tracking-indicator">
                <div class="tracking-title">
                  {{ countNumResolved }} of {{ countTotalUnmatched }}
                  {{ countTotalUnmatched > 1 ? "issues" : "issue" }} resolved
                </div>
                <div class="progress-tracking-wrapper">
                  <div class="progress-tracking">
                    <div
                      class="progress-bar-tracking"
                      :style="{
                        width:
                          (countNumResolved * 100) / countTotalUnmatched + '%',
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
          </div>
        </div>
        <div
          style="display: flex; flex-direction: column; align-items: flex-end"
        >
          <div
            class="unmatched-wrapper"
            v-if="countColumnUnmatchedCurrentFile > 0"
          >
            <template v-if="countColumnUnmatchedCurrentFile !== 1">
              <div class="unmatched-info">
                <span class="unmatched-count">{{
                  countColumnUnmatchedCurrentFile
                }}</span>
                <span v-text="resources['columns']"></span>
              </div>
              <span v-text="resources['are_unmatched']"></span>
            </template>
            <template v-else>
              <div class="unmatched-info">
                <span class="unmatched-count">{{
                  countColumnUnmatchedCurrentFile
                }}</span>
                <span v-text="resources['column']"></span>
              </div>
              <span v-text="resources['is_unmatched']"> </span>
            </template>
          </div>

          <div class="unmatched-wrapper" v-if="countRequiredFieldMissing > 0">
            <div class="missing-detail">
              <div class="missing-icon">
                <img
                  src="/images/import-tooling/question-mark.svg"
                  alt="error icon"
                />
              </div>
              <div class="missing-content">
                <div class="missing-body">
                  <div
                    class="missing-title"
                    v-text="resources['please_provide']"
                  ></div>
                  <div
                    class="missing-item"
                    v-for="(headerName, index) in requiredFieldMissing"
                    :key="index"
                  >
                    {{ index + 1 }}. {{ headerName }}
                  </div>
                </div>
                <div class="missing-arrow"></div>
              </div>
            </div>
            <template v-if="countRequiredFieldMissing !== 1">
              <div class="unmatched-info">
                <span class="unmatched-count">{{
                  countRequiredFieldMissing
                }}</span>
                <span v-text="resources['required_emoldino_props']"></span>
              </div>
              <span v-text="resources['are_missing']"> </span>
            </template>
            <template v-else>
              <div class="unmatched-info">
                <span class="unmatched-count">{{
                  countRequiredFieldMissing
                }}</span>
                <span v-text="resources['required_emoldino_prop']"></span>
              </div>
              <span v-text="resources['is_missing']"> </span>
            </template>
          </div>
        </div>
      </div>
    </div>
    <div v-if="canNext" class="direction-arrow next" @click="nextFile">
      <img
        src="/images/import-tooling/back-next-button.svg"
        alt="next button"
      />
    </div>
    <div v-else class="direction-arrow"></div>
    <create-property-drawer
      :resources="resources"
      :visible="visible"
      @open="showDrawer()"
      @close="closeDrawer"
      @create-success="createSuccess"
      :is-matching-column="true"
      is-type="TOOLING"
    />
  </div>
</template>
<script>
module.exports = {
  name: "MatchingColumn",
  components: {
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
  },
  props: {
    fileList: {
      type: Array,
      default: () => [],
    },
    originHeader: {
      type: Array,
      default: () => [],
    },
    resources: Object,
    createItem: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      currentFileIndex: 0,
      trigger: 0,
      isShowSelectionForm: false,
      currentFormIndex: 0,
      searchingInputText: "",
      visible: false,

      partAction: {
        isShowNotifyChangePart: false,
        parentHeaderItem: null,
        type: "",
      },
      partActionType: {
        add: 1,
        delete: 2,
      },
      currentNotMapping: 0,
      focusIndex: null,
      lastFocusIndex: null,
      currentHeader: null,
    };
  },
  watch: {
    createItem(newVal) {
      if (newVal) {
        this.selectMappedHeader(this.currentHeader, this.createItem);
      }
    },
  },
  computed: {
    currentFile() {
      return this.fileList[this.currentFileIndex];
    },
    searchedOriginHeader() {
      let sortedData = this.currentFile.originHeader
        .filter((originHeaderItem) => {
          if (originHeaderItem.type === "clonedToMap") {
            return false;
          }
          if (originHeaderItem.type === "parent") {
            let isValid = false;
            originHeaderItem.labelForSearching.forEach(
              (labelForSearchingItem) => {
                if (
                  labelForSearchingItem
                    .toUpperCase()
                    .includes(this.searchingInputText.toUpperCase())
                ) {
                  isValid = true;
                }
              }
            );
            if (isValid) {
              return true;
            }
          } else {
            return originHeaderItem.name
              .toUpperCase()
              .includes(
                this.searchingInputText && this.searchingInputText.toUpperCase()
              );
          }
        })
        .sort((first, second) => {
          let firstName = first.name.toUpperCase();
          let secondName = second.name.toUpperCase();
          return firstName > secondName ? 1 : -1;
        });
      console.log("searchedOriginHeader", sortedData);
      return sortedData;
    },

    countPartOfCurrentFile() {
      if (!this.currentFile) return 0;
      const partHeader = this.currentFile.originHeader.filter(
        (originHeaderItem) => originHeaderItem.type === "parent"
      )[0];
      return partHeader.children.length;
    },

    countColumnUnmatchedCurrentFile() {
      return this.countColumnUnmatched(this.currentFileIndex);
    },

    countRequiredFieldMissing() {
      console.log("countRequiredFieldMissing");
      this.checkingCanNextStep();
      const allRequiredField = this.countAllRequiredFieldCurrentFile();
      // if children map, than parent is required, so it can be right for children case
      let requiredFieldNames = [];
      this.currentFile.originHeader.forEach((originHeaderItem) => {
        // checking for each item, that is required
        if (
          originHeaderItem.type !== "parent" &&
          originHeaderItem.type !== "clonedToMap"
        ) {
          if (originHeaderItem.rules.required) {
            requiredFieldNames.push(originHeaderItem.name);
          }
        } else if (originHeaderItem.type === "parent") {
          originHeaderItem.children.forEach(
            (groupChildrenHeader, groupIndex) => {
              groupChildrenHeader.forEach((childrenHeader, childrenIndex) => {
                let childrenName = this.labelOfChildren(
                  originHeaderItem,
                  groupIndex,
                  childrenIndex
                );
                requiredFieldNames.push(childrenName);
              });
            }
          );
        }
      });

      // checking all mapped header is required
      let requiredFieldMappedNames = [];
      this.currentFile.headers.forEach((header) => {
        if (header.mappedHeader) {
          let mappedHeader = header.mappedHeader;
          if (mappedHeader.type !== "parent") {
            if (mappedHeader.rules.required) {
              requiredFieldMappedNames.push(mappedHeader.name);
              if (
                mappedHeader.name === "Toolmaker ACT" &&
                !requiredFieldMappedNames.includes("Supplier ACT")
              ) {
                requiredFieldMappedNames.push("Supplier ACT");
              }
              if (
                mappedHeader.name === "Supplier ACT" &&
                !requiredFieldMappedNames.includes("Toolmaker ACT")
              ) {
                requiredFieldMappedNames.push("Toolmaker ACT");
              }
            }
          } else {
            let childrenName = this.labelOfChildren(
              mappedHeader,
              header.mappedChildrenIndex,
              header.mappedChildrenFieldIndex
            );
            requiredFieldMappedNames.push(childrenName);
          }
        }
      });
      return requiredFieldNames.filter(
        (requiredHeader) => !requiredFieldMappedNames.includes(requiredHeader)
      ).length;
    },
    canNext() {
      return this.currentFileIndex < this.fileList.length - 1;
    },

    canPrevious() {
      return this.currentFileIndex > 0;
    },

    requiredFieldMissing() {
      let requiredFieldNames = [];
      this.currentFile.originHeader.forEach((originHeaderItem) => {
        // checking for each item, that is required
        if (
          originHeaderItem.type !== "parent" &&
          originHeaderItem.type !== "clonedToMap"
        ) {
          if (originHeaderItem.rules.required) {
            requiredFieldNames.push(originHeaderItem.name);
          }
        } else if (originHeaderItem.type === "parent") {
          originHeaderItem.children.forEach(
            (groupChildrenHeader, groupIndex) => {
              groupChildrenHeader.forEach((childrenHeader, childrenIndex) => {
                let childrenName = this.labelOfChildren(
                  originHeaderItem,
                  groupIndex,
                  childrenIndex
                );
                requiredFieldNames.push(childrenName);
              });
            }
          );
        }
      });

      // checking all mapped header is required
      let requiredFieldMappedNames = [];
      this.currentFile.headers.forEach((header) => {
        if (header.mappedHeader) {
          let mappedHeader = header.mappedHeader;
          if (mappedHeader.type !== "parent") {
            if (mappedHeader.rules.required) {
              requiredFieldMappedNames.push(mappedHeader.name);
            }
          } else {
            let childrenName = this.labelOfChildren(
              mappedHeader,
              header.mappedChildrenIndex,
              header.mappedChildrenFieldIndex
            );
            requiredFieldMappedNames.push(childrenName);
          }
        }
      });
      return requiredFieldNames.filter(
        (requiredHeader) => !requiredFieldMappedNames.includes(requiredHeader)
      );
    },
    countTotalUnmatched() {
      return this.countTotalColumnUnmatched(this.currentFileIndex);
    },
    countNumResolved() {
      const numberOfResolved = this.countTotalColumnMatched(
        this.currentFileIndex
      );
      return numberOfResolved > this.countTotalUnmatched
        ? this.countTotalUnmatched
        : numberOfResolved;
    },
  },
  methods: {
    isRequired(header, originHeader) {
      if (
        originHeader.name === "Supplier ACT" ||
        originHeader.name === "Toolmaker ACT"
      ) {
        console.log("isRequired", header, originHeader);
        this.currentFile.headers.some((headerItem) => {
          if (headerItem.mappedHeader) {
            let mappedHeader = headerItem.mappedHeader;
            return !(
              mappedHeader.name === "Toolmaker ACT" ||
              mappedHeader.name === "Supplier ACT"
            );
          }
        });
      } else {
        return originHeader.rules.required;
      }
    },
    showDrawer(index) {
      this.saveLastColumnOpened(index);
      this.visible = true;
    },
    createSuccess() {
      this.$emit("update");
    },
    closeDrawer() {
      this.openLastColumn();
      this.visible = false;
    },
    scroll(ref) {
      const self = this;
      setTimeout(() => {
        const dropdown = self.$refs["dropdown-" + ref];
        const div = self.$refs["cell-" + ref];
        if (dropdown && dropdown[0]) {
          const bottom = div[0].getBoundingClientRect().bottom;
          const heightDropdown = dropdown[0].getBoundingClientRect().height;
          const containerRect = self.$refs.customTable.getBoundingClientRect();
          if (bottom + heightDropdown > containerRect.bottom) {
            self.$refs.customTable.scrollTop += heightDropdown;
          }
        }
      }, 150);
    },
    countColumnUnmatched(fileIndex) {
      return this.fileList[fileIndex].headers.filter(
        (header) => !header.mappedHeader && !header.isDontIImpot
      ).length;
    },
    countTotalColumnUnmatched(fileIndex) {
      return this.fileList[fileIndex].headers.filter(
        (header) =>
          header.isUsedToUnMapped || header.isDontIImpot || !header.mappedHeader
      ).length;
    },
    countTotalColumnMatched(fileIndex) {
      return this.fileList[fileIndex].headers.filter(
        (header) =>
          (header.isUsedToUnMapped && header.mappedHeader) ||
          header.isDontIImpot
      ).length;
    },
    isShowForm(formIndex) {
      return formIndex === this.currentFormIndex && this.isShowSelectionForm;
    },
    toggleShowForm(formIndex, header) {
      // using for open exactly selection form
      this.currentHeader = header;
      this.currentFormIndex = formIndex;
      this.searchingInputText = "";
      this.isShowSelectionForm = !this.isShowSelectionForm;
    },
    hideForm() {
      this.searchingInputText = "";
      this.isShowSelectionForm = false;
    },
    previousFile() {
      if (this.canPrevious) {
        this.reset();
        this.currentFileIndex--;
        this.removeFocusProperty();
      }
    },
    nextFile() {
      if (this.canNext) {
        this.reset();
        this.currentFileIndex++;
        this.removeFocusProperty();
      }
    },
    removeSelectedHeader(header) {
      header.mappedHeader = null;
      header.isUsedToUnMapped = true;
      delete header.mappedChildrenIndex;
      delete header.mappedChildrenField;
      delete header.mappedChildrenFieldIndex;
    },
    getMappedHeaderByName(name) {
      const toolmakerOriginHeaderIndex =
        this.currentFile.originHeader.findIndex((item) => item.name === name);
      return this.currentFile.originHeader[toolmakerOriginHeaderIndex];
    },
    getHeaderByName(name) {
      const toolmakerHeaderIndex = this.currentFile.headers.findIndex(
        (item) => item.mappedHeader && item.mappedHeader.name === name
      );
      return this.currentFile.headers[toolmakerHeaderIndex];
    },
    selectMappedHeader(header, mappedHeader) {
      console.log("selectMappedHeader", header, mappedHeader);
      if (this.isDisabledHeaderOption(header, mappedHeader)) {
        return;
      }
      if (this.isSelectedHeaderOption(header, mappedHeader)) {
        this.removeSelectedHeader(header);
        if (mappedHeader.name === "Supplier ACT") {
          let toolmakerOriginHeader =
            this.getMappedHeaderByName("Toolmaker ACT");
          let toolmakerHeader = this.getHeaderByName("Toolmaker ACT");
          toolmakerOriginHeader.rules.required = true;
          if (toolmakerHeader) {
            mappedHeader.rules.required = false;
          } else {
            mappedHeader.rules.required = true;
          }
        }

        if (mappedHeader.name === "Toolmaker ACT") {
          let mapperSupplierHeader = this.getMappedHeaderByName("Supplier ACT");
          let supplierHeader = this.getHeaderByName("Supplier ACT");
          mapperSupplierHeader.rules.required = true;
          if (!supplierHeader) {
            mappedHeader.rules.required = true;
          }
        }
      } else {
        header.mappedHeader = mappedHeader;
        if (mappedHeader.name === "Supplier ACT") {
          mappedHeader.rules.required = true;
          header.mappedHeader = mappedHeader;
          let toolmakerOriginHeader =
            this.getMappedHeaderByName("Toolmaker ACT");
          toolmakerOriginHeader.rules.required = false;
        }
        if (mappedHeader.name === "Toolmaker ACT") {
          let supplierHeader = this.getHeaderByName("Supplier ACT");
          let mapperSupplierHeader = this.getMappedHeaderByName("Supplier ACT");
          if (!supplierHeader) {
            mapperSupplierHeader.rules.required = false;
          }
        }
      }

      delete header.mappedChildrenIndex;
      delete header.mappedChildrenField;
      delete header.mappedChildrenFieldIndex;
      this.hideForm();
    },
    selectMappedChildrenHeader(
      header,
      originHeaderItem,
      childrenIndex,
      childrenFieldIndex
    ) {
      if (
        this.isDisabledChildrenHeaderOption(
          header,
          originHeaderItem,
          childrenIndex,
          childrenFieldIndex
        )
      ) {
        return;
      }
      if (
        this.isSelectedChildrenHeaderOption(
          header,
          originHeaderItem,
          childrenIndex,
          childrenFieldIndex
        )
      ) {
        this.removeSelectedHeader(header);
      } else {
        header.mappedHeader = originHeaderItem;
        header.mappedChildrenIndex = childrenIndex;
        header.mappedChildrenField =
          originHeaderItem.children[childrenIndex][childrenFieldIndex].code;
        header.mappedChildrenFieldIndex = childrenFieldIndex;
      }
      this.hideForm();
    },
    isDisabledHeaderOption(header, originHeaderItem) {
      let isDisabled = false;
      this.currentFile.headers.forEach((headerElement) => {
        if (
          header !== headerElement &&
          headerElement.mappedHeader &&
          headerElement.mappedHeader.name === originHeaderItem.name
        ) {
          isDisabled = true;
        }
      });
      return isDisabled;
    },

    /**
     * Checking disable for children option of header
     * @param header: current header of column
     * @param originHeaderItem: origin header item of current file, each file will has an originHeader of itself
     * @param childrenIndex: index of children is being checked
     * @param childrenFieldIndex: index of field of children is being checked
     */
    isDisabledChildrenHeaderOption(
      header,
      originHeaderItem,
      childrenIndex,
      childrenFieldIndex
    ) {
      let isDisabled = false;
      this.currentFile.headers.forEach((headerElement) => {
        // only check other element
        if (header !== headerElement) {
          if (
            headerElement.mappedHeader &&
            headerElement.mappedHeader.name === originHeaderItem.name
          ) {
            if (
              headerElement.mappedChildrenIndex === childrenIndex &&
              headerElement.mappedChildrenFieldIndex === childrenFieldIndex
            ) {
              isDisabled = true;
            }
          }
        }
      });
      return isDisabled;
    },
    isSelectedHeaderOption(header, originHeaderItem) {
      return (
        header.mappedHeader &&
        header.mappedHeader.name === originHeaderItem.name
      );
    },

    isSelectedChildrenHeaderOption(
      header,
      originHeaderItem,
      childrenIndex,
      childrenFieldIndex
    ) {
      return (
        header.mappedHeader &&
        header.mappedHeader.name === originHeaderItem.name &&
        header.mappedChildrenIndex === childrenIndex &&
        header.mappedChildrenFieldIndex === childrenFieldIndex
      );
    },
    labelOfChildren(originHeaderItem, childrenIndex, childrenFieldIndex) {
      return `${originHeaderItem.name} ${childrenIndex + 1} - ${
        originHeaderItem.children[childrenIndex][childrenFieldIndex].name
      }`;
    },
    isMappingWithChildren(originHeaderItem, childrenIndex, childrenFieldIndex) {
      let labelForSearchingThisChildren = this.labelOfChildren(
        originHeaderItem,
        childrenIndex,
        childrenFieldIndex
      );
      return labelForSearchingThisChildren
        .toUpperCase()
        .includes(
          this.searchingInputText && this.searchingInputText.toUpperCase()
        );
    },
    setDontImportColumn(header) {
      this.hideForm();
      header.mappedHeader = null;
      header.isDontIImpot = true;
    },
    countAllRequiredField(fileIndex) {
      let countAllRequiredField = 0;
      this.getAllRequiredField(fileIndex).forEach((originHeaderItem) => {
        if (originHeaderItem.type !== "parent") {
          countAllRequiredField++;
        } else {
          originHeaderItem.children.forEach(
            (groupChildrenHeader, groupIndex) => {
              countAllRequiredField += groupChildrenHeader.filter(
                (childrenHeader) => {
                  return childrenHeader.rules.required;
                }
              ).length;
            }
          );
        }
      });
      return countAllRequiredField;
    },
    countAllRequiredFieldCurrentFile() {
      return this.countAllRequiredField(this.currentFileIndex);
    },
    getAllRequiredField(fileIndex) {
      return this.fileList[fileIndex].originHeader.filter((header) => {
        if (header.type === "clonedToMap") {
          return false;
        }
        return header.rules.required;
      });
    },

    getAllRequiredFieldCurrentFile() {
      return this.getAllRequiredField(this.currentFileIndex);
    },
    checkingCanNextStep() {
      let canNextStep = true;
      this.fileList.forEach((fileElement, fileIndex) => {
        const countAllRequiredField = this.countAllRequiredField(fileIndex);
        const countFileElementRequiredField = fileElement.headers.filter(
          (header) => header.mappedHeader && header.mappedHeader.rules.required
        ).length;
        if (countAllRequiredField > countFileElementRequiredField) {
          canNextStep = false;
        }

        if (this.countColumnUnmatched(fileIndex) > 0) {
          canNextStep = false;
        }
      });

      this.$emit("can-next-step", canNextStep);
    },
    addPart() {
      const originHeader = this.currentFile.originHeader;
      const clonedHeaderItem = originHeader.filter(
        (originHeaderItem) => originHeaderItem.type === "clonedToMap"
      )[0];
      const parentHeaderItem = originHeader.filter(
        (originHeaderItem) => originHeaderItem.type === "parent"
      )[0];
      const newChildren = Object.assign([], clonedHeaderItem.children);
      parentHeaderItem.children.push(newChildren);

      // if can map, then will map
      this.currentFile.headers.forEach((header) => {
        // we will have at least one element of part
        if (
          !header.mappedHeader &&
          header.isDuplicated &&
          header.order > 1 &&
          header.order === parentHeaderItem.children.length
        ) {
          newChildren.forEach((childrenField, childrenIndex) => {
            if (
              childrenField.name.toUpperCase() === header.name.toUpperCase()
            ) {
              header.mappedHeader = parentHeaderItem;
              header.mappedChildrenIndex = parentHeaderItem.children.length - 1;
              header.mappedChildrenField = childrenField.code;
              header.mappedChildrenFieldIndex = childrenIndex;
            }
          });
        }
      });

      this.partAction.isShowNotifyChangePart = true;
      this.partAction.type = this.partActionType.add;
      this.partAction.parentHeaderItem = parentHeaderItem;
    },

    removePart() {
      const parentHeaderItem = this.currentFile.originHeader.filter(
        (originHeaderItem) => originHeaderItem.type === "parent"
      )[0];

      // at least one part must be existed
      const lastIndex = parentHeaderItem.children.length - 1;
      if (lastIndex === 0) {
        this.partAction.isShowNotifyChangePart = false;
        return;
      }

      // delete mapped header
      this.currentFile.headers.forEach((header) => {
        if (
          header.mappedHeader &&
          header.mappedHeader.name === parentHeaderItem.name &&
          header.mappedChildrenIndex === lastIndex
        ) {
          this.removeSelectedHeader(header);
        }
      });

      // delete last element
      parentHeaderItem.children.splice(parentHeaderItem.children.length - 1, 1);
      this.partAction.isShowNotifyChangePart = true;
      this.partAction.type = this.partActionType.delete;
      this.partAction.parentHeaderItem = parentHeaderItem;
    },
    reset() {
      this.partAction.isShowNotifyChangePart = false;
      this.partAction.type = null;
      this.partAction.parentHeaderItem = null;
    },
    calcIdElement(isBack = false) {
      if (this.currentFile.headers) {
        if (this.currentFile.headers) {
          let targetIndex = null;
          for (let idx = 0; idx < this.currentFile.headers.length; idx++) {
            let index = isBack
              ? this.currentFile.headers.length - 1 - idx
              : idx;
            if (this.currentNotMapping != null) {
              index =
                (index + this.currentNotMapping) %
                this.currentFile.headers.length;
              index =
                (!isBack
                  ? index + 1
                  : index + this.currentFile.headers.length - 1) %
                this.currentFile.headers.length;
            }
            let header = this.currentFile.headers[index];
            if (!header.mappedHeader && !header.isDontIImpot) {
              targetIndex = index;
              break;
            }
          }
          this.currentNotMapping = targetIndex;
        }
      }
      if (this.currentNotMapping != null) {
        this.focusIndex = this.currentNotMapping;
        return "#property-" + this.currentNotMapping;
      }
      return "";
    },
    isFocusProperty(index) {
      return index === this.focusIndex;
    },
    removeFocusProperty() {
      this.focusIndex = null;
    },
    resetFocusIndex() {
      this.focusIndex = null;
    },
    saveLastColumnOpened(index) {
      this.lastFocusIndex = index;
    },
    openLastColumn() {
      // setTimeout(() => {
      this.currentFormIndex = this.lastFocusIndex;
      this.searchingInputText = "";
      this.isShowSelectionForm = true;
      this.scroll(this.focusIndex);
      this.$forceUpdate;
      // }, 300)
    },
    previousColumn() {
      this.hideForm();
      location.hash = this.calcIdElement(true);
      setTimeout(() => {
        this.currentFormIndex = this.focusIndex;
        this.searchingInputText = "";
        this.isShowSelectionForm = true;
        this.scroll(this.focusIndex);
      }, 300);
    },
    nextColumn() {
      this.hideForm();
      location.hash = this.calcIdElement(false);
      setTimeout(() => {
        this.currentFormIndex = this.focusIndex;
        this.searchingInputText = "";
        this.isShowSelectionForm = true;
        this.scroll(this.focusIndex);
      }, 300);
    },
  },
};
</script>

<style>
.ant-drawer {
  z-index: 4000;
}
.ant-select-dropdown {
  z-index: 4002;
}
.drawer-title {
  color: #1a1a1a;
  font-weight: bold;
  margin-bottom: 5px;
}
.field-type {
  width: 70%;
}
.field-type div {
  margin-top: 7px;
}
.ant-drawer-close {
  display: flex;
  align-items: center;
  justify-content: center;
}
.ant-drawer-close:focus {
  outline: unset !important;
}
.ant-drawer-close .anticon-close {
  color: #fff;
}
.ant-form-item {
  margin: 0 0 10px !important;
}
.ant-drawer-header {
  background: linear-gradient(
    90deg,
    rgba(128, 117, 251, 1) 51%,
    rgba(104, 138, 255, 1) 69%
  );
  border-radius: unset;
}
.ant-drawer-title {
  color: #fff;
}
</style>
