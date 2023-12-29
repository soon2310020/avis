<template>
  <div class="step-content">
    <div class="form-item">
      <label>{{ resources["request_date"] }}:</label>
      <div class="d-flex align-items-center">
        <base-button
          level="primary"
          icon="calendar"
          :disabled="true"
        >
          {{ getRequestDateTitle }}
        </base-button>
        <base-button
          level="primary"
          style="margin-left: 4px;"
          :disabled="true"
        >
          {{ requestTimeDisplay }}
        </base-button>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["due_date"] }}:</label>
      <div class="d-flex align-items-center">
        <base-button
          level="secondary"
          icon="calendar"
          @click="showDatePicker"
        >
          {{ getDueDateTitle }}
        </base-button>
        <common-hour-picker
          style="margin-left: 4px"
          size="medium"
          :selected-time="dueTime"
          @change-time="handleChangeDueTime"
        ></common-hour-picker>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    request: {
      type: Object,
      default: () => ({})
    },
    resources: {
      type: Object,
      default: () => ({})
    },
    requestDate: {
      type: Object,
      default: () => ({})
    },
    getRequestDateTitle: {
      type: String,
      default: () => ('')
    },
    requestTime: {
      type: Object,
      default: () => ({})
    },
    requestTimeDisplay: {
      type: String,
      default: () => ('')
    },
    dueDate: {
      type: Object,
      default: () => ({})
    },
    getDueDateTitle: {
      type: String,
      default: () => ('')
    },
    dueTime: {
      type: Object,
      default: () => ({})
    },
    dueTimeDisplay: {
      type: String,
      default: () => ('')
    },
    showDatePicker: Function
  },
  setup(props, ctx) {

    const handleChangeDueTime = (value, type) => {
        ctx.emit('change-due-time', value, type)
    }

    return {
        handleChangeDueTime
    }
  }
}
</script>
    
<style>
</style>