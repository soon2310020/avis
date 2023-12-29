<template>
  <div class="op-user-list">
    <form class="form-group" @submit.prevent="formData">
      <div class="row">
        <div class="col-md-12">
          <div class="card">
            <div class="card-header">
              <strong>Update Version History</strong>
            </div>
            <div class="card-body card-body-custom">
              <div class="form-inline">
                <label
                  >Version No.
                  <span class="avatar-status badge-danger"></span>
                  <span class="badge-require" id="version"></span>
                </label>
                <input
                  autocomplete="off"
                  v-model="version"
                  @keyup="validate"
                  name="version"
                  type="text"
                  class="form-control  input"
                  id="versionField"
                  :class="version !== '' && !validateVersion ? 'validate' : 'custom-form'"
                  required
                />
              </div>
              <div v-show="version !== '' && !validateVersion" class="validate">
                It should be decimal digits without space.
              </div>
              <br />
              <div class="form-inline">
                <label class=""
                  >Date
                  <span class="avatar-status badge-danger"></span>
                  <span class="badge-require" id="date-badge"></span>
                </label>
                <a-date-picker
                  :allow-clear="false"
                  v-model="date"
                  class="form-control custom-form input"
                  id="date"
                  :format="dateFormat"
                  :placeholder="'YYYY-MM-DD'"
                >
                </a-date-picker>
              </div>
              <br /><br />
              <div
                v-for="(input, index) in detailList"
                :key="`detailInput-${index}`"
              >
                <br />
                <div v-if="index < 1" class="form-inline">
                  <label
                    >Details
                    <span class="avatar-status badge-danger"></span>
                    <span class="badge-require" id="details"></span>
                  </label>
                  <input
                    autocomplete="off"
                    v-model="input.description"
                    type="text"
                    class="form-control custom-form input"
                    id="detailsField"
                    required
                  />
                </div>

                <div v-if="index >= 1" class="form-inline">
                  <label style="opacity: 0"> </label>
                  <input
                    v-model="input.description"
                    type="text"
                    class="form-control custom-form input"
                    required
                  />
                  <span
                    style="cursor: pointer"
                    @click="removeField(index, detailList)"
                    id="remove-details"
                  >
                    <img src="/images/icon/cross.svg" alt="closeIcon" />
                  </span>
                </div>
              </div>
              <div class="form-inline">
                <div class="more-details"></div>
                <span
                  style="cursor: pointer"
                  class="add-more"
                  @click="addField(detailList)"
                  >+ Add more</span
                >
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-12">
          <div class="card">
            <div class="card-body text-center">
              <button type="button" class="btn btn-default mb-2" @click.prevent="cancel">Cancel</button>
              <button type="submit" id="save" class="btn mb-2" :disabled="!validateVersion" :class="!validateVersion ? 'disable-Btn' : 'custom-btn'">
                Save
              </button>
            </div>
          </div>
        </div>
      </div>
    </form>

    <div class="col-lg-12 card-main">
      <div class="card">
        <div style="overflow-x: unset !important" class="card-body">
          <div>
            <div class="card-tool">
              <ul class="nav nav-tabs">
                <li class="nav-item tabs-items">
                  <a
                    href="#"
                    class="nav-link"
                    :class="{ active: requestParam.status == 'all' }"
                    @click.prevent="tab('all')"
                  >
                    <span style="cursor: pointer"
                      >All
                      <span
                        :class="requestParam.status == 'all' ? '' : 'd-none'"
                        class="badgeCustom badge-light badge-pill"
                      >
                        {{ All }}</span
                      >
                    </span>
                  </a>
                </li>
                <li class="nav-item tabs-items">
                  <a
                    href="#"
                    class="nav-link"
                    :class="{ active: requestParam.status == 'disabled' }"
                    @click.prevent="tab('disabled')"
                  >
                    <span style="cursor: pointer"
                      >Disabled
                      <span
                        :class="
                          requestParam.status == 'disabled' ? '' : 'd-none'
                        "
                        class="badgeCustom badge-light badge-pill"
                        >{{ Disable }}</span
                      >
                    </span>
                  </a>
                </li>
              </ul>
            </div>
            <table class="table table-responsive-sm table-striped">
              <colgroup>
                <col />
                <col />
                <col />
                <col />
              </colgroup>
              <thead id="thead-actionbar" class="custom-header-table">
                <tr class="tr-sort">
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <input
                        id="checkbox-all"
                        class="checkbox checkboxhead"
                        type="checkbox"
                        v-model="isAll"
                        @change="select"
                      />
                    </div>
                  </th>
                  <th
                    v-if="
                      listChecked.length < 1 && requestParam.status != 'DELETED'
                    "
                    class="__sort desc text-left"
                     :class="[ {'active': currentSortType === 'version'}, isDesc ? 'desc': 'asc']"
                    @click.prevent="sort('version')"
                  >
                    <span>Version No.</span>
                   <span class="__icon-sort"></span>
                  </th>
                  <th
                    v-if="
                      listChecked.length < 1 && requestParam.status != 'DELETED'
                    "
                    class="__sort desc text-left"
                     :class="[ {'active': currentSortType === 'releaseDate'}, isDesc ? 'desc': 'asc']"
                    @click.prevent="sort('releaseDate')"
                  >
                    <span>Date</span>
                    <span class="__icon-sort"></span>
                  </th>
                  <th
                    v-if="
                      listChecked.length < 1 && requestParam.status != 'DELETED'
                    "
                    class="__sort desc text-left"
                    :class="[ {'active': currentSortType === 'updatedBy'}, isDesc ? 'desc': 'asc']"
                    @click.prevent="sort('updatedBy')"
                  >
                    <span>Uploaded By</span>
                    <span class="__icon-sort"></span>
                  </th>
                  <th
                    v-if="
                      listChecked.length < 1 && requestParam.status != 'DELETED'
                    "
                    class="__sort desc text-left detail-custom"
                     :class="[ {'active': currentSortType === 'id'}, isDesc ? 'desc': 'asc']"
                    @click.prevent="sort('id')"
                  >
                    <span>Details</span>
                     <span class="__icon-sort"></span>
                  </th>
                  <th
                    v-if="
                      listChecked.length == 1 &&
                      requestParam.status != 'DELETED'
                    "
                    class="text-left "
                    @click.prevent="goToEdit()"
                  >
                    <span class="cursor custom-hover"
                      >Edit
                      <span class="icon-action edit-icon inline"></span>
                    </span>
                  </th>
                  <th
                    v-if="
                      listChecked.length >= 1 && requestParam.status == 'all'
                    "
                    
                    class="text-left  "
                    @click.prevent="disabled()"
                  >
                    <span class="cursor custom-hover "
                      >Disable
                      <span class="icon-action disable-icon inline"></span>
                    </span>
                  </th>
                  <th
                    v-if="
                      listChecked.length >= 1 &&
                      requestParam.status == 'disabled'
                    "
                    class="text-left "
                    @click.prevent="enabled()"
                  >
                    <span class="cursor custom-hover"
                      >Enable
                      <span class="icon-action enable-icon inline"></span>
                    </span>
                  </th>
                  <th
                    v-if="
                      listChecked.length >= 1 &&
                      requestParam.status == 'disabled'
                    "
                    class="text-left"
                  ></th>
                  <th
                    v-if="
                      listChecked.length >= 1 &&
                      requestParam.status == 'disabled'
                    "
                    :class="listChecked.length === 1 && 'detail-custom'"
                    class="text-left"
                  ></th>
                  <th
                    v-if="
                      listChecked.length >= 1 && requestParam.status == 'all'
                    "
                    class="text-left"
                  ></th>
                  <th
                    v-if="
                      listChecked.length >= 1 && requestParam.status == 'all'
                    "
                    :class="listChecked.length === 1 && 'detail-custom'"
                    class="text-left "
                  ></th>
                  <th
                    v-if="
                      listChecked.length >= 2 && requestParam.status == 'all'
                    "
                    class="text-left detail-custom"
                  ></th>
                  <th
                    v-if="
                      listChecked.length >= 2 &&
                      requestParam.status == 'disabled'
                    "
                    class="text-left detail-custom "
                  ></th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, index) in list"
                  :id="item.id"
                  v-show="requestParam.status == 'all'"
                  :key="index"
                >
                  <td class="custom-td" v-if="listChecked.length >= 0">
                    <input
                      @click="(e) => check(e, item)"
                      :checked="checkSelect(item.id)"
                      class="checkbox checkboxcustom"
                      type="checkbox"
                      :id="item.id + 'checkbox'"
                      :value="item.id"
                    />
                  </td>
                  <td class="text-left" :key="item.version">
                    <span class="font-size-14">
                      {{ item.version }}
                    </span>
                  </td>
                  <td class="text-left">
                    <span class="font-size-14">{{ item.releaseDate }} </span>
                  </td>
                  <td class="text-left">
                    <div>
                    <a-tooltip placement="bottom">
                      <template slot="title">
                        <div style="padding: 6px;font-size: 13px;">
                          <div><b>{{item.createdByUserName}}</b></div>
                          <div>
                            <div>
                              Comapny: {{ item.companyName }}
                            </div>
                            <div>
                              Department: {{ item.createdByUserDepartment }}
                            </div>
                            <div style="margin-bottom: 15px">
                              Position: {{ item.createdByUserPosition }}
                            </div>
                            <div>
                              Email: {{ item.createdByUserEmail }}
                            </div>
                            <div>
                              Phone: {{ item.createdByUserMobileNumber }}
                            </div>
                          </div>
                        </div>
                      </template>
                      <span
                          :style="{ 'background-color': getRequestedColor(item.createdByUserName,index)}"
                          class="createdByUser"
                      >
                        {{ convertName(item.createdByUserName) }}
                    </span>
                    </a-tooltip>
                    </div>
                  </td>
                  <td class="text-left font-size-14">
                    <div
                      id="tooltip1"
                      v-bind:class="{ cursor: item.items.length >= 2 }"
                    >
                      {{ truncate_details(item.items[0].description) }}
                      <span v-show="item.items.length >= 2" id="toolTipText1">
                        <ul v-for="(value, index) in item.items" :key="index">
                          <li :key="index">{{ truncate_details(value.description) }}</li>
                        </ul>
                      </span>
                      <span v-show="item.items.length >= 2">
                        <img src="/images/more.png" class="show-more" />
                      </span>
                      <span
                        v-show="item.items.length >= 2"
                        :style="badgeStyle"
                        class="badge bg-primary"
                        id="myBadge1"
                        >+{{ item.items.length - 1 }}</span
                      >
                    </div>
                  </td>
                </tr>
                <tr
                  v-for="(item, index) in disableList"
                  :id="item.id"
                  v-show="requestParam.status == 'disabled'"
                  :key="index"
                >
                  <td class="custom-td" v-if="listChecked.length >= 0">
                    <input
                      @click="(e) => check(e, item)"
                      :checked="checkSelect(item.id)"
                      class="checkbox"
                      type="checkbox"
                      :id="item.id + 'checkbox'"
                      :value="item.id"
                    />
                  </td>
                  <td class="text-left" :key="item.version">
                    <span class="font-size-14">
                      {{ item.version }}
                    </span>
                  </td>
                  <td class="text-left">
                    <span class="font-size-14">
                      {{ item.releaseDate }}
                    </span>
                  </td>
                  <td class="text-left">
                    <div>
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 6px;font-size: 13px;">
                            <div><b>{{item.createdByUserName}}</b></div>
                            <div>
                              <div>
                                Comapny: {{ item.companyName }}
                              </div>
                              <div>
                                Department: {{ item.createdByUserDepartment }}
                              </div>
                              <div style="margin-bottom: 15px">
                                Position: {{ item.createdByUserPosition }}
                              </div>
                              <div>
                                Email: {{ item.createdByUserEmail }}
                              </div>
                              <div>
                                Phone: {{ item.createdByUserMobileNumber }}
                              </div>
                            </div>
                          </div>
                        </template>
                        <span
                            :style="{ 'background-color': getRequestedColor(item.createdByUserName,index)}"
                            class="createdByUser"
                        >
                        {{ convertName(item.createdByUserName) }}
                    </span>
                      </a-tooltip>
                    </div>
                  </td>
                  <td class="text-left font-size-14">
                    <div id="tooltip">
                      {{ truncate_details(item.items[0].description) }}

                      <span id="toolTipText" v-show="item.items.length >= 2">
                        <ul v-for="(value, index) in item.items" :key="index">
                          <li :key="index">{{ truncate_details(value.description) }}</li>
                        </ul>
                      </span>
                      <span v-show="item.items.length >= 2">
                        <img src="/images/more.png" class="show-more" />
                      </span>
                      <span
                        v-show="item.items.length >= 2"
                        :style="badgeStyle"
                        class="badge bg-primary"
                        id="myBadge"
                        >+{{ item.items.length - 1 }}</span
                      >
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="row" v-show="this.requestParam.status == 'all'">
            <div class="col-md-8">
              <ul class="pagination">
                <li
                  v-for="(data, index) in pagination"
                  class="page-item"
                  :class="{ active: data.isActive }"
                  :key="index"
                >
                  <a
                    class="page-link"
                    @click.prevent="paging(data.pageNumber)"
                    >{{ data.text }}</a
                  >
                </li>
              </ul>
            </div>
          </div>

          <div class="row" v-show="this.requestParam.status == 'disabled'">
            <div class="col-md-8">
              <ul class="pagination">
                <li
                  v-for="(data, index) in pagination1"
                  class="page-item"
                  :class="{ active: data.isActive }"
                  :key="index"
                >
                  <a
                    class="page-link"
                    @click.prevent="paging1(data.pageNumber)"
                    >{{ data.text }}</a
                  >
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  data() {
    return {
      badgeStyle: {
        width: "22px",
        height: "22px",
        fontSize: "13px",
        borderRadius: "100%",
        textAlign: "center",
        letterSpacing: "0px",
        color: "#FFFFFF",
        opacity: "1",
      },
      currentSortType : '',
      isDesc: true,
      isAll: false,
      version: "",
      selectedDate: "",
      date: null,
      details: "",
      list: [],
      pagination: [],
      pagination1: [],
      results: [],
      total: 0,
      disableList: [],
      colors: [
        "#AFACFF",
        "#FF7171",
        "#DB91FC",
        "#FFACAC",
        "#FFB371",
        "#FFACF2",
        "#FFACE5",
        "#86C7FF",
        "#62B0D9",
        "#ACBFFF",
        "#FF8699",
        "#90E5AA",
        "#CCACFF",
        "#74AEFC",
        "#E0CD7E",
        "#FF8489",
        "#FFA167",
        "#64D0C3",
        "#86B0FF",
        "#D2F8E2",
        "#C69E9E",
        "#FFC68D",
        "#F8D2D2",
        "#E58383",
      ],
      All:0,
      Disable:0,
      detailList: [
        {
          position: 0,
          description: "",
        },
      ],
      detailList1: [
        {
          position: 0,
          description: "",
        },
      ],
      listChecked: [],
      isAllTracked: [],
      listCheckedFull: [],
      listCheckedTracked: {},
      requestParam: {
        status: "all",
        page: 1,
        day: "",
      },
      param: {
        enabled: true,
        sort: "",
        page: 1,
        size: 5,
      },
      param1: {
        enabled: false,
        sort: "",
        page: 1,
        size: 5,
      },
      rawDate: "",
      dateFormat: "YYYY-MM-DD",
      position: 0,
      paramID: {
        id: [],
      },
      edit: false,
      ID: null,
      validateVersion: false,
      model: {
        appCode: "MMS",
        version: "4.3.0",
        releaseDate: "2209-09-09",
        items: [
          {
            position: 0,
            description: "bug fix",
          },
        ],
      },
    };
  },
  methods: {
    moment,
    validate() {
      const reg = /^[0-9_.]+$/;
      if (this.version == '' || reg.test(this.version)) {
        this.validateVersion = true;
      } else {
        this.validateVersion = false;
      }
    },
    cancel: function() {
      location.reload();
    },
    truncate_details(source) {
      return source.length > 50 ? source.slice(0, 50 - 1) + "…" : source;
    },
    convertName(name) {
      return Common.getAvatarName(name);
    },
    getRequestedColor(name, id) {
      return Common.getRequestedColor(name, id);
    },
    paging1(pageNumber) {
      this.isAll = false;
      this.param1.page = pageNumber;
      let param1 = Common.param(this.param1);
      axios.get("/api/common/app-ver-stp?" + param1).then((response) => {
        this.disableList = response.data.content;
        this.pagination1 = Common.getPagingData(response.data);
        this.$emit(this.disableList);
      });
    },
    paging(pageNumber) {
      this.isAll = false;
      this.param.page = pageNumber;
      let param = Common.param(this.param);
      axios.get("/api/common/app-ver-stp?" + param).then((response) => {
        this.list = response.data.content;
        this.pagination = Common.getPagingData(response.data);
        this.$emit(this.list);
      });
    },
    goToEdit() {
      this.edit = true;
      let temp = [];
      if (this.requestParam.status == "all")
        temp = this.list.filter((item) => item.id === this.listChecked[0]);
      else
        temp = this.disableList.filter(
          (item) => item.id === this.listChecked[0]
        );
      this.ID = temp[0].id;
      this.version = temp[0].version;
      this.date = moment(temp[0].releaseDate);
      this.detailList = temp[0].items;
      this.listChecked = [];
    },
    sort(value) {
      this.currentSortType = value;
      if (this.isDesc) {
        value = value + ",asc";
      } else {
        value = value + ",desc";
      }
      this.param.sort = value;
      this.param1.sort = value;

      if (this.requestParam.status == "all") {
        axios
          .get("/api/common/app-ver-stp?" + Common.param(this.param))
          .then((response) => {
            this.list = response.data.content;
          })
          .catch(function (error) {
            Common.alert("Error!!!");
          });
      }

      if (this.requestParam.status == "disabled") {
        axios
          .get("/api/common/app-ver-stp?" + Common.param(this.param1))
          .then((response) => {
            this.disableList = response.data.content;
          })
          .catch(function (error) {
            Common.alert("Error!!!");
          });
      }

      this.isDesc = this.isDesc ? false : true;
    },
    async disabled() {
      this.paramID.id = this.listChecked;
      this.All=this.All-this.listChecked.length;
      this.Disable=this.Disable+this.listChecked.length;
      await axios
        .put("/api/common/app-ver-stp/disable?" + Common.param(this.paramID))
        .then(function (response) {})
        .catch(function (error) {
          Common.alert("Error!!!");
        });
      this.listChecked = [];
      this.paging(1);
      this.isAll = false;
      this.callGetApi();
      Common.initFooterNumber();
    },
    enabled: async function () {
      this.paramID.id = this.listChecked;
      this.All=this.All+this.listChecked.length;
      this.Disable=this.Disable-this.listChecked.length;
      await axios
        .put("/api/common/app-ver-stp/enable?" + Common.param(this.paramID))
        .then(function (response) {})
        .catch(function (error) {
          Common.alert("Error!!!");
        });
      this.listChecked = [];
      this.paging1(1);
      this.isAll = false;
      this.callGetApi();
      Common.initFooterNumber();
    },
    getFeatureParams() {
      return {
        feature: "VERSION_HISTORY"
      }
    },
    async formData() {

      if (!this.validateVersion) {
        Common.alert("Error! Please enter correct input");
      } else {
        this.model.appCode = "MMS";
        this.model.releaseDate = moment(this.date).format("YYYY-MM-DD");
        
        // removing the dots from end of version
        this.model.version = this.version.replace(/\.+$/, "").concat('.')
        this.model.items = this.detailList;

        if (this.edit) {
          this.model.version = this.version.replace(/\.+$/, "").concat('.')
          await axios
            .put("/api/common/app-ver-stp/" + this.ID, this.model)
            .then(function (response) {
              Common.alert("Data Edited Successfully!");
            })
            .catch(function (error) {
              Common.alert("Error! Please enter all compulsory fields");
            });
        } else {
          // update current version number
          console.log('document.getElementById(footerVersionNumber): ', document.getElementById('footerVersionNumber'))
          // document.getElementById('footerVersionNumber').innerHTML = 'Ver. ' + this.model.version.replace(/\.$/, "")
          await axios
            .post("/api/common/app-ver-stp/one", this.model)
            .then(function (response) {
              Common.alert("Data Saved Successfully!");
            })
            .catch(function (error) {
              Common.alert(
                "Error! Please enter all compulsory fields (ex: V.No Can't be duplicated)"
              );
            });
          await axios.get("/api/on-boarding/set-all-version-false?"+Common.param(this.getFeatureParams()))
              .then(response=>{
              })
        }
        await this.callGetApi();

        this.ID = null;
        this.edit = false;
        this.version = "";
        this.date = null;
        this.position = 0;
        this.detailList1 = [...this.detailList];
        this.detailList = [
          {
            position: 0,
            description: "",
          },
        ];
      }
    },
    addField(fieldType) {
      this.position += 1;
      fieldType.push({ description: "", position: this.position });
    },
    removeField(index, fieldType) {
      fieldType.splice(index, 1);
    },
    tab: function (status) {
      this.listChecked = [];
      this.requestParam.status = status;
      if (status == "all") {
        this.paging(1);
      } else {
        this.paging1(1);
      }
      this.isAll = false;
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll && this.requestParam.status == "all") {
        this.listChecked = this.list.map((value) => value.id);
      } else if (isAll && this.requestParam.status == "disabled") {
        this.listChecked = this.disableList.map((value) => value.id);
      } else {
        this.listChecked = [];
      }
    },

    check: function (e, result) {
      if (e.target.checked) {
        this.listChecked.push(+e.target.value);
      } else {
        this.listChecked = this.listChecked.filter(
          (value) => value != e.target.value
        );
        if (this.listChecked.length === 0) {
          this.isAll = false;
        }
      }
    },
    async callGetApi() {
      await axios
        .get("/api/common/app-ver-stp?" + Common.param(this.param))
        .then((response) => {
          this.list = response.data.content;
          this.pagination = Common.getPagingData(response.data);
        })
        .catch(function (error) {
          Common.alert("Error!!!");
        });
      await axios
        .get("/api/common/app-ver-stp?" + Common.param(this.param1))
        .then((response) => {
          this.disableList = response.data.content;
          this.pagination1 = Common.getPagingData(response.data);
        })
        .catch(function (error) {
          Common.alert("Error!");
        });
    },
  },
  mounted() {
    this.callGetApi();
    let param={
      enabled:true
    };
     axios
        .get("/api/common/app-ver-stp?" + Common.param(param))
        .then((response) => {
          this.All = response.data.content.length;
        })
        .catch(function (error) {
          Common.alert("Error!!!");
        });
     param.enabled=false;
    axios
        .get("/api/common/app-ver-stp?" + Common.param(param))
        .then((response) => {
          this.Disable= response.data.content.length;
        })
        .catch(function (error) {
          Common.alert("Error!!!");
        });
  },
};
</script>

<style>
.font-size-14 {
  margin-left: 10px;
}
.__sort span {
  padding-left: 10px;
}
.validate {
  color: red;
  margin-left: 149px;
}
.table th,
.table td {
  border-top: 0;
  padding: 0.5em 0;
  border-bottom: 1px solid #c8ced3;
}
.tr-sort {
  height: 57px;
}

.text-left .custom-hover{
  font-weight: normal;
  padding-left: 0px;
}

.text-left .custom-hover:hover{
  font-weight: bold;
}

.nav-tabs .nav-link{
    line-height: normal;
    text-align: center;
    height: 39px !important;
    padding:0px !important;
    text-align: center;
}

.nav-tabs .nav-link.active {
    font-weight: bold;
    width: 130px;
}


.nav-tabs .nav-link:hover {
    color: #2f353a;
    background: #fff;
    border-color: #c8ced3;
    border-bottom-color: #fff;
}


.tabs-items {
  width: 130px;
}

.tabs-items .active {
  width: 130px;
}
.sort {
  margin: 10px;
  width: 10px;
  height: 10px;
}
.inline {
  display: inline-block;
}
.show-more {
  width: 15px;
  height: 15px;
}
.nav.nav-tabs {
  margin-bottom: 0px;
}
.card-main {
  padding: 0;
  width: 99%;
}
#date-badge {
  margin: 30px;
  left: 25px;
}
.more-details {
  width: 163px;
}
.add-more {
  color: #298bff !important;
  font-size: 11px;
  margin-top: 8px;
  text-align: left;
  width: 100% !important;
  height: 31px !important;
  position: sticky !important;
  left: 170px !important;
}

@media (min-width: 768px) {
  .ml-md-5,
  .mx-md-5 {
    margin-left: 0 !important;
  }
}
.cursor {
  cursor: pointer;
  margin-left: 10px;
}

.card-body-custom {
  padding-right: 40px !important;
}
#remove-details {
  height: 20px;
  position: absolute;
  right: 15px;
}
#details {
  margin: 30px;
  left: 38px;
}
.input {
  width: 100% !important;
  height: 31px !important;
  position: sticky !important;
  left: 170px !important;
}
#version {
  margin: 30px;
  left: 65px;
}
/* .card {
  font-size: 11.5px;
} */


.form-control.custom-form {
  border-radius: 3px;
  border: 1px solid #E3E7EA;
  background: #FFFFFF 0% 0% no-repeat padding-box;
  outline: none ;
  box-shadow: none ;
}

.form-control.validate {
  border-radius: 3px;
  margin-left: 0px;
  border: 0.5px solid #EF4444;
  background: #FFFFFF 0% 0% no-repeat padding-box;
  outline: none ;
  box-shadow: none ;
}

.form-control.custom-form:hover {
  border-radius: 3px;
  border: 1px solid #4B4B4B;
  background: #FFFFFF 0% 0% no-repeat padding-box;
  outline: none ;
  box-shadow: none ;
}

.table thead th {
  width :241px;
}

.form-control.custom-form:focus {
  border-radius: 3px;
  border: 1.5px solid #DEEDFF;
  background: #FFFFFF 0% 0% no-repeat padding-box;
  outline: none ;
  box-shadow: none ;
}

.form-control.custom-form:active {
  border-radius: 3px;
  border: 1.5px solid #DEEDFF;
  background: #FFFFFF 0% 0% no-repeat padding-box;
  outline: none ;
  box-shadow: none ;
}


.createdByUser {
  font: normal normal normal 14px/16px Helvetica Neue;
  display: flex;
  letter-spacing: 0px;
  line-height: 0px;
  font-size: 12px;
  height: 28px;
  width: 28px;
  text-align: center;
  opacity: 1;
  border-radius: 100%;
  padding: 0;
  margin: 0 35px;
  align-items: center;
  flex-direction: row-reverse;
  justify-content: center;
}
.pagination {
  display: flex;
  list-style-type: none;
  padding: 0;
}
input.ant-calendar-picker-input.ant-input {
  padding-left: 34px;
}
.ant-input {
  height: 29px;
  border: #d9d9d9;
}

.ant-input::placeholder {
  color: #616161;
  font-size: 11.5px;
  font: normal normal normal 12px/13px Helvetica Neue;
}

.ant-calendar-picker-clear,
.ant-calendar-picker-icon {
  left: 13px !important;
  color: #3491ff;
}
.op-user-list .form-group {
  margin-right: 10px;
}

@media (min-width: 576px) {
  .form-inline label {
    display: -ms-flexbox;
    display: flex;
    -ms-flex-align: center;
    align-items: center;
    -ms-flex-pack: center;
    justify-content: flex-start;
    width: 160px;
    margin-bottom: 0;
    align-content: flex-start;
  }
}

.form-inline {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-flow: nowrap;
  flex-flow: nowrap;
  -ms-flex-align: center;
  align-items: center;
}

.op-user-list .form-group label {
  margin-right: 5px;
}
.card-body .nav-link {
  display: flex;
  min-height: 32px;
  align-items: center;
  text-align: center;
  flex-direction: column;
}
.card-body .nav-link > span {
  display: inline-block;
  margin: auto;
}

.nav-link {
  padding: 10px;
}
.table {
  display: table;
  overflow-x: auto;
  box-sizing: border-box;
  border-spacing: 2px;
  border-color: grey;
  border-collapse: collapse;
}

.table .thead .th {
  vertical-align: bottom;
  border-bottom: 2px solid #c8ced3;
  display: table-cell;
  font-weight: bold;
}

.table .th,
.table .td {
  border-top: 0;
  padding: 0.5em;
  border-bottom: 1px solid #c8ced3;
}

.table-striped .tbody .tr:nth-of-type(odd) {
  background-color: rgba(0, 0, 0, 0.05);
}

.badgeCustom {
  font-size: 75%;
  font-weight: 700;
  display: inline-block;
  padding: 0.25em 0.4em;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  vertical-align: baseline;
}

#tooltip {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: flex-start;
}

.text-left.detail-custom{
  width: 459px;
}

.btn.btn-default {
  border-radius: 3px;
  text-align: center;
  border: 1px solid #d6dade;
  font: normal normal normal 17px/20px Helvetica;
  width: 69px;
  background: #ffffff 0% 0% no-repeat padding-box;
  height: 32px;
  padding: 0px;
  line-height: normal;
  color: #595959;
  font-size: 17px;
}

.btn.btn-default:hover {
  border-radius: 3px;
  border: 1px solid #595959;
  background: #f4f4f4 0% 0% no-repeat padding-box;
  width: 69px;
  height: 32px;
  padding: 0px;
  line-height: normal;
  color: #000000;
  font-size: 17px;
}

.btn.btn-default:focus {
  border-radius: 3px;
  border: 2px solid #d6dade;
  background: #f4f4f4 0% 0% no-repeat padding-box;
  width: 69px;
  height: 32px;
  padding: 0px;
  line-height: normal;
  color: #000000;
  font-size: 17px;
  box-shadow: none;
}

.btn.custom-btn {
  height: 32px;
   width: 54px;
  padding: 0px;
  font-size: 17px;
  font: normal normal normal 17px/20px Helvetica;
  line-height: normal;
  background: #47b576 0% 0% no-repeat padding-box;
  border-radius: 3px;
  border: 1px solid #47b576;
  color: #ffffff;
  box-shadow: none;
}

.btn.custom-btn:hover {
  width: 54px;
  text-align: center;
  height: 32px;
  padding: 0px;
  line-height: normal;
  background: #3ea662 0% 0% no-repeat padding-box;
  border-radius: 3px;
  border: 1px solid #3ea662;
  color: #ffffff;
  box-shadow: none;
}

.btn.custom-btn:focus {
  width: 54px;
  text-align: center;
  height: 32px;
  padding: 0px;
  line-height: normal;
  background: #3ea662 0% 0% no-repeat padding-box;
  border-radius: 3px;
  outline-color: #C3F2D7 ;
  outline-width: 2px;
  outline-style: solid;
  color: #ffffff;
  box-shadow: none;
}

.btn.disable-Btn {
  width: 54px;
  text-align: center;
  cursor :not-allowed;
  height: 32px;
  padding: 0px;
  line-height: normal;
  background: #C4C4C4 0% 0% no-repeat padding-box;
  border-radius: 3px;
  border: 1px solid #C4C4C4;
  color: #ffffff;
  box-shadow: none;
}

#tooltip #toolTipText {
  visibility: hidden;
  border-width: 4px;
  border-style: solid;
  border-color: #ffffff;
  width: 100%;
  background-color: #fff;
  text-align: left;
  padding: 6px 0;
  font: normal normal normal 14px Helvetica;
  letter-spacing: 0px;
  line-height: 32px;
  color: #4b4b4b;
  opacity: 1;
  position: absolute;
  top: 25px;
  z-index: 1;
}

#tooltip:hover #toolTipText {
  visibility: visible;
}
#myBadge {
  border-radius: 10px 10px 10px 10px;
  background: #43a1fc 0% 0% no-repeat padding-box;
  opacity: 1;
}

#tooltip1 {
  position: relative;
  width: 100%;
  margin-left: 10px;
  display: flex;
  justify-content: flex-start;
}

#tooltip1 #toolTipText1 {
  visibility: hidden;
  border-width: 4px;
  border-style: solid;
  border-color: #ffffff;
  width: 100%;
  background-color: #fff;
  text-align: left;
  padding: 6px 0;
  font: normal normal normal 14px Helvetica;
  letter-spacing: 0px;
  line-height: 32px;
  color: #4b4b4b;
  opacity: 1;
  position: absolute;
  top: 25px;
  z-index: 1;
}

#tooltip1:hover #toolTipText1 {
  visibility: visible;
}
#myBadge1 {
  border-radius: 10px 10px 10px 10px;
  background: #43a1fc 0% 0% no-repeat padding-box;
  opacity: 1;
}

.checkboxcustom {
  margin-top: 3.9px;
}
.checkboxhead {
  margin-top: -3px;
}
.badge {
  font-size: 9px;
  text-align: left;
  height: 18px;
  width: 18px;
  white-space: nowrap;
  border-radius: 0.25rem;
  flex-direction: column-reverse;
  font-weight: normal;
  display: flex;
  float: right;
  margin-left: 4.2px;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  vertical-align: baseline;
  border-radius: 100%;
  flex-direction: row-reverse;
  justify-content: space-around;
  align-items: center;
}
.badge-danger {
  color: #fff;
  background-color: #f86c6b;
}
</style>