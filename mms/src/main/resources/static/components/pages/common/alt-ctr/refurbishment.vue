<template>
    <div>
      <notify-alert type="REFURBISHMENT" @change-alert-generation="changeAlertGeneration" :resources="resources"></notify-alert>
  
      <div class="row">
        <div class="col-lg-12" v-click-outside="hideCalendarPicker">
          <div class="card wave" id="removee" style="height: 47px"> </div>
          <div class="card wave">
            <div class="card-header">
              <strong v-text="resources['refurbishment']"></strong>
  
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
                  <!-- <div class="input-group">
                    <div class="input-group-prepend">
                      <div class="input-group-text"><i class="fa fa-search"></i></div>
                    </div>
                    <input type="text" @input="requestParam.query = $event.target.value" @keyup="search"
                           class="form-control"
                           :placeholder="resources['search_by_columns_shown']">
                  </div> -->
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
                  <a class="nav-link" :class="{active: requestParam.status === 'alert'}" href="#"
                     @click.prevent="tab('alert')">
                                      <span>
                                          <span v-text="resources['alert']"></span>
                                          <span v-show="alertOn" class="badge badge-light badge-pill"
                                                v-if="requestParam.status === 'alert'">{{ total }}</span>
                                      </span>
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status === 'approved'}" href="#"
                     @click.prevent="tab('approved')">
                                      <span>
                                          <span v-text="resources['approved']"></span>
                                          <span v-show="alertOn" class="badge badge-light badge-pill"
                                                v-if="requestParam.status === 'approved'">{{ total }}</span>
                                      </span>
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status === 'disapproved'}" href="#"
                     @click.prevent="tab('disapproved')">
                                      <span>
                                          <span v-text="resources['disapproved']"></span>
                                          <span v-show="alertOn" class="badge badge-light badge-pill"
                                                v-if="requestParam.status === 'disapproved'">{{ total }}</span>
                                      </span>
                  </a>
                </li>
  
                <li class="nav-item">
                  <a class="nav-link" :class="{active: requestParam.status === 'history'}" href="#"
                     @click.prevent="tab('history')">
                                      <span>
                                          <span>History Log</span>
                                          <span v-show="alertOn" class="badge badge-light badge-pill"
                                                v-if="requestParam.status === 'history'">{{ total }}</span>
                                      </span>
                  </a>
                </li>
  
              </ul>
  
              <div v-show="alertOn">
  
                <table class="table table-responsive-sm table-striped">
  
                  <thead id="thead-actionbar" class="custom-header-table">
                  <tr :class="{'invisible': listChecked.length != 0}" style="height: 57px" class="tr-sort">
                    <th v-if="requestParam.status !== 'history'" style="width: 40px; vertical-align: middle!important;">
                      <input style="vertical-align: middle!important;" type="checkbox" v-model="isAll" @change="select" />
                    </th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.ID},
                                       isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.ID)"><span v-text="resources['tooling_id']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.COMPANY}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.COMPANY)"><span v-text="resources['company']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.LOCATION}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.LOCATION)"> <span v-text="resources['location']"></span><span class="__icon-sort"></span></th>
  <!--                  <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.LAST_SHOT}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.LAST_SHOT)"><span v-text="resources['accumulated_shots']"></span><span class="__icon-sort"></span></th>-->
                    <th class="text-left __sort" v-click-outside="closeLastShot" @click="sortBy(sortType.LAST_SHOT)" v-bind:class="[ {'active': currentSortType === sortType.LAST_SHOT}, isDesc ? 'desc': 'asc']">
                      <span v-text="resources['accumulated_shots']"></span>
                      <span class="__icon-sort"></span>
                      <last-shot-filter v-show="lastShotDropdown" @update-variable="updateVariable" :last-shot-data="lastShotData"></last-shot-filter>
                    </th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.ESTIMATED_EXTENDED_LIFE}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.ESTIMATED_EXTENDED_LIFE)"><span v-text="resources['estimated_extended_life']"></span><span class="__icon-sort"></span></th>
  
                    <th class="text-left __sort" v-if="['alert','approved','disapproved'].includes(requestParam.status)" v-bind:class="[ {'active': currentSortType === sortType.FAILURE_TIME}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.FAILURE_TIME)"><span v-text="resources['est_end_of_life']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-if="['history'].includes(requestParam.status)" v-bind:class="[ {'active': currentSortType === sortType.FAILURE_TIME}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.FAILURE_TIME)">  <span v-text="resources['last_refurbishment_time']"></span><span class="__icon-sort"></span></th>
  
                    <th class="text-center __sort" v-bind:class="[ {'active': currentSortType === sortType.OP}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.OP)"><span v-text="resources['op']"></span><span class="__icon-sort"></span></th>
                    <th class="text-center __sort" v-bind:class="[ {'active': currentSortType === sortType.STATUS}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.STATUS)" v-if="['alert', 'approved','disapproved'].includes(requestParam.status)"><span v-text="resources['status']"></span><span class="__icon-sort"></span></th>
                    <th class="text-center __sort" v-if="requestParam.status === 'alert' || requestParam.status === 'approved' || requestParam.status === 'disapproved'"><span v-text="resources['details']"></span><span class="__icon-sort"></span></th>
                    <!-- <th class="text-center" v-if="['alert', 'approved','disapproved'].includes(requestParam.status)">Action</th> -->
                    <th class="text-center" v-else><span v-text="resources['action']"></span></th>
                  </tr>
  
                  <tr :class="{'d-none zindexNegative': listChecked.length == 0}" id="action-bar" class="tr-sort empty-tr">
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
  
                          <div v-if="listChecked.length == 1 && requestParam.status != 'DELETED'" class="action-item" v-click-outside="closeShowDropDown">
                            <div class="change-status-dropdown drowdown d-inline">
                              <template v-if="['alert', 'disapproved'].includes(requestParam.status)">
                                <div title="Change status" class="change-status"  @click="showDropdown = !showDropdown">
                                  <span>Change Status</span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                                <template v-if="listCheckedFull[0].refurbishmentStatus == 'REQUESTED'">
                                  <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                    <!-- <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li> -->
                                    <li>
                                      <a href="#" class="dropdown-item"
                                         @click.prevent="showRequestMaintenance(listCheckedFull[0])" v-text="resources['approve']"></a>
                                    </li>
                                    <li>
                                      <a href="#" class="dropdown-item" @click.prevent="changeStatus(listCheckedFull[0])" v-text="resources['disapprove']"></a>
                                    </li>
                                  </ul>
                                </template>
                                <template v-else>
                                  <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                    <!-- <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li> -->
                                    <li>
                                      <a href="#" class="dropdown-item"
                                         @click.prevent="showRequestMaintenance(listCheckedFull[0])">Request Refurbishment</a>
                                    </li>
                                    <li>
                                      <a href="#" class="dropdown-item" @click.prevent="showDiscardRefurbishment(listCheckedFull[0])">Discard Tooling</a>
                                    </li>
                                  </ul>
                                </template>
                              </template>
                              <template v-else-if="requestParam.status == 'approved'">
                                <div title="Change status" class="change-status"  @click.prevent="showRequestMaintenance(listCheckedFull[0])">
                                  <span>Change Status</span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                              </template>
  
                            </div>
                          </div>
  
                          <div v-if="listChecked.length == 1 && requestParam.status != 'DELETED'" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div class="change-status-dropdown drowdown d-inline" v-click-outside="closeWorkorderDropdown">
                            <div v-if="listChecked.length == 1"
                                 class="action-item"
                                 @click="showWorkOrderDropdown = !showWorkOrderDropdown;"
                                 style="align-items: flex-start"
                            >
                              <span>Create Workorder</span>
                              <img src="/images/icon/icon-workorder.svg" style="margin-left: 4px; height: 15px; width: 15px"/>
                            </div>
                            <ul class="dropdown-menu" :class="[showWorkOrderDropdown ? 'show' : '']">
                              <li v-for="item in workOrderType" :key="item.value">
                                <div class='dropdown-item' @click.prevent="showWorkOrderDropdown = false; showCreateWorkorder(listCheckedFull[0], false, item)">{{`Request ${item.name}`}}</div>
                              </li>
                            </ul>
                          </div>
  <!--                        <div-->
  <!--                            v-if="listChecked.length == 1"-->
  <!--                            class="action-item"-->
  <!--                            @click="showCreateWorkorder(listCheckedFull[0])"-->
  <!--                            style="align-items: flex-start"-->
  <!--                        >-->
  <!--                          <span>Create Workorder</span>-->
  <!--                          <img src="/images/icon/icon-workorder.svg" style="margin-left: 4px; height: 15px; width: 15px"/>-->
  <!--                        </div>-->
                        </div>
                      </div>
                  </thead>
  
                  <tbody class="op-list" style="display:none">
                  <template>
                    <tr v-show="!isLoading" v-for="(result, index, id) in results" :id="result.id">
                      <!-- Tooling ID -->
                      <td  v-if="requestParam.status !== 'history'">
                        <template v-if="listChecked.length >= 0">
                          <input @click="check" :checked="checkSelect(result.id)" class="checkbox"
                                 type="checkbox" :value="result.id">
                        </template>
                      </td>
                      <td class="text-left tooling-id-cell">
                        <a href="#" @click.prevent="showChart(result.mold)" style="color:#1aaa55"
                           class="mr-1"><i class="fa fa-bar-chart"></i></a>
                        <a href="#" @click.prevent="showMoldDetails(result.mold)">
                          {{result.mold.equipmentCode}}
                        </a>
                      </td>
                      <!-- Company -->
                      <td class="company-cell text-left">
                        <div>
                          <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.mold.companyIdByLocation)">
                            {{result.mold.companyName}}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">{{result.mold.companyCode}}</span>
                      </td>
  
                      <!-- Location -->
                      <td class="location-cell text-left">
                        <div class="font-size-14">
                          <a href="#"
                             @click.prevent="showLocationHistory(result)">
                            {{result.mold.locationName}}
                          </a>
                        </div>
                        <span class="small text-muted font-size-11-2">
                                              {{result.mold.locationCode}}
                                          </span>
                      </td>
                      <!-- Last Shot -->
  
                      <td class="last-shot-cell text-left">
                        <span>{{formatNumber(result.mold.accumulatedShot)}}</span>
                        <div>
                                              <span class="small text-muted font-size-11-2">
                                                  shots
                                              </span>
                        </div>
                      </td>
                      <td class="last-shot-cell text-left">
                        <span>{{formatNumber(result.estimateExtendedLife)}}</span>
                        <div>
                                              <span class="small text-muted font-size-11-2">
                                                  shots
                                              </span>
                        </div>
                      </td>
                      <!-- Refurbishment time -->
                      <td class="falure-time-cell text-left" v-if="result.refurbishmentStatus === 'COMPLETED'">
  
                        {{ result.moldEndLifeCycle.endLifeAtDate }}
                      </td>
                      <td class="falure-time-cell text-left" v-else >
                        {{ result.endLifeAtDate }}
                      </td>
  
                      <!-- OP -->
                      <td style="vertical-align: middle!important;" class="op-cell text-center">
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
                      <!-- Status -->
                      <td style="vertical-align: middle!important;" class="status-cell text-center">
  <!--                      <a href="#" @click.prevent="showMaintanceConfirm(result.mold)" title="View"
                           v-if="result.maintenanceStatus === 'DONE'">
                          <img height="18" src="/images/icon/view.png" alt="View" />
                        </a>
                        -->
                        <a href="#" @click.prevent="showCorrectiveMaintenanceHistory(result)"
                           title="View"
                           v-if="result.refurbishmentStatus === 'COMPLETED' || result.refurbishmentStatus === 'DISCARDED'
                                             || ( result.refurbishmentStatus === 'DISAPPROVED' || result.refurbishmentStatus === 'APPROVED') && requestParam.status === 'history'">
                          <img height="18" src="/images/icon/view.png" alt="View" />
                        </a>
                        <span class="label label-danger"
                              v-if="result.refurbishmentStatus === 'END_OF_LIFECYCLE' && result.priority=='HIGH'" v-text="resources['high'] + ' Priority'"></span>
                        <span class="label label-warning"
                              v-if="result.refurbishmentStatus === 'END_OF_LIFECYCLE' && result.priority=='MEDIUM'" v-text="resources['medium']+ ' Priority'"></span>
                        <span class="label label-primary"
                              v-if="result.refurbishmentStatus === 'REQUESTED'" v-text="resources['requested_refurbishment']"></span>
                        <span class="label label-success"
                              v-if="result.refurbishmentStatus === 'APPROVED' && requestParam.status === 'approved'" v-text="resources['approved']"></span>
  
                        <span class="label label-default"
                              v-if="result.refurbishmentStatus === 'DISAPPROVED' && requestParam.status === 'disapproved'" v-text="resources['disapproved']"></span>
  
                        <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note"
                           v-if="!['alert', 'approved', 'disapproved'].includes(requestParam.status)">
                          <img src="/images/icon/note.svg" alt="Add note" />
                        </a>
                      </td>
  
                      <!-- Details -->
                      <td style="vertical-align: middle!important;" class="detail-cell text-center" v-if="requestParam.status === 'alert' || requestParam.status === 'approved' || requestParam.status === 'disapproved'">
                                              <span v-if="['REQUESTED', 'APPROVED', 'DISAPPROVED'].includes(result.refurbishmentStatus)"
                                                    style="font-size: 26px; cursor: pointer"
                                                    @click.prevent="showCorrectiveMaintenanceDetails(result)" >
                                                  <i class="fa fa-file-text-o" aria-hidden="true"></i>
                                              </span>
                      </td>
  
                    </tr>
                  </template>
                  </tbody>
                </table>
  
                <div class="no-results d-none" v-text="resources['no_results']"></div>
  
                <div v-show="!isLoading" class="row">
                  <div class="col-md-8">
                    <ul class="pagination">
                      <li v-for="(data, index) in pagination" class="page-item"
                          :class="{active: data.isActive}">
                        <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                      </li>
                    </ul>
                  </div>
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
  
  
      <request-refurbishment :resources="resources" @callback-message-form="callbackMessageForm"></request-refurbishment>
      <refurbishment-details :resources="resources"></refurbishment-details>
      <refurbishment-history @show-detail="showCorrectiveMaintenanceDetails" :resources="resources"></refurbishment-history>
      <disapproval-refurbishment-form :resources="resources" @callback-message-form="callbackMessageForm"></disapproval-refurbishment-form>
      <confirm-discard-refurbishment-modal :resources="resources" @callback-message-form="callbackMessageForm"></confirm-discard-refurbishment-modal>
  
    </div>
  </template>
  
  <script>
  module.exports = {
    name: "mold-refurbishment",
    components:{
      'refurbishment-details': httpVueLoader('/components/refurbishment-details.vue'),
  
      'request-refurbishment': httpVueLoader('/components/request-refurbishment.vue'),
  
      'refurbishment-history': httpVueLoader('/components/refurbishment-history.vue'),
      'disapproval-refurbishment-form': httpVueLoader('/components/disapproval-refurbishment-form.vue'),
      'confirm-discard-refurbishment-modal': httpVueLoader('/components/confirm-discard-refurbishment-modal.vue'),
  
    },
    props: {
      resources: Object,
      showCreateWorkorder: Function,
    },
    data() {
      return {
        'results': [],
        'total': 0,
  
        showDrowdown: null,
        'pagination': [],
        'requestParam': {
  
          'query': '',
          'status': 'alert',
          'sort': 'id,desc',
          'page': 1,
  
          'operatingStatus': '',
  
        },
  
        'statusCodes': [],
        'listCategories': [],
        sortType: {
          ID: 'mold.equipmentCode',
          COMPANY: 'mold.location.company.name',
          LOCATION: 'mold.location.name',
          LAST_SHOT: 'mold.lastShot',
          PERIOD: 'createdAt',
          OP: 'mold.operatingStatus',
          PM: 'executionRate',
          STATUS: 'refurbishmentStatus',
          ESTIMATED_EXTENDED_LIFE: 'estimateExtendedLife',
          FAILURE_TIME: 'failureTime'
        },
        currentSortType: 'partCode',
        isDesc: true,
        isLoading: false,
        alertOn:true,
        showDropdown: false,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        listCheckedFull: [],
        lastShotDropdown: false,
        lastShotData: {
          filter: null,
          sort: ""
        },
        cancelToken: undefined,
        showWorkOrderDropdown: false,
        workOrderType: [
              // {title: 'Request Refurbishment', key: 'REFURBISHMENT'},
              // {title: 'Request Disposal', key: 'DISPOSAL'},
              // {
              //   name: "General",
              //   value: "GENERAL",
              // },
              // {
              //   name: "Inspection",
              //   value: "INSPECTION",
              // },
              // {
              //   name: "Emergency",
              //   value: "EMERGENCY",
              // },
              // {
              //   name: "Preventive Maintenance",
              //   value: "PREVENTATIVE_MAINTENANCE",
              // },
              // {
              //   name: "Corrective Maintenance",
              //   value: "CORRECTIVE_MAINTENANCE",
              // },
              {
                name: "Disposal",
                value: "DISPOSAL",
              },
              {
                name: "Refurbishment",
                value: "REFURBISHMENT",
              },
            ],
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
          const x = this.results.filter(item => self.listChecked.indexOf(item.id) > -1)
          this.listCheckedFull = x;
        }
      },
    },
    created () {
    },
    methods: {
      closeWorkorderDropdown(){
        this.showWorkOrderDropdown = false;
      },
      updateVariable (data) {
        this.lastShotData = data
        // this.isDesc = data.sort === 'desc'
      },
      closeLastShot () {
        if (this.lastShotDropdown) {
          console.log(this.lastShotData, 'closeLastShot')
          this.requestParam.accumulatedShotFilter= this.lastShotData && this.lastShotData.filter || '';
          if ( this.lastShotData &&  this.lastShotData.sort) {
            this.currentSortType = 'mold.lastShot';
            this.isDesc = this.lastShotData.sort === 'desc';
            this.requestParam.sort = `${this.lastShotData.filter === null ? 'mold.lastShot' : `accumulatedShot.${this.lastShotData.filter}`}`
            this.requestParam.sort+=`,${this.isDesc ? 'desc' : 'asc'}`;
          }
  
          this.sort()
          this.lastShotDropdown = false
        }
      },
      changeAlertGeneration(alertType, isInit, alertOn, specialAlertType) {
        if(alertOn!=null){
          this.alertOn = alertOn;
          //Common.onOffNumAlertOnSidebar(alertOn,null);
        }
        console.log('this.alertOn', this.alertOn);
        if (specialAlertType != null) {
          this.requestParam.specialAlertType = specialAlertType
        }
        if (specialAlertType!=null)
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
      caculaterPeriod: function (item) {
        //((lastShot - (preventCycle - preventUpcoming))/preventCycle + 1) * preventCycle
        console.log('item: ', item);
        const { mold, preventCycle, preventUpcoming } = item;
        const { lastShot } = mold;
        if ((lastShot - (preventCycle - preventUpcoming)) < 0 || (preventCycle - preventUpcoming) < 0) {
          return this.formatNumber(preventCycle)
        }
  
        return this.formatNumber((Math.floor((lastShot - (preventCycle - preventUpcoming)) / preventCycle) + 1) * preventCycle);
      },
      convertDate: function(s) {
        return s?.slice(0, 10);
      },
  
      convertTimeString: function(s) {
        return s?.slice(10);
      },
  
      tab: function (status) {
        this.total=null;
        this.currentSortType = 'id';
        this.requestParam.sort = 'id,desc';
        this.requestParam.status = status;
        this.listChecked = [];
        this.listCheckedFull = [];
        this.paging(1);
      },
      search: function (page) {
        this.paging(1);
      },
  
      closeShowDropDown () {
        this.showDropdown = false
      },
  
      disable: function (user) {
        user.enabled = false;
        //this.save(user);
      },
  
      showChart: function (mold, tab) {
        console.log(mold);
        var child = Common.vue.getChild(this.$parent.$children, 'chart-mold');
        if (child != null) {
          child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
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
      showCorrectiveMaintenanceDetails: function (maintenance) {
        console.log('vao show detail');
        var child = Common.vue.getChild(this.$children, 'refurbishment-details');
        if (child != null) {
          child.showCorrectiveMaintenanceDetails(maintenance);
        }
      },
  
      showCorrectiveMaintenanceHistory: function (maintenance) {
        var child = Common.vue.getChild(this.$children, 'refurbishment-history');
        if (child != null) {
          child.show(maintenance);
        }
      },
  
      showRequestMaintenance: function (maintenance) {
        console.log('request-refurbishment');
        var child = Common.vue.getChild(this.$children, 'request-refurbishment');
        if (child != null) {
          child.show(maintenance);
        }
      },
  
      showLocationHistory: function (mold) {
        var child = Common.vue.getChild(this.$parent.$children, 'location-history');
        if (child != null) {
          child.showLocationHistory(mold);
        }
      },
  
      showCompanyDetailsById: function (company) {
        var child = Common.vue.getChild(this.$parent.$children, 'company-details');
        if (child != null) {
          child.showDetailsById(company);
        }
      },
  
      showMessageDetails: function (data) {
        var child = Common.vue.getChild(this.$parent.$children, 'message-details');
        if (child != null) {
  
          var data = {
            'title': 'Refurbishment Information',
            'messageTitle': 'Checklist',
            'equipmentCode': data.mold.equipmentCode,
            'alertDate': data.createdDateTime,
            'confirmDate': data.maintenancedDateTime,
            'name': data.maintenanceBy,
            'message': data.checklist
          };
  
          child.showMessageDetails(data);
        }
      },
      changeStatus: function (data) {
  
        var messageFormComponent = Common.vue.getChild(this.$children, 'disapproval-refurbishment-form');
        if (messageFormComponent != null) {
          messageFormComponent.showModal(data);
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
        console.log('checked', this.listCheckedFull)
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
  
      paging: function (pageNumber) {
        Common.handleNoResults('#app', 1);
        var self = this;
        self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
        let urlPath = null;
        const requestParam = JSON.parse(JSON.stringify(self.requestParam));
        /*
                            if (['approved', 'history'].includes(requestParam.status)){
        */
        urlPath = "/refurbishment";
        const statusMapping = {
          'alert': 'alert',
          'approved': 'approved',
          'disapproved': 'disapproved',
          'history': 'history'
        };
        requestParam.status = statusMapping[requestParam.status];
        /*  }else {
              urlPath = "/maintenance";
          }*/
        var param = Common.param(requestParam);
        self.isLoading = true;
        if (typeof this.cancelToken != typeof undefined) {
          this.cancelToken.cancel('new request')
        }
        this.cancelToken = axios.CancelToken.source()
        axios.get('/api/molds' + urlPath + '?lastAlert=true&' + param, {
          cancelToken: this.cancelToken.token
        }).then(function (response) {
          self.isLoading = false;
          console.log('response11112: ', response)
          self.total = response.data.totalElements;
          self.results = response.data.content;
          self.pagination = Common.getPagingData(response.data);
          self.results.forEach(result => {
            result.maintenancedAtShow = result.maintenancedAt ? moment.unix(result.maintenancedAt).format("YYYY-MM-DD, HH:mm:ss"): "";
          });
  
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
              self.isLoading = false;
              console.log(error.response);
            });
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
  
  
      ratio: function (mold) {
        return (((mold.lastShot) / mold.nextMaintenanceShot) * 100).toFixed(1);
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
        if (type === 'mold.lastShot') {
          this.lastShotDropdown = true
        } else {
          this.lastShotData.sort="";
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
      discardRefurbishment(refurbishment){
        let self = this;
        axios.post('/api/molds' + '/refurbishment/' + refurbishment.id + '/discard').then(function (response) {
          self.paging(1);
        }).catch(function (error) {
          console.log(error.response);
        });
      },
      showDiscardRefurbishment: function (refurbishment) {
        console.log('vao show discard modal');
        var child = Common.vue.getChild(this.$children, 'confirm-discard-refurbishment-modal');
        if (child != null) {
          child.showConfirm(refurbishment);
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
        // 모든 화면이 렌더링된 후 실행합니다.
        var uri = URI.parse(location.href);
        console.log('uri: ', uri);
        console.log('fragment', uri.fragment);
        if (uri.fragment === 'approved' || uri.fragment === 'history') {
          self.tab(uri.fragment);
        } else {
          // self.paging(1);
        }
  
  
      // })
    }
  }
  </script>
  
  <style scoped>
  
  </style>