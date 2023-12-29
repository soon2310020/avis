<template>
  <div
    style="overflow: scroll"
    id="op-efficiency-confirm-history"
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
              <h5 v-text="resources['uptime_alert_his']"></h5>
              <table
                class="table table-responsive-sm table-striped table-alert"
                style="border: 1px solid #c8ced3"
              >
                <tbody>
                  <tr>
                    <th
                      style="font-weight: 600"
                      v-text="resources['no_dot']"
                    ></th>
                    <th style="font-weight: 600">
                      {{ notificationDateColumnTitle }}
                    </th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['confirmation_date']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['confirmed_by']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['message']"
                    ></th>
                  </tr>
                  <template v-for="cycleTime in cycleTimes">
                    <tr>
                      <td>{{ cycleTime.index }}</td>
                      <td>{{ cycleTime.notificationDate }}</td>
                      <td>{{ cycleTime.confirmedDate }}</td>
                      <td>
                        {{
                          cycleTime.confirmedBy ? cycleTime.confirmedBy : "-"
                        }}
                      </td>
                      <td style="text-align: left !important">
                        {{ cycleTime.message ? cycleTime.message : "-" }}
                      </td>
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
                      @click="findEfficiencyHistory(data.pageNumber)"
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
        sort: "id,desc",
        page: 1,
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
      if (this.alertType === this.alertTypeOptions.MONTHLY)
        return "Alert Month";
      if (this.alertType === this.alertTypeOptions.WEEKLY) return "Alert Week";
      return "Alert Day";
    },
  },
  methods: {
    show: function (mold) {
      this.mold = mold;
      $("#op-efficiency-confirm-history").modal("show");
      this.findEfficiencyHistory();
    },

    findEfficiencyHistory: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/efficiency?id=${this.mold.id}&` + param;
      axios.get(url).then((response) => {
        console.log("response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.cycleTimes = response.data.content.map((value, index) => {
          value.index = index + 1;
          value.notificationDate = this.getNotificationDate(value.createdAt);
          value.confirmedDate = value.confirmedAt
            ? vm.convertTimestampToDateWithFormat(
                value.confirmedAt,
                "MMMM DD YYYY HH:mm:ss"
              )
            : "-";
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
  },
  mounted() {},
};
</script>
