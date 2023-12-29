<template>
  <div v-show="value" class="success-toast">
    <div class="success-toast-content position-relative">
      <b>Success</b>
      <div style="margin-top: 5px">
        {{ downtimeName }} Downtime Alert has been marked as ‘read’.
      </div>
      <div class="exit-button">
        <img
          @click="closeSuccess()"
          src="/images/icon/category/exit.svg"
          alt=""
        />
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "toast",
  components: {},
  props: {
    resources: Object,
    value: Boolean,
    downtimeName: String,
  },
  data() {
    return {};
  },
  computed: {},
  methods: {
    closeSuccess() {
      this.$emit("close-success");
    },
  },
};
</script>

<style scoped>
.success-toast {
  position: absolute;
  bottom: 160px;
  right: 50%;
  transform: translate(50%, 0);
  font-size: 12px;
}
.success-toast-content {
  border: 1px solid #43a5ff;
  background: #e6f7ff;
  border-radius: 2px;
  padding: 8px 14px 8px;
  line-height: 100%;
  color: var(--blue);
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
