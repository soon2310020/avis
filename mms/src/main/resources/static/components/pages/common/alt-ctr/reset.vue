<template>
    <div>
      <notify-alert type="MISCONFIGURE" @change-alert-generation="changeAlertGeneration" :resources="resources"></notify-alert>
  
      <div class="row">
        <div class="col-lg-12">
          <div class="card wave" id="removee" style="height: 47px"> </div>
          <div class="card wave">
            <div class="card-header">
              <strong v-text="resources['reset']"></strong>
  
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
                           class="form-control" :placeholder="resources['search_by_columns_shown']">
                  </div> -->
                  <common-searchbar
                    :request-param="requestParam"
                    :placeholder="resources['search_by_columns_shown']"
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
                  <a class="nav-link" :class="{active: requestParam.status == 'misconfigured'}" href="#"
                     @click.prevent="tab('misconfigured')"><span v-text="resources['alert']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'misconfigured'">{{ total }}</span>
                  </a>
                </li>
                <li class="nav-item dropdown">
                  <a class="nav-link" :class="{active: requestParam.status == 'confirmed'}" href="#"
                     @click.prevent="tab('confirmed')"><span v-text="resources['history_log']"></span>
                    <span v-show="alertOn" class="badge badge-light badge-pill"
                          v-if="requestParam.status == 'confirmed'">{{ total }}</span>
                  </a>
                </li>
              </ul>
  
              <div v-show="alertOn">
  
                <table class="table table-responsive-sm table-striped">
  
                  <thead id="thead-actionbar" class="custom-header-table">
                  <tr :class="{'invisible': listChecked.length != 0}" style="height: 57px" class="tr-sort">
                    <th v-if="requestParam.status !== 'confirmed'" style="width: 40px; vertical-align: middle!important;">
                      <input style="vertical-align: middle!important;" class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                    </th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.ID}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.ID)"><span v-text="resources['tooling_id']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.COUNTER}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.COUNTER)"><span v-text="resources['counter_id']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.COMPANY}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.COMPANY)"><span v-text="resources['company']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.LOCATION}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.LOCATION)"><span v-text="resources['location']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-click-outside="closeLastShot" @click="sortBy(sortType.LAST_SHOT)" v-bind:class="[ {'active': currentSortType === sortType.LAST_SHOT}, isDesc ? 'desc': 'asc']">
                      <span v-text="resources['accumulated_shots']"></span>
                      <span class="__icon-sort"></span>
                      <last-shot-filter v-show="lastShotDropdown" @update-variable="updateVariable" :last-shot-data="lastShotData"></last-shot-filter>
                    </th>
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.RESET}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.RESET)"><span v-text="resources['reset_value']"></span><span class="__icon-sort"></span></th>
  
                    <th class="text-left __sort" v-bind:class="[ {'active': currentSortType === sortType.DATE}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.DATE)"><span v-text="resources['alert_date']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-if="requestParam.status == 'confirmed'" v-bind:class="[ {'active': currentSortType === sortType.CONFIRM_DATE}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.CONFIRM_DATE)"><span v-text="resources['completion_date']"></span><span class="__icon-sort"></span></th>
                    <th class="text-center __sort" v-bind:class="[ {'active': currentSortType === sortType.OP}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.OP)"><span v-text="resources['op']"></span><span class="__icon-sort"></span></th>
                    <th class="text-center __sort" v-bind:class="[ {'active': currentSortType === sortType.STATUS}, isDesc ? 'desc': 'asc']"  @click="sortBy(sortType.STATUS)" v-if="requestParam.status != 'confirmed'"><span v-text="resources['status']"></span><span class="__icon-sort"></span></th>
                    <!-- <th v-if="requestParam.status != 'confirmed'">Edit</th> -->
                    <th class="text-center" v-if="requestParam.status == 'confirmed'"><span v-text="resources['action']"></span></th>
                    <!-- <th class="text-center" v-if="requestParam.status !== 'confirmed'"><span v-text="resources['action']"></span></th> -->
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
  
                          <div v-if="listChecked.length == 1" class="action-item" v-click-outside="closeShowDropDown">
                            <div class="change-status-dropdown drowdown d-inline">
                              <div title="Change status" class="change-status"  @click="openDropDown">
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul class="dropdown-menu" :class="[dropDownShow ? 'show' : '']">
                                <li class="dropdown-header">
                                  <!-- <span class="op-change-status" v-text="resources['change_status']"></span> -->
                                  <span class="op-change-status">No option is available</span>
                                </li>
                                <!-- <li>
                                    <a href="#" class="dropdown-item">No option is available</a>
                                </li> -->
                              </ul>
                            </div>
                          </div>
                          <div v-if="listChecked.length == 1 && requestParam.status != 'DELETED'" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                          <div
                              v-if="listChecked.length == 1"
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
                    <td class="text-left">
                      <a href="#" @click.prevent="showChart(result.mold)" style="color:#1aaa55"
                         class="mr-1"><i class="fa fa-bar-chart"></i></a>
                      <a href="#" @click.prevent="showMoldDetails(result.mold)">
                        {{result.mold.equipmentCode}}
                      </a>
                    </td>
                    <td class="text-left">
                      {{result.counterCode}}
                    </td>
                    <td class="text-left">
                      <!--
                                                              {{result.mold.companyName}}
                                                              <div class="small text-muted font-size-11-2">{{result.mold.companyTypeText}}</div>
                      -->
                      <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.mold.companyIdByLocation)">
                        {{result.mold.companyName}}
                      </a>
                      <div class="small text-muted font-size-11-2">
                        {{result.mold.companyCode}}
                      </div>
                    </td>
                    <td class="text-left">
                      <div v-if="result.mold.locationCode">
                        <div class="font-size-14">
                          <a href="#"
                             @click.prevent="showLocationHistory(result.mold)">{{result.mold.locationName}}</a>
                        </div>
                        <span class='small text-muted font-size-11-2'>{{result.mold.locationCode}}</span>
                      </div>
                    </td>
                    <td class="text-left">
                      {{formatNumber(result.mold.accumulatedShot ? result.mold.accumulatedShot : '0')}}
                      <div>
                                              <span class="font-size-11-2">
                                                  {{formatNumber(result.mold.accumulatedShot ? 'shots' : '')}}
                                              </span>
                      </div>
                    </td>
                    <!--
                                                        <td>
                                                            {{result.mold!=null ? result.mold.lastShot : '0'}}
                                                        </td>
                    -->
                    <td class="text-left">
                      <a href="#" @click.prevent="showResetDetail(result)">
                        {{formatNumber(result.preset)}}
                      </a>
                    </td>
  
  
                    <td class="text-left">
                                          <span class="font-size-14">
                                            {{result.createdDate}}
                                          </span>
                      <div>
                                              <span class='font-size-11-2'>
                                               {{convertTime(result.createdAt)}}
                                              </span>
                      </div>
                    </td>
  
                    <td class="text-left" v-if="requestParam.status == 'confirmed'">
                                          <span class="font-size-14">
                                              {{convertDate(result.confirmedAtShow)}}
                                            </span>
                                            <div>
                                              <span class='font-size-11-2'>
                                                 {{convertTimeString(result.confirmedAtShow)}}
                                              </span>
                                          </div>
                    </td>
  
                    <td style="vertical-align: middle!important;" class="text-center op-status-td">
                                          <span
                                              v-if="result.mold.operatingStatus == 'WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK' )"
                                              class="op-active label label-success"></span>
                      <span
                          v-if="result.mold.operatingStatus == 'IDLE' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK' )"
                          class="op-active label label-warning"></span>
                      <span
                          v-if="result.mold.operatingStatus == 'NOT_WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK' )"
                          class="op-active label label-inactive"></span>
                      <span
                          v-if="result.mold.operatingStatus == 'DISCONNECTED' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK' )"
                          class="op-active label label-danger"></span>
                    </td>
                    <td style="vertical-align: middle!important;" class="text-center op-status-td">
                                          <span class="label label-danger"
                                                v-if="result.misconfigureStatus == 'MISCONFIGURED'" v-text="resources['request_reset']"></span>
                      <a href="#" @click.prevent="showResetConfirmHistory(result)" title="View"
                         v-if="result.misconfigureStatus == 'CONFIRMED'">
                        <img height="18" src="/images/icon/view.png" alt="View" />
                      </a>
                      <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note"
                         v-if="requestParam.status == 'confirmed'">
                        <img src="/images/icon/note.svg" alt="Add note" />
                      </a>
                    </td>
  
                  </tr>
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
      <reset-confirm-history :resources="resources"></reset-confirm-history>
      <reset-detail :resources="resources"></reset-detail>
  
    </div>
  </template>
  
  <script>
  module.exports = {
    name: "mold-misconfigured",
    components:{
      'reset-confirm-history': httpVueLoader('/components/reset-confirm-history.vue'),
      'reset-detail': httpVueLoader('/components/reset-detail.vue'),
  
    },
    props: {
      resources: Object,
      showCreateWorkorder: Function,
    },
    data() {
      return {
        'results': [],
        'total': 0,
        'pagination': [],
        'requestParam': {
  
          'query': '',
          'status': 'misconfigured',
          //'extraStatus': 'op-status-is-not-null',
          'sort': 'id,desc',
          'page': 1,
  
          'operatingStatus': '',
          'notificationStatus': ''
        },
  
        'statusCodes': [],
        'listCategories': [],
        sortType: {
          ID: 'mold.equipmentCode',
          COUNTER: 'counterCode',
          COMPANY: 'mold.location.company.name',
          LOCATION: 'mold.location.name',
          LAST_SHOT: 'mold.lastShot',
          RESET: 'preset',
          CYCLE_TIME: 'cycleTime',
          OP: 'mold.operatingStatus',
          DATE: 'notificationAt',
          STATUS: 'misconfigureStatus',
          CONFIRM_DATE: 'confirmedAt'
        },
        currentSortType: 'id',
        isDesc: true,
        isLoading: false,
        alertOn:true,
        listChecked: [],
        listCheckedTracked: {},
        isAll: false,
        isAllTracked: [],
        listCheckedFull: [],
        dropDownShow: false,
        lastShotDropdown: false,
        lastShotData: {
          filter: null,
          sort: ""
        },
        cancelToken: undefined
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
      showCompanyDetailsById: function (company) {
        var child = Common.vue.getChild(this.$parent.$children, 'company-details');
        if (child != null) {
          child.showDetailsById(company);
        }
      },
      changeAlertGeneration(alertType, isInit, alertOn) {
        if(alertOn!=null){
          this.alertOn = alertOn;
          //Common.onOffNumAlertOnSidebar(alertOn,null);
        }
        console.log('this.alertOn', this.alertOn);
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
  
  
      disable: function (user) {
        user.enabled = false;
        //this.save(user);
      },
      openDropDown(){
        this.dropDownShow = !this.dropDownShow;
      },
  
      closeShowDropDown () {
        this.dropDownShow = false
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
      //reset-confirm-history
      showResetConfirmHistory: function (mold) {
        var child = Common.vue.getChild(this.$children, 'reset-confirm-history');
        if (child != null) {
          child.show(mold);
        }
      },
  
      //reset-detail
      showResetDetail: function (item) {
        var child = Common.vue.getChild(this.$children, 'reset-detail');
        if (child != null) {
          child.show(item);
        }
      },
      showLocationHistory: function (mold) {
        var child = Common.vue.getChild(this.$parent.$children, 'location-history');
        if (child != null) {
          child.showLocationHistory(mold);
        }
      },
      showMessageDetails: function (data) {
        var child = Common.vue.getChild(this.$parent.$children, 'message-details');
        if (child != null) {
  
          var data = {
            'equipmentTitle': 'Sensor ID',
            //'equipmentCode': data.mold.equipmentCode,
            'equipmentCode': data.counterCode,
  
            'title': 'Misconfigure Information',
            'messageTitle': 'Message',
            // 'alertDate': data.notificationDateTime,
            // 'confirmDate': data.confirmedDateTime,
            'alertDate': vm.formatToDateTime(data.notificationAt),
            'confirmDate': vm.formatToDateTime(data.confirmedAt),
            'name': data.confirmedBy,
            'message': data.message
          };
  
          child.showMessageDetails(data);
        }
      },
      changeStatus: function (data, status) {
        var param = {
          'id': data.id,
          'misconfigureStatus': status,
          'message': ''
        };
  
        var data = {
          'title': 'Confirm',
          'messageTitle': 'Message',
          'buttonTitle': 'Confirm',
  
          'url': '/api/molds/misconfigure/' + param.id + '/confirm',
          'param': param
        };
  
        var messageFormComponent = Common.vue.getChild(this.$parent.$children, 'message-form');
        if (messageFormComponent != null) {
          vm.callbackMessageForm = this.callbackMessageForm.bind(this);
          messageFormComponent.showModal(data);
        }
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
      convertTime: function (s) {
        const dtFormat = new Intl.DateTimeFormat('en-GB', {
          timeStyle: 'medium',
          timeZone: 'UTC'
        });
  
        return s!=null ? dtFormat.format(new Date(s * 1e3)):'';
      },
      convertDate: function(s) {
        return s?.slice(0, 10);
      },
  
      convertTimeString: function(s) {
        return s?.slice(10);
      },
  
      paging: function (pageNumber) {
        var self = this;
        Common.handleNoResults('#app', 1);
        self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
  
        var param = Common.param(self.requestParam);
        self.isLoading = true;
        if (typeof this.cancelToken != typeof undefined) {
          this.cancelToken.cancel('new request')
        }
        this.cancelToken = axios.CancelToken.source()
        axios.get('/api/molds/misconfigure?lastAlert=true&' + param, {
          cancelToken: this.cancelToken.token
        }).then(function (response) {
          console.log('response:11 ', response)
          self.isLoading = false;
          self.total = response.data.totalElements;
          self.results = response.data.content.map(value =>{
            // value.confirmedAtShow = value.confirmedAt ? moment.unix(value.confirmedAt).format('YYYY-MM-DD HH:mm:ss'): '';
            value.confirmedAtShow = vm.formatToDateTime(value.confirmedAt);
            return value;
          });
          self.pagination = Common.getPagingData(response.data);
  
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
              console.log(error.response);
              self.isLoading = false;
            });
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
  
  
      efficiencyRatio: function (moldMisconfigured) {
        return ((moldMisconfigured.uptimeSeconds / moldMisconfigured.currentSeconds) * 100).toFixed(1);
  
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
          child.showSystemNote({id: result.mold!=null? result.mold.id : result.id});
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
          if (uri.fragment == 'outside_l1') {
            self.tab('L1');
  
          } else if (uri.fragment == 'outside_l2') {
            self.tab('L2');
  
          } else {
            self.paging(1);
          }
  
      // })
    }
  }
  </script>
  
  <style scoped>
  
  </style>