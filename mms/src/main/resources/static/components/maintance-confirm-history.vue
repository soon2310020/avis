<template>
  <div
    style="overflow: scroll;"
    id="op-maintance-confirm-history"
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
              <span    v-text="resources['tooling_id']+':'" ></span>
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
            <div style="border:none;" class="card">
              <h5  v-text="resources['confirmed_maintenance_alerts']"></h5>
              <table
                class="table table-responsive-sm table-striped table-alert"
                style="border-top: 1px solid #c8ced3"
              >
                <thead>
                  <tr>
                    <th style="font-weight: 600;" v-text="resources['no_dot']"></th>
                    <th style="font-weight: 600;"  v-text="resources['alert_date']"></th>
                    <th style="font-weight: 600;"  v-text="resources['confirmation_date']"></th>
                    <th style="font-weight: 600;"  v-text="resources['confirmed_by']"></th>
                    <th style="font-weight: 600;"  v-text="resources['checklist']"></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="result in results">
                    <tr>
                      <td>{{ result.index }}</td>
                      <td>{{ result.notificationDate }}</td>
                      <td>{{ result.confirmedDate ? result.confirmedDate : '-' }}</td>
                      <td>{{ result.maintenanceBy ? result.maintenanceBy : '-'}}</td>
                      <td>
                        <div
                          style="white-space: pre; text-align: left !important;"
                        >{{ result.checklist ? result.checklist : '-' }}</div>
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
                      @click="findCorrectiveMaintenanceHistory(data.pageNumber)"
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
        operatingStatus: "",
        equipmentStatus: ""
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
      results: []
    };
  },
  methods: {
    show: function(data, type) {
      this.data = data;
      console.log("this.data: ", this.data);
      this.type = type;
      $("#op-maintance-confirm-history").modal("show");
      this.findCorrectiveMaintenanceHistory();
    },

    findCorrectiveMaintenanceHistory: function(pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = `/api/molds/maintenance?id=${this.data.id}&` + param;
      axios.get(url).then(function(response) {
        console.log("response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.results = response.data.content.map((value, index) => {
          value.index = index + 1;
/*
          value.notificationDate = value.notificationAt
            ? moment.unix(value.notificationAt).format("MMMM DD YYYY HH:mm:ss")
            : moment.unix(value.createdAt).format("MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = moment
            .unix(value.updatedAt)
            .format("MMMM DD YYYY HH:mm:ss");
*/

          value.notificationDate = value.notificationAt
                  ? vm.convertTimestampToDateWithFormat(value.notificationAt,"MMMM DD YYYY HH:mm:ss")
                  : vm.convertTimestampToDateWithFormat(value.createdAt,"MMMM DD YYYY HH:mm:ss");
          value.confirmedDate = value.updatedAt ?
                  vm.convertTimestampToDateWithFormat(value.updatedAt, "MMMM DD YYYY HH:mm:ss") : '-';

          return value;
        });
      });
    }
  },
  mounted() {}
};
</script>
