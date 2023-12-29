<template>
    <div class="op-status">
        <span class="" style="margin-right: 70px;">Status</span>
        <div class="parent" v-for="(item, index) in configuration" :key="index">
            <span class="op-active op-active-sm label label-success"
                  :class="item.color"></span>
            <span>{{item.name}}</span>
            <div class="popup" v-text="setConfiguration(item)"></div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        name: "OpStatus",
        data(){
            return {
                enabled: true,
                configCategory: 'OP',
                configuration: [
                    {
                        name: 'Active',
                        operatingStatus: 'WORKING',
                        time: 72,
                        timeUnit: 'HOURS',
                        color: 'label-success'
                    },
                    {
                        name: 'Idle',
                        operatingStatus: 'IDLE',
                        time: 10,
                        timeUnit: 'DAYS',
                        color: 'label-warning'
                    },
                    {
                        name: 'Inactive',
                        operatingStatus: 'NOT_WORKING',
                        time: 11,
                        timeUnit:'DAYS',
                        color: 'label-inactive'
                    },
                    {
                        name: 'Disconnected',
                        operatingStatus: 'DISCONNECTED',
                        time: 48,
                        timeUnit:'HOURS',
                        color: 'label-danger'
                    }
                ],
            }
        },
        methods: {
            getCurrentOpConfig(){
                Common.getCurrentOpConfig('MOLD').then(data => {
                    if (data) {
                        data.forEach((item, index) => {
                            this.fields[item.operatingStatus].id = item.id;
                            if (item.time) {
                                this.fields[item.operatingStatus].time = item.time;
                                this.fields[item.operatingStatus].timeUnit = item.timeUnit;
                            }
                        });
                    }
                });
            },
            setConfiguration (value) {
                if (value.operatingStatus === 'WORKING' || value.operatingStatus === 'IDLE' || value.operatingStatus === 'NOT_WORKING') {
                    if(value.timeUnit==='DAYS') {
                        return 'In use with the last ' + value.time + ' day(s)'
                    } else {
                        return 'In use within  the last ' + value.time + ' hour(s)'
                    }
                } else {
                    if(value.timeUnit==='DAYS') {
                        return 'No signal from the counter within the last ' + value.time  + ' day(s)'
                    } else {
                        return ' No signal from the counter within the last ' + value.time  + ' hour(s)'
                    }
                }
            },
            getConfiguration() {
                Common.getCurrentOpConfig('MOLD').then(data => {
                    if (data) {
                        this.configuration.forEach((value, index) => {
                            data.forEach(item => {
                                if(value.operatingStatus === item.operatingStatus) {
                                    if (item.time)
                                        this.configuration[index].time = item.time;
                                    if (item.timeUnit)
                                        this.configuration[index].timeUnit = item.timeUnit
                                }
                            })
                        });
                    }
                });
            },
            checkingEnableOpConfig(){
                Common.getCategoryConfigStatus().then(data => {
                    if (data && data.length > 0) {
                        data.forEach(item => {
                            if (this.configCategory === item.configCategory) {
                                this.enabled = item.enabled;
                            }
                        });
                    }
                });
            }
        },
        mounted(){
            this.checkingEnableOpConfig();
            this.getConfiguration();
        },
    }
</script>

<style scoped>
    .op-status {
        display: flex;
    }
    .parent {
        margin-right: 1rem;
        position: relative;
        display: inline-block;
    }
    .popup {
        display: none;
        width: 140px;
        background-color: #fff;
        color: black;
        text-align: center;
        border-radius: 6px;
        padding: 3px 3px;
        box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
        position: absolute;
        z-index: 1;
        top: 30px;
        left: 0;
        text-transform: lowercase;
        font-size: 12px;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
    }
    .popup::first-letter {
        text-transform: uppercase;
    }

    .parent .popup:after {
        content: "";
        position: absolute;
        top: -14px;
        left: 20px;
        border-width: 7px;
        border-style: solid;
        border-color: transparent transparent #FFF;
    }

    .parent:hover .popup{
        display: block;
    }

</style>