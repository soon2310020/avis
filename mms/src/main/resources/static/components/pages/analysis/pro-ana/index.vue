<template>
  <div id="process-analysis-main" ref="contentMain">
    <div class="process-analysis-top">
      <div class="analysis-top-main">
        <master-filter-wrapper
          :filter-code="filterCode"
          :notify-filter-updated="getProcessAnalysisTableData"
        ></master-filter-wrapper>
        <emdn-search-bar
          :set-search-complete-keyword="setSearchCompleteKeyword"
          :style-props="searchbarStyleProps"
          :placeholder-text="placeholderText"
        ></emdn-search-bar>
      </div>
      <content-modal
        v-if="isChartModalVisible"
        :is-opened="isChartModalVisible"
        title="Process Analysis"
        button-text="Close Process Graph"
        :width="contentWidth + 'px'"
        height="calc(100vh - 62px)"
        :on-close="closeChartModal"
      >
        <pressure-chart-modal :mold="currentMold"></pressure-chart-modal>
      </content-modal>

      <div class="data-table-tabs">
        <ul class="nav nav-tabs" style="margin-bottom: -1px">
          <li class="nav-item custom-nav">
            <a class="nav-link" :class="{ active: true }">
              <div class="part-name-drop-down one-line">
                <span> All </span>
                <span class="badge badge-light badge-pill">{{
                  totalElements
                }}</span>
              </div>
            </a>
          </li>
        </ul>
      </div>
      <div class="analysis-data-table">
        <div class="table">
          <div>
            <div class="col-lg-12 custom-table">
              <table
                class="table-responsive-sm table-striped"
                style="width: 100%"
              >
                <colgroup>
                  <col />
                  <col />
                  <col />
                  <col />
                  <col />
                </colgroup>
                <thead class="thead-main">
                  <tr class="tr-sort">
                    <th
                      class="__sort desc text-left"
                      :class="[
                        { active: currentSortType === 'moldCode' },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortHandler('moldCode')"
                    >
                      <span>Tooling ID</span>
                      <span class="__icon-sort"></span>
                    </th>
                    <th
                      class="__sort desc text-left"
                      :class="[
                        { active: currentSortType === 'utilizationRate' },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortHandler('utilizationRate')"
                    >
                      <span>Utilization Rate</span>
                      <span class="__icon-sort"></span>
                    </th>
                    <th
                      class="__sort desc text-left"
                      :class="[
                        { active: currentSortType === 'approvedCycleTime' },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortHandler('approvedCycleTime')"
                    >
                      <span>ACT</span>
                      <span class="__icon-sort"></span>
                    </th>
                    <th
                      class="__sort desc text-left"
                      :class="[
                        { active: currentSortType === 'averageCycleTime' },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortHandler('averageCycleTime')"
                    >
                      <span>WACT</span>
                      <span class="__icon-sort"></span>
                    </th>
                    <th
                      class="__sort desc text-left"
                      :class="[
                        { active: currentSortType === 'toolingStatus' },
                        isDesc ? 'desc' : 'asc',
                      ]"
                      @click.prevent="sortHandler('toolingStatus')"
                    >
                      <span>Tooling Status</span>
                      <span class="__icon-sort"></span>
                    </th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in tableDataList" :key="index">
                    <td class="text-left">
                      <span>{{ item.moldCode }}</span>
                      <div class="small text-muted font-size-11-2">
                        Updated on
                        {{
                          moment(
                            item.updatedDateTime,
                            "YYYY-MM-DD HH:mm:ss"
                          ).format("YYYY-MM-DD")
                        }}
                      </div>
                    </td>
                    <td class="text-left">
                      <span>
                        {{
                          item.utilizationRate === null
                            ? "-"
                            : `${item.utilizationRate}%`
                        }}
                      </span>
                    </td>
                    <td class="text-left">
                      <div>
                        <span>
                          {{
                            item.approvedCycleTime === null
                              ? "-"
                              : `${item.approvedCycleTime}`
                          }}
                        </span>
                        <div class="small text-muted font-size-11-2">
                          Seconds
                        </div>
                      </div>
                    </td>
                    <td class="text-left">
                      <span>
                        {{
                          (
                            Math.round(
                              Number(item.averageCycleTime) * 1000 +
                                Number.EPSILON
                            ) / 1000
                          ).toFixed(2)
                        }}
                      </span>
                      <div class="small text-muted font-size-11-2">Seconds</div>
                    </td>
                    <td class="text-left font-style">
                      <div
                        :style="{
                          height: '40px',
                          display: 'flex',
                          alignItems: 'center',
                        }"
                      >
                        <emdn-table-status
                          :category="item.toolingStatus"
                        ></emdn-table-status>
                      </div>
                    </td>
                    <td class="text-left">
                      <div
                        :style="{
                          height: '40px',
                          display: 'flex',
                          alignItems: 'center',
                        }"
                      >
                        <emdn-cta-button :click-handler="() => viewGraph(item)"
                          >View Graph
                        </emdn-cta-button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="analysis-botton">
      <span class="analysis-pagination">
        <emdn-pagination
          :current-page="pageNumber"
          :total-page="totalPages"
          :disabled="false"
          :next-click-handler="paginationHandler"
          :previous-click-handler="paginationHandler"
        >
        </emdn-pagination>
      </span>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "process-analysis-main",
  components: {
    "pressure-chart-modal": httpVueLoader(
      "/components/pages/analysis/pro-ana/chart.vue"
    ),
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      //new
      searchQuery: "",
      contentWidth: 0,
      currentMold: 0,
      tableDataList: [],
      isChartModalVisible: false,
      pageSize: 0,
      pageNumber: 1,
      totalPages: 0,
      totalElements: 0,
      sort: "",
      filterCode: "COMMON",
      // old
      searchbarStyleProps: "width: 286px",
      placeholderText: "Search by Selected Column Below",
      listChecked: [],
      isAll: false,
      currentSortType: "",
      isDesc: true,
    };
  },
  methods: {
    setSearchCompleteKeyword(keyword) {
      console.log("setSearchCompleteKeyword: ", keyword);
      this.searchQuery = keyword;
      this.totalPages = 0;
      this.pageNumber = 1;
      this.getProcessAnalysisTableData();
    },
    async getProcessAnalysisTableData() {
      console.log("getProcessAnalysisTableData");
      let pageParam = `&page=${this.pageNumber}`;
      let sizeParam = `&size=${this.pageSize}`;
      let sortParam = `&sort=${this.sort}`;
      let queryParam = `&query=${this.searchQuery}`;
      let url =
        "/api/analysis/pro-ana/molds?" +
        pageParam +
        sizeParam +
        sortParam +
        queryParam;
      // return await url;
      return await axios.get(url).then((res) => {
        console.log(res);

        this.totalPages = res.data.totalPages;
        this.totalElements = res.data.totalElements;
        this.tableDataList = res.data.content;
      });
    },
    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    check(e, item) {
      if (e.target.checked) {
        this.listChecked.push(+e.target.value);
      } else {
        this.listChecked = this.listChecked.filter(
          (value) => value != e.target.value
        );
        this.isAll = false;
      }
      if (this.listChecked.length == this.list.length) {
        this.isAll = true;
      }
    },
    selectAll() {
      console.error("--- selected all called --");
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.list.map((value) => value.id);
      } else {
        this.listChecked = [];
      }
    },
    sortHandler(value) {
      console.log("sortHandler: ", value);
      this.currentSortType = value;
      if (this.isDesc) {
        value = value + ",asc";
      } else {
        value = value + ",desc";
      }
      console.log("value: ", value);
      this.sort = value;
      this.isDesc = this.isDesc ? false : true;
      this.getProcessAnalysisTableData();
    },
    viewGraph(mold) {
      console.log("View graph: ", mold);
      this.currentMold = mold;
      this.isChartModalVisible = true;
    },
    paginationHandler(pagination) {
      console.log("paginationHandler: ", pagination);
      this.pageNumber = pagination.currentPage;
      this.getProcessAnalysisTableData();
    },
    closeChartModal() {
      this.isChartModalVisible = false;
    },
    setContentWidth() {
      console.log(this.$refs.contentMain);
      const contentWidth = this.$refs.contentMain?.clientWidth;
      console.log("content width:", contentWidth);
      if (!contentWidth) return;
      this.contentWidth = contentWidth;
    },

    //  MasterFilter Func New
    async getFilterItems$(args) {
      return getFilterItems({
        ...args,
        filterCode: this.filterCode,
      });
    },

    async saveFilterItems$(args) {
      return saveFilterItems({
        ...args,
        filterCode: this.filterCode,
      });
    },
  },
  mounted() {
    this.setContentWidth();
    window.addEventListener("resize", this.setContentWidth);

    this.getProcessAnalysisTableData();
  },

  destroyed() {
    window.addEventListener("resize", this.setContentWidth);
  },
};
</script>

<style scoped>
/************************ Page Layout Start  ************************/

.op-active.op-active-sm {
  width: 13px;
  height: 13px;
}

.label-detached {
  border: 1px dashed #e34537;
}

.op-active {
  margin-top: 10px;
}

.custom-nav {
  width: 136px;
  height: 41px;
}

.custom-table {
  padding: 0;
  height: 522px;
  overflow-x: auto;
}

.table {
  display: table;
  overflow-x: auto;
  box-sizing: border-box;
  border-spacing: 2px;
  border-color: grey;
  /*border-collapse: collapse;*/
}

.table td {
  border-top: 0;
  padding: 0.5em;
  border-bottom: none;
}

.table thead th {
  border-top: none;
}

.data-table-tabs {
  width: 100%;
}

#process-analysis-main {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.tr-sort {
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4b4b4b;
}

.custom-decoration {
  color: #3491ff;
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.custom-decoration:hover {
  color: #3491ff;
  text-decoration: underline;
}

.thead-main {
  background: #ffffff;
  position: sticky;
  position: -webkit-sticky;
  top: 0px;
  z-index: 99;
  height: 34px;
}

.tr-sort {
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4b4b4b;
}

.font-style {
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #4b4b4b;
}

.__icon-sort {
  margin-left: 0px !important;
}

.process-analysis-top {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
  width: 100%;
}

.analysis-top-main {
  display: flex;
  justify-content: space-between;
  width: 100%;
  align-items: center;
}

.analysis-top-sub {
  display: flex;
  align-items: center;
  justify-content: end;
  width: 100%;
}

.analysis-dropdown {
  border: 1px solid black;
}

.analysis-data-table {
  width: 100%;
  height: 522px;
  border: 1px solid #c8ced3;
  margin-top: -17px;
}

.part-name-drop-down {
  color: #4b4b4b;
  font-family: "Helvetica Neue";
  font-style: normal;
  font-weight: 700;
  font-size: 14px;
  line-height: 17px;
}

.badge {
  margin-left: 10px;
}

.analysis-botton {
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: end;
  margin-top: 20px;
  margin-bottom: 22px;
}

.analysis-pagination {
}

._dropdown_wrap_xdaez_7 {
  width: max-content !important;
}

.modal-footer-area {
  padding-right: 22px !important;
}

.hint {
  margin-right: 39px;
}

.title {
  margin-top: 14px;
  color: rgb(75, 75, 75);
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-left: -66px;
}

/************************ Page Layout Start  ************************/
</style>
