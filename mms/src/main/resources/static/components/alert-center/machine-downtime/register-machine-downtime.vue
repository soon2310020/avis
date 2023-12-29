<template>
  <div
    id="register-machine-downtime"
    class="modal fade"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <div
            class="head-line"
            style="
              position: absolute;
              background: #52a1ff;
              height: 8px;
              top: 0;
              left: 0;
              width: 100%;
              border-radius: 0.3rem 0.3rem 0 0;
            "
          ></div>
          <div
            class="modal-title"
            id="title-part-chart"
            v-text="modalTitle"
          ></div>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body" style="padding: 0 48px 19px">
          <div class="downtime-header row">
            <div class="col-md-5">
              <div class="row downtime-header-item">
                <span
                  class="downtime-header-label col-md-4"
                  v-text="resources['machine_id']"
                ></span>
                <span class="downtime-header-content">{{
                  itemData.machineCode
                }}</span>
              </div>
            </div>
            <div class="col-md-7">
              <div class="row downtime-header-item">
                <span
                  class="downtime-header-label col-md-4"
                  v-text="resources['start_downtime']"
                ></span>
                <span class="downtime-header-content">{{ startTimeStr }}</span>
              </div>
              <div class="row downtime-header-item">
                <span
                  class="downtime-header-label col-md-4"
                  v-text="resources['end_downtime']"
                ></span>
                <span class="downtime-header-content">{{ endTimeStr }}</span>
              </div>
            </div>
            <div class="line-dropdown"></div>
          </div>
          <div
            class="downtime-body"
            :class="[
              isShowRejectReasonModel || isChangeType || numberDeleteReasons > 0
                ? 'hide'
                : 'show',
            ]"
            :style="modelType === 'REGISTER' ? { position: 'relative' } : {}"
          >
            <div class="row">
              <div class="col-md-6 downtime-date-time">
                <div class="downtime-radio-item">
                  <input
                    type="radio"
                    value="UNPLANNED"
                    id="unplanned"
                    name="unplanned"
                    :checked="planDowntimeType === 'UNPLANNED'"
                    v-model="planDowntimeType"
                    @click="changeType('UNPLANNED')"
                  />
                  <label
                    class="radio-downtime-label"
                    for="unplanned"
                    v-text="resources['unplanned_downtime']"
                  ></label>
                </div>
                <div class="downtime-radio-item">
                  <input
                    type="radio"
                    value="PLANNED"
                    id="planned"
                    name="planned"
                    :checked="planDowntimeType === 'PLANNED'"
                    v-model="planDowntimeType"
                    @click="changeType('PLANNED')"
                  />
                  <label
                    class="radio-downtime-label"
                    for="planned"
                    v-text="resources['planned_downtime']"
                  ></label>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-5">
                <div
                  class="downtime-body-item"
                  style="margin-top: 0px"
                  v-click-outside="closeReasonDropdown"
                >
                  <div class="downtime-body-label">
                    Reason<span class="badge-require"></span>
                  </div>
                  <a-button
                    @click="openShowDropDown"
                    class="choose-reason-btn"
                    >{{
                      reasonSelected
                        ? reasonSelected.title
                        : resources["click_enter_reason"]
                    }}</a-button
                  >
                  <div
                    v-if="showDropdown"
                    class="reason-downtime-dropdown"
                    tabindex="0"
                  >
                    <div class="reason-downtime-dropdown-content">
                      <div class="search-container">
                        <i class="fa fa-search icon-search"></i>
                        <input
                          type="text"
                          placeholder="Search Code Details"
                          @input="searchReason"
                        />
                      </div>
                      <span
                        v-for="(reason, index) in reasonSearchResult"
                        :key="index"
                        v-text="reason.title"
                        @click="selectReason(reason)"
                      ></span>
                    </div>
                  </div>
                </div>
                <div class="downtime-body-item" v-if="modelType !== 'REGISTER'">
                  <div class="downtime-body-label">
                    {{ resources["start_unplanned_downtime"]
                    }}<span class="badge-require"></span>
                  </div>
                  <div class="downtime-date-time">
                    <time-picker
                      :disabled="itemData.downtimeStatus === 'REGISTERED'"
                      :resources="resources"
                      :handle-change-time="handleChangeTime"
                      :type="'start'"
                      :is-show-dropdown="downtime.start.isShowDropDown"
                      :open-close-time-dropdown="openCloseTimeDropdown"
                      :handle-change-date="handleChangeDate"
                      :date-time="downtime.start"
                      :remove-error-mess="removeErrorMess"
                      :start-downtime="momentConvert(this.itemData.startTime)"
                      :end-downtime="momentConvert(this.itemData.endTime)"
                    />
                  </div>
                </div>
                <div class="downtime-body-item" v-if="modelType !== 'REGISTER'">
                  <div class="downtime-body-label">
                    {{ resources["end_unplanned_downtime"]
                    }}<span class="badge-require"></span>
                  </div>
                  <div class="downtime-date-time">
                    <time-picker
                      :disabled="itemData.downtimeStatus === 'REGISTERED'"
                      :resources="resources"
                      :handle-change-time="handleChangeTime"
                      :type="'end'"
                      :is-show-dropdown="downtime.end.isShowDropDown"
                      :open-close-time-dropdown="openCloseTimeDropdown"
                      :handle-change-date="handleChangeDate"
                      :date-time="downtime.end"
                      :remove-error-mess="removeErrorMess"
                      :start-downtime="momentConvert(this.itemData.startTime)"
                      :end-downtime="momentConvert(this.itemData.endTime)"
                    />
                  </div>
                </div>
                <div class="downtime-body-item">
                  <div class="downtime-body-label">
                    {{ resources["note"]
                    }}<span style="font-weight: normal; margin-left: 4px"
                      >(optional)</span
                    >
                  </div>
                  <textarea
                    :disabled="itemData.downtimeStatus === 'REGISTERED'"
                    rows="3"
                    style="width: 100%; border-color: #909090; padding: 7px"
                    :placeholder="resources['type_your_note']"
                    v-model="reasonNote"
                  ></textarea>
                </div>
                <div class="downtime-body-item">
                  <a-button
                    v-if="modelType !== 'REGISTER'"
                    :disabled="!enableSaveAlertReason"
                    @click="saveAlertReason"
                    class="btn-dropdown"
                    :class="[
                      enableSaveAlertReason ? 'save-alert-btn' : 'disableBtn',
                    ]"
                    v-text="resources['add_downtime_reason']"
                  >
                  </a-button>
                </div>
              </div>
              <div class="col-md-7" v-if="modelType !== 'REGISTER'">
                <div
                  class="downtime-body-item"
                  style="border-top: 0px; padding-left: 50px"
                >
                  <div
                    class="downtime-body-label"
                    v-text="resources['downtime_alert_summary']"
                  ></div>
                  <div style="height: 150px; overflow-x: scroll">
                    <table style="width: 100%">
                      <tr>
                        <th
                          style="padding-left: 0px"
                          v-text="resources['number']"
                        ></th>
                        <th v-text="resources['time']"></th>
                        <th
                          style="padding-right: 0px"
                          v-text="resources['reason']"
                        ></th>
                        <th class="trash-column"></th>
                      </tr>
                      <tr
                        v-for="(reason, index) in sortedReasonList"
                        :key="reason.id"
                      >
                        <td
                          style="padding-left: 0px"
                          v-text="
                            reason.codeData
                              ? `Reason ${index + 1}`
                              : 'UNASSIGNED'
                          "
                        ></td>
                        <td>
                          {{ getHoursString(reason.startTime) }} -
                          {{ getHoursString(reason.endTime) }}
                        </td>
                        <td
                          style="padding-right: 0px"
                          v-text="reason.codeData ? reason.codeData.title : '-'"
                        ></td>
                        <td class="trash-column">
                          <div
                            v-if="
                              reason.codeData &&
                              itemData.downtimeStatus !== 'REGISTERED'
                            "
                            class="icon-action trash-icon"
                            @click="deleteReason(index)"
                          ></div>
                        </td>
                      </tr>
                    </table>
                  </div>
                </div>
                <div
                  class="error-block"
                  v-if="isHaveUnassignedReason"
                  style="padding-left: 50px; padding-top: 56px"
                >
                  There is still some time unassigned to a reason.
                </div>
              </div>
            </div>
            <div class="error-block">{{ errorMessage }}</div>
            <a-button
              v-if="
                modelType === 'REGISTER' && isBtnAlertRegisterReasonPermitted
              "
              @click="submitDowntimeMachine()"
              style="
                margin-top: 40px;
                position: absolute;
                right: 0;
                bottom: 3px;
              "
              :disabled="!enableRegisterMachine"
              class="btn-dropdown"
              :class="[enableRegisterMachine ? 'confirm-btn' : 'disableBtn']"
              v-text="resources['register_downtime_reason']"
            >
            </a-button>
            <a-button
              v-if="
                modelType === 'EDIT' &&
                (isBtnAlertEditRegisteredReasonPermitted ||
                  isBtnHistoryLogEditDowntimeReasonPermitted)
              "
              @click="submitDowntimeMachine()"
              style="margin-top: 40px"
              :disabled="!enableEdit"
              class="btn-dropdown"
              :class="[enableEdit ? 'confirm-btn' : 'disableBtn']"
              v-text="resources['edit_downtime_reason']"
            >
            </a-button>
            <a-button
              v-if="
                modelType === 'CONFIRM' &&
                isBtnHistoryLogConfirmDowntimePermitted
              "
              @click="submitDowntimeMachine()"
              style="margin-top: 40px"
              :disabled="!enableConfirm"
              class="btn-dropdown"
              :class="[enableConfirm ? 'confirm-btn' : 'disableBtn']"
              v-text="resources['confirm_machine_downtime']"
            >
            </a-button>
          </div>
          <div
            class="reject-screen"
            :class="[isShowRejectReasonModel ? 'show' : 'hide']"
          >
            <div class="reject-screen-content">
              <div class="text-center reject-header">
                <div
                  class="warning-icon icon-action"
                  style="margin-left: 7px; margin-right: 3px; margin-top: 6px"
                ></div>
                Reject {{ numberReason }} downtime alert
                {{ numberReason > 1 ? "reasons" : "reason" }}?
              </div>
              <div>
                <div class="reject-label">Reject Downtime Alert Reason(s)</div>
                <textarea
                  class="reject-note"
                  rows="4"
                  placeholder="Type your reason for rejecting"
                  v-model="rejectReason"
                ></textarea>
              </div>
              <div style="float: right">
                <a-button
                  class="cancel-button"
                  @click="isShowRejectReasonModel = false"
                  >Cancel</a-button
                >
                <a-button class="confirm-btn" @click="rejectDowntimeMachine"
                  >Yes. Reject</a-button
                >
              </div>
            </div>
          </div>
          <div class="reject-screen" :class="[isChangeType ? 'show' : 'hide']">
            <div class="change-type-content">
              <div class="change-type-header">
                <div
                  class="warning-icon icon-action"
                  style="margin-left: 7px; margin-right: 3px; margin-top: 6px"
                ></div>
                Change Machine Downtime type?
              </div>
              This change will be applied to all reasons entered for this alert.
              <div style="float: right; margin-top: 20px">
                <a-button class="cancel-button" @click="cancelChangeType"
                  >Cancel</a-button
                >
                <a-button
                  class="confirm-btn"
                  style="margin-left: 40px"
                  @click="confirmChangeType"
                  >Yes. Change Type</a-button
                >
              </div>
            </div>
          </div>
          <div
            class="reject-screen"
            :class="[numberDeleteReasons > 0 ? 'show' : 'hide']"
          >
            <div class="change-type-content">
              <div class="change-type-header">
                <div
                  class="warning-icon icon-action"
                  style="margin-left: 7px; margin-right: 3px; margin-top: 6px"
                ></div>
                Delete {{ numberDeleteReasons }} downtime
                {{ numberDeleteReasons === 1 ? "reason" : "reasons" }}?
              </div>
              Once downtime reasons are deleted, the time period will be
              'UNASSIGNED'
              <div style="float: right; margin-top: 20px">
                <a-button class="cancel-button" @click="cancelDeleteReason"
                  >Cancel</a-button
                >
                <a-button
                  class="confirm-btn"
                  style="margin-left: 40px"
                  @click="confirmDeleteReason"
                  >Yes. Delete</a-button
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  components: {
    "time-picker": httpVueLoader(
      "/components/alert-center/machine-downtime/time-picker.vue"
    ),
  },
  props: {
    resources: Object,
  },
  data() {
    return {
      itemData: {},
      machineDowntimeData: {},
      modelType: null,
      showDropdown: false,
      reasonList: [],
      planDowntimeType: "",
      tempDowntimeType: "",
      reasonSelected: null,
      reasonSearchResult: [],
      registerData: {},
      reasonNote: "",
      rejectReason: "",
      isShowRejectReasonModel: false,
      downtime: {
        start: {
          date: null,
          hour: 0,
          minute: 0,
          isShowDropDown: false,
        },
        end: {
          date: null,
          hour: 0,
          minute: 0,
          isShowDropDown: false,
        },
      },
      downtimeReasonList: [],
      downtimeReasonShow: [],
      isChangeType: false,
      numberDeleteReasons: 0,
      tempReasonList: [],
      errorMessage: null,
      isBtnAlertEditRegisteredReasonPermitted: false,
      isBtnAlertRegisterReasonPermitted: false,
      isBtnHistoryLogConfirmDowntimePermitted: false,
      isBtnHistoryLogEditDowntimeReasonPermitted: false,
    };
  },
  computed: {
    modalTitle() {
      if (this.modelType === "REGISTER")
        return this.resources["register_machine_downtime"];
      if (this.modelType === "CONFIRM")
        return this.resources["confirm_machine_downtime"];
      return this.resources["edit_machine_downtime"];
    },
    startTimeStr() {
      if (this.itemData.startTime) {
        return this.formatToDateTime(this.itemData.startTime).slice(0, 16);
      }
    },
    endTimeStr() {
      console.log("endTimeStr", this.itemData.endTime);
      if (
        this.itemData.endTime &&
        this.instantToDate(this.itemData.endTime) < new Date()
      ) {
        return this.formatToDateTime(this.itemData.endTime).slice(0, 16);
      }
      return this.resources["ongoing"];
    },
    enableSaveAlertReason() {
      return (
        this.reasonSelected !== null &&
        (this.modelType === "REGISTER" ||
          this.itemData.downtimeStatus === "REGISTERED" ||
          (this.modelType !== "REGISTER" &&
            this.downtime.start.date !== null &&
            this.downtime.end.date !== null))
      );
    },
    enableRegisterMachine() {
      return this.reasonSelected !== null;
    },
    enableConfirm() {
      return (
        this.downtimeReasonShow.length > 0 &&
        this.downtimeReasonShow.every((item) => item.codeDataId)
      );
    },
    enableReject() {
      return false;
    },
    enableEdit() {
      return this.downtimeReasonShow.every((item) => item.codeDataId);
    },
    numberReason() {
      return this.machineDowntimeData.reasons
        ? this.machineDowntimeData.reasons.length
        : 0;
    },
    sortedReasonList() {
      return this.downtimeReasonShow.sort(
        (item1, item2) => item1.startTime - item2.startTime
      );
    },
    isHaveUnassignedReason() {
      return (
        this.downtimeReasonShow.length > 0 &&
        this.downtimeReasonShow.some((item) => !item.codeDataId) &&
        this.modelType !== "REGISTER"
      );
    },
  },
  methods: {
    removeErrorMess() {
      this.errorMessage = null;
    },
    changeType(type) {
      if (this.planDowntimeType !== type) {
        this.errorMessage = null;
        this.isChangeType = true;
        this.tempDowntimeType = type;
      }
    },
    confirmChangeType() {
      this.downtimeReasonShow = [];
      this.isChangeType = false;
    },
    cancelChangeType() {
      this.planDowntimeType =
        this.planDowntimeType === "UNPLANNED" ? "PLANNED" : "UNPLANNED";
      this.isChangeType = false;
    },
    instantToDate(value) {
      if (value) {
        if (!Common.localTimeZone && Common.timeZoneSever != null) {
          return moment.unix(
            value +
              (Common.timeZoneSever / 1000 +
                new Date().getTimezoneOffset() * 60)
          )._d;
        } else {
          return moment.unix(value)._d;
        }
      }
    },
    dateToInstant(value) {
      if (value) {
        if (!Common.localTimeZone && Common.timeZoneSever != null) {
          return (
            moment(value).unix() -
            (Common.timeZoneSever / 1000 + new Date().getTimezoneOffset() * 60)
          );
        } else {
          return moment(value).unix();
        }
      }
    },
    registerMachineDowntime() {
      this.downtimeReasonList = [
        {
          codeDataId: this.reasonSelected.id,
          note: this.reasonNote,
        },
      ];
    },
    rejectDowntimeMachine() {
      this.machineDowntimeData.rejectReason = this.rejectReason;
    },
    handleChangeDate(event, type) {
      this.errorMessage = null;
      if (this.downtime[type].date !== event) {
        this.downtime[type].date = event;
      }
      console.log("handleChangeDate", event, type);
    },
    openCloseTimeDropdown(type, isShow) {
      if (this.itemData.downtimeStatus === "REGISTERED") {
        this.errorMessage = null;
        this.downtime[type].isShowDropDown = false;
      } else {
        if (this.downtime[type].isShowDropDown !== isShow) {
          this.errorMessage = null;
          this.downtime[type].isShowDropDown = isShow;
        }
      }
    },
    handleChangeTime(time, type, hourType) {
      this.errorMessage = null;
      console.log("handleChangeTime", time, type, hourType);
      if (this.downtime[type][hourType] !== time) {
        this.downtime[type][hourType] = time;
      }
    },
    resetData() {
      this.downtime.start.date = null;
      this.downtime.start.hour = 0;
      this.downtime.start.minute = 0;
      this.downtime.end.date = null;
      this.downtime.end.hour = 0;
      this.downtime.end.minute = 0;
      this.reasonNote = "";
      this.reasonSelected = null;
      this.isShowRejectReasonModel = false;
      this.rejectReason = "";
      this.errorMessage = null;
      this.isChangeType = false;
      this.numberDeleteReasons = 0;
      this.tempReasonList = [];
    },
    resetSavedData() {
      const firstUnassigned = _.find(
        this.sortedReasonList,
        (item) => !item.codeData
      );
      if (firstUnassigned) {
        console.log("case 1");
        console.log("firstUnassigned", firstUnassigned);
        const start = this.instantToDate(firstUnassigned.startTime);
        this.downtime.start.date = this.momentConvert(
          firstUnassigned.startTime
        );
        this.downtime.start.hour = start.getHours();
        this.downtime.start.minute = start.getMinutes();

        const end = this.instantToDate(firstUnassigned.endTime);
        this.downtime.end.date = this.momentConvert(firstUnassigned.endTime);
        this.downtime.end.hour = end.getHours();
        this.downtime.end.minute = end.getMinutes();
      } else {
        console.log("case 2");
      }

      this.reasonNote = "";
      this.reasonSelected = null;
      this.isShowRejectReasonModel = false;
      this.rejectReason = "";
      this.errorMessage = null;
      this.isChangeType = false;
      this.numberDeleteReasons = 0;
      this.tempReasonList = [];
    },
    momentConvert(value) {
      if (value) {
        if (!Common.localTimeZone && Common.timeZoneSever != null) {
          return moment.unix(
            value +
              (Common.timeZoneSever / 1000 +
                new Date().getTimezoneOffset() * 60)
          );
        } else {
          return moment.unix(value);
        }
      }
    },
    deleteReason(index) {
      let reasons = [...this.downtimeReasonShow];
      reasons.map((reason, reasonIndex) => {
        if (reasonIndex === index) {
          delete reason.codeData;
          delete reason.codeDataId;
        }
      });
      const reason = reasons[index];

      let listIndexOfUnassignedPeriods = [];
      const sortedReasons = reasons.sort((a, b) => a.startTime - b.startTime);
      const mergedReasons = sortedReasons.reduce((prev, curr, currentIndex) => {
        const lastEl = prev[prev.length - 1];
        const isMatched = lastEl?.endTime === curr.startTime;
        const isUnassignedPeriod = !curr.codeData;
        // two unassigned period in sequence
        const isUnassignedSpan = !lastEl?.codeData && !curr.codeData;
        if (isUnassignedPeriod) {
          listIndexOfUnassignedPeriods.push(currentIndex);
        }
        if (lastEl && isMatched && isUnassignedSpan) {
          // merge case
          prev[prev.length - 1] = {
            startTime: lastEl.startTime,
            endTime: curr.endTime,
            note: "",
          };
          return prev;
        }
        return [...prev, curr];
      }, []);
      this.$nextTick(() => {
        this.downtimeReasonShow = mergedReasons;
        const newIndex = listIndexOfUnassignedPeriods[0];

        if (newIndex >= 0) {
          const __mergedReason = mergedReasons[newIndex];

          const startDate = this.instantToDate(__mergedReason.startTime);
          this.downtime.start.date = this.momentConvert(
            __mergedReason.startTime
          );
          this.downtime.start.hour = startDate.getHours();
          this.downtime.start.minute = startDate.getMinutes();

          const endDate = this.instantToDate(__mergedReason.endTime);
          this.downtime.end.date = this.momentConvert(__mergedReason.endTime);
          this.downtime.end.hour = endDate.getHours();
          this.downtime.end.minute = endDate.getMinutes();
        }
      });
    },
    selectReason(reason) {
      this.reasonSelected = reason;
      this.showDropdown = false;
    },
    convertInstantToInstant(value) {
      if (value) {
        let date = this.instantToDate(value);
        date.setHours(date.getHours(), date.getMinutes(), 0, 0);
        return this.dateToInstant(date);
      }
    },
    saveAlertReason() {
      if (this.itemData.downtimeStatus === "REGISTERED") {
        const reason = {
          codeData: this.reasonSelected,
          codeDataId: this.reasonSelected.id,
        };
        this.downtimeReasonShow = [reason];
        this.resetSavedData();
      } else {
        this.errorMessage = null;
        let unassignedReason = 0;
        const startTime = this.downtime.start.date._d;
        startTime.setHours(
          this.downtime.start.hour,
          this.downtime.start.minute,
          0,
          0
        );
        const endTime = this.downtime.end.date._d;
        endTime.setHours(
          this.downtime.end.hour,
          this.downtime.end.minute,
          0,
          0
        );
        const reason = {
          codeData: this.reasonSelected,
          codeDataId: this.reasonSelected.id,
          note: this.reasonNote,
          startTime: this.dateToInstant(startTime),
          endTime: this.dateToInstant(endTime),
        };
        if (reason.startTime > reason.endTime) {
          this.errorMessage =
            "Reason Start Downtime must be sooner than Reason End Downtime";
          return;
        }
        if (
          reason.startTime <
          this.convertInstantToInstant(this.itemData.startTime)
        ) {
          this.errorMessage =
            "Reason Start Downtime must be later than or equal to  Alert Start Downtime";
          return;
        }
        if (
          reason.endTime > this.convertInstantToInstant(this.itemData.endTime)
        ) {
          this.errorMessage =
            "Reason End Downtime must be sooner than or equal to Alert End Downtime";
          return;
        }
        console.log("reason", reason);
        if (this.downtimeReasonShow) {
          let reasonSorted = this.sortedReasonList;
          let reasons = [];
          for (let reasonItem of reasonSorted) {
            let compareStartTime = this.convertInstantToInstant(
              reasonItem.startTime
            );
            let compareEndTime = this.convertInstantToInstant(
              reasonItem.endTime
            );
            if (
              compareStartTime >= reason.startTime &&
              compareEndTime <= reason.endTime
            ) {
              if (reasonItem.codeDataId) {
                unassignedReason += 1;
              }
            } else if (
              compareStartTime >= reason.startTime &&
              compareStartTime < reason.endTime &&
              compareEndTime > reason.endTime
            ) {
              reasons.push({
                startTime: reason.endTime,
                endTime: compareEndTime,
              });
              if (reasonItem.codeDataId) {
                unassignedReason += 1;
              }
            } else if (
              compareStartTime < reason.startTime &&
              compareEndTime > reason.startTime &&
              compareEndTime <= reason.endTime
            ) {
              reasons.push({
                startTime: compareStartTime,
                endTime: reason.startTime,
              });
              if (reasonItem.codeDataId) {
                unassignedReason += 1;
              }
            } else if (
              compareStartTime < reason.startTime &&
              compareEndTime > reason.endTime
            ) {
              if (reasonItem.codeDataId) {
                unassignedReason += 1;
              }
              reasons.push({
                startTime: reasonItem.startTime,
                endTime: reason.startTime,
              });
              reasons.push({
                startTime: reason.endTime,
                endTime: reasonItem.endTime,
              });
            } else {
              reasons.push(reasonItem);
            }
          }
          reasons.push(reason);
          if (unassignedReason > 0) {
            this.tempReasonList = reasons;
            this.numberDeleteReasons = unassignedReason;
            return;
          } else {
            this.downtimeReasonShow = reasons;
            this.addRemainingReason();
            this.resetSavedData();
            return;
          }
        } else {
          this.downtimeReasonShow = [reason];
          this.addRemainingReason();
          this.resetSavedData();
        }
      }
    },
    addRemainingReason() {
      if (
        this.convertInstantToInstant(this.sortedReasonList[0].startTime) >
        this.convertInstantToInstant(this.itemData.startTime)
      ) {
        this.downtimeReasonShow.push({
          startTime: this.itemData.startTime,
          endTime: this.sortedReasonList[0].startTime,
        });
      }
      if (
        this.convertInstantToInstant(
          this.sortedReasonList[this.sortedReasonList.length - 1].endTime
        ) < this.convertInstantToInstant(this.itemData.endTime)
      ) {
        this.downtimeReasonShow.push({
          startTime:
            this.sortedReasonList[this.sortedReasonList.length - 1].endTime,
          endTime: this.itemData.endTime,
        });
      }
    },
    confirmDeleteReason() {
      this.downtimeReasonShow = this.tempReasonList;
      this.tempReasonList = [];
      this.numberDeleteReasons = 0;
      this.resetData();
    },
    cancelDeleteReason() {
      this.tempReasonList = [];
      this.numberDeleteReasons = 0;
    },
    processBeforeSubmitDowntime() {
      let reasonList = [];
      this.downtimeReasonShow.forEach((reason) => {
        reasonList.push({
          codeDataId: reason.codeDataId,
          startTime: reason.startTime,
          endTime: reason.endTime,
          note: reason.note,
        });
      });
      this.downtimeReasonList = reasonList;
    },
    submitDowntimeMachine() {
      console.log("submitDowntimeMachine", this.downtimeReasonShow);
      if ("REGISTER" === this.modelType) {
        this.registerMachineDowntime();
      } else {
        this.processBeforeSubmitDowntime();
      }
      const url =
        this.modelType === "CONFIRM"
          ? "/api/machine-downtime-alert/confirm"
          : "/api/machine-downtime-alert/reasons/update";
      axios
        .post(url, {
          machineDowntimeAlertId: this.itemData.id,
          downtimeType:
            this.planDowntimeType === "PLANNED"
              ? "PLANNED_DOWNTIME"
              : "UNPLANNED_DOWNTIME",
          items: this.downtimeReasonList,
        })
        .then((response) => {
          if (response) {
            this.resetData();
            console.log("this.parentType", this.parentType);
            this.$emit("after-submit");
            $("#register-machine-downtime").modal("hide");
          }
        });
    },
    searchReason(event) {
      const searchText = event.target.value;
      if (searchText) {
        this.reasonSearchResult = this.reasonList.filter((reason) =>
          reason.code.toLowerCase().includes(searchText.toLowerCase())
        );
      } else {
        this.reasonSearchResult = this.reasonList;
      }
    },
    openShowDropDown() {
      this.errorMessage = null;
      this.reasonSearchResult = this.reasonList;
      if (!this.showDropdown) {
        axios
          .get(
            `/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group1Code=${this.planDowntimeType}`
          )
          .then((response) => {
            if (response) {
              this.reasonList = response.data.content;
              this.reasonSearchResult = response.data.content;
              this.showDropdown = true;
            }
          });
      } else {
        this.showDropdown = false;
      }
      console.log(this.showDropdown, "this.showDropdown");
    },
    closeReasonDropdown() {
      this.showDropdown = false;
    },
    getHoursString(dateData) {
      if (dateData) {
        const date = this.instantToDate(dateData);
        const hour =
          date.getHours() > 9 ? date.getHours() : `0${date.getHours()}`;
        const minute =
          date.getMinutes() > 9 ? date.getMinutes() : `0${date.getMinutes()}`;
        return `${hour}:${minute}`;
      }
    },
    show(machineDowntimeAlert, type) {
      console.log("@show", machineDowntimeAlert, type);
      axios
        .get(`/api/machine-downtime-alert/reasons/${machineDowntimeAlert.id}`)
        .then((response) => {
          this.itemData = Object.assign(
            {},
            this.itemData,
            machineDowntimeAlert
          );
          this.modelType = type;
          this.tempDowntimeType = "";
          this.reasonSelected = null;
          this.reasonSearchResult = [];
          this.registerData = {};
          this.reasonNote = "";
          this.planDowntimeType =
            machineDowntimeAlert.downtimeType === "PLANNED_DOWNTIME"
              ? "PLANNED"
              : "UNPLANNED";
          this.downtimeReasonShow = response.data.data;
          this.isChangeType = false;
          this.resetData();
          $("#register-machine-downtime").modal("show");
        });
    },
  },
  created() {
    this.$watch(
      () => headerVm?.permissions,
      async () => {
        [
          this.isBtnAlertEditRegisteredReasonPermitted,
          this.isBtnAlertRegisterReasonPermitted,
          this.isBtnHistoryLogConfirmDowntimePermitted,
          this.isBtnHistoryLogEditDowntimeReasonPermitted,
        ] = await Promise.all([
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
            "btnAlertEditRegisteredReason"
          ),
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
            "btnAlertRegisterReason"
          ),
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
            "btnHistoryLogConfirmDowntime"
          ),
          Common.isMenuPermitted(
            Common.PERMISSION_CODE.ALERT_PRODUCTION,
            Common.PERMISSION_CODE.ALERT_CENTER_DOWNTIME_MACHINE,
            "btnHistoryLogEditDowntimeReason"
          ),
        ]);
      },
      { immediate: true }
    );
  },
  mounted() {
    console.log(this.isShow, "this.isShow mounted");
  },
  watch: {
    downtimeReasonList(newVal) {
      console.log("@watch:downtimeReasonList", newVal);
    },
    downtimeReasonShow(newVal) {
      console.log("@watch:downtimeReasonShow", newVal);
    },
    tempReasonList(newVal) {
      console.log("@watch:tempReasonList", newVal);
    },
  },
};
</script>

<style scoped>
.downtime-header {
  display: flex;
}

.choose-reason-btn {
  color: #3491fb;
  border-radius: 3px;
  background-color: #f5f5f5;
}

.downtime-header-label {
  color: #595959;
  font-weight: bold;
  font-size: 16px;
}

.downtime-header-content {
  color: #595959;
  font-size: 16px;
}

.line-dropdown {
  border-bottom: 1px solid #e2e2e2;
  width: 105%;
  margin-left: -23px;
  margin-right: -23px;
  height: 35px;
}

.downtime-header-item {
  padding-top: 15px;
}

.downtime-body {
  margin-top: 20px;
  height: 445px;
}

.downtime-radio-item {
  width: 50%;
}

.downtime-body-label {
  font-weight: bold;
  font-size: 14px;
  display: flex;
}

.choose-reason-btn {
  width: 100%;
}

.downtime-hour {
  width: 35%;
  background-color: #f5f5f5;
  height: 35px;
  padding: 7px 0px 7px 10px;
  margin-left: 5%;
  color: #3491fb;
}

.downtime-date {
  background-color: #f5f5f5;
  height: 35px;
  width: 60%;
  padding: 7px 0px 7px 10px;
  color: #3491fb;
  display: flex;
}

.downtime-date-time {
  display: flex;
}

th,
td {
  padding-top: 10px;
  padding-bottom: 10px;
  width: 30%;
}

.calendar-icon {
  background-image: url("/images/icon/new/calendar-icon.svg");
  margin-top: 5px;
  margin-right: 8px;
}

.downtime-body-item {
  margin-top: 10px;
}

.reason-downtime-dropdown {
  position: relative;
  width: 100%;
  display: block;
}

.reason-downtime-dropdown-content {
  position: absolute;
  display: block;
  width: 100%;
  background-color: #ffffff;
  box-shadow: 1px 3px 6px 3px #00000040;
  z-index: 999;
  max-height: 270px;
  overflow: scroll;
}

.search-container input {
  width: 90%;
  border: none;
}

.reason-downtime-dropdown-content span {
  display: block;
  width: 100%;
  height: 30px;
  padding: 5px;
}

.reason-downtime-dropdown-content span:hover {
  background-color: #e6f7ff;
  cursor: pointer;
}

.search-container {
  border: 1px solid #d6dade;
  margin: 5px;
  border-radius: 3px;
  display: flex;
}

.search-container input:hover,
.search-container input:focus {
  border: none !important;
  outline: none;
}

.icon-search {
  text-align: center;
  padding: 5px;
  background-color: #f5f5f5;
  color: #9d9d9d;
  width: 32px;
}

.trash-column {
  padding: 0px 0px 0px 10px;
}

.badge-require {
  display: block;
  width: 5px;
  height: 5px;
  border-radius: 5px;
  margin-top: 9px;
  margin-left: 4px;
  color: #fff;
  background-color: #f86c6b;
}

.save-alert-btn {
  border-color: #3491ff;
  color: #3491ff;
}

.save-alert-btn:hover {
  background-color: #daeeff;
  border-color: #daeeff;
  color: #3585e5;
}

.save-alert-btn:focus {
  border-color: #3491ff;
  background-color: #daeeff;
  color: #3585e5;
}

.cancel-button {
  border-color: #d6dade;
  color: #595959;
}

.cancel-button:hover {
  border-color: #595959;
  background-color: #f4f4f4;
}

.cancel-button:focus {
  border: 2px solid #d6dade;
  background-color: #f4f4f4;
}

.disableBtn,
disableBtn:hover,
disableBtn:focus {
  background-color: #c4c4c4 !important;
  border-color: #c4c4c4 !important;
  color: #ffffff !important;
}

.btn-dropdown {
  font-size: 14px;
  float: right;
}

.modal-title {
  font-size: 16px;
  color: #4b4b4b;
  font-weight: bold;
  line-height: 16px;
  margin-top: 8px;
}

.trash-column:hover {
  cursor: pointer;
}

.confirm-btn {
  background-color: #3491ff;
  border-color: #3491ff;
  color: #ffffff;
}

.confirm-btn:hover,
confirm-btn:focus {
  background-color: #3585e5;
  border-color: #3585e5;
}

.hide {
  display: none;
}

.reject-screen {
  height: 445px;
  margin-top: 20px;
  padding-top: 100px;
}

.reject-header {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.change-type-header {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  display: flex;
}

.reject-label {
  font-size: 16px;
  font-weight: bold;
  color: #595959;
}

.reject-screen-content {
  margin: auto;
  width: 45%;
}

.change-type-content {
  margin: auto;
  width: 33%;
}

.reject-note,
.reject-note:hover,
.reject-note:focus,
.reject-note:active {
  width: 100%;
  border: 1px solid #909090;
  outline: none !important;
}

.downtime-date-time .new-content-detail,
.downtime-date-time .ant-calendar-picker-input {
  width: 100%;
}

.error-block {
  padding-top: 38px;
  float: left;
  color: #d10000;
  font-size: 16px;
}
</style>
