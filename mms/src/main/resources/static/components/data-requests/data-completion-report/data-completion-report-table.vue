<template>
  <div style="margin-top: 24px">
    <div class="row">
      <div class="col-lg-12">
        <div :class="{ 'wave1': isLoading }">
          <div style="overflow-x: auto !important">
            <div>
              <table style="overflow-x: auto !important; border: 1px solid rgb(201, 206, 210); border-top: 0px;"
                class="table table-responsive-sm table-striped">
                <thead id="thead-actionbar" class="custom-header-table" style="top: 0px">
                  <tr style="height: 57px" class="tr-sort">
                    <template v-for="(column, index) in allColumnListDisplay">
                      <th :key="index" :class="['text-left']">
                        <span>{{ resources[column.labelKey] }}</span>
                      </th>
                    </template>
                  </tr>
                </thead>
                <tbody class="op-list">
                  <tr v-show="!isLoading" v-for="(result) in tableResults.content" :key="result.id" :id="result.id">
                    <template v-for="(column, columnIndex) in allColumnListDisplay">

                    <td v-if="column.field === 'rate'" :key="columnIndex" class="text-left">
                      {{ (result[column.field] || 0) + '%' }}
                    </td>
                    <td v-else-if="column.field === 'code'" :key="columnIndex"
                        class="text-left item-table " :class="{'custom-hyper-link': objectType !== 'LOCATION'}">
                      <i style="color: rgb(26, 170, 85)" v-if="objectType === 'TOOLING'" @click.prevent="showChart(result.data)" class="fa fa-bar-chart mr-1"></i>
                      <span @click="() => onClickCode(result.data)">{{ result[column.field] }}</span>
                    </td>
                    <td v-else-if="column.field === 'name' && objectType === 'TOOLING'" :key="columnIndex" class="text-left item-table custom-hyper-link">
                      {{ result[column.field] }}
                    </td>
                      <td v-else-if="column.labelKey === 'action'" :key="columnIndex" class="text-left">
                        <div v-if="result.rate === 100" class="completed">{{ resources['100_Complete'] }}</div>
                        <base-button v-else level="secondary" @click="handleShowCompletion(result)">
                          {{ resources['complete_data']}}
                        </base-button>
                      </td>
                    <td v-else :key="columnIndex" class="text-left item-table"> {{ result[column.field] }} </td>
                  </template>
                </tr>
                </tbody>
              </table>
              <div v-show="tableResults?.content?.length === 0" class="no-results" v-text="resources['no_results']">
              </div>
              <div v-show="!isLoading" class="row">
                <div class="col-md-10 col-sm-10 col-xs-10">
                  <ul class="pagination">
                    <li v-for="(data, index) in pagination" :key="index" class="page-item"
                      :class="{ active: data.isActive }">
                      <a class="page-link" @click="paging(data.pageNumber)"> {{
                        data.text
                      }} </a>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <machine-details :resources="resources" ref="machine-details"></machine-details>
    <part-details :resources="resources" ref="part-details"></part-details>
    <mold-details :resources="resources" ref="mold-details"></mold-details>
    <company-details :resources="resources" ref="company-details"></company-details>
    <chart-mold :show-file-previewer="showFilePreviewer" :resources="resources" ref="chart-mold"></chart-mold>
    <file-previewer :back="backToMoldDetail"></file-previewer>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({})
    },
    tableResults: {
      type: Object,
      default: () => ({})
    },
    isLoading: {
      type: Boolean,
      default: () => false
    },
    objectType: {
      type: String,
      default: () => ''
    },
    pagination: {
      type: Array,
      default: () => []
    }
  },
  components: {
    'machine-details': httpVueLoader('/components/machine-details.vue'),
    'part-details': httpVueLoader('/components/part-details.vue'),
    // 'mold-details': httpVueLoader('/components/mold-details.vue'),
    'file-previewer': httpVueLoader('/components/mold-detail/file-previewer.vue'),
    'chart-mold': httpVueLoader('/components/chart-mold/chart-mold-modal.vue'),
    'company-details': httpVueLoader('/components/company-details.vue'),
  },
  setup(props, ctx) {
    // STATE //
    // const pagination = ref([]);
    const allColumnList = ref([])

    // COMPUTED //
    const allColumnListDisplay = computed(() => {
      if (props.objectType === 'COMPANY') {
        return _.filter(allColumnList.value, column => ['company_id', 'company_name', 'data_completion_rate', 'action'].includes(column.labelKey))
      }
      if (props.objectType === 'TOOLING') {
        return _.filter(allColumnList.value, column => ['tooling_id', 'data_completion_rate', 'action'].includes(column.labelKey))
      }
      if (props.objectType === 'LOCATION') {
        return _.filter(allColumnList.value, column => ['location_id', 'location_name', 'data_completion_rate', 'action'].includes(column.labelKey))
      }
      if (props.objectType === 'PART') {
        return _.filter(allColumnList.value, column => ['part_id', 'part_name', 'data_completion_rate', 'action'].includes(column.labelKey))
      }
      if (props.objectType === 'MACHINE') {
        return _.filter(allColumnList.value, column => ['machine_id', 'data_completion_rate', 'action'].includes(column.labelKey))
      }
      return allColumnList.value
    })

    // WATCH //


    // METHOD //

    const showMachineDetails = (result) => {
      var child = ctx.refs['machine-details']
      if (!!child) {
        child.showDetails(result);
      }
    }
    const showPartDetails = (result) => {
      var child = ctx.refs['part-details']
      if (!!child) {
        child.showPartDetails(result);
      }
    }
    const showCompanyDetails = (result) => {
      var child = ctx.refs['company-details']
      if (!!child) {
        child.showDetails(result);
      }
    }
    const showChart = (result, tab) => {
      var child = ctx.refs['chart-mold']
      if (child != null) {
        child.showChart(result, 'QUANTITY', 'DAY', tab ? tab : 'switch-graph');
      }
    };
    const showFilePreviewer = (file) => {
      var child = ctx.refs['file-previewer'];
      if(child){
        child.showFilePreviewer(file);
      }
    }
    const backToMoldDetail = () => {
      var child = ctx.refs['chart-mold']
      if (child != null) {
        child.backFromPreview();
      }
    }
    const onClickCode = (result) => {
      console.log("result", result)
      if(props.objectType === 'MACHINE'){
        showMachineDetails(result)
      }
      if(props.objectType === 'PART'){
        showPartDetails(result)
      }
      if(props.objectType === 'TOOLING'){
        showChart(result, 'switch-detail')
      }
      if(props.objectType === 'COMPANY'){
        showCompanyDetails(result)
      }
    }
    const paging = (pageNumber) => {
      console.log("table", props.tableResults)
      ctx.emit('paging', pageNumber)
    }

    const setColumnList = () => {
      allColumnList.value = [
        { label: 'Company ID', labelKey: 'company_id', field: 'code' },
        { label: 'Company Name', labelKey: 'company_name', field: 'name' },
        { label: 'Tooling ID', labelKey: 'tooling_id', field: 'code' },
        { label: 'Location ID', labelKey: 'location_id', field: 'code' },
        { label: 'Location Name', labelKey: 'location_name', field: 'name' },
        { label: 'Part ID', labelKey: 'part_id', field: 'code' },
        { label: 'Part Name', labelKey: 'part_name', field: 'name' },
        { label: 'Machine ID', labelKey: 'machine_id', field: 'code' },
        { label: 'Machine Name', labelKey: 'machine_name', field: 'name' },
        { label: 'Data Completion Rate', labelKey: 'data_completion_rate', field: 'rate', },
        { label: 'Action', labelKey: 'action' },
      ]
    }

    const handleShowCompletion = (item) => {
      ctx.emit('show-completion', item)
    }
    // created method //
    const initModal = async () => {
      setColumnList()
    }

    // CREATED //
    initModal()

    return {
      // STATE //
      // COMPUTED //
      allColumnListDisplay,
      // METHOD //
      paging,
      onClickCode,
      showFilePreviewer,
      backToMoldDetail,
      showChart,
      handleShowCompletion
    }
  }
}
</script>

<style scoped>
.item-table {
  max-width: 206px;
  width: 206px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.completed {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 8px;
  border-radius: 3px;
  font-size: 14px;
  color: rgb(77, 183, 122);
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  width: fit-content;
  height: 30px;
}
</style>