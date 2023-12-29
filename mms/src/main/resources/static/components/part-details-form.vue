<template>
  <div
    style="overflow: scroll"
    id="op-part-details-form"
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
                border-radius: 6px 6px 0 0;
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
                    <strong v-text="resources['part']"></strong>
                  </div>
                  <div class="card-body">
                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['category']"
                    >
                      <label class="col-md-2 col-form-label" for="name">
                        <span v-text="resources['category']"></span>
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <select
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' || part.categoryId == null,
                          }"
                          v-model="part.categoryId"
                          required
                        >
                          <option
                            value=""
                            v-text="resources['category']"
                          ></option>

                          <template v-for="(category1, index1) in categories">
                            <optgroup :label="category1.name" :key="index1">
                              <template
                                v-for="(
                                  category2, index2
                                ) in category1.children"
                              >
                                <option
                                  :key="index2"
                                  :value="category2.id"
                                  :disabled="!category2.enabled"
                                >
                                  {{ category2.name }}
                                </option>
                              </template>
                            </optgroup>
                          </template>
                        </select>
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="partCode">
                        <span v-text="resources['part_id']"></span>
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="partCode"
                          v-model="part.partCode"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.partCode == '' || part.partCode == null,
                          }"
                          :placeholder="resources['part_id']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="name"
                        ><span v-text="resources['part_name']"></span
                        ><span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="name"
                          v-model="part.name"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.name == '' || part.name == null,
                          }"
                          :placeholder="resources['part_name']"
                          required
                        />
                      </div>
                    </div>

                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['resinCode']"
                    >
                      <label class="col-md-2 col-form-label" for="resinCode"
                        ><span v-text="resources['part_resin_code']"></span>
                        <span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('resinCode')"
                        ></span>
                      </label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="resinCode"
                          v-model="part.resinCode"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.resinCode == '' || part.resinCode == null,
                          }"
                          :placeholder="resources['part_resin_code']"
                          :required="isRequiredField('resinCode')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['resinGrade']"
                    >
                      <label class="col-md-2 col-form-label" for="resinCode"
                        ><span v-text="resources['part_resin_grade']"></span>
                        <span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('resinGrade')"
                        ></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="resinGrade"
                          v-model="part.resinGrade"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.resinGrade == '' || part.resinGrade == null,
                          }"
                          :placeholder="resources['part_resin_grade']"
                          :required="isRequiredField('resinGrade')"
                        />
                      </div>
                    </div>

                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['designRevision']"
                    >
                      <label
                        class="col-md-2 col-form-label"
                        for="designRevision"
                        ><span
                          v-text="resources['design_revision_level']"
                        ></span
                        ><span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('designRevision')"
                        ></span>
                      </label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="designRevision"
                          v-model="part.designRevision"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.designRevision == '' ||
                              part.designRevision == null,
                          }"
                          :placeholder="resources['design_revision_level']"
                          :required="isRequiredField('designRevision')"
                        />
                      </div>
                    </div>
                    <div class="form-group row" v-if="!checkDeletePart['size']">
                      <label class="col-md-2 col-form-label" for="size"
                        ><span
                          v-text="resources['part_volume'] + ' (W x D x H)'"
                        ></span>
                        <span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('size')"
                        ></span>
                      </label>
                      <div class="col-md-7">
                        <input
                          type="text"
                          id="size"
                          v-model="part.size"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.size == '' || part.size == null,
                          }"
                          :placeholder="
                            resources['part_volume'] + ' (W x D x H)'
                          "
                          :required="isRequiredField('size')"
                        />
                      </div>
                      <!--
                                <div style="display: flex; justify-content: space-between; align-items: center;"
                                     class="col-md-7">
                                    <input type="number" style="width: 30%;" id="size" v-model="part.sizeWidth" min="0" step="any"
                                           class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" :placeholder="resources['width}" :required="isRequiredField('size')">
                                    <div>x</div>
                                    <input type="number" style="width: 30%;" id="size" v-model="part.sizeDepth" min="0" step="any"
                                           class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" :placeholder="resources['depth}" :required="isRequiredField('size')">
                                    <div>x</div>
                                    <input type="number" style="width: 30%;" id="size" v-model="part.sizeHeight" min="0" step="any"
                                           class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" :placeholder="resources['height}" :required="isRequiredField('size')">
                                </div>-->

                      <div class="col-md-3">
                        <select
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.sizeUnit == '' || part.sizeUnit == null,
                          }"
                          v-model="part.sizeUnit"
                        >
                          <option
                            hidden
                            selected
                            value=""
                            v-text="resources['unit']"
                          ></option>
                          <template v-for="(code, index) in codes.SizeUnit">
                            <option
                              v-if="
                                code.code == 'MM' ||
                                code.code == 'CM' ||
                                code.code == 'M'
                              "
                              :key="index"
                              :value="code.code"
                            >
                              {{ code.title }}³
                            </option>
                            <option v-else :key="index" :value="code.code">
                              {{ code.title }}
                            </option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['weight']"
                    >
                      <label class="col-md-2 col-form-label" for="weight">
                        <span v-text="resources['part_weight']"></span>
                        <span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('weight')"
                        ></span>
                      </label>
                      <div class="col-md-7">
                        <input
                          type="number"
                          min="0.0"
                          id="weight"
                          v-model="part.weight"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.weight == '' || part.weight == null,
                          }"
                          step="any"
                          :placeholder="resources['part_weight']"
                          :required="isRequiredField('weight')"
                        />
                      </div>
                      <div class="col-md-3">
                        <select
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.weightUnit == '' || part.weightUnit == null,
                          }"
                          v-model="part.weightUnit"
                        >
                          <option
                            hidden
                            selected
                            value=""
                            v-text="resources['unit']"
                          ></option>

                          <template v-for="(code, index) in codes.WeightUnit">
                            <option :key="index" :value="code.code">
                              {{ code.title }}
                            </option>
                          </template>
                        </select>
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['weeklyDemand']"
                    >
                      <label
                        class="col-md-2 col-form-label"
                        for="designRevision"
                        ><span v-text="resources['weekly_demand']"></span
                        ><span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('weeklyDemand')"
                        ></span>
                      </label>
                      <div class="col-md-10">
                        <input
                          type="number"
                          id="weeklyDemand"
                          v-model="part.weeklyDemand"
                          min="0"
                          max="99999999"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              part.weeklyDemand == '' ||
                              part.weeklyDemand == null,
                          }"
                          placeholder="Weekly Demand"
                          :required="isRequiredField('weeklyDemand')"
                        />
                      </div>
                    </div>
                    <div
                      class="form-group row"
                      v-if="!checkDeletePart['partPictureFile']"
                    >
                      <label
                        class="col-md-2 col-form-label"
                        for="partPictureFile"
                        ><span v-text="resources['part_picture']"></span
                        ><span class="avatar-status badge-danger"></span>
                        <span
                          class="badge-require"
                          v-if="isRequiredField('partPictureFile')"
                        ></span>
                      </label>
                      <div class="col-md-10">
                        <div class="op-upload-button-wrap">
                          <button
                            id="partPictureFile"
                            class="btn btn-outline-success"
                            v-text="resources['upload_photo']"
                          ></button>
                          <input
                            type="file"
                            ref="fileupload"
                            id="files1"
                            @change="selectedThirdFiles"
                            multiple
                            style="height: 40px; width: 100%"
                            accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
                            :required="isRequiredField('partPictureFile', true)"
                          />
                        </div>

                        <div>
                          <button
                            v-for="(file, index) in thirdFiles"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="deleteThirdFiles(index)"
                            :key="index"
                          >
                            {{ file.name }}
                          </button>
                        </div>

                        <div class="mt-1">
                          <button
                            v-for="(file, index) in partPictureFiles"
                            :key="index"
                            class="btn btn-outline-dark btn-sm mr-1"
                            @click.prevent="
                              deleteFileStorage(file, fileTypes.PART_PICTURE)
                            "
                          >
                            {{ file.fileName }}
                          </button>
                        </div>
                      </div>
                    </div>
                    <!-- <div class="form-group row">
                                <label class="col-md-2 col-form-label" for="price">Part Price</label>
                                <div class="col-md-7">
                                    <input type="number" id="price" min="1" max="99999999"
                                        oninput="Common.maxLength(this, 8)" v-model="part.price" class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }"
                                        placeholder="Part Price">
                                </div>
                                <div class="col-md-3">
                                    <select class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" v-model="part.currencyType">
                                        <option value="">Currency</option>

                                        <template v-for="code in codes.CurrencyType">
                                            <option :value="code.code">{{ code.code }}</option>
                                        </template>
                                    </select>

                                </div>
                            </div> -->

                    <!-- <div class="form-group row">
                                <label class="col-md-2 col-form-label" for="downstreamSite">Downstream site <span
                                        class="avatar-status badge-danger"></span></label>
                                <div class="col-md-10">
                                    <input type="text" id="downstreamSite" v-model="part.downstreamSite"
                                        class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" placeholder="Downstream site">
                                </div>
                            </div> -->

                    <!-- <div class="form-group row">
                                <label class="col-md-2 col-form-label" for="textarea-input">Comments</label>
                                <div class="col-md-10">
                                    <textarea class="form-control"
                          :class="{
                            'form-control-warning':
                              part.categoryId == '' ||
                              part.categoryId == null,
                          }" id="textarea-input" v-model="part.memo"
                                        name="textarea-input" rows="9" placeholder="Memo"></textarea>
                                </div>
                            </div> -->

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
                              v-model="part.enabled"
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
                              v-model="part.enabled"
                              class="form-check-input"
                              value="false"
                              name="disabled"
                            />
                            <span v-text="resources['disable']"></span>
                          </label>
                        </div>
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
    <file-previewer :back="backFromPreview"></file-previewer>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    updated: Function,
  },
  components: {
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
  },
  data() {
    return {
      // mode: Page.MODE,
      categories: [],
      part: {
        // new size
        sizeWidth: "",
        sizeHeight: "",
        sizeDepth: "",
        name: "",
        partCode: "",
        resinCode: "",
        resinGrade: "",
        designRevision: "",
        size: "",
        sizeUnit: "MM",
        weight: "",
        weightUnit: "GRAMS",
        price: "",
        currencyType: "USD",
        downstreamSite: "",

        memo: "",
        enabled: true,
        categoryId: "",
        category: {},
        weeklyDemand: null,
      },
      thirdFiles: [],
      //edit
      partPictureFiles: [],
      fileTypes: {
        PART_PICTURE: "PART_PICTURE",
      },
      requiredFields: [],
      customFieldList: null,
      codes: {},
      configCategory: "PART",
      checkDeletePart: {},
      isTable: "",
      customFieldKey: 0,
    };
  },
  methods: {
    showPartDetails: function (part, page) {
      this.isTable = page;
      this.thirdFiles = [];
      console.log(part, "heyypppppp");
      this.part = part;
      this.getListConfigCustomField();
      this.loadFiles();
      // this.onCreatedRun();
      this.onMountedRun();
      $(this.getRootId() + "#op-part-details-form").modal("show");
    },
    getListConfigCustomField() {
      var self = this;
      const url = `/api/custom-field-value/list-by-object?objectType=PART&objectId=${self.part.id}`;
      axios
        .get(url)
        .then((response) => {
          self.customFieldList = response.data.map((item) => ({
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
          console.log(self.customFieldList, "customFieldList");
        });
    },
    loadFiles: function () {
      var self = this;

      var param = {
        storageTypes: "PART_PICTURE",
        refId: self.part.id,
      };

      axios
        .get("/api/file-storage/mold?" + Common.param(param))
        .then(function (response) {
          self.partPictureFiles = response.data["PART_PICTURE"]
            ? response.data["PART_PICTURE"]
            : [];
        });
    },
    showFile(file) {
      $(this.getRootId() + "#op-part-details-form").modal("hide");
      this.showFilePreviewer(file);
    },
    showFilePreviewer(file) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file);
      }
    },
    backFromPreview() {
      $(this.getRootId() + "#op-part-details-form").modal("show");
    },
    dimissModal: function () {
      this.updated();
      this.closeModal();
    },
    closeModal: function () {
      this.thirdFiles = [];
      $(this.getRootId() + "#op-part-details-form").modal("hide");
    },
    /*
    async onCreatedRun() {
      console.log('Complete part', this.part.id)
      let self=this;
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=PART&objectId=${this.part.id}`
        )
        .then(await function (response) {
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
          this.customFieldKey++;
          console.log('Done map custom list', self.customFieldList)
        })
        .finally(() => {
          this.$forceUpdate();
        }
        );
    },
*/
    isRequiredField(field, isInput) {
      if (
        isInput &&
        field == "partPictureFile" &&
        this.partPictureFiles != null &&
        this.partPictureFiles.length > 0
      ) {
        return false;
      }
      return this.requiredFields.includes(field);
    },
    submit: async function () {
      const { sizeWidth, sizeHeight, sizeDepth, WLH } = this.part;
      /*
                    this.part.size = `${sizeWidth|| ''}x${sizeDepth|| ''}x${sizeHeight|| ''}`;
*/
      this.update();
    },

    create: function () {
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
      axios
        .post(
          "/api/parts/add-multipart",
          this.formData(),
          this.multipartHeader()
        )
        .then(function (response) {
          const objectId = response.data.id;
          axios
            .post(`/api/custom-field-value/edit-list/${objectId}`, params)
            .then(() => {
              Common.alertCallback = function () {
                if (typeof Page !== "undefined")
                  location.href = Common.PAGE_URL.PART;
              };
              Common.alert("success");
            })
            .catch((e) => {
              console.log(e);
            });
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        });
    },
    update: function () {
      this.children = null;
      let self = this;
      axios
        .put(
          "/api/parts/edit-multipart" + "/" + this.part.id,
          this.formData(),
          this.multipartHeader()
        )
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
    async updateCustomField() {
      var objectId = this.part.id;
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
      await axios.post(`/api/custom-field-value/edit-list/${objectId}`, params);
    },
    /*
    async updateCustomField () {
      var objectId = this.part.id
      const arr = this.customFieldList.map((item) =>
      (
        {
          "id": item.id,
          "customFieldValueDTOList": [{
            "value": item.defaultInputValue
          }]
        }
      ));
      const params = {
        'customFieldDTOList': arr
      }
      await axios.post(`/api/custom-field?objectType=PART`, params)
    },
*/
    initDefaultValue() {
      const fields = [
        "category",
        "partCode",
        "name",
        "resinCode",
        "resinGrade",
        "designRevision",
        "size",
        "weight",
        "weeklyDemand",
      ];
      // get current config value
      axios
        .get("/api/config?configCategory=" + this.configCategory)
        .then((response) => {
          if (response.data && response.data.length > 0) {
            console.log("form response data", response.data);
            response.data.forEach((field) => {
              if (field.required) {
                this.requiredFields.push(field.fieldName);
              }
              if (this.checkDeletePart != null)
                this.checkDeletePart[field.fieldName] = field.deletedField;
            });
            /*
            if (!Page.IS_EDIT) {
              fields.forEach((field) => {
                let defaultField = response.data.filter(
                  (item) => item.fieldName === field
                )[0];
                console.log("defaultField", defaultField);
                if (!defaultField) return;
                // switch(field) {
                //     case 'category':
                //         // set for category id
                //         this.categories.forEach(category1 => {
                //             category1.children.forEach(category2 => {
                //                 if (defaultField.defaultInputValue && category2.name.toUpperCase() === defaultField.defaultInputValue.toUpperCase()) {
                //                     this.part.categoryId = category2.id;
                //                     console.log('category2', category2);
                //                 }
                //             })
                //         });
                //         break;
                //     default:
                if (defaultField.fieldName === "weeklyDemand") {
                  if (isNaN(defaultField.defaultInputValue)) {
                    return;
                  }
                }
                if (
                  defaultField.defaultInput &&
                  defaultField.defaultInputValue
                ) {
                  this.part[defaultField.fieldName] =
                    defaultField.defaultInputValue;
                }
                if (this.part.size) {
                  const sizes = this.part.size.split("x");
                  if (sizes.length >= 3) {
                    this.part.sizeWidth = sizes[0].trim();
                    this.part.sizeDepth = sizes[1].trim();
                    this.part.sizeHeight = sizes[2].trim();

                    this.part.sizeWidth = isNaN(this.part.sizeWidth)
                      ? ""
                      : this.part.sizeWidth;
                    this.part.sizeDepth = isNaN(this.part.sizeDepth)
                      ? ""
                      : this.part.sizeDepth;
                    this.part.sizeHeight = isNaN(this.part.sizeHeight)
                      ? ""
                      : this.part.sizeHeight;
                  }
                }
                //         break;
                // }
              });
            }
*/
            //fix lost required
            if (
              this.checkDeleteTooling != null &&
              Object.entries(this.checkDeleteTooling).length > 0
            ) {
              setTimeout(function () {
                Common.initRequireBadge();
              }, 100);
            }
          }
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    //thirdFiles
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
      if (this.thirdFiles.length == 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteFileStorage: function (file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case this.fileTypes.PART_PICTURE:
              this.partPictureFiles = response.data;
              break;
          }
        });
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = this.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = this.partPictureFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          this.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    formData: function () {
      var formData = new FormData();
      /*
                    for (var i = 0; i < this.files.length; i++) {
                        let file = this.files[i];
                        formData.append("files", file);
                    }
                    for (var i = 0; i < this.secondFiles.length; i++) {
                        let file = this.secondFiles[i];
                        formData.append("secondFiles", file);
                    }
*/
      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      formData.append("payload", JSON.stringify(this.part));
      return formData;
    },
    onMountedRun() {
      const self = this;
      this.$nextTick(function () {
        // 부모 카테고리 조회
        axios.get("/api/categories?size=1000").then(function (response) {
          self.categories = response.data.content;
          console.log("Category: ", self.categories);
          // 데이터 조회
          console.log("Current Part: ", self.part);
          axios.get(`/api/parts/${self.part.id}`).then(function (response) {
            self.part = response.data;
            if (response.data.size) {
              const sizes = response.data.size.split("x");
              if (sizes.length >= 3) {
                self.part.sizeWidth = sizes[0].trim();
                self.part.sizeDepth = sizes[1].trim();
                self.part.sizeHeight = sizes[2].trim();
              }
              // this.part.WLH = response.data.size
              if (self.part.partSizeNoUnit != null)
                self.part.size = self.part.partSizeNoUnit;
            }
            //file
            var param = {
              storageTypes: "PART_PICTURE",
              refId: self.part.id,
            };
            axios
              .get("/api/file-storage/mold?" + Common.param(param))
              .then(function (response) {
                console.log("response: ", response);
                self.partPictureFiles = response.data["PART_PICTURE"]
                  ? response.data["PART_PICTURE"]
                  : [];
              });
          });
          Common.getCategoryConfigStatus().then((data) => {
            if (data && data.length > 0) {
              let currentConfig = data.filter(
                (item) => item.configCategory === self.configCategory
              )[0];
              if (currentConfig && currentConfig.enabled) {
                self.initDefaultValue();
              }
            }
          });
        });

        axios.get("/api/codes").then(function (response) {
          self.codes = response.data;
        });
        setTimeout(function () {
          Common.initRequireBadge();
        }, 100);
      });
    },
  },
  computed: {},
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
