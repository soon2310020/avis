<template>
    <base-dialog
        :visible="visible"
        dialog-classes="modal-lg"
        @close="$emit('close')"
    >
        <template #title>
            <div class="custom-title">
                <div class="custom-title__label">{{dialog.title}}</div>
                <div class="custom-title__tabs">
                    <div class="custom-tab">
                        <div
                            class="custom-tab-item"
                            :class="{active: dialog.activeTab === TABS.DETAILS.KEY}"
                            @click="handleChangeTab(TABS.DETAILS.KEY)"
                        ><span v-text="resources['details']"></span></div>
                        <div
                            class="custom-tab-item"
                            :class="{active: dialog.activeTab === TABS.GRAPH.KEY}"
                            @click="handleChangeTab(TABS.GRAPH.KEY)"
                        ><span v-text="resources['graph']"></span></div>
                    </div>
                </div>
            </div>
        </template>
        <div class="custom-container">
            <section class="data-summary">
                <div class="data-row">
                    <div class="data-item data-item-date">
                        <label><span v-text="resources['date']"></span>:</label>
                        <span>{{details.date}}</span>
                    </div>
                    <div class="data-item data-item-work-day">
                        <label><span v-text="resources['work_day']"></span>: </label>
                        <div class="d-flex align-items-center">
                            <span>{{details.workDay}}</span>
                            <sub
                                v-if="isOver1Day"
                                class="ml-1"
                            >+1</sub>
                        </div>
                    </div>
                    <div class="data-item data-item-machine">
                        <label> <span v-text="resources['machine']"></span>: </label>
                        <span>{{details.machine}}</span>
                    </div>
                    <div class="data-item data-item-shifts">
                        <label> <span v-text="resources['shifts']"></span>:</label>
                        <span>{{details.shifts}} {{details.shifts > 1 ? 'Shifts' : 'Shift'}}</span>
                    </div>
                </div>
            </section>
            <section class="data-grid">
                <div v-show="dialog.activeTab === TABS.DETAILS.KEY">
                    <entry-record-details
                        :resources="resources"
                        :list-data="listMachineRejectRate"
                        :is-show="true"
                        :pagination="machineRejectRatePagination"
                        @paginate="(page) => fetchListMachineRejectRate({ page: page })"
                        @show-machine-details="(machineId) => showMachineDetails(machineId)"
                    ></entry-record-details>
                </div>
                <!-- TODO RECHECK -->
                <div v-show="dialog.activeTab === TABS.GRAPH.KEY">
                    <entry-record-graph
                        :is-show="dialog.activeTab === TABS.GRAPH.KEY"
                        :filter="query"
                    ></entry-record-graph>
                </div>
            </section>
        </div>
    </base-dialog>
</template>

<script>
const TABS = {
    DETAILS: {
        KEY: 'DETAILS',
        TITLE: 'Reject Part Entry Record'
    },
    GRAPH: {
        KEY: 'GRAPH',
        TITLE: 'Reject Part Summary'
    }
}

const initialState = {
    date: '',
    workDay: '',
    machine: '',
    shifts: ''
}

module.exports = {
    name: 'EntryRecordDialog',
    components: {
        EntryRecordDetails: httpVueLoader('/components/oee-center/machine-reject-rate/entry-record-details.vue'),
        EntryRecordGraph: httpVueLoader('/components/oee-center/machine-reject-rate/entry-record-graph.vue')
    },
    props: {
        resources: Object,
        visible: Boolean,
        machine: Object,
        machineGraph: Object,
        query: {
            type: Object,
            default: false
        }
    },
    data() {
        return {
            TABS,
            filterDashboard: {
                listLocations: [],
                listMachines: [],
                currentDate: {
                    from: new Date(),
                    to: new Date(),
                    fromTitle: moment().format("YYYY-MM-DD"),
                    toTitle: moment().format("YYYY-MM-DD"),
                },
                frequency: 'DAILY'
            },
            machineRejectRatePagination: {
                pageNumber: 1,
                totalPages: 1
            },
            listMachineRejectRate: [],
            details: {},
            isOver1Day: false,
            dialog: {
                title: this.resources['reject_part_entry_record'],
                activeTab: TABS.DETAILS.KEY
            }
        }
    },
    watch: {
        visible(newVal) {
            if (newVal) {
                this.fetchListMachineRejectRate({ page: 1 })
            } else {
                this.dialog.activeTab = TABS.DETAILS.KEY
            }
        }
    },
    methods: {
        async showMachineDetails(machineId) {
        },
        formatToStringDate(strVal) {
            // return strVal.slice(0,4) + '/' + strVal.slice(4,6) + '/' + strVal.slice(6,8)
            return moment(strVal, 'YYYYMMDD').format('YYYY/MM/DD')
        },
        formatAMPM(hour, minute) {
            let hours = hour;
            let minutes = minute;
            const ampm = hours >= 12 ? 'PM' : 'AM';
            hours %= 12;
            hours = hours < 10 ? `0${hours}` : hours || 12;
            minutes = minutes < 10 ? `${minutes}` : minutes;
            return `${hours}:${minutes} ${ampm}`;
        },
        async fetchListMachineRejectRate(params) {
            const queryParams = {
                page: params.page,
                size: 10,
                ...this.query
            }
            try {
                const response = await axios.get('/api/rejected-part/get-reject-part-entry-record?' + Common.param(queryParams))
                console.log(response, 'responseresponseresponse')
                this.listMachineRejectRate = response.data.data.rejectPartEntryRecordPage.content
                this.details.value = response.data.data.rejectPartEntryRecordPage.content                
                this.details.date = this.formatToStringDate(response.data.data.date)
                this.details.machine = response.data.data.machineCode
                this.details.shifts = response.data.data.numberOfShift
                const startDayConverted = this.formatAMPM(response.data.data.start.slice(0, 2), response.data.data.start.slice(2, 4))
                const endDayConverted = this.formatAMPM(response.data.data.end.slice(0, 2), response.data.data.end.slice(2, 4))
                this.isOver1Day = startDayConverted === endDayConverted
                this.details.workDay = startDayConverted + ' to ' + endDayConverted
                this.machineRejectRatePagination.totalPages = response.data.data.rejectPartEntryRecordPage.totalPages
                this.machineRejectRatePagination.pageNumber = params.page
            } catch (error) {
                console.log(error)
            }
        },
        handleChangeTab(tabKey) {
            this.dialog.activeTab = tabKey
        }
    }
}
</script>

<style scoped>
.custom-title {
    width: 100%;
    display: flex;
    align-items: center;
}
.custom-title__label {
    font-weight: 700;
    font-size: 14.66px;
    line-height: 18px;
    color: var(--grey-dark);
}

.custom-title__tabs {
    margin-left: 40px;
}

.custom-tab {
    display: flex;
    gap: 10px;
}

.custom-tab-item {
    text-align: center;
    border-radius: 3px;
    display: inline-flex;
    width: 79px;
    height: 26px;
    justify-content: center;
    align-items: center;
    background: #ffffff;
    border: 0.5px solid #d0d0d0;
    cursor: pointer;
}

.custom-tab-item.active {
    color: #ffffff;
    background: var(--blue);
}
.data-row {
    display: flex;
    flex-wrap: wrap;
}
.data-item {
    width: 50%;
    font-size: 14.66px;
    display: flex;
    flex-shrink: 0;
    align-items: center;
    margin-bottom: 11px;
}
.data-item label {
    min-width: 90px;
    font-weight: 700;
    color: var(--grey-dark);
    line-height: 17px;
    margin-bottom: 0px;
}
.data-item span {
    flex: 1;
}
</style>