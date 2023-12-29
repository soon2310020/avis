<template>
    <div class="date-filter">
        <svg width="20" height="20" viewBox="0 0 24 24">
            <path d="M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z"/>
        </svg>
        <a-date-picker format="YYYY-MM-DD" @focus="focusCalendar" v-model="selectedDate"></a-date-picker>
    </div>
</template>

<script>
    module.exports = {
        name: "date-filter",
        props: {
        },
        data() {
            return {
                selectedDate: null
            }
        },
        mounted() {
            this.selectedDate = moment();
        },
        computed: {
        },
        watch: {
            selectedDate(value){
                if (value) {
                    this.$emit('change-date', moment(value).format("YYYYMMDD"), value);
                }
            }
        },
        methods: {
            focusCalendar() {
                $('.tool-wrapper').css('box-shadow', '0 0 0 2px rgba(24,144,255,.2)');
                setTimeout(() => {
                    $('.tool-wrapper').css('box-shadow', 'none');
                }, 100);
            },
            disabledDate(current) {
                return current && current > moment().endOf('day');
            }
        }
    }
</script>

<style scoped>
    .ant-input:focus {
        outline: 0;
        /*box-shadow: 0 0 0 2px rgba(24,144,255,.2);*/
        box-shadow: none;
    }
</style>