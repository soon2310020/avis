<template>
  <div
    id="op-corrective-form"
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
            <h4 class="modal-title" id="title-part-chart"> <span v-text="resources['failure']"></span> ({{param.equipmentCode}})</h4>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="failureTime">
              <span v-text="resources['failure_time']"></span> <span class="badge-require"></span></label>
              <div class="input-group date" id="datetimepicker1" data-target-input="nearest">
                <input
                  type="text"
                  id="failureTime"
                  class="form-control datetimepicker-input"
                  data-target="#failureTime"
                  readonly="readonly"
                  required
                />
                <div
                  class="input-group-append"
                  data-target="#failureTime"
                  data-toggle="datetimepicker"
                >
                  <div class="input-group-text">
                    <i class="fa fa-calendar"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label for="reason"  v-text="resources['reason']" ></label>
              <textarea id="reason"
                        v-model="param.failureReason" class="form-control"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal"  v-text="resources['close']"></button>
            <button type="submit" class="btn btn-primary" v-text="resources['confirm']"></button>
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
      param: {
        moldId: null,
        failureTime: null,
        failureReason: null,
        equipmentCode: null
      }
    };
  },
  methods: {
    showModal: function(mold) {
      this.param = {
        moldId: mold.id,
        equipmentCode: mold.equipmentCode,
        failureTime: null,
        failureReason: null
      };

      this.param.failureTime = moment().unix();
      $('#failureTime').val(moment().format("YYYY-MM-DD HH:mm:ss"));

      $("#op-corrective-form").modal("show");
    },

    submit: function() {

      if (!this.param.failureTime) {
        Common.alert("Please enter the failure time.");
        return;
      }

      console.log('param', this.param);
      var self = this;
      axios
        .post("/api/molds/corrective", this.param)
        .then(function(response) {
          $("#op-corrective-form").modal("hide");
          vm.callbackCorrectiveForm(true);
        })
        .catch(function(error) {
          console.log(error.response);
        });
    }
  },
  mounted() {
    this.$nextTick(function() {});
    let self = this;
    $('#failureTime').datetimepicker({
      'ignoreReadonly': true,
      'focusOnShow': true,
      'format': "YYYY-MM-DD HH:mm:ss",
    });


    $("#failureTime").on("change.datetimepicker", function (e) {
      self.param.failureTime = e.date.unix();
    });
  }
};
</script>
<style>
  .bootstrap-datetimepicker-widget {
    min-width: 260px;
  }
</style>