<template>
  <div>
    <div class="reject-rate-machine card" style="display: block; height: 500px; overflow: auto;">
      <div style="padding-top: 0; height: 500px;">
        <table class="table table-responsive-sm">
          <thead id="thead-actionbar" class="custom-header-table" style="top: 0">
          <tr style="height: 57px" class="tr-sort">
            <th v-for="col in getColumns" :key="col.field"
                :class="[col.align ? 'text-' + col.align : 'text-left', {__sort: col.sortable, active: currentSortType === col.sortField}, isDesc ? 'desc': 'asc']"
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
            <tr v-for="(row, index) in listData" :key="index">
              <template 
                v-for="col in getColumns"
                :class="[col.align ? col.align : 'left', col.hyperlink ? 'hyperlink' : '']"
              >
                <td :key="col.field" v-if="col.field === 'period'" class="text-left">
                  {{ row.start.slice(0, 2) + ':' + row.start.slice(2, 4) + ' - ' + row.end.slice(0, 2) + ':' + row.end.slice(2, 4) }}
                </td>
                <td :key="col.field" v-else-if="col.field === 'shift'" class="text-left">
                  Shift {{ row.shiftNumber }}
                </td>
                <td :key="col.field" v-else-if="col.field === 'moldId'" class="text-left">
                  <base-drop-count :is-hyper-link="false" :data-list="row.moldList"
                                   :title="row.moldList[0] ? row.moldList[0].code : ''" @title-click="() => {}">
                    <div style="padding: 6px 8px;">
                      <div v-for="item in row.moldList" :key="item.id"
                           @click="() => {}"
                           style="display: inline-flex; width: fit-content; white-space: nowrap; cursor: pointer;">
                        {{item.code}}
                      </div>
                    </div>
                  </base-drop-count>
                </td>
                <td :key="col.field" v-else-if="col.field === 'reportedBy'" class="text-left">
                  <user-list-cell v-if="row.reportedBy" :resources="resources" :user-list="[row.reportedBy]"></user-list-cell>
                </td>
                <td :key="col.field" v-else-if="col.field === 'totalRejectedAmount'" class="text-right">
                  <span>
                    {{ row.totalRejectedAmount }}
                  </span>
                </td>
                <td :key="col.field" v-else-if="col.field === 'downtimeReason'" class="text-left">
                  <reason-dropdown :reasons="row.rejectedPartDetails" :total-reject-amount="row.totalRejectedAmount" :hour-converted="convertHour(row.start, row.end)" :index="index" ></reason-dropdown>
                </td>
              </template>
            </tr>
          </template>
          </tbody>
        </table>
      </div>
    </div>
    <div style="margin-bottom: 35px;" class="category-data-table__footer d-flex justify-content-end" v-if="pagination.totalPages > 0">
      <div class="modal-body__content__footer">
          <span>{{
              `${pagination.pageNumber} of ${pagination.totalPages}`
            }}</span>
      </div>
      <div class="paging__arrow">
        <a href="javascript:void(0)" class="paging-button" :class="{ 'inactive-button': pagination.pageNumber <= 1 }"
           @click="$emit('paginate', pagination.pageNumber - 1)">
          <img src="/images/icon/category/paging-arrow.svg" alt="" />
        </a>
        <a href="javascript:void(0)" class="paging-button" @click="$emit('paginate', pagination.pageNumber + 1)"
           :class="{
              'inactive-button': pagination.pageNumber >= pagination.totalPages,
            }">
          <img src="/images/icon/category/paging-arrow.svg" style="transform: rotate(180deg)" alt="" />
        </a>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: 'EntryRecordDetails',
  props: {
    resources: Object,
    isShow: Boolean,
    pagination: {
      type: Object,
      default: () => ({ pageNumber: 1, totalPages: 1 })
    },
    listData: { type: Array, default: () => [] },
    hourConverted: String
  },
  components: {
    'customization-modal': httpVueLoader('/components/customization-modal.vue'),
    'reason-dropdown': httpVueLoader('/components/oee-center/machine-reject-rate/reason-dropdown.vue')
  },
  methods: {
    convertHour (start, end) {
      return start.slice(0, 2) + ':' + start.slice(2, 4) + ' - ' + end.slice(0, 2) + ':' + end.slice(2, 4)
    }
  },
  setup(props, ctx) {
    const hasNoData = computed(() => props.listData.length === 0)
    const currentSortType = ref('')
    const isDesc = ref(false)

    const getStatusClass = status => {
      if (status === 'COMPLETED') return props.resources['completed']
      if (status === 'UNCOMPLETED') return props.resources['uncompleted']
      return ''
    }

    // console.log('resources: ', props.resources);

    const allColumnList = ref([
      {
        label: props.resources['period'],
        field: 'period',
        align: 'left',
        hyperlink: true,
        sortField: 'period',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 0, enabled: true
      },
      {
        label: props.resources['shift'],
        field: 'shift',
        align: 'left',
        sortField: 'shift',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 2, enabled: true
      },
      {
        label: props.resources['tooling_id'],
        field: 'moldId',
        align: 'left',
        sortField: 'moldId',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 1, enabled: true,
      },
      {
        label: props.resources['reported_by'],
        field: 'reportedBy',
        default: false,
        align: 'left',
        sortable: true,
        sortField: 'reportedBy',
        defaultSelected: true,
        defaultPosition: 9,
        enabled: true
      },
      {
        label: props.resources['number_of_rejected_parts'],
        field: 'totalRejectedAmount',
        align: 'right',
        hyperlink: false,
        sortField: 'totalRejectedAmount',
        sortable: true, default: true, defaultSelected: true, defaultPosition: 5, enabled: true,
        formatter: val => (!val && val !== 0) ? '-' : Common.formatter.appendThousandSeperator(val)
      },
      {
        label: props.resources['reason'],
        field: 'downtimeReason',
        default: true,
        sortable: true,
        sortField: 'downtimeReason',
        defaultSelected: true,
        defaultPosition: 4,
        enabled: true
      }
    ])

    const getColumns = computed(() => {
      const currentColumn = allColumnList.value.filter(item => item.enabled)
      console.log('getColumns', allColumnList.value, currentColumn)
      return currentColumn
    })

    // expose
    watchEffect(() => console.log('props.listData', props.listData))

    //handler
    const showMachineDetails = (machine) => {
      console.log(machine)
      ctx.emit('show-machine-details', machine.machineId)
    }

    const saveSelectedList = (dataCustomize, list) => {
      console.log('saveSelectedList', list)
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index }
        }
      });
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
      allColumnList.value = dataFake
      console.log('saveSelectedList after', allColumnList.value)
    }

    const getEntryRecordTitle = (entryNumber) => {
      if (entryNumber > 1) {
        return `${entryNumber} Entries`
      } else {
        return `${entryNumber} Entry`
      }
    }

    const handleClickEntryRecord = (item) => {
      console.log('handleClickEntryRecord', item)
    }

    const sortBy = (field) => {
      console.log('sort by', field)
    }

    return {
      hasNoData,
      allColumnList,
      getColumns,
      isDesc,
      currentSortType,
      getStatusClass,
      showMachineDetails,
      saveSelectedList,
      getEntryRecordTitle,
      handleClickEntryRecord,
      sortBy
    }
  }
}
</script>

<style scoped>
.reject-rate-machine {
  border-top: unset;
}
thead tr {
  background: #F5F8FF;
}
.table td {
  border-bottom: none;
  padding: 13px 17px;
  text-align: left;
}
.table th {
  padding: 13px 17px;
}
tbody tr:nth-of-type(even) {
  background-color: #F5F8FF;
}
.op-list {
  color: #4B4B4B;
}
.op-list td label {
  margin-bottom: 0;
}
</style>