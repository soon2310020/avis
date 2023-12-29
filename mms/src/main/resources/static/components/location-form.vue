<template>
  <div
    style="overflow: scroll"
    id="op-location-form"
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
                    <strong v-text="resources['location']"></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="name">
                        {{ resources["company"] }}
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-5">
                        <input type="hidden" v-model="location.companyId" />
                        <input
                          type="text"
                          v-model="location.companyName"
                          readonly="readonly"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              location.companyName == '' ||
                              location.companyName == null,
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
                            '#op-company-search-location-modal'
                          "
                          v-text="resources['company_search']"
                        ></button>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="name">
                        {{ resources["location_name"] }}
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="name"
                          v-model="location.name"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              location.name == '' || location.name == null,
                          }"
                          :placeholder="resources['location_name']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="location-code"
                      >
                        {{ resources["location_code"] }}
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="location-code"
                          v-model="location.locationCode"
                          :maxlength="20"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              location.locationCode == '' ||
                              location.locationCode == null,
                          }"
                          :placeholder="resources['location_code']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="address">
                        {{ resources["address"] }}
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          v-model="location.address"
                          type="text"
                          id="address"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              location.address == '' ||
                              location.address == null,
                          }"
                          :placeholder="resources['address']"
                          required
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
                              location.memo == '' || location.memo == null,
                          }"
                          id="textarea-input"
                          v-model="location.memo"
                          name="textarea-input"
                          rows="9"
                          :placeholder="resources['memo']"
                        >
                        </textarea>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        v-text="resources['enable']"
                      ></label>
                      <div class="col-md-10 col-form-label">
                        <div class="form-check form-check-inline mr-3">
                          <label class="form-check-label">
                            <input
                              type="radio"
                              v-model="location.enabled"
                              class="form-check-input"
                              value="true"
                              name="enabled"
                            />
                            <span v-text="resources['enable']"></span>
                          </label>
                        </div>
                        <div class="form-check form-check-inline">
                          <label class="form-check-label">
                            <input
                              type="radio"
                              v-model="location.enabled"
                              class="form-check-input"
                              value="false"
                              name="disabled"
                            />
                            <span v-text="resources['disable']"></span>
                          </label>
                        </div>
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
    <company-search
      :id="'op-company-search-location-modal'"
      :resources="resources"
      :parent-name="compName"
      @setcompany="callbackCompany"
    ></company-search>
  </div>
</template>

<script>
module.exports = {
  name: "location-form",
  props: {
    resources: Object,
    updated: Function,
  },
  components: {
    "company-search": httpVueLoader("/components/company-search.vue"),
  },
  data() {
    return {
      location: {
        locationType: "",
        locationCode: "",
        name: "",
        address: "",
        memo: "",
        companyId: "",
        companyName: "",
        countryCode: "",
        enabled: true,
      },

      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
      },
      codes: {},
      compName: "modal",
      isTable: "",
    };
  },
  methods: {
    submit() {
      if (!this.location.companyId) {
        Common.alert("Company is required");
        return;
      }

      this.update();
    },

    async update() {
      // var geocoder = new google.maps.Geocoder();
      // let locationData = this.location;
      // let self = this;
      // if (!this.location.countryCode) {
      //   geocoder.geocode(
      //     { address: this.location.address },
      //     function (results, status) {
      //       if (status === "OK" && results.length > 0) {
      //         const place = results[0];
      //         const { address_components, geometry } = place;
      //         if (address_components.length > 0) {
      //           const { location } = geometry;
      //           address_components.forEach((value) => {
      //             if (value.types.includes("country")) {
      //               this.location.countryCode = value.short_name;
      //               this.location.latitude = location.lat();
      //               this.location.longitude = location.lng();
      //               axios
      //                 .put(`/api/locations/${locationData.id}`, locationData)
      //                 .then(function (response) {
      //                   if (response.data.success) {
      //                     if (self.isTable == "table") {
      //                       Common.alertCallback = function () {
      //                         self.updated();
      //                         $(
      //                           self.getRootToParentId() + "#op-location-form"
      //                         ).modal("hide");
      //                       };
      //                       Common.alert("success");
      //                     } else {
      //                       self.dimissModal();
      //                     }
      //                   } else {
      //                     Common.alert(response.data.message);
      //                   }
      //                 })
      //                 .catch(function (error) {
      //                   console.log(error.response);
      //                 });
      //             }
      //           });
      //         }
      //       } else {
      //         Common.alertCallback = function () {
      //           this.apiUpdate();
      //         };
      //         Common.confirm(
      //           "Your location cannot be found on bing map. Please correct your address or confirm your input."
      //         );
      //       }
      //     }
      //   );
      // } else {
      //   axios
      //     .put(`/api/locations/${this.location.id}`, this.location)
      //     .then(function (response) {
      //       console.log(response.data);

      //       if (response.data.success) {
      //         Common.alertCallback = function () {
      //           self.dimissModal();
      //         };
      //         Common.alert("success");
      //       } else {
      //         Common.alert(response.data.message);
      //       }
      //     })
      //     .catch(function (error) {
      //       console.log(error.response);
      //     });
      // }

      const bingMapKey = `Ait1vq_QpGJ1dpbAJ99JFT89jyWTwJ3P3ee5d7DKsXjRhY0D--GzcdUT2Jh9hUan`;

      if (this.location?.countryCode) {
        Common.alertCallback(() => this.apiUpdate());
        Common.confirm(
          "Your location cannot be found on bing map. Please correct your address or confirm your input."
        );
        return;
      }

      try {
        const params = Common.param({
          addressLine: this.location.address,
          incl: "ciso2",
          key: bingMapKey,
        });
        console.log("ndm2010", params);
        const response = await $.getJSON(
          `http://dev.virtualearth.net/REST/v1/Locations?${params}`
        );
        const { resourceSets } = response;
        if (resourceSets?.[0]?.resources?.[0]) {
          const location = resourceSets?.[0]?.resources?.[0];
          this.location.latitude = location.point.coordinates[0];
          this.location.longitude = location.point.coordinates[1];
          this.location.countryCode = location.point.countryRegionIso2;
          const updatedResponse = await axios.put(
            `/api/locations/${this.location.id}`,
            this.location
          );
          if (updatedResponse?.data?.success) {
            if (this.isTable == "table") {
              Common.alertCallback(() => this.updated());
              const locationForm = $(
                this.getRootToParentId() + "#op-location-form"
              );
              locationForm.modal("hide");
              Common.alert("success");
            } else {
              this.dimissModal();
            }
          } else {
            Common.alert(updatedResponse?.data?.message);
          }
        } else {
          Common.alert(
            "Your location cannot be found on bing map. Please correct your address or confirm your input."
          );
        }
      } catch (error) {
        console.log(error?.response);
      }
    },
    apiUpdate() {
      this.children = null;
      let self = this;
      axios
        .put(`/api/locations/${this.location.id}`, this.location)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            Common.alertCallback = function () {
              self.dimissModal();
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
    cancel() {
      if (typeof Page !== "undefined") location.href = Common.PAGE_URL.LOCATION;
    },
    callbackCompany(cbObject) {
      let company = cbObject.company;
      console.log("company: ", company);
      if (this.location.company == undefined) {
        this.location.company = {
          id: "",
          name: "",
        };
      }
      this.location.company.id = company.id;
      this.location.company.name = company.name;
      this.location.companyId = company.id;
      this.location.companyName = company.name;
      this.location.company = company.company;
    },
    onMountedRun() {
      this.$nextTick(function () {
        setTimeout(function () {
          console.log("Run there");
          Common.initRequireBadge();
        }, 200);
      });
    },
    showForm(location, page) {
      console.log("Location: ", location);
      this.isTable = page;
      //clone
      this.location = JSON.parse(JSON.stringify(location));

      $(this.getRootId() + "#op-location-form").modal("show");
    },
    dimissModal() {
      this.updated();
      $(this.getRootId() + "#op-location-form").modal("hide");
    },
  },
  watch: {
    "location.address": function (oldValue, newValue) {
      this.location.countryCode = "";
    },
  },
  created() {
    this.onMountedRun();
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
  background: #d6dade;
  width: 8px;
  padding-right: 8px;
}
.modal-content {
  width: 101% !important;
}
</style>
