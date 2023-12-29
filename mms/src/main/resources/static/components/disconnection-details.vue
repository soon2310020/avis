<template>
  <div
    style="overflow: scroll"
    id="op-disconnection-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="title-part-chart">
            {{ type === "tooling" ? "Tooling" : "Terminal" }}
            <span v-text="resources['id'] + ':'"></span>
            {{ data.equipmentCode }}
          </h4>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
            v-on:click="onCloseModal()"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submit">
            <div style="border: none; margin-bottom: 0rem" class="card">
              <h5>{{ getTitle() }}</h5>
              <table
                v-if="type == 'terminal'"
                class="table table-responsive-sm table-striped table-alert"
                style="border: 1px solid #c8ced3"
              >
                <thead>
                  <tr>
                    <th
                      style="font-weight: 600"
                      v-text="resources['alert_date_or_time']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['event']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['hh_of_disconx']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['message']"
                    ></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="disconnection in disconnections">
                    <tr>
                      <td style="text-align: left; min-width: 220px">
                        {{ disconnection.createdAtShow }}
                      </td>
                      <td style="text-align: left; min-width: 120px">
                        {{ disconnection.event }}
                      </td>
                      <td style="text-align: left; min-width: 175px">
                        {{
                          disconnection.hoursShow
                            ? disconnection.hoursShow
                            : "-"
                        }}
                      </td>
                      <td style="text-align: left !important">
                        {{
                          disconnection.message ? disconnection.message : "-"
                        }}
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>
              <!-- <table
                  v-if="isConfirm"
                  class="table table-responsive-sm table-striped table-alert"
                  style="border: 1px solid #c8ced3"
                >
                  <thead>
                    <tr>
                      <th style="font-weight: 600;" v-text="resources['no_dot']"></th>
                      <th style="font-weight: 600;" v-text="resources['alert_date']"></th>
                      <th style="font-weight: 600;" v-text="resources['confirmation_date']"></th>
                      <th style="font-weight: 600;"  v-text="resources['confirmed_by']"></th>
                      <th style="font-weight: 600;"  v-text="resources['message']"></th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="disconnected in disconnections">
                      <tr>
                        <td style="text-align: left;">{{ disconnected.index }}</td>
                        <td style="text-align: left;">{{ disconnected.createdAtShow }}</td>
                        <td style="text-align: left;">{{ disconnected.confirmedDateShow }}</td>
                        <td style="text-align: left;">{{ disconnected.confirmedBy }}</td>
                        <td style="text-align: left;">{{ disconnected.message }}</td>
                      </tr>
                    </template>
                  </tbody>
                </table>-->
              <table
                v-if="type == 'tooling'"
                class="table table-responsive-sm table-striped table-alert"
                style="border: 1px solid #c8ced3"
              >
                <thead>
                  <tr>
                    <th
                      style="font-weight: 600"
                      v-text="resources['alert_date_or_time']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['event']"
                    ></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['shot_capitalized']"
                    ></th>
                    <th style="font-weight: 600" v-text="resources['gap']"></th>
                    <th
                      style="font-weight: 600"
                      v-text="resources['message']"
                    ></th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="disconnected in disconnections">
                    <tr>
                      <td style="min-width: 220px">
                        {{ disconnected.createdAtShow }}
                      </td>
                      <td style="min-width: 120px">{{ disconnected.event }}</td>
                      <td>
                        {{ disconnected.shots ? disconnected.shots : "0" }}
                      </td>
                      <td>
                        {{
                          disconnected.event === eventDisconnection
                            ? "-"
                            : disconnected.gap
                            ? disconnected.gap
                            : "0"
                        }}
                      </td>
                      <td style="text-align: left !important">
                        {{ disconnected.message ? disconnected.message : "-" }}
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
                      @click="findDisconnection(data.pageNumber)"
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
          </form>
        </div>
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
      type: "",
      isConfirm: false,
      pagination: [],
      requestParam: {
        sort: "id,desc",
        page: 1,
        notificationStatus: "",
      },
      data: {
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
      disconnections: [],
      eventDisconnection: "Disconnection",
    };
  },
  methods: {
    showDisconnectionDetails: function (data, type, isConfirm) {
      this.data = data;
      console.log("showDisconnectionDetails:::data", data);
      this.type = type;
      this.isConfirm = isConfirm;
      if (type == "tooling") {
        this.findMoldParts();
      } else {
        $("#op-disconnection-details").modal("show");
      }
      this.findDisconnection();
    },

    onCloseModal: function () {
      $("#op-disconnection-details").modal("hide");
    },

    findPreReconnection: function (index, list) {
      console.log("list: ", list);
      console.log("index: ", index);
      for (let i = 0; i < list.length; i++) {
        if (
          i > index &&
          (list[i].event == "Disconnection" || list[i].event == "DISCONNECT")
        ) {
          return list[i];
        }
      }
      return null;
    },

    showHourOfDisconnection: function (index, list) {
      const preReconnection = this.findPreReconnection(index, list);
      console.log("preReconnection: ", preReconnection);
      console.log("preReconnection: ", preReconnection);
      if (preReconnection) {
        let secondsDistance = 0;
        if (isNaN(preReconnection.createdAt)) {
          secondsDistance =
            -moment(preReconnection.createdAt).unix() +
            moment(list[index].createdAt).unix();
        } else {
          secondsDistance =
            -moment.unix(preReconnection.createdAt).unix() +
            moment.unix(list[index].createdAt).unix();
        }
        console.log("secondsDistance: ", secondsDistance);
        let hours = Math.floor(secondsDistance / (60 * 60));
        let minutes = Math.floor(Math.floor(secondsDistance % (60 * 60)) / 60);
        return `${hours} hours ${minutes} minutes`;
      } else {
        return "-";
      }
    },

    getTitle: function () {
      if (this.type === "terminal") {
        // if (this.isConfirm) {
        //   return "Terminal Disconnection History";
        // }
        return "Terminal Disconnection";
      }
      // if (this.isConfirm) {
      //   return "Tooling Disconnection History";
      // }
      return "Tooling Disconnection";
    },

    findDisconnection: function (pageNumber) {
      // Parts 조회
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      var param = Common.param(self.requestParam);
      let url = "";
      if (this.type == "terminal") {
        url = `/api/terminals/disconnect/alert/${this.data.id}?` + param;
      } else {
        url = `/api/molds/disconnect/alert/${this.data.id}?` + param;
      }
      axios.get(url).then(function (response) {
        self.pagination = Common.getPagingData(response.data);
        self.disconnections = response.data.content.map((value, index) => {
          if (value.event == "RECONNECT" || value.event == "Reconnection") {
            value.hoursShow = self.showHourOfDisconnection(
              index,
              response.data.content
            );
          }
          value.index = index + 1;
          value.confirmedDateShow = moment
            .unix(value.confirmedAt)
            .format("MMMM DD YYYY HH:mm:ss");
          /*
          value.createdAtShow = moment
            .unix(value.createdAt)
            .format("MMMM DD YYYY HH:mm:ss");
*/
          let eventAt = value.createdAt;
          if (value.eventAt) eventAt = value.eventAt;

          value.createdAtShow = vm.convertTimestampToDateWithFormat(
            eventAt,
            "MMMM DD YYYY HH:mm:ss"
          );

          value.event =
            value.event === "DISCONNECT" ? "Disconnection" : "Reconnection";

          return value;
        });
      });
    },

    findMoldParts: function () {
      // Parts 조회
      var self = this;
      axios
        .get("/api/molds/" + this.data.id + "/parts")
        .then(function (response) {
          self.parts = response.data.content;
          $("#op-disconnection-details").modal("show");
        });
    },
  },
  mounted() {},
};
</script>
