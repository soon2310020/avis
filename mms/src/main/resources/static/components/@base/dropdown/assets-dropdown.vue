<template>
  <div class="assets-dropdown">
    <a-popover v-model="visibleAsset" placement="bottomLeft" trigger="click">
      <custom-dropdown-button
        :title="getAssetTitle"
        :is-selected="true"
        :is-show="visibleAsset"
        @click="openAssetDropdown"
      ></custom-dropdown-button>
      <a-menu
        v-if="!isAssetView"
        slot="content"
        class="wrapper-action-dropdown"
        style="
          border-right: unset !important;
          max-height: 250px;
          overflow: auto;
        "
      >
        <a-menu-item
          v-if="assetTypes.includes('TOOLING')"
          @click="
            isAssetView = 'tooling';
            isVisibleMoldDropdown = true;
          "
        >
          <span>{{ resources["toolings"] }}</span>
        </a-menu-item>
        <a-menu-item
          v-if="assetTypes.includes('MACHINE')"
          @click="
            isAssetView = 'machine';
            isVisibleMachineDropdown = true;
          "
        >
          <span>{{ resources["machines"] }}</span>
        </a-menu-item>
        <a-menu-item
          v-if="assetTypes.includes('TERMINAL')"
          @click="
            isAssetView = 'terminal';
            isVisibleTerminalDropdown = true;
          "
        >
          <span>{{ resources["terminals"] }}</span>
        </a-menu-item>
        <a-menu-item
          v-if="assetTypes.includes('SENSOR')"
          @click="
            isAssetView = 'sensor';
            isVisibleSensorDropdown = true;
          "
        >
          <span>{{ resources["counters"] }}</span>
        </a-menu-item>
      </a-menu>
      <div v-else-if="isAssetView === 'tooling'" slot="content">
        <common-select
          :style="{ position: 'static', width: '200px' }"
          :class="{ show: isVisibleMoldDropdown }"
          :items="moldIds"
          :searchbox="true"
          :multiple="mutil"
          :has-check-box="true"
          :has-toggled-all="true"
          @on-change="onChangeMold"
          :checked-items-list="checkedItemsList"
          @on-search-pagination="onSearchPagination"
          :asset-view="isAssetView"
          @on-change-checked="
            (result) => onChangeCheckedList(result, isAssetView)
          "
          :placeholder="'Search tooling'"
          :paginatable="true"
          :current-page="moldPage"
          :total-page="moldTotalPage"
          @close="closeMoldDropdown"
          @show-more="handleLoadAssets"
          @unselect-all="() => handleUnselectAll(isAssetView)"
        ></common-select>
      </div>
      <div v-else-if="isAssetView === 'machine'" slot="content">
        <common-select
          :style="{ position: 'static', width: '200px' }"
          :class="{ show: isVisibleMachineDropdown }"
          :items="machineIds"
          :searchbox="true"
          :multiple="true"
          :has-toggled-all="true"
          @on-change="onChangeMachine"
          :placeholder="'Search machine'"
          :paginatable="true"
          :checked-items-list="checkedItemsList"
          :asset-view="isAssetView"
          @on-change-checked="
            (result) => onChangeCheckedList(result, isAssetView)
          "
          @on-search-pagination="onSearchPagination"
          :current-page="machinePage"
          :total-page="machineTotalPage"
          @close="closeMachineDropdown"
          @show-more="handleLoadAssets"
          @unselect-all="() => handleUnselectAll(isAssetView)"
        >
        </common-select>
      </div>
      <div v-else-if="isAssetView === 'terminal'" slot="content">
        <common-select
          :style="{ position: 'static', width: '200px' }"
          :class="{ show: isVisibleTerminalDropdown }"
          :items="terminalIds"
          :searchbox="true"
          :multiple="true"
          :has-toggled-all="true"
          @on-change="onChangeTerminal"
          :checked-items-list="checkedItemsList"
          @on-change-checked="
            (result) => onChangeCheckedList(result, isAssetView)
          "
          :asset-view="isAssetView"
          @on-search-pagination="onSearchPagination"
          :placeholder="'Search terminal'"
          :paginatable="true"
          :current-page="terminalPage"
          :total-page="terminalTotalPage"
          @close="closeTerminalDropdown"
          @show-more="handleLoadAssets"
          @unselect-all="() => handleUnselectAll(isAssetView)"
        >
        </common-select>
      </div>
      <div v-else-if="isAssetView === 'sensor'" slot="content">
        <common-select
          :style="{ position: 'static', width: '200px' }"
          :class="{ show: isVisibleSensorDropdown }"
          :items="sensorIds"
          :searchbox="true"
          :multiple="true"
          :has-toggled-all="true"
          @on-change="onChangeSensor"
          :checked-items-list="checkedItemsList"
          @on-change-checked="
            (result) => onChangeCheckedList(result, isAssetView)
          "
          :asset-view="isAssetView"
          @on-search-pagination="onSearchPagination"
          :placeholder="'Search sensor'"
          :paginatable="true"
          :current-page="sensorPage"
          :total-page="sensorTotalPage"
          @close="closeSensorDropdown"
          @show-more="handleLoadAssets"
          @unselect-all="() => handleUnselectAll(isAssetView)"
        >
        </common-select>
      </div>
    </a-popover>
    <div>
      <base-chip
        v-for="item in getSelectedMold"
        :key="item.id"
        :selection="item"
        @close="onRemoveMold"
      >
      </base-chip>
      <base-chip
        v-for="item in getSelectedMachine"
        :key="item.id"
        :color="'red'"
        :selection="item"
        @close="onRemoveMachine"
      >
      </base-chip>
      <base-chip
        v-for="item in getSelectedTerminal"
        :key="item.id"
        :selection="item"
        :color="'green'"
        @close="onRemoveTerminal"
      >
      </base-chip>
      <base-chip
        v-for="item in getSelectedSensor"
        :key="item.id"
        :selection="item"
        :color="'orange'"
        @close="onRemoveSensor"
      >
      </base-chip>
    </div>
  </div>
</template>

<script>
const ASSET_SIZE = 10;
module.exports = {
  props: {
    title: {
      type: String,
      default: "Select asset",
    },
    clearKey: [String, Number],
    assetSelected: {
      type: Array,
    },
    assetTypes: {
      type: Array,
      default: ["TOOLING", "MACHINE", "TERMINAL", "SENSOR"],
    },
    resources: Object,
    mutil: {
      type: Boolean,
      default: true,
    },
  },
  components: {
    "custom-dropdown-button": httpVueLoader(
      "/components/@base/button/custom-dropdown-button.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "custom-dropdown": httpVueLoader(
      "/components/common/selection-dropdown/custom-dropdown.vue"
    ),
  },
  data() {
    return {
      isAssetView: false,
      isChecklistView: false,
      visibleAsset: false,
      visibleChecklist: false,
      rawUserIds: [],
      rawMoldIds: [],
      rawMachineIds: [],
      rawTerminalIds: [],
      rawSensorIds: [],
      checkedItemsList: [],
      moldIds: [],
      machineIds: [],
      terminalIds: [],
      sensorIds: [],
      moldPage: 1,
      moldTotalPage: 1,
      machinePage: 1,
      machineTotalPage: 1,
      terminalPage: 1,
      terminalTotalPage: 1,
      sensorPage: 1,
      sensorTotalPage: 1,
      searchContent: "",
      isVisibleMoldDropdown: false,
      isVisibleMachineDropdown: false,
      isVisibleTerminalDropdown: false,
      isVisibleSensorDropdown: false,
    };
  },
  created() {
    this.init();
  },
  watch: {
    async clearKey() {
      await this.clearData();
    },
    async isAssetView(newVal) {
      switch (newVal) {
        case "tooling":
          this.moldPage = 1;
          this.rawMoldIds = [];
          await this.getMolds(this.moldPage, this.searchContent);
          break;
        case "machine":
          this.rawMachineIds = [];
          this.machinePage = 1;
          await this.getMachines(this.machinePage, this.searchContent);
          break;
        case "terminal":
          this.rawTerminalIds = [];
          this.terminalPage = 1;
          await this.getTerminals(this.terminalPage, this.searchContent);
          break;
        case "counter":
          this.rawSensorIds = [];
          this.sensorPage = 1;
          await this.getSensors(this.sensorPage, this.searchContent);
          break;
      }
    },
    async searchContent(newVal) {
      switch (this.isAssetView) {
        case "tooling":
          this.moldPage = 1;
          this.rawMoldIds = [];
          await this.getMolds(this.moldPage, newVal);
          break;
        case "machine":
          this.rawMachineIds = [];
          this.machinePage = 1;
          await this.getMachines(this.machinePage, newVal);
          break;
        case "terminal":
          this.rawTerminalIds = [];
          this.terminalPage = 1;
          await this.getTerminals(this.terminalPage, newVal);
          break;
        case "counter":
          this.rawSensorIds = [];
          this.sensorPage = 1;
          await this.getSensors(this.sensorPage, newVal);
          break;
      }
    },
    visibleAsset(newVal) {
      if (!newVal) {
        this.sendResult();
      }
    },
  },
  computed: {
    getAssetTitle() {
      // const isSelected =
      //   this.getSelectedMold.length > 0 ||
      //   this.getSelectedMachine.length > 0 ||
      //   this.getSelectedTerminal.length > 0 ||
      //   this.getSelectedSensor.length > 0;
      // if (isSelected) {
      //   return "Add another asset";
      // } else {
      //   return "Add an asset";
      // }
      return this.title;
    },
    getSelectedMold() {
      let temp = _.filter(this.moldIds, (o) => o.checked);

      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "tooling"
        ) {
          temp.push(item);
        }
      });
      return temp;
    },
    getSelectedMachine() {
      let temp = _.filter(this.machineIds, (o) => o.checked);

      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "machine"
        ) {
          temp.push(item);
        }
      });
      return temp;
    },
    getSelectedTerminal() {
      let temp = _.filter(this.terminalIds, (o) => o.checked);

      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "terminal"
        ) {
          temp.push(item);
        }
      });
      return temp;
    },
    getSelectedSensor() {
      let temp = _.filter(this.sensorIds, (o) => o.checked);

      this.checkedItemsList.forEach((item) => {
        if (
          !temp.map((item) => item.id).includes(item.id) &&
          item.assetView == "counter"
        ) {
          temp.push(item);
        }
      });
      return temp;
    },
  },
  methods: {
    async clearData() {
      this.isAssetView = false;
      this.isChecklistView = false;
      this.visibleAsset = false;
      this.visibleChecklist = false;
      this.rawUserIds = [];
      this.rawMoldIds = [];
      this.rawMachineIds = [];
      this.rawTerminalIds = [];
      this.rawSensorIds = [];
      this.checkedItemsList = [];
      this.moldIds = [];
      this.machineIds = [];
      this.terminalIds = [];
      this.sensorIds = [];
      this.moldPage = 1;
      this.moldTotalPage = 1;
      this.machinePage = 1;
      this.machineTotalPage = 1;
      this.terminalPage = 1;
      this.terminalTotalPage = 1;
      this.sensorPage = 1;
      this.sensorTotalPage = 1;
      this.searchContent = "";
      this.isVisibleMoldDropdown = false;
      this.isVisibleMachineDropdown = false;
      this.isVisibleTerminalDropdown = false;
      this.isVisibleSensorDropdown = false;
      await this.init();
    },
    sendResult() {
      const result = {
        mold: this.getSelectedMold,
        machine: this.getSelectedMachine,
        terminal: this.getSelectedTerminal,
        sensor: this.getSelectedSensor,
      };
      this.$emit("change", result);
    },
    onChangeCheckedList(results, assetView) {
      // const checkedItemsWithAssetView = results.filter(
      //   (item) => item.assetView === assetView
      // );

      // this.checkedItemsList = _.mergeByKey(
      //   this.checkedItemsList,
      //   checkedItemsWithAssetView,
      //   "id"
      // );
      this.checkedItemsList = results;
    },
    openAssetDropdown() {
      this.isAssetView = false;
      this.visibleAsset = true;
    },
    onChangeMold(results) {
      this.moldIds = results;
    },
    onChangeMachine(results) {
      this.machineIds = results;
    },
    onChangeTerminal(results) {
      this.terminalIds = results;
    },
    onChangeSensor(results) {
      this.sensorIds = results;
    },
    closeMoldDropdown() {
      this.isVisibleMoldDropdown = false;
      this.isAssetView = false;
    },
    closeMachineDropdown() {
      this.isVisibleMachineDropdown = false;
      this.isAssetView = false;
    },
    closeTerminalDropdown() {
      this.isVisibleTerminalDropdown = false;
      this.isAssetView = false;
    },
    closeSensorDropdown() {
      this.isVisibleSensorDropdown = false;
      this.isAssetView = false;
    },
    onSearchPagination(searchContent) {
      this.searchContent = searchContent;
    },
    onRemoveTerminal(asset) {
      this.terminalIds = this.terminalIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
      this.sendResult();
    },
    onRemoveSensor(asset) {
      this.sensorIds = this.sensorIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
      this.sendResult();
    },
    onRemoveMold(asset) {
      this.moldIds = this.moldIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
      this.sendResult();
    },
    handleUnselectAll(assetView) {
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.assetView !== assetView
      );
    },
    onRemoveMachine(asset) {
      this.machineIds = this.machineIds.map((item) =>
        item.id === asset.id ? { ...item, checked: false } : { ...item }
      );
      this.checkedItemsList = this.checkedItemsList.filter(
        (item) => item.id !== asset.id
      );
      this.sendResult();
    },
    getIndexOf(arr, obj) {
      let index = -1;
      arr.forEach((o, i) => {
        if (o.id == obj.id) index = i;
      });
      return index;
    },
    async getMolds(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/molds/lite-data-work-order?id=&searchEquipmentCode=${searchContent}&status=all&page=${loadPage}&size=${ASSET_SIZE}`
        : `/api/molds/lite-data-work-order?id=&query=&status=all&page=${loadPage}&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.moldTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.equipmentCode,
          checked: false,
        }));
        this.rawMoldIds = [...this.rawMoldIds, ...loadData];

        let temp = _.map(JSON.parse(JSON.stringify(this.rawMoldIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });
        this.moldIds = results;
      });
    },
    async getMachines(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/machines?searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`
        : `/api/machines?query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.machineTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.machineCode,
          checked: false,
        }));
        this.rawMachineIds = [...this.rawMachineIds, ...loadData];

        let temp = _.map(JSON.parse(JSON.stringify(this.rawMachineIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.machineIds = results;
      });
    },
    async getTerminals(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/terminals?status=all&searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`
        : `/api/terminals?status=all&query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}`;
      await axios.get(url).then((res) => {
        this.terminalTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child.terminal,
          title: child.terminal.equipmentCode,
          checked: false,
        }));
        this.rawTerminalIds = [...this.rawTerminalIds, ...loadData];

        let temp = _.map(JSON.parse(JSON.stringify(this.rawTerminalIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.terminalIds = results;
      });
    },
    async getSensors(page, searchContent) {
      const loadPage = page ? page : 1;
      const url = searchContent
        ? `/api/counters?status=all&searchEquipmentCode=${searchContent}&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}&where=opand,eq,eq`
        : `/api/counters?status=all&query=&page=${loadPage}&id=&searchType=&size=${ASSET_SIZE}&where=opand,eq,eq`;
      await axios.get(url).then((res) => {
        this.sensorTotalPage = res.data.totalPages;
        const loadData = res.data.content.map((child) => ({
          ...child,
          title: child.equipmentCode,
          checked: false,
        }));
        this.rawSensorIds = [...this.rawSensorIds, ...loadData];
        let temp = _.map(JSON.parse(JSON.stringify(this.rawSensorIds)), (o) =>
          this.checkedItemsList.map((i) => i.id).includes(o.id)
            ? { ...o, checked: true }
            : { ...o, checked: false }
        );
        let results = temp.filter((o, i) => {
          return this.getIndexOf(temp, o) == i;
        });

        this.sensorIds = results;
      });
    },
    async handleLoadAssets() {
      switch (this.isAssetView) {
        case "tooling":
          this.moldPage++;
          await this.getMolds(this.moldPage, this.searchContent);
          break;
        case "machine":
          this.machinePage++;
          await this.getMachines(this.machinePage, this.searchContent);
          break;
        case "terminal":
          this.terminalPage++;
          await this.getTerminals(this.terminalPage, this.searchContent);
          break;
        case "counter":
          this.sensorPage++;
          await this.getSensors(this.sensorPage, this.searchContent);
          break;
      }
    },
    async init() {
      this.checkedItemsList = [];
      if (this.assetSelected) {
        if (_.isArray(this.assetSelected)) {
          this.checkedItemsList = [...this.assetSelected];
        } else {
          this.checkedItemsList = [this.assetSelected];
        }
        console.log(
          "asset-dropdown:::created:::",
          this.checkedItemsList,
          this.assetSelected
        );
      }
      await Promise.all([
        this.getMolds(),
        this.getMachines(),
        this.getTerminals(),
        this.getSensors(),
      ]);
      console.log("init done");
    },
  },
};
</script>

<style></style>
