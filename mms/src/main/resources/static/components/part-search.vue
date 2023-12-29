<template>
  <div
    id="op-part-search"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5
            class="modal-title"
            id="exampleModalLongTitle"
            v-text="resources['part_search']"
          ></h5>
          <button
            type="button"
            class="close"
            @click="closeModal()"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div>
            <div class="input-group mb-3">
              <div class="input-group-prepend">
                <div class="input-group-text">
                  <i class="fa fa-search"></i>
                </div>
              </div>
              <input
                type="text"
                @input="requestParam.query = $event.target.value"
                @keyup="search"
                class="form-control"
                placeholder="Search by name or part ID"
              />
            </div>

            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'active' }"
                  href="#"
                  @click.prevent="tab('active')"
                >
                  <span v-text="resources['active']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'active'"
                    >{{ total }}</span
                  >
                </a>
              </li>
            </ul>

            <table class="table table-responsive-sm table-striped">
              <colgroup>
                <col />
                <col />
                <col />
                <col style="width: 130px" />
              </colgroup>
              <thead>
                <tr>
                  <th v-text="resources['part_id']"></th>
                  <th v-text="resources['part_name']"></th>
                  <th v-text="resources['category']"></th>
                  <th v-text="resources['edit']"></th>
                </tr>
              </thead>
              <tbody class="op-list" style="display: none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                  <td>
                    <div v-if="result.partCode && result.partCode.length > 17">
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ result.partCode }}
                          </div>
                        </template>
                        {{ truncateText(result.partCode, 17) }}
                      </a-tooltip>
                    </div>
                    <div v-else>
                      {{ result.partCode }}
                    </div>
                  </td>
                  <td class="text-left">
                    <div v-if="result.name && result.name.length > 17">
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px; font-size: 13px">
                            {{ result.name }}
                          </div>
                        </template>
                        <div>{{ truncateText(result.name, 17) }}</div>
                      </a-tooltip>
                    </div>
                    <div v-else>
                      {{ result.name }}
                    </div>
                  </td>
                  <td>
                    {{
                      result.categoryName || categoryLocation(result.categoryId)
                    }}
                  </td>
                  <td>
                    <button
                      type="button"
                      class="btn btn-default"
                      @click="select(result)"
                      v-text="resources['select']"
                    ></button>
                  </td>
                </tr>
              </tbody>
            </table>

            <div
              class="no-results"
              :class="{ 'd-none': total > 0 }"
              v-text="resources['no_results']"
            ></div>

            <div class="row">
              <div class="col-md-12">
                <ul class="pagination">
                  <li
                    v-for="(data, index) in pagination"
                    class="page-item"
                    :class="{ active: data.isActive }"
                  >
                    <a class="page-link" @click="paging(data.pageNumber)">{{
                      data.text
                    }}</a>
                  </li>
                </ul>
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
    resources: Object,
    parentName: String,
  },
  data() {
    return {
      index: 0,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        categoryId: "",
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
      },
      // categories: []
    };
  },
  computed: {
    categories() {
      return headerVm?.listCategories || [];
    },
  },
  methods: {
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    findPart: function (index) {
      this.index = index;
      $(this.getRootToCurrentId()).modal("show");
      this.paging(1);
    },
    category: function () {
      this.paging(1);
    },

    tab: function (status) {
      this.requestParam.status = status;
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },
    select: function (part) {
      if (this.parentName == "modal") {
        this.$emit("setpart", { index: this.index, part: part });
        this.closeModal();
      } else {
        var result = vm.callbackPart(this.index, part);
        if (result) {
          this.closeModal();
        }
      }
    },
    closeModal() {
      $(this.getRootToCurrentId()).modal("hide");
    },
    paging: function (pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(this.requestParam);
      var self = this;
      axios
        .get("/api/parts?" + param)
        .then(function (response) {
          console.log("part-search: ", response);
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);

          Common.handleNoResults(
            self.getRootToCurrentId(),
            self.results.length
          );
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    categoryLocation: function (categoryId) {
      var location = "";
      for (var i = 0; i < this.categories.length; i++) {
        var parent = this.categories[i];
        for (var j = 0; j < parent.children.length; j++) {
          var category = parent.children[j];

          if (category.id == categoryId) {
            return parent.name + " > " + category.name;
          }
        }
      }
      return "";
    },
  },
  async mounted() {
    this.paging(1);
  },
};
</script>
