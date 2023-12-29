<template>
  <div class="oee-center-filter">
    <common-select
      v-if="Object.entries(resources).length"
      :placeholder="resources['all_locations']"
      label="Plants"
      :has-searchbox="listLocations.length > 5"
      :list-options="listLocations"
      :value="filter.listLocations"
      :get-label="getLabelLocation"
      @change="(event) => handleChange('location', event)"
      @check-all="(event) => handleCheckAll('location', event)"
      @close="handleClose"
      :style-props="{ width: 'fit-content' }"
      :resources="resources"
      :all-check-label="'All Plants'"
    ></common-select>

    <common-select
      v-if="Object.entries(resources).length"
      :placeholder="resources['all_machines']"
      :label="'Machines'"
      :has-checkall="showCheckAllBoxMachine"
      :has-checkbox="true"
      :has-searchbox="listMachines.length > 5"
      :list-options="listMachines"
      :value="filter.listMachines"
      :get-label="getLabelMachine"
      @change="(event) => handleChange('machine', event)"
      @check-all="$emit('check-all', 'machine', event)"
      @close="handleClose"
      :style-props="{ width: 'fit-content' }"
      :resources="resources"
    ></common-select>
    <date-picker-button
      :date="currentDate"
      :prefix="'/images/icon/oee/calendar-icon.svg'"
      :hover-prefix="'/images/icon/oee/calendar-icon.svg'"
      :prefix-position="'right'"
      :is-range="isRange"
      :custom-style="{ height: '31px' }"
      :default-class="'btn-custom btn-custom-primary animationPrimary primary-button'"
      :active-class="'outline-animation'"
      :frequency="frequency"
      :parent="'modal'"
      @click="handleOpenPickerModal"
    >
    </date-picker-button>

    <div
      class="information-tooltip"
      style="height: 100%; display: flex; align-items: center"
    >
      <a-tooltip placement="bottomLeft">
        <template slot="title">
          <div style="padding: 6px 8px">
            When more than 1 plant is selected in the filter, the displayed
            results are shown as an average; taking into account the shift times
            for each plant.
          </div>
        </template>
        <a-icon type="info-circle"></a-icon>
      </a-tooltip>
    </div>

    <!-- Date picker modal -->
    <date-picker-modal
      ref="datePickerModalRef"
      :resources="resources"
      :select-option="getDateOptions"
      @change-date="handleChangeDate"
    >
    </date-picker-modal>
  </div>
</template>

<script>
const defaultOptions = [
  { title: "Hourly", type: "HOURLY", isRange: false },
  { title: "Daily", type: "DAILY", isRange: false },
  { title: "Weekly", type: "WEEKLY", isRange: false },
  { title: "Monthly", type: "MONTHLY", isRange: false },
  { title: "Custom Range", type: "CUSTOM", isRange: true },
];

const FIELD_SET = {
  location: "listLocations",
  machine: "listMachines",
};

const OEE_SHOW = {
  DASHBOARD: "dashboard",
  MACHINE_DOWNTIME: "machineDowntime",
  PARTS_PRODUCED: "partsProduces",
  REJECT_RATE: "rejectRate",
};

module.exports = {
  name: "OeeDashboardFilter",
  props: {
    resources: Object,
    currentOeeShow: String,
    listLocations: Array,
    listMachines: Array,
    currentFrequency: {
      type: String,
      default: () => "DAILY",
    },
  },
  setup(props, ctx) {
    const filter = reactive({
      listLocations: [],
      listMachines: [],
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

    const handleChange = (type, event) => {
      const checked = event.target.checked;
      const value = JSON.parse(event.target.value);
      if (type === "location") {
        filter[FIELD_SET[type]] = [value];
        console.log("filter[FIELD_SET[type]]", filter[FIELD_SET[type]]);
        return;
      }
      if (checked) filter[FIELD_SET[type]].push(value);
      else
        filter[FIELD_SET[type]] = filter[FIELD_SET[type]].filter(
          (item) => item.id !== value.id
        );
    };

    const handleCheckAll = (type, event) => {
      ctx.emit("check-all", type, event);
      setTimeout(() => {
        handleClose();
      }, 200);
    };

    const handleChangeDate = (timeValue, frequencyValue) => {
      currentDate.value = timeValue;
      frequency.value = frequencyValue;
      datePickerModalRef.value.closeDatePicker();
      ctx.emit("filter", { ...filter });
    };

    const handleClose = () => {
      console.log("handle close");
      ctx.emit("filter", { ...filter });
    };

    const getLabelLocation = () => {
      const hasNoData =
        filter.listLocations.length === 0 || !filter.listLocations;
      const hasSelectedAll =
        filter.listLocations.length === props.listLocations.length;
      if (hasNoData || hasSelectedAll) return props.resources["all_locations"];
      else if (filter.listLocations.length === 1)
        return filter.listLocations[0].name;
      else
        return filter.listLocations.length + " " + props.resources["locations"];
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

    const getDateOptions = computed(() => {
      if (props.currentOeeShow === OEE_SHOW.DASHBOARD) {
        const newOptions = timeOptions.value.filter(
          (item) => item.type !== "HOURLY"
        );
        return newOptions;
      }
      if (props.currentOeeShow === OEE_SHOW.REJECT_RATE) {
        const newOptions = timeOptions.value.filter(
          (item) => item.type !== "CUSTOM"
        );
        // return newOptions
        return [{ title: "Daily", type: "DAILY", isRange: false }];
      }
      return [...timeOptions.value];
    });

    const handleOpenPickerModal = () => {
      datePickerModalRef.value.showDatePicker(
        props.currentFrequency,
        currentDate.value
      );
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
      () => props.currentOeeShow,
      (newVal) => {
        if (newVal === OEE_SHOW.REJECT_RATE) {
          frequency.value = "DAILY";
          timeOptions.value = defaultOptions.filter(
            (item) => item.type === "DAILY"
          );
        }
        if (newVal === OEE_SHOW.DASHBOARD) {
          timeOptions.value = [...defaultOptions];
        }
      }
    );

    const showCheckAllBoxLocation = computed(
      () =>
        props.listLocations.length > 4 &&
        props.currentOeeShow !== OEE_SHOW.REJECT_RATE
    );
    const showCheckAllBoxMachine = computed(
      () => props.listMachines.length > 4
    );

    // DEBUG
    // watchEffect(() => console.log('props.listLocations', props.listLocations))
    // watchEffect(() => console.log('props.listMachines', props.listMachines))
    // watchEffect(() => console.log('filter', filter.value))
    watchEffect(() => console.log("getDateOptions", getDateOptions.value));

    return {
      datePickerModalRef,
      filter,
      isRange,
      timeOptions,
      frequency,
      currentDate,
      getLabelLocation,
      getLabelMachine,
      getDateTitle,
      getDateOptions,
      handleChange,
      handleChangeDate,
      handleCheckAll,
      handleOpenPickerModal,
      handleClose,
      showCheckAllBoxLocation,
      showCheckAllBoxMachine,
    };
  },
};
</script>

<style scoped>
.oee-center-filter {
  max-width: 600px;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
