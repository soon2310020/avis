<template>
  <div class="step-content">
    <div class="form-item">
      <label>{{ resources["request_type"] }}:</label>
      <base-input width="200px" :readonly="true" :value="getRequestType">
      </base-input>
    </div>
    <div class="d-flex align-center">
      <div class="form-item">
        <label>{{ resources["request_id"] }}:</label>
        <base-input width="200px" :readonly="true" :value="request.requestId">
        </base-input>
      </div>
      <div class="form-item" style="margin-left: 40px">
        <label>{{ resources["assigned_to"] }}:</label>
        <div>
          <base-avatar
            v-for="(item, index) in selectedUsers"
            :key="index"
            :item="item"
            class="mr-2"
          >
          </base-avatar>
        </div>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["request_date"] }}:</label>
      <div class="d-flex align-center">
        <base-input
          style="width: 105px"
          :readonly="true"
          :value="getRequestDateTitle"
        >
        </base-input>
        <base-input
          style="width: 65px; margin-left: 4px;"
          :readonly="true"
          :value="requestTimeDisplay"
        >
        </base-input>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["due_date"] }}:</label>
      <div class="d-flex align-center">
        <base-input
          style="width: 105px"
          :readonly="true"
          :value="getDueDateTitle"
        >
        </base-input>
        <base-input
          style="width: 65px; margin-left: 4px;"
          :readonly="true"
          :value="dueTimeDisplay"
        >
        </base-input>
      </div>
    </div>
    <div class="form-item">
      <label>{{ resources["data_type_s"] }}:</label>
      <div class="d-flex align-center">
        <base-chip
          v-for="item in getDataTypes"
          :key="item.key"
          :selection="item"
          :allow-tooltips="false"
          :color="typeColors[item.key]"
          :close-able="false"
        >
        </base-chip>
      </div>
    </div>
    <div class="form-item align-top">
      <label style="margin-top: 8px">{{ resources["detail_s"] }}:</label>
      <div>
        <textarea
          v-model="request.detail"
          cols="100"
          rows="7"
          readonly
          class="custom-text-area"
          style="width: 100%; resize: none"
          :placeholder="resources['enter_detail_s']"
        ></textarea>
      </div>
    </div>
    <div class="form-item align-top">
      <label style="margin-top: 8px">{{ resources["attachments"] }}:</label>
      <div>
        <preview-images-system
          :is-viewing="false"
          :images="request.attachment"
          :images-uploaded="request.uploadedAttachment"
          @delete-img="handleDeleteAttachment"
          @delete-uploaded-img="handleDeleteUploadedAttachment"
        ></preview-images-system>
      </div>
    </div>
  </div>
</template>

<script>
const TYPE_COLORS = {
  'COMPANY': 'blond',
  'PLANT': 'wheat',
  'PART': 'pale-blue',
  'TOOLING': 'lavender',
  'MACHINE': 'melon',
}
module.exports = {
  components: {
    "preview-images-system": httpVueLoader("/components/common/preview-images-system.vue"),
  },
  props: {
    request: {
      type: Object,
      default: () => ({})
    },
    resources: {
      type: Object,
      default: () => ({})
    },
    types: {
      type: Array,
      default: () => ([])
    },
    selectedUsers: {
      type: Array,
      default: () => ([])
    },
    getRequestDateTitle: {
      type: String,
      default: () => ('')
    },
    requestTimeDisplay: {
      type: String,
      default: () => ('')
    },
    getDueDateTitle: {
      type: String,
      default: () => ('')
    },
    dueTimeDisplay: {
      type: String,
      default: () => ('')
    },

  },
  setup(props) {
    // STATE //
    const typeColors = ref({ ...TYPE_COLORS })

    // COMPUTED //
    const getRequestType = computed(() => {
      return Common.formatter.toCase(props.request.requestType, 'capitalize')
    })

    const getDataTypes = computed(() => {
      const selectedType = props.types.filter(item => item.checked)
      const displayDataType = selectedType.map((item) => {
        if (item.checked) {
          const newItem = { ...item, title: `${props.resources[item.multipleLabelKey]} (${item.value})`}
          return newItem
        }
      })
      return displayDataType
    })

    const handleDeleteAttachment = (index) => {
      ctx.emit('delete-attachment', index)
    }

    const handleDeleteUploadedAttachment = async (file) => {
      const fileId = file.id
      ctx.emit('delete-uploaded-attachment', fileId)
    }

    const handleClearFileUpload = (value) => {
      ctx.refs.fileupload.value = value
    }

    return {
      typeColors,
      getRequestType,
      getDataTypes,
      handleDeleteAttachment,
      handleDeleteUploadedAttachment,
      handleClearFileUpload
    }
  }
}
</script>

<style>
</style>