<template>
  <div class="input-dem-main-container">
    <div>
      <div class="input-dem-header">
        <div class="input-dem-header-nav">
          <emdn-icon-button
            button-type="arrow-backward-skinny"
            :click-handler="buttonBacktoprevious"
          ></emdn-icon-button>
          <emdn-cta-button
            button-type="text"
            :click-handler="buttonBacktoprevious"
            style="padding-left: 0"
          >
            Back to previous page
          </emdn-cta-button>
        </div>
        <div class="input-dem-header-top-bar">
          <emdn-search-bar
            placeholder-text="Search by menu or permission name"
            style-props="position:static; width:302px;"
            :set-search-complete-keyword="setSearchComplete"
            :click-handler="setSearchComplete"
            :placeholder-text="listBarPlaceHolder"
          ></emdn-search-bar>
          <div>
            <span style="font-size: 14.66px">View By</span>
            <span class="set-position">
              <emdn-cta-button
                button-type="dropdown"
                color-type="white"
                :active="showProduct"
                :click-handler="productToggleHandler"
                class="text-capitalize"
                >{{ viewBy }}</emdn-cta-button
              >
              <emdn-dropdown
                id="product"
                :visible="showProduct"
                :items="viewByList"
                :click-handler="viewByClickHandler"
                :on-close="onCloseForProduct"
                style-props="margin-top: 2px"
              ></emdn-dropdown
            ></span>
            <span class="set-position">
              <emdn-cta-button
                button-type="date-picker"
                color-type="blue"
                :click-handler="calendarOpenHandler"
              >
                {{ selectedYear }}
              </emdn-cta-button>
              <emdn-popover
                :visible="showCalendar"
                @close="calendarCloseHandler"
                :position="{ right: '0px' }"
                style="margin-top: 2px"
              >
                <emdn-date-picker
                  style="
                    position: absolute;
                    background: white;
                    z-index: 111;
                    right: 0;
                  "
                  :time-scale="timeScale"
                  :date-range="dateRange"
                  :min-date="minDate"
                  @get-date-range="getDateRange"
                ></emdn-date-picker>
              </emdn-popover>
            </span>
          </div>
        </div>
      </div>

      <div class="input-dem-body">
        <div class="input-dem-table-wrapper" ref="tableInnerRef">
          <table
            class="dem-cpl-table"
            :class="{ 'parts-input-table': viewBy === 'part' }"
          >
            <!-- table header -->
            <tr class="tr-sort">
              <th v-if="viewBy === 'product'" :style="{ width: '200px' }">
                <div style="display: flex; align-items: center">
                  <span>Product Name</span>
                  <span
                    @click.prevent="sortHandler('name')"
                    style="cursor: pointer"
                  >
                    <emdn-icon
                      :button-type="
                        sortParam === 'name,asc' ? 'sort-asce' : 'sort-desc'
                      "
                      style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.125rem;"
                      :active="sortParam.includes('name')"
                    ></emdn-icon>
                  </span>
                </div>
              </th>
              <th v-if="viewBy === 'part'" :style="{ width: '200px' }">
                <div style="display: flex; align-items: center">
                  <span>Part</span>
                  <span
                    @click.prevent="sortHandler('partName')"
                    style="cursor: pointer"
                  >
                    <emdn-icon
                      :button-type="
                        sortParam === 'partName,asc' ? 'sort-asce' : 'sort-desc'
                      "
                      style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.125rem;"
                      :active="sortParam.includes('partName')"
                    ></emdn-icon>
                  </span>
                </div>
              </th>
              <th v-if="viewBy === 'part'" :style="{ width: '200px' }">
                <div style="display: flex; align-items: center">
                  <span>Supplier</span>
                  <span
                    @click.prevent="sortHandler('supplierName')"
                    style="cursor: pointer"
                  >
                    <emdn-icon
                      :button-type="
                        sortParam === 'supplierName,asc'
                          ? 'sort-asce'
                          : 'sort-desc'
                      "
                      style-props="width: 1.125rem; height: 1.125rem; padding: 0; margin-left: 0.125rem;"
                      :active="sortParam.includes('supplierName')"
                    ></emdn-icon>
                  </span>
                </div>
              </th>
              <th v-for="index in totalWeek" :key="index">
                {{
                  `Week ${index >= 10 ? index : "0" + index} , ${selectedYear}`
                }}
              </th>
            </tr>
            <!-- table body -->
            <tr v-if="isLoading || !demandData.length">
              <td
                colspan="100"
                style="
                  background: #ffffff;
                  border-top: 1px solid #c9ced2;
                  height: auto;
                "
              ></td>
            </tr>
            <tr
              v-for="(demandItem, demandIndex) in demandData"
              :key="demandIndex"
            >
              <td v-if="viewBy === 'product'">
                <div
                  @mouseover.stop="(e) => setTooltip(e, demandItem?.name)"
                  @mouseleave.stop="initializeTableTooltip"
                  class="input-dem-truncate-text"
                >
                  {{ demandItem?.name }}
                </div>
              </td>
              <td v-if="viewBy === 'part'">
                <div>
                  <div
                    @mouseover.stop="(e) => setTooltip(e, demandItem?.partName)"
                    @mouseleave.stop="initializeTableTooltip"
                    class="input-dem-truncate-text"
                  >
                    {{ demandItem?.partName }}
                  </div>
                  <div
                    @mouseover.stop="(e) => setTooltip(e, demandItem?.partCode)"
                    @mouseleave.stop="initializeTableTooltip"
                    class="input-dem-truncate-text"
                    style="font-size: 11.25px"
                  >
                    {{ demandItem?.partCode }}
                  </div>
                </div>
              </td>
              <td v-if="viewBy === 'part'">
                <div>
                  <div
                    @mouseover.stop="
                      (e) => setTooltip(e, demandItem?.supplierName)
                    "
                    @mouseleave.stop="initializeTableTooltip"
                    class="input-dem-truncate-text"
                  >
                    {{ demandItem?.supplierName }}
                  </div>
                  <div
                    @mouseover.stop="
                      (e) => setTooltip(e, demandItem?.supplierCode)
                    "
                    @mouseleave.stop="initializeTableTooltip"
                    class="input-dem-truncate-text"
                    style="font-size: 11.25px"
                  >
                    {{ demandItem?.supplierCode }}
                  </div>
                </div>
              </td>
              <td v-for="week in totalWeek" :key="week">
                <div>
                  <emdn-table-input
                    :id="`${demandItem.name}-w${week}`"
                    :number-formatting="true"
                    style-props="padding: 0 16px 0 0"
                    text-position="right"
                    :change-handler="
                      (value, id, isChanged) =>
                        inputChangeHandler(
                          value,
                          id,
                          isChanged,
                          'w' + week,
                          demandIndex
                        )
                    "
                    :value="Number(demandItem['w' + week])"
                  >
                  </emdn-table-input>
                </div>
              </td>
            </tr>
          </table>
        </div>
      </div>
    </div>
    <div class="input-dem-footer">
      <div>
        <emdn-pagination
          :current-page="pageNumber"
          :total-page="totalPages"
          :next-click-handler="nextPaginationHandler"
          :previous-click-handler="prevPaginationHandler"
        >
        </emdn-pagination>
      </div>

      <div>
        <emdn-cta-button
          color-type="green-fill"
          :click-handler="saveHandler"
          style-props="margin: 0;"
          >Save
        </emdn-cta-button>
        <emdn-alert-box
          :show-alert-box="showSaveAlertBox"
          title="Overwrite Part Demand"
        >
          <template #messagebody>
            <span
              >Saving product demand data will result in overwriting any part
              demand input for the week.
            </span>
          </template>
          <template #successbutton>
            <emdn-cta-button
              color-type="blue-fill"
              :click-handler="saveProductDemand"
              style-props="box-sizing: border-box; height: 30px"
            >
              <span v-if="isLoadingForSaveDemand" style="width: 148px">
                <emdn-line-spinner></emdn-line-spinner>
              </span>
              <span v-else>Save Product Demand</span>
            </emdn-cta-button>
          </template>
          <template #rejectbutton>
            <emdn-cta-button
              color-type="white"
              :click-handler="cancelAlert"
              style-props="box-sizing: border-box; height: 30px"
              >Cancel
            </emdn-cta-button>
          </template>
        </emdn-alert-box>

        <emdn-alert-box
          :show-alert-box="showSaveAndMovePageAlertBox"
          :checkbox="true"
          :check-box-handler="saveAndMovePageAlertCheckBoxHandler"
          title="Save your data input"
        >
          <template #messagebody
            ><span
              >If you proceed to a different page without saving, your data may
              be lost.</span
            ></template
          >
          <template #rejectbutton>
            <emdn-cta-button
              color-type="white"
              :click-handler="() => (showSaveAndMovePageAlertBox = false)"
              style-props="box-sizing: border-box; height: 30px"
              >Cancel
            </emdn-cta-button>
          </template>
          <template #successbutton>
            <emdn-cta-button
              color-type="green-fill"
              :click-handler="saveAndMovePageHandler"
              style-props="box-sizing: border-box; height: 30px"
              >Save and Change Page
            </emdn-cta-button>
          </template>
        </emdn-alert-box>
      </div>
    </div>
    <div
      v-if="isOpenedTableTooltip"
      :style="tableTooltipPosition"
      class="input-dem-table-tooltip-wrapper"
      ref="tooltipRef"
    >
      {{ tableTooltipInfo.text }}
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "dem-cpl-input-page",
  data() {
    return {
      filterCode: "COMMON",
      // search
      query: "",

      // new
      sortParam: "",
      sortSet: {
        name: {
          isDesc: false,
        },
        partName: {
          isDesc: false,
        },
        supplierName: {
          isDesc: false,
        },
      },
      pageNumber: 1,
      pageSize: 10,
      totalPages: 1,
      totalWeek: 0,
      isLoading: false,
      isLoadingForSaveDemand: false,
      // old
      demandData: [],
      listBarPlaceHolder: "Search product name",
      showSaveAlertBox: false,
      showSaveAndMovePageAlertBox: false,
      isViewedSaveAndMovePageAlertBoxToday: true,
      isChangedData: false,
      currentClickedPageButton: "",
      currentSortType: "",
      showCalendar: false,
      tempTimeScale: "YEAR",
      timeScale: "YEAR",
      dateRange: {
        start: moment().startOf("year"),
        end: moment().endOf("year"),
      },
      minDate: null,
      showProduct: false,
      toDate: moment().startOf("year").format("YYYYMMDD"),
      fromDate: moment().endOf("year").format("YYYYMMDD"),
      viewBy: "product",
      viewByList: [
        { title: "Product", id: "1", placeholder: "Search product name" },
        { title: "Part", id: "2", placeholder: "Search part ID" },
      ],
      tableTooltipInfo: {
        text: null,
        top: null,
        left: null,
      },
      isOpenedTableTooltip: false,
      visibleBadgeDropdownId: null,
      visibleBadgeDropdownIndex: null,
    };
  },
  computed: {
    selectedYear() {
      return Common.getDatePickerButtonDisplay(
        this.fromDate,
        this.toDate,
        this.tempTimeScale
      );
    },
    tableTooltipPosition() {
      return {
        top: `${this.tableTooltipInfo.top}px`,
        left: `${this.tableTooltipInfo.left}px`,
      };
    },
  },
  methods: {
    inputChangeHandler(value, id, isChanged, key, index) {
      this.demandData[index][key] = Number(value);
      if (isChanged) this.isChangedData = true;
    },
    setSearchComplete(keyword) {
      this.pageNumber = 1;
      this.query = keyword;
      this.getDemandCompliance();
    },
    async getDemandCompliance() {
      this.demandData = [];
      let sortParam = `?sort=${this.sortParam}`;
      let pageParam = `&page=${this.pageNumber}`;
      let sizeParam = `&size=${this.pageSize}`;
      let timeScaleParam = `&timeScale=YEAR`;
      let timeValueParam = `&timeValue=${this.selectedYear}`;
      let queryParam = `&query=${this.query}`;
      let filterCodeParam = `&filterCode=${this.filterCode}`;

      let url =
        `/api/supplychain/dem-cpl/${this.viewBy}-demands` +
        sortParam +
        pageParam +
        sizeParam +
        timeScaleParam +
        timeValueParam +
        queryParam +
        filterCodeParam;

      this.isLoading = true;
      // content, empty, first, last, number, numberOfElements, pageable, size, sort, totalElements, totalPages
      await axios.get(url).then((res) => {
        if (!res.data.content.length) {
          this.pageNumber = 1;
          this.totalPages = 1;
          return;
        }
        this.demandData = res.data.content;
        this.pageNumber = res.data.number + 1;
        this.totalPages = res.data.totalPages;
      });
      this.isLoading = false;
    },
    async postDemandCompliance(redirect = true) {
      let param = { content: this.demandData };
      let timeScaleParam = `?timeScale=YEAR`;
      let timeValueParam = `&timeValue=${this.selectedYear}`;
      let filterCodeParam = `&filterCode=${this.filterCode}`;

      let url =
        `/api/supplychain/dem-cpl/${this.viewBy}-demands` +
        timeScaleParam +
        timeValueParam +
        filterCodeParam;

      this.isLoadingForSaveDemand = true;

      await axios.post(url, param).then((res) => {
        if (res.status === 200) {
          if (redirect) {
            window.location.href = `/supplychain/dem-cpl#${this.viewBy}s`;
          }
        }
      });

      this.isLoadingForSaveDemand = false;
    },
    saveProductDemand() {
      this.postDemandCompliance();
    },
    cancelAlert() {
      this.showSaveAlertBox = false;
    },
    sortHandler(value) {
      this.currentSortType = value;

      this.sortSet[value].isDesc = !this.sortSet[value].isDesc;
      let isDesc = this.sortSet[value].isDesc ? "asc" : "desc";
      this.sortParam = `${value},${isDesc}`;
      this.getDemandCompliance();
    },
    prevPaginationHandler() {
      if (!this.isViewedSaveAndMovePageAlertBoxToday || !this.isChangedData) {
        this.pageNumber--;
        this.getDemandCompliance();
      } else {
        this.currentClickedPageButton = "prev";
        this.showSaveAndMovePageAlertBox = true;
      }
      this.initTableScrollPosition();
    },
    nextPaginationHandler() {
      if (!this.isViewedSaveAndMovePageAlertBoxToday || !this.isChangedData) {
        this.pageNumber++;
        this.getDemandCompliance();
      } else {
        this.currentClickedPageButton = "next";
        this.showSaveAndMovePageAlertBox = true;
      }
      this.initTableScrollPosition();
    },
    async saveAndMovePageHandler() {
      await this.postDemandCompliance(false);
      if (this.currentClickedPageButton === "next") {
        this.pageNumber++;
      } else {
        this.pageNumber--;
      }
      await this.getDemandCompliance();
      this.currentClickedPageButton = "";
      if (!this.isViewedSaveAndMovePageAlertBoxToday) {
        const today = moment().format("YYYY-MM-DD");
        localStorage.setItem("closedDate", today);
      }
      this.showSaveAndMovePageAlertBox = false;
      this.isChangedData = false;
    },
    saveAndMovePageAlertCheckBoxHandler(isChecked) {
      if (isChecked) {
        this.isViewedSaveAndMovePageAlertBoxToday = false;
      }
    },
    saveHandler() {
      if (this.viewBy === "product") {
        this.showSaveAlertBox = true;
        return;
      }
      this.postDemandCompliance();
    },
    calendarOpenHandler() {
      this.showProduct = false;
      this.showCalendar = !this.showCalendar;
    },
    calendarCloseHandler() {
      this.showCalendar = false;
    },
    getDateRange(dateRange) {
      this.fromDate = dateRange.start.format("YYYYMMDD");
      this.toDate = dateRange.end.format("YYYYMMDD");
      this.dateRange = dateRange;
      this.pageNumber = 1;

      this.getDemandCompliance();
    },
    productToggleHandler() {
      this.showCalendar = false;
      this.showProduct = !this.showProduct;
    },
    initTableScrollPosition() {
      const tableInnerRef = this.$refs?.tableInnerRef;
      tableInnerRef.scrollTo({ top: 0, left: 0, behavior: "smooth" });
    },
    viewByClickHandler(item) {
      this.listBarPlaceHolder = item.placeholder;
      this.viewBy = item.title.toLowerCase();
      window.location.hash = this.viewBy + "s";
      this.pageNumber = 1;
      this.resetSearchValue();
      this.getDemandCompliance();
      this.initTableScrollPosition();
      this.showProduct = false;
    },
    onCloseForProduct() {
      this.showProduct = false;
    },
    buttonBacktoprevious() {
      window.location.href = `/supplychain/dem-cpl#${this.viewBy}s`;
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    resetSearchValue() {
      const searchBarComponent = this.$children.filter(
        (el) => el.$refs.searchInputRef
      )?.[0];
      const searchBarInputRef = searchBarComponent.$refs?.searchInputRef;

      this.query = "";
      this.$nextTick(() => {
        searchBarInputRef.value = "";
      });
    },
    setTooltip(e, text) {
      if (e.target.clientWidth >= e.target.scrollWidth || !text) return;

      this.tableTooltipInfo.text = text;
      this.isOpenedTableTooltip = true;

      this.$nextTick(() => {
        const targetRect = e.target.getBoundingClientRect();
        const tooltipRect = this.$refs.tooltipRef.getBoundingClientRect();

        this.tableTooltipInfo.top =
          targetRect.top + targetRect.height / 2 - tooltipRect.height / 2;
        this.tableTooltipInfo.left = targetRect.left + targetRect.width + 8;
      });
    },
    initializeTableTooltip() {
      this.tableTooltipInfo = {
        text: null,
        top: null,
        left: null,
      };
      this.isOpenedTableTooltip = false;
    },
  },
  mounted() {
    if (window.location.hash === "#parts") {
      this.viewBy = "part";
      this.listBarPlaceHolder = "Search part ID";
    } else {
      window.location.hash = "#products";
      this.viewBy = "product";
      this.listBarPlaceHolder = "Search product name";
    }

    const today = moment().format("YYYY-MM-DD");
    const closedDate = localStorage.getItem("closedDate");

    if (closedDate && closedDate === today) {
      this.isViewedSaveAndMovePageAlertBoxToday = false;
    }

    this.getDemandCompliance();
  },
  watch: {
    selectedYear: {
      handler() {
        this.totalWeek = moment().year(this.selectedYear).weeksInYear();
      },
      deep: true,
      immediate: true,
    },
  },
};
</script>

<style scoped>
/* Table CSS Start */
::-webkit-scrollbar {
  height: 10px;
  margin-top: 6px;
  border: 1px solid #d6dade;
}

.dem-cpl-table {
  table-layout: fixed;
  width: 100%;
  height: 100%;
  border: 1px solid #c9ced2;
}

.dem-cpl-table th {
  width: 169px;
  height: 34px;
  padding: 8px 16px;
  font-weight: 700;
  font-size: 14.66px;
  white-space: nowrap;
}

.dem-cpl-table td {
  font-weight: 400;
  font-size: 14.66px;
  padding-left: 16px;
  height: 34px;
}

.dem-cpl-table td,
.dem-cpl-table th {
  vertical-align: top;
}

.dem-cpl-table tr:nth-child(even) {
  background-color: #f2f2f2;
}

.dem-cpl-table th {
  position: sticky;
}

.dem-cpl-table th,
.dem-cpl-table td {
  text-align: -webkit-right;
  vertical-align: middle;
  color: #4b4b4b;
}

.dem-cpl-table th:first-child,
.dem-cpl-table td:first-child {
  position: sticky;
  left: 0;
  z-index: 3;
  border-right: 1px solid #c9ced2 !important;
  padding: 8px 16px;
}

.dem-cpl-table.parts-input-table th:first-child,
.dem-cpl-table.parts-input-table td:first-child {
  position: sticky;
  left: 0;
  border-right: none !important;
  padding: 8px 16px;
  z-index: 3;
}

.dem-cpl-table.parts-input-table th:nth-child(2),
.dem-cpl-table.parts-input-table td:nth-child(2) {
  position: sticky;
  left: 200px;
  border-right: 1px solid #c9ced2 !important;
  padding: 8px 16px;
  z-index: 2;
}

.dem-cpl-table tr:nth-child(odd) td:first-child,
.dem-cpl-table tr:nth-child(odd) th:first-child {
  background: #ffffff;
}

.dem-cpl-table.parts-input-table tr:nth-child(odd) td:nth-child(-n + 2),
.dem-cpl-table.parts-input-table tr:nth-child(odd) th:nth-child(-n + 2) {
  background: #ffffff;
}

.dem-cpl-table tr:nth-child(even) td:first-child {
  background: #f2f2f2;
}

.dem-cpl-table.parts-input-table tr:nth-child(even) td:nth-child(-n + 2) {
  background: #f2f2f2;
}

.dem-cpl-table th:first-child {
  border-right: none;
}

.dem-cpl-table td:first-child {
  border-right: none;
}

.input-dem-table-wrapper {
  width: 100%;
  height: auto;
  overflow-x: scroll;
  overflow-y: visible;
  padding-bottom: 6px;
  margin-bottom: 14px;
  flex-grow: 1;
}

.input-dem-truncate-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
  font-size: 15px;
  font-weight: 400;
}

.input-dem-table-tooltip-wrapper {
  width: fit-content;
  min-width: 24px;
  height: fit-content;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 12px;
  line-height: 14px;
  white-space: nowrap;
  color: #ffffff;
  padding: 6px 8px;
  position: absolute;
  border-radius: 3px;
  background: rgba(75, 75, 75, 0.9);
  z-index: 9999;
  transition: all 0.2s ease-in-out;
}

.input-dem-table-tooltip-wrapper:after {
  content: "";
  position: absolute;
  width: 0;
  height: 0;
  border: 6px solid transparent;
  border-right-color: rgba(75, 75, 75, 0.9);
  left: 0;
  top: 50%;
  border-left: 0;
  margin-top: -6px;
  margin-left: -6px;
}

/* Table CSS End */

/* Main Layout Start */
.input-dem-main-container {
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
  padding: 32px 20px 0 20px;
  justify-content: space-between;
}

.input-dem-main-container .input-dem-header {
  display: flex;
  width: 100%;
  flex-direction: column;
  padding-bottom: 16px;
}

.input-dem-main-container .input-dem-body {
  display: flex;
  width: 100%;
  height: auto;
  flex-direction: column;
}

.input-dem-header-top-bar {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
}

.input-dem-header-top-bar > div:last-child {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-dem-footer {
  width: 100%;
  display: flex;
  flex-direction: column;
  padding-bottom: 32px;
  gap: 14px;
}

.input-dem-footer > div {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  width: 100%;
}

.set-position {
  position: relative;
}

.input-dem-header-nav {
  width: 100%;
  display: flex;
  align-items: center;
  padding-bottom: 24px;
}

/* Main Layout End */
</style>
