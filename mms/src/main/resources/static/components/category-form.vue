<template>
  <div
    style="overflow: scroll"
    id="op-category-form"
    class="modal fade"
    tabindex="-1"
    :key="totalKey"
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
                    <strong v-text="resources['category']"></strong>
                  </div>
                  <div class="card-body">
                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="name"
                        v-text="resources['name']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="name"
                          v-model="category.name"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              category.name == '' || category.name == null,
                          }"
                          :placeholder="resources['name']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label
                        class="col-md-2 col-form-label"
                        for="description"
                        v-text="resources['description']"
                      >
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <input
                          type="text"
                          id="description"
                          v-model="category.description"
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              category.description == '' ||
                              category.description == null,
                          }"
                          :placeholder="resources['description']"
                          required
                        />
                      </div>
                    </div>

                    <div class="form-group row">
                      <label class="col-md-2 col-form-label" for="name"
                        ><span v-text="resources['parent_category']"></span>
                        <span class="avatar-status badge-danger"></span
                      ></label>
                      <div class="col-md-10">
                        <select
                          class="form-control"
                          :class="{
                            'form-control-warning':
                              category.parentId == '' ||
                              category.parentId == null,
                          }"
                          v-model="category.parentId"
                        >
                          <option value="" :checked="category.level == 1">
                            <span v-text="resources['uncategorized']"></span>
                          </option>

                          <template v-for="category1 in parentCategories">
                            <option
                              v-if="category.children != null"
                              :key="category1.id"
                              :value="category1.id"
                              :disabled="
                                category.id == category1.id ||
                                category.children.length > 0
                              "
                            >
                              &nbsp; {{ category1.name }}
                            </option>

                            <template v-for="category2 in category1.children">
                              <option
                                :key="category2.id"
                                :value="category2.id"
                                :disabled="true"
                              >
                                &nbsp; | &nbsp; &nbsp; ㄴ {{ category2.name }}
                              </option>
                              <!--<option :value="category2.id" :disabled="category.id == category2.id || (category.level==2 && category.children.length > 0)">
                                                    &nbsp; | &nbsp; &nbsp; ㄴ {{ category2.name }}  {{category.id}}- {{category2.id}}
                                                </option>

                                                <template v-for="category3 in category2.children">
                                                    <option :value="category3.id" disabled="true">
                                                        &nbsp; | &nbsp; &nbsp; | &nbsp; &nbsp; ㄴ {{ category3.name }}
                                                    </option>


                                                </template>-->
                            </template>
                          </template>
                        </select>
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
                              v-model="category.enabled"
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
                              v-model="category.enabled"
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
                    <button type="submit" class="btn btn-primary">
                      <span v-text="resources['save_changes']"></span>
                    </button>
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
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    updated: Function,
  },
  data() {
    return {
      // mode: Page.MODE,
      parentCategories: [],
      category: {
        name: "",
        description: "",
        parentId: "",
        enabled: true,
        children: [],
      },
      totalKey: 0,
      isTable: '',
    };
  },
  methods: {
    submit: function () {
      this.update();
    },

    // create: function () {
    //   axios
    //     .post(Page.API_POST, this.category)
    //     .then(function (response) {
    //       console.log(response.data);

    //       if (response.data.success) {
    //         Common.alertCallback = function () {
    //           location.href = Page.LIST_PAGE;
    //         };
    //         Common.confirm("success", "다른 카테고리도 등록하시겠습니까?");
    //       } else {
    //         Common.alert(response.data.message);
    //       }
    //     })
    //     .catch(function (error) {
    //       console.log(error.response);
    //     });
    // },

    update() {
      this.children = null;
      let self = this;
      axios
        .put(`/api/categories/${this.category.id}`, this.category)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            console.log('isTable', self.isTable)
            if (self.isTable == 'table') {
              Common.alertCallback = function () {
                // location.href = Page.LIST_PAGE;
                self.dimissModal();
                self.updated();
              };
              Common.alert("success");
            } else {
              self.dimissModal();
              self.updated();
            }
            
            
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    onMountedRun() {
      let self = this;
      self.$nextTick(function () {
        // 부모 카테고리 조회\
        axios.get("/api/categories" + "?size=1000").then(function (response) {
          self.parentCategories = response.data.content;

          console.log(`Current Category: `, self.category);
          // 데이터 조회
          axios
            .get(`/api/categories/${self.category.id}`)
            .then(function (response) {
              self.category = response.data;
              console.log("After API ", self.category);
              setTimeout(function () {
                Common.initRequireBadge();
              }, 100);
            });
        });
      });
    },
    showForm: function (category, page) {
      console.log("Category: ", category);
      this.isTable = page;
      this.category = category;
      this.onMountedRun();
      $(this.getRootId() + "#op-category-form").modal("show");
    },

    // getPic(file) {
    //     return "http://49.247.200.147" + file.saveLocation;
    // },

    dimissModal: function () {
      // this.updated();
      $(this.getRootId() + "#op-category-form").modal("hide");
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
.form-control:invalid, .form-control:valid, .was-validated {
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
  background: #F5F8FF;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 19.5px 26px 11.5px 26px;
}
.modal-header .span-title {
  color: #4B4B4B;
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
  display: grid;
  align-content: space-between;
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
.modal-content{
  width: 101% !important;
  min-height: 680px;

}
</style>