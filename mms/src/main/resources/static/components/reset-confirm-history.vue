<template>
  <div
    style="overflow: scroll"
    id="op-reset-confirm-history"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <form @submit.prevent="submit">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['counter_id'] + ' :'"></span>
              {{ data.counterCode }}
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
            <div style="border: none" class="card">
              <div>
                <h5 v-text="resources['reset_alert_his']"></h5>
                <table
                  class="table table-responsive-sm table-striped table-alert"
                  style="border: 1px solid #c8ced3"
                >
                  <thead>
                    <tr>
                      <th
                        style="font-weight: 600"
                        v-text="resources['no_dot']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['alert_date']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['completion_date']"
                      ></th>
                      <th
                        style="font-weight: 600; width: 130px"
                        v-text="resources['triggered_by']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['message']"
                      ></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="result in results">
                      <td>{{ result.index }}</td>
                      <td>{{ result.notificationAt }}</td>
                      <td>
                        {{ result.confirmedDate ? result.confirmedDate : "" }}
                      </td>
                      <td>
                        {{ result.confirmedBy ? result.confirmedBy : "-" }}
                      </td>
                      <td style="text-align: left !important">
                        {{ result.message ? result.message : "-" }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
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
                      @click="findResetHistory(data.pageNumber)"
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
        </div>
      </form>
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
      pagination: [],
      requestParam: {
        query: "",
        sort: "id,desc",
        page: 1,

        locationChanged: true,
        operatingStatus: "",
        notificationStatus: "",
      },
      data: {
        equipmentCode: "",
        pagination: [],
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
      results: [],
    };
  },
  methods: {
    show: function (data) {
      this.data = data;

      $("#op-reset-confirm-history").modal("show");
      this.findResetHistory();
    },

    findResetHistory: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/misconfigure?id=${this.data.counterId}&` + param;

      axios.get(url).then(function (response) {
        self.pagination = Common.getPagingData(response.data);
        self.results = response.data.content.map((value, index) => {
          value.index = index + 1;
          /*
          value.notificationAt = value.notificationAt
            ? moment.unix(value.notificationAt).format("MMMM DD YYYY HH:mm:ss")
            : moment.unix(value.createdAt).format("MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = value.confirmedAt
            ? moment.unix(value.confirmedAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
*/

          value.notificationAt = value.notificationAt
            ? vm.convertTimestampToDateWithFormat(
                value.notificationAt,
                "MMMM DD YYYY HH:mm:ss"
              )
            : vm.convertTimestampToDateWithFormat(
                value.createdAt,
                "MMMM DD YYYY HH:mm:ss"
              );
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
  },
  mounted() {},
};
</script>
