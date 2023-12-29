<template>
  <div
    style="z-index: 9999"
    id="op-unmatch-popup"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div style="box-shadow: 0px 3px 3px #00000029" class="modal-content">
        <div class="modal-body">
          <div
            style="display: flex; align-items: center; margin-bottom: 5px"
            class="modal-title"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="11.444"
              height="10.097"
              viewBox="0 0 11.444 10.097"
            >
              <path
                id="Icon_awesome-exclamation-triangle"
                data-name="Icon awesome-exclamation-triangle"
                d="M11.315,8.677a.947.947,0,0,1-.826,1.42H.955a.947.947,0,0,1-.826-1.42L4.9.473a.958.958,0,0,1,1.652,0Zm-5.593-1.7a.907.907,0,1,0,.914.907A.911.911,0,0,0,5.722,6.981ZM4.854,3.72,5,6.4a.238.238,0,0,0,.238.224H6.2A.238.238,0,0,0,6.442,6.4L6.59,3.72a.237.237,0,0,0-.238-.25H5.092a.237.237,0,0,0-.238.25Z"
                transform="translate(0)"
                fill="#db3b21"
              />
            </svg>
            <b
              style="
                margin-left: 5px;
                font-size: 16px;
                color: #4b4b4b;
                line-height: normal;
              "
              >Un-match
              <span
                >{{ list.length }}
                {{ list.length > 1 ? "Machines" : "Machine" }}</span
              >
              from its {{ list.length > 1 ? "toolings" : "tooling" }}?</b
            >
          </div>
          <div class="custom-modal-info">
            <div>
              Un-matching the machine from its tooling will increase the
              tooling's unplanned downtime.
            </div>
            <br />
            <span>Are you sure you want to un-match?</span>
          </div>
          <div class="d-flex w-100 justify-content-end align-items-center">
            <div class="mr-2">
              <button
                type="button"
                class="btn btn-default cancel-button"
                @click="onCloseModal"
              >
                Cancel
              </button>
            </div>
            <div>
              <button
                @click="confirm"
                class="btn-custom save-button animationPrimary"
              >
                <span> Yes. Go Ahead </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "unmatch-popup.vue",
  props: {
    resources: Object,
    list: Array,
    confirmUnmatch: Function,
    name: String,
  },
  data() {
    return {
      isChecked: true,
    };
  },
  methods: {
    confirm() {
      const self = this;
      setTimeout(() => {
        self.confirmUnmatch();
      }, 700);
      this.onCloseModal();
    },
    showUnmatchPopup() {
      $("#op-unmatch-popup").modal("show");
    },
    onCloseModal() {
      $("#op-unmatch-popup").modal("hide");
    },
    animationPrimary() {
      $(".animationPrimary").click(function () {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
  },
  mounted() {
    this.$nextTick(function () {});
  },
};
</script>
<style scoped>
.modal-content {
  width: 368px;
  height: 166px;
  border-radius: 0px !important;
  margin: auto;
}

.custom-modal-info {
  font-size: 13px;
  color: #4b4b4b;
  line-height: normal;
  margin-top: 8px;
}

.modal-body {
  overflow-x: unset !important;
  padding: 18.29px 15.54px;
}

.btn-default.cancel-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: auto 8px;
  background: #ffffff;
  height: 29px;
  border: 1px solid #d6dade;
  border-radius: 3px;
  text-align: center;
  font-size: 14.66px;
  line-height: 0px;
  color: #595959;
}

.btn-default.cancel-button:hover {
  border: 1px solid #4b4b4b;
  color: #4b4b4b;
}

.btn-default.cancel-button:focus {
  border: 2px solid #d6dade;
  box-shadow: none;
  color: #4b4b4b;
}

.btn-custom.save-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: auto 8px;
  height: 29px;
  background: #3491ff 0% 0% no-repeat padding-box;
  border-radius: 3px;
  border: none;
  line-height: 0px;
  color: #ffffff;
  font-size: 14.66px;
  text-align: center;
}

.btn-custom.save-button:hover {
  background: #3585e5 0% 0% no-repeat padding-box;
  border: none;
  line-height: 0px;
  color: #ffffff;
  font-size: 14.66px;
  text-align: center;
}

.btn-custom.save-button:focus {
  background: #3585e5 0% 0% no-repeat padding-box;
  outline: 2px solid #deedff;
  border-radius: 3px;
  line-height: 0px;
  color: #ffffff;
  text-align: center;
}
</style>
