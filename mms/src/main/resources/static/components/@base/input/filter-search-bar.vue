<template>
  <div class="form-row">
    <div style="width: 100%">
      <div class="input-group">
        <div class="input-group-prepend">
          <div class="input-group-text">
            <div class="icon-search--small"></div>
          </div>
        </div>
        <input
          type="text"
          class="form-control"
          :value="searchText"
          @input="handleInput"
          :placeholder="placeholder"
        />
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    placeholder: {
      type: String,
      default: "",
    },
    value: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      searchText: "",
      timeout: null,
    };
  },
  watch: {
    value: {
      handler(newVal) {
        this.searchText = newVal;
      },
      immediate: true,
    },
  },
  methods: {
    // handleInput: _.debounce(function (event) {
    //   this.searchText = event.target.value;
    //   this.$emit("on-change", this.searchText);
    // }, 500),
    handleInput(event) {
      this.searchText = event.target.value;
      clearTimeout(this.timeout);
      this.timeout = setTimeout(() => {
        this.$emit("on-change", this.searchText);
      }, 500);
    },
  },
};
</script>

<style></style>
