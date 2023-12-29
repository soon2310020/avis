<template>
  <div>
    <a-dropdown v-model="isShow" :trigger="['click']" style="min-width: 145px">
      <a-menu
        slot="overlay"
        style="max-width: 300px; max-height: 200px; overflow-y: auto"
      >
        <a-menu-item
          v-for="(location, index) in locationList"
          :key="index"
          :class="{ 'selected-item': checkSelected(location) }"
          @click="select(location, index)"
        >
          {{ location.locationTitle }}
        </a-menu-item>
      </a-menu>
      <div class="d-flex" style="align-items: center">
        <span class="hyperlink">{{ selectedLocation.locationTitle }} </span>
        <img
          src="/images/icon/shift-config/chevron-down.svg"
          alt=""
          style="margin: 0 4px"
        />
        <div v-if="locationList.length > 1" class="bonus-badge">
          <span>{{ `+${locationList.length - 1}` }}</span>
        </div>
      </div>
    </a-dropdown>
  </div>
</template>

<script>
module.exports = {
  props: {
    locationList: {
      type: Array,
      default: () => [],
    },
    selectedLocation: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      isShow: false,
    };
  },
  computed: {},
  methods: {
    select(location, index) {
      console.log("select", location, index);
      this.isShow = false;
      this.$emit("select", location);
    },
    checkSelected(location) {
      return location.id == this.selectedLocation.id;
    },
  },
};
</script>

<style>
.bonus-badge {
  /* width: 18px; */
  height: 18px;
  border-radius: 50%;
  background-color: #3491ff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  padding: 0 3px 0 1px;
}
</style>
