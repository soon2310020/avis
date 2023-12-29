<template>
    <div class="step-content">
        <div class="form-item">
            <label>{{ resources['request_type'] }}:</label>
            <base-input width="200px" :readonly="true" :value="getRequestType">
            </base-input>
        </div>
        <div class="form-item">
            <label>{{ resources['request_id'] }}:</label>
            <base-input width="200px" :readonly="true" :value="request.requestId">
            </base-input>
        </div>
        <div class="form-item justify-start align-top">
            <label>{{ resources['data_requested'] }}:</label>
            <div style="position: relative;">
                <base-button type="dropdown" @click="toggleDataTypesDropdown">
                    {{ resources['add_data_type'] }}
                </base-button>
                <common-popover @close="closeDataTypesDropdown" :is-visible="visibleDataType"
                    :style="{ width: '210px', marginTop: '4px' }">
                    <select-custom class="custom-dropdown--assign" :style="{ position: 'static', width: '100%' }"
                        :items="types" :multiple="true" :has-toggled-all="true" :searchbox="true" @close="closeDataTypesDropdown"
                        @on-select="handleSelectRequestType" @select-all="handleSelectAll">
                    </select-custom>
                </common-popover>
                <div class="d-flex">
                    <base-chip v-for="item in getSelectedTypes" :key="item.key" :selection="item" :allow-tooltips="false"
                        :color="typeColors[item.key]" :close-able="false">
                    </base-chip>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
const TYPE_COLORS = {
    'COMPANY': 'blond',
    'PLANT': 'wheat',
    'PART': 'pale-blue',
    'TOOLING': 'lavender',
    'MACHINE': 'melon',
}
module.exports = {
    components: {
        "select-custom": httpVueLoader("/components/@base/dropdown/common-select.vue"),
    },
    props: {
        request: {
            type: Object,
            default: () => ({})
        },
        resources: {
            type: Object,
            default: () => ({})
        },
        types: {
            type: Array,
            default: () => ([])
        },
    },
    setup(props, ctx) { 
        console.log('test')
        // STATE //
        const typeColors = ref({ ...TYPE_COLORS })
        const visibleDataType = ref(false)
        // COMPUTED //
        const getRequestType = computed(() => {
            return Common.formatter.toCase(props.request.requestType, 'capitalize')
        })

        const getSelectedTypes = computed(() => {
            console.log('props.types', props.types)
            return props.types.filter(item => item.checked)
        })

        // METHOD //
        const toggleDataTypesDropdown = () => {
            visibleDataType.value = !visibleDataType.value
        }

        const closeDataTypesDropdown = () => {
            visibleDataType.value = false
        }

        const handleSelectRequestType = (item) => {
            ctx.emit('select-type', item.key)
        }

        const handleSelectAll = (isSelectAll) => {
            ctx.emit('select-all', isSelectAll)
        }

        return {
            typeColors,
            visibleDataType,
            getRequestType,
            getSelectedTypes,
            toggleDataTypesDropdown,
            closeDataTypesDropdown,
            handleSelectRequestType,
            handleSelectAll,
        }
    }
}
</script>

<style scoped>
.pretty.p-default input:checked~.state label:after {
    background-color: #0075FF !important;
}

.pretty.p-default input:checked~.state label::before {
    border-color: #0075FF !important;
}

.input-group-request input {
    box-sizing: border-box;
    margin: 0;
    outline: none;
}

.input-group-request input[type="button"] {
    -webkit-appearance: button;
    cursor: pointer;
}

.input-group-request input::-webkit-outer-spin-button,
.input-group-request input::-webkit-inner-spin-button {
    -webkit-appearance: none;
}

.input-group-request {
    width: 66px;
    clear: both;
    position: relative;
    display: inline-block;
}

.input-group-request input[type='button'] {
    background-color: #eeeeee;
    min-width: 38px;
    width: auto;
    transition: all 300ms ease;
}

.input-group-request .button-minus,
.input-group-request .button-plus {
    font-weight: bold;
    height: 25px;
    padding: 0;
    width: 38px;
    position: relative;
}

.input-group-request .quantity-field {
    position: absolute;
    width: 17px;
    height: 25px;
    left: 0;
    top: 0;
    text-align: center;
    font-weight: 400;
    border-radius: 2px;
    font-size: 17px;
    line-height: 100%;
    resize: vertical;
    background: #909090;
    border: 1px solid #909090;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
}

.input-group-request .quantity-field.active {
    background: #52A1FF !important;
    border: 1px solid #52A1FF;
    cursor: pointer;
}

.input-group-request .quantity-field.active:hover {
    background: #3994FF !important;
    border: 1px solid #3994FF !important;
}

.input-group-request .button-plus {
    right: 0;
    left: unset;
}

.input-group-request input[type="number"] {
    -moz-appearance: textfield;
    -webkit-appearance: none;
}

.custom-dropdown--assign li label {
    width: unset !important;
}
</style>