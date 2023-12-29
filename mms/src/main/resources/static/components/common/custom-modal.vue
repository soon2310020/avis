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
    <div :style="styleProps" class="common-modal-backdrop">
        <div>
            <div class="common-modal-title">
                <div class="head-line"></div>
                <div style="display: flex; align-items: center">
                    <span class="span-title">{{ resources['tooling_report'] }}</span>
                    <!-- modal-header-content -->
                    <slot name="header-content"></slot>
                </div>
                <!-- x button 변경 필요 -->
                <span
                    class="close-button"
                    style="font-size: 25px; display: flex; align-items: center; height: 17px; cursor: pointer"
                    @click="closeHandler"
                >
                    <span class="t-icon-close"></span>
                </span>
            </div>
            <div class="common-modal-body">
                <slot name="body-content"></slot>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    props: {
        resources: Object,
        styleProps: {
            default: '',
            Type: String,
        },
        closeHandler: Function,
    },
};
</script>

<style scoped>
.common-modal-title {
    background: #f5f8ff;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 19.5px 25.5px 11.5px 31px;
    border-radius: 6px 6px 0 0;
}
.head-line {
    position: absolute;
    width: 100%;
    background: #52a1ff;
    height: 8px;
    border-radius: 6px 6px 0 0;
    top: 0;
    left: 0;
}
.common-modal-title .span-title {
    color: #4b4b4b;
    font-weight: bold;
    font-size: 16px;
    line-height: 100%;
    margin-right: 64px;
}

.common-modal-body {
    padding: 20px 56px;
    overflow: scroll;
    min-height: 660px;
    background-color: white;
}

.common-modal-backdrop {
    display: flex;
    justify-content: center;
    align-items: center;
    position: absolute;
    z-index: 9999;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    background-color: rgba(0, 0, 0, 0.5);
}

.common-modal-backdrop > div {
    width: 1024px;
    height: 718px;
}
</style>
