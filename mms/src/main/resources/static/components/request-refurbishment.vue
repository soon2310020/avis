<template>
  <div
    id="op-request-refurbishment-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg-m" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5
            class="modal-title"
            v-text="resources['request_refurbishment']"
          ></h5>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
          >
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
                        :value="refurbishment.mold.equipmentCode"
                        placeholder="Tooling ID"
                        readonly
                      />
                    </div>
                  </div>
                  <div class="form-group row">
                    <label class="col-md-3 col-form-label">
                      <span
                        v-text="
                          resources['estimated_extended_life'] +
                          ' (' +
                          resources['shot'] +
                          ')'
                        "
                      ></span>

                      <span class="badge-require"></span>
                    </label>
                    <div class="col-md-9">
                      <input
                        type="number"
                        id="estimated-extended-life"
                        class="form-control"
                        v-model="mainFormData.estimateExtendedLife"
                        min="0"
                        max="999999999"
                      />
                    </div>
                  </div>
                  <div
                    class="form-group row"
                    v-if="mainFormData.refurbishmentStatus === 'APPROVED'"
                  >
                    <label class="col-md-3 col-form-label"
                      >Number of Working Cavity
                      <span
                        v-if="isWorkingCavityRequired"
                        class="badge-require"
                      ></span>
                    </label>
                    <div class="col-md-9">
                      <input
                        type="number"
                        class="form-control"
                        v-model="mainFormData.numberOfWorkingCavity"
                      />
                    </div>
                  </div>
                  <div class="form-group row" v-if="false">
                    <label
                      class="col-md-3 col-form-label"
                      for="mold-id"
                      v-text="resources['refurbishment_time']"
                    ></label>
                    <div class="col-md-9">
                      <input
                        type="text"
                        id="failure-time"
                        class="form-control"
                        v-model="refurbishment.failureDate"
                        placeholder="Refurbishment Time"
                        readonly
                      />
                    </div>
                  </div>
                  <div class="form-group row">
                    <label
                      class="col-md-3 col-form-label align-items-center"
                      for="reason"
                    >
                      <span v-text="resources['reason']"></span>
                      <span
                        v-if="
                          mainFormData.refurbishmentStatus ===
                            'END_OF_LIFECYCLE' ||
                          mainFormData.refurbishmentStatus === 'MEDIUM_HIGH' ||
                          mainFormData.refurbishmentStatus === 'REQUESTED'
                        "
                        class="badge-require"
                      ></span>
                    </label>
                    <div class="col-md-9">
                      <textarea
                        id="reason"
                        class="form-control"
                        v-model="mainFormData.failureReason"
                        >{{ refurbishment.failureReason }}</textarea
                      >
                    </div>
                  </div>

                  <div
                    class="form-group row"
                    v-if="mainFormData.refurbishmentStatus === 'REQUESTED'"
                  >
                    <label class="col-md-3 col-form-label">
                      <span
                        v-text="resources['expected_refurbishment_end_time']"
                      ></span>
                      <span class="badge-require"></span>
                    </label>
                    <div class="col-md-9">
                      <a-date-picker
                        format="MMM Do, YYYY"
                        v-model="mainFormData.expectedEndDate"
                      ></a-date-picker>
                      <a-time-picker
                        style="margin-left: 10px"
                        v-model="mainFormData.expectedEndTime"
                        placeholder="HH:mm"
                        format="HH:mm"
                      ></a-time-picker>
                    </div>
                  </div>
                  <div
                    class="form-group row"
                    v-if="mainFormData.refurbishmentStatus === 'APPROVED'"
                  >
                    <label
                      class="col-md-3 col-form-label"
                      v-text="resources['expected_refurbishment_end_time']"
                    ></label>
                    <div class="col-md-9">
                      <input
                        type="text"
                        class="form-control"
                        v-model="mainFormData.expectedEndTimeShow"
                        readonly
                      />
                    </div>
                  </div>

                  <div
                    class="form-group row actual-time-field"
                    v-if="mainFormData.refurbishmentStatus === 'APPROVED'"
                    style="align-content: center"
                  >
                    <label class="col-md-3 col-form-label">
                      <span
                        v-text="resources['actual_refurbishment_time']"
                      ></span>
                      <span class="badge-require"></span>
                    </label>
                    <div class="col-md-9">
                      <div
                        style="margin-left: 0.9em; margin-top: 20px"
                        class="row"
                      >
                        <div class="maintence-time-container">
                          <label
                            style="font-size: 16px"
                            v-text="resources['start_time']"
                          >
                          </label>
                        </div>
                        <a-date-picker
                          format="MMM Do, YYYY"
                          v-model="mainFormData.actualStartDate"
                        ></a-date-picker>
                        <a-time-picker
                          style="margin-left: 10px"
                          v-model="mainFormData.actualStartTime"
                          placeholder="HH:mm"
                          format="HH:mm"
                        ></a-time-picker>
                      </div>
                      <div
                        style="margin-left: 0.9em; margin-top: 20px"
                        class="row"
                      >
                        <div class="maintence-time-container">
                          <label
                            style="font-size: 16px"
                            v-text="resources['end_time']"
                          ></label>
                        </div>
                        <a-date-picker
                          format="MMM Do, YYYY"
                          v-model="mainFormData.actualEndDate"
                        ></a-date-picker>
                        <a-time-picker
                          style="margin-left: 10px"
                          v-model="mainFormData.actualEndTime"
                          placeholder="HH:mm"
                          format="HH:mm"
                        ></a-time-picker>
                      </div>
                    </div>
                  </div>

                  <div
                    class="form-group row"
                    v-if="
                      mainFormData.refurbishmentStatus === 'END_OF_LIFECYCLE' ||
                      mainFormData.refurbishmentStatus === 'MEDIUM_HIGH' ||
                      mainFormData.refurbishmentStatus === 'REQUESTED' ||
                      mainFormData.refurbishmentStatus === 'APPROVED'
                    "
                  >
                    <label class="col-md-3 col-form-label"
                      ><span v-text="resources['cost']"></span>
                      <!--                      <span class="badge-require" v-if="mainFormData.refurbishmentStatus === 'APPROVED'"></span>-->
                      <span class="badge-require"></span>
                    </label>
                    <div class="col-md-9">
                      <div class="cost-group">
                        <input
                          type="number"
                          id="cost"
                          class="form-control cost-value"
                          v-model="mainFormData.cost"
                        />
                        <label
                          class="currency-unit-label"
                          v-text="resources['unit']"
                        ></label>
                        <select
                          class="form-control cost-unit"
                          v-model="mainFormData.currencyType"
                        >
                          <option
                            v-for="(currencyType, index) in currencyTypes"
                            :key="index"
                            :value="currencyType.code"
                          >
                            {{ currencyType.code }}
                          </option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <div class="form-group row">
                    <label class="col-md-3 col-form-label"
                      ><span v-text="resources['document']"></span>
                    </label>
                    <div class="col-md-9 op-upload-button-wrap">
                      <button
                        type="button"
                        class="btn btn-outline-success"
                        v-text="resources['upload_files']"
                      ></button>
                      <input
                        type="file"
                        id="file"
                        @change="selectedFile"
                        multiple
                      />
                      <div>
                        <button
                          style="
                            margin-top: 2px;
                            text-overflow: ellipsis;
                            max-width: 200px;
                            overflow: hidden;
                          "
                          v-for="(file, index) in files"
                          class="btn btn-outline-dark btn-sm mr-1"
                          @click.prevent="deleteFile(file, index)"
                        >
                          {{ file.name }}
                        </button>
                      </div>
                      <div>
                        <button
                          style="
                            margin-top: 2px;
                            text-overflow: ellipsis;
                            max-width: 200px;
                            overflow: hidden;
                          "
                          v-for="(maintenanceFile, index) in maintenanceFiles"
                          :key="index"
                          class="btn btn-outline-dark btn-sm mr-1"
                        >
                          {{ maintenanceFile.fileName }}
                        </button>
                      </div>
                    </div>
                  </div>

                  <div class="form-group row">
                    <label
                      class="col-md-3 col-form-label"
                      v-text="resources['memo']"
                    ></label>
                    <div class="col-md-9">
                      <textarea
                        id="memo"
                        class="form-control"
                        v-model="mainFormData.memo"
                      ></textarea>
                      <div
                        class="old-memo"
                        v-if="
                          refurbishment.memo &&
                          refurbishment.memo.split('\n').length > 0
                        "
                      >
                        <div
                          class="memo-item"
                          v-for="(memo, index) in refurbishment.memo.split(
                            '\n'
                          )"
                          :key="index"
                        >
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
        <div
          class="modal-footer"
          style="display: flex; justify-content: center"
        >
          <div
            v-if="mainFormData.refurbishmentStatus === 'APPROVED'"
            style="
              min-width: 100px;
              max-width: 400px;
              font-size: 14px;
              padding: 0px 10px;
              font-weight: 300;
            "
            v-on:click="finishAction"
            class="confirm-button"
          >
            <span v-text="resources['complete']"></span>
          </div>
          <div
            v-else
            style="
              min-width: 100px;
              max-width: 400px;
              font-size: 14px;
              padding: 0px 10px;
              font-weight: 300;
            "
            v-on:click="finishAction"
            class="confirm-button"
          >
            <span v-text="resources['request_refurbishment']"></span>
          </div>
          <div
            v-on:click="closeModal"
            style="
              color: rgba(0, 0, 0, 0.65);
              font-weight: 400;
              border: 1px solid #777;
              min-width: 80px;
              max-width: 80px;
              font-size: 14px;
              background: #fff;
            "
            class="confirm-button"
          >
            <span v-text="resources['cancel']"></span>
          </div>
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
      refurbishment: {
        mold: {},
      },
      mainFormData: {
        id: null,
        failureReason: null,
        estimateExtendedLife: null,
        numberOfWorkingCavity: null,
        memo: null,
      },
      data: {
        param: {
          id: null,
        },
      },
      maintenanceFiles: [],
      files: [],
      // serverName: '',
      currencyTypes: [],
      options: {},
    };
  },
  mounted() {
    this.findCurrencyTypeOption();
  },
  computed: {
    isWorkingCavityRequired() {
      return Boolean(
        this.options?.CLIENT?.requestRefurbishment?.workingCavityRequired
      );
    },
  },
  methods: {
    show: async function (refurbishment) {
      console.log("refurbishment", refurbishment);
      this.refurbishment = refurbishment;
      this.data.param.id = refurbishment.id;
      this.mainFormData = Object.assign({}, refurbishment);
      this.maintenanceFiles = [];
      this.mainFormData.expectedEndTimeShow = this.refurbishment.expectedEndTime
        ? moment
            .unix(this.refurbishment.expectedEndTime)
            .format("YYYY-MM-DD, HH:mm:ss")
        : "-";
      this.mainFormData.memo = null;
      this.files = [];
      // this.serverName = await Common.getSystem('server')
      const options = await Common.getSystem("options");
      this.options = JSON.parse(options);
      this.findDocumentFile(refurbishment);
      $("#op-request-refurbishment-modal").modal("show");
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
        if (i !== index) {
          newFiles.push(file);
        }
      }

      this.files = newFiles;
    },

    requestFormData: function () {
      var requestFormData = new FormData();
      for (var i = 0; i < this.files.length; i++) {
        var file = this.files[i];
        requestFormData.append("files", file);
      }
      let formData = {
        id: this.mainFormData.id,
        failureReason: this.mainFormData.failureReason,
        estimateExtendedLife: this.mainFormData.estimateExtendedLife,
        numberOfWorkingCavity: this.mainFormData.numberOfWorkingCavity,
        memo: this.mainFormData.memo,
        cost: this.mainFormData.cost,
        currencyType: this.mainFormData.currencyType,
      };
      if (this.mainFormData.refurbishmentStatus === "REQUESTED") {
        formData.expectedEndTime = this.handleDateToTimestamp(
          this.mainFormData.expectedEndDate,
          this.mainFormData.expectedEndTime
        );
        requestFormData.append("refurbishmentAction", "APPROVE");
      } else if (this.mainFormData.refurbishmentStatus === "APPROVED") {
        formData.cost = this.mainFormData.cost;
        formData.currencyType = this.mainFormData.currencyType;
        formData.startTime = this.handleDateToTimestamp(
          this.mainFormData.actualStartDate,
          this.mainFormData.actualStartTime
        );
        formData.endTime = this.handleDateToTimestamp(
          this.mainFormData.actualEndDate,
          this.mainFormData.actualEndTime
        );

        requestFormData.append("refurbishmentAction", "COMPLETE");
      } else {
        requestFormData.append("refurbishmentAction", "REQUEST");
      }
      requestFormData.append("payload", JSON.stringify(formData));
      return requestFormData;
    },

    handleDateToTimestamp: function (date, time) {
      const dateString = date.format("YYYY-MM-DD");
      const timeString = time.format("HH:ss");
      const mergeData = dateString + " " + timeString;
      return moment(mergeData).unix();
    },

    finishAction: function () {
      // validate data
      if (!this.mainFormData.estimateExtendedLife) {
        Common.alert("Please enter the Estimated Extended Life (shot).");
        return;
      }
      /*
      if(mainFormData.refurbishmentStatus === 'REQUESTED' || mainFormData.refurbishmentStatus === 'APPROVED'){
      }
*/
      if (
        this.mainFormData.refurbishmentStatus === "END_OF_LIFECYCLE" ||
        this.mainFormData.refurbishmentStatus === "MEDIUM_HIGH" ||
        this.mainFormData.refurbishmentStatus === "REQUESTED"
        // || mainFormData.refurbishmentStatus === 'APPROVED'
      ) {
        if (!this.mainFormData.failureReason) {
          Common.alert(
            "Please provide the reason for requesting refurbishment."
          );
          return;
        }
        if (!this.mainFormData.cost) {
          Common.alert("Please enter the refurbishment cost.");
          return;
        }

        if (!this.mainFormData.currencyType) {
          Common.alert("Please enter the refurbishment cost unit.");
          return;
        }
      }
      if (
        this.mainFormData.refurbishmentStatus === "END_OF_LIFECYCLE" ||
        this.mainFormData.refurbishmentStatus === "MEDIUM_HIGH"
      ) {
        /*
        if (!this.files || this.files.length === 0) {
          Common.alert("Please attach the refurbishment document.");
          return;
        }
*/
      } else if (this.mainFormData.refurbishmentStatus === "REQUESTED") {
        if (!this.mainFormData.expectedEndDate) {
          // Common.alert("Please enter the expected refurbishment end date.");
          Common.alert("Please enter the expected refurbishment end time.");
          return;
        }

        if (!this.mainFormData.expectedEndTime) {
          Common.alert("Please enter the expected refurbishment end time.");
          return;
        }
      } else if (this.mainFormData.refurbishmentStatus === "APPROVED") {
        if (
          !this.mainFormData.numberOfWorkingCavity &&
          this.isWorkingCavityRequired
        ) {
          Common.alert("Please enter the Number of Working Cavity.");
          return;
        }

        if (!this.mainFormData.actualStartDate) {
          Common.alert("Please choosing the actual refurbishment start date.");
          return;
        }

        if (!this.mainFormData.actualStartTime) {
          Common.alert("Please choosing the actual refurbishment start time.");
          return;
        }

        if (!this.mainFormData.actualEndDate) {
          Common.alert("Please choosing the actual refurbishment end date.");
          return;
        }

        if (!this.mainFormData.actualEndTime) {
          Common.alert("Please choosing the actual refurbishment end time.");
          return;
        }

        if (!this.mainFormData.cost) {
          Common.alert("Please enter the refurbishment cost.");
          return;
        }

        if (!this.mainFormData.currencyType) {
          Common.alert("Please enter the refurbishment cost unit.");
          return;
        }
      }
      const header = {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      };
      let self = this;
      axios
        .post(
          `/api/molds/refurbishment/${this.refurbishment.id}`,
          this.requestFormData(),
          header
        )
        .then(function (response) {
          this.reason = "Dry the mold";
          $("#op-request-refurbishment-modal").modal("hide");
          // vm.callbackMessageForm();
          self.$emit("callback-message-form");
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    findDocumentFile(refurbishment) {
      let url =
        `/api/file-storage/mold?storageTypes=MOLD_REFURBISHMENT&refId=` +
        refurbishment.id;
      axios.get(url).then((response) => {
        if (
          response.data &&
          response.data.MOLD_REFURBISHMENT &&
          response.data.MOLD_REFURBISHMENT.length > 0
        ) {
          this.maintenanceFiles = response.data.MOLD_REFURBISHMENT;
        }
      });
    },
    findCurrencyTypeOption() {
      axios.get("/api/codes").then((response) => {
        if (response.data && response.data.CurrencyType) {
          this.currencyTypes = response.data.CurrencyType;
        }
        console.log("checking currency type response", response);
      });
    },

    closeModal: function () {
      $("#op-request-refurbishment-modal").modal("hide");
    },
  },
};
</script>
<style>
#op-request-refurbishment-modal .modal-dialog {
  max-width: 1080px;
}
#op-request-refurbishment-modal .form-group label {
  margin-right: 0;
}
.cost-group {
  display: flex;
}
.ant-calendar-picker-container {
  z-index: 5900 !important;
}
.ant-time-picker-panel {
  z-index: 5900 !important;
}
.ant-time-picker-panel-narrow {
  z-index: 5900 !important;
}
.ant-time-picker-panel-column-2 {
  z-index: 5900 !important;
}
.ant-time-picker-panel-placement-bottomLeft {
  z-index: 5900 !important;
}
.ant-calendar-picker-container-placement-bottomLeft {
  z-index: 5900 !important;
}

.modal-dialog {
  display: flex;
  align-items: center;
  min-height: calc(95% - 0rem);
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
