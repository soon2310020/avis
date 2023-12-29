<template>
  <div
    id="op-company-details"
    class="modal fade"
    role="dialog"
    aria-labelledby="title-companies-detail"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-title">
          <div
            class="head-line"
            style="
              position: absolute;
              background: #52a1ff;
              height: 8px;
              border-radius: 6px 6px 0 0;
              top: 0;
              left: 0;
              width: 100%;
            "
          ></div>
          <div>
            <!-- <span class="span-title">{{ resources["machine_details"] }}</span> -->
            <span class="span-title">Company Details</span>
          </div>
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
            @click="dimissModal"
          >
            <span class="t-icon-close"></span>
          </span>
        </div>
        <div style="padding: 20px 56px">
          <div>
            <div class="d-flex justify-space-between">
              <div style="margin-right: 19px">
                <base-images-slider
                  :images="company.attachment"
                  :key="viewImageKey"
                  :clear-key="clearKey"
                  :handle-submit-file="handleSubmitImage"
                ></base-images-slider>
              </div>
              <div style="width: 520px">
                <div style="height: 600px; padding: 0 10px; overflow: auto">
                  <company-details-data
                    :company="company"
                    :is-doash-board="isDoashBoard"
                    :resources="resources"
                  ></company-details-data>
                </div>
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
  components: {
    "company-details-data": httpVueLoader(
      "/components/company/company-details-data-master-filter.vue"
    ),
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  props: {
    resources: Object,
  },
  data() {
    return {
      isDoashBoard: false,
      company: {
        name: "",
      },
      viewImageKey: 0,
      clearKey: 0,
    };
  },
  methods: {
    async handleSubmitImage(file) {
      const refId = this.company.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.company.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "COMPANY_PICTURE");
      formData.append("refId", `${refId}`);
      return formData;
    },
    async getCompanyAttachment() {
      try {
        const type = {
          storageType: "COMPANY_PICTURE",
          refId: this.company.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.company.attachment = res.data;
        this.viewImageKey++;
        console.log("getCompanyAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
    },
    async showDetails(company, isDoashBoard) {
      const companyDetail = await this.getCompanyDetail(company.id);
      this.company = { ...companyDetail, ...company };
      this.isDoashBoard = isDoashBoard;
      this.clearKey++;
      await this.getCompanyAttachment();
      $("#op-company-details").modal("show");
    },
    getCompanyDetail: async function (companyId) {
      const { data } = await axios.get(`/api/companies/${companyId}`);
      return data;
    },

    dimissModal: function () {
      if (this.isDoashBoard) {
        $("#op-company-details").modal("hide");
        return (location.href = `/admin/companies`);
      }
      $("#op-company-details").modal("hide");
    },
  },
  mounted() {},
};
</script>

<style scoped>
a {
  color: #3491ff;
}

a:hover {
  color: #3585e5;
  text-decoration: underline;
}
.modal-title {
  background: rgb(245, 248, 255);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 25.5px 11.5px 31px;
  border-radius: 6px 6px 0px 0px;
}

.modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}
</style>
