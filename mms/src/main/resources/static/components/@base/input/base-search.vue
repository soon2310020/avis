<template>
    <div class="base-search">
        <span class="base-search__icon">
            <span class="base-search__icon-img"></span>
        </span>
        <input
            type="text"
            class="base-search__input"
            :value="localVal"
            @input="debounceInput"
            :placeholder="placeholder"
        />
    </div>
</template>

<script>
module.exports = {
    name: 'BaseSearch',
    props: {
        value: {
            type: String,
            default: ''
        },
        placeholder: {
            type: String,
            default: ''
        }
    },
    setup(props, { emit }) {
        const localVal = ref('')
        const debounceInput = _.debounce(function (e) {
            localVal.value = e.target.value
            emit('input', e.target.value)
        }, 500)

        watch(() => props.value, newVal => { localVal.value = newVal })

        return {
            localVal,
            debounceInput
        }
    }
}
</script>

<style scoped>
.base-search {
    display: flex;
    padding: 2px;
    border: 0.5px solid #d6dade;
    border-radius: 3px;
}

.base-search__icon {
    display: inline-flex;
    width: 25.4px;
    height: 25.4px;
    justify-content: center;
    align-items: center;
    background-color: #f0f3f5;
}

.base-search__icon-img {
    display: inline-block;
    width: 16.44px;
    height: 16.43px;
    mask-image: url("/images/icon/search.svg");
    -webkit-mask-image: url("/images/icon/search.svg");
    mask-position: center;
    -webkit-mask-position: center;
    mask-size: contain;
    -webkit-mask-size: contain;
    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;
    background-color: var(--grey-4);
}

.base-search__icon img {
    width: 16.44px;
    height: 16.44px;
}

.base-search__input {
    flex: 1;
    border: none;
    padding-left: 5px;
}

.base-search__input::placeholder {
    font-weight: 400;
    font-size: 14.66px;
    line-height: 17px;
    color: #9d9d9d;
}
</style>
