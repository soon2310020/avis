<template>
  <div
    id="op-qr-scan"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <div style="margin: auto;" class="modal-dialog modal-md" role="document">
      <div style="background: #545454;min-height: 100vh;" class="modal-content">
        <div class="modal-header qr-header">
          <img
            v-on:click="closeModal"
            class="qr-back"
            width="25"
            src="/images/icon/back-qr-icon.png"
          />
          <div class="library-button">
            <img width="30" src="/images/icon/image-icon.png" />
            <div style="margin-left: 5px;margin-bottom: 2px; font-size: 15px" v-text="resources['library']"></div>
            <qrcode-capture @decode="onDecode"></qrcode-capture>
          </div>
        </div>
        <div class="qr-body modal-body">
          <div class="qr-top-title" v-text="resources['scan_qr_code']"></div>
          <div class="qr-top-content" v-text="resources['align_the_qr_code']"></div>
          <div class="scan-body">
            <div class="camera-border camera-border-tl"></div>
            <div class="camera-border camera-border-tr"></div>
            <div class="camera-border camera-border-bl"></div>
            <div class="camera-border camera-border-br"></div>
            <qrcode-stream
              style="width: 265px; height: 265px;"
              v-if="isOpenCamera"
              @decode="onDecode"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
      props: {
            resources: Object
      },

  components: {
    "confirm-modal": httpVueLoader("./confirm-modal.vue")
  },
  data() {
    return {
      qrCode: "",
      isOpenCamera: false,
      callBack: null,
      parent: null,
      isDoashBoard: false,
      pageType: "",
      stopScan: true
    };
  },
  methods: {
    showQrScan: function(func, parent, isDoashBoard, pageType) {
      //pageType is COUNTERS or TERMINALS or null when go to from DoashBoard
      $("#op-qr-scan").modal("show");
      this.isOpenCamera = true;
      this.callBack = func;
      this.parent = parent;
      this.isDoashBoard = isDoashBoard;
      this.pageType = pageType;
    },

    errorCaptured: function(error) {
      switch (error.name) {
        case "NotAllowedError":
          this.errorMessage = "Camera permission denied.";
          break;
        case "NotFoundError":
          this.errorMessage = "There is no connected camera.";
          break;
        case "NotSupportedError":
          this.errorMessage =
            "Seems like this page is served in non-secure context.";
          break;
        case "NotReadableError":
          this.errorMessage =
            "Couldn't access your camera. Is it already in use?";
          break;
        case "OverconstrainedError":
          this.errorMessage = "Constraints don't match any installed camera.";
          break;
        default:
          this.errorMessage = "UNKNOWN ERROR: " + error.message;
      }
      Common.alert(this.errorMessage);
    },
    showConfirmModal: function(code) {
      let data = {};
      if (this.pageType === "COUNTERS") {
        data = {
          title: "Warning!",
          content:
            "The QR code you’re scanning belongs to a terminal. Are you sure you want to proceed?",
          titleButton: "Yes. Proceed to install terminal",
          titleButton2: "No. Scan again",
          finish: () => {
            this.stopScan = false;
          },
          reInstall: () => {
            this.closeModal();
            return (location.href = `/admin/terminals/new?terminalId=${code}`);
          }
        };
      } else {
        data = {
          title: "Warning!",
          content:
            "The QR code you’re scanning belongs to a counter. Are you sure you want to proceed?",
          titleButton: "Yes. Proceed to install counter",
          titleButton2: "No. Scan again",
          finish: () => {
            this.stopScan = false;
          },
          reInstall: () => {
            this.closeModal();
            return (location.href = `/admin/counters/new?counterId=${code}`);
          }
        };
      }
      console.log("this.parent: ", this.parent);
      var child = Common.vue.getChild(this.parent.$children, "confirm-modal");
      if (child != null) {
        child.showConfirm(data);
      }
    },
    onDecode: function(result) {
      console.log("result: ", result);
      let decodedString = "";
      decodedString = result;
      if (!decodedString.trim()) {
        return;
      }
      if (decodedString.startsWith("CMS") || decodedString.startsWith("NCM") || decodedString.startsWith("EMA")) {
        if (this.pageType === "TERMINALS")
          return this.checkForCounter(decodedString, true);
        return this.checkForCounter(decodedString);
      }
      if (decodedString.startsWith("TMS") || decodedString.startsWith("BTM")) {
        if (this.pageType === "COUNTERS")
          return this.checkForTerminal(decodedString, true);
        return this.checkForTerminal(decodedString);
      }
      return Common.alert("Invalidate");
    },

    checkForCounter: function(code, isShowConfirm) {
      axios.get(`/api/counters/qr-scan/${code}`).then(response => {
        console.log("response.data: ", response.data);
        if (!response.data) {
          if (isShowConfirm) {
            return this.showConfirmModal(code);
          }
          this.closeModal();
          if (this.isDoashBoard) {
            return (location.href = `/admin/counters/new?counterId=${code}`);
          }
          this.callBack(code);
        } else {
          this.closeModal();
          ///admin/counters/new?
          if (
            !response.data.equipmentStatus ||
            response.data.equipmentStatus === "AVAILABLE"
          ) {
            return (location.href = `/admin/counters/new?${response.data.id}`);
          }
          return (location.href = `/admin/counters/${response.data.id}`);
        }
      });
    },

    checkForTerminal: function(code, isShowConfirm) {
      axios.get(`/api/terminals/qr-scan/${code}`).then(response => {
        console.log("response.data: ", response.data);
        if (!response.data) {
          if (isShowConfirm) {
            return this.showConfirmModal(code);
          }
          this.closeModal();
          if (this.isDoashBoard) {
            return (location.href = `/admin/terminals/new?terminalId=${code}`);
          }
          this.callBack(code);
        } else {
          this.closeModal();
          var child = Common.vue.getChild(
            this.parent.$children,
            "terminal-details"
          );
          if (child != null) {
            child.showDetails(response.data, true);
          }
          return;
        }
      });
    },

    closeModal: function() {
      this.isOpenCamera = false;
      console.log("this: ", this);
      $("#op-qr-scan").modal("hide");
    }
  }
};
</script>
