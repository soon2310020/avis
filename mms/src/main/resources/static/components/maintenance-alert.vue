<template>
  <div
    id="op-maintenance-alert-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg-m" role="document">
      <div style="border-radius: 6px 6px 0 0;border: unset;" class="modal-content custom-modal-content">
        <div class="modal-body">
          <div class="modal-title">
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
            <span class="span-title" v-text="resources['maintenance_confirm']"></span>
            <span
                class="close-button"
                style="
                  font-size: 25px;
                  display: flex;
                  align-items: center;
                  height: 17px;
                  cursor: pointer;
                "
                @click="closeAlert()"
                aria-hidden="true"
            >
              <span class="t-icon-close"></span>
            </span>
          </div>

          <div style="padding: 25px 28px 24px 28px;">
            <label style="margin-bottom: 0px">
              <span v-text="resources['checklist']"></span>
              <span style="margin-top: 7px" class="badge-require"></span>
            </label>
            <div style="margin: 13px 0 16px 0px">
            <a-popover v-model="visible" placement="bottomLeft" trigger="click">
              <div>
                <a
                    href="javascript:void(0)"
                    style="margin-right: 34px;font-size: 14px;margin-left: 3px"
                    class="dropdown_button"
                    :class="{ 'clicked-button': visible }"
                    @mouseover="isHover = true"
                    @mouseleave="isHover = false"
                >
<!--                  <div style="max-width: 150px;overflow: hidden;text-overflow: ellipsis;display: -webkit-box;-webkit-box-orient: vertical;-webkit-line-clamp: 2;">-->
<!--                    {{ checkedItem.checklistCode }}-->
<!--                  </div>-->
                  <div style="max-width: 150px;">{{replaceLongtext(checkedItem.checklistCode, 17)}}</div>
                  <span v-if="isHover && !visible">
                  <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#3491ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-down"><polyline points="6 9 12 15 18 9"></polyline></svg>
                </span>
                  <span v-else-if="visible">
                  <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#3491ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-up"><polyline points="18 15 12 9 6 15"></polyline></svg>
                </span>
                  <span v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-down"><polyline points="6 9 12 15 18 9"></polyline></svg>
                </span>
                </a>
              </div>
              <div
                  slot="content"
                  style="padding: 8px 6px; max-height: 300px; overflow-y: scroll;min-width: 180px"
                  class="dropdown-scroll dropdown-scroll-maintenance"
              >
                <template v-for="item in checkList">
                  <a-col :key="item.id">
                    <a-tooltip placement="right">
                      <template slot="title">
                        <div style="padding: 6px 8px;">
                          {{item.checklistCode}}
                        </div>
                      </template>
                      <div class="custom-truncate-zone">
                        <p-radio v-model="checkedItem" :value="item">
                          {{item.checklistCode}}
                        </p-radio>
                      </div>
                    </a-tooltip>
                  </a-col>
                </template>
              </div>
            </a-popover>
            </div>
            <label style="padding-left: 0;margin-bottom: 0" class="btn radio-confirm-modal dropdown-scroll-maintenance" v-for="(originalReason, index) in checkedItem.checklistItems" :key="index">
<!--              <p-check v-model="reason" :value="originalReason">-->
<!--                  <span style="font-size: 14px;margin-left: 10px;">-->
<!--                    {{ originalReason }}-->
<!--                  </span>-->
<!--              </p-check>-->
              <!-- <a-tooltip placement="right">
                <template slot="title">
                  <div style="padding: 6px 8px;">
                    {{originalReason}}
                  </div>
                </template>
                <div class="custom-truncate-zone2">
                  <p-check v-model="reason" :value="originalReason">
                    <span style="font-size: 14px;margin-left: 10px;">
                      {{ originalReason }}
                    </span>
                  </p-check>
                </div>
              </a-tooltip> -->

              <p-check
                  v-if="originalReason && originalReason.length > 60"
                  v-model="reason" :value="originalReason"
                  v-bind:data-tooltip-text="originalReason"
              >
                    <span style="font-size: 14px;margin-left: 10px;">
                      {{ replaceLongtext(originalReason, 60) }}
                    </span>
                  </p-check>
                  <p-check v-else v-model="reason" :value="originalReason">
                    <span style="font-size: 14px;margin-left: 10px;">
                      {{ originalReason }}
                    </span>
                  </p-check>
            </label>
            <form action="" class="btn radio-confirm-modal" v-for="(addedReason, index) in addedReasons" :key="'added-' + index">
              <p-check  v-model="reason" :value="addedReason.content" style="align-self: baseline;margin-right: 12px;">

              </p-check>
<!--              <input
                  style="margin-right: 0px; width: 16px;height: 16px;"
                  type="checkbox"
                  name="options"
                  :ref="'checkbox-' + index"
                  :value="addedReason.content"
                  autocomplete="off"
                  v-model="reason"
              />
              -->
              <span class="reason-edit-label" v-if="focusReasonIndex !== index"> {{ addedReasons[index].content }}</span>
              <input v-else v-model="addedReasons[index].tmpContent" :id="'added-reason-' + index" :ref="'added-reason-' + index" class="form-control reason-edit-input" :class="{focus: isFocusClass(index)}" @keypress="inputReason($event, addedReason)" />
              <span class="checklist-action">
              <span class="checklist-edit action-item" v-if="focusReasonIndex !== index"  @click="focusReason(index)">
                <img src="/images/checklist/pencil.svg" class="icon-primary" alt="edit" />
                <img src="/images/checklist/pencil-hover.svg" class="icon-hover" alt="edit" />
              </span>
              <span class="checklist-delete action-item" v-if="focusReasonIndex !== index"  @click="deleteReason(index)">
                <img src="/images/checklist/trash.svg" class="icon-primary" alt="delete" />
                <img src="/images/checklist/trash-hover.svg" class="icon-hover" alt="delete" />
              </span>
              <button hidden type="submit" :ref="'btn-checklist-' + index"></button>
              <span class="checklist-confirm-edit" v-if="focusReasonIndex === index"  @click="confirmEditReason(addedReason, index)">
                <img src="/images/checklist/check.svg" alt="confirm-edit" />
              </span>
              <span class="checklist-cancel-edit" v-if="focusReasonIndex === index"  @click="cancelEditReason(addedReason, index)">
                <img src="/images/checklist/close.svg" alt="cancel-edit" />
              </span>
              <span class="checklist-confirm" v-if="false" @click="confirmReason(addedReason, index)">
                <img src="/images/icon/checked.svg" alt="checked" />
              </span>
            </span>
            </form>
<!--            <label class="btn radio-confirm-modal" style="color: #298BFF" @click="addReason" v-if="!isFocusReasonInput">-->
<!--              <span style="font-size: 12px; font-weight: bold; margin-right: 12px">+</span>-->
<!--              <span style="text-transform: unset">Add checklist item</span>-->
<!--            </label>-->
            <div style="margin-top: 8px">
              <a
                  v-if="!isFocusReasonInput"
                  @click="addReason('animation-reason')"
                  href="javascript:void(0)"
                  id="animation-reason"
                  class="invite-link"
              >
                <span style="font-size: 12px; font-weight: bold">+</span>
                <span style="text-transform: unset">Add checklist item</span>
              </a>
            </div>
            <div style="margin-left: 0;margin-top: 20px;" class="row">
              <div class="maintence-time-container">
                <label>
                  <span v-text="resources['start_time']"></span>
                  <span style="margin-top: 7px" class="badge-require"></span>
                </label>
              </div>
              <a-date-picker format="MMM Do, YYYY" v-model="startDate" :disabled-date="disabledDateStartTime"></a-date-picker>
              <a-time-picker
                  :disabled-hours="disabledHourStartTime"
                  :disabled-minutes="disabledMinuteStartTime"
                  style="margin-left: 10px;"
                  v-model="startHour"
                  placeholder="HH:mm"
                  format="HH:mm"
              ></a-time-picker>
            </div>
            <div style="margin-left: 0;margin-top: 20px;" class="row">
              <div class="maintence-time-container">
                <label>
                  <span v-text="resources['end_time']"></span>
                  <span style="margin-top: 7px" class="badge-require"></span>
                </label>
              </div>
              <a-date-picker format="MMM Do, YYYY" v-model="endDate" :disabled-date="disabledDateEndTime"></a-date-picker>
              <a-time-picker
                  :disabled-hours="disabledHourEndTime"
                  :disabled-minutes="disabledMinuteEndTime"
                  style="margin-left: 10px;"
                  v-model="endHour"
                  placeholder="HH:mm"
                  format="HH:mm"
              ></a-time-picker>
            </div>
            <div style="margin-left: 0;margin-top: 20px; align-items: center;" class="row">
              <label style="width: 100px; position: relative;">
                <span v-text="resources['document']"></span>
              <div style="margin-right: 3px;" class="op-upload-button-wrap">
                <button type="button" class="btn btn-outline-success">
                  <span v-text="resources['upload_files']"></span>
                </button>
                <input type="file" id="file" @change="selectedFile" multiple />
              </div>
              <div>
                <button
                    style="margin-top: 2px;text-overflow: ellipsis; max-width: 200px; overflow: hidden;"
                    v-for="(file, index) in files"
                    class="btn btn-outline-dark btn-sm mr-1"
                    @click.prevent="deleteFile(file, index)"
                >{{file.name}}</button>
              </div>
            </div>
          </div>
        </div>
        <div style="border-top: unset!important;" class="modal-footer">
<!--          <div-->
<!--            v-on:click="reInstallAction"-->
<!--            style="background: #c8ced3; color: #000; border: none;min-width: 80px;max-width: 80px;font-size: 14px;"-->
<!--            class="confirm-button"-->
<!--          v-text="resources['close']"></div>-->
          <a
              href="javascript:void(0)"
              id="close-request3"
              style="font-size: 15px;margin-right: 8px;line-height: 100%;"
              class="btn-custom btn-custom-close3"
              type="primary"
              @click="reInstallAction()"
              v-text="resources['close']"
          >
          </a>
          <a style="font-size: 15px;line-height: 100%;" @click="finishAction" id="goToNew2" href="javascript:void(0)" class="btn-custom btn-custom-primary">
            <span v-text="resources['confirm']"></span>
          </a>
<!--          <div-->
<!--            style="min-width: 80px;max-width: 80px;font-size: 14px;"-->
<!--            v-on:click="finishAction"-->
<!--            class="confirm-button"-->
<!--          v-text="resources['confirm']"></div>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>

module.exports = {
    props: {
        resources: Object,
    },
  data() {
    return {
      selected:false,
      startDate: moment(),
      startHour: moment("00:00", "HH:mm"),
      endDate: moment(),
      endHour: moment("00:00", "HH:mm"),
      reason: [],
      originalReasons: [],
      addedReasons: [],
      isFocusReasonInput: false,
      reasonText: "",
      maintenance: null,
      data: {
        param: {
          message: "",
          maintenanceStartTime: 0,
          maintenanceEndTime: 0,
          id: null
        }
      },
      files: [],

      maxHour: 23,
      maxMinute: 59,
      hourFormat: 'HH',
      minuteFormat: 'mm',
      dateFormat: 'YYYY-MM-DD',
      focusReasonIndex: null,
      checkList: [],
      checkedList: [],
      oldCheckedList: [],
      checkedItem: '',
      visible: false,
      displayCaret: false,
      caretTimeout: null,
      animationTimeout: null,
      isHover: false
    };
  },
  methods: {
    onMouseleave: function() {
      this.checkedList = this.checkedItem
    },
    showAnimation (){
      const el = document.getElementById('part-filter-maintenance')
      if (!this.visible) {
        setTimeout(() => {
          el.classList.add("dropdown_button-animation");
          this.animationTimeout = setTimeout(() => {
            el.classList.remove("dropdown_button-animation");
            el.classList.add("selected-dropdown-text");
            el.classList.add("selected-dropdown-button");
          }, 1600);
        }, 1);

      } else {
        this.closeAnimation()
      }
    },
    closeAnimation(){
      const el = document.getElementById('part-filter-maintenance')
      if (this.animationTimeout != null) {
        console.log("Here");
        console.log(this.animationTimeout);
        clearTimeout(this.animationTimeout);
        this.animationTimeout = null;
      }
      if (this.caretTimeout != null) {
        clearTimeout(this.caretTimeout);
        this.caretTimeout = null;
      }
      el.classList.remove("dropdown_button-animation");
      el.classList.remove("selected-dropdown-text");
      el.classList.remove("selected-dropdown-button");
      this.displayCaret = false
    },
    getChecklist (id) {
      axios.get(`/api/checklist/checklist-mold-new?moldId=${id}&checklistType=MAINTENANCE`).then((res) => {
        this.checkList = res.data.data
        this.checkedItem = this.checkList[0]
      })
    },
    isFocusClass(index) {
      return this.isFocusReasonInput && index === this.addedReasons.length - 1;
    },
    addReason: function (id) {
      const el = document.getElementById(id)
      el.classList.add('primary-animation-invite')
      const self = this
      setTimeout(() => {
        self.addedReasons.push({
          content: '',
          tmpContent: '',
          isConfirmed: false
        });
        self.isFocusReasonInput = true;
        self.focusReasonIndex = self.addedReasons.length - 1;
        self.$nextTick(() => {
          self.$refs['added-reason-' + (self.addedReasons.length - 1)][0].focus();
        });
        el.classList.remove('primary-animation-invite')
      }, 700)
    },
    isNewReason(addedReason) {
      return !addedReason.isConfirmed;
    },
    focusOutReason: function (addedReason) {
      if (this.isNewReason(addedReason)) {
        return;
      }
      if (this.isValidAllAddedReason()) {
        this.saveReason();
      }
    },
    inputReason: function (e, addedReason) {
      if (this.isNewReason(addedReason)) {
        return;
      }
      if (e.code === 'Enter') {
        if (this.isValidAllAddedReason()) {
          this.saveReason();
          $('.reason-edit-input').blur();
        }
      }
    },
    isValidAllAddedReason() {
      for (let i = 0; i < this.addedReasons.length; i++) {
        if (!this.addedReasons[i].content) {
          return false;
        }
      }
      return true;
    },
    deleteReason(index) {
      this.addedReasons.splice(index, 1);
      this.isFocusReasonInput = false;
    },
    focusReason(index) {
      if (this.focusReasonIndex !== null) {
        let tmpContent = this.addedReasons[this.focusReasonIndex].tmpContent;
        if (tmpContent && tmpContent.trim()) {
          this.addedReasons[this.focusReasonIndex].content = tmpContent.trim();
        } else {
          this.addedReasons[this.focusReasonIndex].tmpContent = this.addedReasons[this.focusReasonIndex].content;
        }
      }
      this.focusReasonIndex = index;
      this.$nextTick(() => {
        this.$refs['added-reason-' + index][0].focus();
      });
    },
    confirmEditReason(addedReason, index) {
      if (!addedReason.tmpContent || !addedReason.tmpContent.trim()) {
        if (!addedReason.isConfirmed) {
          this.showValidChecklist(index);
          // this.cancelEditReason(addedReason, index);
          return;
          addedReason.content = 'Checklist ' + (index + 1);
          addedReason.isConfirmed = true;
          this.isFocusReasonInput = false;
          this.focusReasonIndex = null;
          this.$nextTick(() => {
            this.$refs['checkbox-' + index][0].checked = true;
          });
        }
        addedReason.tmpContent = addedReason.content;
        return;
      }
      addedReason.content = addedReason.tmpContent.trim();
      this.focusReasonIndex = null;
      addedReason.isConfirmed = true;
      this.isFocusReasonInput = false;
      this.focusReasonIndex = null;
    },
    showValidChecklist(index){
      this.$refs['added-reason-' + index][0].required=true;
      this.$refs['added-reason-' + index][0].setCustomValidity("Please input the checklist name.");
      this.$refs['btn-checklist-' + index][0].click();
    },
    cancelEditReason(addedReason, index) {
      if (!addedReason.isConfirmed) {
        this.deleteReason(index);
        return;
      }
      this.focusReasonIndex = null;
      if ((!addedReason.tmpContent || !addedReason.tmpContent.trim())) {
        addedReason.tmpContent = addedReason.content;
        return;
      }
      addedReason.content = addedReason.tmpContent;
    },
    saveReason: function () {
      this.isFocusReasonInput = false;
      this.addedReasons.forEach(reason => {
        if (!reason.isConfirmed) {
          reason.isConfirmed = true;
        }
      });
    },
    isToday(momentDate) {
      return momentDate.format(this.dateFormat) === moment().format(this.dateFormat);
    },
    isSameDate(momentDateFirst, momentDateSecond) {
      if (!momentDateFirst || !momentDateSecond) {
        return false;
      }
      return momentDateFirst.format(this.dateFormat) === momentDateSecond.format(this.dateFormat);
    },
    range(start, end) {
      let res = [];
      for (let i = start; i <= end; i++) {
        res.push(i);
      }
      return res;
    },
    currentHour() {
      return Number(moment().format(this.hourFormat));
    },
    currentMinute() {
      return Number(moment().format(this.minuteFormat));
    },
    getHour(momentTime) {
      return Number(momentTime.format(this.hourFormat));
    },
    getMinute(momentTime) {
      return Number(momentTime.format(this.minuteFormat));
    },
    disabledDateStartTime(current) {
      return current && current > moment();
    },
    disabledHourStartTime() {
      if (!this.isToday(this.startDate)) {
        return [];
      }
      // return hour < current hour
      let currentHour = this.currentHour();
      return this.range(currentHour, this.maxHour);
    },
    disabledMinuteStartTime(selectedHour) {
      if (!this.isToday(this.startDate) || selectedHour < this.currentHour())
        return [];
      let currentMinute = this.currentMinute();
      return this.range(currentMinute + 1, this.maxMinute);
    },
    disabledDateEndTime(current) {
      return current && current < this.startDate;
    },
    disabledHourEndTime() {
      if (!this.isSameDate(this.startDate, this.endDate))
        return [];
      let startHour = this.getHour(this.startHour);
      return this.range(0, startHour - 1);
    },
    disabledMinuteEndTime(selectedHour) {
      if (!this.isSameDate(this.startDate, this.endDate) || this.getHour(this.startHour) !== selectedHour)
        return [];
      let startMinute = this.getMinute(this.startHour);
      return this.range(0, startMinute);
    },
    readonly: function() {
      return this.reason.findIndex(value => value == "Other") === -1;
    },

    show: async function(maintenance) {
      this.maintenance = maintenance;
      this.data.param.id = maintenance.id;
      this.addedReasons = [];
      //clear form
      console.log('this.reason',this.reason);
      this.reason=[];
      this.startDate = moment();
      this.startHour = moment("00:00", "HH:mm");
      this.endDate = moment();
      this.endHour = moment("00:00", "HH:mm");
      // this.files=[];

      this.isFocusReasonInput = false;
      this.focusReasonIndex = null;
      this.getOriginalChecklist(maintenance.moldId);
      
      axios.get(`/api/checklist/checklist-mold-new?moldId=${maintenance.moldId}&checklistType=MAINTENANCE`).then((res) => {
        this.checkList = res.data.data
        this.checkedItem = this.checkList[0]
        $("#op-maintenance-alert-modal").modal("show");
      })
    },

    selectedFile: function(e) {
      var files = e.target.files;

      if (files) {
        var selectedFiles = Array.from(files);

        for (var i = 0; i < selectedFiles.length; i++) {
          this.files.push(selectedFiles[i]);
        }
      }
    },

    deleteFile: function(f, index) {
      var newFiles = [];
      for (var i = 0; i < this.files.length; i++) {
        var file = this.files[i];
        if (i != index) {
          newFiles.push(file);
        }
      }

      this.files = newFiles;
    },

    formData: function() {
      var formData = new FormData();
      for (var i = 0; i < this.files.length; i++) {
        var file = this.files[i];
        formData.append("files", file);
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      formData.append("payload", JSON.stringify(this.data.param));
      return formData;
    },

    handleDateToTimestamp: function(date, time) {
      const dateString = date.format("YYYY-MM-DD");
      console.log("dateString: ", dateString);
      const timeString = time.format("HH:ss");
      const mergeData = dateString + " " + timeString;
      return moment(mergeData).unix();
    },

    handleReason: function() {
      let newReason = this.reason;
      let result = "";
      console.log("this.reason: ", this.reason);
      const findOther = this.reason.findIndex(value => value === "Other");
      if (findOther !== -1) {
        newReason = this.reason.concat(this.reasonText);
      }
/*
      this.addedReasons.forEach(item => {
        if (item.content) {
          newReason.push(item.content);
        }
      });
*/
      newReason
        .filter(value => value !== "Other")
        .forEach((element, index) => {
          result = result + `${index + 1}. ${element}` + "\n";
        });

      return result;
    },

    finishAction: function() {
      const el = document.getElementById('goToNew2')
      el.classList.add('primary-animation')
      const self = this
      setTimeout(() => {
        el.classList.remove('primary-animation')
        self.data.param.message = self.handleReason();
        if (self.data.param.message == "") {
          Common.alert("Please select at least one option in the maintenance checklist");
          return false;
        }

        if(self.reasonText.length == 0 && self.selected){
          Common.alert("Please specify other maintenance activities");
          return false;
        }

        const { startDate, startHour, endDate, endHour } = self;
        console.log("startDate: ", startDate);
        self.data.param.maintenanceStartTime = self.handleDateToTimestamp(
            startDate,
            startHour
        );
        self.data.param.maintenanceEndTime = self.handleDateToTimestamp(
            endDate,
            endHour
        );
        if (self.data.param.maintenanceStartTime > self.data.param.maintenanceEndTime ||
            self.data.param.maintenanceStartTime == self.data.param.maintenanceEndTime) {
          return Common.alert("Maintenance start time must be earlier than maintenance end time");
        }
        axios.put(
            `/api/molds/maintenance/${self.maintenance.id}/done`,
            self.formData(),
            vm.multipartHeader()
        )
            .then(function(response) {
              self.reason = "Dry the mold";
              $("#op-maintenance-alert-modal").modal("hide");
              // vm.callbackMessageForm();
              self.$emit('callback-message-form')
            })
            .catch(function(error) {
              console.log(error.response);
            });
      }, 700)
    },
    getOriginalChecklist(moldId) {
      Common.getOriginalChecklist(moldId).then(data => {
        this.originalReasons = data;
      });
    },
    reInstallAction: function() {
      const el = document.getElementById('close-request3')
      el.classList.add('close-animation3')
      el.classList.add('borderWhite')
      setTimeout(() => {
        el.classList.remove('close-animation3')
        $("#op-maintenance-alert-modal").modal("hide");
      }, 700)
      setTimeout(() => {
        el.classList.remove('borderWhite')
      }, 600)
    },
    closeAlert () {
      $("#op-maintenance-alert-modal").modal("hide");
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseleave();
      }
    }
  }
};
</script>
<style>
 .custom-truncate-zone label {
   max-width: 150px;
   text-overflow: ellipsis;
   overflow: hidden;
   margin-bottom: -2px!important;
 }
 .custom-truncate-zone2 label {
   max-width: 450px;
   text-overflow: ellipsis;
   overflow: hidden;
   margin-bottom: -2px!important;
 }
  #op-maintenance-alert-modal .pretty .state label:before, #op-maintenance-alert-modal .pretty .state label:after {
    top: 2px;
  }
  #op-maintenance-alert-modal .dropdown_button {
    display: flex;
    justify-content: space-between;
    padding: 2px 10px;
    align-items: center;
    width: 180px;
    background: #ffffff;
    border: 1px solid #3491ff;
    color: #3491ff;
    border-radius: 2px;
    text-decoration: none !important;
  }
  #op-maintenance-alert-modal .dropdown_button:hover {
    border: 1px solid transparent!important;
    color: #3491ff;
    background-color: #deedff;
    border: 1px solid #3491ff;
    color: #3491ff;
  }
  #op-maintenance-alert-modal .clicked-button {
    color: #3491ff !important;
    background-color: #ffffff;
    outline: 2px solid #98d1fd!important;
    border: 1px solid transparent;
  }
  #op-maintenance-alert-modal .selected {
    border: 1px solid transparent;
    color: #707070 !important;
    background-color: #ffffff;
  }
  .ant-btn.active,
  .ant-btn:active,
  .ant-btn:focus {
    color: black !important;
    fill: black !important;
    /* background-image: linear-gradient(to right, #414ff7, #6a4efb) !important; */
  }
  .ant-btn.active,
  .ant-btn:active,
  .ant-btn:focus > svg {
    fill: #fff !important;
  }
  .down-icon {
    fill: #564efb;
  }
  .ant-menu-submenu-title:hover {
    /* background-image: linear-gradient(#414ff7, #6a4efb) !important; */
    background-color: red;
  }
  .first-layer-wrapper {
    position: relative;
  }
  .first-layer {
    position: absolute;
    top: 0;
  }
  .ant-input-affix-wrapper .ant-input:not(:first-child) {
    border-radius: 0px;
    border: unset;
  }
  .ant-input {
    font-size: 14px !important;
  }
  ::-webkit-scrollbar {
    width: 3px !important;
  }
  .dropdown-scroll-maintenance .pretty.p-default input:checked ~ .state label:after {
    background-color: #3491FF !important;
    background: #3491FF !important;
  }
  .dropdown-scroll-maintenance .pretty.p-default input:checked ~ .state label:before {
    border-color: #3491FF;
  }
  .ant-popover-inner-content {
    padding: 2px!important;
  }
  #op-maintenance-alert-modal a:focus {
    text-decoration: unset;
  }
  #op-maintenance-alert-modal .primary-animation-invite {
    /*animation: primary-animation-invite 0.7s;*/
    /*animation-iteration-count: 1;*/
    /*animation-direction: alternate;*/
    color: #006cff !important;
    text-decoration: none !important;
  }

  @keyframes primary-animation-invite {
    0%   {
      color: #006cff !important;
      text-decoration: none !important;
    }
    33%   {
      color: #006cff !important;
      text-decoration: none !important;
    }
    66%   {
      color: #006cff !important;
      text-decoration: none !important;
    }
    100%   {

    }
  }
  #op-maintenance-alert-modal .invite-link {
    /*color: #3491ff;*/
    color: #298BFF;
    cursor: pointer;
    text-decoration: none;
  }

  #op-maintenance-alert-modal .invite-link:hover {
    color: #298BFF;
    text-decoration: underline;
  }
  #op-maintenance-alert-modal .custom-modal-content .modal-body {
    padding: 0
  }
  #op-maintenance-alert-modal .custom-modal-content .modal-title {
    background: #F5F8FF;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    /*margin-bottom: 1px;*/
    padding: 19.5px 25.5px 11.5px 31px;
    border-radius: 6px 6px 0 0;
  }
  #op-maintenance-alert-modal .custom-modal-content .t-icon-close {
    width: 12px;
    height: 12px;
    /*line-height: 12px;*/
    background-image: url("/images/icon/black-close-12.svg");
    background-repeat: no-repeat;
    background-size: 100%;
  }
  #op-maintenance-alert-modal .custom-modal-content .head-line {
    position: absolute;
    background: #52a1ff;
    height: 8px;
    border-radius: 6px 6px 0 0;
    top: 0;
    width: 100%;
  }
  #op-maintenance-alert-modal  .custom-modal-content .modal-title .span-title {
    color: #4B4B4B;
    font-weight: bold;
    font-size: 16px;
    line-height: 100%;
  }
  .reason-edit-input {
    width: 360px;
    /*border: 1px solid #FFF;*/
    padding-left: 5px;
    margin-right: 5px;
    /*margin-left: 5px;*/
    margin-left: -5px;
  }
  .reason-edit-input.focus {
    border: 1px solid #EFEFEF;
  }
  .reason-edit-input:focus,
  .reason-edit-input:hover
  {
    border: 1px solid #EFEFEF !important;
    outline: none;
  }

  .reason-edit-label {
    width: 360px;
    /*margin-left: 5px;*/
    margin-left: -5px;
    padding: 2px 5px;
    text-align: left;
    border: 1px solid #FFF;
    margin-right: 5px;
    white-space: normal;
    overflow-wrap: break-word;
  }

  .checklist-action {
    margin-left: 10px;
  }
  .checklist-action .checklist-edit,
  .checklist-action .checklist-confirm-edit
  {
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

  label span {
    text-transform: capitalize;
    font-weight: 400;
    font-size: 14px;
    line-height: 1.5;
  }

  .reason-edit-input {
    border-radius: 5px;
    color: rgba(0,0,0,.65);
    padding: 2px 5px;

  }

  .ant-calendar-picker-container{
    z-index: 5900 !important;
  }
  .ant-time-picker-panel{
    z-index: 5900 !important;
  }
  .ant-time-picker-panel-narrow{
    z-index: 5900 !important;
  }
  .ant-time-picker-panel-column-2{
    z-index: 5900 !important;
  }
  .ant-time-picker-panel-placement-bottomLeft{
    z-index: 5900 !important;
  }
  .ant-calendar-picker-container-placement-bottomLeft{
    z-index: 5900 !important;
  }

 form.btn.radio-confirm-modal {
   padding-left: 0px;
 }
</style>
