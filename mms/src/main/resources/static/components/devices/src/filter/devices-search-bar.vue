<template>
  <div class="form-row">
    <div style="width: 100%">
      <div class="input-group">
        <div class="input-group-prepend">
          <div class="input-group-text"><i class="fa fa-search"></i></div>
        </div>
        <input
          type="text"
          class="form-control"
          v-model="searchText"
          @input="handleInput"
          :placeholder="resources['search_by_selected_column']"
        />
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
  },
  data() {
    return {
      searchText: "",
    };
  },
  methods: {
    handleInput: _.debounce(function (event) {
      this.searchText = event.target.value;
      this.$emit("on-change", this.searchText);
    }, 500),
  },
};
</script>

<style></style>
