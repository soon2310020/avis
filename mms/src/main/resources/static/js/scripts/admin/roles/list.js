axios.defaults.headers.get["Content-Type"] =
  "application/x-www-form-urlencoded";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";
//axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';

window.onload = function () {
  document.title = "Access Control" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave1");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
  //
};

var Page = Common.getPage("roles");

var vm = new Vue({
  el: "#app",
  components: {
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "user-hierarchy": httpVueLoader(
      "/components/access-group/user-hierarchy.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
  },
  data: {
    isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
    deletePopup: false,
    resources: {},
    showDrowdown: null,
    results: [],
    total: 0,
    pagination: [],
    requestParam: {
      query: "",
      status: "menu",
      sort: "id,desc",
      page: 1,
    },
    sortType: {
      NAME: "name",
      DESCRIPTION: "description",
      TYPE: "roleType",
    },
    currentSortType: "id",
    isDesc: true,
    accFeature: true,
    showDropdown: false,
    listChecked: [],
    listCheckedTracked: {},
    isAll: false,
    isAllTracked: [],
    listCheckedFull: [],
    cancelToken: undefined,
  },
  watch: {
    listChecked(newVal) {
      if (newVal && newVal.length != 0) {
        const self = this;
        const x = this.results.filter(
          (item) => self.listChecked.indexOf(item.id) > -1
        );
        this.listCheckedFull = x;
      }
    },
  },
  methods: {
    onCreateNewRole() {
      Common.onChangeHref(`/admin/roles/new?=status=${this.accFeature}`);
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    select: function (event) {
      let isAll = event.target.checked;
      this.isAll = isAll;
      if (isAll) {
        this.listChecked = this.results.map((value) => value.id);
        Common.setHeightActionBar();
        this.isAllTracked.push({
          page: this.requestParam.page,
          status: this.requestParam.status,
        });
      } else {
        this.listChecked = [];
        this.isAllTracked = this.isAllTracked.filter(
          (item) =>
            item.page !== this.requestParam.page &&
            item.status !== this.requestParam.status
        );
      }
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    hasChecked: function () {
      let isExistedChecked = false;
      Object.keys(this.listCheckedTracked).forEach((status) => {
        Object.keys(this.listCheckedTracked[status]).forEach((page) => {
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
        this.listChecked = this.listChecked.filter(
          (value) => value != e.target.value
        );
        if (this.listChecked.length === 0) {
          this.isAll = false;
          this.isAllTracked = this.isAllTracked.filter(
            (item) =>
              item.page !== this.requestParam.page &&
              item.status !== this.requestParam.status
          );
        }
      }
      this.listCheckedTracked[this.requestParam.status][
        this.requestParam.page
      ] = this.listChecked;
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/roles/${this.listChecked[0]}#${this.requestParam.status}'&status='${this.accFeature}`;
    },
    callbackCompany: function (company, searchType) {
      var child = Common.vue.getChild(this.$children, "user-hierarchy");
      if (child != null) {
        child.createRelationalCompany(company, searchType);
      }
    },
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    hideCalendarPicker(event) {
      if (
        event.toElement &&
        event.toElement &&
        event.toElement.localName != "img"
      ) {
        this.changeShow(null);
      }
    },
    removeSub: function (role) {
      var pattern = new RegExp(role.roleType + "_", "gi");
      if (role.authority != null) {
        var authCode = role.authority.replace(pattern, "");
      }
      return authCode;
    },

    showRevisionHistory: function (id, roleType) {
      // console.log("ROLE TYPE>>>" + roleTypeName);
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "ROLE", roleType);
      }
    },
    enable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = true;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = true;
        });
        this.saveBatch(user, true);
      }
    },
    disable: function (user) {
      if (user.length <= 1) {
        user[0].enabled = false;
        this.save(user[0]);
      } else {
        user.forEach(function (item) {
          item.enabled = false;
        });
        this.saveBatch(user, false);
      }
    },
    saveBatch(user, boolean) {
      console.log(user, "user");
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      axios
        .post("/api/roles/change-status-in-batch", param)
        .then(function (response) {
          vm.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    save: function (user) {
      //var param = Common.param(item);
      console.log(user, "user");
      var param = {
        id: user.id,
        enabled: user.enabled,
      };
      axios
        .put("/api/roles/" + user.id + "/enable", param)
        .then(function (response) {
          vm.paging(1);
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    tab: function (status) {
      this.total = null;
      this.listChecked = [];
      this.requestParam.status = status;
      if (status === "hierarchy") {
        return;
      }
      if (status === "group") {
        this.accFeature = false;
      }
      if (status === "menu") {
        this.accFeature = true;
      }
      //window.location.href = `/admin/roles/new?=status=${this.accFeature}`
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";

      this.paging(1);
    },
    search: function (page) {
      this.paging(1);
    },
    deletePop(user) {
      if (user.length <= 1) {
        this.deleteRole(user[0]);
      } else {
        this.deleteBatch(user, true);
      }
    },
    deleteBatch(user) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
      };
      axios
        .post("/api/roles/delete-in-batch", param)
        .then(function (response) {
          if (response.data.success) {
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    deleteRole: function (role) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }

      var param = {
        id: role.id,
      };
      axios
        .delete(Page.API_BASE + "/" + role.id, param)
        .then(function (response) {
          if (response.data.success) {
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    paging: function (pageNumber) {
      vm.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      // checking is All
      this.isAll = false;
      let pageSelectedAll = this.isAllTracked
        .filter((item) => item.status === this.requestParam.status)
        .map((item) => item.page);
      // if (pageSelectedAll.includes(this.requestParam.page)) {
      //     this.isAll = true;
      // }
      if (!this.listCheckedTracked[this.requestParam.status]) {
        this.listCheckedTracked[this.requestParam.status] = {};
      }
      if (
        !this.listCheckedTracked[this.requestParam.status][
          this.requestParam.page
        ]
      ) {
        this.listCheckedTracked[this.requestParam.status][
          this.requestParam.page
        ] = [];
      }
      var param = Common.param(vm.requestParam);
      //var param = vm.requestParam;
      console.log("/api/roles/api/roles");
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }
      this.cancelToken = axios.CancelToken.source();
      axios
        .get("/api/roles?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          console.log("response: ", response);
          vm.total = response.data.totalElements;
          vm.results = response.data.content.map((value) => value.role);
          vm.pagination = Common.getPagingData(response.data);

          document.querySelector("#app .op-list").style.display =
            "table-row-group";
          document.querySelector(".pagination").style.display = "flex";

          var noResult = document.querySelector(".no-results");
          if (noResult != null) {
            if (vm.results.length == 0) {
              noResult.className = noResult.className.replace("d-none", "");
            } else {
              noResult.className =
                noResult.className.replace("d-none", "") + " d-none";
            }
          }

          var pageInfo =
            vm.requestParam.page == 1 ? "" : "?page=" + vm.requestParam.page;
          history.pushState(null, null, "/admin/roles" + pageInfo);
          if (vm.results.length > 0) {
            Common.triggerShowActionbarFeature(vm.$children);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    sortBy: function (type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort();
      }
    },
    sort: function () {
      this.paging(this.requestParam.page);
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        vm.resources = JSON.parse(messages);
      } catch (error) {
        console.log(error);
      }
    },
  },
  mounted() {
    let vm = this;
    window.onclick = function (event) {
      let resultId = null;
      vm.changeShow(resultId);
    };
    this.$nextTick(function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      var uri = URI.parse(location.href);
      console.log("uri: ", uri);
      if (uri.fragment == "menu") {
        vm.tab(uri.fragment);
      } else {
        this.paging(1);
      }
      vm.getResources();
    });
  },
});
