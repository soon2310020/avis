<template>
  <base-dialog
    :title="isEdit ? 'Edit List' : 'New List'"
    :visible="isShow"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    @close="handleClose"
  >
    <div class="create-wo-container">
      <div class="position-relative">
        <div class="steps-zone">
          <div>
            <div class="step-content">
              <div class="form-group row line-row-wrapper">
                <label class="col-md-2 col-form-label">
                  {{ resources["list_id"] }}
                  <span class="badge-require"></span>
                </label>
                <div class="col-md-6">
                  <div>
                    <input
                      v-model="checklistCode"
                      type="text"
                      class="form-control"
                      :placeholder="resources['list_id']"
                    />
                  </div>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">
                  {{ resources["company"] }}:
                </span>
                <div class="col-md-9">
                  <div class="company-dropdown d-flex align-items-center">
                    <custom-dropdown-button
                      title="Add Company"
                      :is-selected="true"
                      :is-show="isVisibleCompanyDropdown"
                      @click="openAssetDropdown"
                    ></custom-dropdown-button>
                    <div
                      style="
                        height: 40px;
                        overflow-y: scroll;
                        width: calc(100% - 135px);
                        flex-wrap: wrap;
                      "
                      class="ml-2 d-flex align-items-center"
                    >
                      <selection-card
                        v-for="(item, index) in getSelectedCompany"
                        :key="index"
                        :selection="item"
                        @close="onRemoveCompany"
                      >
                      </selection-card>
                    </div>
                  </div>
                  <common-popover
                    @close="visibleAsset = false"
                    :is-visible="visibleAsset"
                    :style="{
                      width: '210px',
                      position: 'fixed',
                      marginTop: '4px',
                    }"
                  >
                    <common-select
                      :style="{ position: 'static', width: '100%' }"
                      :class="{ show: isVisibleCompanyDropdown }"
                      :items="companyIds"
                      :searchbox="companyIds.length >= 5"
                      :multiple="true"
                      :has-toggled-all="true"
                      @on-change="onChangeCompany"
                      placeholder="Search company"
                      @close="closeCompanyDropdown"
                    ></common-select>
                  </common-popover>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <span class="col-md-2 col-form-label">Individual User(s)</span>
                <div class="col-md-9">
                  <div class="d-flex align-items-end">
                    <custom-dropdown-button
                      title="Add User(s)"
                      :is-selected="true"
                      :is-show="selecteUserDropdown"
                      @click="openUserDropdown"
                    ></custom-dropdown-button>
                    <div class="ml-2">
                      <user-list-dropdown
                        v-if="getSelectedUser && getSelectedUser.length > 0"
                        :user-list="getSelectedUser"
                        index="1"
                      ></user-list-dropdown>
                    </div>
                  </div>
                  <common-popover
                    @close="closeUserDropdown"
                    :is-visible="selecteUserDropdown"
                    :style="{
                      width: '210px',
                      position: 'fixed',
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
                      placeholder="Search user name"
                    ></common-select>
                  </common-popover>
                </div>
              </div>
              <div class="form-group row line-row-wrapper">
                <label class="col-md-2 col-form-label">
                  List Type
                  <span class="badge-require"></span>
                </label>
                <div class="col-md-9 d-flex align-items-center">
                  <!--                    <p-radio v-model="listType" value="CHECK_LIST" :class="{'input-disabled': itemDuplicate?.objectType === 'PICK_LIST'}" style="margin-right: 50px">-->
                  <!--                      Checklist-->
                  <!--                    </p-radio>-->
                  <!--                    <p-radio v-model="listType" :class="{'input-disabled': itemDuplicate?.objectType === 'CHECK_LIST'}" value="PICK_LIST">-->
                  <!--                      Picklist-->
                  <!--                    </p-radio>-->
                  <div
                    class="form-check form-check-inline"
                    style="margin-right: 50px"
                  >
                    <label class="form-check-label d-flex align-items-center">
                      <input
                        type="radio"
                        v-model="listType"
                        class="form-check-input"
                        value="CHECK_LIST"
                        :disabled="itemDuplicate?.objectType === 'PICK_LIST'"
                      />
                      <span>Checklist</span>
                    </label>
                  </div>
                  <div class="form-check form-check-inline">
                    <label class="form-check-label d-flex align-items-center">
                      <input
                        type="radio"
                        v-model="listType"
                        class="form-check-input"
                        value="PICK_LIST"
                        :disabled="itemDuplicate?.objectType === 'CHECK_LIST'"
                      />
                      <span>Picklist</span>
                    </label>
                  </div>
                </div>
              </div>
              <div
                v-if="listType === 'CHECK_LIST'"
                class="form-group row line-row-wrapper"
              >
                <label class="col-md-2 col-form-label">
                  List Usage
                  <span class="badge-require"></span>
                </label>
                <div class="col-md-9">
                  <custom-dropdown
                    :list-item="listUsage"
                    :default-item="defaultListUsage"
                    :title-field="'name'"
                    @change="selectListUsage"
                  ></custom-dropdown>
                </div>
              </div>
              <div class="line-row-wrapper">
                <label class="col-form-label">
                  List
                  <span class="badge-require"></span>
                </label>
                <div
                  style="height: 200px; overflow-y: scroll; background: #fbfcfd"
                >
                  <div class="form-below">
                    <div
                      class="btn radio-confirm-modal"
                      style="gap: 0.5rem"
                      v-for="(addedChecklist, index) in checklists"
                      :key="'added-' + index"
                    >
                      <b>{{ index + 1 }}.</b>
                      <!-- <span v-if="focusChecklistIndex !== index && checklists[index].content" class="reason-edit-label" @click="focusChecklist(index)"> {{ checklists[index].content }}</span> -->
                      <input
                        v-if="
                          focusChecklistIndex !== index &&
                          checklists[index].content
                        "
                        type="text"
                        readonly
                        class="reason-edit-label"
                        @click="focusChecklist(index)"
                        v-model="checklists[index].content"
                      />
                      <input
                        v-else
                        v-model="checklists[index].tmpContent"
                        @blur="confirmChecklistWhenBlur(addedChecklist)"
                        @keydown.enter="addChecklist"
                        :ref="'checklist-' + index"
                        class="form-control reason-edit-input"
                        :class="{ focus: isFocusClass(index) }"
                        :placeholder="'Enter List Item #' + (index + 1)"
                        @keypress="inputChecklist($event, addedChecklist)"
                      />
                      <span class="checklist-action">
                        <a-tooltip color="white">
                          <template slot="title">
                            <div
                              style="
                                padding: 6px 8px;
                                font-size: 13px;
                                color: #3491ff;
                                background: #fff;
                                border-radius: 4px;
                                height: 32px;
                                box-shadow: 0px 0px 4px 1px #e5dfdf;
                              "
                            >
                              Enter List Item #{{ index + 1 }}
                              <div class="custom-arrow-tooltip"></div>
                            </div>
                          </template>
                          <a
                            :id="'enter' + index"
                            @click="
                              confirmChecklist('enter' + index, addedChecklist)
                            "
                            href="javascript:void(0)"
                            class="btn-custom btn-outline-custom-primary action-btn btn-custom__icon"
                            :class="[
                              checklists[index].tmpContent
                                ? 'has-content'
                                : 'disable',
                            ]"
                          >
                            <!-- <img
                                  width="11"
                                  height="11"
                                  class="image-duplicate"
                                  src="/images/icon/grey-check.svg"
                              /> -->
                            <span
                              style="
                                mask-image: url('/images/icon/grey-check.svg');
                                -webkit-mask-image: url('/images/icon/grey-check.svg');
                              "
                            ></span>
                          </a>
                        </a-tooltip>
                        <a-tooltip
                          v-if="checklists[index].content"
                          color="white"
                        >
                          <template slot="title">
                            <div
                              style="
                                padding: 6px 8px;
                                font-size: 13px;
                                color: #3491ff;
                                background: #fff;
                                border-radius: 4px;
                                height: 32px;
                                box-shadow: 0px 0px 4px 1px #e5dfdf;
                              "
                            >
                              Delete List Item #{{ index + 1 }}
                              <div class="custom-arrow-tooltip"></div>
                            </div>
                          </template>
                          <a
                            :id="'delete' + index"
                            @click="deleteChecklist(index, 'delete' + index)"
                            href="javascript:void(0)"
                            class="btn-custom btn-outline-custom-primary action-btn btn-custom__icon"
                            :class="{
                              'has-content': checklists[index].tmpContent,
                            }"
                          >
                            <!-- <img
                                  width="11"
                                  height="11"
                                  class="image-duplicate"
                                  src="/images/icon/grey-trash.svg"
                              /> -->
                            <span
                              style="
                                mask-image: url('/iamges/icon/grey-trash.svg');
                                -webkit-mask-image: url('/images/icon/grey-trash.svg');
                              "
                            ></span>
                          </a>
                        </a-tooltip>
                      </span>
                    </div>
                    <div
                      class="btn radio-confirm-modal"
                      style="color: #298bff; width: fit-content"
                      @click="addChecklist"
                    >
                      <span
                        style="
                          font-size: 12px;
                          font-weight: bold;
                          margin-right: 12px;
                        "
                        >+</span
                      >
                      <span>Add list item</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="d-flex modal__footer">
        <base-button
          :size="'medium'"
          :type="'cancel'"
          :level="'secondary'"
          :disabled="false"
          class="modal__footer__button mr-2"
          @click="handleClose()"
        >
          Cancel
        </base-button>
        <base-button
          :size="'medium'"
          :type="'normal'"
          :level="'primary'"
          :disabled="!getChecklists.length || !checklistCode"
          class="modal__footer__button"
          @click="createPicklist()"
        >
          Save
        </base-button>
      </div>
    </div>
    <toast-alert
      @close-toast="closeSuccessToast"
      :show="successToast"
      :title="getToastAlert.title"
      :content="getToastAlert.content"
    ></toast-alert>
  </base-dialog>
</template>

<script>
const defaultOptions = [{ title: "Daily", type: "DAILY", isRange: false }];
module.exports = {
  props: {
    isShow: {
      type: Boolean,
      default: () => false,
    },
    itemDuplicate: {
      type: Object,
      default: () => {},
    },
    createType: {
      type: String,
      default: "",
    },
    isEdit: {
      type: Boolean,
      default: () => false,
    },
    selectedItem: {
      type: Object,
      default: () => {},
    },
  },
  components: {
    "base-button": httpVueLoader("/components/@base/button/base-button.vue"),
    "common-popover": httpVueLoader("/components/@base/popover/popover.vue"),
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
    "selection-card": httpVueLoader(
      "/components/@base/select/selection-card.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "user-list-dropdown": httpVueLoader("/components/user-list-dropdown.vue"),
  },
  data() {
    return {
      isVisibleCompanyDropdown: false,
      isVisibleMaintenanceDropdown: false,
      isVisibleRejectRateDropdown: false,
      attachment: "",
      companyIds: [],
      userIds: [],
      details: "",
      cost: false,
      pickingList: false,
      moldIds: [],
      currentStep: [],
      resources: [],
      selecteUserDropdown: false,
      color: {
        HIGH: "red",
        MEDIUM: "yellow",
        LOW: "green",
        UPCOMING: "yellow",
        REQUESTED: "purple",
      },
      isAssetView: false,
      isChecklistView: false,
      visibleAsset: false,
      visibleChecklist: false,
      rawUserIds: [],
      rawMoldIds: [],
      rawCompanyIds: [],
      checklistMaintenanceList: [],
      checkListRejectRateList: [],
      successToast: false,
      toastAlert: {
        title: "Success!",
        content: "Your workorder has been created.",
      },
      listType: "CHECK_LIST",
      checklists: [],
      isFocusChecklistInput: false,
      focusChecklistIndex: null,
      checklistCode: null,
      checklistType: "MAINTENANCE",
      listUsage: [
        { name: "Maintenance", value: "MAINTENANCE" },
        { name: "Quality Assurance", value: "QUALITY_ASSURANCE" },
        { name: "Reject Rate", value: "REJECT_RATE" },
        { name: "General", value: "GENERAL" },
        { name: "Refurbishment", value: "REFURBISHMENT" },
        { name: "Disposal", value: "DISPOSAL" },
      ],
      defaultListUsage: { name: "Maintenance", value: "MAINTENANCE" },
    };
  },
  computed: {
    getToastAlert() {
      let result = { ...this.toastAlert };
      result.title = `Success!`;
      result.content = `Your work order has been created.`;
      return result;
    },
    getSelectedUser() {
      return _.filter(this.userIds, (o) => o.checked);
    },
    getSelectedCompany() {
      return _.filter(this.companyIds, (o) => o.checked);
    },
    getChecklists() {
      return this.checklists
        .filter((checklist) => checklist.isConfirmed)
        .map((checklist) => checklist.content);
    },
  },
  watch: {
    isShow(newVal) {
      if (newVal) {
        console.log(this.createType, "createTypecreateTypecreateType");
        if (this.createType === "picklist") {
          this.listType = "PICK_LIST";
        } else if (this.createType === "maintenance") {
          this.listType = "CHECK_LIST";
          this.checklistType = "MAINTENANCE";
          this.defaultListUsage = { name: "Maintenance", value: "MAINTENANCE" };
        } else if (this.createType === "quality_assurance") {
          this.listType = "CHECK_LIST";
          this.checklistType = "QUALITY_ASSURANCE";
          this.defaultListUsage = {
            name: "Quality Assurance",
            value: "QUALITY_ASSURANCE",
          };
        } else {
          this.listType = "CHECK_LIST";
          this.checklistType = "REJECT_RATE";
          this.defaultListUsage = { name: "Reject Rate", value: "REJECT_RATE" };
        }
        if (this.itemDuplicate) {
          this.duplicateChecklist();
        } else if (this.isEdit) {
          this.initEditModal();
        } else {
          this.resetModal();
        }
      }
    },
  },
  created() {
    this.getResources();
    this.getUsers();
    this.getCompanies();
    this.timeRange = {
      minDate: null,
      maxDate: new Date("2100-01-01"),
    };
  },
  async mounted() {
    this.addChecklist();
    this.addCurrentCompany();
  },
  methods: {
    initEditModal() {
      this.checklistCode = this.selectedItem.checklistCode;
      this.checklistType = this.selectedItem.checklistType;
      const selectedListUsage = this.listUsage.find(
        (item) => item.value === this.selectedItem.checklistType
      );
      this.defaultListUsage = selectedListUsage;
      this.listType = this.selectedItem.objectType;
      this.selectedItem.assignedCompanies.forEach((item) => {
        this.companyIds = this.companyIds.map((itemChild) =>
          itemChild.id === item.companyId
            ? { ...itemChild, checked: true }
            : itemChild
        );
      });
      this.selectedItem.assignedUsers.forEach((item) => {
        this.userIds = this.userIds.map((itemChild) =>
          itemChild.id === item.userId
            ? { ...itemChild, checked: true }
            : itemChild
        );
      });
      if (this.selectedItem.checklistItems.length) {
        this.checklists = this.selectedItem.checklistItems.map((item) => {
          return {
            content: item,
            tmpContent: item,
            isConfirmed: true,
          };
        });
        this.checklists.push({
          content: "",
          tmpContent: "",
          isConfirmed: false,
        });
        this.focusChecklistIndex = this.checklists.length - 1;
        this.isFocusChecklistInput = true;
        this.$nextTick(() => {
          this.$refs["checklist-" + (this.checklists.length - 1)][0].focus();
        });
      }
    },
    selectListUsage(item) {
      console.log(item.value, "item.value");
      this.checklistType = item.value;
    },
    duplicateChecklist() {
      if (this.itemDuplicate.checklistItems.length) {
        this.checklists = this.itemDuplicate.checklistItems.map((item) => {
          return {
            content: item,
            tmpContent: item,
            isConfirmed: true,
          };
        });
        this.checklists.push({
          content: "",
          tmpContent: "",
          isConfirmed: false,
        });
        this.focusChecklistIndex = this.checklists.length - 1;
        this.isFocusChecklistInput = true;
        this.$nextTick(() => {
          _.head(
            this.$refs["checklist-" + (this.checklists.length - 1)]
          )?.focus();
        });
      }
      this.checklistCode = null;
      this.checklistType = this.itemDuplicate.checklistType;
      const selectedListUsage = this.listUsage.find(
        (item) => item.value === this.itemDuplicate.checklistType
      );
      this.defaultListUsage = selectedListUsage;
      this.itemDuplicate.assignedCompanies.forEach((item) => {
        this.companyIds = this.companyIds.map((itemChild) =>
          itemChild.id === item.companyId
            ? { ...itemChild, checked: true }
            : itemChild
        );
      });
      this.itemDuplicate.assignedUsers.forEach((item) => {
        this.userIds = this.userIds.map((itemChild) =>
          itemChild.id === item.userId
            ? { ...itemChild, checked: true }
            : itemChild
        );
      });
      this.listType = this.itemDuplicate.objectType;
      this.$forceUpdate();
    },
    async getCompanies() {
      const response = await axios.get(
        `/api/companies?query=&size=9999&status=active&sort=id%2Cdesc`
      );
      this.rawCompanyIds = response.data.content.map((child) => ({
        ...child,
        title: child.name,
        checked: false,
      }));
      this.companyIds = JSON.parse(JSON.stringify(this.rawCompanyIds));
    },
    isFocusClass(index) {
      return this.isFocusChecklistInput && index === this.checklists.length - 1;
    },
    addChecklist: function () {
      this.checklists.push({
        content: "",
        tmpContent: "",
        isConfirmed: false,
      });
      this.focusChecklistIndex = this.checklists.length - 1;
      this.isFocusChecklistInput = true;
      this.$nextTick(() => {
        _.head(
          this.$refs["checklist-" + (this.checklists.length - 1)]
        )?.focus();
      });
    },
    async addCurrentCompany() {
      const me = await Common.getSystem("me");
      const currentUser = JSON.parse(me);
      this.companyIds = this.companyIds.map((itemChild) =>
        itemChild.id === currentUser.company.id
          ? { ...itemChild, checked: true }
          : itemChild
      );
    },
    focusOutChecklist: function (addedChecklist) {
      if (this.isNewChecklist(addedChecklist)) {
        return;
      }
      if (this.isValidAllAddedChecklist()) {
        this.saveChecklist();
      }
    },
    isNewChecklist(addedChecklist) {
      return !addedChecklist.isConfirmed;
    },
    inputChecklist: function (e, addedChecklist) {
      if (this.isNewChecklist(addedChecklist)) {
        return;
      }
      if (e.code === "Enter") {
        if (this.isValidAllAddedChecklist()) {
          this.saveChecklist();
          $(".reason-edit-input").blur();
        }
      }
    },
    deleteChecklist(index, id) {
      const self = this;
      const el = document.getElementById(id);
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        self.checklists.splice(index, 1);
        self.isFocusChecklistInput = false;
      }, 700);
    },
    focusChecklist(index) {
      if (this.focusChecklistIndex !== null) {
        let tmpContent = this.checklists[this.focusChecklistIndex].tmpContent;
        if (tmpContent && tmpContent.trim()) {
          this.checklists[this.focusChecklistIndex].content = tmpContent.trim();
        } else {
          this.checklists[this.focusChecklistIndex].tmpContent =
            this.checklists[this.focusChecklistIndex].content;
        }
      }
      this.focusChecklistIndex = index;
      this.$nextTick(() => {
        _.head(this.$refs["checklist-" + index])?.focus();
      });
    },
    confirmChecklist(id, addedChecklist) {
      const self = this;
      const el = document.getElementById(id);
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        if (!addedChecklist.tmpContent || !addedChecklist.tmpContent.trim()) {
          addedChecklist.tmpContent = addedChecklist.content;
          return;
        }
        addedChecklist.content = addedChecklist.tmpContent.trim();
        addedChecklist.isConfirmed = true;
        self.isFocusChecklistInput = false;
      }, 700);
    },
    confirmChecklistWhenBlur(addedChecklist) {
      if (!addedChecklist.tmpContent || !addedChecklist.tmpContent.trim()) {
        addedChecklist.tmpContent = addedChecklist.content;
        return;
      }
      addedChecklist.content = addedChecklist.tmpContent.trim();
      addedChecklist.isConfirmed = true;
      this.focusChecklistIndex = null;
      this.isFocusChecklistInput = false;
    },
    saveChecklist: function () {
      this.isFocusChecklistInput = false;
      this.checklists.forEach((reason) => {
        if (!reason.isConfirmed) {
          reason.isConfirmed = true;
        }
      });
    },
    isValidAllAddedChecklist() {
      for (let i = 0; i < this.checklists.length; i++) {
        if (!this.checklists[i].content) {
          return false;
        }
      }
      return true;
    },
    createPicklist() {
      const self = this;
      setTimeout(() => {
        const userSelected = self.getSelectedUser.map((user) => {
          return user.id;
        });
        const companySelected = self.getSelectedCompany.map((user) => {
          return user.id;
        });
        const params = {
          objectType: self.listType,
          userIds: userSelected,
          checklistItemValue: self.getChecklists,
          companyIds: companySelected,
          checklistCode: self.checklistCode,
        };
        if (self.listType === "CHECK_LIST") {
          params.checklistType = self.checklistType;
          delete params.objectType;
        } else {
          params.objectType = self.listType;
          delete params.checklistType;
        }
        if (this.isEdit) {
          axios
            .put(`/api/checklist/${this.selectedItem.id}`, params)
            .then((res) => {
              if (res.data.success) {
                self.handleClose();
                self.$emit("create-checklist-success");
              } else {
                Common.alert(res.data.message);
              }
            });
        } else {
          axios.post(`/api/checklist/create`, params).then((res) => {
            if (res.data.success) {
              self.handleClose();
              self.$emit("create-checklist-success");
            } else {
              Common.alert(res.data.message);
            }
          });
        }
      }, 700);
    },
    closeSuccessToast() {
      this.successToast = false;
    },
    getUsers() {
      axios
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
    openUserDropdown(value) {
      this.selecteUserDropdown = value;
    },
    closeUserDropdown() {
      this.selecteUserDropdown = false;
    },
    onChangeCompany(results) {
      this.companyIds = results;
    },
    onChangeUser(results) {
      this.userIds = results;
    },
    openAssetDropdown(value) {
      this.isAssetView = false;
      this.visibleAsset = value;
      this.isVisibleCompanyDropdown = value;
    },
    openChecklistDropdown() {
      this.isChecklistView = false;
      this.visibleChecklist = true;
    },
    closeRejectRateDropdown() {
      this.isVisibleRejectRateDropdown = false;
    },
    closeMaintenanceDropdown() {
      this.isVisibleMaintenanceDropdown = false;
    },
    closeCompanyDropdown() {
      this.isVisibleCompanyDropdown = false;
    },
    onRemoveMold(asset) {
      this.moldIds = this.moldIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    onRemoveCompany(asset) {
      this.companyIds = this.companyIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : item
      );
    },
    resetModal() {
      this.checklistCode = null;
      this.companyIds = JSON.parse(JSON.stringify(this.rawCompanyIds));
      this.userIds = JSON.parse(JSON.stringify(this.rawUserIds));
      this.checklists = [];
      this.addChecklist();
      this.addCurrentCompany();
      this.$forceUpdate();
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        this.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
    handleClose() {
      const self = this;
      setTimeout(() => {
        self.resetModal();
        self.$emit("close");
      }, 700);
    },
  },
};
</script>

<style scoped>
.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 25px;
  width: 25px;
}
.btn-custom.btn-custom__icon {
}
.btn-custom.btn-custom__icon span {
  display: inline-block;
  width: 15px;
  height: 15px;
  mask-size: contain;
  mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}

.btn-custom.btn-custom__icon.disable {
  background-color: transparent;
}
.btn-custom.btn-custom__icon.disable span {
  background-color: #c4c4c4;
}
.btn-custom.btn-custom__icon.has-content {
  background-color: #d9d9d9;
}
.btn-custom.btn-custom__icon.has-content span {
  background-color: #595959;
}
.btn-custom.btn-custom__icon.has-content:hover {
  background-color: #daeeff;
}
.btn-custom.btn-custom__icon.has-content:hover span {
  background-color: #3585e5;
}
.action-btn {
  /* background: #D9D9D9; */
  border-color: transparent;
}
.action-btn:hover {
  background: #daeeff;
}
/* .action-btn:hover img {
  filter: sepia(206%) hue-rotate(163deg) saturate(900%);
} */
.action-btn:hover {
  background: #daeeff;
}
.custom-dropdown {
  display: inline-block;
}
.create-wo-container {
  padding: 0 31px;
}
.col-form-label {
  min-width: 120px;
}
.modal__footer {
  margin-top: 80px;
  justify-content: flex-end;
}
.d-flex .modal__footer {
  margin-top: 0;
}
textarea:read-only {
  color: var(--grey-dark);
  background: var(--grey-5);
  border: none;
}
.company-dropdown {
  display: flex;
  align-items: end;
}
.company-dropdown .select-item-card {
  margin: 6px 8px 0 0 !important;
}
.reason-edit-input {
  /* width: 360px; */
  /* margin-left: 5px; */
  padding: 3px 5px;
  /* margin-right: 5px; */
}
.reason-edit-label {
  width: 50%;
  /* margin-left: 5px; */
  padding: 3px 5px;
  text-align: left;
  border-radius: 3px;
  border: 1px solid #909090;
  /* margin-right: 5px; */
  white-space: normal;
  overflow-wrap: break-word;
}
.reason-edit-input.focus {
  border: 1px solid #efefef;
}
.checklist-action {
  width: calc(100% - 360px);
  padding: 3px 5px;

  display: flex;
  justify-content: space-between;
}
.checklist-action .checklist-edit,
.checklist-action .checklist-confirm-edit {
  margin-right: 12px;
}
.checklist-action img {
  width: 15px;
}
.checklist-confirm-edit img {
  width: 18px;
}
.checklist-action .action-item .icon-hover {
  display: none;
}
.checklist-action .action-item:hover .icon-primary {
  display: none;
}
.checklist-action .action-item:hover .icon-hover {
  display: initial;
}
.form-group label span {
  text-transform: capitalize;
  font-weight: 400;
  font-size: inherit;
  line-height: 1.5;
}
.steps-content {
  max-height: unset !important;
}

.form-control {
  font-size: 14.66px;
}
.form-control.reason-edit-input {
  width: 50%;
}

.radio-confirm-modal input {
  font-size: 14.66px;
  line-height: 17px;
  padding: 6px 16px 6px 8px;
}
</style>
