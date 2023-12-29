var Page = Common.getPage("parts");
const newPageUrl = Common.PAGE_URL;

window.onload = function () {
  document.title = "Part" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_part");
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
    "preview-images-system": httpVueLoader(
      "/components/common/preview-images-system.vue"
    ),
  },
  data: {
    project_name: "",
    showProductName: false,
    product_name: "Unit",
    productNameList: [],

    showWeightUnit: false,
    weight_unit: "Unit",
    weightUnitList: [],
    mode: Page.MODE,

    categories: [],
    resources: {},
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
      project: {
        name: "",
        id: null,
      },
      isSubmitting: false,
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
    listProjects: [],
    listCategories: [],
    showListProjects: false,
    isSubmitting: false,
  },
  created() {
    let url_string = window.location.href;
    const objectId = /[^/]*$/.exec(url_string)[0];
    this.isCreate = objectId === "new";
    console.log(this.isCreate, /[^/]*$/.exec(url_string)[0]);
    if (this.isCreate) {
      axios.get("/api/custom-field?objectType=PART").then(function (response) {
        // console.log(response.data, '/api/custom-field')
        // self.customFieldList = response.data;
        vm.customFieldList = response.data;
      });
    } else {
      axios
        .get(
          `/api/custom-field-value/list-by-object?objectType=PART&objectId=${objectId}`
        )
        .then(function (response) {
          // console.log(response.data, '/api/custom-field-value/list-by-object')
          vm.customFieldList = response.data.map((item) => ({
            fieldName: item.fieldName,
            defaultInputValue:
              item.customFieldValueDTOList.length !== 0
                ? item.customFieldValueDTOList[0].value
                : null,
            id: item.id,
            required: item.required,
          }));
        });
    }
  },
  methods: {
    cancel() {
      location.href = newPageUrl.PART;
    },
    handleTogleProductName(flag) {
      this.showProductName = flag;
    },
    closeProductName() {
      this.showProductName = false;
    },
    selectProductName(item) {
      this.part.sizeUnit = item.code;
      this.product_name = item.title;
      this.showProductName = false;
    },

    handleTogleWeightUnit(flag) {
      this.showWeightUnit = flag;
    },
    closeWeightUnit() {
      this.showWeightUnit = false;
    },
    selectWeightUnit(item) {
      this.part.weightUnit = item.code;
      this.weight_unit = item.title;
      this.showWeightUnit = false;
    },
    async fetchListProjects() {
      const size = 1000;
      const url = `/api/categories?enabled=true&query=&status=1&sort=sortOrder%2Casc&page=1&size=${size}`;
      try {
        const res = await axios.get(url);
        console.log("fetchListProjects", res);
        const categoriesHaveProjects = res.data.content.filter((item) =>
          item.children.some(
            (childItem) => childItem.children && childItem.children.length > 0
          )
        );
        this.listCategories = categoriesHaveProjects;
        let _listProjectsInCategory = [];
        categoriesHaveProjects.forEach((category) => {
          for (const brand of category.children) {
            if (brand.children && brand.children.length > 0) {
              const __listProduct = brand.children.map((item) => ({
                ...item,
                title: item.name,
                categoryName: category.name,
              }));
              _listProjectsInCategory =
                _listProjectsInCategory.concat(__listProduct);
            }
          }
        });
        this.listProjects = _listProjectsInCategory;
        console.log(this.listProjects, "-----------");
      } catch (err) {
        console.log("%c error", "background: red; color: white;", err.message);
      }
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
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

      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
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
      this.isSubmitting = true;
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
                location.href = newPageUrl.PART;
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
        })
        .finally(() => {
          this.isSubmitting = false;
        });
    },
    update: function () {
      vm.children = null;
      this.isSubmitting = true;
      axios
        .put(
          "/api/parts/edit-multipart" + "/" + Page.ID,
          this.formData(),
          this.multipartHeader()
        )
        .then(async function (response) {
          // console.log(response.data);

          await vm.updateCustomField();

          // if (response.data.success) {
          Common.alertCallback = function () {
            location.href = newPageUrl.PART;
          };
          Common.alert("success");
          // } else {
          //     Common.alert(response.data.message);
          // }
        })
        .catch(function (error) {
          console.log(error.response);
          Common.alert(error.response.data);
        })
        .finally(() => {
          this.isSubmitting = false;
        });
    },
    async updateCustomField() {
      let url_string = window.location.href;
      var objectId = /[^/]*$/.exec(url_string)[0];
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
            // console.log('form response data', response.data);
            response.data.forEach((field) => {
              if (field.required) {
                this.requiredFields.push(field.fieldName);
              }
              if (this.checkDeletePart != null)
                this.checkDeletePart[field.fieldName] = field.deletedField;
            });
            if (!Page.IS_EDIT) {
              fields.forEach((field) => {
                let defaultField = response.data.filter(
                  (item) => item.fieldName === field
                )[0];
                // console.log('defaultField', defaultField);
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
          console.log("this.part", this.part);
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    //thirdFiles
    deleteThirdFiles: function (index) {
      vm.thirdFiles = vm.thirdFiles.filter((value, idx) => idx !== index);
      if (vm.thirdFiles.length == 0) {
        this.$refs.fileupload.value = "";
      } else {
        this.$refs.fileupload.value = "1221";
      }
    },
    deleteFileStorage: function (file, type) {
      if (confirm("Are you sure you want to delete this file?"))
        axios.delete("/api/file-storage/" + file.id).then(function (response) {
          switch (type) {
            case vm.fileTypes.PART_PICTURE:
              vm.partPictureFiles = response.data;
              break;
          }
        });
    },

    selectedThirdFiles: function (e) {
      var files = e.target.files;
      var isExitsFile = vm.thirdFiles.filter(
        (item) => item.name === files[0].name
      );
      var isExitsOldFile = vm.partPictureFiles.filter(
        (item) => item.fileName === files[0].name
      );
      if (files && isExitsFile.length == 0 && isExitsOldFile.length == 0) {
        var selectedFiles = Array.from(files);
        for (var i = 0; i < selectedFiles.length; i++) {
          vm.thirdFiles.push(selectedFiles[i]);
        }
      }
    },
    formData: function () {
      var formData = new FormData();

      for (var i = 0; i < this.thirdFiles.length; i++) {
        let file = this.thirdFiles[i];
        formData.append("thirdFiles", file);
      }
      // Object.entries(obj).forEach(([key, value]) => alert(key + " : " + value));
      const { project, ..._part } = vm.part;
      _part.categoryId = project.id;
      _part.weight = isNaN(vm.part.weight) ? "" : Number(vm.part.weight);
      formData.append("payload", JSON.stringify(_part));
      return formData;
    },
    selectProject: function (value) {
      // const _selectedProject = JSON.parse(event.target.value)
      console.log("selectProject", value);
      // const _category = this.listCategories.filter(item => item.id === _selectedProject.parentId)[0]
      this.part.quantityRequired = 1;
      this.part.project = value;
      this.part.categoryName = value.categoryName;
      this.project_name = value.title;
      this.showListProjects = false;
    },
    handleClickProjectSelect(flag) {
      // event.stopPropagation()
      this.showListProjects = flag;
    },
    handleInputNumber(event) {
      if (/[\D\.]/.test(event.key)) {
        event.preventDefault();
      }
    },
  },
  computed: {
    readonlyPartCode: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },
    projectName() {
      return this.part?.project?.name || "";
    },
    showQuantityRequired() {
      return this.part?.project?.id;
    },
  },
  watch: {
    checkDeletePart(newVal) {
      console.log("checkDeletePart", newVal);
    },
    requiredFields(newVal) {
      console.log("requiredFields", newVal);
    },
    categories(newVal) {
      console.log("categories", newVal);
    },
    customFieldList(newVal) {
      console.log("customFieldList", newVal);
    },
  },
  mounted() {
    const self = this;
    console.log("%c track", "background: green; color: white");
    this.$nextTick(async function () {
      // 부모 카테고리 조회
      await axios
        .get("/api/categories?size=1000")
        .then(async function (response) {
          vm.categories = response.data.content;

          if (Page.IS_EDIT) {
            // 데이터 조회
            await axios.get(Page.API_GET).then(async function (response) {
              // const transform = (data) => ({
              //     ...data,

              // })
              // vm.part = transform(response.data);
              // console.log('response.data', response.data)
              // console.log('vm.part', vm.part)

              vm.part = {
                ...response.data,
                project: {
                  id: response.data.categoryId,
                  name: response.data.projectName,
                },
              };
              // vm.part.project.id = response.data.categoryId
              // vm.part.project.name = response.data.categoryName
              console.log("vm.part 1", vm.part);

              if (response.data.size) {
                const sizes = response.data.size.split("x");
                if (sizes.length >= 3) {
                  vm.part.sizeWidth = sizes[0].trim();
                  vm.part.sizeDepth = sizes[1].trim();
                  vm.part.sizeHeight = sizes[2].trim();
                }
              }
              //file
              var param = {
                storageTypes: "PART_PICTURE",
                refId: vm.part.id,
              };

              await axios
                .get("/api/file-storage/mold?" + Common.param(param))
                .then(function (response) {
                  console.log("response: ", response);
                  vm.partPictureFiles = response.data["PART_PICTURE"]
                    ? response.data["PART_PICTURE"]
                    : [];
                });
            });
          }
          await Common.getCategoryConfigStatus().then((data) => {
            if (data && data.length > 0) {
              let currentConfig = data.filter(
                (item) => item.configCategory === vm.configCategory
              )[0];
              if (currentConfig && currentConfig.enabled) {
                setTimeout(function () {
                  vm.initDefaultValue();
                }, 100);
              }
            }
          });
        });

      await axios.get("/api/codes").then(function (response) {
        vm.codes = response.data;
        // vm.productNameList=vm.codes.SizeUnit.filter((item)=>item.code == 'MM' || item.code == 'CM' || item.code == 'M')
        vm.productNameList = vm.codes.SizeUnit.filter(
          (item) => item.code !== "INCH"
        );
        // vm.productNameList=vm.productNameList.map((item)=>)
        vm.weightUnitList = vm.codes.WeightUnit;
        // vm.part.WLH = response.data.size

        vm.product_name = vm.codes.SizeUnit.find(
          (item) => item.code == vm.part.sizeUnit
        ).title;
        vm.weight_unit = vm.codes.WeightUnit.find(
          (item) => item.code == vm.part.weightUnit
        ).title;
        vm.project_name = vm.part.projectName;
        console.log(this.project_name, "---------------");
      });
      vm.getResources();
      this.fetchListProjects();
      console.log("end of ====", vm.project_name);
    });
  },
});
