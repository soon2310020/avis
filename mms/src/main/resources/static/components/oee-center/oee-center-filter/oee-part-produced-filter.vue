<template>
  <div class="oee-center-filter">
    <common-select
      v-if="Object.entries(resources).length"
      :placeholder="resources['all_locations']"
      label="Plants"
      :has-searchbox="listLocations.length >= 5"
      :list-options="listLocations"
      :value="filter.listLocations"
      :get-label="getLabelLocation"
      @change="(event) => handleChange('location', event)"
      :style-props="{ width: 'fit-content' }"
      :resource="resources"
    ></common-select>

    <common-select
      v-if="Object.entries(resources).length"
      :placeholder="resources['all_machines']"
      :has-checkall="showCheckAllBoxMachine"
      :has-checkbox="true"
      :has-searchbox="listMachines.length > 5"
      :list-options="listMachines"
      :value="filter.listMachines"
      :get-label="getLabelMachine"
      @change="(event) => handleChange('machine', event)"
      @check-all="(event) => handleCheckAll('machine', event)"
      @close="handleClose"
      label="Machines"
      :style-props="{ width: 'fit-content' }"
      :resource="resources"
    ></common-select>

    <common-select
      v-if="Object.entries(resources).length"
      :placeholder="resources['all_shifts']"
      :has-checkall="showCheckAllBoxShift"
      :has-checkbox="true"
      :has-searchbox="listShifts.length >= 5"
      :list-options="listShifts"
      :value="filter.listShifts"
      :get-label="getLabelShift"
      @change="(event) => handleChange('shift', event)"
      @check-all="(event) => handleCheckAll('shift', event)"
      @close="handleClose"
      label="Shift"
      :style-props="{ width: 'fit-content', minWidth: '140px' }"
      :resource="resources"
    ></common-select>

    <primary-button
      :prefix="'/images/icon/oee/calendar-icon.svg'"
      :hover-prefix="'/images/icon/oee/calendar-icon.svg'"
      :prefix-position="'right'"
      :custom-style="{ height: '31px' }"
      :default-class="'btn-custom btn-custom-primary animationPrimary primary-button'"
      :active-class="'outline-animation'"
      @on-active="handleOpenPickerModal"
      :title="getDateTitle"
    ></primary-button>

    <!-- Date picker modal -->
    <date-picker-modal
      ref="datePickerModalRef"
      :resources="resources"
      :select-option="timeOptions"
      @change-date="handleChangeDate"
    >
    </date-picker-modal>
  </div>
</template>

<script>
const defaultOptions = [{ title: "Daily", type: "DAILY", isRange: false }];

const FIELD_SET = {
  location: "listLocations",
  machine: "listMachines",
  shift: "listShifts",
};

module.exports = {
  name: "OeeDashboardFilter",
  props: {
    resources: Object,
    listLocations: Array,
    listMachines: Array,
    listShifts: Array,
    currentOeeShow: String,
    refetchMachines: Function,
  },
  setup(props, ctx) {
    const filter = reactive({
      listLocations: [],
      listMachines: [],
      listShifts: [],
      currentDate: computed(() => currentDate.value),
      frequency: computed(() => frequency.value),
    });

    const datePickerModalRef = ref(null);
    const timeOptions = ref([...defaultOptions]);
    const frequency = ref("DAILY");
    const currentDate = ref({
      from: new Date(),
      to: new Date(),
      fromTitle: moment().format("YYYY-MM-DD"),
      toTitle: moment().format("YYYY-MM-DD"),
    });

    // HANDLERS
    const handleChange = (type, event) => {
      const checked = event.target.checked;
      const value = JSON.parse(event.target.value);
      if (type === "location") {
        filter[FIELD_SET[type]] = [value];
        return handleClose();
      }
      // if (checked) filter[FIELD_SET[type]].push(value)
      if (checked)
        filter[FIELD_SET[type]] = [...filter[FIELD_SET[type]], value];
      else
        filter[FIELD_SET[type]] = filter[FIELD_SET[type]].filter(
          (item) => item.id !== value.id
        );
    };

    const handleCheckAll = (type, event) => {
      const checked = event.target.checked;
      ctx.emit("check-all", type, checked);
    };

    const handleChangeDate = (timeValue, frequency) => {
      currentDate.value = timeValue;
      frequency.value = frequency;
      datePickerModalRef.value.closeDatePicker();
      ctx.emit("filter", { ...filter });
    };

    const handleClose = () => {
      ctx.emit("filter", { ...filter });
    };

    // GETTERS
    const getLabelLocation = () => {
      const hasNoData =
        filter.listLocations.length === 0 || !filter.listLocations;
      const hasSelectedAll =
        filter.listLocations.length === props.listLocations.length;
      if (hasNoData || hasSelectedAll) return "No plant";
      else if (filter.listLocations.length === 1)
        return filter.listLocations[0].name;
      else return filter.listLocations.length + " " + props.resources["plants"];
    };

    const getLabelMachine = () => {
      const hasNoData =
        filter.listMachines.length === 0 || !filter.listMachines;
      const hasSelectedAll =
        filter.listMachines.length === props.listMachines.length;
      if (hasNoData || hasSelectedAll) return props.resources["all_machines"];
      else if (filter.listMachines.length === 1)
        return filter.listMachines[0].machineCode;
      else
        return filter.listMachines.length + " " + props.resources["machines"];
    };

    const getLabelShift = () => {
      const hasNoData = filter.listShifts.length === 0 || !filter.listShifts;
      const hasSelectedAll =
        filter.listShifts.length === props.listShifts.length;
      if (hasNoData || hasSelectedAll) return props.resources["all_shifts"];
      else if (filter.listShifts.length === 1) return filter.listShifts[0].name;
      else return filter.listShifts.length + " " + props.resources["shifts"];
    };

    const isRange = computed(() => {
      const options = timeOptions.value.filter(
        (item) => item.type === frequency.value
      );
      return options.length > 0 ? options[0].isRange : false;
    });

    const getDateTitle = computed(() => {
      const displayDate = currentDate.value.to;
      return moment(displayDate).format("YYYY-MM-DD");
    });

    const handleOpenPickerModal = () => {
      datePickerModalRef.value.showDatePicker(
        frequency.value,
        currentDate.value
      );
    };

    //SETTER
    const setCurrentDate = (newDate) => {
      currentDate.value = newDate;
      console.log("setCurrentDate", currentDate, newDate);
    };

    watch(
      () => props.listMachines,
      (newVal) => {
        filter.listMachines = [...newVal.filter((item) => item.checked)];
      }
    );
    watch(
      () => props.listLocations,
      (newVal) => {
        filter.listLocations = [...newVal.filter((item) => item.checked)];
      }
    );
    watch(
      () => props.listShifts,
      (newVal) => {
        filter.listShifts = [...newVal.filter((item) => item.checked)];
      }
    );

    const showCheckAllBoxLocation = computed(
      () => props.listLocations.length > 4
    );
    const showCheckAllBoxMachine = computed(
      () => props.listMachines.length > 4
    );
    const showCheckAllBoxShift = computed(() => props.listShifts.length > 4);

    return {
      datePickerModalRef,
      filter,
      isRange,
      timeOptions,
      frequency,
      currentDate,
      showCheckAllBoxLocation,
      showCheckAllBoxMachine,
      showCheckAllBoxShift,
      //   getter
      getLabelLocation,
      getLabelMachine,
      getLabelShift,
      getDateTitle,
      //setter
      setCurrentDate,
      //   handler
      handleChange,
      handleChangeDate,
      handleCheckAll,
      handleOpenPickerModal,
      handleClose,
    };
  },
};
</script>

<style scoped>
.oee-center-filter {
  max-width: 600px;
  display: flex;
  gap: 8px;
}
</style>
