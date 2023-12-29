<template>
    <div class="oee-center-filter">
        <common-select v-if="Object.entries(resources).length" :placeholder="resources['all_locations']" label="Plants" :has-searchbox="listLocations.length >= 5" :list-options="listLocations" :value="filter.listLocations" :get-label="getLabelLocation"
            @change="(event) => handleChange('location', event)" :style-props="{ width: 'fit-content' }" :resource="resources"></common-select>
        <date-picker-button :date="currentDate" :prefix="'/images/icon/oee/calendar-icon.svg'" :hover-prefix="'/images/icon/oee/calendar-icon.svg'" :prefix-position="'right'" :is-range="isRange" :custom-style="{ height: '31px' }"
            :default-class="'btn-custom btn-custom-primary animationPrimary primary-button'" :active-class="'outline-animation'" :frequency="frequency" :parent="'modal'" @click="handleOpenPickerModal">
        </date-picker-button>
    </div>
    <!-- Date picker modal -->
    <date-picker-modal ref="datePickerModalRef" :resources="resources" :select-option="timeOptions" @change-date="handleChangeDate">
    </date-picker-modal>
</template>

<script>
const defaultOptions = [
    { title: 'Daily', type: 'DAILY', isRange: false },
]

const FIELD_SET = {
    'location': 'listLocations',
}
module.exports = {
    name: 'OeeMachineToolingMatchFilter',
    props: {
        resources: Object,
        listLocations: Array,
    },
    setup(props, ctx) {
        const filter = reactive({
            listLocations: [],
            currentDate: computed(() => currentDate.value),
            frequency: computed(() => frequency.value),
        })

        const datePickerModalRef = ref(null)
        const timeOptions = ref([...defaultOptions])
        const frequency = ref('DAILY')
        const currentDate = ref({
            from: new Date(),
            to: new Date(),
            fromTitle: moment().format("YYYY-MM-DD"),
            toTitle: moment().format("YYYY-MM-DD"),
        })

        // HANDLERS
        const handleChange = (type, event) => {
            const checked = event.target.checked
            const value = JSON.parse(event.target.value)
            if (type === 'location') {
                filter[FIELD_SET[type]] = [value]
                return handleClose()
            }

            if (checked) filter[FIELD_SET[type]] = [...filter[FIELD_SET[type]], value]
            else filter[FIELD_SET[type]] = filter[FIELD_SET[type]].filter(item => item.id !== value.id)
        }

        const handleChangeDate = (timeValue, frequency) => {
            currentDate.value = timeValue
            frequency.value = frequency
            datePickerModalRef.value.closeDatePicker()
            ctx.emit('filter', { ...filter })
        }

        const handleClose = () => {
            ctx.emit('filter', { ...filter })
        }


        // GETTERS
        const getLabelLocation = () => {
            const hasNoData = filter.listLocations.length === 0 || !filter.listLocations
            const hasSelectedAll = filter.listLocations.length === props.listLocations.length
            if (hasNoData || hasSelectedAll) return 'No plant'
            else if (filter.listLocations.length === 1) return filter.listLocations[0].name
            else return filter.listLocations.length + ' ' + props.resources['plants']
        }

        const isRange = computed(() => {
            const options = timeOptions.value.filter(item => item.type === frequency.value)
            return options.length > 0 ? options[0].isRange : false
        })

        const getDateTitle = computed(() => {
            const displayDate = currentDate.value.to
            return moment(displayDate).format('YYYY-MM-DD')
        })

        const handleOpenPickerModal = () => {
            datePickerModalRef.value.showDatePicker(frequency.value, currentDate.value)
        }

        //SETTER
        const setCurrentDate = (newDate) => {
            currentDate.value = newDate
            console.log('setCurrentDate', currentDate, newDate)
        }

        watch(() => props.listLocations, newVal => {
            filter.listLocations = [...newVal.filter(item => item.checked)]
        })

        const showCheckAllBoxLocation = computed(() => props.listLocations.length > 4)

        return {
            datePickerModalRef,
            filter,
            isRange,
            timeOptions,
            frequency,
            currentDate,
            showCheckAllBoxLocation,
            // getter
            getLabelLocation,
            getDateTitle,
            //setter
            setCurrentDate,
            // handler
            handleChange,
            handleChangeDate,
            handleOpenPickerModal,
            handleClose,
        }
    }
}
</script>

<style scoped>
.oee-center-filter {
  max-width: 600px;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>