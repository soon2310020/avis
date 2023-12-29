<template>
  <table
    class="table table-responsive-sm table-striped parts-produced-table"
    style="margin-bottom: 0"
  >
    <colgroup>
      <col />
      <col />
      <col />
      <col />
      <col />
    </colgroup>
    <thead id="thead-actionbar" class="custom-header-table">
      <tr style="height: 57px">
        <th
          v-for="(item, index) in columns"
          :key="index"
          :class="{ 'text-left': item.textLeft }"
          style="height: 80px"
        >
          <span>{{ item.label }}</span>
        </th>
      </tr>
    </thead>
    <tbody class="op-list">
      <tr v-for="(result, index) in dataList" :key="index">
        <td
          v-for="(column, index1) in columns"
          :class="{
            'text-left': column.textLeft,
            'content__hyper-link': column.isHyperLink,
          }"
          :key="index1"
        >
          <template v-if="column.field == 'machineCode'">
            <span @click="showMachineDetails(result.machineId)">{{
              textTransfer(result[column.field], column.formatter)
            }}</span>
          </template>
          <template v-else-if="column.field == 'expectedHourlyProduction'">
            <div v-if="result.expectedHourlyProduction">
              <base-drop-count
                :is-hyper-link="false"
                :data-list="result.expectedHourlyProduction"
                :title="
                  result.expectedHourlyProduction &&
                  result.expectedHourlyProduction[
                    result.expectedHourlyProduction.length - 1
                  ]
                    ? result.expectedHourlyProduction[
                        result.expectedHourlyProduction.length - 1
                      ].expectedProducedPart
                    : ''
                "
                @title-click="() => {}"
              >
                <div style="padding: 6px 8px">
                  <div
                    v-for="(item, index) in result.expectedHourlyProduction"
                    :key="index"
                    class="d-flex expected-hourly-prod"
                    style="min-width: 300px; color: #4b4b4b; line-height: 21px"
                  >
                    <span>{{ item.moldCode }}</span>
                    <span>{{ getExpireTime(item) }}</span>
                    <span>{{
                      `${item.expectedProducedPart} ${
                        item.expectedProducedPart > 1 ? "parts" : "part"
                      }`
                    }}</span>
                  </div>
                </div>
              </base-drop-count>
            </div>
          </template>
          <template v-else>
            <span>{{
              textTransfer(result[column.field], column.formatter)
            }}</span>
          </template>

          <template v-if="column.field == 'oee'">
            <div class="status-bar">
              <div
                class="current-status"
                :style="getStatusBarStyle(result.oee)"
              ></div>
            </div>
          </template>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
module.exports = {
  props: {
    columns: {
      type: Array,
      default: () => [],
    },
    dataList: {
      type: Array,
      default: () => [],
    },
    percentColor: Function,
  },
  methods: {
    textTransfer(text, formatter) {
      const result = text === "" ? "-" : text;
      return !formatter ? result : formatter(result);
    },
    getStatusBarStyle(oee) {
      return {
        backgroundColor: this.percentColor(oee, "OEE"),
        width: Math.ceil(oee / 2) + "px",
      };
    },
    showMachineDetails(machineId) {
      this.$emit("show-machine-details", machineId);
    },
    getExpireTime(expectedHourlyProduction) {
      const start = `${expectedHourlyProduction.start.slice(
        0,
        2
      )}:${expectedHourlyProduction.start.slice(2, 4)}`;
      const end = `${expectedHourlyProduction.end.slice(
        0,
        2
      )}:${expectedHourlyProduction.end.slice(2, 4)}`;
      return `${start} - ${end}`;
    },
  },
  watch: {
    columns(newVal) {
      console.log("columns", newVal);
    },
    dataList(newVal) {
      console.log("dataList", newVal);
    },
  },
};
</script>

<style>
.oee-bg-high-bar {
  background-color: #e34537;
}
.oee-bg-medium-bar {
  background-color: #f7cc57;
}
.oee-bg-low-bar {
  background-color: #41ce77;
}
.status-bar {
  height: 4px;
  width: 50px;
  background-color: #d6dade;
  position: relative;
}
.current-status {
  position: absolute;
  height: 4px;
  left: 0;
  right: 0;
}

.expected-hourly-prod > span {
  flex: 1;
  flex-shrink: 0;
}
.d-flex.expected-hourly-prod span:last-child {
  text-align: right;
}
</style>
