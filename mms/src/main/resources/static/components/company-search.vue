<template>
  <div
    id="op-company-search"
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
            v-text="resources['company_search']"
          ></h5>
          <button
            type="button"
            class="close"
            @click="closeModal"
            aria-label="Close"
          >
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div>
            <common-searchbar
              :request-param="requestParam"
              :on-search="search"
              placeholder="Search by company name or company ID"
            ></common-searchbar>

            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{
                    active:
                      requestParam.status == 'active' ||
                      requestParam.status == '' ||
                      requestParam.status == null ||
                      userType !== 'OEM',
                  }"
                  href="#"
                  @click.prevent="tab('active')"
                >
                  <span v-text="resources['all']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'active'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li
                class="nav-item dropdown"
                v-for="code in codes.CompanyType"
                :key="code.code"
                v-if="userType == 'OEM' && code.enabled"
              >
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == code.code }"
                  href="#"
                  @click.prevent="tab(code.code)"
                  >{{ code.title }}
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == code.code"
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
                <col style="width: 70px" />
              </colgroup>
              <thead>
                <tr>
                  <th
                    class="text-left align-middle"
                    v-text="resources['company_name']"
                  ></th>
                  <th
                    v-text="resources['company_code']"
                    class="align-middle"
                  ></th>
                  <th
                    v-text="resources['company_type']"
                    class="align-middle"
                  ></th>
                  <th v-text="resources['select']" class="align-middle"></th>
                </tr>
              </thead>
              <tbody class="op-list" style="display: none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                  <td class="text-left">
                    <div class="text-truncate">{{ result.name }}</div>
                  </td>
                  <td>
                    <div class="text-truncate">{{ result.companyCode }}</div>
                  </td>
                  <td>
                    <div class="text-truncate">
                      {{ result.companyTypeText }}
                    </div>
                  </td>
                  <td>
                    <button
                      type="button"
                      class="btn btn-default"
                      @click="selectCompany(result)"
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
      searchType: "",
      valueType: "",
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
      },
      cancelToken: undefined,
    };
  },
  computed: {
    userType() {
      return headerVm?.userType;
    },
    codes() {
      return headerVm?.systemCodes || {};
    },
  },
  methods: {
    findCompany: function (searchType, valueType) {
      this.searchType = searchType;
      this.valueType = valueType;
      if (this.userType == "OEM") {
        this.requestParam.status = status;
      } else {
        this.requestParam.status = this.searchType;
      }
      $(this.getRootToCurrentId()).modal("show");
      console.log("this.requestParam.status", this.requestParam.status);
      this.paging(1);
    },
    tab: function (status) {
      if (this.userType == "OEM") {
        this.requestParam.status = status;
      } else {
        this.requestParam.status = this.searchType;
      }

      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },
    selectCompany: function (company) {
      console.log(this.parentName, "this.parentName");
      if (this.parentName == "modal") {
        this.$emit("setcompany", {
          company: company,
          searchType: this.searchType,
          valueType: this.valueType,
        });
      } else {
        vm.callbackCompany(company, this.searchType, this.valueType);
      }
      this.closeModal();
    },
    paging: function (pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(this.requestParam);
      var self = this;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/companies?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
          // Duc fix
          Common.handleNoResults(
            self.getRootToCurrentId(),
            self.results.length
          );
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    closeModal() {
      $(this.getRootToCurrentId()).modal("hide");
    },
  },
  mounted() {
    this.paging(1);
  },
};
</script>
<style scoped>
.text-truncate {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  max-width: 150px;
}
</style>
