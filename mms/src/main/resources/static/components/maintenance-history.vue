<template>
  <div
    id="op-maintenance-history-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg-m" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['maintenance']"></h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="row">
            <div class="col-lg-12">
              <div style="border-radius: 0px; border: none" class="card">
                <div class="card-body">
                  <div style="align-items: center;" class="row">
                    <div class="maintenance-title">
                      <span v-text="resources['tooling_id']"></span> :
                      <span style="font-weight: 400;">{{equipmentCode}}</span>
                    </div>
                    <div class="maintenance-title maintenance-depth-3">
                      <span v-text="resources['toolmaker']"></span> :
                      <span style="font-weight: 400;">{{toolMaker}}</span>
                    </div>
                  </div>
                  <table
                    style="border-top: 1px solid #c8ced3;"
                    class="table table-responsive-sm table-striped"
                  >
                    <colgroup>
                      <col />
                      <col />
                      <col />
                      <col />
                      <col />
                      <col />
                      <col />
                      <col />
                      <col style="width:60px" v-if="requestParam.status != 'done'" />
                    </colgroup>
                    <thead>
                      <tr>
                        <th class="text-left" v-text="resources['no_dot']"></th>
                        <th  v-text="resources['number_of_shots']"></th>
                        <th v-text="resources['maintenance']"></th>
                        <th v-text="resources['maintenance_end_time']"></th>
                        <th v-text="resources['maintenance_type']"></th>
                        <th v-text="resources['details']"></th>
                      </tr>
                    </thead>
                    <tbody class="op-list">
                      <tr v-for="(result, index, id) in results" :id="result.index">
                        <td>{{result.index}}</td>
                        <td>{{result.shotCount ? result.shotCount : '-'}}</td>
                        <td>
                          <img
                            v-if="result.confirmed"
                            width="20"
                            src="/images/icon/green-check.svg"
                          />
                          <img
                            v-if="!result.confirmed && !result.latest"
                            width="20"
                            src="/images/icon/red-cancel.svg"
                          />
                          {{!result.confirmed && result.latest?'-':''}}
                        </td>
                        <td>{{result.endTimeShow ? result.endTimeShow : '-'}}</td>
                        <td>{{result.maintenanceType ? result.maintenanceType : '-'}}</td>
                        <td style="cursor: pointer;">
                          <img
                            v-if="checkDetail(result)"
                            v-on:click="showMaintanceDetails(result)"
                            width="20"
                            src="/images/icon/detail.svg"
                          />
                          <div v-if="!checkDetail(result)">-</div>
                        </td>
                      </tr>
                    </tbody>
                  </table>

                  <div class="no-results d-none"  v-text="resources['no_results']"></div>

                  <div style="justify-content: space-between;" class="row">
                    <div></div>
                    <div class="rate-text">PM Compliance Rate: {{!isNaN(executionRate)?Math.round(executionRate*100)/100:executionRate}}%</div>
                  </div>
                  <div v-if="files.length > 0 " style="margin-top: 20px;" class="row">
                    <div style="margin-left: 15px;" v-text="resources['attached_files']+':'"></div>
                    <div style="width: 65%;margin: 0px 10px;">
                      <template v-for="file in filesShow">
                        <a
                          target="_blank"
                          :href="file.saveLocation"
                          class="hyper-link"
                        >{{file.fileName}}</a>
                        <!-- <div class="hyper-link">
                    {{file.fileName}}
                  </div>
                  <div class="hyper-link">
                    Instruction video
                        </div>-->
                      </template>
                    </div>
                    <div
                      v-on:click="showMore()"
                      v-if="files.length > filesShow.length"
                      class="view-more"
                    v-text="resources['view_more']"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- <div class="modal-footer">
          <div
            v-on:click="reInstallAction"
            style="background: #c8ced3; color: #000; border: none;min-width: 80px;max-width: 80px;font-size: 14px;"
            class="confirm-button"
          v-text="resources['close']"></div>
          <div
            style="min-width: 80px;max-width: 80px;font-size: 14px;"
            v-on:click="finishAction"
            class="confirm-button"
          v-text="resources['confirm']"></div>
        </div> -->
      </div>
    </div>
    <maintance-details :resources="resources"></maintance-details>
  </div>
</template>

<script>
module.exports = {
props: {
        resources: Object,
    },
  components: {
    "maintance-details": httpVueLoader("/components/maintance-detail.vue")
  },
  data() {
    return {
      maintenance: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "alert",
        sort: "id,desc",
        page: 1,
        operatingStatus: "",
        maintenanced: true,
        maintenanceStatus: ""
      },
      executionRate: 0,
      equipmentCode: "",
      toolMaker: "",
      filesShow: [],
      files: []
    };
  },
  methods: {
    readonly: function() {
      return this.reason.findIndex(value => value == "Other") == -1;
    },

    show: function(maintenance) {
      this.maintenance = maintenance;
      console.log("data: ", maintenance);
      this.paging(1);
    },

    checkDetail: function(result) {
      return result.onTimeStatus !== null;
    },

    showMore: function() {
      console.log("this.fule: ", this.files);
      if (this.files.length - this.filesShow.length > 3) {
        this.filesShow = this.files.filter(
          (value, index) => index < 3 + this.filesShow.length
        );
      } else {
        this.filesShow = this.files;
      }
    },

    showMaintanceDetails: function(data) {
      if (!data.confirmed) {
        return;
      }
      var child = Common.vue.getChild(this.$children, "maintance-details");
      if (child != null) {
        data.moldId = this.maintenance.mold.id;
        child.show(data);
      }
    },

    paging: function() {
      let url = `/api/molds/maintenance/history/${this.maintenance.mold.id}`;
      axios
        .get(url)
        .then(response => {
          console.log("response: ", response);
          this.getAttachments();
          this.executionRate = response.data.executionRate;
          this.toolMaker = response.data.toolMaker;
          this.equipmentCode = response.data.equipmentCode;
          this.results = response.data.histories.map((value, index) => {
            value.index = index + 1;
            value.maintenanceType =
              value.maintenanceType == "PREVENTIVE"
                ? "Preventive"
                : "Corrective";
/*
            value.endTimeShow = value.endTime
              ? moment.unix(value.endTime).format("MMM Do, YYYY")
              : "-";
            value.startTimeShow = value.startTime
              ? moment.unix(value.startTime).format("MMM Do, YYYY")
              : "-";
*/

            value.endTimeShow = value.endTime
              ? vm.convertTimestampToDateWithFormat(value.endTime,"YYYY-MM-DD hh:mm:ss")
              : "-";
            value.startTimeShow = value.startTime
              ? vm.convertTimestampToDateWithFormat(value.startTime,"YYYY-MM-DD hh:mm:ss")
              : "-";

            value.engineer = response.data.engineer;
            value.onTimeStatusShow =
              value.onTimeStatus == "ON_TIME"
                ? "On-time"
                : value.onTimeStatus == "OVERDUE"
                ? "Overdue"
                : "-";
            return value;
          });
          console.log("this.results: ", this.results);
          $("#op-maintenance-history-modal").modal("show");
        })
        .catch(function(error) {
          console.log(error);
        });
    },

    getAttachments: function() {
      let url = `/api/file-storage?storageType=MOLD_MAINTENANCE_DOCUMENT&refId=${this.maintenance.mold.id}`;
      axios
        .get(url)
        .then(response => {
          console.log("respons11111e: ", response);
          this.files = response.data;
          if (this.files.length > 3) {
            this.filesShow = this.files.filter((value, index) => index < 3);
          } else {
            this.filesShow = this.files;
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    },

    reInstallAction: function() {
      $("#op-maintenance-alert-modal").modal("hide");
    }
  }
};
</script>
