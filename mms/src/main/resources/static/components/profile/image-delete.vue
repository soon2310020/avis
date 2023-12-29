<template>
  <div
      style="z-index: 99999;background: rgba(0, 0, 0, 0.65);"
      id="op-image-delete"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div style="box-shadow: 0 0 3px #00000029" class="modal-content">
        <div style="padding: 1rem!important;" class="modal-body">
          <div style="display: flex; align-items: center;margin-bottom: 5px;">
            <svg xmlns="http://www.w3.org/2000/svg" width="11.444" height="10.097" viewBox="0 0 11.444 10.097">
              <path id="Icon_awesome-exclamation-triangle" data-name="Icon awesome-exclamation-triangle" d="M11.315,8.677a.947.947,0,0,1-.826,1.42H.955a.947.947,0,0,1-.826-1.42L4.9.473a.958.958,0,0,1,1.652,0Zm-5.593-1.7a.907.907,0,1,0,.914.907A.911.911,0,0,0,5.722,6.981ZM4.854,3.72,5,6.4a.238.238,0,0,0,.238.224H6.2A.238.238,0,0,0,6.442,6.4L6.59,3.72a.237.237,0,0,0-.238-.25H5.092a.237.237,0,0,0-.238.25Z" transform="translate(0)" fill="#db3b21"/>
            </svg>
            <span class="head-delete-img">Remove Profile Image?</span>
          </div>
          <div class="custom-modal-info">
            <div>Removed images can not be restored. <div>Are you sure you want to remove the image?</div></div>
          </div>
          <div class="custom-modal-confirm-zone" style="justify-content: right; margin-top: 35px">
            <div>
              <button @click="onCloseModal" class="btn cancel-cta-white btn-no-delete" type="button">
                No. Don’t Remove
              </button>
            </div>
            <div style="width: auto;">
              <a @click="callConfirm()" href="javascript:void(0)" class="btn-custom btn-custom-primary animationPrimary">
                <span>
                  Yes. Remove
                </span>
              </a>
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
    isRemoveImage: Boolean,
    isShowWarningAgain: Boolean,
    confirmDelete: Function,
    name: String
  },
  data() {
    return {
    };
  },
  methods: {
    callConfirm () {
      const self = this
      setTimeout(() => {
        self.confirmDelete()
      }, 700)
    },
    showDeletePopup () {
      $(this.getRootId() + "#op-image-delete").modal("show");
    },
    onCloseModal () {
      $(this.getRootId() + "#op-image-delete").modal("hide");
    },
    animationPrimary () {
      $('.animationPrimary').click(function () {
        $(this).addClass('primary-animation');
        $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function(event) {
          $(this).removeClass('primary-animation')
        });
      });
    }
  },
  mounted() {
    this.animationPrimary();
  }
};
</script>
<style scoped>
.btn-no-delete{
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 6px 8px;
  gap: 10px;
  width: 137px;
  height: 29px;
}
.custom-modal-info{
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
}
.head-delete-img{
  margin-left: 5px;
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4B4B4B;
}
.modal-content {
  width: 353px;
  height: 165px;
  margin: auto;
}
.btn-custom{
  /*background: #E34537 !important;*/
  /*border: 1px solid #E34537 !important;*/
  /*box-shadow: none !important;*/

  /*display: flex;*/
  /*flex-direction: row;*/
  /*justify-content: center;*/
  /*align-items: center;*/
  /*padding: 6px 8px;*/
  /*gap: 10px;*/
  /*width: 102px;*/
  /*height: 29px;*/

  /*font-family: 'Helvetica Neue';*/
  /*font-style: normal;*/
  /*font-weight: 400;*/
  /*font-size: 14.66px;*/
  /*line-height: 17px;*/
  /*text-align: right;*/
  /*color: #FFFFFF;*/
  height: 29px;
  font-family: "Helvetica Neue";
  font-size: 14.66px;
  background: rgb(227, 69, 55) !important;
  border: 1px solid rgb(227, 69, 55) !important;
  box-shadow: none !important;
}
</style>