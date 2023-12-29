<template>
  <div class="tooling-filter" v-show="visible">
    <div class="input-wrapper">
      <a-input style="margin-bottom: 10px;" placeholder="Input search text" v-model.trim="searchText">
        <a-icon slot="prefix" type="search"/>
      </a-input>
    </div>
    <div style="max-height: 250px; overflow-y: auto;" class="content-body">
      <div v-for="item in toolingFiltered" class="user-item">
        <a-col :key="item.id" @click="selectedTooling(item.id)" >
          <p-radio  v-model="checkedToolingId" :value="item.id">{{item.name}}</p-radio>
        </a-col>
      </div>
    </div>
  </div>
</template>

<script>
  module.exports = {
    props: {
    },
    data() {
      return {
        checkedListTooling: [],
        searchText: "",
        visible: false,
        checkedToolingId: null,
        toolings: []
      };
    },
    methods: {
      showToolingFilterPopup(){
        this.visible = true;
      },
      getListTooling(){
        axios.get('/api/molds/maintenance/molds-list').then(response => {
          console.log('response.data', response.data);
          if (response.data) {
            this.toolings = response.data;
          }
        });
      },
      hideToolingFilterPopup(){
        this.visible = false;
      },
      selectedTooling(value) {
        this.visible = false;
        let resList = this.toolings.filter(u => u.id === value);

        let checkedTooling = resList != null && resList.length > 0 ? resList[0] : null;
        if (checkedTooling) {
          this.checkedToolingId = checkedTooling.id;
          this.$emit('tooling', checkedTooling);
        }
      },
    },
    watch: {
    },
    computed: {
      toolingFiltered: function () {
        if (!this.searchText) {
          return this.toolings;
        }
        return this.toolings.filter(item => item.name.toUpperCase().includes(this.searchText.toUpperCase()));
      }
    },
    mounted() {
      this.getListTooling();
    }
  };
</script>

<style scoped>
  .badge-require {
    display: none;
  }
</style>
