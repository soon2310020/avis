<template>
  <div
    id="op-reject-rate-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-edit"
    aria-hidden="true"
    ref="modalRef"
  >
    <div
      class="modal-dialog modal-lg"
      role="document"
      :class="{ 'success-modal': isSuccess }"
    >
      <div class="modal-content">
        <div class="modal-title">
          <div class="head-line"></div>
          <div>
            <span class="span-title" v-text="resources['input_reject_rate']"></span>
          </div>
          <span
            data-dismiss="modal"
            @click="closeRejectRateModal"
            class="close-button"
          >
            <span class="t-icon-close"> </span>
          </span>
        </div>

        <div class="modal-body" :class="{ 'modal-body__success': isSuccess }">
          <div v-show="!isWarning && !isSuccess && !isWarningApplyTemplate">
            <!-- TOOLING TITLE -->
            <div
              v-if="type === MODAL_TYPE.TOOLING"
              class="modal-body__title modal-rj-tooling"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div class="form-item">
                  <span class="form-item__label"  v-text="resources['tooling_id']"> </span>
                  <span
                    class="form-item__value"
                    style="background-color: #fbfcfd; padding: 5px 90px 5px 8px"
                  >
                    {{ currentToolingName }}
                  </span>
                </div>
                <div class="date-picker">
                  <div class="">
                    <span class="form-item__label"> Time Range </span>
                    <span
                      class="form-item__value"
                      style="
                        background-color: #fbfcfd;
                        padding: 5px 90px 5px 8px;
                      "
                    >
                      {{ getDateTitle }}
                    </span>
                  </div>
                </div>
                <div></div>
              </div>
              <div class="form-item reject-rate-checklist d-flex">
                <span class="form-item__label" style="margin-right: 20px" v-text="resources['reject_rate_checklist']">
                </span>
                <custom-dropdown
                  :list-item="listRejectTemplate"
                  :title-field="'checklistCode'"
                  :default-title="defaultTitleCheckList === '' ? 'Select checklist' : defaultTitleCheckList"
                  @change="handleChangeRejectTemplate"
                ></custom-dropdown>
              </div>
            </div>

            <!-- MACHINE TITLE -->
            <div
              v-if="type === MODAL_TYPE.MACHINE"
              class="modal-body__title modal-rj-machine"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div class="form-item">
                  <span class="form-item__label" v-text="resources['machine_id']">  </span>
                  <span
                    class="form-item__value"
                    style="background-color: #fbfcfd; padding: 5px 90px 5px 8px"
                  >
                    {{ currentMachineCode }}
                  </span>
                </div>
                <div class="form-item">
                  <span class="form-item__label" v-text="resources['tooling_id']"></span>
                  <span
                    class="form-item__value"
                    style="background-color: #fbfcfd; padding: 5px 90px 5px 8px"
                  >
                    {{ currentToolingName }}
                  </span>
                </div>
                <div class="form-item reject-rate-checklist d-flex">
                  <span class="form-item__label" style="margin-right: 20px" v-text="resources['reject_rate_checklist']">
                  </span>
                  <custom-dropdown
                    :list-item="listRejectTemplate"
                    :title-field="'checklistCode'"
                    :default-title="'Select checklist'"
                    @change="handleChangeRejectTemplate"
                  ></custom-dropdown>
                </div>
                <div class="form-item date-picker">
                  <div>
                    <span class="form-item__label" v-text="resources['date']">  </span>
                    <span
                      class="form-item__value"
                      style="background-color: #fbfcfd"
                    >
                      <span>{{ dateRange }}</span>
                      <span style="margin-left: 27px">{{ timeRange }}</span>
                    </span>
                  </div>
                </div>
                <div class="form-item date-picker"></div>
                <div class="form-item" style="margin-bottom: 20px;">
                  <span class="form-item__label" v-text="resources['parts_produced']"></span>
                  <span
                    class="form-item__value"
                    style="background-color: #fbfcfd; padding: 5px 90px 5px 8px"
                  >
                    {{ totalPartProduced }}
                  </span>
                </div>
              </div>
            </div>

            <!-- MODAL LIST -->
            <div class="modal-body__list" style="border-radius: 2px; overflow: unset;">
              <reject-rate-reason-table
                ref="rejectRateTableRef"
                :sort-field="sortField"
                :is-desc="isDesc"
                :list-reject-rate-reason="listRejectRateReason"
                :on-change-reject-amount="handleChangeRejectAmount"
                :on-add-reason="handleAddReason"
                :on-remove-reason="handleRemoveReason"
                @update-total-amount="updateTotalAmount"
                @update-error-field="updateErrorField"
                @sort="sortReasonTable"
                :resources="resources"
                :trigger="trigger"
                :on-change-remark="handleChangeRemark"
              ></reject-rate-reason-table>
            </div>

            <!-- PAGINATION -->
            <div
              v-if="totalTooling > 1"
              class="modal-body__pagination d-flex justify-content-end"
            >
              <span>
                {{ currentToolingIndex + 1 }} of
                {{ listToolings.length }} {{resources['toolings_lower']}}
              </span>
              <a
                href="javascript:void(0)"
                ref="prev-page-btn"
                class="paging-button"
                :class="{ 'inactive-button': currentToolingIndex === 0 }"
                @click="handleChangePage('prev-page-btn', '-')"
              >
                <img src="/images/icon/category/paging-arrow.svg" alt="" />
              </a>
              <a
                href="javascript:void(0)"
                ref="next-page-btn"
                class="paging-button"
                :class="{
                  'inactive-button':
                    currentToolingIndex + 1 === listToolings.length,
                }"
                @click="handleChangePage('next-page-btn', '+')"
              >
                <img
                  src="/images/icon/category/paging-arrow.svg"
                  alt=""
                  style="transform: rotate(180deg)"
                />
              </a>
            </div>
            <div class="modal-body__action d-flex justify-content-end">
              <a
                ref="btn-submit"
                :class="{
                  'btn-submit-disable':
                    isErrorExist || totalTooling !== currentToolingIndex + 1,
                }"
                class="btn-submit"
                :loading="isInputting"
                :disabled="
                  isErrorExist || totalTooling !== currentToolingIndex + 1
                "
                @click="showBtnAnimation"
              >
                {{ totalTooling > 1 ? resources['submit_all']: resources['submit'] }}
              </a>
            </div>
          </div>

          <!-- warn -->
          <checklist-update-warning
            v-show="isWarningApplyTemplate"
            :on-cancel="handleCancelApply"
            :on-proceed="handleApplyTemplate"
          ></checklist-update-warning>

          <!-- status -->
          <!-- <success-zone
            v-show="isSuccess"
            :on-cancel="handleCancelSuccess"
          ></success-zone> -->
          <warning-zone
            v-show="isWarning"
            :list-reject-rate-empty="listRejectRateEmpty"
            :on-cancel="handleCancelWarning"
            :on-proceed="handleProceed"
            :is-inputting="isInputting"
          ></warning-zone>

          <!-- status -->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const MODAL_TYPE = {
  TOOLING: 'tooling',
  MACHINE: 'machine'
}

module.exports = {
  name: "RejectRateModal",
  components: {
    WarningZone: httpVueLoader(
      "/components/reject-rates/reject-rate-modal/warning-zone.vue"
    ),
    SuccessZone: httpVueLoader(
      "/components/reject-rates/reject-rate-modal/success-zone.vue"
    ),
    RejectRateReasonTable: httpVueLoader(
      "/components/reject-rates/components/reject-rate-reason-table.vue"
    ),
    DatePickerButton: httpVueLoader(
      "/components/reject-rates/components/date-picker-button.vue"
    ),
    ChecklistUpdateWarning: httpVueLoader(
      "/components/reject-rates/reject-rate-modal/checklist-update-warning.vue"
    ),
    CustomDropdown: httpVueLoader("/components/common/selection-dropdown/custom-dropdown.vue"),
  },
  props: {
    isVisible: {
      type: Boolean,
      default: false,
    },
    resources: {
      type: Object,
      default: () => ({}),
    },
    type: {
      type: String,
      default: 'tooling',
      validator(value) {
        return [MODAL_TYPE.TOOLING, MODAL_TYPE.MACHINE].includes(value)
      }
    },
    currentDate: {
      type: Object,
      default: null,
    },
    frequency: {
      type: String,
      default: '',
    },
    machineId: {
      type: [String, Number],
      default: () => ('')
    },
    machineCode: {
      type: String,
      default: () => ('')
    },
    onClose: {
      type: Function,
      default: () => { },
    },
    onPaging: {
      type: Function,
      default: () => { },
    },
    hour: {
      type: Object,
      default: () => ({
        from: '00',
        to: '01'
      })
    }
  },
  data() {
    return {
      modalRef: null,
      modalClassList: [],
      listToolings: [],
      currentToolingIndex: -1,
      sortField: "",
      _selectedRejectTemplate: "",
      selectedRejectTemplate: "",
      listRejectTemplate: [],
      listRejectRateReason: [],
      listRejectRateEmpty: [],
      isWarning: false,
      isSuccess: false,
      isInputting: false,
      isErrorExist: false,
      isDesc: true,
      isWarningApplyTemplate: false,
      totalProducedAmount: null,
      totalTableAmount: null,
      dateRange: '',
      timeRange: '',
      defaultTitleCheckList: '',
      trigger: 0,
      totalPartProduced: 0,
    };
  },
  computed: {
    totalTooling() {
      return this.listToolings.length;
    },
    currentToolingId() {
      if (this.listToolings) {
        return this.listToolings[this.currentToolingIndex]?._moldId;
      }
    },
    currentToolingName() {
      if (this.listToolings) {
        return this.listToolings[this.currentToolingIndex]?.name;
      }
    },
    currentMachineCode() {
      console.log('currentMachineCode', this.listToolings, this.currentToolingIndex)
      if (this.listToolings) {
        return this.listToolings[this.currentToolingIndex]?.machineCode
      }
    },
    getDateTitle() {
      let dateData = { ...this.currentDate };
      if (this.frequency.includes("MONTHLY")) {
        dateData.fromTitle = moment(dateData.from).format("MMMM, YYYY");
        dateData.toTitle = moment(dateData.to).format("MMMM, YYYY");
      } else if (this.frequency.includes("WEEKLY")) {
        let yearFrom = dateData.fromTitle.split("-")[0];
        let yearTo = dateData.toTitle.split("-")[0];
        let weekFrom = dateData.fromTitle.split("-")[1];
        let weekTo = dateData.toTitle.split("-")[1];
        dateData.fromTitle = `Week ${weekFrom}, ${yearFrom}`
        dateData.toTitle = `Week ${weekTo}, ${yearTo}`
      }
      else if (this.frequency.includes("HOURLY")) {
        return `${dateData.fromTitle}   ${this.hour.from}:00 to ${this.hour.to}:00`;
      }
      // if (this.isRange) {
      //   return `${dateData.fromTitle} - ${dateData.toTitle}`;
      // } else {
      //   return `${dateData.fromTitle}`;
      // }
      return `${dateData.fromTitle}`;
    },
    // getTimeRange() {
    //   if (this.listToolings) {
    //     return this.listToolings?.[0]?.timeRange
    //   }
    //   return ''
    // },
    // getDateRange() {
    //   if (this.listToolings) {
    //     return this.listToolings?.[0]?.dateRange
    //   }
    //   return ''
    // }
  },
  methods: {
    updateTotalAmount(value) {
      this.totalTableAmount = value
    },
    updateErrorField(errorField) {
      this.isErrorExist = errorField.length > 0;
    },
    // api
    async callInputRejectRateApi() {
      const payload = this.listToolings.map((item) => ({
        id: item.id,
        rejectedParts: item?.rejectedParts?.map((subitem) => ({
          reason: subitem.reason,
          rejectedAmount: Number(subitem.rejectedAmount) || 0,
          isDefault: subitem.isDefault ? subitem.isDefault : false,
          remark: subitem.remark,
        })) || [],
      }));
      this.isInputting = true;
      try {
        const res = await axios.post(`/api/rejected-part/register`, payload);
        this.isWarning = false;
        this.isSuccess = true;
        this.onPaging(1);
        this.handleCancelSuccess()
      } catch (error) {
        console.log("%c res", "background: red; color: white", error);
      } finally {
        this.isInputting = false;
      }
    },
    async fetchListRejectTemplate() {
      console.log('fetchListRejectTemplate')
      try {
        const res = await axios.get(
          `/api/checklist/checklist-mold-new?moldId=${this.currentToolingId}&checklistType=REJECT_RATE`
        );
        this.listRejectTemplate = res.data.data;
        // make shallow copy
        this._selectedRejectTemplate = this.listRejectTemplate[0]?.checklistCode || ''
        this.handleApplyTemplate()
      } catch (error) {
        console.log("%c res", "background: red; color: white", error);
      }
    },
    // animation
    // handle
    handleChangeRejectAmount(reasonName, value) {
      console.log('handleChangeRejectAmount125', reasonName, value)
      const reasonIndex = this.listToolings[this.currentToolingIndex].rejectedParts.findIndex(reason => reason.reason === reasonName);
      this.listToolings[this.currentToolingIndex].rejectedParts[
        reasonIndex
      ].rejectedAmount = value;
    },
    handleAddReason(reason) {
      this.listToolings[this.currentToolingIndex].rejectedParts.unshift(reason);
    },
    handleRemoveReason(reason) {
      console.log("reason", reason);
      console.log("listToolings", this.listToolings);
      const { rejectedParts } = this.listToolings[this.currentToolingIndex];
      this.listToolings[this.currentToolingIndex].rejectedParts =
        rejectedParts.filter((item) => item.reason !== reason);
      this.listRejectRateReason =
        this.listToolings[this.currentToolingIndex].rejectedParts;
    },
    showBtnAnimation() {
      const el = this.$refs['btn-submit'];
      if (el) {
        el.classList.add("btn-submit-animation");
        setTimeout(() => {
          el.classList.remove("btn-submit-animation");
          this.handleSubmit();
        }, 700);
      }
    },
    handleChangePage(ref, type) {
      this.defaultTitleCheckList = '';
      const el = this.$refs[ref];
      if (el) {
        el.classList.add("btn-submit-animation");
        setTimeout(() => {
          el.classList.remove("btn-submit-animation");
          if (type == "+") {
            this.currentToolingIndex++
          } else if (type == '-') {
            this.currentToolingIndex--
          }
        }, 700);
      }
      this.trigger++;
    },
    handleSubmit() {
      const listToolingsEmptyRejectRate = this.validateToolingsRejectRate();
      if (this.totalTableAmount > this.totalProducedAmount) {
        Common.alert(
          "The total number of rejected parts must be equal or smaller than produced parts"
        );
      } 
      // else if (listToolingsEmptyRejectRate.length === 0) {
        
      // }
       else {
        // this.isWarning = true;
        this.listRejectRateEmpty = listToolingsEmptyRejectRate.map(
          (item) => item.name
        );
        this.callInputRejectRateApi();
      }
    },
    validateToolingsRejectRate() {
      return this.listToolings.filter((item) => {
        return (
          !item.rejectedParts ||
          item?.rejectedParts?.some((subitem) => !subitem.rejectedAmount)
        );
      });
    },
    handleCancelWarning() {
      this.isWarning = false;
    },
    handleCancelSuccess() {
      this.$emit('on-success', this.machineCode, this.hour)
      this.closeRejectRateModal()
    },
    handleProceed() {
      this.callInputRejectRateApi();
    },
    handleChangeRejectTemplate(option) {
      console.log('handleChangeRejectTemplate')
      let value = option.checklistCode
      if (
        this.listToolings[this.currentToolingIndex]?.rejectedParts.length === 0
      ) {
        this.selectedRejectTemplate = value;
      } else {
        this.isWarningApplyTemplate = true;
        this._selectedRejectTemplate = value;
      }
    },
    handleApplyTemplate(isForce = false) {
      console.log('handleApplyTemplate')
      this.isWarningApplyTemplate = false;
      this.selectedRejectTemplate = this._selectedRejectTemplate;
      const isSelectedTemplate = (item) => item.checklistCode === this.selectedRejectTemplate
      const foundIndex = this.listRejectTemplate.findIndex(isSelectedTemplate);
      if (foundIndex > -1) {
        const { checklistItems } = this.listRejectTemplate[foundIndex];
        // manually overwrite
        const hasRejectedParts = this.listToolings[this.currentToolingIndex].rejectedParts.length > 0
        if (!hasRejectedParts || isForce) {
          this.defaultTitleCheckList = this._selectedRejectTemplate;
          this.listToolings[this.currentToolingIndex].rejectedParts = checklistItems.map((item) => ({
            reason: item,
            rejectedAmount: "",
            isDefault: true,
            remark: ""
          }));
        }
        this.listRejectRateReason = this.listToolings[this.currentToolingIndex].rejectedParts;
      }
    },
    handleCancelApply() {
      this.isWarningApplyTemplate = false;
      this._selectedRejectTemplate = this.selectedRejectTemplate;
    },
    // clean
    cleanEffect() {
      this.listToolings = [];
      this.currentToolingIndex = -1;
      this.sortField = "";
      this._selectedRejectTemplate = "";
      this.selectedRejectTemplate = "";
      this.listRejectTemplate = [];
      this.listRejectRateReason = [];
      this.listRejectRateEmpty = [];
      this.isWarning = false;
      this.isSuccess = false;
      this.isInputting = false;
      this.isErrorExist = false;
      this.isConfirmed = false;
      this.isWarningApplyTemplate = false;
    },
    clearData() {
      this.isWarning = false;
      this.isSuccess = false;
    },
    //
    sortReasonTable(sortField) {
      if (this.sortField != sortField) {
        this.sortField = sortField;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (this.sortField == "reason") {
        if (this.isDesc) {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) =>
            b.reason.localeCompare(a.reason)
          );
        } else {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) =>
            a.reason.localeCompare(b.reason)
          );
        }
      } else if (this.sortField === "remark") {
        if (this.isDesc) {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) => {
            const remarkA = a.remark == null ? '' : a.remark;
            const remarkB = b.remark == null ? '' : b.remark;

            return remarkB.localeCompare(remarkA)
          });
        } else {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) => {
            const remarkA = a.remark == null ? '' : a.remark;
            const remarkB = b.remark == null ? '' : b.remark;
            return remarkA.localeCompare(remarkB)
          });
        }
      } else {
        //sort number
        if (this.isDesc) {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) => {
            if (a.rejectedAmount == "" && b.rejectedAmount == "") {
              return 0;
            } else if (a.rejectedAmount == "" && b.rejectedAmount != "") {
              return 1;
            } else if (a.rejectedAmount != "" && b.rejectedAmount == "") {
              return -1;
            } else {
              return +a.rejectedAmount > +b.rejectedAmount ? -1 : 1;
            }
          });
        } else {
          this.listRejectRateReason = this.listRejectRateReason.sort((a, b) => {
            if (a.rejectedAmount == "" && b.rejectedAmount == "") {
              return 0;
            } else if (a.rejectedAmount == "" && b.rejectedAmount != "") {
              return -1;
            } else if (a.rejectedAmount != "" && b.rejectedAmount == "") {
              return 1;
            } else {
              return +a.rejectedAmount > +b.rejectedAmount ? 1 : -1;
            }
          });
        }
      }
    },
    openRejectRateModal(listCheckedToolings) {
      console.log('openRejectRateModal', listCheckedToolings)
      this.defaultTitleCheckList = '';
      $("#op-reject-rate-modal").on('click', this.handleClickOutside)
      if (this.type === MODAL_TYPE.MACHINE) {
        this.timeRange = listCheckedToolings[0]?.timeRange
        this.dateRange = listCheckedToolings[0]?.dateRange
      }

      // TODO: CHECK
      this.totalProducedAmount = listCheckedToolings?.[0]?.totalProducedAmount
      this.totalPartProduced = listCheckedToolings?.[0]?.totalPartProduced
      this.currentToolingIndex = 0
      $("#op-reject-rate-modal").modal("show");
      const _listChecked = JSON.parse(JSON.stringify(listCheckedToolings))
      this.listToolings = _listChecked.map((item) => {
        return {
          id: item.id,
          _moldId: item.miniMold.id,
          name: item.miniMold.equipmentCode,
          rejectedParts: item.rejectedPartDetails,
          machineCode: item.machineCode
        }
      })
      this.fetchListRejectTemplate();
      this.trigger++;
    },
    closeRejectRateModal() {
      const _rejectRateModal = $("#op-reject-rate-modal")
      _rejectRateModal.modal("hide");
      _rejectRateModal.off('click', this.handleClickOutside)
      setTimeout(() => this.cleanEffect(), 500)
    },
    handleClickOutside(event) {
      const container = document.querySelector('#op-reject-rate-modal .modal-content')
      const isClickOutsideModal = !$.contains(container, event.target)
      if (isClickOutsideModal) {
        this.closeRejectRateModal()
      }
    },
    handleChangeRemark(reasonName, value) {
      const reasonIndex = this.listToolings[this.currentToolingIndex].rejectedParts.findIndex(reason => reason.reason === reasonName);
      this.listToolings[this.currentToolingIndex].rejectedParts[reasonIndex].remark = value;
    }
  },
  watch: {
    currentToolingIndex(newVal) {
      newVal > -1 && this.fetchListRejectTemplate();
    },
    selectedRejectTemplate(newVal) {
      console.log('selectedRejectTemplate')
      const currentTooling = this.listToolings[this.currentToolingIndex];
      if (currentTooling && currentTooling?.rejectedParts) {
        const foundIndex = this.listRejectTemplate.findIndex(
          (item) => item.checklistCode === newVal
        );
        const checklistItems = this.listRejectTemplate[foundIndex]?.checklistItems;
        if (currentTooling.rejectedParts.length === 0 && checklistItems) {
          this.listToolings[this.currentToolingIndex].rejectedParts = checklistItems.map((item) => ({
            reason: item,
            rejectedAmount: "",
            remark: "",
          }));
        }
        // make shallow copy
        this.listRejectRateReason = this.listToolings[this.currentToolingIndex].rejectedParts;
      }
    },
  },
  created() {
    this.MODAL_TYPE = MODAL_TYPE
  }
};
</script>
<style>
.modal-dialog .modal-lg {
  max-width: 1024px !important;
}
.ant-input{
padding-left: 4px 11px !important;
}
</style>
<style scoped src="/components/reject-rates/reject-rate-modal.css"></style>
