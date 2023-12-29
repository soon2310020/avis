<template>
  <div class="priority__container">
    <div class="priority__badge" :class="getPriorityColor"></div>
    <span class="priority__text">
      <slot></slot>
    </span>
  </div>
</template>

<script>
module.exports = {
  props: {
    color: {
      type: String,
      default: () => "green",
      validator: (value) => {
        return [
          "blue",
          "red",
          "yellow",
          "green",
          "purple",
          "grey",
          "dark-grey",
          "pink",
        ].includes(value);
      },
    },
  },
  computed: {
    getPriorityColor() {
      return `badge--${this.color}`;
    },
  },
};
</script>

<style scoped>
.priority__container {
  display: flex;
  align-items: center;
}
.priority__badge {
  height: 15px;
  min-width: 15px;
  border-radius: 15px;
  margin-right: 6px;
}
.priority__text {
  text-transform: capitalize;
  font-size: 14.66px;
  color: #4b4b4b;
}
</style>
