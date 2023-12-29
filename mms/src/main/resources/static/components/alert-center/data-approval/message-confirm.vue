<template>
  <base-dialog
    :visible="isVisible"
    :title="modalTitle"
    @close="handleCloseDialog"
    dialog-classes="modal-lg-m"
    :body-styles="{
      padding: '38px 25px 20px 25px',
    }"
  >
    <div class="create-brand-container">
      <div class="form-group line-row-wrapper">
        <textarea
          :placeholder="
            dataState && dataState.placeholder ? dataState.placeholder : ''
          "
          class="form-control"
          id="message"
          v-model="message"
          rows="5"
          style="height: 178px"
        ></textarea>
      </div>
      <div class="d-flex modal__footer" :style="{ marginTop: '36px' }">
        <base-button
          :size="'medium'"
          :type="'cancel'"
          :level="'primary'"
          class="modal__footer__button"
          @click="handleCloseDialog"
        >
          {{ resources["cancel"] }}
        </base-button>
        <base-button
          :size="'medium'"
          :type="'normal'"
          :level="'primary'"
          class="modal__footer__button"
          @click="handleSubmit"
        >
          {{ dataState.buttonTitle }}
        </base-button>
      </div>
    </div>
  </base-dialog>
</template>

<script
  src="/components/alert-center/data-approval/message-confirm.js"
  cus-no-cache
></script>

<style scoped>
.create-brand-container {
  /* margin-bottom: 100px; */
}
.modal__footer {
  margin-top: 100px;
  justify-content: flex-end;
}
.modal__footer__button {
  margin-left: 8px;
}
.head-line {
  left: 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title-text {
  color: rgb(75, 75, 75);
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}
.error-text {
  font-size: 11px;
  color: #ef4444;
  text-align: left;
}
.custom-modal__body {
  padding: 20px 25px !important;
}
.line-row-wrapper {
  margin-bottom: 31px;
}
.modal-footer {
  border: none;
  padding: 19px 32px;
}
</style>
