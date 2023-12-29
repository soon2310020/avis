<template>
  <div>
    <div
      class="ant-modal-root"
      bodystyle="[object Object]"
      :style="visible ? '' : { display: 'none' }"
    >
      <div
        class="ant-modal-mask"
        :style="visible ? '' : { display: 'none' }"
      ></div>
      <div
        tabindex="-1"
        role="dialog"
        class="ant-modal-wrap ant-modal-centered"
        :style="visible ? '' : { display: 'none' }"
      >
        <div role="document" class="ant-modal" style="width: 355.7px">
          <div
            tabindex="0"
            aria-hidden="true"
            style="width: 0px; height: 0px; overflow: hidden"
          ></div>
          <div class="custom-modal-body-user">
            <div class="d-flex justify-content-start align-items-start">
              <img
                class="warning-icon-model"
                src="/images/icon/warning-icon-png-2757.svg"
              />
              <span class="title"> Save the Changes? </span>
            </div>
            <div
              style="margin-top: 8px"
              class="d-flex justify-content-center align-items-start margin-right-31"
            >
              <span class="message-style">
                Configuration settings were not saved. Do you want to save
                changes?
              </span>
            </div>
            <div
              style="margin-top: 13px"
              class="d-flex justify-content-end margin-right-31"
            >
              <div
                class="d-flex flex-row-reverse footer-style"
                style="gap: 4px"
              >
                <base-button level="primary" type="save" @click="save"
                  >Yes. Save</base-button
                >
                <base-button type="cancel" level="secondary" @click="cancel"
                  >No. Don't Save</base-button
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  // name: "InviteUser",
  props: {
    saveHandler: Function,
    cancelHandler: Function,
  },
  data() {
    return {
      visible: false,
      visibleSuccess: false,
    };
  },
  methods: {
    save() {
      this.visible = false;
      this.saveHandler();
    },
    cancel() {
      this.visible = false;
      this.cancelHandler();
    },
    showSaveAlert() {
      this.visible = true;
      console.log("cancel button data", this.cancelHandler);
    },
  },
  mounted() {},
};
</script>
<style>
.custom-modal-body-user {
  padding: 23px 21.2px 26.35px 26px;
  background: #ffffff;
}

.save-btn:focus {
  outline: 2px solid #c3f2d7;
}

.cancel-btn:focus {
  border: 2px solid #d6dade !important;
}

.ant-modal-body {
  padding: 0;
  border-radius: 6px;
}

.ant-modal-content {
  width: 355.7px;
  height: 154.35px;
  border-radius: 0px !important;
  background: #ffffff;
  box-shadow: 0px 3px 3px rgba(0, 0, 0, 0.16);
}

.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.pre-header {
  height: 10px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  background-color: #52a1ff;
}

.modal-container {
  margin: 0px auto;
}

.main-content {
  padding: 10px 0px;
}

.modal-container-main {
}

.modal-header {
  background-color: #f5f8ff;
}

.modal-header h2 {
  margin-top: 0;
  margin-bottom: 0;
  color: #4e4e4e;
}

.modal-body {
  padding: 20px 30px;
}

@keyframes primary-animation-invite {
  0% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  33% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  66% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  100% {
  }
}

.footer {
  margin-top: 20px;
}

.modal-default-button {
  float: right;
}

.modal-footer {
  border-top: none;
}

.modal-enter {
  opacity: 0;
}

.modal-leave-active {
  opacity: 0;
}

.save-btn:hover {
  background: #3ea662;
  border-radius: 3px;
  color: #ffffff;
}

.save-btn:active {
  background: #3ea662;
  border: 2px solid #c3f2d7;
  color: #ffffff;
}

.cancel-btn:hover {
  background: #f4f4f4;
  /* Background Colour/TOOLTIP BKG */
  color: #595959;

  border: 1px solid #4b4b4b;
}

.cancel-btn:active {
  background: #f4f4f4;
  /* Background Colour/Light Grey_Bkg_border */
  color: #595959;

  border: 2px solid #d6dade;
}

.save-btn,
.save-btn:focus {
  height: 29px;
  width: 80px;
  background: #47b576;
  border-radius: 3px;
  padding: 6px 8px;
  gap: 10px;
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #ffffff;
}

.cancel-btn,
.cancel-btn:focus {
  height: 29px;
  width: 116px;
  background: #ffffff;
  border: 1px solid #d6dade;
  border-radius: 3px;
  padding: 6px 8px;
  gap: 10px;
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #595959;
  margin-right: 10px;
}
.title {
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  /* identical to box height */
  color: #4b4b4b;
}
.message-style {
  font-family: "Helvetica Neue", Helvetica, "Microsoft Sans Serif", Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;

  color: #4b4b4b;
}
.warning-icon-model {
  width: 16px;
  height: 16px;
  margin-right: 4px;
}
</style>
