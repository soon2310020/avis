<template>
  <div class="customization-modal">
    <a-tooltip color="white">
      <template slot="title">
        <div
          style="
            padding: 6px 8px;
            font-size: 13px;
            color: #4b4b4b;
            background: #fff;
            border-radius: 4px;
            box-shadow: 0px 0px 4px 1px #e5dfdf;
          "
        >
          <span v-text="resources['customize_columns']"></span>
          <div class="custom-arrow-tooltip"></div>
        </div>
      </template>
      <a
        id="customization-modal"
        @click="openModal()"
        href="javascript:void(0)"
        class="btn-custom btn-outline-custom-primary customize-btn"
        :class="{ 'visible-item': isMoreAction }"
      >
        <img
          style="margin-bottom: 3px"
          src="/images/icon/customization-icon.svg"
        />
      </a>
    </a-tooltip>
    <div
      style="z-index: 4999"
      id="op-customization-popup"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
        <div
          v-click-outside="saveOutside"
          style="box-shadow: 0 0 3px #00000029"
          class="modal-content"
        >
          <div class="modal-body">
            <div class="modal-title">
              <div
                class="head-line"
                style="
                  position: absolute;
                  background: #52a1ff;
                  height: 8px;
                  border-radius: 6px 6px 0 0;
                  top: 0;
                  left: 0;
                  width: 100%;
                "
              ></div>
              <span class="span-title">
                <span v-text="resources['customize_your_columns']"></span>
              </span>
              <span
                class="close-button"
                style="
                  font-size: 25px;
                  display: flex;
                  align-items: center;
                  height: 17px;
                  cursor: pointer;
                "
                @click="save"
                aria-hidden="true"
              >
                <span class="t-icon-close"></span>
              </span>
            </div>
            <div class="modal-content-order">
              <div class="top-side">
                <div class="top-side-left">
                  <span v-text="resources['available_columns']"></span>
                </div>
                <div class="top-side-right">
                  <span
                    ><span v-text="resources['selected_columns']"></span
                  ></span>
                  <div class="remove-all-customize-zone">
                    <a
                      @click="resetDefault()"
                      href="javascript:void(0)"
                      id="remove-all-customize"
                      class="invite-link"
                      v-text="resources['reset_to_default']"
                    >
                    </a>
                  </div>
                </div>
              </div>
              <div class="modal-content-order-inside">
                <div class="right-side">
                  <div class="search">
                    <a-input
                      class="custom-input-companies"
                      :placeholder="resources['search_available_columns']"
                      v-model="customizeInput"
                    >
                      <svg
                        slot="prefix"
                        xmlns="http://www.w3.org/2000/svg"
                        width="17.999"
                        height="18.002"
                        viewBox="0 0 17.999 18.002"
                      >
                        <path
                          id="Icon_awesome-search"
                          data-name="Icon awesome-search"
                          d="M17.754,15.564l-3.505-3.505a.843.843,0,0,0-.6-.246h-.573a7.309,7.309,0,1,0-1.266,1.266v.573a.843.843,0,0,0,.246.6l3.505,3.505a.84.84,0,0,0,1.192,0l.995-.995a.848.848,0,0,0,0-1.2ZM7.312,11.812a4.5,4.5,0,1,1,4.5-4.5A4.5,4.5,0,0,1,7.312,11.812Z"
                          fill="#9d9d9d"
                        />
                      </svg>
                      <div
                        v-if="customizeInput !== ''"
                        @click="customizeInput = ''"
                        class="invite-link"
                        style="font-size: 12px; color: #3585e5; cursor: pointer"
                        slot="suffix"
                      >
                        reset
                      </div>
                    </a-input>
                  </div>
                  <div style="position: relative">
                    <div class="right-side-content">
                      <div
                        v-for="(column, index) in customizeFiltered"
                        v-bind:key="index + 'drag'"
                        :id="'column ' + column.label"
                        class="right-side-item"
                      >
                        <div
                          :class="{ enabled: column.enabled }"
                          class="list-item"
                        >
                          {{ column.label }}
                        </div>
                        <div
                          :class="{ 'visible-item': column.enabled }"
                          @click="selectColumn(column)"
                          class="minus-item"
                        >
                          <svg
                            class="minus-item-default"
                            xmlns="http://www.w3.org/2000/svg"
                            width="9.843"
                            height="9.843"
                            viewBox="0 0 9.843 9.843"
                          >
                            <g
                              id="Group_8883"
                              data-name="Group 8883"
                              transform="translate(0.861 0.862)"
                            >
                              <rect
                                id="Rectangle_10"
                                data-name="Rectangle 10"
                                width="1.641"
                                height="9.843"
                                transform="translate(8.982 3.24) rotate(90)"
                                fill="#9d9d9d"
                              />
                              <rect
                                id="Rectangle_11"
                                data-name="Rectangle 11"
                                width="1.641"
                                height="9.843"
                                transform="translate(4.881 8.982) rotate(180)"
                                fill="#9d9d9d"
                              />
                            </g>
                          </svg>
                          <svg
                            class="minus-item-hover"
                            xmlns="http://www.w3.org/2000/svg"
                            width="9.843"
                            height="9.843"
                            viewBox="0 0 9.843 9.843"
                          >
                            <g
                              id="Group_8883"
                              data-name="Group 8883"
                              transform="translate(0.861 0.862)"
                            >
                              <rect
                                id="Rectangle_10"
                                data-name="Rectangle 10"
                                width="1.641"
                                height="9.843"
                                transform="translate(8.982 3.24) rotate(90)"
                                fill="#3491ff"
                              />
                              <rect
                                id="Rectangle_11"
                                data-name="Rectangle 11"
                                width="1.641"
                                height="9.843"
                                transform="translate(4.881 8.982) rotate(180)"
                                fill="#3491ff"
                              />
                            </g>
                          </svg>
                        </div>
                      </div>
                    </div>
                    <div
                      id="maximum-zone"
                      :class="
                        listSelectedLength >= maxColumns ? 'isShow' : 'isOff'
                      "
                      class="maximum-zone"
                    ></div>
                    <div
                      :class="{ isShow: listSelectedLength >= maxColumns }"
                      class="maximum-text"
                    >
                      <span
                        >Maximum of {{ maxColumns }} columns are added. </span
                      ><br />
                      <span>Remove items from the right to add more </span>
                    </div>
                  </div>
                </div>
                <div class="left-side">
                  <div style="display: flex" class="left-side-content">
                    <div style="overflow: hidden">
                      <div
                        v-for="index in maxColumns"
                        class="customize-index-zone"
                      >
                        <div class="customize-index">
                          {{ index }}
                        </div>
                      </div>
                    </div>
                    <div class="label-group">
                      <div
                        v-for="(column, index) in listMandatory"
                        :key="index + 'mandatory'"
                      >
                        <div>
                          <div
                            class="selected-item-wrap"
                            :class="{ 'gray-bg': column.mandatory }"
                          >
                            <div class="selected-item">
                              <div
                                :class="{ 'visible-item': column.mandatory }"
                                class="drag-icon"
                              >
                                <img src="/images/icon/drag-icon.svg" alt="" />
                              </div>
                              <span>{{ column.label }}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                      <draggable
                        :list="listSelected"
                        draggable=".nonMandatory"
                        @start="startMove"
                        @end="endMove"
                      >
                        <div
                          v-for="(column, index) in listSelected"
                          :class="{
                            nonMandatory:
                              !column.mandatory && column !== 'empty',
                            'ghost-customize': column.isClone,
                          }"
                          :key="index + 'drag'"
                        >
                          <div v-if="column !== 'empty'">
                            <div
                              class="selected-item-wrap"
                              :class="{ 'gray-bg': column.mandatory }"
                            >
                              <div class="selected-item">
                                <div
                                  :class="{ 'visible-item': column.mandatory }"
                                  class="drag-icon"
                                >
                                  <img
                                    src="/images/icon/drag-icon.svg"
                                    alt=""
                                  />
                                </div>
                                <span>{{ column.label }}</span>
                              </div>
                            </div>
                            <div
                              :class="{ 'visible-item': column.mandatory }"
                              @click.stop="
                                unSelectColumn(
                                  index,
                                  column,
                                  'column ' + column.label
                                )
                              "
                              class="minus-item"
                            >
                              <svg
                                class="minus-item-default"
                                xmlns="http://www.w3.org/2000/svg"
                                width="9.844"
                                height="1.641"
                                viewBox="0 0 9.844 1.641"
                              >
                                <g
                                  id="Group_8891"
                                  data-name="Group 8891"
                                  transform="translate(0.861 -3.24)"
                                >
                                  <rect
                                    id="Rectangle_10"
                                    data-name="Rectangle 10"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(8.982 3.24) rotate(90)"
                                    fill="#ddd"
                                  />
                                  <rect
                                    id="Rectangle_11"
                                    data-name="Rectangle 11"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(-0.861 4.88) rotate(-90)"
                                    fill="#ddd"
                                  />
                                </g>
                              </svg>
                              <svg
                                class="minus-item-hover"
                                xmlns="http://www.w3.org/2000/svg"
                                width="9.844"
                                height="1.641"
                                viewBox="0 0 9.844 1.641"
                              >
                                <g
                                  id="Group_8891"
                                  data-name="Group 8891"
                                  transform="translate(0.861 -3.24)"
                                >
                                  <rect
                                    id="Rectangle_10"
                                    data-name="Rectangle 10"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(8.982 3.24) rotate(90)"
                                    fill="#3491ff"
                                  />
                                  <rect
                                    id="Rectangle_11"
                                    data-name="Rectangle 11"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(-0.861 4.88) rotate(-90)"
                                    fill="#3491ff"
                                  />
                                </g>
                              </svg>
                            </div>
                          </div>
                          <div v-else class="empty-customize">
                            <div class="selected-item-wrap">
                              <div class="selected-item"></div>
                            </div>
                            <div class="visible-item minus-item">
                              <svg
                                class="minus-item-default"
                                xmlns="http://www.w3.org/2000/svg"
                                width="9.844"
                                height="1.641"
                                viewBox="0 0 9.844 1.641"
                              >
                                <g
                                  id="Group_8891"
                                  data-name="Group 8891"
                                  transform="translate(0.861 -3.24)"
                                >
                                  <rect
                                    id="Rectangle_10"
                                    data-name="Rectangle 10"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(8.982 3.24) rotate(90)"
                                    fill="#ddd"
                                  />
                                  <rect
                                    id="Rectangle_11"
                                    data-name="Rectangle 11"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(-0.861 4.88) rotate(-90)"
                                    fill="#ddd"
                                  />
                                </g>
                              </svg>
                              <svg
                                class="minus-item-hover"
                                xmlns="http://www.w3.org/2000/svg"
                                width="9.844"
                                height="1.641"
                                viewBox="0 0 9.844 1.641"
                              >
                                <g
                                  id="Group_8891"
                                  data-name="Group 8891"
                                  transform="translate(0.861 -3.24)"
                                >
                                  <rect
                                    id="Rectangle_10"
                                    data-name="Rectangle 10"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(8.982 3.24) rotate(90)"
                                    fill="#3491ff"
                                  />
                                  <rect
                                    id="Rectangle_11"
                                    data-name="Rectangle 11"
                                    width="1.641"
                                    height="9.843"
                                    transform="translate(-0.861 4.88) rotate(-90)"
                                    fill="#3491ff"
                                  />
                                </g>
                              </svg>
                            </div>
                          </div>
                        </div>
                      </draggable>
                    </div>
                  </div>
                  <div class="customize-footer">
                    <div class="hint">
                      <img
                        style="margin-right: 5px; width: 15px"
                        src="/images/icon/light.png"
                      />
                      <span>Drag items to arrange the order</span>
                    </div>
                    <a
                      @click="saveSettings"
                      href="javascript:void(0)"
                      id="save-setting"
                      class="btn-custom btn-custom-primary"
                    >
                      <div style="font-size: 11px">Save Settings</div>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "customization-modal",
  props: {
    resources: Object,
    allColumnsList: {
      type: Array,
      default: () => [],
    },
    pageType: {
      type: String,
      default: "TOOLING_DASHBOARD",
    },
    isMoreAction: {
      type: Boolean,
      default: false,
    },
    maxColumns: {
      type: Number,
      default: 10,
    },
  },
  data() {
    return {
      dragging: false,
      myArray: [],
      listSelected: [],
      listMandatory: [],
      list: [],
      customizeInput: "",
      isModalOpen: false,
      emptyList: [
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
        "empty",
      ],
      // trigger: 0,
    };
  },
  watch: {
    allColumnsList: {
      handler(newVal) {
        if (newVal.length) {
          const __listSlots = [];
          for (let index = 0; index < this.maxColumns; index++) {
            __listSlots.push("empty");
          }
          this.listMandatory = newVal.filter(
            (item) => item.enabled && !item.hiddenInToggle && item.mandatory
          );
          console.log("this.listMandatory", this.listMandatory);
          this.emptyList = __listSlots;
          this.listSelected = newVal
            .filter(
              (item) => item.enabled && !item.hiddenInToggle && !item.mandatory
            )
            .concat(this.emptyList)
            .slice(0, this.maxColumns - this.listMandatory.length);
          console.log("this.listSelected", this.listSelected);
          this.list = JSON.parse(
            JSON.stringify(
              this.allColumnSort.filter((item) => !item.hiddenInToggle)
            )
          );
          console.log("this.list", this.list);
        }
      },
      immediate: true,
    },
  },
  computed: {
    listSelectedLength() {
      const __checkedList = [...this.listSelected, ...this.listMandatory];
      return __checkedList.filter((res) => res !== "empty" && !res.isClone)
        .length;
    },
    allColumnSort() {
      return this.allColumnsList.slice().sort(function (a, b) {
        return a.position - b.position;
        // return a.id - b.id;
      });
    },
    customizeFiltered() {
      const str = this.customizeInput.trim();
      if (!str) {
        return this.list.sort((firstItem, secondItem) =>
          firstItem.label
            .toUpperCase()
            .localeCompare(secondItem.label.toUpperCase())
        );
      }

      const searchData = this.list
        .filter(
          (o) =>
            o && o.label && o.label.toUpperCase().includes(str.toUpperCase())
        )
        .sort((firstItem, secondItem) =>
          firstItem.label
            .toUpperCase()
            .localeCompare(secondItem.label.toUpperCase())
        );
      return searchData;
    },
  },
  methods: {
    startMove(event) {
      const index = event.oldIndex;
      const item = this.listSelected[index];
      this.listSelected.splice(index + 1, 0, {
        ...item,
        isClone: true,
      });
      this.dragging = true;
    },
    endMove(event) {
      this.listSelected = this.listSelected.filter((layer) => !layer.isClone);
      this.dragging = false;
    },
    removeAll() {
      const self = this;
      const el = document.getElementById("remove-all-customize");
      el.classList.add("primary-animation-invite");
      setTimeout(() => {
        el.classList.remove("primary-animation-invite");
        self.listSelected = self.allColumnsList
          .filter((item) => item.mandatory)
          .concat(self.emptyList)
          .slice(0, this.maxColumns);
        this.list.forEach((item) => {
          if (!item.mandatory) {
            item.enabled = false;
          }
        });
      }, 700);
    },

    resetDefault() {
      const self = this;
      const el = document.getElementById("remove-all-customize");
      el.classList.add("primary-animation-invite");
      setTimeout(() => {
        el.classList.remove("primary-animation-invite");
        self.listMandatory = self.allColumnsList
          .filter((item) => item.mandatory)
          .sort((a, b) => a.defaultPosition - b.defaultPosition);

        self.listSelected = self.allColumnsList
          .filter((item) => !item.mandatory && item.defaultSelected)
          .concat(self.emptyList)
          .slice(0, self.maxColumns - self.listMandatory.length)
          .sort((a, b) => a.defaultPosition - b.defaultPosition);

        console.log(
          "Here is the list sorted by oderid, ",
          self.listSelected,
          self.listMandatory
        );
        this.list.forEach((item) => {
          console.log("this is each item", item);
          if (!item.mandatory) {
            item.enabled = false;
          }
          if (item.defaultSelected) {
            item.enabled = true;
          }
        });
      }, 700);
      console.log("this is the list:after", this.list);
    },
    openModal() {
      const self = this;
      const el = document.getElementById("customization-modal");
      el.classList.add("animation-secondary");
      setTimeout(() => {
        el.classList.remove("animation-secondary");
        self.customizeInput = "";
        self.listSelected = self.allColumnsList
          .filter(
            (item) => item.enabled && !item.hiddenInToggle && !item.mandatory
          )
          .concat(self.emptyList)
          .slice(0, self.maxColumns);
        self.list = JSON.parse(
          JSON.stringify(
            self.allColumnSort.filter((item) => !item.hiddenInToggle)
          )
        );
        this.isModalOpen = true;
        $("#op-customization-popup").modal("show");
      }, 700);
    },
    saveSettings() {
      const el = document.getElementById("save-setting");
      el.classList.add("primary-animation");
      const self = this;
      setTimeout(function () {
        self.save();
        el.classList.remove("primary-animation");
      }, 700);
    },
    save() {
      let newArr = JSON.parse(JSON.stringify(this.list));
      console.log("from save", this.list);
      newArr.forEach((res) => {
        console.log("selected list in resetdefault", this.listSelected);
        this.listMandatory.forEach((res2, index) => {
          if (res.label === res2.label) {
            res.position = index;
          }
        });
        this.listSelected.forEach((res2, index) => {
          if (res.label === res2.label) {
            res.position = index + this.listMandatory.length;
          }
        });
      });
      let positionArr = newArr.filter((o) => o.position !== undefined);
      let noPositionArr = newArr.filter((o) => o.position === undefined);
      positionArr.sort(function (a, b) {
        return a.position - b.position;
      });
      let newArrSort = [...positionArr, ...noPositionArr];
      let combineSelectedAndMandatoryArray = JSON.parse(
        JSON.stringify(this.listMandatory)
      );
      combineSelectedAndMandatoryArray.push(this.listSelected);
      this.$emit("save", combineSelectedAndMandatoryArray, newArrSort);
      this.$forceUpdate();
      this.isModalOpen = false;
      $("#op-customization-popup").modal("hide");
    },
    saveOutside() {
      if (this.isModalOpen) {
        let newArr = JSON.parse(JSON.stringify(this.list));
        newArr.forEach((res) => {
          this.listMandatory.forEach((res2, index) => {
            if (res.label === res2.label) {
              res.position = index;
            }
          });
          this.listSelected.forEach((res2, index) => {
            if (res.label === res2.label) {
              res.position = index + this.listMandatory.length;
            }
          });
        });
        let positionArr = newArr.filter((o) => o.position !== undefined);
        let noPositionArr = newArr.filter((o) => o.position === undefined);
        positionArr.sort(function (a, b) {
          return a.position - b.position;
        });
        let newArrSort = [...positionArr, ...noPositionArr];
        let combineSelectedAndMandatoryArray = JSON.parse(
          JSON.stringify(this.listMandatory)
        );
        combineSelectedAndMandatoryArray.push(this.listSelected);
        this.$emit("save", combineSelectedAndMandatoryArray, newArrSort);
        this.$forceUpdate();
        this.isModalOpen = false;
        $("#op-customization-popup").modal("hide");
      }
    },
    selectColumn(column) {
      console.log("selected column", column);
      // this.trigger++
      if (!column.enabled) {
        // this.listSelected[this.listSelected.indexOf("empty")] = column;
        // this.$forceUpdate()
        this.listSelected.splice(this.listSelected.indexOf("empty"), 1, column);
        this.list.forEach((item) => {
          if (item.field === column.field) {
            item.enabled = true;
          }
        });
      }
    },
    unSelectColumn(index, column, id) {
      // this.trigger++
      // this.listSelected[index] = "empty"
      this.listSelected.splice(index, 1, "empty");
      this.list.forEach((item) => {
        if (item.field === column.field) {
          item.enabled = false;
        }
      });
      // this.$forceUpdate()
      const elem = document.getElementById(id);
      elem.scrollIntoView({ behavior: "smooth" });
    },
  },
};
</script>
<style>
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
.right-side .ant-input-prefix {
  height: 100%;
  left: 0 !important;
  background: rgb(240 243 245 / 31%);
  padding: 0 5px !important;
  border-radius: 4px 0 0 4px;
}
.right-side .ant-input {
  padding-left: 40px !important;
  padding-right: 45px !important;
  border: unset !important;
}
.right-side .custom-input-companies {
  border: 1px solid #3491ff;
  border-radius: 4px;
}
</style>
<style scoped>
.empty-customize .selected-item-wrap {
  border: 1px dashed #dddddd;
  cursor: default;
}
.empty-customize .selected-item {
  background-color: #fff !important;
}
.empty-customize .customize-index {
  border: 1px dashed #dddddd;
}
.customize-index-zone {
  width: 23px;
  height: 23px;
  margin-right: 3px;
  margin-bottom: 9px;
}
.customize-index {
  border: 1px solid #dddddd;
  border-radius: 2px;
  width: 23px;
  height: 23px;
  color: #262931;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 4px;
  position: absolute;
  left: -6px;
}
.right-side-content ::-webkit-scrollbar {
  width: 3px;
  height: 7px;
}

/* Track */
.right-side-content ::-webkit-scrollbar-track {
  background: transparent;
}

/* Handle */
.right-side-content ::-webkit-scrollbar-thumb {
  background: #9d9d9d !important;
  border-radius: 10px;
}

/* Handle on hover */
.right-side-content ::-webkit-scrollbar-thumb:hover {
  background: #9d9d9d !important;
}
.invite-link:focus {
  text-decoration: unset;
}
.primary-animation-invite {
  color: #006cff !important;
  text-decoration: none !important;
}
.invite-link {
  /*color: #3491ff;*/
  color: #3491ff;
  cursor: pointer;
  text-decoration: none;
}

.invite-link:hover {
  color: #0066df !important;
}
.customize-footer {
  margin-top: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.maximum-text {
  position: absolute;
  right: 50%;
  top: 50%;
  transform: translate(50%, -50%);
  color: #3491ff;
  font-size: 11px;
  width: 100%;
  text-align: center;
  z-index: -1;
  transition: visibility 0s 1s, opacity 1s linear;
}
.maximum-text.isShow {
  z-index: 1;
}
.maximum-zone {
  position: absolute;
  right: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  transition: all 0.6s;
}
.maximum-zone.isShow {
  opacity: 0.8;
  z-index: 1;
  animation: openModal 0.6s;
}

.maximum-zone.isOff {
  opacity: 0;
  z-index: -1;
  animation: closeModal 0.6s;
}

@keyframes openModal {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 0.8;
  }
}

@keyframes closeModal {
  0% {
    opacity: 0.8;
  }
  100% {
    opacity: 0;
  }
}
.label-group {
  height: 312px;
  width: calc(100% - 26px);
  overflow: hidden;
}
.gray-bg {
  border: unset !important;
}
.gray-bg .selected-item {
  background: #f2f2f2 !important;
}
.visible-item {
  visibility: hidden;
}
.left-side,
.right-side {
  width: calc(50% - 15px);
}
.right-side-content .right-side-item {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.left-side-content > div > div > div {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  position: relative;
  width: 100%;
}
.left-side-content > div > div > div > div:not(.gray-bg) {
  width: 100%;
  display: flex;
  align-items: center;
}
.enabled {
  opacity: 15%;
  border: 1px solid #9d9d9d;
  color: #262931;
}
.right-side-content {
  background: #f5f8ff;
  padding: 10px 19px;
  height: 270px;
  overflow-x: auto;
  position: relative;
}
.list-item {
  color: #262931;
  background: #fff;
  border: 1px solid #9d9d9d;
  text-align: left;
  font-size: 16px;
  border-radius: 2px;
  padding: 2px 12px;
  height: 22px;
  line-height: 100%;
  width: calc(100% - 27.7px);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.selected-item {
  padding: 2px 10px;
  display: flex;
  align-items: center;
  box-shadow: none;
  background: #d5e8ff;
  /*border: 1px solid #3491FF;*/
  text-align: left;
  font-size: 16px;
  line-height: 100%;
  height: 22px;
  color: #262931;
}
.selected-item span {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 300px;
}
.selected-item-wrap {
  width: calc(100% - 25px);
  border: 1px solid rgb(52, 145, 255);
  padding: 1px;
  border-radius: 1px;
  cursor: pointer;
}
.selected-item .drag-icon {
  margin-right: 11px;
  cursor: pointer;
}
.minus-item:hover {
  background: #d5e8ff;
  border: 1px solid #3491ff;
}
.minus-item-hover {
  display: none;
}
.minus-item-default {
  display: block;
}
.minus-item:hover .minus-item-hover {
  display: block;
}
.minus-item:hover .minus-item-default {
  display: none;
}
.right-side-content .minus-item {
  margin-left: 5.7px;
}
.left-side-content .minus-item {
  padding-left: 1px;
}
.minus-item {
  width: 22px;
  height: 22px;
  border: 1px solid #9d9d9d;
  background: #fff;
  border-radius: 2px;
  margin-left: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-tabs {
  border: unset !important;
  position: absolute;
  top: 11.5px;
  width: 100%;
}
.nav-tabs .nav-link {
  color: #4b4b4b !important;
}
.nav-tabs .nav-link.active {
  border: 1px solid #c1c7cd !important;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
}
.nav-tabs .nav-link:not(.active):hover {
  border-color: unset;
}
.modal-content {
  /*min-width: 800px;*/
  /*height: 400px;*/
  margin: auto;
  background: unset;
  border: unset;
  box-shadow: unset !important;
}
.modal-content-order {
  /*padding: 1px 4px;*/
  /*border: 1px solid #C1C7CD;*/
  background: #ffffff;
  /*border-radius: 3px;*/
}
.modal-content-order-inside {
  padding: 0 30px 22px 31px;
  /*box-shadow: 0px 3px 6px rgb(0 0 0 / 16%);*/
  position: relative;
  min-height: 200px;
  font-size: 14px;
  display: flex;
  /*align-items: center;*/
  justify-content: space-between;
}
.modal-content-order-inside .hint {
  color: #4b4b4b;
  font-size: 14px;
}
.right-side .search {
  height: 34px;
  margin-bottom: 7px;
}
.modal-body {
  padding: 0;
  border-radius: 6px;
}
.modal-title {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 31px;
  border-radius: 6px 6px 0 0;
}
.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}
.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: 0;
  width: 100%;
}
.modal-title .span-title {
  color: #4b4b4b;
  font-weight: bold;
  font-size: 16px;
  line-height: 100%;
}
.received-items {
  margin-top: 39px;
  height: 470px;
  overflow-x: hidden;
}
.received-item {
  border: 1px solid #c1c7cd;
  box-shadow: 0px 3px 6px rgb(0 0 0 / 16%);
  padding: 5.3px 5.9px;
  border-radius: 2px;
  margin-bottom: 15.6px;
}
.received-item-header {
  background: #f0f3f5;
  height: 25px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  line-height: 100%;
}
.received-item-body {
  padding: 3px 7.1px;
}
.received-item-body_row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13.5px 16px 13.5px 12px;
}
.received-item-body_row:nth-child(even) {
  background: rgb(241 244 249 / 30%);
}
.received-item-column.link-order {
  color: #0075ff;
}
.received-item-column.date-order {
  color: #4b4b4b;
}
.received-item-column {
  width: 43%;
}
.received-item-column:last-child {
  width: 14%;
}
.nav-tabs .nav-link:not(.active):hover {
  border: 1px solid transparent;
}
.created-order .received-item-body {
  padding: 12px 24px;
}
.created-order .create-item-body_row-first > div {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.created-order .create-item-body_row-first {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.create-item-body_row-second-item:nth-child(4),
.create-item-body_row-second-item:nth-child(5),
.create-item-body_row-second-item:nth-child(6) {
  margin-bottom: 0 !important;
}
.created-order .create-item-body_row-second .create-item-body_row-second-item {
  width: calc(33.33% - 12.6px);
  padding: 4px 13.3px 4px 8px;
  border: 1px solid rgb(112, 112, 112, 26%);
  color: #a2a2a2;
  /*margin-right: 19px;*/
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.created-order
  .create-item-body_row-second
  .create-item-body_row-second-item
  .order-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #fff;
  border-radius: 999px;
  font-size: 16px;
  color: #4b4b4b;
  font-weight: 500;
}
.create-item-body_row-second-item.selected {
  border: 1px solid #3491ff !important;
  background: rgba(172, 210, 255, 50%);
  border-radius: 3px;
  color: #4b4b4b !important;
}
.created-order .create-item-body_row-second {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  padding: 12px 26px;
}
.avatar-zone {
  display: flex;
  align-items: center;
  width: unset !important;
  padding: 0 !important;
  /*overflow: scroll;*/
  background-image: unset !important;
}
.avatar-item {
  width: 25px;
  height: 25px;
  padding: 1px;
  font-weight: 400;
  font-size: 14px;
  background: #aeacf9;
  border-radius: 99%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: capitalize;
}
.avatar-item-w:not(:last-child) {
  margin-right: 3px;
}
.users-truncated {
  position: absolute;
  right: -9px;
  top: -3px;
  background: #4b4b4b;
  color: #fff;
  width: 14px;
  height: 14px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
}
.inactive-order {
  background: #c4c4c4 !important;
  border: 1px solid rgb(196, 196, 196) !important;
  color: #fff !important;
}
/* width */
.received-items::-webkit-scrollbar {
  width: 3px;
  height: 7px;
}

/* Track */
.received-items::-webkit-scrollbar-track {
  background: transparent;
}

/* Handle */
.received-items::-webkit-scrollbar-thumb {
  background: #707070 !important;
  border-radius: 10px;
}

/* Handle on hover */
.received-items ::-webkit-scrollbar-thumb:hover {
  background: #707070 !important;
}
.page-link {
  background: #f3f3f3;
  border-color: #fff;
  color: #707070;
  font-size: 9px;
  padding: 4px 6px;
}
.active .page-link {
  background: #595959;
  border-color: #fff;
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}
.page-item:last-child .page-link {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}
.page-link:hover {
  z-index: 0;
  background: #f3f3f3;
  border-color: #fff;
}
.active .page-link:hover {
  z-index: 0;
  background: rgb(89, 89, 89);
  border-color: #fff;
  color: #fff;
}
.pagination {
  justify-content: flex-end;
  margin-bottom: 0;
}
.create-pagination {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 5px 24px 20px 24px;
  background: #fff;
}
.received-item-body-popup {
  padding: 12px 24px;
  height: 174px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.received-item-body-popup b {
  font-size: 14px;
  color: #4b4b4b;
}
.received-item-body-popup.received-success {
  font-size: 14px;
  color: #3491ff;
  font-weight: bold;
}
.top-side {
  display: flex;
  justify-content: space-between;
  padding: 16px 30px 0 31px;
  position: relative;
  font-size: 14px;
}
.top-side .top-side-left,
.top-side .top-side-right {
  width: 50%;
  display: flex;
  justify-content: flex-start;
  font-size: 14px;
  color: #020202;
  margin-bottom: 10px;
  position: relative;
}
.top-side .top-side-right {
  margin-left: 30px;
}
.remove-all-customize-zone {
  position: absolute;
  right: 0;
  top: 0;
}
.customize-btn {
  background: rgb(214 218 222 / 37%);
  border-color: transparent;
}
.customize-btn:hover {
  background: #daeeff;
  border-color: #3585e5;
}
.animation-secondary {
  animation: outline-active-2 0.8s;
  animation-iteration-count: 1;
  animation-direction: alternate;
}
@keyframes outline-active-2 {
  0% {
  }
  33% {
    color: #3491ff;
    box-shadow: 0 0 0 1px #3491ff;
    border-color: #3491ff;
    background-color: #daeeff;
  }
  66% {
    color: #3491ff;
    box-shadow: 0 0 0 1px #3491ff;
    border-color: #3491ff;
    background-color: #daeeff;
  }
  100% {
  }
}
</style>
