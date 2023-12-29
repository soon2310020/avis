<template>
  <div>
    <a
      href="javascript:void(0)"
      class="back-button"
      @click="closeNoteFullView()"
    >
      <span class="icon back-arrow"></span>
      <span class="back">{{
        note?.deleted ? resources["back_to_deleted"] : resources["back_to_all"]
      }}</span>
    </a>
    <div class="card-wrapper">
      <div class="header-wrapper">
        <div>{{ formatDate(note?.createdAt) }}</div>
        <a
          v-if="shouldShowDelete && !note?.deleted"
          href="javascript:void(0)"
          class="btn-custom btn-outline-custom-primary action-btn"
          @click="handleBeforeDelete"
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
        :message="note.message"
        :users="users"
        :system-note-param-list="note.systemNoteParamList"
        :disable-limit="true"
      ></view-note-content>
    </div>
    <div style="margin-left: 24px; margin-top: 8px; margin-bottom: 8px">
      <div
        v-if="note.replies && note.replies?.length > 0"
        style="margin-top: 8px; margin-bottom: 8px"
      >
        <div
          v-if="!showAllReplies"
          class="custom-hyper-link"
          @click="toggleShowAllReplies"
        >
          {{
            resources["view_all_replies"] + ` (${note.replies?.length || 0})`
          }}
          <img class="img-transition" src="/images/icon/icon-cta-blue.svg" />
        </div>
        <div
          v-else
          class="custom-hyper-link"
          style="margin-top: 8px; margin-bottom: 8px"
          @click="toggleShowAllReplies"
        >
          {{
            resources["collapse_all_replies"] +
            ` (${note.replies?.length || 0})`
          }}
          <img
            class="img-transition caret-show"
            src="/images/icon/icon-cta-blue.svg"
            alt=""
          />
        </div>
      </div>
      <div
        v-else
        class="cursor-none text--grey"
        style="margin-top: 8px; margin-bottom: 8px"
      >
        <span style="color: #8b8b8b">{{ resources["no_reply"] }}</span>
        <img class="img-transition" src="/images/icon/icon-cta-grey.svg" />
      </div>

      <div>
        <view-note-card
          v-show="showAllReplies"
          v-for="reply in getReadedReplies"
          :key="reply.id"
          :note="reply"
          :resources="resources"
          :current-user="currentUser"
          is-dont-show-warning-again="isDontShowWarningAgain"
          :users="users"
          :hide-delete-button="true"
          :can-delete="false"
          style="margin-bottom: 8px; min-height: fit-content"
          :card-wrapper-style="{ height: 'auto' }"
          @reload-note="$emit('reload-note')"
        ></view-note-card>
      </div>
      <div v-show="showAllReplies && getUnreadedReplies.length > 0">
        <div class="unread-line">
          <span>{{ resources["unread"] }}</span>
        </div>
      </div>
      <div>
        <view-note-card
          v-show="showAllReplies"
          v-for="reply in getUnreadedReplies"
          :key="reply.id"
          :note="reply"
          :resources="resources"
          :current-user="currentUser"
          is-dont-show-warning-again="isDontShowWarningAgain"
          :users="users"
          :hide-delete-button="true"
          style="margin-bottom: 8px; min-height: fit-content"
          :card-wrapper-style="{ height: 'auto' }"
          :can-delete="false"
          @reload-note="$emit('reload-note')"
        ></view-note-card>
      </div>

      <div v-if="!note?.deleted" style="padding-bottom: 67px">
        <add-note-card
          :resources="resources"
          :user="currentUser"
          :users="users"
          :parent-id="note.id"
          @cancel-note="() => undefined"
          @on-add-note="$emit('reload-note')"
          :object-function="objectFunction"
          :system-note-function="systemNoteFunction"
          style="margin-bottom: 67px"
        >
        </add-note-card>
      </div>
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
    objectFunction: Object,
    systemNoteFunction: String,
    users: Array,
  },
  data() {
    return {
      deleteNoteAlert: false,
      showAllReplies: true,
    };
  },
  components: {
    "warning-delete-note": httpVueLoader(
      "/components/@base/notes/warning-delete-note.vue"
    ),
    "view-note-content": httpVueLoader(
      "/components/@base/notes/view-note-content.vue"
    ),
    "view-note-card": httpVueLoader(
      "/components/@base/notes/view-note-card.vue"
    ),
    "add-note-card": httpVueLoader("/components/@base/notes/add-note-card.vue"),
  },
  computed: {
    shouldShowDelete() {
      return this.currentUser.id === this.note?.creator?.id;
    },
    getReadedReplies() {
      if (this.note?.replies) {
        return this.note.replies.filter((item) => item.read);
      }
      return [];
    },
    getUnreadedReplies() {
      if (this.note?.replies) {
        return this.note.replies.filter((item) => !item.read);
      }
      return [];
    },
  },
  methods: {
    toggleShowAllReplies() {
      this.showAllReplies = !this.showAllReplies;
      console.log("toggleShowAllReplies", this.showAllReplies);
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
    closeNoteFullView(from) {
      if (from) {
        this.$emit("close", from);
      } else {
        const directFrom = this.note.deleted ? "DELETED" : "ALL";
        this.$emit("close", directFrom);
      }
    },
    deleteNote() {
      const from = this.note.deleted ? "DELETED" : "ALL";
      axios
        .delete(`/api/system-note/${this.note.id}`)
        .then(() => {
          this.$emit("reload-note");
          this.closeNoteFullView(from);
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
.unread-line {
  position: relative;
  z-index: 1;
  margin: 8px 0;
  display: flex;
  justify-content: flex-end;
}
.unread-line:before {
  border-top: 1px solid #c90065;
  content: "";
  margin: 0 auto; /* this centers the line to the full width specified */
  position: absolute; /* positioning must be absolute here, and relative positioning must be applied to the parent */
  top: 50%;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
}
.unread-line span {
  background: #fff;
  padding: 0 4px;
  color: #c90065;
  margin-right: 24px;
}
</style>
