<template>
  <div class="d-flex align-center">
    <div class="col-md-2" style="padding: 0px">
      <input
        type="number"
        class="form-control"
        @input="handleChangeInput"
        :value="numberOfWeek"
      />
    </div>
    <span style="margin: 0px 8px">week(s) on</span>
    <custom-dropdown
      :list-item="weekDays"
      :title-field="'title'"
      :default-item="getSelectedItem"
      @change="handleChangeFrequency"
    ></custom-dropdown>
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
module.exports = {
  components: {
    "custom-dropdown": httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
  },
  props: {
    defaultItem: {
      type: String,
    },
    defaultNumber: [String, Number],
  },
  setup(props, ctx) {
    const weekDays = ref([...WEEK_DAYS]);
    const selectedItem = ref({ title: "Monday", value: "MONDAY" });
    const numberOfWeek = ref(props.defaultNumber);

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
        numberOfWeek.value = props.defaultNumber;
        console.log("watch props.defaultItem", newVal, selectedItem.value);
      }
    );

    const handleChangeFrequency = (item) => {
      selectedItem.value = item;
      change();
    };

    const handleChangeInput = (e) => {
      numberOfWeek.value = e.target.value;
      handleDebounceInput(e);
    };

    const handleDebounceInput = _.debounce(function (e) {
      change();
    }, 500);

    const change = () => {
      ctx.emit("change", numberOfWeek.value, selectedItem.value);
    };

    return {
      weekDays,
      numberOfWeek,
      selectedItem,
      getSelectedItem,
      handleChangeFrequency,
      handleChangeInput,
    };
  },
};
</script>
