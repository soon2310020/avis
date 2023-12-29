<template>
  <base-dialog
    :title="resources['relocation_approval']"
    :visible="visible"
    dialog-classes="modal-lg"
    body-classes="custom-modal__body"
    style="z-index: 5001"
    @close="handleClose"
  >
    <div>
      <div style="padding: 0 31px; min-height: 520px; max-height: 600px">
        <div class="position-relative">
          <label for="" class="text-bold">{{
            resources["confirmation_status"]
          }}</label>
          <div>
            <base-button type="dropdown" @click="toggleConfirmStatus">
              {{ selectedStatus.title }}
            </base-button>
            <common-popover
              @close="closeConfirmStatus"
              :is-visible="visibleConfirmStatus"
              :style="{ marginTop: '4px', top: '100%' }"
            >
              <common-select
                :style="{ position: 'static', width: '100%', padding: '0 4px' }"
                :items="confirmStatus"
                :multiple="false"
                :has-toggled-all="false"
                @close="closeConfirmStatus"
                @on-select="handleSelectStatus"
              >
              </common-select>
            </common-popover>
          </div>
        </div>
        <div
          style="
            margin-top: 20px;
            overflow: auto;
            height: 520px;
            padding-right: 10px;
          "
        >
          <div>
            <div
              v-for="(singleNote, index) in loadedNote"
              :key="index"
              style="margin-bottom: 16px"
            >
              <view-note-card
                ref="view-note-card"
                :resources="resources"
                :note="singleNote"
                :users="users"
                :current-user="currentUser"
                @change-message="handleChangeNoteContent"
                @cancel-note="handleCancelNote"
                :system-note-function="systemNoteFunction"
                @reload-note="onLoadNote"
              >
              </view-note-card>
            </div>
          </div>
          <div>
            <add-note-card
              ref="add-note-card"
              :resources="resources"
              :users="users"
              :user="currentUser"
              @change-message="handleChangeNoteContent"
              @cancel-note="handleCancelNote"
              @on-add-note="handleAddSingleNote"
              :system-note-function="systemNoteFunction"
            >
            </add-note-card>
          </div>
        </div>
      </div>
      <div
        class="d-flex modal__footer"
        style="margin-top: 80px; justify-content: flex-end"
      >
        <base-button
          level="primary"
          type="save"
          :disabled="!enabled"
          @click="handleSubmit"
        >
          {{ resources["save"] }}
        </base-button>
      </div>
    </div>
  </base-dialog>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    visible: Boolean,
    item: Object,
  },
  components: {
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "add-note-card": httpVueLoader("/components/@base/notes/add-note-card.vue"),
    "view-note-card": httpVueLoader(
      "/components/@base/notes/view-note-card.vue"
    ),
  },
  data() {
    return {
      confirmStatus: [
        { key: "APPROVED", title: "Approved", checked: false },
        { key: "UNAPPROVED", title: "Disapproved", checked: false },
      ],
      selectedStatus: {
        key: "PENDING",
        title: "Select Relocation Status",
        checked: true,
      },
      visibleConfirmStatus: false,
      systemNoteFunction: "CHANGE_RELOCATION_STATUS",
      users: [],
      currentUser: {},
      note: "",
      loadedNote: [],
      companyCategories: [],
    };
  },
  computed: {
    enabled() {
      return this.selectedStatus.key !== "PENDING";
    },
  },
  watch: {
    async visible(newVal) {
      if (newVal) {
        console.log("open", this.item);
        const type = this.item.moldLocationStatus;
        this.confirmStatus = this.confirmStatus.map((item) => {
          const newItem = { ...item };
          if (newItem.key === type) {
            newItem.checked = true;
            this.selectedStatus = newItem;
          } else {
            newItem.checked = false;
          }
          return newItem;
        });
        await this.onLoadNote();
      }
    },
  },
  async created() {
    this.getListUser();
    await this.getCurrentUser();
  },
  methods: {
    handleClose() {
      this.$emit("close");
      this.clearData();
    },
    clearData() {
      this.clearNote();
      this.selectedStatus = {
        key: "PENDING",
        title: "Select Relocation Status",
        checked: true,
      };
      this.confirmStatus = this.confirmStatus.map((item) => ({
        ...item,
        checked: false,
      }));
    },
    toggleConfirmStatus() {
      this.visibleConfirmStatus = !this.visibleConfirmStatus;
    },
    closeConfirmStatus() {
      this.visibleConfirmStatus = false;
    },
    handleSelectStatus(item) {
      if (this.selectedStatus.key !== item.key) {
        this.selectedStatus = item;
        this.closeConfirmStatus();
      }
    },
    handleCancelNote() {
      console.log("handleCancelNote");
    },
    handleAddNote(note) {
      console.log("handleAddNote", note);
    },
    async getCurrentUser() {
      try {
        const me = await Common.getSystem("me");
        this.currentUser = JSON.parse(me);
      } catch (error) {
        console.log(error);
      }
    },
    getListUser() {
      Common.getLiteListUser().then((users) => {
        this.users = users;
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
      });
    },
    async onLoadNote() {
      try {
        const response = await axios.get(
          `/api/system-note/list?objectFunctionId=${this.item.id}&systemNoteFunction=CHANGE_RELOCATION_STATUS&trashBin=false`
        );
        console.log("onLoadNote", response);
        this.loadedNote = response.data.dataList;
      } catch (error) {
        console.log(error);
      }
    },
    handleChangeNoteContent(content) {
      console.log("handleChangeNoteContent", content);
      this.note = content;
      this.note.objectFunctionId = this.item.id;
      this.note.systemNoteFunction = "CHANGE_RELOCATION_STATUS";
      this.note.objectType = "LOCATION";
    },
    clearNote() {
      const el = this.$refs["add-note-card"];
      if (el) {
        el.clearNote();
      }
    },
    async handleAddSingleNote() {
      try {
        this.note.objectFunctionId = this.item.id;
        const payload = { ...this.note };
        const res = await axios.post(
          `/api/system-note/create-system-note`,
          payload
        );
        console.log("handleAddSingleNote", res);
        this.clearNote();
        this.onLoadNote();
      } catch (error) {
        console.log(error);
      }
    },
    async handleSubmit() {
      try {
        console.log("this.item", this.selectedStatus.key, this.item);
        this.note.objectFunctionId = this.item.id;
        const payload = {
          moldPayloadList: [
            {
              id: this.item.id,
              moldLocationStatus: this.selectedStatus.key,
            },
          ],
          systemNotePayload: { ...this.note },
        };

        const response = await axios.put(
          `/api/molds/locations/changeStatus`,
          payload
        );
        console.log(response);
        this.handleClose();
        this.$emit("reload");
      } catch (error) {
        console.log(error);
      }
    },
  },
};
</script>
<style scoped>
.custom-modal__body {
  padding: 20px 25px !important;
}
</style>
