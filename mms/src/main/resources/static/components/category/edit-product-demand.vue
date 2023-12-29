<template>
  <div
    id="op-edit-project-demand"
    class="modal fade"
    role="dialog"
    style="overflow-y: scroll"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-lg-m" role="document">
      <div style="box-shadow: 0 0 1px 1px #a5a2a2" class="modal-content">
        <div class="custom-modal-title">
          <div class="modal-title">
            <div class="icon-action product-demand-icon mr-1"></div>
            <span class="modal-title-text"> Edit Product Demand</span>
          </div>

          <div
            class="t-close-button close-button"
            @click="dimissModal"
            aria-hidden="true"
          >
            <i class="icon-close-black"></i>
          </div>
          <div class="head-line"></div>
        </div>

        <div :style="{ 'overflow-y': 'unset' }">
          <div class="card-body" :style="{ 'overflow-x': 'unset !important' }">
            <div class="form-group row">
              <label class="col-md-3 col-form-label" for="name">
                Product
              </label>
              <div class="col-md-9">
                <div class="product-name">
                  {{ currentData.name }}
                </div>
              </div>
            </div>
            <div style="margin: 23px 5px 46px -15px" class="form-group row">
              <label class="col-md-3 col-form-label" for="name">
                Default Weekly Demand:
              </label>
              <div class="col-md-9">
                <input
                  v-model="currentData.weeklyProductionDemand"
                  type="number"
                  class="form-control"
                  style="width: 150px"
                />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-3 col-form-label" for="name">
                Custom Demand:
              </label>
              <div class="col-md-9">
                <div style="margin-bottom: 25px" class="right-header">
                  <div
                    class="switch-status"
                    :class="{
                      'disable-text': categoryStatus.enabled,
                      'enable-text': !categoryStatus.enabled,
                    }"
                  >
                    Off
                  </div>
                  <div style="height: 18px" class="switch-button">
                    <div class="toggleWrapper">
                      <input
                        type="checkbox"
                        v-model="categoryStatus.enabled"
                        class="mobileToggle"
                        value="1"
                      />
                      <label
                        class="change-checkout-status"
                        @click="updateConfigStatus"
                      ></label>
                    </div>
                  </div>
                  <div
                    class="switch-status"
                    :class="{
                      'disable-text': !categoryStatus.enabled,
                      'enable-text': categoryStatus.enabled,
                    }"
                  >
                    On
                  </div>
                </div>
                <div style="display: flex; align-items: center">
                  <div class="input-has-title">
                    <div :class="{ 'disable-text': !categoryStatus.enabled }">
                      Weekly Demand
                    </div>
                    <input
                      v-model="specificWeeklyProductionDemand"
                      :class="{ 'input-disabled': !categoryStatus.enabled }"
                      class="form-control"
                      type="number"
                      style="width: 150px"
                    />
                  </div>
                  <div class="input-has-title">
                    <div :class="{ 'disable-text': !categoryStatus.enabled }">
                      Choose Week
                    </div>
                    <div v-show="categoryStatus.enabled">
                      <week-picker-popup
                        :key="weekKey"
                        @on-close="handleChangeDate"
                        :current-date="currentDate"
                        :date-range="timeRange"
                        :button-type="'secondary'"
                      ></week-picker-popup>
                    </div>
                    <div v-show="!categoryStatus.enabled">
                      <primary-button
                        :title="'Choose Week'"
                        :default-class="'btn-custom btn-disabled'"
                        :prefix="'/images/icon/cta-icon/inactive-arrow-down.svg'"
                      ></primary-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div style="margin-top: 27px" class="modal-footer text-center">
          <a
            @click="confirmSave"
            id="saveEditProductDemand"
            href="javascript:void(0)"
            class="btn-custom btn-invite"
          >
            <span>{{ resources["save"] }}</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const regExNotNumber = /\D/g; // not number
module.exports = {
  props: {
    resources: Object,
  },
  components: {
    "week-picker-popup": httpVueLoader(
      "/components/category/week-picker-popup.vue"
    ),
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
  },
  data() {
    return {
      categoryStatus: {
        enabled: false,
      },
      currentData: {
        name: null,
        weeklyProductionDemand: null,
      },
      specificWeeklyProductionDemand: null,
      periodValue: [],
      oldData: null,
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      },
      timeRange: {
        minDate: null,
        maxDate: new Date("2100-01-01"),
      },
      weekKey: 1,
    };
  },
  computed: {},
  mounted() {
    this.initCurrentDate();
    this.periodValue = [this.currentDate.fromTitle.replace("-", "")];
  },
  methods: {
    initCurrentDate() {
      let today = new Date();
      this.currentDate.from = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay()
      );
      this.currentDate.to = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay() + 6
      );
      this.periodType = "WEEKLY";
      this.periodValue = [this.currentDate.fromTitle.replaceAll("-", "")];
    },
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
    },
    showModal(currentData) {
      this.clearForm();
      $(this.getRootId() + "#op-edit-project-demand").modal("show");
      console.log(currentData, "currentData");
      this.currentData = JSON.parse(JSON.stringify(currentData));
      const hasSpecificDemand = Boolean(
        currentData.specificWeeklyProductionDemand !== null
      );
      this.categoryStatus.enabled = hasSpecificDemand;
      this.specificWeeklyProductionDemand = hasSpecificDemand
        ? currentData.specificWeeklyProductionDemand
        : currentData.weeklyProductionDemand;
    },
    dimissModal: function () {
      this.$emit("load-success");
      $(this.getRootId() + "#op-edit-project-demand").modal("hide");
    },
    clearForm() {
      this.currentData = {
        name: null,
        weeklyProductionDemand: null,
      };
      this.specificWeeklyProductionDemand = null;
      this.categoryStatus = {
        enabled: false,
      };
    },
    async saveProductDemand() {
      const { enabled } = this.categoryStatus;
      const payload = { ...this.currentData };
      payload.weeklyProductionDemand = Number(
        this.currentData.weeklyProductionDemand
      );
      payload.specificWeeklyProductionDemand = Number(
        this.currentData.weeklyProductionDemand
      );
      payload.periodValue = this.periodValue;
      payload.periodType = this.periodType;
      if (enabled) {
        payload.specificWeeklyProductionDemand = Number(
          this.specificWeeklyProductionDemand
        );
      }
      console.log("payload::::::::::::::::::::::::::::::::::::::", payload);
      await axios.put(
        `/api/categories/edit-product-demand/${this.currentData.id}`,
        payload
      );
      this.dimissModal();
    },
    confirmSave() {
      const el = document.getElementById("saveEditProductDemand");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        this.saveProductDemand();
      }, 500);
    },
    handleChangeDate(date) {
      console.log("change date", date);
      this.periodValue = [date.fromTitle.replace("-", "")];
    },
  },
  watch: {
    currentDate(newVal) {
      if (newVal) this.weekKey++;
      this.periodValue = [this.currentDate.fromTitle.replace("-", "")];
    },
  },
};
</script>
<style scoped src="/css/data-modal-style.css"></style>

<style scoped>
.col-md-9 {
  padding-top: calc(0.375rem + 1px);
  padding-bottom: calc(0.375rem + 1px);
}

input.mobileToggle:checked + .change-checkout-status:before {
  background: #3585e5 !important;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

.head-line {
  left: 0;
}

.modal-footer {
  border: none;
}

.custom-modal-title .modal-title {
  margin-left: 25px;
  display: inline-flex;
  align-items: center;
}

.image-name-zone img {
  cursor: pointer;
}

.form-check-tooltip span {
  padding-left: 8px;
  padding-right: 8px;
}

.btn-custom.btn-custom-primary {
  color: #3491ff;
}

.product-name {
  background: #fbfcfd;
  border-radius: 6px;
  padding: 4px 15px;
  display: inline-block;
}

.input-has-title {
  display: flex;
  flex-direction: column;
  margin-right: 25px;
}

.input-has-title div {
  font-size: 11px;
  margin-bottom: 2px;
  display: inline-flex;
}

.form-group {
  margin-bottom: 5px;
}

.right-header {
  display: flex;
  align-items: center;
  /* width: 80px; */
  gap: 10px;
  justify-content: space-between;
}

/*start switch button*/
.toggleWrapper {
  padding: 0 !important;
  margin: 0 !important;
}

.toggleWrapper input.mobileToggle {
  opacity: 0;
  position: absolute;
}

.toggleWrapper .change-checkout-status {
  margin-bottom: 0;
  cursor: pointer;
}

.toggleWrapper input.mobileToggle + .change-checkout-status {
  position: relative;
  display: inline-block;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  -webkit-transition: 0.7s ease;
  transition: 0.7s ease;
  height: 15px;
  width: 43px;
  border-radius: 60px;
  background: #fff;
  border: 1px solid #4b4b4b;
}

.toggleWrapper input.mobileToggle:checked + .change-checkout-status {
  border: none;
}

.toggleWrapper input.mobileToggle + .change-checkout-status:before {
  content: "";
  position: absolute;
  display: block;
  -webkit-transition: 0.5s cubic-bezier(0.24, 0, 0.5, 1);
  transition: 0.5s cubic-bezier(0.24, 0, 0.5, 1);
  height: 15px;
  width: 43px;
  border-radius: 18px;
}

.toggleWrapper input.mobileToggle + .change-checkout-status:after {
  content: "";
  position: absolute;
  display: block;
  -webkit-transition: 0.3s cubic-bezier(0.54, 1.6, 0.5, 1);
  transition: 0.3s cubic-bezier(0.54, 1.6, 0.5, 1);
  height: 11px;
  width: 11px;
  top: 1px;
  border-radius: 50px;
  background: whitesmoke;
  left: 1px;
}

.toggleWrapper input.mobileToggle + .change-checkout-status:after {
  /* background: black; */
}

.toggleWrapper input.mobileToggle:checked + .change-checkout-status:before {
  background: #694df9;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

.toggleWrapper input.mobileToggle:checked + .change-checkout-status:after {
  left: 30px;
  top: 2px;
  background: whitesmoke;
}

.disable-text {
  color: #c4c4c4 !important;
  font-size: 12px;
}

.enable-text {
  color: #4b4b4b !important;
  font-size: 12px;
}

.btn-invite {
  border: 2px solid #47b576;
  background-color: #47b576;
  color: #ffffff !important;
  line-height: 11pt;
}

.btn-invite:hover {
  border: 2px solid #3ea662;
  background-color: #3ea662;
}

.btn-invite.primary-animation {
  animation: primary-active-green 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
}

@keyframes primary-active-green {
  0% {
  }

  33% {
    box-shadow: 0 0 0 2px #c3f2d7;
    text-decoration: none !important;
  }

  66% {
    box-shadow: 0 0 0 2px #c3f2d7;
    text-decoration: none !important;
  }

  100% {
  }
}

.input-disabled {
  user-select: none;
  pointer-events: none;
  opacity: 0.5;
}
</style>
