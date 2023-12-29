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
    <div class="form-item" style="align-items: flex-start">
      <label>{{ resources["data_type_s"] }}:</label>
      <div class="d-flex align-left flex-column">
        <div v-for="item in getDataTypes" :key="item.key">
          <template v-if="item.total > 0">
            <span class="custom-hyper-link" @click="handleCompleteData(item)">{{ request.requestType === 'DATA_REGISTRATION' ? resources['create'] : resources['complete']}} {{resources[item.multipleLabelKey]}}
              <div class="hyper-link-icon" style="margin-left: 3px"></div>
            </span>
            <span class="type-check">{{ `${item.value}/${item.total}` }} {{resources['completed']}} <span class="icon-check" v-show="item.value == item.total"></span></span>
          </template>
        </div>
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
    selectedType: {
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
  setup(props, ctx) {
    // STATE //
    const typeColors = ref({ ...TYPE_COLORS })

    // COMPUTED //
    const getRequestType = computed(() => {
      return Common.formatter.toCase(props.request.requestType, 'capitalize')
    })

    const getDataTypes = computed(() => {
      const displayDataType = props.selectedType.map((item) => ({
        ...item,
        title: `${props.resources[item.multipleLabelKey]} (${item.value})`,
      }))
      return displayDataType
    })

    // WATCH //

    // METHOD //

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

    const handleCompleteData = (item) => {
      if (item.total > 0) {
        const key = item.key
        if (key === 'TOOLING') {
          ctx.emit('complete-data', 'MOLD')
        } else {
          ctx.emit('complete-data', key)
        }
      }
    }

    return {
      typeColors,
      getRequestType,
      getDataTypes,
      handleDeleteAttachment,
      handleDeleteUploadedAttachment,
      handleClearFileUpload,
      handleCompleteData
    }
  }
}
</script>

<style>

.icon-check {
  display: inline-block;
  width: 12px;
  height: 9px;
  background-color: var(--green);
  mask-image: url("/images/icon/check.svg");
  mask-position: center;
  mask-size: contain;
  -webkit-mask-image: url("/images/icon/check.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
}

.type-check {
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: #4B4B4B;
  margin-left: 16px;
}
</style>