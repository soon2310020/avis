<template>
  <div
    id="op-counter-search"
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
            v-text="resources['search_available_counter']"
          ></h5>
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
          <div>
            <div class="input-group mb-3">
              <div class="input-group-prepend">
                <div class="input-group-text"><i class="fa fa-search"></i></div>
              </div>
              <input
                type="text"
                @input="requestParam.query = $event.target.value"
                @keyup="search"
                class="form-control"
                :placeholder="resources['search_by_displayed_cloumn']"
              />
            </div>

            <ul class="nav nav-tabs" style="margin-bottom: -1px">
              <li class="nav-item">
                <a
                  class="nav-link"
                  :class="{ active: requestParam.status == 'all' }"
                  href="#"
                  @click.prevent="tab('all')"
                  ><span v-text="resources['all']"></span>
                  <span
                    class="badge badge-light badge-pill"
                    v-if="requestParam.status == 'all'"
                    >{{ total }}</span
                  >
                </a>
              </li>
              <li
                class="nav-item dropdown"
                v-for="code in codes.CompanyType"
                v-if="code.enabled"
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

                <col style="width: 130px" />
              </colgroup>
              <thead>
                <tr>
                  <th class="text-left" v-text="resources['counter_id']"></th>
                  <th v-text="resources['company']"></th>
                  <th v-text="resources['op']"></th>
                  <th v-text="resources['select']"></th>
                </tr>
              </thead>
              <tbody class="op-list" style="display: none">
                <tr v-for="(result, index, id) in results" :id="result.id">
                  <td class="text-left">
                    <div>
                      {{ result.equipmentCode }}
                    </div>
                  </td>
                  <td>
                    <div>
                      {{ result.companyName }}
                    </div>
                    <!--
                      회사정보
                      <div v-if="result.location != null && result.location.company != null">
                      <div class="small text-muted">{{result.location.company.companyTypeText}}</div>
                      {{result.location.company.name}}
                  </div>-->
                  </td>
                  <td>
                    <div>
                      <span
                        v-if="result.operatingStatus === 'WORKING'"
                        class="op-active label label-success titleCase"
                      ></span
                      ><!-- && (result.equipmentStatus == 'INSTALLED' || result.equipmentStatus == 'CHECK' )-->
                      <span
                        v-if="result.operatingStatus === 'IDLE'"
                        class="op-active label label-warning"
                      ></span>
                      <span
                        v-if="result.operatingStatus === 'NOT_WORKING'"
                        class="op-active label label-inactive"
                      ></span>
                      <span
                        v-if="result.operatingStatus === 'DISCONNECTED'"
                        class="op-active label label-danger"
                      ></span>
                    </div>
                  </td>

                  <td>
                    <button
                      type="button"
                      class="btn btn-default"
                      data-dismiss="modal"
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
  },
  data() {
    return {
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "all",
        sort: "id,desc",
        page: 1,

        active: "",
        equipmentStatus: "",
        pageSearch: "NEW_TOOLING",
      },
      codes: {},
    };
  },
  methods: {
    tab: function (status) {
      this.requestParam.status = status;
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },
    select: function (counter) {
      console.log("counter");
      vm.callbackCounterTooling(counter);
    },
    paging: function (pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(this.requestParam);
      var self = this;
      axios
        .get("/api/counters?" + param)
        .then(function (response) {
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);

          Common.handleNoResults("#op-counter-search", self.results.length);
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal) {
          this.codes = Object.assign({}, this.codes, newVal);
          this.paging(1);
        }
      },
      { immediate: true }
    );
  },
};
</script>
