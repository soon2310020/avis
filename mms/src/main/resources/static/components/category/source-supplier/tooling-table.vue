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
    <tbody>
      <tr
        v-for="(row, index) in data"
        :key="index"
      >
        <td
          v-for="col in cols"
          :key="col.field"
          :style="{'text-align': col.align }"
        >
          <span @click="handleClickCell(row, col, index)">
            {{ !!col.formatter ? col.formatter(row[col.field]) : row[col.field]}}
          </span>
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

module.exports = {
  name: 'ToolingTable',
  props: {
    data: {
      type: Array,
      default: () => [],
    },
  },
  setup(props) {
    const colConfig = [
      configCol({
        label: 'Tooling',
        field: 'equipmentCode',
        align: 'left'
      }),
      configCol({
        label: 'Status',
        field: 'equipmentStatus',
        align: 'center'
      }),
      configCol({
        label: 'Utilization Rate',
        field: 'utilizationRate',
        align: 'right',
        formatter: val => (val || val === 0) ? val + '%' : '-'
      }),
      configCol({
        label: 'Latest Cycle Time',
        field: 'lastCycleTime',
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val / 10)
      }),
      configCol({
        label: 'Produced Quantity',
        field: 'totalProduced',
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val)
      }),
      configCol({
        label: 'Predicted Quantity',
        field: 'predictedQuantity',
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val)
      }),
      configCol({
        label: 'Production Capacity',
        field: 'totalMaxCapacity',
        align: 'right',
        formatter: val => Common.formatter.appendThousandSeperator(val)
      })
    ];

    const cols = ref(colConfig)

    const handleClickCell = (row, col, index) => {
      ctx.emit('click-cell', row, col, index)
    }

    watchEffect(() => console.log(props.data))

    return {
      cols,
      handleClickCell
    }
  }
}
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

.error-field {
  border: 1px solid #ef4444 !important;
  box-shadow: none !important;
  color: #ef4444;
}

.error-hint {
  color: #ef4444;
  margin-left: 10px;
  font-size: 12px;
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
}

.form-input:focus,
.form-input:focus-visible {
  outline: none;
}
</style>