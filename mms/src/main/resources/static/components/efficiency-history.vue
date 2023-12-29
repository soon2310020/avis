<template>
  <div
    style="overflow: scroll"
    id="op-efficiency-history"
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
              <h5 v-text="resources['uptime']"></h5>
              <table
                class="table table-responsive-sm table-striped table-alert"
                style="border: 1px solid #c8ced3"
              >
                <tbody>
                  <tr>
                    <th v-text="resources['alert_date']"></th>
                    <th v-text="resources['uptime']"></th>
                    <th v-text="resources['variance']"></th>
                    <th v-text="resources['uptime_status']"></th>
                    <th v-text="resources['number_of_shots']"></th>
                  </tr>
                  <template v-for="efficiency in efficiencies">
                    <tr>
                      <td>{{ efficiency.notificationDateTime }}</td>
                      <td>{{ Number(efficiency.efficiency).toFixed(2) }}%</td>
                      <td>{{ efficiency.variance.toFixed(2) }}%</td>
                      <td>{{ efficiency.status }}</td>
                      <td>{{ efficiency.shots ? efficiency.shots : "0" }}</td>
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
                      @click="findEffiencyHistory(data.pageNumber)"
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
        query: "",
        status: "",
        sort: "id,desc",
        //page: 1,
        operatingStatus: "",
        equipmentStatus: "",
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
      efficiencies: [],
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
    showEffiencyHistory: function (mold) {
      this.mold = mold;
      this.findMoldParts();
      this.findEffiencyHistory();
    },

    weekConvert: function (dateString) {
      return `Week ${dateString.substring(4, 6)} ${dateString.substring(0, 4)}`;
    },

    findEffiencyHistory: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/efficiency?id=${this.mold.id}&` + param;
      axios.get(url).then(function (response) {
        console.log("response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.efficiencies = response.data.content.map((value) => {
          /*
          value.notificationDateTime = value.periodType
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

          value.notificationDateTime = self.getNotificationDate(
            value.createdAt
          );

          value.variance =
            ((value.efficiency - value.baseEfficiency) * 100) /
            value.baseEfficiency;
          /*
          value.createdAt = moment
            .unix(value.createdAt)
            .format("MMMM DD YYYY HH:mm:ss");
*/

          value.createdAt = vm.convertTimestampToDateWithFormat(
            value.createdAt,
            "MMMM DD YYYY HH:mm:ss"
          );

          value.status =
            value.status === "WITHIN_TOLERANCE"
              ? "Within Tolerance"
              : value.status === "OUTSIDE_L1"
              ? "Outside L1"
              : "Outside L2";
          return value;
        });
      });
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
          $("#op-efficiency-history").modal("show");
        });
    },
  },
  mounted() {},
};
</script>
