<template>
  <div>
    <div class="row">
      <div class="col-md-12">
        <div v-show="isLoading" class="loading-wave" style="height: 500px; margin-bottom: 16px;"></div>
        <div v-show="!isLoading" class="card custom-card">
          <div class="card-header">
            <strong v-text="resources['company']"></strong>
          </div>
          <div class="card-body">
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="name">
                {{ resources['company_type'] }}
                <span class="avatar-status badge-danger"></span>
              </label>
              <div class="col-md-10">
                <common-button :title="companyDropdownTitle" @click="handleToggle" :is-show="showCompanyType">
                  <span>{{ companyDropdownTitle }}</span>
                  <span class="dropdown-icon-custom"><img class="img-transition" src="/images/icon/icon-cta-black.svg"
                      alt=""></span>

                </common-button>
                <common-select-popover :is-visible="showCompanyType" @close="handlecloseCompanyType"
                  class="w-95 common-popover">

                  <common-select-dropdown id="companyType" :searchbox="false" :class="{ show: showCompanyType }"
                    :style="{ position: 'static' }" :items="codes.CompanyType" :checkbox="false" class="dropdown"
                    :click-handler="select" :set-result="() => { }"></common-select-dropdown>

                </common-select-popover>
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="name">
                {{ resources['company_name'] }}
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="name" v-model="company.name" class="form-control" :class="{
                  'form-control-warning':
                    company.name == '' ||
                    company.name == null,
                }" :placeholder="resources['company_name']" required />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="company-code">{{ resources['company_code'] }}<span
                  class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="company-code" v-model="company.companyCode" :maxlength="20" class="form-control"
                  :class="{
                    'form-control-warning':
                      company.companyCode == '' ||
                      company.companyCode == null,
                  }" :placeholder="resources['company_code']" required />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="address">{{ resources['address'] }}
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="address" v-model="company.address" class="form-control" :class="{
                  'form-control-warning':
                    company.address == '' ||
                    company.address == null,
                }" :placeholder="resources['address']" />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="manager">{{ resources['manager'] }}
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="manager" v-model="company.manager" class="form-control" :class="{
                  'form-control-warning':
                    company.manager == '' ||
                    company.manager == null,
                }" :placeholder="resources['manager']" />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="phone">{{ resources['phone_number'] }}
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="phone" v-model="company.phone" class="form-control" :class="{
                  'form-control-warning':
                    company.phone == '' ||
                    company.phone == null,
                }" :placeholder="resources['phone_number']" />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="email">
                {{ resources['email'] }}
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="email" id="email" v-model="company.email" class="form-control" :class="{
                  'form-control-warning':
                    company.email == '' ||
                    company.email == null,
                }" :placeholder="resources['email']" />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="textarea-input">{{ resources['memo'] }}</label>
              <div class="col-md-10">
                <textarea class="form-control" :class="{
                  'form-control-warning':
                    company.memo == '' ||
                    company.memo == null,
                }" id="textarea-input" v-model="company.memo" name="textarea-input" rows="9"
                  :placeholder="resources['memo']"></textarea>
              </div>
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
    resources: {
      type: Object,
      default: () => ({})
    },
    codes: {
      type: Object,
      default: () => ({})
    },
    company: {
      type: Object,
      default: () => ({})
    },
    isLoading: {
      type: Boolean,
      default: () => (true)
    }
  },
  data() {
    return {
      showCompanyType: false,
    }
  },
  computed: {
    companyDropdownTitle() {
      if (this.company?.companyType) {
        return Common.formatter.toCase(this.company?.companyType, 'capitalize')
      }
      return 'Company Type'
    }
  },
  methods: {
    select(item) {
      this.showCompanyType = false;
      this.company.companyType = item.code;
    },
    handlecloseCompanyType() {
      this.showCompanyType = false
    },
    handleToggle(isShow) {
      this.showCompanyType = isShow;
    },

  },
}
</script>

<style>

</style>