<template>
    <div class="ote-status-filter">
        <a-popover v-model="visible" placement="bottomLeft" trigger="click">
            <a :id="'ote-status-filter-'+id" @click="showAnimation()" style="margin-right: 10px; width: 98px" class="dropdown_button-custom">
                <span style="white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    padding: 3px 0px 3px 0px;
                    width: 60px;
                    text-align: left;"
                    >
                {{ showTitle }}</span> 
                <div>
                    <a-icon class="caret" :class="{'selected-dropdown-text' : visible, 'caret-show' : displayCaret}" type="caret-down" />
                </div>
            </a>
            <div
                    slot="content"
                    style="padding: 8px 6px;"
                    class="dropdown-scroll"
            >
                <div class="content-body">
                    <template v-for="status in Object.keys(statusOptions)">
                        <a-col style="padding: 7px;" :key="status">
                            <p-check :checked="checked(status)" @change="setChangeData(); onChange($event, status); ">{{statusOptions[status]}}</p-check>
                        </a-col>
                    </template>
                </div>
            </div>
        </a-popover>
    </div>
</template>

<script>
    module.exports = {
        props: {
            resources:Object,
            filter: Function,
            requestParam: Object,
            id: String,
            setChangeData: Function
        },
        data() {
            return {
                visible: false,
                statusOptions: {
                    WORKING: 'In Production',
                    IDLE: 'Idle',
                    NOT_WORKING: 'Inactive',
                    DISCONNECTED: 'Sensor Offline'
                },
                selectedStatus: [],
                displayCaret: false,
                caretTimeout: null,
                animationTimeout: null,
              isLoadedData: false
            };
        },
        methods: {
            initStatusDefaultValue() {
                this.selectedStatus = this.requestParam.ops.split(',');
            },
            checked(status) {
                return this.selectedStatus.includes(status)
            },
            onChange(event, status) {
                if (event) {
                    this.selectedStatus.push(status);
                } else {
                    this.selectedStatus = this.selectedStatus.filter(selected => selected !== status);
                }
                this.filter(this.selectedStatus);
                this.displayCaret = true
            },
            showAnimation (){
                const el = document.getElementById('ote-status-filter-'+ this.id)
                if (!this.visible) {
                    setTimeout(() => {
                        el.classList.add("dropdown_button-animation");
                        this.animationTimeout = setTimeout(() => {
                        el.classList.remove("dropdown_button-animation");
                        el.classList.add("selected-dropdown-text");
                        el.classList.add("selected-dropdown-button");
                        }, 1600);
                    }, 1);
                    
                } else {
                    this.closeAnimation()
                }
            },
            closeAnimation(){
                const el = document.getElementById('ote-status-filter-'+ this.id)
                if (this.animationTimeout != null) {
                    console.log("Here");
                    console.log(this.animationTimeout);
                    clearTimeout(this.animationTimeout);
                    this.animationTimeout = null;
                } 
                if (this.caretTimeout != null) {
                    clearTimeout(this.caretTimeout);
                    this.caretTimeout = null;
                }
                el.classList.remove("dropdown_button-animation");
                    el.classList.remove("selected-dropdown-text");
                    el.classList.remove("selected-dropdown-button");
                    this.displayCaret = false
            },
        },

        computed: {
            showTitle(){
                // return `${this.selectedStatus.length} Status`
                if (this.selectedStatus.length == 1) {
                    if (this.visible) {
                        return '1 Status';
                    } else {
                        // let result = this.statusOptions.filter(item => item)
                        return this.statusOptions[`${this.selectedStatus[0]}`]
                    }
                } else {
                    return `${this.selectedStatus.length} Status`
                }
            }
        },
        watch: {
            visible: function() {
                if (!this.visible) {
                    this.closeAnimation();
                }
            },
          requestParam: {
            handler: function(newVal, oldVal) {
              console.log('newVal.ops watch1'+newVal.ops)
              if(newVal.ops && !this.isLoadedData) {
                console.log('newVal.ops watch2'+newVal.ops)
                this.isLoadedData = true;
                this.initStatusDefaultValue();
              }
            },
              immediate: true,
              deep: true,
            },
        },
        mounted() {
          console.log('newVal.ops mounted1'+JSON.stringify(this.requestParam))
            if (this.requestParam.ops && !this.isLoadedData) {
              console.log('newVal.ops mounted2'+this.requestParam.ops)
              this.isLoadedData = true;
              this.initStatusDefaultValue();
            }
        }
    };
</script>
<style>
.ant-popover{
        /* transform: translate(0%, 4px) !important; */
        top: inherit;
        left: inherit;
        transform-origin: bottom right !important;
    }
</style>
<style scoped>
    .dropdown_button {
        display: flex;
        justify-content: space-between;
        padding: 0px 10px;
        align-items: center;
        width: 100px;
        background: #f2f5fa;
    }
    .ant-btn.active,
    .ant-btn:active,
    .ant-btn:focus > svg {
        fill: #fff !important;
    }

    .down-icon {
        fill: #564efb;
    }

    .ant-menu-submenu-title:hover {
        background-image: linear-gradient(#414ff7, #6a4efb) !important;
        background-color: red;
    }

    .first-layer-wrapper {
        position: relative;
    }

    .first-layer {
        position: absolute;
        top: 0;
    }
    
</style>
