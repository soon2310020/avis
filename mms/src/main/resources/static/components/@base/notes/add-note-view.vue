<template>
  <div>
    <a
      href="javascript:void(0)"
      class="back-button"
      @click="closeNoteFullView()"
    >
      <span class="icon back-arrow"></span>
      <span class="back" @click="$emit('back')">{{
        resources["back_to_all"]
      }}</span>
    </a>
    <add-note-card
      :resources="resources"
      :user="currentUser"
      :users="users"
      @cancel-note="$emit('back')"
      @on-add-note="$emit('reload-note')"
      :object-function="objectFunction"
      :system-note-function="systemNoteFunction"
      :handle-submit="handleSubmit"
    >
    </add-note-card>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    currentUser: Object,
    objectFunction: Object,
    systemNoteFunction: String,
    users: Array,
    handleSubmit: Function,
  },
  data() {
    return {
      deleteNoteAlert: false,
    };
  },
  components: {
    "add-note-card": httpVueLoader("/components/@base/notes/add-note-card.vue"),
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
    formatDate(timeStamp) {
      const date = new Date(timeStamp * 1000);
      return moment(date).format("YYYY-MM-DD");
    },
    closeNoteFullView() {
      this.$emit("close");
    },
  },
  mounted() {},
};
</script>

<style scoped>
.card-wrapper {
  height: 184px;
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
  margin-bottom: -15px;
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
