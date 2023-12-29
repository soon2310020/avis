<template>
  <div
    id="op-reject-edit-multiple"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="title-edit"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <form method="POST" action="/admin/reject-rates" @submit.prevent="submitForm">
          <div class="modal-header">
            <div class="alert alert-dark" role="alert">
              <h5 class="alert-heading mb-0"><i class="fa fa-user"></i> Edit reject rate</h5>
            </div>
            <h4 class="modal-title">Multiple Edit</h4>
          </div>
          <div style="overflow:auto;" class="modal-body" v-if="rejectedItems.length > 0">
            <div class="edit-table">
              <div class="edit-table-header">
                <div class="table-time">
                  <svg width="20" height="20" viewBox="0 0 24 24">
                    <path d="M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z"/>
                  </svg>
                  <span>{{ today }}</span>
                </div>
              </div>
              <div class="edit-table-wrapper">
                <div class="first-column">
                  <div class="th">Tooling ID</div>
                  <div class="td" v-for="(rejectedItem, index) in rejectedItems" :key="index">
                    {{ rejectedItem.mold.equipmentCode }}
                  </div>
                </div>
                <div class="table-content-wrapper">
                  <div class="first-content-row">
                    Rejected Part
                  </div>
                  <div class="table-content edit-table-content" id="edit-table-multiple">
                    <table class="table table-responsive-sm table-bordered">
                      <thead>
                      <tr>
                        <th v-for="(rejectedPart, index) in rejectedItems[0].rejectedParts" :key="index" :class="{'th-custom': rejectedPart.isAdditional}">
                          <span v-if="!rejectedPart.isAdditional">{{ rejectedPart.reason }}</span>
                          <div v-else class="custom-reason">
                            <input v-model="rejectedPart.reason" class="reason-input" required />
                            <div class="remove-action" @click="removeRejectReason(index)">&times;</div>
                          </div>
                        </th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(rejectedItem, index) in rejectedItems" :key="rejectedItem.key">
                        <td v-for="(rejectedPart, rejectPartIndex) in rejectedItem.rejectedParts" :key="rejectPartIndex">
                          <span v-if="rejectedPart.isPastData">{{ rejectedPart.rejectedAmount }}</span>
                          <input v-else v-model="rejectedPart.rejectedAmount" required />
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
                <div class="add-action">
                  <div class="add-icon">
                    <img src="/images/icon/add-circle.svg" @click="addRejectReason" class="img-add" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="confirm-button" type="submit">Save Changes</button>
            <button @click="closeRejectEditMultipleModal" type="button" class="cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
  module.exports = {
    props: {
      fixedRejectReasons: Array
    },
    data() {
      return {
        rejectedItems: [],
        today: null,
        lastReasonShowIndex: 0,
      };
    },
    methods: {
      showRejectMultipleModal(list) {    
        this.rejectedItems = list;
        $('#op-reject-edit-multiple').modal('show');
        let extendedReasons = [];
        this.rejectedItems.forEach((rejectedItem, index) => {
          rejectedItem.key = index;
          let rejectedParts = [];
          this.lastReasonShowIndex = this.fixedRejectReasons.length;
          // init field
          this.fixedRejectReasons.forEach(reason => {
            rejectedParts.push({
              reason: reason,
              rejectedAmount: '',
              isAdditional: false,
              isPastData: false
            });
          });
          // set data
          rejectedItem.rejectedPartDetails.forEach(rejectReason => {
            let rejectedPart = rejectedParts.filter(rejectedPart => rejectedPart.reason === rejectReason.reason)[0];
            if (rejectedPart) {
              rejectedPart.rejectedAmount = rejectReason.rejectedAmount;
              rejectedPart.isPastData = true;
            } else {  // not fixed field
              if (!extendedReasons.includes(rejectReason.reason)) {
                extendedReasons.push(rejectReason.reason);
              }
              // rejectedParts.push({
              //   reason: rejectReason.reason,
              //   rejectedAmount: rejectReason.rejectedAmount,
              //   isPastData: true
              // });
            }
          });
          rejectedItem.rejectedParts = rejectedParts;
        });
        this.rejectedItems.forEach(rejectedItem => {
          let rejectedParts = rejectedItem.rejectedParts;
          extendedReasons.forEach(extendedReason => {
            let rejectedPart = rejectedItem.rejectedPartDetails.filter(rejectedDetail => rejectedDetail.reason === extendedReason)[0];
            let rejectedAmount = '';
            if (rejectedPart) {
              rejectedAmount = rejectedPart.rejectedAmount;
            }
            rejectedParts.push({
              reason: extendedReason,
              rejectedAmount: rejectedAmount,
              isAdditional: false,
              isPastData: false
            })
          });
        });
      },
      closeRejectEditMultipleModal() {
        $('#op-reject-edit-multiple').modal('hide');
      },
      addRejectReason() {
        this.rejectedItems.forEach(rejectedItem => {
          rejectedItem.key++;
          rejectedItem.rejectedParts.push({
            reason: '',
            rejectedAmount: '',
            isAdditional: true
          });
        });
        this.$forceUpdate();
        setTimeout(() => {
          let element = document.getElementById('edit-table-multiple');
          element.scrollLeft = element.scrollWidth;
        }, 50);
        this.lastReasonShowIndex++;
      },
      removeRejectReason(index) {
        this.rejectedItems.forEach(rejectedItem => {
          rejectedItem.rejectedParts.splice(index, 1);
        });
        this.$forceUpdate();
      },
      submitForm() {
        for(let index = 0 ; index < this.rejectedItems.length; index++) {
          let rejectedItem = this.rejectedItems[index];
          if (this.getTotalRejectPart(rejectedItem.rejectedParts) > rejectedItem.totalProducedAmount) {
            Common.alert(`The total number of rejected parts of tooling ${rejectedItem.mold.equipmentCode} must be equal or smaller than produced parts`);
            return;
          }
        }
        let requestData = [];
        this.rejectedItems.forEach(rejectedItem => {
          let submittedRejectedPart = rejectedItem.rejectedParts.map((rejectedPart, index) => {
            let reason = this.rejectedItems[0].rejectedParts[index].reason;
            return {
              reason: reason,
              rejectedAmount: rejectedPart.rejectedAmount
            }
          });
          requestData.push({
            id: rejectedItem.id,
            rejectedParts: submittedRejectedPart
          });
        });


        axios.post('/api/rejected-part/register', requestData).then(() => {
          this.closeRejectEditMultipleModal();
          this.$emit('update-done');
        }).catch(error => {
          console.log('error', error.response);
        });
      },
      getTotalRejectPart(rejectedParts) {
        let amountParams = rejectedParts.filter(rejectPart => !!rejectPart.rejectedAmount).map(rejectPart => rejectPart.rejectedAmount);
        if (!amountParams.length) {
          return 0;
        }
        return amountParams.reduce((a, b) => parseInt(a) + parseInt(b), 0);
      }
    },

    computed: {
    },
    mounted() {
      this.today = moment().format('YYYY-MM-DD');
    },

    watch: {}
  };

</script>
<style scoped>
  .modal-footer {
    display: flex;
    justify-content: center;
  }
</style>