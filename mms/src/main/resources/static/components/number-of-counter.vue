<template>
  <div
    id="op-number-of-counter"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div
      style="align-items: center; display: flex; justify-content: center"
      class="modal-dialog modal-lg"
      role="document"
    >
      <div style="width: 400px; overflow: scroll" class="modal-content">
        <div class="modal-header">
          <h4
            style="font-weight: 700; font-size: 17px"
            class="modal-title"
            id="title-part-chart"
            v-text="resources['no_od_counters']"
          ></h4>
          <button
            type="button"
            class="close"
            v-on:click="dimissModal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div style="border: none" class="card">
            <table class="table table-mold-details">
              <thead>
                <th
                  style="border: none; text-align: left"
                  v-text="resources['counter_id']"
                ></th>
                <th
                  style="border: none; text-align: left"
                  v-text="resources['tooling_id']"
                ></th>
              </thead>
              <tbody>
                <tr
                  v-for="(result, index, id) in results"
                  :id="result.counterCode"
                >
                  <td
                    style="
                      border: none;
                      color: #2ca9d6;
                      text-align: left;
                      font-size: 13px;
                      font-weight: 500;
                    "
                  >
                    <a
                      :href="`${Common.PAGE_URL.SENSOR}?query=${result.counterCode}`"
                      >{{ result.counterCode }}</a
                    >
                  </td>
                  <td
                    style="
                      border: none;
                      text-align: left;
                      font-size: 13px;
                      font-weight: 500;
                    "
                  >
                    {{ result.toolingCode }}
                  </td>
                </tr>
              </tbody>
            </table>
            <div style="margin-top: 10px; font-weight: 500">
              <span v-text="resources['total']"></span>: {{ results.length }}
              <span v-text="resources['number_counter']"></span
              >{{ ![0, 1].includes(results.length) ? "s" : "" }}
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
      isDoashBoard: false,
      terminalId: null,
      results: [],
    };
  },
  methods: {
    showDetails: function (terminalId) {
      this.terminalId = terminalId;
      $("#op-number-of-counter").modal("show");
    },

    dimissModal: function () {
      $("#op-number-of-counter").modal("hide");
    },
  },

  watch: {
    terminalId: function (newValue) {
      axios
        .get(`/api/terminals/get-counters/${newValue}`)
        .then((response) => {
          console.log("number: response", response);
          this.results = response.data;
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },
  mounted() {},
};
</script>
