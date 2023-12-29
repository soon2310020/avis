<template>
  <div>
    <a-modal
      :visible="visible"
      :class="{ hidden: !visible }"
      width="356"
      :closable="false"
      :footer="null"
      centered
      @close="onClose"
    >
      <div class="modal-body">
        <div style="margin-bottom: 10px; margin-bottom: 10px; display: flex">
          <span style="margin-right: 6px"
            ><a-icon type="warning" theme="filled" style="color: #db3b21"
          /></span>
          <span>
            <p class="popup-title">
              {{ "Are you sure you want to cancel the counter reset?" }}
            </p>
          </span>
        </div>
        <div style="display: flex; justify-content: flex-end">
          <a
            href="javascript:void(0)"
            id="close-request-popup"
            style="margin-right: 18px"
            class="btn-custom btn-custom-close"
            type="primary"
            @click="closeAnimation()"
          >
            Don’t Cancel
          </a>

          <a
            href="javascript:void(0)"
            id="create-request-popup"
            class="btn-custom btn-custom-primary"
            type="primary"
            @click="showAnimation()"
          >
            Yes. Cancel Counter Reset
          </a>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  name: "cancelPopup",
  props: {
    resources: Object,
    resetdata: Function,
  },
  data() {
    return {
      visible: false,
      trigger: false,
      loadingAction: false,
      // namePopup:vm.resources['reset_command'],
      param: {
        ci: "",
        preset: "",
        toolId: "",
        lastPreset: "",
        lastShot: "",
        shotsFromLastPreset: "",
        comment: "",
        forecastedMaxShots: "",
      },
      preset: {
        id: "",
      },
    };
  },
  computed: {
    isValidRequest() {
      return this.isValidForm();
    },
  },
  created() {
    this.initForm({});
    // this.getRequestId()
  },
  mounted() {
    this.trigger = true;
  },
  methods: {
    resetPopup() {
      this.param = {
        ci: "",
        preset: "",
        toolId: "",
        lastPreset: "",
        lastShot: "",
        company: "",
        location: "",
        shotsFromLastPreset: "",
        forecastedMaxShots: "",
        comment: "",
      };
      this.preset = {
        id: "",
      };
    },
    initForm(item, tabType) {
      this.tabType = tabType;
      this.resetPopup();
      console.log("item ", item);
      if (this.tabType == "counter") {
        this.param.ci = item.equipmentCode;
        this.param.lastShot = item.shotCount;
        this.param.location = item.locationName;
      } else {
        this.param.ci = item.counterCode;
        this.param.toolId = item.equipmentCode;
        this.param.lastShot = item.lastShot;
        this.param.company = item.companyName;
        this.param.location = item.locationName;
      }
      //alert(mold.lastShot);
      if (this.param.lastShot == null) {
        this.param.lastShot = "0";
      }
      this.nameDrawer =
        item.presetStatus == "READY"
          ? this.resources["edit_counter_reset"]
          : this.resources["reset_command"];

      var self = this;
      axios.get("/api/presets/" + this.param.ci).then(function (response) {
        if (response.data != "") {
          self.param.lastPreset = response.data.preset;
          self.param.comment = response.data.comment;
          self.param.preset = Number(response.data.preset);
          self.param.forecastedMaxShots = Number(
            response.data.forecastedMaxShots
          );
          console.log("self.param.preset: ", self.param.preset);
          self.preset = response.data;
        }
      });
    },

    onShow(item,tabType) {
      this.initForm(item,tabType);
      this.visible = true;
    },
    onClose() {
      this.visible = false;
      // this.$emit('close')
      this.resetPopup();
    },
    submitRequest() {
      var self = this;
      console.log(this.preset)
      axios
        .put("/api/presets/" + this.preset.id, this.preset)
        .then(function (response) {
          console.log(response);
          self.resetPopup();
          self.$emit("reload-page");
          self.resetdata();
          self.onClose();
          console.log("Run");
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    callbackPresetForm: function (result) {
      if (result == true) {
        Common.alert("success");
      }
      this.paging(1);
    },
    showAnimation() {
      const el = document.getElementById("create-request-popup");
      el.classList.add("primary-animation");
      setTimeout(() => {
        el.classList.remove("primary-animation");
        this.submitRequest();
      }, 700);
    },
    closeAnimation() {
      const el = document.getElementById("close-request-popup");
      el.classList.add("light-animation");
      setTimeout(() => {
        el.classList.remove("light-animation");
        this.onClose();
      }, 700);
    },
  },
};
</script>

<style>
.popup-title {
  font-size: 16px;
  color: #4b4b4b;
  font-weight: bold;
  margin-bottom: 0;
  width: 284px;
}
/* .btn-custom-light {
  background-color: #ffffff;
  color: #e51616;
  margin-right: 18px;
}
.btn-popup {
  font-size: 14px;
  line-height: 16px;
  display: flex;
  align-items: center;
  height: 32px;
} */
.ant-modal-body {
  padding: 0;
}
.modal-body {
  padding: 14px 18px 19px 25px;
}
.primary-animation {
  animation: button-animation-primary 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  background-color: #3585e5;
  border: 1px solid transparent;
  outline: 2px solid transparent;
}
.light-animation {
  animation: reset-animation-primary 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  background-color: #ffffff;
  border: 1px solid transparent;
  outline: 2px solid transparent;
}
@keyframes reset-animation-primary {
  0% {
  }
  33% {
    outline: 2px solid #e51616;
  }
  66% {
    outline: 2px solid #e51616;
  }
  100% {
  }
}
@keyframes button-animation-primary {
  0% {
  }
  33% {
    outline: 2px solid #89d1fd;
    background-color: #3585e5 !important;
  }
  66% {
    outline: 2px solid #89d1fd;
    background-color: #3585e5 !important;
  }
  100% {
    background-color: #3491ff !important;
  }
}
</style>
<style scoped>
.btn-custom {
  height: 28px;
  display: flex;
  align-items: center;
  line-height: 16px;
  font-size: 14px;
}
/*@import "/css/drawer.css";*/
</style>
<style src="/css/drawer.css" cus-no-cache  rel="stylesheet" type="text/css" scoped> </style>
