<template>
  <div>
    <button class="btn btn-primary" @click.prevent="showPopup()">
      Show CodeList
    </button>
    <div
      id="op-large-code"
      aria-hidden="true"
      aria-labelledby="title-part-chart"
      class="modal fade"
      role="dialog"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-title">
            <div class="head-line"></div>
            <div>
              <span class="span-title">Code Selector</span>
            </div>
            <span aria-label="Close" class="close-button" data-dismiss="modal">
              <span class="t-icon-close"></span>
            </span>
          </div>

          <!-- tabs -->
          <div class="modal-body">
            <div>
              <div class="switch-zone tab">
                <!-- todo: handle state change activity -->
                <div
                  v-for="(tab, index) in allCodeTypes"
                  :key="index"
                  :id="tab.title"
                  :class="{ active: tab.code === activeCode }"
                  @click.prevent="activeTab(tab.code)"
                >
                  {{ CapitalizeTheFirst(tab.code) }}
                </div>
              </div>
            </div>

            <!-- Search -->
            <div class="col-md-12 mt-4 search">
              <div class="input-group">
                <div class="input-group-prepend">
                  <div class="input-group-text">
                    <i class="fa fa-search"></i>
                  </div>
                </div>

                <input
                  type="text"
                  class="form-control"
                  v-model="searchKey"
                  @keyup.enter="searchBy"
                  @keyup.up="searchBy"
                  :placeholder="resources['search_by_code_details']"
                />
              </div>
            </div>

            <!-- table -->
            <div class="table-border-light">
              <table>
                <thead>
                  <tr>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.CODE },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortBy(sortType.CODE)"
                    >
                      <span>Reason</span>
                      <span class="fa fa-sort"></span>
                    </th>
                    <th
                      class="text-left __sort"
                      v-bind:class="[
                        { active: currentSortType === sortType.GROUP },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortBy(sortType.GROUP)"
                    >
                      <span>Planned / Unplanned</span>
                      <span class="fa fa-sort"></span>
                    </th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(reason, index) in allCodeReason" :key="index">
                    <td>{{ reason.title }}</td>
                    <td>{{ CapitalizeTheFirst(reason.group1Code) }}</td>
                    <td class="text-right">
                      <button class="btn-secondary-select">Select</button>
                    </td>
                  </tr>
                </tbody>
              </table>
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
      allCodeTypes: [],
      allCodeReason: [],
      activeCode: "ALL",
      searchKey: "",
      isDesc: true,
      sortType: {
        CODE: "code",
        GROUP: "group1Code",
      },
      currentSortType: "code",
      sortQuery: "",
    };
  },
  methods: {
    // show code list
    showPopup() {
      $("#op-large-code").modal("show");
    },

    CapitalizeTheFirst(str) {
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    },

    searchBy: function () {
      if (this.activeCode === "ALL") {
        return axios
          .get(
            `/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?query=${this.searchKey}&size=500`
          )
          .then((response) => {
            this.allCodeReason = response.data.content;
          })
          .catch(function (error) {
            return [];
          });
      } else {
        return axios
          .get(
            `/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?query=${this.searchKey}&size=500&group2Code=&${this.activeCode}`
          )
          .then((response) => {
            this.allCodeReason = response.data.content;
          })
          .catch(function (error) {
            return [];
          });
      }
    },

    sortBy: function (type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        this.sortQuery = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort(this.sortQuery);
      }
    },

    sort: function (query) {
      return axios
        .get(
          `/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&sort=${query}`
        )
        .then((response) => {
          this.allCodeReason = response.data.content;
        })
        .catch((error) => {
          this.allCodeReason = [];
        });
    },

    // get all tabs list
    getCodeTypesList() {
      return axios
        .get(
          "/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON_GROUP2/data"
        )
        .then(function (response) {
          // add All
          response.data.content.unshift({
            code: "ALL",
            title: "ALL",
            description: null,
            group1Code: null,
            group1Title: null,
            group2Code: null,
            group2Title: null,
          });
          return response.data.content;
        })
        .catch(function (error) {
          // todo: empty case, return []
          return [];
        });
    },

    // get all tabs
    getCodeReasonList() {
      // api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500
      return axios
        .get(
          "/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500"
        )
        .then(function (response) {
          return response.data.content;
        })
        .catch(function (error) {
          return [];
        });
    },

    // get all tabs
    activeTab(codeType) {
      // select active tab
      this.activeCode = codeType;

      if (codeType === "ALL") {
        this.getCodeReasonList().then(
          (result) => (this.allCodeReason = result)
        );
      } else {
        this.getReasonListByType(codeType).then(
          (result) => (this.allCodeReason = result)
        );
      }
    },

    getReasonListByType(codeType) {
      return axios
        .get(
          `/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group2Code=${codeType}`
        )
        .then(function (response) {
          return response.data.content;
        })
        .catch(function (error) {
          return [];
        });
    },
  },

  // mounted
  mounted() {
    this.getCodeTypesList().then((result) => (this.allCodeTypes = result));
    this.getCodeReasonList().then((result) => (this.allCodeReason = result));
  },
};
</script>

<style scoped>
table {
  border-collapse: collapse;
  border-radius: 0.5em;
  overflow: hidden;
  width: 100%;
}

th {
  background: #f5f8ff !important;
  font-size: 16px !important;
  color: #595959 !important;
  height: 51px !important;
}

td {
  background: #ffffff !important;
}

th,
td {
  padding: 0.5em 0.5em 0.5em 40px;
  background: #ddd;
  border-bottom: 2px solid white;
}

.fa {
  color: #595959;
  cursor: pointer;
}

.fa-search {
  color: #9d9d9d;
}

.table-border-light {
  border: 1px solid #d6dade;
  margin-top: 22px;
  border-radius: 0.5em;
  max-height: 600px;
  overflow: scroll;
}

.table-border-light tbody > tr:nth-of-type(2n) > td {
  background-color: #fbfcfd !important;
}

.btn-secondary-select {
  background: rgb(52, 145, 255);
  color: rgb(255, 255, 255);
  border-radius: 3px;
  border: none;
  margin-right: 5rem;
  height: 28px;
  width: 55px;
  font-size: 14px;
}

.btn-secondary-select:hover {
  background: #3585e5;
}

.btn-secondary-select:focus {
  box-shadow: unset;
}

.modal-title {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 31px;
  border-radius: 6px 6px 0 0;
}

.modal-body {
  background: white;
  overflow: auto;
  height: fit-content !important;
  padding: 20px 56px;
  overflow: scroll;
  border-radius: 0.5em;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  top: 0;
  width: 100%;
  left: 0;
}

.modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}

.switch-zone {
  display: inline-flex;
  align-items: center;
}

.switch-zone div {
  cursor: pointer;
  width: fit-content;
  height: 26px;
  background: #fff;
  /*border-radius: 3px;*/
  border: 1px solid #d0d0d0;
  font-size: 14px;
  color: #888888;
  margin-right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px 20px 5px 20px;
}

.switch-zone div:first-child {
  border-top-left-radius: 3px;
  border-bottom-left-radius: 3px;
}
.switch-zone div:last-child {
  border-top-right-radius: 3px;
  border-bottom-right-radius: 3px;
}

.search {
  margin-top: 10px;
  padding: 0;
  font-size: 16px;
}

.search-font {
  font-size: 16px;
}

.switch-zone div:not(.active):hover {
  background: #deedff;
  border-color: #3491ff;
  color: #3491ff;
}

.switch-zone div.active {
  background: #deedff;
  color: #3491ff;
  border-color: #3491ff;
}

.close-button {
  font-size: 25px;
  display: flex;
  align-items: center;
  height: 17px;
  cursor: pointer;
}

.modal-content {
  flex-direction: column;
  width: 100%;
  pointer-events: auto;
  background-color: #fff;
  background-clip: padding-box;
  /* border: 1px solid rgba(0, 0, 0, 0.2); */
  border-radius: 0.5em;
  outline: 0;
}
</style>