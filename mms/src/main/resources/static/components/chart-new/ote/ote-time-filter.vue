<template>
    <div class="ote-status-filter">
        <a-popover v-model="visible" placement="bottomRight" trigger="click">
            <a :id="'ote-time-filter-'+ id" @click="showAnimation()" href="javascript:void(0)" style="width: 132px;" class="dropdown_button-custom">
                <span :class="{'selected-dropdown-text' : visible}">{{ timeOptions[requestParam.dateViewType] }}</span>
                
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
                    <template v-for="time in Object.keys(timeOptions)">
                        <a-col style="padding: 7px;" :key="time">
                            <p-radio v-model="requestParam.dateViewType"  :value="time" @change="setChangeData();onChange($event, time); ">{{timeOptions[time]}}</p-radio>
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
                timeOptions: {
                    LAST7DAYS: 'Last 7 days',
                    LAST30DAYS: 'Last 30 days',
                },
                displayCaret: false,
                caretTimeout: null,
                animationTimeout: null,
            };
        },
        methods: {
            checked(time) {
                return this.time.includes(time)
            },
            onChange(event, time) {
                this.filter()
                this.displayCaret = true;
            },
            showAnimation (){
                const el = document.getElementById('ote-time-filter-'+ this.id)
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
                const el = document.getElementById('ote-time-filter-'+ this.id)
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
        },
        watch: {
            visible: function() {
                if (!this.visible) {
                    this.closeAnimation();
                }
            }
        },
        mounted() {
        }
    };
</script>

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
