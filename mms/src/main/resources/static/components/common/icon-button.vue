

<template>
  <div>
    <a-tooltip color="white">
      <div slot="title">
        <div
          style="
            padding: 6px 8px;
            font-size: 13px;
            color: #4b4b4b;
            background: #fff;
            border-radius: 4px;
            height: 32px;
            box-shadow: 0px 0px 4px 1px #e5dfdf;
          "
        >
          {{ tooltipText }}
          <div class="custom-arrow-tooltip"></div>
        </div>
      </div>
      <div
        @click="executor"
        :class="buttonClassComputed"
        :style="styleProps"
        :active="active"
        :disabled="disabled"
      >
        <div class="icon-size">
          <img v-if="buttonType === 'custom-button'" :src="iconUrl" />
          <img v-else src="" alt="" />
        </div>
      </div>
    </a-tooltip>
  </div>
</template>


<script>
module.exports = {
  props: {
    styleProps: {
      default: "",
      Type: String,
    },
    clickHandler: Function,
    active: Boolean,
    disabled: Boolean,
    // '', 'filter', 'kebab-menu', 'custom-colum',
    // 'edit', 'trash', 'check', 'clone', 'add' ,'remove', 'previous'
    buttonType: {
      type: String,
      default: "",
    },
    iconUrl: {
      type: String,
      default: "",
    },
  },

  data() {
    return {
      tooltipText: "Filter",
    };
  },
  watch: {
    buttonType(value) {
      if (
        value === "filter" || value === "custom-colum" ||
        value === "kebab-menu" ||
        value ==="edit" ||
        value === "trash" ||
        value === "check" ||
        value === "clone"
      ) {
        if (value === "filter") {
          this.tooltipText = "Filter";
        } else if (value === "custom-colum") {
          this.tooltipText = "Customize Columns";
        } else if (value === "kebab-menu") {
          this.tooltipText = "More";
        } else if (value === "edit") {
          this.tooltipText = "Edit";
        } else if (value === "trash") {
          this.tooltipText = "Delete";
        } else if (value === "check") {
          this.tooltipText = "Confirm";
        } else if (value === "clone") {
          this.tooltipText = "Duplicate";
        }
        document.getElementsByClassName('ant-tooltip-placement-top')[0].parentElement.classList.remove('hideTooltip')
      }if (value === "left-arrow" || value === "previous" || value === "custom-button" || value === "remove" || value === "add") {
        document.getElementsByClassName('ant-tooltip-placement-top')[0].parentElement.classList.add('hideTooltip');
}
    },
  },

  computed: {
    buttonClassComputed() {
      let item = `default ${this.buttonType} ${this.classTypeForButton}`;

      return item;
    },
    classTypeForButton() {
      let type = "";
      if (
        this.buttonType === "filter" ||
        this.buttonType === "custom-colum" ||
        this.buttonType === "kebab-menu"
      ) {
        type = "customizing-data-table";
      } else if (
        this.buttonType === "edit" ||
        this.buttonType === "trash" ||
        this.buttonType === "check" ||
        this.buttonType === "clone"
      ) {
        type = "row-inline-action-buttons";
      } else if (this.buttonType === "previous") {
        type = "next-previous-buttons";
      } else if (this.buttonType === "add") {
        type = "plus-button";
      } else if (this.buttonType === "remove") {
        type = "minus-button";
      }

      return type;
    },
  },
  methods: {
    executor() {
      if (this.clickHandler) {
        this.clickHandler();
      }
    },
  },
};
</script>


<style>
.hideTooltip {
  display: none !important;
}
.custom-arrow-tooltip {
  background: #fff;
  left: 51%;
  bottom: -2px;
  margin-left: -5px;
  position: absolute;
  transform: rotate(45deg);
  width: 8px;
  height: 8px;
  box-shadow: 1px 1px 1px rgb(229 223 223 / 50%),
    1px 1px 1px rgb(229 223 223 / 50%);
}

.ant-tooltip-inner {
  padding: 0 !important;
  position: relative;
}
</style>

<style scoped>
/* disabled style start =================================== */
[disabled] {
  background-color: rgb(196, 196, 196);
  color: white;
  border: inherit;
  pointer-events: none;
  cursor: not-allowed !important;
}

/* disabled style end =================================== */

/* dropdown style start ===================================== */
.default {
  padding: 4px 6px;
  border-radius: 3px;
  width: fit-content;
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-right: 5px;
  margin: 2px;
  position: relative;
  cursor: pointer;
  transition: 0.3s;
}
.customizing-data-table {
  background: #f0f1f3;
  border-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  align-content: center;
  border-radius: 3px;
  width: 29px;
  height: 29px;
}
.customizing-data-table:hover {
  background: #daeeff;
  outline: 0.25px solid #3585e5 !important;
}
.customizing-data-table[active] {
  background: #daeeff;
  outline: 2px solid #3585e5 !important;
}

.filter {
  content: url(/images/icon/Icon_feather-filter.svg);
}
.filter > .icon-size {
  width: 13.46px;
  height: 12.11px;
}

.custom-colum {
  content: url(/images/icon/customization-icon.svg);
}
.kebab-menu {
  content: url(/images/icon/icon-kebab-menu.svg);
  padding:7px 6px;
}

.edit {
  content: url(/images/icon/edit-icon-defualt.svg);
}
.edit:hover {
  content: url(/images/icon/edit-icon-blue.svg);
}
.trash {
  content: url(/images/icon/trash-icon-defualt.svg);
}
.trash:hover {
  content: url(/images/icon/trash-icon-blue.svg);
}

.clone {
  content: url(/images/icon/clone-icon-defualt.svg);
}
.clone:hover {
  content: url(/images/icon/clone-icon-blue.svg);
}
.check {
  content: url(/images/icon/check-icon-defualt.svg);
}
.check[disabled] {
  content: url(/images/icon/check-icon-disabled.svg);
  border: transparent !important;
  background: transparent;
}
.check:hover {
  content: url(/images/icon/check-icon-blue.svg);
  background: transparent;
}

.previous {
  content: url(/images/icon/left-arrow-icon-defualt.svg);
}
.left-arrow {
  content: url(/images/icon/left-arrow-graph-icon-light-blue.svg);
  padding: unset;
  background: transparent;
  border: none;
  width: 18px;
  height: 17px;
}
.left-arrow:hover {
  content: url(/images/icon/left-arrow-graph-icon-blue.svg);
}
.left-arrow[disabled] {
  content: url(/images/icon/left-arrow-graph-icon-disabled.svg);
}
.add {
  content: url(/images/icon/plus-icon-defualt.svg);
}
.remove {
  content: url(/images/icon/minus-icon-blue.svg);
}

.row-inline-action-buttons {
  padding: 0px 6px;
  background: #d9d9d9;
  border-color: transparent;
  border-radius: 3px;
  width: 25px;
  height: 25px;
}
.row-inline-action-buttons:hover {
  background: #daeeff;
  box-shadow: 0px 1px 1px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
}

.next-previous-buttons {
  background: #3491ff;
  border-radius: 3px;
  width: 22px;
  height: 22px;
}
.next-previous-buttons:hover {
  background: #3585e5;
  border-radius: 3px;
}
.next-previous-buttons[active] {
  background: #3585e5;
  border: 2px solid #deedff;
  border-radius: 3px;
}
.next-previous-buttons[disabled] {
  border: transparent;
  background: #c4c4c4 !important;
  border-radius: 3px;
}

.plus-button {
  padding-left: unset;
  padding-right: unset;
  padding-bottom: unset;
  padding-top: 6px;
  background: #3491ff;
  border: 1px solid #3491ff;
  box-shadow: inset 0px 0px 6px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
  width: 30px;
  height: 30px;
}
.plus-button[active] {
  border: 1px solid #daeeff;
  box-shadow: inset 0px 0px 15px rgba(0, 0, 0, 0.25);
  border-radius: 3px;
}
.minus-button {
  background: #ffffff;
  /* CTA Colour/Primary CTA, Hyperlink */
  border: 1px solid #3491ff;
  border-radius: 3px;
  width: 30px;
  height: 30px;
}
</style>
