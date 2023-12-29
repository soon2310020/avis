<template>
  <div>
    <a-badge
      :count="getUnreadNumber"
      :overflow-count="99"
      :number-style="{ backgroundColor: '#C90065' }"
      style="width: 100%"
    >
      <div class="preview-card-wrapper">
        <div class="header-wrapper" @click="showObject">
          <a-tooltip placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px; font-size: 13px">
                <div>
                  <b>{{ note.creator.displayName }}</b>
                </div>
                <div>
                  Company:
                  <span v-if="note.companyName">{{ note.companyName }}</span>
                </div>
                <div>Department: {{ note?.creator?.department }}</div>
                <div style="margin-bottom: 15px">
                  Position: {{ note?.creator.position }}
                </div>
                <div>Email: {{ note?.creator?.email }}</div>
                <div>Phone: {{ note?.creator?.mobileNumber }}</div>
              </div>
            </template>
            <div style="cursor: pointer">
              {{ note?.creator?.displayName }}
              <span>({{ note?.companyName }})</span>
            </div>
          </a-tooltip>

          <div>{{ formatDate(note?.createdAt) }}</div>
        </div>
        <view-note-content
          :users="users"
          :message="note.message"
          :system-note-param-list="note.systemNoteParamList"
        ></view-note-content>
        <div>
          <a
            v-if="shouldShowDelete"
            @click="handleBeforeDelete"
            href="javascript:void(0)"
            class="btn-custom btn-outline-custom-primary action-btn"
          >
            <img
              width="11"
              height="11"
              class="image-duplicate"
              src="/images/new-feature/delete.svg"
            />
          </a>
          <a
            href="javascript:void(0)"
            class="custom-hyper-link"
            style="position: absolute; right: 8px; bottom: 8px"
            @click="onClickViewNote"
            >{{ viewNoteTxt }}</a
          >
        </div>
      </div>
    </a-badge>
    <warning-delete-note
      :is-dont-show-warning-again="isDontShowWarningAgain"
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
    users: Array,
    hideUnreadCount: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      deleteNoteAlert: false,
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
    viewNoteTxt() {
      if (this.note?.replies?.length > 0) {
        return (
          this.note?.replies?.length +
          " " +
          (this.note?.replies?.length > 1 ? "Replies" : "Reply")
        );
      }
      return this.resources["view_note"];
    },
    getUnreadNumber() {
      if (this.hideUnreadCount) {
        return 0;
      }
      if (this.note) {
        return this.note.numUnread;
      }
      return 0;
    },
  },
  methods: {
    showObject() {
      console.log(this.note);
    },
    onClickViewNote() {
      this.$emit("view-note", this.note);
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
  },
  mounted() {},
};
</script>

<style scoped>
.preview-card-wrapper {
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
  padding: 9.5px 8px;
  justify-content: space-between;
  height: 33px;
}
.note-body-wrapper {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  display: flex;
  color: #4b4b4b;
  padding: 16px;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
}
.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  height: 25px;
  width: 25px;
  position: absolute;
  bottom: 8px;
  left: 8px;
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
</style>
