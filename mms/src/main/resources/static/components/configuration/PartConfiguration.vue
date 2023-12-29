<template>
  <div>
    <div class="config-header">
      <div class="left-header">
        <strong
          v-text="resources['part']"
          :class="{
            'disable-text': !categoryStatus.enabled,
            'enable-text': categoryStatus.enabled,
          }"
        ></strong>
      </div>
      <div class="right-header">
        <div
          class="switch-status"
          :class="{
            'disable-text': categoryStatus.enabled,
            'enable-text': !categoryStatus.enabled,
          }"
        >
          Off
        </div>
        <div class="switch-button">
          <div class="toggleWrapper">
            <input
              type="checkbox"
              v-model="categoryStatus.enabled"
              class="mobileToggle"
              id="part-toggle"
              value="1"
            />
            <label
              class="change-checkout-status"
              @click="updateConfigStatus"
            ></label>
          </div>
        </div>
        <div
          class="switch-status"
          :class="{
            'disable-text': !categoryStatus.enabled,
            'enable-text': categoryStatus.enabled,
          }"
        >
          On
        </div>
      </div>
    </div>
    <div
      class="config-body"
      :class="{
        'disable-text': !categoryStatus.enabled,
        'enable-text': categoryStatus.enabled,
      }"
    >
      <div class="card">
        <div class="card-header">
          <strong v-text="resources['part']"></strong>
        </div>
        <div class="card-body">
          <div
            :style="
              sectionDialog === false
                ? '-webkit-mask-image: -webkit-gradient(linear, left 30%, left bottom, from(rgba(0, 0, 0, 1)), to(rgba(0, 0, 0, 0)));max-height: 300px;'
                : ''
            "
          >
            <div
              class="row"
              v-for="(field, index) in fields"
              :class="{ 'd-none': field.deletedField }"
              :key="index"
            >
              <label class="col-md-2 col-form-label field-label">
                {{ field.label }}
                <span
                  class="badge-require"
                  v-show="field.requiredType === requiredTypeOptions.REQUIRED"
                ></span>
              </label>
              <div class="d-flex col-md-10 col-form-label">
                <div style="margin-right: 0.75rem">
                  <div
                    class="row"
                    :class="{ 'disable-checked': field.isRequired }"
                  >
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="
                            !categoryStatus.enabled ||
                            field.isRequired ||
                            field.disableChangeRequiredType
                          "
                          class="form-check-input"
                          :value="requiredTypeOptions.REQUIRED"
                          @change="changeHandler"
                          v-model="field.requiredType"
                        />
                        <span
                          class="field-label"
                          v-text="resources['required']"
                        ></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="
                            !categoryStatus.enabled ||
                            field.isRequired ||
                            field.disableChangeRequiredType
                          "
                          class="form-check-input"
                          :value="requiredTypeOptions.OPTIONAL"
                          @change="changeHandler"
                          v-model="field.requiredType"
                        />
                        <span
                          class="field-label"
                          v-text="resources['optional']"
                        ></span>
                      </label>
                    </div>
                  </div>
                  <div class="row group-input-type">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="!categoryStatus.enabled"
                          class="form-check-input"
                          :value="valueTypeOptions.MANUAL"
                          v-model="field.valueType"
                          @change="changeHandler"
                        />
                        <span
                          class="field-label"
                          v-text="resources['manual_input']"
                        ></span>
                      </label>
                    </div>
                    <div
                      class="form-check form-check-inline"
                      :class="{ 'disable-checked': field.isNotExistedDefault }"
                    >
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="
                            !categoryStatus.enabled ||
                            field.isRequired ||
                            field.isNotExistedDefault
                          "
                          class="form-check-input"
                          :value="valueTypeOptions.DEFAULT"
                          v-model="field.valueType"
                          @change="changeHandler"
                        />
                        <span
                          class="field-label"
                          v-text="resources['default']"
                        ></span>
                      </label>
                    </div>
                    <!--                                  <div class="default-value">-->
                    <!--                                    <input type="text"  class="form-control" v-model="field.defaultValue" v-if="field.valueType === valueTypeOptions.DEFAULT"-->
                    <!--                                           :placeholder="resources['default']"  @change="updateConfigData(field)">-->
                    <!--                                  </div>-->
                  </div>
                </div>
                <div
                  style="
                    width: 100%;
                    flex-direction: row-reverse;
                    align-items: flex-end;
                    justify-content: flex-end;
                  "
                  class="action-zone"
                  :class="{
                    'pt-1': field.valueType === valueTypeOptions.DEFAULT,
                  }"
                >
                  <div
                    v-if="!field.isRequired"
                    style="
                      margin-bottom: 8px;
                      height: 100%;
                      display: flex;
                      align-items: center;
                      margin-left: 15px;
                    "
                    class="confirm-zone"
                  >
                    <span
                      style="visibility: hidden"
                      class="checklist-edit action-item"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                    >
                      <img
                        src="/images/checklist/pencil.svg"
                        class="icon-primary"
                        alt="edit"
                      />
                      <img
                        src="/images/checklist/pencil-hover.svg"
                        class="icon-hover"
                        alt="edit"
                      />
                    </span>
                    <span
                      class="checklist-delete action-item"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                      @click="showDeletePopup(field)"
                    >
                      <img
                        src="/images/checklist/trash.svg"
                        class="icon-primary"
                        alt="delete"
                      />
                      <img
                        src="/images/checklist/trash-hover.svg"
                        class="icon-hover"
                        alt="delete"
                      />
                    </span>
                    <!--                                    <button hidden type="submit" :ref="'btn-checklist-' + index"></button>-->
                    <!--                                    <span v-if="focusReasonIndex === index" class="checklist-confirm-edit" @click="updateConfigCustomField(field, index)">-->
                    <!--                                    <img src="/images/checklist/check.svg" alt="confirm-edit" />-->
                    <!--                                  </span>-->
                    <!--                                  <span v-if="focusReasonIndex === index" class="checklist-cancel-edit" @click="cancelUpdateConfigCustomField(index)">-->
                    <!--                                    <img src="/images/checklist/close.svg" alt="cancel-edit" />-->
                    <!--                                  </span>-->
                  </div>
                  <div
                    v-if="
                      field.valueType === valueTypeOptions.DEFAULT &&
                      field.unitSelections
                    "
                    style="max-width: 35%; margin-bottom: 8px"
                  >
                    <unit-selection
                      :title="field.unitSelections"
                      :selection-list="codes[field.unitSelections]"
                      :selected-unit="field.defaultValue"
                      :type="
                        field.code == 'sizeUnit'
                          ? 'volume'
                          : field.unitSelections
                      "
                      @change-unit="
                        (value) => handleChangeUnitField(value, field)
                      "
                    >
                    </unit-selection>
                  </div>
                  <div
                    v-if="
                      field.valueType === valueTypeOptions.DEFAULT &&
                      !field.unitSelections
                    "
                    style="max-width: 35%; margin-bottom: 8px"
                    class="default-value default-zone"
                  >
                    <input
                      type="text"
                      class="form-control"
                      v-model="field.defaultValue"
                      v-if="field.valueType === valueTypeOptions.DEFAULT"
                      :disabled="!categoryStatus.enabled"
                      :placeholder="resources['default']"
                      @change="changeHandler"
                    />
                  </div>
                </div>
                <!--                                <div class="row" :class="{'disable-checked': field.isRequired}">-->
                <!--                                    <div class="form-check first-check form-check-inline">-->
                <!--                                        <label class="form-check-label">-->
                <!--                                            <input type="radio" :disabled="!categoryStatus.enabled" class="form-check-input"-->
                <!--                                                :value="requiredTypeOptions.REQUIRED" @change="updateConfigData(field)" v-model="field.requiredType" :disabled = "field.isRequired">-->
                <!--                                            <span v-text="resources['required']"></span>-->
                <!--                                        </label>-->
                <!--                                    </div>-->
                <!--                                    <div class="form-check form-check-inline">-->
                <!--                                        <label class="form-check-label">-->
                <!--                                            <input type="radio" :disabled="!categoryStatus.enabled" class="form-check-input"-->
                <!--                                                :value="requiredTypeOptions.OPTIONAL" @change="updateConfigData(field)" v-model="field.requiredType" :disabled = "field.isRequired">-->
                <!--                                            <span v-text="resources['optional']"></span>-->
                <!--                                        </label>-->
                <!--                                    </div>-->

                <!--                                </div>-->

                <!--                                <div class="row group-input-type">-->
                <!--                                    <div class="form-check first-check form-check-inline">-->
                <!--                                        <label class="form-check-label">-->
                <!--                                            <input type="radio" :disabled="!categoryStatus.enabled" class="form-check-input"-->
                <!--                                                :value="valueTypeOptions.MANUAL" v-model="field.valueType" @change="updateConfigData(field)">-->
                <!--                                             <span v-text="resources['manual_input']"></span>-->
                <!--                                        </label>-->
                <!--                                    </div>-->
                <!--                                    <div class="form-check form-check-inline" :class="{'disable-checked': field.isNotExistedDefault}">-->
                <!--                                        <label class="form-check-label">-->
                <!--                                            <input type="radio" :disabled="!categoryStatus.enabled" class="form-check-input"-->
                <!--                                                :value="valueTypeOptions.DEFAULT" v-model="field.valueType" @change="updateConfigData(field)" :disabled = "field.isNotExistedDefault">-->
                <!--                                            <span v-text="resources['default']"></span>-->
                <!--                                        </label>-->
                <!--                                    </div>-->
                <!--                                    <div class="default-value">-->
                <!--                                        <input type="text"  class="form-control" v-model="field.defaultValue" v-if="field.valueType === valueTypeOptions.DEFAULT"-->
                <!--                                            :placeholder="resources['default']"  @change="updateConfigData(field)">-->
                <!--                                    </div>-->
                <!--                                </div>-->
              </div>
            </div>
            <form
              class="row"
              action=""
              v-for="(field, index2) in listConfigCustomField"
              :key="index2 + 'config'"
            >
              <label
                v-if="focusReasonIndex !== index2"
                class="col-md-2 col-form-labe field-labell"
              >
                {{ field.fieldName }}
                <span class="badge-require" v-show="field.required"></span>
              </label>
              <div v-else class="col-md-2 col-form-label">
                <input
                  :ref="'edit-field-' + index2"
                  v-model="listConfigCustomField[index2].fieldName"
                  @keyup="removeAlertUpdate(index2)"
                  class="form-control reason-edit-input focus"
                />
              </div>
              <div class="d-flex col-md-10 col-form-label">
                <div style="margin-right: 0.75rem">
                  <div class="row">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="listConfigCustomField[index2].required"
                          :disabled="
                            focusReasonIndex !== index2 ||
                            !categoryStatus.enabled
                          "
                          type="radio"
                          class="form-check-input"
                          :value="true"
                        />
                        <span
                          class="field-label"
                          v-text="resources['required']"
                        ></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="listConfigCustomField[index2].required"
                          :disabled="
                            focusReasonIndex !== index2 ||
                            !categoryStatus.enabled
                          "
                          type="radio"
                          class="form-check-input"
                          :value="false"
                        />
                        <span
                          class="field-label"
                          v-text="resources['optional']"
                        ></span>
                      </label>
                    </div>
                  </div>

                  <div class="row group-input-type">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="field.defaultInput"
                          :disabled="
                            focusReasonIndex !== index2 ||
                            !categoryStatus.enabled
                          "
                          type="radio"
                          class="form-check-input"
                          :value="false"
                        />
                        <span
                          class="field-label"
                          v-text="resources['manual_input']"
                        ></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="field.defaultInput"
                          :disabled="
                            focusReasonIndex !== index2 ||
                            !categoryStatus.enabled
                          "
                          type="radio"
                          class="form-check-input"
                          :value="true"
                        />
                        <span
                          class="field-label"
                          v-text="resources['default']"
                        ></span>
                      </label>
                    </div>
                  </div>
                </div>
                <div
                  style="
                    width: 100%;
                    flex-direction: row-reverse;
                    align-items: flex-end;
                    justify-content: flex-end;
                    width: 22%;
                  "
                  class="action-zone"
                  :class="{
                    'pt-1': listConfigCustomField[index2].defaultInput,
                  }"
                >
                  <div
                    style="
                      margin-bottom: 8px;
                      height: 100%;
                      display: flex;
                      align-items: center;
                      margin-left: 15px;
                    "
                    class="confirm-zone"
                  >
                    <span
                      v-if="focusReasonIndex !== index2"
                      class="checklist-edit action-item"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                      @click="focusReason(field, index2)"
                    >
                      <img
                        src="/images/checklist/pencil.svg"
                        class="icon-primary"
                        alt="edit"
                      />
                      <img
                        src="/images/checklist/pencil-hover.svg"
                        class="icon-hover"
                        alt="edit"
                      />
                    </span>
                    <span
                      v-if="focusReasonIndex !== index2"
                      class="checklist-delete action-item"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                      @click="deleteConfigCustomField(field)"
                    >
                      <img
                        src="/images/checklist/trash.svg"
                        class="icon-primary"
                        alt="delete"
                      />
                      <img
                        src="/images/checklist/trash-hover.svg"
                        class="icon-hover"
                        alt="delete"
                      />
                    </span>
                    <button
                      hidden
                      type="submit"
                      :ref="'btn-checklist-' + index2"
                    ></button>
                    <span
                      v-if="focusReasonIndex === index2"
                      class="checklist-confirm-edit"
                      @click="updateConfigCustomField(field, index2)"
                    >
                      <img
                        src="/images/checklist/check.svg"
                        alt="confirm-edit"
                      />
                    </span>
                    <span
                      v-if="focusReasonIndex === index2"
                      class="checklist-cancel-edit"
                      @click="cancelUpdateConfigCustomField(index2)"
                    >
                      <img
                        src="/images/checklist/close.svg"
                        alt="cancel-edit"
                      />
                    </span>
                  </div>
                  <div
                    v-if="listConfigCustomField[index2].defaultInput"
                    style="max-width: 35%; margin-bottom: 8px"
                    class="default-value default-zone"
                  >
                    <input
                      :disabled="
                        !categoryStatus.enabled || focusReasonIndex !== index2
                      "
                      type="text"
                      :class="{ isDisable: focusReasonIndex !== index2 }"
                      class="form-control"
                      v-model="field.defaultInputValue"
                      :placeholder="resources['default']"
                    />
                  </div>
                </div>
              </div>
            </form>
            <!--                        // Create zone-->
            <form v-if="isShowCreateProperty" action="" class="row">
              <div class="col-md-2 col-form-label">
                <input
                  id="added-input"
                  ref="added-field"
                  v-model="configName"
                  @keyup="removeAlertCreate()"
                  class="form-control reason-edit-input focus"
                />
              </div>
              <div class="d-flex col-md-10 col-form-label">
                <div style="margin-right: 0.75rem">
                  <div class="row">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="!categoryStatus.enabled"
                          class="form-check-input"
                          name="required"
                          :value="requiredTypeOptions.REQUIRED"
                          v-model="isConfigRequired"
                        />
                        <span v-text="resources['required']"></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="!categoryStatus.enabled"
                          class="form-check-input"
                          name="required"
                          :value="requiredTypeOptions.OPTIONAL"
                          v-model="isConfigRequired"
                        />
                        <span v-text="resources['optional']"></span>
                      </label>
                    </div>
                  </div>

                  <div class="row group-input-type">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="!categoryStatus.enabled"
                          class="form-check-input"
                          :value="valueTypeOptions.MANUAL"
                          v-model="isConfigManual"
                        />
                        <span v-text="resources['manual_input']"></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="!categoryStatus.enabled"
                          class="form-check-input"
                          :value="valueTypeOptions.DEFAULT"
                          v-model="isConfigManual"
                        />
                        <span v-text="resources['default']"></span>
                      </label>
                    </div>
                  </div>
                </div>
                <div
                  style="
                    width: 100%;
                    flex-direction: row-reverse;
                    align-items: flex-end;
                    justify-content: flex-end;
                  "
                  class="action-zone"
                >
                  <div class="confirm-zone confirm-create-zone">
                    <span
                      class="checklist-confirm-edit"
                      @click="addNewConfigCustomField()"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                    >
                      <img
                        src="/images/checklist/check.svg"
                        alt="confirm-edit"
                      />
                    </span>
                    <button
                      hidden
                      type="submit"
                      :ref="'btn-checklist-create'"
                    ></button>
                    <span
                      class="checklist-cancel-edit"
                      @click="cancelEditCreateProperty()"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                    >
                      <img
                        src="/images/checklist/close.svg"
                        alt="cancel-edit"
                      />
                    </span>
                  </div>
                  <div
                    v-if="isConfigManual === valueTypeOptions.DEFAULT"
                    style="max-width: 35%; margin-bottom: 8px"
                    class="default-value default-zone"
                  >
                    <input
                      v-model="defaultValue"
                      type="text"
                      :disabled="!categoryStatus.enabled"
                      class="form-control"
                      :placeholder="resources['default']"
                    />
                  </div>
                </div>
              </div>
            </form>
            <!--                      @click="addChecklist" v-if="!isFocusChecklistInput"-->
            <a
              v-if="!isShowCreateProperty && sectionDialog"
              class="btn radio-confirm-modal"
              @click="changeShowCreateProperty(true)"
              style="color: #298bff; text-decoration: none; padding: unset"
              :class="{ 'disable-click disable-text': !categoryStatus.enabled }"
            >
              <span
                style="font-size: 12px; font-weight: bold; margin-right: 12px"
                >+</span
              >
              <span v-text="resources['create_new_property']"></span>
            </a>
          </div>

          <div
            class="show-more"
            :class="{
              'is-show-less': sectionDialog,
              'disable-click': !categoryStatus.enabled,
            }"
            style="cursor: pointer"
            @click="changeSectionDialog()"
          >
            <div>
              <!--                          <div class="mr-1">-->
              <!--                            <img v-if="sectionDialog === false" src="/images/icon/arrow-of-double-angle-pointing-down.svg" height="11">-->
              <!--                            <img v-else src="/images/icon/arrow-of-double-angle-pointing-up.svg" height="11">-->
              <!--                          </div>-->
              <span class="dialogTooling" v-if="sectionDialog === false">
                <img
                  v-show="categoryStatus.enabled"
                  class="mr-1"
                  src="/images/icon/arrow-of-double-angle-pointing-down.svg"
                  height="11"
                />
                <img
                  v-show="!categoryStatus.enabled"
                  class="mr-1"
                  src="/images/icon/arrow-of-double-angle-pointing-down-disable.svg"
                  height="11"
                />
                {{ resources["show_more"] }}
              </span>
              <span class="dialogTooling" v-else>
                <img
                  v-show="categoryStatus.enabled"
                  class="mr-1"
                  src="/images/icon/arrow-of-double-angle-pointing-up.svg"
                  height="11"
                />
                <img
                  v-show="!categoryStatus.enabled"
                  style="transform: rotate(180deg)"
                  class="mr-1"
                  src="/images/icon/arrow-of-double-angle-pointing-down-disable.svg"
                  height="11"
                />
                {{ resources["show_less"] }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <delete-fixed
      key="part-config"
      @delete="confirmDelete"
      :list="fieldDelete"
      name="Part"
      :resources="resources"
    ></delete-fixed>
    <a-modal
      v-model="visibleSuccess"
      width="264px"
      :footer="null"
      :closable="false"
      class="text-center col-3"
      @close="visibleSuccess = false"
      centered
    >
      <div class="flex-column-custom">
        <img
          style="width: 34px"
          src="/images/icon/check-invite.png"
          alt="check-invite"
        />
        <div style="font-size: 16px; margin: 9px 0px 3px; color: #3491ff">
          Property deleted
        </div>
        <div style="font-size: 13px; margin-bottom: 10px; color: #4b4b4b">
          {{ fieldDelete.label }} property has been deleted successfully.
        </div>
        <a
          @click="handleOkay"
          href="javascript:void(0)"
          id="handleOkay2"
          class="btn-custom btn-custom-primary"
        >
          <span style="font-size: 11px; line-height: 12px">Okay</span>
        </a>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  name: "PartConfiguration",
  props: {
    resources: Object,
    updateCategoryConfigStatus: Function,
    handleIsChange: Function,
    categoryStatus: Object,
    isCreating: String,
    updateCreating: Function,
    initConfigValue: {
      type: Array,
      default: () => [],
    },
    codes: {
      type: Object,
      default: () => {},
    },
  },
  components: {
    "delete-fixed": httpVueLoader("/components/delete-fixed.vue"),
    "unit-selection": httpVueLoader(
      "/components/configuration/unit-selection.vue"
    ),
  },
  data() {
    return {
      visibleSuccess: false,
      configCategory: "PART",
      // initConfigValue: [],
      requiredTypeOptions: {
        REQUIRED: "REQUIRED",
        OPTIONAL: "OPTIONAL",
      },
      valueTypeOptions: {
        MANUAL: "MANUAL",
        DEFAULT: "DEFAULT",
      },
      // listSection: [],
      sectionDialog: false,
      fields: [],
      fieldCode: "name",
      configName: "",
      isConfigRequired: "OPTIONAL",
      isConfigManual: "MANUAL",
      defaultValue: "",
      focusReasonIndex: null,
      listConfigCustomField: null,
      isShowCreateProperty: false,
      oldField: null,
      /*
                      listFieds1: [
                          {
                              code: 'category',
                              label: this.resources['project_name'],
                              requiredType: null,
                              isRequired: null,
                              valueType: null,
                              defaultValue: null,
                              isNotExistedDefault: true
                          },
                          //
                          // {
                          //     code: 'categoryName',
                          //     label: this.resources['category_name'],
                          //     requiredType: null,
                          //     isRequired: null,
                          //     valueType: null,
                          //     defaultValue: null,
                          //     isNotExistedDefault: true
                          // },
                          // {
                          //   code: 'projectName',
                          //   label: this.resources['project_name'],
                          //   requiredType: null,
                          //   isRequired: null,
                          //   valueType: null,
                          //   defaultValue: null,
                          // },
                          //
                          {
                              code: 'partCode',
                              label: this.resources['part_id'],
                              requiredType: null,
                              isRequired: true,
                              valueType: null,
                              defaultValue: null
                          },
                          {
                              code: 'name',
                              label: this.resources['part_name'],
                              requiredType: null,
                              isRequired: true,
                              valueType: null,
                              defaultValue: null
                          },
                          {
                            code: 'resinCode',
                            label: this.resources['part_resin_code'],
                            requiredType: null,
                            isRequired: true,
                            valueType: null,
                            defaultValue: null
                          }
                      ],
      */
      listFieds2: [
        {
          code: "category",
          label: this.resources["project_name"],
          requiredType: null,
          isRequired: null,
          valueType: null,
          defaultValue: null,
          isNotExistedDefault: true,
        },
        // {
        //     code: 'categoryName',
        //     label: this.resources['category_name'],
        //     requiredType: null,
        //     isRequired: null,
        //     valueType: null,
        //     defaultValue: null,
        //     isNotExistedDefault: true
        // },
        // {
        //   code: 'projectName',
        //   label: this.resources['project_name'],
        //   requiredType: null,
        //   isRequired: null,
        //   valueType: null,
        //   defaultValue: null,
        // },
        {
          code: "partCode",
          label: this.resources["part_id"],
          requiredType: null,
          isRequired: true,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "name",
          label: this.resources["part_name"],
          requiredType: null,
          isRequired: true,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "resinCode",
          label: this.resources["part_resin_code"],
          requiredType: null,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "resinGrade",
          label: this.resources["part_resin_grade"],
          requiredType: null,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "designRevision",
          label: this.resources["design_revision_level"],
          requiredType: null,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "size",
          label: this.resources["part_volume"] + " (W x D x H)",
          requiredType: null,
          // isRequired: true,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "sizeUnit",
          label: this.resources["part_volume_unit"],
          requiredType: null,
          // isRequired: true,
          valueType: null,
          defaultValue: null,
          unitSelections: "SizeUnit",
        },
        {
          code: "weight",
          label: this.resources["part_weight"],
          requiredType: null,
          // isRequired: true,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "weightUnit",
          label: this.resources["part_weight_unit"],
          requiredType: null,
          // isRequired: true,
          valueType: null,
          defaultValue: null,
          unitSelections: "WeightUnit",
        },
        {
          code: "weeklyDemand",
          label: this.resources["weekly_demand"],
          requiredType: null,
          // isRequired: true,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "quantityRequired",
          label: this.resources["quantity_required"],
          requiredType: null,
          disableChangeRequiredType: true,
          isRequired: null,
          valueType: null,
          defaultValue: null,
        },
        {
          code: "partPictureFile",
          label: this.resources["part_picture"],
          requiredType: null,
          valueType: null,
          defaultValue: null,
          isNotExistedDefault: true,
        },
      ],
      deletePopup: false,
      fieldDelete: {},
    };
  },
  created() {
    this.getListConfigCustomField();
  },
  mounted() {
    this.getFieds();
    this.initValue();
    // this.getConfig();
    this.updateFilds();
    this.updateConfigDataMore();
  },
  watch: {
    isCreating(value) {
      if (value !== "part") {
        this.changeShowCreateProperty(false);
      }
    },
    initConfigValue: function (value, oldValue) {
      console.log("part initConfigValue");
      this.updateFilds();
      // this.listSection = value
      // if (value && value.length > 0) {
      //     value.forEach(item => {
      //         let currentField = null;
      //         this.fields.forEach(field => {
      //             if (field.code === item.fieldName && item.configCategory === this.configCategory) {
      //                 currentField = field;
      //             }
      //         });

      //         if (currentField) {
      //             currentField.requiredType = item.required ? this.requiredTypeOptions.REQUIRED : this.requiredTypeOptions.OPTIONAL;
      //             currentField.valueType = item.defaultInput ? this.valueTypeOptions.DEFAULT :  this.valueTypeOptions.MANUAL;
      //             currentField.defaultValue = item.defaultInputValue;
      //             currentField.deletedField = item.deletedField;
      //             currentField.id = item.id;
      //         }

      //         console.log('fields', this.fields)
      //     })
      // }
    },
  },
  methods: {
    cancelData() {
      this.getListConfigCustomField();
      this.getFieds();
      this.initValue();
      // this.getConfig();
      this.updateFilds();
      this.updateConfigDataMore();
    },
    saveData() {
      console.log("saveData called");
      this.fields.forEach((field) => {
        this.updateConfigData(field);
      });
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled
      );
    },
    changeHandler() {
      this.handleIsChange();
    },
    handleOkay() {
      const el = document.getElementById("handleOkay2");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        el.classList.remove("primary-animation");
        self.visibleSuccess = false;
      }, 700);
    },
    showDeletePopup(field) {
      console.log(field, "comeeee 222");
      this.fieldDelete = field;
      var child = Common.vue.getChild(this.$children, "delete-fixed");
      if (child != null) {
        child.showDeletePopup();
      }
    },
    changeSectionDialog() {
      this.sectionDialog = !this.sectionDialog;
      /*
                      this.fields = [];

                      if(this.sectionDialog === false) {
                          this.getFieds()
                          this.getConfig()
                          this.updateFilds()
                          this.initValue()
                          this.fieldCode= 'name'
                      } else {
                          this.listFieds2.forEach(item => {
                              this.fields.push(item);
                          })
                          this.fieldCode = null
                          this.getConfig()
                          this.updateFilds()
                          this.initValue()
                      }
      */
    },
    changeShowCreateProperty(boolean) {
      this.configName = "";
      this.isConfigRequired = "OPTIONAL";
      this.isConfigManual = "MANUAL";
      this.defaultValue = "";
      this.isShowCreateProperty = boolean;
      if (boolean === true) {
        this.updateCreating("part");
        setTimeout(() => {
          document.getElementById(`added-input`).focus();
        }, 200);
      }
    },
    cancelEditCreateProperty() {
      this.changeShowCreateProperty(false);
    },
    getFieds() {
      // this.fields = this.listFieds1;
      this.listFieds2.forEach((item) => {
        this.fields.push(item);
      });
      console.log(this.fields, "-----------------------------------fj");
    },
    updateFilds() {
      console.log("updateFilds ", this.initConfigValue);
      if (this.initConfigValue && this.initConfigValue.length > 0) {
        this.initConfigValue.forEach((item) => {
          let currentField = null;
          this.fields.forEach((field) => {
            if (
              field.code === item.fieldName &&
              item.configCategory === this.configCategory
            ) {
              currentField = field;
            }
          });
          if (currentField) {
            currentField.requiredType = item.required
              ? this.requiredTypeOptions.REQUIRED
              : this.requiredTypeOptions.OPTIONAL;
            currentField.valueType = item.defaultInput
              ? this.valueTypeOptions.DEFAULT
              : this.valueTypeOptions.MANUAL;
            currentField.defaultValue = item.defaultInputValue;
            currentField.deletedField = item.deletedField;
          }
        });
      }
    },
    initValue() {
      this.fields.forEach((field) => {
        if (field.isRequired) {
          field.requiredType = this.requiredTypeOptions.REQUIRED;
        } else {
          field.requiredType = this.requiredTypeOptions.OPTIONAL;
        }
        field.valueType = this.valueTypeOptions.MANUAL;
      });
    },
    updateConfigDataMore() {
      this.fields.forEach((field) => {
        if (field.code == "category") {
          let fs = this.fields.filter(
            (item) => item.code == "quantityRequired"
          );
          if (fs.length > 0 && fs[0].requiredType != field.requiredType) {
            fs[0].requiredType = field.requiredType;
            this.updateConfigDataSubmit(fs[0]);
          }
        }
      });
    },
    updateConfigData(field) {
      this.updateConfigDataSubmit(field);
      this.updateConfigDataMore();
    },
    updateConfigDataSubmit(field) {
      let required = false;
      if (field.requiredType === this.requiredTypeOptions.REQUIRED) {
        required = true;
      }

      let isDefault = false;
      if (field.valueType === this.valueTypeOptions.DEFAULT) {
        isDefault = true;
      }

      let params = {
        configCategory: this.configCategory,
        fieldName: field.code,
        required: required,
        defaultInput: isDefault,
        defaultInputValue: field.defaultValue,
      };
      if (isDefault) {
        if (["sizeUnit", "weightUnit"].includes(field.code)) {
          field.defaultValue = this.codes[field.unitSelections][0].title;
        }
      }
      if (field.id) {
        params.id = field.id;
      }
      this.$emit("update-config", params, field);
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.handleIsChange();
    },
    /*
                getConfig() {
                    axios.get('/api/config')
                      .then((response) => {
                          this.initConfigValue = response.data;
                          this.updateFilds();
                      }).catch((error) => {
                      console.log(error.response);
                  });
                },
    */
    getListConfigCustomField() {
      axios
        .get("/api/custom-field?objectType=PART")
        .then((response) => {
          this.listConfigCustomField = response.data;
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    removeAlertUpdate(index) {
      this.$refs["edit-field-" + index][0].setCustomValidity("");
      this.$forceUpdate();
    },
    removeAlertCreate() {
      this.$refs["added-field"].setCustomValidity("");
      this.$forceUpdate();
    },
    addNewConfigCustomField() {
      if (!this.configName || this.configName.trim() === "") {
        this.$refs["added-field"].setCustomValidity(
          "Please input the property name."
        );
        this.$refs["btn-checklist-create"].click();
      } else {
        const params = {
          objectType: "PART",
          fieldName: this.configName.trim(),
          required: this.isConfigRequired === this.requiredTypeOptions.REQUIRED,
          defaultInput: this.isConfigManual === this.valueTypeOptions.DEFAULT,
          defaultInputValue: this.defaultValue.trim(),
        };
        axios
          .post("/api/custom-field", params)
          .then(async () => {
            const data = await axios.get("/api/custom-field?objectType=PART");
            this.listConfigCustomField = data.data;
            this.cancelEditCreateProperty();
          })
          .catch((error) => {
            if (
              error.response.data ===
              "Property Name is already registered in the system."
            ) {
              this.$refs["added-field"].setCustomValidity(error.response.data);
            }
            this.$refs["btn-checklist-create"].click();
          });
      }
    },
    updateConfigCustomField(field, index) {
      if (!field.fieldName || field.fieldName.trim() === "") {
        this.$refs["edit-field-" + index][0].setCustomValidity(
          "Please input the property name."
        );
        this.$refs["btn-checklist-" + index][0].click();
      } else {
        const params = {
          objectType: "PART",
          fieldName: field.fieldName.trim(),
          required: field.required,
          defaultInput: field.defaultInput,
          defaultInputValue: field.defaultInputValue.trim(),
        };
        axios
          .put(`/api/custom-field/${field.id}`, params)
          .then(() => {
            this.getListConfigCustomField();
            this.focusReasonIndex = null;
          })
          .catch((error) => {
            console.log(error);
            // this.$refs['edit-field-' + index][0].required=true;
            // if (error.response.data === 'The property not exist.') {
            //   this.getListConfigCustomField()
            // }
            if (
              error.response.data ===
              "Property Name is already registered in the system."
            ) {
              this.$refs["edit-field-" + index][0].setCustomValidity(
                error.response.data
              );
            }
            this.$refs["btn-checklist-" + index][0].click();
          });
      }
    },
    confirmDelete() {
      const params = {
        configCategory: "PART",
        fieldName: this.fieldDelete.code,
        required: this.fieldDelete.requiredType === "REQUIRED",
        defaultInput: this.fieldDelete.valueType === "DEFAULT",
        defaultInputValue: this.fieldDelete.defaultValue
          ? this.fieldDelete.defaultValue.trim()
          : "",
        id: this.fieldDelete.id,
      };
      axios
        .post(`/api/config/delete-fixed-property`, params)
        .then((res) => {
          console.log(res);
          this.visibleSuccess = true;
          // this.getFieds()
          // this.getConfig()
          // this.updateFilds()
          // this.initValue()
          $("#op-delete-popup").modal("hide");
          this.fields.forEach((field) => {
            if (field.code === this.fieldDelete.code) {
              field.deletedField = true;
              this.$forceUpdate();
            }
          });
        })
        .catch((error) => {
          console.log(error);
          // if (error.response.data === 'The property not exist.') {
          //   this.getListConfigCustomField()
          // }
        });
    },
    deleteConfigCustomField(field) {
      axios
        .delete(`/api/custom-field/${field.id}`)
        .then(() => {
          this.listConfigCustomField = this.listConfigCustomField.filter(
            (item) => item.id !== field.id
          );
        })
        .catch((error) => {
          console.log(error.response);
          if (error.response.data === "The property not exist.") {
            this.getListConfigCustomField();
          }
        })
        .finally(() => {
          this.focusReasonIndex = null;
        });
    },
    cancelUpdateConfigCustomField(index) {
      this.listConfigCustomField[index] = this.oldField;
      this.$forceUpdate();
      this.focusReasonIndex = null;
    },
    focusReason(field, index) {
      if (this.focusReasonIndex !== null) {
        this.listConfigCustomField[this.oldField.index] = this.oldField;
      }
      this.oldField = {
        fieldName: field.fieldName,
        required: field.required,
        defaultInputValue: field.defaultInputValue,
        defaultInput: field.defaultInput,
        id: field.id,
        index: index,
      };
      this.focusReasonIndex = index;
    },
    handleChangeUnitField(value, field) {
      let newField = { ...field };
      field.defaultValue = value;
      console.log("handleChangeUnitField-------", value, field);
      console.log("new Field ---------------", newField);
      //this.updateConfigData(newField)
      this.handleIsChange();
    },
  },
};
</script>
<style src="/components/configuration/PartConfiguration/index.css"></style>
<style>
.toggleWrapper input.mobileToggle:checked + .change-checkout-status:before {
  background: #3585e5;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

.card-header strong {
  font-size: 14.66px;
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
}
.field-label {
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px !important;
  line-height: 17px;
  color: #4b4b4b;
}
</style>
