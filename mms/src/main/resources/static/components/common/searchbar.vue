<template>
  <div class="input-group">
    <div class="input-group-prepend">
      <div class="input-group-text"><div class="icon-search--small"></div></div>
    </div>
    <input
      type="text"
      class="form-control"
      v-model="requestParam[queryField]"
      @input="handleInput"
      :placeholder="placeholder"
    />
  </div>
</template>

<script>
module.exports = {
  name: "CommonSearchbar",
  props: {
    requestParam: {
      type: Object,
    },
    onSearch: {
      type: Function,
      default: () => {},
    },
    placeholder: {
      type: String,
      default: "",
    },
    query: {
      type: String,
      default: "",
    },
  },
  setup(props) {
    const queryField = props.query ? props.query : "query";
    const handleInput = _.debounce((event) => {
      props.requestParam[queryField] = event.target.value;
      props.onSearch();
    }, 500);

    return {
      handleInput,
      queryField,
    };
  },
};
</script>
