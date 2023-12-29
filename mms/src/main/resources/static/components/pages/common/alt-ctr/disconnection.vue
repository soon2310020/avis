<template>
  <div v-if="isSubmenuPermitted">
    <notify-alert type="DISCONNECTED" @change-alert-generation="changeAlertGeneration" :resources="resources"></notify-alert>
    <div class="row">
      <div class="col-lg-12" v-click-outside="hideCalendarPicker">
        <div class="card wave" id="removee" style="height: 47px"> </div>
        <div class="card wave">
          <div class="card-header">
            <strong v-text="resources['disconnection']"></strong>
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
                <a class="nav-link customize-tab" :class="{ active: requestParam.status == 'alert' && tabType == 'terminal' }" href="#" @click.prevent="tab('alert', 'terminal')"><span v-text="resources['terminal_disconnection']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'alert' && tabType == 'terminal'">{{ total }}</span>
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link customize-tab" :class="{ active: requestParam.status == 'alert' && tabType == 'tooling' }" href="#" @click.prevent="tab('alert', 'tooling')"><span v-text="resources['tooling_disconnection']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'alert' && tabType == 'tooling'">{{ total }}</span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a class="nav-link customize-tab" :class="{ active: requestParam.status == 'confirmed' && tabType == 'terminal' }" href="#" @click.prevent="tab('confirmed', 'terminal')"><span v-text="resources['terminal_disconnection_history']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'confirmed' && tabType == 'terminal'">{{ total }}</span>
                </a>
              </li>
              <li class="nav-item dropdown">
                <a class="nav-link customize-tab" :class="{ active: requestParam.status == 'confirmed' && tabType == 'tooling' }" href="#" @click.prevent="tab('confirmed', 'tooling')"><span v-text="resources['tooling_disconnection_history']"></span>
                  <span v-show="alertOn" class="badge badge-light badge-pill" v-if="requestParam.status == 'confirmed' && tabType == 'tooling'">{{ total }}</span>
                </a>
              </li>
            </ul>
            <div v-show="alertOn">
              <table class="table table-responsive-sm table-striped">
                <template v-if="tabType == 'tooling'">
                  <thead id="thead-actionbar" class="custom-header-table">
                    <tr :class="{ 'invisible': listChecked.length != 0 }" style="height: 57px" class="tr-sort">
                      <th v-if="requestParam.status !== 'confirmed'" style="width: 40px; vertical-align: middle!important;">
                        <input style="vertical-align: middle!important;" type="checkbox" v-model="isAll" @change="select" />
                      </th>
                      <th class="text-left  __sort" v-bind:class="[{ 'active': currentSortType === sortType.TOOLING_ID }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.TOOLING_ID)"><span v-text="resources['tooling_id']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.TOOLING_COMPANY }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.TOOLING_COMPANY)"><span v-text="resources['company']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.TOOLING_LOCATION }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.TOOLING_LOCATION)"><span v-text="resources['location']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.LAST_SHOT }, isDesc ? 'desc' : 'asc']" v-click-outside="closeLastShot" @click="sortBy(sortType.LAST_SHOT)">
                        <span v-text="resources['accumulated_shots']"></span>
                        <span class="__icon-sort"></span>
                        <last-shot-filter v-show="lastShotDropdown" :last-shot-data="lastShotData" @update-variable="updateVariable"></last-shot-filter>
                      </th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.LAST_CONNECTION }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.LAST_CONNECTION)"> <span v-text="resources['last_connection']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.CONFIRMED_AT }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.CONFIRMED_AT)" v-if="requestParam.status == 'confirmed'"><span v-text="resources['confirmation_date']"></span><span
                          class="__icon-sort"></span></th>
                      <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.TOOLING_OP }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.TOOLING_OP)"><span v-text="resources['op']"></span><span class="__icon-sort"></span></th>
                      <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.STATUS }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.STATUS)" v-if="requestParam.status != 'confirmed'"><span v-text="resources['status']"></span><span class="__icon-sort"></span></th>
                      <th class="text-center" v-if="requestParam.status == 'confirmed'"><span v-text="resources['action']"></span></th>
                    </tr>
                    <tr :class="{ 'd-none zindexNegative': listChecked.length == 0 }" id="action-bar" class="tr-sort empty-tr">
                      <th class="empty-th">
                        <div class="first-checkbox-zone2" style="left: unset!important;">
                          <div class="first-checkbox2" style="display: flex;align-items: center;padding-left: 0;width: 110px;">
                            <div>
                              <input class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                            </div>
                          </div>
                          <div class="action-bar">
                            <div v-if="listChecked.length > 1"> No batch action is available </div>
                            <div v-if="listChecked.length == 1" class="action-item" v-click-outside="closeShowDropDown">
                              <div class="change-status-dropdown drowdown d-inline">
                                <div title="Change status" class="change-status" @click="showDropdown = !showDropdown">
                                  <span v-text="resources['change_status']"></span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                                <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                  <li v-if="isBtnTerminalConfirmPermitted">
                                    <a href="#" class="dropdown-item" @click.prevent="showConfirmOption(listCheckedFull[0])">Confirm Alert</a>
                                  </li>
                                </ul>
                              </div>
                            </div>
                            <div v-if="listChecked.length == 1" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                              <span>Memo</span>
                              <i class="icon-action memo-icon"></i>
                            </div>
                            <div v-if="listChecked.length == 1" class="action-item" @click="showCreateWorkorder(listCheckedFull[0])" style="align-items: flex-start">
                              <span>Create Workorder</span>
                              <img src="/images/icon/icon-workorder.svg" style="margin-left: 4px; height: 15px; width: 15px" />
                            </div>
                          </div>
                        </div>
                      </th>
                    </tr>
                  </thead>
                  <tbody class="op-list" style="display:none">
                    <tr v-for="(result, index, id) in results" :id="result.id">
                      <td v-if="requestParam.status !== 'confirmed'">
                        <template v-if="listChecked.length >= 0">
                          <input @click="check" :checked="checkSelect(result.id)" class="checkbox" type="checkbox" :value="result.id">
                        </template>
                      </td>
                      <td class="text-left">
                        <a href="#" @click.prevent="showChart(result.mold)" style="color:#1aaa55" class="mr-1"><i class="fa fa-bar-chart"></i></a>
                        <a href="#" @click.prevent="showMoldDetails(result.mold)"> {{ result.mold.equipmentCode }} </a>
                      </td>
                      <td class="text-left">
                        <div>
                          <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.mold.companyIdByLocation)"> {{ result.mold.companyName }} </a>
                        </div>
                        <span class="small text-muted font-size-11-2">{{ result.mold.companyTypeText }}</span>
                      </td>
                      <td class="text-left">
                        <div v-if="result.mold.locationCode">
                          <div class="font-size-14">
                            <a href="#" @click.prevent="showLocationHistory(result.mold)"> {{ result.mold.locationName }} </a>
                          </div>
                          <span class="small text-muted font-size-11-2">{{ result.mold.locationCode }}</span>
                        </div>
                      </td>
                      <td class="last-shot-cell text-left">
                        <span>{{ formatNumber(result.mold.accumulatedShot) }}</span>
                      </td>
                      <td class="text-left">
                        <a href="#" @click.prevent="showDisconnectionDetails(result.mold)"> {{ result.mold.lastShotDate }} </a>
                        <div>
                          <span class='font-size-11-2'> {{ convertTime(result.mold.lastShotAt) }} </span>
                        </div>
                      </td>
                      <td class="text-left" v-if="result.notificationStatus !== 'ALERT'">
                        <a href="#" @click.prevent="showDisconnectionDetails(result.mold)"> {{ convertDate(result.confirmedDateTime) }} </a>
                        <div>
                          <span class='font-size-11-2'> {{ convertTimeString(result.confirmedDateTime) }} </span>
                        </div>
                      </td>
                      <td style="vertical-align: middle!important;" class="text-center op-status-td">
                        <span v-if="result.mold.operatingStatus == 'WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-success"></span>
                        <span v-if="result.mold.operatingStatus == 'IDLE' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-warning"></span>
                        <span v-if="result.mold.operatingStatus == 'NOT_WORKING' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-inactive"></span>
                        <span v-if="result.mold.operatingStatus == 'DISCONNECTED' && (result.mold.equipmentStatus == 'INSTALLED' || result.mold.equipmentStatus == 'CHECK')" class="op-active label label-danger"></span>
                      </td>
                      <td style="vertical-align: middle!important;" class="text-center op-status-td" v-if="requestParam.status != 'confirmed'">
                        <span class="label label-danger" v-if="result.notificationStatus == 'ALERT'">Alert</span>
                      </td>
                      <td style="vertical-align: middle!important;" class="text-center" v-if="requestParam.status == 'confirmed'">
                        <a href="#" @click.prevent="showDisconnectedConfirmHistory(result.mold)" title="View" v-if="result.notificationStatus != 'ALERT'">
                          <img height="18" src="/images/icon/view.png" alt="View" />
                        </a>
                        <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="All note" class="table-action-link note">
                          <img src="/images/icon/note.svg" alt="All note" />
                        </a>
                      </td>
                    </tr>
                  </tbody>
                </template>
                <template v-if="tabType == 'terminal'">
                  <thead id="thead-actionbar" class="custom-header-table">
                    <tr :class="{ 'invisible': listChecked.length != 0 }" style="height: 57px" class="tr-sort">
                      <th v-if="requestParam.status !== 'confirmed'" style="width: 40px; vertical-align: middle!important;">
                        <input type="checkbox" v-model="isAll" @change="select" style="vertical-align: middle!important;" />
                      </th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.TERMINAL_ID }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.TERMINAL_ID)"><span v-text="resources['terminal_id']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.COMPANY }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.COMPANY)"><span v-text="resources['company']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.LOCATION }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.LOCATION)"><span v-text="resources['location']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.AREA }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.AREA)"><span v-text="resources['area']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.LAST_CONNECTION }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.LAST_CONNECTION)"> <span v-text="resources['last_connection']"></span><span class="__icon-sort"></span></th>
                      <th class="text-left __sort" v-bind:class="[{ 'active': currentSortType === sortType.CONFIRMED_AT }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.CONFIRMED_AT)" v-if="requestParam.status == 'confirmed'"><span v-text="resources['confirmation_date']"></span><span
                          class="__icon-sort"></span></th>
                      <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.OP }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.OP)">OP<span class="__icon-sort"></span></th>
                      <th class="text-center __sort" v-bind:class="[{ 'active': currentSortType === sortType.STATUS }, isDesc ? 'desc' : 'asc']" @click="sortBy(sortType.STATUS)" v-if="requestParam.status != 'confirmed'"><span v-text="resources['status']"></span><span class="__icon-sort"></span></th>
                      <th class="text-center" style=" vertical-align: middle;" v-if="requestParam.status == 'confirmed'"><span v-text="resources['action']"></span></th>
                    </tr>
                    <tr :class="{ 'd-none zindexNegative': listChecked.length == 0 }" id="action-bar" class="tr-sort empty-tr">
                      <th class="empty-th">
                        <div class="first-checkbox-zone2" style="left: unset!important;">
                          <div class="first-checkbox2" style="display: flex;align-items: center;padding-left: 0;width: 110px;">
                            <div>
                              <input class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                            </div>
                          </div>
                          <div class="action-bar">
                            <div v-if="listChecked.length > 1"> No batch action is available </div>
                            <div v-if="listChecked.length == 1" class="action-item" v-click-outside="closeShowDropDown">
                              <div class="change-status-dropdown drowdown d-inline">
                                <div title="Change status" class="change-status" @click="showDropdown = !showDropdown">
                                  <span v-text="resources['change_status']"></span>
                                  <i class="icon-action change-status-icon"></i>
                                </div>
                                <ul class="dropdown-menu" :class="[showDropdown ? 'show' : '']">
                                  <li v-if="isBtnToolingConfirmPermitted">
                                    <a href="#" class="dropdown-item" @click.prevent="showConfirmOption(listCheckedFull[0])">Confirm Alert</a>
                                  </li>
                                </ul>
                              </div>
                            </div>
                            <div v-if="listChecked.length == 1" class="action-item" @click="showSystemNoteModal(listCheckedFull[0])">
                              <span>Memo</span>
                              <i class="icon-action memo-icon"></i>
                            </div>
                            <div v-if="listChecked.length == 1" class="action-item" @click="showCreateWorkorder(listCheckedFull[0])" style="align-items: flex-start">
                              <span>Create Workorder</span>
                              <img src="/images/icon/icon-workorder.svg" style="margin-left: 4px; height: 15px; width: 15px" />
                            </div>
                          </div>
                        </div>
                      </th>
                    </tr>
                  </thead>
                  <tbody class="op-list" style="display:none">
                    <template>
                      <tr v-show="!isLoading" v-for="(result, index, id) in results" :id="result.id">
                        <td v-if="requestParam.status !== 'confirmed'">
                          <template v-if="listChecked.length >= 0">
                            <input @click="check" :checked="checkSelect(result.id)" class="checkbox" type="checkbox" :value="result.id">
                          </template>
                        </td>
                        <td class="text-left">
                          <a href="#" @click.prevent="showTerminalDetails(result.terminal)"> {{ result.terminal.equipmentCode }} </a>
                        </td>
                        <td class="text-left">
                          <div>
                            <a class="font-size-14" href="#" @click.prevent="showCompanyDetailsById(result.terminal.companyIdByLocation)"> {{ result.terminal.companyName }} </a>
                          </div>
                          <span class="small text-muted font-size-11-2">{{ result.terminal.companyTypeText }}</span>
                        </td>
                        <td class="text-left">
                          <div v-if="result.terminal.locationCode">
                            <div class="font-size-14">
                              <a href="#" @click.prevent="showLocationHistory(result.terminal)">{{ result.terminal.locationName }} </a>
                            </div>
                            <span class="small text-muted font-size-11-2"> {{ result.terminal.locationCode }} </span>
                          </div>
                        </td>
                        <td class="text-left">{{ result.terminal.installationArea }}</td>
                        <td class="text-left">
                          <a href="#" @click.prevent="showDisconnectionDetails(result.terminal)"> {{ result.terminal.updatedDate }} </a>
                          <div>
                            <span class='font-size-11-2'> {{ convertTime(result.terminal.updatedAt) }} </span>
                          </div>
                        </td>
                        <td class="text-left" v-if="result.notificationStatus !== 'ALERT'">
                          <a href="#" @click.prevent="showDisconnectionDetails(result.terminal)"> {{ result.confirmedDate }} </a>
                          <div>
                            <span class='font-size-11-2'> {{ convertTime(result.confirmedAt) }} </span>
                          </div>
                        </td>
                        <td style="vertical-align: middle!important;" class="text-center op-status-td">
                          <span v-if="result.terminal.operatingStatus == 'WORKING' && (result.terminal.equipmentStatus == 'INSTALLED' || result.terminal.equipmentStatus == 'CHECK')" class="op-active label label-success"></span>
                          <span v-if="result.terminal.operatingStatus == 'NOT_WORKING' && (result.terminal.equipmentStatus == 'INSTALLED' || result.terminal.equipmentStatus == 'CHECK')" class="op-active label label-danger"></span>
                        </td>
                        <td style="vertical-align: middle!important;" class="text-center op-status-td" v-if="requestParam.status != 'confirmed'">
                          <span class="label label-danger" v-if="result.notificationStatus == 'ALERT'" v-text="resources['alert']"></span>
                        </td>
                        <td style="vertical-align: middle!important;" class="text-center" v-if="requestParam.status == 'confirmed'">
                          <a href="#" @click.prevent="showDisconnectedConfirmHistory(result.terminal)" title="View" v-if="result.notificationStatus !== 'ALERT'">
                            <img height="18" src="/images/icon/view.png" alt="View" />
                          </a>
                          <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note">
                            <img src="/images/icon/note.svg" alt="Add note" />
                          </a>
                        </td>
                      </tr>
                    </template>
                  </tbody>
                </template>
              </table>
              <div class="no-results d-none" v-text="resources['no_results']"> </div>
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
    <disconnection-details :resources="resources"></disconnection-details>
    <confirm-option-modal :resources="resources" @callback-message-form="callbackMessageForm"></confirm-option-modal>
    <disconnected-confirm-history :resources="resources"></disconnected-confirm-history>
  </div>
</template>
<script>
module.exports = {
  components: {
    'disconnection-details': httpVueLoader('/components/disconnection-details.vue'),
    'confirm-option-modal': httpVueLoader('/components/confirm-option-modal.vue'),
    'disconnected-confirm-history': httpVueLoader('/components/disconnected-confirm-history.vue'),

  },
  props: {
    resources: Object,
    currentTabData: Object,
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
        'sort': 'id,desc',
        'page': 1,

        'operatingStatus': '',
        'notificationStatus': ''
      },
      tabType: 'terminal',
      'statusCodes': [],
      'listCategories': [],
      sortType: {
        TOOLING_ID: 'mold.equipmentCode',
        TOOLING_COMPANY: 'mold.location.company.name',
        TOOLING_LOCATION: 'mold.location.name',
        TOOLING_OP: 'mold.operatingStatus',

        TERMINAL_ID: 'terminal.equipmentCode',
        COMPANY: 'terminal.location.company.name',
        LOCATION: 'terminal.location.name',
        AREA: 'terminal.installationArea',
        LAST_CONNECTION: 'notificationAt',
        OP: 'terminal.operatingStatus',
        LAST_SHOT: 'mold.lastShot',
        CONFIRMED_AT: 'confirmedAt',
        STATUS: 'notificationStatus'
      },
      currentSortType: 'id',
      isDesc: true,
      isLoading: false,
      alertOn: true,
      showDrowdown: false,
      listCheckedFull: [],
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: ""
      },
      cancelToken: undefined
    }
  },
  computed: {
    isSubmenuPermitted() {
      return Boolean(Common.isMenuPermitted(this.currentTabData.menuId, this.currentTabData.submenuId))
    },
    isBtnTerminalConfirmPermitted() {
      return Common.isMenuPermitted(this.currentTabData.menuId, this.currentTabData.submenuId, 'btnTerminalConfirm')
    },
    isBtnToolingConfirmPermitted() {
      return Common.isMenuPermitted(this.currentTabData.menuId, this.currentTabData.submenuId, 'btnToolingConfirm')
    }
  },
  watch: {
    listChecked(newVal) {
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
  methods: {
    updateVariable(data) {
      this.lastShotData = data
    },
    closeLastShot() {
      if (this.lastShotDropdown) {
        console.log(this.lastShotData, 'closeLastShot')
        this.requestParam.accumulatedShotFilter = this.lastShotData && this.lastShotData.filter || '';
        if (this.lastShotData && this.lastShotData.sort) {
          this.currentSortType = 'mold.lastShot';
          this.isDesc = this.lastShotData.sort === 'desc';
          this.requestParam.sort = `${this.lastShotData.filter === null ? 'mold.lastShot' : `accumulatedShot.${this.lastShotData.filter}`}`
          this.requestParam.sort += `,${this.isDesc ? 'desc' : 'asc'}`;
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
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId
      } else {
        this.showDrowdown = null
      }
    },
    hideCalendarPicker(event) {
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
          this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.tabType);
        }
      }
      this.listCheckedTracked[this.tabType][this.requestParam.page] = this.listChecked;
    },

    closeShowDropDown() {
      this.showDropdown = false
    },

    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.results.map(value => value.id);
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.tabType,
        });
      } else {
        this.listChecked = [];
        this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.tabType);
      }
      this.listCheckedTracked[this.tabType][this.requestParam.page] = this.listChecked;
    },

    tab: function (status, tabType) {
      this.total = null;
      this.currentSortType = 'id';
      this.requestParam.sort = 'id,desc';
      if (this.tabType !== tabType || (this.tabType == tabType && this.requestParam.status != status)) {
        this.results = [];
        this.isAll = false;
      }
      this.listChecked = [];
      this.listCheckedFull = [];
      this.requestParam.status = status;
      this.tabType = tabType;

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
        child.showChart(mold, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
      }


    },
    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },
    convertTime: function (s) {
      const dtFormat = new Intl.DateTimeFormat('en-GB', {
        timeStyle: 'medium',
        timeZone: 'UTC'
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : '';
    },
    showMoldDetails: function (mold) {
      const tab = 'switch-detail'
      this.showChart(mold, tab)
    },

    showTerminalDetails: function (terminal) {
      var child = Common.vue.getChild(this.$parent.$children, 'terminal-details');
      if (child != null) {
        child.showDetails(terminal);
      }
    },

    showConfirmOption: function (dataParams) {
      var param = {
        'id': dataParams ? dataParams.id : null,
        'notificationStatus': 'CONFIRMED',
        'message': ''
      };

      var data = {
        url: this.tabType == 'terminal' ? `/api/terminals/disconnect/${param.id}/confirm` : `/api/molds/disconnect/${param.id}/confirm`,
        'param': param,
        title: this.tabType == 'terminal' ? 'Terminal' : 'Tooling',
        tabType: this.tabType
      };

      var child = Common.vue.getChild(this.$children, 'confirm-option-modal');
      if (child != null) {
        child.showConfirm(data);
      }
    },

    //confirm-option-modal

    showLocationHistory: function (mold) {
      console.log('mold: ', mold)
      var child = Common.vue.getChild(this.$parent.$children, 'location-history');
      console.log('child: ', child)
      if (child != null) {

        child.showLocationHistory(mold);
      }
    },
    showDisconnectedConfirmHistory: function (data) {
      var child = Common.vue.getChild(this.$children, 'disconnected-confirm-history');
      if (child != null) {

        child.show(data, this.tabType);
      }
    },

    showMessageDetails: function (data) {
      var child = Common.vue.getChild(this.$children, 'message-details');
      if (child != null) {

        var data = {
          'title': 'Disconntion Information',
          'messageTitle': 'Message',
          'equipmentCode': data.mold.equipmentCode,
          // 'alertDate': data.notificationDateTime,
          'alertDate': vm.formatToDateTime(data.notificationAt),
          'confirmDate': data.confirmedDateTime,
          'name': data.confirmedBy,
          'message': data.message
        };

        child.showMessageDetails(data);
      }
    },

    showDisconnectionDetails: function (mold) {
      var child = Common.vue.getChild(this.$children, 'disconnection-details');
      if (child != null) {
        child.showDisconnectionDetails(mold, this.tabType, this.requestParam.status == 'confirmed');
      }
    },
    changeStatus: function (dataParams, status) {
      var param = {
        'id': dataParams ? dataParams.id : null,
        'notificationStatus': 'CONFIRMED',
        'message': ''
      };

      var data = {
        'title': 'Confirm',
        'messageTitle': 'Message',
        'buttonTitle': 'Confirm',

        'url': Page.API_BASE + '/disconnect/' + param.id + '/confirm',
        'param': param
      };

      if (!dataParams) {
        let listChecked = [];
        Object.keys(this.listCheckedTracked).forEach(status => {
          if (status === this.tabType) {
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
        data = {
          'title': 'Confirm',
          'messageTitle': 'Message',
          'buttonTitle': 'Confirm',

          'url': this.tabType === 'terminal' ? '/api/terminals/disconnect/confirm' : '/api/molds/disconnect/confirm',
          'param': listChecked.map(value => {
            return {
              'id': value,
              'notificationStatus': 'CONFIRMED',
              'message': ''
            };
          })
        };
        var messageFormComponent = Common.vue.getChild(this.$parent.$children, 'message-form');
        if (messageFormComponent != null) {
          vm.callbackMessageForm = this.callbackMessageForm.bind(this);
          messageFormComponent.showModal(data);
        }

        return;
      }
      axios.put(data.url, data.param).then(function (response) {
        Common.alert("success");
        //vm.callbackMessageForm();
      }).catch(function (error) {
        console.log(error.response);
      });

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
      // reset check box
      Common.handleNoResults('#app', 1);
      this.listChecked = [];
      this.isAll = false;
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      // checking is All
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked.filter(item => item.status === this.tabType).map(item => item.page);
      // if (pageSelectedAll.includes(this.requestParam.page)) {
      //   this.isAll = true;
      // }
      if (!this.listCheckedTracked[this.tabType]) {
        this.listCheckedTracked[this.tabType] = {};
      }
      if (!this.listCheckedTracked[this.tabType][this.requestParam.page]) {
        this.listCheckedTracked[this.tabType][this.requestParam.page] = [];
      }

      var param = Common.param(self.requestParam);
      let url = '/api/terminals/disconnect'
      if (this.tabType == 'terminal') {
        url = '/api/terminals/disconnect?lastAlert=true&' + param
      } else if (this.tabType == 'tooling') {
        url = '/api/molds/disconnect?lastAlert=true&' + param
      }
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel('new request') // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source()
      self.isLoading = true;
      axios.get(url, { cancelToken: this.cancelToken.token }).then(function (response) {
        this.listChecked = [];
        this.isAll = false;
        self.isLoading = false;
        console.log('response.data: ', response.data)
        self.total = response.data.totalElements;
        self.results = response.data.content.map(value => {
          if (value.notificationStatus === "FIXED") {
            // value.confirmedDateTime = moment.unix(value.updatedAt).format('YYYY-MM-DD HH:mm:ss')
            value.confirmedDateTime = vm.formatToDateTime(value.updatedAt)
          } else {
            value.confirmedDateTime = vm.formatToDateTime(value.confirmedAt)
          }
          return value;
        });
        self.pagination = Common.getPagingData(response.data);


        // if (self.listCheckedTracked[self.tabType][self.requestParam.page]) {
        //   self.listChecked = self.listCheckedTracked[self.tabType][self.requestParam.page];
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

      return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
    },
    ratioStyle: function (mold) {
      return "width: " + this.ratio(mold) + '%';
    }

    ,
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
      if (this.$parent && typeof this.$parent.reloadAlertCout === 'function') {
        this.$parent.reloadAlertCout();
      }
    },
    resetAfterUpdate() {
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    sortBy: function (type) {
      if (type === 'mold.lastShot') {
        this.lastShotDropdown = true
      } else {
        this.lastShotData.sort = "";
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
      }
    },
    sort: function () {
      this.paging(1);
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$parent.$children, 'system-note');
      if (child != null) {
        child.showSystemNote({
          id: result.moldId || result.terminalId
          , objectType: (result.terminalId != null ? 'TERMINAL' : 'TOOLING')
        });
      }
    },
    changeAlertGeneration(alertType, isInit, alertOn) {
      if (alertOn != null) {
        this.alertOn = alertOn;
        //Common.onOffNumAlertOnSidebar(alertOn,null);
      }
      console.log('this.alertOn', this.alertOn);
    }

  },

  mounted() {
    console.log('disconnection mounted !!!!!!')
    this.$nextTick(function () {
      Common.removeWave(500);
    });
    // this.callSetAllColumnList();
    this.paging(1);
  }
}
</script>
  
<style scoped>

</style>
  