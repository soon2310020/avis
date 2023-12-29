<template>
<div>
  <div class="row">
    <div class="col-lg-12">
      <div class="card wave">
<!--        <div class="card-header">
          <strong v-text="resources['maintenance_checklist']"></strong>

          <div class="card-header-actions">
                            <span class="card-header-action">
                                <small class="text-muted"><span  v-text="resources['total']+':'"></span> {{total}}</small>
                            </span>
          </div>
        </div>-->
<!--        <div class="card-body">-->
          <input type="hidden" v-model="requestParam.sort" />
          <div class="form-row">
            <div class="col-md-12 col-12">

              <div class="form-row">
                <!-- <div class="col-md-9 mb-8 mb-md-0 "> -->
                <div class="col-md-12 mb-3 mb-md-0 col-12">
                  <!-- <div class="input-group">
                    <div class="input-group-prepend">
                      <div class="input-group-text"><i class="fa fa-search"></i></div>
                    </div>
                    <input type="text" v-model="requestParam.query" @input="requestParam.query = $event.target.value"
                           @keyup="search" class="form-control"
                           :placeholder="resources['search_by_selected_column']">
                  </div> -->
                  <common-searchbar
                    :placeholder="resources['search_by_selected_column']"
                    :request-param="requestParam"
                    :on-search="search"
                  ></common-searchbar>
                </div>
                <!-- <div class="col-md-3 mb-4 mb-md-0">
                    <select class="form-control" v-model="requestParam.enabled"
                        @change="changeStatus" disabled>
                        <option value="" hidden v-text="resources['status']"></option>
                        <option :value="false" v-text="resources['disabled']"></option>
                        <option :value="true" v-text="resources['enabled']"></option>
                    </select>
                </div> -->
              </div>
            </div>
          </div>
<!--        </div>-->
        <div class="card-footer" style="display:none">
          <button class="btn btn-sm btn-primary" type="submit">
            <i class="fa fa-dot-circle-o"></i> <span v-text="resources['submit']"></span></button>
          <button class="btn btn-sm btn-danger" type="reset">
            <i class="fa fa-ban"></i> <span v-text="resources['reset']"></span></button>
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
              <a class="nav-link" :class="{active: requestParam.companyType === 'all'}" href="#"
                 @click.prevent="tab('all')"><span v-text="resources['enabled']"></span>
                <span class="badge badge-light badge-pill"
                      v-if="requestParam.companyType === 'all'">{{ total }}</span>
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{active: requestParam.companyType === ''}" href="#"
                 @click.prevent="tabToDisable('disabled')"><span v-text="resources['disabled']"></span>
                <span class="badge badge-light badge-pill"
                      v-if="requestParam.status === 'disabled'">{{ total }}</span>
              </a>
            </li>
            <!--                            <show-columns style="position: absolute; right: 20px;" @reset-columns-list="handleResetColumnsListSelected" @change-value-checkbox="handleChangeValueCheckBox" :all-columns-list="allColumnList" v-if="Object.entries(resources).length" :resources="resources"></show-columns>-->
            <div class="nav-item" style="position: absolute;right: 62px;">
                          <a @click="goToNew" id="goToNew" href="javascript:void(0)" class="btn-custom btn-custom-primary">
                            <span v-text="resources['create_codelist']"></span>
                          </a>
                        </div>
            <customization-modal style="position: absolute; right: 20px;" :all-columns-list="allColumnList" @save="saveSelectedList" :resources="resources" ></customization-modal>
          </ul>
          <table class="table table-responsive-sm table-striped">


            <thead id="thead-actionbar" class="custom-header-table">
            <tr :class="{'invisible': listChecked.length != 0}" style="height: 57px" class="tr-sort">
              <th class="custom-td">
                <div class="custom-td_header">
                  <input class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                </div>
              </th>
              <th v-for="(item, index) in allColumnList" v-if="item.enabled" :key="index"
                  :class="[{__sort: item.sortable, active: currentSortType === item.sortField}, isDesc ? 'desc': 'asc', item.label === 'Status' ? '': 'text-left']"
                  @click="sortBy(item.sortField)">
                <span>{{item.label}}</span>
                <span v-if="item.sortable" class="__icon-sort"></span>
              </th>
              <!--                                    <th style="min-width: 130px"><span v-text="resources['action']"></span></th>-->
            </tr>

            <tr :class="{'d-none zindexNegative': listChecked.length == 0}" id="action-bar" class="tr-sort empty-tr">
              <th class="empty-th">
                <div class="first-checkbox-zone2">
                  <div class="first-checkbox2" style="display: flex;align-items: center;padding-left: 0;width: 110px;">
                    <div>
                      <input id="checkbox-all-2" class="checkbox" type="checkbox" v-model="isAll" @change="select" />
                    </div>
                  </div>
                  <div class="action-bar">
                    <div v-if="listChecked.length == 1" @click="goToEdit" class="action-item">
                      <span>Edit</span>
                      <i class="icon-action edit-icon"></i>
                    </div>
                    <!-- <div v-if="listChecked.length == 1" @click="showRevisionHistory(listChecked[0])" class="action-item">
                      <span>View Edit History</span>
                      <i class="icon-action view-edit-history-icon"></i>
                    </div>
                    <div v-if="listChecked.length == 1" class="action-item" @click="showSystemNoteModal(listChecked[0])">
                      <span>Memo</span>
                      <i class="icon-action memo-icon"></i>
                    </div> -->
                    <div v-if="requestParam.status !== 'disabled'" class="action-item" @click.prevent="disable(listCheckedFull)">
                      <span>Disable</span>
                      <div class="icon-action disable-icon"></div>
                    </div>
                    <div v-if="requestParam.status === 'disabled'" class="action-item" @click.prevent="enable(listCheckedFull)">
                      <span>Enable</span>
                      <div class="icon-action enable-icon"></div>
                    </div>
                    <!--                                    <div v-if="requestParam.status != 'DELETED'" @click="showDeletePopup" class="action-item">-->
                    <!--                                        <span>Delete</span>-->
                    <!--                                        <svg xmlns="http://www.w3.org/2000/svg" width="6.949" height="8.611" viewBox="0 0 6.949 8.611">-->
                    <!--                                            <g id="Icon__delete" data-name="Icon_ delete" transform="translate(0.25 0.25)">-->
                    <!--                                                <path id="Icon_material-delete" data-name="Icon material-delete" d="M7.961,11.71a.914.914,0,0,0,.921.9h3.685a.914.914,0,0,0,.921-.9V6.3H7.961Zm5.988-6.759H12.337L11.876,4.5h-2.3l-.461.451H7.5v.9h6.449Z" transform="translate(-7.5 -4.5)" fill="none" stroke="#4b4b4b" stroke-width="0.5"/>-->
                    <!--                                            </g>-->
                    <!--                                        </svg>-->
                    <!--                                    </div>-->
                    <!--                                    <div v-if="requestParam.status == 'DELETED' && listChecked.length == 1" class="action-item">-->
                    <!--                                        <span @click.prevent="restoreItem()">Restore</span>-->
                    <!--                                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 68.137 58.403">-->
                    <!--                                            <path id="Icon_material-settings-backup-restore" data-name="Icon material-settings-backup-restore" d="M45.425,33.7a6.489,6.489,0,1,0-6.489,6.489A6.508,6.508,0,0,0,45.425,33.7ZM38.935,4.5a29.2,29.2,0,0,0-29.2,29.2H0L12.978,46.68,25.957,33.7H16.223A22.728,22.728,0,1,1,25.762,52.2l-4.607,4.672A29.2,29.2,0,1,0,38.935,4.5Z" transform="translate(0 -4.5)" fill="#4b4b4b"/>-->
                    <!--                                        </svg>-->
                    <!--                                    </div>-->
                  </div>
                </div>
            </thead>

            <tbody class="op-list" style="display:none">
            <tr v-for="(result, index, id) in results" :id="result.id">
              <td class="custom-td">
                <template v-if="listChecked.length >= 0">
                  <input @click="(e) => check(e, result)" :checked="checkSelect(result.id)" class="checkbox"
                         type="checkbox" :id="result.id + 'checkbox'" :value="result.id">
                  <!--                                            <label class="custom-control-labelx" :for="result.id + 'checkbox'"></label>-->
                </template>
              </td>
              <template v-for="(column, columnIndex) in allColumnList" v-if="column.enabled">
                <td v-if="column.field === 'codeTitle'" class="text-left">
                  {{ result.title }}
                  <div class="small text-muted font-size-11-2">
                    <span v-text="resources['registered']+':'"></span>
                    {{formatToDate(result.createdAt)}}
                  </div>
                </td>
                <td v-else-if="column.field === 'codeId'" class="text-left">
                  <!--<div class="small text-muted">{{result.company.companyTypeText}}</div>-->
                  {{ result.code }}
                </td>
                <td v-else-if="column.field === 'company'" class="text-left">
                  {{ result.companyCode }}
                </td>
                 <td v-else-if="column.field === 'mainLevel'" class="text-left">
                  {{ result.group1Code}}
                </td>
                <!-- <td style="vertical-align: middle!important;" class="text-center op-status-td" v-else-if="column.field === 'enabled'">
                  <span v-if="result.enabled" class="label label-success" v-text="resources['enabled']"> </span>
                  <span v-else class="label label-danger" v-text="resources['disabled']"> </span>
                </td> -->
                 <td v-else-if="column.field === 'subLevel'" class="text-left">
                  <!--<div class="small text-muted">{{result.company.companyTypeText}}</div>-->
                  {{ result.group2Code }}
                </td>
                <td class="text-left" v-else-if="column.field === 'description'">
                  {{result.description}}
                </td>
                <td class="text-left" v-else>
                  {{result[column.field]}}
                </td>
              </template>
              <!--                                    <td>-->
              <!--                                        <a :href="'/admin/checklist-center/maintenance/' + result.id + '/edit'" class="table-action-link edit" title="Edit">-->
              <!--                                            <img src="/images/icon/edit.svg" alt="edit" />-->
              <!--                                        </a>-->

              <!--                                        <div class="drowdown d-inline">-->
              <!--                                            <a href="javascript:void(0)" title="Change status" class="table-action-link change-status"-->
              <!--                                               data-toggle="dropdown" role="button"-->
              <!--                                               @click="changeShow(result.id)"-->
              <!--                                               aria-haspopup="true" aria-expanded="false">-->
              <!--                                                <img src="/images/icon/change-status.svg" alt="Change status" />-->
              <!--                                            </a>-->
              <!--                                            <ul class="dropdown-menu" :class="[(parseInt(result.id) === parseInt(showDrowdown)) ? `show` : ``]">-->
              <!--                                                <li class="dropdown-header"><span class="op-change-status" v-text="resources['change_status']"></span></li>-->
              <!--                                                <li v-if="!result.enabled">-->
              <!--                                                    <div class="dropdown-item" @click.prevent="enable(result)">-->
              <!--                                                        <span v-text="resources['enable']"></span>-->
              <!--                                                    </div>-->
              <!--                                                </li>-->
              <!--                                                <li v-if="result.enabled">-->
              <!--                                                    <div class='dropdown-item' @click.prevent="disable(result)">-->
              <!--                                                        <span v-text="resources['disable']"></span>-->
              <!--                                                    </div>-->
              <!--                                                </li>-->
              <!--                                                &lt;!&ndash;<li>&ndash;&gt;-->
              <!--                                                    &lt;!&ndash;<div class="dropdown-item" @click.prevent="deleteChecklist(result)"><span v-text="resources['delete']"></span></div>&ndash;&gt;-->
              <!--                                                &lt;!&ndash;</li>&ndash;&gt;-->
              <!--                                            </ul>-->
              <!--                                        </div>-->
              <!--                                        <a href="javascript:void(0)"-->
              <!--                                           @click.prevent = "showRevisionHistory(result.id)"-->
              <!--                                           title="View edit history"-->
              <!--                                           class="table-action-link revision-history">-->
              <!--                                            <img src="/images/icon/revision-history.svg" alt="View edit history" />-->
              <!--                                        </a>-->
              <!--                                        <a href="javascript:void(0)" @click="showSystemNoteModal(result)" title="Add note" class="table-action-link note">-->
              <!--                                            <img src="/images/icon/note.svg" alt="Add note" />-->
              <!--                                        </a>-->
              <!--                                    </td>-->
            </tr>
            </tbody>
          </table>

          <div class="no-results d-none" v-text="resources['no_results']"></div>
          <div class="row">
            <div class="col-md-8">
              <ul class="pagination">
                <li v-for="(data, index) in pagination" class="page-item"
                    :class="{active: data.isActive}">
                  <a class="page-link" @click="paging(data.pageNumber)">{{data.text}}</a>
                </li>
              </ul>
            </div>
<!--            <div class="col-auto ml-auto">
              <a href="/admin/checklist-center/maintenance/new" class="btn btn-primary"><i class="fa fa-plus"></i>
                <span v-text="resources['create_checklist']"></span>
              </a>
            </div>-->
          </div>
        </div>
      </div>
    </div>
  </div>
    <sublevel-codelist-form @parentmethod="getUpdatedData" :resources="resources"></sublevel-codelist-form>
     <!-- <downtime-create-codelist @parentmethod="getUpdatedData" :resources="resources"></downtime-create-codelist> -->
</div>
</template>


<script>
module.exports = {
  components: {
                'sublevel-codelist-form': httpVueLoader('./sublevel-codelist-form.vue'),
            },
  props: {
    resources: Object,
  },
  data() {
    return {
      showDrowdown: null,
      'results': [],
      'total': 0,
      'pagination': [],
      'requestParam': {
        'query': '',
        'enabled': 'Y',
        'companyType': 'all',
        'id': '',
        'sort': 'id,desc',
        'page': 1,
      },
      currentSortType: 'id',
       role_data: {},
        list: [],
      isDesc: true,
      pageType: 'CHECKLIST_MAINTENANCE',
      allColumnList: [],
      showDropdown: false,
      listChecked: [],
      listCheckedTracked: {},
      isAll: false,
      isAllTracked: [],
      listCheckedFull: [],
      cancelToken: undefined
    }
  },
  watch: {
    resources(newVal, oldVal) {
      if (newVal && Object.entries(newVal).length > 0 && (oldVal == null || Object.entries(oldVal).length == 0)) {
        console.log('change resources');
        this.callSetAllColumnList();
      }
    },
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
  mounted() {
    this.callSetAllColumnList();
    this.paging(1);
    this.$nextTick(function() {
      Common.removeWave(500);
    });
  },
  methods: {
      // async mounted() {
      //           await this.getAllData().then((result) => {
      //               this.results = result.data.content;
      //               this.pagination = Common.getPagingData(result.data);
      //               this.total = this.list.length;
      //           })
      //       },
             async getUpdatedData() {
                   await this.getAllData().then((result) => {
                      console.log(result, '----------------------results from another API')
                        this.results = result.data.content;
                        this.listChecked = [];
                        this.total = this.list.length;
                    })

                },

      getAllData() {
                    return axios.get('/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON_GROUP2/data' + '?' + Common.param(this.requestParam) )
                        .catch(function (error) {
                            Common.alert(error.response.data);
                        });
                },

    goToNew() {
                    var child = Common.vue.getChild(this.$children, 'sublevel-codelist-form');
                    if (child != null) {
                        child.showFormNewRole();
                    }
    },
    closeShowDropDown () {
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
          status: this.requestParam.status,
        });
      } else {
        this.listChecked = [];
        this.isAllTracked = this.isAllTracked.filter(item => item.page !== this.requestParam.page && item.status !== this.requestParam.status);
      }
      this.listCheckedTracked[this.requestParam.status][this.requestParam.page] = this.listChecked;
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
    check: function (e, result) {

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
    // goToEdit () {
    //   console.log(this.listChecked[0], 'this.listChecked[0]')
    //   window.location.href = `/admin/checklist-center/maintenance/${this.listChecked[0]}/edit`
    // },
      goToEdit() {
                    this.role_data = this.results.filter((item) => item.id === this.listChecked[0])
                    console.log(this.role_data, "Checked Data for Edit by Saad")
                    var child = Common.vue.getChild(this.$children, 'sublevel-codelist-form');
                    if (child != null) {
                        child.showEditRole(this.role_data);
                    }
                },

    callSetAllColumnList(){
      if(this.resources && Object.entries(this.resources).length>0 && this.allColumnList.length==0){
        this.setAllColumnList();
      }
    },
    setAllColumnList() {
      this.allColumnList = [
        // { label: this.resources['checklist_id'], field: 'checklistCode', default: true, mandatory: true, sortable: true, defaultSelected: true, defaultPosition: 0},
        // { label: this.resources['company_name'], field: 'companyName', default: true, sortable: true , sortField: 'company.name', defaultSelected: true, defaultPosition: 1},
        // { label: this.resources['company_id'], field: 'companyId', default: true, sortable: true, defaultSelected: true, defaultPosition: 2},
        // { label: this.resources['address'], field: 'address', default: true, sortable: true, sortField: 'company.address', defaultSelected: true, defaultPosition: 3},
        // { label: this.resources['status'], field: 'enabled', default: true, sortable: true, defaultSelected: true, defaultPosition: 4},
       { label: this.resources['code_title'], field: 'codeTitle', default: true, mandatory: true, sortField: 'title', sortable: true, defaultSelected: true, defaultPosition: 0},
        { label: this.resources['code_id'], field: 'codeId', default: true, sortable: true , sortField: 'code', defaultSelected: true, defaultPosition: 1},
        { label: this.resources['company'], field: 'company', default: true, sortable: true, sortField: 'group1Code', defaultSelected: true, defaultPosition: 2},
        { label: this.resources['main_level'], field: 'mainLevel', default: true, sortable: true, sortField: 'group1Code', defaultSelected: true, defaultPosition: 3},
        { label: this.resources['sub_level'], field: 'subLevel', default: true, sortable: true, sortField: 'group2Code', defaultSelected: true, defaultPosition: 4},
         { label: this.resources['description'], field: 'description', default: true, sortField: 'description', sortable: true, defaultSelected: true, defaultPosition: 5},

     ]
      this.resetColumnsListSelected();
      this.getColumnListSelected()
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach(item => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field
        }
      });
    },
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
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
          this.allColumnList.sort(function(a, b) {
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
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter(item => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
    },

    saveSelectedList: function (dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return {...item, position: index}
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
        self.allColumnList.sort(function(a, b) {
          return a.position - b.position;
        });
        self.$forceUpdate();
      })
    },
    changeShow(resultId) {
      if(resultId !=null) {
        this.showDrowdown = resultId
      } else {
        this.showDrowdown = null
      }
    },
    changeStatus: function() {
      this.paging(1);
    },

    tab: function (companyType) {
      this.total=null;
      this.listChecked = []
      this.currentSortType = 'id';
      this.requestParam.sort = 'id,desc';
      this.requestParam.enabled = 'Y';
      this.requestParam.status = companyType;
      this.requestParam.companyType = companyType;
      this.paging(1);
    },
    tabToDisable: function (status) {
      this.listChecked = []
      this.currentSortType = 'id';
      this.requestParam.sort = 'id,desc';
      this.requestParam.status = status;
      this.requestParam.enabled = 'N';
      this.requestParam.companyType = '';
      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },

    deleteChecklist: function (checklist) {

      if (!confirm('Are you sure you want to delete?')) {
        return;
      }
      var param = {
        id: checklist.id
      };
      let self=this;
      axios.delete('/api/checklist' + '/' + checklist.id, param).then(function (response) {
        if (response.data.success) {
          this.paging(1);
        } else {
          alert(response.data.message);
        }
      }).catch(function (error) {
        console.log(error.response);
      });
    },

    enable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = true;
        this.save(user[0], 'enable');
      } else {
        user.forEach(function(item) {
          item.enabled = true
        });
        this.saveBatch(user, 'enable')
      }
    },
    disable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = false;
        this.save(user[0], 'disable');
      } else {
        user.forEach(function(item) {
          item.enabled = false
        });
        this.saveBatch(user, 'disable')
      }
    },
    saveBatch (user, value) {
     
      const idArr = user.map((item) => {
        return item.id
      })

       console.log(idArr, 'user')
      let self=this;
      axios.put('/api/common/cod-stp' + '/' + value +'?id='+ idArr.toString()).then(function (response) {
        self.paging(1);
        console.log(response)
      }).catch(function (error) {
        console.log(error.response);
      }).finally(() => {
        self.listChecked = []
        self.isAll = false
        self.showDropdown = false
      })
    },

    save: function (codelist, value) {
      //var param = Common.param(item);
      let self=this;
      axios.put('/api/common/cod-stp' + '/' + value +'?id='+ codelist.id).then(function (response) {
        console.log(response)
        self.paging(1);
      }).catch(function (error) {
        console.log(error.response);
      }).finally(() => {
        self.listChecked = []
        self.isAll = false
        self.showDropdown = false
      })
    },
    paging: function (pageNumber) {
      var self = this;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let requestParam = JSON.parse(JSON.stringify(self.requestParam));
      if (requestParam.companyType === 'all') {
        delete requestParam.companyType;
      }
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

      var param = Common.param(requestParam);
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel('new request')
      }
      this.cancelToken = axios.CancelToken.source()
      axios.get('/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON_GROUP2/data' + '?' + param, {
        cancelToken: this.cancelToken.token
      }).then(function (response) {
        self.total = response.data.totalElements;
        self.results = response.data.content;
        console.log("Downtime data by Saad" ,self.results)

        console.log("Is it Enable or Diable?",self.requestParam.enabled);

        self.pagination = Common.getPagingData(response.data);
        Common.handleNoResults('#app', self.results.length);

        var pageInfo = self.requestParam.page === 1 ? '' : '?page=' + self.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (self.results.length > 0) {
          Common.triggerShowActionbarFeature(self.$children);
        }
      }).catch(function (error) {
        console.log(error.response);
      });
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
      this.paging(this.requestParam.page);
    },
    showRevisionHistory: function(id){
      var child = Common.vue.getChild(this.$parent.$children, 'revision-history');
      if (child != null) {
        child.showRevisionHistories(id, this.pageType);
      }
    },
    showSystemNoteModal(result){
      var child = Common.vue.getChild(this.$parent.$children, 'system-note');
      if (child != null) {
        child.showSystemNote({id: result});
      }
    },
  },
}

</script>
<style scoped>

</style>