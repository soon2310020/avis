<template>
  <div class="object-filter">
    <a-popover placement="bottom" trigger="click" v-model="isShowFirstLayer">
      <a-button
        @click.prevent="showLayer"
        style="margin-right: 10px"
        class="dropdown_button"
      >
        {{ titleShow }}
        <a-icon type="caret-down" />
      </a-button>
      <div slot="content">
        <div class="first-layer-wrapper">
          <div class="first-layer" v-show="isShowFirstLayer">
            <a-menu style="border-right: none">
              <a-menu-item
                v-if="objectType.isAccessable"
                v-for="(objectType, index) in objectTypes"
                :key="index"
                class="object-menu-item"
                @click="selectObjectType(index)"
                >{{ objectType.label }}</a-menu-item
              >
            </a-menu>
          </div>
        </div>
      </div>
    </a-popover>
    <a-popover v-model="visible" placement="bottom" trigger="click">
      <div slot="content" style="padding: 8px 6px" class="dropdown-scroll">
        <div class="content-header" id="back-popup" @click="showFirstLayer">
          Back
        </div>
        <div class="content-body">
          <template>
            <a-input
              style="margin-bottom: 10px"
              placeholder="Input search text"
              v-model.trim="searchText"
            >
              <a-icon slot="prefix" type="search" />
            </a-input>
            <div style="max-height: 250px; overflow-y: auto">
              <template v-for="item in itemFiltered">
                <a-col style="padding: 7px" :key="item.id">
                  <p-radio v-model="selectedObject.id" :value="item.id">{{
                    item[selectedObject.type.labelField]
                  }}</p-radio>
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
  props: {},
  data() {
    return {
      visible: false,
      isShowFirstLayer: false,
      searchText: "",
      countObjectLoaded: 0,
      objectTypes: [
        {
          value: "ACCESS_FEATURE",
          label: "Access Feature",
          url: "/api/roles",
          items: [],
        },
        {
          value: "ACCESS_GROUP",
          label: "Access Group",
          url: "/api/roles/my",
          items: [],
        },
        {
          value: "CATEGORY",
          label: "Category",
          url: "/api/integration/category",
          items: [],
        },
        {
          value: "COMPANY",
          label: "Company",
          url: "/api/companies",
          items: [],
        },
        {
          value: "COUNTER",
          label: "Counter",
          url: "/api/counters",
          items: [],
        },
        {
          value: "LOCATION",
          label: "Location",
          url: "/api/locations",
          items: [],
        },
        {
          value: "PART",
          label: "Part",
          url: "/api/integration/part",
          items: [],
        },
        {
          value: "TERMINAL",
          label: "Terminal",
          url: "/api/terminals",
          items: [],
        },
        {
          value: "TOOLING",
          label: "Tooling",
          url: "/api/molds",
          items: [],
        },
        {
          value: "USER",
          label: "User",
          url: "/api/users",
          items: [],
        },
      ],
      selectedMenu: "PART",
      selectedObject: {
        type: {
          value: null,
          label: null,
          items: [],
        },
        id: null,
      },
      selectedObjectType: {},
      isFromInit: true,
    };
  },
  methods: {
    showFirstLayer() {
      this.isShowFirstLayer = true;
      this.visible = false;
      this.searchText = "";
    },
    showLayer() {
      if (this.selectedObject.id) {
        this.visible = true;
        this.isShowFirstLayer = false;
      } else {
        this.visible = false;
        this.isShowFirstLayer = true;
      }
    },
    isSelectedMenu(menu) {
      return this.selectedMenu === menu;
    },
    selectObjectType(index) {
      this.visible = true;
      this.isShowFirstLayer = false;
      this.selectedObject.type = this.objectTypes[index];
      this.selectedObject.id = null;
    },
    getObjectTypeData() {
      this.objectTypes.forEach((objectType) => {
        axios.get(objectType.url + "?size=99999999").then((response) => {
          this.countObjectLoaded++;
          // set items
          switch (objectType.value) {
            case "ACCESS_FEATURE":
              objectType.items = response.data.content.map((item) => item.role);
              break;
            case "ACCESS_GROUP":
              objectType.items = response.data;
              break;
            case "CATEGORY":
            case "PART":
              objectType.items = response.data.dataList;
              break;
            case "TERMINAL":
              objectType.items = response.data.content.map(
                (item) => item.terminal
              );
              break;
            default:
              objectType.items = response.data.content;
          }
          // set labelField
          switch (objectType.value) {
            case "COUNTER":
            case "TERMINAL":
            case "TOOLING":
              objectType.labelField = "equipmentCode";
              break;
            default:
              objectType.labelField = "name";
          }
        });
      });
    },
    getListObject() {
      axios
        .get("/api/topics/tags-item-of-user")
        .then((response) => {
          response.data.dataList.forEach((object) => {
            let objectType = this.objectTypes.filter(
              (objectType) => objectType.value === object.code
            )[0];
            if (objectType) {
              objectType.label = object.name;
              objectType.isAccessable = true;
            }
          });
        })
        .catch((error) => {
          console.log("error tags-topic-of-user", error);
        });
    },
    initDefaultValue() {
      let objectTypeValue = Common.getParameter("object-type");
      let objectIdParam = parseInt(Common.getParameter("object-id"));
      if (objectTypeValue) {
        let objectType = this.objectTypes.filter(
          (item) => item.value === objectTypeValue.toUpperCase()
        )[0];
        if (objectType) {
          this.selectedObject.type = objectType;
          if (objectIdParam) {
            this.selectedObject.id = objectIdParam;
            let selectedItem = this.selectedObject.type.items.filter(
              (item) => item.id === objectIdParam
            )[0];
            if (selectedItem) {
              this.searchText = selectedItem[objectType.labelField];
            }
          }
        }
      }
      console.log("this.selectedObject", this.selectedObject);
    },
  },

  computed: {
    titleShow: function () {
      if (this.selectedObject.id) {
        let item = this.selectedObject.type.items.filter(
          (item) => item.id === this.selectedObject.id
        )[0];
        return (
          this.selectedObject.type.label +
          " - " +
          item[this.selectedObject.type.labelField]
        );
      }
      console.log(
        "awwwwwwwwwwwwwwwwwwwwwwwwwwwwthis.selectedObject.type.value",
        this.selectedObject.type.value
      );
      if (this.selectedObject.type.value != null) {
        let label = null;
        this.$emit("selected-value", label);
        return this.selectedObject.type.label;
      }
      return "Select Item";
    },
    itemFiltered: function () {
      let limit = 20;
      if (!this.searchText)
        return this.selectedObject.type.items.filter(
          (item, index) => index < limit
        );
      return this.selectedObject.type.items
        .filter(
          (item) =>
            this.selectedObject.type &&
            this.selectedObject.type.labelField &&
            item[this.selectedObject.type.labelField] &&
            item[this.selectedObject.type.labelField]
              .toUpperCase()
              .includes(this.searchText.toUpperCase())
        )
        .filter((item, index) => index < limit);
    },
  },
  watch: {
    "selectedObject.type": function () {
      this.$emit("change-object", this.selectedObject);
    },
    "selectedObject.id": function (value) {
      if (!value) {
        return;
      }
      if (value != null) {
        this.$emit("selected-value", this.selectedObject.type.label);
      }
      this.$emit("change-object", this.selectedObject);
    },
    isShowFirstLayer: function (value) {
      if (value && this.visible) {
        this.isShowFirstLayer = false;
      }
    },
    countObjectLoaded: function (value) {
      if (value === this.objectTypes.length) {
        this.initDefaultValue();
      }
    },
  },
  mounted() {
    this.getObjectTypeData();
    this.getListObject();
  },
};
</script>

<style scoped>
.first-layer-wrapper {
  position: relative;
}

.first-layer {
  position: absolute;
  top: 0;
}
</style>
