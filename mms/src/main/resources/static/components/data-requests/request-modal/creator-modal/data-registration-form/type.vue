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
        <div class="form-item justify-start align-top" style="margin-top: 36px;">
            <label style="margin-top: 8px;">{{ resources['data_requested'] }}:</label>
            <div>
                <div v-for="item in types" :key="item.key" class="d-flex" style="margin-bottom: 22px;">
                    <div class="choosing-item" style="margin-right: 60px;">
                        <p-check :checked="item.checked"
                            @change="handleSelectDataType($event, item.key)" :key="item.key">
                            <span style="font-size: 14.66px; line-height: 25px;">
                                {{ resources[item.labelKey] }}
                            </span>
                        </p-check>
                    </div>
                    <div class="input-group-request" :key="counterKey">
                        <base-manual-counter :value="item.value" :disabled="!item.checked"
                            :validation="validateDataRequestNumber"
                            @counter-change="(value) => handleChangeDataRequested(item.key, value)"></base-manual-counter>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
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
        counterKey: {
            type: [String, Number]
        }
    },
    components: {
        'base-manual-counter': httpVueLoader('/components/@base/manual-counter/base-manual-counter.vue')
    },
    setup(props, ctx) {
        // STATE //
        // COMPUTED //
        const getRequestType = computed(() => {
            return Common.formatter.toCase(props.request.requestType, 'capitalize')
        })

        // METHOD //
        const handleSelectDataType = (checked, key) => {
            ctx.emit('check', checked, key)
        }

        const handleChangeDataRequested = (key, value) => {
            ctx.emit('change-data-requests', key, value)
        }

        const validateDataRequestNumber = (value) => {
            return value >= 0
        }

        return {
            getRequestType,
            handleSelectDataType,
            handleChangeDataRequested,
            validateDataRequestNumber
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
.pretty .state label:after, .pretty .state label:before {
    top: calc((0% - (100% - 1em)) + 55%);
}
</style>