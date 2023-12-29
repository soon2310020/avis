<template>
  <div class="enable-text">
    <div class="config-header">
      <div class="left-header">
        <h4 v-text="resources['reject_rate_input_configuration']"></h4>
      </div>
    </div>
    <div class="config-body">
      <div class="">
        <div
          style="
            display: flex;
            flex-direction: row;
            justify-content: left;
            align-items: center;
            margin-bottom: 15px;
          "
        >
          <label
            v-text="resources['input_frequency']"
            style="font-size: 16px; font-weight: 500; margin: 0 20px 0 0"
          ></label>
          <custom-dropdown
            :list-item="listFrequency"
            :title-field="'title'"
            :default-title="'Frequency'"
            :default-item="selectedItem"
            @change="handleChangeFrequency"
          ></custom-dropdown>
        </div>
        <label
          class="text-lean"
          v-text="resources['input_frequency_message']"
        ></label>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "rejectRateInputConfig",
  props: {
    resources: Object,
    defaultItem: {
      type: Object,
      default: () => {},
    },
  },
  components: {
    CustomDropdown: httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
    ),
  },
  data() {
    return {
      selectedItem: null,
    };
  },
  created() {
    this.listFrequency = [
      { title: "Hourly input", frequency: "HOURLY" },
      { title: "Daily input", frequency: "DAILY" },
      { title: "Weekly input", frequency: "WEEKLY" },
      { title: "Monthly Input", frequency: "MONTHLY" },
    ];
  },
  computed: {
    // selectedItem() {
    //   const find = this.listFrequency.filter(item => item.frequency == this.defaultItem.frequency)
    //   console.log('selectedItem', this.selectedItem)
    //   if (find.length > 0) {
    //     return find[0]
    //   }
    //   return {}
    // }
  },
  methods: {
    handleChangeFrequency(item, prevItem) {
      console.log("item", item, prevItem);
      this.$emit("update-config", item, prevItem);
    },
  },
  watch: {
    defaultItem: {
      handler(newVal) {
        const find = this.listFrequency.filter(
          (item) => item.frequency == newVal.frequency
        );

        if (find.length > 0) {
          this.selectedItem = find[0];
        } else {
          this.selectedItem = {};
        }

        console.log("selectedItem", this.selectedItem);
      },
      deep: true,
    },
  },
};
</script>

<style></style>
