<template>
  <div>
    <a-modal
      v-model="visible"
      :body-style="{ padding: '0' }"
      width="550px"
      :closable="false"
      :footer="null"
      centered
    >
      <div class="custom-modal-title">
        <span class="modal-title">Invite user to your eMoldino platform</span>
        <div
          class="t-close-button close-button"
          @click="
            resetInviteData();
            visible = false;
          "
          aria-hidden="true"
        >
          <span class="t-icon-close"></span>
        </div>
        <!--        <span
          class="close-button"
          style="font-size: 25px; display: flex; align-items: end; color: #000"
          @click="resetInviteData(); visible = false"
          aria-hidden="true"
        >
          <span style="height: 100%">&#215;</span>
        </span>-->
        <div class="head-line"></div>
      </div>
      <div class="custom-modal-body-user">
        <div class="d-flex flex-column align-items-center">
          <div class="column-item">
            <div style="margin-right: 20px; color: #4b4b4b">Email Address</div>
            <div style="color: #4b4b4b; width: calc(42% + 33px)">
              <span>Company (Required)</span>
              <div style="width: 20px; margin-left: 15px"></div>
            </div>
          </div>
          <div
            v-for="(inviteUser, index) in inviteUsers"
            :key="index"
            style="margin-bottom: 12px"
            class="column-item"
          >
            <div style="margin-right: 20px">
              <div
                v-if="inviteUser.type == 'one'"
                style="position: relative; width: 200px"
              >
                <input
                  @focusout="handleFocusOut(inviteUser)"
                  @input="checkCleanInvalidEmail(inviteUser)"
                  :class="{
                    errorInviteInput:
                      !inviteUser.isValid && inviteUser.message.trim() != '',
                    'invite-input': true,
                  }"
                  type="danger"
                  v-model="inviteUsers[index].email"
                  placeholder="Eg. john@example.com"
                />
                <a-tooltip
                  v-show="
                    !inviteUser.isValid && inviteUser.message.trim() != ''
                  "
                  class="icon-warning"
                  slot="suffix"
                  :title="inviteUser.message"
                  placement="bottomLeft"
                  style="padding: 6px"
                >
                  <a-icon
                    type="warning"
                    theme="filled"
                    style="color: #ff2b3a"
                  />
                </a-tooltip>
              </div>
              <div
                v-else-if="inviteUser.type == 'multiple'"
                style="position: relative; width: 200px"
              >
                <textarea
                  @focusout="handleFocusOut(inviteUser)"
                  @input="checkCleanInvalidEmail(inviteUser)"
                  :class="{
                    errorInviteInput:
                      !inviteUser.isValid && inviteUser.message.trim() != '',
                    'invite-input': true,
                  }"
                  v-model="inviteUsers[index].email"
                  placeholder="Enter emails separated by a comma"
                >
                </textarea>
                <a-tooltip
                  class="icon-warning"
                  v-show="
                    !inviteUser.isValid && inviteUser.message.trim() != ''
                  "
                  slot="suffix"
                  :title="inviteUser.message"
                  placement="bottomLeft"
                  style="padding: 6px"
                >
                  <a-icon
                    type="warning"
                    theme="filled"
                    style="color: #ff2b3a"
                  />
                </a-tooltip>
              </div>
              <!--            </input>-->
            </div>
            <div
              style="
                width: calc(42% + 33px);
                display: flex;
                align-items: center;
              "
            >
              <a-select
                placeholder="Company"
                class="custom-select-invite"
                style="z-index: 4001; width: 200px"
                :value="inviteUser.company"
                :key="index + 'select'"
                @focus="
                  (e) => {
                    onShowCompany('focus', index);
                  }
                "
              >
                <a-select-opt-group
                  v-if="companies && companies.companiesInHouse"
                >
                  <span slot="label">
                    {{ resources["in_house"] }}
                  </span>
                  <a-select-option
                    v-for="company in companies.companiesInHouse"
                    :key="company.id"
                    @click="handleChangeCompany(inviteUser, company)"
                  >
                    <a-tooltip placement="right">
                      <template slot="title">
                        <span v-if="!company.tooltipOff">{{
                          `${company.name} - ${company.id}`
                        }}</span>
                      </template>
                      <div
                        :id="'label-' + index + '-' + company.id"
                        style="width: 100%; cursor: pointer"
                        class="form-check-label"
                      >
                        <input
                          type="radio"
                          :name="'company' + index"
                          class="form-check-input"
                          :checked="inviteUser.companyId === company.id"
                        />
                        <span>{{ `${company.name} - ${company.id}` }}</span>
                      </div>
                    </a-tooltip>
                  </a-select-option>
                </a-select-opt-group>

                <a-select-opt-group>
                  <span slot="label">
                    {{ resources["supplier"] }}
                  </span>

                  <a-select-option
                    v-for="company in companies.companiesSupplier"
                    :key="company.id"
                    @click="handleChangeCompany(inviteUser, company)"
                  >
                    <a-tooltip placement="right">
                      <template slot="title">
                        <span v-if="!company.tooltipOff">{{
                          `${company.name} - ${company.id}`
                        }}</span>
                      </template>
                      <div
                        :id="'label-' + index + '-' + company.id"
                        style="width: 100%; cursor: pointer"
                        class="form-check-label"
                      >
                        <input
                          type="radio"
                          :name="'company' + index"
                          class="form-check-input"
                          :checked="inviteUser.companyId === company.id"
                        />
                        <span>{{ `${company.name} - ${company.id}` }}</span>
                      </div>
                    </a-tooltip>
                  </a-select-option>
                </a-select-opt-group>

                <a-select-opt-group>
                  <span slot="label">
                    {{ resources["toolmaker"] }}
                  </span>
                  <a-select-option
                    v-for="company in companies.companiesToolMaker"
                    :key="company.id"
                    @click="handleChangeCompany(inviteUser, company)"
                  >
                    <!--                      <a-tooltip placement="right" :get-popup-container="getPopupContainer">-->
                    <a-tooltip placement="right">
                      <template slot="title">
                        <span v-if="!company.tooltipOff">{{
                          `${company.name} - ${company.id}`
                        }}</span>
                      </template>
                      <div
                        :id="'label-' + index + '-' + company.id"
                        style="width: 100%; cursor: pointer"
                        class="form-check-label"
                      >
                        <input
                          type="radio"
                          :name="'company' + index"
                          class="form-check-input"
                          :checked="inviteUser.companyId === company.id"
                        />
                        <span>{{ `${company.name} - ${company.id}` }}</span>
                      </div>
                    </a-tooltip>
                  </a-select-option>
                </a-select-opt-group>
                <a-icon
                  style="color: #3491ff"
                  slot="suffixIcon"
                  type="caret-down"
                />
              </a-select>
              <div
                style="
                  display: flex;
                  align-items: center;
                  width: 20px;
                  margin-left: 15px;
                "
              >
                <div
                  v-show="inviteUsers.length > 1"
                  @click="deleteUser(index)"
                  class="t-icon-close-8 close-button"
                ></div>
                <!--                  <a-icon
                      v-show="inviteUsers.length > 1"
                      type="close"
                      @click="deleteUser(index)"
                  />-->
              </div>
            </div>
          </div>
        </div>
        <div
          class="main-content d-flex align-items-center margin-right-31"
          style="height: 21px; margin-bottom: 9.5px"
        >
          <span style="color: #3491ff !important; margin-right: 4px">+ </span>
          <a
            @click="addUser('one')"
            href="javascript:void(0)"
            id="animation-one"
            class="invite-link"
          >
            Add new</a
          >

          <span style="margin: 0 5px">or</span>
          <a
            @click="addUser('multiple')"
            href="javascript:void(0)"
            id="animation-multiple"
            class="invite-link"
          >
            Add multiple at once
          </a>
          <a-tooltip
            style="margin-left: 5px; padding: 6px"
            placement="bottomLeft"
          >
            <template slot="title">
              <div style="font-size: 13px; padding: 6px">
                Invite more than one user from the same company.
              </div>
            </template>
            <div>
              <img
                style="width: 12px"
                src="/images/icon/information.svg"
                alt="error icon"
              />
            </div>
          </a-tooltip>
        </div>
        <div
          class="d-flex justify-content-center align-items-start margin-right-31"
        >
          <img
            style="margin-right: 5px; width: 15px"
            src="/images/icon/light.png"
          />
          <span style="font-size: 11px">
            Invited users will be assigned a default access level (Regular) and
            access feature depending the company they belong to.<br />
            Settings can be changed after the user has joined the platform.
          </span>
        </div>
        <div
          style="margin-top: 13px"
          class="d-flex justify-content-end margin-right-31"
        >
          <a
            v-show="checkValidAllUser()"
            id="submitUsers"
            key="submit"
            class="btn-custom btn-invite"
            @click="submitUsers()"
          >
            Send Invitation
          </a>
          <a
            v-show="!checkValidAllUser()"
            key="submit2"
            class="btn-custom send-disabled btn-invite"
          >
            Send Invitation
          </a>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model="visibleSuccess"
      width="264px"
      :footer="null"
      :closable="false"
      class="text-center col-3"
      @close="handleOkay"
      centered
    >
      <div class="flex-column-custom">
        <img
          style="width: 34px"
          src="/images/icon/check-invite.png"
          alt="check-invite"
        />
        <div style="font-size: 16px; margin: 9px 0px 3px; color: #3491ff">
          Invited {{ getInvitedNumber }}
          <span>{{ inviteUsers.length == 1 ? "user" : "users" }}</span>
        </div>
        <div style="font-size: 13px; margin-bottom: 10px; color: #4b4b4b">
          Your invitation has been sent successfully.
        </div>
        <a
          @click="handleOkay"
          href="javascript:void(0)"
          id="handleOkay"
          class="btn-custom btn-custom-primary"
        >
          <span style="font-size: 11px; line-height: 12px">Okay</span>
        </a>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  // name: "InviteUser",
  props: {
    resources: Object,
  },
  data() {
    return {
      visible: false,
      visibleSuccess: false,
      inviteUsers: [
        {
          index: 1,
          email: "",
          type: "one",
          companyId: undefined,
          companyName: "",
          company: undefined,
          isValid: false,
          message: "",
        },
      ],
      companies: {},
      companyList: [],
    };
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        $("input[name=company0]").attr("checked", false);
        this.inviteUsers = [
          {
            index: 1,
            email: "",
            type: "one",
            companyId: undefined,
            company: undefined,
            isValid: false,
            message: "",
            companyName: "",
          },
        ];
      }
    },
  },
  methods: {
    checkCleanInvalidEmail(inviteUser) {
      if (!inviteUser.isValid && inviteUser.message.trim() != "") {
        this.checkValidEmail(inviteUser);
        console.log("Focus ", inviteUser);
      }
    },
    handleFocusOut(inviteUser) {
      // do something here
      console.log("FocusOut ", inviteUser);
      this.checkValidEmail(inviteUser);
    },
    showInviteUser() {
      this.resetInviteData();
      this.visible = true;
    },
    mappingCompany(name) {
      if (this.companyList) {
        return this.companyList.filter((item) => item.companyType === name);
      }
    },
    async initForm() {
      let sefl = this;
      await axios
        .get("/api/companies/companies-group")
        .then((response) => {
          sefl.companies = response.data.data;
          console.log(this.companies);
        })
        .catch((error) => {
          console.log(error);
        });

      axios
        .get("/api/companies?query=&size=99999&status=active&sort=id%2Cdesc")
        .then((res) => {
          sefl.companyList = res.data.content;
        });
    },
    convertToListInviteOneValue(inviteUsers) {
      if (inviteUsers == null) return [];
      return inviteUsers.map((item) => {
        let newObject = {};
        if (item.type == "multiple") {
          const patten = /\s*,\s*/;
          let emails = item.email.trim().split(patten);
          newObject = {
            index: item.index,
            companyId: item.companyId,
            emailList: emails,
          };
        } else {
          newObject = {
            index: item.index,
            companyId: item.companyId,
            email: item.email.trim(),
          };
        }
        return newObject;
      });
    },
    async submitUsers() {
      const el = document.getElementById("submitUsers");
      el.classList.add("primary-animation");
      let param = this.convertToListInviteOneValue(this.inviteUsers);
      let self = this;
      setTimeout(async () => {
        await axios
          .post("/api/users/invite-user", { userInviteList: param })
          .then((response) => {
            let data = response.data;
            if (!data.success) {
              data.data.forEach((element) => {
                let wrongData = self.inviteUsers.filter(
                  (item) => item.index == element.index
                );
                if (wrongData.length > 0) {
                  setTimeout(() => {
                    wrongData[0].isValid = false;
                    if (wrongData[0].type == "one") {
                      wrongData[0].message = `Email address is already registered in the system.`;
                    } else {
                      let wrongUser = element.data.join(", ");
                      if (element.data.length > 1) {
                        let lastComma = wrongUser.lastIndexOf(", ");
                        let wrongUsers =
                          wrongUser.substring(0, lastComma) +
                          " and " +
                          wrongUser.substring(lastComma + 1);
                        wrongData[0].message = `User ${wrongUsers} are already registered in the system.`;
                      } else {
                        wrongData[0].message = `User ${wrongUser} is already registered in the system.`;
                      }
                    }
                    el.classList.remove("primary-animation");
                  }, 500);
                }
              });
            } else {
              self.visible = false;
              self.visibleSuccess = true;
            }
          })
          .catch((error) => {
            console.log(error);
          })
          .finally(() => {
            el.classList.remove("primary-animation");
          });
      }, 700);
    },
    resetInviteData() {
      this.inviteUsers = [
        {
          index: 1,
          email: "",
          type: "one",
          company: undefined,
          companyId: undefined,
          companyName: "",
          isValid: false,
          message: "",
        },
      ];
    },
    handleOkay(e) {
      const el = document.getElementById("handleOkay");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        self.resetInviteData();
        el.classList.remove("primary-animation");
      }, 700);
      this.visibleSuccess = false;
    },
    addUser(type) {
      const el = document.getElementById(`animation-${type}`);
      el.classList.add("primary-animation-invite");
      const self = this;
      setTimeout(() => {
        let newIndex = +self.inviteUsers[self.inviteUsers.length - 1].index + 1;
        self.inviteUsers.push({
          index: newIndex,
          email: "",
          type: `${type}`,
          company: undefined,
          companyId: undefined,
          companyName: "",
          isValid: false,
          message: "",
        });
        el.classList.remove("primary-animation-invite");
      }, 700);
    },
    deleteUser(index) {
      let inviteUser = this.inviteUsers[index];
      this.inviteUsers.splice(index, 1);
      this.inviteUsers
        .filter((iv) => iv.index != inviteUser.index)
        .forEach((iv) => this.checkDuplicatedEmail(iv));
      console.log(this.inviteUsers);
    },
    checkValidAllUser() {
      let enable = this.inviteUsers.filter(
        (item) => !item.isValid || item.companyId == undefined
      );
      return enable.length == 0;
    },
    clearErrorValidEmail(inviteUser) {
      inviteUser.isValid = false;
      inviteUser.message = "";
    },
    checkValidEmail(inviteUser) {
      if (inviteUser.email == "") {
        inviteUser.isValid = false;
        inviteUser.message = "";
      } else {
        if (inviteUser.type == "one") {
          const patten =
            /^\s*[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)\s*$/;
          if (patten.test(inviteUser.email)) {
            inviteUser.isValid = true;
            inviteUser.message = "";
          } else {
            inviteUser.isValid = false;
            inviteUser.message =
              "Please enter a valid email format (e.g.example@email.com).";
            return;
          }
        } else {
          const patten =
            /^\s*[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)+((\s*,\s*)([A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\.)+([A-Za-z0-9]{2,4}|museum)))*\s*$/;
          if (patten.test(inviteUser.email)) {
            inviteUser.isValid = true;
            inviteUser.message = "";
          } else {
            inviteUser.isValid = false;
            inviteUser.message =
              "Please enter a valid email format (e.g.example@email.com).";
            return;
          }
        }
      }
      this.checkDuplicatedEmail(inviteUser);
      //check for other list error;
      this.inviteUsers
        .filter((iv) => iv.index != inviteUser.index)
        .forEach((iv) => this.checkDuplicatedEmail(iv));
    },
    checkDuplicatedEmail(inviteUser) {
      //check duplicate
      if (
        (inviteUser.isValid || inviteUser.isValidDuplicateOtherItem == false) &&
        inviteUser.email
      ) {
        let current = this.convertToListInviteOneValue([inviteUser])[0];
        console.log("current ", current);
        if (inviteUser.type == "multiple") {
          let listEmailDuplicate = current.emailList.filter((email) => {
            return (
              current.emailList.filter(
                (s) => s.toLowerCase() == email.toLowerCase()
              ).length > 1
            );
          });
          if (listEmailDuplicate.length > 0) {
            inviteUser.isValid = false;
            inviteUser.message = `Email ${this.listToText([
              ...new Set(listEmailDuplicate),
            ])} ${
              listEmailDuplicate.length == 1 ? "has" : "have"
            } already been entered.`;
            return;
          }
        }

        let allInviteList = this.convertToListInviteOneValue(this.inviteUsers);
        // let index=this.inviteUsers.indexOf(inviteUser);
        let listEmailOther = [];

        allInviteList.forEach((invite) => {
          if (invite.index != inviteUser.index) {
            if (invite.email != null)
              listEmailOther.push(invite.email.toLowerCase());
            else if (invite.emailList != null)
              listEmailOther.push(
                ...invite.emailList.map((s) => s.toLowerCase())
              );
          }
        });

        let currList = current.emailList || [current.email];
        let listEmailDuplicate = currList.filter((email) => {
          return listEmailOther.includes(email.toLowerCase());
        });
        if (listEmailDuplicate.length > 0) {
          inviteUser.isValid = false;
          inviteUser.message = `Email ${this.listToText(listEmailDuplicate)} ${
            listEmailDuplicate.length == 1 ? "has" : "have"
          } already been entered.`;
          inviteUser.isValidDuplicateOtherItem = false;
          return;
        } else {
          inviteUser.isValid = true;
          inviteUser.message = "";
          inviteUser.isValidDuplicateOtherItem = true;
        }
      }
    },
    listToText(emailList) {
      let text = "";
      if (emailList.length > 0) {
        if (emailList.length == 1) return emailList[0];
        for (let i = 0; i < emailList.length - 2; i++) {
          text += emailList[i] + ", ";
        }
        text +=
          emailList[emailList.length - 2] +
          " and " +
          emailList[emailList.length - 1];
      }
      return text;
    },

    handleChangeCompany(inviteUser, company) {
      inviteUser.company = company.name + company.id;
      inviteUser.companyId = company.id;
      inviteUser.companyName = company.name;
    },
    isEllipsisActive(e) {
      // console.log("e.offsetWidth ",e.offsetWidth )
      // console.log("e.scrollWidth ",e.scrollWidth )
      return e.offsetWidth < e.scrollWidth;
    },
    getPopupContainer(triggerNode) {
      let isShow = this.isEllipsisActive(triggerNode);
      console.log("isShowTooltip", isShow);
      console.log("triggerNode", triggerNode);
      if (!isShow) {
        // triggerNode.style.display = 'none';
        return document.createElement("div");
      }
      // element.innerHTML=`<span>Test ${company.name} - ${company.id}</span>`;
      return triggerNode.parentElement;
    },
    onShowCompany(open, index) {
      console.log("onShowCompany", open);
      console.log("onShowCompany index ", index);
      let self = this;
      setTimeout(() => {
        if (this.companies) {
          let totalCompany = [
            ...(this.companies.companiesInHouse || []),
            ...(this.companies.companiesSupplier || []),
            ...(this.companies.companiesToolMaker || []),
          ];

          totalCompany.forEach((c) => {
            let id = "label-" + index + "-" + c.id;
            let itemNode = document.getElementById(id);
            if (itemNode != null && itemNode.scrollWidth != 0) {
              // console.log('item node',itemNode)
              let isShow = this.isEllipsisActive(itemNode);
              c.tooltipOff = !isShow;
            }
            console.log(id, " off ", c.tooltipOff);
          });
          self.$forceUpdate();
        }
      }, 50);
    },
  },
  created() {
    this.initForm();
  },
  computed: {
    getInvitedNumber() {
      let count = 0;
      this.inviteUsers.forEach((element) => {
        if (element.type == "multiple") {
          const patten = /\s*,\s*/;
          let emails = element.email.split(patten);
          count += emails.filter((item) => item.trim() != "").length;
        } else {
          count++;
        }
      });
      return count;
    },
  },
  mounted() {},
};
</script>
<style>
.ant-select-dropdown {
  z-index: 99999999;
}
.ant-select-dropdown-menu-item-selected {
  font-weight: 400;
  background-color: #fff;
}
.custom-modal-body-user {
  padding: 23px 32px 26px 32px;
}

.send-disabled {
  background: #c4c4c4 !important;
  border: 2px solid #c4c4c4 !important;
  color: #fff !important;
  pointer-events: none;
}

.invite-input {
  outline: none !important;
  box-shadow: none !important;
  border-radius: 1.7px !important;
  min-height: 100%;
  transition: all 0.3s, border 0s;
  display: inline-block;
  width: 100%;
  height: 32px;
  padding: 4px 30px 4px 11px;
  color: #4b4b4b;
  font-size: 14px;
  line-height: 1.5;
  background-color: #fff;
  border-radius: 2px;
  border: 0.5px solid #909090;
}

textarea.invite-input {
  max-width: 100%;
  height: auto;
  min-height: 32px;
  vertical-align: bottom;
  transition: all 0.3s, height 0s, border 0s;
}

.invite-input::placeholder {
  color: #c9c9c9;
}

.invite-input:hover {
  border: 0.5px solid #4b4b4b;
}

.invite-input:hover::placeholder {
  color: #4b4b4b;
}

.invite-input:focus {
  border: 2px solid #98d1fd;
}

.errorInviteInput {
  border: 0.5px solid #ef4444 !important;
}

.ant-modal-body {
  padding: 0;
  border-radius: 6px;
}

.ant-modal-content {
  border-radius: 6px;
}

.nav-btn {
  border-radius: 3px;
  font-size: 11px;
  padding: 6px 8px 6px 8px;
}

.nav-btn-primary {
  background-color: #3491ff;
  color: #ffffff;
  border: none;
}

.nav-btn-outline {
  color: #3491ff;
  background-color: #ffffff;
  border: 1px solid #3491ff;
}

.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.pre-header {
  height: 10px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  background-color: #52a1ff;
}

.modal-container {
  margin: 0px auto;
}

.main-content {
  padding: 10px 0px;
}

.modal-container-main {
  background-color: #fff;
  border-radius: 2px;
  color: #4e4e4e;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  transition: all 0.3s ease;
  font-family: Helvetica, Arial, sans-serif;
}

.modal-header {
  background-color: #f5f8ff;
}

.modal-header h2 {
  margin-top: 0;
  margin-bottom: 0;
  color: #4e4e4e;
}

.modal-body {
  padding: 20px 30px;
}

.invite-link {
  /*color: #3491ff;*/
  color: #298bff;
  cursor: pointer;
  text-decoration: none;
}

.invite-link:hover {
  color: #298bff;
  text-decoration: underline;
}
/*
a.invite-link:hover:not(a.invite-link.primary-animation-invite){
  color: #298BFF;
}
*/
/*.invite-link:active {*/
/*  color: #006cff !important;*/
/*}*/

/*.invite-link:focus {*/
/*  text-decoration: unset;*/
/*}*/
a:focus {
  text-decoration: unset;
}
.primary-animation-invite {
  /*animation: primary-animation-invite 0.7s;*/
  /*animation-iteration-count: 1;*/
  /*animation-direction: alternate;*/
  color: #006cff !important;
  text-decoration: none !important;
}

@keyframes primary-animation-invite {
  0% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  33% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  66% {
    color: #006cff !important;
    text-decoration: none !important;
  }
  100% {
  }
}

.footer {
  margin-top: 20px;
}

.modal-default-button {
  float: right;
}

.modal-footer {
  border-top: none;
}

.modal-enter {
  opacity: 0;
}

.modal-leave-active {
  opacity: 0;
}

.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}

.icon-warning {
  position: absolute;
  top: 50%;
  right: 12px;
  transform: translate(0px, -50%);
}

.ant-input-warning {
  border-color: #ff6261;
  outline: 0;
  -webkit-box-shadow: 0 0 0 2px rgba(87, 168, 233, 0.2);
  box-shadow: 0 0 0 2px rgba(87, 168, 233, 0.2);
}

.btn-invite {
  border: 2px solid #47b576;
  background-color: #47b576;
  color: #ffffff !important;
  line-height: 11pt;
}

.btn-invite:hover {
  border: 2px solid #378e5c;
  background-color: #378e5c;
}
.ant-select-selection {
  transition: all 0.3s, border 0s;
}
.custom-select-invite.ant-select-open .ant-select-selection {
  border: 2px solid #98d1fd !important;
}
.ant-select-selection:active {
  border: 2px solid #98d1fd !important;
}
/*.btn-invite:active {*/
/*  color: #fff !important;*/
/*  background: #378e5c;*/
/*  border: 2px solid #a1e8bf;*/
/*}*/
.margin-right-31 {
  margin-right: 31px;
}
.ant-select-selection__rendered {
  max-width: 180px;
}
div.form-check-label {
  width: 170px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.ant-select-selection-selected-value {
  height: 30px;
}
</style>
