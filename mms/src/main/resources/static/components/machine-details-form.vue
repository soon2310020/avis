<template>
  <div
    style="overflow: scroll"
    id="op-machine-details-form"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLabel"
    aria-hidden="true"
  >
    <form class="needs-validation" @submit.prevent="submit">
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
                <div class="card">
                  <div class="card-header">
                    <strong v-text="resources['machine']">Machine</strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="machine-code"
                        v-text="resources['machine_id']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          v-model="machine.machineCode"
                          type="text"
                          id="machine-code"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.machineCode == '' ||
                              machine.machineCode == null,
                          }"
                          :placeholder="resources['machine_id']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        v-text="resources['company']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-5">
                        <input type="hidden" v-model="machine.companyId" />
                        <input
                          type="text"
                          v-model="machine.companyName"
                          readonly="readonly"
                          :placeholder="resources['company']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.companyName == '' ||
                              machine.companyName == null,
                          }"
                          required
                        />
                      </div>
                      <div class="col-md-5">
                        <button
                          type="button"
                          class="btn btn-outline-success"
                          data-toggle="modal"
                          :data-target="
                            getRootToCurrentId() +
                            '#op-company-search-machine-modal'
                          "
                          v-text="resources['company_search']"
                        ></button>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        v-text="resources['location']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-5">
                        <input type="hidden" v-model="machine.locationId" />
                        <input
                          type="text"
                          v-model="machine.locationName"
                          readonly="readonly"
                          :placeholder="resources['location']"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.locationName == '' ||
                              machine.locationName == null,
                          }"
                          required
                        />
                      </div>
                      <div class="col-md-5">
                        <button
                          type="button"
                          class="btn btn-outline-success"
                          data-toggle="modal"
                          :data-target="
                            getRootToCurrentId() +
                            '#op-location-search-machine-modal'
                          "
                          v-text="resources['location_search']"
                        ></button>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="line"
                        v-text="resources['line']"
                        >Line <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          v-model="machine.line"
                          type="text"
                          id="line"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.line == '' || machine.line == null,
                          }"
                          :placeholder="resources['Line']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="machine-maker"
                        v-text="resources['machine_maker']"
                        >Machine Maker</label
                      >
                      <div class="col-md-10">
                        <input
                          v-model="machine.machineMaker"
                          type="text"
                          id="machine-maker"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.machineMaker == '' ||
                              machine.machineMaker == null,
                          }"
                          :placeholder="resources['machine_maker']"
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="machine-type"
                        v-text="resources['machine_type']"
                        >Machine Type</label
                      >
                      <div class="col-md-10">
                        <input
                          v-model="machine.machineType"
                          type="text"
                          id="machine-type"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.machineType == '' ||
                              machine.machineType == null,
                          }"
                          :placeholder="resources['machine_type']"
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="machine-model"
                        v-text="resources['machine_model']"
                        >Machine Model</label
                      >
                      <div class="col-md-10">
                        <input
                          v-model="machine.machineModel"
                          type="text"
                          id="machine-model"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.machineModel == '' ||
                              machine.machineModel == null,
                          }"
                          :placeholder="resources['machine_model']"
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="machine-tonnage"
                        v-text="resources['machine_tonnage']"
                        >Machine Tonnage (ton)</label
                      >
                      <div class="col-md-10">
                        <input
                          v-model="machine.machineTonnage"
                          min="1"
                          type="number"
                          id="machine-tonnage"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              machine.machineTonnage == '' ||
                              machine.machineTonnage == null,
                          }"
                          :placeholder="resources['machine_tonnage'] + ' (ton)'"
                        />
                      </div>
                    </div>
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="engineer-in-charge"
                        >Engineer in Charge</label
                      >
                      <div class="col-md-10">
                        <input
                          id="engineer-in-charge"
                          class="form-control"
                          placeholder="Engineer in Charge"
                        />
                      </div>
                    </div>
                    <div
                      v-for="(item, index) in customFieldList"
                      :key="index"
                      class="form-group row"
                    >
                      <label
                        class="col-md-2 col-form-label"
                        :for="'customFieldList' + index"
                      >
                        {{ item.fieldName }}
                        <span class="avatar-status"></span>
                        <span class="badge-require" v-if="item.required"></span>
                      </label>
                      <div class="col-md-10">
                        <input
                          :id="'customFieldList' + index"
                          type="text"
                          v-model="item.defaultInputValue"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              item.defaultInputValue == '' ||
                              item.defaultInputValue == null,
                          }"
                          :placeholder="item.fieldName"
                          :required="item.required"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-lg-12">
                <div class="card">
                  <div class="card-body text-center">
                    <button
                      type="submit"
                      class="btn btn-primary"
                      v-text="resources['save_changes']"
                    ></button>
                    <button
                      type="button"
                      class="btn btn-default"
                      @click="dimissModal()"
                      v-text="resources['cancel']"
                    ></button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
    <!-- Modal -->
    <location-search
      :id="'op-location-search-machine-modal'"
      :resources="resources"
      :parent-name="compName"
      @setlocation="callbackLocation"
    ></location-search>

    <!-- Modal -->
    <company-search
      :id="'op-company-search-machine-modal'"
      :resources="resources"
      :parent-name="compName"
      @setcompany="callbackCompany"
    ></company-search>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    updated: Function,
  },
  components: {
    "company-search": httpVueLoader("/components/company-search.vue"),
    "location-search": httpVueLoader("/components/location-search.vue"),
  },
  data() {
    return {
      machine: {
        machineCode: "",
        locationId: "",
        locationName: "",
        locationCode: "",
        companyId: "",
        companyName: "",
        countryCode: "",
        line: "",
        machineMaker: "",
        machineType: "",
        machineModel: "",
        machineTonnage: "",
        enabled: true,
      },
      customFieldList: null,
      compName: "modal",
      isTable: "",
    };
  },
  methods: {
    engineerSelect() {
      console.log("engineerSelect");
      axios.get().then((res) => {
        console.log(res);
      });
    },
    submit: async function () {
      if (!this.machine.companyId) {
        Common.alert("Company is required");
        return;
      }
      if (!this.machine.locationId) {
        Common.alert("Location is required");
        return;
      }
      if (this.machine.machineTonnage && this.machine.machineTonnage < 1) {
        Common.alert("Machine tonnage must be positive number");
        return;
      }

      // await this.updateCustomField();
      this.update();
    },
    callbackCompany: function (searchCompany) {
      let company = searchCompany.company;
      console.log("Company Selected ", company);
      if (!this.machine.company) {
        this.machine.company = {
          id: "",
          name: "",
        };
      }
      // this.machine.company.id = company.id;
      // this.machine.company.name = company.name;
      this.machine.companyId = company.id;
      this.machine.companyName = company.name;
      this.machine.company = company;
      console.log("After ", this.machine.company);
    },
    callbackLocation: function (location) {
      console.log(location);
      if (!this.machine.location) {
        this.machine.location = {
          id: "",
          name: "",
        };
      }
      // this.machine.location.id = location.id;
      // this.machine.location.name = location.name;
      this.machine.locationId = location.id;
      this.machine.locationName = location.name;
      this.machine.location = location;
      console.log("After ", this.machine.location);
    },

    // create: function () {
    //   this.createApi();
    // },

    // createApi: function () {
    //   const arr = this.customFieldList.map((item) => ({
    //     id: item.id,
    //     customFieldValueDTOList: [
    //       {
    //         value: item.defaultInputValue,
    //       },
    //     ],
    //   }));
    //   const params = {
    //     customFieldDTOList: arr,
    //   };
    //   axios
    //     .post("/api/machines", this.machine)
    //     .then(function (response) {
    //       const objectId = response.data.id;
    //       axios
    //         .post(`/api/custom-field-value/edit-list/${objectId}`, params)
    //         .then(() => {
    //           Common.alertCallback = function () {
    //             location.href = Page.LIST_PAGE;
    //           };
    //           Common.alert("success");
    //         })
    //         .catch((e) => {
    //           console.log(e);
    //         });
    //     })
    //     .catch(function (error) {
    //       console.log("error", error);
    //       Common.alert(error.response.data);
    //     });
    // },
    async updateCustomField() {
      console.log("this.customfield", this.customFieldList);
      if (this.customFieldList != null) {
        const arr = this.customFieldList.map((item) => ({
          id: item.id,
          customFieldValueDTOList: [
            {
              value: item.defaultInputValue,
            },
          ],
        }));
        const params = {
          customFieldDTOList: arr,
        };
        console.log("this.customfield", this.customFieldList);
        console.log("arr", arr);
        await axios.post(
          `/api/custom-field-value/edit-list/${this.machine.id}`,
          params
        );
      }
    },

    apiUpdate: function () {
      this.children = null;
      console.log("this.machine: ", this.machine);
      let self = this;
      axios
        .put(`/api/machines/${this.machine.id}`, this.machine)
        .then(async function (response) {
          console.log(response.data);

          await self.updateCustomField();

          // if (response.data.success) {

          if (self.isTable == "table") {
            Common.alertCallback = function () {
              // location.href = Page.LIST_PAGE;
              self.dimissModal();
            };
            Common.alert("success");
          } else {
            self.dimissModal();
          }
          // } else {
          //     Common.alert(response.data.message);
          // }
        })
        .catch(function (error) {
          console.log(error);
          Common.alert(error.response.data);
        });
    },

    update: function () {
      this.apiUpdate();
    },

    onMountedRun() {
      this.$nextTick(function () {
        var self = this;
        // 데이터 조회
        axios.get(`/api/machines/${this.machine.id}`).then(function (response) {
          this.machine = response.data;
          setTimeout(function () {
            Common.initRequireBadge();
          }, 100);
        });
      });
    },
    onCreatedRun() {
      let self = this;
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=MACHINE&objectId=${this.machine.id}`
        )
        .then(function (response) {
          console.log(response.data, "/api/custom-field-value/list-by-object");
          self.customFieldList = response.data.map((item) => ({
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        });
    },
    getListConfigCustomField() {
      var self = this;
      const url = `/api/custom-field-value/list-by-object?objectType=MACHINE&objectId=${self.machine.id}`;
      axios
        .get(url)
        .then((response) => {
          self.listConfigCustomField = response.data.map((item) => ({
            propertyGroup: item.propertyGroup,
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        })
        .catch((error) => {
          console.log(error.response);
        })
        .finally(() => {
          console.log(self.listConfigCustomField, "listConfigCustomField");
        });
    },
    showDetails: function (machine, page) {
      this.isTable = page;
      console.log(machine, "machine here");
      this.machine = machine;
      this.onCreatedRun();
      this.onMountedRun();
      this.getListConfigCustomField();
      $(this.getRootId() + "#op-machine-details-form").modal("show");
    },

    // getPic(file) {
    //     return "http://49.247.200.147" + file.saveLocation;
    // },

    dimissModal: function () {
      this.updated();
      $(this.getRootId() + "#op-machine-details-form").modal("hide");
    },
  },
};
</script>

<style>
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
  overflow-y: auto;
  max-height: 600px;
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
  background: #D6DADE;
  width: 8px;
  padding-right: 8px;
}
.modal-content {
  width: 101% !important;
}
</style>