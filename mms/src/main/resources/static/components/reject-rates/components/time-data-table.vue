<template>
  <div class="table-content">
    <table class="table table-responsive-sm table-bordered" ref="tableRef">
      <thead>
        <tr>
          <th :style="{width: `${firstColWidth}px`, 'min-width': `${firstColWidth}px`}"></th>
          <template v-if="tableHeaders.length > 0">
            <th
              :style="{width: `${dataColWidth}px`, 'min-width': `${dataColWidth}px`}"
              v-for="(header, index) in tableHeaders"
              :key="index"
            >
              {{ date[index] }}
            </th>
          </template>
          <template v-else>
            <th></th>
          </template>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(field, fieldIndex) in Object.keys(tableData)"
          :key="fieldIndex"
        >
          <td class="row-title">
            {{ tableFields[field] }}
          </td>
          <template v-if="tableHeaders.length > 0">
            <td v-for="(item, index) in tableData[field]" :key="index">
              {{ (item == null || item == '')  ? "-" : item }}
            </td>
            <td v-for="index in getComplementColumn(tableData[field].length)" :key="index">-</td>
          </template>
          <template v-if="tableHeaders.length === 0 && fieldIndex === 0">
            <td class="detail-no-data" :rowspan="Object.keys(tableData).length">
              No data
            </td>
          </template>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    tableHeaders: Array,
    tableFields: Object,
    type: String,
    date: Array,
  },
  setup(props) {
    const tableData = ref({})
    const tableRef = ref(null)
    const isDaily = computed(() => props.type.include('DAILY'))
    const fromFormat = 'YYYY-MM-DD'
    const toFormat = 'MM/DD'

    const initTable = (data) => {
      tableData.value = data
    }

    const getComplementColumn = (current) => {
      props.tableHeaders.length - current
    }

    const getFormatedTitle = (title) => {
      if (isDaily) {
        return moment(title, fromFormat).format(toFormat)
      }
    }


    const firstColWidth = 200
    const dataColWidth = 200
    watch(() => props.tableHeaders, (newVal) => {
      const numberColData = newVal.length
      tableRef.value.style.width = `${firstColWidth + (dataColWidth * numberColData)}px`
      tableRef.value.style.borderRadius = `1px`
    })

    return {
      tableRef,
      tableData,
      initTable,
      getComplementColumn,
      getFormatedTitle,
      firstColWidth,
      dataColWidth
    }
  }
};
</script>

<style scoped>
.table-content {
  margin-top: 20px;
}
th,
td.row-title {
  background-color: #f5f8ff !important;
  color: #595959;
  border: unset !important;
}
.table-bordered {
  border-collapse: separate;
  border-spacing: 0px;
}
.table-bordered tbody tr td {
  border: unset;
}
</style>