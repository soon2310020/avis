<template>
  <div
    style="overflow: scroll;"
    id="op-relocation-confirm-history"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart">
              <span v-text="resources['tooling_id']+':'" ></span>
              {{ data.equipmentCode }}
            </h4>
            <!-- <div style="display: flex;">
             
              <div
                style="border-radius: 5px 0px 0px 5px; margin-left: 10px;"
                v-on:click="changeTab(true)"
                :class="{ 'tab-active': isActive }"
                class="tab-button"
              >Disconnected terminal</div>
              <div
                style="border-radius: 0px 5px 5px 0px;"
                v-on:click="changeTab(false)"
                :class="{ 'tab-active': !isActive }"
                class="tab-button"
              >Disconnected tooling</div>
            </div>-->
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div style="border:none; margin-bottom: 0rem;" class="card">
              <div>
                <h5 v-text="resources['relocation_alert_his']"></h5>
                <table
                  class="table table-responsive-sm table-striped table-alert"
                  style="border: 1px solid #c8ced3"
                >
                  <thead>
                    <tr>
                      <th style="font-weight: 600;" v-text="resources['no_dot']"></th>
                      <th style="font-weight: 600;" v-text="resources['alert_date']"></th>
                      <th style="font-weight: 600;" v-text="resources['confirmation_date']"></th>
                      <th style="font-weight: 600;" v-text="resources['confirmed_by']"></th>
                      <th style="font-weight: 600;"  v-text="resources['message']"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="relocation in relocationHistories">
                      <td>{{ relocation.index }}</td>
                      <td>{{ relocation.notificationDate }}</td>
                      <td>{{ relocation.confirmedDate }}</td>
                      <td>{{ relocation.confirmedBy ? relocation.confirmedBy : '-' }}</td>
                      <td
                        style="text-align: left !important;"
                      >{{ relocation.message ? relocation.message : '-' }}</td>
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
                      @click="findRelocationHistory(data.pageNumber)"
                    >{{ data.text }}</a>
                  </li>
                </ul>
              </div>
              <div class="col-auto ml-auto">
                <button
                  data-dismiss="modal"
                  type="button"
                  class="btn btn-success op-close-details"
                v-text="resources['ok']" ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
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

        locationChanged: true,
        operatingStatus: "",
        notificationStatus: ""
      },
      data: {
        equipmentCode: "",
        pagination: [],
        counter: {
          equipmentCode: ""
        },
        part: {
          partCode: "",
          category: {
            parent: {}
          }
        },
        location: {
          name: ""
        }
      },
      parts: [],
      relocationHistories: []
    };
  },
  methods: {
    show: function(data, type) {
      this.data = data;
      console.log("this.data: ", this.data);
      this.type = type;
      $("#op-relocation-confirm-history").modal("show");
      this.findCorrectiveMaintenanceHistory();
    },

    findCorrectiveMaintenanceHistory: function(pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/locations?id=${this.data.id}&` + param;
      axios.get(url).then(function(response) {
        self.pagination = Common.getPagingData(response.data);
        self.relocationHistories = response.data.content.map((value, index) => {
          value.index = index + 1;
/*
          value.notificationDate = value.notificationAt
            ? moment.unix(value.notificationAt).format("MMMM DD YYYY HH:mm:ss")
            : moment.unix(value.createdAt).format("MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = value.confirmedAt
            ? moment.unix(value.confirmedAt).format("MMMM DD YYYY HH:mm:ss")
            : "-";
*/

          value.notificationDate = value.notificationAt
            ? vm.convertTimestampToDateWithFormat(value.notificationAt,"MMMM DD YYYY HH:mm:ss")
            : vm.convertTimestampToDateWithFormat(value.createdAt,"MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = value.confirmedAt
            ?  vm.convertTimestampToDateWithFormat(value.confirmedAt,"MMMM DD YYYY HH:mm:ss")
            : "-";

          return value;
        });
      });
    }
  },
  mounted() {}
};
</script>
