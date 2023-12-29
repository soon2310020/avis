<template>
  <div
    id="op-reject-edit"
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
            <h4 class="modal-title">Tooling ID: {{ rejectedItem.mold && rejectedItem.mold.equipmentCode }}</h4>
          </div>
          <div style="overflow:auto;" class="modal-body">
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
                  <div class="th"></div>
                  <div class="td">Rejected Part</div>
                </div>
                <div class="table-content edit-table-content" id="edit-table">
                  <table class="table table-responsive-sm table-bordered">
                    <thead>
                    <tr>
                      <th v-for="(rejectedPart, index) in rejectedParts" :key="index" :class="{'th-custom': rejectedPart.isAdditional}">
                        <span v-if="!rejectedPart.isAdditional">{{ rejectedPart.reason }}</span>
                        <div v-else class="custom-reason">
                          <input v-model="rejectedPart.reason" class="reason-input" required />
                          <div class="remove-action" @click="removeRejectReason(index)">&times;</div>
                        </div>
                      </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                      <td v-for="(rejectedPart, index) in rejectedParts" :key="index">
                        <span v-if="rejectedPart.isPastData">{{ rejectedPart.rejectedAmount }}</span>
                        <input v-else v-model="rejectedPart.rejectedAmount" required />
                      </td>
                    </tr>
                    </tbody>
                  </table>
                </div>
                <div class="add-action">
                  <div class="add-icon">
                    <img src="/images/icon/add-circle.svg" @click="addRejectReason" class="img-add" />
                  </div>
                </div>
              </div>

            </div>
            <div class="calculate-table">
              <div class="calculate-table-content">
                <table class="table table-responsive-sm table-bordered">
                  <thead>
                  <tr>
                    <th>Produced Part</th>
                    <th>Total Rejected Parts</th>
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
            <button class="confirm-button" type="submit">Save Changes</button>
            <button @click="closeRejectEditModal" type="button" class="cancel-button">Cancel</button>
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
        rejectedItem: {},
        rejectedParts: [],
        today: null
      };
    },
    methods: {
      showRejectDetail(data) {
        this.rejectedItem = data;
        this.rejectedParts = [];
        this.fixedRejectReasons.forEach(reason => {
          this.rejectedParts.push({
            reason: reason,
            rejectedAmount: '',
            isAdditional: false,
            isPastData: false
          });
        });
        this.bindPastData();
        $('#op-reject-edit').modal('show');
      },
      bindPastData(){
        this.rejectedItem.rejectedPartDetails.forEach(rejectReason => {
          let rejectedPart = this.rejectedParts.filter(rejectedPart => rejectedPart.reason === rejectReason.reason)[0];
          if (rejectedPart) {
            rejectedPart.rejectedAmount = rejectReason.rejectedAmount;
            rejectedPart.isPastData = true;
          } else { // past data is not fixed reason, need add into current
            this.rejectedParts.push({
              reason: rejectReason.reason,
              rejectedAmount: rejectReason.rejectedAmount,
              isPastData: true
            });
          }
        });
      },
      closeRejectEditModal() {
        $('#op-reject-edit').modal('hide');
      },
      addRejectReason() {
        this.rejectedParts.push({
          reason: '',
          rejectedAmount: '',
          isAdditional: true
        });
        setTimeout(() => {
          let element = document.getElementById('edit-table');
          element.scrollLeft = element.scrollWidth;
        }, 50);
      },
      removeRejectReason(index) {
        this.rejectedParts.splice(index, 1);
      },
      submitForm() {
        if (this.totalRejectPart > this.rejectedItem.totalProducedAmount) {
          Common.alert("The total number of rejected parts must be equal or smaller than produced parts");
          return;
        }
        let submittedRejectedPart = this.rejectedParts.map(rejectedPart => {
          return {
            reason: rejectedPart.reason,
            rejectedAmount: rejectedPart.rejectedAmount
          }
        });
        let requestData = [{
          id: this.rejectedItem.id,
          rejectedParts: submittedRejectedPart
        }];
        axios.post('/api/rejected-part/register', requestData).then(() => {
          this.closeRejectEditModal();
          this.$emit('update-done');
        }).catch(error => {
          console.log('error', error.response);
        });
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
        return ((this.totalRejectPart / this.rejectedItem.totalProducedAmount) * 100).toFixed(2);
      }
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
