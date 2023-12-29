<template>
  <div>
    <div
      id="op-part-details"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true"
    >
      <form @submit.prevent="submit">
        <div class="modal-dialog modal-lg" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h4 class="modal-title" id="title-part-chart">
                <span v-text="resources['part_id']"></span>: {{ part.partCode }}
              </h4>
              <button
                type="button"
                class="close"
                data-dismiss="modal"
                aria-label="Close"
              >
                <span aria-hidden="true">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <div class="card">
                <div class="card-header">
                  <i class="fa fa-align-justify"></i>
                  <span v-text="resources['part']"></span>
                </div>

                <table class="table table-mold-details">
                  <tbody>
                    <tr v-if="!checkDeletePart['category']">
                      <th v-text="resources['category']"></th>
                      <td>{{ part.categoryName }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['projectName']">
                      <th v-text="resources['project']"></th>
                      <td>{{ part.projectName || part.productName }}</td>
                    </tr>
                    <tr>
                      <th v-text="resources['part_id']"></th>
                      <td>{{ part.partCode }}</td>
                    </tr>
                    <tr>
                      <th v-text="resources['part_name']"></th>
                      <td>{{ part.name }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['resinCode']">
                      <th v-text="resources['part_resin_code']"></th>
                      <td>{{ part.resinCode }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['resinGrade']">
                      <th v-text="resources['part_resin_grade']"></th>
                      <td>{{ part.resinGrade }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['designRevision']">
                      <th v-text="resources['design_revision_level']"></th>
                      <td>{{ part.designRevision }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['size']">
                      <th v-text="resources['part_volume_w_d_h']"></th>
                      <td>{{ part.partSize }}</td>
                      <!--<td v-if="part.sizeUnit == 'MM' || part.sizeUnit == 'CM' || part.sizeUnit == 'M'">{{part.partSize}}³</td>-->
                      <!--<td v-else>{{part.partSize}}</td>-->
                    </tr>
                    <tr v-if="!checkDeletePart['weight']">
                      <th v-text="resources['part_weight']"></th>
                      <td>{{ part.partWeight }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['weeklyDemand']">
                      <th v-text="resources['weekly_demand']"></th>
                      <td>{{ part.weeklyDemand }}</td>
                    </tr>
                    <tr v-if="!checkDeletePart['partPictureFile']">
                      <th v-text="resources['part_picture']"></th>
                      <td>
                        <a
                          v-for="file in partPictureFiles"
                          href="javascript:void(0)"
                          class="btn btn-outline-dark btn-sm mr-1 mb-1"
                          @click="showFile(file)"
                          >{{ file.fileName }}</a
                        >
                      </td>
                    </tr>
                    <!--Custom field-->
                    <tr
                      v-for="(item, index) in listConfigCustomField"
                      :key="index"
                    >
                      <th>{{ item.fieldName }}</th>
                      <td>
                        {{ item.defaultInputValue }}
                      </td>
                    </tr>
                    <!-- <tr>
                                        <th>Part Price</th>
                                        <td>{{part.partPrice}}</td>
                                    </tr>
                                    <tr>
                                        <th>Downstream site</th>
                                        <td>{{part.downstreamSite}} </td>
                                    </tr>
                                    <tr>
                                        <th>Comments</th>
                                        <td>{{part.memo}} </td>
                                    </tr> -->
                  </tbody>
                </table>
              </div>
            </div>

            <div class="modal-footer text-center">
              <button
                type="button"
                class="btn btn-secondary"
                data-dismiss="modal"
                v-text="resources['close']"
              ></button>
            </div>
          </div>
        </div>
      </form>
    </div>
    <file-previewer :back="backFromPreview"></file-previewer>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  data() {
    return {
      part: {
        partCode: "",
        category: {
          name: "",
          parent: {
            name: "",
          },
        },
      },
      partPictureFiles: [],
      listConfigCustomField: null,
    };
  },
  components: {
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
  },
  computed: {
    checkDeletePart() {
      return headerVm?.options?.PART?.fields.reduce((prev, curr) => {
        return { ...prev, [curr.fieldName]: curr.deletedField };
      }, {});
    },
  },
  methods: {
    async showPartDetails(part) {
      console.log("showPartDetails:::part", part);
      const { data: partData } = await axios.get(`/api/parts/${part.id}`);
      this.part = { ...partData, ...part };
      this.getListConfigCustomField();
      this.loadFiles();

      $("#op-part-details").modal("show");
    },
    getListConfigCustomField() {
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
      var param = {
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
      $("#op-part-details").modal("hide");
      this.showFilePreviewer(file);
    },
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    backFromPreview() {
      $("#op-part-details").modal("show");
    },
  },
};
</script>
