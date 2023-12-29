<template>
  <div id="part-detail-popup">
    <div :style="styleCustom">
      <div class="card">
        <div class="card-header">
          <i class="fa fa-align-justify"></i>
          <span v-text="resources['part']"></span>
        </div>
        <div>
          <table class="table table-mold-details">
            <tbody>
              <tr>
                <th v-text="resources['category_name']"></th>
                <td>{{ part.categoryName }}</td>
              </tr>
              <tr>
                <th v-text="resources['project_name']"></th>
                <td>{{ part.projectName || part.productName }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_id']"></th>
                <td>{{ part.partCode }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_name']"></th>
                <td>{{ part.partName || part.name }}</td>
              </tr>
              <tr>
                <th v-text="resources['quantity_required']"></th>
                <td>{{ formatNumber(part.quantityRequired) }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_resin_code']"></th>
                <td>{{ part.resinCode }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_resin_grade']"></th>
                <td>{{ part.resinGrade }}</td>
              </tr>
              <tr>
                <th v-text="resources['design_revision_level']"></th>
                <td>{{ part.designRevision }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_volume_w_d_h']"></th>
                <td>{{ part.partSize }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_weight']"></th>
                <td>{{ part.partWeight }}</td>
              </tr>
              <tr>
                <th v-text="resources['weekly_demand']"></th>
                <td>{{ part.weeklyDemand }}</td>
              </tr>
              <tr>
                <th v-text="resources['part_picture']"></th>
                <td>
                  <preview-images-system
                    :images-uploaded="partPictureFiles"
                  ></preview-images-system>
                </td>
              </tr>

              <tr v-for="(item, index) in listConfigCustomField" :key="index">
                <th>{{ item.fieldName }}</th>
                <td>
                  {{ item.defaultInputValue }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    part: Object,
    showFilePreviewer: Function,
    styleCustom: Object,
  },
  data() {
    return {
      partPictureFiles: [],
      listConfigCustomField: [],
    };
  },
  components: {
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  methods: {
    getListConfigCustomField() {
      if (!this.part?.id) return;
      const url = `/api/custom-field-value/list-by-object?objectType=PART&objectId=${this.part.id}`;
      axios
        .get(url)
        .then((response) => {
          this.listConfigCustomField = response.data.map((item) => ({
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
          console.log(this.listConfigCustomField, "listConfigCustomField");
        });
    },
    loadFiles() {
      if (!this.part?.id) return;
      const param = {
        storageTypes: "PART_PICTURE",
        refId: this.part.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then((response) => {
          this.partPictureFiles = response.data["PART_PICTURE"]
            ? response.data["PART_PICTURE"]
            : [];
        });
    },
    showFile(file) {
      this.showFilePreviewer(file);
    },
    backFromPreview() {},
  },
  watch: {
    part(newValue) {
      if (!newValue?.id) return;
      this.getListConfigCustomField();
      this.loadFiles();
    },
  },
};
</script>
