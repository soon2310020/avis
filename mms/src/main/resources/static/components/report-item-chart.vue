<template>
  <div
    :id="modalId"
    class="modal fade op-capacity-chart"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-part-chart"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title" id="title-part-chart" v-text="chartTitle"></h4>

          <div class="minimize-chart" data-dismiss="modal" aria-label="Close">
            <img src="/images/icon/exit-fullscreen-icon.svg" alt="minimize icon" />
          </div>
        </div>
        <div style="overflow:scroll;" class="modal-body">
          <div class="report-chart-container" style="position: relative">
            <div class="report-chart-wrapper">
              <div class="report-chart-content">
                <canvas class="report-chart" :id="chartId"></canvas>
              </div>
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
      chartId: String,
      modalId: String,
      chartTitle: String,
    },
    data() {
      return {
      };
    },
    methods: {
    },

    computed: {},

    watch: {}
  };

  // end frequently chart

</script>
<style scoped>

</style>
