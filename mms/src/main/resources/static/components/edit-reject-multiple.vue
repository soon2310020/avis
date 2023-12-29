<template>
  <form
    id="op-reject-edit-multiple"
    method="POST"
    action="/admin/reject-rates"
    @submit.prevent="submitForm"
  >
    <div>
      <div class="alert alert-dark" role="alert">
        <h5 class="alert-heading mb-0">
          <i class="fa fa-user"></i>
          <span v-text="resources['edit_reject_rate']"></span>
        </h5>
      </div>
      <h4 class="modal-title" v-if="rejectedItems.length > 1">Multiple Edit</h4>
      <h4 class="modal-title" v-if="rejectedItems.length == 1"><span v-text="resources['tooling_id']+':'"></span> {{ rejectedItems[0].mold.equipmentCode }}</h4>
    </div>
    <div style="overflow: auto" v-if="rejectedItems.length > 0">
      <div class="edit-table">
        <div class="edit-table-header">
          <div class="table-time box-table-time">
            <span>{{ today }}</span>
          </div>
        </div>
        <div class="edit-table-wrapper">
          <div class="first-column">
            <div class="th" v-if="rejectedItems.length > 1" v-text="resources['tooling_id']"></div>
            <div
              class="td"
              v-for="(rejectedItem, index) in rejectedItems"
              :key="index"
              v-if="rejectedItems.length > 1"
            >
              {{ rejectedItem.mold.equipmentCode }}
            </div>
            <div class="td" style="height: 36px;" v-if="rejectedItems.length == 1"></div>
            <div class="td" style="height: 38px;" v-if="rejectedItems.length == 1" v-text="resources['rejected_part']"></div>
          </div>
          <div class="table-content-wrapper">
            <div class="first-content-row" v-if="rejectedItems.length > 1" v-text="resources['rejected_part']"></div>
            <div
              class="table-content edit-table-content"
              id="edit-table-multiple"
            >
              <table class="table table-responsive-sm table-bordered">
                <thead>
                  <tr>
                    <th
                      v-for="(rejectedPart, index) in rejectedItems[0]
                        .rejectedParts"
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
                  <tr
                    v-for="(rejectedItem, index) in rejectedItems"
                    :key="rejectedItem.key"
                  >
                    <td
                      v-for="(
                        rejectedPart, rejectPartIndex
                      ) in rejectedItem.rejectedParts"
                      :key="rejectPartIndex"
                    >
                      <span v-if="rejectedPart.isPastData">{{
                        rejectedPart.rejectedAmount
                      }}</span>
                      <input
                        :id="`${index}${rejectPartIndex}-focus-input`"
                        class="rejected-amount"
                        v-else
                        v-model="rejectedPart.rejectedAmount"

                        @input="checkInput(`${index}${rejectPartIndex}-focus-input`)"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
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
    </div>

    <div class="calculate-table" v-if="rejectedItems.length == 1">
      <div class="calculate-table-content">
        <table class="table table-responsive-sm table-bordered">
          <thead>
          <tr>
            <th v-text="resources['produced_part']"></th>
            <th v-text="resources['total_rejected_part']"></th>
            <th v-text="resources['yield_rate']"></th>
            <th v-text="resources['reject_rate']"></th>
            <th v-text="resources['edited_by']"></th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>{{ rejectedItems[0].totalProducedAmount }}</td>
            <td>{{ totalRejectPart }}</td>
            <td>{{ yieldRate }}%</td>
            <td>{{ rejectRate }}%</td>
            <td>{{ rejectedItems[0].editedBy }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-footer">
      <div class="card-body text-center">
        <button type="button" class="btn btn-primary" v-text="resources['save_changes']" @click="submitFormValid"></button>
        <button class="btn btn-primary" type="submit" id="btn-submit-hiden" hidden></button>
        <button type="button" @click="cancelEdit" class="btn btn-default" v-text="resources['cancel']"></button>
      </div>
    </div>

    <div id="op-confirm-unfilled" aria-hidden="true" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog modal-dialog-centered modal-md" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ resources['notification'] }}</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="closeForm()">
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
    resources: Object,
    fixedRejectReasons: Array,
  },
  data() {
    return {
      rejectedItems: [],
      rejectedParts: [],
      today: null,
      lastReasonShowIndex: 0,
      isShowEmpty: false,
    };
  },
  methods: {
    cancelEdit() {
      window.location.href = "/admin/reject-rates";
    },

    checkInput(id) {
      let dataInput = document.getElementById(id);
      let patt = /^[0-9]*$/;
      if (!dataInput.value) {
        dataInput.setCustomValidity("Please fill out this field");
      } else if (!patt.test(dataInput.value)) {
        dataInput.setCustomValidity(
          "Invalid data format. This field can only contain numbers"
        );
      } else {
        dataInput.setCustomValidity("");
      }
    },

    focusFirstInput() {
      let stopFindData = false;
      this.rejectedItems.every((rejectedItem, indexRejectItem) => {
        if (rejectedItem.rejectedParts.length > 0 && !stopFindData) {
          rejectedItem.rejectedParts.every(
            (rejectedPart, rejectedPartsIndex) => {
              if (!rejectedPart.isPastData && !stopFindData) {
                document
                  .getElementById(
                    `${indexRejectItem}${rejectedPartsIndex}-focus-input`
                  )
                  .focus();
                stopFindData = true;
              }
              if (!!stopFindData) return false;
              else return true;
            }
          );
        }
        if (!!stopFindData) return false;
        else return true;
      });
    },

    getDataRejected() {
      let url_string = window.location.href;
      let url = new URL(url_string);
      let ids = url.searchParams.get("ids");
      let getDate = url.searchParams.get("startDate");
      console.log("getDATE", getDate);
      axios
        //.get(Page.API_BASE + `?ids=${ids}`)
        .get(Page.API_BASE + `?ids=${ids}&startDate=${getDate}&endDate=${getDate}`)
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
          this.handleDataRejectMultiple(response.data.content);
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    handleDataRejectMultiple(list) {
      this.rejectedItems = list;
      let extendedReasons = [];
      this.rejectedItems.forEach((rejectedItem, index) => {
        rejectedItem.key = index;
        let rejectedParts = [];
        this.lastReasonShowIndex = this.fixedRejectReasons.length;
        this.fixedRejectReasons.forEach((reason) => {
          rejectedParts.push({
            reason: reason,
            rejectedAmount: "",
            isAdditional: false,
            isPastData: false,
          });
        });

        rejectedItem.rejectedPartDetails.forEach((rejectReason) => {
          let rejectedPart = rejectedParts.filter(
            (rejectedPart) => rejectedPart.reason === rejectReason.reason
          )[0];
          if (rejectedPart) {
            rejectedPart.rejectedAmount = rejectReason.rejectedAmount;
            rejectedPart.isPastData = true;
          } else {
            if (!extendedReasons.includes(rejectReason.reason)) {
              extendedReasons.push(rejectReason.reason);
            }
          }
        });
        rejectedItem.rejectedParts = rejectedParts;
        this.rejectedParts = rejectedParts;
      });

      this.rejectedItems.forEach((rejectedItem) => {
        let rejectedParts = rejectedItem.rejectedParts;
        this.rejectedParts = rejectedItem.rejectedParts;
        extendedReasons.forEach((extendedReason) => {
          let rejectedPart = rejectedItem.rejectedPartDetails.filter(
            (rejectedDetail) => rejectedDetail.reason === extendedReason
          )[0];
          let rejectedAmount = "";
          if (rejectedPart) {
            rejectedAmount = rejectedPart.rejectedAmount;
          }
          rejectedParts.push({
            reason: extendedReason,
            rejectedAmount: rejectedAmount,
            isAdditional: false,
            isPastData: rejectedAmount ? true : false,
          });
        });
      });

      setTimeout(() => {
        this.focusFirstInput();
      }, 0);
    },

    addRejectReason() {
      this.rejectedItems.forEach((rejectedItem) => {
        rejectedItem.key++;
        rejectedItem.rejectedParts.push({
          reason: "",
          rejectedAmount: "",
          isAdditional: true,
        });
      });
      this.$forceUpdate();
      setTimeout(() => {
        let element = document.getElementById("edit-table-multiple");
        element.scrollLeft = element.scrollWidth;
        document
          .getElementById(
            `title-header-${this.rejectedItems[0].rejectedParts.length - 1}`
          )
          .focus();
      }, 50);
      this.lastReasonShowIndex++;
    },

    removeRejectReason(index) {
      this.rejectedItems.forEach((rejectedItem) => {
        rejectedItem.rejectedParts.splice(index, 1);
      });
      this.$forceUpdate();
    },

    submitFormValid() {
      //valid
      let numEmpty=0;
      this.rejectedItems.forEach((rejectedItem) => {
        let numEmtyInRow = rejectedItem.rejectedParts.filter(r=>r.rejectedAmount==null || r.rejectedAmount.toString().trim()=='').length;
        numEmpty+=numEmtyInRow;
      });

      console.log("this.rejectedItems ",this.rejectedItems);
      console.log("numEmpty ",numEmpty);
      if (numEmpty > 0) {
        this.isShowEmpty = true;
        $('#op-confirm-unfilled').modal('show');
        return;
      }
      document.getElementById('btn-submit-hiden').click();
    },
    confirmForm(){
      this.isShowEmpty = false;
      $('#op-confirm-unfilled').modal('hide');
      document.getElementById('btn-submit-hiden').click();
    },
    closeForm(){
      this.isShowEmpty = false;
      $('#op-confirm-unfilled').modal('hide');
      // this.$emit('hide-duplicated-form');
    },
    submitForm() {
      for (let index = 0; index < this.rejectedItems.length; index++) {
        let rejectedItem = this.rejectedItems[index];
        if (
          this.getTotalRejectPart(rejectedItem.rejectedParts) >
          rejectedItem.totalProducedAmount
        ) {
          Common.alert(
            `The total number of rejected parts of tooling ${rejectedItem.mold.equipmentCode} must be equal or smaller than produced parts`
          );
          return;
        }
      }
      let requestData = [];
      this.rejectedItems.forEach((rejectedItem) => {
        let submittedRejectedPart = rejectedItem.rejectedParts.map(
          (rejectedPart, index) => {
            let reason = this.rejectedItems[0].rejectedParts[index].reason;
            return {
              reason: reason,
              rejectedAmount: rejectedPart.rejectedAmount || 0,
            };
          }
        );
        requestData.push({
          id: rejectedItem.id,
          rejectedParts: submittedRejectedPart,
        });
      });

      axios
        .post("/api/rejected-part/register", requestData)
        .then(() => {
          this.$emit("update-done");
        })
        .catch((error) => {
          console.log("error", error.response);
        });
    },

    getTotalRejectPart(rejectedParts) {
      let amountParams = rejectedParts
        .filter((rejectPart) => !!rejectPart.rejectedAmount)
        .map((rejectPart) => rejectPart.rejectedAmount);
      if (!amountParams.length) {
        return 0;
      }
      return amountParams.reduce((a, b) => parseInt(a) + parseInt(b), 0);
    },
  },

  computed: {
        totalRejectPart() {
          let amountParams = this.rejectedParts.filter(rejectPart => !!rejectPart.rejectedAmount).map(rejectPart => rejectPart.rejectedAmount);
          if (!amountParams.length) {
            return 0;
          }
          return amountParams.reduce((a, b) => parseInt(a) + parseInt(b), 0);
        },
        yieldRate() {
          return 100 - this.rejectRate;

        },
        rejectRate() {
          return ((this.totalRejectPart / this.rejectedItems[0].totalProducedAmount) * 100).toFixed(2);
        }
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