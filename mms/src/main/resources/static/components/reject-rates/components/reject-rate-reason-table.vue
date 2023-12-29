<template>
  <div style="width: 100%">
    <div class="table-reject-rate-header row tr-sort">
      <div class="header-row col-md-4 __sort"
           :class="[{ active: sortField === 'reason' }, isDesc ? 'desc' : 'asc']"
           @click="sortBy('reason')">
        <span v-text="resources['reason']"></span>
        <span class="__icon-sort"></span>
      </div>
      <div class="header-row col-md-4 __sort"
           :class="[{ active: sortField === 'rejectPart' }, isDesc ? 'desc' : 'asc']"
           @click="sortBy('rejectPart')">
        <span v-text="resources['reject_part_input']"></span>
        <span class="__icon-sort"></span>
      </div>
      <div class="header-row col-md-4 __sort"
           :class="[{ active: sortField === 'remark' }, isDesc ? 'desc' : 'asc']"
           @click="sortBy('remark')">
        <span v-text="resources['remark']"></span>
        <span class="__icon-sort"></span>
      </div>
    </div>
    <div style="overflow: auto; max-height: 417px;">
      <div class="table-body">
        <div v-show="isAdding" class="body-row row sticky-row">
          <div class="add-input col-md-4 reject-rate-item">
            <a-input
                :class="{ 'error-field': isDuplicateName }"
                v-model="appendedReason.reason"
                :ref="`input-ref-new`"
            ></a-input>
          </div>
          <div class="buttons-confirm col-md-4 reject-rate-item">
            <button
                class="icon-button add-item-button"
                :disabled="appendedReason.reason.trim() === ''"
                @click.stop="handleAddItem"
            ><span class="icon icon-add"></span></button>
            <button
                class="icon-button"
                @click.stop="handleRemoveDraftItem"
            ><span class="icon icon-remove"></span></button>
          </div>
  <!--        <div class="col-md-4 reject-rate-item">-->
  <!--          <a-input-->
  <!--              v-model="appendedReason.remark"-->
  <!--              :maxLength="2000"-->
  <!--              :ref="`input-ref-new-remark`"-->
  <!--          ></a-input>-->
  <!--        </div>-->
        </div>
        <div  v-for="(item, index) in listReason"
              :key="item.reason" class="body-row row"
              :class="[{'odd-row': index%2 === 0}]">
          <div class="col-md-4 reject-rate-item">
                      <div class="reason">
                        <a-input
                            v-if="isEditingIndex === index"
                            :class="{ 'error-field': isDuplicateName }"
                            v-model="editText"
                        ></a-input>
                        <span class="reason-text" v-else>{{ item.reason }}</span>
                        <div class="action-group-btn" v-if="!item.isDefault">
                          <button class="icon-button"
                                  @click.stop="handleRemoveItem(item.reason)">
                            <span class="icon icon-remove"></span>
                          </button>
                          <button  class="icon-button"
                                  @click.stop="isEditingIndex = index; editText = item.reason"
                                  v-if="isEditingIndex !== index">
                            <span class="icon icon-edit"></span>
                          </button>
                          <button
                              class="icon-button"
                              v-if="isEditingIndex === index"
                              :disabled="item.reason.trim() === ''"
                              @click.stop="applyEditReason()"
                          ><span class="icon icon-add"></span></button>
                        </div>
                      </div>
          </div>
          <div class="col-md-4 reject-rate-item">
            <div class="reject-rate-input-zone">
              <a-input
                style="max-width: 250px; border-radius: 0"
                :ref="`input-ref-${index}`"
                @keyup.enter="nextInput(index)"
                :class="{ 'error-field': errorField.includes(item.reason) }"
                :value="item.rejectedAmount"
                @change="
                  (event) =>
                    handleChangeRejectAmount(item.reason, event.target.value)
                "
              ></a-input>
              <div
                v-if="errorField.includes(item.reason)"
                class="error-hint"
              >
                This field can only contain positive integer numbers or 0.
              </div>
            </div>
          </div>
          <div class="col-md-4 reject-rate-item">
            <div class="reject-rate-input-zone">
              <a-input
                  style="max-width: 250px; border-radius: 0"
                  :ref="`input-ref-${index}`"
                  @keyup.enter="nextInput(index)"
                  :value="item.remark"
                  :maxLength="2000"
                  @change="(event) => handleChangeRemark(item.reason, event.target.value)"
              ></a-input>
            </div>
          </div>
        </div>
      </div>
      <div class="table-footer" v-if="!isAdding">
          <a
            class="btn-link"
            @click.stop="handleAddReason"
          >
            + Add reject rate reason
          </a>
      </div>
    </div>
  </div>
<!--  <table class="table-striped entered-input-zone">-->
<!--    <thead>-->
<!--      <tr-->
<!--        class="tr-sort"-->
<!--        style="border-top: unset; border-radius: 1px 1px 0 0"-->
<!--      >-->
<!--        <th-->
<!--          style="width: 40%"-->
<!--          :class="[{ active: sortField === 'reason' }, isDesc ? 'desc' : 'asc']"-->
<!--          class="__sort"-->
<!--          @click="sortBy('reason')"-->
<!--        >-->
<!--          <span v-text="resources['reason']"></span>-->
<!--          <span class="__icon-sort"></span>-->
<!--        </th>-->
<!--        <th-->
<!--          style="width: 60%"-->
<!--          :class="[-->
<!--            { active: sortField === 'rejectPart' },-->
<!--            isDesc ? 'desc' : 'asc',-->
<!--          ]"-->
<!--          class="__sort"-->
<!--          @click="sortBy('rejectPart')"-->
<!--        >-->
<!--          <span v-text="resources['reject_part_input']"></span>-->
<!--          <span class="__icon-sort"></span>-->
<!--        </th>-->
<!--      </tr>-->
<!--    </thead>-->
<!--    <tbody>-->
<!--    <tr v-if="isAdding">-->
<!--      <td>-->
<!--        <div class="add-input">-->
<!--          <a-input-->
<!--              :class="{ 'error-field': isDuplicateName }"-->
<!--              v-model="appendedReason.reason"-->
<!--          ></a-input>-->
<!--        </div>-->
<!--      </td>-->
<!--      <td>-->
<!--        <div class="buttons-confirm">-->
<!--          <button-->
<!--              class="icon-button"-->
<!--              :disabled="appendedReason.reason.trim() === ''"-->
<!--              @click.stop="handleAddItem"-->
<!--          ><span class="icon icon-add"></span></button>-->
<!--          <button-->
<!--              class="icon-button"-->
<!--              @click.stop="handleRemoveDraftItem"-->
<!--          ><span class="icon icon-remove"></span></button>-->
<!--        </div>-->
<!--      </td>-->
<!--    </tr>-->
<!--      <tr-->
<!--        v-for="(item, index) in listReason"-->
<!--        :key="item.reason"-->
<!--      >-->
<!--        <td>-->
<!--          <div class="reason">-->
<!--            &lt;!&ndash; <a-button-->
<!--              v-if="item.isAppended"-->
<!--              class="custom-ant-btn danger"-->
<!--              shape="circle"-->
<!--              icon="delete"-->
<!--              @click.stop="handleRemoveItem(item.reason)"-->
<!--            ></a-button> &ndash;&gt;-->
<!--&lt;!&ndash;            <button v-if="item.isAppended" class="icon-button" @click.stop="handleRemoveItem(item.reason)">&ndash;&gt;-->
<!--&lt;!&ndash;              <span class="icon icon-remove"></span>&ndash;&gt;-->
<!--&lt;!&ndash;            </button>&ndash;&gt;-->

<!--            <a-input-->
<!--                v-if="isEditingIndex === index"-->
<!--                :class="{ 'error-field': isDuplicateName }"-->
<!--                v-model="editText"-->
<!--            ></a-input>-->
<!--            <span class="reason-text" v-else>{{ item.reason }}</span>-->
<!--            <div class="action-group-btn" v-if="!item.isDefault">-->
<!--              <button class="icon-button"-->
<!--                      @click.stop="handleRemoveItem(item.reason)">-->
<!--                <span class="icon icon-remove"></span>-->
<!--              </button>-->
<!--              <button  class="icon-button"-->
<!--                       @click.stop="isEditingIndex = index; editText = item.reason"-->
<!--                       v-if="isEditingIndex !== index">-->
<!--                <span class="icon icon-edit"></span>-->
<!--              </button>-->
<!--              <button-->
<!--                  class="icon-button"-->
<!--                  v-if="isEditingIndex === index"-->
<!--                  :disabled="item.reason.trim() === ''"-->
<!--                  @click.stop="applyEditReason()"-->
<!--              ><span class="icon icon-add"></span></button>-->
<!--            </div>-->
<!--          </div>-->
<!--        </td>-->
<!--        <td>-->
<!--          <div class="reject-rate-input-zone">-->
<!--            <a-input-->
<!--              style="max-width: 250px; border-radius: 0"-->
<!--              :ref="`input-ref-${index}`"-->
<!--              @keyup.enter="nextInput(index)"-->
<!--              :class="{ 'error-field': errorField.includes(item.reason) }"-->
<!--              :value="item.rejectedAmount"-->
<!--              @change="-->
<!--                (event) =>-->
<!--                  handleChangeRejectAmount(item.reason, event.target.value)-->
<!--              "-->
<!--            ></a-input>-->
<!--            <div-->
<!--              v-if="errorField.includes(item.reason)"-->
<!--              class="error-hint"-->
<!--            >-->
<!--              This field can only contain positive integer numbers or 0.-->
<!--            </div>-->
<!--          </div>-->
<!--        </td>-->
<!--      </tr>-->
<!--    </tbody>-->
<!--    <tfoot v-if="!isAdding">-->
<!--      <tr>-->
<!--        <td colspan="100">-->
<!--          <a-->
<!--            class="btn-link"-->
<!--            @click.stop="handleAddReason"-->
<!--          >-->
<!--            + Add reject rate reason-->
<!--          </a>-->
<!--        </td>-->
<!--      </tr>-->
<!--    </tfoot>-->
<!--  </table>-->
</template>

<script>
module.exports = {
  name: "RejectRateReasonTable",
  props: {
    resources: Object,
    listRejectRateReason: {
      // base data
      type: Array,
      default: [],
    },
    sortField: {
      type: String,
      default: "",
    },
    isDesc: {
      type: Boolean,
      default: false,
    },
    onChangeRejectAmount: {
      type: Function,
      default: () => { },
    },
    onAddReason: {
      type: Function,
      default: () => { },
    },
    onRemoveReason: {
      type: Function,
      default: () => { },
    },
    trigger: Number,
    onChangeRemark: {
      type: Function,
      default: () => { },
    }
  },
  data() {
    return {
      isAdding: false,
      listReason: [], // imitate data
      appendedReason: {
        reason: "",
        rejectedAmount: null,
        remark: "",
        isAppended: true,
        isDefault: false,
      },
      isDuplicateName: false,
      isEditingIndex: null,
      editText: '',
    };
  },
  computed: {
    errorField() {
      return this.listReason
        .filter((item) => {
          if (!item.rejectedAmount) return false;
          if (!Number.isInteger(+item.rejectedAmount)) return true;
          if (Number(item.rejectedAmount) < 0) return true;
          return;
        })
        .map((filteredItem) => filteredItem.reason);
    },
    sortedList() {
      this.listReason.sort((item1, item2) => item1.id - item2.id)
    }
  },
  methods: {
    nextInput(index) {
      if (this.$refs[`input-ref-${index + 1}`]) {
        this.$refs[`input-ref-${index + 1}`][0].focus();
      } else {
        this.$refs["input-ref-0"][0].focus();
      }
    },
    handleAddReason() {
      this.isAdding = true;
      this.editText = '';
      this.isEditingIndex = null;
      setTimeout(() => {
        this.$refs["input-ref-new"].focus();
      }, 700);
    },
    resetAppendedReason() {
      this.appendedReason = {
        reason: "",
        rejectedAmount: "",
        remark: "",
        isAppended: true,
        isDefault: false,
      };
      this.isAdding = false;
    },
    handleAddItem() {
      const { reason } = this.appendedReason;
      if (this.listReason.some((item) => item.reason === reason)) {
        this.isDuplicateName = true;
        return setTimeout(() => {
          this.isDuplicateName = false;
        }, 1000); // auto clear error msg
      }
      if (this.appendedReason) {
        this.onAddReason(this.appendedReason);
        this.resetAppendedReason();
      }
    },
    applyEditReason() {
      console.log('applyEditReason()')
      const reason = this.listReason[this.isEditingIndex].reason
      if (this.listReason.some((item, index) => index !== this.isEditingIndex && item.reason === reason)) {
        this.isDuplicateName = true;
        return setTimeout(() => {
          this.isDuplicateName = false;
        }, 1000); // auto clear error msg
      }
      this.listReason[this.isEditingIndex].reason = this.editText;
      this.editText = null;
      this.isEditingIndex = null;
    },
    handleRemoveDraftItem() {
      this.resetAppendedReason();
    },
    handleRemoveItem(reason) {
      this.onRemoveReason(reason);
    },
    handleChangeRejectAmount(reasonName, value) {
      console.log('handleChangeRejectAmount12', reasonName, value)
      const currentIndex = this.listReason.findIndex(
        (item) => item.reason === reasonName
      );
      this.listReason[currentIndex].rejectedAmount = value;
      this.onChangeRejectAmount(reasonName, value);
      let total = this.listReason.reduce(
        (a, curr) => a + +curr.rejectedAmount,
        0
      );
      this.$emit("update-total-amount", total);
    },
    handleChangeRemark(reasonName, value) {
      const currentIndex = this.listReason.findIndex(
          (item) => item.reason === reasonName
      );
      this.listReason[currentIndex].remark = value;
      this.onChangeRemark(reasonName, value);
    },
    sortBy(sortField) {
      this.$emit("sort", sortField);
    },
  },
  watch: {
    listRejectRateReason(newVal) {
      let dataList = [...newVal];
      if (this.sortField) {
        this.listReason = dataList;
      } else {
        let appendList = dataList.filter(item => item.isAppended);
        let notAppendList = dataList.filter(item => !item.isAppended);
        notAppendList = notAppendList.sort((item1, item2) => item1.reason.localeCompare(item2.reason));
        this.listReason = [...appendList, ...notAppendList];
      }
      console.log('listRejectRateReason after', this.listReason)
      this.$nextTick(() => {
        setTimeout(() => {
          if (this.$refs["input-ref-0"] && this.$refs["input-ref-0"][0]) {
            this.$refs["input-ref-0"][0].focus();
          }
        }, 500)
      })
    },
    errorField() {
      this.$emit("update-error-field", this.errorField);
    },
    trigger() {
      this.resetAppendedReason();
      this.editText ='';
      this.isEditingIndex = null;
    }
  },
};
</script>

<style scoped src="/components/reject-rates/components/reject-rate-reason-table.css" cus-no-cache></style>