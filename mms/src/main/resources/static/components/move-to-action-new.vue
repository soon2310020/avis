<template>
  <a-popover v-model="visibleMove" placement="bottomLeft" trigger="click">
    <div class="d-flex align-items-center" @click="openMove">
      <span class="mr-1">Move To</span>
      <svg
        width="13"
        height="12"
        viewBox="0 0 13 12"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M7.11447 2.61475C6.89292 2.61475 6.68547 2.50641 6.55872 2.32495L6.07284 1.62783C5.79442 1.22808 5.3378 0.989746 4.85084 0.989746C4.17213 0.989746 3.01838 0.989746 2.16634 0.989746C1.34355 0.989746 0.676758 1.65654 0.676758 2.47933V9.521C0.676758 9.91587 0.833841 10.295 1.1128 10.5745C1.3923 10.8535 1.77147 11.0106 2.16634 11.0106H10.833C11.2279 11.0106 11.607 10.8535 11.8865 10.5745C12.1655 10.295 12.3226 9.91587 12.3226 9.521V4.10433C12.3226 3.70945 12.1655 3.33029 11.8865 3.05079C11.607 2.77183 11.2279 2.61475 10.833 2.61475H7.11447ZM7.11447 3.42725H10.833C11.0128 3.42725 11.1845 3.49875 11.3118 3.6255C11.4386 3.75279 11.5101 3.9245 11.5101 4.10433V9.521C11.5101 9.70083 11.4386 9.87254 11.3118 9.99983C11.1845 10.1266 11.0128 10.1981 10.833 10.1981H2.16634C1.98651 10.1981 1.8148 10.1266 1.68751 9.99983C1.56076 9.87254 1.48926 9.70083 1.48926 9.521V2.47933C1.48926 2.10558 1.79259 1.80225 2.16634 1.80225H4.85084C5.07238 1.80225 5.27984 1.91058 5.40659 2.09204L5.89247 2.78916C6.17088 3.18891 6.62697 3.42725 7.11447 3.42725Z"
          fill="#4B4B4B"
        />
        <path
          d="M7.85384 6.40625H5.14551C4.92126 6.40625 4.73926 6.58825 4.73926 6.8125C4.73926 7.03675 4.92126 7.21875 5.14551 7.21875H7.85384C8.07809 7.21875 8.26009 7.03675 8.26009 6.8125C8.26009 6.58825 8.07809 6.40625 7.85384 6.40625Z"
          fill="#4B4B4B"
        />
        <path
          d="M6.48329 6.01644L7.279 6.81269L6.48329 7.60894C6.32458 7.76711 6.32458 8.02494 6.48329 8.18311C6.64146 8.34182 6.89929 8.34182 7.05746 8.18311L8.14079 7.09977C8.2995 6.94106 8.2995 6.68432 8.14079 6.52561L7.05746 5.44227C6.89929 5.28357 6.64146 5.28357 6.48329 5.44227C6.32458 5.60044 6.32458 5.85827 6.48329 6.01644Z"
          fill="#4B4B4B"
        />
      </svg>
    </div>
    <div slot="content">
      <ul class="child-item move-to-action">
        <div style="padding: 2px">
          <common-searchbar
            v-if="listSelectedTabs.length > 4"
            :placeholder="resources['search_tab_name']"
            :request-param="requestParam"
            :on-search="search"
          ></common-searchbar>
        </div>
        <li
          v-if="!subItem.defaultTab && subItem.name !== currentActiveTab.name"
          v-for="(subItem, index2) in tabFiltered"
          :key="`${index2}`"
          class="child-item-li"
        >
          <label @click="moveToTab(subItem)">
            {{ convertCodeToName(subItem) }}
          </label>
        </li>
        <li>
          <label>
            <a href="javascript:void(0)" @click="createTabView()"
              >+ Create tab view</a
            >
          </label>
        </li>
      </ul>
    </div>
  </a-popover>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    listSelectedTabs: Array,
    currentActiveTab: Object,
    listChecked: Array,
    typeView: {
      type: String,
      default: () => "mold",
    },
  },
  data() {
    return {
      requestParam: {
        query: "",
      },
      tabFiltered: [],
      visibleMove: false,
    };
  },
  watch: {
    visiblePopover(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    },
    listSelectedTabs() {
      this.tabFiltered = this.listSelectedTabs;
    },
  },
  methods: {
    search() {
      const str = this.requestParam.query.trim();
      if (!str) {
        this.tabFiltered = this.listSelectedTabs;
        return;
      }
      const __searchList = this.listSelectedTabs.filter((tabItem) =>
        tabItem.name.toLowerCase().includes(str.toLowerCase())
      );

      this.tabFiltered = __searchList;
    },
    moveToTab(tab) {
      this.visibleMove = false;
      this.$emit("move-to-tab", tab);
    },
    createTabView() {
      this.visibleMove = false;
      this.$emit("create-tab-view");
    },
    openMove() {
      this.visibleMove = true;
      this.requestParam.query = "";
      this.tabFiltered = this.listSelectedTabs;
    },
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
  mounted() {
    this.tabFiltered = this.listSelectedTabs;
  },
};
</script>

<style scoped>
.ant-dropdown-trigger1 {
  border-radius: 0px;
  border: none;
  padding: 5px 0px !important;
  margin-right: 20px;
  border-radius: 7px;
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  border: none;
  align-items: center;
  justify-content: space-between;
}

.ant-dropdown-trigger1:last-child {
  border-radius: 0px;
  border: none;
  padding: 5px 0px !important;
  margin-right: 0px;
  border-radius: 7px;
  display: flex;
  width: 135px;
  /* padding: 10px !important; */
  border: none;
  align-items: center;
  justify-content: space-between;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  color: #fff !important;
  fill: #fff !important;
  background-image: linear-gradient(to right, #414ff7, #6a4efb) !important;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  fill: #fff !important;
}

.down-icon {
  fill: #564efb;
}

.plus-icon {
  fill: #fff;
  position: absolute;
  top: 50%;
  left: 0;
  transform: translate(0, -50%);
}
.all-time-filters {
  position: relative;
}

.ant-menu-submenu-title:hover {
  background-image: linear-gradient(#414ff7, #6a4efb) !important;
  background-color: red;
}
</style>
