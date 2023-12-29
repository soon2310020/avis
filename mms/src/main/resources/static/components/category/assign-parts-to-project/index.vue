<template>
  <base-dialog
    :visible="isVisible"
    :title="modalTitle"
    @close="$emit('close')"
    dialog-classes="modal-lg"
    body-classes="body-class-appended"
    title-classes="title-class-appended"
  >
    <div class="modal-body__title">
      <div class="form-item">
        <!-- TODO RECHECK -->
        <span class="form-item__label">Brand</span>
        <span v-if="categoryDisplay.length <= 20" class="form-item__value">{{ categoryDisplay }}</span>
        <a-tooltip v-else placement="bottomLeft">
          <template slot="title">
            <div class="tooltip-assign-table">{{ categoryDisplay }}</div>
          </template>
          <span class="form-item__value">{{ truncateText(categoryDisplay, 20) }}</span>
        </a-tooltip>
      </div>
      <div class="form-item">
        <span class="form-item__label">Product</span>
        <span v-if="projectDisplay.length <= 20" class="form-item__value">{{ projectDisplay }}</span>
        <a-tooltip v-else placement="bottomLeft">
          <template slot="title">
            <div class="tooltip-assign-table">{{ projectDisplay }}</div>
          </template>
          <span class="form-item__value">{{ truncateText(projectDisplay, 20) }}</span>
        </a-tooltip>
      </div>
    </div>
    <div class="modal-body__list">
      <assign-table
        :is-loading="isLoading"
        :project-name="projectDisplay"
        :list-parts="listParts"
        :pagination="pagination"
        :list-unassigned-parts="listUnassignedParts"
        :on-remove-part="handleRemovePart"
        :on-add-part="handleAddPart"
        :on-fetch-list-parts-by-project="handleFetchListPartsByProject"
      ></assign-table>
    </div>
    <div class="modal-body__action justify-content-end">
      <a-button class="btn-submit" :loading="isSaving" @click="handleSubmit">
        {{ resources["save_project_changes"] }}
      </a-button>
    </div>
  </base-dialog>
</template>

<script>
const mappingParts = (item) => ({
  id: item.id,
  categoryId: item.categoryId,
  quantityRequired: !item.quantityRequired ? 1 : item.quantityRequired,
  name: item.name,
  partCode: item.partCode,
});

module.exports = {
  name: "AssignPartsToProject",
  components: {
    AssignTable: httpVueLoader(
      "/components/category/assign-parts-to-project/assign-table.vue"
    ),
  },
  props: {
    resources: Object,
    isVisible: {
      type: Boolean,
      default: false,
    },
    selectedProject: {
      type: Object,
      default: null,
    },
    onRefetch: {
      type: Function,
      default: () => { },
    },
  },
  data() {
    return {
      modalRef: null,
      categoryDisplay: "",
      projectDisplay: "",
      listParts: [],
      listAddParts: [],
      listRemovedParts: [],
      listUnassignedPartsTemp: [],
      listUnassignedParts: [],
      pagination: {
        page: 1,
        size: 20,
        totalPages: 1,
      },
      isLoading: false,
      isSaving: false,
    };
  },
  computed: {
    totalRecords() {
      return this.listParts.length;
    },
  },
  methods: {
    truncateText(text, length){
      if (text.length > length) {
        return text.substring(0, length) + '...';
      } else {
        return text
      }
    },
    // api
    async fetchListPartsByProject(query) {
      this.isLoading = true;
      try {
        const res = await axios.get(
          `/api/categories/part-by-project?projectId=${query.projectId}&page=${query.page}&size=${query.size}`
        );
        return res.data.data;
      } catch (error) {
        this.handleError(error);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchUnassignedParts() {
      // fixed params to get all
      const query = { searchString: "", page: 0, size: 1000 };
      try {
        const res = await axios.get(
          `/api/parts?accessType=ADMIN_MENU&categoryId&query=${query.searchString}&status=active&sort=id%2Cdesc&page=${query.page}&size=${query.size}&id&pageType=PART_SETTING&noProjectAssigned=true`
        );
        this.listUnassignedPartsTemp = res.data.content.map((item) => ({
          ...mappingParts(item),
          title: `${item.name} (${item.partCode})`,
        }));
        console.log("listUnassignedParts", this.listUnassignedPartsTemp);
        return res.data.data;
      } catch (error) {
        this.handleError(error);
      }
    },
    async updateProjectListParts() {
      this.isSaving = true;
      try {
        const payload = [
          ...this.listParts,
          ...this.listRemovedParts.map((item) => ({
            ...item,
            categoryId: null,
          })),
        ];
        const res = await axios.post("/api/parts/assign-to-project", payload);
        Common.alert("Success!");
        this.onRefetch(1);
      } catch (error) {
        this.handleError(error);
      } finally {
        this.isSaving = false;
      }
    },
    // handler
    handleRemovePart(removedItem) {
      console.log("removedItem");
      // exclude item
      this.listParts = this.listParts.filter(
        (item) => item.id !== removedItem.id
      );
      this.listAddParts = this.listAddParts.filter(
        item => item.id !== removedItem.id
      )
      // include item
      this.listRemovedParts.push({
        ...mappingParts(removedItem),
        title: `${removedItem.name} (${removedItem.partCode})`,
      });

      console.log("listRemoveParts", this.listRemovedParts);
      console.log("listUnassignedPartsTemp", this.listUnassignedPartsTemp);
      console.log("listUnassignedParts", this.listUnassignedParts);
    },
    handleAddPart(event) {
      const appendedItem = JSON.parse(event.target.value);

      const found =
        this.listParts.findIndex((item) => item.id === appendedItem.id) !== -1;
      if (!found)
        this.listParts.unshift(
          mappingParts({ ...appendedItem, categoryId: this.selectedProject.id })
        );
      this.listAddParts.push(mappingParts({ ...appendedItem, categoryId: this.selectedProject.id }))
      this.listUnassignedPartsTemp = this.listUnassignedPartsTemp.filter(
        (item) => item.id !== appendedItem.id
      );
      this.listRemovedParts = this.listRemovedParts.filter(
        (item) => item.id != appendedItem.id
      );
    },
    handleSubmit() {
      this.updateProjectListParts();
    },
    handleError(error) {
      console.log("%c error:", "background: red; color: white", error.message);
      Common.alert("Fail!");
    },
    async handleFetchListPartsByProject(query) {
      const result = await this.fetchListPartsByProject({
        projectId: this.selectedProject?.id,
        size: this.pagination.size,
        ...query,
      });
      if (!result) return;
      const _nextPage = result.content.map(mappingParts);
      this.listParts.push(..._nextPage);
      this.pagination.totalPages = result.totalPages;
    },
    // clean
    cleanEffect() {
      console.log('run clean')
      this.categoryDisplay = "";
      this.projectDisplay = "";
      this.listParts = [];
      this.listAddParts = [];
      this.listRemovedParts = [];
      this.listUnassignedParts = [];
      this.pagination.page = 1;
      this.pagination.totalPages = 1;
    },
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        console.log('this.selectedProject', this.selectedProject)
        this.projectDisplay = this.selectedProject.name ? this.selectedProject.name : '';
        this.categoryDisplay = this.selectedProject.parentName ? this.selectedProject.parentName : 'No Brand';
        this.handleFetchListPartsByProject({ page: 1 });
        this.fetchUnassignedParts()
      } else {
        this.cleanEffect();
      }
    },
    listRemovedParts(newVal) {
      const _arr = [...this.listUnassignedPartsTemp, ...newVal]
      this.listUnassignedParts = _arr.reduce((prev, curr) => {
        if (this.listAddParts.some(item => item.id === curr.id)) return [...prev]
        return [...prev, curr]
      }, [])
    },
    listUnassignedPartsTemp(newVal) {
      const _arr = [...newVal, ...this.listRemovedParts]
      this.listUnassignedParts = _arr.reduce((prev, curr) => {
        if (this.listAddParts.some(item => item.id === curr.id)) return [...prev]
        return [...prev, curr]
      }, [])
    },
  },
  created() {
    this.modalTitle = "Assign Parts to Product";
  },
  mounted() {
    this.modalRef = $("#op-assign-to-project");
  },
};
</script>

<style scoped src="/components/category/assign-parts-to-project/index.css"></style>
<style>
.tooltip-assign-table {
  padding: 6px 8px;
}
</style>