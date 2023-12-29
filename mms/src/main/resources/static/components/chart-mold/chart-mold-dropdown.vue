<template>
    <div class="chart-mold-dropdown" v-click-outside="closeMoldDropdownPopup">
        <div class="dropdown-button" @click="visible = !visible" :class="{open: visible, 'has-left-icon': !!leftIcon}">
            <div class="left-icon" v-if="!!leftIcon">
                <img v-show="!visible" :src="leftIcon" :alt="leftIcon" />
                <img v-show="visible" :src="leftIconOpen" :alt="leftIconOpen" />
            </div>
            <div class="button-label">{{ titleShow }}</div>
            <div class="right-icon">
                <img v-show="!visible" src="images/icon/down-arrow-grey.svg" alt="down arrow grey" />
                <img v-show="visible" src="images/icon/down-arrow-white.svg" alt="down-arrow white" />
            </div>
        </div>
        <div class="dropdown-body" v-show="visible">
            <div v-for="(option, index) in options"
                 :key="index"
                 @click="selectOption(option.value)"
                 class="option-item"
                 :class="{active: option.value === selectedValue}">
                {{ option.label }}
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            filterType: String,
            options: Array,
            selectedValue: [String, Number],
            select: Function,
            leftIcon: String,
            leftIconOpen: String
        },
        data() {
            return {
                visible: false
            };
        },
        methods: {
            selectOption(option) {
                this.select(this.filterType, option);
                this.visible = false;
            },
            closeMoldDropdownPopup(){
                this.visible = false;
            }
        },
        computed: {
            titleShow: function () {
                let label = this.selectedValue;
                this.options.forEach(option => {
                    if (option.value === this.selectedValue) {
                        label = option.label;
                    }
                });
                return label;
            }
        },
        mounted() {
        }
    };
</script>

<style scoped>
</style>
