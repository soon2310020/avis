<template>
  <div
    :style="{ 'background-color': getRequestedColor() }"
    class="avatar__container"
  >
    <a-tooltip placement="bottom">
      <template slot="title">
        <div style="padding: 6px 8px; font-size: 13px">
          <div>
            <b>{{ item.name }}</b>
          </div>
          <div>
            Company: <span v-if="item.company">{{ item.company.name }}</span>
          </div>
          <div>Department: {{ item.department }}</div>
          <div style="margin-bottom: 15px">Position: {{ item.position }}</div>
          <div>Email: {{ item.email }}</div>
          <div>Phone: {{ item.mobileNumber }}</div>
        </div>
      </template>
      <div>
        {{ convertName() }}
      </div>
    </a-tooltip>
  </div>
</template>

<script>
module.exports = {
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    convertName() {
      const name = this.item.name;
      return Common.getAvatarName(name);
    },
    getRequestedColor() {
      const name = this.item.name;
      const id = this.item.id ? this.item.id : 0;
      return Common.getRequestedColor(name, id);
    },
  },
};
</script>

<style scoped>
.avatar__container {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  width: 25px;
  height: 25px;
  border-radius: 25px;
  cursor: context-menu;
}
</style>