<template>
  <div
    style="overflow: scroll"
    id="op-refurbishment-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <form>
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['tooling_id']"></span> :
              {{ maintenance.mold.equipmentCode }}
            </h4>
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
            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span
                  class="card-title"
                  v-text="resources['refurbishment_detail']"
                ></span>
              </div>
              <div class="card-body">
                <table
                  style="display: table"
                  id="maintenance-detail-basic-info"
                  class="table table-mold-details"
                >
                  <tbody>
                    <tr>
                      <th v-text="resources['tooling_id']"></th>
                      <td>{{ maintenance.mold.equipmentCode }}</td>
                    </tr>
                    <tr>
                      <th
                        v-text="
                          resources['estimated_extended_life'] +
                          ' (' +
                          resources['shot'] +
                          ')'
                        "
                      ></th>
                      <td>{{ maintenance.estimateExtendedLife }}</td>
                    </tr>
                    <!--
                  <tr>
                    <th>Failure Time</th>
                    <td>{{maintenance.failureDateTime}}</td>
                  </tr>
                  <tr>
                    <th>Failure Reason</th>
                    <td>{{maintenance.failureReason}}</td>
                  </tr>
                  <tr>
                    <th>Number of Backup</th>
                    <td>{{maintenance.numberOfBackups}}</td>
                  </tr>

-->
                    <tr
                      v-if="
                        ['APPROVED', 'COMPLETED'].includes(
                          maintenance.refurbishmentStatus
                        )
                      "
                    >
                      <th
                        v-text="resources['expected_refurbishment_end_time']"
                      ></th>
                      <td>{{ maintenance.expectedEndTimeShow }}</td>
                    </tr>

                    <tr
                      v-if="
                        ['COMPLETED'].includes(maintenance.refurbishmentStatus)
                      "
                    >
                      <th
                        v-text="resources['actual_refurbishment_start_time']"
                      ></th>
                      <td>{{ maintenance.startTimeShow }}</td>
                    </tr>

                    <tr
                      v-if="
                        ['COMPLETED'].includes(maintenance.refurbishmentStatus)
                      "
                    >
                      <th
                        v-text="resources['actual_refurbishment_end_time']"
                      ></th>
                      <td>{{ maintenance.endTimeShow }}</td>
                    </tr>
                    <tr
                      v-if="
                        ['COMPLETED'].includes(maintenance.refurbishmentStatus)
                      "
                    >
                      <th v-text="resources['refurbishment_downtime']"></th>
                      <td>{{ getDownTime() }}</td>
                    </tr>

                    <tr
                      v-if="
                        [
                          'COMPLETED',
                          'END_OF_LIFECYCLE',
                          'MEDIUM_HIGH',
                          'REQUESTED',
                        ].includes(maintenance.refurbishmentStatus)
                      "
                    >
                      <th v-text="resources['cost']"></th>
                      <td>
                        {{ maintenance.cost }} {{ maintenance.currencyType }}
                      </td>
                    </tr>

                    <tr>
                      <th v-text="resources['engineer_in_charge']"></th>
                      <td>
                        {{
                          maintenance.mold.engineerNames &&
                          maintenance.mold.engineerNames.join(", ")
                        }}
                      </td>
                    </tr>
                    <tr>
                      <th v-text="resources['refurbishment_status']"></th>
                      <td>
                        <span
                          class="label label-primary"
                          v-if="maintenance.refurbishmentStatus === 'REQUESTED'"
                          v-text="resources['requested_refurbishment']"
                        ></span>
                        <span
                          class="label label-success"
                          v-if="maintenance.refurbishmentStatus === 'APPROVED'"
                          v-text="resources['approved']"
                        ></span>
                        <span
                          class="label label-success"
                          v-if="maintenance.refurbishmentStatus === 'COMPLETED'"
                          v-text="resources['completed']"
                        ></span>
                        <span
                          class="label label-default"
                          v-if="
                            maintenance.refurbishmentStatus === 'DISAPPROVED'
                          "
                          v-text="resources['disapproved']"
                        ></span>
                      </td>
                    </tr>
                    <tr>
                      <th v-text="resources['document']"></th>
                      <td>
                        <a
                          v-for="file in files"
                          :href="file.saveLocation"
                          class="btn btn-outline-dark btn-sm mr-1 mb-1"
                          target="_blank"
                          >{{ file.fileName }}</a
                        >
                      </td>
                    </tr>

                    <tr>
                      <th v-text="resources['memo']"></th>
                      <td>
                        <textarea
                          readonly
                          style="
                            border: none;
                            resize: none;
                            outline: none;
                            width: 100%;
                          "
                          rows="4"
                          >{{ maintenance.memo }}</textarea
                        >
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div
              class="card"
              v-if="maintenance.refurbishmentStatus === 'DISAPPROVED'"
            >
              <div class="card-header">
                <i class="fa fa-align-justify"></i>
                <span
                  class="card-title"
                  v-text="resources['reason_for_disapproval']"
                ></span>
              </div>
              <div class="card-body">
                <table
                  style="display: table"
                  id="maintenance-detail-disapproval"
                  class="table table-mold-details"
                >
                  <tbody>
                    <tr>
                      <th v-text="resources['disapproval_by']"></th>
                      <td>{{ maintenance.disapprovedBy }}</td>
                    </tr>
                    <tr>
                      <th v-text="resources['date_time']"></th>
                      <td>{{ maintenance.disapprovedAtShow }}</td>
                    </tr>
                    <tr>
                      <th v-text="resources['reason_for_disapproval']"></th>
                      <td>{{ maintenance.disapprovedMessage }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="modal-footer text-center">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
              v-text="resources['close']"
            ></button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  components: {
    "location-history1": httpVueLoader("./location-history.vue"),
  },
  data() {
    return {
      maintenance: {
        mold: {},
      },
      files: [],
    };
  },
  methods: {
    showCorrectiveMaintenanceDetails(maintenance) {
      console.log("detail maintenance");
      this.findMaintenanceDetail(maintenance);
      this.findDocumentFile(maintenance);
      $("#op-refurbishment-details").modal("show");
    },

    findMaintenanceDetail(maintenance) {
      let url = `/api/molds/refurbishment/` + maintenance.id;
      axios.get(url).then((response) => {
        console.log("refurbishment history response: ", response);
        this.maintenance = response.data;
        this.maintenance.endTimeShow = this.maintenance.endTime
          ? moment.unix(this.maintenance.endTime).format("YYYY-MM-DD, HH:mm:ss")
          : "-";
        this.maintenance.startTimeShow = this.maintenance.startTime
          ? moment
              .unix(this.maintenance.startTime)
              .format("YYYY-MM-DD, HH:mm:ss")
          : "-";
        this.maintenance.expectedEndTimeShow = this.maintenance.expectedEndTime
          ? moment
              .unix(this.maintenance.expectedEndTime)
              .format("YYYY-MM-DD, HH:mm:ss")
          : "-";
        this.maintenance.disapprovedAtShow = this.maintenance.disapprovedAt
          ? moment
              .unix(this.maintenance.disapprovedAt)
              .format("YYYY-MM-DD, HH:mm:ss")
          : "-";
      });
    },
    findDocumentFile(maintenance) {
      var param = {
        storageType: "MOLD_REFURBISHMENT",
        refId: maintenance.id,
      };
      let url = `/api/file-storage?` + Common.param(param);
      axios.get(url).then((response) => {
        // if (response.data && response.data.MOLD_REFURBISHMENT && response.data.MOLD_REFURBISHMENT.length > 0) {
        this.files = response.data;
        // }
      });
    },
    getDownTime() {
      let diffTime = this.maintenance.endTime - this.maintenance.startTime;
      let day = Math.floor(diffTime / (24 * 3600));
      diffTime = diffTime % (24 * 3600);

      let hour = Math.floor(diffTime / 3600);
      diffTime = diffTime % 3600;

      let minute = Math.floor(diffTime / 60);
      diffTime = diffTime % 60;

      let result = [];
      if (day > 0) {
        result.push(day + " days");
      }

      if (hour > 0) {
        if (hour < 10) {
          hour = "0" + hour;
        }
        result.push(hour + " hours");
      } else {
        if (day > 0) {
          if (minute > 0) result.push("00 hours");
        }
      }

      if (minute > 0) {
        result.push(minute + " minutes");
      }

      return result.join(" ");
    },
  },
  mounted() {},
};
</script>
<style scoped>
#op-refurbishment-details .card .card-body {
  padding: 0;
}
</style>
