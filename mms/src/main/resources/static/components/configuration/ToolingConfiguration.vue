<template>
  <div>
    <div class="config-header">
      <div class="left-header">
        <strong
          v-text="resources['tooling']"
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
      <div
        class="card"
        v-for="(section, sectionIndex) in sections"
        :key="sectionIndex"
        v-if="sectionIndex < maxSectionsShow"
      >
        <div class="card-header" v-if="section.category == 'BASIC'">
          <strong v-text="resources['basic_info_static']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'PHYSICAL'">
          <strong v-text="resources['physical_info']"></strong>
        </div>
        <div
          class="card-header"
          v-else-if="section.category == 'RUNNER_SYSTEM'"
        >
          <strong v-text="resources['runner_system_info']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'PRODUCTION'">
          <strong v-text="resources['production_information']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'MAINTENANCE'">
          <strong v-text="resources['maintenance_info']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'COST'">
          <strong v-text="resources['cost_info']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'SUPPLIER'">
          <strong v-text="resources['supplier_info']"></strong>
        </div>
        <div class="card-header" v-else-if="section.category == 'PART'">
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
              v-for="(field, sub_index) in section.fields"
              :class="{ 'd-none': field.deletedField }"
              :key="sub_index"
            >
              <label
                class="col-md-2 col-form-label"
                v-if="sub_index < MAX_ITEM_SHOW_LESS || sectionDialog"
              >
                {{ field.label }}
                <span
                  class="badge-require"
                  v-show="field.requiredType === requiredTypeOptions.REQUIRED"
                ></span
              ></label>
              <div
                class="d-flex col-md-10 col-form-label"
                v-if="sub_index < MAX_ITEM_SHOW_LESS || sectionDialog"
              >
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
                            !categoryStatus.enabled || field.isRequired
                          "
                          class="form-check-input"
                          :value="requiredTypeOptions.REQUIRED"
                          @change="changeHandler"
                          v-model="field.requiredType"
                        />Required
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          type="radio"
                          :disabled="
                            !categoryStatus.enabled || field.isRequired
                          "
                          class="form-check-input"
                          :value="requiredTypeOptions.OPTIONAL"
                          @change="changeHandler"
                          v-model="field.requiredType"
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
                          @change="changeHandler"
                          v-model="field.valueType"
                        />
                        <span v-text="resources['manual_input']"></span>
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
                            !categoryStatus.enabled || field.isNotExistedDefault
                          "
                          class="form-check-input"
                          :value="valueTypeOptions.DEFAULT"
                          @change="changeHandler"
                          v-model="field.valueType"
                        />
                        <span v-text="resources['default']"></span>
                      </label>
                    </div>
                    <!--                                    <div class="default-value">-->
                    <!--                                      <input type="text"  class="form-control" v-model="field.defaultValue" @change="updateConfigData(field)" v-if="field.valueType === valueTypeOptions.DEFAULT"-->
                    <!--                                             :placeholder="resources['default']">-->
                    <!--                                    </div>-->
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
                  style="width: 22%"
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
                      :type="field.unitSelections"
                      @change-unit="
                        (value) => handleChangeUnitField(value, field)
                      "
                    ></unit-selection>
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
                      :placeholder="resources['default']"
                      @change="changeHandler"
                      :disabled="!categoryStatus.enabled"
                    />
                  </div>
                </div>
                <!--                                <div style="width: 100%;flex-direction: row-reverse;align-items: flex-end;justify-content: flex-end;" class="action-zone" :class="{'pt-1': field.valueType === valueTypeOptions.DEFAULT}">-->
                <!--                                  <div class="default-value">-->
                <!--                                    <input style="max-width: 35%;margin-bottom: 8px;" type="text"  class="form-control" v-model="field.defaultValue" @change="updateConfigData(field)" v-if="field.valueType === valueTypeOptions.DEFAULT"-->
                <!--                                           :placeholder="resources['default']">-->
                <!--                                  </div>-->
                <!--                                </div>-->
              </div>
            </div>

            <form
              class="row"
              action=""
              v-for="(field, index2) in mappingListConfigCustomField(
                section.category
              )"
              :key="section.category + index2 + 'config'"
            >
              <label
                v-if="focusReasonIndex !== section.category + index2"
                class="col-md-2 col-form-label"
              >
                {{ field.fieldName }}
                <span class="badge-require" v-show="field.required"></span>
              </label>
              <div v-else class="col-md-2 col-form-label">
                <input
                  :ref="'edit-field-' + section.category + index2"
                  @keyup="removeAlertUpdate(section.category, index2)"
                  v-model="
                    mappingListConfigCustomField(section.category)[index2]
                      .fieldName
                  "
                  class="form-control reason-edit-input focus"
                />
              </div>
              <div class="d-flex col-md-10 col-form-label">
                <div style="margin-right: 0.75rem">
                  <div class="row">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="
                            mappingListConfigCustomField(section.category)[
                              index2
                            ].required
                          "
                          :disabled="
                            !categoryStatus.enabled ||
                            focusReasonIndex !== section.category + index2
                          "
                          type="radio"
                          class="form-check-input"
                          :value="true"
                        />
                        <span v-text="resources['required']"></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <!--                                :disabled="!categoryStatus.enabled || focusReasonIndex !== section.category + index2"-->
                        <input
                          v-model="
                            mappingListConfigCustomField(section.category)[
                              index2
                            ].required
                          "
                          :disabled="
                            !categoryStatus.enabled ||
                            focusReasonIndex !== section.category + index2
                          "
                          type="radio"
                          class="form-check-input"
                          :value="false"
                        />
                        <span v-text="resources['optional']"></span>
                      </label>
                    </div>
                    <!--                            <div class="action-zone">-->
                    <!--                              <div class="confirm-zone">-->
                    <!--                                  <span v-if="focusReasonIndex !== section.category + index2" class="checklist-edit action-item" :class="{'disable-click' : !categoryStatus.enabled}" @click="focusReason(field, section.category, section.category + index2)">-->
                    <!--                                    <img src="/images/checklist/pencil.svg" class="icon-primary" alt="edit" />-->
                    <!--                                    <img src="/images/checklist/pencil-hover.svg" class="icon-hover" alt="edit" />-->
                    <!--                                  </span>-->
                    <!--                                <span v-if="focusReasonIndex !== section.category + index2" class="checklist-delete action-item" :class="{'disable-click' : !categoryStatus.enabled}" @click="deleteConfigCustomField(field)">-->
                    <!--                                    <img src="/images/checklist/trash.svg" class="icon-primary" alt="delete" />-->
                    <!--                                    <img src="/images/checklist/trash-hover.svg" class="icon-hover" alt="delete" />-->
                    <!--                                  </span>-->
                    <!--                                <button hidden type="submit" :ref="'btn-checklist-' + section.category + index2"></button>-->
                    <!--                                <span v-if="focusReasonIndex === section.category + index2" class="checklist-confirm-edit" @click="updateConfigCustomField(field, index2, section.category)">-->
                    <!--                                    <img src="/images/checklist/check.svg" alt="confirm-edit" />-->
                    <!--                                  </span>-->
                    <!--                                <span v-if="focusReasonIndex === section.category + index2" class="checklist-cancel-edit" @click="cancelUpdateConfigCustomField(section.category)">-->
                    <!--                                    <img src="/images/checklist/close.svg" alt="cancel-edit" />-->
                    <!--                                  </span>-->
                    <!--                              </div>-->
                    <!--                            </div>-->
                  </div>

                  <div class="row group-input-type">
                    <div class="form-check first-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="field.defaultInput"
                          :disabled="
                            !categoryStatus.enabled ||
                            focusReasonIndex !== section.category + index2
                          "
                          type="radio"
                          class="form-check-input"
                          :value="false"
                        />
                        <span v-text="resources['manual_input']"></span>
                      </label>
                    </div>
                    <div class="form-check form-check-inline">
                      <label class="form-check-label">
                        <input
                          v-model="field.defaultInput"
                          :disabled="
                            !categoryStatus.enabled ||
                            focusReasonIndex !== section.category + index2
                          "
                          type="radio"
                          class="form-check-input"
                          :value="true"
                        />
                        <span v-text="resources['default']"></span>
                      </label>
                    </div>
                    <!--                            <div class="action-zone">-->
                    <!--                              <div style="margin-top: 8px" class="default-value default-zone">-->
                    <!--                                <input-->
                    <!--                                    v-if="mappingListConfigCustomField(section.category)[index2].defaultInput"-->
                    <!--                                    @change="updateConfigRadio(field, index2, section.category)"-->
                    <!--                                    type="text"-->
                    <!--                                    class="form-control"-->
                    <!--                                    v-model="field.defaultInputValue"-->
                    <!--                                    :placeholder="resources['default']"-->
                    <!--                                >-->
                    <!--                              </div>-->
                    <!--                            </div>-->
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
                    'pt-1': mappingListConfigCustomField(section.category)[
                      index2
                    ].defaultInput,
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
                      v-if="focusReasonIndex !== section.category + index2"
                      class="checklist-edit action-item"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                      @click="
                        focusReason(
                          field,
                          section.category,
                          section.category + index2
                        )
                      "
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
                      v-if="focusReasonIndex !== section.category + index2"
                      class="checklist-delete action-item"
                      @click="deleteConfigCustomField(field, section.category)"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
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
                      :ref="'btn-checklist-' + section.category + index2"
                    ></button>
                    <span
                      v-if="focusReasonIndex === section.category + index2"
                      class="checklist-confirm-edit"
                      @click="
                        updateConfigCustomField(field, index2, section.category)
                      "
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                    >
                      <img
                        src="/images/checklist/check.svg"
                        alt="confirm-edit"
                      />
                    </span>
                    <span
                      v-if="focusReasonIndex === section.category + index2"
                      class="checklist-cancel-edit"
                      @click="cancelUpdateConfigCustomField(section.category)"
                      :class="{ 'disable-click': !categoryStatus.enabled }"
                    >
                      <img
                        src="/images/checklist/close.svg"
                        alt="cancel-edit"
                      />
                    </span>
                  </div>
                  <div
                    v-if="
                      mappingListConfigCustomField(section.category)[index2]
                        .defaultInput
                    "
                    style="max-width: 35%; margin-bottom: 8px"
                    class="default-value default-zone"
                  >
                    <input
                      :disabled="
                        !categoryStatus.enabled ||
                        focusReasonIndex !== section.category + index2
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
            <form
              v-if="isShowCreateProperty === section.category"
              action=""
              class="row"
            >
              <div class="col-md-2 col-form-label">
                <input
                  id="added-input-tooling"
                  :ref="'added-field-' + section.category"
                  @keyup="removeAlertCreate(section.category)"
                  v-model="configName"
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
                      @click="addNewConfigCustomField(section.category)"
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
                      :ref="'btn-checklist-create-' + section.category"
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
                      class="form-control"
                      :placeholder="resources['default']"
                      :disabled="!categoryStatus.enabled"
                    />
                  </div>
                </div>
              </div>
            </form>
            <a
              v-else-if="section.category !== 'PART' && sectionDialog"
              class="btn radio-confirm-modal"
              @click="changeShowCreateProperty(section.category)"
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
            v-if="sectionIndex == maxSectionsShow - 1"
            class="show-more"
            :class="{
              'is-show-less': sectionDialog,
              'disable-click': !categoryStatus.enabled,
            }"
            style="cursor: pointer"
            @click="changeSectionDialog(section)"
          >
            <div>
              <!--                          <div class="mr-1">-->
              <!--                            <img v-if="sectionDialog === false" src="/images/icon/arrow-of-double-angle-pointing-down.svg" height="11">-->
              <!--                            <img v-else src="/images/icon/arrow-of-double-angle-pointing-up.svg" height="11">-->
              <!--                          </div>-->
              <!--                          <span class="dialogTooling" v-if="sectionDialog === false" v-text="resources['show_more']"></span>-->
              <!--                          <span class="dialogTooling" v-else v-text="resources['show_less']"></span>-->
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
    <delete-fixed-tooling
      key="tooling-config"
      @delete="confirmDelete"
      :list="fieldDelete"
      name="Tooling"
      :resources="resources"
    ></delete-fixed-tooling>
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
          id="handleOkay3"
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
  name: "ToolingConfiguration",
  props: {
    initConfigValue: {
      type: Array,
      default: () => [],
    },
    handleIsChange: Function,
    resources: Object,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    isCreating: String,
    updateCreating: Function,
    codes: Object,
  },
  components: {
    "delete-fixed-tooling": httpVueLoader(
      "/components/delete-fixed-tooling.vue"
    ),
    "unit-selection": httpVueLoader(
      "/components/configuration/unit-selection.vue"
    ),
  },
  data() {
    return {
      visibleSuccess: false,
      styleHeight: "200",
      configCategory: "TOOLING",
      requiredTypeOptions: {
        REQUIRED: "REQUIRED",
        OPTIONAL: "OPTIONAL",
      },
      valueTypeOptions: {
        MANUAL: "MANUAL",
        DEFAULT: "DEFAULT",
      },
      // sectionIndex: 1,
      sectionDialog: false,
      // sectionSategory: 'BASIC',

      configName: null,
      isConfigRequired: "OPTIONAL",
      isConfigManual: "MANUAL",
      defaultValue: "",
      focusReasonIndex: null,
      listConfigCustomField: null,
      isShowCreateProperty: null,
      // oldField: null,
      // sections: [],
      // listSection: [],
      MAX_ITEM_SHOW_LESS: 5,
      maxSectionsShow: 1,
      deletePopup: false,
      fieldDelete: {},
      // fieldCode: 'toolingType',
      /*
                      listItem: [
                          {
                              dialog: false,
                              category: 'BASIC',
                              index: '5',
                              fields: [
                                  {
                                      code: 'equipmentCode',
                                      label: this.resources['tooling_id'],
                                      requiredType: null,
                                      isRequired: true,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  {
                                      code: 'toolingLetter',
                                      label: this.resources['tooling_letter'],
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  {
                                      code: 'toolingType',
                                      label: this.resources['tooling_type'],
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  {
                                      code: 'toolingComplexity',
                                      label: this.resources['tooling_complexity'],
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  {
                                      code: 'designedShot',
                                      label: this.resources['forecasted_max_shot'],
                                      requiredType: null,
                                      isRequired: true,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  {
                                      code: 'lifeYears',
                                      label: this.resources['forecasted_tool_life'],
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null,
                                      notDyson: true
                                  },
                                  {
                                      code: 'madeYear',
                                      label: this.resources['year_of_tool_made'],
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null
                                  },
                                  // {
                                  //     code: 'approvedCycleTime',
                                  //     label: 'Approved Cycle Time (s)',
                                  //     requiredType: null,
                                  //     isRequired: true,
                                  //     valueType: null,
                                  //     defaultValue: null
                                  // },
                                  {
                                      code: 'locationId',
                                      label: this.resources['location'],
                                      requiredType: null,
                                      isRequired: true,
                                      valueType: null,
                                      defaultValue: null,
                                      isNotExistedDefault: true
                                  },
                                  {
                                    code: 'engineers',
                                    label: this.resources['engineer_in_charge'],
                                    requiredType: null,
                                    valueType: null,
                                    defaultValue: null,
                                    isNotExistedDefault: true
                                  },
                                  {
                                      code: 'toolDescription',
                                      label: 'Tool Description',
                                      requiredType: null,
                                      valueType: null,
                                      defaultValue: null
                                  },
                              ]
                          },
                      ],
      */
      // listItem1: [
      sections: [
        {
          dialog: false,
          category: "BASIC",
          index: "15",
          fields: [
            {
              code: "equipmentCode",
              label: this.resources["tooling_id"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "supplierMoldCode",
              label: this.resources["supplier_tooling_id"],
              requiredType: null,
              isRequired: null,
              valueType: null,
              defaultValue: null,
            },

            {
              code: "counterId",
              label: this.resources["counter_id"],
              requiredType: null,
              isRequired: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "accumulatedShots",
              label: this.resources["accumulated_shots"],
              requiredType: null,
              isRequired: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "toolingLetter",
              label: this.resources["tooling_letter"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "toolingType",
              label: this.resources["tooling_type"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "backup",
              label: this.resources["backup_tooling"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "toolingComplexity",
              label: this.resources["tooling_complexity"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "designedShot",
              label: this.resources["forecasted_max_shot"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "lifeYears",
              label: this.resources["forecasted_tool_life"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              // notDyson: true,
            },
            {
              code: "madeYear",
              label: this.resources["year_of_tool_made"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            // {
            //     code: 'approvedCycleTime',
            //     label: this.resources['approved_cycle_time'],
            //     requiredType: null,
            //     isRequired: true,
            //     valueType: null,
            //     defaultValue: null
            // },
            {
              code: "locationId",
              label: this.resources["location"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "engineers",
              label: this.resources["oem_engineer_in_charge"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "plantEngineers",
              label: this.resources["plant_engineer_in_charge"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "toolDescription",
              label: this.resources["tool_description"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
          ],
        },
        {
          dialog: false,
          category: "PHYSICAL",
          index: "15",
          fields: [
            // {
            //     code: 'size',
            //     label: this.resources['tool_size'],
            //     requiredType: null,
            //     valueType: null,
            //     defaultValue: null,
            // },
            {
              code: "size_w",
              label: this.resources["tool_size_w"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "size_l",
              label: this.resources["tool_size_l"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "size_h",
              label: this.resources["tool_size_h"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "sizeUnit",
              label: this.resources["tool_size_unit"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: "SizeUnit",
            },
            {
              code: "weight",
              label: this.resources["tool_weight"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "weightUnit",
              label: this.resources["tool_weight_unit"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: "WeightUnit",
            },
            {
              code: "shotSize",
              label: this.resources["shot_weight"] + "(gram)",
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "toolMakerCompanyName",
              label: this.resources["toolmaker"],
              isRequired: true,
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "injectionMachineId",
              label: this.resources["injection_molding_machine_id"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "totalCavities",
              label: this.resources["total_number_of_cavities"],
              isRequired: true,
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "quotedMachineTonnage",
              label: this.resources["machine_tonnage"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "toolingPictureFile",
              label: this.resources["tooling_picture"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
          ],
        },
        {
          dialog: false,
          category: "RUNNER_SYSTEM",
          index: "14",
          fields: [
            {
              code: "runnerType",
              label: this.resources["type_of_runner_system"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "runnerMaker",
              label: this.resources["maker_of_runner_system"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "weightRunner",
              label: this.resources["weight_of_runner_system_g"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "hotRunnerDrop",
              label: this.resources["hot_runner_number_of_drop"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "hotRunnerZone",
              label: this.resources["hot_runner_zone"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
          ],
        },
        {
          dialog: false,
          category: "COST",
          index: "15",
          fields: [
            {
              code: "cost",
              label: this.resources["acquisition_cost"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "costCurrencyType",
              label: this.resources["cost_of_tooling_unit"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: "CurrencyType",
            },
            {
              code: "salvageValue",
              label: this.resources["salvage_value"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: null,
            },
            {
              code: "poDate",
              label: this.resources["po_date"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: null,
            },
            {
              code: "poNumber",
              label: this.resources["po_number"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              unitSelections: null,
            },
            {
              code: "forthFiles",
              label: this.resources["po_document"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "memo",
              label: this.resources["memo"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
          ],
        },
        {
          dialog: false,
          category: "SUPPLIER",
          index: "15",
          fields: [
            {
              code: "supplierCompanyName",
              label: this.resources["supplier"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            // {
            //   code: 'uptimeTarget',
            //   label: this.resources['uptime_target'],
            //   requiredType: null,
            //   isRequired: true,
            //   valueType: null,
            //   defaultValue: null
            // },
            // {
            //   code: 'uptimeLimitL1',
            //   label: this.resources['uptime_tolerance_l1'],
            //   requiredType: null,
            //   isRequired: true,
            //   valueType: null,
            //   defaultValue: null
            // },
            // {
            //   code: 'uptimeLimitL2',
            //   label: this.resources['uptime_tolerance_l2'],
            //   requiredType: null,
            //   isRequired: true,
            //   valueType: null,
            //   defaultValue: null
            // },
            {
              code: "labour",
              label: this.resources["required_labor"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "shiftsPerDay",
              label: this.resources["production_hour_per_day"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "productionDays",
              label: this.resources["production_day_per_week"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "maxCapacityPerWeek",
              label: this.resources["maximum_capacity_per_week"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
            },
          ],
        },
        {
          dialog: false,
          category: "PRODUCTION",
          index: "15",
          fields: [
            {
              code: "approvedCycleTime",
              label: this.resources["approved_cycle_time_form_second"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "cycleTimeLimit1",
              label: this.resources["cycle_time_tolerance_l1"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "cycleTimeLimit1Unit",
              label: this.resources["cycle_time_tolerance_l1_unit"],
              requiredType: null,
              isRequired: null,
              valueType: null,
              defaultValue: null,
              unitSelections: "OutsideUnit",
            },
            {
              code: "cycleTimeLimit2",
              label: this.resources["cycle_time_tolerance_l2"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "cycleTimeLimit2Unit",
              label: this.resources["cycle_time_tolerance_l2_unit"],
              requiredType: null,
              isRequired: null,
              valueType: null,
              defaultValue: null,
              unitSelections: "OutsideUnit",
            },
            {
              code: "uptimeTarget",
              label: this.resources["uptime_target"] + " (%)",
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "uptimeLimitL1",
              label: this.resources["uptime_tolerance_l1"] + " (%)",
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "uptimeLimitL2",
              label: this.resources["uptime_tolerance_l2"] + " (%)",
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
          ],
        },
        {
          dialog: false,
          category: "MAINTENANCE",
          index: "15",
          fields: [
            // {
            //     code: 'toolingConditionFiles',
            //     label: this.resources['condition_of_tooling'],
            //     requiredType: null,
            //     valueType: null,
            //     defaultValue: null,
            //     isNotExistedDefault: true
            // },
            {
              code: "preventCycle",
              label: this.resources["maintenance_interval"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            {
              code: "preventUpcoming",
              label: this.resources["upcoming_maintenance_tolerance"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            // {
            //   code: 'preventOverdue',
            //   label: this.resources['overdue_maintenance_tolerance'],
            //   requiredType: null,
            //   isRequired: true,
            //   valueType: null,
            //   defaultValue: null
            // },
            {
              code: "documentFiles",
              label: this.resources["maintenance_document"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "instructionVideo",
              label: this.resources["instruction_video"],
              requiredType: null,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
          ],
        },
        {
          dialog: false,
          category: "PART",
          index: "3",
          fields: [
            {
              code: "partId",
              label: this.resources["part_id"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
              isNotExistedDefault: true,
            },
            {
              code: "cavity",
              label: this.resources["working_cavities"],
              requiredType: null,
              isRequired: true,
              valueType: null,
              defaultValue: null,
            },
            // {
            //     code: 'totalCavities',
            //     label: this.resources['total_number_of_cavities'],
            //     requiredType: null,
            //     isRequired: true,
            //     valueType: null,
            //     defaultValue: null
            // }
          ],
        },
      ],
      options: {},
    };
  },
  watch: {
    isCreating(value) {
      if (value !== "tooling") {
        this.changeShowCreateProperty(false);
      }
    },
    initConfigValue: function (value, oldValue) {
      /*
                    this.listSection = value
      */
      this.updateSection();
      /*
                      if (value && value.length > 0) {
                          value.forEach(item => {
                              let currentField = null;
                              this.sections.forEach(section => {
                                  section.fields.forEach(field => {
                                      if (field.code === item.fieldName && item.configCategory === this.configCategory) {
                                          currentField = field;
                                      }
                                  });
                              });

                              if (currentField) {
                                  currentField.requiredType = item.required ? this.requiredTypeOptions.REQUIRED : this.requiredTypeOptions.OPTIONAL;
                                  currentField.valueType = item.defaultInput ? this.valueTypeOptions.DEFAULT :  this.valueTypeOptions.MANUAL;
                                  currentField.defaultValue = item.defaultInputValue;
                                  currentField.id = item.id;
                              }
                          })
                      }
      */
    },
  },
  created() {
    this.getListConfigCustomField();
  },
  mounted() {
    // this.getSection();
    this.checkingFieldForDyson();
    this.initValue();
    this.updateSection();
  },
  methods: {
    cancelData() {
      this.checkingFieldForDyson();
      this.initValue();
      this.updateSection();
      this.getListConfigCustomField();
    },
    saveData() {
      this.sections.forEach((section) => {
        section.fields.forEach((field) => {
          this.updateConfigData(field);
        });
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
      const el = document.getElementById("handleOkay3");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        el.classList.remove("primary-animation");
        self.visibleSuccess = false;
      }, 700);
    },
    showDeletePopup(field) {
      console.log(field.label, "comeeee 1");
      this.fieldDelete = field;
      var child = Common.vue.getChild(this.$children, "delete-fixed-tooling");
      if (child != null) {
        child.showDeletePopup();
      }
    },
    mappingListConfigCustomField(category) {
      if (this.listConfigCustomField) {
        let _category = "TOOLING_" + category;
        return this.listConfigCustomField.filter(
          (item) => item.propertyGroup === _category
        );
      }
    },
    /*
                getSection () {
                    this.sections = this.listItem
                },
    */
    changeSectionDialog(section) {
      this.sectionDialog = !this.sectionDialog;
      /*
                      this.sections = []
      */
      console.log(
        "this.sectionDialog=======================",
        this.sectionDialog,
        "index",
        section.index
      );
      if (this.sectionDialog === false) {
        this.maxSectionsShow = 1;
        /*
                            section.index = '5'
                            this.getSection()
                            this.sectionSategory = 'BASIC'
                            // this.fieldCode = 'toolingType'
                            this.checkingFieldForDyson()
                            this.initValue()
                            this.updateSection()
                            console.log('this.sectionSategory-------------------is.listItem)', this.sections)
        */
      } else {
        this.maxSectionsShow = this.sections.length;
        /*
                            this.listItem1.forEach(item => {
                                this.sections.push(item);
                                section.index = '5';
                            })
                            // this.fieldCode = null
                            console.log('this.sections.push(this.listItem)', this.sections)
                            this.sectionSategory = 'PART'
                            this.checkingFieldForDyson()
                            this.initValue()
                            this.updateSection()
        */
      }
      this.checkingFieldForDyson();
      console.log("this.maxSectionsShow:", this.maxSectionsShow);
    },
    updateSection() {
      console.log(
        "swwwwwwwwwwwwwwwwwwwwww initConfigValue",
        this.initConfigValue
      ); //use only for init
      // console.log('swwwwwwwwwwwwwwwwwwwwww updateSection', this.listSection)
      if (this.initConfigValue && this.initConfigValue.length > 0) {
        this.initConfigValue.forEach((item) => {
          let currentField = null;
          this.sections.forEach((section) => {
            section.fields.forEach((field) => {
              if (
                field.code === item.fieldName &&
                item.configCategory === this.configCategory
              ) {
                currentField = field;
              }
            });
          });

          if (currentField) {
            currentField.requiredType =
              currentField.isRequired || item.required
                ? this.requiredTypeOptions.REQUIRED
                : this.requiredTypeOptions.OPTIONAL;
            currentField.valueType = item.defaultInput
              ? this.valueTypeOptions.DEFAULT
              : this.valueTypeOptions.MANUAL;
            currentField.defaultValue = item.defaultInputValue;
            currentField.deletedField = item.deletedField;
            currentField.id = item.id;
          }
        });
      }
    },
    initValue() {
      this.sections.forEach((section) => {
        section.fields.forEach((field) => {
          if (field.isRequired) {
            field.requiredType = this.requiredTypeOptions.REQUIRED;
          } else {
            field.requiredType = this.requiredTypeOptions.OPTIONAL;
          }
          field.valueType = this.valueTypeOptions.MANUAL;
        });
      });
    },
    updateConfigData(field) {
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
        if (["costCurrencyType"].includes(field.code)) {
          field.defaultValue = this.codes[field.unitSelections][0].code;
        }
        if (
          [
            "sizeUnit",
            "weightUnit",
            "cycleTimeLimit1Unit",
            "cycleTimeLimit2Unit",
          ].includes(field.code)
        ) {
          field.defaultValue = this.codes[field.unitSelections][0].title;
        }
      }
      if (field.id) {
        params.id = field.id;
      }
      this.$emit("update-config", params);
    },
    async checkingFieldForDyson() {
      try {
        // const server = await Common.getSystem('server')
        // const condition = server === 'dyson'
        const options = await Common.getSystem("options");
        this.options = JSON.parse(options);
        const isUptimeTargetRequired = Boolean(
          this.options?.CLIENT?.moldDetail?.uptimeTargetRequired
        );

        if (!isUptimeTargetRequired) {
          this.sections.forEach((section) => {
            section.fields = section.fields.filter((field) => !field.notDyson);
            if (section.category === "SUPPLIER") {
              section.fields.forEach((field) => {
                if (field.code === "uptimeTarget") {
                  console.log("uptimeTarget", field);
                  field.isNotExistedDefault = true;
                }
              });
            }
          });
        }
      } catch (error) {
        console.log(error);
      }
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.handleIsChange();
    },
    //custom property
    changeShowCreateProperty(section) {
      this.configName = "";
      this.isConfigRequired = "OPTIONAL";
      this.isConfigManual = "MANUAL";
      this.defaultValue = "";
      this.isShowCreateProperty = section;
      if (section !== false) {
        this.updateCreating("tooling");
        setTimeout(() => {
          document.getElementById(`added-input-tooling`).focus();
        }, 200);
      }
    },
    cancelEditCreateProperty() {
      this.changeShowCreateProperty(false);
    },
    getListConfigCustomField() {
      axios
        .get(`/api/custom-field?objectType=TOOLING`)
        .then((response) => {
          this.listConfigCustomField = response.data;
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    removeAlertUpdate(category, index) {
      // this.$refs['edit-field-' + index][0].setCustomValidity("");
      this.$refs["edit-field-" + category + index][0].setCustomValidity("");
      this.$forceUpdate();
    },
    removeAlertCreate(category) {
      this.$refs["added-field-" + category][0].setCustomValidity("");
      this.$forceUpdate();
    },
    addNewConfigCustomField(category) {
      if (!this.configName || this.configName.trim() === "") {
        this.$refs["added-field-" + category][0].setCustomValidity(
          "Please input the property name."
        );
        this.$refs["btn-checklist-create-" + category][0].click();
      } else {
        const params = {
          objectType: "TOOLING",
          propertyGroup: "TOOLING_" + category,
          fieldName: this.configName.trim(),
          required: this.isConfigRequired === this.requiredTypeOptions.REQUIRED,
          defaultInput: this.isConfigManual === this.valueTypeOptions.DEFAULT,
          defaultInputValue: this.defaultValue.trim(),
        };
        axios
          .post("/api/custom-field", params)
          .then(async () => {
            // await this.getListConfigCustomField()
            const data = await axios.get(
              "/api/custom-field?objectType=TOOLING"
            );
            this.listConfigCustomField = data.data;
            this.mappingListConfigCustomField(category);
            this.cancelEditCreateProperty();
            // this.focusReasonIndex = null;
          })
          .catch((error) => {
            // this.$refs['added-field-' + category][0].required=true;
            if (
              error.response.data ===
              "Property Name is already registered in the system."
            ) {
              this.$refs["added-field-" + category][0].setCustomValidity(
                error.response.data
              );
            }
            // if (error.response.data === 'Please input the property name.') {
            //   this.$refs['added-field-' + category][0].setCustomValidity(error.response.data);
            // }
            // this.$refs['added-field-' + category][0].setCustomValidity(error.response.data);
            this.$refs["btn-checklist-create-" + category][0].click();
          });
      }
    },
    updateConfigCustomField(field, index, category) {
      if (!field.fieldName || field.fieldName.trim() === "") {
        // this.$refs['edit-field-' + index][0].setCustomValidity('Please input the property name.');
        this.$refs["edit-field-" + category + index][0].setCustomValidity(
          "Please input the property name."
        );
        this.$refs["btn-checklist-" + category + index][0].click();
      } else {
        const params = {
          objectType: "TOOLING",
          propertyGroup: "TOOLING_" + category,
          fieldName: field.fieldName.trim(),
          required: field.required,
          defaultInput: field.defaultInput,
          defaultInputValue: field.defaultInputValue.trim(),
        };
        axios
          .put(`/api/custom-field/${field.id}`, params)
          .then(async () => {
            const data = await axios.get(
              `/api/custom-field?objectType=TOOLING`
            );
            this.listConfigCustomField = data.data;
            this.mappingListConfigCustomField(category);
            this.focusReasonIndex = null;
          })
          .catch((error) => {
            // this.$refs['edit-field-' + category + index][0].required=true;
            if (
              error.response.data ===
              "Property Name is already registered in the system."
            ) {
              this.$refs["edit-field-" + category + index][0].setCustomValidity(
                error.response.data
              );
            }
            if (error.response.data === "The property not exist.") {
              this.getListConfigCustomField();
            }
            this.$refs["btn-checklist-" + category + index][0].click();
          });
      }
    },
    confirmDelete() {
      const params = {
        configCategory: "TOOLING",
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
          this.visibleSuccess = true;
          $("#op-delete-popup2").modal("hide");
          this.sections.forEach((section) => {
            section.fields.forEach((field) => {
              if (field.code === this.fieldDelete.code) {
                field.deletedField = true;
                this.$forceUpdate();
              }
            });
          });
        })
        .catch((error) => {
          console.log(error);
        });
    },
    deleteConfigCustomField(field, category) {
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
    async cancelUpdateConfigCustomField(category) {
      const data = await axios.get(`/api/custom-field?objectType=TOOLING`);
      this.listConfigCustomField = data.data;
      this.focusReasonIndex = null;
    },
    async focusReason(field, category, index) {
      if (this.focusReasonIndex !== null) {
        const data = await axios.get(`/api/custom-field?objectType=TOOLING`);
        this.listConfigCustomField = data.data;
      }
      this.focusReasonIndex = index;
    },
    handleChangeUnitField(value, field) {
      let newField = { ...field };
      field.defaultValue = value;
      console.log("handleChangeUnitField", value, field);
      //this.updateConfigData(newField)
      this.handleIsChange();
    },
  },
};
</script>
<style>
.toggleWrapper input.mobileToggle:checked + .change-checkout-status:before {
  background: #3585e5;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

.ant-modal-body {
  padding: 0;
  border-radius: 6px;
}
</style>
<style scoped>
.dialogTooling {
  text-align: center;
  /*width: 100%;*/
}
.dialogTooling:hover {
  color: blue;
}
.configuration {
  -webkit-mask-image: -webkit-gradient(
    linear,
    left 30%,
    left bottom,
    from(rgba(0, 0, 0, 0.8)),
    to(rgba(0, 0, 0, 0.1))
  );
}
.radio-confirm-modal:hover span {
  color: #76a4ec;
}
.radio-confirm-modal:focus span {
  color: #183abe;
}
.radio-confirm-modal:active span {
  color: #1d44d9;
}
.action-zone {
  display: flex;
  flex: 1;
  flex-direction: column;
  height: 100%;
  justify-content: center;
}
.confirm-create-zone {
  margin-bottom: 8px;
  height: 100%;
  display: flex;
  align-items: center;
  margin-left: 15px;
}
.confirm-zone img {
  width: 18px;
}
.confirm-zone span {
  cursor: pointer;
}
.confirm-zone .checklist-cancel-edit {
  width: 15px;
  margin-left: 12px;
}
.confirm-zone .checklist-cancel-edit img {
  width: 15px !important;
}
.show-more > div {
  /*margin-top: 30px;*/
  display: flex;
  justify-content: center;
  z-index: 1;
}
.show-more div,
.show-more span {
  z-index: 1;
}
.isDisable {
  background-color: unset !important;
}
.is-show-less {
  margin-top: 30px;
  z-index: 1;
}
.confirm-zone .action-item img {
  width: 14px !important;
}
.confirm-zone .checklist-edit {
  margin-right: 10px;
}
.confirm-zone .action-item .icon-hover {
  display: none;
}
.confirm-zone .action-item .icon-primary {
  display: block;
}
.confirm-zone .action-item:hover .icon-hover {
  display: block;
}
.confirm-zone .action-item:hover .icon-primary {
  display: none;
}
</style>
