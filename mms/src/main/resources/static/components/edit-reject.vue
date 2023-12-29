<template>
  <form method="POST" action="/admin/reject-rates" @submit.prevent="submitForm">
    <div>
      <div class="alert alert-dark" role="alert">
        <h5 class="alert-heading mb-0">
          <i class="fa fa-user"></i> Edit reject rate
        </h5>
      </div>
      <h4 class="modal-title">
        Tooling ID: {{ rejectedItem.mold && rejectedItem.mold.equipmentCode }}
      </h4>
    </div>
    <div style="overflow: auto">
      <div class="edit-table">
        <div class="edit-table-header">
          <div class="table-time box-table-time">
            <span>{{ today }}</span>
          </div>
        </div>
        <div class="edit-table-wrapper">
          <div class="first-column">
            <div class="th"></div>
            <div class="td">Rejected Part</div>
          </div>
          <div class="table-content edit-table-content" id="edit-table">
            <table class="table table-responsive-sm table-bordered">
              <thead>
                <tr>
                  <th
                    v-for="(rejectedPart, index) in rejectedParts"
                    :key="index"
                    :class="{ 'th-custom': rejectedPart.isAdditional }"
                  >
                    <span v-if="!rejectedPart.isAdditional">{{
                      rejectedPart.reason
                    }}</span>
                    <div v-else class="custom-reason">
                      <input
                        :id="`title-header-${index}`"
                        v-model="rejectedPart.reason"
                        class="reason-input"
                        required
                      />
                      <div
                        class="remove-action"
                        @click="removeRejectReason(index)"
                      >
                        &times;
                      </div>
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td
                    v-for="(rejectedPart, index) in rejectedParts"
                    :key="index"
                  >
                    <span v-if="rejectedPart.isPastData">{{
                      rejectedPart.rejectedAmount
                    }}</span>
                    <input
                      :id="`input-rejected-part-${index}`"
                      v-else
                      v-model="rejectedPart.rejectedAmount"

                      class="rejected-amount"
                      @input="checkInput(`input-rejected-part-${index}`)"
                      pattern="^[0-9]*$"
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="add-action">
            <div class="add-icon">
              <img
                src="/images/icon/add-circle.svg"
                @click="addRejectReason"
                class="img-add"
              />
            </div>
          </div>
        </div>
      </div>
      <div class="calculate-table">
        <div class="calculate-table-content">
          <table class="table table-responsive-sm table-bordered">
            <thead>
              <tr>
                <th style="width: 120px">Produced Part</th>
                <th style="width: 150px">Total Rejected Part</th>
                <th>Yield Rate</th>
                <th>Reject Rate</th>
                <th>Edited By</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{{ rejectedItem.totalProducedAmount }}</td>
                <td>{{ totalRejectPart }}</td>
                <td>{{ yieldRate }}%</td>
                <td>{{ rejectRate }}%</td>
                <td>{{ rejectedItem.editedBy }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="modal-footer">
      <div class="card-body text-center">
        <button type="button" class="btn btn-primary" @click="submitFormValid">Save Changes</button>
        <button class="btn btn-primary" type="submit" id="btn-submit-hiden" hidden></button>

        <button type="button" @click="cancelEdit" class="btn btn-default">
          Cancel
        </button>
      </div>
    </div>

    <div id="op-confirm-unfilled" aria-hidden="true" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-dialog-centered modal-md" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ resources['notification'] }}</h5>
            <button type="button" class="close custom-close-btn" data-dismiss="modal" aria-label="Close" @click="closeForm()">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <p>The unfilled reason will be assumed as 0. Are you sure you want to proceed?</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary op-modal-ok" @click="confirmForm"
                    v-text="resources['yes']"></button>
            <button type="button" class="btn btn-default" @click="closeForm" v-text="resources['no']"></button>
          </div>
        </div>
      </div>
    </div>
  </form>

</template>

<script>
module.exports = {
  props: {
    // fixedRejectReasons: Array,
    resources:Object,
  },

  data() {
    return {
      fixedRejectReasons: [
        'Black Dot', 'Bubbles', 'Burn Mark', 'Dented', 'Drag Mark',
        'Flashing', 'Flow Mark', 'Gas Mark', 'Loose Burr', 'Missing',
        'Oily', 'Parting Line', 'Pin Mark', 'Wrinkle'
      ],
      rejectedItem: {},
      rejectedParts: [],
      today: null,
      isShowEmpty: false,
    };
  },

  methods: {
    cancelEdit() {
      window.location.href = "/admin/reject-rates";
    },

    checkInput(id) {
      let dataInput = document.getElementById(id);
      let patt = /^[0-9]*$/
      if (!dataInput.value) {
        dataInput.setCustomValidity("Please fill out this field");
      } else if (!patt.test(dataInput.value)) {
        dataInput.setCustomValidity("Invalid data format. This field can only contain numbers");
      } else {
        dataInput.setCustomValidity("");
      }
    },

    focusFirstInput() {
      let stopFindData = false;
      this.rejectedParts.every((item, index) => {
        if (!item.isPastData && !stopFindData) {
          document.getElementById(`input-rejected-part-${index}`).focus();
          stopFindData = true;
        }
      });
    },
    getRejectReasons () {
      const api = '/api/rejected-part/get-reject-rate-reason'
      axios.get(api + '?id=' + this.rejectedItem.id).then((res) => {
        console.log(res.data, 'resss')
        this.fixedRejectReasons = res.data.data
        this.fixedRejectReasons.forEach((reason) => {
          this.rejectedParts.push({
            reason: reason,
            rejectedAmount: "",
            isAdditional: false,
            isPastData: false,
          });
        });
      })
    },
    getDataRejected() {
      let url_string = window.location.href;
      let url = new URL(url_string);
      let id = url.searchParams.get("id");
      let date = url.searchParams.get("date");
      let paramDate="";
      if(date!=null && date!=''){
        paramDate="&startDate="+date+"&endDate="+date;
        this.today =moment(date, "YYYYMMDD").format("YYYY-MM-DD");
      }
      axios
        .get(Page.API_BASE + `?id=${id}` + paramDate)
        .then((response) => {
          let moldByMoldIds = {};
          response.data.content.forEach((item) => {
            if (!moldByMoldIds[item.moldId] && item.mold && item.mold.id) {
              moldByMoldIds[item.moldId] = item.mold;
            }
          });
          response.data.content.sort((first, second) => {
            return first.moldId < second.moldId ? 1 : -1;
          });
          response.data.content.forEach((item, index) => {
            if (index === 0) {
              item.identifiedClass = "odd";
            } else {
              let previousResult = response.data.content[index - 1];
              if (item.moldId === previousResult.moldId) {
                item.identifiedClass = previousResult.identifiedClass;
              } else {
                item.identifiedClass =
                  previousResult.identifiedClass === "odd" ? "even" : "odd";
                item.positionClass = "first";
                previousResult.positionClass = "last";
              }
              if (index === response.data.content.length - 1) {
                item.positionClass = "last";
              }
            }
            item.mold = moldByMoldIds[item.moldId];
          });
          this.rejectedItem = response.data.content[0];
          this.getRejectReasons()
          this.rejectedParts = [];
          this.bindPastData();
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    bindPastData() {
      this.rejectedItem.rejectedPartDetails.forEach((rejectReason) => {
        let rejectedPart = this.rejectedParts.filter(
          (rejectedPart) => rejectedPart.reason === rejectReason.reason
        )[0];
        if (rejectedPart) {
          rejectedPart.rejectedAmount = rejectReason.rejectedAmount;
          rejectedPart.isPastData = true;
        } else {
          this.rejectedParts.push({
            reason: rejectReason.reason,
            rejectedAmount: rejectReason.rejectedAmount,
            isPastData: true,
          });
        }
      });
      setTimeout(() => {
        this.focusFirstInput();
      }, 0);
    },

    addRejectReason() {
      this.rejectedParts.push({
        reason: "",
        rejectedAmount: "",
        isAdditional: true,
      });
      setTimeout(() => {
        let element = document.getElementById("edit-table");
        element.scrollLeft = element.scrollWidth;
        document
          .getElementById(`title-header-${this.rejectedParts.length - 1}`)
          .focus();
      }, 50);
    },
    removeRejectReason(index) {
      this.rejectedParts.splice(index, 1);
    },
    submitFormValid() {
      //valid
      let numEmpty=this.rejectedParts.filter(r=>r.rejectedAmount==null || r.rejectedAmount.toString().trim()=='').length;
      console.log("this.rejectedParts ",this.rejectedParts);
      console.log("numEmpty ",numEmpty);
      if (numEmpty > 0) {
        this.isShowEmpty = true;
        $('#op-confirm-unfilled').modal('show');
        return;
      }
      console.log("click 1");
      document.getElementById('btn-submit-hiden').click();
    },
    confirmForm(){
      this.isShowEmpty = false;
      console.log("click cf");
      $('#op-confirm-unfilled').modal('hide');

      console.log("click 2");
      document.getElementById('btn-submit-hiden').click();
    },
    closeForm(){
      console.log("click close");

      $('#op-confirm-unfilled').modal('hide');
      this.isShowEmpty = false;
    },
    submitForm() {
      if (this.totalRejectPart > this.rejectedItem.totalProducedAmount) {
        Common.alert(
          "The total number of rejected parts must be equal or smaller than produced parts"
        );
        return;
      }
      let submittedRejectedPart = this.rejectedParts.map((rejectedPart) => {
        return {
          reason: rejectedPart.reason,
          rejectedAmount: rejectedPart.rejectedAmount || 0,
        };
      });
      let requestData = [
        {
          id: this.rejectedItem.id,
          rejectedParts: submittedRejectedPart,
        },
      ];
      axios
        .post("/api/rejected-part/register", requestData)
        .then(() => {
          this.$emit("update-done");
        })
        .catch((error) => {
          console.log("error", error.response);
        });
    },
  },

  computed: {
    totalRejectPart() {
      let amountParams = this.rejectedParts
        .filter((rejectPart) => !!rejectPart.rejectedAmount)
        .map((rejectPart) => rejectPart.rejectedAmount);
      if (!amountParams.length) {
        return 0;
      }
      return amountParams.reduce((a, b) => parseInt(a) + parseInt(b), 0);
    },
    yieldRate() {
      return 100 - this.rejectRate;
    },
    rejectRate() {
      return (
        (this.totalRejectPart / this.rejectedItem.totalProducedAmount) *
        100
      ).toFixed(2);
    },
  },

  mounted() {
    this.today = moment().format("YYYY-MM-DD");
    this.getDataRejected();
  },

  watch: {},
};
</script>
<style scoped>
.modal-footer {
  display: flex;
  justify-content: center;
}

.box-table-time {
  border: 1px solid #cccc;
  padding: 5px 10px;
}

.rejected-amount,
.rejected-amount:hover,
.rejected-amount:focus {
  border: 1px solid #ccc !important;
  height: 23px;
}
.modal-footer button{
  min-width: 70px;
}
/*#op-confirm-unfilled .modal-dialog{*/
/*  max-width: 345px;*/
/*}*/
#op-confirm-unfilled .modal-title{
  font-size: 20px;
}
#op-confirm-unfilled .modal-body{
  color: rgba(0,0,0,.85);font-size: 16px;
}
</style>