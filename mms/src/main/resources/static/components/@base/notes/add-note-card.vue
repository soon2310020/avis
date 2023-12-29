<template>
  <div class="card-wrapper" style="height: auto">
    <div class="header-wrapper">
      <div>{{ formatDate() }}</div>
    </div>
    <div class="profile-info-wrapper">
      <img
        width="24px"
        height="24px"
        src="/images/icon/uploadImage.svg"
        style="border-radius: 50%"
      />
      <div class="creator-name">
        <span style="margin-right: 4px">{{ user?.name }}</span>
        <span>({{ user?.company?.name }})</span>
      </div>
    </div>
    <add-note-content
      ref="add-note-content"
      :users="users"
      :parent-id="parentId"
      :resources="resources"
      style="margin-left: 32px"
      @on-cancel="onCancelNote"
      @on-add-note="(content) => $emit('on-add-note', content)"
      :object-function="objectFunction"
      :system-note-function="systemNoteFunction"
      @change-message="handleChangeContent"
      :handle-submit="handleSubmit"
    >
    </add-note-content>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    user: Object,
    objectFunction: Object,
    systemNoteFunction: String,
    parentId: Number,
    users: Array,
    handleSubmit: Function,
  },
  data() {
    return {};
  },
  components: {
    "add-note-content": httpVueLoader(
      "/components/@base/notes/add-note-content.vue"
    ),
  },
  computed: {},
  methods: {
    truncateText(text, size) {
      if (text) {
        const trimText = text.trim();
        if (trimText) {
          if (trimText.length > size) {
            return text.slice(0, size) + "...";
          }
          return trimText;
        }
      }
      return "";
    },
    formatDate() {
      return moment().format("YYYY-MM-DD");
    },
    onCancelNote() {
      this.$emit("cancel-note");
    },
    handleChangeContent(content) {
      console.log("handleChangeContent", content);
      this.$emit("change-message", content);
    },
    clearNote() {
      const el = this.$refs["add-note-content"];
      if (el) {
        el.clearNote();
      }
    },
  },
  mounted() {},
};
</script>

<style scoped>
.card-wrapper {
  min-height: 184px;
  background: #ffffff;
  border: 0.5px solid #8b8b8b;
  border-radius: 3px;
  position: relative;
}
.header-wrapper {
  background: #f5f8ff;
  border-bottom: 0.5px solid #8b8b8b;
  border-radius: 3px 3px 0px 0px;
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  padding: 4px 8px;
  justify-content: space-between;
  height: 33px;
}
.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  height: 25px;
  width: 25px;
}
.action-btn {
  background: #d9d9d9;
  border-color: transparent;
}
.action-btn:focus {
  background: #daeeff;
}
.action-btn:hover img {
  filter: sepia(206%) hue-rotate(163deg) saturate(900%);
}
.action-btn:hover {
  background: #daeeff;
}
.creator-name {
  font-weight: 600;
  font-size: 14.66px;
  line-height: 18px;
  display: flex;
  align-items: center;
  color: #4b4b4b;
  margin-left: 8px;
}
.profile-info-wrapper {
  padding: 8px 16px;
  display: flex;
  margin-bottom: 9px;
}
.back-button {
  display: flex;
  align-items: center;
  margin-bottom: 29.73px;
}
.back {
  margin-left: 12.5px;
}
</style>
