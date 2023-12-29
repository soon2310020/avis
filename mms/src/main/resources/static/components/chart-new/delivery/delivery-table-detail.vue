<template>
  <div
    id="op-delivery-table-detail"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    style="overflow-y: scroll;"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div
      style=" align-items: center;display: flex;justify-content: center;"
      class="modal-dialog modal-lg"
      role="document"
    >
      <div style="width: 400px; overflow: scroll;" class="modal-content">
        <div class="modal-header">
          <h4
            style="font-weight: 700;font-size: 17px;"
            class="modal-title"
            id="title-part-chart">Weekly Output</h4>
          <button type="button" class="close" v-on:click="dismissModal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div style="border: none;" class="delivery-detail-wrapper">
            <table class="table table-responsive-sm table-striped">
              <thead>
              <tr class="tr-sort">
                <th class="text-left">
                  <span>Tooling ID</span>
                </th>
                <th>
                  <span>Produced Part</span>
                </th>
              </tr>
              </thead>
              <tbody class="op-list op-list-child">
              <tr v-for="(result, index) in results" :key="index">
                <td class="text-left">{{ result.title }}</td>
                <td>{{ result.data }}</td>
              </tr>
              </tbody>
            </table>
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
    requestParam: Object
  },
  data() {
    return {
      isDoashBoard: false,
      terminalId: null,
      results: [],
      parentId: null,
      showDetailTimestamp: 0
    };
  },
  methods: {
    showDeliveryDetail: function(companyId, parentId) {
      // $('#' + parentId).html();
      this.results = [];
      let currentTime = new Date().getTime();
      if (currentTime - this.showDetailTimestamp < 200)
        return;
      this.showDetailTimestamp = currentTime;
      // $("#op-delivery-table-detail").modal("show");
      $('#' + parentId).html($('.delivery-detail-wrapper').html());
        let requestParam = {
        partId: this.requestParam.partId,
        week: this.requestParam.week,
        companyId: companyId
      };
      let params = Common.param(requestParam);
      axios.get(`/api/chart/graph-otd-details?${params}`).then(response => {
        this.results = response.data;
        this.$nextTick(() => {
          $('#' + parentId).html($('.delivery-detail-wrapper').html());
        });
      });
    },

    dismissModal: function() {
      $("#op-delivery-table-detail").modal("hide");
    }
  },

  mounted() {}
};
</script>
<style scoped>
  .delivery-detail-wrapper {
    max-height: 360px;
    overflow-y: auto;
  }
</style>
