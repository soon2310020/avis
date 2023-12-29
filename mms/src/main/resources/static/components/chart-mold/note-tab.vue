<template>
  <div>
    <div
      class="d-flex justify-space-between align-center"
      style="margin-bottom: 20px"
    >
      <div style="font-size: 16px">
        <strong>{{ `${_.upperFirst(type)} ${resources["id"]} :` }}</strong>
        <span
          v-if="getCode && getCode.length > 20"
          v-bind:data-tooltip-text="getCode"
          style="width: auto; margin: 4px 104px 2px 15px; padding: 0"
        >
          {{ replaceLongtext(getCode, 20) }}</span
        >
        <span v-else>{{ getCode }}</span>
      </div>
      <div v-if="showNoteFullView" class="d-flex">
        <base-button
          v-if="selectNote?.deleted"
          @click="handleRestore"
          level="primary"
          >{{ resources["restore"] }}</base-button
        >
      </div>
      <div v-if="!showNoteFullView && !showAddNoteView" class="d-flex">
        <base-search
          v-model="searchText"
          placeholder="Enter keyword"
          style="margin-right: 8px"
          @input="handleSearch"
        ></base-search>
        <base-button level="primary" @click="showAddNoteView = true">{{
          resources["add_note"]
        }}</base-button>
      </div>
    </div>
    <div v-if="showNoteFullView">
      <note-full-view
        ref="note-full-view"
        :note="selectNote"
        :is-dont-show-warning-again="isDontShowWarningAgain"
        :resources="resources"
        @close="onCloseViewNote"
        @reload-note="getSystemNoteData"
        :current-user="currentUser"
        :object-function="objectFunction"
        :users="users"
        :system-note-function="systemNoteFunction"
      ></note-full-view>
    </div>
    <div v-else-if="showAddNoteView">
      <add-note-view
        :resources="resources"
        :current-user="currentUser"
        @back="handleCloseAddNote"
        @reload-note="getSystemNoteData"
        :object-function="objectFunction"
        :users="users"
        :system-note-function="systemNoteFunction"
        :handle-submit="handleSubmit"
      ></add-note-view>
    </div>
    <template v-else>
      <div v-show="isLoading" style="height: 500px" class="loading-wave"></div>
      <div v-show="!isLoading" style="height: 500px">
        <div style="margin-bottom: 24px; height: 30px">
          <div
            style="flex-wrap: wrap"
            class="btn-group btn-group-toggle float-left"
            data-toggle="buttons"
          >
            <label
              class="btn switch-btn"
              v-bind:class="{ active: noteType === 'ALL' }"
              @click.prevent="handleChangeNoteType('ALL')"
            >
              <input
                type="radio"
                name="options"
                value="ALL"
                autocomplete="off"
              />
              <span v-text="resources['all']"></span>
            </label>
            <label
              class="btn switch-btn"
              v-bind:class="{ active: noteType === 'DELETED' }"
              @click.prevent="handleChangeNoteType('DELETED')"
            >
              <input
                type="radio"
                name="options"
                value="DELETED"
                autocomplete="off"
              />
              <span v-text="resources['deleted']"></span>
            </label>
          </div>
        </div>
        <template v-if="this.searchedNotes.length > 0">
          <div>
            <div class="preview-notes-container">
              <preview-note-card
                v-for="(note, index) in getNotes"
                :ref="`preview-note-card-${index}`"
                :is-dont-show-warning-again="isDontShowWarningAgain"
                :key="index"
                :note="note"
                :resources="resources"
                @reload-note="getSystemNoteData"
                @view-note="onSelectViewNote"
                :users="users"
                :current-user="currentUser"
                :hide-unread-count="noteType === 'DELETED'"
              ></preview-note-card>
            </div>
            <div class="d-flex justify-end" style="margin-top: 40px">
              <base-paging
                :total-page="getTotalPage"
                :current-page="currentPage"
                @next="handleNextPage"
                @back="handleBackPage"
              ></base-paging>
            </div>
          </div>
        </template>
        <template v-else>
          <div
            v-if="this.notes.length === 0"
            class="d-flex justify-center align-center"
            style="height: 400px"
          >
            <div class="d-flex flex-column align-center" style="color: #8b8b8b">
              <img src="/images/icon/mold-note/memo-icon.svg" alt="" />
              <div style="margin: 20px 0">
                {{ resources["no_notes_yet"] }}
              </div>
              <div>
                {{
                  resources[
                    "click_add_note_to_leave_a_note_and_tag_people_to_notify_them"
                  ]
                }}.
              </div>
            </div>
          </div>
          <div
            v-else-if="this.searchedNotes.length === 0"
            class="d-flex justify-center align-center"
            style="height: 400px"
          >
            <div class="d-flex flex-column align-center" style="color: #8b8b8b">
              <img src="/images/icon/mold-note/search-icon.svg" alt="" />
              <div style="margin: 20px 0">
                {{ resources["no_matching_results"] }}
              </div>
              <div>
                {{
                  resources[
                    "try_another_keyword_or_click_All_to_go_back_to_default"
                  ]
                }}.
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script>
const PAGE_SIZE = 6;
module.exports = {
  props: {
    isShow: {
      type: Boolean,
      default: () => false,
    },
    resources: {
      type: Object,
      default: () => ({}),
    },
    currentUser: {
      type: Object,
      default: () => ({}),
    },
    objectFunction: {
      type: Object,
      default: () => ({}),
    },
    type: {
      type: String,
      default: () => "",
    },
    isDontShowWarningAgain: {
      type: Boolean,
      default: () => false,
    },
    notes: {
      type: Array,
      default: () => [],
    },
    totalNotes: {
      type: [String, Number],
      default: () => 1,
    },
    isLoading: {
      type: Boolean,
      default: () => false,
    },
    getSystemNoteData: Function,
    systemNoteFunction: {
      type: String,
      default: () => "",
    },
    handleSubmit: Function,
  },
  components: {
    "preview-note-card": httpVueLoader(
      "/components/@base/notes/preview-note-card.vue"
    ),
    "note-full-view": httpVueLoader(
      "/components/@base/notes/note-full-view.vue"
    ),
    "add-note-view": httpVueLoader("/components/@base/notes/add-note-view.vue"),
  },
  data() {
    return {
      showNoteFullView: false,
      selectNote: {},
      noteType: "ALL",
      showAddNoteView: false,
      currentPage: 1,
      focusPosition: 0,
      searchList: [],
      searchKeyword: "",
      searchCompleteKeyword: "",
      focusChangeWatcher: 0,
      searchText: "",
      completeText: "",
      searchedNotes: [],
      users: [],
      companyCategories: [],
    };
  },
  created() {
    this.captureQuery();
  },
  mounted() {
    this.getListUser();
  },
  watch: {
    isShow(newVal) {
      if (!newVal) {
        this.showNoteFullView = false;
        this.selectNote = {};
        this.noteType = "ALL";
        this.showAddNoteView = false;
        this.currentPage = 1;
      }
    },
    completeText(newVal) {
      if (newVal) {
        const cloneNotes = [...this.notes].map((item) => {
          const newItem = { ...item };
          newItem.message = newItem.message
            .replaceAll("<div>", "\n")
            .replaceAll("</div>", "");
          return newItem;
        });
        this.searchedNotes = cloneNotes.filter((item) =>
          item.message.includes(newVal)
        );
      } else {
        this.searchedNotes = [...this.notes];
      }
    },
    notes(newVal) {
      if (this.selectNote?.id) {
        this.selectNote = _.find(newVal, { id: this.selectNote?.id });
      }
      this.searchedNotes = [...newVal];
    },
  },
  computed: {
    getTotalPage() {
      const currentNotesNumber = this.searchedNotes.length;
      return Math.ceil(currentNotesNumber / PAGE_SIZE);
    },
    getNotes() {
      const firstIndex = (this.currentPage - 1) * PAGE_SIZE;
      return [...this.searchedNotes].splice(firstIndex, PAGE_SIZE);
    },
    getCode() {
      if (this.type === "TOOLING") {
        return this.objectFunction.equipmentCode;
      } else if (this.type === "PART") {
        return this.objectFunction.partCode;
      }
    },
  },
  methods: {
    onCloseViewNote(from) {
      console.log("onCloseViewNote", from);
      this.showNoteFullView = false;
      if (from === "DELETED") {
        this.handleChangeNoteType("DELETED");
      } else {
        this.handleChangeNoteType("ALL", true);
      }
      this.selectNode = {};
    },
    async onSelectViewNote(note) {
      this.selectNote = note;
      this.showNoteFullView = true;
      const ids = [...note.replies].map((item) => item.id);
      ids.push(note.id);
      await this.handleUpdateToRead(ids);
    },
    async handleUpdateToRead(ids) {
      try {
        const param = {
          ids: ids,
        };
        const res = await axios.post(`/api/system-note/read`, param);
        return res.data;
      } catch (error) {
        console.log(error);
      }
    },
    handleCloseAddNote() {
      this.showAddNoteView = false;
      this.handleChangeNoteType("ALL", true);
    },
    handleChangeNoteType(type, countTotal) {
      this.noteType = type;
      this.currentPage = 1;
      this.showAddNoteView = false;
      this.showNoteFullView = false;
      this.$emit("change-tab", type, countTotal);
    },
    handleNextPage() {
      if (this.currentPage < this.getTotalPage) {
        this.currentPage++;
      }
    },
    handleBackPage() {
      if (this.currentPage > 0) {
        this.currentPage--;
      }
    },

    changeShowAddNoteView(isShow) {
      this.showAddNoteView = isShow;
    },

    handleSearch() {
      console.log("handleSearch", this.searchText);
      this.completeText = this.searchText;
      this.currentPage = 1;
    },
    handleShowNoteFullView(note) {
      this.showNoteFullView = true;
      this.onSelectViewNote(note);
    },

    async handleRestore() {
      const id = this.selectNote.id;
      try {
        const res = await axios.post(`/api/system-note/restore/${id}`);
        this.showNoteFullView = false;
        this.selectNode = {};
        this.noteType = "ALL";
        this.getSystemNoteData();
      } catch (error) {
        console.log(error);
      }
    },
    toggleShowAllReplies() {
      const noteFullView = this.$refs["note-full-view"];
      console.log("toggleShowAllReplies 1", noteFullView);
      if (noteFullView) {
        noteFullView.toggleShowAllReplies();
      }
    },
    async getListUser() {
      const response = await headerVm?.getListUsers();
      console.log("getListUser>>>>>>>>>>>>>>>>>>>>>>>>>>", response);
      this.users = response;

      this.users.forEach((user) => {
        let companyCategory = this.companyCategories.filter(
          (item) => item.companyType === user.companyType
        )[0];
        if (!companyCategory) {
          companyCategory = {
            companyType: user.companyType,
            companyTypeText: user.companyTypeText,
          };
          this.companyCategories.push(companyCategory);
        }
        if (!companyCategory.companies) {
          companyCategory.companies = [];
        }
        let company = companyCategory.companies.filter(
          (item) => item.companyName === user.companyName
        )[0];
        if (!company) {
          company = {
            companyName: user.companyName,
            companyId: user.companyId,
          };
          companyCategory.companies.push(company);
        }
        if (!company.users) {
          company.users = [];
        }
        company.users.push(user);
      });
      this.companyCategories.sort((first, second) => {
        if (!first.companyTypeText || !second.companyTypeText) {
          return 1;
        }
        let firstName = first.companyTypeText.toUpperCase();
        let secondName = second.companyTypeText.toUpperCase();
        return firstName > secondName ? 1 : -1;
      });
    },

    async captureQuery() {
      const params = new URLSearchParams(location.href.split("?")[1]);
      const popup = params.get("popup");
      if (["NOTE_MENTIONED", "NOTE_REPLIED"].includes(popup)) {
        const noteId = params.get("noteId");
        const note = await this.findNoteById(noteId);
        if (note) {
          this.onSelectViewNote(note);
        }
      }
    },
    async findNoteById(id) {
      try {
        const res = await axios.get(`/api/system-note/${id}/detail`);
        return res.data;
      } catch (error) {
        console.log(error);
      }
      return {};
    },
  },
};
</script>

<style scoped>
.preview-notes-container {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-gap: 48px;
  height: 416px;
}
</style>
