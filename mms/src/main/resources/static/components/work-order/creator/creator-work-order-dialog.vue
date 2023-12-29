<template>
  <base-dialog
    :title="getModalTitle"
    :visible="isShow"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    @close="handleClose"
  >
    <div class="create-wo-container">
      <div class="position-relative">
        <div v-if="currentStep.value > 1" class="modal__container__header">
          <div class="back-icon" @click="navigate('back')">
            <span class="icon back-arrow"></span>
            <span class="back-icon__text text--blue">
              {{ resources["back_to_previous_page"] }}
            </span>
          </div>
        </div>
        <div class="steps-zone">
          <div class="custom-base-steps">
            <base-steps
              :list="getListSteps"
              :current="currentStep.key"
              @change="handleChangeStep"
            ></base-steps>
          </div>
          <div v-if="isShow" class="steps-content">
            <!--Type-->
            <approval-step
              v-show="isVisibleApprovalModal"
              :resources="resources"
              :is-show="isVisibleApprovalModal"
              :work-order-id="workOrderId"
              :list-mold-ids="getMoldIds"
              :list-machine-ids="getMachineIds"
              :list-terminal-ids="getTerminalIds"
              :list-sensor-ids="getSensorIds"
              :checked-items-list="checkedItemsList"
              :default-failure-time="getDefaultFailureTime"
              :get-selected-mold="getSelectedMold"
              :get-selected-machine="getSelectedMachine"
              :get-selected-terminal="getSelectedTerminal"
              :get-selected-sensor="getSelectedSensor"
              :report-failure-shot="getReportFailureShot"
              :has-plant-location-code="hasPlantLocationCode"
              :item="approvalItem"
              :order-type="orderType.value"
              :third-files="thirdFiles"
              :third-files2="thirdFiles2"
              :part-picture-files="partPictureFiles"
              :part-picture-files2="partPictureFiles2"
              :delete-third-files="deleteThirdFiles"
              :selected-third-files="selectedThirdFiles"
              :selected-third-files2="selectedThirdFiles2"
              :delete-third-files2="deleteThirdFiles2"
            >
            </approval-step>
            <div
              v-show="!isVisibleApprovalModal && currentStep.value === 1"
              class="step-content"
            >
              <template
                v-if="
                  modalType === 'EDIT' &&
                  orderType.value === 'CORRECTIVE_MAINTENANCE' &&
                  selectedItem?.status === 'REQUESTED_NOT_FINISHED'
                "
              >
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["work_order_id"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      :value="workOrderId"
                      :readonly="false"
                      size="small"
                    ></base-input>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["ref_id"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      v-model="refCode"
                      size="small"
                      :placeholder="resources['order_reference_id']"
                    ></base-input>
                  </div>
                </div>

                <div class="form-group row line-row-wrapper">
                  <span
                    class="text--black col-md-2 col-form-label d-flex align-center"
                  >
                    <span>{{ resources["asset_s"] }}</span>
                    <div class="dot--red" style="margin: 0 5px"></div>
                  </span>
                  <div class="col-md-9 align-items-center">
                    <div>
                      <base-chip
                        v-for="(item, index) in getWorkOrderAssets"
                        :key="`${item?.id}_assets_${index}`"
                        :selection="item"
                        :color="item.color"
                        :close-able="false"
                      >
                      </base-chip>
                    </div>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["plant_s"]
                  }}</span>
                  <div class="col-md-9 align-items-center">
                    <div>
                      {{
                        _.join(
                          _.map(selectedItem.plantList, (plant) => plant.name),
                          ", "
                        )
                      }}
                    </div>
                  </div>
                </div>
                <div
                  v-show="'CORRECTIVE_MAINTENANCE' === orderType.value"
                  class="align-center form-group row line-row-wrapper"
                >
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["failure_time"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <div>
                      <base-button level="secondary" disabled>
                        {{ getEditFailureDateTitle.date }}
                      </base-button>
                    </div>
                    <div style="margin-left: 8px">
                      <base-button level="secondary" disabled>
                        {{ getEditFailureDateTitle.time }}
                      </base-button>
                    </div>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">
                    {{ resources["accumulated_shots"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      :value="`${
                        selectedItem.reportFailureShot > 0
                          ? selectedItem.reportFailureShot + ' shots'
                          : '0 shot'
                      }`"
                      :readonly="true"
                      size="small"
                    ></base-input>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">
                    {{ resources["order_type"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      :value="orderType.name"
                      :readonly="true"
                      size="small"
                    ></base-input>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["priority"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-priority-card :color="color[selectedItem.priority]">
                      {{ selectedItem.priority }}
                    </base-priority-card>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper align-top">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["details"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <textarea
                      class="custom-textarea text-area--disable"
                      rows="7"
                      style="resize: none"
                      :readonly="true"
                      disabled
                      placeholder="Enter any notes here"
                      v-model="selectedItem.details"
                    ></textarea>
                  </div>
                </div>
                <div class="align-center form-group row line-row-wrapper">
                  <span
                    class="text--black col-md-2 col-form-label d-flex align-center"
                    ><span>{{ resources["attachment_s"] }}</span></span
                  >
                  <div class="col-md-9">
                    <preview-images-system
                      :is-viewing="true"
                      :images-uploaded="partPictureFiles"
                    ></preview-images-system>
                  </div>
                </div>
                <div class="align-center form-group row line-row-wrapper">
                  <span
                    class="text--black col-md-2 col-form-label d-flex align-center"
                    ><span>{{ resources["number_of_backups"] }}</span>
                    <a-icon type="info-circle" />
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      v-model="cmEditItem.numberOfBackups"
                      size="small"
                      placeholder="Number of Backups"
                    >
                    </base-input>
                  </div>
                </div>
                <div class="align-center form-group row line-row-wrapper">
                  <span class="text--black col-md-2 col-form-label">{{
                    resources["cost_estimate_usd"]
                  }}</span>
                  <div class="col-md-3 d-flex align-items-center">
                    <base-input
                      v-model="cmEditItem.costEstimate"
                      size="small"
                      placeholder="Cost Estimate (USD)"
                    >
                    </base-input>
                  </div>
                  <span class="text--black col-md-1 col-form-label">{{
                    resources["details"]
                  }}</span>
                  <div class="col-md-5 d-flex align-items-center">
                    <base-input
                      v-model="cmEditItem.costEstimateDetails"
                      size="small"
                      style="width: 100%"
                      placeholder="Details"
                    ></base-input>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label">
                    {{ resources["work_order_id"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      style="width: 198px"
                      :readonly="true"
                      :value="workOrderId"
                    ></base-input>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label">{{
                    resources["ref_id"]
                  }}</span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-input
                      v-model="refCode"
                      size="small"
                      :placeholder="resources['order_reference_id']"
                    ></base-input>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label d-flex align-center">
                    <span>{{ resources["asset_s"] }}</span>
                    <div class="dot--red" style="margin: 0 5px"></div>
                  </span>
                  <div class="col-md-9">
                    <template v-if="allowAssetSelection">
                      <base-input
                        v-if="isSelectSingleAssets && getSelectedMold.length"
                        style="width: 198px"
                        :readonly="true"
                        :value="getSingleSelectedTitle"
                      ></base-input>
                      <template v-else>
                        <div>
                          <assets-dropdown
                            :mutil="!isSelectSingleAssets"
                            :title="getAssetTitle"
                            :resources="resources"
                            :clear-key="clearAssetsKey"
                            :asset-types="getAssetTypes"
                            :asset-selected="
                              _.isEmpty(checkedItemsList)
                                ? assetSelected
                                : checkedItemsList
                            "
                            @change="handleChangeAssets"
                          ></assets-dropdown>
                        </div>
                      </template>
                    </template>
                    <template v-else>
                      <base-input
                        style="width: 198px"
                        :readonly="true"
                        :value="getSingleSelectedTitle"
                      ></base-input>
                    </template>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label">
                    {{ resources["plants_s"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <i v-if="!plantsList.length"
                      >*{{
                        resources[
                          "plants_will_auto_populate_based_on_the_location_of_selected_assets"
                        ]
                      }}.</i
                    >
                    <i v-else-if="!hasPlantLocationCode"
                      >{{ resources["associated_plant_unavailable"] }}.</i
                    >
                    <div v-else>
                      <span>{{
                        _.join(
                          checkedItemsList
                            .map((item) => item.locationName)
                            .filter((item) => !!item),
                          ", "
                        )
                      }}</span>
                    </div>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label">
                    {{ resources["order_type"] }}
                  </span>
                  <div
                    v-if="selectedItem?.status === 'REQUESTED_NOT_FINISHED'"
                    class="col-md-9 d-flex align-items-center"
                  >
                    <base-button disabled>{{ orderType.name }}</base-button>
                  </div>
                  <div class="col-md-9" v-else>
                    <custom-dropdown
                      :list-item="getOrderTypeList"
                      :default-item="orderTypeDefaultSelected"
                      :title-field="'name'"
                      @change="onChangeOrderType"
                    ></custom-dropdown>
                    <div
                      v-if="
                        [
                          'CORRECTIVE_MAINTENANCE',
                          'GENERAL',
                          'REFURBISHMENT',
                          'DISPOSAL',
                        ].includes(orderType.value) &&
                        selectedItem?.status !== 'REQUESTED_NOT_FINISHED'
                      "
                      class="mt-3"
                    >
                      <div class="mb-1 d-flex align-items-center">
                        <input
                          :key="orderType.value"
                          v-model="isRequestApproval"
                          id="isRequestApproval"
                          type="checkbox"
                          class="mr-2"
                        />
                        <label for="isRequestApproval" style="margin-bottom: 0">
                          {{ resources["request_approval_from"] }}
                          {{ clientName }}
                        </label>
                      </div>
                      <div>
                        <i
                          >*{{
                            resources[
                              "if_unselected__approval_will_not_be_required"
                            ]
                          }}.</i
                        >
                      </div>
                    </div>
                  </div>
                </div>
                <div
                  v-show="'CORRECTIVE_MAINTENANCE' === orderType.value"
                  class="form-group row line-row-wrapper"
                >
                  <span class="col-md-2 col-form-label">
                    {{ resources["failure_time"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <base-button
                      level="secondary"
                      :icon="modalType !== 'EDIT' ? 'calendar' : undefined"
                      :disabled="modalType === 'EDIT'"
                      @click="handleShowFailureTimeDatePicker"
                    >
                      {{ getFailureTimeTitle }}
                    </base-button>
                    <common-hour-picker
                      style="margin-left: 4px"
                      :disabled="modalType === 'EDIT'"
                      size="medium"
                      :selected-time="selectedFailureTime"
                      @change-time="handleChangeFailureTime"
                      @click="handleScrollContentToBottom"
                    ></common-hour-picker>
                  </div>
                </div>
                <div class="form-group row line-row-wrapper">
                  <span class="col-md-2 col-form-label">
                    {{ resources["priority"] }}
                  </span>
                  <div class="col-md-9 d-flex align-items-center">
                    <custom-dropdown
                      :list-item="priorityList"
                      :title-field="'name'"
                      :default-item="priorityDefaultSelected"
                      @change="onChangePriorityType"
                    ></custom-dropdown>
                  </div>
                </div>
              </template>
            </div>
            <!--Assign-->
            <div
              v-show="!isVisibleApprovalModal && currentStep.value === 2"
              class="step-content"
            >
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label d-flex align-center">
                  <span>{{ resources["request_to"] }}</span>
                  <div class="dot--red" style="margin: 0 5px"></div>
                </span>
                <div class="col-md-9">
                  <div>
                    <custom-dropdown-button
                      title="Add User(s)"
                      :is-selected="true"
                      :is-show="selecteUserDropdown"
                      @click="openUserDropdown"
                    ></custom-dropdown-button>
                    <common-popover
                      @close="closeUserDropdown"
                      :is-visible="selecteUserDropdown"
                      :style="{
                        width: '210px',
                        marginTop: '4px',
                      }"
                    >
                      <common-select
                        @close="closeUserDropdown"
                        :style="{ position: 'static', width: '100%' }"
                        :items="userIds"
                        :searchbox="userIds.length >= 5"
                        :multiple="true"
                        :has-toggled-all="true"
                        @on-change="onChangeUser"
                        :placeholder="'Search user'"
                      >
                      </common-select>
                    </common-popover>
                    <div class="mt-2">
                      <base-avatar
                        v-for="(item, index) in getSelectedUser"
                        :key="`${item?.id}_user_${index}`"
                        :item="item"
                        class="mr-2"
                      >
                      </base-avatar>
                    </div>
                  </div>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label d-flex align-center">
                  <span>{{ resources["detail_s"] }}</span>
                  <div class="dot--red" style="margin: 0 5px"></div>
                </span>
                <div class="col-md-9">
                  <div>
                    <textarea
                      v-model="details"
                      cols="100"
                      rows="7"
                      class="custom-text-area"
                      style="width: 100%; resize: none"
                      placeholder="Enter Detail(s)"
                    ></textarea>
                  </div>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["attachments"] }}
                </span>
                <div class="col-md-9">
                  <div class="mb-2">
                    <div class="op-upload-button-wrap">
                      <base-button level="secondary">
                        {{ resources["upload_file_image"] }}
                        <img
                          class="ml-1"
                          src="/images/icon/upload-blue.svg"
                          alt=""
                        />
                      </base-button>
                      <!-- <button
                            id="partPictureFile"
                            class="base-button button-secondary button-normal button-medium"
                        >

                        </button> -->
                      <input
                        type="file"
                        ref="fileupload"
                        id="files1"
                        @change="selectedThirdFiles"
                        style="height: 40px; width: 100%"
                        accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
                      />
                    </div>
                    <preview-images-system
                      :is-viewing="false"
                      @delete-img="deleteThirdFiles"
                      @delete-uploaded-img="deleteUploadedFiles"
                      :images="thirdFiles"
                      :images-uploaded="partPictureFiles"
                    ></preview-images-system>
                  </div>
                  <a-popover
                    v-model="visibleChecklist"
                    placement="bottomLeft"
                    trigger="click"
                  >
                    <custom-dropdown-button
                      :title="!checklistId ? `Add Checklist` : `Edit Checklist`"
                      :is-selected="true"
                      :is-show="visibleChecklist"
                      @click="openChecklistDropdown"
                    ></custom-dropdown-button>
                    <a-menu
                      v-if="!isChecklistView"
                      slot="content"
                      class="wrapper-action-dropdown"
                      style="
                        border-right: unset !important;
                        max-height: 250px;
                        overflow: auto;
                      "
                    >
                      <a-menu-item
                        v-if="
                          ['GENERAL', 'INSPECTION', 'EMERGENCY'].includes(
                            orderType.value
                          ) && checkListCountByType['GENERAL'] > 0
                        "
                        @click="
                          isChecklistView = 'general';
                          isVisibleMaintenanceDropdown = true;
                        "
                      >
                        <span>{{ resources["general"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        v-if="
                          [
                            'INSPECTION',
                            'EMERGENCY',
                            'CORRECTIVE_MAINTENANCE',
                            'PREVENTATIVE_MAINTENANCE',
                          ].includes(orderType.value) &&
                          checkListCountByType['MAINTENANCE'] > 0
                        "
                        @click="
                          isChecklistView = 'maintenance';
                          isVisibleMaintenanceDropdown = true;
                        "
                      >
                        <span>{{ resources["maintenance"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        v-if="
                          'INSPECTION' === orderType.value &&
                          checkListCountByType['QUALITY_ASSURANCE'] > 0
                        "
                        @click="
                          isChecklistView = 'quality_assurance';
                          isVisibleQualityAssuranceDropdown = true;
                        "
                      >
                        <span>{{ resources["quality_assurance"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        v-if="false"
                        @click="
                          isChecklistView = 'reject-rate';
                          isVisibleRejectRateDropdown = true;
                        "
                      >
                        <span>{{ resources["reject_rate"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        v-if="
                          'REFURBISHMENT' === orderType.value &&
                          checkListCountByType['REFURBISHMENT'] > 0
                        "
                        @click="isChecklistView = 'refurbishment'"
                      >
                        <span>{{ resources["refurbishment"] }}</span>
                      </a-menu-item>
                      <a-menu-item
                        v-if="
                          'DISPOSAL' === orderType.value &&
                          checkListCountByType['DISPOSAL'] > 0
                        "
                        @click="
                          isChecklistView = 'disposal';
                          isVisibleDisposalDropdown = true;
                        "
                      >
                        <span>{{ resources["disposal"] }}</span>
                      </a-menu-item>
                    </a-menu>
                    <div
                      v-else-if="isChecklistView === 'general'"
                      slot="content"
                    >
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleGeneralDropdown }"
                        :items="checklistGeneralList"
                        :searchbox="checklistGeneralList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeGeneralDropdown"
                      ></common-select>
                    </div>
                    <div
                      v-else-if="isChecklistView === 'maintenance'"
                      slot="content"
                    >
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleMaintenanceDropdown }"
                        :items="checklistMaintenanceList"
                        :searchbox="checklistMaintenanceList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeMaintenanceDropdown"
                      ></common-select>
                    </div>
                    <div
                      v-else-if="isChecklistView === 'disposal'"
                      slot="content"
                    >
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleDisposalDropdown }"
                        :items="checklistDisposalList"
                        :searchbox="checklistDisposalList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeDisposalDropdown"
                      ></common-select>
                    </div>
                    <div
                      v-else-if="isChecklistView === 'refurbishment'"
                      slot="content"
                    >
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleRefurbishmentDropdown }"
                        :items="checklistRefurbishmentList"
                        :searchbox="checklistRefurbishmentList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeRefurbishmentDropdown"
                      ></common-select>
                    </div>
                    <div
                      v-else-if="isChecklistView === 'quality_assurance'"
                      slot="content"
                    >
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleQualityAssuranceDropdown }"
                        :items="checklistQualityAssuranceList"
                        :searchbox="checklistQualityAssuranceList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeQualityAssuranceDropdown"
                      ></common-select>
                    </div>
                    <div v-else slot="content">
                      <common-select
                        :style="{
                          position: 'static',
                          width: '100%',
                          'min-width': '220px',
                        }"
                        :class="{ show: isVisibleRejectRateDropdown }"
                        :items="checklistRejectRateList"
                        :searchbox="checklistRejectRateList.length >= 5"
                        :has-toggled-all="true"
                        @on-select="onChangeChecklist"
                        :placeholder="'Search list name'"
                        @close="closeRejectRateDropdown"
                      ></common-select>
                    </div>
                  </a-popover>
                  <base-chip
                    v-if="checklistId"
                    :color="'red'"
                    :selection="checklistId"
                    title-field="checklistCode"
                    class="mr-3"
                    @close="onRemoveChecklist"
                  >
                  </base-chip>
                </div>
              </div>
              <div
                class="form-group row line-row-wrapper"
                v-show="!hasAssetTerminalSensor"
              >
                <span class="col-md-2 col-form-label">
                  {{ resources["request_s"] }}
                </span>
                <div class="col-md-9">
                  <div class="mb-3">
                    <div class="mb-1 d-flex align-items-center">
                      <input v-model="cost" type="checkbox" class="mr-2" />
                      <span>{{ resources["cost_details_required"] }}</span>
                    </div>
                    <div>
                      <i>*{{ resources["cost_details_required_message"] }}</i>
                    </div>
                  </div>
                  <div class="mb-3">
                    <div class="mb-1 d-flex align-items-center">
                      <input
                        v-model="pickingList"
                        type="checkbox"
                        class="mr-2"
                      />
                      <span>{{ resources["picking_list"] }}</span>
                    </div>
                    <div>
                      <i
                        >*{{
                          resources[
                            "when_selected__the_requested_user_will_be_required_to_select_or_create_a_picking_list_for_the_workorder"
                          ]
                        }}</i
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!--Schedule-->
            <div
              v-show="!isVisibleApprovalModal && currentStep.value === 3"
              class="step-content"
            >
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label d-flex align-center">
                  <span>{{ resources["start_date"] }}</span>
                  <div class="dot--red" style="margin: 0 5px"></div>
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-button
                    level="secondary"
                    icon="calendar"
                    @click="handleShowStartDatePicker"
                  >
                    {{ getStartOnTitle }}
                  </base-button>
                  <common-hour-picker
                    style="margin-left: 4px"
                    size="medium"
                    :selected-time="selectedHourStart"
                    @change-time="handleChangeHourStart"
                  ></common-hour-picker>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label d-flex align-center">
                  <span>{{ resources["due_date"] }}</span>
                  <div class="dot--red" style="margin: 0 5px"></div>
                </span>
                <div
                  class="col-md-9 d-flex align-items-center"
                  style="justify-content: flex-start"
                >
                  <base-button
                    level="secondary"
                    @click="handleShowEndDatePicker"
                    icon="calendar"
                  >
                    {{ getEndOnTitle }}
                  </base-button>
                  <common-hour-picker
                    style="margin-left: 4px"
                    size="medium"
                    :selected-time="selectedHourEnd"
                    @change-time="handleChangeHourEnd"
                  ></common-hour-picker>
                </div>
              </div>
              <div
                v-if="
                  ['PREVENTATIVE_MAINTENANCE'].includes(orderType.value) &&
                  modalType !== 'EDIT' &&
                  getSelectedMold.length &&
                  getSelectedMold[0]?.pmStrategy === 'SHOT_BASED'
                "
                class="form-group row line-row-wrapper"
              >
                <div class="col-md-2"></div>
                <div class="col-md-9 d-flex flex-column">
                  <i>*{{ resources["this_tooling_pm_is_shot_based"] }}.</i>
                  <i
                    >{{
                      resources[
                        "due_date_may_be_modified_dynamically_based_on_the_pm_checkpoint_prediction"
                      ]
                    }}.</i
                  >
                </div>
              </div>
              <div class="form-group row line-row-wrapper" style="opacity: 0">
                <span class="col-md-2 col-form-label"> </span>
                <div class="col-md-9">
                  <div class="mt-3">
                    <div class="mb-1 d-flex align-items-center">
                      <input
                        v-model="isDueDateRepeats"
                        type="checkbox"
                        class="mr-2"
                      />
                      <span>{{ resources["due_date_repeats"] }}</span>
                    </div>
                    <div>
                      <i
                        >*{{
                          resources[
                            "when_selected__the_work_order_will_be_scheduled_to_repeat"
                          ]
                        }}.</i
                      >
                    </div>
                  </div>
                </div>
              </div>
              <div style="width: 100%; height: 204px"></div>
            </div>
            <!--Review-->
            <div
              v-show="!isVisibleApprovalModal && currentStep.value === 4"
              class="step-content"
            >
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["work_order_id"] }}
                </span>
                <div class="col-md-4">
                  <base-button disabled>{{ workOrderId }}</base-button>
                </div>
                <div class="col-md-5">
                  <div class="row">
                    <div class="col-md-4">{{ resources["assigned_to"] }}</div>
                    <div class="col-md-8">
                      <base-avatar
                        v-for="(item, index) in getSelectedUser"
                        :key="`${item?.id}_user_${index}`"
                        :item="item"
                        class="mr-2"
                      >
                      </base-avatar>
                    </div>
                  </div>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["ref_id"] }}</span
                >
                <div class="col-md-9 d-flex align-items-center">
                  <base-input
                    :value="refCode"
                    :readonly="true"
                    size="small"
                  ></base-input>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["start_by"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-button disabled>{{ getStartOnTitle }}</base-button>
                  <base-button disabled class="ml-2">{{
                    getSelectedHourStart
                  }}</base-button>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["due_by"] }}</span
                >
                <div class="col-md-9 d-flex align-items-center">
                  <base-button disabled>{{ getEndOnTitle }}</base-button>
                  <base-button disabled class="ml-2">{{
                    getSelectedHourEnd
                  }}</base-button>
                </div>
              </div>
              <div
                v-if="orderType.value === 'CORRECTIVE_MAINTENANCE'"
                class="form-group row line-row-wrapper"
              >
                <span class="col-md-2 col-form-label">
                  {{ resources["failure_time"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-button level="secondary" :disabled="true">
                    {{ getFailureTimeTitle }}
                  </base-button>
                  <base-button disabled class="ml-2"
                    >{{ getFailureHour }}
                  </base-button>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["asset_s"] }}
                </span>
                <div class="col-md-9 align-items-center">
                  <base-chip
                    v-for="(item, index) in getSelectedMold"
                    :close-able="false"
                    :key="`${item?.id}_mold_${index}`"
                    :selection="item"
                  >
                  </base-chip>
                  <base-chip
                    v-for="(item, index) in getSelectedMachine"
                    :close-able="false"
                    :key="`${item?.id}_machine_${index}`"
                    :color="'red'"
                    :selection="item"
                  >
                  </base-chip>
                  <base-chip
                    v-for="item in getSelectedTerminal"
                    :key="item.id"
                    :color="'green'"
                    :selection="item"
                    :close-able="false"
                  >
                  </base-chip>
                  <base-chip
                    v-for="item in getSelectedSensor"
                    :key="item.id"
                    :color="'orange'"
                    :selection="item"
                    :close-able="false"
                  >
                  </base-chip>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["plant_s"] }}</span
                >
                <div class="col-md-9 align-items-center">
                  <i v-if="!hasPlantLocationCode"
                    >{{ resources["associated_plant_unavailable"] }}.</i
                  >
                  <div v-else>
                    <span>{{
                      _.join(
                        checkedItemsList
                          .map((item) => item.locationName)
                          .filter((item) => !!item),
                        ", "
                      )
                    }}</span>
                  </div>
                  <!--                                    <span v-for="(plant, index) in plantsList" :key="index" style="white-space: pre-wrap;">{{ plant.locationCode }}{{ index + 1 === plantsList.length ? "" : ", " }}</span>-->
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["order_type"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-button disabled>{{ orderType.name }}</base-button>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["priority"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-priority-card :color="color[priority.value]">
                    <span>{{ priority.name }}</span>
                  </base-priority-card>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["detail_s"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <textarea
                    readonly
                    cols="100"
                    rows="7"
                    class="custom-text-area"
                    :value="details"
                    style="resize: none"
                  ></textarea>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["checklist"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <base-chip
                    v-if="checklistId"
                    :selection="checklistId"
                    title-field="checklistCode"
                  ></base-chip>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["cost"] }}</span
                >
                <div class="col-md-9 d-flex align-items-center">
                  {{ cost ? "Required" : "Not Required" }}
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["picking_list"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  {{ pickingList ? "Required" : "Not Required" }}
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["attachment_s"] }}
                </span>
                <div class="col-md-9 d-flex align-items-center">
                  <div v-if="!thirdFiles.length && !partPictureFiles.length">
                    {{ resources["none"] }}
                  </div>
                  <preview-images-system
                    v-else
                    :images="thirdFiles"
                    :images-uploaded="partPictureFiles"
                  ></preview-images-system>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div
        class="d-flex modal__footer"
        style="margin-top: 80px; justify-content: flex-end"
      >
        <base-button
          v-show="isVisibleApprovalModal"
          :size="'medium'"
          :type="'normal'"
          :level="'primary'"
          :disabled="!canRequestApproval"
          class="modal__footer__button"
          @click="handleRequestApproval"
        >
          {{ resources["request_approval"] }}
        </base-button>
        <base-button
          v-show="!isVisibleApprovalModal"
          :size="'medium'"
          :type="'normal'"
          :level="'primary'"
          :disabled="!canNavigate"
          class="modal__footer__button"
          @click="navigate('next')"
        >
          {{ isRequestApproval ? "Continue" : currentStep.btnTitle }}
        </base-button>
      </div>
    </div>
    <date-picker-modal
      ref="datePickerModalRef"
      :select-option="timeOptionsStart"
      :resources="resources"
      :date-range="timeRange"
      @change-date="handleChangeDate"
    >
    </date-picker-modal>
    <date-picker-modal
      ref="datePickerModalRefEnd"
      :select-option="timeOptionsEnd"
      :resources="resources"
      :date-range="getEndTimeRange"
      @change-date="handleChangeDateEnd"
    >
    </date-picker-modal>
    <date-picker-modal
      ref="datePickerModalRefFailureTime"
      :select-option="failureOptions"
      :resources="resources"
      :date-range="failureTimeRange"
      @change-date="handleChangeFailureTimeDate"
    >
    </date-picker-modal>
  </base-dialog>
</template>

<script>
const ORDER_TYPE_LIST = [
  {
    name: "General",
    value: "GENERAL",
  },
  {
    name: "Inspection",
    value: "INSPECTION",
  },
  {
    name: "Emergency",
    value: "EMERGENCY",
  },
  {
    name: "Preventive Maintenance",
    value: "PREVENTATIVE_MAINTENANCE",
  },
  {
    name: "Corrective Maintenance",
    value: "CORRECTIVE_MAINTENANCE",
  },
  {
    name: "Disposal",
    value: "DISPOSAL",
  },
  {
    name: "Refurbishment",
    value: "REFURBISHMENT",
  },
];

const defaultOptions = [{ title: "Daily", type: "DAILY", isRange: false }];
const ASSET_SIZE = 10;
module.exports = {
  props: {
    isShow: {
      type: Boolean,
      default: () => false,
    },
    resources: {
      type: Object,
      default: () => ({}),
    },
    assetSelected: {
      type: Array,
    },
    enableAssetSelection: {
      type: Boolean,
      default: () => true,
    },
    isToolingAssetSelected: {
      type: Boolean,
      default: () => false,
    },
    isTerminalAssetSelected: {
      type: Boolean,
      default: () => false,
    },
    isSensorAssetSelected: {
      type: Boolean,
      default: () => false,
    },
    priorityDefault: {
      type: Object,
      default: () => ({}),
    },
    orderTypeDefault: {
      type: Object,
      default: () => ({}),
    },
    selectedItem: {
      type: Object,
      default: () => ({}),
    },
    modalType: {
      type: String,
      default: () => "CREATE",
    },
    workOrderType: Object,
    notification: Object,
  },
  components: {
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
    "date-picker-modal": httpVueLoader(
      "/components/@base/date-picker/date-picker-modal.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "approval-step": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog/approval-step.vue"
    ),
    "assets-dropdown": httpVueLoader(
      "/components/@base/dropdown/assets-dropdown.vue"
    ),
  },
  data() {
    return {
      isInitialized: false,
      isRequestApproval: false,
      workOrderId: null,
      isVisibleMoldDropdown: false,
      isVisibleMachineDropdown: false,
      isVisibleTerminalDropdown: false,
      isVisibleSensorDropdown: false,
      isVisibleGeneralDropdown: false,
      isVisibleMaintenanceDropdown: false,
      isVisibleRejectRateDropdown: false,
      isVisibleQualityAssuranceDropdown: false,
      isVisibleDisposalDropdown: false,
      isVisibleRefurbishmentDropdown: false,
      isVisibleApprovalModal: false,
      refCode: "",
      orderType: {
        name: "General",
        value: "GENERAL",
      },
      orderTypeList: [...ORDER_TYPE_LIST],
      attachmentList: [
        {
          name: "Maintenance",
          value: "MAINTENANCE",
        },
        {
          name: "Reject Rate",
          value: "REJECT RATE",
        },
        {
          name: "Spare Parts",
          value: "SPARE PARTS",
        },
        {
          name: "Preventive Maintenance",
          value: "PREVENTATIVE_MAINTENANCE",
        },
        {
          name: "Corrective Maintenance",
          value: "CORRECTIVE_MAINTENANCE",
        },
      ],
      attachment: "",
      priority: {
        name: "Medium",
        value: "MEDIUM",
      },
      priorityList: [
        {
          name: "Low",
          value: "LOW",
        },
        {
          name: "Medium",
          value: "MEDIUM",
        },
        {
          name: "High",
          value: "HIGH",
        },
      ],
      machineIds: [],
      terminalIds: [],
      sensorIds: [],
      userIds: [],
      checkedItemsList: [],
      details: "",
      checklistId: null,
      cost: false,
      pickingList: false,
      moldIds: [],
      currentStep: {
        key: "TYPE",
        label: "Type",
        modalTitle: "select_work_order_type",
        btnTitle: "Proceed to Assign",
        value: 1,
        checked: false,
      },
      selectedCondition: "Location",
      listSteps: [
        {
          key: "TYPE",
          label: "type",
          modalTitle: "select_work_order_type",
          btnTitle: "Proceed to Assign",
          value: 1,
          checked: false,
        },
        {
          key: "ASSIGN",
          label: "assign",
          modalTitle: "assign_items_to_work_order",
          btnTitle: "Proceed to Schedule",
          value: 2,
          checked: false,
        },
        {
          key: "SCHEDULE",
          label: "schedule",
          modalTitle: "schedule_work_order",
          btnTitle: "Proceed to Review",
          value: 3,
          checked: false,
        },
        {
          key: "REVIEW",
          label: "review",
          modalTitle: "review_work_order",
          btnTitle: "Create Work Order",
          value: 4,
          checked: false,
        },
      ],
      listApprovalStep: [
        {
          key: "TYPE",
          label: "type",
          modalTitle: "select_work_order_type",
          btnTitle: "Proceed to Assign",
          value: 1,
          checked: false,
        },
        {
          key: "APPROVAL",
          label: "request_approval",
          modalTitle: "request_approval",
          btnTitle: "Request Approval",
          value: 2,
          checked: false,
        },
      ],
      selecteUserDropdown: false,
      isDueDateRepeats: false,
      failureTime: "",
      selectedHourStart: {
        hour: "0",
        minute: "0",
        isAddDay: false,
      },
      selectedFailureTime: {
        hour: "0",
        minute: "0",
        isAddDay: false,
      },
      selectedHourEnd: {
        hour: "0",
        minute: "0",
        isAddDay: false,
      },
      start: 1631063503,
      end: 1659921103,
      frequent: {
        name: "Weekly",
        value: "weekly",
      },
      repeatOn: {
        name: "Exact date",
        value: null,
      },
      endAfter: {
        name: "Never",
        value: null,
      },
      frequentList: [
        {
          name: "Weekly",
          value: "WEEKLY",
        },
        {
          name: "Monthly",
          value: "MONTHLY",
        },
        {
          name: "Quarterly",
          value: "QUARTERLY",
        },
      ],
      repeatOnList: [
        {
          name: "Exact date",
          value: null,
        },
        {
          name: "Monday",
          value: "monday",
        },
        {
          name: "Tuesday",
          value: "tuesday",
        },
        {
          name: "Wednesday",
          value: "webnesday",
        },
        {
          name: "Thursday",
          value: "thurday",
        },
        {
          name: "Friday",
          value: "friday",
        },
        {
          name: "Saturday",
          value: "saturday",
        },
        {
          name: "Sunday",
          value: "sunday",
        },
      ],
      endAfterList: [
        {
          name: "Never",
          value: null,
        },
        {
          name: "1 time",
          value: "1",
        },
        {
          name: "2 times",
          value: "2",
        },
        {
          name: "3 times",
          value: "3",
        },
        {
          name: "4 times",
          value: "4",
        },
        {
          name: "5 times",
          value: "5",
        },
        {
          name: "6 times",
          value: "6",
        },
        {
          name: "7 times",
          value: "7",
        },
        {
          name: "8 times",
          value: "8",
        },
        {
          name: "9 times",
          value: "9",
        },
        {
          name: "10 times",
          value: "10",
        },
      ],
      color: {
        HIGH: "red",
        MEDIUM: "yellow",
        LOW: "green",
        UPCOMING: "yellow",
        REQUESTED: "purple",
        PENDING: "blue",
      },
      isAssetView: false,
      isChecklistView: false,
      visibleAsset: false,
      visibleChecklist: false,
      rawUserIds: [],
      rawMoldIds: [],
      rawMachineIds: [],
      rawTerminalIds: [],
      rawSensorIds: [],
      checklistMaintenanceList: [],
      checklistRejectRateList: [],
      checklistDisposalList: [],
      checklistRefurbishmentList: [],
      checklistQualityAssuranceList: [],
      thirdFiles: [],
      partPictureFiles: [],
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      currentDateEnd: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      failureDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      frequency: "DAILY",
      timeOptionsEnd: null,
      timeOptionsStart: null,
      failureOptions: null,
      successToast: false,
      toastAlert: {
        title: `Success!`,
        content: `Your work order has been created.`,
      },
      clientName: "",
      priorityDefaultSelected: null,
      orderTypeDefaultSelected: null,
      cmEditItem: {
        costEstimate: "",
        costDetails: "",
        numberOfBackups: "",
      },
      moldPage: 1,
      moldTotalPage: 1,
      machinePage: 1,
      machineTotalPage: 1,
      terminalPage: 1,
      terminalTotalPage: 1,
      sensorPage: 1,
      sensorTotalPage: 1,
      searchContent: "",
      isLoading: false,
      checkListCountByType: {
        DISPOSAL: 0,
        GENERAL: 0,
        MAINTENANCE: 0,
        QUALITY_ASSURANCE: 0,
        REFURBISHMENT: 0,
        REJECT_RATE: 0,
      },
      assetTypes: ["TOOLING", "MACHINE", "TERMINAL", "SENSOR"],
      clearAssetsKey: 0,
      thirdFiles: [],
      thirdFiles2: [],
      partPictureFiles: [],
      partPictureFiles2: [],
      // accumulatedShot: 0,
      approvalItem: {
        orderType: "",
        details: "",
        costEstimate: "",
        numberOfBackups: "",
        costDetails: "",
        failureTime: "",
        shotAtRequest: "",
      },
      loading: false,
      loadingRequestApproval: false,
    };
  },
  computed: {
    canRequestApproval() {
      return !this.loadingRequestApproval && this.approvalItem.details !== "";
    },
    isOverview() {
      return this.currentStep.key === "REVIEW";
    },
    allowAssetSelection() {
      if (this.enableAssetSelection) return true;
      return ["GENERAL", "INSPECTION", "EMERGENCY"].includes(
        this.orderType.value
      );
    },
    getAssetTypes() {
      const assetTypes = ["TOOLING"];
      if (
        ![
          "CORRECTIVE_MAINTENANCE",
          "PREVENTATIVE_MAINTENANCE",
          "DISPOSAL",
          "REFURBISHMENT",
        ].includes(this.orderType.value)
      ) {
        assetTypes.push("MACHINE");
      }
      if (this.showAssetTerminalSensor) {
        assetTypes.push("TERMINAL");
        assetTypes.push("SENSOR");
      }
      return assetTypes;
    },
    getEndTimeRange() {
      return {
        minDate: this.currentDate.from,
        maxDate: new Date("2100-01-01"),
      };
    },
    canNavigate() {
      if (this.currentStep.key === "TYPE") {
        return (
          ((this.checkedItemsList || []).length > 0 &&
            this.checkedItemsList[0]?.id) ||
          ((this.assetSelected || []).length > 0 && this.assetSelected[0]?.id)
        );
      }
      if (this.currentStep.key === "ASSIGN") {
        return this.details && this.getSelectedUser.length;
      }
      if (this.currentStep.key === "SCHEDULE") {
        const start = this.currentDate.fromTitle;
        const end = this.currentDateEnd.toTitle;
        const startDateDetail = moment(
          `${start}-${this.selectedHourStart.hour}-${this.selectedHourStart.minute}`,
          "YYYY-MM-DD-hh-mm"
        );
        const startDateEndDetail = moment(
          `${end}-${this.selectedHourEnd.hour}-${this.selectedHourEnd.minute}`,
          "YYYY-MM-DD-hh-mm"
        );
        const difference = startDateEndDetail.diff(startDateDetail, "minutes");
        console.log(
          "difference",
          difference,
          start,
          end,
          this.selectedHourStart,
          this.selectedHourEnd
        );
        return difference > 0;
      }
      return !this.loading;
    },
    getTotalImage() {
      const total = [...this.thirdFiles, ...this.partPictureFiles];
      console.log(
        "getTotalImage",
        this.thirdFiles,
        this.partPictureFiles,
        total
      );
      return total;
    },
    getEditFailureDateTitle() {
      const failureTime = this.failureTime;
      if (failureTime) {
        const convertedDate = Common.convertTimestampToDate(failureTime);
        console.log("getEditFailureDateTitle", convertedDate, failureTime);
        return {
          date: moment(convertedDate).format("YYYY-MM-DD"),
          time: moment(convertedDate).format("HH:mm"),
        };
      }
      return "";
    },
    getWorkOrderAssets() {
      console.log(
        "this.selectedItem",
        this.selectedItem,
        this.checkedItemsList
      );
      if (this.selectedItem.workOrderAssets.length == 0) {
        return [];
      }
      return _.map(this.selectedItem.workOrderAssets, (o) => {
        return {
          id: o.id,
          title: o.assetCode,
          color: o.type == "MACHINE" ? "red" : "blue",
        };
      });
    },
    getCostEstimate() {
      if (
        this.selectedItem &&
        this.selectedItem.workOrderCostList &&
        this.selectedItem.workOrderCostList[0]
      ) {
        return {
          cost: this.selectedItem.workOrderCostList[0].cost,
          details: this.selectedItem.workOrderCostList[0].details,
        };
      }
      return {
        cost: "",
        details: "",
      };
    },
    getDefaultFailureTime() {
      return {
        date: this.failureDate,
        time: this.selectedFailureTime,
      };
    },
    hasPlantLocationCode() {
      if (this.plantsList.length <= 0) return false;
      else {
        let isPlantAvailable = false;
        this.plantsList.some((plant) => {
          if (!_.isEmpty(plant.locationName)) {
            isPlantAvailable = true;
            return false;
          }
          return true;
        });
        this.checkedItemsList.some((item) => {
          if (!_.isEmpty(item.locationName)) {
            isPlantAvailable = true;
            return false;
          }
          return true;
        });
        return isPlantAvailable;
      }
    },
    showAssetTerminalSensor() {
      return ["GENERAL", "INSPECTION", "EMERGENCY"].includes(
        this.orderType.value
      );
    },
    hasAssetTerminalSensor() {
      return (
        this.getSelectedTerminal.length > 0 || this.getSelectedSensor.length > 0
      );
    },
    hasAssetMachine() {
      return this.getSelectedMachine.length > 0;
    },
    getAssetTitle() {
      const isSelected =
        this.getSelectedMold.length > 0 ||
        this.getSelectedMachine.length > 0 ||
        this.getSelectedTerminal.length > 0 ||
        this.getSelectedSensor.length > 0;
      if (isSelected) {
        return "Add another asset";
      } else {
        return "Add an asset";
      }
    },
    getApprovalPayload() {
      const payload = {
        workOrderId: this.workOrderId,
        orderType: this.orderType.value,
        priority: this.priority.value,
        pickingList: this.pickingList,
        start: Common.convertDateToTimestamp(this.currentDate.to),
        end: Common.convertDateToTimestamp(this.currentDateEnd.to),
      };
      return payload;
    },
    getToastAlert() {
      let result = { ...this.toastAlert };
      return result;
    },
    getModalTitle() {
      return this.resources[this.currentStep.modalTitle];
    },
    getSelectedUser() {
      return _.filter(this.userIds, (o) => o.checked);
    },
    getSelectedMold() {
      let temp = _.filter(this.moldIds, (o) => o.checked);
      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((i) => i.id).includes(item.id) &&
          item.assetView == "tooling"
        ) {
          temp.push(item);
        }
      });
      console.log("getSelectedMold", temp);
      return temp;
    },
    getSelectedMachine() {
      let temp = _.filter(this.machineIds, (o) => o.checked);
      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "machine"
        ) {
          temp.push(item);
        }
      });
      console.log("getSelectedMachine", temp);
      return temp;
    },
    getSelectedTerminal() {
      let temp = _.filter(this.terminalIds, (o) => o.checked);
      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "terminal"
        ) {
          temp.push(item);
        }
      });
      console.log("getSelectedTerminal", temp);
      return temp;
    },
    getSelectedSensor() {
      let temp = _.filter(this.sensorIds, (o) => o.checked);
      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "counter"
        ) {
          temp.push(item);
        }
      });
      console.log("getSelectedSensor", temp);
      return temp;
    },
    plantsList() {
      return [
        ...this.getSelectedMachine,
        ...this.getSelectedMold,
        ...this.getSelectedTerminal,
        ...this.getSelectedSensor,
      ];
    },
    getStartOnTitle() {
      const displayDate = this.currentDate.to;
      return moment(displayDate).format("YYYY-MM-DD");
    },
    getEndOnTitle() {
      const displayDate = this.currentDateEnd.to;
      return moment(displayDate).format("YYYY-MM-DD");
    },
    getFailureTimeTitle() {
      const displayDate = this.failureDate.to;
      return moment(displayDate).format("YYYY-MM-DD");
    },
    getSelectedHourStart() {
      const date = this.currentDate.to;
      date.setHours(this.selectedHourStart.hour, this.selectedHourStart.minute);
      return moment(date).format("HH:mm");
    },
    getSelectedHourEnd() {
      const date = this.currentDateEnd.to;
      date.setHours(this.selectedHourEnd.hour, this.selectedHourEnd.minute);
      return moment(date).format("HH:mm");
    },
    getFailureHour() {
      const date = this.failureDate.to;
      date.setHours(
        this.selectedFailureTime.hour,
        this.selectedFailureTime.minute
      );
      return moment(date).format("HH:mm");
    },
    isSelectSingleAssets() {
      return [
        "REFURBISHMENT",
        "DISPOSAL",
        "PREVENTATIVE_MAINTENANCE",
        "CORRECTIVE_MAINTENANCE",
      ].includes(this.orderType.value);
    },
    getSingleSelectedTitle() {
      console.log(
        "getSingleSelectedTitle",
        this.checkedItemsList,
        this.assetSelected
      );
      if (!this.allowAssetSelection) {
        return this.assetSelected[0].title;
      }
      if (this.checkedItemsList.length > 0) {
        return this.checkedItemsList[0].title;
      }
      return "";
    },
    getOrderTypeList() {
      const validMold = this.getSelectedMold.length <= 1;
      if (this.getSelectedMachine.length >= 1) {
        const result = this.orderTypeList.filter((item) =>
          ["GENERAL", "INSPECTION", "EMERGENCY"].includes(item.value)
        );
        console.log("getOrderTypeList", this.orderTypeList, result);
        return result;
      } else if (validMold) {
        console.log("getOrderTypeList", this.orderTypeList);
        return this.orderTypeList;
      } else {
        const result = this.orderTypeList.filter(
          (item) =>
            item.value !== "REFURBISHMENT" &&
            item.value !== "DISPOSAL" &&
            item.value !== "PREVENTATIVE_MAINTENANCE" &&
            item.value !== "CORRECTIVE_MAINTENANCE"
        );
        console.log("getOrderTypeList", this.orderTypeList, result);
        return result;
      }
    },
    getListSteps() {
      if (this.isRequestApproval) {
        return this.listApprovalStep.map((item) => {
          const newItem = { ...item };
          newItem.label = this.resources[newItem.label];
          return newItem;
        });
      }
      return this.listSteps.map((item) => {
        const newItem = { ...item };
        newItem.label = this.resources[newItem.label];
        return newItem;
      });
    },
    getMoldIds() {
      return this.getSelectedMold.map((item) => item.id);
    },
    getMachineIds() {
      return this.getSelectedMachine.map((item) => item.id);
    },
    getTerminalIds() {
      return this.getSelectedTerminal.map((item) => item.id);
    },
    getSensorIds() {
      return this.getSelectedSensor.map((item) => item.id);
    },
    getReportFailureShot() {
      if (
        [
          "CORRECTIVE_MAINTENANCE",
          "REFURBISHMENT",
          "DISPOSAL",
          "GENERAL",
        ].includes(this.orderType.value)
      ) {
        return this.approvalItem.shotAtRequest || 0;
      }

      console.log("exception case");
      return 0;
    },
  },
  watch: {
    "orderType.value": function (newVal) {
      console.log("watch orderType.value", newVal);
      if (
        ![
          "GENERAL",
          "CORRECTIVE_MAINTENANCE",
          "REFURBISHMENT",
          "DISPOSAL",
        ].includes(newVal)
      ) {
        this.isRequestApproval = false;
      }
    },
    isRequestApproval: {
      handler(newVal) {
        console.log("isRequestApproval", newVal);
        if (newVal) {
          this.currentStep = {
            key: "TYPE",
            label: "type",
            modalTitle: "select_work_order_type",
            btnTitle: "Continue",
            value: 1,
            checked: false,
          };
        } else {
          this.currentStep = {
            key: "TYPE",
            label: "Type",
            modalTitle: "select_work_order_type",
            btnTitle: "Proceed to Assign",
            value: 1,
            checked: false,
          };
        }
      },
      immediate: true,
    },
    checkedItemsList(newVal) {
      console.log("checked item", newVal);
    },
    currentStep(newVal) {
      console.log("watch step", newVal);
    },
    selectedHourEnd(newVal) {
      console.log("change hour end", newVal);
    },
    selectedHourStart(newVal) {
      console.log("change hour start", newVal);
    },
    async isAssetView(newVal) {
      switch (newVal) {
        case "tooling":
          this.moldPage = 1;
          this.rawMoldIds = [];
          await this.getMolds(this.moldPage, this.searchContent);
          break;
        case "machine":
          this.rawMachineIds = [];
          this.machinePage = 1;
          await this.getMachines(this.machinePage, this.searchContent);
          break;
        case "terminal":
          this.rawTerminalIds = [];
          this.terminalPage = 1;
          await this.getTerminals(this.terminalPage, this.searchContent);
          break;
        case "sensor":
          this.rawSensorIds = [];
          this.sensorPage = 1;
          await this.getSensors(this.sensorPage, this.searchContent);
          break;
      }
    },
    async searchContent(newVal) {
      switch (this.isAssetView) {
        case "tooling":
          this.moldPage = 1;
          this.rawMoldIds = [];
          await this.getMolds(this.moldPage, newVal);
          break;
        case "machine":
          this.rawMachineIds = [];
          this.machinePage = 1;
          await this.getMachines(this.machinePage, newVal);
          break;
        case "terminal":
          this.rawTerminalIds = [];
          this.terminalPage = 1;
          await this.getTerminals(this.terminalPage, newVal);
          break;
        case "sensor":
          this.rawSensorIds = [];
          this.sensorPage = 1;
          await this.getSensors(this.sensorPage, newVal);
          break;
      }
    },
    async isShow(newVal) {
      console.log("show work order", newVal, this.modalType);
      this.searchContent = "";
      if (newVal) {
        this.checkedItemsList = [];
        if (!this.isInitialized) {
          this.isInitialized = true;
          await this.init();
        }
        if (this.modalType === "EDIT" || this.modalType === "REOPEN") {
          console.log("open edit modal", this.selectedItem);
          console.log("open edit modal before", this.moldIds, this.machineIds);
          if (this.modalType === "EDIT") {
            if (
              [
                "CORRECTIVE_MAINTENANCE",
                "GENERAL",
                "REFURBISHMENT",
                "DISPOSAL",
              ].includes(this.selectedItem.orderType) &&
              this.selectedItem?.status === "REQUESTED_NOT_FINISHED"
            ) {
              this.listSteps[3].btnTitle = "Create Work Order";
            } else {
              this.listSteps[3].btnTitle = "Edit Work Order";
            }
          } else if (this.modalType === "REOPEN") {
            this.listSteps[3].btnTitle = "Reopen Work Order";
          }

          this.workOrderId = this.selectedItem.workOrderId;
          this.refCode = this.selectedItem.refCode;
          this.orderType = {
            name: Common.formatter.toCase(
              this.selectedItem.orderType,
              "capitalize"
            ),
            value: this.selectedItem.orderType,
          };
          this.orderTypeDefaultSelected = {
            name: Common.formatter.toCase(
              this.selectedItem.orderType,
              "capitalize"
            ),
            value: this.selectedItem.orderType,
          };
          this.priority = {
            name: Common.formatter.toCase(
              this.selectedItem.priority,
              "capitalize"
            ),
            value: this.selectedItem.priority,
          };
          this.priorityDefaultSelected = {
            name: Common.formatter.toCase(
              this.selectedItem.priority,
              "capitalize"
            ),
            value: this.selectedItem.priority,
          };
          // this.details = this.selectedItem.note;
          this.details = this.selectedItem.details;
          this.cost = this.selectedItem.cost;
          this.failureTime = this.selectedItem.failureTime;
          if (this.selectedItem?.checklist) {
            // this.checklistId = {...this.selectedItem.checklist, title: this.selectedItem.checklist.checklistCode}
            this.checklistId = this.selectedItem.checklist;
          }
          this.pickingList = this.selectedItem.pickingList;

          if (
            this.selectedItem.repeatOn ||
            this.selectedItem.frequent ||
            this.selectedItem.endAfter
          ) {
            this.isDueDateRepeats = true;
          } else {
            this.isDueDateRepeats = false;
          }

          this.repeatOn = {
            name: `${
              this.selectedItem.repeatOn
                ? this.selectedItem.repeatOn.charAt(0)
                : ""
            }${
              this.selectedItem.repeatOn
                ? this.selectedItem.priority.slice(1).toLowerCase()
                : ""
            }`,
            value: this.selectedItem.repeatOn,
          };

          this.frequent = {
            name: `${
              this.selectedItem.frequent
                ? this.selectedItem.frequent.charAt(0)
                : ""
            }${
              this.selectedItem.frequent
                ? this.selectedItem.frequent.slice(1).toLowerCase()
                : ""
            }`,
            value: this.selectedItem.frequent,
          };

          this.endAfter = {
            name: `${
              this.selectedItem.endAfter
                ? this.selectedItem.endAfter.charAt(0)
                : ""
            }${
              this.selectedItem.endAfter
                ? this.selectedItem.endAfter.slice(1).toLowerCase()
                : ""
            }`,
            value: this.selectedItem.endAfter,
          };
          if (this.selectedItem.attachments) {
            const attachmentsRes = await axios.get(
              `/api/file-storage?storageType=WORK_ORDER_FILE&refId=${this.selectedItem.id}`
            );
            const attachments = attachmentsRes.data;
            this.partPictureFiles = attachments;
          }

          //MAP TIME//
          const dateStart = Common.convertTimestampToDate(
            this.selectedItem.start
          );
          const dateEnd = Common.convertTimestampToDate(this.selectedItem.end);
          this.currentDate = {
            from: dateStart,
            to: dateStart,
            fromTitle: moment(dateStart).format("YYYY-MM-DD"),
            toTitle: moment(dateStart).format("YYYY-MM-DD"),
          };
          this.currentDateEnd = {
            from: dateEnd,
            to: dateEnd,
            fromTitle: moment(dateEnd).format("YYYY-MM-DD"),
            toTitle: moment(dateEnd).format("YYYY-MM-DD"),
          };
          this.selectedHourStart.hour = dateStart.getHours();
          this.selectedHourStart.minute = dateStart.getMinutes();
          this.selectedHourEnd.hour = dateEnd.getHours();
          this.selectedHourEnd.minute = dateEnd.getMinutes();

          if (this.selectedItem.orderType === "CORRECTIVE_MAINTENANCE") {
            const time = Common.convertTimestampToDate(this.failureTime);
            this.failureDate = {
              from: time,
              to: time,
              fromTitle: moment(time).format("YYYY-MM-DD"),
              toTitle: moment(time).format("YYYY-MM-DD"),
            };
            this.selectedFailureTime.hour = moment(time).format("HH");
            this.selectedFailureTime.minute = moment(time).format("mm");
          }
          //MAP CHECK ITEM//
          this.moldIds = JSON.parse(JSON.stringify(this.rawMoldIds));
          this.machineIds = JSON.parse(JSON.stringify(this.rawMachineIds));
          this.terminalIds = JSON.parse(JSON.stringify(this.rawTerminalIds));
          this.sensorIds = JSON.parse(JSON.stringify(this.rawSensorIds));
          this.userIds = JSON.parse(JSON.stringify(this.rawUserIds));

          this.checkedItemsList = [...this.selectedItem.workOrderAssets].map(
            (item) => {
              return {
                id: item.assetId,
                title: item.assetCode,
                checked: true,
                assetView: item.type.toLowerCase(),
                locationCode: item.locationCode,
                locationName: item.locationName,
                locationId: item.locationId,
              };
            }
          );

          this.moldIds = this.moldIds.map((item) => {
            let newItem = { ...item };
            const itemId = item.id;
            const find = this.selectedItem.workOrderAssets.find(
              (o) => o.assetId === itemId && o.type === "TOOLING"
            );
            console.log("find mold", find);
            if (find) {
              newItem.checked = true;
            }
            return newItem;
          });

          this.machineIds = this.machineIds.map((item) => {
            let newItem = { ...item };
            const itemId = item.id;
            const find = this.selectedItem.workOrderAssets.find(
              (o) => o.assetId === itemId && o.type === "MACHINE"
            );
            console.log("find machine", find);
            if (find) {
              newItem.checked = true;
            }
            return newItem;
          });

          this.terminalIds = this.terminalIds.map((item) => {
            let newItem = { ...item };
            const itemId = item.id;
            const find = this.selectedItem.workOrderAssets.find(
              (o) => o.assetId === itemId && o.type === "TERMINAL"
            );
            console.log("find terminal", find);
            if (find) {
              newItem.checked = true;
            }
            return newItem;
          });

          this.sensorIds = this.sensorIds.map((item) => {
            let newItem = { ...item };
            const itemId = item.id;
            const find = this.selectedItem.workOrderAssets.find(
              (o) => o.assetId === itemId && o.type === "COUNTER"
            );
            console.log("find sensor", find);
            if (find) {
              newItem.checked = true;
            }
            return newItem;
          });
          this.userIds = this.userIds.map((item) => {
            let newItem = { ...item };
            const itemId = item.id;
            const find = this.selectedItem.workOrderUsers.find(
              (o) => o.userId === itemId
            );
            console.log("find user", find);
            if (find) {
              newItem.checked = true;
              console.log("find user check", newItem);
            }
            return newItem;
          });

          if (
            this.orderType.value === "CORRECTIVE_MAINTENANCE" &&
            this.selectedItem &&
            this.selectedItem.workOrderCostList &&
            this.selectedItem.workOrderCostList[0]
          ) {
            (this.cmEditItem.costEstimate =
              this.selectedItem.workOrderCostList[0].cost),
              (this.cmEditItem.costEstimateDetails =
                this.selectedItem.workOrderCostList[0].details);
            this.cmEditItem.numberOfBackups = this.selectedItem.numberOfBackups;
          }

          console.log("open edit modal after", this.checkedItemsList);
          this.$forceUpdate();
        } else {
          this.setSelectedAsset();
          await this.generateWOID();
          if (this.workOrderType) {
            this.orderTypeDefaultSelected = this.workOrderType;
            this.orderType = this.workOrderType;
            this.priority = {
              name: "Medium",
              value: "MEDIUM",
            };

            if (
              ["GENERAL", "INSPECTION", "PREVENTATIVE_MAINTENANCE"].includes(
                this.workOrderType.value
              )
            )
              this.priorityDefaultSelected = {
                name: "Medium",
                value: "MEDIUM",
              };
            if (
              ["EMERGENCY", "CORRECTIVE_MAINTENANCE"].includes(
                this.workOrderType.value
              )
            ) {
              this.priorityDefaultSelected = { name: "High", value: "HIGH" };
              this.priority = {
                name: "High",
                value: "HIGH",
              };
            }
          }
          this.currentDate = {
            from: new Date(),
            to: new Date(),
            fromTitle: moment(new Date()).format("YYYY-MM-DD"),
            toTitle: moment(new Date()).format("YYYY-MM-DD"),
          };
          this.currentDateEnd = {
            from: new Date(),
            to: new Date(),
            fromTitle: moment(new Date()).format("YYYY-MM-DD"),
            toTitle: moment(new Date()).format("YYYY-MM-DD"),
          };
          this.failureDate = {
            from: new Date(),
            to: new Date(),
            fromTitle: moment(new Date()).format("YYYY-MM-DD"),
            toTitle: moment(new Date()).format("YYYY-MM-DD"),
          };
          this.selectedHourStart.hour = new Date().getHours();
          this.selectedHourStart.minute = new Date().getMinutes();
          this.selectedHourEnd.hour = new Date().getHours();
          this.selectedHourEnd.minute = new Date().getMinutes();
          this.selectedFailureTime.hour = new Date().getHours();
          this.selectedFailureTime.minute = new Date().getMinutes();
          if (this.assetSelected) {
            if (_.isArray(this.assetSelected)) {
              this.checkedItemsList = [
                ...this.checkedItemsList,
                ...this.assetSelected,
              ];
            } else {
              this.checkedItemsList = [
                ...this.checkedItemsList,
                this.assetSelected,
              ];
            }
            console.log(
              "map checkedItemsList by assetSelected",
              this.checkedItemsList,
              this.assetSelected
            );
          }
          this.$forceUpdate();
        }
        if (this.modalType === "CREATE") {
          this.listSteps[3].btnTitle = "Create Work Order";
        } else if (this.modalType === "EDIT") {
          if (
            [
              "CORRECTIVE_MAINTENANCE",
              "GENERAL",
              "REFURBISHMENT",
              "DISPOSAL",
            ].includes(this.selectedItem.orderType) &&
            this.selectedItem?.status === "REQUESTED_NOT_FINISHED"
          ) {
            this.listSteps[3].btnTitle = "Create Work Order";
          } else {
            this.listSteps[3].btnTitle = "Edit Work Order";
          }
        } else {
          this.listSteps[3].btnTitle = "Reopen Work Order";
        }
        this.$forceUpdate();
      } else {
        this.resetModal();
      }
      // else {
      //   this.setSelectedAsset();
      //   await this.generateWOID();
      // }
    },
    hasAssetMachine(newVal) {
      if (newVal)
        this.orderTypeList = ORDER_TYPE_LIST.filter(
          (i) =>
            !["PREVENTATIVE_MAINTENANCE", "CORRECTIVE_MAINTENANCE"].includes(
              i.value
            )
        );
      else this.orderTypeList = [...ORDER_TYPE_LIST];
    },
    hasAssetTerminalSensor(newVal) {
      if (newVal)
        this.orderTypeList = ORDER_TYPE_LIST.filter((i) =>
          ["GENERAL", "INSPECTION", "EMERGENCY"].includes(i.value)
        );
      else this.orderTypeList = [...ORDER_TYPE_LIST];
    },
  },
  created() {
    this.timeOptionsEnd = defaultOptions;
    this.failureOptions = defaultOptions;
    this.timeOptionsStart = defaultOptions;
    this.timeRange = {
      minDate: new Date(),
      maxDate: new Date("2100-01-01"),
    };
    this.failureTimeRange = {
      minDate: null,
      maxDate: new Date(),
    };
    this.clientName = Common.getClientNameAndCompanyId().clientName;
    console.log(this.clientName);
  },
  methods: {
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
    },
    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = this.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      if (files && isExitsFile.length === 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    deleteThirdFiles2: function (index) {
      this.thirdFiles2 = this.thirdFiles2.filter((value, idx) => idx !== index);
    },
    selectedThirdFiles2: function (e) {
      var files = e.target.files;
      var isExitsFile = this.thirdFiles2.filter(
        (item) => item.name === files[0].name
      );
      if (files && isExitsFile.length === 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles2.push(selectedFiles[i]);
        }
      }
    },
    async handleRequestApproval() {
      this.loadingRequestApproval = true;
      let paramObject = { ...this.approvalItem };
      const hourObject = { ...this.getDefaultFailureTime.time };
      let failureTime = this.getDefaultFailureTime.date.from;
      failureTime.setHours(hourObject.hour, hourObject.minute);
      const convertedFailureTime = Common.convertDateToTimestamp(failureTime);
      paramObject.workOrderId = this.workOrderId;
      paramObject.orderType = this.orderType.value;
      paramObject.failureTime = convertedFailureTime;
      paramObject.moldIds = this.getSelectedMold.map((item) => item.id);
      paramObject.machineIds = this.getSelectedMachine.map((item) => item.id);
      paramObject.terminalIds = this.getSelectedTerminal.map((item) => item.id);
      paramObject.counterIds = this.getSelectedSensor.map((item) => item.id);
      paramObject.reportFailureShot = this.getReportFailureShot;
      const totalParams = { ...this.getApprovalPayload, ...paramObject };
      console.log(
        "paramObject",
        paramObject,
        this.getApprovalPayload,
        totalParams
      );
      const params = this.createApprovalFormData(totalParams);
      const res = await axios.post(`/api/work-order/request-approval`, params);
      console.log("res", res);
      this.loadingRequestApproval = false;
      this.handleClose();
      this.setToastAlertGlobal({
        title: "Success!",
        content: "Your request has been sent successfully.",
        show: true,
      });
    },
    createApprovalFormData: function (data) {
      var formData = new FormData();
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("secondFiles", file);
      }
      for (var i = 0; i < this.thirdFiles2.length; i++) {
        let file = this.thirdFiles2[i];
        formData.append("thirdFiles", file);
      }
      formData.append("payload", JSON.stringify(data));
      return formData;
    },
    handleChangeAssets(assets) {
      console.log("handleChangeAssets", assets);
      const { machine, mold, sensor, terminal } = assets;
      if (machine) {
        this.onChangeMachine(machine);
      }
      if (mold) {
        this.onChangeMold(mold);
      }
      if (sensor) {
        this.onChangeSensor(sensor);
      }
      if (terminal) {
        this.onChangeTerminal(terminal);
      }
      this.onChangeCheckedList([...machine, ...mold, ...sensor, ...terminal]);
    },
    async init() {
      try {
        await Promise.all([
          this.getUsers(),
          this.getMolds(),
          this.getMachines(),
          this.getTerminals(),
          this.getSensors(),
          this.getChecklists(),
          this.getChecklistsCount(),
        ]);
      } catch (error) {
        console.log("creator-work-order-dialog.vue>init", error);
      }
    },
    async getChecklistsCount() {
      try {
        const res = await axios.get(`/api/checklist/checkListTypeCount`);
        this.checkListCountByType = res.data;
        console.log("getChecklistsCount", this.checkListCountByType, res);
      } catch (error) {
        console.log(error);
      }
    },
    handleUnselectAllTooling() {
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.assetView !== "tooling"
      );
    },
    handleUnselectAllMachine() {
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.assetView !== "machine"
      );
    },
    handleUnselectAllTerminal() {
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.assetView !== "terminal"
      );
    },
    handleUnselectAllSensor() {
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.assetView !== "counter"
      );
    },
    setSelectedAsset() {
      this.moldIds = JSON.parse(JSON.stringify(this.rawMoldIds));
      this.machineIds = JSON.parse(JSON.stringify(this.rawMachineIds));
      this.terminalIds = JSON.parse(JSON.stringify(this.rawTerminalIds));
      this.sensorIds = JSON.parse(JSON.stringify(this.rawSensorIds));

      this.orderType = {
        name: "General",
        value: "GENERAL",
      };
      this.orderTypeDefaultSelected = {
        name: "General",
        value: "GENERAL",
      };
      this.priority = {
        name: "Medium",
        value: "MEDIUM",
      };
      this.priorityDefaultSelected = {
        name: "Medium",
        value: "MEDIUM",
      };
      if (this.priorityDefault && Object.keys(this.priorityDefault)?.length) {
        if (this.priorityDefault.value === "HIGH") {
          this.orderTypeDefaultSelected = {
            name: "Corrective Maintenance",
            value: "CORRECTIVE_MAINTENANCE",
          };
          this.orderType = {
            name: "Corrective Maintenance",
            value: "CORRECTIVE_MAINTENANCE",
          };
        }
        this.priorityDefaultSelected = this.priorityDefault;
        this.priority = this.priorityDefault;
      }
      if (this.orderTypeDefault && Object.keys(this.orderTypeDefault)?.length) {
        this.orderTypeDefaultSelected = this.orderTypeDefault;
        this.orderType = this.orderTypeDefault;
      }
      setTimeout(() => {
        const assetId =
          _.isArray(this.assetSelected) && this.assetSelected.length > 0
            ? this.assetSelected[0].id
            : this.assetSelected;
        if (this.isToolingAssetSelected) {
          this.moldIds = this.moldIds.map((item) => {
            return item.id === assetId ? { ...item, checked: true } : item;
          });
        } else {
          if (this.isTerminalAssetSelected) {
            this.terminalIds = this.terminalIds.map((item) => {
              return item.id === assetId ? { ...item, checked: true } : item;
            });
          } else if (this.isSensorAssetSelected) {
            this.sensorIds = this.sensorIds.map((item) => {
              return item.id === assetId ? { ...item, checked: true } : item;
            });
          } else {
            this.machineIds = this.machineIds.map((item) => {
              return item.id === assetId ? { ...item, checked: true } : item;
            });
          }
        }
      }, 500);
      console.log(
        "this.moldIds",
        this.moldIds,
        "this.isToolingAssetSelected",
        this.isToolingAssetSelected
      );
    },
    closeSuccessToast() {
      this.successToast = false;
    },
    handleChangeDate(timeValue, frequency) {
      const datePickerModalRef = this.$refs.datePickerModalRef;
      this.currentDate = timeValue;
      this.frequency = frequency;
      datePickerModalRef.closeDatePicker();
    },
    handleShowStartDatePicker() {
      console.log(
        this.$refs.datePickerModalRef,
        "this.$refs.datePickerModalRef"
      );
      console.log("handleShowStartedDatePicker", this.timeRange);
      const datePickerModalRef = this.$refs.datePickerModalRef;
      const dateValue = { ...this.currentDate };
      datePickerModalRef.showDatePicker(this.frequency, dateValue);
    },
    handleChangeDateEnd(timeValue, frequency) {
      const datePickerModalRefEnd = this.$refs.datePickerModalRefEnd;
      this.currentDateEnd = timeValue;
      this.frequency = frequency;
      datePickerModalRefEnd.closeDatePicker();
    },
    handleShowFailureTimeDatePicker() {
      console.log(
        this.$refs.datePickerModalRefFailureTime,
        "this.$refs.datePickerModalRefFailureTime"
      );
      console.log("handleShowFailureTimeDatePicker", this.timeRange);
      const datePickerModalRefFailureTime =
        this.$refs.datePickerModalRefFailureTime;
      const dateValue = { ...this.failureDate };
      datePickerModalRefFailureTime.showDatePicker(this.frequency, dateValue);
    },
    handleChangeFailureTimeDate(timeValue, frequency) {
      const datePickerModalRefFailureTime =
        this.$refs.datePickerModalRefFailureTime;
      this.failureDate = timeValue;
      this.frequency = frequency;
      console.log("handleChangeFailureTimeDate", timeValue, this.failureDate);
      datePickerModalRefFailureTime.closeDatePicker();
    },
    handleShowEndDatePicker() {
      const datePickerModalRefEnd = this.$refs.datePickerModalRefEnd;
      const dateValue = { ...this.currentDateEnd };
      datePickerModalRefEnd.showDatePicker(this.frequency, dateValue);
    },
    deleteThirdFiles: function (index) {
      console.log("deleteThirdFiles", index);
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
      if (this.thirdFiles.length === 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteUploadedFiles: async function (file) {
      const fileId = file.id;
      console.log("deleteThirdFiles", file);
      this.partPictureFiles = this.partPictureFiles.filter(
        (item) => item.id !== fileId
      );
      await axios.delete(`/api/file-storage/${fileId}`);
      this.$forceUpdate();
    },
    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = this.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      if (files && isExitsFile.length === 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    onChangeChecklist(item) {
      console.log("@onChangeChecklist", item);
      this.checklistId = item;
      this.closeRejectRateDropdown();
      this.closeMaintenanceDropdown();
      this.visibleChecklist = false;
    },
    onSearchPagination(searchContent) {
      this.searchContent = searchContent;
    },
    getIndexOf(arr, obj) {
      let index = -1;
      arr.forEach((o, i) => {
        if (o.id == obj.id) index = i;
      });
      return index;
    },
    async getUsers() {
      await axios
        .get("/api/users?status=active&query=&page=1&size=9999")
        .then((res) => {
          this.rawUserIds = res.data.content.map((child) => ({
            ...child,
            title: child.displayName,
            checked: false,
          }));
          this.userIds = JSON.parse(JSON.stringify(this.rawUserIds));
        });
    },
    async getMolds(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/molds/lite-data-work-order?id=&searchEquipmentCode=${searchContent}&status=all&page=${loadPage}&size=${ASSET_SIZE}`
        : `/api/molds/lite-data-work-order?id=&query=&status=all&page=${loadPage}&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.moldTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.equipmentCode,
          checked: false,
        }));
        this.rawMoldIds = [...this.rawMoldIds, ...loadData];
        let temp = _.map(JSON.parse(JSON.stringify(this.rawMoldIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.moldIds = results;
        console.log("getMolds", this.moldIds);
      });
    },
    async getMachines(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/machines?searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`
        : `/api/machines?query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.machineTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.machineCode,
          checked: false,
        }));
        this.rawMachineIds = [...this.rawMachineIds, ...loadData];
        let temp = _.map(JSON.parse(JSON.stringify(this.rawMachineIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.machineIds = results;
      });
    },
    async getTerminals(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/terminals?status=all&searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`
        : `/api/terminals?status=all&query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.terminalTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child.terminal,
          title: child.terminal.equipmentCode,
          checked: false,
        }));
        this.rawTerminalIds = [...this.rawTerminalIds, ...loadData];
        let temp = _.map(JSON.parse(JSON.stringify(this.rawTerminalIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.terminalIds = results;
      });
    },
    async getSensors(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/counters?status=all&searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}&where=opand,eq,eq`
        : `/api/counters?status=all&query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}&where=opand,eq,eq`;
      await axios.get(url).then((res) => {
        this.sensorTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.equipmentCode,
          checked: false,
        }));
        this.rawSensorIds = [...this.rawSensorIds, ...loadData];
        let temp = _.map(JSON.parse(JSON.stringify(this.rawSensorIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.sensorIds = results;
      });
    },
    async handleLoadAssets() {
      switch (this.isAssetView) {
        case "tooling":
          this.moldPage++;
          await this.getMolds(this.moldPage, this.searchContent);
          break;
        case "machine":
          this.machinePage++;
          await this.getMachines(this.machinePage, this.searchContent);
          break;
        case "terminal":
          this.terminalPage++;
          await this.getTerminals(this.terminalPage, this.searchContent);
          break;
        case "sensor":
          this.sensorPage++;
          await this.getSensors(this.sensorPage, this.searchContent);
          break;
      }
    },
    async getChecklists() {
      await Promise.all([
        axios.get("/api/checklist?enabled=true&size=2000").then((res) => {
          this.checklistMaintenanceList = res.data.content.map((child) => ({
            ...child,
            title: child.checklistCode,
          }));
        }),
        axios
          .get("/api/checklist?checklistType=GENERAL&enabled=true&size=2000")
          .then((res) => {
            this.checklistGeneralList = res.data.content.map((child) => ({
              ...child,
              title: child.checklistCode,
            }));
          }),
        axios
          .get(
            "/api/checklist?checklistType=REJECT_RATE&enabled=true&size=2000"
          )
          .then((res) => {
            this.checklistRejectRateList = res.data.content.map((child) => ({
              ...child,
              title: child.checklistCode,
            }));
          }),
        axios
          .get("/api/checklist?checklistType=DISPOSAL&enabled=true&size=2000")
          .then((res) => {
            this.checklistDisposalList = res.data.content.map((child) => ({
              ...child,
              title: child.checklistCode,
            }));
          }),
        axios
          .get(
            "/api/checklist?checklistType=REFURBISHMENT&enabled=true&size=2000"
          )
          .then((res) => {
            this.checklistRefurbishmentList = res.data.content.map((child) => ({
              ...child,
              title: child.checklistCode,
            }));
          }),
        axios
          .get(
            "/api/checklist?checklistType=QUALITY_ASSURANCE&enabled=true&size=2000"
          )
          .then((res) => {
            this.checklistQualityAssuranceList = res.data.content.map(
              (child) => ({
                ...child,
                title: child.checklistCode,
              })
            );
          }),
      ]);
    },
    async generateWOID() {
      await axios.get("/api/work-order/generate-id").then((res) => {
        this.workOrderId = res.data;
      });
    },
    async createWO() {
      this.loading = true;
      const userSelected = this.getSelectedUser.map((user) => {
        return user.id;
      });
      const moldsSelected = this.getSelectedMold.map((user) => {
        return user.id;
      });
      const machinesSelected = this.getSelectedMachine.map((user) => {
        return user.id;
      });
      const terminalsSelected = this.getSelectedTerminal.map((user) => user.id);
      const sensorsSelected = this.getSelectedSensor.map((user) => user.id);
      const start = this.currentDate.to;
      const end = this.currentDateEnd.to;
      start.setHours(
        this.selectedHourStart.hour,
        this.selectedHourStart.minute
      );
      end.setHours(this.selectedHourEnd.hour, this.selectedHourEnd.minute);

      console.log({ start, end });

      const payload = {
        workOrderId: this.workOrderId,
        refCode: this.refCode,
        orderType: this.orderType.value,
        priority: this.priority.value,
        moldIds: moldsSelected,
        machineIds: machinesSelected,
        terminalIds: terminalsSelected,
        counterIds: sensorsSelected,
        userIds: userSelected,
        details: this.details,
        checklistId: this.checklistId ? this.checklistId.id : null,
        cost: this.cost,
        pickingList: this.pickingList,
        start: Common.convertDateToTimestamp(start),
        end: Common.convertDateToTimestamp(end),
        frequent: this.isDueDateRepeats ? this.frequent.value : null,
        repeatOn: this.isDueDateRepeats
          ? this.repeatOn.value.toUpperCase()
          : null,
        endAfter: this.isDueDateRepeats ? this.endAfter.value : null,
      };
      console.log(payload, "params");
      console.log(this.thirdFiles, "thirdFiles");

      if (payload.orderType === "CORRECTIVE_MAINTENANCE") {
        const failureTime = this.failureDate.to;
        failureTime.setHours(
          this.selectedFailureTime.hour,
          this.selectedFailureTime.minute
        );
        payload.failureTime = Common.convertDateToTimestamp(failureTime);
      }
      await axios.post(
        "/api/work-order/add-multipart",
        this.createFormData(payload),
        this.multipartHeader()
      );
      this.loading = false;
      this.handleClose();

      if (this.notification) {
        this.notification.title = `Success!`;
        this.notification.content = `Your work order has been created.`;
        this.notification.show = true;
      } else {
        this.setToastAlertGlobal({
          title: `Success!`,
          content: `Your work order has been created.`,
          show: true,
        });
      }

      this.$emit("reload-page");
      this.resetModal();
      setTimeout(() => {
        if (this.notification) {
          this.notification.show = false;
        } else {
          this.setToastAlertGlobal({ title: "", content: "", show: false });
        }
      }, 3000);
    },
    async editWO() {
      this.loading = true;
      const userSelected = this.getSelectedUser.map((user) => {
        return user.id;
      });
      const moldsSelected = this.getSelectedMold.map((user) => {
        return user.id;
      });
      const machinesSelected = this.getSelectedMachine.map((user) => {
        return user.id;
      });
      const terminalsSelected = this.getSelectedTerminal.map((user) => user.id);
      const sensorsSelected = this.getSelectedSensor.map((user) => user.id);
      const start = this.currentDate.to;
      const end = this.currentDateEnd.to;
      start.setHours(
        this.selectedHourStart.hour,
        this.selectedHourStart.minute
      );
      end.setHours(this.selectedHourEnd.hour, this.selectedHourEnd.minute);
      const payload = {
        id: this.selectedItem.id,
        workOrderId: this.workOrderId,
        refCode: this.refCode,
        orderType: this.orderType.value,
        priority: this.priority.value,
        moldIds: moldsSelected,
        machineIds: machinesSelected,
        terminalIds: terminalsSelected,
        counterIds: sensorsSelected,
        userIds: userSelected,
        details: this.details,
        checklistId: this.checklistId ? this.checklistId.id : null,
        cost: this.cost,
        pickingList: this.pickingList,
        start: Common.convertDateToTimestamp(start),
        end: Common.convertDateToTimestamp(end),
        frequent: this.isDueDateRepeats ? this.frequent.value : null,
        repeatOn: this.isDueDateRepeats
          ? this.repeatOn.value.toUpperCase()
          : null,
        endAfter: this.isDueDateRepeats ? this.endAfter.value : null,
      };
      if (
        [
          "CORRECTIVE_MAINTENANCE",
          "GENERAL",
          "REFURBISHMENT",
          "DISPOSAL",
        ].includes(this.orderType.value) &&
        this.selectedItem?.status === "REQUESTED_NOT_FINISHED"
      ) {
        payload.numberOfBackups = this.cmEditItem.numberOfBackups;
        payload.costEstimate = this.cmEditItem.costEstimate;
        payload.costDetails = this.cmEditItem.costEstimateDetails;
        payload.failureTime = Common.convertDateToTimestamp(
          this.failureDate.to
        );
      }
      console.log(payload, "params");
      console.log(this.thirdFiles, "thirdFiles");

      await axios.post(
        "/api/work-order/edit-multipart",
        this.createFormData(payload),
        this.multipartHeader()
      );
      this.loading = false;
      this.handleClose();
      if (
        [
          "CORRECTIVE_MAINTENANCE",
          "GENERAL",
          "REFURBISHMENT",
          "DISPOSAL",
        ].includes(this.orderType.value) &&
        this.selectedItem?.status === "REQUESTED_NOT_FINISHED"
      ) {
        if (this.notification) {
          this.notification.title = "Success";
          this.notification.content = `Your work order has been created.`;
          this.notification.show = true;
        } else {
          this.setToastAlertGlobal({
            title: "Success!",
            content: "Your work order has been created.",
            show: true,
          });
        }
      } else {
        if (this.notification) {
          this.notification.title = "Success";
          this.notification.content = `Your work order has been edited.`;
          this.notification.show = true;
        } else {
          this.setToastAlertGlobal({
            title: "Success!",
            content: "Your work order has been edited.",
            show: true,
          });
        }
      }

      this.$emit("reload-page");
      this.resetModal();
      setTimeout(() => {
        if (this.notification) this.notification.show = false;
        else this.setToastAlertGlobal({ title: "", content: "", show: false });
      }, 3000);
    },
    async reopenWO() {
      this.loading = true;
      const userSelected = this.getSelectedUser.map((user) => {
        return user.id;
      });
      const moldsSelected = this.getSelectedMold.map((user) => {
        return user.id;
      });
      const machinesSelected = this.getSelectedMachine.map((user) => {
        return user.id;
      });
      const terminalsSelected = this.getSelectedTerminal.map((user) => user.id);
      const sensorsSelected = this.getSelectedSensor.map((user) => user.id);
      const start = this.currentDate.to;
      const end = this.currentDateEnd.to;
      start.setHours(
        this.selectedHourStart.hour,
        this.selectedHourStart.minute
      );
      end.setHours(this.selectedHourEnd.hour, this.selectedHourEnd.minute);
      const payload = {
        id: this.selectedItem.id,
        workOrderId: this.workOrderId,
        orderType: this.orderType.value,
        priority: this.priority.value,
        moldIds: moldsSelected,
        machineIds: machinesSelected,
        terminalIds: terminalsSelected,
        counterIds: sensorsSelected,
        userIds: userSelected,
        details: this.details,
        checklistId: this.checklistId ? this.checklistId.id : null,
        cost: this.cost,
        pickingList: this.pickingList,
        start: Common.convertDateToTimestamp(start),
        end: Common.convertDateToTimestamp(end),
        frequent: this.isDueDateRepeats ? this.frequent.value : null,
        repeatOn: this.isDueDateRepeats
          ? this.repeatOn.value.toUpperCase()
          : null,
        endAfter: this.isDueDateRepeats ? this.endAfter.value : null,
      };
      console.log(payload, "params");
      console.log(this.thirdFiles, "thirdFiles");

      await axios.post(
        "/api/work-order/re-open",
        this.createFormData(payload),
        this.multipartHeader()
      );
      this.loading = false;
      this.handleClose();
      this.toastAlert.content = `Your work order has been reopened.`;
      this.successToast = true;

      this.$emit("reload-page");
      this.resetModal();
      setTimeout(() => {
        this.successToast = false;
      }, 3000);
    },
    createFormData: function (data) {
      var formData = new FormData();
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }

      formData.append("payload", JSON.stringify(data));
      return formData;
    },
    onChangeOrderType(item) {
      this.orderType = item;
      console.log(item.value, "item.value");
      if (
        item.value === "EMERGENCY" ||
        item.value === "CORRECTIVE_MAINTENANCE"
      ) {
        this.priorityDefaultSelected = {
          name: "High",
          value: "HIGH",
        };
        this.priority = {
          name: "High",
          value: "HIGH",
        };
      } else {
        this.priorityDefaultSelected = {
          name: "Medium",
          value: "MEDIUM",
        };
        this.priority = {
          name: "Medium",
          value: "MEDIUM",
        };
      }
    },
    onChangeFrequent(item) {
      this.frequent = item;
    },
    onChangeRepeatOn(item) {
      this.repeatOn = item;
    },
    onChangeEndAfter(item) {
      this.endAfter = item;
    },
    handleChangeHourStart(value, type) {
      this.selectedHourStart[type] = value;
      console.log("handleChangeHourStart", value, type);
    },
    handleChangeHourEnd(value, type) {
      this.selectedHourEnd[type] = value;
      console.log("handleChangeHourEnd", value, type);
    },
    handleChangeFailureTime(value, type) {
      this.selectedFailureTime[type] = value;
      console.log("handleChangeFailureTime", value, type);
    },
    onChangeAttachment(item) {
      this.attachment = item.value;
      this.closeUserDropdown();
    },
    openUserDropdown() {
      this.selecteUserDropdown = true;
    },
    closeUserDropdown() {
      this.selecteUserDropdown = false;
    },
    onChangePriorityType(item) {
      this.priority = item;
    },
    onChangeMold(results) {
      this.moldIds = results;
    },
    onChangeMachine(results) {
      this.machineIds = results;
    },
    onChangeTerminal(results) {
      this.terminalIds = results;
    },
    onChangeSensor(results) {
      this.sensorIds = results;
    },
    onChangeCheckedList(results) {
      console.log("onChangeCheckedList", results);
      this.checkedItemsList = results;
    },
    onChangeUser(results) {
      this.userIds = results;
    },
    openAssetDropdown() {
      this.isAssetView = false;
      this.visibleAsset = true;
    },
    openChecklistDropdown() {
      this.isChecklistView = false;
      this.visibleChecklist = true;
    },
    closeRejectRateDropdown() {
      this.isVisibleRejectRateDropdown = false;
      this.isChecklistView = "";
    },
    closeQualityAssuranceDropdown() {
      this.isVisibleQualityAssuranceDropdown = false;
      this.isChecklistView = "";
    },
    closeMaintenanceDropdown() {
      this.isVisibleMaintenanceDropdown = false;
      this.isChecklistView = "";
    },
    closeGeneralDropdown() {
      this.isVisibleGeneralDropdown = false;
      this.isChecklistView = "";
    },
    closeDisposalDropdown() {
      this.isVisibleDisposalDropdown = false;
      this.isChecklistView = "";
    },
    closeRefurbishmentDropdown() {
      this.isVisibleRefurbishmentDropdown = false;
      this.isChecklistView = "";
    },
    closeMoldDropdown() {
      this.isVisibleMoldDropdown = false;
      this.isAssetView = false;
    },
    closeMachineDropdown() {
      this.isVisibleMachineDropdown = false;
      this.isAssetView = false;
    },
    closeTerminalDropdown() {
      this.isVisibleTerminalDropdown = false;
      this.isAssetView = false;
    },
    closeSensorDropdown() {
      this.isVisibleSensorDropdown = false;
      this.isAssetView = false;
    },
    onRemoveMold(asset) {
      console.log("onRemoveMold", asset);
      this.moldIds = this.moldIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
    },
    onRemoveMachine(asset) {
      console.log("onRemoveMachine", asset);
      this.machineIds = this.machineIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
    },
    handleScrollContentToBottom() {
      const selectedDom = document.querySelector(".steps-content");
      console.log("handleScrollContentToBottom", selectedDom);
      setTimeout(() => {
        selectedDom.scrollTop = selectedDom.scrollHeight;
      }, 100);
    },
    onRemoveTerminal(asset) {
      console.log("onRemoveTerminal", asset);
      this.terminalIds = this.terminalIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
    },
    onRemoveSensor(asset) {
      console.log("onRemoveSensor", asset);
      this.sensorIds = this.sensorIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
    },
    onRemoveChecklist() {
      this.checklistId = null;
    },
    resetModal() {
      this.listSteps = this.listSteps.map((i) => ({ ...i, checked: false }));
      this.refCode = "";
      this.currentStep = this.listSteps[0];
      this.selectedHourStart = {
        hour: "0",
        minute: "0",
        isAddDay: false,
      };
      this.selectedHourEnd = {
        hour: "0",
        minute: "0",
        isAddDay: false,
      };
      if (!this.isEdit) {
        this.userIds = JSON.parse(JSON.stringify(this.rawUserIds));
      }
      this.details = "";
      this.checklistId = null;
      this.cost = false;
      this.pickingList = false;
      this.start = this.end = "";
      this.frequent = {
        name: "Weekly",
        value: "WEEKLY",
      };
      this.repeatOn = {
        name: "Exact date",
        value: null,
      };
      this.endAfter = {
        name: "Never",
        value: null,
      };
      this.thirdFiles = [];
      this.partPictureFiles = [];
      (this.thirdFiles2 = []),
        (this.partPictureFiles2 = []),
        (this.isDueDateRepeats = false);
      this.clearAssetsKey++;
      this.$forceUpdate();
    },
    async navigate(state) {
      if (state === "next") {
        const foundIndex = this.listSteps.findIndex(
          (i) => i.key === this.currentStep?.key
        );
        if (foundIndex > -1)
          this.listSteps.splice(foundIndex, 1, {
            ...this.listSteps[foundIndex],
            checked: true,
          });
        this.currentStep = this.listSteps[foundIndex];

        if (this.currentStep.value === 1 && this.isRequestApproval) {
          this.showApprovalModal();
          this.getShotAtRequest();
          // TODO: call api get shot at request
        } else {
          const isLast = this.currentStep.value === this.listSteps.length;
          this.currentStep = isLast
            ? this.currentStep
            : this.listSteps[this.currentStep.value];
          if (isLast && !this.isLoading) {
            this.isLoading = true;
            if (this.modalType === "EDIT") {
              await this.editWO();
            } else if (this.modalType === "REOPEN") {
              await this.reopenWO();
            } else {
              await this.createWO();
            }
            this.isLoading = false;
          }
        }
      } else {
        if (this.isVisibleApprovalModal) {
          this.currentStep = this.listSteps[0];
          this.isVisibleApprovalModal = false;
        }
        const isFirst = this.currentStep.value === 1;
        const nextIndex = this.currentStep.value - 2;
        this.currentStep = isFirst
          ? this.currentStep
          : this.listSteps[nextIndex];
      }
    },
    handleClose() {
      this.isRequestApproval = false;
      this.currentDate = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      };
      (this.currentDateEnd = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      }),
        (this.isAssetView = "");
      this.visibleAsset = false;
      this.isVisibleMoldDropdown = false;
      this.isVisibleMachineDropdown = false;
      this.isVisibleTerminalDropdown = false;
      this.isVisibleSensorDropdown = false;
      this.isVisibleApprovalModal = false;
      this.$emit("close");
    },
    showApprovalModal() {
      this.isVisibleApprovalModal = true;
      this.currentStep = Object.assign({}, this.currentStep, {
        key: "APPROVAL",
        label: `Request Approval for ${this.orderType.value.toLowerCase()}`,
        modalTitle: `request_approval_for_${this.orderType.value.toLowerCase()}`,
        btnTitle: "Request Approval",
        value: 2,
        checked: false,
      });
      Vue.set(this.listApprovalStep, 0, {
        ...this.listApprovalStep[0],
        checked: true,
      });
      console.log("showApprovalModal", this.currentStep);
      // this.handleClose();
    },
    closeApprovalModal() {
      this.isVisibleApprovalModal = false;
    },
    handleChangeStep(newStep) {
      this.currentStep = Object.assign({}, this.currentStep, { ...newStep });
    },
    async getShotAtRequest() {
      try {
        const hourObject = { ...this.getDefaultFailureTime.time };
        let failureTime = this.getDefaultFailureTime.date.from;
        failureTime.setHours(hourObject.hour, hourObject.minute);
        const convertedFailureTime = Common.convertDateToTimestamp(failureTime);
        const params = {};
        params.id = this.getSelectedMold[0].id;
        params.failureTime = convertedFailureTime;
        const response = await axios.get("/api/molds/accumulated-shot", {
          params,
        });
        this.approvalItem.shotAtRequest = response?.data?.data;
      } catch (error) {
        console.log(error);
      }
    },
  },
};
</script>

<style scoped>
.steps-content {
  overflow-y: scroll;
  max-height: 550px;
  padding-top: 10px;
  scroll-behavior: smooth;
}

.custom-dropdown {
  display: inline-block;
}

.custom-base-steps {
  display: flex;
  justify-content: center;
  margin-top: 6px;
  margin-bottom: 24px;
}

.create-wo-container {
  padding: 0 31px;
}

.step-content {
  /* min-height: 400px; */
}

.hidden-zone {
  opacity: 0.5;
  pointer-events: none;
}

.modal__container__header {
  position: absolute;
  left: 0;
  top: 0;
}

.back-icon {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.back-icon__text {
  margin-left: 8px;
  cursor: pointer;
}

.col-form-label {
  min-width: 120px;
}

.modal__footer {
  margin-top: 80px;
  justify-content: flex-end;
}

.modal__footer__button {
  margin-left: 8px;
}

.custom-modal__body {
  padding: 20px 25px !important;
}

.d-flex .modal__footer {
  margin-top: 0;
}

textarea:read-only {
  color: var(--grey-dark);
  background: var(--grey-5);
  border: none;
}

@media (max-height: 800px) {
  .steps-zone .steps-content {
    max-height: 350px;
  }
}
</style>
