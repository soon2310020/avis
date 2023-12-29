<template>
  <div
      style="z-index: 999999"
      id="op-delete-popup"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div style="box-shadow: 0 0 3px #00000029" class="modal-content">
        <div style="padding: 22px 16px 24px 16px" class="modal-body">
          <div style="display: flex;align-items: center;line-height: 100%;margin-bottom: 8.7px;" class="modal-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="12.444" height="11.097" viewBox="0 0 11.444 10.097">
              <path id="Icon_awesome-exclamation-triangle" data-name="Icon awesome-exclamation-triangle" d="M11.315,8.677a.947.947,0,0,1-.826,1.42H.955a.947.947,0,0,1-.826-1.42L4.9.473a.958.958,0,0,1,1.652,0Zm-5.593-1.7a.907.907,0,1,0,.914.907A.911.911,0,0,0,5.722,6.981ZM4.854,3.72,5,6.4a.238.238,0,0,0,.238.224H6.2A.238.238,0,0,0,6.442,6.4L6.59,3.72a.237.237,0,0,0-.238-.25H5.092a.237.237,0,0,0-.238.25Z" transform="translate(0)" fill="#db3b21"/>
            </svg>
            <b style="margin-left: 5px;font-size: 16px;color: #4B4B4B">
              Delete existing property in {{ name }} Configuration?
            </b>
          </div>
          <div class="custom-modal-info2">
            <span>Deleting {{ list.label }} property will also erase any existing data related to it.</span>
            <div style="margin-top: 12px">
              Are you sure you want to delete?
            </div>
          </div>
          <div class="custom-modal-confirm-zone2" style="display: flex; justify-content: flex-end; align-items: center; margin-top: 8px;">
            <div>
<!--              <div @click="onCloseModal">-->
<!--                No. Keep Property-->
<!--              </div>-->
              <a
                  href="javascript:void(0)"
                  id="close-request3"
                  style="margin-right: 8px"
                  class="btn-custom btn-custom-close3"
                  type="primary"
                  @click="onCloseModal()"
              >
                No. Keep Property
              </a>
            </div>
            <div>
<!--              <div @click="emitDelete()" class="custom-modal-confirm-btn">-->
<!--                Yes. Delete Forever-->
<!--              </div>-->
              <a
                  href="javascript:void(0)"
                  id="close-request2"
                  style="margin-right: 8px"
                  class="btn-custom btn-custom-close2"
                  type="primary"
                  @click="emitDelete()"
              >
                Yes. Delete Forever
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
    list: Object,
    name: String
  },
  data() {
    return {
      isChecked: false
    };
  },
  methods: {
    emitDelete () {
      const el = document.getElementById('close-request2')
      el.classList.add('close-animation2')
      setTimeout(() => {
        el.classList.remove('close-animation2')
        this.$emit('delete');
      }, 700)
    },
    showDeletePopup () {
      $("#op-delete-popup").modal("show");
    },
    onCloseModal () {
      const el = document.getElementById('close-request3')
      el.classList.add('close-animation3')
      el.classList.add('borderWhite')
      setTimeout(() => {
        el.classList.remove('close-animation3')
        $("#op-delete-popup").modal("hide");
      }, 700)
      setTimeout(() => {
        el.classList.remove('borderWhite')
      }, 600)
    }
  },
  mounted() {
    this.$nextTick(function() {
      // 모든 화면이 렌더링된 후 실행합니다.
      //this.paging(1);
    });
  }
};
</script>
<style scoped>
.modal-content {
  width: 450px;
  margin: auto;
}
.custom-modal-info2 {
  font-size: 13px!important;
}
</style>