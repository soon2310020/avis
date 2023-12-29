<template>
  <div
    id="op-confirm-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['notification']"></h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <p>{{data.content}}</p>
        </div>
        <div style="justify-content: space-around;" class="modal-footer">
          <div v-on:click="reInstallAction" class="confirm-button">{{data.titleButton}}</div>
          <div v-on:click="finishAction" class="confirm-button">{{data.titleButton2}}</div>
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
      data: {
        content: "",
        title: "Success!",
        titleButton: "Install",
        titleButton2: "Finish",
        finish: function() {},
        reInstall: function() {}
      }
    };
  },
  methods: {
    showConfirm: function(data) {
      this.data = data;
      console.log("data: ", data);
      $("#op-confirm-modal").modal("show");
    },

    finishAction: function() {
      console.log(" this.data.finish: ", this.data);
      this.data.finish();
      $("#op-confirm-modal").modal("hide");
    },

    reInstallAction: function() {
      this.data.reInstall();
      $("#op-confirm-modal").modal("hide");
    }
  }
};
</script>