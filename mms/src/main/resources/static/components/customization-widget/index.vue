<template>
  <!-- Modal custom -->
  <emdn-modal
    v-if="showModal"
    :is-opened="showModal"
    :heading-text="modalConfig.title"
    :modal-handler="closeModal"
  >
    <template #body>
      <div class="container widget-settings">
        <div class="row" style="margin-top: 3px">
          <div class="col-4">
            <div class="list-area">
              <div class="input-group mb-2">
                <div class="input-group-prepend">
                  <div class="input-group-text">
                    <i class="fa fa-search"></i>
                  </div>
                </div>
                <input
                  type="text"
                  class="form-control"
                  v-model="searchbar"
                  placeholder="Search Widget"
                />
              </div>
              <div class="widget-list">
                <div
                  v-for="item in listSearchedWidget"
                  :draggable="true"
                  :key="item.key"
                  @dragstart="handleDragStart($event, item, true)"
                  class="widget-item"
                  :class="{ 'in-used': item.enabled }"
                >
                  <div class="widget-item-content">
                    <span class="widget-item-content__label">
                      {{ item.label }}
                    </span>
                  </div>
                  <button
                    class="widget-item-buttonPlus"
                    @click="handleAddBlock(item)"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="col-8">
            <div>
              <div style="height: 35px; margin-bottom: 8px"></div>
            </div>
            <div
              :style="{ position: 'relative' }"
              style="max-height: 390px; overflow-y: auto"
            >
              <!-- empty box (background widget) -->
              <div class="grid-layout">
                <div
                  v-for="(item, index) in gridRowCount * 4"
                  :key="`slotCount-${index}`"
                  class="widget-block empty size-1"
                >
                  <div class="widget-block-content"></div>
                </div>
              </div>
              <!-- content box (real widget) -->
              <div
                class="grid-layout"
                :style="{
                  position: 'absolute',
                  zIndex: 1,
                  width: '100%',
                  top: 0,
                  background: 'inherit',
                }"
                @drop="handleDrop($event)"
                @dragenter.prevent
                @dragover.prevent="handleDragOver"
              >
                <div
                  v-for="(item, index) in listWidgetBlock"
                  :key="index"
                  class="widget-block item"
                  :class="[`size-${item.size}`]"
                  :draggable="true"
                  :ref="item.key"
                  @dragstart="
                    handleDragStart($event, __retrieveBlockData(item, index))
                  "
                  :data="JSON.stringify(__retrieveBlockData(item, index))"
                  @dragenter.capture="handleDragEnter"
                  @dragleave="handleDragLeave"
                >
                  <div class="widget-block-content">
                    <button
                      v-if="item.key"
                      class="widget-block__buttonRemove"
                      @click="handleRemoveBlock(item, index)"
                    >
                      <span class="widget-block__buttonRemoveIcon"></span>
                    </button>
                    <div v-if="item.label" class="widget-block__textContent">
                      <span>
                        <img src="/images/icon/drag-icon-2.svg" alt="drag" />
                      </span>
                      <span>
                        {{ item.label }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template #footer>
      <div
        class="d-flex align-items-center justify-content-between w-100"
        style="padding-left: 1rem; padding-right: 1rem"
      >
        <emdn-cta-button
          :click-handler="handleResetToDefault"
          button-type="text"
        >
          {{ resources["reset_to_default"] }}
        </emdn-cta-button>
        <emdn-cta-button :click-handler="handleSave" color-type="green-fill">
          {{ resources["save"] }}
        </emdn-cta-button>
      </div>
    </template>
  </emdn-modal>
</template>

<script>
const EMITS = {
  SAVE: "save",
};
const DATA_TRANSFER_KEY = "WIDGET";

module.exports = {
  name: "WidgetCustomization",
  props: {
    resources: Object,
    listWidget: Array,
  },
  data() {
    return {
      currentWidget: {},
      cursorPosition: {},
      widgetCenter: {},
      dragItem: {},
      xyPositionList: [],
      modalConfig: {
        title: "Customize Dashboard",
      },
      showModal: false,
      searchbar: "",
      listAvailableWidget: [],
      listSearchedWidget: [],
      listActiveWidget: [],
      listWidgetBlock: [],
      movingFromList: false,
    };
  },
  computed: {
    gridRowCount() {
      let rowCount = 1;
      let rowItem = 0;
      this.listWidgetBlock.map((item) => {
        if (rowItem + item.size > 4) {
          rowCount += 1;
          rowItem = item.size;
        } else {
          rowItem += item.size;
        }
      });
      // 기본으로 두줄 보여준다.
      if (rowCount < 2) rowCount = 2;
      return rowCount;
    },
  },
  methods: {
    // public
    openModal() {
      this.showModal = true;
      this.fetchDashboardSettings();
    },
    closeModal() {
      this.showModal = false;
    },

    // api
    async fetchDashboardSettings() {
      try {
        const response = await axios.get("/api/common/dsh/configs");

        if (!response?.data?.content) throw new Error("No data");

        this.listWidgetBlock = response.data.contentEnabled.map((o) => {
          return {
            key: o.id,
            label: o.name,
            size: o.width,
            enabled: true,
          };
        });

        this.listAvailableWidget = response.data.content.map((o) => {
          return {
            key: o.id,
            label: o.name,
            size: o.width,
            enabled: this.listWidgetBlock.some((i) => o.key === i.key),
          };
        });
      } catch (error) {
        console.log(error);
      }
    },

    // drag 시작시의 동작
    handleDragStart(event, item, movingFromList) {
      console.log("handleDragStart");
      this.movingFromList = movingFromList;

      // dragItem 저장 (drop 시에 listWidgetBlock 에 추가하기 위해)
      this.dragItem = {
        enabled: item.enabled,
        key: item.key,
        label: item.label,
        size: item.size,
        x: null,
        y: null,
        position: null,
      };

      // drag 를 시작할 때 widget 항목 들의 xy position 저장 (xyPositionList)
      this.xyPositionList = this.listWidgetBlock
        .map((i) => {
          return {
            enabled: i.enabled,
            key: i.key,
            label: i.label,
            size: i.size,
            x: this.$refs[i.key][0].getBoundingClientRect().left,
            y: this.$refs[i.key][0].getBoundingClientRect().top,
            position: null,
          };
        })
        .filter((i) => {
          return i.key !== item.key;
        });

      event.dataTransfer.dropEffect = "move";
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("application/json", JSON.stringify(item));
    },

    handleDragEnter(event) {
      console.log("handleDragEnter: ", event);
      event.preventDefault();

      if (this.currentWidget?.classList) {
        this.currentWidget.classList.remove("dropline-right");
        this.currentWidget.classList.remove("dropline-left");
      }

      this.currentWidget = event.target.closest(".widget-block");

      console.log("this.currentWidget: ", this.currentWidget);

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

    // drop 시에 dragItem 에 x 와 y 를 설정하고, xyPositionList 에 추가
    handleDrop(event) {
      // new ============================
      console.log("%c handleDrop 22: ", "color:red;", event);

      this.currentWidget.classList.remove("dropline-right");
      this.currentWidget.classList.remove("dropline-left");

      /**
       * dragItem 의 y 값 구하기 (y열의 수 구하고 dragItem이 어디에 속하는 지 판단하기.)
       *
       * 1. y값만 담은 array 만들고 중복값 제거.
       * 2. dragItem y값 추가하고 sort.
       * 3. sort 한 index 에서 바로 앞에 있는 값을 y값으로 사용.
       * (커서의 위치가 첫 번째 줄보다 아래에 있고, 두 번째 줄보다 커서가 위에 있을 경우 첫번째 줄로 설정되어야 하기 때문에)
       */
      let yPositionList = this.xyPositionList.map((item) => {
        return item.y;
      });
      yPositionList = [...new Set(yPositionList)];
      yPositionList.push(event.clientY);
      yPositionList.sort();
      let y = yPositionList[yPositionList.indexOf(event.clientY) - 1];

      /**
       * dragItem 의 x 값 구하기
       *
       * - 위젯에 drop 했을 경우에는 위젯의 중간을 기준으로 왼쪽은 앞으로, 오른쪽은 뒤로 drop한 위젯이 배치되게 한다.
       * - drop된 위치가 button 또는 span 일 경우 상위인 div 의 offset 을 기준으로 계산한다.
       * - 위젯이 아닌 grid 에 drop 됐을 경우는 커서의 위치를 기준으로 계산한다.
       */
      let isButtonOrSpan =
        event.target.nodeName === "BUTTON" || event.target.nodeName === "SPAN";

      let x = isButtonOrSpan
        ? event.clientX - event.target.offsetParent.offsetWidth / 2
        : event.clientX - event.target.offsetWidth / 2;

      let isGridLayout = event.target.classList.contains("grid-layout");
      x = isGridLayout ? event.clientX : x;

      // dragItem 설정 후 xyPositionList 에 추가
      this.dragItem.y = y;
      this.dragItem.x = x;
      this.xyPositionList.push(this.dragItem);

      // xyPositionList 를 x,y 위치를 기준으로 재정렬하고 정렬된 순서대로 position 재설정 (1차 y축, 2차 x축)
      this.xyPositionList.sort((a, b) => {
        if (a.y == b.y) return a.x - b.x;
        return a.y - b.y;
      });

      // position 동기화
      this.xyPositionList.map((item, index) => {
        item.position = index;
      });
      this.listWidgetBlock = JSON.parse(JSON.stringify(this.xyPositionList));
      this.movingFromList = false;
    },

    handleDragLeave(event) {
      event.preventDefault();
    },

    handleAddBlock(sourceData) {
      let totalSize = 0;
      this.listWidgetBlock.map((item) => {
        totalSize += item.size;
      });
      totalSize += sourceData.size;

      if (totalSize > 12) return console.log("No available slot left !!");

      this.listWidgetBlock.push(sourceData);
    },

    handleRemoveBlock(item) {
      const newArray = this.listWidgetBlock.filter(
        (listWidget) => listWidget.key !== item.key
      );
      newArray.map((item, index) => {
        return { ...item, position: index };
      });
      this.listWidgetBlock = newArray;
    },
    handleResetToDefault() {
      // this.listWidgetBlock.splice(0);
      this.listWidgetBlock = this.listAvailableWidget.slice(0, 8);
    },
    handleSave() {
      this.$emit(EMITS.SAVE, this.listWidgetBlock);
      this.closeModal();
    },
    // utilities
    __retrieveBlockData(item, index) {
      const key = item === null ? "" : item.key;
      const size = item === null ? 1 : item.size;
      const label = item === null ? "" : item.label;

      return { key, size, label, position: index };
    },
  },
  watch: {
    searchbar: {
      handler(newVal) {
        this.listSearchedWidget = this.listAvailableWidget.filter((item) =>
          item?.label?.toUpperCase()?.includes(newVal?.toUpperCase())
        );
      },
      immediate: true,
    },
    listWidgetBlock(newVal) {
      this.listAvailableWidget = this.listAvailableWidget.map((item) => {
        return { ...item, enabled: newVal.some((o) => o.key === item.key) };
      });
    },
    listAvailableWidget(newVal) {
      // update list search follow list available
      this.listSearchedWidget = newVal?.filter((item) =>
        item?.label?.toUpperCase()?.includes(this.searchbar?.toUpperCase())
      );
    },
  },
};
</script>
<style scoped>
/* Main content */

.list-available-widget {
  background-color: #f0f1f3;
  padding: 10px 16px 13px 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.widget-grid {
  display: flex;
  flex-wrap: wrap;
  background-color: rgb(240, 241, 243);
  padding: 10px;
}

/* Base search */
.base-search {
  display: flex;
  padding: 2px;
  border: 0.5px solid #d6dade;
  border-radius: 3px;
}

.base-search__icon {
  display: inline-flex;
  width: 25.4px;
  height: 25.4px;
  justify-content: center;
  align-items: center;
  background-color: #f0f3f5;
}

.base-search__icon-img {
  display: inline-block;
  width: 16.44px;
  height: 16.43px;
  mask-image: url("/images/icon/search.svg");
  -webkit-mask-image: url("/images/icon/search.svg");
  mask-position: center;
  -webkit-mask-position: center;
  mask-size: contain;
  -webkit-mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-repeat: no-repeat;
  background-color: var(--grey-4);
}

.base-search__icon img {
  width: 16.44px;
  height: 16.44px;
}

.base-search__input {
  flex: 1;
  border: none;
  padding-left: 5px;
}

.base-search__input::placeholder {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #9d9d9d;
}

.widget-settings {
  --grid-gap: 10px;
}

/* container */
/* widget list */
.list-area {
  flex: 1;
}

.widget-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: #f0f1f3;
  /* padding: 10px 16px 10px 8px; */
  padding: 10px 8px;
  height: 415px;
  overflow: auto;
}

.widget-item {
  position: relative;
  height: 30px;
  display: flex;
  align-items: center;
  pointer-events: auto;
}

.widget-item.in-used {
  pointer-events: none;
}

.widget-item .widget-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  background-color: #fff;
  padding: 0.25rem;
  border: 1px solid black;
}

.widget-item:hover .widget-item-content {
  border-color: var(--blue);
  color: var(--blue);
}

.widget-item:hover .widget-item-buttonPlus {
  border-color: var(--blue);
  color: var(--blue);
}

.widget-item.in-used .widget-item-content {
  background-color: transparent;
  border-color: rgba(0, 0, 0, 0.3);
  color: rgba(0, 0, 0, 0.3);
}

.widget-item-content__label {
  padding-left: 8px;
}

.widget-item .widget-item-content__buttonDrag {
  border: none;
  padding-right: 8px;
  padding-left: 8px;
  background: inherit;
}

.widget-item .widget-item-content__buttonDrag:focus,
.widget-item .widget-item-content__buttonDrag:focus-visible {
  outline: none;
}

.widget-item .widget-item-buttonPlus {
  border: none;
  padding: 0 8px;
  background-color: #fff;
  height: 25px;
  border: 1px solid black;
  /* margin-left: 8px; */
  margin-left: 4px;
  border-radius: 3px;
}

.widget-item.in-used .widget-item-buttonPlus {
  background-color: transparent;
  border-color: rgba(0, 0, 0, 0.3);
  color: rgba(0, 0, 0, 0.3);
  opacity: 0;
}

/* widget grid */
.grid-area {
  flex: 1;
}

.grid-area .button-add-more {
  margin-top: 1rem;
}

.grid-layout {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  background-color: #f0f1f3;
  padding-right: 10px;
  padding-left: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
  gap: 10px;
}

.widget-block {
  height: 100px;
  position: relative;
  flex-shrink: 0;
  flex-grow: 0;
}

.widget-block.dropline-left::after,
.widget-block.dropline-right::after {
  content: "";
  position: absolute;
  width: 12px;
  height: calc(100% + 8px);
  border-top: solid 2px #4b4b4b;
  border-bottom: solid 2px #4b4b4b;
  top: -4px;
}

.widget-block.dropline-left::after {
  left: -11px;
}

.widget-block.dropline-right::after {
  right: -11px;
}

.widget-block.dropline-left::before,
.widget-block.dropline-right::before {
  content: "";
  position: absolute;
  width: 1px;
  height: calc(100% + 8px);
  border-left: solid 2px #4b4b4b;
  top: -4px;
}

.widget-block.dropline-left::before {
  left: -6px;
}

.widget-block.dropline-right::before {
  right: -6px;
}

.widget-block.size-1 {
  flex-basis: calc(25% - 7.5px);
}

.widget-block.size-2 {
  flex-basis: calc(50% - 5px);
}

.widget-block.size-3 {
  flex-basis: calc(75% - 2.5px);
}

.widget-block.size-4 {
  flex-basis: 100%;
}

.widget-block.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-basis: 1;
}

.widget-block.item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.widget-block-content {
  width: 100%;
  height: 100%;
  display: flex;
  /* justify-content: center; */
  /* align-items: center; */
  position: relative;
  /* text-align: center; */
  border-radius: 3px;
  flex-direction: column;
}

.widget-block.item .widget-block-content {
  background-color: var(--light);
  /* padding: 12px; */
  padding: 6px 8px;
  border: 2px solid var(--grey-dark);
  /* box-shadow: 1px 1px 2px -1px rgba(0, 0, 0, 0.3); */
}

.widget-block.item .widget-block-content span {
  pointer-events: none;
}

.widget-block.empty .widget-block-content {
  background-color: transparent;
  border: 1px dashed #8b8b8b;
}

.widget-block.item:hover .widget-block-content {
  background-color: var(--blue-light);
  border-color: var(--blue);
}

.widget-block.item:hover .widget-block-content .widget-block__buttonRemove {
  border-color: var(--blue);
}

.widget-block.item:hover .widget-block-content .widget-block__buttonRemoveIcon {
  background-color: var(--blue);
}

.widget-block__buttonDrag {
  position: absolute;
  right: 2rem;
  top: 0.5rem;
}

.widget-block__buttonRemove {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 21px;
  height: 21px;
  border-radius: 3px;
  border: 1px solid var(--grey-dark);
  background-color: #fff;
  transition: 300ms;
  align-self: flex-end;
}

.widget-block__buttonRemoveIcon {
  mask-image: url("/images/icon/icon-minus.svg");
  -webkit-mask-image: url("/images/icon/icon-minus.svg");
  mask-repeat: no-repeat;
  -webkit-mask-repeat: no-repeat;
  mask-position: center;
  -webkit-mask-position: center;
  mask-size: contain;
  -webkit-mask-size: contain;
  background-color: var(--grey-dark);
  width: 8px;
  height: 1px;
  display: inline-block;
}

.widget-block__buttonRemove:hover {
  background-color: var(--blue);
  border-color: var(--blue);
  box-shadow: inset 0 0 6px 0 rgba(0 0 0 / 25%), 0 0.5px 1px 0 rgba(0 0 0/ 29%);
}

.widget-block__buttonRemove:hover .widget-block__buttonRemoveIcon {
  background-color: #fff !important;
}

.widget-block__textContent {
  margin-top: 10px;
  display: flex;
  align-items: flex-start;
}

.widget-block__textContent span:last-child {
  padding-left: 10px;
}

.prevent-select {
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
</style>
