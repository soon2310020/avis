<template>
  <div
    style="overflow: scroll"
    id="op-mold-details-form"
    class="modal fade"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form class="needs-validation" @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <div
              class="head-line"
              style="
                position: absolute;
                background: #52a1ff;
                height: 8px;
                border-radius: 6px 6px 0 0;
                top: 0;
                left: 0;
                width: 100%;
              "
            ></div>
            <span class="span-title">Complete Data</span>
            <span
              class="close-button"
              style="
                font-size: 25px;
                display: flex;
                align-items: center;
                height: 17px;
                cursor: pointer;
              "
              data-dismiss="modal"
              aria-label="Close"
            >
              <span class="t-icon-close"></span>
            </span>
          </div>
          <div class="modal-body">
            <!-- Basic Information-->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['basic_info_static']"></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label
                        class="col-md-3 col-form-label"
                        for="mold-id"
                        v-text="resources['tooling_id']"
                      ></label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="mold-id"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.equipmentCode == '' ||
                              mold.equipmentCode == null,
                          }"
                          v-model="mold.equipmentCode"
                          :placeholder="resources['tooling_id']"
                          required
                        />

                        <input type="hidden" v-model="mold.equipmentStatus" />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolingLetter']"
                    >
                      <label class="col-md-3 col-form-label" for="mold-id"
                        ><span v-text="resources['tooling_letter']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolingLetter')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          :placeholder="resources['tooling_letter']"
                          v-model="mold.toolingLetter"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.toolingLetter == '' ||
                              mold.toolingLetter == null,
                          }"
                          :required="isRequiredField('toolingLetter')"
                        />

                        <input type="hidden" v-model="mold.toolingLetter" />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolingType']"
                    >
                      <label class="col-md-3 col-form-label" for="toolingType"
                        ><span v-text="resources['tooling_type']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolingType')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          :placeholder="resources['tooling_type']"
                          id="toolingType"
                          v-model="mold.toolingType"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.toolingType == '' ||
                              mold.toolingType == null,
                          }"
                          :required="isRequiredField('toolingType')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolingComplexity']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="toolingComplexity"
                        ><span v-text="resources['tooling_complexity']"></span>
                        <!--<span class="badge-require"></span>-->
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolingComplexity')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <div v-if="isToolingComplexityEnabled">
                          <input
                            id="toolingComplexity"
                            type="text"
                            v-model="mold.toolingComplexity"
                            class="form-control"
                            :class="{
                              'form-control-warning':
                                mold.toolingComplexity == '' ||
                                mold.toolingComplexity == null,
                            }"
                            :placeholder="resources['tooling_complexity']"
                            :required="isRequiredField('toolingComplexity')"
                          />
                        </div>
                        <div v-else>
                          <select
                            :required="isRequiredField('toolingComplexity')"
                            :placeholder="resources['tooling_complexity']"
                            style="color: #9ca5af"
                            class="form-control"
                            v-model="mold.toolingComplexity"
                            :class="{
                              'form-control-warning':
                                mold.toolingComplexity == '' ||
                                mold.toolingComplexity == null,
                            }"
                          >
                            <option
                              class="first-option"
                              value=""
                              disabled
                              selected
                              hidden
                              v-text="resources['tooling_complexity']"
                            ></option>
                            <template
                              v-for="(code, index) in ['A+', 'A', 'B', 'C']"
                              :key="index"
                            >
                              <option :value="code">{{ code }}</option>
                            </template>
                          </select>
                        </div>
                      </div>
                    </div>

                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['designedShot']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="forecasted-max-shots"
                        v-text="resources['forecasted_max_shot']"
                      >
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="forecasted-max-shots"
                          min="1"
                          max="99999999"
                          :placeholder="resources['forecasted_max_shot']"
                          oninput="Common.maxLength(this, 8)"
                          v-model="mold.designedShot"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.designedShot == '' ||
                              mold.designedShot == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                    <div
                      v-if="
                        isLifeYearsEnabled && !checkDeleteTooling['lifeYears']
                      "
                      class="form-group row"
                    >
                      <label class="col-md-3 col-form-label" for="lifeYears">
                        <span v-text="resources['year_of_tool_made']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('lifeYears')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="lifeYears"
                          min="1"
                          max="99"
                          :placeholder="resources['year_of_tool_made']"
                          oninput="Common.maxLength(this, 2)"
                          v-model="mold.lifeYears"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.lifeYears == '' || mold.lifeYears == null,
                          }"
                          :required="isRequiredField('lifeYears')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['madeYear']"
                    >
                      <label class="col-md-3 col-form-label" for="madeYear"
                        ><span v-text="resources['year_of_tool_made']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('madeYear')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="madeYear"
                          min="1900"
                          :placeholder="resources['year_of_tool_made']"
                          oninput="Common.maxLength(this, 4)"
                          v-model="mold.madeYear"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.madeYear == '' || mold.madeYear == null,
                          }"
                          :required="isRequiredField('madeYear')"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-3 col-form-label" for="location"
                        ><span v-text="resources['location']"></span>
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-5">
                        <input
                          type="hidden"
                          v-model="mold.locationId"
                          required
                        />
                        <input
                          type="text"
                          id="location"
                          :key="locationKey"
                          :placeholder="resources['location']"
                          v-model="mold.locationName"
                          readonly="readonly"
                          class="form-control"
                          required
                        />
                      </div>
                      <div class="col-md-4">
                        <button
                          type="button"
                          class="btn btn-outline-success"
                          data-toggle="modal"
                          :data-target="
                            getRootToCurrentId() +
                            '#op-location-search-mold-modal'
                          "
                          @click="findLocation()"
                          v-text="resources['location_search']"
                        ></button>
                      </div>
                    </div>
                    <div
                      v-if="
                        engineers.length > 0 && !checkDeleteTooling['engineers']
                      "
                      class="form-group row"
                    >
                      <label class="col-md-3 col-form-label"
                        ><span v-text="resources['engineer_in_charge']"></span>
                        <!--<span class="badge-require"></span>-->
                        <span
                          class="badge-require"
                          v-if="isRequiredField('engineers')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <engineer-charge-dropdown
                          :checked-list="checkedList"
                          :set-result="handleChange"
                          :default-value="engineerIndex"
                          :items="engineers"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              engineerIndex == undefined ||
                              engineerIndex == null ||
                              engineerIndex.length < 1,
                          }"
                          :required="isRequiredField('engineers')"
                          :resources="resources"
                        >
                        </engineer-charge-dropdown>
                        <!-- <a-select :default-value="engineerIndex" :value="engineerIndex" mode="multiple"
                                        style="width: 100%" @change="handleChange" placeholder="Engineer in Charge">
                                        <a-select-option v-for="value in engineers" :key="value.id" :value="value.id">
                                            {{value ? value.name : ''}}</a-select-option>
                                    </a-select> -->
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolDescription']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="toolDescription"
                      >
                        <span v-text="resources['tool_description']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolDescription')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <textarea
                          type="text"
                          id="toolDescription"
                          v-model="mold.toolDescription"
                          :placeholder="resources['tool_description']"
                          rows="4"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.toolDescription == '' ||
                              mold.toolDescription == null,
                          }"
                          :required="isRequiredField('toolDescription')"
                        ></textarea>
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'BASIC'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Physical Information -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['physical_info']"></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['size']"
                    >
                      <label class="col-md-3 col-form-label" for="size"
                        ><span v-text="resources['tool_size']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('size')"
                        ></span>
                      </label>
                      <div
                        style="
                          display: flex;
                          justify-content: space-between;
                          align-items: center;
                        "
                        class="col-md-6"
                      >
                        <input
                          type="number"
                          style="width: 30%"
                          id="size"
                          v-model="mold.sizeWidth"
                          :placeholder="resources['width']"
                          min="0"
                          step="any"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.sizeWidth == '' ||
                              mold.sizeWidth == null ||
                              mold.sizeHeight == '0',
                          }"
                          :required="isRequiredField('size')"
                        />
                        <div>x</div>
                        <input
                          type="number"
                          style="width: 30%"
                          id="size"
                          v-model="mold.sizeDepth"
                          :placeholder="resources['length']"
                          min="0"
                          step="any"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.sizeDepth == '' ||
                              mold.sizeDepth == null ||
                              mold.sizeHeight == '0',
                          }"
                          :required="isRequiredField('size')"
                        />
                        <div>x</div>
                        <input
                          type="number"
                          style="width: 30%"
                          id="size"
                          v-model="mold.sizeHeight"
                          :placeholder="resources['height']"
                          min="0"
                          step="any"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.sizeHeight == '' ||
                              mold.sizeHeight == null ||
                              mold.sizeHeight == '0',
                          }"
                          :required="isRequiredField('size')"
                        />
                        <!-- <input id="size" v-model="mold.WLH" class="form-control" placeholder="Tool Size (WxLxH)"> -->
                      </div>
                      <div class="col-md-3">
                        <select
                          style="color: #9ca5af"
                          class="form-control"
                          v-model="mold.sizeUnit"
                          :class="{
                            'form-control-warning':
                              mold.sizeUnit == '' || mold.sizeUnit == null,
                          }"
                        >
                          <option
                            disabled
                            selected
                            hidden
                            :value="null"
                            v-text="resources['unit']"
                          ></option>
                          <template
                            v-for="(code, index) in codes.SizeUnit"
                            :key="index"
                          >
                            <option :value="code.code">{{ code.title }}</option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['weight']"
                    >
                      <label class="col-md-3 col-form-label" for="weight"
                        ><span v-text="resources['tool_weight']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('weight')"
                        ></span>
                      </label>
                      <div class="col-md-6">
                        <input
                          type="number"
                          id="weight"
                          v-model="mold.weight"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.weight == '' || mold.weight == null,
                          }"
                          step="any"
                          :placeholder="resources['tool_weight']"
                          :required="isRequiredField('weight')"
                        />
                      </div>
                      <div class="col-md-3">
                        <select
                          style="color: #9ca5af"
                          class="form-control"
                          v-model="mold.weightUnit"
                          :class="{
                            'form-control-warning':
                              mold.weightUnit == '' || mold.weightUnit == null,
                          }"
                        >
                          <option
                            disabled
                            selected
                            hidden
                            :value="null"
                            v-text="resources['unit']"
                          ></option>
                          <template
                            v-for="(code, index) in codes.WeightUnit"
                            :key="index"
                          >
                            <option :value="code.code">{{ code.title }}</option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['shotSize']"
                    >
                      <label class="col-md-3 col-form-label" for="shotSize"
                        ><span
                          v-text="resources['shot_weight'] + '(gram)'"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('shotSize')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          disabled
                          type="number"
                          v-model="mold.shotSize"
                          class="form-control"
                          step="any"
                          :placeholder="resources['shot_weight'] + '(gram)'"
                          readonly
                          :required="isRequiredField('shotSize')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolMakerCompanyName']"
                    >
                      <label class="col-md-3 col-form-label" for="toolMaker"
                        ><span v-text="resources['toolmaker']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolMakerCompanyName')"
                        ></span>
                      </label>
                      <div class="col-md-5">
                        <input
                          type="text"
                          id="toolMaker"
                          v-model="mold.toolMakerCompanyName"
                          :placeholder="resources['toolmaker']"
                          readonly="readonly"
                          class="form-control"
                          :required="isRequiredField('toolMakerCompanyName')"
                        />
                      </div>
                      <div class="col-md-4">
                        <button
                          type="button"
                          class="btn btn-outline-success"
                          v-text="resources['toolmaker_search']"
                          @click.prevent="findCompany('TOOL_MAKER')"
                        ></button>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['injectionMachineId']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="injectionMolding"
                        ><span
                          v-text="resources['injection_molding_machine_id']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('injectionMachineId')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          v-model="mold.injectionMachineId"
                          :placeholder="
                            resources['injection_molding_machine_id']
                          "
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.injectionMachineId == '' ||
                              mold.injectionMachineId == null,
                          }"
                          :required="isRequiredField('injectionMachineId')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['quotedMachineTonnage']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="machineTonnageQuote"
                        ><span v-text="resources['machine_tonnage']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('quotedMachineTonnage')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          v-model="mold.quotedMachineTonnage"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.quotedMachineTonnage == '' ||
                              mold.quotedMachineTonnage == null,
                          }"
                          :placeholder="resources['machine_tonnage']"
                          step="any"
                          :required="isRequiredField('quotedMachineTonnage')"
                        />
                      </div>
                    </div>

                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['toolingPictureFile']"
                    >
                      <label class="col-md-3 col-form-label"
                        ><span v-text="resources['tooling_picture']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('toolingPictureFile')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <div class="op-upload-button-wrap">
                          <button
                            class="btn btn-outline-success"
                            v-text="resources['upload_photo']"
                          ></button>
                          <input
                            type="file"
                            id="files1"
                            @change="selectedThirdFiles"
                            multiple
                            accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
                            :required="
                              isRequiredField('toolingPictureFile') &&
                              toolingConditionFiles.length == 0
                            "
                          />
                        </div>
                        <div>
                          <button
                            v-for="(file, index) in thirdFiles"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="deleteThirdFiles(index)"
                            :key="index"
                          >
                            {{ file.name }}
                          </button>
                        </div>
                        <div class="mt-1">
                          <button
                            v-for="(file, index) in toolingConditionFiles"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="
                              deleteFileStorage(file, fileTypes.MOLD_CONDITION)
                            "
                            :key="index"
                          >
                            {{ file.fileName }}
                          </button>
                        </div>
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'PHYSICAL'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Runner -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['runner_system_info']"></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['runnerType']"
                    >
                      <label class="col-md-3 col-form-label" for="runnerType"
                        ><span
                          v-text="resources['type_of_runner_system']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('runnerType')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="runnerType"
                          v-model="mold.runnerType"
                          :placeholder="resources['type_of_runner_system']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.runnerType == '' || mold.runnerType == null,
                          }"
                          :required="isRequiredField('runnerType')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['runnerMaker']"
                    >
                      <label class="col-md-3 col-form-label" for="runnerMaker"
                        ><span
                          v-text="resources['maker_of_runner_system']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('runnerMaker')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="runnerMaker"
                          :placeholder="resources['maker_of_runner_system']"
                          v-model="mold.runnerMaker"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.runnerMaker == '' ||
                              mold.runnerMaker == null,
                          }"
                          :required="isRequiredField('runnerMaker')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['weightRunner']"
                    >
                      <label class="col-md-3 col-form-label" for="weightRunner"
                        ><span
                          v-text="resources['weight_of_runner_system_g']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('weightRunner')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="weightRunner"
                          v-model="mold.weightRunner"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.weightRunner == '' ||
                              mold.weightRunner == null,
                          }"
                          :placeholder="resources['weight_of_runner_system_g']"
                          step="any"
                          :required="isRequiredField('weightRunner')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['hotRunnerDrop']"
                    >
                      <label class="col-md-3 col-form-label" for="weightRunner"
                        ><span
                          v-text="resources['hot_runner_number_of_drop']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('hotRunnerDrop')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="hotRunnerDrop"
                          v-model="mold.hotRunnerDrop"
                          :placeholder="resources['hot_runner_number_of_drop']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.hotRunnerDrop == '' ||
                              mold.hotRunnerDrop == null,
                          }"
                          step="any"
                          :required="isRequiredField('hotRunnerDrop')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['hotRunnerZone']"
                    >
                      <label class="col-md-3 col-form-label" for="weightRunner"
                        ><span v-text="resources['hot_runner_zone']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('hotRunnerZone')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="hotRunnerZone"
                          v-model="mold.hotRunnerZone"
                          :placeholder="resources['hot_runner_zone']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.hotRunnerZone == '' ||
                              mold.hotRunnerZone == null,
                          }"
                          step="any"
                          :required="isRequiredField('hotRunnerZone')"
                        />
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'RUNNER_SYSTEM'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Cost -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['cost_info']"></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['cost']"
                    >
                      <label class="col-md-3 col-form-label" for="cost">
                        <span v-text="resources['cost_tooling']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('cost')"
                        ></span>
                      </label>
                      <div class="col-md-6">
                        <input
                          id="cost"
                          type="text"
                          v-model="mold.cost"
                          :placeholder="resources['cost_tooling']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.cost == '' || mold.cost == null,
                          }"
                          :required="isRequiredField('cost')"
                        />
                      </div>
                      <div class="col-md-3">
                        <select
                          class="form-control"
                          v-model="mold.costCurrencyType"
                          :class="{
                            'form-control-warning':
                              mold.costCurrencyType == '' ||
                              mold.costCurrencyType == null,
                          }"
                          :required="isRequiredField('cost')"
                        >
                          <option
                            value=""
                            disabled
                            selected
                            hidden
                            v-text="resources['currency']"
                          ></option>
                          <template
                            v-for="(code, index) in codes.CurrencyType"
                            :key="index"
                          >
                            <option :value="code.code">{{ code.code }}</option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['accumulatedMaintenanceCost']"
                    >
                      <label class="col-md-3 col-form-label">
                        <span
                          v-text="resources['accumulated_maintenance_cost']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('accumulatedMaintenanceCost')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          disabled
                          type="text"
                          :placeholder="
                            resources['accumulated_maintenance_cost']
                          "
                          v-model="mold.accumulatedMaintenanceCost"
                          class="form-control"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['memo']"
                    >
                      <label class="col-md-3 col-form-label">
                        <span v-text="resources['memo']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('memo')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <textarea
                          v-model="mold.memo"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.memo == '' || mold.memo == null,
                          }"
                          rows="4"
                          :placeholder="resources['memo']"
                          :required="isRequiredField('memo')"
                        ></textarea>
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'COST'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Suppilier information -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong
                      ><span v-text="resources['supplier_info']"></span
                    ></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['supplierCompanyName']"
                    >
                      <label class="col-md-3 col-form-label" for="supplier">
                        <span v-text="resources['supplier']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('supplierCompanyName')"
                        ></span>
                      </label>
                      <div class="col-md-9" v-if="isToolMaker">
                        <input
                          type="text"
                          id="supplier"
                          v-model="mold.supplierForToolMaker"
                          :placeholder="resources['supplier']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.supplierForToolMaker == '' ||
                              mold.supplierForToolMaker == null,
                          }"
                          :required="isRequiredField('supplierCompanyName')"
                        />
                      </div>
                      <div v-if="!isToolMaker" class="col-md-5">
                        <input
                          type="text"
                          id="supplier"
                          v-model="mold.supplierCompanyName"
                          :placeholder="resources['supplier']"
                          :key="companyKey"
                          readonly="readonly"
                          class="form-control"
                          :required="isRequiredField('supplierCompanyName')"
                        />
                      </div>
                      <div v-if="!isToolMaker" class="col-md-4">
                        <button
                          type="button"
                          class="btn btn-outline-success"
                          @click.prevent="findCompany('SUPPLIER')"
                          v-text="resources['supplier_search']"
                        ></button>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['labour']"
                    >
                      <label class="col-md-3 col-form-label" for="labour">
                        <span v-text="resources['required_labor']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('labour')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          step="1"
                          id="labour"
                          v-model="mold.labour"
                          :placeholder="resources['required_labor']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.labour == '' || mold.labour == null,
                          }"
                          :required="isRequiredField('labour')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['shiftsPerDay']"
                    >
                      <label class="col-md-3 col-form-label" for="shiftsPerDay">
                        <span
                          v-text="resources['production_hour_per_day']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('shiftsPerDay')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="shiftsPerDay"
                          v-model="mold.shiftsPerDay"
                          :placeholder="resources['production_hour_per_day']"
                          step="any"
                          min="1"
                          max="24"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.shiftsPerDay == '' ||
                              mold.shiftsPerDay == null,
                          }"
                          :required="isRequiredField('shiftsPerDay')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['productionDays']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="productionDays"
                      >
                        <span
                          v-text="resources['production_day_per_week']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('productionDays')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="productionDays"
                          min="1"
                          max="7"
                          :placeholder="resources['production_day_per_week']"
                          v-model="mold.productionDays"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.productionDays == '' ||
                              mold.productionDays == null,
                          }"
                          :required="isRequiredField('productionDays')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['maxCapacityPerWeek']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="productionDays"
                      >
                        <span
                          v-text="resources['maximum_capacity_per_week']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('maxCapacityPerWeek')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          disabled
                          type="text"
                          v-model="mold.maxCapacityPerWeek"
                          :placeholder="resources['maximum_capacity_per_week']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.maxCapacityPerWeek == '' ||
                              mold.maxCapacityPerWeek == null,
                          }"
                          :required="isRequiredField('maxCapacityPerWeek')"
                        />
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'SUPPLIER'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Product Information -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong
                      ><span v-text="resources['production_information']"></span
                    ></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['approvedCycleTime']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="contracted-cycle-time"
                        v-text="resources['approved_cycle_time_form_second']"
                      ></label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="contracted-cycle-time"
                          min="1"
                          max="99999999"
                          oninput="Common.maxLength(this, 8)"
                          :placeholder="
                            resources['approved_cycle_time_form_second']
                          "
                          v-model="mold.approvedCycleTime"
                          step="any"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.approvedCycleTime == '' ||
                              mold.approvedCycleTime == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['cycleTimeLimit1']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="cycle-time-tolerance-l1"
                        v-text="resources['cycle_time_tolerance_l1']"
                      ></label>
                      <div class="col-md-6">
                        <input
                          type="number"
                          id="cycle-time-tolerance-l1"
                          min="1"
                          max="99"
                          step="any"
                          oninput="Common.maxLength(this, 2)"
                          :placeholder="resources['cycle_time_tolerance_l1']"
                          v-model="mold.cycleTimeLimit1"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.cycleTimeLimit1 == '' ||
                              mold.cycleTimeLimit1 == null,
                          }"
                          required
                        />
                      </div>
                      <div class="col-md-3">
                        <select
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.cycleTimeLimit1Unit == '' ||
                              mold.cycleTimeLimit1Unit == null,
                          }"
                          v-model="mold.cycleTimeLimit1Unit"
                        >
                          <option disabled selected hidden :value="null">
                            <span v-text="resources['unit']"></span>
                          </option>
                          <template
                            v-for="(code, index) in codes.OutsideUnit"
                            :key="index"
                          >
                            <option :value="code.code">
                              {{ code.title === "s" ? "second" : code.title }}
                            </option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['cycleTimeLimit2']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="cycle-time-tolerance-l2"
                      >
                        <span
                          v-text="resources['cycle_time_tolerance_l2']"
                        ></span>
                      </label>
                      <div class="col-md-6">
                        <input
                          type="number"
                          id="cycle-time-tolerance-l2"
                          min="1"
                          max="99"
                          step="any"
                          oninput="Common.maxLength(this, 2)"
                          :placeholder="resources['cycle_time_tolerance_l2']"
                          v-model="mold.cycleTimeLimit2"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.cycleTimeLimit2 == '' ||
                              mold.cycleTimeLimit2 == null,
                          }"
                          required
                        />
                      </div>
                      <div class="col-md-3">
                        <select
                          class="form-control"
                          v-model="mold.cycleTimeLimit2Unit"
                          :class="{
                            'form-control-warning':
                              mold.cycleTimeLimit2Unit == '' ||
                              mold.cycleTimeLimit2Unit == null,
                          }"
                        >
                          <option disabled selected hidden :value="null">
                            <span v-text="resources['unit']"></span>
                          </option>
                          <template
                            v-for="(code, index) in codes.OutsideUnit"
                            :key="index"
                          >
                            <option :value="code.code">
                              {{ code.title === "s" ? "second" : code.title }}
                            </option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['uptimeTarget']"
                    >
                      <label class="col-md-3 col-form-label" for="uptimeTarget">
                        <span
                          v-text="resources['uptime_target'] + ' (%)'"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('uptimeTarget')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          v-if="isUptimeTargetRequired"
                          disabled
                          type="number"
                          id="uptimeTarget"
                          min="1"
                          max="99"
                          oninput="Common.maxLength(this, 2)"
                          v-model="mold.uptimeTarget"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.uptimeTarget == '' ||
                              mold.uptimeTarget == null,
                          }"
                          placeholder="Uptime Target (%)"
                          required
                        />
                        <input
                          v-else
                          type="number"
                          id="uptimeTarget"
                          min="1"
                          max="99"
                          oninput="Common.maxLength(this, 2)"
                          v-model="mold.uptimeTarget"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.uptimeTarget == '' ||
                              mold.uptimeTarget == null,
                          }"
                          placeholder="Uptime Target (%)"
                          :required="isRequiredField('uptimeTarget')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['uptimeLimitL1']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="uptimeLimitL1"
                      >
                        <span v-text="resources['uptime_tolerance1']"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="uptimeLimitL1"
                          min="1"
                          max="99"
                          step="any"
                          :placeholder="resources['uptime_tolerance1']"
                          oninput="Common.maxLength(this, 2)"
                          v-model="mold.uptimeLimitL1"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.uptimeLimitL1 == '' ||
                              mold.uptimeLimitL1 == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['uptimeLimitL2']"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        for="uptimeLimitL2"
                      >
                        <span v-text="resources['uptime_tolerance2']"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="uptimeLimitL2"
                          min="1"
                          max="99"
                          step="any"
                          :placeholder="resources['uptime_tolerance2']"
                          oninput="Common.maxLength(this, 2)"
                          v-model="mold.uptimeLimitL2"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.uptimeLimitL2 == '' ||
                              mold.uptimeLimitL2 == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'PRODUCTION'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Maintenance Information -->
            <div class="row">
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['maintenance_info']"></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label
                        class="col-md-3 col-form-label"
                        for="prevent-cycle"
                        v-text="resources['maintenance_interval']"
                      ></label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="prevent-cycle"
                          min="1"
                          max="99999999"
                          :placeholder="resources['maintenance_interval']"
                          oninput="Common.maxLength(this, 8)"
                          v-model="mold.preventCycle"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.preventCycle == '' ||
                              mold.preventCycle == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-3 col-form-label"
                        for="prevent-upcoming"
                        v-text="resources['upcoming_maintenance_tolerance']"
                      ></label>
                      <div class="col-md-9">
                        <input
                          type="number"
                          id="prevent-upcoming"
                          min="1"
                          max="99999999"
                          :placeholder="
                            resources['upcoming_maintenance_tolerance']
                          "
                          oninput="Common.maxLength(this, 8)"
                          v-model="mold.preventUpcoming"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              mold.preventUpcoming == '' ||
                              mold.preventUpcoming == null,
                          }"
                          required
                        />
                      </div>
                    </div>

                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['documentFiles']"
                    >
                      <label class="col-md-3 col-form-label"
                        ><span
                          v-text="resources['maintenance_document']"
                        ></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('documentFiles')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <div class="op-upload-button-wrap">
                          <button
                            class="btn btn-outline-success"
                            v-text="resources['maintenance_document']"
                          ></button>
                          <input
                            type="file"
                            id="file"
                            @change="selectedFile"
                            multiple
                            :required="isRequiredField('documentFiles')"
                          />
                        </div>
                        <div>
                          <button
                            v-for="(file, index) in files"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="deleteFile(file, index)"
                            :key="index"
                          >
                            {{ file.name }}
                          </button>
                        </div>
                        <div class="mt-1">
                          <button
                            v-for="(file, index) in documentFiles"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="
                              deleteFileStorage(
                                file,
                                fileTypes.MOLD_MAINTENANCE_DOCUMENT
                              )
                            "
                            :key="index"
                          >
                            {{ file.fileName }}
                          </button>
                        </div>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeleteTooling['instructionVideo']"
                    >
                      <label class="col-md-3 col-form-label"
                        ><span v-text="resources['instruction_video']"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('instructionVideo')"
                        ></span>
                      </label>
                      <div class="col-md-9">
                        <div class="op-upload-button-wrap">
                          <button
                            class="btn btn-outline-success"
                            v-text="resources['upload_video']"
                          ></button>
                          <input
                            type="file"
                            id="files2"
                            @change="selectedSecondFiles"
                            multiple
                            accept="video/*"
                            :required="isRequiredField('instructionVideo')"
                          />
                        </div>
                        <div>
                          <button
                            v-for="(file, index) in secondFiles"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="deleteSecondFiles(index)"
                            :key="index"
                          >
                            {{ file.name }}
                          </button>
                        </div>
                        <div class="mt-1">
                          <button
                            v-for="(file, index) in toolingPictureFile"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="
                              deleteFileStorage(
                                file,
                                fileTypes.MOLD_INSTRUCTION_VIDEO
                              )
                            "
                            :key="index"
                          >
                            {{ file.fileName }}
                          </button>
                        </div>
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in mappingListConfigCustomField(
                        'MAINTENANCE'
                      )"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-3 col-form-label"
                        :for="'customFieldList' + item.id"
                      >
                        {{ item.fieldName }} <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          :id="'customFieldList' + item.id"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- Part -->
            <div
              class="row"
              v-for="(part, index) in mold.parts"
              :id="part.id"
              :key="index"
            >
              <div class="col-md-12">
                <div class="card card-accent-warning">
                  <div class="card-header">
                    <strong v-text="resources['parts']"></strong>
                    <strong>{{ index + 1 }}</strong>
                    <div class="card-header-actions">
                      <a
                        href="#"
                        class="card-header-action btn-setting"
                        @click.prevent="addPart()"
                      >
                        <i class="fa fa-plus"></i>
                      </a>
                      <a
                        href="#"
                        class="card-header-action btn-setting"
                        @click.prevent="removePart(index)"
                      >
                        <i class="fa fa-minus"></i>
                      </a>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label class="col-md-3 col-form-label" for="part">
                        <span v-text="resources['part_id']"></span>
                        <span class="badge-require"></span
                      ></label>
                      <div class="col-md-7">
                        <input type="hidden" v-model="part.partId" />
                        <input
                          type="text"
                          id="part"
                          v-model="part.partId"
                          :placeholder="resources['part_id_form']"
                          readonly="readonly"
                          class="form-control"
                          required
                        />
                      </div>
                      <div class="col-md-2">
                        <button
                          type="button"
                          class="btn btn-outline-success btn-block"
                          @click.prevent="findPart(index)"
                          v-text="resources['part_search']"
                        ></button>
                      </div>
                    </div>
                    <div class="form-group row">
                      <label class="col-md-3 col-form-label" for="cavity">
                        <span v-text="resources['working_cavities']"></span> /
                        <span
                          v-text="resources['total_number_of_cavities']"
                        ></span>
                        <span class="badge-require"></span>
                      </label>
                      <div class="col-md-5" style="padding-right: 0px">
                        <div class="row">
                          <div class="col-md-11" style="padding-right: 0px">
                            <input
                              type="number"
                              id="cavity"
                              min="0"
                              max="99"
                              v-on:input="autoMaxCapacityPerWeek"
                              oninput="Common.maxLength(this, 2)"
                              :placeholder="resources['working_cavities']"
                              v-model="part.cavity"
                              class="form-control"
                              :class="{
                                'form-control-warning':
                                  part.cavity == '' || part.cavity == null,
                              }"
                              required
                            />
                          </div>
                          <div class="col-md-1" style="padding-right: 0px">
                            <span
                              style="
                                font-size: 2.1rem;
                                line-height: 0.9;
                                font-weight: 300;
                              "
                              >/</span
                            >
                          </div>
                        </div>
                      </div>
                      <div class="col-md-4">
                        <input
                          type="number"
                          id="totalCavities"
                          :min="part.cavity"
                          max="99"
                          :placeholder="resources['total_cavities']"
                          oninput="Common.maxLength(this, 2)"
                          v-model="part.totalCavities"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.totalCavities == '' ||
                              part.totalCavities == null,
                          }"
                          required
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="
                mold.dataSubmission === 'DISAPPROVED' &&
                isDisapprovedReasonRequired
              "
              class="row"
            >
              <div class="col-md-12">
                <div class="card">
                  <div class="card-header">
                    <strong></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label class="col-md-3 col-form-label" for="designed-shot"
                        ><span></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="part"
                          v-model="reasonData.approvedBy"
                          readonly="readonly"
                          class="form-control"
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-3 col-form-label"
                        for="designed-shot"
                      >
                        <span></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="part"
                          v-model="reasonData.approvedAt"
                          readonly="readonly"
                          class="form-control"
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-3 col-form-label"
                        for="designed-shot"
                      >
                        <span></span>
                      </label>
                      <div class="col-md-9">
                        <input
                          type="text"
                          id="part"
                          v-model="reasonData.reason"
                          readonly="readonly"
                          class="form-control"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="row">
              <div class="col-lg-12">
                <div class="card">
                  <div class="card-body text-center">
                    <button
                      type="submit"
                      class="btn btn-primary"
                      v-text="resources['save_changes']"
                    ></button>
                    <button
                      type="button"
                      class="btn btn-default"
                      @click="dimissModal()"
                      v-text="resources['cancel']"
                    ></button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
    <part-search
      :id="'op-part-search-mold-modal'"
      :resources="resources"
      :parent-name="compName"
      @setpart="callbackPart"
    ></part-search>
    <location-search
      :id="'op-location-search-mold-modal'"
      :resources="resources"
      :parent-name="compName"
      @setlocation="callbackLocation"
    ></location-search>
    <company-search
      :id="'op-company-search-mold-modal'"
      :resources="resources"
      :parent-name="compName"
      @setcompany="callbackCompany"
    ></company-search>
  </div>
</template>

<script>
module.exports = {
  props: {
    showFilePreviewer: Function,
    resources: Object,
    configDeleteField: {
      default: () => ({}),
      type: Object,
    },
    updated: Function,
  },
  components: {
    "part-search": httpVueLoader("/components/part-search.vue"),
    "location-search": httpVueLoader("/components/location-search.vue"),
    "company-search": httpVueLoader("/components/company-search.vue"),
    "engineer-charge-dropdown": httpVueLoader(
      "/components/engineer-charge-dropdown.vue"
    ),
  },
  data() {
    return {
      isToolMaker: false,
      // serverName: '',
      userType: "",
      customFieldList: null,
      moldCorrective: {
        id: "",
        moldId: "",
        failureTimeText: "",
        repairDateTime: "",
        cost: "",
        currencyType: "USD",
        memo: "",
        mold: {
          equipmentCode: "",
        },
      },
      mold: {
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
        scrapRate: "",

        toolingCondition: null,
        preventCycle: "50000",
        preventUpcoming: "",
        preventOverdue: "",
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
      },
      partValueInit: {
        cavity: "",
        totalCavities: "",
      },
      files: [],
      selectedFiles: [],

      secondFiles: [],
      thirdFiles: [],

      //edit
      toolingConditionFiles: [],
      toolingPictureFile: [],
      documentFiles: [],

      roles: [],
      codes: {},
      engineers: [],
      size: "default",
      engineerIndex: [],
      checkedList: [],

      reasonData: {},
      requiredFields: [],
      isCreate: false,
      configCategory: "TOOLING",
      fileTypes: {
        MOLD_CONDITION: "MOLD_CONDITION",
        MOLD_INSTRUCTION_VIDEO: "MOLD_INSTRUCTION_VIDEO",
        MOLD_MAINTENANCE_DOCUMENT: "MOLD_MAINTENANCE_DOCUMENT",
      },
      checkDeleteTooling: {},
      compName: "modal",
      locationKey: 0,
      companyKey: 0,
      isTable: "",
      options: {},
    };
  },
  methods: {
    mappingListConfigCustomField(category) {
      if (this.customFieldList) {
        let _category = "TOOLING_" + category;
        return this.customFieldList.filter(
          (item) => item.propertyGroup === _category
        );
      }
    },
    isRequiredField(field) {
      if (
        [
          "toolMakerCompanyName",
          "supplierCompanyName",
          "uptimeTarget",
        ].includes(field)
      ) {
        return true;
      }
      return this.requiredFields.includes(field);
    },
    handleChange(value, options) {
      //engineer
      this.engineerIndex = value;
      this.mold.engineerIds = value;
      this.checkedList = [];
      value.map((id) => {
        this.engineers.map((item) => {
          if (id === item.id) {
            this.checkedList.push(item);
          }
        });
      });
      this.engineerIndex = [...value];
    },

    submit: async function () {
      console.log("this.mold: ", this.mold);
      if (Number(this.mold.preventCycle) <= Number(this.mold.preventUpcoming)) {
        return Common.alert(
          "Maintenance tolerance must not be larger or equal to maintenance interval"
        );
      }
      //by order
      if (!this.mold.locationId) {
        Common.alert("Please select location.");
        return;
      }
      if (!this.mold.toolMakerCompanyId) {
        Common.alert("Please select Toolmaker.");
        return;
      }

      if (!this.isToolMaker && !this.mold.supplierCompanyId) {
        Common.alert("Please select supplier.");
        return;
      }

      if (this.mold.parts.length == 0) {
        Common.alert("Please select part.");
        return;
      }

      if (
        this.requiredFields.includes("engineers") &&
        (this.mold.engineerIds == null || this.mold.engineerIds.length == 0)
      ) {
        Common.alert("Please select engineer.");
        return;
      }

      for (var i = 0; i < this.mold.parts.length; i++) {
        if (this.mold.parts[i].partId == "") {
          Common.alert("Please select part.");
          return;
        }
      }

      // if (this.userType == 'OEM' && this.mold.authorities.length == 0) {
      //     Common.alert('Please check access group');
      //     return;
      // }

      // 등록 처리용 part 정보 설정.
      this.mold.moldParts = this.mold.parts;
      console.log("this.mold.moldParts", this.mold.moldParts);
      const { sizeWidth, sizeHeight, sizeDepth, WLH } = this.mold;
      // this.mold.size = WLH
      this.mold.size = `${sizeWidth}x${sizeDepth}x${sizeHeight}`;

      console.log(this.mold.uptimeTarget);
      if (false && Page.IS_NEW) {
        this.create();
      } else {
        this.updateCustomField();
        this.update();
      }
    },

    create: function () {
      const arr = this.customFieldList.map((item) => ({
        id: item.id,
        customFieldValueDTOList: [
          {
            value: item.defaultInputValue,
          },
        ],
      }));
      const params = {
        customFieldDTOList: arr,
      };
      axios
        // .post(Page.API_POST, this.formData(), this.multipartHeader())
        .post(
          `/api/custom-field-value/edit-list/${this.mold.id}`,
          this.formData(),
          this.multipartHeader()
        )
        .then(function (response) {
          console.log(`First Post`, response.data);
          const objectId = response.data.id;
          axios
            .post(`/api/custom-field-value/edit-list/${this.mold.id}`, params)
            .then(() => {
              Common.alertCallback = function () {
                if (typeof Page !== "undefined") location.href = Page.LIST_PAGE;
              };
              Common.alert("success");
            })
            .catch((e) => {
              console.log(e);
            });
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },
    update: function () {
      this.children = null;
      //axios.put(Page.API_PUT, this.mold).then(function(response) {
      let self = this;
      axios
        // .put(Page.API_PUT, this.formData(), this.multipartHeader())
        .put(
          `/api/molds/${this.mold.id}`,
          this.formData(),
          this.multipartHeader()
        )
        .then(function (response) {
          console.log(response.data);
          // if (response.data.success) {

          if (self.isTable == "table") {
            Common.alertCallback = function () {
              // location.href = Page.LIST_PAGE;
              self.dimissModal();
            };
            Common.alert("success");
          } else {
            self.dimissModal();
          }
          // } else {
          //     Common.alert(response.data.message);
          // }
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },
    updateCustomField() {
      var objectId = this.mold.id;
      const arr = this.customFieldList.map((item) => ({
        id: item.id,
        customFieldValueDTOList: [
          {
            value: item.defaultInputValue,
          },
        ],
      }));
      const params = {
        customFieldDTOList: arr,
      };
      axios
        .post(`/api/custom-field-value/edit-list/${objectId}`, params)
        .then(() => {});
    },

    formData: function () {
      var formData = new FormData();
      for (var i = 0; i < this.files.length; i++) {
        let file = this.files[i];
        formData.append("files", file);
      }
      for (var i = 0; i < this.secondFiles.length; i++) {
        let file = this.secondFiles[i];
        formData.append("secondFiles", file);
      }
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      const body = {
        ...this.mold,
        poDate: this.mold.poDateFormat ? this.mold.poDateFormat : null,
      };
      formData.append("payload", JSON.stringify(body));
      return formData;
    },

    findPart: function (index) {
      var child = Common.vue.getChild(this.$children, "part-search");
      if (child != null) {
        child.findPart(index);
      }
    },

    addPart: function () {
      var part = {
        moldId: "",
        partId: "",
        partName: "",
        cavity: this.partValueInit.cavity || "",
        totalCavities: this.partValueInit.totalCavities || "",
        weight: 1,
      };
      this.mold.parts.push(part);
      //this.mold.parts.push(part);
    },
    removePart: function (index) {
      if (this.mold.parts.length > 1) {
        this.mold.parts.splice(index, 1);
        //this.mold.parts.splice(index, 1);
      }
    },

    cancel: function () {
      // location.href = Page.LIST_PAGE;
    },

    findLocation: function () {
      var child = Common.vue.getChild(this.$children, "location-search");
      if (child != null) {
        child.tab("active");
      }
    },

    callbackLocation(location) {
      console.log(location);
      this.mold.locationId = location.id;
      this.mold.locationName = location.name;
      console.log(`${this.mold.locationId} - ${this.mold.locationName}`);
      this.locationKey++;
    },
    callbackPart: function (passData) {
      console.log(passData);
      let index = passData.index;
      let part = passData.part;
      for (var i = 0; i < this.mold.parts.length; i++) {
        if (i == index) {
          continue;
        }

        if (this.mold.parts[i].partId == part.id) {
          Common.alert("This part has already been added.");
          return false;
        }
      }

      //this.mold.moldParts[index].partId = part.id;
      //this.mold.moldParts[index].partName= part.name;

      this.mold.parts[index].partId = part.id;
      this.mold.parts[index].partName = part.name;
      this.mold.parts[index].weight = part.weight;
      this.mold.parts[index].partWeight = part.partWeight;
      this.mold.parts[index].nameShow = part.name
        ? `${part.partCode} (${part.name})`
        : part.partCode;
      this.autoShotSize();
      console.log("auto shot size parts", part);
      this.autoMaxCapacityPerWeek();
      return true;
    },
    callbackCounter: function (counter) {
      this.mold.counterId = counter.id;
      this.mold.counterCode = counter.equipmentCode;
    },

    findCompany: function (searchType) {
      var child = Common.vue.getChild(this.$children, "company-search");
      if (child != null) {
        child.findCompany(searchType);
      }
    },
    callbackCompany: function (passData) {
      let company = passData.company;
      let searchType = passData.searchType;
      if ("TOOL_MAKER" == searchType) {
        this.mold.toolMakerCompanyId = company.id;
        this.mold.toolMakerCompanyName = company.name;
      } else if ("SUPPLIER" == searchType) {
        this.mold.supplierCompanyId = company.id;
        this.mold.supplierCompanyName = company.name;
      }
      this.companyKey++;
    },

    //second
    deleteSecondFiles: function (index) {
      this.secondFiles = this.secondFiles.filter((value, idx) => idx !== index);
    },

    selectedSecondFiles: function (e) {
      var files = e.target.files;
      if (files) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.secondFiles.push(selectedFiles[i]);
        }
      }
    },

    //thirdFiles
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      if (files) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },

    selectedFile: function (e) {
      var files = e.target.files;

      if (files) {
        var selectedFiles = Array.from(files);

        for (var i = 0; i < selectedFiles.length; i++) {
          this.files.push(selectedFiles[i]);
        }
      }
    },
    deleteFile: function (f, index) {
      var newFiles = [];
      for (var i = 0; i < this.files.length; i++) {
        var file = this.files[i];
        if (i != index) {
          console.log("splice: " + i + " : " + (i + 1));
          //this.files.splice(i, 1);

          newFiles.push(file);
        }
      }

      this.files = newFiles;
    },
    deleteFileStorage: function (file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case this.fileTypes.MOLD_CONDITION:
              this.toolingConditionFiles = response.data;
              break;
            case this.fileTypes.MOLD_MAINTENANCE_DOCUMENT:
              this.documentFiles = response.data;
              break;
            case this.fileTypes.MOLD_INSTRUCTION_VIDEO:
              this.toolingPictureFile = response.data;
              break;
          }
        });
    },

    autoMaxCapacityPerWeek: function () {
      const {
        productionDays,
        contractedCycleTime,
        parts: partsMold,
        shiftsPerDay,
        uptimeTarget,
      } = this.mold;
      let parts = partsMold || [];
      let cavity =
        parts.length > 0 && parts[0].cavity ? Number(parts[0].cavity) : 1;
      parts.forEach((value, index) => {
        if (!isNaN(value.cavity) && index > 0) {
          cavity += Number(value.cavity);
        }
      });
      console.log("cavity", cavity);
      this.autoShotSize();
      console.log(
        !isNaN(productionDays),
        !isNaN(shiftsPerDay),
        !isNaN(cavity),
        !isNaN(contractedCycleTime),
        !isNaN(uptimeTarget)
      );

      if (
        productionDays &&
        !isNaN(productionDays) &&
        shiftsPerDay &&
        !isNaN(shiftsPerDay) &&
        cavity &&
        !isNaN(cavity) &&
        contractedCycleTime &&
        !isNaN(contractedCycleTime) &&
        uptimeTarget &&
        !isNaN(uptimeTarget)
      )
        this.mold.maxCapacityPerWeek =
          ((Number(productionDays) *
            Number(shiftsPerDay) *
            3600 *
            (Number(uptimeTarget) / 100)) /
            (Number(contractedCycleTime) / 10)) *
          Number(cavity);
      this.mold.maxCapacityPerWeek = Math.ceil(
        Number(this.mold.maxCapacityPerWeek)
      );
    },

    autoShotSize: function () {
      const { weightRunner, parts: partsMold } = this.mold;
      let parts = partsMold || [];

      let totalPartWeightCaviteis = 0;
      console.log("parts", parts);

      let sumCavities = 0;
      parts.forEach((part) => {
        if (part.partWeight) {
          let partWeightPart = part.partWeight.split(" ");

          if (partWeightPart.length === 2) {
            let number = Number(partWeightPart[0]);
            let unit = partWeightPart[1];
            totalPartWeightCaviteis +=
              this.convertWeightUnitToGram(unit, number) * part.cavity;
          }
        }
        if (!isNaN(part.cavity)) {
          sumCavities += part.cavity;
        }
      });
      console.log("weightRunner", weightRunner);
      let shotWeight = null;
      if (
        weightRunner != null &&
        !isNaN(weightRunner) &&
        String(weightRunner).trim() != ""
      ) {
        shotWeight =
          totalPartWeightCaviteis + Number(weightRunner) * sumCavities;
      }
      console.log("shotWeight", shotWeight);
      this.mold.shotSize = shotWeight;
    },

    convertWeightUnitToGram(unit, value) {
      switch (unit.toLowerCase()) {
        case "ounce":
          return value * 28.3495;
        case "pound":
          return value * 453.592;
        case "kg":
          return value * 1000;
        case "gram":
          return value;
        default:
          return value;
      }
    },

    initDefaultValue() {
      const fields = [
        "equipmentCode",
        "toolingLetter",
        "toolingType",
        "toolingComplexity",
        "designedShot",
        "lifeYears",
        "madeYear",
        "approvedCycleTime",
        "locationId",
        "toolDescription",
        "size",
        "weight",
        "shotSize",
        "toolMakerCompanyName",
        "injectionMachineId",
        "quotedMachineTonnage",
        "runnerType",
        "runnerMaker",
        "weightRunner",
        "hotRunnerDrop",
        "hotRunnerZone",
        "toolingPictureFile",
        "preventCycle",
        "preventUpcoming",
        "preventOverdue",
        "cycleTimeLimit1",
        "cycleTimeLimit2",
        "useageType",
        "engineers",
        "documentFiles",
        "instructionVideo",
        "cost",
        "memo",
        "supplierCompanyName",
        "uptimeTarget",
        "uptimeLimitL1",
        "uptimeLimitL2",
        "labour",
        "shiftsPerDay",
        "productionDays",
        "maxCapacityPerWeek",
        "partId",
        "cavity",
        "totalCavities",
      ];
      // get current config value
      axios
        .get("/api/config?configCategory=" + this.configCategory)
        .then((response) => {
          if (response.data && response.data.length > 0) {
            // console.log('form response data', this.categories);
            console.log("form response data", response.data);
            response.data.forEach((field) => {
              if (field.required) {
                this.requiredFields.push(field.fieldName);
              }
              if (this.checkDeleteTooling != null)
                this.checkDeleteTooling[field.fieldName] = field.deletedField;
            });

            fields.forEach((field) => {
              let defaultField = response.data.filter(
                (item) => item.fieldName === field
              )[0];
              // console.log("defaultField", defaultField);
              if (!defaultField) return;
              if (defaultField.defaultInput && defaultField.defaultInputValue) {
                if (defaultField.fieldName === "cavity") {
                  this.partValueInit.cavity = defaultField.defaultInputValue;
                } else if (defaultField.fieldName === "totalCavities") {
                  this.partValueInit.totalCavities =
                    defaultField.defaultInputValue;
                }
              }
            });
            /*
                        }
            */
            if (
              this.checkDeleteTooling != null &&
              Object.entries(this.checkDeleteTooling).length > 0
            ) {
              setTimeout(function () {
                Common.initRequireBadge();
              }, 100);
            }
          }
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    accumulatedMaintenanceCostView(mold) {
      mold.accumulatedMaintenanceCost = mold.accumulatedMaintenanceCost || 0;
      let currencyTitle =
        mold.costCurrencyTypeTitle != null ? mold.costCurrencyTypeTitle : "$";
      if (this.codes) {
        this.codes.CurrencyType.forEach((code) => {
          if (code.code == mold.costCurrencyType) currencyTitle = code.title;
        });
      }
      mold.accumulatedMaintenanceCostView =
        currencyTitle + mold.accumulatedMaintenanceCost;
      return mold.accumulatedMaintenanceCostView;
    },
    showMoldDetails: async function (mold, page, func) {
      this.isTable = page;
      this.thirdFiles = [];
      this.secondFiles = [];
      this.files = [];
      console.log("mold: ", mold);
      this.mold = mold;
      this.func = func;
      // this.serverName = await Common.getSystem('server')

      this.findMold();
      //this.findDocumentFiles();
      // this.onMountedRun();
      this.instructionVideoFiles();
      this.getListConfigCustomField();
    },
    findMold() {
      var self = this;
      axios.get("/api/molds/" + self.mold.id).then(function (response) {
        response.data.approvedCycleTime =
          response.data.contractedCycleTimeSeconds;

        self.mold = response.data;
        if (self.mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(
              `/api/molds/data-submission/${self.mold.id}/disapproval-reason`
            )
            .then(function (response) {
              console.log("response: reson: ", response);
              self.reasonData = response.data;
              self.reasonData.approvedAt = self.reasonData.approvedAt
                ? moment
                    .unix(self.reasonData.approvedAt)
                    .format("MMMM DD YYYY HH:mm:ss")
                : "-";
            });
        }
        console.log("response.data: ", response.data);
        // if (self.mold.useageType == null) {
        //     self.mold.useageType = 'TO_MARKET'
        // }
        if (response.data.size) {
          const sizes = response.data.size.split("x");
          if (sizes.length >= 3) {
            self.mold.sizeWidth = sizes[0].trim();
            self.mold.sizeDepth = sizes[1].trim();
            self.mold.sizeHeight = sizes[2].trim();
          }
          // self.mold.WLH = response.data.size
        }
        self.mold.costCurrencyType = self.mold.costCurrencyType || "USD";
        self.mold.accumulatedMaintenanceCost =
          self.mold.accumulatedMaintenanceCost || 0;

        if (response.data.engineers && response.data.engineers.length > 0) {
          self.engineerIndex = response.data.engineers.map((value) => value.id);
          self.mold.engineerIds = response.data.engineers.map(
            (value) => value.id
          );
          self.engineerIndex.map((id) => {
            response.data.engineers.map((value) => {
              if (id === value.id) {
                self.checkedList.push(value);
              }
            });
          });
        } else {
          self.engineerIndex = [];
          self.mold.engineerIds = [];
        }

        //uptimeTarget
        if (self.mold.uptimeTarget == null) {
          self.mold.uptimeTarget = 90;
        }

        //uptimeLimitL1
        if (self.mold.uptimeLimitL1 == null) {
          self.mold.uptimeLimitL1 = 5;
        }
        if (self.mold.uptimeLimitL2 == null) {
          self.mold.uptimeLimitL2 = 10;
        }

        var authorities = [];
        for (var i = 0; i < response.data.moldAuthorities.length; i++) {
          var moldAuthority = response.data.moldAuthorities[i];
          authorities.push(moldAuthority.authority);
        }
        self.mold.authorities = authorities;

        if (self.mold.counter != null) {
          self.mold.counterId = self.mold.counter.id;
          self.mold.counterCode = self.mold.counter.equipmentCode;
        }

        if (self.mold.part != null) {
          self.mold.partId = self.mold.part.id;
          self.mold.partName = self.mold.part.name;
        }

        if (self.mold.location != null) {
          self.mold.locationId = self.mold.location.id;
          self.mold.locationName = self.mold.location.name;
        }
        //self.mold.moldParts = self.mold.parts;
        console.log("self.mold.parts", self.mold.parts);
        if (self.mold.parts.length > 0) {
          self.mold.parts = self.mold.parts.map((value) => {
            value.nameShow = value.partName
              ? `${value.partCode} (${value.partName})`
              : value.partCode;
            return value;
          });
        } else {
          self.mold.parts = [
            {
              moldId: "",
              partId: "",
              partName: "",
              cavity: "",
              totalCavities: "",
            },
          ];
        }
        console.log("self.mold.parts after update", self.mold.parts);
        if (self.mold.upperLimitTemperatureUnit == null) {
          self.mold.upperLimitTemperatureUnit = "CELSIUS";
        }
        if (self.mold.lowerLimitTemperatureUnit == null) {
          self.mold.lowerLimitTemperatureUnit = "CELSIUS";
        }

        var param = {
          storageTypes:
            "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION",
          refId: self.mold.id,
        };
        //var param = Common.param(self.requestParam);
        axios
          .get("/api/file-storage/mold?" + Common.param(param))
          .then(function (response) {
            console.log("response /api/file-storage/mold : ", response);
            self.documentFiles = response.data["MOLD_MAINTENANCE_DOCUMENT"]
              ? response.data["MOLD_MAINTENANCE_DOCUMENT"]
              : [];
            self.toolingConditionFiles = response.data["MOLD_CONDITION"]
              ? response.data["MOLD_CONDITION"]
              : [];
            self.toolingPictureFile = response.data["MOLD_INSTRUCTION_VIDEO"]
              ? response.data["MOLD_INSTRUCTION_VIDEO"]
              : [];
            //  toolingConditionFiles: [],
            //     toolingPictureFile: [],
            //         'documentFiles': [],
          });
        console.log("response:mold ", response);
        console.log("findMold form");
        console.log($("#op-mold-details-form"));
        $(self.getRootId() + "#op-mold-details-form").modal("show");
        console.log($("#op-mold-details-form"));
        self.$forceUpdate();
      });
    },
    findMoldParts: function () {
      // Parts 조회
      var self = this;
      axios
        .get("/api/molds/" + seld.mold.id + "/parts")
        .then(function (response) {
          console.log("response:parts ", response);
          self.parts = response.data.content;
          console.log($("#op-mold-details-form"));
          $(self.getRootId() + "#op-mold-details-form").modal("show");
          console.log($("#op-mold-details-form"));
        });
    },
    getListConfigCustomField() {
      var self = this;
      console.log(`Get List Config Custom Field: `, self.mold);
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=TOOLING&objectId=${self.mold.id}`
        )
        .then(function (response) {
          console.log(response, "hey this is your response");
          self.customFieldList = response.data.map((item) => ({
            propertyGroup: item.propertyGroup,
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        })
        .finally(() => {
          console.log(self.customFieldList, "customFieldList");
        });
    },
    instructionVideoFiles: function () {
      var self = this;

      var param = {
        storageTypes:
          "MOLD_MAINTENANCE_DOCUMENT,MOLD_INSTRUCTION_VIDEO,MOLD_CONDITION",
        refId: self.mold.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then(function (response) {
          self.conditionFiles = response.data["MOLD_CONDITION"]
            ? response.data["MOLD_CONDITION"]
            : [];
          self.documentFiles = response.data["MOLD_MAINTENANCE_DOCUMENT"]
            ? response.data["MOLD_MAINTENANCE_DOCUMENT"]
            : [];
          self.instructionFiles = response.data["MOLD_INSTRUCTION_VIDEO"]
            ? response.data["MOLD_INSTRUCTION_VIDEO"]
            : [];
        });
    },
    showFile(file) {
      $(this.getRootId() + "#op-mold-details-form").modal("hide");
      this.showFilePreviewer(file);
      console.log("showFile form");
    },
    backFromPreview() {
      $(this.getRootId() + "#op-mold-details-form").modal("show");
      console.log("showFile form");
    },
    dimissModal: function () {
      console.log(this.mold);
      this.updated();
      this.closeModal();
    },
    closeModal: function () {
      this.thirdFiles = [];
      this.secondFiles = [];
      this.files = [];
      $(this.getRootId() + "#op-mold-details-form").modal("hide");
    },
    onMountedRun() {
      this.$nextTick(async function () {
        // 데이터 조회
        const self = this;

        try {
          const type = await Common.getSystem("type");
          const options = await Common.getSystem("options");
          const me = await Common.getSystem("me");
          const currentUser = JSON.parse(me);
          const resEngineers = await axios.get("/api/users/engineers");
          self.codes = await headerVm?.getCodes();
          self.engineers = resEngineers.data;
          self.engineers.map((item) => (item.title = item.name));
          self.roles = currentUser.roles;
          self.options = JSON.parse(options);

          if (type) {
            self.userType = type;
            self.isToolMaker = type == "TOOL_MAKER";
          }

          const condition = Boolean(
            this.options?.CLIENT?.moldDetail?.uptimeTargetRequired
          );
          if (condition) {
            self.mold.preventUpcoming = "10000";
            self.mold.preventOverdue = "5000";
            self.mold.cycleTimeLimit1 = "2";

            self.mold.cycleTimeLimit2 = "5";
            self.mold.uptimeLimitL1 = "3";
            self.mold.uptimeLimitL2 = "5";
            const findIndexUnit = self.codes.OutsideUnit.findIndex(
              (value) => value.code == "SECOND"
            );
            if (findIndexUnit !== -1) {
              self.mold.cycleTimeLimit2Unit =
                self.codes.OutsideUnit[findIndexUnit].code;
              self.mold.cycleTimeLimit1Unit =
                self.codes.OutsideUnit[findIndexUnit].code;
            }
          }
          if (currentUser.companyType === "TOOL_MAKER") {
            self.mold.toolMakerCompanyId = res.data.id;
            self.mold.toolMakerCompanyName = res.data.name;
          }

          // set default value
          Common.getCategoryConfigStatus().then((data) => {
            if (data && data.length > 0) {
              let currentConfig = data.filter(
                (item) => item.configCategory === self.configCategory
              )[0];
              if (currentConfig && currentConfig.enabled) {
                self.initDefaultValue();
              }
            }
          });
        } catch (error) {
          console.log(error);
        }
      });
    },
  },
  computed: {
    isToolingComplexityEnabled() {
      return Boolean(this.options?.CLIENT?.moldDetail?.toolingComplexity);
    },
    isLifeYearsEnabled() {
      console.log("mold-details-form");
      return Boolean(this.options?.CLIENT?.moldDetail?.lifeYearsEnabled);
    },
    isUptimeTargetRequired() {
      return Boolean(this.options?.CLIENT?.moldDetail?.uptimeTargetRequired);
    },
    isDisapprovedReasonRequired() {
      return Boolean(
        this.options?.CLIENT?.moldDetail?.disapprovedReasonRequired
      );
    },
  },

  watch: {
    "mold.approvedCycleTime": function (newValue) {
      console.log(`Watch approve`, newValue);
      if (newValue === "") {
        this.mold.contractedCycleTime = newValue;
      } else {
        this.mold.contractedCycleTime = Number(newValue) * 10;
      }
    },
    "mold.shiftsPerDay": function (newValue) {
      console.log("newValue: ", newValue);
      this.autoMaxCapacityPerWeek();
    },
    "mold.productionDays": function (newValue) {
      this.autoMaxCapacityPerWeek();
    },
    "mold.contractedCycleTime": function (newValue) {
      this.autoMaxCapacityPerWeek();
    },
    "mold.uptimeTarget": function () {
      this.autoMaxCapacityPerWeek();
    },
    "mold.parts": function (newValue) {
      // if (newValue.length > 1) {
      //     this.mold.familyTool = true;
      // }
      this.autoMaxCapacityPerWeek();
      console.log("parts list");
      this.autoShotSize();
    },
    "mold.toolingComplexity": function (newValue) {
      // const condition = this.serverName === 'dyson'
      const condition = this.isToolingComplexityEnabled;
      if (condition) {
        if (newValue == "A+") {
          this.mold.uptimeTarget = "80";
        } else if (newValue == "A") {
          this.mold.uptimeTarget = "80";
        } else if (newValue == "B") {
          this.mold.uptimeTarget = "85";
        } else if (newValue == "C") {
          this.mold.uptimeTarget = "90";
        } else {
          this.mold.uptimeTarget = "0";
        }
      }
    },

    "mold.weightRunner": function () {
      console.log("mold.weightRunner trigger");
      this.autoShotSize();
    },
  },
  created() {
    this.onMountedRun();
  },
};
</script>
<style>
.modal-title {
  color: #ffffff;
}

.shadow {
  outline: 0;
  border: 1px solid #5d43f7;
  box-shadow: 0 0 5px 5px rgba(93, 67, 247, 0.3) !important;
}

.form-control:focus {
  border: 1px solid #8ad4ee;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgb(32, 168, 216, 0.25);
}

.form-control-warning {
  outline: 0;
  border: 1px solid #5d43f7;
  box-shadow: 0 0 5px 5px rgba(93, 67, 247, 0.3);
}

.form-control:invalid,
.form-control:valid,
.was-validated {
  border: 1px solid #e4e7ea;
}
</style>
<style scoped>
.modal-header {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 26px 11.5px 26px;
}

.modal-header .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.modal-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 550;
}

.modal-body {
  overflow-y: auto;
  max-height: 600px;
  margin: 15px 5px;
  padding-top: 0px;
}

.close {
  opacity: 1;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background: #d6dade;
  width: 8px;
  padding-right: 8px;
}

.modal-content {
  width: 101% !important;
}
</style>
