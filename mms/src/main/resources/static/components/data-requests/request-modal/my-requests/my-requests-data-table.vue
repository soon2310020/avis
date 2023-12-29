<template>
    <div>
        <div v-show="isLoading" class="work-order-data-table loading-wave"></div>
        <div v-show="!isLoading" class="work-order-data-table">
            <table v-if="dataList.length > 0" class="table table-striped">
                <thead class="custom-header-table">
                    <tr>
                        <th v-for="(field, index) in fieldList" :key="index" class="text-left">
                            <span>{{ field.title }}</span>
                        </th>
                    </tr>
                </thead>
                <tbody class="op-list">
                    <tr v-for="data in dataList" :key="data.id">
                        <td v-for="(field, index) in fieldList" :key="index" class="text-left" style="height: 40px;">
                            <template v-if="'requestId' === field.field">
                                <span>{{ data[field.field] }}</span>
                            </template>
                            <template v-else-if="'requestDataType' === field.field">
                                <span>{{ getFormatType(data[field.field]) }}</span>
                            </template>
                            <template v-else-if="'dueDate' === field.field">
                                <span>{{ formatDate(data.dueDate) }}</span>
                            </template>
                            <template v-else-if="'progress' === field.field">
                                <div>{{ `${0}% completed` }}</div>
                            </template>
                            <template v-else-if="'status'=== field.field">
                                <base-priority-card :color="color[data[field.field]]">
                                    {{ formatText(data[field.field])}}
                                </base-priority-card>
                            </template>
                            <template v-else-if="'dataRequestUserList' === field.field">
                                <template v-if="data[field.field]">
                                    <base-avatar v-for="(item, index) in data[field.field]" :key="index" :item="item.user"
                                    style="margin-right: 8px;">
                                </base-avatar>
                                </template>
                            </template>
                            <template v-else-if="'createdByUser' === field.field">
                                <template v-if="data[field.field]">
                                    <base-avatar :item="data[field.field]"> </base-avatar>
                                </template>
                            </template>
                            <template v-else-if="'action' === field.field">
                                <span class="custom-hyper-link" @click="handleViewClick(data)">
                                    {{ resources['view_request'] }}
                                    <div class="hyper-link-icon"></div>
                                </span>
                            </template>
                        </td>
                    </tr>
                </tbody>
            </table>
            <!-- <div v-if="dataList.length == 0" class="no-results-table">{{ resources['no_results'] }}</div> -->
        </div>
    </div>
</template>
  
<script>
module.exports = {
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        dataList: {
            type: Array,
            default: () => [],
        },
        fieldList: {
            type: Array,
            default: () => [],
        },
        isLoading: Boolean,
    },
    data() {
        return {};
    },
    created() {
        this.color = {
            HIGH: "red",
            MEDIUM: "yellow",
            LOW: "green",
            Upcoming: "yellow",
            UPCOMING: "yellow",
            IN_PROGRESS: "yellow",
            in_progress: "yellow",
            Requested: "purple",
            REQUESTED: "purple",
            OVERDUE: "red",
            Overdue: "red",
            Declined: "grey",
            DECLINED: "grey",
            Cancelled: "dark-grey",
            CANCELLED: "dark-grey",
        };
    },
    methods: {
        formatDate(timeStamp) {
            const date = Common.convertTimestampToDate(timeStamp)
            return moment(date).format("YYYY-MM-DD");
        },
        handleViewClick(workOrder) {
            this.$emit("view-click", workOrder);
        },
        formatText(text) {
            return Common.formatter.toCase(text, 'capitalize')
        },
        getFormatType(type) {
            if (type === 'DATA_REGISTRATION') {
                return 'D.R.'
            } else if (type === 'DATA_COMPLETION') {
                return 'D.C.'
            }
        }
    },
};
</script>
  
<style scoped>
.work-order-data-table {
    border: 1px solid #d6dade;
    border-radius: 4px;
    height: 459px;
    overflow-y: auto;
}

.table-footer {
    margin-top: 10px;
    height: 25px;
    display: flex;
}
</style>