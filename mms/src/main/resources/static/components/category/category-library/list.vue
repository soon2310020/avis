<template>
  <div>
    <div class="modal-body__header d-flex">
      <div class="back-icon" @click="back()">
        <img src="/images/icon/category/back-arrow.svg" alt="" />
      </div>
      <div>
        <div class="modal-body__title">
          <div class="title__name">Category</div>
          <span class="title__detail content__hyper-link">{{
            category.name
          }}</span>
        </div>
        <div class="modal-body__title">
          <div class="title__name">Product</div>
          <span class="title__detail">{{ project.name }}</span>
        </div>
      </div>
    </div>
    <div class="modal-body__container">
      <div class="modal-body__content">
        <div class="modal-body__content__table">
          <data-table
            :type="type"
            :data-list="dataList"
            :field-list="fieldList"
            :page-size="12"
            :size="'large'"
            :project="project"
            @change-route="handleChangeRoute"
          ></data-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    type: {
      type: String,
      default: () => "",
    },
    project: {
      type: Object,
      default: () => ({
        id: null,
        maximumCapacity: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      }),
    },
    category: Object,
    dataList: {
      type: Array,
      default: () => [],
    },
    fieldList: {
      type: Array,
      default: () => [],
    },
  },
  components: {
    "data-table": httpVueLoader(
      "/components/category/category-library/project-data-table.vue"
    ),
  },
  methods: {
    back() {
      this.$emit("change-step", "backward");
    },
    handleChangeRoute(route){
      this.$emit('change-route', route)
    }
  },
};
</script>

<style>
</style>