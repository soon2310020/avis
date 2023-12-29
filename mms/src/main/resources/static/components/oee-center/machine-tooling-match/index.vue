<template>
    <div class="data-table__main">
        <table class="data-grid__table">
            <thead class="data-grid__head">
                <!-- ACTION BARS -->
                <tr v-show="showActionBar" class="data-grid__row">
                    <th class="data-grid__cell cell-checkbox">
                        <input type="checkbox" v-model="isCheckAll" />
                    </th>
                    <th class="data-grid__cell" colspan="100">
                        <div class="action-bar">
                            <span v-for="item in listActionsEnable" :key="item.key" class="action-item" @click="handleClickAction(item.key)">
                                <span class="action-item__label">{{ item.label }}</span>
                                <span class="action-item__icon" :style="`mask-image: url(${item.iconSrc}); -webkit-mask-image: url(${item.iconSrc}); ${item.iconStyle}`"></span>
                            </span>
                        </div>
                    </th>
                </tr>
                <!-- DEFAULT HEADER -->
                <tr v-show="!showActionBar" class="data-grid__row">
                    <th class="data-grid__cell cell-checkbox">
                        <input type="checkbox" v-model="isCheckAll" />
                    </th>
                    <th v-for="col in enabledColumns" :key="col.field" class="data-grid__cell" :class="[
                        `cell-${col.field}`,
                        { 'cell-active': col.field === sortColumn },
                    ]">
                        <span class="cell-inner">
                            <span class="cell-inner__label"> {{ col.label }} </span>
                            <span v-if="col.sort" class="cell-inner__sort" :class="{ 'desc': sortDesc && col.field === sortColumn }" @click="() => handleSort(col.field)">
                                <span class="cell-inner__sort-icon"></span>
                            </span>
                        </span>
                    </th>
                </tr>
            </thead>
            <tbody class="data-grid__body">
                <tr v-for="(row, rowIndex) in listRenderedItems" :key="rowIndex" class="data-grid__row">
                    <td class="data-grid__cell cell-checkbox">
                        <input type="checkbox" v-model="listRenderedItems[rowIndex].checked" />
                    </td>
                    <td v-for="col in enabledColumns" :key="col.field" class="data-grid__cell">
                        <!-- HAS SUBFIELD -->
                        <div v-if="col.subField" class="cell-inner">
                            <span class="cell-inner__text"> {{ col.formatter ? col.formatter(row[col.field]) : row[col.field] }} </span>
                            <span class="cell-inner__subText"> {{ col.subFormatter ? col.subFormatter(row[col.subField]) : row[col.subField] }} </span>
                        </div>
                        <!-- IS STATUS -->
                        <div v-else-if="col.status && row[col.field]" class="cell-inner cell-inner__status">
                            <span class="cell-inner__iconStatus" :style="`background-color: ${col.status[row[col.field]].color}`"></span>
                            <span class="cell-inner__text"> {{ col.status[row[col.field]].label }} </span>
                        </div>
                        <!-- DEFAULT -->
                        <div v-else class="cell-inner"> {{ col.formatter ? col.formatter(row[col.field]) : row[col.field] || ' - ' }} </div>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="data-grid__pagination">
            <ul class="pagination">
                <li v-for="(data, index) in pagination" :key="index" class="page-item" :class="{ active: data.isActive }">
                    <a class="page-link" @click="$emit('paginate', data.pageNumber)"> {{ data.text }} </a>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
const COLUMN_TYPE = {
    // TEXT: 'TEXT',
    STATUS: 'STATUS',
    ACCOUNT: 'ACCOUNT'
}
module.exports = {
    name: 'FileManageTable',
    props: {
        resources: Object,
        listData: Array,
        pagination: [Object, Array]
    },
    setup(props, { root, emit, refs }) {

        const isCheckAll = ref(false)
        const listRenderedItems = ref([])
        const listSelectedItems = computed(() => listRenderedItems.value.filter(i => i.checked))

        watch(() => props.listData, newVal => {
            listRenderedItems.value = newVal.map(i => ({
                ...i,
                releasedDate: root.formatToDateTime(i.releasedAt).slice(0, 'YYYY-MM-DD'.length),
                releasedTime: root.formatToDateTime(i.releasedAt).slice('YYYY-MM-DD'.length, 'YYYY-MM-DD HH:mm:ss'.length),
                checked: false
            }))
        })
        watch(isCheckAll, newVal => {
            listRenderedItems.value = listRenderedItems.value.map(i => ({ ...i, checked: newVal }))
        })
        watch(listSelectedItems, newVal => {
            if (listRenderedItems.value.length && newVal.length === listRenderedItems.value.length) { isCheckAll.value = true }
            else { isCheckAll.value = false }
        })

        const allColumns = [
            {
                label: 'Machine ID',
                field: 'machineCode',
                sort: true,
                enabled: true
            },
            {
                label: 'Tooling ID',
                field: 'moldId',
                sort: true,
                enabled: true
            },
            {
                label: 'Plant',
                field: 'locationName',
                sort: true,
                enabled: true
            },
            {
                label: 'Date',
                field: 'date',
                sort: true,
                enabled: true,
            },
            {
                label: 'Reported By',
                field: 'account',
                type: COLUMN_TYPE.ACCOUNT,
                sort: true,
                enabled: true,
            },
            {
                label: 'State',
                field: 'state',
                sort: true,
                enabled: true
            }
        ]

        const columnConfig = ref([...allColumns])

        const enabledColumns = computed(() => columnConfig.value.filter(c => c.enabled))

        const handleClickAction = (type) => {
            emit(type, listSelectedItems.value)
        }

        // SORT
        const sortColumn = ref('')
        const sortDesc = ref(false)
        const handleSort = field => {
            sortDesc.value = !sortDesc.value
            sortColumn.value = field
            emit('sort', { sortDesc: sortDesc.value, field: sortColumn.value })
        }

        // ACTION BAR
        const listActions = reactive([])
        const listActionsEnable = computed(() => listActions.filter(i => i.show.value))
        const showActionBar = computed(() => listActions.some(i => i.show.value))

        watchEffect(() => console.log('props.listData', props.listData))
        watchEffect(() => console.log('listSelectedItems', listSelectedItems.value))
        watchEffect(() => console.log('showActionBar', showActionBar.value))

        return {
            isCheckAll,
            listRenderedItems,
            sortColumn,
            sortDesc,
            listActionsEnable,
            showActionBar,
            handleSort,
            handleClickAction,
            enabledColumns
        }
    }
}
</script>

<style scoped>
/* DATA TABLE ACTIONS */
.action-item {
    display: inline-flex;
    align-items: center;
    cursor: pointer;
    margin-right: 23px;
    color: var(--grey-dark);
}

.action-bar .action-item .action-item__label {
    font-size: 14.66px;
    line-height: 1;
}

.action-item__icon {
    display: inline-block;
    background-color: var(--grey-dark);
    mask-size: contain;
    mask-repeat: no-repeat;
    mask-position: center;
    -webkit-mask-size: contain;
    -webkit-mask-repeat: no-repeat;
    -webkit-mask-position: center;
    margin-left: 0.25rem;
}

/* DATA GRID */
.data-grid__table {
    width: 100%;
    overflow-x: auto;
    border: 1px solid rgb(200, 206, 211);
    border-radius: 3px;
    margin-bottom: 0px;
}

.data-grid__head {
    background: #ffffff;
    position: sticky;
    top: 6px;
    z-index: 99;
}

.data-grid__body {}

.data-grid__row {
    background: #ffffff;
}

.data-grid__head .data-grid__row {
    height: 57px;
}

.data-grid__cell {}

.data-grid__head .data-grid__cell,
.data-grid__body .data-grid__cell {
    padding: 7px 14px 7px 7px;
}

.data-grid__head .data-grid__cell {
    border-bottom: 2px solid #c8ced3;
}

.data-grid__head .cell-inner,
.data-grid__body .cell-inner {
    font-size: 14.66px;
}

.data-grid__body tr:nth-of-type(odd) {
    background-color: rgba(0, 0, 0, 0.05);
}

.cell-checkbox {
    width: 27px;
    padding: 0.5em 0;
    vertical-align: middle;
}

.data-grid__head .cell-inner {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.cell-inner__sort {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 4px;
    border-radius: 50%;
    overflow: hidden;
    width: 15px;
    height: 15px;
}

.cell-active .cell-inner__sort {
    background-color: #f2f2f2;
}

.cell-inner__sort.desc {
    transform: rotate(180deg);
}

.cell-inner__sort-icon {
    display: inline-block;
    width: 100%;
    height: 100%;
    background-color: var(--grey-8);
    mask-image: url("/images/icon/sort.svg");
    mask-size: contain;
    mask-position: center;
    mask-repeat: no-repeat;
    -webkit-mask-image: url("/images/icon/sort.svg");
    -webkit-mask-size: contain;
    -webkit-mask-position: center;
    -webkit-mask-repeat: no-repeat;
}

.cell-inner__iconStatus {
    display: inline-block;
    width: 15px;
    height: 15px;
    border-radius: 25px;
    margin-right: 6px;
}

.cell-inner__text {
    font-size: 14.66px;
    color: var(--grey-dark);
}

.cell-inner__subText {
    display: block;
    font-size: 12px;
}

.cell-inner__status {
    display: flex;
    align-items: center;
}

/* DATA PAGINATION */
.data-grid__pagination {
    margin-top: 1rem;
}

.data-grid__pagination .page-item.active .page-link {
    background-color: var(--blue);
    border-color: var(--blue);
}

.data-grid__pagination .page-item.active .page-link:hover {
    color: var(--white);
}

.data-grid__pagination .page-item .page-link:hover {
    background-color: var(--blue-light);
}
</style>