<template>
  <div>
    <a-modal
      v-model="visible"
      class="custom-modal"
      :body-style="{ padding: '0' }"
      width="1032px"
      :closable="false"
      :footer="null"
      centered
    >
      <div class="custom-modal-title custom-title-edit-role">
        <div class="head-line"></div>
        <span class="modal-title title-edit-role custom-header">{{
          newForm ? "Create Code" : "Edit Code"
        }}</span>
        <div
          class="t-close-button close-button close-edit custom-close"
          @click="visible = false"
          aria-hidden="true"
        >
          <!-- <span class="t-icon-close"></span> -->
          <img src="/images/icon/black-close-12.svg" alt="close icon" />
        </div>
      </div>
      <div class="">
        <div class="align-items-center d-flex heading-custom-row">
          <label class="heading-label" for="role_name">
            <span>Code Type</span>
          </label>
          <div class="role-name">{{ codeName }}</div>
        </div>
        <hr class="mt-0 mb-0" />
        <form class="needs-validation" @submit.prevent="submit">
          <div class="custom-form">
            <div class="col-md-12 custom">
              <div class="card-body custom-body">
                <div class="form-group d-flex custom-row">
                  <label class="col-form-label custom-style" for="role_name">
                    <span>Company</span>
                    <span class="avatar-status badge-danger"></span>
                    <span class="badge-require"></span>
                  </label>
                  <div class="w-100">
                    <div class="w-75 d-flex">
                      <input
                        class="w-100"
                        type="hidden"
                        v-model="role.name"
                        required
                      />
                      <input
                        type="text"
                        v-model="role.companyCode"
                        readonly="readonly"
                        class="w-100 form-control readonly-input"
                        required
                      />
                      <button
                        type="button"
                        @click="showCompany"
                        class="btn btn-outline-success ml-4"
                        data-toggle="modal"
                        data-target="#op-company-search"
                      >
                        Company Search
                      </button>
                    </div>
                  </div>
                </div>

                <div class="form-group d-flex custom-row">
                  <label class="col-form-label custom-style" for="role_name">
                    <span>Code</span>
                    <span class="avatar-status badge-danger"></span>
                    <span class="badge-require"></span>
                  </label>
                  <div class="w-100">
                    <input
                      placeholder="Enter Code ID"
                      @keyup="validate"
                      type="text"
                      id="role_name"
                      v-model="role.code"
                      class="form-control w-100 input-name-edit"
                      :class="
                        validateRole ? 'validation' : 'form-control-custom'
                      "
                      :readonly="false"
                      required
                    />
                    <div v-show="validateRole" class="validate">
                      Role should only be alphabetic without space.
                    </div>
                  </div>
                </div>

                <div class="form-group d-flex custom-row">
                  <label class="col-form-label custom-style" for="role_name">
                    <span>Code Title</span>
                    <span class="avatar-status badge-danger"></span>
                    <span class="badge-require"></span>
                  </label>
                  <div class="w-100">
                    <input
                      placeholder="Enter Code Title"
                      @keyup="validate"
                      type="text"
                      id="role_name"
                      v-model="role.title"
                      class="form-control w-100 input-name-edit"
                      :class="
                        validateRole ? 'validation' : 'form-control-custom'
                      "
                      :readonly="false"
                      required
                    />
                    <div v-show="validateRole" class="validate">
                      Role should only be alphabetic without space.
                    </div>
                  </div>
                </div>

                <div class="form-group d-flex custom-row">
                  <label class="col-form-label custom-style" for="description"
                    >Description</label
                  >
                  <div class="w-100">
                    <div class="custom-text-area">
                      <textarea
                        name="w3review"
                        placeholder="Enter Description"
                        type="text"
                        id="description"
                        v-model="role.description"
                        rows="4"
                        cols="50"
                        class="w-75 form-control w-100 form-control-custom custom-input"
                        :readonly="false"
                      ></textarea>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex-md-row-reverse btn-submission">
            <button
              type="submit"
              :disabled="validateRole"
              class="btn btn-primary"
              :class="validateRole ? 'disable-button-edit' : 'button-edit'"
            >
              Save
            </button>
            <button
              type="button"
              @click="visible = false"
              class="btn btn-default button-cancel"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </a-modal>
    <company-search
      :resources="resources"
      @setcompany="callbackCompany"
      :parent-name="compName"
    ></company-search>
  </div>
</template>

<script>
module.exports = {
  name: "edit-codelist",
  components: {
    "company-search": httpVueLoader("../../components/company-search.vue"),
  },
  props: {
    resources: Object,
    updated: Function,
  },
  data() {
    return {
      codeName: "Machine Downtime Reason Level 2",
      compName: "modal",
      visible: false,
      validateRole: false,
      visibleSuccess: false,
      newForm: false,
      id: "",
      role: {
        id: "",
        companyCode: "",
        companyId: "",
        codeType: "MACHINE_DOWNTIME_REASON_GROUP2",
        code: "",
        title: "",
        group1Title: "Select Main Level",
        group2Title: "Select Sub Level",
        group1Code: "",
        group2Code: "",
        description: "",
      },
    };
  },
  methods: {
    callbackCompany: function (cbObject) {
      let company = cbObject.company;
      console.log("company: ", company);
      if (this.role.companyCode == undefined) {
        this.role.companyCode = "";
      }
      this.role.companyCode = company.companyCode;
      this.role.companyId = company.id;
      console.log("hi-------", this.role.companyCode);
    },

    showCompany() {
      var child = Common.vue.getChild(this.$children, "company-search");
      if (child != null) {
        child.check = true;
      }
    },
    validate() {
      if (this.role.name == "") {
        this.validateRole = false;
      } else {
        const reg = /^[A-Za-z]+$/;
        if (reg.test(this.role.name)) {
          this.validateRole = false;
        } else {
          this.validateRole = true;
        }
      }
    },
    showEditRole(data) {
      this.newForm = false;
      this.visible = true;
      this.role.id = data[0].id;
      this.role.companyCode = data[0].companyCode;
      this.role.code = data[0].code;
      this.role.description = data[0].description;
      this.role.title = data[0].title;
      this.role.companyId = data[0].companyId;
      this.role.group1Code = data[0].group1Code;
      this.role.group2Code = data[0].group2Code;
      if (data[0].group1Code === "") {
        this.role.group1Title = "Select Main Level";
      } else {
        this.role.group1Title = data[0].group1Code;
      }
      if (data[0].group2Code === "") {
        this.role.group2Title = "Select Sub Level";
      } else {
        this.role.group2Title = data[0].group2Code;
      }
    },
    showFormNewRole() {
      this.newForm = true;
      this.visible = true;
      this.role.id = "";
      this.role.companyCode = "";
      this.role.code = "";
      this.role.description = "";
      this.role.title = "";
      this.role.companyId = "";
      this.role.group1Title = "Select Main Level";
      this.role.group2Title = "Select Sub Level";
    },
    async submit() {
      if (!this.validateRole) {
        if (this.newForm) {
          this.create();
        } else {
          this.edit();
        }
      } else {
        Common.alert("Please enter correct input");
      }
    },
    dimissModal: function () {
      this.updated();
    },
    async create() {
      await axios
        .post("/api/common/cod-stp/one", this.role)
        .then((response) => {
          this.visible = false;
          if (response.status === 200) {
            Common.alert("Successfully Created!");
          }
          this.$emit("parentmethod");
        })
        .catch((error) => {
          Common.alert("Either Role is Duplicate or Internal Error");
        });
    },

    async edit() {
      await axios
        .put("/api/common/cod-stp/" + this.role.id, this.role)
        .then((response) => {
          this.visible = false;

          if (response.status === 200) {
            Common.alert("Successfully Updated!");
          }
          this.$emit("parentmethod");
        })
        .catch((error) => {
          Common.alert("Either Role is Duplicate or Internal Error");
        });
    },
  },
};
</script>
<style>
.custom-popover {
  width: 164px !important;
  min-width: 0px !important;
}
.rotate {
  transform: rotate(180deg);
}

.dropdown {
  width: auto !important;
}

.custom-dropdown.intialSelect {
  color: #c9c9c9;
}

.custom-dropdown.fontSelect {
  color: #4b4b4b;
}
.custom-dropdown {
  line-height: normal;
  border: 0.5px solid #909090;
  border-radius: 1.7px;

  background-color: #fff;
  border: 0.5px solid #909090;
  font-size: 15px;
  border-radius: 3px;
  padding: 6px 5px;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.custom-dropdown:hover {
  background-color: #fff;

  border: 0.5px solid #4b4b4b !important;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.custom-dropdown:focus {
  background-color: #fff;
  border: 0.5px solid #909090;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.custom-text-area {
  width: 636px;
}

.form-control-custom {
  color: #5c6873;
  background-color: #fff;
  border: 0.5px solid #909090;
  font-size: 15px;
  border-radius: 3px;
  padding: 6px 5px;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control-custom::-ms-expand {
  background-color: transparent;
  padding: 6px 5px;
  border: 0;
}

.form-control.readonly-input:focus {
  box-shadow: none;
  border-color: #e4e7ea;
}

.form-control.readonly-input {
  font-size: 15px;
  border-radius: 3px;
  padding: 6px 5px;
}

.custom-dropdown-button {
  border-radius: 2px !important;
  color: #4b4b4b !important;
}

.form-control-custom:hover {
  background-color: #fff;
  border: 0.5px solid #4b4b4b !important;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.form-control-custom:focus {
  background-color: #fff;
  border: 1.5px solid #98d1fd !important;
  padding: 6px 5px;
  outline: 0;
  box-shadow: none !important;
}

.row.custom-row {
  margin-left: 0px;
}

.form-control-custom::-webkit-input-placeholder {
  color: #c9c9c9;
  font-size: 15px;
  opacity: 1;
}

.form-control-custom::-moz-placeholder {
  color: #c9c9c9;
  font-size: 15px;
  opacity: 1;
}

.form-control-custom:-ms-input-placeholder {
  color: #c9c9c9;
  font-size: 15px;
  opacity: 1;
}

.form-control-custom::-ms-input-placeholder {
  color: #c9c9c9;
  opacity: 1;
  font-size: 15px;
}

.form-control-custom::placeholder {
  color: #c9c9c9;
  font-size: 15px;
  opacity: 1;
}

.ant-modal-body {
  padding: 0px !important;
}

.col-md-12.custom {
  padding-left: 0px;
}

.form-group.row.custom-row {
  margin-bottom: 13px;
}

.validate {
  color: red;
}

.close-edit {
  left: 668px;
  width: 12px;
}
.button-edit {
  width: 54px;
  height: 29px;
  border: 1px solid #47b576;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #47b576 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}
.button-edit:hover {
  width: 54px;
  height: 29px;
  border: 1px solid #3ea662;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #3ea662 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

.btn-primary:focus,
.btn-primary.focus {
  box-shadow: none;
  background: #3ea662 0% 0% no-repeat padding-box !important;
}

.btn-primary.button-edit:focus {
  outline: none !important;
  box-shadow: none !important;
  width: 54px;
  height: 29px;
  border: 2px solid #c3f2d7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #3ea662 0% 0% no-repeat padding-box !important;
  border-radius: 3px;
  opacity: 1;
}

.heading-label {
  text-align: left;
  margin-bottom: 3px;
  font-size: 15px;
  font: normal normal bold 16px/19px Helvetica Neue;
  letter-spacing: 0px;
  color: #595959;
  opacity: 1;
}

.role-name {
  width: 260px;
  height: 24px;
  background: #fbfcfd;
  text-align: center;
  color: #595959;
  padding: 3px auto;
  border-radius: 4px;
  margin-left: 5px;
  font-size: 15px;
}

.disable-button-edit {
  width: 54px;
  height: 29px;
  border: 1px solid #c4c4c4;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #c4c4c4 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}
.disable-button-edit:hover {
  width: 54px;
  height: 29px;
  border: 1px solid #c4c4c4;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #c4c4c4 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

textarea {
  resize: none;
}

.btn-primary.disable-button-edit:disabled,
.btn-primary.disable-button-edit.focus {
  box-shadow: none;
  background: #c4c4c4 0% 0% no-repeat padding-box !important;
}

.btn-primary.disable-button-edit:disabled {
  outline: none !important;
  box-shadow: none !important;
  cursor: not-allowed;
  width: 54px;
  height: 29px;
  border: 2px solid #c4c4c4;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #c4c4c4 0% 0% no-repeat padding-box !important;
  border-radius: 3px;
  opacity: 1;
}

.custom-form {
  padding: 35px 82px 105px 52px;
}

.heading-custom-row {
  padding: 24px 82px 33px 52px;
}

.button-cancel {
  width: 66px;
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #595959;
  /* UI Properties */
  background: #ffffff 0% 0% no-repeat padding-box;
  border: 1px solid #d6dade;
  border-radius: 3px;
  opacity: 1;
}
.button-cancel:hover {
  width: 66px;
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
  /* UI Properties */
  background: #f4f4f4 0% 0% no-repeat padding-box;
  border: 1px solid #595959;
  border-radius: 3px;
  opacity: 1;
}

.button-cancel:focus {
  width: 66px;
  outline: none !important;
  box-shadow: none;
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
  /* UI Properties */
  background: #f4f4f4 0% 0% no-repeat padding-box;
  border: 2px solid #d6dade;
  border-radius: 3px;
  opacity: 1;
}

label > .badge-require {
  margin-top: 8px;
}

.modal-title.title-edit-role.custom-header {
  margin-left: 25.5px;
}

.t-close-button.close-button.close-edit.custom-close {
  width: auto;
  margin-right: 14.34px;
}

.col-form-label {
  width: 150px;
  color: #2f2f2f;
  font-size: 16px;
  font: normal normal normal 16px/18px Helvetica Neue;
  letter-spacing: 0px;
}
::placeholder {
  color: #c9c9c9;
  letter-spacing: 0px;
  font-size: 16px;
  font: normal normal normal 16px/18px Helvetica;
}

.validation {
  border: 0.5px solid red;
  box-shadow: none !important;
}

.validation:focus {
  border: 0.5px solid red;
  box-shadow: none !important;
}

.card-body.custom-body {
  padding: 0px !important;
  overflow-x: unset !important;
}

.ant-modal-wrap {
  z-index: 1000 !important;
}

.ant-modal {
  padding: 0 0 20px;
}

.custom-modal {
  top: 0px;
  left: 0px;
  /* UI Properties */
  background: #ffffff 0% 0% no-repeat padding-box;
  box-shadow: 0px 3px 6px #00000029;
  border-radius: 5px;
  opacity: 1;
}
.ant-modal-content {
  box-shadow: none !important;
}
.custom-title-edit-role {
  width: 100%;
  height: 48px;
  /* UI Properties */
  background: #f5f8ff 0% 0% no-repeat padding-box;
  border-radius: 6px 6px 0px 0px;
  opacity: 1;
}
.custom-style {
  font: normal normal normal 16px/18px Helvetica;
}

.custom-modal-title {
  padding: 15px 0px 0px !important;
}

.title-edit-role {
  /* Layout Properties */

  height: 19px;
  /* UI Properties */
  text-align: left;
  font: normal normal bold 16px/19px Helvetica;
  letter-spacing: 0px;
  color: #4b4b4b;
  opacity: 1;
}

.head-line {
  margin-top: -3px;
  left: 0;
}

.input-name-edit {
  width: 638px;
  height: 30px;
  border-radius: 2px;
  opacity: 1;
}
.input-id-edit {
  width: 365px;
  height: 30px;
  /* UI Properties */
  border: 0.5px solid #4b4b4b;
  border-radius: 2px;
  opacity: 1;
}
.input-description-edit {
  height: 30px;
  /* UI Properties */
  opacity: 1;
}
.btn-submission {
  display: flex;
  margin-right: 52px;
  right: 70px;
  align-content: flex-end;
  align-items: flex-end;
  flex-direction: column;
}
.btn-search-location {
  width: 126px;
  height: 32px;
  border: 1px solid #48b46e;
  color: #48b46e;
  border-radius: 4px;
  opacity: 1;
}
.input-location-edit {
  width: 499px;
  height: 36px;
  /* UI Properties */
  background: #e0e3e6 0% 0% no-repeat padding-box;
  border: 1px solid #d5d7d8;
  border-radius: 4px;
  opacity: 1;
}

.modal-header h2 {
  margin-top: 0;
  margin-bottom: 0;
  color: #4e4e4e;
}

.custom-input {
  border: 0.5px solid #909090 !important;
  border-radius: 2px !important;
}

a:focus {
  text-decoration: unset;
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

.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}

.custom-select-invite.ant-select-open .ant-select-selection {
  border: 2px solid #98d1fd !important;
}
</style>
