<template>
  <div>
    <a-drawer
      :title="nameDrawerTitle"
      placement="right"
      :visible="visible"
      :class="{ hidden: !visible }"
      wrap-class-name="category-drawer"
      id="category-drawer"
      width="384"
      :body-style="{
        marginBottom: '50px',
        padding:
          nameDrawer === 'Create Data Completion Order' || !nameDrawer
            ? '0'
            : '24px',
      }"
      :wrap-class-name="
        (nameDrawer === 'Create Data Completion Order' || !nameDrawer
          ? 'create-data-completion'
          : '') + ' category-drawer'
      "
      @close="onClose"
    >
      <div
        v-if="nameDrawer === 'Create Data Completion Order'"
        class="data-completion-modal-title"
      >
        <div
          class="head-line"
          style="
            position: absolute;
            background: #52a1ff;
            height: 8px;
            border-radius: unset;
            top: 0;
            left: 0;
            width: 100%;
          "
        ></div>
        <span class="span-title">{{ nameDrawer }}</span>
        <span
          class="close-button"
          style="
            font-size: 25px;
            display: flex;
            align-items: center;
            height: 17px;
            cursor: pointer;
          "
          @click="onClose"
          aria-hidden="true"
        >
          <span class="t-icon-close"></span>
        </span>
      </div>
      <div v-else-if="!nameDrawer" class="data-completion-modal-title">
        <div
          class="head-line"
          style="
            position: absolute;
            background: #52a1ff;
            height: 8px;
            top: 0;
            left: 0;
            width: 100%;
            border-radius: 8px 0px 0px 0px;
          "
        ></div>
        <span class="span-title">{{ nameDrawerTitle }}</span>
        <span
          class="close-button"
          style="
            font-size: 25px;
            display: flex;
            align-items: center;
            height: 17px;
            cursor: pointer;
          "
          @click="onClose"
          aria-hidden="true"
        >
          <span class="t-icon-close"></span>
        </span>
      </div>

      <form
        v-if="nameDrawer === 'location'"
        style="padding-right: 10px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <template>
              <div class="create-field">
                <label
                  style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                  class="drawer-title"
                >
                  {{ resources["company"] }}
                  <span class="badge-require"></span>
                </label>
                <a-select
                  :placeholder="resources['company']"
                  style="z-index: 4001"
                  key="companySelect"
                >
                  <a-select-opt-group>
                    <span slot="label">
                      {{ resources["in_house"] }}
                    </span>
                    <a-select-option
                      v-for="(item, index) in mappingCompany('IN_HOUSE')"
                      :key="index + 'a'"
                      @click="handleChangeLocation(item)"
                    >
                      <a-tooltip placement="bottomRight">
                        <template slot="title">
                          <div style="padding: 6px">
                            {{ item.name }} - {{ item.id }}
                          </div>
                        </template>
                        <label
                          style="width: 100%; cursor: pointer"
                          class="form-check-label"
                        >
                          <input
                            type="radio"
                            name="location"
                            class="form-check-input"
                          />
                          <span> {{ item.name }} - {{ item.id }} </span>
                        </label>
                      </a-tooltip>
                    </a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group>
                    <span slot="label">
                      {{ resources["supplier"] }}
                    </span>
                    <a-select-option
                      v-for="(item, index2) in mappingCompany('SUPPLIER')"
                      :key="index2 + 'b'"
                      @click="handleChangeLocation(item)"
                    >
                      <a-tooltip placement="bottomRight">
                        <template slot="title">
                          <div style="padding: 6px">
                            {{ item.name }} - {{ item.id }}
                          </div>
                        </template>
                        <label
                          style="width: 100%; cursor: pointer"
                          class="form-check-label"
                        >
                          <input
                            type="radio"
                            name="location"
                            class="form-check-input"
                          />
                          <span> {{ item.name }} - {{ item.id }} </span>
                        </label>
                      </a-tooltip>
                    </a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group>
                    <span slot="label">
                      {{ resources["toolmaker"] }}
                    </span>
                    <a-select-option
                      v-for="(item, index3) in mappingCompany('TOOL_MAKER')"
                      :key="index3 + 'c'"
                      @click="handleChangeLocation(item)"
                    >
                      <a-tooltip placement="bottomRight">
                        <template slot="title">
                          <div style="padding: 6px">
                            {{ item.name }} - {{ item.id }}
                          </div>
                        </template>
                        <label
                          style="width: 100%; cursor: pointer"
                          class="form-check-label"
                        >
                          <input
                            type="radio"
                            name="location"
                            class="form-check-input"
                          />
                          <span> {{ item.name }} - {{ item.id }} </span>
                        </label>
                      </a-tooltip>
                    </a-select-option>
                  </a-select-opt-group>
                  <a-icon
                    style="color: #3491ff"
                    slot="suffixIcon"
                    type="caret-down"
                  />
                </a-select>
              </div>
            </template>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["location_name"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorName }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('name')"
                :placeholder="resources['location_name']"
                v-model="locationObj.locationName"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorName"
                style="font-size: 11px; color: #ef4444; text-align: left"
                v-text="resources['the_location_name_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["location_id"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorId }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('id')"
                :placeholder="resources['location_id']"
                v-model="locationObj.locationId"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorId"
                style="font-size: 11px; color: #ef4444; text-align: left"
                v-text="resources['the_location_id_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["address"] }}
                <span class="badge-require"></span>
              </label>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['address']"
                v-model="locationObj.address"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['memo']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['memo']"
                v-model="locationObj.memo"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
      </form>
      <form
        v-else-if="nameDrawer === 'toolmaker'"
        style="padding-right: 10px; margin-bottom: 100px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["company_name"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorName }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('name')"
                :placeholder="resources['company_name']"
                v-model="toolmakerObj.companyName"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorName"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['the_company_name_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["company_id"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorId }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('id')"
                :placeholder="resources['company_id']"
                v-model="toolmakerObj.companyId"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorId"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['the_company_id_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["address"] }}
              </div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['address']"
                v-model="toolmakerObj.address"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['manager']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['manager']"
                v-model="toolmakerObj.manager"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['phone_number']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['phone_number']"
                v-model="toolmakerObj.phoneNumber"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['email']"
              ></div>
              <input
                :class="{ errorFieldName: errorEmail }"
                :placeholder="resources['email']"
                ref="email-field"
                @keyup="removeAlertCreateCompany('email')"
                v-model="toolmakerObj.email"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorEmail"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['invalid_email_format']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['memo']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['memo']"
                v-model="toolmakerObj.memo"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
      </form>
      <form
        v-else-if="nameDrawer === 'supplier'"
        style="padding-right: 10px; margin-bottom: 100px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["company_name"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorName }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('name')"
                :placeholder="resources['company_name']"
                v-model="supplierObj.companyName"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorName"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['the_company_name_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["company_id"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorId }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('id')"
                :placeholder="resources['company_id']"
                v-model="supplierObj.companyId"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorId"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['the_company_id_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["address"] }}
              </div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['address']"
                v-model="supplierObj.address"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['manager']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['manager']"
                v-model="supplierObj.manager"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['phone_number']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['phone_number']"
                v-model="supplierObj.phoneNumber"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['email']"
              ></div>
              <input
                :class="{ errorFieldName: errorEmail }"
                ref="email-field"
                :placeholder="resources['email']"
                @keyup="removeAlertCreate()"
                v-model="supplierObj.email"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorEmail"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="resources['invalid_email_format']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['memo']"
              ></div>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                v-model="supplierObj.memo"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
      </form>
      <form
        v-else-if="nameDrawer === 'part'"
        style="padding-right: 10px; margin-bottom: 100px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <template>
              <div class="create-field">
                <label
                  style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                  class="drawer-title"
                >
                  {{ resources["category"] }}
                  <span class="badge-require"></span>
                </label>
                <a-select
                  style="z-index: 4001"
                  :placeholder="resources['category']"
                  key="selectPart"
                >
                  <a-select-opt-group
                    v-for="(category1, indexCategory1) in categories"
                    :key="indexCategory1 + category1.name"
                  >
                    <span slot="label">
                      {{ category1.name }}
                    </span>
                    <a-select-option
                      v-for="(category2, indexCategory2) in category1.children"
                      :disabled="!category2.enabled"
                      :key="indexCategory2 + category2.id"
                      @click="handleChangeCategory(category2)"
                    >
                      <a-tooltip placement="bottomRight">
                        <template slot="title">
                          <div style="padding: 6px">{{ category2.name }}</div>
                        </template>
                        <label
                          style="width: 100%; cursor: pointer"
                          class="form-check-label"
                        >
                          <input
                            type="radio"
                            name="part-category"
                            :disabled="!category2.enabled"
                            class="form-check-input"
                          />
                          <span>{{ category2.name }}</span>
                        </label>
                      </a-tooltip>
                    </a-select-option>
                  </a-select-opt-group>
                </a-select>
              </div>
            </template>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["part_id"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorId }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('id')"
                :placeholder="resources['part_id']"
                v-model="partObj.partCode"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorId"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="
                  resources['part_id_is_already_registered_in_the_system']
                "
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["part_name"] }}
                <span class="badge-require"></span>
              </label>
              <input
                :class="{ errorFieldName: errorName }"
                ref="added-field"
                @keyup="removeAlertCreateCompany('name')"
                :placeholder="resources['part_name']"
                v-model="partObj.name"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorName"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  line-height: 14px;
                "
                v-text="
                  resources['part_name_is_already_registered_in_the_system']
                "
              ></div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["part_resin_code"] }}
                <span
                  class="badge-require"
                  v-if="isRequiredField('resinCode')"
                ></span>
              </label>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['part_resin_code']"
                v-model="partObj.resinCode"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["part_resin_grade"] }}
                <span
                  class="badge-require"
                  v-if="isRequiredField('resinGrade')"
                ></span>
              </label>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['part_resin_grade']"
                v-model="partObj.resinGrade"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ resources["design_revision_level"] }}
                <span
                  class="badge-require"
                  v-if="isRequiredField('designRevision')"
                ></span>
              </label>
              <input
                ref="added-field"
                @keyup="removeAlertCreate()"
                :placeholder="resources['design_revision_level']"
                v-model="partObj.designRevision"
                class="form-control reason-edit-input focus"
              />
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16" class="create-field custom-option">
          <a-col :span="24">
            <label
              style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
              class="drawer-title"
            >
              {{ resources["part_volume"] + " (W x D x H)" }}
              <span class="badge-require" v-if="isRequiredField('size')"></span>
            </label>
          </a-col>
          <a-col :span="17">
            <input
              ref="added-field"
              @keyup="removeAlertCreate()"
              :placeholder="resources['part_volume'] + ' (W x D x H)'"
              v-model="partObj.size"
              class="form-control reason-edit-input focus"
            />
          </a-col>
          <a-col :span="7">
            <a-select v-model="partObj.sizeUnit" style="z-index: 4001">
              <a-select-option
                v-for="(code, indexCode) in codes.SizeUnit"
                :value="code.code"
                :key="indexCode + 'code'"
              >
                <span>
                  {{ code.title }}
                  <span
                    v-if="
                      code.code == 'MM' || code.code == 'CM' || code.code == 'M'
                    "
                    >³</span
                  >
                </span>
              </a-select-option>
            </a-select>
          </a-col>
        </a-row>
        <a-row :gutter="16" class="create-field custom-option">
          <a-col :span="24">
            <label
              style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
              class="drawer-title"
            >
              {{ resources["part_weight"] }}
              <span
                class="badge-require"
                v-if="isRequiredField('weight')"
              ></span>
            </label>
          </a-col>
          <a-col :span="17">
            <input
              style="font-size: 13px"
              type="number"
              id="weight"
              v-model="partObj.weight"
              class="form-control"
              step="any"
              :placeholder="resources['part_weight']"
            />
          </a-col>
          <a-col :span="7">
            <a-select v-model="partObj.weightUnit" style="z-index: 4001">
              <a-select-option
                v-for="(code, indexCode) in codes.WeightUnit"
                :value="code.code"
                :key="indexCode + 'code2'"
              >
                {{ code.title }}
              </a-select-option>
            </a-select>
          </a-col>
        </a-row>
        <a-row :gutter="16" class="create-field">
          <a-col :span="24">
            <label
              style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
              class="drawer-title"
            >
              {{ resources["weekly_demand"] }}
              <span
                class="badge-require"
                v-if="isRequiredField('weeklyDemand')"
              ></span>
            </label>
            <input
              ref="added-field"
              type="number"
              min="0"
              max="99999999"
              onKeyUp="if(this.value>99999999){this.value='99999999';}else if(this.value<0){this.value='0';}"
              :placeholder="resources['weekly_demand']"
              v-model="partObj.weeklyDemand"
              class="form-control reason-edit-input focus"
            />
          </a-col>
        </a-row>
        <a-row :gutter="16" class="create-field">
          <a-col :span="24">
            <label
              style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
              class="drawer-title"
            >
              {{ resources["part_picture"] }}
              <span
                class="badge-require"
                v-if="isRequiredField('partPictureFile')"
              ></span>
            </label>
          </a-col>
          <a-col :span="24">
            <div class="op-upload-button-wrap">
              <button id="partPictureFile" class="btn btn-outline-success">
                {{ resources["upload_photo"] }}
              </button>
              <input
                type="file"
                ref="partUpload"
                id="files1"
                @change="selectedThirdFiles"
                multiple
                style="height: 40px"
                accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
                :required="isRequiredField('partPictureFile', true)"
              />
            </div>
            <div>
              <button
                v-for="(file, index) in thirdFiles"
                class="btn btn-outline-dark btn-sm mr-1"
                @click.prevent="deleteThirdFiles(index)"
              >
                {{ file.name }}
              </button>
            </div>

            <div class="mt-1">
              <button
                v-for="file in partPictureFiles"
                class="btn btn-outline-dark btn-sm mr-1"
                @click.prevent="deleteFileStorage(file, fileTypes.PART_PICTURE)"
              >
                {{ file.fileName }}
              </button>
            </div>
          </a-col>
        </a-row>
        <a-row
          v-for="(item, index) in customFieldList"
          :key="index"
          :gutter="16"
        >
          <a-col :span="24">
            <div class="create-field">
              <label
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
              >
                {{ item.fieldName }}
                <span class="badge-require" v-if="item.required"></span>
              </label>
              <input
                :id="'customFieldList' + index"
                type="text"
                v-model="item.defaultInputValue"
                class="form-control"
                :placeholder="item.fieldName"
                :required="item.required"
              />
            </div>
          </a-col>
        </a-row>
      </form>
      <form
        v-else-if="
          nameDrawer === 'Request Data Input' ||
          nameDrawer === 'Create Data Completion Order'
        "
        style="padding: 24px 20px 24px 24px; margin-bottom: 100px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <label
                v-if="nameDrawer === 'Request Data Input'"
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 12px !important;
                "
                class="drawer-title"
              >
                Data Input Request ID
              </label>
              <label
                v-else
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 14px !important;
                "
                class="drawer-title"
              >
                Data Completion Order ID
              </label>
              <div
                style="
                  background: #e8e8e8;
                  border-radius: 1.7px;
                  padding: 5px 10px 6px 12px;
                  cursor: pointer;
                  width: 127px;
                  line-height: 100%;
                  height: 25px;
                "
              >
                <span style="font-size: 13px; color: #909090">
                  #{{ requestObject }}
                </span>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row
          v-if="
            nameDrawer === 'Create Data Completion Order' &&
            dataOder.length === 0
          "
          :gutter="16"
        >
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <div
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 14px !important;
                "
                class="drawer-title"
              >
                Data Type
                <span class="badge-require2"></span>
                <div
                  class="select-all"
                  @click="checkAll"
                  style="
                    font-size: 14px;
                    color: #3491ff;
                    margin: 6px 0 6.5px 0;
                    cursor: pointer;
                    font-weight: 400;
                  "
                >
                  Select All
                </div>
                <div class="order-zone">
                  <div
                    v-for="(item, index) in objectTypeOrder"
                    :key="index"
                    :class="{
                      selected: item.isSelect,
                      disabled:
                        item.name == 'Category' &&
                        currentUser != null &&
                        currentUserType != 'OEM' &&
                        !currentUser.admin,
                    }"
                    class="order-item"
                    v-if="
                      item.name != 'Category' ||
                      (currentUser != null &&
                        (currentUserType == 'OEM' || currentUser.admin))
                    "
                    @click="onChangeTooling(!item.isSelect, item)"
                  >
                    <p-check
                      :checked="item.isSelect"
                      @change="onChangeTooling($event, item)"
                    >
                      <span style="margin-left: 25px">
                        {{ item.name }}
                      </span>
                    </p-check>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row
          v-else-if="nameDrawer === 'Create Data Completion Order'"
          :gutter="16"
        >
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <div
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 14px !important;
                "
                class="drawer-title"
              >
                Data Type
                <span class="badge-require2"></span>
                <div
                  style="
                    height: 31px;
                    display: flex;
                    align-items: center;
                    margin-top: 7px;
                  "
                >
                  <all-tooling-dropdown
                    :query="changeSelectCompany"
                    :company-selected-list="companySelectedList"
                    :option-array-tooling="dataOder"
                    :resources="resources"
                    :type-name="typeName"
                  ></all-tooling-dropdown>
                </div>
                <div class="list-zone">
                  <div
                    v-for="(item, index) in companySelectedList"
                    class="list-zone_item"
                    :key="index"
                    v-if="
                      item.name != 'Category' ||
                      (currentUser &&
                        (currentUser.admin || currentUserType == 'OEM'))
                    "
                  >
                    <template v-if="typeName == 'PART'">
                      <span class="text-truncate">{{ item.code }}</span>
                    </template>
                    <template v-else>
                      <span class="text-truncate">{{ item.name }}</span>
                    </template>
                    <a
                      href="javascript:void(0)"
                      :id="'close-request-list' + index"
                      style="margin-left: 4px"
                      class="close-list-item btn-custom btn-custom-close-list"
                      type="primary"
                      @click="
                        onCloseRequest2(
                          'close-request-list' + index,
                          index + 'list',
                          item
                        )
                      "
                    >
                      <svg
                        v-show="isHover === index + 'list'"
                        xmlns="http://www.w3.org/2000/svg"
                        width="6.036"
                        height="6.036"
                        viewBox="0 0 6.036 6.036"
                      >
                        <g
                          id="Group_8480"
                          data-name="Group 8480"
                          transform="translate(0)"
                        >
                          <rect
                            id="Rectangle_10"
                            data-name="Rectangle 10"
                            width="1.219"
                            height="7.317"
                            rx="0.5"
                            transform="translate(5.174 0) rotate(45)"
                            fill="#e51616"
                          />
                          <rect
                            id="Rectangle_11"
                            data-name="Rectangle 11"
                            width="1.219"
                            height="7.317"
                            rx="0.5"
                            transform="translate(6.036 5.174) rotate(135)"
                            fill="#e51616"
                          />
                        </g>
                      </svg>
                      <svg
                        v-show="isHover !== index + 'list'"
                        xmlns="http://www.w3.org/2000/svg"
                        width="6.036"
                        height="6.036"
                        viewBox="0 0 6.036 6.036"
                      >
                        <g
                          id="Group_8480"
                          data-name="Group 8480"
                          transform="translate(0)"
                        >
                          <rect
                            id="Rectangle_10"
                            data-name="Rectangle 10"
                            width="1.219"
                            height="7.317"
                            rx="0.5"
                            transform="translate(5.174 0) rotate(45)"
                            fill="#4b4b4b"
                          />
                          <rect
                            id="Rectangle_11"
                            data-name="Rectangle 11"
                            width="1.219"
                            height="7.317"
                            rx="0.5"
                            transform="translate(6.036 5.174) rotate(135)"
                            fill="#4b4b4b"
                          />
                        </g>
                      </svg>
                    </a>
                  </div>
                  <div
                    v-if="companySelectedList.length === 0"
                    style="color: #909090; font-size: 12px"
                  >
                    <div>No items added.</div>
                    <div>Select items from the dropdown above.</div>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row v-else :gutter="16">
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <div
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 12px !important;
                "
                class="drawer-title"
              >
                <div>
                  Data Requested
                  <span class="badge-require2"></span>
                </div>
                <div
                  v-click-outside="clickOutsideCompany"
                  style="margin-top: 8px"
                  class="data-request-item"
                >
                  <div
                    @click="companyBoolean = !companyBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="companyBoolean"
                      @change="
                        ($event) => {
                          $event
                            ? (companyBoolean = true)
                            : (companyBoolean = false);
                        }
                      "
                      key="Company"
                    >
                      <span> Company </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      companyBoolean = true;
                      onFocus('companyValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus1"
                      @click="
                        () => {
                          companyBoolean
                            ? companyValue !== 0
                              ? (companyValue -= 1)
                              : 0
                            : '';
                          createRequest('minus1');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: companyBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="companyValue"
                      :disabled="!companyBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="companyValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus1"
                      @click="
                        () => {
                          companyBoolean ? +companyValue++ : '';
                          createRequest('plus1');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: companyBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
                <div
                  v-click-outside="clickOutsideLocation"
                  class="data-request-item"
                >
                  <div
                    @click="locationBoolean = !locationBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="locationBoolean"
                      @change="
                        ($event) => {
                          $event
                            ? (locationBoolean = true)
                            : (locationBoolean = false);
                        }
                      "
                      key="Location"
                    >
                      <span> Location </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      locationBoolean = true;
                      onFocus('locationValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus2"
                      @click="
                        () => {
                          locationBoolean
                            ? locationValue !== 0
                              ? (locationValue -= 1)
                              : 0
                            : '';
                          createRequest('minus2');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: locationBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="locationValue"
                      :disabled="!locationBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="locationValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus2"
                      @click="
                        () => {
                          locationBoolean ? +locationValue++ : '';
                          createRequest('plus2');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: locationBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
                <div
                  v-click-outside="clickOutsideCategory"
                  class="data-request-item"
                  v-if="
                    currentUser &&
                    (currentUser.admin || currentUserType == 'OEM')
                  "
                >
                  <div
                    @click="categoryBoolean = !categoryBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="categoryBoolean"
                      @change="
                        ($event) => {
                          $event
                            ? (categoryBoolean = true)
                            : (categoryBoolean = false);
                        }
                      "
                      key="Category"
                    >
                      <span> Category </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      categoryBoolean = true;
                      onFocus('categoryValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus3"
                      @click="
                        () => {
                          categoryBoolean
                            ? categoryValue !== 0
                              ? (categoryValue -= 1)
                              : 0
                            : '';
                          createRequest('minus3');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: categoryBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="categoryValue"
                      :disabled="!categoryBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="categoryValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus3"
                      @click="
                        () => {
                          categoryBoolean ? +categoryValue++ : '';
                          createRequest('plus3');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: categoryBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
                <div
                  v-click-outside="clickOutsidePart"
                  class="data-request-item"
                >
                  <div
                    @click="partBoolean = !partBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="partBoolean"
                      @change="
                        ($event) => {
                          $event ? (partBoolean = true) : (partBoolean = false);
                        }
                      "
                      key="Part"
                    >
                      <span> Part </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      partBoolean = true;
                      onFocus('partValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus4"
                      @click="
                        () => {
                          partBoolean
                            ? partValue !== 0
                              ? (partValue -= 1)
                              : 0
                            : '';
                          createRequest('minus4');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: partBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="partValue"
                      :disabled="!partBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="partValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus4"
                      @click="
                        () => {
                          partBoolean ? +partValue++ : '';
                          createRequest('plus4');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: partBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
                <div
                  v-click-outside="clickOutsideTooling"
                  class="data-request-item"
                >
                  <div
                    @click="toolingBoolean = !toolingBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="toolingBoolean"
                      @change="
                        ($event) => {
                          $event
                            ? (toolingBoolean = true)
                            : (toolingBoolean = false);
                        }
                      "
                      key="Tooling"
                    >
                      <span> Tooling </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      toolingBoolean = true;
                      onFocus('toolingValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus5"
                      @click="
                        () => {
                          toolingBoolean
                            ? toolingValue !== 0
                              ? (toolingValue -= 1)
                              : 0
                            : '';
                          createRequest('minus5');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: toolingBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="toolingValue"
                      :disabled="!toolingBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="toolingValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus5"
                      @click="
                        () => {
                          toolingBoolean ? +toolingValue++ : '';
                          createRequest('plus5');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: toolingBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
                <div
                  v-click-outside="clickOutsideMachine"
                  class="data-request-item"
                >
                  <div
                    @click="machineBoolean = !machineBoolean"
                    class="choosing-item"
                  >
                    <p-check
                      :checked="machineBoolean"
                      @change="
                        ($event) => {
                          $event
                            ? (machineBoolean = true)
                            : (machineBoolean = false);
                        }
                      "
                      key="Machine"
                    >
                      <span> Machine </span>
                    </p-check>
                  </div>
                  <div
                    @click="
                      machineBoolean = true;
                      onFocus('machineValue');
                    "
                    class="input-group-request"
                  >
                    <a
                      id="minus6"
                      @click="
                        () => {
                          machineBoolean
                            ? machineValue !== 0
                              ? (machineValue -= 1)
                              : 0
                            : '';
                          createRequest('minus6');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: machineBoolean }"
                      class="btn-custom quantity-field"
                    >
                      <img src="/images/icon/icon-minus.svg" alt="" />
                    </a>
                    <input
                      v-model="machineValue"
                      :disabled="!machineBoolean"
                      onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                      ref="machineValue"
                      type="number"
                      step="1"
                      max=""
                      min="0"
                      value="0"
                      name="quantity"
                      class="custom-request-input"
                    />
                    <a
                      id="plus6"
                      @click="
                        () => {
                          machineBoolean ? +machineValue++ : '';
                          createRequest('plus6');
                        }
                      "
                      href="javascript:void(0)"
                      :class="{ active: machineBoolean }"
                      class="btn-custom button-plus quantity-field"
                    >
                      <img src="/images/icon/icon-plus.svg" alt="" />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <div
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 14px !important;
                "
                class="drawer-title"
              >
                Due Date
                <span class="badge-require2"></span>
                <div class="date-zone" @click="datePickerOpen">
                  <!--                <img style="width: 11.37pt;height: 11.37pt;margin-right: 8.84px;" src="/images/icon/request-calendar.png" alt="">-->
                  <a-date-picker
                    style="z-index: 4001"
                    :allow-clear="false"
                    class="date-picker-customize date-picker-request"
                    @click="datePickerOpen"
                    v-click-outside="hideDatePickerOpen"
                    :class="{
                      isSlectedDate: startDate,
                      isOpenRequest: isFocusInput,
                    }"
                    format="MMM Do, YYYY"
                    v-model="startDate"
                  >
                    <div slot="suffixIcon">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="9.973"
                        height="5.737"
                        viewBox="0 0 9.973 5.737"
                      >
                        <path
                          id="Icon_feather-chevron-down"
                          data-name="Icon feather-chevron-down"
                          d="M9,13.5l3.926,3.926L16.852,13.5"
                          transform="translate(-7.939 -12.439)"
                          fill="none"
                          stroke="#595959"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="1.5"
                        />
                      </svg>
                      <!--                    <svg xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737">-->
                      <!--                      <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(17.912 18.176) rotate(-180)" fill="none" stroke="#595959" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"/>-->
                      <!--                    </svg>-->
                    </div>
                  </a-date-picker>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div style="margin-bottom: 28px" class="create-field">
              <div
                style="
                  color: #4b4b4b;
                  font-weight: bold;
                  margin-bottom: 5px;
                  font-size: 14px !important;
                "
                class="drawer-title"
              >
                Assigned To
                <span class="badge-require2"></span>
                <div style="margin-top: 8px">
                  <asigned-to
                    v-if="trigger"
                    :change-part="changePart"
                    :option-array-part="optionArrayPart"
                    :resources="resources"
                  ></asigned-to>
                </div>
                <div v-if="usersList.length !== 0" class="avatar-zone">
                  <div v-for="(item, index) in usersList" class="avatar-item-w">
                    <a-tooltip placement="bottom">
                      <template slot="title">
                        <div style="padding: 6px; font-size: 13px">
                          <div>
                            <b>{{ item.name }}</b>
                          </div>
                          <div>
                            Company:
                            <span v-if="item.company">{{
                              item.company.name
                            }}</span>
                          </div>
                          <div>Department: {{ item.department }}</div>
                          <div style="margin-bottom: 15px">
                            Position: {{ item.position }}
                          </div>
                          <div>Email: {{ item.email }}</div>
                          <div>Phone: {{ item.mobileNumber }}</div>
                        </div>
                      </template>
                      <div
                        class="avatar-item"
                        :style="{
                          'background-color': getRequestedColor(
                            item.name,
                            item.id
                          ),
                        }"
                      >
                        {{ convertName(item.name) }}
                      </div>
                    </a-tooltip>
                  </div>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </form>
      <form
        v-else
        style="padding-right: 44px; padding-left: 24px; padding-top: 24px"
        layout="vertical"
        action=""
        hide-required-mark
      >
        <button hidden type="submit" :ref="'btn-checklist-create'"></button>
        <a-row :gutter="16">
          <a-col :span="24">
            <template>
              <div class="create-field create-field-category">
                <div
                  style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                  class="drawer-title"
                >
                  Property Type
                </div>
                <custom-dropdown
                  :list-item="propertyTypeArray"
                  :title-field="'name'"
                  @change="selectPropertyType"
                ></custom-dropdown>
              </div>
            </template>
          </a-col>
        </a-row>
        <a-row v-if="propertyType === 'TOOLING'" :gutter="16">
          <a-col :span="24">
            <template>
              <div class="create-field create-field-category">
                <div
                  style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                  class="drawer-title"
                >
                  Property Group
                </div>
                <a-select v-model="propertyGroup">
                  <a-select-option
                    v-for="(item, index) in propertyGroupList"
                    :key="index"
                    :value="item.value"
                  >
                    {{ item.name }}
                  </a-select-option>
                  <a-icon
                    style="color: #3491ff"
                    slot="suffixIcon"
                    type="caret-down"
                  />
                </a-select>
              </div>
            </template>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="create-field create-field-category">
              <div
                style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
                class="drawer-title"
                v-text="resources['property_name']"
              ></div>
              <input
                :class="{ errorFieldName: errorFieldName }"
                ref="added-field"
                @keyup="removeAlertCreate()"
                v-model="configName"
                class="form-control reason-edit-input focus"
              />
              <div
                v-if="errorFieldName"
                style="
                  font-size: 11px;
                  color: #ef4444;
                  text-align: left;
                  margin-top: 1px;
                "
                v-text="resources['property_name_already_exists']"
              ></div>
            </div>
          </a-col>
        </a-row>
        <div
          style="color: #1a1a1a; font-weight: bold; margin-bottom: 5px"
          class="drawer-title"
          v-text="resources['field_type']"
        ></div>
        <div class="field-type">
          <div v-text="resources['is_this_field_required_or_optional']"></div>
          <div class="d-flex justify-content-between align-items-center">
            <div class="form-check form-check-inline">
              <label class="form-check-label">
                <input
                  type="radio"
                  class="form-check-input"
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
                  class="form-check-input"
                  :value="requiredTypeOptions.OPTIONAL"
                  v-model="isConfigRequired"
                />
                <span v-text="resources['optional']"></span>
              </label>
            </div>
          </div>
          <div
            v-text="resources['what_is_the_input_type_for_this_field']"
          ></div>
          <div class="d-flex justify-content-between align-items-center">
            <div class="form-check form-check-inline">
              <label class="form-check-label">
                <input
                  type="radio"
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
                  class="form-check-input"
                  :value="valueTypeOptions.DEFAULT"
                  v-model="isConfigManual"
                />
                <span v-text="resources['default']"></span>
              </label>
            </div>
          </div>
          <div>
            <input
              v-if="isConfigManual === valueTypeOptions.DEFAULT"
              :placeholder="resources['default']"
              v-model="defaultValue"
              class="form-control reason-edit-input focus"
            />
          </div>
        </div>
      </form>
      <div
        v-if="nameDrawer === 'Request Data Input'"
        :style="{
          position: 'absolute',
          right: 0,
          bottom: 0,
          width: '100%',
          padding: '24px 24px 55px 24px',
          background: '#fff',
          display: 'flex',
          justifyContent: 'flex-end',
          alignItems: 'center',
          textAlign: 'right',
          zIndex: 4002,
        }"
      >
        <!--      <div class="d-flex align-items-center custom-link" style="font-size: 12pt;color: #E51616;margin-right: 8px" v-text="resources['cancel']" @click="onClose">-->

        <!--      </div>-->
        <div>
          <a
            href="javascript:void(0)"
            id="close-request"
            style="margin-right: 8px"
            class="btn-custom btn-custom-close"
            type="primary"
            @click="onCloseRequest('close-request')"
          >
            {{ resources["cancel"] }}
          </a>
        </div>
        <div>
          <a
            v-if="isValidRequest"
            href="javascript:void(0)"
            id="create-request"
            class="btn-custom btn-custom-primary"
            type="primary"
            @click="createRequest('create-request')"
          >
            {{ "Create Request" }}
          </a>
          <a
            v-else
            class="btn-custom"
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              border: 1px solid #c1c1c1;
              color: #fff;
            "
            type="primary"
          >
            Create Request
          </a>
        </div>
      </div>
      <div
        v-else-if="nameDrawer === 'Create Data Completion Order'"
        :style="{
          position: 'absolute',
          right: 0,
          bottom: 0,
          width: '100%',
          padding: '24px 24px 55px 24px',
          background: '#fff',
          display: 'flex',
          justifyContent: 'space-around',
          alignItems: 'center',
          textAlign: 'right',
          zIndex: 4002,
        }"
      >
        <div>
          <a
            href="javascript:void(0)"
            id="close-request2"
            style="margin-right: 8px"
            class="btn-custom btn-custom-close"
            type="primary"
            @click="onCloseRequest('close-request2')"
          >
            {{ resources["cancel"] }}
          </a>
        </div>
        <div>
          <a
            v-if="isValidRequestOrder"
            href="javascript:void(0)"
            id="create-request2"
            class="btn-custom btn-custom-primary"
            type="primary"
            @click="createRequestOrder('create-request2')"
          >
            {{ "Create Request" }}
          </a>
          <a
            v-else
            class="btn-custom"
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              border: 1px solid #c1c1c1;
              color: #fff;
            "
            type="primary"
          >
            Create Completion Order
          </a>
        </div>
      </div>
      <div
        v-else
        :style="{
          position: 'absolute',
          right: 0,
          bottom: 0,
          width: '100%',
          padding: '24px',
          background: '#fff',
          display: 'flex',
          justifyContent: 'end',
          textAlign: 'right',
          zIndex: 4002,
        }"
      >
        <div
          class="d-flex align-items-center custom-link cancel-btn-drawer"
          v-text="resources['cancel']"
          @click="onClose"
        ></div>
        <div v-if="nameDrawer === 'location'">
          <a-button
            v-if="isValidLocation"
            class="btn-custom-primary"
            style="
              width: 75px;
              font-size: 15px;
              height: unset;
              padding: 6px 8px;
              border-radius: 3px;
            "
            type="primary"
            :loading="loadingAction"
            @click="createPropertyLocation()"
          >
            {{ loadingAction ? "" : resources["create"] }}
          </a-button>
          <a-button
            v-else
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            v-text="resources['create']"
            type="primary"
          ></a-button>
        </div>
        <div v-else-if="nameDrawer === 'toolmaker'">
          <a-button
            v-if="isValidToolmaker"
            class="btn-custom-primary"
            style="
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            type="primary"
            :loading="loadingAction"
            @click="createPropertyToolmaker()"
          >
            {{ loadingAction ? "" : resources["create"] }}
          </a-button>
          <a-button
            v-else
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            v-text="resources['create']"
            type="primary"
          ></a-button>
        </div>
        <div v-else-if="nameDrawer === 'supplier'">
          <a-button
            v-if="isValidSupplier"
            class="btn-custom-primary"
            type="primary"
            style="
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            :loading="loadingAction"
            @click="createPropertySupplier()"
          >
            {{ loadingAction ? "" : resources["create"] }}
          </a-button>
          <a-button
            v-else
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            v-text="resources['create']"
            type="primary"
          ></a-button>
        </div>
        <div v-else-if="nameDrawer === 'part'">
          <a-button
            v-if="isValidPart && isValidCustomField"
            class="btn-custom-primary"
            style="
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            type="primary"
            :loading="loadingAction"
            @click="createPropertyPart()"
          >
            {{ loadingAction ? "" : resources["create"] }}
          </a-button>
          <a-button
            v-else
            style="
              background: #c1c1c1;
              border-color: #c1c1c1;
              width: 75px;
              font-size: 12pt;
              height: unset;
              padding: 6px 10px;
            "
            v-text="resources['create']"
            type="primary"
          ></a-button>
        </div>
        <div v-else>
          <a-button
            v-if="configName && configName.trim() != ''"
            class="create-property-btn"
            :loading="loadingAction"
            @click="createProperty()"
          >
            {{ loadingAction ? "" : "Create New Property" }}
          </a-button>
          <a-button v-else class="create-property-btn-disable" type="primary"
            >Create New Property</a-button
          >
        </div>
      </div>
    </a-drawer>
    <requested-popup
      :visible-requested="visibleRequested"
      :total="+totalAmount"
    ></requested-popup>
  </div>
</template>

<script>
module.exports = {
  name: "CreatePropertyDrawer",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    isType: {
      type: String,
      default: "Tooling",
    },
    isMatchingColumn: {
      type: Boolean,
      default: false,
    },
    nameDrawer: {
      type: String,
      default: null,
    },
    resources: Object,
    categorySelected: {
      type: String,
      default: null,
    },
    dataOder: {
      type: Array,
      default: null,
    },
    dataSelected: {
      type: Array,
      default: null,
    },
    typeName: {
      type: String,
      default: null,
    },
    // getRequestedColor: Function,
    currentUser: Object,
    currentUserType: String,
  },
  components: {
    "asigned-to": httpVueLoader("/components/import-tooling/AsignedTo.vue"),
    "requested-popup": httpVueLoader(
      "/components/import-tooling/requested-popup.vue"
    ),
    "all-tooling-dropdown": httpVueLoader(
      "/components/all-tooling-dropdown.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
  },
  data() {
    return {
      propertyTypeArray: [
        { name: "Category", value: "CATEGORY" },
        { name: "Tooling", value: "TOOLING" },
        { name: "Part", value: "PART" },
        { name: "Machine", value: "MACHINE" },
      ],
      isHover: null,
      // TODO: RECHECK: THIS STATE WAS ACTUALLY ASSIGN TO USER
      optionArrayPart: [],
      trigger: false,
      loadingAction: false,
      usersList: [],
      isConfigRequired: "OPTIONAL",
      isConfigManual: "MANUAL",
      configName: "",
      defaultValue: "",
      defaultValueTime: moment(),
      requiredTypeOptions: {
        REQUIRED: "REQUIRED",
        OPTIONAL: "OPTIONAL",
      },
      valueTypeOptions: {
        MANUAL: "MANUAL",
        DEFAULT: "DEFAULT",
      },
      propertyType: "",
      propertyGroup: "BASIC",
      propertyGroupList: [
        {
          name: "Basic Information (Static)",
          value: "BASIC",
        },
        {
          name: "Physical Information",
          value: "PHYSICAL",
        },
        {
          name: "Runner System Information",
          value: "RUNNER_SYSTEM",
        },
        {
          name: "Cost Information",
          value: "COST",
        },
        {
          name: "Supplier Information",
          value: "SUPPLIER",
        },
        {
          name: "Production Information",
          value: "PRODUCTION",
        },
        {
          name: "Maintenance Information",
          value: "MAINTENANCE",
        },
      ],
      locationObj: {
        company: {
          id: null,
          name: null,
        },
        locationName: null,
        locationId: null,
        address: null,
        memo: null,
      },
      toolmakerObj: {
        companyName: null,
        companyId: null,
        address: null,
        manager: null,
        phoneNumber: null,
        email: null,
        memo: null,
      },
      supplierObj: {
        companyName: null,
        companyId: null,
        address: null,
        manager: null,
        phoneNumber: null,
        email: null,
        memo: null,
      },
      partObj: {
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
      },
      customFieldList: [],
      errorFieldName: false,
      errorEmail: false,
      errorId: false,
      errorName: false,
      companyList: [],
      categories: [],
      codes: {},
      requiredFields: [],
      companyValue: 0,
      companyBoolean: false,
      locationValue: 0,
      locationBoolean: false,
      categoryValue: 0,
      categoryBoolean: false,
      partValue: 0,
      partBoolean: false,
      toolingValue: 0,
      toolingBoolean: false,
      machineValue: 0,
      machineBoolean: false,
      dataRequestType: null,
      startDate: null,
      searchValue: ["china"],
      defaultSelected: "",
      isFocusInput: false,
      requestObject: "",
      thirdFiles: [],
      partPictureFiles: [],
      fileTypes: {
        PART_PICTURE: "PART_PICTURE",
      },
      objectTypeOrder: [
        {
          name: "Tooling",
          isSelect: false,
        },
        {
          name: "Part",
          isSelect: false,
        },
        {
          name: "Machine",
          isSelect: false,
        },
        {
          name: "Company",
          isSelect: false,
        },
        {
          name: "Location",
          isSelect: false,
        },
        {
          name: "Category",
          isSelect: false,
        },
      ],
      paramsOrder: {
        allLocation: false,
        allCategory: false,
        allPart: false,
        allMold: false,
        allMachine: false,
      },
      checkedListTooling: [],
      companySelectedList: "",
      visibleRequested: false,
      totalAmount: 0,
    };
  },
  computed: {
    nameDrawerTitle() {
      if (this.nameDrawer) {
        if (this.nameDrawer === "Request Data Input") {
          return "Request Data Input";
        } else if (this.nameDrawer === "Create Data Completion Order") {
          return false;
        } else {
          return "Create " + this.nameDrawer;
        }
      } else {
        return "Create Property";
      }
    },
    isValidRequest() {
      return (
        this.usersList.length !== 0 &&
        this.startDate &&
        this.checkValue() >= 1 &&
        this.isValidField()
      );
    },
    isValidRequestOrder() {
      const valid =
        this.dataOder.length !== 0
          ? this.companySelectedList.length > 0
          : this.dataTypeOrder;
      return this.usersList.length !== 0 && this.startDate && valid;
    },
    isValidLocation() {
      return (
        this.locationObj.locationName &&
        this.locationObj.locationName.trim() != "" &&
        this.locationObj.locationId &&
        this.locationObj.locationId.trim() != "" &&
        this.locationObj.address &&
        this.locationObj.address.trim() != "" &&
        this.locationObj.company.id &&
        this.locationObj.company.name
      );
    },
    isValidToolmaker() {
      return (
        this.toolmakerObj.companyName &&
        this.toolmakerObj.companyName.trim() != "" &&
        this.toolmakerObj.companyId &&
        this.toolmakerObj.companyId.trim() != ""
      );
    },
    isValidSupplier() {
      return (
        this.supplierObj.companyName &&
        this.supplierObj.companyName.trim() != "" &&
        this.supplierObj.companyId &&
        this.supplierObj.companyId.trim() != ""
      );
    },
    isValidPart() {
      const fields = [
        "category",
        "partCode",
        "name",
        "resinCode",
        "resinGrade",
        "designRevision",
        "size",
        "weight",
        "weeklyDemand",
        "partPictureFile",
      ];
      const value = [
        this.partObj.category,
        this.partObj.partCode,
        this.partObj.name,
        this.partObj.resinCode,
        this.partObj.resinGrade,
        this.partObj.designRevision,
        this.partObj.size,
        this.partObj.weight,
        this.partObj.weeklyDemand,
        this.thirdFiles,
      ];
      const invalidItem = [];
      fields.forEach((item, index) => {
        if (
          (this.requiredFields.includes(item) &&
            (!value[index] ||
              (item === "partPictureFile" && value[index].length === 0))) ||
          this.partObj.name.trim() === ""
        ) {
          invalidItem.push(item);
        }
      });
      return invalidItem.length <= 0;
    },
    isValidCustomField() {
      const validArr = this.customFieldList.filter(
        (item) =>
          item.required &&
          !item.defaultInputValue &&
          item.defaultInputValue.trim() == ""
      );
      return validArr.length == 0;
    },
    locationModel() {
      return (
        this.locationObj.company.id + " - " + this.locationObj.company.name
      );
    },
    dataTypeOrder() {
      const dataTypeOrderNotNull = this.objectTypeOrder.filter(
        (value) => value.isSelect === true
      );
      return dataTypeOrderNotNull.length > 0;
    },
  },
  created() {
    this.initForm();
    if (this.nameDrawer === "Create Data Completion Order") {
      this.getRequestIdOrder();
    } else {
      this.getRequestId();
    }
  },
  watch: {
    isType(newVal) {
      this.propertyType = newVal;
    },
    visible(newVal) {
      console.log(newVal, "new");
      if (newVal) {
        this.switchCase();
      } else {
        this.usersList = [];
        this.trigger = false;
      }
    },
    companyBoolean(newVal) {
      if (!newVal) {
        this.companyValue = 0;
      }
    },
    locationBoolean(newVal) {
      if (!newVal) {
        this.locationValue = 0;
      }
    },
    categoryBoolean(newVal) {
      if (!newVal) {
        this.categoryValue = 0;
      }
    },
    partBoolean(newVal) {
      if (!newVal) {
        this.partValue = 0;
      }
    },
    toolingBoolean(newVal) {
      if (!newVal) {
        this.toolingValue = 0;
      }
    },
    machineBoolean(newVal) {
      if (!newVal) {
        this.machineValue = 0;
      }
    },
  },
  mounted() {
    this.propertyType = this.isType;
    this.switchCase();
    this.getUsers();
    this.trigger = true;
    if (this.dataSelected && this.dataSelected.length !== 0) {
      this.companySelectedList = this.dataSelected;
      console.log(this.companySelectedList, "this.companySelectedList");
    }
  },
  methods: {
    selectPropertyType(item) {
      this.propertyType = item.value;
    },
    getRequestedColor(name, id) {
      return Common.getRequestedColor(name, id);
    },
    onMouseOverList(data) {
      this.isHover = data;
    },
    changeSelectCompany(item) {
      this.companySelectedList = item;
    },
    checkedTooling: function (item) {
      const findIndex = this.checkedListTooling.findIndex(
        (value) => value.name == item.name
      );
      return findIndex !== -1;
    },
    checkAll() {
      this.objectTypeOrder.forEach((value) => {
        value.isSelect = true;
      });
    },
    onChangeTooling: function (isChecked, item) {
      item.isSelect = isChecked;
    },
    checkValue() {
      return (
        +this.companyValue +
        +this.locationValue +
        +this.categoryValue +
        +this.partValue +
        +this.toolingValue +
        +this.machineValue
      );
    },
    getRequestId() {
      return axios
        .get("/api/data-registration/generate-request_id")
        .then((res) => {
          this.requestObject = res.data;
        });
    },
    getRequestIdOrder() {
      return axios
        .get("/api/data-completion/generate-request_id")
        .then((res) => {
          this.requestObject = res.data;
        });
    },
    datePickerOpen() {
      console.log("click");
      this.isFocusInput = true;
    },
    hideDatePickerOpen() {
      this.isFocusInput = false;
    },
    switchCase() {
      this.usersList = [];
      this.trigger = true;
      switch (this.categorySelected) {
        case "companies":
          this.defaultSelected = "companyValue";
          this.companyBoolean = true;
          break;
        case "locations":
          this.defaultSelected = "locationValue";
          this.locationBoolean = true;
          break;
        case "categories":
          this.defaultSelected = "categoryValue";
          this.categoryBoolean = true;
          break;
        case "parts":
          this.defaultSelected = "partValue";
          this.partBoolean = true;
          break;
        case "molds":
          this.defaultSelected = "toolingValue";
          this.toolingBoolean = true;
          break;
        case "machines":
          this.defaultSelected = "machineValue";
          this.machineBoolean = true;
          break;
      }
      this.initForm();
    },
    async getUsers() {
      this.optionArrayPart = await headerVm?.getListUsers();
    },
    timeConverter(UNIX_timestamp) {
      var a = new Date(UNIX_timestamp * 1000);
      var months = [
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
      ];
      var year = a.getFullYear();
      var month = months[a.getMonth()];
      var date = a.getDate();
      date2 = ("0" + date).slice(-2);
      var hour = a.getHours();
      hour2 = ("0" + hour).slice(-2);
      var min = a.getMinutes();
      min2 = ("0" + min).slice(-2);
      var sec = a.getSeconds();
      sec2 = ("0" + sec).slice(-2);
      var time =
        year + "" + month + "" + date2 + "" + hour2 + "" + min2 + "" + sec2;
      return time;
    },
    convertName(name) {
      var split_names = name.trim().split(" ");
      if (split_names.length > 1) {
        return (
          split_names[0].charAt(0) +
          split_names[split_names.length - 1].charAt(0)
        );
      }
      return split_names[0].charAt(0);
    },
    changePart(part) {
      this.usersList = part;
    },
    clickOutsideCompany() {
      if (
        (this.companyValue === "" || this.companyValue === 0) &&
        this.defaultSelected !== "companyValue"
      ) {
        this.companyBoolean = false;
      }
    },
    clickOutsideLocation() {
      if (
        (this.locationValue === "" || this.locationValue === 0) &&
        this.defaultSelected !== "locationValue"
      ) {
        this.locationBoolean = false;
      }
    },
    clickOutsideCategory() {
      if (
        (this.categoryValue === "" || this.categoryValue === 0) &&
        this.defaultSelected !== "categoryValue"
      ) {
        this.categoryBoolean = false;
      }
    },
    clickOutsidePart() {
      if (
        (this.partValue === "" || this.partValue === 0) &&
        this.defaultSelected !== "partValue"
      ) {
        this.partBoolean = false;
      }
    },
    clickOutsideTooling() {
      if (
        (this.toolingValue === "" || this.toolingValue === 0) &&
        this.defaultSelected !== "toolingValue"
      ) {
        this.toolingBoolean = false;
      }
    },
    clickOutsideMachine() {
      if (
        (this.machineValue === "" || this.machineValue === 0) &&
        this.defaultSelected !== "machineValue"
      ) {
        this.machineBoolean = false;
      }
    },
    getListSelected() {},
    onCloseRequest(id) {
      const el = document.getElementById(id);
      el.classList.add("close-animation");
      setTimeout(() => {
        el.classList.remove("close-animation");
        this.onClose();
      }, 700);
    },
    onCloseRequest2(id, hover, item) {
      this.isHover = hover;
      const el = document.getElementById(id);
      el.classList.add("close-animation");
      el.classList.add("active-list");
      setTimeout(() => {
        this.companySelectedList = this.companySelectedList.filter(
          (res) => res.code !== item.code
        );
        this.isHover = null;
        el.classList.remove("close-animation");
        el.classList.remove("active-list");
      }, 700);
    },
    createRequest(id) {
      const el = document.getElementById(id);
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        if (id === "create-request") {
          this.confirmCreateRequest();
        }
      }, 700);
    },
    createRequestOrder(id) {
      const el = document.getElementById(id);
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        this.confirmCreateRequestOrder();
      }, 700);
    },
    confirmCreateRequestOrder() {
      this.loadingAction = true;
      const user = this.usersList.map((item) => {
        return {
          id: item.id,
        };
      });
      console.log(this.objectTypeOrder, "this.objectTypeOrder[3]");
      const params = {
        allCompany: this.objectTypeOrder[3].isSelect,
        allLocation: this.objectTypeOrder[4].isSelect,
        allCategory: this.objectTypeOrder[5].isSelect,
        allPart: this.objectTypeOrder[1].isSelect,
        allTooling: this.objectTypeOrder[0].isSelect,
        allMachine: this.objectTypeOrder[2].isSelect,
        companies: [],
        locations: [],
        categories: [],
        parts: [],
        molds: [],
        machines: [],
        orderId: this.requestObject,
        dueDay: this.timeConverter(this.startDate.unix()),
        assignedUsers: user,
      };
      if (this.dataOder.length !== 0) {
        const arrId = this.companySelectedList.map((res) => {
          return {
            id: res.id,
          };
        });
        switch (this.typeName.toLocaleLowerCase()) {
          case "company":
            params.companies = arrId;
            break;
          case "location":
            params.locations = arrId;
            break;
          case "category":
            params.categories = arrId;
            break;
          case "part":
            params.parts = arrId;
            break;
          case "tooling":
            params.molds = arrId;
            break;
          case "machine":
            params.machines = arrId;
            break;
        }
      }
      console.log(params, "params");
      axios
        .put("/api/data-completion/create-order", params)
        .then((res) => {
          console.log(res, "completion");
        })
        .finally(() => {
          this.loadingAction = false;
          vm.getDataCompletionData();
          this.onClose();
        });
    },
    confirmCreateRequest() {
      this.loadingAction = true;
      const user = this.usersList.map((item) => {
        return {
          id: item.id,
        };
      });
      const params = {
        requestId: this.requestObject,
        companyNumber: this.companyValue,
        locationNumber: this.locationValue,
        categoryNumber: this.categoryValue,
        partNumber: this.partValue,
        moldNumber: this.toolingValue,
        machineNumber: this.machineValue,
        dueDay: this.timeConverter(this.startDate.unix()),
        assignedUsers: user,
      };
      console.log(params, "params");
      axios
        .put("/api/data-registration/create-request", params)
        .then((res) => {
          console.log(res, "registration");
          this.totalAmount = this.checkValue();
          setTimeout(() => {
            this.visibleRequested = !this.visibleRequested;
          }, 50);
        })
        .finally(() => {
          this.loadingAction = false;
          this.onClose();
        });
    },
    onFocus(ref) {
      const self = this;
      setTimeout(() => {
        self.$refs[ref].focus();
      }, 100);
    },
    incrementValue() {},
    incrementValue2(e) {
      e.preventDefault();
      var fieldName = $(e.target).data("field");
      var parent = $(e.target).closest("div");
      var currentVal = parseInt(
        parent.find("input[name=" + fieldName + "]").val(),
        10
      );

      if (!isNaN(currentVal)) {
        parent.find("input[name=" + fieldName + "]").val(currentVal + 1);
      } else {
        parent.find("input[name=" + fieldName + "]").val(0);
      }
    },
    decrementValue2(e) {
      e.preventDefault();
      var fieldName = $(e.target).data("field");
      var parent = $(e.target).closest("div");
      var currentVal = parseInt(
        parent.find("input[name=" + fieldName + "]").val(),
        10
      );

      if (!isNaN(currentVal) && currentVal > 0) {
        parent.find("input[name=" + fieldName + "]").val(currentVal - 1);
      } else {
        parent.find("input[name=" + fieldName + "]").val(0);
      }
    },
    isRequiredField(field) {
      return this.requiredFields.includes(field);
    },
    initDefaultValue() {
      const fields = [
        "category",
        "partCode",
        "name",
        "resinCode",
        "resinGrade",
        "designRevision",
        "size",
        "weight",
        "weeklyDemand",
      ];
      // get current config value
      axios
        .get("/api/config?configCategory=" + "PART")
        .then((response) => {
          if (response.data && response.data.length > 0) {
            console.log("form response data", response.data);
            let requiredFieldList = [];
            response.data.forEach((field) => {
              if (field.required) {
                requiredFieldList.push(field.fieldName);
              }
            });
            this.requiredFields = requiredFieldList;
            fields.forEach((field) => {
              let defaultField = response.data.filter(
                (item) => item.fieldName === field
              )[0];
              console.log("defaultField", defaultField);
              if (!defaultField) return;
              if (defaultField.fieldName === "weeklyDemand") {
                if (
                  isNaN(defaultField.defaultInputValue) ||
                  defaultField.defaultInputValue < 0 ||
                  defaultField.defaultInputValue > 99999999
                ) {
                  return;
                }
              }
              if (defaultField.defaultInput && defaultField.defaultInputValue) {
                this.partObj[defaultField.fieldName] =
                  defaultField.defaultInputValue;
              }
              if (this.partObj.size) {
                const sizes = this.partObj.size.split("x");
                if (sizes.length >= 3) {
                  this.partObj.sizeWidth = sizes[0].trim();
                  this.partObj.sizeDepth = sizes[1].trim();
                  this.partObj.sizeHeight = sizes[2].trim();

                  this.partObj.sizeWidth =
                    isNaN(this.partObj.sizeWidth) || this.partObj.sizeWidth < 0
                      ? ""
                      : this.partObj.sizeWidth;
                  this.partObj.sizeDepth =
                    isNaN(this.partObj.sizeDepth) || this.partObj.sizeDepth < 0
                      ? ""
                      : this.partObj.sizeDepth;
                  this.partObj.sizeHeight =
                    isNaN(this.partObj.sizeHeight) ||
                    this.partObj.sizeHeight < 0
                      ? ""
                      : this.partObj.sizeHeight;
                }
              }
            });
          }
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    initForm() {
      // TODO: RECHECK REDUCE CALL
      axios
        .get("/api/companies?query=&size=99999&status=active&sort=id%2Cdesc")
        .then((res) => {
          this.companyList = res.data.content;
        });
      axios.get("/api/categories?size=1000").then((res) => {
        console.log(res, "resssss");
        this.categories = res.data.content;
      });
      axios.get("/api/custom-field?objectType=PART").then((res) => {
        this.customFieldList = res.data;
        console.log(this.customFieldList, "/api/custom-field");
      });
      axios.get("/api/codes").then((res) => {
        this.codes = res.data;
        console.log(this.codes, "codesssss");
      });
      Common.getCategoryConfigStatus().then((data) => {
        if (data && data.length > 0) {
          let currentConfig = data.filter(
            (item) => item.configCategory === "PART"
          )[0];
          if (currentConfig && currentConfig.enabled) {
            this.initDefaultValue();
          }
        }
      });
    },
    handleChangeLocation(value) {
      console.log(value, "handleChangeLocation");
      this.locationObj.company.id = value.id;
      this.locationObj.company.name = value.name;
    },
    handleChangeCategory(value) {
      console.log(value, "handleChangeCategory");
      this.partObj.categoryId = value.id;
    },
    mappingCompany(name) {
      if (this.companyList) {
        return this.companyList.filter((item) => item.companyType === name);
      }
    },
    removeAlertCreate() {
      this.errorFieldName = false;
      this.$forceUpdate();
    },
    removeAlertCreateCompany(type) {
      if (type === "name") {
        this.errorName = false;
      } else if (type === "id") {
        this.errorId = false;
      } else if (type === "email") {
        this.errorEmail = false;
      }
      this.$forceUpdate();
    },
    async createPropertyLocation() {
      try {
        this.loadingAction = true;
        const params = {
          address: this.locationObj.address
            ? this.locationObj.address.trim()
            : this.locationObj.address,
          company: {
            id: this.locationObj.company.id,
            name: this.locationObj.company.name,
          },
          companyId: this.locationObj.company.id,
          companyName: this.locationObj.company.name,
          enabled: true,
          locationCode: this.locationObj.locationId
            ? this.locationObj.locationId.trim()
            : this.locationObj.locationId,
          memo: this.locationObj.memo
            ? this.locationObj.memo.trim()
            : this.locationObj.memo,
          name: this.locationObj.locationName
            ? this.locationObj.locationName.trim()
            : this.locationObj.locationName,
        };
        // this.createToolingSuccess(params.locationCode)
        // this.onClose();
        // Common.alert("Success!");
        const data = await axios.post("/api/locations", params);
        if (data.data.success) {
          this.createToolingSuccess(params.locationCode);
          this.onClose();
          Common.alert(`New ${this.nameDrawer} has been successfully created.`);
        } else {
          if (
            data.data.message ===
            "Location Name is already registered in the system."
          ) {
            this.errorName = true;
          }
          if (
            data.data.message ===
            "Location ID is already registered in the system."
          ) {
            this.errorId = true;
          }
          // Common.alert(data.data.message);
        }
      } catch (e) {
        console.log(e);
        Common.alert("Fail!");
      } finally {
        this.loadingAction = false;
      }
    },
    async createPropertyToolmaker() {
      if (
        this.toolmakerObj.email &&
        this.toolmakerObj.email.trim() != "" &&
        !/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,24}))$/.test(
          this.toolmakerObj.email
        )
      ) {
        this.errorEmail = true;
      } else {
        try {
          this.loadingAction = true;
          const params = {
            address: this.toolmakerObj.address
              ? this.toolmakerObj.address.trim()
              : this.toolmakerObj.address,
            companyCode: this.toolmakerObj.companyId
              ? this.toolmakerObj.companyId.trim()
              : this.toolmakerObj.companyId,
            companyType: "TOOL_MAKER",
            email: this.toolmakerObj.email
              ? this.toolmakerObj.email.trim()
              : this.toolmakerObj.email,
            enabled: true,
            manager: this.toolmakerObj.manager
              ? this.toolmakerObj.manager.trim()
              : this.toolmakerObj.manager,
            memo: this.toolmakerObj.memo
              ? this.toolmakerObj.memo.trim()
              : this.toolmakerObj.memo,
            name: this.toolmakerObj.companyName
              ? this.toolmakerObj.companyName.trim()
              : this.toolmakerObj.companyName,
            phone: this.toolmakerObj.phoneNumber
              ? this.toolmakerObj.phoneNumber.trim()
              : this.toolmakerObj.phoneNumber,
          };
          const data = await axios.post("/api/companies", params);
          if (data.data.success) {
            this.createToolingSuccess(params.companyCode);
            this.onClose();
            Common.alert(
              `New ${this.nameDrawer} has been successfully created.`
            );
          } else {
            if (
              data.data.message ===
              "Company Name is already registered in the system."
            ) {
              this.errorName = true;
            }
            if (
              data.data.message ===
              "Company ID is already registered in the system."
            ) {
              this.errorId = true;
            }
          }
        } catch (e) {
          Common.alert("Fail!");
        } finally {
          this.loadingAction = false;
        }
      }
    },
    async createPropertySupplier() {
      if (
        this.supplierObj.email &&
        this.supplierObj.email.trim() != "" &&
        !/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,24}))$/.test(
          this.supplierObj.email
        )
      ) {
        this.errorEmail = true;
      } else {
        try {
          this.loadingAction = true;
          const params = {
            address: this.supplierObj.address
              ? this.supplierObj.address.trim()
              : this.supplierObj.address,
            companyCode: this.supplierObj.companyId
              ? this.supplierObj.companyId.trim()
              : this.supplierObj.companyId,
            companyType: "SUPPLIER",
            email: this.supplierObj.email
              ? this.supplierObj.email.trim()
              : this.supplierObj.email,
            enabled: true,
            manager: this.supplierObj.manager
              ? this.supplierObj.manager.trim()
              : this.supplierObj.manager,
            memo: this.supplierObj.memo
              ? this.supplierObj.memo.trim()
              : this.supplierObj.memo,
            name: this.supplierObj.companyName
              ? this.supplierObj.companyName.trim()
              : this.supplierObj.companyName,
            phone: this.supplierObj.phoneNumber
              ? this.supplierObj.phoneNumber.trim()
              : this.supplierObj.phoneNumber,
          };
          const data = await axios.post("/api/companies", params);
          console.log(data, "data");
          if (data.data.success) {
            this.createToolingSuccess(params.companyCode);
            this.onClose();
            Common.alert(
              `New ${this.nameDrawer} has been successfully created.`
            );
          } else {
            if (
              data.data.message ===
              "Company Name is already registered in the system."
            ) {
              this.errorName = true;
            }
            if (
              data.data.message ===
              "Company ID is already registered in the system."
            ) {
              this.errorId = true;
            }
          }
        } catch (e) {
          Common.alert("Fail!");
        } finally {
          this.loadingAction = false;
        }
      }
    },

    //thirdFiles
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
      if (this.thirdFiles.length == 0) {
        this.$refs.partUpload.value = "";
      } else {
        this.$refs.partUpload.value = "1221";
      }
    },
    deleteFileStorage: function (file, type) {
      let self = this;
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case self.fileTypes.PART_PICTURE:
              self.partPictureFiles = response.data;
              break;
          }
        });
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = this.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      if (files && isExitsFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    partFormData: function () {
      var formData = new FormData();
      /*
                          for (var i = 0; i < this.files.length; i++) {
                              let file = this.files[i];
                              formData.append("files", file);
                          }
                          for (var i = 0; i < this.secondFiles.length; i++) {
                              let file = this.secondFiles[i];
                              formData.append("secondFiles", file);
                          }
      */
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      formData.append("payload", JSON.stringify(this.partObj));
      return formData;
    },

    async createPropertyPart() {
      try {
        this.loadingAction = true;
        /*
                const { sizeWidth, sizeHeight, sizeDepth } = this.partObj;
                this.partObj.size = `${sizeWidth|| ''}x${sizeDepth|| ''}x${sizeHeight|| ''}`;
        */
        const arr = await this.customFieldList.map((item) => ({
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
        console.log(this.partObj, "this.partObjthis.partObj");
        // const resPart = await axios.post('/api/parts', this.partObj)
        const resPart = await axios.post(
          "/api/parts/add-multipart",
          this.partFormData(),
          this.multipartHeader()
        );
        const objectId = resPart.data.id;
        await axios.post(
          `/api/custom-field-value/edit-list/${objectId}`,
          params
        );
        this.createToolingSuccess(
          this.partObj.partCode
            ? this.partObj.partCode.trim()
            : this.partObj.partCode
        );
        this.onClose();
        Common.alert(`New ${this.nameDrawer} has been successfully created.`);
      } catch (e) {
        console.log(e);
        let message = e.response.data;
        if (message === "Part Name is already registered in the system.") {
          this.errorName = true;
        }
        if (message === "Part ID is already registered in the system.") {
          this.errorId = true;
        }
        // Common.alert(e.response.data);
        // Common.alert("Fail!");
      } finally {
        this.loadingAction = false;
      }
    },
    async createProperty() {
      try {
        this.loadingAction = true;
        const params = {
          objectType: this.propertyType,
          fieldName: this.configName.trim(),
          required: this.isConfigRequired === this.requiredTypeOptions.REQUIRED,
          defaultInput: this.isConfigManual === this.valueTypeOptions.DEFAULT,
          defaultInputValue: this.defaultValue.trim(),
        };
        this.propertyType === "TOOLING"
          ? (params.propertyGroup = "TOOLING_" + this.propertyGroup)
          : "";
        const success = await axios.post("/api/custom-field", params);
        this.resetDrawer();
        this.onClose();
        this.createSuccess();
        this.$emit("reload");
        if (!this.isMatchingColumn) {
          Common.alert("A new property has been successfully created.");
        }
      } catch (e) {
        console.log(e);
        if (
          e.response.data ===
          "Property Name is already registered in the system."
        ) {
          this.errorFieldName = true;
        }
      } finally {
        this.loadingAction = false;
      }
    },
    resetDrawer() {
      this.thirdFiles = [];
      this.propertyType = this.isType;
      this.errorFieldName = false;
      this.errorEmail = false;
      this.errorId = false;
      this.errorName = false;
      this.configName = "";
      this.isConfigRequired = "OPTIONAL";
      this.isConfigManual = "MANUAL";
      this.defaultValue = "";
      this.companyBoolean = false;
      this.companyValue = 0;
      this.locationBoolean = false;
      this.locationValue = 0;
      this.categoryBoolean = false;
      this.categoryValue = 0;
      this.partBoolean = false;
      this.partValue = 0;
      this.toolingBoolean = false;
      this.toolingValue = 0;
      this.machineBoolean = false;
      this.machineValue = 0;
      this.startDate = null;
      this.usersList = [];
    },
    createToolingSuccess(value) {
      this.$emit("create-tooling-success", value);
    },
    createSuccess() {
      this.$emit("create-success");
    },
    onClose() {
      this.$emit("close");
      (this.locationObj = {
        company: {
          id: null,
          name: null,
        },
        locationName: null,
        locationId: null,
        address: null,
        memo: null,
      }),
        (this.partObj = {
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
        }),
        (this.toolmakerObj = {
          companyName: null,
          companyId: null,
          address: null,
          manager: null,
          phoneNumber: null,
          email: null,
          memo: null,
        }),
        (this.supplierObj = {
          companyName: null,
          companyId: null,
          address: null,
          manager: null,
          phoneNumber: null,
          email: null,
          memo: null,
        }),
        this.resetDrawer();
    },
    isValidField() {
      if (this.companyBoolean) {
        if (isNaN(this.companyValue)) {
          return false;
        } else {
          if (+this.companyValue <= 0) {
            return false;
          }
        }
      }
      if (this.locationBoolean) {
        if (isNaN(this.locationValue)) {
          return false;
        } else {
          if (+this.locationValue <= 0) {
            return false;
          }
        }
      }
      if (this.categoryBoolean) {
        if (isNaN(this.categoryValue)) {
          return false;
        } else {
          if (+this.categoryValue <= 0) {
            return false;
          }
        }
      }
      if (this.partBoolean) {
        if (isNaN(this.partValue)) {
          return false;
        } else {
          if (+this.partValue <= 0) {
            return false;
          }
        }
      }
      if (this.toolingBoolean) {
        if (isNaN(this.toolingValue)) {
          return false;
        } else {
          if (+this.toolingValue <= 0) {
            return false;
          }
        }
      }
      if (this.machineBoolean) {
        if (isNaN(this.machineValue)) {
          return false;
        } else {
          if (+this.machineValue <= 0) {
            return false;
          }
        }
      }
      return true;
    },
  },
};
</script>

<style scoped>
.errorFieldName {
  border-color: #ef4444;
  /*color: #EF4444;*/
  /*font-weight: 500;*/
  margin-top: 1px;
}
.choosing-item span {
  font-size: 12px !important;
}
.choosing-item {
  margin-right: 22px !important;
  width: 70px !important;
}
</style>
<style>
.create-property-btn-disable,
.create-property-btn-disable:hover,
.create-property-btn-disable:active {
  height: unset;
  font-size: 15px;
  padding: 6px 8px;
  border-radius: 3px;
  background: #c4c4c4 !important;
  border-color: #c4c4c4 !important;
  color: #ffffff;
  cursor: auto;
}
.create-property-btn {
  background: #3491ff;
  border-color: #3491ff;
  height: unset;
  font-size: 15px;
  padding: 6px 8px;
  border-radius: 3px;
  color: #ffffff;
}
.create-property-btn:hover {
  background: #3585e5;
  border-color: #3585e5;
  color: #ffffff;
}
.create-property-btn:active,
.create-property-btn:focus {
  background: #3585e5;
  border: 1px solid #daeeff;
  color: #ffffff;
}
.cancel-btn-drawer {
  font-size: 15px;
  color: #595959;
  border: 1px solid #d6dade;
  padding: 6px 8px;
  margin-right: 8px;
  border-radius: 3px;
}
.cancel-btn-drawer:hover {
  background-color: #f4f4f4;
  border: 1px solid #4b4b4b;
  color: #4b4b4b;
}
.cancel-btn-drawer:active {
  background-color: #f4f4f4;
  border: 1px solid #d6dade;
  color: #4b4b4b;
}
.category-drawer .anticon-close {
  color: #000000 !important;
}
.create-field-category .ant-select-selection-selected-value {
  font-size: 15px;
}
.category-drawer .ant-drawer-header {
  padding: 0px !important;
  background: #52a1ff !important;
  padding-top: 10px !important;
  border-top-left-radius: 8px;
}
.category-drawer .ant-drawer-title {
  background-color: #f5f8ff;
  padding-left: 13px;
  padding-top: 10px;
  padding-bottom: 10px;
  color: #4b4b4b;
  font-size: 16px !important;
  font-weight: 600;
}
.category-drawer .ant-drawer-content {
  border-radius: 10px 0 0 0;
}
.create-data-completion .ant-drawer-wrapper-body {
  border-radius: 8px 0 0 0;
}
.create-data-completion .ant-drawer-header {
  display: none;
}
.category-drawer .ant-dropdown-trigger {
  width: fit-content !important;
}
.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}
.data-completion-modal-title {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 26px 11.5px 26px;
}
.data-completion-modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
}
.select-all:hover {
  color: #0066df !important;
}
.btn-custom-close-list.close-list-item {
  border-radius: 1px;
  background: #fff;
  background-color: #fff;
  border: 1px solid #fff;
  color: #e51616 !important;
}
.close-list-item.active-list {
  background: #fff !important;
}
.list-zone .list-zone_item .close-list-item {
  background: #e8e8e8;
  border-radius: 2px;
  display: none;
  padding: 0 !important;
  width: 14px;
  /*align-items: center;*/
  /*justify-content: center;*/
  height: 14px;
}
.list-zone .list-zone_item .close-list-item svg {
  position: absolute;
  right: 50%;
  top: 50%;
  transform: translate(50%, -50%);
}
.list-zone {
  border: 1px solid #c1c7cd;
  height: 118px;
  overflow: auto;
  padding: 9px 10px;
  margin-top: 9px;
}
.list-zone .list-zone_item:hover {
  color: #000000;
}
.list-zone .list-zone_item {
  font-size: 13px;
  color: #4b4b4b;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}
.list-zone .list-zone_item:hover .close-list-item {
  display: block;
  position: relative;
}
/* width */
.list-zone ::-webkit-scrollbar {
  width: 3px;
  height: 7px;
}

/* Track */
.list-zone ::-webkit-scrollbar-track {
  background: transparent;
}

/* Handle */
.list-zone ::-webkit-scrollbar-thumb {
  background: #707070 !important;
  border-radius: 10px;
}

/* Handle on hover */
.list-zone ::-webkit-scrollbar-thumb:hover {
  background: #707070 !important;
}
.order-zone {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
}
.order-zone .order-item {
  width: calc(50% - 11.4px);
  overflow: hidden;
  font-size: 14px;
  display: flex;
  color: #4b4b4b;
  align-items: center;
  justify-content: flex-start;
  padding: 10px 0 9px 7px;
  cursor: pointer;
}
.order-zone .order-item:not(.selected) {
  border: 1px dashed #d9d9d9;
  /*background-image: url("data:image/svg+xml,%3csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3e%3crect width='100%25' height='100%25' fill='none' stroke='%232342' stroke-width='4' stroke-dasharray='10' stroke-dashoffset='5' stroke-linecap='round'/%3e%3c/svg%3e");*/
}
.order-zone .order-item:not(:nth-child(5)),
.order-zone .order-item:not(:nth-child(6)) {
  margin-bottom: 14px;
}
.order-zone .order-item:nth-child(odd) {
  margin-right: 22.8px;
}
.order-zone .order-item:hover {
  background: rgba(172, 210, 255, 26%);
}
.order-zone .order-item:hover .pretty .state label:after {
  background: rgb(52 145 255 / 26%);
}
.order-zone .order-item:hover .pretty .state label:before {
  border-color: rgb(52 145 255 / 26%);
}
.order-zone .order-item.selected {
  background: #acd2ff;
  border: 1px solid #3491ff;
  border-radius: 3px;
}
.ant-tooltip-placement-bottom .ant-tooltip-arrow,
.ant-tooltip-placement-bottomLeft .ant-tooltip-arrow,
.ant-tooltip-placement-bottomRight .ant-tooltip-arrow {
  border-bottom-color: #4b4b4b;
}
.ant-tooltip {
  z-index: 5001;
}
.ant-tooltip-inner {
  /*create property*/
  background-color: #4b4b4b !important;
  color: #fff;
  /*border-radius: 0;*/
  padding: 0;
  box-shadow: none;
}
.ant-select-dropdown-menu {
  padding: 10px 0px;
}
.ant-select-dropdown-menu-item-selected,
.ant-select-dropdown-menu-item-selected:hover {
  /*color: rgba(0,0,0,.65);*/
  font-weight: 400;
  background-color: #fff;
}
.ant-select-dropdown-menu-item:hover,
.ant-select-dropdown-menu-item-active {
  background-color: #e6f7ff;
}
.ant-tooltip-inner {
  background-color: #4b4b4b !important;
  color: #fff;
}
.input-group-request input {
  box-sizing: border-box;
  margin: 0;
  outline: none;
}

.input-group-request input[type="button"] {
  -webkit-appearance: button;
  cursor: pointer;
}

.input-group-request input::-webkit-outer-spin-button,
.input-group-request input::-webkit-inner-spin-button {
  -webkit-appearance: none;
}

.input-group-request {
  width: 66px;
  clear: both;
  position: relative;
  display: inline-block;
}

.input-group-request input[type="button"] {
  background-color: #eeeeee;
  min-width: 38px;
  width: auto;
  transition: all 300ms ease;
}

.input-group-request .button-minus,
.input-group-request .button-plus {
  font-weight: bold;
  height: 25px;
  padding: 0;
  width: 38px;
  position: relative;
}

.input-group-request .quantity-field {
  position: absolute;
  width: 17px;
  height: 25px;
  left: 0;
  top: 0;
  text-align: center;
  font-weight: 400;
  border-radius: 2px;
  font-size: 17px;
  line-height: 100%;
  resize: vertical;
  background: #909090;
  border: 1px solid #909090;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.input-group-request .quantity-field.active {
  background: #52a1ff !important;
  border: 1px solid #52a1ff;
  cursor: pointer;
}
.input-group-request .quantity-field.active:hover {
  background: #3994ff !important;
  border: 1px solid #3994ff !important;
}
.input-group-request .button-plus {
  right: 0;
  left: unset;
}

.input-group-request input[type="number"] {
  -moz-appearance: textfield;
  -webkit-appearance: none;
}
.custom-request-input {
  cursor: pointer;
  height: 25px;
  width: 66px;
  text-align: center;
  font-size: 11px;
  border-radius: 2px;
  border: 1px solid #909090;
  color: #4b4b4b;
  padding: 0 17px;
}
.custom-request-input:hover {
  border: 1px solid #4b4b4b;
}

.custom-request-input:focus {
  border: 2px solid #98d1fd;
}
.pretty.p-default input:checked ~ .state label:after {
  background-color: #0075ff !important;
  background: #0075ff !important;
}
.pretty.p-round {
}
.pretty .state label {
  margin-left: 1px;
}
.pretty .state label:before,
.pretty .state label:after {
  width: 12px;
  height: 12px;
}
.order-item .pretty .state label:after {
  width: 18px !important;
  height: 18px !important;
  left: -1px;
  top: -1px !important;
}
.order-item .pretty .state label:before,
.order-item .pretty .state label:after {
  width: 16px;
  height: 16px;
  top: calc((0% - (100% - 16px)) - 11%);
}
.order-item .pretty .state label {
  text-indent: unset !important;
  cursor: pointer;
}
.order-item .pretty {
  display: block;
}
.pretty .state label:after {
  transform: scale(0.5) !important;
}
:not(.p-round) .state label:before,
:not(.p-round) .state label:after {
  border-radius: 1px;
}
.p-round .state label:before {
  border: 0.7px solid #909090;
}
.pretty .state label:before {
  border-color: #909090;
}
.pretty:hover .state label:before {
  border-color: #4b4b4b;
}
.pretty.p-default input:checked ~ .state label:before {
  border-color: #0075ff !important;
  /*border-radius: 1px;*/
}
.pretty span {
  margin-left: 0;
}

.pretty {
  margin-right: unset;
}
.data-request-item {
  display: inline-flex;
  align-items: center;
}
.data-request-item:not(:last-child) {
  margin-bottom: 31.41px;
}
.dropdown_button-ab {
  position: absolute;
  top: 50%;
  right: -2px;
  transform: translate(0, -50%);
  /*margin-right: -1px;*/
  height: calc(100% + 2px);
  width: 250px !important;
}
.custom-icon-filter {
  width: 275px !important;
  margin: 0 0 10px 0 !important;
}
.avatar-zone {
  display: flex;
  align-items: center;
  width: 275px;
  padding: 7px 10px;
  overflow: scroll;
  background-image: url("data:image/svg+xml,%3csvg width='100%25' height='100%25' xmlns='http://www.w3.org/2000/svg'%3e%3crect width='100%25' height='100%25' fill='none' stroke='%232342' stroke-width='4' stroke-dasharray='10' stroke-dashoffset='5' stroke-linecap='round'/%3e%3c/svg%3e");
}
.avatar-item {
  width: 33px;
  height: 33px;
  padding: 1px;
  font-weight: 500;
  font-size: 15px;
  background: #aeacf9;
  border-radius: 99%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: capitalize;
}
.avatar-item-w:not(:last-child) {
  margin-right: 7px;
}
.date-zone {
  display: flex;
  align-items: center;
  margin-top: 8.84px;
}
.date-zone ::placeholder {
  font-size: 12px;
  color: #909090;
}
</style>
