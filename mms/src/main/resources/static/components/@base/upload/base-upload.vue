<template>
  <div
    class="base-upload"
    @dragover="dragover"
    @dragleave="dragleave"
    @drop="drop"
    @click="onClickArea"
  >
    <input
      style="display: none"
      type="file"
      multiple
      class="w-px h-px opacity-0 overflow-hidden absolute"
      @change="onChange"
      ref="file"
      :accept="acceptTypes"
    />
    <slot></slot>
  </div>
</template>

<script>
module.exports = {
  props: {
    replace: {
      type: Boolean,
      default: false,
    },
    accept: {
      type: Array,
      default: undefined,
    },
    listFiles: {
      type: Array,
      default: () => [],
    },
  },
  setup(props, { refs, emit }) {
    const acceptTypes = computed(() =>
      props.accept.map((i) => `.${i}`).join(",")
    );

    const onClickArea = () => {
      refs.file.click();
    };
    const onChange = () => {
      emit("change", [...refs.file.files]);
      refs.file.value = "";
      console.log("refs.file", refs.file);
    };
    const dragover = (event) => {
      event.preventDefault();
      if (!event.currentTarget.classList.contains("bg-green-300")) {
        event.currentTarget.classList.add("drag-over");
      }
    };
    const dragleave = (event) => {
      event.currentTarget.classList.remove("drag-over");
    };

    const __getFileExtension = (fileName) => {
      if (typeof fileName === "string" && fileName.split(".").length > 1) {
        const splitted = fileName.split(".");
        return splitted[splitted.length - 1];
      }
      return undefined;
    };

    const drop = (event) => {
      event.preventDefault();
      const hasAcceptTypes =
        props.accept?.length && Array.isArray(props.accept);
      const hasInvalidFile = !Array.from(event.dataTransfer.files).some((i) => {
        const extension = __getFileExtension(i.name);
        const hasMatched = props.accept.includes(extension);
        return hasMatched;
      });
      // const isInvalid = !hasAcceptTypes || !Array.from(event.dataTransfer.files).some())
      if (!hasAcceptTypes || hasInvalidFile) {
        console.log("handle invalid file case");
        event.currentTarget.classList.remove("drag-over");
        return;
      }
      refs.file.files = event.dataTransfer.files;
      onChange();
      event.currentTarget.classList.remove("drag-over");
    };

    return {
      acceptTypes,
      onClickArea,
      onChange,
      dragover,
      dragleave,
      drop,
    };
  },
};
</script>

<style scoped>
[v-cloak] {
  display: none;
}

.base-upload {
  background: #fafafa;
  border: 1px dashed #dfe2e5;
  cursor: pointer;
}

.base-upload.drag-over {
  border-width: 2px;
  border-color: var(--blue);
}
</style>
