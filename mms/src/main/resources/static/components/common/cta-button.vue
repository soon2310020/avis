<!-- 
이 컴포넌트는 공통 컴포넌트 이므로, 수정했을 경우, 이 컴포넌트를 사용한 모든 곳에 영향을 끼칠 수 있습니다.
그렇기 때문에 이 컴포넌트의 수정은 금지하며, 수정이 필요할 경우 woojin에게 요청해주시기 바랍니다. 

This component is a common component, so if you modify it, it can affect everywhere you use this component.
Therefore, modification of this component is prohibited, and if you need to modify it, please ask woojin.

Design reference link:
https://www.figma.com/file/UftykJ37WoqcvLUgmEOWYX/eMoldino-Design-System?node-id=0%3A1

Component docs link: 
https://docs.google.com/presentation/d/1XaU9Orvhj8qIFkbvlfqtnArz6QWV6m2t_k4W6cvzwPc/edit#slide=id.g131e2b4a0aa_0_17


- emoldino woojin
-->
// changed @click to @click.stop because popover was not working
<template>
    <div @click.stop="executor" :class="buttonClassComputed" :style="styleProps" :active="active" :disabled="disabled">
        <img v-if="imagePosition === 'left'" class="left-side-image" src="" alt="button-left-side-image" />
        <slot></slot>
        <img v-if="imagePosition === 'right'" class="right-side-image" src="" alt="button-right-side-image" />
    </div>
</template>

<script>
module.exports = {
    props: {
        styleProps: {
            default: '',
            Type: String,
        },
        clickHandler: Function,
        active: Boolean,
        disabled: Boolean,
        colorType: String, // '', 'blue'(default), 'blue-fill', 'red', 'red-fill', 'green', 'green-fill', 'white'
        // '', 'dropdown', 'date-picker', 'upload', 'export', 'text', 'text dropdown'
        buttonType: {
            type: String,
            default: '',
        },
        // '', 'small'
        size: {
            type: String,
            default: '',
        },
    },
    computed: {
        colorTypeComputed() {
            let item = this.colorType;

            if (!this.colorType) {
                item = 'blue';
            }
            if (this.buttonType === 'upload') {
                item = 'blue';
            }
            if (this.buttonType === 'dropdown' && this.colorType === '') {
                item = 'blue';
            }
            if (this.buttonType === 'export') {
                item = 'blue-fill';
            }
            if (this.buttonType === 'text' || this.buttonType === 'text dropdown') {
                item = '';
            }
            return item;
        },
        textPositionComputed() {
            let item = this.imagePosition ? '' : 'center';
            return item;
        },
        buttonClassComputed() {
            let item = `default ${this.colorTypeComputed} ${this.buttonType} ${this.size} ${this.textPositionComputed}`;

            return item;
        },
        imagePosition() {
            let item = '';
            if (
                this.buttonType === 'export' ||
                this.buttonType === 'upload' ||
                this.buttonType === 'dropdown' ||
                this.buttonType === 'text dropdown' ||
                this.buttonType === 'date-picker'
            ) {
                item = 'right';
            }
            console.log('item: ', item);
            return item;
        },
    },
    methods: {
        executor() {
            if (this.clickHandler) {
                this.clickHandler();
            }
        },
    },
};
</script>

<style scoped>
/* small size style start ==================================== */
.small {
    height: 25px !important;
    font-size: 11.25px !important;
}
/* small size style end ====================================== */

/* text button style start =================================== */
.text {
    color: #3491ff;
    font-weight: normal !important;
}
.text:hover {
    color: #3585e5;
    text-decoration: underline;
}
.text[active] {
    color: #3585e5;
}
/* text button style end ===================================== */

/* blue color style start ==================================== */
.blue {
    border: solid 1px #3491ff;
    color: #3491ff;
    transition: 0.2s;
}
.blue:hover {
    color: #3585e5;
    background-color: #daeeff;
    border-color: transparent;
}
.blue[active] {
    color: #3585e5;
}
.blue[active]:before {
    border: 2px solid #3491ff;
    animation-name: blue-border-show;
}

@-webkit-keyframes blue-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #3491ff;
    }
}
/* blue color style end ==================================== */

/* blue-fill style start ===================================== */
.blue-fill {
    background-color: #3491ff;
    color: #fff;
}
.blue-fill:hover {
    background-color: #3585e5;
}
.blue-fill[active] {
    background-color: #3585e5;
}
.blue-fill[active]:before {
    border: 2px solid #deedff;
    animation-name: blue-fill-border-show;
}

@-webkit-keyframes blue-fill-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #deedff;
    }
}
/* blue-fill style end ======================================= */

/* green color style start =================================== */
.green {
    border: solid 1px #47b576;
    color: #47b576;
}
.green:hover {
    background-color: #c3f2d7;
    border-color: transparent;
}
.green[active] {
    background-color: #c3f2d7;
    color: #3ea662;
}
.green[active]:before {
    border: 2px solid #47b576;
    animation-name: green-border-show;
}
@-webkit-keyframes green-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #47b576;
    }
}
/* green color style end ===================================== */

/* green-fill color style start ============================== */
.green-fill {
    background-color: #47b576;
    color: #fff;
}
.green-fill:hover {
    background-color: #3ea662;
    border-color: transparent;
}
.green-fill[active] {
    background-color: #3ea662;
}
.green-fill[active]:before {
    border: 2px solid #c3f2d7;
    animation-name: green-fill-border-show;
}
@-webkit-keyframes green-fill-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #c3f2d7;
    }
}
/* green-fill color style end ================================ */

/* red color style start ===================================== */
.red {
    border: solid 1px #e34537;
    color: #e34537;
}
.red:hover {
    background-color: #f5c7c7;
    border-color: transparent;
}
.red[active] {
    color: #c92617;
}
.red[active]:before {
    border: 2px solid #c92617;
    animation-name: red-border-show;
}
@-webkit-keyframes red-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #c92617;
    }
}
/* red color style end ======================================= */

/* red-fill color style start ================================ */
.red-fill {
    background-color: #e34537;
    color: #fff;
}
.red-fill:hover {
    background-color: #c92617;
    border-color: transparent;
}
.red-fill[active] {
    background-color: #c92617;
}
.red-fill[active]:before {
    border: 2px solid #f5c7c7;
    animation-name: red-fill-border-show;
}
@-webkit-keyframes red-fill-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #f5c7c7;
    }
}
/* red-fill color style end ================================== */

/* white color style start ================================== */
.white {
    border: solid 1px #d6dade;
    color: #595959;
}
.white:hover {
    background-color: #f4f4f4;
    border-color: #4b4b4b;
    color: #4b4b4b;
}
.white[active] {
    color: #4b4b4b;
    background-color: #f4f4f4;
    border-color: transparent;
}
.white[active]:before {
    border: 2px solid #f4f4f4;
    animation-name: white-border-show;
}
@-webkit-keyframes white-border-show {
    0% {
        border-color: transparent;
    }
    100% {
        border-color: #f4f4f4;
    }
}
/* white color style end ================================== */

/* default button style start ======================================== */
.default {
    padding: 0px 8px;
    border-radius: 3px;
    font-size: 14.66px;
    width: fit-content;
    height: 29px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-right: 5px;
    margin: 2px;
    position: relative;
    cursor: pointer;
    transition: 0.3s;
}
.default.center {
    justify-content: center;
}
.default[active] {
    border-radius: 0px;
}
.default[active]:before {
    content: '';
    top: -2px;
    left: -2px;
    width: calc(100% + 4px);
    height: calc(100% + 4px);
    position: absolute;
    border-radius: 3px;
    animation-duration: 1s;
}

/* default button style end ======================================== */

/* disabled style start =================================== */
[disabled] {
    background-color: rgb(196, 196, 196);
    color: white !important;
    border: inherit;
    pointer-events: none;
}

/* disabled style end =================================== */

/* dropdown style start ===================================== */
.dropdown > .right-side-image {
    background-repeat: no-repeat;
    width: 13px;
    height: 6px;
    display: inline-flex;
    align-items: center;
    margin-left: 5px;
}
.dropdown[active] > .right-side-image {
    transform: rotate(180deg);
}

.dropdown.blue-fill > .right-side-image {
    content: url(/images/common-component/dropdown-arrow-white.svg);
    transition: 0.2s;
}

.dropdown.blue > .right-side-image,
.dropdown.text > .right-side-image {
    content: url(/images/common-component/dropdown-arrow-blue.svg);
    transition: 0.2s;
}

.dropdown[disabled] > .right-side-image {
    content: url(/images/common-component/dropdown-arrow-white.svg) !important;
    transform: rotate(0deg) !important;
}
/* dropdown style end ===================================== */

/* date-picker style end ======================================== */
.date-picker.blue > .right-side-image {
    content: url(/images/icon/blue-calendar.svg);
    margin-left: 5px;
}
.date-picker.blue:hover > .right-side-image,
.date-picker.blue[active] > .right-side-image {
    content: url(/images/icon/blue-calendar-hover.svg);
}

.date-picker.blue-fill > .right-side-image {
    content: url(/images/icon/white-calendar.svg);
    margin-left: 5px;
}
/* date-picker style end ======================================== */

/* export style start ======================================== */
.export > .right-side-image {
    content: url(/images/icon/icon-white-export.svg);
    margin-left: 5px;
}
.export:hover > .right-side-image {
    content: url(/images/icon/icon-white-export.svg);
}
/* export style end ======================================== */

/* upload style start ======================================== */
.upload > .right-side-image {
    content: url(/images/icon/icon-blue-upload.svg);
    margin-left: 5px;
}
.upload:hover > .right-side-image {
    content: url(/images/icon/icon-blue-upload.svg);
}
.upload[disabled] > .right-side-image {
    content: url(/images/icon/icon-white-upload.svg) !important;
}
/* upload style end ======================================== */
</style>
