<template>
    <div>
      <notify-alert type="EFFICIENCY" @change-alert-generation="changeAlertGeneration" :resources="resources"></notify-alert>
  
      <div class="row">
        <div class="col-lg-12" v-click-outside="hideCalendarPicker">
          <div class="card wave" id="removee" style="height: 47px"> </div>
          <div class="card wave">
            <div class="card-header">
              <strong v-text="resources['uptime']"></strong>
  
              <div class="card-header-actions">
                              <span class="card-header-action">
                                  <small v-show="alertOn" class="text-muted"><span  v-text="resources['total']+':'"></span> {{total}}</small>
                              </span>
              </div>
            </div>
            <div class="card-body">
              <input type="hidden" v-model="requestParam.sort" />
  
              <div class="form-row">
                <div class="mb-3 mb-md-0 col-12 ">
                  <common-searchbar
                    :placeholder="resources['search_by_columns_shown']"
                    :request-param="requestParam"
                    :on-search="search"
                  ></common-searchbar>
                </div>
              </div>
            </div>
          </div>
  
        </div>
      </div>
  
      <div class="row">
        <div class="col-lg-12">
          <div class="card wave1">
            <div class="card-header" style="display:none">
              <i class="fa fa-align-justify"></i> <span v-text="resources['striped_tbl']"></span>
            </div>
            <div class="card-body">
  
              <ul class="nav nav-tabs" style="margin-bottom: -1px">
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status == 'alert'}" href="#"
                     @click.prevent="tab('alert')"><span v-text="resources['alert']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'alert'">{{ total }}</span>
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status == 'L1'}" href="#"
                     @click.prevent="tab('L1')"><span v-text="resources['outside_l1']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'L1'">{{ total }}</span>
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status == 'L2'}" href="#"
                     @click.prevent="tab('L2')"><span v-text="resources['outside_l2']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'L2'">{{ total }}</span>
                  </a>
                </li>
                <li class="nav-item dropdown">
                  <a class="nav-link" :class="{active: requestParam.status == 'confirmed'}" href="#"
                     @click.prevent="tab('confirmed')"><span v-text="resources['history_log']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'confirmed'">{{ total }}</span>
                  </a>
                </li>
  
                <customization-modal
                    v-show="alertOn"
                    style="position: absolute; right: 20px"
                    :all-columns-list="allColumnList"
                    @save="saveSelectedList"
                    :resources="resources"
                />
              </ul>
  
              <div v-show="alertOn">
  
                <table class="table table-responsive-sm table-striped">
  
                  <thead id="thead-actionbar" class="custom-header-table">
                  <tr :class="{'invisible': listChecked.length != 0}" style="height: 57px" class="tr-sort">
                    <th v-if="requestParam.status !== 'confirmed'" style="width: 40px; vertical-align: middle!important;">
                      <input style="vertical-align: middle!important;" type="checkbox" v-model="isAll" @change="select" />
                    </th>
                    <template
                        v-for="(column, columnIndex) in allColumnList"
                        v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <template v-if="requestParam.status !== 'confirmed' &&
                      (column.field === sortType.GOALS || column.field === sortType.UPTIME || column.field === sortType.VARIANCE ||column.field === sortType.STATUS)">
                        <th
                            :key="columnIndex"
                            :class="[{__sort: column.sortable,active: currentSortType === column.sortField,},isDesc ? 'desc' : 'asc', column.label === 'OP' || column.label === 'Status'? '': 'text-left',]"
                            @click="sortBy(column.sortField)"
                        >
                          <span>{{ column.label }}</span>
                          <span
                              v-if="column.sortable"
                              class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <template v-if="requestParam.status === 'confirmed' && column.field === sortType.CONFIRM_DATE">
                        <th
                            :key="columnIndex"
                            :class="[{__sort: column.sortable,active: currentSortType === column.sortField,},isDesc ? 'desc' : 'asc', column.label === 'OP' || column.label === 'Status'? '': 'text-left',]"
                            @click="sortBy(column.sortField)"
                        >
                          <span>{{ column.label }}</span>
                          <span
                              v-if="column.sortable"
                              class="__icon-sort"
                          ></span>
                        </th>
                      </template>
                      <th
                          :key="columnIndex"
                          :class="[{__sort: column.sortable,active: currentSortType === column.sortField,},isDesc ? 'desc' : 'asc', column.label === 'OP' || column.label === 'Status'? '': 'text-left',]"
                          @click="sortBy(column.sortField)"
                          v-else-if="column.field !== sortType.UPTIME && column.field !==sortType.GOALS
                          && column.field !==sortType.VARIANCE && column.field !==sortType.CONFIRM_DATE
                          && column.field !==sortType.STATUS"
                      >
                        <span>{{ column.label }}</span>
                        <span
                            v-if="column.sortable"
                            class="__icon-sort"
                        ></span>
                      </th>
                    </template>
                    <!-- <th class="text-center " v-if="requestParam.status != 'confirmed'">Action</th> -->
                    <th class="text-center " v-if="requestParam.status === 'confirmed'"><span v-text="resources['action']"></span></th>
                  </tr>
  
                  <tr :class="{'d-none zindexNegative': listChecked.length === 0}" id="action-bar" class="tr-sort empty-tr">
                    <th class="empty-th">
                      <div class="first-checkbox-zone2" style="left: unset!important;">
                        <div class="first-checkbox2" style="display: flex;align-items: center;padding-left: 0;width: 110px;">
                          <div>
                            <input id="checkbox-all-2" class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                          </div>
                        </div>
                        <div class="action-bar">
                          <div v-if="listChecked.length > 1">
                            No batch action is available
                          </div>
  
                          <div v-if="listChecked.length === 1" class="action-item" v-click-outside="closeShowDropDown">
                            <div class="change-status-dropdown drowdown d-inline">
  <!--                            <div title="Change status" class="change-status"  @click.prevent="changeStatus(listCheckedFull[0], 'CONFIRMED')">
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>-->
                              <div title="Change status" class="change-status"  @click="showDropdown = !showDropdown">
                                <span v-text="resources['change_status']">Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                <li>
                                  <a href="#" class="dropdown-item" @click.prevent="changeStatus(listCheckedFull[0], 'CONFIRMED')">Confirm Alert</a>
                                </li>
                              </ul>
  
                              <!-- <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                  <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li>
                                  <li>
                                      <a href="#" class="dropdown-item" @click.prevent="changeStatus(result, 'CONFIRMED')">Confirm</a>
                                  </li>
                              </ul> -->
                            </div>
                          </div>
  
                          <div v-if="listChecked.length === 1" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                              v-if="listChecked.length === 1"
                              class="action-item"
                              @click="showCreateWorkorder(listCheckedFull[0])"
                              style="align-items: flex-start"
                          >
                            <span>Create Workorder</span>
                            <img src="/images/icon/icon-workorder.svg" style="margin-left: 4px; height: 15px; width: 15px"/>
                          </div>
                        </div>
                      </div>
                  </thead>
  
                  <tbody class="op-list" style="display:none">
                  <tr v-show="!isLoading" v-for="(result, index, id) in results" :id="result.id">
                    <td  v-if="requestParam.status !== 'confirmed'">
                      <template v-if="listChecked.length >= 0">
                        <input @click="check" :checked="checkSelect(result.id)" class="checkbox"
                               type="checkbox" :value="result.id">
                      </template>
                    </td>
                    <template
                        v-for="(column, columnIndex) in allColumnList"
                        v-if="column.enabled && !column.hiddenInToggle"
                    >
                      <td class="text-left" v-if="column.field === sortType.ID">
                        <a href="#" @click.prevent="showChart(result.mold)" style="color:#1aaa55"
                           class="mr-1"><i class="fa fa-bar-chart"></i></a>
                        <a href="#" @click.prevent="showMoldDetails(result.mold)">
                          {{result.mold.equipmentCode}}
                        </a>
                      </td>
                      <td  class="text-left" v-else-if="column.field === sortType.COMPANY">
                        <div>
                          <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.mold.companyIdByLocation)">
                            {{result.mold.companyName}}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">{{result.mold.companyTypeText}}</span>
                      </td>
                      <td  class="text-left" v-else-if="column.field === sortType.LOCATION">
                        <div v-if="result.mold.locationCode">
                          <div class="font-size-14">
                            <a href="#"
                               @click.prevent="showLocationHistory(result.mold)">{{result.mold.locationName}}</a>
                          </div>
                          <span class="font-size-11-2 small text-muted">{{result.mold.locationCode}}</span>
                        </div>
                      </td>
                      <td style="vertical-align: middle!important;"  class="text-center op-status-td" v-else-if="column.field === sortType.UPTIME_STATUS">
                                          <span class="label label-warning"
                                                v-if="result.efficiencyStatus === 'OUTSIDE_L1'" v-text="resources['outside_l1']"> </span>
                        <span class="label label-danger"
                              v-if="result.efficiencyStatus === 'OUTSIDE_L2'" v-text="resources['outside_l2']"></span>
                      </td>
  
                      <td  class="text-left" v-else-if="requestParam.status !== 'confirmed' && column.field === sortType.GOALS">
                        {{result.baseEfficiency}}
                        <div>
                          <span class='font-size-11-2'>%</span>
                        </div>
                      </td>
  
                      <td  class="text-left" v-else-if="requestParam.status !== 'confirmed' && column.field === sortType.UPTIME">
                        <a href="#" @click.prevent="showEfficiencyHistory(result.mold)">
                          <strong>{{result.efficiency.toFixed(2)}}</strong>
                          <div>
                            <span class='font-size-11-2'>%</span>
                          </div>
                        </a>
                      </td>
  
                      <td  class="text-left" v-else-if="requestParam.status !== 'confirmed' && column.field === sortType.VARIANCE">
                        <strong>{{result.variance.toFixed(2)}}</strong>
                        <div>
                          <span class='font-size-11-2'>%</span>
                        </div>
                      </td>
  
                      <td class="text-left" v-else-if="column.field === sortType.DATE">
                        {{result.notificationDateTime}}
                      </td>
  
                      <td  class="text-left" v-else-if="requestParam.status === 'confirmed' && column.field === sortType.CONFIRM_DATE">
                                      <span >
                                          {{convertDate(result.confirmedAtShow)}}
                                      </span>
                        <div>
                                          <span class='font-size-11-2'>
                                            {{convertTimeString(result.confirmedAtShow)}}
                                          </span>
                        </div>
                      </td>
  
                      <td style="vertical-align: middle!important;" class="text-center op-status-td" v-else-if="column.field === sortType.OP">
                                          <span
                                              v-if="result.mold.operatingStatus === 'WORKING' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK' )"
                                              class="op-active label label-success"></span>
                        <span
                            v-if="result.mold.operatingStatus === 'IDLE' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK' )"
                            class="op-active label label-warning"></span>
                        <span
                            v-if="result.mold.operatingStatus === 'NOT_WORKING' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK' )"
                            class="op-active label label-inactive"></span>
                        <span
                            v-if="result.mold.operatingStatus === 'DISCONNECTED' && (result.mold.equipmentStatus === 'INSTALLED' || result.mold.equipmentStatus === 'CHECK' )"
                            class="op-active label label-danger"></span>
                      </td>
                      <td style="vertical-align: middle!important;" class="text-center op-status-td" v-if="requestParam.status !== 'confirmed' && column.field === sortType.STATUS">
                        <span class="label label-danger" v-if="result.notificationStatus === 'ALERT'" v-text="resources['alert']"></span>
                      </td>
                      <td
                          class="text-left"
                          v-else-if="column.field === 'machineCode'">
                        <div v-if="result.machine">
                          <a href="#" @click.prevent="showMachineDetails(result.machine.id)">
                            {{result.machine.machineCode}}
                          </a>
                        </div>
                      </td>
                    </template>
                    <td style="vertical-align: middle!important;" class="text-center" v-if="requestParam.status === 'confirmed'">
                      <a href="#" @click.prevent="showMessageDetails(result.mold)" title="View"
                         v-if="result.notificationStatus === 'CONFIRMED'">
                        <img height="18" src="/images/icon/view.png" alt="View" />
                      </a>
                      <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note">
                        <img src="/images/icon/note.svg" alt="Add note" />
                      </a>
                    </td>
                  </tr>
                  </tbody>
                </table>
  
                <div class="no-results d-none" v-text="resources['no_results']"></div>
  
                <div v-show="!isLoading" class="row">
                  <div class="col-md-10 col-sm-10 col-xs-10">
                    <ul class="pagination">
                      <li v-for="(data, index) in pagination" class="page-item"
                          :class="{active: data.isActive}">
                        <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                      </li>
                    </ul>
                  </div>
                  <!-- <div class="col-md-2 col-sm-2 col-xs-2">
                      <div v-on:click="changeStatus(null)" v-if="hasChecked()" class="col-auto ml-auto"  style="display: flex; justify-content: flex-end">
                          <a style="color: #fff" class="btn btn-success" v-text="resources['confirm']"></a>
                      </div>
                  </div> -->
                </div>
              </div>
              <div v-show="!alertOn" class="card-body empty-wrap">
                <div class="square-empty">
                </div>
                <div class="empty-wrap-caption"><span v-text="resources['alert_turn_off_message_r1']"></span></div>
                <div class="empty-wrap-sub"><span v-text="resources['alert_turn_off_message_r2']"></span></div>
              </div>
            </div>
  
          </div>
        </div>
      </div>
  
      <efficiency-confirm-history :resources="resources" :alert-type="alertType"></efficiency-confirm-history>
      <efficiency-history :resources="resources" :alert-type="alertType"></efficiency-history>
  
    </div>
  </template>
  
  <script>
  module.exports = {
    name: "mold-efficiency",
    components:{
      'efficiency-history': httpVueLoader('/components/efficiency-history.vue'),
      'efficiency-confirm-history': httpVueLoader('/components/efficiency-confirm-history.vue'),
  
    },
    props: {
      resources: Object,
      showCreateWorkorder: Function,
    },
    data() {
      return {
        showDropdown: false,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        showDrowdown: null,
        isAllTracked: [],
        'results': [],
        'total': 0,
        'pagination': [],
        'requestParam': {
  
          'query': '',
          'status': 'alert',
          'extraStatus': 'op-status-is-not-null',
          'sort': 'id,desc',
          'page': 1,
  
          'operatingStatus': '',
          'notificationStatus': ''
        },
  
        'statusCodes': [],
        'listCategories': [],
        sortType: {
          ID: 'mold.equipmentCode',
          COMPANY: 'mold.location.company.name',
          LOCATION: 'mold.location.name',
          UPTIME_STATUS: 'efficiencyStatus',
          GOALS: 'mold.uptimeTarget',
          UPTIME: 'efficiency',
          OP: 'mold.operatingStatus',
          VARIANCE: 'variance',
          DATE: 'notificationAt',
          STATUS: 'notificationStatus',
          CONFIRM_DATE: 'confirmedAt'
        },
        currentSortType: 'id',
        isDesc: true,
        isLoading: false,
        alertType: 'DAILY',
        alertTypeOptions: {
          DAILY: 'DAILY',
          WEEKLY: 'WEEKLY',
          MONTHLY: 'MONTHLY'
        },
        alertOn:true,
        listCheckedFull: [],
        cancelToken: undefined,
        allColumnList: [],
        pageType: 'EFFICIENCY_ALERT',
      }
    },
    watch: {
  /*
      resources(newVal, oldVal) {
        if (newVal && Object.entries(newVal).length > 0 && (oldVal == null || Object.entries(oldVal).length == 0)) {
          console.log('change resources');
          this.callSetAllColumnList();
        }
      },
  */
      listChecked (newVal) {
        if (newVal && newVal.length != 0) {
          const self = this
          console.log(this.results, 'xxx')
          console.log(self.listChecked, 'xxx')
          const x = this.results.filter(item => self.listChecked.indexOf(item.id) > -1)
          console.log(x, 'xxx')
          this.listCheckedFull = x;
        }
      },
    },
    created () {
    },
    methods: {
      showMachineDetails: function (machineId) {
        var child = Common.vue.getChild(this.$parent.$children, 'machine-details');
        if (child != null) {
          child.showDetailsById(machineId);
        }
      },
      saveSelectedList: function (dataCustomize, list) {
        const dataFake = list.map((item, index) => {
          if (item.field) {
            return { ...item, position: index }
          }
        });
        this.allColumnList = dataFake
        const self = this
        let data = list.map((item, index) => {
          let response = {
            columnName: item.field,
            enabled: item.enabled,
            position: index,
          };
          if (item.id) {
            response.id = item.id;
          }
          return response;
        });
        axios.post("/api/config/update-column-config", {
          pageType: this.pageType,
          columnConfig: data
        }).then((response) => {
          let hashedByColumnName = {};
          response.data.forEach(item => {
            hashedByColumnName[item.columnName] = item;
          });
          self.allColumnList.forEach(item => {
            if (hashedByColumnName[item.field] && !item.id) {
              item.id = hashedByColumnName[item.field].id;
              item.position = hashedByColumnName[item.field].position
            }
          });
        }).finally(() => {
          self.allColumnList.sort(function (a, b) {
            return a.position - b.position;
          });
          self.$forceUpdate();
        })
      },
      callSetAllColumnList() {
        if (this.resources && Object.entries(this.resources).length > 0 && this.allColumnList.length == 0) {
          this.setAllColumnList();
        }
      },
      setAllColumnList() {
        this.allColumnList = [
          { label: this.resources['tooling_id'], field: this.sortType.ID, mandatory: true, default: true, sortable: true, sortField: this.sortType.ID, defaultSelected: true, defaultPosition: 0 },
          { label: this.resources['company'], field: this.sortType.COMPANY, default: true, sortable: true, sortField: this.sortType.COMPANY, defaultSelected: true, defaultPosition: 1 },
          { label: this.resources['location'], field: this.sortType.LOCATION, default: true, sortable: true, sortField: this.sortType.LOCATION, defaultSelected: true, defaultPosition: 2 },
          { label: this.resources['uptime_status'], field: this.sortType.UPTIME_STATUS, default: true, sortable: true, sortField: this.sortType.UPTIME_STATUS, defaultSelected: true, defaultPosition: 3 },
          { label: this.resources['goals'], field: this.sortType.GOALS, default: true, sortable: true, sortField: this.sortType.GOALS, defaultSelected: true, defaultPosition: 4 },
          { label: this.resources['uptime'], field: this.sortType.UPTIME, default: true, sortable: true, sortField: this.sortType.UPTIME, defaultSelected: true, defaultPosition: 5 },
          { label: this.resources['variance'], field: this.sortType.VARIANCE, default: true, sortable: true, sortField: this.sortType.VARIANCE, defaultSelected: true, defaultPosition: 6 },
          { label: this.resources['alert_date'], field: this.sortType.DATE, default: true, sortable: true, sortField: this.sortType.DATE, defaultSelected: true, defaultPosition: 6 },
          { label: this.resources['op'], field: this.sortType.OP, default: true, sortable: true, sortField: this.sortType.OP, defaultSelected: true, defaultPosition: 6 },
          { label: this.resources['status'], field: this.sortType.STATUS, default: true, sortable: true, sortField: this.sortType.STATUS, defaultSelected: true, defaultPosition: 6 },
          { label: this.resources['confirmation_date'], field: this.sortType.CONFIRM_DATE, default: true, sortable: true, sortField: this.sortType.CONFIRM_DATE, defaultSelected: true, defaultPosition: 6 },
          { label: this.resources['machine_id'], field: 'machineCode', sortable: true, sortField: 'mold.machine.machineCode'},
  
        ];
        try {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
          Common.changeDeletedColumn(this, 'TOOLING', this.allColumnList, 'mold');
        } catch (e) {
          console.log(e);
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        }
      },
      resetColumnsListSelected() {
        this.allColumnList.forEach(item => {
          item.enabled = !!item.default;
          if (item.sortable && !item.sortField) {
            item.sortField = item.field
          }
        });
      },
      getColumnListSelected() {
        axios.get(`/api/config/column-config?pageType=${this.pageType}`).then((response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach(item => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach(item => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id
                item.position = hashedByColumnName[item.field].position
              }
            });
            this.allColumnList.sort(function (a, b) {
              return a.position - b.position;
            });
            this.$forceUpdate();
            let child = Common.vue.getChild(this.$children, 'show-columns');
            if (child != null) {
              child.$forceUpdate();
            }
          }
        })
            .catch(function (error) {
              // Common.alert(error.response.data);
            })
      },
      showCompanyDetailsById: function (company) {
        var child = Common.vue.getChild(this.$parent.$children, 'company-details');
        if (child != null) {
          child.showDetailsById(company);
        }
      },
      changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
        this.alertType = alertType;
        if(alertOn!=null){
          this.alertOn = alertOn;
          //Common.onOffNumAlertOnSidebar(alertOn,null);
        }
        console.log('this.alertOn', this.alertOn);
        if (specialAlertType != null) {
          this.requestParam.specialAlertType = specialAlertType
        }
        if (!isInit || specialAlertType!=null)
          this.paging(1);
      },
      changeShow(resultId) {
        if(resultId !=null) {
          this.showDrowdown = resultId
        } else {
          this.showDrowdown = null
        }
      },
      hideCalendarPicker (event) {
        if (event.toElement && event.toElement.localName != 'img') {
          this.changeShow(null)
        }
      },
      checkSelect: function (id) {
        const findIndex = this.listChecked.findIndex(value => {
          return value == id;
        });
        return findIndex !== -1;
      },
  
      hasChecked: function () {
        let isExistedChecked = false;
        Object.keys(this.listCheckedTracked).forEach(status => {
          Object.keys(this.listCheckedTracked[status]).forEach(page => {
            if (this.listCheckedTracked[status][page].length > 0) {
              isExistedChecked = true;
            }
          });
        });
        return isExistedChecked;
      },
  
      check: function (e) {
  
        if (e.target.checked) {
          this.listChecked.push(+e.target.value);
          Common.setHeightActionBar();
        } else {
          this.listChecked = this.listChecked.filter(value => value != e.target.value);
          if (this.listChecked.length === 0) {
            this.isAll = false;
            this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.requestParam.status);
          }
        }
        this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
      },
  
      select: function (event) {
        let isAll = event.target.checked;
        this.isAll = isAll;
        if (isAll) {
          this.listChecked = this.results.map(value => value.id);
          Common.setHeightActionBar();
          this.isAllTracked.push({
            page: this.requestParam.page,
            status: this.requestParam.status,
          });
        } else {
          this.listChecked = [];
          this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.requestParam.status);
        }
        this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
      },
  
      tab: function (status) {
        this.total=null;
        this.currentSortType = 'id';
        this.requestParam.sort = 'id,desc';
        this.requestParam.status = status;
        this.isAll = false;
        this.listChecked = [];
        this.listCheckedFull = [];
        this.paging(1);
      },
      search: function (page) {
        this.paging(1);
      },
  
  
      disable: function (user) {
        user.enabled = false;
        //this.save(user);
      },
  
      showChart: function (mold, tab) {
        console.log(mold);
        var child = Common.vue.getChild(this.$parent.$children, 'chart-mold');
        if (child != null) {
          child.showChart(mold, 'UPTIME', 'DAY', tab ? tab : 'switch-graph');
        }
  
  
      },
      showMoldDetails: function (mold) {
        // var child = Common.vue.getChild(this.$parent.$children, 'mold-details');
        // if (child != null) {
        //   child.showMoldDetails(mold);
        // }
        const tab = 'switch-detail'
        this.showChart(mold, tab)
      },
  
      //efficiency-history
  
      showEfficiencyHistory: function (mold) {
        var child = Common.vue.getChild(this.$children, 'efficiency-history');
        if (child != null) {
          child.showEffiencyHistory(mold, this.alertType);
        }
      },
      showLocationHistory: function (mold) {
        var child = Common.vue.getChild(this.$parent.$children, 'location-history');
        if (child != null) {
          child.showLocationHistory(mold);
        }
      },
      showMessageDetails: function (mold) {
        var child = Common.vue.getChild(this.$children, 'efficiency-confirm-history');
        if (child != null) {
          child.show(mold);
        }
      },
      closeShowDropDown () {
        this.showDropdown = false
      },
      changeStatus: function (dataParam, status) {
        var param = {
          'id': dataParam ? dataParam.id : null,
          'moldLocationStatus': status,
          'message': ''
        };
  
        var data = {
          'title': 'Confirm',
          'messageTitle': 'Message',
          'buttonTitle': 'Confirm',
  
          'url': '/api/molds/efficiency/' + param.id + '/confirm',
          'param': param
        };
        if (!dataParam) {
          // let listChecked = [];
          Object.keys(this.listCheckedTracked).forEach(status => {
            if (status === this.requestParam.status) {
              Object.keys(this.listCheckedTracked[status]).forEach(page => {
                if (this.listCheckedTracked[status][page].length > 0) {
                  listChecked = listChecked.concat(this.listCheckedTracked[status][page]);
                }
              });
            }
          });
          if (listChecked.length === 0) {
            return;
          }
          // open for all select
          data = {
            'title': 'Confirm',
            'messageTitle': 'Message',
            'buttonTitle': 'Confirm',
            'url': '/api/molds/efficiency/confirm',
            'param': listChecked.map(value => {
              return {
                'id': value,
                'message': ''
              };
            })
          };
        }
        var messageFormComponent = Common.vue.getChild(this.$parent.$children, 'message-form');
        if (messageFormComponent != null) {
          vm.callbackMessageForm = this.callbackMessageForm.bind(this);
          messageFormComponent.showModal(data);
        }
        // this.listChecked = [];
      },
  
      setListCategories: function () {
        for (var i = 0; i < this.results.length; i++) {
          var mold = this.results[i];
          if (mold.part != null) {
            if (typeof mold.part.category === 'object') {
              this.listCategories.push(mold.part.category);
            } else {
              var categoryId = mold.part.category;
  
              for (var j = 0; j < this.listCategories.length; j++) {
                var category = this.listCategories[j];
  
                if (categoryId == category.id) {
                  mold.part.category = category;
                  break;
                }
              }
            }
          }
        }
      },
  
      weekConvert: function (dateString) {
        return `Week ${dateString.substring(4, 6)} ${dateString.substring(0, 4)}`
      },
  
      paging: function (pageNumber) {
        Common.handleNoResults('#app', 1);
        var self = this;
        self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
        // checking is All
        this.isAll = false;
        let pageSelectedAll = this.isAllTracked.filter(item => item.status === this.requestParam.status).map(item => item.page);
        // if (pageSelectedAll.includes(this.requestParam.page)) {
        //     this.isAll = true;
        // }
        if (!this.listCheckedTracked[this.requestParam.status]) {
          this.listCheckedTracked[this.requestParam.status] = {};
        }
        if (!this.listCheckedTracked[this.requestParam.status][this.requestParam.page]) {
          this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = [];
        }
        var param = Common.param(self.requestParam);
        self.isLoading = true;
        if (typeof this.cancelToken != typeof undefined) {
          this.cancelToken.cancel('new request')
        }
        this.cancelToken = axios.CancelToken.source()
        axios.get('/api/molds/efficiency?lastAlert=true&' + param, {
          cancelToken: this.cancelToken.token
        }).then(function (response) {
          self.isLoading = false;
          self.total = response.data.totalElements;
          console.log('response: ', response)
          this.listChecked = [];
          self.results = response.data.content.map(value => {
            value.variance = (-value.baseEfficiency + value.efficiency) * 100 / value.baseEfficiency;
  
            value.confirmedAtShow = self.convertTimestampToDateWithFormat(value.confirmedAt,'YYYY-MM-DD HH:mm:ss');
            value.notificationDateTime = self.getNotificationDate(value.createdAt, value.periodType);
  
  
            return value;
          });
          self.pagination = Common.getPagingData(response.data);
          // if (self.listCheckedTracked[self.requestParam.status][self.requestParam.page]) {
          //     self.listChecked = self.listCheckedTracked[self.requestParam.status][self.requestParam.page];
          // }
  
          // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
          self.setResultObject();
  
          // 카테고리 정보 저장.
          self.setListCategories();
  
          Common.handleNoResults('#app', self.results.length);
  
          var pageInfo = self.requestParam.page == 1 ? '' : '?page=' + self.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
            .catch(function (error) {
              console.log(error);
              self.isLoading = false;
            });
      },
      getNotificationDate(timestamp, alertType) {
        timestamp = timestamp * 1000;
        if (alertType === this.alertTypeOptions.MONTHLY)
          return moment(timestamp).format('MMM YYYY');
        if (alertType === this.alertTypeOptions.WEEKLY) {
          let week = moment(timestamp).week();
          if (week < 10) {
            week = '0' + week;
          }
          return 'W-' + week + ', ' + moment(timestamp).subtract(1, "day").format('YYYY');
        }
        return moment(timestamp).subtract(1, "day").format('MMM DD, YYYY');
      },
      setResultObject: function () {
        // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
        for (var i = 0; i < this.results.length; i++) {
          if (typeof this.results[i].mold !== 'object') {
            var moldId = this.results[i].mold;
            this.results[i].mold = this.findMoldFromList(this.results, moldId);
          }
        }
      },
      convertDate: function(s) {
        return s?.slice(0, 10);
      },
  
      convertTimeString: function(s) {
        return s?.slice(10);
      },
  
      findMoldFromList: function (results, moldId) {
        for (var j = 0; j < results.length; j++) {
          if (typeof results[j].mold !== 'object') {
            continue;
          }
  
          var mold = results[j].mold;
          if (moldId == mold.id) {
            return mold;
            break;
          }
  
          // parts에서 검색
          if (mold.part != null && mold.part.molds != null) {
            var findMold = this.findMold(mold.part.molds, moldId);
            if (typeof findMold === 'object') {
              return findMold;
              break;
            }
          }
        }
      },
  
      findMold: function (results, moldId) {
        for (var j = 0; j < results.length; j++) {
          if (typeof results[j] !== 'object') {
            continue;
          }
  
          var mold = results[j];
          if (moldId == mold.id) {
            return mold;
            break;
          }
  
          // parts에서 검색
          if (mold.part != null && mold.part.molds != null) {
            var findMold = this.findMold(mold.part.molds, moldId);
            if (typeof findMold === 'object') {
              return findMold;
              break;
            }
          }
        }
      },
  
  
      efficiencyRatio: function (moldEfficiency) {
        return ((moldEfficiency.uptimeSeconds / moldEfficiency.currentSeconds) * 100).toFixed(1);
  
      },
      ratio: function (mold) {
        return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
      },
      ratioStyle: function (mold) {
        return "width: " + this.ratio(mold) + '%';
      },
      ratioColor: function (mold) {
        var ratio = this.ratio(mold);
  
        if (ratio < 25) return "bg-info";
        else if (ratio < 50) return "bg-success";
        else if (ratio < 75) return "bg-warning";
        else return "bg-danger";
  
      },
      callbackMessageForm: function () {
        this.resetAfterUpdate()
        this.paging(1);
        if(this.$parent && typeof this.$parent.reloadAlertCout === 'function'){
          this.$parent.reloadAlertCout();
        }
      },
      resetAfterUpdate(){
        this.listChecked = [];
        this.listCheckedFull = [];
      },
      sortBy: function (type) {
        if(this.currentSortType !== type) {
          this.currentSortType = type
          this.isDesc = true;
        } else {
          this.isDesc = !this.isDesc
        }
        if(type) {
          this.requestParam.sort = `${type},${this.isDesc ? 'desc' : 'asc'}`
          this.sort()
        }
      },
      sort: function() {
        this.paging(1);
      },
      showSystemNoteModal(result){
        var child = Common.vue.getChild(this.$parent.$children, 'system-note');
        if (child != null) {
          child.showSystemNote({id: result.moldId});
        }
      },
  
  
    },
    mounted() {
      this.$nextTick(function() {
        Common.removeWave(500);
      });
      // this.$nextTick(function () {
        // 모든 화면이 렌더링된 후 실행합니다.
        var self = this;
        var uri = URI.parse(location.href);
      let params = Common.parseParams(uri.query);
      if (params.dashboardRedirected) {
        self.requestParam.dashboardRedirected = params.dashboardRedirected;
      }
      this.callSetAllColumnList();
          //call list by update notification
        if (uri.fragment == 'outside_l1') {
          self.tab('L1');
  
        } else if (uri.fragment == 'outside_l2') {
          self.tab('L2');
        } else {
          // self.paging(1);
        }
      // })
    }
  }
  </script>
  
  <style scoped>
  
  </style>