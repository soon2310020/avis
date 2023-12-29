<template>
  <div class="assign-table">
    <div class="table-wrapper" ref="tableRef">
      <table>
        <thead>
          <tr class="tr-sort">
            <th class="__sort" scope="col">
              <span>Part Name (Part ID)</span>
              <span class="__icon-sort"></span>
            </th>
            <th class="__sort" scope="col">
              <span>Quantity Required</span>
              <span class="__icon-sort"></span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in listParts" :key="item.id">
            <td colspan="1">
              <div v-if="item.name.length <= 20" class="part-name">{{ truncateText(item.name, 20) }} ({{ truncateText(item.partCode, 20) }})</div>
              <a-tooltip v-else placement="bottomLeft">
                <template slot="title">
                  <div class="tooltip-assign-table">{{item.name}} ({{ item.partCode }})</div>
                </template>
                <div class="part-name">{{ truncateText(item.name, 20) }} ({{ truncateText(item.partCode, 20) }})</div>
              </a-tooltip>
            </td>
            <td colspan="1">
              <div class="part-quantity">
                <input
                  type="number"
                  v-model="listParts[index].quantityRequired"
                  min="1"
                />
                <a-button
                  type="link"
                  class="part-quantity__remove"
                  @click="() => onRemovePart(item)"
                >
                  <span>Remove from Product</span>
                  <span class="icon-action__minus"></span>
                </a-button>
              </div>
            </td>
          </tr>
          <tr v-show="isLoading">
            <td colspan="2">
              <div class="spinner">
                <a-spin>
                  <a-icon
                    slot="indicator"
                    type="loading"
                    style="font-size: 24px"
                    spin
                  />
                </a-spin>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="assign-more">
      <a-button  type="link" class="btn-show-picker" @click.stop="handleToggle">
        + Assign another part to {{ truncateText(projectName, 20) }}
      </a-button>
      <common-popover
        :is-visible="showPartPicker"
        @close="handleClosePartPicker"
        :position="{ top: '100%' }"
      >
        <common-dropdown
          :style="{ position: 'static' }"
          :class="{ show: showPartPicker }"
          :searchbox="true"
          :items="listUnassignedParts"
          :click-handler="onAddPart"
        ></common-dropdown>
      </common-popover>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "AssignTable",
  props: {
    isLoading: {
      type: Boolean,
      default: false,
    },
    projectName: {
      type: String,
      default: "",
    },
    listParts: {
      type: Array,
      default: () => [],
    },
    pagination: {
      type: Object,
      default: () => ({ page: 0, size: 0, totalPages: 0 }),
    },
    listUnassignedParts: {
      type: Array,
      default: () => [],
    },
    onFetchListPartsByProject: {
      type: Function,
      default: () => { },
    },
    onFetchUnassignedParts: {
      type: Function,
      default: () => { },
    },
    onRemovePart: {
      type: Function,
      default: () => { },
    },
    onAddPart: {
      type: Function,
      default: () => { },
    },
  },
  data() {
    return {
      tableRef: null,
      showPartPicker: false,
      isReachEnd: false,
    };
  },
  computed: {
    isLoadMore() {
      return (
        this.isReachEnd && this.pagination.page <= this.pagination.totalPages
      );
    },
  },
  methods: {
    truncateText(text, length){
      if (text.length > length) {
        return text.substring(0, length) + '...';
      } else {
        return text
      }
    },
    // toggle
    handleShowPartPicker() {
      this.showPartPicker = true;
    },
    handleClosePartPicker() {
      this.showPartPicker = false;
    },
    handleToggle() {
      if (!this.showPartPicker) {
        this.handleShowPartPicker();
      } else {
        this.handleClosePartPicker();
      }
    },
    handleShowMore() {
      this.isReachEnd = Common.listener.onReachEnd(this.tableRef);
    },
  },
  watch: {
    isLoadMore(newVal) {
      if (newVal) {
        this.pagination.page += 1;
        this.onFetchListPartsByProject({ page: this.pagination.page });
      }
    },
  },
  mounted() {
    this.tableRef = this.$refs.tableRef;
    this.$refs.tableRef.addEventListener("scroll", this.handleShowMore);
  },
  unmounted() {
    this.$refs.tableRef.removeEventListener("scroll", this.handleShowMore);
  },
};
</script>

<style scoped src="/components/category/assign-parts-to-project/assign-table.css"></style>
<style>
  .tooltip-assign-table {
    padding: 6px 8px;
  }
</style>