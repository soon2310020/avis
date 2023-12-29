<template>
  <div
      style="z-index: 4999"
      id="op-order-popup"
      class="modal fade"
      role="dialog"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div style="box-shadow: 0 0 3px #00000029" class="modal-content">
        <div class="modal-body">
          <div class="modal-title">
            <div
                class="head-line"
                style="
                  position: absolute;
                  background: #52a1ff;
                  height: 8px;
                  border-radius: 6px 6px 0 0;
                  top: 0;
                  left: 0;
                  width: 100%;
                "
            ></div>
            <span class="span-title">My Completion Orders</span>
<!--            <svg @click="onCloseModal" style="cursor: pointer" id="Group_8480" data-name="Group 8480" xmlns="http://www.w3.org/2000/svg" width="11.699" height="11.699" viewBox="0 0 11.699 11.699">-->
<!--              <rect id="Rectangle_10" data-name="Rectangle 10" width="2.363" height="14.181" transform="translate(10.028 0) rotate(45)" fill="#fff"/>-->
<!--              <rect id="Rectangle_11" data-name="Rectangle 11" width="2.364" height="14.181" transform="translate(11.699 10.028) rotate(135)" fill="#fff"/>-->
<!--            </svg>-->
            <span
                class="close-button"
                style="
                  font-size: 25px;
                  display: flex;
                  align-items: center;
                  height: 17px;
                  cursor: pointer;
                "
                @click="onCloseModal"
                aria-hidden="true"
              >
              <span class="t-icon-close"></span>
            </span>
          </div>
          <div class="modal-content-order">
            <div class="modal-content-order-inside">
              <ul class="nav nav-tabs">
                <li class="nav-item">
                  <a
                      style="padding: 0.5rem 1rem;border: 1px solid transparent"
                      class="nav-link"
                      :class="{'active': tab == 'received'}"
                      href="#"
                      @click.prevent="changeTab('received')"
                  >
                    <span>Received Completion Orders</span>
                    <span
                        style="position: static!important;"
                        class="badge badge-light badge-pill"
                    >{{ listReceivedOrder ? listReceivedOrder.length : '0' }}</span>
                  </a>
                </li>
                <li class="nav-item">
                  <a
                      style="padding: 0.5rem 1rem;border: 1px solid transparent"
                      class="nav-link"
                      :class="{'active': tab == 'created'}"
                      href="#"
                      @click.prevent="changeTab('created')"
                  >
                    <span>Created Completion Orders</span>
                    <span
                        style="position: static!important;"
                        class="badge badge-light badge-pill"
                    >{{ listCreatedOrder.content ? listCreatedOrder.content.length : '0' }}</span>
                  </a>
                </li>
              </ul>
              <div v-if="tab === 'received'" class="received-items">
                <div v-for="(item, index) in listReceivedOrder" :key="index" class="received-item">
                  <div class="received-item-header">
                    Data Completion Order <b style="margin: 0 4px">{{ item.orderId }}</b> was assigned to you by <b style="margin: 0 4px">{{ item.managerName }}</b> from <b style="margin: 0 4px">{{ item.companyName }}.</b>
                  </div>
                  <div class="received-item-body">
                    <div class="received-item-body_row">
                      <div class="received-item-column">
                        <b>{{ item.objectType }}</b>
                      </div>
                      <div class="received-item-column">
                        <b>Due Date</b>
                      </div>
                      <div class="received-item-column">
                      </div>
                    </div>
                    <div v-for="(item2, index2) in item.items.content" :key="index2" class="received-item-body_row">
                      <div class="received-item-column link-order">
                        {{ item2.name }}
                      </div>
                      <div class="received-item-column">
                        {{ item2.dueDate }}
                      </div>
                      <div class="received-item-column">

                        <a v-if="item2.completed" style="white-space: nowrap; pointer-events: none; width: 118px; height: 30px; line-height: 16px; display: block; text-align: center;" href="javascript:void(0)" class="btn-custom btn-custom-primary inactive-order">
                            <span style="margin-right: 2px;">
                                Complete
                            </span>
                        </a>
                        <a v-else :id="'completion-data-order' + item.objectType + index2 + index" @click="showCompleteDataFunction(item.objectType, item2, 'completion-data-order' + item.objectType + index2 + index)" style="white-space: nowrap;" href="javascript:void(0)" class="btn-custom btn-custom-primary">
                            <span style="margin-right: 2px;">
                                Complete Data
                            </span>
                        </a>
                      </div>
                    </div>
                  </div>
                  <div>
                    <div>
                      <ul class="pagination">
                        <li v-for="(data, index) in returnPaging(item.items)" class="page-item"
                            :class="{'active': data.isActive}">
                          <a class="page-link" @click="pagingReceived(item.id, item.objectType, data.pageNumber, item)">{{data.text}}</a>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="tab === 'created'" class="received-items created-order">
                <div v-for="(item, index) in listCreatedOrder.content" :key="index" class="received-item">
                  <div class="received-item-header">
                    You assigned Data Completion Order <b style="margin: 0 4px">{{ item.orderId }}</b> to <b style="margin: 0 4px">{{ item.users.length > 1 ? item.users.length + ' users.' : item.users[0].name }}</b> <span>{{ item.users.length > 1 ? '' : 'at ' }}</span> <b style="margin: 0 4px">{{ item.users.length > 1 ? '' : item.users[0].company.name + '.' }}</b>
                  </div>
                  <div v-if="reissueOpening !== index + '-order'" class="received-item-body">
                    <div class="create-item-body_row-first">
                      <div style="width: 22%;">
                        <b style="margin-right: 13px">Due Date</b>
                        <span>{{ item.dueDay }}</span>
                      </div>
                      <div style="width: 32%;margin-left: 3%;">
                        <b style="margin-right: 13px">Requested to</b>
                        <div style="position: relative">
                          <div v-if="item.users.length !== 0" class="avatar-zone">
                            <div v-for="(item, index) in item.users.slice(0, 4)" :key="index" class="avatar-item-w">
                              <a-tooltip placement="bottom">
                                <template slot="title">
                                  <div style="padding: 6px;font-size: 13px;">
                                    <div><b>{{item.name}}</b></div>
                                    <div>
                                      Company: <span v-if="item.company">{{ item.company.name }}</span>
                                    </div>
                                    <div>
                                      Department: {{ item.department }}
                                    </div>
                                    <div style="margin-bottom: 15px">
                                      Position: {{ item.position }}
                                    </div>
                                    <div>
                                      Email: {{ item.email }}
                                    </div>
                                    <div>
                                      Phone: {{ item.mobileNumber }}
                                    </div>
                                  </div>
                                </template>
                                <div class="avatar-item" :style="{ 'background-color': getRequestedColor(item.name,item.id)}">
                                  {{ convertName(item.name) }}
                                </div>
                              </a-tooltip>
                            </div>
                          </div>
                          <div v-if="item.users.length > 4" class="users-truncated">
                            +{{ item.users.length - 4 }}
                          </div>
                        </div>
                      </div>
                      <div style="width: 43%;justify-content: flex-end">
<!--                        v-click-outside="changeDropdown"-->
                        <div v-if="!item.completed" class="position-relative d-flex">
                          <a @click="openDropdown(index + '-order')" :id="index + '-order'" href="javascript:void(0)" class="btn-custom btn-outline-custom-primary">
                            <span style="margin-right: 2px;">
                              Add/ Remove User
                            </span>
                            <img v-show="dropdownOpening === index + '-order'" class="custom-dropdown" style="transform: unset;width: 9pt;height: 4.5pt;vertical-align: baseline;" src="/images/icon/dropdown-up.svg" alt="">
                            <img v-show="dropdownOpening === index + '-order'" class="custom-dropdown-hover" style="transform: unset;width: 9pt;height: 4.5pt;vertical-align: baseline;" src="/images/icon/dropdown-up-hover.svg" alt="">
                            <img v-show="dropdownOpening !== index + '-order'" class="custom-dropdown" style="transform: unset;width: 9pt;height: 4.5pt;vertical-align: baseline;" src="/images/icon/dropdown.svg" alt="">
                            <img v-show="dropdownOpening !== index + '-order'" class="custom-dropdown-hover" style="transform: unset;width: 9pt;height: 4.5pt;vertical-align: baseline;" src="/images/icon/dropdown-hover.svg" alt="">
                          </a>
                          <div v-if="dropdownOpening === index + '-order'" style="z-index: 9;top: calc(100% + 2px);position: absolute;left: 0;width: 250px">
                            <div style="position: absolute;left: 0">
                              <asigned-to-order v-if="dropdownOpening === index + '-order'" :change-part="changeUser" :key="item.id + '-order'" :id="item.id" :users-selected="item.users" :option-array-part="usersList" :resources="resources" :get-requested-color="getRequestedColor"></asigned-to-order>
                            </div>
                          </div>
                        </div>
                        <a v-else href="javascript:void(0)" class="btn-custom btn-custom-primary inactive-order">
                          <span>
                              Add/ Remove User
                          </span>
                          <img style="transform: unset;width: 9pt;height: 4.5pt;vertical-align: baseline;" src="/images/icon/dropdown-white.svg" alt="">
                        </a>
                        <a v-if="!item.completed" @click.prevent="openReissue(index + '-order', index + 're')" :id="index + 're'" style="margin-left: 8px" href="javascript:void(0)" class="btn-custom btn-custom-primary animationPrimary">
                          <span>
                              Re-issue Completion Order
                          </span>
                        </a>
                        <a v-else style="margin-left: 8px" href="javascript:void(0)" class="btn-custom btn-custom-primary inactive-order">
                          <span>
                              Completed
                          </span>
                        </a>
                      </div>
                    </div>
                    <div class="create-item-body_row-second">
                      <div :class="{'selected': item.numberOfMolds > 0}" class="create-item-body_row-second-item">
                        <span>Tooling</span>
                        <div class="order-number">
                          <span v-if="item.numberOfMolds > 0">
                            {{ item.numberOfMolds }}
                          </span>
                        </div>
                      </div>
                      <div :class="{'selected': item.numberOfParts > 0}" class="create-item-body_row-second-item">
                        <span>Part</span>
                        <div class="order-number">
                          <span v-if="item.numberOfParts > 0">
                            {{ item.numberOfParts }}
                          </span>
                        </div>
                      </div>
                      <div :class="{'selected': item.numberOfMachines > 0}" class="create-item-body_row-second-item">
                        <span>Machine</span>
                        <div class="order-number">
                          <span v-if="item.numberOfMachines > 0">
                            {{ item.numberOfMachines }}
                          </span>
                        </div>
                      </div>
                      <div :class="{'selected': item.numberOfCompanies > 0}" class="create-item-body_row-second-item">
                        <span>Company</span>
                        <div class="order-number">
                          <span v-if="item.numberOfCompanies > 0">
                            {{ item.numberOfCompanies }}
                          </span>
                        </div>
                      </div>
                      <div :class="{'selected': item.numberOfLocations > 0}" class="create-item-body_row-second-item">
                        <span>Location</span>
                        <div class="order-number">
                          <span v-if="item.numberOfLocations > 0">
                            {{ item.numberOfLocations }}
                          </span>
                        </div>
                      </div>
<!--                      <div :class="{'selected': item.numberOfCategories > 0}" class="create-item-body_row-second-item">-->
<!--                        <span>Category</span>-->
<!--                        <div class="order-number">-->
<!--                          <span v-if="item.numberOfCategories > 0">-->
<!--                            {{ item.numberOfCategories }}-->
<!--                          </span>-->
<!--                        </div>-->
<!--                      </div>-->
                    </div>
                  </div>
                  <div v-else-if="!successText" class="received-item-body-popup">
                    <div style="display: flex;align-items: center;">
                      <b style="margin-right: 29px">Please Select New Due Date</b>
                      <div>
                        <a-date-picker
                            placeholder="Select Date"
                            format="MMM Do, YYYY"
                            v-model="dueDayReIssue"
                        >
                          <img slot="suffixIcon" src="/images/icon/expand-button.svg" alt="">
                        </a-date-picker>
                      </div>
                    </div>
                    <a v-if="dueDayReIssue !== undefined" :id="item.id + 'order'" @click="reIssue(item.id, item.id + 'order')" style="margin-left: 8px;position: absolute;right: 9.5px;bottom: 9.5px;" href="javascript:void(0)" class="btn-custom btn-custom-primary animationPrimary">
                        <span>
                            Re-issue Completion Order
                        </span>
                    </a>
                    <a v-else style="margin-left: 8px;position: absolute;right: 9.5px;bottom: 9.5px;" href="javascript:void(0)" class="btn-custom btn-custom-primary inactive-order">
                        <span>
                            Re-issue Completion Order
                        </span>
                    </a>
                  </div>
                  <div v-else class="received-item-body-popup received-success">
                    Success!
                  </div>
                </div>
                <div class="create-pagination">
                  <div>
                    <ul class="pagination">
                      <li v-for="(data, index) in paginationCreated" class="page-item"
                          :class="{'active': data.isActive}">
                        <a class="page-link" @click="pagingCreated(data.pageNumber)">{{data.text}}</a>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    showCompleteData: Function,
    showCompleteDataHeader: Function,
    changeOpenOrderModal: Function,
    parentName: String,
    // getRequestedColor: Function,
    // listCreatedOrder: Object,
    // listReceivedOrder: Array
  },
  components: {
    'asigned-to-order': httpVueLoader('/components/AsignedToOrder.vue')
  },
  data() {
    return {
      isChecked: false,
      tab: 'received',
      total: {
        received: 10,
        created: 0
      },
      dropdownOpening: false,
      dataType: [
        { name: 'Tooling', number: 5 },
        { name: 'Part', number: 0 },
        { name: 'Machine', number: 4 },
        { name: 'Company', number: 15 },
        { name: 'Location', number: 25 },
        { name: 'Category', number: 35 }
      ],
      listReceivedOrder: [],
      listCreatedOrder: [],
      usersList: [],
      userParams: {},
      currentId: false,
      currentUser: null,
      // paginationReceived: [],
      paginationCreated: [],
      requestParam: {
        page: 1,
        size: 20,
        id: null,
        objectType: 'TOOLING'
      },
      requestParamCreated: {
        page: 1,
        size: 2
      },
      reissueOpening: null,
      dueDayReIssue: null,
      successText: false
      // totalReceived: 0,
      // totalCreated: 0
    };
  },
  watch: {
    dropdownOpening (newVal) {
      if (newVal === null && this.currentId && this.currentId.length !== 0) {
        this.updateUser()
      }
    }
  },
  methods: {
    // getRequestedColor(name, id){
    //   let f=this.convertName(name);
    //   let index=0;
    //   if (f.length > 0) {
    //     index = f.charCodeAt(0);
    //     if (index > 65) index -= 65;
    //     if (f.length > 1) {
    //       index += f.charCodeAt(1);
    //       if (index > 65) index -= 65;
    //     }
    //   }
    //   if(id){
    //     index +=id;
    //   }
    //   index = index % this.colorList.length;
    //   return this.colorList[index]
    // },
    getRequestedColor(name, id){
      return Common.getRequestedColor(name, id)
    },
    showCompleteDataFunction (name, data, id) {
      console.log(data, 'data')
      const el = document.getElementById(id)
      el.classList.add('primary-animation')
      const self = this
      setTimeout(() => {
        if (self.parentName !== 'Header') {
          self.showCompleteData(data, name)
        } else {
          self.showCompleteDataHeader(data, name)
        }
        el.classList.remove('primary-animation')
      }, 700)
    },
    async reIssue(id, idRef) {
      const params = {
        "id": id,
        "dueDay": this.timeConverter(this.dueDayReIssue.unix())
      }
      const self = this
      const el = document.getElementById(idRef)
      el.classList.add('primary-animation')
      await axios.post('/api/data-completion/update-order', params).then((res) => {
        console.log(res, 'ress')
        self.successText = true
      }).finally(async () => {
        await this.updateUser();
        this.pagingCreated();
        setTimeout(() => {
          el.classList.remove('primary-animation')
        }, 700)
        setTimeout(() => {
          self.successText = false
          self.reissueOpening = null
        }, 4000)
      })
    },
    timeConverter (UNIX_timestamp){
      var a = new Date(UNIX_timestamp * 1000);
      var months = ['01','02','03','04','05','06','07','08','09','10','11','12'];
      var year = a.getFullYear();
      var month = months[a.getMonth()];
      var date = a.getDate();
      date2 = ("0" + date).slice(-2);
      var hour = a.getHours();
      hour2 = ("0" + hour).slice(-2);
      var min = a.getMinutes();
      min2 = ("0" + min).slice(-2);
      var sec = a.getSeconds();
      sec2 = ("0" + sec).slice(-2);
      var time = year + '' + month + '' + date2 + '' + hour2 + '' + min2 + '' + sec2 ;
      return time;
    },
    pagingReceived (id, type, pageNumber, item) {
      const apiUrl = '/api/data-completion/list-received-items-by-type'
      var self = this;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      self.requestParam.id = id;
      self.requestParam.objectType = type;
      let requestParam = JSON.parse(JSON.stringify(self.requestParam));

      var param = Common.param(requestParam);
      axios.get(apiUrl + '?' + param).then((response) => {
        item.items = response.data.data
      }).catch((error) => {
        console.log(error.response);
      }).finally(()=> {
        this.$forceUpdate()
      })
    },
    returnPaging (data) {
      return Common.getPagingData(data)
    },
    async pagingCreated (pageNumber) {
      const apiUrl = '/api/data-completion/list-created-orders'
      var self = this;
      self.requestParamCreated.page = pageNumber === undefined ? 1 : pageNumber;
      let requestParam = JSON.parse(JSON.stringify(self.requestParamCreated));

      var param = Common.param(requestParam);
      axios.get(apiUrl + '?' + param).then(await function (response) {
        // self.totalCreated = response.data.totalElements;
        self.listCreatedOrder = response.data.data
          self.paginationCreated = Common.getPagingData(response.data.data);
        document.querySelector('.op-list').style.display = 'table-row-group';
        document.querySelector('.pagination').style.display = 'flex';
      }).catch(function (error) {
        console.log(error.response);
      }).finally(()=> {
        this.$forceUpdate()
      })
    },
    getUsers () {
      const params = {
        status: 'active',
        query: '',
        page: 1,
        size: 9999
      }
      return axios.get('/api/users?status=active&query=&page=1&size=9999').then((res) => {
        this.usersList = res.data.content
        console.log(res, '/api/users')
      })
    },
    changeUser (id, user) {
      this.userParams = {
        "id": id,
        "assignedUsers": user.map((res) => {
          return { id: res.id}
        })
      }
      this.currentId = this.listCreatedOrder.content.filter(res => res.id === id)
      this.currentUser = user
    },
    async updateUser () {
      this.currentId[0].users = this.currentUser
      await axios.post('/api/data-completion/update-order', this.userParams).then((res) => {
        console.log(res.data, 'res')
      })
    },
    // getListCreated () {
    //   axios.get('/api/data-completion/list-created-orders').then((res) => {
    //     this.listCreatedOrder = res.data.data
    //   })
    // },
    getListReceived () {
      axios.get('/api/data-completion/list-received-orders').then((res) => {
        this.listReceivedOrder = res.data.data
      })
    },
    getDetailsReceived () {
      const params = {
        id: 1,
        objectType: 'TOOLING',
        page: 1,
        size: 20
      }
      axios.get('/api/data-completion/list-received-items-by-type').then((res) => {
        console.log(res, 'res2')
      })
    },
    convertName (name) {
      var split_names = name.trim().split(" ");
      if (split_names.length > 1) {
        return (split_names[0].charAt(0) + split_names[split_names.length - 1].charAt(0));
      }
      return split_names[0].charAt(0);
    },
    // changeTypeSelect (type) {
    //   if (this.typeSelected.includes(type)) {
    //     console.log(this.typeSelected, 'come 1')
    //     this.typeSelected = this.typeSelected.filter(
    //         value => value !== type
    //     );
    //   } else {
    //     console.log(this.typeSelected, 'come 2')
    //     this.typeSelected.push(type);
    //   }
    // },
    changeDropdown() {
      this.dropdownOpening = null
    },
    changeReissue() {
      this.reissueOpening = null
    },
    openDropdown (item) {
      const self = this
      const el = document.getElementById(item)
      el.classList.add('outline-animation')
      if (self.dropdownOpening === item) {
        self.dropdownOpening = null
      } else {
        self.dropdownOpening = item
      }
      setTimeout(() => {
        el.classList.remove('outline-animation')
      }, 700)
    },
    openReissue (item, id) {
      console.log('item',item)
      const self = this
      this.dueDayReIssue = null
      const el = document.getElementById(id)
      console.log(el)
      el.classList.add('primary-animation')
      setTimeout(() => {
        if (self.reissueOpening === item) {
          self.reissueOpening = null
        } else {
          self.reissueOpening = item
        }
        el.classList.remove('primary-animation')
      }, 700)
    },
    changeTab (tab) {
      this.tab = tab
    },
    showOrderPopup (tab) {
      this.tab = tab
      console.log(tab);
      this.getListReceived()
      this.pagingCreated()
      setTimeout(() => {
        console.log('this.listReceivedOrder: ', this.listReceivedOrder)
        console.log('this.listCreatedOrder: ', this.listCreatedOrder)
      }, 500);
      
      $(this.getRootId() + "#op-order-popup").modal("show");
    },
    onCloseModal () {
      console.log('close')
      this.tab = 'received'
      this.dropdownOpening = null
      this.reissueOpening = null
      this.dueDayReIssue = null
      $(this.getRootId() + "#op-order-popup").modal("hide");
      this.changeOpenOrderModal(false)
    },
    animationPrimary () {
      $('.animationPrimary').click(function () {
        $(this).addClass('primary-animation');
        $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function(event) {
          $(this).removeClass('primary-animation')
        });
      });
    }
  },
  mounted() {
    const self = this
    this.$nextTick(function() {
      // 모든 화면이 렌더링된 후 실행합니다.
      //this.paging(1);
      self.animationPrimary();
      self.getListReceived()
      // self.pagingReceived()
      self.pagingCreated()
      self.getUsers()
    });
  }
};
</script>
<style>
.app-header .nav-item .nav-link .badge {
    position: absolute;
    top: 85% !important;
    left: 90% !important;
    margin-top: -16px;
    margin-left: 0;
}
</style>
<style scoped>
.btn-custom{
  line-height: 17px !important;
}
.nav-tabs {
  border: unset!important;
  position: absolute;
  top: 11.5px;
  width: 100%;
}
.nav-tabs .nav-link {
  color: #4b4b4b!important;
}
.nav-tabs .nav-link.active {
  border: 1px solid #C1C7CD!important;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
}
.nav-tabs .nav-link:not(.active):hover {
  border-color: unset;
}
.modal-content {
  /*min-width: 800px;*/
  /*height: 400px;*/
  top: 5px;
  margin: auto;
  background: unset;
  border: unset;
  box-shadow: unset!important;
}
.modal-content-order {
  /*padding: 1px 4px;*/
  /*border: 1px solid #C1C7CD;*/
  background: #FFFFFF;
  /*border-radius: 3px;*/
}
.modal-content-order-inside {
  padding: 11.5px 6px 50px 24.5px;
  /*box-shadow: 0px 3px 6px rgb(0 0 0 / 16%);*/
  position: relative;
  min-height: 200px;
  font-size: 14px;
}
.modal-body {
  padding: 0;
  overflow-x: hidden;
}
.modal-title {
  /*background: linear-gradient(*/
  /*    90deg , rgba(128,117,251,1) 51%, rgba(104,138,255,1) 69%);*/
  background: #F5F8FF;
  border-radius: unset;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 25.5px;
  border-radius: 6px 6px 0 0;
}
.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}
.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: 0;
  width: 100%;
}
.modal-title .span-title {
  color: #4B4B4B;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
}
.received-items {
  margin-top: 39px;
  height: 470px;
  padding-right: 5.5px;
  overflow-x: hidden;
}
.received-item {
  border: 1px solid #C1C7CD;
  box-shadow: 0px 3px 6px rgb(0 0 0 / 16%);
  padding: 5.3px 5.9px;
  border-radius: 2px;
  margin-bottom: 15.6px;
}
.received-item-header {
  background: #F0F3F5;
  height: 25px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  line-height: 100%;
}
.received-item-body {
  padding: 3px 7.1px;
}
.received-item-body_row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13.5px 16px 13.5px 12px;
}
.received-item-body_row:nth-child(even) {
  background: rgb(241 244 249 / 30%);
}
.received-item-column.link-order {
  color: #0075FF
}
.received-item-column.date-order {
  color: #4B4B4B
}
.received-item-column {
  width: 43%;
}
.received-item-column:last-child {
  width: 14%;
}
.nav-tabs .nav-link:not(.active):hover {
  border: 1px solid transparent;
}
.created-order .received-item-body {
  padding: 12px 24px;
}
.created-order .create-item-body_row-first > div {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.created-order .create-item-body_row-first {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.create-item-body_row-second-item:nth-child(4), .create-item-body_row-second-item:nth-child(5), .create-item-body_row-second-item:nth-child(6) {
  margin-bottom: 0!important;
}
.created-order .create-item-body_row-second .create-item-body_row-second-item{
  width: calc(33.33% - 12.6px);
  padding: 4px 13.3px 4px 8px;
  border: 1px solid rgb(112, 112, 112, 26%);
  color: #A2A2A2;
  /*margin-right: 19px;*/
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.created-order .create-item-body_row-second .create-item-body_row-second-item .order-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #fff;
  border-radius: 999px;
  font-size: 16px;
  color: #4B4B4B;
  font-weight: 500;
}
.create-item-body_row-second-item.selected {
  border: 1px solid #3491FF!important;
  background: rgba(172, 210, 255, 50%);
  border-radius: 3px;
  color: #4b4b4b!important;
}
.created-order .create-item-body_row-second {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  padding: 12px 26px;
}
.avatar-zone {
  display: flex;
  align-items: center;
  width: unset!important;
  padding: 0!important;
  /*overflow: scroll;*/
  background-image: unset!important;
  overflow-x: scroll;
}
::-webkit-scrollbar{
  height: 5px;
}
.avatar-item {
  width: 25px;
  height: 25px;
  padding: 1px;
  font-weight: 400;
  font-size: 14px;
  background: #AEACF9;
  border-radius: 99%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: capitalize;
}
.avatar-item-w:not(:last-child) {
  margin-right: 3px;
}
.users-truncated {
  position: absolute;
  right: -9px;
  top: -3px;
  background: #4B4B4B;
  color: #fff;
  width: 14px;
  height: 14px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
}
.inactive-order {
  background: #C4C4C4!important;
  border: 1px solid rgb(196, 196, 196) !important;
  color: #fff!important;
}
/* width */
.received-items::-webkit-scrollbar {
  width: 6px;
  height: 7px;
}

/* Track */
.received-items::-webkit-scrollbar-track {
  background: transparent;
}

/* Handle */
.received-items::-webkit-scrollbar-thumb {
  background: #4B4B4B!important;
  border-radius: 10px;
}

/* Handle on hover */
.received-items ::-webkit-scrollbar-thumb:hover {
  background: #4B4B4B!important;
}
.page-link {
  background: #F3F3F3;
  border-color: #fff;
  color: #707070;
  font-size: 9px;
  padding: 4px 6px;
}
.active .page-link {
  background: #595959;
  border-color: #fff;
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}
.page-item:last-child .page-link  {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}
.page-link:hover {
  z-index: 0;
  background: #F3F3F3;
  border-color: #fff;
}
.active .page-link:hover {
  z-index: 0;
  background: rgb(89, 89, 89);
  border-color: #fff;
  color: #fff;
}
.pagination {
  justify-content: flex-end;
  margin-bottom: 0;
}
.create-pagination {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 5px 24px 20px 24px;
  background: #fff;
}
.received-item-body-popup {
  padding: 12px 24px;
  height: 174px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.received-item-body-popup b {
  font-size: 14px;
  color: #4B4B4B;
}
.received-item-body-popup.received-success {
  font-size: 14px;
  color: #3491FF;
  font-weight: bold;
}
</style>