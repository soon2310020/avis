<template>
  <div ref="counter" class="d-flex counter-container justify-content-center">
    <a href="javascript:void(0)" class="
        d-flex
        justify-content-center
        align-center
        counter-button counter-button--left
      " :class="{ disabled: disabled }" @click="handleChangeNumber('-')">
      <img src="/images/icon/icon-minus.svg" alt="" />
    </a>
    <input type="number" class="counter-input" :class="{ disabled: disabled }" :value="getDisplayValue"
      @input="handleInput" @keydown="handleKeydown" />
    <a href="javascript:void(0)" @click="handleChangeNumber('+')" class="
        d-flex
        justify-content-center
        align-center
        counter-button counter-button--right
      " :class="{ disabled: disabled }">
      <img src="/images/icon/icon-plus.svg" alt="" />
    </a>
  </div>
</template>

<script>
module.exports = {
  name: 'BaseManualCounter',
  props: {
    value: {
      type: [Number, String],
      default: "",
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    allowDecimal:  {
      type: Boolean,
      default: false,
    },
    validation: Function
  },
  setup(props, ctx) {
    const getDisplayValue = computed(() => {
      if (props.disabled) {
        return ''
      } else {
        return props.value
      }
    })

    const handleValidate = (value) => {
      if (props.validation) {
        const check = props.validation(value)
        if (check) {
          ctx.emit('counter-change', value)
        } else {
          addWarningAnimation()
        }
      } else {
        ctx.emit('counter-change', value)
      }
    }

    const handleInput = _.debounce(function (event) {
      let inputText = event.target.value
      if (inputText === '') {
        inputText = 0
      }
      handleValidate(inputText)
      console.log('handleInput', inputText)
      
    }, 500);

    const handleKeydown = (event) => {
      const isNumber = props.allowDecimal ? /^[0-9.]$/.test(event.key) : /^[0-9]$/.test(event.key)
      const isAllowed = ['Backspace', 'Enter'].includes(event.key)
      const isDuplicateDot = event.key === '.' && event.target.value.includes('.')
      if (isDuplicateDot) {
        event.preventDefault()
      }
      if (!isNumber && !isAllowed) {
        event.preventDefault()
      }
    };

    const handleChangeNumber = (sign) => {
      const result = eval(`${props.value} ${sign} 1`)
      handleValidate(result)
    }

    const addWarningAnimation = () => {
      const element = ctx.refs['counter']
      if (element) {
        element.classList.add('horizontal-shaking-animation')
        // element.classList.add('warning-border-animation')
        setTimeout(() => {
          element.classList.remove('horizontal-shaking-animation')
          // element.classList.remove('warning-border-animation')
        }, 750);
      }
    }

    return {
      getDisplayValue,
      handleInput,
      handleKeydown,
      handleChangeNumber
    };
  },
}
</script>

<style scoped>
.counter-container {
  height: 25px;
  border: 1px solid #d6dade;
  position: relative;
  width: 80px;
  border-radius: 1.29288px;
}

.counter-input-container {
  width: 80px;
}

.counter-input {
  font-size: 14.66px;
  color: #000000;
  border: none;
  width: 40px;
  text-align: center;
}

.counter-input:focus {
  border: none;
  outline: none;
}

.counter-input::-webkit-outer-spin-button,
.counter-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.counter-container:not(.horizontal-shaking-animation) .counter-button {
  height: 25px;
  text-decoration: none !important;
  background-color: #3491ff;
  border: 1px solid #3491ff;
  padding: 0 6px;
  box-shadow: inset 0px 0px 2.58576px rgba(0, 0, 0, 0.25);
  border-radius: 1.29288px;
  transform: rotate(0.14deg);
  position: absolute;
}
.horizontal-shaking-animation .counter-button{
  height: 23px;
  text-decoration: none !important;
  background-color: var(--red-dark);
  border: 1px solid var(--red-dark);
  padding: 0 6px;
  box-shadow: inset 0px 0px 2.58576px rgba(0, 0, 0, 0.25);
  border-radius: 1.29288px;
  transform: rotate(0.14deg);
  position: absolute;
  margin: 1px 1px 0 1px;
}
.counter-button--left {
  top: -1px;
  left: -1px;
}

.counter-button--right {
  top: -1px;
  right: -1px;
}

.counter-container .counter-button.disabled {
  background: #c4c4c4;
  border: 1px solid #c4c4c4;
  box-shadow: inset 0px 0px 2.58576px rgba(0, 0, 0, 0.25);
  border-radius: 1.29288px;
  transform: rotate(0.14deg);
  pointer-events: none;
}

.counter-input.disabled {
  pointer-events: none;
}

.horizontal-shaking-animation {
  animation: horizontal-shaking 0.2s;
  animation-iteration-count: 2;
  animation-timing-function: linear;
  animation-direction: alternate;
  border: 1px solid var(--red-dark);
  
}
.warning-border-animation {
  animation: warning-border 0.75s;
  animation-iteration-count: 1;
  animation-timing-function: ease-in-out;
  animation-direction: alternate;
}

@keyframes horizontal-shaking {
  0% {
    transform: translateX(0)
  }

  25% {
    transform: translateX(3px)
  }

  50% {
    transform: translateX(-3px)
  }

  75% {
    transform: translateX(3px)
  }

  100% {
    transform: translateX(0)
  }
}

@keyframes warning-border {
  0% {}

  25% {
    border: 1px solid var(--red-dark);
  }

  50% {
    border: 1px solid var(--red-dark);
  }

  75% {
    border: 1px solid var(--red-dark);
  }

  100% {}
}
</style>