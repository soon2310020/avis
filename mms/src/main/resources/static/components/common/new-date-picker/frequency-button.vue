<template>
  <div>
    <div class="block-button">
      <button type="button"
        v-for="(button, index) in getListButton"
        :key="index"
        class="select-type-button"
        :class="{ 'selected-button': selectedFrequency == button }"
        @click="changeFrequency(button)"
      >
        {{ getButtonTitle(button) }}
      </button>
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
              return this.listButton.filter(item => !item.includes('RANGE'))
      }
  },
  methods: {
    changeFrequency(frequency) {
      this.$emit("change-frequency", frequency);
    },
    getButtonTitle(rawTitle){
      return rawTitle.replace('_', ' ').replace('RANGE', '').toLowerCase()
    }
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
  font-size: 14.66px;
  display: flex;
  align-content: center;
  justify-content: center;
  align-items: center;
  color: #888888;
  border: 0.5px solid #D0D0D0;
  background-color: #fff;
  border-radius: 3px;
  margin-bottom: 10px;
  text-decoration: none;
  text-align: center;
  text-transform: capitalize;
  width: 171px;
  height: 25px;
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