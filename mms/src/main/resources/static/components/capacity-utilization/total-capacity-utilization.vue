<template>
  <div>
    <div class="row">
      <div class="col-lg-12">
        <div class="card">
          <div class="card-header">
            <strong v-text="resources['capacity_utilization']"></strong>
            <div class="card-header-actions">
              <span class="card-header-action">
                <!-- <small class="text-muted">Total</small> -->
              </span>
            </div>
          </div>
          <div class="card-body">
            <input type="hidden" />

            <div class="form-row">
              <div class="col-md-12 col-12">
                <div class="form-row">
                  <div class="col-xl mb-3 mb-md-0 col-12">
                    <common-button
                        placeholder="Select Parts"
                        :title="all_parts"
                        @click="handleToggle"
                        :is-show="showAllParts"

                    >

                    </common-button>
                    <common-select-popover
                        :is-visible="showAllParts"
                        @close="closeAllPartsFilter">
                      <common-select-dropdown
                          id="parts-dropdown"
                          :class="{show:showAllParts}"
                          :style="{position:'static'}"
                          :items="allPartsItems"
                          :placeholeder="resources['all_parts']"
                          :searchbox="false"
                          class="dropdown"
                          :set-result="setResultforDropDown"
                          :click-handler="handlerForAllParts"

                      ></common-select-dropdown>

                    </common-select-popover>
                    <!--                    <div class="input-group">-->
                    <!--                      <div class="input-group-prepend" style="width: 13%">-->
                    <!--                        <div class="input-group-text" style="width: 100%">-->
                    <!--                          <i class="fa fa-search"></i>-->
                    <!--                        </div>-->
                    <!--                      </div>-->
                    <!--                      <cycle-filters-->
                    <!--                      v-if = "Object.entries(resources).length"-->
                    <!--                        :option-array-productivity="listPart"-->
                    <!--                        :width="87"-->
                    <!--                        :backgrounds="`white`"-->
                    <!--                        filter-dialog="true"-->
                    <!--                        :cycle-placeholder="resources['all_parts']"-->
                    <!--                        @changepartime="productivityQuery.partName=$event"-->
                    <!--                        :resources="resources"></cycle-filters>-->
                    <!--                    </div>-->
                  </div>

                  <div class="col-xl mb-3 mb-md-0">
                    <common-button
                        placeholder="Select Supplier"
                        :title="supplierTitle"
                        @click="handleToggleSupplier"
                        :is-show="showSelectSupplier"
                    >

                    </common-button>
                    <common-select-popover
                        :is-visible="showSelectSupplier"
                        @close="closeSelectSupplier">
                      <common-select-dropdown
                          id="select-suppliers"
                          :class="{show:showSelectSupplier}"
                          :style="{position:'static'}"
                          :items="listSearch.supplier"
                          :checkbox="true"
                          :searchbox="false"
                          class="dropdown"
                          :set-result="selectedSupplier"


                      ></common-select-dropdown>

                    </common-select-popover>
<!--                    <location-filter v-if = "Object.entries(resources).length"-->
<!--                                     :title="resources['supplier']" type="supplier" :title-placeholder="resources['all_suppliers_normal']"-->
<!--                                     :option-array="listSearch.supplier" all-type="All suppliers"-->
<!--                                     @operatingstatus='productivityQuery.Suppliers = $event' :resources="resources">-->
<!--                    </location-filter>-->
                    <!--                    <location-filter v-if = "Object.entries(resources).length"-->
                    <!--                    :title="resources['country']" type="countries" :title-placeholder="resources['all_countries']"-->
                    <!--                    :option-array="listSearch.countries" all-type="All countries"-->
                    <!--                    @operatingstatus='productivityQuery.Countries = $event' :resources="resources">-->
                    <!--                    </location-filter>-->
                  </div>

                  <div class="col-xl mb-3 mb-md-0">
                    <common-button
                        placeholder="Select days"
                        :title="selected_last_days"
                        @click="handleToggleSelectDays"
                        :is-show="showSelectLastDays"
                    >

                    </common-button>
                    <common-select-popover
                        :is-visible="showSelectLastDays"
                        @close="closeSelectDaysDropDown">
                      <common-select-dropdown
                          id="select-days"
                          :class="{show:showSelectLastDays}"
                          :style="{position:'static'}"
                          :items="sortedTooling"
                          :checkbox="false"
                          :searchbox="false"
                          class="dropdown"
                          :set-result="()=>{}"
                          :click-handler="handleSelectDays"

                      ></common-select-dropdown>

                    </common-select-popover>
                    <!--                    <cycle-filters v-if = "Object.entries(resources).length"-->
                    <!--                    :option-array-productivity="sortedTooling"-->
                    <!--                    :width="100"-->
                    <!--                    :backgrounds="`white`"-->
                    <!--                    filter-dialog="false"-->
                    <!--                    @changepartime="productivityQuery.lastdays=$event"-->
                    <!--                    :resources="resources"></cycle-filters>-->
                  </div>

                  <div class="col-xl">
                    <common-button
                        placeholder="Select Status"
                        :title="statusTitle"
                        @click="handleToggleFilterStatus"
                        :is-show="showSelectFilterStatus"
                    >

                    </common-button>
                    <common-select-popover
                        :is-visible="showSelectFilterStatus"
                        @close="closeSelectFilterStatus">
                      <common-select-dropdown
                          id="select-status"
                          :class="{show:showSelectFilterStatus}"
                          :style="{position:'static'}"
                          :items="listSearch.location"
                          :checkbox="true"
                          :searchbox="false"
                          class="dropdown"
                          :set-result="selectedFilterStatus"


                      ></common-select-dropdown>

                    </common-select-popover>
<!--                    <location-filter v-if = "Object.entries(resources).length"-->
<!--                                     :title="resources['filter_status']" type="status" :title-placeholder="resources['all_statuses']"-->
<!--                                     :option-array="listSearch.location" all-type="All statuses"-->
<!--                                     @operatingstatus='productivityQuery.Operatingstatus = $event' :resources="resources">-->
<!--                    </location-filter>-->
                  </div>

                  <div class="col-xl">
                    <common-button
                        placeholder="Select Compare by"
                        :title="compare_by"
                        @click="handleToggleCompareBy"
                        :is-show="showCompareBy"
                    >

                    </common-button>
                    <common-select-popover
                        :is-visible="showCompareBy"
                        @close="closeCompareByDropDown">
                      <common-select-dropdown
                          id="compare-by"
                          :class="{show:showCompareBy}"
                          :style="{position:'static'}"
                          :items="listCompare"
                          :checkbox="false"
                          :searchbox="false"
                          class="dropdown"
                          :set-result="()=>{}"
                          :click-handler="handleCompareBy"

                      ></common-select-dropdown>

                    </common-select-popover>
                    <!--                    <cycle-filters-->
                    <!--                    v-if = "Object.entries(resources).length"-->
                    <!--                      :option-array-productivity="listCompare"-->
                    <!--                      :width="100"-->
                    <!--                      :backgrounds="`white`"-->
                    <!--                      filter-dialog="false"-->
                    <!--                      :cycle-placeholder="`Compare by tooling`"-->
                    <!--                      @changepartime="productivityQuery.Compare=$event"-->
                    <!--                      :resources="resources"></cycle-filters>-->
                  </div>

                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <capacity-utilization-summary
        v-if = "Object.entries(resources).length"
        :change-productivity="changeProductivity"
        :gettooling="gettooling"
        :query="query"
        :tooling-query="toolingQuery"
        :productivity-Query="productivityQuery"
        :items="items"
        :productivity-data="productivity"
        :resources="resources"
        :show-file-previewer="showFilePreviewer"
    ></capacity-utilization-summary>
  </div>
</template>

<script>
module.exports = {
  props: {
    changeProductivity: [Array, Object],
    gettooling: Array,
    query: Function,
    toolingQuery: Function,
    productivityQuery: Object,
    items: Array,
    resources: Object,
    showFilePreviewer: Function,
  },
  components: {
    'cycle-filters': httpVueLoader('/components/cycle-time/cycle-time-filters.vue'),
    'location-filter': httpVueLoader('/components/cycle-time/location-filter.vue'),
    'capacity-utilization-summary': httpVueLoader('./capacity-utilization-summary.vue')
  },
  data() {
    return {
      supplierTitle: "",
      statusTitle: "",
      isHover:false,
      showSelectFilterStatus:false,
      showSelectSupplier:false,
      supplierList:[],
      isHoverCompare:false,
      isHoverSelect:false,
      all_parts:vm.resources['all_parts'],
      allPartsItems: [],
      showCompareBy: false,
      selected_last_days: vm.resources['last_seven'],
      compare_by: vm.resources['compare_by_tooling'],
      showSelectLastDays: false,
      showAllParts: false,
      titleTooling: "",
      productivity: {},
      listPart: {},
      listCodes: {},
      listCountryCode: {},
      listContinentName: {},
      itemid: "",
      listTooling: {},
      listToolingName: {},
      paramCountries: '',
      show: false,
      listCompare: [
        {
          id: 'TOOL',
          title: vm.resources['compare_by_tooling'],
          key: 'Tool'
        },
        {
          id: 'SUPPLIER',
          title: vm.resources['compare_by_supplier'],
          key: 'Supplier'
        },
        {
          id: 'TOOLMAKER',
          title: vm.resources['compare_by_toolmaker'],
          key: 'Toolmaker'
        }
      ],
      allPart: {
        id: 'null',
        name: vm.resources['all_parts']
      },
      allCountryCode: {
        code: 'null',
        title: vm.resources['all_countries']
      },
      allContinentName: {
        code: 'null',
        title: vm.resources['all_continents']
      },
      listSearch: {
        countries: [],
        supplier: [],
        location: []
      },
      sortedTooling: [
        {
          id: '7',
          title: vm.resources['last_seven']
        },
        {
          id: '30',
          title: vm.resources['last_three']
        },
        {
          id: '90',
          title: vm.resources['last_nine']
        },
        {
          id: '180',
          title: vm.resources['last_one']
        }
      ]
    };
  },
  created() {
    this.changePart();
    this.changeCodes();
    this.changeCountries();
  },
  methods: {

    handleToggleFilterStatus(isShow){
      this.showSelectLastDays=false;
      this.showSelectSupplier=false;
      this.showAllParts=false;
      this.showCompareBy=false;
      this.showSelectFilterStatus=isShow;
    },

    closeSelectFilterStatus(){
      this.showSelectFilterStatus=false;
    },

    selectedFilterStatus(items){
      console.log('selectedFilterStatus', items)
      this.productivityQuery.moldStatusList=items;
      if(items.length==this.listSearch.location.length){
        this.statusTitle= "All Statuses"
      }
      else if(items.length<=0){
        this.statusTitle= ""
      }
      else{
        this.statusTitle= items.length+" "+vm.resources['filter_status']
      }
    },
    handleToggleSupplier(isShow){
      this.showSelectLastDays=false;
      this.showAllParts=false;
      this.showCompareBy=false;
      this.showSelectFilterStatus=false;
      this.showSelectSupplier=isShow;
    },

    closeSelectSupplier(){
      this.showSelectSupplier=false;
    },

    selectedSupplier(items){
      this.productivityQuery.Suppliers=items;
      if(items.length==this.listSearch.supplier.length){
        this.supplierTitle= "All Suppliers"
      }
      else if(items.length<=0){
        this.supplierTitle= ""
      }
      else{
        this.supplierTitle= items.length+" "+vm.resources['supplier']
      }
    },
    showallPartsFilter: function () {

      this.showAllParts = true;

    },
    closeAllPartsFilter: function () {

      this.showAllParts = false;
    },
    showSelectDaysFilter: function () {

      this.showSelectLastDays = true;

    },
    handleCompareBy(item) {
      this.compare_by = item.title;
      this.productivityQuery.Compare = {id: item.id, name: item.name, key: item.key};
      this.showCompareBy=false;

    },
    closeSelectDaysDropDown: function () {
      this.showSelectLastDays = false;
    },
    handleToggle: function (isShow) {
      this.showAllParts=isShow;
      this.showSelectSupplier=false;
      this.showCompareBy=false;
      this.showSelectFilterStatus=false
      this.showSelectLastDays=false;

    },
    closeCompareByDropDown() {
      this.showCompareBy = false;
    },
    handleToggleCompareBy(isShow) {
      this.showSelectLastDays=false;
      this.showSelectSupplier=false;
      this.showAllParts=false;
      this.showSelectFilterStatus=false;
      this.showCompareBy=isShow;
    },
    handleToggleSelectDays(isShow) {
      this.showSelectLastDays=isShow;
      this.showSelectSupplier=false;
      this.showAllParts=false;
      this.showCompareBy=false;
      this.showSelectFilterStatus=false;
      // this.showAllParts=false;
      // this.showCompareBy=false;
      // if (!this.showSelectLastDays) {
      //   // alert(this.ShowSortDirection +'handle second');
      //
      //   this.showSelectDaysFilter();
      //
      // } else {
      //
      //   this.closeSelectDaysDropDown();
      // }
    },
    handleSelectDays(item) {
      this.selected_last_days = item.title;
      this.productivityQuery.lastdays = {id: item.id, name: item.title};
      this.showSelectLastDays=false;
      console.log(item)
    },
    handlerForAllParts(item){
      this.all_parts=item.title;
      this.showAllParts=false;
      this.productivityQuery.partName = item;
    },
    setResultforDropDown(items) {
      // let temp = [];
      // items.forEach((item) => {
      //   temp.push({
      //     id: item.id,
      //     code: item.code ? item.code : 'null',
      //     name: item.name,
      //     type: item.type ? item.type : 'null'
      //   })
      // });
      // console.log(temp)
      // this.productivityQuery.partName = temp[0];
    },
    changTooLing(item) {
      this.itemid = item;
      let param = null;
      if (item === 1) {
        this.titleTooling = "Utilized Capacity";
        param = 'PRODUCTIVITY';
      }
      if (item === 2) {
        this.titleTooling = "Produced Amount";
        param = 'PRODUCED_QUANTITY';
      }
      if (item === 3) {
        this.titleTooling = "Remaining Capacity";
        param = 'AVAILABLE_PRODUCTIVITY';
      }
      if (item === 4) {
        this.titleTooling = "Overall Trend";
        param = 'TREND';
      }
      this.show = true
      this.toolingQuery('variable', param);
    },
    changePart() {
      /*
            axios.get("/api/chart/dashboard-data").then((response) => {
              this.listPart = response.data["list-search"].Part
              this.listPart.unshift(this.allPart)
            })
      */
      axios.get('/api/chart/list-search').then(reponse => {
        this.listPart = reponse.data.Part
        this.listPart.unshift(this.allPart)
        this.listPart.forEach((item) => {
          this.allPartsItems.push({
            id: item.id,
            code: item.code ? item.code : 'null',
            name: item.name,
            type: item.type ? item.type : 'null',
            title: item.name
          })
        })
      })
    },
    changeCountries (partId) {
      if(partId === 'null') {
        partId = null
      }
      this.paramCountries = 'partId=' + partId
      if (!partId) {
        delete this.paramCountries
      }
      // axios.get(`/api/parts/countries${this.paramCountries ? '?' + this.paramCountries: '' }`).then(reponse => {
      //   console.log(reponse, 'reponsereponsereponsereponse 222')
      //   this.listSearch.countries = reponse.data
      // })
      axios.get(`/api/companies?size=999999&query=&sort=id%2Cdesc&forReport=true`).then(reponse => {
        this.listSearch.supplier = reponse.data.content.map((item) => {
          return {
            id: item.id,
            code: item.companyCode,
            title: item.name
          }
        })
      })
    },
    changeCodes () {
      axios.get('/api/codes').then(reponse => {
        this.listSearch.location  = reponse.data.MoldStatus
      })
    },
  },
  watch: {
    'productivityQuery.lastdays' (item) {
      this.query('duration', item.id);
    },
    'productivityQuery.Compare' (item) {
      this.query('compareBy', item.id)
    },
    'productivityQuery.Countries' (items) {
      let contry_code = items.map(value => value.code)
      this.query('countryCode', contry_code)
    },
    'productivityQuery.Suppliers' (items) {
      let supplierIds = items.map(value => value.id)
      this.query('supplierIds', supplierIds)
    },
    'productivityQuery.partName' (item) {
      let partId = item.id
      this.changeCountries(partId)
      this.query('partId', partId)
    },
    'productivityQuery.Operatingstatus' (items) {
      let status = items.map(value => value.code)
      let operting_status = status.length === 0 ? 'null' : status
      console.log('productivityQuery.Operatingstatus', operting_status)
      this.query('operatingStatus', operting_status)
    },
    'productivityQuery.moldStatusList' (items) {
      let status = items.map(value => value.code)
      let operting_status = status.length === 0 ? 'null' : status
      console.log('productivityQuery.moldStatusList', operting_status)
      this.query('moldStatusList', operting_status)
    },
    gettooling (item) {
      let itemObject = this.items.filter((item) => item.id === this.itemid)[0];
      console.log('itemObject',itemObject)
      if(itemObject==null) return {};
      if (this.itemid === 1) {
        this.listTooling = item.map(item => {
          return {
            id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
            name: item[itemObject.key].toFixed(2) + '%'
          }
        })
      }else if (this.itemid === 4) {
        this.listTooling = item.map(item => {
          if(item[itemObject.key] === null) {
            return {
              id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
              name: 'N/A'
            }
          } else if (item[itemObject.key] === -999999) {
            return {
              id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
              name: 'INF'
            }
          } else {
            return {
              id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
              name: item[itemObject.key].toFixed(2) + '%'
            }
          }
        })
      } else {
        this.listTooling = item.map(item => {
          return {
            id: this.productivityQuery.Compare.key === 'Tool' ? item[itemObject.moldcode] : item[itemObject.name],
            name: item[itemObject.key].toLocaleString("en-US")
          }
        })
      }
    },
    changeProductivity (items) {
      let self = this
      self.productivity = items
      for (const i in self.items) {
        self.items[0]["total"] = self.productivity.avgProductivity.toFixed(2) + "%";
        self.items[1]["total"] = Number(self.productivity.totalProductivity).toLocaleString("en-US");
        self.items[2]["total"] = Number(self.productivity.availableProductivity).toLocaleString("en-US");
        self.items[3]["total"] = self.productivity.trend === null ? 'N/A' : self.productivity.trend.toFixed(2) + "%";
      }
    }
  },
};
</script>


<style scoped>
@media (max-width: 576px) {
  .statistical-item-container {
    width: 100%;
    height: 109px;
    min-width: 220px;
    border-radius: 10px;
    display: flex;
    justify-content: space-between;
    padding: 0px 26px;
    color: black;
    margin-top: 20px;
    align-items: center;
    margin-bottom: 5px;
    -moz-box-shadow: 3px 3px #e8e8e8;
    -webkit-box-shadow: 3px 3px #e8e8e8;
    box-shadow: 3px 3px #e8e8e8;
  }
}
@media (min-width: 576px) {
  .statistical-item-container {
    width: 23%;
    margin: 1%;
    height: 109px;
    min-width: 220px;
    border-radius: 10px;
    display: flex;
    justify-content: space-between;
    padding: 0px 26px;
    background: white;
    color: black;
    border: 0.5px solid #e8e8e8;
    align-items: center;
    margin-bottom: 5px;
    -moz-box-shadow: 3px 3px #e8e8e8;
    -webkit-box-shadow: 3px 3px #e8e8e8;
    box-shadow: 3px 3px 3px 3px #e8e8e8;
  }
}
/*::-webkit-scrollbar{*/
/*  width: 20px;*/
/* }*/
::-webkit-scrollbar-thumb {
  background: #c8ced3;
}
.total-value {
  font-size: 30px;
  /*font-weight: 900;*/
  /*font-family: Raleway;*/
  font-weight: 600;
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  line-height: 100%;

}
.total-title {
  font-size: 15px;
  font-weight: 600;
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
}
.custom-dropdown-search {
  background-color: white;
  background: white;
  width: 100%;
  box-shadow: 0 2px 0 rgb(0 0 0 / 2%);
  border: 1px solid #d9d9d9;
  padding-left: 0px !important;
  color: rgba(0,0,0,.65);
}
.ant-btn:hover {
  background-color: white;
  background: white !important;
  border: 1px solid #40a9ff !important;
  color: #40a9ff !important;
}
.ant-btn:focus {
  background-color: white;
  background: white !important;
  border: 1px solid #40a9ff !important;
}

.dropdown-text {
  margin-top: -25px;
  position: unset;
  float: left;
  margin-left: 46px;

}

.dropdown-wrap {
  min-width: unset !important;
  width: 100% !important;
  border-radius: 3px !important;
  padding: 2px !important;
}

.common-popover {
  min-width: unset !important;
  width: calc(100% - 12px);
  margin-left: -5px;
  margin-top: 3px;
  left: 12px;
  border-radius: 3px !important;
  padding: 2px !important;
}

.input-group-text {
  height: 100%;
}

.img-transition {
  float: right !important;
  margin-top: -26px;
}

.img-transition-select-days {
  float: right !important;
  margin-left: 7px;
}
/*svg:hover {*/
/*  fill: red;*/
/*}*/
/*svg:hover{*/
/*  fill:#40a9ff;*/
/*}*/
.dropdown-text-select-days {
  float: left;
  margin-left: 7px;
}
</style>