<template>
  <div
    style="overflow: scroll"
    id="op-cycle-time-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <form @submit.prevent="submit">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['tooling_id']"></span> :
              {{ mold.equipmentCode }}
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
            <div style="border: none; margin-bottom: 0rem" class="card">
              <h5 v-text="resources['cycle_time']"></h5>
              <table
                class="table table-responsive-sm table-striped table-alert"
                style="border: 1px solid #c8ced3"
              >
                <thead>
                  <tr>
                    <th
                      style="font-weight: 600"
                      v-text="resources['alert_date']"
                    ></th>
                    <th
                      style="font-weight: bold"
                      v-text="resources['cycle_time']"
                    ></th>
                    <th
                      style="font-weight: bold"
                      v-text="resources['variance']"
                    ></th>
                    <th
                      style="font-weight: bold"
                      v-text="resources['ct_status']"
                    ></th>
                    <th
                      style="font-weight: bold"
                      v-text="resources['number_of_shots']"
                    ></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="cycleTime in cycleTimes">
                    <tr>
                      <td>{{ cycleTime.createdDateTime }}</td>
                      <td>
                        {{ Number(cycleTime.cycleTime / 10).toFixed(2) }}s
                      </td>
                      <td>
                        {{
                          (
                            ((Number(cycleTime.cycleTime) -
                              Number(cycleTime.contractedCycleTime)) *
                              100) /
                            Number(cycleTime.contractedCycleTime)
                          ).toFixed(2)
                        }}%
                      </td>
                      <td>{{ cycleTime.cycleTimeStatus }}</td>
                      <td>{{ cycleTime.shots ? cycleTime.shots : "0" }}</td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>
            <div class="row">
              <div class="col-md-8">
                <ul class="pagination">
                  <li
                    v-for="(data, index) in pagination"
                    class="page-item"
                    :class="{ active: data.isActive }"
                  >
                    <a
                      class="page-link"
                      @click="findCycleTimes(data.pageNumber)"
                      >{{ data.text }}</a
                    >
                  </li>
                </ul>
              </div>
              <div class="col-auto ml-auto">
                <button
                  data-dismiss="modal"
                  type="button"
                  class="btn btn-success op-close-details"
                  v-text="resources['ok']"
                ></button>
              </div>
            </div>
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
    alertType: String,
  },
  data() {
    return {
      pagination: [],
      requestParam: {
        sort: "id,desc",
        page: 1,
        notificationStatus: "",
      },
      mold: {
        equipmentCode: "",
        counter: {
          equipmentCode: "",
        },
        part: {
          partCode: "",
          category: {
            parent: {},
          },
        },
        location: {
          name: "",
        },
      },
      parts: [],
      cycleTimes: [],
      alertTypeOptions: {
        DAILY: "DAILY",
        WEEKLY: "WEEKLY",
        MONTHLY: "MONTHLY",
      },
    };
  },
  computed: {
    notificationDateColumnTitle() {
      if (this.alertType === this.alertTypeOptions.MONTHLY) return "Month";
      if (this.alertType === this.alertTypeOptions.WEEKLY) return "Week";
      return "Day";
    },
  },
  methods: {
    showCycleTimeDetails: function (mold) {
      this.mold = mold;
      console.log("mold: ", mold);
      this.findMoldParts();
      this.findCycleTimes();
    },
    weekConvert: function (dateString) {
      return `Week ${dateString.substring(4, 6)} ${dateString.substring(0, 4)}`;
    },

    findCycleTimes: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      ``;
      axios
        .get("/api/molds/cycle-time?id=" + this.mold.id + "&" + param)
        .then(function (response) {
          console.log("response: ", response);
          self.pagination = Common.getPagingData(response.data);
          self.cycleTimes = response.data.content.map((value) => {
            /*

            value.notificationDateTimeShow = value.periodType
              ? value.periodType === "MONTHLY"
                ? moment
                    .unix(value.createdAt)
                    .subtract(1, "day")
                    .format("MMM YYYY")
                : value.periodType === "WEEKLY"
                ? moment
                    .unix(value.createdAt)
                    .subtract(1, "day")
                    .format("ll")
                : value.periodType === "DAILY"
                ? moment
                    .unix(value.createdAt)
                    .subtract(1, "day")
                    .format("MMM DD YYYY")
                : moment
                    .unix(value.createdAt)
                    .subtract(1, "day")
                    .format("YYYY-MM-DD HH:mm:ss")
              : moment
                  .unix(value.createdAt)
                  .subtract(1, "day")
                  .format("MMM DD YYYY");
*/

            value.notificationDateTimeShow = self.getNotificationDate(
              value.createdAt
            );

            value.createdAt = moment
              .unix(value.createdAt)
              .format("MMMM DD YYYY HH:mm:ss");
            value.cycleTimeStatus =
              value.cycleTimeStatus === "WITHIN_TOLERANCE"
                ? "Within Tolerance"
                : value.cycleTimeStatus === "OUTSIDE_L1"
                ? "Outside L1"
                : "Outside L2";
            return value;
          });
        });
    },
    getNotificationDateTimeShow(value) {
      if (!value.periodType) {
        return vm.convertTimestampToDateWithFormat(
          moment.unix(value.createdAt).subtract(1, "day").unix(),
          "MMM DD YYYY"
        );
      }
      if (value.periodType === "MONTHLY") {
        return vm.convertTimestampToDateWithFormat(
          moment.unix(value.createdAt).subtract(1, "day").unix(),
          "MMM YYYY"
        );
      }
      if (value.periodType === "WEEKLY") {
        let week = moment.unix(value.createdAt).subtract(1, "day").week();
        if (week < 10) {
          week = "0" + week;
        }
        return (
          "W-" +
          week +
          ", " +
          moment.unix(value.createdAt).subtract(1, "day").format("YYYY")
        );
      }

      if (value.periodType === "DAILY") {
        return vm.convertTimestampToDateWithFormat(
          moment.unix(value.createdAt).subtract(1, "day").unix(),
          "MMM DD YYYY"
        );
      }
      return vm.convertTimestampToDateWithFormat(
        moment.unix(value.createdAt).subtract(1, "day").unix(),
        "YYYY-MM-DD HH:mm:ss"
      );
    },
    getNotificationDate(timestamp) {
      timestamp = timestamp * 1000;
      if (this.alertType === this.alertTypeOptions.MONTHLY)
        return moment(timestamp).format("MMM YYYY");
      if (this.alertType === this.alertTypeOptions.WEEKLY) {
        let week = moment(timestamp).week();
        if (week < 10) {
          week = "0" + week;
        }
        return (
          "W-" +
          week +
          ", " +
          moment(timestamp).subtract(1, "day").format("YYYY")
        );
      }
      return moment(timestamp).subtract(1, "day").format("MMM DD, YYYY");
    },

    findMoldParts: function () {
      // Parts 조회
      var self = this;
      axios
        .get("/api/molds/" + this.mold.id + "/parts")
        .then(function (response) {
          self.parts = response.data.content;
          $("#op-cycle-time-details").modal("show");
        });
    },
  },
  mounted() {},
};
</script>
