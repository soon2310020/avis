<template>
  <div
    style="overflow: scroll"
    id="op-refurbishment-history"
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
              <span v-text="resources['tooling_id']"></span>
              {{ data.mold.equipmentCode }}
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
              <h5 v-text="resources['refurbishment_history']"></h5>
              <table
                class="table table-responsive-sm table-striped table-alert"
                style="border-top: 1px solid #c8ced3"
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
                      v-text="resources['status']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['detail']"
                    ></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="result in results">
                    <tr>
                      <td>{{ result.index }}</td>
                      <td>{{ result.failureDateTime }}</td>
                      <td>
                        <span
                          class="label label-success"
                          v-if="result.refurbishmentStatus === 'COMPLETED'"
                          v-text="resources['completed']"
                        ></span>
                        <span
                          class="label label-inactive"
                          v-if="result.refurbishmentStatus === 'DISAPPROVED'"
                          v-text="resources['disapproved']"
                        ></span>
                        <span
                          class="label label-success"
                          v-if="result.refurbishmentStatus === 'APPROVED'"
                          v-text="resources['approved']"
                        ></span>
                        <span
                          class="label label-danger"
                          v-if="result.refurbishmentStatus === 'DISCARDED'"
                          v-text="resources['discarded']"
                        ></span>
                      </td>
                      <td>
                        <span
                          v-if="result.refurbishmentStatus == 'DISCARDED'"
                          style="text-align: center"
                          >-</span
                        >
                        <span
                          v-if="result.refurbishmentStatus != 'DISCARDED'"
                          style="font-size: 26px; cursor: pointer"
                          @click="showDetail(result)"
                        >
                          <i class="fa fa-file-text-o" aria-hidden="true"></i>
                        </span>
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
                      @click="findRefurbishmentHistory(data.pageNumber)"
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
  },
  data() {
    return {
      page: 1,
      type: "",
      pagination: [],
      requestParam: {
        query: "",
        sort: "id,desc",
        page: 1,
        operatingStatus: "",
        equipmentStatus: "",
      },
      data: {
        mold: {},
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
    show: function (data, type) {
      this.data = data;
      console.log("this.data: ", this.data);
      this.type = type;
      $("#op-refurbishment-history").modal("show");
      this.findRefurbishmentHistory();
    },

    findRefurbishmentHistory: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/refurbishment?id=${this.data.mold.id}&` + param;
      axios.get(url).then(function (response) {
        console.log("refurbishment history response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.results = response.data.content.map((value, index) => {
          value.index = index + 1;
          return value;
        });
      });
    },
    showDetail(data) {
      $("#op-refurbishment-history").modal("hide");
      this.$emit("show-detail", data);
    },
  },
  mounted() {},
};
</script>
