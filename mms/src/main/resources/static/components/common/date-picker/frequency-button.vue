<template>
  <div>
    <div class="block-button">
      <a
        v-for="(button, index) in getListButton"
        :key="index"
        class="select-type-button"
        href="javascript:void(0)"
        :class="{ 'selected-button': selectedFrequency == button.type }"
        @click="changeFrequency(button)"
      >
        {{ button.title }}
      </a>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    isRange: {
      type: Boolean,
      default: () => false,
    },
    listButton: {
      type: Array,
      default: () => [],
    },
    selectedFrequency: {
      type: String,
      default: () => "",
    },
  },
  computed:{
      getListButton(){
          return this.listButton
      }
  },
  methods: {
    changeFrequency(frequency) {
      this.$emit("change-frequency", frequency);
    },
  },
};
</script>

<style scoped>
.block-button {
  display: flex;
  flex-direction: column;
  margin-top: 30px;
}
.select-type-button {
  font-size: 13px;
  color: #52a1ff;
  border: 1px solid #52a1ff;
  background-color: #fff;
  border-radius: 2px;
  margin-bottom: 20px;
  text-decoration: none;
  text-align: center;
  text-transform: capitalize;
  width: 184px;
  height: 24px;
}

.select-type-button:hover {
  background-color: #ebf4ff;
}

.selected-button {
  background-color: #52a1ff;
  color: #ffffff;
}

.selected-button:hover {
  background-color: #3994ff;
  color: #ffffff;
}
</style>