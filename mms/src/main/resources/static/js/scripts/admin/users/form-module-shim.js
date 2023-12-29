// import { CtaButton, Dropdown, Modal, Chips } from "@emoldino/components";

Vue.component("emdn-cta-button", EmoldinoComponents.CtaButton);
Vue.component("emdn-dropdown", EmoldinoComponents.Dropdown);
Vue.component("emdn-modal", EmoldinoComponents.Modal);
Vue.component("emdn-chips", EmoldinoComponents.Chips);

let timeout = null;
let Page = Common.getPage("users");

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";

let intervalCheckAccessFeature;

let vm = new Vue({
  el: "#app",
  components: {
    "company-search": httpVueLoader("/components/filter/company-search.vue"),
  },
  data: {
    logTest: true,
    userData: {},
    availableRoles: [],
    plants: [],
    resources: {},
    isNew: Page.IS_NEW,
    requireCheckbox: false,
    listAccessFeature: [],
    alertStatusList: [],
    show: false,
    codes: {},
    typing: false,
    type: "password",
    has_number: false,
    has_lowercase: false,
    has_uppercase: false,
    has_special: false,
    pattern1:
      "^(?=.*[A-Z])(?=.*\\d)(?=.*[.,!@#$%^&*\\+=\\-_])[A-Za-z\\d.,!@#$%^&*\\+=\\-_]{8,}$",
    pattern2:
      "^(?=.*[A-Z])(?=.*\\d)(?=.*[.,!@#$%^&*\\+=\\-_])[A-Za-z\\d.,!@#$%^&*\\+=\\-_]{8,}$",

    // 현재 미사용
    listAlertType: [
      "CORRECTIVE_MAINTENANCE",
      "CYCLE_TIME",
      "DATA_SUBMISSION",
      "DISCONNECTED",
      "MAINTENANCE",
      "MISCONFIGURE",
      "RELOCATION",
      "EFFICIENCY",
      "REFURBISHMENT",
      "DETACHMENT",
      "DOWNTIME",
    ],
    roleId: [],
    roles: [],
  },
  methods: {
    getCompany(company, searchType, valueType) {
      console.log("getCompany");
      console.log("company: ", company);
      console.log("searchType: ", searchType);
      console.log("valueType: ", valueType);
      // this.userData.company = company;
      this.userData.companyId = company.id;
      this.userData.companyName = company.name;
      this.userData.companyType = company.companyType;

      // plants와 roles 새로 구성하기
      let url = this.userData.id
        ? `/api/common/usr-stp/${this.userData.id}/plants?companyId=${this.userData.companyId}`
        : `/api/common/usr-stp/plants?companyId=${this.userData.companyId}`;

      console.log("url: ", url);

      axios.get(url).then((res) => {
        this.availableRoles = res.data.availableRoles;

        res.data.content.map((plant) => {
          plant.visible = false;
          plant.availableRoles = JSON.parse(
            JSON.stringify(this.availableRoles)
          );
          if (plant.roles.length > 0) {
            plant.roles.map((selectedRole) => {
              plant.availableRoles.map((role) => {
                if (selectedRole.id === role.id) {
                  role.checked = true;
                }
              });
            });
          }
        });

        this.plants = res.data.content;
      });
    },
    dropdownButtonName(plant) {
      let selectedRole = plant.availableRoles.filter((item) => {
        return item.checked;
      });

      return selectedRole.length === 0
        ? "Select Role(s)"
        : `${selectedRole.length} ${
            selectedRole.length === 1 ? "Role" : "Roles"
          }`;
    },
    deleteChipHandler(plantIndex, availableRoleIndex) {
      this.logTest &&
        console.log("deleteChipHandler: ", plantIndex, availableRoleIndex);
      this.logTest &&
        console.log(this.plants[plantIndex].availableRoles[availableRoleIndex]);
      this.plants[plantIndex].availableRoles[
        availableRoleIndex
      ].checked = false;
      this.$set(this.plants, plantIndex, this.plants[plantIndex]);
    },
    plantFilterToggle(index) {
      this.logTest && console.log("plantFilterToggle: ", index);
      this.plants[index].visible = !this.plants[index].visible;
      this.$set(this.plants, index, this.plants[index]);
    },
    setPlantFilterResult(result, index) {
      this.logTest && console.log("setPlantFilterResult----", result, index);
    },
    plantFilterClose(index) {
      this.logTest && console.log("plantFilterClose: ", index);
      this.plants[index].visible = false;
      this.$set(this.plants, index, this.plants[index]);
    },
    checkEditPage: function () {
      return Page.IS_EDIT;
    },
    password_check: function () {
      this.typing = true;
      this.has_number = /\d/.test(this.userData.password);
      this.has_lowercase = /[a-z]/.test(this.userData.password);
      this.has_uppercase = /[A-Z]/.test(this.userData.password);
      this.has_special =
        /(?=.*[.,!@#$%^&*\+=\-_])[A-Za-z\d.,!@#$%^&*\+=\-_]/.test(
          this.userData.password
        );
    },
    checkAll: function () {
      let roleIds = [];

      $("input[name=roleId]:checked").each(function (index) {
        roleIds[index] = $(this).val();
      });

      this.userData.roleIds = roleIds;
    },
    showPassword: function () {
      if (this.type === "password") {
        this.type = "text";
        this.show = true;
      } else {
        this.type = "password";
        this.show = false;
      }
    },
    submit: function () {
      console.log("submit");

      let newArray = [];

      // this.userData.alertStatus = this.getAlertStatus();

      this.userData.plants = this.plants;
      this.userData.availableRoles = this.availableRoles;

      if (this.userData.plants) {
        this.userData.plants.map((plant) => {
          newArray = plant.availableRoles.filter((availableRole) => {
            return availableRole.checked === true;
          });
          plant.roles = newArray;
        });
      }

      if (this.userData.password != this.userData.passwordConfirm) {
        Common.alert("Your password and confirmation password do not match");
        return;
      }

      if (!this.userData.companyId) {
        Common.alert("Company is required!");
        return;
      }

      let parameter = JSON.parse(JSON.stringify(this.userData));

      if (parameter.plants) {
        parameter.plants.map((plant) => {
          delete plant.availableRoles;
          delete plant.visible;
          plant.roles.map((role) => {
            delete role.checked;
          });
        });
      }

      if (Page.IS_NEW) {
        this.create(parameter);
      } else {
        this.update(parameter);
      }
    },
    create: function (parameter) {
      const self = this;

      console.log("create !!!");
      console.log("parameter: ", parameter);

      axios
        .post(`/api/common/usr-stp/one`, parameter)
        .then(function (response) {
          self.logTest && console.log(response.data);

          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = "/admin/users";
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          self.logTest && console.log(error.response);
        });
    },
    update: function (parameter) {
      const self = this;

      console.log("update !!!");
      console.log("parameter: ", parameter);

      if (this.userData.companyType !== "IN_HOUSE") {
        this.userData.accessLevel = "REGULAR";
      }

      axios
        .put(`/api/common/usr-stp/${this.userData.id}`, parameter)
        .then(function (response) {
          self.logTest && console.log("Page.API_PUT: ", response.data);

          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = "/admin/users";
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          self.logTest && console.log(error.response);
        });
    },

    // mobile number 국가 선택
    selectMobileDialing: function (country) {
      console.log("selectMobileDialing: ", country);
      this.userData.mobileDialingCode = country.description;
    },

    // 현재 사용 안함.
    checkAccessFeature: function () {
      try {
        this.logTest &&
          console.log("this.userData.status: ", this.userData.status);
        this.logTest && console.log(JSON.stringify(this.user));
        if (this.userData.companyType !== "IN_HOUSE") {
          this.userData.accessLevel = "REGULAR";
        }
        if (
          document
            .getElementById("accessFeature")
            ?.getElementsByClassName("input_checkbox")?.[0]
        ) {
          if (
            this.userData.roleIds.length === 0 &&
            this.userData.companyType !== "IN_HOUSE" &&
            this.userData.status == "ENABLED"
          ) {
            document
              .getElementById("accessFeature")
              .getElementsByClassName("input_checkbox")[0].required = true;
          } else {
            document
              .getElementById("accessFeature")
              .getElementsByClassName("input_checkbox")[0].required = false;
          }
        }
      } catch (ex) {
        this.logTest && console.log("ex", ex);
      }

      let url =
        Page.API_BASE +
        `/alert-status?companyType=${this.userData.companyType}` +
        `&roleIds=${this.userData.roleIds.toString()}`;

      if (this.userData.id) {
        url += `&userId=${this.userData.id}`;
      }

      this.logTest && console.log("url", url);

      this.getListAlertStatus(url);
    },

    // 현재 사용 안함.
    sortAlertType: function (array) {
      let i,
        j,
        lengthType = this.listAlertType.length,
        lengthArray = array.length,
        alertTypes = [];
      for (i = 0; i < lengthType; i++) {
        for (j = 0; j < lengthArray; j++) {
          if (this.listAlertType[i] === array[j].alertType) {
            alertTypes.push(array[j]);
            break;
          }
        }
      }
      return alertTypes;
    },

    // 현재 사용 안함
    getListAlertStatus: function (url) {
      console.log("getListAlertStatus: ", url);
      const self = this;

      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
      function getAlerts() {
        this.alertStatusList = [];

        self.logTest && console.log("url: ", url);

        axios.get(url).then(function (response) {
          if (response.data) {
            let alertListLength = response.data.length;

            let alertList = this.sortAlertType(response.data);

            self.logTest && console.log("Response", response.data);
            self.logTest && console.log("alertList", alertList);

            let i,
              chuck = 3,
              newArray = [];

            for (i = 0; i < alertListLength; i++) {
              self.logTest && console.log(`alertList ${i}`, alertList[i]);
              alertList[i].status = false;
              switch (alertList[i].alertType) {
                case "CYCLE_TIME":
                  alertList[i].name = "Cycle Time";
                  break;
                case "DATA_SUBMISSION":
                  alertList[i].name = "Data Submission";
                  break;
                case "DISCONNECTED":
                  alertList[i].name = "Disconnection";
                  break;
                case "EFFICIENCY":
                  alertList[i].name = "Uptime";
                  break;
                case "MAINTENANCE":
                  alertList[i].name = "Preventive Maintenance";
                  break;
                case "CORRECTIVE_MAINTENANCE":
                  alertList[i].name = "Corrective Maintenance";
                  break;
                case "MISCONFIGURE":
                  alertList[i].name = "Reset";
                  break;
                case "RELOCATION":
                  alertList[i].name = "Relocation";
                  break;
                case "REFURBISHMENT":
                  alertList[i].name = "Refurbishment";
                  break;
                case "DETACHMENT":
                  alertList[i].name = "Detachment";
                  break;
                case "DOWNTIME":
                  alertList[i].name = "Downtime";
                  break;
              }

              newArray.push(alertList[i]);

              if (
                (i !== 0 && i + (1 % chuck) === 0) ||
                i === alertListLength - 1
              ) {
                this.alertStatusList.push(newArray);
                newArray = [];
              }
            }
          }
        });
      }
      timeout = setTimeout(getAlerts, 200);
    },
    // 현재 사용 안함.
    getAlertStatus: function () {
      let status = {};

      let i,
        secondIndex,
        lengthItem,
        length = this.alertStatusList.length;

      this.logTest && console.log("alertStatusList", this.alertStatusList);

      for (i = 0; i < length; i++) {
        lengthItem = this.alertStatusList[i].length;
        for (secondIndex = 0; secondIndex < lengthItem; secondIndex++) {
          status[this.alertStatusList[i][secondIndex].alertType] =
            this.alertStatusList[i][secondIndex].email == true
              ? true
              : this.alertStatusList[i][secondIndex].email == "true"
              ? true
              : false;
        }
      }

      this.logTest && console.log("status", status);

      return status;
    },

    async getResources() {
      try {
        const messages = await Common.getSystem("messages");
        this.resources = JSON.parse(messages);
      } catch (error) {
        this.logTest && console.log(error);
      }
    },
  },

  watch: {
    plants: {
      handler(newVal) {
        this.logTest && console.log("plants watcher: ", newVal);
      },
    },
    availableRoles(newVal) {
      this.logTest && console.log("availableRoles: ", newVal);
    },
  },
  computed: {
    // 국가 코드(ex: 82)로 국가 약어(ex: KR) 가져오기
    mobileDialingAbbreviation() {
      let result = "";
      if (this.codes?.CountryCode?.length > 0) {
        this.codes.CountryCode.map((item) => {
          if (item.description === this.userData.mobileDialingCode) {
            result = item.code;
          }
        });
      }
      return result;
    },
    readonly: function () {
      return Page.IS_EDIT;
    },
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },
  },

  mounted() {
    const self = this;

    this.userData.status = "ENABLED";
    this.userData.accessLevel = "REGULAR";
    this.userData.mobileDialingCode = Const.defaultDialingCode;

    this.$nextTick(function () {
      axios.get("/api/codes").then(function (response) {
        self.codes = response.data;
        self.logTest && console.log("codes: ", self.codes);
      });

      if (Page.ID !== "new") {
        axios.get(`/api/common/usr-stp/${Page.ID}`).then((res) => {
          self.logTest && console.log("/api/common/usr-stp/Page.ID 22: ", res);

          self.userData = res.data;
          self.availableRoles = res.data.availableRoles;
          self.userData.lastLoginShow = self.userData.lastLogin
            ? vm.convertTimestampToDateWithFormat(
                self.userData.lastLogin,
                "YYYY-MM-DD HH:mm:ss"
              )
            : "";

          self.logTest && console.log("password");
          self.logTest && console.log(self.userData.password);

          res.data.plants.map((plant) => {
            plant.visible = false;
            plant.availableRoles = JSON.parse(
              JSON.stringify(self.availableRoles)
            );
            if (plant.roles.length > 0) {
              plant.roles.map((selectedRole) => {
                plant.availableRoles.map((role) => {
                  if (selectedRole.id === role.id) {
                    role.checked = true;
                  }
                });
              });
            }
          });

          self.plants = res.data.plants;
        });
      }

      self.getResources();
    });
  },
});
