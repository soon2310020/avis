<template>
  <div>
    <div
        id="userRole"
        class="modal fade"
        tabindex="-1"
        role="dialog"
        aria-labelledby="title-part-chart"
        aria-hidden="true"
    >
      <div class="modal-dialog h-100 modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-title">
            <div class="head-line"></div>
            <div>
              <span class="span-title">Role - User Configuration</span>
            </div>
            <span class="close-button" data-dismiss="modal" aria-label="Close" @click="close">
            <span class="t-icon-close"></span>
          </span>
          </div>

          <div class="modal-body mx-1 mx-lg-5">
            <div class="row">
              <div class="col-md-2 role-name">
                Role
              </div>
              <div class="col-md-6 custom-role role-name">{{ this.roleName.name }}</div>
            </div>

            <div class="form-group d-flex align-items-center form-location row">
              <div>
                <label class="col-form-label custom-style location-label">
                  <span>Select Plant</span>
                  <span class="avatar-status badge-danger"></span>
                  <span class="badge-require"></span>
                </label>
              </div>
<!--              <div class="">-->
<!--                <input type="text" id="location" v-model="location.name" class="form-control input-location-edit"-->
<!--                       disabled="true"-->
<!--                       :readonly="true" required>-->
<!--              </div>-->
<!--
<!--                <button type="button" class="btn btn-default btn-search-location" data-toggle="modal"-->
<!--                        data-target="#op-location-search" @click="showLocation">Plant Search-->
<!--                </button>-->
<!--              </div>-->
              <div class="col-md-5 p-0">
                <common-button
                  :title="plant_title"
                  :is-show="showPlant"
                  @click="handleToggle">
                </common-button>
                <common-select-popover
                    :is-visible="showPlant"
                    @close="closeShowPlant"
                >
                  <common-select-dropdown
                      :style="{ position: 'static' }"
                      :class="{show: showPlant}"
                      :items="getLocationList"
                      :click-handler="categorySelect"
                      :searchbox="true"
                      :placeholder="'Select plant'"
                  ></common-select-dropdown>
                </common-select-popover>
              </div>
            </div>

            <div class="d-flex flex-lg-row flex-column w-100  align-items-start justify-content-start">
              <div class="d-flex align-items-stretch flex-column w-100 ">
                <div class="btn-group">
                </div>
                <div class="setMargin">
                <span class="available-users">
                  Available Users
                </span>
                  <common-searchbar
                      :placeholder="availablePlaceholder"
                      :request-param="requestParam"
                      :on-search="availableUserFilter"
                      class="searchBox"
                  ></common-searchbar>
                </div>

                <div class="scroll-item">
                  <div class="selected-item" v-for="(user,index) in availableUserList">
                    <div
                        :style="{background:user.enable?null:'#C4C4C4 0% 0% no-repeat padding-box',color:user.enable?'#262931':'#FFFFFF'}"
                        class="availableListItem" :id="index" :disabled="user.enable==false">
                      {{ user.name }}
                    </div>
                    <div :style="{visibility:user.enable?'visible':'hidden'}" @click="availableUser(index)"
                         class="minus-item">
                      <svg class="minus-item-default" xmlns="http://www.w3.org/2000/svg" width="9.843" height="9.843"
                           viewBox="0 0 9.843 9.843">
                        <g id="Group_8883" data-name="Group 8883" transform="translate(0.861 0.862)">
                          <rect id="Rectangle_10" data-name="Rectangle 10" width="1.641" height="9.843"
                                transform="translate(8.982 3.24) rotate(90)" fill="#9d9d9d"/>
                          <rect id="Rectangle_11" data-name="Rectangle 11" width="1.641" height="9.843"
                                transform="translate(4.881 8.982) rotate(180)" fill="#9d9d9d"/>
                        </g>
                      </svg>
                      <svg class="minus-item-hover" xmlns="http://www.w3.org/2000/svg" width="9.843" height="9.843"
                           viewBox="0 0 9.843 9.843">
                        <g id="Group_8883" data-name="Group 8883" transform="translate(0.861 0.862)">
                          <rect id="Rectangle_10" data-name="Rectangle 10" width="1.641" height="9.843"
                                transform="translate(8.982 3.24) rotate(90)" fill="#3491ff"/>
                          <rect id="Rectangle_11" data-name="Rectangle 11" width="1.641" height="9.843"
                                transform="translate(4.881 8.982) rotate(180)" fill="#3491ff"/>
                        </g>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              <div class="ml-1 mt-1 ml-lg-5 d-flex align-items-end justify-content-between flex-column w-100  ">
                <div class="d-flex align-items-stretchh w-100 flex-column">
                  <div>
                <span class="assigned-users">
                  Assigned Users
                </span>
                    <common-searchbar
                        :placeholder="assignedPlaceholder"
                        :request-param="requestParam2"
                        :on-search="assignedUserFilter"
                        class="searchBox"
                    ></common-searchbar>
                  </div>

                  <div class="assignedUserScroll">
                    <div class="selected-item" v-for="(user,index) in assignedUserList">
                      <div class="assignedListItem">
                        {{ user.name }}
                      </div>
                      <div @click="assignedUser(index)" class="minus-item">
                        <svg class="minus-item-default" xmlns="http://www.w3.org/2000/svg" width="9.844" height="1.641"
                             viewBox="0 0 9.844 1.641">
                          <g id="Group_8891" data-name="Group 8891" transform="translate(0.861 -3.24)">
                            <rect id="Rectangle_10" data-name="Rectangle 10" width="1.641" height="9.843"
                                  transform="translate(8.982 3.24) rotate(90)" fill="#ddd"/>
                            <rect id="Rectangle_11" data-name="Rectangle 11" width="1.641" height="9.843"
                                  transform="translate(-0.861 4.88) rotate(-90)" fill="#ddd"/>
                          </g>
                        </svg>
                        <svg class="minus-item-hover" xmlns="http://www.w3.org/2000/svg" width="9.844" height="1.641"
                             viewBox="0 0 9.844 1.641">
                          <g id="Group_8891" data-name="Group 8891" transform="translate(0.861 -3.24)">
                            <rect id="Rectangle_10" data-name="Rectangle 10" width="1.641" height="9.843"
                                  transform="translate(8.982 3.24) rotate(90)" fill="#3491ff"/>
                            <rect id="Rectangle_11" data-name="Rectangle 11" width="1.641" height="9.843"
                                  transform="translate(-0.861 4.88) rotate(-90)" fill="#3491ff"/>
                          </g>
                        </svg>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="save w-100">
                  <button type="button" @click="save" class="btn btn-primary button-edit"
                          :class="{'disableClass':disable}" :disabled="disable">
                    Save
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
<!--    <location-search :resources="resources" parent-name="modal" @setlocation="callbackLocation"></location-search>-->
  </div>
</template>

<script>
module.exports = {
  name: "user-role-config",
  // components: {
  //   'location-search': httpVueLoader('../location-search.vue')
  // },
  data() {
    return {
      showPlant:false,
      plant_title:"Search Plant",
      resources: {},
      'requestParam': {
        lastAlert: true,
        'query': '',
        'status': 'alert',
        'sort': 'id,desc',
        'page': 1,

        'operatingStatus': '',
        'notificationStatus': ''
      },
      'requestParam2': {
        lastAlert: true,
        'query': '',
        'status': 'alert',
        'sort': 'id,desc',
        'page': 1,

        'operatingStatus': '',
        'notificationStatus': '',
      },
      tempList: [],
      roleName: [],
      temp: [],
      search: "",
      disable: true,
      availableUserList: [],
      assignedUserList: [],
      availablePlaceholder: "Search Available User",
      assignedPlaceholder: "Search Assigned User",
      location: [],
      getLocationList:[],
      newlist:[]
    };
  },

  async mounted() {
    await this.getResources().then(result => {
      this.resources = result.data;
    }).catch((error) => {
      Common.alert(error.response.data, 'Error');
    })
   await this.getLocation()
  },
  methods: {
    async getLocation(){
      await axios.get('/api/common/rol-stp/locations?size=1000').then((response) =>{
        this.newlist=response;
      });
      this.newlist.data.content.map(item=>{
        this.getLocationList.push({id:item.id,title:item.name});
      })
    },
    closeShowPlant(){
      this.showPlant=false;
    },
    handleToggle(flag){
      this.showPlant=flag;
    },
    async categorySelect(item) {
      this.location=item;
      this.showPlant=false;
      this.plant_title=item.title;
      this.assignedUserList = [];
      this.availableUserList = [];
      this.disable = false;
      await axios.get("/api/common/rol-stp/" + this.roleName.id + "/users?size=1000&locationId=" + this.location.id).then(result => {
        this.assignedUserList = result.data.content;
      }).catch(error => {
        Common.alert(error.response.data);
      })

      await axios.get("/api/common/rol-stp/" + this.roleName.id + "/available/users?size=1000&locationId=" + this.location.id).then(result => {
        var check;
        result.data.content.forEach(item => {
          check = false;
          this.assignedUserList.forEach(item1 => {
            if (item.id == item1.id) {
              check = true;
            }
          })
          if (check) {
            this.availableUserList.push({id: item.id, name: item.name, enable: false});
          } else {
            this.availableUserList.push({id: item.id, name: item.name, enable: true});
          }
        })
      }).catch(error => {
        Common.alert(error.response.data);
      })

      this.setAvailableuser();

      this.tempList = [...this.availableUserList];
      this.temp = [...this.assignedUserList];

    },
    showUserRoleConfig(roleName) {
      this.roleName = roleName;
      $("#userRole").modal("show");

    },
    close: function () {
      this.disable = true;
      this.location = [];
      this.availableUserList = [];
      this.assignedUserList = [];
      this.plant_title="Search Plant"
      this.tempList = [];
    },
    getResources() {
      return axios.get("/api/resources/getAllMessagesOfCurrentUser").then(function (response) {
        return response;
      }).catch(function (error) {
        Common.alert(error.response.data);
      });
    },
    availableUserFilter() {
      setTimeout(() => {
        const str = this.requestParam.query.trim();
        if (!str || str == '') {
          this.availableUserList = [...this.tempList];
        }
        if (this.tempList != null) {
          let filter = this.tempList.filter((item) => {
            return item.name != null && item.name.toUpperCase().includes(str.toUpperCase());
          });
          let result = filter.sort((firstItem, secondItem) => {
            return firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1;
          });
          if (result != null) {
            this.availableUserList = result;
          } else {
            this.availableUserList = [];
          }
        }
      }, 500);
    },
    assignedUserFilter() {
      setTimeout(() => {
        const str = this.requestParam2.query.trim();
        if (!str || str == '') {
          this.assignedUserList = [...this.temp];
        }
        if (this.temp != null) {
          let filter = this.temp.filter((item) => {
            return item.name != null && item.name.toUpperCase().includes(str.toUpperCase());
          });
          let result = filter.sort((firstItem, secondItem) => {
            return firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1;
          });
          if (result != null) {
            this.assignedUserList = result;
          } else {
            this.assignedUserList = [];
          }
        }
      }, 500);
    },
    save() {
      var list = {
        'locationId': this.location.id,
        'content': this.assignedUserList
      }
      axios.post('/api/common/rol-stp/' + this.roleName.id + '/users/save', list).then(() => {
      }).catch((error) => {
        Common.alert(error.content.data, 'Error');
      })
      this.location = [];
      this.availableUserList = [];
      this.assignedUserList = [];
      this.disable = true;
      this.plant_title="Search Plant"
      $("#userRole").modal("hide");
    },
    availableUser(index) {
      this.assignedUserList.push({id: this.availableUserList[index].id, name: this.availableUserList[index].name});
      this.availableUserList[index].enable = false;
      this.temp = [...this.assignedUserList]
    },
    setAvailableuser() {
      this.assignedUserList.forEach(item => {
        this.availableUserList.push({id: item.id, name: item.name, enable: false});
      })
    },
    assignedUser(index) {
      var id = this.assignedUserList[index].id;
      let temp = this.availableUserList.find(item => {
        return item.id == id
      });

      if (temp != undefined) {
        temp.enable = true;
      } else {
        this.availableUserList.push({
          id: this.assignedUserList[index].id,
          name: this.assignedUserList[index].name,
          enable: true
        });
      }
      this.assignedUserList.splice(index, 1);

      this.temp = [...this.assignedUserList]
      this.tempList = [...this.availableUserList]
    },
  },
};
</script>

<style scoped>
.col-md-6.custom-role.role-name{
  padding-left:20px;
}
.disableClass {
  background-color: #C4C4C4 !important;
  border: none !important;
  color: #FFFFFF;
  cursor: not-allowed;
}

.btn-search-location {
  line-height: normal;
  text-align: center;
  width: 110px;
}

.btn-search-location:hover {
  color: #fff;
  background-color: #4dbd74;
  border-color: #4dbd74;
}

.setMargin {
  margin-top: 5.5px;
}


.button-edit {
  width: 54px;
  height: 29px;
  color: #FFFFFF;
  border: 1px solid #47B576;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #47B576 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

.button-edit:hover {
  width: 54px;
  height: 29px;
  color: #FFFFFF;
  border: 1px solid #3EA662;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #3EA662 0% 0% no-repeat padding-box;
  border-radius: 3px;
  opacity: 1;
}

.btn-primary:focus,
.btn-primary.focus {
  box-shadow: none;
  background: #3EA662 0% 0% no-repeat padding-box !important;
}

.btn-primary.button-edit:focus {
  outline: none !important;
  box-shadow: none !important;
  width: 54px;
  color: #FFFFFF;
  height: 29px;
  border: 2px solid #C3F2D7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 3px;
  background: #3EA662 0% 0% no-repeat padding-box !important;
  border-radius: 3px;
  opacity: 1;
}


.save {
  /* margin-top: 190px; */
  height: 159px;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  align-content: flex-end;
}

.location-label {
  font: 16px / 18px Helvetica;
}

.form-location {
  padding: 15px 15px 0px 15px;
}

.modal-dialog {
  position: relative;
  display: flex;
  pointer-events: none;
  align-content: center;
  justify-content: center;
  align-items: center;
  margin: auto !important;
}

.scroll-item {
  margin-top: 30px;
  width: 95%;
  overflow: auto;
  height: 400px;
  max-height: 420px;
}

.assignedUserScroll {
  margin-top: 30px;
  width: 95%;
  overflow: auto;
  height: 260px;
  max-height: 260px;
}

.availableListItem {
  padding: 5px 8px;
  height: 30px;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  border: 1px solid #9D9D9D;
  border-radius: 2px;
  text-align: left;
  font: normal normal normal 14px/16px Helvetica Neue;
  letter-spacing: 0px;
  color: #262931;
  opacity: 1;
}

.assignedListItem {
  background: #DAEEFF;
  padding: 5px 8px;
  height: 30px;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  border: 1px solid #9D9D9D;
  border-radius: 2px;
  text-align: left;
  font: normal normal normal 14px/16px Helvetica Neue;
  letter-spacing: 0px;
  color: #262931;
  opacity: 1;
}

.selected-item {
  padding: 2px 10px;
  display: flex;
  align-items: center;
  box-shadow: none;
  text-align: left;
  font-size: 16px;
  line-height: 100%;
  color: #262931;
}

.btn.btn-default.btn-search-location:focus {
  box-shadow: none !important;
  outline: none;
}

.selected-item span {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 300px;
}

.minus-item:hover .minus-item-hover {
  display: block;
}

.minus-item:hover .minus-item-default {
  display: none;
}

.selected-item .drag-icon {
  margin-right: 11px;
  cursor: pointer;
}

.minus-item:hover {
  border: 1px solid #3491FF;
}

.minus-item-hover {
  display: none;
}

.minus-item-default {
  display: block;
}

.minus-item {
  border: 1px solid #9D9D9D;
  border-radius: 2px;
  opacity: 1;
  width: 30px;
  height: 30px;
  margin-left: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.assigned-users {
  text-align: left;
  font: normal normal normal 14px/17px Helvetica;
  letter-spacing: 0px;
  color: #020202;
  opacity: 1;
}

.available-users {
  font: normal normal normal 14px/16px Helvetica;
  letter-spacing: 0px;
  text-align: left;
  color: #020202;
  opacity: 1;
}

.role-name {
  text-align: left;
  font: normal normal bold 16px/19px Helvetica Neue;
  letter-spacing: 0px;
  color: #595959;
  opacity: 1;
}

.dropdown-searchbox input:hover {
  border-color: #3491FF;
}

.dropdown-searchbox input:focus {
  color: #5c6873;
  border-color: #8ad4ee;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgb(32 168 216 / 25%);
}

.searchBox .form-control {
  border: none !important;
  height: 100% !important;
}

.searchBox .input-group-prepend .input-group-text {
  height: 100% !important;
  border: none !important;
}

.searchBox {
  box-shadow: none !important;
  height: 34px;
  border: 1px solid #D5D7D8;
  border-radius: 0.25rem;
}

.input-group-text {
  height: 32px;
}

.form-control {
  height: 32px;
}

.form-control:focus {
  box-shadow: none !important;
  border: none !important;
}

.searchBox:hover {
  border: 0.5px solid #3491FF;
}

.searchBox:focus {
  border: 1px solid #3491FF;
}

.more-details > li {
  font: normal normal normal 10px/12px Helvetica Neue;
  letter-spacing: 0px;
  color: #595959;
  list-style: none;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.more-details > li:before {
  content: "";
  width: 4px;
  height: 4px;
  border-radius: 50%;
  margin-right: 4.5px;
  background-color: #595959;
  display: block;
}

.close-button {
  font-size: 25px;
  display: flex;
  align-items: center;
  height: 17px;
  cursor: pointer;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: unset;
  top: 0;
  left: 0;
  width: 100%;
}

@media (min-width: 1100px) {
  .modal-lg {
    max-width: 1032px;
  }
}

.modal-content {
  width: 1032px;
  height: 695px;
  pointer-events: auto;
  background-color: rgb(255, 255, 255);
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  outline: 0px;
}

.modal-title {
  background: #f5f8ff;
  position: relative;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 20px;
  border-radius: 6px 6px 0 0;
}

.modal-body {
  background: white;
  padding: 20px;
  border-radius: 0px 0px 6px 6px;
}

.modal-title .span-title {
  font-size: 16px;
  color: #4B4B4B !important;
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
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
  margin-right: 64px;
}

.switch-zone div {
  cursor: pointer;
  width: 130px;
  height: 26px;
  background: #fff;
  border-radius: 3px;
  border: 1px solid #d0d0d0;
  font-size: 16px;
  color: #888888;
  margin-right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes primary-active-switch {
  0% {
  }
  33% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  66% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  100% {
  }
}

.info-zone .item .head b {
  color: #4b4b4b;
}

.graph-dropdown-wrap label {
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li {
  list-style: none;
  color: #4b4b4b;
  font-size: 11px;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li input {
  margin-right: 8px;
}
</style>