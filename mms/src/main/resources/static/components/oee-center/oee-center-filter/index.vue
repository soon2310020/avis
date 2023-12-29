<template>
  <div class="oee-center-filter">
    <common-select
      v-if="resources && Object.entries(resources).length"
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
      v-if="resources && Object.entries(resources).length"
      :placeholder="resources['all_machines']"
      :has-checkall="showCheckAllBoxMachine"
      :has-checkbox="true"
      :has-searchbox="listMachines.length > 5"
      :list-options="listMachines"
      :value="filter.listMachines"
      :get-label="getLabelMachine"
      @change="(event) => handleChange('machine', event)"
      @check-all="(event) => handleCheckAll('machine', event)"
      @close="handleClose('machine')"
      label="Machines"
      :style-props="{ width: 'fit-content' }"
      :resource="resources"
    ></common-select>

    <common-select
      v-if="displayShifts"
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

    <!-- <primary-button :prefix="'/images/icon/oee/calendar-icon.svg'" :hover-prefix="'/images/icon/oee/calendar-icon.svg'"
            :prefix-position="'right'" :custom-style="{ height: '31px' }"
            :default-class="'btn-custom btn-custom-primary animationPrimary primary-button'"
            :active-class="'outline-animation'" @on-active="handleOpenPickerModal" :title="getDateTitle"></primary-button> -->
    <date-picker-button
      :date="filter.currentDate"
      :prefix="'/images/icon/oee/calendar-icon.svg'"
      :hover-prefix="'/images/icon/oee/calendar-icon.svg'"
      :prefix-position="'right'"
      :is-range="filter.frequency.includes('CUSTOM')"
      :custom-style="{ height: '31px' }"
      :default-class="'btn-custom btn-custom-primary animationPrimary primary-button'"
      :active-class="'outline-animation'"
      :frequency="filter.frequency"
      :parent="'modal'"
      @click="handleOpenPickerModal"
    >
    </date-picker-button>

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
const FIELD_SET = {
  location: "location",
  machine: "machine",
  shift: "shift",
};

const FREQUENCY_OPTION = {
  DAILY: "DAILY",
  WEEKLY: "WEEKLY",
  MONTHLY: "MONTHLY",
  CUSTOM: "CUSTOM",
};

const OEE_SHOW = {
  DASHBOARD: "dashboard",
  MACHINE_DOWNTIME: "machineDowntime",
  PARTS_PRODUCED: "partsProduced",
  REJECT_RATE: "rejectRate",
  MACHINE_TOOLING_MATCHES: "machineToolingMatches",
};

const CHART_TYPE = "OEE_CENTER";

const PICKER_OPTIONS = [
  { title: "Daily", type: FREQUENCY_OPTION.DAILY, isRange: false },
  { title: "Weekly", type: FREQUENCY_OPTION.WEEKLY, isRange: false },
  { title: "Monthly", type: FREQUENCY_OPTION.MONTHLY, isRange: false },
  { title: "Custom Range", type: FREQUENCY_OPTION.CUSTOM, isRange: true },
];

module.exports = {
  name: "OeeDashboardFilter",
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    currentOeeShow: String,
  },
  setup(props, ctx) {
    const listLocations = ref([]);
    const listMachines = ref([]);
    const listShifts = ref([]);

    const filter = reactive({
      listLocations: computed(() =>
        listLocations.value.filter((i) => i.checked)
      ),
      listMachines: computed(() => listMachines.value.filter((i) => i.checked)),
      listShifts: computed(() => listShifts.value.filter((i) => i.checked)),
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment(new Date()).format("YYYY-MM-DD"),
        toTitle: moment(new Date()).format("YYYY-MM-DD"),
      },
      frequency: FREQUENCY_OPTION.DAILY,
    });

    const datePickerModalRef = ref(null);
    const timeOptions = ref([...PICKER_OPTIONS]);

    const savedMachineIds = ref([]);
    const savedLocationIds = ref([]);

    const displayShifts = computed(() => {
      console.log(props.resources);
      return (
        props.resources &&
        Object.entries(props.resources).length &&
        props.currentOeeShow === OEE_SHOW.PARTS_PRODUCED
      );
    });

    const getDateOptions = computed(() => {
      if (props.currentOeeShow === OEE_SHOW.DASHBOARD) {
        const newOptions = timeOptions.value.filter(
          (item) => item.type !== "HOURLY"
        );
        return newOptions;
      }
      if (
        props.currentOeeShow === OEE_SHOW.REJECT_RATE ||
        props.currentOeeShow === OEE_SHOW.PARTS_PRODUCED
      ) {
        // return newOptions
        return [{ title: "Daily", type: "DAILY", isRange: false }];
      }
      return [...timeOptions.value];
    });

    const setDefaultMachines = async (locationId) => {
      const response = await fetchListMachines({ locationIds: locationId });
      if (savedMachineIds.value.length) {
        listMachines.value = response.map((i) => ({
          ...i,
          title: i.machineCode,
          value: i.id,
          checked: savedMachineIds.value.some((j) => +j === i.id),
        }));
        savedMachineIds.value = [];
      } else {
        // in case no saved settings, default is select all
        listMachines.value = response.map((i) => ({
          ...i,
          title: i.machineCode,
          value: i.id,
          checked: true,
        }));
      }
    };

    const setDefaultLocations = (resLocations) => {
      const location =
        resLocations.filter((i) =>
          savedLocationIds.value.some((j) => +j == i.id)
        )[0] ?? resLocations[0]; // default
      listLocations.value = resLocations.map((i) => ({
        ...i,
        title: i.name,
        value: i.id,
        checked: location.id === i.id,
      }));
      return location;
    };

    const resetDate = () => {
      filter.currentDate = {
        from: new Date(),
        to: new Date(),
        fromTitle: moment(new Date()).format("YYYY-MM-DD"),
        toTitle: moment(new Date()).format("YYYY-MM-DD"),
      };
      filter.frequency = FREQUENCY_OPTION.DAILY;
    };

    // HANDLERS
    const handleChange = async (type, event) => {
      console.log("handleChange", type);
      const checked = event.target.checked;
      const item = JSON.parse(event.target.value);

      if (type === FIELD_SET.location) {
        listLocations.value = listLocations.value.map((i) => ({
          ...i,
          checked: i.id === item.id,
        }));
        await saveDefaultOptions();
        const event = {
          target: {
            checked: true,
          },
        };
        handleCheckAll("machine", event);
        changeFilter();
      }
      if (type === FIELD_SET.machine) {
        const findIndex = listMachines.value.findIndex((i) => i.id === item.id);
        listMachines.value.splice(findIndex, 1, { ...item, checked });
      }
      if (type === FIELD_SET.shift) {
        const findIndex = listShifts.value.findIndex((i) => i.id === item.id);
        listShifts.value.splice(findIndex, 1, { ...item, checked });
      }
    };

    const handleCheckAll = (type, event) => {
      console.log(type, event);
      const checked = event.target.checked;
      if (type === FIELD_SET.machine) {
        listMachines.value = listMachines.value.map((i) => ({ ...i, checked }));
      }
    };

    const handleChangeDate = async (timeValue, frequency) => {
      console.log("handleChangeDate", timeValue, frequency);
      filter.currentDate = timeValue;
      filter.frequency = frequency;
      changeFilter();
      if (props.currentOeeShow === OEE_SHOW.PARTS_PRODUCED) {
        await fetchListShifts();
      }
      datePickerModalRef.value.closeDatePicker();
      // ctx.emit('input', {
      //     ...JSON.parse(JSON.stringify(props.filter)),
      //     currentDate: timeValue,
      //     frequency: frequency
      // })
    };

    const handleClose = async (type) => {
      await saveDefaultOptions();
      changeFilter();
    };

    const changeFilter = () => {
      ctx.emit("change", filter);
    };

    // GETTERS
    const getLabelLocation = () => {
      const hasNoData =
        filter.listLocations.length === 0 || !filter.listLocations;
      const hasSelectedAll =
        filter.listLocations.length === listLocations.value.length;
      if (hasNoData || hasSelectedAll) return "No plant";
      else if (filter.listLocations.length === 1)
        return filter.listLocations[0].name;
      else return filter.listLocations.length + " " + props.resources["plants"];
    };

    const getLabelMachine = () => {
      const hasNoData =
        filter.listMachines.length === 0 || !filter.listMachines;
      const hasSelectedAll =
        filter.listMachines.length === listMachines.value.length;
      if (hasNoData || hasSelectedAll) return props.resources["all_machines"];
      else if (filter.listMachines.length === 1)
        return filter.listMachines[0].machineCode;
      else
        return filter.listMachines.length + " " + props.resources["machines"];
    };

    const getLabelShift = () => {
      const hasNoData = filter.listShifts.length === 0 || !filter.listShifts;
      const hasSelectedAll =
        filter.listShifts.length === listShifts.value.length;
      if (hasNoData || hasSelectedAll) return props.resources["all_shifts"];
      else if (filter.listShifts.length === 1) return filter.listShifts[0].name;
      else return filter.listShifts.length + " " + props.resources["shifts"];
    };

    const getDateTitle = computed(() => {
      return moment(filter.currentDate.to).format("YYYY-MM-DD");
    });

    const handleOpenPickerModal = () => {
      console.log("@handleOpenPickerModal", {
        frequency: filter.frequency,
        currentDate: filter.currentDate,
      });
      datePickerModalRef.value.showDatePicker(
        filter.frequency,
        filter.currentDate
      );
    };

    //SETTER
    const setCurrentDate = (newDate) => {
      console.log("setCurrentDate", filter.currentDate, newDate);
    };

    const fetchDefaultOptions = async () => {
      const query = Common.param({ chartType: CHART_TYPE });
      try {
        const response = await axios.get(
          "/api/dashboard-chart-display-settings/get-by-chart-type?" + query
        );
        return response.data.data.dashboardSettingData;
      } catch (error) {
        console.log(error);
        return false;
      }
    };

    const saveDefaultOptions = async () => {
      const payload = { chartType: CHART_TYPE, dashboardSettingData: {} };
      payload.dashboardSettingData.locationIdList = filter.listLocations
        .map((i) => i.id)
        .toString();
      payload.dashboardSettingData.machineIdList = filter.listMachines
        .map((i) => i.id)
        .toString();
      try {
        await axios.post("/api/dashboard-chart-display-settings", payload);
      } catch (error) {
        console.log(error);
      }
    };

    const fetchListLocations = async () => {
      const param = Common.param({
        status: "active",
        sort: "name,asc",
        page: 1,
        size: 10000,
      });
      try {
        const response = await axios.get("/api/locations?" + param);
        return response.data.content;
      } catch (error) {
        return error;
      }
    };

    const fetchListMachines = async ({ locationIds }) => {
      const param = Common.param({
        locationIds,
        status: "enabled",
        sort: "machineCode,asc",
        page: 1,
        size: 10000,
      });
      try {
        const response = await axios.get("/api/machines?" + param);
        return response.data.content;
      } catch (error) {
        console.log(error);
      }
    };

    const fetchListShifts = async (params) => {
      const queryParams = Common.param({
        locationId: filter.listLocations.map((i) => i.id).toString(),
        day: Common.formatter.numberOnly(filter.currentDate.fromTitle),
        ...params,
      });
      try {
        const response = await axios.get(
          "/api/shift-config/get-by-location-and-day?" + queryParams
        );
        listShifts.value = response.data.data.map((item) => ({
          ...item,
          title: item.name,
          checked: true,
          id: item.shiftNumber,
        }));
        ctx.emit("load-shift", listShifts.value);
      } catch (error) {
        console.log(error);
      }
    };

    const showCheckAllBoxLocation = computed(
      () => listLocations.value.length > 4
    );
    const showCheckAllBoxMachine = computed(
      () => listMachines.value.length > 4
    );
    const showCheckAllBoxShift = computed(() => listShifts.value.length > 4);

    watch(
      () => props.currentOeeShow,
      async (newVal) => {
        if (newVal === OEE_SHOW.PARTS_PRODUCED) {
          await fetchListShifts();
          if (filter.frequency !== FREQUENCY_OPTION.DAILY) {
            resetDate();
          }
        }
        if (newVal === OEE_SHOW.REJECT_RATE) {
          if (filter.frequency !== FREQUENCY_OPTION.DAILY) {
            resetDate();
          }
        }
      },
      { immediate: true }
    );

    const init = async () => {
      const [resDefault, resLocations] = await Promise.all([
        fetchDefaultOptions(),
        fetchListLocations(),
      ]);
      savedLocationIds.value = resDefault.locationIdList
        ? resDefault.locationIdList.split(",")
        : [];
      savedMachineIds.value = resDefault.machineIdList
        ? resDefault.machineIdList.split(",")
        : [];
      setDefaultLocations(resLocations);
      changeFilter();
    };

    watch(
      () => filter.listLocations,
      (newVal) => {
        if (newVal.length) setDefaultMachines(newVal[0].id);
      }
    );

    onMounted(() => init());

    watchEffect(() => console.log(listMachines.value.map((o) => o.checked)));

    return {
      filter,
      datePickerModalRef,
      timeOptions,
      listLocations,
      listMachines,
      listShifts,

      displayShifts,
      getDateOptions,

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
