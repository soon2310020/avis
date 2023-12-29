<template>
  <base-dialog
    title="Plant Details"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div>
      <div class="d-flex justify-space-between">
        <div style="margin-right: 19px">
          <base-images-slider
            :images="plant.attachment"
            :key="viewImageKey"
            :clear-key="clearKey"
            :handle-submit-file="handleSubmitImage"
          ></base-images-slider>
        </div>
        <div style="width: 520px">
          <div style="height: 600px; padding: 0 10px; overflow: auto">
            <div class="card">
              <div
                @click="expand(`plant-detail-information`)"
                class="card-header"
              >
                <i class="fa fa-align-justify"></i>
                <span>Plant</span>
                <!-- <img id="plant-detail-information-image"
                                    style="position: absolute; right: 20px; top: 1rem"
                                    src="/images/icon/angle-arrow-up.svg" width="8" height="8" /> -->
              </div>

              <table
                id="plant-detail-information"
                class="table table-mold-details"
                style="display: table"
              >
                <tbody>
                  <tr>
                    <th>Plant Name</th>
                    <td>{{ plant.locationTitle }}</td>
                  </tr>
                  <tr>
                    <th>Plant ID</th>
                    <td>{{ plant.locationCode }}</td>
                  </tr>
                  <tr>
                    <th>Plant Areas</th>
                    <td>
                      <div>
                        <div
                          v-for="area in plant.areas"
                          :key="area.id"
                          class="d-flex align-center justify-space-between"
                          style="min-width: 50%"
                        >
                          <span>{{ area.name }}</span>
                          <base-chip
                            :selection="areaTypes[area.areaType]"
                            :allow-tooltips="false"
                            :color="'wheat'"
                            :close-able="false"
                          >
                          </base-chip>
                        </div>
                        <span
                          class="custom-hyper-link"
                          @click="handleViewPlantTerminals"
                        >
                          View Plant Terminals
                          <div
                            class="hyper-link-icon"
                            style="margin-left: 3px"
                          ></div>
                        </span>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th>Company</th>
                    <td>{{ plant.companyName }}</td>
                  </tr>
                  <tr>
                    <th>Address</th>
                    <td>{{ plant.address }}</td>
                  </tr>
                  <tr>
                    <th>Time Zone</th>
                    <td>{{ plant.timeZoneId }}</td>
                  </tr>
                  <tr>
                    <th>Memo</th>
                    <td>{{ plant.memo }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </base-dialog>
</template>

<script>
const API_BASE_PLT_STP = Common.getApiBase().LOCATION;
const TYPES = {
  MAINTENANCE_AREA: {
    id: 1,
    title: "Maintenance",
  },
  PRODUCTION_AREA: {
    id: 2,
    title: "Production",
  },
  WAREHOUSE: {
    id: 3,
    title: "Warehouse",
  },
};
module.exports = {
  components: {
    "base-images-slider": httpVueLoader(
      "/components/@base/images-preview/base-images-slider.vue"
    ),
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    selectedPlant: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      plant: {},
      viewImageKey: 0,
      clearKey: 0,
      areaTypes: { ...TYPES },
    };
  },
  watch: {
    async visible(newVal) {
      if (newVal) {
        this.plant = { ...this.selectedPlant };
        this.clearKey++;
        await this.getPlantAttachment();
        console.log("visible show", this.plant);
      }
    },
  },
  methods: {
    async handleViewPlantTerminals() {
      const plantId = this.plant.id;
      const { data: plantAreasData } = await axios.get(
        `${API_BASE_PLT_STP}/${plantId}/areas`
      );
      const areaIds = plantAreasData.content.map((item) => item.id);

      console.log("handleViewPlantTerminals", areaIds);
      window.location.href = `${
        Common.PAGE_URL.TERMINAL
      }?areaIds=${areaIds.join("-")}`;
    },
    expand(elementId) {
      var content = document.getElementById(elementId);
      var contentImage = document.getElementById(`${elementId}-image`);
      // basic info and part is default
      if (content) {
        if (content.style.display === "table") {
          content.style.display = "none";
          contentImage.src = "/images/icon/angle-arrow-down.svg";
        } else {
          content.style.display = "table";
          contentImage.src = "/images/icon/angle-arrow-up.svg";
        }
      }
    },
    handleClose() {
      this.$emit("close");
    },
    async getPlantAttachment() {
      try {
        const type = {
          storageType: "LOCATION_PICTURE",
          refId: this.plant.id,
        };
        const param = Common.param(type);
        const res = await axios.get(`/api/file-storage?${param}`);
        this.plant.attachment = res.data;
        this.viewImageKey++;
        console.log("getPlantAttachment", res.data);
      } catch (error) {
        console.log(error);
      }
    },
    async handleSubmitImage(file) {
      const refId = this.plant.id;
      const payload = this.createImageFormData(refId, file);
      try {
        const res = await axios.post("/api/file-storage", payload);
        const last = [...res.data].pop();
        this.plant.attachment.push(last);
      } catch (error) {
        console.log(error);
      }
    },
    createImageFormData(refId, file) {
      let formData = new FormData();
      formData.append("files", file);
      formData.append("storageType", "LOCATION_PICTURE");
      formData.append("refId", `${refId}`);
      return formData;
    },
  },
};
</script>

<style></style>
