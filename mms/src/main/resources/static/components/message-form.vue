<template>
  <div
    id="op-message-form"
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
            <h4 class="modal-title" id="title-part-chart">{{ data.title }}</h4>
            <button
              type="button"
              class="close"
              data-dismiss="modal"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="message">{{ data.messageTitle }}</label>
              <textarea
                :placeholder="data && data.placeholder ? data.placeholder : ''"
                class="form-control"
                id="message"
                v-model="message"
                rows="5"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
              v-text="resources['close']"
            ></button>
            <button type="submit" class="btn btn-primary">
              {{ data.buttonTitle }}
            </button>
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
      data: {
        param: {
          message: "",
        },
      },
      message: "",
    };
  },
  methods: {
    showModal: function (data) {
      this.data = data;
      console.log("data: ", data);
      this.message = data.param.message;
      $("#op-message-form").modal("show");
    },
    submit: function () {
      console.log("this.message: ", this.message);
      if (!this.message || this.message.trim() == "") {
        Common.alert("Please enter the message!");
        return false;
      }
      if (Array.isArray(this.data.param)) {
        this.data.param = this.data.param.map((value) => {
          value.message = this.message;
          return value;
        });
      } else {
        this.data.param.message = this.message;
      }
      const self = this;
      if (this.data.method == "POST") {
        return axios
          .post(this.data.url, this.data.param)
          .then(function (response) {
            this.message = "";
            if (
              response.data != null &&
              !response.data.success &&
              response.data.message
            ) {
              Common.alert(response.data.message);
              return;
            }
            $("#op-message-form").modal("hide");
            self.$emit("callback-message-form");
          })
          .catch(function (error) {
            console.log(error.response);
          });
      }
      axios
        .put(this.data.url, this.data.param)
        .then(function (response) {
          this.message = "";
          if (
            response.data != null &&
            !response.data.success &&
            response.data.message
          ) {
            Common.alert(response.data.message);
            return;
          }
          $("#op-message-form").modal("hide");
          self.$emit("callback-message-form");
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },
  mounted() {},
};
</script>
