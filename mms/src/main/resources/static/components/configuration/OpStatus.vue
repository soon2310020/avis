<template>
    <div class="op-status">
        <span class="" v-text="resources['status']" style="margin-right: 21px;"></span>
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
        props: {
            resources: Object,
        },
        data(){
            return {
                enabled: true,
                configCategory: 'OP',
                configuration: [
                    {
                        name:  this.resources['active'],
                        operatingStatus: 'WORKING',
                        time: 3,
                        timeUnit: 'DAYS',
                        color: 'label-success'
                    },
                    {
                        name:  this.resources['idle'],
                        operatingStatus: 'IDLE',
                        time: 10,
                        timeUnit: 'DAYS',
                        color: 'label-warning'
                    },
                    {
                        name:  this.resources['inactive'],
                        operatingStatus: 'NOT_WORKING',
                        time: 10,
                        timeUnit:'DAYS',
                        color: 'label-inactive'
                    },
                    {
                        name:  this.resources['disconnected'],
                        operatingStatus: 'DISCONNECTED',
                        time: 6,
                        timeUnit:'HOURS',
                        color: 'label-danger'
                    }
                ],
            }
        },
        methods: {
            getCurrentOpConfig(){
                Common.getCurrentOpConfig('MOLD').then(data => {
                    console.log(`Common.getCurrentOpConfig('MOLD')`, data)
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
                console.log("this is from OP status value", value)
                if (value.operatingStatus === 'WORKING' || value.operatingStatus === 'IDLE') {
                    if(value.timeUnit==='DAYS') {
                        return this.resources['in_use_within'] + ' ' + value.time + ' ' + this.resources['dds1']
                    } else {
                        return this.resources['in_use_within'] + ' ' + value.time + ' ' + this.resources['hh2']
                    }
                } else if ( value.operatingStatus === 'NOT_WORKING') {
                  if(value.timeUnit==='DAYS') {
                    return this.resources['not_in_use_within'] + ' ' + value.time + ' ' + this.resources['dds1']
                  } else {
                    return this.resources['not_in_use_within'] + ' ' + value.time + ' ' + this.resources['hh2']
                  }
                } else {
                    if(value.timeUnit==='DAYS') {
                        return this.resources['no_signal_from'] + ' ' + value.time + ' ' + this.resources['dds2']
                    } else {
                        return this.resources['no_signal_from'] + ' ' + value.time + ' ' + this.resources['hh2']
                    }
                }
            },
            getConfiguration() {
                Common.getCurrentOpConfig('MOLD').then(data => {
                    console.log(`Common.getCurrentOpConfig('MOLD')`, data)
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
                console.log('@checkingEnableOpConfig')
                Common.getCategoryConfigStatus()
                .then(data => {
                    this.enabled = data[this.configCategory].enabled
                    if (this.enabled) this.getConfiguration()
                })
                .catch(err => console.log(err))
            }
        },
        mounted(){
            this.checkingEnableOpConfig();
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
        z-index: 100;
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
        display: block !important;
    }

</style>
