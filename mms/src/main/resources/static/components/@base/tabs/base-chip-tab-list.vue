<template>
  <div class="base-chip-tab-list" :class="[`base-chip-align-${align}`]">
    <base-chip-tab
      v-for="chip in listTabs"
      :key="chip.key"
      :level="level"
      :active="chip.key === currentTab"
      @click="handleClick(chip)"
    >
      {{ chip.label }}
    </base-chip-tab>
  </div>
</template>

<script>
module.exports = {
  name: "BaseChipTabList",
  components: {
    "base-chip-tab": httpVueLoader("./BaseChipTab.vue"),
  },
  props: {
    listTabs: {
      type: Array,
      default: () => [], // {key: string, label: string}
    },
    currentTab: {
      type: String,
      default: "",
    },
    level: {
      type: String,
      default: "primary",
      validator(val) {
        return ["primary", "secondary"].includes(val);
      },
    },
    align: {
      type: String,
      default: "horizontal",
      validator(val) {
        return ["vertical", "horizontal"].includes(val);
      },
    },
  },
  methods: {
    handleClick(chip) {
      this.$emit("change", chip);
    },
  },
};
</script>

<style scoped>
.base-chip-tab-list {
  display: flex;
  gap: 10px;
}
/* ALIGN HORIZONTAL */
.base-chip-align-horizontal {
  align-items: center;
}

/* ALIGN VERTICAL */
.base-chip-align-vertical {
  flex-direction: column;
}
</style>
