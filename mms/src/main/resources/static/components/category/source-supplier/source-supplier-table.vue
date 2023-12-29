<template>
  <table class="table-striped">
    <thead>
      <tr class="tr-sort">
        <th
          v-for="col in cols"
          :key="col.field"
          :style="{'text-align': col.align }"
        >
          <span>{{ col.label }}</span>
        </th>
      </tr>
    </thead>
    <tbody v-if="!hasNoData">
      <tr
        v-for="(row, index) in data"
        :key="row.id"
      >
        <td
          v-for="col in cols"
          :key="col.field"
          :style="{'text-align': col.align }"
        >

          <span
            v-if="col.field === 'totalProductionDemand'"
            style="position: relative;"
          >
            <input
              class="form-input"
              type="number"
              min="0"
              style="text-align: right;"
              v-model="row[col.field]"
              @blur="handleBlur($event, row, col, index)"
            >
          </span>

          <span v-else-if="col.field === 'moldCount'">
            <a
              href="javascript:void(0)"
              @click="handleClickCell(row, col, index)"
            >{{col.formatter(row[col.field])}}</a>
          </span>

          <span v-else-if="col.field === 'name'">
            <a
              href="javascript:void(0)"
              @click="handleClickCell(row, col, index)"
            >{{row[col.field]}}</a>
          </span>

          <span v-else-if="col.field === 'deliveryRiskLevel'">
            <span
              :class="[row[col.field] ? row[col.field].toLowerCase() : '']"
              class="risk-level__icon"
              @click="handleClickCell(row, col, index)"
            >
            </span>
            {{ col.formatter(row[col.field]) }}
          </span>

          <span
            v-else
            @click="handleClickCell(row, col, index)"
          >
            {{ !!col.formatter ? col.formatter(row[col.field]) : row[col.field]}}
          </span>
        </td>
      </tr>
    </tbody>
    <tbody v-else>
      <tr>
        <td colspan="100">
          <div style="height: 300px; display: flex; align-items: center; justify-content: center;">
            No results
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
const configCol = ({ label, field, formatter, align }) => ({
  label,
  field,
  formatter,
  align: align || 'left',
})

const RISK_LEVEL = {
  LOW: 'Low Risk',
  MEDIUM: 'Medium Risk',
  HIGH: 'High Risk'
}

// emits = ['click-cell']
module.exports = {
  name: "SourceSupplierTable",
  props: {
    data: {
      type: Array,
      default: () => [],
    },
  },
  setup(props, ctx) {
    const colConfig = [
      configCol({
        label: "Supplier",
        field: "name",
        isSort: false
      }),
      configCol({
        label: "Number of Toolings",
        field: "moldCount",
        align: 'right',
        formatter: val => val === 1 ? `${val} Tooling` : `${val} Toolings`,
        isSort: false
      }),
      configCol({
        label: "Produced Quantity",
        field: "totalProduced",
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val),
        isSort: false
      }),
      configCol({
        label: "Remaining Capacity",
        field: "remainingCapacity",
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val),
        isSort: false
      }),
      configCol({
        label: "Production Demand",
        field: "totalProductionDemand",
        align: 'left',
        isSort: false
      }),
      configCol({
        label: "Delivery Risk",
        field: "deliveryRiskLevel",
        align: 'center',
        formatter: val => RISK_LEVEL[val],
        isSort: false
      }),
    ];

    const cols = ref(colConfig)

    const hasNoData = computed(() => props.data.length === 0)

    const handleClickCell = (row, col, index) => {
      ctx.emit('click-cell', row, col, index)
    }

    const handleBlur = (event, row, col, index) => {
      if (row.totalProductionDemand) {
        row.totalProductionDemand = row?.totalProductionDemand?.toString()?.replace(/\D/g, '') || '';
      }
      ctx.emit('change')
    }

    return {
      cols,
      handleClickCell,
      handleBlur,
      hasNoData
    };
  },
};
</script>

<style scoped>
/* TABLE */
table {
  width: 100%;
  border: 1px solid #c8ced3;
}

table thead {
  background-color: rgb(245, 248, 255);
  position: sticky;
  top: 0;
  z-index: 99;
}

table thead tr th {
  padding: 17px;
}

table tbody tr td {
  padding: 7px 17px;
}

.table-striped tbody tr:nth-of-type(odd) {
  background-color: #fbfcfd !important;
}

.table-striped td,
.table-striped th {
  text-align: left;
}

.form-input {
  border: 1px solid #c4c4c4;
  padding: 6px 8px;
  font-size: 14px;
  line-height: 15px;
  border-radius: 2px;
  width: 100%;
}

.form-input:focus,
.form-input:focus-visible {
  outline: none;
}
</style>