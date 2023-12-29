<template>
    <div class="d-flex justify-space-between">
        <div class="back-icon">
            <span v-show="listItem.length > 5" class="icon back-arrow" :class="{ 'disable': currentIndex === 0 }"
                @click="handleChangePage('-')"></span>
        </div>
        <div class="d-flex justify-center">
            <div v-for="item in getDisplayItem" :key="item.id" class="item-card"
                :class="{ 'active': item.id === selectedItemId }" @click="handleClickItem(item)">
                <div class="item-card-content">
                    <div style="font-size: 14.66px">{{ type === 'mold' ? resources['tooling'] : resources[type]}}</div>
                    <div style="font-size: 12px; ">
                        <a-tooltip placement="bottom">
                            <template slot="title">
                                <div style="padding: 6px;font-size: 13px;">
                                    {{ resources[`${type === 'mold' ? 'tooling' : type}_id`]}} : {{ item.code }}
                                </div>
                            </template>
                            <div class="item-card-content-subtitle">
                                {{ resources[`${type === 'mold' ? 'tooling' : type}_id`]}} : {{ item.code }}
                            </div>
                        </a-tooltip>
                    </div>
                </div>
                <div v-if="item.completed" class="item-card-icon">
                    <!-- <img src="/images/icon/data-requests/checked-icon.svg" alt=""> -->
                    <span class="checked-icon"></span>
                </div>
            </div>
        </div>
        <div class="back-icon">
            <span v-show="listItem.length > 5" class="icon back-arrow rotate-180" :class="{ 'disable': isLast }"
                @click="handleChangePage('+')"></span>
        </div>
    </div>
</template>

<script>
const STEP = 1
const SIZE = 5
module.exports = {
    props: {
        resources: {
            type: Object,
            default: () => ({})
        },
        listItem: {
            type: Array,
            default: () => ([])
        },
        type: {
            type: String,
            default: () => ('')
        },
        selectedItemId: {
            type: String,
            default: () => ('')
        }
    },
    setup(props, ctx) {
        const currentIndex = ref(0)
        const getDisplayItem = computed(() => {
            const firstIndex = currentIndex.value * STEP
            const result = [...props.listItem].splice(firstIndex, SIZE)
            console.log('getDisplayItem', result)
            return result
        })

        const isLast = computed(() => {
            const index = currentIndex.value * STEP + SIZE
            return index >= props.listItem.length
        })

        const handleChangePage = (sign) => {
            currentIndex.value = eval(`${currentIndex.value} ${sign} 1`)
        }

        const handleClickItem = (item) => {
            ctx.emit('select-item', item)
        }

        return {
            currentIndex,
            isLast,
            getDisplayItem,
            handleChangePage,
            handleClickItem
        }
    }
}
</script>

<style scoped>
.item-card {
    display: flex;
    align-items: center;
    padding: 4px 8px;
    box-shadow: rgb(0 0 0 / 25%) 0px 1px 4px;
    border-radius: 3px;
    width: fit-content;
    margin: 0 6px;
    cursor: pointer;
}

.item-card-content {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.item-card-icon {
    margin-left: 4px;
}

.item-card.active {
    color: #FFFFFF;
    background-color: #3491FF;
}

.item-card-content-subtitle {
    width: 120px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-align: center;
}
</style>