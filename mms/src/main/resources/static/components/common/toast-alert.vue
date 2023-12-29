<template>
  <div v-show="value" class="success-toast">
    <div class="success-toast-content position-relative">
      <b>{{ title }}</b>
      <div style="margin-top: 5px;">
        {{ content }}
      </div>
      <div class="exit-button">
        <img @click="closeSuccess()" src="/images/icon/category/exit.svg" alt="">
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "toast",
  components: {
    "project-card": httpVueLoader("/components/category/category-library/project-card.vue"),
  },
  props: {
    resources: Object,
    value: Boolean,
    type: String,
    title: String,
    content: String
  },
  data() {
    return {
    };
  },
  computed: {
  },
  methods: {
    closeSuccess () {
      this.$emit('close-toast')
    }
  },
};
</script>

<style scoped>
.success-toast {
  position: absolute;
  bottom: 9px;
  right: 50%;
  transform: translate(50%, 0);
  font-size: 12px;
  z-index: 5000;
}
.success-toast-content {
  border: 1px solid #3491FF;
  background: #E6F7FF;
  border-radius: 2px;
  padding: 8px 14px;
  line-height: 100%;
  color: #3491FF;
}
.exit-button {
  position: absolute;
  top: 6px;
  right: 8px;
}
.exit-button img {
  cursor: pointer;
}
</style>