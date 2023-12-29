<template>
  <div
    class="dropdown-box"
    style="position: relative"
  >
    <common-select-button
      type="outline"
      :selected="cloneList"
      :is-active="showDropdown"
      :get-label="getLabel"
      :max-char="labelMaxLength"
      v-bind="selectButtonProps"
      @click="handleShow"
      animation-secondary
    >
      {{ placeholder }}
    </common-select-button>
    <common-popover
      :is-visible="showDropdown"
      :position="{ top: '100%', left: '-30px' }"
      @close="handleClose"
    >
      <common-dropdown
        style="position: static"
        :placeholder="`Search ${label}`"
        :allcheck="hasCheckall"
        :searchbox="hasSearchbox"
        :checkbox="hasCheckbox"
        :items="cloneList"
        :click-handler="handleClick"
        :all-click-handler="handleClickAll"
        v-bind="dropdownProps"
        :style="styleProps"
        :all-check-label="allCheckLabel"
      ></common-dropdown>
    </common-popover>
  </div>
</template>

<script>
module.exports = {
  name: 'CommonSelect',
  props: {
    resources: Object,
    hasSearchbox: {
      type: Boolean,
      default: false
    },
    hasCheckbox: {
      type: Boolean,
      default: false
    },
    hasCheckall: {
      type: Boolean,
      default: false
    },
    listOptions: {
      type: Array,
      default: () => []
    },
    value: {
      type: [Array, Object, String, Number],
      default: null
    },
    getLabel: {
      type: Function,
    },
    label: {
      type: String,
      default: 'Items'
    },
    labelMaxLength: {
      type: Number,
      default: 20
    },
    placeholder: {
      type: String,
      default: 'Placeholder'
    },
    dropdownProps: {
      type: Object,
      default: () => ({})
    },
    selectButtonProps: {
      type: Object,
      default: () => ({})
    },
    popoverProps: {
      type: Object,
      default: () => ({})
    },
    styleProps: Object,
    allCheckLabel: {
      type: String,
      default: () => 'All Check'
    }
  },
  setup(props, ctx) {
    const showDropdown = ref(false)
    const cloneList = ref([]) // avoid binding two way by v-model in dropdown

    const handleClick = (event) => {
      ctx.emit('change', event)
      if (!props.hasCheckbox) {
        handleClose()
      }
    }

    const handleClickAll = (event) => {
      ctx.emit('check-all', event)
      if(!props.hasCheckbox){
        showDropdown.value = false;
      }
    }

    const handleClose = () => {
      showDropdown.value = false
      ctx.emit('close')
    }

    const handleShow = () => {
      showDropdown.value = true
      ctx.emit('show')
    }

    // watchEffect(() => console.log('props.listOptions', props.listOptions))

    watch(() => props.listOptions, newVal => {
      cloneList.value = [...newVal]
    }, { deep: true, immediate: true })

    watchEffect(() => console.log('cloneList.value', cloneList.value))

    return {
      cloneList,
      showDropdown,
      handleShow,
      handleClose,
      handleClick,
      handleClickAll
    }
  }
}
</script>


<style scoped>
.dropdown-box {
  position: relative;
}

.dropdown-box:last-of-type {
  margin-right: 0px;
}
</style>