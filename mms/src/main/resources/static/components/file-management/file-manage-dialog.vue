<template>
  <base-dialog
    :title="dialogState.title"
    :visible="dialogState.show"
    dialog-classes="dialog-lg"
    @close="$emit('close')"
  >
    <div class="file-manage-dialog">
      <div class="form">
        <div class="form-item">
          <label>File Group Code</label>
          <base-input
            v-model="form.fileGroupCode"
            :readonly="
              dialogState.mode === 'view' || dialogState.mode === 'update'
            "
            class="custom-input"
            placeholder="File Group Code"
          ></base-input>
        </div>

        <div class="form-item">
          <label>File Group Name</label>
          <base-input
            v-model="form.fileGroupName"
            :readonly="dialogState.mode === 'view'"
            class="custom-input"
            placeholder="File Group Name"
          ></base-input>
        </div>

        <div class="form-item">
          <label>Version</label>
          <base-input
            v-model="form.version"
            :readonly="['view', 'update'].includes(dialogState.mode)"
            class="custom-input"
            placeholder="Input Version Value"
          ></base-input>
        </div>

        <div
          v-if="['view', 'update'].includes(dialogState.mode)"
          class="form-item"
        >
          <label>Recently Released Version</label>
          <base-input
            :readonly="true"
            class="custom-input"
            v-model="form.releasedVersion"
          ></base-input>
        </div>

        <div class="form-item">
          <label>Description</label>
          <textarea
            v-model="form.description"
            :readonly="['view'].includes(dialogState.mode)"
            rows="4"
            class="custom-input"
            placeholder="Description is written right here in the box. "
          ></textarea>
        </div>

        <div v-if="form.fileGroupStatus" class="form-item">
          <label>Status:</label>
          <div style="display: flex; align-items: center">
            <span
              class="form-item__status-icon"
              :style="{
                'background-color': LIST_STATUS[form.fileGroupStatus].color,
              }"
            ></span>
            <span class="form-item__status-label">
              {{ LIST_STATUS[form.fileGroupStatus].label }}
            </span>
          </div>
        </div>

        <div class="form-item">
          <label>File Upload</label>
          <div style="width: 100%">
            <base-upload
              v-show="['create', 'update'].includes(dialogState.mode)"
              ref="baseUploadRef"
              class="custom-base-upload"
              :accept="acceptTypes"
              @change="handleChangeFile"
            >
              <div class="upload-area">
                <div class="upload-area__icon"></div>
                <div class="upload-area__description">
                  Drag and drop or
                  <span class="highlight">browse file</span> to upload
                </div>
                <div class="upload-area__extension">
                  Accepted File Types: {{ acceptTypes.join(",") }}
                </div>
              </div>
            </base-upload>

            <div v-if="listUpload.length" class="list-upload">
              <div
                v-if="isFetchingTemp"
                class="upload-item loading-wave"
                style="height: 68px"
              ></div>
              <template v-else>
                <div
                  v-for="(item, index) in listUpload"
                  :key="`file-${index}-${item.id}`"
                  class="upload-item"
                  :class="{ available: Boolean(item.filePath) }"
                >
                  <div class="upload-item__icon"></div>
                  <div
                    class="upload-item__description"
                    @click="handleClickFile(item.filePath)"
                  >
                    <div class="description-file-name">
                      {{ item.name || item.fileName }}
                    </div>
                    <div class="description-file-size">
                      {{
                        appendThousandSeperator(item.size) ||
                        appendThousandSeperator(item.fileSize) ||
                        0
                      }}
                      KB
                    </div>
                  </div>
                  <div
                    v-if="dialogState.mode !== 'view'"
                    class="upload-item__remove"
                    @click="(event) => handleRemoveFile(event, index)"
                  ></div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="form-item form-item__action">
          <base-button type="cancel" @click="handleClickCancel">
            {{ resources["cancel"] }}
          </base-button>
          <base-button
            v-if="['view'].includes(dialogState.mode)"
            level="primary"
            @click="upgradeDialog.show = true"
          >
            {{ resources["upgrade_version"] }}
          </base-button>
          <base-button
            v-if="
              ['update', 'create'].includes(dialogState.mode) &&
              form.fileGroupStatus !== 'RELEASED'
            "
            level="secondary"
            type="save"
            @click="handleClickSave"
          >
            {{ resources["save"] }}
          </base-button>
          <base-button
            v-if="
              ['update'].includes(dialogState.mode) &&
              form.fileGroupStatus !== 'RELEASED'
            "
            level="primary"
            @click="handleClickRelease"
          >
            {{ resources["release"] }}
          </base-button>
        </div>
      </div>
    </div>
    <base-dialog
      :visible="upgradeDialog.show"
      :title="upgradeDialog.title"
      dialog-classes="dialog-md"
      :nested="true"
      @close="
        () => {
          upgradeDialog.show = false;
          upgradeDialog.newVersion = '';
        }
      "
    >
      <div
        class="form-item"
        style="margin-bottom: 23px; display: flex; align-items: center"
      >
        Enter the new version you would like to upgrade to.
      </div>

      <div
        class="form-item"
        style="margin-bottom: 23px; display: flex; align-items: center"
      >
        <label style="width: 110px">Current Version</label>
        <base-input
          v-model="form.releasedVersion"
          :readonly="true"
        ></base-input>
      </div>
      <div
        class="form-item"
        style="margin-bottom: 23px; display: flex; align-items: center"
      >
        <label style="width: 110px">New Version</label>
        <base-input
          v-model="upgradeDialog.newVersion"
          placeholder="New Version"
        ></base-input>
      </div>
      <div
        class="form-item form-item__action"
        style="
          margin-top: 140px;
          display: flex;
          justify-content: center;
          gap: 0.5rem;
        "
      >
        <base-button type="cancel" @click="upgradeDialog.show = false">
          Cancel
        </base-button>
        <base-button level="primary" @click="handleClickUpgrade">
          Upgrade
        </base-button>
      </div>
    </base-dialog>

    <toast-alert
      :resources="resources"
      :title="toastState.title"
      :content="toastState.content"
      :show="toastState.show"
    ></toast-alert>
  </base-dialog>
</template>

<script>
const DEFAULT_FORM = {
  fileGroupCode: "",
  fileGroupName: "",
  version: "",
  description: "",
  fileGroupStatus: "",
};

const LIST_STATUS = {
  UNMANAGED: {
    color: "var(--grey-dark)",
    label: "Unmanaged",
  },
  UNRELEASED: {
    color: "var(--red)",
    label: "Unreleased",
  },
  RELEASED: {
    color: "var(--green)",
    label: "Released",
  },
};

module.exports = {
  name: "FileManageDialog",
  props: {
    mode: {
      type: String,
      default: "create",
      validator(val) {
        return ["view", "create", "update"].includes(val);
      },
    },
    resources: Object,
    visible: Boolean,
    title: String,
    data: Object,
    refetch: Function,
    fileGroupType: String,
  },
  setup(props, { emit, root, refs }) {
    const form = ref({
      enabled: false,
      fileGroupCode: "",
      fileGroupName: "",
      fileGroupStatus: "",
      version: "",
      releasedVersion: "",
      description: "",
    });

    const dialogState = ref({
      mode: toRef(props, "mode"),
      title: toRef(props, "title"),
      show: toRef(props, "visible"),
    });

    const acceptTypes = ["cvs", "xlsx"];

    const listUpload = ref([]);
    const isProcessing = ref(false);
    const isFetchingTemp = ref(false);

    // ALERT
    const toastState = ref({
      show: false,
      title: "",
      message: "",
    });
    const closeAlert = () => {
      setTimeout(() => {
        toastState.value.show = false;
        setTimeout(() => {
          toastState.value.title = "";
          toastState.value.content = "";
        }, 1000);
      }, 3000);
    };
    const setAlert = ({ title, content, show }) => {
      toastState.value.title = title;
      toastState.value.content = content;
      root.$nextTick(() => {
        toastState.value.show = show;
      });
      closeAlert();
    };

    const upgradeDialog = ref({
      show: false,
      title: "Upgrade Version",
      newVersion: "",
    });

    const handleError = (err) => {
      console.log(err);
      setAlert({ title: "Failure!", content: err.message, show: true });
    };

    const fetchPostTempFile = async (file, index) => {
      console.log("file", file, index);
      isFetchingTemp.value = true;
      const formData = new FormData();
      formData.append("file", file);
      try {
        const response = await axios.post("/api/common/fle-tmp", formData);
        listUpload.value.splice(index, 1, {
          fileName: file.name,
          fileSize: file.size,
          type: file.type,
          id: response.data.id,
        });
        console.log("listUpload", listUpload.value);
      } catch (error) {
        handleError(error);
      } finally {
        isFetchingTemp.value = false;
      }
    };

    const fetchPostOrUpdateFile = async (formData) => {
      isProcessing.value = true;
      console.log("@fetchPostOrUpdateFile", formData);
      try {
        const payload = {};
        payload.fileGroupCode = formData.fileGroupCode;
        payload.fileGroupName = formData.fileGroupName;
        payload.version = formData.version;
        payload.description = formData.description;

        payload.fileRefs = listUpload.value.map((o) => ({
          id: o.id,
          contentType: o.contentType,
          fileNo: o.fileNo,
          fileName: o.name || o.fileName,
          fileSize: o.size || o.fileSize,
          filePath: o.filePath,
        }));
        payload.enabled = true;
        payload.fileGroupType = props.fileGroupType;

        if (dialogState.value.mode === "update") {
          payload.id = formData.id;
          await axios.put(`/api/common/fle-mng/${formData.id}`, payload);
          props.refetch();
          setAlert({
            title: "Success!",
            content: "Your file has been updated.",
            show: true,
          });
        }
        if (dialogState.value.mode === "create") {
          const response = await axios.post("/api/common/fle-mng/one", payload);
          console.log("@create", { payload, response });
          form.value.id = response.data.id;
          props.refetch({ pageNumber: 0 });
          emit("after-create");

          // dialogState.value = Object.assign({}, dialogState.value, {
          //     mode: 'create',
          //     title: 'Create new file',
          //     show: false
          // })

          setAlert({
            title: "Success!",
            content: "Your file has been saved.",
            show: true,
          });
        }
      } catch (error) {
        handleError(error);
      } finally {
        isProcessing.value = false;
      }
    };

    const fetchUpdateReleaseStatus = async (formData, newStatus) => {
      isProcessing.value = true;
      try {
        const payload = {};
        payload.fileGroupCode = formData.fileGroupCode;
        payload.fileGroupName = formData.fileGroupName;
        payload.version = formData.version;
        payload.description = formData.description;

        payload.fileRefs = listUpload.value.map((o) => ({ ...o }));
        payload.enabled = true;
        payload.fileGroupType = props.fileGroupType;

        console.log("payload", payload);

        const response = await axios.put(
          `/api/common/fle-mng/${formData.id}/${newStatus}`,
          payload
        );
        console.log("response", response);
        props.refetch({ pageNumber: 0 });
        setAlert({
          title: "Success!",
          content: "Your file has been updated.",
          show: true,
        });
        form.value.fileGroupStatus = "RELEASED";
      } catch (error) {
        handleError(error);
      } finally {
        isProcessing.value = false;
      }
    };

    const fetchUpgradeVersion = async () => {
      isProcessing.value = true;
      try {
        const payload = {};
        payload.version = upgradeDialog.value.newVersion;
        await axios.put(
          `/api/common/fle-mng/${form.value?.id}/version`,
          payload
        );
        upgradeDialog.value.newVersion = "";
        upgradeDialog.value.show = false;
        props.refetch({ pageNumber: 0 });
        root.setToastAlertGlobal({
          title: "Success!",
          content: `File's version has been upgrade`,
          show: true,
        });
      } catch (error) {
        handleError(error);
      } finally {
        isProcessing.value = false;
      }
    };

    const handleClickCancel = () => {
      emit("close");
    };

    const handleClickSave = async () => {
      fetchPostOrUpdateFile(form.value);
    };

    const handleClickRelease = () => {
      console.log("@release");
      if (form.value.id) fetchUpdateReleaseStatus(form.value, "release");
      else console.log("create file group with release status");
    };

    const handleClickFile = (filePath) => {
      if (filePath) window.open(filePath, "_blank");
    };

    const handleClickUpgrade = () => {
      fetchUpgradeVersion();
    };

    const handleChangeFile = (listFiles) => {
      const fileNumber = listUpload.value.length;
      listUpload.value.push(...listFiles);
      listFiles.forEach((f, i) => {
        fetchPostTempFile(f, i + fileNumber);
      });
    };

    const handleRemoveFile = (event, fileIndex) => {
      event.stopPropagation();
      listUpload.value = listUpload.value.filter(
        (_, index) => index !== fileIndex
      );
      console.log("@handleRemoveFile", fileIndex);
    };

    watch(
      () => props.data,
      (newVal) => {
        if (newVal) {
          form.value = Object.assign({}, form.value, { ...newVal });
          listUpload.value = newVal.fileRefs;
          console.log("@watch:props.data", form.value, listUpload.value);
        }
      },
      { deep: true }
    );

    watch(
      () => props.visible,
      (newVal) => {
        console.log("@watch:props.visible", newVal);
        if (!newVal) {
          form.value = Object.assign({}, form.value, { ...DEFAULT_FORM });
          listUpload.value = [];
        }
      },
      { immediate: true }
    );

    // watchEffect(() => console.log('form', form.value))
    // watchEffect(() => console.log('listUpload', listUpload.value))
    watchEffect(() => console.log("dialogState", dialogState));

    return {
      LIST_STATUS,

      form,
      dialogState,
      acceptTypes,
      listUpload,
      isFetchingTemp,

      toastState,
      upgradeDialog,

      handleClickFile,
      handleChangeFile,
      handleRemoveFile,

      handleClickCancel,
      handleClickUpgrade,
      handleClickSave,
      handleClickRelease,
    };
  },
};
</script>

<style scoped>
.form {
  margin-top: 23px;
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 23px;
}

.form-item label {
  width: 200px;
  flex-shrink: 0;
  align-self: flex-start;
  font-size: 14.66px;
  color: var(--grey-dark);
}

.form-item .custom-input {
  color: var(--grey-dark);
  width: 50%;
}

.form-item textarea {
  resize: none;
  border-color: var(--grey-2);
  border-radius: 3px;
  font-size: 14.66px;
  padding: 5px 16px 5px 8px;
}
.form-item textarea:read-only {
  background-color: var(--grey-5);
  border: unset;
}
.form-item textarea:focus-visible {
  outline: none;
}
.form-item textarea::placeholder {
  color: var(--grey-3);
}

.form-item .form-item__status-icon {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  margin-right: 0.5rem;
}

.form-item__action {
  margin-top: 46px;
  margin-bottom: 20px;
  justify-content: flex-end;
  gap: 0.5rem;
}

.custom-base-upload {
  width: 100%;
}

.upload-area {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 128px;
  background: #fafafa;
}

.upload-area__icon {
  display: inline-block;
  width: 34px;
  height: 42px;
  background-color: var(--grey-dark);
  mask-image: url("/images/icon/upload.svg");
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/upload.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
}

.upload-item__loading {
  display: inline-block;
  width: 34px;
  height: 42px;
  background-color: var(--grey-dark);
  mask-image: url("/images/icon/upload.svg");
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/upload.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
}

.upload-area__description {
  color: var(--grey-dark);
  font-size: 14.66px;
}

.upload-area__description .highlight {
  color: var(--blue);
}

.upload-area__extension {
  position: absolute;
  left: 0.5rem;
  bottom: 0.25rem;
  color: var(--grey-3);
  font-size: 11px;
}

.list-upload {
  height: 80px;
  margin-top: 1rem;
  overflow-y: auto;
}

.upload-item {
  position: relative;
  display: flex;
  width: 100%;
  max-width: 485px;
  padding-top: 13px;
  padding-bottom: 13px;
  padding-left: 26px;
  padding-right: 26px;
  margin: auto;
  gap: 1rem;
  border-bottom: 1px solid rgb(214, 218, 222);
  cursor: pointer;
}

.upload-item.available {
  cursor: pointer;
}

.upload-item:last-child {
  border-bottom: none;
}

.upload-item__icon {
  display: inline-block;
  width: 34px;
  height: 42px;
  background-color: var(--grey-dark);
  mask-image: url("/images/icon/single-doc.svg");
  mask-position: center;
  mask-size: contain;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/single-doc.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: contain;
  -webkit-mask-repeat: no-repeat;
}

.upload-item__description {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.upload-item__remove {
  position: absolute;
  right: 26px;
  top: 13px;
  display: inline-block;
  width: 16px;
  height: 16px;
  background-color: var(--grey-dark);
  mask-image: url("/images/icon/remove.svg");
  mask-position: center;
  mask-size: 8px 8px;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/remove.svg");
  -webkit-mask-position: center;
  -webkit-mask-size: 8px 8px;
  -webkit-mask-repeat: no-repeat;
  cursor: pointer;
}

.description-file-name {
  font-weight: 500;
  font-size: 14.66px;
  line-height: 18px;
  color: var(--grey-dark);
}
.description-file-name:hover {
  text-decoration: underline;
}
.description-file-size {
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: var(--grey-8);
}
</style>
