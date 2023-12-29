<template>
  <div>
    <div style="padding: 1.25rem 0; display: flex; justify-content: flex-end; margin-top: -60px;">
      <customization-modal style="margin-left: 8px; " :all-columns-list="allColumnList" @save="saveSelectedList"
        :resources="resources"></customization-modal>
    </div>
    <div class="reject-rate-machine card">

      <div class="card-body" style="padding-top: 0; height: 600px;">
        <table class="table table-responsive-sm table-striped">
          <thead id="thead-actionbar" class="custom-header-table">
            <tr style="height: 57px" class="tr-sort">
              <th v-for="col in getColumns" :key="col.field"
                :class="[col.align != 'left' ? col.align == 'right' ? `text-right ${col.sortable ? 'pr-4' : ''}` : 'text-center' :'text-left', {__sort: col.sortable, active: sortType === col.field}, isDesc ? 'desc': 'asc']"
                style="border-top: unset;"
                @click="sortBy(col.sortField)">
                <span>{{ col.label }}</span>
                <span v-if="col.sortable" class="__icon-sort"></span>
              </th>
            </tr>
          </thead>
          <tbody class="op-list">
            <tr v-if="hasNoData">
              <td colspan="100" style="background-color: #fff;">
                <div style="display: flex; height: 300px; justify-content: center; align-items: center;">
                  <span v-text="resources['no_result']"></span>
                </div>
              </td>
            </tr>
            <template v-else>
              <tr v-for="(row, index) in listDataLocal" :key="index">
                <td v-for="col in getColumns" :key="col.field"
                  :class="[col.align ? `text-${col.align}` : 'text-left', col.hyperlink ? 'hyperlink' : '']">
                  <span v-if="col.field === 'rejectedRateStatus' && !!row[col.field]">
                    <div class="reject-rate-status" :class="getStatusClass(row[col.field])"></div>
                    <label>{{ row[col.field] }}</label>
                  </span>

                  <span v-else-if="col.field == 'machineCode'" @click="showMachineDetails(row)">
                    {{
                    col.formatter ? col.formatter(row[col.field]) : row[col.field]
                    }}
                  </span>
                  <div v-else-if="col.field == 'moldId'">                    
                    <base-drop-count v-if="row.moldList.length" :is-hyper-link="true" :data-list="row.moldList"
                      :title="row.moldList[0] ? row.moldList[0].code : ''" @title-click="showMoldChart(row.moldList[0].id)">
                      <div style="padding: 6px 8px;">
                        <div v-for="item in row.moldList" :key="item.id"
                          @click="showMoldChart(item.id)"
                          style="display: inline-flex; width: fit-content; white-space: nowrap; cursor: pointer;">
                          <span class="custom-hyper-link">{{item.code}}</span>
                        </div>
                      </div>
                    </base-drop-count>
                    <span v-else> - </span>
                  </div>
                  <div v-else-if="col.field == 'entryRecord'">
                    <span v-if="row[col.field]" class="custom-hyper-link" @click="showEntryRecord(row)">
                      {{getEntryRecordTitle(row[col.field])}}
                      <div class="hyper-link-icon" style="margin-left: 3px"></div>
                    </span>
                    <span v-else>{{getEntryRecordTitle(row[col.field])}}</span>
                  </div>
                  <span v-else>{{ col.formatter ? col.formatter(row[col.field]) : row[col.field] }}</span>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
        <!--      <div class="category-data-table__footer d-flex justify-content-end" v-if="pagination.totalPages > 0">-->
        <!--        <div class="modal-body__content__footer">-->
        <!--          <span>{{-->
        <!--          `${pagination.pageNumber} of ${pagination.totalPages}`-->
        <!--          }}</span>-->
        <!--        </div>-->
        <!--        <div class="paging__arrow">-->
        <!--          <a href="javascript:void(0)" class="paging-button" :class="{ 'inactive-button': pagination.pageNumber <= 1 }"-->
        <!--            @click="$emit('paginate', pagination.pageNumber - 1)">-->
        <!--            <img src="/images/icon/category/paging-arrow.svg" alt="" />-->
        <!--          </a>-->
        <!--          <a href="javascript:void(0)" class="paging-button" @click="$emit('paginate', pagination.pageNumber + 1)"-->
        <!--            :class="{-->
        <!--              'inactive-button': pagination.pageNumber >= pagination.totalPages,-->
        <!--            }">-->
        <!--            <img src="/images/icon/category/paging-arrow.svg" style="transform: rotate(180deg)" alt="" />-->
        <!--          </a>-->
        <!--        </div>-->
        <!--      </div>-->
      </div>
    </div>
  </div>

</template>

<script>
module.exports = {
  name: 'RejectRateMachine',
  props: {
    resources: Object,
    isShow: Boolean,
    pagination: {
      type: Object,
      default: () => ({ pageNumber: 1, totalPages: 1 })
    },
    listData: { type: Array, default: () => [] },
    sortType: { type: String, default: () => ('') },
    isDesc: { type: Boolean, default: () => (true) },
    permissions: {
      type: Object,
      default: () => ({})
    },
    detailPermissions: {
      type: Object,
      default: () => ({})
    }
  },
  components: {
    'customization-modal': httpVueLoader('/components/customization-modal.vue'),
  },
  setup(props, ctx) {

    // console.log('props: ', props);
    // console.log('permission Reject Rate', this.permissions)

    const isCheckAll = ref(false)
    const listDataLocal = ref([])
    const hasNoData = computed(() => props.listData.length === 0)
    const pageType = ref('OEE_PART_PRODUCED')

    const getStatusClass = status => {
      if (status === 'COMPLETED') return props.resources['completed']
      if (status === 'UNCOMPLETED') return props.resources['uncompleted']
      return ''
    }

    // console.log('resources: ', props.resources);

    // config
    const colConfig = [
      {
        label: props.resources['machine_id'],
        field: 'machineCode',
        align: 'left',
        hyperlink: true,
        formatter: val => !val ? '-' : val
      },
      {
        label: props.resources['location'],
        field: 'location',
        align: 'left',
        formatter: val => !val ? '-' : val
      },
      {
        label: props.resources['tooling_id'],
        field: 'moldId',
        align: 'left',
      },
      {
        label: props.resources['total_produced_amount'],
        field: 'totalProducedAmount',
        align: 'right',
        hyperlink: false,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(val)
      },
      {
        label: 'Yield',
        field: 'yieldRate',
        align: 'right',
        hyperlink: false,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(Number(val).toFixed(1)) + '%'
      },
      {
        label: props.resources['rejected_parts'],
        field: 'totalRejectedAmount',
        align: 'right',
        hyperlink: false,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(val)
      },
      {
        label: props.resources['status'],
        field: 'rejectedRateStatus',
        align: 'center',
        hyperlink: false,
        formatter: val => !val ? '-' : val
      }
    ]
    const cols = ref(colConfig)

    const allColumnList = ref([
      {
        label: props.resources['machine_id'],
        field: 'machineCode',
        align: 'left',
        hyperlink: true,
        sortField: 'machineCode',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 0, enabled: true,
        formatter: val => !val ? '-' : val
      },
      {
        label: props.resources['tooling_id'],
        field: 'moldId',
        align: 'left',
        sortField: 'moldId',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 1, enabled: true,
      },
      {
        label: props.resources['location'],
        field: 'location',
        align: 'left',
        sortField: 'location',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 2, enabled: true,
        formatter: val => !val ? '-' : val
      },
      {
        label: props.resources['total_produced_amount'],
        field: 'totalProducedAmount',
        align: 'right',
        hyperlink: false,
        sortField: 'totalProducedAmount',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 3, enabled: true,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(val)
      },
      {
        label: 'Yield',
        field: 'yieldRate',
        align: 'right',
        hyperlink: false,
        sortField: 'yieldRate',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 4, enabled: true,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(Number(val).toFixed(1)) + '%'
      },
      {
        label: props.resources['rejected_parts'],
        field: 'totalRejectedAmount',
        align: 'right',
        hyperlink: false,
        sortField: 'totalRejectedAmount',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 5, enabled: true,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(val)
      },
      {
        label: props.resources['entry_record'],
        field: 'entryRecord',
        align: 'left',
        hyperlink: false,
        sortField: 'entryRecord',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 6, enabled: true,
      },
      {
        label: props.resources['reject_rate'],
        field: 'rejectedRate',
        align: 'right',
        hyperlink: false,
        sortField: 'rejectedRate',
        sortable: true, default: false, defaultSelected: false, defaultPosition: 7, enabled: false,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(Number(val).toFixed(1)) + '%'
      },
      // {
      //   label: props.resources['status'],
      //   field: 'rejectedRateStatus',
      //   align: 'center',
      //   hyperlink: false,
      //   sortable: true, default: true,  defaultSelected: true, defaultPosition: 6,
      //   formatter: val => !val ? '-' : val
      // },
    ])

    const getColumns = computed(() => {
      const currentColumn = allColumnList.value.filter(item => item.enabled)
      console.log('getColumns', allColumnList.value, currentColumn)
      return currentColumn
    })

    // tracking after 3 state, only call api when the content is showed
    watch(isCheckAll, newVal => {
      if (newVal) { listDataLocal.value = listDataLocal.value.map(item => ({ ...item, checked: true })) }
      else { listDataLocal.value = listDataLocal.value.map(item => ({ ...item, checked: false })) }
    })
    watch(() => props.listData, newVal => listDataLocal.value = newVal, { deep: true, immediate: true })

    watch(() => props.sortType, newVal => { console.log('change sort type', newVal) }, { deep: true, immediate: true })

    // expose
    watchEffect(() => console.log('props.listData', props.listData))

    //handler
    const showMachineDetails = (machine) => {
      console.log(machine)
      ctx.emit('show-machine-details', machine.machineId)
    }

    const showEntryRecord = (machine) => {
      ctx.emit('show-entry-record', machine.machineId)
    }

    const saveSelectedList = (dataCustomize, list) => {
      console.log('saveSelectedList', list)
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index }
        }
      });
      allColumnList.value = dataFake
      let data = list.map((item, index) => {
        let response = {
          columnName: item.field,
          enabled: item.enabled,
          position: index,
        };
        if (item.id) {
          response.id = item.id;
        }
        return response;
      });
      axios.post("/api/config/update-column-config", {
        pageType: pageType.value,
        columnConfig: data
      }).then((response) => {
        console.log('this is the current pagetype:', pageType.value);
        let hashedByColumnName = {};
        response.data.forEach(item => {
          hashedByColumnName[item.columnName] = item;
        });
        allColumnList.value.forEach(item => {
          if (hashedByColumnName[item.field] && !item.id) {
            item.id = hashedByColumnName[item.field].id;
            item.position = hashedByColumnName[item.field].position
          }
        });
      }, () => {
        allColumnList.value.sort((a, b) => {
          return a.position - b.position;
        });
      })
    }

    const getEntryRecordTitle = (entryNumber) => {
      if (!entryNumber) return '-'
      if (entryNumber === 1) return entryNumber + ' ' + props.resources['entry']
      return entryNumber + ' ' + props.resources['entries']
    }

    const handleClickEntryRecord = (item) => {
      console.log('handleClickEntryRecord', item)
    }

    const sortBy = (field) => {
      console.log('sort by', field)
      ctx.emit('sort', field)
    }

    const getColumnSetting = () => {
      axios.get(`/api/config/column-config?pageType=${pageType.value}`).then(async (response) => {
        const currentColumns = [...allColumnList.value]
        const loadColumn = response.data.map(item => {
          let newItem = { ...item }
          newItem.field = newItem.columnName
          return newItem
        })
        allColumnList.value = currentColumns.map(item => {
          let newItem = { ...item }
          const findItem = loadColumn.find(o => o.columnName === newItem.field)
          if (findItem) {
            newItem = { ...newItem, ...findItem }
            newItem.enabled = findItem.enabled
          }
          return newItem
        })
        // console.log('column after load', loadColumn, currentColumns)
        // const mergedColumn = _.unionBy(loadColumn, currentColumns, 'field')
        // allColumnList.value = mergedColumn
        console.log('column after merge', allColumnList.value)
      }).finally((error) => {
        console.log(error)
      });
    }

    const showMoldChart = (id) => {
      ctx.emit('show-mold-chart', id)
    }

    // CREATED //
    getColumnSetting()

    return {
      cols,
      isCheckAll,
      hasNoData,
      listDataLocal,
      allColumnList,
      getColumns,
      getStatusClass,
      showMachineDetails,
      showEntryRecord,
      saveSelectedList,
      getEntryRecordTitle,
      handleClickEntryRecord,
      sortBy,
      showMoldChart
    }
  }
}
</script>


<style scoped>
.table th {
  padding-right: 0.5em;
}

.paging-button {
  padding: 6px 8px;
  border-radius: 3px;
  background-color: rgb(52, 145, 255);
  margin-left: 15px;
  display: inline-flex;
}

.paging-button.inactive-button {
  background-color: rgb(196, 196, 196);
  pointer-events: none;
}

.reject-rate-status {
  border-radius: 50%;
  width: 10px;
  height: 10px;
  display: inline-block;
  margin-right: 4px;
}

.reject-rate-status.completed {
  background-color: #1aaa55;
}

.reject-rate-status.uncompleted {
  background-color: rgb(219, 59, 33);
}

.op-list td label {
  margin-bottom: 0;
}

</style>