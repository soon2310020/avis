<template>
  <div class="tooling-filter">
    <a-popover v-model="visible" placement="bottom" trigger="click">
      <div class="tooling-select-content">
        <div class="tooling-value">
          {{ titleShow }}
        </div>
        <div class="select-icon">
          <img class="caret-down" alt="caret down" src="/images/icon/caret-arrow-grey.svg" />
        </div>
      </div>
      <div
              slot="content"
              style="padding: 8px 6px;"
              class="dropdown-scroll"
      >
        <div class="content-body">
          <template>
            <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="toolingInput">
              <a-icon slot="prefix" type="search"/>
            </a-input>
            <div style="max-height: 250px; overflow-y: auto;">
              <template v-for="item in toolingFiltered">
                <a-col style="padding: 7px;" :key="item.id">
                  <p-check :checked="checkedTooling(item)" @change="onChangeTooling($event, item)">{{item.equipmentCode}}</p-check>
                </a-col>
              </template>
            </div>
          </template>
        </div>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    optionArrayTooling: Array,
    selectTooling: Function,
    resources: Object,
    parent: Object,
    initToolingIds: Array
  },
  data() {
    return {
      checkedListTooling: [],
      oldCheckedListTooling: [],
      toolingInput: "",
      visible: false,
    };
  },
  methods: {
    checkedTooling: function(item) {
      const findIndex = this.checkedListTooling.findIndex(
        value => value.id === item.id
      );
      return findIndex !== -1;
    },
    onChangeTooling: function(isChecked, item) {
      if (isChecked) {
        this.checkedListTooling = this.checkedListTooling.concat(item);
      } else {
        this.checkedListTooling = this.checkedListTooling.filter(
          value => value.id !== item.id
        );
      }
    },
    onMouseleaveTooling: function() {
      this.oldCheckedListTooling = this.checkedListTooling;
      this.selectTooling(this.parent.id, this.checkedListTooling.map(item => item.id));
    },
  },

  computed: {
    toolingFiltered() {
      const str = this.toolingInput;
      if (!str) {
        return this.optionArrayTooling;
      }
      var options = {
        threshold: 0,
        location: 0,
        distance: 100,
        keys: ["equipmentCode"]
      };
      var searcher = new Fuse(this.optionArrayTooling, options);
      return searcher.search(str).sort((firstItem, secondItem) => firstItem.name.toUpperCase() > secondItem.name.toUpperCase() ? 1 : -1);

    },
    titleShow: function () {
      if (this.checkedListTooling.length === 0) {
        return '';
      }
      if (this.checkedListTooling.length === 1) {
        return '1 Tooling';
      }
      return this.checkedListTooling.length + 'Toolings';
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue === false && oldValue === true) {
        this.onMouseleaveTooling();
      }
    },
    initToolingIds: function (value) {
      if (value) {
        this.checkedListTooling = this.optionArrayTooling.filter(item => value.includes(item.id));
        this.oldCheckedListTooling = this.checkedListTooling;
      }

    }
  },
  mounted() {
  }
};
</script>

<style scoped>
.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: 100px;
  background: #f2f5fa;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  fill: #fff !important;
}

.down-icon {
  fill: #564efb;
}

.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}

.first-layer-wrapper {
  position: relative;
}

.first-layer {
  position: absolute;
  top: 0;
}
</style>
