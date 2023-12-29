var Page = Common.getPage("locations");
const newPageUrl = Common.PAGE_URL;

window.onload = function () {
  document.title = "Locations" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_location");
    $("div").removeClass("profile_wave");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    document.getElementById("remove_profile").remove();
    $("div").removeClass("hide_account");
  }, 500);
};

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

var vm = new Vue({
  el: "#app",
  components: {
    "company-search": httpVueLoader("/components/company-search.vue"),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  data: {
    resources: {},
    mode: Page.MODE,
    location: {
      locationType: "",
      locationCode: "",
      name: "",
      // TODO(ducnm2010): change back when commit
      address: "",
      memo: "",
      companyId: "",
      companyName: "",
      countryCode: "",
      timeZoneId: "",
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
    thirdFiles: [],
    uploadedFiles: [],
  },
  methods: {
    submit() {
      if (!this.location.companyId) {
        Common.alert("Company is required");
        return;
      }
      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
    },

    async create() {
      // TODO(ducnm2010): Remove after bing map working fine
      // const key = "ngQzfIPj6nvS2S5Azu9GfMSAVYDuJMn4";
      // const mapQuestApiUrl = `https://www.mapquestapi.com/geocoding/v1/address?key=${key}`;
      // console.log("vm.location: ", vm.location);
      // if (!vm.location.countryCode) {
      //   await $.getJSON(
      //     mapQuestApiUrl + `&location=${vm.location.address}`,
      //     async function (geo) {
      //       let data = await geo.results[0]?.locations[0];
      //       if (data) {
      //         vm.location.countryCode = data.adminArea1;
      //         vm.location.latitude = data.latLng.lat;
      //         vm.location.longitude = data.latLng.lng;
      //         vm.createApi();
      //       } else {
      //         Common.alertCallback = function () {
      //           vm.createApi();
      //         };
      //         Common.confirm(
      //           "Your location cannot be found on google map. Please correct your address or confirm your input."
      //         );
      //       }
      //     }
      //   );
      // } else {
      //   vm.createApi();
      // }

      // TODO(ducnm2010): BING MAP API Ait1vq_QpGJ1dpbAJ99JFT89jyWTwJ3P3ee5d7DKsXjRhY0D--GzcdUT2Jh9hUan
      if (this.location?.countryCode) {
        this.createApi();
        return;
      }

      const bingMapKey =
        "Ait1vq_QpGJ1dpbAJ99JFT89jyWTwJ3P3ee5d7DKsXjRhY0D--GzcdUT2Jh9hUan";
      try {
        const params = Common.param({
          addressLine: this.location.address,
          incl: "ciso2",
          key: bingMapKey,
        });
        console.log("nmd2010", params);
        const response = await $.getJSON(
          `http://dev.virtualearth.net/REST/v1/Locations?${params}`
        );
        const { resourceSets } = response;
        if (resourceSets?.[0]?.resources?.[0]) {
          const location = resourceSets[0].resources[0];
          this.location.latitude = location.point.coordinates[0];
          this.location.longitude = location.point.coordinates[1];
          this.location.countryCode = location.address.countryRegionIso2;
          this.createApi();
        } else {
          console.log("nmd2010:no result");
          Common.alertCallback(() => this.createApi());
          Common.confirm(
            "Your location cannot be found on bing map. Please correct your address or confirm your input."
          );
        }
      } catch (error) {
        console.log(error);
      }
    },

    deleteThirdFiles(index) {
      vm.thirdFiles = vm.thirdFiles.filter((value, idx) => idx !== index);
      if (vm.thirdFiles.length == 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteFileStorage(file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case "LOCATION_PICTURE":
              vm.uploadedFiles = response.data;
              break;
          }
        });
    },
    selectedThirdFiles(e) {
      var files = e.target.files;
      var isExitsFile = vm.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = vm.uploadedFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.thirdFiles.push(selectedFiles[i]);
        }
      }
    },

    getFormData() {
      var formData = new FormData();
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("files", file);
      }
      formData.append("payload", JSON.stringify(this.location));
      return formData;
    },

    async initFiles() {
      try {
        const param = {
          storageTypes: "LOCATION_PICTURE",
          refId: this.location.id,
        };
        const response = await axios.get(
          "/api/file-storage/mold?" + Common.param(param)
        );
        this.uploadedFiles = response.data["LOCATION_PICTURE"]
          ? response.data["LOCATION_PICTURE"]
          : [];
      } catch (error) {
        console.log(error);
      }
    },

    async createApi() {
      try {
        const payload = this.getFormData();
        const response = await axios.post(
          `/api/locations/add-multipart`,
          payload
        );
        if (response.data.success) {
          Common.alertCallback = function () {
            location.href = newPageUrl.LOCATION;
          };
          Common.alert("success");
        } else {
          Common.alert(response.data.message);
        }
      } catch (error) {
        console.log(error);
      }
    },

    async apiUpdate() {
      vm.children = null;
      try {
        const id = vm.location.id;
        const payload = this.getFormData();
        const response = await axios.put(
          `/api/locations/edit-multipart/${id}`,
          payload
        );
        if (response.data.success) {
          Common.alertCallback = function () {
            location.href = newPageUrl.LOCATION;
          };
          Common.alert("success");
        } else {
          Common.alert(response.data.message);
        }
      } catch (error) {
        console.log(error);
      }
    },

    async update() {
      // var geocoder = new google.maps.Geocoder();
      // console.log("vm.location: ", vm.location);
      // if (!vm.location.countryCode) {
      //   geocoder.geocode(
      //     { address: vm.location.address },
      //     function (results, status) {
      //       console.log("results: ", results);
      //       if (status === "OK" && results.length > 0) {
      //         const place = results[0];
      //         const { address_components, geometry } = place;
      //         if (address_components.length > 0) {
      //           const { location } = geometry;
      //           address_components.forEach((value) => {
      //             if (value.types.includes("country")) {
      //               vm.location.countryCode = value.short_name;
      //               vm.location.latitude = location.lat();
      //               vm.location.longitude = location.lng();
      //               vm.apiUpdate();
      //             }
      //           });
      //         }
      //       } else {
      //         Common.alertCallback = function () {
      //           vm.apiUpdate();
      //         };
      //         Common.confirm(
      //           "Your location cannot be found on google map. Please correct your address or confirm your input."
      //         );
      //       }
      //     }
      //   );
      // } else {
      //   vm.apiUpdate();
      // }

      if (this.location?.countryCode) {
        this.apiUpdate();
        return;
      }

      const bingMapKey =
        "Ait1vq_QpGJ1dpbAJ99JFT89jyWTwJ3P3ee5d7DKsXjRhY0D--GzcdUT2Jh9hUan";
      try {
        const params = Common.param({
          addressLine: this.location.address,
          incl: "ciso2",
          key: bingMapKey,
        });
        console.log("nmd2010", params);
        const response = await $.getJSON(
          `http://dev.virtualearth.net/REST/v1/Locations?${params}`
        );
        const { resourceSets } = response;
        if (resourceSets?.[0]?.resources?.[0]) {
          const location = resourceSets[0].resources[0];
          this.location.latitude = location.point.coordinates[0];
          this.location.longitude = location.point.coordinates[1];
          this.location.countryCode = location.address.countryRegionIso2;
          this.apiUpdate();
        } else {
          console.log("nmd2010:no result");
          Common.alertCallback(() => this.apiUpdate());
          Common.confirm(
            "Your location cannot be found on bing map. Please correct your address or confirm your input."
          );
        }
      } catch (error) {
        console.log(error);
      }

      // countryCode*
      // latitude*
      // longtitude*
      // timeZoneId (optional)
    },

    cancel() {
      location.href = newPageUrl.LOCATION;
    },
    callbackCompany(company) {
      console.log("company: ", company);
      if (vm.location.company == undefined) {
        vm.location.company = {
          id: "",
          name: "",
        };
      }
      vm.location.company.id = company.id;
      vm.location.company.name = company.name;
      vm.location.companyId = company.id;
      vm.location.companyName = company.name;
    },
  },
  computed: {
    readonlyLocationCode() {
      return Page.IS_EDIT;
    },
    New() {
      return Page.IS_NEW ? this.resources["new"] : this.resources["edit"];
    },
  },
  watch: {
    "location.address": function (oldValue, newValue) {
      this.location.countryCode = "";
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.codes = Object.assign({}, this.codes, { ...newVal });
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, { ...newVal });
        }
      },
      { immediate: true }
    );
  },
  async mounted() {
    if (!Page.IS_EDIT) return;

    try {
      const response = await axios.get(Page.API_GET);
      this.location = response.data;
      this.initFiles();
    } catch (error) {
      console.log(error);
    }
  },
});
