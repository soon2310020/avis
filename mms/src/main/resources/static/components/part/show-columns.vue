<template>
  <div
    class="part-filter"
    v-click-outside-except-calendar="hidePopupFromOutside"
  >
    <a-button @click="togglePopup" class="ant-dropdown-trigger1" :key="columnSelected().length">
      <img
        height="18"
        src="/images/icon/column-setting.svg"
        alt="column-setting"
        :class="{ 'card-shadow': displayShadowColumn }"
      />
      <span
        v-if="columnSelected().length === 1"
        style="
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          padding: 3px 5px;
        "
        >1 <span v-text="resources['column_selected']"></span></span
      >
      <span
        v-if="columnSelected().length > 1"
        style="
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          padding: 3px 5px;
        "
        >{{ `${columnSelected().length} ` + resources['columns_selected']}}</span
      >
      <svg class="down-icon" width="16" height="16" viewBox="0 0 24 24">
        <path
          d="M16.293 9.293L12 13.586L7.707 9.293l-1.414 1.414L12 16.414l5.707-5.707z"
        />
      </svg>
    </a-button>
    <div class="dropdown-scroll" v-show="isShowSelectColumnsList">
      <span style="margin-left: 15px; margin-bottom: 10px; font-weight: 600">{{
        `${columnSelected().length} ` + resources['out_of'] + ` ${allColumnsList.filter(c => !c.hiddenInToggle).length} ` + resources['selected']
      }}</span>
      <div class="label-group">
        <label
          v-for="(column, index) in allColumnsList.filter(c => !c.hiddenInToggle)"
          v-bind:key="index"
          style="padding-left: 10px; font-weight: 600"
          class="radio-confirm-modal"
          :class="{disabled: column.mandatory}"
        >
          <input
            style="margin-right: 5px; margin-top: 3px"
            type="checkbox"
            :name="column.label"
            :value="column.field"
            autocomplete="off"
            :checked="column.enabled"
            @change="handleChangeCheckBox"
            :disabled="column.mandatory"
          />
          <span :for="column.field" class="column-name">{{ column.label }}</span>
        </label>
      </div>
      <div class="btn-reset-list" @click="resetColumnList">
        <span v-text="resources['default']"></span>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "show-columns",
  props: {
    resources: Object,
    allColumnsList: {
      type: Array,
      default: () => [],
    }
  },
  data() {
    return {
      isShowSelectColumnsList: false,
      displayShadowColumn: false,
    };
  },
  mounted() {},
  computed: {},
  watch: {},
  methods: {
    togglePopup() {
      this.isShowSelectColumnsList = true;
      this.displayShadowColumn = true;
    },
    hidePopup() {
      this.isShowSelectColumnsList = false;
      this.displayShadowColumn = false;
    },
    columnSelected() {
      return this.allColumnsList.filter(item => item.enabled && !item.hiddenInToggle).map(item => item.label);
    },

    hidePopupFromOutside() {
      this.hidePopup();
    },
    resetColumnList(){
      this.$emit('reset-columns-list');
      this.$forceUpdate();
    },
    handleChangeCheckBox(e) {
      let value = e.target.value;
      console.log(value, "value");
      this.$emit("change-value-checkbox", value);
      this.$forceUpdate();
    },
  },
};
</script>

<style scoped>
.ant-popover {
  position: relative;
  padding-top: 0;
}

.column-name:hover {
  font-weight: 700;
}
.radio-confirm-modal.disabled .column-name {
  color: #AAA;
  font-weight: 400;
}
.radio-confirm-modal.disabled .column-name:hover {
  font-weight: 400;
}

.card-shadow {
  box-shadow: 0 3px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24);
}

.label-group {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  margin-top: 10px;
}

.btn-reset-list {
  font-weight: 600;
  text-align: end;
  margin-right: 10px;
  cursor: pointer;
}

.btn-reset-list:hover {
  color: #1890ff;
}

.ant-dropdown-trigger1 {
  border: none;
  padding: 5px 0px !important;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-right: 0;
}

.ant-dropdown-trigger1:last-child {
  border-radius: 0px;
  border: none;
  padding: 5px 0px !important;
  margin-right: 0px;
  border-radius: 7px;
  display: flex;
  /* padding: 10px !important; */
  border: none;
  align-items: center;
  justify-content: space-between;
}

button {
  border: 1px solid #c8ced3;
  padding: 5px 10px !important;
  border-radius: 0;
}

.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  border-radius: 0;
}

.ant-btn:focus,
.ant-btn:hover {
  color: #000;
}

.between-option {
  padding: 0 10px;
}

.divider-line {
  margin-top: 30px;
  height: 2px;
  width: 15px;
  background: #c8ced3;
}

.dropdown-scroll {
  padding: 10px;
  width: 600px;
  max-height: 300px;
  overflow-y: scroll;
  position: absolute;
  background: #fff;
  right: -7px;
  border: 1px solid #ccc;
  border-radius: 5px;
  top: 40px !important;
  z-index: 100 !important;
}

.all-time-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.all-time-option.active input {
  border: 1px solid #684df8;
}

.option-item {
  border: 1px solid #c8ced3;
  text-align: center;
  margin-top: 10px;
  padding: 3px;
  border-radius: 5px;
  cursor: pointer;
}

.option-item.active {
  color: #fff;
  background: #684df8;
}

.calendar-prev-btn:after {
  content: "\AB";
}

.calendar-next-btn:after {
  content: "\BB";
}

.time-picker {
  padding: 10px;
  overflow-y: scroll;
  position: absolute;
  background: #fff;
  right: -7px;
  border: 1px solid #ccc;
  border-radius: 5px;
  top: 40px !important;
  z-index: 100 !important;
}

.header-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.body-option {
  padding-top: 7px;
  display: grid;
  grid-template-columns: auto auto auto auto;
}

.time-option-item {
  padding: 2px;
  text-align: center;
}

.time-option-item .option-item-content {
  border-radius: 5px;
  color: #777;
  cursor: not-allowed;
}

.time-option-item.can-choosing .option-item-content {
  color: #000;
  cursor: pointer;
}

.time-option-item.can-choosing.selected .option-item-content {
  background: #684df8;
  color: #fff;
}

.time-option-item.can-choosing .option-item-content:hover {
  background: #684df8;
  color: #fff;
}

.time-option-item.can-choosing .option-item-content.active {
  background: #684df8;
  color: #fff;
}

.monthly-option .time-option-item .option-item-content {
  padding: 20px 10px;
}

.yearly-option .time-option-item .option-item-content {
  padding: 20px 5px;
}

.weekly-option .body-option {
  grid-template-columns: auto auto auto auto auto auto auto auto auto;
}

.weekly-option .body-option .option-item-content {
  font-weight: normal;
}

.weekly-option .time-option-item .option-item-content {
  border-radius: 0;
  padding: 3px;
}
</style>
