<template>
  <div>
    <div class="modal-body__content__table">
      <div v-show="isLoading" class="category-data-table loading-wave"
        :class="{ 'mini-size': pageSize == 6, 'large-size': pageSize != 6 }"></div>
      <div v-show="!isLoading" class="category-data-table"
        :class="{ 'mini-size': pageSize == 6, 'large-size': pageSize != 6 }">
        <table class="table table-striped">
          <thead class="custom-header-table">
            <tr>
              <th v-for="(field, index) in fieldList" :key="index"
                :class="[field.isNumber ? 'text-right' : field.isCenter ? 'text-center' : 'text-left']">
                <span>{{ field.title }}</span>
              </th>
            </tr>
          </thead>
          <tbody v-if="dataList.length > 0" class="op-list">
            <tr v-for="(data, index) in dataList" :key="index">
              <td v-for="(dataField, index1) in fieldList" :key="index1" :class="[
              { 'content__hyper-link': dataField.isHyperLink },
              { 'overflowUnset': dataField.field === 'totalProduced' },
              dataField.isNumber ? 'text-right' : dataField.isCenter ? 'text-center' : 'text-left'
              ]" @click="handleCellClick(dataField.isHyperLink, data, dataField)">
                <a-tooltip placement="bottomLeft">
                  <template slot="title">
                    <div style="padding: 6px 8px">
                      <span v-if="dataField.field === 'deliveryRiskLevel'">
                        {{transferText(data[`${dataField.field}`],dataField.formatter)}}
                      </span>
                      <span v-else-if="dataField.isArrayField">{{
                      transferText(
                      convertArrayField(
                      data[`${dataField.field}`],
                      dataField.numberField
                      ),
                      dataField.formatter
                      )
                      }}</span>
                      <span v-else-if="dataField.numberField != null">{{
                      transferText(
                      convertNumberField(
                      data[`${dataField.field}`],
                      dataField.numberField
                      ),
                      dataField.formatter
                      )
                      }}</span>
                      <span v-else>{{
                      transferText(data[`${dataField.field}`])
                      }}</span>
                    </div>
                  </template>
                  <!-- SEPCIAL FIELD -->
                  <span v-if="dataField.field === 'totalProduced'"
                    class="cell product-produced position-relative hyperlink" @click="openCustomChartBar(index)"
                    style="color: #3491ff;" @mouseover="isHover = index" @mouseleave="isHover = -1">
                    {{transferText(data[`${dataField.field}`],dataField.formatter)}}
                    <!-- <img
                      src="/images/icon/duplicate.svg"
                      alt="icon"
                    > -->
                    <div v-show="isHover !== index" class="link-icon1">
                      <img src="/images/icon/hyperlink-icon.svg" alt="icon" />
                    </div>
                    <div v-show="isHover === index" class="link-icon1">
                      <img src="/images/icon/hyperlink-icon-hover.svg" alt="icon" />
                    </div>
                    <div v-if="isShowCommonBarChart === index" v-click-outside="closeCommonChartBar"
                      class="modal-custom-barchart">
                      <div class="icon-wrapper" @click.stop="isShowCommonBarChart = null" aria-hidden="true">
                        <span class="t-icon-close"></span>
                      </div>
                      <common-barchart :data="data"></common-barchart>
                    </div>
                  </span>
                  <span v-else-if="dataField.field === 'deliveryRiskLevel'">
                    <span :class="data[`${dataField.field}`].toLowerCase()" class="risk-level__icon"></span>
                    {{transferText(
                    data[`${dataField.field}`],
                    dataField.formatter
                    )}}
                  </span>

                  <!-- CONFIG FIELD -->
                  <span v-else-if="dataField.isArrayField">{{
                  transferText(
                  convertArrayField(
                  data[`${dataField.field}`],
                  dataField.numberField
                  )
                  )
                  }}</span>
                  <span v-else-if="dataField.isPercent">
                    {{
                    transferText(
                    convertPercentField(data[`${dataField.field}`])
                    )
                    }}
                  </span>
                  <span v-else-if="dataField.numberField != null">{{
                  transferText(
                  convertNumberField(
                  data[`${dataField.field}`],
                  dataField.numberField,
                  ),
                  dataField.formatter
                  )
                  }}</span>
                  <span v-else>{{
                  transferText(
                  data[`${dataField.field}`],
                  dataField.formatter
                  )
                  }}</span>
                </a-tooltip>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="dataList.length == 0" class="no-results-category">
          No Results
        </div>
      </div>
      <div class="category-data-table__footer d-flex">
        <div class="modal-body__content__footer">
          <span>{{ `${getCurrentPage} of ${totalPage}` }}</span>
        </div>
        <div class="paging__arrow">
          <a href="javascript:void(0)" class="paging-button" :class="{ 'inactive-button': getCurrentPage <= 1 }"
            @click="changePage('-')">
            <img src="/images/icon/category/paging-arrow.svg" alt="" />
          </a>
          <a href="javascript:void(0)" class="paging-button" @click="changePage('+')"
            :class="{ 'inactive-button': getCurrentPage == totalPage }">
            <img src="/images/icon/category/paging-arrow.svg" style="transform: rotate(180deg)" alt="" />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: {
      type: String,
      default: () => "",
    },
    dataList: {
      type: Array,
      default: () => [],
    },
    fieldList: {
      type: Array,
      default: () => [],
    },
    pageSize: {
      type: Number,
      default: 0,
    },
    project: {
      type: Object,
      default: () => ({
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      }),
    },
    brand: {
      type: Object,
      default: {
        id: null,
        molds: [],
        name: "",
        parts: [],
        products: [],
        brandImage: null,
        suppliers: [],
        totalProduced: 0,
      },
    },
    currentPage: {
      type: Number,
      default: () => 1,
    },
    totalPage: {
      type: Number,
      default: () => 1,
    },
    isLoading: Boolean,
    setRouteName: Function,
    step: Array,
  },
  components: {
    "common-barchart": httpVueLoader("/components/common-barchart.vue"),
  },
  data() {
    return {
      isShowCommonBarChart: null,
      isHover: -1
    };
  },
  computed: {
    getCurrentPage() {
      if (this.currentPage) {
        if (this.totalPage == 0) {
          return 0;
        } else {
          return this.currentPage;
        }
      } else {
        return 1;
      }
    },
  },
  methods: {
    openCustomChartBar(index) {
      setTimeout(() => {
        this.isShowCommonBarChart = index;
      }, 200)
    },
    closeCommonChartBar() {
      this.isShowCommonBarChart = null
    },
    transferText(root, formatter) {
      const result = !root && root !== 0 ? "-" : root;
      return !formatter ? result : formatter(result);
    },
    convertNumberField(data, fieldName) {
      let number = 0;
      if (data != null) {
        if (`${data}`.includes(".")) {
          number = data.toFixed(1);
        } else {
          number = data;
        }
      }
      return `${number} ${number > 1 ? fieldName + "s" : fieldName}`;
    },
    convertArrayField(data, fieldName) {
      let fieldNameAfter = ''
      if (fieldName) {
        fieldName = data.length > 1 ? fieldName + "s" : fieldName
      }
      return `${data.length} ${fieldNameAfter}`;
    },
    convertPercentField(data, fieldName) {
      if (!data) return;
      return `${data}%`;
    },
    changePage(sign) {
      this.$emit("change-page", sign);
    },
    handleChangeRoute(route, isBrand) {
      this.$emit("change-route", route, isBrand);
    },
    handleCellClick(isEnable, data, field) {
      if (isEnable) {
        console.log(this.type, data, field);
        let newRoute = null;
        if (field) {
          switch (this.type) {
            case "list-mold":
              if (field.field == "partCount") {
                this.setRouteName(data.equipmentCode);
                newRoute = {
                  route: "list-part",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    moldId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "equipmentCode") {
                newRoute = {
                  route: "mold-detail",
                  data: { dataDetail: data },
                };
              } else if (field.field == "companyName") {
                newRoute = {
                  route: "company-detail",
                  data: { dataDetail: null, id: data.companyId },
                };
              }
              break;
            case "list-part":
              if (field.field == "moldCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-mold",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    partId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "supplierCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-supplier",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    partId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "name") {
                newRoute = {
                  route: "part-detail",
                  data: { dataDetail: data },
                };
              }
              break;
            case "project-profile":
              if (field.field == "moldCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-mold",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    partId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "supplierCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-supplier",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    partId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "name") {
                newRoute = {
                  route: "part-detail",
                  data: { dataDetail: data },
                };
              }
              break;
            case "list-supplier":
              if (field.field == "moldCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-mold",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    supplierId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "partCount") {
                this.setRouteName(data.name);
                newRoute = {
                  route: "list-part",
                  data: {
                    project: this.project,
                    brand: this.brand,
                    supplierId: data.id,
                    page: 1,
                    dataDetail: data,
                  },
                };
              } else if (field.field == "name") {
                newRoute = {
                  route: "company-detail",
                  data: { dataDetail: data },
                };
              }
              break;
          }
        }
        console.log(newRoute);
        if (newRoute) {
          console.log("run there!", newRoute, this.step);
          const find = this.step.find(item => item.route === 'brand-list')
          if (find) {
            this.handleChangeRoute(newRoute, false);
          } else {
            this.handleChangeRoute(newRoute, true);
          }
        }
      }
    },
  },

  watch: {
    project(newVal) {
      console.log('@project-data-table:project', newVal)
    },
    fieldList(newVal) {
      console.log('@project-data-table:fieldList', newVal)
    },
    dataList(newVal) {
      console.log('@project-data-table:dataList', newVal)
    }
  }
};
</script>

<style scoped>
.category-data-table {
  border: 1px solid #d6dade;
  border-radius: 4px;
  height: 300px;
}

.category-data-table__footer {
  height: 30px;
  padding-top: 8px;
  align-items: center;
  justify-content: flex-end;
}

.paging-button {
  padding: 6px 8px;
  border-radius: 3px;
  background-color: #3491ff;
  margin-left: 15px;
  display: inline-flex;
}

.inactive-button {
  background-color: #c4c4c4;
  pointer-events: none;
}

.mini-size {
  height: 300px;
}

.large-size {
  height: 555px;
}

.custom-header-table {
  border-bottom: 1px solid #d6dade;
  color: #454545;
}

.table tbody {
  color: #7e7e7e;
}

.table td {
  border-bottom: unset;
}

.table-striped tbody tr:nth-of-type(odd) {
  background: transparent;
}

.table-striped tbody tr:nth-of-type(even) {
  background: #eef0f1;
}

.table th,
.table td {
  width: 114px;
}

.table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 114px;
}

.spinner {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.no-results-category {
  text-align: center;
  padding: 50px;
  margin-bottom: 1rem;
}

.risk-level__icon {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  display: inline-block;
  margin-right: 4px;
}

.risk-level__icon.high {
  background-color: #db3b21;
}

.risk-level__icon.medium {
  background-color: #ffc107;
}

.risk-level__icon.low {
  background-color: #1aaa55;
}

.text-right span {
  padding-right: 2%;
}

span.product-produced {
  margin-right: 10px;
  position: relative;
}

span.product-produced img {
  position: absolute;
  left: calc(100% + 4px);
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 15px;
}

.t-icon-close {
  width: 8px;
  height: 8px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.overflowUnset {
  overflow: unset !important;
  cursor: pointer;
}
</style>