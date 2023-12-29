<template>
  <div
    id="op-request-maintenance-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg-m" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['request_maintenance']"></h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="row">
            <div class="col-md-12">
              <div class="card">
                <div class="card-header">
                  <strong v-text="resources['basic_info']"></strong>
                </div>
                <div class="card-body">

                  <div class="form-group row">
                    <label class="col-md-3 col-form-label" for="mold-id" v-text="resources['tooling_id']"></label>
                    <div class="col-md-9">
                      <input type="text" id="mold-id" class="form-control" :value="maintenance.mold.equipmentCode"
                             placeholder="Tooling ID" readonly>
                    </div>
                  </div>

                  <div class="form-group row">
                    <label class="col-md-3 col-form-label" for="mold-id"  v-text="resources['failure_time']" ></label>
                    <div class="col-md-9">
                      <input type="text" id="failure-time" class="form-control" v-model="maintenance.failureDateTime"
                             placeholder="Failure Time" readonly>
                    </div>
                  </div>
                  <div class="form-group row">
                    <label class="col-md-3 col-form-label align-items-center" for="reason"  v-text="resources['reason']" ></label>
                    <div class="col-md-9">
                      <textarea id="reason" class="form-control" v-model="mainFormData.failureReason">{{maintenance.failureReason}}</textarea>
                    </div>
                  </div>
                  <div class="form-group row">
                    <label class="col-md-3 col-form-label" for="backup-count"><span  v-text="resources['number_of_backup']" ></span> <span v-if="isNumberOfBackupsRequired" class="badge-require"></span></label>
                    <div class="col-md-9">
                      <input type="number" id="backup-count" class="form-control" v-model="mainFormData.numberOfBackups" />
                    </div>
                  </div>
                  <div class="form-group row" v-if="mainFormData.correctiveStatus === 'REQUESTED'">
                    <label class="col-md-3 col-form-label" ><span  v-text="resources['expected_maintenance_end_time']"></span>  <span class="badge-require"></span></label>
                    <div class="col-md-9">
                      <a-date-picker format="MMM Do, YYYY" v-model="mainFormData.expectedEndDate"></a-date-picker>
                      <a-time-picker
                              style="margin-left: 10px;"
                              v-model="mainFormData.expectedEndTime"
                              placeholder="HH:mm"
                              format="HH:mm"
                      ></a-time-picker>
                    </div>
                  </div>
                  <div class="form-group row" v-if="mainFormData.correctiveStatus === 'APPROVED'">
                    <label class="col-md-3 col-form-label"  v-text="resources['expected_maintenance_end_time']"></label>
                    <div class="col-md-9">
                      <input type="text" class="form-control" v-model="mainFormData.expectedEndTimeShow" readonly />
                    </div>
                  </div>

                  <div class="form-group row actual-time-field" v-if="mainFormData.correctiveStatus === 'APPROVED'" style="align-content: center">
                    <label class="col-md-3 col-form-label" ><span v-text="resources['actual_maintenance_time']"></span>  <span class="badge-require"></span></label>
                    <div class="col-md-9">
                      <div style="margin-left: 0.9em; margin-top: 20px;" class="row">
                        <div class="maintence-time-container">
                          <label style="font-size: 16px;">
                            <label style="font-size: 16px;" v-text="resources['start_time']"></label>
                          </label>
                        </div>
                        <a-date-picker format="MMM Do, YYYY" v-model="mainFormData.actualStartDate"></a-date-picker>
                        <a-time-picker
                                style="margin-left: 10px;"
                                v-model="mainFormData.actualStartTime"
                                placeholder="HH:mm"
                                format="HH:mm"
                        ></a-time-picker>
                      </div>
                      <div style="margin-left: 0.9em; margin-top: 20px;" class="row">
                        <div class="maintence-time-container">
                          <label style="font-size: 16px;">
                           <label style="font-size: 16px;" v-text="resources['end_time']"> </label>
                          </label>
                        </div>
                        <a-date-picker format="MMM Do, YYYY" v-model="mainFormData.actualEndDate"></a-date-picker>
                        <a-time-picker
                                style="margin-left: 10px;"
                                v-model="mainFormData.actualEndTime"
                                placeholder="HH:mm"
                                format="HH:mm"
                        ></a-time-picker>
                      </div>
                    </div>
                  </div>

                  <div class="form-group row" v-if="mainFormData.correctiveStatus === 'APPROVED'">
                    <label class="col-md-3 col-form-label" for="backup-count">
                    <span v-text="resources['cost']"></span>
                    <span class="badge-require"></span></label>
                    <div class="col-md-9">
                      <div class="cost-group">
                        <input type="number" id="cost" class="form-control cost-value" v-model="mainFormData.cost" />
                        <label class="currency-unit-label" v-text="resources['unit']"></label>
                        <select class="form-control cost-unit" v-model="mainFormData.currencyType">
                          <option v-for="(currencyType, index) in currencyTypes" :key="index" :value="currencyType.code">{{ currencyType.code }}</option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <div class="form-group row">
                    <label class="col-md-3 col-form-label"><span v-text="resources['document']"></span> <span v-if="isMaintenanceDocRequired" class="badge-require"></span></label>
                    <div class="col-md-9 op-upload-button-wrap">
                      <button type="button" class="btn btn-outline-success" v-text="resources['upload_files']"></button>
                      <input type="file" id="file" @change="selectedFile" multiple />
                      <div>
                        <button
                                style="margin-top: 2px;text-overflow: ellipsis; max-width: 200px; overflow: hidden;"
                                v-for="(file, index) in files"
                                class="btn btn-outline-dark btn-sm mr-1"
                                @click.prevent="deleteFile(file, index)"
                        >{{file.name}}</button>
                      </div>
                      <div>
                        <button
                                style="margin-top: 2px;text-overflow: ellipsis; max-width: 200px; overflow: hidden;"
                                v-for="(maintenanceFile, index) in maintenanceFiles"
                                :key="index"
                                class="btn btn-outline-dark btn-sm mr-1"
                        >{{maintenanceFile.fileName}}</button>
                      </div>
                    </div>

                  </div>

                  <div class="form-group row">
                    <label class="col-md-3 col-form-label" for="backup-count"  v-text="resources['memo']"></label>
                    <div class="col-md-9">
                      <textarea id="memo" class="form-control" v-model="mainFormData.memo"></textarea>
                      <div class="old-memo" v-if="maintenance.memo && maintenance.memo.split('\n').length > 0">
                          <div class="memo-item" v-for="(memo, index) in maintenance.memo.split('\n')" :key="index">
                              {{ memo }}
                          </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="display: flex; justify-content: center;">
          <div v-if="mainFormData.correctiveStatus === 'APPROVED'"
                  style="min-width: 100px;max-width: 400px;font-size: 14px;padding: 0px 10px;font-weight: 300;"
                  v-on:click="finishAction"
                  class="confirm-button"
          v-text="resources['complete']"></div>
          <div v-else
               style="min-width: 100px;max-width: 400px;font-size: 14px;padding: 0px 10px;font-weight: 300;"
               v-on:click="finishAction"
               class="confirm-button"
               v-text="resources['maintenace_approve']"
          ></div>
          <div
            v-on:click="closeModal"
            style="color: rgba(0, 0, 0, 0.65); font-weight: 400; border: 1px solid #777;min-width: 80px;max-width: 80px;font-size: 14px;background: #FFF;"
            class="confirm-button"
         v-text="resources['cancel']"></div>

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
      maintenance: {
        mold: {}
      },
      mainFormData: {
        id: null,
        failureReason: null,
        numberOfBackups: null,
        memo: null
      },
      data: {
        param: {
          id: null
        }
      },
      maintenanceFiles: [],
      files: [],
      // serverName: '',
      currencyTypes: [],
      options: {}
    };
  },
  mounted(){
    this.findCurrencyTypeOption();
  },
  computed: {
    isNumberOfBackupsRequired() {
      return Boolean(this.options?.CLIENT?.requestMaintenance?.numberOfBackupsRequired )
    },
    isMaintenanceDocRequired() {
      return Boolean(this.options?.CLIENT?.requestMaintenance?.maintenanceDocRequired)
    }
  },
  methods: {
    show: async function(maintenance) {
      this.maintenance = maintenance;
      this.data.param.id = maintenance.id;
      this.mainFormData = Object.assign({}, maintenance);
      this.maintenanceFiles = [];
      this.mainFormData.expectedEndTimeShow = this.maintenance.expectedEndTime
              ? moment.unix(this.maintenance.expectedEndTime).format("YYYY-MM-DD, HH:mm:ss")
              : "-";
      this.mainFormData.memo = null;
      this.files = [];
      // this.serverName = await Common.getSystem('server')
      const options = await Common.getSystem('options')
      this.options = JSON.parse(options)
      this.findDocumentFile(maintenance);
      $("#op-request-maintenance-modal").modal("show");
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
        if (i !== index) {
          newFiles.push(file);
        }
      }

      this.files = newFiles;
    },

    requestFormData: function() {
      var requestFormData = new FormData();
      for (var i = 0; i < this.files.length; i++) {
        var file = this.files[i];
        requestFormData.append("files", file);
      }
      let formData = {
        id: this.mainFormData.id,
        failureReason: this.mainFormData.failureReason,
        numberOfBackups: this.mainFormData.numberOfBackups,
        memo: this.mainFormData.memo
      };
      if (this.mainFormData.correctiveStatus === 'REQUESTED') {
        formData.expectedEndTime = this.handleDateToTimestamp(this.mainFormData.expectedEndDate, this.mainFormData.expectedEndTime);
        requestFormData.append("correctiveAction", 'APPROVE');
      }else if(this.mainFormData.correctiveStatus === 'APPROVED') {
        formData.cost = this.mainFormData.cost;
        formData.currencyType = this.mainFormData.currencyType;
        formData.startTime = this.handleDateToTimestamp(this.mainFormData.actualStartDate, this.mainFormData.actualStartTime);
        formData.endTime = this.handleDateToTimestamp(this.mainFormData.actualEndDate, this.mainFormData.actualEndTime);

        requestFormData.append("correctiveAction", 'COMPLETE');
      }else {
        requestFormData.append("correctiveAction", 'REQUEST');
      }
      requestFormData.append("payload", JSON.stringify(formData));
      return requestFormData;
    },

    handleDateToTimestamp: function(date, time) {
      const dateString = date.format("YYYY-MM-DD");
      const timeString = time.format("HH:ss");
      const mergeData = dateString + " " + timeString;
      return moment(mergeData).unix();
    },

    finishAction: function() {
      // validate data
      if (this.isNumberOfBackupsRequired && !this.mainFormData.numberOfBackups) {
        Common.alert("Please enter the number of backups.");
        return;
      }

      if (this.mainFormData.correctiveStatus === 'FAILURE') {
        if (this.isMaintenanceDocRequired && (!this.files || this.files.length === 0)) {
          Common.alert("Please attach maintenance document.");
          return;
        }
      }else if (this.mainFormData.correctiveStatus === 'REQUESTED') {
        if (!this.mainFormData.expectedEndDate) {
          Common.alert("Please enter the expected maintenance end date.");
          return;
        }

        if (!this.mainFormData.expectedEndTime) {
          Common.alert("Please enter the expected maintenance end time.");
          return;
        }
      }else if (this.mainFormData.correctiveStatus === 'APPROVED') {

        if (!this.mainFormData.actualStartDate) {
          Common.alert("Please choosing the actual maintenance start date.");
          return;
        }

        if (!this.mainFormData.actualStartTime) {
          Common.alert("Please choosing the actual maintenance start time.");
          return;
        }

        if (!this.mainFormData.actualEndDate) {
          Common.alert("Please choosing the actual maintenance end date.");
          return;
        }

        if (!this.mainFormData.actualEndTime) {
          Common.alert("Please choosing the actual maintenance end time.");
          return;
        }

        if (!this.mainFormData.cost) {
          Common.alert("Please enter the cost");
          return;
        }

        if (!this.mainFormData.currencyType) {
          Common.alert("Please enter the currency type.");
          return;
        }
      }
      let self=this;
      axios
        .post(
          `/api/molds/corrective/${this.maintenance.id}`,
          this.requestFormData(),
          vm.multipartHeader()
        )
        .then(function(response) {
          this.reason = "Dry the mold";
          $("#op-request-maintenance-modal").modal("hide");
          Common.alert("success");
          // vm.callbackMessageForm();
          self.$emit('callback-message-form')

        })
        .catch(function(error) {
          console.log(error.response);
        });
    },
    findDocumentFile(maintenance) {
      let url = `/api/file-storage/mold?storageTypes=MOLD_CORRECTIVE&refId=` + maintenance.id;
      axios.get(url).then((response) => {
        if (response.data && response.data.MOLD_CORRECTIVE && response.data.MOLD_CORRECTIVE.length > 0) {
          this.maintenanceFiles = response.data.MOLD_CORRECTIVE;
        }
      });
    },
    findCurrencyTypeOption(){
      axios.get('/api/codes').then((response) => {
        if (response.data && response.data.CurrencyType) {
          this.currencyTypes = response.data.CurrencyType;
        }
        console.log('checking currency type response', response);
      });
    },

    closeModal: function() {
      $("#op-request-maintenance-modal").modal("hide");
    }
  }
};
</script>
<style>
  #op-request-maintenance-modal .modal-dialog {
    max-width: 1080px;
  }
  #op-request-maintenance-modal .form-group label {
    margin-right: 0;
  }
  .cost-group {
    display: flex;
  }

  .cost-group .cost-value {
    width: 70%;
  }

  .cost-group .cost-unit {
    width: calc(100% - 15px);
    margin-left: 15px;
    max-width: 250px;
  }
  .currency-unit-label {
    margin-left: 25px;
    padding-top: 5px;
  }

  .actual-time-field {
    align-items: center;
  }
</style>
