<template>
  <div class="assign-tooling">
    <div
      style="overflow: auto;"
      id="op-assign-tooling"
      class="modal fade"
      role="dialog"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true">
      <div class="modal-dialog assign-dialog" role="document">
        <div class="modal-content">
          <div class="assign-header">
            Assigned Tooling
          </div>
          <div class="assign-content">
            <div class="assign-item" v-for="(parent, index) in parents" :key="index">
              <div class="tooling-item">
                <div class="tooling-title">Tooling ID</div>
                <div class="tooling-select">
                  <assign-tooling-filter :resources="resources" :select-tooling="selectTooling" :parent="parent" :option-array-tooling="toolingByParent(parent)" :init-tooling-ids="toolingByParentId[parent.id]"></assign-tooling-filter>
                </div>
              </div>
              <div class="company-item">
                <div class="company-title">
                  Company
                </div>
                <div class="company-value">
                  {{ parent.company.name }}
                </div>
              </div>
            </div>
          </div>
          <div class="assign-footer text-right">
            <a href="javascript:void(0)" class="btn btn-primary" @click.prevent="saveAssignTooling">Save</a>
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

  module.exports = {
    props: {
      resources: Object,
      hierarchyData: Array
    },
    components: {
      'assign-tooling-filter': httpVueLoader('/components/access-group/assign-tooling-filter.vue'),
    },
    data() {
      return {
        hierarchyItem: {},
        toolingOptions: [],
        parents: [],
        toolingByParentId: {}
      }
    },
    methods: {
      openAssignedToolingModal(hierarchyItem) {
        this.hierarchyItem = hierarchyItem;
        this.parents = this.hierarchyData.filter(item => this.hierarchyItem.parentId.includes(item.id));
        this.loadCompanyTooling();
        $('#op-assign-tooling').modal('show');
      },
      selectTooling(parentId, toolingIds) {
        this.toolingByParentId[parentId] = toolingIds;
        this.$forceUpdate();
      },
      toolingByParent(parent) {
        let otherParentToolingId = [];
        Object.keys(this.toolingByParentId).forEach(parentId => {
          if (parseInt(parentId) !== parseInt(parent.id)) {
            this.toolingByParentId[parentId].forEach((toolingId => {
              otherParentToolingId.push(toolingId);
            }));
          }
        });
        console.log('otherParentToolingId', otherParentToolingId);
        console.log('this.toolingOptions.filter(tooling => !otherParentToolingId.includes(tooling.id))', this.toolingOptions.filter(tooling => !otherParentToolingId.includes(tooling.id)));
        return this.toolingOptions.filter(tooling => !otherParentToolingId.includes(tooling.id));
      },
      loadAssignTooling() {
        if(this.hierarchyItem && this.hierarchyItem.accessCompanyParentRelations){
          this.hierarchyItem.accessCompanyParentRelations.forEach(item =>{

        axios.get(`/api/access-hierarchy/access-mold?accessCompanyRelationId=${item.id}`).then(response => {
          if (response.data && response.data.content) {
            this.toolingByParentId = {};
            response.data.content.forEach(item => {
              let parent = this.hierarchyData.filter(element => element.companyId === item.companyId)[0];
              if (!this.toolingByParentId[parent.id]) {
                this.toolingByParentId[parent.id] = [];
              }
              this.toolingByParentId[parent.id].push(item.moldId);
            });

          }
        });
          });

        }
      },
      saveAssignTooling() {
        let toolingIds = Object.keys(this.toolingByParentId).map(field => this.toolingByParentId[field]);
        if (toolingIds.length < this.hierarchyItem.parentId.length) {
        }
        let relationList = [];
        Object.keys(this.toolingByParentId).forEach(parentId => {
          let parent = this.hierarchyData.filter(item => item.id === parseInt(parentId))[0];
          relationList.push({
            companyId: parent.companyId,
            moldIdList: this.toolingByParentId[parentId]
          });
        });
        let payload = {
          accessMoldPayloadList: relationList
        };
        axios.post(`/api/access-hierarchy/assigned-tooling/${this.hierarchyItem.id}`, payload).then(response => {
          $('#op-assign-tooling').modal('hide');
        });
      },
      loadCompanyTooling() {
        axios.get(`/api/access-hierarchy/molds/${this.hierarchyItem.companyId}`).then(response => {
          if (response.data && response.data.content) {
            this.toolingOptions = response.data.content;
            this.loadAssignTooling();
          }
        });
      }

    },
    mounted() {
    },
    computed: {
    },
    watch: {
    }
  }
</script>

<style scoped>
  .btn-success {
    padding: 0.25rem 0.5rem;
  }
</style>