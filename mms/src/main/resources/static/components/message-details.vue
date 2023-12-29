<template>
  <div
    id="op-message-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-md" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h4
            class="modal-title"
            id="title-part-chart"
          >{{data.equipmentTitle}} : {{data.equipmentCode}}</h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div class="card">
            <div class="card-header">
              <i class="fa fa-align-justify"></i>
              <span>{{data.title}}</span>
            </div>

            <table class="table table-mold-details table-message">
              <tbody>
                <tr>
                  <th  v-text="resources['alert_date']"></th>
                  <td>{{data.alertDate}}</td>
                </tr>
                <tr>
                  <th v-text="resources['confirm_date']"></th>
                  <td>{{data.confirmDate}}</td>
                </tr>
                <tr>
                  <th  v-text="resources['name']"></th>
                  <td>{{data.name}}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="card">
            <div class="card-header">
              <i class="fa fa-align-justify"></i>
              <span>{{data.messageTitle}}</span>
            </div>
            <div class="p-3" v-html="confirmMessage"></div>
          </div>
        </div>

        <div class="modal-footer text-center">
          <button type="button" class="btn btn-secondary" data-dismiss="modal" v-text="resources['close']"></button>
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
        title: "",
        messageTitle: "",
        equipmentCode: "",
        alertDate: "",
        confirmDate: "",
        name: "",
        message: ""
      }
    };
  },
  methods: {
    showMessageDetails: function(data) {
      this.data = data;

      if (data.equipmentTitle == undefined) {
        this.data.equipmentTitle = "Tooling ID";
      }

      $("#op-message-details").modal("show");
    }
  },
  computed: {
    confirmMessage: function() {
      if (this.data.message != null) {
        return this.data.message.split("\n").join("<br />");
      }

      return "";
    }
  },
  mounted() {}
};
</script>