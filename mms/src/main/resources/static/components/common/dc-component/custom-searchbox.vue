<template>
  <div>
    <div class="search-box">
      <div class="search-icon">
        <img src="/images/icon/icon-search.svg" alt="" />
      </div>
      <input
          class="search-input"
          type="text"
          v-model="requestParam[queryField]"
          @input="handleInput"
          :placeholder="placeholder"
      />
    </div>
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
    const queryField = computed(() => props.query || "query");
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
<style scoped>
.search-box {
  position: relative;
  background: #ffffff;
  width: 201px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.search-icon {
  position: absolute;
  height: 28.44px;
  width: 28.44px;
  left: 2px;
  display: flex;
  padding: 6px;
  align-items: center;
  justify-content: center;
  background: #f0f3f5;
  border-radius: 3px 0px 0px 3px;
}

.search-input {
  border: 0.5px solid #d6dade;
  border-radius: 3px;
  height: 100%;
  width: 100%;
  background: transparent;
  padding-left: 35px;
  outline: none;
}

.search-input:focus {
  outline: none;
  border: 0.5px solid #3585e5;
}

.search-input::placeholder {
  font-size: 14.66px;
  color: #9d9d9d;
}
</style>