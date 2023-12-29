<template>
  <div class="d-flex align-center" style="gap: 10px">
    <div v-if="dropdown">
      <custom-dropdown
        :list-item="getPages"
        :title-field="'title'"
        :default-item="getSelectedItem"
        @change="handleChangePage"
      ></custom-dropdown>
      <!-- <base-button type="dropdown" @click="toggleDropdown">
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
          :items="getPages"
          @close="closeDropdown"
          @on-select="handleChangePage"
        >
        </select-custom>
      </common-popover> -->
    </div>
    <div class="d-flex align-center">
      <div>
        <span>{{ `${getDisplayCurrentPage} of ${getDisplayTotalPage}` }}</span>
      </div>
      <div class="paging__arrow">
        <a
          href="javascript:void(0)"
          class="paging-button"
          :class="{ 'inactive-button': currentPage <= 1 }"
          @click="$emit('back')"
        >
          <img src="/images/icon/common/paging-arrow.svg" alt="" />
        </a>
        <a
          href="javascript:void(0)"
          class="paging-button"
          @click="$emit('next')"
          :class="{ 'inactive-button': currentPage >= totalPage }"
        >
          <img
            src="/images/icon/common/paging-arrow.svg"
            style="transform: rotate(180deg)"
            alt=""
          />
        </a>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  components: {
    // "select-custom": httpVueLoader(
    //   "/components/@base/dropdown/common-select.vue"
    // ),
    "custom-dropdown": httpVueLoader(
      "/components/@base/selection-dropdown/custom-dropdown.vue"
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
    dropdown: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      visibleDropdown: false,
    };
  },
  computed: {
    getDisplayCurrentPage() {
      if (this.currentPage) {
        return this.currentPage;
      }
      return 1;
    },
    getDisplayTotalPage() {
      if (this.totalPage) {
        return this.totalPage;
      }
      return 1;
    },
    getPages() {
      const pages = [];
      for (let i = 1; i <= this.getDisplayTotalPage; i++) {
        pages.push({
          title: i,
          value: i,
          checked: false,
        });
      }
      return pages;
    },
    getSelectedItem() {
      return { title: this.currentPage, value: this.currentPage };
    },
  },
  methods: {
    handleChangePage(item) {
      console.log("handleSelectPage", item);
      this.$emit("change", item.value);
    },
    closeDropdown() {
      this.visibleDropdown = false;
    },
    toggleDropdown() {
      this.visibleDropdown = !this.visibleDropdown;
    },
  },
};
</script>

<style scoped>
.paging-button {
  margin-left: 8px;
}
</style>
