<template>
    <div style="position: relative;">
        <input type="checkbox" v-if="show" :checked="checked" @click.prevent="showSelectAllOptions = true">
        <base-popover :is-visible="showSelectAllOptions" class="select-all-popover"
            @close="showSelectAllOptions = false">
            <ul class="select-all-options">
                <li class="select-all-item" @click="handleSelectAll(ACTION_ALL.PAGE)">
                    Select <b>{{ count }}</b> {{ count > 1 ? unit[1] : unit[0] }} on
                    this page
                </li>
                <li class="select-all-item" @click="handleSelectAll(ACTION_ALL.ALL)">
                    Select all <b>{{ total }}</b> {{ total > 1 ? unit[1] : unit[0] }}
                </li>
                <li class="select-all-item" @click="handleSelectAll(ACTION_ALL.DESELECT)">
                    Deselect all
                </li>
            </ul>
        </base-popover>
    </div>
</template>

<script>
const ACTION_ALL = {
    PAGE: 'SELECT_PAGE',
    ALL: 'SELECT_ALL',
    DESELECT: 'DESELECT_ALL'
}
module.exports = {
    name: 'SelectAll',
    props: {
        resources: Object,
        show: Boolean,
        checked: Boolean,
        total: Number,
        count: Number,
        unit: Array,
    },
    setup(props, { emit }) {
        const showSelectAllOptions = ref(false)

        const handleSelectAll = (actionType) => {
            if (actionType === ACTION_ALL.PAGE) emit('select-page')
            if (actionType === ACTION_ALL.ALL) emit('select-all')
            if (actionType === ACTION_ALL.DESELECT) emit('deselect')
            showSelectAllOptions.value = false
        }

        return {
            ACTION_ALL,
            showSelectAllOptions,
            handleSelectAll,
        }
    }
}
</script>

<style scoped>
.select-all-options {
    list-style-type: none;
    text-align: left;
    padding: 2px;
    margin: 0;
}

.select-all-item {
    padding: 6px 8px;
    margin-bottom: 2px;
    font-weight: normal;
    cursor: pointer;
}

.select-all-item:hover {
    background-color: var(--blue-light-2);
}

.select-all-popover {
    width: 250px;
    position: absolute;
    margin-top: 4px;
    left: 0;
    top: 100%;
}
</style>