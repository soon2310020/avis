<template>
  <div
    style="z-index: 9999"
    id="op-location-history"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5
            class="modal-title"
            id="exampleModalLongTitle"
            v-text="title ? title : resources['location_his']"
          ></h5>
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
          <div class="row">
            <div class="col-md-11">
              <h4>{{ mold.equipmentCode }}</h4>
              <ul class="timeline">
                <li v-for="(result, index, id) in results" :id="result.id">
                  <p class="date">
                    <!--{{result.createdDateTime}}-->
                    {{ formatToDateTime(result.createdAt) }}
                  </p>

                  <p class="mb-1" style="margin-left: 32px">
                    <span
                      class="label label-success"
                      v-if="result.moldLocationStatus == 'CONFIRMED'"
                      v-text="resources['confirmed']"
                    ></span>
                    <span
                      class="label label-danger"
                      v-if="result.moldLocationStatus == 'CHANGED'"
                      v-text="resources['changed']"
                    ></span>
                    <strong>{{ result.location.locationCode }},</strong>
                    {{ result.location.name }},
                    {{ result.location.companyName }}
                    <br />
                    {{ result.location.memo }}
                  </p>

                  <p
                    v-if="result.previousLocation"
                    class="mb-1"
                    style="margin-left: 32px"
                  >
                    <!--                    <span-->
                    <!--                        class="label label-success"-->
                    <!--                        v-if="results[results.length - 1].moldLocationStatus == 'CONFIRMED'"-->
                    <!--                        v-text="resources['confirmed']"></span>-->
                    <!--                    <span-->
                    <!--                        class="label label-danger"-->
                    <!--                        v-if="result.moldLocationStatus == 'CHANGED'"-->
                    <!--                        v-text="resources['changed']"></span>-->
                    <strong>{{ result.previousLocation.locationCode }},</strong>
                    {{ result.previousLocation.name }},
                    {{ result.previousLocation.companyName }}
                    <br />
                    {{ result.previousLocation.memo }}
                  </p>

                  <p
                    v-if="result.moldLocationStatus == 'CONFIRMED'"
                    class="text-md text-muted mb-0"
                  >
                    <!--<span class="small">[{{result.updatedDate}}]</span>-->
                    <span class="small"
                      >[{{ formatToDate(result.updatedAt) }}]</span
                    >
                    {{ result.message }}
                  </p>
                </li>
              </ul>
            </div>
          </div>
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
      mold: {
        equipmentCode: "",
        name: "",
        part: {},
      },

      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        id: "",
        query: "",
        sort: "id,desc",
        page: 1,
        size: 1000,
      },
      title: "",
    };
  },
  methods: {
    showLocationHistory: function (mold, title) {
      this.mold = mold;
      this.requestParam.id = mold.id;
      this.title = title;
      this.clearData();
      this.paging(1);

      $("#op-location-history").modal("show");
    },

    onCloseModal: function () {
      $("#op-location-history").modal("hide");
    },
    clearData: function () {
      this.total = 0;
      this.results = [];
      this.pagination = [];
    },

    paging: function (pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(this.requestParam);
      var self = this;
      axios
        .get("/api/molds/locations?" + param)
        .then(function (response) {
          console.log("response: ", response);
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },
  mounted() {
    this.$nextTick(function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      //this.paging(1);
    });
  },
};
</script>
