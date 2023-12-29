<template>
  <div class="d-flex align-center">
    <div class="col-md-2" style="padding: 0px">
      <input
        v-model="numberOfWeek"
        type="number"
        class="form-control"
        @input="handleChangeInput"
      />
    </div>
    <span style="margin: 0px 8px"> month(s) on the </span>
    <div style="margin-right: 8px">
      <custom-dropdown
        :list-item="monthDays"
        :title-field="'title'"
        :default-item="getSelectedMonthItem"
        @change="handleChangeScheduleDate"
      ></custom-dropdown>
    </div>
    <div>
      <custom-dropdown
        :list-item="weekDays"
        :title-field="'title'"
        :default-item="getSelectedItem"
        @change="handleChangeFrequency"
      ></custom-dropdown>
    </div>
  </div>
</template>

<script>
const WEEK_DAYS = [
  { title: "Monday", value: "MONDAY" },
  { title: "Tuesday", value: "TUESDAY" },
  { title: "Wednesday", value: "WEDNESDAY" },
  { title: "Thursday", value: "THURSDAY" },
  { title: "Friday", value: "FRIDAY" },
  { title: "Saturday", value: "SATURDAY" },
  { title: "Sunday", value: "SUNDAY" },
];
const MONTH_DAYS = [
  { title: "1st", value: 1 },
  { title: "2nd", value: 2 },
  { title: "3rd", value: 3 },
  { title: "4th", value: 4 },
];
module.exports = {
  components: {
    "custom-dropdown": httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
  },
  props: {
    defaultItem: {
      type: [String, Number],
    },
    defaultNumber: [String, Number],
    scheduleAt: {
      type: [String, Number],
    },
  },
  setup(props, ctx) {
    const weekDays = ref([...WEEK_DAYS]);
    const selectedItem = ref({ title: "Monday", value: "MONDAY" });
    const numberOfWeek = ref(props.defaultNumber);
    const monthDays = ref([...MONTH_DAYS]);
    const selectedMonthItem = ref({ title: "1st", value: 1 });

    const getSelectedMonthItem = computed(() => {
      const find = monthDays.value.find(
        (item) => item.value === props.scheduleAt
      );
      if (find) {
        return find;
      }
      return monthDays.value[0];
    });

    const getSelectedItem = computed(() => {
      return weekDays.value.find((item) => item.value === props.defaultItem);
    });

    watch(
      () => props.defaultItem,
      (newVal) => {
        selectedItem.value = weekDays.value.find(
          (item) => item.value === newVal
        );
        console.log("watch props.defaultItem", newVal, selectedItem.value);
      }
    );
    watch(
      () => props.defaultNumber,
      (newVal) => {
        numberOfWeek.value = newVal;
      }
    );
    watch(
      () => props.scheduleAt,
      (newVal) => {
        selectedMonthItem.value = monthDays.value.find(
          (item) => item.value === newVal
        );
        console.log("watch props.defaultItem", newVal, selectedItem.value);
      }
    );

    const handleChangeFrequency = (item) => {
      selectedItem.value = item;
      change();
    };

    const handleChangeScheduleDate = (item) => {
      selectedMonthItem.value = item;
      change();
    };

    const handleChangeInput = _.debounce(function (e) {
      change();
    }, 500);

    const change = () => {
      ctx.emit(
        "change",
        numberOfWeek.value,
        selectedItem.value,
        selectedMonthItem.value
      );
    };

    return {
      weekDays,
      numberOfWeek,
      selectedItem,
      getSelectedMonthItem,
      getSelectedItem,
      monthDays,
      selectedMonthItem,
      handleChangeFrequency,
      handleChangeInput,
      handleChangeScheduleDate,
    };
  },
};
</script>

<style></style>
