<template>
  <div
    style="overflow: scroll"
    id="op-submission-history"
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
            <h4
              class="modal-title"
              id="title-part-chart"
              style="padding: unset; background: unset"
            >
              <span v-text="resources['tooling_id']"></span> :
              {{ data.equipmentCode }}
            </h4>
            <button
              type="button"
              class="close"
              v-on:click="onCloseModal()"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div style="border: none" class="card">
              <h5 v-text="resources['data_submission_his']"></h5>
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
                      v-text="resources['status']"
                    ></th>
                    <th
                      v-if="data.userType == 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['approval_disapproval_date']"
                    ></th>
                    <th
                      v-if="data.userType !== 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['confirmation_date']"
                    ></th>
                    <th
                      v-if="data.userType == 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['approved_disapproved_by']"
                    ></th>
                    <th
                      v-if="data.userType !== 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['confirmed_by']"
                    ></th>
                    <th
                      v-if="data.userType == 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['reason']"
                    ></th>
                    <th
                      v-if="data.userType !== 'OEM'"
                      style="font-weight: 600"
                      v-text="resources['message']"
                    ></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="item in histories">
                    <tr>
                      <td>{{ item.index }}</td>
                      <td>{{ item.alertDate }}</td>
                      <td>
                        {{
                          item.notificationStatus === "DISAPPROVED"
                            ? "Disapproved"
                            : "Approved"
                        }}
                      </td>
                      <td>{{ item.confirmedDate }}</td>
                      <td>
                        {{
                          data.userType == "OEM"
                            ? item.approvedBy
                            : item.confirmedBy
                        }}
                      </td>
                      <td>{{ item.reasonShow ? item.reasonShow : "-" }}</td>
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
                      @click="findHistories(data.pageNumber)"
                      >{{ data.text }}</a
                    >
                  </li>
                </ul>
              </div>
              <div class="col-auto ml-auto">
                <button
                  v-on:click="onCloseModal()"
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
      isActive: true,
      pagination: [],
      requestParam: {
        sort: "id,desc",
        page: 1,
      },
      data: {
        equipmentCode: "",
        moldId: "",
      },
      histories: [],
      fromPopup: null,
    };
  },
  methods: {
    changeTab: function (isActive) {
      this.isActive = isActive;
    },

    show: function (data, fromPopup) {
      this.data = data;
      this.fromPopup = fromPopup;
      $(this.getRootToCurrentId()).modal("show");
      this.findHistories(1);
    },
    onCloseModal: function () {
      $(this.getRootToCurrentId()).modal("hide");
      this.$emit("back", this.fromPopup);
    },

    findHistories: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/data-submission/${this.data.id}?sort=id,desc`;
      axios.get(url).then(function (response) {
        console.log("response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.histories = response.data.content.map((value, index) => {
          value.index = index + 1;
          value.reasonShow =
            self.data.userType == "OEM" ? value.reason : value.message;
          /*
          value.alertDate = value.notificationAt
            ? moment.unix(value.notificationAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
            if(self.data.userType == 'OEM'){
               value.confirmedDate = value.approvedAt
            ? moment.unix(value.approvedAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
            } else {
               value.confirmedDate = value.confirmedAt
            ? moment.unix(value.confirmedAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
            }
*/

          value.alertDate = value.notificationAt
            ? vm.convertTimestampToDateWithFormat(
                value.notificationAt,
                "MMMM DD YYYY HH:mm:ss"
              )
            : "-";
          if (self.data.userType == "OEM") {
            value.confirmedDate = value.approvedAt
              ? vm.convertTimestampToDateWithFormat(
                  value.approvedAt,
                  "MMMM DD YYYY HH:mm:ss"
                )
              : "-";
          } else {
            value.confirmedDate = value.confirmedAt
              ? vm.convertTimestampToDateWithFormat(
                  value.confirmedAt,
                  "MMMM DD YYYY HH:mm:ss"
                )
              : "-";
          }

          return value;
        });
      });
    },
  },
  mounted() {},
};
</script>
