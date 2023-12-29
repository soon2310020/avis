<template>
  <div
    style="flex-wrap: wrap; margin-bottom: 10px;"
    class="btn-group btn-group-toggle"
    data-toggle="buttons"
  >
    <label
      v-for="(option, index) in optionList"
      :key="index"
      class="btn switch-btn"
      :class="{ active: selectedOption === option }"
      @click.prevent="handleClick(option)"
    >
      <input type="radio" name="options" :value="option" autocomplete="off" />
      <span>{{ option }}</span>
    </label>
  </div>
</template>

<script>
module.exports = {
  props: {
    selectedOption: {
      type: String,
      default: () => "",
    },
    optionList: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    handleClick(option) {
      this.$emit("change-option", option);
    },
  },
};
</script>

<style scoped>
.switch-btn {
  padding: 5px 10px;
  border: 1px solid #d0d0d0;
  font-size: 12px;
  color: #8b8b8b;
}

.switch-btn:hover {
  background: #deedff;
  color: #3585e5;
}

.switch-btn.active {
  border: 1px solid #3491ff;
  background: rgb(222, 237, 255);
  color: #3491ff;
}
</style>