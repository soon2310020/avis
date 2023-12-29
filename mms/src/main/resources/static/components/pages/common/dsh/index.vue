<template>
  <dashboard-overview-template>
    <template #filter>
      <master-filter-wrapper
        filter-code="COMMON"
        :notify-filter-updated="masterFilterUpdateHandler"
      ></master-filter-wrapper>
    </template>
    <template #setting>
      <!-- Button trigger -->
      <a-tooltip color="white">
        <template slot="title">
          <div class="custom-tooltip">
            <span>Customize Widget</span>
            <div class="custom-arrow-tooltip"></div>
          </div>
        </template>
        <a
          @click="handleClickSettings"
          href="javascript:void(0)"
          class="btn-custom customize-button"
        >
          <img
            style="margin-bottom: 3px"
            width="16"
            height="16"
            src="/images/icon/settings.svg"
          />
        </a>
      </a-tooltip>
    </template>
    <template #content>
      <div
        v-if="resources && Object.keys(resources)?.length"
        ref="grid-widget-board"
        class="grid-widget-board"
        @drop="handleDrop($event)"
        @dragenter.prevent
        @dragover.prevent="handleDragOver"
      >
        <div
          v-for="(item, index) in listActiveWidget"
          :key="item.key"
          :ref="item.key"
          class="widget-board"
          :class="[`size-${item.size}`, { moving: isMoving }]"
          draggable="true"
          :data="JSON.stringify(__retrieveBlockData(item, index))"
          @dragstart="handleDragStart($event, item)"
          @dragenter.capture="handleDragEnter"
          @dragleave="handleDragLeave"
        >
          <component
            :key="COMPONENTS[item.key] + renderKey"
            :style="{ height: '100%' }"
            :is="COMPONENTS[item.key]"
            :resources="resources"
            v-bind="item"
          />
        </div>
      </div>
    </template>
    <template #meta>
      <customization-widget
        ref="customizationWidgetRef"
        :resources="resources"
        @save="handleSave"
      ></customization-widget>
    </template>
  </dashboard-overview-template>
</template>

<script>
// note: component names are declared in /dsh/index.js
// should update key followed by in the future
const COMPONENTS = {
  // 'W02': 'pm-compliance-widget',
  // W03: "work-order-status-widget",
  // W05: "tooling-distribution-widget",
  // W06: "demand-compliance-tracker-widget",
  "WGT-TOL": "total-toolings-widget",
  "WGT-TOL-OPR": "operational-summary-widget",
  "WGT-TOL-UTL": "overall-utilization-rate-widget",
  "WGT-TOL-INA": "inactive-toolings-widget",
  "WGT-TOL-TCO": "total-cost-of-ownership-widget",
  "WGT-TOL-EOL": "end-of-life-widget",
  "WGT-TOL-RLO": "relocations-widget",
  "WGT-CYC-TIM-CPL": "cycle-time-compliance-widget",
};

module.exports = {
  name: "DashboardPage",
  components: {
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  data() {
    return {
      resources: null,
      currentWidget: {},
      cursorPosition: {},
      widgetCenter: {},
      COMPONENTS,
      renderKey: 0,
      listActiveWidget: [],
      xyPositionList: [],
      showCustomizationModal: false,
      showMoreRow: false,
      isMoving: false,
    };
  },
  computed: {
    // tracking global state
    permission() {
      return true;
    },
  },
  methods: {
    masterFilterUpdateHandler() {
      this.renderKey++;
    },
    // api
    async fetchSaveDashboardSettings(listWidget) {
      const payload = listWidget.map((o) => ({
        id: o.key,
        name: o.label,
        width: o.size,
        height: 1,
      }));
      try {
        await axios.post("/api/common/dsh/configs", {
          content: payload,
        });
      } catch (error) {
        console.log(error);
      }
    },

    async fetchDashboardContent() {
      try {
        const response = await axios.get("/api/common/dsh");
        this.listActiveWidget = response?.data?.content?.map((o) => ({
          key: o.id,
          label: o.name,
          size: o.width,
          // TODO update logic with variable height
        }));
      } catch (error) {
        console.log(response);
      }
    },
    // handler
    handleSave(listWidget) {
      this.listActiveWidget = listWidget;
      this.fetchSaveDashboardSettings(listWidget);
    },
    // drag 시작 시
    handleDragStart(event, item) {
      this.isMoving = true;
      event.dataTransfer.dropEffect = "move";
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("application/json", JSON.stringify(item));
    },
    // drag 요소가 유효한 놓기 대상에 들어갈 시
    handleDragEnter(event) {
      event.preventDefault();

      if (this.currentWidget?.classList) {
        this.currentWidget.classList.remove("dropline-right");
        this.currentWidget.classList.remove("dropline-left");
      }

      this.currentWidget = event.target.closest(".widget-board");

      let rect = this.currentWidget.getBoundingClientRect();
      this.widgetCenter = {
        x: rect.left + rect.width / 2,
        y: rect.top + rect.height / 2,
      };
    },
    // 유효한 놓기 대상 위에서 드래그 중일 때 (수백밀리초마다)
    handleDragOver(event) {
      this.cursorPosition = { x: event.clientX, y: event.clientY };
      if (this.cursorPosition.x < this.widgetCenter.x) {
        this.currentWidget.classList.add("dropline-left");
        this.currentWidget.classList.remove("dropline-right");
      }
      if (this.cursorPosition.x > this.widgetCenter.x) {
        this.currentWidget.classList.add("dropline-right");
        this.currentWidget.classList.remove("dropline-left");
      }
    },
    // 유효한 놓기 대상 영역에서 벗어날 때
    handleDragLeave(event) {
      event.preventDefault();
    },
    handleDrop(event) {
      this.currentWidget.classList.remove("dropline-right");
      this.currentWidget.classList.remove("dropline-left");

      const widgetStringified = event.dataTransfer.getData("application/json");
      const sourceData = JSON.parse(widgetStringified);
      if (!this.currentWidget) {
        console.log("drop outside");
        return;
      }

      let xyPositionList = this.listActiveWidget
        .map((i) => {
          return {
            key: i.key,
            label: i.label,
            size: i.size,
            x: this.$refs[i.key][0].getBoundingClientRect().left,
            y: this.$refs[i.key][0].getBoundingClientRect().top,
            position: null,
          };
        })
        .filter((i) => {
          return i.key !== sourceData.key;
        });
      const yPositionList = [...new Set(xyPositionList.map((i) => i.y))];
      yPositionList.push(event.clientY);
      yPositionList.sort();
      sourceData.y = yPositionList[yPositionList.indexOf(event.clientY) - 1];
      sourceData.x = event.clientX - this.currentWidget.offsetWidth / 2;
      xyPositionList.push(sourceData);

      this.xyPositionList = xyPositionList
        .sort((a, b) => {
          if (a.y == b.y) return a.x - b.x;
          return a.y - b.y;
        })
        .map((i, index) => ({ ...i, position: index }));

      this.listActiveWidget = JSON.parse(JSON.stringify(this.xyPositionList));
      // update list available
      this.fetchSaveDashboardSettings(this.listActiveWidget);

      // manipulate DOM
      this.isMoving = false;
    },
    handleClickSettings() {
      this.$refs["customizationWidgetRef"].openModal();
    },
    __retrieveBlockData(item, index) {
      const key = item === null ? "" : item.key;
      const size = item === null ? 1 : item.size;
      const label = item === null ? "" : item.label;

      return { key, size, label, position: index };
    },
  },
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );
    this.fetchDashboardContent();
  },
};
</script>
<style>
.customize-button {
  background: #f0f1f3;
}

/* Tooltip */
.custom-tooltip {
  padding: 6px 8px;
  font-size: 13px;
  color: #3491ff;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0px 0px 4px 1px #e5dfdf;
}

.custom-arrow-tooltip {
  background: #fff;
  left: 51%;
  bottom: -4px;
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
.grid-widget-board {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.widget-board {
  min-height: 300px;
  /* TODO: 245px 부분을 추후에 레이아웃 높이를 token 으로 가져올 수 있을 경우 변경 */
  height: calc((100vh - 245px) / 2);
  flex-shrink: 0;
  flex-grow: 0;
  position: relative;
}

.widget-board.dropline-left::after,
.widget-board.dropline-right::after {
  content: "";
  position: absolute;
  width: 12px;
  height: calc(100% + 8px);
  border-top: solid 2px #4b4b4b;
  border-bottom: solid 2px #4b4b4b;
  top: -4px;
}

.widget-board.dropline-left::after {
  left: -11px;
}
.widget-board.dropline-right::after {
  right: -11px;
}

.widget-board.dropline-left::before,
.widget-board.dropline-right::before {
  content: "";
  position: absolute;
  width: 1px;
  height: calc(100% + 8px);
  border-left: solid 2px #4b4b4b;
  top: -4px;
}

.widget-board.dropline-left::before {
  left: -6px;
}
.widget-board.dropline-right::before {
  right: -6px;
}

.widget-board.moving * {
  user-select: none !important;
  pointer-events: none !important;
}

.widget-board.size-1 {
  flex-basis: calc(25% - 7.5px);
  min-width: 280px;
}

.widget-board.size-2 {
  flex-basis: calc(50% - 5px);
  min-width: 560px;
}

.widget-board.size-3 {
  flex-basis: calc(75% - 2.5px);
  min-width: 840px;
}

.widget-board.size-4 {
  flex-basis: calc(100%);
}
</style>
