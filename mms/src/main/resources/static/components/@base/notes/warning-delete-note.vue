<template>
  <div class="matching-modal" v-if="isShowing">
    <div class="modal-wrapper duplicated-modal" style="color: #4B4B4B">
      <div class="modal-container" v-click-outside="closeWarning">
        <div class="modal-body">
          <div class="modal-body-content">
            <div class="warning-title">
              <img src="/images/icon/warning-2.svg" width="16" height="16" alt="warning" />
              <p>{{resources['delete_note_?']}}</p>
            </div>
            <div class="duplicated-title" style="font-size: 19px">
              <p>{{resources['delete_notes_can_be_accessed_and_restored_from_the_delete_tab']}}</p>
              <div class="d-flex align-items-center my-2">
                <input v-model="isDontShowWarningAgain" style="width: 10px;height: 10px;" type="checkbox" />
                <span class="check-box-title" style="margin-left: 7px" v-text="resources['dont_show_warning_again']"></span>
              </div>
            </div>
            <div class="duplicated-action">
              <button class="btn btn-back" @click="closeWarning" v-text="resources['no_delete']"></button>
              <button class="btn btn-delete" style="margin-left: 5px" @click="deleteNote" v-text="resources['yes_delete']"></button>
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
    isShowing: Boolean,
    isDontShowWarningAgain: Boolean
  },
  data() {
    return {
    }
  },
  computed: {
  },
  methods: {
    closeWarning(){
      this.$emit('set-is-showing', false)
    },
    deleteNote(){
      this.$emit('set-is-showing', false)

      this.$emit('delete-note');
      if(this.isDontShowWarningAgain){
        this.updateIsShowWarningAgain()
      }
    },
    updateIsShowWarningAgain() {
      axios.post('/api/on-boarding/update', {'feature': 'SHOW_WARNING_DELETE_NOTE', 'seen': true}).then(response => {})
    }
  },
  mounted() {
  }
};
</script>

<style scoped>

.matching-modal {
  position: fixed;
  background: rgba(0, 0, 0, 0.1);
  top: 0;
  width: 100%;
  height: 113%;
  /* padding-top: 50px; */
  z-index: 12;
  left: 0px;
  transform: translate(0px, -89px);
}

.matching-modal .modal-wrapper {
  width: 300px;
  margin: 33vh auto;
  background: #FFF;
  border-radius: 5px;
}
.matching-modal .modal-container .modal-header {
  padding: 5px 15px 10px;
}

.matching-modal .modal-container .modal-header .modal-title {
  font-size: 20px;
  font-weight: bold;
}


.matching-modal .modal-container .modal-header button {
  color: #777;
  cursot: pointer;
}

.matching-modal .importing-modal .modal-container .modal-body {
  padding: 15px 5px;
}


.matching-modal .importing-modal .modal-container .modal-body .modal-body-content {
  text-align: center;
}


.matching-modal .importing-modal .modal-container .modal-body .modal-body-content .loading-title {
  color: #463DA5;

}

.matching-modal .importing-modal .modal-container .modal-body .modal-body-content .loading-progress {
  padding: 15px 15px;
}


.matching-modal .importing-modal .modal-container .modal-body .modal-body-content .loading-progress .progress-bar-wrapper {
  border: 1px solid #463DA5;
  height: 23px;
  border-radius: 15px;
  padding: 2px;
}


.matching-modal .importing-modal .modal-container .modal-body .modal-body-content .loading-progress .progress-bar-wrapper .progress-bar {
  background: #263162;
  height: 100%;
  border-radius: 15px;
  position: relative;
}


.matching-modal .importing-modal .modal-container .modal-body .modal-body-content .loading-progress .progress-bar-wrapper .progress-bar .direction-icon {
  position: absolute;
  height: 100%;
  width: 20px;
  border-radius: 15px;
  background: #615EA8;
  right: 0;

}

.matching-modal .notification-modal {
  width: 400px;
}
.matching-modal .duplicated-modal {
  width: 400px;
}
.matching-modal .warning-delete-modal {
  width: 350px;
}
.btn-delete {
  background-color: #E34537;
  color: #FFFFFF;
  border-radius: 3px;
}
.btn-delete :hover, .btn-delete :active {
  background-color: #C92617;
}
.btn-back {
  background-color: #FFFFFF;
  border: 1px solid #D6DADE;
  color: #595959;
  padding: 6px 8px;
}
.btn-back:focus {
  /*
  background-color: #F4F4F4;
  border: 2px solid #D6DADE;
  */
  box-shadow:unset;
}
.btn-back:active {
  background-color: #F4F4F4;
  border: 1px solid #D6DADE !important;
  box-shadow: 0 0 0 1px #D6DADE;
}
.btn-back:hover {
  background-color: #F4F4F4;
  border: 1px solid #595959;
}
.btn{
  border-radius: 3px;
}
.duplicated-action {
  text-align: end;
}
.check-box-title{
  color: #757575;
  font-size: 11px;
  line-height: 13px;
}
.warning-title{
  display: flex;
  margin-bottom: 11px;
}
.warning-title p {
  margin-left: 7px;
  margin-bottom: 0px;
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  color: #4B4B4B;
}
.duplicated-title p {
  font-weight: 400;
  font-size: 15px;
  line-height: 17px;
  color: #4B4B4B;
}
</style>