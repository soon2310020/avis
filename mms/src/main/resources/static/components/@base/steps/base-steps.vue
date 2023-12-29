<template>
    <div
        class="base-steps"
    >
        <div class="base-steps-list">
            <div
                v-for="step in list"
                :key="step.value"
                class="base-steps-item"
                :class="{
                  active: step.key === current,
                  done: step.checked,
                }"
                @click="$emit('change', step)"
            >
                <span class="base-steps-item__label">{{ step.label }}</span>
                <span class="base-steps-item__icon"></span>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    name: 'BaseSteps',
    props: {
        list: {
            type: Array,
            default: () => [] // { label: '', value: '' }
        },
        type: {
            type: String,
            default: 'check',
            validator(val) {
                return ['check', 'number'].includes(val)
            }
        },
        readonly: {
            type: Boolean,
            default: false
        },
        current: {
            type: String,
            default: ''
        },
    },
};
</script>

<style scoped>
.base-steps.readonly {
    cursor: not-allowed;
}

.base-steps-list {
    display: flex;
    height: 54px;
    gap: 80px;
}

.base-steps-item {
    position: relative;
    cursor: not-allowed;
    pointer-events: none;
}

.base-steps-item__label {
    width: max-content;
    font-weight: 400;
    font-size: 14.66px;
    line-height: 17px;
    color: var(--grey-dark);
    position: absolute;
    left: 50%;
    top: 36px;
    transform: translateX(-50%);
}

.base-steps-item__icon {
    width: 32px;
    height: 32px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 50%;
    border-width: 2px solid;
    border-style: solid;
    border-color: var(--grey-4);
    position: relative;
    transition: linear 300ms;
}

/* .base-steps-item.not-allowed {
    pointer-events: none;
    cursor: not-allowed;
} */

.base-steps-item.active {
    cursor: pointer;
    pointer-events: auto;
}

.base-steps-item.active .base-steps-item__icon {
    border-color: var(--blue-dark);
}
.base-steps-item .base-steps-item__icon::after {
    content: "";
    display: inline-block;
    height: 2px;
    width: 80px;
    position: absolute;
    top: 50%;
    left: calc(100% + 3px);
    background: var(--grey-4);
    transition: linear 300ms;
}

.base-steps-item:last-child .base-steps-item__icon::after {
    display: none;
}

.base-steps-item.active .base-steps-item__icon::before {
    content: "";
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    position: absolute;
    background-color: var(--blue-dark);
}

.base-steps-item.done {
    cursor: pointer;
    pointer-events: auto;
}

.base-steps-item.done .base-steps-item__icon {
    border-color: var(--blue);
    background-color: var(--blue);
}

.base-steps-item.done .base-steps-item__icon::before {
    content: "";
    display: inline-block;
    width: 13px;
    height: 13px;
    border-radius: 50%;
    position: absolute;
    background-color: #fff;
    mask-image: url("/images/icon/check.svg");
    -webkit-mask-image: url("/images/icon/check.svg");
    mask-position: center;
    -webkit-mask-position: center;
    mask-size: contain;
    -webkit-mask-size: contain;
}

.base-steps-item.done .base-steps-item__icon::after {
    background-color: var(--blue-dark);
}

.base-steps-item.active.done .base-steps-item__icon {
    background-color: var(--white);
}

.base-steps-item.active.done .base-steps-item__icon::before {
    background-color: var(--blue-dark);
    mask-image: unset;
    -webkit-mask-image: unset;
    width: 10px;
    height: 10px;
}

.base-steps-item.active.done .base-steps-item__icon::after {
    background-color: var(--grey-4);
}

/* STATE */
</style>
