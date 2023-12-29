<template>
  <div class="d-flex align-center justify-space-between">
    <div>
      <base-button type="dropdown" @click="toggleDataTypesDropdown">
        {{ currentPage }}
      </base-button>
      <common-popover
        @close="closeDropdown"
        :is-visible="visibleDropdown"
        :style="{ width: '210px', marginTop: '4px' }"
      >
        <select-custom
          class="custom-dropdown--assign"
          :style="{ position: 'static', width: '100%' }"
          :items="pages"
          @close="closeDropdown"
          @on-select="handleSelectPage"
        >
        </select-custom>
      </common-popover>
    </div>
    <base-paging
      :total-page="totalPage"
      :current-page="currentPage"
      @next="handleNextPage"
      @back="handleBackPage"
    ></base-paging>
  </div>
</template>
<script>
module.exports = {
  components: {
    "select-custom": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
  },
  props: {
    currentPage: {
      type: [String, Number],
      default: () => 0,
    },
    totalPage: {
      type: [String, Number],
      default: () => 0,
    },
  },
  setup(props, ctx) {
    const visibleDropdown = ref(false);

    const pages = computed(() => {
      pages;
    });

    const handleNextPage = () => {
      ctx.emit("next");
    };
    const handleBackPage = () => {
      ctx.emit("back");
    };
    const handleSelectPage = (page) => {
      console.log("handleSelectPage", page);
    };
    const closeDropdown = () => {
      visibleDropdown.value = false;
    };
    return {
      visibleDropdown,
      handleNextPage,
      handleBackPage,
      handleSelectPage,
      closeDropdown,
    };
  },
};
</script>
