<template>
  <base-dialog
    :title="getModalTitle"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div class="" style="height: 560px">
      <div class="container content-group">
        <div class="row">
          <div class="col-md-6 tab-group-title active" style="background-color: #FFFFFF">
            <p v-text="resources['area_name'] || 'Area Name'"></p>
            <span
              class="__icon-sort"
              :class="areaNameSortType"
              @click="sort('name')"
            ></span>
          </div>
          <div class="col-md-6 tab-group-title" style="background-color: #FFFFFF">
            <p v-text="resources['area_type'] || 'Area Type'"></p>
            <span
              class="__icon-sort"
              :class="areaTypeSortType"
              @click="sort('areaType')"
            ></span>
          </div>
        </div>
        <div
          v-for="(area, index) in listAreas"
          :key="index"
          class="row"
          :class="{
            'row--white': index % 2 !== 0,
            'row--grey': index % 2 === 0,
          }"
          style="padding: 11.5px 0"
        >
          <div class="col-md-6" style="padding-left: 0px; padding-right: 0px">
            <div class="tab-group-item">
              <div class="col-md-8 d-flex align-center">
                <span style="margin-right: 8px">{{ index + 1 }}.</span>
                <div>
                  <input
                    type="text"
                    class="form-control"
                    :class="{'is-invalid' : !area.valid}"
                    v-model="area.name"
                    :disabled="!area.active"
                    placeholder="Enter area name"
                    @input="(e) => handleAreaNameInput(area, index, e)"
                  />
                  <small v-show="!area.valid" class="text--red">{{resources['please_remove_the_special_characters']}}</small>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6" style="padding-left: 0; padding-right: 0">
            <div class="tab-group-item">
              <div class="col-md-8">
                <custom-dropdown
                  :list-item="listAreaTypes"
                  :title-field="'name'"
                  :default-title="resources['select_area'] || 'Select Area'"
                  :default-item="area.areaTypeObject"
                  :disabled="!area.active"
                  :btn-class="!area.active ? 'bg-transparent' : ''"
                  @change="(type) => handleChangeArea(area, type, index)"
                ></custom-dropdown>
              </div>
              <div class="col-md-4 d-flex justify-end">
                <a-tooltip v-if="!area.active" color="white">
                  <template slot="title">
                    <div
                      style="
                        padding: 6px 8px;
                        font-size: 13px;
                        color: #3491ff;
                        background: #fff;
                        border-radius: 4px;
                        height: 32px;
                        box-shadow: 0 0 4px 1px #e5dfdf;
                      "
                    >
                      Edit
                      <div class="custom-arrow-tooltip"></div>
                    </div>
                  </template>
                  <a
                    @click="handleEditArea(area, index)"
                    href="javascript:void(0)"
                    class="btn-custom btn-outline-custom-primary action-btn"
                    style="width: unset; margin-right: 14px;"
                  >
                    <div class="icon-pencil--grey"></div>
                  </a>
                </a-tooltip>
                <a-tooltip color="white">
                  <template slot="title">
                    <div
                      style="
                        padding: 6px 8px;
                        font-size: 13px;
                        color: #3491ff;
                        background: #fff;
                        border-radius: 4px;
                        height: 32px;
                        box-shadow: 0px 0px 4px 1px #e5dfdf;
                      "
                    >
                      Delete
                      <div class="custom-arrow-tooltip"></div>
                    </div>
                  </template>
                  <a
                    @click="handleDeleteArea(area, index)"
                    href="javascript:void(0)"
                    class="btn-custom btn-outline-custom-primary action-btn"
                  >
                    <div class="icon-trashbin--grey"></div>
                  </a>
                </a-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div
          class="row"
          :class="{
            'row--white': listAreas.length % 2 !== 0,
            'row--grey': listAreas.length % 2 === 0,
          }"
          style="height: 45px"
        >
          <div class="col-md-8 d-flex align-center">
            <span class="custom-hyper-link" @click="handleAddNewPlantArea"
              >+ {{ resources["add_plant_area"] }}</span
            >
          </div>
        </div>
      </div>
      <div class="absolute d-flex justify-end" style="bottom: 20px; right: 52px">
        <base-button
          level="primary"
          type="save"
          :disabled="disabled"
          @click="handleSubmit"
          >{{ resources["save"] }}</base-button
        >
      </div>
    </div>
  </base-dialog>
</template>

<script>
const AREA_TYPES = {
  MAINTENANCE_AREA: { name: "Maintenance", value: "MAINTENANCE_AREA" },
  PRODUCTION_AREA: { name: "Production", value: "PRODUCTION_AREA" },
  WAREHOUSE: { name: "Warehouse", value: "WAREHOUSE" },
};
const NEW_AREA = {
  name: "",
  areaType: null,
  areaTypeObject: null,
  active: true,
  valid: true,
};
module.exports = {
  components: {
    "custom-dropdown": httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => {},
    },
    plantId: {
      type: [String, Number],
    },
    visible: {
      type: Boolean,
      default: false,
    },
  },
  setup(props, ctx) {
    const areaNameSortType = ref("asc");
    const areaTypeSortType = ref("asc");
    const listAreas = ref([]);
    const listAreaTypes = ref([]);
    const currentSortType = ref('')

    // COMPUTED //
    const getModalTitle = computed(() => {
      return props.resources["plant_areas"]
        ? props.resources["plant_areas"]
        : "Plant Areas";
    });

    const disabled = computed(() => {
      console.log('disabled 1', listAreas.value)
      if (listAreas.value.length) {
        const validItems = listAreas.value.filter((item) => item.name && item.areaType);
        console.log('disabled', validItems, listAreas.value)
        return validItems.length !== listAreas.value.length;
      }
      return false;
    });

    // WATCH //
    watch(
      () => props.visible,
      async (newVal) => {
        console.log("watch props.visible", newVal);
        if (newVal) {
          init();
          await getAreasByPlantId();
        }
      }
    );

    // METHOD //

    const sort = (sortType) => {
      console.log("sort", sortType, areaNameSortType.value, areaNameSortType.value);
      if (sortType === 'name') {
        if (areaNameSortType.value === 'asc') {
          areaNameSortType.value = 'desc'
        } else {
          areaNameSortType.value = 'asc'
        }
      } else if (sortType === 'areaType') {
        if (areaTypeSortType.value === 'asc') {
          areaTypeSortType.value = 'desc'
        } else {
          areaTypeSortType.value = 'asc'
        }
      }
      currentSortType.value = sortType
      const direction = sortType === 'name' ? areaNameSortType.value : areaTypeSortType.value
      if (direction === 'asc') {
        listAreas.value = [...listAreas.value].sort((a, b) => {
          if (a[sortType] < b[sortType]) return -1;
          if (a[sortType] > b[sortType]) return 1;
          return 0;
        })
      } else if (direction === 'desc') {
        listAreas.value = [...listAreas.value].sort((a, b) => {
          if (a[sortType] > b[sortType]) return -1;
          if (a[sortType] < b[sortType]) return 1;
          return 0;
        })
      }
      console.log('sort', sortType, direction, listAreas.value)
    };

    const handleDeleteArea = (area, index) => {
      console.log("handleDeleteArea", area, index);
      listAreas.value = listAreas.value.filter((item, i) => index !== i);
    };

    const handleEditArea = (area, index) => {
      area.active = true
      Vue.set(listAreas.value, index, { ...area });
      console.log("handleEditArea", area, listAreas.value);
    };

    const handleChangeArea = (area, type, index) => {
      area.areaTypeObject = type;
      area.areaType = type.value;
      Vue.set(listAreas.value, index, { ...area });
      console.log("handleChangeArea", listAreaTypes.value, area, type);
    };

    const handleAreaNameInput = _.debounce((area, index, e) => {
      const inputText = e.target.value;
      const re = /[/\*|:<>,?]/
      const regex = new RegExp(re);
      const check = regex.test(inputText)
      listAreas.value[index].valid = !check
    }, 500);

    const handleAddNewPlantArea = () => {
      const newArea = { ...NEW_AREA };
      listAreas.value = [...listAreas.value].map(item => ({...item, active: false}))
      listAreas.value.push(newArea);
      console.log('handleAddNewPlantArea', listAreas.value)
    };

    const handleClose = () => {
      ctx.emit("close");
    };

    const getAreasByPlantId = async () => {
      const id = props.plantId;
      try {
        const res = await axios.get(`/api/common/plt-stp/${id}/areas`);
        listAreas.value = res.data.content.map((item) => {
          const newItem = { ...item };
          newItem.areaTypeObject = AREA_TYPES[newItem.areaType];
          newItem.avtive = false;
          newItem.valid = true;
          return newItem;
        });
        console.log('getAreasByPlantId', listAreas.value)
        if (listAreas.value.length === 0) {
          handleAddNewPlantArea();
        }
      } catch (error) {
        console.log(error);
      }
    };

    const handleSubmit = async () => {
      await postAreas();
      ctx.emit("success");
      ctx.emit("close");
    };

    const postAreas = async () => {
      const content = [...listAreas.value];
      const locationId = props.plantId;
      const payload = { content, locationId };
      console.log("postAreas", payload);
      try {
        const res = await axios.post(`/api/common/plt-stp/${locationId}/areas`, payload);
        console.log("postAreas", res);
      } catch (error) {
        console.log(error);
      }
    };

    const init = () => {
      console.log("init");
      listAreaTypes.value = Object.values(AREA_TYPES);
      areaNameSortType.value = "asc";
      areaTypeSortType.value = "asc";
      currentSortType.value = ''
      console.log("init after", listAreas.value, listAreas.value);
    };

    return {
      // STATE //
      listAreas,
      areaTypeSortType,
      areaNameSortType,
      listAreaTypes,
      currentSortType,
      // COMPUTED //
      getModalTitle,
      disabled,
      // METHOD //
      handleClose,
      sort,
      handleDeleteArea,
      handleEditArea,
      handleChangeArea,
      handleAddNewPlantArea,
      handleSubmit,
      handleAreaNameInput
    };
  },
};
</script>
<style>
.ant-tooltip-inner {
  padding: 0 !important;
  position: relative;
}
</style>
<style scoped>
.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  height: 25px;
  width: 25px;
}
.action-btn {
  background: #d9d9d9;
  border-color: transparent;
}
.action-btn:focus {
  background: #daeeff;
}
.action-btn:hover img {
  filter: sepia(206%) hue-rotate(163deg) saturate(900%);
}
.action-btn:hover {
  background: #daeeff;
}
.content-group {
  margin-top: 20px;
  border: solid 1px #d0d0d0;
  border-radius: 3px;
}
.tab-group-title {
  color: #4b4b4b;
  font-weight: 700;
  font-size: 15px;
  line-height: 18px;
  height: 50px;
  background-color: #f5f8ff;
  display: flex;
  align-items: center;
}
.tab-group-title p {
  margin-bottom: 0px;
}
.tab-group-item {
  padding-top: 10px;
  padding-bottom: 10px;
  color: #4b4b4b;
  font-size: 15px;
  display: flex;
  align-items: center;
  height: 45px;
}
.tab-group-item.odd-column {
  background-color: #fbfcfd;
}
.image-duplicate {
  cursor: pointer;
}
.desc.__icon-sort {
  transform: rotate(180deg);
}
.asc.__icon-sort {
  transform: rotate(0deg);
}
.row--grey {
  background-color: #f2f2f2;
}
.row--white {
  background-color: #ffffff;
}
</style>
