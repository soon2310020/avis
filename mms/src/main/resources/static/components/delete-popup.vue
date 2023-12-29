<template>
  <div
    style="z-index: 9999"
    id="op-delete-popup"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div style="box-shadow: 0 0 3px #00000029" class="modal-content">
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
                font-size: 14px;
                color: #4b4b4b;
                line-height: 100%;
              "
              >Delete
              <span
                >{{ list.length }}
                {{ list.length > 1 ? "Items" : "Item" }}</span
              >
              From {{ name ? name : "Tooling" }}?</b
            >
          </div>
          <div class="custom-modal-info">
            <span
              >Deleted items can still be accessed and restored within the
              ‘Deleted’ tab.</span
            >
          </div>
          <div class="custom-modal-warning card-body-custom">
            <!--            v-model="isShowWarningAgain"-->
            <input
              v-model="isChecked"
              id="checkbox-all"
              class="checkbox"
              type="checkbox"
            />
            <label class="custom-control-label" for="checkbox-all"></label>
            <span style="margin-left: 4px">Don’t show this warning again.</span>
          </div>
          <div class="custom-modal-confirm-zone">
            <div>
              <div @click="onCloseModal">No. Don’t Delete</div>
            </div>
            <div>
              <!--              :class="{'is-save-content': !isShowWarningAgain}"-->
              <a
                style="font-size: 12px"
                @click="callConfirm(isChecked)"
                href="javascript:void(0)"
                class="btn-custom btn-custom-primary animationPrimary"
              >
                <span> Yes, Move to Deleted </span>
              </a>
              <!--              <div @click="confirmDelete(isChecked)" class="custom-modal-confirm-btn">-->
              <!--                Yes, Move to Deleted-->
              <!--                &lt;!&ndash;                {{ isShowWarningAgain ? 'Yes, Move to Deleted' : 'Save' }}&ndash;&gt;-->
              <!--              </div>-->
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    list: Array,
    isShowWarningAgain: Boolean,
    confirmDelete: Function,
    name: String,
  },
  data() {
    return {
      isChecked: false,
      // isShowWarningAgain: false
    };
  },
  methods: {
    callConfirm(isChecked) {
      const self = this;
      setTimeout(() => {
        self.confirmDelete(isChecked);
      }, 700);
    },
    showDeletePopup() {
      $("#op-delete-popup").modal("show");
    },
    onCloseModal() {
      $("#op-delete-popup").modal("hide");
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
    this.$nextTick(function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      //this.paging(1);
    });
  },
};
</script>
<style scoped>
.modal-content {
  width: 353px;
  height: 169px;
  margin: auto;
}
</style>
