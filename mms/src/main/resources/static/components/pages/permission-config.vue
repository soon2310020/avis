<template>
  <div
    class="permission-config-container"
    style="position: relative; padding-bottom: 90px"
  >
    <!-- modal data: items 밑에 subpermissons 가 붙어서 나오는 구조임. -->
    <emdn-modal
      :is-opened="isModalVisible"
      modal-type="slide-in"
      heading-text="Function Permissions"
      :sub-heading-text="modalData.name"
      :modal-handler="closeModal"
    >
      <template #body>
        <ul>
          <!-- first modal item: items -->
          <li
            v-for="(modalFirstItem, modalFirstIndex) in modalData.items"
            v-if="modalFirstItem.visible"
            :key="modalFirstIndex"
          >
            <emdn-check-list
              :item-list="[modalFirstItem]"
              id-name="id"
              value-name="permitted"
              checked-name="permitted"
              :enabled-name="
                typeof modalFirstItem.editable === 'undefined' ? '' : 'editable'
              "
              label-text-name="name"
              :change-handler="oneDepthModalChange"
              :change-handler-param="[modalFirstIndex]"
              style-props="margin-bottom: 8px"
              :category="modalData.resourceId"
            >
              <span>{{ modalFirstItem.name }}</span>
            </emdn-check-list>
            <ul>
              <!-- second modal item: items of items -->
              <li
                v-for="(
                  modalSecondItem, modalSecondIndex
                ) in modalFirstItem.items"
                :key="modalSecondIndex"
              >
                <emdn-check-list
                  :item-list="[modalSecondItem]"
                  id-name="resourceId"
                  value-name="permitted"
                  checked-name="permitted"
                  :enabled-name="
                    typeof modalSecondItem.editable === 'undefined'
                      ? ''
                      : 'editable'
                  "
                  label-text-name="name"
                  :change-handler="twoDepthModalChange"
                  :change-handler-param="[
                    'items',
                    modalFirstIndex,
                    modalSecondIndex,
                  ]"
                  style-props="margin-bottom: 8px"
                  :category="modalFirstItem.resourceId"
                >
                  <slot>
                    <div style="cursor: pointer">
                      {{ modalSecondItem.name }}
                    </div>
                  </slot>
                </emdn-check-list>
              </li>
            </ul>
          </li>
          <!-- first modal item: subPermissions -->
          <li
            v-for="(
              modalFirstItem, modalFirstIndex
            ) in modalData.subpermissions"
            v-if="modalFirstItem.items && modalFirstItem.visible"
            :key="modalFirstIndex"
            style="margin-top: 16px"
          >
            <span style="font-size: 14.66px">{{ modalFirstItem.name }}</span>
            <ul style="margin-left: 14px; margin-top: 8px">
              <!-- second modal item: items of subpermissions -->
              <li
                v-for="(
                  modalSecondItem, modalSecondIndex
                ) in modalFirstItem.items"
                :key="modalSecondIndex"
              >
                <emdn-check-list
                  :item-list="[modalSecondItem]"
                  id-name="id"
                  value-name="permitted"
                  checked-name="permitted"
                  :enabled-name="
                    typeof modalSecondItem.editable === 'undefined'
                      ? ''
                      : 'editable'
                  "
                  label-text-name="name"
                  :change-handler="twoDepthModalChange"
                  :change-handler-param="[
                    'subpermissions',
                    modalFirstIndex,
                    modalSecondIndex,
                  ]"
                  :category="modalFirstItem.resourceId"
                >
                  <slot>
                    <div style="cursor: pointer">
                      {{ modalSecondItem.name }}
                    </div>
                  </slot>
                </emdn-check-list>
              </li>
            </ul>
          </li>
        </ul>
      </template>

      <template #footer>
        <emdn-cta-button :click-handler="closeModal" color-type="blue-fill"
          >Back to Menu Permissions</emdn-cta-button
        >
      </template>
    </emdn-modal>

    <div class="body">
      <!-- Role 선택 사이드 바  -->
      <div class="role-container">
        <div class="container-title">Roles</div>
        <ul>
          <li
            v-for="(role, index) in roles"
            :key="index"
            @click="selectRole(role)"
            :class="currentRole === role ? 'active' : ''"
          >
            {{ role.name }}
          </li>
        </ul>
      </div>

      <!-- Menu Permissions -->
      <div class="menu-permission-container">
        <div class="title-container">
          <div style="display: flex; align-items: flex-end">
            <span class="container-title">Menu Permission</span>
            <div class="company-type-wrap">
              <span>Available for company type:</span>
              <div class="company-type-box">
                <div>OEM</div>
                <div>Supplier</div>
                <div>Toolmaker</div>
              </div>
            </div>
          </div>
          <emdn-search-bar
            placeholder-text="Search by menu or permission name"
            style-props="width: 426px;"
            :search-list="searchList"
            :focus-position="focusPosition"
            :set-search-complete-keyword="setSearchCompleteKeyword"
            :set-focus-position="setFocusPostion"
            :search-complete-keyword="searchCompleteKeyword"
            :is-focus-change="focusChangeWatcher"
          ></emdn-search-bar>
        </div>

        <div class="main-content">
          <div
            v-for="(firstItem, firstIndex) in permissionTree"
            :key="firstIndex"
          >
            <!-- first category -->
            <div class="first-category-container">
              <div>
                <emdn-check-list
                  :item-list="[firstItem]"
                  id-name="resourceId"
                  value-name="permitted"
                  checked-name="permitted"
                  enabled-name="editable"
                  label-text-name="name"
                  :change-handler="oneDepthChange"
                  :change-handler-param="[firstIndex]"
                >
                  <slot>
                    <div
                      style="cursor: pointer"
                      class="search-target"
                      :id="firstItem.resourceId"
                      :ref="firstItem.resourceId"
                      tabindex="0"
                      v-html="
                        getHasKeyword(firstItem.name, firstItem.resourceId)
                          ? firstItem.name
                          : setHighlight(firstItem.name, firstItem.resourceId)
                      "
                    ></div>
                    <div class="available-company-list">
                      <div
                        v-if="firstItem.resourceFields.oemEnabled"
                        class="oem"
                      ></div>
                      <div
                        v-if="firstItem.resourceFields.supplierEnabled"
                        class="supplier"
                      ></div>
                      <div
                        v-if="firstItem.resourceFields.toolmakerEnabled"
                        class="toolmaker"
                      ></div>
                    </div>
                  </slot>
                </emdn-check-list>
              </div>
              <emdn-cta-button
                v-if="conditionCheck(firstItem)"
                :click-handler="() => showModal(firstIndex)"
                button-type="text hyperlink"
                >Function Permissions</emdn-cta-button
              >
            </div>
            <!-- second category -->
            <div>
              <ul>
                <li
                  v-for="(secondItem, secondIndex) in firstItem.subpermissions"
                  :key="secondIndex"
                >
                  <div>
                    <emdn-check-list
                      :item-list="[secondItem]"
                      id-name="resourceId"
                      value-name="permitted"
                      checked-name="permitted"
                      enabled-name="editable"
                      label-text-name="name"
                      :change-handler="twoDepthChange"
                      :change-handler-param="[firstIndex, secondIndex]"
                    >
                      <slot>
                        <div
                          style="cursor: pointer"
                          class="search-target"
                          :id="secondItem.resourceId"
                          :ref="secondItem.resourceId"
                          tabindex="0"
                          v-html="
                            getHasKeyword(
                              secondItem.name,
                              secondItem.resourceId
                            )
                              ? secondItem.name
                              : setHighlight(
                                  secondItem.name,
                                  secondItem.resourceId
                                )
                          "
                        ></div>
                        <div class="available-company-list">
                          <div
                            v-if="secondItem.resourceFields.oemEnabled"
                            class="oem"
                          ></div>
                          <div
                            v-if="secondItem.resourceFields.supplierEnabled"
                            class="supplier"
                          ></div>
                          <div
                            v-if="secondItem.resourceFields.toolmakerEnabled"
                            class="toolmaker"
                          ></div>
                        </div>
                      </slot>
                    </emdn-check-list>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="footer">
      <emdn-cta-button
        style-props="margin: 0;"
        button-type="text"
        :click-handler="resetAlertToggleHandler"
        >Reset to default</emdn-cta-button
      >
      <emdn-cta-button
        :click-handler="savePermissionTree"
        color-type="green-fill"
        >Save Permissions</emdn-cta-button
      >
    </div>
    <emdn-alert-box
      v-if="isOpendResetAlert"
      :show-alert-box="isOpendResetAlert"
      title="Reset Menu Permissions to Default"
      :on-click-close="closeResetAlert"
      target-id="permission-reset-modal"
    >
      <template #messagebody>
        <span
          >Are you sure you want to reset all permissions for [{{
            currentRole.name
          }}] to default?</span
        >
      </template>
      <template #successbutton>
        <emdn-cta-button color-type="red-fill" :click-handler="resetRoles"
          >Yes. Reset to Default</emdn-cta-button
        >
      </template>
      <template #rejectbutton>
        <emdn-cta-button
          color-type="white"
          :click-handler="resetAlertToggleHandler"
          >Cancel</emdn-cta-button
        >
      </template>
    </emdn-alert-box>
  </div>
</template>

<script>
module.exports = {
  name: "PermissionConfigPage",
  props: {
    resources: Object,
  },
  mounted() {
    this.getRoles();
  },
  watch: {
    searchCompleteKeyword() {
      this.searchList = [];
    },
    searchList(newVal) {
      if (newVal.length > 0) {
        this.focusPosition = 1;
      }
      if (newVal.length === 0) {
        this.focusPosition = 0;
      }
    },
    focusPosition(newVal) {
      if (newVal >= 1 && this.$refs) {
        this.$refs[this.searchList[newVal - 1]][0].focus();
      }
    },
    currentRole() {
      this.getPermissionTree();
    },
    permissionTree(newVal) {
      console.log("permissionTree", newVal);
    },
  },
  data() {
    return {
      logTest: true,
      focusPosition: 0,
      searchList: [],
      searchKeyword: "",
      searchCompleteKeyword: "",
      modalData: [],
      modalFirstIndex: 0,
      isModalVisible: false,
      currentRole: "",
      roles: [],
      permissionTree: [],
      prevPermissionTree: [],
      focusChangeWatcher: 0,
      isOpendResetAlert: false,
    };
  },
  methods: {
    conditionCheck(firstItem) {
      let subPermissionItemCheck = false;

      if (firstItem.subpermissions) {
        firstItem.subpermissions.map((sub) => {
          if (sub.items) {
            subPermissionItemCheck = true;
          }
        });
      }
      return firstItem.permitted && (firstItem.items || subPermissionItemCheck);
    },
    focusHandler() {
      this.focusChangeWatcher += 1;
    },
    setSearchCompleteKeyword(keyword) {
      this.searchCompleteKeyword = keyword;
    },
    setFocusPostion(position) {
      this.focusPosition = position;
      this.focusHandler();
    },
    getHasKeyword(word, ref) {
      if (Common.SearchJS) {
        return Common.SearchJS.getHasKeywordJS(
          word,
          ref,
          this.searchCompleteKeyword,
          this.searchList,
          this.setSearchList
        );
      } else {
        return word;
      }
    },
    setSearchList(newSearchList) {
      this.searchList = newSearchList;
    },

    setHighlight(word, ref) {
      if (Common.SearchJS && word && ref) {
        return Common.SearchJS.setHighlightJS(
          word,
          ref,
          this.searchCompleteKeyword,
          this.searchList,
          this.focusPosition
        );
      } else {
        return word;
      }
    },
    // get PermissionTree
    getPermissionTree() {
      const self = this;
      axios
        .get(
          `/api/common/pms-stp/${self.currentRole.id}/permissionTree?resourceType=MENU`
        )
        .then(function (res) {
          this.logTest && console.log("permissionTree res: ", res.data.content);
          self.permissionTree = JSON.parse(JSON.stringify(res.data.content));
          self.prevPermissionTree = JSON.parse(
            JSON.stringify(res.data.content)
          );

          self.permissionTree = self?.permissionTree.map((item) => {
            if (item.subpermissions && !item.permitted) {
              return {
                ...item,
                subpermissions: item.subpermissions.map((subItem) => {
                  return {
                    ...subItem,
                    editable: false,
                  };
                }),
              };
            }
            return item;
          });
        });
    },
    // save PermissionTree
    savePermissionTree() {
      const self = this;

      let postItem = {
        content: this.permissionTree,
        resourceType: "MENU",
      };

      axios
        .post(
          `/api/common/pms-stp/${self.currentRole.id}/permissionTree/save`,
          postItem
        )
        .then(function (res) {
          this.logTest && console.log("permissionTree save: ", res);
          if (res.status === 200) {
            self.getPermissionTree();
            self.setToastAlertGlobal({
              title: "Success",
              content: `Permissions Saved`,
              show: true,
            });
          } else {
            self.setToastAlertGlobal({
              title: "Error",
              content: `Failed to Save Permissions`,
              show: true,
              status: "fail",
            });
          }
        })
        .catch(function (error) {
          this.logTest && console.log("permissionTree save error: ", error);
          self.setToastAlertGlobal({
            title: "Error",
            content: `Failed to Save Permissions`,
            show: true,
            status: "fail",
          });
        });
    },
    // get Roles
    getRoles() {
      const self = this;
      axios.get("/api/common/pms-stp").then(function (res) {
        self.roles = res.data.content;
        self.currentRole = self.roles[0];
      });
    },
    showModal(firstIndex) {
      this.logTest && console.log("showModal");

      // permetted false 일 경우 modal permission 안나오도록 처리 (product 팀 요청)

      let newPermission = JSON.parse(
        JSON.stringify(this.permissionTree[firstIndex])
      );

      if (newPermission.subpermissions) {
        newPermission.subpermissions.map((subpermission) => {
          this.logTest && console.log("subpermission: ", subpermission);
          // return subpermission.permitted === true;
          if (subpermission.permitted === true) {
            subpermission.visible = true;
          } else {
            subpermission.visible = false;
          }
        });
      }

      if (newPermission.items) {
        newItems = newPermission.items.map((item) => {
          this.logTest && console.log("item: ", item);
          if (item.permitted === true) {
            item.visible = true;
          } else {
            item.visible = false;
          }
        });
      }

      this.modalData = newPermission;

      this.modalFirstIndex = firstIndex;
      this.isModalVisible = true;
    },
    closeModal() {
      // permetted false 일 경우 modal permission 안나오도록 처리 (product 팀 요청)
      // visible 제거하기 (subPermissions.item visible, items.item visible)
      let newPermission = JSON.parse(JSON.stringify(this.modalData));

      if (newPermission.subpermissions) {
        newPermission.subpermissions.map((subpermission) => {
          this.logTest && console.log("subpermission: ", subpermission);
          delete subpermission.visible;
        });
      }

      if (newPermission.items) {
        newItems = newPermission.items.map((item) => {
          this.logTest && console.log("item: ", item);
          delete item.visible;
        });
      }

      this.logTest && console.log("newPermission: ", newPermission);

      this.permissionTree[this.modalFirstIndex] = newPermission;
      this.isModalVisible = false;
    },
    selectRole(item) {
      this.currentRole = item;
    },
    oneDepthModalChange(ev, firstIndex) {
      this.modalData.items[firstIndex].permitted = ev.target.checked;
    },
    twoDepthModalChange(ev, firstCategory, firstIndex, secondIndex) {
      this.modalData[firstCategory][firstIndex].items[secondIndex].permitted =
        ev.target.checked;
    },
    oneDepthChange(ev, firstIndex) {
      let oneDepth = this.permissionTree[firstIndex];
      let prevOneDepth = this.prevPermissionTree[firstIndex];
      oneDepth.permitted = ev.target.checked;

      let copiedSubpermissions = [];
      if (oneDepth.subpermissions) {
        copiedSubpermissions = JSON.parse(
          JSON.stringify(oneDepth.subpermissions)
        );
      }

      if (!ev.target.checked && oneDepth.subpermissions) {
        oneDepth.subpermissions = oneDepth.subpermissions.map((item) => {
          return {
            ...item,
            editable: false,
          };
        });
      } else {
        if (prevOneDepth.subpermissions) {
          oneDepth.subpermissions = JSON.parse(
            JSON.stringify(
              prevOneDepth.subpermissions.map((prevItem, index) => {
                if (
                  prevItem.permitted !== copiedSubpermissions[index].permitted
                ) {
                  return {
                    ...prevItem,
                    permitted: copiedSubpermissions[index].permitted,
                  };
                }
                return prevItem;
              })
            )
          );
        }
      }
    },
    twoDepthChange(ev, firstIndex, secondIndex) {
      this.permissionTree[firstIndex].subpermissions[secondIndex].permitted =
        ev.target.checked;
    },
    resetAlertToggleHandler() {
      this.isOpendResetAlert = !this.isOpendResetAlert;
    },
    resetRoles() {
      const self = this;
      axios
        .post(
          `/api/common/pms-stp/${self.currentRole.id}/permissionTree/reset`,
          {
            resourceType: "MENU",
          }
        )
        .then(function (res) {
          if (res.data.success) {
            self.getPermissionTree();
            self.setToastAlertGlobal({
              title: "Success",
              content: `Success to Reset Permissions`,
              show: true,
            });
          } else {
            self.setToastAlertGlobal({
              title: "Error",
              content: `Failed to Reset Permissions`,
              show: true,
              status: "fail",
            });
          }
        })
        .catch(function (error) {
          this.logTest && console.log("permissionTree reset error: ", error);
          self.setToastAlertGlobal({
            title: "Error",
            content: `Failed to Reset Permissions`,
            show: true,
            status: "fail",
          });
        });
      this.isOpendResetAlert = false;
    },
    closeResetAlert() {
      this.isOpendResetAlert = false;
    },
  },
};
</script>

<style>
.available-company-list {
  display: flex;
  margin-left: 2px;
}

.available-company-list > div {
  margin-left: 4px;
  width: 5px !important;
  height: 5px !important;
  border-radius: 50%;
}
.available-company-list .oem {
  background-color: #3491ff;
}
.available-company-list .supplier {
  background-color: #d78151;
}
.available-company-list .toolmaker {
  background-color: #9c51d7;
}

.company-type-wrap {
  color: #4b4b4b;
  margin-left: 32px;
}
.company-type-wrap > span {
  font-size: 9px;
}
.company-type-box {
  font-size: 11.25px;
  display: flex;
}
.company-type-box > div {
  display: flex;
  align-items: center;
  margin-right: 8px;
}
.company-type-box > div::before {
  content: "";
  display: block;
  width: 12.46px;
  height: 12.46px;
  border-radius: 50%;
  background-color: #000;
  margin-right: 4px;
}
.company-type-box > div:nth-of-type(1):before {
  background-color: #3491ff;
}
.company-type-box > div:nth-of-type(2):before {
  background-color: #d78151;
}
.company-type-box > div:nth-of-type(3):before {
  background-color: #9c51d7;
}

.search-target .highlight {
  background-color: #fff50e;
}

.search-target .highlight.focus {
  background-color: #ffe083;
}

.permission-config-container > .body {
  display: flex;
  height: 562px;
}

.permission-config-container .container-title {
  font-size: 14.66px;
  font-weight: 700;
}

.role-container {
  width: 238px;
  height: 100%;
  background-color: #fbfcfd;
  padding-left: 27px;
}

.role-container .container-title {
  margin-top: 12.6px;
  margin-bottom: 29.62px;
}

.role-container li {
  width: 100%;
  height: 31.6px;
  background-color: #fff;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  padding-left: 6px;
  font-size: 14.66px;
  cursor: pointer;
}

.role-container li:hover,
.role-container li.active {
  background-color: #e6f7ff;
}

.menu-permission-container {
  width: 100%;
  height: 100%;
  padding-left: 26px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.menu-permission-container .title-container {
  border-bottom: solid 0px #fff;
  margin-top: 4.6px;
  padding-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-permission-container .main-content {
  width: 100%;
  flex: 1;
  padding-left: 20px;
  overflow-y: scroll;
  border: solid 1px #f2f2f2;
  border-radius: 3px;
  max-height: 499px;
}

.menu-permission-container .main-content > div {
  display: flex;
  margin-top: 38px;
}

.menu-permission-container .main-content .first-category-container {
  display: flex;
  flex-direction: column;
  width: 250px;
}

.menu-permission-container .main-content .first-category-container {
  display: flex;
  flex-direction: column;
  width: 250px;
}

.permission-config-container > .footer {
  width: calc(100vw - 90px);
  height: 69px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  bottom: 46px;
  left: 90px;
  padding: 26px 37px;
}

.permission-config-container .checkbox-custom {
  cursor: pointer;
}

* {
  margin: 0;
  padding: 0;
  list-style: none;
}
html,
body,
.container {
  width: 100%;
  height: 100%;
}
</style>
