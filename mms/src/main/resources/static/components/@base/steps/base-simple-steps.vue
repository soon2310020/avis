<template>
  <div class="base-steps d-flex justify-content-center">
    <div class="base-steps-list">
      <div
          v-for="step in list"
          :key="step.value"
          class="base-steps-item"
          :class="[
          { active: step.value === value },
          { done: value > step.value },
        ]"
      >
        <span class="base-steps-item__label" @click="handleChange(step)">{{ step.label }}</span>
        <span class="base-steps-item__icon" @click="handleChange(step)"></span>
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
    value: {
      type: [String, Number],
      default: 0
    }
  },
  setup(props, {emit}) {
    const handleChange = (targetStep) => {
      emit('@change', targetStep)
    }

    return {
      handleChange
    }
  }
};
</script>

<style scope>
.base-steps {
}

.base-steps-list {
  display: flex;
  height: 54px;
  gap: 80px;
}

.base-steps-item {
  cursor: not-allowed;
  position: relative;
  width: fit-content;
  height: fit-content;
}

.base-steps-item__label {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: var(--grey-dark);
  position: absolute;
  left: 50%;
  top: 36px;
  transform: translateX(-50%);
  width: max-content;
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

.base-steps-item.done .base-steps-item__icon {
  border-color: var(--blue-dark);
  background-color: var(--blue-dark);
}

.base-steps-item.done .base-steps-item__icon::before {
  content: "";
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  position: absolute;
  background-color: #fff;
  mask: url("/images/icon/check.svg");
  -webkit-mask: url("/images/icon/check.svg");
  mask-position: center;
  -webkit-mask-position: center;
  mask-size: initial;
  -webkit-mask-size: initial;
}

.base-steps-item.done .base-steps-item__icon::after {
  background-color: var(--blue-dark);
}
</style>