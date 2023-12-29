<!-- 
이 컴포넌트는 공통 컴포넌트 이므로, 수정했을 경우, 이 컴포넌트를 사용한 모든 곳에 영향을 끼칠 수 있습니다.
그렇기 때문에 이 컴포넌트의 수정은 금지하며, 수정이 필요할 경우 woojin에게 요청해주시기 바랍니다. 
props에 설명된 주석을 읽고 활용해주세요.

This component is a common component, so if you modify it, it can affect everywhere you use this component.
Therefore, modification of this component is prohibited, and if you need to modify it, please ask woojin.
Please read and use the comments described in props.


- emoldino woojin
-->

<template>
    <div :class="`tab-group-box ${size} ${buttonType}`" :style="styleProps">
        <label v-for="(item, index) in items" :key="index" @click="executor(item)" :disabled="disabled">
            <input type="radio" :name="tabName" :value="item.title" autocomplete="off" :checked="item.default" />
            <div v-text="resources[item.title]"></div>
        </label>
    </div>
</template>

<script>
module.exports = {
    props: {
        items: {
            type: Object,
            default: {},
        },
        resources: Object,
        styleProps: {
            default: '',
            Type: String,
        },
        clickHandler: Function, // The function to act upon when the button is clicked..
        disabled: Boolean, // Set whether disabled or not
        size: {
            type: String,
            default: '',
        },
        tabName: {
            type: String,
            default: 'options',
        },
        buttonType: {
            type: String,
            default: '',
        },
    },
    data() {
        return {};
    },
    methods: {
        executor(item) {
            if (this.clickHandler) {
                this.clickHandler(item);
            }
        },
    },
};
</script>

<style scoped>
label {
    margin-bottom: 0rem !important;
}
.tab-group-box > label > input {
    display: none;
}

/* primary tab style start --------------------------------------------------------------------- */
.tab-group-box.primary {
    display: flex;
    align-items: center;
}
.tab-group-box.primary > label > div {
    min-width: 132px;
    height: 26px;
    padding: 0px 6px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #888888;
    font-size: 14.66px;
    background-color: #fff;
    border: 0.5px solid #d0d0d0;
    border-radius: 3px;
}
.tab-group-box.primary > label > div {
    margin-right: 10px;
}
.tab-group-box.primary > label:last-of-type > div {
    margin-right: 0px;
}
.tab-group-box.primary > label > input:checked + div {
    background-color: #3491ff;
    color: #fff;
}
/* secondary tab style end --------------------------------------------------------------------- */

/* secondary tab style start --------------------------------------------------------------------- */
.tab-group-box.secondary {
    display: flex;
}
.tab-group-box.secondary > label > div {
    min-width: 56px;
    padding: 0px 6px;
    height: 30px;
    border: 0.5px solid #d0d0d0;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #8b8b8b;
    font-size: 14.66px;
}
.tab-group-box.secondary > label:first-of-type > div {
    border-radius: 3px 0px 0px 3px;
}
.tab-group-box.secondary > label:last-of-type > div {
    border-radius: 0px 3px 3px 0px;
}
.tab-group-box.secondary > label > input:checked + div {
    border-color: #3491ff;
    background-color: #deedff;
    color: #3491ff;
}
/* secondary tab style end --------------------------------------------------------------------- */
</style>
