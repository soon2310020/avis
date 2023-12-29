<template>
  <div
    id="op-confirm-option-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg-m" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['confirm']"></h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div>{{data.title}}  <span v-text="resources['is_disconnected_bcs']+':'"></span></div>
          <template v-if="data.tabType == 'terminal'">
            <label style="margin-top: 10px;" class="btn radio-confirm-modal">
              <input
                style="margin-right: 3px;"
                type="radio"
                name="options"
                value="Terminal power is down"
                autocomplete="off"
                v-model="reason"
              />
              <span v-text="resources['terminal_power']"></span>
            </label>
            <label class="btn radio-confirm-modal">
              <input
                style="margin-right: 3px;"
                type="radio"
                name="options"
                :value="resources['no_internet']"
                autocomplete="off"
                v-model="reason"
              />
              <span v-text="resources['no_internet']"></span>
            </label>
          </template>
          <template v-if="data.tabType === 'tooling'">
            <label style="margin-top: 10px;" class="btn radio-confirm-modal">
              <input
                style="margin-right: 3px;"
                type="radio"
                name="options"
                value="Tooling is relocated to another location"
                autocomplete="off"
                v-model="reason"
              />
              <span>Tooling is relocated to another location</span>
            </label>
            <label style="margin-top: 10px;" class="btn radio-confirm-modal">
              <input
                style="margin-right: 3px;"
                type="radio"
                name="options"
                value="Tooling is under maintenance"
                autocomplete="off"
                v-model="reason"
              />
              <span>Tooling is under maintenance</span>
            </label>
            <label class="btn radio-confirm-modal">
              <input
                style="margin-right: 3px;"
                type="radio"
                name="options"
                value="Tooling power is down"
                autocomplete="off"
                v-model="reason"
              />
              <span>Tooling power is down</span>
            </label>
          </template>
          <label style="padding-bottom: 0px;margin-bottom: 0px;" class="btn radio-confirm-modal">
            <input
              style="margin-right: 3px;"
              type="radio"
              name="options"
              value="Other"
              autocomplete="off"
              v-model="reason"
            />
            Others
          </label>
          <div style="width: 90%;" class="input-confirm-modal" v-if="reason === 'Other'">
            <a-textarea
              style="width: 100%;"
              placeholder="Please specify"
              :autosize="{ minRows: 2, maxRows: 6 }"
              v-model="reasonText"
            />
          </div>
        </div>
        <div class="modal-footer">
          <div
            style="background-color: #c8ced3; color: #000; border: none;"
            v-on:click="reInstallAction"
            class="confirm-button"
            v-text="resources['close']"></div>
          <div v-on:click="finishAction" class="confirm-button" v-text="resources['confirm']"></div>
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
      reason: "Other",
      reasonText: "",
      data: {
        title: "",
        tabType: "terminal",
        param: {
          message: ""
        }
      }
    };
  },
  methods: {
    showConfirm: function(data) {
      this.data = data;
      this.reason =
        data.tabType === "terminal"
          ? "Terminal power is down"
          : "Tooling relocation";
      console.log("data: ", data);
      this.reasonText = '';
      $("#op-confirm-option-modal").modal("show");
    },

    finishAction: function() {
      if (this.reason !== "Other") {
        this.data.param.message = this.reason;
      } else {
        if (!this.reasonText) {
          Common.alert(`Please specify other reasons for ${this.data.tabType} disconnection`);
          return;
        }
        this.data.param.message = this.reasonText;
      }
      if (this.data.param.message == "") {
        Common.alert("Please enter the message !");
        return false;
      }
      console.log("this.data.param: ", this.data.param);
      let self=this;
      axios
        .put(this.data.url, this.data.param)
        .then(response => {
          this.reason =
            this.data.tabType == "terminal"
              ? "Terminal power is down"
              : "Tooling relocation";
          $("#op-confirm-option-modal").modal("hide");
          // vm.callbackMessageForm();
          self.$emit('callback-message-form')
        })
        .catch(function(error) {
          console.log(error.response);
        });
    },

    reInstallAction: function() {
      $("#op-confirm-option-modal").modal("hide");
    }
  }
};
</script>
