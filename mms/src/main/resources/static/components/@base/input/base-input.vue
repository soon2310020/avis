<template>
    <div
        class="base-input"
        :class="[
      `base-input__${size}`,
      { 'base-input__disabled': disabled },
      { 'base-input__readonly': readonly },
      { 'base-input__fullWidth': fullWidth },
    ]"
        :style="[width]"
    >
        <span
            v-if="$slots.prefix"
            class="base-input__prefix"
        >
            <slot name="prefix"></slot>
        </span>
        <input
            :value="value"
            :type="inputType"
            :placeholder="placeholder"
            :readonly="readonly"
            @keydown="handleKeydown"
            @keypress="handleKeypress"
            @keyup="handleKeyup"
            @change="handleChange"
            @input="handleInput"
        />
        <span
            v-if="$slots.prefix"
            class="base-input__postfix"
        >
            <slot name="postfix"></slot>
        </span>
    </div>
</template>

<script>
module.exports = {
    name: "BaseInput",
    props: {
        placeholder: String,
        type: {
            type: String,
            default: "text",
            validator(val) {
                return ["text", "number", "radio", "checkbox"].includes(val);
            },
        },
        icon: {
            type: String,
            validator(val) {
                return ["search"].includes(val);
            },
        },
        size: {
            type: String,
            default: "medium",
            validator(val) {
                return ["small", "medium", "large"].includes(val);
            },
        },
        disabled: {
            type: Boolean,
            default: false,
        },
        value: {
            type: [Number, String],
            default: "",
        },
        isNumber: {
            type: Boolean,
            default: false
        },
        width: {
            type: [Number, String],
            default: undefined,
        },
        readonly: {
            type: Boolean,
            default: false,
        },
        fullWidth: {
            type: Boolean,
            default: false,
        },
    },
    setup(props, ctx) {
        const inputType = computed(() => {
            // for scale purpose
            return props.type;
        });

        const handleInput = (event) => {
            ctx.emit("input", event.target.value);
        };

        const handleChange = (event) => {
            ctx.emit("change", event.target.value);
        };

        const handleKeyup = (event) => {
            ctx.emit("keyup", event);
        };

        const handleKeydown = (event) => {
            const isNumber = /^[0-9.]$/.test(event.key)
            const isAllowed = ['Backspace', 'Enter'].includes(event.key)
            const isDuplicateDot = event.key === '.' && event.target.value.includes('.')
            if (props.isNumber && isDuplicateDot) {
                event.preventDefault()
            }
            if (props.isNumber && !isNumber && !isAllowed) {
                event.preventDefault()
            }
            ctx.emit('keydown', event)
        };

        const handleKeypress = (event) => {
            ctx.emit("keypress", event);
        };

        return {
            inputType,
            handleInput,
            handleChange,
            handleKeyup,
            handleKeydown,
            handleKeypress,
        };
    },
};
</script>

<style scoped>
.base-input {
    display: inline-flex;
    flex-direction: row;
    align-items: center;
    padding: 5px 16px 5px 8px;
    border-radius: 3px;
    border: 0.5px solid var(--grey-2);
}

.base-input input {
    flex: 1;
    width: 100%;
    border: none;
    color: var(--grey-dark);
    font-style: normal;
    font-weight: 400;
    font-size: 14.66px;
    line-height: 17px;
    background-color: inherit;
    padding: 0;
}

.base-input input::placeholder {
    color: var(--grey-3);
    font-style: normal;
    font-weight: 400;
    font-size: 14.66px;
    line-height: 17px;
}
.base-input__disabled {
    cursor: not-allowed;
}
.base-input__readonly {
    cursor: default;
    background-color: var(--grey-5);
    border-color: var(--grey-5);
}

.base-input__fullWidth {
    width: 100%;
}

.base-input__readonly input {
    cursor: default;
    pointer-events: none;
}

/* STATE */
</style>
