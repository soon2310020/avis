<template>
  <div>
    <div class="row">
      <div class="col-md-12">
        <div v-show="isLoading" class="loading-wave" style="height: 500px; margin-bottom: 16px;"></div>
        <div v-show="!isLoading" class="card custom-card">
          <div class="card-header">
            <strong v-text="resources['part']"></strong>
          </div>
          <div class="card-body">
            <div class="form-group row" v-if="!checkDeletePart['category']">
              <label class="col-md-2 col-form-label" for="name">
                <span v-text="resources['category']"></span>
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <select class="form-control" :class="{
                  'form-control-warning':
                    part.categoryId == '' ||
                    part.categoryId == null,
                }" v-model="part.categoryId" required>
                  <option value="" v-text="resources['category']"></option>
                  <template v-for="(category1, index1) in categories">
                    <optgroup :label="getTruncateText(category1.name)" :key="index1" style="max-width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                      <template v-for="(category2, index2) in category1.children">
                        <option :key="index2" :value="category2.id" :disabled="!category2.enabled">
                          {{ getTruncateText(category2.name) }}
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
                <span class="avatar-status badge-danger"></span></label>
              <div class="col-md-10">
                <input type="text" id="partCode" v-model="part.partCode" class="form-control" :class="{
                  'form-control-warning':
                    part.partCode == '' ||
                    part.partCode == null,
                }" :placeholder="resources['part_id']" required />
              </div>
            </div>
            <div class="form-group row">
              <label class="col-md-2 col-form-label" for="name">
                <span>{{ resources['part_name'] }}</span>
                <span class="avatar-status badge-danger"></span>
              </label>
              <div class="col-md-10">
                <input type="text" id="name" v-model="part.name" class="form-control" :class="{
                  'form-control-warning':
                    part.name == '' ||
                    part.name == null,
                }" :placeholder="resources['part_name']" required />
              </div>
            </div>
            <div class="form-group row" v-if="!checkDeletePart['resinCode']">
              <label class="col-md-2 col-form-label" for="resinCode"><span v-text="resources['part_resin_code']"></span>
                <span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('resinCode')"></span>
              </label>
              <div class="col-md-10">
                <input type="text" id="resinCode" v-model="part.resinCode" class="form-control" :class="{
                  'form-control-warning':
                    part.resinCode == '' ||
                    part.resinCode == null,
                }" :placeholder="resources['part_resin_code']" :required="isRequiredField('resinCode')" />
              </div>
            </div>
            <div class="form-group row" v-if="!checkDeletePart['resinGrade']">
              <label class="col-md-2 col-form-label" for="resinCode"><span
                  v-text="resources['part_resin_grade']"></span>
                <span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('resinGrade')"></span></label>
              <div class="col-md-10">
                <input type="text" id="resinGrade" v-model="part.resinGrade" class="form-control" :class="{
                  'form-control-warning':
                    part.resinGrade == '' ||
                    part.resinGrade == null,
                }" :placeholder="resources['part_resin_grade']" :required="isRequiredField('resinGrade')" />

              </div>
            </div>
            <div class="form-group row" v-if="!checkDeletePart['designRevision']">
              <label class="col-md-2 col-form-label" for="designRevision"><span
                  v-text="resources['design_revision_level']"></span><span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('designRevision')"></span>
              </label>
              <div class="col-md-10">
                <input type="text" id="designRevision" v-model="part.designRevision" class="form-control" :class="{
                  'form-control-warning':
                    part.designRevision == '' ||
                    part.designRevision == null,
                }" :placeholder="resources['design_revision_level']" :required="isRequiredField('designRevision')" />
              </div>
            </div>
            <div class="form-group row" v-if="!checkDeletePart['size']">
              <label class="col-md-2 col-form-label" for="size"><span
                  v-text="resources['part_volume'] + ' (W x D x H)'"></span>
                <span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('size')"></span>
              </label>
              <div class="col-md-7">
                <input type="text" id="size" v-model="part.size" class="form-control" :class="{
                  'form-control-warning':
                    part.size == '' ||
                    part.size == null,
                }" :placeholder="resources['part_volume'] + ' (W x D x H)'" :required="isRequiredField('size')" />
              </div>

              <div class="col-md-3">
                <select class="form-control" :class="{
                  'form-control-warning':
                    part.sizeUnit == '' ||
                    part.sizeUnit == null,
                }" v-model="part.sizeUnit">
                  <option hidden selected value="" v-text="resources['unit']"></option>
                  <template v-for="(code, index) in codes.SizeUnit">
                    <option v-if="
                      code.code == 'MM' ||
                      code.code == 'CM' ||
                      code.code == 'M'
                    " :key="index" :value="code.code">
                      {{ code.title }}³
                    </option>
                    <option v-else :key="index" :value="code.code">
                      {{ code.title }}
                    </option>
                  </template>
                </select>
              </div>
            </div>

            <div class="form-group row" v-if="!checkDeletePart['weight']">
              <label class="col-md-2 col-form-label" for="weight">
                <span v-text="resources['part_weight']"></span>
                <span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('weight')"></span>
              </label>
              <div class="col-md-7">
                <input type="number" min="0.0" id="weight" v-model="part.weight" class="form-control" :class="{
                  'form-control-warning':
                    part.weight == '' ||
                    part.weight == null,
                }" step="any" :placeholder="resources['part_weight']" :required="isRequiredField('weight')" />
              </div>
              <div class="col-md-3">
                <select class="form-control" :class="{
                  'form-control-warning':
                    part.weightUnit == '' ||
                    part.weightUnit == null,
                }" v-model="part.weightUnit">
                  <option hidden selected value="" v-text="resources['unit']"></option>
                  <option v-for="(code, index) in codes.WeightUnit" :key="index" :value="code.code">
                    {{ code.title }}
                  </option>
                </select>
              </div>
            </div>

            <div class="form-group row" v-if="!checkDeletePart['weeklyDemand']">
              <label class="col-md-2 col-form-label" for="designRevision"><span
                  v-text="resources['weekly_demand']"></span><span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('weeklyDemand')"></span>
              </label>
              <div class="col-md-10">
                <input type="number" id="weeklyDemand" v-model="part.weeklyDemand" min="0" max="99999999"
                  class="form-control" :class="{
                    'form-control-warning':
                      part.weeklyDemand == '' ||
                      part.weeklyDemand == null,
                  }" placeholder="Weekly Demand" :required="isRequiredField('weeklyDemand')" />
              </div>
            </div>

            <div class="form-group row" v-if="!checkDeletePart['partPictureFile']">
              <label class="col-md-2 col-form-label" for="partPictureFile"><span
                  v-text="resources['part_picture']"></span><span class="avatar-status badge-danger"></span>
                <span class="badge-require" v-if="isRequiredField('partPictureFile')"></span>
              </label>
              <div class="col-md-10">
                <div class="op-upload-button-wrap">
                  <button id="partPictureFile" class="btn btn-outline-success"
                    v-text="resources['upload_photo']"></button>
                  <input type="file" ref="fileupload" id="files1" @change="selectedThirdFiles" multiple
                    style="height: 40px; width: 100%"
                    accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
                    :required="isRequiredField('partPictureFile', true)" />
                </div>

                <div>
                  <button v-for="(file, index) in thirdFiles" class="btn btn-outline-dark btn-sm mr-1"
                    @click.prevent="deleteThirdFiles(index)" :key="index">
                    {{ file.name }}
                  </button>
                </div>

                <div class="mt-1">
                  <button v-for="(file, index) in partPictureFiles" :key="index"
                    class="btn btn-outline-dark btn-sm mr-1" @click.prevent="
                      deleteFileStorage(file, fileTypes.PART_PICTURE)
                    ">
                    {{ file.fileName }}
                  </button>
                </div>
              </div>
            </div>

            <div class="form-group row">
              <label class="col-md-2 col-form-label" v-text="resources['enable']"></label>
              <div class="col-md-10 col-form-label">
                <div class="form-check form-check-inline mr-3">
                  <label class="form-check-label">
                    <input type="radio" v-model="part.enabled" class="form-check-input" value="true" name="enabled" />
                    <span v-text="resources['enable']"></span>
                  </label>
                </div>
                <div class="form-check form-check-inline">
                  <label class="form-check-label">
                    <input type="radio" v-model="part.enabled" class="form-check-input" value="false" name="disabled" />
                    <span v-text="resources['disable']"></span>
                  </label>
                </div>
              </div>
            </div>

            <div v-for="(item, index) in customFieldList" :key="index" class="form-group row">
              <label class="col-md-2 col-form-label" :for="'customFieldList' + index">
                {{ item.fieldName }}
                <span class="avatar-status"></span>
                <span class="badge-require" v-if="item.required"></span>
              </label>
              <div class="col-md-10">
                <input :id="'customFieldList' + index" type="text" v-model="item.defaultInputValue" class="form-control"
                  :class="{
                    'form-control-warning':
                      item.defaultInputValue == '' ||
                      item.defaultInputValue == null,
                  }" :placeholder="item.fieldName" :required="item.required" />
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
    part: {
      type: Object,
      default: () => ({})
    },
    isLoading: {
      type: Boolean,
      default: () => (true)
    },
    customFieldList: {
      type: Array,
      default: () => ([])
    },
    thirdFiles: {
      type: Array,
      default: () => ([])
    },
    partPictureFiles: {
      type: Array,
      default: () => ([])
    },
    categories: {
      type: Array,
      default: () => ([])
    }
  },
  data() {
    return {
      requiredFields: [],
      checkDeletePart: {},
    }
  },
  computed: {

  },
  methods: {
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
    async initDefaultValue() {
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
      const configCategory = 'PART'
      try {
        const response = await axios.get("/api/config?configCategory=" + configCategory)
        if (response.data && response.data.length > 0) {
          console.log("form response data", response.data);
          response.data.forEach((field) => {
            if (field.required) {
              this.requiredFields.push(field.fieldName);
            }
            if (this.checkDeletePart != null)
              this.checkDeletePart[field.fieldName] = field.deletedField;
          });
          if (
            this.checkDeleteTooling != null &&
            Object.entries(this.checkDeleteTooling).length > 0
          ) {
            setTimeout(function () {
              Common.initRequireBadge();
            }, 100);
          }
        }
      } catch (error) {
        console.log(error);
      }
    },
    //thirdFiles
    deleteThirdFiles: function (index) {
      this.thirdFiles = this.thirdFiles.filter((value, idx) => idx !== index);
      const fileupload = this.$refs.fileupload
      if (fileupload && this.thirdFiles.length == 0) {
        fileupload.value = "";
      } else {
        fileupload.value = "1221";
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

    getTruncateText(text) {
      if (text.length > 90) {
        return `${text.slice(0, 90)}...`
      }
      return text
    }
  },
}
</script>

<style>

</style>