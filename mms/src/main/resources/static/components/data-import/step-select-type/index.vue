<template>
    <div class="select-type">
        <div class="lead">
            <span> {{ capitalize(resources['select']) }} <b>{{ resources['single'] }}</b> {{ resources['or'] }} <b>{{
                resources['multiple'] }}</b> {{ resources['data_you_would_like_to_import'] }} </span>
        </div>
        <div class="select-type-file">
            <div class="list-option">
                <label v-for="(item, index) in listFileOptionsAvailable" :key="`${item.value}-${item.checked}`"
                    class="list-option-item">
                    <input type="checkbox" class="list-option-item__input" :checked="item.checked"
                        @change="e => handleChange(e, index)" />
                    <span class="list-option-item__icon">
                        <span class="icon" :class="`icon-${item.iconSrc}`"></span>
                    </span>
                    <span class="list-option-item__label">{{ item.label }}</span>
                </label>
            </div>
        </div>
        <guide-import style="margin-top: 40px; margin-bottom: 40px" :resources="resources"></guide-import>
        <div class="action-bar">
            <base-button level="primary" :disabled="!enableNext" @click="$emit('next', selectedFileOptions)"> {{
                resources['next'] }} </base-button>
        </div>
    </div>
</template>

<script>
const STEPS = {
    UPLOAD: 'UPLOAD',
    FILE: 'FILE'
}
module.exports = {
    name: 'StepSelectType',
    components: {
        'guide-import': httpVueLoader('../guide-import/guide-import.vue')
    },
    props: {
        resources: Object,
        permissions: Object,
        listSelected: Array,
    },
    setup(props) {
        const listFileOptions = ref([
            { label: 'Company', value: 'COMPANY', checked: false, iconSrc: 'company', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_COMPANY },
            // { label: 'Category', value: 'CATEGORY', checked: false, iconSrc: 'category' },
            { label: 'Plant', value: 'PLANT', checked: false, iconSrc: 'plant', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_PLANT },
            { label: 'Part', value: 'PART', checked: false, iconSrc: 'part', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_PART },
            { label: 'User', value: 'USER', checked: false, iconSrc: 'group-user', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_USER },
            { label: 'Machine', value: 'MACHINE', checked: false, iconSrc: 'machine', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_MACHINE },
            { label: 'Tooling', value: 'TOOLING', checked: false, iconSrc: 'tooling', permissionCode: Common.PERMISSION_CODE.DATA_IMPORT_IMPORT_TOOLING },
        ])

        const listFileOptionsAvailable = computed(() => listFileOptions.value.slice())

        const handleChange = (event, index) => {
            listFileOptions.value.splice(index, 1, { ...listFileOptions.value[index], checked: event.target.checked })
        }

        const selectedFileOptions = computed(() => listFileOptions.value.filter(f => f.checked))
        const enableNext = computed(() => selectedFileOptions.value.length)

        watch([() => props.permissions, () => props.resources], ([newValPermissions, newValResources]) => {
            listFileOptions.value = listFileOptions.value
                .map(i => ({ ...i, label: newValResources?.[i?.label]?.toLowerCase() || i.label }))
                .filter(j => Object.keys(newValPermissions).some(permissionCode => j.permissionCode === permissionCode))
        }, { immediate: true })

        watch(() => props.listSelected, newVal => {
            const listValues = newVal.map(i => i.value)
            listFileOptions.value = listFileOptions.value.map(i => {
                return { ...i, checked: listValues.includes(i.value) }
            })
        })

        watchEffect(() => {
            console.log('listFileOptions', listFileOptions.value)
        })

        return {
            STEPS,
            enableNext,
            handleChange,
            listFileOptions,
            listFileOptionsAvailable,
            selectedFileOptions,
        }
    }
}
</script>

<style scoped>
.select-type .lead {
    font-size: 14.66px;
    font-weight: 400;
    text-align: center;
    margin-bottom: 40px;
}

.list-option {
    display: flex;
    justify-content: center;
    align-items: stretch;
    gap: 2rem;
}

.list-option-item {
    display: flex;
    max-width: 133px;
    width: 100%;
    gap: 10px;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    position: relative;
    border: 1px solid #d6dade;
    border-radius: 3px;
    padding: 1rem 0.5rem;
}

.list-option-item__input {
    position: absolute;
    top: 6px;
    right: 6px;
}

.list-option-item__icon {
    max-width: 100%;
}

.list-option-item__label {
    display: inline-block;
    text-align: center;
    font-size: 14.66px;
    line-height: 17px;
    max-width: 110px;
}

.action-bar {
    position: fixed;
    bottom: calc(46px + 2rem);
    right: 2rem;
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
}

/* ICON */
.list-option-item__icon .icon {
    display: inline-block;
    width: 63px;
    height: 70px;
    background-color: var(--grey-dark);
    mask-size: contain;
    -webkit-mask-size: contain;
    mask-position: center;
    -webkit-mask-position: center;
    mask-repeat: no-repeat;
    -webkit-mask-repeat: no-repeat;
}

.icon-single-document {
    mask-image: url("/images/icon/single-document.svg");
    -webkit-mask-image: url("/images/icon/single-document.svg");
}

.icon-multiple-document {
    mask-image: url("/images/icon/multiple-document.svg");
    -webkit-mask-image: url("/images/icon/multiple-document.svg");
}

.icon-company {
    mask-image: url("/images/icon/company-2.svg");
    -webkit-mask-image: url("/images/icon/company-2.svg");
}

.icon-category {
    mask-image: url("/images/icon/category-2.svg");
    -webkit-mask-image: url("/images/icon/category-2.svg");
}

.icon-part {
    mask-image: url("/images/icon/part-2.svg");
    -webkit-mask-image: url("/images/icon/part-2.svg");
}

.icon-plant {
    mask-image: url("/images/icon/plant.svg");
    -webkit-mask-image: url("/images/icon/plant.svg");
}

.icon-group-user {
    mask-image: url("/images/icon/group-user.svg");
    -webkit-mask-image: url("/images/icon/group-user.svg");
}

.icon-machine {
    mask-image: url("/images/icon/machine.svg");
    -webkit-mask-image: url("/images/icon/machine-2.svg");
}

.icon-tooling {
    mask-image: url("/images/icon/tooling-2.svg");
    -webkit-mask-image: url("/images/icon/tooling-2.svg");
}</style>
