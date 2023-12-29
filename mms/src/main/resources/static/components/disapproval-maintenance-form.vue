<template>
  <div
    id="op-disapproval-maintenance-form"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-md" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart" v-text="resources['message']"></h4>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <textarea placeholder="Please provide reason for disapproval" class="form-control" id="message" v-model="mainFormData.disapprovedMessage" rows="5"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="submit" class="btn btn-primary" v-text="resources['submit']"></button>
            <button type="button" class="btn btn-secondary" data-dismiss="modal" v-text="resources['close']"></button>
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
      mainFormData: {}
    };
  },
  methods: {
    showModal: function(data) {
      this.mainFormData.id = data.id;
      $("#op-disapproval-maintenance-form").modal("show");
    },
    submit: function() {
      if (!this.mainFormData.disapprovedMessage || !this.mainFormData.disapprovedMessage.trim()) {
        Common.alert("Please enter the message!");
        return false;
      }
      let self=this;
      axios.post(`/api/molds/corrective/${this.mainFormData.id}`,this.requestFormData(), vm.multipartHeader())
        .then(function () {
          $("#op-disapproval-maintenance-form").modal("hide");
          // vm.callbackMessageForm();
          self.$emit('callback-message-form')

        }).catch(function (error) {
          console.log(error.response);
        });
    },
    requestFormData: function() {
      var requestFormData = new FormData();
      requestFormData.append("correctiveAction", 'DISAPPROVE');
      requestFormData.append("payload", JSON.stringify(this.mainFormData));
      return requestFormData;
    },

  },
  mounted() {}
};
</script>
