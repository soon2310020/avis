<template>
  <div v-if="isSubmenuPermitted">
    <notify-alert type="DATA_SUBMISSION" @change-alert-generation="changeAlertGeneration" :resources="resources"></notify-alert>
    <div class="row">
      <div class="col-lg-12">
        <div class="card wave" id="removee" style="height: 60px"> </div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['data_approval']"></strong>
            <div class="card-header-actions">
              <span class="card-header-action">
                <small v-show="alertOn" class="text-muted"><span v-text="resources['total'] + ':'"></span> {{ total }}</small>
              </span>
            </div>
          </div>
          <div class="card-body">
            <input type="hidden" v-model="requestParam.sort" />
            <div class="form-row">
              <div class="mb-3 mb-md-0 col-12 ">
                <common-searchbar :placeholder="resources['search_by_columns_shown']" :request-param="requestParam" :on-search="search"></common-searchbar>
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
                <a class="nav-link" :class="{ active: requestParam.status == 'alert' && requestParam.notificationStatus == '' }" href="#" @click.prevent="tab('alert')"><span v-text="resources['alert']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'alert' && requestParam.notificationStatus == ''">{{ total }}</span>
                </a>
              </li>
              <li v-if="userType !== 'OEM'" class="nav-item dropdown">
                <a class="nav-link" :class="{ active: requestParam.notificationStatus == 'APPROVED' }" href="#" @click.prevent="tab('APPROVED')"><span v-text="resources['approved']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.notificationStatus == 'APPROVED'">{{ total }}</span>
                </a>
              </li>
              <li v-if="userType !== 'OEM'" class="nav-item dropdown">
                <a class="nav-link" :class="{ active: requestParam.notificationStatus == 'DISAPPROVED' }" href="#" @click.prevent="tab('DISAPPROVED')"><span v-text="resources['disapproved']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.notificationStatus == 'DISAPPROVED'">{{ total }}</span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a class="nav-link" :class="{ active: requestParam.status == 'confirmed' }" href="#" @click.prevent="tab('confirmed')"><span v-text="resources['history_log']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'confirmed'">{{ total }}</span>
                </a>
              </li>
            </ul>
            <div v-show="alertOn">
              <table class="table table-responsive-sm table-striped">
                <thead id="thead-actionbar" class="custom-header-table">
                  <tr :class="{ 'invisible': listChecked.length != 0 }" style="height: 57px" class="tr-sort">
                    <th style="width: 40px; vertical-align: middle!important;">
                      <input style="vertical-align: middle!important;" type="checkbox" v-model="isAll" @change="select" />
                    </th>
                    <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.ID }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.ID)"><span v-text="resources['tooling_id']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.COMPANY }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.COMPANY)"><span v-text="resources['company']"></span><span class="__icon-sort"></span></th>
                    <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.LOCATION }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.LOCATION)"><span v-text="resources['location']"></span><span class="__icon-sort"></span>
                    </th>
                    <th v-if="userType == 'OEM' || userType == 'SUPPLIER'" class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.DATE || currentSortType === sortType.APPROVED_AT }, isDesc ? 'desc' : 'asc']"
                      @click="sortBy(userType == 'OEM' ? sortType.DATE : sortType.APPROVED_AT)">
                      <span v-text="resources['alert_date']"></span>
                      <span class="__icon-sort">
                      </span>
                    </th>
                    <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.APPROVED_AT }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.APPROVED_AT)" v-if="userType == 'OEM' && requestParam.status == 'confirmed'">
                      <span v-text="resources['approval_disapproval_date']"></span><span class="__icon-sort"></span>
                    </th>
                    <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.CONFIRM_AT }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.CONFIRM_AT)" v-if="requestParam.status == 'confirmed' && userType !== 'OEM'">
                      <span v-text="resources['confirmation_date']"></span><span class="__icon-sort"></span>
                    </th>
                    <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.OP }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.OP)" v-if="requestParam.status !== 'confirmed'"><span v-text="resources['op']"></span><span class="__icon-sort"></span></th>
                    <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.STATUS }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.STATUS)" v-if="requestParam.status !== 'confirmed'"> Status<span class="__icon-sort"></span></th>
                  </tr>
                  <tr :class="{ 'd-none zindexNegative': listChecked.length == 0 }" id="action-bar" class="tr-sort empty-tr">
                    <th class="empty-th">
                      <div class="first-checkbox-zone2" style="left: unset!important;">
                        <div class="first-checkbox2" style="display: flex;align-items: center;padding-left: 0;width: 110px;">
                          <div>
                            <input id="checkbox-all-2" class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                          </div>
                        </div>
                        <div class="action-bar">
                          <!--
                          <div v-if="listChecked.length > 1">
                            No batch action is available
                          </div>
  -->
                          <div v-if="listChecked.length >= 1 && requestParam.status != 'DELETED' && requestParam.status != 'confirmed'" class="action-item" v-click-outside="closeShowDropDown">
                            <div class="change-status-dropdown drowdown d-inline">
                              <div title="Change status" class="change-status" @click="showDropdown = !showDropdown">
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul v-if="listChecked.length == 1" class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                <li v-if="userType !== 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeStatus(listChecked[0], 'CONFIRM', false)">Confirm</a>
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeStatus(listChecked[0], 'APPROVE', false)">Approve</a>
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeStatus(listChecked[0], 'DISAPPROVE', false)">Disapprove</a>
                                </li>
                              </ul>
                              <ul v-else class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                <li v-if="userType !== 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeSelectedStatus('CONFIRM')">Confirm</a>
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeSelectedStatus('APPROVE')">Approve</a>
                                </li>
                                <li v-if="userType == 'OEM'">
                                  <a href="#" class="dropdown-item" @click.prevent="changeSelectedStatus('DISAPPROVE')">Disapprove</a>
                                </li>
                              </ul>
                            </div>
                          </div>
                          <div v-if="requestParam.status == 'confirmed' && listChecked.length > 1"> No batch action is available </div>
                          <div v-if="listChecked.length == 1 && requestParam.status != 'DELETED' && requestParam.status == 'confirmed'" class="action-item" @click="showHistories(listCheckedFull[0].mold)">
                            <span>View</span>
                            <i class="icon-action icon-view"></i>
                          </div>
                          <div v-if="listChecked.length == 1 && requestParam.status != 'DELETED'" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                            <span>Memo</span>
                            <i class="icon-action memo-icon"></i>
                          </div>
                        </div>
                      </div>
                    </th>
                  </tr>
                </thead>
                <tbody v-show="!isLoading" class="op-list">
                  <tr v-for="(result, index, id) in results" :id="result.id">
                    <td>
                      <template v-if="listChecked.length >= 0">
                        <input @click="check" :checked="checkSelect(result.moldId)" class="checkbox" type="checkbox" :value="result.moldId">
                      </template>
                    </td>
                    <td class="text-left">
                      <a href="#" @click.prevent="showChart(result.mold)" style="color:#1aaa55" class="mr-1"><i class="fa fa-bar-chart"></i></a>
                      <a href="#" @click.prevent="showMoldDetails(result.mold, result)"> {{ result.mold.equipmentCode }} </a>
                    </td>
                    <td class="text-left">
                      <div>
                        <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.mold.companyIdByLocation)"> {{ result.mold.companyName }} </a>
                      </div>
                      <span class="small text-muted font-size-11-2">{{ result.mold.companyTypeText }}</span>
                    </td>
                    <td class="text-left">
                      <div class="font-size-14">
                        <a href="#" @click.prevent="showLocationHistory(result.mold)"> {{ result.mold.locationName }} </a>
                      </div>
                      <span class="small text-muted font-size-11-2"> {{ result.mold.locationCode }} </span>
                    </td>
                    <td class="text-left" v-if="userType == 'OEM' || userType == 'SUPPLIER'">
                      <span class="font-size-14"> {{ result.createdDate }} </span>
                      <div>
                        <span class='font-size-11-2'> {{ convertTime(result.createdAt) }} </span>
                      </div>
                    </td>
                    <td class="text-left" v-if="requestParam.status === 'confirmed'">
                      <span class="font-size-14"> {{ convertDate(result.confirmedAtShow) }} </span>
                      <div>
                        <span class='font-size-11-2'> {{ convertTimeString(result.confirmedAtShow) }} </span>
                      </div>
                    </td>
                    <td style="vertical-align: middle!important;" class="text-center op-status-td" v-if="requestParam.status !== 'confirmed'">
                      <span v-if="result.mold.operatingStatus == 'WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-success"></span>
                      <span v-if="result.mold.operatingStatus == 'IDLE' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-warning"></span>
                      <span v-if="result.mold.operatingStatus == 'NOT_WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-inactive"></span>
                      <span v-if="result.mold.operatingStatus == 'DISCONNECTED' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-danger"></span>
                    </td>
                    <td style="vertical-align: middle!important;" class="text-center op-status-td" v-if="requestParam.status !== 'confirmed'">
                      <div :style="{ backgroundColor: result.notificationStatus == 'PENDING' ? '#ac24d3' : (result.notificationStatus == 'DISAPPROVED' ? '#b8b8b8' : '#26a958') }" class="data-submission"> {{ result.notificationStatus == "PENDING" ? 'Pending' : (result.notificationStatus ==
                          'DISAPPROVED' ? 'Disapproved' : 'Approved')
                      }} </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div class="no-results d-none" v-text="resources['no_results']"></div>
              <div v-show="!isLoading" class="row">
                <div class="col-md-10 col-sm-10 col-xs-10">
                  <ul class="pagination">
                    <li v-for="(data, index) in pagination" class="page-item" :class="{ active: data.isActive }">
                      <a class="page-link" @click="paging(data.pageNumber)">{{ data.text }}</a>
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
    <submission-history id="data-submission-history" :resources="resources"></submission-history>
    <message-confirm :is-visible="isShowReason" :resources="resources" :data="reasonParam" @close="handleCloseReasonDialog" :callback-message-form="callbackMessageForm"></message-confirm>
  </div>
</template>
  
<script>
module.exports = {
  name: "mold-data-submission",
  components: {
    'submission-history': httpVueLoader('/components/data-submission-history.vue'),

  },
  props: {
    resources: Object,
    currentTabData: Object,
  },
  data() {
    return {
      userType: '',

      'results': [],
      'total': 0,
      'pagination': [],
      'requestParam': {
        lastAlert: true,
        'query': '',
        'status': 'alert',
        //'extraStatus': 'op-status-is-not-null',
        'sort': 'id,desc',
        'page': 1,

        'operatingStatus': '',
        'notificationStatus': ''
      },


      'listCategories': [],
      sortType: {
        ID: 'mold.equipmentCode',
        COMPANY: 'mold.location.company.name',
        LOCATION: 'mold.location.name',
        CT: 'cycleTimeStatus',
        APPROVED_AT: 'approvedAt',
        CONFIRM_AT: 'confirmedAt',
        OP: 'mold.operatingStatus',
        VARIANCE: 'variance',
        DATE: 'notificationAt',
        STATUS: 'notificationStatus'
      },
      currentSortType: 'id',
      isDesc: true,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      isLoading: false,
      alertOn: true,
      showDropdown: false,
      listCheckedFull: [],
      cancelToken: undefined,
      isShowReason: false,
      reasonParam: {},
    }
  },
  computed: {
    isSubmenuPermitted() {
      return Boolean(Common.isMenuPermitted(this.currentTabData.menuId, this.currentTabData.submenuId))
    },
  },
  watch: {
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this
        const x = this.results.filter(item => self.listChecked.indexOf(item.moldId) > -1)
        this.listCheckedFull = x;
      }
    },
  },
  async created() {
    var self = this;
    try {
      const userType = await Common.getSystem('type')
      self.userType = userType
    } catch (error) {
      console.log(error)
    }

  },
  methods: {
    changeAlertGeneration(alertType, isInit, alertOn) {
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log('this.alertOn', this.alertOn);
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
        this.listChecked = this.results.map(value => value.moldId);
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

    changeSelectedStatus: function (status) {
      let listChecked = [];
      Object.keys(this.listCheckedTracked).forEach(status => {
        if (status === this.requestParam.status) {
          Object.keys(this.listCheckedTracked[status]).forEach(page => {
            if (this.listCheckedTracked[status][page].length > 0) {
              listChecked = listChecked.concat(this.listCheckedTracked[status][page]);
            }
          });
        }
      });
      this.changeStatus(listChecked, status, true);
    },

    closeShowDropDown() {
      this.showDropdown = false
    },

    tab: function (status) {
      this.total = null;
      this.currentSortType = 'id';
      this.requestParam.sort = 'id,desc';
      if (status == 'APPROVED' || status == 'DISAPPROVED') {
        this.requestParam.notificationStatus = status;
        this.requestParam.status = 'alert';
      } else {
        this.requestParam.notificationStatus = '';
        this.requestParam.status = status;
      }

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
    showMoldDetails: function (mold, data) {
      const tab = 'switch-detail'
      var child = Common.vue.getChild(this.$parent.$children, 'mold-details');
      if (child != null) {
        mold.reason = data.reason;
        mold.notificationStatus = data.notificationStatus;
        // mold.approvedAt = data.approvedAt ? moment.unix(data.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
        mold.approvedAt = data.approvedAt ? vm.convertTimestampToDateWithFormat(data.approvedAt, 'MMMM DD YYYY HH:mm:ss') : '-';
        mold.approvedBy = data.approvedBy;
        // child.showMoldDetails(mold);
        this.showChart(mold, tab)
      }
    },

    showHistories: function (mold) {
      console.log('mold: ', mold)

      var child = Common.vue.getChild(this.$children, 'submission-history');
      if (child != null) {
        child.show({
          ...mold,
          userType: this.userType
        });
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
          'alertDate': data.notificationDateTime,
          'confirmDate': data.confirmedDateTime,
          'name': data.confirmedBy,
          'message': data.message
        };

        child.showMessageDetails(data);
      }
    },
    changeStatusOld: function (data, status, isBottom) {
      let param = {}
      if (isBottom) {
        param = {
          'moldIds': data,
          'action': status,
          'message': ''
        };
      } else {
        param = {
          'moldIds': [data],
          'action': status,
          'message': ''
        };
      }

      var data = {
        'title': 'Message',
        'messageTitle': '',
        'buttonTitle': 'Submit',
        placeholder: 'Please provide reason for disapproval.',
        'url': '/api/molds/data-submission/confirm',
        'param': param,
        method: 'POST'
      };

      if (status == 'DISAPPROVE') {
        var messageFormComponent = Common.vue.getChild(this.$parent.$children, 'message-form');
        if (messageFormComponent != null) {
          vm.callbackMessageForm = this.callbackMessageForm.bind(this);
          messageFormComponent.showModal(data);
        }
      } else {
        if (status == 'CONFIRM') {
          data = {
            'title': 'Confirm',
            'messageTitle': 'Message',
            'buttonTitle': 'Confirm',
            placeholder: '',
            'url': '/api/molds/data-submission/confirm',
            'param': param,
            method: 'POST'
          };
          var messageFormComponent = Common.vue.getChild(this.$parent.$children, 'message-form');
          if (messageFormComponent != null) {
            vm.callbackMessageForm = this.callbackMessageForm.bind(this);
            messageFormComponent.showModal(data);
          }

          return;
        }
        axios
          .post(data.url, data.param)
          .then((response) => {
            this.listChecked = [];
            this.isAll = false;
            Object.keys(this.listCheckedTracked).forEach(status => {
              if (status === this.requestParam.status) {
                this.listCheckedTracked[status] = {};
              }
            });
            this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.requestParam.status);

            this.tab(this.requestParam.status)
          })
          .catch(function (error) {
            console.log(error.response);
          });
      }

    },
    changeStatus: function (data, status, isBottom) {
      let param = {}
      if (isBottom) {
        param = {
          'moldIds': data,
          'action': status,
          'message': ''
        };
      } else {
        param = {
          'moldIds': [data],
          'action': status,
          'message': ''
        };
      }

      var data = {
        'title': 'Message',
        'messageTitle': '',
        'buttonTitle': 'Submit',
        placeholder: 'Please provide reason for disapproval.',
        'url': '/api/molds/data-submission/confirm',
        'param': param,
        method: 'POST'
      };

      if (status == 'DISAPPROVE') {
        data.title = this.resources['reason_for_disapproval'];

        this.isShowReason = true;
        this.reasonParam = data;
      } else
        if (status == 'CONFIRM') {
          data = {
            'title': 'Confirm',
            'messageTitle': 'Message',
            'buttonTitle': 'Confirm',
            placeholder: '',
            'url': '/api/molds/data-submission/confirm',
            'param': param,
            method: 'POST'
          };
          this.isShowReason = true;
          this.reasonParam = data;

          return;
        } else {
          data = {
            ...data,
            'title': this.resources['reason_for_approval'],
            'messageTitle': 'Message',
            'buttonTitle': 'Submit',
            placeholder: 'Please provide reason for approval.',
            'url': '/api/molds/data-submission/confirm',
            'param': param,
            method: 'POST'
          };
          this.isShowReason = true;
          this.reasonParam = data;
        }

    },

    handleCloseReasonDialog() {
      this.isShowReason = false;
    },
    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(this.$parent.$children, 'company-details');
      if (child != null) {
        child.showDetailsById(company);
      }
    },

    convertTime: function (s) {
      const dtFormat = new Intl.DateTimeFormat('en-GB', {
        timeStyle: 'medium',
        timeZone: 'UTC'
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : '';
    },

    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },

    log: function (res) {
      console.log('res', res)
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
      console.log('param: ', param)
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel('new request')
      }
      this.cancelToken = axios.CancelToken.source()
      axios.get('/api/molds/data-submission?' + param, {
        cancelToken: this.cancelToken.token
      }).then(function (response) {
        self.isLoading = false;
        const { userType } = self;
        self.total = response.data.totalElements;
        self.results = response.data.content.map(value => {
          value.confirmedAtShow = value.approvedAt ? vm.formatToDateTime(value.approvedAt)
            : (value.confirmedAt ? vm.formatToDateTime(value.confirmedAt) : '')
          value.alertDateShow = userType == 'OEM' ? (
            value.notificationAt ? vm.formatToDateTime(value.notificationAt) : ''
          ) : (value.approvedAt ? vm.formatToDateTime(value.approvedAt) : '')


          return value;
        });
        console.log('self.results: ', self.results)
        self.pagination = Common.getPagingData(response.data);
        Common.handleNoResults('#app', self.results.length);
        if (self.listCheckedTracked[self.requestParam.status][self.requestParam.page]) {
          self.listChecked = self.listCheckedTracked[self.requestParam.status][self.requestParam.page];
        }
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
      this.listChecked = [];
      this.isAll = false;
      this.paging(1);
      if (this.$parent && typeof this.$parent.reloadAlertCout === 'function') {
        this.$parent.reloadAlertCout();
      }
    },
    sortBy: function (type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc
      }
      if (type) {
        this.requestParam.sort = `${type},${this.isDesc ? 'desc' : 'asc'}`
        this.sort()
      }
    },
    sort: function () {
      this.paging(1);
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$parent.$children, 'system-note');
      if (child != null) {
        child.showSystemNote({ id: result.moldId });
      }
    },

  },

  mounted() {
    this.$nextTick(function () {
      Common.removeWave(500);
    });
    // this.$nextTick(function () {
    // 모든 화면이 렌더링된 후 실행합니다.
    var self = this;
    var uri = URI.parse(location.href);
    if (uri.fragment) {
      self.tab(uri.fragment);

    } else {
      self.paging(1);
    }
    // })
  }
}
</script>
  
<style scoped>

</style>
  