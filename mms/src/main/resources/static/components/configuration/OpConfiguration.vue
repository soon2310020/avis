<template>
  <div>
    <div class="config-area op-configuration">
      <div class="config-header">
        <div class="left-header">
          <h4
            v-text="resources['tooling_status']"
            :class="{
              'disable-text': !categoryStatus.enabled,
              'enable-text': categoryStatus.enabled,
            }"
          ></h4>
        </div>
        <!--      <div class="right-header">-->
        <!--        <div class="switch-status" :class="{'disable-text' : categoryStatus.enabled, 'enable-text' : !categoryStatus.enabled}">Off </div>-->
        <!--        <div class="switch-button">-->
        <!--          <div class="toggleWrapper">-->
        <!--            <input type="checkbox" v-model="categoryStatus.enabled" class="mobileToggle" id="op-toggle" value="1">-->
        <!--            <label class="change-checkout-status" @click="updateConfigStatus"></label>-->
        <!--          </div>-->
        <!--        </div>-->
        <!--        <div class="switch-status" :class="{'disable-text' : !categoryStatus.enabled, 'enable-text' : categoryStatus.enabled}"> On</div>-->
        <!--      </div>-->
      </div>
      <div
        class="config-body"
        :class="{
          'disable-text': !categoryStatus.enabled,
          'enable-text': categoryStatus.enabled,
        }"
      >
        <div class="op-item row odd-col">
          <div class="col-md-2 row">
            <div class="circle-icon active"></div>
            <div class="switch-title" v-text="resources['in_production']"></div>
          </div>
          <div class="col-md-4 row">
            <span v-text="resources['a_shot_made_within']"></span>
          </div>
          <div class="col-md-6 row">
            <!--        <span v-text="resources['shot_made_within']"></span>-->
            <input
              v-model="fields.WORKING.time"
              @keyup.enter="
                updateOpConfiguration(fields.WORKING, status.ACTIVE)
              "
              @blur="updateOpConfiguration(fields.WORKING, status.ACTIVE)"
              class="hour-input form-control"
              style="background-color: #ffffff"
              :readonly="!categoryStatus.enabled"
            />
            <div
              class="common-dropdown-class"
              :class="{ 'setting-zindex': showActive }"
            >
              <common-button
                :title="active1"
                @click="handleActiveToggle"
                :is-show="showActive"
              >
              </common-button>
              <common-select-popover
                :is-visible="showActive"
                @close="handlecloseActive"
                class="w-95"
              >
                <common-select-dropdown
                  id="showActive"
                  :searchbox="false"
                  :class="{ show: showActive }"
                  :items="days"
                  :checkbox="false"
                  class="dropdown"
                  :set-result="() => {}"
                  :click-handler="selectActive"
                ></common-select-dropdown>
              </common-select-popover>
            </div>
          </div>
        </div>

        <div class="op-item row">
          <div class="col-md-2 row">
            <div class="circle-icon idle"></div>
            <div class="switch-title" v-text="resources['idle']"></div>
          </div>
          <div class="col-md-4 row">
            <span v-text="resources['a_shot_made_after']"></span>
          </div>

          <div class="col-md-6 row">
            <!-- IDLE -->
            <input
              :value="fields.WORKING.time"
              class="hour-input form-control"
              :readonly="true"
              disabled
            />
            <div class="common-dropdown-class">
              <common-button
                :is-disabled="true"
                :is-show="false"
                :title="active1"
              >
              </common-button>
            </div>

            <!--  -->
            <span
              v-text="resources['but_within']"
              style="padding-left: 4px; align-self: center"
            ></span>
            <input
              v-model="fields.IDLE.time"
              @keyup.enter="updateOpConfiguration(fields.IDLE, status.IDLE)"
              @blur="updateOpConfiguration(fields.IDLE, status.IDLE)"
              class="hour-input form-control"
              style="background-color: #ffffff"
              :readonly="!categoryStatus.enabled"
            />
            <div
              class="common-dropdown-class"
              :class="{ 'setting-zindex': showIdle }"
            >
              <common-button
                :title="Idle"
                @click="handleIdleToggle"
                :is-show="showIdle"
              >
              </common-button>
              <common-select-popover
                :is-visible="showIdle"
                @close="handlecloseIdle"
                class="w-95"
              >
                <common-select-dropdown
                  id="showIdle"
                  :searchbox="false"
                  :class="{ show: showIdle }"
                  :set-result="() => {}"
                  :items="days"
                  :checkbox="false"
                  class="dropdown"
                  :click-handler="selectIdle"
                ></common-select-dropdown>
              </common-select-popover>
            </div>
          </div>
        </div>

        <div
          class="op-item row odd-col"
          :style="
            dialog === false
              ? '-webkit-mask-image: -webkit-gradient(linear, left 30%, left bottom, from(rgba(0, 0, 0, 1)), to(rgba(0, 0, 0, 0)))'
              : ''
          "
        >
          <div class="col-md-2 row">
            <div class="circle-icon inactive"></div>
            <div class="switch-title" v-text="resources['inactive']"></div>
          </div>
          <div class="col-md-4 row">
            <span v-text="resources['no_shot_made_within']"></span>
          </div>
          <div class="col-md-6 row">
            <input
              v-model="fields.NOT_WORKING.time"
              class="hour-input form-control"
              disabled
            />
            <div
              class="common-dropdown-class"
              :class="{ 'setting-zindex': showNotworking }"
            >
              <common-button
                :is-disabled="true"
                :title="Notworking"
                @click="handleNotworkingToggle"
                :is-show="showNotworking"
              >
              </common-button>
              <common-select-popover
                :is-visible="showNotworking"
                @close="handlecloseNotworking"
                class="w-95"
              >
                <common-select-dropdown
                  id="show-networking"
                  :searchbox="false"
                  :class="{ show: showNotworking }"
                  :items="days"
                  :checkbox="false"
                  class="dropdown"
                  :click-handler="selectNotworking"
                  :set-result="() => {}"
                ></common-select-dropdown>
              </common-select-popover>
            </div>
          </div>
        </div>

        <div class="op-item row" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon sensor-offline"></div>
            <div
              class="switch-title"
              v-text="resources['sensor_offline']"
            ></div>
          </div>
          <div class="col-md-4 row">
            <span v-text="resources['no_signal_from_sensor_within']"></span>
          </div>
          <div class="col-md-6 row">
            <input
              v-model="fields.DISCONNECTED.time"
              @keyup.enter="
                updateOpConfiguration(fields.DISCONNECTED, status.DISCONNECTED)
              "
              @blur="
                updateOpConfiguration(fields.DISCONNECTED, status.DISCONNECTED)
              "
              class="hour-input form-control"
              style="background-color: #ffffff"
              :readonly="!categoryStatus.enabled"
            />
            <div
              class="common-dropdown-class"
              :class="{ 'setting-zindex': showDisconnected }"
            >
              <common-button
                :title="Disconnected"
                @click="handleDisconnectedToggle"
                :is-show="showDisconnected"
              >
              </common-button>
              <common-select-popover
                :is-visible="showDisconnected"
                @close="handlecloseDisconnected"
                class="w-95"
              >
                <common-select-dropdown
                  id="show-disocnnected"
                  :searchbox="false"
                  :class="{ show: showDisconnected }"
                  disabled="true"
                  :items="days"
                  :checkbox="false"
                  class="dropdown"
                  :click-handler="selectDisconnected"
                  :set-result="() => {}"
                ></common-select-dropdown>
              </common-select-popover>
            </div>
          </div>
        </div>

        <div class="op-item row odd-col" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon sensor-detached"></div>
            <div
              class="switch-title"
              v-text="resources['sensor_detached']"
            ></div>
          </div>
          <div class="col-md-10 row">
            <span
              v-text="resources['sensor_has_become_physically_detached']"
            ></span>
          </div>
        </div>

        <div class="op-item row" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon disabled"></div>
            <div class="switch-title" v-text="resources['disabled']"></div>
          </div>
          <div class="col-md-10 row">
            <span
              v-text="resources['sensor_has_been_manually_disabled']"
            ></span>
          </div>
        </div>

        <div class="op-item row odd-col" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon disposed"></div>
            <div class="switch-title" v-text="resources['disposed']"></div>
          </div>
          <div class="col-md-10 row">
            <span
              v-text="
                resources['tooling_has_been_through_the_disposal_process']
              "
            ></span>
          </div>
        </div>

        <div class="op-item row" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon no-sensor"></div>
            <div class="switch-title" v-text="resources['no_sensor']"></div>
          </div>
          <div class="col-md-10 row">
            <span v-text="resources['tooling_has_no_matched']"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="config-area op-configuration" v-if="dialog === true">
      <div class="config-header">
        <div class="left-header">
          <h4
            v-text="resources['sensor_status']"
            :class="{
              'disable-text': !categoryStatus.enabled,
              'enable-text': categoryStatus.enabled,
            }"
          ></h4>
        </div>
      </div>
      <div
        class="config-body"
        :class="{
          'disable-text': !categoryStatus.enabled,
          'enable-text': categoryStatus.enabled,
        }"
      >
        <div class="op-item row">
          <div class="col-md-2">
            <div
              class="counter-status-title counter-install-label"
              v-text="resources['installed']"
            ></div>
          </div>
          <div class="col-md-10">
            <span v-text="resources['sensor_matched_tooling']"></span>
          </div>
        </div>

        <div class="op-item odd-col row">
          <div class="col-md-2">
            <div
              class="counter-status-title counter-not-install-label"
              v-text="resources['not_installed']"
            ></div>
          </div>
          <div class="col-md-10">
            <span v-text="resources['sensor_not_matched_tooling']"></span>
          </div>
        </div>

        <div class="op-item row">
          <div class="col-md-2">
            <div
              class="counter-status-title counter-detached-label"
              v-text="resources['detached']"
            ></div>
          </div>
          <div class="col-md-10">
            <span v-text="resources['sensor_detached_tooling']"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="config-area op-configuration">
      <div class="config-header" v-if="dialog === true">
        <div class="left-header">
          <h4
            v-text="resources['terminal_status']"
            :class="{
              'disable-text': !categoryStatus.enabled,
              'enable-text': categoryStatus.enabled,
            }"
          ></h4>
        </div>
      </div>
      <div
        class="config-body"
        :class="{
          'disable-text': !categoryStatus.enabled,
          'enable-text': categoryStatus.enabled,
        }"
      >
        <div class="op-item row" v-if="dialog === true">
          <div class="col-md-2 row">
            <div class="circle-icon sensor-offline"></div>
            <div class="switch-title" v-text="resources['disconnected']"></div>
          </div>
          <div class="col-md-4 row">
            <span v-text="resources['no_signal_from_terminal_within']"></span>
          </div>
          <div class="col-md-6 row">
            <input
              v-model="fields.TERMINAL_DISCONNECTED.time"
              @keyup.enter="
                updateOpConfiguration(
                  fields.TERMINAL_DISCONNECTED,
                  status.DISCONNECTED
                )
              "
              @blur="
                updateOpConfiguration(
                  fields.TERMINAL_DISCONNECTED,
                  status.DISCONNECTED
                )
              "
              class="hour-input form-control"
              style="background-color: #ffffff"
              :readonly="!categoryStatus.enabled"
            />
            <div
              class="common-dropdown-class"
              :class="{ 'setting-zindex': showTDisconnected }"
            >
              <common-button
                :title="TDisconnected"
                :is-show="showTDisconnected"
                @click="handleTDisconnectedToggle"
              >
              </common-button>
              <common-select-popover
                :is-visible="showTDisconnected"
                @close="handlecloseTDisconnected"
                class="w-95"
              >
                <common-select-dropdown
                  :searchbox="false"
                  :class="{ show: showTDisconnected }"
                  disabled="true"
                  :items="days"
                  :checkbox="false"
                  class="dropdown"
                  :click-handler="selectTDisconnected"
                  :set-result="() => {}"
                ></common-select-dropdown>
              </common-select-popover>
            </div>
          </div>
        </div>
        <!--        <div class="d-flex justify-content-center mt-3" style="cursor: pointer" @click="changeSectionDialog()" :class="{'disable-click' : !categoryStatus.enabled}">-->
        <!--          <div v-show="categoryStatus.enabled" class="mr-1">-->
        <!--            <img v-if="dialog === false" src="/images/icon/arrow-of-double-angle-pointing-down.svg" height="11">-->
        <!--            <img v-else src="/images/icon/arrow-of-double-angle-pointing-up.svg" height="11">-->
        <!--          </div>-->
        <!--          <div v-show="!categoryStatus.enabled" class="mr-1">-->
        <!--            <img v-if="dialog === false" src="/images/icon/arrow-of-double-angle-pointing-down-disable.svg" height="11">-->
        <!--            <img v-else style="transform: rotate(180deg);" src="/images/icon/arrow-of-double-angle-pointing-down-disable.svg" height="11">-->
        <!--          </div>-->
        <!--          <span class="dialogTooling" v-if="dialog === false" v-text="resources['show_more']"></span>-->
        <!--          <span class="dialogTooling" v-else v-text="resources['show_less']"></span>-->
        <!--        </div>-->
      </div>
    </div>
  </div>
</template>

<script>
const mappingTimeUnit = {
  WORKING: "active1",
  IDLE: "Idle",
  NOT_WORKING: "Notworking",
  DISCONNECTED: "Disconnected",
  TERMINAL_DISCONNECTED: "TDisconnected",
};

module.exports = {
  name: "OpConfiguration",
  props: {
    resources: Object,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    handleIsChange: Function,
  },
  data() {
    return {
      dialog: true,
      configCategory: "OP",
      status: {
        ACTIVE: "ACTIVE",
        IDLE: "IDLE",
        INACTIVE: "INACTIVE",
        DISCONNECTED: "DISCONNECTED",
      },
      timeOption: {
        DAYS: "DAYS",
        HOURS: "HOURS",
      },
      fields: {
        WORKING: {
          time: 3,
          timeLastValue: 3,
          timeUnit: "DAYS",
          timeUnitLast: "DAYS",
          equipmentType: "MOLD",
        },
        IDLE: {
          time: 10,
          timeLastValue: 10,
          timeUnit: "DAYS",
          timeUnitLast: "DAYS",
          equipmentType: "MOLD",
        },
        NOT_WORKING: {
          time: 10,
          timeLastValue: 10,
          timeUnit: "DAYS",
          timeUnitLast: "DAYS",
          equipmentType: "MOLD",
        },
        DISCONNECTED: {
          time: 6,
          timeLastValue: 6,
          timeUnit: "HOURS",
          timeUnitLast: "HOURS",
          equipmentType: "MOLD",
        },
        TERMINAL_DISCONNECTED: {
          time: 1,
          timeLastValue: 1,
          timeUnit: "HOURS",
          timeUnitLast: "HOURS",
          equipmentType: "TERMINAL",
        },
      },
      active1: "day(s)",
      showActive: false,
      Idle: "day(s)",
      showIdle: false,
      Notworking: "day(s)",
      showNotworking: false,
      Disconnected: "day(s)",
      showDisconnected: false,
      TDisconnected: "day(s)",
      showTDisconnected: false,
      days: [
        { title: "day(s)", code: "DAYS" },
        { title: "hour(s)", code: "HOURS" },
      ],
    };
  },
  methods: {
    selectActive(item) {
      this.handlecloseActive();
      this.$emit("handlecloseActive");
      console.log("showActive");
      this.active1 = item.title;
      this.fields.WORKING.timeUnit = item.code;
      this.updateOpConfiguration(this.fields.WORKING, this.status.ACTIVE);
    },
    handlecloseActive: function () {
      this.showActive = false;
    },
    handleActiveToggle: function (isShow) {
      this.showActive = isShow;
    },

    selectIdle(item) {
      this.handlecloseIdle();
      this.Idle = item.title;
      this.fields.IDLE.timeUnit = item.code;
      this.updateOpConfiguration(this.fields.IDLE, this.status.IDLE);
    },

    handlecloseIdle: function () {
      this.showIdle = false;
    },
    handleIdleToggle: function (isShow) {
      this.showIdle = isShow;
    },

    selectNotworking(item) {
      this.handlecloseNotworking();
      this.Notworking = item.title;
      this.fields.NOT_WORKING.timeUnit = item.code;
      this.updateOpConfiguration(this.fields.NOT_WORKING, this.status.INACTIVE);
    },

    handlecloseNotworking: function () {
      this.showNotworking = false;
    },
    handleNotworkingToggle: function (isShow) {
      this.showNotworking = isShow;
    },

    selectDisconnected(item) {
      this.showDisconnected = false;
      console.log(this.showDisconnected);
      this.Disconnected = item.title;
      this.fields.DISCONNECTED.timeUnit = item.code;
      this.updateOpConfiguration(
        this.fields.DISCONNECTED,
        this.status.DISCONNECTED
      );
    },
    handleshowDisconnected: function () {
      this.showDisconnected = true;
    },
    handlecloseDisconnected: function () {
      this.showDisconnected = false;
    },
    handleDisconnectedToggle: function (isShow) {
      this.showDisconnected = isShow;
    },

    selectTDisconnected(item) {
      this.showTDisconnected = false;
      console.log(this.showTDisconnected);
      this.TDisconnected = item.title;
      this.fields.DISCONNECTED.timeUnit = item.code;
      this.updateOpConfiguration(
        this.fields.TERMINAL_DISCONNECTED,
        this.status.DISCONNECTED
      );
    },

    handlecloseTDisconnected: function () {
      this.showTDisconnected = false;
    },
    handleTDisconnectedToggle: function (isShow) {
      this.showTDisconnected = isShow;
    },
    cancelData() {
      this.getCurrentConfig();
      this.setupOperatingStatus();
    },
    saveData() {
      this.updateCategoryConfigStatus(
        this.configCategory,
        this.categoryStatus.enabled
      );
      for (let field in this.fields) {
        this.$emit("update-config", this.fields[field]);
      }
    },
    changeSectionDialog() {
      this.dialog = !this.dialog;
    },
    cancelInputData() {
      this.getCurrentConfig();
    },
    getCurrentConfig() {
      Common.getCurrentOpConfig().then((data) => {
        if (data) {
          data.forEach((item) => {
            const objectKey =
              item.equipmentType === "TERMINAL"
                ? `${item.equipmentType}_${item.operatingStatus}`
                : item.operatingStatus;
            this.$set(this.fields, objectKey, {
              id: item.id,
              time: item.time,
              timeLastValue: item.time,
              timeUnit: item.timeUnit,
              timeUnitLast: item.timeUnit,
              equipmentType: item.equipmentType,
              operatingStatus: item.operatingStatus,
            });
            this[mappingTimeUnit[objectKey]] =
              item.timeUnit === "HOURS" ? "hour(s)" : "day(s)";
          });
        }
      });
    },
    setupOperatingStatus() {
      Object.keys(this.fields).forEach((field) => {
        if (!field.startsWith("TERMINAL_"))
          this.fields[field].operatingStatus = field;
        else
          this.fields[field].operatingStatus = field.replace("TERMINAL_", "");
      });
      console.log("@setupOperatingStatus:this.fields", this.fields);
    },
    updateOpConfiguration(field, inputType) {
      //valid
      if (field) {
        if (isNaN(+field.time)) {
          Common.alert(this.resources["configuration.inputMustBeNumber"]);
          field.time = field.timeLastValue;
          field.timeUnit = field.timeUnitLast;
          return;
        }

        let ac = 0;
        let id = 0;
        let inac = 0;
        let ds = 0;
        let terminal_ds = 0;
        if (this.fields.WORKING && this.fields.WORKING.time) {
          ac =
            this.fields.WORKING.timeUnit === "DAYS"
              ? this.fields.WORKING.time * 24
              : Number(this.fields.WORKING.time);
        }
        if (this.fields.IDLE && this.fields.IDLE.time) {
          id =
            this.fields.IDLE.timeUnit === "DAYS"
              ? this.fields.IDLE.time * 24
              : Number(this.fields.IDLE.time);
        }

        if (this.fields.DISCONNECTED && this.fields.DISCONNECTED.time) {
          ds =
            this.fields.DISCONNECTED.timeUnit === "DAYS"
              ? this.fields.DISCONNECTED.time * 24
              : Number(this.fields.DISCONNECTED.time);
        }
        if (this.fields.NOT_WORKING && this.fields.NOT_WORKING.time) {
          inac =
            this.fields.NOT_WORKING.timeUnit === "DAYS"
              ? this.fields.NOT_WORKING.time * 24
              : Number(this.fields.NOT_WORKING.time);
        }
        if (
          this.fields.TERMINAL_DISCONNECTED &&
          this.fields.TERMINAL_DISCONNECTED.time
        ) {
          terminal_ds =
            this.fields.TERMINAL_DISCONNECTED.timeUnit === "DAYS"
              ? this.fields.TERMINAL_DISCONNECTED.time * 24
              : Number(this.fields.TERMINAL_DISCONNECTED.time);
        }

        let smallestUnit = 1;
        let smallest = smallestUnit;

        if (field.timeUnit === this.timeOption.DAYS) {
          // smallestUnit = 0.04167;
          smallestUnit = 1 / 27;
          smallest = smallestUnit * 24;
        }
        let errorMessage = null;

        console.log("inputType", inputType);
        console.log("ac", ac);
        console.log("id", id);
        console.log("ds", ds);
        console.log("smallestUnit", smallest);

        switch (inputType) {
          case this.status.ACTIVE:
            if (ac >= id) {
              errorMessage =
                this.resources["configuration.op.ac.validValue.smaller"] +
                " " +
                this.getValueUnit(
                  this.fields.IDLE.time,
                  this.fields.IDLE.timeUnit
                );
            } else if (ac < smallest) {
              errorMessage =
                this.resources["configuration.op.ac.validValue.greater"] +
                " " +
                this.getValueUnit(smallestUnit, field.timeUnit);
            }
            break;
          case this.status.IDLE:
            if (ac >= id) {
              errorMessage =
                this.resources["configuration.op.id.validValue"] +
                " " +
                this.getValueUnit(
                  this.fields.WORKING.time,
                  this.fields.WORKING.timeUnit
                );
            }
            break;
          case this.status.INACTIVE:
            break;
          case this.status.DISCONNECTED:
            if (ds < 1 || terminal_ds < 1) {
              errorMessage = this.resources["invalid_value"];
            }
            break;
        }
        console.log(errorMessage);
        if (errorMessage) {
          Common.alert(errorMessage);
          $("button").on("click", () => {
            field.time = field.timeLastValue;
            field.timeUnit = field.timeUnitLast;
            const objectKey =
              field.equipmentType === "TERMINAL"
                ? `${field.equipmentType}_${field.operatingStatus}`
                : field.operatingStatus;
            this[mappingTimeUnit[objectKey]] =
              field.timeUnitLast === this.timeOption.HOURS
                ? "hour(s)"
                : "day(s)";
          });
          return;
        } else {
          field.timeLastValue = field.time;
          field.timeUnitLast = field.timeUnit;
        }
        console.log(
          `field.operatingStatus===this.status.IDLE`,
          field.operatingStatus === this.status.IDLE
        );
        if (field.operatingStatus === this.status.IDLE) {
          this.fields.NOT_WORKING.time = field.time;
          this.fields.NOT_WORKING.timeUnit = field.timeUnit;
          this.fields.NOT_WORKING.timeLastValue = field.time;
          this.fields.NOT_WORKING.timeUnitLast = field.timeUnit;
          this.fields.NOT_WORKING.equipmentType = field.equipmentType;
        }
      }
      console.log("Field ", field);
      this.handleIsChange();
    },
    updateConfigStatus() {
      console.log(
        "this.configCategory",
        this.configCategory,
        this.categoryStatus.enabled
      );
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.handleIsChange();
      console.log("change");
    },
    getValueUnit(num, unit) {
      return num + (unit === "DAYS" ? " day(s)" : " hour(s)");
    },
  },
  mounted() {
    this.getCurrentConfig();
    this.setupOperatingStatus();
  },
  watch: {
    fields(newVal) {
      console.log("newVal", newVal);
    },
  },
};
</script>

<style scoped>
.dialogTooling:hover {
  color: blue;
}

.cursor {
  cursor: pointer;
}

.toggleWrapper input.mobileToggle:checked + .change-checkout-status:before {
  background: #3585e5;
  -webkit-transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
  transition: width 0.2s cubic-bezier(0, 0, 0, 0.1);
}

.hour-input-bc {
  background-color: #ffffff;
}
.w-6 {
  width: 6rem;
}
.ant-btn {
  height: 34px !important;
}
.btn-custom {
  /*width: 100% !important;*/
  text-align: left !important;
  position: relative;
}
.common-popover {
  top: 34px !important;
  left: 1px !important;
  width: calc(100% - 2px) !important;
  min-width: unset !important;
  position: static !important;
  padding: 2px !important;
  border-radius: 3px !important;
  /*position: unset !important;*/
}
.dropdown-wrap {
  top: 0px !important;
  left: 0px !important;
  position: absolute;
  padding: 2px !important;
  border-radius: 3px !important;
}
.custom-dropdown-button {
  width: 100px !important;
  height: 34px !important;
}
.common-dropdown-class {
  position: inherit;
}
.setting-zindex {
  z-index: 10;
}
.dropdown {
  /*width: 248% !important;*/
  width: 100% !important;
}

.btn-custom-primary {
  border: 1px solid rgba(22, 23, 24, 0.13) !important;

  color: #b2a9a9 !important;
  background: #ffffff !important;
  background-color: #ffffff !important;
}
.img-transition {
  float: right;
  margin-top: 8px;
}
.btn-custom-primary:hover {
  /*border:2px solid rgba(255, 255, 255, 0.31) !important;*/
  background-color: #ffffff !important;
  color: #b2a9a9 !important;
  outline: none !important;
}

.btn-custom-primary:focus {
  border: 2px solid rgba(17, 237, 245, 0.81) !important;
  /*background-color: transparent !important;*/
}
.circle-icon {
  width: 13px;
  height: 13px;
  border-radius: 50%;
}
.circle-icon.active {
  background-color: #1aa955;
}
.circle-icon.idle {
  background: #fec104;
}
.circle-icon.inactive {
  background: #b0b0b0;
}

.circle-icon.sensor-offline {
  background-color: #e34537;
}
.circle-icon.sensor-detached {
  border: 1px dashed #e34537;
}
.circle-icon.disabled {
  background: #535453;
}
.circle-icon.disposed {
  background: #000000;
}
.circle-icon.no-sensor {
  border: 1px solid #4b4b4b;
}
.op-item .circle-icon {
  margin-right: 6px;
  align-self: center;
}
.op-item .switch-title {
  font-size: 14px;
}
.config-body .op-item {
  padding-left: 15px;
  height: 45px;
  margin-top: 0px;
}
.counter-status-title {
  border-radius: 25px;
  width: 100px;
  text-align: center;
  height: 25px;
}

.op-item a,
.op-item input {
  height: 35px;
}

.counter-install-label {
  background-color: #d2f8e2;
}
.counter-not-install-label {
  background-color: #d6dade;
}

.counter-detached-label {
  background-color: #ff8489;
}
.odd-col {
  background-color: #f2f2f2;
}
.config-area.op-configuration {
  margin-bottom: 0px;
}
</style>
