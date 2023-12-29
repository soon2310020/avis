<template>
  <div class="summary-tooltip">
    <div class="summary-head">
      <div class="summary-title">{{ title }}</div>
      <div v-if="displayTotalCount" class="summary-total">
        {{ total }}
        <span class="unit">{{ total <= 1 ? unit[0] : unit[1] }}</span>
      </div>
      <div v-else></div>
    </div>

    <div class="divider"></div>

    <div class="summary-content">
      <div class="summary-content-headbar">
        <span v-for="col in colLabels" class="headbar-label">{{ col }}</span>
      </div>

      <div v-for="item in listItems" class="summary-content--item">
        <div class="summary-content--item__label">
          <emdn-table-status :category="item.category"></emdn-table-status>
        </div>
        <div class="summary-content--item__value">
          {{ item.value }}
        </div>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "SummaryTooltip",
  props: {
    title: String,
    total: [String, Number],
    unit: {
      type: Array,
      default: () => ["item", "items"],
      validator(val) {
        return Array.isArray(val) && val.length === 2;
      },
    },
    colLabels: Array,
    tooltip: String, // need to add later
    listItems: Array,
  },
  data() {
    return {
      results: [],
    };
  },
  computed: {
    displayTotalCount() {
      return this.total !== undefined;
    },
  },
  methods: {},
  watch: {},
  created() {},
  mounted() {},
};
</script>

<style scoped>
.summary-tooltip {
  flex: 1;
}

.summary-head {
  display: flex;
  justify-content: space-between;
}
.summary-title {
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
}

.summary-total {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
}

.divider {
  border-bottom: 1px solid #000;
  margin: 10px 0;
  opacity: 0.2;
}

.summary-content {
}

.summary-content-headbar {
  display: flex;
  justify-content: space-between;
}

.headbar-label {
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
}

.summary-content--item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  margin-top: 10px;
  font-size: 14.66px;
  line-height: 17px;
}

.summary-content--item__label {
  display: inline-flex;
  align-items: center;
}
</style>
