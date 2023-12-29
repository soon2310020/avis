<template>
    <div class="custom-tooltip" :class="getTooltipsBackgroundClass">
        <slot></slot>
        <span v-if="hasContent" ref="tooltipContent" class="custom-tooltip-text"
            :class="[getTooltipsTextClass, getArrowClass]">
            <slot name="content"></slot>
        </span>
    </div>
</template>
  
<script>
const BASE_BACKGROUND_CLASS = "tooltip-background--";
const BASE_TEXT_CLASS = "tooltip-text--";
const BASE_ARROW_CLASS = "custom-tooltip-arrow--";
module.exports = {
    props: {
        color: {
            type: String,
            default: () => "grey",
            validator(val) {
                return ["grey", "white", "blue"].includes(val);
            },
        },
        background: {
            type: String,
            default: () => "grey",
            validator(val) {
                return ["grey", "white"].includes(val);
            },
        },
    },
    computed: {
        getTooltipsBackgroundClass() {
            return `${BASE_BACKGROUND_CLASS}${this.background}`;
        },
        getTooltipsTextClass() {
            return `${BASE_TEXT_CLASS}${this.color}`;
        },
        getArrowClass() {
            return `${BASE_ARROW_CLASS}${this.background}`;
        },
        hasContent() {
            console.log("hasContent", this.$slots);
            // if (this.$slots.content) {
            //     const tooltipContent = this.$slots.content();
            //     console.log(tooltipContent[0], tooltipContent[0].children !== "");
            //     if (tooltipContent[0] && tooltipContent[0].children !== "") {
            //         return true;
            //     }
            // }
            return true;
        },
    },
};
</script>
  
<style>
.custom-tooltip {
    position: relative;
    cursor: pointer;
    display: inline-block;
}

.custom-tooltip .custom-tooltip-text {
    visibility: hidden;
    text-align: center;
    line-height: 17px;
    font-size: 14.66px;
    padding: 8px;
    box-shadow: rgb(229 223 223) 0px 0px 4px 1px;
    border-radius: 3px;
    position: absolute;
    z-index: 10;
    top: 150%;
    left: 50%;
    opacity: 0;
    transform: translate3d(-50%, -10px, 0);
    transition: all 0.2s ease-in-out;
}

.custom-tooltip .custom-tooltip-text::after {
    content: "";
    position: absolute;
    bottom: 100%;
    left: 50%;
    margin-left: -5px;
}

.custom-tooltip:hover .custom-tooltip-text {
    visibility: visible;
    opacity: 1;
    transform: translate3d(-50%, 5px, 0);
}

.tooltip-background--grey .custom-tooltip-text {
    background: rgba(0, 0, 0, 0.75);
}

.tooltip-background--white .custom-tooltip-text {
    background: #FFFFFF;
}

.tooltip-text--white {
    color: #FFFFFF;
}

.tooltip-text--grey {
    color: #4B4B4B;
}

.tooltip-text--blue {
    color: #3491ff;
}

.custom-tooltip-arrow--grey::after {
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid rgba(0, 0, 0, 0.75);
    transform: rotate(180deg);
}

.custom-tooltip-arrow--white::after {
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid #FFFFFF;
    transform: rotate(180deg);
}
</style>