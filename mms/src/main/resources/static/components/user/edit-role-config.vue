<template>
  <div>
    <div
        id="edit-role-config"
        class="modal fade"
        tabindex="-1"
        role="dialog"
        aria-labelledby="title-part-chart"
        aria-hidden="true"
    >
      <div class="modal-dialog h-100 modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-title">
            <div class="head-line"></div>
            <div>
              <span class="span-title">Edit Role</span>
            </div>
            <span class="close-button" data-dismiss="modal" aria-label="Close" @click="close">
            <span class="t-icon-close"></span>
          </span>
          </div>

          <div class="modal-body mx-1 mx-lg-5">
            <form class="needs-validation" @submit.prevent="submit">
              <div class="row custom-row">
                <div class="col-md-12 custom">
                  <div class="card-body custom-body">
                    <div class="form-group row custom-row">
                      <label class="col-form-label custom-style col-md-2" for="role_name">
                        <span>Role Name</span>
                        <span class="avatar-status badge-danger"></span>
                        <span class="badge-require"></span>
                      </label>
                      <div class="col-md-9 custom-input-style">
                        <input placeholder="Operator" type="text" id="role_name" v-model="role.name" class="form-control  input-name-edit"
                               :class="validateRole ? 'validation' : 'form-control-custom1'"
                               :readonly="true">
                        <!--                    <div v-show="validateRole" class="validate">-->
                        <!--                      Role should only be alphabetic without space.-->
                        <!--                    </div>-->
                      </div>
                    </div>

                    <div class="form-group row custom-row">
                      <label class="col-form-label custom-style col-md-2" for="description">
                        <span>Description</span>
                      </label>
                      <div class="col-md-9 custom-input-style">
                        <input placeholder="Description"  @keyup="validate" type="text" id="description" v-model="role.description" class="form-control form-control-custom custom-input input-description-edit"
                               :readonly="false">
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="row flex-row-reverse btn-submission">
                <button type="submit" :disabled="disable"  class="btn btn-primary" :class="disable ? 'disable-button-edit' : 'button-edit' " >Save</button>
                <!--            <button type="button" @click="visible = false;" class="btn btn-default button-cancel">Cancel</button>-->
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  components: {
  },
  data() {
    return {
      resources:{},
      visible: false,
      disable:true,
      validateRole:false,
      visibleSuccess: false,
      id:'',
      'role':{
        description:'',
      },
    };
  },
  methods: {
    close(){
      this.disable=true;
      $("#edit-role-config").modal("hide");
    },
    validate(){
      this.disable=false;
    },
    showEditRole(data) {
      $("#edit-role-config").modal("show");
      this.id=data[0].id;
      this.role.name=data[0].name;
      this.role.description=data[0].description;
    },
    getResources() {
      return  axios.get("/api/resources/getAllMessagesOfCurrentUser").then(function(response) {
        return response.data;
      })
          .catch(function(error) {
            Common.alert(error.response.data);
          });
    },
    async submit() {
        this.create();
    },
    async create(){
      await axios.put('/api/common/rol-stp/'+this.id,this.role).then((response) => {
        this.disable=true;
        $("#edit-role-config").modal("hide");
        this.$emit('parentmethod');
        if (response.data.success) {
          this.disable=true;
          Common.alert("Successfully Updated!");
        }
      })
          .catch((error) => {
            Common.alert("Either Role is Duplicate or Internal Error");
          });
    }
  }
};
</script>
<style>
.custom-input-style{
  font: normal normal normal 14px Helvetica Neue;
}
@media (min-width: 1100px) {
  .modal-lg {
    max-width: 1032px !important;
  }
}
.modal-dialog {
  position: relative;
  display: flex;
  pointer-events: none;
  align-content: center;
  justify-content: center;
  align-items: center;
  margin: auto !important;
}
.modal-content {
  width: 1032px;
  pointer-events: auto;
  background-color: rgb(255, 255, 255);
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  outline: 0px;
}

.modal-title {
  background: #f5f8ff;
  position: relative;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 20px;
  border-radius: 6px 6px 0 0;
}

.modal-body {
  background: white;
  padding: 20px;
  border-radius: 0px 0px 6px 6px;
}

.modal-title .span-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 25px;
  margin-bottom: 12px;
  font-size: 16px;
  font-size: 16px;
  color: #4B4B4B !important;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
  margin-right:14px;
}

.form-control-custom {
  font:normal normal normal 14px/16px Helvetica Neue;
  color: #5c6873;
  background-color: #fff;
  border: 0.5px solid #909090;
  padding: 6px 5px;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control-custom::-ms-expand {
  background-color: transparent;
  padding: 6px 5px;
  border: 0;
}

.form-control-custom:hover {
  background-color: #fff;
  border: 0.5px solid #4B4B4B !important;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.form-control-custom:focus {
  background-color: #fff;
  border: 1.5px solid #98D1FD !important;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.row.custom-row {
  margin-left: 0px;
  margin-right: 0px;
}

.form-control-custom::-webkit-input-placeholder {
  color: #C9C9C9;
  opacity: 1;
}

.form-control-custom::-moz-placeholder {
  color: #C9C9C9;
  opacity: 1;
}

.form-control-custom:-ms-input-placeholder {
  color: #C9C9C9;
  opacity: 1;
}

.form-control-custom::-ms-input-placeholder {
  color: #C9C9C9;
  opacity: 1;
}

.form-control-custom::placeholder {
  color: #C9C9C9;
  opacity: 1;
}

.form-group.row.custom-row{
 margin-bottom: 13px;
}
.button-edit{
  width: 54px;
  height: 29px;
  border:  1px solid #47B576;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #47B576 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}
.button-edit:hover{
  width: 54px;
  height: 29px;
  border:  1px solid #3EA662;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #3EA662 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

.btn-primary:focus,
.btn-primary.focus {
  box-shadow: none;
  background: #3EA662 0% 0% no-repeat padding-box !important;
}

.btn-primary.button-edit:focus{
  outline: none !important;
  box-shadow: none !important;
  width: 54px;
  height: 29px;
  border:  2px solid #C3F2D7;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #3EA662 0% 0% no-repeat padding-box !important;
  border-radius: 3px;
  opacity: 1;
}
.disable-button-edit{
  width: 54px;
  height: 29px;
  border:  1px solid ##C4C4C4;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #C4C4C40% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}
.disable-button-edit:hover{
  width: 54px;
  height: 29px;
  border:  1px solid #C4C4C4;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #C4C4C4 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

.btn-primary.disable-button-edit:disabled,
.btn-primary.disable-button-edit.focus {
  box-shadow: none;
  background: #C4C4C4 0% 0% no-repeat padding-box !important;
}

.btn-primary.disable-button-edit:disabled{
  outline: none !important;
  box-shadow: none !important;
  cursor:not-allowed;
  width: 54px;
  height: 29px;
  border:  2px solid #C4C4C4;
      display: flex;
      align-items: center;
      justify-content: center;
  margin-left: 3px;
  background: #C4C4C4 0% 0% no-repeat padding-box !important;
  border-radius: 3px;
  opacity: 1;
}


label > .badge-require {
    margin-top: 8px;
}
.col-form-label{
  width : 150px;
  color:#2F2F2F;
  font-size: 16px;
  font: normal normal normal 16px/18px Helvetica Neue;
  letter-spacing: 0px;
}
::placeholder{
  color : #C9C9C9;
  letter-spacing: 0px;
  font-size: 16px;
  font: normal normal normal 16px/18px Helvetica
}
.btn-submission{
       /* margin-top: 350px; */
    display: flex;
    height: 320px;
    right: 70px;
    /* position: absolute; */
    align-content: flex-end;
    align-items: flex-end;
    flex-direction: column;
}
.btn-search-location{
  width: 126px;
  height: 32px;
  border: 1px solid #48B46E;
  color: #48B46E;
  border-radius: 4px;
  opacity: 1;
}
.input-location-edit{
  width: 499px;
  height: 36px;
  /* UI Properties */
  background: #E0E3E6 0% 0% no-repeat padding-box;
  border: 1px solid #D5D7D8;
  border-radius: 4px;
  opacity: 1;
}
.modal-header h2 {
  margin-top: 0;
  margin-bottom: 0;
  color: #4e4e4e;
}
.custom-input{
 border: 0.5px solid #909090 !important;
border-radius: 2px !important;

}
@keyframes primary-animation-invite {
  0%   {
    color: #006cff !important;
    text-decoration: none !important;
  }
  33%   {
    color: #006cff !important;
    text-decoration: none !important;
  }
  66%   {
    color: #006cff !important;
    text-decoration: none !important;
  }
  100%   {

  }
}
.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
.custom-select-invite.ant-select-open .ant-select-selection{
  border: 2px solid #98D1FD!important;
}
</style>


