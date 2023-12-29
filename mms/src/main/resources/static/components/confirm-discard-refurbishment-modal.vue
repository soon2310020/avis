<template>
  <div
    id="discard-refurbishment-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div
      class="modal-dialog modal-md"
      role="document"
      style="top: 20%; max-width: 410px"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['notification']"></h5>
          <button
            type="button"
            class="close"
            data-dismiss="modal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body" style="padding: 16px; padding-bottom: 0px">
          <p v-text="resources['refurbishment.discard.notification.row1']"></p>
          <p>
            <span
              v-text="resources['refurbishment.discard.notification.row2p1']"
            ></span>
            <span
              v-text="resources['refurbishment.discard.notification.row2p2']"
              style="color: #20a8d8"
            ></span>
            <span
              v-text="resources['refurbishment.discard.notification.row2p3']"
            ></span>
          </p>
        </div>
        <div class="modal-footer" style="border-top: unset">
          <div
            style="
              color: #000;
              background: white;
              border: 1.5px solid rgba(0, 0, 0, 0.2);
            "
            v-on:click="cancelAction"
            class="confirm-button"
            v-text="resources['cancel']"
          ></div>
          <div
            v-on:click="finishAction"
            class="confirm-button"
            v-text="resources['yes_discard_it']"
          ></div>
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
      refurbishment: {},
    };
  },
  methods: {
    showConfirm: function (data) {
      this.refurbishment = data;
      $("#discard-refurbishment-modal").modal("show");
    },

    finishAction: function () {
      let self = this;
      axios
        .post("/api/molds/refurbishment/" + this.refurbishment.id + "/discard")
        .then(function (response) {
          $("#discard-refurbishment-modal").modal("hide");
          // vm.callbackMessageForm();
          self.$emit("callback-message-form");
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    cancelAction: function () {
      $("#discard-refurbishment-modal").modal("hide");
    },
  },
};
</script>
<style scoped>
.confirm-button {
  font-size: 14px;
  font-weight: unset;
  height: 35px;
}
</style>
