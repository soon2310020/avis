<template>
    <base-dialog
        :visible="visible"
        :title="title"
        dialog-classes="dialog-xl"
        @close="$emit('close')"
    >
        <div class="version-manage-dialog">
            <div class="form">
                <div class="form-item">
                    <label>File Group Code</label>
                    <base-input
                        class="custom-input"
                        v-model="form.fileGroupCode"
                    ></base-input>
                </div>
                <div class="form-item">
                    <label>File Group Name</label>
                    <base-input
                        class="custom-input"
                        v-model="form.fileGroupName"
                    ></base-input>
                </div>
            </div>

            <div class="data-grid">
                <version-manage-table
                    :resources="resources"
                    :form-data="form"
                    :list-data="form.listVersions"
                    :refetch="refetch"
                ></version-manage-table>
            </div>

            <div class="dialog-actions">
                <base-button
                    type="cancel"
                    @click="handleCancel"
                >Cancel</base-button>
            </div>
        </div>
    </base-dialog>
</template>

<script>
module.exports = {
    name: 'VersionManageDialog',
    components: {
        'version-manage-table': httpVueLoader('./version-manage-table.vue')
    },
    props: {
        resources: Object,
        visible: Boolean,
        title: String,
        data: Object,
        refetch: Function
    },
    setup(props, { emit }) {
        const form = ref({
            id: '',
            enabled: false,
            fileGroupCode: '',
            fileGroupName: '',
            fileGroupStatus: '',
            fileGroupType: '',
            listVersions: [],
            releasedAt: '',
            releasedVersion: '',
            version: ''
        })

        const handleCancel = () => {
            emit('close')
        }

        watch(() => props.data, newVal => {
            if (newVal) {
                form.value = Object.assign({}, form.value, newVal)
                console.log('props.data', newVal)
            }
        })

        return {
            form,
            handleCancel
        }
    }
}
</script>

<style scoped>
.form {
    display: flex;
    margin-bottom: 40px;
}
.form-item {
    flex: 1;
}
.form-item label {
    font-weight: bold;
    font-size: 14.66px;
    width: 130px;
    color: var(--grey-dark);
}
.form-item .custom-input {
    border-color: var(--white);
}

.search-bar {
    margin-bottom: 12px;
}

.dialog-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 118px;
    margin-bottom: 20px;
}
</style>