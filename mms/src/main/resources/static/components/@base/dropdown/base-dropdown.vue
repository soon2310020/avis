<template>
  <div
    ref="baseDropdownRef"
    class="base-dropdown"
    :class="[
      `base-dropdown-${level}`,
      `base-dropdown-${size}`,
      { 'base-dropdown-multiple': multiple },
      { 'base-dropdown-loading': loading },
    ]"
  >
    <base-button
      ref="baseButtonRef"
      :level="level"
      :size="size"
      :disabled="disabled"
      type="dropdown"
      class="base-dropdown__selected"
      :active="isActive"
      @click="isActive = !isActive"
    >
      {{ !selected.label ? $slots.default?.[0].text : selected.label }}
    </base-button>
    <div class="base-dropdown__popover" :class="{ 'visible': isActive }">
      <div class="base-dropdown__popover-inner">
        <base-search
          v-if="filterDisplay"
          v-model="filter"
          :placeholder="placeholder"
        ></base-search>
        <div v-if="filterDisplay && multiple" class="base-dropdown__toolbar">
          <base-button
            class="base-dropdown__toolbar-option"
            @click="handleMultiSelect(true)"
            >Select All</base-button
          >
          <base-button
            class="base-dropdown__toolbar-option"
            @click="handleMultiSelect(false)"
            >Unselect All</base-button
          >
        </div>
        <ul class="base-dropdown__options">
          <li
            v-if="loading"
            class="base-dropdown__options-item"
            style="pointer-events: none"
          >
            Loading ...
          </li>
          <li v-for="option in filteredOptions" :key="option.index">
            <label
              class="base-dropdown__options-item"
              @click="handleSingleSelect(option)"
            >
              <input
                v-if="multiple && originOptions[option.index]"
                class="base-dropdown__options-checkbox"
                type="checkbox"
                v-model.lazy="originOptions[option.index].checked"
              >
              <div class="base-dropdown__options-box"></div>
              <span class="base-dropdown__options-label">
                {{ shorten(option.label) }}
              </span>
            </label>
          </li>
        </ul>
      </div>
    </div>

    <div v-if="result" class="base-dropdown__results">
      <ul class="base-dropdown__results-list">
        <li
          v-for="item in selected.list"
          :key="item.index"
          class="base-dropdown__results-item"
        >
          {{ item.label }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>

module.exports = {
  name: 'BaseDropdown',
  props: {
    level: {
      type: String,
      default: '',
      validator(val) {
        return ['primary', 'secondary', 'tertiary'].includes(val)
      }
    },
    type: {
      type: String,
      default: 'default',
      validator(val) {
        return ['default', 'select'].includes(val)
      }
    },
    size: {
      type: String,
      default: 'medium',
      validator(val) {
        return ['small', 'medium', 'large'].includes(val)
      }
    },
    placeholder: {
      type: String,
      default: ''
    },
    value: {
      type: [Object, String],
      default: null
    },
    options: {
      type: Array,
      default: () => []
    },
    multiple: {
      type: Boolean,
      default: false
    },
    label: {
      type: String,
      default: 'Item'
    },
    pluralLabel: String,
    default: {
      type: Boolean,
      default: false
    },
    maxChar: {
      type: Number,
      default: undefined
    },
    maxWidth: {
      type: [Number, String]
    },
    primitive: {
      type: Boolean,
      default: false
    },
    result: {
      type: Boolean,
      default: false
    },
    sort: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props, ctx) {
    const baseDropdownRef = ref(null)
    const baseButtonRef = ref(null)
    const originOptions = ref([])

    const isActive = ref(false)

    const count = reactive({
      selected: 0, // need manually set in order to avoid rerender
      origin: computed(() => originOptions.value.length)
    })

    const filter = ref('')
    const filterDisplay = computed(() => count.origin >= 4)
    const filteredOptions = ref([])

    const getLabel = () => {
      if (!props.multiple) {
        return selected?.item?.label
      }
      if (props.multiple) {
        const isAll = count.selected === count.origin
        const isSingle = count.selected <= 1
        const isPlural = count.selected > 1
        if (isAll) return props.pluralLabel ? `All ${props.pluralLabel}` : `All ${props.label}s`
        if (isSingle) return `${count.selected} ${props.label}`
        if (isPlural) return props.pluralLabel ? `${count.selected} ${props.pluralLabel}` : `${count.selected} ${props.label}s`
      }
    }

    const selected = reactive({
      item: null,
      list: computed(() => originOptions.value.filter(item => item.checked)),
      label: computed(getLabel)
    })

    const emitValue = () => {
      if (props.primitive) ctx.emit('input', selected.item?.value) // emit primitive value
      else ctx.emit('input', selected.item) // emit object value
    }

    // TODO Add truncate + tooltip

    const handleClose = () => {
      isActive.value = false
      setTimeout(() => {
        filter.value = ''
      }, 700)
    }

    const handleClickOutside = (event) => {
      const isOutside = !baseDropdownRef.value.contains(event.target)
      if (isOutside) {
        handleClose()
        if (props.multiple) {
          ctx.emit('change', selected.list)
          count.selected = selected.list.length
        }
      }
    }

    const handleSingleSelect = (selectedItem) => {
      selected.item = selectedItem
      ctx.emit('select', selectedItem)
      emitValue()
      if (!props.multiple) handleClose()
    }

    const handleMultiSelect = (state) => {
      originOptions.value = originOptions.value.map(item => ({ ...item, checked: state }))
    }

    watch(() => props.options, newVal => {
      if (newVal && !props.multiple) originOptions.value = newVal.map((item, index) => ({ ...item, index }))
      else if (newVal && props.multiple) originOptions.value = newVal.map((item, index) => ({ ...item, index, checked: false }))
    })

    watch(originOptions, newVal => {
      filteredOptions.value = newVal.filter(item => item.label?.toLowerCase().includes(filter.value.toLowerCase()))
    })

    watch(filter, newVal => {
      filteredOptions.value = originOptions.value.filter(item => item.label?.toLowerCase().includes(newVal?.toLowerCase()))
    })

    watch(() => props.value, newVal => {
      if (Common.isPrimitive(newVal)) {
        selected.item = originOptions.value.filter(opt => opt.value === newVal)?.[0]
      }
      else selected.item = { ...newVal }
    }, { immediate: true, deep: true })

    watch(() => props.default, newVal => {
      if (newVal) {
        selected.item = originOptions.value[0]
      } else {
        selected.item = null
      }
      emitValue()
    }, { immediate: true })

    watch(isActive, newVal => {
      if (newVal) window.addEventListener('click', handleClickOutside)
      else window.removeEventListener('click', handleClickOutside)
    })

    onMounted(() => {
      count.selected = selected.list.length
      originOptions.value = props.options.map((item, index) => ({ ...item, index }))
    })


    // DEBUG
    
    return {
      baseDropdownRef,
      originOptions,
      filter,
      filterDisplay,
      filteredOptions,
      selected,
      handleSingleSelect,
      handleMultiSelect,
      isActive,
    }
  }
}
</script>

<style scoped>
ul {
  list-style-type: none;
  padding-left: 0;
  margin-bottom: 0;
  max-height: 200px;
  overflow: auto;
}

.base-dropdown {
  position: relative;
  display: inline-block;
  align-self: baseline;
}

.base-dropdown__popover {
  position: absolute;
  top: 100%;
  left: 50%;
  z-index: 10;
}

.base-dropdown__popover-inner {
  position: absolute;
  padding: 2px;
  background: var(--white);
  border-radius: 3px;
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.25);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transform: translate(-50%, 0px) scale(0);
  transform-origin: top center;
  transition: opacity 200ms, visibility 200ms, transform 200ms;
}

.base-dropdown__popover.visible .base-dropdown__popover-inner {
  z-index: 1;
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
  transform: translate(-50%, 4px) scale(1);
}

.base-dropdown__toolbar {
  margin-top: 2px;
}

.base-dropdown__toolbar-option {
  font-size: 14.66px;
  line-height: 17px;
}

.base-dropdown__options {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.base-dropdown__options-item {
  position: relative;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 18px;
  color: var(--grey-dark);
  display: block;
  min-width: 201px;
  width: unset;
  padding: 8px 6px;
  margin-top: 2px;
  margin-bottom: 2px;
  cursor: pointer;
}

.base-dropdown__options-item:last-child {
  margin-bottom: 0;
}

.base-dropdown__options-item:hover {
  background: #e6f7ff;
}

.base-dropdown__options-checkbox {
  display: none;
}

/* MULTIPLE */

.base-dropdown-multiple .base-dropdown__options-label {
  padding-left: 32px;
}

.base-dropdown-multiple .base-dropdown__options-box {
  position: absolute;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  left: 11px;
  top: 50%;
  transform: translateY(-50%);
  width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
}

.base-dropdown-multiple
  .base-dropdown__options-checkbox:checked
  ~ .base-dropdown__options-box {
  border: 1px solid #0075ff;
  background-color: var(--white);
}

.base-dropdown-multiple
  .base-dropdown__options-checkbox:checked
  ~ .base-dropdown__options-box::after {
  content: "";
  display: inline-block;
  width: 9px;
  height: 9px;
  background-color: #0075ff;
  border-radius: 1px;
}

/* LEVEL */

.base-dropdown.primary {
  color: var(--white);
  background: var(--blue);
}

.base-dropdown.primary:hover {
  background: var(--blue-dark);
}
</style>