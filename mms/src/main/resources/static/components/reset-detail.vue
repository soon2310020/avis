<template>
  <div
    style="overflow: scroll"
    id="op-reset-detail"
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
                <h5 v-text="resources['reset']"></h5>
                <table
                  class="table table-responsive-sm table-striped table-alert"
                  style="border: 1px solid #c8ced3"
                >
                  <thead>
                    <tr>
                      <th
                        style="font-weight: 600"
                        v-text="resources['date_or_time']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['accumulated_shots']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['reset_value']"
                      ></th>
                      <th
                        style="font-weight: 600"
                        v-text="resources['shot_inc_due_reset']"
                      ></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="result in results">
                      <td style="text-align: left">
                        {{ result.createdAtShow }}
                      </td>
                      <td style="text-align: left">
                        {{
                          formatNumber(result.lastShot ? result.lastShot : 0)
                        }}
                      </td>
                      <td style="text-align: left">{{ result.preset }}</td>
                      <td style="text-align: left">
                        {{ result.shotIncrease ? result.shotIncrease : 0 }}
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
      results: [],
    };
  },
  methods: {
    show: function (data, type) {
      this.data = data;
      console.log("this.data: ", this.data);
      $("#op-reset-detail").modal("show");
      this.findResetHistory();
    },

    findResetHistory: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/misconfigure?id=${this.data.counterId}&` + param;
      axios.get(url).then((response) => {
        self.pagination = Common.getPagingData(response.data);
        self.results = response.data.content.map((value, index) => {
          value.index = index + 1;
          value.preset = vm.formatNumber(value.preset);
          /*
          value.createdAtShow = moment
            .unix(value.createdAt)
            .format("MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = value.confirmedAt
            ? moment.unix(value.confirmedAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
*/

          value.createdAtShow = vm.convertTimestampToDateWithFormat(
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
