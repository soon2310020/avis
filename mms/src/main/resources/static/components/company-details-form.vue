<template>
  <div
    style="overflow: scroll"
    id="op-company-details-form"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form class="needs-validation">
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <div
              class="head-line"
              style="
                position: absolute;
                background: #52a1ff;
                height: 8px;
                border-radius: unset;
                top: 0;
                left: 0;
                width: 100%;
              "
            ></div>
            <span class="span-title">Complete Data</span>
            <span
              class="close-button"
              style="
                font-size: 25px;
                display: flex;
                align-items: center;
                height: 17px;
                cursor: pointer;
              "
              data-dismiss="modal"
              aria-label="Close"
            >
              <span class="t-icon-close"></span>
            </span>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-12">
                <div class="card custom-card">
                  <div class="card-header">
                    <strong v-text="resources['company']"></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="name"
                        v-text="resources['company_type']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <common-button
                          :title="company_type"
                          @click="handleToggle"
                          :is-show="showCompanyType"
                        >
                          <span>{{ company_type }}</span>
                          <span class="dropdown-icon-custom"
                            ><img
                              class="img-transition"
                              src="/images/icon/icon-cta-black.svg"
                              alt=""
                          /></span>
                        </common-button>
                        <common-select-popover
                          :is-visible="showCompanyType"
                          @close="handlecloseCompanyType"
                          class="w-95 common-popover"
                        >
                          <common-select-dropdown
                            id="companyType"
                            :searchbox="false"
                            :class="{ show: showCompanyType }"
                            :style="{ position: 'static' }"
                            :items="companyTypes"
                            :checkbox="false"
                            class="dropdown"
                            :click-handler="select"
                            :set-result="() => {}"
                          ></common-select-dropdown>
                        </common-select-popover>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="name"
                        v-text="resources['company_name']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="name"
                          v-model="company.name"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.name == '' || company.name == null,
                          }"
                          :placeholder="resources['company_name']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="company-code"
                        v-text="resources['company_code']"
                        ><span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="company-code"
                          v-model="company.companyCode"
                          :maxlength="20"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.companyCode == '' ||
                              company.companyCode == null,
                          }"
                          :placeholder="resources['company_code']"
                          required
                        />
                        <!--:readonly="readonlyCompanyCode"-->
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="address"
                        v-text="resources['address']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="address"
                          v-model="company.address"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.address == '' || company.address == null,
                          }"
                          :placeholder="resources['address']"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="manager"
                        v-text="resources['manager']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="manager"
                          v-model="company.manager"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.manager == '' || company.manager == null,
                          }"
                          :placeholder="resources['manager']"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="phone"
                        v-text="resources['phone_number']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="phone"
                          v-model="company.phone"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.phone == '' || company.phone == null,
                          }"
                          :placeholder="resources['phone_number']"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="email"
                        v-text="resources['email']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="email"
                          id="email"
                          v-model="company.email"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.email == '' || company.email == null,
                          }"
                          :placeholder="resources['email']"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="textarea-input"
                        v-text="resources['memo']"
                      ></label>
                      <div class="col-md-10">
                        <textarea
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              company.memo == '' || company.memo == null,
                          }"
                          id="textarea-input"
                          v-model="company.memo"
                          name="textarea-input"
                          rows="9"
                          :placeholder="resources['memo']"
                        ></textarea>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="d-flex justify-end">
              <base-button
                level="primary"
                type="save"
                @click.prevent="submit"
                >{{ resources["save_changes"] }}</base-button
              >
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    updated: Function,
  },
  data() {
    return {
      company: {
        companyType: "",
        companyCode: "",
        name: "",
        address: "",
        manager: "",
        phone: "",
        email: "",
        memo: "",
        enabled: true,
      },
      isTable: "",
      showCompanyType: false,
      company_type: "Company Type",
      CompanyType: [
        {
          code: "IN_HOUSE",
          title: "In-house",
        },
        {
          code: "TOOL_MAKER",
          title: "Tool maker",
        },
        {
          code: "SUPPLIER",
          title: "Supplier",
        },
      ],
    };
  },
  computed: {
    companyTypes() {
      return headerVm?.systemCodes?.CompanyType || [];
    },
  },
  methods: {
    select(item) {
      this.company_type = item.title;
      this.showCompanyType = false;
      console.log(item);
      this.company.companyType = item.code;
    },
    handleshowCompanyType: function () {
      this.showCompanyType = true;
    },
    handlecloseCompanyType: function () {
      this.showCompanyType = false;
    },
    handleToggle: function (isShow) {
      this.showCompanyType = isShow;
    },
    submit: function () {
      this.update();
    },

    create: function () {
      axios
        .post(`/api/companies/${this.company.id}`, this.company)
        .then(function (response) {
          console.log(response.data);
          if (response.data.success) {
            Common.alertCallback = function () {
              if (typeof Page !== "undefined")
                location.href = Common.PAGE_URL.COMPANY;
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    update: function () {
      this.children = null;
      let self = this;
      axios
        .put(`/api/companies/${this.company.id}`, this.company)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            if (self.isTable == "table") {
              Common.alertCallback = function () {
                self.dimissModal();
              };
              Common.alert("success");
            } else {
              self.dimissModal();
            }
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    onMountedRun() {
      this.$nextTick(function () {
        axios
          .get(`/api/companies/${this.company.id}`)
          .then(function (response) {
            this.company = response.data;
            console.log(response.data);
          });

        setTimeout(function () {
          Common.initRequireBadge();
        }, 100);
      });
    },
    showDetails: function (company, page) {
      this.isTable = page;
      this.company = company;
      this.company_type = this.company.companyTypeText;
      this.onMountedRun();
      $(this.getRootId() + "#op-company-details-form").modal("show");
    },

    dimissModal: function () {
      this.updated();
      $(this.getRootId() + "#op-company-details-form").modal("hide");
    },
  },
  mounted() {},
};
</script>

<style>
.dropdown {
  /*width: 248% !important;*/
  width: 100% !important;
}
.dropdown-wrap {
  top: 0px !important;
  left: 0px !important;
  border-radius: 3px !important;
  padding: 2px !important;
}
.common-popover {
  top: 34px !important;
  left: 17px !important;
  width: calc(100% - 34px);
  min-width: unset !important;
  border-radius: 3px !important;
  padding: 2px !important;
}
.modal-title {
  color: #ffffff;
}
.shadow {
  outline: 0;
  border: 1px solid #5d43f7;
  box-shadow: 0 0 5px 5px rgba(93, 67, 247, 0.3) !important;
}
.form-control-warning {
  outline: 0;
  border: 1px solid #5d43f7 !important;
  box-shadow: 0 0 5px 5px rgba(93, 67, 247, 0.3);
}
.form-control:invalid,
.form-control:valid,
.was-validated {
  border: 1px solid #e4e7ea;
}
</style>
<style scoped>
.form-control:focus {
  border: 1px solid #8ad4ee !important;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgb(32, 168, 216, 0.25) !important;
}
.modal-header {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 26px 11.5px 26px;
}
.modal-header .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
}
.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}
.modal-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 550;
}
.modal-body {
  /* overflow-y: auto; */
  /* max-height: 600px; */
  margin: 15px 5px;
  padding-top: 0px;
}
.close {
  opacity: 1;
}
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background: #d6dade;
  width: 8px;
  padding-right: 8px;
}
.modal-content {
  width: 101% !important;
}
</style>
