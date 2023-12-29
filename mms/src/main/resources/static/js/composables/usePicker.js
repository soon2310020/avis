const usePicker = () => {

    // date
    const pickerOptions = ref([
        { title: 'Hourly', type: 'HOURLY', isRange: false },
        { title: 'Daily', type: 'DAILY', isRange: false },
        { title: 'Weekly', type: 'WEEKLY', isRange: false },
        { title: 'Monthly', type: 'MONTHLY', isRange: false },
        { title: 'Custom Range', type: 'CUSTOM', isRange: true },
    ])

    const currentDate = ref({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
    })

    const frequency = ref('DAILY')

    const isRange = ref(false)

    const getTitle = computed(() => {
        let dateData = { ...currentDate.value };
        if (frequency.value.includes("MONTHLY")) {
            dateData.fromTitle = moment(dateData.from).format("MMMM, YYYY");
            dateData.toTitle = moment(dateData.to).format("MMMM, YYYY");
        } else if (frequency.value.includes("WEEKLY")) {
            let yearFrom = dateData.fromTitle.split("-")[0];
            let yearTo = dateData.toTitle.split("-")[0];
            let weekFrom = dateData.fromTitle.split("-")[1];
            let weekTo = dateData.toTitle.split("-")[1];
            dateData.fromTitle = `Week ${weekFrom}, ${yearFrom}`
            dateData.toTitle = `Week ${weekTo}, ${yearTo}`
        }
        if (isRange.value) {
            return `${dateData.fromTitle} - ${dateData.toTitle}`;
        } else {
            return `${dateData.fromTitle}`;
        }
    })

    const getFromTimestampValue = computed(() => {
        const totalValue = currentDate.value.from
        totalValue.setHours(
            time.value.hour,
            time.value.minute
        );
        return Common.convertDateToTimestamp(totalValue)
    })

    const getToTimestampValue = computed(() => {
        const totalValue = currentDate.value.to
        totalValue.setHours(
            time.value.hour,
            time.value.minute
        );
        return Common.convertDateToTimestamp(totalValue)
    })

    // hour
    const hour = (new Date).getHours()
    const minute = (new Date).getMinutes()
    const time = ref({ hour: hour, minute: minute, isAddDay: false })

    const timeFormatted = computed(() => {
        let hh = time.value.hour
        let mm = time.value.minute
        if (time.value.hour < 10) hh = `0${time.value.hour}`
        if (time.value.minute < 10) mm = `0${time.value.minute}`
        return `${hh}:${mm}`
    })

    return {
        // date
        pickerOptions,
        currentDate,
        frequency,
        getTitle,
        isRange,
        // hour
        time,
        timeFormatted,
        //timestamp
        getFromTimestampValue,
        getToTimestampValue,
    }
}