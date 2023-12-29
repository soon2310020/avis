<template>
  <div
    style="overflow: scroll;"
    id="op-maintance-details"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form @submit.prevent="submit">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-title" id="title-part-chart" v-text="resources['detail']"></h4>
            <button type="button" class="close" v-on:click="dimiss()" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="card">
              <div class="card-header">
                <i class="fa fa-align-justify"></i> <span v-text="resources['maintenance_info']"></span>
              </div>

              <table class="table table-mold-details">
                <tbody>
                  <!-- <tr>
                    <th>Failure time</th>
                    <td>{{data.failureTime}}</td>
                  </tr>-->
                  <tr>
                    <th v-text="resources['maintenance_start_time']"></th>
                    <td>{{data.startTimeShow}}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['maintenance_end_time']"></th>
                    <td>{{data.endTimeShow}}</td>
                  </tr>
                  <tr>
                    <th v-text="resources['maintenance_downtime']"></th>
                    <td>
                      <span>{{handleDownTime(data)}}</span>
                    </td>
                  </tr>

                  <tr>
                    <th v-text="resources['engineer_in_charge']"></th>
                    <td>{{data.engineer}}</td>
                  </tr>

                  <tr>
                    <th v-text="resources['maintenance_status']"></th>
                    <td>{{ data.onTimeStatusShow ? data.onTimeStatusShow : '-'}}</td>
                  </tr>


                  <tr>
                    <th  v-text="resources['checklist']"></th>
                    <td>
                      <div style="white-space: pre;">{{data.checkList}}</div>
                    </td>
                  </tr>
                  <tr>
                    <th v-text="resources['document']"></th>
                    <td v-if="!isLost">
                         <a v-for="file in files"
                           target="_blank"
                           :href="file.saveLocation"
                           class="btn btn-outline-dark btn-sm mr-1 mb-1"
                         >{{file.fileName}}</a>
                    </td>
                    <td v-if="isLost">
                         <a target="_blank"
                           :href="fileLocation"
                           class="btn btn-outline-dark btn-sm mr-1 mb-1"
                         >{{fName}}</a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer text-center">
            <button v-on:click="dimiss()" type="button" class="btn btn-secondary" v-text="resources['close']"></button>
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
        failureTime: null
      },

      documentFiles: [],
      files: [],
      locations: [1, 2, 3],
      isLost: false,
      fileLocation: "",
      fName: "",
    };
  },
  methods: {
    dimiss: function(){
      $("#op-maintance-details").modal("hide");

    },

    show: function(data) {
      this.data = data;
      console.log("mainteance data", data.moldId);
      console.log("mainteance data", data);
      this.findFiles(data);
    },

    handleDownTime: function(data) {
      if (data.startTime && data.endTime) {
        const secondsDistance = -data.startTime + data.endTime;
        console.log("distance: ", secondsDistance);
        let hours = Math.floor(secondsDistance / (60 * 60));
        let minutes = Math.floor(Math.floor(secondsDistance % (60 * 60)) / 60);
        return `${hours} hours ${minutes} minutes`;
      }
      return "-";
    },

    findFiles(data) {
      //Adding two files that were lost before in PM alert
      if(data.moldId == 5340 && data.endTime == 1608998400 && data.startTime == 1608912000){
        this.isLost = true;
        this.fileLocation = "https://qrcgcustomers.s3-eu-west-1.amazonaws.com/account12320436/12808710_1.pdf?0.3577414459802142";
        this.fName = "PM-T119378-250K"; //T119738 toolid
        $("#op-maintance-details").modal("show");
      }
      else if(data.moldId == 5281 && data.endTime == 1608184800 && data.startTime == 1607994000 ){
        this.isLost = true;
        this.fileLocation = "https://qrcgcustomers.s3-eu-west-1.amazonaws.com/account12320436/12808738_1.pdf?0.13005335162426235";
        this.fName = "1220-082"; //119666 toolId
        $("#op-maintance-details").modal("show");

      }else{
        this.isLost = false;
        var param = {
           storageType: "MOLD_MAINTENANCE",
           refId: data.moldId
        };

        let url = `/api/file-storage?` + Common.param(param);
        axios.get(url).then((response) => {
           this.files = response.data;
           $("#op-maintance-details").modal("show");
        });
      }

    },
  },
  mounted() {
  }
};
</script>
