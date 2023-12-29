<script>
const FILE_TYPE = {
  IMAGE: "image",
  DOCUMENT: "document",
};

module.exports = {
  name: "InputFile",
  components: {
    "emdn-file-upload-root": EmoldinoComponents.FileUploadRoot,
    "emdn-file-upload-trigger": EmoldinoComponents.FileUploadTrigger,
    "emdn-cta-button": EmoldinoComponents.CtaButton,
    "emdn-chip": EmoldinoComponents.Chips,
    "input-file-view-image": httpVueLoader("./input-file-view-image.vue"),
  },
  props: {
    label: {
      type: String,
      default: "Upload File",
    },
    type: {
      type: String,
      default: FILE_TYPE.DOCUMENT,
      validator(val) {
        return [FILE_TYPE.DOCUMENT, FILE_TYPE.IMAGE].includes(val);
      },
    },
    multiple: {
      type: Boolean,
      default: true,
    },
    accept: {
      type: String,
      default: "",
    },
    value: {
      type: Array /** { id: string, fileName: string, contentType: string }[] */,
      default: () => [],
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    chipStyle: {
      type: [String, Object],
      default: "display: inline-flex; width: 100px;",
    },
  },
  setup(props, { emit }) {
    const { mutateAsync } = useFileUploadMutation();
    const fileList = ref([]);
    const fileListRaw = ref([]);
    const onViewDocument = () => {
      Common.downloadMultipleFile(fileList.value);
    };

    const onInput = (fileListInfo) => {
      fileListInfo.forEach((fileInfo) => {
        fileListRaw.value.forEach((fileRaw) => {
          if (fileInfo.fileName === fileRaw.name) i.fileRaw = fileRaw;
        });
      });
      fileList.value = fileListInfo;
      emit("input", fileList.value);
    };

    const preUpload = (fRaw) => {
      return mutateAsync(fRaw, {
        onSuccess: () => {
          fileListRaw.value.push(fRaw);
        },
      });
    };

    return {
      FILE_TYPE,
      fileList,
      preUpload,
      onInput,
      onViewDocument,
    };
  },
};
</script>

<template>
  <div>
    <emdn-file-upload-root
      v-slot="{ removeFile }"
      :value="fileList"
      :pre-upload="preUpload"
      :accept="accept"
      :multiple="multiple"
      @input="onInput"
    >
      <emdn-file-upload-trigger v-if="!readonly" as-child>
        <emdn-cta-button color-type="blue" variant="upload">
          {{ label }}
        </emdn-cta-button>
      </emdn-file-upload-trigger>

      <div
        v-if="fileList.length"
        class="d-flex align-items-center mt-1"
        style="gap: 0.25rem"
      >
        <emdn-chip
          v-for="item in fileList"
          :key="item.id"
          :style-props="chipStyle"
          :inactive="readonly"
          :click-handler="() => removeFile(item.id)"
        >
          {{ item.fileName }}
        </emdn-chip>

        <emdn-cta-button
          v-if="type === FILE_TYPE.DOCUMENT"
          variant="text hyperlink"
          :click-handler="viewDocument"
        >
          View File(s)
        </emdn-cta-button>
        <input-file-view-image
          v-if="type === FILE_TYPE.IMAGE"
          :file-list="fileList"
        ></input-file-view-image>
      </div>
    </emdn-file-upload-root>
  </div>
</template>
