<template>
  <div class="step-content">
    <div class="form-item align-top">
      <label style="margin-top: 8px;">{{ resources["request_to"] }}:</label>
      <div>
        <base-button
          type="dropdown"
          level="secondary"
          @click="toggleUserDropdown"
        >
            {{resources['add_user_s']}}
        </base-button>
        <common-popover
          @close="closeUserDropdown"
          :is-visible="visibleUserDropdown"
          :style="{
            width: '210px',
            marginTop: '4px',
            transform: 'translate(88px, 85px)'
          }"
        >
          <common-select
            class="custom-dropdown--assign"
            :style="{ position: 'static', width: '100%' }"
            :items="userIds"
            :searchbox="userIds.length >= 5"
            :multiple="true"
            :has-toggled-all="true"
            :placeholder="resources['search_user_name']"
            @close="closeUserDropdown"
            @on-change="handleChangeUser"
          >
          </common-select>
        </common-popover>
        <div class="mt-2">
          <base-avatar
            v-for="(item, index) in getSelectedUser"
            :key="index"
            :item="item"
            class="mr-2"
          >
          </base-avatar>
        </div>
      </div>
    </div>
    <div class="form-item align-top">
      <label style="margin-top: 8px;">{{ resources["detail_s"] }}:</label>
      <div>
        <textarea
          v-model="request.detail"
          cols="100"
          rows="7"
          class="custom-text-area"
          style="width: 100%; resize: none"
          :placeholder="resources['enter_detail_s']"
        ></textarea>
      </div>
    </div>
    <div class="form-item align-top">
      <label style="margin-top: 8px;">{{ resources["attachments"] }}:</label>
      <div>
        <div class="op-upload-button-wrap">
          <base-button level="secondary">
            {{ resources["upload_file_image"] }}
            <img class="ml-1" src="/images/icon/upload-blue.svg" alt="" />
          </base-button>
          <input
            type="file"
            ref="fileupload"
            id="files1"
            @change="handleSelectAttachment"
            multiple
            style="height: 40px; width: 100%"
            accept=".gif, .jpg, .jpeg, .png, .doc, .zip, .pdf, .docx, .xls, .xlsx, .ppt, .pptx"
          />
        </div>
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
module.exports = {
  components: {
    "common-select": httpVueLoader("/components/@base/dropdown/common-select.vue"),
    "preview-images-system": httpVueLoader("/components/common/preview-images-system.vue"),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({})
    },
    request: {
      type: Object,
      default: () => ({})
    },
    userIds: {
      type: Array,
      default: () => ([])
    },
  },
  setup(props, ctx) {
    // STATE //
    const visibleUserDropdown = ref(false)

    // COMPUTED //
    const getSelectedUser = computed(() => {
      return _.filter(props.userIds, (o) => o.checked);
    })

    // METHODS //
    const toggleUserDropdown = () => {
      visibleUserDropdown.value = !visibleUserDropdown.value
    }

    const closeUserDropdown = () => {
      visibleUserDropdown.value = false
    }

    const handleChangeUser = (userIds) => {
      ctx.emit('select-users', userIds)
    }

    const handleSelectAttachment = (e) => {
      const files = e.target.files;
      let isExitsFile = []
      if (props.request?.attachment) {
        isExitsFile = props.request.attachment.filter(
          (item) => item.name === files[0].name
        );
      }
      if (files && isExitsFile.length === 0) {
        const selectedFiles = Array.from(files);
        ctx.emit('select-attachment', selectedFiles)
      }
    }

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
      //state//
      visibleUserDropdown,
      //computed//
      getSelectedUser,
      //methods//
      toggleUserDropdown,
      closeUserDropdown,
      handleChangeUser,
      handleSelectAttachment,
      handleDeleteAttachment,
      handleDeleteUploadedAttachment,
      handleClearFileUpload
    }
  }
}
</script>

<style scoped>
.custom-dropdown--assign li label{
    width: unset !important;
}
</style>