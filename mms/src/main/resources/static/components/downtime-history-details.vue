<template>
  <div
      style="overflow: scroll;"
      id="op-downtime-history-details"
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
              {{ "Tooling" }}
              <span v-text="resources['id']+':'"></span>
              {{ data.equipmentCode }}
            </h4>
            <!-- <div style="display: flex;">

              <div
                style="border-radius: 5px 0px 0px 5px; margin-left: 10px;"
                v-on:click="changeTab(true)"
                :class="{ 'tab-active': isActive }"
                class="tab-button"
              v-text="resources['terminal']"></div>
              <div
                style="border-radius: 0px 5px 5px 0px;"
                v-on:click="changeTab(false)"
                :class="{ 'tab-active': !isActive }"
                class="tab-button"
              v-text="resources['tooling']"></div>
            </div>-->
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div style="border:none;" class="card">
              <h5>{{ "Tooling" }}
                <span v-text="resources['downtime']"></span></h5>
              <table
                  class="table table-responsive-sm table-striped table-alert"
                  style="border: 1px solid #c8ced3"
              >
                <thead>
                <tr>
                  <th style="font-weight: 600;" v-text="resources['date_or_time']"></th>
                  <th style="font-weight: 600;" v-text="resources['event']"></th>
                  <th style="font-weight: 600;"  v-text="'Downtime Duration'"></th>
                </tr>
                </thead>
                <tbody>
                <template v-for="downtime in downtimeHistories">
                  <tr>
                    <td>{{ downtime.dateTime }}</td>
                    <td>{{ downtime.event }}</td>
                    <td>{{ downtime.downtimeDuration}}</td>
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
                        @click="findDowntimeHistory(data.pageNumber)"
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
      isActive: true,
      type: "",
      pagination: [],
      requestParam: {
        sort: "id,desc",
        page: 1,
        status: "all",
        id: 1
      },
      data: {
        equipmentCode: "",
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
      downtimeHistories: []
    };
  },
  methods: {
    changeTab: function(isActive) {
      this.isActive = isActive;
    },

    showDowntimeDetails: function(data, type) {
      this.data = data;
      this.type = type;
      $("#op-downtime-history-details").modal("show");
      this.findDowntimeHistory();
    },

    findDowntimeHistory: function(pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      self.requestParam.id = this.data.id;
      var param = Common.param(self.requestParam);
      let url = "/api/molds/downtime" + "?" + param;
      axios.get(url).then(function(response) {
        console.log("response: ", response);
        self.pagination = Common.getPagingData(response.data);
        self.downtimeHistories = response.data.content.map(
            (value, index) => {
              value.index = index + 1;
              value.dateTime = vm.convertTimestampToDateWithFormat(value.createdAt, "MMMM DD YYYY HH:mm:ss");
              value.event = value.downtimeStatus === "DOWNTIME" ? "Downtime" : "Uptime";
              value.downtimeDuration = value.downtimeSeconds
                  ? vm.getDowntimeDuration(value.downtimeSeconds)
                  : "-";

              return value;
            }
        );
      });
    }
  },
  mounted() {}
};
</script>
