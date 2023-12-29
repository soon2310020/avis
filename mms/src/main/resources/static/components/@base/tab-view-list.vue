<template>
  <div>
    <ul class="child-item">
      <li v-for="(subItem, index2) in listSelectedTabs" :key="`${index2}`">
        <label :for="`dropdown-input-${index2}`">
          <template>
            <input
              :id="`dropdown-input-${index2}`"
              type="checkbox"
              :value="subItem.id"
              :checked="subItem.shown"
              :disabled="subItem.name === 'Enabled' && subItem.defaultTab"
              @change="selectTab(subItem, index2)"
            />
            <div
              class="checkbox-custom"
              :class="{
                checkboxDisabled:
                  subItem.name === 'Enabled' && subItem.defaultTab,
              }"
            ></div>
          </template>
          {{ convertCodeToName(subItem) }}
        </label>
      </li>
    </ul>
    <ul class="child-item">
      <li>
        <label>
          <a href="javascript:void(0)" @click="createTabView()"
            >+ Create tab view</a
          >
        </label>
      </li>
      <li>
        <label>
          <a href="javascript:void(0)" @click="openManageTabView"
            >Manage tab view</a
          >
        </label>
      </li>
    </ul>
  </div>
</template>

<script>
module.exports = {
  props: {
    listSelectedTabs: {
      type: Array,
      default: () => [],
    },
    createTabView: Function,
    openManageTabView: Function,
    selectTab: Function,
  },
  computed: {},
  methods: {
    truncateText(text, size) {
      if (text) {
        const trimText = text.trim();
        if (trimText) {
          if (trimText.length > size) {
            return text.slice(0, size) + "...";
          }
          return trimText;
        }
      }
      return "";
    },
    convertCodeToName(tabItem) {
      if (tabItem.defaultTab) {
        if (tabItem.name === "DELETED") {
          return "Disabled";
        } else {
          return (
            tabItem.name.charAt(0).toUpperCase() +
            tabItem.name.slice(1).toLocaleLowerCase().replace("_", " ")
          );
        }
      } else {
        return this.truncateText(tabItem.name, 20);
      }
    },
  },
};
</script>

<style scoped>
ul.child-item {
  padding-left: 0 !important;
  max-height: 212px;
}

/*.dropdown-list {*/
.child-item li label {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  height: 32px;
  padding: 0px 8px;
  cursor: pointer;
}

/*.child-item li label:hover {*/
/*  background: #e6f7ff;*/
/*}*/

.child-item li {
  margin: -4px 0px;
  list-style-type: none;
}

.child-item li label input {
  display: none;
}

.child-item li label input:checked + .checkbox-custom {
  border-color: #0075ff;
}

.child-item li label input:checked + .checkbox-custom::before {
  content: "";
  width: 9px;
  height: 9px;
  background-color: #0075ff;
}

.child-item li label .checkbox-custom {
  width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.checkboxDisabled::before {
  background-color: #cecece !important;
}
.checkboxDisabled {
  border-color: #cecece !important;
}
</style>
