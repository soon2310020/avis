<template>
  <div class="base-card-tab-list">
    <base-card-tab
      v-for="card in listTabs"
      :size="size"
      :active="card.key === currentTab"
      :icon-src="card.iconSrc"
      :icon-style="card.iconStyle"
      :key="card.value"
      :title="card.title"
      :content="card.content"
      :style="{ width: itemWidth }"
      @click="handleClick(card)"
    ></base-card-tab>
  </div>
</template>

<script>
module.exports = {
  name: "BaseCardTabList",
  props: {
    listTabs: Array,
    currentTab: String,
    size: String,
    itemWidth: String,
    iconStyle: String,
  },
  methods: {
    handleClick(card) {
      this.$emit("change", card);
    },
  },
};
</script>

<style scoped>
.base-card-tab-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
