<template>
  <div>
    <div class="card-wrapper" :style="cardWrapperStyle">
      <div class="header-wrapper">
        <div>{{ formatDate(note?.createdAt) }}</div>
        <a
          v-if="!note?.deleted && canDelete"
          @click="handleBeforeDelete"
          href="javascript:void(0)"
          class="btn-custom btn-outline-custom-primary action-btn"
          v-if="shouldShowDelete && !hideDeleteButton"
        >
          <img
            width="11"
            height="11"
            class="image-duplicate"
            src="/images/new-feature/delete.svg"
          />
        </a>
      </div>
      <div class="profile-info-wrapper">
        <img
          width="24px"
          height="24px"
          src="/images/icon/uploadImage.svg"
          style="border-radius: 50%"
        />
        <div class="creator-name">
          <span style="margin-right: 4px">{{
            note?.creator?.displayName
          }}</span>
          <span>({{ note?.companyName }})</span>
        </div>
      </div>
      <view-note-content
        :users="users"
        :message="note.message"
        :system-note-param-list="note.systemNoteParamList"
        :disable-limit="true"
      ></view-note-content>
    </div>
    <warning-delete-note
      :is-show-dont-warning-again="isDontShowWarningAgain"
      :resources="resources"
      :is-showing="deleteNoteAlert"
      @set-is-showing="(e) => (deleteNoteAlert = e)"
      @delete-note="deleteNote"
    ></warning-delete-note>
  </div>
</template>

<script>
module.exports = {
  props: {
    note: Object,
    resources: Object,
    currentUser: Object,
    isDontShowWarningAgain: Boolean,
    cardWrapperStyle: Object,
    hideDeleteButton: Boolean,
    users: Array,
    canDelete: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      deleteNoteAlert: false,
      showAllReplies: false,
    };
  },
  components: {
    "warning-delete-note": httpVueLoader(
      "/components/@base/notes/warning-delete-note.vue"
    ),
    "view-note-content": httpVueLoader(
      "/components/@base/notes/view-note-content.vue"
    ),
  },
  computed: {
    shouldShowDelete() {
      return (
        this.currentUser.id === this.note?.creator?.id && !this.note?.deleted
      );
    },
  },
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
    deleteNote() {
      axios
        .delete(`/api/system-note/${this.note.id}`)
        .then(() => {
          this.$emit("reload-note");
          this.checkIsShowWarningAgain();
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    handleBeforeDelete() {
      if (this.isDontShowWarningAgain) {
        this.deleteNote();
      } else {
        setTimeout(() => {
          this.deleteNoteAlert = true;
        }, 200);
      }
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
