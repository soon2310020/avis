<template>
  <div
    id="confirm"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"  v-text="resources['Notification']"></h5>
        </div>
        <div class="modal-body">
            {{ message }}
        </div>
        <div v-if="questionRevert" class="modal-footer">
          <button v-on:click="aceptRestore" type="button" class="btn btn-primary btn-sm" style="background-color: #009dd7; color: #fff" v-text="resources['restore']"></button>
          <button v-on:click="finishAction" type="button" class="btn btn-secondary btn-sm" style="background-color: #fff; color: #000" v-text="resources['cancel']"></button>

          <!-- <div v-on:click="aceptRestore" class="confirm-button">Restore</div> -->
          <!-- <div v-on:click="finishAction" class="confirm-button">Cancel</div> -->
        </div>
        <div v-if="!questionRevert" class="modal-footer">
            <button v-on:click="success" type="button" class="btn btn-primary btn-sm" style="background-color: #009dd7; color: #fff" v-text="resources['ok']"></button>
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
      param: Object,
      message: String,
      questionRevert: true
    };
  },
  methods: {
    showConfirm: function(param, createdDateTime) {
      this.param = param;
      console.log("data: ", param);
      this.questionRevert = true
      this.message =  'Your current data will revert to the version from ' + createdDateTime;
      $("#confirm").modal("show");
    },

    finishAction: function() {
    //   console.log(" this.data.finish: ", this.data);
    //   this.data.finish();
    this.$emit('fuction-from-child');
      $("#confirm").modal("hide");
    },
    aceptRestore: function(){
        let sef = this;
      axios.post('/api/version/restore', this.param).then(function (response) {
            console.log(response)
            if("SUCCESS" === response.data){
              console.log("SUCCESS");
                // sef.finishAction();
                // location.reload();
                sef.message = 'success'
                sef.questionRevert = false;
            }
      });
    },
    success: function(){
      $("#confirm").modal("hide");
      location.reload();
    }
  }
};
</script>
