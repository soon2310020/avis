<template>
    <base-dialog :title="getModalTitle" :visible="isShow" dialog-classes="modal-lg custom-fade-modal" body-classes="custom-modal__body" @close="handleClose">
        <div class="modal__container">
            <div class="modal__container__header">
                <div style="display: flex;margin-bottom: 20px;justify-content: space-between;">
                    <ul class="type-bar">
                        <li>
                            <a href="javascript:void(0)" class="nav-button" :class="{ 'nav-button-selected': type == 'ASSIGNED' }" @click="handleChangeType('ASSIGNED')">{{ resources['assigned_to_me'] }}</a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" class="nav-button" :class="{ 'nav-button-selected': type == 'CREATED', }" @click="handleChangeType('CREATED')">{{ resources['created_by_me'] }}</a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="modal__container__form">
                <my-list-work-orders :resources="resources" :data-list="getDataTable.dataList" :field-list="getDataTable.fieldList" :current-page="getDataTable.page" :total-page="getDataTable.totalPage" :is-loading="isLoading" @view-click="handleViewClick" @change-page="handleChangePage">
                </my-list-work-orders>
            </div>
            <div style="height: 43px"></div>
        </div>
    </base-dialog>
</template>

<script>
const PAGE_SIZE = 10;
module.exports = {
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        isShow: {
            type: Boolean,
            default: () => false,
        },
        showDetail: Function,
        myOrderKey: {
            type: [Number, String],
        },
    },
    components: {
        'my-list-work-orders': httpVueLoader('/components/work-order/owner/my-work-order-dialog/my-list-work-orders.vue')
    },
    data() {
        return {
            type: "ASSIGNED",
            assignedList: [],
            createdList: [],
            assignedPage: 1,
            assignedTotalPage: 1,
            createdPage: 1,
            createdTotalPage: 1,
            isLoading: false,
            tableKey: 0,
            page: 1,
        };
    },
    mounted() {
        this.assignedFields = [
            { title: this.resources['work_order_id'], field: "workOrderId" },
            { title: this.resources['type'], field: "orderType" },
            { title: this.resources['assigned_by'], field: "createdBy" },
            { title: this.resources['priority'], field: "priority" },
            { title: this.resources['due_date'], field: "end" },
            { title: this.resources['status'], field: "workOrderStatus" },
            { title: this.resources['action'], field: "action" },
        ];
        this.createdFields = [
            { title: this.resources['work_order_id'], field: "workOrderId" },
            { title: this.resources['type'], field: "orderType" },
            { title: this.resources['requested_to'], field: "workOrderUsers" },
            { title: this.resources['priority'], field: "priority" },
            { title: this.resources['due_date'], field: "end" },
            { title: this.resources['status'], field: "workOrderStatus" },
            { title: this.resources['action'], field: "action" },
        ];
    },
    computed: {
        getModalTitle() {
            return this.resources['my_work_orders'];
        },
        getDataTable() {
            let result = {};
            if (this.type === "ASSIGNED") {
                result.fieldList = this.assignedFields;
                result.dataList = this.assignedList;
                result.page = this.assignedPage;
                result.totalPage = this.assignedTotalPage;
            } else if (this.type === "CREATED") {
                result.fieldList = this.createdFields;
                result.dataList = this.createdList;
                result.page = this.createdPage;
                result.totalPage = this.createdTotalPage;
            }
            this.tableKey++;
            // console.log(result);
            return result;
        },
    },
    watch: {
        isShow(newVal) {
            console.log("isShow", newVal);
            if (newVal) {
                this.initData();
            }
        },
        myOrderKey() {
            this.initData();
        },
    },
    methods: {
        async handleChangePage(sign) {
            if (sign === "+") {
                if (this.type === "ASSIGNED") {
                    this.assignedPage++
                    await this.fetchAssignedOrders(this.assignedPage);
                } else if (this.type === "CREATED") {
                    this.createdPage++
                    await this.fetchCreatedOrders(this.createdPage);
                }
            } else {
                if (this.type === "ASSIGNED") {
                    this.assignedPage--
                    await this.fetchAssignedOrders(this.assignedPage);
                } else if (this.type === "CREATED") {
                    this.createdPage--
                    await this.fetchCreatedOrders(this.createdPage);
                }
            }
        },
        handleClose() {
            this.$emit("close");
        },
        clearData() { },
        handleChangeType(type) {
            this.type = type;
        },
        async fetchAssignedOrders(page) {
            try {
                this.isLoading = true;
                const selectPage = page ? page : 1;
                const res = await axios.get(`/api/work-order?assignedToMe=true&size=${PAGE_SIZE}&page=${selectPage}`);
                console.log("fetchAssignedOrders", res);
                this.assignedList = res.data.workOrderList.content;
                this.assignedPage = +res.data.workOrderList.number + 1;
                this.assignedTotalPage = res.data.workOrderList.totalPages;
                this.page = selectPage;
            } catch (error) {
                console.log(error);
            } finally {
                this.isLoading = false;
            }
        },
        async fetchCreatedOrders(page) {
            try {
                const selectPage = page ? page : 1;
                const res = await axios.get(
                    `/api/work-order?createdByMe=true&size=${PAGE_SIZE}&page=${selectPage}`
                );
                console.log("fetchCreatedOrders", res);
                this.createdList = res.data.workOrderList.content;
                this.createdPage = +res.data.workOrderList.number + 1;
                this.createdTotalPage = res.data.workOrderList.totalPages;
            } catch (error) {
                console.log(error);
            }
        },
        async initData() {
            this.isLoading = true;
            await this.fetchAssignedOrders();
            await this.fetchCreatedOrders();
            this.isLoading = false;
        },
        handleViewClick(workOrder) {
            console.log("workOrder", workOrder, this.type);
            this.showDetail(workOrder, this.type);
        },
    },
};
</script>

<style scoped>
.modal__footer {
    margin-top: 100px;
    justify-content: flex-end;
}

.modal__footer__button {
    margin-left: 8px;
}

.custom-modal__body {
    padding: 20px 25px !important;
}

.modal__container__form__row {
    margin: 10px 0;
    display: flex;
}

.modal__container__form__row label {
    width: 110px;
}

.back-icon {
    display: inline-flex;
    align-items: center;
}

.back-icon__text {
    margin-left: 8px;
    cursor: pointer;
}

.custom-fade-modal {
    z-index: 5000;
}

.form-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
}

.back-icon {
    display: inline-flex;
    align-items: center;
    cursor: pointer;
}

.back-icon__text {
    margin-left: 8px;
    cursor: pointer;
}
</style> 